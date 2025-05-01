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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.LtCompareType;
import org.genevaers.repository.components.enums.LtRecordType;
import org.genevaers.utilities.GersFile;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

/**
 * Text logger for the contents of a Logic Table
 * 
 * Perhaps a better way of implementing this is to add a log interface to each 
 * function code type.
 * Then implement the function for the possible codes within?
 * That is how the C++ version does it.
 * It may also be that we could add the logger to part of the Component Generation?
 */
public class LTLogger {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

	private static final String ROW_WIDTH ="7";
	private static final String COLUMN_WIDTH ="5";
	private static final String LEAD_IN = " %" + ROW_WIDTH + "d %" + COLUMN_WIDTH + "d %-4s";
	
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

	private static final String ARGLFLRFIELD = "%7d %7d %7d ";
	private static final String ARGTYPESIGNANDDATE = "%1s (%s/%d,%d %s)";

	private static final String ARGCOLVALUES = "%4d %3d %s (%s/%d,%d %s)";
	private static final String ARGCOLREF = "%2s %3d %3d %3d ";
	private static final String KEYVALUES = "     %3d %s (%s/%d,%d %s)";
	private static final String LEAD2GOTOS = "%-120s %s";
	private static final String LKLR2GOTOS = "%-23s %-95s %s";
	private static final String GOTOS = "%4d %4d";
	private static final String AGOTO = "%-120s %4d";
	private static final String JOIN = " %d -> \"%s\" %d/%d"; // 3296 -> "1" C++ gets it wrong so use this
	// "1"
																											// 10203/10245
	private static final String REEX = "%s %d, User Exit ID=%d";
	private static final String LUEX = "%s  , User Exit ID=%d";

	private static final String COMPARISON = "%s %47s  %s  %-47s %s";  //leading LHS op RHS gotos

	private static final String CCCOMP = "%s %-48s %s %-46s %s";
	private static final String DECLARATION = "%s Declare %s  = 0";
	private static final String ASSIGNMENT = "%s %47s  ->  %-47s";
	private static final String CONSTASSIGNMENT = "%s %47s  ->  %s";
	private static final String ARITHOP = "%s %s  ->  %s";
	private static final String CTASSIGNMENT = "%-68s <-  %s";
	private static final String ACCUMASSIGNMENT = "%s %47s  =  %s";
	private static final String ACCUMAOP = "%s %s %s %s";
	private static final String FNCC = "%s %s %s %s -> %s";
	private static final String WRSU = "%s Dest=%s, Buffered records=%d, Partition ID=%d, Prog ID = %d, Param = '%s'";
	private static final String WRSUNOPF = "%s Dest=%s, Buffered records=%d";
	private static final String WRDT = "%s Dest=%s, Partition ID=%d, Prog ID = %d, Param = '%s'";
	private static final String WRDESTONLY = "%s Dest=%s";
	private static final String FILEID = "%s %d";
	private static final String CFA = "%s %47s  %s %-47s %s";

	private static Map<LtRecordType, LtRecordLogger> typeMap;
	static {
		typeMap = new HashMap<>();
		// typeMap.put(LtRecordType.CALC, "Calc");
		// typeMap.put(LtRecordType.CC, "CC");
		// typeMap.put(LtRecordType.F0, "F0");
		typeMap.put(LtRecordType.F1, new F1LtLogger());
		typeMap.put(LtRecordType.F2, new F2LtLogger());
		// typeMap.put(LtRecordType.F2, "F2");
		typeMap.put(LtRecordType.GENERATION, new GenerationLtEntry());
		typeMap.put(LtRecordType.HD, new HdEntry());
		// typeMap.put(LtRecordType.NAME, "NAME");
		// typeMap.put(LtRecordType.NAMEF1, "NAMEF1");
		// typeMap.put(LtRecordType.NAMEF2, "NAMEF2");
		// typeMap.put(LtRecordType.NAMEVALUE, "NAMEVALUE");
		typeMap.put(LtRecordType.NV, new NvLtEntry());
		typeMap.put(LtRecordType.RE, new ReLtLogger());
		typeMap.put(LtRecordType.WR, new WrLtLogger());
	}

	private static Map<String, String> descriptionCache = new TreeMap<>();

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
			boolean newWay = true;
			String logme = newWay ? getLogEntry(ltr) : getLogString(ltr);
			out.write(logme + System.lineSeparator());
		}
		out.write("\nEnd of LT Records");
	}

	private static String getLogEntry(LTRecord ltr) {
		LtRecordLogger ltrl = typeMap.get(ltr.getRecordType());
		if(ltrl != null) {
			return ltrl.getLogEntry(ltr);
		} else {
			return ltr.getFunctionCode() + "(" + ltr.getRecordType() + ") Logger not defined";
		}
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
				return (String.format(CONSTASSIGNMENT, leadin, getArgConst(lks.getArg()), getArgKeyDetails(lks.getArg())));
			case "LKE":
				LogicTableF2 lke = (LogicTableF2) ltr;
				return (String.format(CONSTASSIGNMENT, leadin, getFullArg(lke.getArg1()), getArgKeyDetails(lke.getArg2())));
			case "LKC":
				LogicTableF1 lkc = (LogicTableF1) ltr;
				return (String.format(CONSTASSIGNMENT, leadin, getArgConst(lkc.getArg()), getArgKeyDetails(lkc.getArg())));
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
			case "SFAA": 
			case "SFAC": 
			case "CFAA": 
			case "CFAC": {
					LogicTableNameValue cfa = (LogicTableNameValue) ltr;
			return(String.format(CFA, leadin, cfa.getTableName(), cfa.getCompareType(), cfa.getValue().getPrintString(), getGotos(ltr)));
			}
			case "CFCX":
			case "SFCX": {
				LogicTableF1 cx = (LogicTableF1) ltr;
				return(String.format(COMPARISON, leadin, cx.getArg().getValue().getPrintString(), getCompareCode(cx.getCompareType()), getColRefArgDetails(cx.getArg()) , getGotos(ltr)));
			}
			case "CFXC":
			case "SFXC": {
				LogicTableF1 cx = (LogicTableF1) ltr;
				return(String.format(COMPARISON, leadin, getColRefArgDetails(cx.getArg()), getCompareCode(cx.getCompareType()), cx.getArg().getValue().getPrintString(), getGotos(ltr)));
			}
			case "CFXX":
			case "SFXX": {
				LogicTableF2 xx = (LogicTableF2) ltr;
				return(String.format(COMPARISON, leadin, getColRefArgDetails(xx.getArg1()), getCompareCode(xx.getCompareType()), getColRefArgDetails(xx.getArg2()), getGotos(ltr)));
			}
			case "SFCE": 
			case "SFCL": 
			case "CFCE": 
			case "CFCL": {
					LogicTableF1 cf = (LogicTableF1) ltr;
				return(String.format(COMPARISON, leadin, cf.getArg().getValue().getPrintString(), getCompareCode(cf.getCompareType()), getFullArg(cf.getArg()) , getGotos(ltr)));
			}
			case "SFEC":
			case "SFLC":
			case "CFEC":
			case "CFLC":
				LogicTableF1 cf = (LogicTableF1) ltr;
				return(String.format(COMPARISON, leadin, getFullArg(cf.getArg()), getCompareCode(cf.getCompareType()), cf.getArg().getValue().getPrintString(), getGotos(ltr)));
			case "SFEE":
			case "SFEL":
			case "SFLE":
			case "CFEE":
			case "CFPP":
			case "CFPL":
			case "CFEL":
			case "CFLE":
			case "CFLP":
				LogicTableF2 cfee = (LogicTableF2) ltr;
				return(String.format(COMPARISON, leadin, getFullArg(cfee.getArg1()), getCompareCode(cfee.getCompareType()),	getFullArg(cfee.getArg2()), getGotos(ltr)));
			case "SFCC":
			case "CFCC":
				LogicTableCC cfcc = (LogicTableCC) ltr;
			return(String.format(CCCOMP, leadin, cfcc.getValue1().getPrintString(), getCompareCode(cfcc.getCompareType()),	cfcc.getValue2().getPrintString(), getGotos(ltr)));
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
				return(String.format(ASSIGNMENT, leadin, dta .getAccumulatorName(), getColArgDetails(dta.getArg())));
			case "CTA":
				LogicTableNameF1 ct = (LogicTableNameF1) ltr;
				return(String.format(CTASSIGNMENT, leadin, ct.getAccumulatorName()));
			case "SFEA":
			case "SFLA":
			case "SFPA":
			case "SFXA":
			case "CFEA":
			case "CFLA":
			case "CFPA":
				LogicTableNameF1 cfea = (LogicTableNameF1) ltr;
				return(String.format(COMPARISON, leadin, getFullArg(cfea.getArg()), getCompareCode(cfea.getCompareType()), cfea.getAccumulatorName(), getGotos(ltr)));
			case "CFXA":
				LogicTableNameF1 cfxa = (LogicTableNameF1) ltr;
				return(String.format(COMPARISON, leadin, getColRefArgDetails(cfxa.getArg()), getCompareCode(cfxa.getCompareType()), cfxa.getAccumulatorName(), getGotos(ltr)));
			case "CFEX":
			case "CFPX":
			case "CFLX":
			case "SFEX":
			case "SFPX":
			case "SFLX":
				LogicTableF2 sfex = (LogicTableF2) ltr;
				return(String.format(COMPARISON, leadin, getFullArg(sfex.getArg1()), getCompareCode(sfex.getCompareType()), getColRefArgDetails(sfex.getArg2()), getGotos(ltr)));
			case "CFXE":
			case "CFXP":
			case "CFXL":
			case "SFXE":
			case "SFXP":
			case "SFXL":
				LogicTableF2 sfxe = (LogicTableF2) ltr;
				return(String.format(COMPARISON, leadin, getColRefArgDetails(sfxe.getArg1()),  getCompareCode(sfxe.getCompareType()), getFullArg(sfxe.getArg2()), getGotos(ltr)));
			case "SFAE":
			case "SFAL":
			case "SFAP":
			case "SFAX":
			case "CFAE":
			case "CFAL":
			case "CFAP":
				LogicTableNameF1 cfae = (LogicTableNameF1) ltr;
				return(String.format(COMPARISON, leadin, cfae.getAccumulatorName(), getCompareCode(cfae.getCompareType()), getFullArg(cfae.getArg()), getGotos(ltr)));
			case "CFAX":
				LogicTableNameF1 cfax = (LogicTableNameF1) ltr;
				return(String.format(COMPARISON, leadin, cfax.getAccumulatorName(), getCompareCode(cfax.getCompareType()), getColRefArgDetails(cfax.getArg()), getGotos(ltr)));
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
			case "DTE":
			case "SKE":
				LogicTableF2 ass = (LogicTableF2) ltr;
				return(String.format(ASSIGNMENT, leadin, getFullArg(ass.getArg1()), getColArgDetails(ass.getArg2())));
			case "DTX":
			case "SKX":
				LogicTableF2 xass = (LogicTableF2) ltr;
				return(String.format(ASSIGNMENT, leadin, getColRefArgDetails(xass.getArg1()), getColArgDetails(xass.getArg2())));
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
		return String.format(ARGLFLRFIELD, a.getLogfileId(), a.getLrId(), a.getFieldId());
	}

	private static String getArgDetails(LogicTableArg a) {
		return String.format(ARGCOLVALUES, a.getStartPosition(), a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
				a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId());
	}

	private static String getColArgDetails(LogicTableArg a) {
		return String.format(ARGCOLVALUES, a.getStartPosition(), a.getFieldLength(), getDataTypeLetter(a.getFieldFormat()),
				a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId() + getAlignmentLetter(a));
	}

	private static String getColRefArgDetails(LogicTableArg a) {
		return getColRef(a) + getSignAndDateDetails(a);
	}

	private static String getColRef(LogicTableArg a) {
		return String.format(ARGCOLREF, getExtractArea(a), a.getFieldId(), a.getStartPosition(), a.getFieldLength());
	}

	private static String getSignAndDateDetails(LogicTableArg a) {
		return String.format(ARGTYPESIGNANDDATE, getDataTypeLetter(a.getFieldFormat()), a.isSignedInd() ? "S" : "U", a.getRounding(), a.getDecimalCount(), a.getFieldContentId() + getAlignmentLetter(a));
	}

	private static String getExtractArea(LogicTableArg a) {
		switch (a.getLogfileId()) { //file id used as extract area code
			case 1:
				return "SK";
			case 2:
				return "SKT";
			case 3:
				return "DT";
			case 4:
				return "CT";
			default:
				return "NA";
		}
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

	private static String getCompareCode(LtCompareType comp) {
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

}
