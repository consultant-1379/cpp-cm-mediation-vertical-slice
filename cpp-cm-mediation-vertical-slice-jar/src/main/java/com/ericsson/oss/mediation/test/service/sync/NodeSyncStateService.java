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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData;
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent;
import com.ericsson.oss.itpf.sdk.eventbus.annotation.Consumes;

/**
 * Singleton bean which listens for the updating of CmFunction MO's syncStatus.
 * Syncs the node upon creation of the MO.
 * <p>
 * Provides Service methods to get the current sync status. Has a programatically
 * configured timeout.
 *
 * @author eshacow
 */
@Singleton
@Startup
public class NodeSyncStateService {

    @Inject
    private Logger logger;

    private final AtomicBoolean syncChangeCompleted = new AtomicBoolean(false);
    private final AtomicBoolean nodeSynced = new AtomicBoolean(false);

    public boolean isSyncChangeCompleted() {
        return syncChangeCompleted.get();
    }

    public boolean isNodeSynced() {
        return nodeSynced.get();
    }

    public void listenForObjectUpdateOnCreateChannel(
                                        @Observes 
                                        @Consumes(
                                            endpoint = "jms:/topic/dps-notification-event", 
                                            filter = "type = 'CmFunction'") 
                                                final DpsAttributeChangedEvent event) {

        final String fdn = event.getFdn();
        final Set<AttributeChangeData> attributes = event.getChangedAttributes();
        for (final AttributeChangeData attribute : attributes) {
            if (attribute.getName().equals("syncStatus")) {
                final Object newValue = attribute.getNewValue();
                logger.info("syncStatus has changed for {}, status is now {}, was {}", fdn, newValue, attribute.getOldValue());

                final boolean unsynchronized = newValue.equals("UNSYNCHRONIZED");
                final boolean synced = newValue.equals("SYNCHRONIZED");

                this.setSyncChangeCompletion((unsynchronized) || (synced));
                this.setSyncFinished(synced);
            }
        }
    }

    public boolean reset(final boolean syncFinished) {
        logger.info("resetting; syncFinished status is now " + syncFinished);
        this.nodeSynced.set(syncFinished);
        return syncFinished;
	}

    public void reset() {
        logger.info("resetting; syncChangeCompleted and syncFinished status are now false.");
        this.syncChangeCompleted.set(false);
        this.nodeSynced.set(false);
    }

    private void setSyncChangeCompletion(final boolean syncStatus) {
        logger.info("syncChangeCompleted status is now {}", syncStatus);
        this.syncChangeCompleted.compareAndSet(false, syncStatus);
    }

    private void setSyncFinished(final boolean syncStatus) {
        logger.info("syncFinished status is now {}", syncStatus);
        this.nodeSynced.set(syncStatus);
    }
}