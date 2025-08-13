package org.genevaers.extractgenerator.codegenerators;

import java.util.List;
import java.util.stream.Collectors;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableRE;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalFile;

import com.google.common.flogger.FluentLogger;

public class RENXGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private List<String> inputs;

    @Override
    public ExtractorEntry processRecord(LTRecord lt)  {
        LogicTableRE renx = (LogicTableRE)lt;
        LogicalFile lf = Repository.getLogicalFiles().get(renx.getFileId());
        //Create a list of input ddnames
        inputs = lf.getPFs().stream().map(pf -> pf.getInputDDName()).collect(Collectors.toList());
        logger.atInfo().log("RENX read from file id %d", renx.getFileId());
        return new ExtractorEntry(inputs.toString());
    }

    public List<String> getInputs() {
        return inputs;
    }
    
}
