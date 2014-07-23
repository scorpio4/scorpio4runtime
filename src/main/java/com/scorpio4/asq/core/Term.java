package com.scorpio4.asq.core;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.oops.ASQException;
import com.scorpio4.util.string.PrettyString;

/**
 * Fact:Core (c) 2012
 * @author lee
 * Date: 31/07/13
 * Time: 9:53 AM
 * <p/>
 * Encapsulates an ASQ Term. A term may be a (local or declared) binding or static
 * It accepts literal bindings prefixed with ? or : or # or localized URIs relative to the base URI
 *
 */
public class Term {
    protected ASQ asq = null;
	protected String term = null;

	public Term(String _term) throws ASQException {
        if (_term==null ||_term.equals("")) throw new ASQException("urn:scorpio4:fact:domain:oops:invalid-term");
		this.term = _term.trim();
	}

	public Term(Term _term) throws ASQException {
		this.term = _term.toString();
	}

	public void bind(ASQ asq) {
		this.asq=asq;
	}

    public boolean isBinding() {
	    if (asq==null) return false;
        return (term.startsWith("?")|| term.startsWith(":")|| term.startsWith("#") || isDefined() || term.indexOf(":")<0 );
    }

    public boolean isDefined() {
	    return (asq!=null && term!=null && term.startsWith(asq.getIdentity()));
    }

    public boolean isFunction() {
        int fx = term.indexOf("(");
        if (fx>0) return term.indexOf(")",fx)>fx;
        return false;
    }

    public String toString() {
        if (!isBinding()) return term;
        if (isDefined()) return PrettyString.prettySafe(term.substring(asq.getIdentity().length()));
        return term;
    }
}
