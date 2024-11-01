package org.genevaers.compilers.extract.emitters.lookupemitters;

import org.genevaers.compilers.extract.emitters.helpers.EmitterArgHelper;

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


import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.ArgHelper;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.jltviews.JLTView;

public class LKFieldEmitter extends LookupEmitter {

    private int srcLFID;

    public LogicTableF2 emit(LookupPathKey lkpkey, LookupPath lookup) {
        // There should be a valid converson check done


        LtFuncCodeFactory ltfact = LtFactoryHolder.getLtFunctionCodeFactory();
        //Can be an LKE or and LKL
        LogicTableF2 lk;
        if(lkpkey.getStepNumber() == 1) {
            lk = (LogicTableF2) ltfact.getLKE(Repository.getFields().get(lkpkey.getFieldId()), lkpkey);
            LogicTableArg arg1 = lk.getArg1();
            arg1.setLogfileId(srcLFID);
        } else {
            //we need the the mapped field not the original
            int keyfieldLR = Repository.getFields().get(lkpkey.getFieldId()).getLrID();
            //does key field come from the event LR?
            if(keyfieldLR == lookup.getStep(1).getSourceLR()) {
                lk = (LogicTableF2) ltfact.getLKE(Repository.getFields().get(lkpkey.getFieldId()), lkpkey);
                LogicTableArg arg1 = lk.getArg1();
                arg1.setLogfileId(srcLFID);
        } else { //Keyfield is from a previous step
                JLTView jltvOfTargetSourceStep = Repository.getJoinViews().getJltViewFromKeyField(lkpkey.getFieldId(), lookup);
                LRField redfld = jltvOfTargetSourceStep.getRedFieldFromLookupField(lkpkey.getFieldId());
                lk = (LogicTableF2) ltfact.getLKL(Repository.getFields().get(lkpkey.getFieldId()), lkpkey);
                if(redfld != null) {
                    LogicTableArg arg1 = lk.getArg1();
                    arg1.setStartPosition((short)(redfld.getStartPosition())); // Remap to RED LR position
                    arg1.setLogfileId(jltvOfTargetSourceStep.getSourceLF());
                }
            }
        }
        EmitterArgHelper.checkAndFixDateCodes(lk);
        return lk;
    }

    public void setLogicalFileID(int lfid) {
        srcLFID = lfid;
    }

}
