package org.genevaers.genevaio.ltfactory;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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

/*
** This file was automatically generated. 
**
** Do not edit.
**
** This file is here in the src tree because the template and code that generated it disappeared.
** And for the moment I can not be bothered/do not have time to recreate the generator.
**
*/
import org.genevaers.repository.components.enums.LtRecordType;

/**
 * Function Code cache for coverage analysis
 */
public abstract class LtFunctionCodeCache {

 
    public LtFunctionCodeCache()
    {
        addFunctionCode("ADDA", LtRecordType.NAMEVALUE, "Add to an accumulator from another accumulator", "Arithmetic");
        addFunctionCode("ADDC", LtRecordType.NAMEVALUE, "Add a constant value to an accumulator", "Arithmetic");
        addFunctionCode("ADDE", LtRecordType.NAMEF1, "Add an event field value to an accumulator", "Arithmetic");
        addFunctionCode("ADDL", LtRecordType.NAMEF1, "Add a lookup field value to an accumulator", "Arithmetic");
        addFunctionCode("ADDP", LtRecordType.NAMEF1, "Add a prior event field value to an accumulator", "Arithmetic");
        addFunctionCode("ADDX", LtRecordType.NAMEF1, "Add a prior event field value to an accumulator", "Arithmetic");
        addFunctionCode("CFAA", LtRecordType.NAMEVALUE, "Compare two accumulated values", "Comparison");
        addFunctionCode("CFAC", LtRecordType.NAMEVALUE, "Compare an event field value to a constant value", "Comparison");
        addFunctionCode("CFAE", LtRecordType.NAMEF1, "Compare two event field values", "Comparison");
        addFunctionCode("CFAL", LtRecordType.NAMEF1, "Compare an event field value to a lookup field value", "Comparison");
        addFunctionCode("CFAP", LtRecordType.NAMEF1, "Compare an event field value to a prior event field value", "Comparison");
        addFunctionCode("CFAX", LtRecordType.NAMEF1, "Compare an event field value to a prior event field value", "Comparison");
        addFunctionCode("CFCA", LtRecordType.NAMEVALUE, "Compare a constant value to an accumulated value", "Comparison");
        addFunctionCode("CFCC", LtRecordType.CC, "Compare two constant values", "Comparison");
        addFunctionCode("CFCE", LtRecordType.F1, "Compare two event field values", "Comparison");
        addFunctionCode("CFCL", LtRecordType.F1, "Compare an event field value to a lookup field value", "Comparison");
        addFunctionCode("CFCP", LtRecordType.F1, "Compare an event field value to a prior event field value", "Comparison");
        addFunctionCode("CFCX", LtRecordType.F1, "Compare an event field value to a prior event field value", "Comparison");
        addFunctionCode("CFEA", LtRecordType.NAMEF1, "Compare an event field value to an accumulated value", "Comparison");
        addFunctionCode("CFEC", LtRecordType.F1, "Compare an event field value to a constant value", "Comparison");
        addFunctionCode("CFEE", LtRecordType.F2, "Compare two event field values", "Comparison");
        addFunctionCode("CFEL", LtRecordType.F2, "Compare an event field value to a lookup field value", "Comparison");
        addFunctionCode("CFEP", LtRecordType.F2, "Compare an event field value to a prior event field value", "Comparison");
        addFunctionCode("CFEX", LtRecordType.F2, "Compare an event field value to a prior event field value", "Comparison");
        addFunctionCode("CFLA", LtRecordType.NAMEF1, "Compare a lookup field value to an accumulated value", "Comparison");
        addFunctionCode("CFLC", LtRecordType.F1, "Compare a lookup field value to a constant value", "Comparison");
        addFunctionCode("CFLE", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFLL", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFLP", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFLX", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFPA", LtRecordType.NAMEF1, "Compare a lookup field value to an accumulated value", "Comparison");
        addFunctionCode("CFPC", LtRecordType.F1, "Compare a prior event field value to a constant value", "Comparison");
        addFunctionCode("CFPE", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFPL", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFPP", LtRecordType.F2, "Compare two prior event field values", "Comparison");
        addFunctionCode("CFPX", LtRecordType.F2, "Compare two prior event field values", "Comparison");
        addFunctionCode("CFXA", LtRecordType.NAMEF1, "Compare a lookup field value to an accumulated value", "Comparison");
        addFunctionCode("CFXC", LtRecordType.F1, "Compare a prior event field value to a constant value", "Comparison");
        addFunctionCode("CFXE", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFXL", LtRecordType.F2, "Compare a lookup field value to a prior event field value", "Comparison");
        addFunctionCode("CFXP", LtRecordType.F2, "Compare two prior event field values", "Comparison");
        addFunctionCode("CFXX", LtRecordType.F2, "Compare two prior event field values", "Comparison");
        addFunctionCode("CNE", LtRecordType.F1, "IsNumeric function applied to an event field", "Comparison");
        addFunctionCode("CNL", LtRecordType.F1, "IsNumeric function applied to a lookup field", "Comparison");
        addFunctionCode("CNP", LtRecordType.F1, "IsNumeric function applied to a lookup field", "Comparison");
        addFunctionCode("CNX", LtRecordType.F1, "IsNumeric function applied to a lookup field", "Comparison");
        addFunctionCode("CSE", LtRecordType.F1, "IsSpaces function applied to an event field", "Comparison");
        addFunctionCode("CSL", LtRecordType.F1, "IsSpaces function applied to a lookup field", "Comparison");
        addFunctionCode("CSP", LtRecordType.F1, "IsSpaces function applied to a lookup field", "Comparison");
        addFunctionCode("CSX", LtRecordType.F1, "IsSpaces function applied to a lookup field", "Comparison");
        addFunctionCode("CTA", LtRecordType.NAMEF1, "Write an accumulator value to a CT column", "Assignment");
        addFunctionCode("CTC", LtRecordType.F1, "Write a constant value to a CT column", "Assignment");
        addFunctionCode("CTE", LtRecordType.F1, "Write an event field value to a CT column", "Assignment");
        addFunctionCode("CTL", LtRecordType.F1, "Write a lookup field value to a CT column", "Assignment");
        addFunctionCode("CTP", LtRecordType.F1, "Write a prior event field value to a CT column", "Assignment");
        addFunctionCode("CTX", LtRecordType.F1, "Write a prior event field value to a CT column", "Assignment");
        addFunctionCode("CXE", LtRecordType.F1, "isNull function applied to an event field", "Comparison");
        addFunctionCode("CXL", LtRecordType.F1, "isNull function applied to a lookup field", "Comparison");
        addFunctionCode("CXP", LtRecordType.F1, "isNull function applied to a prior field", "Comparison");
        addFunctionCode("CXX", LtRecordType.F1, "isNull function applied to a column reference", "Comparison");
        addFunctionCode("DIM1", LtRecordType.NAME, "Declare a local variable(1 byte)", "Directive");
        addFunctionCode("DIM2", LtRecordType.NAME, "Declare a local variable(2 byte)", "Directive");
        addFunctionCode("DIM4", LtRecordType.NAME, "Declare a local variable(4 byte)", "Directive");
        addFunctionCode("DIM8", LtRecordType.NAME, "Declare a local variable(8 byte)", "Directive");
        addFunctionCode("DIMD", LtRecordType.NAME, "Declare a date variable", "Directive");
        addFunctionCode("DIMN", LtRecordType.NAME, "Declare a numeric variable", "Directive");
        addFunctionCode("DIMS", LtRecordType.NAME, "Decalre a string variable", "Directive");
        addFunctionCode("DIVA", LtRecordType.NAMEVALUE, "Divide an accumulated value by an accumulated value", "Arithmetic");
        addFunctionCode("DIVC", LtRecordType.NAMEVALUE, "Divide an accumulated value by a constant value", "Arithmetic");
        addFunctionCode("DIVE", LtRecordType.NAMEF1, "Divide an accumulated value by an event field value", "Arithmetic");
        addFunctionCode("DIVL", LtRecordType.NAMEF1, "Divide an accumulated value by a lookup field value", "Arithmetic");
        addFunctionCode("DIVP", LtRecordType.NAMEF1, "Divide an accumulated value by a prior event field value", "Arithmetic");
        addFunctionCode("DIVX", LtRecordType.NAMEF1, "Divide an accumulated value by a prior event field value", "Arithmetic");
        addFunctionCode("DTA", LtRecordType.NAMEF1, "Write an accumulated value to a DT column", "Assignment");
        addFunctionCode("DTC", LtRecordType.F1, "Write a constant value to a DT column", "Assignment");
        addFunctionCode("DTE", LtRecordType.F2, "Write an event field value to a DT column", "Assignment");
        addFunctionCode("DTL", LtRecordType.F2, "Write a lookup field value to a DT column", "Assignment");
        addFunctionCode("DTP", LtRecordType.F2, "Write prior event field value to a DT column", "Assignment");
        addFunctionCode("DTX", LtRecordType.F2, "Write prior event field value to a DT column", "Assignment");
        addFunctionCode("EN", LtRecordType.F0, "end of an event set", "Directive");
        addFunctionCode("ES", LtRecordType.F0, "start of an event set", "Directive");
        addFunctionCode("ET", LtRecordType.F0, "start of a token event set", "Directive");
        addFunctionCode("FNCC", LtRecordType.NAMEF2, "Set accumulator to result of function with two constant arguments", "Function");
        addFunctionCode("FNCE", LtRecordType.NAMEF2, "Set accumulator to result of function with two constant arguments", "Function");
        addFunctionCode("FNCL", LtRecordType.NAMEF2, "Set accumulator to result of function with two constant arguments", "Function");
        addFunctionCode("FNCP", LtRecordType.NAMEF2, "Set accumulator to result of function with two constant arguments", "Function");
        addFunctionCode("FNCX", LtRecordType.NAMEF2, "Set accumulator to result of function with two constant arguments", "Function");
        addFunctionCode("FNEC", LtRecordType.NAMEF2, "Set accumulator to result of function with event field and a constant argument", "Function");
        addFunctionCode("FNEE", LtRecordType.NAMEF2, "Set accumulator to result of function with two event field arguments", "Function");
        addFunctionCode("FNEL", LtRecordType.NAMEF2, "Set accumulator to result of function with an event field and a lookup argument", "Function");
        addFunctionCode("FNEP", LtRecordType.NAMEF2, "Set accumulator to result of function with an event field and a lookup argument", "Function");
        addFunctionCode("FNEX", LtRecordType.NAMEF2, "Set accumulator to result of function with an event field and a lookup argument", "Function");
        addFunctionCode("FNLC", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNLE", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNLL", LtRecordType.NAMEF2, "Set accumulator to result of function with two lookup field arguments", "Function");
        addFunctionCode("FNLP", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNLX", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNPC", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNPE", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNPL", LtRecordType.NAMEF2, "Set accumulator to result of function with two lookup field arguments", "Function");
        addFunctionCode("FNPP", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNPX", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNXC", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNXE", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNXL", LtRecordType.NAMEF2, "Set accumulator to result of function with two lookup field arguments", "Function");
        addFunctionCode("FNXP", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNXX", LtRecordType.NAMEF2, "Set accumulator to result of function with lookup field and a constant argument", "Function");
        addFunctionCode("FNE", LtRecordType.NAMEF1, "Set accumulator to result of function with an event field argument", "Function");
        addFunctionCode("FNL", LtRecordType.NAMEF1, "Set accumulator to result of function with lookup field argument", "Function");
        addFunctionCode("FNP", LtRecordType.NAMEF1, "Set accumulator to result of function with prior field argument", "Function");
        addFunctionCode("GEN", LtRecordType.GENERATION, "Generation Record", "GENERATION");
        addFunctionCode("GOTO", LtRecordType.F0, "Name says it all", "Directive");
        addFunctionCode("HD", LtRecordType.HD, "Start of Logic Table", "Directive");
        addFunctionCode("JOIN", LtRecordType.F1, "Make a Join", "Lookup");
        addFunctionCode("KSLK", LtRecordType.F1, "Description to be completed", "Lookup");
        addFunctionCode("LKC", LtRecordType.F1, "Add a constant value to the lookup key", "Lookup");
        addFunctionCode("LKDC", LtRecordType.F1, "Description to be completed", "Lookup");
        addFunctionCode("LKDE", LtRecordType.F2, "Description to be completed", "Lookup");
        addFunctionCode("LKDL", LtRecordType.F1, "Description to be completed", "Lookup");
        addFunctionCode("LKDX", LtRecordType.F1, "Description to be completed", "Lookup");
        addFunctionCode("LKE", LtRecordType.F2, "Add an event field value to the lookup key", "Lookup");
        addFunctionCode("LKL", LtRecordType.F2, "Add a lookup field value to the lookup key", "Lookup");
        addFunctionCode("LKLR", LtRecordType.F1, "Does the next step", "Lookup");
        addFunctionCode("LKS", LtRecordType.F1, "Add a symbolic value to the lookup key", "Lookup");
        addFunctionCode("LKX", LtRecordType.F2, "Add a prior event field value to the lookup key", "Lookup");
        addFunctionCode("LUEX", LtRecordType.RE, "Lookup exit", "Lookup");
        addFunctionCode("LUSM", LtRecordType.RE, "Lookup from memory", "Lookup");
        addFunctionCode("MULC", LtRecordType.NAMEVALUE, "Multiply an accumulated value by a constant value", "Arithmetic");
        addFunctionCode("MULE", LtRecordType.NAMEF1, "Multiply an accumulated value by an event field value", "Arithmetic");
        addFunctionCode("MULL", LtRecordType.NAMEF1, "Multiply an accumulated value by a lookup field value", "Arithmetic");
        addFunctionCode("MULP", LtRecordType.NAMEF1, "Multiply an accumulated value by a prior event field value", "Arithmetic");
        addFunctionCode("MULA", LtRecordType.NAMEVALUE, "Multiply an accumulated value by an accumulated value", "Arithmetic");
        addFunctionCode("MULX", LtRecordType.NAMEF1, "Multiply a previous extract column value", "Arithmetic");
        addFunctionCode("NV", LtRecordType.NV, "new view", "Directive");
        addFunctionCode("REEX", LtRecordType.RE, "read exit", "Directive");
        addFunctionCode("RENX", LtRecordType.RE, "read next physical file", "Directive");
        addFunctionCode("RETK", LtRecordType.RE, "read token", "Directive");
        addFunctionCode("SETA", LtRecordType.NAMEVALUE, "Set an accumulator from an accumulator", "Arithmetic");
        addFunctionCode("SETC", LtRecordType.NAMEVALUE, "Set an accumulator from a constant", "Arithmetic");
        addFunctionCode("SETE", LtRecordType.NAMEF1, "Set an accumulator from an event field", "Arithmetic");
        addFunctionCode("SETL", LtRecordType.NAMEF1, "Set an accumulator from a lookup field", "Arithmetic");
        addFunctionCode("SETP", LtRecordType.NAMEF1, "Set an accumulator from a prior event field", "Arithmetic");
        addFunctionCode("SETX", LtRecordType.NAMEF1, "Set an accumulator from a prior event field", "Arithmetic");
        addFunctionCode("SFCE", LtRecordType.F1, "Search within a constant for a current source field", "Seacrh");
        addFunctionCode("SFCL", LtRecordType.F1, "Search within a constant for a lookup field", "Seacrh");
        addFunctionCode("SFCP", LtRecordType.F1, "Search within a constant for a prior source field", "Seacrh");
        addFunctionCode("SFCX", LtRecordType.F1, "Search within a constant for a column", "Seacrh");
        addFunctionCode("SFEC", LtRecordType.F1, "Search within a current source field for a constant", "Seacrh");
        addFunctionCode("SFEE", LtRecordType.F2, "Search within a current source field for a current source field", "Seacrh");
        addFunctionCode("SFEL", LtRecordType.F2, "Search within a current source field for a lookup field", "Seacrh");
        addFunctionCode("SFEP", LtRecordType.F2, "Search within a current source field for a prior source field", "Seacrh");
        addFunctionCode("SFEX", LtRecordType.F2, "Search within a current source field for a column", "Seacrh");
        addFunctionCode("SFLC", LtRecordType.F1, "Search within a lookup field for a constant", "Seacrh");
        addFunctionCode("SFLE", LtRecordType.F2, "Search within a lookup field for a current source field", "Seacrh");
        addFunctionCode("SFLL", LtRecordType.F2, "Search within a lookup field for a lookup field", "Seacrh");
        addFunctionCode("SFLP", LtRecordType.F2, "Search within a lookup field for a prior source field", "Seacrh");
        addFunctionCode("SFLX", LtRecordType.F2, "Search within a lookup field for a column", "Seacrh");
        addFunctionCode("SFPC", LtRecordType.F1, "Search within a prior source field for a constant", "Seacrh");
        addFunctionCode("SFPE", LtRecordType.F2, "Search within a prior source field for a current source field", "Seacrh");
        addFunctionCode("SFPL", LtRecordType.F2, "Search within a prior source field for a lookup field", "Seacrh");
        addFunctionCode("SFPP", LtRecordType.F2, "Search within a prior source field for a prior source field", "Seacrh");
        addFunctionCode("SFPX", LtRecordType.F2, "Search within a prior source field for a column", "Seacrh");
        addFunctionCode("SFXC", LtRecordType.F1, "Search within a column for a constant", "Seacrh");
        addFunctionCode("SFXE", LtRecordType.F2, "Search within a column for a current source field", "Seacrh");
        addFunctionCode("SFXL", LtRecordType.F2, "Search within a column for a lookup field", "Seacrh");
        addFunctionCode("SFXP", LtRecordType.F2, "Search within a column for a prior source field", "Seacrh");
        addFunctionCode("SFXX", LtRecordType.F2, "Search within a column for a column", "Seacrh");
        addFunctionCode("SKA", LtRecordType.NAMEF1, "Sort key value from an accumulated value", "Sort");
        addFunctionCode("SKC", LtRecordType.F1, "Sort key value from a constant", "Sort");
        addFunctionCode("SKE", LtRecordType.F2, "Sort key value from an event field", "Sort");
        addFunctionCode("SKL", LtRecordType.F2, "Sort key value from a lookup value", "Sort");
        addFunctionCode("SKP", LtRecordType.F2, "Sort key value from a prior event field", "Sort");
        addFunctionCode("SKX", LtRecordType.F2, "Sort key value from a prior event field", "Sort");
        addFunctionCode("SUBA", LtRecordType.NAMEVALUE, "Subtract an accumulated value from an accumulator", "Arithmetic");
        addFunctionCode("SUBC", LtRecordType.NAMEVALUE, "Subtract a constant value from an accumulator", "Arithmetic");
        addFunctionCode("SUBE", LtRecordType.NAMEF1, "Subtract an event field value from an accumulator", "Arithmetic");
        addFunctionCode("SUBL", LtRecordType.NAMEF1, "Subtract a lookup field value from an accumulator", "Arithmetic");
        addFunctionCode("SUBP", LtRecordType.NAMEF1, "Subtract a prior event field value from an accumulator", "Arithmetic");
        addFunctionCode("SUBX", LtRecordType.NAMEF1, "Subtract a prior event field value from an accumulator", "Arithmetic");
        addFunctionCode("WRDT", LtRecordType.WR, "Write the data record", "Write");
        addFunctionCode("WRIN", LtRecordType.WR, "Write the input record", "Write");
        addFunctionCode("WRSU", LtRecordType.WR, "Write a summarised record", "Write");
        addFunctionCode("WRTK", LtRecordType.WR, "Write to a token", "Write");
        addFunctionCode("WRXT", LtRecordType.WR, "Write the extract record", "Write");
    }

    public abstract void addFunctionCode(String name, LtRecordType type, String desc, String category);

}
