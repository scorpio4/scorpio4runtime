package com.scorpio4.iq.vocab;

import com.scorpio4.asq.parser.Sesame2ASQ;
import com.scorpio4.asq.sparql.ConstructSPARQL;
import com.scorpio4.asq.sparql.SelectSPARQL;
import com.scorpio4.assets.SesameAssetRegister;
import com.scorpio4.oops.ASQException;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.sesame.io.SPARQLRules;
import com.scorpio4.vendor.sesame.util.RDFScalars;
import com.scorpio4.vocab.COMMON;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq.vocab
 * User  : lee
 * Date  : 16/07/2014
 * Time  : 10:56 PM
 *
 * ASQ generates SPARQL queries. It supports two modes - SELECT and INFER
 * A simple ASQ return the ASQ as a SPARQL SELECT or CONSTRUCT
 * For an ASQ Learn, it will infer the triples, then return a SELECT point to inferred.
 * When started, it converts all ASQ queries into Assets,
 * it should started before Bean & FLO
 *
 */
public class ASQVocabulary extends AbstractActiveVocabulary {

	public ASQVocabulary(ExecutionEnvironment engine) throws Exception {
		super(COMMON.CORE + "asq/", engine, false);
	}

	public void start() throws Exception {
		log.debug("ASQ Connected: " + connection);
		isActive=true;
		reboot();
	}

	public void stop() {
		this.isActive=false;
		try {
			this.connection.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void reboot() throws Exception {
		SesameCRUD crud = new SesameCRUD(connection, getIdentity(), engine.getAssetRegister() );

		Map<String,Boolean> seen = new HashMap();
		connection.begin();
		Collection<Map> selects = crud.read("self/asq/selects", engine.getConfig());
		updateAssets(seen, selects);

		Collection<Map> infers = crud.read("self/asq/infers", engine.getConfig());
		updateAssets(seen, infers);

		Collection<Map> intuitions = crud.read("self/asq/intuitions", engine.getConfig());
		updateAssets(seen, intuitions);
		connection.commit();
	}

	private void updateAssets(Map<String, Boolean> seen, Collection<Map> selects) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ASQException {
		for(Map select: selects) {
			String aThis = (String) select.get("this");
			if (!seen.containsKey(aThis)) {
				SelectSPARQL sparql = toSPARQL(aThis, select);
				if (sparql!=null) {
					updateAsset(aThis, sparql);
					seen.put(aThis,true);
				}
			}
		}
	}

	private void updateAsset(String asqURI, SelectSPARQL sparql) throws RepositoryException {
		if (!asqURI.contains(":")) return;
		Resource queryURI = vf.createURI(asqURI);
		URI hasAsset = vf.createURI(SesameAssetRegister.HAS_ASSET);
		connection.remove(queryURI, hasAsset, null);
		Literal asset = vf.createLiteral(sparql.toString(), vf.createURI(COMMON.MIME_SPARQL));
		log.debug("Update Asset: "+queryURI+" -> "+sparql);
		connection.add(queryURI, hasAsset, asset);
	}

	@Override
	public Object activate(String asqURI, Object body) throws IQException {
		try {
			SelectSPARQL sparql = toSPARQL(asqURI, null);
			if (sparql==null) throw new ASQException("ASQ not found: "+asqURI);
			else if (sparql instanceof ConstructSPARQL) {
				return new SelectSPARQL( ((ConstructSPARQL)sparql).getConstruct() );
			} else return sparql;
		} catch (RepositoryException e) {
			throw new IQException("Repository Error",e);
		} catch (QueryEvaluationException e) {
			throw new IQException("Query Error",e);
		} catch (MalformedQueryException e) {
			throw new IQException("Query Syntax Error",e);
		} catch (ASQException e) {
			throw new IQException("ASQ Error",e);
		}
	}

	protected SelectSPARQL toSPARQL(String asqURI, Map meta) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ASQException {
		if (!asqURI.contains(":")) return null;
		RDFScalars scalars = new RDFScalars(connection);
		Resource queryURI = vf.createURI(asqURI);

		boolean isQuery  = scalars.isTypeOf(queryURI, vf.createURI(getIdentity() + "Query"));
		boolean isInference = scalars.isTypeOf(queryURI, vf.createURI(getIdentity() + "Inference"));
		boolean isIntuition = scalars.isTypeOf(queryURI, vf.createURI(getIdentity() + "Intuition"));

		if (isIntuition) {
			URI learnVerb = vf.createURI(getIdentity() + "learn");
			URI learnURI = scalars.getURI(queryURI, learnVerb);
			log.debug("toCONSTRUCT: "+learnURI);

			ConstructSPARQL sparql = learn(scalars, queryURI, learnURI);
			SPARQLRules sparqlRules = new SPARQLRules(connection, learnURI.stringValue(), true);
			sparqlRules.apply(sparql.toString());
			return sparql;
		}

		if (isQuery || isInference) {
			try {
				log.debug("toSELECT: "+queryURI);
				Sesame2ASQ selectASQ = new Sesame2ASQ(connection, asqURI);
				return new SelectSPARQL(selectASQ.getASQ());
			} catch (RepositoryException e) {
				e.printStackTrace();
			} catch (ASQException e) {
				e.printStackTrace();
			}
		}
		log.debug("UNKNOWN: "+queryURI+" "+isInference);
		return null;
	}

	private ConstructSPARQL learn(RDFScalars scalars, Resource queryURI, URI learnURI) throws RepositoryException, ASQException, QueryEvaluationException, MalformedQueryException {
		URI whenVerb = vf.createURI(getIdentity() + "when");
		URI whenURI = scalars.getURI(queryURI, whenVerb);
		if (whenURI!=null) {
			log.debug("Learn: "+learnURI+" -> "+whenURI);
			Sesame2ASQ whenASQ = new Sesame2ASQ(connection,whenURI.stringValue());
			Sesame2ASQ learnASQ = new Sesame2ASQ(connection,learnURI.stringValue());
			return new ConstructSPARQL(learnASQ.getASQ(), whenASQ.getASQ());
		}
		throw new ASQException("Unbound ASQ: No 'when' for 'learn' in: "+learnURI);
	}
}
