package org.genevaers.compilers.extract.JavaEmitter.generators;


import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.extract.JavaEmitter.ExtractorEntry;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.LookupPathAST;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LookupPath;

import com.google.common.flogger.FluentLogger;

public class ViewSourceGenerator extends ExtractRecordGenerator {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private ViewSourceAstNode vst;
    private Map<Integer, LookupInfo> currenLookupIds = filterLookupIds;
    private boolean inColumn = false;

    public ViewSourceGenerator(ViewSourceAstNode vsAST) {
        this.vst = vsAST;
    };

    @Override
    public void generateCode() {
        filterRecs.add("/* View " + vst.getViewSource().getViewId() + " Source " + vst.getViewSource().getSequenceNumber() + " */");
        logger.atInfo().log("Looking ahead for JOINS");
        //Want to separate out the extract filter lookups
        lookAheadForJoins(vst, 0, false);
        logJoins();
        generateFromChildNodes(vst);
        outputLength = vst.getAreaValues().getDtLen();
        lrLength = Repository.getLRLength(vst.getViewSource().getSourceLRID());
        logger.atInfo().log("View source output length %d", outputLength);
        //HACK!!!!
        columnRecs.add(String.format("            outWriter.getRecordToFill().bytes.position(%d);\n" + //
                        "            outWriter.writeAndClearTheRecord();\n", outputLength));
     }

    //Want the levels to check for hidden?
    //Probably better to loo at parents?
    //As it is the first entry will determin the level. If found again we lose that knowledge - or overwrite the previous?
    //If we use the parent(s) we need to not overwrite the hidden... key is we want to find looksup that are always used.
    private void logJoins() {
        logger.atInfo().log("Filter JOINS");
        filterLookupIds.entrySet().stream().forEach(e -> {
            LookupInfo li = e.getValue();
            logger.atInfo().log("Found a JOIN %s[%d] at level %d hidden=%s", li.getLookupName(), li.getLookupId(), li.getLevel(), li.isHidden());
        });
        logger.atInfo().log("Column logic JOINS");
        columnLookupIds.entrySet().stream().forEach(e -> {
            LookupInfo li = e.getValue();
            logger.atInfo().log("Found a JOIN %s[%d] at level %d hidden=%s", li.getLookupName(), li.getLookupId(), li.getLevel(), li.isHidden());
        });
        logger.atInfo().log("Hidden JOINS");
        hiddenLookupIds.entrySet().stream().forEach(e -> {
            LookupInfo li = e.getValue();
            logger.atInfo().log("Found a JOIN %s[%d] at level %d hidden=%s", li.getLookupName(), li.getLookupId(), li.getLevel(), li.isHidden());
        });
    }

    //Do we need to separate the filter and column lookups?
    private void lookAheadForJoins(ExtractBaseAST node, int depth, boolean ifFound) {
        Iterator<ASTBase> ci = node.getChildIterator();
        while(ci.hasNext()) {
            ExtractBaseAST n = (ExtractBaseAST)ci.next();
            boolean newIfFound = ifFound || n.getType() == Type.IFNODE;
            if(n.getType() == Type.VIEWCOLUMNSOURCE) {
                currenLookupIds = columnLookupIds;
            }
            if(n.getType() == Type.LOOKUPREF) {
                LookupPathAST lkast = (LookupPathAST) n;
                saveLookupInfo(node, lkast, depth, ifFound);
            } else if(n.getType() == Type.IFNODE) {
                newIfFound = true;
            } else if(n.getType() == Type.LOOKUPFIELDREF) {
                LookupFieldRefAST lkf = (LookupFieldRefAST) n;
                saveLookupInfo(node, lkf, depth, ifFound);
            }
            lookAheadForJoins(n, depth + 1, newIfFound);
        }
    }

    private void saveLookupInfo(ExtractBaseAST parent, LookupPathAST lkast, int depth, boolean ifFound) {
        //just use the id as the key and a computeifabsent function that overrides the hidden value if it is already found as false?
        //or have 2 maps one for hidden and one for not hidden
        //Then juggle
       LookupInfo lki = joins.computeIfAbsent(lkast.getNewJoinId(), id -> addJoin(lkast, depth));
       if(parent.getType() == Type.COLUMNASSIGNMENT && ifFound) {
            lki.setHidden(ifFound);
            hiddenLookupIds.computeIfAbsent(lkast.getNewJoinId(), j -> lki);
        } else {
            currenLookupIds.computeIfAbsent(lkast.getNewJoinId(), j -> lki);
        }

    }

    private LookupInfo addJoin(LookupPathAST lkast, int depth) {
        return new LookupInfo(lkast, depth);
     }
}
