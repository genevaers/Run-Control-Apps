package org.genevaers.runcontrolanalyser.ltcoverage;

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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.genevaers.genevaio.ltfactory.LtFunctionCodeCache;
import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTable;
import org.genevaers.repository.components.enums.LtRecordType;
import org.genevaers.utilities.GenevaLog;

import com.google.common.flogger.FluentLogger;


public class LTCoverageAnalyser extends LtFunctionCodeCache{

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static LTCoverageFile ltcov = new LTCoverageFile();
    private static LTCoverageFile aggcov;
    private static boolean aggFileInitRequired; 
    private static List<Path> sources = new ArrayList<>();

    //Create a CoverageAnalyser that we can run from a root directory
    //It will then search for child ltcov yaml files and aggregate them
    //Probably better than trying to add as we go

    public LTCoverageAnalyser() {
        super();
    }

	public static void main(String[] args) throws IOException, InterruptedException {
		GenevaLog.formatConsoleLogger(LTCoverageAnalyser.class.getName(), Level.FINE);

        GenevaLog.closeLogger(LTCoverageAnalyser.class.getName());
	}


    public void addDataFrom(Path xltp, LogicTable lt) {
        sources.add(xltp);
        Iterator<LTRecord> lti = lt.getIterator();
        while(lti.hasNext()) {
            LTRecord ltr = lti.next();
            hit(ltr);
        }
    }


    @Override
    public void addFunctionCode(String name, LtRecordType type, String desc, String category) {
        //LtCoverageEntry ce = new LtCoverageEntry();
        LtCoverageEntry ce = getCoverageEntryFromType(name, category, type);
        ce.setName(name);
        ce.setType(type);
        ce.setDescription(desc);
        ce.setCategory(category);
        ltcov.addCoverageEntry(ce);
    }

    private LtCoverageEntry getCoverageEntryFromType(String name, String category, LtRecordType type) {
        LtCoverageEntry ce = null;
        switch(type) {
            case CALC:
            case CC:
            case F0:
            case GENERATION:
            case HD:
            case INVALID:
            case NAME:
            case NAMEVALUE:
            case NV:
            case RE:
            case WR:
                ce = new LtCoverageEntry();
                break;
            case F1:
            case NAMEF1:
                ce = new LTCoverageEntry1Arg();
                if(category.equalsIgnoreCase("Arithmetic")) {
                    ce.setExpectedItems(15); //Maybe via catgory?
                } else {
                    ce.setExpectedItems(LtCoverageEntry.MAXTYPES);
                }
                break;
            case F2:
            case NAMEF2:
                ce = new LTCoverageEntry2Args();
                ce.setExpectedItems(226);
                break;
             default:
                break;
        }
        return ce;
    }

    public void hit(LTRecord ltr) {
        ltcov.hit(ltr);
    }

    public void aggregateCoverage() {
		String locroot = System.getProperty("user.dir");
    	//locroot = locroot.replaceAll("^[Cc]:", "");
    	locroot = locroot.replace("\\", "/");
    	Path root = Paths.get(locroot);

        Path aggPath = root.resolve("aggregateCov.yaml");
        makeAggregationCoverage();

        final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(
            WildcardFileFilter.builder().setWildcards("pass.html").get(), FileFilterUtils.trueFileFilter());

        aggFileInitRequired = true;
        if(aggPath.toFile().exists()) { 
            aggPath.toFile().delete();
        }
        // Walk dir tree
        try {
            Files.walkFileTree(root, visitor);
            logger.atInfo().log("Found %d passed tests", visitor.getPathCounters().getFileCounter().get());
            for(Path p :visitor.getFileList()) {
                aggregateLTFromPath(p);
            }   
            
            LtCoverageYamlWriter.writeYaml(root.resolve("aggregateCov.yaml"), aggcov);
            LtCoverageHtmlWriter.init();
            LtCoverageHtmlWriter.writeTo(root.resolve("aggregateCov.html"), aggcov);
        } catch (IOException e) {
            logger.atSevere().log("aggregateCoverage failed\n%s", e.getMessage());
        }
    }

    private void makeAggregationCoverage() {
        aggcov = new LTCoverageFile();
        aggcov.setLtcoverage("Aggregation");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date dt = Calendar.getInstance().getTime();
        aggcov.setGenerationDate(dateFormat.format(dt));
    }

    private static void aggregateLTFromPath(Path p) {
        Path testPath = p.getParent();
        logger.atInfo().log(testPath.toString());  
        Path ltcovPath = testPath.resolve("ltcov.yaml");
        if(ltcovPath.toFile().exists()) {
            aggcov.addSource(ltcovPath);
            if(aggFileInitRequired) {
                aggcov.loadFrom(LtCoverageYamlReader.readYaml(ltcovPath));
                aggFileInitRequired = false;
            }  else {
                aggcov.aggregateFrom(LtCoverageYamlReader.readYaml(ltcovPath));
            }
        } else {
            logger.atWarning().log("Test passed but no LTCov");  
        }
    }

    public void writeResults(Path root) {
        LtCoverageYamlWriter.writeYaml(root.resolve("ltcov.yaml"), ltcov);
        LtCoverageHtmlWriter.init();
        LtCoverageHtmlWriter.writeTo(root.resolve("ltcov.html"), ltcov);
    }

    public void setName(Path fileName) {
        ltcov.setLtcoverage(fileName.toString());
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date dt = Calendar.getInstance().getTime();
        ltcov.setGenerationDate(dateFormat.format(dt) );
    }

}
