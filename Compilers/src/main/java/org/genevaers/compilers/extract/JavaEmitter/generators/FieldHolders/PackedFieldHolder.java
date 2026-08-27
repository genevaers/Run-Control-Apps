package org.genevaers.compilers.extract.JavaEmitter.generators.FieldHolders;

import org.genevaers.repository.components.LRField;

public class PackedFieldHolder extends FieldHolder {

    boolean useCompareTo = false;

    public PackedFieldHolder(LRField f) {
        super(f);
        field = f;
        int length = f.getLength();
        boolean signed = f.isSigned();
        int numDecimals = f.getNumDecimalPlaces();
        int precision = length * 2 - 1;
        setDetails(f.getName(), length, signed, numDecimals, precision);
    }

    public PackedFieldHolder(String name,LRField f) {
        super(f);
        field = f;
        int length = f.getLength();
        boolean signed = f.isSigned();
        int numDecimals = f.getNumDecimalPlaces();
        int precision = length * 2 - 1;
        setDetails(name, length, signed, numDecimals, precision);
    }

    private void setDetails(String name, int length, boolean signed, int numDecimals, int precision) {
        if (numDecimals > 0) {
            setAccessor("BigDecimal");
            useCompareTo = true;
            setDefinition(String.format("private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b);", name, length, numDecimals, signed));
        } else if (numDecimals < 0) {
            setAccessor("BigDecimal");
            useCompareTo = true;
            setDefinition(String.format("private static final PackedDecimalAsBigDecimalField %s = factory.getPackedDecimalAsBigDecimalField(%d, %d, %b);", name, length, numDecimals, signed));
        } else if (precision <= 9) {
            setAccessor("Int");
            setDefinition(String.format("private static final PackedDecimalAsIntField %s = factory.getPackedDecimalAsIntField(%d, %b);", name, length, signed));
        } else if (precision <= 18) {
            setAccessor("Long");
            setDefinition(String.format("private static final PackedDecimalAsLongField %s = factory.getPackedDecimalAsLongField(%d, %b);", name, length, signed));
        } else if (precision <= 31) {
            setAccessor("BigInteger");
            useCompareTo = true;
            setDefinition(String.format("private static final PackedDecimalAsBigIntegerField %s = factory.getPackedDecimalAsBigIntegerField(%d, %d, %b);", name, length, numDecimals, signed));
        } else {
            setAccessor("length too long");
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
    public boolean useCompareTo() {
        return useCompareTo;
    }
}
