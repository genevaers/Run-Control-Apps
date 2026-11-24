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

# Running locally (no mainframe)

The framework can also run on Mac, Linux or Windows. It cannot run the mainframe extract (MR95) and format (MR88) jobs.
Instead it compares the run control files RCA generates (VDP, XLT and JLT) with those saved from a previous verified run, the *baseline*.
MR95 and MR88 are driven entirely by those files, so if they are unchanged the verified extract and format outputs are unchanged too.

Local mode is used on any platform other than z/OS, or when `RUNOS=LOCAL` is set.

The local run is a separate step from the z/OS run and does not change it. On z/OS the framework behaves as described above.
A clean local run on its own is not a substitute for the z/OS run. It shows the generated run control files are unchanged from a release already verified on z/OS.

## Build

The framework needs the IBM `jzos` and `sdsf` jars to compile (they are not used at run time locally).
Install them into your local Maven repository once, then build with both profiles so the RCA jar contains the binary record writer:

```
mvn install:install-file -Dfile=ibmjzos.jar   -DgroupId=com.ibm     -DartifactId=jzos -Dversion=2.4.8 -Dpackaging=jar
mvn install:install-file -Dfile=sdsf-2.4.jar  -DgroupId=com.ibm.zos -DartifactId=sdsf -Dversion=2.4   -Dpackaging=jar
mvn install -DskipTests -P "windows,z/os"
```

The RCA jar to test is `RCApps/target/rcapps-<version>-jar-with-dependencies.jar`.
Put a copy named `rcapps-latest.jar` in a directory and point `GERS_RCA_JAR_DIR` at it.

## Run

Work from a copy of the PETestFramework directory. The run writes `jcl`, `Config` and `out` under it, and the baseline goes to `baseline` (or `GERS_BASELINE_DIR`).

1. Create the baseline using the RCA release that is already verified:

```
export GERS_TEST_SPEC_LIST=JBasespeclistV4.yaml
export GERS_RCA_JAR_DIR=/path/to/dir/with/verified/rcapps-latest.jar
/path/to/target/bin/gerstf -baseline
```

2. Test the new RCA release against it:

```
export GERS_RCA_JAR_DIR=/path/to/dir/with/new/rcapps-latest.jar
/path/to/target/bin/gerstf
```

Results are in `out/fmoverview.html`, `.txt` and `.csv`. The exit code is 0 only if every test passed.
For a test that fails, `out/<test>/` holds the generated files, `RCAOUT` (console output), `RCALOG` and a `<file>.diff` naming the first differences.

Result messages:

| Message | Meaning |
|---|---|
| `pass run control files match baseline` | VDP, XLT and JLT are the same as the baseline |
| `fail compare VDPNEW XLTNEW` | the named files differ from the baseline |
| `fail no baseline` | no baseline has been saved for this test |
| `fail RCA rc=n` / `fail RCA timed out` | RCA did not complete successfully |
| `pass baseline saved` | `-baseline` run |

When comparing, digits in the first three records of each file (the generation time stamps) are ignored. All other bytes must match.
Every view in a test shares the test result, since RCA processes the whole test at once.

## Baselines are not stored in git

The baseline is regenerated from the verified release each time (step 1 above), so no VDP, XLT or JLT files are committed.
The `baseline` directory is in `.gitignore`. It is about 45 MB for the 195 or so tests in the speclists below.
Whether to keep baselines in the repository is still open. This approach, rebuilding them from the previous verified release, is the one being tried first.

If you are testing a release that follows a verified one, build the verified release from its commit (not from `main`, which may already hold later changes) and use that jar for the baseline.

## Example: RC27 against RC25

RC25 is the verified release (commit `7d100ee4`, PR #293). RC27 differs from it only by dependency updates:
`postgresql` 42.7.12 to 42.7.14 and `jackson-databind` and `jackson-dataformat-yaml` 3.2.3 to 3.2.4.
Baselines were saved with the RC25 jar and the same speclists then run with the RC27 jar. Every comparable test passed.

| Speclist | Tests compared | Passed |
|---|---|---|
| `JBasespeclistV4.yaml` | 40 | 40 |
| `JBaseandExitsspeclist.yaml` | 95 | 95 |
| `JDateTestspeclist.yaml` | 2 | 2 |
| `JFormatspeclist.yaml` | 10 | 10 |
| `JDemospeclist.yaml` | 4 | 4 |
| `JMR91Basespeclist.yaml` | 47 | 44 |

The three that did not pass in `JMR91Basespeclist` (`STRFNC`, `STRFNR` and `STRFNP`, from `PeTests/Function/STRFN.yaml`) fail with `RCA rc=8` on RC25 as well.
RCA reports a syntax error on `=` and `>`, so they were not valid tests for RCA in the first place. `JSTRFN.yaml` is the Java compiler version and passes.
The speclists overlap, so the number of distinct tests is a little lower than the total.
`JDB2speclist.yaml` was not run; it needs Db2.

## Reading metadata from Postgres

RCA can read the views straight from a Workbench Postgres database (`INPUT_TYPE=POSTGRES`). This exercises the `postgresql` driver, which the WBXML tests do not.
The framework does not drive this yet. To check it by hand:

```
INPUT_TYPE=POSTGRES
DB_SERVER=localhost
DB_PORT=5432
DB_DATABASE=<database>
DB_SCHEMA=<schema>
ENVIRONMENT_ID=<environment id>
GENERATE_RC_FILES=Y
```

Save these as `RCAPARM` in an empty directory, add a `DBVIEWS` file listing the view numbers one per line, set `PG_USERID` and `PG_PASSWORD` in the environment, and run `java -jar rcapps-latest.jar`.
Do this once with the verified jar and once with the new jar, then compare `VDPNEW`, `XLTNEW` and `JLTNEW`.
Never put the user id or password in a file that is committed.

RC25 and RC27 were run like this against a local database holding the five DEMO views (10689, 10700, 10702, 10714, 10715).
Both ended with return code 0 and produced files of identical size.
The only bytes that differed were the generation time stamps in the first records.
