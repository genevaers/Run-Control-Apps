package org.genevaers.compilers.extract.emitters.assignmentemitters;

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


import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.jltviews.JLTView;

public class AssignmentLookupEmitter extends AssignmentEmitter{

    @Override
    public LTFileObject makeLTEntry(ExtractBaseAST lhs, ExtractBaseAST rhs) {
        LookupFieldRefAST lkf = (LookupFieldRefAST)rhs;
        lkf.getLkEmitter().emitJoin(lkf, false);
        emitDTL(lhs, lkf);
        return null;
    }

    private LogicTableF2 emitDTL(ExtractBaseAST lhs, LookupFieldRefAST lkf) {
        ViewColumn vc = ((ColumnAST)lhs).getViewColumn();
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        LogicTableF2 ltEntry = null;
        JLTView jv = Repository.getJoinViews().getJLTViewFromLookup(lkf.getLookup(), false);
        LRField redFld = jv.getRedFieldFromLookupField(lkf.getRef().getComponentId());
        if(redFld != null) {
            if(vc.getExtractArea() == ExtractArea.SORTKEY) {
                ViewSortKey sk = Repository.getViews().get(vc.getViewId()).getViewSortKeyFromColumnId(vc.getComponentId());
                ltEntry = (LogicTableF2)(fcf.getSKL(redFld, vc, sk));
            } else {
                ltEntry = (LogicTableF2)(fcf.getDTL(redFld, vc));
            }
            ((LogicTableF2)ltEntry).getArg1().setLogfileId(getLtEmitter().getFileId());        
        }
        LogicTableArg arg1 = ((LogicTableF2)ltEntry).getArg1();
        arg1.setLogfileId(lkf.getLookup().getTargetLFID());
        arg1.setLrId(lkf.getRef().getLrID());
        arg1.setFieldId(lkf.getRef().getComponentId());
        arg1.setFieldContentId(DateCode.NONE);
        ((LogicTableF2)ltEntry).getArg2().setFieldContentId(DateCode.NONE);
        ExtractBaseAST.getLtEmitter().addToLogicTable(ltEntry);
        return ltEntry;
    }
    
    public boolean isLookup() {
        return true;
    }

}
