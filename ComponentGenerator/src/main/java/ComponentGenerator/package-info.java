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

 /**
  * Command line level package for the Component Generator.
  * <p>The Component Generator is first built and then used to generate the classes we use within the Run Control Apps.</p>
  * <p>There is another discussion of the Component Generator in the <a href="https://github.com/genevaers/Run-Control-Apps/tree/main/ComponentGenerator">Github repo</a></p>
  * <p>The GenevaERS model is made up from a number of segments defined in the <a href="file:../../../../ComponentGenerator//src/main/resources/modelConfig.yaml">model config</a>.</p>
  * <p>The individual segements are defined in YAML files located in 
  * <a href="file:../../../../ComponentGenerator/src/main/resources">resources</a></p>
  * <p>The segments of the our model describe</p>
  * <ul>
  * <li>The Repository <a href="file:../../../../ComponentGenerator/src/main/resources/componentconfig.yaml">Components</a> see {@link ComponentGenerator.model.segments.components}</li>
  * <li>The VDP <a href="file:../../../../ComponentGenerator/src/main/resources/vdpconfig.yaml">Records</a> see details {@link ComponentGenerator.model.segments.record}</li>
  * <li>The Logic Table <a href="file:../../../../ComponentGenerator/src/main/resources/ltconfig.yaml">Records</a> see details {@link ComponentGenerator.model.segments.record}</li>
  * <li>The logic Table <a href="file:../../../../ComponentGenerator/src/main/resources/functionCodes.yaml">Function Codes</a> see details {@link ComponentGenerator.model.segments.functioncodes}</li>
  * <li>Our component <a href="file:../../../../ComponentGenerator/src/main/resources/enumsAndEquates.yaml">Enumerations</a> see details {@link ComponentGenerator.model.segments.enums}</li>
  * </ul>
  * 
  * <p>The ComponentGenerator also produces diagrams to capture the relationship between the <a href="file:../../../../ComponentGenerator/target/VDPRecs2Components.dot.svg">VDP Records and the Components</a>.</p>
  * <p>And a <a href="file:../../../../ComponentGenerator/target/LTandFCs.dot.svg">summary</a> of the Logic Table records and their associated function codes.</p>
  * <p>An additional diagram shows the <a href="file:../../../../ComponentGenerator/target/VDPMngrecs.dot.svg">VDP Managment Records</a>. Those records for which there are no generated Components.</p>
  * 
  * The <a href="https://github.com/genevaers/Run-Control-Apps/tree/main/ComponentGenerator#description">Description</a> section of the Github page mentioned above shows the basic flow.
  */ 
package ComponentGenerator;

