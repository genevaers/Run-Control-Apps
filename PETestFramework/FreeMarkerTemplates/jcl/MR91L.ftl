//${test.name}L JOB (ACCT),'REGRESSION JOB',
//          NOTIFY=&SYSUID.,
//          CLASS=A,
//          MSGLEVEL=(1,1),
//          MSGCLASS=H
//*
//JOBLIB   DD DISP=SHR,DSN=${env["OVERRIDE"]}
//         DD DISP=SHR,DSN=${env["PMLOAD"]}
//         DD DISP=SHR,DSN=GEBT.PM418108.B0000.GVBLOAD MR91 HERE
//         DD DISP=SHR,DSN=${env["GERS_DB2_LOAD_LIB"]}
//         DD DISP=SHR,DSN=${env["GERS_DB2_EXIT_LIB"]}
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
<#-- To avoid the line break at the end I butted up the closing tag -->
${env["GERS_TEST_HLQ"]}.${test.dataSet}</#macro>
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
//*
//*********************************************************************
//*   RUN MR91
//*********************************************************************
//*
//MR91 EXEC PGM=GVBMR91,REGION=0M
//*
//MR91PARM DD DISP=SHR,DSN=<@qualifiedTest/>.PARM(MR91PARM)
//*
<#if test.source?matches("DB")>
//DBVIEWS  DD *  
${test.DBDetails.Viewlist.View}
//* Got to figure out how to manage this ODBCINI thing             
//DSNAOINI DD DISP=SHR,DSN=GEBT.RTC20251.JCL(@ODBCINI)
<#elseif test.source?matches("WBXML")>
//WBXMLI   DD DISP=SHR,DSN=<@qualifiedTest/>.MR91.XMLS
<#else>
//VDPXSD   DD DISP=SHR,DSN=${env["VDPXSD"]}
//*                            
//VDPXMLI  DD DISP=SHR,DSN=<@qualifiedTest/>.MR91.XMLS
</#if>  
<#if test.runviews??>
//RUNVIEWS  DD *  
${test.runviews}</#if>
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
//*
//SPM      DD DUMMY
//*
<#if test.mr91only?matches("Y")>
//MR91RPT  DD DSN=<@qualifiedTest/>.MR91RPT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=512)
//MR91LOG  DD DSN=<@qualifiedTest/>.MR91LOG,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=512)
<#else>
//MR91RPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255,BLKSIZE=32760)
//MR91LOG  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
</#if>
//MR91TRAC DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//SYSPRINT DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//SYSOUT   DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//CEEDUMP  DD SYSOUT=*
//SYSMDUMP DD SYSOUT=*
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