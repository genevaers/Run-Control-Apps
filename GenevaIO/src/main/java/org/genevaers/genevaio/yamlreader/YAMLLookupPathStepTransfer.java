package org.genevaers.genevaio.yamlreader;

import java.util.ArrayList;
import java.util.List;

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


public class YAMLLookupPathStepTransfer  {

	private List<YAMLLookupPathSourceFieldTransfer> sources = new ArrayList<>();
	private int targetLR;
	private int targetLF;
	private Integer id;
	private Integer environmentId;
	private int sequenceNumber;
	private int sourceLRId;
	private int joinId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}	

	public YAMLLookupPathStepTransfer() {
		
	}
	
	public void addSource(YAMLLookupPathSourceFieldTransfer s) {
		sources.add(s);
	}
	
	public List<YAMLLookupPathSourceFieldTransfer> getSources() {
		return sources;
	}
	
	public void setSources(List<YAMLLookupPathSourceFieldTransfer> srcs) {
		sources = srcs;
	}
	
	public void clearSources() {
		sources.clear();
	}

	public int getTargetLR() {
		return targetLR;
	}

	public void setTargetLR(int targetLR) {
		this.targetLR = targetLR;
	}

	public int getTargetLF() {
		return targetLF;
	}

	public void setTargetLF(int targetLF) {
		this.targetLF = targetLF;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setJoinId(int joinId) {
		this.joinId = joinId;
	}

	public int getJoinId() {
		return joinId;
	}

	public void setSourceLRId(int sourceLRId) {
		this.sourceLRId = sourceLRId;
	}
	
	public int getSourceLRId() {
		return sourceLRId;
	}
}
