package org.blue.star.registry.exceptions;

/**
 * This Exception is thrown by the RegistryObjects store if an attempt is made
 * to retrieve an unknown object.
 * 
 * @author Rob.Blake@arjuna.com
 * @verson 0.1
 *
 */
public class UnknownObjectException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnknownObjectException() {
		super();
	}

	public UnknownObjectException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownObjectException(String message) {
		super(message);
	}

	public UnknownObjectException(Throwable cause) {
		super(cause);
	}
}
