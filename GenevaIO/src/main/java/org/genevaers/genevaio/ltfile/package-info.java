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
  * Classes to read and write logic table files.
  * <p><b>What is a Logic Table?</b></p>
  * <p>A logic table is a sequence of {@link org.genevaers.genevaio.ltfile.LTRecord} entires.</p>
  * <p>Each table entry represents a Function Code. Which is an instruction to the Performance Engince.</p>
  * <p>A loose but inaccurate equivalent of a functon code is a byte code in Java.</p>
  * <p>A table describing the function codes can be found <a href="file:../../../../../../../ComponentGenerator/target/FCDescriptions.dot.svg">here</a>
  */ 
package org.genevaers.genevaio.ltfile;

