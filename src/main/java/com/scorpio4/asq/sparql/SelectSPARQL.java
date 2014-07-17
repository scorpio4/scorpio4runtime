package com.scorpio4.asq.sparql;
/*
 *

 */

import com.scorpio4.asq.ASQ;

/**
 * Fact:Core (c) 2012
 * User: lee
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
        sparql.append("\n# ASQ: " + where.getIdentity() + "\n");
        sparql.append("CONSTRUCT ");
        if (construct.getPatterns().isEmpty())
            buildEmptyConstruct(sparql, construct);
        else
            buildWherePattern(sparql, construct);

		sparql.append("\nWHERE ");
		buildWherePattern(sparql, where);
	}

    private void buildEmptyConstruct(StringBuilder sparql, ASQ construct) {


    }

    protected void build(ASQ asq, StringBuilder sparql) {
        sparql.append("\n# ASQ: " + asq.getIdentity() + "\n");
        sparql.append("SELECT DISTINCT").append(" ");
        buildProjections(sparql, asq);
        sparql.append("WHERE ");
        buildWherePattern(sparql, asq);
    }
}
