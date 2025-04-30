package org.genevaers.genevaio.ltfile;

public class HdEntry extends LtRecordLogger {

  @Override
  public String getLogEntry(LTRecord ltr, DescriptionKey descriptionRoot) {
    return String.format(LEAD_IN, ltr.getRowNbr(), ltr.getSuffixSeqNbr(), ltr.getFunctionCode());
  }

}
