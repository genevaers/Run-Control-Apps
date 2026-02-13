package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;

import com.google.common.flogger.FluentLogger;

public class CFECGenerator extends ComparisonGenerator implements EndScopeGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private LogicTableF1 cfec;

    public CFECGenerator(FunctionSection section) {
        super(section);
    }

    @Override
    public EndScope getEndScope() {
        int nextRow = lt.getRowNbr() + 1;
        if(cfec.getGotoRow1() > nextRow && cfec.getGotoRow2() > nextRow) {
            logger.atSevere().log("CFEC at row %d has both GOTOs forward to rows %d and %d. Must be an OR?", lt.getRowNbr(), cfec.getGotoRow1(), cfec.getGotoRow2());
            if(cfec.getGotoRow2() > cfec.getGotoRow1()) {
                return new EndScope(EndScopeType.IF_END, cfec.getGotoRow2());
            } else {
                return new EndScope(EndScopeType.IF_END, cfec.getGotoRow1());
            }
        } else {
            if(cfec.getGotoRow2() > cfec.getGotoRow1()) {
                return new EndScope(EndScopeType.IF_END, cfec.getGotoRow2());
            } else {
                return new EndScope(EndScopeType.IF_END, cfec.getGotoRow1());
            }
        }
    }

    @Override
    protected void getPredicateAndProcessFunctionCode(LTRecord lt) {
        cfec = (LogicTableF1)lt;
        LogicTableArg arg = cfec.getArg();
        cfSource = String.format("(%d)CFEC if src pos %d len %d equals %s True %d False %d", lt.getRowNbr(), arg.getStartPosition(),  arg.getFieldLength(), arg.getValue().getPrintString(), cfec.getGotoRow1(), cfec.getGotoRow2());
        logger.atInfo().log(cfSource);
        trueRow = cfec.getGotoRow1();
        falseRow = cfec.getGotoRow2();

        predicate = String.format("new String(src, %d , %d).equals(\"%s\")", arg.getStartPosition()-1,  arg.getFieldLength(), arg.getValue().getPrintString());
    }

}
