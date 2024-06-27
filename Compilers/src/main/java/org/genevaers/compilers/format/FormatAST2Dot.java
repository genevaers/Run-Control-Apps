package org.genevaers.compilers.format;

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
import java.util.Iterator;
import java.util.stream.Stream;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.format.astnodes.ColRef;
import org.genevaers.compilers.format.astnodes.FormatASTFactory;
import org.genevaers.compilers.format.astnodes.FormatBaseAST;
import org.genevaers.compilers.format.astnodes.FormatErrorAST;
import org.genevaers.compilers.format.astnodes.FormatView;
import org.genevaers.compilers.format.astnodes.NumConst;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewNode;

import com.google.common.flogger.FluentLogger;

/**
 * Generate a dot file of the AST so we can see a pretty picture. <BR>
 * To dot the picture more readable we can filter on views and columns.
 * Filtering defaults to not enabled.
 */
public class FormatAST2Dot {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     *
     */
    // Node Type Colours
    private static final String LIGHTGREY = "lightgrey";
    private static final String FRAME = "lightgrey";
    private static final String DATASOURCE = "deepskyblue";
    private static final String COMPARISON = "lightgreen";
    private static final String ASSIGNMENT = "violet";
    private static final String STRINGCONST = "springgreen";
    private static final String NUMCONST = "springgreen";
    private static final String DATECONST = "springgreen";
    private static final String COLUMNDATA = "skyblue";
    private static final String SKCOLUMN = "yellow";
    private static final String CALC_COLOUR = "bisque";
    // Node Function Shapes
    private static final String EMITABLE = "octagon";
    private static final String FRAMEWORK = "rect";
    private static FileWriter fw;
    private static String[] views;
    private static String[] cols;
    private static boolean nodeEnabled = true;
    private static boolean filter = false;
    private static String lf_id;
    private static FormatBaseAST formatRoot;
    static int nodeNum = 1;
    private static String idString;
    private static String label;
    private static String colour;
    private static String shape;

    private static boolean reverseArrow = false; // Default arrow direction
    private static boolean dataflow = false; // Default arrow direction

    FormatAST2Dot() {

    }

    public static void write(FormatBaseAST root, Path dest) {
        try {
            fw = new FileWriter(dest.toFile());
            recursivelyWriteTheTree(root);
            fw.write("}\n");
            fw.close();
        } catch (IOException e) {
            logger.atSevere().log("FormatAST2Dot write failed %s", e.getMessage());
        }
    }

    private static void recursivelyWriteTheTree(FormatBaseAST root) throws IOException {
        writeHeader();
        writeNodes(root);

    }

    private static void writeNodes(FormatBaseAST root) throws IOException {
        String r = dotNode(root);
        Iterator<ASTBase> asti = root.getChildIterator();
        while (asti.hasNext()) {
            FormatBaseAST n = (FormatBaseAST) asti.next();
            String child = dotNode(n);
            fw.write(r + " -> " + child + "\n");
            writeBranch(child, n);
        }

    }

    private static void writeBranch(String from, FormatBaseAST next) throws IOException {
        if (next != null) {
            Iterator<ASTBase> asti = next.getChildIterator();
            while (asti.hasNext()) {
                FormatBaseAST node = (FormatBaseAST) asti.next();
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

    private static String dotNode(FormatBaseAST node) throws IOException {
        idString = "";
        label = "";
        colour = "red";
        shape = "oval";
        if (node != null) {
            switch (node.getType()) {
                case FORMATROOT:
                    dotRoot();
                    break;
                case FORMATFILTERROOT:
                    dotFormatFilter(node);
                    break;
                case FORMATVIEW:
                    doFormatView(node);
                    break;
                case SELECTIF:
                case SKIPIF:
                    doFilterType(node);
                    break;
                    case GT:
                    case GE:
                    case EQ:
                    case NE:
                    case LE:
                    case LT:
                    doComparison(node);
                    break;
                case NUMCONST:
                    doNumConst(node);
                    break;
                case COLREF:
                    doColRef(node);
                    break;
                case ERRORS:
                    dotErrorNode(node);
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

    // private static void dotUnaryInt(FormatBaseAST node) {
    //     idString = "UN_" + nodeNum++;
    //     UnaryInt ui = (UnaryInt) node;
    //     label = ui.getValue();
    //     colour = DATECONST;
    //     reverseArrow = true;
    // }

    private static void doFormatView(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString() + ((FormatView)node).getView().getID();
        colour = COMPARISON;
    }

    private static void doFilterType(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString() ;
        colour = COMPARISON;
    }

    private static void doColRef(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString() + " (" + ((ColRef)node).getText() + ")";
        colour = COLUMNDATA;
    }

    private static void doNumConst(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString() + " (" + ((NumConst)node).getValue() + ")";
        colour = NUMCONST;
    }

    private static void doComparison(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
        colour = COMPARISON;
    }

    private static void dotFormatFilter(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
        colour = "PaleGreen";
    }

    private static void dotFrameworkNode(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
        colour = FRAME;
        shape = FRAMEWORK;
    }

    private static void dotDefaultNode(FormatBaseAST node) {
        idString = "UN_" + nodeNum++;
        label = node.getType().toString();
    }


    private static void dotErrorNode(FormatBaseAST node) {
        FormatErrorAST errs = (FormatErrorAST) node;
        label = errs.getError();
        colour = "red";
        shape = "octagon";
        idString = "err_" + nodeNum++;
    }

    // private static void dotColumnNode(FormatBaseAST node) {
    //     ColumnAST col = (ColumnAST) node;
    //     label = "Column " + col.getViewColumn().getColumnNumber();
    //     colour = COLUMNDATA;
    //     idString = "col_" + col.getViewColumn().getComponentId() + nodeNum;
    //     dataflow = true;
    // }

    private static void dotRoot() {
        idString = "Root";
        label = "Root";
        colour = FRAME;
    }

    private static String dotEscape(String string) {
        return string.replace("\"", "\\\"");
    }

    private static void writeHeader() throws IOException {
        fw.write("digraph xml {\nrankdir=TB\n//Nodes\n");
        fw.write(
                "graph [label=\" MR91 Format Logic Trees\", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22, concentrate=true];\n");
        fw.write("labeljust=center;\n");
    }

    public static void writeRawSources(Path dest) {
        // iterate through the views
        // We want the LFs -> view sources
        // Then dot false AST nodes?
        formatRoot = FormatASTFactory.getNodeOfType(FormatASTFactory.Type.FORMATROOT);
        Stream<ViewNode> vs = Repository.getViews().getValues().stream();
//        vs.forEachOrdered(v -> viewSources(v));

        write(formatRoot, dest);
    }

    /**
     * Set filtering enabled or not.
     * 
     * @param filter
     */
    public static void setFilter(Boolean filter) {
        FormatAST2Dot.filter = filter;
    }

    /**
     * Comma separated list of view IDs to be generated.
     * 
     * @param views
     */
    public static void setViews(String[] views) {
        FormatAST2Dot.views = views;
    }

    /**
     * Comma separated list of column numbers to be generated.
     * 
     * @param cols
     */
    public static void setCols(String[] cols) {
        FormatAST2Dot.cols = cols;
    }
}
