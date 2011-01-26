package org.blue.star.registry.exceptions;

/**
 * This class is used to wrap any Exceptions thrown when accessing the HostCache of
 * the Registry.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class RegistryCacheException extends Exception
{
	private static final long serialVersionUID = 1L;

	public RegistryCacheException() {
		super();
	}

	public RegistryCacheException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RegistryCacheException(String arg0) {
		super(arg0);
	}

	public RegistryCacheException(Throwable arg0) {
		super(arg0);
	}
}
