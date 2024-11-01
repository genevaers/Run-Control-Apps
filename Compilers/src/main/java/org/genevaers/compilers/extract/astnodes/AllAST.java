package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.repository.components.enums.DataType;
import org.genevaers.repository.components.enums.DateCode;

public class AllAST extends StringAtomAST {

    public AllAST() {
        type = Type.ALL;
    }

    public String getValue() {
        return getValueString();
    }

    @Override
    public DataType getDataType() {
        return DataType.CONSTSTRING;
    }

    @Override
    public DateCode getDateCode() {
        return DateCode.NONE;
    }

    @Override
    public String getMessageName() {
        return "All";
    }

    @Override
    public int getMaxNumberOfDigits() {
        return value.length() * 2;
    }

    @Override
    public String getValueString() {
        String retval = value;
        if(value.startsWith("\\x") || value.startsWith("\\X") )
        {
            //need the length of the source
            ExtractBaseAST otherSide = (ExtractBaseAST) parent.getChild(0);
            int len = 0;
            if(otherSide.getType() == ASTFactory.Type.LRFIELD) {
                len = ((FieldReferenceAST)otherSide).getRef().getLength();
            } else if(otherSide.getType() == ASTFactory.Type.LOOKUPFIELDREF) {
                len = ((LookupFieldRefAST)otherSide).getRef().getLength();
            }
            String hexStr = value.substring(2,4);
            retval = "\\x";
            for(int i=0; i<len; i++){
                retval += hexStr;
            }
    }
        return retval;
    }
}
