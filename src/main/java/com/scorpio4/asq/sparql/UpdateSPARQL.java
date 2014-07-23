package com.scorpio4.asq.sparql;
/*
 *

 */

import com.scorpio4.asq.ASQ;

/**
 * Fact:Core (c) 2013
 * Module: com.scorpio4.asq.core.sparql
 * @author lee
 * Date  : 11/01/2014
 * Time  : 11:23 PM
 */
public class UpdateSPARQL extends GenericSPARQL {

    public UpdateSPARQL(ASQ asq) {
        build(asq, asq, this.sparql);
    }

    public UpdateSPARQL(ASQ update, ASQ where) {
        build(update, where, this.sparql);
    }

    protected void build(ASQ update, ASQ where, StringBuilder sparql) {
        sparql.append("\n# ASQ: " + update.getIdentity() + "\n");
        sparql.append("UPDATE ");
        buildBindings(sparql, update);
        sparql.append("WHERE ");
        buildWherePattern(sparql, where);
    }
}
