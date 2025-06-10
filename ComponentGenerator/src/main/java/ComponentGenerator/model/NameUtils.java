package ComponentGenerator.model;

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


import org.apache.commons.lang3.StringUtils;

/**
 * Utils class for string processing.
 */
public class NameUtils {
/**
 * Convert the YAML snake case to camel. .
 */
public static String getCamelCaseName(String in, boolean capFirst) {
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
            if(capFirst)
                return StringUtils.capitalize(in);
            else 
                return in;
        }
	}

}
