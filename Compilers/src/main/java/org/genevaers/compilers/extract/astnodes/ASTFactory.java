package org.genevaers.compilers.extract.astnodes;

import org.genevaers.compilers.base.ASTBase;
import org.genevaers.repository.components.ViewColumn;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


public class ASTFactory {
    public enum Type {
        EBASE("Extract Base"), 
        LF("Logical File"), 
        VIEWSOURCE("View Source"), 
        VIEWCOLUMNSOURCE("View Column Source"), 
        STATEMENTLIST("StatementList"),
        STATEMENT("Statement"),
        PF("Physical File"), 
        COLUMNASSIGNMENT("Column Assignment"), 
        NUMATOM("Number"), 
        STRINGATOM("String"), 
        SELECTIF("SelectIf"), 
        SKIPIF("SkipIf"), 
        LRFIELD("LR Field"), 
        ERRORS("Errors"), 
        WARNING("Warning"),
        COLUMN("Column"), 
        COLUMNREF("Column Ref"), 
        CT_COLUMN("CT Column"), 
        DT_COLUMN("DT Column"), 
        SK_COLUMN("SK Column"), 
        EXPRCOMP("Expr Comparison"),
        STRINGCOMP("String Comparison"),
        EXTRFILTER("Extract Filter"), 
        ERSROOT("Geneva ERS Root"), 
        IFNODE("If"), 
        BOOLAND("And"),
        BOOLOR("Or"), 
        BOOLNOT( "Not"),
        NUMACC("Numeric Accumulator"), 
        LOOKUPREF("Lookup"),
        LOOKUPFIELDREF("Lookup Field"), 
        SYMBOLLIST("Symbol list"), 
        SYMBOL("Symbol"), 
        EFFDATEVALUE("Effective Data Value"), 
        ERSJOINSROOT("Geneva ERS Joins Root"), 
        WRITE("WRITE"),
        WRITESOURCE("Source"), 
        WRITESOURCEARG("Arg"),
        WRITEEXIT("Write Exit"),
        WRITEDEST("Destination"),
        WRITEDESTARG("Arg"),
        WRITEFILE("DEST FILE"),
        WRITEEXTRACT("EXTRACT"),
        DATEFUNC("Date Function"),
        DATATYPE("Datatype"),
        CAST("Cast"),
        RUNDATE("Rundate"),
        FISCALDATE("FiscalDate"),
        UNARYINT("Int"),
        EXTRACTOUTPUT("Extract Ouptut"),
        CALCULATION("Calculation"),
        MULTIPLICATION("Multiplication"),
        DIVISION("Division"),
        SETTER("Setter"),
        ADDITION("Addition"),
        SUBTRACTION("Subtraction"),
        RECORD_COUNT("Record Count"),
        SORTTITLE("Sort Title"),
        EOS("End of Set"), 
        PRIORLRFIELD("Prior Field"), 
        ISNULL("Is Null"),
        ISSPACES("Is Spaces"),
        ISNUMERIC("Is Numeric"), 
        ISNOTSPACES("Is Not Spaces"),
        ISNOTNULL("Is Not Null"),
        ISNOTNUMERIC("Is Not Numeric"), 
        BETWEENFUNC("Between"), 
        REPEAT("Repeat"), 
        STRINGCONCAT("Concatination"), 
        RIGHT("Right"),
        LEFT("Left"),
        SUBSTR("Substring"), 
        ISFOUND("Is Found"),
        ISNOTFOUND("Is Not Found"), 
        ALL("ALL")
        ;

        private String name;
        private Type(String n) {
            name = n;
        }
        @Override
        public String toString() {
            return name;
        }

    }

    public static ExtractBaseAST getNodeOfType(Type t) {
        switch (t) {
            case ERSROOT:
                return new GenevaERSRoot();
            case ERSJOINSROOT:
                return new GenevaERSJoinsRoot();
            case LF:
                return new LFAstNode();
            case PF:
                return new PFAstNode();
            case VIEWSOURCE:
                return new ViewSourceAstNode();
            case VIEWCOLUMNSOURCE:
                return new ViewColumnSourceAstNode();
            case COLUMNASSIGNMENT:
                return new ColumnAssignmentASTNode();
            case NUMATOM:
                return new NumAtomAST();
            case STRINGATOM:
                return new StringAtomAST();
            case STRINGCONCAT:
                return new StringConcatinationAST();
            case RIGHT:
                return new RightASTNode();
            case LEFT:
                return new LeftASTNode();
            case SUBSTR:
                return new SubStringASTNode();
            case LRFIELD:
                return new FieldReferenceAST();
            case ERRORS:
                return new ErrorAST();
            case WARNING:
                return new WarningAST();
            case SELECTIF:
                return new SelectIfAST();
            case SKIPIF:
                return new SkipIfAST();
            case EXPRCOMP:
                return new ExprComparisonAST();
            case STRINGCOMP:
                return new StringComparisonAST();
            case EXTRFILTER:
                return new ExtractFilterAST();
            case IFNODE:
                return new IfAST();
            case BOOLAND:
                return new BooleanAndAST();
            case BOOLOR:
                return new BooleanOrAST();
            case BOOLNOT:
                return new BooleanNotAST();
            case NUMACC:
                return new NumericAccumulator();
            case LOOKUPREF:
                return new LookupPathRefAST();
            case LOOKUPFIELDREF:
                return new LookupFieldRefAST();
            case SYMBOLLIST:
                return new SymbolList();
            case SYMBOL:
                return new SymbolEntry();
            case EFFDATEVALUE:
                return new EffDateValue();
            case WRITE:
                return new WriteASTNode();
            case WRITESOURCE:
                return new WriteSourceNode();
            case WRITESOURCEARG:
                return new WriteSourceArg();
            case WRITEDEST:
                return new WriteDestNode();
            case WRITEDESTARG:
                return new WriteDestArg();
            case WRITEEXIT:
                return new WriteExitNode();
            case WRITEFILE:
                return new WriteFileNode();
            case WRITEEXTRACT:
                return new WriteExtractNode();
            case DATEFUNC:
                return new DateFunc();
            case CAST:
                return new CastAST();
            case DATATYPE:
                return new DataTypeAST();
            case RUNDATE:
                return new RundateAST();
            case FISCALDATE:
                return new FiscaldateAST();
            case UNARYINT:
                return new UnaryInt();
            case EXTRACTOUTPUT:
                return new ExtractOutputAST();
            case CALCULATION:
                return new CalculationAST();
            case MULTIPLICATION:
                return new MultiplcationAST();
            case SETTER:
                return new SetterAST();
            case ADDITION:
                return new AdditionAST();
            case SUBTRACTION:
                return new SubtractionAST();
            case DIVISION:
                return new DivisionAST();
            case EOS:
                return new EndOfSetASTNode();
            case RECORD_COUNT:
                return new RecordCountAST();
            case SORTTITLE:
                return new SortTitleAST();
            case PRIORLRFIELD:
                return new PriorFieldReferenceAST();
            case COLUMNREF:
                return new ColumnRefAST();
            case ISNULL:
                return new IsNullAST();
            case ISNUMERIC:
                return new IsNumericAST();
            case ISSPACES:
                return new IsSpacesAST();
            case ISNOTNULL:
                return new IsNotNullAST();
            case ISNOTNUMERIC:
                return new IsNotNumericAST();
            case ISNOTSPACES:
                return new IsNotSpacesAST();
            case BETWEENFUNC:
                return new BetweenFunc();
            case REPEAT:
                return new RepeatAST();
            case ISFOUND:
                return new IsFoundAST();
            case ISNOTFOUND:
                return new IsNotFoundAST();
            case STATEMENTLIST:
                return new StatementList();
            case STATEMENT:
                return new Statement();
            case ALL:
                return new AllAST();
            default:
                return null;
        }
    }

    public static ColumnAST getColumnNode(ViewColumn vc) {
        switch (vc.getExtractArea()) {
            case AREACALC:
                return new CTColumnAST(vc);
            case AREADATA:
                return new DTColumnAST(vc);
            case INVALID:
                return null;
            case SORTKEY:
                return new SKColumnAST(vc);
            case SORTKEYTITLE:
                return null;
            default:
                return null;
        }
    }

    public static boolean isStringFunction(ExtractBaseAST n) {
        switch (n.getType()) {
            case LEFT:
            case RIGHT:
            case SUBSTR:
            return true;
            default:
            return false;
        }
    }

    public static boolean isDateCode(ExtractBaseAST n) {
        switch (n.getType()) {
            case RUNDATE:
            case FISCALDATE:
            return true;
            default:
            return false;
        }
    }
}
