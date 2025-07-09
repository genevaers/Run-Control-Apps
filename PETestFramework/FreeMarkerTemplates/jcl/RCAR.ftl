//${test.name}R JOB (${env["GERS_JOB_ACCT_INFO"]}),'REGRESSION JOB',
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
<#list test.reffiles as ref>
 DELETE <@qualifiedTest/>.OUTR.MR95.F${ref?counter?left_pad(3,"0")}.RED PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
</#list>
 DELETE  <@qualifiedTest/>.OUTR.MR95.REH PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
 DELETE  <@qualifiedTest/>.OUTR.MR95.RTH PURGE
 IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//*
//*********************************************************************
//* PSTEP705 - PREPARE REFERENCE DATA
//*
//*********************************************************************
//*
//PSTEP705 EXEC PGM=GVBMR95R,
// REGION=0M
//*
//*        INPUT GENEVA FILES
//*
//REFRPARM DD DSN=<@qualifiedTest/>.PARM(REGRR95C),
//            DISP=SHR
<#if env.EXTRACT_TRACE="Y">
//         DD *
TRACE=Y
//REFRTPRM DD *
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
</#if>//*
//REFRENVV DD DSN=<@qualifiedTest/>.PARM(REGRR95E),
//            DISP=SHR
//*
//MR95VDP  DD DSN=<@qualifiedTest/>.RCA.VDP,
//            DISP=SHR
//*
//REFRLTBL DD DSN=<@qualifiedTest/>.RCA.JLT,
//            DISP=SHR
//*
//*        INPUT REFERENCE FILES
//*
<#list test.reffiles as ref>
<#if ref.dummy??>
//${ref.ddname} DD DUMMY
<#elseif ref.dsn??>
//${ref.ddname} DD DSN=${ref.dsn},
//            DISP=SHR
<#elseif ref.filename??>
//${ref.ddname} DD DSN=${env["GERS_TEST_HLQ"]}.INPUT.${ref.filename},
//            DISP=SHR
</#if>
</#list>
//*
//*        OUTPUT GENEVA FILES
//*
//*
//REFRREH  DD DSN=<@qualifiedTest/>.OUTR.MR95.REH,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,1),RLSE),
//            DCB=(DSORG=PS,RECFM=FB,LRECL=100)
//*
//REFRRTH  DD DSN=<@qualifiedTest/>.OUTR.MR95.RTH,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,1),RLSE),
//            DCB=(DSORG=PS,RECFM=FB,LRECL=100)
//*
<#list test.reffiles as ref>
//REFR${ref?counter?left_pad(3,"0")}  DD DSN=<@qualifiedTest/>.OUTR.MR95.F${ref?counter?left_pad(3,"0")}.RED,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)
//*
</#list>
//*
//SORT001  DD DUMMY
//SORT002  DD DUMMY
//SORT003  DD DUMMY
//SORT004  DD DUMMY
//SORT005  DD DUMMY
//SORT006  DD DUMMY
//SORT007  DD DUMMY
//SORT008  DD DUMMY
//SORT009  DD DUMMY
//SORT010  DD DUMMY
//SORT011  DD DUMMY
//SORT012  DD DUMMY
//*
//REFRRPT  DD SYSOUT=*
//REFRLOG  DD SYSOUT=*
//REFRTRAC DD SYSOUT=*
//REFRDUMP DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//*
//SYSABEND DD SYSOUT=*
//*
//*******************************************************************
//* JSTEPNX1 - SUBMIT NEXT JOB
//*******************************************************************
//*
//DONEXT IF RC < 8 THEN
//JSTEPNX1 EXEC PGM=IEBGENER
//*
//SYSIN    DD DUMMY
//*
//SYSUT1   DD DSN=<@qualifiedTest/>.JCL(${test.name}E),
//            DISP=SHR
//*
//SYSUT2   DD SYSOUT=(*,INTRDR)
//*
//SYSPRINT DD SYSOUT=*
//*
//       ENDIF