package org.genevaers.testframework;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

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

import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.genevaers.testframework.yamlreader.Spec;
import org.genevaers.testframework.yamlreader.SpecFiles;
import org.genevaers.testframework.yamlreader.YAMLReader;
import org.genevaers.utilities.GersEnvironment;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.flogger.FluentLogger;

public class SpecGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final String WELCOME_MESSAGE = "Hello, welcome to the server";
    private static final String BLACK = "\\[001b[0;30m\\]";
    private static final String BLACKB = "\\[001b[1;30m\\]";
    private static final String RED = "\u001b[0;31m";
    private static final String REDB = "\\001b[31m";
    private static final String GREEN = "\u001b[32m";
    private static final String GREENB = "\u001b[1;32m\\]";
    private static final String YELLOW = "\u001b[33m";
    private static final String YELLOWB = "\u001b[1;33m\\]";
    private static final String BLUE = "\u001b[0;34m";
    private static final String BLUEB = "\u001b[1;34m\\]";
    private static final String PURPLE = "\u001b[0;35m";
    private static final String PURPLEB = "\u001b[1;35m\\]";
    private static final String CYAN = "\u001b[0;36m";
    private static final String CYANB = "\u001b[1;36m\\]";
    private static final String WHITE = "\u001b[0;37m\\]";
    private static final String WHITEB = "\u001b[1;37m\\]";
    private static final String RESET = "\u001b[0m";
    private static final String CLEARSCREAN = "\u001b[27m";

    private static final String DEVGERS_TEST_SPEC_LIST = "devspecfilelist.yaml";

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private YAMLReader yr;
    private List<String> allSpecs;
    private SpecFiles specFiles;
    private Spec spec;
    private boolean notdone=true;
    private GersEnvironment fmEnv;

    public static void main(String[] args) {
        System.out.println(GREEN + "Spec Generator\n" + RESET);
        SpecGenerator specGen = new SpecGenerator();
        while(specGen.isNotDone()) {
            specGen.showMenu();
        }
    }

    SpecGenerator() {
    }

    public boolean isNotDone() {
        return notdone;
    }

    @SuppressWarnings("resource")
    private void showMenu() {
        StringBuilder menu = new StringBuilder();
        menu.append(YELLOW+"Current SpecFileList: "+RESET + fmEnv.get("GERS_TEST_SPEC_LIST")+"\n");
        menu.append(YELLOW+"Current spec        : ");
        if(spec!=null) {
            menu.append(CYAN+spec.getCategory()+"/"+spec.getName()+RESET+"\n\n");
        } else {
            menu.append(RED+"not set\n"+RESET);
        }
        menu.append(PURPLE+"\nSpeclist"+RESET+ "\n");
        menu.append("  c) Create a new specfilelist "+YELLOW+"(and create a new one. Use s to see what is in it)"+RESET+ "\n");
        menu.append("  c) Select spec from the specfilelist "+YELLOW+"(defined in the environmen variable SPEC)"+RESET+ "\n");
        menu.append("  l) Set the current category/spec "+YELLOW+"(existing spec)"+RESET+ "\n");
        menu.append("  n) Make new spec "+YELLOW+"(and append to specfilelist)"+RESET+ "\n");
        
        // menu.append(PURPLE+"Process Spec"+RESET+ "\n");
        // menu.append("  t) Build the tests from the development spec file list"+ "\n");
        
        // menu.append(YELLOW+"Test Results"+RESET+ "\n");
        // menu.append("  s) Show test results (text)"+ "\n");
        // menu.append("  h) Show test results (html)"+ "\n");
        // menu.append("  x) Clear test results"+ "\n");
        
        menu.append(YELLOW+"Specs"+RESET+ "\n");
        menu.append("  c) Create a new spec "+YELLOW+"(and create a new one. Use s to see what is in it)"+RESET+ "\n");
        menu.append("  c) Add existing spec "+YELLOW+"(and create a new one. Use s to see what is in it)"+RESET+ "\n");
        menu.append("  c) Edit spec "+YELLOW+"(and create a new one. Use s to see what is in it)"+RESET+ "\n");
        menu.append("  c) Delete spec "+YELLOW+"(and create a new one. Use s to see what is in it)"+RESET+ "\n");

        menu.append(GREEN+"Tests"+RESET+ "\n");
        //menu.append("  r) Run test"+ "\n");
        menu.append("  a) Add new test to current spec "+YELLOW+"(helpful but unforgiving - use an editor when you understand tests)"+RESET+ "\n");
        menu.append("  a) Show tests in current spec "+YELLOW+"(helpful but unforgiving - use an editor when you understand tests)"+RESET+ "\n");
        menu.append("  a) Add test to current spec "+YELLOW+"(helpful but unforgiving - use an editor when you understand tests)"+RESET+ "\n");
        menu.append("  d) Delete test from spec"+ "\n");
        
        menu.append("  0) Exit"+ "\n");
        
        menu.append("Choose an option:  ");
        System.out.println(menu.toString());
        Scanner sc = new Scanner(System.in);
        handleChoice(sc.nextLine());
        System.out.print("\033[H\033[2J");  
        System.out.flush(); 
        System.out.println(CLEARSCREAN);
    }

    private void handleChoice(String opt) {
        switch(opt) {
            case "c":
            specFiles = new SpecFiles();
            specFiles.setName("Development Tests");
            specFiles.setTemplateSetName("MR91toPE.yaml");
            break;
            case "l":
            break;
            case "n":
            createNewSpec();
            break;
            case "t":
            break;
            case "s":
            break;
            case "h":
            break;
            case "x":
            break;
            case "r":
            break;
            case "a":
            break;
            case "d":
            break;
            case "0":
            notdone=false;
            break;

        }
    }

    private void createNewSpec() {
        getDevSpecFileList();
        // We can use the Spec class
        Spec spec = new Spec();
        spec.setName(promptedRead("Enter new spec name" + YELLOW + " (up to 7 characters - treated as uppercase)" + RESET));
        spec.setTitle(promptedRead("Enter title" + YELLOW + " (more descriptive than name)" + RESET));
        spec.setDescription(promptedRead("Enter description" + YELLOW + " (what are these group of tests doing?)" + RESET));
        spec.setCategory("dev/"+promptedRead("Enter category" + YELLOW + " (single word eg Arithmetic)" + RESET));

        specFiles.getSpecs().add(spec.getCategory() + "/" + spec.getName() + ".yaml");

        confirmAndSave();

        // We can use the Yaml magic to save the spec at the end after a confirmation?
    }

    @SuppressWarnings("resource")
    private String promptedRead(String p) {
        Scanner sc = new Scanner(System.in);
        System.out.println(p);
        return sc.nextLine();
    }

    private void confirmAndSave() {
        try {
            String cfrm = mapper.writeValueAsString(specFiles);
            String yn = promptedRead("This what you expect?\n\n" + cfrm + YELLOW + "y/n (default y)");
            if(!yn.equalsIgnoreCase("n")) {
                saveSpecList();
                saveSpec();
            }
        } catch (JsonProcessingException e) {
            logger.atSevere().log("Exception occurred in create new spec  \n%s", e.getMessage());
        }
    }

    private void saveSpec() {
    }

    private void saveSpecList() {
        try {
             mapper.writeValue(new File(DEVGERS_TEST_SPEC_LIST), specFiles);
        } catch (IOException e) {
            logger.atSevere().log("Exception occurred in save dev spec list \n%s", e.getMessage());
        }
    }

    private void getDevSpecFileList() {
        yr = new YAMLReader();
        try {
            specFiles = yr.readSpecFileList(new File(DEVGERS_TEST_SPEC_LIST));
        } catch (IOException e) {
            logger.atSevere().log("Exception occurred in get dev spec list \n%s", e.getMessage());
        }
    }
}
