package org.genevaers.genevaio.wbxml;

import java.util.Map;

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

/**
 * This class will parse a File-Partition Record element into a
 * FileAssociationTransfer object.
 */
public class LFPFAssocRecordParser extends RecordParser {

	private int pfid;
	private String seqNum;

	private int assocId;

	@Override
	public void parseRecord(XMLStreamReader reader) throws XMLStreamException {
		String part = reader.getName().getLocalPart();
		int n = reader.next();
		if (n == XMLEvent.CHARACTERS) {
			String text = reader.getText();
			switch (part) {
				case "LFPFASSOCID":
					assocId = Integer.parseInt(text);
					break;
				case "PHYFILEID":
					pfid = Integer.parseInt(text);
					break;
				case "PARTSEQNBR":
					seqNum = text;
					break;
				case "LOGFILEID":
					LFPF lfpf = new LFPF();
					lfpf.lfid = Integer.parseInt(text);
					lfpf.pfid = pfid;
					lfpf.seq = Integer.parseInt(seqNum);

					Map<Integer, LFPF> lfseq = RecordParserData.addOrGetLfSeqMap(lfpf.lfid);
					lfseq.put(lfpf.seq , lfpf);
					RecordParserData.lfpfs.put(assocId, lfpf);
					break;
				default:
					break;
			}
		}
	}
}
