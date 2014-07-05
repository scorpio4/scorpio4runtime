package com.scorpio4.runtime;

import com.scorpio4.ExecutionEnvironment;
import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.fact.FactSpace;
import com.scorpio4.iq.ActiveVocabularies;
import com.scorpio4.util.Identifiable;
import com.scorpio4.vendor.sesame.RepositoryManager;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.runtime
 * User  : lee
 * Date  : 24/06/2014
 * Time  : 12:00 AM
 */
public class Engine implements ExecutionEnvironment, Identifiable, Runnable {
	final Logger log = LoggerFactory.getLogger(this.getClass());

	RepositoryManager manager = null;

	Repository repository = null;
	AssetRegister assetRegister = null;
	FactSpace factSpace = null;

	boolean isRunning = false;
	Map<String,String> properties = null;

	ActiveVocabularies activeVocabularies;

	protected Engine() {
	}

	public Engine(String identity, RepositoryManager manager, Map<String,String> properties) throws Exception {
		init(identity,manager,properties);
	}

	protected void init(String identity, RepositoryManager manager, Map<String,String> properties) throws Exception {
		log.debug("Engine: "+identity);
		this.properties=properties;
		this.manager=manager;
		boot(identity);
	}

	protected void boot(String identity) throws Exception {
		repository = manager.getRepository(identity);
		if (repository==null) throw new RepositoryException("Missing repository: "+identity);

		RepositoryConnection connection = repository.getConnection();
		factSpace = new FactSpace( connection, identity );
		assetRegister = new AssetRegisters(connection);
		activeVocabularies = new ActiveVocabularies(this);

	}

	public void start() throws Exception {
		log.debug("Starting Engine");
		final Engine self = this;

		activeVocabularies.start();
		internalRoutes();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void xrun() {
				try {
					activeVocabularies.trigger("direct:self:graceful");
					log.error("Graceful shutdown ...");
					self.stop();
				} catch (Exception e) {
					log.error("FATAL shutdown", e);
				}
			}
		});
		isRunning = true;
		activeVocabularies.trigger("direct:self:started");
	}

	private void internalRoutes() throws Exception {
		final Engine self = this;
		RouteBuilder routeBuilder = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:self:reboot").process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						self.reboot();
					}
				});
				from("direct:self:stop").process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						self.stop();
					}
				});
			}
		};
		activeVocabularies.addRoutes(routeBuilder);
	}

	public void reboot() throws Exception {
		stop();
		boot(getIdentity());
		start();
	}

	public void run() {
		log.debug("Running ...");
		while(isRunning) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("FATAL", e);
			}
		}
	}

	public void stop() throws Exception {
		activeVocabularies.trigger("direct:self:stopping");
		log.debug("Stopping Engine");
		activeVocabularies.stop();
		factSpace.getConnection().close();
		repository.shutDown();
		isRunning = false;
	}

	public String getIdentity() {
		return factSpace==null?"bean:"+getClass().getCanonicalName():factSpace.getIdentity();
	}

	public RepositoryManager RepositoryManager() {
		return manager;
	}

	public AssetRegister getAssetRegister() {
		return assetRegister;
	}

	public FactSpace getFactSpace() {
		return factSpace;
	}

	public ActiveVocabularies ActiveVocabularies() {
		return activeVocabularies;
	}

	public CamelContext getCamelContext() {
		return activeVocabularies.getCamelContext();
	}

	public Map<String, String> getConfig() {
		return properties;
	}

	public GenericApplicationContext getRegistry() {
		return activeVocabularies.getRegistry();
	}

	public RepositoryManager getRepositoryManager() {
		return manager;
	}

	public Repository getRepository() {
		return repository;
	}

	public boolean isRunning() {
		return isRunning;
	}

//	public ActiveVocabularies getActiveVocabularies() {
//		return activeVocabularies;
//	}

}
