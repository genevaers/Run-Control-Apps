package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.genevaio.ltfile.LogicTableWR;
import org.genevaers.repository.Repository;

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


import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.LtRecordType;

public class WriteASTNode extends ExtractBaseAST implements EmittableASTNode {

    private PhysicalFile pf;
    private ViewSource vs;
    private int fileNumber = 0;

    public WriteASTNode() {
        type = ASTFactory.Type.WRITE;
    }

    public PhysicalFile getPhysicalFile() {
        return pf;
    }

    public void setPhysicalFile(PhysicalFile pf) {
        this.pf = pf;
    }

    @Override
    public void emit() {
        boolean extractWrite = true;
        short savedSeqNum = ltEmitter.getSuffixSeqNbr();
        //The correct function code to generate comes from the Source
        //Yeah, the source...
        //So we should find the child Source node and figure out what to generate
        //Or simply ask it for the correct function code.
        WriteSourceNode srcNode = (WriteSourceNode) getFirstNodeOfType(Type.WRITESOURCE);
        //Suffix Sequence number is the extract file number - if there is one
        WriteExtractNode en = (WriteExtractNode) getFirstNodeOfType(Type.WRITEEXTRACT);
        if(en != null) {
            ltEmitter.setSuffixSeqNbr((short)en.getFileNumber());
            //We need to accumulate the extract ids
            //then we can generate the 801 record
            if(vs.getViewId() < 9000000) {
                Repository.addExtractFileNumber((short)en.getFileNumber());
            } else {
                extractWrite = false;
            }
        } else {
            //We need to know the view type
            //if there is a format phase then we use the extract number in the view defintion
            //If not we just leave it at 0
            //Looks different for JLTs...not sure that should be the case
            // if(vs != null) {
            //     ltEmitter.setSuffixSeqNbr(vs.getSequenceNumber());
            // } else {
            //     ltEmitter.setSuffixSeqNbr((short)fileNumber);
            // }
            ltEmitter.setSuffixSeqNbr((short)0);
        }

        LogicTableWR wr = new LogicTableWR();
        wr.setRecordType(LtRecordType.WR);
        ViewNode view = Repository.getViews().get(vs.getViewId());
        if(srcNode != null) {
            if(view.isFormat() && view.isExtractSummarized()) {
                wr.setFunctionCode("WRSU");
                wr.setExtrSumRecCnt(view.getViewDefinition().getMaxExtractSummaryRecords());
            } else {
                wr.setFunctionCode(srcNode.getFunctionCode());
            }
        } else {
            //What are the rules for default? Always create a Source node and have it manage it?
            if(view.isFormat()) {
                wr.setFunctionCode("WRXT");
            } else {         
                wr.setFunctionCode("WRDT");
            }
        }
        wr.setViewId(vs.getViewId());

        wr.setFileId(vs.getSourceLFID());

        //Where are the destination types defined.... 
        WriteSourceNode wrSource = ((WriteSourceNode)getFirstNodeOfType(Type.WRITESOURCE));
        if(wrSource != null && wrSource.getFunctionCode().equals("WRXT") ) {
            wr.setDestType(0); //0 extract 1 = File 2 = token
        } else {
            if(extractWrite && en != null) {
                 wr.setDestType(0); //0 extract 1 = File 2 = token
            } else {
                wr.setDestType(1); //0 extract 1 = File 2 = token
            }
        }
        getPFFromChildNode();
        if(pf != null) {
            setDestTypeFromPF(wr);
            ltEmitter.setSuffixSeqNbr((short)0); //This is what we need in JLT will it work for XLT?
        } else {
            wr.setOutputFileId(0);
        }
        WriteExitNode wen = (WriteExitNode) getFirstNodeOfType(Type.WRITEEXIT);
        if(wen != null) {
            wr.setWriteExitParms(wen.getParams());
            wr.setWriteExitId(wen.getExitID());
        } else {
            wr.setWriteExitParms("");
            wr.setWriteExitId(0);
        }
        wr.setSourceSeqNbr((short) ltEmitter.getLogicTable().getNumberOfRecords());
        ltEmitter.addToLogicTable(wr);
        ltEmitter.setSuffixSeqNbr(savedSeqNum);
    }

    private void getPFFromChildNode() {
        if(pf == null) {
            PFAstNode pfn = (PFAstNode) getFirstNodeOfType(Type.PF);
            pf = pfn != null ? pfn.getPhysicalFile() : null;
        }
    }

    private void setDestTypeFromPF(LogicTableWR wr) {
        wr.setOutputFileId(pf.getComponentId());
        switch (pf.getFileType()) {
            case EXTRACT:
                wr.setDestType(0);
                break;
            case TOKEN:
                wr.setDestType(2);
                // Silly fixup
                wr.setFunctionCode("WRTK");
                break;
            default:
                wr.setDestType(1);
                break;
        }
    }

    public void setViewSource(ViewSource viewSource) {
        vs = viewSource;
    }

    public void setFileNumber(int num) {
        fileNumber = num;
    }

}
