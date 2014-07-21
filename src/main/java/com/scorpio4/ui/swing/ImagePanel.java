package com.scorpio4.ui.swing;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.ui.splash
 * @author lee
 * Date  : 31/10/2013
 * Time  : 5:26 PM
 */

public class ImagePanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);

    private Image image;

    public ImagePanel(File imageFile) throws IOException {
        image = ImageIO.read(imageFile);
        if (image==null) throw new IOException("Can't find Image file: "+imageFile);
    }

    public ImagePanel(String imageAsset) throws IOException {
        log.debug("Image Panel ToolKit: "+this.getToolkit());
        URL resource = Thread.currentThread().getContextClassLoader().getResource(imageAsset);
        if (resource==null) throw new IOException("Can't find Image resource: "+imageAsset);

        this.image =  getToolkit().createImage(resource);
//        this.image = new ImageIcon(resource).getImage();
        log.debug("Image Panel Resource: "+imageAsset+" -> "+resource+" "+image);
        if (image==null) throw new IOException("Can't get Image resource: "+imageAsset);
        MediaTracker tracker = new MediaTracker(this);
        try {
            ImageObserver iobs = new ImageObserver() {
                @Override
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    log.debug("Image Updated: "+infoflags+" -> "+x+" x "+y+" -> "+width+" x "+height);
                    return true;
                }
            };
            tracker.addImage(image, 0);
            tracker.waitForID(0,3000);// wait for 3 seconds
            log.debug("Found Image: " + image.getWidth(iobs)+" x "+image.getHeight(iobs));
        } catch(Exception e) {
            log.error("Image Panel Failed: " + e.getMessage(), e);
        }
        log.debug("Image Loaded: "+image);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image!=null) {
            g.drawImage(image, 0, 10, null); // see javadoc for more info on the parameters
        }
    }

}
