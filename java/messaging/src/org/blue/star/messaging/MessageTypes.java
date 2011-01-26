package org.blue.star.messaging;

/**
 * This interface defines the currently available message types that can be used in conjunction
 * with the Message class.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public interface MessageTypes
{
	/** Identifies an error has occurred with the Registry */
    	final public static String REGISTRY_ERROR = "registry_error";
	/** Identifies an error has occurred with the Agent */
    	final public static String AGENT_ERROR = "agent_error";
    	/** Identifies a request to register with the Registry */
	final public static String REQUEST_TO_REGISTER = "request_to_register";
	/** Identifies a response from the registry to a request to register */
	final public static String REGISTRATION_RESPONSE = "registration_response";
	/** Identifies a request to unregister from the Registry*/
	final public static String REQUEST_TO_UNREGISTER = "request_to_unregister";
	/** Identifies a response from the registry to a request to unregister */
	final public static String UNREGISTER_RESPONSE = "unregister_response";
	/** Used to instruct a remote Agent to expect Aggressive Checks */
	final public static String ENTER_AGRESSIVE_MODE = "enter_aggressive_mode";
	/** Used to instruct a remote Agent to conduct Passive Checks */
	final public static String ENTER_PASSIVE_MODE = "enter_passive_mode";
	/** Used to notify a remote agent to a change in it's configuration */
	final public static String CONFIGURATION_UPDATE = "configuration_update";
	/** Used to identify the result of a remote service check */
	final public static String REMOTE_SERVICE_CHECK_RESULT = "remote_service_check_result";
}