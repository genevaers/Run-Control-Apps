package org.genevaers.runcontrolgenerator.repositorybuilders;

import java.util.Set;

import org.genevaers.genevaio.yamlreader.ViewColumnData;
import org.genevaers.genevaio.yamlreader.ViewColumnSourceTransfer;
import org.genevaers.genevaio.yamlreader.ViewLogicDependencyTransfer;
import org.genevaers.genevaio.yamlreader.YAMLLogicalFileReader;
import org.genevaers.genevaio.yamlreader.YAMLLogicalRecordReader;
import org.genevaers.genevaio.yamlreader.YAMLLookupsReader;
import org.genevaers.genevaio.yamlreader.YAMLViewColumnTransfer;
import org.genevaers.genevaio.yamlreader.YAMLViewSourceTransfer;
import org.genevaers.genevaio.yamlreader.YAMLViewTransfer;
import org.genevaers.genevaio.yamlreader.YAMLizer;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewDefinition;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.ColumnSourceType;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.ExtractArea;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.OutputMedia;
import org.genevaers.repository.components.enums.SubtotalType;
import org.genevaers.repository.components.enums.ViewStatus;
import org.genevaers.repository.components.enums.ViewType;
import org.genevaers.utilities.GersConfigration;
import org.genevaers.utilities.NamesReader;
import org.genevaers.utilities.Status;
import com.google.common.flogger.FluentLogger;

public class YAMLBuilder implements RepositoryBuilder {
	private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private Status retval = Status.OK;

    @Override
    public Status run() {
        if(validDirectoryExists()) {
            Set<String> views = readViewsRequired();
            views.stream().forEach(v -> addViewToRepo(v));
            return retval;
        } else {
           logger.atSevere().log("No .genevaers directory found");
            return Status.ERROR;
         }
    }

    private void addViewToRepo(String v) {
        logger.atInfo().log("Add %s to repo", v);
        YAMLizer.setEnvironmentName(GersConfigration.getValue(GersConfigration.ENVIRONMENT_ID));
        YAMLViewTransfer viewTxfr = YAMLizer.readView(YAMLizer.getViewsPath().resolve(v + ".yaml"));
        logger.atInfo().log("Read %s", v);
        ViewNode vn = createAndAddView(viewTxfr);
        viewTxfr.getViewSources().stream().forEach(vs -> addViewSource(vs, vn));
        viewTxfr.getViewColumns().stream().forEach(vc -> addViewColumn(vc, vn));
        viewTxfr.getViewDepenencies().stream().forEach(vd -> addViewDependency(vd));
    }

    private void addViewDependency(ViewLogicDependencyTransfer vd) {
        if(vd.getLookupPathId() != null) {
            YAMLLookupsReader ylkr = new YAMLLookupsReader();
           // ylkr.addNamedLookupToRepo(GersConfigration.getParm(GersConfigration.ENVIRONMENT_ID), null);
        }
    }

    private void addViewColumn(YAMLViewColumnTransfer yvct, ViewNode vn) {
        ViewColumn vc = new ViewColumn();
        ViewColumnData vcd = yvct.getColumn();
        vc.setColumnCalculation(vcd.getFormatColumnLogic());
        vc.setColumnNumber(vcd.getColumnNo());
        vc.setComponentId(vcd.getColumnNo());
        vc.setDataType(DataType.fromdbcode(vcd.getDataType()));
        vc.setDateCode(vcd.getDateTimeFormat() != null ? DateCode.fromdbcode(vcd.getDateTimeFormat()) : DateCode.NONE);
        vc.setDecimalCount(vcd.getDecimalPlaces().shortValue());
        vc.setDefaultValue(vcd.getDefaultValue() != null ? vcd.getDefaultValue() : "");
        vc.setDetailPrefix("");
        vc.setExtractArea(ExtractArea.fromdbcode(vcd.getExtractAreaCode()));
        vc.setExtractAreaPosition(vcd.getExtractAreaPosition().shortValue());
        vc.setFieldLength(vcd.getLength().shortValue());
        vc.setHidden(!vcd.getVisible());
        vc.setJustifyId(vcd.getDataAlignmentCode() != null ? JustifyId.fromdbcode(vcd.getDataAlignmentCode()) : JustifyId.NONE);
        vc.setOrdinalPosition(vcd.getOrdinalPosition().shortValue());
        vc.setReportMask(vcd.getNumericMask() != null ? vcd.getNumericMask() : "");
        vc.setRounding(vcd.getScalingFactor().shortValue());
        vc.setSigned(vcd.getSigned());
        vc.setSpacesBeforeColumn(vcd.getSpacesBeforeColumn().shortValue());
        vc.setStartPosition(vcd.getStartPosition().shortValue());
        vc.setSubtotalPrefix(vcd.getSubtotalLabel() != null ? vcd.getSubtotalLabel() : "");
        vc.setSubtotalType(vcd.getSubtotalTypeCode() != null ? SubtotalType.fromdbcode(vcd.getSubtotalTypeCode()) : SubtotalType.NONE);
        vc.setHeaderJustifyId(JustifyId.NONE);
        vc.setViewId(vn.getID());
        yvct.getColumnSources().stream().forEach(vcs -> addViewColumnSource(vcs, vc, vn));
        vn.addViewColumn(vc);
    }

    private void  addViewColumnSource(ViewColumnSourceTransfer vcst, ViewColumn vc, ViewNode vn) {
        ViewColumnSource vcs = new ViewColumnSource();
        vcs.setViewId(vc.getViewId());
        vcs.setColumnID(vc.getComponentId());
        short vsndx = (short)(vc.getValuesOfSourcesByID().size() + 1);
        vcs.setComponentId(vsndx);
        vcs.setColumnNumber(vc.getColumnNumber());
        vcs.setLogicText(vcst.getExtractColumnLogic());
        vcs.setSourceType(ColumnSourceType.values()[vcst.getSourceTypeId()]);
        vcs.setSrcJoinId(vcst.getLookupPathId() != null ? vcst.getLookupPathId() : 0);
        if(vcst.getSourceValue() != null) {
            vcs.setSrcValue(vcst.getSourceValue());
            vcs.setValueLength(vcst.getSourceValue().length());
        }
        vcs.setViewSrcLrFieldId(vcst.getSourceLRFieldId() != null ? vcst.getSourceLRFieldId() : 0);
        vcs.setSequenceNumber(vsndx);
        vc.addToSourcesByID(vcs);
        vc.addToSourcesByNumber(vcs);
        ViewSource vsrc = vn.getViewSource(vsndx);
        vcs.setViewSourceId(vsndx);
        vsrc.addToColumnSourcesByNumber(vcs);
    }

    private void addViewSource(YAMLViewSourceTransfer vst, ViewNode vn) {
        ViewSource vs = new ViewSource();
        vs.setExtractFilter(vst.getExtractFilter());
        vs.setExtractOutputLogic(vst.getOutputLogic());
        // vs.setOutputLFID(vst.getLogicalFile()));
        // vs.setOutputPFID(v);
        YAMLLogicalFileReader ylfr = new YAMLLogicalFileReader();
        ylfr.addLFtoRepo(GersConfigration.getValue(GersConfigration.ENVIRONMENT_ID), vst.getLogicalFile());
        YAMLLogicalRecordReader ylrr = new YAMLLogicalRecordReader();
        ylrr.addLRToRepo(GersConfigration.getValue(GersConfigration.ENVIRONMENT_ID), vst.getLogicalRecord());
        vs.setSourceLFID(Repository.getLogicalFiles().get(vst.getLogicalFile()).getID());
        vs.setSourceLRID(Repository.getLogicalRecords().get(vst.getLogicalRecord()).getComponentId());
        vs.setViewId(vn.getID());
        int vsndx = vn.getNumberOfViewSources() + 1;
        vs.setComponentId(vsndx);
        vs.setSequenceNumber((short)vsndx);
        vn.addViewSource(vs);
    }

    private ViewNode createAndAddView(YAMLViewTransfer viewTxfr) {
        ViewNode vn = null;
        ViewDefinition vd = new ViewDefinition();
        vd.setComponentId(viewTxfr.getId());
        vd.setName(viewTxfr.getName());
        //Don't really care here if view is enabled or not
        if(Repository.isViewEnabled(viewTxfr.getId())) {
            vn = Repository.getViewNodeMakeIfDoesNotExist(vd);
        } else {
            logger.atInfo().log("View %d not enabled", viewTxfr.getId());
        }
        vd.setControlRecordId(viewTxfr.getControlRecId()); //Get CR
        vd.setDefaultOutputFileId(0);
        //Based on type code
        switch (viewTxfr.getTypeCode()) {
            case "EXTR":
                vd.setDetailed(false);
                vd.setExtractSummarized(false);
                break;
        
            default:
                vd.setDetailed(false);
                break;
        }
        vd.setExtractFileNumber(viewTxfr.getWorkFileNumber().shortValue());
        vd.setExtractMaxRecCount(viewTxfr.getExtractMaxRecCount());
        vd.setFormatExitId(viewTxfr.getFormatExitId() != null ? viewTxfr.getFormatExitId() : 0);
        vd.setGenerateDelimitedHeader(viewTxfr.getFieldDelimCode() != null ? viewTxfr.getFieldDelimCode().isEmpty() ? false : true : false);
        vd.setMaxExtractSummaryRecords(viewTxfr.getExtractSummaryBuffer());
        vd.setOutputLineSizeMax(viewTxfr.getLineSize().shortValue());
        vd.setOutputMaxRecCount(viewTxfr.getOutputMaxRecCount());
        vd.setOutputMedia(OutputMedia.fromdbcode(viewTxfr.getOutputFormatCode()));
        vd.setOutputPageSizeMax(viewTxfr.getPageSize().shortValue());
        vd.setStatus(ViewStatus.fromdbcode(viewTxfr.getStatusCode()));
        vd.setViewType(ViewType.fromdbcode(viewTxfr.getTypeCode()));
        vd.setZeroValueRecordSuppression(viewTxfr.getZeroSuppressInd());

        vd.setFormatExitParams(viewTxfr.getFormatExitParams() != null ? viewTxfr.getFormatExitParams() : "");
        return vn;
    }

    private Set<String> readViewsRequired() {
        return NamesReader.getNamesFrom(GersConfigration.VIEW_NAMES);
    }

    private boolean validDirectoryExists() {
        return GersConfigration.getGersHome().toFile().exists();
    }

}
