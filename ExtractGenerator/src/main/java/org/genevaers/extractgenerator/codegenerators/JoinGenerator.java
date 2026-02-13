package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;

import com.google.common.flogger.FluentLogger;

public class JoinGenerator extends ExtractRecordGenerator implements EndScopeGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private int joinid;
    private int targLf;
    private int targLr;
    private String newid;
    private int trueGoto;
    private int falseGoto;
    private int keyLength;
    private int recLength;
    private int fileid;
    private LogicTableF1 join;

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        join = (LogicTableF1)lt;
        joinid = join.getColumnId();
        targLf = join.getArg().getLogfileId();
        targLr = join.getArg().getLrId();
        keyLength = Repository.getLrKeyLen(targLr);
        newid = join.getArg().getValue().getPrintString();
        fileid = Integer.parseInt(newid);
        recLength = Repository.getLRLength(9000000 + fileid);
        trueGoto = join.getGotoRow1();
        falseGoto = join.getGotoRow2();
        String joinSource = String.format("(%s)Join %d -> %s targ LF %d LR %d True:%d False:%d", join.getRowNbr(), joinid, newid, targLf, targLr, trueGoto, falseGoto);
        logger.atInfo().log(joinSource);
        this.lt = lt;
        return new ExtractorEntry(String.format("//%s\n" +
"         jn = JoinsRepo.getJoin(\"%s\");\n" +
"        //Record count used for do again \n" +
"        joinBuffer = jn.getBufferForRecord(numrecords);\n" +
"        if(joinBuffer == null && jn.updateRequired()) {", joinSource, getNewid()));
    }

    public int getJoinid() {
        return joinid;
    }
    
    public String getNewid() {
        return String.format("%d/%d", targLf, targLr);
    }

    public int getTargLf() {
        return targLf;
    }

    public int getTargLr() {
        return targLr;
    }

    public int getFalseRow() {
        return falseGoto;
    }

    public int getTrueeRow() {
        return trueGoto;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getRecLength() {
        return recLength;
    }

    public int getFileid() {
        return fileid;
    }

    @Override
    public EndScope getEndScope() {
        return new EndScope(EndScopeType.JOIN_FOUND, join.getGotoRow1());
    }
 
    public EndScope getNotFoundEndScope() {
        return new EndScope(EndScopeType.JOIN_NOT_FOUND, join.getGotoRow2());
    }
}
