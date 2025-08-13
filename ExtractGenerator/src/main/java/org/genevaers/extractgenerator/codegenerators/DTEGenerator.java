package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;

import com.google.common.flogger.FluentLogger;

public class DTEGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF2 dte = (LogicTableF2)lt;
        LogicTableArg arg1 = dte.getArg1();
        LogicTableArg arg2 = dte.getArg2();
        logger.atInfo().log("DTE from pos %d len %d to pos %d len %d", arg1.getStartPosition(),  arg1.getFieldLength(), arg2.getStartPosition(), arg2.getFieldLength());
        return new ExtractorEntry(
        String.format("        target.put(src, %d, %d);", arg1.getStartPosition()-1, arg1.getFieldLength()));
    }

}
