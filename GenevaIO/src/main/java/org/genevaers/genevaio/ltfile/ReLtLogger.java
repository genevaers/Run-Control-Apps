package org.genevaers.genevaio.ltfile;

import java.util.HashMap;
import java.util.Map;

public class ReLtLogger extends LtRecordLogger{

   	private static Map<String, LtRecordLogger> fcMap;
	static {
		fcMap = new HashMap<>();
		fcMap.put("LUEX", new LuexLtEntry());
	}

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? getF2Entry(ltr) : fcl.getLogEntry(ltr, descriptionRoot);
    }

    private String getF2Entry(LTRecord ltr) {
        LogicTableRE f2 = (LogicTableRE) ltr;
        return (getLeadin(ltr) + "(RE) more details to come");
    }

}
