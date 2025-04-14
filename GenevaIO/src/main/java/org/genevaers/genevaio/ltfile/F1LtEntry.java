package org.genevaers.genevaio.ltfile;

import java.util.HashMap;
import java.util.Map;

public class F1LtEntry extends LtEntry{

   	private static Map<String, LtEntry> fcMap;
	static {
		fcMap = new HashMap<>();
		// typeMap.put(LtRecordType.CALC, "Calc");
		// typeMap.put(LtRecordType.CC, "CC");
		// typeMap.put(LtRecordType.F0, "F0");
		fcMap.put("JOIN", new JoinLtEntry());
		fcMap.put("LKS", new LksLtEntry());
		// typeMap.put(LtRecordType.F2, "F2");
		//fcMap.put(LtRecordType.GENERATION, new GenerationLtEntry());
		//fcMap.put(LtRecordType.HD, new HdEntry());
		// typeMap.put(LtRecordType.NAME, "NAME");
		// typeMap.put(LtRecordType.NAMEF1, "NAMEF1");
		// typeMap.put(LtRecordType.NAMEF2, "NAMEF2");
		// typeMap.put(LtRecordType.NAMEVALUE, "NAMEVALUE");
		//fcMap.put(LtRecordType.NV, new NvLtEntry());
		// typeMap.put(LtRecordType.RE, "RE");
		// typeMap.put(LtRecordType.WR, "WR");
	}

    @Override
    public String getEntry(LTRecord ltr) {
        LtEntry fce = fcMap.get(ltr.getFunctionCode());
        return fce == null ? getF1Entry(ltr) : fce.getEntry(ltr);
    }

    private String getF1Entry(LTRecord ltr) {
        LogicTableF1 f1 = (LogicTableF1) ltr;
        return (getLeadin(ltr) + " \"" + f1.getArg().getValue().getPrintString() + "\"");
    }

}
