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
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;

public class ColumnZonedMaxLength extends Rule{ 

    private static final String ERR_MESSAGE = "Column %d length exceeds maximum ZONED length of %d";

    @Override
    public RuleResult apply(final ExtractBaseAST op1, final ExtractBaseAST op2) {
        final ViewColumn vc = ((ColumnAST)op1).getViewColumn();
        if(vc.getFieldLength() < MAX_ZONED_LENGTH ) {
            return RuleResult.RULE_PASSED;
        } else {
            Repository.addErrorMessage(ExtractBaseAST.makeCompilerMessage(String.format(ERR_MESSAGE, vc.getColumnNumber(), MAX_ZONED_LENGTH)));
            return RuleResult.RULE_ERROR;
        }
    }
    
}
