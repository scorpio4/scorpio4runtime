package com.scorpio4.runtime;

import com.scorpio4.ui.swing.DeskTray;
import com.scorpio4.util.map.MapUtil;
import com.scorpio4.vendor.sesame.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.runtime
 * @author lee
 * Date  : 24/06/2014
 * Time  : 12:00 AM
 */
public class Personal extends Engine {
	static final Logger log = LoggerFactory.getLogger(Personal.class);
	DeskTray deskTray;

	public Personal(String name, String identity, File rootDir, Map<String,String> properties) throws Exception {
		log.debug("Working directory: " + rootDir.getAbsolutePath());
		boolean done = rootDir.mkdirs();
		init(name, identity, new RepositoryManager(rootDir), properties);
	}

	public void init(String name, String identity, RepositoryManager repositoryManager, Map<String, String> properties) throws Exception {
		this.deskTray = new DeskTray(name, name, "images/logo.png");
		super.init(identity, repositoryManager, properties);
	}

	public static void main(String[] args) {
		String name = "scorpio4";
		String configPath = name+".properties";
		File configFile = null;
		try {
			if (args.length>0) configPath = args[0];
			configFile = new File(configPath);
			if (!configFile.exists()) throw new IOException("Properties not found: "+configFile.getAbsolutePath());
			Properties properties = new Properties();
			properties.load(new FileReader(configFile));
			
			String identity = MapUtil.getString(properties, name+".id");
			File path = MapUtil.getFile(properties, name+".home", new File("runtime.facts", name));
			boolean doBootStrap = !path.exists();

			Map headers = new HashMap();
			headers.putAll(properties);
			Personal personal = new Personal(name, identity, path, headers);
			log.debug("Starting "+name+" from "+path.getAbsolutePath());

			String bootstrap = MapUtil.getString(properties, name+".bootstrap");
			if (doBootStrap && bootstrap!=null && !bootstrap.equals("")) {
				log.warn("Bootstrap from " + bootstrap);
				RuntimeHelper.provision(personal.getRepository(), personal.getClassLoader(), bootstrap);
			}

			personal.start();

		} catch (FileNotFoundException e) {
			log.debug("Properties not found: "+configFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
