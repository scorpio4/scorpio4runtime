package com.scorpio4.iq.vocab;

import com.scorpio4.fact.FactSpace;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.camel.component.any23.Any23Component;
import com.scorpio4.vendor.camel.component.sesame.SesameComponent;
import com.scorpio4.vendor.camel.crud.CRUDComponent;
import com.scorpio4.vendor.camel.flo.RDFCamelPlanner;
import com.scorpio4.vendor.camel.self.SelfComponent;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vocab.COMMONS;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultFactoryFinderResolver;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.spi.FactoryFinder;
import org.apache.camel.spi.FactoryFinderResolver;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * @author lee
 * Date  : 7/07/2014
 * Time  : 8:37 PM
 */
public class ActiveFLOVocabulary extends AbstractActiveVocabulary{
	public final static String DO_BOOTSTRAP = "direct:self:active";

	protected RDFCamelPlanner floSupport;
	protected CamelContext camel = null;
	protected boolean tracing = true;

	public ActiveFLOVocabulary(ExecutionEnvironment engine) throws Exception {
		super(COMMONS.ACTIVE_FLO, engine, true);
		boot(engine);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		super.boot(engine);
		bootCamel(engine);
//		activate(DO_BOOTSTRAP, engine.getConfig());
	}

	protected void bootCamel(ExecutionEnvironment engine) throws Exception {
		createCamel(engine);

		FactSpace factSpace = new FactSpace(engine.getIdentity(), engine.getRepository());
		SesameCRUD crud = new SesameCRUD(factSpace);

		// Custom Components
		// TODO: Find a better way to register them
		camel.addComponent("urn", new DirectComponent()); // synonym - maybe 'vm' instead?

		camel.addComponent("crud", new CRUDComponent(crud));
		camel.addComponent("self", new SelfComponent(engine));
		camel.addComponent("flo", camel.getComponent("self"));

		camel.addComponent("any23", new Any23Component(engine));

		camel.addComponent("sparql", new SesameComponent(engine));
//		camel.addComponent("curate", new CurateComponent(engine));
		camel.addComponent("properties", new PropertiesComponent());

//		JettyHttpComponent jetty = (JettyHttpComponent) camel.getComponent("jetty");
//		jetty.set

		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) getEngine().getRegistry();
		List<String> componentNames = camel.getComponentNames();
		for(String name: componentNames) {
			// consider components part of the FLO namespace
			// helpful, but is it wise?
			registry.registerAlias(COMMONS.CORE+"flo/"+name, name);
		}

		floSupport = new RDFCamelPlanner(camel, engine);
		floSupport.setVocabURI(getIdentity());
		factSpace.close();
		crud.close();

		log.debug("Active FLO Booted: "+engine.getIdentity());
	}

	private void createCamel(ExecutionEnvironment engine) {
		// Camel
		ApplicationContext registry = engine.getRegistry();
		this.camel = new DefaultCamelContext(new ApplicationContextRegistry(registry));
		camel.setProperties(engine.getConfig());
		camel.setTracing(tracing);
		camel.setTypeConverterStatisticsEnabled(true);
//		camel.setApplicationContextClassLoader(engine.getClassLoader());
		final DefaultFactoryFinderResolver defaultFactoryFinderResolver = new DefaultFactoryFinderResolver();
// EXPERIMENT - what does this do? Will it solve the JAXRS service resolution problem?
		camel.setFactoryFinderResolver(new FactoryFinderResolver() {

			/**
			 * Creates a new default factory finder using a default resource path.
			 *
			 * @param classResolver the class resolver to use
			 * @return a factory finder.
			 */
			@Override
			public FactoryFinder resolveDefaultFactoryFinder(ClassResolver classResolver) {
				log.debug("resolveDefaultFactoryFinder: "+classResolver);
				return defaultFactoryFinderResolver.resolveDefaultFactoryFinder(classResolver);
			}

			/**
			 * Creates a new factory finder.
			 *
			 * @param classResolver the class resolver to use
			 * @param resourcePath  the resource path as base to lookup files within
			 * @return a factory finder.
			 */
			@Override
			public FactoryFinder resolveFactoryFinder(ClassResolver classResolver, String resourcePath) {
				log.debug("resolveDefaultFactoryFinder: "+resourcePath+" @ "+classResolver);
				return defaultFactoryFinderResolver.resolveFactoryFinder(classResolver, resourcePath);
			}
		});
		log.debug("Camel Initialized: "+toString());
	}

	public void start() throws Exception {
		super.start();
		if (camel==null) throw new IQException("Not booted: "+toString());
		camel.start();
		floSupport.plan();

		log.debug("Started Active FLOs:");
		List<Route> routes = camel.getRoutes();
		for(Route route: routes) {
			log.debug("\t"+route.getEndpoint().getEndpointUri());
		}
	}

	protected void addRoutes(RouteBuilder routeBuilder) throws Exception {
		camel.addRoutes(routeBuilder);
		log.trace("Route Added: " + routeBuilder);
	}

	public void stop() throws Exception {
		camel.stop();
		super.stop();
	}

	public CamelContext getCamelContext() {
		return camel;
	}

	public Object activate(String floURI, Object body, Class type) {
		return floSupport.trigger(floURI, body, engine.getConfig(), type );
	}

	public Object activate(String floURI, Object body) {
		return activate(floURI, body, Object.class);
	}

	public boolean isTracing() {
		return tracing;
	}

	public void setTracing(boolean tracing) {
		this.tracing = tracing;
	}

}
