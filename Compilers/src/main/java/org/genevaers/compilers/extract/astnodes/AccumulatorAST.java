package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.genevaio.ltfile.LogicTableName;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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


import org.genevaers.repository.components.enums.LtRecordType;

public class AccumulatorAST extends ExtractBaseAST implements EmittableASTNode{

    protected int accumNumber;
    private String name;
    
    public enum AcummType {
        GENEVA_AC,
        DATE_AC,
        STRING_AC,
        BIN4
    }
    
    //Force implementing classes to emit an accumulator
    public void emitDeclation() {
        LogicTableName ltn = new LogicTableName();
        ltn.setAccumulatorName(name);
        ltn.setRecordType(LtRecordType.NAME);
        ltn.setFunctionCode(getDeclarationCode());
        ltEmitter.addToLogicTable(ltn);
    }

    protected String getDeclarationCode() {
        return "??";
    }

    public String getAccumulatorName() {
        return name;
    }
    
    public void setAccumulatorName(String accumulatorName) {
        this.name = accumulatorName;
    }

    public int getAccumNumber() {
        return accumNumber;
    }

    public void setAccumNumber(int accumNumber) {
        this.accumNumber = accumNumber;
    }

    @Override
    public void emit() {
        emitDeclation();
    }


}
