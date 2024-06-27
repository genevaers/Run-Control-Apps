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


import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FormattedASTNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.enums.DateCode;

public class CanCompareDates extends Rule{ 

    private static final String WARN_MESSAGE = "Incompatible date formats comparing %s to %s";

    @Override
    public RuleResult apply(final ExtractBaseAST lhs, final ExtractBaseAST rhs) {
        FormattedASTNode frmtLhs = ((FormattedASTNode) lhs);
        FormattedASTNode frmtRhs = ((FormattedASTNode) rhs);
        if (frmtLhs.getDateCode() != DateCode.NONE &&  frmtRhs.getDateCode() != DateCode.NONE) {
            if(canCompareDates(frmtLhs.getDateCode(), frmtRhs.getDateCode())) {
                return RuleResult.RULE_PASSED;                
            } else {
                Repository.addWarningMessage(ExtractBaseAST.makeCompilerMessage(String.format(WARN_MESSAGE, frmtLhs.getDateCode(), frmtRhs.getDateCode())));
                return RuleResult.RULE_WARNING;
            }
        } else {
                return RuleResult.RULE_PASSED;
        }
    }
}
