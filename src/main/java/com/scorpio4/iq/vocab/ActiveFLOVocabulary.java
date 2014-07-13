package com.scorpio4.iq.vocab;

import com.scorpio4.fact.FactSpace;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.camel.CRUDComponent;
import com.scorpio4.vendor.camel.SelfComponent;
import com.scorpio4.vendor.camel.component.Any23Component;
import com.scorpio4.vendor.camel.component.SesameComponent;
import com.scorpio4.vendor.camel.flo.RDFCamelPlanner;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import com.scorpio4.vocab.COMMON;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.iq
 * User  : lee
 * Date  : 7/07/2014
 * Time  : 8:37 PM
 */
public class ActiveFLOVocabulary implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	public final static String DO_BOOTSTRAP = "direct:self:active";

	private RDFCamelPlanner floSupport;
	CamelContext camel = null;

	private boolean tracing = true;

	public ActiveFLOVocabulary() {
	}

	public ActiveFLOVocabulary(ExecutionEnvironment engine) throws Exception {
		boot(engine);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		bootCamel(engine);
		bootSelf(engine);
		activate(DO_BOOTSTRAP, engine.getConfig());

	}

	protected void bootCamel(ExecutionEnvironment engine) throws Exception {
		ApplicationContext registry = engine.getRegistry();

		// Camel
		this.camel = new DefaultCamelContext(new ApplicationContextRegistry(registry));
		camel.setProperties(engine.getConfig());
		camel.setTracing(tracing);

		// custom Scorpio4 components
		// TODO: Find a better way to register them
		camel.addComponent("self", new SelfComponent(engine));
		camel.addComponent("any23", new Any23Component());
		camel.addComponent("sparql", new SesameComponent(engine.getRepositoryManager()));
//		camel.addComponent("curate", new CurateComponent(engine));

		FactSpace factSpace = new FactSpace(engine.getIdentity(), engine.getRepository());
		SesameCRUD crud = new SesameCRUD(factSpace);
		camel.addComponent("crud", new CRUDComponent(crud));

		floSupport = new RDFCamelPlanner(camel, factSpace );

		floSupport.setVocabURI(COMMON.ACTIVE_FLO);
		floSupport.plan();
		factSpace.close();

		log.debug("Active FLO Booted: "+engine.getIdentity());
	}

	private void bootSelf(final ExecutionEnvironment engine) throws Exception {
//		Deprecated
//		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
//
//		Collection<Map> routes = crud.read("self/routes", engine.getConfig());
//		for(Map route:routes) {
//			floSupport.plan( (String)route.get("from"), (String)route.get("to"));
//		}
//		log.debug("Deployed "+routes.size()+" primary routes");


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
		camel.addRoutes(routeBuilder);

		routeBuilder = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:self:reboot").process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						engine.reboot();
					}
				});
//				from("direct:self:stop").process(new Processor() {
//					@Override
//					public void process(Exchange exchange) throws Exception {
//						engine.stop();
//					}
//				});
			}
		};
		camel.addRoutes(routeBuilder);
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

	public Object activate(String doBootstrap, Object body) {
		return floSupport.trigger(doBootstrap, body);
	}

	public boolean isTracing() {
		return tracing;
	}

	public void setTracing(boolean tracing) {
		this.tracing = tracing;
	}

}
