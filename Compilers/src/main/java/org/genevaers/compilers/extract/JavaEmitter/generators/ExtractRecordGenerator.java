package org.genevaers.compilers.extract.JavaEmitter.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.extract.JavaEmitter.ExtractorEntry;
import org.genevaers.compilers.extract.astnodes.BooleanAndAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.ExtractFilterAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.IfAST;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.PFAstNode;
import org.genevaers.compilers.extract.astnodes.SelectIfAST;
import org.genevaers.compilers.extract.astnodes.StatementList;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.compilers.extract.astnodes.ViewColumnSourceAstNode;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;
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

    protected static List<String> filterRecs = new ArrayList<>();
    protected static List<String> columnRecs = new ArrayList<>();
    protected static List<String> inputDDnames = new ArrayList<>();
    protected static int outputLength;
    protected static int lrLength;
    protected static int endScopeRow;
    protected static int lookupFieldLength;

    protected static Map<Integer, LookupInfo> joins = new HashMap<>();
    protected static Map<Integer, LookupInfo> filterLookupIds = new TreeMap<>();
    protected static Map<Integer, LookupInfo> hiddenLookupIds = new TreeMap<>();
    protected static Map<Integer, LookupInfo> columnLookupIds = new TreeMap<>();

    private static boolean selectionFilterFound;

    public abstract void generateCode();

    public LTRecord getLt() {
        return lt;
    }

    protected void generateFromChildNodes(ExtractBaseAST node) {
        Iterator<ASTBase> ci = node.getChildIterator();
        while(ci.hasNext()) {
            ASTBase n = ci.next();
            ExtractRecordGenerator cg = getcodeGenerator((ExtractBaseAST)n);
            if(cg != null) {
                cg.generateCode();
            } else {
                logger.atInfo().log("No code generator found for node type %s", ((ExtractBaseAST)n).getType());
            }
        }
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

    protected ExtractRecordGenerator getcodeGenerator(ExtractBaseAST node) {
        if (node != null) {
            switch (node.getType()) {
        //         case EBASE:
        //             dotRoot();
        //             break;
                case LF:
                    return new LFGenerator((LFAstNode) node);
                case PF:
                    return new PFGenerator((PFAstNode) node);
                 case VIEWSOURCE:
                    return new ViewSourceGenerator((ViewSourceAstNode) node);
                 case VIEWCOLUMNSOURCE:
                    return new ViewColumnSourceGenerator((ViewColumnSourceAstNode) node)    ;
        //         case NUMATOM:
        //             dotNumAtomNode(node);
        //             break;
                case STRINGATOM:
                    return new StringAtomGenerator((StringAtomAST) node);
        //         case STRINGCONCAT:
        //             doStringConcat(node);
        //             break;
        //         case LEFT:
        //         case RIGHT:
        //             doStringFunction(node);
        //             break;
        //         case SUBSTR:
        //             doSubStringFunction(node);
        //             break;
                case COLUMNASSIGNMENT:
                    return new ColumnAssignmentGenerator((ColumnAssignmentASTNode)node);
        //         case PRIORLRFIELD:
        //             dotPriorLrFieldNode(node);
        //             break;
        //         case DATATYPE:
        //             dotDatatype(node);
        //             break;
        //         case DT_COLUMN:
        //             dotColumnNode(node);
        //             break;
        //         case SK_COLUMN:
        //             dotSKColumnNode(node);
        //             break;
        //         case CT_COLUMN:
        //             dotColumnNode(node);
        //             break;
        //         case COLUMNREF:
        //             //dotColumnNode(node);
        //             dotColumnRefNode(node);
        //             break;
        //         case NUMACC:
        //             dotNumericAccumNode(node);
        //             break;
        //         case ERRORS:
        //             dotErrorNode(node);
        //             break;
                case SELECTIF:
                    return new SelectIfGenerator((SelectIfAST) node);
                case BOOLAND:
                    return new BooleanAndGenerator((BooleanAndAST)node);
                case LRFIELD:
                    return new LRFieldGenerator((FieldReferenceAST) node);
        //         case SKIPIF:
                case IFNODE:
                    return new IfNodeGenerator((IfAST) node);
        //         case BOOLAND:
        //         case ISFOUND:
        //         case ISNOTFOUND:
        //             dotFrameworkNode(node);
        //             break;
        //         case ISNULL:
        //         case ISNUMERIC:
        //         case ISSPACES:
        //         case ISNOTNULL:
        //         case ISNOTNUMERIC:
        //         case ISNOTSPACES:
        //             doFunctionNode(node);
        //             break;
        //         case CAST:
        //             dotCast(node);
        //             break;
        //         case SYMBOL:
        //             dotSymbolNode(node);
        //             break;
        //         case WRITESOURCEARG:
        //             dotWriteSourceNode(node);
        //             break;
        //         case WRITEEXTRACT:
        //             dotWriteExtractNode(node);
        //             break;
        //         case WRITEFILE:
        //             dotWriteFileNode(node);
        //             break;
                case EXPRCOMP:
                    return new ExprComparisonGenerator((ExprComparisonAST) node);
        //         case RUNDATE:
        //             dotRundate(node);
        //             break;
        //         case UNARYINT:
        //             dotUnaryInt(node);
        //             break;
        //         case SORTTITLE:
        //             doSortTitle(node);
        //             break;
        //         case CALCULATION:
        //         case RECORD_COUNT:
        //         case ADDITION:
        //         case SUBTRACTION:
        //         case SETTER:
        //         case MULTIPLICATION:
        //         case DIVISION:
        //             doCalculation(node);
        //             break;
        //         case EOS:
        //             doEOS(node);
        //             break;
        //         case LOOKUPREF:
        //             dotLookupNode(node);
        //             break;
                case LOOKUPFIELDREF:
                    return new LookupFieldRefGenerator((LookupFieldRefAST)node);
                    //break;
        //         case DATEFUNC:
        //             doDateFunc(node);
        //             break;
                case EXTRFILTER:
                    return new ExtractFilterGenerator((ExtractFilterAST) node);
                case STATEMENTLIST:
                    return new StatementListGenerator((StatementList) node);
        //         case ALL:
        //             doAll(node);
        //             break;
        //         default:
        //             dotDefaultNode(node);
        //             break;
        //     }
        //     if (nodeEnabled) {
        //         fw.write(idString + "[label=\"" + label + "\" " + "color=" + colour + " shape=" + shape
        //                 + " style=filled]\n");
                default:
                    logger.atInfo().log("No code generator found for node type %s", node.getType());
                    return new TBDGenerator();
            }
        }
        return null;
    }

    public String getCode(ExtractBaseAST node) {
            switch (node.getType()) {
                case BOOLAND:
                    BooleanAndGenerator bandgen = new BooleanAndGenerator((BooleanAndAST)node);
                    return bandgen.getCode(node);
                case COLUMNASSIGNMENT:
                    ColumnAssignmentGenerator cagen = new ColumnAssignmentGenerator((ColumnAssignmentASTNode)node);
                    return cagen.getCode(node);
                default:
                    logger.atInfo().log("No code generated for node type %s", node.getType());
            }
        return "No code generated for node " + node.getClass().getSimpleName();
    }

    public static List<String> getFilterRecs() {
        return filterRecs;
    }

    public static List<String> getColumnRecs() {
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

    public static  Collection<LookupInfo> getJoins() {
        return joins.values();
    }

    public static Map<Integer, LookupInfo> getColumnLookupIds() {
        return columnLookupIds;
    }

    public static Map<Integer, LookupInfo> getFilterLookupIds() {
        return filterLookupIds;
    }

    public static Map<Integer, LookupInfo> getHiddenLookupIds() {
        return hiddenLookupIds;
    }

}