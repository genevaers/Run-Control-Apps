//${test.name}L JOB (ACCT),'REGRESSION JOB',
//          NOTIFY=&SYSUID.,
//          CLASS=A,
//          MSGLEVEL=(1,1),
//          MSGCLASS=H
//*
//         JCLLIB ORDER=AJV.V11R0M0.PROCLIB
//*
//JOBLIB   DD DISP=SHR,DSN=AJV.V11R0M0.SIEALNKE
//*
//*The following DDs can/should be present in the calling JCL
//*
<#include "../common/generation.ftl"/>
//*********************************************************************
//*   DELETE DATA SETS
//*********************************************************************
//*
//DELETE    EXEC PGM=IDCAMS
//*
//SYSPRINT  DD SYSOUT=*,DCB=(LRECL=133,BLKSIZE=12901,RECFM=FBA)
//*
//SYSIN     DD *
<#macro qualifiedTest>
${env["TEST_HLQ"]}.${test.dataSet}</#macro>
 DELETE  <@qualifiedTest/>.MR91.JLT PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

 DELETE  <@qualifiedTest/>.MR91.VDP PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

 DELETE  <@qualifiedTest/>.MR91.XLT PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
 DELETE  <@qualifiedTest/>.MR91LOG PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//*********************************************************************
//*
//*******************************************************************
//* Licensed Materials - Property of IBM
//* 5655-DGJ
//* Copyright IBM Corp. 1997, 2021
//* STATUS = HJVBB00
//*
//* Batch job to run the Java VM
//*
//* Tailor the proc and job for your installation:
//* 1.) Modify the Job card per your installation's requirements
//* 2.) Modify the PROCLIB card to point to this PDS
//* 3.) edit JAVA_HOME to point the location of the SDK
//* 4.) edit APP_HOME to point the location of your app (if any)
//* 5.) Modify the CLASSPATH as required to point to your Java code
//* 6.) Modify JAVACLS and ARGS to launch desired Java class
//*
//*******************************************************************
//JAVA EXEC PROC=JVMPRC16,
// JAVACLS='org.genevaers.runcontrolgenerator.App'
//STDENV DD *
# This is a shell script which configures
# any environment variables for the Java JVM.
# Variables must be exported to be seen by the launcher.

. /etc/profile
. /u/icunnin/jmr91profile

LIBPATH=/lib:/usr/lib:"$JAVA_HOME"/bin
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib/j9vm
export LIBPATH="$LIBPATH":
# Customize your CLASSPATH here


# Add Application required jars to end of CLASSPATH
for i in $(find $APP_HOME -type f);do
    CLASSPATH="$CLASSPATH":"$i"
    done
export CLASSPATH="$CLASSPATH":

# Set JZOS specific options
# Use this variable to specify encoding for DD STDOUT and STDERR
#export JZOS_OUTPUT_ENCODING=Cp1047
# Use this variable to prevent JZOS from handling MVS operator commands
#export JZOS_ENABLE_MVS_COMMANDS=false
# Use this variable to supply additional arguments to main
#export JZOS_MAIN_ARGS=""

# Configure JVM options
IJO="-Xms16m -Xmx128m"
# Uncomment the following to aid in debugging "Class Not Found" problems
#IJO="$IJO -verbose:class"
# Uncomment the following if you want to run with Ascii file encoding..
IJO="$IJO -Dfile.encoding=ISO8859-1"
export IBM_JAVA_OPTIONS="$IJO "

//*
//*        INPUTS
//*
//WBXMLI   DD DISP=SHR,DSN=<@qualifiedTest/>.MR91.XMLS
//MR91PARM DD DISP=SHR,DSN=<@qualifiedTest/>.PARM(MR91PARM) 
//*
//*        OUTPUT FILES
//*
//VDP      DD DSN=<@qualifiedTest/>.MR91.VDP,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192,BLKSIZE=0)
//*
//JLT      DD DSN=<@qualifiedTest/>.MR91.JLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//*
//XLT      DD DSN=<@qualifiedTest/>.MR91.XLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//MR91LOG  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//*
//*******************************************************************
//* SUBMIT NEXT JOB
//*******************************************************************
<#if test.mr91only?matches("Y")>
//* MR91 only run - stop here
//
</#if>
//*
//DONEXT IF RC < 8 THEN
//JSTEPNX1 EXEC PGM=IEBGENER
//*
//SYSIN    DD DUMMY
//*
//SYSUT1   DD DSN=<@qualifiedTest/>.JCL(${test.name}R),
//            DISP=SHR
//*
//SYSUT2   DD SYSOUT=(*,INTRDR)
//*
//SYSPRINT DD SYSOUT=*
//*
//       ENDIF
