package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.EmittableASTNode;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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


public class BooleanAndAST extends ExtractBaseAST implements EmittableASTNode{

    Integer andEnd = 0;

    public BooleanAndAST() {
        type = ASTFactory.Type.BOOLAND;
    }

    @Override
    public void emit() {
        EmittableASTNode lhs = (EmittableASTNode) children.get(0);
        EmittableASTNode rhs = (EmittableASTNode) children.get(1);
        lhs.emit();
        andEnd = ltEmitter.getNumberOfRecords();
        rhs.emit();
    }

    @Override
    public void resolveGotos(Integer compT, Integer compF, Integer joinT, Integer joinF) {
        // resolve children
        ExtractBaseAST lhs = (ExtractBaseAST) children.get(0);
        ExtractBaseAST rhs = (ExtractBaseAST) children.get(1);
        if (lhs != null && rhs != null) {
            if (isNot) {
                lhs.resolveGotos(andEnd, compT, joinT, compT);
                rhs.resolveGotos(compF, compT, joinT, compT);
            } else {
                lhs.resolveGotos(andEnd, compF, joinT, joinF);
                rhs.resolveGotos(compT, compF, joinT, joinF);
            }
        }
    }

}
