package org.genevaers.repository.jltviews;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.genevaers.repository.RepoHelper;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.FieldPositionComparator;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupType;
import org.genevaers.repository.components.OutputFile;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.OutputMedia;
import org.genevaers.repository.components.enums.ViewStatus;
import org.genevaers.repository.components.enums.ViewType;

import com.google.common.flogger.FluentLogger;

/**
 * Java MR91 deals only with plain lookups
 * This class tracks the need for a view to generate
 * the reference data needed by a lookup.
 * 
 * As the compiler finds lookup fields a JLTView is created
 * and the referenced fields are accumulated.
 * 
 * The JLT View is then used a JLT generation time to make the view
 * representing the extraction of the reference fields into the RED file.
 * 
 * We generate two sets of fields to manage the reference process
 *     The set of fields required to build the lookup record.
 *     The set of fields that refer to the lookup record.
 * 
 * We also need to maintain a mapping between the original field and its reference version.
 * So at XLT emit time we can generate the correct offsets into the RED file for the "L" function codes.
 * 
 * Couple of gotyas...
 * The record fields in the generation of the lookup view includes the key fields.
 * The RED file refers to only the fields after the sort key and position 1 is immediately after the key.
 * 
 */
public class JLTView {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     *
     */
    private static final String PADDING = "Padding";
    public static final int EFF_DATE_NONE = 0;
    public static final int EFF_DATE_START = 1;
    public static final int EFF_DATE_BOTH = 2;
    public static final int EFF_DATE_END = 3;

    public class FieldMapping {
        public LRField src;
        public LRField trg;

        FieldMapping(LRField s, LRField t){
            src = s;
            trg = t;
        }
    }

    public class Reason {
    
        public String key;
        public int lookupId;
        public int fieldId;
        public int viewID;
        public int columnNumber;

        public Reason(String key, int lookupId, int fieldId) {
            this.key = key;
            this.lookupId = lookupId;
            this.fieldId = fieldId;
        }

        
    }


    private int lrid;
    private int originalLookupId;

    //Using a set here so it can manage the uniquness
    private Set<LRField> referencedFields = new HashSet<>();
    public static final int JOINVIEWBASE = 9000000;

    private List<LRField> keyFields = new ArrayList<>();
    private LogicalRecord genLR;
    private int genLRlength = 0;
    private LogicalRecord redLR;

    private List<LRField> sortableRefFields = new ArrayList<>();

    //The compiler needs to get the new field - this map will hold it
    private Map<Integer, LRField> refFieldToRedField = new TreeMap<>();
    //
    private Map<Integer, LRField> genFieldToRefField = new HashMap<>();

    private short genStartPos = 1;
    private short keyLen;
    private int effDateCode = 0;
    private boolean indexIsText = true;
    private short redStartpos = 1;
    private int refViewNum;
    private ViewNode vn;
    private int colNum;
    private String uniqueKey; //This will grow once we figure it out

    protected LookupType lookupType;
    private int ddNum;

    private List<Reason> reasons = new ArrayList<>();
    private Integer sourceLF;

    public JLTView(int lr, int lkup) {
        lrid = lr;
        originalLookupId = lkup;
    }

    public void addRefField(LRField f) {
        if(f != null)
            referencedFields.add(f);
    }

    public String getName() {
        return "JLT for LR " + lrid;
    }

    public Set<LRField> getRefFields() {
        return referencedFields;
    }

    public ViewNode buildRefViewForLF(int lfid) {
        ViewDefinition vd = new ViewDefinition();
        refViewNum = JOINVIEWBASE + ddNum;
        vd.setComponentId(refViewNum);
        String ddname = String.format("REFR%03d", ddNum);
        String pfDdname = Repository.getLogicalFiles().get(lfid).getPFIterator().next().getInputDDName();
        String name = String.format("Ref-phase work file %03d from %s",ddNum,  pfDdname);
        vd.setName(name);
        vd.setOutputMedia(OutputMedia.FILE);
        vd.setViewType(ViewType.EXTRACT);
        vd.setExtractSummarized(false);
        vd.setStatus(ViewStatus.ACTIVE);
        vd.setWriteExitParams("");
        vd.setFormatExitParams("");
        vd.setOwnerUser("SAFR");
        
        vn = Repository.getViewNodeMakeIfDoesNotExist(vd);

        ViewSource vs = addViewSource(lfid);
        //Set the output file. Which should be part of the VDP.
        //Its name if from the view... and ddname from above?
        addColumns(vs);
        addPaddingIfNeeded(genStartPos, vs);
        setupViewOutputFile(vn, ddname);
        return vn;
    }

    private void setupViewOutputFile(ViewNode vn, String ddname) {
        OutputFile vnopf = vn.getOutputFile();
        vnopf.setComponentId(vn.getID());
        vnopf.setName("PF Generated for " + ddname);
        vnopf.setOutputDDName(ddname);
        vnopf.setLogicalFilename("LF Generated for " + ddname);
        vnopf.setFileType(FileType.DISK);
    }

    private void addPaddingIfNeeded(short genStartPos, ViewSource vs) {
        //genStartPos is the start of the next field
        //if it is on and multiple of 8 + 1 no padding needed
        int padSize = (8 - (genStartPos-1) % 8)%8;
        addPadToViewColumnSources(genStartPos, padSize, vs);
    }

    private void addPadToViewColumnSources(short genStartPos, int padSize, ViewSource vs) {
        ViewColumn vc = vn.makeJLTColumn(PADDING, colNum);
        RepoHelper.setViewAlnumColumn(vc, genStartPos, (short)padSize, PADDING);
        ViewColumnSource vcs = new ViewColumnSource();
        vcs.setColumnID(vc.getComponentId());
        vcs.setColumnNumber(colNum);
        vcs.setComponentId(colNum);
        vcs.setSequenceNumber((short)1);
        vcs.setSourceType(ColumnSourceType.CONSTANT);
        vcs.setViewId(vn.getID());
        vcs.setViewSourceId(vs.getComponentId());
        String val = Integer.toString(padSize);
        vcs.setSrcValue(" ");
        vcs.setValueLength(val.length());
        vcs.setLogicText("Padding " + padSize);
        vs.addToColumnSourcesByNumber(vcs);
        colNum++;
    }

    private ViewSource addViewSource(int lfid) {
        ViewSource vs = new ViewSource();
        //What else do we care about for the ViewSource.
        //Probably needs a new id -> ask the repo to make it
        //It will know that the ids are - or use the view number. 
        //It will be unique and the will only be one source
        vs.setComponentId(1);
        vs.setSequenceNumber((short)1);
        vs.setSourceLFID(lfid);
        vs.setSourceLRID(lrid);
        vs.setViewId(vn.getID());
        vn.addViewSource(vs);
        return vs;
    }

    private void addColumns(ViewSource vs) {
        // Add the data from the generation fields
        // We make a view that models its lr.
        Iterator<LRField> fi = genLR.getIteratorForFieldsByID();
        colNum = 1;
        while(fi.hasNext()) {
            LRField f = fi.next();
                if(f.getName().startsWith("Quadword")) {
                } else {
                    ViewColumn vc = vn.makeJLTColumn(f.getName(), colNum);
                    RepoHelper.setViewColumnFromLRField(vc, f);
                    ViewColumnSource vcs = new ViewColumnSource();
                    vcs.setColumnID(vc.getComponentId());
                    vcs.setColumnNumber(colNum);
                    vcs.setComponentId(colNum);
                    vcs.setSequenceNumber((short)1);
                    vcs.setSourceType(ColumnSourceType.EVENTLR);
                    vcs.setViewId(vn.getID());
                    vcs.setViewSourceId(vs.getComponentId());
                    vcs.setViewSrcLrFieldId(genFieldToRefField.get(f.getComponentId()) != null ? genFieldToRefField.get(f.getComponentId()).getComponentId() : 0);
                    vcs.setViewSrcLrId(genLR.getComponentId());
                    vcs.setLogicText("Ref from " + f.getName());
                        vs.addToColumnSourcesByNumber(vcs);
                    colNum++;
                }
        }       
    }

    public void buildREDLRs(int joinNumber) {
        // make the RED LR - the LR 
        // make the generation RED LR - the LR used to generate the ref data
        // they can be different due to redefines
        // And the RED LR is just the data following the keys

        //Need to sort the fields in start position order
        //The ignore the overlapped/redefined 
        sortRefFields();
        makeREDLR(joinNumber);
        makeREDGenerationLR();
        //This has to be after above since it works out the key length

        //Maybe at the same time as genration
    }

    private void makeREDGenerationLR() {
        genLR = Repository.makeLR("Ref Generation LR for Lookup lr " + lrid);
        addFieldsFromLookupKeys();
        if(sortableRefFields.size() > 0) { //Intermediate step may not have a ref field
            addReferencedFieldsToGenAndREDLRs();
        }
    }

    private void addReferencedFieldsToGenAndREDLRs() {
        Iterator<LRField> ri = sortableRefFields.iterator();
        LRField refFld = ri.next();
        LRField genFld = makeGenFld(refFld);
        genFld.setStartPosition(genStartPos);
        genStartPos += genFld.getLength();
        LRField redFld = makeREDFld(refFld);
        redFld.setStartPosition(redStartpos);
        redStartpos += redFld.getLength();
        LRField lastTopLevelRefFld = refFld;
        genFieldToRefField.put(genFld.getComponentId(), refFld);
        refFieldToRedField.put(refFld.getComponentId(), redFld);

        while(ri.hasNext()) {
            refFld = ri.next();
            if(refFld.getStartPosition() < lastTopLevelRefFld.getStartPosition() + lastTopLevelRefFld.getLength()) { 
                //this is a redefined field
                //Need to check if it overflows past the last ref field
                if(refFld.getStartPosition() + refFld.getLength() > lastTopLevelRefFld.getStartPosition() + lastTopLevelRefFld.getLength()) {
                    //This is the problem child
                    //We should extend the length of the gen field to cater for this?
                    //Must make sure we don't go over 256
                    int overlapped = 1;
                }
                redFld = makeREDFld(refFld);
                //Juggle redefined field's start position
                // we only have the next redStartPos to use as a reference
                // this field's start pos is relative to redStartPos
                short refOffset = (short)(refFld.getStartPosition() - lastTopLevelRefFld.getStartPosition());
                short redSP = (short)(redStartpos - lastTopLevelRefFld.getLength() + refOffset);
                redFld.setStartPosition(redSP);
            } else {
                lastTopLevelRefFld = refFld; //Not a redefine so set
                genFld = makeGenFld(refFld);
                genFld.setStartPosition(genStartPos);
                genStartPos += genFld.getLength();
                redFld = makeREDFld(refFld);
                redFld.setStartPosition(redStartpos);
                redStartpos += redFld.getLength();
                genFieldToRefField.put(genFld.getComponentId(), refFld);
            }
            refFieldToRedField.put(refFld.getComponentId(), redFld);
        }
        quadWordAlignAfter(genFld);
        
    }

    private void quadWordAlignAfter(LRField genFld) {
        int alignStartPosition = genFld.getStartPosition() + genFld.getLength();
        int genLen = alignStartPosition - 1;
        int alignmentLength = (8 - (genLen % 8))%8;
        logger.atInfo().log("Generarion LR length %d", genLen);
        logger.atInfo().log("Generarion LR needs alignment of %d", alignmentLength);
        LRField alignField = Repository.makeNewField(genLR);
        alignField.setName("QuadwordAlign");
        alignField.setDatatype(DataType.ALPHANUMERIC);
        alignField.setLength((short)alignmentLength);
        alignField.setDateTimeFormat(DateCode.NONE);
        alignField.setJustification(JustifyId.LEFT);
        alignField.setStartPosition((short)alignStartPosition);
        LRField genalignField = makeGenFld(alignField);
        genFieldToRefField.put(genalignField.getComponentId(), alignField);
        genLR.addToFieldsByID(alignField);
        genLR.addToFieldsByName(alignField);
    }

    private LRField makeGenFld(LRField refFld) {
        LRField genFld = Repository.makeNewField(genLR);
        genFld.setName(refFld.getName());
        RepoHelper.copyFieldFormat(refFld, genFld);
        genLR.addToFieldsByID(genFld);
        genLR.addToFieldsByName(genFld);
        return genFld;
    }

    private LRField makeREDFld(LRField refFld) {
        LRField redFld = Repository.makeNewField(redLR);
        redFld.setName(refFld.getName());
        RepoHelper.copyFieldFormat(refFld, redFld);
        redLR.addToFieldsByID(redFld);
        redLR.addToFieldsByName(redFld);
        return redFld;
    }

    private void sortRefFields() {
        Iterator<LRField> rfi = referencedFields.iterator();
        while(rfi.hasNext()) {
            sortableRefFields.add(rfi.next());
        }
        FieldPositionComparator fpc = new FieldPositionComparator();
        Collections.sort(sortableRefFields, fpc);
    }

    private void addFieldsFromLookupKeys() {
        // get the target LR
        // get its index fields 
        // add then to the generation LR
        genStartPos = 1;
        LogicalRecord targetlr = Repository.getLogicalRecords().get(lrid);
        Iterator<LRIndex> ii = targetlr.getIteratorForIndexBySeq();
        while(ii.hasNext()) {
            LRIndex ndx = ii.next();
            LRField keyFld = addIndexField(ndx);
            keyFld.setStartPosition(genStartPos);
            genStartPos += keyFld.getLength();
            if(ndx.isEffectiveDateStart() || ndx.isEffectiveDateEnd()) {
                //ignore for key length
            } else {
                 keyLen += keyFld.getLength();
            }
        }
    }

    private LRField addIndexField(LRIndex ndx) {
        LRField genFld = Repository.makeNewField(genLR);
        LRField ndxFld = Repository.getFields().get(ndx.getFieldID());
        if(ndx.isEffectiveDateStart() || ndx.isEffectiveDateEnd()) {
            handleEffectiveDates(ndx, genFld);
        } else {
            ndxFld = Repository.getFields().get(ndx.getFieldID());
            RepoHelper.copyFieldFormat(ndxFld, genFld);
            if(ndxFld.getDatatype() != DataType.ALPHANUMERIC) {
                indexIsText = false;
            }
            genFld.setName(ndxFld.getName());
        }
        genLR.addToFieldsByID(genFld);
        genLR.addToFieldsByName(genFld);
        genFieldToRefField.put(genFld.getComponentId(), ndxFld);
        keyFields.add(genFld);
        return genFld;
    }

    private void handleEffectiveDates(LRIndex ndx, LRField genFld) {
        // This is not bit mapped and is counter-intuitive
        //So treat like a little state machine
        switch(effDateCode) {
            case EFF_DATE_NONE:
            if (ndx.isEffectiveDateStart()) {
                setStartEffDate(genFld);
                effDateCode = EFF_DATE_START;
            } else if (ndx.isEffectiveDateEnd()) {
                setEndEffDate(genFld);
                effDateCode = EFF_DATE_END;
            }
            break;
            case EFF_DATE_START:
            if (ndx.isEffectiveDateStart()) {
                //This is odd and should be flagged as an error?
            } else if (ndx.isEffectiveDateEnd()) {
                setEndEffDate(genFld);
                effDateCode = EFF_DATE_BOTH;
            }
            break;
            case EFF_DATE_END:
            if (ndx.isEffectiveDateStart()) {
                setStartEffDate(genFld);
                effDateCode = EFF_DATE_BOTH;
            } else if (ndx.isEffectiveDateEnd()) {
                //This is odd and should be flagged as an error?
            }
            break;
            case EFF_DATE_BOTH:
                //This is odd and should be flagged as an error?
                break;
        }
    }

    private void setEndEffDate(LRField genFld) {
        RepoHelper.setEffDateBinaryField(genFld);
        genFld.setName("end date");
    }

    private void setStartEffDate(LRField genFld) {
        RepoHelper.setEffDateBinaryField(genFld);
        genFld.setName("start date");
    }

    private void makeREDLR(int joinNumber) {
        //need to use LR id based BASE number
        redLR = Repository.makeLR("Ref LR for Lookup lr " + lrid, JOINVIEWBASE + joinNumber);
    }

    public int getLRid() {
        return lrid;
    }

    public LogicalRecord getGenLR() {
        return genLR;
    }

    public short getGenLrLength() {
        return (short)(genStartPos-1); //When done this will be the length
    }  

    public short getKeyLength() {
        return keyLen;
    }  

    public int getEffDateCode() {
        return effDateCode;
    }

    public boolean isIndexText() {
        return indexIsText;
    }

    public LRField getRedFieldFromLookupField(int id) {
        //If this is an external lookup we need to apply a field position fixup
        //Subtract the key length
        if(lookupType == LookupType.NORMAL) {
            return  refFieldToRedField.get(id);
        } else {
            LRField lrf = Repository.getLogicalRecords().get(lrid).findFromFieldsByID(id);
            return RepoHelper.cloneFieldWithNewStartPos(lrf, keyLen);
        }
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public LookupType getLookupType() {
        return lookupType;
    }

    public void setLookupType(LookupType lookupType) {
        this.lookupType = lookupType;
    }

    public int getOrginalLookupId() {
        return originalLookupId;
    }

    public  Iterator<Entry<Integer, LRField>> getRefToRedIterator() {
        return refFieldToRedField.entrySet().iterator();
    }

    public void setDDNumber(int n) {
        ddNum = n;
    }

    public int getDdNum() {
        return ddNum;
    }

    public LogicalRecord getRedLR() {
        return redLR;
    }

    public int getRefViewNum() {
        return refViewNum;
    }

    public void addReason(String key, int lkid, int fldid) {
        reasons.add(new Reason(key, lkid, fldid));
    }

    public List<Reason> getReasons() {
        return reasons;
    }

    public void updateLastReason(int viewId, int columnNumber) {
        Reason last = reasons.get(reasons.size() - 1);
        last.viewID = viewId;
        last.columnNumber = columnNumber;
    }

    public ViewNode getView() {
        return vn;
    }

    public int getSourceLF() {
        return sourceLF;
    }

    public void setSourceLFID(Integer lfOfKey) {
        sourceLF = lfOfKey;
    }

    public int calculateExternalKeyLength() {
        genStartPos = 1;
        LogicalRecord targetlr = Repository.getLogicalRecords().get(lrid);
        Iterator<LRIndex> ii = targetlr.getIteratorForIndexBySeq();
        while(ii.hasNext()) {
            LRIndex ndx = ii.next();
            LRField keyFld = Repository.getFields().get(ndx.getFieldID());
            if(ndx.isEffectiveDateStart() || ndx.isEffectiveDateEnd()) {
                keyLen += 4;
            } else {
                keyLen += keyFld.getLength();
            }
        }

        return keyLen;
    }
}
