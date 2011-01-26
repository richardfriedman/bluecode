package org.blue.star.registry.objects;

/**
 * This class is used to represent the new dynamic_template object type.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class DynamicTemplate 
{
	/** The remote_template name of this dynamic template */
	private String remoteTemplateName;
	/** The name of the host this template uses */
	private String usesHost;
	/** The services that this template runs */
	private String[] runsServices;
	/** The contact groups associated with this service */
	private String[] contactGroups;
	/** The host group this dynamic template should join */
	private String joinsHostGroup;
	/** Are registrations of this type persistent */
	private boolean persistRegistration;
	
	/** Blank Constructor */
	public DynamicTemplate()
	{
		
	}
	
	/**
	 * Constructor that takes the full parameter set.
	 * @param rTemplateName - The remote template name of this dynamic template.
	 * @param uHost - The host definition that this dynamic template uses.
	 * @param rServices - The services that are run on this dynamic template.
	 * @param cGroups - The contact groups that this dynamic template uses
	 * @param jhostGroups - The host groups that this dynamic template uses.
	 * @param persist - Is this dynamic template registration persistent.
	 */
	public DynamicTemplate(String rTemplateName,String uHost,String[] rServices,String[] cGroups,String jhostGroups,boolean persist)
	{
		this.remoteTemplateName = rTemplateName;
		this.usesHost = uHost;
		this.runsServices = rServices;
		this.contactGroups = cGroups;
		this.joinsHostGroup = jhostGroups;
		this.persistRegistration = persist;
	}
	
	/**
	 * This method returns the current list of contactgroups associated with this dynamic template.
	 * @return - String[] of the currently associated contact groups.
	 */
	public String[] getContactGroups() {
		return contactGroups;
	}
	
	/**
	 * This method sets the current list of contact groups
	 * @param contactGroups - The list of contact groups.
	 */
	public void setContactGroups(String[] contactGroups) {
		this.contactGroups = contactGroups;
	}
	
	/**
	 * This method gets the current host group that this dynamic template joins.
	 * @return - The name of the current host group that is joined.
	 */
	public String getJoinsHostGroup() {
		return joinsHostGroup;
	}
	
	/**
	 * This method sets the current host group to be joined.
	 * @param joinsHostGroup - The name of the host group to join.
	 */
	public void setJoinsHostGroup(String joinsHostGroup) {
		this.joinsHostGroup = joinsHostGroup;
	}
	
	/**
	 * This method determines if this registration is persistent.
	 * @return - true if this template supports persistent registrations.
	 */
	public boolean isPersistRegistration() {
		return persistRegistration;
	}
	
	/**
	 *  This method sets if this registrarion should be persistent.
	 * @param persistRegistration - boolean, true if the registration should be persisted.
	 */
	public void setPersistRegistration(boolean persistRegistration) {
		this.persistRegistration = persistRegistration;
	}
	
	/**
	 * Gets the remote template name associated with this dynamic template.
	 * @return - The name of the remote template.
	 */
	public String getRemoteTemplateName() {
		return remoteTemplateName;
	}
	
	/**
	 * This method sets the remote template name of this dynamic template.
	 * @param remoteTemplateName
	 */
	public void setRemoteTemplateName(String remoteTemplateName) {
		this.remoteTemplateName = remoteTemplateName;
	}
	
	/**
	 * This method gets the services that currently run on this dynamic template.
	 * @return - The array of services that currently run on this dynamic template.
	 */
	public String[] getRunsServices() {
		return runsServices;
	}
	
	/**
	 * This method sets the services that run on this dynamic template.
	 * @param runsServices - The array of services that should run on this template.
	 */
	public void setRunsServices(String[] runsServices) {
		this.runsServices = runsServices;
	}
	
	/**
	 * This method gets the host that is currently used by this template.
	 * @return - the name of the host that is currently used by this template.
	 */
	public String getUsesHost() {
		return usesHost;
	}
	
	/**
	 *  This method sets the host that is currently used by the template.
	 * @param usesHost
	 */
	public void setUsesHost(String usesHost) {
		this.usesHost = usesHost;
	}
}