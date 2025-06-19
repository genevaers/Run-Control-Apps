package org.genevaers.testframework.sdsf;

import java.io.IOException;
import java.util.ArrayList;

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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.flogger.FluentLogger;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZUtil;
import com.ibm.zos.sdsf.core.ISFAllocationEntry;
import com.ibm.zos.sdsf.core.ISFException;
import com.ibm.zos.sdsf.core.ISFHeldOutput;
import com.ibm.zos.sdsf.core.ISFHeldOutputRunner;
import com.ibm.zos.sdsf.core.ISFJobDataSet;
import com.ibm.zos.sdsf.core.ISFJobStep;
import com.ibm.zos.sdsf.core.ISFLineResults;
import com.ibm.zos.sdsf.core.ISFRequestResults;
import com.ibm.zos.sdsf.core.ISFRequestSettings;
import com.ibm.zos.sdsf.core.ISFScrollConstants;
import com.ibm.zos.sdsf.core.ISFStatus;
import com.ibm.zos.sdsf.core.ISFStatusRunner;


//Use the ISFActive Runner to look for the Active jobs 
//Once the list is empty then they will have been completed
//But they won't be in the list if not submitted?
//Need to juggle/compare to the ST list?
//Or do we just use the ST list?
public class HeldJobs {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final String prefix_;
    private boolean showSDSFMessages = false;
    private String resultRC = "";
    private int maxrcNum = 0;
    private static final String JESMSGLGDD = "JESMSGLG";

    List<ISFHeldOutput> jobList = null;
    List<ISFHeldOutput> failedJobs = new ArrayList<>();
    List<String> failedSteps = new ArrayList<>();
    private int stepNameNdx;
    private ISFStatusRunner jmrunner;

    public HeldJobs(final String prefix) {
        this.prefix_ = prefix + "*";
    }

    public void run() {
        final ISFRequestSettings settings = new ISFRequestSettings();
        settings.addISFPrefix(prefix_);
        settings.addISFCols("jname jobid ownerid retcode");
        settings.addISFOwner(System.getenv("USER"));
        settings.addISFSort("jobid a");
        settings.addNoModify();
        final ISFHeldOutputRunner runner = new ISFHeldOutputRunner(settings);

        try {
            jobList = runner.exec();
        } catch (ISFException e) {
            // Perform error handling here @P1C
        } finally {
            // List any SDSF messages related to request
            if (showSDSFMessages) {
                runner.getRequestResults().printMessageList(System.out);
            }
        }
    }

    public void show() {
        if (jobList != null) {
            for (ISFHeldOutput statObj : jobList) {
                System.out.println(statObj.toVerboseString());
            }
        }
    }

    public void collectFailures() {
        if (jobList != null) {
            for (ISFHeldOutput statObj : jobList) {
                logger.atFine().log(statObj.toVerboseString());
                String rc = statObj.getValue("retcode");
                if (rc.startsWith("ABEND")) {
                    maxrcNum = Integer.parseInt(rc.substring(6));
                    failedJobs.add(statObj);
                } else if (rc.startsWith("CC ")) {
                    int rcNum = Integer.parseInt(rc.substring(3));
                    logger.atFine().log("RC " + rc + " " + rcNum);
                    if (rcNum > 0) {
                        logger.atFine().log("add Failed Job" + statObj.toVerboseString());
                        failedJobs.add(statObj);
                    }
                }
            }
        }
    }

    public String getDatasetFromLastJob(String errddname) {
        String result = "";
        if (jobList != null) {
            ISFHeldOutput last = jobList.get(jobList.size() - 1);
            logger.atInfo().log("Last job %s RC %s", last.getJName(), last.getValue("retcode"));
            result = findOutputInLastJob(last.getJName(), errddname);
        }
        return result;
    }

    public String findOutputInLastJob(String jobName, String dataset) {
        String output = null;
        List<ISFStatus> statObjList = getIFStatusForJob(jobName);
        if (statObjList != null) {
            for (ISFStatus statObj : statObjList) {
                logger.atInfo().log(statObj.getJName() + " datasets");
                List<ISFJobDataSet> jdsObjList = getDatasetList(statObj);
                ISFJobDataSet ds = findJobDataset(jdsObjList, dataset);
                logger.atInfo().log("found output %s", ds.getDDName());
                output = readSpoolDataser(ds);
                logger.atInfo().log(output);
            }
        }
        return output;
    }

    private String readSpoolDataser(ISFJobDataSet ds) {
        StringBuilder sb = new StringBuilder();
        logger.atFine().log("Examining " + JESMSGLGDD + "...");
        // Browse the job data set
        ds.browseAllocate();

        // Browse the spool data sets
        try {
            final List<ISFAllocationEntry> allocList = jmrunner.getRequestResults().getAllocationList();

            if (allocList != null) {
                //There should only be one
                logger.atFine().log("Number of allocated datasets " + allocList.size());
                // Loop for all allocated data sets - there should only be one?
                ISFAllocationEntry allocEntry = allocList.get(0);
                final String ddspec = "//DD:" + allocEntry.getDDName();

            // Create a zFile to represent the data set
            final ZFile zFile = new ZFile(ddspec, "rb,type=record,noseek");
    
            // Read each record in the data set
//            try {
                final byte[] recBuf = new byte[zFile.getLrecl()];
                int nread;
                final String encoding = ZUtil.getDefaultPlatformEncoding();
                while ((nread = zFile.read(recBuf)) >= 0) { // @KQC
                    sb.append(new String(recBuf, 0, nread, encoding));
                    sb.append("\n");
                }
                zFile.close();
           }
          }  catch (IOException e) {
        }

        // clear startlinetoken for the next job data set
        //settings.removeISFStartLineToken();        
        return sb.toString();
    }

    private List<ISFJobDataSet> getDatasetList(ISFStatus statObj) {
        List<ISFJobDataSet> jdsObjList = null; // Returned list of job data sets
        final ISFRequestSettings settings = new ISFRequestSettings();
        // Initial scrolltype set to read from TOP of the job data set
        settings.addISFScrollType(ISFScrollConstants.Options.TOP);
        try {
            jdsObjList = statObj.getJobDataSets();
        } catch (ISFException e) {
            // Perform error handling here @P1C
        }
        return jdsObjList;
    }

    private List<ISFStatus> getIFStatusForJob(String jobName) {
        List<ISFStatus> statObjList = null; // Returned list of jobs
        // Create a request settings
        final ISFRequestSettings settings = new ISFRequestSettings();

        final StringBuilder sb = new StringBuilder();
        sb.append("jname eq ");
        sb.append(jobName);
        settings.addISFFilter(sb.toString()); // Filter requested job

        // Limit the columns returned using the SDSF column (FLD) names
        // rather than the column titles. The interactive COLSHELP
        // command can be used to find the names for each panel.
        settings.addISFCols("jname jobid");

        // Use this setting to improve performance since properties will not be
        // changed
        settings.addNoModify();

        // set settings for the browse operation
        settings.addISFLineLim(30);
        jmrunner = new ISFStatusRunner(settings);
        // Get the list of jobs
        try {
            statObjList = jmrunner.exec();
        } catch (ISFException e) {
            // Perform error handling here @P1C
        } finally {
            // List any SDSF messages related to request
            // jmrunner.getRequestResults().printMessageList(System.err);
        }
        return statObjList;
    }

    public void showFailedSteps() {
        if (failedJobs != null) {
            for (ISFHeldOutput statObj : failedJobs) {
                logger.atFine().log(statObj.toVerboseString());
                getJESMSGLGforFailedJob(statObj.getValue("jname"));
            }
            listFails();
        }
    }

    private void getJESMSGLGforFailedJob(String jobName) {
        List<ISFStatus> statObjList = null; // Returned list of jobs
        List<ISFJobDataSet> jdsObjList = null; // Returned list of job data sets
        // Create a request settings
        final ISFRequestSettings settings = new ISFRequestSettings();

        final StringBuilder sb = new StringBuilder();
        sb.append("jname eq ");
        sb.append(jobName);
        settings.addISFFilter(sb.toString()); // Filter requested job

        // Limit the columns returned using the SDSF column (FLD) names
        // rather than the column titles. The interactive COLSHELP
        // command can be used to find the names for each panel.
        settings.addISFCols("jname jobid");

        // Use this setting to improve performance since properties will not be
        // changed
        settings.addNoModify();

        // set settings for the browse operation
        settings.addISFLineLim(30);
        ISFStatusRunner jmrunner = new ISFStatusRunner(settings);
        // Get the list of jobs
        try {
            statObjList = jmrunner.exec();
        } catch (ISFException e) {
            // Perform error handling here @P1C
        } finally {
            // List any SDSF messages related to request
            // jmrunner.getRequestResults().printMessageList(System.err);
        }

        if (statObjList != null) {

            // Process each job
            for (ISFStatus statObj : statObjList) {

                // Get the jobname
                final String jobname = statObj.getJName();

                // Get the jobid name
                final String jobid = statObj.getJobID();

                // Initial scrolltype set to read from TOP of the job data set
                settings.addISFScrollType(ISFScrollConstants.Options.TOP);

                logger.atFine().log("Now processing " + jobname + " " + jobid
                        + "...");
                logger.atFine().log();

                // obtain Job Data Sets for the job
                try {
                    jdsObjList = statObj.getJobDataSets();
                } catch (ISFException e) {
                    // Perform error handling here @P1C
                }

                // find the JESMSGLG data set
                ISFJobDataSet jesmsglg = null;
                jesmsglg = findJobDataset(jdsObjList, JESMSGLGDD);

                if (jesmsglg != null) {
                    lookForComparisonFailsInJesmsglg(settings, jmrunner, jesmsglg);
                } else {
                    logger.atSevere().log("Job Dataset " + JESMSGLGDD + " not found.");
                }
            }
        }
    }

    private void listFails() {
        logger.atInfo().log("Failed Steps");
        failedSteps.stream().forEach(f -> list(f));
    }

    private void list(String f) {
        logger.atInfo().log(f);
    }

    private void lookForComparisonFailsInJesmsglg(final ISFRequestSettings settings, ISFStatusRunner jmrunner,
            ISFJobDataSet jesmsglg) {
        logger.atFine().log("Examining " + JESMSGLGDD + "...");
        // Browse the job data set
        jesmsglg.browseAllocate();

        // List any SDSF messages related to allocation
        // Make this an optional fine log level
        // jmrunner.getRequestResults().printMessageList(System.err);

        // Browse the spool data sets
        try {
            parseJesmsglg(jmrunner.getRequestResults());
        } catch (IOException e) {
            // Perform error handling here @P1C
        }

        // clear startlinetoken for the next job data set
        settings.removeISFStartLineToken();
    }

    private ISFJobDataSet findJobDataset(List<ISFJobDataSet> jdsObjList, String jobName) {
        return jdsObjList.stream()
                .filter(j -> j.getDDName().equalsIgnoreCase(jobName))
                .findAny()
                .orElse(null);
    }

    /**
     * Reads a spool file and list its contents.
     * 
     * @param results Request results
     * @throws IOException Unable to read file
     */
    private void parseJesmsglg(final ISFRequestResults results) throws IOException {

        // Get allocated ddname list from results object
        final List<ISFAllocationEntry> allocList = results.getAllocationList();

        if (allocList != null) {
            // There should only be one
            logger.atFine().log("Number of allocated datasets " + allocList.size());
            // Loop for all allocated data sets
            for (ISFAllocationEntry allocEntry : allocList) {
                logger.atFine().log("Now processing " + allocEntry + "...");
                parse(allocEntry.getDDName());
            }
        } else {
            logger.atSevere().log("No allocated DDNames found");
        }
    }

    /**
     * Reads a spool data set and displays its contents.
     * 
     * @param ddname Allocated ddname
     * @throws IOException Unable to read data set
     */
    private void parse(final String ddname) throws IOException {

        final String ddspec = "//DD:" + ddname;

        // Create a zFile to represent the data set
        final ZFile zFile = new ZFile(ddspec, "rb,type=record,noseek");

        // Read each record in the data set
        try {
            final byte[] recBuf = new byte[zFile.getLrecl()];
            int nread; // @KQC
            final String encoding = ZUtil.getDefaultPlatformEncoding();
            int state = 0; // 0 = searching 1= gathering 2= ended
            stepNameNdx = 0;
            while (state != 2 && (nread = zFile.read(recBuf)) >= 0) { // @KQC
                final String line = new String(recBuf, 0, nread, encoding); // @KQC
                // System.out.println(line);
                switch (state) {
                    case 0:
                        state = lookupForStepname(line);
                        break;
                    case 1:
                        state = gatherSteps(line);
                        break;
                }
            }
        } finally {
            zFile.close();
        }
    }

    private int gatherSteps(String line) {
        int state = line.contains("ENDED") ? 2 : 1;
        if (state == 1) {
            String stepName = line.substring(stepNameNdx, stepNameNdx + 8);
            String rc = line.substring(stepNameNdx + 21, stepNameNdx + 23);
            if (rc.equals("01")) {
                failedSteps.add(stepName);
            }
        }
        return state;
    }

    private int lookupForStepname(String line) {
        stepNameNdx = line.indexOf("STEPNAME");
        int state = 0;
        if ((stepNameNdx != -1)) {
            logger.atFine().log("STEPNAME found state -> 1 ndx " + stepNameNdx);
            state = 1;
        }
        return state;
    }

    public int getNumJobs() {
        return jobList.size();
    }

    public void setShowSDSFMessages(boolean showSDSFMessages) {
        this.showSDSFMessages = showSDSFMessages;
    }

    public int getMaxRC() {
        if (maxrcNum < 8) {
            if (jobList != null) {
                for (ISFHeldOutput j : jobList) {
                    String rc = j.getValue("retcode");
                    resultRC = rc;
                    if (rc.startsWith("ABEND")) {
                        if (StringUtils.isNumeric(rc.substring(6))) {
                            maxrcNum = Integer.parseInt(rc.substring(6));
                        } else {
                            maxrcNum = 88;
                        }
                        failedJobs.add(j);
                    } else if (rc.startsWith("CC ")) {
                        int rcNum = Integer.parseInt(rc.substring(3));
                        if (rcNum > 4) {
                            failedJobs.add(j);
                        }
                        if (rcNum > maxrcNum) {
                            maxrcNum = rcNum;
                            resultRC = rc;
                        }
                    }
                }
            }
        }
        return maxrcNum;
    }

    public String getResultRC() {
        return resultRC;
    }

    public boolean allJobsCompleted(int numExpectedJobs) {
        return jobList.size() == numExpectedJobs;
    }

    public boolean maxReturnCodeDoesNotExceed(int rcAsInt) {
        return maxrcNum <= rcAsInt;
    }

    public List<ISFJobStep> getJobSteps() {
        List<ISFJobStep> totalJobSteps = new ArrayList<>();
        List<ISFJobStep> jobSteps;
        for (ISFHeldOutput j : jobList) {
            try {
                jobSteps = j.getJobSteps();
                for (ISFJobStep s : jobSteps) {
                    totalJobSteps.add(s);
                }
            } catch (ISFException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return totalJobSteps;
    }

    public List<String> getFailedSteps() {
        return failedSteps;
    }

    public boolean didStepPass(String name) {
        // Passed if not in failed list
        return !failedSteps.contains(name);
    }
}
