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

import java.io.Reader;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.genevaers.genevaio.wbxml.RecordParser;
import com.google.common.flogger.FluentLogger;

public class VDPXMLSaxIterator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private XMLStreamReader reader;
    private Reader xmlReader;

    private TreeMap<String, CatalogEntry> catalog = new TreeMap<>();
    
        private BaseParser currentParser;
    
        public void addToRepository() {
            initXMLFactoriesAndParse();
            try {
                readWBXML();
            } catch (NumberFormatException | XMLStreamException e) {
                logger.atSevere().log("VDPXMLSaxIterator read XML failed\n%s", e.getMessage());
            }
            dumpCatalog();
        }
    
        private void readWBXML() throws XMLStreamException {
            readTheRecords();
//            fixupTheAssociations();
//            RecordParserData.fixupEffectiveDateIndexes();
        }
    
        private void dumpCatalog() {
        }
    
        private void initXMLFactoriesAndParse() {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    
            // https://rules.sonarsource.com/java/RSPEC-2755
            // prevent xxe
            xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
    
            try {
                reader = xmlInputFactory.createXMLStreamReader(xmlReader);
            } catch (XMLStreamException e) {
                logger.atSevere().log("initXMLFactories failed %s", e.getMessage());
            }
    
            // try {
            //     //SAXParser saxParser = saxParserFactory.newSAXParser();
            //     reader = xmlInputFactory.createXMLStreamReader(xmlReader);
            //     //VDPHandler handler = new VDPHandler();
            // } catch (XMLStreamException e) {
            // }
    
            // XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    
            // xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            // xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
    
            // try {
            // VDPHandler handler = new VDPHandler();
            // reader = xmlInputFactory.createXMLStreamReader(xmlReader);
            // saxParser.parse(inputBuffer, handler);
            // } catch (ParserConfigurationException | SAXException | IOException e) {
            // logger.atSevere().log("initXMLFactoriesAndParse failes %s", e.getMessage());
            // }
        }
    
        public Collection<CatalogEntry> getCatalogEntries() {
            return catalog.values();
        }
    
        public void setInputReader(Reader inputReader) {
            xmlReader = inputReader;
        }
    
        public String getGenerationID() {
            return "VDP XML generation id to come";
        }
    
        private void readTheRecords() throws XMLStreamException {
            int eventType = reader.getEventType();
            while (reader.hasNext()) {
    
                eventType = reader.next();
    
                if (eventType == XMLEvent.START_ELEMENT) {
                    // System.out.println(eventType);
                    // System.out.println(reader.getName().getLocalPart());
                    String elementName = reader.getName().getLocalPart();
                    switch (elementName) {
                        case "ControlRecords":
                        logger.atFine().log("Parsing ControlRecords");
                        currentParser = new CRRecordParser();
                        currentParser.parse(reader);
                        logger.atFine().log("Parsing ControlRecords Completed");
                        //currentParser.setComponentID(Integer.parseInt(attributes.getValue(0)));
                    break;
                    case "Partitions":
                        logger.atFine().log("Parsing Partitions");
                        currentParser = new PhysicalFileRecordParser();
                        currentParser.parse(reader);
                        logger.atFine().log("Parsing Partitions Completed");
                    break;
                    case "LogicalFiles":
                        logger.atFine().log("Logical Files");
                        currentParser = new LogicalFileRecordParser();
                        currentParser.parse(reader);
                        logger.atFine().log("Parsing Logical Files Completed");
                    break;
                    case "LogicalRecords":
                        logger.atFine().log("Logical Record");
                        currentParser = new LRRecordParser();
                        currentParser.parse(reader);
//                        currentParser.setComponentID(currentLRID);
                        logger.atFine().log("Parsing Logical Records Completed");
                        break;
                     case "Lookups":
                        logger.atFine().log("Lookups");
                        currentParser = new LookupRecordParser();
                        currentParser.parse(reader);
                        logger.atFine().log("Parsing Lookups completed");
                        break;
                        case "Exits":
                        logger.atFine().log("Parsing Exit");
                        currentParser = new ExitRecordParser();
                        currentParser.parse(reader);
                        logger.atFine().log("Parsing Exits completed");
                        break;
                        case "Views":
                        logger.atFine().log("Parsing Views");
                        currentParser = new ViewRecordParser();
                        currentParser.parse(reader);
                        logger.atFine().log("Parsing Views completed");
                        break;
                                        // case "safrxml":
                    //     break;
                    // case "Generation":
                    //     logger.atFine().log("Parsing Generation");
                    //     parseRecordsForElementWithParser(elementName, new GenerationParser());
                    //     break;
                    // case "View":
                    //     logger.atFine().log("Parsing View");
                    //     parseRecordsForElementWithParser(elementName, new ViewRecordParser());
                    //     break;
                    // case "View-Column":
                    //     logger.atFine().log("Parsing View-Column");
                    //     parseRecordsForElementWithParser(elementName, new ViewColumnRecordParser());
                    //     break;
                    // case "View-Source":
                    //     logger.atFine().log("Parsing View-Source");
                    //     parseRecordsForElementWithParser(elementName, new ViewSourceRecordParser());
                    //     break;
                    // case "View-Column-Source":
                    //     logger.atFine().log("Parsing View-Column-Source");
                    //     parseRecordsForElementWithParser(elementName, new ViewColumnSourceRecordParser());
                    //     break;
                    // case "View-SortKey":
                    //     logger.atFine().log("Parsing View-SortKey");
                    //     parseRecordsForElementWithParser(elementName, new ViewSortKeyRecordParser());
                    //     break;
                    // case "View-HeaderFooter":
                    //     logger.atFine().log("Parsing View-HeaderFooter");
                    //     parseRecordsForElementWithParser(elementName, new ViewHeaderFooterParser());
                    //     break;
                    // case "Lookup":
                    //     logger.atFine().log("Parsing Lookup");
                    //     parseRecordsForElementWithParser(elementName, new LookupRecordParser());
                    //     break;
                    // case "Lookup-Source-Key":
                    //     logger.atFine().log("Parsing Lookup-Source-Key");
                    //     parseRecordsForElementWithParser(elementName, new LookupSourceKeyRecordParser());
                    //     break;
                    // case "Lookup-Step":
                    //     logger.atFine().log("Parsing Lookup-Step");
                    //     parseRecordsForElementWithParser(elementName, new LookupStepRecordParser());
                    //     break;
                    // case "LogicalRecord":
                    //     logger.atFine().log("Parsing LogicalRecord");
                    //     parseRecordsForElementWithParser(elementName, new LRRecordParser());
                    //     break;
                    // case "LRField":
                    //     logger.atFine().log("Parsing LRField");
                    //     parseRecordsForElementWithParser(elementName, new LRFieldRecordParser());
                    //     break;
                    // case "LR-Field-Attribute":
                    //     logger.atFine().log("Parsing LR-Field-Attribute");
                    //     parseRecordsForElementWithParser(elementName, new LRFieldAttributeRecordParser());
                    //     break;
                    // case "LR-Index":
                    //     logger.atFine().log("Parsing LR-Index");
                    //     parseRecordsForElementWithParser(elementName, new LRIndexRecordParser());
                    //     break;
                    // case "LR-LF-Association":
                    //     logger.atFine().log("Parsing LR-LF-Association");
                    //     parseRecordsForElementWithParser(elementName, new LRLFAssocRecordParser());
                    //     break;
                    // case "LR-IndexField":
                    //     logger.atFine().log("Parsing LR-IndexFiel");
                    //     parseRecordsForElementWithParser(elementName, new LRIndexFieldRecordParser());
                    //     break;
                    // case "LogicalFile":
                    //     logger.atFine().log("Parsing LogicalFile");
                    //     parseRecordsForElementWithParser(elementName, new LogicalFileRecordParser());
                    //     break;
                    // case "LF-PF-Association":
                    //     logger.atFine().log("Parsing LF-PF-Association");
                    //     parseRecordsForElementWithParser(elementName, new LFPFAssocRecordParser());
                    //     break;
                    // case "PhysicalFile":
                    //     logger.atFine().log("Parsing PhysicalFile");
                    //     parseRecordsForElementWithParser(elementName, new PhysicalFileRecordParser());
                    //     break;
                    // case "ControlRecord":
                    //     logger.atFine().log("Parsing ControlRecord");
                    //     parseRecordsForElementWithParser(elementName, new CRRecordParser());
                    //     break;
                }

            }

            if (eventType == XMLEvent.END_ELEMENT) {
                // System.out.println(reader.getName().getLocalPart());
                // if </staff>
                if (reader.getName().getLocalPart().equals("safrxml")) {
                    logger.atInfo().log("VDP XML read");
                }
            }

        }
    }


private void parseRecordsForElementWithParser(String elementName, BaseParser rp) throws XMLStreamException {
    addRecordsUsingParserForElementName(rp, elementName);
}

private void addRecordsUsingParserForElementName(BaseParser bp, String elementName) throws XMLStreamException {
    boolean record = false;
    boolean notdone = true;
    int eventType = reader.getEventType();
    while (notdone && reader.hasNext()) {

        eventType = reader.next();

        if (eventType == XMLEvent.START_ELEMENT) {
            String name = reader.getName().getLocalPart();

            //bp.addElement(elementName, name);
            //parseRecord(reader); // Calls derived class
        }

        if (eventType == XMLEvent.END_ELEMENT) {
            System.out.println("End Element " + reader.getName().getLocalPart());
            // if </staff>
            if (reader.getName().getLocalPart().equals("Record")) {
//                rp.endRecord();
//                addCatalogEntry(elementName, rp);
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

}

