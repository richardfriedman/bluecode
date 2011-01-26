package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceDependency extends BlueObject
{

	private String dependentHostname = "";
	private String dependentServiceDescription = "";
	private String hostname = "";
	private String serviceDescription = "";
	private boolean inheritsParents = false;
	private String[] executionFailureCriteria;
	private String[] notificationFailureCriteria;
	
	public ServiceDependency()
	{
		
	}
	
	public ServiceDependency(ServiceDependency sd)
	{
		super(sd);
		this.dependentHostname = sd.dependentHostname;
		this.dependentServiceDescription = sd.dependentServiceDescription;
		this.hostname = sd.hostname;
		this.serviceDescription = sd.serviceDescription;
		this.inheritsParents = sd.inheritsParents;
		this.executionFailureCriteria = sd.executionFailureCriteria;
		this.notificationFailureCriteria = sd.notificationFailureCriteria;
	}
	
	public String getDependentHostname()
	{
		return this.dependentHostname;
	}
	
	public void setDependentHostname(String dependentHostname)
	{
		this.dependentHostname = dependentHostname;
	}
	
	public String getDependentServiceDescription()
	{
		return this.dependentServiceDescription;
	}
	
	public void setDependentServiceDescription(String dependentServiceDescription)
	{
		this.dependentServiceDescription = dependentServiceDescription;
	}
	
	public String[] getExecutionFailureCriteria()
	{
		return this.executionFailureCriteria;
	}
	
	public void setExecutionFailureCriteria(String[] executionFailureCriteria)
	{
		this.executionFailureCriteria = executionFailureCriteria;
	}
	
	public String getHostname()
	{
		return this.hostname;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
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
	
	public String getDependentServiceHost()
	{
		return getDependentHostname() + "," + getDependentServiceDescription(); 
	}
	
	public void setDependentServiceHost(String dependentServiceHost)
	{
			String bits[] = dependentServiceHost.split(",");
		
			setDependentHostname(bits[0]);
			setDependentServiceDescription(bits[1]);
		
	}
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("dependent_host_name",getDependentHostname());
		details.put("dependent_service_description",getDependentServiceDescription());
		details.put("host_name",getHostname());
		details.put("service_description",getServiceDescription());
		details.put("inherits_parent",Utils.booleanToString(getInheritsParents()));
		details.put("execution_failure_criteria",Utils.arrayToString(getExecutionFailureCriteria()));
		details.put("notification_failure_criteria",Utils.arrayToString(getNotificationFailureCriteria()));
		
		return details;
	}
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("dependent_host_name");details.add(getDependentHostname());
		details.add("dependent_service_description");details.add(getDependentServiceDescription());
		details.add("host_name");details.add(getHostname());
		details.add("service_description");details.add(getServiceDescription());
		details.add("inherits_parent");details.add(Utils.booleanToString(getInheritsParents()));
		details.add("execution_failure_criteria");details.add(Utils.arrayToString(getExecutionFailureCriteria()));
		details.add("notification_failure_criteria");details.add(Utils.arrayToString(getNotificationFailureCriteria()));
		
		return details;
	}
	
}
