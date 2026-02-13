package org.genevaers.extractgenerator.codegenerators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

import org.genevaers.genevaio.ltfile.LTRecord;
import com.google.common.flogger.FluentLogger;

public class LT2JavaRecords {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static List<ExtractorEntry> filterRecs = new ArrayList<>();
    private static List<ExtractorEntry> columnRecs = new ArrayList<>();
    private static List<String> inputDDnames = new ArrayList<>();
    private static int outputLength;
    private static int lrLength;
    private static Stack<EndScope> endScopes = new Stack<>();
    private static int endScopeRow;

    private static Map<String, JoinGenerator> joins = new HashMap<>();

    private static int lookupFieldLength;

    private static int currentColumnNumber = -1;

    public static ExtractorEntry processRecord(LTRecord lt) {
        String fc = lt.getFunctionCode();
        currentColumnNumber = lt.getSuffixSeqNbr();
        if(currentColumnNumber == 0 && (!fc.equals("EN") && !fc.equals("ES") && !fc.startsWith("WR"))) {
            addToSectionFilter(lt);
        } else {
            addToColumnLogic(lt);
        }
        return null;
    }

    private static void addToColumnLogic(LTRecord lt) {
        addFunctionCode(lt, columnRecs, FunctionSection.COLUMN);
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
        addFunctionCode(lt, filterRecs, FunctionSection.FILTER);
    }

    public static ExtractorEntry addFunctionCode(LTRecord lt, List<ExtractorEntry> entryList, FunctionSection section) {

        String fc = lt.getFunctionCode();
        if(ExtractRecordGenerator.isDteAggregationInProgress() && !fc.equals("DTE")) {
            //we should remove the last added DTE and replace with aggregated one
            logger.atInfo().log("Finalising DTE aggregation before processing %s at row %d", fc, lt.getRowNbr());
            //exrecs.remove(exrecs.size()-1);
            entryList.add(DTEGenerator.getAggregatedDTE());
        }
        switch(fc) {
            case "RENX":
                RENXGenerator reg = new RENXGenerator();
                reg.processRecord(lt);
                inputDDnames.addAll(reg.getInputs());
                break;
            case "NV":
                NVGenerator nv = new NVGenerator();
                entryList.add(nv.processRecord(lt));
                outputLength = nv.getOutputLength();
                lrLength =  nv.getLrLength();
                break;
            case "DTC":
                DTCGenerator dtc = new DTCGenerator();
                entryList.add(dtc.processRecord(lt));
                break;
            case "DTE":
                DTEGenerator dte = new DTEGenerator();
                ExtractorEntry ee = dte.processRecord(lt);
                if(ee != null) {    
                    entryList.add(ee);
                }
                break;
            case "DTL":
                DTLGenerator dtl = new DTLGenerator();
                entryList.add(dtl.processRecord(lt));
                //do we need to stack them
                lookupFieldLength = dtl.getFieldLength();
                break;
            case "CFEC":
                CFECGenerator cfec = new CFECGenerator(section);
                ExtractorEntry exe = cfec.processRecord(lt);
                entryList.add(exe);
                //endScopes.push(cfec.getEndScope());
                break;
            case "JOIN":
                JoinGenerator join = new JoinGenerator();
                //Collect the Reference information needed
                // Input DDName - record len, key Len, so we can populate the Map of data
                //We also need to generate the if logic that is a JOIN - three way
                //Flag on if reference data already read for this event record
                //
                // REH has LF/LR ID
                // Record Length and key length
                // Data file number
                // should key the joins map on lf/lr 
                entryList.add(join.processRecord(lt));
                joins.computeIfAbsent(join.getNewid(), id -> addJoin(join));
                endScopes.push(join.getNotFoundEndScope());
                endScopes.push(join.getEndScope());
                break;
            case "LKE":
                LKEGenerator lke = new LKEGenerator();
                entryList.add(lke.processRecord(lt));
                break;
            case "LUSM":
                LUSMGenerator lusm = new LUSMGenerator();
                entryList.add(lusm.processRecord(lt));
                break;
            case "GOTO":
                GOTOGenerator gotofc = new GOTOGenerator();
                // if(endScopes.size() > 0) {
                //     if(endScopes.peek().getLt().getGotoRow2() == lt.getRowNbr()+1) {
                //         ExtractRecordGenerator gotFrom = endScopes.pop();
                //         gotofc.setFrom(gotFrom);
                //     }
                // }
                // entryList.add(gotofc.processRecord(lt));
                // endScopeRow = gotofc.getEndScopeRow();
                break;
            case "WRDT":
                WRDTGenerator wrdt = new WRDTGenerator();
                wrdt.setOutputLength(outputLength);
                entryList.add(wrdt.processRecord(lt));
                break;
            case "ES":
                ESGenerator es = new ESGenerator();
                entryList.add(es.processRecord(lt));
                break;
            case "EN":
                ENGenerator en = new ENGenerator();
                entryList.add(en.processRecord(lt));
                break;
            default:
                logger.atInfo().log("%s not handled", fc);
            break;
        }
        if(lt.getRowNbr() > 0 && lt.getRowNbr() == endScopeRow) {
            //entryList.add(new ExtractorEntry("        }"));  //will need a stack here? And manage indent
        }
        if(endScopes.size() > 0 && (lt.getRowNbr() == endScopes.peek().getTargetRow())) {
            //while(gotos.size() > 0 && (lt.getRowNbr() == gotos.peek().getLt().getGotoRow1() || lt.getRowNbr() == gotos.peek().getLt().getGotoRow2())) {
            EndScope es = endScopes.pop();
            switch(es.getType()) {
                case IF_END:
                    String endScopeSource = String.format("End of scope for Goto at row %d", lt.getRowNbr());
                    logger.atInfo().log(endScopeSource);
                    entryList.add(new ExtractorEntry(String.format("//%s\n        }", endScopeSource))); 
                    break;
                case JOIN_FOUND:
                    String joinEndScopeSource = String.format("End of scope for JOIN Found at row %d", lt.getRowNbr());
                    logger.atInfo().log(joinEndScopeSource);
                    entryList.add(new ExtractorEntry(String.format("//%s\n         else {", joinEndScopeSource))); 
                    break;
                case JOIN_NOT_FOUND:
                    String joinNFEndScopeSource = String.format("End of scope for JOIN Not Found at row %d", lt.getRowNbr());
                    logger.atInfo().log(joinNFEndScopeSource);
                    entryList.add(new ExtractorEntry(String.format("//%s\n        }", joinNFEndScopeSource))); 
                    break;
                default:
                    break;
            }
        }
        if(endScopes.size() > 0) {
            logger.atInfo().log("GOTO stack size %d top row %d fc %s", endScopes.size(), endScopes.peek().getTargetRow(), endScopes.peek().getType().name());
        }
        return new ExtractorEntry("//" + fc);
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