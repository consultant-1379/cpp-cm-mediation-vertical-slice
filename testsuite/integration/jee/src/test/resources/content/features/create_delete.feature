Feature: Creating and deleting Managed Objects, positive and negative flows

    Background:
        Given the node is added
          And the node is synchronized

      Scenario: User can create and delete a LoadModule under normal conditions
        Given the user wants to create a LoadModule MO named "LoadModuleCreateTest"
          And its parent is a random MO of type SwManagement
          And it is created using the following attributes:
              | Name                          | Value                                     | Type    |
              | productData                   |\| Name            \| Value    \| Type   \|| Map     |
              |                               |\| productRevision \| rev      \| String \||         |
              |                               |\| productName     \| name     \| String \||         |
              |                               |\| productInfo     \| info     \| String \||         |
              |                               |\| productNumber   \| 123      \| String \||         |
              |                               |\| productionDate  \| 20150330 \| String \||         |
              | loadModuleFilePath            | testPath                                  | String  |
          Then no error will occur
           And the "LoadModuleCreateTest" LoadModule will be present in the system
           But after the user requests to delete the "LoadModuleCreateTest" MO
          Then the "LoadModuleCreateTest" LoadModule will no longer be present in the system

      Scenario: If LoadModule deletion throws an exception, the deletion is rolled back and the MO is restored
        Given the user wants to create a LoadModule MO named "LoadModuleDeleteWithRollbackTest"
          And its parent is a random MO of type SwManagement
          And it is created using the following attributes:
              | Name                          | Value                                     | Type    |
              | productData                   |\| Name            \| Value    \| Type   \|| Map     |
              |                               |\| productRevision \| rev      \| String \||         |
              |                               |\| productName     \| name     \| String \||         |
              |                               |\| productInfo     \| info     \| String \||         |
              |                               |\| productNumber   \| 123      \| String \||         |
              |                               |\| productionDate  \| 20150330 \| String \||         |
              | loadModuleFilePath            | testPath                                  | String  |
          Then no error will occur
           And the "LoadModuleDeleteWithRollbackTest" LoadModule will be present in the system
           But after the user requests to delete the "LoadModuleDeleteWithRollbackTest" MO with a forced exception
          Then the "LoadModuleDeleteWithRollbackTest" LoadModule will still be present in the system

      Scenario: If user tries to create LoadModule with supervision disabled, an error happens and the MO is not created
        Given the node is not synchronized
          And the user wants to create a LoadModule MO named "LoadModuleCreateWithSupervisionDisabledTest"
          And its parent is a random MO of type SwManagement
          And it is created using the following attributes:
              | Name                          | Value                                     | Type    |
              | productData                   |\| Name            \| Value    \| Type   \|| Map     |
              |                               |\| productRevision \| rev      \| String \||         |
              |                               |\| productName     \| name     \| String \||         |
              |                               |\| productInfo     \| info     \| String \||         |
              |                               |\| productNumber   \| 123      \| String \||         |
              |                               |\| productionDate  \| 20150330 \| String \||         |
              | loadModuleFilePath            | testPath                                  | String  |
          Then an error will occur
           And the "LoadModuleCreateWithSupervisionDisabledTest" LoadModule will not be present in the system

      Scenario: If user tries to delete LoadModule with supervision disabled, an error happens and the MO is not deleted
        Given the user wants to create a LoadModule MO named "LoadModuleDeleteWithSupervisionDisabledTest"
          And its parent is a random MO of type SwManagement
          And it is created using the following attributes:
              | Name                          | Value                                     | Type    |
              | productData                   |\| Name            \| Value    \| Type   \|| Map     |
              |                               |\| productRevision \| rev      \| String \||         |
              |                               |\| productName     \| name     \| String \||         |
              |                               |\| productInfo     \| info     \| String \||         |
              |                               |\| productNumber   \| 123      \| String \||         |
              |                               |\| productionDate  \| 20150330 \| String \||         |
              | loadModuleFilePath            | testPath                                  | String  |
          Then no error will occur
           And the "LoadModuleDeleteWithSupervisionDisabledTest" LoadModule will be present in the system
           But if the node is now not synchronized
           And the user requests to delete the "LoadModuleDeleteWithSupervisionDisabledTest" MO
          Then an error will occur
           And if the node is now synchronized
          Then the "LoadModuleDeleteWithSupervisionDisabledTest" LoadModule will still be present in the system