package org.genevaers.repository.components;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.genevaers.repository.Repository;

import com.google.common.flogger.FluentLogger;

public class LookupPath extends ComponentNode {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private List<LookupPathStep> steps = new ArrayList<LookupPathStep>();
	private int id;
	private String name;
	private int targetLRid = 0;
	private int targetLFid = 0;
	private int destLrLfid;
	private Boolean optimizable;

	private Map<Integer, Integer> lr2lf = new HashMap<>();

	public LookupPath() {
		// super.record = new LookupPathRecord();
	}

	public int getNumberOfSteps() {
		return steps.size();
	}

	public void addStep(LookupPathStep step) {
		steps.add(step);
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public Iterator<LookupPathStep> getStepIterator() {
		return steps.iterator();
	}

	public LookupPathStep getStep(int i) {
		if(i <= steps.size()) {
			return steps.get(i-1);
		} else {
			return null;
		}
	}

	public String getName() {
		if(name == null) {
			name = steps.get(0).getName();
		}
		return name;
	}

	public String getSourceLR() {
		LogicalRecord srcLR = Repository.getLogicalRecords().get(steps.get(0).getSourceLR());
		if(srcLR != null) {
			return srcLR.getName();
		} else {
			return "Unknown";
		}
	}

	public String getTargetLRName() {
		LogicalRecord trgLR = Repository.getLogicalRecords().get(targetLRid);
		if(trgLR != null) {
			return trgLR.getName();
		} else {
			return "Unknown";
		}
	}
	public LogicalRecord getTargetLR() {
		return Repository.getLogicalRecords().get(getTargetLRID());
	}

	public String getTargetLF() {
		// TODO Auto-generated method stub
		return "";
	}

	public int getTargetLRID() {
		if(targetLRid == 0)
			targetLRid = steps.get(steps.size()-1).getTargetLR();
		return targetLRid; 
	}

	public void setName(String n) {
		name = n;
	}

	public void setTargetLRid(int assocLRid) {
		targetLRid = assocLRid;
	}

	public int getTargetLFID() {
		if(targetLFid == 0)
			targetLFid = steps.get(steps.size()-1).getTargetLF();
		return targetLFid; 
	}

	public void setTargetLFid(int assocLFid) {
		targetLFid = assocLFid;
	}

    public boolean isOptimizable() {
		if(optimizable == null) {
			int ei = Repository.getLogicalRecords().get(targetLRid).getLookupExitID();
			if(ei > 0) {
				optimizable = Repository.getUserExits().get(ei).isOptimizable();
			} else {
				optimizable = true;
			}
		}
        return optimizable;
    }

	public int getTargetLRIndexID() {
		//This is not correct
		//We need the index field ID
		LogicalRecord targLR = Repository.getLogicalRecords().get(steps.get(0).getTargetLR());
		LRIndex ndx = targLR.findFromIndexBySeq((short)1);
		return ndx.getFieldID();
	}

    public boolean isEffectiveDated() {
		LogicalRecord lr = getTargetLR();
		Iterator<LRIndex> ndxi = lr.getIteratorForIndexBySeq();
		while(ndxi.hasNext()) {
			LRIndex ndx = ndxi.next();
			if(ndx.isEffectiveDateStart() || ndx.isEffectiveDateEnd()) {
				return true;
			}
		}
        return false;
    }

    public int getTargetLFMatchingLrid(int lrId) {
		int lfid = 0;
		for (LookupPathStep lookupPathStep : steps) {
			if(lookupPathStep.getTargetLR() == lrId) {
				lfid = lookupPathStep.getTargetLF();
			}
		}
		return lfid;
    }

	public boolean hasExit() {
		return Repository.getLogicalRecords().get(targetLRid).getLookupExitID() > 0;
	}

	public int getReadExitId() {
		return Repository.getLogicalRecords().get(targetLRid).getLookupExitID();
	}

	public void setDestLrLfid(int destLrLfid) {
		this.destLrLfid = destLrLfid;
	}

	public int getDestLrLfid() {
		return destLrLfid;
	}

	public void saveLrLFPair(int lr, int lf) {
		lr2lf.put(lr, lf);
	}

	public Integer getLfFromLr(int lr) {
		return lr2lf.get(lr);
	}

	public void dumpDetails(String from) {
		logger.atInfo().log("LK %s from %s", name, from);
		Iterator<LookupPathStep> sti = steps.iterator();
		while (sti.hasNext()) {
			LookupPathStep st = sti.next();
			logger.atInfo().log("  Step %d", st.getStepNum());
			Iterator<LookupPathKey> ki = st.getKeyIterator();
			while (ki.hasNext()) {
				LookupPathKey k = ki.next();
				logger.atInfo().log("    Step %d", k.getKeyNumber());
			}
		}
	}
}
