package org.genevaers.compilers.extract.emitters.assignmentemitters;

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
import org.genevaers.compilers.extract.emitters.rules.CanAssignDates;
import org.genevaers.compilers.extract.emitters.rules.ColumnZonedMaxLength;
import org.genevaers.compilers.extract.emitters.rules.Rule.RuleResult;
import org.genevaers.compilers.extract.emitters.rules.Truncation;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.repository.data.CompilerMessageSource;

public class FlipColumnChecker extends AssignmentRulesChecker {

    public FlipColumnChecker() {
        addRule(new ColumnZonedMaxLength());  //Should be able use static?
        addRule(new CanAssignDates());
        addRule(new Truncation());
}

    @Override
    public RuleResult verifyOperands(ColumnAST column, FormattedASTNode rhs) {
        RuleResult result = RuleResult.RULE_WARNING;
        //We already know the column is Alphanumeric otherwise we would not be here
        column.overrideDataType(DataType.ZONED);
        ViewColumn vc = column.getViewColumn();
        updateResult(result, apply(column, rhs));
        CompilerMessage warn = ExtractBaseAST.makeCompilerMessage(String.format("Treating column %d as ZONED.", column.getViewColumn().getColumnNumber()));
        Repository.addWarningMessage(warn);

        // Change the alnum data type to zoned
        // Then treat as a DateChecker
            // We don't want to change the actual data type of the column
            // Just how we treat it.
            // That is where the ArithInfo came in.
            // And the Formatted AST Node..
            // Also allows management of the casting
            // Which in C++ is called in the generateASTValueRef or generateASTUnaryNode in  ExtractParserBase
        return result;
    }

    @Override
    public void generateErrorOrWarning() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateErrorOrWarning'");
    }
    
}
