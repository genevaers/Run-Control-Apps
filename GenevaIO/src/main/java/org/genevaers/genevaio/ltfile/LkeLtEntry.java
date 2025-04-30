package org.genevaers.genevaio.ltfile;

public class LkeLtEntry extends LtRecordLogger {

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        LogicTableF2 lke = (LogicTableF2) ltr;
        return (String.format(CONSTASSIGNMENT, getLeadin(ltr), getFullArg(lke.getArg1()), getArgKeyDetails(lke.getArg2())));
}

}
