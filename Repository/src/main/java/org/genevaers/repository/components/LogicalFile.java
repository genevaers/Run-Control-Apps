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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.genevaers.repository.components.enums.FileType;


public class LogicalFile extends ComponentNode {

	private int id = 0;
	List<PhysicalFile> pfs = new ArrayList<>();
	Map<String, PhysicalFile> pfsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	String name;

	// @Override
	// public void accept(VDPNodeVisitor vdpNodeVisitor) {
	// // TODO Auto-generated method stub
	//
	// }

	public void addPF(PhysicalFile ourPF) {
		pfs.add(ourPF);
		pfsByName.put(ourPF.getName(), ourPF);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		if(name == null)
		  name = pfs.get(0).getLogicalFilename();
		return name;
	}

	public int getNumberOfPFs() {
		return pfs.size();
	}

	public void setID(int recordID) {
		id = recordID;
	}

	public Collection<PhysicalFile> getPFs() {
		return pfs;
	}

	public Iterator<PhysicalFile> getPFIterator() {
		return pfsByName.values().iterator();
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Integer> getSetOfPFIDs() {
		Set<Integer> pfids = new HashSet<>();
		pfs.stream().forEach(pf -> pfids.add(pf.getComponentId()));
		return pfids;
	}

	public boolean isToken() {
		return pfs.get(0).getFileType().equals(FileType.TOKEN); //There should be only one for a token
	}

	public boolean isNotToken() {
		return !pfs.get(0).getFileType().equals(FileType.TOKEN);
	}

	public PhysicalFile getPhysicalFile(String name) {
		return pfsByName.get(name);
	}

    public void makePFsNotRequired() {
		for(PhysicalFile pf : pfs) {
			pf.setRequired(false);
		}
    }
}
