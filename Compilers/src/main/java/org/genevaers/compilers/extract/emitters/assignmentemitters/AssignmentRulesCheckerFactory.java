package org.genevaers.compilers.extract.emitters.assignmentemitters;

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


import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FormattedASTNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.ExtractArea;

public class AssignmentRulesCheckerFactory {

    private static ErrorChecker errorChecker = new ErrorChecker();
    private static DateChecker dateChecker = new DateChecker();
    private static FlipColumnChecker flipColumnChecker = new FlipColumnChecker();
    private static FlipDataChecker flipChecker = new FlipDataChecker();
    private static SameTypeChecker sameTypesChecker = new SameTypeChecker();

    private AssignmentRulesCheckerFactory() {}

    // C++ takes the original data types and adds constant num, strin and date to the matrix
    // Do we need a matrix or can we program it?

    // get types as numeric or string and masked as a special
    // and same or just do same within the dater?
    // if src and targ same type call date check
    // else call flipper
    // can't assign to a const... but compiler should not all this anyway?
    public static AssignmentRulesChecker getChecker(ColumnAST column, FormattedASTNode rhs) {
        ViewColumn vc = column.getViewColumn();
        DataType colDataType;
        if(vc.getExtractArea() == ExtractArea.SORTKEY) {
            ViewSortKey sk = Repository.getViews().get(vc.getViewId()).getViewSortKeyFromColumnId(vc.getComponentId());
            column.overrideDataType(sk.getSortKeyDataType());
            column.overrideDateCode(sk.getSortKeyDateTimeFormat());
            colDataType = sk.getSortKeyDataType();
        } else {
            colDataType = vc.getDataType();
        }
        if (sameDataTypes(colDataType, rhs)) {
            return sameTypesChecker;
        } else {
            if(colDataType != DataType.ALPHANUMERIC && rhs.isNumeric()) {
                return dateChecker;
            } else {
                if(!column.isNumeric()) {
                    return flipColumnChecker;
                } else {
                    return flipChecker;
                }
            }
        }
    }

    private static boolean sameDataTypes(DataType colDataType, ExtractBaseAST rhs) {
        return colDataType == ((FormattedASTNode)rhs).getDataType();
    }

}
