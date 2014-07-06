package com.scorpio4.iq;

import com.scorpio4.ExecutionEnvironment;
import com.scorpio4.vendor.camel.CurateComponent;
import com.scorpio4.vendor.camel.component.Any23Component;
import com.scorpio4.vendor.camel.component.CRUDComponent;
import com.scorpio4.vendor.camel.component.SelfComponent;
import com.scorpio4.vendor.camel.component.SesameComponent;
import com.scorpio4.vendor.camel.flo.SesameFLO;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vendor.spring.CachedBeanFactory;
import com.scorpio4.vendor.spring.RDFBeanDefinitionReader;
import com.scorpio4.vocab.COMMON;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

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
	private SesameFLO floSupport;
	CachedBeanFactory cachedBeanFactory;
	RDFBeanDefinitionReader springyBeans;
	GenericApplicationContext registry;
	CamelContext camel = null;

	public ActiveVocabularies(ExecutionEnvironment engine) throws Exception {
		bindEngine(engine);
		boot(engine);
	}

	public void boot(ExecutionEnvironment engine) throws Exception {
		RepositoryConnection connection = engine.getFactSpace().getConnection();
		springyBeans = new RDFBeanDefinitionReader(connection, engine.getRegistry());
		cachedBeanFactory = new CachedBeanFactory();

		log.debug("Activating Vocabularies");
		bootSpringyBeans(engine);
		bootCamelFLO(engine);
		bootSelfFLO(engine);
		trigger(DO_BOOTSTRAP);
	}

	protected void bindEngine(ExecutionEnvironment engine) throws Exception {
		cachedBeanFactory.cache("camel",   camel);
		// Engine's dependencies
		cachedBeanFactory.cache("engine",  engine);
		cachedBeanFactory.cache("facts",   engine.getFactSpace());
		cachedBeanFactory.cache("assets",  engine.getAssetRegister());
		cachedBeanFactory.cache("config",  engine.getConfig());
		cachedBeanFactory.cache("registry",engine.getRegistry());
		cachedBeanFactory.cache("sesame",  engine.getRepositoryManager());

		Repository repository = engine.getRepositoryManager().getRepository(engine.getIdentity());
		cachedBeanFactory.cache("core",    repository);
	}

	protected void bootSpringyBeans(ExecutionEnvironment engine) throws Exception {
		log.debug("\tSpringy Beans");

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		Collection<Map> prototypes = crud.read("self/prototypes.sparql", engine.getConfig());
		for(Map bean:prototypes) springyBeans.loadBeanDefinitions((String)bean.get("this"));

		Collection<Map> singletons = crud.read("self/singletons.sparql", engine.getConfig());
		for(Map bean:singletons) springyBeans.loadBeanDefinitions((String)bean.get("this"));
	}

	protected void bootCamelFLO(ExecutionEnvironment engine) throws Exception {
		log.debug("\tCamel FLO");
		// Get a Spring Bean Factory
		this.registry = new GenericApplicationContext(cachedBeanFactory);
		registry.setId(engine.getIdentity());
		registry.setDisplayName(engine.getIdentity());

		// Camel
		this.camel = new DefaultCamelContext(new ApplicationContextRegistry(registry));
		camel.setProperties(engine.getConfig());

		// custom Scorpio4 components
		// TODO: Find a better way to register them
		camel.addComponent("self", new SelfComponent(engine));
		camel.addComponent("any23", new Any23Component());
		camel.addComponent("sparql", new SesameComponent(engine.getRepositoryManager()));
		camel.addComponent("curate", new CurateComponent(engine));

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		camel.addComponent("crud", new CRUDComponent(crud));

		floSupport = new SesameFLO(camel, engine.getFactSpace());
		floSupport.setBaseURI(COMMON.CAMEL_FLO);
		floSupport.plan();

//		floSupport.plan(engine.getFactSpace(),engine.getIdentity());
	}

	private void bootSelfFLO(final ExecutionEnvironment engine) throws Exception {
		RouteBuilder routeBuilder = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
//				from("direct:self:noop").process(new Processor() {
//					@Override
//					public void process(Exchange exchange) throws Exception {
//					}
//				});
			}
		};
		log.debug("Booted Self: "+routeBuilder);
		camel.addRoutes(routeBuilder);
	}

	public void trigger(ExecutionEnvironment engine, String triggerURI) {
		floSupport.trigger(triggerURI, engine.getConfig());
	}

	public void trigger(String triggerURI) {
		try {
			floSupport.trigger(triggerURI, cachedBeanFactory.getBeanConfig());
		} catch (Exception e) {
			log.warn("Faulty Trigger: "+triggerURI+" ->"+e.getCause().getMessage());
		}
	}

	public void start() throws Exception {
		camel.start();
	}

	public void addRoutes(RouteBuilder routeBuilder) throws Exception {
		camel.addRoutes(routeBuilder);
	}

	public void stop() throws Exception {
		camel.stop();
	}

	public CamelContext getCamelContext() {
		return camel;
	}

	public GenericApplicationContext getRegistry() {
		return registry;
	}

}
