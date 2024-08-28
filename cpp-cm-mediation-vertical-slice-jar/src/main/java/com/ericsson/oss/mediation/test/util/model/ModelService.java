/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.test.util.model;

import static com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.ReadBehavior.FROM_DELEGATE;
import static com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants.DPS_PRIMARYTYPE;

import java.util.Collection;
import java.util.HashSet;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.dto.ManagedObjectDto;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeAttributeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.mediation.test.service.mo.CliService;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;

/**
 * Utility to abstract interaction with the ModelService.
 */
@Stateless
public class ModelService {

    @Inject
    private com.ericsson.oss.itpf.modeling.modelservice.ModelService modelService;

    @EJB
    private CliService cliUtil;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<String> getNonPersistedAttributeNames(final Fdn fdn) {
        final Collection<String> attributeNames = new HashSet<String>();
        final PrimaryTypeSpecification pts = modelService.getTypedAccess().getEModelSpecification(
                                                                                            getModelInfo(fdn), 
                                                                                            PrimaryTypeSpecification.class);

        final Collection<PrimaryTypeAttributeSpecification> attributeSpecs = pts.getAttributeSpecifications();
        for (final PrimaryTypeAttributeSpecification attribute : attributeSpecs) {
            if (attribute.getReadBehavior() == FROM_DELEGATE) {
                attributeNames.add(attribute.getName());
            }
        }

        return attributeNames;
    }

    private ModelInfo getModelInfo(final Fdn fdn) {
        final ManagedObjectDto dto = cliUtil.getManagedObject(fdn);
        return new ModelInfo(DPS_PRIMARYTYPE, dto.getNamespace(), dto.getType(), dto.getVersion());
    }

}
