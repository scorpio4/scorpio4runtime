package com.scorpio4.fact.onto;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.fact.onto
 * @author lee
 * Date  : 26/06/2014
 * Time  : 9:28 AM
 */
public class SesameVocabulary implements Vocabulary {
	RepositoryConnection connection;

	public SesameVocabulary(RepositoryConnection connection) {
		this.connection=connection;
	}

	public String getObject(String subject, String predicate) {
		Collection<String> objects;
		try {
			objects = getObjects(subject, predicate);
			return objects.isEmpty()?null:objects.iterator().next();
		} catch (RepositoryException e) {
			return null;
		}
	}

	public Collection<String> getObjects(String subject, String predicate) throws RepositoryException {
		ValueFactory vf = connection.getValueFactory();
		RepositoryResult<Statement> statements = connection.getStatements(vf.createURI(subject), vf.createURI(predicate), null, false);
		Collection<String> objs = new ArrayList();
		while(statements.hasNext()) {
			Statement statement = statements.next();
			objs.add(statement.getObject().stringValue());
		}
		return objs;
	}

	@Override
	public boolean isKnown(String uri) {
		ValueFactory vf = connection.getValueFactory();
		try {
			return connection.hasStatement(vf.createURI(uri), RDFS.ISDEFINEDBY, null, false);
		} catch (RepositoryException e) {
			return false;
		}
	}

	@Override
	public String getLabel(String uri) {
		return getObject(uri, RDFS.LABEL.stringValue());
	}

	@Override
	public String getComment(String uri) {
		return getObject(uri, RDFS.COMMENT.stringValue());
	}

	@Override
	public String getRange(String uri) {
		return getObject(uri, RDFS.RANGE.stringValue());
	}

	@Override
	public String getDomain(String uri) {
		return getObject(uri, RDFS.DOMAIN.stringValue());
	}

	@Override
	public String getType(String uri) {
		return getObject(uri, RDF.TYPE.stringValue());
	}
}
