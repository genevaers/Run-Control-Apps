package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;

public abstract class ComparisonGenerator extends ExtractRecordGenerator {
    protected FunctionSection section;
    protected String CFHeader;
    protected String cfSource;
    protected String predicate;
    protected int trueRow;
    protected int falseRow;
    protected String extractEntryString;
    protected boolean isOrCondition = false;
    protected boolean isAndCondition = false;
    protected static int level;

    public ComparisonGenerator(FunctionSection section) {
        this.section = section;
    }

    @Override
    public ExtractorEntry processRecord(LTRecord ltr) {
        //This is a base class for the CFEC and CFET generators which produce the conditional logic in the extract. It manages the gotos and scopes of the conditions. The actual code generation is done in the subclasses.
        //Basicaly the start of an if. Then we need the predicates and associated logic.
        getPredicateAndProcessFunctionCode(ltr);
        LTRecord trueRec = xlt.getFromPosition(trueRow);
        LTRecord falseRec = xlt.getFromPosition(falseRow);



        // if(isOrCondition) {
        //     buildORCondition(trueRec, falseRec, cfSource, predicate);
        //     CFHeader = String.format("%s\n//CFEC is part of an OR\n", cfSource);
        // } else if (isAndCondition) {
        //     buildANDCondition(trueRec, falseRec, cfSource, predicate);
        //     CFHeader = String.format("%s\n//CFEC is part of an AND\n", cfSource);
        // } else {
        //     buildCompareAndSetCondition(trueRec, falseRec, cfSource, predicate);
        //     CFHeader = String.format("%s\n//CFEC is not part of an OR or AND\n", cfSource);
        // }




        LTRecord lookAhead = xlt.getFromPosition(ltr.getRowNbr() + 1);

        if(section == FunctionSection.FILTER) {
            if(trueRec.getSuffixSeqNbr() == 1 || falseRec.getSuffixSeqNbr() == 1) {
                if(level == 0) {
                    String CFORIfFormat = "//%s\n        if( %s ) {\n            columnLogic(src, target, outWriter, numrecords);\n        }\n";
                    return new ExtractorEntry(String.format(CFORIfFormat, cfSource, predicate));
                } else {
                    return new ExtractorEntry(predicate);
                }
            } else {
                String CFECANDIfFormat = "//%s\n        if( %s ) {\n            columnLogic(src, target, outWriter, numrecords);\n        }\n";
                while(lookAhead != null && lookAhead.getSuffixSeqNbr() == 0) {
                    level++;
                    ExtractorEntry exe = addFunctionCode(lookAhead, filterRecs, section);
                    predicate += " && " + exe.getEntryString();
                    lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
                }
                currentRow = lookAhead.getRowNbr();
                // if(currentRow < lookAhead.getRowNbr()) {
                //     currentRow = lookAhead.getRowNbr() - 1;
                // }   
                level = 0;
                return new ExtractorEntry(String.format(CFECANDIfFormat, cfSource, predicate));
            }
        } else {
            while(lookAhead != null && lookAhead.getSuffixSeqNbr() != currentColumnNumber) {
                addFunctionCode(lookAhead, columnRecs, section);
                lookAhead = xlt.getFromPosition(lookAhead.getRowNbr() + 1);
            }
        }

        if(section == FunctionSection.FILTER) {
            //if we are in an OR add to it
            //if we are in an AND add to it
            //if neither work out which we have



        } else {
            CFHeader = String.format("%s\n//for column logic", cfSource);
        }
        return new ExtractorEntry(" ");
    }

    protected abstract void getPredicateAndProcessFunctionCode(LTRecord ltr);

    private String buildCompareAndSetCondition(LTRecord trueRec, LTRecord falseRec, String cfecSource, String predicate) {
        String CFHeader;
        if(trueRec.getSuffixSeqNbr() == 1 || falseRec.getSuffixSeqNbr() == 1) {
                CFHeader = String.format("%s\n//CF is start part of an OR\n", cfecSource);
                isOrCondition = true;
            } else {
                CFHeader = String.format("%s\n//CF is not start part of an OR\n", cfecSource);
            }
            if(trueRec.getFunctionCode().equals("ES") || falseRec.getFunctionCode().equals("ES")) {
                CFHeader = String.format("%s\n//CF is start part of an AND\n", cfecSource);
                isAndCondition = true;
            } else {
                CFHeader = String.format("%s\n//CF is not start part of an AND\n", cfecSource);
            }
        return CFHeader;
    }

    private void buildANDCondition(LTRecord trueRec, LTRecord falseRec, String cfecSource, String predicate) {
        String CFECANDIfFormat = "//%s\n        if( %s && ";
    }

    private ExtractorEntry buildORCondition(LTRecord trueRec, LTRecord falseRec, String cfecSource, String predicate) {
        String CFECORIfFormat = "//%s\n        if( %s ) {\n            columnLogic(src, target, outWriter, numrecords);\n        }\n";
        return new ExtractorEntry(String.format(CFECORIfFormat, cfecSource, predicate));
    }
}
