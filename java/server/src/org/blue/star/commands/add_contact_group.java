/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
 *
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 *****************************************************************************/

package org.blue.star.commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.common.objects;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.xdata.xodtemplate;

public class add_contact_group implements ICommand {

	private static Logger logger = LogManager.getLogger("org.blue.commands.add_contact_group");
	
	public String getCommandName()
	{
		return "EXECUTE_JAVA_COMMAND";
	}

	public String getCommandString()
	{
		return "EXECUTE_JAVA_COMMAND;org.blue.commands.add_contact_group;%s;%s";
	}

	public void processCommand(long timestamp, String args)
	{
		if(args == null )
		{
		    logger.info( "No parameters passed to command. Expecting : " + getCommandString());
		    return;
		}

		String[] split = args.split(";");
		
		if(split.length != 2)
		{
		    logger.info( "Incorrect command parameters. Expecting : " + getCommandString() );
		    return;
		}

		String file = split[0].trim();
		String contact_group = split[1].trim();
		int priorCount = 0;
		
		if(contact_group.equalsIgnoreCase("all"))
		{
			priorCount = objects.contactgroup_list.size();
		}

		logger.debug( "Processed command " + args );
		
		if(xodtemplate.xodtemplate_read_dynamic_data(blue.config_file,file,common_h.READ_CONTACTGROUPS,common_h.TRUE) == common_h.OK)
		{
			
			if(!contact_group.equalsIgnoreCase("all"))
			{
			    if(objects.find_contactgroup(contact_group)!=null)
			    {
			    	logger.info("INFO: Contact '" +  contact_group + "' Successfully added!");
			    }
			    else
			    {
			    	logger.info("ERROR: Failed to add Contact Group '" + contact_group + "'");
			    }
			}
			else
			{
				logger.info("INFO: Added '" + (objects.contactgroup_list.size() - priorCount) + "' contact groups from file '" + file + "'");
			}
		}
		else
		{
		    logger.info( "Failed to add Contact Group '" + contact_group + "'");
		}
	}
}