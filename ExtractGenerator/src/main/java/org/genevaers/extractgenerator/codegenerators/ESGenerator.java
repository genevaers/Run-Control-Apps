package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF0;
import org.genevaers.genevaio.ltfile.LogicTableWR;

import com.google.common.flogger.FluentLogger;

public class ESGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF0 es = new LogicTableF0();
        logger.atInfo().log("ES - end of set");
        return new ExtractorEntry(String.format("//ES - end of set"));
    }

}
