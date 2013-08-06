package com.kerberos.device.rest.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.management.InvalidAttributeValueException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.kerberos.device.rest.api.AuthenticationAPIImpl.ServiceTicketResponseAttributes;
import com.kerberos.device.rest.representation.kerberos.ServiceTicketRequest;
import com.kerberos.device.rest.representation.kerberos.ServiceTicketResponse;
import com.kerberos.device.util.connectionmanager.ConnectionManagerImpl.ContentType;
import com.kerberos.device.util.connectionmanager.ConnectionManagerImpl.RequestMethod;
import com.kerberos.device.util.connectionmanager.IConnectionManager;
import com.kerberos.device.util.dateutil.IDateUtil;
import com.kerberos.device.util.encryption.IEncryptionUtil;
import com.kerberos.device.util.hashing.IHashUtil;

public class ServiceRequestAPIImpl implements IServiceRequestAPI {
	
	private @Autowired IDateUtil iDateUtil;
	private @Autowired IEncryptionUtil iEncryptionUtil;
	private @Autowired IHashUtil iHashUtil;
	private @Autowired IConnectionManager iConnectionManager;
	
	private static Logger log = Logger.getLogger(ServiceRequestAPIImpl.class);
	
	@Override
	public Map<ServiceTicketResponseAttributes, String> requestServiceTicketForApp(String url, String encAppTGTPacket, String serviceName, String sessionKey) throws IOException, InvalidAttributeValueException {
		
		log.debug("Entering requestServiceTicketForApp method");
		
		if (!iEncryptionUtil.validateDecryptedAttributes(url, encAppTGTPacket, serviceName, sessionKey)){
			log.error("Invalid input parameter to requestServiceTicketForApp");
			throw new InvalidAttributeValueException("Invalid input parameter to requestServiceTicketForApp");
		}
		
		String authenticator = iDateUtil.createAuthenticator();
		
		SecretKey key = iEncryptionUtil.generateSecretKey(sessionKey);
		String[] encryptedData  = iEncryptionUtil.encrypt(key, serviceName, authenticator);
		String encServiceName  = encryptedData[0];
		String encAuthenticator = encryptedData[1];
		
		ServiceTicketRequest request = new ServiceTicketRequest(encAppTGTPacket, encServiceName, encAuthenticator);
		String requestString = iConnectionManager.generateJSONStringForObject(request);
		
		ServiceTicketResponse response = (ServiceTicketResponse)iConnectionManager.generateRequest(url, RequestMethod.POST_REQUEST_METHOD, ContentType.APPLICATION_JSON, ServiceTicketResponse.class, requestString);
		if (response == null){
			return null;
		}
		
		log.debug("Returning from requestServiceTicketForApp method");
		
		return processServiceTicketResponse(response, authenticator, key);
	}
	
	/**
	 * @param response
	 * @param authenticator
	 * @param key
	 * @return
	 * @throws InvalidAttributeValueException
	 */
	public Map<ServiceTicketResponseAttributes, String> processServiceTicketResponse(ServiceTicketResponse response, String authenticator, SecretKey key) throws InvalidAttributeValueException {
		
		log.debug("Entering processServiceTicketResponse method");
		
		if (response == null || authenticator == null || authenticator.isEmpty() || key == null){
			log.error("Invalid input parameter to processServiceTicketResponse");
			throw new InvalidAttributeValueException("Invalid input parameter to processServiceTicketResponse");
		}
		
		Map<ServiceTicketResponseAttributes, String> responseAttributes = decryptServiceTicketResponse(response, key);
		if (responseAttributes == null){
			return null;
		}
		
		String serviceTicketPacket = responseAttributes.get(ServiceTicketResponseAttributes.SERVICE_TICKET_PACKET); 
		String serviceSessionID = responseAttributes.get(ServiceTicketResponseAttributes.SERVICE_SESSION_ID);
		String serviceName = responseAttributes.get(ServiceTicketResponseAttributes.SERVICE_NAME);
		String decAuthenticator = responseAttributes.get(ServiceTicketResponseAttributes.AUTHENTICATOR);
		
		if (!iEncryptionUtil.validateDecryptedAttributes(serviceTicketPacket, serviceSessionID, serviceName, decAuthenticator) 
				|| !iDateUtil.validateAuthenticator(iDateUtil.generateDateFromString(decAuthenticator), iDateUtil.generateDateFromString(authenticator))){
			return null;
		}
		
		return responseAttributes;
	}	

	
	/**
	 * @param response
	 * @param key
	 * @return
	 * @throws InvalidAttributeValueException
	 */
	public Map<ServiceTicketResponseAttributes, String> decryptServiceTicketResponse(ServiceTicketResponse response, SecretKey key) throws InvalidAttributeValueException{
		
		log.debug("Entering method decryptServiceTicketResponse");
		
		if (response == null || key == null){
			log.error("Invalid Input parameter to decryptServiceTicketResponse");
			throw new InvalidAttributeValueException("Invalid Input parameter to decryptServiceTicketResponse");
		}
		
		String[] decryptedData  = iEncryptionUtil.decrypt(key, response.getEncServiceTicket(), response.getEncServiceSessionID(), response.getEncServiceName(), response.getEncAuthenticator());
		String serviceTicketPacket = decryptedData[0];
		String serviceSessionID = decryptedData[1];
		String serviceName = decryptedData[2];
		String authenticator = decryptedData[3];
		
		Map<ServiceTicketResponseAttributes, String> responseAttributes = new HashMap<ServiceTicketResponseAttributes, String>();
		
		responseAttributes.put(ServiceTicketResponseAttributes.SERVICE_TICKET_PACKET, serviceTicketPacket);
		responseAttributes.put(ServiceTicketResponseAttributes.SERVICE_NAME, serviceName);
		responseAttributes.put(ServiceTicketResponseAttributes.SERVICE_SESSION_ID, serviceSessionID);
		responseAttributes.put(ServiceTicketResponseAttributes.AUTHENTICATOR, authenticator);
		
		return responseAttributes;
	}
	
}
