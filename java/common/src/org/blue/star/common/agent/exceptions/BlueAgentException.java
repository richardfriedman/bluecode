package org.blue.star.common.agent.exceptions;

/**
 * This class should be viewed as a wrapper class for all exceptions that can be thrown by
 * the Blue Agent.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class BlueAgentException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Blank Constructor */
    public BlueAgentException()
    {
	
    }
    
    /**
     * Constructor that takes a Message as a parameter
     * @param message - The message of the exception
     */
    public BlueAgentException(String message)
    {
		super(message);
    }
    
    /**
     * Constructor that takes a Message and Throwable t as parameters
     * @param message - The message of the Exception
     * @param t - The throwable associated with the exception.
     */
    public BlueAgentException(String message, Throwable t)
    {
    	super(message,t);
    }
    
    /**
     * Constructor that takes a Throwable t as a parameter
     * @param t - The Throwable t.
     */
    public BlueAgentException(Throwable t)
    {
    	super(t);
    }
}