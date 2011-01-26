package org.blue.star.messaging;

/**
 * This interface is used to identify the properties that can currently be sent within the Message
 * class. 
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */

public interface MessageProperties
{
	/** This property identifies the 'type' of the remote agent. */
    final public static String AGENT_TYPE = "agent_type"; 
    /** Identifies the host name of origin for this message */
    final public static String HOST_NAME = "host_name";
    /** Identifies the IP Address of origin for this message */
	final public static String IP_ADDRESS = "ip_address";
	final public static String REMOTE_CONFIG = "remote_config";
	final public static String KEEP_ALIVE = "keep_alive";
	final public static String PERSIST_REGISTRATION = "persist_registration";
	final public static String REGISTRY_ERROR_MESSAGE = "registry_error_message";
	final public static String AGENT_ERROR_MESSAGE = "agent_error_message";
	/** This property identifies the name of the plugin that should be executed */
	final public static String PLUGIN_NAME = "plugin_name";
	/** This property identifies any arguments that should be passed to the plugin*/
	final public static String PLUGIN_ARGS = "plugin_args";
	/** This property identifies any output from the execution of the plugin */
	final public static String PLUGIN_OUTPUT = "plugin_output";
	/** This property identifies the return code from the execution of the plugin */
	final public static String PLUGIN_RETURN_CODE = "plugin_return_code";
}