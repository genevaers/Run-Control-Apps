package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;

public class BinaryFieldHolder extends FieldHolder {

    public BinaryFieldHolder(LRField f) {
        super(f);
        field = f;
        int length = f.getLength();
        boolean signed = f.isSigned();
        if ((length <= 0 || length >= 4) && (length != 4 || !signed)) {
            if (length <= 8) {
                setAccessor("Long");
                setDefinition(String.format("private static final BinaryAsLongField %s = factory.getBinaryAsLongField(%d, %b)", f.getName(), length, signed));
            } else if (length > 8) {
                setAccessor("BigInteger");
                setDefinition(String.format("private static final BinaryAsBigIntegerField %s = factory.getBinaryAsBigIntegerField(%d, %b)", f.getName(), length, signed));
            } else {
                setAccessor("illegal length");
            }
        } else {
                setAccessor("Int");
                setDefinition(String.format("private static final BinaryAsIntField %s = factory.getBinaryAsIntField(%d, %b)", f.getName(), length, signed));
        }
    }

    @Override
    public String getAssignmentSource(int len) {
        //Issue here is that the format length is that of the target colum not the source field!!!
        //Pass targ length in?
        //Handle at assignment level? 
        return String.format("String.format(\"%%%dd\", %s.get%s(src))",len, field.getName(), getAccessor());
    }

    @Override
    public String getValueFrom(String src) {
        return getName() + ".get" + accessor + "(" + src + ")";
    }
}
