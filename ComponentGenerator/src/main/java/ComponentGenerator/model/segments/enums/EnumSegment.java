package ComponentGenerator.model.segments.enums;

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
import java.util.List;

import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import com.google.common.flogger.FluentLogger;

import ComponentGenerator.model.segments.ModelSegment;
import freemarker.template.Configuration;

public class EnumSegment extends ModelSegment {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private List<EnumItem> enums;

    public List<EnumItem> getEnums() {
        return enums;
    }

    public void setEnums(List<EnumItem> enums) {
        this.enums = enums;
    }

    @Override
    public void readYaml() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeOutputs(ObjectMapper mapper, Configuration cfg) throws StreamReadException, DatabindException, JacksonIOException {
        EnumGenerator eg = new EnumGenerator();
        eg.setFreeMarkerCfg(cfg);
        for(EnumItem ei : getEnums()) {
            String enumFileName = getSourceDirectory() + "/" +ei.getSource();
            MetaDataEnums genEnum = mapper.readValue(new File(enumFileName), MetaDataEnums.class);
            ei.setEnum(genEnum);
            logger.atInfo().log("Add %s from %s", ei.getName(), enumFileName);
        }
        eg.writeOutputs(this);
    }
}
