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
 * User  : lee
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
