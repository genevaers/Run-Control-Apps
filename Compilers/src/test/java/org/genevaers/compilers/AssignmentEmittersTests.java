package org.genevaers.compilers;

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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.DTColumnAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.compilers.extract.emitters.LogicTableEmitter;
import org.genevaers.compilers.extract.emitters.assignmentemitters.AssignmentConstEmitter;
import org.genevaers.compilers.extract.emitters.assignmentemitters.AssignmentEmitter;
import org.genevaers.compilers.extract.emitters.assignmentemitters.AssignmentEmitterFactory;
import org.genevaers.compilers.extract.emitters.assignmentemitters.AssignmentFieldEmitter;
import org.genevaers.compilers.extract.emitters.assignmentemitters.AssignmentRulesChecker;
import org.genevaers.compilers.extract.emitters.assignmentemitters.AssignmentRulesCheckerFactory;
import org.genevaers.compilers.extract.emitters.assignmentemitters.DateChecker;
import org.genevaers.compilers.extract.emitters.assignmentemitters.FlipColumnChecker;
import org.genevaers.compilers.extract.emitters.assignmentemitters.FlipDataChecker;
import org.genevaers.compilers.extract.emitters.assignmentemitters.SameTypeChecker;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;
import org.junit.jupiter.api.Test;

public class AssignmentEmittersTests {

    @Test
	public void testALPHANUMERICsDataChecker() {
        ViewColumn vc = new ViewColumn();
        vc.setDataType(DataType.ALPHANUMERIC);
        ColumnAST cast = new DTColumnAST(vc);
        cast.setViewColumn(vc);
        FieldReferenceAST fldast = new FieldReferenceAST();
        LRField fld = new LRField();
        fld.setDatatype(DataType.ALPHANUMERIC);
        fldast.setRef(fld);
		AssignmentRulesChecker dc = AssignmentRulesCheckerFactory.getChecker(cast, fldast);
        assertTrue(dc instanceof SameTypeChecker);
	}

    @Test
	public void testAl2NumtDataChecker() {
        ViewColumn vc = new ViewColumn();
        vc.setDataType(DataType.ALPHANUMERIC);
        ColumnAST cast = new DTColumnAST(vc);
        cast.setViewColumn(vc);
        FieldReferenceAST fldast = new FieldReferenceAST();
        LRField fld = new LRField();
        fld.setDatatype(DataType.PACKED);
        fldast.setRef(fld);
		AssignmentRulesChecker dc = AssignmentRulesCheckerFactory.getChecker(cast, fldast);
        assertTrue(dc instanceof FlipColumnChecker);
	}

    @Test
	public void testNum2NumtDataChecker() {
        ViewColumn vc = new ViewColumn();
        vc.setDataType(DataType.BINARY);
        ColumnAST cast = new DTColumnAST(vc);
        cast.setViewColumn(vc);
        FieldReferenceAST fldast = new FieldReferenceAST();
        LRField fld = new LRField();
        fld.setDatatype(DataType.PACKED);
        fldast.setRef(fld);
		AssignmentRulesChecker dc = AssignmentRulesCheckerFactory.getChecker(cast, fldast);
        assertTrue(dc instanceof FlipDataChecker);
	}

    @Test
	public void testNum2AlDataChecker() {
        ViewColumn vc = new ViewColumn();
        vc.setDataType(DataType.BINARY);
        ColumnAST cast = new DTColumnAST(vc);
        cast.setViewColumn(vc);
        FieldReferenceAST fldast = new FieldReferenceAST();
        LRField fld = new LRField();
        fld.setDatatype(DataType.ALPHANUMERIC);
        fldast.setRef(fld);
		AssignmentRulesChecker dc = AssignmentRulesCheckerFactory.getChecker(cast, fldast);
        assertTrue(dc instanceof FlipDataChecker);
	}

    @Test
	public void testAssignmentFieldEmitter() {
        FieldReferenceAST fldast = new FieldReferenceAST();
        AssignmentEmitter ae = AssignmentEmitterFactory.getAssignmentEmitter(fldast);
        assertTrue(ae instanceof AssignmentFieldEmitter);
	}

    @Test
	public void testAssignmentStringConstEmitter() {
        StringAtomAST strast = new StringAtomAST();
        AssignmentEmitter ae = AssignmentEmitterFactory.getAssignmentEmitter(strast);
        assertTrue(ae instanceof AssignmentConstEmitter);
	}

    @Test
	public void testAssignmentNumConstEmitter() {
        NumAtomAST numast = new NumAtomAST();
        AssignmentEmitter ae = AssignmentEmitterFactory.getAssignmentEmitter(numast);
        assertTrue(ae instanceof AssignmentConstEmitter);
	}
   
}
