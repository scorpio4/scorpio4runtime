package com.scorpio4.output.email;
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
import com.scorpio4.util.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.output.email
 * User  : lee
 * Date  : 15/11/2013
 * Time  : 4:50 PM
 */
public class SendEmail implements Configurable {
    private static final Logger log = LoggerFactory.getLogger(SendEmail.class);
    Properties properties = System.getProperties();
    Session session = null;
    String from = null;

    public SendEmail() {
    }

    public void boot() {
//        properties.setProperty("mail.user", "myuser");
//        properties.setProperty("mail.password", "mypwd");
        session = Session.getDefaultInstance(properties);
    }


    public void send(String to, String subject, String body) {
        send(to,subject,body,null);
    }

    public void send(String to, String subject, String body, Map<String,InputStream> attachments) {
        try{
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message.setSubject(subject);

            if (attachments==null||attachments.size()==0) {
                message.setContent(body,"text/html" );
            } else {
                Multipart multipart = new MimeMultipart();

                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);
                multipart.addBodyPart(messageBodyPart);

                attach( multipart, attachments );
                message.setContent( multipart );
            }


            // Send message
            Transport.send(message);
            log.debug("Sent message to: "+to);
        }catch (MessagingException e) {
            log.debug("urn:scorpio4:output:email:Send:oops:sending#" + e.getMessage(), e);
        }
    }

    private void attach(Multipart multipart, Map<String,InputStream> attachments) throws MessagingException {
        for(String filename: attachments.keySet()) {
            log.debug("Attaching : "+filename);
            InputStream inputStream = attachments.get(filename);

            MimeBodyPart attachment = new MimeBodyPart(inputStream);
            attachment.setDisposition(Part.ATTACHMENT);
            attachment.setHeader("Content-Type", "application/pdf");
            multipart.addBodyPart(attachment);
        }

    }

    @Override
    public void configure(Map config) throws ConfigException {
        properties.clear();
        properties.putAll(config);
    }

    @Override
    public Map getConfiguration() {
        return properties;
    }
}
