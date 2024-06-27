package org.genevaers.compilers.extract.astnodes;

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


import org.genevaers.compilers.base.EmittableASTNode;
public class IsFoundAST extends ExtractBaseAST implements EmittableASTNode{

    public IsFoundAST() {
        type = ASTFactory.Type.ISFOUND;
    }

    @Override
    public void emit() {
        LookupPathRefAST lkref = (LookupPathRefAST)getChildIterator().next();
        lkref.getLkEmitter().emitJoin(lkref, false);
    }

    @Override
    public void resolveGotos(Integer compT, Integer compF, Integer joinT, Integer joinF) {
        ExtractBaseAST source = (ExtractBaseAST) children.get(0);
        source.resolveGotos(compT, compF, compT, compF);
    }

}
