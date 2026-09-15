package org.genevaers.compilers.extract.JavaEmitter.generators;

import org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders.ComponentFieldHolder;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LRField;

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
        String joinBufName = "joinBuffer" + fn.getNewJoinId();
        // Look up the typed field holder registered in lookupFieldHolders so that
        // the correct accessor (getLong, getBigInteger, getBigDecimal, etc.) is used.
        // The holder key is "<lookupName>_<fieldName>" as built by ViewSourceGenerator.
        String holderKey = fn.getLookup().getName() + "_" + fld.getName();
        ComponentFieldHolder holder = lookupFieldHolders.get(holderKey);
        if (holder != null) {
            // Build the accessor call using holderKey as the Java field name.
            // We cannot use holder.getValueFrom() because FieldHolder.getName() returns
            // the bare LRField name, not the lookup-prefixed holder name (holderKey).
            String method = holder.getAccessor().startsWith("get")
                    ? holder.getAccessor()
                    : "get" + holder.getAccessor();
            return holderKey + "." + method + "(" + joinBufName + ")";
        }
        // Fallback: treat as a raw string slice (String fields or unknown types)
        LRField redField = Repository.getREDfieldFrom(fn.getLookup(), fld);
        if (redField != null) {
            return String.format("new String(%s.array(), %d, %d)", joinBufName, redField.getStartPosition() - 1, redField.getLength());
        }
        return String.format("new String(%s.array(), %d, %d)", joinBufName, fld.getStartPosition() - 1, fld.getLength());
     }
}
