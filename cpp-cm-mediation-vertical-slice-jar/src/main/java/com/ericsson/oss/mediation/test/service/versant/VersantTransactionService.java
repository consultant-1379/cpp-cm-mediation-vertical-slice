package com.ericsson.oss.mediation.test.service.versant;

import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;

@Stateless
public class VersantTransactionService {

    @Resource(lookup = "java:jboss/versant-emf")
    private EntityManagerFactory factory;

    @Inject
    private Logger logger;

    @TransactionAttribute(REQUIRES_NEW)
    public void executeUpdate(final VersantQuery... queries) {
        try (final VersantTransaction vt = new VersantTransaction(factory)) {
            for (final VersantQuery query : queries) {
                final int result = vt.executeUpdate(query);
                logger.info("The query '{}' affected {} entities", query, result);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @TransactionAttribute(REQUIRED)
    public Object executeQuery(final VersantQuery query) {
        try (final VersantTransaction vt = new VersantTransaction(factory)) {
            return vt.executeQuery(query);
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @TransactionAttribute(REQUIRED)
    public List<?> executeListQuery(final VersantQuery query) {
        try (final VersantTransaction vt = new VersantTransaction(factory)) {
            return vt.executeListQuery(query);
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
