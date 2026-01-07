package org.genevaers.engine.extractor;

import java.nio.ByteBuffer;

import org.genevaers.genevaio.recordreader.RecordFileWriter;

public interface Extractor {

    public void processRecord(byte[] src, ByteBuffer bytes, RecordFileWriter outWriter, int numrecords);

    public int getOutputLen();
    public int getLrLen();

}