package org.genevaers.genevaio.ltfile;

public class LklrLtEntry extends LtRecordLogger {

    @Override
    public String getLogEntry(LTRecord ltr) {
        return(getLeadin(ltr) + getLKLRInfo(ltr));
    }

    private static Object getLKLRInfo(LTRecord ltr) {
		LogicTableF1 lklr = (LogicTableF1) ltr;
		return String.format(JOIN_MAPPING, lklr.getArg().getLogfileId(), lklr.getArg().getLrId(), lklr.getColumnId(), lklr.getArg().getValue().getPrintString());
	}

}
