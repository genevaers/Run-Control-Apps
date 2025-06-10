package ComponentGenerator;

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


import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Colours and formatting for the Generator.
 */
public class LogFormatter extends Formatter{

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001b[36m";
	public static final String ANSI_YELLOW = "\u001b[33m";
	public static final String ANSI_GREEN = "\u001b[32m";
	public static final String ANSI_RED = "\u001b[31m";
	public static final String ANSI_BLACK_ON_WHITE = "\u001B[30;107m";
	public static final String ANSI_RED_ON_GRAY = "\u001B[91;47m";
	public static final String ANSI_RED_ON_WHITE = "\u001B[91;107m";

    @Override
    public String format(LogRecord rec) {
        return getLevel(rec) + getMessage(rec) + "\n";
    }

    private String getMessage(LogRecord rec) {
        String msgStr;
        String lName = rec.getLevel().getName();
        if(lName.equals("CONFIG")) {
            msgStr = ANSI_GREEN + "--- ";
            msgStr += rec.getMessage();
            msgStr += " ---" + ANSI_RESET;
            return msgStr;
        } else {
            return rec.getMessage();
        }
    }

    private String getLevel(LogRecord rec) {
        String levStr;
        String lName = rec.getLevel().getName();
        if(lName.equals("INFO")) {
            levStr = ANSI_BLUE;
        } else if (lName.equals("WARNING")) {
            levStr = ANSI_YELLOW;
        } else if (lName.equals("CONFIG")) {
            levStr = ANSI_BLUE;
            lName = "INFO";
        } else if (lName.equals("FINE")) {
            levStr = ANSI_BLUE;
            lName = "DEBUG";
        } else {
            levStr = ANSI_RED;
        }
        levStr += "[" + lName + "] " + ANSI_RESET; 
        return levStr;
    }
    
}
