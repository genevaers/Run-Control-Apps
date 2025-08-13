package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.genevaio.ltfile.LogicTableNV;

import com.google.common.flogger.FluentLogger;

public class DTCGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF1 dtc = (LogicTableF1)lt;
        LogicTableArg arg = dtc.getArg();
        logger.atInfo().log("DTC value %s", arg.getValue().getPrintString());
        int targfieldLength = arg.getFieldLength();
        String targString = "0";
        if(arg.getValue().getPrintString().equals("")) {
            targString = String.format("%-" + targfieldLength + "s", " ");
        } else {
            targString = arg.getValue().getPrintString();
        }
        return new ExtractorEntry(
            String.format("        target.put(\"%s\".getBytes());", targString));
    }

}
