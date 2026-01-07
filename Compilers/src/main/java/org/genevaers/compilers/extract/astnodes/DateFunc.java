package org.genevaers.compilers.extract.astnodes;

import org.antlr.v4.runtime.tree.ParseTree;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.data.NormalisedDate;

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


public class DateFunc extends FormattedASTNode implements GenevaERSValue,Assignable{

    private String value;
    private String dateCodeStr;
    private DateCode dateCode;

    public DateFunc() {
        type = ASTFactory.Type.DATEFUNC;
    }

    public String getValue() {
        return value;
    }

    public void resolve(String dateStr, String format) {
        //TODO We need to think about this some more 
        //Check the string matches the format etc
        value = dateStr.replace("\"", "");
        dateCodeStr = format.replace("\"", "");
        dateCode = DateCode.fromValue(format);
    }

    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : dateCode;
    }

    public String getDateCodeStr() {
        return dateCodeStr;
    }

    public String getNormalisedDate() {
        return NormalisedDate.get(value, getDateCode());
    }

    public int getCookieCode() {
        return 0;
    }

    @Override
    public String getMessageName() {
        return "date function";
    }

    @Override
    public int getMaxNumberOfDigits() {
        return dateCodeStr.length();
    }

     @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LTRecord fc;
        if(currentViewColumn.getExtractArea() == ExtractArea.AREACALC) {
            fc = (LTRecord) fcf.getCTC(String.valueOf(value), currentViewColumn);
        } else if(currentViewColumn.getExtractArea() == ExtractArea.AREADATA) {
            fc = (LTRecord) fcf.getDTC(String.valueOf(value), currentViewColumn);
        } else {
            ViewSortKey sk = Repository.getViews().get(currentViewColumn.getViewId()).getViewSortKeyFromColumnId(currentViewColumn.getComponentId());
             fc = (LTRecord) fcf.getSKC(String.valueOf(value), currentViewColumn, sk);
        }
        expandArgCookieValue((LogicTableF1)fc);
        fc.setSourceSeqNbr((short) (ltEmitter.getLogicTable().getNumberOfRecords() + 1));
        ltEmitter.addToLogicTable((LTRecord)fc);
        return null;
    }

     private void expandArgCookieValue(LogicTableF1 f) {
        LogicTableArg arg = f.getArg();
        arg.setValue(new Cookie(getCookieCode(), getValue()));
    }

     @Override
     public int getAssignableLength() {
        return getMaxNumberOfDigits();
     }

}
