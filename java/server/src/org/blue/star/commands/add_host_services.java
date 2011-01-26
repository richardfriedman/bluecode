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
 * This command is used to add a host and all services within the same cfg file.
 * 
 * @extended 16/07/07 - Command tidied slightly and now provides more information to user.
 * 
 * @author - Richard.Friedman
 * @author - Rob.Blake@arjuna.com
 * 
 * @version 0.2
 * 
 */
public class add_host_services implements ICommand {

    private static Logger logger = LogManager.getLogger("org.blue.commands");

    public String getCommandName() {
	return "EXECUTE_JAVA_COMMAND";
    }

    public String getCommandString() {
	return "EXECUTE_JAVA_COMMAND;org.blue.commands.add_host_services;%s;%s;%s" ;
    }

    public void processCommand(long timestamp, String args) {
	if ( args == null ) {
	    logger.info( "No parameters passed to command. Expecting : " + getCommandString() );
	    return;
	}

	String[] split = args.split(";");
	
	if( split.length != 3)
	{
	    logger.info( "Incorrect command parameters. Expecting : " + getCommandString() );
	    return;
	}

	String file = split[0].trim();
	String host = split[1].trim();
	String serviceName = split[2].trim();
	int priorCount =0;
	
	if(serviceName.equalsIgnoreCase("all"))
	{
		priorCount = objects.service_list.size();
	}
	
	if ( xodtemplate.xodtemplate_read_dynamic_data( blue.config_file, file, common_h.READ_HOSTS | common_h.READ_SERVICES, common_h.TRUE ) == common_h.OK )
	{

	    objects_h.host temp_host= objects.find_host( host );

	    if(temp_host!=null)
	    {
	    	commands.enable_host_checks(temp_host);
	    	statusdata.update_host_status( temp_host, common_h.FALSE );
	    }
	    else
	    {
	    	logger.info("ERROR: Failed to Enable Checks on Added Host '" + host + "'");
	    }

	    for ( int x = 2;  x < split.length; x++ )
	    {
	    	String service = split[x].trim();
	    	
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
	    	   		logger.info( "ERROR: Failed to Enable Checks on Service '" + service +"' on Host '" + host + "'");
	    	   	}
	    	}
	    	else
	    	{
	    		logger.info("INFO: Added '" + (objects.service_list.size() - priorCount) + "' services to Host '" + host + "'");
	    	}
	    }
	}
	else
	{
	    logger.info("ERROR: Failed to add host '" + host + "'");
	}
 }
}