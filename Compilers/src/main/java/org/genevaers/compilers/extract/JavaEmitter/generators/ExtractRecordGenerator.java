package org.genevaers.compilers.extract.JavaEmitter.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.extract.JavaEmitter.ExtractorEntry;
import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ColumnFieldHolder;
import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.FieldHolder;
import org.genevaers.compilers.extract.astnodes.BooleanAndAST;
import org.genevaers.compilers.extract.astnodes.BooleanOrAST;
import org.genevaers.compilers.extract.astnodes.CalculationAST;
import org.genevaers.compilers.extract.astnodes.StringComparisonAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.ExtractFilterAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.IfAST;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.PFAstNode;
import org.genevaers.compilers.extract.astnodes.SelectIfAST;
import org.genevaers.compilers.extract.astnodes.StatementList;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.compilers.extract.astnodes.WriteASTNode;
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
    protected static int valueNumber = 0;
    protected List<ExtractorEntry> exrecs = new ArrayList<>();

    protected String constName;

    protected static Map<String, ComponentFieldHolder> sourceFieldHolders = new LinkedHashMap<>();
    protected static Map<String, ColumnFieldHolder> columnFieldHolders = new LinkedHashMap<>();
    protected static Map<String, ComponentFieldHolder> lookupFieldHolders = new LinkedHashMap<>();
    protected static Map<String, Map<String, ComponentFieldHolder>> lookupHoldersByName = new LinkedHashMap<>();
    protected static Map<String, String> constantDeclarations = new LinkedHashMap<>();
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

    /**
     * Set of joinBuffer variable names (e.g. "joinBuffer2") that are guaranteed
     * non-null by the predicate of the immediately enclosing if-statement.
     * Populated by ExprComparisonGenerator, cleared by IfNodeGenerator on scope exit.
     */
    protected static Set<String> guardedJoinBuffers = new HashSet<>();

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
                case NUMATOM:
                    return new NumAtomGenerator((NumAtomAST) node);
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
                case BOOLOR:
                    return new BooleanOrGenerator((BooleanOrAST)node);
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
                case STRINGCOMP:
                    return new StringComparisonGenerator((StringComparisonAST) node);
                case CALCULATION:
                    return new CalculationGenerator((CalculationAST) node);
                case ADDITION:
                    return new CalculationGenerator((CalculationAST) node.getParent());
                case SUBTRACTION:
                    return new CalculationGenerator((CalculationAST) node.getParent());
                case MULTIPLICATION:
                    return new CalculationGenerator((CalculationAST) node.getParent());
                case DIVISION:
                    return new CalculationGenerator((CalculationAST) node.getParent());
        //         case RUNDATE:
        //             dotRundate(node);
        //             break;
        //         case UNARYINT:
        //             dotUnaryInt(node);
        //             break;
        //         case SORTTITLE:
        //             doSortTitle(node);
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
                case WRITE:
                    return new WriteStatementGenerator((WriteASTNode) node);
                case EXTRACTOUTPUT:
                    return new ExtractOutputGenerator(node);
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
                case BOOLOR:
                    BooleanOrGenerator borgen = new BooleanOrGenerator((BooleanOrAST)node);
                    return borgen.getCode(node);
                case COLUMNASSIGNMENT:
                    ColumnAssignmentGenerator cagen = new ColumnAssignmentGenerator((ColumnAssignmentASTNode)node);
                    return cagen.getCode(node);
                case EXPRCOMP:
                    ExprComparisonGenerator compgen = new ExprComparisonGenerator((ExprComparisonAST) node);
                    return compgen.getCode(node);
                case STRINGCOMP:
                    StringComparisonGenerator strcmpgen = new StringComparisonGenerator((StringComparisonAST) node);
                    return strcmpgen.getCode(node);
                default:
                    logger.atInfo().log("No code generated for node type %s", node.getType());
            }
        return "No code generated for node " + node.getClass().getSimpleName();
    }

    public static List<String> getSourceFieldDefinitons() {
        List<String> defs = new ArrayList<>();
        sourceFieldHolders.values().stream().forEach(c -> defs.add(c.getDefinition()));
        return defs;
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

    public static void setOutputLength(int length) {
        outputLength = length;
    }

    public static int getLrLength() {
        return lrLength;
    }

    public static void setLrLength(int length) {
        lrLength = length;
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

    protected String getValueName() {
        return "value_" + valueNumber++;
     }

    public static List<String> getColumnFieldDefinitons() {
        List<String> defs = new ArrayList<>();
        columnFieldHolders.values().stream().forEach(c -> defs.add(c.getDefinition()));
        return defs;
    }

    public static List<List<String>> getLookupFieldDefinitons() {
        List<List<String>> retList = new ArrayList<>();
        lookupHoldersByName.entrySet().stream().forEach(e -> addLookupDefs(e, retList));
        return retList;
    }

    private static void addLookupDefs(Entry<String, Map<String, ComponentFieldHolder>> e, List<List<String>> lkFields) {
        String lkname = e.getKey();
        List<String> defs = new ArrayList<>();
        e.getValue().values().stream().forEach(c -> defs.add(c.getDefinition()));
        lkFields.add(defs);
    }

    protected static void resetLookupFieldHolders() {
        lookupFieldHolders = new LinkedHashMap<>();
    }

    public static List<String> getConstantDefinitions() {
        List<String> defs = new ArrayList<>();
        constantDeclarations.values().stream().forEach(c -> defs.add(c));
        return defs;
    }

    public void addConstName(String cn) {
        constName = cn;
    }

    public String getConstName() {
        return constName;
    }

    /**
     * Wrap a plain value expression so it matches the type required by a
     * BigDecimal or BigInteger method-chain call.
     *
     * For numeric literal strings (raw output of NumAtomGenerator) the wrap
     * must account for whether the literal is a float or an integer:
     *   - Float literals (contain '.') need  new BigDecimal("3.14")
     *     because BigDecimal.valueOf(double) loses precision.
     *   - BigDecimal expressions (getBigDecimal) already return BigDecimal —
     *     no wrapping needed; BigDecimal.valueOf(BigDecimal) does not compile.
     *   - BigInteger expressions (getBigInteger / BigInteger.valueOf) need
     *     new BigDecimal(expr) — there is no BigDecimal.valueOf(BigInteger) overload.
     *   - Integer / long expressions use the efficient valueOf(long) form.
     */
    protected static String wrapForCompareTo(ComponentFieldHolder comparingHolder, String valueExpr) {
        switch(comparingHolder.getAccessor()) {
            case "BigDecimal":
                if (isFloatLiteral(valueExpr)) {
                    return String.format("new BigDecimal(\"%s\")", valueExpr);
                }
                if (isBigDecimalExpr(valueExpr)) {
                    return valueExpr;
                }
                if (isBigIntegerExpr(valueExpr)) {
                    return String.format("new BigDecimal(%s)", valueExpr);
                }
                return String.format("BigDecimal.valueOf(%s)", valueExpr);
            case "BigInteger":
                return String.format("BigInteger.valueOf(%s)", valueExpr);
            default:
                return valueExpr;
        }
    }

    /** True if the expression is a raw floating-point literal string (e.g. "3.14"). */
    private static boolean isFloatLiteral(String expr) {
        return expr.contains(".") && expr.matches("-?\\d+\\.\\d+");
    }

    /**
     * True if the expression already evaluates to a BigDecimal — i.e. it came from
     * a getBigDecimal() accessor call or a new BigDecimal() construction.
     * BigDecimal.valueOf(BigDecimal) does not exist, so these must pass through as-is.
     */
    protected static boolean isBigDecimalExpr(String expr) {
        return expr.contains(".getBigDecimal(") || expr.startsWith("new BigDecimal(");
    }

    /**
     * True if the expression already evaluates to a BigInteger — i.e. it came from
     * a getBigInteger() accessor call or a BigInteger.valueOf() wrap.
     * BigDecimal has no valueOf(BigInteger) overload, so these must be wrapped with
     * new BigDecimal(expr) instead of BigDecimal.valueOf(expr).
     */
    protected static boolean isBigIntegerExpr(String expr) {
        return expr.contains(".getBigInteger(") || expr.startsWith("BigInteger.valueOf(");
    }

    /**
     * Parse a guard string like "joinBuffer2 != null && " and record the variable
     * names in guardedJoinBuffers so that nested generators can skip redundant checks.
     * A guard string may contain multiple chained guards separated by " && ".
     */
    protected static void recordGuardedBuffers(String guardStr) {
        // Each guard token looks like "joinBufferN != null"
        for (String part : guardStr.split("&&")) {
            String token = part.trim();
            int spaceIdx = token.indexOf(' ');
            if (spaceIdx > 0) {
                guardedJoinBuffers.add(token.substring(0, spaceIdx));
            }
        }
    }

}