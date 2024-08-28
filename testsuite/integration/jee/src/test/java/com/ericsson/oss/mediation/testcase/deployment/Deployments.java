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

package com.ericsson.oss.mediation.testcase.deployment;

import static com.ericsson.oss.itpf.testware.commons.dependencies.Artifacts.resolveArchive;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.ericsson.oss.mediation.testcase.BaseJeeTest;

/**
 * Creates the {@link org.jboss.shrinkwrap.api.Archive} files to be deployed in the tests.
 *
 * @author eshacow
 */
public class Deployments {
    private static final String SFWK_CONFIGURATION_PROPERTIES_LOCATION = System.getProperty(
                                                                                    "service.framework.configuration.properties", 
                                                                                    "ServiceFrameworkConfiguration.properties");

    private Deployments() {
    }

    public static EnterpriseArchive getDeploymentEar() {
        final EnterpriseArchive ear = ShrinkWrap
                                        .create(EnterpriseArchive.class, "cpp-cm-mediation-vertical-slice.ear")
                                        .addAsModule(
                                                resolveArchive(
                                                        JavaArchive.class, 
                                                        "com.ericsson.nms.mediation:cpp-cm-mediation-vertical-slice-jar:?")
                                                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                                                    .addAsResource(SFWK_CONFIGURATION_PROPERTIES_LOCATION))
                                        .addAsModule(
                                                ShrinkWrap
                                                    .create(WebArchive.class, "cpp-cm-mediation-vertical-slice.war")
                                                    .addPackages(true, BaseJeeTest.class.getPackage()))
                                        .addAsManifestResource("jboss-deployment-structure.xml");

        ear.delete("META-INF/application.xml");
        return ear;
    }
}