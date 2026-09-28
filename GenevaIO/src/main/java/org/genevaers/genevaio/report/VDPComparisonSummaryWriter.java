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
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.*;
import org.genevaers.repository.data.ComponentCollection;
import org.genevaers.genevaio.vdpfile.VDPFileReader;
import org.genevaers.utilities.GersFile;

import com.google.common.flogger.FluentLogger;

/**
 * VDP Comparison Summary Report Writer.
 * Generates CSUMRPT format comparing two VDP files.
 */
public class VDPComparisonSummaryWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final String SEP = " *---------------------------------------------------------------------\n";
    private static final String USER_EXIT_ROUTINE_TYPE = "User Exit Routine";

    private String vdp1Path;
    private String vdp2Path;
    private String vdp1Date;
    private String vdp2Date;

    private final Map<String, Integer> vdp1Counts = new LinkedHashMap<>();
    private final Map<String, Integer> vdp2Counts = new LinkedHashMap<>();
    private final Set<String> extractInputDDs1  = new TreeSet<>();
    private final Set<String> extractInputDDs2  = new TreeSet<>();
    private final Set<String> extractOutputDDs1 = new TreeSet<>();
    private final Set<String> extractOutputDDs2 = new TreeSet<>();
    private final Set<String> formatPhaseDDs1   = new TreeSet<>();
    private final Set<String> formatPhaseDDs2   = new TreeSet<>();
    private final Map<Integer, List<PhysicalFile>> lfPfMap1   = new HashMap<>();
    private final Map<Integer, List<PhysicalFile>> lfPfMap2   = new HashMap<>();
    private final Map<Integer, String>             lfNameMap1 = new HashMap<>();
    private final Map<Integer, String>             lfNameMap2 = new HashMap<>();

    // Keyed on "type:id" for O(1) dedup
    private final Map<String, ComponentDifference> differencesMap = new LinkedHashMap<>();
    private final Map<Integer, ViewLogicDiff>      viewLogicDiffs = new TreeMap<>();
    private final Map<Integer, LookupDiff>         lookupDiffs    = new TreeMap<>();
    private final Map<Integer, LogicalRecordDiff>  logicalRecordDiffs = new TreeMap<>();

    // -------------------------------------------------------------------------
    // Inner data classes
    // -------------------------------------------------------------------------

    private static class ViewLogicDiff {
        int viewId;
        String name1 = "", name2 = "";
        List<ColumnDiff>      columnDiffs      = new ArrayList<>();
        List<ColumnLogicDiff> columnLogicDiffs = new ArrayList<>();
        List<SortKeyDiff>     sortKeyDiffs     = new ArrayList<>();
    }

    private static class ColumnDiff {
        int columnNumber, id;
        String name1 = "", name2 = "", vdp1Status, vdp2Status;
        List<PropertyDiff> props = new ArrayList<>();
    }

    private static class ColumnLogicDiff {
        int columnNumber, sourceNumber;
        String logic1 = "", logic2 = "";
    }

    private static class SortKeyDiff {
        int sequenceNumber, columnId;
        List<PropertyDiff> props = new ArrayList<>();
    }

    private static class LookupDiff {
        int id;
        String name1 = "", name2 = "";
        Integer mappedTo1 = null, mappedTo2 = null;
        List<LookupStepDiff> steps = new ArrayList<>();
    }

    private static class LookupStepDiff {
        int stepNumber;
        List<KeyPropDiff> keyProps = new ArrayList<>();
    }

    private static class KeyPropDiff {
        int seqNum;
        String propLabel, vdp1Value, vdp2Value;
    }

    private static class LogicalRecordDiff {
        int id;
        String name1 = "", name2 = "";
        Integer lookupExit1 = null, lookupExit2 = null;
        String lookupExitParams1 = "", lookupExitParams2 = "";
        List<LogicalFieldDiff> fields = new ArrayList<>();
    }

    private static class LogicalFieldDiff {
        int id;
        String name = "";
        boolean missing1, missing2;
        List<PropertyDiff> props = new ArrayList<>();
    }

    private static class PropertyDiff {
        String label, vdp1Value, vdp2Value;
    }

    private static class ComponentDifference implements Comparable<ComponentDifference> {
        final String componentType;
        final int    id;
        final String vdp1Status, vdp2Status;

        ComponentDifference(String type, int id, String vdp1Status, String vdp2Status) {
            this.componentType = type;
            this.id = id;
            this.vdp1Status = vdp1Status;
            this.vdp2Status = vdp2Status;
        }

        @Override
        public int compareTo(ComponentDifference o) {
            int c = this.componentType.compareTo(o.componentType);
            return c != 0 ? c : Integer.compare(this.id, o.id);
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public VDPComparisonSummaryWriter(String vdp1Path, String vdp2Path) {
        this.vdp1Path = vdp1Path;
        this.vdp2Path = vdp2Path;
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        this.vdp1Date = currentDate;
        this.vdp2Date = currentDate;
    }

    // -------------------------------------------------------------------------
    // Public entry points
    // -------------------------------------------------------------------------

    public void compareRepositories(Repository repo1, Repository repo2, String outputPath) throws IOException {
        logger.atInfo().log("Starting VDP comparison: %s vs %s", vdp1Path, vdp2Path);
        collectExtractPhaseDDNames(repo1.getPhysicalFiles(), extractInputDDs1, extractOutputDDs1);
        collectExtractPhaseDDNames(repo2.getPhysicalFiles(), extractInputDDs2, extractOutputDDs2);
        collectFormatPhaseDDNames(repo1.getViews(), formatPhaseDDs1);
        collectFormatPhaseDDNames(repo2.getViews(), formatPhaseDDs2);
        compareAllComponents(repo1, repo2);
        writeReport(outputPath);
        logger.atInfo().log("Comparison complete. Found %d differences", differencesMap.size());
    }

    public void writeFromVDPFiles(String vdp1PathStr, String vdp2PathStr, String outputPath) throws IOException {
        try {
            Path p1 = Paths.get(vdp1PathStr);
            Path p2 = Paths.get(vdp2PathStr);

            Repository.clearAndInitialise();
            VDPFileReader r = new VDPFileReader();
            r.open(p1, p1.getFileName().toString());
            r.addToRepsitory();
            Snapshot snap1 = Snapshot.capture();

            Repository.clearAndInitialise();
            r.open(p2, p2.getFileName().toString());
            r.addToRepsitory();
            Snapshot snap2 = Snapshot.capture();

            collectExtractPhaseDDNames(snap1.physicalFiles, extractInputDDs1, extractOutputDDs1);
            collectExtractPhaseDDNames(snap2.physicalFiles, extractInputDDs2, extractOutputDDs2);
            collectFormatPhaseDDNames(snap1.views, formatPhaseDDs1);
            collectFormatPhaseDDNames(snap2.views, formatPhaseDDs2);

            compareUserExits(snap1.userExits, snap2.userExits);
            compareControlRecords(snap1.controlRecords, snap2.controlRecords);
            comparePhysicalFiles(snap1.physicalFiles, snap2.physicalFiles);
            compareLogicalFiles(snap1.logicalFiles, snap2.logicalFiles);
            compareLogicalRecords(snap1.logicalRecords, snap2.logicalRecords, snap1.fields, snap2.fields);
            compareLookupPaths(snap1.lookupPaths, snap2.lookupPaths);
            compareViews(snap1.views, snap2.views);
            compareViewProperties(snap1.views, snap2.views);

            writeReport(outputPath);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /** Holds a captured snapshot of one Repository load. */
    private static class Snapshot {
        ComponentCollection<UserExit>       userExits;
        ComponentCollection<ControlRecord>  controlRecords;
        ComponentCollection<PhysicalFile>   physicalFiles;
        ComponentCollection<LogicalFile>    logicalFiles;
        ComponentCollection<LogicalRecord>  logicalRecords;
        ComponentCollection<LRField>        fields;
        ComponentCollection<LookupPath>     lookupPaths;
        ComponentCollection<ViewNode>       views;

        static Snapshot capture() {
            Snapshot s = new Snapshot();
            s.userExits      = copyCollection(Repository.getUserExits());
            s.controlRecords = copyCollection(Repository.getControlRecords());
            s.physicalFiles  = copyCollection(Repository.getPhysicalFiles());
            s.logicalFiles   = copyCollection(Repository.getLogicalFiles());
            s.logicalRecords = copyCollection(Repository.getLogicalRecords());
            s.fields         = copyCollection(Repository.getFields());
            s.lookupPaths    = copyCollection(Repository.getLookups());
            s.views          = copyCollection(Repository.getViews());
            return s;
        }
    }

    private static <T> ComponentCollection<T> copyCollection(ComponentCollection<T> src) {
        ComponentCollection<T> dst = new ComponentCollection<>();
        Iterator<T> it = src.getIterator();
        while (it.hasNext()) {
            T c = it.next();
            int id = getComponentId(c);
            String name = getComponentName(c);
            if (name != null && !name.isEmpty()) dst.add(c, id, name);
            else                                 dst.add(c, id);
        }
        return dst;
    }

    // -------------------------------------------------------------------------
    // Comparison helpers
    // -------------------------------------------------------------------------

    private void compareAllComponents(Repository repo1, Repository repo2) {
        compareUserExits(repo1.getUserExits(), repo2.getUserExits());
        compareControlRecords(repo1.getControlRecords(), repo2.getControlRecords());
        comparePhysicalFiles(repo1.getPhysicalFiles(), repo2.getPhysicalFiles());
        compareLogicalFiles(repo1.getLogicalFiles(), repo2.getLogicalFiles());
        compareLogicalRecords(repo1.getLogicalRecords(), repo2.getLogicalRecords(),
                              repo1.getFields(), repo2.getFields());
        compareLookupPaths(repo1.getLookups(), repo2.getLookups());
        compareViews(repo1.getViews(), repo2.getViews());
        compareViewProperties(repo1.getViews(), repo2.getViews());
    }

    private void compareUserExits(ComponentCollection<UserExit> c1, ComponentCollection<UserExit> c2) {
        Map<Integer, UserExit> m1 = buildMap(c1), m2 = buildMap(c2);
        vdp1Counts.put("User Exit Routines", m1.size());
        vdp2Counts.put("User Exit Routines", m2.size());
        compareByIdAndName(USER_EXIT_ROUTINE_TYPE, m1, m2, UserExit::getComponentId, UserExit::getName);
    }

    private void compareControlRecords(ComponentCollection<ControlRecord> c1, ComponentCollection<ControlRecord> c2) {
        vdp1Counts.put("Control Records", c1.size());
        vdp2Counts.put("Control Records", c2.size());
    }

    private void comparePhysicalFiles(ComponentCollection<PhysicalFile> c1, ComponentCollection<PhysicalFile> c2) {
        Map<Integer, PhysicalFile> m1 = buildMap(c1), m2 = buildMap(c2);
        vdp1Counts.put("Physical Files", m1.size());
        vdp2Counts.put("Physical Files", m2.size());
        compareByIdAndName("Physical File", m1, m2, PhysicalFile::getComponentId, PhysicalFile::getName);
    }

    private void compareLogicalFiles(ComponentCollection<LogicalFile> c1, ComponentCollection<LogicalFile> c2) {
        Map<Integer, LogicalFile> m1 = buildMap(c1), m2 = buildMap(c2);
        vdp1Counts.put("Logical Files", m1.size());
        vdp2Counts.put("Logical Files", m2.size());
        compareByIdAndName("Logical File", m1, m2, LogicalFile::getID, LogicalFile::getName);
        populateLfPfMaps(c1, lfPfMap1, lfNameMap1);
        populateLfPfMaps(c2, lfPfMap2, lfNameMap2);
    }

    private void populateLfPfMaps(ComponentCollection<LogicalFile> coll,
                                   Map<Integer, List<PhysicalFile>> pfMap,
                                   Map<Integer, String> nameMap) {
        pfMap.clear();
        nameMap.clear();
        Iterator<LogicalFile> it = coll.getIterator();
        while (it.hasNext()) {
            LogicalFile lf = it.next();
            List<PhysicalFile> pfs = new ArrayList<>();
            Iterator<PhysicalFile> pfi = lf.getPFIterator();
            while (pfi.hasNext()) pfs.add(pfi.next());
            pfMap.put(lf.getID(), pfs);
            nameMap.put(lf.getID(), lf.getName());
        }
    }

    private void compareViews(ComponentCollection<ViewNode> c1, ComponentCollection<ViewNode> c2) {
        Map<Integer, ViewNode> m1 = buildMap(c1), m2 = buildMap(c2);
        vdp1Counts.put("Views", m1.size());
        vdp2Counts.put("Views", m2.size());
        compareByIdAndName("View", m1, m2, ViewNode::getID, ViewNode::getName);
    }

    private <T> void compareByIdAndName(String typeName,
                                         Map<Integer, T> map1, Map<Integer, T> map2,
                                         java.util.function.Function<T, Integer> getId,
                                         java.util.function.Function<T, String> getName) {
        Set<Integer> allIds = new HashSet<>(map1.keySet());
        allIds.addAll(map2.keySet());
        for (Integer id : allIds) {
            T c1 = map1.get(id), c2 = map2.get(id);
            if      (c1 == null)                               addDifference(typeName, id, "missing",        "exists");
            else if (c2 == null)                               addDifference(typeName, id, "exists",         "missing");
            else if (!getName.apply(c1).equals(getName.apply(c2))) addDifference(typeName, id, "does not match", "does not match");
        }
    }

    private void addDifference(String type, int id, String vdp1Status, String vdp2Status) {
        differencesMap.putIfAbsent(type + ":" + id, new ComponentDifference(type, id, vdp1Status, vdp2Status));
    }

    private <T> Map<Integer, T> buildMap(ComponentCollection<T> collection) {
        Map<Integer, T> map = new HashMap<>();
        Iterator<T> iter = collection.getIterator();
        while (iter.hasNext()) {
            T component = iter.next();
            map.put(getComponentId(component), component);
        }
        return map;
    }

    private static int getComponentId(Object component) {
        if (component instanceof ViewNode)      return ((ViewNode)      component).getID();
        if (component instanceof LookupPath)    return ((LookupPath)    component).getID();
        if (component instanceof LogicalFile)   return ((LogicalFile)   component).getID();
        if (component instanceof LogicalRecord) return ((LogicalRecord) component).getComponentId();
        if (component instanceof PhysicalFile)  return ((PhysicalFile)  component).getComponentId();
        if (component instanceof UserExit)      return ((UserExit)      component).getComponentId();
        if (component instanceof ControlRecord) return ((ControlRecord) component).getComponentId();
        if (component instanceof LRField)       return ((LRField)       component).getComponentId();
        return 0;
    }

    private static String getComponentName(Object component) {
        try {
            return (String) component.getClass().getMethod("getName").invoke(component);
        } catch (Exception e) {
            return "";
        }
    }

    // -------------------------------------------------------------------------
    // Column property comparison (reflection-based, driven by BeanInfo)
    // -------------------------------------------------------------------------

    private List<PropertyDiff> compareColumnProperties(ViewColumn col1, ViewColumn col2) {
        Class<?> cls = col1 != null ? col1.getClass() : col2.getClass();
        Map<String, String> displayNames = ViewColumn.getDisplayNames();

        Map<String, Method> properties = new TreeMap<>();
        for (Method m : cls.getMethods()) {
            if (m.getParameterCount() != 0 || m.getReturnType() == Void.TYPE) continue;
            String prop = null;
            if (m.getName().startsWith("get") && m.getName().length() > 3)
                prop = Introspector.decapitalize(m.getName().substring(3));
            else if (m.getName().startsWith("is") && m.getName().length() > 2
                    && (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class))
                prop = Introspector.decapitalize(m.getName().substring(2));
            if (prop != null && isColumnProperty(prop, m.getReturnType()))
                properties.put(prop, m);
        }

        if (properties.isEmpty()) {
            logger.atWarning().log("Unable to inspect ViewColumn properties");
            return Collections.emptyList();
        }

        List<PropertyDiff> diffs = new ArrayList<>();
        for (Map.Entry<String, Method> entry : properties.entrySet()) {
            Object v1 = invoke(entry.getValue(), col1);
            Object v2 = invoke(entry.getValue(), col2);
            String s1 = v1 == null ? "" : String.valueOf(v1);
            String s2 = v2 == null ? "" : String.valueOf(v2);
            if (isMissing(s1) && isMissing(s2)) continue;
            if (Objects.equals(v1, v2))          continue;

            PropertyDiff pd = new PropertyDiff();
            pd.label     = resolveLabel(entry.getKey(), displayNames);
            pd.vdp1Value = s1;
            pd.vdp2Value = s2;
            diffs.add(pd);
        }
        return diffs;
    }

    private static boolean isColumnProperty(String name, Class<?> type) {
        return !name.equals("componentId") && !name.equals("viewId") && !name.equals("columnNumber")
                && !name.equals("ordinalPosition")
                && (type.isPrimitive() || type.isEnum() || type == String.class
                    || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class);
    }

    private static Object invoke(Method m, ViewColumn col) {
        if (col == null) return null;
        try { return m.invoke(col); } catch (Exception e) { return null; }
    }

    private static String resolveLabel(String propertyName, Map<String, String> displayNames) {
        String dn = displayNames.get(propertyName);
        if (dn != null && !dn.equals(propertyName)) return dn;
        // Fall back: split camelCase → "Camel Case"
        StringBuilder sb = new StringBuilder();
        for (char c : propertyName.toCharArray()) {
            if (Character.isUpperCase(c) && sb.length() > 0) sb.append(' ');
            sb.append(sb.length() == 0 ? Character.toUpperCase(c) : c);
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Column logic comparison
    // -------------------------------------------------------------------------

    private List<ColumnLogicDiff> compareColumnLogic(ViewColumn col1, ViewColumn col2) {
        Map<Integer, String> logic1 = columnSourceLogic(col1);
        Map<Integer, String> logic2 = columnSourceLogic(col2);
        Set<Integer> sources = new TreeSet<>(logic1.keySet());
        sources.addAll(logic2.keySet());

        List<ColumnLogicDiff> diffs = new ArrayList<>();
        for (Integer sn : sources) {
            String t1 = logic1.getOrDefault(sn, "");
            String t2 = logic2.getOrDefault(sn, "");
            if (!Objects.equals(t1, t2) && !(isMissing(t1) && isMissing(t2))) {
                ColumnLogicDiff d = new ColumnLogicDiff();
                d.columnNumber = col1 != null ? col1.getColumnNumber() : col2.getColumnNumber();
                d.sourceNumber = sn;
                d.logic1 = t1;
                d.logic2 = t2;
                diffs.add(d);
            }
        }
        return diffs;
    }

    private static Map<Integer, String> columnSourceLogic(ViewColumn col) {
        Map<Integer, String> logic = new TreeMap<>();
        if (col == null) return logic;
        Iterator<ViewColumnSource> it = col.getIteratorForSourcesByNumber();
        int n = 1;
        while (it.hasNext()) {
            ViewColumnSource src = it.next();
            logic.put(n++, src.getLogicText() == null ? "" : src.getLogicText());
        }
        return logic;
    }

    // -------------------------------------------------------------------------
    // View properties comparison
    // -------------------------------------------------------------------------

    private void compareViewProperties(ComponentCollection<ViewNode> c1, ComponentCollection<ViewNode> c2) {
        Map<Integer, ViewNode> m1 = buildMap(c1), m2 = buildMap(c2);
        Set<Integer> allIds = new HashSet<>(m1.keySet());
        allIds.addAll(m2.keySet());

        viewLogicDiffs.clear();
        for (Integer id : allIds) {
            ViewNode v1 = m1.get(id), v2 = m2.get(id);
            if (v1 == null || v2 == null) continue;

            ViewLogicDiff vld = new ViewLogicDiff();
            vld.viewId = id;
            vld.name1  = v1.getName();
            vld.name2  = v2.getName();

            if (!Objects.equals(v1.getName(), v2.getName())) {
                viewLogicDiffs.put(id, vld);
                continue;
            }

            boolean viewDiff = v1.getNumberOfColumns() != v2.getNumberOfColumns()
                            || v1.getNumberOfViewSources() != v2.getNumberOfViewSources();

            // Columns
            Map<Integer, ViewColumn> cols1 = buildColumnMap(v1);
            Map<Integer, ViewColumn> cols2 = buildColumnMap(v2);
            Set<Integer> colNums = new TreeSet<>(cols1.keySet());
            colNums.addAll(cols2.keySet());

            for (Integer cn : colNums) {
                ViewColumn a = cols1.get(cn), b = cols2.get(cn);
                if (a == null && b == null) continue;

                ColumnDiff cd = new ColumnDiff();
                cd.columnNumber = cn;
                cd.id        = a != null ? a.getComponentId() : b.getComponentId();
                cd.vdp1Status = a != null ? "exists" : "missing";
                cd.vdp2Status = b != null ? "exists" : "missing";
                cd.name1 = a != null ? a.getName() : "";
                cd.name2 = b != null ? b.getName() : "";
                cd.props.addAll(compareColumnProperties(a, b));

                if (!cd.props.isEmpty()) {
                    vld.columnDiffs.add(cd);
                    viewDiff = true;
                }
                vld.columnLogicDiffs.addAll(compareColumnLogic(a, b));
            }

            compareSortKeys(v1, v2, vld.sortKeyDiffs);

            if (viewDiff || !vld.columnDiffs.isEmpty()
                         || !vld.columnLogicDiffs.isEmpty()
                         || !vld.sortKeyDiffs.isEmpty()) {
                viewLogicDiffs.put(id, vld);
                addDifference("View Logic", id, "does not match", "does not match");
            }
        }
    }

    private static Map<Integer, ViewColumn> buildColumnMap(ViewNode view) {
        Map<Integer, ViewColumn> map = new TreeMap<>();
        Iterator<ViewColumn> it = view.getColumnIterator();
        while (it.hasNext()) { ViewColumn vc = it.next(); map.put(vc.getColumnNumber(), vc); }
        return map;
    }

    private void compareSortKeys(ViewNode v1, ViewNode v2, List<SortKeyDiff> diffs) {
        Map<Short, ViewSortKey> keys1 = new TreeMap<>(), keys2 = new TreeMap<>();
        Iterator<ViewSortKey> it1 = v1.getSortKeyIterator(), it2 = v2.getSortKeyIterator();
        while (it1.hasNext()) { ViewSortKey k = it1.next(); keys1.put(k.getSequenceNumber(), k); }
        while (it2.hasNext()) { ViewSortKey k = it2.next(); keys2.put(k.getSequenceNumber(), k); }

        Set<Short> seqs = new TreeSet<>(keys1.keySet());
        seqs.addAll(keys2.keySet());
        for (Short seq : seqs) {
            ViewSortKey k1 = keys1.get(seq), k2 = keys2.get(seq);
            SortKeyDiff skd = new SortKeyDiff();
            skd.sequenceNumber = seq;
            skd.columnId = k1 != null && k1.getViewSortKeyId() > 0 ? k1.getViewSortKeyId()
                         : (k2 != null && k2.getViewSortKeyId() > 0 ? k2.getViewSortKeyId()
                         : (k1 != null && k1.getColumnId() > 0 ? k1.getColumnId()
                         : (k2 != null ? k2.getColumnId() : 0)));
            skd.props.addAll(compareSortKeyProperties(k1, k2));
            if (!skd.props.isEmpty()) diffs.add(skd);
        }
    }

    private List<PropertyDiff> compareSortKeyProperties(ViewSortKey sk1, ViewSortKey sk2) {
        if (sk1 == null && sk2 == null) {
            return Collections.emptyList();
        }
        Class<?> cls = sk1 != null ? sk1.getClass() : sk2.getClass();
        Map<String, Method> properties = getSortKeyProperties(cls);
        if (properties.isEmpty()) {
            logger.atWarning().log("Unable to inspect ViewSortKey properties");
            return Collections.emptyList();
        }

        Map<String, String> displayNames = ViewSortKey.getDisplayNames();
        List<PropertyDiff> diffs = new ArrayList<>();
        for (Map.Entry<String, Method> entry : properties.entrySet()) {
            Object v1 = invokeSortKey(entry.getValue(), sk1);
            Object v2 = invokeSortKey(entry.getValue(), sk2);

            if (Objects.equals(v1, v2)) continue;

            String s1 = v1 == null ? "" : String.valueOf(v1);
            String s2 = v2 == null ? "" : String.valueOf(v2);
            if (isMissing(s1) && isMissing(s2)) continue;

            PropertyDiff pd = new PropertyDiff();
            pd.label     = resolveSortKeyLabel(entry.getKey(), displayNames);
            pd.vdp1Value = s1;
            pd.vdp2Value = s2;
            diffs.add(pd);
        }
        return diffs;
    }

    private static final Map<Class<?>, Map<String, Method>> SK_PROPERTY_CACHE = new HashMap<>();

    private static Map<String, Method> getSortKeyProperties(Class<?> cls) {
        return SK_PROPERTY_CACHE.computeIfAbsent(cls, c -> {
            Map<String, Method> props = new TreeMap<>();
            try {
                for (PropertyDescriptor pd : Introspector.getBeanInfo(c).getPropertyDescriptors()) {
                    Method reader = pd.getReadMethod();
                    if (reader != null && isSortKeyProperty(pd.getName(), pd.getPropertyType())) {
                        props.put(pd.getName(), reader);
                    }
                }
            } catch (Exception e) {
                logger.atWarning().withCause(e).log("Unable to inspect ViewSortKey properties for %s", c.getSimpleName());
            }
            return Collections.unmodifiableMap(props);
        });
    }

    private static String resolveSortKeyLabel(String propertyName, Map<String, String> displayNames) {
    String label = resolveLabel(propertyName, displayNames);
    String lower = label.toLowerCase();
    if (lower.startsWith("sort key ")) {
        label = label.substring(9).trim();
    } else if (lower.startsWith("skt ")) {
        label = label.substring(4).trim();
    } else if (lower.startsWith("sk ")) {
        label = label.substring(3).trim();
    } else if (label.equalsIgnoreCase("sort key")) {
        label = "Key";
    }
    return label;
}

    private static boolean isSortKeyProperty(String name, Class<?> type) {
        return !name.equals("componentId") && !name.equals("viewSortKeyId")
                && !name.equals("columnId") && !name.equals("sequenceNumber")
                && (type.isPrimitive() || type.isEnum() || type == String.class
                    || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class);
    }

    private static Object invokeSortKey(Method m, ViewSortKey sk) {
        if (sk == null) return null;
        try { return m.invoke(sk); } catch (Exception e) { return null; }
    }

    // -------------------------------------------------------------------------
    // Logical Record comparison
    // -------------------------------------------------------------------------

    private void compareLogicalRecords(ComponentCollection<LogicalRecord> c1, ComponentCollection<LogicalRecord> c2,
                                        ComponentCollection<LRField> fc1, ComponentCollection<LRField> fc2) {
        Map<Integer, LogicalRecord> m1 = buildMap(c1), m2 = buildMap(c2);
        vdp1Counts.put("Logical Records", m1.size());
        vdp2Counts.put("Logical Records", m2.size());
        compareByIdAndName("Logical Record", m1, m2, LogicalRecord::getComponentId, LogicalRecord::getName);

        logicalRecordDiffs.clear();
        Set<Integer> allIds = new TreeSet<>(m1.keySet());
        allIds.addAll(m2.keySet());

        for (Integer id : allIds) {
            LogicalRecord lr1 = m1.get(id), lr2 = m2.get(id);
            LogicalRecordDiff lrd = new LogicalRecordDiff();
            lrd.id = id;
            if (lr1 != null) { lrd.name1 = lr1.getName(); lrd.lookupExit1 = lr1.getLookupExitID(); lrd.lookupExitParams1 = lr1.getLookupExitParams(); }
            if (lr2 != null) { lrd.name2 = lr2.getName(); lrd.lookupExit2 = lr2.getLookupExitID(); lrd.lookupExitParams2 = lr2.getLookupExitParams(); }

            if (lr1 == null || lr2 == null) {
                addDifference("Logical Record", id,
                        lr1 == null ? "missing" : "exists", lr2 == null ? "missing" : "exists");
                LogicalFieldDiff fd = new LogicalFieldDiff();
                fd.id = -1; fd.missing1 = lr1 == null; fd.missing2 = lr2 == null;
                fd.name = lr1 != null ? lr1.getName() : (lr2 != null ? lr2.getName() : "");
                lrd.fields.add(fd);
                logicalRecordDiffs.put(id, lrd);
                continue;
            }

            boolean hasDiff = lr1.getLookupExitID() != lr2.getLookupExitID()
                           || !Objects.equals(lr1.getLookupExitParams(), lr2.getLookupExitParams());

            Map<Integer, LRField> f1 = buildLRFieldMap(lr1, id, fc1);
            Map<Integer, LRField> f2 = buildLRFieldMap(lr2, id, fc2);
            Set<Integer> fieldIds = new TreeSet<>(f1.keySet());
            fieldIds.addAll(f2.keySet());

            for (Integer fid : fieldIds) {
                LRField field1 = f1.get(fid), field2 = f2.get(fid);
                LogicalFieldDiff fd = new LogicalFieldDiff();
                fd.id = fid;
                fd.name     = field1 != null ? field1.getName() : (field2 != null ? field2.getName() : "");
                fd.missing1 = field1 == null;
                fd.missing2 = field2 == null;

                if (field1 == null || field2 == null) { lrd.fields.add(fd); hasDiff = true; continue; }

                fd.props.addAll(compareLRFieldProperties(field1, field2));

                boolean nameChanged = !Objects.equals(field1.getName(), field2.getName());
                if (nameChanged || !fd.props.isEmpty()) { lrd.fields.add(fd); hasDiff = true; }
            }

            if (hasDiff || !lrd.fields.isEmpty()) {
                logicalRecordDiffs.put(id, lrd);
                addDifference("Logical Record", id, "does not match", "does not match");
            }
        }
    }

    private static Map<Integer, LRField> buildLRFieldMap(LogicalRecord lr, int lrId, ComponentCollection<LRField> fallback) {
        Map<Integer, LRField> map = new TreeMap<>();
        Iterator<LRField> it = lr.getIteratorForFieldsByID();
        while (it.hasNext()) { LRField f = it.next(); if (f != null) map.put(f.getComponentId(), f); }
        if (map.isEmpty() && fallback != null) {
            Iterator<LRField> fb = fallback.getIterator();
            while (fb.hasNext()) { LRField f = fb.next(); if (f != null && f.getLrID() == lrId) map.put(f.getComponentId(), f); }
        }
        return map;
    }

    private List<PropertyDiff> compareLRFieldProperties(LRField f1, LRField f2) {
        Class<?> cls = f1 != null ? f1.getClass() : f2.getClass();
        Map<String, String> displayNames = getLRFieldDisplayNames(cls);

        Map<String, Method> properties = new TreeMap<>();
        for (Method m : cls.getMethods()) {
            if (m.getParameterCount() != 0 || m.getReturnType() == Void.TYPE) continue;
            String prop = null;
            if (m.getName().startsWith("get") && m.getName().length() > 3)
                prop = Introspector.decapitalize(m.getName().substring(3));
            else if (m.getName().startsWith("is") && m.getName().length() > 2
                    && (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class))
                prop = Introspector.decapitalize(m.getName().substring(2));
            if (prop != null && isLRFieldProperty(prop, m.getReturnType()))
                properties.put(prop, m);
        }

        if (properties.isEmpty()) {
            logger.atWarning().log("Unable to inspect LRField properties");
            return Collections.emptyList();
        }

        List<PropertyDiff> diffs = new ArrayList<>();
        for (Map.Entry<String, Method> entry : properties.entrySet()) {
            Object v1 = invokeLRField(entry.getValue(), f1);
            Object v2 = invokeLRField(entry.getValue(), f2);
            String s1 = v1 == null ? "" : String.valueOf(v1);
            String s2 = v2 == null ? "" : String.valueOf(v2);
            if (isMissing(s1) && isMissing(s2)) continue;
            if (Objects.equals(v1, v2))          continue;

            PropertyDiff pd = new PropertyDiff();
            pd.label     = resolveLabel(entry.getKey(), displayNames);
            pd.vdp1Value = s1;
            pd.vdp2Value = s2;
            diffs.add(pd);
        }
        return diffs;
    }

    private static final Map<Class<?>, Map<String, String>> LRF_DISPLAY_NAME_CACHE = new HashMap<>();

    private static Map<String, String> getLRFieldDisplayNames(Class<?> cls) {
        return LRF_DISPLAY_NAME_CACHE.computeIfAbsent(cls, c -> {
            Map<String, String> names = new HashMap<>();
            try {
                for (PropertyDescriptor pd : Introspector.getBeanInfo(c).getPropertyDescriptors()) {
                    names.put(pd.getName(), pd.getDisplayName());
                }
            } catch (Exception e) {
                logger.atFine().withCause(e).log("Unable to inspect display names for %s", c.getSimpleName());
            }
            return names;
        });
    }

    private static boolean isLRFieldProperty(String name, Class<?> type) {
        return !name.equals("componentId") && !name.equals("lrID") && !name.equals("name")
                && !name.equals("ordinalPosition")
                && (type.isPrimitive() || type.isEnum() || type == String.class
                    || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class);
    }

    private static Object invokeLRField(Method m, LRField f) {
        if (f == null) return null;
        try { return m.invoke(f); } catch (Exception e) { return null; }
    }

    // -------------------------------------------------------------------------
    // Lookup Path comparison
    // -------------------------------------------------------------------------

    private void compareLookupPaths(ComponentCollection<LookupPath> c1, ComponentCollection<LookupPath> c2) {
        Map<Integer, LookupPath> m1 = buildMap(c1), m2 = buildMap(c2);
        vdp1Counts.put("Lookup Paths", m1.size());
        vdp2Counts.put("Lookup Paths", m2.size());
        compareByIdAndName("Lookup", m1, m2, LookupPath::getID, LookupPath::getName);

        lookupDiffs.clear();
        Set<Integer> allIds = new TreeSet<>(m1.keySet());
        allIds.addAll(m2.keySet());

        for (Integer id : allIds) {
            LookupPath lp1 = m1.get(id), lp2 = m2.get(id);
            LookupDiff ld = new LookupDiff();
            ld.id = id;
            if (lp1 != null) { ld.name1 = lp1.getName(); ld.mappedTo1 = lp1.getTargetLFID(); }
            if (lp2 != null) { ld.name2 = lp2.getName(); ld.mappedTo2 = lp2.getTargetLFID(); }

            if (lp1 == null) { addDifference("Lookup", id, "missing", "exists");  lookupDiffs.put(id, ld); continue; }
            if (lp2 == null) { addDifference("Lookup", id, "exists",  "missing"); lookupDiffs.put(id, ld); continue; }

            boolean stepDiff = lp1.getNumberOfSteps() != lp2.getNumberOfSteps();
            Map<Integer, LookupPathStep> steps1 = buildStepMap(lp1);
            Map<Integer, LookupPathStep> steps2 = buildStepMap(lp2);
            Set<Integer> stepNums = new TreeSet<>(steps1.keySet());
            stepNums.addAll(steps2.keySet());

            for (Integer sn : stepNums) {
                LookupPathStep st1 = steps1.get(sn), st2 = steps2.get(sn);
                LookupStepDiff lsd = new LookupStepDiff();
                lsd.stepNumber = sn;

                Map<Short, LookupPathKey> keys1 = buildKeyMap(st1);
                Map<Short, LookupPathKey> keys2 = buildKeyMap(st2);
                Set<Short> keyNums = new TreeSet<>(keys1.keySet());
                keyNums.addAll(keys2.keySet());

                for (Short kn : keyNums) {
                    LookupPathKey k1 = keys1.get(kn), k2 = keys2.get(kn);
                    stepDiff |= addKeyPropIfDiff(lsd.keyProps, kn, "LR Field",
                            k1 != null && k1.getFieldId() > 0 ? String.valueOf(k1.getFieldId()) : "",
                            k2 != null && k2.getFieldId() > 0 ? String.valueOf(k2.getFieldId()) : "");
                    stepDiff |= addKeyPropIfDiff(lsd.keyProps, kn, "Source Type", keySourceType(k1), keySourceType(k2));
                    stepDiff |= addKeyPropIfDiff(lsd.keyProps, kn, "Constant",
                            k1 != null && k1.getValue() != null ? k1.getValue() : "",
                            k2 != null && k2.getValue() != null ? k2.getValue() : "");
                    stepDiff |= addKeyPropIfDiff(lsd.keyProps, kn, "Constant Length",
                            k1 != null ? String.valueOf(k1.getValueLength()) : "",
                            k2 != null ? String.valueOf(k2.getValueLength()) : "");
                    stepDiff |= addKeyPropIfDiff(lsd.keyProps, kn, "LR", lrName(k1, st1), lrName(k2, st2));
                }
                if (!lsd.keyProps.isEmpty()) ld.steps.add(lsd);
            }

            if (stepDiff) {
                addDifference("Lookup Logic", id, "does not match", "does not match");
                lookupDiffs.put(id, ld);
            }
        }
    }

    private static Map<Integer, LookupPathStep> buildStepMap(LookupPath lp) {
        Map<Integer, LookupPathStep> map = new TreeMap<>();
        Iterator<LookupPathStep> it = lp.getStepIterator();
        while (it.hasNext()) { LookupPathStep s = it.next(); map.put(s.getStepNum(), s); }
        return map;
    }

    private static Map<Short, LookupPathKey> buildKeyMap(LookupPathStep step) {
        Map<Short, LookupPathKey> map = new TreeMap<>();
        if (step == null) return map;
        Iterator<LookupPathKey> it = step.getKeyIterator();
        while (it.hasNext()) { LookupPathKey k = it.next(); map.put(k.getKeyNumber(), k); }
        return map;
    }

    private static boolean addKeyPropIfDiff(List<KeyPropDiff> props, short seqNum, String label, String v1, String v2) {
        if (v1.equals(v2)) return false;
        KeyPropDiff kp = new KeyPropDiff();
        kp.seqNum = seqNum; kp.propLabel = label; kp.vdp1Value = v1; kp.vdp2Value = v2;
        props.add(kp);
        return true;
    }

    private static String keySourceType(LookupPathKey k) {
        if (k == null) return "";
        if (k.getFieldId() > 0) return "LR Field";
        if (k.getSymbolicName() != null && !k.getSymbolicName().isEmpty()) return "Symbol";
        return "Constant";
    }

    private static String lrName(LookupPathKey k, LookupPathStep step) {
        if (k == null || step == null) return "";
        try {
            LogicalRecord lr = Repository.getLogicalRecords().get(step.getSourceLR());
            return lr != null ? lr.getName() : "";
        } catch (Exception e) { return ""; }
    }

    // -------------------------------------------------------------------------
    // DD name collection
    // -------------------------------------------------------------------------

    private static void collectExtractPhaseDDNames(ComponentCollection<PhysicalFile> coll,
                                                    Set<String> inputs, Set<String> outputs) {
        Iterator<PhysicalFile> it = coll.getIterator();
        while (it.hasNext()) {
            PhysicalFile pf = it.next();
            addIfNonEmpty(inputs,  pf.getInputDDName());
            addIfNonEmpty(inputs,  pf.getExtractDDName());
            addIfNonEmpty(outputs, pf.getOutputDDName());
        }
    }

    private static void collectFormatPhaseDDNames(ComponentCollection<ViewNode> coll, Set<String> collector) {
        Iterator<ViewNode> it = coll.getIterator();
        while (it.hasNext()) {
            ViewNode v = it.next();
            if (v.getOutputFile() != null) addIfNonEmpty(collector, v.getOutputFile().getOutputDDName());
        }
    }

    private static void addIfNonEmpty(Set<String> set, String value) {
        if (value != null && !value.isEmpty()) set.add(value);
    }

    // -------------------------------------------------------------------------
    // Report writing
    // -------------------------------------------------------------------------

    private void writeReport(String outputPath) throws IOException {
        Writer output = new GersFile().getWriter(outputPath);
        if (output == null) throw new IOException("Unable to open report output " + outputPath);
        try (BufferedWriter w = new BufferedWriter(output)) {
            writeHeader(w);
            writeSummaryCounts(w);
            writeExtractPhaseDDList(w);
            writeFormatPhaseDDList(w);
            writeUserExitComparison(w);
            writeLogicalFileComparison(w);
            writeLogicalRecordComparison(w);
            writeViewLogicComparison(w);
            writeLookupPathComparison(w);
            writeDifferences(w);
            writeFooter(w);
        }
        logger.atInfo().log("Comparison report written to: %s", outputPath);
    }

    private static void line(Writer w, String fmt, Object... args) throws IOException {
        w.write(args.length == 0 ? fmt : String.format(fmt, args));
    }

    private void writeHeader(Writer w) throws IOException {
        line(w, "VDP Comparison Summary Report\n");
        line(w, " -------------------------\n");
        line(w, "  \n");
        line(w, " VDP Run Date           %s    %s\n", vdp1Date, vdp2Date);
        line(w, "  \n");
    }

    private void writeSummaryCounts(Writer w) throws IOException {
        line(w, " -------------------------------------------------------------------------------\n");
        line(w, " Component Type        VDPNEW Count  VDPOLD Count\n");
        line(w, " -------------------------------------------------------------------------------\n");
        for (String type : vdp1Counts.keySet())
            line(w, " %-20s %7d     %7d\n", type, vdp1Counts.get(type), vdp2Counts.getOrDefault(type, 0));
        line(w, "  \n");
    }

    private void writeUserExitComparison(Writer w) throws IOException {
        List<ComponentDifference> exitDiffs = differences().stream()
                .filter(d -> USER_EXIT_ROUTINE_TYPE.equals(d.componentType))
                .collect(Collectors.toList());
        if (exitDiffs.isEmpty()) return;
        line(w, "User Exit Routine Comparison\n");
        line(w, " ----------------------------\n");
        line(w, "  \n");
        line(w, " Compared :  All\n");
        line(w, SEP);
        for (ComponentDifference d : exitDiffs) {
            line(w, "   ID %26d\n", d.id);
            line(w, " -  VDPNEW                       %s\n", d.vdp1Status);
            line(w, " -  VDPOLD                       %s\n", d.vdp2Status);
            line(w, SEP);
        }
        line(w, "  \n");
    }

    private void writeLogicalFileComparison(Writer w) throws IOException {
        Set<Integer> allLfIds = new TreeSet<>(lfPfMap1.keySet());
        allLfIds.addAll(lfPfMap2.keySet());

        boolean anyDiff = false;
        outer:
        for (Integer lfId : allLfIds) {
            Map<Integer, String> pm1 = toPfNameMap(lfPfMap1.getOrDefault(lfId, Collections.emptyList()));
            Map<Integer, String> pm2 = toPfNameMap(lfPfMap2.getOrDefault(lfId, Collections.emptyList()));
            for (Integer pfId : union(pm1.keySet(), pm2.keySet())) {
                if (!Objects.equals(pm1.get(pfId), pm2.get(pfId))) { anyDiff = true; break outer; }
            }
        }
        if (!anyDiff) return;

        line(w, "Logical File Comparison\n");
        line(w, " -----------------------\n");
        line(w, "  \n");
        line(w, " Compared :  All\n");
        for (Integer lfId : allLfIds) {
            Map<Integer, String> pm1 = toPfNameMap(lfPfMap1.getOrDefault(lfId, Collections.emptyList()));
            Map<Integer, String> pm2 = toPfNameMap(lfPfMap2.getOrDefault(lfId, Collections.emptyList()));
            Set<Integer> pfIds = union(pm1.keySet(), pm2.keySet());
            boolean lfDiff = pfIds.stream().anyMatch(pfId -> !Objects.equals(pm1.get(pfId), pm2.get(pfId)));
            if (!lfDiff) continue;

            String name = lfNameMap1.getOrDefault(lfId, lfNameMap2.getOrDefault(lfId, ""));
            line(w, SEP);
            line(w, "   ID %26d\n", lfId);
            line(w, "   Name %24s\n", name);
            line(w, "   Associated Physical Files\n");
            for (Integer pfId : pfIds) {
                String n1 = pm1.getOrDefault(pfId, "missing"), n2 = pm2.getOrDefault(pfId, "missing");
                if (Objects.equals(n1, n2)) continue;
                line(w, "   ID        %d\n", pfId);
                line(w, " -  VDPNEW     %s\n", n1);
                line(w, " -  VDPOLD     %s\n", n2);
            }
        }
        line(w, "\n");
    }

    private static Map<Integer, String> toPfNameMap(List<PhysicalFile> pfs) {
        Map<Integer, String> m = new TreeMap<>();
        for (PhysicalFile pf : pfs) m.put(pf.getComponentId(), pf.getName());
        return m;
    }

    private static <T extends Comparable<T>> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> s = new TreeSet<>(a); s.addAll(b); return s;
    }

    private void writeLogicalRecordComparison(Writer w) throws IOException {
        if (logicalRecordDiffs.isEmpty()) return;
        line(w, "Logical Record Comparison\n");
        line(w, " -------------------------\n");
        line(w, "  \n");
        line(w, " Compared :  All\n");
        for (LogicalRecordDiff lrd : logicalRecordDiffs.values()) {
            boolean anyDiff = !Objects.equals(lrd.name1, lrd.name2)
                    || !Objects.equals(lrd.lookupExit1, lrd.lookupExit2)
                    || !Objects.equals(lrd.lookupExitParams1, lrd.lookupExitParams2)
                    || !lrd.fields.isEmpty();
            if (!anyDiff) continue;

            line(w, SEP);
            line(w, "   ID %26d\n", lrd.id);
            line(w, "   Name %24s\n", orFirst(lrd.name1, lrd.name2));
            line(w, "   \n");

            writeDiffPair(w, "   Lookup Exit                  \n",
                    lrd.lookupExit1 != null && lrd.lookupExit1 > 0,
                    lrd.lookupExit2 != null && lrd.lookupExit2 > 0,
                    !Objects.equals(lrd.lookupExit1, lrd.lookupExit2));
            writeDiffPair(w, "   Lookup Exit Parms           \n",
                    !lrd.lookupExitParams1.isEmpty(),
                    !lrd.lookupExitParams2.isEmpty(),
                    !Objects.equals(lrd.lookupExitParams1, lrd.lookupExitParams2));

            if (!lrd.fields.isEmpty()) {
                line(w, "   LR Fields\n");
                line(w, "     \n");
                for (LogicalFieldDiff fd : lrd.fields) {
                    if (fd.id <= 0) continue;
                    line(w, "     ID %24d\n", fd.id);
                    if (fd.missing1 || fd.missing2) {
                        line(w, " -    VDPNEW %22s\n", fd.missing1 ? "missing" : "exists");
                        line(w, " -    VDPOLD %22s\n", fd.missing2 ? "missing" : "exists");
                    } else {
                        line(w, "     Name %22s\n", fd.name);
                        for (PropertyDiff pd : fd.props) {
                            line(w, "     %-24s\n", pd.label);
                            line(w, " -    VDPNEW %22s\n", pd.vdp1Value);
                            line(w, " -    VDPOLD %22s\n", pd.vdp2Value);
                        }
                    }
                    line(w, "     \n");
                }
            }
        }
        line(w, "\n");
    }

    private void writeViewLogicComparison(Writer w) throws IOException {
        if (viewLogicDiffs.isEmpty()) return;
        line(w, "View Properties Comparison\n");
        line(w, " --------------------------\n");
        line(w, "  \n");
        line(w, " Compared :  All\n");
        for (ViewLogicDiff vld : viewLogicDiffs.values()) {
            if (Objects.equals(vld.name1, vld.name2) && vld.columnDiffs.isEmpty()
                    && vld.columnLogicDiffs.isEmpty() && vld.sortKeyDiffs.isEmpty()) continue;

            line(w, SEP);
            line(w, "   View ID %26d\n", vld.viewId);
            line(w, "   View Name %19s\n", orFirst(vld.name1, vld.name2));
            if (!Objects.equals(vld.name1, vld.name2)) {
                line(w, " -  VDPNEW %25s\n", orMissing(vld.name1));
                line(w, " -  VDPOLD %25s\n", orMissing(vld.name2));
            }

            if (!vld.columnDiffs.isEmpty()) {
                line(w, "   Column Properties\n");
                for (ColumnDiff cd : vld.columnDiffs) {
                    line(w, "     Column Data\n");
                    line(w, "       Column Number %d\n", cd.columnNumber);
                    line(w, "       Column ID %20d\n", cd.id);
                    line(w, "       Name %24s\n", orMissing(orFirst(cd.name1, cd.name2)));
                    for (PropertyDiff pd : cd.props) {
                        if (isMissing(pd.vdp1Value) && isMissing(pd.vdp2Value)) continue;
                        line(w, "       %-24s\n", pd.label);
                        line(w, " -      VDPNEW %20s\n", orMissing(pd.vdp1Value));
                        line(w, " -      VDPOLD %20s\n", orMissing(pd.vdp2Value));
                    }
                }
            }

            if (!vld.columnLogicDiffs.isEmpty()) {
                line(w, "   Column Logic\n");
                for (ColumnLogicDiff cld : vld.columnLogicDiffs) {
                    line(w, "     Column Number %d\n", cld.columnNumber);
                    line(w, "       Column Source Properties %d\n", cld.sourceNumber);
                    line(w, "         Column Logic Text\n");
                    line(w, " -        VDPNEW                 \n");
                    writeLogicText(w, cld.logic1);
                    line(w, " -        VDPOLD                 \n");
                    writeLogicText(w, cld.logic2);
                }
            }

            if (!vld.sortKeyDiffs.isEmpty()) {
                line(w, "   Sort Key Properties\n");
                for (SortKeyDiff skd : vld.sortKeyDiffs) {
                    line(w, "       Column ID %20d\n", skd.columnId);
                    for (PropertyDiff pd : skd.props) {
                        line(w, "       %-24s\n", pd.label);
                        line(w, " -      VDPNEW %20s\n", orMissing(pd.vdp1Value));
                        line(w, " -      VDPOLD %20s\n", orMissing(pd.vdp2Value));
                    }
                }
            }
        }
        line(w, SEP);
        line(w, "\n");
    }

    private void writeLookupPathComparison(Writer w) throws IOException {
        if (lookupDiffs.isEmpty()) return;
        line(w, "Lookup Path Comparison\n");
        line(w, " ----------------------\n");
        line(w, "  \n");
        line(w, " Compared :  All\n");
        for (LookupDiff ld : lookupDiffs.values()) {
            if (Objects.equals(ld.name1, ld.name2) && Objects.equals(ld.mappedTo1, ld.mappedTo2) && ld.steps.isEmpty()) {
                continue;
            }
            String m1 = formatMappedTo(ld.mappedTo1);
            String m2 = formatMappedTo(ld.mappedTo2);

            writeLookupHeader(w, ld, m1, m2, false);
            line(w, "   Lookup Steps\n");
            for (LookupStepDiff lsd : ld.steps) {
                writeLookupStepDiff(w, ld, lsd, m1, m2);
            }
        }
        line(w, "\n");
    }

    private void writeLookupHeader(Writer w, LookupDiff ld, String m1, String m2, boolean forceDiffLines) throws IOException {
        line(w, SEP);
        line(w, "   ID %26d\n", ld.id);
        line(w, "   Name %24s\n", orFirst(ld.name1, ld.name2));
        line(w, "   Mapped To %20s\n", m1);
        if (forceDiffLines || !Objects.equals(ld.name1, ld.name2) || !Objects.equals(ld.mappedTo1, ld.mappedTo2)) {
            line(w, " -  VDPNEW %25s\n", orMissing(m1));
            line(w, " -  VDPOLD %25s\n", orMissing(m2));
        }
    }

    private void writeLookupStepDiff(Writer w, LookupDiff ld, LookupStepDiff lsd, String m1, String m2) throws IOException {
        writeLookupHeader(w, ld, m1, m2, true);
        line(w, "   Step Number %d\n", lsd.stepNumber);
        line(w, "     Source Field Properties\n");
        for (KeyPropDiff kpd : lsd.keyProps) {
            line(w, "       Source Field Seq Num %d\n", kpd.seqNum);
            line(w, "         %-25s\n", kpd.propLabel);
            line(w, " -        VDPNEW                 %s\n", orMissing(kpd.vdp1Value));
            line(w, " -        VDPOLD                 %s\n", orMissing(kpd.vdp2Value));
        }
    }

    private static String formatMappedTo(Integer mappedTo) {
        return mappedTo != null && mappedTo > 0 ? String.valueOf(mappedTo) : "";
    }

    private void writeExtractPhaseDDList(Writer w) throws IOException {
        boolean inputDiff  = hasDDDiff(extractInputDDs1,  extractInputDDs2);
        boolean outputDiff = hasDDDiff(extractOutputDDs1, extractOutputDDs2);
        if (!inputDiff && !outputDiff) return;

        String sep = "~---------------------------------------------------------------------\n";
        line(w, sep);
        line(w, " Extract-phase DD statement list\n");
        line(w, sep);
        if (inputDiff)  writeDDSection(w, " Input DD  \n",  extractInputDDs1,  extractInputDDs2);
        line(w, sep);
        if (outputDiff) writeDDSection(w, " Output DD \n", extractOutputDDs1, extractOutputDDs2);
        line(w, "\n");
    }

    private static boolean hasDDDiff(Set<String> s1, Set<String> s2) {
        Set<String> union = new TreeSet<>(s1); union.addAll(s2);
        return union.stream().anyMatch(dd -> s1.contains(dd) != s2.contains(dd));
    }

    private static void writeDDSection(Writer w, String header, Set<String> s1, Set<String> s2) throws IOException {
        line(w, header);
        Set<String> union = new TreeSet<>(s1); union.addAll(s2);
        for (String dd : union) {
            String st1 = s1.contains(dd) ? "exists" : "missing";
            String st2 = s2.contains(dd) ? "exists" : "missing";
            if (st1.equals(st2)) continue;
            line(w, "     %-25s\n", dd);
            line(w, " -    VDPNEW %25s\n", st1);
            line(w, " -    VDPOLD %25s\n", st2);
        }
    }

    private void writeFormatPhaseDDList(Writer w) throws IOException {
        Set<String> union = new TreeSet<>(formatPhaseDDs1);
        union.addAll(formatPhaseDDs2);
        line(w, "Format-phase DD statement list (%02d)\n\n", union.size());
        line(w, " \n");
    }

    private void writeDifferences(Writer w) throws IOException {
        List<ComponentDifference> sorted = differences().stream()
                .filter(d -> !USER_EXIT_ROUTINE_TYPE.equals(d.componentType))
                .sorted()
                .collect(Collectors.toList());
        if (sorted.isEmpty()) {
            if (differencesMap.isEmpty()) line(w, " No differences found.\n");
            return;
        }
        line(w, " -------------------------------------------------------------------------------\n");
        line(w, " Component Type            ID  VDPNEW                VDPOLD\n");
        line(w, " -------------------------------------------------------------------------------\n");
        for (ComponentDifference d : sorted)
            line(w, " %-20s %7d  %-19s %-19s\n", d.componentType, d.id, d.vdp1Status, d.vdp2Status);
        line(w, "  \n");
    }

    private void writeFooter(Writer w) throws IOException {
        line(w, " Total Number of Differences: %d\n", computeTotalDifferences());
    }

    private int computeTotalDifferences() {
        int count = 0;

        // 1. User Exit differences
        count += (int) differences().stream()
                .filter(d -> USER_EXIT_ROUTINE_TYPE.equals(d.componentType))
                .count();

        // 2. Logical File differences (associated Physical Files mismatches)
        Set<Integer> allLfIds = new TreeSet<>(lfPfMap1.keySet());
        allLfIds.addAll(lfPfMap2.keySet());
        for (Integer lfId : allLfIds) {
            Map<Integer, String> pm1 = toPfNameMap(lfPfMap1.getOrDefault(lfId, Collections.emptyList()));
            Map<Integer, String> pm2 = toPfNameMap(lfPfMap2.getOrDefault(lfId, Collections.emptyList()));
            for (Integer pfId : union(pm1.keySet(), pm2.keySet())) {
                String n1 = pm1.getOrDefault(pfId, "missing"), n2 = pm2.getOrDefault(pfId, "missing");
                if (!Objects.equals(n1, n2)) {
                    count++;
                }
            }
        }

        // 3. Logical Record differences
        for (LogicalRecordDiff lrd : logicalRecordDiffs.values()) {
            if (lrd.lookupExit1 != null && lrd.lookupExit2 != null && !Objects.equals(lrd.lookupExit1, lrd.lookupExit2)) {
                count++;
            } else if ((lrd.lookupExit1 != null && lrd.lookupExit1 > 0) != (lrd.lookupExit2 != null && lrd.lookupExit2 > 0)) {
                count++;
            }
            if (!Objects.equals(lrd.lookupExitParams1, lrd.lookupExitParams2)
                    && (!lrd.lookupExitParams1.isEmpty() || !lrd.lookupExitParams2.isEmpty())) {
                count++;
            }
            for (LogicalFieldDiff fd : lrd.fields) {
                if (fd.id <= 0) continue;
                if (fd.missing1 || fd.missing2) {
                    count++;
                } else {
                    count += fd.props.size();
                }
            }
        }

        // 4. View Properties differences
        for (ViewLogicDiff vld : viewLogicDiffs.values()) {
            if (!Objects.equals(vld.name1, vld.name2)) {
                count++;
            }
            for (ColumnDiff cd : vld.columnDiffs) {
                for (PropertyDiff pd : cd.props) {
                    if (!isMissing(pd.vdp1Value) || !isMissing(pd.vdp2Value)) {
                        count++;
                    }
                }
            }
            count += vld.columnLogicDiffs.size();
            for (SortKeyDiff skd : vld.sortKeyDiffs) {
                for (PropertyDiff pd : skd.props) {
                    if (!isMissing(pd.vdp1Value) || !isMissing(pd.vdp2Value)) {
                        count++;
                    }
                }
            }
        }

        // 5. Lookup Path differences
        for (LookupDiff ld : lookupDiffs.values()) {
            if (!Objects.equals(ld.name1, ld.name2) || !Objects.equals(ld.mappedTo1, ld.mappedTo2)) {
                count++;
            }
            for (LookupStepDiff lsd : ld.steps) {
                count += lsd.keyProps.size();
            }
        }

        // 6. Extract Phase DD differences
        Set<String> allInputDDs = new TreeSet<>(extractInputDDs1);
        allInputDDs.addAll(extractInputDDs2);
        for (String dd : allInputDDs) {
            if (extractInputDDs1.contains(dd) != extractInputDDs2.contains(dd)) {
                count++;
            }
        }
        Set<String> allOutputDDs = new TreeSet<>(extractOutputDDs1);
        allOutputDDs.addAll(extractOutputDDs2);
        for (String dd : allOutputDDs) {
            if (extractOutputDDs1.contains(dd) != extractOutputDDs2.contains(dd)) {
                count++;
            }
        }

        return count;
    }

    // -------------------------------------------------------------------------
    // Small utilities
    // -------------------------------------------------------------------------

    private List<ComponentDifference> differences() {
        return new ArrayList<>(differencesMap.values());
    }

    private static void writeDiffPair(Writer w, String label, boolean p1, boolean p2, boolean mismatch) throws IOException {
        if (!p1 && !p2) return;
        line(w, label);
        line(w, " -  VDPNEW %25s\n", describeStatus(p1, p2, mismatch));
        line(w, " -  VDPOLD %25s\n", describeStatus(p2, p1, mismatch));
    }

    private static String describeStatus(boolean present1, boolean present2, boolean mismatch) {
        if (!present1 && !present2) return "missing";
        if (present1 && present2)   return mismatch ? "does not match" : "exists";
        return present1 ? "exists" : "missing";
    }

    private static String orFirst(String a, String b) {
        return a != null && !a.isEmpty() ? a : (b != null ? b : "");
    }

    private static String orMissing(String v) {
        return v != null && !v.isEmpty() ? v : "missing";
    }

    private static boolean isMissing(String v) {
        return v == null || v.isEmpty() || "missing".equalsIgnoreCase(v);
    }

    private static void writeLogicText(Writer w, String text) throws IOException {
        if (text == null || text.isEmpty()) { line(w, "           missing\n"); return; }
        w.write(text);
        if (!text.endsWith("\n")) w.write("\n");
    }
}
