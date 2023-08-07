package org.genevaers.testframework.menu.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

import org.fusesource.jansi.Ansi;
import org.genevaers.testframework.TestReporter;
import org.genevaers.utilities.TestEnvironment;
import org.genevaers.utilities.menu.Menu;
import org.genevaers.utilities.menu.MenuItem;

public class ShowCurrentTests extends MenuItem {

    public ShowCurrentTests() {
        color = Ansi.Color.MAGENTA;
        header = "Tests";
        prompt = "Show current test status";
    }

    @Override
    public boolean doIt() {
        nextMenu = "Main";
        TestReporter reporter = new TestReporter();
        try {
            reporter.generate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(TestEnvironment.get("LOCALROOT") + "/out/fmoverview.txt"));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            Menu.promptedRead("Enter to continue");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

}
