package org.genevaers.testframework;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares two run control files (VDP, XLT or JLT).
 *
 * The files are sequences of records, each starting with a two byte big endian
 * length that includes the length field itself.
 *
 * The first few records (generation and header) carry the date and time the
 * file was made. So differences in digit characters (ASCII or EBCDIC) within
 * those records are ignored. Every other byte must match exactly.
 */
public class ControlFileComparer {

	private static final int HEADER_RECORDS = 3;
	private static final int MAX_REPORTED = 5;

	private ControlFileComparer() {
	}

	/**
	 * @return a description of each difference found, empty if the files match
	 */
	public static List<String> compare(Path expected, Path actual) throws IOException {
		List<String> diffs = new ArrayList<>();
		byte[] e = Files.readAllBytes(expected);
		byte[] a = Files.readAllBytes(actual);
		List<int[]> eRecs = splitRecords(e);
		List<int[]> aRecs = splitRecords(a);
		if (eRecs.size() != aRecs.size()) {
			diffs.add(String.format("record count %d expected %d", aRecs.size(), eRecs.size()));
		}
		int common = Math.min(eRecs.size(), aRecs.size());
		int mismatched = 0;
		for (int i = 0; i < common; i++) {
			String d = compareRecord(i, e, eRecs.get(i), a, aRecs.get(i));
			if (d != null) {
				mismatched++;
				if (diffs.size() < MAX_REPORTED) {
					diffs.add(d);
				}
			}
		}
		if (mismatched > MAX_REPORTED) {
			diffs.add(String.format("%d records differ in total", mismatched));
		}
		return diffs;
	}

	private static String compareRecord(int index, byte[] e, int[] er, byte[] a, int[] ar) {
		if (er[1] != ar[1]) {
			return String.format("record %d length %d expected %d", index + 1, ar[1], er[1]);
		}
		boolean header = index < HEADER_RECORDS;
		for (int i = 0; i < er[1]; i++) {
			byte eb = e[er[0] + i];
			byte ab = a[ar[0] + i];
			if (eb != ab && !(header && isDigit(eb) && isDigit(ab))) {
				return String.format("record %d offset %d byte %02X expected %02X", index + 1, i, ab & 0xFF,
						eb & 0xFF);
			}
		}
		return null;
	}

	private static boolean isDigit(byte b) {
		int v = b & 0xFF;
		return (v >= 0x30 && v <= 0x39) || (v >= 0xF0 && v <= 0xF9);
	}

	// each entry is {start, length}
	private static List<int[]> splitRecords(byte[] data) {
		List<int[]> recs = new ArrayList<>();
		int pos = 0;
		while (pos < data.length) {
			int len = pos + 2 <= data.length ? ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF) : 0;
			if (len < 2 || pos + len > data.length) {
				len = data.length - pos; // malformed so treat the remainder as one record
			}
			recs.add(new int[] { pos, len });
			pos += len;
		}
		return recs;
	}

}
