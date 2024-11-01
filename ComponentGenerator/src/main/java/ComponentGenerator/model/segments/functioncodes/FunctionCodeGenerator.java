package ComponentGenerator.model.segments.functioncodes;

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


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.generators.GeneratorBase;

public class FunctionCodeGenerator  extends GeneratorBase {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private FunctionCodeSegment definitions;

    public void writeOutputs(FunctionCodeSegment fcseg) {
        logger.atConfig().log("Generate Function Code items");
        definitions = fcseg;
        String packName = fcseg.getPackageName();
        logger.atConfig().log("do something in " + packName);

        Iterator<FunctionCodeItem> fcii = fcseg.getCodes().iterator();
        while(fcii.hasNext()) {
            FunctionCodeItem fcEntry = fcii.next();
              logger.atConfig().log("For " + fcEntry.getName());

            writeFunctionCodeFactory(packName, fcEntry);

            // Iterator<FunctionCodeDefinition> fci = fcEntry.getFcdefs().getCodes().iterator();
            // while(fci.hasNext()) {
            // 	FunctionCodeDefinition fc = fci.next();
            // 	logger.atInfo().log("Feed fc %s (%s) to the template ", fc.getFunction_code(), fc.getDescription());
            // }
            // Map<String, Object> nodeMap = populateNodeMap(prefix, argRec, ltrentry);
      //       writeJavaObject(ltrentry, nodeMap);
      //       writeRecordToDSECT(ltrentry, nodeMap);
        }
        logger.atConfig().log("------------------");
        logger.atInfo().log(" ");
    }

    private void writeFunctionCodeFactory(String packName, FunctionCodeItem fcEntry) {
        logger.atConfig().log("Generate Function codes from %s", fcEntry.getName());
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("packageName", packName);
        nodeMap.put("funcCodeFunctions", prepocessTemplateEntries(fcEntry.getFcdefs().getCodes()));
        nodeMap.put("codes", fcEntry.getFcdefs().getCodes());
        writeJavaCode(nodeMap);
    }

    private List<String> prepocessTemplateEntries(List<FunctionCodeDefinition> codes) {
        List<String> entries = new ArrayList<>();
        for( FunctionCodeDefinition c: codes) {
            switch(c.getLtRecordType()) {
                case "NAMEVALUE":
                entries.add(processNameValue(c));
                break;
                case "NAMEF1":
                entries.add(processNameF1(c));
                break;
                case "NAMEF2":
                entries.add(processNameF2(c));
                break;
                case "F2":
                entries.add(processF2(c));
                break;
                case "F1":
                entries.add(processF1(c));
                break;
                case "CC":
                entries.add(processCC(c));
                break;
                default:
                entries.add(getFunctionName(c) + "()"); //{" + getBody() + "    }");
                break;
            }
        }
        return entries;
    }

    private String processCC(FunctionCodeDefinition c) {
        return getFunctionName(c) + "(String c1, String c2, String op);";
    }

    private String processF1(FunctionCodeDefinition c) {
        return getFunctionName(c) + processSubFunction(c); // + "{" + getBody() + "    }" ;
    }

    private String getBody() {
        return "\n        return null;\n";
    }

    private String processF2(FunctionCodeDefinition c) {
        return getFunctionName(c) + processSubFunction(c); // + "{" + getBody() + "    }" ;
    }

    private String processSubFunction(FunctionCodeDefinition c) {
        String args = "(";
        String fc = c.getFunctionCode();
        if (fc.equals("JOIN") || fc.equals("LKLR") || fc.startsWith("LKD")) {
            args += "String val"; // {" + getBody() + "    }" ;
        } else {
            if (fc.length() == 3) {
                char subfunc1 = fc.charAt(2);
                args += getSubFunctionArg(subfunc1, "");
                String func = c.getFunctionCode().substring(0,2);
                if(func.equals("LK") && c.getLtRecordType().equals("F2")) {
                    args += ", LookupPathKey key";
                } else if(func.equals("SK")) {
                        args += ", ViewColumn vc, ViewSortKey vsk";
                    } else if(c.getLtRecordType().equals("F2") 
                || func.equals("DT")
                || func.equals("CT")) {
                    //add the column anyway
                    args += ", ViewColumn vc";
                }
            }
            if (fc.length() > 3) {
                if(fc.equalsIgnoreCase("KSLK")){
                    args += "LRField f";
                } else {
                    char subfunc1 = fc.charAt(2);
                    char subfunc2 = fc.charAt(3);
                    args += getSubFunctionArg(subfunc1, "1") + ", " + getSubFunctionArg(subfunc2, "2");
                    if(fc.startsWith("CF")) {
                        args += ", String op";
                    }
                }
            }
        }
        return args + ")";
    }

    private String getSubFunctionArg(char subfunc1, String appendix) {
        String arg;
        switch(subfunc1) {
            case 'A':
            arg = "Accumulator a";
            break;
            case 'C':
            arg = "String v";
            break;
            case 'E':
            arg = "LRField f";
            break;
            case 'L':
            arg = "LRField f";
            break;
            case 'P':
            arg = "LRField f";
            break;
            case 'X':
            arg = "ViewColumn c";
            break;
            default:
            arg = "";
            break;
        }
        return arg + appendix;
    }

    private String processNameF2(FunctionCodeDefinition c) {
        return getFunctionName(c) + "(String accum, LogicTableArg arg1, LogicTableArg arg2)"; // {" + getBody() + "    }" ;
    }

    private String processNameF1(FunctionCodeDefinition c) {
        //The second arg may be a field or view column
        // What about an ADDX the X is a column ref
        String fc = c.getFunctionCode();
        String func = fc.substring(0,2);
        if(func.equals("DT") || func.equals("CT") || func.equals("SK")) {        
            return getFunctionName(c) + "(String accum, ViewColumn vc)";
        } else if(fc.startsWith("CFA")) {
            if(fc.charAt(3) == 'X') {
                return getFunctionName(c) + "(String accum, ViewColumn vc, String op)";
            } else {        
                return getFunctionName(c) + "(String accum, LRField f, String op)";
            }
        } else if(fc.startsWith("CF")) {
            if(fc.charAt(2) == 'X') {
                return getFunctionName(c) + "(String accum, ViewColumn vc, String op)";
            } else {        
                return getFunctionName(c) + "(String accum, LRField f, String op)";
            }
        } else if(fc.endsWith("X")) {
            return getFunctionName(c) + "(String accum, ViewColumn vc)";
        } else {
            return getFunctionName(c) + "(String accum, LRField f)";
        }
    }

    private String processNameValue(FunctionCodeDefinition c) {
        String fcname = getFunctionName(c);
        String nvfc; 
        if(c.getFunctionCode().startsWith("CF")) {
            nvfc = fcname + "(String accum, String rhs, String op)";
        } else {
            nvfc = fcname + "(String accum, String rhs)";
        }

        return  nvfc;
    }

    private String getFunctionName(FunctionCodeDefinition c) {
        return "get" + c.getFunctionCode();
    }

    private void writeJavaCode(Map<String, Object> nodeMap) {
        String dir = definitions.getTargetDirectory();
        String pkg = definitions.getPackageName();
        Path dirPath = Paths.get(dir);
        Path trg = dirPath.resolve(pkg.replace('.', '/'));
        trg.resolve(pkg);
        trg.toFile().mkdirs();
        //writeModelWithTemplateToPath(nodeMap, "ltRecordFactory.ftl", trg.resolve("LtRecordFactory.java"));
        writeModelWithTemplateToPath(nodeMap, "ltFunctionCodeFactory.ftl", trg.resolve("LtFunctionCodeFactory.java"));
    }

}
