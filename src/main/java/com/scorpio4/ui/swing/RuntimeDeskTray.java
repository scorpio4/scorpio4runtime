package com.scorpio4.ui.swing;

import com.scorpio4.iq.vocab.ActiveVocabulary;
import com.scorpio4.oops.ConfigException;
import com.scorpio4.oops.FactException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import org.openrdf.repository.RepositoryException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.ui.swing
 * User  : lee
 * Date  : 3/08/2014
 * Time  : 9:55 PM
 */
public class RuntimeDeskTray {
	DeskTray tray;
	ExecutionEnvironment engine;

	public RuntimeDeskTray(ExecutionEnvironment engine, DeskTray tray) throws RepositoryException, FactException, IOException, ConfigException {
		this.engine=engine;
		this.tray=tray;
	}

	public Menu addFloMenu(Menu menu, final ActiveVocabulary vocab) throws RepositoryException, FactException, IOException, ConfigException {
		SesameCRUD crud = new SesameCRUD(engine);
		Collection<Map> flows = crud.read("self/menu/flows", engine.getConfig());
		for(final Map flow: flows) {
			MenuItem menuItem = tray.newMenu((String) flow.get("label"), new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Object activated = vocab.activate((String) flow.get("from"), flow);
					} catch (Exception e1) {
						tray.notify(e1.getMessage());
					}
				}
			});
			menu.add(menuItem);
		}
		crud.close();
		return menu;
	}

	public Menu addMenu(String label, ActionListener al) throws RepositoryException, FactException, IOException, ConfigException {
		Menu menu = tray.addMenu(label);
		if (al!=null) menu.addActionListener(al);
		return menu;
	}

	public Menu addAdminMenu() {
		tray.getMenu().addSeparator();
//		Menu admin = tray.addMenu("Admin");
		MenuItem reboot = tray.newMenu("Reboot", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					tray.getMenu().setEnabled(false);
					engine.reboot();
					tray.getMenu().setEnabled(true);
				} catch (Exception e1) {
					tray.notify(e1.getMessage());
				}
			}
		});
		tray.getMenu().add(reboot);
//		admin.add(reboot);
		MenuItem shutdown = tray.newMenu("Quit", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					tray.getMenu().setEnabled(false);
					System.exit(0);
				} catch (Exception e1) {
					tray.notify(e1.getMessage());
				}
			}
		});
		tray.getMenu().add(shutdown);
//		admin.add(shutdown);
		return tray.getMenu();
	}

	public DeskTray getDeskTray() {
		return this.tray;
	}

	public static void browse(String url) throws URISyntaxException, IOException {
		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.BROWSE)) return;
		URI uri = new URI(url);
		desktop.browse(uri);
	}

	public MenuItem add(MenuItem mainMenu) {
		getDeskTray().getMenu().add(mainMenu);
		return mainMenu;
	}

	public MenuItem add(String label, ActionListener al) {
		MenuItem menuItem = getDeskTray().newMenu(label, al);
		return add(menuItem);
	}
}
