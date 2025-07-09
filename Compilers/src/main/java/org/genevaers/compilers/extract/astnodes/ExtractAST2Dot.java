package org.genevaers.compilers.extract.astnodes;

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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;

import com.google.common.flogger.FluentLogger;

/**
 * Generate a dot file of the AST so we can see a pretty picture. <BR>
 * To dot the picture more readable we can filter on views and columns.
 * Filtering defaults to not enabled.
 */
public class ExtractAST2Dot {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    /**
     *
     */
    // Node Type Colours
    private static final String LIGHTGREY = "lightgrey";
    private static final String FRAME = "lightgrey";
    private static final String DATASOURCE = "deepskyblue";
    private static final String LKDATASOURCE = "limegreen";
    private static final String COMPARISON = "lightgreen";
    private static final String ASSIGNMENT = "violet";
    private static final String EXTACT_FILTER = "lightpink";
    private static final String CAST = "red";
    private static final String STRINGCONST = "springgreen";
    private static final String NUMCONST = "springgreen";
    private static final String DATECONST = "springgreen";
    private static final String COLUMNDATA = "skyblue";
    private static final String SKCOLUMN = "yellow";
    private static final String CALC_COLOUR = "bisque";
    private static final String ST_COLOUR = "gold";
    private static final String PRIOR_CLR = "pink";
    // Node Function Shapes
    private static final String EMITABLE = "octagon";
    private static final String CASTFUNC = "septagon";
    private static final String FRAMEWORK = "rect";
    private static final String EOS_END = "circle";
    private static FileWriter fw;
    private static String[] views;
    private static String[] cols;
    private static boolean nodeEnabled = true;
    private static boolean filter = false;
    private static String lf_id;
    private static ExtractBaseAST xltRoot;
    static int nodeNum = 1;
    private static String idString;
    private static String label;
    private static String colour;
    private static String shape;

    private static boolean reverseArrow = false; // Default arrow direction
    private static boolean dataflow = false; // Default arrow direction

    ExtractAST2Dot() {

    }

    public static void write(ExtractBaseAST root, Path dest) {
        try {
            fw = new FileWriter(dest.toFile());
            recursivelyWriteTheTree(root);
            fw.write("}\n");
            fw.close();
        } catch (IOException e) {
            logger.atSevere().log("ExtractAST2Dot write failed %s\n", e.getMessage());
        }
    }

    private static void recursivelyWriteTheTree(ExtractBaseAST root) throws IOException {
        writeHeader();
        writeNodes(root);

    }

    private static void writeNodes(ExtractBaseAST root) throws IOException {
        String r = dotNode(root);
        Iterator<ASTBase> asti = root.getChildIterator();
        while (asti.hasNext()) {
            ExtractBaseAST n = (ExtractBaseAST) asti.next();
            String child = dotNode(n);
            fw.write(r + " -> " + child + "\n");
            writeBranch(child, n);
        }

    }

    private static void writeBranch(String from, ExtractBaseAST next) throws IOException {
        if (next != null) {
            Iterator<ASTBase> asti = next.getChildIterator();
            while (asti.hasNext()) {
                ExtractBaseAST node = (ExtractBaseAST) asti.next();
                String child = dotNode(node);
                if (nodeEnabled) {
                    fw.write(from + " -> " + child);
                    if (reverseArrow) {
                        fw.write(" [dir = back color=red] ");
                        reverseArrow = false;
                    }
                    if (dataflow) {
                        fw.write(" [color=red] ");
                        dataflow = false;
                    }
                    fw.write("\n");
                    writeBranch(child, node);
                }
            }
        }
    }

    private static String dotNode(ExtractBaseAST node) throws IOException {
        idString = "";
        label = "";
        colour = "red";
        shape = "oval";
        if (node != null) {
            switch (node.getType()) {
                case EBASE:
                    dotRoot();
                    break;
                case LF:
                    dotLfNode(node);
                    break;
                case PF:
                    dotPfNode(node);
                    break;
                case VIEWSOURCE:
                    dotViewSourceNode(node);
                    break;
                case VIEWCOLUMNSOURCE:
                    dotViewColumnSourceNode(node);
                    break;
                case NUMATOM:
                    dotNumAtomNode(node);
                    break;
                case STRINGATOM:
                    dotStringAtomNode(node);
                    break;
                case STRINGCONCAT:
                    doStringConcat(node);
                    break;
                case LEFT:
                case RIGHT:
                    doStringFunction(node);
                    break;
                case SUBSTR:
                    doSubStringFunction(node);
                    break;
                case COLUMNASSIGNMENT:
                    dotColumnAssignmentNode(node);
                    break;
                case LRFIELD:
                    dotLrFieldNode(node);
                    break;
                case PRIORLRFIELD:
                    dotPriorLrFieldNode(node);
                    break;
                case DATATYPE:
                    dotDatatype(node);
                    break;
                case DT_COLUMN:
                    dotColumnNode(node);
                    break;
                case SK_COLUMN:
                    dotSKColumnNode(node);
                    break;
                case CT_COLUMN:
                    dotColumnNode(node);
                    break;
                case COLUMNREF:
                    //dotColumnNode(node);
                    dotColumnRefNode(node);
                    break;
                case NUMACC:
                    dotNumericAccumNode(node);
                    break;
                case ERRORS:
                    dotErrorNode(node);
                    break;
                case SELECTIF:
                case SKIPIF:
                case IFNODE:
                case BOOLAND:
                case ISFOUND:
                case ISNOTFOUND:
                    dotFrameworkNode(node);
                    break;
                case ISNULL:
                case ISNUMERIC:
                case ISSPACES:
                case ISNOTNULL:
                case ISNOTNUMERIC:
                case ISNOTSPACES:
                    doFunctionNode(node);
                    break;
                case CAST:
                    dotCast(node);
                    break;
                case SYMBOL:
                    dotSymbolNode(node);
                    break;
                case WRITESOURCEARG:
                    dotWriteSourceNode(node);
                    break;
                case WRITEEXTRACT:
                    dotWriteExtractNode(node);
                    break;
                case WRITEFILE:
                    dotWriteFileNode(node);
                    break;
                case EXPRCOMP:
                    dotComparison(node);
                    break;
                case RUNDATE:
                    dotRundate(node);
                    break;
                case UNARYINT:
                    dotUnaryInt(node);
                    break;
                case SORTTITLE:
                    doSortTitle(node);
                    break;
                case CALCULATION:
                case RECORD_COUNT:
                case ADDITION:
                case SUBTRACTION:
                case SETTER:
                case MULTIPLICATION:
                case DIVISION:
                    doCalculation(node);
                    break;
                case EOS:
                    doEOS(node);
                    break;
                case LOOKUPREF:
                    dotLookupNode(node);
                    break;
                case LOOKUPFIELDREF:
                    dotLookupFieldNode(node);
                    break;
                case DATEFUNC:
                    doDateFunc(node);
                    break;
                case EXTRFILTER:
                    doExtractFilter(node);
                    break;
                case ALL:
                    doAll(node);
                    break;
                default:
                    dotDefaultNode(node);
                    break;
            }
            if (nodeEnabled) {
                fw.write(idString + "[label=\"" + label + "\" " + "color=" + colour + " shape=" + shape
                        + " style=filled]\n");
            }
        }
        return idString;
    }

    private static void doAll(ExtractBaseAST node) {
        AllAST allNode = (AllAST) node;
        label = allNode.getType().toString() + " (" + dotEscape(allNode.getValue()) + ")";
        label = label.replace("\\", "\\\\");
        colour = STRINGCONST;
        idString = "All_" + nodeNum++;
        reverseArrow = true;
    }

    private static void doExtractFilter(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        ExtractFilterAST ef = (ExtractFilterAST) node;
        label =  dotEscape(node.getType().toString() + "\n" + ef.getLogicText());
        colour = EXTACT_FILTER;
    }

    private static void doDateFunc(ExtractBaseAST node) {
       idString = "UN_" + nodeNum++;
        DateFunc df = (DateFunc) node;
        label =  "DATE(" + df.getValue() + "," + df.getDateCodeStr() + ")";
        colour = DATECONST;
        reverseArrow = true;
    }

    private static void doFunctionNode(ExtractBaseAST node) {
        //ColumnRefAST col = (ColumnRefAST) node;
        label = node.getType().toString(); // + col.getViewColumn().getColumnNumber();
        colour = COMPARISON;
        idString = "func_" +  + nodeNum;
        dataflow = true;
        reverseArrow = true;
    }

    private static void dotColumnRefNode(ExtractBaseAST node) {
        //Interesting if we can link to the real column??
        //
        ColumnRefAST col = (ColumnRefAST) node;
        label = "Column Ref " + col.getViewColumn().getColumnNumber();
        colour = COLUMNDATA;
        idString = "col_" +  col.getViewColumn().getComponentId() + nodeNum;
        dataflow = true;
        reverseArrow = true;
    }

    private static void dotWriteFileNode(ExtractBaseAST node) {
        WriteFileNode wfn = (WriteFileNode) node;
        label = "WRDEST ";
        colour = LIGHTGREY;
        idString = "wen" + nodeNum++;
    }

    private static void dotWriteDestNode(ExtractBaseAST node) {
        // WriteDesttNode wen = (WriteDesttNode) node;
        // label = "WRDEST " + wen.getFileNumber();
        // colour = LIGHTGREY;
        // idString = "wen" + nodeNum++;
    }

    private static void doEOS(ExtractBaseAST node) {
        idString = "EOS_" + nodeNum++;
        label = node.getType().toString();
        colour = FRAME;
        shape = EOS_END;
    }

    private static void doSortTitle(ExtractBaseAST node) {
        idString = "ST_" + nodeNum++;
        label = node.getType().toString();
        colour = ST_COLOUR;
    }

    private static void doCalculation(ExtractBaseAST node) {
        idString = "AR_" + nodeNum++;
        label = node.getType().toString();
        colour = CALC_COLOUR;
        reverseArrow = true;
    }

    private static void dotUnaryInt(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        UnaryInt ui = (UnaryInt) node;
        label = ui.getValue();
        colour = DATECONST;
        reverseArrow = true;
    }

    private static void dotCast(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
        colour = CAST;
        shape = CASTFUNC;
        reverseArrow = true;
    }

    private static void dotDatatype(ExtractBaseAST node) {
        DataTypeAST dt = (DataTypeAST) node;
        label = dt.getDatatype().toString();
        idString = "UN_" + nodeNum++;
        colour = DATASOURCE;
        reverseArrow = true;
    }

    private static void dotRundate(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        RundateAST rd = (RundateAST) node;
        label = rd.getValueString();
        colour = DATECONST;
        reverseArrow = true;
    }

    private static void dotComparison(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        ExprComparisonAST ec = (ExprComparisonAST) node;
        label = ec.getOp();
        colour = COMPARISON;
        shape = EMITABLE;
    }

    private static void dotFrameworkNode(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
        colour = FRAME;
        shape = FRAMEWORK;
    }

    private static void dotDefaultNode(ExtractBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
    }

    private static void dotWriteExtractNode(ExtractBaseAST node) {
        WriteExtractNode wen = (WriteExtractNode) node;
        label = "EXTRACT " + wen.getFileNumber();
        colour = LIGHTGREY;
        idString = "wen" + nodeNum++;
    }

    private static void dotWriteSourceNode(ExtractBaseAST node) {
        WriteSourceArg arg = (WriteSourceArg) node;
        label = arg.getArg();
        colour = LIGHTGREY;
        idString = "arg_" + nodeNum++;
    }

    private static void dotSymbolNode(ExtractBaseAST node) {
        SymbolEntry sym = (SymbolEntry) node;
        label = sym.getName() + " " + sym.getValue();
        colour = LIGHTGREY;
        idString = "sym_" + nodeNum;
    }

    private static void dotErrorNode(ExtractBaseAST node) {
        ErrorAST errs = (ErrorAST) node;
        label = errs.getError();
        colour = "red";
        shape = "octagon";
        idString = "err_" + nodeNum++;
    }

    private static void dotNumericAccumNode(ExtractBaseAST node) {
        NumericAccumulator na = (NumericAccumulator) node;
        label = na.getAccumulatorName() + "(" + na.getAccumNumber() + ") " + na.getOperation();
        colour = "tomato";
        shape = EMITABLE;
        idString = "na_" + nodeNum;
        reverseArrow = true;
    }

    private static void dotColumnNode(ExtractBaseAST node) {
        ColumnAST col = (ColumnAST) node;
        label = "Column " + col.getViewColumn().getColumnNumber();
        idString = "col_" + col.getViewColumn().getComponentId() + nodeNum;
        colour = COLUMNDATA;
        dataflow = true;
    }

    private static void dotSKColumnNode(ExtractBaseAST node) {
        ColumnAST col = (ColumnAST) node;
        label = "Column " + col.getViewColumn().getColumnNumber();
        colour = SKCOLUMN;
        idString = "col_" + col.getViewColumn().getComponentId() + nodeNum;
        dataflow = true;
    }

    private static void dotLrFieldNode(ExtractBaseAST node) {
        FieldReferenceAST fieldRef = (FieldReferenceAST) node;
        label = fieldRef.getName();
        colour = DATASOURCE;
        idString = "Field_" + nodeNum++;
        reverseArrow = true;
    }

    private static void dotLookupFieldNode(ExtractBaseAST node) {
        LookupFieldRefAST lkfieldRef = (LookupFieldRefAST) node;
        String labelName = lkfieldRef.getRef() != null ? lkfieldRef.getRef().getName() : "____";
        label = lkfieldRef.getLookup().getName() + "." + labelName + "\n";
        label += lkfieldRef.getUniqueKey() + " -> " + lkfieldRef.getNewJoinId();;
        colour = LKDATASOURCE;
        idString = "Field_" + nodeNum++;
        reverseArrow = true;
    }

    private static void dotLookupNode(ExtractBaseAST node) {
        LookupPathRefAST lkRef = (LookupPathRefAST) node;
        label = lkRef.getMessageName() + "\n" + lkRef.getUniqueKey() + " -> " + lkRef.getNewJoinId();
        colour = LKDATASOURCE;
        idString = "LK_" + nodeNum++;
        reverseArrow = true;
    }


    private static void dotPriorLrFieldNode(ExtractBaseAST node) {
        FieldReferenceAST fieldRef = (FieldReferenceAST) node;
        label = fieldRef.getName();
        colour = PRIOR_CLR;
        idString = "Prior_" + nodeNum++;
        reverseArrow = true;
    }

    private static void dotColumnAssignmentNode(ExtractBaseAST node) {
        ColumnAssignmentASTNode colassNode = (ColumnAssignmentASTNode) node;
        label = colassNode.getType().toString() + " " + colassNode.getLineNumber() +":" + colassNode.getCharPositionInLine();
        colour = ASSIGNMENT;
        shape = EMITABLE;
        idString = "Colass" + nodeNum++;
    }

    private static void dotStringAtomNode(ExtractBaseAST node) {
        StringAtomAST strNode = (StringAtomAST) node;
        String escValue = strNode.getValue().replace("\"", "'");
        label = strNode.getType().toString() + " (" + escValue + ")";
        colour = NUMCONST;
        idString = "Str_" + nodeNum++;
        reverseArrow = true;
    }

    private static void doStringConcat(ExtractBaseAST node) {
        FormattedASTNode strNode = (FormattedASTNode) node;
        colour = STRINGCONST;
        label = strNode.getType().toString();
        idString = "Str_" + nodeNum++;
        reverseArrow = true;
    }

    private static void doStringFunction(ExtractBaseAST node) {
        StringFunctionASTNode strNode = (StringFunctionASTNode) node;
        colour = STRINGCONST;
        label = strNode.getType().toString() + "(" + strNode.getLength() + ")";
        idString = "Str_" + nodeNum++;
        reverseArrow = true;
    }

    private static void doSubStringFunction(ExtractBaseAST node) {
        SubStringASTNode strNode = (SubStringASTNode) node;
        colour = STRINGCONST;
        label = strNode.getType().toString() + "(" + strNode.getStartOffest() + "," + strNode.getLengthString() + ")";
        idString = "Str_" + nodeNum++;
        reverseArrow = true;
    }

    private static void dotNumAtomNode(ExtractBaseAST node) {
        NumAtomAST numNode = (NumAtomAST) node;
        label = numNode.getType().toString() + " (" + numNode.getValueString() + ")";
        colour = NUMCONST;
        idString = "Num_" + nodeNum++;
        reverseArrow = true;
    }

    private static void dotViewColumnSourceNode(ExtractBaseAST node) {
        ViewColumnSourceAstNode vcsn = (ViewColumnSourceAstNode) node;
        ViewColumnSource vcs = vcsn.getViewColumnSource();
        if (filter) {
            if (cols.length > 0) {
                nodeEnabled = Arrays.stream(cols).anyMatch(Integer.toString(vcs.getColumnNumber())::equals);
            } else {
                nodeEnabled = true;
            }
        }
        ViewSourceAstNode vsn = (ViewSourceAstNode) vcsn.getParent();
        ViewSource vs = vsn.getViewSource();
        idString = lf_id + "_VC_" + vcs.getViewId() + "_" + vcs.getViewSourceId() + "_" + vs.getSequenceNumber() + "_"
                + vcs.getComponentId();
        String escLogic = vcs.getLogicText().replace("\"", "'");
        label = "Column " + vcs.getColumnNumber() + "\n" + escLogic;
        if(vcsn.isAssignedTo()) {
            colour = "lightblue";
        } else {
            colour = LIGHTGREY;
            shape = EMITABLE;
        }
    }

    private static void dotViewSourceNode(ExtractBaseAST node) {
        ViewSourceAstNode vsn = (ViewSourceAstNode) node;
        ViewSource vs = vsn.getViewSource();
        if (filter) {
            nodeEnabled = Arrays.stream(views).anyMatch(Integer.toString(vs.getViewId())::equals);
        }
        idString = lf_id + "_VS_" + vs.getViewId() + "_" + vs.getSequenceNumber();
        label = dotEscape("View " + vs.getViewId() + " Source " + vs.getSequenceNumber());
        colour = "PaleGreen";
    }

    private static void dotPfNode(ExtractBaseAST node) {
        nodeEnabled = true;
        PFAstNode pfn = (PFAstNode) node;
        PhysicalFile pf = pfn.getPhysicalFile();
        idString = "PF_" + pfn.getName();
        label = pfn.getName();
        colour = "pink";
        shape = "rect";
    }

    private static void dotLfNode(ExtractBaseAST node) {
        nodeEnabled = true;
        LFAstNode lfn = (LFAstNode) node;
        LogicalFile lf = lfn.getLogicalFile();
        idString = "LF_" + lfn.getName();
        label = lfn.getName();
        if (lf != null) {
            label = label + "(" + lf.getID() + ")\nPFs " + lf.getSetOfPFIDs();
        }
        colour = "palevioletred";
        shape = EMITABLE;
        lf_id = idString;
    }

    private static void dotRoot() {
        nodeEnabled = false;
        idString = "Root";
        label = "Root";
    }

    private static String dotEscape(String string) {
        return string.replace("\"", "\\\"");
    }

    private static void writeHeader() throws IOException {
        fw.write("digraph xml {\nrankdir=TB\n//Nodes\n");
        fw.write(
                "graph [label=\" MR91 Logic Trees\", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22, concentrate=true];\n");
        fw.write("labeljust=center;\n");
    }

    public static void writeRawSources(Path dest) {
        // iterate through the views
        // We want the LFs -> view sources
        // Then dot false AST nodes?
        xltRoot = ASTFactory.getNodeOfType(ASTFactory.Type.ERSROOT);
        Stream<ViewNode> vs = Repository.getViews().getValues().stream();
        vs.forEachOrdered(v -> viewSources(v));

        write(xltRoot, dest);
    }

    private static void viewSources(ViewNode v) {
        Iterator<ViewSource> vsi = v.getViewSourceIterator();
        while (vsi.hasNext()) {
            buildLF2View(vsi.next());
        }
    }

    private static void buildLF2View(ViewSource vs) {
        LFAstNode lfNode = (LFAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.LF);
        lfNode.setLogicalFile(Repository.getLogicalFiles().get(vs.getSourceLFID()));

        ViewSourceAstNode vsnode = (ViewSourceAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.VIEWSOURCE);
        vsnode.setViewSource(vs);
        lfNode.addChildIfNotNull(vsnode);

        addPFNodes(vs, lfNode);
    }

    public static void addPFNodes(ViewSource vs, LFAstNode lfNode) {
        Iterator<PhysicalFile> pfi = Repository.getLogicalFiles().get(vs.getSourceLFID()).getPFIterator();
        while (pfi.hasNext()) {
            PFAstNode pfn = (PFAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.PF);
            pfn.setPhysicalFile(pfi.next());
            pfn.addChildIfNotNull(lfNode);
            xltRoot.addChildIfNotNull(pfn);
        }
    }

    /**
     * Set filtering enabled or not.
     * 
     * @param filter
     */
    public static void setFilter(Boolean filter) {
        ExtractAST2Dot.filter = filter;
    }

    /**
     * Comma separated list of view IDs to be generated.
     * 
     * @param views
     */
    public static void setViews(String[] views) {
        ExtractAST2Dot.views = views;
    }

    /**
     * Comma separated list of column numbers to be generated.
     * 
     * @param cols
     */
    public static void setCols(String[] cols) {
        ExtractAST2Dot.cols = cols;
    }
}
