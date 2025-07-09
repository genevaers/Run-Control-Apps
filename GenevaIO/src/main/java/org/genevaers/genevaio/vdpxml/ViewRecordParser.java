package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
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
import org.genevaers.repository.components.OutputFile;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.OutputMedia;
import org.genevaers.repository.components.enums.ViewStatus;
import org.genevaers.repository.components.enums.ViewType;
import org.xml.sax.Attributes;

public class ViewRecordParser extends BaseParser {

	private ViewDefinition vd;
	private ViewNode vn;
	private OutputFile outfile;
	private int exitID;
	private int prefid;
	private int pid;

	public ViewRecordParser() {
		sectionName = "Views";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) throws XMLStreamException {
		switch (name.toUpperCase()) {
			case "VIEW":
				componentID = Integer.parseInt(attributes.get("ID"));
				break;
			case "NAME":
				logger.atFine().log("Parsing View " + text);
				vd = new ViewDefinition();
				vd.setComponentId(componentID);
				if (Repository.isViewEnabled(componentID)) {
					vn = Repository.getViewNodeMakeIfDoesNotExist(vd);
				}
				vd.setName(text);
				break;
			case "CONTROLRECORDREF":
				vd.setControlRecordId(Integer.parseInt(attributes.get("ID")));
				break;
			case "DATASOURCES":
				logger.atFine().log("Parsing View Sources");
				ViewSourceRecordParser vsp = new ViewSourceRecordParser();
				vsp.setViewNode(vn);
				vsp.parse(reader);
				logger.atFine().log("Parsing View Sources completed");
				break;
			case "EXTRACT":
				logger.atFine().log("Parsing View Extract");
				ViewExtractParser vep = new ViewExtractParser();
				vep.setViewNode(vn);
				vep.parse(reader);
				logger.atFine().log("Parsing View Extract completed");
				break;
				case "SORT":
				logger.atFine().log("Parsing Sort Keys");
				ViewSortKeyRecordParser vskp = new ViewSortKeyRecordParser();
				vskp.setViewNode(vn);
				vskp.parse(reader);
				logger.atFine().log("Parsing Sort Keys completed");
				break;
				case "OUTPUT":
				logger.atFine().log("Parsing Output");
				ViewOutputParser vop = new ViewOutputParser();
				vop.setViewNode(vn);
				vop.parse(reader);
				logger.atFine().log("Parsing Output completed");
				break;
			case "STATUS":
				vd.setStatus(ViewStatus.fromdbcode(text.trim()));
				break;

			case "TYPE":
				vd.setViewType(ViewType.fromdbcode(text.trim()));
				break;

			case "FILENUMBER":
				short efn = (short) Integer.parseInt(text.trim());
				vd.setExtractFileNumber(efn);
				break;

			case "MEDIA":
				vd.setOutputMedia(OutputMedia.fromdbcode(text.trim()));
				break;

			case "OUTPUTLRID":
				vd.setOutputLrId(Integer.parseInt(text.trim()));
				break;

			case "LFPFASSOCID":
				RecordParserData.viewOutlfpf.put(componentID, Integer.parseInt(text.trim()));
				break;

			case "LINESPERPAGE":
				vd.setOutputPageSizeMax((short) Integer.parseInt(text.trim()));
				break;
			case "MAXCHARSPERLINE":
				vd.setOutputLineSizeMax((short) Integer.parseInt(text.trim()));
				break;
			case "ZEROSUPPRESSIND":
				vd.setZeroValueRecordSuppression(text.equals("0") ? false : true);
				break;
			case "EXTRACTMAXRECCNT":
				vd.setExtractMaxRecCount(Integer.parseInt(text.trim()));
				break;
			case "ENABLEAGGREGATION":
				vd.setExtractSummarized(text.equals("0") ? false : true);
				break;
			case "AGGREGATIONRECORDS":
				vd.setMaxExtractSummaryRecords(Integer.parseInt(text.trim()));
				break;
			case "OUTPUTMAXRECCNT":
				vd.setOutputMaxRecCount(Integer.parseInt(text.trim()));
				break;
			case "WRITEEXIT":
				vd.setWriteExitId(Integer.parseInt(text.trim()));
				break;
			case "WRITEEXITSTARTUP":
				vd.setWriteExitParams(text.trim());
				break;
			case "FORMATEXITID":
				vd.setFormatExitId(Integer.parseInt(text.trim()));
				break;
			case "FORMATEXITSTARTUP":
				vd.setFormatExitParams(text.trim());
				break;
			case "DELIMHEADERROWIND":
				vd.setGenerateDelimitedHeader(text.equals("0") ? false : true);
				break;
			case "FORMATFILTLOGIC":
				if (Repository.isViewEnabled(componentID)) {
					vn.setFormatFilterLogic(removeBRLineEndings(text));
				}
				break;
			case "OWNERUSER":
				vd.setOwnerUser(text);
				break;

			default:
				// logger.atInfo().log(reader.getText());
				break;
		}
	}

	public void setContolRecord(int crid) {
		vd.setControlRecordId(crid);
	}

}
