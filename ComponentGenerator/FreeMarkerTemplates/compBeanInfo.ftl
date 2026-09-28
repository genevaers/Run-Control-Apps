<#ftl strip_whitespace="true">
package org.genevaers.repository.components;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ${component.componentName}BeanInfo extends SimpleBeanInfo {
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] properties = new PropertyDescriptor[] {
<#list component.members as member>
<#if member.type != "map" && member.type != "existing">
                new PropertyDescriptor("${member.name}", ${component.componentName}.class,
                        "<#if member.type == "boolean">is<#else>get</#if>${member.name?cap_first}",
                        "set${member.name?cap_first}")<#if member_has_next>,</#if>
</#if>
</#list>
            };
            for (PropertyDescriptor property : properties) {
                switch (property.getName()) {
<#list component.members as member>
<#if member.displayName??>
                    case "${member.name}":
                        property.setDisplayName("${member.displayName}");
                        break;
</#if>
</#list>
                    default:
                        break;
                }
            }
            return properties;
        } catch (IntrospectionException e) {
            return new PropertyDescriptor[0];
        }
    }
}