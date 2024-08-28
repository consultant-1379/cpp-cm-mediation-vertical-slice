
package com.ericsson.oss.mediation.test.service.cleanup;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.TemporalType;

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.mediation.test.service.versant.VersantQuery;
import com.ericsson.oss.mediation.test.service.versant.VersantTransactionService;

@Singleton
@SuppressWarnings("unchecked")
public class CleanupService {

    private static final String SELECT_ROOT_FDNS = "SELECT mi.rootFdn FROM MibInfo mi";
    private static final String SELECT_CREATED_TIMES = "SELECT p.createdTime FROM %1$s p ORDER BY p.createdTime DESC";

    private static final String DELETE_MAX_TIME = "DELETE FROM %1$s p WHERE p.createdTime > :date";
    private static final String DELETE_MIB_INFO = "DELETE FROM MibInfo mi WHERE mi.rootFdn NOT IN (%1$s)";
    private static final String DELETE_UNIQUE_OBJECT_COORDINATOR = "DELETE FROM UniqueObjectCoordinatorEntity u WHERE u.uniqueKey NOT LIKE '%Live'";

    @EJB
    private VersantTransactionService transactionService;
    @Inject
    private RetryManager retryManager;

    private VersantQuery[] cleanupQueries;

    public void mark() {
        if (cleanupQueries == null) {
            cleanupQueries = new VersantQuery[] {
                new VersantQuery(DELETE_UNIQUE_OBJECT_COORDINATOR),

                new VersantQuery(
                        String.format(
                                DELETE_MIB_INFO,
                                toList(transactionService.executeListQuery(new VersantQuery(SELECT_ROOT_FDNS))))),

                getDeleteAllAfterCurrentMaxDateQuery("PersistenceObjectEntity"),
            };
        }
    }

    private VersantQuery getDeleteAllAfterCurrentMaxDateQuery(final String tableName) {
        return new VersantQuery(String.format(DELETE_MAX_TIME, tableName))
                .withParameter(
                        "date",
                        getMaxCreationDate(tableName),
                        TemporalType.TIMESTAMP);
    }

    private Date getMaxCreationDate(final String tableName) {
        return first(transactionService
                .executeListQuery(
                        new VersantQuery(
                                String.format(SELECT_CREATED_TIMES, tableName))));
    }

    private static <T> T first(final List<?> list) {
        if (list.size() > 0) {
            return (T) list.get(0);
        }

        throw new IllegalArgumentException("Did not get any items for list");
    }

    private String toList(final List<?> existingFdns) {
        final StringBuilder deleteQuery = new StringBuilder();
        for (final Object existingFdn : existingFdns) {
            if (deleteQuery.length() > 0) {
                deleteQuery.append(",");
            }

            deleteQuery
                    .append("'")
                    .append(existingFdn)
                    .append("'");
        }

        return deleteQuery.toString();
    }

    public void performCleanup() {
        if (cleanupQueries != null) {
            final RetryPolicy retryPolicy = RetryPolicy.builder().retryOn(EJBException.class).attempts(10).build();
            retryManager.executeCommand(retryPolicy, new RetriableCommand<Void>() {
                @Override
                public Void execute(final RetryContext retryContext) throws Exception {
                    transactionService.executeUpdate(cleanupQueries);
                    return null;
                }
            });
        }
    }
}
