package org.genevaers.compilers.extract.astnodes;

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


import org.genevaers.compilers.extract.emitters.arithmeticemitters.ArithDataTypeCheckerFactory;
import org.genevaers.compilers.extract.emitters.arithmeticemitters.ArithEmitter;
import org.genevaers.compilers.extract.emitters.arithmeticemitters.ArithEmitterFactory;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableName;
import org.genevaers.genevaio.ltfile.LogicTableNameValue;

import org.genevaers.repository.components.enums.LtCompareType;
import org.genevaers.repository.components.enums.LtRecordType;

public class NumericAccumulator extends AccumulatorAST{

    private String operation;

    public NumericAccumulator() {
        type = ASTFactory.Type.NUMACC;
    }

    /**
     * Move the emit declaration to a base function
     * make declration function code a derived class getmethod
     * Then an arith Operation node can call its child accumulator's enit declaration
     * And deal with the lhs and rhs enitters
     * 
     * The local emit will then just be the ADD/SUB/MUL/DIC/SET depending on the operation
     */
    @Override
    public String getDeclarationCode() {
        return "DIMN";
    }

    public void setOperator(String text) {
        operation = text;
    }

    public String getOperation() {
        return operation;
    }
    
    @Override
    public void emit() {
        switch(operation) {
            case "inc":
            emitInc();
            break;
            case "+":
            emitDeclation();
            emitLeftAndRight();
            break;
            case "*":
            emitDeclation();
            emitLeftAndRight();
            break;
            case "-":
            emitDeclation();
            emitLeftAndRight();
            break;
            case "/":
            emitDeclation();
            emitLeftAndRight();
            break;
            default:
            break;
        }
    }

    private void emitLeftAndRight() {
         //Left hand side 
         ExtractBaseAST lhs = (ExtractBaseAST) children.get(0);
         ArithEmitterFactory.init(); //These should be done somewhere else higher up the tree
         ArithDataTypeCheckerFactory.init();
         ArithEmitter lhse = ArithEmitterFactory.getLHSArithEmitter(lhs);
         LTFileObject entry = lhse.generateTableEntry(this, lhs);
         if (entry != null) {
            ltEmitter.addToLogicTable((LTRecord)entry);
         }
 
         //Right hand side
         ExtractBaseAST rhs = (ExtractBaseAST) children.get(1);
         ArithEmitter rhse = ArithEmitterFactory.getRHSArithEmitter(rhs);
         LTFileObject rentry = rhse.generateTableEntry(this, rhs);
         if (rentry != null) {
            ltEmitter.addToLogicTable((LTRecord)rentry);
         }
    }

    private void emitInc() {
        //Make the SET or SETP
        LogicTableNameValue addc = new LogicTableNameValue();
        addc.setRecordType(LtRecordType.NAMEVALUE);
        addc.setFunctionCode("ADDC");
        addc.setValue(new Cookie("1"));
        addc.setTableName(getAccumulatorName());
        addc.setCompareType(LtCompareType.EQ);
        addc.setSuffixSeqNbr((short)accumNumber);
        ltEmitter.addToLogicTable(addc);
    }

}
