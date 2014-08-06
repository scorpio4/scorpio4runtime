package com.scorpio4.runtime;

import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.deploy.Scorpio4SesameDeployer;
import com.scorpio4.oops.FactException;
import com.scorpio4.vendor.sesame.MockRepositoryManager;
import com.scorpio4.vendor.sesame.store.MemoryRDFSRepository;
import com.scorpio4.vendor.sesame.util.SesameHelper;
import com.scorpio4.vocab.COMMONS;
import org.apache.camel.CamelContext;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.springframework.context.ApplicationContext;

import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.runtime
 * @author lee
 * Date  : 7/07/2014
 * Time  : 11:18 PM
 */
public class MockEngine implements ExecutionEnvironment {
	Repository repository;
	RepositoryResolver repositoryResolver;
	String identity = "bean:"+getClass().getCanonicalName();
	ApplicationContext context;
	AssetRegisters assetRegisters;
	Map config = null;
	private RepositoryConnection connection;

	public MockEngine() throws RepositoryException {
		this("urn:scorpio4tests:", defaultConfig());
		start();
	}

	public MockEngine(String identity, Map config) throws RepositoryException {
		init(identity, config, new MemoryRDFSRepository() );
	}

	public MockEngine(String identity, Map config, Repository repository) throws RepositoryException {
		init(identity, config, repository );
	}

	public void init(String identity, Map config, Repository repository) throws RepositoryException {
		this.identity=identity;
		this.config=config;
		this.repository=repository;
		repositoryResolver = new MockRepositoryManager(repository);

		RepositoryConnection connection = repository.getConnection();
		SesameHelper.defaultNamespaces(connection);
		connection.begin();
		// Scorpio4 default namespaces
		connection.setNamespace("core", COMMONS.CORE);
		connection.setNamespace("flo", COMMONS.CORE+"flo/");
		connection.setNamespace("bean", COMMONS.CORE+"bean/");
		connection.setNamespace("asq", COMMONS.CORE+"asq/");
		connection.commit();
		connection.close();
	}

	public void start() {
		try {
			connection = repository.getConnection();
			assetRegisters = new AssetRegisters(connection);

			context = RuntimeHelper.newSpringContext(this);

		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (RepositoryConfigException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			connection.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

	}

	/* Convenience */
	public CamelContext newCamel() {
		return RuntimeHelper.newCamelContext(this);
	}

	private static Map defaultConfig() {
		Map config = new HashMap();
		config.put("id", "urn:scorpio4tests:");
		config.put("httpPort", 9091);
		config.put("java.naming.factory.initial","org.apache.camel.util.jndi.CamelInitialContextFactory");
		return config;
	}

	@Override
	public ApplicationContext getRegistry() {
		return context;
	}

	@Override
	public AssetRegister getAssetRegister() {
		return assetRegisters;
	}

	@Override
	public RepositoryResolver getRepositoryManager() {
		return repositoryResolver;
	}

	@Override
	public Repository getRepository() {
		return repository;
	}

	@Override
	public Map getConfig() {
		return config;
	}

	public Properties getProperties() {
		Properties properties = new Properties();
		properties.putAll(getConfig());
		return properties;
	}

	@Override
	public void reboot() throws Exception {
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	@Override
	public String getIdentity() {
		return identity;
	}

	public void provision(String resource) throws RepositoryException, FactException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		provision(resource, classLoader);
	}

	public void provision(String resource, ClassLoader loader) throws RepositoryException, FactException, IOException {
		provision(getRepository(),resource, loader);
	}

	public static void provision(Repository repository, String resource, ClassLoader loader) throws RepositoryException, FactException, IOException {
		InputStream stream = loader.getResourceAsStream(resource);
		if (stream==null) throw new IOException("Deploy resource not found: "+resource+" in "+loader);
		RepositoryConnection connection = repository.getConnection();
		SesameHelper.defaultNamespaces(connection);

		Scorpio4SesameDeployer deployer = new Scorpio4SesameDeployer("classpath:"+resource, connection);
		deployer.deploy(resource, stream);

		connection.close();
	}

}