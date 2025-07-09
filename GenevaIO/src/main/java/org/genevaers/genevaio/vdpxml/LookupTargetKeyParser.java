package org.genevaers.genevaio.vdpxml;

import java.util.Map;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalRecord;

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

import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.xml.sax.Attributes;

public class LookupTargetKeyParser extends BaseParser {
	private LookupPathKey lookupKey;
	private int lrid;
	private int lfid;
	private LogicalRecord lr;
	private LookupPathStep lookupStep;

	public LookupTargetKeyParser() {
		sectionName = "Target";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			case "FIELDREF":
				lookupKey.setFieldId(Integer.parseInt(attributes.getValue("ID")));
				break;
			default:
				break;
		}
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "LOGICALRECORDREF":
				lrid = Integer.parseInt(attributes.get("ID"));
				lookupKey.setTargetLrId(lrid);
				lookupStep.setTargetLRid(lrid);
				break;
			case "LOGICALFILEREF":
				lfid = Integer.parseInt(attributes.get("ID"));
				lookupKey.setTargetlfid(lfid);
				lookupStep.setTargetLFid(lfid);
				break;
			case "EXITREF":
				lr = Repository.getLogicalRecords().get(lrid);
				lr.setLookupExitID(Integer.parseInt(attributes.get("ID")));
				break;
			case "PARAMETER":
				lr.setLookupExitParams(text);
				break;
			default:
				break;
		}
	}

	public void setLookupKey(LookupPathKey lookupKey) {
		this.lookupKey = lookupKey;
	}

	public void setLookupStep(LookupPathStep lookupStep) {
		this.lookupStep = lookupStep;
	}

	public int getTargetLF() {
		return lfid;
	}

	public int getTargetLR() {
		return lrid;
	}

}
