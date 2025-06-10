package org.genevaers.compilers.extract;

import java.util.Iterator;

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

import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.genevaers.compilers.extract.astnodes.ASTFactory;
import org.genevaers.compilers.extract.astnodes.AllAST;
import org.genevaers.compilers.extract.astnodes.BetweenFunc;
import org.genevaers.compilers.extract.astnodes.ASTFactory.Type;
import org.genevaers.genevaio.dataprovider.CompilerDataProvider;
import org.genevaers.compilers.extract.astnodes.BooleanAndAST;
import org.genevaers.compilers.extract.astnodes.BooleanNotAST;
import org.genevaers.compilers.extract.astnodes.BooleanOrAST;
import org.genevaers.compilers.extract.astnodes.CalculationAST;
import org.genevaers.compilers.extract.astnodes.CastAST;
import org.genevaers.compilers.extract.astnodes.ColumnAST;
import org.genevaers.compilers.extract.astnodes.ColumnAssignmentASTNode;
import org.genevaers.compilers.extract.astnodes.ColumnRefAST;
import org.genevaers.compilers.extract.astnodes.DataTypeAST;
import org.genevaers.compilers.extract.astnodes.DateFunc;
import org.genevaers.compilers.extract.astnodes.EffDateValue;
import org.genevaers.compilers.extract.astnodes.ExprComparisonAST;
import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.extract.astnodes.FieldReferenceAST;
import org.genevaers.compilers.extract.astnodes.FiscaldateAST;
import org.genevaers.compilers.extract.astnodes.IfAST;
import org.genevaers.compilers.extract.astnodes.LFAstNode;
import org.genevaers.compilers.extract.astnodes.LeftASTNode;
import org.genevaers.compilers.extract.astnodes.LookupFieldRefAST;
import org.genevaers.compilers.extract.astnodes.LookupPathAST;
import org.genevaers.compilers.extract.astnodes.LookupPathRefAST;
import org.genevaers.compilers.extract.astnodes.NumAtomAST;
import org.genevaers.compilers.extract.astnodes.RepeatAST;
import org.genevaers.compilers.extract.astnodes.RightASTNode;
import org.genevaers.compilers.extract.astnodes.RundateAST;
import org.genevaers.compilers.extract.astnodes.SelectIfAST;
import org.genevaers.compilers.extract.astnodes.SetterAST;
import org.genevaers.compilers.extract.astnodes.SkipIfAST;
import org.genevaers.compilers.extract.astnodes.SortTitleAST;
import org.genevaers.compilers.extract.astnodes.Statement;
import org.genevaers.compilers.extract.astnodes.StatementList;
import org.genevaers.compilers.extract.astnodes.StringAtomAST;
import org.genevaers.compilers.extract.astnodes.StringComparisonAST;
import org.genevaers.compilers.extract.astnodes.StringConcatinationAST;
import org.genevaers.compilers.extract.astnodes.SubStringASTNode;
import org.genevaers.compilers.extract.astnodes.SymbolEntry;
import org.genevaers.compilers.extract.astnodes.SymbolList;
import org.genevaers.compilers.extract.astnodes.UnaryInt;
import org.genevaers.compilers.extract.astnodes.WriteASTNode;
import org.genevaers.compilers.extract.astnodes.WriteDestNode;
import org.genevaers.compilers.extract.astnodes.WriteExitNode;
import org.genevaers.compilers.extract.astnodes.WriteExtractNode;
import org.genevaers.compilers.extract.astnodes.WriteFileNode;
import org.genevaers.compilers.extract.astnodes.WriteSourceArg;
import org.genevaers.compilers.extract.astnodes.WriteSourceNode;
import org.genevaers.grammar.GenevaERSBaseVisitor;
import org.genevaers.grammar.GenevaERSParser;
import org.genevaers.grammar.GenevaERSParser.ArithExprContext;
import org.genevaers.grammar.GenevaERSParser.ExprArithFactorContext;
import org.genevaers.grammar.GenevaERSParser.ExprArithTermContext;
import org.genevaers.grammar.GenevaERSParser.LrFieldContext;
import org.genevaers.grammar.GenevaERSParser.StmtContext;
import org.genevaers.grammar.GenevaERSParser.StringExprContext;
import org.genevaers.repository.Repository;
import org.genevaers.repository.components.LogicalRecord;
import org.genevaers.repository.components.LookupPath;
import org.genevaers.repository.components.ViewColumn;
import org.genevaers.repository.components.ViewColumnSource;
import org.genevaers.repository.components.ViewNode;
import org.genevaers.repository.components.ViewSource;

import com.google.common.flogger.FluentLogger;

public class BuildGenevaASTVisitor extends GenevaERSBaseVisitor<ExtractBaseAST> {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static CompilerDataProvider dataProvider;
    
    private ViewColumnSource viewColumnSource;
    private ViewSource viewSource;
    private ExtractContext extractContext;

    private ExtractBaseAST parent;

    private boolean procedure;

    public enum ExtractContext {
        FILTER,
        COLUMN,
        OUTPUT
    }

    public BuildGenevaASTVisitor(ExtractContext ctx) {
        extractContext = ctx;
    }

    @Override public ExtractBaseAST visitGoal(GenevaERSParser.GoalContext ctx) { 
        ExtractBaseAST tree = visit(ctx.stmtList());
        shouldVisitNextChild(ctx.getRuleContext(), tree);
        parent.addChildIfNotNull(tree);
        return tree;
    }

    @Override public ExtractBaseAST visitStmtList(GenevaERSParser.StmtListContext ctx) {
        StatementList stmnts = (StatementList)ASTFactory.getNodeOfType(ASTFactory.Type.STATEMENTLIST);
        for (StmtContext s : ctx.stmt()) {
            stmnts.addChildIfNotNull(visitChildren(s));
        }
        return stmnts;          
    }
        
    @Override public ExtractBaseAST visitStmt(GenevaERSParser.StmtContext ctx) {
        Statement stmnt = (Statement)ASTFactory.getNodeOfType(ASTFactory.Type.STATEMENT);
        stmnt.addChildIfNotNull(visit(ctx.getChild(2)));
        return stmnt; 
    }

    
    @Override public ExtractBaseAST visitSelectIf(GenevaERSParser.SelectIfContext ctx) { 
        SelectIfAST sfn = (SelectIfAST)ASTFactory.getNodeOfType(ASTFactory.Type.SELECTIF);
        if(extractContext == ExtractContext.FILTER) {
            sfn.addChildIfNotNull(visit(ctx.getChild(2)));
        } else {
            sfn.addError("SELECTIF cannot be used here");
        }
        return sfn; 
    }

    @Override public ExtractBaseAST visitSkipIf(GenevaERSParser.SkipIfContext ctx) { 
        SkipIfAST sfn = (SkipIfAST)ASTFactory.getNodeOfType(ASTFactory.Type.SKIPIF);
        sfn.addChildIfNotNull(visit(ctx.getChild(2)));
        return sfn; 
    }

	@Override public ExtractBaseAST visitCastColumnAssignment(GenevaERSParser.CastColumnAssignmentContext ctx) { 
        //replace the column node with a cast column node
        CastAST cn = (CastAST) ASTFactory.getNodeOfType(ASTFactory.Type.CAST);
        DataTypeAST dtn = (DataTypeAST) ASTFactory.getNodeOfType(ASTFactory.Type.DATATYPE);
        String datattype = ctx.getChild(0).getText();
        datattype = datattype.substring(1, datattype.length()-1);
        dtn.setDatatype(datattype);
        cn.addChildIfNotNull(dtn);
        
        ColumnAssignmentASTNode casnode = (ColumnAssignmentASTNode) visit(ctx.columnAssignment());
        casnode.getChild(1).addChildIfNotNull(cn);
        return casnode;
    }

    //Let's just visit a column node
	@Override public ExtractBaseAST visitColumnAssignment(GenevaERSParser.ColumnAssignmentContext ctx) { 
        ColumnAssignmentASTNode casnode = (ColumnAssignmentASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.COLUMNASSIGNMENT);
        ExtractBaseAST.setCurrentColumnNumber((short)viewColumnSource.getColumnNumber());
        TerminalNode eq = ctx.EQ();
        casnode.setLineNumber(eq.getSymbol().getLine());
        casnode.setCharPostionInLine(eq.getSymbol().getCharPositionInLine());
        TerminalNode c = ctx.COLUMN();
        if(c.getSymbol().getType() == GenevaERSParser.COLUMN) {
            ArithExprContext ae = ctx.arithExpr();
            StringExprContext se = ctx.stringExpr();
            if(ae != null) {
                casnode.addChildIfNotNull(visit(ae));
            }
            if(se != null) {
                casnode.addChildIfNotNull(visit(se));
            }
            ViewNode view = dataProvider.getView(viewColumnSource.getViewId());
            ColumnAST colNode = (ColumnAST)ASTFactory.getColumnNode(view.getColumnByID(viewColumnSource.getColumnID())); // Change this to make column type more specific
            colNode.setViewColumn(view.getColumnByID(viewColumnSource.getColumnID()));
            casnode.addChildIfNotNull(colNode);
            if(viewColumnSource.getSortTitleLookupId() > 0) {
                SortTitleAST stn = (SortTitleAST) ASTFactory.getNodeOfType(Type.SORTTITLE);
                stn.setSortTitleLookupId(viewColumnSource.getSortTitleLookupId());
                stn.setSortTitleFieldId(viewColumnSource.getSortTitleFieldId());
                colNode.addChildIfNotNull(stn);
            }
        }
        return casnode;
    }

	@Override public ExtractBaseAST visitColumnRefAssignment(GenevaERSParser.ColumnRefAssignmentContext ctx) { 
        ColumnAssignmentASTNode colRefAss = (ColumnAssignmentASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.COLUMNASSIGNMENT);
        TerminalNode c = ctx.COL_REF();
        colRefAss.addChildIfNotNull(visit(ctx.getChild(2)));
        if(c.getSymbol().getType() == GenevaERSParser.COL_REF) {
            String col = c.getText();
            String[] bits = col.split("\\.");
            ViewNode view = dataProvider.getView(viewSource.getViewId());
            int colnum = Integer.parseInt(bits[1]);
            if(colnum > 0 && colnum <= viewColumnSource.getColumnNumber()) {
                ViewColumn vc = view.getColumnNumber(colnum); 
                ColumnAST colNode = (ColumnAST)ASTFactory.getColumnNode(vc); // Change this to make column type more specific
                colNode.setViewColumn(vc);
                colRefAss.addChildIfNotNull(colNode);
            } else {
                if(colnum == 0) {
                    colRefAss.addError("Column number cannot be zero");
                } else {
                    colRefAss.addError("Column number must be less than the current column");
                }
            }
        }
        return colRefAss;
    }

	@Override public ExtractBaseAST visitExprBoolOr(GenevaERSParser.ExprBoolOrContext ctx) { 
        if(ctx.getChildCount() > 1) { 
            Iterator<ParseTree> ci = ctx.children.iterator();
            BooleanOrAST boolOrNode = (BooleanOrAST) ASTFactory.getNodeOfType(ASTFactory.Type.BOOLOR);
            int childNum = 0;
            while (ci.hasNext()) {
                ParseTree ctxEntry = ci.next();
                if(ctxEntry.getText().equalsIgnoreCase("OR") && childNum > 2) {
                    BooleanOrAST nextboolOrNode = (BooleanOrAST) ASTFactory.getNodeOfType(ASTFactory.Type.BOOLOR);
                    nextboolOrNode.addChildIfNotNull(boolOrNode);
                    boolOrNode = nextboolOrNode;
                } else {
                    boolOrNode.addChildIfNotNull(visit(ctxEntry));
                }
                childNum ++;
            }
            return boolOrNode; 
        } else {
            //Just passing through
            return visitChildren(ctx); 
        }
    }


    public ExtractBaseAST visitIsFunctions(GenevaERSParser.IsFunctionsContext ctx) {
        ExtractBaseAST fast = getIsFunctionsNode(ctx.getChild(0).toString()); // = ASTFactory.getNodeOfType(ASTFactory.Type.ISNULL)
        if( fast != null) 
            fast.addChildIfNotNull(visit(ctx.getChild(2)));

        return fast;
     }
  
    private ExtractBaseAST getIsFunctionsNode(String funcType) {
        ExtractBaseAST fast = null;
        switch (funcType) {
            case "ISSPACES":
                fast = ASTFactory.getNodeOfType(ASTFactory.Type.ISSPACES);
                break;
            case "ISNOTSPACES":
                fast = ASTFactory.getNodeOfType(ASTFactory.Type.ISNOTSPACES);
                break;
            case "ISNUMERIC":
                fast = ASTFactory.getNodeOfType(ASTFactory.Type.ISNUMERIC);
                break;
            case "ISNOTNUMERIC":
                fast = ASTFactory.getNodeOfType(ASTFactory.Type.ISNOTNUMERIC);
                break;
            case "ISNULL":
                fast = ASTFactory.getNodeOfType(ASTFactory.Type.ISNULL);
                break;
            case "ISNOTNULL":
                fast = ASTFactory.getNodeOfType(ASTFactory.Type.ISNOTNULL);
                break;
            default:
            logger.atSevere().log("Unknown function type %s", funcType);
                break;
        }
        return fast;
    }

    public ExtractBaseAST visitIsFounds(GenevaERSParser.IsFoundsContext ctx) {
        //1st child is the type of ISFOUND
        ExtractBaseAST fnd;
        if(ctx.getChild(0).getText().equals("ISFOUND")) {
            fnd = ASTFactory.getNodeOfType(ASTFactory.Type.ISFOUND);
        } else {
            fnd = ASTFactory.getNodeOfType(ASTFactory.Type.ISNOTFOUND);
        }
        //3rd is the child - lookup
        fnd.addChildIfNotNull(visit(ctx.getChild(2)));
        return fnd;
     }
  
    public ExtractBaseAST visitADataSource(GenevaERSParser.ADataSourceContext ctx) {
        return this.visitChildren(ctx);
     }
  

	@Override public ExtractBaseAST visitExprBoolAnd(GenevaERSParser.ExprBoolAndContext ctx) { 
        if(ctx.getChildCount() > 1) { 
            Iterator<ParseTree> ci = ctx.children.iterator();
            BooleanAndAST boolAndNode = (BooleanAndAST) ASTFactory.getNodeOfType(ASTFactory.Type.BOOLAND);
            int childNum = 0;
            while (ci.hasNext()) {
                ParseTree ctxEntry = ci.next();
                if(ctxEntry.getText().equalsIgnoreCase("AND") && childNum > 2) {
                    BooleanAndAST nextboolAndNode = (BooleanAndAST) ASTFactory.getNodeOfType(ASTFactory.Type.BOOLAND);
                    nextboolAndNode.addChildIfNotNull(boolAndNode);
                    boolAndNode = nextboolAndNode;
                } else {
                    boolAndNode.addChildIfNotNull(visit(ctxEntry));
                }
                childNum ++;
            }
            return boolAndNode; 
        } else {
            //Just passing through
            return visitChildren(ctx); 
        }
    }

    @Override public ExtractBaseAST visitExprBoolUnary(GenevaERSParser.ExprBoolUnaryContext ctx) {
        if(ctx.NOT() != null) {
            BooleanNotAST boolNot = (BooleanNotAST) ASTFactory.getNodeOfType(ASTFactory.Type.BOOLNOT);
            boolNot.addChildIfNotNull(visit(ctx.children.get(1)));
            return boolNot;

        } else {
            return this.visitChildren(ctx);
        }
    }
  
  

    @Override public ExtractBaseAST visitExprBoolAtom(GenevaERSParser.ExprBoolAtomContext ctx) {
        //Account for term in parenthesis
        if(ctx.getChildCount() > 1) {
            //assuming 3 for the moment
            return visit(ctx.children.get(1));
        } else {
            return this.visitChildren(ctx);
        }
     }

	@Override public ExtractBaseAST visitArithExpr(GenevaERSParser.ArithExprContext ctx) {
        //There may be one or many child nodes here now
        //The nodes are of type arithExprFactor - a Factor may have one or many arithExprTerms
        //single nodes imply 
        // 1 A single term that may be cast
        // 2 A combination of multiplied/divided terms
        //
        //
        // But what to we want to do?
        // Here we are only creating AST nodes.
        // We either create one or not

        //What does it mean if there is one or more than one child nodes?
        //Multiple child nodes imply addition/subtraction of factors 
        // >1 (total should be odd) 
        // means we can declare an accumulator into which we will sum the child nodes
        // so make a summation node? A new thing
        // !!!!!! Note we need the operations they are in the terminal nodes between the nodes
        //
        // lower if an factor node has multiple children we can make a multiplication node
        if (ctx.getChildCount() > 1) {
            CalculationAST calc = getCalculationAST(ctx.children);
            return calc;
        } else {
            return visit(ctx.getChild(0));
        }
    }

    private CalculationAST getCalculationAST(List<ParseTree> children) {
        CalculationAST calc = (CalculationAST) ASTFactory.getNodeOfType(ASTFactory.Type.CALCULATION);
        //should be in blocks of three
        // factor op factor -> create an ArithOP node - to hold the op
        // or we could add a ArithTerm(as an interface? for get op) node that has the op and a child that is of whatever type
        //  a 
        //
        // similarly could h

        // use a while
        // make the declaration
        // then iterate the nodes
        int childnum = 0;
        SetterAST decl = (SetterAST) ASTFactory.getNodeOfType(ASTFactory.Type.SETTER);
        calc.addChildIfNotNull(decl);
        decl.addChildIfNotNull(visit(children.get(childnum)));  //This is will be a Declaration node 
        //Should in each of these we go the stage further and derive the declaration type?
        //Or the Addition/Subtraction type?
        childnum++;
        while (childnum < children.size()) {
            ExtractBaseAST arithOp = getArithmeticNode(children.get(childnum).getText(), childnum);
            calc.addChildIfNotNull(arithOp);
        childnum++;
            arithOp.addChildIfNotNull(visit(children.get(childnum)));
            childnum++;
        }
        return calc;
    }

    private ExtractBaseAST getArithmeticNode(String string, int childnum) {
        switch (string) {
            case "+":
                return (ExtractBaseAST) ASTFactory.getNodeOfType(Type.ADDITION);
            case "-":
                return (ExtractBaseAST) ASTFactory.getNodeOfType(Type.SUBTRACTION);
            case "*":
                return (ExtractBaseAST) ASTFactory.getNodeOfType(Type.MULTIPLICATION);
            case "/":
                return (ExtractBaseAST) ASTFactory.getNodeOfType(Type.DIVISION);
            default:
                return null;
        }
    }

    @Override
    public ExtractBaseAST visitExprArithFactor(ExprArithFactorContext ctx) {
        // if the factor has many terms we have a multiplicative node
        // should be in blocks of three
        // term op term 
        if (ctx.getChildCount() > 1) {
            CalculationAST calc = getCalculationAST(ctx.children);
            return calc;
        } else {
            return visit(ctx.getChild(0));
        }
    }

    @Override
    public ExtractBaseAST visitExprArithTerm(ExprArithTermContext ctx) {
        //Always as an ArithAtom as a child (possibly a sign too)
        ExtractBaseAST atom;
        if(ctx.getChildCount() > 1) {
            atom = visit(ctx.getChild(1));
            if(ctx.getChild(0).getText().equals("-")) {
                atom.setNegative();
            }

        } else {
            atom = visit(ctx.getChild(0));
        }        
        return atom;
    }

	@Override public ExtractBaseAST visitExprArithAtom(GenevaERSParser.ExprArithAtomContext ctx) { 
        if(ctx.getChildCount() > 1) {
            //Must be bracketed
            return visit(ctx.getChild(1)); 
        } else {
            return visit(ctx.getChild(0)); 
        }
    }

    @Override public ExtractBaseAST visitCastDataSource(GenevaERSParser.CastDataSourceContext ctx) { 
        //First child is the cast type
        //Next iteration resolve this to the correct type
        CastAST cn = (CastAST) ASTFactory.getNodeOfType(ASTFactory.Type.CAST);
        DataTypeAST dtn = (DataTypeAST) ASTFactory.getNodeOfType(ASTFactory.Type.DATATYPE);
        String datattype = ctx.getChild(0).getText();
        datattype = datattype.substring(1, datattype.length()-1);
        dtn.setDatatype(datattype);
        cn.addChildIfNotNull(dtn);
        //second is the datasource... just walk it
        cn.addChildIfNotNull(visit(ctx.getChild(1)));
        return cn; 
    }


    @Override
    public ExtractBaseAST visitConstant(GenevaERSParser.ConstantContext ctx) {
        logger.atFine().log("Constant start type " + ctx.getStart().getType());
        return visitChildren(ctx);
    }

	@Override public ExtractBaseAST visitLrField(GenevaERSParser.LrFieldContext ctx) { 
        FieldReferenceAST fieldRef = makeCurrentOrPriorFieldReferenceAST(ctx);
        LogicalRecord lr = dataProvider.getLogicalRecord(viewSource.getSourceLRID());
        fieldRef.resolveField(lr, stripBraces(ctx.CURLED_NAME().getText()));
        return fieldRef; 
    }

    private String stripBraces(String curledName) {
        int braceNdx = curledName.indexOf("{");
        int endNdx = curledName.indexOf("}");
        return curledName.substring(braceNdx+1, endNdx);
    }

    private FieldReferenceAST makeCurrentOrPriorFieldReferenceAST(LrFieldContext ctx) {
        FieldReferenceAST fieldRef;
        if(ctx.PRIOR() != null) {
            fieldRef = (FieldReferenceAST) ASTFactory.getNodeOfType(ASTFactory.Type.PRIORLRFIELD);             
        } else {
        //we need the repository to verify that the field exists?
            fieldRef = (FieldReferenceAST) ASTFactory.getNodeOfType(ASTFactory.Type.LRFIELD);
        }
        fieldRef.setCharPostionInLine(ctx.getStart().getCharPositionInLine());
        fieldRef.setLineNumber(ctx.getStart().getLine());
        return fieldRef;
    }

    @Override public ExtractBaseAST visitIfbody(GenevaERSParser.IfbodyContext ctx) { 
        IfAST ifNode = (IfAST) ASTFactory.getNodeOfType(ASTFactory.Type.IFNODE);
        ifNode.addChildIfNotNull(visit(ctx.children.get(0))); //The predicate
        ifNode.addChildIfNotNull(visit(ctx.children.get(2))); //The if block
        if(ctx.getChildCount() > 4) { //Must be an ELSE block
            //Add and ELSE Node.... just for clarity?
            ifNode.addChildIfNotNull(visit(ctx.children.get(4))); //The else block
        }
        return ifNode; 
    }

    @Override public ExtractBaseAST visitArithComp(GenevaERSParser.ArithCompContext ctx) { 
        //Need to make these common between logic types
        //Or make the contents common?
        ExprComparisonAST ec = (ExprComparisonAST)ASTFactory.getNodeOfType(ASTFactory.Type.EXPRCOMP);
        if(ctx.getChildCount() == 3) {
            ec.addChildIfNotNull(visit(ctx.children.get(0)));
            //want the operation type from the middle child
            ec.setComparisonOperator(ctx.children.get(1).getText());
            ec.addChildIfNotNull(visit(ctx.children.get(2)));
        }
        return ec; 
    }

    @Override public ExtractBaseAST visitLookup(GenevaERSParser.LookupContext ctx) {
        LookupPathRefAST lkRef = (LookupPathRefAST) ASTFactory.getNodeOfType(ASTFactory.Type.LOOKUPREF);
        lkRef.setCharPostionInLine(ctx.getStart().getCharPositionInLine());
        lkRef.setLineNumber(ctx.getStart().getLine());
        if(ctx.getChildCount() == 1) {
            String lkname = ctx.getText();
            int braceNdx = lkname.indexOf("{");
            int endNdx = lkname.indexOf("}");
            String strippedName = lkname.substring(braceNdx+1, endNdx);
            addLookupReferenceToNode(lkRef, strippedName);
        } else if(ctx.getChildCount() >= 4) {
            addLookupReferenceToNode(lkRef, ctx.getChild(1).getText());
        } else {
            logger.atSevere().log("visitLookup lookup not found for %s\n", ctx.getText());
        }
        if(ctx.symbollist() != null) {
            lkRef.addChildIfNotNull(visitSymbollist(ctx.symbollist()));
            lkRef.setSymbols((SymbolList) visitSymbollist(ctx.symbollist()));
            checkSymbolListValid(lkRef);
        }
        if(ctx.effDate() != null) {
            lkRef.addChildIfNotNull(visitEffDate(ctx.effDate()));
            lkRef.setEffDateValue((EffDateValue) visitEffDate(ctx.effDate()));
        }
        lkRef.makeUnique();
        return lkRef;
     }

	private void addLookupReferenceToNode(LookupPathRefAST lkRef, String lkname) {
        LookupPath lookup =  dataProvider.getLookup(lkname);
		if(lookup != null) {
            lkRef.setLookup(lookup);
            lkRef.resolveLookup(lookup);
            Repository.getDependencyCache().addLookupIfAbsent(lkname, lookup);
            if(lookup.isActive() == false) {
                lkRef.addError("Lookup " + lkname + " is not active");
            }
		} else {
            logger.atSevere().log("addLookupReferenceToNode null lookup for %s\n", lkname);
        }		
    }

    @Override public ExtractBaseAST visitLookupField(GenevaERSParser.LookupFieldContext ctx) { 
        LookupFieldRefAST lkfieldRef = (LookupFieldRefAST) ASTFactory.getNodeOfType(ASTFactory.Type.LOOKUPFIELDREF);
		String fullName = ctx.getChild(1).getText();
		String[] parts = fullName.split("\\.");
        LookupPath lookup =  dataProvider.getLookup(parts[0]);
		if(lookup != null) {
            lkfieldRef.resolveField(lookup, parts[1]);
		} else {
            lkfieldRef.setCharPostionInLine(ctx.getStart().getCharPositionInLine());
            lkfieldRef.setLineNumber(ctx.getStart().getLine());
            lkfieldRef.addError("Unknown Lookup " + parts[0]);
        }		
        if(ctx.symbollist() != null) {
            lkfieldRef.addChildIfNotNull(visitSymbollist(ctx.symbollist()));
            lkfieldRef.setSymbols((SymbolList) visitSymbollist(ctx.symbollist()));
            checkSymbolListValid(lkfieldRef);
        }
        if(ctx.effDate() != null) {
            lkfieldRef.addChildIfNotNull(visitEffDate(ctx.effDate()));
            lkfieldRef.setEffDateValue((EffDateValue) visitEffDate(ctx.effDate()));
        }
		if(lookup != null) {
            lkfieldRef.makeUnique();
        } else {
            logger.atSevere().log("visitLookupField null lookup for %s",fullName);
        }
        return lkfieldRef;
    }

    private void checkSymbolListValid(LookupPathAST lkref) {
        for (String s : lkref.getSymbols().getSymbols()) {
            //Is the symbol defined in the lookup?
            if(lkref.getLookup().isSymbolNotDefined(s)) {
                lkref.addError("Symbol " + s + " not defined in lookup " + lkref.getLookup().getName());
            }
        }
    }

	@Override public ExtractBaseAST visitExprStringAtom(GenevaERSParser.ExprStringAtomContext ctx) { 
        if(ctx.getChildCount() > 1) {
            //Must be bracketed
            return visit(ctx.getChild(1)); 
        } else {
            return visit(ctx.getChild(0)); 
        }
     }

	@Override public ExtractBaseAST visitNumAtom(GenevaERSParser.NumAtomContext ctx) { 
        NumAtomAST num = (NumAtomAST) ASTFactory.getNodeOfType(ASTFactory.Type.NUMATOM);
        num.setValue(ctx.getText());
        return num;
    }

    @Override public ExtractBaseAST visitSymbollist(GenevaERSParser.SymbollistContext ctx) { 
        SymbolList symList = (SymbolList) ASTFactory.getNodeOfType(ASTFactory.Type.SYMBOLLIST);
        Iterator<ParseTree> ci = ctx.children.iterator();
        while (ci.hasNext()) {
            symList.addChildIfNotNull(visit(ci.next()));             
        }
        return symList;
    }

    @Override public ExtractBaseAST visitSymbolEntry(GenevaERSParser.SymbolEntryContext ctx) { 
        SymbolEntry sym = (SymbolEntry) ASTFactory.getNodeOfType(ASTFactory.Type.SYMBOL);
        sym.setSymbol(ctx.getChild(0).getText());
        sym.setValue(ctx.getChild(2).getText());
        return sym; 
    }

	@Override public ExtractBaseAST visitEffDate(GenevaERSParser.EffDateContext ctx) { 
        return visitChildren(ctx); 
    }

    @Override public ExtractBaseAST visitEffDateValue(GenevaERSParser.EffDateValueContext ctx) { 
        EffDateValue effdate = (EffDateValue)ASTFactory.getNodeOfType(ASTFactory.Type.EFFDATEVALUE);
         effdate.addChildIfNotNull(visitChildren(ctx)); 
        return effdate;
    }

    @Override public ExtractBaseAST visitRunDate(GenevaERSParser.RunDateContext ctx) { 
        RundateAST rd = (RundateAST) ASTFactory.getNodeOfType(ASTFactory.Type.RUNDATE);
        rd.setValue(ctx.getChild(0).getText());
        if(ctx.getChildCount() > 3){
            rd.addChildIfNotNull(visit(ctx.getChild(2)));
        }
        return rd; 
    }

    @Override public ExtractBaseAST visitWriteStatement(GenevaERSParser.WriteStatementContext ctx) {
        procedure = false;
        WriteASTNode wr = (WriteASTNode)ASTFactory.getNodeOfType(ASTFactory.Type.WRITE);
        ExtractBaseAST.setLastColumnWithAWrite();
        wr.setViewSource(viewSource);

        //Should we process here to see what the child nodes are?
        //Or just let the nodes do their own thing and sort it at emit time?
        for(int c=0; c<ctx.getChildCount(); c++) {
            wr.addChildIfNotNull(visit(ctx.children.get(c)));
        }
        return wr; 
    }

	@Override public ExtractBaseAST visitSource(GenevaERSParser.SourceContext ctx) { 
        WriteSourceNode source = (WriteSourceNode)ASTFactory.getNodeOfType(ASTFactory.Type.WRITESOURCE);
        for(int c=0; c<ctx.getChildCount(); c++) {
            source.addChildIfNotNull(visit(ctx.children.get(c)));
        }
        return source; 
    }

    @Override public ExtractBaseAST visitSourceArg(GenevaERSParser.SourceArgContext ctx) { 
        WriteSourceArg arg = (WriteSourceArg)ASTFactory.getNodeOfType(ASTFactory.Type.WRITESOURCEARG);
        arg.setArg(ctx.getText());
        return arg; 
    }

    @Override public ExtractBaseAST visitDestination(GenevaERSParser.DestinationContext ctx) { 
        WriteDestNode dest = (WriteDestNode) ASTFactory.getNodeOfType(ASTFactory.Type.WRITEDEST);
        //for(int c=0; c<ctx.getChildCount(); c++) {
        //The 3rd child has the info... what happens if the text was bad something like DEST:fred
            dest.addChildIfNotNull(visit(ctx.children.get(2)));
        //}
        return visit(ctx.children.get(2)); 
    }

	@Override public ExtractBaseAST visitDestArg(GenevaERSParser.DestArgContext ctx) { 
        return visit(ctx.children.get(0)); 
    }

    @Override public ExtractBaseAST visitFileArg(GenevaERSParser.FileArgContext ctx) { 
        WriteFileNode wf = (WriteFileNode) ASTFactory.getNodeOfType(ASTFactory.Type.WRITEFILE);
        wf.addChildIfNotNull(visit(ctx.children.get(2)));
        return wf; 
    }

    @Override public ExtractBaseAST visitFile(GenevaERSParser.FileContext ctx) { 
        LFAstNode lf =  (LFAstNode) ASTFactory.getNodeOfType(ASTFactory.Type.LF);
        String lfpf = ctx.children.get(1).getText();
		String[] parts = lfpf.split("\\.");
        Repository.getDependencyCache().setNamedLfPfAssoc(lfpf, dataProvider.findPFAssocID(parts[0], parts[1]));
        //Resolve the LF.PF name
        lf.resolve(ctx.children.get(1).getText());
        return lf; 
    }

    @Override public ExtractBaseAST visitExitArg(GenevaERSParser.ExitArgContext ctx) { 
        ExtractBaseAST retVal = null;
        if(ctx.getChildCount() == 1) {
            retVal = visit(ctx.children.get(0));
        }
        if(ctx.getChildCount() == 5) {
            retVal = visit(ctx.children.get(1));
            retVal.addChildIfNotNull(visit(ctx.children.get(3)));
        }
        return retVal; 
    }

    @Override public ExtractBaseAST visitWriteExit(GenevaERSParser.WriteExitContext ctx) { 
        WriteExitNode we =  (WriteExitNode) ASTFactory.getNodeOfType(ASTFactory.Type.WRITEEXIT);
        String exitName = ctx.getText().replace("{", "");
        exitName = exitName.replace("}", ""); //Must be a better way of doing this
        dataProvider.findExitID(exitName, procedure);
        we.resolveExit(exitName, procedure);
        return we; 
    }

    @Override public ExtractBaseAST visitProcedure(GenevaERSParser.ProcedureContext ctx) {
        if(ctx.getChild(0).getText().equalsIgnoreCase("PROCEDURE") 
           || ctx.getChild(0).getText().equalsIgnoreCase("PROC")) {
            procedure = true;
        }
        return visitChildren(ctx); 
    }
  

    @Override 
    public ExtractBaseAST visitDateConstant(GenevaERSParser.DateConstantContext ctx) {
        if(ctx.getChild(0).getChildCount() > 0) {
            return visit(ctx.getChild(0));
        } else {
            StringAtomAST str = (StringAtomAST) ASTFactory.getNodeOfType(ASTFactory.Type.STRINGATOM);
            str.setValue(ctx.getText());
            return str;
        }
    }

  
    @Override 
    public ExtractBaseAST visitStringComp(GenevaERSParser.StringCompContext ctx) {
        //A String comparison is just the same as an arithmetic comparison
        //But the operators are restricted
        //And the operands must not be numeric
        ExtractBaseAST retval = null;
        if(ctx.getChildCount() == 3) {
            ExtractBaseAST lhs = visit(ctx.children.get(0));
            String op = ctx.children.get(1).getText();
            ExtractBaseAST rhs = visit(ctx.children.get(2));
            if(StringDataTypeChecker.allows(lhs, rhs, op)) {
                if(op.equalsIgnoreCase("CONTAINS") || op.equalsIgnoreCase("BEGINS_WITH") || op.equalsIgnoreCase("ENDS_WITH")) {
                    StringComparisonAST strcmp = (StringComparisonAST)ASTFactory.getNodeOfType(ASTFactory.Type.STRINGCOMP);
                    strcmp.addChildIfNotNull(lhs);
                    strcmp.setComparisonOperator(op);
                    strcmp.addChildIfNotNull(rhs);
                    restrictLeftAndRight(strcmp);
                    return strcmp; 
                } else {
                    ExprComparisonAST exprcmp = (ExprComparisonAST)ASTFactory.getNodeOfType(ASTFactory.Type.EXPRCOMP);
                    exprcmp.addChildIfNotNull(lhs);
                    exprcmp.setComparisonOperator(op);
                    exprcmp.addChildIfNotNull(rhs);
                    return exprcmp; 
                }
            } else {
                StringComparisonAST strcmp = (StringComparisonAST)ASTFactory.getNodeOfType(ASTFactory.Type.STRINGCOMP);
                strcmp.addError("Incompatable data types");
                return strcmp; 
            }
        }
        //should be an error node 
        return retval;
    }

    private void restrictLeftAndRight(StringComparisonAST strcmp) {
        // For the moment as a fix for MR95 limit the RHS of LEFT and RIGHT to strings
        String op = strcmp.getOp();
        if(op.equalsIgnoreCase("BEGINS_WITH") || op.equalsIgnoreCase("ENDS_WITH")) {
            ExtractBaseAST rhs = (ExtractBaseAST)(strcmp.getChild(1));
            if(rhs.getType() != ASTFactory.Type.STRINGATOM) {
                strcmp.addError("Only string constants allowed after a 'BEGINS_WITH' or an 'ENDS_WITH'");
            }
        }
    }

    @Override public ExtractBaseAST  visitExprConcatString(GenevaERSParser.ExprConcatStringContext ctx) {
        if(ctx.getChildCount() > 1) {
            //The terms will be on the odd child nodes
            StringConcatinationAST strconcat = (StringConcatinationAST) ASTFactory.getNodeOfType(ASTFactory.Type.STRINGCONCAT);
            int c=0; 
            while(c<ctx.getChildCount()) {
                ParseTree n = ctx.children.get(c);
                ExtractBaseAST concatNode = visit(n);
                if(StringDataTypeChecker.allowConcatNode(concatNode)) {
                    strconcat.addChildIfNotNull(concatNode);
                } else {
                    strconcat.addError("Incompatable data type for " + n.getText());
                }
                c+=2;
            }
            return strconcat;
        } else {
            return visitChildren(ctx); 
        }
     }
    
    @Override  public ExtractBaseAST visitRight(GenevaERSParser.RightContext ctx) {
        RightASTNode rn = (RightASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.RIGHT);
        rn.addChildIfNotNull(visit(ctx.getChild(2)));
        rn.setLength(ctx.getChild(4).getText());
        return rn;
     }
  
    @Override  public ExtractBaseAST visitLeft(GenevaERSParser.LeftContext ctx) {
        LeftASTNode ln = (LeftASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.LEFT);
        ln.addChildIfNotNull(visit(ctx.getChild(2)));
        ln.setLength(ctx.getChild(4).getText());
        return ln;
     }
  
    @Override  public ExtractBaseAST visitSubstr(GenevaERSParser.SubstrContext ctx) {
        SubStringASTNode sn = (SubStringASTNode) ASTFactory.getNodeOfType(ASTFactory.Type.SUBSTR);
        //Subtring may have one or two numbers
        //start and len
        //or len only ... so really just a left
        if(ctx.getChildCount() == 6) {
            sn.addChildIfNotNull(visit(ctx.getChild(2)));
            resolveLength(ctx.getChild(4).getText(), sn);
        } else if(ctx.getChildCount() == 8) {
            sn.addChildIfNotNull(visit(ctx.getChild(2)));
            sn.setStartOffest(ctx.getChild(4).getText());
            resolveLength(ctx.getChild(6).getText(), sn);
        }
        return sn;
    }

    private void resolveLength(String len, SubStringASTNode sn) {
        Integer parsedLen = checkLengthOkay(len);
        if(parsedLen == null || parsedLen.equals(0) || parsedLen < 0) {
            sn.addError(String.format("The length %s is not valid", len));
        } else {
            sn.setLength(len);                
        }
    }

    private Integer checkLengthOkay(String len) {
        try {
            return Integer.parseInt(len);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override public ExtractBaseAST visitString(GenevaERSParser.StringContext ctx) { 
        StringAtomAST str = (StringAtomAST) ASTFactory.getNodeOfType(ASTFactory.Type.STRINGATOM);
        str.setValue(ctx.getText());
        return str;
    }

    @Override public ExtractBaseAST visitExtractArg(GenevaERSParser.ExtractArgContext ctx) { 
        WriteExtractNode wen =  (WriteExtractNode) ASTFactory.getNodeOfType(ASTFactory.Type.WRITEEXTRACT);
        wen.setFileNumber(Integer.parseInt(ctx.children.get(2).getText()));
        return wen; 
    }

    @Override public ExtractBaseAST visitDateFunc(GenevaERSParser.DateFuncContext ctx) { 
        DateFunc df =  (DateFunc)ASTFactory.getNodeOfType(ASTFactory.Type.DATEFUNC);
        df.resolve(ctx.getChild(2).getText(), ctx.getChild(4).getText());
        return df; 
    }

    @Override public ExtractBaseAST visitUnaryInt(GenevaERSParser.UnaryIntContext ctx) { 
        UnaryInt ui =  (UnaryInt) ASTFactory.getNodeOfType(ASTFactory.Type.UNARYINT);
        ui.setValue(ctx.getText());
        return ui; 
    }

    @Override public ExtractBaseAST visitColRef(GenevaERSParser.ColRefContext ctx) { 
        ColumnRefAST colRef = (ColumnRefAST) ASTFactory.getNodeOfType(ASTFactory.Type.COLUMNREF);
        String col = ctx.getText();
        String[] bits = col.split("\\.");
        ViewNode view = dataProvider.getView(viewSource.getViewId());
        colRef.setViewColumn(view.getColumnNumber(Integer.parseInt(bits[1])));
        return colRef; 
    }

    @Override public ExtractBaseAST visitFiscalDate(GenevaERSParser.FiscalDateContext ctx) {
        FiscaldateAST rd = (FiscaldateAST) ASTFactory.getNodeOfType(ASTFactory.Type.FISCALDATE);
        rd.setValue(ctx.getChild(0).getText());
        if(ctx.getChildCount() > 3){
            rd.addChildIfNotNull(visit(ctx.getChild(2)));
        }
        return rd; 
    }

    @Override public ExtractBaseAST visitRepeat(GenevaERSParser.RepeatContext ctx) {
        RepeatAST rep = (RepeatAST) ASTFactory.getNodeOfType(ASTFactory.Type.REPEAT);
        rep.addChildIfNotNull(visit(ctx.getChild(2)));
        rep.setValue(ctx.getChild(4).getText());
        return rep;
     }
      
    public ExtractBaseAST visitBetweenFunc(GenevaERSParser.BetweenFuncContext ctx) {
        BetweenFunc btw = (BetweenFunc)ASTFactory.getNodeOfType(ASTFactory.Type.BETWEENFUNC);
        btw.setFunction(ctx.getChild(0).getText());
        btw.addChildIfNotNull(visit(ctx.getChild(2)));
        btw.addChildIfNotNull(visit(ctx.getChild(4)));
        return btw;
     }

     @Override public ExtractBaseAST  visitAll(GenevaERSParser.AllContext ctx) { 
        AllAST allAst = null;
        allAst = (AllAST) ASTFactory.getNodeOfType(ASTFactory.Type.ALL);
        allAst.setValue(ctx.string().getText().replace("\"", ""));
        return allAst ; 
    }

    public void setViewColumnSource(ViewColumnSource viewColumnSource) {
        this.viewColumnSource = viewColumnSource;
        int vsid = viewColumnSource.getViewSourceId();
        int viewID = viewColumnSource.getViewId();
        this.viewSource = dataProvider.getView(viewID).getViewSourceById(vsid);
    }

    public void setViewSource(ViewSource viewSource) {
        this.viewSource = viewSource;
    }

    public void setParent(ExtractBaseAST parent) {
        this.parent = parent;
    }

    public static void setDataProvider(CompilerDataProvider dp) {
        dataProvider = dp;
    }

}
