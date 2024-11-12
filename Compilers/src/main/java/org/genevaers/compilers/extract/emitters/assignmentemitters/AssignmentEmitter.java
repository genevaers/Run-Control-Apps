package org.genevaers.compilers.extract.emitters.assignmentemitters;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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


import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FormattedASTNode;
import org.genevaers.compilers.extract.emitters.CodeEmitter;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.repository.components.ViewColumn;

/**
 * Base class for Assignment Emitters.
 * 
 * The {@see #getLTEntry(ExtractBaseAST, ExtractBaseAST)} is the function used by the compiler to emit the appropriate function code
 * to assign a value to a column.
 * 
 * Internally it is uses the {@see AssignmentDataCheckerFactory} to verify the data types of the assignment statement.
 * The {@see AssignmentEmitterFactory} is then used to get the concrete emitter.
 * The concrete emitter used is determined by the type of the RHS.
 */
public abstract class AssignmentEmitter extends CodeEmitter {

    //C++ has a 16 x 16 table/array of data types from which to select the functionoid
    //But with the relaxing of the rules the table may now be uncessary?
    //  we have
    // AssignmentErrorFoid         AssignmentEmitter::m_Error; 
    // AssignmentDateFoid          AssignmentEmitter::m_dater;
    // AssignmentSameTypeDateFoid  AssignmentEmitter::m_sameT;
    // AssignmentFlipperFoid       AssignmentEmitter::m_flip;

    // The datatypes used are an extended version of the native Mainframe types
    // Since they include Constan Num, Constant String and Constant Date.
    // Should be able to manage these via ASTFactory.Type?
    // 


    // in the same way that the ANTLR parser returns static context classes 
    // we can return static Emitter classes

    // And the Checker can return different checker types
 
    /** 
    * Get the function code to implement the assignment defined by the parameter types.
    */
    public LTFileObject getLTEntry(ExtractBaseAST columnAST, ExtractBaseAST rhs) {
        LTFileObject lte = null;
        // Get a DataType checker
        AssignmentRulesChecker rulesChecker = AssignmentRulesCheckerFactory.getChecker((ColumnAST)columnAST, (FormattedASTNode)rhs);
        rulesChecker.verifyOperands((ColumnAST)columnAST, (FormattedASTNode)rhs);
        AssignmentEmitter ae =  AssignmentEmitterFactory.getAssignmentEmitter(rhs);
        lte = ae.makeLTEntry(columnAST, rhs);        
        return lte;
    }

    /** Classes that extend from here will implement this function
    * to make the appropriate function code(s).
    */
    public abstract LTFileObject makeLTEntry(ExtractBaseAST lhs, ExtractBaseAST rhs);

    protected LogicTableArg getColumnArg(ViewColumn vc) {
        LogicTableArg colarg = new LogicTableArg(); //This is the column data
        colarg.setDecimalCount(vc.getDecimalCount());
        colarg.setFieldContentId(vc.getDateCode());
        colarg.setFieldFormat(vc.getDataType());
        colarg.setFieldId(0);
        //TODO the start pos is dependent on extract type
        colarg.setStartPosition(vc.getStartPosition());
        colarg.setFieldLength(vc.getFieldLength());
        colarg.setJustifyId(vc.getJustifyId());
        colarg.setSignedInd(vc.isSigned());
        colarg.setValue(new Cookie(""));
        colarg.setPadding2("");  //This seems a little silly
        return colarg;
    }

    public boolean isLookup() {
        return false;
    }

}
