package org.genevaers.genevaio.ltfile;

import java.util.ArrayList;
import java.util.List;

public class JoinLtEntry extends LtRecordLogger {

    private static List<DescriptionPart> descParts;

    public JoinLtEntry() {
        super();
     }
    
    @Override
    public String getLogEntry(LTRecord ltr) {
        LogicTableF1 j = (LogicTableF1) ltr;
        LogicTableArg arg = j.getArg();
        return(String.format(LEAD2GOTOS,
                                getLeadin(ltr) + String.format(JOIN_MAPPING, arg.getLogfileId(), arg.getLrId(), j.getColumnId(), arg.getValue().getPrintString()),
                                getGotos(ltr)));
    }

    @Override
    public String getDescription(LTRecord ltr) {
        return descParts == null ? "no parts" : addDescripionParts(descParts);
    }

}
