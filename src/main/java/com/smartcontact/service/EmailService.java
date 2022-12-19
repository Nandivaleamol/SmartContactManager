package com.smartcontact.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;


@Service
public class EmailService {
	
	 // this is responsible to send email...
		public boolean sendEmail(String message, String subject, String to) {
			
			boolean flag = false;
			
			// variable for gmail
			String host = "smtp.gmail.com";
			
			String from = "nandivaleamol@gmail.com";
			
			// get the system properties
			Properties properties = System.getProperties();
			System.out.println("PROPERTIES " + properties);
			
			// host set
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", "465");
			properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.smtp.auth", "true");
			
			// Step 1: to get the session object...
			Session session = Session.getInstance(properties, new Authenticator() {
				

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {

					
					return new PasswordAuthentication("nandivaleamol@gmail.com", "omcjjybygprzkcvb");
				}
				
			});
			
			session.setDebug(true);
			
			// Step 2: Compose the message [text, multi media]
			MimeMessage mimeMessage = new MimeMessage(session);
			
			try {
				//from email
				mimeMessage.setFrom(from);
				
				//adding recipient to message
				mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				
				//adding subject to message
				mimeMessage.setSubject(subject);
				
				// adding text to message
				mimeMessage.setText(message);
				
				//send
			
				
				// Step 3: send the message using Transport class
				Transport.send(mimeMessage);
				
				System.out.println("Sent success...");
				
				flag = true;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return flag;
		}
		

}
