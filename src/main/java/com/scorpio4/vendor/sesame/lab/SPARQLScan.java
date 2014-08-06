package com.scorpio4.vendor.sesame.lab;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *

 */

import com.scorpio4.vendor.sesame.util.VariableCollector;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 *
 * Scans SPARQL and determines the variable bindings, and with their possible RDF types
 *
 * !! BROKEN !! Only here for educational purposes
 *
 */
public class SPARQLScan {
	private static final Logger log = LoggerFactory.getLogger(SPARQLScan.class);
	SPARQLParser parser = new SPARQLParser();

	public SPARQLScan() {

	}

	public SPARQLScan(String queryString) throws Exception {
		log.debug(queryString);

		Map<String, String> types = scanTypes(queryString);
		for (Map.Entry<String, String> type: types.entrySet()) {
			log.debug(type.getValue());
		}
	}

	public Map<String,String> scanTypes(String queryString) throws Exception {
		ParsedQuery query = parser.parseQuery(queryString, null);
		VariableCollector collector = new VariableCollector();
		query.getTupleExpr().visit(collector);
		Map typesOf = new HashMap();
		Map<String, Var> vars = collector.getVariables();
		for (Map.Entry<String, Var> var : vars.entrySet()) {
			StringBuilder sparql = new StringBuilder("SELECT DISTINCT ");
			String varName = var.getKey();
//			sparql.append("?").append(varName);
			sparql.append(" ?type WHERE { ?").append(varName).append(" a ?type.");

			List<Object> ranges = collector.getRanges(varName);
			if (ranges!=null) {
				sparql.append("{");
				for(Object range: ranges) {
					sparql.append("<" ).append(range.toString()).append("> rdfs:range ?type.");
				}
				sparql.append("}");
			}

			List<Object> domains = collector.getDomains(varName);
			if (domains!=null) {
				if (ranges!=null) sparql.append(" UNION ");
				sparql.append("{");
				for(Object domain: domains) {
					sparql.append("<" ).append(domain.toString()).append("> rdfs:domain ?type.");
				}
				sparql.append("}");
			}
			sparql.append("}");
			typesOf.put(varName, sparql.toString());
		}
		return typesOf;
	}
}
