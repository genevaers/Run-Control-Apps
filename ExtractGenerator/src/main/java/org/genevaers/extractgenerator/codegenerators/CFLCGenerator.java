package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;

import com.google.common.flogger.FluentLogger;

public class CFLCGenerator extends ComparisonGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private LogicTableF1 cflc;

    public CFLCGenerator(FunctionSection section) {
        super(section);
    }

    @Override
    protected void getPredicateAndProcessFunctionCode(LTRecord ltr) {
        cflc = (LogicTableF1)ltr;
        LogicTableArg arg = cflc.getArg();
        cfSource = String.format("(%d) CFLC if src pos %d len %d equals %s True %d False %d", ltr.getRowNbr(), arg.getStartPosition(),  arg.getFieldLength(), arg.getValue().getPrintString(), cflc.getGotoRow1(), cflc.getGotoRow2());
        logger.atInfo().log(cfSource);
        trueRow = cflc.getGotoRow1();
        falseRow = cflc.getGotoRow2();

        predicate = String.format("new String(joinBuffer.bytes.array(), %d , %d).equals(\"%s\")", arg.getStartPosition()-1,  arg.getFieldLength(), arg.getValue().getPrintString());
    }

}
