package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Host extends BlueObject
{
	private String hostname = "";
	private String alias = "";
	private String IPAddress = "";
	private String[] parents;
	private String[] hostGroups;
	private String checkCommand = "";
	private boolean checksEnabled = false;
	private int maxCheckAttempts;
	private int checkInterval;
	private boolean activeChecksEnabled = false;
	private boolean passiveChecksEnabled = false;
	private String checkPeriod = "";
	private boolean obsessOverHost = false;
	private boolean checkFreshness = false;
	private int freshnessThreshold;
	private String eventHandler = "";
	private boolean eventHandlerEnabled = false;
	private int lowFlapThreshold;
	private int highFlapThreshold;
	private boolean flapDetectionEnabled = false;
	private boolean failurePredictionEnabled = false;
	private boolean processPerfData = false;
	private boolean retainStatusInformation = false;
	private boolean retainNonStatusInformation = false;
	private int notificationInterval;
	private String notificationPeriod = "";
	private String[] notificationOptions;
	private boolean notificationsEnabled = false;
	private String[] stalkingOptions;
	private String[] contactGroups;
	private String templateName;
		
	public Host()
	{
		
	}
	
	/* Object to Object constructor */
	
	public Host(Host host)
	{
		super(host);
		
		this.hostname = host.hostname;
		this.alias = host.alias;
		this.IPAddress = host.IPAddress;
		this.parents = host.parents;
		this.checkCommand = host.checkCommand;
		this.checksEnabled = host.checksEnabled;
		this.maxCheckAttempts = host.maxCheckAttempts;
		this.checkInterval = host.checkInterval;
		this.activeChecksEnabled = host.activeChecksEnabled;
		this.passiveChecksEnabled = host.passiveChecksEnabled;
		this.checkPeriod = host.checkPeriod;
		this.obsessOverHost = host.obsessOverHost;
		this.checkFreshness = host.checkFreshness;
		this.freshnessThreshold = host.freshnessThreshold;
		this.eventHandler = host.eventHandler;
		this.eventHandlerEnabled = host.eventHandlerEnabled;
		this.lowFlapThreshold = host.lowFlapThreshold;
		this.highFlapThreshold = host.highFlapThreshold;
		this.flapDetectionEnabled = host.flapDetectionEnabled;
		this.processPerfData = host.processPerfData;
		this.retainStatusInformation = host.retainStatusInformation;
		this.retainNonStatusInformation = host.retainNonStatusInformation;
		this.notificationsEnabled = host.notificationsEnabled;
		this.notificationInterval = host.notificationInterval;
		this.notificationPeriod = host.notificationPeriod;
		this.notificationOptions = host.notificationOptions;
		this.contactGroups = host.contactGroups;
		this.stalkingOptions = host.stalkingOptions;
		this.templateName = host.templateName;
		this.hostGroups = host.hostGroups;
	}

	public void setName(String name)
	{
		this.hostname = name;
	}
	
	public String getName()
	{
		return this.hostname;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	public String getHostname()
	{
		return this.hostname;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	public void setIPAddress(String ipAddress)
	{
		this.IPAddress = ipAddress;
	}
	
	public String getIPAddress()
	{
		return this.IPAddress;
	}
	
	public void setParents(String[] parents)
	{
		this.parents = parents;
	}
	
	public String[] getParents()
	{
		return this.parents;
	}
	
	public void setHostGroups(String[] hostGroups)
	{
		this.hostGroups = hostGroups;
	}
	
	public String[] getHostGroups()
	{
		return this.hostGroups;
	}
	
	public void setCheckCommand(String checkCommand)
	{
		this.checkCommand = checkCommand;
	}
	
	public String getCheckCommand()
	{
		return this.checkCommand;
	}
	
	public void setMaxCheckAttempts(int maxCheckAttempts)
	{
		this.maxCheckAttempts = maxCheckAttempts;
	}
	
	public int getMaxCheckAttempts()
	{
		return this.maxCheckAttempts;
	}
	
	public void setCheckInterval(int checkInterval)
	{
		this.checkInterval = checkInterval;
	}
	
	public int getCheckInterval()
	{
		return this.checkInterval;
	}
	
	public void setActiveChecksEnabled(boolean activeChecksEnabled)
	{
		this.activeChecksEnabled = activeChecksEnabled;
	}
	
	public boolean getActiveChecksEnabled()
	{
		return this.activeChecksEnabled;
	}
	
	public void setPassiveChecksEnabled(boolean passiveChecksEnabled)
	{
		this.passiveChecksEnabled = passiveChecksEnabled;
	}
	
	public boolean getPassiveChecksEnabled()
	{
		return this.passiveChecksEnabled;
	}
	
	public void setCheckPeriod(String checkPeriod)
	{
		this.checkPeriod = checkPeriod;
	}
	
	public String getCheckPeriod()
	{
		return this.checkPeriod;
	}
	
	public void setObsessOverHost(boolean obsessOverHost)
	{
		this.obsessOverHost = obsessOverHost;
	}
	
	public boolean getObsessOverHost()
	{
		return this.obsessOverHost;
	}
	
	public void setCheckFreshness(boolean checkFreshness)
	{
		this.checkFreshness = checkFreshness;
	}
	
	public boolean getCheckFreshness()
	{
		return this.checkFreshness;
	}
	
	public void setFreshnessThreshold(int freshnessThreshold)
	{
		this.freshnessThreshold = freshnessThreshold;
	}
	
	public int getFreshnessThreshold()
	{
		return this.freshnessThreshold;
	}
	
	public void setEventHandler(String eventHandler)
	{
		this.eventHandler = eventHandler;
	}
	
	public String getEventHandler()
	{
		return this.eventHandler;
	}
	
	public void setEventHandlerEnabled(boolean eventHandlerEnabled)
	{
		this.eventHandlerEnabled = eventHandlerEnabled;
	}
	
	public boolean getEventHandlerEnabled()
	{
		return this.eventHandlerEnabled;
	}
	
	public void setLowFlapThreshold(int lowFlapThreshold)
	{
		this.lowFlapThreshold = lowFlapThreshold;
	}
	
	public int getLowFlapThreshold()
	{
		return this.lowFlapThreshold;
	}
	
	public void setHighFlapThreshold(int highFlapThreshold)
	{
		this.highFlapThreshold = highFlapThreshold;
	}
	
	public int getHighFlapThreshold()
	{
		return this.highFlapThreshold;
	}
	
	public void setFlapDetectionEnabled(boolean flapDetectionEnabled)
	{
		this.flapDetectionEnabled = flapDetectionEnabled;
	}
	
	public boolean getFlapDetectionEnabled()
	{
		return this.flapDetectionEnabled;
	}
	
	public void setProcessPerfData(boolean processPerfData)
	{
		this.processPerfData = processPerfData;
	}
	
	public boolean getProcessPerfData()
	{
		return this.processPerfData;
	}
	
	public void setRetainStatusInformation(boolean retainStatusInformation)
	{
		this.retainStatusInformation = retainStatusInformation;
	}
	
	public boolean getRetainStatusInformation()
	{
		return this.retainStatusInformation;
	}
	
	public void setRetainNonStatusInformation(boolean retainNonStatusInformation)
	{
		this.retainNonStatusInformation = retainNonStatusInformation;
	}
	
	public boolean getRetainNonStatusInformation()
	{
		return this.retainNonStatusInformation;
	}
	
	public void setNotificationInterval(int notificationInterval)
	{
		this.notificationInterval = notificationInterval;
	}
	
	public int getNotificationInterval()
	{
		return this.notificationInterval;
	}
	
	public void setNotificationPeriod(String notificationPeriod)
	{
		this.notificationPeriod = notificationPeriod;
	}
	
	public String getNotificationPeriod()
	{
		return this.notificationPeriod;
	}
	
	public void setNotificationOptions(String[] notificationOptions)
	{
		this.notificationOptions = notificationOptions;
	}
	
	public String[] getNotificationOptions()
	{
		return this.notificationOptions;
	}
	
	public void setContactGroups(String[] contactGroups)
	{
		this.contactGroups = contactGroups;
	}
	
	public String[] getContactGroups()
	{
		return this.contactGroups;
	}
	
	public void setNotificationsEnabled(boolean notificationsEnabled)
	{
		this.notificationsEnabled = notificationsEnabled;
	}
	
	public boolean getNotificationsEnabled()
	{
		return this.notificationsEnabled;
	}

	public void setStalkingOptions(String[] stalkingOptions)
	{
		this.stalkingOptions = stalkingOptions;
	}
	
	public String[] getStalkingOptions()
	{
		return this.stalkingOptions;
	}
	
	public String getTemplateName()
	{
		return this.templateName;
	}
	
	public void setTemplateName(String templateName)
	{
		this.templateName = templateName;
	}
	
	public boolean getChecksEnabled()
	{
		return this.checksEnabled;
	}
	
	public void setChecksEnabled(boolean checksEnabled)
	{
		this.checksEnabled = checksEnabled;
	}
	
	public boolean getFailurePredictionEnabled()
	{
		return this.failurePredictionEnabled;
	}
	
	public void setFailurePredictionEnabled(boolean failurePredictionEnabled)
	{
		this.failurePredictionEnabled = failurePredictionEnabled;
	}
	
	
	/*
	 * Returns a HashMap with all the current attribute/value pairings for this Host.
	 * 
	 *  @return = HashMap,contains all current attribute/value pairings for this host.
	 */
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("host_name");details.add(getHostname());
		details.add("alias");details.add(getAlias());
		details.add("address");details.add(getIPAddress());
		details.add("parents");details.add(Utils.arrayToString(getParents()));
		//details.add("hostgroups");details.add(arrayToString(getHostGroups()));
		details.add("checks_enabled");details.add(Utils.booleanToString(getChecksEnabled()));
		details.add("check_command");details.add(getCheckCommand());
		details.add("max_check_attempts");details.add(String.valueOf(getMaxCheckAttempts()));
		details.add("check_interval");details.add(String.valueOf(getCheckInterval()));
		details.add("checks_enabled");details.add(Utils.booleanToString(getChecksEnabled()));
		details.add("active_checks_enabled");details.add(Utils.booleanToString(getActiveChecksEnabled()));
		details.add("passive_checks_enabled");details.add(Utils.booleanToString(getPassiveChecksEnabled()));
		details.add("check_period");details.add(getCheckPeriod());
		details.add("obsess_over_host");details.add(Utils.booleanToString(getObsessOverHost()));
		details.add("check_freshness");details.add(Utils.booleanToString(getCheckFreshness()));
		details.add("freshness_threshold");details.add(String.valueOf(getFreshnessThreshold()));
		details.add("event_handler");details.add(getEventHandler());
		details.add("event_handler_enabled");details.add(Utils.booleanToString(getEventHandlerEnabled()));
		details.add("low_flap_threshold");details.add(String.valueOf(getLowFlapThreshold()));
		details.add("high_flap_threshold");details.add(String.valueOf(getHighFlapThreshold()));
		details.add("flap_detection_enabled");details.add(Utils.booleanToString(getFlapDetectionEnabled()));
		details.add("failure_prediction_enabled");details.add(Utils.booleanToString(getFailurePredictionEnabled()));
		details.add("process_perf_data");details.add(Utils.booleanToString(getProcessPerfData()));
		details.add("retain_status_information");details.add(Utils.booleanToString(getRetainStatusInformation()));
		details.add("retain_nonstatus_information");details.add(Utils.booleanToString(getRetainNonStatusInformation()));
		details.add("contact_groups");details.add(Utils.arrayToString(getContactGroups()));
		details.add("notifications_enabled");details.add(Utils.booleanToString(getNotificationsEnabled()));
		details.add("notification_interval");details.add(String.valueOf(getNotificationInterval()));
		details.add("notification_period"); details.add(getNotificationPeriod());
		details.add("notification_options");details.add(Utils.arrayToString(getNotificationOptions()));
		details.add("notifications_enabled");details.add(Utils.booleanToString(getNotificationsEnabled()));
		details.add("stalking_options");details.add(Utils.arrayToString(getStalkingOptions()));
		
		return details;
		
	}
	
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		
		details.put("id",String.valueOf(getId()));
		details.put("host_name",getHostname());
		details.put("alias",getAlias());
		details.put("address",getIPAddress());
		details.put("parents",Utils.arrayToString(getParents()));
		//details.put("hostgroups",arrayToString(getHostGroups()));
		details.put("check_command",getCheckCommand());
		details.put("max_check_attempts",String.valueOf(getMaxCheckAttempts()));
		details.put("check_interval",String.valueOf(getCheckInterval()));
		details.put("checks_enabled",Utils.booleanToString(getChecksEnabled()));
		details.put("active_checks_enabled",Utils.booleanToString(getActiveChecksEnabled()));
		details.put("passive_checks_enabled",Utils.booleanToString(getPassiveChecksEnabled()));
		details.put("check_period",getCheckPeriod());
		details.put("obsess_over_host",Utils.booleanToString(getObsessOverHost()));
		details.put("check_freshness",Utils.booleanToString(getCheckFreshness()));
		details.put("freshness_threshold",String.valueOf(getFreshnessThreshold()));
		details.put("event_handler",getEventHandler());
		details.put("event_handler_enabled",Utils.booleanToString(getEventHandlerEnabled()));
		details.put("low_flap_threshold",String.valueOf(getLowFlapThreshold()));
		details.put("high_flap_threshold",String.valueOf(getHighFlapThreshold()));
		details.put("flap_detection_enabled",Utils.booleanToString(getFlapDetectionEnabled()));
		details.put("failure_prediction_enabled",Utils.booleanToString(getFailurePredictionEnabled()));
		details.put("process_perf_data",Utils.booleanToString(getProcessPerfData()));
		details.put("retain_status_information",Utils.booleanToString(getRetainStatusInformation()));
		details.put("retain_nonstatus_information",Utils.booleanToString(getRetainNonStatusInformation()));
		details.put("contact_groups",Utils.arrayToString(getContactGroups()));
		details.put("notifications_enabled",Utils.booleanToString(getNotificationsEnabled()));
		details.put("notification_interval",String.valueOf(getNotificationInterval()));
		details.put("notification_period",getNotificationPeriod());
		details.put("notification_options",Utils.arrayToString(getNotificationOptions()));
		details.put("notifications_enabled",Utils.booleanToString(getNotificationsEnabled()));
		details.put("stalking_options",Utils.arrayToString(getStalkingOptions()));
		
		return details;
	}
	
}
