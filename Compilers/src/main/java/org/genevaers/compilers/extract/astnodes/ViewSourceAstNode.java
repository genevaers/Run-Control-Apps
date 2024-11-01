package org.genevaers.compilers.extract.astnodes;

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


import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.genevaio.ltfile.LogicTableNV;
import org.genevaers.genevaio.ltfile.LogicTableWR;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.LtRecordType;
import org.genevaers.repository.data.ViewAreaValues;

import com.google.common.flogger.FluentLogger;

public class ViewSourceAstNode extends ExtractBaseAST implements EmittableASTNode {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private ViewSource vs;
    private ViewAreaValues areaValues = new ViewAreaValues();
    private static short lfsourceNumber = 1; //C++ counts LF sources in the NV

    //Instead of this introduce a ScopedNode interface to hold the end point
    private Integer nextViewPosition = 0;

    public ViewSourceAstNode() {
        type = ASTFactory.Type.VIEWSOURCE;
    }

    public ViewSource getViewSource() {
        return vs;
    }

    public void setViewSource(ViewSource vs) {
        currentViewSource = vs;
        this.vs = vs;
    }

    @Override
    public void emit() {
        currentViewSource = vs;
        currentViewColumn = null;
        logger.atFine().log("Emit from view source %d:%s ", vs.getViewId(), vs.getSequenceNumber());
        ltEmitter.setViewId(vs.getViewId());
        LogicTableNV nv = emitNV();
        emitChildNodes();
        addAreaLengths(nv);
        
        //Do we need to generate a WR
        //At the moment yes... it is the only way.
        //TODO need some smarts to manage this correctly
        //emitDefaultWrite();
        //We now know where the end of the view is...
        setNextViewPosition(ltEmitter.getNumberOfRecords());
    }

    private void emitDefaultWrite() {
        LogicTableWR wr = new LogicTableWR();
        wr.setRecordType(LtRecordType.WR);
        wr.setFunctionCode("WRDT");
        wr.setSuffixSeqNbr((short)0);
        wr.setViewId(vs.getViewId());

        wr.setFileId(vs.getSourceLFID());
        //Where are the destination types defined.... 
        wr.setDestType(1); //0 extract 1 = File 2 = token
        wr.setOutputFileId(vs.getOutputLFID());
        wr.setWriteExitParms("");
        wr.setWriteExitId(0);
        ltEmitter.addToLogicTable(wr);
    }

    private LogicTableNV emitNV() {
        LogicTableNV nv = new LogicTableNV();
        nv.setRecordType(LtRecordType.NV);
        nv.setFunctionCode("NV");
        nv.setViewID(vs.getViewId());
        //we should make the vs have a back ref to its view?
        //Nodes will need the repo...
        //Keep in the base as a static?
        ViewNode vw = Repository.getViews().get(vs.getViewId());
        nv.setViewType(vw.viewDef.getViewType());
        nv.setSourceLrId(vs.getSourceLRID());
        nv.setFileId(vs.getSourceLFID());
        nv.setSourceSeqNbr(vs.getSequenceNumber());
        ltEmitter.setSuffixSeqNbr((short)0);
        ltEmitter.addToLogicTable(nv);
        ltEmitter.setFileId(vs.getSourceLFID());
        return nv;
    }


    private void addAreaLengths(LogicTableNV nv) {
        nv.setDtAreaLen(areaValues.getDtLen());
        //CTC is meant to be a column count
        //But CPP version has it set to 0
        nv.setCtColCount((short)0);
        nv.setSortKeyLen(areaValues.getSkLen());
        nv.setSortTitleLen(areaValues.getSkTLen());
    }

    public void setNextViewPosition(Integer nextViewPosition) {
        this.nextViewPosition = nextViewPosition;
        //Need to pass this down to the Selectif
        ASTBase sfn = getFirstNodeOfType(ASTFactory.Type.SELECTIF);
        if(sfn != null) {
            ((SelectIfAST)sfn).setNextViewPosition(nextViewPosition);;
        }
        ASTBase skipn = getFirstNodeOfType(ASTFactory.Type.SKIPIF);
        if(skipn != null) {
            ((SkipIfAST)skipn).setNextViewPosition(nextViewPosition);;
        }
    }

    public Integer getNextViewPosition() {
        return nextViewPosition;
    }

    public boolean hasExtractFilterText() {
        return vs.getExtractFilter() != null && vs.getExtractFilter().length() > 0;
    }

    public ViewAreaValues getAreaValues() {
        return areaValues;
    }

    public int getNumberOfColumns() {
        return vs.getValuesOfColumnSourcesByNumber().size();
    }

    public static void resetLfSourceNumber() {
        lfsourceNumber = 1;
    }

}
