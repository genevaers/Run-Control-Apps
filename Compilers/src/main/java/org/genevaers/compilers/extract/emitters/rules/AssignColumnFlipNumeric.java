package org.genevaers.compilers.extract.emitters.rules;

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


import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FormattedASTNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;

public class AssignColumnFlipNumeric extends Rule{ 

    @Override
    public RuleResult apply(final ExtractBaseAST op1, final ExtractBaseAST op2) {
        RuleResult result = RuleResult.RULE_PASSED;
        final ViewColumn vc = ((ColumnAST)op1).getViewColumn();
        FormattedASTNode frhs = (FormattedASTNode) op2;
        //if column ALNUM and field is numeric (AND both sides do not have dates)
        if(vc.getExtractArea() != ExtractArea.SORTKEY) {
            if(vc.getDataType() == DataType.ALPHANUMERIC && frhs.isNumeric() && (vc.getDateCode() == DateCode.NONE && frhs.getDateCode() == DateCode.NONE)) {
                ((FormattedASTNode)op1).overrideDataType(DataType.ZONED);
                Repository.addWarningMessage(ExtractBaseAST.makeCompilerMessage(String.format("Treating column %s as ZONED.", vc.getColumnNumber())));
                return RuleResult.RULE_WARNING;
            }
        }
        return result;
    }
}
