package org.blue.star.messaging;

import java.util.Map;

/**
 * This interface defines the Message class. The Message class is a serializable object
 * that can be exchanged between the Registry and Remote Blue Agents.
 * 
 * The design of this class should be simple to reduce the overheads of sending/receiving messages.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public interface Message
{
    /**
     * This sets the current type of this message.
     * @param messageType - The current type of this message.
     */
    public void setMessageType(String messageType);
    
    /**
     * This method returns the current type of this message.
     * @return - The current type of this message.
     */
    public String getMessageType();
    
    /**
     * This method is used to set the properties of the message.
     * @param properties - The properties associated with this message.
     */
    public void setMessageProperties(Map<String,String> properties);
    
    /**
     * This method is used to retrieve any properties currently associated with this message.
     * @return - The properties associated with this message.
     */
    public Map getMessageProperties();
}