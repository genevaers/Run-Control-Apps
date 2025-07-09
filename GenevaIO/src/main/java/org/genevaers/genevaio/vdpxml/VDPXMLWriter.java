package org.genevaers.genevaio.vdpxml;

import java.io.IOException;
import java.io.Writer;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.text.StringEscapeUtils;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ControlRecord;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalFile;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.ReportHeader;
import org.genevaers.repository.components.UserExit;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSortKey;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.PerformBreakLogic;
import org.genevaers.repository.components.enums.SortBreakFooterOption;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.GersFile;
import org.genevaers.utilities.GersFilesUtils;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.StackSize;

public class VDPXMLWriter {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private ViewNode currentView;

    public void writeFromRepository(String filename) {
        logger.atInfo().log("Write VDPXML to %s", filename);
        try (Writer fw = new GersFile().getWriter(filename)) {
            write(fw);
        } catch (IOException e) {
            logger.atSevere().withCause(e).withStackTrace(StackSize.FULL);
        }
        logger.atInfo().log("VDPXML written to %s", filename);
    }

    private void write(Writer fw) throws IOException {
        writeHeader(fw);
        writeJobInfo(fw);
        writeEnvironment(fw);
        writeMetadata(fw);
        writeViews(fw);
        closeHeader(fw);
    }

    private void closeHeader(Writer fw) throws IOException {
        fw.write("    </Environment>\n");
        fw.write("</SAFRVDP>\n");
    }

    private void writeViews(Writer fw) throws IOException {
        openElement("Views", fw);
        fw.write("\n");
        Iterator<ViewNode> vi = Repository.getViews().getIterator();
        while (vi.hasNext()) {
            writeView(vi.next(), fw);
        }
        closeElement("Views", fw);
        fw.write("\n");
    }

    private void writeView(ViewNode vw, Writer fw) throws IOException {
        if (vw.getID() < 9000000) {
            currentView = vw;
            writeIdElement("View", vw.getID(), fw);
            writeElement("Name", vw.getName(), fw);
            writeElement("Status", vw.getViewDefinition().getStatus().dbcode(), fw);
            writeElement("Type", vw.getViewDefinition().getViewType().dbcode(), fw);
            writeFolderRefs(fw);
            writeInput(vw, fw);
            writeExtract(vw, fw);
            writeSort(vw, fw);
            writeOutput(vw, fw);
            closeElement("View", fw);
            fw.write("\n");
        }
    }

    private void writeSort(ViewNode vw, Writer fw) throws IOException {
        openElement("Sort", fw);
        fw.write("\n");
        openElement("SortColumns", fw);
        fw.write("\n");
        Iterator<ViewSortKey> ski = vw.getSortKeyIterator();
        while (ski.hasNext()) {
            writeSortKey(ski.next(), fw);
        }
        closeElement("SortColumns", fw);
        fw.write("\n");
        closeElement("Sort", fw);
        fw.write("\n");
    }

    private void writeSortKey(ViewSortKey sk, Writer fw) throws IOException {
        writeSeqIdElement("SortColumn", sk.getComponentId(), sk.getSequenceNumber(), fw);
        writeClosedAttributedElement("ColumnRef", "ID", Integer.toString(sk.getColumnId()), fw);
        writeElement("Order", sk.getSortorder().dbcode(), fw);
        writeBreakDisplay(sk, fw);
        closeElement("SortColumn", fw);
        fw.write("\n");
    }

    private void writeBreakDisplay(ViewSortKey sk, Writer fw) throws IOException {
        openElement("BreakDisplay", fw);
        fw.write("\n");
        writeElement("Break", sk.getPerformBreakLogic() == PerformBreakLogic.BREAK ? "1" : "0", fw);
        writeElement("Footer", sk.getSortBreakFooterOption() == SortBreakFooterOption.PRINT ? "1" : "0", fw);
        writeElement("Header", sk.getSortBreakHeaderOption().dbcode(), fw);
        writeElement("Prefix", sk.getLabel(), fw);
        writeElement("Hardcopy", sk.getSortDisplay().dbcode(), fw);
        closeElement("BreakDisplay", fw);
        fw.write("\n");
    }

    private void writeOutput(ViewNode vw, Writer fw) throws IOException {
        openElement("Output", fw);
        fw.write("\n");
        writeElement("Media", vw.getViewDefinition().getOutputMedia().dbcode(), fw);
        writeOutputFileDetails(vw, fw);
        writeExitRefs(vw, fw);
        writePageLayout(vw, fw);
        closeElement("Output", fw);
        fw.write("\n");
    }

    private void writePageLayout(ViewNode vw, Writer fw) throws IOException {
        ViewDefinition vd = vw.getViewDefinition();
        openElement("PageLayout", fw);
        fw.write("\n");
        writeElement("LinesPerPage", Integer.toString(vd.getOutputPageSizeMax()), fw);
        writeElement("MaxCharsPerLine", Integer.toString(vd.getOutputLineSizeMax()), fw);
        writeElement("MaxColumnHeaderLines", Integer.toString(vd.getOutputColHdrLnsMax()), fw);
        writeElement("ErrorText", "****************", fw);
        writeElement("TruncationText", "################", fw);
        writeHeadersAndFooters(vw, fw);
        writeFormatColumns(vw, fw);
        closeElement("PageLayout", fw);
        fw.write("\n");
    }

    private void writeFormatColumns(ViewNode vw, Writer fw) throws IOException {
        openElement("FormatColumns", fw);
        fw.write("\n");
        Iterator<ViewColumn> ci = vw.getColumnIterator();
        while (ci.hasNext()) {
            writeFormatColumn(ci.next(), fw);
        }
        closeElement("FormatColumns", fw);
        fw.write("\n");
    }

    private void writeFormatColumn(ViewColumn vc, Writer fw) throws IOException {
        writeSeqIdElement("FormatColumn", vc.getComponentId(), vc.getColumnNumber(), fw);
        writeElement("Name", vc.getName(), fw);
        writeElement("HeaderAlignment", vc.getHeaderJustifyId().dbcode(), fw);
        writeElement("HeaderLine1", vc.getHeaderLine1(), fw);
        writeElement("HeaderLine2", vc.getHeaderLine2(), fw);
        writeElement("HeaderLine3", vc.getHeaderLine3(), fw);
        if (vc.isHidden()) {
            writeElement("Hidden", "1", fw);
        }
        writeElement("SpacesBeforeColumn", Integer.toString(vc.getSpacesBeforeColumn()), fw);
        writeElement("DataType", vc.getDataType().dbcode(), fw);
        writeElement("DateFormat", vc.getDateCode().dbcode(), fw);
        writeElement("Length", Integer.toString(vc.getFieldLength()), fw);
        writeElement("Position", Integer.toString(vc.getStartPosition()), fw);
        writeElement("Alignment", vc.getJustifyId().dbcode(), fw);
        writeElement("Ordinal", Integer.toString(vc.getOrdinalPosition()), fw);
        if (vc.isSigned()) {
            writeElement("SignedData", "1", fw);
        }
        if (vc.getDecimalCount() > 0) {
            writeElement("DecimalPlaces", Integer.toString(vc.getDecimalCount()), fw);
        }
        if (vc.getReportMask() != null && vc.getReportMask().length() > 0) {
            writeElement("Mask", vc.getReportMask(), fw);
        }
        writeElement("AggregationPrefix", vc.getSubtotalPrefix(), fw);
        writeElement("Aggregation", vc.getSubtotalType().dbcode(), fw);
        if (vc.getColumnCalculation().length() > 0) {
            writeColumnCalculations(vc, fw);
        }
        closeElement("FormatColumn", fw);
        fw.write("\n");
    }

    private void writeColumnCalculations(ViewColumn vc, Writer fw) throws IOException {
        openElement("Calculations", fw);
        fw.write("\n");
        writeChunkedText("Calculation", vc.getColumnCalculation(), fw);
        closeElement("Calculations", fw);
        fw.write("\n");
    }

    private void writeHeadersAndFooters(ViewNode vw, Writer fw) throws IOException {
        openElement("Headers", fw);
        fw.write("\n");
        Iterator<ReportHeader> hi = vw.getHeadersIterator();
        while (hi.hasNext()) {
            writeHeaderRecord(hi.next(), fw);
        }
        closeElement("Headers", fw);
        fw.write("\n");
    }

    private void writeHeaderRecord(ReportHeader hdr, Writer fw) throws IOException {
        writeAttributedElement("Header", "ID", Integer.toString(hdr.getComponentId()), fw);
        writeElement("Line", Integer.toString(hdr.getRow()), fw);
        writeElement("Position", Integer.toString(hdr.getColumn()), fw);
        writeElement("Alignment", hdr.getJustification().dbcode(), fw);
        writeElement("Function", Repository.getReportFunctionEnum(hdr.getFunction()).dbcode(), fw);
        writeElement("Text", StringEscapeUtils.escapeHtml4(hdr.getText()), fw);
        closeElement("Header", fw);
        fw.write("\n");
    }

    private void writeExitRefs(ViewNode vw, Writer fw) throws IOException {
        if (vw.getViewDefinition().getFormatExitId() > 0) {
            writeExitRef(vw.getViewDefinition().getFormatExitId(), vw.getViewDefinition().getFormatExitParams(), fw);
        }
    }

    private void writeOutputFileDetails(ViewNode vw, Writer fw) throws IOException {
        ViewDefinition vd = vw.getViewDefinition();
        if (vd.getDefaultOutputFileId() > 0) {

        } else {
            writeAttributedElement("Partition", "ID", "1", fw);
            writeElement("Name", vw.getOutputFile().getName(), fw);
            writeClosedAttributedElement("ServerRef", "ID", "1", fw);
            writeElement("DDNameOutput", vw.getOutputFile().getOutputDDName(), fw);
            closeElement("Partition", fw);
            fw.write("\n");
        }
    }

    private void writeExtract(ViewNode vw, Writer fw) throws IOException {
        openElement("Extract", fw);
        fw.write("\n");
        writeElement("FileNumber", Integer.toString(vw.getViewDefinition().getExtractFileNumber()), fw);
        writeElement("EnableAggregation", vw.getViewDefinition().isExtractSummarized() ? "1" : "0", fw);
        writeElement("AggregationRecords", Integer.toString(vw.getViewDefinition().getMaxExtractSummaryRecords()), fw);
        writeExtractColumns(vw, fw);
        closeElement("Extract", fw);
        fw.write("\n");
    }

    private void writeExtractColumns(ViewNode vw, Writer fw) throws IOException {
        openElement("ExtractColumns", fw);
        fw.write("\n");
        Iterator<ViewColumn> ci = vw.getColumnIterator();
        while (ci.hasNext()) {
            writeColumn(ci.next(), fw);
        }
        closeElement("ExtractColumns", fw);
        fw.write("\n");
    }

    private void writeColumn(ViewColumn vc, Writer fw) throws IOException {
        writeSeqIdElement("ExtractColumn", vc.getComponentId(), vc.getColumnNumber(), fw);
        writeElement("Area", vc.getExtractArea().dbcode(), fw);
        ViewSortKey vsk = currentView.getViewSortKeyFromColumnId(vc.getComponentId());
        if (vc.getExtractArea() != ExtractArea.AREACALC) {
            if(vc.getExtractArea().name() == ExtractArea.SORTKEY.name() && vsk != null){
                writeElement("DataType", vsk.getSortKeyDataType().dbcode(), fw);
                writeElement("Length", Integer.toString(vsk.getSkFieldLength()), fw);
                writeElement("DateFormat", vsk.getSortKeyDateTimeFormat().dbcode(), fw);
            }else {
                writeElement("DataType", vc.getDataType().dbcode(), fw);
                writeElement("Length", Integer.toString(vc.getFieldLength()), fw);
                writeElement("DateFormat", vc.getDateCode().dbcode(), fw);
            }
            writeElement("Position", Integer.toString(vc.getExtractAreaPosition()), fw);
            writeElement("Ordinal", Integer.toString(vc.getOrdinalPosition()), fw);
        }
        ViewColumnSource vcs = vc.getIteratorForSourcesByNumber().next();
        if (vcs.getSortTitleFieldId() > 0) {
            writeIdElement("SortTitleKey", vcs.getSortTitleFieldId(), fw);
            writeElement("DataType", vsk.getDescDataType().dbcode(), fw);
            writeElement("Length", Integer.toString(vsk.getSktFieldLength()), fw);
            closeElement("SortTitleKey", fw);
            fw.write("\n");
        }
        closeElement("ExtractColumn", fw);
        fw.write("\n");
    }

    private void writeInput(ViewNode vw, Writer fw) throws IOException {
        openElement("Input", fw);
        fw.write("\n");
        writeClosedAttributedElement("ControlRecordRef", "ID",
                Integer.toString(vw.getViewDefinition().getControlRecordId()), fw);
        writeDataSources(vw, fw);
        closeElement("Input", fw);
        fw.write("\n");
    }

    private void writeDataSources(ViewNode vw, Writer fw) throws IOException {
        openElement("DataSources", fw);
        fw.write("\n");
        Iterator<ViewSource> vsi = vw.getViewSourceIterator();
        while (vsi.hasNext()) {
            writeViewSource(vsi.next(), fw);
        }
        closeElement("DataSources", fw);
        fw.write("\n");
    }

    private void writeViewSource(ViewSource vs, Writer fw) throws IOException {
        writeSeqIdElement("DataSource", vs.getComponentId(), vs.getSequenceNumber(), fw);
        writeClosedAttributedElement("LogicalFileRef", "ID", Integer.toString(vs.getSourceLFID()), fw);
        writeClosedAttributedElement("LogicalRecordRef", "ID", Integer.toString(vs.getSourceLRID()), fw);
        writeFilters(vs, fw);
        writeColumnAssignments(vs, fw);
        closeElement("DataSource", fw);
        fw.write("\n");
    }

    private void writeColumnAssignments(ViewSource vs, Writer fw) throws IOException {
        openElement("ColumnAssignments", fw);
        fw.write("\n");
        Iterator<ViewColumnSource> cas = vs.getIteratorForColumnSourcesByNumber();
        while (cas.hasNext()) {
            writeColumnAssignment(cas.next(), fw);
        }
        closeElement("ColumnAssignments", fw);
        fw.write("\n");
    }

    private void writeLogic(ViewColumnSource vcs, Writer fw) throws IOException {
        openElement("Logics", fw);
        fw.write("\n");
        writeChunkedText("Logic", vcs.getLogicText(), fw);
        closeElement("Logics", fw);
        fw.write("\n");
    }

    private void writeColumnAssignment(ViewColumnSource vcs, Writer fw) throws IOException {
        writeAttributedElement("ColumnAssignment", "ID", Integer.toString(vcs.getComponentId()), fw);
        writeClosedAttributedElement("ColumnRef", "ID", Integer.toString(vcs.getColumnID()), fw);
        writeElement("SourceType", Integer.toString(vcs.getSourceType().ordinal()), fw);
        writeClosedAttributedElement("FieldRef", "ID", Integer.toString(vcs.getViewSrcLrFieldId()), fw);
        writeClosedAttributedElement("LookupRef", "ID", Integer.toString(vcs.getSrcJoinId()), fw);
        if (vcs.getSortTitleFieldId() > 0) {
            writeClosedAttributedElement("TitleFieldRef", "ID", Integer.toString(vcs.getSortTitleFieldId()), fw);
        }
        if (vcs.getSortTitleLookupId() > 0) {
            writeClosedAttributedElement("TitleLookupRef", "ID", Integer.toString(vcs.getSortTitleLookupId()), fw);
        }
        writeLogic(vcs, fw);
        closeElement("ColumnAssignment", fw);
        fw.write("\n");
    }

    private void writeFilters(ViewSource vs, Writer fw) throws IOException {
        String ef = vs.getExtractFilter();
        if (ef != null && ef.length() > 0) {
            openElement("Filters", fw);
            fw.write("\n");
            writeChunkedText("Filter", ef, fw);
            closeElement("Filters", fw);
            fw.write("\n");
        }
    }

    private void writeChunkedText(String element, String text, Writer fw) throws IOException {
        int chunkSize = 256;
        int pos = 0;
        int seq = 1;
        int len = text.length();
        while (pos <= len) {
            String chunk;
            if (pos + chunkSize < len) {
                chunk = text.substring(pos, pos + chunkSize);
            } else {
                chunk = text.substring(pos, len);
            }
            writeNoNLAttributedElement(element, "seq", Integer.toString(seq++), fw);
            fw.write(escapeString(chunk));
            closeElement(element, fw);
            fw.write("\n");
            pos += chunkSize;
        }
    }

    private void writeFolderRefs(Writer fw) throws IOException {
        // Dummy - we don't have this info
        openElement("FolderRefs", fw);
        fw.write("\n");
        writeClosedAttributedElement("FolderRef", "ID", "1", fw);
        closeElement("FolderRefs", fw);
        fw.write("\n");
    }

    private void writeMetadata(Writer fw) throws IOException {
        fw.write("    <MetaData>\n");
        writeControls(fw);
        writeServers(fw);
        writePartitions(fw);
        writeLogicFiles(fw);
        writeLogicalRecords(fw);
        writeLookups(fw);
        writeExits(fw);
        fw.write("    </MetaData>\n");
    }

    private void writeExits(Writer fw) throws IOException {
        openElement("Exits", fw);
        fw.write("\n");
        Iterator<UserExit> ei = Repository.getUserExits().getIterator();
        while (ei.hasNext()) {
            writeExit(ei.next(), fw);
        }
        closeElement("Exits", fw);
        fw.write("\n");
    }

    private void writeExit(UserExit ex, Writer fw) throws IOException {
        writeIdElement("Exit", ex.getComponentId(), fw);
        writeElement("Program", ex.getName(), fw);
        writeElement("Module", ex.getExecutable(), fw);
        writeElement("Type", ex.getExitType().dbcode(), fw);
        writeElement("ProgramType", ex.getProgramType().dbcode(), fw);
        writeElement("Optimize", ex.isOptimizable() ? "1" : "0", fw);
        closeElement("Exit", fw);
        fw.write("\n");
    }

    private void writeLookups(Writer fw) throws IOException {
        openElement("Lookups", fw);
        fw.write("\n");
        Iterator<LookupPath> lki = Repository.getLookups().getIterator();
        while (lki.hasNext()) {
            writeLookup(lki.next(), fw);
        }
        closeElement("Lookups", fw);
        fw.write("\n");
    }

    private void writeLookup(LookupPath lk, Writer fw) throws IOException {
        writeIdElement("Lookup", lk.getID(), fw);
        writeElement("Name", lk.getName(), fw);
        writeSteps(lk, fw);
        closeElement("Lookup", fw);
        fw.write("\n");
    }

    private void writeSteps(LookupPath lk, Writer fw) throws IOException {
        openElement("Steps", fw);
        fw.write("\n");
        Iterator<LookupPathStep> si = lk.getStepIterator();
        while (si.hasNext()) {
            writeStep(si.next(), fw);
        }
        closeElement("Steps", fw);
        fw.write("\n");
    }

    private void writeStep(LookupPathStep step, Writer fw) throws IOException {
        writeAttributedElement("Step", "Number", Integer.toString(step.getStepNum()), fw);
        openElement("Source", fw);
        fw.write("\n");
        writeStepSource(step, fw);
        closeElement("Source", fw);
        fw.write("\n");
        writeStepTarget(step, fw);
        closeElement("Step", fw);
        fw.write("\n");
    }

    private void writeStepTarget(LookupPathStep step, Writer fw) throws IOException {
        openElement("Target", fw);
        fw.write("\n");
        int targLrid = step.getTargetLR();
        writeClosedAttributedElement("LogicalRecordRef", "ID", Integer.toString(targLrid), fw);
        writeClosedAttributedElement("LogicalFileRef", "ID", Integer.toString(step.getTargetLF()), fw);
        LogicalRecord lr = Repository.getLogicalRecords().get(targLrid);
        if (lr.getLookupExitID() > 0) {
            writeExitRef(lr.getLookupExitID(), lr.getLookupExitParams(), fw);
        }
        closeElement("Target", fw);
        fw.write("\n");
    }

    private void writeStepSource(LookupPathStep step, Writer fw) throws IOException {
        writeClosedAttributedElement("LogicalRecordRef", "ID", Integer.toString(step.getSourceLR()), fw);
        writeKeyFields(step, fw);
    }

    private void writeKeyFields(LookupPathStep step, Writer fw) throws IOException {
        openElement("KeyFields", fw);
        fw.write("\n");
        Iterator<LookupPathKey> ki = step.getKeyIterator();
        while (ki.hasNext()) {
            writeKey(ki.next(), fw);
        }
        closeElement("KeyFields", fw);
        fw.write("\n");
    }

    private void writeKey(LookupPathKey key, Writer fw) throws IOException {
        writeSeqIdElement("KeyField", key.getComponentId(), key.getKeyNumber(), fw);
        if (key.getFieldId() > 0) {
            writeClosedAttributedElement("FieldRef", "ID", Integer.toString(key.getFieldId()), fw);
        } else if (key.getSymbolicName().length() > 0) {
            writeElement("SymbolicName", key.getSymbolicName(), fw);
            writeElement("SymbolicDefault", key.getValue(), fw);
        } else {
            writeElement("Constant", key.getValue(), fw);
        }
        writeElement("DataType", key.getDatatype().dbcode(), fw);
        writeElement("DateFormat", key.getDateTimeFormat().dbcode(), fw);
        writeElement("Length", Integer.toString(key.getFieldLength()), fw);
        writeElement("SignedData", key.isSigned() ? "1" : "0", fw);
        closeElement("KeyField", fw);
        fw.write("\n");
    }

    private void writeLogicalRecords(Writer fw) throws IOException {
        openElement("LogicalRecords", fw);
        fw.write("\n");
        Iterator<LogicalRecord> lri = Repository.getLogicalRecords().getIterator();
        while (lri.hasNext()) {
            writeLr(lri.next(), fw);
        }
        closeElement("LogicalRecords", fw);
        fw.write("\n");
    }

    private void writeLr(LogicalRecord lr, Writer fw) throws IOException {
        if (lr.getComponentId() <= Repository.getMaxVdpLrId()) {
            writeIdElement("LogicalRecord", lr.getComponentId(), fw);
            writeElement("Name", lr.getName(), fw);
            writeElement("LRStatus", lr.getStatus().dbcode(), fw);
            writeLrIndexes(lr, fw);
            writeLrFields(lr.getIteratorForFieldsByID(), fw);
            closeElement("LogicalRecord", fw);
            fw.write("\n");
        }
    }

    private void writeLrIndexes(LogicalRecord lr, Writer fw) throws IOException {
        if (lr.getValuesOfIndexBySeq().size() > 0) {
            writeIndex(lr.getIteratorForIndexBySeq(), fw);
        }
    }

    private void writeIndex(Iterator<LRIndex> ii, Writer fw) throws IOException {
        while (ii.hasNext()) {
            LRIndex ndx = ii.next();
            if (ndx.getKeyNumber() == 1) {
                writeIdElement("Index", ndx.getComponentId(), fw);
                writeElement("Name", ndx.getName(), fw);
                openElement("IndexFieldRefs", fw);
                fw.write("\n");
            }
            writeIndexRef(ndx, fw);
        }
        closeElement("IndexFieldRefs", fw);
        fw.write("\n");
        closeElement("Index", fw);
        fw.write("\n");
    }

    private void writeIndexRef(LRIndex ndx, Writer fw) throws IOException {
        writeClosedSeqIdElement("IndexFieldRef", ndx.getFieldID(), ndx.getKeyNumber(), fw);
    }

    private void writeLrFields(Iterator<LRField> fi, Writer fw) throws IOException {
        openElement("Fields", fw);
        fw.write("\n");
        while (fi.hasNext()) {
            writeLRField(fi.next(), fw);
        }
        closeElement("Fields", fw);
        fw.write("\n");
    }

    private void writeLRField(LRField lrf, Writer fw) throws IOException {
        writeIdElement("Field", lrf.getComponentId(), fw);
        writeElement("Name", lrf.getName(), fw);
        writeElement("DataType", lrf.getDatatype().dbcode(), fw);
        writeElement("DateFormat", lrf.getDateTimeFormat().dbcode(), fw);
        writeElement("Length", Integer.toString(lrf.getLength()), fw);
        writeElement("Position", Integer.toString(lrf.getStartPosition()), fw);
        writeElement("Alignment", lrf.getJustification().dbcode(), fw);
        writeElement("SignedData", lrf.isSigned() ? "1" : "0", fw);
        writeElement("DecimalPlaces", Integer.toString(lrf.getNumDecimalPlaces()), fw);
        writeElement("Ordinal", Integer.toString(lrf.getOrdinalPosition()), fw);
        closeElement("Field", fw);
        fw.write("\n");
    }

    private void writeLogicFiles(Writer fw) throws IOException {
        openElement("LogicalFiles", fw);
        fw.write("\n");
        Iterator<LogicalFile> lfi = Repository.getLogicalFiles().getIterator();
        while (lfi.hasNext()) {
            writeLf(lfi.next(), fw);
        }
        closeElement("LogicalFiles", fw);
        fw.write("\n");
    }

    private void writeLf(LogicalFile lf, Writer fw) throws IOException {
        if (lf.getID() < 9000000) {
            writeIdElement("LogicalFile", lf.getID(), fw);
            writeElement("Name", lf.getName(), fw);
            writeParitionRefs(lf.getPFSeqIterator(), fw);
            closeElement("LogicalFile", fw);
            fw.write("\n");
        }
    }

    private void writeParitionRefs(Iterator<Entry<Integer, PhysicalFile>> spfi, Writer fw) throws IOException {
        openElement("PartitionRefs", fw);
        fw.write("\n");
        while (spfi.hasNext()) {
            writePartitionRef(spfi.next(), fw);
        }
        closeElement("PartitionRefs", fw);
    }

    private void writePartitionRef(Entry<Integer, PhysicalFile> spf, Writer fw) throws IOException {
        writeClosedSeqIdElement("PartitionRef", spf.getValue().getComponentId(), spf.getKey(), fw);
    }

    private void writePartitions(Writer fw) throws IOException {
        openElement("Partitions", fw);
        fw.write("\n");
        Iterator<PhysicalFile> pfi = Repository.getPhysicalFiles().getIterator();
        while (pfi.hasNext()) {
            writePf(pfi.next(), fw);
        }
        closeElement("Partitions", fw);
        fw.write("\n");
    }

    private void writePf(PhysicalFile pf, Writer fw) throws IOException {
        if (pf.getComponentId() < 9000000) {
            writeIdElement("Partition", pf.getComponentId(), fw);
            writeElement("Name", pf.getName(), fw);
            writeElement("PartitionType", pf.getFileType().dbcode(), fw);
            writeElement("AccessMethod", pf.getAccessMethod().dbcode(), fw);
            writeElement("RecordDelimiter", pf.getRecordDelimiter().toString(), fw);
            if (pf.getSqlText().length() > 0) {
                writeDatabaseSection(pf, fw);
            }
            writeElement("DDNameInput", pf.getInputDDName(), fw);
            writeElement("DDNameOutput", pf.getOutputDDName(), fw);
            writeElement("LRECL", Integer.toString(pf.getLrecl()), fw);
            writeElement("RECFM", pf.getRecfm().toString(), fw);
            writeElement("MinRecordLength", Integer.toString(pf.getMinimumLength()), fw);
            if (pf.getReadExitID() > 0) {
                writeExitRef(pf.getReadExitID(), pf.getReadExitIDParm(), fw);
            }
            closeElement("Partition", fw);
            fw.write("\n");
        }
    }

    private void writeDatabaseSection(PhysicalFile pf, Writer fw) throws IOException {
        openElement("Database", fw);
        fw.write("\n");
        writeElement("Connection", pf.getDatabaseConnection(), fw);
        writeElement("Table", pf.getDatabaseTable(), fw);
        writeElement("RowFormat", pf.getDatabaseRowFormat().dbcode(), fw);
        writeElement("IncludeNulls", pf.isIncludeNulls() ? "1" : "0", fw);
        writeChunkedText("Query", pf.getSqlText(), fw);
        closeElement("Database", fw);
        fw.write("\n");
    }

    private void writeExitRef(int exitid, String params, Writer fw) throws IOException {
        openElement("ExitRefs", fw);
        fw.write("\n");
        UserExit ue = Repository.getUserExits().get(exitid);
        writeOpenIdTypeElement("ExitRef", ue.getComponentId(), ue.getExitType().dbcode(), fw);
        if (params != null && params.length() > 0) {
            writeElement("Parameter", params, fw);
        }
        closeElement("ExitRef", fw);
        fw.write("\n");
        closeElement("ExitRefs", fw);
        fw.write("\n");
    }

    private void writeServers(Writer fw) throws IOException {
        openElement("Servers", fw);
        fw.write("\n");
        closeElement("Servers", fw);
        fw.write("\n");
    }

    private void writeControls(Writer fw) throws IOException {
        openElement("ControlRecords", fw);
        fw.write("\n");
        Iterator<ControlRecord> ci = Repository.getControlRecords().getIterator();
        while (ci.hasNext()) {
            writeCR(ci.next(), fw);
        }
        closeElement("ControlRecords", fw);
        fw.write("\n");
    }

    private void writeCR(ControlRecord cr, Writer fw) throws IOException {
        writeIdElement("ControlRecord", cr.getComponentId(), fw);
        writeElement("Name", cr.getName(), fw);
        writeElement("MonthFirstPeriod", Integer.toString(cr.getFirstFiscalMonth()), fw);
        writeElement("MinPeriod", Integer.toString(cr.getBeginningPeriod()), fw);
        writeElement("MaxPeriod", Integer.toString(cr.getEndingPeriod()), fw);
        closeElement("ControlRecord", fw);
        fw.write("\n");
    }

    private void writeIdElement(String name, int componentId, Writer fw) throws IOException {
        fw.write("<" + name + " ID=\"" + componentId + "\">\n"); // Need an Environment = don't care on ID
    }

    private void writeIdTypeElement(String name, int componentId, String type, Writer fw) throws IOException {
        fw.write("<" + name + " ID=\"" + componentId + "\" Type=\"" + type + "\"/>\n");
    }

    private void writeOpenIdTypeElement(String name, int componentId, String type, Writer fw) throws IOException {
        fw.write("<" + name + " ID=\"" + componentId + "\" Type=\"" + type + "\">\n");
    }

    private void writeSeqIdElement(String name, int componentId, int seq, Writer fw) throws IOException {
        fw.write("<" + name + " seq=\"" + seq + "\" ID=\"" + componentId + "\">\n");
    }

    private void writeClosedSeqIdElement(String name, int componentId, int seq, Writer fw) throws IOException {
        fw.write("<" + name + " seq=\"" + seq + "\" ID=\"" + componentId + "\"/>\n");
    }

    private void writeAttributedElement(String name, String attr, String attrValue, Writer fw) throws IOException {
        fw.write("<" + name + " " + attr + "=\"" + attrValue + "\">\n");
    }

    private void writeNoNLAttributedElement(String name, String attr, String attrValue, Writer fw) throws IOException {
        fw.write("<" + name + " " + attr + "=\"" + attrValue + "\">");
    }

    private void writeClosedAttributedElement(String name, String attr, String attrValue, Writer fw)
            throws IOException {
        fw.write("<" + name + " " + attr + "=\"" + attrValue + "\"/>\n");
    }

    private void writeEnvironment(Writer fw) throws IOException {
        fw.write("<Environment ID=\"0\">\n"); // Need an Environment = don't care on ID
    }

    private void writeJobInfo(Writer fw) throws IOException {
        openElement("JobInfo", fw);
        fw.write("\n");
        // const std::string & strRunDate = m_pConfig->getEntry(CFG_GLOBAL,
        // CFG_RUNDATE);

        // CVDPDate dtRunDate;
        // if (!strRunDate.empty())
        // {
        // dtRunDate = strRunDate.c_str();
        // }
        // std::string rData;
        // dtRunDate.fillXMLdateString(rData);
        // //Don't want the Database thing at all... we want to say generated by MR91
        // strmXML << " <RunDate>" << rData << "</RunDate>\n"
        // << " <MaxExtractFileNumber>" << 0 << "</MaxExtractFileNumber>\n";

        int digits = 23;
        int places = 3;
        if (GersConfigration.isNumberModeStandard()) {
            places = 8;
        }
        writeElement("DecimalDataDigits", Integer.toString(digits), fw);
        writeElement("DecimalDataPlaces", Integer.toString(places), fw);

        // CVDPExtractOutputFile & extrFile = m_VDPFile.GetExtractOutputFile();
        // strmXML << " <ExtractFile ID=\"1\">\n";
        // appendInputFileInfo(extrFile.GetFileInfo(), extrFile.GetRecordID(), strmXML);
        // strmXML << " </ExtractFile>\n";
        closeElement("JobInfo", fw);
        fw.write("\n");
    }

    private void writeHeader(Writer fw) throws IOException {
        fw.write("<?xml version = \"1.0\" ?>\n");
        fw.write("<SAFRVDP xmlns = \"http://www.ibm.com\"\n");
        fw.write("    xmlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n");
        fw.write("    xsi:schemaLocation = \"http://www.ibm.com\n");
        fw.write("                           SAFRVDP.xsd\">\n");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Date dt = Calendar.getInstance().getTime();
        writeElement("CreatedDateTime", dateFormat.format(dt), fw);
        writeElement("Program", "RCG", fw);
    }

    private void writeElement(String name, String value, Writer fw) throws IOException {
        openElement(name, fw);
        fw.write(value);
        closeElement(name, fw);
        fw.write("\n");
    }

    private void openElement(String name, Writer fw) throws IOException {
        fw.write("<" + name + ">");
    }

    private void closeElement(String name, Writer fw) throws IOException {
        fw.write("</" + name + ">");
    }

    private String escapeString(String strTemp) {
        String strEscape = "";
        CharacterIterator it = new StringCharacterIterator(strTemp);
        for(int i=0; i< strTemp.length(); i++) {
            char ch = strTemp.charAt(i);
            switch (ch) {
                case '<':
                    strEscape += "&lt;";
                    break;
                case '>':
                    strEscape += "&gt;";
                    break;
                case '\"':
                    strEscape += "&quot;";
                    break;
                case '\n':
                    strEscape += "&#10;";
                    break;
                case '\r':
                    strEscape += "&#13;";
                    break;
                case '\'':
                    strEscape += "&apos;";
                    break;
                case '&':
                    strEscape += "&amp;";
                    break;
                default:
                    strEscape += ch;
                    // Do nothing
                    break;
            } // End of switch
        }
        return strEscape;
    }

}
