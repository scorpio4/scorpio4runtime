package com.scorpio4.runtime;

import com.scorpio4.fact.FactSpace;
import com.scorpio4.util.map.MapUtil;
import com.scorpio4.vendor.sesame.RepositoryManager;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.runtime
 * User  : lee
 * Date  : 24/06/2014
 * Time  : 8:00 PM
 */
public class Server extends Engine {
	static final Logger log = LoggerFactory.getLogger(Server.class);
	static String name = "scorpio4";
	Map runtimes = new HashMap();

	public Server(String identity, File rootDir, Map<String,String> properties) throws Exception {
		log.debug("Working directory: "+rootDir.getAbsolutePath());
		rootDir.mkdirs();
		init(identity, new RepositoryManager(rootDir), properties);
		initRuntimes();
	}


	protected void initRuntimes() throws Exception {
		FactSpace factSpace = new FactSpace(getIdentity(), getRepository());
		Collection<Map> all_runtimes = new SesameCRUD(factSpace).read("self/engines", properties);
		for(Map runtime: all_runtimes) {
			String id = (String)runtime.get("id");
			// don't start self again
			if (!id.equalsIgnoreCase(getIdentity())) {
				Engine engine = new Engine();
				// shared repository manager
				engine.init(id, manager, runtime);
				runtimes.put(id, engine);
				log.debug("Starting "+engine.getIdentity());
				engine.start();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("Configuration file not specified");
			System.exit(-1);
		}

		File configFile = new File(args[0]);
		try {
			if (!configFile.exists()) throw new IOException("Properties not found: "+configFile.getAbsolutePath());

			Properties properties = new Properties();
			properties.load(new FileReader(configFile));

			String identity = MapUtil.getString(properties, name + ".id");
			File path = MapUtil.getFile(properties, name+".home", new File("runtime.facts", name));

			Map headers = new HashMap();
			headers.putAll(properties);

			Server server = new Server(identity, path, headers);
			Thread thread = new Thread(server);

			log.debug("Starting Engines: "+server.getIdentity() );
			thread.start();
			while(server.isRunning()) {
				Thread.sleep(100);
			}
			log.debug("Stopping Engines: "+server.getIdentity() );
			server.stop();

		} catch (FileNotFoundException e) {
			log.debug("Properties not found: "+configFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
