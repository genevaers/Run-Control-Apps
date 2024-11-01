package org.genevaers.compilers.extract.emitters.arithmeticemitters;

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


import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.NumericAccumulator;
import org.genevaers.compilers.extract.emitters.arithmeticemitters.ArithDataTypeChecker.ArithResult;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableNameValue;
import org.genevaers.repository.components.enums.LtRecordType;

public class ArithRHSConstEmitter extends ArithEmitter  {

    @Override
    public LTFileObject generateTableEntry(NumericAccumulator nuAccum, ExtractBaseAST type) {
        LogicTableNameValue arithfn = null;
        NumAtomAST num = (NumAtomAST) type;
        ArithDataTypeChecker checker = ArithDataTypeCheckerFactory.getTypeChecker(num.getDataType());
        ArithResult res = checker.verifyArithType(type);
        if(res != ArithResult.ARITH_ERROR) {
            //Make the SET or SETP
            arithfn = new LogicTableNameValue();
            arithfn.setRecordType(LtRecordType.NAMEVALUE);
            switch(nuAccum.getOperation()) {
                case "+":
                arithfn.setFunctionCode("ADDC");
                break;
                case "-":
                arithfn.setFunctionCode("SUBC");
                break;
                case "*":
                arithfn.setFunctionCode("MULC");
                break;
                case "//":
                arithfn.setFunctionCode("DIVC");
                break;
                default:
                break;
            }
            arithfn.setValue(new Cookie(num.getValueString()));
        }
        if(res != ArithResult.ARITH_OK) {
            //we have a message to report
        }
        return arithfn;
    }
    
}
