package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;
import org.genevaers.repository.components.LogicalRecord;

public class LookupFieldRefGenerator extends ExtractRecordGenerator {

    private LookupFieldRefAST fieldnode;

    public LookupFieldRefGenerator(LookupFieldRefAST fieldnode) {
        this.fieldnode = fieldnode;
    }

    @Override
    public void generateCode() {
    }
    
     @Override
     public String getCode(ExtractBaseAST node) {
        LookupFieldRefAST fn = (LookupFieldRefAST) node;
        LRField fld = fn.getRef();
        //This will be dependent on the type of the field, for now we will assume all fields are strings and use the String
        //We need the key length since the record start after the key in the join buffer?
        LogicalRecord refLR = Repository.getLogicalRecords().get(9000000 + fn.getNewJoinId());
        LRField reffld = refLR.findFromFieldsByName(fld.getName());
        return String.format("new String(joinBuffer.bytes.array(), %d , %d)", reffld.getStartPosition()-1, reffld.getLength());
     }
}
