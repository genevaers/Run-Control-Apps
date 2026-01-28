package org.genevaers.genevaio.ltfile;

import java.util.HashMap;
import java.util.Map;

public class WrLtLogger extends LtRecordLogger{

   	private static Map<String, LtRecordLogger> fcMap;

    static {
		fcMap = new HashMap<>();
		// fcMap.put("LKE", new LkeLtEntry());
		// fcMap.put("DTE", new AssignmentLtEntry());
	}

    @Override
    public String getLogEntry(LTRecord ltr) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? getWrEntry(ltr) : fcl.getLogEntry(ltr);
    }

    private String getWrEntry(LTRecord ltr) {
        LogicTableWR f2 = (LogicTableWR) ltr;
        return (getLeadin(ltr) + "(WR) more details to come" );
    }

	@Override
    public String getDescription(LTRecord ltr) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? "Not sure what to say" : fcl.getDescription(ltr);
    }

}
