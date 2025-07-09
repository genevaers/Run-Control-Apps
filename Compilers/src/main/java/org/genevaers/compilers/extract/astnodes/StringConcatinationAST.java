package org.genevaers.compilers.extract.astnodes;

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


import java.util.Iterator;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import com.google.common.flogger.FluentLogger;

public class StringConcatinationAST extends FormattedASTNode implements Assignable{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public StringConcatinationAST() {
        type = ASTFactory.Type.STRINGCONCAT;
    }

    @Override
    public LTFileObject getAssignmentEntry(ColumnAST col, ExtractBaseAST rhs) {
        //Iterate through the child nodes and get the appropriate function code;
        Iterator<ASTBase> ci = children.iterator();
        short currentPos = col.getViewColumn().getStartPosition();
        while(ci.hasNext()) {
            Concatable c = (Concatable) ci.next();
            //We need to do different things depending on the source type
            //so have a specifc function implemented in the type
            //getConcatination entry - return length so we can increase the current pos
            // make them implement concatable 
            currentPos += c.getConcatinationEntry(col, (ExtractBaseAST)c, currentPos);
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

    @Override
    public String getMessageName() {
        return "string concatination";
    }

    @Override
    public int getAssignableLength() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFormattedLength'");
    }

    @Override
    public int getMaxNumberOfDigits() {
        return 99; //just for something
    }

}
