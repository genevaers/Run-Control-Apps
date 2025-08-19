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
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.data.CompilerMessage;

public class ConstStringToDateColumnError extends Rule{ 

    @Override
    public RuleResult apply(final ExtractBaseAST op1, final ExtractBaseAST op2) {
        final ViewColumn vc = ((ColumnAST)op1).getViewColumn();
        FormattedASTNode frhs = (FormattedASTNode) op2;
        ColumnAST colAST = (ColumnAST)op1;
        if(colAST.isNumeric() && op2.getType() == Type.STRINGATOM) {
            CompilerMessage err = ExtractBaseAST.makeCompilerMessage(String.format("Assign %s to numeric column %s.", frhs.getMessageName(), vc.getColumnNumber()));
            Repository.addErrorMessage(err);
            return RuleResult.RULE_WARNING;
        } else if(colAST.getDateCode() != DateCode.NONE && op2.getType() == Type.STRINGATOM) {
            CompilerMessage warn = ExtractBaseAST.makeCompilerMessage(String.format("Assign %s to column %s which has a date code.", frhs.getMessageName(), vc.getColumnNumber()));
            Repository.addWarningMessage(warn);
            return RuleResult.RULE_WARNING;
        } else {
            return RuleResult.RULE_PASSED;
        }
    }
    
}
