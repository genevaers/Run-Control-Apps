package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.enums.DataType;

import com.google.common.flogger.FluentLogger;

import java.util.Map;

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
        ColumnAST col = (ColumnAST) trg;
        if (trg.getType() == Type.DT_COLUMN && src.getType() == Type.LRFIELD) {
            FieldReferenceAST fr = (FieldReferenceAST) src;
            // Break out base on source and target data types.
            return dteEquivalentBasedOnTypes();
        } else if (trg.getType() == Type.DT_COLUMN && src.getType() == Type.LOOKUPFIELDREF) {
            LookupFieldRefAST lfr = (LookupFieldRefAST) src;
            LRField fld = lfr.getRef();
            // This will be dependent on the type of the field, for now we will assume all
            // fields are strings and use the String
            // We need the key length since the record start after the key in the join
            // buffer?
            LRField redField = Repository.getREDfieldFrom(lfr.getLookup(), fld);
            if(redField == null) {
                logger.atSevere().log("Unable to find reference field for lookup field reference %s", fld.getName());
                return "/* Unable to find reference field for lookup field reference " + fld.getName() + " */";
            }
            String joinBufString = "joinBuffer" + lfr.getNewJoinId();
            String joinLogicFormat = "        if(" + joinBufString + " != null) {\n        %s\n        } else {\n        %s\n        }";
            String name = lfr.getLookup().getName() + "_" + redField.getName();
            String body = dtlEquivalentBasedOnTypes(joinBufString, redField , name);
            String elseBody = getElseBody(col); 
            return String.format(joinLogicFormat, body, elseBody);
        } else if (trg.getType() == Type.DT_COLUMN && src.getType() == Type.STRINGATOM) {
            StringAtomAST sa = (StringAtomAST) src;
            String targString = sa.getValue();
            if (targString.equals("")) {
                targString = String.format("%-" + col.getViewColumn().getFieldLength() + "s", " ");
            } else {
                targString = String.format("%-" + col.getViewColumn().getFieldLength() + "s", sa.getValue());
            }
            return String.format("        target.put(\"%s\".getBytes());", targString);
        } else {
            return "/* Assignment type not yet implemented " + trg.getType() + " = " + src.getType() + " */";
        }
    }

    private String getElseBody(ColumnAST col) {
        // Else body is either a numerical 0 or a bunch of spaces
        ViewColumn vc = col.getViewColumn();
        if(vc.getDataType() == DataType.ALPHANUMERIC) {
             return String.format("                COL_%d.putString(String.format(\"%%-%ds\", \" \"), target);", vc.getColumnNumber(), vc.getFieldLength());
        } else {
            //need the accessor here because different puts?
             return String.format("                COL_%d.putString(String.format(\"%%-%ds\", \" \"), target);", vc.getColumnNumber(), vc.getFieldLength());
        }
        
    }

    private String dteEquivalentBasedOnTypes() {
        FieldReferenceAST fr = (FieldReferenceAST) src;
        //return assignBasedOnTypes(fr.getRef().getDatatype(), "src", fr.getRef().getStartPosition() - 1, fr.getRef().getLength());
        return assignBasedOnTypes(fr.getRef(), "src", fr.getRef().getName());
    }

    private String dtlEquivalentBasedOnTypes(String joinbuffer, LRField redField, String name) {
        return assignBasedOnTypes(redField, joinbuffer, name);
        //return "TBD"; //assignBasedOnTypes(redField.getDatatype(), joinbuffer, redField.getStartPosition() - 1, redField.getLength());
    }

    private String assignBasedOnTypes(LRField f, String source, String name) {

        ColumnAST col = (ColumnAST) trg;
        if (col.getViewColumn().getDataType() == f.getDatatype() && col.getViewColumn().getFieldLength() >= f.getLength()) {
                    //COL_1.putString(ORDER_ID.getString(src), target);

        //System.arraycopy(src, srcOffset, target, offset, length);
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
                case BINARY:
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
                sb.append(String.format("        COL_%d.putString(String.format(\"%%%d.%df\", %s.get%s(%s)), target);", 
                col.getViewColumn().getColumnNumber(), col.getViewColumn().getFieldLength(), col.getViewColumn().getDecimalCount(), fieldName, holders.get(fieldName).getAccessor(), source));
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
