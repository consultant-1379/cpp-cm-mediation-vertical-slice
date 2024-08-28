Feature: Adding nodes, positive and negative flows

    Background:
        Given the node is added
          And the node is synchronized

      Scenario: Change IP action will correctly change the "ipAddress" attribute on CppConnectivityInformation MO
        Given the system contains a NetworkElement MO named "ipsec1"
          And it was created using namespace "OSS_NE_DEF", version "2.0.0", and the following attributes:
              | Name                          | Value            | Type    |
              | networkElementId              | ipsec1           | String  |
              | ossModelIdentity              | 6607-651-025     | String  |
              | neType                        | ERBS             | String  |
              | platformType                  | CPP              | String  |
              | ossPrefix                     |                  | String  |
          And "ipsec1" contains a child CppConnectivityInformation named "cppConnInfo"
          And it was created using namespace "CPP_MED", version "1.0.0", and the following attributes:
              | Name                          | Value            | Type    |
              | CppConnectivityInformationId  | cppConnInfo      | String  |
              | ipAddress                     | 1.2.3.4          | String  |
              | port                          | 2345             | Integer |
          And "ipsec1" has network security
          And it was created using namespace "OSS_NE_SEC_DEF", version "4.1.1", and the following attributes:
              | Name                          | Value            | Type    |
              | NetworkElementSecurityId      | 1                | String  |
              | summaryFileHash               | hash123          | String  |
              | rootUserName                  | enmuser          | String  |
              | rootUserPassword              | TestPassw0rd     | String  |
              | normalUserName                | enmuser          | String  |
              | normalUserPassword            | TestPassw0rd     | String  |
              | secureUserName                | enmuser          | String  |
              | secureUserPassword            | TestPassw0rd     | String  |
         When we request to change "ipsec1" ip address from "1.2.3.4" to "0.0.0.0"
         Then no error will occur
          And "cppConnInfo" will now have attribute "ipAddress" with value "0.0.0.0"

      Scenario: Executing "install" action twice in a row will work and yield different results
        Given the system contains a random fdn of type "UpgradePackage"
         When we request action "install" on this fdn without any arguments
         Then no error will occur
          And the result of the action will not be null
          And the result of the action will be a number
          And if we request action "install" on this fdn without any arguments again
         Then no error will occur
          And the result of the 2nd action will not be null
          And the result of the 2nd action will be a number
          And the result of the 2nd action will be the result of the 1st action plus 1