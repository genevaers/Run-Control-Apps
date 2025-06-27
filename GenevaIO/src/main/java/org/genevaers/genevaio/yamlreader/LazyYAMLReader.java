package org.genevaers.genevaio.yamlreader;

import java.util.Map;

import org.genevaers.genevaio.dataprovider.CompilerDataProvider;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.ViewNode;

public class LazyYAMLReader implements CompilerDataProvider {
    private int environmentId;
    private String environmentName;
    private int sourceLogicalRecordID;

    @Override
    public Integer findExitID(String string, boolean procedure) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findExitID'");
    }

    @Override
    public Integer findPFAssocID(String lfName, String pfName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findPFAssocID'");
    }

    @Override
    public Map<String, Integer> getFieldsFromLr(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFieldsFromLr'");
    }

    @Override
    public Map<String, Integer> getLookupTargetFields(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLookupTargetFields'");
    }

    @Override
    public void setEnvironmentID(int environmentId) {
       this.environmentId =  environmentId;
    }

    @Override
    public int getEnvironmentID() {
        return environmentId;
    }

    @Override
    public void setLogicalRecordID(int lrid) {
        sourceLogicalRecordID = lrid;
    }

    @Override
    public LogicalRecord getLogicalRecord(int id) {
       //Uncomment me for more info Repository.dumpLRs();
        LogicalRecord lr = Repository.getLogicalRecords().get(id);
        if(lr == null) {
          loadLR(id);
          lr = Repository.getLogicalRecords().get(id);
        } 
        if(lr.getLookupExitID() > 0) {
            YAMLExitReader exitReader = new YAMLExitReader();
            exitReader.addToRepoById(lr.getLookupExitID());              
        }
        return lr;
    }

    private void loadLR(int id) {
       YAMLLogicalRecordReader lrReader = new YAMLLogicalRecordReader();
        lrReader.addLRToRepo(environmentName, id);
        getDependenciesForLookup(null);
    }

    @Override
    public void loadLR(int environmentID, int sourceLR) {
        //We'll need to have loaded the LRs like in WB -> some sort of init
        YAMLLogicalRecordReader lrReader = new YAMLLogicalRecordReader();
        lrReader.addLRToRepo(environmentName, sourceLR);
     }

    @Override
    public LookupPath getLookup(String name) {
        LookupPath lk = Repository.getLookups().get(name);
        if(lk == null) {
          loadLookup(environmentName, name);
          lk = Repository.getLookups().get(name);
        } 
        getDependenciesForLookup(lk);
        return lk;
    }
   private void getDependenciesForLookup(LookupPath lk) {
        //Need to have build up a list of required LRs.
        //Then below gets them.
        YAMLLogicalRecordReader lrReader = new YAMLLogicalRecordReader();
        lrReader.addRequiredToRepo(environmentName);
        YAMLLogicalFileReader lfReader = new YAMLLogicalFileReader();
        lfReader.addRequiredToRepo();
        YAMLPhysicalFileReader pFileReader = new YAMLPhysicalFileReader();
        pFileReader.addToRequiredRepo();
        // YAMLExitReader er = new YAMLExitReader();
        // er.addToRepo();
    }

    // private LogicalFile getLogicalFile(int lfid) {
    //     LogicalFile lf = Repository.getLogicalFiles().get(lfid);
    //     if(lf == null) {
    //         loadLogicalFile(lfid);
    //         lf = Repository.getLogicalFiles().get(lfid);
    //     }
    //     return lf;
    // }


    // private void loadLogicalFile(int lfid) {
    //     YAMLLogicalFileReader lfReader = new YAMLLogicalFileReader();
    //     lfReader.addLFtoRepo(environmentId, lfid);
    // }

    private void loadLookup(String environmentName, String name) {
        YAMLLookupsReader lkReader = new YAMLLookupsReader();
        lkReader.addNamedLookupToRepo(environmentName, name);

    }


    @Override
    public ViewNode getView(int id) {
        ViewNode vw = Repository.getViews().get(id);
        if(vw == null) {
          loadView(environmentId, id);
          vw = Repository.getViews().get(id);
        } 
        return vw;
    }

    private void loadView(int environmentId2, int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadView'");
    }

    @Override
    public void setEnvironmentName(String n) {
        environmentName = n;
    }

    @Override
    public String getEnvironmentName() {
        return environmentName;
    }

    
}
