//${test.name}L JOB (${env["GERS_JOB_ACCT_INFO"]}),'REGRESSION JOB',
//          NOTIFY=&SYSUID.,
//          CLASS=${env["GERS_JOB_CLASS"]},
//          MSGLEVEL=${env["GERS_MSG_LEVEL"]},
//          MSGCLASS=${env["GERS_MSG_CLASS"]}
//*
//         JCLLIB ORDER=${env["GERS_JVM_PROC_LIB"]}
//*
//JOBLIB   DD DISP=SHR,DSN=${env["GERS_JZOS_LOAD_LIB"]}
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
${env["GERS_TEST_HLQ"]}.${test.dataSet}</#macro>
 DELETE  <@qualifiedTest/>.RCA.JLT PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

 DELETE  <@qualifiedTest/>.RCA.VDP PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

 DELETE  <@qualifiedTest/>.RCA.XLT PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
 DELETE  <@qualifiedTest/>.RCALOG PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//*********************************************************************
//* Execute the Run-Control App
//*******************************************************************
//*
//JAVA EXEC PROC=JVMPRC16,
// JAVACLS='org.genevaers.rcapps.Runner'
//*
//STDENV DD *
# This is a shell script which configures
# any environment variables for the Java JVM.
# Variables must be exported to be seen by the launcher.

. /etc/profile
export A2E=-ofrom=ISO8859-1,to=IBM-1047
export JAVA_HOME=${env["GERS_JAVA_HOME"]}
export IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"

export APP_HOME=${env["GERS_RCA_JAR_DIR"]}
export APP_NAME=rcapps-latest.jar
export CLASSPATH=$APP_HOME:"$JAVA_HOME"/lib

LIBPATH=/lib:/usr/lib:"$JAVA_HOME"/bin
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib/j9vm
export LIBPATH="$LIBPATH":
# Customize your CLASSPATH here
# Add Application required jars to end of CLASSPATH
CLASSPATH="$CLASSPATH":"$APP_HOME"/"$APP_NAME"
echo $CLASSPATH
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
//WBXMLI   DD DISP=SHR,DSN=<@qualifiedTest/>.RCA.XMLS
//RCAPARM DD DISP=SHR,DSN=<@qualifiedTest/>.PARM(RCAPARM)
<#if test.runviews??>
//RUNVIEWS  DD *
${test.runviews}</#if>
//*
//*        OUTPUT FILES
//*
//VDPNEW   DD DSN=<@qualifiedTest/>.RCA.VDP,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192,BLKSIZE=0)
//*
//JLTNEW   DD DSN=<@qualifiedTest/>.RCA.JLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//*
//XLTNEW   DD DSN=<@qualifiedTest/>.RCA.XLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//RCARPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//RCALOG  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//*
//*******************************************************************
//* SUBMIT NEXT JOB
//*******************************************************************
<#if test.mr91only?matches("Y")>
//* RCA only run - stop here
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
