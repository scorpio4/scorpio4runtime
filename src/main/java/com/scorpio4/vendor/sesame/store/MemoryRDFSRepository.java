package com.scorpio4.vendor.sesame.store;

import com.scorpio4.deploy.Scorpio4SesameDeployer;
import com.scorpio4.oops.FactException;
import com.scorpio4.vendor.sesame.io.SPARQLRules;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.vendor.sesame.store
 * User  : lee
 * Date  : 3/07/2014
 * Time  : 11:41 AM
 *
 *
 */
public class MemoryRDFSRepository extends SailRepository {
	private static final Logger log = LoggerFactory.getLogger(MemoryRDFSRepository.class);

	public MemoryRDFSRepository() throws RepositoryException {
		super(new ForwardChainingRDFSInferencer( new MemoryStore() ));
		initialize();
		log.debug("MemoryRDFSRepository initialized");
	}

	public MemoryRDFSRepository deploy(String resource) throws RepositoryException, FactException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return deploy(resource,classLoader);
	}

	public MemoryRDFSRepository deploy(String resource, ClassLoader loader) throws RepositoryException, FactException, IOException {
		InputStream stream = loader.getResourceAsStream(resource);
		if (stream==null) throw new IOException("Deploy resource not found: "+resource+" in "+loader);
		SailRepositoryConnection connection = getConnection();
		SPARQLRules.defaultNamespaces(connection);

		Scorpio4SesameDeployer deployer = new Scorpio4SesameDeployer("classpath:"+resource, connection);
		deployer.deploy(resource, stream);

		connection.close();
		return this;
	}

}
