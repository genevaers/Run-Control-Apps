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
import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.flogger.FluentLogger;

public class LtCoverageYamlReader {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static ObjectMapper yamlMapper;
    private static LTCoverageFile ltCovFile;

    public static LTCoverageFile readYaml(Path input) {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.findAndRegisterModules();
        try {
            ltCovFile = yamlMapper.readValue(input.toFile(), LTCoverageFile.class);
        } catch (IOException e) {
            logger.atSevere().log("read coverage failed\n%s", e.getMessage());
        };
        return ltCovFile;
    }

}
