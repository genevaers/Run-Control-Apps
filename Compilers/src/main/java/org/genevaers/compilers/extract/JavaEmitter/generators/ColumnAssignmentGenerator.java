package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;

public class ColumnAssignmentGenerator extends ExtractRecordGenerator {

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
            LogicalRecord refLR = Repository.getLogicalRecords().get(9000000 + lfr.getNewJoinId());
            LRField reffld = refLR.findFromFieldsByName(fld.getName());
            String joinLogicFormat = "        if(joinBuffer != null) {\n        %s\n        } else {\n        %s\n        }";
            String body = String.format("target.put(joinBuffer.bytes.array(), %d, %d);", reffld.getStartPosition() - 1,
                    reffld.getLength());
            String elseBody = String.format("target.put(String.format(\"%%-%ds\", \" \").getBytes(), 0, %d);",
                    reffld.getLength(), reffld.getLength());
            return String.format(joinLogicFormat, body, elseBody);
        } else if (trg.getType() == Type.DT_COLUMN && src.getType() == Type.STRINGATOM) {
            StringAtomAST sa = (StringAtomAST) src;
            String targString = sa.getValue();
            ColumnAST col = (ColumnAST) trg;
            if (targString.equals("")) {
                targString = String.format("%-" + col.getViewColumn().getFieldLength() + "s", " ");
            } else {
                targString = String.format("%-" + col.getViewColumn().getFieldLength() + "s", sa.getValue());
            }
            return String.format("        target.put(\"%s\".getBytes());", targString);
        } else {
            return "ASSTBD";
        }
    }

    private String dteEquivalentBasedOnTypes() {

        ColumnAST col = (ColumnAST) trg;
        FieldReferenceAST fr = (FieldReferenceAST) src;
        if (col.getViewColumn().getDataType() == fr.getRef().getDatatype()) {
            // We can do a straight copy.
            return String.format("        target.put(src, %d, %d);", fr.getRef().getStartPosition() - 1,
                    fr.getRef().getLength());
        } else {
            // We need to do a data type conversion.
            // Break out based on source and target data types.

            switch (col.getViewColumn().getDataType()) {
                case ALPHA:
                    break;
                case ALPHANUMERIC:
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
                case EDITED:
                    return getEditedResult(col, fr);
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

    private String getEditedResult(ColumnAST col, FieldReferenceAST fr) {
        switch (fr.getRef().getDatatype()) {
            case ALPHA:
                break;
            case ALPHANUMERIC:
                break;
            case BCD:
                break;
            case BINARY:
//        int value = ((src[27] & 0xFF) << 8) |
//                        ((src[28] & 0xFF));
//        target.put(String.format("%4d", value).getBytes());
                StringBuilder sb = new StringBuilder();
                int offset =fr.getRef().getStartPosition() - 1;
                //make this a for loop base on field length
                sb.append(String.format("int value = ((src[%d] & 0xFF) << 8) |\n", offset));
                sb.append(String.format("                        ((src[%d] & 0xFF));\n", offset + 1));
                return String.format("%s        target.put(String.format(\"%%%dd\", value).getBytes());", sb.toString(),col.getViewColumn().getFieldLength());
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
            case PACKED:
                break;
            case PSORT:
                break;
            case ZONED:
                break;
            default:
                return String.format("        target.put(\"%s\".getBytes());", String.format("%." + col.getViewColumn().getFieldLength() + "s", "NNNNNNNNN"));
            }
        return String.format("        target.put(\"%s\".getBytes());", String.format("%." + col.getViewColumn().getFieldLength() + "s", "NNNNNNNNN"));
    }

}
    