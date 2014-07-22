package com.scorpio4.vendor.sesame.util;

import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.sparql.SelectSPARQL;
import com.scorpio4.iq.bean.ConvertsType;
import com.scorpio4.iq.bean.XSD2POJOConverter;
import com.scorpio4.vocab.COMMONS;
import org.openrdf.model.Namespace;
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
public class SesameHelper {
    private static final Logger log = LoggerFactory.getLogger(SesameHelper.class);

	public static void defaultNamespaces(RepositoryConnection to) throws RepositoryException {
		to.begin();
		to.setNamespace("rdf", COMMONS.RDF);
		to.setNamespace("rdfs", COMMONS.RDFS);
		to.setNamespace("owl", COMMONS.OWL);
		to.setNamespace("skos", COMMONS.SKOS);
		to.setNamespace("dc", COMMONS.DC);
		to.setNamespace("xsd", COMMONS.XSD);
		to.setNamespace("acl", COMMONS.ACL);
		to.commit();
	}

	public static StringBuilder toSPARQLPrefix(RepositoryConnection connection) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
		RepositoryResult<Namespace> namespaces = connection.getNamespaces();
		StringBuilder namespace$ = new StringBuilder();
		while(namespaces.hasNext()) {
			Namespace namespace = namespaces.next();
			namespace$.append("PREFIX ").append(namespace.getPrefix()).append(": <").append(namespace.getName()).append( ">\n");
		}
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

        TupleQueryResult result = tuple.evaluate();
        Collection reply = new ArrayList();
        while(result.hasNext()) {
            BindingSet bindingSet = result.next();
	        log.trace("SPARQL binding: "+bindingSet.getBindingNames());
            Map map = new HashMap();
            for(Binding name:bindingSet) {
                map.put(name.getName(), name.getValue().stringValue() );
            }
            reply.add(map);
        }
        log.trace("SPARQL found: "+reply.size()+" results");
        return reply;
    }

	public static String explodePragmas(RepositoryConnection connection, String sparql) throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {
		return sparql.replace("@namespaces", "\n"+toSPARQLPrefix(connection).toString() );
	}
}
