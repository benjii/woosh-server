package com.luminos.woosh.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.luminos.woosh.domain.common.User;

/**
 * 
 * @author Ben
 */
@Service
public class PushNotificationService {

	private static final Logger LOGGER = Logger.getLogger(PushNotificationService.class);

	
	// note that these two constants must match
	//	i.e.: if we're using the DEV APNS certificate then IS_PRODUCTION must be FALSE
	private static final String APNS_P12_NAME = "woosh-prod-apns.p12";
	
	private static final Boolean IS_PRODUCTION = Boolean.TRUE;

	private static final String APNS_PASSWORD = "cat7,flow";

	
	/**
	 * 
	 * @param recipients
	 * @param message
	 */
	public void alert(final List<User> recipients, final String message) {

		// create a list of APNS tokens to send notifications to
		final String[] tokens = new String[recipients.size()];
		for (int i = 0; i < recipients.size(); i++) {
			tokens[i] = recipients.get(i).getApnsToken();
		}

		// now send the notifications
		try {
			final ClassPathResource cpr = new ClassPathResource(APNS_P12_NAME);
			final InputStream is = cpr.getInputStream();
			
			// execute the APNS call on a separate thread (the response can be slow)
			Executors.newSingleThreadExecutor().execute(new Runnable() {
	
				@Override
				public void run() {
					try {
			
						// send all of the notifications
						// we do this on a thread because it can take a while to publish to APNS
						if ( tokens.length > 0 ) {
							Push.alert(message, is, APNS_PASSWORD, IS_PRODUCTION, tokens);							
						}

						LOGGER.info("Sent APNS notifications to " + recipients.size() + " users.");

					} catch (CommunicationException e) {
						
						// not sending a Push notification is not fatal so we swallow the exception in this case
						LOGGER.info("There was a problem sending Push messages.");
						LOGGER.info(e.getMessage());
						
					} catch (KeystoreException e) {
					
						// not sending a Push notification is not fatal so we swallow the exception in this case
						LOGGER.info("There was a problem sending Push messages.");
						LOGGER.info(e.getMessage());

					}
				}
				
			});
			
		} catch (IOException e) {
			LOGGER.error("The Blue servers could not read the APNS certificate - please investigate.");
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * 
	 * @param recipient
	 */
	public void alert(final User recipient, final String message) {

		try {
			final ClassPathResource cpr = new ClassPathResource(APNS_P12_NAME);
			final InputStream is = cpr.getInputStream();
			
			// execute the APNS call on a separate thread (the response can be slow)
			Executors.newSingleThreadExecutor().execute(new Runnable() {
	
				@Override
				public void run() {
					
					try {
						
						Push.alert(message, is, APNS_PASSWORD, IS_PRODUCTION, recipient.getApnsToken());
						LOGGER.info("Sent APNS notification to user " + recipient.getUsername() + " (" + recipient.getApnsToken() + ")");
					
					} catch (CommunicationException e) {
						
						// not sending a Push notification is not fatal so we swallow the exception in this case
						LOGGER.info("There was a problem sending Push messages.");
						LOGGER.info(e.getMessage());
						
					} catch (KeystoreException e) {
					
						// not sending a Push notification is not fatal so we swallow the exception in this case
						LOGGER.info("There was a problem sending Push messages.");
						LOGGER.info(e.getMessage());

					}
				}
				
			});

		} catch (IOException e) {
			LOGGER.error("The Blue servers could not read the APNS certificate - please investigate.");
			throw new RuntimeException(e);
		}
		
	}
	
}
