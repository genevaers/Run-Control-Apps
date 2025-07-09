package org.genevaers.genevaio.vdpxml;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LookupPathKey;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.flogger.FluentLogger;

// import com.ibm.safr.we.constants.DateFormat;
// import com.ibm.safr.we.data.transfer.SAFRTransfer;
// import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This abstract class represent a parser which converts Record XML
 * elements into component transfer objects. It defines one public method which
 * provides the algorithm for retrieving the elements, parsing them and
 * returning the transfer objects. Concrete subclasses must implement two
 * abstract methods which define component type-specific behavior.
 */
abstract public class BaseParser {
	public static final FluentLogger logger = FluentLogger.forEnclosingClass();

	protected int componentID;
	protected String componentName;
	protected String created;
	protected String lastMod;
	protected String elementName;
	protected static TreeMap<String, CatalogEntry> catalog;

	protected String sectionName;

	protected static Map<Integer, LRIndex> effdateStarts = new HashMap<>();
	protected static Map<Integer, LRIndex> effdateEnds = new HashMap<>();

	protected Map<String, String> attributes = new HashMap<>();

	protected XMLStreamReader reader;

	public BaseParser() {
	}

	public static String removeBRLineEndings(String str) {
		if (str == null) {
			return null;
		}
		return str.replaceAll("(?i)\\<BR\\/\\>", "");
	}

	public String getCreated() {
		return created;
	}

	public String getLastMod() {
		return lastMod;
	}

	public int getComponentID() {
		return componentID;
	}

	public void setComponentID(int componentID) {
		this.componentID = componentID;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	abstract public void addElement(String name, String text, Map<String, String> attributes)  throws XMLStreamException ;

	public void endRecord() {
		// override for special handling
	}

	public static void clearAndInitialise() {
		// protected static TreeMap<String, CatalogEntry> catalog;

		effdateStarts = new HashMap<>();
		effdateEnds = new HashMap<>();

		RecordParserData.clearAndInitialise();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
	}

	public void endElement() {}

	public void parse(XMLStreamReader reader) throws XMLStreamException {
		boolean notdone = true;
		this.reader = reader;
		int eventType = reader.getEventType();
		while (notdone && reader.hasNext()) {

			eventType = reader.next();

			if (eventType == XMLEvent.START_ELEMENT) {
				String elementName = reader.getName().getLocalPart();
				getAttributes(reader);
				int n = reader.next();
				if (n == XMLEvent.CHARACTERS) {
					String text = reader.getText();
					addElement(elementName, text, attributes);
				} else {
					addElement(elementName, "", attributes);
				}
			}

			if (eventType == XMLEvent.END_ELEMENT) {
				String elementName = reader.getName().getLocalPart();
				if (elementName.equals(sectionName)) {
					// Time to return to top level
					endElement();
					notdone = false;
				}
			}
		}

	}

	private void getAttributes(XMLStreamReader reader) {
		attributes.clear();
		for (int i=0; i<reader.getAttributeCount(); i++ ) {
			attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i)) ;
		}
	}
}

