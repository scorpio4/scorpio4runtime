package com.scorpio4.vendor.sesame.asq;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.ASQParser;
import com.scorpio4.asq.core.BasicASQ;
import com.scorpio4.asq.core.Pattern;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 *
 * Scans SPARQL and converts what it can into ASQ
 *
 * !! BROKEN !! Only here for educational purposes
 *
 */
public class SPARQL2ASQ extends QueryModelVisitorBase<Exception> implements ASQParser  {
	private static final Logger log = LoggerFactory.getLogger(SPARQL2ASQ.class);
	BasicASQ basicASQ = null;

	public SPARQL2ASQ(String identity, String sparql) throws Exception {
		basicASQ = new BasicASQ(identity);
		SPARQLParser parser = new SPARQLParser();
		ParsedQuery query = parser.parseQuery(sparql, basicASQ.getIdentity());
		query.getTupleExpr().visit(this);
	}

	@Override
	public void meet(StatementPattern node) {
		try {
			log.debug("Meet: "+node);
			process(node);
		} catch (Exception e) {
			log.error(node.toString(), e);
		}
	}

	private void process(StatementPattern node) throws Exception {
		Pattern pattern = new Pattern();
		pattern.setThis(toTerm(node.getSubjectVar()));
		pattern.setHas(toTerm(node.getPredicateVar()));
		pattern.setThat(toTerm(node.getObjectVar()));

		basicASQ.where(pattern);

		log.debug("Pattern: "+pattern);
//		super.meet(node);
	}

	private String toTerm(Var var) {
		if (!var.isConstant()) {
			return basicASQ.getIdentity()+"#"+var.getName();
		}
		return var.getValue().toString();
	}

	@Override
	public ASQ getASQ() {
		return basicASQ;
	}
}
