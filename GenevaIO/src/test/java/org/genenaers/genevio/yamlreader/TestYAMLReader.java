package org.genenaers.genevio.yamlreader;

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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.genenaers.genevio.dbreader.DBTestHelper;
import org.genevaers.genevaio.dbreader.DBReader;
import org.genevaers.genevaio.yamlreader.LazyYAMLReader;
import org.genevaers.genevaio.yamlreader.LogicalRecordYAMLBean;
import org.genevaers.genevaio.yamlreader.YAMLEnvironmentReader;
import org.genevaers.genevaio.yamlreader.YAMLLogicalRecordReader;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.utilities.GersConfigration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestYAMLReader {


    // This test relies on checking out the test GenevaERS YAML repository

    private static final String GERS_TEST_ENV = "gersTestEnv";

    @Test 
    public void testMakeGenevaers() throws SQLException, ClassNotFoundException {
        YAMLEnvironmentReader envReader = new YAMLEnvironmentReader();
        envReader.makeEnvironment(GERS_TEST_ENV);
        envReader.queryAllEnvironments();
        String env = envReader.getEnvironment(GERS_TEST_ENV);
        assertNotNull(env);
    }
    
     @Test 
    public void testGenevaers() throws SQLException, ClassNotFoundException {
        YAMLEnvironmentReader envReader = new YAMLEnvironmentReader();
        envReader.queryAllEnvironments();
        String env = envReader.getEnvironment(GERS_TEST_ENV);
        assertNotNull(env);
    }

    @Test 
    public void testLReader() throws SQLException, ClassNotFoundException {
        YAMLLogicalRecordReader ylrr = new YAMLLogicalRecordReader();
        ylrr.setEnvironmentName(GERS_TEST_ENV);
        LogicalRecordYAMLBean lr = ylrr.getLogicalRecord(2);
        assertNotNull(lr);
    }

     @Test 
    public void testGetLR() throws SQLException, ClassNotFoundException {
        LazyYAMLReader lyr = new LazyYAMLReader();
        lyr.setEnvironmentName(GERS_TEST_ENV);
        LogicalRecord lr = lyr.getLogicalRecord(2);
        assertNotNull(lr);
        assertEquals("eventLR", lr.getName());
    }

}
