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
import org.blue.star.base.commands;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.xdata.xodtemplate;

/**
 *
 */
public class add_host implements ICommand {

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.commands");

    public void processCommand(long timestamp, String args)
    {
    	if( args == null )
    	{
    		logger.info("ERROR: No parameters passed to command. Expecting : " + getCommandString() );
    		return;
    	}

    	String[] split = args.split(";");
    	
    	if ( split.length != 2 )
    	{
    		logger.info( "ERROR: Incorrect command parameters. Expecting : " + getCommandString() );
    		return;
    	}

    	String file = split[0].trim();
    	String host = split[1].trim();
    	
    	if(objects.find_host(host) != null)
    	{
    		logger.info("Error: Host with name '" + host + "' already exists.");
    		return;
    	}
    	
    	int priorCount = 0;
    	
    	
    	if(host.equalsIgnoreCase("all"))
    	{
    		priorCount = objects.host_list.size();
    	}
    	
    	logger.debug( "Processed command " + args );
    	
    	if ( xodtemplate.xodtemplate_read_dynamic_data( blue.config_file, file, common_h.READ_HOSTS, common_h.TRUE ) == common_h.OK )
    	{
    		if(!host.equalsIgnoreCase("all"))
    		{
    			objects_h.host temp_host = objects.find_host(host);
    			
    			if(temp_host!=null)
    			{
    				commands.enable_host_checks(temp_host);
    				statusdata.update_host_status( temp_host, common_h.FALSE );
    			}
    			else
    			{
    				logger.info( "ERROR: Failed to Enable Checks on Host '" + host + "'");
    			}
    		}
    		else
    		{
    			logger.info("INFO: Added '" + (objects.host_list.size() - priorCount) + "' hosts from file '" + file + "'");
    		}
    	}
    	else
    	{
    		logger.info("ERROR: Failed to add host '" + host + "'");
    	}
    }
   
    public String getCommandString()
    {
    	return "EXECUTE_JAVA_COMMAND;org.blue.commands.add_host;%s;%s";
    }
    
    public String getCommandName()
    {
    	return "EXECUTE_JAVA_COMMAND";
    }
}