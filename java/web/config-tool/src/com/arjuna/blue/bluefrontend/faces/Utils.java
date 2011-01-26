package com.arjuna.blue.bluefrontend.faces;

import javax.faces.context.FacesContext;

import org.jdom.Namespace;

/*
 * A Class for common methods & variables shared between the beans.
 */

public class Utils 
{
	
	private static FacesContext context;
	private static AccountHandler handler;
	
	public static String[] elementOrder = new String[]{"log_file","cfg_file","object_cache_file","resource_file",
			"temp_file","status_file","aggregate_status_updates","status_update_interval","nagios_user","nagios_group",
			"enable_notifications","execute_service_checks","accept_passive_service_checks","execute_host_checks","accept_passive_host_checks",
			"enable_event_handlers","log_rotation_method","log_archive_path","check_external_commands","command_check_interval","command_file",
			"downtime_file","comment_file","lock_file","retain_state_information","state_retention_file","retention_update_interval","use_retained_program_state",
			"use_retained_scheduling_info","use_syslog","log_notifications","log_service_retries","log_host_retries","log_event_handlers","log_initial_states",
			"log_external_commands","log_passive_checks","global_host_event_handler","global_service_event_handler","sleep_time",
			"service_inter_check_delay_method","max_service_check_spread","service_interleave_factor","max_concurrent_checks","service_reaper_frequency",
			"host_inter_check_delay_method","max_host_check_spread","interval_length","auto_reschedule_checks","auto_rescheduling_interval","auto_rescheduling_window",
			"use_aggressive_host_checking","enable_flap_detection","low_service_flap_threshold","high_service_flap_threshold","soft_state_dependencies","service_check_timeout",
			"host_check_timeout","event_handler_timeout","notification_timeout","ocsp_timeout","ochp_timeout","perfdata_timeout","obsess_over_services",
			"ocsp_command","obsess_over_hosts","ochp_command","process_performance_data","host_perfdata_command","service_perfdata_command","host_perfdata_file",
			"service_perfdata_file","host_perfdata_file_template","service_perfdata_file_template","host_perfdata_file_mode","service_perfdata_file_mode",
			"host_perfdata_file_processing_interval","service_perfdata_file_processing_interval","host_perfdata_file_processing_command","service_perfdata_file_processing_command",
			"check_for_orphaned_services","check_service_freshness","service_freshness_check_interval","check_host_freshness","host_freshness_check_interval","date_format",
			"illegal_object_name_chars","illegal_macro_output_chars","use_regexp_matching","use_true_regexp_matching","admin_email","admin_pager"};
	
	/* XML File locations for object data */
	public static String serviceXMLFile = "services.xml";
	public static String hostXMLFile = "hosts.xml";
	public static String contactXMLFile = "contacts.xml";
	public static String commandXMLFile = "commands.xml";
	public static String timeperiodXMLFile = "timeperiods.xml";
	public static String contactgroupXMLFile = "contactgroups.xml";
	public static String hostgroupXMLFile = "hostgroups.xml";
	public static String servicegroupXMLFile = "servicegroups.xml";
	public static String serviceescalationXMLFile = "serviceescalations.xml";
	public static String hostescalationXMLFile = "hostescalations.xml";
	public static String hostextinfoXMLFile = "hostextinfo.xml";
	public static String serviceextinfoXMLFile = "serviceextinfo.xml";
	public static String hostdependencyXMLFile = "hostdependencies.xml";
	public static String servicedependencyXMLFile = "servicedependencies.xml";
	
	/* Array containing the names of all XML documents */
	
	public static String[] xmlFileLocations = new String[]{
			"hosts.xml",
			"hostgroups.xml",
			"services.xml",
			"servicegroups.xml",
			"contacts.xml",
			"contactgroups.xml",
			"timeperiods.xml",
			"commands.xml",
			"servicedependencies.xml",
			"serviceescalations.xml",
			"hostdependencies.xml",
			"hostescalations.xml",
			"hostextinfo.xml",
			"serviceextinfo.xml"};
	
	/* Schema Host & Schema Context. We're shipping schemas with everything at the mo, so schema host is localhost. */
	public static String schemaHost = "http://localhost:8080";
	public static String schemaContext = "/blue-config/ns/";
	
	/* Array of all available Namespaces */
	public static Namespace[] nameSpaces = new Namespace[]{
			Namespace.getNamespace("hosts",schemaHost + schemaContext + "hosts"),
			Namespace.getNamespace("hostgroups",schemaHost + schemaContext + "hostgroups"),
			Namespace.getNamespace("services",schemaHost + schemaContext + "services"),
			Namespace.getNamespace("servicegroups",schemaHost + schemaContext + "servicegroups"),
			Namespace.getNamespace("contacts",schemaHost + schemaContext + "contacts"),
			Namespace.getNamespace("contactgroups",schemaHost + schemaContext + "contactgroups"),
			Namespace.getNamespace("timeperiods",schemaHost + schemaContext + "timeperiods"),
			Namespace.getNamespace("commands",schemaHost + schemaContext + "commands"),
			Namespace.getNamespace("servicedependencies",schemaHost + schemaContext + "servicedependency"),
			Namespace.getNamespace("serviceescalations",schemaHost + schemaContext + "serviceescalation"),
			Namespace.getNamespace("hostdependencies",schemaHost + schemaContext + "hostdependency"),
			Namespace.getNamespace("hostescalations",schemaHost + schemaContext + "hostescalation"),
			Namespace.getNamespace("hostextinfos",schemaHost + schemaContext + "hostextinfo"),
			Namespace.getNamespace("serviceextinfos",schemaHost + schemaContext + "serviceextinfo")};
	
	public static String booleanToString(boolean toChange)
	{
		if(toChange)
		{
			return "1";
		}
		
		return "0";
	}
	
	public static String arrayToString(String[] arrayToConvert)
	{
	
		String line = "";
		int counter = 1;
		
		for(String s: arrayToConvert)
		{
			if(counter == arrayToConvert.length)
			{
				line += s;
			}
			else
			{
				line += s + ",";
			}
			
			counter++;
		}
		
		return line;
	}
	
	public static boolean stringToBoolean(String toChange)
	{
		if(toChange.trim().equals("1"))
		{
			return true;
		}
		
		return false;
	}
	
	/*
	 * Simple method for deciding if we are currently running the wizard.
	 * 
	 * @return boolean, true if the wizard is currently running.
	 */
	
	public static boolean inWizard()
	{
		try
		{
			context = FacesContext.getCurrentInstance();
			handler = (AccountHandler)context.getExternalContext().getApplicationMap().get("accountHandler");
			
			if(handler.getInWizard())
				return true;
		}
		catch(Exception e)
		{}
		
		return false;
		
	}
	
	public static String getCurrentOutputLocation()
	{
		String outputLocation;
		try
		{
			context = FacesContext.getCurrentInstance();
			handler = (AccountHandler)context.getExternalContext().getApplicationMap().get("accountHandler");
			outputLocation = handler.getOutputLocation();
			
			/* If the user has ignored me and not set the directory, I need to set it myself */

			if(outputLocation.equals(""))
			{
				handler.setOutputLocation("blue-config");
				handler.setUserSetDirectory(true);
				outputLocation = "blue-config";
			}
			outputLocation = outputLocation.replace("\\","/");
			return outputLocation;
		}
		catch(Exception e)
		{
			return "";
		}
	}
}
