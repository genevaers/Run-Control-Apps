package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.repository.components.LookupPath;

import com.google.common.flogger.FluentLogger;

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


public class LookupPathRefAST extends LookupPathAST {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public LookupPathRefAST() {
        type = ASTFactory.Type.LOOKUPREF;
    }

    public void setLookup(LookupPath lookup) {
        this.lookup = lookup;
    }
}
