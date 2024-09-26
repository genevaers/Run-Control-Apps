package org.genevaers.compilers.extract.astnodes;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;

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

public class DTColumnAST extends ColumnAST {

    public DTColumnAST(ViewColumn vc) {
        type = ASTFactory.Type.DT_COLUMN;
        this.vc = vc;
    }

    @Override
    public void emit() {
        //Nothing to do... maybe we should be using an interface?
        currentViewColumn = vc;
    }

    @Override
    public LTFileObject getAccumLtEntry(String accumulatorName) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        return fcf.getDTA(accumulatorName, vc);
    }

    @Override
    public LTFileObject getFieldLtEntry(LRField field) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        return fcf.getDTE(field, vc);
    }

    @Override
    public LTFileObject getPriorFieldLtEntry(LRField field) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        return fcf.getDTP(field, vc);
    }

    @Override
    public LTFileObject getConstLtEntry(String value) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        return fcf.getDTC(value, vc);
    }

    @Override
    public DataType getDataType() {
        if(vc != null) {
            return overriddenDataType != DataType.INVALID ? overriddenDataType : vc.getDataType();
        } else {
            return overriddenDataType;
        }
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : vc.getDateCode();
    }

    @Override
    public int getMaxNumberOfDigits() {
         return RepoHelper.getMaxNumberOfDigitsForType(vc.getDataType(), vc.getFieldLength());
    }

    @Override
    public String getMessageName() {
        return vc.getName();
    }

}
