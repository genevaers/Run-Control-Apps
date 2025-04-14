package org.genevaers.genevaio.ltfile;

public class LksLtEntry extends LtEntry{

    @Override
    public String getEntry(LTRecord ltr) {
        LogicTableF1 lks = (LogicTableF1) ltr;
        return (String.format(CONSTASSIGNMENT, getLeadin(lks), getArgConst(lks.getArg()), getArgKeyDetails(lks.getArg())));
    }

}
