package org.genevaers.compilers.extract.JavaEmitter;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.genevaers.compilers.extract.JavaEmitter.generators.ExtractBaseGenerator;
import org.genevaers.compilers.extract.JavaEmitter.generators.ExtractRecordGenerator;
import org.genevaers.compilers.extract.JavaEmitter.generators.JoinGenerator;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import com.google.common.flogger.FluentLogger;

/**
 * Write the Extract AST to a Java file.
 */
public class ExtractAST2Java {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    /**
     *
     */
    protected static List<String> inputDDnames = new ArrayList<>();
    protected static int outputLength;
    protected static int lrLength;
    static int nodeNum = 1;

    protected static List<String> filterRecs = new ArrayList<>();
    protected static List<String> columnRecs = new ArrayList<>();
    protected static List<String> currentRecords;
    protected static Map<String, JoinGenerator> joins = new HashMap<>();

    ExtractAST2Java() {

    }

    //Instead of writing the nodes build up entries to be written via Fremarker template?
    public static void write(ExtractBaseAST root) {
        logger.atInfo().log("Generate Java extract code from AST");
        try {
            generateJavaEntries(root);
            ExtractorWriter.addJoinInitialisation();
            ExtractorWriter.write(  ExtractRecordGenerator.getFilterRecs(), 
                                    ExtractRecordGenerator.getColumnRecs(), 
                                    ExtractRecordGenerator.getInputDDnames(), 
                                    ExtractRecordGenerator.getOutputLength(), 
                                    ExtractRecordGenerator.getLrLength());
        } catch (IOException e) {
            logger.atSevere().log("ExtractAST2Java write failed %s\n", e.getMessage());
        }
    }

    private static void generateJavaEntries(ExtractBaseAST root) throws IOException {
        ExtractBaseGenerator ebg = new ExtractBaseGenerator(root);
        ebg.generateCode();
    }


    /**
     * Comma separated list of view IDs to be generated.
     * 
     * @param views
     */
    public static void setViews(String[] views) {
    }

    /**
     * Comma separated list of column numbers to be generated.
     * 
     * @param cols
     */
    public static void setCols(String[] cols) {
    }
}
