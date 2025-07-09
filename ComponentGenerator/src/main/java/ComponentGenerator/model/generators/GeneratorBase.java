package ComponentGenerator.model.generators;

import java.io.BufferedWriter;

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


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.flogger.FluentLogger;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Base class for generation.
 * <p>Holds common generation functions</p>
 */
public class GeneratorBase {
    
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	protected static final String DETAILS = "details";
	protected static final String ITEMS = "items";
	private Configuration cfg;

    protected String getFieldEntry() {
        return "";

    }

    protected String getCamelCaseName(String in, boolean capFirst) {
        if(in.contains("_")) {
            StringBuilder cc = new StringBuilder();
            String[] parts = in.toLowerCase().split("_");
            for (int p=0; p<parts.length; p++) {
                if(p == 0) {
                    if(capFirst)
                        cc.append(StringUtils.capitalize(parts[p]));
                    else
                        cc.append(parts[p]);
                } else {
                    cc.append(StringUtils.capitalize(parts[p]));
                }
            }
            return cc.toString();
        } else {
            return in.toLowerCase();
        }
	}

    protected void writeModelWithTemplateToPath(Map<String, Object> nodeMap, String templateName, Path to) {
        logger.atInfo().log("Write %s via %s", to, templateName);
        Template template;
        try {
            template = cfg.getTemplate(templateName);
            generateTemplatedOutput(template, nodeMap, to);
        } catch (IOException e) {
            logger.atSevere().log("Freemarker template error " + e);
        }
    }

    protected void generateTemplatedOutput(Template temp, Map<String, Object> templateModel, Path target) {
        Writer fstream = null;
        try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
            fstream = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            temp.process(templateModel, fstream);
        } catch (IOException e) {
            logger.atSevere().log("File error " + e);
        } catch (TemplateException e) {
            logger.atSevere().log("Freemarker error " + e);
        }
    }

    public void setFreeMarkerCfg(Configuration cfg) {
		this.cfg = cfg;
	}

    public Configuration getFreeMarkerCfg() {
        return cfg;
    }

    protected Template getTemplate(String templateName)
    {
        Template t = null;
        try {
            t = cfg.getTemplate(templateName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return t;
    }
}
