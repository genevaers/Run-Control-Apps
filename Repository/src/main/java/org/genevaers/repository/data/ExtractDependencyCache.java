package org.genevaers.repository.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.data.ViewLogicDependency.LogicType;

public class ExtractDependencyCache {

	private List<ViewLogicDependency> dependencies = new ArrayList<>();

	private Map<String, Integer> pfsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, Integer> exitsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, Integer> procsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, Integer> fieldsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, LookupRef> lookupsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Set<Integer> writeExits = new HashSet<>();

	private LogicType currenLogicType;
	private int currentParentId;

	public void clearNamedEntries() {
		pfsByName.clear();
		exitsByName.clear();
		procsByName.clear();
		fieldsByName.clear();
		lookupsByName.clear();
		writeExits.clear();
	}

	public void clear() {
		clearNamedEntries();
		dependencies.clear();
	}

	public Integer addLookupIfAbsent(String name, LookupPath lk) {
		LookupRef localLkref = lookupsByName.computeIfAbsent(name, s -> getLookupRefByName(s,lk.getID()));
		Integer lkid = localLkref != null ? localLkref.getId() : null;
		if(lkid != null) {
			dependencies.add(new ViewLogicDependency(currenLogicType, null, lkid, null, null, null, currentParentId));
		}
		return lkid;
	}

	private LookupRef getLookupRefByName(String name, int id) {
		LookupRef lkref = null;
		//Get all of the lookup target fields here
				lkref = new LookupRef();
				lkref.setId(id);
				lkref.setName(name);
				Iterator<Entry<String, Integer>> lkfsi = lkref.getLookupFieldsByName().entrySet().iterator();
				while(lkfsi.hasNext()) {
					Entry<String, Integer> lkf = lkfsi.next();
					if(!lkf.getKey().equals("Lookup_ID")) {
						lkref.getLookupFieldsByName().put(lkf.getKey(), lkf.getValue());
					}
				}
		return lkref;
	}

	/*
	 * Add the named field within the context of the parent component
	 * That is say only one dependenct on a given field per column
	 */
	public Integer addNamedField(String name, int id) {
		Integer fid = fieldsByName.putIfAbsent(name, Integer.valueOf(id));
		if(fid == null) {
			dependencies.add(new ViewLogicDependency(currenLogicType, null, null, id, null, null, currentParentId));
		}
		return id;

	}

	public Integer getNamedField(String fieldName) {
		return fieldsByName.get(fieldName);
	}

	public Integer addNamedLookupField(LookupPath lookup, LRField fld){
		LookupRef localLkref = lookupsByName.computeIfAbsent(lookup.getName(), s -> getLookupRefByName(s, lookup.getID()));
		Integer r = localLkref.getLookupFieldsByName().putIfAbsent(fld.getName(), fld.getComponentId());
		if(r == null) {
			dependencies.add(new ViewLogicDependency(currenLogicType, null, lookup.getID(), fld.getComponentId(), null, null, currentParentId));
		}
		return r;
	}

	public Integer getNamedLookupField(String lkname, String field){
		return lookupsByName.get(lkname).getField(field);
	}
	private Integer getLkField(LookupRef lkref, LRField fld) {
		return fld.getComponentId();
	}


	//Need to manage the overlap... name and procdure to same exit
	public Integer addIfAbsent(String name, int id) {
		return exitsByName.putIfAbsent(name, id);
	}
	
	private Integer setExitID(String name, int id) {
		return id;
	}

	public Integer getProcedure(String name) {
		return procsByName.computeIfAbsent(name, s -> getProcedureID(s));
	}
	
	private Integer getProcedureID(String name) {
		return 0;
	}

	public Integer setNamedLfPfAssoc(String fullName, int id) { 
		Integer pfid = pfsByName.putIfAbsent(fullName, id);
		if(pfid == null) {
			dependencies.add(new ViewLogicDependency(currenLogicType, null, null, null, null, id, currentParentId));
		}
		return id;
	}

	public Integer getPfAssocID(String fullName) {
		return pfsByName.get(fullName);
	}

	public void setLRFieldNameIds(Map<String, Integer> fieldsFromLr) {
		fieldsByName = fieldsFromLr;
	}

	public boolean needsFieldNames() {
		return fieldsByName.isEmpty();
	}

	public void addExitID(int componentId) {
		if(writeExits.add(componentId)) {
			dependencies.add(new ViewLogicDependency(currenLogicType, null, null, null, componentId, null, currentParentId));
		}
	}

	public Set<Integer> getExitIDs() {		
		return writeExits;
	}

    public Stream<Integer> getFieldIDs() {
		return fieldsByName.values().stream();
    }

	public Stream<LookupRef> getLookupsStream() {
		return lookupsByName.values().stream();
	}

	public Stream<Integer> getLFPFAssocIDs() {
		return pfsByName.values().stream();
	}

	public Stream<ViewLogicDependency> getDependenciesStream() {
		return dependencies.stream();
	}

	public void setCurrenLogicType(LogicType currenLogicType) {
		this.currenLogicType = currenLogicType;
	}

	public LogicType getCurrenLogicType() {
		return currenLogicType;
	}

	public void setCurrentParentId(int currentParentId) {
		this.currentParentId = currentParentId;
	}

	public int getCurrentParentId() {
		return currentParentId;
	}

}
