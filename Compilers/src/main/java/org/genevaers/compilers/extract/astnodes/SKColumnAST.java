package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
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


public class SKColumnAST extends ColumnAST {
    private ViewSortKey vsk;
    public SKColumnAST(ViewColumn vc) {
        type = ASTFactory.Type.SK_COLUMN;
        vsk = Repository.getViews().get(vc.getViewId()).getViewSortKeyFromColumnId(vc.getComponentId());
        this.vc = vc;
    }

    @Override
    public void emit() {
        //Nothing to do... maybe we should be using an interface?
        currentViewColumn = vc;
        if(getNumberOfChildren() > 0) {
            //Must have a sort title
            ExtractBaseAST c = (ExtractBaseAST)getChild(0);
            if(c.getType() == ASTFactory.Type.SORTTITLE ) {
                SortTitleAST st = (SortTitleAST)c;
                st.emit();
            }
        }
    }

    @Override
    public LTFileObject getAccumLtEntry(String accumulatorName) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        return fcf.getSKA(accumulatorName, vc);
    }

    @Override
    public LTFileObject getFieldLtEntry(LRField field) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableF2 ske = (LogicTableF2) fcf.getSKE(field, vc, vsk);
        return ske;
    }

    @Override
    public LTFileObject getPriorFieldLtEntry(LRField field) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        return fcf.getSKP(field, vc, vsk);
    }

    @Override
    public LTFileObject getConstLtEntry(String value) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableF1 skc = (LogicTableF1) fcf.getSKC(value, vc, vsk);
        return skc;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : vsk.getSortKeyDataType();
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : vsk.getSortKeyDateTimeFormat();
    }

    @Override
    public String getMessageName() {
        return "Sort Key" + vsk.getSequenceNumber();
    }
    
    @Override
    public int getMaxNumberOfDigits() {
        return RepoHelper.getMaxNumberOfDigitsForType(vsk.getSortKeyDataType(), vsk.getSkFieldLength());
    }
}
