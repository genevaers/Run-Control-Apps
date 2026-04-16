package org.genevaers.compilers.extract.JavaEmitter.generators;

import java.util.Iterator;
import java.util.List;

import org.genevaers.genevaio.ltfile.LTRecord;
import org.genevaers.genevaio.ltfile.LogicTableF1;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.LookupPathKey;
import org.genevaers.repository.components.LookupPathStep;
import org.genevaers.repository.jltviews.ReferenceJoin;

import com.google.common.flogger.FluentLogger;

public class JoinGenerator extends ExtractRecordGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private int joinid;
    private int targLf;
    private int targLr;
    private String newid;
    private int trueGoto;
    private int falseGoto;
    private int keyLength;
    private int recLength;
    private int fileid;
    private LogicTableF1 join;
    private CodeSection section;
    private LTRecord lookAhead;

    public JoinGenerator(CodeSection section) {
        this.section = section;
    }

    public static void generateFilterCode() {
        filterRecs.add("/* Filter Lookups */");
        filterLookupIds.entrySet().stream().forEach(e -> {
            LookupInfo li = e.getValue();
            filterRecs.add(String.format("/* JOIN %s[%d]*/", li.getLookupName(), li.getLookupId()));
            generateLookupCode(filterRecs, li);
        });
    }

    public static void generateColumnLogicCode() {
        columnRecs.add("/* Precall Lookups */");
        columnLookupIds.entrySet().stream().forEach(e -> {
            LookupInfo li = e.getValue();
            columnRecs.add(String.format("/* JOIN %s[%d]*/", li.getLookupName(), li.getLookupId()));
            generateLookupCode(columnRecs, li);
        });
    }

    private static void generateLookupCode(List<String> recs, LookupInfo li) {
        recs.add(String.format("/* lookup %d code here*/", li.getLookupId()));
        LookupPath lkp = li.getLkast().getLookup();
        ReferenceJoin jltv = Repository.getJoinViews().getReferenceJLTViews().getJLTView(lkp.getTargetLRID(), false);

        String joinSource = String.format("Join %d -> %s targ LF %d LR %d", li.getLookupId(), jltv.getUniqueKey(),
                lkp.getTargetLFID(), lkp.getTargetLRID());
        logger.atInfo().log(joinSource);
        String joinEntry = String.format("//%s\n" +
                "        jn = JoinsRepo.getJoin(\"%s\");\n" +
                "        //Record count used for do again \n" +
                "        joinBuffer = jn.getBufferForRecord(numrecords);\n" +
                "        if(joinBuffer == null && jn.updateRequired()) {\n%s" +
                "            joinBuffer = jn.updateBuffer();\n" + 
                "        }\n" , joinSource, li.getNewid(), getLookupKeys(li));
        recs.add(joinEntry);
    }

    private static String getLookupKeys(LookupInfo li) {
        StringBuilder keycode = new StringBuilder();
        LookupPath lkp = li.getLkast().getLookup();
        Iterator<LookupPathStep> si = lkp.getStepIterator();
        while(si.hasNext()) {
            LookupPathStep step = si.next();
            Iterator<LookupPathKey> ki = step.getKeyIterator();
            while (ki.hasNext()) {
                LookupPathKey key = ki.next();
                if(key.getFieldId() != 0) {
                    LRField kf = Repository.getFields().get(key.getFieldId());
                    keycode.append(String.format("            //Key from field value %d\n", key.getFieldId()));
                    keycode.append(String.format("            jn.addToKey(src, %d, %d);\n", kf.getStartPosition()-1, kf.getLength()));
                } else if(key.getSymbolicName().length() > 0) {
                    keycode.append(String.format("            //Key from symbol %s\n", key.getSymbolicName()));
                } else {
                    keycode.append(String.format("            //Key from constant %d\n", key.getValue()));
                }
            }
        }
        return keycode.toString();
    }

    public int getJoinid() {
        return joinid;
    }
    
    public String getNewid() {
        return String.format("%d/%d", targLf, targLr);
    }

    public int getTargLf() {
        return targLf;
    }

    public int getTargLr() {
        return targLr;
    }

    public int getFalseRow() {
        return falseGoto;
    }

    public int getTrueeRow() {
        return trueGoto;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getRecLength() {
        return recLength;
    }

    public int getFileid() {
        return fileid;
    }

    @Override
    public void generateCode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateCode'");
    }
}

