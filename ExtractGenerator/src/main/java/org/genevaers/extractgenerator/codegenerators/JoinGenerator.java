package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;

import com.google.common.flogger.FluentLogger;

public class JoinGenerator extends ExtractRecordGenerator {
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
    private FunctionSection section;

    public JoinGenerator(FunctionSection section) {
        this.section = section;
    }

    @Override
    public ExtractorEntry processRecord(LTRecord ltr) {
        join = (LogicTableF1)ltr;
        joinid = join.getColumnId();
        targLf = join.getArg().getLogfileId();
        targLr = join.getArg().getLrId();
        keyLength = Repository.getLrKeyLen(targLr);
        newid = join.getArg().getValue().getPrintString();
        fileid = Integer.parseInt(newid);
        recLength = Repository.getLRLength(9000000 + fileid);
        trueGoto = join.getGotoRow1();
        falseGoto = join.getGotoRow2();
        LTRecord trueRec = xlt.getFromPosition(trueGoto);
        LTRecord falseRec = xlt.getFromPosition(falseGoto);

        String joinSource = String.format("(%d) Join %d -> %s targ LF %d LR %d True:%d False:%d", join.getRowNbr(), joinid, newid, targLf, targLr, trueGoto, falseGoto);
        logger.atInfo().log(joinSource);
        String joinEntry = String.format("//%s\n" +
"        jn = JoinsRepo.getJoin(\"%s\");\n" +
"        //Record count used for do again \n" +
"        joinBuffer = jn.getBufferForRecord(numrecords);\n" +
"        if(joinBuffer == null && jn.updateRequired()) {", joinSource, getNewid());

        String joinLogicFormat = "        if(joinBuffer != null) {\n%s\n        } else {\n%s\n        }";

        String JOINFormat = "%s\n%s\n";
        String body = "";
        String logicbody = "";
        String elsebody = "";
        LTRecord lookAhead = xlt.getFromPosition(ltr.getRowNbr() + 1);
        if(section == FunctionSection.FILTER) {
            return null; //generateExtractFilter(trueRec, falseRec, lookAhead);
        } else {
            while(lookAhead != null && !lookAhead.getFunctionCode().equals("LUSM")) {
                ExtractorEntry exe = addFunctionCode(lookAhead, columnRecs, section);
                body += exe.getEntryString() + "\n";
                lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
            }
            //Drop out on the LUSM which will be the join buffer update and end of the join logic
            //LUSM is itself a recurse up to the False
            body +=addLUSMCode(lookAhead);
            lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
            while(lookAhead != null && !lookAhead.getFunctionCode().equals("GOTO")) {
                ExtractorEntry exe = addFunctionCode(lookAhead, columnRecs, section);
                logicbody += exe.getEntryString() + "\n";
                lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
            }
            lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
            while(lookAhead != null && lookAhead.getRowNbr() <= falseGoto) {
                ExtractorEntry exe = addFunctionCode(lookAhead, columnRecs, section);
                elsebody += exe.getEntryString() + "\n";
                lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
            }
            body += String.format(joinLogicFormat, logicbody, elsebody);
            currentRow = lookAhead.getRowNbr();
        }
        return new ExtractorEntry(String.format(JOINFormat, joinEntry, body));
    }

    private String addLUSMCode(LTRecord lookAhead) {
            ExtractorEntry exe = addFunctionCode(lookAhead, columnRecs, section);
            return exe.getEntryString() + "\n";
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
}

