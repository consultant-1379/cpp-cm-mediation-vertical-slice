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
package com.ericsson.oss.mediation.test.service.versant;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Safely wraps a transaction for the user, closing the EntityManager and commiting the transaction
 * after it is done.
 * 
 * @author emairoo
 */
class VersantTransaction implements Closeable {

    private EntityManager entityManager;

    private final EntityManagerFactory factory;

    VersantTransaction(final EntityManagerFactory factory) {
        this.factory = factory;
    }

    /**
     * Commits (or rolls back) the transaction 
     */
    public void close() throws IOException {
        if ((entityManager != null) && (entityManager.isOpen())) {
            entityManager.close();
        }
    }

    /**
     * Executes an select query against the database, returning a single object
     * @param qlString the query in HQL syntax
     * @return the entity queried
     */
    public Object executeQuery(final VersantQuery query) {
        try {
            return query
                    .build(getEntityManager())
                    .getSingleResult();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Executes an select query against the database, returning a list of object
     * @param qlString the query in HQL syntax
     * @return the entities queried
     */
    public List<?> executeListQuery(final VersantQuery query) {
        try {
            return query
                    .build(getEntityManager())
                    .getResultList();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Executes an update query (e.g. DELETE, UPDATE) against the database
     * @param qlString the query in HQL syntax
     * @return the amount of entities affected by the query
     */
    public int executeUpdate(final VersantQuery query) {
        try {
            return query
                      .build(getEntityManager())
                      .executeUpdate();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private EntityManager getEntityManager() {
        if (this.entityManager == null) {
            this.entityManager = factory.createEntityManager();
            this.entityManager.joinTransaction();
        }

        return this.entityManager;
    }
}