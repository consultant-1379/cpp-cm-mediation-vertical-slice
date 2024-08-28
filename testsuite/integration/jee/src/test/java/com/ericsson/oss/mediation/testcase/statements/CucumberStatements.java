package com.ericsson.oss.mediation.testcase.statements;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.mediation.test.service.mo.CliService;
import com.ericsson.oss.mediation.test.service.sync.NodeSyncService;

import cucumber.api.java.en.Then;

public abstract class CucumberStatements {

    @Inject
    protected CliService cliService;

    @Inject
    protected NodeSyncService nodeSyncService;

    @Inject
    protected Logger logger;

    private static Throwable caughtException;

    public <T> T executeWithTry(final Callable<T> callable) {
        try {
            return callable.call();
        } catch (final Throwable t) {
            caughtException = t;
            return null;
        }
    }

    public Throwable getCaughtException() {
        return caughtException;
    }

    public boolean hasException() {
        return (caughtException != null);
    }

    @Then("^(an|no) error will occur$")
    public void validate_error(final String condition) {
        if ("an".equals(condition)) {
            assertNotNull("Exception was expected, but wasn't present", caughtException);
        } else {
            if (caughtException != null) {
                throw new AssertionError(
                                "An exception was present when none was expected", 
                                caughtException);
            }
        }
    }

    public final void clear() {
        caughtException = null;
        doClear();
    }

    protected void doClear() {
        
    }
}
