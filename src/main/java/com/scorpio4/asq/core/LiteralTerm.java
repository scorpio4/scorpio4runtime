package com.scorpio4.asq.core;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.oops.ASQException;

/**
 * Fact:Core (c) 2013
 * Module: com.factcore.fact.domain.core
 * User  : lee
 * Date  : 11/01/2014
 * Time  : 10:31 PM
 */
public class LiteralTerm extends Term {
    protected String type = null;

    public LiteralTerm(ASQ asq, String _that, String type) throws ASQException {
        super(asq, _that);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
