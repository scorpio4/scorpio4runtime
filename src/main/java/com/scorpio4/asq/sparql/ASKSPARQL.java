package com.scorpio4.asq.sparql;

import com.scorpio4.asq.ASQ;
import com.scorpio4.oops.ASQException;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.asq.sparql
 * User  : lee
 * Date  : 23/07/2014
 * Time  : 11:48 AM
 */
public class AskSPARQL extends GenericSPARQL {

	public AskSPARQL(ASQ asq) throws ASQException {
		build(asq, sparql);
	}

	protected void build(ASQ where, StringBuilder sparql) {
		sparql.append("ASK");
		sparql.append("\nWHERE ");
		buildWherePattern(sparql, where);
	}

}
