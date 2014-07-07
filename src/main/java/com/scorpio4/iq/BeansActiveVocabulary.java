package com.scorpio4.iq;

import com.scorpio4.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.spring.RDFBeanDefinitionReader;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 7/07/2014
 * Time  : 8:41 PM
 */
public class BeansActiveVocabulary implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(this.getClass());

	RDFBeanDefinitionReader springyBeans;


	public BeansActiveVocabulary() {
	}

	public BeansActiveVocabulary(ExecutionEnvironment engine) throws Exception {
		boot(engine);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		// Engine's dependencies
		bootCamelFLO(engine);
	}

	protected void bootCamelFLO(ExecutionEnvironment engine) throws Exception {
		log.debug("\tSpringy Beans");


		ApplicationContext beanFactory = engine.getRegistry();
		Repository repository = engine.getRepositoryManager().getRepository(engine.getIdentity());
		RepositoryConnection connection = repository.getConnection();

		GenericApplicationContext registry = new GenericApplicationContext(beanFactory);
//		beanFactory.cache("registry",registry);

		springyBeans = new RDFBeanDefinitionReader(connection, registry);

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		Collection<Map> prototypes = crud.read("self/prototypes.sparql", engine.getConfig());
		for(Map bean:prototypes) springyBeans.loadBeanDefinitions((String)bean.get("this"));

		Collection<Map> singletons = crud.read("self/singletons.sparql", engine.getConfig());
		for(Map bean:singletons) springyBeans.loadBeanDefinitions((String)bean.get("this"));
		connection.close();
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}
}
