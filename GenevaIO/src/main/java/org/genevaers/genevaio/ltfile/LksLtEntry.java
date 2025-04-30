package org.genevaers.genevaio.ltfile;

public class LksLtEntry extends LtRecordLogger{

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        LogicTableF1 lks = (LogicTableF1) ltr;
        return (String.format(CONSTASSIGNMENT, getLeadin(lks), getArgConst(lks.getArg()), getArgKeyDetails(lks.getArg())));
    }

}
