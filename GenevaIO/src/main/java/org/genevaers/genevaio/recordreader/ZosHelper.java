package org.genevaers.genevaio.recordreader;

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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import com.google.common.flogger.FluentLogger;
import com.ibm.jzos.RcException;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileException;

public class ZosHelper {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static void deleteDataSet(String name) {
        try {
            ZFile.remove(name);
        } catch (ZFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void makePDS(String name) {

    }

    //The general copy should handle this. ?!
    public static void copyFileToPDS(File f, String pdsName) {

    }

    public static void copyFile2Dataset(File source, String dataset) {
        ZFile fileOut = null;
        BufferedReader in = null;

        // try {
        // open the source and iterate each line
        // write each line as a record to the target dataset
        try {
            fileOut = new ZFile(dataset, "wb,type=record,noseek");
            in = new BufferedReader(new FileReader(source));
            String line;
            int count = 0;
            while ((line = in.readLine()) != null) {
                fileOut.write(line.getBytes());
                count++;
            }
        } catch ( IOException e ) {
            logger.atSevere().log("Failed to copy %s to %s\n%s", source.toString(), dataset, e.getMessage());
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException e) {
                logger.atSevere().log("copyFile2Dataset Failed to close\n%s", e.getMessage());
            }
        }
    }

    public static void convertA2EAndCopyFile2Dataset(File source, String dataset, String recfm, String lrecl) {
        ZFile fileOut = null;
        BufferedReader in = null;

        // open the source and iterate each line
        // write each line as a record to the target dataset
        try {
            fileOut = new ZFile(dataset, "wb,type=record,recfm=" + recfm.toLowerCase() + ",lrecl=" + lrecl + ", space=(trk,(300,10,10),rlse) ,noseek");
            in = new BufferedReader(new FileReader(source));
            String line;
            int count = 0;
            while ((line = in.readLine()) != null) {
                if(recfm.equals("fb")) {
                    fileOut.write(asciiToEbcdic(StringUtils.rightPad(line, Integer.parseInt(lrecl))));
                } else {
                    fileOut.write(asciiToEbcdic(line)); 
                }
                count++;
            }
        } catch ( IOException e ) {
            logger.atSevere().log("convertA2EAndCopyFile2Dataset Failed to copy %s to %s\n%s", source.toString(), dataset, e.getMessage());
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException e) {
                logger.atSevere().log("convertA2EAndCopyFile2Dataset Failed to close\n%s", e.getMessage());
            }
        }
    }

    private static byte[] asciiToEbcdic(String str) {
        Charset utf8charset = Charset.forName("ISO8859-1");
        Charset ebccharset = Charset.forName("IBM-1047");
        ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());
        CharBuffer data = utf8charset.decode(inputBuffer);
        return ebccharset.encode(data).array();
      }
  
      private static byte[] ebcdicToAscii(byte[] buf) {
        Charset utf8charset = Charset.forName("ISO8859-1");
        Charset ebccharset = Charset.forName("IBM-1047");
        ByteBuffer inputBuffer = ByteBuffer.wrap(buf);
        CharBuffer data = ebccharset.decode(inputBuffer);
        return utf8charset.encode(data).array();
      }
  
      public static boolean convertE2AAndCopyDataset2File(String dataset, File target, String recfm, String lrecl) {
          boolean copied = false;
          ZFile fileIn = null;
          BinRecordWriter fw = new BinRecordWriter();
          try {
              fw.writeRecordsTo(target);
              fileIn = new ZFile(dataset,"rb,type=record,recfm=" + recfm.toLowerCase() + ",lrecl=" + lrecl + ",noseek");
              byte[] recBuf = new byte[Integer.parseInt(lrecl)];
              while (fileIn.read(recBuf) != -1) {
                  ByteBuffer convbb = ByteBuffer.wrap(ebcdicToAscii(recBuf));
                  convbb.position(recBuf.length);
                  fw.writeArray(convbb);
              }
              copied = true;
          } catch (IOException e) {
            logger.atSevere().log("convertE2AAndCopyDataset2File Failed to copy %s to %s\n%s", target.toString(), dataset, e.getMessage());
          } finally {
              try {
                  if (fileIn != null) {
                      fileIn.close();
                  }
                  fw.close();
              } catch (IOException e) {
                logger.atSevere().log("convertE2AAndCopyDataset2File Failed to close\n%s", e.getMessage());
              }
          }
          return copied;
      }

      public static void putEventFile(String destDataset, File input, String name, String lrecl) {
        String datasetName = destDataset + "." + name;
        if (datasetDoesNotExist(datasetName)) {
            String ddname = ZFile.allocDummyDDName();
            String cmd = "alloc fi(" + ddname + ") da(" + datasetName + ") reuse new catalog msg(2)"
                    + " recfm(f,b) space(1,1) cyl"
                    + " lrecl(" + lrecl + ")";
            ZFile.bpxwdyn(cmd); // might throw RcException
            com.ibm.jzos.RecordWriter writer = null;
            try (FileInputStream fis = new FileInputStream(input)) {
                try {
                    writer = com.ibm.jzos.RecordWriter.newWriterForDD(ddname);
                    byte[] recordBuf = new byte[writer.getLrecl()];
                    int len = Integer.valueOf(lrecl);
                    while (fis.read(recordBuf, 0, len) > 0) {
                        writer.write(recordBuf, 0, len);
                    }
                } catch (ZFileException e) {
                    logger.atSevere().log("putEventFile %s failed\n%s", datasetName, e.getMessage());
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (ZFileException zfe) {
                            zfe.printStackTrace(); // but continue
                        }
                    }
                    try {
                        ZFile.bpxwdyn("free fi(" + ddname + ") msg(2)");
                    } catch (RcException rce) {
                        rce.printStackTrace(); // but continue
                    }
                }
            } catch (IOException e) {
                logger.atSevere().log("putEventFile %s failed\n%s", datasetName, e.getMessage());
            }
        }
    }

    private static boolean datasetDoesNotExist(String datasetName) {
        boolean notExist = true;
        try {
            notExist = !ZFile.dsExists("//'" + datasetName + "'");
        } catch (ZFileException e) {
            logger.atSevere().log("datasetDoesNotExist %s failed\n%s", datasetName, e.getMessage());
        }
        return notExist;
    }

}
