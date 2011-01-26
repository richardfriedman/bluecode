package org.blue.star.registry.exceptions;

/**
 * This Exception is used to wrap any exceptions thrown during the registration process.
 * This Exception is only thrown by the BlueDynamicRegistry.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class RegistrationRequestException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public RegistrationRequestException()
	{
		
	}
	
	public RegistrationRequestException(String message)
	{
		super(message);
	}
	
	public RegistrationRequestException(String message,Throwable t)
	{
		super(message,t);
	}
	
	public RegistrationRequestException(Throwable t)
	{
		super(t);
	}
}
