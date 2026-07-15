package org.genevaers.genevaio.report;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2024
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.*;
import org.genevaers.repository.data.ComponentCollection;

import com.google.common.flogger.FluentLogger;

/**
 * Efficient VDP Comparison Summary Report Writer
 * Generates CSUMRPT format comparing two VDP files
 * 
 * Uses single-pass comparison algorithm with HashMap lookups for O(n) performance
 */
public class VDPComparisonSummaryWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
    private String vdp1Path;
    private String vdp2Path;
    private String vdp1Date;
    private String vdp2Date;
    
    // Component counts
    private Map<String, Integer> vdp1Counts = new LinkedHashMap<>();
    private Map<String, Integer> vdp2Counts = new LinkedHashMap<>();
    
    // Component differences
    private List<ComponentDifference> differences = new ArrayList<>();
    
    /**
     * Component difference record
     */
    private static class ComponentDifference implements Comparable<ComponentDifference> {
        String componentType;
        int id;
        String vdp1Status;  // "exists", "missing", "does not match"
        String vdp2Status;  // "exists", "missing", "does not match"
        
        ComponentDifference(String type, int id, String vdp1Status, String vdp2Status) {
            this.componentType = type;
            this.id = id;
            this.vdp1Status = vdp1Status;
            this.vdp2Status = vdp2Status;
        }
        
        @Override
        public int compareTo(ComponentDifference other) {
            int typeCompare = this.componentType.compareTo(other.componentType);
            if (typeCompare != 0) return typeCompare;
            return Integer.compare(this.id, other.id);
        }
    }
    
    public VDPComparisonSummaryWriter(String vdp1Path, String vdp2Path) {
        this.vdp1Path = vdp1Path;
        this.vdp2Path = vdp2Path;
        
        // Extract dates from paths or use current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDate = sdf.format(new Date());
        this.vdp1Date = currentDate;
        this.vdp2Date = currentDate;
    }
    
    /**
     * Compare two Repository instances and generate CSUMRPT
     * Note: This compares the current Repository state, so you need to:
     * 1. Load VDP1 into Repository
     * 2. Save Repository state
     * 3. Clear Repository
     * 4. Load VDP2 into Repository
     * 5. Call this method with both states
     */
    public void compareRepositories(Repository repo1, Repository repo2, String outputPath) throws IOException {
        logger.atInfo().log("Starting VDP comparison: %s vs %s", vdp1Path, vdp2Path);
        
        // Compare all component types
        compareUserExits(repo1.getUserExits(), repo2.getUserExits());
        compareControlRecords(repo1.getControlRecords(), repo2.getControlRecords());
        comparePhysicalFiles(repo1.getPhysicalFiles(), repo2.getPhysicalFiles());
        compareLogicalFiles(repo1.getLogicalFiles(), repo2.getLogicalFiles());
        compareLogicalRecords(repo1.getLogicalRecords(), repo2.getLogicalRecords());
        compareLookupPaths(repo1.getLookups(), repo2.getLookups());
        compareViews(repo1.getViews(), repo2.getViews());
        compareViewLogic(repo1.getViews(), repo2.getViews());
        
        // Sort differences
        Collections.sort(differences);
        
        // Write report
        writeReport(outputPath);
        
        logger.atInfo().log("Comparison complete. Found %d differences", differences.size());
    }
    
    /**
     * Compare User Exit Routines
     */
    private void compareUserExits(ComponentCollection<UserExit> coll1, ComponentCollection<UserExit> coll2) {
        Map<Integer, UserExit> map1 = buildMap(coll1);
        Map<Integer, UserExit> map2 = buildMap(coll2);
        
        vdp1Counts.put("User Exit Routines", map1.size());
        vdp2Counts.put("User Exit Routines", map2.size());
        
        compareByIdAndName("Exit", map1, map2, 
            (e) -> e.getComponentId(), 
            (e) -> e.getName());
    }
    
    /**
     * Compare Control Records
     */
    private void compareControlRecords(ComponentCollection<ControlRecord> coll1, ComponentCollection<ControlRecord> coll2) {
        vdp1Counts.put("Control Records", coll1.size());
        vdp2Counts.put("Control Records", coll2.size());
    }
    
    /**
     * Compare Physical Files
     */
    private void comparePhysicalFiles(ComponentCollection<PhysicalFile> coll1, ComponentCollection<PhysicalFile> coll2) {
        Map<Integer, PhysicalFile> map1 = buildMap(coll1);
        Map<Integer, PhysicalFile> map2 = buildMap(coll2);
        
        vdp1Counts.put("Physical Files", map1.size());
        vdp2Counts.put("Physical Files", map2.size());
        
        compareByIdAndName("Physical File", map1, map2,
            (pf) -> pf.getComponentId(),
            (pf) -> pf.getName());
    }
    
    /**
     * Compare Logical Files
     */
    private void compareLogicalFiles(ComponentCollection<LogicalFile> coll1, ComponentCollection<LogicalFile> coll2) {
        Map<Integer, LogicalFile> map1 = buildMap(coll1);
        Map<Integer, LogicalFile> map2 = buildMap(coll2);
        
        vdp1Counts.put("Logical Files", map1.size());
        vdp2Counts.put("Logical Files", map2.size());
        
        compareByIdAndName("Logical File", map1, map2,
            (lf) -> lf.getID(),
            (lf) -> lf.getName());
    }
    
    /**
     * Compare Logical Records
     */
    private void compareLogicalRecords(ComponentCollection<LogicalRecord> coll1, ComponentCollection<LogicalRecord> coll2) {
        Map<Integer, LogicalRecord> map1 = buildMap(coll1);
        Map<Integer, LogicalRecord> map2 = buildMap(coll2);
        
        vdp1Counts.put("Logical Records", map1.size());
        vdp2Counts.put("Logical Records", map2.size());
        
        compareByIdAndName("Logical Record", map1, map2,
            (lr) -> lr.getComponentId(),
            (lr) -> lr.getName());
    }
    
    /**
     * Compare Lookup Paths
     */
    private void compareLookupPaths(ComponentCollection<LookupPath> coll1, ComponentCollection<LookupPath> coll2) {
        Map<Integer, LookupPath> map1 = buildMap(coll1);
        Map<Integer, LookupPath> map2 = buildMap(coll2);
        
        vdp1Counts.put("Lookup Paths", map1.size());
        vdp2Counts.put("Lookup Paths", map2.size());
        
        compareByIdAndName("Lookup", map1, map2,
            (lp) -> lp.getID(),
            (lp) -> lp.getName());
    }
    
    /**
     * Compare Views
     */
    private void compareViews(ComponentCollection<ViewNode> coll1, ComponentCollection<ViewNode> coll2) {
        Map<Integer, ViewNode> map1 = buildMap(coll1);
        Map<Integer, ViewNode> map2 = buildMap(coll2);
        
        vdp1Counts.put("Views", map1.size());
        vdp2Counts.put("Views", map2.size());
        
        compareByIdAndName("View", map1, map2,
            (v) -> v.getID(),
            (v) -> v.getName());
    }
    
    /**
     * Compare View Logic (simplified - checks if views have different logic)
     */
    private void compareViewLogic(ComponentCollection<ViewNode> coll1, ComponentCollection<ViewNode> coll2) {
        Map<Integer, ViewNode> map1 = buildMap(coll1);
        Map<Integer, ViewNode> map2 = buildMap(coll2);
        
        // Check for views with logic differences
        Set<Integer> allIds = new HashSet<>();
        allIds.addAll(map1.keySet());
        allIds.addAll(map2.keySet());
        
        for (Integer id : allIds) {
            ViewNode v1 = map1.get(id);
            ViewNode v2 = map2.get(id);
            
            if (v1 == null) {
                differences.add(new ComponentDifference("View Logic", id, "missing", "exists"));
            } else if (v2 == null) {
                differences.add(new ComponentDifference("View Logic", id, "exists", "missing"));
            } else {
                // Check if logic differs (simplified check)
                if (hasLogicDifference(v1, v2)) {
                    differences.add(new ComponentDifference("View Logic", id, "does not match", "does not match"));
                }
            }
        }
    }
    
    /**
     * Build a map from ComponentCollection by ID
     */
    private <T> Map<Integer, T> buildMap(ComponentCollection<T> collection) {
        Map<Integer, T> map = new HashMap<>();
        Iterator<T> iter = collection.getIterator();
        while (iter.hasNext()) {
            T component = iter.next();
            // We need to get the ID - this is component-specific
            // For now, use the collection's get method which uses ID
            map.put(getComponentId(component), component);
        }
        return map;
    }
    
    /**
     * Get component ID using reflection or type checking
     */
    private int getComponentId(Object component) {
        if (component instanceof ViewNode) {
            return ((ViewNode) component).getID();
        } else if (component instanceof LookupPath) {
            return ((LookupPath) component).getID();
        } else if (component instanceof LogicalFile) {
            return ((LogicalFile) component).getID();
        } else if (component instanceof LogicalRecord) {
            return ((LogicalRecord) component).getComponentId();
        } else if (component instanceof PhysicalFile) {
            return ((PhysicalFile) component).getComponentId();
        } else if (component instanceof UserExit) {
            return ((UserExit) component).getComponentId();
        }
        return 0;
    }
    
    /**
     * Generic component comparison using functional interfaces
     */
    private <T> void compareByIdAndName(String typeName, Map<Integer, T> map1, Map<Integer, T> map2,
                                        java.util.function.Function<T, Integer> getId,
                                        java.util.function.Function<T, String> getName) {
        Set<Integer> allIds = new HashSet<>();
        allIds.addAll(map1.keySet());
        allIds.addAll(map2.keySet());
        
        for (Integer id : allIds) {
            T comp1 = map1.get(id);
            T comp2 = map2.get(id);
            
            if (comp1 == null) {
                differences.add(new ComponentDifference(typeName, id, "missing", "exists"));
            } else if (comp2 == null) {
                differences.add(new ComponentDifference(typeName, id, "exists", "missing"));
            } else {
                // Both exist - check if they match
                if (!getName.apply(comp1).equals(getName.apply(comp2))) {
                    differences.add(new ComponentDifference(typeName, id, "does not match", "does not match"));
                }
            }
        }
    }
    
    /**
     * Check if views have logic differences
     */
    private boolean hasLogicDifference(ViewNode v1, ViewNode v2) {
        // Simplified check - compare column count and sources
        if (v1.getNumberOfColumns() != v2.getNumberOfColumns()) return true;
        if (v1.getNumberOfViewSources() != v2.getNumberOfViewSources()) return true;
        
        return false;
    }
    
    /**
     * Write the comparison report in CSUMRPT format
     */
    private void writeReport(String outputPath) throws IOException {
        Path path = Paths.get(outputPath);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writeHeader(writer);
            writeSummaryCounts(writer);
            writeDifferences(writer);
            writeFooter(writer);
        }
        
        logger.atInfo().log("Comparison report written to: %s", outputPath);
    }
    
    /**
     * Write report header
     */
    private void writeHeader(Writer writer) throws IOException {
        writer.write("1VDP Record Summary Report\n");
        writer.write(" -------------------------\n");
        writer.write("  \n");
        writer.write(String.format(" VDP Run Date           %s    %s\n", vdp1Date, vdp2Date));
        writer.write("  \n");
    }
    
    /**
     * Write summary counts section
     */
    private void writeSummaryCounts(Writer writer) throws IOException {
        writer.write(" -------------------------------------------------------------------------------\n");
        writer.write(" Component Type        VDP1 Count  VDP2 Count\n");
        writer.write(" -------------------------------------------------------------------------------\n");
        
        for (String componentType : vdp1Counts.keySet()) {
            int count1 = vdp1Counts.get(componentType);
            int count2 = vdp2Counts.getOrDefault(componentType, 0);
            writer.write(String.format(" %-20s %7d     %7d\n", componentType, count1, count2));
        }
        
        writer.write("  \n");
    }
    
    /**
     * Write differences section
     */
    private void writeDifferences(Writer writer) throws IOException {
        if (differences.isEmpty()) {
            writer.write(" No differences found.\n");
            return;
        }
        
        writer.write(" -------------------------------------------------------------------------------\n");
        writer.write(" Component Type            ID  VDP1                VDP2\n");
        writer.write(" -------------------------------------------------------------------------------\n");
        
        for (ComponentDifference diff : differences) {
            writer.write(String.format(" %-20s %7d  %-19s %-19s\n",
                diff.componentType,
                diff.id,
                diff.vdp1Status,
                diff.vdp2Status));
        }
        
        writer.write("  \n");
    }
    
    /**
     * Write report footer
     */
    private void writeFooter(Writer writer) throws IOException {
        writer.write(String.format(" Total number of components with differences: %d\n", differences.size()));
    }
}

// Made with Bob
