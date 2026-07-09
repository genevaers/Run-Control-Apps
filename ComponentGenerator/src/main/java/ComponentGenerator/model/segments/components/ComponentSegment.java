package ComponentGenerator.model.segments.components;

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

public class ComponentSegment extends ModelSegment {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private List<ComponentItem> components;

    public List<ComponentItem> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentItem> components) {
        this.components = components;
    }

    @Override
    public void readYaml() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeOutputs(ObjectMapper mapper, Configuration cfg) throws StreamReadException, DatabindException, JacksonIOException {
        //This seems like a circular dependency
        //we need the Generator
        //and the generator needs us
        //Should pass down to the generator what it needs
        ComponentGenerator compgen = new ComponentGenerator();
        compgen.setFreeMarkerCfg(cfg);
        for(ComponentItem ci : getComponents()) {
            String compFileName = getSourceDirectory() + "/" +ci.getSource();
            Component comp = mapper.readValue(new File(compFileName), Component.class);
            ci.setComponent(comp);
            logger.atInfo().log("Add %s from %s", ci.getName(), compFileName);
        }
        compgen.writeOutputs(this);
    }

}
