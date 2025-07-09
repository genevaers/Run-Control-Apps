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


import java.util.ArrayList;
import java.util.List;

import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FormattedASTNode;
import org.genevaers.compilers.extract.emitters.rules.AssignColumnFlipNumeric;
import org.genevaers.compilers.extract.emitters.rules.ConstStringToDateColumnError;
import org.genevaers.compilers.extract.emitters.rules.FieldFlipNumeric;
import org.genevaers.compilers.extract.emitters.rules.FieldZonedMaxLength;
import org.genevaers.compilers.extract.emitters.rules.Rule;
import org.genevaers.compilers.extract.emitters.rules.Truncation;
import org.genevaers.compilers.extract.emitters.rules.Rule.RuleResult;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;

public class FlipDataChecker extends AssignmentRulesChecker {

    public FlipDataChecker() {
        addRule(new ConstStringToDateColumnError());
        addRule(new Truncation());
        addRule(new AssignColumnFlipNumeric());
        addRule(new FieldFlipNumeric());
    }

    @Override
    public RuleResult verifyOperands(ColumnAST column, FormattedASTNode rhs) {
        RuleResult result = RuleResult.RULE_WARNING;
        ViewColumn vc = column.getViewColumn();
        result = updateResult(result, apply(column, rhs));
        if (result != RuleResult.RULE_ERROR) {
            if(vc.getDataType() != DataType.ALPHANUMERIC && !rhs.isNumeric()) {
                Repository.addWarningMessage(ExtractBaseAST.makeCompilerMessage(String.format("Treating field {%s} as ZONED.", rhs.getMessageName())));
            }
            List<Rule> flippedRules = new ArrayList<>();
            flippedRules.add(new FieldZonedMaxLength());
            result = updateResult(result, applyRulesTo(flippedRules, column, rhs));
        }

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
