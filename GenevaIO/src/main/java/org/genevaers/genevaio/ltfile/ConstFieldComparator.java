package org.genevaers.genevaio.ltfile;

public class ConstFieldComparator extends LtRecordLogger{

    @Override
    public String getLogEntry(LTRecord ltr) {
        LogicTableF1 cf = (LogicTableF1) ltr;
        return(String.format(COMPARISON, getLeadin(ltr), cf.getArg().getValue().getPrintString(), getCompareCode(cf.getCompareType()), getFullArg(cf.getArg()) , getGotos(ltr)));
    }

}
