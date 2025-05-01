package org.genevaers.genevaio.ltfile;

public class AssignmentLtEntry extends LtRecordLogger {

    private static final String name = "Assignment";

    public AssignmentLtEntry() {
        super();
    }

    @Override
    public String getLogEntry(LTRecord ltr) {
        LogicTableF2 ass = (LogicTableF2) ltr;
        LogicTableArg arg1 = ass.getArg1();
        LogicTableArg arg2 = ass.getArg2();
        String descString = String.format(ASSIGNMENT, arg1.getLogfileId(), arg1.getLrId(), arg1.getFieldId(), arg1.getStartPosition(), arg1.getFieldLength(),
                                       arg2.getStartPosition(), arg2.getFieldLength());
        return getLeadin(ltr) + descString;
    }

    @Override
    public String getDescription(LTRecord ltr) {
        return name;
    }
}
