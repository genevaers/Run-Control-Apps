package org.genevaers.genevaio.ltfile;

import java.util.ArrayList;
import java.util.List;

public class AssignmentLtEntry extends LtRecordLogger {

    private static final String name = "Assignment";
    private static List<DescriptionKey> descParts;

    public AssignmentLtEntry() {
        super();

        //Defined in the logger 
        // if (descParts == null) {
        //     descParts = new ArrayList<>();
        //     descParts.add(DescriptionKeysCache.get(name));
        //     descParts.add(new TargetColumnDesc());
        // }

    }

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        addDescription(descriptionRoot);
        LogicTableF2 ass = (LogicTableF2) ltr;
        //to get the log entry pass the ltr into the Key tree?
        String fs = DescriptionKeysCache.get(name).getFormatString(); // %-7d %-7d %-7d %-5d %-0d -> %-5d %-0d
        LogicTableArg arg1 = ass.getArg1();
        LogicTableArg arg2 = ass.getArg2();
        String descString = String.format(fs, arg1.getLogfileId(), arg1.getLrId(), arg1.getFieldId(), arg1.getStartPosition(), arg1.getFieldLength(),
                                       arg2.getStartPosition(), arg2.getFieldLength());
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
