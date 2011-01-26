package org.blue.star.commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.commands;
import org.blue.star.common.objects;
import org.blue.star.include.objects_h;

public class delete_service implements ICommand
{
	private static Logger logger = LogManager.getLogger("org.blue.commands");
	
	public String getCommandName() {
		
		return "EXECUTE_JAVA_COMMAND";
	}

	public String getCommandString() {
		
		return "EXECUTE_JAVA_COMMAND;org.blue.commands.delete_service;%s;%s";
	}

	public void processCommand(long timestamp, String args)
	{
		if(args == null)
			return;
		
		String[] bits = args.split(";");
		
		if(bits.length !=2)
		{
			logger.info( "Incorrect command parameters. Expecting : " + getCommandString() );
		    return;
		}
		
		objects_h.service s = objects.find_service(bits[0],bits[1]);
		
		if(s == null)
		{
			logger.info("Service '" + bits[0]+ "," + bits[1] + "' does not exist in current Blue Objects");
			return;
		}
		
		/* Again try to be clean about this an disable the service checking first */
		commands.disable_service_checks(s);
		
		if(objects.remove_service(bits[0],bits[1]))
		{
			if(objects.find_service(bits[0],bits[1]) == null)
			{
				logger.info("Service '" + bits[0] + "," + bits[1] + "' successfully removed from Blue");
				return;
			}
		}
		
		logger.info("Error: Unable to remove Service '" + bits[0] + "," + bits[1]);				
	}

}
