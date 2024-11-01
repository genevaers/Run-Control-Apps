package org.genevaers.compilers.format;

import java.util.Set;
import java.util.TreeSet;

import org.genevaers.compilers.extract.astnodes.ExtractBaseAST;
import org.genevaers.compilers.format.astnodes.AndOp;
import org.genevaers.compilers.format.astnodes.ColRef;
import org.genevaers.compilers.format.astnodes.EmittableFormatASTNode;
import org.genevaers.compilers.format.astnodes.FormatASTFactory;
import org.genevaers.compilers.format.astnodes.FormatASTFactory.Type;
import org.genevaers.grammar.GenevaFormatBaseVisitor;
import org.genevaers.grammar.GenevaFormatParser;
import org.genevaers.grammar.GenevaFormatParser.CalcstmtContext;
import org.genevaers.grammar.GenevaFormatParser.ExprArithAddSubContext;
import org.genevaers.grammar.GenevaFormatParser.ExprArithMulDivContext;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
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


import org.genevaers.compilers.format.astnodes.FormatBaseAST;
import org.genevaers.compilers.format.astnodes.FormatErrorAST;
import org.genevaers.compilers.format.astnodes.NotOP;
import org.genevaers.compilers.format.astnodes.NumConst;
import org.genevaers.compilers.format.astnodes.OrOP;

public class BuildGenevaFormatASTVisitor extends GenevaFormatBaseVisitor<FormatBaseAST>{

    private boolean fromFilter;
	private Set<Integer> columnRefs = new TreeSet<>();
    private FormatBaseAST assign;

    BuildGenevaFormatASTVisitor(boolean fromFilter) {
        this.fromFilter = fromFilter;
    }

    @Override public FormatBaseAST visitGoal(GenevaFormatParser.GoalContext ctx) { 
        FormatBaseAST.resetOffset();
        FormatBaseAST froot;
        if(fromFilter) {
            froot = FormatASTFactory.getNodeOfType(Type.FORMATFILTERROOT);
            froot.addChildIfNotNull(visit(ctx.fstmt()));
        } else {
            froot = visit(ctx.fstmt());
        }
        shouldVisitNextChild(ctx.getRuleContext(), froot);
        return froot;
    }

    @Override public FormatBaseAST visitFstmt(GenevaFormatParser.FstmtContext ctx) { 
        int num = ctx.getChildCount();
        if (fromFilter) {
            FormatBaseAST filterNode = null;
            if (ctx.getChild(0) == ctx.SELECTIF()) {
                filterNode = FormatASTFactory.getNodeOfType(Type.SELECTIF);
            } else if (ctx.getChild(0) == ctx.SKIPIF()) {
                filterNode = FormatASTFactory.getNodeOfType(Type.SKIPIF);
            }
            if (num == 4 && filterNode != null) {
                filterNode.addChildIfNotNull(visit(ctx.getChild(2)));
            }
            NumConst trueCase = new NumConst();
            trueCase.setValue("1");
            filterNode.addChildIfNotNull(trueCase);
            NumConst falseCase = new NumConst();
            falseCase.setValue("0");
            filterNode.addChildIfNotNull(falseCase);
            return filterNode;
        } else {
            return visitChildren(ctx);
        }
    }

    @Override
    public FormatBaseAST visitCalcstmt(CalcstmtContext ctx) {
        switch(ctx.getChildCount()) {
            case 1:
            return visitChildren(ctx);

            case 3:
            //FormatBaseAST assign = null;
            if(ctx.getChild(0).getText().equalsIgnoreCase("COLUMN")) {
                assign = FormatASTFactory.getNodeOfType(Type.COLASSIGN);
                assign.addChildIfNotNull(visitChildren(ctx));
            } else {
                //not sure what the else is here
            }
            return assign;

            case 5: // IF pred THEN stmnt ENDIF
            {
                if (ctx.getChild(0).getText().equalsIgnoreCase("IF")) {
                    FormatBaseAST ifNode = FormatASTFactory.getNodeOfType(FormatASTFactory.Type.IF);
                    ifNode.addChildIfNotNull(visit(ctx.getChild(1)));
                    ifNode.addChildIfNotNull(visit(ctx.getChild(3)));
                    return ifNode;
                } else {
                    //Error? Can this occur?
                    return null;
                }
            }
            case 7: // IF pred THEN stmnt ELSE stmnt ENDIF
            {
                if (ctx.getChild(0).getText().equalsIgnoreCase("IF")) {
                    FormatBaseAST ifNode = FormatASTFactory.getNodeOfType(FormatASTFactory.Type.IF);
                    ifNode.addChildIfNotNull(visit(ctx.getChild(1)));
                    ifNode.addChildIfNotNull(visit(ctx.getChild(3)));
                    ifNode.addChildIfNotNull(visit(ctx.getChild(5)));
                    return ifNode;
                }
                else {
                    //Error?
                    return null;
                }
            }
            default:
            return null;
        }
    }

    @Override public FormatBaseAST visitPredicate(GenevaFormatParser.PredicateContext ctx) { 
        return visitChildren(ctx); 
    }

    @Override public FormatBaseAST visitExprBoolOr(GenevaFormatParser.ExprBoolOrContext ctx) { 
        if(ctx.getChildCount() == 1) {
            return visitChildren(ctx); 
        } else {
            // Iterate through the nodes.
            // Should be in pattern N And N And N And N....
            int childNum = 0;
            OrOP orOp = (OrOP) FormatASTFactory.getNodeOfType(FormatASTFactory.Type.OROP);
            while(childNum < ctx.getChildCount()) {
                orOp.addChildIfNotNull(visit(ctx.getChild(childNum)));
                childNum+=2;
            }
            return orOp;
        }
    }

    @Override public FormatBaseAST visitExprBoolAtom(GenevaFormatParser.ExprBoolAtomContext ctx) {
        if(ctx.getChildCount() == 3) {
            return visit(ctx.getChild(1));
        } else {
            return visitChildren(ctx); 
        }
     }
  
  

     @Override
     public FormatBaseAST visitExprBoolUnary(GenevaFormatParser.ExprBoolUnaryContext ctx) {
         // If there are two children may be a NOT
         // So probably need an node here to manage
         if (ctx.getChildCount() == 1) {
             return visitChildren(ctx);
         } else if (ctx.getChild(0).getText().equalsIgnoreCase("NOT")) {
             NotOP notop = (NotOP) FormatASTFactory.getNodeOfType(FormatASTFactory.Type.NOTOP);
             notop.addChildIfNotNull(visit(ctx.getChild(1)));
             return notop;
         } else {
             FormatErrorAST errs = (FormatErrorAST) FormatASTFactory.getNodeOfType(FormatASTFactory.Type.ERRORS);
             errs.setError(null, fromFilter);
            return errs;
         }
     }

    @Override
    public FormatBaseAST visitExprArithAddSub(ExprArithAddSubContext ctx) {
        if (ctx.getChildCount() > 1) {
            int nodenum = 1;
            assign.addChildIfNotNull(visit(ctx.getChild(nodenum - 1)));
            while (nodenum < ctx.getChildCount()) {
                assign.addChildIfNotNull(visit(ctx.getChild(nodenum + 1)));
                if(nodenum < ctx.getChildCount()) {
                    FormatBaseAST opNode = FormatASTFactory.getArithNode(ctx.getChild(nodenum).getText());
                    assign.addChildIfNotNull(opNode);
                } else {
                }
                nodenum += 2;
            }
            return null;
        } else {
            return visitChildren(ctx);
        }
    }

    @Override
    public FormatBaseAST visitExprArithMulDiv(ExprArithMulDivContext ctx) {
        FormatBaseAST arithNode = null;
        if(ctx.getChildCount() == 3) {
            arithNode = FormatASTFactory.getArithNode(ctx.getChild(1).getText());
            if(arithNode != null) {
                arithNode.addChildIfNotNull(visit(ctx.getChild(0)));
                arithNode.addChildIfNotNull(visit(ctx.getChild(2)));
            }
            return arithNode; 
        } else {
            FormatBaseAST n = visitChildren(ctx); 
            return n;
        }
    }

 
	@Override public FormatBaseAST visitExprBoolAnd(GenevaFormatParser.ExprBoolAndContext ctx) { 

        if(ctx.getChildCount() == 1) {
            return visitChildren(ctx); 
        } else {
            // Iterate through the nodes.
            // Should be in pattern N And N And N And N....
            int childNum = 0;
            AndOp andOp = (AndOp) FormatASTFactory.getNodeOfType(FormatASTFactory.Type.ANDOP);
            while(childNum < ctx.getChildCount()) {
                andOp.addChildIfNotNull(visit(ctx.getChild(childNum)));
                childNum+=2;
            }
            return andOp;
        }
    }

    @Override public FormatBaseAST visitExprComp(GenevaFormatParser.ExprCompContext ctx) { 
        if(ctx.getChildCount() == 3) {
            FormatBaseAST comp = (FormatBaseAST) FormatASTFactory.getComparisonNode(ctx.getChild(1).getText());
            comp.addChildIfNotNull(visit(ctx.getChild(0)));
            comp.addChildIfNotNull(visit(ctx.getChild(2)));
            return comp;
        } else {
            FormatBaseAST n = visitChildren(ctx); 
            return n; 
        }
    }


	@Override public FormatBaseAST visitStmtIfSelect(GenevaFormatParser.StmtIfSelectContext ctx) { 
        FormatBaseAST si = FormatASTFactory.getNodeOfType(FormatASTFactory.Type.SELECTIF);
        si.addChildIfNotNull(visitChildren(ctx));
        return si; 
    }

    @Override public FormatBaseAST visitStmtIfSkip(GenevaFormatParser.StmtIfSkipContext ctx) { 
        FormatBaseAST si = FormatASTFactory.getNodeOfType(FormatASTFactory.Type.SKIPIF);
        visitChildren(ctx); 
        return si;
    }

    @Override public FormatBaseAST visitExprArithUnary(GenevaFormatParser.ExprArithUnaryContext ctx) { 
        //This could have a leading minus sign
        //How do we manage that?
        //So probably need an node here to manage - or add and attribute to the child?
        //Does not make sense for a string
        //NOTE Parser does not tread -"fred" as an error
        FormatBaseAST atom;
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


	@Override public FormatBaseAST visitExprArithAtom(GenevaFormatParser.ExprArithAtomContext ctx) { 
        //if were here and the only child node is as terminal it must be a string const

        //Can also be brackets around the beast - strip off matching sets
        if(ctx.getChildCount() == 3) {
            return visit(ctx.getChild(1)); 
        } else {
            return visitChildren(ctx); 
        }
    }


    @Override public FormatBaseAST visitColumnRef(GenevaFormatParser.ColumnRefContext ctx) { 
        ColRef cr = (ColRef) FormatASTFactory.getNodeOfType(Type.COLREF);
        String colText = ctx.getText();
        cr.setText(colText);
        String[] bits = colText.split("\\.");
        columnRefs.add(Integer.valueOf(bits[1]));
        return cr; 
    }

    @Override public FormatBaseAST visitNum(GenevaFormatParser.NumContext ctx) { 
        NumConst nc = (NumConst) FormatASTFactory.getNodeOfType(Type.NUMCONST);
        nc.setValue(ctx.getText());
        return nc; 
    }

    public Set<Integer> getColumnRefs() {
        return columnRefs;
    }

}
