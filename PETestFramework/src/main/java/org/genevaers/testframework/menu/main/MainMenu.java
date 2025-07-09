package org.genevaers.testframework.menu.main;

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


import java.util.Iterator;

import org.genevaers.testframework.TestDriver;
import org.genevaers.testframework.TestRepository;
import org.genevaers.testframework.yamlreader.GersTest;
import org.genevaers.testframework.yamlreader.Spec;
import org.genevaers.utilities.menu.Menu;
import org.genevaers.utilities.menu.MenuItem;
import org.genevaers.utilities.menu.MenuSetting;

public class MainMenu extends Menu{

    private RunTest rtm;
    private int numPassed;
    private int numFailed;
    private int numUnknown;

    public MainMenu() {
        menuItems.add(new ShowCurrentTests()); 
        menuItems.add(new ClearTestResults()); 
        menuItems.add(new SelectTest());
        menuItems.add(new SelectSpec());
        rtm = new RunTest();
        menuItems.add(rtm);
        menuItems.add(new RunAllTests());
        menuItems.add(new ShuffleOff());
    }

    @Override
    public void showSettings(StringBuilder menuStr) {
        menuStr.append(YELLOW+"\nTest Run Settings\n");
        showUSSSettings(menuStr);
    }

    private void showUSSSettings(StringBuilder menuStr) {
        showCommonSettings(menuStr);
        showTestSummary(menuStr);
    }

    private void showTestSummary(StringBuilder menuStr) {
        menuStr.append(BLUE + "\nSummary");
        getTestNumbers();
        menuStr.append(String.format("\n    %sPassed: %d %sFailed: %d %sUnknown: %d", GREEN, numPassed, RED, numFailed, YELLOW, numUnknown));
    }

    private void getTestNumbers() {
        clearTestNumbers();
        Iterator<Spec> si = TestRepository.getSpecIterator();
        while (si.hasNext()) {
            Spec s = si.next();
            Iterator<GersTest> ti = s.getTests().iterator();
            while (ti.hasNext()) {
                GersTest t = ti.next();
                if (t != null) {
                    if (t.getResult().getMessage().startsWith("pass")) {
                        numPassed++;
                    } else if (t.getResult().getMessage().startsWith("fail")) {
                        numFailed++;
                    } else {
                        numUnknown++;
                    }
                } else {
                    System.out.println("null test");
                }
            }
        }
    }

    private void clearTestNumbers() {
        numFailed = 0;
        numPassed = 0;
        numUnknown = 0;
    }

    private void showCommonSettings(StringBuilder menuStr) {
        Menu.addMenuSummaryItem(menuStr, "GERS_RCA_JAR_DIR",System.getenv("GERS_RCA_JAR_DIR"),"");
        Menu.addMenuSummaryItem(menuStr, "GERS_TEST_SPEC_LIST",System.getenv("GERS_TEST_SPEC_LIST"),"");
        Menu.addMenuSummaryItem(menuStr, "GERS_TEST_HLQ", System.getenv("GERS_TEST_HLQ"), "");
        Menu.addMenuSummaryItem(menuStr, "GERS_ENV_HLQ", System.getenv("GERS_ENV_HLQ"), "");
        for(MenuItem mi : menuItems) {
            if(mi instanceof MenuSetting) {
                Menu.addMenuSummaryItem(menuStr, mi.getPrompt(), ((MenuSetting)mi).getValue(), mi.getComment());
            }
        }
    }

}
