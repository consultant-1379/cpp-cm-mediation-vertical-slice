/*------------------------------------------------------------------------------
 *******************************************************************************
s * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.testcase.modify;

import static org.junit.Assert.assertEquals;

import static com.ericsson.oss.mediation.test.constants.Namespace.ERBS_NODE_MODEL;
import static com.ericsson.oss.mediation.test.constants.Type.EUTRAN_CELL_FDD;
import static com.ericsson.oss.mediation.testcase.util.TestUtils.toMapList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.testcase.BaseJeeTest;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@SuppressWarnings("unchecked")
public class ModifyMoTest extends BaseJeeTest {

    private static final String EMPTY = "<EMPTY>";
    private Collection<Fdn> modifiedMos;

    @When("^the user tries to modify (.+)'s attribute \"(.+)\" of type (.+) with (value|values) \"(.+)\"$")
    public void modify_attribute(
            final String moName,
            final String attributeName,
            final String attributeType,
            final String valueCondition,
            final String attributeValue) {

        final Object realValue;
        if ("value".equals(valueCondition)) {
            realValue = moStatements.getAttributeValue(attributeType, attributeValue);
        } else {
            realValue = new ArrayList<>();
            for (final String value : attributeValue.split(",\\s*")) {
                if (!EMPTY.equals(value)) {
                    ((List<Object>) realValue).add(moStatements.getAttributeValue(attributeType, value));
                }
            }
        }

        final Map<String, Object> modifiedAttributes = new HashMap<>();
        modifiedAttributes.put(attributeName, realValue);
        modifiedMos = moStatements.executeWithTry(new Callable<Collection<Fdn>>() {
            @Override
            public Collection<Fdn> call() throws Exception {
                if ("".equals(attributeType)) {
                    return cliUtil.setAttributes(null, ERBS_NODE_MODEL, EUTRAN_CELL_FDD, modifiedAttributes);
                }

                cliUtil.setAttributes(moStatements.getFdn(moName), modifiedAttributes);
                return null;
            }
        });
    }

    @When("^the user tries to modify (.+)'s attribute \"(.+)\" with (a list containing the|the) following map:$")
    public void modify_eutrancell_struct_attribute(
            final String moName,
            final String attribute,
            final String type,
            final DataTable dataTable) {

        final Map<String, Object> attributeMap = new HashMap<>();
        for (final Map<String, String> row : toMapList(dataTable)) {
            attributeMap.put(row.get("Key"), moStatements.getAttributeValue(row.get("Type"), row.get("Value")));
        }

        final Map<String, Object> modifiedAttributes = new HashMap<>();
        if ("the".equals(type)) {
            modifiedAttributes.put(attribute, attributeMap);
        } else {
            final List<Map<String, Object>> attributeList = new ArrayList<>();
            attributeList.add(attributeMap);
            modifiedAttributes.put(attribute, attributeList);
        }

        moStatements.executeWithTry(new Callable<Collection<Fdn>>() {
            @Override
            public Collection<Fdn> call() throws Exception {
                cliUtil.setAttributes(moStatements.getFdn(moName), modifiedAttributes);
                return null;
            }
        });
    }

    @When("^the user tries to modify (.+)'s attribute \"(.+)\" with (a list containing the|the) following list:$")
    public void modify_eutrancell_sequence_of_structs_attribute(
            final String moName,
            final String attribute,
            final String type,
            final DataTable dataTable) {
        final List<Object> sequenceOfStructs = new ArrayList<>();
        final Map<String, Object> sequence = moStatements.attributesAsMap(dataTable);
        for (final Object struct : sequence.values()) {
            sequenceOfStructs.add(struct);
        }
        final Map<String, Object> modifiedAttributes = new HashMap<>();
        modifiedAttributes.put(attribute, sequenceOfStructs);
        moStatements.executeWithTry(new Callable<Collection<Fdn>>() {
            @Override
            public Collection<Fdn> call() throws Exception {
                cliUtil.setAttributes(moStatements.getFdn(moName), modifiedAttributes);
                return null;
            }
        });
    }

    @When("^the user tries to modify (.+)'s attribute \"(.+)\" to an empty sequence$")
    public void modify_eutrancell_empty_sequence_of_structs_attribute(
            final String moName,
            final String attribute) {
        final Map<String, Object> modifiedAttributes = new HashMap<>();
        final List<String> attributeList = new ArrayList<>();
        modifiedAttributes.put(attribute, attributeList);
        moStatements.executeWithTry(new Callable<Collection<Fdn>>() {
            @Override
            public Collection<Fdn> call() throws Exception {
                cliUtil.setAttributes(moStatements.getFdn(moName), modifiedAttributes);
                return null;
            }
        });
    }

    @Then("^the system will be altered to reflect that (.+)'s attribute \"(.+)\" of type (.+) now has (value|values) \"(.+)\"$")
    public void validate_modified_attributes(
            final String moName,
            final String attributeName,
            final String attributeType,
            final String valueType,
            final String attributeValue) {

        final Object actualAttributeValue;
        if ("value".equals(valueType)) {
            actualAttributeValue = moStatements.getAttributeValue(attributeType, attributeValue);
        } else {
            actualAttributeValue = new ArrayList<>();
            for (final String value : attributeValue.split(",\\s*")) {
                if (!EMPTY.equals(value)) {
                    ((List<Object>) actualAttributeValue).add(moStatements.getAttributeValue(attributeType, value));
                }
            }
        }

        if (modifiedMos == null) {
            assertEquals(actualAttributeValue, cliUtil.getMoAttribute(moStatements.getFdn(moName), attributeName));
        } else {
            for (final Fdn modifiedMo : modifiedMos) {
                assertEquals(actualAttributeValue, cliUtil.getMoAttribute(modifiedMo, attributeName));
            }
        }
    }

    @Then("^the system will be altered to reflect that (.+)'s attribute \"(.+)\" now has as value (a list containing the|the) following map:$")
    public void validate_eutrancell_struct_attribute(
            final String moName,
            final String attribute,
            final String type,
            final DataTable dataTable) {

        final Map<String, Object> attributeMap = new HashMap<>();
        for (final Map<String, String> row : toMapList(dataTable)) {
            attributeMap.put(row.get("Key"), moStatements.getAttributeValue(row.get("Type"), row.get("Value")));
        }

        final Fdn moFdn = moStatements.getFdn(moName);
        if ("the".equals(type)) {
            assertEquals(attributeMap, cliUtil.getMoAttribute(moFdn, attribute));
        } else {
            final List<Map<String, Object>> attributeMapList = new ArrayList<>();
            attributeMapList.add(attributeMap);
            assertEquals(attributeMapList, cliUtil.getMoAttribute(moFdn, attribute));
        }
    }

    @Then("^the system will be altered to reflect that (.+)'s attribute \"(.+)\" now has as value a list containing the following list:$")
    public void validate_eutrancell_sequence_of_structs_attribute(
            final String moName,
            final String attribute,
            final DataTable dataTable) {
        final List<Object> sequenceOfStructs = new ArrayList<>();
        final Map<String, Object> sequence = moStatements.attributesAsMap(dataTable);
        for (final Object struct : sequence.values()) {
            sequenceOfStructs.add(struct);
        }
        final Fdn moFdn = moStatements.getFdn(moName);
        assertEquals(sequenceOfStructs, cliUtil.getMoAttribute(moFdn, attribute));
    }

    @Then("^the system will be altered to reflect that (.+)'s attribute \"(.+)\" now has as value an empty sequence$")
    public void validate_eutrancell_empty_sequence_of_structs_attribute(
            final String moName,
            final String attribute) {
        final Fdn moFdn = moStatements.getFdn(moName);
        final List<String> attributeMapList = new ArrayList<>();
        assertEquals(attributeMapList, cliUtil.getMoAttribute(moFdn, attribute));
    }
}
