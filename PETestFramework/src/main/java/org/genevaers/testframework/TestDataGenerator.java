package org.genevaers.testframework;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genevaers.testframework.yamlreader.GersTest;
import org.genevaers.testframework.yamlreader.TemplateSetEntry;
import org.genevaers.utilities.GersEnvironment;

import com.google.common.flogger.FluentLogger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/*
** Keeper of the tests
*/
public class TestDataGenerator  {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static Configuration fmConfig;
    private static TestPaths testPaths;

    private TestDataGenerator() {
        //Make sure we can't intantiate this - there will be only one
    }


    // private void applyTemplateSetToSpec(String specName) {
    //     logger.atConfig().log("Apply template set %s to Spec '%s'", templateSet.getName(), specName);
    //     try {
    //         // Build a Spec object from the spec file
    //         spec = yr.readSpec("spec/" + specName);
    //         if(testNamesNotTooLong()) {
    //             applyTemplateSetToTests();
    //             //generateJUnit();
    //         }
    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    // }

    // private boolean testNamesNotTooLong() {
    //     boolean lenOkay = true;
    //         String qualifiedTest = envVars.get("GERS_TEST_HLQ");  //}.${spec.name}.${test.name}">
    //         for (GersTest t : spec.getTests()) {
    //             String fullName = qualifiedTest + "." + spec.getName() + "." + t.getName(); 
    //             if(spec.getName().length() > 8) {
    //                 System.out.printf("Spec name %s too long. Max length 8.\n", spec.getName());                    
    //                 lenOkay = false;
    //             }
    //             if(t.getName().length() > 7) {
    //                 System.out.printf("Test name %s too long. Max length 7.\n", t.getName());                    
    //                 lenOkay = false;
    //             }
    //             for(InputFile r : t.getReffiles()) {
    //                 boolean res = testDDNameAndTotalLength(qualifiedTest, "Reference", t, r.getDdname());
    //                 lenOkay = lenOkay && res;
    //             }
    //             for( OutputFile e : t.getExtractfiles()) {
    //                 boolean res = testDDNameAndTotalLength(qualifiedTest, "Extract", t, e.getDdname());
    //                 lenOkay = lenOkay && res;
    //             }
    //             for(OutputFile f : t.getFormatfiles()) {
    //                 boolean res = testDDNameAndTotalLength(qualifiedTest, "Format", t, f.getDdname());
    //                 lenOkay = lenOkay && res;
    //             }
    //         }
    //         return lenOkay;
    // }

    // private boolean testDDNameAndTotalLength(String qualifiedTest, String type, GersTest t, String ddname) {
    //     boolean lenOkay = true;
    //     if(ddname == null) { //it may be for extracts
    //         ddname = "EXTR001"; //Typically will be generated as a 7 char name
    //     }
    //     if(ddname.length() > 8) {
    //         System.out.printf("Test %s %s ddname %s too long. Max length 8.\n", t.getName(), type, ddname);                    
    //         lenOkay = false;
    //     } else {
    //         lenOkay = allLengthCheck(qualifiedTest, t, ddname);
    //     }
    //     return lenOkay;
    // }

    // private boolean allLengthCheck(String qualifiedTest, GersTest t, String ddname) {
    //     boolean okay = true;
    //     StringBuilder datasetName = new StringBuilder();
    //     datasetName.append(qualifiedTest);
    //     datasetName.append(".");
    //     datasetName.append(spec.getName());
    //     datasetName.append(".");
    //     datasetName.append(t.getName());
    //     datasetName.append(".OUTE.MR95.");
    //     datasetName.append(ddname);
    //     if(datasetName.length() > 44) {
    //         System.out.printf("Combined dataset length of %s is %d - too long. Max length 44.\n", datasetName.toString(), datasetName.length());                    
    //         okay = false;
    //     }
    //     return okay;
    // }


    // private void applyTemplateSetToTests() {
    //     for (GersTest t : spec.getTests()) {
    //         applyTemplatesToTest(t);
    //     }
    // }

    public static void applyTemplatesToTest(GersTest t) {
        generateTestConfig(t);
        generateTestJCL(t);
    }

    private static void generateTestJCL(GersTest t) {
        Path testJCLPath = testPaths.getJclPath().resolve(t.getFullName());
        generateOutputs(t, t.getSpec().getParentSpecFiles().getJclTemplates(), testJCLPath);
    }

    private static void generateTestConfig(GersTest t) {
        Path testConfigPath = testPaths.getConfigPath().resolve(t.getFullName());
        generateOutputs(t, t.getSpec().getParentSpecFiles().getConfigTemplates(), testConfigPath);
    }

    private static void generateOutputs(GersTest t, List<TemplateSetEntry> templates, Path outputPath) {
        outputPath.toFile().mkdirs();
        for (TemplateSetEntry te : templates) {
            Template template;
            try {
                boolean enabled = true;
                if (te.getCondition() != null) {
                    if (te.getCondition().equalsIgnoreCase("DB2Bind")) {
                        enabled = false;
                        if (t.getDb2bind() != null)
                            enabled = t.getDb2bind().equalsIgnoreCase("Y") ? true : false;
                    } else if (te.getCondition().equalsIgnoreCase("comparephase")) {
                        enabled = false;
                        if (t.getComparephase() != null)
                            enabled = t.getComparephase().equalsIgnoreCase("Y") ? true : false;
                    } else if (te.getCondition().equalsIgnoreCase("mergeparm")) {
                        enabled = t.getMergeparm() != null ? true : false;
                    }
                }
                if (enabled) {
                    template = fmConfig.getTemplate(te.getName());
                    String targetStr = te.getTarget();
                    if (targetStr.startsWith("${test}")) {
                        targetStr = targetStr.replace("${test}", t.getName());
                    }
                    TemplateApplier.generateTestTemplatedOutput(template, buildTemplateModel(t),
                            outputPath.resolve(targetStr));
                }
            } catch (IOException | TemplateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static Map<String, Object> buildTemplateModel(GersTest t) {
        GersEnvironment.initialiseFromTheEnvironment();
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("test", t);
        nodeMap.put("env", GersEnvironment.getEnvironmentVariables());
        return nodeMap;
    }

    public static void setFreemarkerConfig(Configuration cfg) {
        fmConfig = cfg;
    }

    public static void setTestPaths(TestPaths tps) {
        testPaths = tps;
    }


    // public static GersTest generateDataForTest(String testName) throws StreamReadException, DatabindException, IOException {

    //     //We will need the category
        
    //     if(testName.contains("#")) {
    //         String[] testParts = testName.split("#");
    //         System.out.println("Spec " + testParts[0]);            
    //         spec = yr.readSpec("spec/" + testParts[0]);


    //         System.out.println("Test " + testParts[1]); 
    //         GersTest gt = findTest(testParts[1]);
    //         generateConfig(gt);
    //         generateJCL(gt);
    //             //specFiles.getSpecs().           
    //     } else {
    //         //This is a spec
    //     }
    //     return null;
    // }

    private static void generateJCL(GersTest gt) {
        System.out.println("generateJCL ");
    }

    private static void generateConfig(GersTest gt) {
        System.out.println("generateConfig ");

    }

    private static GersTest findTest(String testName) {
        System.out.println("findTest ");
        
        //is the test a spec of a single test
        // GersTest test = specFiles.getSpecs().stream()
        // .filter(test -> testName.equals(((GerstTest)test.getName())))
        // .findAny()
        // .orElse(null);
        return null;
    }

    public static void process(GersTest test) {
        generateTestConfig(test);
        generateTestJCL(test);
    }
}
