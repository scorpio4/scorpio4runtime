package com.scorpio4.runtime;

import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.iq.vocab.ActiveVocabulary;
import com.scorpio4.iq.vocab.Scorpio4ActiveVocabularies;
import com.scorpio4.util.Identifiable;
import com.scorpio4.vendor.sesame.RepositoryManager;
import com.scorpio4.vendor.sesame.util.SesameHelper;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.runtime
 * @author lee
 * Date  : 24/06/2014
 * Time  : 12:00 AM
 */
public class Engine implements ExecutionEnvironment, Identifiable, Runnable {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	String identity;
	RepositoryManager manager = null;

	Repository repository = null;
	AssetRegister assetRegister = null;
	RepositoryConnection connection = null;
	GenericApplicationContext springContext;

	boolean isRunning = false;
	Map<String,String> properties = null;

	ActiveVocabulary activeVocabulary;
//	private CachedBeanFactory beanFactory;
	private ClassLoader classloader;

	protected Engine() {
	}

	public Engine(String identity, RepositoryManager manager, Map<String,String> properties) throws Exception {
		init(identity, manager, properties);
	}

	protected void init(String identity, RepositoryManager manager, Map<String,String> properties) throws Exception {
		log.debug("Engine: "+identity);
		this.properties=properties;
		this.manager=manager;
		this.classloader = Thread.currentThread().getContextClassLoader();
		boot(identity);
	}

	protected void boot(String identity) throws Exception {
		this.identity=identity;

		this.repository = manager.getRepository(identity);
		if (repository==null) throw new RepositoryException("Missing repository: "+identity);

		connection = repository.getConnection();
		SesameHelper.defaultNamespaces(connection);

		this.assetRegister = new AssetRegisters(connection);
		springContext = RuntimeHelper.newSpringContext(this);
		log.debug("VM stats: " + RuntimeHelper.getVMStats());

		activeVocabulary = new Scorpio4ActiveVocabularies(this);
	}

	public void start() throws Exception {
		if (connection==null||!connection.isOpen()) {
			// should only be called after a stop(), never a cold start
			connection = repository.getConnection();
		}

		log.debug("Starting Engine: "+RuntimeHelper.getVMStats());
		final Engine self = this;

		activeVocabulary.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					activeVocabulary.activate("direct:self:graceful", self);
					log.error("Graceful shutdown ...");
					self.stop();
				} catch (Exception e) {
					log.error("FATAL shutdown", e);
				}
			}
		});
		isRunning = true;
		activeVocabulary.activate("direct:self:started", self);
		log.debug("Engine Running: " + RuntimeHelper.getVMStats());
	}

	public void reboot() throws Exception {
		stop();
		boot(getIdentity());
		start();
	}

	@Override
	public ClassLoader getClassLoader() {
		return classloader;
	}

	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
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
		activeVocabulary.activate("direct:self:stopping", this);

		log.debug("Stopping Engine");
		activeVocabulary.stop();

		connection.close();
		repository.shutDown();
		isRunning = false;
	}

	public String getIdentity() {
		return identity;
	}

	public RepositoryManager RepositoryManager() {
		return manager;
	}

	public AssetRegister getAssetRegister() {
		return assetRegister;
	}

//	public FactSpace getFactSpace() {
//		return factSpace;
//	}

	public ActiveVocabulary getActiveVocabulary() {
		return activeVocabulary;
	}

	public Map<String, String> getConfig() {
		return properties;
	}

	public ApplicationContext getRegistry() {
		return springContext;
	}

	public RepositoryResolver getRepositoryManager() {
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
