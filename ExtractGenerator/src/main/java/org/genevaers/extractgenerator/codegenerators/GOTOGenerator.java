package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF0;

import com.google.common.flogger.FluentLogger;

public class GOTOGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private boolean generateElse;
    private int endScopeRow;
    private ExtractRecordGenerator from;

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF0 gotofc = (LogicTableF0)lt;
        gotofc.getGotoRow1();
        logger.atInfo().log("GOTO %d from %s", gotofc.getGotoRow1(), from.getLt().getFunctionCode());
        String exec;

        if(from.getLt().getFunctionCode().equals("JOIN")) {
            exec = "//GOTO join default\n        else {";
            endScopeRow = gotofc.getGotoRow1()-1;
        } else {
            exec = String.format("//GOTO %d from %s\n     } else {", gotofc.getGotoRow1(), from.getLt().getFunctionCode());  
            endScopeRow = gotofc.getGotoRow1()-1;
        }
        return new ExtractorEntry(exec);
    }

    public void generateElse() {
        generateElse = true;
    }

    public int getEndScopeRow() {
        return endScopeRow;
    }

    public void setFrom(ExtractRecordGenerator gotoFrom) {
        from = gotoFrom;
    }

}
