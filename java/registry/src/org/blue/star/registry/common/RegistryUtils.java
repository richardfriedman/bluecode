package org.blue.star.registry.common;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Utility class for common methods between the packages of the Registry.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class RegistryUtils 
{
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger(RegistryUtils.class);
	private static String cn = "org.blue.star.registry.common.RegistryUtils";
	
	/**
	 * This method returns the integer value of a String.
	 * @param value - The String to get the integer from.
	 * @return - The integer value of the String.
	 */
	public static int atoi(String value)
	{
	    try
	    {
	        return Integer.parseInt(value);
	    }
	    catch(NumberFormatException nfE)
	    {
	       logger.error(cn + ".atoi() - warning: " + nfE.getMessage(), nfE);
	       return -1;
	    }
	}
	
	/**
	 * Method to change an int value into a boolean.
	 * @param change - The int value to change.
	 * @return - boolean true or false dependent on int value.
	 */
	public static boolean intToBoolean(int change)
	{
		return change == 1;
	}
	
	/**
	 * This method is used to get the boolean value of a string.
	 * @param change - The string to get the boolean value from.
	 * @return - The boolean value of this string.
	 */
	public static boolean stringToBoolean(String change)
	{
		if(change == null || change.length() == 0)
			return false;
		
		return change.equals("1");
	}
	
	public static String booleanToString(boolean b)
	{
	    if(b)
	    {
		return "1";
	    }
	    
	    return "0";
	}
}
