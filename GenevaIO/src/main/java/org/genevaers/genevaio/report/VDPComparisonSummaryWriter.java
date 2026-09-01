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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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
import org.genevaers.genevaio.vdpfile.VDPFileReader;
import java.lang.reflect.Method;

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
    private static final String USER_EXIT_ROUTINE_TYPE = "User Exit Routine";

    private Map<String, Integer> vdp1Counts = new LinkedHashMap<>();
    private Map<String, Integer> vdp2Counts = new LinkedHashMap<>();
    private Set<String> extractInputDDs1 = new TreeSet<>();
    private Set<String> extractInputDDs2 = new TreeSet<>();
    private Set<String> extractOutputDDs1 = new TreeSet<>();
    private Set<String> extractOutputDDs2 = new TreeSet<>();
    private Set<String> formatPhaseDDs1 = new TreeSet<>();
    private Set<String> formatPhaseDDs2 = new TreeSet<>();
    private Map<Integer, List<PhysicalFile>> lfPfMap1 = new HashMap<>();
    private Map<Integer, List<PhysicalFile>> lfPfMap2 = new HashMap<>();
    private Map<Integer, String> lfNameMap1 = new HashMap<>();
    private Map<Integer, String> lfNameMap2 = new HashMap<>();
    
    // Component differences
    private List<ComponentDifference> differences = new ArrayList<>();
    // View logic differences
    private Map<Integer, ViewLogicDiff> viewLogicDiffs = new TreeMap<>();

    private static class ViewLogicDiff {
        int viewId;
        String name1 = "";
        String name2 = "";
        List<ColumnDiff> columnDiffs = new ArrayList<>();
        List<ColumnLogicDiff> columnLogicDiffs = new ArrayList<>();
    }

    private static class ColumnLogicDiff {
        int columnNumber;
        int sourceNumber;
        String logic1 = "";
        String logic2 = "";
    }

    private List<ColumnLogicDiff> compareColumnLogic(ViewColumn column1, ViewColumn column2) {
        Map<Integer, String> logic1 = getColumnSourceLogic(column1);
        Map<Integer, String> logic2 = getColumnSourceLogic(column2);
        Set<Integer> sourceNumbers = new TreeSet<>();
        sourceNumbers.addAll(logic1.keySet());
        sourceNumbers.addAll(logic2.keySet());

        List<ColumnLogicDiff> differences = new ArrayList<>();
        for (Integer sourceNumber : sourceNumbers) {
            String text1 = logic1.getOrDefault(sourceNumber, "");
            String text2 = logic2.getOrDefault(sourceNumber, "");
            if (!Objects.equals(text1, text2) && !(isMissing(text1) && isMissing(text2))) {
                ColumnLogicDiff difference = new ColumnLogicDiff();
                difference.columnNumber = column1 != null ? column1.getColumnNumber() : column2.getColumnNumber();
                difference.sourceNumber = sourceNumber;
                difference.logic1 = text1;
                difference.logic2 = text2;
                differences.add(difference);
            }
        }
        return differences;
    }

    private Map<Integer, String> getColumnSourceLogic(ViewColumn column) {
        Map<Integer, String> logic = new TreeMap<>();
        if (column == null) {
            return logic;
        }
        Iterator<ViewColumnSource> sources = column.getIteratorForSourcesByNumber();
        int sourceNumber = 1;
        while (sources.hasNext()) {
            ViewColumnSource source = sources.next();
            logic.put(sourceNumber++, source.getLogicText() == null ? "" : source.getLogicText());
        }
        return logic;
    }

    private static class ColumnDiff {
        int columnNumber;
        int id;
        String name1 = "";
        String name2 = "";
        String vdp1Status;
        String vdp2Status;
        List<PropertyDiff> props = new ArrayList<>();
    }

    // Lookup path detailed diffs
    private Map<Integer, LookupDiff> lookupDiffs = new TreeMap<>();

    private static class LookupDiff {
        int id;
        String name1 = "";
        String name2 = "";
        Integer mappedTo1 = null;
        Integer mappedTo2 = null;
        List<LookupStepDiff> steps = new ArrayList<>();
    }

    // Logical record detailed diffs
    private Map<Integer, LogicalRecordDiff> logicalRecordDiffs = new TreeMap<>();

    private static class LogicalRecordDiff {
        int id;
        String name1 = "";
        String name2 = "";
        Integer lookupExit1 = null;
        Integer lookupExit2 = null;
        String lookupExitParams1 = "";
        String lookupExitParams2 = "";
        List<LogicalFieldDiff> fields = new ArrayList<>();
    }

    private static class LogicalFieldDiff {
        int id;
        String name = "";
        boolean missing1;
        boolean missing2;
        List<PropertyDiff> props = new ArrayList<>();
    }

    private static class PropertyDiff {
        String label;
        String vdp1Value;
        String vdp2Value;
    }

    private List<PropertyDiff> compareColumnProperties(ViewColumn column1, ViewColumn column2) {
        Map<String, PropertyDescriptor> properties = new TreeMap<>();
        try {
            Class<?> columnClass = column1 != null ? column1.getClass() : column2.getClass();
            for (PropertyDescriptor property : Introspector.getBeanInfo(columnClass, Object.class).getPropertyDescriptors()) {
                if (property.getReadMethod() != null && isColumnProperty(property.getName(), property.getPropertyType())) {
                    properties.put(property.getName(), property);
                }
            }
        } catch (Exception e) {
            logger.atWarning().withCause(e).log("Unable to inspect ViewColumn properties");
            return Collections.emptyList();
        }

        List<PropertyDiff> differences = new ArrayList<>();
        for (PropertyDescriptor property : properties.values()) {
            Object value1 = readColumnProperty(property, column1);
            Object value2 = readColumnProperty(property, column2);
            String displayValue1 = value1 == null ? "" : String.valueOf(value1);
            String displayValue2 = value2 == null ? "" : String.valueOf(value2);
            if (isMissing(displayValue1) && isMissing(displayValue2)) {
                continue;
            }
            if (Objects.equals(value1, value2)) {
                continue;
            }

            PropertyDiff difference = new PropertyDiff();
            difference.label = toPropertyLabel(property.getName());
            difference.vdp1Value = displayValue1;
            difference.vdp2Value = displayValue2;
            differences.add(difference);
        }
        return differences;
    }

    private boolean isColumnProperty(String name, Class<?> type) {
        return !name.equals("componentId")
                && !name.equals("viewId")
                && !name.equals("columnNumber")
                && (type.isPrimitive() || type.isEnum() || type == String.class
                        || Number.class.isAssignableFrom(type) || type == Boolean.class
                        || type == Character.class);
    }

    private Object readColumnProperty(PropertyDescriptor property, ViewColumn column) {
        if (column == null) {
            return null;
        }
        try {
            return property.getReadMethod().invoke(column);
        } catch (Exception e) {
            return null;
        }
    }

    private String toPropertyLabel(String propertyName) {
        StringBuilder label = new StringBuilder();
        for (char character : propertyName.toCharArray()) {
            if (Character.isUpperCase(character) && label.length() > 0) {
                label.append(' ');
            }
            label.append(label.length() == 0 ? Character.toUpperCase(character) : character);
        }
        return label.toString();
    }

    private static class LookupStepDiff {
        int stepNumber;
        List<KeyPropDiff> keyProps = new ArrayList<>();
    }

    private static class KeyPropDiff {
        int seqNum;
        String propLabel;
        String vdp1Value;
        String vdp2Value;
    }
    
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

    private void addDifference(String type, int id, String vdp1Status, String vdp2Status) {
        for (ComponentDifference difference : differences) {
            if (difference.componentType.equals(type) && difference.id == id) {
                return;
            }
        }
        differences.add(new ComponentDifference(type, id, vdp1Status, vdp2Status));
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
        collectExtractPhaseDDNames(repo1.getPhysicalFiles(), extractInputDDs1, extractOutputDDs1);
        collectExtractPhaseDDNames(repo2.getPhysicalFiles(), extractInputDDs2, extractOutputDDs2);
        collectFormatPhaseDDNames(repo1.getViews(), formatPhaseDDs1);
        collectFormatPhaseDDNames(repo2.getViews(), formatPhaseDDs2);
        
        // Compare all component types
        compareUserExits(repo1.getUserExits(), repo2.getUserExits());
        compareControlRecords(repo1.getControlRecords(), repo2.getControlRecords());
        comparePhysicalFiles(repo1.getPhysicalFiles(), repo2.getPhysicalFiles());
        compareLogicalFiles(repo1.getLogicalFiles(), repo2.getLogicalFiles());
        compareLogicalRecords(repo1.getLogicalRecords(), repo2.getLogicalRecords(), repo1.getFields(), repo2.getFields());
        compareLookupPaths(repo1.getLookups(), repo2.getLookups());
        compareViews(repo1.getViews(), repo2.getViews());
        compareViewProperties(repo1.getViews(), repo2.getViews());

        
        // Sort differences
        Collections.sort(differences);
        
        // Write report
        writeReport(outputPath);
        
        logger.atInfo().log("Comparison complete. Found %d differences", differences.size());
    }

    /**
     * Convenience method: read two VDP files into the Repository (one at a time),
     * capture their component collections and produce a CSUMRPT comparison.
     */
    public void writeFromVDPFiles(String vdp1PathStr, String vdp2PathStr, String outputPath) throws IOException {
        try {
            Path p1 = Paths.get(vdp1PathStr);
            Path p2 = Paths.get(vdp2PathStr);

            // Load first VDP into Repository and capture collections
            Repository.clearAndInitialise();
            VDPFileReader r = new VDPFileReader();
            r.open(p1, p1.getFileName().toString());
            r.addToRepsitory();

            ComponentCollection<UserExit> ue1 = copyCollection(Repository.getUserExits());
            ComponentCollection<ControlRecord> cr1 = copyCollection(Repository.getControlRecords());
            ComponentCollection<PhysicalFile> pf1 = copyCollection(Repository.getPhysicalFiles());
            ComponentCollection<LogicalFile> lf1 = copyCollection(Repository.getLogicalFiles());
            ComponentCollection<LogicalRecord> lr1 = copyCollection(Repository.getLogicalRecords());
            ComponentCollection<LRField> fields1 = copyCollection(Repository.getFields());
            ComponentCollection<LookupPath> lp1 = copyCollection(Repository.getLookups());
            ComponentCollection<ViewNode> v1 = copyCollection(Repository.getViews());

            // Load second VDP into fresh Repository and capture collections
            Repository.clearAndInitialise();
            r.open(p2, p2.getFileName().toString());
            r.addToRepsitory();

            ComponentCollection<UserExit> ue2 = copyCollection(Repository.getUserExits());
            ComponentCollection<ControlRecord> cr2 = copyCollection(Repository.getControlRecords());
            ComponentCollection<PhysicalFile> pf2 = copyCollection(Repository.getPhysicalFiles());
            ComponentCollection<LogicalFile> lf2 = copyCollection(Repository.getLogicalFiles());
            ComponentCollection<LogicalRecord> lr2 = copyCollection(Repository.getLogicalRecords());
            ComponentCollection<LRField> fields2 = copyCollection(Repository.getFields());
            ComponentCollection<LookupPath> lp2 = copyCollection(Repository.getLookups());
            ComponentCollection<ViewNode> v2 = copyCollection(Repository.getViews());

            collectExtractPhaseDDNames(pf1, extractInputDDs1, extractOutputDDs1);
            collectExtractPhaseDDNames(pf2, extractInputDDs2, extractOutputDDs2);
            collectFormatPhaseDDNames(v1, formatPhaseDDs1);
            collectFormatPhaseDDNames(v2, formatPhaseDDs2);

            // Run comparisons using the copied collections
            compareUserExits(ue1, ue2);
            compareControlRecords(cr1, cr2);
            comparePhysicalFiles(pf1, pf2);
            compareLogicalFiles(lf1, lf2);
            compareLogicalRecords(lr1, lr2, fields1, fields2);
            compareLookupPaths(lp1, lp2);
            compareViews(v1, v2);
            compareViewProperties(v1, v2);

            Collections.sort(differences);
            writeReport(outputPath);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Make a shallow copy of a ComponentCollection so we can hold a snapshot
     * while the Repository static state is re-used for the second load.
     */
    private <T> ComponentCollection<T> copyCollection(ComponentCollection<T> src) {
        ComponentCollection<T> dst = new ComponentCollection<>();
        Iterator<T> it = src.getIterator();
        while (it.hasNext()) {
            T c = it.next();
            int id = getComponentId(c);
            String name = getComponentName(c);
            if (name != null && name.length() > 0) {
                dst.add(c, id, name);
            } else {
                dst.add(c, id);
            }
        }
        return dst;
    }

    private void collectExtractPhaseDDNames(ComponentCollection<PhysicalFile> coll, Set<String> inputCollector, Set<String> outputCollector) {
        Iterator<PhysicalFile> iter = coll.getIterator();
        while (iter.hasNext()) {
            PhysicalFile pf = iter.next();
            if (pf.getInputDDName() != null && !pf.getInputDDName().isEmpty()) {
                inputCollector.add(pf.getInputDDName());
            }
            if (pf.getExtractDDName() != null && !pf.getExtractDDName().isEmpty()) {
                inputCollector.add(pf.getExtractDDName());
            }
            if (pf.getOutputDDName() != null && !pf.getOutputDDName().isEmpty()) {
                outputCollector.add(pf.getOutputDDName());
            }
        }
    }

    private void collectFormatPhaseDDNames(ComponentCollection<ViewNode> coll, Set<String> collector) {
        Iterator<ViewNode> iter = coll.getIterator();
        while (iter.hasNext()) {
            ViewNode view = iter.next();
            if (view.getOutputFile() != null) {
                String ddName = view.getOutputFile().getOutputDDName();
                if (ddName != null && !ddName.isEmpty()) {
                    collector.add(ddName);
                }
            }
        }
    }

    private String getComponentName(Object component) {
        try {
            Method m = component.getClass().getMethod("getName");
            Object res = m.invoke(component);
            return res == null ? "" : res.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Compare User Exit Routines
     */
    private void compareUserExits(ComponentCollection<UserExit> coll1, ComponentCollection<UserExit> coll2) {
        Map<Integer, UserExit> map1 = buildMap(coll1);
        Map<Integer, UserExit> map2 = buildMap(coll2);
        
        vdp1Counts.put("User Exit Routines", map1.size());
        vdp2Counts.put("User Exit Routines", map2.size());
        
        compareByIdAndName(USER_EXIT_ROUTINE_TYPE, map1, map2, 
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

        // Capture associated Physical Files for reporting
        lfPfMap1.clear();
        lfPfMap2.clear();
        Iterator<LogicalFile> it1 = coll1.getIterator();
        while (it1.hasNext()) {
            LogicalFile lf = it1.next();
            List<PhysicalFile> pfs = new ArrayList<>();
            Iterator<PhysicalFile> pfi = lf.getPFIterator();
            while (pfi.hasNext()) {
                pfs.add(pfi.next());
            }
            lfPfMap1.put(lf.getID(), pfs);
            lfNameMap1.put(lf.getID(), lf.getName());
        }
        Iterator<LogicalFile> it2 = coll2.getIterator();
        while (it2.hasNext()) {
            LogicalFile lf = it2.next();
            List<PhysicalFile> pfs = new ArrayList<>();
            Iterator<PhysicalFile> pfi = lf.getPFIterator();
            while (pfi.hasNext()) {
                pfs.add(pfi.next());
            }
            lfPfMap2.put(lf.getID(), pfs);
            lfNameMap2.put(lf.getID(), lf.getName());
        }
    }

    private void writeLogicalFileComparison(Writer writer) throws IOException {
        Set<Integer> allLfIds = new TreeSet<>();
        allLfIds.addAll(lfPfMap1.keySet());
        allLfIds.addAll(lfPfMap2.keySet());

        boolean hasAnyDifference = false;
        for (Integer lfId : allLfIds) {
            Map<Integer, String> pfMap1 = new TreeMap<>();
            Map<Integer, String> pfMap2 = new TreeMap<>();
            List<PhysicalFile> list1 = lfPfMap1.getOrDefault(lfId, Collections.emptyList());
            for (PhysicalFile pf : list1) {
                pfMap1.put(pf.getComponentId(), pf.getName());
            }
            List<PhysicalFile> list2 = lfPfMap2.getOrDefault(lfId, Collections.emptyList());
            for (PhysicalFile pf : list2) {
                pfMap2.put(pf.getComponentId(), pf.getName());
            }
            Set<Integer> allPfIds = new TreeSet<>();
            allPfIds.addAll(pfMap1.keySet());
            allPfIds.addAll(pfMap2.keySet());
            for (Integer pfId : allPfIds) {
                String name1 = pfMap1.get(pfId) != null ? pfMap1.get(pfId) : "missing";
                String name2 = pfMap2.get(pfId) != null ? pfMap2.get(pfId) : "missing";
                if (!Objects.equals(name1, name2)) {
                    hasAnyDifference = true;
                    break;
                }
            }
            if (hasAnyDifference) {
                break;
            }
        }
        if (!hasAnyDifference) {
            return;
        }

        writer.write("Logical File Comparison\n");
        writer.write(" -----------------------\n");
        writer.write("  \n");
        writer.write(" Compared :  All\n");
        for (Integer lfId : allLfIds) {
            Map<Integer, String> pfMap1 = new TreeMap<>();
            Map<Integer, String> pfMap2 = new TreeMap<>();
            List<PhysicalFile> list1 = lfPfMap1.getOrDefault(lfId, Collections.emptyList());
            for (PhysicalFile pf : list1) {
                pfMap1.put(pf.getComponentId(), pf.getName());
            }
            List<PhysicalFile> list2 = lfPfMap2.getOrDefault(lfId, Collections.emptyList());
            for (PhysicalFile pf : list2) {
                pfMap2.put(pf.getComponentId(), pf.getName());
            }

            Set<Integer> allPfIds = new TreeSet<>();
            allPfIds.addAll(pfMap1.keySet());
            allPfIds.addAll(pfMap2.keySet());

            boolean thisLfHasDifference = false;
            for (Integer pfId : allPfIds) {
                String name1 = pfMap1.get(pfId) != null ? pfMap1.get(pfId) : "missing";
                String name2 = pfMap2.get(pfId) != null ? pfMap2.get(pfId) : "missing";
                if (!Objects.equals(name1, name2)) {
                    thisLfHasDifference = true;
                    break;
                }
            }
            if (!thisLfHasDifference) {
                continue;
            }

            String name = lfNameMap1.getOrDefault(lfId, lfNameMap2.getOrDefault(lfId, ""));
            writer.write(" *---------------------------------------------------------------------\n");
            writer.write(String.format("   ID %26d\n", lfId));
            writer.write(String.format("   Name %24s\n", name));
            writer.write("   Associated Physical Files\n");
            for (Integer pfId : allPfIds) {
                String name1 = pfMap1.get(pfId) != null ? pfMap1.get(pfId) : "missing";
                String name2 = pfMap2.get(pfId) != null ? pfMap2.get(pfId) : "missing";
                if (Objects.equals(name1, name2)) {
                    continue;
                }
                writer.write(String.format("   ID        %d\n", pfId));
                writer.write(String.format(" -  VDP1     %s\n", name1));
                writer.write(String.format(" -  VDP2     %s\n", name2));
            }
        }
        writer.write("\n");
    }

    private void writeViewLogicComparison(Writer writer) throws IOException {
        if (viewLogicDiffs.isEmpty()) return;
        writer.write("View Properties Comparison\n");
        writer.write(" --------------------------\n");
        writer.write("  \n");
        writer.write(" Compared :  All\n");
        for (ViewLogicDiff vld : viewLogicDiffs.values()) {
            if (Objects.equals(vld.name1, vld.name2) && vld.columnDiffs.isEmpty() && vld.columnLogicDiffs.isEmpty()) {
                continue;
            }
            writer.write(" *---------------------------------------------------------------------\n");
            writer.write(String.format("   View ID %26d\n", vld.viewId));
            writer.write(String.format("   View Name %19s\n", (vld.name1 != null && !vld.name1.isEmpty()) ? vld.name1 : vld.name2));
            if (!Objects.equals(vld.name1, vld.name2)) {
                writer.write(String.format(" -  VDP1 %25s\n", (vld.name1 != null && !vld.name1.isEmpty()) ? vld.name1 : "missing"));
                writer.write(String.format(" -  VDP2 %25s\n", (vld.name2 != null && !vld.name2.isEmpty()) ? vld.name2 : "missing"));
            }
            if (!vld.columnDiffs.isEmpty()) {
                writer.write("   Column Properties\n");
                for (ColumnDiff cd : vld.columnDiffs) {
                    writer.write("     Column Data\n");
                    writer.write(String.format("       Column Number %d\n", cd.columnNumber));
                    writer.write(String.format("       Column ID %20d\n", cd.id));
                    String displayName = (cd.name1 != null && !cd.name1.isEmpty()) ? cd.name1 : cd.name2;
                    writer.write(String.format("       Name %24s\n", displayName != null && !displayName.isEmpty() ? displayName : "missing"));
                    for (PropertyDiff pd : cd.props) {
                        if (isMissing(pd.vdp1Value) && isMissing(pd.vdp2Value)) {
                            continue;
                        }
                        String v1 = (pd.vdp1Value != null && !pd.vdp1Value.isEmpty()) ? pd.vdp1Value : "missing";
                        String v2 = (pd.vdp2Value != null && !pd.vdp2Value.isEmpty()) ? pd.vdp2Value : "missing";
                        writer.write(String.format("       %-24s\n", pd.label));
                        writer.write(String.format(" -      VDP1 %20s\n", v1));
                        writer.write(String.format(" -      VDP2 %20s\n", v2));
                    }
                }
            }
            if (!vld.columnLogicDiffs.isEmpty()) {
                writer.write("   Column Logic\n");
                for (ColumnLogicDiff cld : vld.columnLogicDiffs) {
                    writer.write(String.format("     Column Number %d\n", cld.columnNumber));
                    writer.write(String.format("       Column Source Properties %d\n", cld.sourceNumber));
                    writer.write("         Column Logic Text\n");
                    writer.write(" -        VDP1                 \n");
                    writeLogicText(writer, cld.logic1);
                    writer.write(" -        VDP2                 \n");
                    writeLogicText(writer, cld.logic2);
                }
            }
        }
        writer.write(" *---------------------------------------------------------------------\n");
        writer.write("\n");
    }

    private boolean isMissing(String value) {
        return value == null || value.isEmpty() || "missing".equalsIgnoreCase(value);
    }

    private void writeLogicText(Writer writer, String logicText) throws IOException {
        if (logicText == null || logicText.isEmpty()) {
            writer.write("           missing\n");
            return;
        }
        writer.write(logicText);
        if (!logicText.endsWith("\n")) {
            writer.write("\n");
        }
    }

    private void writeLookupPathComparison(Writer writer) throws IOException {
        if (lookupDiffs.isEmpty()) return;
        writer.write("Lookup Path Comparison\n");
        writer.write(" ----------------------\n");
        writer.write("  \n");
        writer.write(" Compared :  All\n");
        for (LookupDiff ld : lookupDiffs.values()) {
            if (Objects.equals(ld.name1, ld.name2) && Objects.equals(ld.mappedTo1, ld.mappedTo2) && ld.steps.isEmpty()) {
                continue;
            }
            writer.write(" *---------------------------------------------------------------------\n");
            writer.write(String.format("   ID %26d\n", ld.id));
            writer.write(String.format("   Name %24s\n", (ld.name1 != null && !ld.name1.isEmpty()) ? ld.name1 : ld.name2));
            writer.write(String.format("   Mapped To %20s\n", (ld.mappedTo1 != null && ld.mappedTo1 > 0) ? String.valueOf(ld.mappedTo1) : ""));
            if (!Objects.equals(ld.name1, ld.name2) || !Objects.equals(ld.mappedTo1, ld.mappedTo2)) {
                writer.write(String.format(" -  VDP1 %25s\n", (ld.mappedTo1 != null && ld.mappedTo1 > 0) ? String.valueOf(ld.mappedTo1) : "missing"));
                writer.write(String.format(" -  VDP2 %25s\n", (ld.mappedTo2 != null && ld.mappedTo2 > 0) ? String.valueOf(ld.mappedTo2) : "missing"));
            }
            writer.write("   Lookup Steps\n");
            for (LookupStepDiff lsd : ld.steps) {
                writer.write(" *---------------------------------------------------------------------\n");
                writer.write(String.format("   ID %26d\n", ld.id));
                writer.write(String.format("   Name %24s\n", (ld.name1 != null && !ld.name1.isEmpty()) ? ld.name1 : ld.name2));
                writer.write(String.format("   Mapped To %20s\n", (ld.mappedTo1 != null && ld.mappedTo1 > 0) ? String.valueOf(ld.mappedTo1) : ""));
                writer.write(String.format(" -  VDP1 %25s\n", (ld.mappedTo1 != null && ld.mappedTo1 > 0) ? String.valueOf(ld.mappedTo1) : "missing"));
                writer.write(String.format(" -  VDP2 %25s\n", (ld.mappedTo2 != null && ld.mappedTo2 > 0) ? String.valueOf(ld.mappedTo2) : "missing"));
                writer.write(String.format("   Step Number %d\n", lsd.stepNumber));
                writer.write("     Source Field Properties\n");
                for (KeyPropDiff kpd : lsd.keyProps) {
                    writer.write(String.format("       Source Field Seq Num %d\n", kpd.seqNum));
                    writer.write(String.format("         %-25s\n", kpd.propLabel));
                    writer.write(String.format(" -        VDP1                 %s\n", (kpd.vdp1Value != null && !kpd.vdp1Value.isEmpty()) ? kpd.vdp1Value : "missing"));
                    writer.write(String.format(" -        VDP2                 %s\n", (kpd.vdp2Value != null && !kpd.vdp2Value.isEmpty()) ? kpd.vdp2Value : "missing"));
                }
            }
        }
        writer.write("\n");
    }

    private String describeComparisonStatus(boolean present1, boolean present2, boolean mismatch) {
        if (!present1 && !present2) {
            return "missing";
        }
        if (present1 && present2) {
            return mismatch ? "does not match" : "exists";
        }
        return present1 ? "exists" : "missing";
    }

    private String describeValueComparisonStatus(String value1, String value2) {
        boolean present1 = value1 != null && !value1.isEmpty();
        boolean present2 = value2 != null && !value2.isEmpty();
        return describeComparisonStatus(present1, present2, !Objects.equals(value1, value2));
    }

    private void writeLogicalRecordComparison(Writer writer) throws IOException {
        if (logicalRecordDiffs.isEmpty()) return;
        writer.write("Logical Record Comparison\n");
        writer.write(" -------------------------\n");
        writer.write("  \n");
        writer.write(" Compared :  All\n");
        for (LogicalRecordDiff lrd : logicalRecordDiffs.values()) {
            boolean anyDifference = !Objects.equals(lrd.name1, lrd.name2)
                    || !Objects.equals(lrd.lookupExit1, lrd.lookupExit2)
                    || !Objects.equals(lrd.lookupExitParams1, lrd.lookupExitParams2)
                    || !lrd.fields.isEmpty();
            if (!anyDifference) {
                continue;
            }
            writer.write(" *---------------------------------------------------------------------\n");
            writer.write(String.format("   ID %26d\n", lrd.id));
            writer.write(String.format("   Name %24s\n", (lrd.name1 != null && !lrd.name1.isEmpty()) ? lrd.name1 : lrd.name2));
            writer.write("   \n");
            boolean hasLookupExit1 = lrd.lookupExit1 != null && lrd.lookupExit1 > 0;
            boolean hasLookupExit2 = lrd.lookupExit2 != null && lrd.lookupExit2 > 0;
            if (hasLookupExit1 || hasLookupExit2) {
                writer.write("   Lookup Exit                  \n");
                writer.write(String.format(" -  VDP1 %25s\n", describeComparisonStatus(hasLookupExit1, hasLookupExit2, !Objects.equals(lrd.lookupExit1, lrd.lookupExit2))));
                writer.write(String.format(" -  VDP2 %25s\n", describeComparisonStatus(hasLookupExit2, hasLookupExit1, !Objects.equals(lrd.lookupExit1, lrd.lookupExit2))));
            }
            boolean hasLookupExitParams1 = !lrd.lookupExitParams1.isEmpty();
            boolean hasLookupExitParams2 = !lrd.lookupExitParams2.isEmpty();
            if (hasLookupExitParams1 || hasLookupExitParams2) {
                writer.write("   Lookup Exit Parms           \n");
                writer.write(String.format(" -  VDP1 %25s\n", describeComparisonStatus(hasLookupExitParams1, hasLookupExitParams2, !Objects.equals(lrd.lookupExitParams1, lrd.lookupExitParams2))));
                writer.write(String.format(" -  VDP2 %25s\n", describeComparisonStatus(hasLookupExitParams2, hasLookupExitParams1, !Objects.equals(lrd.lookupExitParams1, lrd.lookupExitParams2))));
            }
            if (!lrd.fields.isEmpty()) {
                writer.write("   LR Fields\n");
                writer.write("     \n");
                for (LogicalFieldDiff fd : lrd.fields) {
                    if (fd.id <= 0) {
                        continue;
                    }
                    writer.write(String.format("     ID %24d\n", fd.id));
                    if (fd.missing1 || fd.missing2) {
                        writer.write(String.format(" -    VDP1 %22s\n", fd.missing1 ? "missing" : "exists"));
                        writer.write(String.format(" -    VDP2 %22s\n", fd.missing2 ? "missing" : "exists"));
                    } else {
                        writer.write(String.format("     Name %22s\n", fd.name));
                        for (PropertyDiff pd : fd.props) {
                            writer.write(String.format("     %-24s%20s\n", pd.label, pd.vdp1Value));
                            writer.write(String.format(" -    VDP2 %22s\n", pd.vdp2Value));
                        }
                    }
                    writer.write("     \n");
                }
            }
        }
        writer.write("\n");
    }
    
    /**
     * Compare Logical Records
     */
    private void compareLogicalRecords(ComponentCollection<LogicalRecord> coll1, ComponentCollection<LogicalRecord> coll2,
                                       ComponentCollection<LRField> fieldColl1, ComponentCollection<LRField> fieldColl2) {
        Map<Integer, LogicalRecord> map1 = buildMap(coll1);
        Map<Integer, LogicalRecord> map2 = buildMap(coll2);
        
        vdp1Counts.put("Logical Records", map1.size());
        vdp2Counts.put("Logical Records", map2.size());
        
        compareByIdAndName("Logical Record", map1, map2,
            (lr) -> lr.getComponentId(),
            (lr) -> lr.getName());

        logicalRecordDiffs.clear();
        Set<Integer> allIds = new TreeSet<>();
        allIds.addAll(map1.keySet());
        allIds.addAll(map2.keySet());

        for (Integer id : allIds) {
            LogicalRecord lr1 = map1.get(id);
            LogicalRecord lr2 = map2.get(id);
            LogicalRecordDiff lrd = new LogicalRecordDiff();
            lrd.id = id;
            if (lr1 != null) {
                lrd.name1 = lr1.getName();
                lrd.lookupExit1 = lr1.getLookupExitID();
                lrd.lookupExitParams1 = lr1.getLookupExitParams();
            }
            if (lr2 != null) {
                lrd.name2 = lr2.getName();
                lrd.lookupExit2 = lr2.getLookupExitID();
                lrd.lookupExitParams2 = lr2.getLookupExitParams();
            }

            if (lr1 == null || lr2 == null) {
                differences.add(new ComponentDifference("Logical Record", id, lr1 == null ? "missing" : "exists", lr2 == null ? "missing" : "exists"));
                LogicalFieldDiff fd = new LogicalFieldDiff();
                fd.id = -1;
                fd.missing1 = lr1 == null;
                fd.missing2 = lr2 == null;
                fd.name = lr1 != null ? lr1.getName() : (lr2 != null ? lr2.getName() : "");
                lrd.fields.add(fd);
                logicalRecordDiffs.put(id, lrd);
                continue;
            }

            boolean hasDifference = false;
            if (lr1.getLookupExitID() != lr2.getLookupExitID()) {
                hasDifference = true;
            }
            if (!Objects.equals(lr1.getLookupExitParams(), lr2.getLookupExitParams())) {
                hasDifference = true;
            }

            Map<Integer, LRField> fields1 = new TreeMap<>();
            Map<Integer, LRField> fields2 = new TreeMap<>();
            if (lr1 != null) {
                Iterator<LRField> f1 = lr1.getIteratorForFieldsByID();
                while (f1.hasNext()) {
                    LRField field = f1.next();
                    if (field != null) {
                        fields1.put(field.getComponentId(), field);
                    }
                }
            }
            if (fields1.isEmpty() && fieldColl1 != null) {
                Iterator<LRField> f1 = fieldColl1.getIterator();
                while (f1.hasNext()) {
                    LRField field = f1.next();
                    if (field != null && field.getLrID() == id) {
                        fields1.put(field.getComponentId(), field);
                    }
                }
            }
            if (lr2 != null) {
                Iterator<LRField> f2 = lr2.getIteratorForFieldsByID();
                while (f2.hasNext()) {
                    LRField field = f2.next();
                    if (field != null) {
                        fields2.put(field.getComponentId(), field);
                    }
                }
            }
            if (fields2.isEmpty() && fieldColl2 != null) {
                Iterator<LRField> f2 = fieldColl2.getIterator();
                while (f2.hasNext()) {
                    LRField field = f2.next();
                    if (field != null && field.getLrID() == id) {
                        fields2.put(field.getComponentId(), field);
                    }
                }
            }

            Set<Integer> fieldIds = new TreeSet<>();
            fieldIds.addAll(fields1.keySet());
            fieldIds.addAll(fields2.keySet());

            for (Integer fid : fieldIds) {
                LRField field1 = fields1.get(fid);
                LRField field2 = fields2.get(fid);
                LogicalFieldDiff fd = new LogicalFieldDiff();
                fd.id = fid;
                fd.name = field1 != null ? field1.getName() : (field2 != null ? field2.getName() : "");
                fd.missing1 = field1 == null;
                fd.missing2 = field2 == null;

                if (field1 == null || field2 == null) {
                    lrd.fields.add(fd);
                    hasDifference = true;
                    continue;
                }

                boolean nameChanged = !Objects.equals(field1.getName(), field2.getName());
                if (field1.getStartPosition() != field2.getStartPosition()) {
                    PropertyDiff pd = new PropertyDiff();
                    pd.label = "Fixed Position";
                    pd.vdp1Value = String.valueOf(field1.getStartPosition());
                    pd.vdp2Value = String.valueOf(field2.getStartPosition());
                    fd.props.add(pd);
                }
                if (field1.getOrdinalPosition() != field2.getOrdinalPosition()) {
                    PropertyDiff pd = new PropertyDiff();
                    pd.label = "Ordinal Position";
                    pd.vdp1Value = field1.getOrdinalPosition() > 0 ? String.valueOf(field1.getOrdinalPosition()) : "";
                    pd.vdp2Value = field2.getOrdinalPosition() > 0 ? String.valueOf(field2.getOrdinalPosition()) : "";
                    fd.props.add(pd);
                }
                if (!Objects.equals(field1.getDbColName(), field2.getDbColName())) {
                    PropertyDiff pd = new PropertyDiff();
                    pd.label = "DBMS ColName";
                    pd.vdp1Value = field1.getDbColName();
                    pd.vdp2Value = field2.getDbColName();
                    fd.props.add(pd);
                }

                boolean fieldHasDifference = nameChanged || !fd.props.isEmpty();
                if (fieldHasDifference) {
                    lrd.fields.add(fd);
                    hasDifference = true;
                }
            }

            if (hasDifference || !lrd.fields.isEmpty()) {
                logicalRecordDiffs.put(id, lrd);
            }
        }
    }
    
    /**
     * Compare Lookup Paths
     */
    private void compareLookupPaths(ComponentCollection<LookupPath> coll1, ComponentCollection<LookupPath> coll2) {
        Map<Integer, LookupPath> map1 = buildMap(coll1);
        Map<Integer, LookupPath> map2 = buildMap(coll2);
        
        vdp1Counts.put("Lookup Paths", map1.size());
        vdp2Counts.put("Lookup Paths", map2.size());
        
        // Basic id/name differences
        compareByIdAndName("Lookup", map1, map2,
            (lp) -> lp.getID(),
            (lp) -> lp.getName());

        // Build detailed diffs for lookup paths where structure differs or one side missing
        lookupDiffs.clear();
        Set<Integer> allIds = new TreeSet<>();
        allIds.addAll(map1.keySet());
        allIds.addAll(map2.keySet());

        for (Integer id : allIds) {
            LookupPath lp1 = map1.get(id);
            LookupPath lp2 = map2.get(id);
            LookupDiff ld = new LookupDiff();
            ld.id = id;
            if (lp1 != null) ld.name1 = lp1.getName();
            if (lp2 != null) ld.name2 = lp2.getName();
            if (lp1 != null) ld.mappedTo1 = lp1.getTargetLFID();
            if (lp2 != null) ld.mappedTo2 = lp2.getTargetLFID();

            if (lp1 == null) {
                differences.add(new ComponentDifference("Lookup", id, "missing", "exists"));
                lookupDiffs.put(id, ld);
                continue;
            }
            if (lp2 == null) {
                differences.add(new ComponentDifference("Lookup", id, "exists", "missing"));
                lookupDiffs.put(id, ld);
                continue;
            }

            // Both exist - compare steps
            boolean stepDiff = false;
            if (lp1.getNumberOfSteps() != lp2.getNumberOfSteps()) stepDiff = true;

            // iterate steps and compare key-level properties
            Iterator<LookupPathStep> s1 = lp1.getStepIterator();
            Iterator<LookupPathStep> s2 = lp2.getStepIterator();
            Map<Integer, LookupPathStep> steps1 = new TreeMap<>();
            Map<Integer, LookupPathStep> steps2 = new TreeMap<>();
            while (s1.hasNext()) { LookupPathStep st = s1.next(); steps1.put(st.getStepNum(), st); }
            while (s2.hasNext()) { LookupPathStep st = s2.next(); steps2.put(st.getStepNum(), st); }

            Set<Integer> stepNums = new TreeSet<>();
            stepNums.addAll(steps1.keySet());
            stepNums.addAll(steps2.keySet());

            for (Integer sn : stepNums) {
                LookupPathStep st1 = steps1.get(sn);
                LookupPathStep st2 = steps2.get(sn);
                LookupStepDiff lsd = new LookupStepDiff();
                lsd.stepNumber = sn;

                // collect keys by seq
                Map<Short, org.genevaers.repository.components.LookupPathKey> keys1 = new TreeMap<>();
                Map<Short, org.genevaers.repository.components.LookupPathKey> keys2 = new TreeMap<>();
                if (st1 != null) {
                    Iterator<org.genevaers.repository.components.LookupPathKey> ki = st1.getKeyIterator();
                    while (ki.hasNext()) { org.genevaers.repository.components.LookupPathKey k = ki.next(); keys1.put(k.getKeyNumber(), k); }
                }
                if (st2 != null) {
                    Iterator<org.genevaers.repository.components.LookupPathKey> ki = st2.getKeyIterator();
                    while (ki.hasNext()) { org.genevaers.repository.components.LookupPathKey k = ki.next(); keys2.put(k.getKeyNumber(), k); }
                }

                Set<Short> keyNums = new TreeSet<>();
                keyNums.addAll(keys1.keySet());
                keyNums.addAll(keys2.keySet());

                for (Short kn : keyNums) {
                    org.genevaers.repository.components.LookupPathKey k1 = keys1.get(kn);
                    org.genevaers.repository.components.LookupPathKey k2 = keys2.get(kn);

                    // LR Field
                    KeyPropDiff kp = new KeyPropDiff();
                    kp.seqNum = kn;
                    kp.propLabel = "LR Field";
                    kp.vdp1Value = (k1 != null && k1.getFieldId() > 0) ? String.valueOf(k1.getFieldId()) : "";
                    kp.vdp2Value = (k2 != null && k2.getFieldId() > 0) ? String.valueOf(k2.getFieldId()) : "";
                    if (!kp.vdp1Value.equals(kp.vdp2Value)) { lsd.keyProps.add(kp); stepDiff = true; }

                    // Source Type
                    KeyPropDiff kp2 = new KeyPropDiff();
                    kp2.seqNum = kn;
                    kp2.propLabel = "Source Type";
                    String st1Type = "";
                    String st2Type = "";
                    if (k1 != null) {
                        if (k1.getFieldId() > 0) st1Type = "LR Field";
                        else if (k1.getSymbolicName() != null && !k1.getSymbolicName().isEmpty()) st1Type = "Symbol";
                        else st1Type = "Constant";
                    }
                    if (k2 != null) {
                        if (k2.getFieldId() > 0) st2Type = "LR Field";
                        else if (k2.getSymbolicName() != null && !k2.getSymbolicName().isEmpty()) st2Type = "Symbol";
                        else st2Type = "Constant";
                    }
                    kp2.vdp1Value = st1Type;
                    kp2.vdp2Value = st2Type;
                    if (!kp2.vdp1Value.equals(kp2.vdp2Value)) { lsd.keyProps.add(kp2); stepDiff = true; }

                    // Constant value presence
                    KeyPropDiff kp3 = new KeyPropDiff();
                    kp3.seqNum = kn;
                    kp3.propLabel = "Constant";
                    kp3.vdp1Value = (k1 != null && k1.getValue() != null && !k1.getValue().isEmpty()) ? k1.getValue() : "";
                    kp3.vdp2Value = (k2 != null && k2.getValue() != null && !k2.getValue().isEmpty()) ? k2.getValue() : "";
                    if (!kp3.vdp1Value.equals(kp3.vdp2Value)) { lsd.keyProps.add(kp3); stepDiff = true; }

                    // Constant Length
                    KeyPropDiff kp4 = new KeyPropDiff();
                    kp4.seqNum = kn;
                    kp4.propLabel = "Constant Length";
                    kp4.vdp1Value = (k1 != null) ? String.valueOf(k1.getValueLength()) : "";
                    kp4.vdp2Value = (k2 != null) ? String.valueOf(k2.getValueLength()) : "";
                    if (!kp4.vdp1Value.equals(kp4.vdp2Value)) { lsd.keyProps.add(kp4); stepDiff = true; }

                    // LR name
                    KeyPropDiff kp5 = new KeyPropDiff();
                    kp5.seqNum = kn;
                    kp5.propLabel = "LR";
                    if (k1 != null && st1 != null) {
                        try { LogicalRecord src = Repository.getLogicalRecords().get(st1.getSourceLR()); kp5.vdp1Value = src != null ? src.getName() : ""; } catch (Exception ex) { kp5.vdp1Value = ""; }
                    } else kp5.vdp1Value = "";
                    if (k2 != null && st2 != null) {
                        try { LogicalRecord src = Repository.getLogicalRecords().get(st2.getSourceLR()); kp5.vdp2Value = src != null ? src.getName() : ""; } catch (Exception ex) { kp5.vdp2Value = ""; }
                    } else kp5.vdp2Value = "";
                    if (!kp5.vdp1Value.equals(kp5.vdp2Value)) { lsd.keyProps.add(kp5); stepDiff = true; }

                }

                if (!lsd.keyProps.isEmpty()) {
                    ld.steps.add(lsd);
                }
            }

            if (stepDiff) {
                differences.add(new ComponentDifference("Lookup Logic", id, "does not match", "does not match"));
                lookupDiffs.put(id, ld);
            }
        }
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
    private void compareViewProperties(ComponentCollection<ViewNode> coll1, ComponentCollection<ViewNode> coll2) {
        Map<Integer, ViewNode> map1 = buildMap(coll1);
        Map<Integer, ViewNode> map2 = buildMap(coll2);
        
        // Check for views with logic differences
        Set<Integer> allIds = new HashSet<>();
        allIds.addAll(map1.keySet());
        allIds.addAll(map2.keySet());
        
        viewLogicDiffs.clear();
        for (Integer id : allIds) {
            ViewNode v1 = map1.get(id);
            ViewNode v2 = map2.get(id);

            if (v1 == null || v2 == null) {
                continue;
            }

            ViewLogicDiff vld = new ViewLogicDiff();
            vld.viewId = id;
            vld.name1 = v1.getName();
            vld.name2 = v2.getName();

            if (!Objects.equals(v1.getName(), v2.getName())) {
                viewLogicDiffs.put(id, vld);
                continue;
            }

            boolean viewLogicDifference = hasLogicDifference(v1, v2);

            Map<Integer, ViewColumn> cols1 = new TreeMap<>();
            Iterator<ViewColumn> c1 = v1.getColumnIterator();
            while (c1.hasNext()) {
                ViewColumn vc = c1.next();
                cols1.put(vc.getColumnNumber(), vc);
            }
            Map<Integer, ViewColumn> cols2 = new TreeMap<>();
            Iterator<ViewColumn> c2 = v2.getColumnIterator();
            while (c2.hasNext()) {
                ViewColumn vc = c2.next();
                cols2.put(vc.getColumnNumber(), vc);
            }

            Set<Integer> colNums = new TreeSet<>();
            colNums.addAll(cols1.keySet());
            colNums.addAll(cols2.keySet());

            boolean hasAnyColumnDifference = false;
            for (Integer cn : colNums) {
                ViewColumn a = cols1.get(cn);
                ViewColumn b = cols2.get(cn);
                if (a == null && b == null) continue;

                ColumnDiff cd = new ColumnDiff();
                cd.columnNumber = cn;
                cd.id = (a != null) ? a.getComponentId() : (b != null ? b.getComponentId() : 0);
                cd.vdp1Status = (a != null) ? "exists" : "missing";
                cd.vdp2Status = (b != null) ? "exists" : "missing";
                if (a != null) {
                    cd.name1 = a.getName();
                }
                if (b != null) {
                    cd.name2 = b.getName();
                }

                cd.props.addAll(compareColumnProperties(a, b));
                boolean columnDiff = !cd.props.isEmpty();

                if (columnDiff) {
                    vld.columnDiffs.add(cd);
                    hasAnyColumnDifference = true;
                }

                List<ColumnLogicDiff> columnLogicDiffs = compareColumnLogic(a, b);
                if (!columnLogicDiffs.isEmpty()) {
                    vld.columnLogicDiffs.addAll(columnLogicDiffs);
                }
            }
            if (hasAnyColumnDifference || !vld.columnLogicDiffs.isEmpty()) {
                viewLogicDiffs.put(id, vld);
                viewLogicDifference = true;
            }
            if (viewLogicDifference) {
                addDifference("View Logic", id, "does not match", "does not match");
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
                addDifference(typeName, id, "missing", "exists");
            } else if (comp2 == null) {
                addDifference(typeName, id, "exists", "missing");
            } else {
                // Both exist - check if they match
                if (!getName.apply(comp1).equals(getName.apply(comp2))) {
                    addDifference(typeName, id, "does not match", "does not match");
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
            writeExtractPhaseDDList(writer);
            writeFormatPhaseDDList(writer);
            writeUserExitComparison(writer);
            writeLogicalFileComparison(writer);
            writeLogicalRecordComparison(writer);
            writeViewLogicComparison(writer);
            writeLookupPathComparison(writer);
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
     * Write dedicated User Exit Routine comparison section
     */
    private void writeUserExitComparison(Writer writer) throws IOException {
        List<ComponentDifference> exitDiffs = new ArrayList<>();
        for (ComponentDifference diff : differences) {
            if (USER_EXIT_ROUTINE_TYPE.equals(diff.componentType)) {
                exitDiffs.add(diff);
            }
        }
        if (exitDiffs.isEmpty()) {
            return;
        }
        writer.write("User Exit Routine Comparison\n");
        writer.write(" ----------------------------\n");
        writer.write("  \n");
        writer.write(" Compared :  All\n");
        writer.write(" *---------------------------------------------------------------------\n");
        for (ComponentDifference diff : exitDiffs) {
            writer.write(String.format("   ID %26d\n", diff.id));
            writer.write(String.format(" -  VDP1                       %s\n", diff.vdp1Status));
            writer.write(String.format(" -  VDP2                       %s\n", diff.vdp2Status));
            writer.write(" *---------------------------------------------------------------------\n");
        }
        writer.write("  \n");
    }

    private void writeExtractPhaseDDList(Writer writer) throws IOException {
        Set<String> inputUnion = new TreeSet<>();
        inputUnion.addAll(extractInputDDs1);
        inputUnion.addAll(extractInputDDs2);
        boolean inputHasDifference = false;
        for (String dd : inputUnion) {
            String status1 = extractInputDDs1.contains(dd) ? "exists" : "missing";
            String status2 = extractInputDDs2.contains(dd) ? "exists" : "missing";
            if (!Objects.equals(status1, status2)) {
                inputHasDifference = true;
                break;
            }
        }

        Set<String> outputUnion = new TreeSet<>();
        outputUnion.addAll(extractOutputDDs1);
        outputUnion.addAll(extractOutputDDs2);
        boolean outputHasDifference = false;
        for (String dd : outputUnion) {
            String status1 = extractOutputDDs1.contains(dd) ? "exists" : "missing";
            String status2 = extractOutputDDs2.contains(dd) ? "exists" : "missing";
            if (!Objects.equals(status1, status2)) {
                outputHasDifference = true;
                break;
            }
        }

        if (!inputHasDifference && !outputHasDifference) {
            return;
        }

        String sep = "~---------------------------------------------------------------------\n";
        writer.write(sep);
        writer.write(" Extract-phase DD statement list\n");
        writer.write(sep);

        if (inputHasDifference) {
            writer.write(" Input DD  \n");
            for (String dd : inputUnion) {
                String status1 = extractInputDDs1.contains(dd) ? "exists" : "missing";
                String status2 = extractInputDDs2.contains(dd) ? "exists" : "missing";
                if (Objects.equals(status1, status2)) {
                    continue;
                }
                writer.write(String.format("     %-25s\n", dd));
                writer.write(String.format(" -    VDP1 %25s\n", status1));
                writer.write(String.format(" -    VDP2 %25s\n", status2));
            }
        }

        writer.write(sep);

        if (outputHasDifference) {
            writer.write(" Output DD \n");
            for (String dd : outputUnion) {
                String status1 = extractOutputDDs1.contains(dd) ? "exists" : "missing";
                String status2 = extractOutputDDs2.contains(dd) ? "exists" : "missing";
                if (Objects.equals(status1, status2)) {
                    continue;
                }
                writer.write(String.format("     %-25s\n", dd));
                writer.write(String.format(" -    VDP1 %25s\n", status1));
                writer.write(String.format(" -    VDP2 %25s\n", status2));
            }
        }
        writer.write("\n");
    }

    private void writeFormatPhaseDDList(Writer writer) throws IOException {
        int unionCount = 0;
        Set<String> union = new TreeSet<>();
        union.addAll(formatPhaseDDs1);
        union.addAll(formatPhaseDDs2);
        unionCount = union.size();

        writer.write(String.format("Format-phase DD statement list (%02d)\n\n", unionCount));
        // nothing further requested in sample beyond the count
        writer.write(" \n");
    }

    private void writeDDNameDifferences(Writer writer, Set<String> set1, Set<String> set2, String phaseLabel) throws IOException {
        Set<String> onlyIn1 = new TreeSet<>(set1);
        onlyIn1.removeAll(set2);
        Set<String> onlyIn2 = new TreeSet<>(set2);
        onlyIn2.removeAll(set1);

        if (onlyIn1.isEmpty() && onlyIn2.isEmpty()) {
            writer.write(String.format(" %s DD names match between VDP1 and VDP2.\n", phaseLabel));
            writer.write("  \n");
            return;
        }

        if (!onlyIn1.isEmpty()) {
            writer.write(String.format(" %s names only in VDP1:\n", phaseLabel));
            for (String ddName : onlyIn1) {
                writer.write(String.format("   %s\n", ddName));
            }
            writer.write("  \n");
        }
        if (!onlyIn2.isEmpty()) {
            writer.write(String.format(" %s names only in VDP2:\n", phaseLabel));
            for (String ddName : onlyIn2) {
                writer.write(String.format("   %s\n", ddName));
            }
            writer.write("  \n");
        }
    }
    
    /**
     * Write differences section
     */
    private void writeDifferences(Writer writer) throws IOException {
        List<ComponentDifference> otherDiffs = new ArrayList<>();
        for (ComponentDifference diff : differences) {
            if (!USER_EXIT_ROUTINE_TYPE.equals(diff.componentType)) {
                otherDiffs.add(diff);
            }
        }
        if (otherDiffs.isEmpty()) {
            if (differences.isEmpty()) {
                writer.write(" No differences found.\n");
            }
            return;
        }
        
        writer.write(" -------------------------------------------------------------------------------\n");
        writer.write(" Component Type            ID  VDP1                VDP2\n");
        writer.write(" -------------------------------------------------------------------------------\n");
        
        for (ComponentDifference diff : otherDiffs) {
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
        writer.write(String.format(" Total Number of Differences: %d\n", differences.size()));
    }
}
