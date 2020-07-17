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
Then type: (to run the nodes)
```
call build/nodes/runnodes.bat
```
To start the localhost:8080:
gradlew bootRun