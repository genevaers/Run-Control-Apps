grammar GenevaERS;
/*
 * Copyright Contributors to the GenevaERS Project.
 * (c) Copyright IBM Corporation 2020.
 * SPDX-License-Identifier: Apache-2.0  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import ExtractParser;

goal            : stmtList EOF   ;

stmtList        : ( stmt )*
                ;

stmt            : ifStatement 
                | selectStatement
                | castColumnAssignment 
                | columnAssignment 
                | columnRefAssignment
                | writeStatement
                ;

ifStatement     : IF ifbody
                ;

ifbody          : predicate  THEN stmtList
                  ( ELSE stmtList)? 
		              ENDIF
		            ;

castColumnAssignment  : (CAST) columnAssignment ;
                  
columnAssignment     : COLUMN  EQ LPAREN? ( arithExpr | stringExpr ) RPAREN?  ;
columnRefAssignment  : COL_REF EQ LPAREN? ( arithExpr | stringExpr ) RPAREN?  ;

selectStatement : selectIf 
                | skipIf;

selectIf        : SELECTIF LPAREN predicate RPAREN;
skipIf          : SKIPIF LPAREN predicate RPAREN;
