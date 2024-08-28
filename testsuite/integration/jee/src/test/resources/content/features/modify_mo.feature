Feature: Modifying MOs from DPS

    Background:
        Given the node is added
          And the node is synchronized
          And the system contains a SectorEquipmentFunction ManagedElement named "testSectorEquipment"
          And it was created using the following attributes:
              | Name                       | Value                     | Type     |
              | SectorEquipmentFunctionId  | testSectorEquipment       | String   |
          And the system also contains a SectorCarrier ENodeBFunction named "testSectorCarrier"
          And it was created using the following attributes:
              | Name                       | Value                     | Type     |
              | SectorCarrierId            | testSectorCarrier         | String   |
              | sectorFunctionRef          | testSectorEquipment's fdn | Fdn      |
          And the system also contains a EUtranCellFDD ENodeBFunction named "testEnodeB"
          And it was created using the following attributes:
              | Name                       | Value                     | Type     |
              | EUtranCellFDDId            | testEnodeB                | String   |
              | sectorCarrierRef           | testSectorCarrier's fdn   | Fdn List |
              | earfcndl                   | 0                         | Integer  |
              | earfcnul                   | 18000                     | Integer  |
              | cellId                     | 100                       | Integer  |
              | physicalLayerCellIdGroup   | 100                       | Integer  |
              | physicalLayerSubCellId     | 2                         | Integer  |
              | tac                        | 60000                     | Integer  |
              | userLabel                  | testEnodeB                | String   |

    Scenario Outline: After changing an attribute from an MO, its value will be persisted accordingly, without errors
         When the user tries to modify testEnodeB's attribute "<Name>" of type <Type> with value "<Value>"
         Then no error will occur
          And the system will be altered to reflect that testEnodeB's attribute "<Name>" of type <Type> now has value "<Value>"
            Examples: Valid attributes
              | Name            | Value                 | Type          |
              | userLabel       | TestUserLabel         | String        |
              | isDlOnly        | true                  | Boolean       |
              | pdcchTargetBler | 25                    | Integer       |
              | cellBarred      | BARRED                | enum          |
              | userLabel       | WildCardTestUserLabel | WildCard      |

    Scenario Outline: After changing a list attribute from an MO, its values will be persisted accordingly, without errors
         When the user tries to modify testEnodeB's attribute "<Name>" of type <Type> with values "<Values>"
         Then no error will occur
          And the system will be altered to reflect that testEnodeB's attribute "<Name>" of type <Type> now has values "<Values>"
            Examples: Valid attributes
              | Name                 | Values                | Type          |
              | includeLcgInMacUeThp | true,true,true,true   | Boolean       |
              | pciConflict          | <EMPTY>               | enum          |

     Scenario: After changing an attribute that consists of a struct, its values will be persisted accordingly, without errors
          When the user tries to modify testEnodeB's attribute "frameStartOffset" with the following map:
              | Key            | Type     | Value |
              | subFrameOffset | Integer  | 2     |
          Then no error will occur
           And the system will be altered to reflect that testEnodeB's attribute "frameStartOffset" now has as value the following map:
              | Key            | Type     | Value |
              | subFrameOffset | Integer  | 2     |

     Scenario: After changing an attribute that consists of a list of structs, its values will be persisted accordingly, without errors
          When the user tries to modify testEnodeB's attribute "pciDetectingCell" with a list containing the following map:
              | Key       | Type     | Value |
              | enbId     | Integer  | 3     |
              | cellId    | Integer  | 3     |
              | mcc       | Integer  | 3     |
              | mnc       | Integer  | 3     |
              | mncLength | Integer  | 3     |
          Then no error will occur
           And the system will be altered to reflect that testEnodeB's attribute "pciDetectingCell" now has as value a list containing the following map:
              | Key       | Type     | Value |
              | enbId     | Integer  | 3     |
              | cellId    | Integer  | 3     |
              | mcc       | Integer  | 3     |
              | mnc       | Integer  | 3     |
              | mncLength | Integer  | 3     |

     Scenario: After changing an attribute that consists of a struct with enum, its values will be persisted accordingly, without errors
          When the user tries to modify testEnodeB's attribute "mappingInfo" with the following map:
              | Key              | Type     | Value         |
              | mappingInfoSIB3  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB4  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB5  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB6  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB7  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB8  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB10 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB11 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB12 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB13 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB15 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB16 | enum     | MAPPED_SI_10  |
          Then no error will occur
           And the system will be altered to reflect that testEnodeB's attribute "mappingInfo" now has as value the following map:
              | Key              | Type     | Value         |
              | mappingInfoSIB3  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB4  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB5  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB6  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB7  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB8  | enum     | MAPPED_SI_10  |
              | mappingInfoSIB10 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB11 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB12 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB13 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB15 | enum     | MAPPED_SI_10  |
              | mappingInfoSIB16 | enum     | MAPPED_SI_10  |

     Scenario: After changing an attribute that consists of a struct with enum from non-empty to empty, its values will be persisted accordingly, without errors
          When the user tries to modify testEnodeB's attribute "eutranCellPolygon" with the following list:
              | Key | Value                                   | Type |
              | 1   |\| Name            \| Value \| Type    \|| Map  |
              |     |\| cornerLongitude \| 0     \| Integer \||      |
              |     |\| cornerLatitude  \| 0     \| Integer \||      |
              | 2   |\| Name            \| Value \| Type    \|| Map  |
              |     |\| cornerLongitude \| 0     \| Integer \||      |
              |     |\| cornerLatitude  \| 0     \| Integer \||      |
          Then no error will occur
           And the system will be altered to reflect that testEnodeB's attribute "eutranCellPolygon" now has as value a list containing the following list:
              | Key | Value                                   | Type |
              | 1   |\| Name            \| Value \| Type    \|| Map  |
              |     |\| cornerLongitude \| 0     \| Integer \||      |
              |     |\| cornerLatitude  \| 0     \| Integer \||      |
              | 2   |\| Name            \| Value \| Type    \|| Map  |
              |     |\| cornerLongitude \| 0     \| Integer \||      |
              |     |\| cornerLatitude  \| 0     \| Integer \||      |
           And the user tries to modify testEnodeB's attribute "eutranCellPolygon" to an empty sequence
          Then no error will occur
           And the system will be altered to reflect that testEnodeB's attribute "eutranCellPolygon" now has as value an empty sequence
