package com.ericsson.oss.mediation.testcase.util;

import static com.ericsson.oss.mediation.testcase.util.TestUtils.toMapList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.testcase.statements.attribute.AttributeType;

import cucumber.api.DataTable;

public class Attributes {

    public final Map<String, Fdn> fdns;

    public Attributes(final Map<String, Fdn> fdns) {
        this.fdns = fdns;
    }

    public Object getValue(final String attributeType, final String attributeValue) {
        for (final AttributeType type : AttributeType.values()) {
            if (type.matches(attributeValue, attributeType)) {
                return type.extract(attributeValue, attributeType, fdns);
            }
        }

        return attributeValue;
    }

    public Map<String, Object> get(final DataTable dataTable) {
        return get(toMapList(dataTable));
    }

    public Map<String, Object> get(final List<Map<String, String>> userAttributes) {
        final Map<String, Object> attributes = new HashMap<>();
        for (int i = 0; i < userAttributes.size(); i++) {
            final Map<String, String> userAttribute = userAttributes.get(i);
            if ("Map".equalsIgnoreCase(userAttribute.get("Type"))) {
                final Map<String, Object> argument = new HashMap<>();

                try {
                    Map<String, String> nextUserAttribute = userAttributes.get(i + 1);
                    while (nextUserAttribute.get("Type").isEmpty()) {
                        final String[] nvt = split(nextUserAttribute.get("Value"));
                        argument.put(
                                nvt[0], 
                                getValue(nvt[2], nvt[1]));

                        nextUserAttribute = userAttributes.get((++i) + 1);
                    }
                } catch (final IndexOutOfBoundsException e) {
                }

                attributes.put(
                        userAttribute.get("Name"), 
                        argument);
            } else {
                attributes.put(
                        userAttribute.get("Name"), 
                        getValue(
                                userAttribute.get("Type"),
                                userAttribute.get("Value")));
            }
        }

        return attributes;
    }

    private String[] split(final String text) {
        final String sub = text.substring(1, text.length() - 1);
        final String[] result = sub.split("\\|");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }

        return result;
    }
}
