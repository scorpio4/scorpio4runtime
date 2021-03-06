package com.scorpio4.vendor.sesame.util;

import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.sparql.SelectSPARQL;
import com.scorpio4.iq.bean.ConvertsType;
import com.scorpio4.util.bean.XSD2POJOConverter;
import com.scorpio4.oops.FactException;
import com.scorpio4.vocab.COMMONS;
import org.apache.camel.Converter;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Scorpio4 (c) 2014
 * Module: com.scorpio4.vendor.sesame.util
 * @author lee
 * Date  : 17/06/2014
 * Time  : 10:16 PM
 */
@Converter
public class SesameHelper {
    private static final Logger log = LoggerFactory.getLogger(SesameHelper.class);

	public static void defaultNamespaces(RepositoryConnection to) throws RepositoryException {
		defaultNamespaces(to, null);
	}

	public static void defaultNamespaces(RepositoryConnection to, String identity) throws RepositoryException {
		to.begin();
		// W3C Vocabularies
		to.setNamespace("rdf", COMMONS.RDF);
		to.setNamespace("rdfs", COMMONS.RDFS);
		to.setNamespace("owl", COMMONS.OWL);
		to.setNamespace("skos", COMMONS.SKOS);
		to.setNamespace("dc", COMMONS.DC);
		to.setNamespace("xsd", COMMONS.XSD);
		to.setNamespace("acl", COMMONS.ACL);
		// Scorpio4 Active Vocabularies
		to.setNamespace("flo", COMMONS.CORE+"flo/");
		to.setNamespace("bean", COMMONS.CORE+"bean/");
		to.setNamespace("asq", COMMONS.CORE+"asq/");
		to.setNamespace("core", COMMONS.CORE);
		if (identity!=null) {
			to.setNamespace("self", identity);
		}
		to.commit();
	}

	public static StringBuilder toSPARQLPrefix(RepositoryConnection connection) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		RepositoryResult<Namespace> namespaces = connection.getNamespaces();
		StringBuilder namespace$ = new StringBuilder();
		while(namespaces.hasNext()) {
			Namespace namespace = namespaces.next();
			namespace$.append("PREFIX ").append(namespace.getPrefix()).append(": <").append(namespace.getName()).append( ">\n");
		}
		log.trace("SPARQL prefices: " + namespace$);
		return namespace$;
	}

	public static Collection<Map> toMapCollection(RepositoryConnection connection, String sparql) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		return toMapCollection(connection, sparql, new XSD2POJOConverter());
	}

	public static Collection<Map> toMapCollection(RepositoryConnection connection, ASQ asq) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		SelectSPARQL sparql = new SelectSPARQL(asq);
		return toMapCollection(connection, sparql.toString(), new XSD2POJOConverter());
	}

	public static Collection<Map> toMapCollection(RepositoryConnection connection, String sparql, ConvertsType convertsType) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		sparql = explodePragmas(connection, sparql);
		TupleQuery tuple = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
		return toMapCollection(tuple,convertsType);
	}

	@Converter
	public static Collection<Map> toMapCollection(TupleQuery tuple, ConvertsType convertsType) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		return toMapCollection(tuple.evaluate(),convertsType);
	}

	@Converter
	public static Collection<Map> toMapCollection(TupleQueryResult result, ConvertsType convertsType) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        Collection reply = new ArrayList();
        while(result.hasNext()) {
            BindingSet bindingSet = result.next();
	        log.trace("SPARQL binding: "+bindingSet.getBindingNames());
            Map map = new HashMap();
            for(Binding name:bindingSet) {
	            if (convertsType!=null && name.getValue() instanceof Literal) {
		            Literal literal = (Literal)name.getValue();
		            if (literal.getDatatype()!=null) {
			            Class aClass = XSD2POJOConverter.convertXSDToClass(literal.getDatatype().stringValue());
			            map.put(name.getName(), convertsType.convertToType(literal.stringValue(), aClass) );
		            } else
			            map.put(name.getName(), name.getValue().stringValue());
	            }
	            else
		            map.put(name.getName(), name.getValue().stringValue());
            }
            reply.add(map);
        }
        log.trace("SPARQL found: "+reply.size()+" results");
        return reply;
    }

	@Converter
	public static Collection<Statement> toStatements(TupleQuery query) throws QueryEvaluationException {
		return toStatements(query.evaluate());
	}

	@Converter
	public static Collection<Statement> toStatements(TupleQueryResult result) throws QueryEvaluationException {
		Collection statements = new ArrayList();
		while (result.hasNext()) {
			Statement stmt = (Statement) result.next();
			statements.add(stmt);
		}
		result.close();
		return statements;
	}

	public static Map<Object, Map> toMap(RepositoryConnection connection, String sparql, ConvertsType convertsType, String idAttribute) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, FactException {
		sparql = explodePragmas(connection, sparql);
		TupleQuery tuple = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
		return toMap(new HashMap(), tuple.evaluate(), convertsType, idAttribute);
	}

	public static Map<Object, Map> toMap(Map items, RepositoryConnection connection, String sparql, ConvertsType convertsType, String idAttribute) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, FactException {
		sparql = explodePragmas(connection, sparql);
		TupleQuery tuple = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
		return toMap(items, tuple.evaluate(), convertsType, idAttribute);
	}

	public static Map<Object, Map> toMap(Map items, TupleQueryResult result, ConvertsType convertsType, String idAttribute ) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, FactException {
		while(result.hasNext()) {
			BindingSet bindingSet = result.next();
			log.trace("SPARQL binding: "+bindingSet.getBindingNames());
			Map map = new HashMap();
			for(Binding name:bindingSet) {
				if (convertsType!=null && name.getValue() instanceof Literal) {
					Literal literal = (Literal)name.getValue();
					if (literal.getDatatype()!=null) {
						Class aClass = XSD2POJOConverter.convertXSDToClass(literal.getDatatype().stringValue());
						map.put(name.getName(), convertsType.convertToType(literal.stringValue(), aClass) );
					} else
						map.put(name.getName(), name.getValue().stringValue());
				}
				else
					map.put(name.getName(), name.getValue().stringValue());
			}
			Object id = map.get(idAttribute);
			if (id==null) throw new FactException("Missing mandatory ?"+idAttribute);
			items.put(id, map);
		}
		log.trace("SPARQL found: "+items.size()+" items");
		return items;
	}


	public static String explodePragmas(RepositoryConnection connection, String sparql) throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {
		String namespaces = toSPARQLPrefix(connection).toString();
		return sparql.replace("@namespaces", "\n"+ namespaces);
	}
}
