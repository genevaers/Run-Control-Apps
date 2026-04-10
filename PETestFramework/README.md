# GenevaERS-Test-Framework
Exercise the mainframe programs and provide a regression test system.

A quick summary to get you going....

# Prerequisites
The test framework is a Java application built as part of the GenevaERS Java Front end.  

It is intended to be run from z/OS Unix.

Log into your z/OS Unix account.

I suggest via git bash and entering (with your username).

```
ssh username@sp13.svl.ibm.com
```

We make use of the JZos library to manage datasets and jobs. 
As such before running the test framework there are some environment settings that are required.

```
export LIBPATH=/usr/lib/java_runtime64:$LIBPATH
```

The above is best added to your ~/.profile

# Generate and run tests

There are three environment variables that need to be set before running the test framework.
Again you should probably add these to your ~/.profile

```
export GERS_TEST_SPEC_LIST= the YAML file for the specs eg fmspeclist.yaml
export GERS_TEST_HLQ= Your Test dataset HLQ eg GEBT.GTEST
export GERS_ENV_HLQ= the build HLQ eg GEBT.LATEST
```

To run the test app enter

```
./target/bin/gerstf -menu
```

A quick note on the menu options.

For the moment ignore options 1 and 2. They will become live once we figure out how and where to write the test reports.

3. Lists and allows you to select a test to run. Enter the number of the test.  
You can also use this to see the status of the tests.  
A couple of enters gets you back to the main menu.  

4. As above, but for a spec (group of tests).

And just what it says for 5 6 and 7. 

Note. By design the app does not remember the state of tests once you leave the app and restart it.  
It does not take long to run the whole lot.  
And when developing tests use your own specfilelist to concentrate on the tests you're interested in.  

Follow this link for details of [how to create your own tests](./How-To-Create-New-Tests.md).
