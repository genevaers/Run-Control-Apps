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
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.NumericAccumulator;
import org.genevaers.compilers.extract.emitters.arithmeticemitters.ArithDataTypeChecker.ArithResult;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableNameF1;
import org.genevaers.repository.components.LRField;

public class ArithLHSFieldEmitter extends ArithEmitter {

    @Override
    public LTFileObject generateTableEntry(NumericAccumulator nu, ExtractBaseAST type) {
        LTFileObject setfn = null;
        FieldReferenceAST fr = (FieldReferenceAST) type;
        ArithDataTypeChecker checker = ArithDataTypeCheckerFactory.getTypeChecker(fr.getDataType());
        ArithResult res = checker.verifyArithType(type);
        LRField field = ((FieldReferenceAST)type).getRef();
        if(res != ArithResult.ARITH_ERROR) {
            //Make the SET or SETP
            LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
            setfn = fcf.getSETE(nu.getAccumulatorName(), field);
            //need to set the additional entries of suffix etc...
            //These are done as part of the general add to logic table if none 0
            //Should they always be done there?
            ((LogicTableNameF1)setfn).getArg().setLogfileId(ExtractBaseAST.getLtEmitter().getFileId());
        }
        if(res != ArithResult.ARITH_OK) {
            //we have a message to report
        }
        return setfn;
    }
    
    
}
