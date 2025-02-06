package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;

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


import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.repository.components.enums.DataType;

public class SubStringASTNode extends StringFunctionASTNode implements Assignable{

    String startOffest = "0";

    public SubStringASTNode() {
        type = ASTFactory.Type.SUBSTR;
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        if(getNumberOfChildren() == 1) {
            Concatable cc =  (Concatable) getChildIterator().next();
            cc.getSubstreEntry(col, (ExtractBaseAST) cc, Short.valueOf(getStartOffest()), (short)getLength());
        }
        return null;
    }

    public String getStartOffest() {
        return startOffest;
    }

    public int getStartOffestInt() {
        return Integer.parseInt(startOffest);
    }

    public void setStartOffest(String startOffest) {
        this.startOffest = startOffest;
    }

    @Override
    public int getAssignableLength() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAssignableLength'");
    }
 
    @Override
    public DataType getDataType() {
        return DataType.ALPHANUMERIC;
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

}
