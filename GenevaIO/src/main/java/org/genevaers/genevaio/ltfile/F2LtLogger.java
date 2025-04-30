package org.genevaers.genevaio.ltfile;

import java.util.HashMap;
import java.util.Map;

public class F2LtLogger extends LtRecordLogger{

   	private static Map<String, LtRecordLogger> fcMap;

    static {
		fcMap = new HashMap<>();
		fcMap.put("LKE", new LkeLtEntry());
		fcMap.put("DTE", new AssignmentLtEntry());
	}

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? getF2Entry(ltr) : fcl.getLogEntry(ltr, descriptionRoot);
    }

    private String getF2Entry(LTRecord ltr) {
        LogicTableF2 f2 = (LogicTableF2) ltr;
        return (getLeadin(ltr) + "(F2) more details to come" );
    }

	@Override
    public String getDescription(LTRecord ltr) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? "Not sure what to say" : fcl.getDescription(ltr);
    }
}
