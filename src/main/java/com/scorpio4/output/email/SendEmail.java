package com.scorpio4.output.email;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
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
 * @author lee
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
