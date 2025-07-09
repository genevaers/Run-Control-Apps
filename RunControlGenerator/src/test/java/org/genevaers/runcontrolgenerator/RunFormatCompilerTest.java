package org.genevaers.runcontrolgenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Calendar;
import java.util.logging.Level;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.format.FormatAST2Dot;
import org.genevaers.compilers.format.astnodes.FormatBaseAST;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.wbxml.RecordParser;
import org.genevaers.repository.Repository;
import org.genevaers.repository.calculationstack.CalcStack;
import org.genevaers.repository.calculationstack.CalcStackEntry;
import org.genevaers.repository.calculationstack.CalcStack.CalcStackOpcode;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.runcontrolgenerator.compilers.FormatRecordsBuilder;
import org.genevaers.utilities.GenevaLog;
import org.genevaers.utilities.GersConfigration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
class RunFormatCompilerTest extends RunCompilerBase {

    @BeforeEach
    public void initEach(TestInfo info){
        FormatRecordsBuilder.reset();
        Repository.clearAndInitialise();
        FormatBaseAST.resetStack();
        Repository.setGenerationTime(Calendar.getInstance().getTime());
        RecordParser.clearAndInitialise();
        ExtractBaseAST.setCurrentColumnNumber((short)0);
        LtFactoryHolder.getLtFunctionCodeFactory().clearAccumulatorMap();
        java.nio.file.Path target = Paths.get("target/test-logs/");
        target.toFile().mkdirs();
        GersConfigration.clear();
        GersConfigration.initialise();
        GenevaLog.initLogger(RunFormatCompilerTest.class.getName(), target.resolve(info.getDisplayName()).toString(), Level.FINE);
    }

    @AfterEach
    public void afterEach(TestInfo info){
		GenevaLog.closeLogger(RunFormatCompilerTest.class.getName());
    }
    
    @Test
    void testFormatFilterGT() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_COMPILE, "");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 1, CalcStackOpcode.CalcStackPushNumber, "10");
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchLE, "128");
        assertStackEntry(cs, 4, CalcStackOpcode.CalcStackBranchAlways, "180");
    }

    @Test
    void testFormatFilterGE() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_COMPILE, "SELECTIF( COL.2  >= 10 )");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 1, CalcStackOpcode.CalcStackPushNumber, "10");
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchLT, "128");
        assertStackEntry(cs, 4, CalcStackOpcode.CalcStackBranchAlways, "180");
    }

    @Test
    void testFormatFilterEQ() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_COMPILE, "SELECTIF( COL.2  = 10 )");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 1, CalcStackOpcode.CalcStackPushNumber, "10");
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchNE, "128");
        assertStackEntry(cs, 4, CalcStackOpcode.CalcStackBranchAlways, "180");
    }

    @Test
    void testFormatFilterNE() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_COMPILE, "SELECTIF( COL.2  <> 10 )");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 1, CalcStackOpcode.CalcStackPushNumber, "10");
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchEQ, "128");
        assertStackEntry(cs, 4, CalcStackOpcode.CalcStackBranchAlways, "180");
    }

    @Test
    void testFormatFilterLE() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_COMPILE, "SELECTIF( COL.2  <= 10 )");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 1, CalcStackOpcode.CalcStackPushNumber, "10");
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchGT, "128");
        assertStackEntry(cs, 4, CalcStackOpcode.CalcStackBranchAlways, "180");
    }
    @Test
    void testFormatFilterLT() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_COMPILE, "SELECTIF( COL.2  < 10 )");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 1, CalcStackOpcode.CalcStackPushNumber, "10");
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchGE, "128");
        assertStackEntry(cs, 4, CalcStackOpcode.CalcStackBranchAlways, "180");
    }

    /*
     * Important point here is to check the exit from the first condition
     * Which has the opposite logic to the OR case following
     */
    @Test
    void testFormatFilterWithAND() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_AND_COMPILE, "");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchLE, "196");
        assertStackEntry(cs, 5, CalcStackOpcode.CalcStackBranchGE, "196");
    }

    /*
     * Important point here is to check the exit from the first condition
     * Which has the opposite logic to the AND case preceding
     */
    @Test
    void testFormatFilterWithOR() {
        FormatBaseAST ffTree = runFromXMLOverrideFormatFilter(12087, TestHelper.FORMAT_OR_COMPILE, "");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
        CalcStack cs = v.getFormatFilterCalcStack();
        assertStackEntry(cs, 2, CalcStackOpcode.CalcStackBranchGT, "136");
        assertStackEntry(cs, 5, CalcStackOpcode.CalcStackBranchGE, "196");
    }

    @Test
    void testColumnCalculation() {
        FormatBaseAST ffTree = runFromXMLOverrideColumnCalculation(12087, TestHelper.FORMAT_OR_COMPILE, 3, "");
        assertNotNull(ffTree);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
        ViewNode v = Repository.getViews().get(12087);
    }

    @Test
    void testColumnCalculationIfElse() {
        FormatBaseAST ffTree = runFromXMLOverrideColumnCalculation(12087, TestHelper.FORMAT_OR_COMPILE, 3, "IF col.2 > 10 AND col.2 < 100 THEN " +
            " COLUMN = COL.2 / 4 " +
            " ELSE COLUMN = COL.2 * 3 ENDIF ");
        assertNotNull(ffTree);
        ViewNode v = Repository.getViews().get(12087);
        assertEquals(14, v.getColumnNumber(3).getColumnCalculationStack().getNumEntries());
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 2, CalcStackOpcode.CalcStackBranchLE, "208");
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 5, CalcStackOpcode.CalcStackBranchGE, "208");
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 9, CalcStackOpcode.CalcStackBranchAlways, "272");
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
    }

    @Test
    void testColumnCalculationIfNoElse() {
        FormatBaseAST ffTree = runFromXMLOverrideColumnCalculation(12087, TestHelper.FORMAT_OR_COMPILE, 3, "IF col.2 > 10 AND col.2 < 100 THEN " +
            " COLUMN = COL.2 / 4 ENDIF");
        assertNotNull(ffTree);
        ViewNode v = Repository.getViews().get(12087);
        assertEquals(10, v.getColumnNumber(3).getColumnCalculationStack().getNumEntries());
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 2, CalcStackOpcode.CalcStackBranchLE, "200");
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 5, CalcStackOpcode.CalcStackBranchGE, "200");
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
    }

    @Test
    void testColumnCalculationNestedIf() {
        FormatBaseAST ffTree = runFromXMLOverrideColumnCalculation(12087, TestHelper.FORMAT_OR_COMPILE, 3, "IF col.2 > 10 AND col.2 < 100 THEN \n" +
            " IF Col.2 > 50 THEN COLUMN = COL.2 / 4 ELSE COLUMN = Col.2 - 33 ENDIF ENDIF");
        assertNotNull(ffTree);
        ViewNode v = Repository.getViews().get(12087);
        assertEquals(17, v.getColumnNumber(3).getColumnCalculationStack().getNumEntries());
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 2, CalcStackOpcode.CalcStackBranchLE, "340");
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 5, CalcStackOpcode.CalcStackBranchGE, "340");
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 8, CalcStackOpcode.CalcStackBranchLE, "276");
        assertStackEntry(v.getColumnNumber(3).getColumnCalculationStack(), 12, CalcStackOpcode.CalcStackBranchAlways, "340");
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
    }

    @Test
    void testColumnCalculationError() {
        FormatBaseAST ffTree = runFromXMLOverrideColumnCalculation(12087, TestHelper.FORMAT_OR_COMPILE, 3, "IF rubbish");
        assertNotNull(ffTree);
        assertTrue(Repository.newErrorsDetected());
        ViewNode v = Repository.getViews().get(12087);
        FormatAST2Dot.write(ffTree, Paths.get("target/ff.dot"));
    }


    private void assertStackEntry(CalcStack cs, int pos, CalcStackOpcode op, String val) {
        CalcStackEntry cse =  cs.getEntryAt(pos);
        assertEquals(op, cse.getOpCode());
        assertEquals(val, cse.getValue());
    }

}
