package org.genevaers.genevaio.ltfile;

import java.util.List;

import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.LtCompareType;

public abstract class LtRecordLogger {


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
	protected static final String JOIN_MAPPING = " %d/%d %d -> \"%s\"";
	protected static final String GOTOS = "%" + GOTO_WIDTH + "d %" + GOTO_WIDTH + "d";
	protected static final String LEAD2GOTOS = "%-" + BEFORE_GOTOS + "s %s";
	protected static final String LKLR2GOTOS = "%-23s %-95s %s";

    protected static final String ARGLFLRFIELD = LFLR_FIELD + " " + LFLR_FIELD + " " + LFLR_FIELD;
	protected static final String ARGTYPESIGNANDDATE = DATA_TYPE + SIGN_RND_DATE;

	protected static final String ARGCOLVALUES = "%4d %3d %s " + SIGN_RND_DATE;
	protected static final String ARGCOLREF = "%2s %3d %3d %3d ";

	protected static final String KEYVALUES = "     %3d %s " + SIGN_RND_DATE;

    protected static final String CONSTASSIGNMENT = "%s %47s  ->  %s";
	protected static final String COMPARISON = "%s %47s  %s  %-47s %s";  //leading LHS op RHS gotos

	protected static final String LUEX = "%s  User Exit ID=%d";

	protected static final String ASSIGNMENT = "%s %47s  ->  %-47s";

	protected static List<DescriptionPart> descParts;

    public abstract String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot);

    public String getDescription(LTRecord ltr) {
		return "Don't know what to say yet";
	}

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

	protected static String getFullArg(LogicTableArg arg1) {
		return getArgLFLRData(arg1) + getArgDetails(arg1);
	}

	private static String getArgLFLRData(LogicTableArg a) {
		return String.format(ARGLFLRFIELD, a.getLogfileId(), a.getLrId(), a.getFieldId());
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


	protected static String getCompareCode(LtCompareType comp) {
		switch (comp) {
			case CONTAINS:
				return "CO";
			case BEGINS:
				return "BW";
			case ENDS:
				return "EW";
		
			default:
				return comp.toString();
		}
	}

    protected String addDescripionParts(List<DescriptionPart> descParts) {
        StringBuilder sb = new StringBuilder();
        for (DescriptionPart descriptionPart : descParts) {
            sb.append(" ");
            sb.append(descriptionPart.getDescriptionKey());
        }
        return sb.toString();
    }

	protected static String getColArgDetails(LogicTableArg a) {
		return String.format(ARGCOLVALUES, a.getStartPosition(), a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
				a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId() + getAlignmentLetter(a));
	}


	private static String getAlignmentLetter(LogicTableArg a) {
		switch (a.getJustifyId()) {
			case LEFT:
				return " L";
			case CENTER:
				return " C";
			case NONE:
				return " N";
			case RIGHT:
				return " R";
			default:
				return " N";
		}
	}

	public static void initialiseKeys() {
		//Need to tie the Description Keys more closily with the LtEnries?
		//Or maybe the name is enough?

		//Function Code level keys
		DescriptionKey ass = DescriptionKeysCache.add("Assignment", new DescriptionKey("LF_LR_Field -> Targ_Column", 0));
		ass.setSeparator(" -> ");
		DescriptionKey constass = DescriptionKeysCache.add("ConstAssignment", new DescriptionKey("ConstValue -> Targ_Column", 0));
		constass.setSeparator(" -> ");

		//Assignemnt keys
		DescriptionKey lflrf = DescriptionKeysCache.add("LF_LR_Field", new DescriptionKey("LFID LRID FieldID fPosition FieldAttributes", 0));
		ass.addChild(lflrf);
		DescriptionKey tc = DescriptionKeysCache.add("Targ_Column", new DescriptionKey("cPosition ColumnAttributes", 0));
		ass.addChild(tc);

		//LF_LR_Field
		DescriptionKey lf = DescriptionKeysCache.add("LFID", new DescriptionKey("Integer", 7));
		lflrf.addChild(lf);
        DescriptionKey lr = DescriptionKeysCache.add("LRID", new DescriptionKey("Integer", 7));
		lflrf.addChild(lr);
        DescriptionKey field = DescriptionKeysCache.add("FieldID", new DescriptionKey("Integer", 7));
		lflrf.addChild(field);
		DescriptionKey fp = DescriptionKeysCache.add("fPosition", new DescriptionKey("Integer", 5));
		lflrf.addChild(fp);
		//Can make a specific FieldAttributes class that knows how to describe its children
		DescriptionKey fa = DescriptionKeysCache.add("FieldAttributes", new DescriptionKey("Variable", 1));
		lflrf.addChild(fa);


		DescriptionKey p = DescriptionKeysCache.add("cPosition", new DescriptionKey("Integer", 5));
		tc.addChild(p);
        DescriptionKey ca = DescriptionKeysCache.add("ColumnAttributes", new DescriptionKey("Length Type ColumnDetails", 1));
		tc.addChild(ca);

		DescriptionKey cd = DescriptionKeysCache.add("ColumnDetails", new DescriptionKey("cPosition ColumnAttributes", 1));
		ca.addChild(cd);

		DescriptionKey cv = DescriptionKeysCache.add("ConstValue", new DescriptionKey("String", 47));
		constass.addChild(cv);
		constass.addChild(tc);
	}
}
