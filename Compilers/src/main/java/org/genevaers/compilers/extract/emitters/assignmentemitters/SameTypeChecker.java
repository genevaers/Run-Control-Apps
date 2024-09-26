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
import org.genevaers.compilers.extract.emitters.rules.Truncation;
import org.genevaers.compilers.extract.emitters.rules.Rule.RuleResult;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DateCode;

public class SameTypeChecker extends AssignmentRulesChecker {

    public SameTypeChecker() {
        addRule(new CanAssignDates());
        addRule(new Truncation());
    }

    @Override
    public RuleResult verifyOperands(ColumnAST column, FormattedASTNode rhs) {
        RuleResult result = RuleResult.RULE_PASSED;
        ViewColumn vc = column.getViewColumn();
        FormattedASTNode frhs = (FormattedASTNode) rhs;
        // LRField f = null;
        // if (rhs.getType() == ASTFactory.Type.LRFIELD) {
        //     FieldReferenceAST fr = (FieldReferenceAST) rhs;
        //     f = fr.getRef();
        // } else if (rhs.getType() == ASTFactory.Type.LOOKUPFIELDREF) {
        //     LookupFieldRefAST lfr = (LookupFieldRefAST) rhs;
        //     f = lfr.getRef();
        // }
        if (vc.getDateCode() != DateCode.NONE && frhs.getDateCode() != DateCode.NONE) {
            updateResult(result, apply(column, rhs));
            stripOffDateCodes(column, frhs);
         } else {
            //need to strip here as well but know which side and generate warning
            if(vc.getDateCode() != DateCode.NONE) {
                column.overrideDateCode(DateCode.NONE);
            }
            if(frhs.getDateCode() != DateCode.NONE) {
                frhs.overrideDateCode(DateCode.NONE);
            }
            updateResult(result, apply(column, rhs));
            //generate warning
            //If both sides had compatible dates ok
            //  leave them
            //else 
            //    generate Error
            //checkDateCodeCompatibility();
            //Use bit map!!!!!!!!!!!!!!!!!!!
        }
        return result;
    }


    private void stripOffDateCodes(ColumnAST column, FormattedASTNode frhs) {
            // Stip off the content codes.
            // But in a copy not the original - or use an override type
            // Which is where the TypeASTNode should come in...
            // Would deal with the above functions too
            //if (vc.getFieldLength() != frhs.getLength()) {
                // If the lengths are the same as well then just strip off the content codes
            //}
        frhs.overrideDateCode(DateCode.NONE);
        column.overrideDateCode(DateCode.NONE);
    }

    @Override
    public void generateErrorOrWarning() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateErrorOrWarning'");
    }

}
