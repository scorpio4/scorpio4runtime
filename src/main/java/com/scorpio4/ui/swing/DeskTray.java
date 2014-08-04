package com.scorpio4.ui.swing;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.ui.desktop
 * @author lee
 * Date  : 5/11/2013
 * Time  : 3:41 PM
 */
public class DeskTray implements ActionListener {
    private static final Logger log = LoggerFactory.getLogger(DeskTray.class);
    TrayIcon trayIcon = null;
    Map<String,MenuItem> menus = new HashMap();
    String title = "Scorpio4", tooltip = "Not Configured", icon = null;
    PopupMenu popup = new PopupMenu();
	ActionListener actionListener = this;

    public DeskTray(String title, String tooltip, String icon) {
        this.title=title;
        this.tooltip=tooltip;
        this.icon=icon;
        display();
    }

    public void display() {
        try {
            trayIcon = addTrayIcon(icon);
        } catch (AWTException e) {
            log.error("Failed to add Tray icon", e);
        }
    }

	public PopupMenu getMenu() {
		return this.popup;
	}

    public SystemTray getTray() {
        return SystemTray.getSystemTray();
    }

    public TrayIcon addTrayIcon(String _iconURL) throws AWTException {
	    Image icon = null;
	    if (_iconURL!=null) {
		    URL iconURL = Thread.currentThread().getContextClassLoader().getResource(_iconURL);
		    icon = Toolkit.getDefaultToolkit().getImage(iconURL);
	    }
        TrayIcon trayIcon = new TrayIcon(icon);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(tooltip);
	    trayIcon.setPopupMenu(popup);
        getTray().add(trayIcon);
        return trayIcon;
    }

    public MenuItem on(String uri, ActionListener al) {
        MenuItem menuItem = getMenu(uri);
        if (menuItem!=null) menuItem.addActionListener(al);
        else log.warn("Missing DeskTray Icon: "+uri);
        return menuItem;
    }

	public Menu addMenu(String label) {
		Menu menu = new Menu(label);
		popup.add(menu);
		return menu;
	}

    public MenuItem addMenu(String _this, String label) {
        MenuItem menuItem = new MenuItem(label);
        popup.add(menuItem);
	    register(_this, menuItem);
        return menuItem;
    }

	public MenuItem newMenu(String label, ActionListener al) {
		MenuItem menuItem = new MenuItem(label);
		if (al!=null) menuItem.addActionListener(al);
		return menuItem;
	}

	private void register(String menuURI, MenuItem item) {
        this.menus.put(menuURI, item);
	    item.addActionListener(actionListener);
    }

    public MenuItem getMenu(String menuURI) {
        return menus.get(menuURI);
    }

    public boolean browser(String url) {
        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.BROWSE)) return false;
        try {
            URI uri = new URI(url);
            desktop.browse(uri);
            return true;
        } catch (IOException e) {
            log.warn("Failed Browser IO for: "+url,e);
        } catch (URISyntaxException e) {
            log.warn("Failed Browser URI for: "+url,e);
        }
        return false;
    }

    public void notify(String msg) {
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.WARNING);
    }

    public void notify(String title, String msg) {
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.INFO);
        log.debug("DeskTray: "+msg);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

	/**
	 * Invoked when an action occurs.
	 *
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		log.debug("ACTION: "+e.getActionCommand()+" -> "+e.getSource());
	}
}
