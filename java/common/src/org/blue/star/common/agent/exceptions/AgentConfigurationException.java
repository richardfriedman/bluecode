package org.blue.star.common.agent.exceptions;

/**
 * This class is used to act as a wrapper for Exceptions that are thrown in the process
 * of configuring the Agent.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class AgentConfigurationException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Blank Constructor
	 */
	public AgentConfigurationException()
	{
		super();
	}
	
	/**
	 * Constructor that takes a string message as a parameter
	 * @param message - The message of the exception
	 */
	public AgentConfigurationException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructor that takes a message and throwable t as parameters
	 * @param message - The message of the exception
	 * @param t - The throwable t.
	 */
	public AgentConfigurationException(String message, Throwable t)
	{
		super(message,t);
	}
	
	/**
	 * Constructor that takes a Throwable t as a parameter
	 * @param t - The throwable t.
	 */
	public AgentConfigurationException(Throwable t)
	{
		super(t);
	}
}