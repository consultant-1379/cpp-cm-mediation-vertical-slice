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

public enum Type {

    CM_FUNCTION("CmFunction"), 
    CM_SUPERVISION("CmNodeHeartbeatSupervision"), 
    COM_CONN_INFO("ComConnectivityInformation"), 
    CPP_CONN_INFO("CppConnectivityInformation"),
    DRX_PROFILE("DrxProfile"), 
    ENODEB("ENodeBFunction"),
    EUTRA_NETWORK("EUtraNetwork"), 
    EUTRAN_CELL_FDD("EUtranCellFDD"), 
    EUTRAN_FREQUENCY("EUtranFrequency"),
    EUTRAN_FREQUENCY_RELATION("EUtranFreqRelation"), 
    EUTRANCELL_RELATION("EUtranCellRelation"), 
    EXTERNAL_EUTRANCEL_FDD("ExternalEUtranCellFDD"), 
    LOAD_MODULE("LoadModule"), 
    MANAGED_ELEMENT("ManagedElement"),
    ME_CONTEXT("MeContext"), 
    MIMO_SLEEP_FUNCTION("MimoSleepFunction"), 
    NETWORK_ELEMENT("NetworkElement"), 
    NETWORK_ELEMENT_SECURITY("NetworkElementSecurity"), 
    QCI_TABLE("QciTable"),
    SECTOR_CARRIER("SectorCarrier"),
    SECTOR_EQUIPMENT_FUNCTION("SectorEquipmentFunction"), 
    SECURITY("Security"), 
    SECURITY_FUNCTION("SecurityFunction"),
    SUB_NETWORK("SubNetwork"), 
    SW_MANAGEMENT("SwManagement"), 
    TARGET("Target"),
    TMA_SUB_UNIT("TmaSubUnit"), 
    UPGRADE_PACKAGE("UpgradePackage");

    private final String type;

    private Type(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static Type fromString(final String type) {
        if (type != null) {
            for (final Type t : Type.values()) {
                if (type.equalsIgnoreCase(t.type)) {
                    return t;
                }
            }
        }
        throw new IllegalArgumentException("There is no Enum Type for " + type);
    }

}
