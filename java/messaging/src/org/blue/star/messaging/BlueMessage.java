package org.blue.star.messaging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is an implementation of the org.blue.star.registry.messaging.Message interface.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 * @see - <b>org.blue.star.registry.messaging.Message</b> - The Interface this class implements.
 *
 */
public class BlueMessage implements Serializable,Message
{
	private static final long serialVersionUID = 1L;
	/** The properties of this Message */
	private Map properties;
	/** The type of this Message */
	private String messageType;
	
	/** Blank Constructor */
	public BlueMessage()
	{
	    
	}
	
	/**
	 * Constructor
	 * @param messageType - String the type of this message.
	 * @param properties - Any properties associated with this message.
	 */
	public BlueMessage(String messageType,HashMap properties)
	{
		this.messageType = messageType;
		this.properties = properties;
	}
	
	/**
	 * Returns the current Properties associated with this Message
	 * 
	 * @return - Hashmap of the current Message properties.
	 */
	public Map getMessageProperties()
	{
		return this.properties;
	}
	
	/**
	 * Set the properties that are associated with this message. Valid properties can be
	 * found in org.blue.registry.MessageProperties.
	 * 
	 * @param HashMap containing the relevant message Properties.
	 */
	public void setMessageProperties(Map<String,String> properties)
	{
		this.properties = properties;
	}
	
	/**
	 * Returns the Type of message that this represents. Valid message Types can be found
	 * in org.blue.registry.Requests.
	 * 
	 * @return String - The current Message Type.
	 */
	public String getMessageType()
	{
		return this.messageType;
	}
	
	/**
	 * Sets the Type of this message. Valid message Types can be found in the interface
	 * org.blue.registry.Requests.
	 * 
	 * @param messageType - The Type of message this object represents.
	 */
	
	public void setMessageType(String messageType)
	{
		this.messageType = messageType;
	}
}