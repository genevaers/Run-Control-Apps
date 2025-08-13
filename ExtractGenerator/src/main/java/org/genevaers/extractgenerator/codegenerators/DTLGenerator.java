package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;

import com.google.common.flogger.FluentLogger;

public class DTLGenerator extends ExtractRecordGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private int fieldLength;

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF2 dte = (LogicTableF2)lt;
        LogicTableArg arg1 = dte.getArg1();
        LogicTableArg arg2 = dte.getArg2();
        //We need to ensure that the management of the join buffer is done in the same way as the preformance engine
        //It is restarted from the data and so does not include the key fields
        //That would be managed in the JOIN function code.
        logger.atInfo().log("DTL from pos %d len %d to pos %d len %d", arg1.getStartPosition(),  arg1.getFieldLength(), arg2.getStartPosition(), arg2.getFieldLength());
        fieldLength = arg1.getFieldLength();
        return new ExtractorEntry(
        String.format("//DTL\n        if(joinBuffer != null) {\n" + //
                        "            target.put(joinBuffer.bytes.array(), %d, %d);\n" + //
                        "        }", arg1.getStartPosition()-1, fieldLength));
    }

    public int getFieldLength() {
        return fieldLength;
    }

}
