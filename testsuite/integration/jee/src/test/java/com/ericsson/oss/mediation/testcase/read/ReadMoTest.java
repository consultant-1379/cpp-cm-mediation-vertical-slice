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

package com.ericsson.oss.mediation.testcase.read;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;

import com.ericsson.oss.mediation.test.constants.Namespace;
import com.ericsson.oss.mediation.test.constants.Type;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.testcase.BaseJeeTest;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ReadMoTest extends BaseJeeTest {

    private static final String ERROR_READING_ATTRIBUTE_MESSAGE = "Error while reading %s's %s from %s layer";
    private static final Random RANDOM = new Random();

    private List<String> attributes = new ArrayList<>();

    private String currentAttribute;
    private Fdn currentFdn;

    @When("^we request the attributes of an MO of type \"(.+)\", namespace \"(.+)\"$")
    public void request_attributes(final String type, final String namespace) {
        currentFdn = cliUtil.getRandomFdnForType(
                                            Namespace.valueOf(namespace), 
                                            Type.fromString(type));
        
        attributes.addAll(modelingUtil.getNonPersistedAttributeNames(currentFdn));
    }

    @When("^we choose a random non-persisted attribute$") 
    public void choose_random_attribute() {
        currentAttribute = attributes.get(RANDOM.nextInt(attributes.size()));
    }

    @Then("a read of those attributes using the (persistence|mediation) layer will return (null|the attributes values)")
    public void validate_attributes_read(final String layer, final String expected) {
        final Map<String, Object> attributeValues;
        if ("persistence".equals(layer)) {
            attributeValues = cliUtil.getMoAttributesFromPersistenceLayer(currentFdn, attributes);
        } else {
            attributeValues = cliUtil.getMoAttributes(currentFdn, attributes);
        }

        for (final Entry<String, Object> entry : attributeValues.entrySet()) {
            if ("null".equals(expected)) {
                assertNull(String.format(ERROR_READING_ATTRIBUTE_MESSAGE, currentFdn, entry.getKey(), layer), entry.getValue());
            } else {
                assertNotNull(String.format(ERROR_READING_ATTRIBUTE_MESSAGE, currentFdn, entry.getKey(), layer), entry.getValue());
            }
        }
    }

    @Then("a read of that attribute using the (persistence|mediation) layer will return (null|the attribute's value)")
    public void validate_attribute_read(final String layer, final String expected) {
        final Object attribute;
        if ("persistence".equals(layer)) {
            attribute = cliUtil.getMoAttributeFromPersistenceLayer(currentFdn, currentAttribute);
        } else {
            attribute = cliUtil.getMoAttribute(currentFdn, currentAttribute);
        }

        if ("null".equals(expected)) {
            assertNull(String.format(ERROR_READING_ATTRIBUTE_MESSAGE, currentFdn, currentAttribute, layer), attribute);
        } else {
            assertNotNull(String.format(ERROR_READING_ATTRIBUTE_MESSAGE, currentFdn, currentAttribute, layer), attribute);
        }
    }

    @Then("^we request a read of that attribute using the (persistence|mediation) layer$")
    public void request_read(final String layer) {
        moStatements.executeWithTry(new Callable<Object>() {
            public Object call() throws Exception {
                return cliUtil.getMoAttribute(currentFdn, currentAttribute);
            }
        });
    }
}