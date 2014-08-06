package com.scorpio4.asq.core;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.oops.ASQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Fact:Core (c) 2012
 * @author lee
 * Date: 30/07/13
 * Time: 10:38 AM
 *
 * A collection of terms used to specify projection bindings, constrain joins or connect one branch to another
 */
public class Pattern {
	private static final Logger log = LoggerFactory.getLogger(Pattern.class);

	private Term _this, _has, _that;
	private boolean optional = false;
	Collection<Pattern> nested = new ArrayList();

	public Pattern() {
	}

	public Pattern(String _this, String _has, String _that) throws ASQException {
		this(_this, _has, _that, false);
	}

	public Pattern(String _this, String _has, String _that, boolean _optional) throws ASQException {
		init(_this, _has, _that, _optional);
	}

	public Pattern(Pattern pattern) throws ASQException {
		this._this = new Term(pattern.getThis());
		this._has = new Term(pattern.getHas());
		this._that = new Term(pattern.getThat());
	}

	public void init(String _this, String _has, String _that, boolean _optional) throws ASQException {
        if (_this.equals("")|| _has.equals("") || _that.equals("")) throw new ASQException("urn:scorpio4:asq:core:oops:invalid-terms");
		this._this = new Term(_this);
		this._has = new Term(_has);
		this._that = new Term(_that);
		this.optional = _optional;
	}

    public Pattern(String _this, String _has, String _that, String type, boolean _optional) throws ASQException {
        if (_this.equals("")|| _has.equals("") || _that.equals("")) throw new ASQException("urn:scorpio4:asq:core:oops:invalid-terms");
        this._this = new Term(_this);
        this._has = new Term(_has);
        this._that = new LiteralTerm( _that, type);
	    this.optional = _optional;
    }

	public void bind(ASQ asq) {
        if (!isFunctional()) {
            asq.bind(this._this);
            asq.bind(this._has);
            asq.bind(this._that);
        } else {
            if (!this._this.toString().equals(this._that.toString())) {
	            asq.bind(this._that);
            }
        }
		for(Pattern pattern: nested) {
			pattern.bind(asq);
		}
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
		_this = new Term( term);
	}

	public void setHas(String term) throws ASQException {
		_has = new Term( term);
	}

	public void setPath(String term) throws ASQException {
		_has = new RawTerm( term);
	}

	public void setThat(String term) throws ASQException {
		_that = new Term( term);
	}

	public void setThat(String term, String xsdType) throws ASQException {
		_that = new Term( term, xsdType);
	}

	// is this clause optional?
	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional=optional;
	}

	// are any terms using a binding?
//	public boolean isBound() {
//        return this._this.isBinding() || this._has.isBinding() || this._that.isBinding();
//	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		toString(stringBuilder, 0);
		return stringBuilder.toString();
	}

	public void toString(StringBuilder root, int level) {
		for(int i=0;i<level;i++) root.append("\t");
		root.append( (isOptional() ? "OPTIONAL " : "WHERE "));
		root.append("").append(_this).append(" & ").append(_has).append(" & ").append(_that);
		root.append("\n");
		for(Pattern nest:nested) {
			nest.toString(root, level + 1);
		}
	}

	public void addNested(Pattern pattern) {
		nested.add(pattern);
	}

	public Collection<Pattern> getNested() {
		return nested;
	}

	public boolean isDefined() {
		return _that!=null&&_has!=null&&_this!=null;
	}

}
