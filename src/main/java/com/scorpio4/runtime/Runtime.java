package com.scorpio4.runtime;

import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.AssetRegisters;
import com.scorpio4.fact.FactSpace;
import com.scorpio4.util.Identifiable;
import com.scorpio4.vendor.camel.planner.RDFRoutePlanner;
import com.scorpio4.vendor.camel.planner.RoutePlanner;
import com.scorpio4.vendor.camel.component.Any23Component;
import com.scorpio4.vendor.camel.component.CoreComponent;
import com.scorpio4.vendor.camel.component.SelfComponent;
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
public class Runtime implements Identifiable, Runnable {
	final Logger log = LoggerFactory.getLogger(this.getClass());

	RepositoryManager manager = null;
	AssetRegister assetRegister = null;
	FactSpace factSpace = null;
	Registry registry = null;
	CamelContext camel = null;

	RoutePlanner routePlanner;
	boolean isRunning = false;
	Repository repository = null;
	Map<String,Object> properties = null;
//	File rootDir = null;

	protected Runtime() {
	}

	protected void init(String identity, RepositoryManager manager, Map<String,Object> properties) throws Exception {
		log.debug("Runtime: "+identity);
		this.properties=properties;
		this.manager=manager;
		initFactSpace(identity);
		initCamel();
		initPlanning();
		initBootstrap(properties);
	}

	protected void initFactSpace(String identity) throws MalformedURLException, RepositoryException, RepositoryConfigException {
		repository = manager.getRepository(identity);
		if (repository==null) throw new RepositoryException("Missing repository: "+identity);

		RepositoryConnection connection = repository.getConnection();
		factSpace = new FactSpace( connection, identity );
		assetRegister = new AssetRegisters(connection);

	}

	protected void initBootstrap(Map<String, Object> properties) {
		log.debug("Booting API");
		String bootstrapURI = "direct:"+getIdentity();
		try {
			routePlanner.trigger(bootstrapURI, null, properties);
		} catch(Exception e) {
			log.debug("Bootstrap Failed: "+bootstrapURI+" -> "+e.getMessage());
		}
	}

	protected void initPlanning() throws Exception {
		log.debug("Routing API"+getFactSpace().getIdentity());
		routePlanner = new RDFRoutePlanner(camel, getFactSpace());
		routePlanner.plan();
	}

	protected void initCamel() {
		log.debug("Connecting API");
		registry = new JndiRegistry();
		camel = new DefaultCamelContext(registry);

		camel.addComponent("self", new SelfComponent(this));
		camel.addComponent("core", new CoreComponent(getFactSpace(), assetRegister ));
		camel.addComponent("any23", new Any23Component());
	}

	public void start() throws Exception {
		log.debug("Starting Runtime");
		camel.start();
		isRunning = true;
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

	public RoutePlanner getRoutePlanner() {
		return routePlanner;
	}

}
