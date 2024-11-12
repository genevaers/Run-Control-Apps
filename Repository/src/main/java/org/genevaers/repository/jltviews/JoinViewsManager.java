package org.genevaers.repository.jltviews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.LookupType;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.data.ReferenceReportEntry;

import com.google.common.flogger.FluentLogger;

/**
 * There be dragons here!
 * A lookup in the workbench defines the Path to the reference data. 
 * But it is only once we process views that use the reference data do we know how to build the JLT.
 * Amd this class acts as an accumulator of that data
 * 
 * So in an attempt to tame the beast I am creating this class
 * and throwing out some words (rather a lot... take heed).
 * 
 * There are 5 manageable types of joins/lookups (create an enum to define them)
 *  Normal
 *  Exit
 *  External Data
 *  SortKeyTitle
 *  SortKeyTitleViaExternalData - yet to be confirmed if these exist
 * 
 * A Join can have one or more steps - we have seen a customer instance with 14
 * Each step is in effect its own join.
 * 
 * A Normal join results in the generation of
 * ------------------------------------------
 *  Entries in the XLT to control the runtime execution of the lookup
 *  Entries in the JLT to control the generation of the reference data 
 *  2 or more views added to the VDP file
 *  2 or more Logical Records added to the VDP file
 * 
 * XLT entries are made up of 
 *  A declaration function code with its own 3 way gotos
 *  One or more function codes to buld the lookup key
 *  A function code to execute the lookup
 *  Closing function codes that use the result of the lookup
 * 
 * The JLT entries determine the contents of the reference data files
 * Each reference data source will result in a RED file.
 * The content of the RED (reference data file) is derived from the fields
 * of the original that are used in logic statements As well as those that form
 * the key for the lookup.
 * For each RED file there is also an entry made to the REH (reference header) file.
 * It describes the contents of the RED file.
 * 
 * There is a single reference header file and each normal join step results in
 * a RED file and an entry in the REH file.
 * 
 * The lookup path from which the JLT is generated has a source Logical Record (LR).
 * We generate two internal LRs from original. 
 *      The generation LR, from which the JLT is generated.
 *      The reference LR that maps the fields that are looked up and is used at run time.
 *          These are the original fields but are reorganised as needed.
 *          The fields are used in the xxL function codes.
 * 
 * We also generate an LR to map the REH.
 *
 * There are no LFs and PFs associate with the generated LRs.
 * The data sets written too are associated with predefined DDnames of the form REFRnnn
 * Where nnn is derived from the runtime lookup id.
 * Note runtime lookup ids are generated sequentially based on the number of lookups.
 * 
 * Each reference file results in a genereated view that maps the source LR fields to the
 * reference file LR.
 * An additonal view is also generated for the REH file.
 * 
 * The first function code of a lookup is either JOIN or LKLR.
 * This is a 3 way function code.
 *  If the lookup has been performed before then 
 *      execution continues at the true or false goto line of the previous run.
 *  else
 *      the lookup key is generated and the lookup performed
 * 
 * Exit Lookup results in the generation of
 * ----------------------------------------
 *  Entries in the XLT to control the runtime execution of the lookup
 * 
 * An exit lookup has no generated reference data. 
 * Therefore does not result in any JLT entries.
 * It is up to the lookup exit to supply the data based upon the 
 * input key value. 
 * This means a lookup exit is realy just a means of providing external functionality 
 * to add values to the resulting extract record.
 * 
 * A lookup exit is associated with a lookup LR that defines the result returned from the exit.
 * 
 * A similar set of function codes are generated for a lookup exit.
 * The difference being in the function code that executes the lookup. 
 * It specifies the exit to use.
 * 
 * An exit as an associated optimizable flag. If true the lookup is execute in the normal way.
 * Else the lookup is always performed. Therefore the exit is always called.
 * 
 * External Data Lookup
 * --------------------
 * 
 * For this case again there are no JLT entries generated.
 * However, we do build a RED LR based on the LR that defines the exernal data.
 * The field offsets are the same as the original minus the primary key length including effective dates.
 * 
 * It is the generated LR fields that are used in subsequent xxL function codes.
 * 
 * Sort Key Title Lookups
 * ----------------------
 * An SKT lookup is used to generate data for subsequent use in MR88.
 * 
 * The generation of the JLT entries are similar to those of a "normal" lookup.
 * A RED file is generated in the same way.
 * 
 * However, the summary of the reference data is written to the RTH file not the REH file.
 * 
 * The XLT entries are similar to that of a "normal" lookup.
 * However the lookup itself is not performed.
 * Instead the key value is written to the Sort Title Area of the extract record via a KSLK function code.
 * The function codes for a sort key title lookup are generated immediatly after the sork key function code 
 * with which they are associated.
 * 
 * Sort Key Title via external data
 * --------------------------------
 * This is only here for completeness and is not used at the moment.
 * 
 */
public class JoinViewsManager {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	public class JoinTargetEntry
	{
		public byte flag;
		public int lfid;
		public String lfName;
	};
    
	//Note reference Joins are for both normal and SKT
	Map<Integer, JLTViewMap<ReferenceJoin>> referenceDataSet = new TreeMap<>();
    JLTViewMap<ReferenceJoin> referenceJoins;
    JLTViewMap<ExitJoin> exitJoins = new JLTViewMap<ExitJoin>();
    JLTViewMap<ExternalJoin> externalJoins = new JLTViewMap<ExternalJoin>();
	int totalNumberOfJoins = 0;

	private Map<Integer, JLTView> keyFieldsToJoin = new TreeMap<>();

	private List<JoinTargetEntry> joinTargets = new ArrayList<>();

	private int headerViewNumber = 0;

    //The way C++ does this is to keep 3 maps for
    //Normal, Exit and External lookups
    //They each can have an association SKT flag

    //How do we want to handle it?
    //create a key base on LF/LR/SKT/JointType
    //and a single map
    //Key will need to be comparable so we can order them
    //
    //or key the maps separate like C++?
    // public JLTViews getJLTViews() {
	// 	return jViews;
	// }

	// public JLTView getJLTViewForLF(int id) {
	// 	return jViews.getJLTViewForLF(id);
	// }

	// public void addJLTViewField(LookupPath lookup, LRField fld) {
	// 	JLTView jltv = jViews.getJLTViewForLF(lookup.getTargetLFID());
	// 	if(jltv == null) {
	// 		jltv = new JLTView(lookup.getTargetLFID(), lookup.getTargetLRID());
	// 		// TODO We will need to be cleverer here - expand when there are symbols etc
	// 		jViews.add(lookup.getTargetLFID(), jltv);
	// 	}
	// 	jltv.addRefField(fld);
	// }

	/*
	 * Logic refers to a lookup.
	 */
	public JLTView addJLTView(LookupPath lookup) {
		JLTView jltv = getOrMakeJLTView(lookup);
		jltv.addReason("Lookup Ref", lookup.getID(), 0);
		//Need any internal source steps too
		resolveLookupSourceKeys(lookup, false);
		return jltv;
	}

	/*
	 * Logic refers to a looked up field
	 */
	public JLTView addJLTViewFromLookupField(LookupPath lookup, LRField refField) {
		JLTView jltv = getOrMakeJLTView(lookup);
		jltv.addReason("Ref", lookup.getID(), refField.getComponentId());
		//Need any internal source steps too
		resolveLookupSourceKeys(lookup, false);
		jltv.addRefField(refField);
		//if the ref field is not from the target is must be from an internal step
		return jltv;
	}

	public void addSortKeyJLTViewField(LookupPath lookup, LRField refField) {
		int lrid = lookup.getTargetLRID();
		int lfid = lookup.getTargetLFID();
		lookup.saveLrLFPair(lrid, lfid);

		referenceJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.SKT, lfid));

		JLTView jltv = referenceJoins.getOrAddJoinifAbsent(LookupType.SKT, lrid, lookup.getID());
		//Need any internal source steps too
		resolveLookupSourceKeys(lookup, true);
		jltv.addRefField(refField);
		//jltv.setUniqueKey(generateUniqueKey(lookup));
	}

	private JLTViewMap<ReferenceJoin> makeReferenceJoinMap(LookupType skt, int lfid) {
		return new JLTViewMap<ReferenceJoin>();
	}

	public JLTView getJLTViewFromLookup(LookupPath lookup, boolean skt) {
		int lrid = lookup.getTargetLRID();
		int lfid = lookup.getTargetLFID();
		LogicalRecord lr = Repository.getLogicalRecords().get(lrid);
		if(lr.getLookupExitID() > 0) {
			return exitJoins.getJLTView(lrid, false);
		} else {
			LogicalFile lf = Repository.getLogicalFiles().get(lfid);
			if(lf.getPFIterator().next().getInputDDName().startsWith("$$")) {
				//External files have an input DDname starting with $$
				//Go figure...
				return externalJoins.getJLTView(lrid, false);
			} else {
				referenceJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.NORMAL, lfid));
				return referenceJoins.getJLTView(lrid, skt);
			}
		}
	}

	private JLTView getOrMakeJLTView(LookupPath lookup) {
		int lrid = lookup.getTargetLRID();
		int lfid = lookup.getTargetLFID();
		lookup.saveLrLFPair(lrid, lfid);

		LogicalRecord lr = Repository.getLogicalRecords().get(lrid);
		JLTView jltv;
		if(lr.getLookupExitID() > 0) {
			jltv = exitJoins.getOrAddJoinifAbsent(LookupType.EXIT, lrid, lookup.getID());
		} else {
			LogicalFile lf = Repository.getLogicalFiles().get(lfid);
			if(lf.getPFIterator().next().getInputDDName().startsWith("$$")) {
				//External files have an input DDname starting with $$
				//Go figure...
				jltv = externalJoins.getOrAddJoinifAbsent(LookupType.EXTERNAL, lrid, lookup.getID());
			} else {
				referenceJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.NORMAL, lfid));
				jltv = referenceJoins.getOrAddJoinifAbsent(LookupType.NORMAL, lrid, lookup.getID());
			}
		}
		return jltv;
	}

	private void resolveLookupSourceKeys(LookupPath lookup, boolean skt) {
        //Need to iterate the steps and get any dependent views
        //Or this could be done in the repo step above...
		if(lookup.getNumberOfSteps() > 1) { //single step lookups take care of themselves
			Iterator<LookupPathStep> si = lookup.getStepIterator();
			while(si.hasNext()) {
				LookupPathStep step = si.next();
				int lrid = step.getTargetLR();
				int lfid = step.getTargetLF();
				ReferenceJoin stepJoin = null;
				if(step.getStepNum() <= lookup.getNumberOfSteps()) { //This is an internal step 
					lookup.saveLrLFPair(lrid, lfid);
					//This looks like a call to getOrMakeJLTView(lookup);
					if(Repository.getLogicalRecords().get(step.getTargetLR()).getLookupExitID() == 0) {
						if(skt) {
							referenceJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.SKT, lfid));							
							referenceJoins.getOrAddJoinifAbsent(LookupType.SKT, step.getTargetLR(), lookup.getID());
						} else {
							LogicalFile lf = Repository.getLogicalFiles().get(lfid);
							if(lf.getPFIterator().next().getInputDDName().startsWith("$$")) {
								//externalJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.EXTERNAL, lfid));
								ExternalJoin extJoin = externalJoins.getOrAddJoinifAbsent(LookupType.EXTERNAL, step.getTargetLR(), lookup.getID());
								extJoin.addReason("SrcKeyStep" + step.getStepNum(), lookup.getID(), 0);
							} else {
								referenceJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.NORMAL, lfid));
								stepJoin = referenceJoins.getOrAddJoinifAbsent(LookupType.NORMAL, step.getTargetLR(), lookup.getID());
								stepJoin.addReason("SrcKeyStep" + step.getStepNum(), lookup.getID(), 0);
							}
						}
					} else {
						exitJoins.getOrAddJoinifAbsent(LookupType.EXIT, step.getTargetLR(), lookup.getID());
					}
				}
				if(step.getStepNum() > 1) {
					//resolve the source fields - if not from the input lr
					//we don't know what that is here?
					resolveKeyFields(step, lookup);
				}
			}
		}
    }

	private void resolveKeyFields(LookupPathStep step, LookupPath lookup) {
		Iterator<LookupPathKey> ki = step.getKeyIterator();
		while(ki.hasNext()) {
			LookupPathKey srcKey = ki.next();
			int keyfldID = srcKey.getFieldId();
			if(keyfldID != 0) { //where does the field come from?
				int keyLrId = Repository.getFields().get(keyfldID).getLrID();
				Integer keyLf = lookup.getLfFromLr(keyLrId);
				//need to find a JLTView that has this LF LR
				//a problem with lookup records is that we do not have the LF of the source
				//CPP solution was to maintain a map of LRs to LFs
				//built up as we process Lookup steps
				if(keyLf != null) {
					JLTView jltv = null;;
					//add the intermediate target join if needed
					if(Repository.getLogicalRecords().get(step.getTargetLR()).getLookupExitID() == 0) {
						if(Repository.getLogicalRecords().get(keyLrId).getLookupExitID() == 0) {
							LogicalFile lf = Repository.getLogicalFiles().get(keyLf);
							if(lf.getPFIterator().next().getInputDDName().startsWith("$$")) {
								//externalJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.EXTERNAL, lfid));
								ExternalJoin extJoin = externalJoins.getOrAddJoinifAbsent(LookupType.EXTERNAL, step.getTargetLR(), lookup.getID());
								extJoin.addReason("KeyField" + step.getStepNum(), lookup.getID(), 0);
							} else {
								referenceJoins = referenceDataSet.computeIfAbsent(keyLf, k -> makeReferenceJoinMap(LookupType.NORMAL, keyLf));
								jltv = referenceJoins.getOrAddJoinifAbsent(LookupType.NORMAL, keyLrId, lookup.getID());
								jltv.addReason("KeyField", lookup.getID(), keyfldID);
							}
						} else {
							//key field comes from a lookup exit
							jltv = exitJoins.getOrAddJoinifAbsent(LookupType.EXIT, keyLrId, lookup.getID());
							jltv.addReason("KeyField", lookup.getID(), keyfldID);
						}
					} else {
						jltv = exitJoins.getOrAddJoinifAbsent(LookupType.EXIT, keyLrId, lookup.getID());
					}
					if(jltv != null) {
						keyFieldsToJoin.put(keyfldID, jltv);
					}

					//Since we're here (keyLF not null)
					//There must be an existing jltv for the key
					if(Repository.getLogicalRecords().get(keyLrId).getLookupExitID() == 0) {
						LogicalFile lf = Repository.getLogicalFiles().get(keyLf);
						if(lf.getPFIterator().next().getInputDDName().startsWith("$$")) {
							//externalJoins = referenceDataSet.computeIfAbsent(lfid, k -> makeReferenceJoinMap(LookupType.EXTERNAL, lfid));
							ExternalJoin extJoin = externalJoins.getOrAddJoinifAbsent(LookupType.EXTERNAL, step.getTargetLR(), lookup.getID());
							extJoin.addReason("KeyField" + step.getStepNum(), lookup.getID(), 0);
						} else {
							referenceJoins = referenceDataSet.computeIfAbsent(keyLf, k -> makeReferenceJoinMap(LookupType.NORMAL, keyLf));
							jltv = referenceJoins.getOrAddJoinifAbsent(LookupType.NORMAL, keyLrId, lookup.getID());
						}
					} else {
						jltv = exitJoins.getOrAddJoinifAbsent(LookupType.EXIT, keyLrId, lookup.getID());
					}

					if(jltv == null) {
						//big trouble
					} else {
						//Add the intermediate field as requied to be looked up
						jltv.addRefField(Repository.getFields().get(keyfldID));
					}
					//jltv.setUniqueKey(generateUniqueKey(lookup));
				}  // if it is null then what?
			}
		}
	}

	public JLTViewMap<ReferenceJoin> getReferenceJLTViews() {
		return referenceJoins;
	}

	public void get(LookupPath lookup) {
    }

    public JLTViewMap<ExitJoin> getExitJLTViews() {
        return exitJoins;
    }

    public void logdata() {
		logger.atFine().log("Lookup data");
		Iterator<Entry<Integer, JLTViewMap<ReferenceJoin>>> refdi = referenceDataSet.entrySet().iterator();
		while(refdi.hasNext()) {
			Entry<Integer, JLTViewMap<ReferenceJoin>> refd = refdi.next();
			refd.getValue().log(refd.getKey());
		}
		Iterator<Entry<Integer, JLTView>> k2ji = keyFieldsToJoin.entrySet().iterator();
		logger.atFine().log("Key fields to Join View");
		while (k2ji.hasNext()) {
			Entry<Integer, JLTView> k2j = k2ji.next();
			logger.atFine().log("%d -> %d", k2j.getKey(), k2j.getValue().getRefViewNum());
		}
		logger.atFine().log("Exit Joins");
		Iterator<ExitJoin> eji = exitJoins.getIterator();
		while (eji.hasNext()) {
			ExitJoin ej = eji.next();
			logger.atFine().log("Orig ID %d", ej.getOrginalLookupId());			
		}
		logger.atFine().log("External Joins");
		Iterator<ExternalJoin> xji = externalJoins.getIterator();
		while (xji.hasNext()) {
			ExternalJoin xj = xji.next();
			logger.atFine().log("Orig ID %d", xj.getOrginalLookupId());			
		}
		for (Entry<String, UniqueKeyData> ukv : UniqueKeys.keysMap.entrySet()) {
			logger.atFine().log("Unique Join Key %s, %d -> %d", ukv.getKey(), ukv.getValue().getOriginalJoinId(), ukv.getValue().getNewJoinId());			
		}
    }

	public List<ReferenceReportEntry> getRefReportEntries() {
		List<ReferenceReportEntry> entries = new ArrayList<>();
		Iterator<Entry<Integer, JLTViewMap<ReferenceJoin>>> refdi = referenceDataSet.entrySet().iterator();
		while(refdi.hasNext()) {
			Entry<Integer, JLTViewMap<ReferenceJoin>> refd = refdi.next();
			Collection<ReferenceJoin> refViews = refd.getValue().getValues();
			addToEntriesForReferenceViews(entries, refd.getKey(), refViews);
		}
		return entries;
	}

    private void addToEntriesForReferenceViews(List<ReferenceReportEntry> entries, Integer lfid, Collection<ReferenceJoin> refViews) {
		Iterator<ReferenceJoin> ri = refViews.iterator();
		while (ri.hasNext()) {
			ReferenceJoin ref = ri.next();
			ReferenceReportEntry refEntry = new ReferenceReportEntry();
			refEntry.setWorkDDName(String.format("REFR%03d", ref.getDdNum()));
			refEntry.setViewID(ref.getRefViewNum());
			refEntry.setViewName(ref.getView().getName());
			PhysicalFile pf = Repository.getLogicalFiles().get(lfid).getPFIterator().next();
			refEntry.setRefDDName(pf.getInputDDName());
			refEntry.setRefPFID(pf.getComponentId());
			refEntry.setRefPFName(pf.getName());
			refEntry.setRefLRID(ref.getLRid());
			refEntry.setRefLFID(lfid);
			refEntry.setKeylen(ref.getKeyLength());
			switch (ref.getEffDateCode()) {
				case JLTView.EFF_DATE_BOTH:
					refEntry.setEffStart("Y ");
					refEntry.setEffEnd("Y ");
					break;
				case JLTView.EFF_DATE_START:
					refEntry.setEffStart("Y ");
					refEntry.setEffEnd("N ");
					break;
				case JLTView.EFF_DATE_END:
					refEntry.setEffStart("N ");
					refEntry.setEffEnd("Y ");
					break;
				case JLTView.EFF_DATE_NONE:
					refEntry.setEffStart("N ");
					refEntry.setEffEnd("N ");
					break;
				default:
					break;
			}
			entries.add(refEntry);
		}
	}

	public int getHeaderViewNumber() {
		if(headerViewNumber == 0) {
			headerViewNumber = JLTView.JOINVIEWBASE + getNumberOfReferenceJoins() + 1;
		} else {
			headerViewNumber++;
		}
        return headerViewNumber;
    }

    private int getNumberOfReferenceJoins() {
		Iterator<JLTViewMap<ReferenceJoin>> refdi = referenceDataSet.values().iterator();
		totalNumberOfJoins = 0;
		while(refdi.hasNext()) {
			JLTViewMap<ReferenceJoin> refd = refdi.next();
			totalNumberOfJoins += refd.getNumberOfJoins();
		}
		return totalNumberOfJoins + externalJoins.getNumberOfJoins();
	}

	public Map<Integer, JLTViewMap<ReferenceJoin>> getReferenceDataSet() {
		return referenceDataSet;
	}

	public void addJoinTarget(byte flag, int id, String name) {
		JoinTargetEntry ntry = new JoinTargetEntry();
		ntry.flag = flag;
		ntry.lfid = id;
		ntry.lfName = name;
		joinTargets.add(ntry);
	}

	public Iterator<JoinTargetEntry> getJoinTargetsIterator() {
		return joinTargets.iterator();
	}

	public JLTView getJltViewFromKeyField(int keyfld, LookupPath lookup) {
		LRField kf = Repository.getFields().get(keyfld);
		int lridOfkf = kf.getLrID();
		Integer lfOfKey = lookup.getLfFromLr(lridOfkf);
		JLTView jv;
		LogicalFile lf = Repository.getLogicalFiles().get(lfOfKey);
		if(lf.getPFIterator().next().getInputDDName().startsWith("$$")) {
			jv = externalJoins.getJLTView(lridOfkf, false);
		} else {
			if(Repository.getLogicalRecords().get(lridOfkf).getLookupExitID() == 0) {
				JLTViewMap<ReferenceJoin> jvs = referenceDataSet.get(lfOfKey);
				jv = jvs.getJLTView(lridOfkf, false);
			} else {
				jv = exitJoins.getJLTView(lridOfkf, false);
			}
		}
		jv.setSourceLFID(lfOfKey);
		return jv;
	}
}
