package org.genevaers.engine.extractor;

import org.genevaers.genevaio.recordreader.RecordFileWriter;

public interface Extractor {

    public void processRecord(byte[] src, byte[] target, RecordFileWriter outWriter, int numrecords);

    public int getOutputLen();
    public int getLrLen();

}