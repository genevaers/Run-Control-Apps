package org.genevaers.genevaio.ltfile;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.genevaers.repository.components.enums.DataType;
import org.genevaers.utilities.GersFile;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

public class LTLogger {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String LEAD_IN = " %7d %5d %-4s";
	private static final String NV_FORMAT = " %s  LR=%s SKA=%s, STA=%s, DTA=%s, CTC=%s Source Number=%s";
	private static final String GEN_FORMAT = "%s  Text %s\n"
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

	private static final String ARGLFLR = "%7d %7d %7d ";
	private static final String ARGVALUES = "%4d %3d %s (%s/%d,%d %s)";
	private static final String KEYVALUES = "     %3d %s (%s/%d,%d %s)";
	private static final String LEAD2GOTOS = "%-119s %s";
	private static final String LKLR2GOTOS = "%-23s %-95s %s";
	private static final String GOTOS = "%4d %4d";
	private static final String AGOTO = "%-119s %4d";
	private static final String OLDJOIN = " %d -> \"%s\" %d/%d"; // 3296 -> "1" 10203/10245
	private static final String JOIN = " %d -> \"%s\" %d/%d"; // 3296 -> "1" C++ gets it wrong so use this
	private static final String LUSM = "                                                         %5d %5d"; // 3296 ->
																											// "1"
																											// 10203/10245
	private static final String REEX = "%s %d, User Exit ID=%d";
	private static final String LUEX = "%s  , User Exit ID=%d";
	private static final String CECOMP = "%s  %-47s %s %s %s";
	private static final String ECCOMP = "%s %s  %s  %-46s %s";
	private static final String EECOMP = "%s %s  %s %-46s %s";
	private static final String CCCOMP = "%s %-48s  %s %-46s %s";
	private static final String DECLARATION = "%s Declare %s  = 0";
	private static final String ASSIGNMENT = "%s %s  ->  %s";
	private static final String KEYASSIGNMENT = "%s %s  ->  %s";
	private static final String CONSTASSIGNMENT = "%s %-47s  ->  %s";
	private static final String SYMASSIGNMENT = "%s %-47s  ->  %s";
	private static final String ACCUM2COLUMN = "%s %-47s  ->  %s";
	private static final String ARITHOP = "%s %s  ->  %s";
	private static final String CTASSIGNMENT = "%-68s <-  %s";
	private static final String ACCUMASSIGNMENT = "%s %-47s  =  %s";
	private static final String ACCUMAOP = "%s %s %s %s";
	private static final String FNCC = "%s %s %s %s -> %s";
	private static final String WRSU = "%s Dest=%s, Buffered records=%d, Partition ID=%d, Prog ID = %d, Param = '%s'";
	private static final String WRSUNOPF = "%s Dest=%s, Buffered records=%d";
	private static final String WRDT = "%s Dest=%s, Partition ID=%d, Prog ID = %d, Param = '%s'";
	private static final String WRDESTONLY = "%s Dest=%s";
	private static final String FILEID = "%s %d";
	private static final String CFA = "%s %-47s  %s %-47s %s";


	// Format strings for the parts
	// Format strings for the layout
	// Layout func code dependent and generally type dependent
	// leadin + gen
	// leadin + nv
	// leadin + join
	// leadin + gotos
	// leadin + goto
	// leadin + file id
	// leadin + assignment (different types)
	// leadin + comparison (different types) + gotos
	// leadin + declaration
	// leadin + arithmetic op
	// leadin + CT assignment
	// leadin + WR

	public static String logRecords(LogicTable lt) {
		StringBuilder sb = new StringBuilder();
		Iterator<LTRecord> lti = lt.getIterator();
		while (lti.hasNext()) {
			LTRecord ltr = lti.next();
			sb.append(getLogString(ltr) +"\n");
		}
		return sb.toString();
	}

	public static void writeRecordsTo(LogicTable lt, String ltPrint, String generation) {
		try (Writer out = new GersFile().getWriter(ltPrint);) {
			writeDetails(lt, out, generation);
		} catch (Exception e) {
			logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
		}
		logger.atInfo().log("%s LT report written", ltPrint);
	}

	private static void writeDetails(LogicTable lt, Writer out, String generation) throws IOException {
		out.write(String.format("Logic Table Report: %s\n\n", generation));
		Iterator<LTRecord> lti = lt.getIterator();
		while (lti.hasNext()) {
			LTRecord ltr = lti.next();
			String logme = getLogString(ltr);
			logger.atFine().log(logme);
			out.write(logme + "\n");
		}
		out.write("\nEnd of LT Records");
	}

	private static String getLogString(LTRecord ltr) {
		String leadin = getLeadin(ltr);
		switch (ltr.getFunctionCode()) {
			case "HD":
			case "EN":
				return(leadin);
			case "GEN":
				return(leadin + getGenDetails((LogicTableGeneration) ltr));
			case "REEX":
				LogicTableRE reex = (LogicTableRE) ltr;
				return("\n" + String.format(REEX, leadin, reex.getFileId(), reex.getReadExitId()));
			case "NV":
				return(getViewLine(ltr) + leadin + getNewViewDetails((LogicTableNV) ltr));
			case "JOIN":
				LogicTableF1 j = (LogicTableF1) ltr;
				LogicTableArg arg = j.getArg();
				return(String.format(LEAD2GOTOS,
										leadin + String.format(JOIN, j.getColumnId(), arg.getValue().getPrintString(), arg.getLogfileId(), arg.getLrId()),
										getGotos(ltr)));
			case "LUSM":
				return(String.format(LEAD2GOTOS, leadin, getGotos(ltr)));
			case "LKS":
				LogicTableF1 lks = (LogicTableF1) ltr;
				return (String.format(SYMASSIGNMENT, leadin, getArgConst(lks.getArg()), getArgKeyDetails(lks.getArg())));
			case "LKE":
				LogicTableF2 lke = (LogicTableF2) ltr;
				return (String.format(KEYASSIGNMENT, leadin, getFullArg(lke.getArg1()), getArgKeyDetails(lke.getArg2())));
			case "LKC":
				LogicTableF1 lkc = (LogicTableF1) ltr;
				return (String.format(SYMASSIGNMENT, leadin, getArgConst(lkc.getArg()), getArgKeyDetails(lkc.getArg())));
			case "LUEX":
				LogicTableRE luex = (LogicTableRE) ltr;
				return(String.format(LUEX, leadin, luex.getReadExitId()));
			case "LKLR":
				return(String.format(LKLR2GOTOS, leadin, getLKLRInfo(ltr), getGotos(ltr)));
			case "LKDC": {
				LogicTableF1 f1 = (LogicTableF1) ltr;
				return(leadin + "  " + f1.getArg().getValue().getPrintString());
			}
			case "KSLK": {
				return(leadin + "  " +  getKSLKInfo(ltr));
			}
			case "GOTO":
			LogicTableF0 agoto = (LogicTableF0) ltr;
			return(String.format(AGOTO, leadin, agoto.getGotoRow1()));
			case "CFAA": 
			case "CFAC": {
					LogicTableNameValue cfa = (LogicTableNameValue) ltr;
			return(String.format(CFA, leadin, cfa.getTableName(), cfa.getCompareType(), cfa.getValue(), getGotos(ltr)));
			}
			case "CFCE": 
			case "CFCL": {
				LogicTableF1 cf = (LogicTableF1) ltr;
				return(String.format(CECOMP, leadin, cf.getArg().getValue().getPrintString(), cf.getCompareType(), getFullArg(cf.getArg()) , getGotos(ltr)));
			}
			case "CFEC":
			case "CFLC":
				LogicTableF1 cf = (LogicTableF1) ltr;
				return(String.format(ECCOMP, leadin, getFullArg(cf.getArg()), cf.getCompareType(), cf.getArg().getValue().getPrintString(), getGotos(ltr)));
			case "CFEE":
			case "CFEL":
			case "CFLE":
				LogicTableF2 cfee = (LogicTableF2) ltr;
				return(String.format(EECOMP, leadin, getFullArg(cfee.getArg1()), cfee.getCompareType(),
						getFullArg(cfee.getArg2()), getGotos(ltr)));
			case "CFCC":
				LogicTableCC cfcc = (LogicTableCC) ltr;
			return(String.format(CCCOMP, leadin, cfcc.getValue1().getPrintString(), cfcc.getCompareType(),	cfcc.getValue2().getPrintString(), getGotos(ltr)));
			case "DIMN":
			case "DIM4":
				LogicTableName ln = (LogicTableName) ltr;
				return(String.format(DECLARATION, leadin, ln.getAccumulatorName()));
			case "SETC":
			case "ADDC":
			case "SUBC":
			case "MULC":
			case "DIVC":
				LogicTableNameValue setc = (LogicTableNameValue) ltr;
				return(String.format(ACCUMASSIGNMENT, leadin, setc.getTableName(), setc.getValue().getPrintString()));
			case "SETA":
				LogicTableNameValue seta = (LogicTableNameValue) ltr;
				return(String.format(ACCUMAOP, leadin, seta.getTableName(), "<-", seta.getValue().getPrintString()));
			case "ADDA":
				LogicTableNameValue adda = (LogicTableNameValue) ltr;
				return (String.format(ACCUMAOP, leadin, adda.getTableName(), "/", adda.getValue().getPrintString()));
			case "SUBA":
				LogicTableNameValue suba = (LogicTableNameValue) ltr;
				return (String.format(ACCUMAOP, leadin, suba.getTableName(), "-", suba.getValue().getPrintString()));
			case "DIVA":
				LogicTableNameValue diva = (LogicTableNameValue) ltr;
				return(String.format(ACCUMAOP, leadin, diva.getTableName(), "/", diva.getValue().getPrintString()));
			case "MULA":
				LogicTableNameValue mula = (LogicTableNameValue) ltr;
				return(String.format(ACCUMAOP, leadin, mula.getTableName(), "*", mula.getValue().getPrintString()));
			case "SETE":
			case "ADDE":
			case "DIVE":
			case "SUBE":
			case "MULE":
				LogicTableNameF1 se = (LogicTableNameF1) ltr;
				return(String.format(ARITHOP, leadin, getFullArg(se.getArg()), se.getAccumulatorName()));
			case "DTA":
				LogicTableNameF1 dta = (LogicTableNameF1) ltr;
				return(String.format(ACCUM2COLUMN, leadin, dta .getAccumulatorName(), getColArgDetails(dta.getArg())));
			case "CTA":
				LogicTableNameF1 ct = (LogicTableNameF1) ltr;
				return(String.format(CTASSIGNMENT, leadin, ct.getAccumulatorName()));
			case "CTC":
				LogicTableF1 ctc = (LogicTableF1) ltr;
				return(String.format(CTASSIGNMENT, leadin, ctc.getArg().getValue().getPrintString()));
			case "CTE":
				LogicTableF1 cte = (LogicTableF1) ltr;
				return(String.format(CTASSIGNMENT, leadin, getColArgDetails(cte.getArg()) ));
			case "WRSU": {
				LogicTableWR wr = (LogicTableWR) ltr;
				if(wr.getOutputFileId() == 0) {
					return(String.format(WRSUNOPF, leadin, getWrDest(wr), wr.getExtrSumRecCnt()));
				} else {
					return(String.format(WRSU, leadin, getWrDest(wr), wr.getExtrSumRecCnt(), wr.getOutputFileId(), wr.getWriteExitId(),wr.getWriteExitParms()));
				}
			}
			case "WRDT": 
			case "WRXT": {
				LogicTableWR wr = (LogicTableWR) ltr;
				if(wr.getOutputFileId() == 0) {
					if(wr.getWriteExitId() == 0) {
						return(String.format(WRDESTONLY, leadin, getWrDest(wr), wr.getExtrSumRecCnt()));
					} else {
						return(String.format(WRDT, leadin, getWrDest(wr), wr.getOutputFileId(), wr.getWriteExitId(),wr.getWriteExitParms()));
					}
				} else {
					return(String.format(WRDT, leadin, getWrDest(wr), wr.getOutputFileId(), wr.getWriteExitId(),wr.getWriteExitParms()));
				}
			}
			case "ET":
			case "ES":
				LogicTableF0 f0 = (LogicTableF0) ltr;
				return(String.format(FILEID, leadin, f0.getFileId()));
			case "DTC":
			case "SKC":
				LogicTableF1 dtc = (LogicTableF1) ltr;
				return(String.format(CONSTASSIGNMENT, leadin, getArgConst(dtc.getArg()), getColArgDetails(dtc.getArg())));
			case "FNCC":
				LogicTableNameF2 nf2 = (LogicTableNameF2) ltr;
				return(String.format(FNCC, leadin, nf2.getArg1().getValue().getPrintString(), nf2.getArg2().getValue().getPrintString(), "DaysBetween", nf2.getAccumulatorName()));
			default: {
				switch (ltr.getRecordType()) {
					case RE:
						LogicTableRE re = (LogicTableRE) ltr;
						return("\n" + leadin + String.format("  %d", re.getFileId()));
					case F1:
						String fc = ltr.getFunctionCode();
						if (fc.startsWith("CF") || fc.startsWith("SF")) {

						}
						LogicTableF1 f1 = (LogicTableF1) ltr;
						return(leadin + " \"" + f1.getArg().getValue().getPrintString() + "\"");
					case F2:
						LogicTableF2 f2 = (LogicTableF2) ltr;
						return(String.format(ASSIGNMENT, leadin, getFullArg(f2.getArg1()), getColArgDetails(f2.getArg2())));
					default:
						return(leadin + " More details?");
				}
			}
		}
	}

	private static Object getLKLRInfo(LTRecord ltr) {
		LogicTableF1 lklr = (LogicTableF1) ltr;
		return String.format("%d/%d %d -> \"%s\"", lklr.getArg().getLogfileId(), lklr.getArg().getLrId(), lklr.getColumnId(), lklr.getArg().getValue().getPrintString());
	}

	private static Object getKSLKInfo(LTRecord ltr) {
		LogicTableF1 kslk = (LogicTableF1) ltr;
		return String.format("%d/%d %d fld %d len %d", kslk.getArg().getLogfileId(), kslk.getArg().getLrId(), kslk.getColumnId(), kslk.getArg().getFieldId(), kslk.getArg().getFieldLength());
	}

	private static String getViewLine(LTRecord ltr) {
		return String.format("------------\nView %07d\n------------\n", ltr.getViewId());
	}

	private static Object getArgConst(LogicTableArg arg) {
		return "\"" + arg.getValue().getPrintString() + "\"";
	}

	private static String getWrDest(LogicTableWR wr) {
		switch(wr.getDestType()) {
			case 0:
			return "Extract";
			case 1:
			return "File";
			case 2:
			return "Token";
			default:
			return "Unknown";
		}
	}

	private static String getNewViewDetails(LogicTableNV nv) {
		return String.format(NV_FORMAT, nv.getViewType().value(),   nv.getSourceLrId(), nv.getSortKeyLen(),
				nv.getSortTitleLen(), nv.getDtAreaLen(), nv.getCtColCount(), nv.getSourceSeqNbr());
	}

	private static String getGenDetails(LogicTableGeneration g) {
		return String.format(GEN_FORMAT, g.isExtract() ? "Extract" : "Join",
				g.isIsAscii() ? "ASCII" : "EBCDIC",
				g.getReccnt(), g.getBytecnt(), g.getHdCnt(), g.getNvCnt(), g.getF0Cnt(), g.getF1Cnt(),
				g.getF2Cnt(), g.getReCnt(), g.getWrCnt(), g.getCcCnt(), g.getNameCnt(), g.getNamevalueCnt(),
				g.getCalcCnt(), g.getNamef1Cnt(), g.getNamef2Cnt(), g.getDateCc(), g.getDateYy(),
				g.getDateMm(), g.getDateDd(), 0, 0, 0);
				//g.getDateMm(), g.getDateDd(), g.getTimeHh(), g.getTimeHh(), g.getTimeSs());
	}

	private static String getGotos(LTRecord ltr) {
		return String.format(GOTOS, ltr.getGotoRow1(), ltr.getGotoRow2());
	}

	private static String getLeadin(LTRecord ltr) {
		return String.format(LEAD_IN, ltr.getRowNbr(), ltr.getSuffixSeqNbr(), ltr.getFunctionCode());
	}

	private static String getFullArg(LogicTableArg arg1) {
		return getArgLFLRData(arg1) + getArgDetails(arg1);
	}

	private static String getArgLFLRData(LogicTableArg a) {
		return String.format(ARGLFLR, a.getLogfileId(), a.getLrId(), a.getFieldId());
	}

	private static String getArgDetails(LogicTableArg a) {
		return String.format(ARGVALUES, a.getStartPosition(), a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
				a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId());
	}

	private static String getColArgDetails(LogicTableArg a) {
		return String.format(ARGVALUES, a.getStartPosition(), a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
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

	private static String getArgKeyDetails(LogicTableArg a) {
		return String.format(KEYVALUES, a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
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

}
