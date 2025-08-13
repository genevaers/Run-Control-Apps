package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF2;
import org.genevaers.genevaio.ltfile.LogicTableNV;
import org.genevaers.repository.Repository;

import com.google.common.flogger.FluentLogger;

public class LKEGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF2 lke = (LogicTableF2)lt;
        logger.atInfo().log("LKE" );
        return new ExtractorEntry(String.format("//LKE - make the key value\n             jn.addToKey(src, 0, 1);"));
    }

}
