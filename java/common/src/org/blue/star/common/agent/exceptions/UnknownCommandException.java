package org.blue.star.common.agent.exceptions;

/**
 * This Exception can be thrown by the CommandCache should a request for an 
 * unknown command be made.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class UnknownCommandException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Blank Constructor
     */
    public UnknownCommandException()
    {
	
    }
    
    /**
     * Constructor that takes a String message as a parameter
     * @param message - The message for the exception
     */
    public UnknownCommandException(String message)
    {
    	super(message);
    }
    
    /**
     * Constructor that takes a String message and throwable t as parameters
     * @param message - The message for the Exception
     * @param t - The throwable t of the Exception
     */
    public UnknownCommandException(String message, Throwable t)
    {
    	super(message,t);
    }
    
    /**
     * Constructor that takes a Throwable t as a parameter
     * @param t - The throwable t
     */
    public UnknownCommandException(Throwable t)
    {
    	super(t);
    }
}