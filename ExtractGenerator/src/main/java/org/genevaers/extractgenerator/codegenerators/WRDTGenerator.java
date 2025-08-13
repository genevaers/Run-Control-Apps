package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableWR;

import com.google.common.flogger.FluentLogger;

public class WRDTGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private int outLen;

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableWR wrdt = (LogicTableWR)lt;
        logger.atInfo().log("WRITE DT buffer");
        return new ExtractorEntry(String.format("            outWriter.getRecordToFill().bytes.position(%d);\n" + //
                        "            outWriter.writeAndClearTheRecord();\n", outLen));
    }

    public void setOutputLength(int outputLength) {
        outLen = outputLength;
    }

}
