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


import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import org.xml.sax.SAXException;

import com.google.common.flogger.FluentLogger;

public class VDPXMLSaxIterator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private BufferedInputStream inputBuffer;

    private TreeMap<String, CatalogEntry> catalog = new TreeMap<>();

    public void addToRepository() {
        initXMLFactoriesAndParse();
        dumpCatalog();
    }

    private void dumpCatalog() {
    }

    private void initXMLFactoriesAndParse() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            VDPHandler handler = new VDPHandler();
            saxParser.parse(inputBuffer, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.atSevere().log("initXMLFactoriesAndParse failes %s", e.getMessage());
        }
    }

    public Collection<CatalogEntry> getCatalogEntries() {
        return catalog.values();
    }

    public void setInputBuffer(BufferedInputStream ib) {
        inputBuffer = ib;
    }

    public String getGenerationID() {
        return "VDP XML generation id to come";
    }

}
