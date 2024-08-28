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

package com.ericsson.oss.mediation.test.constants;

public enum Version {

    V1_0_0("1.0.0"), V1_1_0("1.1.0"), V2_0_0("2.0.0"), V3_0_0("3.0.0"), V4_1_0("4.1.0"), V4_1_1("4.1.1");

    private final String version;

    private Version(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public static Version fromString(final String value) {
        for (final Version v : Version.values()) {
            if (v.version.equals(value)) {
                return v;
            }
        }

        return null;
    }
}