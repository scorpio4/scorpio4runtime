package com.scorpio4.iq;

import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.spring.RDFBeanDefinitionReader;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 7/07/2014
 * Time  : 8:41 PM
 */
public class SpringyBeansVocabulary implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(this.getClass());

	RDFBeanDefinitionReader rdfBeanReader;


	public SpringyBeansVocabulary() {
	}

	public SpringyBeansVocabulary(ExecutionEnvironment engine) throws Exception {
		boot(engine);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		// Engine's dependencies
		log.debug("Springy Beans Vocabulary");


		Repository repository = engine.getRepositoryManager().getRepository(engine.getIdentity());
		RepositoryConnection connection = repository.getConnection();

		ApplicationContext registry = engine.getRegistry();
		rdfBeanReader = new RDFBeanDefinitionReader(connection, (BeanDefinitionRegistry)registry);

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		Collection<Map> prototypes = crud.read("self/prototypes.sparql", engine.getConfig());
		for(Map bean:prototypes) rdfBeanReader.loadBeanDefinitions((String)bean.get("this"));
		log.debug("Registered "+prototypes.size()+" Prototypes");

		Collection<Map> singletons = crud.read("self/singletons.sparql", engine.getConfig());
		for(Map bean:singletons) rdfBeanReader.loadBeanDefinitions((String)bean.get("this"));
		log.debug("Registered "+singletons.size()+" Singletons");

		connection.close();
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}
}
