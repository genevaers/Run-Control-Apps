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


import java.util.ArrayList;
import java.util.List;

import ComponentGenerator.model.segments.components.members.Member;

/**
 * A POJO Class to map a component defined in a component YAML file.
 * 
 * 
 * <p>Class instances will be populated via Jackson when the {@link ComponentSegment#readYaml()} is called.</p>
 * <p>It has the class name, a list of members and the name of the associated VDP Record.</p> 
 * 
 * <p>The components are used in the FreeMarker template <a href="file:../../../../../../../ComponentGenerator/FreeMarkerTemplates/compJavaObject.ftl">compJavaObject.ftl</a>
 */
public class Component {

    private String componentName;
    private String vdpSource;
    private List<Member> members = new ArrayList<>();

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> properties) {
        this.members = properties;
    }

    public String getVdpSource() {
        return vdpSource;
    }

    public void setVdpSource(String vdpSource) {
        this.vdpSource = vdpSource;
    }


}
