package org.genevaers.compilers.extract.astnodes;

import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;

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


public class RepeatAST extends FormattedASTNode implements GenevaERSValue, Assignable{

    private String value;

    public RepeatAST() {
        type = ASTFactory.Type.REPEAT;
    }

    public void setValue(String value) {
        this.value = value.replace("\"", "");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST lhs, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        if(currentViewColumn.getExtractArea() == ExtractArea.AREACALC) {
            ltEmitter.addToLogicTable((LTRecord)fcf.getCTC(getRepeatString(), currentViewColumn));
        } else if(currentViewColumn.getExtractArea() == ExtractArea.AREADATA) {
            ltEmitter.addToLogicTable((LTRecord)fcf.getDTC(getRepeatString(), currentViewColumn));
        } else {
            ViewSortKey sk = Repository.getViews().get(currentViewColumn.getViewId()).getViewSortKeyFromColumnId(currentViewColumn.getComponentId());
            ltEmitter.addToLogicTable((LTRecord)fcf.getSKC(getRepeatString(), currentViewColumn, sk));
        }
        return null;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    public String getRepeatString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<Integer.parseInt(value); i++) {
            StringAtomAST sa = (StringAtomAST)getChildIterator().next();
            if(sa != null) {
                sb.append(sa.getValueString());
            }
        }
        return sb.toString();
    }

    @Override
    public String getMessageName() {
        return "repeat";
    }

    @Override
    public int getAssignableLength() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFormattedLength'");
    }

    @Override
    public int getMaxNumberOfDigits() {
        return value.length();
    }

}
