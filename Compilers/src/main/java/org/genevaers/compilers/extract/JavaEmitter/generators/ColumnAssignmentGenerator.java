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
        //Make the format string dependent on the operator and types.
        columnRecs.add(getAssignmentFormatString());
    }

     @Override
     public String getCode(ExtractBaseAST node) {
        src = (ExtractBaseAST) node.getChild(0);
        trg = (ExtractBaseAST) node.getChild(1);
        srcgen = getcodeGenerator(src);
        trggen = getcodeGenerator(trg);
        //Make the format string dependent on the operator and types.
        return getAssignmentFormatString();
     }

     private String getAssignmentFormatString() {
        if(trg.getType() == Type.DT_COLUMN && src.getType() == Type.LRFIELD) {
            FieldReferenceAST fr = (FieldReferenceAST) src;
            return String.format("\n        target.put(src, %d, %d);", fr.getRef().getStartPosition() - 1,
                            fr.getRef().getLength());
        } else if(trg.getType() == Type.DT_COLUMN && src.getType() == Type.LOOKUPFIELDREF) {
            LookupFieldRefAST lfr = (LookupFieldRefAST) src;
            LRField fld = lfr.getRef();
            //This will be dependent on the type of the field, for now we will assume all fields are strings and use the String
            //We need the key length since the record start after the key in the join buffer?
            LogicalRecord refLR = Repository.getLogicalRecords().get(9000000 + lfr.getNewJoinId());
            LRField reffld = refLR.findFromFieldsByName(fld.getName());
            String joinLogicFormat = "        if(joinBuffer != null) {\n        %s\n        } else {\n        %s\n        }";
            String body = String.format("target.put(joinBuffer.bytes.array(), %d, %d);", reffld.getStartPosition()-1, reffld.getLength());
            String elseBody = String.format("target.put(String.format(\"%%-%ds\", \" \").getBytes(), 0, %d);", reffld.getLength(), reffld.getLength());
            return String.format(joinLogicFormat, body, elseBody);
        } else if(trg.getType() == Type.DT_COLUMN && src.getType() == Type.STRINGATOM) {
            StringAtomAST sa = (StringAtomAST) src;
            String targString = sa.getValue();
            ColumnAST col = (ColumnAST) trg;
            if(targString.equals("")) {
                targString = String.format("%-" + col.getViewColumn().getFieldLength() + "s", " ");
            } else {
                targString = String.format("%-" + col.getViewColumn().getFieldLength() + "s", sa.getValue());
            }
            return String.format("        target.put(\"%s\".getBytes());", targString);
       } else {
            return "ASSTBD";
        }
     }
    
}
