package org.genevaers.utilities.menu;

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
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.genevaers.utilities.CommandRunner;

import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.Ansi.Color;


public abstract class MenuItem {

    String key;
    protected String header;
    protected String prompt;
    protected String comment;
    protected Ansi.Color color = Ansi.Color.DEFAULT;
    protected String nextMenu;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Ansi.Color getColor() {
        return color;
    }

    public String getNextMenu() {
        return nextMenu;
    }

    public abstract boolean doIt();

    // These functions probably do not belong here
    // They should be static in a RCAMenuExecutor or something
    // protected void runRCG(Path rcDir) {
    //     CommandRunner runner = new CommandRunner();
    //     try {
    //         String os = System.getProperty("os.name");
    //         if(os.startsWith("Windows")) {
    //             runner.run("gvbrcg.bat", rcDir.toFile());
    //         } else {
    //             runner.run("gvbrcg", rcDir.toFile());
    //         }
    //     } catch (IOException | InterruptedException e) {
    //          logger.atSevere().log("IO exception on windows file run\n%s", e.getMessage())    //     }
    // }

    /*
     * The game is all about setting up to call this function.
     * The function above may be called first to generate 
     * a local RC set. Once the appropriate MR91Parms have been configured.
     * Or we may get the RC set by transferring it via FTP.
     * Or we may just be processing/re-processling a local RC set.
     */
    // protected void generateFlow() {
    //     try {
    //         flow.makeRunControlAnalyserDataStore(null);
    //         flow.setTargetDirectory(RCAGenerationData.getRcSet());
    //         flow.generateFlowDataFrom(RCAGenerationData.getRcSet(),
    //          true,  //default to generate
    //         false,
    //         ""
    //         );
    //     } catch (Exception e) {
    //          logger.atSevere().log("Exception on gerenate flow\n%s", e.getMessage());
    //     }
    // }

    //Utility function
    protected String chooseRcSetFromlist(String baseDir) {
        File rcsDir = new File(baseDir);
        boolean failed = false;
        Scanner sc = new Scanner(System.in);
        if (rcsDir.exists()) {
            String[] rcs = rcsDir.list();
            int num = 0;
            for (String rc : rcs) {
                System.out.println(num + ") " + rc);
                num++;
            }
            System.out.println("\nChoose an RC set - enter its number");
            String selection = sc.nextLine();
            if (StringUtils.isNumeric(selection)) {
                int val = Integer.parseInt(selection);
                if (val < rcs.length) {
                    return rcs[val];
                } else {
                    System.out.println(ansi().fg(Color.RED).a("Selection out of range.").reset());
                    failed = true;
                }
            } else {
                failed = true;
                System.out.println(ansi().fg(Color.RED).a("None numeric selection.").reset());
            }
        } else {
            failed = true;
            System.out.println(ansi().fg(Color.RED).a("There are no RC sets to select.").reset());
        }
        if (failed) {
            System.out.println(ansi().fg(Color.GREEN).a("Enter to continue.").reset());
            sc.nextLine();
        }
        return "";
    }
}
