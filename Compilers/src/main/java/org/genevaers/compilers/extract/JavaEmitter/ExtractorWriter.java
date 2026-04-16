package org.genevaers.compilers.extract.JavaEmitter;



import java.io.File;

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
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.genevaers.compilers.extract.JavaEmitter.generators.ExtractRecordGenerator;
import org.genevaers.compilers.extract.JavaEmitter.generators.LookupInfo;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersFile;
import com.google.common.flogger.FluentLogger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ExtractorWriter {

	private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final static String EXTRACTOR_TEMPLATE = "FreeMarkerTemplates/ExtractJava.ftl";

	private static  Configuration cfg;

    //private static Collection<JoinGenerator> joins;
    protected static Collection<LookupInfo> joins;

	public static void write(List<String> filterRecs, List<String> columnRecs, List<String> inputdds, int outLen, int lrLen){
		configureFreeMarker();
        Template template;
        try {
            //ClassLoader loader = Thread.currentThread().getContextClassLoader();
            template = cfg.getTemplate(EXTRACTOR_TEMPLATE);
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("filterRecs", filterRecs);
            nodeMap.put("columnRecs", columnRecs);
            nodeMap.put("inputdds", inputdds);
            nodeMap.put("outLen", outLen);
            nodeMap.put("lrLen", lrLen);
            nodeMap.put("joins", joins);
            generateTemplatedOutput(template, nodeMap, "XLT.java");
            logger.atInfo().log("Extract Java Generated");
        } catch (IOException e) {
            logger.atSevere().log("Extract Java error %s",e.getMessage());
        }
	}

	private static  void generateTemplatedOutput(Template template, Map<String, Object> nodeMap, String reportFileName) {
        //Generate the output file into the generated source area of the Performance Engine
        String genSrcDir = GersConfigration.getPerformanceEngineGenSourceDir();
        File targdir = new File(genSrcDir);
        targdir.mkdirs();
        reportFileName = genSrcDir + System.getProperty("file.separator") + reportFileName;
        try(Writer fw = new GersFile().getWriter(reportFileName)) {
	    	template.process(nodeMap, fw);
		} catch (IOException | TemplateException e) {
			logger.atSevere().log("Template generation failed %e", e.getMessage());;
		}
    }


    private  static void configureFreeMarker() {
		cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(ExtractorWriter.class, "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}


    public static void addJoinInitialisation() {
        joins = ExtractRecordGenerator.getJoins();
    }



}
