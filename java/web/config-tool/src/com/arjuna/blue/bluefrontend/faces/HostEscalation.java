package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HostEscalation extends BlueObject
{
	private String hostname = "";
	private String[] hostGroups;
	private String[] contactGroups;
	private int firstNotification;
	private int lastNotification;
	private int notificationInterval;
	private String escalationPeriod = "";
	private String[] escalationOptions;
		
	public HostEscalation()
	{
		
	}
	
	public HostEscalation(HostEscalation he)
	{
		super(he);
		this.hostname = he.hostname;
		this.hostGroups = he.hostGroups;
		this.contactGroups = he.contactGroups;
		this.firstNotification = he.firstNotification;
		this.lastNotification = he.lastNotification;
		this.notificationInterval = he.notificationInterval;
		this.escalationPeriod = he.escalationPeriod;
		this.escalationOptions = he.escalationOptions;
	}
	
	public String[] getContactGroups()
	{
		return this.contactGroups;
	}

	public void setContactGroups(String[] contactGroups)
	{
		this.contactGroups = contactGroups;
	}

	public String[] getEscalationOptions()
	{
		return this.escalationOptions;
	}

	public void setEscalationOptions(String[] escalationOptions)
	{
		this.escalationOptions = escalationOptions;
	}

	public String getEscalationPeriod()
	{
		return this.escalationPeriod;
	}

	public void setEscalationPeriod(String escalationPeriod) 
	{
		this.escalationPeriod = escalationPeriod;
	}

	public int getFirstNotification()
	{
		return this.firstNotification;
	}

	public void setFirstNotification(int firstNotification)
	{
		this.firstNotification = firstNotification;
	}

	public String[] getHostGroups() 
	{
		return this.hostGroups;
	}

	public void setHostGroups(String[] hostGroups)
	{
		this.hostGroups = hostGroups;
	}

	public String getHostname()
	{
		return this.hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public int getLastNotification() 
	{
		return this.lastNotification;
	}

	public void setLastNotification(int lastNotification)
	{
		this.lastNotification = lastNotification;
	}

	public int getNotificationInterval() 
	{
		return this.notificationInterval;
	}

	public void setNotificationInterval(int notificationInterval)
	{
		this.notificationInterval = notificationInterval;
	}
	
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("host_name");details.add(getHostname());
		details.add("hostgroup_name");details.add(Utils.arrayToString(getHostGroups()));
		details.add("contact_groups");details.add(Utils.arrayToString(getContactGroups()));
		details.add("first_notification");details.add(String.valueOf(getFirstNotification()));
		details.add("last_notification");details.add(String.valueOf(getLastNotification()));
		details.add("notification_interval");details.add(String.valueOf(getNotificationInterval()));
		details.add("escalation_period");details.add(getEscalationPeriod());
		details.add("escalation_options");details.add(Utils.arrayToString(getEscalationOptions()));
		
		return details;
		
	}
	
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("host_name",getHostname());
		details.put("hostgroup_name",Utils.arrayToString(getHostGroups()));
		details.put("contact_groups",Utils.arrayToString(getContactGroups()));
		details.put("first_notification",String.valueOf(getFirstNotification()));
		details.put("last_notification",String.valueOf(getLastNotification()));
		details.put("notification_interval",String.valueOf(getNotificationInterval()));
		details.put("escalation_period",getEscalationPeriod());
		details.put("escalation_options",Utils.arrayToString(getEscalationOptions()));
		
		return details;
	}
	
}
