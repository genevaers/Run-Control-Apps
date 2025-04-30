package org.genevaers.genevaio.ltfile;

public class NvLtEntry extends LtRecordLogger{

	private static String getViewLine(LTRecord ltr) {
		return String.format("------------\nView %07d\n------------\n", ltr.getViewId());
	}

	private static String getNewViewDetails(LogicTableNV nv) {
		return String.format(NV_FORMAT, nv.getViewType().value(),   nv.getSourceLrId(), nv.getSortKeyLen(),
				nv.getSortTitleLen(), nv.getDtAreaLen(), nv.getCtColCount(), nv.getSourceSeqNbr());
	}

	@Override
	public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        return(getViewLine(ltr) + getLeadin(ltr) + getNewViewDetails((LogicTableNV) ltr));
	}
}
