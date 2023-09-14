package org.genevaers.genevaio.wbxml;

import java.io.InputStream;
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

    private InputStream inputStream;
    private XMLStreamReader reader;

    private TreeMap<String, CatalogEntry> catalog = new TreeMap<>();

    public void inputFrom(InputStream is) {
        inputStream = is;
    }

    public void addToRepsitory() {
        initXMLFactories();
        try {
            readWBXML();
        } catch (NumberFormatException | XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        try {
            reader = xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readWBXML() throws NumberFormatException, XMLStreamException {
        readTheRecords();
        fixupTheAssociations();
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
                // System.out.println(eventType);
                // System.out.println(reader.getName().getLocalPart());
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
                        logger.atFine().log("Parsing View-Column-Sourc");
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
                // System.out.println(reader.getName().getLocalPart());
                // if </staff>
                if (reader.getName().getLocalPart().equals("safrxml")) {
                    logger.atInfo().log("All Done");
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
                // System.out.println(reader.getName().getLocalPart());
                // if </staff>
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

}
