package org.genevaers.genevaio.yamlreader;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.genevaers.genevaio.dbreader.DBReaderBase;
import org.genevaers.genevaio.dbreader.DatabaseConnection;
import org.genevaers.genevaio.dbreader.DatabaseConnectionParams;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LRIndex;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;
import org.genevaers.repository.components.enums.JustifyId;
import org.genevaers.repository.components.enums.LrStatus;
import org.genevaers.repository.data.CompilerMessage;
import org.genevaers.repository.data.CompilerMessageSource;

import com.google.common.flogger.FluentLogger;

public class YAMLLogicalRecordReader extends YAMLReaderBase{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
	private static int maxid;
	private static int maxLrLfAssocid = 1;
	private static Map<Integer, LogicalRecordYAMLBean> lrBeans = new TreeMap<>();
	private static Map<Integer, String> lrIds2Names = new TreeMap<>();
	
	// private static Map<Integer, List<ComponentAssociationTransfer>> ourLRLFAssociateionsByLR = new HashMap<>();
	// private static Map<Integer, ComponentAssociationTransfer> ourLRLFAssociateionsById = new HashMap<>();
	
	private static LogicalRecordYAMLBean currentlrbxf;
	private static Map<Integer, LogicalRecordYAMLBean> lrbxfrsByID = new TreeMap<>();
	private static Map<String, LogicalRecordYAMLBean> lrbxfrsByName = new TreeMap<>();


    public void addLRToRepo(String environmentName, int lrid) {
        LogicalRecord lr = new LogicalRecord();
		YAMLReaderBase.environmentName = environmentName;
		if(lrbxfrsByID.isEmpty()) {
			queryAllLogicalRecords();
		}
		if(lrid > 0) {
			LogicalRecordYAMLBean lrb = lrbxfrsByID.get(lrid);
			lr.setComponentId(lrb.getId());
			lr.setName(lrb.getName());
			lr.setStatus(LrStatus.fromdbcode(lrb.getLrStatusCode()));
			if(lr.getStatus() == LrStatus.INACTIVE) {
				Repository.addErrorMessage(new CompilerMessage(0, CompilerMessageSource.VIEW_PROPS, lr.getComponentId(), 0, 0, "Logical record " + lr.getName() + "[" + lr.getComponentId() + "] is not active"));
			}
			int le = lrb.getLookupExitId() != null ? lrb.getLookupExitId() : 0;
			lr.setLookupExitID(le);
			if(le > 0) {
				requiredExits.add(le);
			}
			lr.setLookupExitParams(getDefaultedString(lrb.getLookupExitParams(), ""));
			Repository.addLogicalRecord(lr);
			addLRFields(lrb);
			addIndexes(lrb);
			lrb.getLfs().keySet().stream().forEach(lf -> requiredLFs.add(lf));
		}
   }

    private void addIndexes(LogicalRecordYAMLBean lrb) {
		LRIndex pndx = new LRIndex();
		LRIndexTransfer nt = lrb.getIndex();
		lrb.getIndexes().stream().forEach(x -> addIndexToRepo(x, nt));
	}

	private void addIndexToRepo(LRIndexFieldTransfer x, LRIndexTransfer nt) {
		LRIndex pndx = new LRIndex();
		pndx.setComponentId(nt.getId());
		pndx.setEffectiveDateEnd(nt.getEffectiveEndDateLRFieldId()!=0);
		pndx.setEffectiveDateStart(nt.getEffectiveStartDateLRFieldId()!=0);
		pndx.setLrId(nt.getLrId());
		pndx.setKeyNumber(x.getFldSeqNbr().shortValue());
		pndx.setFieldID(x.getAssociatedComponentId());
		Repository.addLRIndex(pndx);
	}

	private void addFieldToRepo(LRFieldTransfer f) {
       LRField lrf = new LRField();
        lrf.setComponentId(f.getId());
        lrf.setLrID(f.getLrId());
        lrf.setName(f.getName());
        lrf.setStartPosition(f.getFixedStartPos().shortValue());
        lrf.setOrdinalPosition(f.getOrdinalPos().shortValue());
        lrf.setOrdinalOffset(f.getOrdinalOffset() != null ? f.getOrdinalOffset().shortValue() : 0);
        lrf.setDatatype(DataType.fromdbcode(f.getDataType()));
        lrf.setSigned(f.getSigned());
        lrf.setLength(f.getLength().shortValue());
        lrf.setNumDecimalPlaces(f.getDecimalPlaces().shortValue());
        lrf.setRounding(f.getScalingFactor().shortValue());
        lrf.setDateTimeFormat(DateCode.fromdbcode(getDefaultedString(f.getDateTimeFormat(), "NONE")));
        if (lrf.getDatatype() == DataType.ALPHANUMERIC) {
            lrf.setJustification(JustifyId.LEFT);
        } else {
            lrf.setJustification(JustifyId.RIGHT);
        }
        //lrf.setJustification(JustifyId.fromdbcode(getDefaultedString(rs.getString("JUSTIFYCD"), "NONE")));
        lrf.setMask("");  //These probably should not be here at all
        lrf.setDbColName("");
        Repository.addLRField(lrf);
	}

	private void addLRFields(LogicalRecordYAMLBean lrb) {
		lrb.getFields().values().stream().forEach(f -> addFieldToRepo(f));
	}

	public void addRequiredToRepo(String environmentName) {
		logger.atInfo().log("Adding required LRs %s", requiredLRs.toString());
		requiredLRs.stream().forEach(lr -> addLRToRepo(environmentName, lr));
        // LogicalRecord lr = new LogicalRecord();
        // lr.setComponentId(rs.getInt("LOGRECID"));
        // lr.setName(rs.getString("NAME"));
        // lr.setStatus(LrStatus.fromdbcode(rs.getString("LRSTATUSCD")));
        // if(lr.getStatus() == LrStatus.INACTIVE) {
        //     Repository.addErrorMessage(new CompilerMessage(0, CompilerMessageSource.VIEW_PROPS, lr.getComponentId(), 0, 0, "Logical record " + lr.getName() + "[" + lr.getComponentId() + "] is not active"));
        // }
        // int le = rs.getInt("LOOKUPEXITID");
        // lr.setLookupExitID(le);
        // if(le > 0) {
        //     requiredExits.add(le);
        // }
        // lr.setLookupExitParams(getDefaultedString(rs.getString("LOOKUPEXITSTARTUP"), ""));
        // Repository.addLogicalRecord(lr);
    }

	public List<LogicalRecordYAMLBean> queryAllLogicalRecords()  {
		List<LogicalRecordYAMLBean> result = new ArrayList<LogicalRecordYAMLBean>();
		if(lrBeans.isEmpty()) {
			logger.atInfo().log("No Beans read the dir");
			maxid = 0;
			YAMLizer.setEnvironmentName(environmentName);
			Path lrsPath = YAMLizer.getLRsPath();
			File[] lrs = lrsPath.toFile().listFiles();
			if(lrs.length > 0) {
				Stream.of(lrs)
			    	      .filter(file -> file.isFile())
			    	      .forEach(lr -> addToResults(result, lr));
			}
		}
		result.addAll(lrBeans.values());
		return result;
	}

	private void addToResults(List<LogicalRecordYAMLBean> result, File lr) {
		LogicalRecordYAMLBean lrb = (LogicalRecordYAMLBean) YAMLizer.readYamlLR(lr.toPath());
		if(lrb.getId() > maxid) {
			maxid = lrb.getId();
		}
		LogicalRecordYAMLBean lrBean = new LogicalRecordYAMLBean();
		lrBean.setId(lrb.getId());
		lrBean.setName(lrb.getName());
		result.add(lrBean);
		lrBeans.put(lrb.getId(), lrBean);
		lrbxfrsByID.put(lrb.getId(), lrb);
		lrbxfrsByName.put(lrb.getName(), lrb);
		logger.atInfo().log("Bean Added lrbxf for LR:" + lrb.getId());
	}

	public LogicalRecordYAMLBean getLogicalRecord(int id) {
		if(lrbxfrsByID.isEmpty()) {
			queryAllLogicalRecords();
		}
		return lrbxfrsByID.get(id);
	}

	public void addLRToRepo(String environmentName, String logicalRecord) {
        LogicalRecord lr = new LogicalRecord();
		YAMLReaderBase.environmentName = environmentName;
		if(lrbxfrsByName.isEmpty()) {
			queryAllLogicalRecords();
		}
			LogicalRecordYAMLBean lrb = lrbxfrsByName.get(logicalRecord);
			lr.setComponentId(lrb.getId());
			lr.setName(lrb.getName());
			lr.setStatus(LrStatus.fromdbcode(lrb.getLrStatusCode()));
			if(lr.getStatus() == LrStatus.INACTIVE) {
				Repository.addErrorMessage(new CompilerMessage(0, CompilerMessageSource.VIEW_PROPS, lr.getComponentId(), 0, 0, "Logical record " + lr.getName() + "[" + lr.getComponentId() + "] is not active"));
			}
			int le = lrb.getLookupExitId() != null ? lrb.getLookupExitId() : 0;
			lr.setLookupExitID(le);
			if(le > 0) {
				requiredExits.add(le);
			}
			lr.setLookupExitParams(getDefaultedString(lrb.getLookupExitParams(), ""));
			Repository.addLogicalRecord(lr);
			addLRFields(lrb);
			addIndexes(lrb);
			lrb.getLfs().keySet().stream().forEach(lf -> requiredLFs.add(lf));
	}

}
