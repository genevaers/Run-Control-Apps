package org.genevaers.genevaio.fieldnodes;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2008
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

import com.google.common.flogger.FluentLogger;

public class Records2Dot {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
    private static final String LIGHTGREY = "lightgrey";
    private static final String FRAME = "lightgrey";
    private static final String DATASOURCE = "deepskyblue";
    private static final String COMPARISON = "lightgreen";

    private static String idString;
    private static String label;
    private static String colour;
    private static String shape;
    private static boolean nodeEnabled = true;
    private static int nodeNum = 1;
    

    public static void write(FieldNodeBase recordsRoot, Path dest) {
        try (FileWriter fw = new FileWriter(dest.toFile());) {
            recursivelyWriteTheTree(recordsRoot, fw);
            fw.write("}\n");
        } catch (IOException e) {
            logger.atSevere().log("Records2Dot write error\n%s", e.getMessage());
        }
    }

    private static void recursivelyWriteTheTree(FieldNodeBase root, FileWriter fw) throws IOException {
        writeHeader(fw);
        writeNodes(root, fw);

    }

    private static void writeNodes(FieldNodeBase root, FileWriter fw) throws IOException {
        //String r = dotNode(root);
        Iterator<FieldNodeBase> asti = root.getChildren().iterator();
        while (asti.hasNext()) {
            FieldNodeBase n = (FieldNodeBase) asti.next();
            String child = dotNode(n, fw);
            //fw.write(r + " -> " + child + "\n");
            writeBranch(child, n, fw);
        }

    }

    private static void writeBranch(String from, FieldNodeBase next, FileWriter fw) throws IOException {
        if (next != null ) {
            Iterator<FieldNodeBase> asti = next.getChildren().iterator();
            while (asti.hasNext()) {
                FieldNodeBase node = asti.next();
                String child = dotNode(node, fw);
                if (nodeEnabled) {
                    fw.write(from + " -> " + child);
                    fw.write("\n");
                    writeBranch(child, node, fw);
                }
            }
        }
    }

    private static String dotNode(FieldNodeBase node, FileWriter fw) throws IOException {
        idString = node.getName();
        label = "";
        colour = "red";
        shape = "oval";
        switch(node.getFieldNodeType()) {
            case NUMBERFIELD:
                label = node.getName() +" (" + Integer.toString(((NumericFieldNode)node).getValue()) + ")";
                colour = "skyblue";
                idString = "num" + nodeNum++;
                break;
            case VIEW:
                label = ((ViewFieldNode)node).getName();
                colour = "pink";
                idString = "type" + ((ViewFieldNode)node).getTypeNumber();
                break;
           case RECORD:
                label = ((RecordNode)node).getName();
                colour = "pink";
                idString = "rec" + label;
                break;
            case STRINGFIELD:
                label = node.getName() +" (" + dotEscape(((StringFieldNode)node).getValue()) + ")";
                colour = "lightgreen";
                idString = "str" + nodeNum++;
                break;
            default:
                label = node.getName();
                idString = "def" + nodeNum++;
                break;
            
        }
        if (node != null) {
                fw.write(idString + "[label=\"" + label + "\" " + "color=" + colour + " shape=" + shape
                        + " style=filled]\n");
            }
        return idString;
    }

    private static void writeHeader(FileWriter fw) throws IOException {
        fw.write("digraph xml {\nrankdir=LR\n//Nodes\n");
        fw.write(
                "graph [label=\" MR91 Logic Trees\", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22, concentrate=true];\n");
        fw.write("labeljust=center;\n");
    }

    private static String dotEscape(String string) {
        return string.replace("\"", "\\\"");
    }

}
