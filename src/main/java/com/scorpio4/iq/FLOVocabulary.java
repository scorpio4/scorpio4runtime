package com.scorpio4.iq;

import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.camel.SelfComponent;
import com.scorpio4.vendor.camel.component.Any23Component;
import com.scorpio4.vendor.camel.component.CRUDComponent;
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
public class FLOVocabulary implements ActiveVocabulary {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	public final static String DO_BOOTSTRAP = "direct:self:active";

	private RDFCamelPlanner floSupport;
	CamelContext camel = null;

	public FLOVocabulary() {
	}

	public FLOVocabulary(ExecutionEnvironment engine) throws Exception {
		boot(engine);
	}

	@Override
	public void boot(ExecutionEnvironment engine) throws Exception {
		bootCamel(engine);
		bootSelf(engine);
		trigger(DO_BOOTSTRAP, engine.getConfig());

	}

	public void trigger(String doBootstrap, Object body) {
		floSupport.trigger(doBootstrap, body);
	}

	protected void bootCamel(ExecutionEnvironment engine) throws Exception {
		log.debug("FLO Active Vocabulary: "+engine.getIdentity());
		ApplicationContext registry = engine.getRegistry();

		// Camel
		this.camel = new DefaultCamelContext(new ApplicationContextRegistry(registry));
		camel.setProperties(engine.getConfig());
//		cachedBeanFactory.cache("camel", camel);

		// custom Scorpio4 components
		// TODO: Find a better way to register them
		camel.addComponent("self", new SelfComponent(engine));
		camel.addComponent("any23", new Any23Component());
		camel.addComponent("sparql", new SesameComponent(engine.getRepositoryManager()));
//		camel.addComponent("curate", new CurateComponent(engine));

		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
		camel.addComponent("crud", new CRUDComponent(crud));

		floSupport = new RDFCamelPlanner(camel, engine.getFactSpace());

		floSupport.setVocabURI(COMMON.CAMEL_FLO);
		floSupport.plan();

//		SesameCRUD crud = new SesameCRUD(engine.getFactSpace());
//		Collection<Map> routes = crud.read("self/routes", engine.getConfig());
//		for(Map route:routes) {
//			floSupport.plan( (String)route.get("from"), (String)route.get("to"));
//		}
//		log.debug("Deployed "+routes.size()+" primary routes");

//		floSupport.plan(engine.getFactSpace(),engine.getIdentity());
	}

	private void bootSelf(final ExecutionEnvironment engine) throws Exception {
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

}
