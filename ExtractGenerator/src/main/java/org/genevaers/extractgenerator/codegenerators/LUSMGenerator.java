package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableRE;

import com.google.common.flogger.FluentLogger;

public class LUSMGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableRE lusm = (LogicTableRE)lt;
        logger.atInfo().log("LUSM" );
        return new ExtractorEntry(String.format("//LUSM - update lookup buffer\n" + //
                        "            joinBuffer = jn.updateBuffer();  // not found case?\r\n" + //
                        "        }"));
    }

}
