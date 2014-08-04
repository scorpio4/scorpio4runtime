package com.scorpio4.runtime;

import com.scorpio4.oops.FactException;
import com.scorpio4.security.webid.WebIDMaker;
import com.scorpio4.ui.swing.DeskTray;
import com.scorpio4.ui.swing.RuntimeDeskTray;
import com.scorpio4.util.map.MapUtil;
import com.scorpio4.vendor.camel.flo.FLOSupport;
import com.scorpio4.vendor.sesame.RepositoryManager;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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
	RuntimeDeskTray trayUI = null;
	File rootDir, srcDir;

	public Personal(String name, String identity, File rootDir, Map<String,String> properties) throws Exception {
		log.debug("Working directory: " + rootDir.getAbsolutePath());
		init(name, identity, rootDir, new RepositoryManager(rootDir), properties);
	}

	public void init(String name, String identity, File rootDir, RepositoryManager repositoryManager, Map<String, String> properties) throws Exception {
		this.trayUI = new RuntimeDeskTray(this, new DeskTray(name, name, "images/logo.png"));
		this.rootDir=rootDir;
		this.srcDir = MapUtil.getFile(properties, "src", null);
		init(identity, repositoryManager, MapUtil.getConfig(properties, name + "."));

		// menu
		trayUI.getDeskTray().getMenu().add(name);
		trayUI.getDeskTray().getMenu().addSeparator();
	}

	@Override
	public void start() throws Exception {
		Menu actions = trayUI.addMenu("Actions", null);
		Menu menu = trayUI.addAdminMenu();

		actions.setEnabled(false);
		menu.setEnabled(false);

		bootstrap();
		super.start();

		menu.setEnabled(true);
		actions.setEnabled(true);
		trayUI.addFloMenu(actions, getActiveVocabulary());
	}

	protected void bootstrap() throws RepositoryException, IOException, FactException {
		if (srcDir!=null) {
			if (!srcDir.isAbsolute()) srcDir = new File(rootDir, srcDir.toString());
			srcDir.mkdirs();
			log.debug("SDK Provision: "+srcDir.getAbsolutePath());
			RuntimeHelper.empty(this);
			RuntimeHelper.provision(this, srcDir);
		}

		if (!isInstalled() && FLOSupport.canDereference(getIdentity())) {
			RuntimeHelper.empty(this);
			RuntimeHelper.provision(this, new URL(getIdentity()));
		}

	}

	public void stop() throws Exception {
		super.stop();
	}

	public static void main(String[] args) {
		String name = "scorpio4";
		String configPath = name+".properties";
		try {
			if (args.length>0) configPath = args[0];
			File configFile = new File(configPath);
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

			personal.start();
		} catch (IOException e) {
			log.error("IO Error: "+e.getMessage(), e);
		} catch (Exception e) {
			log.error("Initialization Error: "+e.getMessage(), e);
		}
	}
}
