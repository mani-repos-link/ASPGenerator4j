# ASPGenerator4j

ASPGenerator4j is a Java-based tool that generates logs from a Declare Process Model, a [declarative modelling language](https://www.win.tue.nl/~hreijers/H.A.%20Reijers%20Bestanden/emmsad09.pdf) used in Business Process Management (BPM).
It can be used to simulate process execution and generate event logs that can be used for process mining analysis.


### Use Cases:
- ASPGenerator4j can be used in various scenarios where event logs are required for process mining analysis. Some of the use cases include:
- Process Discovery: The generated event logs can be used to discover the actual process flows and identify bottlenecks and inefficiencies.
- Process Conformance Checking: The event logs can be used to check if the process adheres to the prescribed business rules and regulations.
- Process Improvement: The generated event logs can be used to identify areas where process improvement is required.
- Process Automation: The tool can be used to automate the generation of event logs for testing and validation purposes.
- Process Simulation: The tool can be used to simulate process execution and generate event logs that can be used for process simulation.


### Context
#### Introduction
In Business Process Management (BPM), a business process is a set of activities that are performed to achieve a specific goal. The process may involve multiple stakeholders, such as customers, suppliers, and employees, and may span multiple departments or organizations. To manage such complex processes, BPM uses models to represent the process flows and business rules.

#### Declarative Modelling
Declarative modelling focuses on specifying what needs to be done, rather than how it needs to be done. In other words, it defines the constraints and conditions that must be satisfied, without specifying the exact sequence of steps that must be followed. Declarative modelling provides a higher level of abstraction, which makes it easier to understand, maintain, and modify the process models. Whereas BPMN and PetriNet are the most common modelling languages to design the process in imperative way but in complex scenario some aspects may remain undiscovered.


#### Declare Modelling Language
The Declare Modelling Language is a declarative modelling language to represent process constraints and business rules. It provides a set of predefined constraints and operators that can be used to specify the relationships between activities in a process. Some common constraints include:
- Precedence: specifies that one activity must be completed before another activity can start.
- Response: specifies that an activity must be performed as a response to another activity.
- Chain: specifies that a sequence of activities must be performed in a specific order.
- Exclusive Choice: specifies that only one of a set of activities can be performed.
- Parallelism: specifies that a set of activities can be performed in parallel.

An example of Declare process model:

```ASP
activity Driving_Test 
bind Driving_Test: Driver, Grade 
activity Getting_License 
bind Getting_License: Driver, Grade 
activity Resit 
bind Resit: Driver, Grade 
activity Test_Failed 
bind Test_Failed: Driver 
Driver: Fabrizio, Mike, Marlon, Raimundas 
Grade: integer between 1 and 5 
Response[Driving_Test, Getting_License] | |T.Grade>3 | 
Response[Driving_Test, Resit] |A.Grade<=2 | | 
Response[Driving_Test, Test_Failed] |A.Grade<=2 | | 
```

Usually, Declare Process Model has 4 types of lines:
- Definition the events( activity, action etc)
- Definition of binding the attributes or resources to the event or events
- Defining the resources values and a value can be only 3 types: integer range, float range and enumeration(strings with , separated)
- Constraint templates


#### TOOL
The tool is based on the ASP and clingo.
##### ASP
Answer Set Programming ([ASP](https://www.mat.unical.it/ricca/downloads/aspapps.pdf)) is a declarative programming paradigm used to solve complex problems. It is based on the idea of defining a set of rules and constraints that describe the problem, and then computing the possible answer sets that satisfy those rules and constraints. ASP has been used in a wide range of applications, including knowledge representation, planning, and optimization.

##### Clingo
[Clingo](https://potassco.org/clingo/) is an open-source ASP solver that is widely used in academic and industrial settings. It is based on the grounder and solver paradigm, where the input program is first grounded to generate a propositional formula, and then the answer sets of the formula are computed using a SAT solver. Clingo also supports optimization, which allows users to find the optimal answer sets based on a given objective function.

ASPGenerator4j uses Clingo as its underlying solver to generate event logs from Declare Models. The tool generates an ASP program that encodes the constraints and conditions specified in the input Declare Model, and then uses Clingo to compute the answer sets that satisfy those constraints. The answer sets are then translated into event logs that can be used for process mining analysis.

### Acknowledgments
ASPGenerator4j is inspired by the RuM toolkit, an open-source framework for process mining.


See the advanced python tool Declare4Py on github with additional features.
