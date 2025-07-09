package org.genevaers.genevaio.vdpxml;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ExitType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.flogger.FluentLogger;

public class VDPHandler extends DefaultHandler {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private StringBuilder data;
	private BaseParser currentParser;
	private int currentLRID;
	private int currentIndexID;
	private int currentLookupID;
	private int currentViewID;

	private boolean srcLR;

	private ViewRecordParser currentViewParser;

	private Attributes elAttributes;

	private int currentViewSourceID;

	private LookupStepParser currentStepParser;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		switch (qName) {
			case "JobInfo":
				logger.atFine().log("Parsing Job Info");
				// parseRecordsForElementWithParser(elementName, new SafrvdpParser(),
				// "Database");
				break;
			case "ControlRecord":
				logger.atFine().log("Parsing ControlRecords");
				currentParser = new CRRecordParser();
				currentParser.setComponentID(Integer.parseInt(attributes.getValue(0)));
				break;
			case "Partitions":
				logger.atFine().log("Parsing Partitions");
				currentParser = new PhysicalFileRecordParser();
				break;
			// case "ExitRef":
			// int exitRef = Integer.parseInt(attributes.getValue("ID"));
			// ExitType exitType = ExitType.fromdbcode(attributes.getValue("Type"));
			// switch (exitType) {
			// case READ:
			// ((PhysicalFileRecordParser)currentParser).setExitRef(exitRef);
			// break;

			// case WRITE:

			// break;
			// case LOOKUP:
			// ((LookupStepParser)currentStepParser).setExitRef(exitRef);
			// break;
			// case FORMAT:
			// break;
			// default:
			// break;
			// }
			// break;
			case "LogicalFile":
				logger.atFine().log("Logical Files");
				currentParser = new LogicalFileRecordParser();
				currentParser.setComponentID(Integer.parseInt(attributes.getValue(0)));
				break;
			// case "PartitionRef":
			// logger.atFine().log("Logical Files");
			// String a0 = elAttributes.getValue("seq");
			// String a1 = elAttributes.getValue("ID");
			// currentParser = new LogicalFileRecordParser();
			// currentParser.setComponentID(Integer.parseInt(attributes.getValue(0)));
			// break;
			case "LogicalRecord":
				logger.atFine().log("Logical Record");
				currentParser = new LRRecordParser();
				currentLRID = Integer.parseInt(attributes.getValue(0));
				currentParser.setComponentID(currentLRID);
				break;
			case "Field":
				logger.atFine().log("Logical Field");
				currentParser = new LRFieldRecordParser();
				currentParser.setComponentID(Integer.parseInt(attributes.getValue(0)));
				((LRFieldRecordParser) currentParser).setCurrentLrId(currentLRID);
				break;
			case "Index":
				logger.atFine().log("Index");
				currentParser = new LRIndexRecordParser();
				currentIndexID = Integer.parseInt(attributes.getValue("ID"));
				currentParser.setComponentID(currentIndexID);
				((LRIndexRecordParser) currentParser).setCurrentLrId(currentLRID);
				break;
			case "Lookup":
				logger.atFine().log("Lookup");
				currentParser = new LookupRecordParser();
				currentLookupID = Integer.parseInt(attributes.getValue(0));
				currentParser.setComponentID(currentLookupID);
				break;
			case "Exit":
				logger.atFine().log("Exit");
				currentParser = new ExitRecordParser();
				currentParser.setComponentID(Integer.parseInt(attributes.getValue("ID")));
				break;
			case "View":
				logger.atFine().log("View");
				currentParser = new ViewRecordParser();
				currentViewParser = (ViewRecordParser) currentParser;
				currentViewID = Integer.parseInt(attributes.getValue(0));
				currentParser.setComponentID(currentViewID);
				break;
			case "ControlRecordRef":
				if (Repository.isViewEnabled(currentViewID)) {
					logger.atFine().log("ControlRecordRef");
					currentViewParser.setContolRecord(Integer.parseInt(attributes.getValue(0)));
				}
				break;
			case "DataSource":
				if (Repository.isViewEnabled(currentViewID)) {
					logger.atFine().log("DataSource");
					currentParser = new ViewSourceRecordParser();
					currentViewSourceID = Integer.parseInt(attributes.getValue(0));
					currentParser.setComponentID(currentViewSourceID);
				}
				break;
			case "ColumnAssignment":
				// if (Repository.isViewEnabled(currentViewID)) {
				// 	logger.atFine().log("ColumnAssignment");
				// 	currentParser = new ViewColumnSourceParser();
				// 	currentParser.setComponentID(Integer.parseInt(attributes.getValue(0)));
				// 	((ViewColumnSourceParser) currentParser).setViewId(currentViewID);
				// 	((ViewColumnSourceParser) currentParser).setViewSourceId(currentViewSourceID);
				// }
				break;
			case "Extract":
				if (Repository.isViewEnabled(currentViewID)) {
					logger.atFine().log("Extract");
					currentParser = currentViewParser;
				}
				break;
			case "ExtractColumn":
				break;
			case "SortColumn":
				// if (Repository.isViewEnabled(currentViewID)) {
				// 	logger.atFine().log("SortColumn");
				// 	currentParser = new ViewSortKeyRecordParser();
				// 	currentParser.setComponentID(Integer.parseInt(attributes.getValue("ID")));
				// 	((ViewSortKeyRecordParser) currentParser).setViewId(currentViewID);
				// 	((ViewSortKeyRecordParser) currentParser).setSeqNum(Integer.parseInt(attributes.getValue("seq")));

				// }
				break;
			case "Output":
				if (Repository.isViewEnabled(currentViewID)) {
					logger.atFine().log("Output");
					fixupVCS();
					currentParser = currentViewParser;
				}
				break;
			case "FormatColumn":
				if (Repository.isViewEnabled(currentViewID)) {
					logger.atFine().log("FormatColumn");
					currentParser = new ViewColumnRecordParser();
					currentParser.setComponentID(Integer.parseInt(attributes.getValue("ID")));
				}
				break;
			default:
				if (currentParser != null) {
					currentParser.startElement(uri, localName, qName, attributes);
				}
				break;

		}
		data = new StringBuilder();
	}

	private void fixupVCS() {
		Repository.getViews().get(currentViewID).fixupVDPXMLColumns();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (currentParser != null) {
//			currentParser.addElement(qName, data.toString(), attributes);
		}

		if (qName.equalsIgnoreCase("SAFRVDP")) {
			System.out.println("End of VDP");
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		data.append(new String(ch, start, length));
	}
}
