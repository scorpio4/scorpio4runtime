package com.scorpio4.iq.vocab;

import com.scorpio4.fact.FactSpace;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.spring.RDFBeanDefinitionReader;
import com.scorpio4.vocab.COMMONS;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * @author lee
 * Date  : 7/07/2014
 * Time  : 8:41 PM
 */
public class ActiveBeansVocabulary extends AbstractActiveVocabulary{
	RDFBeanDefinitionReader rdfBeanReader;

	public ActiveBeansVocabulary(ExecutionEnvironment engine) throws Exception {
		super(COMMONS.CORE+"bean/",engine, true);
		boot(engine);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		super.boot(engine);
		// Engine's dependencies
		log.debug("Active Beans Vocabulary: "+getIdentity());

	}

	@Override
	public void start() throws Exception {
		super.start();
		isActive=false;
		try {
			_start();
			isActive=true;
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		} catch (ConfigException e) {
			log.error(e.getMessage(), e);
		} catch (FactException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void _start() throws RepositoryException, FactException, IOException, ConfigException {
		FactSpace factSpace = new FactSpace(engine.getIdentity(), engine.getRepository());

		rdfBeanReader = new RDFBeanDefinitionReader(connection, (BeanDefinitionRegistry) getEngine().getRegistry());

		SesameCRUD crud = new SesameCRUD(factSpace);

		// load bean:Bean instances, should be bean: prefixed fully qualified classes
		Collection<Map> prototypes = crud.read("self/prototypes.sparql", engine.getConfig());
		register(prototypes);
		log.debug("Registered "+prototypes.size()+" Prototypes");

		// load instances of prototypes, ideally urn: name-spaced
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
	public Object activate(String resource, Object body) {
		log.warn("Bean Trigger not supported: "+resource);
		return rdfBeanReader.getBean(resource);
	}
}
