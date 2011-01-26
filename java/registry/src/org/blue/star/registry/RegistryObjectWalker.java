package org.blue.star.registry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.objects_h;
import org.blue.star.include.common_h;
import org.blue.star.registry.exceptions.ConfigurationGenerationException;
import org.blue.star.registry.exceptions.UnknownObjectException;
import org.blue.star.registry.objects.DynamicTemplate;
import org.blue.star.registry.objects.RegistryObjects;
import org.blue.star.registry.xod.RegistryXODTemplate;

/**
 * <p>This class generates configuration files for remote hosts that are trying to register.
 * It takes a dynamic_template name, the hostname of the remote host and the ipaddress of 
 * the remote host. The will replace the host_name, and address variables of the objects_h.host
 * objects with these values, thereby creating a dynamic configuration.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 */

public class RegistryObjectWalker
{
	/** Logging variables */
	private static Logger logger = LogManager.getLogger("org.blue.registry.RegistryObjectWalker");
	private static String cn = "org.blue.star.registry.RegistryObjectWalker";
	
	/** The Registry Object that we are generating configuration files for */
	private BlueDynamicRegistry registry;
	
	/**
	 * Constructor that takes a Registry as a parameter.
	 * @param registry
	 */
	public RegistryObjectWalker(BlueDynamicRegistry registry)
	{
		this.registry = registry;
	}
	
	/**
	 * This method is used to generate a configuration file for a specific host that is attempting to dynamically
	 * join the Blue monitoring network.
	 * 
	 * @param templateName - The name of the dynamic template to use.
	 * @param hostname - The hostname of the host joining.
	 * @param IPAddress - The IPAddress of the host joining.
	 * @param persistRegistration - I
	 * @return
	 */
	public void addHostAndServicesToBlue(String templateName, String hostname, String IPAddress,boolean persistRegistration) throws ConfigurationGenerationException
	{
		logger.trace("Entering " + cn + ".generateConfigFile");
		
		if(templateName == null || hostname == null || IPAddress == null)
		{
			logger.debug(cn + ".generateConfigFile() - Required Parameters were missing");
			throw new ConfigurationGenerationException("Required Parameters To Generate Configuration File Were Missing");
		}
		
		String configFile = this.generateConfigFileName(templateName,hostname,IPAddress,persistRegistration);
		
		try
		{
		
			DynamicTemplate template = RegistryObjects.retrieveDynamicTemplate(templateName);
			objects_h.host host = RegistryObjects.retrieveHost(template.getUsesHost());
			
			/* Set the host details to be that of the current remote host*/
			host.name = hostname;
			host.alias = hostname;
			host.address = IPAddress;
			
			/* Write out our host details */
			this.writeHostDetails(host,configFile);
			
			/* Walk the list of service Objects associated with this Host */
			this.writeAllServiceDetails(host.name,template.getRunsServices(),configFile);
		}
		catch(UnknownObjectException e)
		{
			logger.debug(cn + ".generateConfigFile() - Dynamic Template references Unknown Object");
			throw new ConfigurationGenerationException(e);
		}
			
		/* Now we need to register the Host with Blue and Enable all Service checks */
		this.registerHostWithBlue(hostname,configFile);
		this.enableAllHostServiceChecks(hostname);
	}
	
	/**
	 * This method is used to enable the services for a given
	 * @param hostname - The name of the host to add these services to.
	 * @param ipAddress - The Ip Address of the machine to which we are adding these services.
	 * @param agentType - The agent type to get the services that should be added from.
	 * @param persistent - Is the host we are adding these services to persistent?
	 * 
	 * @throws - ConfigurationGenerationException - thrown if there is any issue adding the new services to Blue.
	 */
	public void addAgentServicesToHost(String hostname,String ipAddress,String agentType,boolean persistent) throws ConfigurationGenerationException
	{
		if(hostname == null || agentType == null)
		{
			return;
		}
		
		try
		{
			String filename = generateConfigFileName(agentType, hostname,ipAddress,persistent);
			
		    	this.writeAllServiceDetails(hostname,RegistryObjects.retrieveDynamicTemplate(agentType).getRunsServices(),filename);
			this.registerServicesWithBlue(hostname,filename);
			this.enableAllHostServiceChecks(hostname);
		}
		catch(UnknownObjectException e)
		{
			logger.debug(cn + ".addAgentServices() - Unknown Dynamic Template Type");
			throw new ConfigurationGenerationException("Unknown Agent Type Identified",e);
		}
	}
	
	/**
	 * This method is used to write out the details of a host to a given configuration file.
	 * @param host - The host object to write out to file.
	 * @param configFile - The configuration File to write out to.
	 * @throws ConfigurationGenerationException - Thrown if there is a problem writing to the file.
	 */
	private void writeHostDetails(objects_h.host host,String configFile) throws ConfigurationGenerationException
	{
		BufferedWriter pw = null;
		
		try
		{
			pw = new BufferedWriter(new FileWriter(configFile));
			
			pw.write("# AUTOMATICALLY GENERATED CONFIGURATION FILE FROM BLUE DYNAMIC REGISTRY#\n");
			pw.write("\n");			
			pw.write("define host{\n");
			if(host.name != null )
	            pw.write("\thost_name\t"+host.name + "\n");
	        if(host.alias != null )
	            pw.write("\talias\t"+host.alias + "\n");
	        if(host.address != null )
	            pw.write("\taddress\t"+host.address + "\n");
	        if(host.check_period != null )
	            pw.write("\tcheck_period\t"+host.check_period + "\n");
	        if(host.host_check_command != null )
	            pw.write("\tcheck_command\t"+host.host_check_command + "\n");
	        if(host.event_handler != null )
	            pw.write("\tevent_handler\t"+host.event_handler + "\n");
	        if(host.contact_groups != null)
	        {
	        	pw.write("\tcontact_groups\t");
	        	for(ListIterator i = host.contact_groups.listIterator();i.hasNext();)
	        	{
	        		objects_h.contactgroupsmember member = (objects_h.contactgroupsmember)i.next();
	        		
	        		if(i.hasNext())
	        			pw.write(member.group_name + ",");
	        		else
	        			pw.write(member.group_name);
	        	}
	        	pw.write("\n");
	        	pw.flush();
	        }
	        if(host.notification_period != null )
	            pw.write("\tnotification_period\t"+host.notification_period + "\n");
	        if(host.failure_prediction_options != null )
	            pw.write("\tfailure_prediction_options\t"+host.failure_prediction_options + "\n");
	        
	        pw.write("\tcheck_interval\t"+host.check_interval + "\n");
	        pw.write("\tmax_check_attempts\t"+host.max_attempts + "\n");
	        pw.write("\tchecks_enabled\t" + host.checks_enabled + "\n");
	        pw.write("\tactive_checks_enabled\t"+host.checks_enabled + "\n");
	        pw.write("\tpassive_checks_enabled\t"+host.accept_passive_host_checks + "\n");
	        pw.write("\tobsess_over_host\t"+host.obsess_over_host + "\n");
	        pw.write("\tevent_handler_enabled\t"+host.event_handler_enabled + "\n");
	        pw.write("\tlow_flap_threshold\t"+host.low_flap_threshold + "\n");
	        pw.write("\thigh_flap_threshold\t"+host.high_flap_threshold + "\n");
	        pw.write("\tflap_detection_enabled\t"+host.flap_detection_enabled + "\n");
	        pw.write("\tfreshness_threshold\t"+host.freshness_threshold + "\n");
	        pw.write("\tcheck_freshness\t"+host.check_freshness + "\n");
	        pw.write("\tnotification_options\t");
	        int x=0;
	        if(host.notify_on_down==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "d");
	        if(host.notify_on_unreachable==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "u");
	        if(host.notify_on_recovery==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "r");
	        if(host.notify_on_flapping==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "f");
	        if(x==0)
	            pw.write("n");
	        pw.write("\n");
	        pw.write("\tnotifications_enabled\t"+host.notifications_enabled + "\n");
	        pw.write("\tnotification_interval\t"+host.notification_interval + "\n");
	        pw.write("\tstalking_options\t");
	        x=0;
	        if(host.stalk_on_up==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "o");
	        if(host.stalk_on_down==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "d");
	        if(host.stalk_on_unreachable==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "u");
	        if(x==0)
	            pw.write("n");
	        pw.write("\n");
	        pw.write("\tprocess_perf_data\t"+host.process_performance_data + "\n");
	        pw.write("\tfailure_prediction_enabled\t"+host.failure_prediction_enabled + "\n");
	        pw.write("\tretain_status_information\t"+host.retain_status_information + "\n");
	        pw.write("\tretain_nonstatus_information\t"+host.retain_nonstatus_information + "\n");
	        pw.write("}");
	        pw.write("\n\n");
	        
	        /* Flush writer and close */
	        pw.flush();
		}
		catch(Exception e)
		{
			logger.debug("Error: Unable to write Host configuration into config file '" + configFile + "'");
			throw new ConfigurationGenerationException("Unable To Create Configuration File.",e);
		}
		finally
		{
			try
			{
				if(pw != null)
				{
					pw.close();
				}
			}
			catch(IOException e)
			{
				logger.debug(cn + ".writeHostDetails() - Error closing file Handle");
			}
		}
	}
	
	/**
	 * This method is used to write out the details of all services associated with a particular
	 * host to the configuration file.
	 * 
	 * @param hostname - The name of the Host these services run on.
	 * @param serviceList - The list of services to write out.
	 * @param filename - The filename to write out to.
	 * @throws ConfigurationGenerationException - Thrown if there are any issues writing out to file.
	 */
	private void writeAllServiceDetails(String hostname,String[] serviceList,String filename) throws ConfigurationGenerationException
	{
		if(serviceList == null || serviceList.length == 0)
		{
			throw new ConfigurationGenerationException("Cannot Register a Null List of Services");
		}
		
		for(String s: serviceList)
		{
			try
			{
				objects_h.service tempService = RegistryObjects.retrieveService(s);
				tempService.host_name = hostname;
				this.writeServiceDetails(tempService,filename);
			}
			catch(UnknownObjectException e)
			{
				logger.debug(cn + ".writeAllServiceDetails() - Attempting to write Unknown Service Object");
				throw new ConfigurationGenerationException(e);
			}
		}
	}
	
	/**
	 * This method is used to write out the details of the services that should run on the particular host.
	 * @param service - The service object to write out the details for.
	 * @param configFile - The configuration file to write into.
	 */
	private void writeServiceDetails(objects_h.service service,String configFile) throws ConfigurationGenerationException
	{
		logger.trace("Entering " + cn + ".writeServiceDetails");
		
		BufferedWriter pw = null;
		
		try
		{
			pw = new BufferedWriter(new FileWriter(configFile,true));
			
			pw.write("define service{ "+ "\n");
	        if(service.host_name != null )
	            pw.write("\thost_name\t"+service.host_name + "\n");
	        if(service.description != null )
	            pw.write("\tservice_description\t"+service.description + "\n");
	        if(service.check_period != null )
	            pw.write("\tcheck_period\t"+service.check_period + "\n");
	        if(service.service_check_command != null )
	            pw.write("\tcheck_command\t"+service.service_check_command + "\n");
	        if(service.event_handler != null )
	            pw.write("\tevent_handler\t"+service.event_handler + "\n");
	        if(service.contact_groups != null )
	        {
	        	pw.write("\tcontact_groups\t");
	        	
	        	for(ListIterator i = service.contact_groups.listIterator();i.hasNext();)
	        	{
	        		objects_h.contactgroupsmember member = (objects_h.contactgroupsmember)i.next();
	        		
	        		if(i.hasNext())
	        			pw.write(member.group_name + ",");
	        		else
	        			pw.write(member.group_name);
	        	}
	        	pw.write("\n");
	        	pw.flush();
	        }
	        if(service.notification_period != null )
	            pw.write("\tnotification_period\t"+service.notification_period + "\n");
	        if(service.failure_prediction_options != null )
	            pw.write("\tfailure_prediction_options\t"+service.failure_prediction_options + "\n");
	        pw.write("\tnormal_check_interval\t"+service.check_interval + "\n");
	        pw.write("\tretry_check_interval\t"+service.retry_interval + "\n");
	        pw.write("\tmax_check_attempts\t"+service.max_attempts + "\n");
	        pw.write("\tis_volatile\t"+service.is_volatile + "\n");
	        pw.write("\tparallelize_check\t"+service.parallelize + "\n");
	        pw.write("\tactive_checks_enabled\t"+service.checks_enabled + "\n");
	        pw.write("\tpassive_checks_enabled\t"+service.accept_passive_service_checks + "\n");
	        pw.write("\tobsess_over_service\t"+service.obsess_over_service + "\n");
	        pw.write("\tevent_handler_enabled\t"+service.event_handler_enabled + "\n");
	        pw.write("\tlow_flap_threshold\t"+service.low_flap_threshold + "\n");
	        pw.write("\thigh_flap_threshold\t"+service.high_flap_threshold + "\n");
	        pw.write("\tflap_detection_enabled\t"+service.flap_detection_enabled + "\n");
	        pw.write("\tfreshness_threshold\t"+service.freshness_threshold + "\n");
	        pw.write("\tcheck_freshness\t"+service.check_freshness + "\n");
	        pw.write("\tnotification_options\t");
	        int x=0;
	        if(service.notify_on_unknown==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "u");
	        if(service.notify_on_warning==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "w");
	        if(service.notify_on_critical==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "c");
	        if(service.notify_on_recovery==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "r");
	        if(service.notify_on_flapping==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "f");
	        if(x==0)
	            pw.write("n");
	        pw.write("\n");
	        pw.write("\tnotifications_enabled\t"+service.notifications_enabled + "\n");
	        pw.write("\tnotification_interval\t"+service.notification_interval + "\n");
	        pw.write("\tstalking_options\t");
	        x=0;
	        if(service.stalk_on_ok==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "o");
	        if(service.stalk_on_unknown==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "u");
	        if(service.stalk_on_warning==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "w");
	        if(service.stalk_on_critical==common_h.TRUE)
	            pw.write( ((x++>0)?",":"") + "c");
	        if(x==0)
	            pw.write("n");
	        pw.write("\n");
	        pw.write("\tprocess_perf_data\t"+service.process_performance_data + "\n");
	        pw.write("\tfailure_prediction_enabled\t"+ service.failure_prediction_enabled + "\n");
	        pw.write("\tretain_status_information\t"+ service.retain_status_information + "\n");
	        pw.write("\tretain_nonstatus_information\t"+ service.retain_nonstatus_information + "\n");
	        
	        pw.write("\t}");  
	        pw.write("\n\n");
	        
	        /* Flush writer and close */
	        pw.flush();
		}
		catch(Exception e)
		{
			logger.debug("Error: Unable to write Service configuration data into configuration file '" + configFile + "'");
			throw new ConfigurationGenerationException("Unable to Write Service Details to Configuration File",e);
		}
		finally
		{
			try
			{
				if(pw != null)
					pw.close();
			}
			catch(IOException e)
			{
				logger.debug(cn + ".writeServiceDetails() - Unable to Close File Handle");
			}
		}
		
	}
	
	/**
	 * This method is used to register a particular host with Blue.
	 * @param hostName - The name of the host to register.
	 * @param filename - The filename where details of this host can be found.
	 */
	public void registerHostWithBlue(String hostName,String filename) throws ConfigurationGenerationException
	{
		String cmd  = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_host_services;" + filename + ";" + hostName + ";all\n";
		this.writeCommandToExternalCommandFile(cmd);
	}
	
	/**
	 * This method is used to add a list of services to a already present host in Blue.
	 * @param hostname - The name of the host to add the services to.
	 * @param filename - The filename from which to read the service config.
	 * @throws ConfigurationGenerationException - Thrown if there is any issue generating the configuration.
	 */
	public void registerServicesWithBlue(String hostname,String filename) throws ConfigurationGenerationException
	{
		String cmd = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_service;"+filename + ";" + hostname + ";ALL\n";
		this.writeCommandToExternalCommandFile(cmd);
	}
	
	/**
	 * This method is used to register a particular service with Blue.
	 * @param hostname - The name of the host these services are runing on
	 * @param serviceName - The description of the service
	 * @param filename - The filename in which this service is defined.
	 * @throws ConfigurationGenerationException - Thrown if there is an issue registering the service.
	 */
	public void registerServiceWithBlue(String hostname,String serviceName,String filename) throws ConfigurationGenerationException
	{
		String cmd = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_service;" + filename + ";" + hostname + ";" + serviceName + "\n";
		this.writeCommandToExternalCommandFile(cmd);
	}
	
	/**
	 * This method is used to remove all services associated with a particular agent type from a host.
	 * @param hostname - The name of the host to remove the services from
	 * @param agentType - The agent type that defines the services that should be removed.
	 * @throws ConfigurationGenerationException - throw if there is any issue removing the services.
	 */
	public void removeAgentServicesFromHost(String hostname,String agentType) throws ConfigurationGenerationException
	{
		try
		{
			for(String s: RegistryObjects.retrieveDynamicTemplate(agentType).getRunsServices())
			{
				logger.info("Requesting removal of service " + hostname + ":" + s);
				String cmd = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.delete_service;" + hostname + ";" + s + "\n";
				this.writeCommandToExternalCommandFile(cmd);
			}
		}
		catch(UnknownObjectException e)
		{
			logger.debug(cn + ".removeAgentServicesFromHost - Unknown Agent Type");
			throw new ConfigurationGenerationException("Unknown Agent Type To Remove From Host",e);
		}
	}
	
	/**
	 * This method is used to pre-register all objects defined in the registry configuraton files
	 * with Blue.
	 * @throws ConfigurationGenerationException - Thrown if there is any issue registering the objects.
	 */
	public void preRegisterObjects() throws ConfigurationGenerationException
	{
		logger.trace("Entering " + cn + ".preRegisterObjects()");
		
		this.preRegisterCommands();
		this.preRegisterTimePeriods();
		this.preRegisterContacts();
		this.preRegisterContactGroups();

		logger.trace("Exiting " + cn + ".preRegisterObjects()");
	}
	
	/**
	 * This method is used to pre-register and Command objects defined within the Registry configuration
	 * files with Blue.
	 * @throws ConfigurationGenerationException - Thrown if there are any issues pre-registering the command
	 * objects.
	 */
	private void preRegisterCommands() throws ConfigurationGenerationException
	{
		for(Iterator i = RegistryXODTemplate.commandConfigFiles.keySet().iterator();i.hasNext();)
		{
		   	String cmd  = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_command;" + i.next() + ";all\n";
		   	this.writeCommandToExternalCommandFile(cmd);
		}
	}
	
	/**
	 * This Method is used to pre-register any contacts defined in the Dynamic Registry configuraton files
	 * to the Blue Server.
	 * @throws ConfigurationGenerationException - Thrown if there are any issues pre-registering 
	 */
	private void preRegisterContacts() throws ConfigurationGenerationException
	{
		for(Iterator i = RegistryXODTemplate.contactConfigFiles.keySet().iterator();i.hasNext();)
		{
		  	String cmd  = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_contact;" + i.next() + ";all\n";
		   	this.writeCommandToExternalCommandFile(cmd);
		}
	}
	
	/**
	 * This method is used to pre-register any contact groups defined in the Registry configuraton files.
	 * @throws ConfigurationGenerationException - Thrown if there is any issue pre-registering the contact groups.
	 */
	private void preRegisterContactGroups() throws ConfigurationGenerationException
	{
		for(Iterator i = RegistryXODTemplate.contactGroupConfigFiles.keySet().iterator();i.hasNext();)
		{
		   	String cmd  = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_contact_group;" + i.next() + ";all\n";
		   	this.writeCommandToExternalCommandFile(cmd);
		}
	}
	
	/**
	 * This method is used to pre-register any timeperiods defined in the registry configuration
	 * files with Blue.
	 * @throws ConfigurationGenerationException - Thrown if the timeperiods cannot be registered.
	 */
	private void preRegisterTimePeriods() throws ConfigurationGenerationException
	{
		for(Iterator i = RegistryXODTemplate.timeperiodConfigFiles.keySet().iterator();i.hasNext();)
		{
		   	String cmd  = "[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.add_timeperiod;" + i.next() + ";all\n";
		   	this.writeCommandToExternalCommandFile(cmd);
		}
	}
	
	/**
	 * This method is used to generate a configuration file location for a specifc host.
	 * @param templateName - The name of the template being utilised.
	 * @param hostname - The name of the host 
	 * @param IPAddress - The IPAddress of the host.
	 * @param persistRegistration - Is the registration persistent
	 * @return - String, the returned filename.
	 */
	private String generateConfigFileName(String templateName,String hostname,String IPAddress,boolean persistRegistration)
	{
	    if(persistRegistration)
	    {
		return this.registry.getPersistDirectory() + "/" + templateName + "-" + hostname + "-" + IPAddress + "-" + System.currentTimeMillis() + ".cfg";
	    }
		
	    return this.registry.getTempDirectory() + "/" + templateName + "-" + hostname + "-" + IPAddress + "-" + System.currentTimeMillis() + ".cfg";
	}
	
	/**
	 * This method is used to enable all service checks for a given host.
	 * @param hostname - The name of the host to enable service checks for.
	 * @throws ConfigurationGenerationException - thrown if there is any issue enabling service checks
	 * for a particular host.
	 */
	private void enableAllHostServiceChecks(String hostname) throws ConfigurationGenerationException
	{
		this.writeCommandToExternalCommandFile("[" + System.currentTimeMillis() + "] ENABLE_HOST_SVC_CHECKS;" + hostname + "\n");
	}
	
	/**
	 * This method is used to enable Monitoring for a specific Host.
	 * @param hostname - The name of the host to enable monitoring for.
	 * @throws ConfigurationGenerationException - thrown if there is any issue enabling monitoring for this host.
	 */
	public void enableHostMonitoring(String hostname) throws ConfigurationGenerationException
	{
		this.writeCommandToExternalCommandFile("[" + System.currentTimeMillis() + "] ENABLE_HOST_CHECK;" + hostname + "\n");
		this.writeCommandToExternalCommandFile("[" + System.currentTimeMillis() + "] ENABLE_HOST_SVC_CHECKS;" + hostname + "\n");
	}
	
	/**
	 * This method is used to disable the Montoring of a particular Host.
	 * @param hostname - The name of the Host to disable monitoring for.
	 * @throws ConfigurationGenerationException - Thrown if there is an issue disabling monitoring for
	 * this host.
	 */
	public void disableHostMonitoring(String hostname) throws ConfigurationGenerationException
	{
		this.writeCommandToExternalCommandFile("[" + System.currentTimeMillis() + "] DISABLE_HOST_CHECK;" + hostname + "\n");
		this.writeCommandToExternalCommandFile("[" + System.currentTimeMillis() + "] DISABLE_HOST_SVC_CHECKS;" + hostname + "\n");
	}
	
	/**
	 * This method is used to remove a particular Host from the Blue Monitoring setup.
	 * @param hostname - The name of the Host to Remove.
	 * @throws ConfigurationGenerationException - Thrown if there is any issue removing the Host.
	 */
	public void deleteHostFromBlue(String hostname) throws ConfigurationGenerationException
	{
		this.writeCommandToExternalCommandFile("[" + System.currentTimeMillis() + "] EXECUTE_JAVA_COMMAND;org.blue.star.commands.delete_host;" + hostname + "\n");
	}
	
	/**
	 * This method is used to write a command to the External Command File
	 * @param command - The command to write to the External Command File.
	 * @throws ConfigurationGenerationException - Thrown if any issue occurs writing to the External Command File.
	 */
	private void writeCommandToExternalCommandFile(String command) throws ConfigurationGenerationException
	{
		try
		{
			FileChannel channel = new FileOutputStream(new File(this.registry.getCommandFile()),true).getChannel();
			FileLock lock = channel.tryLock();
		
			while (lock == null)
				lock = channel.tryLock();
			
			channel.write(ByteBuffer.wrap(command.getBytes()));
		
			lock.release();
			channel.close();
		}
		catch(FileNotFoundException e)
		{
			logger.debug(cn + ".writeCommandToExternalCommandFile() - Cannot Find External Command File");
			throw new ConfigurationGenerationException("Cannot Locate External Command File to write too",e);
		}
		catch(IOException e)
		{
			logger.debug(cn + ".writeCommandToExternalCommandFile() - Error Writing to External Command File");
			throw new ConfigurationGenerationException("Cannot Write To Specified External Command File",e);
		}
	}
}