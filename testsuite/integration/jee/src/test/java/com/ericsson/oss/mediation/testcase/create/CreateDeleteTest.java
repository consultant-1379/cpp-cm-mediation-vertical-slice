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

package com.ericsson.oss.mediation.testcase.create;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ericsson.oss.itpf.datalayer.dps.dto.ManagedObjectDto;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.testcase.BaseJeeTest;

import cucumber.api.java.en.Then;

public class CreateDeleteTest extends BaseJeeTest {

    static class ExceptionAnswer implements Answer<Object> {
        private final String methodName;

        ExceptionAnswer(final String methodName) {
            this.methodName = methodName;
        }

        public Object answer(final InvocationOnMock invocation) throws Throwable {
            final Object result = invocation.callRealMethod();
            if (methodName.equals(invocation.getMethod().getName())) {
                throw new RuntimeException("Error requested for method:" + methodName);
            }

            return result;
        }
    }

    @Then("^the \"(.+)\" (.+) will (still be|no longer be|be|not be) present in the system$")
    public void validate_module_present(final String name, final String type, final String condition) {
        final Fdn fdn = moStatements.getFdn(name);
        assertNotNull(name + "'s fdn", fdn);

        final ManagedObjectDto mo = cliUtil.getManagedObject(fdn);
        if (("not be".equals(condition)) || ("no longer be".equals(condition))) {
            assertNull(name + " MO", mo);
        } else {
            assertNotNull(name + " MO", mo);
        }
    }

    @Then("^(?:after )?the user requests to delete the \"(.+)\" (MO|MO with a forced exception)$")
    public void request_deletion(final String name, final String condition) {
        final boolean hasException = ("MO with a forced exception".equals(condition));
        execute_deletion(moStatements.getFdn(name), hasException);
    }

    private void execute_deletion(final Fdn lastFdn, final boolean hasException) {
        moStatements.executeWithTry(new Callable<Void>() {
            public Void call() throws Exception {
                if (hasException) {
                    cliUtil.deleteManagedObject(lastFdn, new ExceptionAnswer("deletePo"));
                } else {
                    cliUtil.deleteManagedObject(lastFdn);
                }

                return null;
            }
        });

        if ((hasException) && (!moStatements.hasException())) {
            fail("Expected delete to throw an exception, but none occurred");
        }
    }
}