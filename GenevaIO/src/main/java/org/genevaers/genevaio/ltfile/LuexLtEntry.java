package org.genevaers.genevaio.ltfile;

public class LuexLtEntry extends LtRecordLogger{

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        LogicTableRE luex = (LogicTableRE) ltr;
        return(String.format(LUEX, getLeadin(ltr), luex.getReadExitId()));
    }

}
