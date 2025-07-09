package org.genevaers.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import com.google.common.flogger.FluentLogger;

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


public class GersCodePage {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private String codpage;
    
        public String getCodePage() {
            if (GersConfigration.isZos() && codpage == null) {
                try {
                    Class<?> rrc = Class.forName("org.genevaers.utilities.ZosCodePage");
                    Constructor<?>[] constructors = rrc.getConstructors();
                    codpage = ((GersCodePage) constructors[0].newInstance()).getCodePage();
                    System.out.println("loaded ZosCodePage. Codepage is " + codpage);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | ClassNotFoundException e) {
                    logger.atSevere().log("getCodePage failed %s", e.getMessage());
                    System.out.println("getCodePage failed\n" + e.getMessage());
                }
            }
            return codpage;
    }

    public static byte[] asciiToEbcdic(String str) {
        Charset utf8charset = Charset.forName("UTF-8");
        Charset ebccharset = Charset.forName(GersConfigration.getZosCodePage());
        ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());
        CharBuffer data = utf8charset.decode(inputBuffer);
        return ebccharset.encode(data).array();
    }

    public static byte[] ebcdicToAscii(byte[] buf) {
        Charset utf8charset = Charset.forName("ISO8859-1");
        Charset ebccharset = Charset.forName(GersConfigration.getZosCodePage());
        ByteBuffer inputBuffer = ByteBuffer.wrap(buf);
        CharBuffer data = ebccharset.decode(inputBuffer);
        return utf8charset.encode(data).array();
    }
}
