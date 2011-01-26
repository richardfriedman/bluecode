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
import org.blue.star.xdata.xodtemplate;

public class add_contact implements ICommand
{
	private static Logger logger = LogManager.getLogger("org.blue.commands.add_contact");
	
	public String getCommandName()
	{
		return "EXECUTE_JAVA_COMMAND";
	}

	public String getCommandString()
	{
		return "EXECUTE_JAVA_COMMAND;org.blue.commands.add_contact;%s;%s";
	}

	public void processCommand(long timestamp, String args)
	{
		if(args == null )
		{
		    logger.info("ERROR: No parameters passed to command. Expecting : " + getCommandString());
		    return;
		}

		String[] split = args.split(";");
		
		if(split.length != 2)
		{
		    logger.info("ERROR: Incorrect command parameters. Expecting : " + getCommandString() );
		    return;
		}

		String file = split[0].trim();
		String contact = split[1].trim();
		int priorCount = 0;

		if(contact.equalsIgnoreCase("all"))
		{
			priorCount = objects.contact_list.size();
		}
		
		logger.debug( "Processed command " + args );
		
		if(xodtemplate.xodtemplate_read_dynamic_data(blue.config_file,file,common_h.READ_CONTACTS,common_h.TRUE) == common_h.OK)
		{
		    if(!contact.equalsIgnoreCase("all"))
		    {
			    if(objects.find_contact(contact) != null)
			    {
			    	logger.info("Contact '" +  contact + "' Successfully added!");
			    }
			    else
			    {
			    	logger.info("Failed to add Contact '" + contact + "'");
			    }
		    }
		    else
		    {
		    	logger.info("INFO: Added '" + (objects.contact_list.size() - priorCount) + "' contacts from file '" + file + "'");
		    }
		}
		else
		{
		    logger.info( "Failed to add Contact '" + contact + "'");
		}
	}
}