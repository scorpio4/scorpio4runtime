package com.scorpio4.runtime;

import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.fact.FactSpace;
import com.scorpio4.iq.ActiveVocabularies;
import com.scorpio4.util.Identifiable;
import com.scorpio4.vendor.sesame.RepositoryManager;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spi.Registry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.runtime
 * User  : lee
 * Date  : 24/06/2014
 * Time  : 12:00 AM
 */
public class Engine implements Identifiable, Runnable {
	final Logger log = LoggerFactory.getLogger(this.getClass());

	RepositoryManager manager = null;
	AssetRegister assetRegister = null;
	FactSpace factSpace = null;
	Registry registry = null;
	CamelContext camel = null;

	boolean isRunning = false;
	Repository repository = null;
	Map<String,Object> properties = null;
//	File rootDir = null;

	protected Engine() {
	}

	protected void init(String identity, RepositoryManager manager, Map<String,Object> properties) throws Exception {
		log.debug("Runtime: "+identity);
		this.properties=properties;
		this.manager=manager;
		boot(identity);

		new ActiveVocabularies(this);
	}

	protected void boot(String identity) throws MalformedURLException, RepositoryException, RepositoryConfigException {
		this.registry = new JndiRegistry();
		this.camel = new DefaultCamelContext(registry);
		repository = manager.getRepository(identity);
		if (repository==null) throw new RepositoryException("Missing repository: "+identity);

		RepositoryConnection connection = repository.getConnection();
		factSpace = new FactSpace( connection, identity );
		assetRegister = new AssetRegisters(connection);
	}

	public void start() throws Exception {
		log.debug("Starting Runtime");
		camel.start();
		isRunning = true;
		final Engine self = this;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					log.error("Graceful shutdown ...");
					self.stop();
				} catch (Exception e) {
					log.error("FATAL shutdown", e);
				}
			}
		});
	}

	public void run() {
		log.debug("Running ...");
		while(isRunning) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() throws Exception {
		log.debug("Stopping Runtime");
		camel.stop();
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

	public Registry getRegistry() {
		return registry;
	}

	public CamelContext getCamelContext() {
		return camel;
	}

	public Map<String, Object> getConfig() {
		return properties;
	}
}
