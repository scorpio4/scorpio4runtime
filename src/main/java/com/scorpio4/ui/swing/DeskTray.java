package com.scorpio4.ui.swing;
/*
 *   Scorpio4 - CONFIDENTIAL
 *   Unpublished Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *   NOTICE:  All information contained herein is, and remains the property of Lee Curtis. The intellectual and technical concepts contained
 *   herein are proprietary to Lee Curtis and may be covered by Australian, U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 *   from Lee Curtis.  Access to the source code contained herein is hereby forbidden to anyone except current Lee Curtis employees, managers or contractors who have executed
 *   Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 *   The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 *   information that is confidential and/or proprietary, and is a trade secret, of Lee Curtis.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 *   OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF LEE CURTIS IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 *   LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 *   TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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
 * User  : lee
 * Date  : 5/11/2013
 * Time  : 3:41 PM
 */
public class DeskTray {
    private static final Logger log = LoggerFactory.getLogger(DeskTray.class);
    TrayIcon trayIcon = null;
    Map<String,MenuItem> menus = new HashMap<String, MenuItem>();
    String title = "Scorpio4", tooltip = "Not Configured", icon = null;
    PopupMenu popup = new PopupMenu();

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
        trayIcon.setToolTip( tooltip );
        trayIcon.setPopupMenu(makePopup());
        getTray().add(trayIcon);
        return trayIcon;
    }

    public MenuItem on(String uri, ActionListener al) {
        MenuItem menuItem = getMenu(uri);
        if (menuItem!=null) menuItem.addActionListener(al);
        else log.warn("Missing DeskTray Icon: "+uri);
        return menuItem;
    }

    public MenuItem addMenu(String _this, String label) {
        MenuItem aboutItem = new MenuItem(label);
        register(_this, aboutItem);
        popup.add(aboutItem);
        return aboutItem;
    }

    protected PopupMenu makePopup() {
        // About Menu
        MenuItem aboutItem = new MenuItem("About "+title);
        register("urn:scorpio4:ui:desktop:tray:about", aboutItem);
        popup.add(aboutItem);

        // Login Menu
        MenuItem adminItem = new MenuItem("Login ...");
        register("urn:scorpio4:ui:desktop:tray:login", adminItem);
        popup.add(adminItem);

        // Monitor Menu
        Menu monitorMenu = new Menu("Commands");
        popup.addSeparator();
        popup.add(monitorMenu);

        MenuItem appsItem = new MenuItem("Applications");
        register("urn:scorpio4:ui:desktop:tray:applications", appsItem);
        monitorMenu.add(appsItem);

        MenuItem statsItem = new MenuItem("System Stats");
        register("urn:scorpio4:ui:desktop:tray:stats", statsItem);
        monitorMenu.add(statsItem);

        MenuItem sanityItem = new MenuItem("Sanity Checks");
        register("urn:scorpio4:ui:desktop:tray:sanity", sanityItem);
        monitorMenu.add(sanityItem);

        // Admin Menu
        Menu adminMenu = new Menu("Admin");
        popup.addSeparator();
        popup.add(adminMenu);

        // Reboot
        MenuItem rebootItem = new MenuItem("Reboot");
        register("urn:scorpio4:ui:desktop:tray:reboot", rebootItem);
        adminMenu.add(rebootItem);

        // Redeploy
        MenuItem redeployItem = new MenuItem("Re-deploy");
        register("urn:scorpio4:ui:desktop:tray:redeploy", redeployItem);
        adminMenu.add(redeployItem);

        // Shutdown
        MenuItem shutdownItem = new MenuItem("Shutdown");
        register("urn:scorpio4:ui:desktop:tray:shutdown", shutdownItem);
        adminMenu.add(shutdownItem);

        return popup;
    }

    private void register(String menuURI, MenuItem item) {
        this.menus.put(menuURI, item);
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

}
