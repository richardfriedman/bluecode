package org.blue.star.common.agent.utils;

/**
 * This class provides Utility methods for the Agent.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class Utils 
{
	/**
	 * This method is used to replace $ARG$ variables in a command line with actual
	 * values.
	 * @param commandLine - The command line containing the macros.
	 * @param commandArgs - The command args that should be used to replace the macros.
	 * @return - The command line with all macros replaced.
	 */
	public static String parseCommandMacros(String commandLine,String commandArgs)
	{
		if(commandLine == null || commandArgs == null)
			return null;
		
		/* Split the string around spaces to get a list of our commands args */
		String[] args = commandArgs.split(" ");
		String[] split = commandLine.split("\\$");
		String newCommandLine = "";
		int replaceCount = 0;
		
		for(String s: split)
		{
			/* Test to see if we have a $ARG$ */
			if(s.indexOf("ARG") == 0 && replaceCount < args.length)
			{
				s = s.replaceAll("ARG[0-9]+",args[replaceCount]);
				replaceCount++;
			}
			
			newCommandLine += s;
		}
		
		/* Remove all $ characters in the string */
		newCommandLine = newCommandLine.replace("$","");
		
		return newCommandLine;
	}
}