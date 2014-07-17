package com.scorpio4.iq.vocab;

import com.scorpio4.fact.FactSpace;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.spring.RDFBeanDefinitionReader;
import com.scorpio4.vocab.COMMON;
import org.openrdf.repository.RepositoryConnection;
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
public class ActiveBeansVocabulary extends AbstractActiveVocabulary{
	RDFBeanDefinitionReader rdfBeanReader;

	public ActiveBeansVocabulary(ExecutionEnvironment engine) throws Exception {
		super(COMMON.CORE+"bean/",engine, true);
		start();
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		super.boot(engine);
		// Engine's dependencies
		log.debug("Active Beans Vocabulary: "+getIdentity());

		ApplicationContext registry = engine.getRegistry();

		FactSpace factSpace = new FactSpace(engine.getIdentity(), engine.getRepository());
		rdfBeanReader = new RDFBeanDefinitionReader(connection, (BeanDefinitionRegistry)registry);

		SesameCRUD crud = new SesameCRUD(factSpace);

		Collection<Map> prototypes = crud.read("self/prototypes.sparql", engine.getConfig());
		register(prototypes);
		log.debug("Registered "+prototypes.size()+" Prototypes");

		Collection<Map> singletons = crud.read("self/singletons.sparql", engine.getConfig());
		register(singletons);
		log.debug("Registered "+singletons.size()+" Singletons");

	}

	public int register(Collection<Map> beans) {
		int c = 0;
		for(Map bean:beans) {
			String beanURI = (String)bean.get("this");
			try {
				c+=rdfBeanReader.loadBeanDefinitions(beanURI);
				log.debug("Registered Bean: "+beanURI);
			} catch(Exception e) {
				log.error("Bean ERROR: "+beanURI+" -> "+e.getMessage());
			}
		}
		return c;
	}

	@Override
	public void start() throws Exception {
		// NO-OP
	}

	@Override
	public void stop() throws Exception {
		// NO-OP
	}

	@Override
	public Object activate(String resource, Object body) {
		log.warn("Bean Trigger not supported: "+resource);
		return rdfBeanReader.getBean(resource);
	}
}
