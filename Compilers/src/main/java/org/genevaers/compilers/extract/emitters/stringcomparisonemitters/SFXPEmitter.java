package org.genevaers.compilers.extract.emitters.stringcomparisonemitters;

import org.genevaers.compilers.extract.astnodes.ColumnRefAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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


import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;

public class SFXPEmitter extends StringComparisonEmitter{

    @Override
    public LTFileObject getLTEntry(String op, ExtractBaseAST lhs, ExtractBaseAST rhs) {
        LtFuncCodeFactory ltFact = LtFactoryHolder.getLtFunctionCodeFactory();
        ltFact.setLogFileId(getLtEmitter().getFileId());
        LogicTableF2 sfxp = (LogicTableF2) ltFact.getSFXP(((ColumnRefAST) lhs).getViewColumn(), ((FieldReferenceAST) rhs).getRef());
        LogicTableArg arg1 = sfxp.getArg1();
        arg1.setFieldId(((ColumnRefAST) lhs).getViewColumn().getColumnNumber());
        arg1.setLogfileId(((ColumnRefAST) lhs).getViewColumn().getExtractArea().ordinal());
        return sfxp;
    }

}
