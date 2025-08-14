package org.genevaers.engine.lookups;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.genevaers.genevaio.recordreader.FileRecord;

import com.google.common.flogger.FluentLogger;

public class Join {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Map<ByteArrayKey, FileRecord> data = new HashMap<>();
    private int joinId;
    private boolean read;

    private boolean found;

    private FileRecord currentBuffer;

    private int currentRecord;

    private boolean updateRequired;

    private ByteArrayKey key; 
    private int targLf;
    private int targLr;
    private String lflrid;
    private int keyLength;
    private int recLength;
    private int fileid;
    
    public Join(String lflr, int lf, int lr, int rlen, int klen, int fid) {
        lflrid = lflr;
        targLf = lf;
        targLr = lr;
        recLength = rlen;
        keyLength = klen;
        fileid = fid;
    }

    public void addReferenceRecord(FileRecord refRecord) {
        //get the key
        FileRecord joinRecord = new FileRecord();
        //here we need to know how long the key is.
        //That is what is written to the REH file. Which we do not have...
        //We could get from the VDP? Or Repository?
        //Or at the time we build the XLT.java we can add the join details to be read here
        ByteArrayKey ipkey = new ByteArrayKey(refRecord.bytes.array(), 0, 1);
        joinRecord.bytes.put(refRecord.bytes.array(), keyLength, recLength+keyLength);
        data.put(ipkey, joinRecord);
    }

    public void logContent() {
        Iterator<Entry<ByteArrayKey, FileRecord>> jei = data.entrySet().iterator();
        while (jei.hasNext()) {
            Entry<ByteArrayKey, FileRecord> je = jei.next();
            byte[] joinrecBuffer = new byte[recLength];
            FileRecord val = je.getValue();
            val.bytes.position(1);
            for(int i=0; i<recLength; i++) {
                joinrecBuffer[i] = val.bytes.get();
            }
            logger.atInfo().log("Key %s -> %s",je.getKey() , new String(joinrecBuffer));
        }
    }

    public void setJoinId(int joinId) {
        this.joinId = joinId;
    }

    public int getJoinId() {
        return joinId;
    }

    public boolean bufferNotUpdated() {
        return !read;
    }

    public void addToKey(byte[] src, int offset, int len) {
        //This will need to be a ByteBuffer wrapping the key array
        key = new ByteArrayKey(src, offset, len);
    }

    public FileRecord updateBuffer() {
        currentBuffer = data.get(key);
        if(currentBuffer != null) {
            currentBuffer.bytes.position(keyLength);
        }
        return currentBuffer;
    }

    public boolean found() {
        return found;
    }

    public FileRecord getBufferForRecord(int numrecords) {
        if(currentRecord != numrecords) {
            currentRecord = numrecords;
            currentBuffer = null;
            updateRequired = true;
        }
        return currentBuffer;
    }

    public boolean updateRequired() {
        return updateRequired;
    }

    public int getFileid() {
        return fileid;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getRecLength() {
        return recLength;
    }

    public int getTargLf() {
        return targLf;
    }

    public int getTargLr() {
        return targLr;
    }

    public String getLflrid() {
        return lflrid;
    }
}
