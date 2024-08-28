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

package com.ericsson.oss.mediation.testcase.add;

import static com.ericsson.oss.mediation.test.constants.Type.CM_FUNCTION;
import static com.ericsson.oss.mediation.test.constants.Type.CM_SUPERVISION;
import static com.ericsson.oss.mediation.test.constants.Type.CPP_CONN_INFO;
import static com.ericsson.oss.mediation.test.constants.Type.NETWORK_ELEMENT;
import static com.ericsson.oss.mediation.test.constants.Type.fromString;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.dto.ManagedObjectDto;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.mediation.test.constants.Type;
import com.ericsson.oss.mediation.test.service.mo.ManagedObjectService;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.testcase.BaseJeeTest;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class AddNodeTest extends BaseJeeTest {

    private static final String NON_ROOT_MO_ID = "1";
    private static final String OSS_PREFIX = "ossPrefix";

    @Inject
    private ManagedObjectService moData;

    private Fdn networkElementFdn;
    private Fdn cppInfoFdn;

    @Given("^a FDN of a NetworkElement MO named \"(.+)\"$")
    public void build_network_element_FDN(final String name) {
        networkElementFdn = Fdn
                .builder()
                .type(NETWORK_ELEMENT)
                .name(name)
                .build();
    }

    @Given("^a FDN of a child CppConnectivityInformation MO named \"(.+)\"$")
    public void build_cpp_info_FDN(final String name) {
        cppInfoFdn = Fdn
                .builder()
                .parent(networkElementFdn)
                .type(CPP_CONN_INFO)
                .name(name)
                .build();
    }

    @Given("^the node is added with ossPrefix \"([^\"]*)\"$")
    public void add_node(final String ossPrefix) {
        moStatements.executeWithTry(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                moData.createNetworkElement(networkElementFdn, ossPrefix);
                moData.createCppConnectivityInfo(cppInfoFdn);
                return null;
            }
        });
    }

    @Given("^the node is added with ossPrefix \"([^\"]*)\" and with(?: invalid)? ipAddress \"([^\"]*)\"$")
    public void add_node_withIpAddress(final String ossPrefix, final String ipAddress) {
        moStatements.executeWithTry(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                moData.createNetworkElement(networkElementFdn, ossPrefix);
                moData.createCppConnectivityInfo(cppInfoFdn, ipAddress);
                return null;
            }
        });
    }

    @Given("^the \"([^\"]*)\" exists$")
    public void create_meContext(final String moToCreate) {
        moStatements.executeWithTry(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createMoTree(moToCreate);
                return null;
            }
        });
    }

    @Given("^the user has added (\\d+) nodes to the system under a SubNetwork$")
    public void add_multiple_nodes(final int numberOfNodes) {
        for (int i = 1; i <= numberOfNodes; i++) {
            final String nodeName = "LTE_" + i;
            moStatements.executeWithTry(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final Fdn neFdn = Fdn.builder().fdn("NetworkElement=" + nodeName).build();
                    final Fdn ciFdn = Fdn.builder().parent(neFdn).type(Type.CPP_CONN_INFO).name("1").build();
                    moData.createNetworkElement(neFdn, "SubNetwork=Sub_1");
                    moData.createCppConnectivityInfo(ciFdn, "0.0.0.0");
                    return null;
                }
            });
        }
    }

    @Then("^the \"([^\"]*)\" MO has been successfully created$")
    public void verify_mo_creation_successful(final String createdMo) {

        if ("networkElement".equals(createdMo)) {
            assertNotNull(cliUtil.getManagedObject(networkElementFdn));
        } else if ("cppConnectivityInformation".equals(createdMo)) {
            assertNotNull(cliUtil.getManagedObject(cppInfoFdn));
        } else if ("MeContext".equals(createdMo)) {
            assertNotNull(getMeContextFdn());
        } else if (!createdMo.isEmpty()) {
            final Fdn fdn = Fdn.builder().fdn(createdMo).build();
            assertNotNull(cliUtil.getManagedObject(fdn));
        }
    }

    @Then("^the \"([^\"]*)\" MO has been successfully deleted$")
    public void verify_mo_deletion_successful(final String deletedMo) {

        if ("networkElement".equals(deletedMo)) {
            assertNull(cliUtil.getManagedObject(networkElementFdn));
        } else if (!deletedMo.isEmpty()) {
            final Fdn fdn = Fdn.builder().fdn(deletedMo).build();

            if (fdn.getType().equals("MeContext")) {
                verifyMeContextDeleted(fdn);
            } else {
                assertNull(cliUtil.getManagedObject(fdn));
            }
        }
    }

    @Then("^the Target PO has been successfully deleted$")
    public void verify_target_deletion_successful() {
        boolean isTargetDeleted = false;
        int count = 0;
        final int maxRetries = 9;
        final int waitTime = 1000;
        while ((!isTargetDeleted) && (count < maxRetries)) {
            try {
                Thread.sleep(waitTime);
                final PersistenceObject target = (cliUtil.findTarget(networkElementFdn.getName()));
                isTargetDeleted = target == null;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        assertTrue("Target has not been deleted!", isTargetDeleted);
    }

    private void verifyMeContextDeleted(final Fdn fdn) {
        boolean isMeContextDeleted = false;
        int count = 0;
        final int maxRetries = 9;
        final int waitTime = 1000;
        while ((!isMeContextDeleted) && (count < maxRetries)) {
            try {
                Thread.sleep(waitTime);
                final ManagedObjectDto meContextDto = (cliUtil.getManagedObject(fdn));
                isMeContextDeleted = meContextDto == null;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        assertTrue("MeContext has not been deleted!", isMeContextDeleted);
    }

    @Then("^the \"([^\"]*)\" MO is not created$")
    public void verify_mo_creation_failed(final String createdMo) {

        if ("networkElement".equals(createdMo)) {
            assertNull(cliUtil.getManagedObject(networkElementFdn));
        } else if ("cppConnectivityInformation".equals(createdMo)) {
            assertNull(cliUtil.getManagedObject(cppInfoFdn));
        } else if ("MeContext".equals(createdMo)) {
            assertNull(getMeContextFdn());
        }

    }

    @Then("^with SubNetwork MO \"([^\"]*)\"$")
    public void verify_subNetworkMo_creation(final String createdMo) {
        if (!createdMo.isEmpty()) {
            final Fdn subNetworkFdn = Fdn.builder().fdn(createdMo).build();
            assertNotNull(cliUtil.getManagedObject(subNetworkFdn));
        }

    }

    @Then("^the ossPrefix attribute has been successfully set to \"([^\"]*)\"$")
    public void verify_ossPrefixAttribute_set(final String ossPrefix) {
        assertEquals(ossPrefix, cliUtil.getMoAttribute(networkElementFdn, OSS_PREFIX));
    }

    @Then("^the supervision functions have( not)? been successfully created$")
    public void verify_supervision_functions(final String not) {
        final Fdn cmFunctionFdn = Fdn
                .builder()
                .parent(networkElementFdn)
                .type(CM_FUNCTION)
                .name(NON_ROOT_MO_ID)
                .build();
        final Fdn cmSupervision = Fdn
                .builder()
                .parent(networkElementFdn)
                .type(CM_SUPERVISION)
                .name(NON_ROOT_MO_ID)
                .build();
        if (not != null && not.contains("not")) {
            assertNull(cliUtil.getManagedObject(cmFunctionFdn));
            assertNull(cliUtil.getManagedObject(cmSupervision));
        } else {
            assertNotNull(cliUtil.getManagedObject(cmFunctionFdn));
            assertNotNull(cliUtil.getManagedObject(cmSupervision));
        }
    }

    @Then("^the networkElement association has been succesfully created$")
    public void verify_association() {
        final ManagedObjectDto dto = cliUtil.getManagedObject(getMeContextFdn());
        if (dto == null) {
            fail("The MeContext must exist for CPP nodes");
        } else {
            final ManagedObjectDto association = cliUtil.getAssociatedDto(Fdn.builder().fdn(dto.getFdn()).build(), "networkElementRef");
            assertEquals(networkElementFdn, Fdn.builder().fdn(association.getFdn()).build());
            assertEquals(dto.getFdn(), cliUtil.getMoAttribute(networkElementFdn, OSS_PREFIX));
        }
    }

    @Given("^the user tries to modify the \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void modify_networkElement_attribute(final String attributeName, final String attributeValue) {
        moStatements.executeWithTry(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Map<String, Object> attributes = new HashMap<>();
                attributes.put(attributeName, attributeValue);
                cliUtil.setAttributes(networkElementFdn, attributes);
                return null;
            }
        });
    }

    @Then("the target has been set")
    public void verify_target() {
        assertEquals(getTargetId(networkElementFdn), getTargetId(getMeContextFdn()));
        assertEquals(getTargetId(networkElementFdn), getTargetId(cppInfoFdn));
    }

    @Then("^the user tries to delete all (\\d+) nodes in parallel$")
    public void delete_nodes_in_parallel(final int numberOfNodes) {
        final ExecutorService executors = Executors.newFixedThreadPool(numberOfNodes);
        final List<Future<Void>> futures = new ArrayList<>();
        for (int i = 1; i <= numberOfNodes; i++) {
            final String neFdn = "NetworkElement=LTE_" + i;
            futures.add(executors.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final Fdn cmFunctionFdn = Fdn.builder().fdn(neFdn + ",CmFunction=1").build();
                    cliUtil.performAction(cmFunctionFdn, "deleteNrmDataFromEnm", null);
                    cliUtil.deleteManagedObject(Fdn.builder().fdn(neFdn).build());
                    return null;
                }
            }));
        }
        nodeStatements.executeWithTry(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (final Future<Void> future : futures) {
                    future.get();
                }
                return null;
            }

        });

    }

    @When("the node is deleted")
    public void delete_node() {
        deleteNode(networkElementFdn);
    }

    private void createMoTree(final String moToCreate) {
        if (moToCreate == null || moToCreate.isEmpty()) {
            return;
        }

        final String[] rdns = moToCreate.split(",");
        Fdn parentFdn = null;
        for (final String rdn : rdns) {
            final String[] typeAndName = rdn.split("=");
            final Fdn fdn = Fdn
                    .builder()
                    .parent(parentFdn)
                    .type(fromString(typeAndName[0]))
                    .name(typeAndName[1])
                    .build();

            createMo(fdn);
            parentFdn = fdn;
        }
    }

    private Fdn getMeContextFdn() {
        String mecFdn = (String) cliUtil.getMoAttribute(networkElementFdn, OSS_PREFIX);
        if (!mecFdn.contains("MeContext")) {
            final String neId = (String) cliUtil.getMoAttribute(networkElementFdn, "networkElementId");
            mecFdn += ",MeContext=" + neId;
        }

        mecFdn = removeStart(mecFdn, ",");
        return Fdn.builder().fdn(mecFdn).build();
    }

    private void createMo(final Fdn fdn) {
        final Type type = fromString(fdn.getType());
        if (type == Type.SUB_NETWORK) {
            moData.createSubNetwork(fdn);
        } else if (type == Type.ME_CONTEXT) {
            moData.createMeContext(fdn);
        }
    }

    private long getTargetId(final Fdn fdn) {
        final ManagedObjectDto dto = cliUtil.getManagedObject(fdn);
        return dto.getTargetPoId();
    }

    private void deleteNode(final Fdn networkElementFdn) {
        final Map<String, Object> actionArguments = new HashMap<>();
        final Fdn cmFunctionFdn = Fdn.builder().parent(networkElementFdn).type(CM_FUNCTION).name(NON_ROOT_MO_ID).build();
        try {
            cliUtil.performAction(cmFunctionFdn, "deleteNrmDataFromEnm", actionArguments);
        } catch (final Exception e) {
            logger.warn("The NRM Data may not exist");
        }

        cliUtil.deleteManagedObject(networkElementFdn);
    }
}
