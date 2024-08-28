package com.ericsson.oss.mediation.testcase.statements;

import javax.inject.Inject;

import com.ericsson.oss.mediation.test.service.sync.NodeSyncService;

import cucumber.api.java.en.Given;

public class NodeStatements extends CucumberStatements {

    @Inject
    protected NodeSyncService nodeSyncService;

    @Given("^the node is added$")
    public void add_node() {
        nodeSyncService.addNode();
    }

    @Given("^(?:if )?the node (is|is now|is not|is now not) synchronized$")
    public void synchronize_node(final String state) {
        if (("is".equals(state)) || ("is now".equals(state))) {
            nodeSyncService.enableSupervision();
        } else {
            nodeSyncService.disableSupervision();
        }
    }
}