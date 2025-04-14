package org.genevaers.genevaio.ltfile;

public class JoinLtEntry extends LtEntry {

    @Override
    public String getEntry(LTRecord ltr) {
        LogicTableF1 j = (LogicTableF1) ltr;
        LogicTableArg arg = j.getArg();
        return(String.format(LEAD2GOTOS,
                                getLeadin(ltr) + String.format(JOIN, j.getColumnId(), arg.getValue().getPrintString(), arg.getLogfileId(), arg.getLrId()),
                                getGotos(ltr)));
    }

}
