package com.kerberos.service.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.management.InvalidAttributeValueException;

public class AppSession {
	
	private String appSessionID;					//Session ID generated by Service
	private String appServiceSessionID;		//Session ID generated by kerberos for the app
	private String appID;							//App username
	private List<UserSession> userSessions;
	private List<Date> authenticators;
	
	public AppSession() {
		userSessions = new ArrayList<UserSession>();
		authenticators = new LinkedList<>();
	}
	
	/**
	 * @return the appSessionID
	 */
	public String getAppSessionID() {
		return appSessionID;
	}

	/**
	 * @param appSessionID the appSessionID to set
	 */
	public void setAppSessionID(String appSessionID) {
		this.appSessionID = appSessionID;
	}

	/**
	 * @return the appID
	 */
	public String getAppID() {
		return appID;
	}

	/**
	 * @param appID the appID to set
	 */
	public void setAppID(String appID) {
		this.appID = appID;
	}

	/**
	 * @return the userSession
	 */
	public List<UserSession> getUserSession() {
		return userSessions;
	}
	/**
	 * @return the appServiceSessionID
	 */
	public String getAppServiceSessionID() {
		return appServiceSessionID;
	}
	/**
	 * @param appServiceSessionID the appServiceSessionID to set
	 */
	public void setAppServiceSessionID(String appServiceSessionID) {
		this.appServiceSessionID = appServiceSessionID;
	}

	/**
	 * @return the authenticators
	 */
	public List<Date> getAuthenticators() {
		return authenticators;
	}
	
	/**
	 * @param username
	 * @param userSessionID
	 * @return
	 */
	public UserSession createUserSession(String username, String userSessionID){
		
		UserSession userSession = new UserSession();
		userSession.setUsername(username);
		userSession.setUserSessionID(userSessionID);
		
		userSessions.add(userSession);
		
		return userSession;
	}
	
	/**
	 * @param username
	 * @return
	 */
	public UserSession findUserSessionByUsername(String username){
		
		for(UserSession session: userSessions){
			if (session.getUsername().equals(username)){
				return session;
			}
		}
		return null;
	}
	
	/**
	 * @param sessionID
	 * @return
	 */
	public UserSession findUserSessionBySessionID(String sessionID){
		
		for(UserSession session: userSessions){
			if (session.getUserSessionID().equals(sessionID)){
				return session;
			}
		}
		return null;
	}
	
	
	/**
	 * @param authenticator
	 * @throws InvalidAttributeValueException
	 */
	public void addAuthenticator(Date authenticator) throws InvalidAttributeValueException{
		
		if(authenticator == null){
			throw new InvalidAttributeValueException("Invalid parameter provided to addAuthenticator");
		}
		authenticators.add(authenticator);
	}
	
	/**
	 * @param authenticator
	 * @return
	 * @throws InvalidAttributeValueException
	 */
	public boolean validateAuthenticator(Date authenticator) throws InvalidAttributeValueException{
		
		if(authenticator == null){
			throw new InvalidAttributeValueException("Invalid parameter provided to addAuthenticator");
		}
		
		Date lastAuthenticator;
		if (authenticators.size() > 0)
			lastAuthenticator = authenticators.get(authenticators.size()-1);
		else
			return false;
		
		if (((new Date().getTime() - authenticator.getTime()) > 5000 * 60) && authenticator.getTime() - lastAuthenticator.getTime() != 60000) {
			return false;
		}
		return true;
	}
}
