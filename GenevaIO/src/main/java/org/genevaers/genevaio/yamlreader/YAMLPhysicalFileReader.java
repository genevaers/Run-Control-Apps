package org.genevaers.genevaio.yamlreader;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.genevaers.genevaio.dbreader.DatabaseConnection;
import org.genevaers.genevaio.dbreader.DatabaseConnectionParams;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.PhysicalFile;
import org.genevaers.repository.components.enums.AccessMethod;
import org.genevaers.repository.components.enums.DbmsRowFmtOptId;
import org.genevaers.repository.components.enums.FieldDelimiter;
import org.genevaers.repository.components.enums.FileRecfm;
import org.genevaers.repository.components.enums.FileType;
import org.genevaers.repository.components.enums.RecordDelimiter;
import org.genevaers.repository.components.enums.TextDelimiter;

public class YAMLPhysicalFileReader extends YAMLReaderBase {

    private static Map<String, PhysicalFileTransfer> pfsByName = new TreeMap<>();

    public void addToRequiredRepo() {
        requiredPfNames.stream().forEach(pf -> addPfToRepo(pf));
    }

	public void addPfToRepo(String pfName) {
        if(pfsByName.isEmpty()) {
            queryAllPhysicalFiles();
        }
        PhysicalFileTransfer pftxfr = pfsByName.get(pfName);
        makePF(pftxfr);
    }

    private void makePF(PhysicalFileTransfer pftxfr) {
        PhysicalFile pf = new PhysicalFile();
        pf.setAccessMethod(pftxfr.getAccessMethodCode() != null ? AccessMethod.fromdbcode(pftxfr.getAccessMethodCode()) : AccessMethod.SEQUENTIAL);
        pf.setComponentId(pftxfr.getId());
        pf.setDataSetName(pftxfr.getDatasetName());
        pf.setDatabase(pftxfr.getSubSystem() != null ? pftxfr.getSubSystem() : "");
        pf.setExtractDDName(pftxfr.getInputDDName());
        pf.setFileType(FileType.fromdbcode(pftxfr.getFileTypeCode()));
        pf.setInputDDName(pftxfr.getInputDDName() != null ? pftxfr.getInputDDName() : "");
        pf.setName(pftxfr.getName());
        pf.setOutputDDName(pftxfr.getOutputDDName() != null ? pftxfr.getOutputDDName() : "");
        pf.setReadExitID(pftxfr.getReadExitId() != null ? pftxfr.getReadExitId() : 0);
        pf.setReadExitIDParm(pftxfr.getReadExitParams() != null ? pftxfr.getReadExitParams() : "");
        pf.setRecfm(pftxfr.getRecfm() != null ? FileRecfm.fromdbcode(pftxfr.getRecfm()) : FileRecfm.VB);
        pf.setMaximumLength((short)pftxfr.getMaxRecordLen());
        pf.setMinimumLength((short)pftxfr.getMinRecordLen());
        pf.setDataSetName("");
        pf.setExtractDDName("");
					pf.setFieldDelimiter(FieldDelimiter.FIXEDWIDTH);
					pf.setRecordDelimiter(RecordDelimiter.CR);
					pf.setTextDelimiter(TextDelimiter.INVALID);
					pf.setDatabaseRowFormat(DbmsRowFmtOptId.DB2);
        Repository.addPhysicalFileOnly(pf);
    }

    public void queryAllPhysicalFiles() {
		Path pfsPath = YAMLizer.getPFsPath();
		pfsPath.toFile().mkdirs();
		File[] pfs = pfsPath.toFile().listFiles();
		
		if(pfs.length > 0) {
			Stream.of(pfs)
		    	      .filter(file -> file.isFile())
		    	      .forEach(pf -> addToMap(pf));
		}
	}

	private void addToMap(File pf) {
		PhysicalFileTransfer pft = YAMLizer.readYamlPf(pf.toPath());
        if(pft != null) {
            pfsByName.put(pft.getName(), pft);
        }
	}
	

	// public PhysicalFileTransfer getPhysicalFile(Integer id,	Integer environmentId) throws DAOException {
	// 	PhysicalFileTransfer result = null;
	// 	Path pfsPath = YAMLizer.getPFsPath();
	// 	pfsPath.toFile().mkdirs();
	// 	if(pfBeans.size() == 0) {
	// 		queryAllPhysicalFiles(environmentId, null);
	// 	}
	// 	Path crPath = pfsPath.resolve(pfBeans.get(id).getName()+".yaml");
	// 	result = (PhysicalFileTransfer) YAMLizer.readYaml(crPath, ComponentType.PhysicalFile);
	// 	return result;
	// }

	// public PhysicalFileTransfer getPhysicalFile(String name, Integer environmentId) throws DAOException {
	// 	PhysicalFileTransfer result = null;
	// 	Path pfsPath = YAMLizer.getPFsPath();
	// 	pfsPath.toFile().mkdirs();
	// 	Path crPath = pfsPath.resolve(name+".yaml");
	// 	result = (PhysicalFileTransfer) YAMLizer.readYaml(crPath, ComponentType.PhysicalFile);
	// 	return result;
	// }


}
