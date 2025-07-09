package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.genevaers.repository.RepoHelper;

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
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.enums.AccessMethod;
import org.genevaers.repository.components.enums.DbmsRowFmtOptId;
import org.genevaers.repository.components.enums.FieldDelimiter;
import org.genevaers.repository.components.enums.FileRecfm;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.RecordDelimiter;
import org.genevaers.repository.components.enums.TextDelimiter;
import org.xml.sax.Attributes;

/**
 * This class will parse a PhysicalFile Record element into a
 * PhysicalFileTransfer object.
 */
public class PhysicalFileRecordParser extends BaseParser {

	private PhysicalFile pf;

	public PhysicalFileRecordParser() {
		sectionName = "Partitions";
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		switch (qName.toUpperCase()) {
			case "PARTITION":
				componentID = Integer.parseInt(attributes.getValue(0));
				break;
			case "EXITREF":
				pf.setReadExitID(Integer.parseInt(attributes.getValue("ID")));
				break;
				default:
				break;
		}
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "PARTITION":
				componentID = Integer.parseInt(attributes.get("ID"));
				break;
			case "NAME":
				pf = new PhysicalFile();
				pf.setComponentId(componentID);
				pf.setExtractDDName("");
				pf.setDatabase("");
				pf.setFieldDelimiter(FieldDelimiter.FIXEDWIDTH);
				pf.setRecordDelimiter(RecordDelimiter.CR);
				pf.setTextDelimiter(TextDelimiter.INVALID);
				pf.setDatabaseRowFormat(DbmsRowFmtOptId.NONE);
				pf.setRecfm(FileRecfm.VB);
				pf.setName(text);
				pf.setFileType(FileType.DISK); //default
				pf.setAccessMethod(AccessMethod.SEQUENTIAL); //default
				//RepoHelper.fillPF(pf);
				Repository.getPhysicalFiles().add(pf, componentID, text);
				break;
			case "PARTITIONTYPE":
				pf.setFileType(FileType.fromdbcode(text));
				break;
			case "ACCESSMETHOD":
				pf.setAccessMethod(AccessMethod.fromdbcode(text.trim()));
				break;
			case "READEXITSTARTUP":
				pf.setReadExitIDParm(text);
				break;
			case "DDNAMEINPUT":
				pf.setInputDDName(text.trim());
				break;
			case "DSN":
				if (text.trim().length() > 0) {
					pf.setDataSetName(text.trim());
				} else {
					pf.setDataSetName("");
				}
				break;
			case "MINRECORDLENGTH":
				short s = (short) Integer.parseInt(text);
				pf.setMinimumLength(s);
				break;
			case "MAXRECLEN":
				s = (short) Integer.parseInt(text);
				pf.setMaximumLength(s);
				break;
			case "DDNAMEOUTPUT":
				if (text.trim().length() > 0) {
					pf.setOutputDDName(text.trim());
				} else {
					pf.setOutputDDName(String.format("O%07d", componentID));
				}
				break;
			case "RECFM":
				pf.setRecfm(FileRecfm.fromdbcode(text));
				break;
			case "LRECL":
				s = (short) Integer.parseInt(text);
				pf.setLrecl(s);
				break;
			case "CONNECTION":
				pf.setDatabaseConnection(text.trim());
				break;
			case "QUERY":
				//may be many segments we need to append them
				String sql = pf.getSqlText();
				sql = sql + removeBRLineEndings(text.trim());
				pf.setSqlText(sql);
				break;
			case "TABLE":
				pf.setDatabaseTable(text.trim());
				break;
			case "ROWFORMAT":
				pf.setDatabaseRowFormat(DbmsRowFmtOptId.fromdbcode(text.trim()));
				break;
			case "INCLUDENULLS":
				pf.setIncludeNulls(text.equals("1") ? true : false);
				break;
			case "EXITREF":
				pf.setReadExitID(Integer.parseInt(attributes.get("ID")));
				break;
			case "CREATEDTIMESTAMP":
				created = text;
				break;
			case "ModifiedTimestamp":
				lastMod = text;
				break;
			default:
				break;
		}
	}

	public void setExitRef(int exitRef) {
		pf.setReadExitID(exitRef);
	}
}
