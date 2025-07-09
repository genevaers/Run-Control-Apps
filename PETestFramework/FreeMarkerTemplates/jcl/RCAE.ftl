//${test.name}E JOB (${env["GERS_JOB_ACCT_INFO"]}),'REGRESSION JOB',
//            NOTIFY=&SYSUID.,
//            CLASS=${env["GERS_JOB_CLASS"]},
//            MSGLEVEL=${env["GERS_MSG_LEVEL"]},
//            MSGCLASS=${env["GERS_MSG_CLASS"]},
//            REGION=0M
//*
//JOBLIB   DD DISP=SHR,DSN=${env["OVERRIDE"]}
//         DD DISP=SHR,DSN=${env["PMLOAD"]}
//         DD DISP=SHR,DSN=${env["GERS_DB2_LOAD_LIB"]}
//         DD DISP=SHR,DSN=${env["GERS_DB2_EXIT_LIB"]}
<#if test.exitload??>
//         DD DISP=SHR,DSN=${test.exitload}
</#if>
<#include "../common/generation.ftl"/>
//**********************************************************************
//* PSTEP700 - DELETE THE FILE(S) CREATED IN NEXT STEP
//*
//**********************************************************************
//*
//PSTEP700 EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *
<#macro qualifiedTest>
<#-- To avoid the line break at the end I butted up the closing tag -->
${env["GERS_TEST_HLQ"]}.${test.dataSet}</#macro>
<#list test.extractfiles as ext>
<#if ext.dsn??>
 DELETE  ${ext.dsn} PURGE
<#elseif ext.ddname??>
 DELETE  <@qualifiedTest/>.OUTE.MR95.${ext.ddname} PURGE
<#elseif ext.workfile??>
 DELETE  <@qualifiedTest/>.OUTE.MR95.EXTR${ext.workfile?left_pad(3,"0")} PURGE
 <#else>
 DELETE  <@qualifiedTest/>.OUTE.MR95.EXTR${ext?counter?left_pad(3,"0")} PURGE
</#if>
  IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
      SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
</#list>
<#-- Test for format phase. We only need SORT DDs if there is a format phase -->
<#assign fmtFiles = test.formatfiles?size>
<#if fmtFiles gt 0>
<#list test.extractfiles as ext>
<#if ext.workfile??>
 DELETE  <@qualifiedTest/>.OUTE.MR95.SORT${ext.workfile?left_pad(3,"0")} PURGE
<#else>
 DELETE  <@qualifiedTest/>.OUTE.MR95.SORT${ext?counter?left_pad(3,"0")} PURGE
</#if>
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
</#list>
</#if>
<#if test.mergerpt??>
 DELETE  <@qualifiedTest/>.OUTE.MR95.MERGRPT
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
</#if>
//*********************************************************************
//* PSTEP705 - EXTRACT DATA FOR VIEWS
//*
//*********************************************************************
//*
//PSTEP705 EXEC PGM=GVBMR95E,
// REGION=0M
//*
//*        INPUT GENEVA FILES
//*
//EXTRPARM DD DSN=<@qualifiedTest/>.PARM(REGRE95C),
//            DISP=SHR
//         DD *
* Parms set by environment variables 
<#if env.GERS_DB2_PLAN_SUFFIX??>
DB2_SQL_PLAN_NAME=GVBMRSQ${env.GERS_DB2_PLAN_SUFFIX}
</#if>
<#if env.EXTRACT_TRACE="Y">
TRACE=Y
//EXTRTPRM DD *
<#if env.VIEW?number gt 0>
VIEW=${env.VIEW}
</#if>
<#if env.FROMREC?number gt 0>
FROMREC=${env.FROMREC}
</#if>
<#if env.THRUREC?number gt 0>
THRUREC=${env.THRUREC}
</#if>
<#if env.FROMLTROW?number gt 0>
FROMLTROW=${env.FROMLTROW}
</#if>
<#if env.THRULTROW?number gt 0>
THRULTROW=${env.THRULTROW}
</#if>
<#if env.FROMCOL?number gt 0>
FROMCOL=${env.FROMCOL}
</#if>
<#if env.THRUCOL?number gt 0>
THRUCOL=${env.THRUCOL}
</#if>
</#if>
//*
//EXTRENVV DD DSN=<@qualifiedTest/>.PARM(REGRE95E),
//            DISP=SHR
//*
<#if test.mergeparm??>
//MERGPARM DD DSN=<@qualifiedTest/>.PARM(REGRE95M),
//            DISP=SHR
//MERGINIT DD DUMMY
//*
</#if>
//MR95VDP  DD DSN=<@qualifiedTest/>.RCA.VDP,
//            DISP=SHR
//*
//EXTRLTBL  DD DSN=<@qualifiedTest/>.RCA.XLT,
//            DISP=SHR
//*
//*
<#if test.reffiles??>
//EXTRREH  DD DSN=<@qualifiedTest/>.OUTR.MR95.REH,
//            DISP=SHR
//*
<#else>
//EXTRREH  DD DUMMY
</#if>
//*
//*        INPUT REF FILES
//*
//*
<#list test.reffiles as ref>
//REFR${ref?counter?left_pad(3,"0")} DD DSN=<@qualifiedTest/>.OUTR.MR95.F${ref?counter?left_pad(3,"0")}.RED,
//            DISP=SHR
</#list>
//*
//*        INPUT EVENT FILES
//*
//*
<#list test.eventfiles as evt>
<#if evt.dummy??>
//* Dummy out the input
//${evt.ddname} DD DUMMY
<#elseif evt.dsn??>
//* Input from direct DSN
//${evt.ddname} DD DSN=${evt.dsn},
//            DISP=SHR
<#elseif evt.ddname??>
//* Input from test input set
//${evt.ddname} DD DSN=${env["GERS_TEST_HLQ"]}.INPUT.${evt.filename},
//            DISP=SHR
</#if>
</#list>
//*
//*        OUTPUT GENEVA FILES
//*
<#list test.extractfiles as ext>
<#if ext.dsn??>
//${ext.ddname}  DD DSN=${ext.dsn},
<#elseif ext.ddname??>
//${ext.ddname}  DD DSN=<@qualifiedTest/>.OUTE.MR95.${ext.ddname},
<#elseif ext.workfile??>
//EXTR${ext.workfile?left_pad(3,"0")} DD DSN=<@qualifiedTest/>.OUTE.MR95.EXTR${ext.workfile?left_pad(3,"0")},
<#else>
//EXTR${ext?counter?left_pad(3,"0")} DD DSN=<@qualifiedTest/>.OUTE.MR95.EXTR${ext?counter?left_pad(3,"0")},
</#if>
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(${ext.space},(${ext.primary},${ext.secondary}),RLSE),
<#if ext.blksize??>
//            DCB=(DSORG=PS,RECFM=${ext.recfm},LRECL=${ext.lrecl},BLKSIZE=${ext.blksize})
<#else>
//            DCB=(DSORG=PS,RECFM=${ext.recfm},LRECL=${ext.lrecl})
</#if>
</#list>
//*
<#if fmtFiles gt 0>
<#list test.extractfiles as ext>
<#if ext.workfile??>
//SORT${ext.workfile?left_pad(3,"0")}  DD DSN=<@qualifiedTest/>.OUTE.MR95.SORT${ext.workfile?left_pad(3,"0")},
<#else>
//SORT${ext?counter?left_pad(3,"0")}  DD DSN=<@qualifiedTest/>.OUTE.MR95.SORT${ext?counter?left_pad(3,"0")},
</#if>
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1,1),RLSE),
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)
//*
</#list>
</#if>
//EXTRRPT DD SYSOUT=*
//EXTRLOG DD SYSOUT=*
//*
<#if test.mergeparm??>
<#if test.mergerpt??>
//MERGRPT DD DSN=<@qualifiedTest/>.OUTE.MR95.MERGRPT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(${test.mergerpt.space},(${test.mergerpt.primary},${test.mergerpt.secondary}),RLSE),
//            DCB=(DSORG=PS,RECFM=${test.mergerpt.recfm},LRECL=${test.mergerpt.lrecl},BLKSIZE=0)
<#else>
//MERGRPT DD SYSOUT=*
</#if>
</#if>
//*
//EXTRTRAC DD SYSOUT=*
//EXTRDUMP DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//*
//SYSUDUMP DD SYSOUT=*
//*
<#assign fmtFiles = test.formatfiles?size>
<#if fmtFiles gt 0>
//*******************************************************************
//* JSTEPNX1 - SUBMIT NEXT JOB
//*******************************************************************
//*
//DONEXT IF RC < 8 THEN
//JSTEPNX1 EXEC PGM=IEBGENER
//*
//SYSIN    DD DUMMY
//*
//SYSUT1   DD DSN=<@qualifiedTest/>.JCL(${test.name}F),
//            DISP=SHR
//*
//SYSUT2   DD SYSOUT=(*,INTRDR)
//*
//SYSPRINT DD SYSOUT=*
//*
//       ENDIF
<#elseif env["GENERATE_COVERAGE"]?matches("Y")>
//*
//*******************************************************************
//* SUBMIT NEXT JOB
//*******************************************************************
//*
//DONEXT IF RC < 8 THEN
//JSTEPNX1 EXEC PGM=IEBGENER
//*
//SYSIN    DD DUMMY
//*
//SYSUT1   DD DSN=<@qualifiedTest/>.JCL(${test.name}X),
//            DISP=SHR
//*
//SYSUT2   DD SYSOUT=(*,INTRDR)
//*
//SYSPRINT DD SYSOUT=*
//*
//       ENDIF
<#else>
//***********************************************************
//* No Format Files Defined... this is just a generation dag
</#if>
