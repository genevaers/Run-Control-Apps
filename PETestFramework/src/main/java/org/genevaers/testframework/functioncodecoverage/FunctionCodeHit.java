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


import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

public class FunctionCodeHit {

	public enum HITS_STATE {
		NONE, SOME, ALL
	}

	private String name = "";
	private String description = "";
	private int hits = 0;
	private int args = 0;
	private int itemsHit = 0;
	private int expectedItems = 0;

	// should we have a different class for the case of Type Hits
	// and Type Matrix?

	private List<TypeHit> typeHits = new ArrayList<TypeHit>();
	private List<List<TypeHit>> typesMatrix = new ArrayList<List<TypeHit>>();
	HITS_STATE state = HITS_STATE.NONE;
	boolean zeroTypeHit = false;

	FunctionCodeHit()
	{
	}

	public int getArgs() {
		return args;
	}

	public void setArgs(int args) {
		this.args = args;
	}

	public int getHits() {
		return hits;
	}

	public int getItemsHit() {
		return itemsHit;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public void setExpectedItems(int e) {
		expectedItems = e;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addTypeHit(JsonNode typeNode) {
		TypeHit th = new TypeHit();
		th.setName(typeNode.findValue("typeName").asText());
		typeNode.findValue("hits").asInt();
		int numHits = typeNode.findValue("hits").asInt();
		itemsHit += (numHits > 0) ? 1 : 0;
		th.setHits(numHits);
		typeHits.add(th);
	}

	public List<TypeHit> getTypeHits() {
		return typeHits;
	}

	public List<TypeHit> getTypesMatrixHeader() {
		return typesMatrix.get(0);
	}

	public HITS_STATE getHitsState() {
		// if args is 1 the check all relevant types have passed
		// Can we do this... will typehits have been set
		if(args == 0){
			return hits > 0 ? HITS_STATE.ALL : HITS_STATE.NONE; 
		} else {
			if (itemsHit == 0)
			{
				return HITS_STATE.NONE;
			}
			else {
				return itemsHit >= expectedItems ? HITS_STATE.ALL : HITS_STATE.SOME;
			}
		}
	}

	public void addTypeHitRow(JsonNode typesRow) {
		List<TypeHit> dataTypeHits = new ArrayList<TypeHit>();
		String typeName = typesRow.findValue("typeName").asText();
		
		ArrayNode dataArray = (ArrayNode) typesRow.findValue("data");
		for(int d=0; d<dataArray.size(); d++)
		{
			TypeHit dth = new TypeHit();
			int numHits = dataArray.get(d).asInt();
			dth.setHits(numHits);
			itemsHit += (numHits > 0) ? 1 : 0;
			dth.setName(typeName);
			dataTypeHits.add(dth);
		}
		typesMatrix.add(dataTypeHits);
	}

	public void addMatrixHeader(JsonNode headerRow) {
		ArrayNode headerArray = (ArrayNode) headerRow.findValue("header");
		List<TypeHit> headerTypeHits = new ArrayList<>();
		TypeHit tth = new TypeHit();
		tth.setName("Types");
		headerTypeHits.add(tth);
		for(int h=0; h<headerArray.size(); h++)
		{
			TypeHit th = new TypeHit();
			th.setName(headerArray.get(h).asText());
			headerTypeHits.add(th);
		}
		typesMatrix.add(headerTypeHits);
	}

	public List<List<TypeHit>> getTypeMatrixHits() {
		return typesMatrix.subList(1, typesMatrix.size());
	}

	public String getHitsData() {
		
		String data;
		if(args > 0) {
			data = new Integer(hits).toString() + " hits on " + new Integer(itemsHit).toString() + " items. Expected " + new Integer(expectedItems).toString();
		}else {
			data = new Integer(hits).toString();
		}
		return data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
