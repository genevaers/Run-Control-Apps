package org.genevaers.extractgenerator.codegenerators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTable;

import com.google.common.flogger.FluentLogger;

public abstract class ExtractRecordGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    protected LTRecord lt;
    protected static boolean dteAggregationInProgress = false;
    protected static LogicTable xlt;
    protected static int currentRow = 0;
    protected static int currentColumnNumber;
    protected List<ExtractorEntry> exrecs = new ArrayList<>();

    protected static List<ExtractorEntry> filterRecs = new ArrayList<>();
    protected static List<ExtractorEntry> columnRecs = new ArrayList<>();
    protected static List<String> inputDDnames = new ArrayList<>();
    protected static int outputLength;
    protected static int lrLength;
    protected static int endScopeRow;
    protected static int lookupFieldLength;

    protected static Map<String, JoinGenerator> joins = new HashMap<>();

    private static boolean selectionFilterFound;

    public abstract ExtractorEntry processRecord(LTRecord lt);
    public String generateCode() { return ""; }

    public LTRecord getLt() {
        return lt;
    }

    public static boolean isDteAggregationInProgress() {
        return dteAggregationInProgress;
    }

    public static void setXLTandLookaheadForSelectFilter(LogicTable xlt) {
        ExtractRecordGenerator.xlt = xlt;
        lookaheadForSelectFilter(xlt);
    }

    protected static void lookaheadForSelectFilter(LogicTable xlt){
        LTRecord lt = xlt.getFromPosition(4);
        String fc = lt.getFunctionCode();
        if(lt.getSuffixSeqNbr() == 0 && (!fc.equals("EN") && !fc.equals("ES") && !fc.startsWith("WR"))) {
            logger.atInfo().log("Lookahead found selection filter at row %d", currentRow);
            selectionFilterFound = true;
        } else {
            logger.atInfo().log("No selection filter found in XLT, treating all logic as column logic");
        }

    }

    public static boolean notAtEndOfXLT() {
        return currentRow < xlt.getNumberOfRecords();
    }

    public static LTRecord addToExtractEntiries() {
        LTRecord lt = xlt.getFromPosition(currentRow);
        currentRow++;
        String fc = lt.getFunctionCode();
        currentColumnNumber = lt.getSuffixSeqNbr();
        //Want to track header function codes but not add to filter unless there is a filter section
        //Use lookahead?    
        if(selectionFilterFound) {
            if(currentColumnNumber == 0 && (!fc.equals("EN") && !fc.equals("ES") && !fc.startsWith("WR"))) {
                addToSectionFilter(lt);
            } else {
                addToColumnLogic(lt);
            }
        } else {
            addToColumnLogic(lt);
        }   
        return null;
    }
    private static void addToColumnLogic(LTRecord lt) {
        ExtractorEntry exe = addFunctionCode(lt, columnRecs, FunctionSection.COLUMN);
        columnRecs.add(exe);
    }

    private static void addToSectionFilter(LTRecord lt) {
        //A selection filter contains only predicates that determine if a record is to be included in the extract
        //Conditions of fields, ISFOUNDS, and Lookup fields.
        //Can also have arithmetic conditions

        // Produce code along the lines of -> Look ahead via gotos to the XLT for target row... column or still in filter
        // OR means
        // if( condition) {
        //    columnLogic()
        // }
        // if( condition2) {
        //    columnLogic()
        // }
        // And AND means
        // if( condition1) {    
        //    if( condition2) {
        //        columnLogic()        
        //    }
        // }
        ExtractorEntry exe = addFunctionCode(lt, filterRecs, FunctionSection.FILTER);
        filterRecs.add(exe);
    }

    public static ExtractorEntry addFunctionCode(LTRecord lt, List<ExtractorEntry> entryList, FunctionSection section) {
        ExtractorEntry exe = null;
        String fc = lt.getFunctionCode();
        if(ExtractRecordGenerator.isDteAggregationInProgress() && !fc.equals("DTE")) {
            //we should remove the last added DTE and replace with aggregated one
            logger.atInfo().log("Finalising DTE aggregation before processing %s at row %d", fc, lt.getRowNbr());
        }
        switch(fc) {
            case "RENX":
                RENXGenerator reg = new RENXGenerator();
                reg.processRecord(lt);
                inputDDnames.addAll(reg.getInputs());
                break;
            case "NV":
                NVGenerator nv = new NVGenerator();
                exe = nv.processRecord(lt);
                break;
            case "DTC":
                DTCGenerator dtc = new DTCGenerator();
                exe = dtc.processRecord(lt);
                break;
            case "DTE":
                DTEGenerator dte = new DTEGenerator();
                exe = dte.processRecord(lt);
                break;
            case "DTL":
                DTLGenerator dtl = new DTLGenerator();
                exe = dtl.processRecord(lt);
                //do we need to stack them
                lookupFieldLength = dtl.getFieldLength();
                break;
            case "CFEC":
                CFECGenerator cfec = new CFECGenerator(section);
                exe = cfec.processRecord(lt);
                break;
            case "JOIN":
                JoinGenerator join = new JoinGenerator(section);
                //Collect the Reference information needed
                // Input DDName - record len, key Len, so we can populate the Map of data
                //We also need to generate the if logic that is a JOIN - three way
                //Flag on if reference data already read for this event record
                //
                // REH has LF/LR ID
                // Record Length and key length
                // Data file number
                // should key the joins map on lf/lr 
//                entryList.add(join.processRecord(lt));
                exe = join.processRecord(lt);
                joins.computeIfAbsent(join.getNewid(), id -> addJoin(join));
                break;
            case "LKE":
                LKEGenerator lke = new LKEGenerator();
                exe = lke.processRecord(lt);
                break;
            case "LUSM":
                LUSMGenerator lusm = new LUSMGenerator();
                exe = lusm.processRecord(lt);
                break;
            case "GOTO":
                GOTOGenerator gotofc = new GOTOGenerator();
                break;
            case "WRDT":
                WRDTGenerator wrdt = new WRDTGenerator();
                wrdt.setOutputLength(outputLength);
                exe = wrdt.processRecord(lt);
                break;
            case "ES":
                ESGenerator es = new ESGenerator();
                exe = es.processRecord(lt);
                break;
            case "EN":
                ENGenerator en = new ENGenerator();
                exe = en.processRecord(lt);
                break;
            default:
                logger.atInfo().log("%s not handled", fc);
            break;
        }
        if(lt.getRowNbr() > 0 && lt.getRowNbr() == endScopeRow) {
            //entryList.add(new ExtractorEntry("        }"));  //will need a stack here? And manage indent
        }
        return exe != null ? exe : new ExtractorEntry(String.format("//(%d) %s",lt.getRowNbr(), fc));
        //  + fc);
         //return null;
    }

    private static JoinGenerator addJoin(JoinGenerator join) {
        return join;
    }

    public static List<ExtractorEntry> getFilterRecs() {
        return filterRecs;
    }

    public static List<ExtractorEntry> getColumnRecs() {
        return columnRecs;
    }

    public static List<String> getInputDDnames() {
        return inputDDnames;
    }

    public static int getOutputLength() {
        return outputLength;
    }

    public static int getLrLength() {
        return lrLength;
    }

    public static  Collection<JoinGenerator> getJoins() {
        return joins.values();
    }
}