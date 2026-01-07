package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF0;

import com.google.common.flogger.FluentLogger;

public class ENGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF0 en = new LogicTableF0();
        logger.atInfo().log("EN - end of extract");
        return new ExtractorEntry(String.format("//EN - end of extract"));
    }

}
