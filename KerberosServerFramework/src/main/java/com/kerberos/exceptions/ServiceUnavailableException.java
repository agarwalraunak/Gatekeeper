/**
 * 
 */
package com.kerberos.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author raunak
 *
 */
public class ServiceUnavailableException extends WebApplicationException {

	public ServiceUnavailableException(String message, Response.Status status, String mediaType) {
		super(Response.status(status).entity(message).type(mediaType).build());
	}	
}
