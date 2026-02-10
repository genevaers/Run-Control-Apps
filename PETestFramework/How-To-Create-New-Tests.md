# GenevaERS Test Framework
This describes how to add new tests to the test framework. The tests can be added to spec lists to be run as part of the regression suite.
# Prerequisites
Go through the [README](./README.md) for the PETestFramework and make sure you can run the tests.

# Create new tests
As problems arise and are fixed in the Run Control Apps or the Performance Engine, it is good practice to add a test to the regression suite to exercise the fix.

## Create a test in the GenevaERS Workbench

1. Create a view or set of views that test the specific logic required. 
Keep in mind that the test data (source and reference data) used will have to be uploaded to git, so keep it small.

2. Use the Workbench export function to create an XML file of the view or view folder. This should be placed under the "PETestFramework/xml" directory.

## Add a 'spec' to describe the inputs and outputs

A YAML file defines the inputs and output, and is used to generate the JCL to run the test. This is the 'spec' file. 
Use [Example.yaml](./spec/Example.yaml) as a guide to define the test.

Notes:
 The **name** and the **test name** are used to create the data set names that will contain the JCL, the input, and the job output, hence they must be upper case and less than 8 characters.  
 The **description** will show up when you use the menu option to run the test framework, and will also appear on the reports; make it meaningful.  
 Under **eventfiles** and **reffiles** the ddname must match the ddname defined for the PF in the Workbench. The filename refers to the file name saved in PETestFramework/event/data which is described below.  
 **expectedresult** can be omitted if the test is expected to complete successfully with RC 0. If the test is expected to fail then set with, for example RC: "8"

Tests that are designed to test the Performance Engine functionality (GVBMR95, GVBMR88) should be put under a subdirectory in "PETestFramework/spec/PeTests". Put it in a meaningful category, as the categories are used in the final report.

## Save the input data - event data and reference data

Event (source) and reference files can be preserved in git.  
They must be saved as ebcdic - when transferring from the mainframe data set use binary and the site command "quote site rdw".  
The data can be transferred from a data set to a local directory using, for example, ftp or WinSCP.  

Save in directory "PETestFramework/event/data"  

If the file in event/data is named, for example, SUMMARY, then the generated
source file data set will be \<GERS_TEST_HLQ\>.INPUT.SUMMARY
For this example, in the spec file, set the eventfiles filename to SUMMARY.
  
Edit the file PETestFramework/event/datasetParms.txt and add a line:  
*filename*,LRECL  
where LRECL is the record length of the original source data set.

e.g.   
SUMMARY,100  

## Add the 'spec' to a 'specfile' 

Speclists live in directory PETestFramework/ 

The speclist uses a templateSetName defined in templateSets, and points to one or more test cases. 

You can create your own speclist in the PETestFramework directory for testing e.g. <your name>speclist.yaml

Currently the templateSetName should be set to "gvbrcatoPE.yaml" or "gvbrcatoPEError.yaml", the latter being used for error test cases. See JBaseandExitsspeclist.yaml for an example of how to use these two template sets.

When you have defined your speclist, you can point to it using environment variable GERS_TEST_SPEC_LIST

e.g.
export GERS_TEST_SPEC_LIST=JBasespeclist.yaml

## Create a base file

The base files are used to confirm that the test produced the expected output. The base files are saved in PETestFramework/base and are used to compare to the output - either an extract file or a format file.  

When the spec files and spec list have been created, run the test to generate output data. See the [README](./README.md) for the PETestFramework for instructions on running the tests on z/OS UNIX. The test will report as a fail as there are no base files to compare to at this point.

The output written to the format data set will be automatically copied to PETestFramework/out  
The directory structure below PETestFramework/out follows the same structure as the PETestFramework/spec directory. The directory structure of PETestFramework/base should follow this same structure too.

When you are sure the output generated is correct, copy the output in PETestFramework/out to the matching directory location in PETestFramework/base

## Running tests

It is good practice to clean up the PETestFramework/out directory between tests
e.g.  
```
cd PETestFramework
rm -rf out
```