package com.scorpio4.asq.core;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.iq.bean.XSD2POJOConverter;
import com.scorpio4.oops.ASQException;
import com.scorpio4.util.string.PrettyString;
import com.scorpio4.vocab.COMMONS;

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
	private String xsdType = null;

	public Term(String _term) throws ASQException {
		this(_term, COMMONS.XSD+"anyURI");
	}

	public Term(String _term, String xsdType) throws ASQException {
        if (_term==null ||_term.equals("")) throw new ASQException("urn:scorpio4:asq:core:oops:invalid-term");
		this.term = _term.trim();
		this.xsdType =xsdType;
	}

	public Term(Term _term) throws ASQException {
		this.term = _term.toString();
	}

	public void bind(ASQ asq) {
		this.asq=asq;
		if (term.startsWith(asq.getIdentity())) term = "?"+term.substring(asq.getIdentity().length());
	}

    public boolean isBinding() {
	    if (asq==null) return false;
        return (term.startsWith(asq.getIdentity()) || isBinding(term));
    }


	protected boolean isBinding(String term) {
		return term.startsWith("?")|| term.startsWith(":")|| term.startsWith("#") || isDefined() || term.indexOf(":")<0;
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

	public String getXSDType() {
		return xsdType;
	}

	public Class getTypeClass() {
		return XSD2POJOConverter.convertXSDToClass(getXSDType());
	}
}
