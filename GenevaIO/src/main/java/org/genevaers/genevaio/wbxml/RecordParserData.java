package org.genevaers.genevaio.wbxml;

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
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;

// import com.ibm.safr.we.constants.DateFormat;
// import com.ibm.safr.we.data.transfer.SAFRTransfer;
// import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This abstract class represent a parser which converts Record XML
 * elements into component transfer objects. It defines one public method which
 * provides the algorithm for retrieving the elements, parsing them and
 * returning the transfer objects. Concrete subclasses must implement two
 * abstract methods which define component type-specific behavior.
 */
public class RecordParserData {

	public static Map<Integer, Integer> vs2lrlf = new HashMap<>();
	public static Map<Integer, Integer> vs2lfpf = new HashMap<>();
	public static Map<Integer, Integer> viewOutlfpf = new HashMap<>();

	public static Map<Integer, LKStepKeyAssocs> lookupKeyDestAssocs = new HashMap<>();

	public static Map<Integer, LRIndex> effdateStarts = new HashMap<>();
	public static Map<Integer, LRIndex> effdateEnds = new HashMap<>();

	public static Map<Integer, LRLF> lrlfs = new HashMap<>();
	public static Map<Integer, LFPF> lfpfs = new HashMap<>();

	public RecordParserData() {
	}

	public static void viewSourceFixups() {
		Iterator<ViewNode> vi = Repository.getViews().getIterator();
		while (vi.hasNext()) {
			ViewNode v = vi.next();
			Iterator<ViewSource> vsi = v.getViewSourceIterator();
			while (vsi.hasNext()) {
				ViewSource vs = vsi.next();
				int lrlfAssocID = vs2lrlf.get(vs.getComponentId());
				LRLF lrlf = lrlfs.get(lrlfAssocID);
				vs.setSourceLFID(lrlf.lfid);
				vs.setSourceLRID(lrlf.lrid);
				if (vs2lfpf.size() > 0) {
					int lfpfAssocID = vs2lfpf.get(vs.getComponentId());
					LFPF lfpf = lfpfs.get(lfpfAssocID);
					if (lfpf != null) {
						vs.setOutputLFID(lfpf.lfid);
						vs.setOutputPFID(lfpf.pfid);
					}
				}
			}
		}
	}

	public static void lookupfixups() {
		Iterator<LookupPath> li = Repository.getLookups().getIterator();
		while (li.hasNext()) {
			LookupPath lk = li.next();
			LRLF lrlf = lrlfs.get(lk.getDestLrLfid());
			lk.setTargetLFid(lrlf.lfid);
			lk.setTargetLRid(lrlf.lrid);

			Iterator<LookupPathStep> lksi = lk.getStepIterator();
			while (lksi.hasNext()) {
				LookupPathStep lkstep = lksi.next();
				LRLF slrlf = lrlfs.get(lkstep.getLrLfAssocid());
				lkstep.setTargetLFid(slrlf.lfid);
				lkstep.setTargetLRid(slrlf.lrid);

				LKStepKeyAssocs assocs = lookupKeyDestAssocs.get(lkstep.getStepID());
				Iterator<LookupPathKey> lkski = lkstep.getKeyIterator();
				while (lkski.hasNext()) {
					LookupPathKey lksk = lkski.next();
					int destassocid = assocs.getAssocID(lksk.getKeyNumber());
					if (destassocid != 0) {
						LRLF trglrlf = lrlfs.get(destassocid);
						lksk.setTargetLrId(trglrlf.lrid);
						lksk.setTargetlfid(trglrlf.lfid);
					} else {
						lksk.setTargetLrId(lkstep.getTargetLR());
						lksk.setTargetlfid(lkstep.getTargetLF());
					}
					lksk.setSourceLrId(lkstep.getSourceLR());
					lksk.setStepNumber((short) lkstep.getStepNum());
				}
			}
		}
	}

	public static void viewOutputFixups() {
		for( Entry<Integer, Integer> entry : viewOutlfpf.entrySet()) {
			ViewNode vw = Repository.getViews().get(entry.getKey());
			LFPF lfpf = lfpfs.get(entry.getValue());
			if (lfpf != null) {
				PhysicalFile outpf = Repository.getPhysicalFiles().get(lfpf.pfid);
				outpf.setRequired(true);
				vw.setOutputFileFrom(outpf);
			}
		}
	}

	public static void buildLFs() {
		Iterator<LFPF> lfpfi = lfpfs.values().iterator();
		int lf = 0;
		LogicalFile logFile = null;
		while (lfpfi.hasNext()) {
			LFPF lfpf = lfpfi.next();
			if (lfpf.lfid != lf) {
				logFile = Repository.getLogicalFiles().get(lfpf.lfid);
			}
			PhysicalFile pf = Repository.getPhysicalFiles().get(lfpf.pfid);
			pf.setLogicalFileId(lfpf.lfid);
			pf.setLogicalFilename(logFile.getName());
			logFile.addPF(pf);
		}
	}

	public static void clearAndInitialise() {
		vs2lrlf = new HashMap<>();
		vs2lfpf = new HashMap<>();
		viewOutlfpf = new HashMap<>();

		lookupKeyDestAssocs = new HashMap<>();

		effdateStarts = new HashMap<>();
		effdateEnds = new HashMap<>();

		lrlfs = new HashMap<>();
		lfpfs = new HashMap<>();
	}

}
