package org.genevaers.genevaio.ltfile;

public class HdEntry extends LtEntry{

    @Override
    public String getEntry(LTRecord ltr) {
		return String.format(LEAD_IN, ltr.getRowNbr(), ltr.getSuffixSeqNbr(), ltr.getFunctionCode());
    }

}
