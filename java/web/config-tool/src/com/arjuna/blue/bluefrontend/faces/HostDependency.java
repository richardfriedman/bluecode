package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HostDependency extends BlueObject
{
	private String dependentHostname = "";
	private String hostname = "";
	private boolean inheritsParents = false;
	private String[] executionFailureCriteria;
	private String[] notificationFailureCriteria;
		
	public HostDependency()
	{
		
	}
	
	public HostDependency(HostDependency hd)
	{
		super(hd);
		this.dependentHostname = hd.dependentHostname;
		this.hostname = hd.hostname;
		this.inheritsParents = hd.inheritsParents;
		this.executionFailureCriteria = hd.executionFailureCriteria;
		this.notificationFailureCriteria = hd.notificationFailureCriteria;
	}
	
	public String getDependentHostname()
	{
		return this.dependentHostname;
	}
	
	public void setDependentHostname(String dependentHostname)
	{
		this.dependentHostname = dependentHostname;
	}
	
	public String getHostname()
	{
		return this.hostname;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	public String[] getExecutionFailureCriteria()
	{
		return this.executionFailureCriteria;
	}
	
	public void setExecutionFailureCriteria(String[] executionFailureCriteria) 
	{
		this.executionFailureCriteria = executionFailureCriteria;
	}
	
	public boolean getInheritsParents()
	{
		return this.inheritsParents;
	}
	
	public void setInheritsParents(boolean inheritsParents)
	{
		this.inheritsParents = inheritsParents;
	}
	
	public String[] getNotificationFailureCriteria()
	{
		return this.notificationFailureCriteria;
	}
	
	public void setNotificationFailureCriteria(String[] notificationFailureCriteria)
	{
		this.notificationFailureCriteria = notificationFailureCriteria;
	}

	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("dependent_host_name");details.add(getDependentHostname());
		details.add("host_name");details.add(getHostname());
		details.add("inherits_parent");details.add(Utils.booleanToString(getInheritsParents()));
		details.add("execution_failure_criteria");details.add(Utils.arrayToString(getExecutionFailureCriteria()));
		details.add("notification_failure_criteria");details.add(Utils.arrayToString(getNotificationFailureCriteria()));
		
		return details;
	}
	
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("dependent_host_name",getDependentHostname());
		details.put("host_name",getHostname());
		details.put("inherits_parent",Utils.booleanToString(getInheritsParents()));
		details.put("execution_failure_criteria",Utils.arrayToString(getExecutionFailureCriteria()));
		details.put("notification_failure_criteria",Utils.arrayToString(getNotificationFailureCriteria()));
		
		return details;
	}
	
}
