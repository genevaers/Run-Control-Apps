package org.genevaers.genevaio.ltfile;

public class NvLtEntry extends LtEntry{

    @Override
    public String getEntry(LTRecord ltr) {
        return(getViewLine(ltr) + getLeadin(ltr) + getNewViewDetails((LogicTableNV) ltr));
    }

	private static String getViewLine(LTRecord ltr) {
		return String.format("------------\nView %07d\n------------\n", ltr.getViewId());
	}

	private static String getNewViewDetails(LogicTableNV nv) {
		return String.format(NV_FORMAT, nv.getViewType().value(),   nv.getSourceLrId(), nv.getSortKeyLen(),
				nv.getSortTitleLen(), nv.getDtAreaLen(), nv.getCtColCount(), nv.getSourceSeqNbr());
	}
}
