
package com.ericsson.oss.mediation.testcase.statements;

import static org.junit.Assert.assertEquals;

import static com.ericsson.oss.mediation.test.constants.Namespace.ERBS_NODE_MODEL;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.ericsson.oss.mediation.test.constants.Namespace;
import com.ericsson.oss.mediation.test.constants.Type;
import com.ericsson.oss.mediation.test.constants.Version;
import com.ericsson.oss.mediation.test.util.fdn.Fdn;
import com.ericsson.oss.mediation.test.util.fdn.FdnBuilder;
import com.ericsson.oss.mediation.testcase.statements.attribute.AttributeType;
import com.ericsson.oss.mediation.testcase.util.Attributes;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class MoStatements extends CucumberStatements {

    private final Deque<FdnBuilder> toBeCreated;

    private final Deque<Fdn> createdMos;
    private final Deque<Fdn> existingMos;

    private final Map<String, Fdn> fdns;
    private final Map<String, Fdn> topFdns;

    private final Attributes attributes;

    public MoStatements() {
        toBeCreated = new LinkedList<>();

        createdMos = new LinkedList<>();
        existingMos = new LinkedList<>();

        fdns = new HashMap<>();
        topFdns = new HashMap<>();
        attributes = new Attributes(fdns);
    }

    @Override
    public void doClear() {
        while (!createdMos.isEmpty()) {
            nodeSyncService.enableSupervision();
            final Fdn toDelete = createdMos.poll();
            if (!toDelete.isRoot()) {
                try {
                    cliService.deleteManagedObject(toDelete);
                } catch (final Exception e) {
                    logger.error("An error occurred while trying to delete " + toDelete, e);
                }
            }
        }

        existingMos.clear();
        fdns.clear();
        topFdns.clear();
    }

    @Given("^the user wants to create a (.+) MO named \"(.+)\"$")
    public void create_cell_without_parent(final String type, final String name) {
        add_cell_without_parent(type, name);
    }

    @Given("^the system (?:also contains|contains) a (.+) MO named \"(.+)\"$")
    public void add_cell_without_parent(final String type, final String name) {
        addCellWithParent((Fdn) null, type, name);
    }

    @Given("^its parent is a random MO of type (.+)$")
    public void set_cell_parent(final String parentType) {
        final FdnBuilder builder = toBeCreated.peek();
        builder.parent(cliService.getRandomFdnForType(Namespace.ERBS_NODE_MODEL, Type.fromString(parentType)));
    }

    @Given("^the system (?:also contains|contains) a (.+) ("
            + "CmFunction|CmNodeHeartbeatSupervision|ComConnectivityInformation|CppConnectivityInformation|"
            + "DrxProfile|"
            + "ENodeBFunction|EUtraNetwork|EUtranCellFDD|EUtranFrequency|EUtranFreqRelation|EUtranCellRelation|ExternalEUtranCellFDD|"
            + "LoadModule|"
            + "ManagedElement|MeContext|MimoSleepFunction|"
            + "NetworkElement|NetworkElementSecurity|"
            + "QciTable|"
            + "SectorCarrier|SectorEquipmentFunction|Security|SecurityFunction|SubNetwork|SwManagement|"
            + "TmaSubUnit|"
            + "UpgradePackage) "
            + "named \"(.+)\"$")
    public void add_cell(final String type, final String parentType, final String name) {
        final Fdn parent;
        if (topFdns.containsKey(parentType)) {
            parent = topFdns.get(parentType);
        } else {
            parent = cliService.getRandomFdnForType(ERBS_NODE_MODEL, Type.fromString(parentType));
            topFdns.put(parentType, parent);
        }

        addCellWithParent(parent, type, name);
    }

    @Given("^\"(.+)\" (?:also contains|contains) a child (.+) named \"(.+)\"$")
    public void add_cell_with_parent(final String parentName, final String type, final String name) {
        addCellWithParent(getFdn(parentName), type, name);
    }

    @Given("^the system contains a random fdn of type \"(.+)\"$")
    public void retrieve_random_fdn(final String type) {
        final Fdn fdn = cliService.getRandomFdnForType(ERBS_NODE_MODEL, Type.fromString(type));
        fdns.put(fdn.getName(), fdn);
        existingMos.addFirst(fdn);
    }

    @Given("^\"(.+)\" has network security$")
    public void add_network_security(final String name) {
        final FdnBuilder fdn = Fdn
                .builder()
                .parent(
                        Fdn
                                .builder()
                                .parent(getFdn(name))
                                .type(Type.SECURITY_FUNCTION)
                                .name("1")
                                .build())
                .name("1")
                .type(Type.NETWORK_ELEMENT_SECURITY);

        toBeCreated.addFirst(fdn);
    }

    @Given("^it (?:was|is) created using the following attributes:$")
    public void create_cell(final DataTable dataTable) {
        executeWithTry(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Fdn fdn = createFdn();
                final Map<String, Object> attr = attributes.get(dataTable);
                cliService.createManagedObject(fdn, attr);
                return null;
            }
        });
    }

    @Given("^it was created using namespace \"(.+)\", version \"(.+)\", and the following attributes:$")
    public void create_cell(final String namespace, final String version, final DataTable dataTable) {
        executeWithTry(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Fdn fdn = createFdn();
                final Map<String, Object> attr = attributes.get(dataTable);
                cliService.createManagedObject(fdn, attr, Namespace.valueOf(namespace), Version.fromString(version));
                cliService.findMo(fdn); // TODO: remove this workaround when TORF-124828 is fixed
                return null;
            }
        });
    }

    @Then("\"(.+)\" will(?: now)? have attribute \"(.+)\" with value \"(.+)\"")
    public void validate_mo_attribute(final String name, final String attribute, final String value) {
        final Object actualValue = cliService.getMoAttribute(getFdn(name), attribute);
        assertEquals(value, actualValue == null ? null : actualValue.toString());
    }

    public Object getAttributeValue(final String attributeType, final String attributeValue) {
        for (final AttributeType type : AttributeType.values()) {
            if (type.matches(attributeValue, attributeType)) {
                return type.extract(attributeValue, attributeType, fdns);
            }
        }

        return attributeValue;
    }

    public String getId(final Fdn fdn) {
        for (final Entry<String, Fdn> entry : fdns.entrySet()) {
            if (fdn.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Fdn getLastUsedFdn() {
        return existingMos.getLast();
    }

    public Fdn getFdn(final String name) {
        return fdns.get(name);
    }

    public Fdn getFdn(final Type type) {
        for (final Fdn fdn : fdns.values()) {
            if (Type.fromString(fdn.getType()) == type) {
                return fdn;
            }
        }

        return null;
    }

    public Map<String, Object> attributesAsMap(final DataTable dataTable) {
        return attributes.get(dataTable);
    }

    private Fdn createFdn() {
        final Fdn fdn = toBeCreated.poll().build();
        fdns.put(fdn.getName(), fdn);
        createdMos.addFirst(fdn);
        existingMos.addFirst(fdn);
        return fdn;
    }

    private void addCellWithParent(final Fdn parent, final String type, final String name) {
        final FdnBuilder fdn = Fdn
                .builder()
                .parent(parent)
                .type(Type.fromString(type))
                .name(name);

        toBeCreated.addFirst(fdn);
    }
}
