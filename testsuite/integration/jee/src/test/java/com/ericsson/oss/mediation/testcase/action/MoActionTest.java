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
package com.ericsson.oss.mediation.testcase.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.ericsson.oss.mediation.test.constants.Type;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.testcase.BaseJeeTest;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class MoActionTest extends BaseJeeTest {

    private List<Object> executionResults = new ArrayList<>();

    @When("we request to change \"(.+)\" ip address from \"(.+)\" to \"(.+)\"")
    public void change_ip_address(final String name, final String oldIp, final String newIp) {
        final Map<String, Object> actionArgs = new HashMap<>();
        actionArgs.put("hash", "hash123");
        actionArgs.put("oldIP", oldIp);
        actionArgs.put("newIP", newIp);
        actionArgs.put("reason", "complete");
        actionArgs.put("nodeID", "LTE02ERBS00001");

        final Fdn nesFdn = moStatements.getFdn(Type.NETWORK_ELEMENT_SECURITY);
        executionResults.add(moStatements.executeWithTry(new Callable<Object>() {
            public Object call() throws Exception {
                return cliUtil.performAction(nesFdn, "changeNodeIpAddress", actionArgs);
            }
        }));
    }

    @When("^(?:if )?we request action \"(.+)\" on this fdn without any arguments(?: again)?")
    public void request_action(final String action) {
        final Fdn fdn = moStatements.getLastUsedFdn();
        executionResults.add(moStatements.executeWithTry(new Callable<Object>() {
            public Object call() throws Exception {
                return cliUtil.performAction(fdn, action, new HashMap<String, Object>());
            }
        }));
    }

    @Then("^the result of the action will not be null$")
    public void validate_action() {
        validate_action(1);
    }

    @Then("^the result of the (\\d+)(?:st|nd|rd|th) action will not be null$")
    public void validate_action(final int index) {
        final Object result = executionResults.get(index - 1);
        assertNotNull("The action did not return any value", result);
    }

    @Then("^the result of the action will be a number$")
    public void validate_result_type() {
        validate_result_type(1);
    }

    @Then("^the result of the (\\d+)(?:st|nd|rd|th) action will be a number$")
    public void validate_result_type(final int index) {
        final Object result = executionResults.get(index - 1);
        assertTrue("The result of action " + index + " was not a number", (result instanceof Number));
    }

    @Then("^the result of the (\\d+)(?:st|nd|rd|th) action will be the result of the (\\d+)(?:st|nd|rd|th) action plus (\\d)$")
    public void validate_result_increment(final int i, final int j, final int increment) {
        assertEquals(
                "The result of action "
                   + i 
                   + " was "
                   + executionResults.get(i - 1) 
                   + ", and of action " 
                   + j 
                   + " was " 
                   + executionResults.get(j - 1),
                executionResults.get(i - 1), 
                ((Number) executionResults.get(j - 1)).intValue() + increment);
    }
}
