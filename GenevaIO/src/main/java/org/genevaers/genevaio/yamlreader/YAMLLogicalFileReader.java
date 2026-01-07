package org.genevaers.genevaio.yamlreader;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;

public class YAMLLogicalFileReader extends YAMLReaderBase{

    private static Map<Integer, YAMLLogicalFileTransfer> lfBeans = new TreeMap<>();
    private static Map<String, YAMLLogicalFileTransfer> lfsByName = new TreeMap<>();
	private static Map<Integer, String> lfIdsToNames = new TreeMap<>();

	private static int maxid;
	
	private static YAMLLogicalFileTransfer ourTxf;

	private static Path lfPath;
    
    public void addRequiredToRepo() {
        requiredLFs.stream().forEach(lfid -> addLFtoRepo(environmentName, lfid));
    }

    public void addLFtoRepo(String environmentName, int lfid) {
		Path lfsPath = YAMLizer.getLFsPath();
		lfsPath.toFile().mkdirs();
		if(lfBeans.size() == 0) {
			queryAllLogicalFiles(environmentName);
		}
		// Path lfPath = lfsPath.resolve(lfBeans.get(lfid)+".yaml");
		// ourTxf = YAMLizer.readLf(lfPath.toFile());
        if(lfid >0) {
            YAMLLogicalFileTransfer lftxfr = lfBeans.get(lfid);
            LogicalFile lf = makeLF(lftxfr);
            Repository.getLogicalFiles().add(lf, lfid, lf.getName());
            //lftxfr.getPfs().values().stream().forEach(pf -> requiredPfNames.add(pf));
            lftxfr.getPfs().values().stream().forEach(pf -> getAndAddPf(pf, lf));
        }
    }

    private void getAndAddPf(String pf, LogicalFile lf) {
        YAMLPhysicalFileReader pFileReader = new YAMLPhysicalFileReader();
        pFileReader.addPfToRepo(pf);   
        lf.addPF(Repository.getPhysicalFiles().get(pf));    
    }

    private LogicalFile makeLF(YAMLLogicalFileTransfer lft) {
        LogicalFile lf = new LogicalFile();
        lf.setID(lft.getId());
        lf.setName(lft.getName());
        lf.setRequired(true);
        return lf;
    }

    private void addToResults(Map<Integer, String> result, File lf, String environmentName) {
		YAMLLogicalFileTransfer lft = YAMLizer.readLf(lf);
        if(lft != null) {
            if(lft.getId() > maxid) {
                maxid = lft.getId();
            }
            lfIdsToNames.put(lft.getId(), lft.getName());
            lfBeans.put(lft.getId(), lft);
            lfsByName.put(lft.getName(), lft);
        }
	}

	public void queryAllLogicalFiles(String environmentName) {
		Map<Integer, String> lfIdsToNames = new TreeMap<>();
		maxid = 0;
		Path lfsPath = YAMLizer.getLFsPath();
		lfsPath.toFile().mkdirs();
		File[] lfs = lfsPath.toFile().listFiles();	
		if(lfs.length > 0) {
			Stream.of(lfs)
		    	      .filter(file -> file.isFile())
		    	      .forEach(lf -> addToResults(lfIdsToNames, lf, environmentName));
		}
	}

   public void addLFtoRepo(String environmentName, String logicalFile) {
		if(lfsByName.size() == 0) {
			queryAllLogicalFiles(environmentName);
		}
        YAMLLogicalFileTransfer lftxfr = lfsByName.get(logicalFile);
        LogicalFile lf = makeLF(lftxfr);
        Repository.getLogicalFiles().add(lf, lftxfr.getId(), logicalFile);
        lftxfr.getPfs().values().stream().forEach(pf -> getAndAddPf(pf, lf));
    }

}
