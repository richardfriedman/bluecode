package com.arjuna.blue.bluefrontend.faces;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceEscalation extends BlueObject
{
	private String hostname = "";
	private String serviceDescription = "";
	private String[] contactGroups;
	private int firstNotification;
	private int lastNotification;
	private int notificationInterval;
	private String escalationPeriod = "";
	private String[] escalationOptions;
		
	public ServiceEscalation()
	{
		
	}
	
	public ServiceEscalation(ServiceEscalation serviceEscalation)
	{
		super(serviceEscalation);
		this.hostname = serviceEscalation.hostname;
		this.serviceDescription = serviceEscalation.serviceDescription;
		this.contactGroups = serviceEscalation.contactGroups;
		this.firstNotification = serviceEscalation.firstNotification;
		this.lastNotification = serviceEscalation.lastNotification;
		this.notificationInterval = serviceEscalation.notificationInterval;
		this.escalationPeriod = serviceEscalation.escalationPeriod;
		this.escalationOptions = serviceEscalation.escalationOptions;
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

	public String getServiceDescription() 
	{
		return this.serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) 
	{
		this.serviceDescription = serviceDescription;
	}
	
	public void setServiceHost(String serviceHost)
	{
		String bits[] = serviceHost.split(",");
		setHostname(bits[0]);
		setServiceDescription(bits[1]);
	}
	
	public String getServiceHost()
	{
		return getHostname() + "," + getServiceDescription();
	}
	
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("host_name",getHostname());
		details.put("service_description",getServiceDescription());
		details.put("contact_groups",Utils.arrayToString(getContactGroups()));
		details.put("first_notification",String.valueOf(getFirstNotification()));
		details.put("last_notification",String.valueOf(getLastNotification()));
		details.put("notification_interval",String.valueOf(getNotificationInterval()));
		details.put("escalation_period",getEscalationPeriod());
		details.put("escalation_options",Utils.arrayToString(getEscalationOptions()));
		
		return details;
	}
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("host_name");details.add(getHostname());
		details.add("service_description");details.add(getServiceDescription());
		details.add("contact_groups");details.add(Utils.arrayToString(getContactGroups()));
		details.add("first_notification");details.add(String.valueOf(getFirstNotification()));
		details.add("last_notification");details.add(String.valueOf(getLastNotification()));
		details.add("notification_interval");details.add(String.valueOf(getNotificationInterval()));
		details.add("escalation_period");details.add(getEscalationPeriod());
		details.add("escalation_options");details.add(Utils.arrayToString(getEscalationOptions()));
		
		return details;
	}
	
}
