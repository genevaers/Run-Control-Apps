package org.genevaers.compilers.extract.astnodes;

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


import org.genevaers.compilers.base.EmittableASTNode;
import org.genevaers.genevaio.ltfactory.LtFactoryHolder;
import org.genevaers.genevaio.ltfactory.LtFuncCodeFactory;
import org.genevaers.genevaio.ltfile.LTFileObject;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF1;

import com.google.common.flogger.FluentLogger;

public class IsNumericAST extends ExtractBaseAST implements  EmittableASTNode{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Integer goto1 = 0;
    private Integer goto2 = 0;

    protected LTFileObject ltfo;

    public IsNumericAST() {
        type = ASTFactory.Type.ISNUMERIC;
    }

    @Override
    public void emit() {
        ExtractBaseAST child = (ExtractBaseAST) children.get(0);
        if(child != null) {
            emitFunctionCodeBasedOnDataSource(child);
        } else { 
            logger.atSevere().log("Is Numeric has no child node");
        }
    }

    protected void emitFunctionCodeBasedOnDataSource(ExtractBaseAST child) {
        switch(child.getType()) {
            case LRFIELD:
                emitCNE(child);
            break;
            case LOOKUPFIELDREF:
                emitCNL(child);
            break;
            case COLUMNREF:
                emitCNX(child);
            break;
            case PRIORLRFIELD:
                emitCNP(child);
            break;
            default:
                logger.atSevere().log("Incorrect type for Is Null %s", child.getType());
            break;
        }
    }

    protected void emitCNP(ExtractBaseAST prior) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltfo = fcf.getCNP(((FieldReferenceAST)prior).getRef());
        addToLogicTableAndInitialiseGotos(ltfo);
    }

    protected void emitCNX(ExtractBaseAST col) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltfo = fcf.getCNX(((ColumnAST)col).getViewColumn());
        addToLogicTableAndInitialiseGotos(ltfo);
    }

    protected void emitCNL(ExtractBaseAST lkp) {
        LookupFieldRefAST lkf = (LookupFieldRefAST) lkp;
        lkf.getLkEmitter().emitJoin(lkf, false);
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltfo = fcf.getCNL(((LookupFieldRefAST)lkp).getRef());
        ((LogicTableF1)ltfo).getArg().setLogfileId(lkf.getLookup().getTargetLFID());
        addToLogicTableAndInitialiseGotos(ltfo);
    }

    protected void emitCNE(ExtractBaseAST field) {
        LtFuncCodeFactory fcf = LtFactoryHolder.getLtFunctionCodeFactory();
        ltfo = fcf.getCNE(((FieldReferenceAST)field).getRef());
        addToLogicTableAndInitialiseGotos(ltfo);
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
        ExtractBaseAST source = (ExtractBaseAST) children.get(0);

        if (source != null) {
            ((LTRecord)ltfo).setGotoRow1(compT);
            ((LTRecord)ltfo).setGotoRow2( compF );
            source.resolveGotos(compT, compF, joinT, joinF);
        }
    }

    protected void addToLogicTableAndInitialiseGotos(LTFileObject ltfo) {
        if(ltfo != null) {
            ltEmitter.addToLogicTable((LTRecord)ltfo);

            goto1 = ltEmitter.getNumberOfRecords();
            ((LTRecord)ltfo).setGotoRow1(goto1);
            ((LTRecord)ltfo).setGotoRow2( goto2 );
        }
    }
    
}
