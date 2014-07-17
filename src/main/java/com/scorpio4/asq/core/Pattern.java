package com.scorpio4.asq.core;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.oops.ASQException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Fact:Core (c) 2012
 * User: lee
 * Date: 30/07/13
 * Time: 10:38 AM
 *
 * A collection of terms used to specify projection bindings, constrain joins or connect one branch to another
 */
public class Pattern {
	private ASQ asq = null;
	private Term _this, _has, _that;
	private boolean optional = false;
	Collection<Pattern> nested = new ArrayList();

	public Pattern(ASQ _asq) {
		this.asq = _asq;
	}

	public Pattern(ASQ _asq, String _this, String _has, String _that) throws ASQException {
		this(_asq, _this, _has, _that, false);
	}

	public Pattern(ASQ _asq, String _this, String _has, String _that, boolean _optional) throws ASQException {
		init(_asq, _this, _has, _that, _optional);
	}

	public void init(ASQ _asq, String _this, String _has, String _that, boolean _optional) throws ASQException {
        if (_asq==null || _this == null || _has == null || _that == null) throw new ASQException("urn:factcore:fact:domain:oops:null-in-pattern");
        if (_this.equals("")|| _has.equals("") || _that.equals("")) throw new ASQException("urn:factcore:fact:domain:oops:invalid-terms");
		this.asq = _asq;
		this._this = new Term(asq,_this);
		this._has = new Term(asq,_has);
		this._that = new Term(asq,_that);
        if (!isFunctional()) {
            bind(this._this);
            bind(this._has);
            bind(this._that);
        } else {
            if(!this._this.toString().equals(this._that.toString())) bind(this._that);
        }
		this.optional = _optional;
	}

    public Pattern(BasicASQ _asq, String _this, String _has, String _that, String type, boolean _optional) throws ASQException {
        if (_asq==null || _this == null || _has == null || _that == null) throw new ASQException("urn:factcore:fact:domain:oops:null-in-pattern");
        if (_this.equals("")|| _has.equals("") || _that.equals("")) throw new ASQException("urn:factcore:fact:domain:oops:invalid-terms");
        this.asq = _asq;
        this._this = new Term(asq,_this);
        this._has = new Term(asq,_has);
        this._that = new LiteralTerm(asq, _that, type);
	    this.optional = _optional;
	    bind();
    }

	public void bind() {
        if (!isFunctional()) {
            bind(this._this);
            bind(this._has);
            bind(this._that);
        } else {
            if(!this._this.toString().equals(this._that.toString())) bind(this._that);
        }
    }

    private Term bind(Term term) {
        if ( term!=null && term.isBinding() )
	        asq.bind(term);
        return term;
    }

    public boolean isFunctional() {
        return this._has!=null&&this._has.isFunction();
    }

    public boolean isFilter() {
        return this._this.toString().equals(this._that.toString());
    }

	// get the subject clause
	public Term getThis() {
		return _this;
	}

	// get the predicate clause
	public Term getHas() {
		return _has;
	}

	// get the object clause
	public Term getThat() {
		return _that;
	}

	public void setThis(String term) throws ASQException {
		_this = new Term(asq, term);
	}

	public void setHas(String term) throws ASQException {
		_has = new Term(asq, term);
	}

	public void setThat(String term) throws ASQException {
		_that = new Term(asq, term);
	}

	public void setThat(String term, String xsdType) throws ASQException {
		_that = new LiteralTerm(asq, term, xsdType);
	}

	// is this clause optional?
	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional=optional;
	}

	// are any terms using a binding?
	public boolean isBound() {
        return this._this.isBinding() || this._has.isBinding() || this._that.isBinding();
	}

    protected ASQ getASQ() {
        return this.asq;
    }

	public String toString() {
		return toString(new StringBuilder(), 0);
	}

	public String toString(StringBuilder root, int level) {
		for(int i=0;i<level;i++) root.append("\t");
		root.append( (isOptional() ? "OPTIONAL " : "WHERE "));
		root.append(""+_this+" & "+_has+" & "+_that);
		root.append("\n");
		for(Pattern nest:nested) nest.toString(root, level + 1);
		return root.toString();
	}

	public void addNested(Pattern pattern) {
		pattern.bind();
		nested.add(pattern);
	}

	public Collection<Pattern> getNested() {
		return nested;
	}

	public boolean isDefined() {
		return _that!=null&&_has!=null&&_this!=null;
	}
}
