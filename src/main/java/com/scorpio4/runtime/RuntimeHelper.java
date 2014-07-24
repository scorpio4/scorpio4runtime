package com.scorpio4.runtime;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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

	public static GenericApplicationContext newSpringContext(ExecutionEnvironment engine) throws RepositoryConfigException, RepositoryException, NamingException {
		Map self = new HashMap();
		self.put("assets", engine.getAssetRegister());
		self.put("config", engine.getConfig());
		self.put("repositoryManager", engine.getRepositoryManager());
		self.put("core", engine.getRepository());
		return newSpringContext(self);
	}

	public static Map getVMStats() {
		Runtime runtime = Runtime.getRuntime();
		Map stats = new HashMap();
		stats.put("maxMemory", runtime.maxMemory());
		stats.put("totalMemory", runtime.totalMemory());
		stats.put("freeMemory", runtime.freeMemory());
		return stats;
	}

}
