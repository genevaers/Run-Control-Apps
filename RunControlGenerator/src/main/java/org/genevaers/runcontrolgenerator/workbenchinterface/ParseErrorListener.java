package org.genevaers.runcontrolgenerator.workbenchinterface;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.repository.Repository;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.repository.data.CompilerMessageSource;

public class ParseErrorListener extends BaseErrorListener {
	private List<String> errors = new ArrayList<String>();
	
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {
        List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
        Collections.reverse(stack);
        if(msg.startsWith("mismatched") || msg.startsWith("no viable")) {
            msg = "Invalid syntax. Symbol '" + ((Token)offendingSymbol).getText() + "'";
        }
        String err = msg + " at line " +line + " offset " + charPositionInLine;
        errors.add(err);
        CompilerMessage cerr = new CompilerMessage(
                    0, 
                    CompilerMessageSource.COLUMN, 
                    0, 
                    0, 
                    0,
                    err);

        Repository.addErrorMessage(cerr);
    }
    
    public List<String> getErrors() {
    	return errors;
    }
}
