grammar ExtractParser;
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
import GenevaERSLexer;

//predicate       : ( LPAREN exprBoolOr RPAREN )
//                | exprBoolUnary AND exprBoolUnary
//                | exprBoolUnary OR exprBoolUnary
//                ;

predicate       : exprBoolOr
                ;

exprBoolOr      : exprBoolAnd ( OR exprBoolAnd )*
                ;

exprBoolAnd     : exprBoolUnaryResolve ( AND exprBoolUnaryResolve )*
                ;

exprBoolUnaryResolve : exprBoolUnary
                ;

exprBoolUnary   : ( NOT )? exprBoolAtom
                ;

exprBoolAtom    : ( LPAREN exprBoolOr RPAREN )
                  | stringComp
                  | arithComp
                  | isFunctions
                  | isFounds
                ;

// is Functions
// Casting not relevant is operates on a dataSource
isFunctions     : ( ISSPACES
                  | ISNOTSPACES
                  | ISNUMERIC
                  | ISNOTNUMERIC
                  | ISNULL
                  | ISNOTNULL ) LPAREN dataSource RPAREN
                ;

aDataSource  : castDataSource 
             | dataSource
             ;

castDataSource  : (CAST) dataSource
                ;

dataSource      : lrField
                | lookupField
                | colRef
                ;

isFounds       : ( ISFOUND
                 | ISNOTFOUND ) LPAREN lookup RPAREN
               ;

// comparisons
stringComp      : exprConcatString ( CONTAINS
                                   | BEGINS_WITH
                                   | ENDS_WITH ) exprConcatString
                ;

arithComp       : arithExpr ( LT
                            | GT
                            | LE
                            | GE
                            | EQ
                            | NE ) arithExpr
                ;

lookup          : CURLED_NAME
                | LEFTBRACE META_REF symbollist RIGHTBRACE
                | LEFTBRACE META_REF effDate RIGHTBRACE
                | LEFTBRACE META_REF effDate symbollist RIGHTBRACE
                ;

// string expressions
stringExpr       : exprStringAtom
                 | exprConcatString
                 ;

exprConcatString : exprStringAtom ( (CONCAT) exprStringAtom)*
                 ;

// Do we care about casting in here?
exprStringAtom  : LPAREN exprConcatString RPAREN
                | aDataSource
                | stringAtom
                | dateConstant
                | strfunc
                ;

// arithmetic expressions
//arithExpr       : (PLUS | MINUS )* exprArithAddSub ;
                // | exprArithFactor MULT exprArithFactor
                // | exprArithFactor DIV exprArithFactor
                // | exprArithFactor PLUS exprArithFactor
                // | exprArithFactor MINUS exprArithFactor
                // ;

arithExpr       : exprArithFactor ((
                    PLUS |
                    MINUS) exprArithFactor)*
                ;

exprArithFactor : exprArithTerm ((
                    MULT |
                    DIV ) exprArithTerm)*
                ;

//exprArithResolveUnary : exprArithTerm
//                      ;

exprArithTerm  : (PLUS | MINUS )? exprArithAtom
                ;

exprArithAtom   : LPAREN arithExpr RPAREN
                | aDataSource
                | numAtom
                | betweenFunc
                | dateConstant
                | stringAtom //not really allowed but pick up in processing
		        		;

colRef          : COL_REF
                ;

castColRef      : cast colRef
                ;

lrField         : CURLED_NAME
                | CURRENT LPAREN CURLED_NAME RPAREN
                | PRIOR   LPAREN CURLED_NAME RPAREN
                ;

castField       : cast lrField
                ;

lookupField     : LEFTBRACE DOTTED_NAME RIGHTBRACE
                | LEFTBRACE DOTTED_NAME symbollist RIGHTBRACE
                | LEFTBRACE DOTTED_NAME effDate RIGHTBRACE
                | LEFTBRACE DOTTED_NAME effDate symbollist RIGHTBRACE
                ;

castLookupField : cast lookupField
                ;

cast            : '<'   ALPHA 
                  | NODTF 
                  | K_BINARY 
                  | BCD 
                  | EDITED 
                  | MASKED 
                  | PACKED 
                  | BINARY 
                  | SPACKED 
                  | SZONED 
                  | ZONED '>'
                ;

symbollist      : ';' symbolEntry*
                ;

effDate         : ',' effDateValue
                ;

effDateValue    : lrField
                | dateConstant
                ;

symbolEntry     : SYMBIDENT '=' NUM (COMMA)?
                | SYMBIDENT '=' STRING (COMMA)?
                ;

eventSelector   : CURRENT LPAREN lrField RPAREN
                | PRIOR   LPAREN lrField RPAREN
                ;

constant        : numAtom | stringAtom | dateConstant
				;

numAtom         : (PLUS | MINUS)? (FLOAT | NUM) 
                ;

stringAtom      : string
                | repeat
                | all
                ;

repeat          : REPEAT LPAREN string COMMA NUM RPAREN
                ;

all             : ALL LPAREN string RPAREN
                ;

string          :  STRING
                ;

// dates
dateConstant    : dateFunc
                | period
                | STRING
                ;

dateFunc        : DATE LPAREN string COMMA ( CCYY
				                                   | CCYYDDD
                                           | CCYYMMDD
                                           | MMDDCCYY
                                           | MMDDYY
                                           | CCYYMM
                                           | DDMMYY
                                           | DDMMCCYY
                                           | DD
                                           | MM
                                           | MMDD
                                           | YYDDD
                                           | YYMMDD
                                           | YY  ) RPAREN
                ;

period          : runDate
                | fiscalDate
                | timeStamp
                ;

runDate         : ( RUNDAY
                |   RUNMONTH
                |   RUNYEAR ) LPAREN ( unaryInt )? RPAREN
                ;

fiscalDate      : ( FISCALDAY
                |   FISCALMONTH
                |   FISCALYEAR ) LPAREN ( unaryInt )? RPAREN
                ;

timeStamp       : BATCHDATE LPAREN ( unaryInt )? RPAREN
                ;

unaryInt        : unaryIntResolve
                ;

unaryIntResolve : (MINUS | PLUS)? NUM
                ;

// betweenFunc
betweenFunc     : ( DAYSBETWEEN
                  | MONTHSBETWEEN
                  | YEARSBETWEEN ) LPAREN dateArg COMMA dateArg RPAREN
			        	;

dateArgLP       : dateArg (.)*?
                ;

dateArg         : string
                | dateConstant
                | lrField
                ;

// string functions

strfunc         : substr
                | left
                | right
                ;

substr          : SUBSTR LPAREN stringExpr COMMA NUM (COMMA NUM)? RPAREN
                ;

left            : LEFT LPAREN stringExpr COMMA NUM RPAREN
                ;

right           : RIGHT LPAREN stringExpr COMMA NUM RPAREN
                ;

// write statements
writeStatement  : WRITE LPAREN (writeParam)? (COMMA writeParam)*  RPAREN
                ;

writeParam      : source
                | destination
                | procedure
                ;

source          : SOURCE EQ sourceArg
                ;

sourceArg       : VIEW | INPUT | DATA
                ;

destination     : (DESTINATION | DEST) EQ destArg
                ;

destArg         : extractArg | fileArg | DEFAULT
                ;

extractArg      : ( EXTRACT | EXT ) EQ NUM
                ;

fileArg         : FILE EQ file
                ;

file            : LEFTBRACE DOTTED_NAME RIGHTBRACE
                ;

procedure       : ( PROCEDURE | PROC ) EQ exitArg
                | USEREXIT EQ exitArg
                ;

exitArg         : ( writeExit | LPAREN writeExit COMMA string RPAREN )
                ;

writeExit       : CURLED_NAME
                ;


