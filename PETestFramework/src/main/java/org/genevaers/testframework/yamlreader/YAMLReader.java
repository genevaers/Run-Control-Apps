package org.genevaers.testframework.yamlreader;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.flogger.FluentLogger;

import org.genevaers.utilities.GenevaLog;

public class YAMLReader {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static ObjectMapper mapper;

    public static void main(String[] args) {

         try {
            YAMLReader yr = new YAMLReader(); 
            Spec spec = yr.readSpec("XMLConverter/PeTests/Assignment/BIGASS.yaml");
            logger.atInfo().log("Read %s", spec.getName());
            SpecFiles specs = yr.readSpecFileList(new File("fmspeclist.yaml"));
            logger.atInfo().log("Read %s", specs.getName());
        } catch (IOException e) {
            logger.atSevere().log("Error while reading YAML: \n%s", e.getMessage());
        }

    }
    public YAMLReader() {
        GenevaLog.initLoggerWithColours("YAML Reader", Level.FINE);
        logger.atConfig().log("YAML Reader");
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
    }

    public Spec readSpec(String path) throws StreamReadException, DatabindException, IOException {
            return mapper.readValue(new File(path), Spec.class);
    }

    public SpecFileSets readSpecFileSets(File specFileSets) throws StreamReadException, DatabindException, IOException {
        return mapper.readValue(specFileSets, SpecFileSets.class);
    }


    public SpecFiles readSpecFileList(File specFileList) throws StreamReadException, DatabindException, IOException {
        return mapper.readValue(specFileList, SpecFiles.class);
    }

    public TemplateSet yaml2TemplateSet(File tempSet) throws StreamReadException, DatabindException, IOException {
        return mapper.readValue(tempSet, TemplateSet.class);
    }

    public PassViews readPassViews(File file) throws StreamReadException, DatabindException, IOException {
        return mapper.readValue(file, PassViews.class);
    }

}
