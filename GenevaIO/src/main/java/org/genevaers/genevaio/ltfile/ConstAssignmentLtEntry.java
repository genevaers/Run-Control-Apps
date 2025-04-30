package org.genevaers.genevaio.ltfile;

import java.util.List;

public class ConstAssignmentLtEntry extends LtRecordLogger {

    private static final String name = "ConstAssignment";
    private static List<DescriptionKey> descParts;

    public ConstAssignmentLtEntry() {
        super();
    }

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        addDescription(descriptionRoot);
        LogicTableF1 ass = (LogicTableF1) ltr;
        //to get the log entry pass the ltr into the Key tree?
        String fs = DescriptionKeysCache.get(name).getFormatString(); 
        LogicTableArg arg = ass.getArg();
        String descString = String.format(fs, arg.getValue().getPrintString(),
                                       arg.getStartPosition(), arg.getFieldLength());
        //return(String.format(fs, descString));
        return getLeadin(ltr) + descString;
    }

    private void addDescription(DescriptionKey descriptionRoot) {
        DescriptionKey assKey = DescriptionKeysCache.get(name);
        if(assKey != null && assKey.isReferenced() == false) {
            descriptionRoot.addChild(assKey);
            assKey.setReferenced(true);
        } 
    }

    @Override
    public String getDescription(LTRecord ltr) {
        return name;
    }
}
