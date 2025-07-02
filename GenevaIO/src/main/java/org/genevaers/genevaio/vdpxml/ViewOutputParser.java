package org.genevaers.genevaio.vdpxml;

import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewDefinition;

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

import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.OutputMedia;

public class ViewOutputParser extends BaseParser {

	private ViewNode viewNode;
	private Integer pid;
	private Integer prefid;
	private ViewDefinition vd;
	private String pfname;

	public ViewOutputParser() {
		sectionName = "Output";
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) throws XMLStreamException {
		switch (name.toUpperCase()) {
			case "MEDIA":
				vd.setOutputMedia(OutputMedia.fromdbcode(text.trim()));
				viewNode.getOutputFile().setFileType(FileType.DISK);;
				break;
			case "PARTITION":
				pid = Integer.parseInt(attributes.get("ID"));
				generateExtractOutputLogic(pid);
				break;
			case "PARTITIONREF":
				prefid = Integer.parseInt(attributes.get("ID"));
				generateExtractOutputLogic(prefid);
				break;
			case "NAME":
				pfname = text;
				viewNode.getOutputFile().setName(text);
				break;
			case "DDNAMEOUTPUT":
				pfname = text;
				viewNode.getOutputFile().setOutputDDName(text);
				break;
			case "EXITREF":
				int exitID = Integer.parseInt(attributes.get("ID"));
				viewNode.getViewDefinition().setFormatExitId(exitID);
				break;
				case "PARAMETER":
				viewNode.getViewDefinition().setFormatExitParams(text);;
				break;

			case "HEADERS":
				logger.atFine().log("Parsing Headers for %s", viewNode.getViewDefinition().getName());
				ViewHeaderFooterParser hfp = new ViewHeaderFooterParser();
				hfp.setViewNode(viewNode);
				hfp.parse(reader);
				logger.atFine().log("Parsing Headers completed");
				break;
			case "FORMATCOLUMNS":
				logger.atFine().log("Parsing Format Columns");
				ViewFormatColumnParser fcp = new ViewFormatColumnParser();
				fcp.setViewNode(viewNode);
				fcp.parse(reader);
				logger.atFine().log("Parsing Format completed");
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
				viewNode.setFormatFilterLogic(removeBRLineEndings(text));
				break;
			default:
				break;
		}
	}

	public void setViewNode(ViewNode vn) {
		this.viewNode = vn;
		vd = viewNode.getViewDefinition();
		vn.fixupVDPXMLColumns();
	}

	private void generateExtractOutputLogicWithWrite(int exitID) {
		if (pid != 0) {
			generateExtractOutputLogic(pid);
		} else if (prefid != 0) {
			generateExtractOutputLogic(prefid);
		} else {
			// In trouble here
		}
	}

	private void generateExtractOutputLogic(int id) {
		if (viewNode != null) {
			String writeLogic = "";
			switch (viewNode.getViewDefinition().getViewType()) {
				case COPY:
					if (id > 0) {
						writeLogic = String.format("WRITE(SOURCE=INPUT, %s", getFileParm(viewNode, id));
					} else {
						writeLogic = "WRITE(SOURCE=INPUT,DEST=DEFAULT)";
					}
					break;
				case SUMMARY:
				case DETAIL:
					writeLogic = String.format("WRITE(SOURCE=VIEW,DEST=EXT=%03d",
							viewNode.getViewDefinition().getExtractFileNumber());
					writeLogic += getWriteParm(viewNode) + ")";
					break;
				case EXTRACT:
					if (viewNode.getOutputFile().getComponentId() > 0) {
						writeLogic = String.format("WRITE(SOURCE=DATA, %s)", getFileParm(viewNode, id));
					} else if (Repository.getPhysicalFiles().get(id) != null) {
						writeLogic = String.format("WRITE(SOURCE=DATA, %s)", getFileParm(viewNode, id));
					} else {
						writeLogic = "WRITE(SOURCE=DATA,DEST=DEFAULT)";
					}
					break;
				default:
					// Error case
					break;

			}
			Iterator<ViewSource> vsi = viewNode.getViewSourceIterator();
			while (vsi.hasNext()) {
				ViewSource vs = vsi.next();
				vs.setExtractOutputLogic(writeLogic);
			}
		}
	}

	private String getWriteParm(ViewNode vn) {
		String exitStr = "";
		int exitID = vn.getViewDefinition().getWriteExitId();
		if (exitID != 0) {
			UserExit ex = Repository.getUserExits().get(exitID);
			String wparms = vn.getViewDefinition().getWriteExitParams();
			if (wparms.length() > 0) {
				exitStr += String.format(",USEREXIT=({%s, \"%s\"})", ex.getName(), wparms);
			} else {
				exitStr += String.format(",USEREXIT={%s}", ex.getName());
			}
		}
		return exitStr;
	}

	private String getFileParm(ViewNode vn, int partId) {
		String fileStr;
		PhysicalFile pf = Repository.getPhysicalFiles().get(partId);
		fileStr = "DEST=FILE={" + pf.getLogicalFilename() + "." + pf.getName() + "}" + getWriteParm(vn);
		return fileStr;
	}

}
