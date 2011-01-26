package org.blue.star.registry.exceptions;

/**
 * This Exception is used to wrap exceptions thrown by the Blue Dynamic Registry
 * when attempting to write out configuration files.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */

public class ConfigurationGenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConfigurationGenerationException() {
		super();
	}

	public ConfigurationGenerationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConfigurationGenerationException(String arg0) {
		super(arg0);
	}

	public ConfigurationGenerationException(Throwable arg0) {
		super(arg0);
	}
}
