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

/**
 * A concreate representation of a validated FDN.
 *
 * @author eshacow
 */
public class Fdn {

    private final String fdn;
    private final String type;
    private final String parent;
    private final String name;

    /**
     * Gets the utility tool for Building an FDN.
     *
     * @return The FdnBuilder utility.
     */
    public static FdnBuilder builder() {
        return new FdnBuilder();
    }

    Fdn(final FdnBuilder builder) {
        fdn = builder.getFdn();
        type = builder.getType();
        parent = builder.getParent();
        name = builder.getName();
    }

    /**
     * Gets a String representation of this FDN.
     *
     * @return the String fdn.
     */
    public String toString() {
        return fdn;
    }

    /**
     * Gets the type of MO this FDN represents.
     *
     * @return the type of MO.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets a String representation of the parent FDN of this MO.
     *
     * @return The parent FDN, or empty String if this is a root MO.
     */
    public String getParent() {
        return parent;
    }

    public boolean isRoot(){
        return getParent().isEmpty();
    }
    
    /**
     * Gets the name of the MO represented by this FDN.
     *
     * @return the name of the MO.
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fdn == null ? 0 : fdn.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Fdn other = (Fdn) obj;
        if (fdn == null) {
            if (other.fdn != null) {
                return false;
            }
        } else if (!fdn.equals(other.fdn)) {
            return false;
        }
        return true;
    }
}
