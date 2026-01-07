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
import java.util.Iterator;

import org.genenaers.genevio.dbreader.DBTestHelper;
import org.genevaers.genevaio.dbreader.DBReader;
import org.genevaers.genevaio.yamlreader.LazyYAMLReader;
import org.genevaers.genevaio.yamlreader.LogicalRecordYAMLBean;
import org.genevaers.genevaio.yamlreader.YAMLEnvironmentReader;
import org.genevaers.genevaio.yamlreader.YAMLLogicalRecordReader;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.utilities.GersConfigration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestYAMLReader {


    // This test relies on checking out the test GenevaERS YAML repository

    private static final String GERS_TEST_ENV = "gersTestEnv";

    @Test @Disabled
    public void testMakeGenevaers() throws SQLException, ClassNotFoundException {
        YAMLEnvironmentReader envReader = new YAMLEnvironmentReader();
        envReader.makeEnvironment(GERS_TEST_ENV);
        envReader.queryAllEnvironments();
        String env = envReader.getEnvironment(GERS_TEST_ENV);
        assertNotNull(env);
    }
    
     @Test @Disabled
    public void testGenevaers() throws SQLException, ClassNotFoundException {
        YAMLEnvironmentReader envReader = new YAMLEnvironmentReader();
        envReader.queryAllEnvironments();
        String env = envReader.getEnvironment(GERS_TEST_ENV);
        assertNotNull(env);
    }

    @Test @Disabled
    public void testLReader() throws SQLException, ClassNotFoundException {
        YAMLLogicalRecordReader ylrr = new YAMLLogicalRecordReader();
        ylrr.setEnvironmentName(GERS_TEST_ENV);
        LogicalRecordYAMLBean lr = ylrr.getLogicalRecord(2);
        assertNotNull(lr);
    }

     @Test @Disabled
    public void testGetLR() throws SQLException, ClassNotFoundException {
        LazyYAMLReader lyr = new LazyYAMLReader();
        lyr.setEnvironmentName(GERS_TEST_ENV);
        LogicalRecord lr = lyr.getLogicalRecord(2);
        assertNotNull(lr);
        assertEquals("eventLR", lr.getName());
        LogicalFile lf = Repository.getLogicalFiles().get("lf");
        assertNotNull(lf);
        PhysicalFile tpf = Repository.getPhysicalFiles().get("pf");
        assertNotNull(tpf);
    }

    @Test @Disabled
    public void testGetIndexedLR() throws SQLException, ClassNotFoundException {
        LazyYAMLReader lyr = new LazyYAMLReader();
        lyr.setEnvironmentName(GERS_TEST_ENV);
        LogicalRecord lr = lyr.getLogicalRecord(3);
        assertNotNull(lr);
        assertEquals("targ", lr.getName());
        assertEquals(3, lr.getIteratorForIndexBySeq().next().getFieldID());
        Iterator<LRField> fi = lr.getIteratorForFieldsByName();
        assertEquals("ndx", fi.next().getName());
        assertEquals("payload", fi.next().getName());
    }

    @Test @Disabled
    public void testGetLookup() throws SQLException, ClassNotFoundException {
        LazyYAMLReader lyr = new LazyYAMLReader();
        lyr.setEnvironmentName(GERS_TEST_ENV);
        LookupPath lk = lyr.getLookup("lk");
        assertNotNull(lk);
        LogicalRecord targLr = Repository.getLogicalRecords().get("targ");
        assertNotNull(targLr);
        LRField f = targLr.findFromFieldsByName("payload");
        assertNotNull(f);
        LogicalFile lf = Repository.getLogicalFiles().get("targlf");
        assertNotNull(lf);
        PhysicalFile tpf = Repository.getPhysicalFiles().get("targPF");
        assertNotNull(tpf);
        assertTrue(lf.getNumberOfPFs() > 0);
        assertEquals(lf.getPFIterator().next().getName(), tpf.getName());
    }
}
