package com.scorpio4.iq.vocab;

import com.scorpio4.runtime.ExecutionEnvironment;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.vendor.hydra
 * User  : lee
 * Date  : 9/07/2014
 * Time  : 12:40 AM
 */
public class HydraVocabulary extends AbstractActiveVocabulary {

	public static String BASE = "http://www.w3.org/ns/hydra/core#";

	public HydraVocabulary(ExecutionEnvironment engine) throws Exception {
		super(BASE, engine, false);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		super.boot(engine);
		RepositoryResult<Statement> apiDocs = connection.getStatements(vf.createURI(engine.getIdentity()), vf.createURI(BASE + "apiDocumentation"), null, useInferencing);
		while(apiDocs.hasNext()) {
			bootAPI(connection, apiDocs.next().getObject());
		}
	}

	private void bootAPI(RepositoryConnection connection, Value apiDoc) throws RepositoryException {
		ValueFactory vf = connection.getValueFactory();

		log.debug("Hydra API: "+apiDoc);
		RepositoryResult<Statement> classes = connection.getStatements((Resource)apiDoc, vf.createURI(BASE + "hydra:supportedClass"), null, useInferencing);
		while(classes.hasNext()) {
			bootClass(connection, vf, classes.next().getObject());
		}
	}

	private void bootClass(RepositoryConnection connection, ValueFactory vf, Value supportedClass) throws RepositoryException {
		log.debug("Hydra Class: "+supportedClass);
		RepositoryResult<Statement> templates = connection.getStatements((Resource)supportedClass, vf.createURI(BASE + "hydra:template"), null, useInferencing);
		while(templates.hasNext()) {
			Statement template = templates.next();
			log.debug("Hydra Template: "+template);
		}
	}

	@Override
	public Object activate(String resource, Object body) {
		return body;
	}
}
