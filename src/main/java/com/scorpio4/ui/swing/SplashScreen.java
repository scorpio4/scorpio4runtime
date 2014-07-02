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

import com.scorpio4.oops.ConfigException;
import com.scorpio4.template.PicoTemplate;
import com.scorpio4.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.ui.splash
 * User  : lee
 * Date  : 31/10/2013
 * Time  : 5:37 PM
 */
public class SplashScreen extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(SplashScreen.class);
    JLabel introLabel = null;
    JLabel progressPanel = null;
    JPanel mainPanel = null;
    Map progressMessages = new HashMap();
    String browserURL = null;

    public SplashScreen(String title, String version, String splashAsset, String splashHTML) {
        display(title, version, splashAsset, splashHTML);
    }

    public void display(final String title, String version, String splashAsset, String splashHTML) {
        setTitle(title);
        log.debug("Display Splash: "+title);

        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        URL iconURL = Thread.currentThread().getContextClassLoader().getResource(splashAsset);
        Image icon = Toolkit.getDefaultToolkit().getImage(iconURL);
        setIconImage(icon);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(Color.WHITE);

        JPanel logoPanel = new JPanel();
        logoPanel.setSize(150,150);
        ImagePanel splashLogo = null;
        try {
            log.debug("Loading Image Panel: "+splashAsset);
            splashLogo = new ImagePanel(splashAsset);
            logoPanel.add(splashLogo);
        } catch(IOException e) {
            log.error("Failed to load Image Panel: "+splashAsset,e);
        }

        introLabel = new JLabel("<html><body><h3>"+title+" "+version+"</h3></body></html>");
        introLabel.setPreferredSize(new Dimension(400, 50));
        introLabel.setBackground(Color.CYAN);

        progressPanel = new JLabel(splashHTML);
        progressPanel.setPreferredSize(new Dimension(400, 50));

        JPanel messagePanel = new JPanel();

        BorderLayout messageLayout = new BorderLayout();
        messagePanel.setLayout(messageLayout);
        messageLayout.setVgap(0);
        messageLayout.setHgap(0);
//        messagePanel.setPreferredSize(new Dimension(400, 125));
        messagePanel.add(introLabel, "North");
        messagePanel.add(progressPanel, "South");

        mainPanel.add(logoPanel, "West");
        mainPanel.add(messagePanel, "East");

        getContentPane().add(mainPanel);

        log.debug("Decorate Splash Frame");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(450, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(3);
        pack();
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public void redisplay(String message) {
        progressPanel.setText(message);
        setVisible(true);
    }

    public void done() {
        introLabel.setText("<html><body><b>"+progressPanel.getText()+"</b> "+getBrowserURL()+"</body></html>");
        progress("System is ready for users");
        this.setVisible(false);
    }

    public void progress(String text) {
        progress(text, null, null);
    }

    public void setProgressMessages(Map progressMessages) {
        this.progressMessages = progressMessages;
        log.debug("Progress Messages: "+progressMessages);
    }

//    @Override
    public void progress(String markerURI, Stopwatch elapsed, Map meta) {
        if (markerURI.startsWith("urn:")) {
            markerURI = markerURI.substring(4).replace(":",".");
        }
        String text = (String)progressMessages.get(markerURI);
        if (text==null) text = markerURI;
        try {
            PicoTemplate picoTemplate = new PicoTemplate(text);
            progressPanel.setText( picoTemplate.translate(meta) );
            log.debug("progress: {}", progressPanel.getText());
        } catch (ConfigException e) {
            progressPanel.setText(text + "[!!]");
            log.error("progress: {}", progressPanel.getText(),e);
        }
    }

    public void setBrowserURL(String url) {
        this.browserURL = url;
    }

    public String getBrowserURL() {
        return this.browserURL;
    }

}
