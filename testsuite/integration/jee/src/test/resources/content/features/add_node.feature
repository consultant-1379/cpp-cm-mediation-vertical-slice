Feature: Adding nodes, positive and negative flows

    Background:
        Given a FDN of a NetworkElement MO named "LTE99ERBS00001"
          And a FDN of a child CppConnectivityInformation MO named "1"

    Scenario Outline: The adding of a node with correct ossPrefix will work as expected
          When the node is added with ossPrefix "<OSSPrefix>"
          Then no error will occur
           And the "networkElement" MO has been successfully created
           And the "cppConnectivityInformation" MO has been successfully created
           And the "<MeContext>" MO has been successfully created
           And with SubNetwork MO "<SubNetwork>"
           And the ossPrefix attribute has been successfully set to "<MeContext>"
           And the supervision functions have been successfully created
           And the networkElement association has been succesfully created
           And the target has been set
          When the node is deleted
          Then the "networkElement" MO has been successfully deleted 
           And the "<MeContext>" MO has been successfully deleted
           And the Target PO has been successfully deleted

             Examples:
               | OSSPrefix                             | MeContext                              | SubNetwork               |
               |                                       | MeContext=LTE99ERBS00001               |                          |
               | MeContext=LTE99ERBS00001              | MeContext=LTE99ERBS00001               |                          |
               | SubNetwork=1                          | SubNetwork=1,MeContext=LTE99ERBS00001  | SubNetwork=1             |
               | SubNetwork=1,MeContext=LTE99ERBS00001 | SubNetwork=1,MeContext=LTE99ERBS00001  | SubNetwork=1             |

     Scenario Outline: The adding of a node when an MeContext with the same name exists will work as expected
          When the "<MeContext>" exists
            And the node is added with ossPrefix "<OSSPrefix>"
          Then no error will occur
           And the "networkElement" MO has been successfully created
           And the "cppConnectivityInformation" MO has been successfully created
           And the "<OSSPrefix>" MO has been successfully created
           And the "<MeContext>" MO has been successfully deleted
           And the ossPrefix attribute has been successfully set to "<OSSPrefix>"
           And the supervision functions have been successfully created
           And the networkElement association has been succesfully created
           And the target has been set
           When the node is deleted
          Then the "networkElement" MO has been successfully deleted 
           And the "<MeContext>" MO has been successfully deleted
           And the Target PO has been successfully deleted

              Examples:
                | OSSPrefix                             | MeContext                             |
                | MeContext=LTE99ERBS00001              | SubNetwork=1,MeContext=LTE99ERBS00001 |
                | SubNetwork=1,MeContext=LTE99ERBS00001 | MeContext=LTE99ERBS00001 |

     Scenario Outline: The adding of a node with incorrect ossPrefix will fail
          When the node is added with ossPrefix "<OSSPrefix>"
          Then an error will occur
           And the "networkElement" MO is not created
           And the "cppConnectivityInformation" MO is not created

              Examples:
                | OSSPrefix                             |
                | MeContext=BadMeContextId              |
                | SubNetwork=1,MeContext=BadMeContextId |

     Scenario Outline: The adding of a node with invalid IP Address will fail
          When the node is added with ossPrefix "<OSSPrefix>" and with invalid ipAddress "<IPAddress>"
          Then an error will occur
           And the "networkElement" MO has been successfully created
           And the "<MeContext>" MO has been successfully created
           And the ossPrefix attribute has been successfully set to "<MeContext>"
           And the "cppConnectivityInformation" MO is not created
           And the supervision functions have not been successfully created
           When the node is deleted
          Then the "networkElement" MO has been successfully deleted 
           And the "<MeContext>" MO has been successfully deleted
           And the Target PO has been successfully deleted

             Examples:
               | OSSPrefix            | MeContext                | IPAddress     |
               |                      | MeContext=LTE99ERBS00001 | 192.1hey.1.1, |

     Scenario Outline: The ossPrefix cannot be modified after the NetworkElement is created
          When the node is added with ossPrefix "<OSSPrefix>"
          Then the "networkElement" MO has been successfully created
           And the ossPrefix attribute has been successfully set to "<MeContext>"
           And the user tries to modify the "ossPrefix" with value "<OSSPrefix>"
          Then an error will occur
           When the node is deleted
          Then the "networkElement" MO has been successfully deleted 
           And the "<MeContext>" MO has been successfully deleted
           And the Target PO has been successfully deleted

             Examples:
               | OSSPrefix            | MeContext                |
               |                      | MeContext=LTE99ERBS00001 |

     Scenario Outline: The deletion of multiple MeContexts in parallel under a SubNetwork will be successful
          When the user has added <NumberOfNodes> nodes to the system under a SubNetwork
           And the user tries to delete all <NumberOfNodes> nodes in parallel
          Then no error will occur

             Examples:
               | NumberOfNodes |
               | 10            |