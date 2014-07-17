package com.scorpio4.asq.sparql;
/*
 *

 */

import com.scorpio4.util.map.Factualize;
import com.scorpio4.util.string.PrettyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * User: lee
 * Date: 9/11/12
 */
public class SPARQLBuilder {
	private static final Logger log = LoggerFactory.getLogger(SPARQLBuilder.class);
	String graph = null;

	public SPARQLBuilder() {
	}

	public SPARQLBuilder(String graph) {
		this.graph = graph;
	}

	public String creator(String uri, Map<String, Object> model) {
		StringBuilder sparql = new StringBuilder();
		sparql.append( buildGraph() );
		sparql.append("INSERT {\n").append( buildFields(uri, model) ).append("\n}");
		return sparql.toString();
	}

	public String updater(String uri, Map<String, Object> model) {
		StringBuilder sparql = new StringBuilder();
		sparql.append( buildGraph() );
		sparql.append("DELETE {\n").append( buildOptionals(uri, model) ).append("}\n");
		sparql.append("INSERT {\n").append( buildFields(uri, model) ).append("\n}");
		sparql.append("\nWHERE {\n").append( buildWhere(uri, model) ).append("\n}");
		return sparql.toString();
	}

	public String deleter(String uri, Map<String, Object> model) {
		StringBuilder sparql = new StringBuilder();
		sparql.append( buildGraph() );
		sparql.append("DELETE {\n").append( buildOptionals(uri, model) ).append("}\n");
		sparql.append("\nWHERE {\n").append( buildWhere(uri, model) ).append("\n}");
		return sparql.toString();
	}


	public String buildFields(String uri, Map<String, Object> model) {
		List fields = Factualize.scalars(model);
		StringBuilder sparql = new StringBuilder();
		Object value;

		appendURI(sparql, uri);

		for(int i=0;i<fields.size();i++) {
			Object field = fields.get(i);
			value = model.get(field.toString());
			if (value!=null) {
				try {
					value = new URI(value.toString());
				} catch(Exception e) {
					log.debug("ERROR: converting to URI", e);
				}
			}
			sparql.append("\t\n");
			appendURI(sparql, field);
			sparql.append(" \"").append(value).append("\";");
		}
		return sparql.toString();
	}

	public String buildOptionals(String uri, Map<String, Object> model) {
		List fields = Factualize.scalars(model);
		StringBuilder sparql = new StringBuilder();
		Object value, field;

		for(int i=0;i<fields.size();i++) {
			field = fields.get(i);
			value = model.get( field.toString() );
			sparql.append("\t");
			appendURI(sparql, uri);
			appendURI(sparql, field);
			sparql.append(" ?").append(PrettyString.sanitize(field)).append(". \n");
		}
		return sparql.toString();
	}

	public String buildWhere(String uri, Map<String,Object> model) {
		StringBuilder sparql = new StringBuilder();
		appendURI(sparql, uri).append("a rdfs:Resource");
		return sparql.toString();
	}

	private StringBuilder appendURI(StringBuilder str, Object uri) {
		str.append("<").append(uri.toString()).append("> ");
		return str;
	}

	public String buildGraph() {
		return graph==null?"":"WITH <"+graph+"> ";
	}
}
