package org.genevaers.compilers.extract.astnodes;

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
import java.util.Map;
import java.util.Objects;

import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFCEEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFCLEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFCPEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFCXEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFECEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFEEEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFELEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFEPEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFEXEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFLCEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFLEEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFLLEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFLPEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFLXEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFPCEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFPEEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFPLEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFPPEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFPXEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFXCEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFXEEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFXLEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFXPEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.SFXXEmitter;
import org.genevaers.compilers.extract.emitters.stringcomparisonemitters.StringComparisonEmitter;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.LtRecordType;

public class StringComparisonAST extends ExtractBaseAST implements EmittableASTNode{

    private String op;
    private Integer goto1;
    private Integer goto2 = 0;

    private class ComparisonKey {
        ASTFactory.Type lhsType;
        ASTFactory.Type rhsType;

        public ComparisonKey(Type lhs, Type rhs) {
            lhsType = lhs;
            rhsType = rhs;
        }
        @Override
        public boolean equals(Object ck)
        {
            if (ck == this) {
                return true;
            }
     
            if (ck == null || ck.getClass() != getClass()) {
                return false;
            }
            ComparisonKey comp = (ComparisonKey) ck;
            return lhsType == comp.lhsType && rhsType == comp.rhsType;
        }
     
        @Override
        public int hashCode() {
            return Objects.hash(lhsType, rhsType);
        }
     
        @Override
        public String toString() {
            return "{" + lhsType + ", " + rhsType + "}";
        }    
    }

    Map<ComparisonKey, StringComparisonEmitter> emitters = new HashMap<>();
    private LTFileObject ltfo;
    private ExtractBaseAST lhs;
    private ExtractBaseAST rhs;
    private DataType lhsCastTo;

    public StringComparisonAST() {
        type = ASTFactory.Type.STRINGCOMP;

        //LHS Const
        emitters.put(new ComparisonKey(ASTFactory.Type.STRINGATOM, ASTFactory.Type.LRFIELD), new SFCEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.RUNDATE, ASTFactory.Type.LRFIELD), new SFCEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.DATEFUNC, ASTFactory.Type.LRFIELD), new SFCEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.STRINGATOM, ASTFactory.Type.LOOKUPFIELDREF), new SFCLEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.STRINGATOM, ASTFactory.Type.PRIORLRFIELD), new SFCPEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.STRINGATOM, ASTFactory.Type.COLUMNREF), new SFCXEmitter());

        //LHS Field
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.STRINGATOM), new SFECEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.RUNDATE), new SFECEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.DATEFUNC), new SFECEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.FISCALDATE), new SFECEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.LRFIELD), new SFEEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.LOOKUPFIELDREF), new SFELEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.PRIORLRFIELD), new SFEPEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LRFIELD, ASTFactory.Type.COLUMNREF), new SFEXEmitter());

        //LHS Lookup
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.STRINGATOM), new SFLCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.DATEFUNC), new SFLCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.RUNDATE), new SFLCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.LRFIELD), new SFLEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.LOOKUPFIELDREF), new SFLLEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.PRIORLRFIELD), new SFLPEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.LOOKUPFIELDREF, ASTFactory.Type.COLUMNREF), new SFLXEmitter());

        //LHS PRIOR
        emitters.put(new ComparisonKey(ASTFactory.Type.PRIORLRFIELD, ASTFactory.Type.STRINGATOM), new SFPCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.PRIORLRFIELD, ASTFactory.Type.RUNDATE), new SFPCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.PRIORLRFIELD, ASTFactory.Type.LRFIELD), new SFPEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.PRIORLRFIELD, ASTFactory.Type.LOOKUPFIELDREF), new SFPLEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.PRIORLRFIELD, ASTFactory.Type.PRIORLRFIELD), new SFPPEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.PRIORLRFIELD, ASTFactory.Type.COLUMNREF), new SFPXEmitter());

        //LHS ColumnRef
        emitters.put(new ComparisonKey(ASTFactory.Type.COLUMNREF, ASTFactory.Type.STRINGATOM), new SFXCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.COLUMNREF, ASTFactory.Type.RUNDATE), new SFXCEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.COLUMNREF, ASTFactory.Type.LRFIELD), new SFXEEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.COLUMNREF, ASTFactory.Type.LOOKUPFIELDREF), new SFXLEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.COLUMNREF, ASTFactory.Type.PRIORLRFIELD), new SFXPEmitter());
        emitters.put(new ComparisonKey(ASTFactory.Type.COLUMNREF, ASTFactory.Type.COLUMNREF), new SFXXEmitter());

    }

    @Override
    public void emit() {
        if(children.size() < 2) {
            int bang = 1;
        }
        ExtractBaseAST lhsin = (ExtractBaseAST) children.get(0);
        ExtractBaseAST rhsin = (ExtractBaseAST) children.get(1);
        StringComparisonEmitter emitter = getComparisonEmitter(lhsin, rhsin); 
        if(emitter != null) {
            emitter.setLtEmitter(ltEmitter);
            ltfo = emitter.getLTEntry(op, lhs, rhs);
            applyComparisonRules(op, lhsin, rhsin);
            if(ltfo != null) {
                ltEmitter.addToLogicTable((LTRecord)ltfo);

                goto1 = ltEmitter.getNumberOfRecords();
                ((LTRecord)ltfo).setGotoRow1(goto2);
                ((LTRecord)ltfo).setGotoRow2( goto2 );
            }
        } else {
            int workToDo = 1;
        }
    }


    private StringComparisonEmitter getComparisonEmitter( ExtractBaseAST lhsin, ExtractBaseAST rhsin) {
        //The real type can be under a cast.
        lhs = lhsin;
        rhs = rhsin;
        if(lhsin.getType() == Type.CAST){
            //add a decast to the node
            //note we need to change the formatID of the datasource
            CastAST cast = (CastAST) lhsin;
            lhs = (ExtractBaseAST) cast.decast();
            lhsCastTo = ((DataTypeAST)cast.getChildIterator().next()).getDatatype();
        }
        return emitters.get(new ComparisonKey(lhs.getType(), rhs.getType()));
    }

    public void setComparisonOperator(String op) {
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    public void setGoto1(Integer goto1) {
        this.goto1 = goto1;
    }

    public void setGoto2(Integer goto2) {
        this.goto2 = goto2;
        ((LTRecord)ltfo).setGotoRow2(goto2);
    }

    public Integer getGoto1() {
        return goto1;
    }

    public Integer getGoto2() {
        return goto2;
    }

    @Override
    public void resolveGotos(Integer compT, Integer compF, Integer joinT, Integer joinF) {
        if (isNot) {
            goto1 = compF;
            goto2 = compT;
        } else {
            ((LTRecord)ltfo).setGotoRow1(compT);
            ((LTRecord)ltfo).setGotoRow2(compF);
        }

        // resolve children
        ExtractBaseAST lhs = (ExtractBaseAST) children.get(0);
        ExtractBaseAST rhs = (ExtractBaseAST) children.get(1);
        if (lhs != null && rhs != null) {
            if (isNot) {
                lhs.resolveGotos(compT, compF, joinT, getEndOfLogic());
                rhs.resolveGotos(compT, compF, joinT, getEndOfLogic());
            } else {
                lhs.resolveGotos(compT, compF, joinT, joinF);
                rhs.resolveGotos(compT, compF, joinT, joinF);
            }
        }
    }

    private void applyComparisonRules(String op, ExtractBaseAST lhs, ExtractBaseAST rhs) {
        //if both sides have compatiible date codes strip date codes
        //if sides hava ALNUM and NUM flip ALNUM to Zoned
        if( lhsCastTo != null) {
            if(((LTRecord)ltfo).getRecordType() == LtRecordType.F1) {
                ((LogicTableF1)ltfo).getArg().setFieldFormat(lhsCastTo);
            }
        }
    }

}
