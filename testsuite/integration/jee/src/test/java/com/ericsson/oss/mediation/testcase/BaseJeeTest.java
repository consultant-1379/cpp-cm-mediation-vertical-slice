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

package com.ericsson.oss.mediation.testcase;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.testware.cucumber.client.Cucumber;
import com.ericsson.oss.mediation.test.service.cleanup.CleanupService;
import com.ericsson.oss.mediation.test.service.mo.CliService;
import com.ericsson.oss.mediation.test.service.sync.NodeSyncService;
import com.ericsson.oss.mediation.test.util.model.ModelService;
import com.ericsson.oss.mediation.test.util.queue.ChannelManagement;
import com.ericsson.oss.mediation.testcase.deployment.Deployments;
import com.ericsson.oss.mediation.testcase.statements.MoStatements;
import com.ericsson.oss.mediation.testcase.statements.NodeStatements;

/**
 * Base test class for all tests in the JEE project. This handles the deployment
 * of common components.
 *
 * @author eshacow
 */
@RunWith(Cucumber.class)
@ArquillianSuiteDeployment
public abstract class BaseJeeTest {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

	@Inject
    protected NodeStatements nodeStatements;

    @Inject
    protected MoStatements moStatements;

    @Inject
    protected CliService cliUtil;

    @Inject
    protected ModelService modelingUtil;

    @Inject
    private CleanupService cleanupService;

    @Inject
    private NodeSyncService nodeSyncService;

    @Inject
    protected Logger logger;

    private final ChannelManagement networkElementNotifications = new ChannelManagement("jms.queue.NetworkElementNotifications");

    @Deployment
    protected static EnterpriseArchive createTestEar() {
        return Deployments.getDeploymentEar();
    }

    @Before
    public void clearBeforeStatements() {
        nodeStatements.clear();
        moStatements.clear();
        networkElementNotifications.pauseChannel();
    }

    @Before
    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            nodeSyncService.addNode();
            nodeSyncService.enableSupervision();
            cleanupService.mark();
        }
    }

    @After
    public void clearAfterStatements() {
        moStatements.clear();
        networkElementNotifications.resumeAndWaitUtilEmpty();
        cleanupService.performCleanup();
    }
}
