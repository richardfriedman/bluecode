package org.blue.star.registry.xod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.common_h;
import org.blue.star.registry.common.RegistryUtils;
import org.blue.star.registry.exceptions.RegistryConfigurationException;
import org.blue.star.registry.exceptions.UnknownObjectException;
import org.blue.star.registry.objects.RegistryObjects;
import org.blue.star.xdata.xodtemplate_h;

/**
 * This class is used to support the Dynamic Registry object definition and configuration
 * system. It is based around the Blue Server xod system and much of the functionality is 
 * borrowed from that class. This class tries to move away from the use of common_h.OK and
 * common_h.ERROR and utilises exceptions instead. 
 * 
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class RegistryXODTemplate
{
	/** List stores for our various object types */
	private static ArrayList<XODDynamicTemplate> xodDynamicTemplateList = new ArrayList<XODDynamicTemplate>();
	private static ArrayList<xodtemplate_h.xodtemplate_host> xodHostList = new ArrayList<xodtemplate_h.xodtemplate_host>();
	private static ArrayList<xodtemplate_h.xodtemplate_service> xodServiceList = new ArrayList<xodtemplate_h.xodtemplate_service>();
	private static ArrayList<xodtemplate_h.xodtemplate_contactgroup> xodContactGroupList = new ArrayList<xodtemplate_h.xodtemplate_contactgroup>();
	private static ArrayList<xodtemplate_h.xodtemplate_hostgroup> xodHostGroupList = new ArrayList<xodtemplate_h.xodtemplate_hostgroup>();
	private static ArrayList<String> xodCommandNameList = new ArrayList<String>();
	private static ArrayList<String> xodTimePeriodNameList = new ArrayList<String>();
	private static ArrayList<String> xodContactNameList = new ArrayList<String>();
	
	
	/** Hashmaps for storing locations of object definitions */
	public static HashMap<String,String> timeperiodConfigFiles = new HashMap<String,String>();
	public static HashMap<String,String> contactConfigFiles = new HashMap<String,String>();
	public static HashMap<String,String> commandConfigFiles = new HashMap<String,String>();
	public static HashMap<String,String> contactGroupConfigFiles = new HashMap<String,String>();
	
	private final static int XODTEMPLATE_DYNAMIC_TEMPLATE = 99;
	
	/** The current object pointer */
	private static Object currentXODObject;
	/** The current object type */
	private static int currentObjectType;
	
	/** Logging */
	private static Logger logger = LogManager.getLogger("org.blue.registry.RegistryXODTemplate");
	private static String cn = "org.blue.registry.RegistryXODTemplate";
	
	/** Config File information */
	private static String config_file;
	private static String currentConfigFile;
	
	
	public static void readObjectConfiguration(String configurationFile) throws RegistryConfigurationException
	{
		BufferedReader reader;
		int lineCount = 0;
		String line;
		String value;
		
		logger.info(System.getProperty("user.dir"));
				
		try
		{
			reader = new BufferedReader(new FileReader(configurationFile));
			line = reader.readLine();
			
			while(line != null)
			{
				line = line.trim();
								
				if(line.length() != 0 && !line.startsWith("#") && !line.startsWith(";"))
				{
					String[] bits = line.split("=",2);
					
					/* Make sure we actually ahve something in the variable/value parings */
					if(bits[0].length() != 0 && bits[1].length() !=0)
					{
						if(bits[0].equals("xodtemplate_config_file") || bits[0].equals("cfg_file"))
						{
							value = bits[1].trim();
						
							if(value.endsWith( "\\") || value.endsWith( "/" ))
							{
								value = value.substring(0,value.length()-1);
							}
				        
							processConfigurationFile(value);
						}
						else if(bits[0].equals("xodtemplate_config_dir") || bits[0].equals("cfg_dir"))
						{
							value = bits[1].trim();
	        	        											        
							/* Remember to strip off any trailing slashes */
							if(value.endsWith( "\\") || value.endsWith( "/" ))
							{
								value = value.substring(0,value.length()-1);
							}
				        
							processConfigurationDir(value) ;
						}
					}
				}
				
				line = reader.readLine();
				lineCount++;
			}
			
			reader.close();
		}
		catch(IOException e)
		{
			throw new RegistryConfigurationException("Could not open configuration file '" + configurationFile + "' for reading");		
		}
		
		/* Resolve all Objects */
		resolveObjects();
		
		/* Register all objects */
		registerObjects();
	}
	
	public static void processConfigurationFile(String filename) throws RegistryConfigurationException
	{
		BufferedReader reader;
		String input;
		int lineCount = 0;
		int result = common_h.OK;
		int in_definition = common_h.FALSE;
		currentConfigFile = filename;
		
		try
		{
			reader = new BufferedReader(new FileReader(filename));
			input = reader.readLine();
			
			
			while(input != null)
			{
				input = input.trim();
				int commentIndex = input.indexOf(';');
	        
				/* If the line does not begin with a comment character */
	        
				if (commentIndex > 0)
				{
					input = input.substring(0,commentIndex);
					input = input.trim();
				}
	        
				/* skip empty/blank lines */
				if(input.length() != 0 && input.charAt(0) !='#' && input.charAt(0) !=';')
				{
								
					if (input.startsWith("define"))
					{
						/* Move 6 characters into the string (leap over 'define' statement) */
						input = input.substring(6).trim();
			    
						/* Find the first index of '{' character, anything before this will be our object type! */
						int bIndex = input.indexOf('{');
	        	
						/* If there is no index of '{' character, we have a malformed object definition. */
						if (bIndex == 0)
						{
							input = "";
						}
						else if (bIndex > 0)
						{
							input = input.substring(0,bIndex).trim();
						}
			    
			    
						if(input.length() == 0)
						{
							logger.fatal( "Error: No object type specified in file '"+filename+"' on line "+lineCount+".");
							throw new RegistryConfigurationException("No Object Type Specified in file '" + filename + "' at Line " + lineCount);
						}
				    
						/* check validity of object type */
						
						if (!input.equals("timeperiod") && !input.equals("command") && !input.equals("contact") && !input.equals( "contactgroup") && !input.equals( "host") && !input.equals( "hostgroup") && !input.equals( "servicegroup") && !input.equals( "service") && !input.equals( "servicedependency") && !input.equals( "serviceescalation") && !input.equals( "hostdependency") && !input.equals( "hostescalation") && !input.equals( "hostextinfo") && !input.equals( "serviceextinfo") && !input.equals("dynamic_template"))
						{
							logger.fatal( "Error: Invalid object definition type "+input+" in file '"+filename+"' on line "+lineCount+".");
							throw new RegistryConfigurationException("Invalid Object Definition in file '" + filename + "' on line " + lineCount);							
						}
			    
						/* we're already in an object definition... */
						if(in_definition == common_h.TRUE)
						{
							logger.fatal( "Error: Unexpected start of object definition in file '"+filename+"' on line "+lineCount+".");
							throw new RegistryConfigurationException("Unexpected start of Object definition in file '" + filename + "' on line " + lineCount);
						}
				    					
						beginObjectDefinition(input,0,lineCount);
						
						/* Identify that we are within the body of an object definition */
						in_definition = common_h.TRUE;
					}
					else if (input.equals("}") && in_definition== common_h.TRUE )
					{
						/* Identify that we are no longer within the body of an object definition */
						in_definition= common_h.FALSE;

						endObjectDefinition();
					}
					else if(in_definition == common_h.TRUE)
					{
						/* Begin to add properties to the objects that we have created. */
						addObjectProperty(input);
					}
					else if(input.startsWith("include_file="))
					{
				    
						int eIndex = input.indexOf('=');
						if (eIndex > 0 && !input.endsWith( "=" ))
						{	
				        	processConfigurationFile(input.substring(eIndex + 1).trim());
						}
					}
					else if( input.startsWith("include_dir"))
					{
						int eIndex = input.indexOf('=');
				    
						if (eIndex > 0 && !input.endsWith("="))
						{
							processConfigurationDir(input.substring(eIndex + 1).trim());
						}
					}
					/* unexpected token or statement */
					else
					{
						logger.fatal("Error: Unexpected token or statement in file '"+filename+"' on line "+lineCount+".");
						throw new RegistryConfigurationException("Unexcepted token or statement in file '" + filename + "' on line " + lineCount);
					}
				}
				input = reader.readLine();
				lineCount++;
			}
		
			reader.close();

			/* whoops - EOF while we were in the middle of an object definition... */
			if(in_definition == common_h.TRUE && result == common_h.OK)
			{
				logger.fatal("Error: Unexpected EOF in file '"+filename+"' on line "+lineCount+" - check for a missing closing bracket.");
				throw new RegistryConfigurationException("Unexpected EOF in file '" + filename + "' on line " + lineCount + " - check for a missing closing bracket");
			}
	
		    logger.trace("exiting " + cn + ".xodtemplate_process_config_file");
		}
		catch(IOException e)
		{
			throw new RegistryConfigurationException("Unable to open Configuration File '" + filename + "' for Reading");
		}
	}
	
	/* Process any Configuration Directory variables we come across */
	public static void processConfigurationDir(String dirname) throws RegistryConfigurationException
	{
		logger.trace( "entering " + cn + ".xodtemplate_process_config_dir" );
	    
	    try
	    {
	        String[] files = new File(dirname).list();
	        
	        if(files != null)
	        {
	            for (int i = 0;i < files.length;i++)
	            {
	                File file = new File(files[i]);
	                
	                /* If the specified file is a directory, recursively call this method! */
	                if(file.isDirectory())
	                {
	                    processConfigurationDir(file.toString());
	                }
	                
	                /* Otherwise process the file using xodtemplate_process_config_file() assuming the suffix
	                 * of the file is .cfg.  Skip hiddent config files ( those which start with '.' )
	                 */
	                else if(files[i].endsWith(".cfg") && !files[i].startsWith( "." ))
	                {
	                    processConfigurationFile(file.toString());
	                }
	            }
	        }
	    }
	    catch (Exception e)
	    {
	        logger.fatal( "Error: Could not open config directory '"+dirname+"' for reading.");
	        throw new RegistryConfigurationException("Could not open Config Directory '" + dirname + "' for Reading");
	    }
	    
	    logger.trace( "exiting " + cn + ".xodtemplate_process_config_dir" );
	}
	
	/* Begin an object definition */
	public static void beginObjectDefinition(String input,int config_file, int start_line)
	{
	    /* Set the type of the object that we are about to create */ 
	    
	    if(input.equals("service"))currentObjectType = xodtemplate_h.XODTEMPLATE_SERVICE;
	    else if(input.equals("host")) currentObjectType = xodtemplate_h.XODTEMPLATE_HOST;
	    else if(input.equals("contactgroup")) currentObjectType = xodtemplate_h.XODTEMPLATE_CONTACTGROUP;
	    else if(input.equals("hostgroup")) currentObjectType = xodtemplate_h.XODTEMPLATE_HOSTGROUP;
	    else if(input.equals("dynamic_template")) currentObjectType = XODTEMPLATE_DYNAMIC_TEMPLATE;
	    else if(input.equals("timeperiod")) currentObjectType = xodtemplate_h.XODTEMPLATE_TIMEPERIOD;
	    else if(input.equals("command")) currentObjectType = xodtemplate_h.XODTEMPLATE_COMMAND;
	    else if(input.equals("contact")) currentObjectType = xodtemplate_h.XODTEMPLATE_CONTACT;
	    
	    /* add a new (blank) object */
	    switch(currentObjectType)
	    {
	    	case xodtemplate_h.XODTEMPLATE_TIMEPERIOD:
	    		if(!timeperiodConfigFiles.containsKey(currentConfigFile))
	    			timeperiodConfigFiles.put(currentConfigFile,currentConfigFile);
	    		break;
	    
	    	case xodtemplate_h.XODTEMPLATE_COMMAND:
	    		
	    		if(!commandConfigFiles.containsKey(currentConfigFile))
	    			commandConfigFiles.put(currentConfigFile,currentConfigFile);
	    		break;
	    
	    	case xodtemplate_h.XODTEMPLATE_CONTACT:
	    	
	    		if(!contactConfigFiles.containsKey(currentConfigFile))
	    			contactConfigFiles.put(currentConfigFile,currentConfigFile);
	    		
	    		break;
	    
	    	case xodtemplate_h.XODTEMPLATE_CONTACTGROUP:
	        
	       	xodtemplate_h.xodtemplate_contactgroup new_contactgroup = new xodtemplate_h.xodtemplate_contactgroup();
	        
	        new_contactgroup.template= null;
	        new_contactgroup.name= null;
	        new_contactgroup.contactgroup_name= null;
	        new_contactgroup.alias= null;
	        new_contactgroup.members= null;
	        new_contactgroup.has_been_resolved= common_h.FALSE;
	        new_contactgroup.register_object= common_h.TRUE;
	        new_contactgroup._config_file=config_file;
	        new_contactgroup._start_line=start_line;
	        
	        /* add new contactgroup to head of list in memory */
	        xodContactGroupList.add(new_contactgroup);
	        
	        /* update current object pointer */
	        currentXODObject = new_contactgroup;
	        if(!contactGroupConfigFiles.containsKey(currentConfigFile))
	        	contactGroupConfigFiles.put(currentConfigFile,currentConfigFile);
	        
	        break;
	        
	       	case xodtemplate_h.XODTEMPLATE_HOST:
	            
	       		/* allocate memory */
	            xodtemplate_h.xodtemplate_host new_host = new xodtemplate_h.xodtemplate_host();
	            
	            new_host.template = null;
	            new_host.name = null;
	            new_host.host_name=null;
	            new_host.alias=null;
	            new_host.address=null;
	            new_host.parents=null;
	            new_host.hostgroups=null;
	            new_host.check_command=null;
	            new_host.check_period=null;
	            new_host.event_handler=null;
	            new_host.contact_groups=null;
	            new_host.notification_period=null;
	            new_host.check_interval=0;
	            new_host.have_check_interval=common_h.FALSE;
	            new_host.active_checks_enabled=common_h.TRUE;
	            new_host.have_active_checks_enabled=common_h.FALSE;
	            new_host.passive_checks_enabled=common_h.TRUE;
	            new_host.have_passive_checks_enabled=common_h.FALSE;
	            new_host.obsess_over_host=common_h.TRUE;
	            new_host.have_obsess_over_host=common_h.FALSE;
	            new_host.max_check_attempts=-2;
	            new_host.have_max_check_attempts=common_h.FALSE;
	            new_host.event_handler_enabled=common_h.TRUE;
	            new_host.have_event_handler_enabled=common_h.FALSE;
	            new_host.check_freshness=common_h.FALSE;
	            new_host.have_check_freshness=common_h.FALSE;
	            new_host.freshness_threshold=0;
	            new_host.have_freshness_threshold=0;
	            new_host.flap_detection_enabled=common_h.TRUE;
	            new_host.have_flap_detection_enabled=common_h.FALSE;
	            new_host.low_flap_threshold=0.0F;
	            new_host.have_low_flap_threshold=common_h.FALSE;
	            new_host.high_flap_threshold=0.0F;
	            new_host.have_high_flap_threshold=common_h.FALSE;
	            new_host.notify_on_down=common_h.FALSE;
	            new_host.notify_on_unreachable=common_h.FALSE;
	            new_host.notify_on_recovery=common_h.FALSE;
	            new_host.notify_on_flapping=common_h.FALSE;
	            new_host.have_notification_options=common_h.FALSE;
	            new_host.notifications_enabled=common_h.TRUE;
	            new_host.have_notifications_enabled=common_h.FALSE;
	            new_host.notification_interval=-2;
	            new_host.have_notification_interval=common_h.FALSE;
	            new_host.stalk_on_up=common_h.FALSE;
	            new_host.stalk_on_down=common_h.FALSE;
	            new_host.stalk_on_unreachable=common_h.FALSE;
	            new_host.have_stalking_options=common_h.FALSE;
	            new_host.process_perf_data=common_h.TRUE;
	            new_host.have_process_perf_data=common_h.FALSE;
	            new_host.failure_prediction_enabled=common_h.TRUE;
	            new_host.have_failure_prediction_enabled=common_h.FALSE;
	            new_host.failure_prediction_options=null;
	            new_host.retain_status_information=common_h.TRUE;
	            new_host.have_retain_status_information=common_h.FALSE;
	            new_host.retain_nonstatus_information=common_h.TRUE;
	            new_host.have_retain_nonstatus_information=common_h.FALSE;
	            new_host.register_object = common_h.TRUE;
	            	            
	            /* add new host to head of list in memory */
	            xodHostList.add(new_host);
	            
	            /* update current object pointer */
	            currentXODObject = new_host;
	            break;
	            
	        case xodtemplate_h.XODTEMPLATE_SERVICE:
	            
	            /* allocate memory */
	            xodtemplate_h.xodtemplate_service new_service= new xodtemplate_h.xodtemplate_service();
	            
	            new_service.template=null;
	            new_service.name=null;
	            new_service.hostgroup_name=null;
	            new_service.host_name=null;
	            new_service.service_description=null;
	            new_service.servicegroups=null;
	            new_service.check_command=null;
	            new_service.check_period=null;
	            new_service.event_handler=null;
	            new_service.notification_period=null;
	            new_service.contact_groups=null;
	            new_service.max_check_attempts=-2;
	            new_service.normal_check_interval=-2;
	            new_service.retry_check_interval=-2;
	            new_service.active_checks_enabled=common_h.TRUE;
	            new_service.passive_checks_enabled=common_h.TRUE;
	            new_service.parallelize_check=common_h.TRUE;
	            new_service.is_volatile=common_h.FALSE;
	            new_service.obsess_over_service=common_h.TRUE;
	            new_service.event_handler_enabled=common_h.TRUE;
	            new_service.check_freshness=common_h.FALSE;
	            new_service.freshness_threshold=0;
	            new_service.flap_detection_enabled=common_h.TRUE;
	            new_service.low_flap_threshold=0.0;
	            new_service.high_flap_threshold=0.0;
	            new_service.notify_on_unknown=common_h.FALSE;
	            new_service.notify_on_warning=common_h.FALSE;
	            new_service.notify_on_critical=common_h.FALSE;
	            new_service.notify_on_recovery=common_h.FALSE;
	            new_service.notify_on_flapping=common_h.FALSE;
	            new_service.notifications_enabled=common_h.TRUE;
	            new_service.notification_interval=-2;
	            new_service.stalk_on_ok=common_h.FALSE;
	            new_service.stalk_on_unknown=common_h.FALSE;
	            new_service.stalk_on_warning=common_h.FALSE;
	            new_service.stalk_on_critical=common_h.FALSE;
	            new_service.process_perf_data=common_h.TRUE;
	            new_service.failure_prediction_enabled=common_h.TRUE;
	            new_service.failure_prediction_options=null;
	            new_service.retain_status_information=common_h.TRUE;
	            new_service.retain_nonstatus_information=common_h.TRUE;
	            new_service.register_object = common_h.TRUE;
	            
	            /* add new service to head of list in memory */
	            xodServiceList.add(new_service);
	            
	            /* update current object pointer */
	            currentXODObject = new_service;
	            break;
	            
	        case xodtemplate_h.XODTEMPLATE_HOSTGROUP:
	        	
	        	xodtemplate_h.xodtemplate_hostgroup hostgroup = new xodtemplate_h.xodtemplate_hostgroup();
	        	
	        	hostgroup.alias = null;
	        	hostgroup.hostgroup_name = null;
	        	hostgroup.hostgroup_name = null;
	        	hostgroup.register_object = common_h.TRUE;
	        	
	        	xodHostGroupList.add(hostgroup);
	        	currentXODObject = hostgroup;
	        	break;
	        	
	        case XODTEMPLATE_DYNAMIC_TEMPLATE:
	        	
	        	XODDynamicTemplate dynamicTemplate = new XODDynamicTemplate();
	        	dynamicTemplate.setRegisterObject(common_h.TRUE);
	        	xodDynamicTemplateList.add(dynamicTemplate);
	        	currentXODObject = dynamicTemplate;
	        	break;
	    }
	}
	
   public static void addObjectProperty(String input) throws RegistryConfigurationException
   {
	
	 String[] inputs = input.split( "\\s", 2 );
	    
	 /* Verify that the variable/value pair is correct (as best we can tell */
	 if(inputs == null || inputs.length != 2 || inputs[1].trim().length() == 0)
	    logger.fatal("Error: NULL variable value in object definition.");
	    
	 /* Set the variable name and the value */
	 String variable = inputs[0].trim();
	 String value = inputs[1].trim();
	
	 switch(currentObjectType)
	 {
	 
	 case xodtemplate_h.XODTEMPLATE_CONTACT:
		 
		 if(variable.equals("contact_name"))
			 xodContactNameList.add(value);
		 break;
		 
	 case xodtemplate_h.XODTEMPLATE_COMMAND:
		 
		 if(variable.equals("command_name"))
			 xodCommandNameList.add(value);
		 
		 break;
		 
	 case xodtemplate_h.XODTEMPLATE_TIMEPERIOD:
		 
		 if(variable.equals("timeperiod_name"))
		 	 xodTimePeriodNameList.add(value);
		 break;
		 
	 case xodtemplate_h.XODTEMPLATE_HOST:
	 
		 xodtemplate_h.xodtemplate_host temp_host=(xodtemplate_h.xodtemplate_host)currentXODObject;
     
		 if(variable.equals("use"))
		 {
			 temp_host.template=value;
		 }
		 else if(variable.equals("name"))
		 {
             try
             {
            	 findHost(value);
                 logger.warn("Warning: Duplicate definition found for host'"+value+")");
             }
             catch(UnknownObjectException e)
             {}
             
             temp_host.name = value;
		 }
		 else if(variable.equals("host_name"))
		 	 temp_host.host_name=value;
		 else if(variable.equals("alias"))
			 temp_host.alias=value;
		 else if(variable.equals("address"))
			 temp_host.address=value;
		 else if(variable.equals("parents"))
			 temp_host.parents=value;
		 else if(variable.equals("hostgroups"))
		 	 temp_host.hostgroups=value;
		 else if(variable.equals("contact_groups"))
			 temp_host.contact_groups=value;
		 else if(variable.equals("notification_period"))
			 temp_host.notification_period=value;
		 else if(variable.equals("check_command"))
			 temp_host.check_command=value;
		 else if(variable.equals("check_period"))
			 temp_host.check_period=value;
		 else if(variable.equals("event_handler"))
		 	 temp_host.event_handler=value;
		 else if(variable.equals("failure_prediction_options"))
			 temp_host.failure_prediction_options=value;
     	 else if(variable.equals("check_interval") || variable.equals("normal_check_interval"))
     	 {
     		 temp_host.check_interval= RegistryUtils.atoi(value);
     		 temp_host.have_check_interval = common_h.TRUE;
     	 }
     	 else if(variable.equals("max_check_attempts"))
     	 {
     		 temp_host.max_check_attempts=RegistryUtils.atoi(value);
     		 temp_host.have_max_check_attempts = common_h.TRUE;
     	 }
         else if(variable.equals("checks_enabled") || variable.equals("active_checks_enabled"))
         {
        	 temp_host.active_checks_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_active_checks_enabled = common_h.TRUE;
         }
         else if(variable.equals("passive_checks_enabled"))
         {
        	 temp_host.passive_checks_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_passive_checks_enabled = common_h.TRUE;
         }
         else if(variable.equals("event_handler_enabled"))
         {
        	 temp_host.event_handler_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_event_handler_enabled = common_h.TRUE;
         }
     	 else if(variable.equals("check_freshness"))
     	 {
     		 temp_host.check_freshness=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
     		 temp_host.have_check_freshness = common_h.TRUE;
     	 }
     	 else if(variable.equals("freshness_threshold"))
     	 {
     		 temp_host.freshness_threshold=RegistryUtils.atoi(value);
     		 temp_host.have_freshness_threshold = common_h.TRUE;
     	 }
         else if(variable.equals("low_flap_threshold"))
         {
        	 temp_host.low_flap_threshold=Float.parseFloat(value);
        	 temp_host.have_low_flap_threshold = common_h.TRUE;
         }
         else if(variable.equals("high_flap_threshold"))
         {
        	 temp_host.high_flap_threshold=Float.parseFloat(value);
        	 temp_host.have_high_flap_threshold = common_h.TRUE;
         }
         else if(variable.equals("flap_detection_enabled"))
         {
         		temp_host.flap_detection_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         		temp_host.have_flap_detection_enabled = common_h.TRUE;
         }
         else if(variable.equals("notification_options"))
         {
        	 String[] split = value.split( "[, ]" );
        	 for(int i = 0; i<split.length; i ++)
        	 {
        		 String temp_ptr = split[i].trim();
        		 if(temp_ptr.equals("d") || temp_ptr.equals("down"))
        			 temp_host.notify_on_down=common_h.TRUE;
        		 else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
        			 temp_host.notify_on_unreachable=common_h.TRUE;
        		 else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
        			 temp_host.notify_on_recovery=common_h.TRUE;
        		 else if(temp_ptr.equals("f") || temp_ptr.equals("flapping"))
        			 temp_host.notify_on_flapping=common_h.TRUE;
        		 else if(temp_ptr.equals("n") || temp_ptr.equals("none"))
        		 {
        			 temp_host.notify_on_down=common_h.FALSE;
        			 temp_host.notify_on_unreachable=common_h.FALSE;
        			 temp_host.notify_on_recovery=common_h.FALSE;
        			 temp_host.notify_on_flapping=common_h.FALSE;
        		 }
        		 else
        		 {
        			 logger.fatal( "Error: Invalid notification option '"+temp_ptr+"' in host definition.");
        			 throw new RegistryConfigurationException("Invalid Notification Option '" + temp_ptr + "' in host deinfition");
        		 }
        	 }
        	 temp_host.have_notification_options = common_h.TRUE;
         }
         else if(variable.equals("notifications_enabled"))
         {
        	 temp_host.notifications_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_notifications_enabled = common_h.TRUE;
         }
		 else if(variable.equals("notification_interval"))
		 {
           	 temp_host.notification_interval=RegistryUtils.atoi(value);
           	 temp_host.have_notification_interval = common_h.TRUE;
		 }
         else if(variable.equals("stalking_options"))
         {
        	 String[] split = value.split( "[, ]" );
        	 for(int i = 0; i<split.length; i ++ )
        	 {
        		 String temp_ptr = split[i].trim();
        		 if(temp_ptr.equals("o") || temp_ptr.equals("up"))
        			 temp_host.stalk_on_up=common_h.TRUE;
        		 else if(temp_ptr.equals("d") || temp_ptr.equals("down"))
        			 temp_host.stalk_on_down=common_h.TRUE;
        		 else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
        			 temp_host.stalk_on_unreachable=common_h.TRUE;
        		 else if(temp_ptr.equals("n") || temp_ptr.equals("none"))
        		 {
        			 temp_host.stalk_on_up=common_h.FALSE;
        			 temp_host.stalk_on_down=common_h.FALSE;
        			 temp_host.stalk_on_unreachable=common_h.FALSE;
        		 }
        		 else
        		 {
        			 logger.fatal( "Error: Invalid stalking option '"+temp_ptr+"' in host definition.");
        			 throw new RegistryConfigurationException("Invalid stalking option '" + temp_ptr + "' in host definition");
        		 }
        		 temp_host.have_stalking_options = common_h.TRUE;
        	 }
         
         }
         else if(variable.equals("process_perf_data"))
         {
        	 temp_host.process_perf_data=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_process_perf_data = common_h.TRUE;
         }
         else if(variable.equals("failure_prediction_enabled"))
         {
        	 temp_host.failure_prediction_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_failure_prediction_enabled = common_h.TRUE;
         }
         else if(variable.equals("obsess_over_host"))
         {
        	 temp_host.obsess_over_host=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_obsess_over_host = common_h.TRUE;
         }
         else if(variable.equals("retain_status_information"))
         {
        	 temp_host.retain_status_information=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_retain_status_information = common_h.TRUE;
         }
         else if(variable.equals("retain_nonstatus_information"))
         {
        	 temp_host.retain_nonstatus_information=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        	 temp_host.have_retain_nonstatus_information = common_h.TRUE;
         }
         else if(variable.equals("register"))
         	 temp_host.register_object=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         else
         {
        	 logger.fatal( "Error: Invalid host object directive '"+variable+"'.");
        	 throw new RegistryConfigurationException("Invalid Host object directive '" + variable + "'");
         }
     break;
     
 case xodtemplate_h.XODTEMPLATE_SERVICE:
     
     xodtemplate_h.xodtemplate_service temp_service = (xodtemplate_h.xodtemplate_service)currentXODObject;
     
     if(variable.equals("use"))
     {
         temp_service.template=value;
     }
     else if(variable.equals("name"))
     {
        try
        {
        	findService(value);
            logger.warn("Warning: Duplicate definition found for contact'"+value+")");
        }
        catch(UnknownObjectException e)
        {}
        
        temp_service.name=value;
     }
     else if(variable.equals("hostgroup") || variable.equals("hostgroups") || variable.equals("hostgroup_name"))
         temp_service.hostgroup_name=value;
     else if(variable.equals("host") || variable.equals("hosts") || variable.equals("host_name"))
         temp_service.host_name=value;
     else if(variable.equals("service_description") || variable.equals("description"))
         temp_service.service_description=value;
     else if(variable.equals("servicegroups"))
         temp_service.servicegroups=value;
     else if(variable.equals("check_command"))
         temp_service.check_command=value;
     else if(variable.equals("check_period"))
         temp_service.check_period=value;
     else if(variable.equals("event_handler"))
         temp_service.event_handler=value;
     else if(variable.equals("notification_period"))
         temp_service.notification_period=value;
     else if(variable.equals("contact_groups"))
         temp_service.contact_groups=value;
     else if(variable.equals("failure_prediction_options"))
         temp_service.failure_prediction_options=value;
     else if(variable.equals("max_check_attempts"))
     {
         temp_service.max_check_attempts=RegistryUtils.atoi(value);
         temp_service.have_max_check_attempts = common_h.TRUE;
     }
     else if(variable.equals("normal_check_interval"))
     {
         temp_service.normal_check_interval=RegistryUtils.atoi(value);
         temp_service.have_normal_check_interval = common_h.TRUE;
     }
     else if(variable.equals("retry_check_interval"))
     {
         temp_service.retry_check_interval=RegistryUtils.atoi(value);
         temp_service.have_retry_check_interval = common_h.TRUE;
     }
	 else if(variable.equals("active_checks_enabled"))
	 {
         temp_service.active_checks_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_active_checks_enabled = common_h.TRUE;
	 }
     else if(variable.equals("passive_checks_enabled"))
     {
         temp_service.passive_checks_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_passive_checks_enabled = common_h.TRUE;
     }
     else if(variable.equals("parallelize_check"))
     {
         temp_service.parallelize_check=RegistryUtils.atoi(value);
         temp_service.have_parallelize_check = common_h.TRUE;
     }
     else if(variable.equals("is_volatile"))
     {
         temp_service.is_volatile=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_is_volatile = common_h.TRUE;
     }
     else if(variable.equals("obsess_over_service"))
     {
         temp_service.obsess_over_service=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_obsess_over_service = common_h.TRUE;
     }
     else if(variable.equals("event_handler_enabled"))
     {
         temp_service.event_handler_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_event_handler_enabled = common_h.TRUE;
     }
     else if(variable.equals("check_freshness"))
     {
         temp_service.check_freshness=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_check_freshness = common_h.TRUE;
     }
     else if(variable.equals("freshness_threshold"))
     {
         temp_service.freshness_threshold=RegistryUtils.atoi(value);
         temp_service.have_freshness_threshold = common_h.TRUE;
     }
     else if(variable.equals("low_flap_threshold"))
     {
         temp_service.low_flap_threshold=Float.parseFloat(value);
         temp_service.have_low_flap_threshold = common_h.TRUE;
     }
     else if(variable.equals("high_flap_threshold"))
     {
         temp_service.high_flap_threshold=Float.parseFloat(value);
         temp_service.have_high_flap_threshold = common_h.TRUE;
     }
     else if(variable.equals("flap_detection_enabled"))
     {
         temp_service.flap_detection_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_flap_detection_enabled = common_h.TRUE;
     }
     else if(variable.equals("notification_options"))
     {
         String[] split = value.split("[, ]");
         for( int i = 0; i<split.length; i ++ )
         {
             String temp_ptr = split[i].trim();
             if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                 temp_service.notify_on_unknown=common_h.TRUE;
             else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                 temp_service.notify_on_warning=common_h.TRUE;
             else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                 temp_service.notify_on_critical=common_h.TRUE;
             else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                 temp_service.notify_on_recovery=common_h.TRUE;
             else if(temp_ptr.equals("f") || temp_ptr.equals("flapping"))
                 temp_service.notify_on_flapping=common_h.TRUE;
             else if(temp_ptr.equals("n") || temp_ptr.equals("none"))
             {
                 temp_service.notify_on_unknown=common_h.FALSE;
                 temp_service.notify_on_warning=common_h.FALSE;
                 temp_service.notify_on_critical=common_h.FALSE;
                 temp_service.notify_on_recovery=common_h.FALSE;
                 temp_service.notify_on_flapping=common_h.FALSE;
             }
             else
             {
                 logger.fatal( "Error: Invalid notification option '"+temp_ptr+"' in service definition.");
                 throw new RegistryConfigurationException("Invalid notification option '" + temp_ptr + "' in service definiton");
             }
             
         }
         temp_service.have_notification_options=common_h.TRUE;
     }
     else if(variable.equals("notifications_enabled"))
     {
         temp_service.notifications_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_notifications_enabled = common_h.TRUE;
     }
     else if(variable.equals("notification_interval"))
     {
         temp_service.notification_interval=RegistryUtils.atoi(value);
         temp_service.have_notification_interval = common_h.TRUE;
     }
     else if(variable.equals("stalking_options"))
     {
         String[] split = value.split( "[, ]" );
         for( int i = 0; i<split.length; i ++ )
         {
             String temp_ptr = split[i].trim();
             if(temp_ptr.equals("o") || temp_ptr.equals("ok"))
                 temp_service.stalk_on_ok=common_h.TRUE;
             else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                 temp_service.stalk_on_warning=common_h.TRUE;
             else if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                 temp_service.stalk_on_unknown=common_h.TRUE;
             else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                 temp_service.stalk_on_critical=common_h.TRUE;
             else if(temp_ptr.equals("n") || temp_ptr.equals("none"))
             {
                 temp_service.stalk_on_ok=common_h.FALSE;
                 temp_service.stalk_on_warning=common_h.FALSE;
                 temp_service.stalk_on_unknown=common_h.FALSE;
                 temp_service.stalk_on_critical=common_h.FALSE;
             }
             else
             {
                 logger.fatal( "Error: Invalid stalking option '"+temp_ptr+"' in service definition.");
                 throw new RegistryConfigurationException("Invalid stalking option '" + temp_ptr + "' in service definition");
             }
         }
         temp_service.have_stalking_options=common_h.TRUE;
     }
     else if(variable.equals("process_perf_data"))
     {
         temp_service.process_perf_data=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_process_perf_data = common_h.TRUE;
     }
     else if(variable.equals("failure_prediction_enabled"))
     {
         temp_service.failure_prediction_enabled=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_failure_prediction_enabled = common_h.TRUE;
     }
     else if(variable.equals("retain_status_information"))
     {
         temp_service.retain_status_information=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_retain_status_information = common_h.TRUE;
     }
     else if(variable.equals("retain_nonstatus_information"))
     {
         temp_service.retain_nonstatus_information=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
         temp_service.have_retain_nonstatus_information = common_h.TRUE;
     }
     else if(variable.equals("register"))
         temp_service.register_object=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
     else
     {
         logger.fatal("Error: Invalid service object directive '"+variable+"'.");
         throw new RegistryConfigurationException("Invalid Service object directive '" + variable + "'");
     }
     break;
	 
	 case xodtemplate_h.XODTEMPLATE_CONTACTGROUP:
		 
		 xodtemplate_h.xodtemplate_contactgroup temp_contactgroup=(xodtemplate_h.xodtemplate_contactgroup) currentXODObject;
	        
	     if(variable.equals("use"))
	     {
	            temp_contactgroup.template=value;
	     }
	     else if(variable.equals("name"))
	     {
	         try
	         {
	        	 findContactGroup(value);
	        	 logger.warn("Warning: Duplicate definition found for contactgroup '"+value+")");
	         }
	         catch(UnknownObjectException e)
	         {}
	        
	         temp_contactgroup.name=value;
	     }
	     else if(variable.equals("contactgroup_name"))
	         temp_contactgroup.contactgroup_name=value;
	     else if(variable.equals("alias"))
	         temp_contactgroup.alias=value;
	     else if(variable.equals("members"))
	         temp_contactgroup.members=value;
	     else if(variable.equals("register"))
	         temp_contactgroup.register_object=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
	     else
	     {
	       	logger.fatal("Error: Invalid contactgroup object directive '"+variable+"'.");
	        throw new RegistryConfigurationException("Invalid Contactgroup object directive '" + variable + "'");
	     }
	        break;
	        
	    case xodtemplate_h.XODTEMPLATE_HOSTGROUP:
	        
	        xodtemplate_h.xodtemplate_hostgroup temp_hostgroup=(xodtemplate_h.xodtemplate_hostgroup)currentXODObject;
	        
	        if(variable.equals("use"))
	        {
	            temp_hostgroup.template=value;
	        }
	        else if(variable.equals("name"))
	        {
	            try
	            {
	            	findHostGroup(value);
	                logger.warn("Warning: Duplicate definition found for hostgroup'" + value + ")");
	            }
	            catch(UnknownObjectException e)
	            {}
	             
	            temp_hostgroup.name=value;
	        }
	        else if(variable.equals("hostgroup_name"))
	        {
	            temp_hostgroup.hostgroup_name=value;
	        }
	        else if(variable.equals("alias"))
	        {
	            temp_hostgroup.alias=value;
	        }
	        else if(variable.equals("members"))
	        {
	            if(temp_hostgroup.members==null)
	                temp_hostgroup.members=value;
	            else
	                temp_hostgroup.members += "," + value;
	        }
	        else if(variable.equals("register"))
	            temp_hostgroup.register_object=(RegistryUtils.atoi(value)>0)?common_h.TRUE:common_h.FALSE;
	        else
	        {
	            logger.fatal( "Error: Invalid hostgroup object directive '"+variable+"'.");
	            throw new RegistryConfigurationException("Invalid Hostgroup object directive '" + variable + "'");
	        }
	        
	        break;
	 		
	 case XODTEMPLATE_DYNAMIC_TEMPLATE:
		 
		 	XODDynamicTemplate tempDynamicTemplate = (XODDynamicTemplate)currentXODObject;
		 	
		 	if(variable.equals("use"))
		 		tempDynamicTemplate.setTemplate(value);
		 	else if(variable.equals("remote_template_name"))
		 	{
		 		try
		 		{
		 			findDynamicTemplate(value);
		 			logger.warn("Warning: Duplicate Dynamic Template definition found for Dynamic Template '" +value+ "' (config file '" + config_file + "', starting on line "+tempDynamicTemplate.getStartLine()+")");
		 		}
		 		catch(UnknownObjectException e)
		 		{}
			
		 		tempDynamicTemplate.setRemoteTemplateName(value);
		 	}
		 	else if(variable.equals("uses_host"))
		 		tempDynamicTemplate.setUsesHost(value);
		 	else if(variable.equals("runs_services"))
		 		tempDynamicTemplate.setRunsServices(value);
		 	else if(variable.equals("contact_group"))
		 		tempDynamicTemplate.setContactGroups(value);
		 	else if(variable.equals("joins_hostgroup"))
		 		tempDynamicTemplate.setJoinsHostGroup(value);
		 	else if(variable.equals("persist_registration"))
		 		tempDynamicTemplate.setPersistRegistration((RegistryUtils.atoi(value)>0)? common_h.TRUE : common_h.FALSE);
		 	else
		 	{
		 		logger.fatal("Error: Invalid Dynamic Template object directive '" +variable+ "'.");
		 		throw new RegistryConfigurationException("Invalid Dynamic Template object directive '" + variable + "'");
		 	}   
		 	
		 	break;
	 }
   }

   /* End object definition */
   public static void endObjectDefinition() throws RegistryConfigurationException
   {
       logger.trace("entering " + cn + ".xodtemplate_end_object_definition");
       
       /* Reset current object and current object type */
       currentXODObject = null;
       currentObjectType = xodtemplate_h.XODTEMPLATE_NONE;
      
       if(currentXODObject != null && currentObjectType != xodtemplate_h.XODTEMPLATE_NONE)
       {
    	   throw new RegistryConfigurationException("Error ending Object Definition");
       }
   }
   
   /* Resolve all our objects */
   public static void resolveObjects() throws RegistryConfigurationException
   {
	   resolveHosts();
	   resolveServices();
	   resolveDynamicTemplates();
   }
   
   /*Resolve all dynamic template objects */
	public static void resolveDynamicTemplates() throws RegistryConfigurationException
	{
		for(ListIterator iter = xodDynamicTemplateList.listIterator(); iter.hasNext();)
		{
			resolveDynamicTemplate((XODDynamicTemplate)iter.next());
		}
	}
	
	/* Resolve all hosts */
	public static void resolveHosts() throws RegistryConfigurationException
	{
		for(ListIterator i = xodHostList.listIterator();i.hasNext();)
		{
			resolveHost((xodtemplate_h.xodtemplate_host)i.next());
		}
	}
   
	/* Resolve all services */
	public static void resolveServices() throws RegistryConfigurationException
	{
		for(ListIterator i = xodServiceList.listIterator();i.hasNext();)
		{
			resolveService((xodtemplate_h.xodtemplate_service)i.next());
		}
	}
	
	/* Resolves a dynamic template should it be composed of templates itself */
	public static void resolveDynamicTemplate(XODDynamicTemplate this_dynamic_template) throws RegistryConfigurationException
	{
		logger.trace("Entering " + cn + ".xodtemplate_resolve_dynamic_template");
		XODDynamicTemplate template_dynamic_template;
		
		if(this_dynamic_template.getHasBeenResolved() == common_h.TRUE)
			return;
		
		/* Set the resolved flag */
		this_dynamic_template.setHasBeenResolved(common_h.TRUE);
		
		/* Check for a valid template name */
		if(this_dynamic_template.getTemplate() == null)
			return;
		
		try
		{
			template_dynamic_template = findDynamicTemplate(this_dynamic_template.getTemplate());
		}
		catch(UnknownObjectException e)
		{
			throw new RegistryConfigurationException(e.getMessage(),e);
		}
				
		/* Deal with any inheritance in our template definitions */
		resolveDynamicTemplate(template_dynamic_template);
				
		if(this_dynamic_template.getRemoteTemplateName() == null && template_dynamic_template.getRemoteTemplateName()!=null)
			this_dynamic_template.setRemoteTemplateName(template_dynamic_template.getRemoteTemplateName());
		
		if(this_dynamic_template.getUsesHost() == null && template_dynamic_template.getUsesHost()!= null)
			this_dynamic_template.setUsesHost(template_dynamic_template.getUsesHost());
		
		if(this_dynamic_template.getRunsServices() == null && template_dynamic_template.getRunsServices() != null)
			this_dynamic_template.setRunsServices(template_dynamic_template.getRunsServices());
		
		if(this_dynamic_template.getContactGroups() == null && template_dynamic_template.getContactGroups()!= null)
			this_dynamic_template.setContactGroups(template_dynamic_template.getContactGroups());
		
		if(this_dynamic_template.getJoinsHostGroup() == null && template_dynamic_template.getJoinsHostGroup()!= null)
			this_dynamic_template.setJoinsHostGroup(template_dynamic_template.getJoinsHostGroup());
		
		if(this_dynamic_template.getPersistRegistration() == common_h.FALSE && template_dynamic_template.getPersistRegistration() == common_h.TRUE)
			this_dynamic_template.setPersistRegistration(common_h.TRUE);
	}
    
	/* Resolve our service objects */
	public static void resolveService(xodtemplate_h.xodtemplate_service this_service) throws RegistryConfigurationException
	{
	    xodtemplate_h.xodtemplate_service template_service;
	    logger.trace( "entering " + cn + ".xodtemplate_resolve_service");
	    
	    /* return if this service has already been resolved */
	    if(this_service.has_been_resolved == common_h.TRUE)
	        return;
	    
	    /* set the resolved flag */
	    this_service.has_been_resolved = common_h.TRUE;
	    
	    /* return if we have no template */
	    if(this_service.template == null)
	        return;
	    
	    try
	    {
	    	template_service = findService(this_service.template);
	    }
	    catch(UnknownObjectException e)
	    {
	    	throw new RegistryConfigurationException(e.getMessage(),e);
	    }
        
	    /* resolve the template service... */
	    resolveService(template_service);
	    
	    /* apply missing properties from template service... */
	    if(this_service.hostgroup_name==null && template_service.hostgroup_name!=null)
	        this_service.hostgroup_name=(template_service.hostgroup_name);
	    if(this_service.host_name==null && template_service.host_name!=null)
	        this_service.host_name=(template_service.host_name);
	    if(this_service.service_description==null && template_service.service_description!=null)
	        this_service.service_description=(template_service.service_description);
	    if(this_service.servicegroups==null && template_service.servicegroups!=null)
	        this_service.servicegroups=(template_service.servicegroups);
	    if(this_service.check_command==null && template_service.check_command!=null)
	        this_service.check_command=(template_service.check_command);
	    if(this_service.check_period==null && template_service.check_period!=null)
	        this_service.check_period=(template_service.check_period);
	    if(this_service.event_handler==null && template_service.event_handler!=null)
	        this_service.event_handler=(template_service.event_handler);
	    if(this_service.notification_period==null && template_service.notification_period!=null)
	        this_service.notification_period=(template_service.notification_period);
	    if(this_service.contact_groups==null && template_service.contact_groups!=null)
	        this_service.contact_groups=(template_service.contact_groups);
	    if(this_service.failure_prediction_options==null && template_service.failure_prediction_options!=null)
	        this_service.failure_prediction_options=(template_service.failure_prediction_options);
	    if(this_service.have_max_check_attempts==common_h.FALSE && template_service.have_max_check_attempts==common_h.TRUE){
	        this_service.max_check_attempts=template_service.max_check_attempts;
	        this_service.have_max_check_attempts=common_h.TRUE;
	    }
	    if(this_service.have_normal_check_interval==common_h.FALSE && template_service.have_normal_check_interval==common_h.TRUE){
	        this_service.normal_check_interval=template_service.normal_check_interval;
	        this_service.have_normal_check_interval=common_h.TRUE;
	    }
	    if(this_service.have_retry_check_interval==common_h.FALSE && template_service.have_retry_check_interval==common_h.TRUE){
	        this_service.retry_check_interval=template_service.retry_check_interval;
	        this_service.have_retry_check_interval=common_h.TRUE;
	    }
	    if(this_service.have_active_checks_enabled==common_h.FALSE && template_service.have_active_checks_enabled==common_h.TRUE){
	        this_service.active_checks_enabled=template_service.active_checks_enabled;
	        this_service.have_active_checks_enabled=common_h.TRUE;
	    }
	    if(this_service.have_passive_checks_enabled==common_h.FALSE && template_service.have_passive_checks_enabled==common_h.TRUE){
	        this_service.passive_checks_enabled=template_service.passive_checks_enabled;
	        this_service.have_passive_checks_enabled=common_h.TRUE;
	    }
	    if(this_service.have_parallelize_check == common_h.FALSE && template_service.have_parallelize_check==common_h.TRUE){
	        this_service.parallelize_check = template_service.parallelize_check;
	        this_service.have_parallelize_check=common_h.TRUE;
	    }
	    if(this_service.have_is_volatile==common_h.FALSE && template_service.have_is_volatile==common_h.TRUE){
	        this_service.is_volatile=template_service.is_volatile;
	        this_service.have_is_volatile=common_h.TRUE;
	    }
	    if(this_service.have_obsess_over_service==common_h.FALSE && template_service.have_obsess_over_service==common_h.TRUE){
	        this_service.obsess_over_service=template_service.obsess_over_service;
	        this_service.have_obsess_over_service=common_h.TRUE;
	    }
	    if(this_service.have_event_handler_enabled==common_h.FALSE && template_service.have_event_handler_enabled==common_h.TRUE){
	        this_service.event_handler_enabled=template_service.event_handler_enabled;
	        this_service.have_event_handler_enabled=common_h.TRUE;
	    }
	    if(this_service.have_check_freshness==common_h.FALSE && template_service.have_check_freshness==common_h.TRUE){
	        this_service.check_freshness=template_service.check_freshness;
	        this_service.have_check_freshness=common_h.TRUE;
	    }
	    if(this_service.have_freshness_threshold==common_h.FALSE && template_service.have_freshness_threshold==common_h.TRUE){
	        this_service.freshness_threshold=template_service.freshness_threshold;
	        this_service.have_freshness_threshold=common_h.TRUE;
	    }
	    if(this_service.have_low_flap_threshold==common_h.FALSE && template_service.have_low_flap_threshold==common_h.TRUE){
	        this_service.low_flap_threshold=template_service.low_flap_threshold;
	        this_service.have_low_flap_threshold=common_h.TRUE;
	    }
	    if(this_service.have_high_flap_threshold==common_h.FALSE && template_service.have_high_flap_threshold==common_h.TRUE){
	        this_service.high_flap_threshold=template_service.high_flap_threshold;
	        this_service.have_high_flap_threshold=common_h.TRUE;
	    }
	    if(this_service.have_flap_detection_enabled==common_h.FALSE && template_service.have_flap_detection_enabled==common_h.TRUE){
	        this_service.flap_detection_enabled=template_service.flap_detection_enabled;
	        this_service.have_flap_detection_enabled=common_h.TRUE;
	    }
	    if(this_service.have_notification_options==common_h.FALSE && template_service.have_notification_options==common_h.TRUE){
	        this_service.notify_on_unknown=template_service.notify_on_unknown;
	        this_service.notify_on_warning=template_service.notify_on_warning;
	        this_service.notify_on_critical=template_service.notify_on_critical;
	        this_service.notify_on_recovery=template_service.notify_on_recovery;
	        this_service.notify_on_flapping=template_service.notify_on_flapping;
	        this_service.have_notification_options=common_h.TRUE;
	    }
	    if(this_service.have_notifications_enabled==common_h.FALSE && template_service.have_notifications_enabled==common_h.TRUE){
	        this_service.notifications_enabled=template_service.notifications_enabled;
	        this_service.have_notifications_enabled=common_h.TRUE;
	    }
	    if(this_service.have_notification_interval==common_h.FALSE && template_service.have_notification_interval==common_h.TRUE){
	        this_service.notification_interval=template_service.notification_interval;
	        this_service.have_notification_interval=common_h.TRUE;
	    }
	    if(this_service.have_stalking_options==common_h.FALSE && template_service.have_stalking_options==common_h.TRUE){
	        this_service.stalk_on_ok=template_service.stalk_on_ok;
	        this_service.stalk_on_unknown=template_service.stalk_on_unknown;
	        this_service.stalk_on_warning=template_service.stalk_on_warning;
	        this_service.stalk_on_critical=template_service.stalk_on_critical;
	        this_service.have_stalking_options=common_h.TRUE;
	    }
	    if(this_service.have_process_perf_data==common_h.FALSE && template_service.have_process_perf_data==common_h.TRUE){
	        this_service.process_perf_data=template_service.process_perf_data;
	        this_service.have_process_perf_data=common_h.TRUE;
	    }
	    if(this_service.have_failure_prediction_enabled==common_h.FALSE && template_service.have_failure_prediction_enabled==common_h.TRUE){
	        this_service.failure_prediction_enabled=template_service.failure_prediction_enabled;
	        this_service.have_failure_prediction_enabled=common_h.TRUE;
	    }
	    if(this_service.have_retain_status_information==common_h.FALSE && template_service.have_retain_status_information==common_h.TRUE){
	        this_service.retain_status_information=template_service.retain_status_information;
	        this_service.have_retain_status_information=common_h.TRUE;
	    }
	    if(this_service.have_retain_nonstatus_information==common_h.FALSE && template_service.have_retain_nonstatus_information==common_h.TRUE){
	        this_service.retain_nonstatus_information=template_service.retain_nonstatus_information;
	        this_service.have_retain_nonstatus_information=common_h.TRUE;
	    }
	    
	    logger.trace( "exiting " + cn + ".xodtemplate_resolve_service");
	}
	
	public static void resolveHost(xodtemplate_h.xodtemplate_host this_host) throws RegistryConfigurationException
	{
	    xodtemplate_h.xodtemplate_host template_host;
	    logger.trace( "entering " + cn + ".xodtemplate_resolve_host");
	    
	    /* return if this host has already been resolved */
	    if(this_host.has_been_resolved==common_h.TRUE)
	        return;
	    
	    /* set the resolved flag */
	    this_host.has_been_resolved=common_h.TRUE;
	    
	    /* return if we have no template */
	    if(this_host.template == null)
	        return;
	    
	    try
	    {
	    	template_host = findHost(this_host.template);
	    }
	    catch(UnknownObjectException e)
	    {
	    	throw new RegistryConfigurationException(e.getMessage(),e);
	    }
	    	    
	    /* resolve the template host... */
	    resolveHost(template_host);
	    
	    /* apply missing properties from template host... */
	    if(this_host.host_name==null && template_host.host_name!=null)
	        this_host.host_name=(template_host.host_name);
	    if(this_host.alias==null && template_host.alias!=null)
	        this_host.alias=(template_host.alias);
	    if(this_host.address==null && template_host.address!=null)
	        this_host.address=(template_host.address);
	    if(this_host.parents==null && template_host.parents!=null)
	        this_host.parents=(template_host.parents);
	    if(this_host.hostgroups==null && template_host.hostgroups!=null)
	        this_host.hostgroups=(template_host.hostgroups);
	    if(this_host.check_command==null && template_host.check_command!=null)
	        this_host.check_command=(template_host.check_command);
	    if(this_host.check_period==null && template_host.check_period!=null)
	        this_host.check_period=(template_host.check_period);
	    if(this_host.event_handler==null && template_host.event_handler!=null)
	        this_host.event_handler=(template_host.event_handler);
	    if(this_host.contact_groups==null && template_host.contact_groups!=null)
	        this_host.contact_groups=(template_host.contact_groups);
	    if(this_host.notification_period==null && template_host.notification_period!=null)
	        this_host.notification_period=(template_host.notification_period);
	    if(this_host.failure_prediction_options==null && template_host.failure_prediction_options!=null)
	        this_host.failure_prediction_options=(template_host.failure_prediction_options);
	    if(this_host.have_check_interval==common_h.FALSE && template_host.have_check_interval==common_h.TRUE)
	    {
	        this_host.check_interval=template_host.check_interval;
	        this_host.have_check_interval=common_h.TRUE;
	    }
	    if(this_host.have_max_check_attempts==common_h.FALSE && template_host.have_max_check_attempts==common_h.TRUE){
	        this_host.max_check_attempts=template_host.max_check_attempts;
	        this_host.have_max_check_attempts=common_h.TRUE;
	    }
	    if(this_host.have_active_checks_enabled==common_h.FALSE && template_host.have_active_checks_enabled==common_h.TRUE){
	        this_host.active_checks_enabled=template_host.active_checks_enabled;
	        this_host.have_active_checks_enabled=common_h.TRUE;
	    }
	    if(this_host.have_passive_checks_enabled==common_h.FALSE && template_host.have_passive_checks_enabled==common_h.TRUE){
	        this_host.passive_checks_enabled=template_host.passive_checks_enabled;
	        this_host.have_passive_checks_enabled=common_h.TRUE;
	    }
	    if(this_host.have_obsess_over_host==common_h.FALSE && template_host.have_obsess_over_host==common_h.TRUE){
	        this_host.obsess_over_host=template_host.obsess_over_host;
	        this_host.have_obsess_over_host=common_h.TRUE;
	    }
	    if(this_host.have_event_handler_enabled==common_h.FALSE && template_host.have_event_handler_enabled==common_h.TRUE){
	        this_host.event_handler_enabled=template_host.event_handler_enabled;
	        this_host.have_event_handler_enabled=common_h.TRUE;
	    }
	    if(this_host.have_check_freshness==common_h.FALSE && template_host.have_check_freshness==common_h.TRUE){
	        this_host.check_freshness=template_host.check_freshness;
	        this_host.have_check_freshness=common_h.TRUE;
	    }
	    if(this_host.have_freshness_threshold==common_h.FALSE && template_host.have_freshness_threshold==common_h.TRUE){
	        this_host.freshness_threshold=template_host.freshness_threshold;
	        this_host.have_freshness_threshold=common_h.TRUE;
	    }
	    if(this_host.have_low_flap_threshold==common_h.FALSE && template_host.have_low_flap_threshold==common_h.TRUE){
	        this_host.low_flap_threshold=template_host.low_flap_threshold;
	        this_host.have_low_flap_threshold=common_h.TRUE;
	    }
	    if(this_host.have_high_flap_threshold==common_h.FALSE && template_host.have_high_flap_threshold==common_h.TRUE){
	        this_host.high_flap_threshold=template_host.high_flap_threshold;
	        this_host.have_high_flap_threshold=common_h.TRUE;
	    }
	    if(this_host.have_flap_detection_enabled==common_h.FALSE && template_host.have_flap_detection_enabled==common_h.TRUE){
	        this_host.flap_detection_enabled=template_host.flap_detection_enabled;
	        this_host.have_flap_detection_enabled=common_h.TRUE;
	    }
	    if(this_host.have_notification_options==common_h.FALSE && template_host.have_notification_options==common_h.TRUE){
	        this_host.notify_on_down=template_host.notify_on_down;
	        this_host.notify_on_unreachable=template_host.notify_on_unreachable;
	        this_host.notify_on_recovery=template_host.notify_on_recovery;
	        this_host.notify_on_flapping=template_host.notify_on_flapping;
	        this_host.have_notification_options=common_h.TRUE;
	    }
	    if(this_host.have_notifications_enabled==common_h.FALSE && template_host.have_notifications_enabled==common_h.TRUE){
	        this_host.notifications_enabled=template_host.notifications_enabled;
	        this_host.have_notifications_enabled=common_h.TRUE;
	    }
	    if(this_host.have_notification_interval==common_h.FALSE && template_host.have_notification_interval==common_h.TRUE){
	        this_host.notification_interval=template_host.notification_interval;
	        this_host.have_notification_interval=common_h.TRUE;
	    }
	    if(this_host.have_stalking_options==common_h.FALSE && template_host.have_stalking_options==common_h.TRUE){
	        this_host.stalk_on_up=template_host.stalk_on_up;
	        this_host.stalk_on_down=template_host.stalk_on_down;
	        this_host.stalk_on_unreachable=template_host.stalk_on_unreachable;
	        this_host.have_stalking_options=common_h.TRUE;
	    }
	    if(this_host.have_process_perf_data==common_h.FALSE && template_host.have_process_perf_data==common_h.TRUE){
	        this_host.process_perf_data=template_host.process_perf_data;
	        this_host.have_process_perf_data=common_h.TRUE;
	    }
	    if(this_host.have_failure_prediction_enabled==common_h.FALSE && template_host.have_failure_prediction_enabled==common_h.TRUE){
	        this_host.failure_prediction_enabled=template_host.failure_prediction_enabled;
	        this_host.have_failure_prediction_enabled=common_h.TRUE;
	    }
	    if(this_host.have_retain_status_information==common_h.FALSE && template_host.have_retain_status_information==common_h.TRUE){
	        this_host.retain_status_information=template_host.retain_status_information;
	        this_host.have_retain_status_information=common_h.TRUE;
	    }
	    if(this_host.have_retain_nonstatus_information==common_h.FALSE && template_host.have_retain_nonstatus_information==common_h.TRUE){
	        this_host.retain_nonstatus_information=template_host.retain_nonstatus_information;
	        this_host.have_retain_nonstatus_information=common_h.TRUE;
	    }
	    
	    logger.trace( "exiting " + cn + ".xodtemplate_resolve_host");
	}
	
	/* Register all our objects */
	public static void registerObjects() throws RegistryConfigurationException
	{
		registerHosts();
		registerServices();
		registerHostGroups();
		registerContactGroups();
		registerDynamicTemplates();
	}
	
	/* Register all Dynamic Templates with the registry */
	public static void registerDynamicTemplates() throws RegistryConfigurationException
	{
		for(ListIterator iter = xodDynamicTemplateList.listIterator();iter.hasNext();)
		{
			registerDynamicTemplate((XODDynamicTemplate)iter.next());
		}
	}
	
	/* Register all Hosts */
	public static void registerHosts() throws RegistryConfigurationException
	{
		for(ListIterator i = xodHostList.listIterator();i.hasNext();)
		{
			registerHost((xodtemplate_h.xodtemplate_host)i.next());
		}
	}
	
	/* Register All Services */
	public static void registerServices() throws RegistryConfigurationException
	{
		for(ListIterator i = xodServiceList.listIterator();i.hasNext();)
		{
			registerService((xodtemplate_h.xodtemplate_service)i.next());
		}
	}
	
	/* Register all Contact Groups */
	public static void registerContactGroups() throws RegistryConfigurationException
	{
		for(ListIterator i = xodContactGroupList.listIterator();i.hasNext();)
		{
			registerContactGroup((xodtemplate_h.xodtemplate_contactgroup)i.next());
		}
	}
	
	/* Register all Host Groups */
	public static void registerHostGroups() throws RegistryConfigurationException
	{
		for(ListIterator i = xodHostGroupList.listIterator();i.hasNext();)
		{
			registerHostGroup((xodtemplate_h.xodtemplate_hostgroup)i.next());
		}
	}
	
    
    /* Sorts dynamic templates by remote_template_name */
    public static void sortDynamicTemplates()
    {
    	logger.trace("Entering " + cn + ".xodtemplate_sort_dynamic_templates");
    	
    	Comparator<XODDynamicTemplate> comp = new Comparator<XODDynamicTemplate>(){
    		
    		public int compare(XODDynamicTemplate a,XODDynamicTemplate b)
    		{
    			if(a.getRemoteTemplateName().equals(b.getRemoteTemplateName())) return 0;
    			if(a.getRemoteTemplateName() == null) return 1;
    			
    			return a.getRemoteTemplateName().compareTo(b.getRemoteTemplateName());
    		}
    	};
    	
    	java.util.Collections.sort(xodDynamicTemplateList,comp);
    }


/* Register our Dynamic Template Objects */
public static void registerDynamicTemplate(XODDynamicTemplate this_template) throws RegistryConfigurationException
{
	logger.trace("Entering " + cn + ".xodtemplate_register_dynamic_template");
	
	/* Verify that we actually want to register this object */
	if(this_template.getRegisterObject() == common_h.FALSE)
		return;
	
	try
	{
		findRealHost(this_template.getUsesHost());
	
		/* Check that all specified services exist */
		String[] services = this_template.getRunsServices().split(",");
		
		for(String s:  services)
		{
			findRealService(s);	
		}
	
		/* Check that the specified contact group exists */
		String[] contactGroups = this_template.getContactGroups().split(",");
	
		for(String s:  contactGroups)
		{
			findRealContactGroup(s);
		}
	
		/* Check that all specified Host groups actually exist! */
		if(this_template.getJoinsHostGroup() != null)
		{
			String[] hostGroups = this_template.getJoinsHostGroup().split(",");
	
			for(String s: hostGroups)
			{
				findRealHostGroup(s);
			}
		}
	}
	catch(UnknownObjectException e)
	{
		logger.debug(cn + ".registerDynamicTemplate() - Object associated with Dynamic Temaplate Definition Could not be found");
		throw new RegistryConfigurationException(e.getMessage(),e);
	}
	
	if(!RegistryObjects.addDynamicTemplate(this_template.getRemoteTemplateName(),this_template.getUsesHost(),this_template.getRunsServices(),this_template.getContactGroups(),this_template.getJoinsHostGroup(),this_template.getPersistRegistration()))
	{
		logger.fatal("Error: Could not register dynamic template (config file '"+ config_file + "', starting on line "+ this_template.getStartLine()+")");
		throw new RegistryConfigurationException("Could Not Register Dynamic Template with Registry Object Store");
	}
}

/* Register our Host Objects */
public static void registerHost(xodtemplate_h.xodtemplate_host host) throws RegistryConfigurationException
{
	logger.trace( "entering " + cn + ".xodtemplate_register_host");
    
	/* We register normal hosts for the registry config. */
	if(host.register_object == common_h.FALSE || host.name != null)
		return;
	
	try
	{
		if(host.check_period != null)
		{
			verifyTimePeriodExists(host.check_period);
		}
		
		if(host.notification_period != null)
		{
			verifyTimePeriodExists(host.notification_period);
		}
		
		if(host.check_command != null)
		{
			verifyCommandExists(host.check_command);
		}
		
		if(host.event_handler != null)
		{
			verifyCommandExists(host.event_handler.split("!")[0]);
		}
	}
	catch(UnknownObjectException e)
	{
		logger.debug(cn + ".registerHost() - Object associated with Host '" + host.name + "' could not be found");
		throw new RegistryConfigurationException(e.getMessage(),e);
	}
   
   /* Register our host */
   if(RegistryObjects.addHost(host.host_name,host.alias,(host.address==null)?host.host_name:host.address,host.check_period,host.check_interval,host.max_check_attempts,host.notify_on_recovery,host.notify_on_down,host.notify_on_unreachable,host.notify_on_flapping,host.notification_interval,host.notification_period,host.notifications_enabled,host.check_command,host.active_checks_enabled,host.passive_checks_enabled,host.event_handler,host.event_handler_enabled,host.flap_detection_enabled,host.low_flap_threshold,host.high_flap_threshold,host.stalk_on_up,host.stalk_on_down,host.stalk_on_unreachable,host.process_perf_data,host.failure_prediction_enabled,host.failure_prediction_options,host.check_freshness,host.freshness_threshold,host.retain_status_information,host.retain_nonstatus_information,host.obsess_over_host,host.contact_groups) == null)
   {
	   logger.debug(cn + ".registerHost() - Error Registering Host Object");
	   throw new RegistryConfigurationException("Cannot Register Host Object '" + host.name + "' with Registry Object Store");
   }
}

/* Register our Service Objects */
public static void registerService(xodtemplate_h.xodtemplate_service service) throws RegistryConfigurationException
{
	/* We only register services that have register = 1 */
	if(service.register_object == common_h.FALSE|| service.name != null)
		return;
	
	try
	{
		/* Verify all objects associated with this Service */
		if(service.check_period != null)
		{
			verifyTimePeriodExists(service.check_period);
		}
	
		if(service.notification_period != null)
		{
			verifyTimePeriodExists(service.notification_period);
		}
		
		if(service.check_command != null)
		{
			verifyCommandExists(service.check_command.split("!")[0]);
		}
		
		if(service.event_handler != null && service.event_handler_enabled == common_h.TRUE)
		{
			verifyCommandExists(service.event_handler.split("!")[0]);
		}
	}
	catch(UnknownObjectException e)
	{
		logger.debug(cn + ".registerService() - Object associated with service definition '" + service.service_description + "' could not be found");
		throw new RegistryConfigurationException(e.getMessage(),e);
	}
	
	/* Attempt to register the service object */
	if(RegistryObjects.addService(service.host_name,service.service_description,service.check_period,service.max_check_attempts,service.parallelize_check,service.passive_checks_enabled,service.normal_check_interval,service.retry_check_interval,service.notification_interval,service.notification_period,service.notify_on_recovery,service.notify_on_unknown,service.notify_on_warning,service.notify_on_critical,service.notify_on_flapping,service.notifications_enabled,service.is_volatile,service.event_handler,service.event_handler_enabled,service.check_command,service.active_checks_enabled,service.flap_detection_enabled,service.low_flap_threshold,service.high_flap_threshold,service.stalk_on_ok,service.stalk_on_warning,service.stalk_on_unknown,service.stalk_on_critical,service.process_perf_data,service.failure_prediction_enabled,service.failure_prediction_options,service.check_freshness,service.freshness_threshold,service.retain_status_information,service.retain_nonstatus_information,service.obsess_over_service,service.contact_groups) == null)
	{
		logger.debug(cn + ".registerService() - Error Registering Service with Registry Object Store");
		throw new RegistryConfigurationException("Cannot Register Service Object '" + service.service_description + "' with Registry Object Store");
	}
		
}

/* Register our Host group objects */
public static void registerHostGroup(xodtemplate_h.xodtemplate_hostgroup hostgroup) throws RegistryConfigurationException
{
	if(hostgroup.register_object == common_h.FALSE)
		return;
	
	if(RegistryObjects.addHostGroup(hostgroup.hostgroup_name,hostgroup.alias,hostgroup.members) == null)
	{
		logger.debug(cn + ".registerHostGroup() - Cannot Register HostGroup '" + hostgroup.name + "' with Registry Object store");
		throw new RegistryConfigurationException("Cannot Register HostGroup with Registy Object Store");
	}
}

/* Register our contact Group objects */
public static void registerContactGroup(xodtemplate_h.xodtemplate_contactgroup contactgroup) throws RegistryConfigurationException
{
	if(contactgroup.register_object == common_h.FALSE)
		return;
	
	try
	{
		/* Verify that all members exist */
		for(String s: contactgroup.members.split(","))
		{
			verifyContactExists(s);
		}
	}
	catch(UnknownObjectException e)
	{
		logger.debug(cn + ".registerContactGroup() - Contact associated with group '" + contactgroup.name + "' could not be found");
		throw new RegistryConfigurationException(e.getMessage(),e);
	}
	
	/* Add our contact group */
	if(RegistryObjects.addContactGroup(contactgroup.contactgroup_name,contactgroup.alias,contactgroup.members) == null)
	{
		logger.debug(cn + ".registerContactGroup() - Error Registering Contact Group '" + contactgroup.name + "' with Registry Object Store");
		throw new RegistryConfigurationException("Error Registering Contact Group '" + contactgroup.name + "' with Registry Object Store");
	}
}

/* We need to check that the commands actually exist, but we don't need to store them */
private static void verifyCommandExists(String commandname) throws UnknownObjectException
{
	for(ListIterator i = xodCommandNameList.listIterator();i.hasNext();)
	{
		if(i.next().toString().equals(commandname))
			return;
	}
	
	throw new UnknownObjectException("ERROR: Command with name '" + commandname + "' could not be found in current object definitions");
}

/* We need to check that the timeperiod actually exists, but we don't need to store them */
private static void verifyTimePeriodExists(String timeperiodname) throws UnknownObjectException
{
	for(ListIterator i = xodTimePeriodNameList.listIterator();i.hasNext();)
	{
		if(i.next().toString().equals(timeperiodname))
			return; 
	}	
	
	throw new UnknownObjectException("ERROR: TimePeriod with name '" + timeperiodname + "' could not be found in current object definitions");
}

/* Verify that a given contact exists */
private static void verifyContactExists(String contactName) throws UnknownObjectException
{
	for(ListIterator i = xodContactNameList.listIterator();i.hasNext();)
	{
		if(i.next().toString().equals(contactName))
			return;
	}
	
	throw new UnknownObjectException("ERROR: Contact with name '" + contactName + "' could not be found in current object definitions");
}

/* Check to see if the template host exists */
private static xodtemplate_h.xodtemplate_host findHost(String hostname) throws UnknownObjectException
{
	for(ListIterator i = xodHostList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_host host = (xodtemplate_h.xodtemplate_host)i.next();
		
		if(host.name != null && host.name.equals(hostname))
			return host;
	}
	
	throw new UnknownObjectException("ERROR: Host with name'" + hostname + "' could not be found in current object definitions");
}

/* Find the real host object */
private static void findRealHost(String hostname) throws UnknownObjectException
{
	for(ListIterator i = xodHostList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_host host = (xodtemplate_h.xodtemplate_host)i.next();
		
		if(host.host_name != null && host.host_name.equals(hostname))
			return;
	}
	
	throw new UnknownObjectException("ERROR: Host with name '" + hostname + "' cannot be found in current object definitons");
}


/* Check to see if the template service exists */
private static xodtemplate_h.xodtemplate_service findService(String serviceDescription) throws UnknownObjectException
{
	for(ListIterator i = xodServiceList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_service service = (xodtemplate_h.xodtemplate_service)i.next();
		
		if(service.name != null && service.name.equals(serviceDescription))
			return service;
	}
	
	throw new UnknownObjectException("ERROR: Service with description '" + serviceDescription + "' cannot be found in current object definitions");
}

/* Find the real service and not just the template version */
private static void findRealService(String serviceDescription) throws UnknownObjectException
{
	for(ListIterator i = xodServiceList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_service service = (xodtemplate_h.xodtemplate_service)i.next();
		
		if(service.service_description != null && service.service_description.equals(serviceDescription))
			return;
	}
	
	throw new UnknownObjectException("ERROR: Service with description '" + serviceDescription + "' cannot be found in current object definitons");
}


/* Method to find a specific dynamic template type. Also used within config parsing to warn of duplicate names */
private static XODDynamicTemplate findDynamicTemplate(String template_name) throws UnknownObjectException
{
	for(ListIterator i = xodDynamicTemplateList.listIterator();i.hasNext();)
	{
		XODDynamicTemplate temp_dynamic_template = (XODDynamicTemplate)i.next();
		
		if((temp_dynamic_template.getRemoteTemplateName() != null) && temp_dynamic_template.getRemoteTemplateName().equals(template_name))
			return temp_dynamic_template;
	}
	
	throw new UnknownObjectException("ERROR: Dynamic Template with name '" + template_name + "' cannot be found in current object definitions");
}

/* Check to see if the contact group exists */
private static xodtemplate_h.xodtemplate_contactgroup findContactGroup(String groupName) throws UnknownObjectException
{
	for(ListIterator i = xodContactGroupList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_contactgroup contactgroup = (xodtemplate_h.xodtemplate_contactgroup)i.next();
		
		if(contactgroup.name != null && contactgroup.name.equals(groupName))
			return contactgroup;
	}
	
	throw new UnknownObjectException("ERROR: Contact Group with name '" + groupName + "' cannot be found in current object definitions");
}

/* find a real contact group */
private static void findRealContactGroup(String groupName) throws UnknownObjectException
{
	for(ListIterator i = xodContactGroupList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_contactgroup contactgroup = (xodtemplate_h.xodtemplate_contactgroup)i.next();
		
		if(contactgroup.contactgroup_name != null && contactgroup.contactgroup_name.equals(groupName))
			return;
	}
	
	throw new UnknownObjectException("ERROR: Contact Group with name '" + groupName + "' cannot be found in current object definitions");
}

/* Check to see if the host group exists */
private static xodtemplate_h.xodtemplate_hostgroup findHostGroup(String groupName) throws UnknownObjectException
{
	for(ListIterator i = xodHostGroupList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_hostgroup hostgroup = (xodtemplate_h.xodtemplate_hostgroup)i.next();
		
		if(hostgroup.name != null && hostgroup.name.equals(groupName))
			return hostgroup;
	}

	throw new UnknownObjectException("ERROR: Host Group with name '" + groupName + "' cannot be found in current object definitions");
}

/* find a real host group */
private static void findRealHostGroup(String groupName) throws UnknownObjectException 
{
	for(ListIterator i = xodHostGroupList.listIterator();i.hasNext();)
	{
		xodtemplate_h.xodtemplate_hostgroup hostgroup = (xodtemplate_h.xodtemplate_hostgroup)i.next();
		
		if(hostgroup.hostgroup_name != null && hostgroup.hostgroup_name.equals(groupName))
			return;
	}
	
	throw new UnknownObjectException("ERROR: Host Group with name '" + groupName + "' cannot be found in current object definitions");
}
}