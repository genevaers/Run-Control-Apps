package org.genevaers.genevaio.ltfile;

import java.util.HashMap;
import java.util.Map;

public class F1LtLogger extends LtRecordLogger{

	private static ConstFieldComparator cfComp = new ConstFieldComparator();
	private static FieldConstComparator fcComp = new FieldConstComparator();
   	private static Map<String, LtRecordLogger> fcMap;
	static {
		fcMap = new HashMap<>();
		fcMap.put("JOIN", new JoinLtEntry());
		fcMap.put("LKLR", new LklrLtEntry());
		fcMap.put("LKS", new LksLtEntry());
		fcMap.put("SFCE", cfComp);
		fcMap.put("SFCL", cfComp);
		fcMap.put("CFCE", cfComp);
		fcMap.put("CFCL", cfComp);
		fcMap.put("SFEC", fcComp);
		fcMap.put("SFLC", fcComp);
		fcMap.put("CFEC", fcComp);
		fcMap.put("CFLC", fcComp);
		fcMap.put("DTC", new ConstAssignmentLtEntry());
	}

    @Override
    public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? getF1Entry(ltr) : fcl.getLogEntry(ltr, descriptionRoot);
    }

    private String getF1Entry(LTRecord ltr) {
        LogicTableF1 f1 = (LogicTableF1) ltr;
        return (getLeadin(ltr) + "(" + ltr.getRecordType() + ") more details to come");
    }

	@Override
    public String getDescription(LTRecord ltr) {
        LtRecordLogger fcl = fcMap.get(ltr.getFunctionCode());
        return fcl == null ? "Not sure what to say" : fcl.getDescription(ltr);
    }

}
