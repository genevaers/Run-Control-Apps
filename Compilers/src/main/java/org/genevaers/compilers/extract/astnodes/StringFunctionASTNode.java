package org.genevaers.compilers.extract.astnodes;

import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;

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


import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.jltviews.JLTView;

public class StringFunctionASTNode extends FormattedASTNode implements Assignable{

    protected String startOffest = "0";
    protected String length;

    public int getLength() {
        return Integer.valueOf(length);
    }

    public String getLengthString() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public DataType getDataType() {
        return overriddenDataType != DataType.INVALID ? overriddenDataType : DataType.ALPHANUMERIC;
    }

    @Override
    public DateCode getDateCode() {
        return (overriddenDateCode != null) ? overriddenDateCode : DateCode.NONE;
    }

    @Override
    public int getMaxNumberOfDigits() {
        return Integer.valueOf(length);
    }

    public int getStartOffestInt() {
        return Integer.parseInt(startOffest);
    }

    public String getStartOffest() {
        return startOffest;
    }

    public void setStartOffest(String startOffest) {
        this.startOffest = startOffest;
    }

    @Override
    public String getMessageName() {
        ExtractBaseAST c = (ExtractBaseAST) getChild(0);
        String name = "";
        switch (c.getType()) {
            case LRFIELD:
                name = ((FieldReferenceAST)c).getMessageName();
                break;
            case PRIORLRFIELD:
                name = ((FieldReferenceAST)c).getMessageName();
                break;
            case LOOKUPFIELDREF:
                name = ((LookupFieldRefAST)c).getMessageName();                
                break;
            case COLUMNREF:
                name = ((ColumnRefAST)c).getMessageName();                                
                break;
        
            default:
                break;
        }
        return name;
    }

    public int getChildStartPosition() {
        ExtractBaseAST c = (ExtractBaseAST) getChild(0);
        switch (c.getType()) {
            case LRFIELD:
            case PRIORLRFIELD:
                return ((FieldReferenceAST)c).getRef().getStartPosition();
            case LOOKUPFIELDREF:
                LookupFieldRefAST lkf = (LookupFieldRefAST) getChild(0);
                JLTView jv = Repository.getJoinViews().getJLTViewFromLookup(lkf.getLookup(), false);
                LRField redFld = jv.getRedFieldFromLookupField(lkf.getRef().getComponentId());
                return redFld.getStartPosition();
            case COLUMNREF:
                return ((ColumnRefAST)c).getViewColumn().getExtractAreaPosition();
            default:
                return 0;
        }
    }

    public int getChildLength() {
        ExtractBaseAST c = (ExtractBaseAST) getChild(0);
        switch (c.getType()) {
            case LRFIELD:
            case PRIORLRFIELD:
                return ((FieldReferenceAST)c).getRef().getLength();
            case LOOKUPFIELDREF:
                return ((LookupFieldRefAST)c).getRef().getLength();
            case COLUMNREF:
                return ((ColumnRefAST)c).getViewColumn().getFieldLength();
            default:
                return 0;
        }
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        if(getNumberOfChildren() == 1) {
            Concatable cc =  (Concatable) getChildIterator().next();
            cc.getLeftEntry(col, (ExtractBaseAST) cc, (short)getLength());
        }
        return null;
    }

    @Override
    public int getAssignableLength() {
        return getLength();
    }

}
