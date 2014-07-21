package com.scorpio4.asq.sparql;
/*
 *

 */

import com.scorpio4.asq.ASQ;

/**
 * Fact:Core (c) 2012
 * @author lee
 * Date: 30/07/13
 * Time: 1:12 PM
 *
 *
 */
public class InsertSPARQL extends GenericSPARQL {

    public InsertSPARQL(ASQ asq) {
        build(asq, this.sparql);
    }

    protected void build(ASQ asq, StringBuilder sparql) {
        sparql.append("\n# ASQ: " + asq.getIdentity() + "\n");
        sparql.append("INSERT DATA \n");
        buildBindings(sparql, asq);
    }

}
