package com.scorpio4.runtime;

import com.scorpio4.ExecutionEnvironment;
import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.fact.FactSpace;
import com.scorpio4.iq.DefaultActiveVocabularies;
import com.scorpio4.util.Identifiable;
import com.scorpio4.vendor.sesame.RepositoryManager;
import com.scorpio4.vendor.spring.CachedBeanFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
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
	ApplicationContext applicationContext;

	boolean isRunning = false;
	Map<String,String> properties = null;

	DefaultActiveVocabularies defaultActiveVocabularies;
	private CachedBeanFactory beanFactory;

	protected Engine() {
	}

	public Engine(String identity, RepositoryManager manager, Map<String,String> properties) throws Exception {
		init(identity, manager, properties);
	}

	protected void init(String identity, RepositoryManager manager, Map<String,String> properties) throws Exception {
		log.debug("Engine: "+identity);
		this.properties=properties;
		this.manager=manager;
		boot(identity);
	}

	protected void boot(String identity) throws Exception {
		this.beanFactory = new CachedBeanFactory();
		this.applicationContext = new GenericApplicationContext(this.beanFactory);

		beanFactory.cache("engine", this);
		beanFactory.cache("facts", this.getFactSpace());
		beanFactory.cache("assets", this.getAssetRegister());
		beanFactory.cache("config", this.getConfig());
		beanFactory.cache("sesame", this.getRepositoryManager());
		beanFactory.cache("core", repository);

		repository = manager.getRepository(identity);
		if (repository==null) throw new RepositoryException("Missing repository: "+identity);

		RepositoryConnection connection = repository.getConnection();
		factSpace = new FactSpace( connection, identity );
		assetRegister = new AssetRegisters(connection);
		defaultActiveVocabularies = new DefaultActiveVocabularies(this);
	}

	public void start() throws Exception {
		log.debug("Starting Engine");
		final Engine self = this;

		defaultActiveVocabularies.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void xrun() {
				try {
					defaultActiveVocabularies.trigger("direct:self:graceful");
					log.error("Graceful shutdown ...");
					self.stop();
				} catch (Exception e) {
					log.error("FATAL shutdown", e);
				}
			}
		});
		isRunning = true;
		defaultActiveVocabularies.trigger("direct:self:started");
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
		defaultActiveVocabularies.trigger("direct:self:stopping");
		log.debug("Stopping Engine");
		defaultActiveVocabularies.stop();
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

	public DefaultActiveVocabularies ActiveVocabularies() {
		return defaultActiveVocabularies;
	}

	public Map<String, String> getConfig() {
		return properties;
	}

	public ApplicationContext getRegistry() {
		return applicationContext;
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
