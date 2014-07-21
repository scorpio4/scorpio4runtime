package com.scorpio4.asq.sparql;
/*
 *

 */

import com.scorpio4.asq.ASQ;

import java.util.Date;

/**
 * Fact:Core (c) 2012
 * @author lee
 * Date: 30/07/13
 * Time: 1:12 PM
 *
 *
 */
public class SelectSPARQL extends GenericSPARQL {

	protected SelectSPARQL() {
	}

	public SelectSPARQL(ASQ where) {
		build(where, this.sparql);
	}

    protected void build(ASQ construct, ASQ where, StringBuilder sparql) {
	    prolog(where, sparql);
        sparql.append("CONSTRUCT ");
        if (construct.getPatterns().isEmpty())
            buildEmptyConstruct(sparql, construct);
        else
            buildWherePattern(sparql, construct);

		sparql.append("\nWHERE ");
		buildWherePattern(sparql, where);
	}

	private void prolog(ASQ where, StringBuilder sparql) {
		sparql.append("# ASQ generated on ").append(new Date()).append("\n");
		sparql.append("# @this <").append(where.getIdentity()).append(">\n");
		sparql.append("# @namespaces\n\n");
	}

	private void buildEmptyConstruct(StringBuilder sparql, ASQ construct) {
		sparql.append("{}");
    }

    protected void build(ASQ asq, StringBuilder sparql) {
		prolog(asq, sparql);
        sparql.append("SELECT DISTINCT").append(" ");
        buildProjections(sparql, asq);
        sparql.append("WHERE ");
        buildWherePattern(sparql, asq);
    }
}
