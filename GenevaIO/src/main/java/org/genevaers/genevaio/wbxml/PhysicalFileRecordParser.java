package org.genevaers.genevaio.wbxml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

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

/**
 * This class will parse a PhysicalFile Record element into a
 * PhysicalFileTransfer object.
 */
public class PhysicalFileRecordParser extends RecordParser {

	private PhysicalFile pf;

	@Override
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "PHYFILEID":
					pf = new PhysicalFile();
					componentID = Integer.parseInt(text);
					pf.setComponentId(componentID);
					pf.setExtractDDName("");
					pf.setDatabase("");
					pf.setFieldDelimiter(FieldDelimiter.FIXEDWIDTH);
					pf.setRecordDelimiter(RecordDelimiter.CR);
					pf.setTextDelimiter(TextDelimiter.INVALID);
					pf.setDatabaseRowFormat(DbmsRowFmtOptId.DB2);
					pf.setRecfm(FileRecfm.VB);
					break;
				case "NAME":
					pf.setName(text);
					Repository.getPhysicalFiles().add(pf, componentID, text);
					break;
				case "FILETYPECD":
					pf.setFileType(FileType.fromdbcode(text));
					break;
				case "ACCESSMETHODCD":
					// Not sure what this one is
					// fieldValue = parseField("DISKFILETYPECD", record);
					pf.setAccessMethod(AccessMethod.fromdbcode(text.trim()));
					break;
				case "READEXITID":
					// translate old access codes
					// if (fieldValue.equals("EXCP ") || fieldValue.equals("SEQEX")) {
					// fieldValue = "SEQIN";
					// }
					pf.setReadExitID(Integer.parseInt(text));
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
				case "MINRECLEN":
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
				case "DBMSSUBSYS":
					pf.setDatabaseConnection(text.trim());
					break;
				case "DBMSSQL":
					pf.setSqlText(removeBRLineEndings(text.trim()));
					break;
				case "DBMSTABLE":
					pf.setDatabaseTable(text.trim());
					break;
				case "DBMSROWFMTCD":
					pf.setDatabaseRowFormat(DbmsRowFmtOptId.fromdbcode(text.trim()));
					break;
				case "DBMSINCLNULLSIND":
					pf.setIncludeNulls(text.equals("1") ? true : false);
					break;
				case "CREATEDTIMESTAMP":
					created = text;
					break;
				case "LASTMODTIMESTAMP":
					lastMod = text;
					break;
				default:
					break;
			}
		}
	}
}
