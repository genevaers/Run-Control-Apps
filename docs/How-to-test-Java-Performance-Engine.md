# How to test the Java Performance Engine

## Testing the Java Performance Engine on the Mainframe

Log on to z/OS UNIX using a git bash session e.g.

ssh username@xxx.yyy.com

1. Build RCA from branch 'PerformanceEngine'
- git pull
- git checkout PerformanceEngine
```
./mfbuild.sh -Pdb2
```

2. Use RCA to generate the 'Performance Engine' Java from a view  
- Copy (to an MVS data set) and edit the sample JCL [RCAPE1](../SampleJCL/RCAPE1)  
This JCL reads WBXML and generates some Java code.  
**or**
- Copy (to an MVS data set) and edit the sample JCL [RCAPE1D](../SampleJCL/RCAPE1D)  
This JCL reads Db2 and generates some Java code.  

Before submitting the JCL, make sure the directory ~/PEJAVA/ exists.  
Submit the JCL, and this will generate code in ~/PEJAVA/PECode.java.  
The XLT, JLT and VDP, and corresponding reports will still be generated, if requested.  

3. Build the generated Java 

Go to Run-Control-Apps/
```
./mfbuildPE.sh
```
This script will copy the source from ~/PEJAVA to a target directory, convert to ascii, and build the executable jar.

This will put pe-latest.jar in $GERS_RCA_JAR_DIR.

4. Run the jar

Copy and edit the sample JCL [RCAPE2](../SampleJCL/RCAPE2)  

The output currently goes to the DD DUMMYOUT
