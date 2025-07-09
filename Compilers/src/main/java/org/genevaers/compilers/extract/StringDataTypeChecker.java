package org.genevaers.compilers.extract;

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


import org.genevaers.compilers.extract.astnodes.ColumnRefAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.LeftASTNode;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.RightASTNode;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.compilers.extract.astnodes.SubStringASTNode;
import org.genevaers.repository.components.enums.DataType;

public class StringDataTypeChecker {

    public static boolean allows(ExtractBaseAST lhs, ExtractBaseAST rhs, String op) {
        //The operands must both be Alphanumeric
        boolean allowed = true;
        DataType lhsdtype = getDataType(lhs);
        DataType rhsdtype = getDataType(rhs);
        if(lhsdtype != DataType.ALPHANUMERIC || rhsdtype != DataType.ALPHANUMERIC) {
            allowed = false;
        }


        //And we need to think about the bounds
        return allowed;
    }

    public static boolean allowConcatNode(ExtractBaseAST node) {
        return getDataType(node) != DataType.ALPHANUMERIC ? false : true;
    }

    private static DataType getDataType(ExtractBaseAST node) {
        DataType dtype = DataType.INVALID;
        switch(node.getType()) {
            case LRFIELD:
            case PRIORLRFIELD:
            dtype = ((FieldReferenceAST)node).getRef().getDatatype();
            break;
            case LOOKUPFIELDREF:
            dtype = ((LookupFieldRefAST)node).getRef().getDatatype();
            break;
            case COLUMNREF:
            dtype = ((ColumnRefAST) node).getViewColumn().getDataType();
            break;
            case STRINGATOM:
            dtype = ((StringAtomAST)node).getDataType();
            break;
            case SUBSTR:
            dtype = ((SubStringASTNode)node).getDataType();
            break;
            case LEFT:
            dtype = ((LeftASTNode)node).getDataType();
            break;
            case RIGHT:
            dtype = ((RightASTNode)node).getDataType();
            break;
            default:
            break;

        }
        return dtype;
    }

}
