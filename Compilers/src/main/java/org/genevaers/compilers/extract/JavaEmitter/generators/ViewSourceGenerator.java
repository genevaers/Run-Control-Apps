package org.genevaers.compilers.extract.JavaEmitter.generators;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.LookupPathAST;
import org.genevaers.compilers.extract.astnodes.ViewSourceAstNode;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.ComponentNode;
import org.genevaers.repository.components.FieldPositionComparator;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.jltviews.JLTView;

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
        generateSourceLRFields(vst);
        generateViewColumnFields(vst);
        generateLookupFields(vst);
        generateFromChildNodes(vst);
        outputLength = vst.getAreaValues().getDtLen();
        lrLength = Repository.getLRLength(vst.getViewSource().getSourceLRID());
        logger.atInfo().log("View source output length %d", outputLength);
        //HACK!!!!
        columnRecs.add(String.format("            outWriter.getRecordToFill().bytes.position(%d);\n" + //
                        "            outWriter.writeAndClearTheRecord();\n", outputLength));
     }

    private void generateLookupFields(ViewSourceAstNode vst) {
        columnLookupIds.entrySet().stream().forEach(e -> addLookupFieldHolder(e));
    }

    private void  addLookupFieldHolder(Entry<Integer, LookupInfo> e) {
        String lkname = e.getValue().getLkast().getLookup().getName();
        LogicalRecord redLR = Repository.getLogicalRecords().get(JLTView.JOINVIEWBASE + e.getValue().getLookupId());
        Iterator<LRField> rfi = redLR.getIteratorForFieldsByID();
        List<LRField> fieldsByPosition = new ArrayList<>();
        while(rfi.hasNext()) {
            fieldsByPosition.add(rfi.next());
        }
        FieldPositionComparator fpc = new FieldPositionComparator();
        Collections.sort(fieldsByPosition, fpc);

        lookupFieldHolders = new LinkedHashMap<>();
        Iterator<LRField> fbpi = fieldsByPosition.iterator();
        while (fbpi.hasNext()) {
            LRField lrf = fbpi.next();
            String lkfldName = lkname + "_" + lrf.getName();
            addFieldToHolders(lkfldName, lrf, lrf.getDatatype(), lrf.getLength(), lrf.isSigned(), lrf.getNumDecimalPlaces(), lookupFieldHolders);
        }
        lookupHoldersByName.put(lkname, lookupFieldHolders);
    }

    private void addFieldToHolders(String name, ComponentNode node, DataType dataType, short length, boolean signed, int numDecimals, Map<String, ComponentFieldHolder> holders) {
        switch(dataType) {
            case ALPHA:
                break;
            case ALPHANUMERIC: {
                //To cater for redefines we will need to set the offset directly - or correct for redefines
                //Detect redefines as we iterate through the fields?
                ComponentFieldHolder cfh = new ComponentFieldHolder(node);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d)", name, length));
                holders.put(name, cfh);
                break;
            }
            case BCD:
                break;
            case BINARY:
                addBinaryField(name, node, length, signed, holders);
                break;
            case BSORT:
                break;
            case CONSTDATE:
                break;
            case CONSTNUM:
                break;
            case CONSTSTRING:
                break;
            case EDITED: {
                ComponentFieldHolder cfh = new ComponentFieldHolder(node);
                cfh.setAccessor("getString");
                cfh.setDefinition(String.format("private static final StringField %s = factory.getStringField(%d); //For Edited Numeric", name, length));
                holders.put(name, cfh);
                break;
            }
            case FLOAT:
                break;
            case GENEVANUMBER:
                break;
            case INVALID:
                break;
            case MASKED:
                break;
            case PACKED: {
                addPackedField(name, node, length, signed, numDecimals, holders);
                break;
            }
            case PSORT:
                break;
            case ZONED:
                break;
            default: {
                ComponentFieldHolder cfh = new ComponentFieldHolder(node);
                cfh.setAccessor("Default");
                cfh.setDefinition(String.format("//private static final TBD %s = factory.getStringField(%d)", name, length));
                holders.put(name, cfh);
                break;
            }
            
        }
    }

    private void generateViewColumnFields(ViewSourceAstNode vst) {
        ViewNode view = Repository.getViews().get(vst.getViewSource().getViewId());
        Iterator<ViewColumn> ci = view.getColumnIterator();
        while(ci.hasNext()) {
            ViewColumn col = ci.next();
            addFieldToHolders("COL_" + col.getColumnNumber(), col, col.getDataType(), col.getFieldLength(), col.isSigned(), col.getDecimalCount(), columnFieldHolders);
        }
    }

    private void generateSourceLRFields(ViewSourceAstNode vst) {
        ViewSource vs = vst.getViewSource();
        LogicalRecord lr = Repository.getLogicalRecords().get(vs.getSourceLRID());
        //We really want the fields sorted by position
        
        List<LRField> fieldsByPosition = new ArrayList<>();
        Iterator<LRField> rfi = lr.getIteratorForFieldsByID();
        while(rfi.hasNext()) {
            fieldsByPosition.add(rfi.next());
        }
        FieldPositionComparator fpc = new FieldPositionComparator();
        Collections.sort(fieldsByPosition, fpc);

        Iterator<LRField> fbpi = fieldsByPosition.iterator();
        while (fbpi.hasNext()) {
            LRField lrf = fbpi.next();
            addFieldToHolders(lrf.getName(), lrf, lrf.getDatatype(), lrf.getLength(), lrf.isSigned(), lrf.getNumDecimalPlaces(), sourceFieldHolders);
        }
    }

    private void addBinaryField(String name, ComponentNode node, short length, boolean signed, Map<String, ComponentFieldHolder> holders) {
        ComponentFieldHolder cfh = new ComponentFieldHolder(node);
        if ((length <= 0 || length >= 4) && (length != 4 || !signed)) {
            if (length <= 8) {
                cfh.setAccessor("getLong");
                cfh.setDefinition(String.format("private static final BinaryAsLongField %s = factory.getBinaryAsLongField(%d, %b)", name, length, signed));
            } else if (length > 8) {
                cfh.setAccessor("getBigInteger");
                cfh.setDefinition(String.format("private static final BinaryAsBigIntegerField %s = factory.getBinaryAsBigIntegerField(%d, %b)", name, length, signed));
            } else {
                cfh.setAccessor("illegal length");
            }
        } else {
                cfh.setAccessor("getInt");
                cfh.setDefinition(String.format("private static final BinaryAsIntField %s = factory.getBinaryAsIntField(%d, %b)", name, length, signed));
        }
        holders.put(name, cfh);
    }

    private void addStringFieldtoDefinitions(LRField f, List<String> defs) {
        defs.add(String.format("private static final StringField %s = factory.getStringField(%d);", f.getName(), f.getLength()));
    }
    private String getStringFieldDefinitions(LRField f) {
        return String.format("private static final StringField %s = factory.getStringField(%d);", f.getName(), f.getLength());
    }

    private void addPackedField(String name, ComponentNode node, short length, boolean signed, int numDecimals, Map<String, ComponentFieldHolder> holders) {
        // private static final PackedDecimalAsIntField AdmissionDate =
        // factory.getPackedDecimalAsIntField(7, true);
        // Different lengths mean use different converters
        int precision = length * 2 - 1;
        ComponentFieldHolder fh = new ComponentFieldHolder(node);
        if (numDecimals > 0) {
            fh.setAccessor("getBigDecimal");
            fh.setDefinition(String.format("private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b);", name, length, numDecimals, signed));
        } else if (numDecimals < 0) {
            fh.setAccessor("getBigDecimal");
            fh.setDefinition(String.format("private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b);", name, length, numDecimals, signed));
        } else if (precision <= 9) {
            fh.setAccessor("getInt");
            fh.setDefinition(String.format("private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, %b);", name, length, signed));
        } else if (precision <= 18) {
            fh.setAccessor("getLong");
            fh.setDefinition(String.format("private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, %b);", name, length, signed));
        } else if (precision <= 31) {
            fh.setAccessor("getBigInteger");
            fh.setDefinition(String.format("private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, %b);", name, length, numDecimals, signed));
        } else {
            fh.setAccessor("length too long");
        }
        holders.put(name, fh);
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
