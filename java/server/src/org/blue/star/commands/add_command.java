package org.blue.star.commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.common.objects;
import org.blue.star.include.common_h;
import org.blue.star.xdata.xodtemplate;

/**
 * This class is used to add a list of commands to the Blue Server monitoring configuration.
 * 
 * @author Rich.Friedman
 * @author Rob.Blake@arjuna.com
 * 
 * @verson 0.2 - <p>V2: Extended to support the 'all' parameter as the command to add. This means
 * that all the command will report the number of commands added from the designated config file
 * rather than searching for a single command</p>
 *
 */
public class add_command implements ICommand 
{
	private static Logger logger = LogManager.getLogger("org.blue.commands");
	
	public String getCommandName()
	{
		return "EXECUTE_JAVA_COMMAND";
	}

	public String getCommandString()
	{
		return "EXECUTE_JAVA_COMMAND;org.blue.command.add_command;%s;%s";
	}

	public void processCommand(long timestamp, String args)
	{
		if(args == null )
		{
		    logger.info("ERROR: No parameters passed to command. Expecting : " + getCommandString() );
		    return;
		}

		String[] split = args.split(";");
		
		if(split.length != 2)
		{
		    logger.info("ERROR: Incorrect command parameters. Expecting : " + getCommandString() );
		    return;
		}

		String file = split[0];
		String command = split[1];
		int priorCount = 0;
		
		if(command.equalsIgnoreCase("all"))
		{
			priorCount = objects.command_list.size();
		}
		
		if(xodtemplate.xodtemplate_read_dynamic_data(blue.config_file,file,common_h.READ_COMMANDS,common_h.TRUE) == common_h.OK)
		{
		    if(!command.equalsIgnoreCase("all"))
		    {
		    	if(objects.find_command(command)!=null)
		    	{
		    		logger.info("INFO: Command '" +  command + "' Successfully added!");
		    	}
		    	else
		    	{
		    		logger.info("ERROR: Failed to add command '" + command + "'");
		    	}
		    }
		    else
		    {
		    	logger.info("INFO: Added '" + (objects.command_list.size() - priorCount) + "' Commands from file '" + file + "'");
		    }
		}
		else
		{
		    logger.info("ERROR: Failed to add Command '" + command + "'");
		}
	}
}