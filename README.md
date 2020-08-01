# Work Insurance cordapp 

In this CorDapp we would use an `Insurance` state and persist its properties in a
custom table in the database.  The `Insurance` state among other fields also
contains a `WorkerDetail` object, which is the asset being insured. `Claim` objects in the `Insurance` state represents claims
made against the insurance.


### Flows

There are two flow in this cordapp:

1. IssueInsuranceInitiator - Creates the insurance state with the associated worker
information.

2. InsuranceClaimInitiator - Creates the claims against the insurance.


## Usage

### Running the CorDapp

Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
gradlew clean deployNodes
```
Start the nodes by typing:
```
call build/nodes/runnodes.bat
```
Start a Spring Boot server for each node by opening a terminal/command prompt for each node and typing:
```
gradlew run_InsurerAGS_Server
```
```
gradlew run_InsureeHSA_Server
```
```
gradlew run_InsureeHSJ_Server
```


### Connecting to the Database

The JDBC url to connect to the database would be printed in the console in node startup. Use the url to connect to the database using a suitable client. 
The default username is 'sa' and password is '' (blank).

You could download H2 Console to connect to h2 database here:
http://www.h2database.com/html/download.html

