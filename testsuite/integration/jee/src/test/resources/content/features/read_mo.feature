Feature: Reading MOs from DPS

    Scenario: Reading a non-persisted attribute from an MO will return the attribute's value from the node and not from the Database
       Given the node is added
         And the node is synchronized
        When we request the attributes of an MO of type "EUtranCellFDD", namespace "ERBS_NODE_MODEL"
         And we choose a random non-persisted attribute
        Then a read of that attribute using the persistence layer will return null
         But a read of that attribute using the mediation layer will return the attribute's value

    Scenario: Reading several non-persisted attributes from an MO will return the attributes values from the node and not from the Database
       Given the node is added
         And the node is synchronized
        When we request the attributes of an MO of type "EUtranFreqRelation", namespace "ERBS_NODE_MODEL"
        Then a read of those attributes using the persistence layer will return null
         But a read of those attributes using the mediation layer will return the attributes values

    Scenario: Reading a non-persisted attribute from an MO will fail if the node is not synchronized
       Given the node is added
         But the node is not synchronized
        When we request the attributes of an MO of type "EUtranCellFDD", namespace "ERBS_NODE_MODEL"
         And we choose a random non-persisted attribute
         And we request a read of that attribute using the mediation layer
        Then an error will occur