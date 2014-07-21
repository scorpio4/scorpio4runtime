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
import java.lang.*;
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
	static String name = "scorpio4";
	DeskTray deskTray;

	public Personal(String identity, File rootDir, Map<String,String> properties) throws Exception {
		log.debug("Working directory: " + rootDir.getAbsolutePath());
		boolean done = rootDir.mkdirs();
		init(identity, new RepositoryManager(rootDir), properties);
	}

	@Override
	public void init(String identity, RepositoryManager repositoryManager,  Map<String,String> properties) throws Exception {
		this.deskTray = new DeskTray(name,name, "images/logo.png");
		super.init(identity, repositoryManager, properties);
	}

	public static void main(String[] args) {
		String configPath = name+".properties";
		File configFile = null;
		try {
			if (args.length>0) configPath = args[0];
			configFile = new File(configPath);
			if (!configFile.exists()) throw new IOException("Properties not found: "+configFile.getAbsolutePath());
			Properties properties = new Properties();
			properties.load(new FileReader(configFile));
			
			String identity = MapUtil.getString(properties, name+".id");
			File path = MapUtil.getFile(properties, name+".directory", new File("runtime.facts", name));

			Map headers = new HashMap();
			headers.putAll(properties);
			Personal personal = new Personal(identity, path, headers);
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
