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


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.genevaers.testframework.yamlreader.GersTest;
import org.genevaers.testframework.yamlreader.PassViews;
import org.genevaers.testframework.yamlreader.Spec;
import org.genevaers.testframework.yamlreader.SpecFileSets;
import org.genevaers.testframework.yamlreader.SpecFiles;
import org.genevaers.testframework.yamlreader.TemplateSet;
import org.genevaers.testframework.yamlreader.TemplateSetEntry;
import org.genevaers.testframework.yamlreader.YAMLReader;

import com.google.common.flogger.FluentLogger;

/*
** Keeper of the tests
*/
public class TestRepository  {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static SpecFileSets specFileSets;
    private static Map<String, Spec> specRepo = new TreeMap<>();
    private static Map<Integer, Spec> specNum2Names = new HashMap<>();
    private static Map<Integer, GersTest> testNum2Tests = new HashMap<>();


    private static YAMLReader yr;

    private static TemplateSet templateSet;

    private TestRepository() {
        //Make sure we can't intantiate this - there will be only one
    }

    public static void buildTheRepo(File specFileList, String overrideTemplateSet) {
        yr = new YAMLReader();
        try {
            //specFiles = yr.readSpecFileList(specFileList);
            specFileSets = yr.readSpecFileSets(specFileList);
            processSpecFileSets(overrideTemplateSet);
        } catch (IOException e) {
            logger.atSevere().log("Error in building the Repo: \n%s", e.getMessage());
        }
    }

    private static void processSpecFileSets(String overrideTemplateSet) {
        for(SpecFiles specFiles : specFileSets.getSpecFileSets()) {
            TemplateSet ts = readTemplateSet(overrideTemplateSet != null ? overrideTemplateSet : specFiles.getTemplateSetName());
            specFiles.addTemplateSet(ts);
            specFiles.buildTemplateTypeLists();
            readTheTestSpecs(specFiles);
        }
    }

    private static void readTheTestSpecs(SpecFiles sf) {
        for (String specName : sf.getSpecs()) {
            Spec spec = readSpec(specName);
            specRepo.put(specName, spec);
            spec.setParent(sf);
        }
    }

    private static Spec readSpec(String s) {
        Spec spec = null;
        try {
            // Build a Spec object from the spec file
            spec = yr.readSpec("spec/" + s);
            readPassViewsIfNeeded(spec);
        } catch (IOException e) {
            logger.atSevere().log("Exception in reading Test specs \n%s", e.getMessage());
        }
        return spec;
    }

   
    private static void readPassViewsIfNeeded(Spec spec) {
        if(spec.isHaspassviews()) {
            for(GersTest t :spec.getTests()) {
                if(t.getPassviews() != null) {
                    Path basePath = Paths.get("spec");
                    Path catPath = basePath.resolve(spec.getCategory());
                    t.setPassViewEntries(readPassViews(catPath.resolve(t.getPassviews())));
                }
            }
        }
    }

    private static PassViews readPassViews(Path passViewsPath) {
        try {
            return yr.readPassViews(passViewsPath.toFile());
        } catch (IOException e) {
            logger.atSevere().log("Exception in reading Pass views \n%s", e.getMessage());
            return null;
        } 
    }

    public static GersTest getTest(int num) {
        return testNum2Tests.get(num);
    }

    public static List<String> getSelectionList() {
        List<String> selList = new ArrayList<>();
        int specNum = 1;
        int testNum = 1;
        for(Entry<String, Spec> si : specRepo.entrySet()) {
            Spec spec = si.getValue();
            selList.add(SpecSelectFormatter.format(spec, specNum));
            specNum2Names.put(specNum++, spec);
            for(GersTest t :si.getValue().getTests()) {
                selList.add(TestSelectFormatter.format(t, testNum));
                t.setSpecPath(spec);
                testNum2Tests.put(testNum++, t);
            }
        }
        return selList;
    }

    public static List<String> getSpecSelectionList() {
        List<String> selList = new ArrayList<>();
        int specNum = 1;
        for(Entry<String, Spec> si : specRepo.entrySet()) {
            Spec spec = si.getValue();
            selList.add(SpecSelectFormatter.formatWithNum(spec, specNum));
            specNum2Names.put(specNum++, spec);
        }
        return selList;
    }

    public static Spec getSpec(int s) {
        return specNum2Names.get(s);
    }

    private static TemplateSet readTemplateSet(String tempSet) {
        TemplateSet templateSet = null;
        logger.atInfo().log("RequiredTemplateSet '%s'", tempSet);
        try {
            templateSet = yr.yaml2TemplateSet(new File("templateSets/" + tempSet));
        } catch (IOException e) {
            logger.atSevere().log("Exception in reading template sets \n%s", e.getMessage());
        }
        return templateSet;
    }

    public static List<TemplateSetEntry> getJCLTemplates(GersTest t) {
        return null;
    }

    public static List<TemplateSetEntry> getConfigTemplates() {
        return null;
    }

    public static Iterator<Spec> getSpecIterator() {
        return specRepo.values().iterator();
    }
}
