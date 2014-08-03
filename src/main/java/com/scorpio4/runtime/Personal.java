package com.scorpio4.runtime;

import com.scorpio4.security.webid.WebIDMaker;
import com.scorpio4.ui.swing.DeskTray;
import com.scorpio4.util.map.MapUtil;
import com.scorpio4.vendor.sesame.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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
	File rootDir;

	public Personal(String name, String identity, File rootDir, Map<String,String> properties) throws Exception {
		log.debug("Working directory: " + rootDir.getAbsolutePath());
		init(name, identity, rootDir, new RepositoryManager(rootDir), properties);
	}

	public void init(String name, String identity, File rootDir, RepositoryManager repositoryManager, Map<String, String> properties) throws Exception {
		this.deskTray = new DeskTray(name, name, "images/logo.png");
		this.rootDir=rootDir;
		init(identity, repositoryManager, MapUtil.getConfig(properties, name + "."));
	}

	@Override
	public void start() throws Exception {
		super.start();
	}

	public static void main(String[] args) {
		String name = "scorpio4";
		String configPath = name+".properties";
		File configFile = null;
		try {
			if (args.length>0) configPath = args[0];
			configFile = new File(configPath);
			log.debug("Properties from: " + configFile.getAbsolutePath());
			if (!configFile.exists()) throw new IOException("Properties not found: "+configFile.getAbsolutePath());
			Properties properties = new Properties();
			properties.load(new FileReader(configFile));
			
			String identity = MapUtil.getString(properties, name+".id");
			File homeDir = MapUtil.getFile(properties, name+".home", new File(name+".home"));
			homeDir.mkdirs();

			File keystore = new File(homeDir, "keystore.ks");
			if (!keystore.exists()) {
				WebIDMaker.newStore(identity, identity, keystore);
			}

			Map headers = new HashMap();
			headers.putAll(properties);
			Personal personal = new Personal(name, identity, homeDir, headers);
			log.debug("---------------------------------------- ----------------------------------------");

			File srcDir = MapUtil.getFile(properties, name + ".src", null);
			if (srcDir!=null) {
				if (!srcDir.isAbsolute()) srcDir = new File(homeDir, srcDir.toString());
				srcDir.mkdirs();
				log.debug("SDK Provision: "+srcDir.getAbsolutePath());
				RuntimeHelper.provision(personal, srcDir);
			}
			if (!personal.isInstalled()) {
				RuntimeHelper.provision(personal, new URL(identity));
			}

			personal.start();
		} catch (IOException e) {
			log.error("IO Error: "+e.getMessage(), e);
		} catch (Exception e) {
			log.error("Initialization Error: "+e.getMessage(), e);
		}
	}
}
