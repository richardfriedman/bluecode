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

public class add_service implements ICommand {

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.commands");

    public void processCommand(long timestamp, String args) {
	if ( args == null ) {
	    logger.info( "No parameters passed to command. Expecting : " + getCommandString() );
	    return;
	}

	String[] split = args.split(";");
	if ( split.length != 3 ) {
	    logger.info( "Incorrect command parameters. Expecting : " + getCommandString() );
	    return;
	}

	String file = split[0];
	String host = split[1];
	String service = split[2];
	int priorCount =0;
	
	if(service.equalsIgnoreCase("all"))
	{
		priorCount = objects.service_list.size();
	}
	
	logger.debug( "Processed command " + args );
	
	if ( xodtemplate.xodtemplate_read_dynamic_data( blue.config_file, file, common_h.READ_SERVICES, 1 ) == common_h.OK )
	{
	    if(!service.equalsIgnoreCase("all"))
	    {
	    	objects_h.service temp_service = objects.find_service( host, service );
	    	
	    	if(temp_service!=null)
	    	{
	    		commands.enable_service_checks(temp_service);
	    		statusdata.update_service_status( temp_service, common_h.FALSE );
	    	}
	    	else
	    	{
	    		logger.info("ERROR: Failed to Enable Checks on Service '" + service + "'");
	    	}
	    }
	    else
	    {
	    	logger.info("INFO: Added '" + (objects.service_list.size() - priorCount) + "' services from file + '" + file + "'");
	    }
	}        

    }

    public String getCommandString() {
	return "EXECUTE_JAVA_COMMAND;org.blue.commands.add_service;%s;%s;%s";
    }

    public String getCommandName() {
	return "EXECUTE_JAVA_COMMAND";
    }
}