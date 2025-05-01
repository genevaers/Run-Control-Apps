package org.genevaers.genevaio.ltfile;

public class FieldConstComparator extends LtRecordLogger{

    @Override
    public String getLogEntry(LTRecord ltr) {
        LogicTableF1 cf = (LogicTableF1) ltr;
        return(String.format(COMPARISON, getLeadin(ltr), getFullArg(cf.getArg()), getCompareCode(cf.getCompareType()), cf.getArg().getValue().getPrintString(), getGotos(ltr)));
}

}
