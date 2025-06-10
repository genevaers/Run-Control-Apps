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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.genevaers.repository.components.enums.FileType;

/**
 * A Logical File is a collection of PhycicalFile components.
 * <p>Physcial Files are stored by both sequence number and by name.</p>
 * <p>Within a VDP there are no Logical File records. There are denormailsed Physcial File records that duplicate the LF id and name.</p>
 */
public class LogicalFile extends ComponentNode {

	private int id = 0;
	Map<Integer, PhysicalFile> pfsBySeq = new TreeMap<>();
	Map<String, PhysicalFile> pfsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	String name;

	// @Override
	// public void accept(VDPNodeVisitor vdpNodeVisitor) {
	// // TODO Auto-generated method stub
	//
	// }

	public void addPF(PhysicalFile ourPF) {
		pfsBySeq.put(getNumberOfPFs()+1, ourPF);
		pfsByName.put(ourPF.getName(), ourPF);
	}

	public void addPF(PhysicalFile ourPF, int seq) {
		pfsBySeq.put(seq, ourPF);
		pfsByName.put(ourPF.getName(), ourPF);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		if(name == null)
		  name = pfsBySeq.values().iterator().next().getLogicalFilename();
		return name;
	}

	public int getNumberOfPFs() {
		return pfsBySeq.size();
	}

	public void setID(int recordID) {
		id = recordID;
	}

	public Collection<PhysicalFile> getPFs() {
		return pfsBySeq.values();
	}

	public Iterator<PhysicalFile> getPFIterator() {
		return pfsBySeq.values().iterator();
	}

	public Iterator<Entry<Integer, PhysicalFile>> getPFSeqIterator() {
		return pfsBySeq.entrySet().iterator();
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Integer> getSetOfPFIDs() {
		Set<Integer> pfids = new HashSet<>();
		pfsBySeq.values().stream().forEach(pf -> pfids.add(pf.getComponentId()));
		return pfids;
	}

	public boolean isToken() {
		return pfsBySeq.values().iterator().next().getFileType().equals(FileType.TOKEN); //There should be only one for a token
	}

	public boolean isNotToken() {
		return !isToken();
	}

	public PhysicalFile getPhysicalFile(String name) {
		return pfsByName.get(name);
	}

    public void makePFsNotRequired() {
		for(PhysicalFile pf : pfsBySeq.values()) {
			pf.setRequired(false);
		}
    }
}
