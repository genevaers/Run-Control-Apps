package org.genevaers.testframework.functioncodecoverage;

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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CoverageFile {

	private JsonFactory jFactory;
	private ObjectMapper mapper;
	private File covFile;
	private ObjectNode rootNode;
	private ArrayNode funcCodesArray;
	private ObjectNode accumulationRootNode;
	private ArrayNode accumulationFuncCodesArray;
	private boolean accumulating;
	private File accumulationFile;

	CoverageFile(File cov) {
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
		covFile = cov;
	}

	// Want to be able to accumulate into a new coverage file

	public boolean read() {
		boolean readOkay = false;
		if (covFile.exists()) {
			try (JsonParser jParser = jFactory.createParser(covFile)) {
				jParser.setCodec(mapper);
				rootNode = (ObjectNode) jParser.readValueAsTree();
				if (rootNode != null) {
					funcCodesArray = (ArrayNode) rootNode.findValue("funcCodes");
					readOkay = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return readOkay;
	}

	List<FunctionCodeHit> getFunctionCodes() {
		List<FunctionCodeHit> codeHits = new ArrayList<>();
		if (funcCodesArray.isArray()) {
			// The accumulationFuncCodesArray should be a direct parallel.
			// But not for the first iteration of the accumulator
			for (int c = 0; c < funcCodesArray.size(); c++) {
				processFunctionCode(codeHits, c);
			}
		}
		return codeHits;
	}

	private void processFunctionCode(List<FunctionCodeHit> codeHits, int c) {
		JsonNode codeNode = funcCodesArray.get(c);
		FunctionCodeHit fc = new FunctionCodeHit();
		fc.setName(codeNode.get("name").asText());
		if (codeNode.get("description") != null) {
			fc.setDescription(codeNode.get("description").asText());
		}
		int hits = codeNode.get("hits").asInt();
		fc.setHits(hits);
		JsonNode accCodeNode = null;
		if (accumulating) {
			accCodeNode = accumulationFuncCodesArray.get(c);
			if (accCodeNode != null) {
				int accHits = accCodeNode.get("hits").asInt();
				accHits += hits;
				((ObjectNode) accCodeNode).put("hits", accHits);
			}
		}
		fc.setExpectedItems(codeNode.get("expected").asInt());
		int args = codeNode.get("args").asInt();
		fc.setArgs(args);
		codeHits.add(fc);
		if (args == 1) {
			processSingleArgFunctionCode(codeNode, accCodeNode, fc);
		} else if (args == 2) {
			processDoubleArgFunctionCode(codeNode, accCodeNode, fc);
		}
	}

	private void processDoubleArgFunctionCode(JsonNode codeNode, JsonNode accCodeNode, FunctionCodeHit fc) {
		// typesMatrix
		ArrayNode accTypesMatrixArray = null;
		ArrayNode typesMatrixArray = (ArrayNode) codeNode.findValue("typeMatrix");
		if (accumulating && accCodeNode != null) {
			accTypesMatrixArray = (ArrayNode) accCodeNode.findValue("typeMatrix");
		}
		if (typesMatrixArray.isArray()) {
			fc.addMatrixHeader(typesMatrixArray.get(0));
			// If we are accumulating don't need to worry about the accum header
			// it will already be there
			for (int t = 1; t < typesMatrixArray.size(); t++) {
				fc.addTypeHitRow(typesMatrixArray.get(t));
				if (accumulating) {
					acculumulateTypeData(accTypesMatrixArray, typesMatrixArray, t);
				}
			}
		}
	}

	private void acculumulateTypeData(ArrayNode accTypesMatrixArray, ArrayNode typesMatrixArray, int t) {
		JsonNode typesArrayNode = typesMatrixArray.get(t);
		if (accTypesMatrixArray != null) {
			JsonNode accumTypesArrayNode = accTypesMatrixArray.get(t);
			ArrayNode dataArray = (ArrayNode) typesArrayNode.findValue("data");
			ArrayNode accumDataArray = (ArrayNode) accumTypesArrayNode.findValue("data");
			for (int d = 0; d < dataArray.size(); d++) {
				int numHits = dataArray.get(d).asInt();
				int accNumHits = accumDataArray.get(d).asInt();
				accNumHits += numHits;
				JsonNode val = new IntNode(accNumHits);
				accumDataArray.set(d, val);
			}
		}
	}

	private void processSingleArgFunctionCode(JsonNode codeNode, JsonNode accCodeNode, FunctionCodeHit fc) {
		ArrayNode typesArray = (ArrayNode) codeNode.findValue("dataTypes");
		if (typesArray.isArray()) {
			for (int t = 0; t < typesArray.size(); t++) {
				fc.addTypeHit(typesArray.get(t));
				if (accumulating && accCodeNode != null) {
					ArrayNode accTypesArray = (ArrayNode) accCodeNode.findValue("dataTypes");
					JsonNode typesArrayNode = typesArray.get(t);
					if (accTypesArray != null) {
						JsonNode accTypesArrayNode = accTypesArray.get(t);
						int numHits = typesArrayNode.findValue("hits").asInt();
						int accNumHits = accTypesArrayNode.findValue("hits").asInt();
						accNumHits += numHits;
						((ObjectNode) accTypesArrayNode).put("hits", accNumHits);
					} else {
						System.out.println("Badness");
					}
				}
			}
		}
	}

	public void accumulateTo(File accFile) {
		accumulationFile = accFile;
		if (accumulationFile.exists()) {
			try (JsonParser jParser = jFactory.createParser(accumulationFile)) {
				jParser.setCodec(mapper);
				accumulationRootNode = (ObjectNode) jParser.readValueAsTree();
				if (rootNode != null) {
					accumulationFuncCodesArray = (ArrayNode) accumulationRootNode.findValue("funcCodes");
				}
				accumulating = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// we just need to save the current JSON tree
			// So it must be build first
			accumulating = false;
		}
		// We need to Walk the list of FunctionCodeHit and add the values to
		// accumulationFuncCodesArray?
		// Or if we set this up first the values can be addeds as we go?
	}

	public void close() {
		try (JsonGenerator generator = jFactory.createGenerator(new FileWriter(accumulationFile))){
			if (accumulationFile != null) {
				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
					generator.setCodec(mapper);
					generator.useDefaultPrettyPrinter();
					if(accumulating) {
						generator.writeTree(accumulationRootNode);
					} else {
						generator.writeTree(rootNode);
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
	}
}
