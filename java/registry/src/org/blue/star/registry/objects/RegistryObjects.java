package org.blue.star.registry.objects;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.registry.common.RegistryUtils;
import org.blue.star.registry.exceptions.RegistryConfigurationException;
import org.blue.star.registry.exceptions.UnknownObjectException;
import org.blue.star.registry.xod.RegistryXODTemplate;

/**
 * <p>This Class is used as a store of all objects that are known to the Registry.</p>
 * 
 * <p>The Registry uses this store to fetch the details of the objects it needs to build
 * out the configuration files for dynamically adding hosts/services to Blue.</p>
 * 
 * <p>This class borrows heavily from the object store associated with Blue as the operations
 * are similar to the objects defined by Blue</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 */

public class RegistryObjects
{
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.registry.RegistryObjects");
	private static String cn = "org.blue.registry.RegistryObjects";
	
	/** Object Stores */
	private static HashMap<String,DynamicTemplate> dynamic_template_hashmap = new HashMap<String,DynamicTemplate>();
	private static HashMap<String,objects_h.host> hostHashmap = new HashMap<String,objects_h.host>();
	private static HashMap<String,objects_h.service> serviceHashmap = new HashMap<String,objects_h.service>();
	private static HashMap<String,objects_h.contactgroup> contactgroupHashmap = new HashMap<String,objects_h.contactgroup>();
	private static HashMap<String,objects_h.hostgroup> hostgroupHashmap = new HashMap<String,objects_h.hostgroup>();
	
	/**
	 * This method is used to add a DynamicTemplate to the DynamicTemplate HashList.
	 * @param dynamicTemplate - The DynamicTemplate to add.
	 * @return - boolean, true if the Template was successfully added.
	 */
	public static boolean addDynamicTemplateToHashList(DynamicTemplate dynamicTemplate)
	{
		if(dynamicTemplate == null)
			return false;
		
		if(dynamic_template_hashmap.containsKey(dynamicTemplate.getRemoteTemplateName()))
		{
			logger.fatal("Error: Could not add dupliate Dynamic Template '" + dynamicTemplate.getRemoteTemplateName()+ "'");
			return false;
		}
		
		dynamic_template_hashmap.put(dynamicTemplate.getRemoteTemplateName(),dynamicTemplate);
		return true;
	}
	
	/**
	 * This method is used to add a host object to the host hashlist.
	 * @param host - The host that is to be added.
	 * @return - boolean, true if the host was successfully added.
	 */
	public static boolean addHostToHashList(objects_h.host host)
	{
		if(host == null)
			return false;
		
		if(hostHashmap.containsKey(host.name))
		{
			logger.fatal("Error: Could not add duplicate Host '" + host.name + "'");
			return false;
		}
		
		hostHashmap.put(host.name,host);
		return true;
	}
	
	/**
	 * This method is used to add a service to the service hashlist.
	 * @param service - The service to be added.
	 * @return - boolean, true if the service was successfully added.
	 */
	public static boolean addServiceToHashList(objects_h.service service)
	{
		if(service == null)
			return false;
		
		if(serviceHashmap.containsKey(service.description))
		{
			logger.fatal("Error: Could not add duplicate Service '" + service.description + "'");
			return false;
		}
		
		serviceHashmap.put(service.description,service);
		
		return true;
	}
	
	/**
	 * This method is used to add a contact group to the contact group hashlist
	 * @param contactgroup - The contact group to add
	 * @return - true if the contact group was successfully added.
	 */
	public static boolean addContactGroupToHashList(objects_h.contactgroup contactgroup)
	{
		if(contactgroup == null)
			return false;
		
		if(contactgroupHashmap.containsKey(contactgroup.group_name))
		{
			logger.fatal("Error: Could not add duplicate Contact Group '" + contactgroup.group_name + "'");
			return false;
		}

		contactgroupHashmap.put(contactgroup.group_name,contactgroup);
		return true;
	}
	
	/**
	 * This method is used to add a HostGroup to the Host Group hash list
	 * @param hostgroup - The hostgroup to add.
	 * @return - boolean, true if the host group was successfully added.
	 */
	public static boolean addHostGroupToHashList(objects_h.hostgroup hostgroup)
	{
		if(hostgroup == null)
			return false;
		
		if(hostgroupHashmap.containsKey(hostgroup.group_name))
		{
			logger.fatal("Error: Could not add duplicate Host Group '" + hostgroup.group_name + "'");
			return false;
		}
				
		hostgroupHashmap.put(hostgroup.group_name,hostgroup);
		return true;
	}
	
	/**
	 * This method is used to add a DynamicTemplate object to our object store.
	 * @param remoteTemplateName - the remote template name of this dynamic template
	 * @param usesHost - The host this dynamic template uses.
	 * @param runsServices - The services that this dynamic template runs.
	 * @param contactGroup - The contact groups associated with this dynamic template.
	 * @param joinsHostGroup - The host group that this dynamic template joins
	 * @param persistRegistration - Flag for persistence of this DynamicTemplate.
	 * @return - boolean, true if the template was successfully added to the object store.
	 */	
	public static boolean addDynamicTemplate(String remoteTemplateName, String usesHost,String runsServices,String contactGroup,String joinsHostGroup,int persistRegistration)
	{
		logger.trace("Entering " + cn + ".addDynamicTemplate");
		
		DynamicTemplate dynamicTemplate;
		
		/* Verify that we have the minimum needed */
		if(remoteTemplateName == null || usesHost == null || runsServices == null || contactGroup == null)
		{
			logger.fatal("remote_template_name, uses_host, runs_services or contact_groups is null");
			return false;
		}
		
		try
		{
			retrieveDynamicTemplate(remoteTemplateName);
			logger.fatal("Remote Template '" + remoteTemplateName + "' has already been defined");
			return false;
		}
		catch(UnknownObjectException e)
		{
			// NOTE: This is reverse logic. If the template has not been defined, then an Exception
			// is thrown, and in this scenario we simply wish to carry on.
		}
			
		dynamicTemplate = new DynamicTemplate();
		dynamicTemplate.setRemoteTemplateName(remoteTemplateName.trim());
		dynamicTemplate.setUsesHost(usesHost.trim());
		dynamicTemplate.setRunsServices(runsServices.split(","));
		dynamicTemplate.setContactGroups(contactGroup.split(","));
		
		if(joinsHostGroup != null)
			dynamicTemplate.setJoinsHostGroup(joinsHostGroup.trim());
		
		dynamicTemplate.setPersistRegistration(RegistryUtils.intToBoolean(persistRegistration));
		
		if(!addDynamicTemplateToHashList(dynamicTemplate))
		{
			logger.fatal("Error: could not allocate memory in Dynamic Template List for template '" + dynamicTemplate.getRemoteTemplateName()+ "'");
			return false;
		}
		
		logger.trace("Leaving " + cn + ".addDynamicTemplate");
		return true;
	}
	
	/**
	 * This method is used to add a contact group to our object store.
	 * @param name - The name of the group
	 * @param alias - The alias of the group
	 * @param members - The members of the group
	 * @return - the Added Contactgroup object.
	 */	
	public static objects_h.contactgroup addContactGroup(String name,String alias,String members)
	{
	    logger.trace( "entering " + cn + ".add_contactgroup" );
	    
	    /* make sure we have the data we need */
	    if(name==null || alias==null || members == null)
	    {
	        logger.fatal("Error: Contactgroup name,alias or members is null\n");
	        return null;
	    }
	    
	    name = name.trim();
	    alias = alias.trim();
	    members = members.trim();
	    
	    if(name.length() == 0 || alias.length() == 0 || members.length() == 0)
	    {
	        logger.fatal("Error: Contactgroup name,alias or members is null\n");
	        return null;
	    }
	    
	    /* make sure there isn't a contactgroup by this name added already */
	    if(findContactGroup(name))
	    {
	        logger.fatal(  "Error: Contactgroup '"+name+"' has already been defined");
	        return null;
	    }
	    
	    /* allocate memory for a new contactgroup entry */
	    objects_h.contactgroup new_contactgroup = new objects_h.contactgroup();
	    new_contactgroup.group_name=name;
	    new_contactgroup.alias=alias;
	    
	    /* We just add our members as an ArrayList of Strings */
	    if(members != null)
	    {
	    	ArrayList<objects_h.contactgroupmember> groupmembers = new ArrayList<objects_h.contactgroupmember>();
	    	
	    	 for(String s: members.split(","))
	    	 {
	    		 objects_h.contactgroupmember member = new objects_h.contactgroupmember();
	    		 member.contact_name = s;
	    		 groupmembers.add(member);
	    	 }
	    	
	    	new_contactgroup.members = groupmembers;
	    }
	    
	    /* add new contactgroup to contactgroup chained hash list */
	    addContactGroupToHashList(new_contactgroup);
	    return new_contactgroup;
	}
	
	/**
	 * This method is used to add a hostgroup object to our datastore.
	 * 
	 * @param name - The name of the hostgroup to add.
	 * @param alias - The alias of the hostgroup to add.
	 * @param members - The members of the hostgroup to add.
	 * @return - the newly added hostgroup object.
	 */
	public static objects_h.hostgroup addHostGroup(String name,String alias,String members)
	{
	    logger.trace( "entering " + cn + ".addHostGroup" );
	    
	    /* make sure we have the data we need */
	    if(name==null || alias==null || members == null)
	    {
	        logger.fatal("Error: Hostgroup name,alias or members is null\n");
	        return null;
	    }
	    
	    name = name.trim();
	    alias = alias.trim();
	    members = members.trim();
	    
	    if(name.length() == 0 || alias.length() == 0 || members.length() == 0)
	    {
	        logger.fatal("Error: Hostgroup name,alias or members is null\n");
	        return null;
	    }
	    
	    /* make sure there isn't a hostgroup by this name added already */
	    if(findHostGroup(name))
	    {
	        logger.fatal("Error: Hostgroup '"+name+"' has already been defined");
	        return null;
	    }
	    
	    /* allocate memory for a new hostgroup entry */
	    objects_h.hostgroup hostgroup = new objects_h.hostgroup();
	    hostgroup.group_name = name;
	    hostgroup.alias =  alias;
	    
	    if(members != null)
	    {
	    	ArrayList<objects_h.hostgroupmember> groupmembers = new ArrayList<objects_h.hostgroupmember>();
	    	
	    	for(String s: members.split(","))
	    	{
	    		objects_h.hostgroupmember member = new objects_h.hostgroupmember();
	    		member.host_name = s;
	    		groupmembers.add(member);
	    	}
	    	
	    	hostgroup.members = groupmembers;
	    }
	    
	    /* add new contactgroup to contactgroup chained hash list */
	    addHostGroupToHashList(hostgroup);
	    return hostgroup;
	}
	
	/**
	 * This method is taken directly from org.blue.star.common.objects.
	 * return - The newly Added host object.
	 */
	public static objects_h.host addHost(String name, String alias, String address, String check_period, int check_interval, int max_attempts, int notify_up, int notify_down, int notify_unreachable, int notify_flapping, int notification_interval, String notification_period, int notifications_enabled, String check_command, int checks_enabled, int accept_passive_checks, String event_handler, int event_handler_enabled, int flap_detection_enabled, double low_flap_threshold, double high_flap_threshold, int stalk_up, int stalk_down, int stalk_unreachable, int process_perfdata, int failure_prediction_enabled, String failure_prediction_options, int check_freshness, int freshness_threshold, int retain_status_information, int retain_nonstatus_information, int obsess_over_host,String contactGroups)
	{

		logger.trace( "entering " + cn + ".add_host");

		/* make sure we have the data we need */
		if(name==null || alias==null || address==null ){
		    logger.fatal("Error: Host name, alias, or address is null\n");
		    return null;
		}
		
	    name = name.trim();
		alias = alias.trim();
		address = address.trim();
		if (check_command != null ) check_command = check_command.trim();
		if (event_handler != null ) event_handler = event_handler.trim();
		if (notification_period != null) notification_period = notification_period.trim();

		if ( name.length() == 0 || alias.length() == 0 || address.length() == 0 ) {
		    logger.fatal( "Error: Host name, alias, or address is null\n");
		    return null;
		}
		
		/* make sure there isn't a host by this name already added */
		if(findHost(name))
		{
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
		new_host.max_attempts=max_attempts;
		
		/* We add our contact groups as an array list of strings */
		if(contactGroups != null)
		{
			ArrayList<objects_h.contactgroupsmember> contacts = new ArrayList<objects_h.contactgroupsmember>();
			
			for(String s: contactGroups.split(","))
			{
				objects_h.contactgroupsmember member = new objects_h.contactgroupsmember();
				member.group_name = s;
				contacts.add(member);
			}
			
			new_host.contact_groups = contacts;
			
		}
		
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
		new_host.checks_enabled=(checks_enabled>0)?common_h.TRUE:common_h.FALSE;
		new_host.notifications_enabled=(notifications_enabled>0)?common_h.TRUE:common_h.FALSE;
		
		/* add new host to host chained hash list */
		if(!addHostToHashList(new_host))
		{
		    logger.fatal( "Error: Could not allocate memory for host list to add host '"+name+"'");
		    return null;
		}
				
		return new_host;
	}
	
	/**
	 * This method is taken directly from org.blue.star.common.objects.
	 * @return - The newly added service object.
	 */
	public static objects_h.service addService(String host_name, String description, String check_period, int max_attempts, int parallelize, int accept_passive_checks, int check_interval, int retry_interval, int notification_interval, String notification_period, int notify_recovery, int notify_unknown, int notify_warning, int notify_critical, int notify_flapping, int notifications_enabled, int is_volatile, String event_handler, int event_handler_enabled, String check_command, int checks_enabled, int flap_detection_enabled, double low_flap_threshold, double high_flap_threshold, int stalk_ok, int stalk_warning, int stalk_unknown, int stalk_critical, int process_perfdata, int failure_prediction_enabled, String failure_prediction_options, int check_freshness, int freshness_threshold, int retain_status_information, int retain_nonstatus_information, int obsess_over_service,String contactgroup)
	{
		logger.trace("entering " + cn + ".add_service");

			/* make sure we have everything we need */
			if(description==null || check_command==null)
			{
				logger.fatal(  "Error: Service description or check command is null");
				return null;
			 }

			/* We always set the hostname to be blank */
			host_name = ""; 
			description = description.trim();
			check_command = check_command.trim();
			
			if(event_handler != null)
				event_handler = event_handler.trim();
			
			if(notification_period != null)
				notification_period = notification_period.trim();
			
			if(check_period != null)
				check_period = check_period.trim();

			if(description.length() == 0 || check_command.length() == 0)
			{
				logger.fatal(  "Error: Service description, host name, or check command is null");
				return null;
			}

			/* make sure there isn't a service by this name added already */
			if(findService(description))
			{
				logger.fatal(  "Error: Service '" + description + "' has already been defined");
				return null;
			}

			/* Perform checks on our service values */
			if(description.length()>objects_h.MAX_SERVICEDESC_LENGTH-1)
			{
				logger.fatal(  "Error: Name of service ' " + description + " ' on host '" + host_name + "' exceeds maximum length of " + (objects_h.MAX_SERVICEDESC_LENGTH-1) + " characters");
				return null;
			}

			if(parallelize<0 || parallelize>1)
			{
				logger.fatal(  "Error: Invalid parallelize value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}

			if(accept_passive_checks<0 || accept_passive_checks>1)
			{
				logger.fatal(  "Error: Invalid accept_passive_checks value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(event_handler_enabled<0 || event_handler_enabled>1)
			{
				logger.fatal(  "Error: Invalid event_handler_enabled value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(checks_enabled<0 || checks_enabled>1)
			{
				logger.fatal("Error: Invalid checks_enabled value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(notifications_enabled<0 || notifications_enabled>1)
			{
				logger.fatal("Error: Invalid notifications_enabled value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(max_attempts<=0 || check_interval<0 || retry_interval<=0 || notification_interval<0)
			{
				logger.fatal("Error: Invalid max_attempts, check_interval, retry_interval, or notification_interval value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(notify_recovery<0 || notify_recovery>1)
			{
				logger.fatal("Error: Invalid notify_recovery value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(notify_critical<0 || notify_critical>1)
			{
				logger.fatal("Error: Invalid notify_critical value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(notify_flapping<0 || notify_flapping>1)
			{
				logger.fatal("Error: Invalid notify_flapping value ' " + notify_flapping + " ' for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(notify_recovery<0 || notify_recovery>1)
			{
				logger.fatal("Error: Invalid notify_recovery value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(is_volatile<0 || is_volatile>1)
			{
				logger.fatal("Error: Invalid is_volatile value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(flap_detection_enabled<0 || flap_detection_enabled>1)
			{
				logger.fatal("Error: Invalid flap_detection_enabled value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(stalk_ok<0 || stalk_ok>1)
			{
				logger.fatal("Error: Invalid stalk_ok value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(stalk_warning<0 || stalk_warning>1)
			{
				logger.fatal(  "Error: Invalid stalk_warning value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(stalk_unknown<0 || stalk_unknown>1)
			{
				logger.fatal(  "Error: Invalid stalk_unknown value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(stalk_critical<0 || stalk_critical>1)
			{
				logger.fatal(  "Error: Invalid stalk_critical value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(process_perfdata<0 || process_perfdata>1)
			{
				logger.fatal(  "Error: Invalid process_perfdata value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(failure_prediction_enabled<0 || failure_prediction_enabled>1)
			{
				logger.fatal(  "Error: Invalid failure_prediction_enabled value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(check_freshness<0 || check_freshness>1)
			{
				logger.fatal(  "Error: Invalid check_freshness value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
		    }
			if(freshness_threshold<0)
			{
				logger.fatal(  "Error: Invalid freshness_threshold value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(retain_status_information<0 || retain_status_information>1)
			{
				logger.fatal(  "Error: Invalid retain_status_information value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(retain_nonstatus_information<0 || retain_nonstatus_information>1)
			{
				logger.fatal(  "Error: Invalid retain_nonstatus_information value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}
			if(obsess_over_service<0 || obsess_over_service>1)
			{
				logger.fatal(  "Error: Invalid obsess_over_service value for service ' " + description + " ' on host '" + host_name + "'");
				return null;
			}

			objects_h.service new_service= new objects_h.service();
			new_service.host_name=host_name;
			new_service.description=description;
			new_service.service_check_command=check_command;
			new_service.event_handler=event_handler;
			new_service.notification_period=notification_period;
			new_service.check_period=check_period;
			new_service.failure_prediction_options=failure_prediction_options;
			
			/* We simply add the names of our contact groups */
			if(contactgroup != null)
			{
				ArrayList<objects_h.contactgroupsmember> contacts = new ArrayList<objects_h.contactgroupsmember>();
				
				for(String s: contactgroup.split(","))
				{
					objects_h.contactgroupsmember member = new objects_h.contactgroupsmember();
					member.group_name = s;
					contacts.add(member);
				}
				
				new_service.contact_groups = contacts;
			}
			
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
			
			/* add new service to service chained hash list */
			addServiceToHashList(new_service);
			
			return new_service;
	}
	
	
	/**
	 * This method is used to return the details of a specific dynamic template.
	 * @param remoteTemplateName - The remote template name that identifies this dynamic template.
	 * @return - The DynamicTemplate Object.
	 */
	public static DynamicTemplate retrieveDynamicTemplate(String remoteTemplateName) throws UnknownObjectException
	{
		logger.trace("Entering " + cn + ".findDynamicTemplate");
		if(dynamic_template_hashmap.get(remoteTemplateName) == null)
			throw new UnknownObjectException("Unknown Dynamic Template Object '" + remoteTemplateName + "'");
		
		return dynamic_template_hashmap.get(remoteTemplateName);
	}
	
	/**
	 * This method is used to verify that a particular host exists.
	 * @param hostname - the name of the host to verify.
	 * @return - boolean, true if the host exists.
	 */
	public static boolean findHost(String hostname)
	{
		if(hostname == null || hostHashmap == null)
			return false;
		
		return hostHashmap.containsKey(hostname);
	}
	
	/**
	 * Method to return a host object from the object store
	 * @param hostname - The name of the host to retrieve.
	 * @return - The host object.
	 * @throws - UnknownObjectException - thrown if the object cannot be found
	 */
	public static objects_h.host retrieveHost(String hostname) throws UnknownObjectException
	{
		if(hostHashmap.get(hostname) == null)
			throw new UnknownObjectException("Unknown Host Object '" + hostname + "'");
		
		return hostHashmap.get(hostname);
	}
	
	/**
	 * This method checks to see if a service with a given service description actually exists.
	 * @param serviceDescription - The service description to check for.
	 * @return  - boolean, true if the service exists.
	 */
	public static boolean findService(String serviceDescription)
	{
		if(serviceDescription == null || serviceHashmap == null)
			return false;
		
		return serviceHashmap.containsKey(serviceDescription);
	}
	
	/**
	 * This method is used to retrieve a given service object.
	 * @param serviceDescription - The service description to identify the service object.
	 * @return - The service object, null if not found.
	 * @throws - UnknownObjectException - Thrown if the object cannot be found.
	 */
	public static objects_h.service retrieveService(String serviceDescription) throws UnknownObjectException
	{
		if(serviceHashmap.get(serviceDescription) == null)
			throw new UnknownObjectException("Unknown Service Object '" + serviceDescription + "'");
		
		return serviceHashmap.get(serviceDescription);
	}
	
	/**
	 * Method used to check for the existence of a given hostgroup
	 * @param groupName - The name of the hostgroup to check for.
	 * @return - boolean, true if the hostgroup exists.
	 */
	public static boolean findHostGroup(String groupName)
	{
		if(groupName == null || hostgroupHashmap == null)
			return false;
		
		return hostgroupHashmap.containsKey(groupName);
	}
	
	/**
	 * This method is used to retrieve a given host group object identified by the group name.
	 * @param groupName - the group name used to identify the host group.
	 * @return - The hostgroup object if found, null otherwise.
	 * @throws - UnknownObjectException - thrown if the object cannot be found.
	 */
	public static objects_h.hostgroup retrieveHostGroup(String groupName) throws UnknownObjectException
	{
		if(hostgroupHashmap.get(groupName) == null)
			throw new UnknownObjectException("Unknown HostGroup object '" + groupName + "'");
		
		return hostgroupHashmap.get(groupName);
	}
	
	/**
	 * This method is used to verify that a given contact group exists.
	 * @param groupName - The name of the group to check that exists.
	 * @return - boolean, true if the group exists.
	 */
	public static boolean findContactGroup(String groupName)
	{
		if(groupName == null || contactgroupHashmap == null)
			return false;
		
		return contactgroupHashmap.containsKey(groupName);
	}
	
	/**
	 * This method is used to find a particular contactgroup object.
	 * @param groupName - The name of the contactgroup object to find.
	 * @return - the contactgroup object if found, null otherwise.
	 * @throws UnknownObjectException - thrown if the object cannot be found.
	 */
	public static objects_h.contactgroup retrieveContactGroup(String groupName) throws UnknownObjectException
	{
		if(contactgroupHashmap.get(groupName) == null)
			throw new UnknownObjectException("Unknown Contact Group Object '" + groupName + "'");
		
		return contactgroupHashmap.get(groupName);
	}
	
	/**
	 * This method instructs the XODTemplate system to read in any configuration files.
	 * @param configurationFile - The configuration file to read.
	 * @return - int, common_h.OK if everything passed.
	 */
	public static void readObjectConfigurationData(String configurationFile) throws RegistryConfigurationException
	{
	    RegistryXODTemplate.readObjectConfiguration(configurationFile);
	}
	
	/**
	 * This method is used to check the number of DynamicTemplates we have available.
	 * @return - int, the number of dynamicTempaltes available.
	 */
	public static int getDynamicTemplateCount()
	{
		return dynamic_template_hashmap.size();
	}
	
	/**
	 * This method is used to see if a certain agent type is made persistent.
	 * 
	 * @param agentType - The agent type to see if it should be made persistent.
	 * @return - boolean, true if this DynamicTemplate type is persistent.
	 */
	public static boolean persistAgentType(String agentType)
	{
		if(agentType == null || !dynamic_template_hashmap.containsKey(agentType))
			return false;
		
		return dynamic_template_hashmap.get(agentType).isPersistRegistration();
	}
}