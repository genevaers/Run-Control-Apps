package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF1;

import com.google.common.flogger.FluentLogger;

public class CFECGenerator extends ExtractRecordGenerator{
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    int trueGoto;
    int falseGoto;

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        //Need the manage the start and end hava a pending goto
        //Collect somehow and when the targer row is reached we add the closing }
        //How to we recognise an ELSE... because there is a goto there?
        //Goto will be the row before..
        LogicTableF1 cfec = (LogicTableF1)lt;
        LogicTableArg arg = cfec.getArg();
        logger.atInfo().log("CFEC from pos %d len %d %s %s", arg.getStartPosition()-1,  arg.getFieldLength(), cfec.getCompareType(), arg.getValue().getPrintString());
        trueGoto = cfec.getGotoRow1();
        falseGoto = cfec.getGotoRow2();
        this.lt = lt;
        return new ExtractorEntry(
        String.format("        if(new String(src, %d , %d).equals(\"%s\")) {", arg.getStartPosition()-1,  arg.getFieldLength(), arg.getValue().getPrintString()));
    }

    public int getFalseRow() {
        return falseGoto;
    }

    public int getTrueGoto() {
        return trueGoto;
    }

}
