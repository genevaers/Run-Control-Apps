package org.genevaers.extractgenerator.codegenerators;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableArg;
import org.genevaers.genevaio.ltfile.LogicTableF2;

import com.google.common.flogger.FluentLogger;

public class DTEGenerator extends ExtractRecordGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static int aggSourceStart = -1;
    private static int aggTargetStart = -1;
    private static int lastTargetEnd = -1;
    private static int lastSourceEnd = -1;

    @Override
    public ExtractorEntry processRecord(LTRecord lt) {
        LogicTableF2 dte = (LogicTableF2) lt;
        LogicTableArg arg1 = dte.getArg1();
        LogicTableArg arg2 = dte.getArg2();
        // As an optimisation we can aggregate adjacent LR fields into adjacent columns
        // in the target
        // So keep track of the end position of the last field added

        //if not already in an aggregation and this DTE is adjacent to the last one added, we can aggregate
        if (!dteAggregationInProgress && lastSourceEnd == arg1.getStartPosition() && lastTargetEnd == arg2.getStartPosition()) {
            if (aggSourceStart == -1) {
                aggSourceStart = arg1.getStartPosition();
                aggTargetStart = arg2.getStartPosition();
            }
            logger.atInfo().log("DTE aggregation from pos %d len %d to pos %d len %d", arg1.getStartPosition(),
                    arg1.getFieldLength(), arg2.getStartPosition(), arg2.getFieldLength());
            dteAggregationInProgress = true;
        } else {
            // If we were aggregating and this DTE is not adjacent, we need to end the aggregation
            if (lastSourceEnd + 1 == arg1.getStartPosition() && lastTargetEnd + 1 == arg2.getStartPosition()) {
                // write aggregated DTE
                //need to replace previous entry in exrecs with aggregated one
                return getAggregatedDTE();  
            }
        }
        lastSourceEnd = arg1.getStartPosition() + arg1.getFieldLength();
        lastTargetEnd = arg2.getStartPosition() + arg2.getFieldLength();
        // Need to trigger end of aggregation in the Extractor if aggregation was in
        // progress but this DTE is not aggregating
        if (!dteAggregationInProgress) {
            String dteSource = String.format("(%d)DTE from pos %d len %d to pos %d len %d", lt.getRowNbr(), arg1.getStartPosition(),
                    arg1.getFieldLength(), arg2.getStartPosition(), arg2.getFieldLength());
            logger.atInfo().log(dteSource);
            return new ExtractorEntry(
                    String.format("//%s\n        target.put(src, %d, %d);", dteSource, arg1.getStartPosition() - 1,
                            arg1.getFieldLength()));
        } else {
            return null;
        }
    }

    public static ExtractorEntry getAggregatedDTE() {
        logger.atInfo().log("Ending DTE aggregation at source pos %d target pos %d", lastSourceEnd, lastTargetEnd);
        String dteSource = String.format("Aggregate DTE from pos %d len %d to pos %d len %d", aggSourceStart, lastSourceEnd - aggSourceStart, aggTargetStart, lastTargetEnd - aggTargetStart);
        logger.atInfo().log(dteSource);
        ExtractorEntry ee = new ExtractorEntry(
                String.format("//%s\n        target.put(src, %d, %d);", dteSource, aggSourceStart - 1,
                        lastSourceEnd - aggSourceStart));
        dteAggregationInProgress = false;
        aggSourceStart = -1;
        aggTargetStart = -1;
        lastTargetEnd = -1;
        lastSourceEnd = -1;
        return ee;
    }

}
