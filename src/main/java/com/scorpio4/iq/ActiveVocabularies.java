package com.scorpio4.iq;

import com.scorpio4.runtime.Engine;
import com.scorpio4.vendor.camel.component.Any23Component;
import com.scorpio4.vendor.camel.component.CRUDComponent;
import com.scorpio4.vendor.camel.component.CoreComponent;
import com.scorpio4.vendor.camel.component.SesameComponent;
import com.scorpio4.vendor.camel.planner.CamelFLO;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.spring.RDFBeanDefinitionReader;
import com.scorpio4.vocab.COMMON;
import org.apache.camel.CamelContext;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 3/07/2014
 * Time  : 11:47 PM
 */
public class ActiveVocabularies {
	public final static String DO_BOOTSTRAP = "direct:self:active";

	final Logger log = LoggerFactory.getLogger(this.getClass());
	private CamelFLO floSupport;
	Engine engine;
	RDFBeanDefinitionReader springyBeans;

	public ActiveVocabularies(Engine engine) throws Exception {
		this.engine=engine;
		boot();
	}

	public void boot() throws Exception {
		RepositoryConnection connection = engine.getFactSpace().getConnection();
		springyBeans = new RDFBeanDefinitionReader(connection, engine.getRegistry());

		log.debug("Activating Vocabularies");
		bootSpringyBeans();
		bootCamelFLO();
		trigger(DO_BOOTSTRAP);
	}

	protected void bootSpringyBeans() throws Exception {
		log.debug("\tSpringy Beans");

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		Collection<Map> prototypes = crud.read("self/prototypes.sparql", engine.getConfig());
		for(Map bean:prototypes) springyBeans.loadBeanDefinitions((String)bean.get("this"));

		Collection<Map> singletons = crud.read("self/singletons.sparql", engine.getConfig());
		for(Map bean:singletons) springyBeans.loadBeanDefinitions((String)bean.get("this"));
	}

	protected void bootCamelFLO() throws Exception {
		log.debug("\tCamel FLO");
		CamelContext camel = engine.getCamelContext();

		camel.addComponent("core", new CoreComponent(engine.getFactSpace(), engine.getAssetRegister()));
		camel.addComponent("any23", new Any23Component());
		camel.addComponent("sparql", new SesameComponent(engine.getRepositoryManager()));

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		camel.addComponent("crud", new CRUDComponent(crud));

		floSupport = new CamelFLO(camel, engine.getFactSpace());
		floSupport.setBaseURI(COMMON.CAMEL_FLO);
		floSupport.plan();

//		floSupport.plan(engine.getFactSpace(),engine.getIdentity());
	}

	public void trigger(String triggerURI) {
		try {
			floSupport.trigger(triggerURI, null, engine.getConfig());
		} catch (Exception e) {
			log.warn("Faulty Trigger: "+triggerURI+" ->"+e.getCause().getMessage());
		}
	}

}
