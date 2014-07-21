package com.scorpio4.asq.core;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.oops.ASQException;
import com.scorpio4.util.Bindable;
import com.scorpio4.util.io.JarHelper;

import java.util.*;

/**
 * Fact:Core (c) 2012
 * @author lee
 * Date: 30/07/13
 * Time: 10:37 AM
 *
 * The default implemenation for an Abstract Semantic Query
 */
public class BasicASQ implements ASQ, Bindable {
	private String identity = null;
	private Map binding = null;
	private List<Pattern> patterns = new ArrayList();
    Set<Pattern> functions = new HashSet();
    private Map<String,Term> bindings = new HashMap();
    private Properties ns = JarHelper.loadProperties("META-INF/rdf.namespace.props");
	private List<Term> selects = new ArrayList();

    public BasicASQ(String _baseURI) {
        this.identity = _baseURI;
        this.binding = new HashMap();
    }

	public BasicASQ(String _baseURI, Map meta) {
		this.identity = _baseURI;
		bind(meta);
	}

	public BasicASQ(String _baseURI, Bindable asq) {
		this.identity = _baseURI;
		bind(asq.getBindings());
	}

    public void bind(String s, Object o) {
        this.binding.put(s,o);
    }

	public String getIdentity() {
		return this.identity;
	}

	public ASQ where(String _this, String _has, String _that) throws ASQException {
		Pattern pattern = new Pattern(_this, _has, _that, false);
        return where(pattern);
	}

    public ASQ where(String _this, String _has, String _that, String type) throws ASQException {
        Pattern pattern = new Pattern(_this, _has, _that, type, false);
        return where(pattern);
    }

    public ASQ where(String _this, String _has, String _that, String _type, boolean optional) throws ASQException {
        Pattern pattern = new Pattern(_this, _has, _that, _type, optional);
        return where(pattern);
    }

    public ASQ where(String _this, String _has, String _that, boolean optional) throws ASQException {
        Pattern pattern = new Pattern(_this, _has, _that, optional);
        return where(pattern);
    }


    public ASQ where(Pattern pattern) {
	    pattern.bind(this);
        if (pattern.isFunctional()) functions.add(pattern);
        else patterns.add(pattern);
        return this;
    }

    // syntax sugar
    public ASQ optional(String _this, String _has, String _that) throws ASQException {
        return where(_this, _has, _that, null, true);
    }

    // syntax sugar
    public ASQ optional(String _this, String _has, String _that, String _type) throws ASQException {
        return where(_this, _has, _that, _type, true);
    }

    @Override
    public ASQ filter(String _fn) throws ASQException {
        return where( getIdentity(), _fn, getIdentity(), null, false);
    }

    public void clear() {
		this.patterns.clear();
	}

	public Collection<Pattern> getPatterns() {
		return this.patterns;
	}

    public Set<Pattern> getFunctions() {
        return this.functions;
    }

    @Override
    public void bind(Term term) {
	    term.bind(this);
	    if (term.isBinding()) {
		    this.bindings.put(term.toString(), term);
	    }
    }

    @Override
    public Map<String,Term> getBindings() {
        return this.bindings;
    }

    public String cname(String _this) {
        int ix = _this.indexOf(":");
        if (ix>0) {
            String baseURI = (String) ns.get( _this.substring(0,ix) );
            return baseURI+_this.substring(ix+1);
        }
        return _this;
    }

	public void select(Term term) {
		selects.add(term);
	}

	public Collection<Term> getSelects() {
		if (selects.isEmpty()) return getBindings().values();
		return selects;
	}

    public boolean isEmpty() {
        return patterns.isEmpty();
    }

	@Override
	public void bind(Map bindings) {
		
	}
}

