package org.genevaers.utilities;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.ibm.jzos.Transcoder;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class FileProcessor {
    static transient Logger logger = Logger.getLogger("org.genevaers.utilities.FileProcessor");

    public static void sed(File infile, File outfile, List<Substitution> substs) throws IOException {

        logger.fine("Processing replacements infile " + infile.getAbsolutePath() + ", outfile "
                + outfile.getAbsolutePath());
        // delimit the {, }, \, $,

        String line;
        StringBuffer buffer = new StringBuffer();
        FileReader fileInputStream = new FileReader(infile);
        BufferedReader reader = new BufferedReader(fileInputStream);
        int i = 1;
        while ((line = reader.readLine()) != null) {

            String newline = line;
            for (Substitution subst : substs) {
                if ((subst.getLineStart() == null || i >= subst.getLineStart())
                        && (subst.getLineStop() == null || i <= subst.getLineStop())
                        && newline.indexOf(subst.getReplace()) != -1) {
                    newline = newline.replace(subst.getReplace(), subst.getWith());
                    logger.fine("Found pattern " + subst.getReplace() + " in \"" + line + "\" at line " + i
                            + "\nNew line: \"" + newline + "\", replace was "
                            + subst.getWith());
                }
            }
            buffer.append(newline + "\n");
            i++;
        }
        reader.close();
        BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
        out.write(buffer.toString());
        out.close();
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dst);) {

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "copy File Not found %s", e.getMessage());
        }
    }

    public static void deleteRecursive(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteR(c);
        } else {
            if (!f.delete())
                throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    protected static void deleteR(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteR(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    public static boolean diff(File left, File right, File deltaF, Integer start, Integer end, boolean trim)
            throws IOException {
        boolean different = false;

        List<String> original = fileToLines(left, start, end, trim);
        List<String> revised = fileToLines(right, start, end, trim);

        FileWriter writer = new FileWriter(deltaF);
        if (original.size() != revised.size()) {
            writer.write("Base has " + original.size() + " lines. Test has " + revised.size());
            different = true;
        } else {
            Integer index = 0;
            for (String orig : original) {
                String test = revised.get(index++);
                int offset = StringUtils.indexOfDifference(orig, test);
                if (offset != -1) {
                    // we have a diff
                    different = true;
                    writer.write("Line " + index.toString() + " offset " + offset + "\n");
                    String highlight = String.format("%1$" + (offset + 7) + "s", "Here: |");
                    writer.write(highlight + "\n");
                    writer.write("Diff: " + orig + "\n");
                    writer.write("to  : " + test + "\n");
                }
            }
        }
        writer.close();
        return different;
    }

    public static boolean diff(File left, File right, File deltaF) throws IOException {
        boolean different = false;
        List<String> original = fileToLines(left);
        List<String> revised = fileToLines(right);

        // Compute diff. Get the Patch object. Patch is the container for
        // computed deltas.
        Patch patch = DiffUtils.diff(original, revised);

        FileWriter writer = new FileWriter(deltaF);
        Iterator<Delta> di = patch.getDeltas().iterator();
        while (di.hasNext()) {
            Delta delta = di.next();
            writer.write(delta.toString() + "\n");
            different = true;
        }
        writer.close();
        return different;
    }

    public static List<String> fileToLines(File filename) throws IOException {
        List<String> lines = new LinkedList<String>();
        String line = "";
        BufferedReader in = new BufferedReader(new FileReader(filename));
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }
        in.close();
        return lines;
    }

    public static List<String> fileToLines(File filename, Integer start, Integer end, boolean trim) throws IOException {
        List<String> lines = new LinkedList<String>();
        String line = "";
        int i = 1;
        BufferedReader in = new BufferedReader(new FileReader(filename));
        while ((line = in.readLine()) != null) {
            if ((start == null || i >= start.intValue()) && (end == null || i <= end.intValue())) {
                if (trim) {
                    line = line.trim();
                }
                lines.add(line);
            }
            i++;
        }
        in.close();
        return lines;
    }

    public static String readFile(File filename) throws IOException {
        StringBuffer lines = new StringBuffer();
        String line = "";
        BufferedReader in = new BufferedReader(new FileReader(filename));
        while ((line = in.readLine()) != null) {
            lines.append(line + "\n");
        }
        in.close();
        return lines.toString();
    }

    public static void toPrintableFile(File orig) throws IOException {
        File newf = new File(orig.getAbsolutePath() + ".tmp");
        orig.renameTo(newf);
        toPrintableFile(newf, orig);
        newf.delete();
    }

    public static void toPrintableFile(File infile, File outfile) throws IOException {
        String line = "";
        BufferedReader in = new BufferedReader(new FileReader(infile));
        BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
        while ((line = in.readLine()) != null) {
            line = line.replaceAll("[^\\p{Print}]", " ");
            line.trim();
            out.append(line + "\n");
        }
        in.close();
        out.close();
    }

    public static List<String> extract(File output, String startPattern,
            String endPattern) {

        String line;
        List<String> lines = new LinkedList<String>();
        ;
        try (FileReader fileInputStream = new FileReader(output);
                BufferedReader reader = new BufferedReader(fileInputStream);) {

            boolean extracting = false;
            boolean done = false;
            while ((line = reader.readLine()) != null && done == false) {

                if (extracting == false) {
                    if (line.contains(startPattern)) {
                        extracting = true;
                        lines.add(line);
                    }
                } else {
                    // for the moment ignore the endPattern and hardcode
                    if (line.contains(endPattern)) {
                        done = true;
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "FileProcessor extract error %s", e.getMessage());
        }
        return lines;
    }

    public static boolean diff(File base, File output, File diff,
            String startKey, String stopKey, boolean trim) throws IOException {

        // Extract from the file.
        List<String> result = extract(output, startKey, stopKey);

        List<String> original = fileToLines(base, null, null, trim);
        // List<String> revised = fileToLines(right, start, end, trim);

        FileWriter writer = new FileWriter(diff);
        boolean different = false;
        if (original.size() != result.size()) {
            writer.write("Base has " + original.size() + " lines. Test has " + result.size() + "\n");
            writer.write("Extracted \n");
            for (String ext : result) {
                writer.write(ext + "\n");
            }
            different = true;
        } else {
            Integer index = 0;
            for (String orig : original) {
                String test = result.get(index++);
                int offset = StringUtils.indexOfDifference(orig, test);
                if (offset != -1) {
                    // we have a diff
                    different = true;
                    writer.write("Line " + index.toString() + " offset " + offset + "\n");
                    String highlight = String.format("%1$" + (offset + 7) + "s", "Here: |");
                    writer.write(highlight + "\n");
                    writer.write("Diff: " + orig + "\n");
                    writer.write("to  : " + test + "\n");
                }
            }
        }
        writer.close();
        return different;
    }

}
