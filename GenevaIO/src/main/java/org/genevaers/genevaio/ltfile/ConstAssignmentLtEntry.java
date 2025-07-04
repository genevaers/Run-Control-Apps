package org.genevaers.genevaio.ltfile;

import java.util.List;

public class ConstAssignmentLtEntry extends LtRecordLogger {

    private static final String name = "ConstAssignment";
    private static List<DescriptionKey> descParts;

    public ConstAssignmentLtEntry() {
        super();
    }

    @Override
    public String getLogEntry(LTRecord ltr) {
        LogicTableF1 ass = (LogicTableF1) ltr;
        LogicTableArg arg = ass.getArg();
        String descString = String.format(CONSTASSIGNMENT, arg.getValue().getPrintString(),
                                       arg.getStartPosition(), arg.getFieldLength());
        return getLeadin(ltr) + descString;
    }

    @Override
    public String getDescription(LTRecord ltr) {
        return name;
    }
}
