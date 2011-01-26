package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;
import com.arjuna.blue.bluefrontend.xml.ObjectXMLConverter;
import com.arjuna.blue.bluefrontend.xml.XMLObjectLocator;

public class BlueConfigXMLFileStore extends BlueConfigFileStore
{
	
	private HashMap<Integer,BlueObject> objectList;
	private HashMap<Integer,BlueObject> templateList;
	private BlueObject storeObject;
	private int nextObjectId = 0;
	private int templateCount = 0;
	
	private ObjectXMLBuilder builder;
	private ObjectXMLConverter converter;
	private XMLObjectLocator locator;
	private String outputLocation;
	
	private int objectType;
	
	public BlueConfigXMLFileStore(int objectType)
	{
		this.objectType = objectType;
		outputLocation = Utils.getCurrentOutputLocation();
		
		builder = new ObjectXMLBuilder(outputLocation);
		converter = new ObjectXMLConverter(outputLocation);
		locator = new XMLObjectLocator(outputLocation);
		
		try
		{
			switch(objectType)
			{
				case ObjectXMLBuilder.MACRO:
					storeObject = new Macro();
					objectList = locator.buildMacroObjectsFromXML();
				break;
			case ObjectXMLBuilder.HOST:
				
				/* Host catches its own exceptions as there is no reason why a failure should 
				 * prohibit us from trying to populate the template List. Same for timeperiods,
				 * contacts, services.
				 */
				storeObject = new Host();
				
				try
				{
					objectList = locator.buildHostObjectsFromXML(false);
					nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.COMMAND);
				}
				catch(Exception e)
				{
					objectList = new HashMap<Integer,BlueObject>();
				}
				
				try{
					templateList = locator.buildHostObjectsFromXML(true);
				}
				catch(Exception e)
				{
					templateList = new HashMap<Integer,BlueObject>();
				}
				break;
			
			case ObjectXMLBuilder.HOSTGROUP:
				storeObject = new Group();
				objectList = locator.buildGroupObjectsFromXML(ObjectXMLBuilder.HOSTGROUP);
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.HOSTGROUP);
				break;
				
			case ObjectXMLBuilder.SERVICEGROUP:
				storeObject = new Group();
				objectList = locator.buildGroupObjectsFromXML(ObjectXMLBuilder.SERVICEGROUP);
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.SERVICEGROUP);
				break;
				
			case ObjectXMLBuilder.CONTACTGROUP:
				storeObject = new Group();
				objectList = locator.buildGroupObjectsFromXML(ObjectXMLBuilder.CONTACTGROUP);
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.CONTACTGROUP);
				break;
			case ObjectXMLBuilder.COMMAND:
				storeObject = new Command();
				objectList = locator.buildCommandObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.COMMAND);
				break;
				
			case ObjectXMLBuilder.HOSTDEPENDENCY:
				storeObject = new HostDependency();
				objectList = locator.buildHostDependencyObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.HOSTDEPENDENCY);
				break;
			
			case ObjectXMLBuilder.SERVICEDEPENDENCY:
				storeObject = new ServiceDependency();
				objectList = locator.buildServiceDependencyObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.SERVICEDEPENDENCY);
				break;
			
			case ObjectXMLBuilder.SERVICEESCALATION:
				storeObject = new ServiceEscalation();
				objectList = locator.buildServiceEscalationObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.SERVICEESCALATION);
				break;
				
			case ObjectXMLBuilder.HOSTESCALATION:
				storeObject = new HostEscalation();
				objectList = locator.buildHostEscalationObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.HOSTESCALATION);
				break;
			
			case ObjectXMLBuilder.HOSTEXTINFO:
				storeObject = new HostExtInfo();
				objectList = locator.buildHostExtInfoObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.HOSTEXTINFO);
				break;
			
			case ObjectXMLBuilder.SERVICEEXTINFO:
				storeObject = new ServiceExtInfo();
				objectList = locator.buildServiceExtInfoObjectsFromXML();
				nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.SERVICEEXTINFO);
				break;
			
			case ObjectXMLBuilder.TIMEPERIOD:
				storeObject = new TimePeriod();
				
				try
				{
					objectList = locator.buildTimePeriodObjectsFromXML(false);
					nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.TIMEPERIOD);
				}
				catch(Exception e)
				{
					objectList = new HashMap<Integer,BlueObject>();
				}
				
				try
				{
					templateList = locator.buildTimePeriodObjectsFromXML(true);
				}
				catch(Exception e)
				{
					templateList = new HashMap<Integer,BlueObject>();
				}
				break;
						
			case ObjectXMLBuilder.CONTACT:
				storeObject = new Contact();
				
				try
				{
					objectList = locator.buildContactObjectsFromXML(false);
					nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.CONTACT);
				}
				catch(Exception e)
				{
					objectList = new HashMap<Integer,BlueObject>();
				}
				
				try
				{
					templateList = locator.buildContactObjectsFromXML(true);
				}
				catch(Exception e)
				{
					templateList = new HashMap<Integer,BlueObject>();
				}
				break;
			
			case ObjectXMLBuilder.SERVICE:
				storeObject = new Service();
				
				try
				{
					objectList = locator.buildServiceObjectsFromXML(false);
					nextObjectId = locator.getNextObjectId(ObjectXMLBuilder.SERVICE);
				}
				catch(Exception e)
				{
					objectList = new HashMap<Integer,BlueObject>();
				}
				
				try
				{
					templateList = locator.buildServiceObjectsFromXML(true);
				}
				catch(Exception e)
				{
					templateList = new HashMap<Integer,BlueObject>();
				}
				break;
				
			case ObjectXMLBuilder.BLUECONFIG:
				break;
			}
		}
		catch(Exception e)
		{
			/* If we've thrown an exception in the above, we can be sure that the objectList
			 * has not been populated */			 
			objectList = new HashMap<Integer,BlueObject>();
		}
		
				
	}
	
	/* Add an object to the store */
	public synchronized String addObject(BlueObject blueObject,int objectType)
	{
		storeObject = blueObject;
				
		if(storeObject.getId() == -1)
		{
			storeObject.setId(nextObjectId);
			storeObject.setIsModifiable(true);
			storeObject.setIsTemplate(false);
			
			try
			{
				builder.buildObject(blueObject.getObjectDetails(),objectType,false);
				converter.convertDocument(Utils.xmlFileLocations[objectType],objectType);
			}
			catch(Exception e)
			{
				return "write-failure";
			}

			objectList.put(nextObjectId,storeObject);
			nextObjectId++;
									
			return "add-success";
		}
		return "failure";
	}

	/* Add a template to the store */
	public synchronized String addTemplate(BlueObject blueObject,int objectType)
	{
		storeObject = blueObject;
		
		if(checkTemplateNameExists(storeObject.getName(),objectType))
			return "failure";
		
		if(storeObject.getId() == -1)
		{
			templateCount++;
			storeObject.setId(templateCount);
			storeObject.setIsModifiable(true);
			storeObject.setIsTemplate(true);
			
			try
			{
				builder.buildObject(storeObject.getObjectDetails(),objectType,true);
			}
			catch(Exception e)
			{
				return "write-failure";
			}

			templateList.put(templateCount,storeObject);
		}
		return "success";
	}
	
	/* Modify an existing object */
	public synchronized String modifyObject(BlueObject blueObject,int objectType)
	{
		storeObject = blueObject;
		if(!checkObjectExists(storeObject.getId(),objectType))
			return "failure";
						
		try
		{
			locator.updateObjectDetails(objectType,storeObject.getId(),storeObject.getObjectMapDetails());
			converter.convertDocument(Utils.xmlFileLocations[objectType],objectType);
		}
		catch(Exception e)
		{
			return "failure";
		}
			
		objectList.put(storeObject.getId(),storeObject);
		return "modify-success";
	}
	
	/* Store a macro */
	public synchronized String storeMacro(BlueObject blueObject)
	{
		storeObject = blueObject;		
		storeObject.setIsModifiable(true);
				
		try
		{
			builder.buildMacros(storeObject.getObjectDetails());
			converter.convertMacros();
		}
		catch(Exception e)
		{
			storeObject.setIsModifiable(false);
			storeObject.setId(-1);
			return "failure";
		}
		
		objectList.put(storeObject.getId(),storeObject);
		return "add-success";
	}
	
	/* Delete a macro */
	public synchronized String deleteMacro(int objectId)
	{
		if(objectList.containsKey(objectId))
		{
			try
			{
				locator.deleteMacro(objectId);
				converter.convertMacros();
			}
			catch(Exception ex)
			{
				return "failure";
			}
			objectList.remove(objectId);
			
			return "delete-success";
		}
		
		return "failure";
	}
	
	/* Modify a macro */
	public synchronized String modifyMacro(BlueObject blueObject)
	{
		storeObject = blueObject;
		
		if(objectList.containsKey(storeObject.getId()))
		{
			try
			{
				locator.updateMacroDetails(storeObject.getObjectDetails());
				converter.convertMacros();
			}
			catch(Exception e)
			{
				return "failure";
			}
		
			objectList.put(storeObject.getId(),storeObject);
			return "modify-success";
		}
		
		return "failure";
	}
	
	/* Verify that a given objectId exists within the store */
	public boolean checkObjectExists(int objectId,int objectType)
	{
		if(objectList.containsKey(objectId))
			return true;
		
		return false;
	}

	/* Verify that a given object name exists */
	public boolean checkObjectNameExists(String objectName,int objectType)
	{
		Iterator i = objectList.keySet().iterator();
		
		while(i.hasNext())
		{
			BlueObject o = objectList.get(i.next());
			
			if(o.getName().equalsIgnoreCase(objectName))
				return true;
		}
		
		return false;
	}
	
	/* Verify that a given template name exists */
	public boolean checkTemplateNameExists(String templateName,int objectType)
	{
		Iterator i = templateList.keySet().iterator();
		while(i.hasNext())
		{
			BlueObject o = objectList.get(i.next());
			if(o.getName().equalsIgnoreCase(templateName))
				return true;
		}
		return false;
	}

	/* Delete an object */
	public synchronized String deleteObject(int objectId,int objectType)
	{
		try
		{
			locator.deleteObject(objectType,objectId);
			converter.convertDocument(Utils.xmlFileLocations[objectType],objectType);
			objectList.remove(objectId);
		}
		catch(Exception e)
		{
			return "failure";
		}
		return "delete-success";		
	}

	/* Return the count of a specific object type */
	public int getObjectCount(int objectType)
	{
		return objectList.size();
	}

	/* Return a list of the names of a specific object type */
	public List<String> getObjectNames(int objectType)
	{
		List<String> objectNames = new ArrayList<String>();
		Iterator i = objectList.keySet().iterator();
		
		while(i.hasNext())
		{
			objectNames.add(objectList.get(i.next()).getName());
		}
		
		return objectNames;
	}

	/* Return a list of all stored objects of a specific type */
	public List<BlueObject> getStoredObjects(int objectType)
	{
		List<BlueObject> objects = new ArrayList<BlueObject>();
		Iterator i = objectList.keySet().iterator();
		
		while(i.hasNext())
		{
			objects.add(objectList.get(i.next()));
		}
		
		return objects;
	}

	/* Load a particular object by it's id */
	public BlueObject loadObjectById(int objectId,int objectType)
	{
		if(objectList.containsKey(objectId))
			return objectList.get(objectId); 
		
		return null;
	}

	/* Search for an object by name */
	public List<BlueObject> searchByObjectName(String objectName,int objectType)
	{
		List<BlueObject> searchList = new ArrayList<BlueObject>();
		Iterator i = objectList.keySet().iterator();
		
		while(i.hasNext())
		{
			BlueObject o = objectList.get(i.next());
			
			if(o.getName().toLowerCase().contains(objectName.toLowerCase()))
				searchList.add(o);
		}
		
		return searchList;
		
	}
	
	/* Return the template count for a specific object type */
	public int getTemplateCount(int objectType)
	{
		return templateList.size();
	}
	
	/* Return a list of all stored templates for a specific object type */
	public List<BlueObject> getStoredTemplates(int objectType)
	{
		List<BlueObject> templates = new ArrayList<BlueObject>();
		Iterator i = templateList.keySet().iterator();
		
		while(i.hasNext())
		{
			templates.add(templateList.get(i.next()));
		}
		
		return templates;
	}
	
	/* Load a template specified by the given id */
	public BlueObject loadTemplateById(int objectId)
	{
		if(templateList.containsKey(objectId))
			return templateList.get(objectId);
		
		return null;
	}

	/* Returns the hashmap store of a specifc object */
	public HashMap<Integer,BlueObject> getObjectHashMap()
	{
		return this.objectList;
	}
	
	/* Store the settings of the main blue.cfg */
	public String storeMainBlueConfig(BlueConfig blueConfig)
	{
		try
		{
			builder.buildBlueMainConfig(blueConfig.getBlueOptions());
			converter.convertBlueMainConfig("blue.xml","blue.cfg");
		}
		catch(Exception e)
		{
			return "failure";
		}
		
		return "success";
	}
	
	/* Load the settings of the main blue.cfg */
	public boolean loadMainBlueConfig(BlueConfig blueConfig)
	{
		SAXBuilder builder = new SAXBuilder();
		String outputLocation = Utils.getCurrentOutputLocation();
		Element e =null;
		
		try
		{
			Document objectDoc = builder.build(outputLocation + "/xml/blue.xml");
			XPath x = XPath.newInstance("blue:blue_config");
			e = (Element)x.selectSingleNode(objectDoc);
		}
		catch(Exception ex)
		{
			return false;
		}
		
		for(Object o: e.getChildren())
		{
			Element el = (Element)o;
			if(el.getName().equals("log_file"))	blueConfig.setLogFile(el.getText());
			else if(el.getName().equals("object_cache_file")) blueConfig.setObjectCacheFile(el.getText());
			else if(el.getName().equals("temp_file")) blueConfig.setTempFile(el.getText());
			else if(el.getName().equals("status_file")) blueConfig.setStatusFile(el.getText());
			else if(el.getName().equals("aggregate_status_updates")) blueConfig.setAggregateStatusUpdates(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("status_update_interval")) blueConfig.setStatusUpdateInterval(Integer.valueOf(el.getText()));
			else if(el.getName().equals("nagios_user")) blueConfig.setBlueUser(el.getText());
			else if(el.getName().equals("nagios_group")) blueConfig.setBlueGroup(el.getText());
			else if(el.getName().equals("enable_notifications")) blueConfig.setEnableNotifications(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("execute_service_checks")) blueConfig.setExecuteServiceChecks(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("accept_passive_service_checks")) blueConfig.setAcceptPassiveServiceChecks(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("execute_host_checks")) blueConfig.setExecuteHostChecks(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("accept_passive_host_checks")) blueConfig.setAcceptPassiveHostChecks(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("enable_event_handlers")) blueConfig.setEnableEventHandlers(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_rotation_method")) blueConfig.setLogRotationMethod(el.getText());
			else if(el.getName().equals("log_archive_path")) blueConfig.setLogArchivePath(el.getText());
			else if(el.getName().equals("check_external_commands")) blueConfig.setCheckExternalCommands(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("command_check_interval")) blueConfig.setCommandCheckInterval(el.getText());
			else if(el.getName().equals("command_file")) blueConfig.setCommandFile(el.getText());
			else if(el.getName().equals("downtime_file")) blueConfig.setDowntimeFile(el.getText());
			else if(el.getName().equals("comment_file")) blueConfig.setCommentFile(el.getText());
			else if(el.getName().equals("lock_file")) blueConfig.setLockFile(el.getText());
			else if(el.getName().equals("retain_state_information")) blueConfig.setRetainStateInformation(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("state_retention_file")) blueConfig.setStateRetentionFile(el.getText());
			else if(el.getName().equals("retention_update_interval")) blueConfig.setRetentionUpdateInterval(Integer.valueOf(el.getText()));
			else if(el.getName().equals("use_retained_program_state")) blueConfig.setUseRetainedProgramState(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("use_retained_scheduling_info")) blueConfig.setUseRetainedSchedulingInfo(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("use_syslog")) blueConfig.setUseSyslog(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_notifications")) blueConfig.setLogNotifications(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_service_retries")) blueConfig.setLogServiceRetries(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_host_retries")) blueConfig.setLogHostRetries(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_event_handlers")) blueConfig.setLogEventHandlers(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_initial_states")) blueConfig.setLogInitialStates(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_external_commands")) blueConfig.setLogExternalCommands(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("log_passive_checks")) blueConfig.setLogPassiveChecks(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("global_host_event_handler")) blueConfig.setGlobalHostEventHandler(el.getText());
			else if(el.getName().equals("global_service_event_handler")) blueConfig.setGlobalServiceEventHandler(el.getText());
			else if(el.getName().equals("sleep_time")) blueConfig.setSleepTime(Double.valueOf(el.getText()));
			else if(el.getName().equals("service_inter_check_delay_method")) blueConfig.setServiceInterCheckDelayMethod(el.getText());
			else if(el.getName().equals("max_service_check_spread")) blueConfig.setMaxServiceCheckSpread(Integer.valueOf(el.getText()));
			else if(el.getName().equals("service_interleave_factor")) blueConfig.setServiceInterleaveFactor(el.getText());
			else if(el.getName().equals("max_concurrent_checks")) blueConfig.setMaxConcurrentChecks(Integer.valueOf(el.getText()));
			else if(el.getName().equals("service_reaper_frequency")) blueConfig.setServiceReaperFrequency(Integer.valueOf(el.getText()));
			else if(el.getName().equals("host_inter_check_delay_method")) blueConfig.setHostInterCheckDelayMethod(el.getText());
			else if(el.getName().equals("max_host_check_spread")) blueConfig.setMaxHostCheckSpread(Integer.valueOf(el.getText()));
			else if(el.getName().equals("interval_length")) blueConfig.setIntervalLength(Integer.valueOf(el.getText()));
			else if(el.getName().equals("auto_reschedule_checks")) blueConfig.setAutoRescheduleChecks(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("auto_rescheduling_interval")) blueConfig.setAutoReschedulingInterval(Integer.valueOf(el.getText()));
			else if(el.getName().equals("auto_rescheduling_window")) blueConfig.setAutoReschedulingWindow(Integer.valueOf(el.getText()));
			else if(el.getName().equals("use_aggressive_host_checking")) blueConfig.setUseAggressiveHostChecking(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("enable_flap_detection")) blueConfig.setEnableFlapDetection(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("low_service_flap_threshold")) blueConfig.setLowServiceFlapThreshold(Double.valueOf(el.getText()));
			else if(el.getName().equals("high_service_flap_thresold")) blueConfig.setHighServiceFlapThreshold(Double.valueOf(el.getText()));
			else if(el.getName().equals("low_host_flap_threshold")) blueConfig.setLowHostFlapThreshold(Double.valueOf(el.getText()));
			else if(el.getName().equals("high_host_flap_threshold")) blueConfig.setHighHostFlapThreshold(Double.valueOf(el.getText()));
			else if(el.getName().equals("soft_state_dependencies")) blueConfig.setSoftStateDependencies(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("service_check_timeout")) blueConfig.setServiceCheckTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("host_check_timeout")) blueConfig.setHostCheckTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("event_handler_timeout")) blueConfig.setEventHandlerTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("notification_timeout")) blueConfig.setNotificationTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("ocsp_timeout")) blueConfig.setOcspTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("ochp_timeout")) blueConfig.setOchpTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("perfdata_timeout")) blueConfig.setPerfdataTimeout(Integer.valueOf(el.getText()));
			else if(el.getName().equals("obsess_over_services")) blueConfig.setObsessOverServices(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("ocsp_command")) blueConfig.setOcspCommand(el.getText());
			else if(el.getName().equals("obsess_over_hosts")) blueConfig.setObsessOverHosts(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("ochp_command")) blueConfig.setOchpCommand(el.getText());
			else if(el.getName().equals("process_performance_data")) blueConfig.setProcessPerformanceData(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("host_perfdata_command")) blueConfig.setHostPerfdataCommand(el.getText());
			else if(el.getName().equals("service_perfdata_command")) blueConfig.setServicePerfdataCommand(el.getText());
			else if(el.getName().equals("host_perfdata_file")) blueConfig.setHostPerfdataFile(el.getText());
			else if(el.getName().equals("service_perfdata_file")) blueConfig.setServicePerfdataFile(el.getText());
			else if(el.getName().equals("host_perfdata_file_template")) blueConfig.setHostPerfdataFileTemplate(el.getText());
			else if(el.getName().equals("service_perfdata_file_template")) blueConfig.setServicePerfdataFileTemplate(el.getText());
			else if(el.getName().equals("host_perfdata_file_mode")) blueConfig.setHostPerfdataFileMode(el.getText());
			else if(el.getName().equals("service_perfdata_file_mode")) blueConfig.setServicePerfdataFileMode(el.getText());
			else if(el.getName().equals("host_perfdata_file_processing_interval")) blueConfig.setHostPerfdataFileProcessingInterval(Integer.parseInt(el.getText()));
			else if(el.getName().equals("service_perfdata_file_processing_interval")) blueConfig.setServicePerfdataFileProcessingInterval(Integer.valueOf(el.getText()));
			else if(el.getName().equals("host_perfdata_file_processing_command")) blueConfig.setHostPerfdataFileProcessingCommand(el.getText());
			else if(el.getName().equals("service_perfdata_file_processing_command")) blueConfig.setServicePerfdataFileProcessingCommand(el.getText());
			else if(el.getName().equals("check_for_orphaned_services")) blueConfig.setCheckForOrphanedServices(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("check_service_freshness")) blueConfig.setCheckServiceFreshness(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("service_freshness_check_interval")) blueConfig.setServiceFreshnessCheckInterval(Integer.valueOf(el.getText()));
			else if(el.getName().equals("check_host_freshness")) blueConfig.setCheckHostFreshness(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("host_freshness_check_interval")) blueConfig.setHostFreshnessCheckInterval(Integer.valueOf(el.getText()));
			else if(el.getName().equals("date_format")) blueConfig.setDateFormat(el.getText());
			else if(el.getName().equals("illegal_object_name_chars")) blueConfig.setIllegalObjectNameChars(el.getText());
			else if(el.getName().equals("illegal_macro_output_chars")) blueConfig.setIllegalMacroOutputChars(el.getText());
			else if(el.getName().equals("use_regexp_matching")) blueConfig.setUseRegexpMatching(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("use_true_regexp_matching")) blueConfig.setUseTrueRegexpMatching(Utils.stringToBoolean(el.getText()));
			else if(el.getName().equals("admin_email")) blueConfig.setAdminEmail(el.getText());
			else if(el.getName().equals("admin_pager")) blueConfig.setAdminPager(el.getText());
		}
		
		return true;
	}
}
