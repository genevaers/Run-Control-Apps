package org.genevaers.genevaio.vdpxml;

import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;


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

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.xml.sax.Attributes;

import com.google.common.flogger.FluentLogger;

/**
 * This class will parse a Join-Target Record element into a
 * LookupPathStepTransfer object.
 */
public class LookupStepParser extends BaseParser {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private LookupPathStep lookupStep;
	private LookupPath currentLookupPath;

	private int currentLookupId;
	private int stepNumber;
	private int sourceLrid;

	private boolean srcLR = true;
	private LogicalRecord targetLR;
	private LookupSourceKeyParser lkskp;
	private LookupTargetKeyParser lktrgp;

	public LookupStepParser() {
		sectionName = "Steps";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			case "LOGICALFILEREF":
				lookupStep.setTargetLFid(Integer.parseInt(attributes.getValue("ID")));
				break;
			default:
				break;
		}
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) throws XMLStreamException {
		switch (name.toUpperCase()) {
			case "STEP":
				lookupStep = new LookupPathStep();
				stepNumber = Integer.parseInt(attributes.get("Number"));
				lookupStep.setStepNum(stepNumber);
				currentLookupPath.addStep(lookupStep);
				logger.atFiner().log("Add Step %d to Lookup %s",stepNumber, currentLookupPath.getName());
				break;
			case "SOURCE":
				logger.atFine().log("Source");
				lkskp = new LookupSourceKeyParser();
				lkskp.setLookupID(componentID);
				lkskp.setLookupStep(lookupStep);
				lkskp.parse(reader);
				logger.atFine().log("Source parsing completed for lookup " + currentLookupPath.getName());
				break;
			case "TARGET":
				logger.atFine().log("Target");
				lktrgp = new LookupTargetKeyParser();
				lktrgp.setLookupKey(lkskp.getLookupKey());
				lktrgp.setLookupStep(lookupStep);
				lktrgp.parse(reader);
				logger.atFine().log("Target parsing completed for lookup " + currentLookupPath.getName());
				break;
			case "PARAMETER":
				targetLR.setLookupExitParams(text);
				break;
			default:
				break;
		}
	}

	@Override
	public void endElement() {
		int targlf = lktrgp.getTargetLF();
		int targlr = lktrgp.getTargetLR();
		currentLookupPath.setTargetLFid(targlf);
		currentLookupPath.setTargetLRid(targlr);
		// set target LF and LR for each key
		Iterator<LookupPathKey> ki = lookupStep.getKeyIterator();
		while (ki.hasNext()) {
			LookupPathKey key = ki.next();
			key.setTargetLrId(targlr);
			key.setTargetlfid(targlf);
		}
	}

	public void setLookupID(int id) {
		currentLookupId = id;
	}

	public void setStepNumber(int s) {
		stepNumber = s;
	}

	public void setLrRef(int lrid) {
		if (srcLR == false) {
			sourceLrid = lrid;
			srcLR = true;
		} else {
			srcLR = false;
			lookupStep.setTargetLRid(lrid);
		}
	}

	public void setExitRef(int exitRef) {
		int trgLR = lookupStep.getTargetLR();
		targetLR = Repository.getLogicalRecords().get(trgLR);
		targetLR.setLookupExitID(exitRef);
	}

	public void setLookup(LookupPath lookup) {
		currentLookupPath = lookup;
	}

}
