package org.genevaers.compilers.extract.astnodes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.genevaio.ltfile.Cookie;
import org.genevaers.genevaio.ltfile.LogicTableF0;
import org.genevaers.genevaio.ltfile.LogicTableGeneration;
import org.genevaers.genevaio.ltfile.LogicTableHD;
import org.genevaers.genevaio.ltfile.LogicTableName;
import org.genevaers.genevaio.ltfile.LogicTableNameValue;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.enums.LtCompareType;
import org.genevaers.repository.components.enums.LtRecordType;

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


public class GenevaERSJoinsRoot extends ExtractBaseAST implements EmittableASTNode {
    

    GenevaERSJoinsRoot() {
        type = ASTFactory.Type.ERSJOINSROOT;
    }


    @Override
    public void emit() {
        emitGeneration();
        emitHDRecord();
        addDeclareRecordCount();
        zeroRecordCount();
        Iterator<ASTBase> ci = children.iterator();
        while(ci.hasNext()) {
            EmittableASTNode c = (EmittableASTNode) ci.next();
            c.emit();
            if(ci.hasNext()) {
                zeroRecordCount();
            }
        }
        emitEN();

        //Now we should go back and update the generation record counts
        LogicTableGeneration gen = ltEmitter.getGenerationRecord();
        gen.setExtract(false);
        ltEmitter.updateGenerationCounters();
        gen.setReccnt(ltEmitter.getNumberOfRecords()-1); //Do not include the gen.
        resolveGotosTop();
    }



    // TODO this stuff is duplicated from GenevaERSRoot
    private void emitHDRecord() {
        LogicTableHD hd = new LogicTableHD();
        //These functions are the equivalent of the fillFromComponent
        //in the VDP case
        hd.setRecordType(LtRecordType.HD);
        hd.setFunctionCode("HD");

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("hhmmss");
        Date dt = Calendar.getInstance().getTime();
        hd.setDate(dateFormat.format(dt));
        hd.setTime(timeFormat.format(dt));
        ltEmitter.addToLogicTable(hd);
    }

    private void emitGeneration() {
        LogicTableGeneration gen = new LogicTableGeneration();
        gen.setRecordType(LtRecordType.GENERATION);
        gen.setFunctionCode("GEN");
        gen.setExtract(true);
        gen.setIsAscii(System.getProperty("os.name").startsWith("z") ? false : true);
        gen.setDesc("Java MR91");
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("HHmmss");
        Date dt = Repository.getGenerationTime();
        String date = dateFormat.format(dt);
        gen.setDateCc(date.substring(0,2));
        gen.setDateYy(date.substring(2,4));
        gen.setDateMm(date.substring(4,6));
        gen.setDateDd(date.substring(6,8));
        String time = timeFormat.format(dt);
        gen.setTimeHh(time.substring(0,2));
        gen.setTimeMm(time.substring(2,4));
        gen.setTimeSs(time.substring(4,6));
        gen.setTimeTt("00");
        gen.setPadding("padding");
        ltEmitter.addToLogicTable(gen);
    }

    private void emitEN() {
        LogicTableF0 en = new LogicTableF0();
        en.setRecordType(LtRecordType.F0);
        en.setFunctionCode("EN");
        en.setViewId(0);
        ltEmitter.addToLogicTable(en);
    }

    public ASTBase getSelectIfNode() {
        return getFirstNodeOfType(ASTFactory.Type.SELECTIF);
    }

    private void addDeclareRecordCount() {    
        //This is overriding the logic in the AST nodes?    
        LogicTableName recs = new LogicTableName();
        recs.setAccumulatorName("lRecordCount");
        recs.setRecordType(LtRecordType.NAME);
        recs.setFunctionCode("DIM4");
        recs.setSourceSeqNbr((short)1);
        ltEmitter.setSuffixSeqNbr((short)1);
        ltEmitter.addToLogicTable(recs);
    }

    public void zeroRecordCount() {
        LogicTableNameValue setc = new LogicTableNameValue();
        setc.setTableName("lRecordCount");
        setc.setValue(new Cookie("0"));
        setc.setRecordType(LtRecordType.NAMEVALUE);
        setc.setFunctionCode("SETC");
        ltEmitter.setSuffixSeqNbr((short)1);
        setc.setCompareType(LtCompareType.EQ);
        ltEmitter.addToLogicTable(setc);
    }


}
