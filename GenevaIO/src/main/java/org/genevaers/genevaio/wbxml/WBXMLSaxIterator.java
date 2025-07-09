package org.genevaers.genevaio.wbxml;

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


import java.io.Reader;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.google.common.flogger.FluentLogger;

/*
	 * LOOKIE HERE !!!
	 * 
	 * Workbench XML was not created as means of generating a VDP.
	 * It provides a means of exporting and importing workbench data.
	 * Using as the source of a VDP presents some annoyances.
	 * Particulary in the area of PFs. 
	 * We can end up dragging in PFs that are not required in the VDP.
	 * Due to the many to many relationships between LFs and PFs.
	 * 
	 * The initial approach here was to only get the LFs (and hence PFs)
	 * required for view sources and lookup steps.
	 * But this misses the LF/PFs explicitly defined in a write statement.
	 * So we end up parsing all LFs. 
	 * (Which means we can forget the juggling done via view sources and lookup steps?)
	 * This means we can end up dragging in the unwanted.
	 * 
	 * C++ MR91 uses the solution of marking all PFs as not required.
	 * And then reversing that as they are need in SPO and when found in write logic
	 * or view outputs. Then at write time we only add the required to the VDP.
	 * 
	 * Wonder if we can just keep the XML tree and query it as needed?
	 * Too much of a memory hit? Is the current tree disposed off after reading?
	 * 
	 * Another argument for using files/language as input. 
	 * Then we can define what is required and only what is required.
	 * 
	 * For the moment trying the required flag method like C++.
	 */

public class WBXMLSaxIterator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private XMLStreamReader reader;

    private TreeMap<String, CatalogEntry> catalog = new TreeMap<>();

    private Reader xmlReader;

    public void addToRepsitory() {
        initXMLFactories();
        try {
            readWBXML();
        } catch (NumberFormatException | XMLStreamException e) {
            logger.atSevere().log("WBXMLSaxIterator readWBXML failed\n%s", e.getMessage());
        }
        dumpCatalog();
    }

    private void dumpCatalog() {
    }

    private void initXMLFactories() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        // https://rules.sonarsource.com/java/RSPEC-2755
        // prevent xxe
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);

        try {
           reader = xmlInputFactory.createXMLStreamReader(xmlReader);
        } catch (XMLStreamException e) {
            logger.atSevere().log("WBXMLSaxIterator readWBXML failed\n%s", e.getMessage());
        }
    }

    private void readWBXML() throws NumberFormatException, XMLStreamException {
        readTheRecords();
        fixupTheAssociations();
        RecordParserData.fixupEffectiveDateIndexes();
    }

    private void fixupTheAssociations() {
        RecordParserData.viewSourceFixups();
        RecordParserData.lookupfixups();
        RecordParserData.viewOutputFixups();
        RecordParserData.buildLFs();
    }

    private void readTheRecords() throws XMLStreamException {
        int eventType = reader.getEventType();
        while (reader.hasNext()) {

            eventType = reader.next();

            if (eventType == XMLEvent.START_ELEMENT) {
                String elementName = reader.getName().getLocalPart();
                switch (elementName) {
                    case "safrxml":
                        break;
                    case "Generation":
                        logger.atFine().log("Parsing Generation");
                        parseRecordsForElementWithParser(elementName, new GenerationParser());
                        break;
                    case "View":
                        logger.atFine().log("Parsing View");
                        parseRecordsForElementWithParser(elementName, new ViewRecordParser());
                        break;
                    case "View-Column":
                        logger.atFine().log("Parsing View-Column");
                        parseRecordsForElementWithParser(elementName, new ViewColumnRecordParser());
                        break;
                    case "View-Source":
                        logger.atFine().log("Parsing View-Source");
                        parseRecordsForElementWithParser(elementName, new ViewSourceRecordParser());
                        break;
                    case "View-Column-Source":
                        logger.atFine().log("Parsing View-Column-Source");
                        parseRecordsForElementWithParser(elementName, new ViewColumnSourceRecordParser());
                        break;
                    case "View-SortKey":
                        logger.atFine().log("Parsing View-SortKey");
                        parseRecordsForElementWithParser(elementName, new ViewSortKeyRecordParser());
                        break;
                    case "View-HeaderFooter":
                        logger.atFine().log("Parsing View-HeaderFooter");
                        parseRecordsForElementWithParser(elementName, new ViewHeaderFooterParser());
                        break;
                    case "Lookup":
                        logger.atFine().log("Parsing Lookup");
                        parseRecordsForElementWithParser(elementName, new LookupRecordParser());
                        break;
                    case "Lookup-Source-Key":
                        logger.atFine().log("Parsing Lookup-Source-Key");
                        parseRecordsForElementWithParser(elementName, new LookupSourceKeyRecordParser());
                        break;
                    case "Lookup-Step":
                        logger.atFine().log("Parsing Lookup-Step");
                        parseRecordsForElementWithParser(elementName, new LookupStepRecordParser());
                        break;
                    case "LogicalRecord":
                        logger.atFine().log("Parsing LogicalRecord");
                        parseRecordsForElementWithParser(elementName, new LRRecordParser());
                        break;
                    case "LRField":
                        logger.atFine().log("Parsing LRField");
                        parseRecordsForElementWithParser(elementName, new LRFieldRecordParser());
                        break;
                    case "LR-Field-Attribute":
                        logger.atFine().log("Parsing LR-Field-Attribute");
                        parseRecordsForElementWithParser(elementName, new LRFieldAttributeRecordParser());
                        break;
                    case "LR-Index":
                        logger.atFine().log("Parsing LR-Index");
                        parseRecordsForElementWithParser(elementName, new LRIndexRecordParser());
                        break;
                    case "LR-LF-Association":
                        logger.atFine().log("Parsing LR-LF-Association");
                        parseRecordsForElementWithParser(elementName, new LRLFAssocRecordParser());
                        break;
                    case "LR-IndexField":
                        logger.atFine().log("Parsing LR-IndexFiel");
                        parseRecordsForElementWithParser(elementName, new LRIndexFieldRecordParser());
                        break;
                    case "LogicalFile":
                        logger.atFine().log("Parsing LogicalFile");
                        parseRecordsForElementWithParser(elementName, new LogicalFileRecordParser());
                        break;
                    case "LF-PF-Association":
                        logger.atFine().log("Parsing LF-PF-Association");
                        parseRecordsForElementWithParser(elementName, new LFPFAssocRecordParser());
                        break;
                    case "PhysicalFile":
                        logger.atFine().log("Parsing PhysicalFile");
                        parseRecordsForElementWithParser(elementName, new PhysicalFileRecordParser());
                        break;
                    case "Exit":
                        logger.atFine().log("Parsing Exit");
                        parseRecordsForElementWithParser(elementName, new ExitRecordParser());
                        break;
                    case "ControlRecord":
                        logger.atFine().log("Parsing ControlRecord");
                        parseRecordsForElementWithParser(elementName, new CRRecordParser());
                        break;
                }

            }

            if (eventType == XMLEvent.END_ELEMENT) {
                if (reader.getName().getLocalPart().equals("safrxml")) {
                    logger.atInfo().log("WBXML Read");
                }
            }

        }
    }

    private void parseRecordsForElementWithParser(String elementName, RecordParser rp) throws XMLStreamException {
        addRecordsUsingParserForElementName(rp, elementName);
    }

    private void addRecordsUsingParserForElementName(RecordParser rp, String elementName) throws XMLStreamException {
        boolean record = false;
        boolean notdone = true;
        int eventType = reader.getEventType();
        while (notdone && reader.hasNext()) {

            eventType = reader.next();

            if (eventType == XMLEvent.START_ELEMENT) {
                String name = reader.getName().getLocalPart();
                switch (name) {
                    case "Record":
                        record = true;
                        break;
                    default:
                        if (record) {
                            rp.parseRecord(reader); // Calls derived class
                        }
                        break;
                }
            }

            if (eventType == XMLEvent.END_ELEMENT) {
                if (reader.getName().getLocalPart().equals("Record")) {
                    rp.endRecord();
                    addCatalogEntry(elementName, rp);
                    record = false;
                }
                if (reader.getName().getLocalPart().equals(elementName)) {
                    notdone = false;
                }
            }
        }
    }

    private void addCatalogEntry(String type, RecordParser parser) {
        CatalogEntry ce = new CatalogEntry();
        ce.setType(type);
        ce.setId(parser.getComponentID());
        ce.setName(parser.getComponentName());
        ce.setCreated(parser.getCreated());
        ce.setLastMod(parser.getLastMod());

        catalog.put(type + parser.getComponentID(), ce);
    }

    public Collection<CatalogEntry> getCatalogEntries() {
        return catalog.values();
    }

    public String getGenerationID() {
        return catalog.get("Generation0").getCreated();
    }

    public void setInputReader(Reader inputReader) {
        xmlReader = inputReader;
    }

}
