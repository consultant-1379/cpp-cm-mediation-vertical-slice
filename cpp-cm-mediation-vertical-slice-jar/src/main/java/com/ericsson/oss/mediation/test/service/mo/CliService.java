/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.test.service.mo;

import static com.ericsson.oss.itpf.datalayer.dps.BucketProperties.SUPPRESS_MEDIATION;
import static com.ericsson.oss.mediation.test.constants.Namespace.DPS;
import static com.ericsson.oss.mediation.test.constants.Type.TARGET;
import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import org.mockito.stubbing.Answer;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.dto.DataTransferObjectHandler;
import com.ericsson.oss.itpf.datalayer.dps.dto.ManagedObjectDto;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryExecutor;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.mediation.test.constants.Namespace;
import com.ericsson.oss.mediation.test.constants.Type;
import com.ericsson.oss.mediation.test.constants.Version;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;

@Stateless
public class CliService {

    @EJB(lookup = DataPersistenceService.JNDI_LOOKUP_NAME)
    private DataPersistenceService dps;

    @EJB(lookup = DataTransferObjectHandler.JNDI_LOOKUP_NAME)
    private DataTransferObjectHandler dtoHandler;

    private final Random randomGenerator = new Random();

    @TransactionAttribute(REQUIRES_NEW)
    public ManagedObjectDto getManagedObject(final Fdn fdn) {
        final DataBucket liveBucket = dps.getDataBucket("live", SUPPRESS_MEDIATION);
        final ManagedObject mo = liveBucket.findMoByFdn(fdn.toString());
        return (ManagedObjectDto) (mo == null ? null : dtoHandler.copy(mo, null));
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void createManagedObject(
                            final Fdn fdn, 
                            final Map<String, Object> attributes, 
                            final Namespace namespace, 
                            final Version version,
                            final boolean suppressMediation) {

        final DataBucket liveBucket = getLiveBucket(suppressMediation);
        liveBucket
            .getMibRootBuilder()
            .parent(getParent(fdn, liveBucket))
            .namespace(namespace.name())
            .type(fdn.getType())
            .name(fdn.getName())
            .version(version.getVersion())
            .addAttributes(attributes)
            .create();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void createManagedObject(
                            final Fdn fdn, 
                            final Map<String, Object> attributes, 
                            final Namespace namespace, 
                            final Version version) {

        createManagedObject(fdn, attributes, namespace, version, false);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void createManagedObject(final Fdn fdn, final Map<String, Object> attributes) {
        final DataBucket liveBucket = getLiveBucket(false);
        liveBucket
            .getManagedObjectBuilder()
            .parent(getParent(fdn, liveBucket))
            .type(fdn.getType())
            .name(fdn.getName())
            .addAttributes(attributes)
            .create();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void setAttributes(final Fdn fdn, final Map<String, Object> attributes) {
        final DataBucket liveBucket = getLiveBucket(false);
        final ManagedObject mo = liveBucket.findMoByFdn(fdn.toString());
        mo.setAttributes(attributes);
    }

    /**
     * Sets the given attributes on the given types underneath the given parent.
     *
     * @param parentFdn
     *            The FDN of the parent MO. If this is {@code null}, all MO's of the given type will be updated.
     * @param namespace
     *            The namespace of the MO's to update.
     * @param type
     *            The type of the MO's to update.
     * @param attributes
     *            The attributes to be updated.
     * @return a {@code Collection} of {@link Fdn} Objects containing the original attributes.
     */
    @TransactionAttribute(REQUIRES_NEW)
    public Collection<Fdn> setAttributes(
                                final Fdn parentFdn, 
                                final Namespace namespace, 
                                final Type type, 
                                final Map<String, Object> attributes) {

        final Collection<Fdn> moHolders = new ArrayList<>();

        final DataBucket liveBucket = getLiveBucket(false);
        final QueryBuilder qb = dps.getQueryBuilder();
        final QueryExecutor qe = liveBucket.getQueryExecutor();
        final Query<?> q;
        if (parentFdn == null) {
            q = qb.createTypeQuery(namespace.toString(), type.toString());
        } else {
            q = qb.createTypeQuery(namespace.toString(), type.toString(), parentFdn.toString());
        }

        for (final Iterator<PersistenceObject> poIterator = qe.execute(q); poIterator.hasNext(); ) {
            final ManagedObject mo = (ManagedObject) poIterator.next();
            moHolders.add(Fdn.builder().fdn(mo.getFdn()).build());
        }

        return moHolders;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public Object performAction(final Fdn fdn, final String actionName, final Map<String, Object> actionArguments) {
        final DataBucket liveBucket = getLiveBucket(false);
        final ManagedObject mo = findMo(fdn, liveBucket);
        if (mo == null) {
            return new Object();
        }

        return mo.performAction(actionName, actionArguments);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public int deleteManagedObject(final Fdn fdn) {
        return deleteManagedObject(fdn, null);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public int deleteManagedObject(final Fdn fdn, final Answer<?> customAnswer) {
        final DataBucket liveBucket = (customAnswer == null) ? 
                                                getLiveBucket(false) : 
                                                getLiveBucket(false, customAnswer);

        final ManagedObject mo = findMo(fdn, liveBucket);
        return mo == null ? 0 : liveBucket.deletePo(mo);
    }

    /**
     * Returns an unmodifiable map of the requested attributes for the ManagedObject specified by the fdn.
     *
     * @param fdn
     *            The FDN of the ManagedObject.
     * @param attributeNames
     *            The specified attributes.
     * @return
     */
    @TransactionAttribute(REQUIRES_NEW)
    public Map<String, Object> getMoAttributes(final Fdn fdn, final Collection<String> attributeNames) {
        return getMoAttributes(fdn, attributeNames, false);
    }

    /**
     * Returns an unmodifiable map of the requested attributes for the ManagedObject specified by the fdn.
     *
     * @param fdn
     *            The FDN of the ManagedObject.
     * @param attributeNames
     *            The specified attributes.
     * @return
     */
    @TransactionAttribute(REQUIRES_NEW)
    public Map<String, Object> getMoAttributesFromPersistenceLayer(final Fdn fdn, final Collection<String> attributeNames) {
        return getMoAttributes(fdn, attributeNames, true);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public Object getMoAttribute(final Fdn fdn, final String attributeName) {
        return getMoAttribute(fdn, attributeName, false);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public Object getMoAttributeFromPersistenceLayer(final Fdn fdn, final String attributeName) {
        return getMoAttribute(fdn, attributeName, true);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public Fdn getRandomFdnForType(final Namespace namespace, final Type type) {
        final List<Fdn> fdns = getFdnsForType(namespace, type);
        if (fdns.isEmpty()) {
            throw new IllegalArgumentException("There are no ManagedObjects for the given type");
        }
        final int random = randomGenerator.nextInt(fdns.size());
        return fdns.get(random);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public List<Fdn> getFdnsForType(final Namespace namespace, final Type type) {
        final List<Fdn> fdns = new ArrayList<>();

        final DataBucket liveBucket = getLiveBucket(false);
        final QueryBuilder qb = dps.getQueryBuilder();
        final QueryExecutor qe = liveBucket.getQueryExecutor();
        final Query<?> q = qb.createTypeQuery(namespace.name(), type.toString());
        for (final Iterator<PersistenceObject> poIterator = qe.execute(q); poIterator.hasNext(); ) {
            final PersistenceObject po = poIterator.next();
            if (po instanceof ManagedObject) {
                final Fdn fdn = Fdn.builder().fdn(((ManagedObject) po).getFdn()).build();
                fdns.add(fdn);
            }
        }

        return fdns;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public ManagedObject findMo(final Fdn fdn) {
        final DataBucket liveBucket = getLiveBucket(true);
        return findMo(fdn, liveBucket);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public ManagedObjectDto getAssociatedDto(final Fdn fdn, final String endpointName) {
        final ManagedObject mo = findMo(fdn);
        final Collection<PersistenceObject> associations = mo.getAssociations(endpointName);
        if (associations.size() != 1) {
            throw new IllegalArgumentException("Dto has " + associations.size() + " associations instead of 1.");
        }

        final ManagedObject associatedMo = (ManagedObject) associations.iterator().next();
        return dtoHandler.copy(associatedMo, null);
    }

    private Object getMoAttribute(final Fdn fdn, final String attributeName, final boolean suppressMediation) {
        final DataBucket liveBucket = getLiveBucket(suppressMediation);
        final ManagedObject mo = findMo(fdn, liveBucket);
        return mo == null ? null : mo.getAttribute(attributeName);
    }

    private Map<String, Object> getMoAttributes(final Fdn fdn, final Collection<String> attributeNames, final boolean suppressMediation) {
        final DataBucket liveBucket = getLiveBucket(suppressMediation);
        final ManagedObject mo = findMo(fdn, liveBucket);
        if (mo == null) {
            return Collections.<String, Object>emptyMap();
        }

        return mo.getAttributes(attributeNames);
    }


    private DataBucket getLiveBucket(final boolean suppressMediation) {
        return getLiveBucket(suppressMediation, null);
    }

    private DataBucket getLiveBucket(final boolean suppressMediation, final Answer<?> defaultAnswer) {
        final DataBucket result = (suppressMediation) ?
                                            dps.getDataBucket("live", SUPPRESS_MEDIATION) :
                                            dps.getLiveBucket();

        return (defaultAnswer == null) ?
                    result :
                    mock(
                        result.getClass(), 
                        withSettings()
                            .spiedInstance(result)
                            .defaultAnswer(defaultAnswer));
    }

    private ManagedObject getParent(final Fdn fdn, final DataBucket bucket) {
        if ((fdn == null) || (fdn.getParent() == null) || (fdn.getParent().equalsIgnoreCase(""))) {
            return null;
        }

        return bucket.findMoByFdn(fdn.getParent());
    }

    private ManagedObject findMo(final Fdn fdn, final DataBucket bucket) {
        return bucket.findMoByFdn(fdn.toString());
    }

    public PersistenceObject findTarget(final String targetName) {
        final QueryBuilder queryBuilder = dps.getQueryBuilder();
        final Query<TypeRestrictionBuilder> query = queryBuilder.createTypeQuery(DPS.toString(), TARGET.toString());
        query.setRestriction(query.getRestrictionBuilder().equalTo("name", targetName));
        final Iterator<PersistenceObject> result = getLiveBucket(false).getQueryExecutor().execute(query);
        return result.hasNext() ? result.next() : null;
    }
}