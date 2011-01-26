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

package org.blue.star.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.objects_h.hostgroupmember;
import org.blue.star.include.objects_h.servicegroupmember;
import org.blue.star.xdata.xodtemplate;

public class objects
{
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.common.objects");
    private static String cn = "org.blue.common.objects";

    public static ArrayList<objects_h.host> host_list = new ArrayList<objects_h.host>(); 
    public static ArrayList<objects_h.service> service_list = new ArrayList<objects_h.service>();
    public static ArrayList<objects_h.contact> contact_list = new ArrayList<objects_h.contact>();
    public static ArrayList<objects_h.contactgroup> contactgroup_list = new ArrayList<objects_h.contactgroup>();
    public static ArrayList<objects_h.hostgroup> hostgroup_list = new ArrayList<objects_h.hostgroup>();
    public static ArrayList<objects_h.servicegroup> servicegroup_list = new ArrayList<objects_h.servicegroup>();
    public static ArrayList<objects_h.command> command_list = new ArrayList<objects_h.command>();
    public static ArrayList<objects_h.timeperiod> timeperiod_list = new ArrayList<objects_h.timeperiod>();
    public static ArrayList<objects_h.serviceescalation> serviceescalation_list = new ArrayList<objects_h.serviceescalation>();
    public static ArrayList<objects_h.servicedependency> servicedependency_list = new ArrayList<objects_h.servicedependency>();
    public static ArrayList<objects_h.hostdependency> hostdependency_list = new ArrayList<objects_h.hostdependency>();
    public static ArrayList<objects_h.hostescalation> hostescalation_list = new ArrayList<objects_h.hostescalation>();
    public static ArrayList<objects_h.hostextinfo> hostextinfo_list = new ArrayList<objects_h.hostextinfo>();
    public static ArrayList<objects_h.serviceextinfo> serviceextinfo_list = new ArrayList<objects_h.serviceextinfo>();

    public static HashMap<String,objects_h.host> host_hashlist = new HashMap<String,objects_h.host>();
    public static HashMap<String,objects_h.service> service_hashlist = new HashMap<String,objects_h.service>();
    public static HashMap<String,objects_h.command> command_hashlist = new HashMap<String,objects_h.command>();
    public static HashMap<String,objects_h.timeperiod> timeperiod_hashlist = new HashMap<String,objects_h.timeperiod>();
    public static HashMap<String,objects_h.contact> contact_hashlist = new HashMap<String,objects_h.contact>();
    public static HashMap<String,objects_h.contactgroup> contactgroup_hashlist = new HashMap<String,objects_h.contactgroup>();
    public static HashMap<String,objects_h.hostgroup> hostgroup_hashlist = new HashMap<String,objects_h.hostgroup>();
    public static HashMap<String,objects_h.servicegroup> servicegroup_hashlist = new HashMap<String,objects_h.servicegroup>();
    public static HashMap<String,objects_h.hostextinfo> hostextinfo_hashlist = new HashMap<String,objects_h.hostextinfo>();
    public static HashMap<String,objects_h.serviceextinfo> serviceextinfo_hashlist = new HashMap<String,objects_h.serviceextinfo>();

    /* These maps have Object as one of their permitted types as they can contain lists of escalations/dependencies */
    public static HashMap<String,Object> hostdependency_hashlist = new HashMap<String,Object>();
    public static HashMap<String,Object> servicedependency_hashlist = new HashMap<String,Object>();
    public static HashMap<String,Object> hostescalation_hashlist = new HashMap<String,Object>();
    public static HashMap<String,Object> serviceescalation_hashlist = new HashMap<String,Object>();

/******************************************************************/
/******* TOP-LEVEL HOST CONFIGURATION DATA INPUT FUNCTION *********/
/******************************************************************/


/* read all host configuration data from external source */
public static int read_object_config_data(String main_config_file,int options,int cache)
{
	int result=common_h.OK;
	
	logger.trace( "entering " + cn + ".read_object_config_data" );

	/* read in data from all text host config files (template-based) */
	result = xodtemplate.xodtemplate_read_config_data(main_config_file,options,cache);
	
	if(result!= common_h.OK)
		return common_h.ERROR;


	logger.trace( "exiting " + cn + ".read_object_config_data" );
	return result;
}



/******************************************************************/
/****************** CHAINED HASH FUNCTIONS ************************/
/******************************************************************/
    
/* adds host to hash list in memory */
public static boolean add_host_to_hashlist(objects_h.host new_object ){

    if ( new_object == null ) { 
        return false;
    } else if (host_hashlist.containsKey( new_object.name ) ) {
        logger.fatal( "Error: Could not add duplicate host '"+new_object.name+"'" );
        return false;
    } else {
        host_hashlist.put( new_object.name, new_object );
        return true;
    }
    
}

/* Removes a host from the host hash_list */
public static boolean remove_host(String hostname)
{
	if(hostname == null)
		return false;
	
	/* Find the host object that we are supposedly removing */
	objects_h.host host = find_host(hostname);
	
	if(host == null)
		return false;
	
	/* Remove all of the services associated with this host */
	boolean result = remove_host_services(host);
			
	/* Remove our host from the host_hashlist */
	if(result)
		result = remove_host_from_hashlist(host.name);
	
	if(result)
		result = remove_host_from_host_list(host.name);
	
	return result;
}

/* Remove a service from all object lists */
public static boolean remove_service(String hostname,String serviceDescription)
{
	if(hostname == null || serviceDescription == null)
		return false;
	
	logger.info("Removing Service :" + hostname + ":" + serviceDescription);
	
	/* Find the Service that we are removing */
	objects_h.service service = find_service(hostname,serviceDescription);
	
	if(service == null)
		return false;
	
	
	/* Remove service from all associated lists */
	boolean result = remove_service_from_service_list(hostname,serviceDescription);
	
	if(result)
		result = remove_service_from_service_hashlist(hostname,serviceDescription);
	
	/* Remove service from any service groups */
	if(result)
		result = remove_service_from_service_group_list(hostname,serviceDescription);
	
	if(result)
		result = remove_service_from_service_group_hashlist(hostname,serviceDescription);
	
	return result;
}

/* Remove a host from the hashlist */
public static boolean remove_host_from_hashlist(String hostname)
{
	if(hostname == null)
		return false;
	
	if(!host_hashlist.containsKey(hostname))
		return false;
	
	host_hashlist.remove(hostname);
	
	return true;
}

/* Remove the host from the host list */
public static boolean remove_host_from_host_list(String hostname)
{
	if(hostname == null)
		return false;
	
	for(ListIterator<objects_h.host> i = host_list.listIterator(); i.hasNext();)
	{
		objects_h.host host = i.next();
		
		if(host.name.equals(hostname))
		{
			host_list.remove(host);
			return true;
		}
		
	}
	
	return false;
}

/* Remove all services for a specific host */
public static boolean remove_host_services(objects_h.host host)
{
	if(host == null)
	{
		return false;
	}
	
	int beforeCount = service_list.size();
	int removeCount = 0;
	
	/* Take a copy of the current service list to remove everything */
	ArrayList<objects_h.service> currentServiceList = new ArrayList<objects_h.service>();
	currentServiceList.addAll(service_list);
	
	for(objects_h.service s : currentServiceList)
	{
		if(s.host_name.equals(host.name))
		{
			if(remove_service(s.host_name,s.description))
			{
				removeCount++;
			}
		}
	}
	
	/* Verify that we have removed as many services that are attached to this host */
	return service_list.size() == (beforeCount - removeCount);
}

public static int add_service_to_hashlist( objects_h.service new_service){
    if ( new_service == null ) { 
        return 0;
    } else if (service_hashlist.containsKey( new_service.host_name + "." + new_service.description ) ) {
        logger.fatal( "Error: Could not add duplicate service '"+new_service.description +"' on host '" + new_service.host_name + "'."  );
        return 0;
    } else {
        service_hashlist.put( new_service.host_name + "." + new_service.description , new_service );
        return 1;
    }
}

/* Remove a service from the service list */
public static boolean remove_service_from_service_list(String host_name,String service_description)
{
	for(ListIterator<objects_h.service> i = service_list.listIterator();i.hasNext();)
	{
		objects_h.service service = i.next();
			
		if(service.description.equals(service_description) && service.host_name.equals(host_name))
		{
			service_list.remove(service);
			return true;
		}
			
	}
	
	return true;
}

/* Remove a service frm the service hashlist */
public static boolean remove_service_from_service_hashlist(String host_name,String service_description)
{
	if(host_name == null || service_description == null)
		return false;
	
	if(!service_hashlist.containsKey(host_name + "." + service_description))
		return false;
	
	service_hashlist.remove(host_name + "." + service_description);
	return true;
}

/* Removes a service from all service groups */
public static boolean remove_service_from_service_groups(String hostname,String serviceDescription)
{
	if(hostname == null || serviceDescription == null)
		return false;
	
	if(!remove_service_from_service_group_list(hostname,serviceDescription))
		return false;
	
	if(!remove_service_from_service_group_hashlist(hostname,serviceDescription))
		return false;
	
	return true;
}

/* Removes a service from the service group list */
public static boolean remove_service_from_service_group_list(String hostname,String serviceDescription)
{
	for(ListIterator<objects_h.servicegroup> i = servicegroup_list.listIterator();i.hasNext();)
	{
			objects_h.servicegroup servicegroup = i.next();
			
			for(objects_h.servicegroupmember s: servicegroup.members)
			{
				if(s.host_name.equals(hostname) && s.service_description.equals(serviceDescription))
				{
					servicegroup.members.remove(s);
				}
			}
	}
	
	return true;
}

/* Removes a service from a service group within the service group hashlist */
public static boolean remove_service_from_service_group_hashlist(String hostname,String serviceDescription)
{
	if(hostname == null || serviceDescription == null)
		return false;
	
	for(Iterator<String> i = servicegroup_hashlist.keySet().iterator();i.hasNext();)
	{
		objects_h.servicegroup servicegroup = servicegroup_hashlist.get(i.next());
		
		for(objects_h.servicegroupmember s: servicegroup.members)
		{
			if(s.host_name.equals(hostname) && s.service_description.equals(serviceDescription))
			{
				servicegroup.members.remove(s);
			}
		}
	}
	
	return true;
}

/* Removes a host from all hostgroups */
public static boolean remove_host_from_hostgroups(String hostname)
{
	if(hostname == null)
		return false;
	
	if(!remove_host_from_hostgroup_list(hostname))
		return false;
	
	if(!remove_host_from_hostgroup_hashlist(hostname))
		return false;
	
	return true;
}

/* Removes a host from all host groups with the host group list */
public static boolean remove_host_from_hostgroup_list(String hostname)
{
	if(hostname == null)
		return false;
	
	for(ListIterator<objects_h.hostgroup> i = hostgroup_list.listIterator();i.hasNext();)
	{
		objects_h.hostgroup hostgroup = i.next();
		
		for(objects_h.hostgroupmember h: hostgroup.members)
		{
			if(h.host_name.equals(hostname))
			{
				hostgroup.members.remove(h);
			}
		}
	}

	return true;
}

/* Removes a host from the host group hashlist */
public static boolean remove_host_from_hostgroup_hashlist(String hostname)
{
	if(hostname == null)
		return false;
	
	for(Iterator<String> i = hostgroup_hashlist.keySet().iterator();i.hasNext();)
	{
		objects_h.hostgroup hostgroup = hostgroup_hashlist.get(i.next());
		
		for(objects_h.hostgroupmember h: hostgroup.members)
		{
			if(h.host_name.equals(hostname))
			{
				hostgroup.members.remove(h);
			}
		}
	}
	
	return true;
}


public static boolean add_command_to_hashlist(objects_h.command new_command){

    if ( new_command == null ) { 
        return false;
    } else if (command_hashlist.containsKey( new_command.name ) ) {
        logger.fatal( "Error: Could not add duplicate command '"+new_command.name + "'."  );
        return false;
    } else {
        command_hashlist.put( new_command.name, new_command );
        return true;
    }
}

public static boolean add_timeperiod_to_hashlist(objects_h.timeperiod new_timeperiod){

    if ( new_timeperiod == null ) { 
        return false;
    } else if (timeperiod_hashlist.containsKey( new_timeperiod.name ) ) {
        logger.fatal( "Error: Could not add duplicate timeperiod '"+new_timeperiod.name + "'."  );
        return false;
    } else {
        timeperiod_hashlist.put( new_timeperiod.name, new_timeperiod );
        return true;
    }
}


public static int add_contact_to_hashlist(objects_h.contact new_contact){

    if ( new_contact == null ) { 
        return 0;
    } else if (contact_hashlist.containsKey( new_contact.name ) ) {
        logger.fatal( "Error: Could not add duplicate contact '"+new_contact.name + "'."  );
        return 0;
    } else {
        contact_hashlist.put( new_contact.name, new_contact );
        return 1;
    }
}

public static int add_contactgroup_to_hashlist(objects_h.contactgroup new_contactgroup){

    if ( new_contactgroup == null ) { 
        return 0;
    } else if (contactgroup_hashlist.containsKey( new_contactgroup.group_name ) ) {
        logger.fatal( "Error: Could not add duplicate contactgroup '"+new_contactgroup.group_name + "'."  );
        return 0;
    } else {
        contactgroup_hashlist.put( new_contactgroup.group_name, new_contactgroup );
        return 1;
    }
}

public static boolean add_hostgroup_to_hashlist(objects_h.hostgroup new_hostgroup){
 
    if ( new_hostgroup == null ) { 
        return false;
    } else if (hostgroup_hashlist.containsKey( new_hostgroup.group_name ) ) {
        logger.fatal( "Error: Could not add duplicate hostgroup '"+new_hostgroup.group_name + "'."  );
        return false;
    } else {
        hostgroup_hashlist.put( new_hostgroup.group_name, new_hostgroup );
        return true;
    }
}

public static boolean add_servicegroup_to_hashlist( objects_h.servicegroup new_servicegroup ) {

    if ( new_servicegroup == null ) { 
        return false;
    } else if (servicegroup_hashlist.containsKey( new_servicegroup.group_name ) ) {
        logger.fatal( "Error: Could not add duplicate servicegroup '"+new_servicegroup.group_name + "'."  );
        return false;
    } else {
        servicegroup_hashlist.put( new_servicegroup.group_name, new_servicegroup );
        return true;
    }

}

public static int add_hostextinfo_to_hashlist( objects_h.hostextinfo new_hostextinfo){

    if ( new_hostextinfo == null ) { 
        return 0;
    } else if (hostextinfo_hashlist.containsKey( new_hostextinfo.host_name ) ) {
        logger.fatal( "Error: Could not add duplicate hostgroup for host'"+new_hostextinfo.host_name + "'."  );
        return 0;
    } else {
        hostextinfo_hashlist.put( new_hostextinfo.host_name, new_hostextinfo );
        return 1;
    }
}

public static int add_serviceextinfo_to_hashlist(objects_h.serviceextinfo new_serviceextinfo){

    if ( new_serviceextinfo == null ) { 
        return 0;
    } else if (serviceextinfo_hashlist.containsKey( new_serviceextinfo.host_name + "." + new_serviceextinfo.description ) ) {
        logger.fatal( "Error: Could not add duplicate hostgroup for service '"+new_serviceextinfo.description+"' on host'"+new_serviceextinfo.host_name + "'."  );
        return 0;
    } else {
        serviceextinfo_hashlist.put( new_serviceextinfo.host_name + "." + new_serviceextinfo.description, new_serviceextinfo );
        return 1;
    }
}

/* adds hostdependency to hash list in memory */
public static int add_hostdependency_to_hashlist(objects_h.hostdependency new_hostdependency){

    if( new_hostdependency == null )
    { 
        return 0;
    }
    else if(hostdependency_hashlist.containsKey(new_hostdependency.dependent_host_name))
    {
        Object o = hostdependency_hashlist.get( new_hostdependency.dependent_host_name );
        if ( o instanceof ArrayList ) 
            ((ArrayList) o).add( new_hostdependency );
        else
        {
            ArrayList list = new ArrayList();
            list.add( o );
            list.add( new_hostdependency );
            hostdependency_hashlist.put( new_hostdependency.dependent_host_name, list );
        }
        return 0;
    }
    else
    {
        hostdependency_hashlist.put( new_hostdependency.dependent_host_name, new_hostdependency );
        return 1;
    }
}


public static int add_servicedependency_to_hashlist(objects_h.servicedependency new_servicedependency){
    
    if ( new_servicedependency == null ) { 
        return 0;
    } else { 
        String key = new_servicedependency.dependent_host_name + "." + new_servicedependency.dependent_service_description;
        if (servicedependency_hashlist.containsKey( key ) ) {
            Object o = servicedependency_hashlist.get( key );
            if ( o instanceof ArrayList ) 
                ((ArrayList) o).add( new_servicedependency );
            else {
                ArrayList list = new ArrayList();
                list.add( o );
                list.add( new_servicedependency );
                servicedependency_hashlist.put( key, list );
            }
            return 0;
        } else {
            servicedependency_hashlist.put( key, new_servicedependency );
            return 1;
        }
    }
}

/* adds hostescalation to hash list in memory */
public static int add_hostescalation_to_hashlist(objects_h.hostescalation new_hostescalation){

    if ( new_hostescalation == null ) { 
        return 0;
    } else if (hostescalation_hashlist.containsKey( new_hostescalation.host_name ) ) {
        Object o = hostescalation_hashlist.get( new_hostescalation.host_name );
        if ( o instanceof ArrayList ) 
            ((ArrayList) o).add( new_hostescalation );
        else {
            ArrayList list = new ArrayList();
            list.add( o );
            list.add( new_hostescalation );
            hostescalation_hashlist.put( new_hostescalation.host_name, list );
        }
        return 1;
    } else {
        hostescalation_hashlist.put( new_hostescalation.host_name, new_hostescalation );
        return 1;
    }
}

public static int add_serviceescalation_to_hashlist(objects_h.serviceescalation new_serviceescalation){
    
    if ( new_serviceescalation == null ) { 
        return 0;
    } else { 
        String key = new_serviceescalation.host_name + "." + new_serviceescalation.description;
        if (serviceescalation_hashlist.containsKey( key ) ) {
            Object o = serviceescalation_hashlist.get( key );
            if ( o instanceof ArrayList ) 
                ((ArrayList) o).add( new_serviceescalation );
            else {                ArrayList list = new ArrayList();
                list.add( o );
                list.add( new_serviceescalation );
                serviceescalation_hashlist.put( key, list );
            }
            return 0;
        } else {
            serviceescalation_hashlist.put( key, new_serviceescalation );
            return 1;
        }
    }
}

/******************************************************************/
/**************** OBJECT ADDITION FUNCTIONS ***********************/
/******************************************************************/

/* add a new timeperiod to the list in memory */
public static objects_h.timeperiod add_timeperiod(String name, String alias){
    logger.trace( "entering " + cn + ".add_timeperiod()" );
    
    /* make sure we have the data we need */
    if(name==null || alias==null || name.trim().length() == 0 || alias.trim().length() == 0 ) {
        logger.fatal( "Error: Name or alias for timeperiod is null" );
        return null;
    }
    
    /* make sure there isn't a timeperiod by this name added already */
    if(find_timeperiod(name)!=null){
        logger.fatal( "Error: Timeperiod '"+name+"' has already been defined\n" );
        return null;
    }
    
    objects_h.timeperiod new_timeperiod = new objects_h.timeperiod(); 
    new_timeperiod.name = name;
    new_timeperiod.alias = alias;
    
    /* add new timeperiod to timeperiod chained hash list */
    if(!add_timeperiod_to_hashlist(new_timeperiod)){
        logger.fatal("Error: Could not allocate memory for timeperiod list to add timeperiod '"+name+"'\n");
        return null;
    }
    
    /* timeperiods are sorted alphabetically for daemon, so add new items to tail of list */
    timeperiod_list.add(new_timeperiod );
    
    logger.debug( "Name:  " + new_timeperiod.name );
    logger.debug( "Alias: " + new_timeperiod.alias );
    
    logger.trace( "exiting " + cn + ".add_timeperiod()" );
    
    return new_timeperiod;
}

/* add a new timerange to a timeperiod */
public static objects_h.timerange add_timerange_to_timeperiod(objects_h.timeperiod period, int day, long start_time, long end_time){
    logger.trace( "entering " + cn + ".add_timerange_to_timeperiod");
    
    /* make sure we have the data we need */
    if(period==null)
        return null;
    
    if(day<0 || day>6){
        logger.fatal( "Error: Day "+day+" is not valid for timeperiod '"+period.name+"'" );
        return null;
    }
    if(start_time<0 || start_time>86400){
        logger.fatal( "Error: Start time "+start_time+" on day "+day+" is not valid for timeperiod '"+period.name+"'");
        return null;
    }
    if(end_time<0 || end_time>86400){
        logger.fatal( "Error: End time "+end_time+" on day "+day+" is not value for timeperiod '"+period.name+"'");
        return null;
    }
    
    /* allocate memory for the new time range */
    objects_h.timerange new_timerange = new objects_h.timerange();
    new_timerange.range_start=start_time;
    new_timerange.range_end=end_time;
    
    /* add the new time range to the head of the range list for this day */
    if ( period.days[day] == null ) 
       period.days[day] = new ArrayList<objects_h.timerange>();
    period.days[day].add( new_timerange );
    
    logger.trace( "exiting " + cn + ".add_timerange_to_timeperiod");
    return new_timerange;
}

/* add a new host definition */
public static objects_h.host add_host(String name, String alias, String address, String check_period, int check_interval, int max_attempts, int notify_up, int notify_down, int notify_unreachable, int notify_flapping, int notification_interval, String notification_period, int notifications_enabled, String check_command, int checks_enabled, int accept_passive_checks, String event_handler, int event_handler_enabled, int flap_detection_enabled, double low_flap_threshold, double high_flap_threshold, int stalk_up, int stalk_down, int stalk_unreachable, int process_perfdata, int failure_prediction_enabled, String failure_prediction_options, int check_freshness, int freshness_threshold, int retain_status_information, int retain_nonstatus_information, int obsess_over_host){

	logger.trace( "entering " + cn + ".add_host");

	/* make sure we have the data we need */
	if( name==null || alias==null || address==null ){
	    logger.fatal("Error: Host name, alias, or address is null\n");
	    return null;
	}
	
    name = name.trim();
	alias = alias.trim();
	address = address.trim();
	if ( check_command != null ) check_command = check_command.trim();
	if (event_handler != null ) event_handler = event_handler.trim();
	if (notification_period != null) notification_period = notification_period.trim();

	if ( name.length() == 0 || alias.length() == 0 || address.length() == 0 ) {
	    logger.fatal( "Error: Host name, alias, or address is null\n");
	    return null;
	}
	
	/* make sure there isn't a host by this name already added */
	if(find_host(name)!=null){
	    logger.fatal( "Error: Host '"+name+"' has already been defined");
	    return null;
	}
	
	/* make sure the name isn't too long */
	if(name.length()>(objects_h.MAX_HOSTNAME_LENGTH-1)){
	    logger.fatal( "Error: Host name '"+name+"' exceeds maximum length of "+(objects_h.MAX_HOSTNAME_LENGTH-1)+" characters");
	    return null;
	}
	
	if(max_attempts<=0){
	    logger.fatal( "Error: Invalid max_check_attempts value for host '"+name+"'");
	    return null;
	}
	
	if(check_interval<0){
	    logger.fatal( "Error: Invalid check_interval value for host '"+name+"'");
	    return null;
	}
	
	if(notification_interval<0){
	    logger.fatal( "Error: Invalid notification_interval value for host '"+name+"'");
	    return null;
	}
	
	if(notify_up<0 || notify_up>1){
	    logger.fatal("Error: Invalid notify_up value for host '" + name + "'" );
	    return null;
	}
	if(notify_down<0 || notify_down>1){
	    logger.fatal("Error: Invalid notify_down value for host '" + name + "'" );
	    return null;
	}
	if(notify_unreachable<0 || notify_unreachable>1){
	    logger.fatal("Error: Invalid notify_unreachable value for host '" + name + "'" );
	    return null;
	}
	if(notify_flapping<0 || notify_flapping>1){
	    logger.fatal("Error: Invalid notify_flappingvalue for host '" + name + "'" );
	    return null;
	}
	if(checks_enabled<0 || checks_enabled>1){
	    logger.fatal("Error: Invalid checks_enabled value for host '" + name + "'" );
	    return null;
	}
	if(accept_passive_checks<0 || accept_passive_checks>1){
	    logger.fatal("Error: Invalid accept_passive_checks value for host '" + name + "'" );
	    return null;
	}
	if(notifications_enabled<0 || notifications_enabled>1){
	    logger.fatal("Error: Invalid notifications_enabled value for host '" + name + "'" );
	    return null;
	}
	if(event_handler_enabled<0 || event_handler_enabled>1){
	    logger.fatal("Error: Invalid event_handler_enabled value for host '" + name + "'" );
	    return null;
	}
	if(flap_detection_enabled<0 || flap_detection_enabled>1){
	    logger.fatal("Error: Invalid flap_detection_enabled value for host '" + name + "'" );
	    return null;
	}
	if(stalk_up<0 || stalk_up>1){
	    logger.fatal("Error: Invalid stalk_up value for host '" + name + "'" );
	    return null;
	}
	if(stalk_down<0 || stalk_down>1){
	    logger.fatal("Error: Invalid stalk_warning value for host '" + name + "'" );
	    return null;
	}
	if(stalk_unreachable<0 || stalk_unreachable>1){
	    logger.fatal("Error: Invalid stalk_unknown value for host '" + name + "'" );
	    return null;
	}
	if(process_perfdata<0 || process_perfdata>1){
	    logger.fatal("Error: Invalid process_perfdata value for host '" + name + "'" );
	    return null;
	}
	if(failure_prediction_enabled<0 || failure_prediction_enabled>1){
	    logger.fatal("Error: Invalid failure_prediction_enabled value for host '" + name + "'" );
	    return null;
	}
	if(check_freshness<0 || check_freshness>1){
	    logger.fatal("Error: Invalid check_freshness value for host '" + name + "'" );
	    return null;
	}
	if(freshness_threshold<0){
	    logger.fatal("Error: Invalid freshness_threshold value for host '" + name + "'" );
	    return null;
	}
	if(obsess_over_host<0 || obsess_over_host>1){
	    logger.fatal("Error: Invalid obsess_over_host value for host '" + name + "'" );
	    return null;
	}
	if(retain_status_information<0 || retain_status_information>1){
	    logger.fatal("Error: Invalid retain_status_information value for host '" + name + "'" );
	    return null;
	}
	if(retain_nonstatus_information<0 || retain_nonstatus_information>1){
	    logger.fatal("Error: Invalid retain_nonstatus_information value for host '" + name + "'" );
	    return null;
	}

	/* allocate memory for a new host */
	objects_h.host new_host = new objects_h.host();
	new_host.name=name;
	new_host.alias=alias;
	new_host.address=address;
	new_host.check_period=check_period;
	new_host.notification_period=notification_period;
	new_host.host_check_command=check_command;
	new_host.event_handler=event_handler;
	new_host.failure_prediction_options=failure_prediction_options;

	// new_host.parent_hosts=null;
	new_host.max_attempts=max_attempts;
	new_host.contact_groups=null;
	new_host.check_interval=check_interval;
	new_host.notification_interval=notification_interval;
	new_host.notify_on_recovery=(notify_up>0)?common_h.TRUE:common_h.FALSE;
	new_host.notify_on_down=(notify_down>0)?common_h.TRUE:common_h.FALSE;
	new_host.notify_on_unreachable=(notify_unreachable>0)?common_h.TRUE:common_h.FALSE;
	new_host.notify_on_flapping=(notify_flapping>0)?common_h.TRUE:common_h.FALSE;
	new_host.flap_detection_enabled=(flap_detection_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_host.low_flap_threshold=low_flap_threshold;
	new_host.high_flap_threshold=high_flap_threshold;
	new_host.stalk_on_up=(stalk_up>0)?common_h.TRUE:common_h.FALSE;
	new_host.stalk_on_down=(stalk_down>0)?common_h.TRUE:common_h.FALSE;
	new_host.stalk_on_unreachable=(stalk_unreachable>0)?common_h.TRUE:common_h.FALSE;
	new_host.process_performance_data=(process_perfdata>0)?common_h.TRUE:common_h.FALSE;
	new_host.check_freshness=(check_freshness>0)?common_h.TRUE:common_h.FALSE;
	new_host.freshness_threshold=freshness_threshold;
	new_host.accept_passive_host_checks=(accept_passive_checks>0)?common_h.TRUE:common_h.FALSE;
	new_host.event_handler_enabled=(event_handler_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_host.failure_prediction_enabled=(failure_prediction_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_host.obsess_over_host=(obsess_over_host>0)?common_h.TRUE:common_h.FALSE;
	new_host.retain_status_information=(retain_status_information>0)?common_h.TRUE:common_h.FALSE;
	new_host.retain_nonstatus_information=(retain_nonstatus_information>0)?common_h.TRUE:common_h.FALSE;
	new_host.current_state=blue_h.HOST_UP;
	new_host.last_state=blue_h.HOST_UP;
	new_host.last_hard_state=blue_h.HOST_UP;
	new_host.check_type=common_h.HOST_CHECK_ACTIVE;
	new_host.last_host_notification=0L;
	new_host.next_host_notification=0L;
	new_host.next_check=0L;
	new_host.should_be_scheduled=common_h.TRUE;
	new_host.last_check=0L;
	new_host.current_attempt=1;
	new_host.state_type=common_h.HARD_STATE;
	new_host.execution_time=0.0;
	new_host.latency=0.0;
	new_host.last_state_change=0L;
	new_host.last_hard_state_change=0L;
	new_host.last_time_up=0L;
	new_host.last_time_down=0L;
	new_host.last_time_unreachable=0L;
	new_host.has_been_checked=common_h.FALSE;
	new_host.is_being_freshened=common_h.FALSE;
	new_host.problem_has_been_acknowledged=common_h.FALSE;
	new_host.acknowledgement_type=common_h.ACKNOWLEDGEMENT_NONE;
	new_host.notified_on_down=common_h.FALSE;
	new_host.notified_on_unreachable=common_h.FALSE;
	new_host.current_notification_number=0;
	new_host.no_more_notifications=common_h.FALSE;
	new_host.check_flapping_recovery_notification=common_h.FALSE;
	new_host.checks_enabled=(checks_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_host.notifications_enabled=(notifications_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_host.scheduled_downtime_depth=0;
	new_host.check_options=blue_h.CHECK_OPTION_NONE;
	new_host.pending_flex_downtime=0;
	for(int x=0;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++)
		new_host.state_history[x]=blue_h.STATE_OK;
	new_host.state_history_index=0;
	new_host.last_state_history_update=0L;
	new_host.is_flapping=common_h.FALSE;
	new_host.flapping_comment_id=0;
	new_host.percent_state_change=0.0;
	new_host.total_services=0;
	new_host.total_service_check_interval=0L;
	new_host.modified_attributes=common_h.MODATTR_NONE;
    new_host.circular_path_checked=common_h.FALSE; /* UPDATED 2.2 */
    new_host.contains_circular_path=common_h.FALSE; /* UPDATED 2.2 */
    new_host.plugin_output = "";
    new_host.perf_data = "";

	/* add new host to host chained hash list */
	if(!add_host_to_hashlist(new_host)){
	    logger.fatal( "Error: Could not allocate memory for host list to add host '"+name+"'");
	    return null;
	}

    host_list.add( new_host );

    logger.debug("\tHost Name:                " + new_host.name);
    logger.debug("\tHost Alias:               " + new_host.alias);
    logger.debug("\tHost Address:             " + new_host.address);
    logger.debug("\tHost Check Command:       " + new_host.host_check_command);
    logger.debug("\tMax. Check Attempts:      " + new_host.max_attempts);
    logger.debug("\tHost Event Handler:       " + ((new_host.event_handler==null)?"N/A":new_host.event_handler));
    logger.debug("\tNotify On Down:           " + ((new_host.notify_on_down==1)?"yes":"no"));
    logger.debug("\tNotify On Unreachable:    " + ((new_host.notify_on_unreachable==1)?"yes":"no"));
    logger.debug("\tNotify On Recovery:       " + ((new_host.notify_on_recovery==1)?"yes":"no"));
    logger.debug("\tNotification Interval:    " + new_host.notification_interval);
	logger.debug("\tNotification Time Period: " + ((new_host.notification_period==null)?"N/A":new_host.notification_period));

	logger.trace( "exiting " + cn + ".add_host");

	return new_host;
	}

public static objects_h.hostsmember add_parent_host_to_host(objects_h.host hst,String host_name){
    logger.trace( "entering " + cn + ".add_parent_host_to_host");
    
    /* make sure we have the data we need */
    if(hst== null|| host_name== null || host_name.length() == 0 ){
        logger.fatal("Error: Host is null or parent host name is null\n");
        return null;
    }
    
    host_name = host_name.trim();
    
    /* a host cannot be a parent/child of itself */
    if(host_name.equals(hst.name)){
        logger.fatal( "Error: Host '"+hst.name+"' cannot be a child/parent of itself");
        return null;
    }
    
    /* allocate memory */
    objects_h.hostsmember new_hostmember = new objects_h.hostsmember();
    new_hostmember.host_name=host_name;
    
    /* add the parent host entry to the host definition */
    if ( hst.parent_hosts == null )
       hst.parent_hosts = new ArrayList<objects_h.hostsmember>();
       
    hst.parent_hosts.add(new_hostmember);
    
    logger.trace( "exiting " + cn + ".add_parent_host_to_host");
    return new_hostmember;
}

/* add a new contactgroup to a host */
public static objects_h.contactgroupsmember add_contactgroup_to_host(objects_h.host hst, String group_name){
    logger.trace( "entering " + cn + ".add_contactgroup_to_host");
    
    /* make sure we have the data we need */
    if(hst==null || group_name==null ){
        logger.fatal( "Error: Host or contactgroup member is null");
        return null;
    }
    
    group_name = group_name.trim();
    
    if( group_name.length() == 0 ){
        logger.fatal( "Error: Host '"+hst.name+"' contactgroup member is null" );
        return null;
    }
    
    /* allocate memory for a new member */
    objects_h.contactgroupsmember new_contactgroupsmember = new objects_h.contactgroupsmember ();
    new_contactgroupsmember.group_name=group_name;
    
    /* add the new member to the head of the member list */
    if ( hst.contact_groups == null ) hst.contact_groups = new ArrayList<objects_h.contactgroupsmember>(); 
    hst.contact_groups.add( new_contactgroupsmember );
    
    logger.trace( "exiting " + cn + ".add_host_to_host");
    return new_contactgroupsmember;
}

/* add a new host group to the list in memory */
public static objects_h.hostgroup add_hostgroup(String name, String alias){
	logger.trace( "entering " + cn + ".add_hostgroup");
	
	/* make sure we have the data we need */
	if(name== null || alias== null ){
	    logger.fatal( "Error: Hostgroup name and/or alias is null\n");
	    return null;
	}
	
	name = name.trim();
	alias = alias.trim();
	
	if( name.length() == 0  ||  alias.length() == 0 ){
	    logger.fatal( "Error: Hostgroup name and/or alias is null\n");
	    return null;
	}
	
	/* make sure a hostgroup by this name hasn't been added already */
	if(find_hostgroup(name)!=null){
	    logger.fatal( "Error: Hostgroup '"+name+"' has already been defined");
	    return null;
	}
	
	/* allocate memory */
	objects_h.hostgroup new_hostgroup= new objects_h.hostgroup();
	new_hostgroup.group_name=name;
	new_hostgroup.alias=alias;
	new_hostgroup.members=null;
	
	/* add new hostgroup to hostgroup chained hash list */
	if(!add_hostgroup_to_hashlist(new_hostgroup)){
	    logger.fatal( "Error: Could not allocate memory for hostgroup list to add hostgroup '"+name+"'"  );
	    return null;
	}
	
	hostgroup_list.add( new_hostgroup );
	
	logger.debug("\tGroup name:     " + new_hostgroup.group_name);
	logger.debug("\tAlias:          " + new_hostgroup.alias);
	logger.trace( "exiting " + cn + ".add_hostgroup");
	return new_hostgroup;
}

/* add a new host to a host group */
public static objects_h.hostgroupmember add_host_to_hostgroup(objects_h.hostgroup temp_hostgroup, String host_name){
    logger.trace( "entering " + cn + ".add_host_to_hostgroup");
    
    /* make sure we have the data we need */
    if(temp_hostgroup==null || host_name==null ){
        logger.fatal( "Error: Hostgroup or group member is null\n");
        return null;
    }
    
    host_name = host_name.trim();
    
    if( host_name.length() == 0 ) {
        logger.fatal( "Error: Hostgroup member is null\n");
        return null;
    }
    
    /* allocate memory for a new member */
    objects_h.hostgroupmember new_member = new objects_h.hostgroupmember();
    new_member.host_name=host_name;
    
    
    /* add the new member to the member list, sorted by host name */
    // todo add in sorted manner
    
    if ( temp_hostgroup.members == null )
        temp_hostgroup.members = new ArrayList<hostgroupmember>();
    temp_hostgroup.members.add( new_member );
    
    logger.trace( "exiting " + cn + ".add_host_to_hostgroup");
    return new_member;
}

/* add a new service group to the list in memory */
public static objects_h.servicegroup add_servicegroup(String name,String alias){
	logger.trace( "entering " + cn + ".add_servicegroup");

	/* make sure we have the data we need */
	if(name== null || alias== null ){
		logger.fatal( "Error: Servicegroup name and/or alias is null\n");
		return null;
	        }

    name = name.trim();
    alias = alias.trim();

    if ( name.length() == 0 || alias.length() == 0 ) {
		logger.fatal("Error: Servicegroup name and/or alias is null\n");
		return null;
	        }

	/* make sure a servicegroup by this name hasn't been added already */
	if(find_servicegroup(name)!=null){
		logger.fatal( "Error: Servicegroup '"+name+"' has already been defined");
		return null;
	        }

	/* allocate memory */
	objects_h.servicegroup new_servicegroup= new objects_h.servicegroup();
	new_servicegroup.group_name=name;
	new_servicegroup.alias=alias;

	new_servicegroup.members=null;

	/* add new servicegroup to servicegroup chained hash list */
	if(!objects.add_servicegroup_to_hashlist(new_servicegroup)){
		logger.fatal( "Error: Could not allocate memory for servicegroup list to add servicegroup '"+name+"'");
		return null;
	        }

    servicegroup_list.add( new_servicegroup );
		
	logger.debug("\tGroup name:     " + new_servicegroup.group_name);
	logger.debug("\tAlias:          " + new_servicegroup.alias);
    logger.trace( "exiting " + cn + ".add_servicegroup");
	return new_servicegroup;
	}

/* add a new service to a service group */
public static objects_h.servicegroupmember add_service_to_servicegroup(objects_h.servicegroup temp_servicegroup, String host_name, String svc_description){
	logger.trace( "entering " + cn + ".add_service_to_servicegroup");

	/* make sure we have the data we need */
	if(temp_servicegroup==null || host_name==null || svc_description==null ){
		logger.fatal( "Error: Servicegroup or group member is null\n");
		return null;
	        }

    host_name = host_name.trim();
    svc_description = svc_description.trim();
    if ( host_name.length() == 0 || svc_description.length() == 0 ) {
		logger.fatal( "Error: Servicegroup member is null\n");
		return null;
	        }

	/* allocate memory for a new member */
	objects_h.servicegroupmember new_member = new objects_h.servicegroupmember();
	new_member.host_name=host_name;
	new_member.service_description=svc_description;
	
	/* add new member to member list, sorted by host name then service description */
    if ( temp_servicegroup.members == null ) 
        temp_servicegroup.members = new ArrayList<servicegroupmember>(); 
    temp_servicegroup.members.add( new_member );
    
	logger.trace( "exiting " + cn + ".add_service_to_servicegroup");
	return new_member;
        }

/**
 * Ability to remove objects from the model.
 * 
 * @param contact to remove
 */
public static void remove_contact( String contact ) { 
    remove_contact( find_contact(contact) );
}

/**
 * Ability to remove objects from the model.
 * 
 * @param contact to remove
 */
public static void remove_contact( objects_h.contact contact ) {
    if ( contact != null ) {
	contact_list.remove(contact);
	contact_hashlist.remove( contact.name );
	for ( objects_h.contactgroup group : contactgroup_list ) {
	    objects_h.contactgroupmember member = find_contactgroupmember(contact.name, group );
	    if ( member != null ) {
		group.members.remove( member );
	    }
	}
    }
}

/* add a new contact to the list in memory */
public static objects_h.contact add_contact(String name, String alias, String email, String pager, String[] addresses, String svc_notification_period, String host_notification_period,int notify_service_ok,int notify_service_critical,int notify_service_warning, int notify_service_unknown, int notify_service_flapping, int notify_host_up, int notify_host_down, int notify_host_unreachable, int notify_host_flapping){
	logger.trace( "entering " + cn + ".add_contact");

	/* make sure we have the data we need */
	if(name==null || alias==null || (email==null && pager==null)){
		logger.fatal( "Error: Contact name, alias, or email address and pager number are null\n");
		return null;
	        }

    name = name.trim();
    alias = alias.trim();
    if ( email != null ) email = email.trim();
    if ( pager != null ) pager = pager.trim();
    svc_notification_period = svc_notification_period.trim();
    host_notification_period =host_notification_period.trim(); 

    if ( name.length() == 0 || alias.length() == 0 ) {
		logger.fatal( "Error: Contact name or alias is null\n");
		return null;
	        }

	if((email==null || email.length()==0 ) && ( pager==null || pager.length() == 0)){
		logger.fatal( "Error: Contact email address and pager number are both null\n");
		return null;
	        }

	/* make sure there isn't a contact by this name already added */
	if(find_contact(name)!=null){
		logger.fatal( "Error: Contact '"+name+"' has already been defined");
		return null;
	        }

	if(notify_service_ok<0 || notify_service_ok>1){
		logger.fatal( "Error: Invalid notify_service_ok value for contact '" + name + "'");
		return null;
	        }
	if(notify_service_critical<0 || notify_service_critical>1){
        logger.fatal( "Error: Invalid notify_service_critical value for contact '" + name + "'");
		return null;
	        }
	if(notify_service_warning<0 || notify_service_warning>1){
        logger.fatal( "Error: Invalid notify_service_warning value for contact '" + name + "'");
		return null;
	        }
	if(notify_service_unknown<0 || notify_service_unknown>1){
        logger.fatal( "Error: Invalid notify_service_unknown value for contact '" + name + "'");
		return null;
	        }
	if(notify_service_flapping<0 || notify_service_flapping>1){
        logger.fatal( "Error: Invalid notify_service_flapping value for contact '" + name + "'");
		return null;
	        }

	if(notify_host_up<0 || notify_host_up>1){
        logger.fatal( "Error: Invalid notify_host_up value for contact '" + name + "'");
		return null;
	        }
	if(notify_host_down<0 || notify_host_down>1){
        logger.fatal( "Error: Invalid notify_host_down value for contact '" + name + "'");
		return null;
	        }
	if(notify_host_unreachable<0 || notify_host_unreachable>1){
        logger.fatal( "Error: Invalid notify_host_unreachable value for contact '" + name + "'");
		return null;
	        }
	if(notify_host_flapping<0 || notify_host_flapping>1){
        logger.fatal( "Error: Invalid notify_host_flapping value for contact '" + name + "'");
        return null;
	}
	
	/* allocate memory for a new contact */
	objects_h.contact new_contact= new objects_h.contact();
	new_contact.name=name;
	new_contact.alias=alias;
	new_contact.email=email;
	new_contact.pager=pager;
	new_contact.service_notification_period=svc_notification_period;
	new_contact.host_notification_period=host_notification_period;
	
	for(int x=0;x<addresses.length;x++)
	    new_contact.address[x]=addresses[x];
	
	new_contact.host_notification_commands=null;
	new_contact.service_notification_commands=null;
	
	new_contact.notify_on_service_recovery=(notify_service_ok);
	new_contact.notify_on_service_critical=(notify_service_critical);
	new_contact.notify_on_service_warning=(notify_service_warning);
	new_contact.notify_on_service_unknown=(notify_service_unknown);
	new_contact.notify_on_service_flapping=(notify_service_flapping);
	new_contact.notify_on_host_recovery=(notify_host_up);
	new_contact.notify_on_host_down=(notify_host_down);
	new_contact.notify_on_host_unreachable=(notify_host_unreachable);
	new_contact.notify_on_host_flapping=(notify_host_flapping);
	
	/* add new contact to contact chained hash list */
	// there is no memory error to worry about, hence, plus did findContact already
	add_contact_to_hashlist( new_contact );
	contact_list.add( new_contact );
	
	logger.debug("\tContact Name:                  " + new_contact.name);
	logger.debug("\tContact Alias:                 " + new_contact.alias);
	logger.debug("\tContact Email Address:         " + ((new_contact.email==null)?"":new_contact.email));
	logger.debug("\tContact Pager Address/Number:  " + ((new_contact.pager==null)?"":new_contact.pager));
	logger.debug("\tSvc Notification Time Period:  " + new_contact.service_notification_period);
	logger.debug("\tHost Notification Time Period: " + new_contact.host_notification_period);
	
	logger.trace( "exiting " + cn + ".add_contact");
	return new_contact;
}

/* adds a host notification command to a contact definition */
public static objects_h.commandsmember add_host_notification_command_to_contact(objects_h.contact cntct,String command_name){
    logger.trace( "entering " + cn + ".add_host_notification_command_to_contact");
    
    /* make sure we have the data we need */
    if(cntct==null || command_name==null){
        logger.fatal( "Error: Contact or host notification command is null\n");
        return null;
    }
    command_name = command_name.trim();
    
    if( command_name.length() == 0 ){
        logger.fatal( "Error: Contact '"+cntct.name+"' host notification command is null\n");
        return null;
    }
    
    /* allocate memory */
    objects_h.commandsmember new_commandsmember = new objects_h.commandsmember();
    new_commandsmember.command=command_name;
    
    /* add the notification command */
    if ( cntct.host_notification_commands == null ) 
        cntct.host_notification_commands = new ArrayList<objects_h.commandsmember>();
    cntct.host_notification_commands.add( new_commandsmember );
    
    logger.trace( "exiting " + cn + ".add_host_notification_command_to_contact");
    return new_commandsmember;
}

/* adds a service notification command to a contact definition */
public static objects_h.commandsmember add_service_notification_command_to_contact(objects_h.contact cntct,String command_name){
    logger.trace( "entering " + cn + ".add_service_notification_command_to_contact");
    
    /* make sure we have the data we need */
    if( cntct==null || command_name==null ){
        logger.fatal( "Error: Contact or service notification command is null\n");
        return null;
    }
    
    command_name = command_name.trim();
    
    if( command_name.length() == 0 ){
        logger.fatal( "Error: Contact '"+cntct.name+"' service notification command is null\n");
        return null;
    }
    
    /* allocate memory */
    objects_h.commandsmember new_commandsmember = new objects_h.commandsmember();
    new_commandsmember.command = command_name ;
    
    /* add the notification command */
    if ( cntct.service_notification_commands == null ) 
        cntct.service_notification_commands = new ArrayList<objects_h.commandsmember>();
    cntct.service_notification_commands.add( new_commandsmember );
    
    logger.trace( "entering " + cn + ".add_service_notification_command_to_contact");
    return new_commandsmember;
}
/* add a new command to the list in memory */
public static objects_h.command add_command(String name, String value){
    
    logger.trace( "entering " + cn + ".add_command");
    
    /* make sure we have the data we need */
    if(name== null || value==  null){
        logger.fatal( "Error: Command name of command line is null");
        return null;
    }
    
    name = name.trim();
    value = value.trim();
    
    if(name.length() == 0 || value.length() ==  0){
        logger.fatal( "Error: Command name of command line is empty");
        return null;
    }
    
    /* make sure there isn't a command by this name added already */
    objects_h.command temp_command=find_command(name);
    if(temp_command!=null){
        logger.fatal( "Error: Command '"+name+"' has already been defined" );
        return null;
    }
    
    objects_h.command new_command = new objects_h.command();
    new_command.name=name;
    new_command.command_line=value;
    
    /* add new command to command chained hash list */
    if(!add_command_to_hashlist(new_command)){
        logger.fatal( "Error: Could not allocate memory for command list to add command '"+name+"'");
        return null;
    }
    
    /* commands are sorted alphabetically for daemon, so add new items to tail of list */
    command_list.add( new_command );
    
    logger.debug("\tName:\t" + new_command.name);
    logger.debug("\tCommand Line: " + new_command.command_line);
    logger.trace( "exiting " + cn + ".add_command");
    return new_command;
}

/* add a new contact group to the list in memory */
public static objects_h.contactgroup add_contactgroup(String name,String alias){
    logger.trace( "entering " + cn + ".add_contactgroup" );
    
    /* make sure we have the data we need */
    if(name==null || alias==null){
        logger.fatal(  "Error: Contactgroup name or alias is null\n");
        return null;
    }
    
    name = name.trim();
    alias = alias.trim();
    
    if(name.length() == 0 || alias.length() == 0){
        logger.fatal(  "Error: Contactgroup name or alias is null\n");
        return null;
    }
    
    /* make sure there isn't a contactgroup by this name added already */
    if(find_contactgroup(name)!=null){
        logger.fatal(  "Error: Contactgroup '"+name+"' has already been defined");
        return null;
    }
    
    /* allocate memory for a new contactgroup entry */
    objects_h.contactgroup new_contactgroup = new objects_h.contactgroup();
    new_contactgroup.group_name=name;
    new_contactgroup.alias=alias;
    
    new_contactgroup.members=null;
    
    /* add new contactgroup to contactgroup chained hash list */
    add_contactgroup_to_hashlist(new_contactgroup);
    contactgroup_list.add( new_contactgroup );
    
    logger.debug("\tGroup name:   " + new_contactgroup.group_name);
    logger.debug("\tAlias:        " + new_contactgroup.alias);
    
    logger.trace( "exiting " + cn + ".add_contactgroup" );
    
    return new_contactgroup;
	}

/* add a new member to a contact group */
public static objects_h.contactgroupmember add_contact_to_contactgroup(objects_h.contactgroup grp,String contact_name){
    
    logger.trace( "entering " + cn + ".add_contact_to_contactgroup" );
    
    /* make sure we have the data we need */
    if(grp==null || contact_name==null){
        logger.fatal(  "Error: Contactgroup or contact name is null");
        return null;
    }
    
    contact_name = contact_name.trim();
    
    if(contact_name.length() == 0){
        logger.fatal(  "Error: Contactgroup '" + grp.group_name + "' contact name is null");
        return null;
    }
    
    /* allocate memory for a new member */
    objects_h.contactgroupmember new_contactgroupmember= new objects_h.contactgroupmember ();
    
    new_contactgroupmember.contact_name=contact_name;
    
    /* add the new member to the head of the member list */
    if ( grp.members == null ) 
        grp.members = new ArrayList<objects_h.contactgroupmember>();
    grp.members.add( new_contactgroupmember );
    
    logger.trace( "exiting " + cn + ".add_contact_to_contactgroup" );
    
    return new_contactgroupmember;
}

/* add a new service to the list in memory */
public static objects_h.service add_service(String host_name, String description, String check_period, int max_attempts, int parallelize, int accept_passive_checks, int check_interval, int retry_interval, int notification_interval, String notification_period, int notify_recovery, int notify_unknown, int notify_warning, int notify_critical, int notify_flapping, int notifications_enabled, int is_volatile, String event_handler, int event_handler_enabled, String check_command, int checks_enabled, int flap_detection_enabled, double low_flap_threshold, double high_flap_threshold, int stalk_ok, int stalk_warning, int stalk_unknown, int stalk_critical, int process_perfdata, int failure_prediction_enabled, String failure_prediction_options, int check_freshness, int freshness_threshold, int retain_status_information, int retain_nonstatus_information, int obsess_over_service){
logger.trace( "entering " + cn + ".add_service" );

	/* make sure we have everything we need */
	if(host_name==null || description==null || check_command==null){
logger.fatal(  "Error: Service description, host name, or check command is null");
		return null;
	        }

	host_name = host_name.trim();
	description = description.trim();
	check_command = check_command.trim();
	if (event_handler != null ) event_handler = event_handler.trim();
	if (notification_period != null) notification_period = notification_period.trim();
	if (check_period != null) check_period = check_period.trim();

	if(host_name.length() == 0 || description.length() == 0 || check_command.length() == 0){
logger.fatal(  "Error: Service description, host name, or check command is null");
		return null;
	        }

	/* make sure the host name isn't too long */
	if(host_name.length()>objects_h.MAX_HOSTNAME_LENGTH-1){
logger.fatal(  "Error: Host name '" + host_name + "' for service '" + description + "' exceeds maximum length of " + (objects_h.MAX_HOSTNAME_LENGTH-1) + " characters");
		return null;
	        }

	/* make sure there isn't a service by this name added already */
	if(find_service(host_name,description)!=null){
logger.fatal(  "Error: Service ' " + description + " ' on host '" + host_name + "' has already been defined");
		return null;
	        }

	/* make sure the service description isn't too long */
	if(description.length()>objects_h.MAX_SERVICEDESC_LENGTH-1){
logger.fatal(  "Error: Name of service ' " + description + " ' on host '" + host_name + "' exceeds maximum length of " + (objects_h.MAX_SERVICEDESC_LENGTH-1) + " characters");
		return null;
	        }

	if(parallelize<0 || parallelize>1){
logger.fatal(  "Error: Invalid parallelize value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }

	if(accept_passive_checks<0 || accept_passive_checks>1){
logger.fatal(  "Error: Invalid accept_passive_checks value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }

	if(event_handler_enabled<0 || event_handler_enabled>1){
logger.fatal(  "Error: Invalid event_handler_enabled value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }

	if(checks_enabled<0 || checks_enabled>1){
logger.fatal(  "Error: Invalid checks_enabled value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }

	if(notifications_enabled<0 || notifications_enabled>1){
logger.fatal(  "Error: Invalid notifications_enabled value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }

	if(max_attempts<=0 || check_interval<0 || retry_interval<=0 || notification_interval<0){
logger.fatal(  "Error: Invalid max_attempts, check_interval, retry_interval, or notification_interval value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(notify_recovery<0 || notify_recovery>1){
logger.fatal(  "Error: Invalid notify_recovery value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(notify_critical<0 || notify_critical>1){
logger.fatal(  "Error: Invalid notify_critical value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(notify_flapping<0 || notify_flapping>1){
logger.fatal(  "Error: Invalid notify_flapping value ' " + notify_flapping + " ' for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(notify_recovery<0 || notify_recovery>1){
logger.fatal(  "Error: Invalid notify_recovery value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(is_volatile<0 || is_volatile>1){
logger.fatal(  "Error: Invalid is_volatile value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(flap_detection_enabled<0 || flap_detection_enabled>1){
logger.fatal(  "Error: Invalid flap_detection_enabled value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(stalk_ok<0 || stalk_ok>1){
logger.fatal(  "Error: Invalid stalk_ok value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(stalk_warning<0 || stalk_warning>1){
logger.fatal(  "Error: Invalid stalk_warning value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(stalk_unknown<0 || stalk_unknown>1){
logger.fatal(  "Error: Invalid stalk_unknown value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(stalk_critical<0 || stalk_critical>1){
logger.fatal(  "Error: Invalid stalk_critical value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(process_perfdata<0 || process_perfdata>1){
logger.fatal(  "Error: Invalid process_perfdata value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(failure_prediction_enabled<0 || failure_prediction_enabled>1){
logger.fatal(  "Error: Invalid failure_prediction_enabled value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(check_freshness<0 || check_freshness>1){
logger.fatal(  "Error: Invalid check_freshness value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(freshness_threshold<0){
logger.fatal(  "Error: Invalid freshness_threshold value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(retain_status_information<0 || retain_status_information>1){
logger.fatal(  "Error: Invalid retain_status_information value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(retain_nonstatus_information<0 || retain_nonstatus_information>1){
logger.fatal(  "Error: Invalid retain_nonstatus_information value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }
	if(obsess_over_service<0 || obsess_over_service>1){
logger.fatal(  "Error: Invalid obsess_over_service value for service ' " + description + " ' on host '" + host_name + "'");
		return null;
	        }

	/* allocate memory */
	objects_h.service new_service= new objects_h.service ();
	new_service.host_name=host_name;
	new_service.description=description;

	new_service.service_check_command=check_command;
	new_service.event_handler=event_handler;
	new_service.notification_period=notification_period;
	new_service.check_period=check_period;
	new_service.failure_prediction_options=failure_prediction_options;

	new_service.contact_groups=null;
	new_service.check_interval=check_interval;
	new_service.retry_interval=retry_interval;
	new_service.max_attempts=max_attempts;
	new_service.parallelize=(parallelize>0)?common_h.TRUE:common_h.FALSE;
	new_service.notification_interval=notification_interval;
	new_service.notify_on_unknown=(notify_unknown>0)?common_h.TRUE:common_h.FALSE;
	new_service.notify_on_warning=(notify_warning>0)?common_h.TRUE:common_h.FALSE;
	new_service.notify_on_critical=(notify_critical>0)?common_h.TRUE:common_h.FALSE;
	new_service.notify_on_recovery=(notify_recovery>0)?common_h.TRUE:common_h.FALSE;
	new_service.notify_on_flapping=(notify_flapping>0)?common_h.TRUE:common_h.FALSE;
	new_service.is_volatile=(is_volatile>0)?common_h.TRUE:common_h.FALSE;
	new_service.flap_detection_enabled=(flap_detection_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_service.low_flap_threshold=low_flap_threshold;
	new_service.high_flap_threshold=high_flap_threshold;
	new_service.stalk_on_ok=(stalk_ok>0)?common_h.TRUE:common_h.FALSE;
	new_service.stalk_on_warning=(stalk_warning>0)?common_h.TRUE:common_h.FALSE;
	new_service.stalk_on_unknown=(stalk_unknown>0)?common_h.TRUE:common_h.FALSE;
	new_service.stalk_on_critical=(stalk_critical>0)?common_h.TRUE:common_h.FALSE;
	new_service.process_performance_data=(process_perfdata>0)?common_h.TRUE:common_h.FALSE;
	new_service.check_freshness=(check_freshness>0)?common_h.TRUE:common_h.FALSE;
	new_service.freshness_threshold=freshness_threshold;
	new_service.accept_passive_service_checks=(accept_passive_checks>0)?common_h.TRUE:common_h.FALSE;
	new_service.event_handler_enabled=(event_handler_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_service.checks_enabled=(checks_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_service.retain_status_information=(retain_status_information>0)?common_h.TRUE:common_h.FALSE;
	new_service.retain_nonstatus_information=(retain_nonstatus_information>0)?common_h.TRUE:common_h.FALSE;
	new_service.notifications_enabled=(notifications_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_service.obsess_over_service=(obsess_over_service>0)?common_h.TRUE:common_h.FALSE;
	new_service.failure_prediction_enabled=(failure_prediction_enabled>0)?common_h.TRUE:common_h.FALSE;
	new_service.problem_has_been_acknowledged=common_h.FALSE;
	new_service.acknowledgement_type=common_h.ACKNOWLEDGEMENT_NONE;
	new_service.check_type=common_h.SERVICE_CHECK_ACTIVE;
	new_service.current_attempt=1;
	new_service.current_state=blue_h.STATE_OK;
	new_service.last_state=blue_h.STATE_OK;
	new_service.last_hard_state=blue_h.STATE_OK;
	/* initial state type changed from SOFT_STATE on 6/17/03 - shouldn't this have been HARD_STATE all along? */
	new_service.state_type=common_h.HARD_STATE;
	new_service.host_problem_at_last_check=common_h.FALSE;
//#ifdef REMOVED_041403
	new_service.no_recovery_notification=common_h.FALSE;
//#endif
	new_service.check_flapping_recovery_notification=common_h.FALSE;
	new_service.next_check=0L;
	new_service.should_be_scheduled=common_h.TRUE;
	new_service.last_check=0L;
	new_service.last_notification=0L;
	new_service.next_notification=0L;
	new_service.no_more_notifications=common_h.FALSE;
	new_service.last_state_change=0L;
	new_service.last_hard_state_change=0L;
	new_service.last_time_ok=0L;
	new_service.last_time_warning=0L;
	new_service.last_time_unknown=0L;
	new_service.last_time_critical=0L;
	new_service.has_been_checked=common_h.FALSE;
	new_service.is_being_freshened=common_h.FALSE;
	new_service.notified_on_unknown=common_h.FALSE;
	new_service.notified_on_warning=common_h.FALSE;
	new_service.notified_on_critical=common_h.FALSE;
	new_service.current_notification_number=0;
	new_service.latency=0.0;
	new_service.execution_time=0.0;
	new_service.is_executing=common_h.FALSE;
	new_service.check_options=blue_h.CHECK_OPTION_NONE;
	new_service.scheduled_downtime_depth=0;
	new_service.pending_flex_downtime=0;
	for(int x=0;x<objects_h.MAX_STATE_HISTORY_ENTRIES;x++)
		new_service.state_history[x]=blue_h.STATE_OK;
	new_service.state_history_index=0;
	new_service.is_flapping=common_h.FALSE;
	new_service.flapping_comment_id=0;
	new_service.percent_state_change=0.0;
	new_service.modified_attributes=common_h.MODATTR_NONE;

	/* allocate new plugin output buffer */
	new_service.plugin_output="(Service assumed to be ok)";

	/* allocate new performance data buffer */
	new_service.perf_data="";

	/* add new service to service chained hash list */
	add_service_to_hashlist(new_service);
	service_list.add( new_service );

	logger.debug("\tHost:                     " + new_service.host_name);
	logger.debug("\tDescription:              " + new_service.description);
	logger.debug("\tCommand:                  " + new_service.service_check_command);
	logger.debug("\tCheck Interval:           " + new_service.check_interval);
	logger.debug("\tRetry Interval:           " + new_service.retry_interval);
	logger.debug("\tMax attempts:             " + new_service.max_attempts);
	logger.debug("\tNotification Interval:    " + new_service.notification_interval);
	logger.debug("\tNotification Time Period: " + new_service.notification_period);
	logger.debug("\tNotify On Warning:        " + ((new_service.notify_on_warning==1)?"yes":"no"));
	logger.debug("\tNotify On Critical:       " + ((new_service.notify_on_critical==1)?"yes":"no"));
	logger.debug("\tNotify On Recovery:       " + ((new_service.notify_on_recovery==1)?"yes":"no"));
	logger.debug("\tEvent Handler:            " + ((new_service.event_handler==null)?"N/A":new_service.event_handler));

logger.trace( "exiting " + cn + ".add_service" );

	return new_service;
	}

/* adds a contact group to a service */
public static objects_h.contactgroupsmember add_contactgroup_to_service(objects_h.service svc, String group_name){
    logger.trace( "entering " + cn + ".add_contactgroup_to_service" );
    
    /* bail out if we weren't given the data we need */
    if(svc==null || group_name==null){
        logger.fatal(  "Error: Service or contactgroup name is null");
        return null;
    }
    
    group_name = group_name.trim();
    
    if(group_name.length() == 0){
        logger.fatal(  "Error: Contactgroup name is null");
        return null;
    }
    
    /* allocate memory for the contactgroups member */
    objects_h.contactgroupsmember new_contactgroupsmember= new objects_h.contactgroupsmember ();
    new_contactgroupsmember.group_name=group_name;
    
    /* add this contactgroup to the service */
    if ( svc.contact_groups == null ) svc.contact_groups = new ArrayList<objects_h.contactgroupsmember>();
    svc.contact_groups.add( new_contactgroupsmember );
    
    logger.trace( "exiting " + cn + ".add_contactgroup_to_service" );
    
    return new_contactgroupsmember;
}

/* add a new service escalation to the list in memory */
public static objects_h.serviceescalation add_serviceescalation( String host_name, String description,int first_notification,int last_notification, int notification_interval, String escalation_period, int escalate_on_warning, int escalate_on_unknown, int escalate_on_critical, int escalate_on_recovery){
    logger.trace( "entering " + cn + ".add_serviceescalation" );
    
    /* make sure we have the data we need */
    if(host_name==null || description==null){
        logger.fatal(  "Error: Service escalation host name or description is null");
        return null;
    }
    
    host_name = host_name.trim();
    description = description.trim();
    
    if(host_name.length() == 0 || description.length() == 0){
        logger.fatal(  "Error: Service escalation host name or description is null");
        return null;
    }
    
    /* check options */
    if(escalate_on_warning<0 || escalate_on_warning>1 || escalate_on_unknown<0 || escalate_on_unknown>1 || escalate_on_critical<0 || escalate_on_critical>1 || escalate_on_recovery<0 || escalate_on_recovery>1){
        logger.fatal(  "Error: Invalid escalation options in service ' " + description + " ' on host '" + host_name + "' escalation");
        return null;
    }
    
    /* allocate memory for a new service escalation entry */
    objects_h.serviceescalation new_serviceescalation= new objects_h.serviceescalation ();
    new_serviceescalation.host_name=host_name;
    new_serviceescalation.description=description;
    new_serviceescalation.escalation_period=escalation_period;
    
    new_serviceescalation.first_notification=first_notification;
    new_serviceescalation.last_notification=last_notification;
    new_serviceescalation.notification_interval=(notification_interval<=0)?0:notification_interval;
    new_serviceescalation.escalate_on_recovery=(escalate_on_recovery>0)?common_h.TRUE:common_h.FALSE;
    new_serviceescalation.escalate_on_warning=(escalate_on_warning>0)?common_h.TRUE:common_h.FALSE;
    new_serviceescalation.escalate_on_unknown=(escalate_on_unknown>0)?common_h.TRUE:common_h.FALSE;
    new_serviceescalation.escalate_on_critical=(escalate_on_critical>0)?common_h.TRUE:common_h.FALSE;
    new_serviceescalation.contact_groups=null;
    
    /* add new serviceescalation to serviceescalation chained hash list */
    add_serviceescalation_to_hashlist(new_serviceescalation);
    serviceescalation_list.add( new_serviceescalation );
    
    logger.debug("\tHost name:             " + new_serviceescalation.host_name);
    logger.debug("\tSvc description:       " + new_serviceescalation.description);
    logger.debug("\tFirst notification:    " + new_serviceescalation.first_notification);
    logger.debug("\tLast notification:     " + new_serviceescalation.last_notification);
    logger.debug("\tNotification Interval: " + new_serviceescalation.notification_interval);
    
    logger.trace( "exiting " + cn + ".add_serviceescalation" );
    
    return new_serviceescalation;
}

/* adds a contact group to a service escalation */
public static objects_h.contactgroupsmember add_contactgroup_to_serviceescalation(objects_h.serviceescalation se, String group_name){
    logger.trace( "entering " + cn + ".add_contactgroup_to_serviceescalation" );
    
    /* bail out if we weren't given the data we need */
    if(se==null || group_name==null){
        logger.fatal(  "Error: Service escalation or contactgroup name is null");
        return null;
    }
    
    group_name = group_name.trim();
    
    if(group_name.length() == 0){
        logger.fatal(  "Error: Contactgroup name is null");
        return null;
    }
    
    /* allocate memory for the contactgroups member */
    objects_h.contactgroupsmember new_contactgroupsmember= new objects_h.contactgroupsmember ();
    new_contactgroupsmember.group_name=group_name;
    
    /* add this contactgroup to the service escalation */
    if ( se.contact_groups == null ) se.contact_groups = new ArrayList<objects_h.contactgroupsmember>();
    se.contact_groups.add( new_contactgroupsmember );
    
    logger.trace( "exiting " + cn + ".add_contactgroup_to_serviceescalation" );
    
    return new_contactgroupsmember;
}

/* adds a service dependency definition */
public static objects_h.servicedependency add_service_dependency(String dependent_host_name, String dependent_service_description, String host_name, String service_description, int dependency_type, int inherits_parent, int fail_on_ok, int fail_on_warning, int fail_on_unknown, int fail_on_critical, int fail_on_pending){
    logger.trace( "entering " + cn + ".add_service_dependency" );
    
    /* make sure we have what we need */
    if(dependent_host_name==null || dependent_service_description==null || host_name==null || service_description==null){
        logger.fatal(  "Error: null service description/host name in service dependency definition");
        return null;
    }
    
    dependent_host_name = dependent_host_name.trim();
    dependent_service_description = dependent_service_description.trim();
    host_name = host_name.trim();
    service_description = service_description.trim();
    
    if(dependent_host_name.length() == 0 || dependent_service_description.length() == 0 || host_name.length() == 0 || service_description.length() == 0){
        logger.fatal(  "Error: null service description/host name in service dependency definition");
        return null;
    }
    
    if(fail_on_ok<0 || fail_on_ok>1){
        logger.fatal(  "Error: Invalid fail_on_ok value for service ' " + dependent_service_description + " ' on host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_warning<0 || fail_on_warning>1){
        logger.fatal(  "Error: Invalid fail_on_warning value for service ' " + dependent_service_description + " ' on host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_unknown<0 || fail_on_unknown>1){
        logger.fatal(  "Error: Invalid fail_on_unknown value for service ' " + dependent_service_description + " ' on host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_critical<0 || fail_on_critical>1){
        logger.fatal(  "Error: Invalid fail_on_critical value for service ' " + dependent_service_description + " ' on host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_pending<0 || fail_on_pending>1){
        logger.fatal(  "Error: Invalid fail_on_pending value for service ' " + dependent_service_description + " ' on host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(inherits_parent<0 || inherits_parent>1){
        logger.fatal(  "Error: Invalid inherits_parent value for service ' " + dependent_service_description + " ' on host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    /* allocate memory for a new service dependency entry */
    objects_h.servicedependency new_servicedependency= new objects_h.servicedependency();
    new_servicedependency.dependent_host_name=dependent_host_name;
    new_servicedependency.dependent_service_description=dependent_service_description;
    new_servicedependency.host_name=host_name;
    new_servicedependency.service_description=service_description;
    new_servicedependency.dependency_type=(dependency_type==common_h.EXECUTION_DEPENDENCY)?common_h.EXECUTION_DEPENDENCY:common_h.NOTIFICATION_DEPENDENCY;
    new_servicedependency.inherits_parent=(inherits_parent>0)?common_h.TRUE:common_h.FALSE;
    new_servicedependency.fail_on_ok=(fail_on_ok==1)?common_h.TRUE:common_h.FALSE;
    new_servicedependency.fail_on_warning=(fail_on_warning==1)?common_h.TRUE:common_h.FALSE;
    new_servicedependency.fail_on_unknown=(fail_on_unknown==1)?common_h.TRUE:common_h.FALSE;
    new_servicedependency.fail_on_critical=(fail_on_critical==1)?common_h.TRUE:common_h.FALSE;
    new_servicedependency.fail_on_pending=(fail_on_pending==1)?common_h.TRUE:common_h.FALSE;
    new_servicedependency.circular_path_checked=common_h.FALSE; /* UPDATED 2.2 */
    new_servicedependency.contains_circular_path=common_h.FALSE; /* UPDATED 2.2 */
    
    /* add new servicedependency to servicedependency chained hash list */
    add_servicedependency_to_hashlist(new_servicedependency);
    servicedependency_list.add( new_servicedependency );
    
    logger.trace( "exiting " + cn + ".add_service_dependency" );
    
    return new_servicedependency;
}

/* adds a host dependency definition */
public static objects_h.hostdependency add_host_dependency(String dependent_host_name, String host_name, int dependency_type, int inherits_parent, int fail_on_up, int fail_on_down, int fail_on_unreachable, int fail_on_pending){
    logger.trace( "entering " + cn + ".add_host_dependency" );
    
    /* make sure we have what we need */
    if(dependent_host_name==null || host_name==null){
        logger.fatal(  "Error: null host name in host dependency definition");
        return null;
    }
    
    dependent_host_name = dependent_host_name.trim();
    host_name = host_name.trim();
    
    if(dependent_host_name.length() == 0 || host_name.length() == 0){
        logger.fatal(  "Error: null host name in host dependency definition");
        return null;
    }
    
    if(fail_on_up<0 || fail_on_up>1){
        logger.fatal(  "Error: Invalid fail_on_up value for host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_down<0 || fail_on_down>1){
        logger.fatal(  "Error: Invalid fail_on_down value for host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_unreachable<0 || fail_on_unreachable>1){
        logger.fatal(  "Error: Invalid fail_on_unreachable value for host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    if(fail_on_pending<0 || fail_on_pending>1){
        logger.fatal(  "Error: Invalid fail_on_pending value for host '" + dependent_host_name + "' dependency definition");
        return null;
    }
    
    /* allocate memory for a new host dependency entry */
    objects_h.hostdependency new_hostdependency= new objects_h.hostdependency();
    new_hostdependency.dependent_host_name=dependent_host_name;
    new_hostdependency.host_name=host_name;
    
    new_hostdependency.dependency_type=(dependency_type==common_h.EXECUTION_DEPENDENCY)?common_h.EXECUTION_DEPENDENCY:common_h.NOTIFICATION_DEPENDENCY;
    new_hostdependency.inherits_parent=(inherits_parent>0)?common_h.TRUE:common_h.FALSE;
    new_hostdependency.fail_on_up=(fail_on_up==1)?common_h.TRUE:common_h.FALSE;
    new_hostdependency.fail_on_down=(fail_on_down==1)?common_h.TRUE:common_h.FALSE;
    new_hostdependency.fail_on_unreachable=(fail_on_unreachable==1)?common_h.TRUE:common_h.FALSE;
    new_hostdependency.fail_on_pending=(fail_on_pending==1)?common_h.TRUE:common_h.FALSE;
    
    new_hostdependency.circular_path_checked=common_h.FALSE;
    new_hostdependency.contains_circular_path=common_h.FALSE;

    /* add new hostdependency to hostdependency chained hash list */
    add_hostdependency_to_hashlist(new_hostdependency);
    hostdependency_list.add( new_hostdependency );
    
    logger.trace( "exiting " + cn + ".add_host_dependency" );
    
    return new_hostdependency;
}
//
///* add a new host escalation to the list in memory */
public static objects_h.hostescalation add_hostescalation(String host_name,int first_notification,int last_notification, int notification_interval, String escalation_period, int escalate_on_down, int escalate_on_unreachable, int escalate_on_recovery){
    logger.trace( "entering " + cn + ".add_hostescalation" );
    
    /* make sure we have the data we need */
    if(host_name==null){
        logger.fatal(  "Error: Host escalation host name is null");
        return null;
    }
    
    host_name = host_name.trim();
    
    if(host_name.length() == 0){
        logger.fatal(  "Error: Host escalation host name is null");
        return null;
    }
    
    /* check options */
    if(escalate_on_down<0 || escalate_on_down>1 || escalate_on_unreachable<0 || escalate_on_unreachable>1 || escalate_on_recovery<0 || escalate_on_recovery>1){
        logger.fatal(  "Error: Invalid escalation options in host '" + host_name + "' escalation");
        return null;
    }
    
    /* allocate memory for a new host escalation entry */
    objects_h.hostescalation new_hostescalation= new objects_h.hostescalation ();
    new_hostescalation.host_name=host_name;
    
    new_hostescalation.escalation_period=escalation_period;
    new_hostescalation.first_notification=first_notification;
    new_hostescalation.last_notification=last_notification;
    new_hostescalation.notification_interval=(notification_interval<=0)?0:notification_interval;
    new_hostescalation.escalate_on_recovery=(escalate_on_recovery>0)?common_h.TRUE:common_h.FALSE;
    new_hostescalation.escalate_on_down=(escalate_on_down>0)?common_h.TRUE:common_h.FALSE;
    new_hostescalation.escalate_on_unreachable=(escalate_on_unreachable>0)?common_h.TRUE:common_h.FALSE;
    new_hostescalation.contact_groups=null;
    
    /* add new hostescalation to hostescalation chained hash list */
    add_hostescalation_to_hashlist(new_hostescalation);
    hostescalation_list.add( new_hostescalation );
    
    logger.debug("\tHost name:             " + new_hostescalation.host_name);
    logger.debug("\tFirst notification:    " + new_hostescalation.first_notification);
    logger.debug("\tLast notification:     " + new_hostescalation.last_notification);
    logger.debug("\tNotification Interval: " + new_hostescalation.notification_interval);
    
    logger.trace( "exiting " + cn + ".add_hostescalation" );
    
    return new_hostescalation;
}

///* adds a contact group to a host escalation */
public static objects_h.contactgroupsmember add_contactgroup_to_hostescalation(objects_h.hostescalation he,String group_name){
    logger.trace( "entering " + cn + ".add_contactgroup_to_hostescalation" );
    
    /* bail out if we weren't given the data we need */
    if(he==null || group_name==null){
        logger.fatal(  "Error: Host escalation or contactgroup name is null");
        return null;
    }
    
    group_name = group_name.trim();
    
    if(group_name.length() == 0){
        logger.fatal(  "Error: Contactgroup name is null");
        return null;
    }
    
    /* allocate memory for the contactgroups member */
    objects_h.contactgroupsmember new_contactgroupsmember= new objects_h.contactgroupsmember ();
    new_contactgroupsmember.group_name=group_name;
    
    /* add this contactgroup to the host escalation */
    if ( he.contact_groups == null ) he.contact_groups = new ArrayList<objects_h.contactgroupsmember>(); 
    he.contact_groups.add(new_contactgroupsmember);
    
    logger.trace( "exiting " + cn + ".add_contactgroup_to_hostescalation" );
    
    return new_contactgroupsmember;
}

///* adds an extended host info structure to the list in memory */
public static objects_h.hostextinfo add_hostextinfo(String host_name, String notes, String notes_url, String action_url, String icon_image, String vrml_image, String statusmap_image, String icon_image_alt, int x_2d, int y_2d, double x_3d, double y_3d, double z_3d, int have_2d_coords, int have_3d_coords){
    logger.trace( "entering " + cn + ".add_hostextinfo" );
    
    /* make sure we have what we need */
    if(host_name==null || host_name.length() == 0){
        logger.fatal(  "Error: Host name is null");
        return null;
    }
    
    /* allocate memory for a new data structure */
    objects_h.hostextinfo new_hostextinfo= new objects_h.hostextinfo ();
    new_hostextinfo.host_name=host_name;
    new_hostextinfo.notes=notes;
    new_hostextinfo.notes_url=notes_url;
    new_hostextinfo.action_url=action_url;
    new_hostextinfo.icon_image=icon_image;
    new_hostextinfo.vrml_image=vrml_image;
    new_hostextinfo.statusmap_image=statusmap_image;
    new_hostextinfo.icon_image_alt=icon_image_alt;
    
    /* 2-D coordinates */
    new_hostextinfo.x_2d=x_2d;
    new_hostextinfo.y_2d=y_2d;
    new_hostextinfo.have_2d_coords=have_2d_coords;
    
    /* 3-D coordinates */
    new_hostextinfo.x_3d=x_3d;
    new_hostextinfo.y_3d=y_3d;
    new_hostextinfo.z_3d=z_3d;
    new_hostextinfo.have_3d_coords=have_3d_coords;
    
    /* default is to not draw this item */
    new_hostextinfo.should_be_drawn=common_h.FALSE;
    
    /* add new hostextinfo to hostextinfo chained hash list */
    add_hostextinfo_to_hashlist(new_hostextinfo);
    hostextinfo_list.add( new_hostextinfo );
    
    logger.trace( "exiting " + cn + ".add_hostextinfo" );
    return new_hostextinfo;
}

///* adds an extended service info structure to the list in memory */
public static objects_h.serviceextinfo add_serviceextinfo(String host_name, String description, String notes, String notes_url, String action_url, String icon_image, String icon_image_alt){
    logger.trace( "entering " + cn + ".add_serviceextinfo" );
    
    /* make sure we have what we need */
    if((host_name==null || host_name.length() == 0) || (description==null || description.length() == 0)){
        logger.fatal(  "Error: Host name or service description is null");
        return null;
    }
    
    /* allocate memory for a new data structure */
    objects_h.serviceextinfo new_serviceextinfo= new objects_h.serviceextinfo ();
    new_serviceextinfo.host_name=host_name;
    new_serviceextinfo.description=description;
    new_serviceextinfo.notes=notes;
    new_serviceextinfo.notes_url=notes_url;
    new_serviceextinfo.action_url=action_url;
    new_serviceextinfo.icon_image=icon_image;
    new_serviceextinfo.icon_image_alt=icon_image_alt;
    
    /* add new serviceextinfo to serviceextinfo chained hash list */
    add_serviceextinfo_to_hashlist(new_serviceextinfo);
    serviceextinfo_list.add( new_serviceextinfo );
    
    logger.trace( "exiting " + cn + ".add_serviceextinfo" );
    
    return new_serviceextinfo;
}
	



/******************************************************************/
/******************** OBJECT SEARCH FUNCTIONS *********************/
/******************************************************************/

    /*
     * given a timeperiod name and a starting point, find a timeperiod from the
     * list in memory
     */
    public static objects_h.timeperiod find_timeperiod(String name) {
        logger.trace( "entering " + cn + ".find_timeperiod()");

        Object result = null;
        if (name != null && timeperiod_hashlist != null) 
            result = timeperiod_hashlist.get(name);

        logger.trace( "exiting " + cn + ".find_timeperiod()");
        return (objects_h.timeperiod) result;
    }

    /* given a host name, find it in the list in memory */
    public static objects_h.host find_host(String name) {
        logger.trace( "entering " + cn + ".find_host()");
        
        Object result = null;
        if (name != null && host_hashlist != null) 
            result = host_hashlist.get(name);
        
        logger.trace( "exiting " + cn + ".find_host()");
        return (objects_h.host) result;
    }
    
    /* find a hostgroup from the list in memory */
    public static objects_h.hostgroup find_hostgroup(String name){
        logger.trace( "entering " + cn + ".find_hostgroup()");
        
        Object result = null;
        if (name != null && hostgroup_hashlist != null) 
            result = hostgroup_hashlist.get(name);
        
        logger.trace( "exiting " + cn + ".find_hostgroup()");
        return (objects_h.hostgroup) result;
    }
    
////#ifdef REMOVED_061803
    /* find a member of a host group */
    public static objects_h.hostgroupmember find_hostgroupmember(String name, objects_h.hostgroup grp){
        logger.trace( "entering " + cn + ".find_hostgroupmember()");
        
        Object result = null;
        if( name != null && grp != null ) {
            ListIterator iterator = grp.members.listIterator();
            while( iterator.hasNext() && result == null ){
                objects_h.hostgroupmember temp_member = (objects_h.hostgroupmember) iterator.next();
                if ( name.equals( temp_member.host_name ) ) 
                    result = temp_member;
            }
        }
        logger.trace( "exiting " + cn + ".find_hostgroupmember()");
        return (objects_h.hostgroupmember) result;
    }
// #endif
    

/* find a servicegroup from the list in memory */
public static objects_h.servicegroup find_servicegroup( String name){
    logger.trace( "entering " + cn + ".find_servicegroup()");
    
    Object result = null;
    if( name!= null && servicegroup_hashlist != null ) {
        result = servicegroup_hashlist.get( name );
    }
    
    logger.trace( "exiting " + cn + ".find_servicegroup()");
    return (objects_h.servicegroup) result;
}

// #ifdef REMOVED_0618003
/* find a member of a service group */
public static objects_h.servicegroupmember find_servicegroupmember( String host_name, String svc_description, objects_h.servicegroup grp){
    logger.trace( "entering " + cn + ".find_servicegroupmember()");

    Object result = null;
	if(host_name!=null && svc_description!=null && grp!= null) {
	    ListIterator iterator = grp.members.listIterator();
        while ( iterator.hasNext() && result == null ) {
            objects_h.servicegroupmember temp_member = (objects_h.servicegroupmember) iterator.next();
            if ( host_name.equals( temp_member.host_name ) && svc_description.equals( temp_member.service_description ) )
                result = temp_member;
        }
    }

    logger.trace( "exiting " + cn + ".find_servicegroupmember()");
	return (objects_h.servicegroupmember) result;
}
    
/* find a contact from the list in memory */
public static objects_h.contact find_contact( String name){
    logger.trace( "entering " + cn + ".find_contact()");

    Object result = null;
	if(name!= null && contact_hashlist!= null) {
     result = contact_hashlist.get( name );   
    }

    logger.trace( "exiting " + cn + ".find_contact()");
	return (objects_h.contact) result;
	}


/* find a contact group from the list in memory */
public static objects_h.contactgroup find_contactgroup(String name){
    logger.trace( "entering " + cn + ".find_contactgroup()");
    Object result = null;
	if( name!=null && contactgroup_hashlist!=null )
	    result = contactgroup_hashlist.get( name );

    logger.trace( "exiting " + cn + ".find_contactgroup()");
	return (objects_h.contactgroup) result;
	}


/* find the corresponding member of a contact group */
public static objects_h.contactgroupmember find_contactgroupmember( String name, objects_h.contactgroup grp){
    logger.trace( "entering " + cn + ".find_contactgroupmember()");
    
    objects_h.contactgroupmember result = null;
    if(name != null && grp != null ) {
       for ( objects_h.contactgroupmember temp_member : grp.members ) {
          if ( name.equals( temp_member.contact_name ) ) {
             result = temp_member;
             break;
          }
       }
    }
    
    logger.trace( "exiting " + cn + ".find_contactgroupmember()");
    return result;
}


/* given a command name, find a command from the list in memory */
public static objects_h.command find_command(String name){
    logger.trace( "entering " + cn + ".find_command");

    Object result = null;
    if (name != null && command_hashlist != null) 
        result = command_hashlist.get(name);

    logger.trace( "exiting " + cn + ".find_command");
    return (objects_h.command) result;
}


/* given a host/service name, find the service in the list in memory */
public static objects_h.service find_service( String host_name, String svc_desc){
    logger.trace( "entering " + cn + ".find_service");
    Object result = null;
    if( host_name!=null && svc_desc!=null && service_hashlist!=null ) {
        result = service_hashlist.get( host_name + "." + svc_desc );
    }
    logger.trace( "exiting " + cn + ".find_service()");
    return (objects_h.service) result;
}



/* find the extended information for a given host */
public static objects_h.hostextinfo find_hostextinfo(String host_name){
    logger.trace( "entering " + cn + ".find_hostextinfo()");
    
    Object result = null;
    if(host_name!=null && hostextinfo_hashlist!= null ) {
        result = hostextinfo_hashlist.get( host_name );
    }

    logger.trace( "exiting " + cn + ".find_hostextinfo()");
    return (objects_h.hostextinfo) result;
}


/* find the extended information for a given service */
public static objects_h.serviceextinfo find_serviceextinfo(String host_name, String description){
    logger.trace( "entering " + cn + ".find_serviceextinfo()");
    
    Object result = null;
    if(host_name!=null && description!=null && serviceextinfo_hashlist!=null ) {
        result = serviceextinfo_hashlist.get( host_name + "." + description );
    }

    logger.trace( "exiting " + cn + ".find_serviceextinfo()");
    return (objects_h.serviceextinfo) result;
}



//
///******************************************************************/
///******************* OBJECT TRAVERSAL FUNCTIONS *******************/
///******************************************************************/

public static ArrayList<objects_h.hostescalation> get_hostescalation_list_by_dependent_host (String host_name) {
    Object o = hostescalation_hashlist.get( host_name );
    if ( o == null )
        return new ArrayList<objects_h.hostescalation>();
    else if ( o instanceof ArrayList )
        return (ArrayList<objects_h.hostescalation>) o;
    else {
        ArrayList<objects_h.hostescalation> list = new ArrayList<objects_h.hostescalation>();
        list.add( (objects_h.hostescalation) o );
        return list;
    }
}

//
//hostescalation *get_first_hostescalation_by_host(String host_name){
//
//	return get_next_hostescalation_by_host(host_name,null);
//        }
//
//
//hostescalation *get_next_hostescalation_by_host(String host_name, hostescalation *start){
//	hostescalation *temp_hostescalation;
//
//	if(host_name==null || hostescalation_hashlist==null)
//		return null;
//
//	if(start==null)
//		temp_hostescalation=hostescalation_hashlist[hashfunc1(host_name,HOSTESCALATION_HASHSLOTS)];
//	else
//		temp_hostescalation=start.nexthash;
//
//	for(;temp_hostescalation && compare_hashdata1(temp_hostescalation.host_name,host_name)<0;temp_hostescalation=temp_hostescalation.nexthash);
//
//	if(temp_hostescalation && compare_hashdata1(temp_hostescalation.host_name,host_name)==0)
//		return temp_hostescalation;
//
//	return null;
//        }
//

public static ArrayList<objects_h.serviceescalation> get_serviceescalation_list_by_service (String host_name, String svc_description) {
    String key = host_name + "." + svc_description;    
    Object o = serviceescalation_hashlist.get( key );
    if ( o == null )
        return new ArrayList<objects_h.serviceescalation>();
    else if ( o instanceof ArrayList )
        return (ArrayList<objects_h.serviceescalation>) o;
    else {
        ArrayList<objects_h.serviceescalation> list = new ArrayList<objects_h.serviceescalation>();
        list.add( (objects_h.serviceescalation) o );
        return list;
    }
}
//
//serviceescalation *get_first_serviceescalation_by_service(String host_name, String svc_description){
//
//  return get_next_serviceescalation_by_service(host_name,svc_description,null);
//      }
//
//serviceescalation *get_next_serviceescalation_by_service(String host_name, String svc_description, serviceescalation *start){
//	serviceescalation *temp_serviceescalation;
//
//	if(host_name==null || svc_description==null || serviceescalation_hashlist==null)
//		return null;
//
//	if(start==null)
//		temp_serviceescalation=serviceescalation_hashlist[hashfunc2(host_name,svc_description,SERVICEESCALATION_HASHSLOTS)];
//	else
//		temp_serviceescalation=start.nexthash;
//
//	for(;temp_serviceescalation && compare_hashdata2(temp_serviceescalation.host_name,temp_serviceescalation.description,host_name,svc_description)<0;temp_serviceescalation=temp_serviceescalation.nexthash);
//
//	if(temp_serviceescalation && compare_hashdata2(temp_serviceescalation.host_name,temp_serviceescalation.description,host_name,svc_description)==0)
//		return temp_serviceescalation;
//
//	return null;
//        }
//
//

public static ArrayList<objects_h.hostdependency> get_hostdependency_list_by_dependent_host (String host_name) {
    Object o = hostdependency_hashlist.get( host_name );
    if ( o == null )
        return new ArrayList<objects_h.hostdependency>();
    else if ( o instanceof ArrayList )
        return (ArrayList<objects_h.hostdependency>) o;
    else {
        ArrayList<objects_h.hostdependency> list = new ArrayList<objects_h.hostdependency>();
        list.add( (objects_h.hostdependency) o );
        return list;
    }
}

//public static objects_h.hostdependency get_first_hostdependency_by_dependent_host(String host_name){
//
//	return get_next_hostdependency_by_dependent_host(host_name,null);
//        }
//
//
//public static objects_h.hostdependency get_next_hostdependency_by_dependent_host(String host_name, objects_h.hostdependency start){
//	hostdependency *temp_hostdependency;
//
//	if(host_name==null || hostdependency_hashlist==null)
//		return null;
//
//	if(start==null)
//		temp_hostdependency=hostdependency_hashlist[hashfunc1(host_name,HOSTDEPENDENCY_HASHSLOTS)];
//	else
//		temp_hostdependency=start.nexthash;
//
//	for(;temp_hostdependency && compare_hashdata1(temp_hostdependency.dependent_host_name,host_name)<0;temp_hostdependency=temp_hostdependency.nexthash);
//
//	if(temp_hostdependency && compare_hashdata1(temp_hostdependency.dependent_host_name,host_name)==0)
//		return temp_hostdependency;
//
//	return null;
//        }
//

public static ArrayList<objects_h.servicedependency> get_servicedependency_list_by_dependent_host (String host_name, String svc_description) {
    String key = host_name + "." + svc_description;    
    Object o = servicedependency_hashlist.get( key );
    if ( o == null )
        return new ArrayList<objects_h.servicedependency>();
    else if ( o instanceof ArrayList )
        return (ArrayList<objects_h.servicedependency>) o;
    else {
        ArrayList<objects_h.servicedependency> list = new ArrayList<objects_h.servicedependency>();
        list.add( (objects_h.servicedependency) o );
        return list;
    }
}
//servicedependency *get_next_servicedependency_by_dependent_service(String host_name, String svc_description, servicedependency *start){
//servicedependency *temp_servicedependency;
//
//if(host_name==null || svc_description==null || servicedependency_hashlist==null)
//  return null;
//
//if(start==null)
//  temp_servicedependency=servicedependency_hashlist[hashfunc2(host_name,svc_description,SERVICEDEPENDENCY_HASHSLOTS)];
//else
//  temp_servicedependency=start.nexthash;
//
//for(;temp_servicedependency && compare_hashdata2(temp_servicedependency.dependent_host_name,temp_servicedependency.dependent_service_description,host_name,svc_description)<0;temp_servicedependency=temp_servicedependency.nexthash);
//
//if(temp_servicedependency && compare_hashdata2(temp_servicedependency.dependent_host_name,temp_servicedependency.dependent_service_description,host_name,svc_description)==0)
//  return temp_servicedependency;
//
//return null;
//    }
//

/******************************************************************/
/********************* OBJECT QUERY FUNCTIONS *********************/
/******************************************************************/

/* determines whether or not a specific host is an immediate child of another host */
public static int is_host_immediate_child_of_host(objects_h.host parent_host,objects_h.host child_host){

    if(child_host==null)
        return common_h.FALSE;
    
    if(parent_host==null){
        if(child_host.parent_hosts==null || child_host.parent_hosts.size() == 0)
            return common_h.TRUE;
    } else if ( child_host.parent_hosts != null ){
        for ( ListIterator iter = child_host.parent_hosts.listIterator(); iter.hasNext(); ) {
            objects_h.hostsmember temp_hostsmember = (objects_h.hostsmember) iter.next();
            if ( parent_host.name.equals( temp_hostsmember.host_name ) )
                return common_h.TRUE;
        }
    }
    
    return common_h.FALSE;
}


/* determines whether or not a specific host is an immediate child (and the primary child) of another host */
public static int is_host_primary_immediate_child_of_host(objects_h.host parent_host, objects_h.host child_host){

	if(is_host_immediate_child_of_host(parent_host,child_host)==common_h.FALSE)
		return common_h.FALSE;

	if(parent_host==null)
		return common_h.TRUE;

	if(child_host==null)
		return common_h.FALSE;

	if(child_host.parent_hosts==null || child_host.parent_hosts.size() == 0 )
		return common_h.TRUE;

    objects_h.hostsmember temp_hostsmember = (objects_h.hostsmember) child_host.parent_hosts.get( child_host.parent_hosts.size() - 1 );
	
	if( temp_hostsmember.host_name.equals( parent_host.name))
		return common_h.TRUE;

	return common_h.FALSE;
        }



/* determines whether or not a specific host is an immediate parent of another host */
public static int is_host_immediate_parent_of_host(objects_h.host child_host, objects_h.host parent_host){

    return is_host_immediate_child_of_host(parent_host,child_host);
	}


/* returns a count of the immediate children for a given host */
public static int number_of_immediate_child_hosts(objects_h.host hst){
	int children=0;

	for(objects_h.host temp_host : host_list ){
		if(is_host_immediate_child_of_host(hst,temp_host)==common_h.TRUE)
			children++;
		}

	return children;
	}


/* returns a count of the total children for a given host */
public static int number_of_total_child_hosts(objects_h.host hst){
	int children=0;

    for(objects_h.host temp_host : host_list ){
		if(is_host_immediate_child_of_host(hst,temp_host)==common_h.TRUE)
			children+=number_of_total_child_hosts(temp_host)+1;
		}

	return children;
	}
   
   
   /* get the number of immediate parent hosts for a given host */
   public static int number_of_immediate_parent_hosts(objects_h.host hst){
      int parents=0;
   
      for(objects_h.host temp_host : host_list ){
         if(is_host_immediate_parent_of_host(hst,temp_host)==common_h.TRUE){
            parents++;
            break;
         }
      }
   
      return parents;
   }
//
//
///* get the total number of parent hosts for a given host */
//int number_of_total_parent_hosts(host *hst){
//	int parents=0;
//	host *temp_host;
//
//	for(temp_host=host_list;temp_host!=null;temp_host=temp_host.next){
//		if(is_host_immediate_parent_of_host(hst,temp_host)==TRUE){
//			parents+=number_of_total_parent_hosts(temp_host)+1;
//			break;
//		        }
//	        }
//
//	return parents;
//        }
//
//
/*  tests whether a host is a member of a particular hostgroup */
public static int is_host_member_of_hostgroup(objects_h.hostgroup group, objects_h.host hst){
    objects_h.hostgroupmember temp_hostgroupmember;
    
    if(group== null || hst== null)
        return common_h.FALSE;
    
    for ( ListIterator iter = group.members.listIterator(); iter.hasNext(); ) {
        temp_hostgroupmember = (objects_h.hostgroupmember) iter.next();
        if ( temp_hostgroupmember.host_name.equals( hst.name ))
            return common_h.TRUE;
    }
    
    return common_h.FALSE;
}

/*  tests whether a host is a member of a particular servicegroup */
public static int is_host_member_of_servicegroup(objects_h.servicegroup group, objects_h.host hst){
	if(group==null || hst==null)
		return common_h.FALSE;

	for(objects_h.servicegroupmember temp_servicegroupmember : (ArrayList<objects_h.servicegroupmember>) group.members){
		if( temp_servicegroupmember.host_name.equals( hst.name))
			return common_h.TRUE;
	        }

	return common_h.FALSE;
        }


/*  tests whether a service is a member of a particular servicegroup */
public static int is_service_member_of_servicegroup(objects_h.servicegroup group, objects_h.service svc){
    objects_h.servicegroupmember temp_servicegroupmember;

	if(group==null || svc==null)
		return common_h.FALSE;

    for (ListIterator iter = group.members.listIterator(); iter.hasNext(); ) {
        temp_servicegroupmember = (objects_h.servicegroupmember) iter.next();
        if ( temp_servicegroupmember.host_name.equals( svc.host_name ) && temp_servicegroupmember.service_description.equals( svc.description ) )
            return common_h.TRUE;
    }
    
    return common_h.FALSE;
}

/*  tests whether a contact is a member of a particular contactgroup */
public static int is_contact_member_of_contactgroup(objects_h.contactgroup group, objects_h.contact cntct){
    
    if(group==null || cntct==null)
        return common_h.FALSE;
    
    /* search all contacts in this contact group */
    for ( objects_h.contactgroupmember temp_contactgroupmember : group.members ) {
        
        /* we found the contact! */
        if(temp_contactgroupmember.contact_name.equals( cntct.name ) )
            return common_h.TRUE;
    }
    
    return common_h.FALSE;
}

///*  tests whether a contact is a member of a particular hostgroup - used only by the CGIs */
//int is_contact_for_hostgroup(hostgroup *group, contact *cntct){
//	hostgroupmember *temp_hostgroupmember;
//	host *temp_host;
//
//	if(group==null || cntct==null)
//		return FALSE;
//
//	for(temp_hostgroupmember=group.members;temp_hostgroupmember!=null;temp_hostgroupmember=temp_hostgroupmember.next){
//		temp_host=find_host(temp_hostgroupmember.host_name);
//		if(temp_host==null)
//			continue;
//		if(is_contact_for_host(temp_host,cntct)==TRUE)
//			return TRUE;
//	        }
//
//	return FALSE;
//        }
//
//
//
///*  tests whether a contact is a member of a particular servicegroup - used only by the CGIs */
//int is_contact_for_servicegroup(servicegroup *group, contact *cntct){
//	servicegroupmember *temp_servicegroupmember;
//	service *temp_service;
//
//	if(group==null || cntct==null)
//		return FALSE;
//
//	for(temp_servicegroupmember=group.members;temp_servicegroupmember!=null;temp_servicegroupmember=temp_servicegroupmember.next){
//		temp_service=find_service(temp_servicegroupmember.host_name,temp_servicegroupmember.service_description);
//		if(temp_service==null)
//			continue;
//		if(is_contact_for_service(temp_service,cntct)==TRUE)
//			return TRUE;
//	        }
//
//	return FALSE;
//        }
//
//
//
/*  tests whether a contact is a contact for a particular host */
    public static boolean is_contact_for_host(objects_h.host hst, objects_h.contact cntct){
	
	if(hst== null || cntct== null)
		return false;

	/* search all contact groups of this host */
    for ( objects_h.contactgroupsmember temp_contactgroupsmember : hst.contact_groups ) {

		/* find the contact group */
		objects_h.contactgroup temp_contactgroup=  find_contactgroup(temp_contactgroupsmember.group_name);
		if(temp_contactgroup==null)
			continue;

		if(is_contact_member_of_contactgroup(temp_contactgroup,cntct)==common_h.TRUE)
			return true;
	        }

	return false;
        }
    
    /* tests whether or not a contact is an escalated contact for a particular host */
    public static int is_escalated_contact_for_host(objects_h.host hst, objects_h.contact cntct){
        objects_h.contactgroup temp_contactgroup;
        
        
        /* search all host escalations */
        for ( objects_h.hostescalation temp_hostescalation : get_hostescalation_list_by_dependent_host( hst.name )) {
            
            /* search all the contact groups in this escalation... */
            for ( objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>)  temp_hostescalation.contact_groups ) {
                
                /* find the contact group */
                temp_contactgroup=find_contactgroup(temp_contactgroupsmember.group_name);
                if(temp_contactgroup==null)
                    continue;
                
                /* see if the contact is a member of this contact group */
                if(is_contact_member_of_contactgroup(temp_contactgroup,cntct)==common_h.TRUE)
                    return common_h.TRUE;
            }
        }
        
        return common_h.FALSE;
    }

    /*  tests whether a contact is a contact for a particular service */
    public static boolean is_contact_for_service(objects_h.service svc, objects_h.contact cntct){
        
        if(svc==null || cntct==null)
            return false;
        
        /* search all contact groups of this service */
        for ( objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>) svc.contact_groups ) {
            
            /* find the contact group */
            objects_h.contactgroup temp_contactgroup = find_contactgroup(temp_contactgroupsmember.group_name);
            if(temp_contactgroup==null)
                continue;
            
            if( is_contact_member_of_contactgroup(temp_contactgroup,cntct)== common_h.TRUE)
                return true;
        }
        
        return false;
    }
    
    /* tests whether or not a contact is an escalated contact for a particular service */
    public static int is_escalated_contact_for_service(objects_h.service svc, objects_h.contact cntct){
        objects_h.contactgroup temp_contactgroup;
        
        /* search all the service escalations */
        for ( objects_h.serviceescalation temp_serviceescalation : get_serviceescalation_list_by_service( svc.host_name, svc.description )) {
            
            /* search all the contact groups in this escalation... */
            for ( objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>)  temp_serviceescalation.contact_groups ) {
                
                /* find the contact group */
                temp_contactgroup=find_contactgroup(temp_contactgroupsmember.group_name);
                if(temp_contactgroup==null)
                    continue;
                
                /* see if the contact is a member of this contact group */
                if(is_contact_member_of_contactgroup(temp_contactgroup,cntct)==common_h.TRUE)
                    return common_h.TRUE;
            }
        }
        
        return common_h.FALSE;
    }

//#ifdef NSCORE

/* checks to see if there exists a circular parent/child path for a host */
public static int check_for_circular_path(objects_h.host root_hst, objects_h.host hst){
    
    /* don't go into a loop, don't bother checking anymore if we know this host already has a loop */
    if(root_hst.contains_circular_path==common_h.TRUE)
        return common_h.TRUE;

    /* host has already been checked - there is a path somewhere, but it may not be for this particular host... */
    /* this should speed up detection for some loops */
    /* UPDATED 2.2 */
    if(hst.circular_path_checked==common_h.TRUE) {
        root_hst.contains_circular_path = common_h.TRUE;
        hst.contains_circular_path = common_h.TRUE;
        return common_h.TRUE;
    }
    
    /* set the check flag so we don't get into an infinite loop */
    hst.circular_path_checked=common_h.TRUE;


    /* check this hosts' parents to see if a circular path exists */
    if( is_host_immediate_parent_of_host(root_hst,hst) == common_h.TRUE)
        return common_h.TRUE;
    
    /* check all immediate children for a circular path */
    for ( ListIterator iterator=host_list.listIterator(); iterator.hasNext(); ) {
        objects_h.host temp_host = (objects_h.host) iterator.next();
        if(is_host_immediate_child_of_host(hst,temp_host)==common_h.TRUE)
            if(check_for_circular_path(root_hst,temp_host)==common_h.TRUE)
                return common_h.TRUE;
    }
    return common_h.FALSE;
}


/* checks to see if there exists a circular dependency for a service */
public static int check_for_circular_servicedependency(objects_h.servicedependency root_dep, objects_h.servicedependency dep, int dependency_type){

	if(root_dep==null || dep==null )
		return common_h.FALSE;

	/* this is not the proper dependency type */
	if(root_dep.dependency_type!=dependency_type || dep.dependency_type!=dependency_type)
		return common_h.FALSE;


    /* don't go into a loop, don't bother checking anymore if we know this dependency already has a loop */
    /* UPDATED 2.2 */
    if(root_dep.contains_circular_path==common_h.TRUE)
        return common_h.TRUE;
    
	/* dependency has already been checked */
	/* changed to return TRUE - this should speed up detection for some loops (although they are not necessary loops for the root dep) - EG 05/29/03 */
	if(dep.circular_path_checked == common_h.TRUE)
		return common_h.TRUE;

	/* set the check flag so we don't get into an infinite loop */
    /* UDPATED 2.2 */
	dep.circular_path_checked=common_h.TRUE;

	/* is this service dependent on the root service? */
    /* UPDATED 2.2 */
	if(dep!=root_dep){
	    if ( root_dep.dependent_host_name.equals( dep.host_name ) && root_dep.dependent_service_description.equals( dep.service_description ) ) {
            root_dep.contains_circular_path= common_h.TRUE;
            dep.contains_circular_path= common_h.TRUE;
            return common_h.TRUE;
        }
	}
	
	/* notification dependencies are ok at this point as long as they don't inherit */
	if(dependency_type==common_h.NOTIFICATION_DEPENDENCY && dep.inherits_parent==common_h.FALSE)
	    return common_h.FALSE;
	
	/* check all parent dependencies */
	for ( ListIterator iter = servicedependency_list.listIterator(); iter.hasNext(); ) {
	    objects_h.servicedependency temp_sd = (objects_h.servicedependency) iter.next();
	    if ( !dep.host_name.equals( temp_sd.dependent_host_name ) || !dep.service_description.equals( temp_sd.dependent_service_description ) )
	        continue;
	    if(check_for_circular_servicedependency(root_dep,temp_sd,dependency_type)==common_h.TRUE)
	        return common_h.TRUE;
	}
	
	return common_h.FALSE;
}


/* checks to see if there exists a circular dependency for a host */
public static int check_for_circular_hostdependency(objects_h.hostdependency root_dep, objects_h.hostdependency dep, int dependency_type){
    
    if(root_dep==null|| dep==null)
        return common_h.FALSE;
    
    /* this is not the proper dependency type */
    if(root_dep.dependency_type!=dependency_type || dep.dependency_type!=dependency_type)
        return common_h.FALSE;
    
    /* don't go into a loop, don't bother checking anymore if we know this dependency already has a loop */
    /* UPDATED 2.2 */
    if(root_dep.contains_circular_path==common_h.TRUE)
        return common_h.TRUE;
    
    /* dependency has already been checked - there is a path somewhere, but it may not be for this particular dep... */
    /* this should speed up detection for some loops */
    /* UPDATED 2.2 */
    if(dep.circular_path_checked==common_h.TRUE)
        return common_h.FALSE;
    
    /* set the check flag so we don't get into an infinite loop */
    /* UPDATED 2.2 */
    dep.circular_path_checked=common_h.TRUE;
    
    /* is this host dependent on the root host? */
    /* UPDATED 2.2 */
    if(dep!=root_dep){
        if ( root_dep.dependent_host_name.equals( dep.host_name ) ) {
            root_dep.contains_circular_path= common_h.TRUE;
            dep.contains_circular_path= common_h.TRUE;
            return common_h.TRUE;
        }
    }
    
    /* notification dependencies are ok at this point as long as they don't inherit */
    if(dependency_type==common_h.NOTIFICATION_DEPENDENCY && dep.inherits_parent==common_h.FALSE)
        return common_h.FALSE;
    
    /* check all parent dependencies */
    for ( ListIterator iter = hostdependency_list.listIterator(); iter.hasNext(); ) {
        objects_h.hostdependency temp_hd = (objects_h.hostdependency) iter.next();
        if  (!dep.host_name.equals( temp_hd.dependent_host_name )) 
            continue;
        if(check_for_circular_hostdependency(root_dep,temp_hd,dependency_type)==common_h.TRUE)
            return common_h.TRUE;
    }
    
    return common_h.FALSE;
}

//#endif




/******************************************************************/
/******************* OBJECT DELETION FUNCTIONS ********************/
/******************************************************************/

public static void free_object_data () {
   host_list.clear();
   service_list.clear();
   contact_list.clear();
   contactgroup_list.clear();
   hostgroup_list.clear();
   servicegroup_list.clear();
   command_list.clear();
   timeperiod_list.clear();
   serviceescalation_list.clear();
   servicedependency_list.clear();
   hostdependency_list.clear();
   hostescalation_list.clear();
   hostextinfo_list.clear();
   serviceextinfo_list.clear();

   host_hashlist.clear();
   service_hashlist.clear();
   command_hashlist.clear();
   timeperiod_hashlist.clear();
   contact_hashlist.clear();
   contactgroup_hashlist.clear();
   hostgroup_hashlist.clear();
   servicegroup_hashlist.clear();
   hostextinfo_hashlist.clear();
   serviceextinfo_hashlist.clear();
   hostdependency_hashlist.clear();
   servicedependency_hashlist.clear();
   hostescalation_hashlist.clear();
   serviceescalation_hashlist.clear();

}

}