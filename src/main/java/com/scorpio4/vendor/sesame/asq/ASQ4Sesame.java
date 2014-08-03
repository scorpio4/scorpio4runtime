package com.scorpio4.vendor.sesame.asq;

import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.ASQParser;
import com.scorpio4.asq.core.BasicASQ;
import com.scorpio4.asq.core.Pattern;
import com.scorpio4.oops.ASQException;
import com.scorpio4.vocab.COMMONS;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.asq.parser
 * @author lee
 * Date  : 16/07/2014
 * Time  : 1:32 PM
 *
 * Build an ASQ query object by following terms of the ASQ Vocabulary
 *
 *
 */
public class ASQ4Sesame implements ASQParser {
	static protected final Logger log = LoggerFactory.getLogger(ASQ4Sesame.class);

	private ASQ asq = null;
	private RepositoryConnection connection;
	private ValueFactory vf;
	String vocabURI = COMMONS.CORE+"asq/";
	Map patternVerbs = new HashMap();

	public ASQ4Sesame(RepositoryConnection connection, String asqURI) throws RepositoryException, ASQException {
		build(connection, asqURI);
	}

	private void build(RepositoryConnection connection, String asqURI) throws RepositoryException, ASQException {
		this.connection=connection;
		this.vf = connection.getValueFactory();
		this.asq = new BasicASQ(asqURI);
		this.patternVerbs.put("where", Pattern.class);
		this.patternVerbs.put("optional", Pattern.class);
		this.patternVerbs.put("and", Pattern.class);
		buildWhere(asq);
	}

	private void buildWhere(ASQ asq) throws RepositoryException, ASQException {
		Resource asqURI = vf.createURI(asq.getIdentity());
		RepositoryResult<Statement> where = connection.getStatements(asqURI, null, null, false);
		while(where.hasNext()) {
			Statement next = where.next();
			String verb = getLocalName(next.getPredicate());
			Pattern pattern = buildWhere(asq, verb, next.getObject());
			if (pattern!=null) asq.where(pattern);
		}

	}

	private Pattern buildWhere(ASQ asq, String verb, Value object) throws ASQException, RepositoryException {
		if (verb!=null && (patternVerbs.containsKey(verb))) {
			log.debug("verb: " + verb);
			if (object instanceof Resource) {
				Pattern pattern = builtPattern(asq, (Resource) object);
				if (verb.equals("optional")) {
					pattern.setOptional(true);
				}
				return pattern;
			}
		}
		return null;
	}

	private Pattern builtPattern(ASQ asq, Resource whereURI) throws RepositoryException, ASQException {
		log.trace("where?: "+whereURI);
		RepositoryResult<Statement> terms = connection.getStatements(whereURI, null, null, false);
		Pattern pattern = null;
		while(terms.hasNext()) {
			Statement next = terms.next();
			URI predicate = next.getPredicate();
			String name = getLocalName(predicate);
			if (name!=null) {
				pattern = buildPattern(pattern, name, next.getObject());
			}
		}
		return pattern;
	}

	private Pattern buildPattern(Pattern pattern, String name, Value object) throws ASQException, RepositoryException {
		if (pattern==null) pattern = new Pattern();
		switch(name) {
			case "this":
				pattern.setThis( object.stringValue());
				break;
			case "has":
				pattern.setHas(object.stringValue());
				break;
			case "path":
				pattern.setPath(object.stringValue());
				break;
			case "that":
				if (object instanceof Literal) {
					pattern.setThat(object.stringValue(), ((Literal) object).getDatatype().stringValue());
				} else {
					pattern.setThat( object.stringValue());
				}
				break;
			default:
				Pattern nested = buildWhere(asq, name, object);
				if (nested!=null) pattern.addNested(nested);
				break;
		}
		log.trace("pattern: "+pattern);
		return pattern;
	}

	@Override
	public ASQ getASQ() {
		return asq;
	}

	private URI getVocabURI(String term) {
		return vf.createURI(vocabURI+term);
	}

	private String getLocalName(Value term) {
		String value = term.stringValue();
		if (!value.startsWith(vocabURI)) return null;
		return value.substring(vocabURI.length());
	}
}
