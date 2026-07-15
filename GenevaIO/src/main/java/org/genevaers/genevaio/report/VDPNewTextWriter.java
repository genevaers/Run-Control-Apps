package org.genevaers.genevaio.report;

import java.io.FileWriter;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.genevaers.genevaio.fieldnodes.ComparisonState;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase;
import org.genevaers.genevaio.fieldnodes.FieldNodeBase.FieldNodeType;
import org.genevaers.genevaio.fieldnodes.MetadataNode;
import org.genevaers.genevaio.fieldnodes.NumericFieldNode;
import org.genevaers.genevaio.fieldnodes.StringFieldNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ControlRecord;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.enums.ExtractArea;
import com.google.common.flogger.FluentLogger;

public class VDPNewTextWriter extends TextRecordWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private boolean compareMode;
    private Map<Integer, ViewDetails> viewDetailsById = new TreeMap<>();

    public VDPNewTextWriter() {
        setIgnores();
    }

    @Override
    public void setIgnores() {
        // Keep all differences visible in the alternate output.
    }

    @Override
    protected String getDiffKey(FieldNodeBase n) {
        return n.getParent().getParent().getName() + "_" + n.getName();
    }

    @Override
    public void writeDetails(MetadataNode recordsRoot, Writer fw, String generated) throws IOException {
        compareMode = recordsRoot.getName().equals("Compare");
        writeHeader(generated, fw);
        writeComponentCounts(fw);
        writeComponentDetails(fw);
        if (compareMode) {
            writeComparisonSummary(recordsRoot, fw);
        }
    }

    private void writeHeader(String generated, Writer fw) throws IOException {
        String title = "VDP Record Summary Report";
        fw.write(title + "\n");
        fw.write(StringUtils.repeat('-', title.length()) + "\n\n");
        fw.write(String.format(" VDP Run Date           %s\n\n", generated));
        fw.write(StringUtils.repeat('-', 79) + "\n");
    }

    private void writeComponentCounts(Writer fw) throws IOException {
        fw.write(" Component Type              Count\n");
        fw.write(StringUtils.repeat('-', 79) + "\n");
        fw.write(String.format(" %-24s %7d\n", "User Exit Routines", Repository.getUserExits().size()));
        fw.write(String.format(" %-24s %7d\n", "Control Records", Repository.getControlRecords().size()));
        fw.write(String.format(" %-24s %7d\n", "Physical Files", Repository.getPhysicalFiles().size()));
        fw.write(String.format(" %-24s %7d\n", "Logical Files", Repository.getLogicalFiles().size()));
        fw.write(String.format(" %-24s %7d\n", "Logical Records", Repository.getLogicalRecords().size()));
        fw.write(String.format(" %-24s %7d\n", "Lookup Paths", Repository.getLookups().size()));
        fw.write(String.format(" %-24s %7d\n\n\n", "Views", Repository.getViews().size()));
    }

    private void writeComponentDetails(Writer fw) throws IOException {
        fw.write(" Component Type                ID  Name\n");
        fw.write(StringUtils.repeat('-', 79) + "\n");
        writeUserExitDetails(fw);
        writeControlRecordDetails(fw);      
        writePhysicalFileDetails(fw);       
        writeLogicalFileDetails(fw);       
        writeLogicalRecordDetails(fw);      
        writeLookUpPathDetails(fw);
        writeViewDetails(fw);
        writeControlRecordReport(fw);
        writePhysicalFileReport(fw);
        writeLogicalFileReport(fw);
        writeLogicalRecordReport(fw);
        writeLookUpPathReport(fw);
        writeViewPropertiesReport(fw);
    }

    private void writeViewPropertiesReport(Writer fw) throws IOException {
        fw.write(String.format("View Properties Report \n"));
        Iterator<ViewNode> viewIterator = Repository.getViews().getIterator();
        while (viewIterator.hasNext()) {
            ViewNode view = viewIterator.next();
            ViewDefinition viewDef = view.getViewDefinition();
            writeRecord(fw, 3, "View ID", view.getID());
            writeRecord(fw, 3, "View Name", view.getName());
            writeRecord(fw, 3, "Status", view.getStatus());
            writeRecord(fw, 3, "View Phase", viewDef.getViewType().name());
            writeRecord(fw, 3, "Output Format", "TO DO");
            writeRecord(fw, 3, "View Aggregation Level", viewDef.isDetailed() ? "Detail" : "Summary");
            writeRecord(fw, 3, "Lines Per Page", viewDef.getOutputPageSizeMax());
            writeRecord(fw, 3, "Report Width", viewDef.getOutputLineSizeMax());
            writeRecord(fw, 3, "View folder ID", "TO DO");
            writeRecord(fw, 3, "Control Record", Repository.getControlRecords().get(viewDef.getControlRecordId()));
            writeRecord(fw, 3, "Extract Phase", "");
            writeRecord(fw, 5, "Output Logical File", viewDef.getOutputLrId() > 0 ? Repository.getLogicalRecords().get(viewDef.getOutputLrId()).getName() : "");
            writeRecord(fw, 5, "Output File Destination", viewDef.getOutputDestinationId() > 0 ? Repository.getPhysicalFiles().get(viewDef.getOutputDestinationId()).getName() : "");
            writeRecord(fw, 5, "User Exit Name", viewDef.getWriteExitId() > 0 ? Repository.getUserExits().get(viewDef.getWriteExitId()).getName() : "");
            writeRecord(fw, 5, "User Exit Parameters", viewDef.getWriteExitParams());
            

            String outputName = "Auto-generated Name for Extract Phase Output";
            if (view.getOutputFile() != null && view.getOutputFile().getName() != null && !view.getOutputFile().getName().isEmpty()) {
                outputName = view.getOutputFile().getName();
            }
            fw.write(String.format("   Extract to %s\n", outputName));
            fw.write("   Src Num        LR        LF\n");
            Iterator<ViewSource> vsi = view.getViewSourceIterator();
            while (vsi.hasNext()) {
                ViewSource vs = vsi.next();
                fw.write(String.format("         %3d %8d %8d\n",
                        vs.getSequenceNumber(), vs.getSourceLRID(), vs.getSourceLFID()));
            }
            fw.write(String.format("   Number of columns:   %7d\n", view.getNumberOfColumns()));
            fw.write(String.format("   Extract Length:      %7d\n", calculateExtractLength(view)));
            fw.write(String.format("   Number of sort keys: %7d\n", view.getNumberOfSortKeys()));
            fw.write("\n");
        }
    }

    private void writeUserExitDetails(Writer fw) throws IOException {
        Iterator<UserExit> exitIterator = Repository.getUserExits().getIterator();
        while (exitIterator.hasNext()) {
            UserExit ue = exitIterator.next();
            fw.write(String.format(" %-24s %7d  %s\n", "User Exit Routine", ue.getComponentId(), ue.getName()));
        }
        fw.write("\n");
    }

    private void writeControlRecordDetails(Writer fw) throws IOException {
        Iterator<ControlRecord> crIterator = Repository.getControlRecords().getIterator();
        while (crIterator.hasNext()) {
            ControlRecord cr = crIterator.next();
            fw.write(String.format(" %-24s %7d  %s\n", "Control Record", cr.getComponentId(), cr.getName()));
        }
        fw.write("\n");
    }

    private void writePhysicalFileDetails(Writer fw) throws IOException {
        Iterator<PhysicalFile> pfi = Repository.getPhysicalFiles().getIterator();
        while (pfi.hasNext()) {
            PhysicalFile pf = pfi.next();
            fw.write(String.format(" %-24s %7d  %s\n", "Physical File", pf.getComponentId(), pf.getName()));
        }
        fw.write("\n");
    }

    private void writePhysicalFileReport(Writer fw) throws IOException {
        Iterator<PhysicalFile> pfi = Repository.getPhysicalFiles().getIterator();
        if (!pfi.hasNext()) {
            fw.write("Physical File Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            fw.write("   <none>\n\n");
            writeKV(fw, 1, "Total Number of Physical File records:", Repository.getPhysicalFiles().size());
            fw.write("\n");
            return;
        }

        while (pfi.hasNext()) {
            PhysicalFile pf = pfi.next();
            fw.write("Physical File Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            writeKV(fw, 3, "ID", pf.getComponentId());
            writeKV(fw, 3, "Name", pf.getName());
            writeKV(fw, 3, "File Type", pf.getFileType());
            writeKV(fw, 3, "Access Method", pf.getAccessMethod());
            fw.write("   Dataset Input Attributes\n");
            writeKV(fw, 5, "Input DD Name", pf.getInputDDName());
            writeKV(fw, 5, "DSN", pf.getDataSetName());
            writeKV(fw, 5, "PF_RD_DISP", "");
            writeKV(fw, 5, "Record Length", pf.getMinimumLength());
            writeKV(fw, 5, "Max Record Length", pf.getMaximumLength());
            fw.write("   Dataset Output Attributes\n");
            writeKV(fw, 5, "Output DD Name", pf.getOutputDDName());
            writeKV(fw, 5, "Device Type", "");
            writeKV(fw, 5, "RECFM", pf.getRecfm());
            writeKV(fw, 5, "LRECL", pf.getLrecl());
            fw.write("\n");
        }
        writeKV(fw, 1, "Total Number of Physical File records:", Repository.getPhysicalFiles().size());
        fw.write("\n");
    }

    private void writeLogicalFileDetails(Writer fw) throws IOException {
        Iterator<org.genevaers.repository.components.LogicalFile> lfi = Repository.getLogicalFiles().getIterator();
        while (lfi.hasNext()) {
            org.genevaers.repository.components.LogicalFile lf = lfi.next();
            fw.write(String.format(" %-24s %7d  %s\n", "Logical File", lf.getID(), lf.getName()));
            fw.write("   PF ID           PF Name\n");
            Iterator<PhysicalFile> pfIterator = lf.getPFIterator();
            while (pfIterator.hasNext()) {
                PhysicalFile pf = pfIterator.next();
                fw.write(String.format("   %-14d %s\n", pf.getComponentId(), pf.getName()));
            }
            fw.write("\n");
        }
    }

    private void writeLogicalRecordDetails(Writer fw) throws IOException {
        Iterator<org.genevaers.repository.components.LogicalRecord> lri = Repository.getLogicalRecords().getIterator();
        while (lri.hasNext()) {
            org.genevaers.repository.components.LogicalRecord lr = lri.next();
            fw.write(String.format(" %-24s %7d  %s\n", "Logical Record", lr.getComponentId(), lr.getName()));
            fw.write("   Field ID        Field Name                 Data Type\n");
            Iterator<org.genevaers.repository.components.LRField> fieldIterator = lr.getIteratorForFieldsByID();
            while (fieldIterator.hasNext()) {
                org.genevaers.repository.components.LRField field = fieldIterator.next();
                fw.write(String.format("   %-14d %-28s %s\n", field.getComponentId(), field.getName(), field.getDatatype().dbcode()));
            }
            fw.write("\n");
        }
    }

    private void writeLookUpPathDetails(Writer fw) throws IOException{
        Iterator<LookupPath> lpi = Repository.getLookups().getIterator();
        while (lpi.hasNext()) {
            LookupPath lp = lpi.next();
            fw.write(String.format(" %-24s %7d  %s\n", "Lookup Path", lp.getID(), lp.getName()));
            fw.write("   Step   Seq Num    Src LR    Field ID    Trg LR    Trg File\n");
            fw.write(StringUtils.repeat('-', 86) + "\n");
            Iterator<LookupPathStep> stepIterator = lp.getStepIterator();
            int i=1;
            while (stepIterator.hasNext()) {
                LookupPathStep step = stepIterator.next();
                fw.write(String.format("   %-7d %7d %7d %7d %7d %s\n", step.getStepNum(), i++, step.getSourceLR(), 0, step.getTargetLR(), step.getTargetLF()));
            }
            fw.write("\n");
        }
    }

    private void writeViewDetails(Writer fw) throws IOException {
        Iterator<ViewNode> viewIterator = Repository.getViews().getIterator();
        while (viewIterator.hasNext()) {
            ViewNode view = viewIterator.next();
            fw.write(String.format(" %-24s %7d  %s\n", "View", view.getID(), view.getName()));
            String outputName = "Auto-generated Name for Extract Phase Output";
            if (view.getOutputFile() != null && view.getOutputFile().getName() != null && !view.getOutputFile().getName().isEmpty()) {
                outputName = view.getOutputFile().getName();
            }
            fw.write(String.format("   Extract to %s\n", outputName));
            fw.write("   Src Num        LR        LF\n");
            Iterator<ViewSource> vsi = view.getViewSourceIterator();
            while (vsi.hasNext()) {
                ViewSource vs = vsi.next();
                fw.write(String.format("         %3d %8d %8d\n",
                        vs.getSequenceNumber(), vs.getSourceLRID(), vs.getSourceLFID()));
            }
            fw.write(String.format("   Number of columns:   %7d\n", view.getNumberOfColumns()));
            fw.write(String.format("   Extract Length:      %7d\n", calculateExtractLength(view)));
            fw.write(String.format("   Number of sort keys: %7d\n", view.getNumberOfSortKeys()));
            fw.write("\n");
        }
    }

    private void writeControlRecordReport(Writer fw) throws IOException {
        Iterator<ControlRecord> crIterator = Repository.getControlRecords().getIterator();
        if (!crIterator.hasNext()) {
            // No control records
            fw.write("Control Record Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            fw.write("   <none>\n\n");
            fw.write(String.format(" Total Number of Control Record records: %d\n\n", Repository.getControlRecords().size()));
            return;
        }
        fw.write("Control Record Report\n");
        fw.write(" *---------------------------------------------------------------------\n");
        while (crIterator.hasNext()) {
            ControlRecord cr = crIterator.next();
            writeKV(fw, 3, "ID", cr.getComponentId());
            writeKV(fw, 3, "Name", cr.getName());
            writeKV(fw, 3, "First Fiscal Month", cr.getFirstFiscalMonth());
            writeKV(fw, 3, "Beginning Period", cr.getBeginningPeriod());
            writeKV(fw, 3, "Ending Period", cr.getEndingPeriod());
            writeKV(fw, 3, "Max Extr file Num", 0);
            fw.write("\n");
        }
        writeKV(fw, 1, "Total Number of Control Record records:", Repository.getControlRecords().size());
        fw.write("\n");
    }

    private int calculateExtractLength(ViewNode view) {
        int length = 0;
        Iterator<ViewColumn> columns = view.getColumnIterator();
        while (columns.hasNext()) {
            ViewColumn col = columns.next();
            if (col.getExtractArea() == ExtractArea.AREADATA) {
                length += col.getFieldLength();
            }
        }
        return length;
    }

    private void writeLogicalFileReport(Writer fw) throws IOException {
        Iterator<org.genevaers.repository.components.LogicalFile> lfi = Repository.getLogicalFiles().getIterator();
        if (!lfi.hasNext()) {
            fw.write("Logical File Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            fw.write("   <none>\n\n");
            writeKV(fw, 1, "Total Number of Logical File records:", Repository.getLogicalFiles().size());
            fw.write("\n");
            return;
        }

        while (lfi.hasNext()) {
            org.genevaers.repository.components.LogicalFile lf = lfi.next();
            fw.write("Logical File Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            writeKV(fw, 3, "ID", lf.getID());
            writeKV(fw, 3, "Name", lf.getName());
            fw.write("   Associated Physical Files\n");
            Iterator<PhysicalFile> pfIterator = lf.getPFIterator();
            while (pfIterator.hasNext()) {
                PhysicalFile pf = pfIterator.next();
                writeKV(fw, 5, "ID", pf.getComponentId());
                writeKV(fw, 5, "Name", pf.getName());
                fw.write("\n");
            }
        }
        writeKV(fw, 1, "Total Number of Logical File records:", Repository.getLogicalFiles().size());
        fw.write("\n");
    }

    private void writeLogicalRecordReport(Writer fw) throws IOException {
        Iterator<org.genevaers.repository.components.LogicalRecord> lri = Repository.getLogicalRecords().getIterator();
        if (!lri.hasNext()) {
            fw.write("Logical Record Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            fw.write("   <none>\n\n");
            writeKV(fw, 1, "Total Number of Logical Record records:", Repository.getLogicalRecords().size());
            fw.write("\n");
            return;
        }

        while (lri.hasNext()) {
            org.genevaers.repository.components.LogicalRecord lr = lri.next();
            fw.write("Logical Record Report\n");
            fw.write(" *---------------------------------------------------------------------\n");
            writeKV(fw, 3, "ID", lr.getComponentId());
            writeKV(fw, 3, "Name", lr.getName());
            writeKV(fw, 3, "Status", lr.getStatus().value());
            writeKV(fw, 3, "Lookup Exit", lr.getLookupExitID());
            writeKV(fw, 3, "Lookup Exit Params", lr.getLookupExitParams());
            fw.write("   LR Fields\n");
            Iterator<org.genevaers.repository.components.LRField> fieldIterator = lr.getIteratorForFieldsByID();
            while (fieldIterator.hasNext()) {
                org.genevaers.repository.components.LRField field = fieldIterator.next();
                writeKV(fw, 5, "ID", field.getComponentId());
                writeKV(fw, 5, "Name", field.getName());
                writeKV(fw, 5, "Data Type", field.getDatatype().dbcode());
                writeKV(fw, 5, "Fixed Position", field.getStartPosition());
                writeKV(fw, 5, "Length", field.getLength());
                writeKV(fw, 5, "Decimal Places", field.getNumDecimalPlaces());
                writeKV(fw, 5, "Primary Key Sequence #", "");
                writeKV(fw, 5, "Effective Date", "");
                writeKV(fw, 5, "Ordinal Position", field.getOrdinalPosition());
                writeKV(fw, 5, "Ordinal Offset", field.getOrdinalOffset());
                writeKV(fw, 5, "Scaling", field.getRounding());
                writeKV(fw, 5, "Date/Time Format", field.getDateTimeFormat());
                writeKV(fw, 5, "Align Heading", field.getJustification());
                writeKV(fw, 5, "Numeric Mask", field.getMask());
                writeKV(fw, 5, "DBMS ColName", field.getDbColName());
                fw.write("\n");
            }
        }
        writeKV(fw, 1, "Total Number of Logical Record records:", Repository.getLogicalRecords().size());
        fw.write("\n");
    }

    private void writeLookUpPathReport(Writer fw) throws IOException {
        fw.write("Lookup Path Report\n");
        Iterator<LookupPath> lpi = Repository.getLookups().getIterator();
        while (lpi.hasNext()) {
            LookupPath lp = lpi.next();
            writeRecord(fw, 1, " *---------------------------------------------------------------------", "");
            writeRecord(fw, 3, "ID", lp.getID());
            writeRecord(fw, 3, "Name", lp.getName());
            writeRecord(fw, 3, "Lookup Steps", lp.getNumberOfSteps());
            Iterator<LookupPathStep> lps = lp.getStepIterator();
            while (lps.hasNext()) {
                LookupPathStep step = lps.next();
                LogicalRecord sourceLR = Repository.getLogicalRecords().get(step.getSourceLR());
                LogicalRecord targetLR = Repository.getLogicalRecords().get(step.getTargetLR());
                LogicalFile targetLF = Repository.getLogicalFiles().get(step.getTargetLF());
                fw.write(String.format("   Step Number %d%n", step.getStepNum()));
                writeRecord(fw, 5, "Source Logical Record", sourceLR.getName());
                writeRecord(fw, 5, "Target Logical Record", targetLR.getName());
                writeRecord(fw, 5, "Target Logical File", targetLF.getName())         ;
                writeRecord(fw, 5, "Source Field Properties", "");
                Iterator<org.genevaers.repository.components.LookupPathKey> lpKeyIterator = step.getKeyIterator();
                while (lpKeyIterator.hasNext()) {
                org.genevaers.repository.components.LookupPathKey key = lpKeyIterator.next();
                fw.write(String.format("       Source Field Seq Num %d%n", key.getKeyNumber()));
                int length = key.getFieldLength();
                if (key.getFieldId() > 0) {
                    writeRecord(fw, 9, "Source Type", "LR Field");
                    writeRecord(fw, 9, "LR Field", key.getFieldId());
                    writeRecord(fw, 9, "LR", sourceLR.getName());
                } else if (key.getSymbolicName() != null && !key.getSymbolicName().isEmpty()) {
                    writeRecord(fw, 9, "Source Type", "Symbol");
                    writeRecord(fw, 9, "Symbol", key.getSymbolicName());
                    writeRecord(fw, 9, "Default Symbol", key.getValue());
                } else {
                    writeRecord(fw, 9, "Source Type", "Constant");
                    writeRecord(fw, 9, "Constant", key.getValue());
                    length = key.getValueLength();
                }
                writeRecord(fw, 9, "Data Attributes", "");
                writeRecord(fw, 11     , "Data Type", key.getDatatype().dbcode());
                writeRecord(fw, 11, "Length", length);
                writeRecord(fw, 11, "Scaling Factor", key.getRounding());
                writeRecord(fw, 11, "Date/Time Format", key.getDateTimeFormat());
                writeRecord(fw, 11, "Decimal Places", key.getDecimalCount());
                writeRecord(fw, 11, "Signed", key.isSigned() ? "Y" : "N");
            }
                
            }

            fw.write("\n");
        }
        fw.write(String.format(" Total Number of Lookup Path records: %d\n\n", Repository.getLookups().size()));
    }

    private static void writeRecord(Writer fw, int indent, String label, Object value)
        throws IOException {
        String prefix = " ".repeat(indent);
        int valueColumn = 35;
        fw.write(prefix);
        fw.write(String.format("%-" + (valueColumn - indent) + "s%s%n",
                label, value));
    }

    private static void writeKV(Writer fw, int indent, String key, Object value) throws IOException {
        writeRecord(fw, indent, key, value);
    }

    private void writeComparisonSummary(MetadataNode recordsRoot, Writer fw) throws IOException {
        fw.write("Comparison Summary\n");
        fw.write("==================\n");
        fw.write(String.format("Source1: %s\n", recordsRoot.getSource1()));
        fw.write(String.format("Source2: %s\n", recordsRoot.getSource2()));
        fw.write("\n");
    }
}