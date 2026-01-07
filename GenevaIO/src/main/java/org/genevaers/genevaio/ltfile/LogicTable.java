package org.genevaers.genevaio.ltfile;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.genevaers.repository.components.enums.LtRecordType;

public class LogicTable {
	private List<LTRecord> records = new ArrayList<>();
	private LTRecord lastEntry;

	public int getNumberOfRecords() {
		return records.size();
	}

	public int getNumberOfRecords(LtRecordType type) {
		return (int) records.stream().filter(l -> l.getRecordType().equals(type)).count();
	}

	public void add(LTRecord ltRecord) {
		lastEntry = ltRecord;
		// TODO keep type counters as records added
		records.add(ltRecord);
	}

	public Iterator<LTRecord> getIterator() {
		return records.iterator();
	}

	public LTRecord getFromPosition(int i) {
		return records.get(i);
	}

	public LTRecord getLastEntry() {
		return lastEntry;
	}

	public Stream<LTRecord> getStream() {
		return records.stream();
	}

}
