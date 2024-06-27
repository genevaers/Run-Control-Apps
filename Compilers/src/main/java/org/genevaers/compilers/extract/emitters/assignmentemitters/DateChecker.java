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
import org.genevaers.compilers.extract.astnodes.FormattedASTNode;
import org.genevaers.compilers.extract.emitters.rules.CanAssignDates;
import org.genevaers.compilers.extract.emitters.rules.ColumnStripDate;
import org.genevaers.compilers.extract.emitters.rules.FieldStripDate;
import org.genevaers.compilers.extract.emitters.rules.Truncation;
import org.genevaers.compilers.extract.emitters.rules.Rule.RuleResult;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DateCode;

public class DateChecker extends AssignmentRulesChecker {

    public DateChecker() {
        addRule(new CanAssignDates());
        addRule(new ColumnStripDate());
        addRule(new FieldStripDate());
        addRule(new Truncation());
    }

    @Override
    public RuleResult verifyOperands(ColumnAST column, FormattedASTNode rhs) {
        RuleResult result = RuleResult.RULE_PASSED;
        ViewColumn vc = column.getViewColumn();
        FormattedASTNode frhs = (FormattedASTNode) rhs;
        if (vc.getDateCode() != DateCode.NONE && frhs.getDateCode() != DateCode.NONE) {
            updateResult(result, apply(column, rhs));
        } else {
            updateResult(result, apply(column, rhs));
        }
        return result;
    }

    @Override
    public void generateErrorOrWarning() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateErrorOrWarning'");
    }
    
}
