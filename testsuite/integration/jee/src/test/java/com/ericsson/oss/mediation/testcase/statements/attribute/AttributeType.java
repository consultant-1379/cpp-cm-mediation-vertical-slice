package com.ericsson.oss.mediation.testcase.statements.attribute;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.mediation.test.util.fdn.Fdn;

public enum AttributeType {

    BOOLEAN {
        public boolean matches(final String attributeValue, final String attributeType) {
            return ("Boolean".equals(attributeType));
        }

        public Boolean extract(final String attributeValue, final String attributeType, final Map<String, Fdn> fdns) {
            return Boolean.valueOf(attributeValue);
        }
    },

    FDN_NAME {
        private final Pattern pattern = Pattern.compile("([^']+)'s fdn name");
        public boolean matches(final String attributeValue, final String attributeType) {
            return (attributeType.startsWith("Fdn")) && (attributeValue.matches("^([^']+)'s fdn name$"));
        }

        public Object extract(final String attributeValue, final String attributeType, final Map<String, Fdn> fdns) {
            final String result = getFromPattern(fdns, attributeValue, pattern, 1).getName();
            return ((attributeType.endsWith("List")) ? Arrays.asList(result) : result);
        }
    }, 

    FDN_REF {
        private final Pattern pattern = Pattern.compile("([^']+)'s fdn");
        public boolean matches(final String attributeValue, final String attributeType) {
            return (attributeType.startsWith("Fdn")) 
                && (attributeValue.matches("^([^']+)'s fdn$"));
        }

        public Object extract(final String attributeValue, final String attributeType, final Map<String, Fdn> fdns) {
            final String result = getFromPattern(fdns, attributeValue, pattern, 1).toString();
            return ((attributeType.endsWith("List")) ? Arrays.asList(result) : result);
        }
    },

    INTEGER {
        public boolean matches(final String attributeValue, final String attributeType) {
            return ("Integer".equals(attributeType));
        }

        public Integer extract(final String attributeValue, final String attributeType, final Map<String, Fdn> fdns) {
            return Integer.valueOf(attributeValue);
        }
    };

    public abstract boolean matches(final String attributeValue, final String attributeType);
    public abstract Object extract(final String attributeValue, final String attributeType, final Map<String, Fdn> fdns);

    private static Fdn getFromPattern(
                                final Map<String, Fdn> fdns, 
                                final String text, 
                                final Pattern p, 
                                final int idx) {

        final Matcher matcher = p.matcher(text);
        if (matcher.find()) {
            return fdns.get(matcher.group(1));
        }

        return null;
    }
}
