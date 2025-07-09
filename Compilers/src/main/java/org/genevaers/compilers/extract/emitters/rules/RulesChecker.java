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


import java.util.ArrayList;
import java.util.List;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.emitters.rules.Rule.RuleResult;

public class RulesChecker {

    List<Rule> localRules = new ArrayList<>();

    public RuleResult apply(ExtractBaseAST op1, ExtractBaseAST op2) {
        return getResult(localRules, op1, op2);
    }

    public RuleResult applyRulesTo(List<Rule> rules, ExtractBaseAST op1, ExtractBaseAST op2) {
        return getResult(rules, op1, op2);
    }

    public void addRule(Rule r) {
        localRules.add(r);
    }
    
    private RuleResult getResult(List<Rule> rules, ExtractBaseAST op1, ExtractBaseAST op2) {
        RuleResult allPassed = RuleResult.RULE_PASSED;
        for (Rule rule : rules) {
            RuleResult ruleResult = rule.apply(op1, op2);
            allPassed = updateResult(allPassed, ruleResult);
        }
        return allPassed;
    }

    protected RuleResult updateResult(RuleResult allPassed, RuleResult ruleResult) {
        return ruleResult.ordinal() > allPassed.ordinal() ? ruleResult : allPassed;
    }

    public void clearRules() {
        localRules.clear();;
    }

}
