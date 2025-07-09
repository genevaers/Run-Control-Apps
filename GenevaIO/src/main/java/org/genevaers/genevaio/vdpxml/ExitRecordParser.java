package org.genevaers.genevaio.vdpxml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

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

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.enums.ExitType;
import org.genevaers.repository.components.enums.ProgramType;
import org.xml.sax.Attributes;

/**
 * This class will parse a Procedure Record element into a
 * UserExitRoutineTransfer object.
 */
public class ExitRecordParser extends BaseParser {

	private UserExit userExit;

	public ExitRecordParser() {
		sectionName = "Exits";
	}

	public UserExit getUserExit() {
		return userExit;
	}

	@Override
	public void addElement(String name, String text, Map<String, String> attributes) {
		switch (name.toUpperCase()) {
			case "EXIT":
				componentID = Integer.parseInt(attributes.get("ID"));
			break;
			case "PROGRAM":
				userExit = new UserExit();
				userExit.setComponentId(componentID);
				userExit.setName(text);
				Repository.getUserExits().add(userExit, componentID, text);
				break;
			case "MODULE":
				userExit.setExecutable(text.trim());
				Repository.getProcedures().add(userExit, componentID, text.trim());
				break;
			case "TYPE":
				userExit.setExitType(ExitType.fromdbcode(text.trim()));
				break;
			case "PROGRAMTYPE":
				userExit.setProgramType(ProgramType.fromdbcode(text.trim()));
				break;
			case "OPTIMIZE":
				userExit.setOptimizable(text.equals("1") ? true : false);
				break;
			default:
				break;
		}
	}

}
