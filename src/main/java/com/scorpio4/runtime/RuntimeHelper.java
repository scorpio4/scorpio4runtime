package com.scorpio4.runtime;

import com.scorpio4.deploy.Scorpio4SesameDeployer;
import com.scorpio4.oops.FactException;
import com.scorpio4.vendor.sesame.util.SesameHelper;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.runtime
 * User  : lee
 * Date  : 23/07/2014
 * Time  : 12:59 AM
 *
 *
 */
public class RuntimeHelper {

	public static CamelContext newCamelContext(ExecutionEnvironment executionEnvironment) {
		return new DefaultCamelContext(new ApplicationContextRegistry(executionEnvironment.getRegistry()));
	}

	public static GenericApplicationContext newSpringContext(ExecutionEnvironment engine) throws RepositoryConfigException, RepositoryException, NamingException {
		Map self = new HashMap();
		self.put("assets", engine.getAssetRegister());
		self.put("config", engine.getConfig());
		self.put("repositoryManager", engine.getRepositoryManager());
		self.put("core", engine.getRepository());
		return newSpringContext(self);
	}

	public static GenericApplicationContext newSpringContext(Map<String, Object> map) throws NamingException {
		if (map==null) throw new NamingException("NULL Spring configuration");
//		SimpleNamingContextBuilder jndi = new SimpleNamingContextBuilder();
//		for(String k: map.keySet()) jndi.bind(k, map.get(k));
//		jndi.activate();

		InitialContext jndi = new InitialContext();
		for(String k: map.keySet()) jndi.bind(k, map.get(k));

		GenericApplicationContext applicationContext = new GenericApplicationContext();
		RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
		applicationContext.registerBeanDefinition("bean:"+CommonAnnotationBeanPostProcessor.class.getCanonicalName(), rootBeanDefinition);
		rootBeanDefinition.getPropertyValues().add("alwaysUseJndiLookup", true);
		return applicationContext;
	}

	public static Map getVMStats() {
		Runtime runtime = Runtime.getRuntime();
		Map stats = new HashMap();
		stats.put("maxMemory", runtime.maxMemory());
		stats.put("totalMemory", runtime.totalMemory());
		stats.put("freeMemory", runtime.freeMemory());
		return stats;
	}

	public static void provision(Repository repository,  String resource, InputStream stream) throws IOException, FactException, RepositoryException {
		RepositoryConnection connection = repository.getConnection();
		SesameHelper.defaultNamespaces(connection, resource);
		Scorpio4SesameDeployer deployer = new Scorpio4SesameDeployer(resource, connection);
		deployer.deploy(resource, stream);
		connection.close();
	}

	public static void provision(ExecutionEnvironment engine,  URL resource) throws IOException, FactException, RepositoryException {
		InputStream stream = resource.openStream();
		if (stream==null) throw new IOException("Deploy resource not found: "+resource.toExternalForm());
		provision(engine.getRepository(), resource.toExternalForm(), stream);
	}

	public static void provision(ExecutionEnvironment engine, File srcDir) throws FactException, RepositoryException, IOException {
		if (!srcDir.exists()) return;
		RepositoryConnection connection = engine.getRepository().getConnection();
		SesameHelper.defaultNamespaces(connection, engine.getIdentity());
		Scorpio4SesameDeployer sesameDeployer = new Scorpio4SesameDeployer(engine.getIdentity(), connection);
		sesameDeployer.deploy(srcDir);
		connection.close();
	}
}
