package com.scorpio4.vendor.sesame.util;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 * Date: 13/01/13
 * Time: 10:21 PM
 * <p/>
 * Visits each RDF Statement in a SPARQL query, and collects bound Variables
 */
public class VariableCollector extends QueryModelVisitorBase<Exception> {
	Map variables = null;
	Map ranges = null;
	Map domains = null;

	public VariableCollector() {
		this.variables = new HashMap();
		this.ranges= new HashMap();
		this.domains = new HashMap();
	}

	public Map getVariables() {
		return this.variables;
	}

	public List<Object> getRanges(String varName) {
		return (List)this.ranges.get(varName);
	}

	public List<Object> getDomains(String varName) {
		return (List)this.domains.get(varName);
	}

	@Override
	public void meet(StatementPattern node) {
		String varName = null;
		varName = collectProjection(node.getSubjectVar());
		if (varName!=null) {
			collectDomain(varName, node.getPredicateVar());
		}
		varName = collectProjection(node.getPredicateVar());
		if (varName!=null) {
			collectRange(varName, node.getSubjectVar());
		}
		varName = collectProjection(node.getObjectVar());
		if (varName!=null) {
			collectRange(varName, node.getPredicateVar());
		}
		try {
			super.meet(node);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List collectRange(String varName, Var var) {
		List range = (List) this.ranges.get(varName);
		if (range==null) {
			range = new ArrayList();
			this.ranges.put(varName, range);
		}
		if (var.hasValue()) {
			range.add(var.getValue());
		}
		return range;
	}

	private List collectDomain(String varName, Var var) {
		List domain = (List) this.domains.get(varName);
		if (domain==null) {
			domain = new ArrayList();
			this.domains.put(varName, domain);
		}
		if (var.hasValue()) {
			domain.add(var.getValue());
		}
		return domain;
	}

	private String collectProjection(Var var) {
		if (var.getName()!=null && !var.isAnonymous()) {
			this.variables.put(var.getName(), var);
			return var.getName();
		}
		return null;
	}

}
