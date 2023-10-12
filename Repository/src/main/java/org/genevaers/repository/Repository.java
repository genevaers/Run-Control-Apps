package org.genevaers.repository;

import java.util.Date;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

import org.genevaers.repository.components.ControlRecord;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.enums.LrStatus;
import org.genevaers.repository.data.ComponentCollection;
import org.genevaers.repository.jltviews.JLTView;
import org.genevaers.repository.jltviews.JoinViewsManager;
import org.genevaers.repository.jltviews.UniqueKey;

import com.google.common.flogger.FluentLogger;

public class Repository {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static Set<Short> extractFileNubers = new TreeSet<>();

	//Helper fields
	private static LookupPath currentlp;
	private static LookupPathStep currentLookupPathStep;
	private static int currentLookupPathID;
	private static int currentStepNumber;
	private static LookupPathKey currentLookupKey;
	private static int currentLRID = 0;
	private static LogicalRecord currentLR;
	private static int maxViewID = 0;
	private static int maxComponentLRID = 0;
	private static int maxFieldID = 0;
	private static int maxIndexID = 0;

	//The main components
	private static ComponentCollection<ControlRecord> crs = new ComponentCollection<ControlRecord>();
	private static ComponentCollection<LogicalFile> lfs = new ComponentCollection<LogicalFile>();
	private static ComponentCollection<PhysicalFile> pfs = new ComponentCollection<PhysicalFile>();
	private static ComponentCollection<UserExit> exits = new ComponentCollection<UserExit>();
	private static ComponentCollection<LogicalRecord> lrs = new ComponentCollection<LogicalRecord>();
	private static ComponentCollection<LRField> fields = new ComponentCollection<LRField>();
	private static ComponentCollection<LRIndex> indexes = new ComponentCollection<LRIndex>();
	private static ComponentCollection<LookupPath> lookups = new ComponentCollection<LookupPath>();
	private static ComponentCollection<ViewNode> views = new ComponentCollection<ViewNode>();
	private static ComponentCollection<ViewNode> formatViews = new ComponentCollection<ViewNode>();
	private static JoinViewsManager jvm = new JoinViewsManager();

	private static Date generationTime;

	public static void clearAndInitialise() {
		crs = new ComponentCollection<ControlRecord>();
		lfs = new ComponentCollection<LogicalFile>();
		pfs = new ComponentCollection<PhysicalFile>();
		exits = new ComponentCollection<UserExit>();
		lrs = new ComponentCollection<LogicalRecord>();
		fields = new ComponentCollection<LRField>();
		indexes = new ComponentCollection<LRIndex>();
		lookups = new ComponentCollection<LookupPath>();
		views = new ComponentCollection<ViewNode>();
		formatViews = new ComponentCollection<ViewNode>();
		jvm = new JoinViewsManager();	

		UniqueKey.reset();

		currentlp = null;
		currentLookupPathStep = null;
		currentLookupPathID = 0;
		currentStepNumber = 0;
		currentLookupKey = null;
		currentLRID = 0;
		currentLR = null;
		maxViewID = 0;
		maxComponentLRID = 0;
		maxFieldID = 0;
		maxIndexID = 0;
	
		extractFileNubers = new TreeSet<>();
	}

	public static ComponentCollection<ControlRecord> getControlRecords() {
		return crs;
	}

	public static JoinViewsManager getJoinViews() {
		return jvm; 
	}

	public static ComponentCollection<LogicalFile> getLogicalFiles() {
		return lfs;
	}

	public static ComponentCollection<PhysicalFile> getPhysicalFiles() {
		return pfs;
	}

	public static ComponentCollection<UserExit> getUserExits() {
		return exits;
	}

	public static ComponentCollection<LogicalRecord> getLogicalRecords() {
		return lrs;
	}

	public static ComponentCollection<LRField> getFields() {
		return fields;
	}

	public static ComponentCollection<LRIndex> getIndexes() {
		return indexes;
	}

	public static ComponentCollection<LookupPath> getLookups() {
		return lookups;
	}

	public static ComponentCollection<ViewNode> getViews() {
		return views;
	}

	public static ComponentCollection<ViewNode> getFormatViews() {
		return formatViews;
	}

    public static OptionalInt getMaxFileID() {
		return lfs.getValues().stream().mapToInt(lf -> lf.getID()).max();
    }

	public static void addPhysicalFile(PhysicalFile pf) {
		// The Record ID is the LF number
		// That means the inputFileID is not unique there can be many records with the
		// same PF id.
		// do we already know about this pf?
		PhysicalFile ourPF = pfs.get(pf.getComponentId());
		LogicalFile lf = lfs.get(pf.getLogicalFileId());
		if (ourPF == null) {
			pfs.add(pf, pf.getComponentId(), pf.getName());
		}
		if (lf == null) {
			lf = new LogicalFile();
			lf.setName(pf.getLogicalFilename());
			lf.setID(pf.getLogicalFileId());
			lfs.add(lf, lf.getID(), lf.getName());
		}
		if (ourPF != null) {
			lf.addPF(ourPF);
		} else {
			lf.addPF(pf);
		}
	}

	public static void addLogicalRecord(LogicalRecord lr) {
		if(maxComponentLRID < lr.getComponentId())
			maxComponentLRID = lr.getComponentId();
		lrs.add(lr, lr.getComponentId(), lr.getName());
	}

	public static void addLRField(LRField lrf) {
		if(maxFieldID < lrf.getComponentId())
			maxFieldID = lrf.getComponentId();
		fields.add(lrf, lrf.getComponentId(), lrf.getName());
		if (lrf.getLrID() != currentLRID) {
			currentLRID = lrf.getLrID();
			currentLR = lrs.get(currentLRID);
		}
		if (currentLR != null) {
			currentLR.addToFieldsByID(lrf);
			if(lrf.getName() != null) { //Can be from tests
				currentLR.addToFieldsByName(lrf);
			}
		} else {
			logger.atInfo().log("Ignoring Null LR for field %s" + lrf.getName());
		}
	}

	// Should this even be here really the index fields should just be part of an
	// LR?
	public static void addLRIndex(LRIndex lri) {
		if(maxIndexID < lri.getComponentId())
			maxIndexID = lri.getComponentId();
		indexes.add(lri, lri.getComponentId());
		lrs.get(lri.getLrId()).setPrimaryKey(lri.getComponentId());
	}

	public static void addLookupPathKey(LookupPathKey lpk) {
		if (currentLookupPathID != lpk.getComponentId()) {
			LookupPath lkup = makeANewLookupPath(lpk);
			lookups.add(lkup, lkup.getID(), lkup.getName());
		} else if (currentStepNumber != lpk.getStepNumber()) {
			makeANewStep(lpk);
		} else {
			makeAKeyStep(lpk);
		}
	}

	public static ViewNode getViewNodeMakeIfDoesNotExist(ViewDefinition vd) {
		ViewNode vn = views.get(vd.getComponentId());
		if(vn == null) {
			vn = new ViewNode();
			vn.setDefinition(vd);
			if(maxViewID < vd.getComponentId())
				maxViewID = vd.getComponentId();
			views.add(vn, vd.getComponentId(), vd.getName());
		}
		return vn;
	}

//	public void fixupPFExits() {
//		for(PhysicalFile pf : pfs.values()) {
//			if(pf.hasReadExit()) {
//				pf.addReadExit(userExitRoutines.get(pf.getReadExitID()));
//			}
//		}
//	}
	
	private static LookupPath makeANewLookupPath(LookupPathKey lpk) {
		currentLookupPathID = lpk.getComponentId();
		currentStepNumber = lpk.getStepNumber();
		currentlp = new LookupPath();
		currentlp.setID(currentLookupPathID);
		makeANewStep(lpk);
		return currentlp;
	}

	private static void makeANewStep(LookupPathKey lpk) {
		currentLookupPathStep = new LookupPathStep();
		currentLookupPathStep.setStepNum(lpk.getStepNumber());
		currentlp.addStep(currentLookupPathStep);
		makeAKeyStep(lpk);
	}

	private static void makeAKeyStep(LookupPathKey lpk) {
		currentLookupKey = lpk;
		currentLookupPathStep.addKey(currentLookupKey);
	}

	// public void addLookupPath(LookupPath lookup) {
	// 	lookupPaths.put(lookup.getID(), lookup);
	// 	lookupPathsByName.put(lookup.getName(), lookup);
	// }

	public static OptionalInt getMaxPFID() {
		return pfs.getValues().stream().mapToInt(pf -> pf.getComponentId()).max();
    }


	/**
	 * How should we get the next id?
	 * Keep track of the value as they are added
	 * Or go to the source an query?
	 */
    public static int getMaxViewID() {
        return maxViewID;
    }

	public static LogicalRecord makeLR(String name) {
        int nextLRid = maxComponentLRID + 1;
        LogicalRecord lr = new LogicalRecord();
        lr.setComponentId(nextLRid);
        lr.setName(name);
        lr.setStatus(LrStatus.ACTIVE);
        lr.setLookupExitParams("");
        addLogicalRecord(lr);    
		return lr;
	}

    public static int getNextLRid() {
		maxComponentLRID++;
		return maxComponentLRID;
    }

    public static LRField makeNewField(LogicalRecord lr) {
		int id = maxFieldID + 1;
		LRField fld = new LRField();
		fld.setComponentId(id);
		fld.setLrID(lr.getComponentId());
		addLRField(fld);
		return fld;
    }

    public static LRIndex makeNewIndex(LogicalRecord lr) {
		int id = maxIndexID + 1;
		LRIndex ndx = new LRIndex();
		ndx.setComponentId(id);
		ndx.setKeyNumber((short)(lr.getValuesOfIndexBySeq().size()+1));
		lr.addToIndexBySeq(ndx);
		return ndx;
    }

	public static void addPhysicalFileOnly(PhysicalFile pf) {
		pfs.add(pf, pf.getComponentId(), pf.getName());
	}

	public static LRField getREDfieldFrom(LookupPath lookup, LRField ref) {
		JLTView jltview = jvm.getJLTViewFromLookup(lookup, false);
		return jltview.getRedFieldFromLookupField(ref.getComponentId());
	}

	public static Set<Short> getExtractFileNubers() {
		return extractFileNubers;
	}

    public static void addExtractFileNumber(short fileNumber) {
		extractFileNubers.add(fileNumber);
    }

    public static void allLFsNotRequired() {
		for(LogicalFile lf : lfs.getValues()) {
			lf.setRequired(false);
			lf.makePFsNotRequired();
		}
    }

    public static void setGenerationTime(Date dt) {
		generationTime = dt;
    }

	public static Date getGenerationTime() {
		return generationTime;
	}

	public static void fixupPFDDNames() {
		for(PhysicalFile pf : pfs.getValues()) {
			if(pf.getOutputDDName().length() == 0) {
				pf.setOutputDDName(String.format("O%07d", pf.getComponentId()));
			}
			if(pf.getInputDDName().length() == 0) {
				pf.setInputDDName(String.format("I%07d", pf.getComponentId()));
			}
		}
	}

    public static void fixupMaxHeaderLines() {
		Iterator<ViewNode> vi = views.getIterator();
		while(vi.hasNext()) {
			ViewNode view = vi.next();
			view.fixupMaxHeaderLines();
		}
    }

	public static int getNumberOfRequiredPhysicalFiles() {
		int count = 0;
		Iterator<PhysicalFile> pfi = pfs.getIterator();
		while(pfi.hasNext()) {
			if(pfi.next().isRequired()) {
				count++;
			}
		}
		return count;
	}

	public static LogicalRecord makeLR(String name, int lrid) {
        LogicalRecord lr = new LogicalRecord();
        lr.setComponentId(lrid);
        lr.setName(name);
        lr.setStatus(LrStatus.ACTIVE);
        lr.setLookupExitParams("");
		lrs.add(lr, lr.getComponentId(), lr.getName());
		return lr;
	}


}
