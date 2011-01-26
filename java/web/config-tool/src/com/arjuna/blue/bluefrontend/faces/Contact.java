/*
 * A class used to represent the Contact object within the Blue framework.
 */

package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Contact extends BlueObject
{
	private String contactName ="";
	private String alias ="";
	private String[] contactGroups;
	private String hostNotificationPeriod = "";
	private String serviceNotificationPeriod = "";
	private String[] hostNotificationOptions;
	private String[] serviceNotificationOptions;
	private String[] hostNotificationCommands;
	private String[] serviceNotificationCommands;
	private String email ="";
	private String pager = "";
	
	// TODO - Add in addressx variable.
	
	public Contact()
	{
		
	}
	
	public Contact(Contact contact)
	{
		super(contact);
		this.contactName = contact.contactName;
		this.alias = contact.alias;
		this.contactGroups = contact.contactGroups;
		this.hostNotificationPeriod = contact.hostNotificationPeriod;
		this.serviceNotificationPeriod = contact.serviceNotificationPeriod;
		this.hostNotificationOptions = contact.hostNotificationOptions;
		this.serviceNotificationOptions = contact.serviceNotificationOptions;
		this.hostNotificationCommands = contact.hostNotificationCommands;
		this.serviceNotificationCommands = contact.serviceNotificationCommands;
		this.email = contact.email;
		this.pager = contact.pager;
	}
	
	public String getName()
	{
		return this.contactName;
	}
	
	public void setContactName(String contactName)
	{
		this.contactName = contactName;
	}
	
	public String getContactName()
	{
		return this.contactName;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	public void setContactGroups(String[] contactGroups)
	{
		this.contactGroups = contactGroups;
	}
	
	public String[] getContactGroups()
	{
		return this.contactGroups;
	}
	
	public void setHostNotificationPeriod(String hostNotificationPeriod)
	{
		this.hostNotificationPeriod = hostNotificationPeriod;
	}
	
	public String getHostNotificationPeriod()
	{
		return this.hostNotificationPeriod;
	}
	
	public void setServiceNotificationPeriod(String serviceNotificationPeriod)
	{
		this.serviceNotificationPeriod = serviceNotificationPeriod;
	}
	
	public String getServiceNotificationPeriod()
	{
		return this.serviceNotificationPeriod;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getEmail()
	{
		return this.email;
	}
	
	public void setHostNotificationCommands(String[] hostNotificationCommands)
	{
		this.hostNotificationCommands = hostNotificationCommands;
	}
	
	public String[] getHostNotificationCommands()
	{
		return this.hostNotificationCommands;
	}
	
	public void setServiceNotificationCommands(String[] serviceNotificationCommands)
	{
		this.serviceNotificationCommands = serviceNotificationCommands;
	}
	
	public String[] getServiceNotificationCommands()
	{
		return this.serviceNotificationCommands;
	}
	
	public void setHostNotificationOptions(String[] hostNotificationOptions)
	{
		this.hostNotificationOptions = hostNotificationOptions;
	}
	
	public String[] getHostNotificationOptions()
	{
		return this.hostNotificationOptions;
	}
	
	public void setServiceNotificationOptions(String[] serviceNotificationOptions)
	{
		this.serviceNotificationOptions = serviceNotificationOptions;
	}
	
	public String[] getServiceNotificationOptions()
	{
		return this.serviceNotificationOptions;
	}
	
	public void setPager(String pager)
	{
		this.pager = pager;
	}
	
	public String getPager()
	{
		return this.pager;
	}
	
	public List<String> getObjectDetails()
	{
		/* unfortunately we have to return the values in this way to retain element
		 * ordering to conform with the XML schema
		 */
		
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("contact_name");details.add(getContactName());
		details.add("alias");details.add(getAlias());
		//details.add("contactgroups");details.add(arrayToString(getContactGroups()));
		details.add("service_notification_period");details.add(String.valueOf(getServiceNotificationPeriod()));
		details.add("host_notification_period");details.add(String.valueOf(getHostNotificationPeriod()));
		details.add("service_notification_options");details.add(Utils.arrayToString(getServiceNotificationOptions()));
		details.add("host_notification_options");details.add(Utils.arrayToString(getHostNotificationOptions()));
		details.add("service_notification_commands");details.add(Utils.arrayToString(getServiceNotificationCommands()));
		details.add("host_notification_commands");details.add(Utils.arrayToString(getServiceNotificationCommands()));
		details.add("email");details.add(getEmail());
		details.add("pager");details.add(getPager());
		
		return details;
		
	}
	
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		
		details.put("id",String.valueOf(getId()));
		details.put("contact_name",getContactName());
		details.put("alias",getAlias());
		//details.put("contactgroups",arrayToString(getContactGroups()));
		details.put("service_notification_period",getServiceNotificationPeriod());
		details.put("host_notification_period",getHostNotificationPeriod());
		details.put("service_notification_options",Utils.arrayToString(getServiceNotificationOptions()));
		details.put("host_notification_options",Utils.arrayToString(getHostNotificationOptions()));
		details.put("service_notification_commands",Utils.arrayToString(getServiceNotificationCommands()));
		details.put("host_notification_commands",Utils.arrayToString(getHostNotificationCommands()));
		details.put("email",getEmail());
		details.put("pager",getPager());
		
		return details;
		
	}
	
}
