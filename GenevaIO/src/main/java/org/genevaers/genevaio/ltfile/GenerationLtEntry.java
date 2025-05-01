package org.genevaers.genevaio.ltfile;

public class GenerationLtEntry extends LtRecordLogger {

    @Override
    public String getLogEntry(LTRecord ltr) {
        LogicTableGeneration g = (LogicTableGeneration)ltr; 
        return String.format(GEN_FORMAT, g.isExtract() ? "Extract" : "Join",
                g.isIsAscii() ? "ASCII" : "EBCDIC",
                g.getReccnt(), g.getBytecnt(), g.getHdCnt(), g.getNvCnt(), g.getF0Cnt(), g.getF1Cnt(),
                g.getF2Cnt(), g.getReCnt(), g.getWrCnt(), g.getCcCnt(), g.getNameCnt(), g.getNamevalueCnt(),
                g.getCalcCnt(), g.getNamef1Cnt(), g.getNamef2Cnt(), g.getDateCc(), g.getDateYy(),
                g.getDateMm(), g.getDateDd(), 0, 0, 0);
    }

}
