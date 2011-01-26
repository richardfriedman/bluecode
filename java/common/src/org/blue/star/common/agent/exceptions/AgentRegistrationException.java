package org.blue.star.common.agent.exceptions;

public class AgentRegistrationException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Blank Constructor
	 */
	public AgentRegistrationException()
	{
		super();
	}
	
	/**
	 * Constructor that takes the exception message as a parameter
	 * @param message - The Exception message
	 */
	public AgentRegistrationException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructor that takes an Exception message and throwable t as parameters
	 * @param message - The Exception message
	 * @param t - The throwable t.
	 */
	public AgentRegistrationException(String message, Throwable t)
	{
		super(message,t);
	}
	
	/**
	 * Constructor that takes a throwable t as a parameter.
	 * @param t - The throwable t.
	 */
	public AgentRegistrationException(Throwable t)
	{
		super(t);
	}
	
}