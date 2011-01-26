package org.blue.star.registry.exceptions;

/**
 * This exception is used to wrap any exceptions that are thrown during the configuration
 * of the Blue Dynamic Registry
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class RegistryConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;

	public RegistryConfigurationException() {
		super();
	}

	public RegistryConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RegistryConfigurationException(String arg0) {
		super(arg0);
	}

	public RegistryConfigurationException(Throwable arg0) {
		super(arg0);
	}
}