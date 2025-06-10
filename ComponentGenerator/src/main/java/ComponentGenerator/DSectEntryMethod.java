package ComponentGenerator;

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


import java.util.List;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Add a Method to the FreeMarker world to generate the DSECT string
 */
public class DSectEntryMethod implements TemplateMethodModelEx {

    // Generate the dsect entry for the assembler program 
    // Accepting the record name, field details and arg number as args
    @Override
    public String exec(List args) throws TemplateModelException {
        TemplateScalarModel recType = (TemplateScalarModel)args.get(0);
        TemplateScalarModel fName = (TemplateScalarModel)args.get(1);
        TemplateScalarModel fType = (TemplateScalarModel)args.get(2);
        TemplateScalarModel argNum = (TemplateScalarModel)args.get(3);
        String nameSection = fName.getAsString();
        if(argNum.getAsString().length() > 0) {
            nameSection += argNum.getAsString();
        }
        StringUtils.rightPad(fName.getAsString().toUpperCase(), 42);
        String entry = recType + "_" + StringUtils.rightPad(nameSection, 42) + "DS " + fType;
        return entry.toUpperCase();
    }
    
}
