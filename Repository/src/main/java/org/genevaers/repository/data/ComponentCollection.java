package org.genevaers.repository.data;

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


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ComponentCollection<T> {

	private Map<Integer, T> components = new TreeMap<>();
	private Map<String, T> componentsByName = new TreeMap<>();

    public void add(T c, int id, String name) {
		components.put(id, c);
		componentsByName.put(name, c);
	}

    public void add(T c, int id) {
		components.put(id, c);
	}

	public int size() {
		return components.size();
	}

	public Collection<T> getValues() {
		return components.values();
	}

	public T get(int id) {
		return components.get(id);
	}

	public T get(String name) {
		return componentsByName.get(name);
	}

    public Iterator<T> getIterator() {
		return components.values().iterator();
    }
}
