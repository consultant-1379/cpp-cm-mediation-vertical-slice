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

package com.ericsson.oss.mediation.test.util.fdn;

import com.ericsson.oss.mediation.test.constants.Type;

public final class FdnBuilder {

    private String parent = "";
    private String type;
    private String name;
    private String fdn;

    FdnBuilder() {
        // Package protected constructor to prevent tests from instantiating it
    }

    public Fdn build() {
        final StringBuilder sb = new StringBuilder();
        if (!parent.isEmpty()) {
            sb.append(parent);
            sb.append(",");
        }
        sb.append(getRdn());
        fdn = sb.toString();
        return new Fdn(this);
    }

    public FdnBuilder parent(final Fdn parent) {
        if (parent != null) {
            this.parent = parent.toString();
        }
        return this;
    }

    public FdnBuilder fdn(final String fdn) {
        parent = extractParentFdn(fdn);
        type = extractType(fdn);
        name = extractName(fdn);
        return this;
    }

    private String extractParentFdn(final String fdn) {
        final int lastCommaPosition = fdn.lastIndexOf(",");
        return lastCommaPosition == -1 ? "" : fdn.substring(0, lastCommaPosition);
    }

    private String extractType(final String fdn) {
        final int lastCommaPosition = fdn.lastIndexOf(",");
        final int lastEqualsPosition = fdn.lastIndexOf("=");
        final String type = fdn.substring(lastCommaPosition + 1, lastEqualsPosition);
        Type.fromString(type); // Validate it is a valid type
        return type;
    }

    private String extractName(final String fdn) {
        return fdn.substring(fdn.lastIndexOf("=") + 1);
    }

    public FdnBuilder type(final Type type) {
        this.type = type.toString();
        return this;
    }

    public FdnBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getFdn() {
        return fdn;
    }

    private String getRdn() {
        if (type == null || name == null) {
            throw new IllegalArgumentException("The type and name must be set prior to building");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("=");
        sb.append(name);
        return sb.toString();
    }

}
