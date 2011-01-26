package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Service extends BlueObject
{
	private String[] hostname;
	private String serviceDescription = "";
	private String[] serviceGroups;
	private boolean isVolatile = false;
	private String checkCommand = "";
	private String commandArgs = "";
	private int maxCheckAttempts;
	private int normalCheckInterval;
	private int retryCheckInterval;
	private boolean activeChecksEnabled = false;
	private boolean passiveChecksEnabled = false;
	private String checkPeriod = "";
	private boolean parallelizeChecks = false;
	private boolean obsessOverService = false;
	private boolean checkFreshness = false;
	private int freshnessThreshold;
	private String[] eventHandler;
	private boolean eventHandlerEnabled = false;
	private int lowFlapThreshold;
	private int highFlapThreshold;
	private boolean flapDetectionEnabled= false;
	private boolean processPerfData = false;
	private boolean retainStatusInformation = false;
	private boolean retainNonStatusInformation = false;
	private int notificationInterval;
	private String notificationPeriod = "";
	private String[] notificationOptions;
	private boolean notificationsEnabled = false;
	private String[] contactGroups;
	private String[] stalkingOptions;
	
	public Service()
	{
		
	}
	
	public Service(Service s)
	{
		super(s);
		this.hostname = s.hostname;
		this.serviceDescription = s.serviceDescription;
		this.serviceGroups = s.serviceGroups;
		this.isVolatile = s.isVolatile;
		this.checkCommand = s.checkCommand;
		this.commandArgs = s.commandArgs;
		this.maxCheckAttempts = s.maxCheckAttempts;
		this.normalCheckInterval = s.normalCheckInterval;
		this.retryCheckInterval = s.retryCheckInterval;
		this.activeChecksEnabled = s.activeChecksEnabled;
		this.passiveChecksEnabled = s.passiveChecksEnabled;
		this.checkPeriod = s.checkPeriod;
		this.parallelizeChecks = s.parallelizeChecks;
		this.obsessOverService = s.obsessOverService;
		this.checkFreshness = s.checkFreshness;
		this.freshnessThreshold = s.freshnessThreshold;
		this.eventHandler = s.eventHandler;
		this.eventHandlerEnabled = s.eventHandlerEnabled;
		this.lowFlapThreshold = s.lowFlapThreshold;
		this.highFlapThreshold = s.highFlapThreshold;
		this.flapDetectionEnabled = s.flapDetectionEnabled;
		this.processPerfData = s.processPerfData;
		this.retainStatusInformation = s.retainStatusInformation;
		this.retainNonStatusInformation = s.retainNonStatusInformation;
		this.notificationInterval = s.notificationInterval;
		this.notificationPeriod = s.notificationPeriod;
		this.notificationOptions = s.notificationOptions;
		this.notificationsEnabled = s.notificationsEnabled;
		this.contactGroups = s.contactGroups;
		this.stalkingOptions = s.stalkingOptions;
	}
	
	
	public String getCommandArgs()
	{
		return this.commandArgs;
	}
	
	public void setCommandArgs(String commandArgs)
	{
		
		// Make sure we pre-pend a bang to the front of it */
		
		if(commandArgs.indexOf("!") != 0)
		{
			commandArgs = "!" + commandArgs;
		}
		
		this.commandArgs = commandArgs;
	}
	
	public void setIsVolatile(boolean isVolatile)
	{
		this.isVolatile = isVolatile;
	}
	
	public boolean getIsVolatile()
	{
		return this.isVolatile;
	}
	
	public String getServiceDescription()
	{
		return this.serviceDescription;
	}
	
	public void setServiceDescription(String serviceDescription)
	{
		this.serviceDescription = serviceDescription;
	}
	
	public boolean getActiveChecksEnabled()
	{
		return this.activeChecksEnabled;
	}

	public void setActiveChecksEnabled(boolean activeChecksEnabled) 
	{
		this.activeChecksEnabled = activeChecksEnabled;
	}

	public String getCheckCommand()
	{
		return this.checkCommand;
	}

	public void setCheckCommand(String checkCommand) 
	{
		this.checkCommand = checkCommand;
	}

	public boolean getCheckFreshness()
	{
		return this.checkFreshness;
	}

	public void setCheckFreshness(boolean checkFreshness) 
	{
		this.checkFreshness = checkFreshness;
	}

	public String getCheckPeriod() 
	{
		return this.checkPeriod;
	}

	public void setCheckPeriod(String checkPeriod) 
	{
		this.checkPeriod = checkPeriod;
	}

	public String[] getContactGroups() 
	{
		return this.contactGroups;
	}

	public void setContactGroups(String[] contactGroups) 
	{
		this.contactGroups = contactGroups;
	}

	public String[] getEventHandler() 
	{
		return this.eventHandler;
	}

	public void setEventHandler(String[] eventHandler) 
	{
		this.eventHandler = eventHandler;
	}

	public boolean getEventHandlerEnabled() 
	{
		return this.eventHandlerEnabled;
	}

	public void setEventHandlerEnabled(boolean eventHandlerEnabled) 
	{
		this.eventHandlerEnabled = eventHandlerEnabled;
	}

	public boolean getFlapDetectionEnabled() 
	{
		return this.flapDetectionEnabled;
	}

	public void setFlapDetectionEnabled(boolean flapDetectionEnabled) 
	{
		this.flapDetectionEnabled = flapDetectionEnabled;
	}

	public int getFreshnessThreshold() 
	{
		return this.freshnessThreshold;
	}

	public void setFreshnessThreshold(int freshnessThreshold) 
	{
		this.freshnessThreshold = freshnessThreshold;
	}

	public int getHighFlapThreshold()
	{
		return this.highFlapThreshold;
	}

	public void setHighFlapThreshold(int highFlapThreshold) 
	{
		this.highFlapThreshold = highFlapThreshold;
	}

	public String[] getHostname() 
	{
		return this.hostname;
	}

	public void setHostname(String[] hostname) 
	{
		this.hostname = hostname;
	}

	public int getLowFlapThreshold()
	{
		return this.lowFlapThreshold;
	}

	public void setLowFlapThreshold(int lowFlapThreshold)
	{
		this.lowFlapThreshold = lowFlapThreshold;
	}

	public int getMaxCheckAttempts() 
	{
		return this.maxCheckAttempts;
	}

	public void setMaxCheckAttempts(int maxCheckAttempts) 
	{
		this.maxCheckAttempts = maxCheckAttempts;
	}

	public int getNormalCheckInterval()
	{
		return this.normalCheckInterval;
	}

	public void setNormalCheckInterval(int normalCheckInterval) 
	{
		this.normalCheckInterval = normalCheckInterval;
	}

	public int getNotificationInterval()
	{
		return this.notificationInterval;
	}

	public void setNotificationInterval(int notificationInterval) 
	{
		this.notificationInterval = notificationInterval;
	}

	public String[] getNotificationOptions() 
	{
		return this.notificationOptions;
	}

	public void setNotificationOptions(String[] notificationOptions) 
	{
		this.notificationOptions = notificationOptions;
	}

	public String getNotificationPeriod() 
	{
		return this.notificationPeriod;
	}

	public void setNotificationPeriod(String notificationPeriod) 
	{
		this.notificationPeriod = notificationPeriod;
	}

	public boolean getNotificationsEnabled() 
	{
		return this.notificationsEnabled;
	}

	public void setNotificationsEnabled(boolean notificationsEnabled)
	{
		this.notificationsEnabled = notificationsEnabled;
	}

	public boolean getObsessOverService()
	{
		return this.obsessOverService;
	}

	public void setObsessOverService(boolean obsessOverService) 
	{
		this.obsessOverService = obsessOverService;
	}

	public boolean getParallelizeChecks()
	{
		return this.parallelizeChecks;
	}

	public void setParallelizeChecks(boolean parallelizeChecks)
	{
		this.parallelizeChecks = parallelizeChecks;
	}

	public boolean getPassiveChecksEnabled() 
	{
		return this.passiveChecksEnabled;
	}

	public void setPassiveChecksEnabled(boolean passiveChecksEnabled)
	{
		this.passiveChecksEnabled = passiveChecksEnabled;
	}

	public boolean getProcessPerfData()
	{
		return this.processPerfData;
	}

	public void setProcessPerfData(boolean processPerfData) 
	{
		this.processPerfData = processPerfData;
	}

	public boolean getRetainNonStatusInformation()
	{
		return this.retainNonStatusInformation;
	}

	public void setRetainNonStatusInformation(boolean retainNonStatusInformation)
	{
		this.retainNonStatusInformation = retainNonStatusInformation;
	}

	public boolean getRetainStatusInformation() 
	{
		return this.retainStatusInformation;
	}

	public void setRetainStatusInformation(boolean retainStatusInformation)
	{
		this.retainStatusInformation = retainStatusInformation;
	}

	public int getRetryCheckInterval()
	{
		return this.retryCheckInterval;
	}

	public void setRetryCheckInterval(int retryCheckInterval)
	{
		this.retryCheckInterval = retryCheckInterval;
	}

	public String[] getServiceGroups() 
	{
		return this.serviceGroups;
	}

	public void setServiceGroups(String[] serviceGroups)
	{
		this.serviceGroups = serviceGroups;
	}

	public String[] getStalkingOptions()
	{
		return this.stalkingOptions;
	}

	public void setStalkingOptions(String[] stalkingOptions)
	{
		this.stalkingOptions = stalkingOptions;
	}
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("host_name",Utils.arrayToString(getHostname()));
		details.put("service_description",getServiceDescription());
		//details.put("service_groups",arrayToString(getServiceGroups()));
		details.put("is_volatile",Utils.booleanToString(getIsVolatile()));
		details.put("check_command",getCheckCommand() + getCommandArgs());
		details.put("max_check_attempts",String.valueOf(getMaxCheckAttempts()));
		details.put("normal_check_interval",String.valueOf(getNormalCheckInterval()));
		details.put("retry_check_interval",String.valueOf(getRetryCheckInterval()));
		details.put("active_checks_enabled",Utils.booleanToString(getActiveChecksEnabled()));
		details.put("passive_checks_enabled",Utils.booleanToString(getPassiveChecksEnabled()));
		details.put("check_period",getCheckPeriod());
		details.put("parallelize_check",Utils.booleanToString(getParallelizeChecks()));
		details.put("obsess_over_service",Utils.booleanToString(getObsessOverService()));
		details.put("check_freshness",Utils.booleanToString(getCheckFreshness()));
		details.put("freshness_threshold",String.valueOf(getFreshnessThreshold()));
		details.put("event_handler",Utils.arrayToString(getEventHandler()));
		details.put("event_handler_enabled",Utils.booleanToString(getEventHandlerEnabled()));
		details.put("low_flap_threshold",String.valueOf(getLowFlapThreshold()));
		details.put("high_flap_threshold",String.valueOf(getHighFlapThreshold()));
		details.put("flap_detection_enabled",Utils.booleanToString(getFlapDetectionEnabled()));
		details.put("process_perf_data",Utils.booleanToString(getProcessPerfData()));
		details.put("retain_status_information",Utils.booleanToString(getRetainStatusInformation()));
		details.put("retain_nonstatus_information",Utils.booleanToString(getRetainNonStatusInformation()));
		details.put("notification_interval",String.valueOf(getNotificationInterval()));
		details.put("notification_period",getNotificationPeriod());
		details.put("notification_options",Utils.arrayToString(getNotificationOptions()));
		details.put("notifications_enabled",Utils.booleanToString(getNotificationsEnabled()));
		details.put("contact_groups",Utils.arrayToString(getContactGroups()));
		details.put("stalking_options",Utils.arrayToString(getStalkingOptions()));
		
		return details;
		
	}
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("host_name");details.add(Utils.arrayToString(getHostname()));
		details.add("service_description");details.add(getServiceDescription());
		//details.add("service_groups");details.add(arrayToString(getServiceGroups()));
		details.add("is_volatile");details.add(Utils.booleanToString(getIsVolatile()));
		details.add("check_command");details.add(getCheckCommand() + getCommandArgs());
		details.add("max_check_attempts");details.add(String.valueOf(getMaxCheckAttempts()));
		details.add("normal_check_interval");details.add(String.valueOf(getNormalCheckInterval()));
		details.add("retry_check_interval");details.add(String.valueOf(getRetryCheckInterval()));
		details.add("active_checks_enabled");details.add(Utils.booleanToString(getActiveChecksEnabled()));
		details.add("passive_checks_enabled");details.add(Utils.booleanToString(getPassiveChecksEnabled()));
		details.add("check_period");details.add(getCheckPeriod());
		details.add("parallelize_check");details.add(Utils.booleanToString(getParallelizeChecks()));
		details.add("obsess_over_service");details.add(Utils.booleanToString(getObsessOverService()));
		details.add("check_freshness");details.add(Utils.booleanToString(getCheckFreshness()));
		details.add("freshness_threshold");details.add(String.valueOf(getFreshnessThreshold()));
		details.add("event_handler");details.add(Utils.arrayToString(getEventHandler()));
		details.add("event_handler_enabled");details.add(Utils.booleanToString(getEventHandlerEnabled()));
		details.add("low_flap_threshold");details.add(String.valueOf(getLowFlapThreshold()));
		details.add("high_flap_threshold");details.add(String.valueOf(getHighFlapThreshold()));
		details.add("flap_detection_enabled");details.add(Utils.booleanToString(getFlapDetectionEnabled()));
		details.add("process_perf_data");details.add(Utils.booleanToString(getProcessPerfData()));
		details.add("retain_status_information");details.add(Utils.booleanToString(getRetainStatusInformation()));
		details.add("retain_nonstatus_information");details.add(Utils.booleanToString(getRetainNonStatusInformation()));
		details.add("notification_interval");details.add(String.valueOf(getNotificationInterval()));
		details.add("notification_period");details.add(getNotificationPeriod());
		details.add("notification_options");details.add(Utils.arrayToString(getNotificationOptions()));
		details.add("notifications_enabled");details.add(Utils.booleanToString(getNotificationsEnabled()));
		details.add("contact_groups");details.add(Utils.arrayToString(getContactGroups()));
		details.add("stalking_options");details.add(Utils.arrayToString(getStalkingOptions()));
		
		return details;
	}
	
}
