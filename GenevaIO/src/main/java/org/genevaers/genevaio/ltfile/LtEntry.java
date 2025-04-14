package org.genevaers.genevaio.ltfile;

import org.genevaers.repository.components.enums.DataType;

public abstract class LtEntry {

	protected static final String ROW_WIDTH ="7";
	protected static final String COLUMN_WIDTH ="5";
	protected static final String GOTO_WIDTH ="5";
	protected static final String BEFORE_GOTOS ="120";
	protected static final String LFLR_FIELD ="%7d";
	protected static final String SIGN_RND_DATE ="(%s/%d,%d %s)";
	protected static final String DATA_TYPE ="%1s ";
    
	protected static final String LEAD_IN = " %" + ROW_WIDTH + "d %" + COLUMN_WIDTH + "d %-4s";
	protected static final String GEN_FORMAT = "%s  Text %s\n"
			+ "               Number of Records              %d\n"
			+ "               Total byte count               %d\n"
			+ "               Number of HD Records           %d\n"
			+ "               Number of NV Records           %d\n"
			+ "               Number of F0 Records           %d\n"
			+ "               Number of F1 Records           %d\n"
			+ "               Number of F2 Records           %d\n"
			+ "               Number of RE Records           %d\n"
			+ "               Number of WR Records           %d\n"
			+ "               Number of CC Records           %d\n"
			+ "               Number of Name Records         %d\n"
			+ "               Number of NameV Records        %d\n"
			+ "               Number of Calc Records         %d\n"
			+ "               Number of NameF1 Records       %d\n"
			+ "               Number of NameF2 Records       %d\n"
			+ "\n"
			+ "Generated on %s%s-%s-%s %s:%s:%s";
    protected static final String NV_FORMAT = " %s  LR=%s SKA=%s, STA=%s, DTA=%s, CTC=%s Source Number=%s";
	protected static final String JOIN = " %d -> \"%s\" %d/%d";
	protected static final String GOTOS = "%" + GOTO_WIDTH + "d %" + GOTO_WIDTH + "d";
	protected static final String LEAD2GOTOS = "%-" + BEFORE_GOTOS + "s %s";

    protected static final String ARGLFLRFIELD = LFLR_FIELD + " " + LFLR_FIELD + " " + LFLR_FIELD;
	protected static final String ARGTYPESIGNANDDATE = DATA_TYPE + SIGN_RND_DATE;

	protected static final String ARGCOLVALUES = "%4d %3d %s " + SIGN_RND_DATE;
	protected static final String ARGCOLREF = "%2s %3d %3d %3d ";

	protected static final String KEYVALUES = "     %3d %s " + SIGN_RND_DATE;

    protected static final String CONSTASSIGNMENT = "%s %47s  ->  %s";

    public abstract String getEntry(LTRecord ltr);

    protected String getLeadin(LTRecord ltr) {
		return String.format(LEAD_IN, ltr.getRowNbr(), ltr.getSuffixSeqNbr(), ltr.getFunctionCode());
    }

    protected static String getGotos(LTRecord ltr) {
		return String.format(GOTOS, ltr.getGotoRow1(), ltr.getGotoRow2());
	}

	protected static Object getArgConst(LogicTableArg arg) {
		return "\"" + arg.getValue().getPrintString() + "\"";
	}

	protected static String getArgDetails(LogicTableArg a) {
		return String.format(ARGCOLVALUES, a.getStartPosition(), a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
				a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId());
	}

	private static Object getDataTypeLetter(DataType fieldFormat) {
		switch (fieldFormat) {
			case ALPHANUMERIC:
				return "X";
			case BINARY:
				return "B";
			case BCD:
				return "C";
			case ZONED:
				return "Z";
			case PACKED:
				return "P";
			case BSORT:
				return "T";
			case EDITED:
				return "E";
			case MASKED:
				return "M";
			case PSORT:
				return "S";
			default:
				return "?";
		}
	}

	protected static String getArgKeyDetails(LogicTableArg a) {
		return String.format(KEYVALUES, a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
				a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId());
	}


}
