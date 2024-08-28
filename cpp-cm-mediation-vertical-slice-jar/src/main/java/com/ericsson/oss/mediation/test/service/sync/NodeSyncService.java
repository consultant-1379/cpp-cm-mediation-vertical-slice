/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.test.service.sync;

import static com.ericsson.oss.mediation.test.constants.Type.CM_FUNCTION;
import static com.ericsson.oss.mediation.test.constants.Type.CM_SUPERVISION;
import static com.ericsson.oss.mediation.test.constants.Type.CPP_CONN_INFO;
import static com.ericsson.oss.mediation.test.constants.Type.ME_CONTEXT;
import static com.ericsson.oss.mediation.test.constants.Type.NETWORK_ELEMENT;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.mediation.test.service.mo.CliService;
import com.ericsson.oss.mediation.test.service.mo.ManagedObjectService;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.awaitility.core.ConditionTimeoutException;

/**
 * Service to add and sync a node before tests execute.
 *
 * @author eshacow
 */
@Singleton
public class NodeSyncService {

    private static final String NE_ID = "LTE02ERBS00001";
    private static final String NON_ROOT_MO_ID = "1";

    private static final Fdn ME_CONTEXT_FDN = Fdn.builder().type(ME_CONTEXT).name(NE_ID).build();
    private static final Fdn NETWORK_ELEMENT_FDN = Fdn.builder().type(NETWORK_ELEMENT).name(NE_ID).build();
    private static final Fdn CM_SUPERVISION_FDN = Fdn.builder().parent(NETWORK_ELEMENT_FDN).type(CM_SUPERVISION).name(NON_ROOT_MO_ID).build();

    @EJB
    private ManagedObjectService moData;

    @EJB
    private CliService cliUtil;

    @EJB
    private NodeSyncStateService syncStateService;

    @Inject
    private Logger logger;

    public void addNode() {
        if (cliUtil.findMo(NETWORK_ELEMENT_FDN) == null) {
            moData.createNetworkElement(NETWORK_ELEMENT_FDN, ME_CONTEXT_FDN.toString());

            final Fdn connectivityInfoFdn = Fdn.builder().parent(NETWORK_ELEMENT_FDN).type(CPP_CONN_INFO).name(NON_ROOT_MO_ID).build();
            moData.createCppConnectivityInfo(connectivityInfoFdn);
        } else if (!syncStateService.isNodeSynced()) {
            enableSupervision();
        }
    }

    public void resync() {
        syncStateService.reset();

        final Fdn cmFunctionFdn = Fdn.builder().parent(NETWORK_ELEMENT_FDN).type(CM_FUNCTION).name(NON_ROOT_MO_ID).build();
        cliUtil.performAction(cmFunctionFdn, "sync", new HashMap<String, Object>());

        waitUntilSyncChangeIsCompleted();
    }

    public void enableSupervision() {
        if (!syncStateService.isNodeSynced() && !isActive()) {
            setActiveState(true);
            waitUntilSyncIsCompleted();
        }
    }

    public void disableSupervision() {
        if (syncStateService.isNodeSynced() || isActive()) {
            setActiveState(false);
            waitUntilSyncChangeIsCompleted();
        }
    }

    private void setActiveState(final boolean state) {
        syncStateService.reset();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("active", state);

        cliUtil.setAttributes(CM_SUPERVISION_FDN, attributes);
    }

    private boolean isActive() {
        final Object activeAttribute = cliUtil.getMoAttribute(CM_SUPERVISION_FDN, "active");
        return syncStateService.reset(activeAttribute instanceof Boolean && (Boolean) activeAttribute);
    }

    private void waitUntilSyncChangeIsCompleted() {
        try {
            Awaitility
                    .await()
                    .pollDelay(Duration.ONE_SECOND)
                    .atMost(Duration.TWO_MINUTES)
                    .until(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return syncStateService.isSyncChangeCompleted();
                        }
                    });
        } catch (final ConditionTimeoutException timeout) {
            logger.error("======= THE NODE SYNC CHANGE REQUEST DID NOT COMPLETE WITHIN 2 MINUTES =======");
        }
    }

    private void waitUntilSyncIsCompleted() {
        try {
            Awaitility
                    .await()
                    .pollDelay(Duration.ONE_SECOND)
                    .atMost(Duration.TWO_MINUTES)
                    .until(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return syncStateService.isNodeSynced();
                        }
                    });
        } catch (final ConditionTimeoutException timeout) {
            logger.error("======= THE NODE SYNC DID NOT COMPLETE WITHIN 2 MINUTES =======");
        }
    }
}
