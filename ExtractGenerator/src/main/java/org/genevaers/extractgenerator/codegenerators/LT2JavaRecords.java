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

    private static List<ExtractorEntry> exrecs = new ArrayList<>();
    private static List<String> inputDDnames = new ArrayList<>();
    private static int outputLength;
    private static int lrLength;
    private static Stack<ExtractRecordGenerator> gotos = new Stack<>();
    private static int endScopeRow;

    private static Map<String, JoinGenerator> joins = new HashMap<>();

    private static int lookupFieldLength;

    public static ExtractorEntry processRecord(LTRecord lt) {
        String fc = lt.getFunctionCode();
        switch(fc) {
            case "RENX":
                RENXGenerator reg = new RENXGenerator();
                reg.processRecord(lt);
                inputDDnames.addAll(reg.getInputs());
                break;
            case "NV":
                NVGenerator nv = new NVGenerator();
                exrecs.add(nv.processRecord(lt));
                outputLength = nv.getOutputLength();
                lrLength =  nv.getLrLength();
                break;
            case "DTC":
                DTCGenerator dtc = new DTCGenerator();
                exrecs.add(dtc.processRecord(lt));
                break;
            case "DTE":
                DTEGenerator dte = new DTEGenerator();
                exrecs.add(dte.processRecord(lt));
                break;
            case "DTL":
                DTLGenerator dtl = new DTLGenerator();
                exrecs.add(dtl.processRecord(lt));
                //do we need to stack them
                lookupFieldLength = dtl.getFieldLength();
                break;
            case "CFEC":
                CFECGenerator cfec = new CFECGenerator();
                exrecs.add(cfec.processRecord(lt));
                gotos.push(cfec);
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
                exrecs.add(join.processRecord(lt));
                joins.computeIfAbsent(join.getNewid(), id -> addJoin(join));
                endScopeRow = join.getFalseRow();
                gotos.push(join);
                break;
            case "LKE":
                LKEGenerator lke = new LKEGenerator();
                exrecs.add(lke.processRecord(lt));
                break;
            case "LUSM":
                LUSMGenerator lusm = new LUSMGenerator();
                exrecs.add(lusm.processRecord(lt));
                break;
            case "GOTO":
                GOTOGenerator gotofc = new GOTOGenerator();
                if(gotos.size() > 0) {
                    if(gotos.peek().getLt().getGotoRow2() == lt.getRowNbr()+1) {
                        ExtractRecordGenerator gotFrom = gotos.pop();
                        gotofc.setFrom(gotFrom);
                    }
                }
                exrecs.add(gotofc.processRecord(lt));
                endScopeRow = gotofc.getEndScopeRow();
                break;
            case "WRDT":
                WRDTGenerator wrdt = new WRDTGenerator();
                wrdt.setOutputLength(outputLength);
                exrecs.add(wrdt.processRecord(lt));
                break;
            case "ES":
                ESGenerator es = new ESGenerator();
                exrecs.add(es.processRecord(lt));
                break;
            case "EN":
                ENGenerator en = new ENGenerator();
                exrecs.add(en.processRecord(lt));
                break;
            default:
                logger.atInfo().log("%s not handled", fc);
            break;
        }
        if(lt.getRowNbr() > 0 && lt.getRowNbr() == endScopeRow) {
            exrecs.add(new ExtractorEntry("        }"));  //will need a stack here? And manage indent
        }
        return null;
    }

    private static JoinGenerator addJoin(JoinGenerator join) {
        return join;
    }

    public static List<ExtractorEntry> getExrecs() {
        return exrecs;
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