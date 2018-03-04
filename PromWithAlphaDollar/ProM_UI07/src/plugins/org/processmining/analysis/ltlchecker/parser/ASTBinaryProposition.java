/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.ltlchecker.parser;

/* Generated By:JJTree: Do not edit this line. ASTBinaryProposition.java */

public class ASTBinaryProposition extends SimpleNode {
	public ASTBinaryProposition(int id) {
		super(id);
	}

	public ASTBinaryProposition(LTLParser p, int id) {
		super(p, id);
	}

	public String asParseableString() {
		String result = " ( ";
		
		assert(children != null);
		assert(children.length == 2);
		assert(children[0] != null);
		assert(children[1] != null);
		
		result += ((SimpleNode) children[0]).asParseableString();
		
		switch (getType()) {
		case AND: result += "/\\"; break;
		case OR: result += "\\/"; break;
		case IMPLIES: result += "->"; break;
		case BIIMPLIES: result += "<->"; break;
		case UNTIL: result += "_U"; break;
		default:
			assert(false);
		};
		
		result += ((SimpleNode) children[1]).asParseableString();
		
		result += " ) ";
		return result;
	}
}
