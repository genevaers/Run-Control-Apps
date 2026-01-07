package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;

public abstract class ExtractRecordGenerator {
    protected LTRecord lt;

    public abstract ExtractorEntry processRecord(LTRecord lt);

    public LTRecord getLt() {
        return lt;
    }

}