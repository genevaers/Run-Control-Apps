package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.CalculationAST;
import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;

import com.google.common.flogger.FluentLogger;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ColumnFieldHolder;
import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;

public class ColumnAssignmentGenerator extends ExtractRecordGenerator {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private ColumnAssignmentASTNode ca;
    private ExtractRecordGenerator srcgen;
    private ExtractRecordGenerator trggen;
    private ExtractBaseAST src;
    private ExtractBaseAST trg;

    public ColumnAssignmentGenerator(ColumnAssignmentASTNode node) {
        this.ca = node;
    }

    @Override
    public void generateCode() {
        src = (ExtractBaseAST) ca.getChild(0);
        trg = (ExtractBaseAST) ca.getChild(1);
        srcgen = getcodeGenerator(src);
        trggen = getcodeGenerator(trg);
        // Make the format string dependent on the operator and types.
        columnRecs.add(getAssignmentFormatString());
    }

    @Override
    public String getCode(ExtractBaseAST node) {
        src = (ExtractBaseAST) node.getChild(0);
        trg = (ExtractBaseAST) node.getChild(1);
        srcgen = getcodeGenerator(src);
        trggen = getcodeGenerator(trg);
        // Make the format string dependent on the operator and types.
        return getAssignmentFormatString();
    }

    private String getAssignmentFormatString() {

        /*
        switch on target type DT CT SK 
        Then for each swtich on source type LR, LKLR, Const, Arithmetic
         */
        switch (trg.getType()) {
            case DT_COLUMN:
                return getDtAssignment();
            case CT_COLUMN:
                return "// WARNING: CT columns not supported in Java emitter";
            case SK_COLUMN:
                return "// WARNING: SK columns not supported in Java emitter";
            default:
                return "// WARNING: unknown assignment target type " + trg.getType();
        }
    }

    private String getDtAssignment() {
        switch(src.getType()) {
            case LRFIELD:
                return dteEquivalentBasedOnTypes();
            case LOOKUPFIELDREF:
                return dtlEquivalentBasedOnTypes();
            case STRINGATOM:
                return dtcString();
            case NUMATOM:
                return dtcNumeric();
            case CALCULATION:
                return dtCalculationAssignment();
            default:
                return "// WARNING: unsupported source type " + src.getType() + " in column assignment";
        }
    }

    private String dtCalculationAssignment() {
        ColumnAST col = (ColumnAST) trg;
        ColumnFieldHolder cfh = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
        CalculationGenerator calcgen = new CalculationGenerator((CalculationAST) src);
        String calcExpr = calcgen.getCode(src);
        int len = col.getViewColumn().getFieldLength();
        int dec = col.getViewColumn().getDecimalCount();
        String valueArg = buildAssignmentValue(cfh, calcExpr, len, dec);
        String assignment = String.format("                %s(%s, target);",
                cfh.getAssignmentTarget(), valueArg);

        // If the calculation references any lookup field, wrap the assignment in a
        // null-check guard — but only for buffers not already guarded by the enclosing
        // if-predicate (tracked in guardedJoinBuffers).
        Set<String> joinBuffers = collectJoinBufferNames(src);
        joinBuffers.removeAll(guardedJoinBuffers);
        if (!joinBuffers.isEmpty()) {
            String condition = String.join(" != null && ", joinBuffers) + " != null";
            String elseBody = getElseBody(col);
            return String.format("        if(%s) {\n        %s\n        } else {\n        %s\n        }",
                    condition, assignment, elseBody);
        }
        return assignment;
    }

    /**
     * Walk the calculation AST tree and collect the joinBuffer variable name for
     * every LOOKUPFIELDREF operand found (breadth-first, insertion-ordered).
     */
    private Set<String> collectJoinBufferNames(ExtractBaseAST node) {
        Set<String> buffers = new LinkedHashSet<>();
        collectJoinBufferNamesRecursive(node, buffers);
        return buffers;
    }

    private void collectJoinBufferNamesRecursive(ExtractBaseAST node, Set<String> buffers) {
        if (node == null) return;
        if (node.getType() == Type.LOOKUPFIELDREF) {
            LookupFieldRefAST lfr = (LookupFieldRefAST) node;
            buffers.add("joinBuffer" + lfr.getNewJoinId());
        }
        Iterator<ASTBase> ci = node.getChildIterator();
        while (ci.hasNext()) {
            collectJoinBufferNamesRecursive((ExtractBaseAST) ci.next(), buffers);
        }
    }

    private String dtcString() {
        ColumnAST col = (ColumnAST) trg;
        StringAtomAST sa = (StringAtomAST) src;
        ColumnFieldHolder cfh = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
        int fieldLen = col.getViewColumn().getFieldLength();
        String rawValue = sa.getValue();
        String padded = rawValue.isEmpty()
            ? String.format("%-" + fieldLen + "s", " ")
            : String.format("%-" + fieldLen + "s", rawValue);
        return String.format("                %s(\"%s\", target);", cfh.getAssignmentTarget(), padded);
    }

    private String dtcNumeric() {
        ColumnAST col = (ColumnAST) trg;
        NumAtomAST na = (NumAtomAST) src;
        ColumnFieldHolder cfh = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
        int len = col.getViewColumn().getFieldLength();
        int dec = col.getViewColumn().getDecimalCount();
        String valueArg = buildAssignmentValue(cfh, na.getValueString(), len, dec);
        return String.format("                %s(%s, target);",
                cfh.getAssignmentTarget(), valueArg);
    }

    private String dtlEquivalentBasedOnTypes() {
        ColumnAST col = (ColumnAST) trg;
        LookupFieldRefAST lfr = (LookupFieldRefAST) src;
        LRField fld = lfr.getRef();
        // This will be dependent on the type of the field, for now we will assume all
        // fields are strings and use the String
        // We need the key length since the record start after the key in the join
        // buffer?
        LRField redField = Repository.getREDfieldFrom(lfr.getLookup(), fld);
        if (redField == null) {
            logger.atSevere().log("Unable to find reference field for lookup field reference %s", fld.getName());
            return "/* Unable to find reference field for lookup field reference " + fld.getName() + " */";
        }
        String joinBufString = "joinBuffer" + lfr.getNewJoinId();
        String joinLogicFormat = "        if(" + joinBufString
                + " != null) {\n        %s\n        } else {\n        %s\n        }";
        String name = lfr.getLookup().getName() + "_" + redField.getName();
        String body = dtlEquivalentBasedOnTypes(joinBufString, redField, name);
        String elseBody = getElseBody(col);
        return String.format(joinLogicFormat, body, elseBody);
    }

    private String getElseBody(ColumnAST col) {
        ColumnFieldHolder cfh = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
        ViewColumn vc = col.getViewColumn();
        String accessor = cfh != null ? cfh.getAccessor() : "putString";
        switch (accessor) {
            case "putBigDecimal":
                return String.format("                COL_%d.putBigDecimal(BigDecimal.ZERO, target);", vc.getColumnNumber());
            case "putInt":
                return String.format("                COL_%d.putInt(0, target);", vc.getColumnNumber());
            case "putLong":
                return String.format("                COL_%d.putLong(0L, target);", vc.getColumnNumber());
            case "putBigInteger":
                return String.format("                COL_%d.putBigInteger(BigInteger.ZERO, target);", vc.getColumnNumber());
            default:
                return String.format("                COL_%d.putString(String.format(\"%%-%ds\", \" \"), target);", vc.getColumnNumber(), vc.getFieldLength());
        }
    }

    private String dteEquivalentBasedOnTypes() {
        FieldReferenceAST fr = (FieldReferenceAST) src;
        ColumnAST col = (ColumnAST) trg;
        ColumnFieldHolder cfh = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
        ComponentFieldHolder srcfh = sourceFieldHolders.get(fr.getRef().getName());
        if (srcfh == null) {
            return String.format("// WARNING: no field holder for source field %s — skipped", fr.getRef().getName());
        }
        String valueArg;
        String colAccessor = cfh != null ? cfh.getAccessor() : "putString";
        if (!colAccessor.equals("putString")) {
            // Target column is a typed numeric field (packed or binary).
            // Read via the SOURCE field's accessor and convert to the target type if needed.
            String srcAccessor = srcfh.getAccessor(); // e.g. "BigDecimal", "Int", "Long", "getString"
            String rawRead = String.format("%s.get%s(src)", fr.getRef().getName(), srcAccessor);
            valueArg = convertNumericValue(rawRead, srcAccessor, colAccessor);
        } else {
            // Target is a string column — use the pre-formatted assignment source expression.
            valueArg = srcfh.getAssignmentSource(col.getViewColumn().getFieldLength());
            if (valueArg == null || valueArg.isEmpty()) {
                return String.format("// WARNING: field %s has unsupported data type %s — skipped",
                    fr.getRef().getName(), fr.getRef().getDatatype());
            }
        }
        return String.format("                %s(%s, target);", cfh.getAssignmentTarget(), valueArg);
    }

    /**
     * Builds the value argument for a column assignment.
     * For packed-decimal output columns (putBigDecimal/putInt/putLong/putBigInteger)
     * the raw numeric expression is passed directly.
     * For string output columns (putString) the value is wrapped in String.format().
     */
    private String buildAssignmentValue(ColumnFieldHolder cfh, String rawExpr, int len, int dec) {
        if (cfh == null) {
            // Fallback: can't determine column type, use formatted string
            String fmtSpec = dec > 0
                    ? String.format("%%%d.%df", len, dec)
                    : String.format("%%%dd", len);
            return String.format("String.format(\"%s\", %s)", fmtSpec, rawExpr);
        }
        switch (cfh.getAccessor()) {
            case "putBigDecimal":
                // rawExpr is already a Java expression (e.g. a calc result or literal).
                // If it looks like a plain numeric literal, wrap it in new BigDecimal().
                // Otherwise pass through as-is (it's already a BigDecimal expression).
                if (rawExpr.matches("-?[0-9]+(\\.[0-9]+)?")) {
                    return String.format("new BigDecimal(\"%s\")", rawExpr);
                }
                return rawExpr;
            case "putInt":
                // If it's a plain integer literal, pass as-is; otherwise cast.
                return rawExpr;
            case "putLong":
                if (rawExpr.matches("-?[0-9]+")) {
                    return rawExpr + "L";
                }
                return rawExpr;
            case "putBigInteger":
                if (rawExpr.matches("-?[0-9]+")) {
                    return String.format("new BigInteger(\"%s\")", rawExpr);
                }
                return rawExpr;
            default: {
                // putString — wrap in String.format as before
                String fmtSpec = dec > 0
                        ? String.format("%%%d.%df", len, dec)
                        : String.format("%%%dd", len);
                return String.format("String.format(\"%s\", %s)", fmtSpec, rawExpr);
            }
        }
    }

    /** Returns true when the accessor is a bare numeric type name (as set by PackedFieldHolder / BinaryFieldHolder etc.) */
    private boolean isNumericAccessor(String accessor) {
        switch (accessor) {
            case "BigDecimal":
            case "BigInteger":
            case "Long":
            case "Int":
                return true;
            default:
                return false;
        }
    }

    /**
     * Wraps a raw read expression to convert from one numeric accessor type to another.
     * e.g. packed BigDecimal source → binary Long target: expr.longValue()
     *      packed BigDecimal source → packed BigDecimal target: no conversion needed
     *
     * @param rawRead     the Java expression that reads the value (e.g. "PRICE.getBigDecimal(src)")
     * @param srcAccessor bare source type: "BigDecimal", "BigInteger", "Long", "Int", "getString"
     * @param colAccessor target column put accessor: "putBigDecimal", "putLong", "putInt", "putBigInteger"
     */
    private String convertNumericValue(String rawRead, String srcAccessor, String colAccessor) {
        // Same type — no conversion needed
        if (colAccessor.equals("put" + srcAccessor)) {
            return rawRead;
        }
        // Cross-type conversions
        switch (colAccessor) {
            case "putBigDecimal":
                switch (srcAccessor) {
                    case "Int":    return String.format("new BigDecimal(%s)", rawRead);
                    case "Long":   return String.format("new BigDecimal(%s)", rawRead);
                    case "BigInteger": return String.format("new BigDecimal(%s)", rawRead);
                    default:       return rawRead;
                }
            case "putLong":
                switch (srcAccessor) {
                    case "BigDecimal": return String.format("%s.longValue()", rawRead);
                    case "BigInteger": return String.format("%s.longValue()", rawRead);
                    case "Int":        return String.format("(long) %s", rawRead);
                    default:           return rawRead;
                }
            case "putInt":
                switch (srcAccessor) {
                    case "BigDecimal": return String.format("%s.intValue()", rawRead);
                    case "BigInteger": return String.format("%s.intValue()", rawRead);
                    case "Long":       return String.format("(int) %s", rawRead);
                    default:           return rawRead;
                }
            case "putBigInteger":
                switch (srcAccessor) {
                    case "BigDecimal": return String.format("%s.toBigInteger()", rawRead);
                    case "Long":       return String.format("BigInteger.valueOf(%s)", rawRead);
                    case "Int":        return String.format("BigInteger.valueOf(%s)", rawRead);
                    default:           return rawRead;
                }
            default:
                return rawRead;
        }
    }

    private String dtlEquivalentBasedOnTypes(String joinbuffer, LRField redField, String name) {
        return assignBasedOnTypes(redField, joinbuffer, name);
        //return "TBD"; //assignBasedOnTypes(redField.getDatatype(), joinbuffer, redField.getStartPosition() - 1, redField.getLength());
    }

    private String assignBasedOnTypes(LRField f, String source, String name) {

        ColumnAST col = (ColumnAST) trg;
        if (col.getViewColumn().getDataType() == f.getDatatype() && col.getViewColumn().getFieldLength() >= f.getLength()) {
            // For typed numeric fields (e.g. PackedDecimalAsBigDecimalField) we must
            // use the accessor returned by the holder and format the value as a string.
            // Falling back to .getString() would produce a compile error because those
            // field types do not expose that method.
            // Note: lookupFieldHolders is reset per-lookup so may only contain the last
            // lookup's fields; search lookupHoldersByName across all lookups instead.
            ComponentFieldHolder fh;
            if (source.equals("src")) {
                fh = sourceFieldHolders.get(name);
            } else {
                fh = lookupHoldersByName.values().stream()
                        .map(m -> m.get(name))
                        .filter(h -> h != null)
                        .findFirst()
                        .orElse(null);
            }
            ColumnFieldHolder colFH = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
            String colAccessor = colFH != null ? colFH.getAccessor() : "putString";
            // Source field accessors for typed numerics are bare type names: "BigDecimal", "Long", "Int", "BigInteger"
            // String fields use "getString". Only enter the numeric path for the bare-type accessors.
            boolean srcIsNumeric = fh != null && isNumericAccessor(fh.getAccessor());
            if (srcIsNumeric) {
                String srcGetter = fh.getAccessor(); // e.g. "BigDecimal"
                String rawValueExpr = String.format("%s.get%s(%s)", name, srcGetter, source);
                if (!colAccessor.equals("putString")) {
                    // Packed column target: pass raw value directly
                    return String.format("                %s(%s, target);", colFH.getAssignmentTarget(), rawValueExpr);
                } else {
                    // String column target: format the numeric value
                    int len = col.getViewColumn().getFieldLength();
                    int dec = col.getViewColumn().getDecimalCount();
                    String fmtSpec = dec > 0
                            ? String.format("%%%d.%df", len, dec)
                            : String.format("%%%dd", len);
                    return String.format("                COL_%d.putString(String.format(\"%s\", %s), target);",
                            col.getViewColumn().getColumnNumber(), fmtSpec, rawValueExpr);
                }
            }
             return String.format("                COL_%d.putString(%s.getString(%s), target);", col.getViewColumn().getColumnNumber(), name,  source);
        } else {
            // We need to do a data type conversion.
            // Break out based on source and target data types.

            switch (col.getViewColumn().getDataType()) {
                case ALPHA:
                    break;
                case ALPHANUMERIC:
                    break;
                case BCD:
                    break;
                case BINARY: {
                    // Source is a different type (e.g. packed) being written into a binary column.
                    ColumnFieldHolder colFH = columnFieldHolders.get("COL_" + col.getViewColumn().getColumnNumber());
                    String colAccessor = colFH != null ? colFH.getAccessor() : "putString";
                    ComponentFieldHolder srcFH;
                    if (source.equals("src")) {
                        srcFH = sourceFieldHolders.get(name);
                    } else {
                        srcFH = lookupHoldersByName.values().stream()
                                .map(m -> m.get(name)).filter(h -> h != null).findFirst().orElse(null);
                    }
                    if (srcFH != null && colFH != null) {
                        String rawRead = String.format("%s.get%s(%s)", name, srcFH.getAccessor(), source);
                        String valueArg = convertNumericValue(rawRead, srcFH.getAccessor(), colAccessor);
                        return String.format("                %s(%s, target);", colFH.getAssignmentTarget(), valueArg);
                    }
                    break;
                }
                case BSORT:
                    break;
                case CONSTDATE:
                    break;
                case CONSTNUM:
                    break;
                case CONSTSTRING:
                    break;
                case EDITED: {
                    //return getEditedResult(col, sourceDataType, source, offset, length);
                    return getEditedResult(col, f.getDatatype(), source, name);
                }
                case FLOAT:
                    break;
                case GENEVANUMBER:
                    break;
                case INVALID:
                    break;
                case MASKED:
                    break;
                case PACKED:
                    break;
                case PSORT:
                    break;
                case ZONED:
                    break;
                default:
                    break;
            }
            String targString = String.format("%." + col.getViewColumn().getFieldLength() + "s", "!!!!!!!!");
            return String.format("        target.put(\"%s\".getBytes());", targString);
       }
    }

//    private String getEditedResult(ColumnAST col, DataType sourceDataType, String source, int offset, int length) {
    private String getEditedResult(ColumnAST col, DataType sourceDataType, String source, String fieldName) {
        StringBuilder sb = new StringBuilder();
        switch (sourceDataType) {
            case ALPHA:
                break;
            case ALPHANUMERIC:
                break;
            case BCD:
                break;
            case BINARY: {
                //sb.append(String.format("        Bin2ToEdited.transformField(%s, %d, %d, %d, %d);", source, offset, length, col.getViewColumn().getStartPosition() - 1, col.getViewColumn().getFieldLength()));
                Map<String, ComponentFieldHolder> holders;
                if(source.equals("src")) {
                    holders = sourceFieldHolders;
                } else {
                    holders = lookupFieldHolders;
                }
                sb.append(String.format("        COL_%d.putString(String.format(\"%%%dd\", %s.get%s(%s)), target);", 
                col.getViewColumn().getColumnNumber(), col.getViewColumn().getFieldLength(), fieldName, holders.get(fieldName).getAccessor(), source));
                break;
            }
            case BSORT:
                break;
            case CONSTDATE:
                break;
            case CONSTNUM:
                break;
            case CONSTSTRING:
                break;
            case EDITED:
                break;
            case FLOAT:
                break;
            case GENEVANUMBER:
                break;
            case INVALID:
                break;
            case MASKED:
                break;
            case PACKED: {
                //We need to distinguish which PackedToEdited it is.
                //Put them in a FieldHolder and then use the accessor to determine which one to use.
                //         COL_5.putString(String.format("%f", PRICE.getBigDecimal(src)), target);
                //sb.append(String.format("        PackedToEdited.transformField(%s, %d, %d, %d, %d);", source, offset, length, col.getViewColumn().getStartPosition() - 1, col.getViewColumn().getFieldLength()));
                Map<String, ComponentFieldHolder> holders;
                if(source.equals("src")) {
                    holders = sourceFieldHolders;
                } else {
                    holders = lookupFieldHolders;
                }
                ViewColumn vc = col.getViewColumn();
                sb.append(String.format("        COL_%d.putString(String.format(\"%%%d.%df\", %s.get%s(%s)), target);", 
                    vc.getColumnNumber(), vc.getFieldLength(), vc.getDecimalCount(), fieldName, holders.get(fieldName).getAccessor(), source));
                break;
            }
            case PSORT:
                break;
            case ZONED:
                //sb.append(String.format("        ZonedToEdited.transformField(%s, %d, %d, %d, %d);", source, offset, length, col.getViewColumn().getStartPosition() - 1, col.getViewColumn().getFieldLength()));
                break;
            default:
                return String.format("        target.put(\"%s\".getBytes());", String.format("%." + col.getViewColumn().getFieldLength() + "s", "NNNNNNNNN"));
        }
        return sb.toString();
    }

}
