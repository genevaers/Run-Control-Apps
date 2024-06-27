package org.genevaers.repository.data;

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


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.genevaers.repository.components.enums.DateCode;
import org.junit.jupiter.api.Test;

public class NormalisedDatesTest {
    
	@Test
	public void testNormalisations() throws Exception {
		assertEquals("0000000000000000", NormalisedDate.get("123", DateCode.CCYYMMDD));
		assertEquals("2023110700000000", NormalisedDate.get("20231107", DateCode.CCYYMMDD));
		assertEquals("0023110700000000", NormalisedDate.get("231107", DateCode.YYMMDD));
		assertEquals("2023110700000000", NormalisedDate.get("11072023", DateCode.MMDDCCYY));
	}
}
