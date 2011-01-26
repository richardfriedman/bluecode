package org.blue.star.registry.xod;

import org.blue.star.include.common_h;

/**
 * <p>This class is used to represent the DynamicTemplate object type within the registry
 * XOD system before the object has been resolved and expanded.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class XODDynamicTemplate
{
	/** The template (if any) this object uses */
	private String template;
	
	/** The remote template name of this dynamic template */
	private String remoteTemplateName;
	
	/** The host definition this dynamic template uses */
	private String usesHost;
	
	/** The services that this dynamic template runs */
	private String runsServices;
	
	/** The contact groups this dynamic template is associated with */
	private String contactGroups;
	
	/** The hostgroup (if any) this template uses */
	private String joinsHostGroup;
	
	/** Is this dynamic registration to be persisted */
	private int persistRegistration;
	
	/** Has the object been resolved */
	private int hasBeenResolved = common_h.FALSE;
	
	/** Is this object to be registered */
	private int registerObject = common_h.FALSE;
	
	/** The config file that this dynamic template is defined in */
	private int configFile;
	
	/** The start line that this object definition begins on */
	private int startLine;

	/**
	 * Get then config file this object is defined in.
	 * @return - int, the numerical identifier of the config file this object is defined in.
	 */
	public int getConfigFile() {
		return configFile;
	}
	
	/**
	 * Set the numerical identifier of the config file this object is defined in.
	 * @param configFile - int, the numerical identifier of the config file this object
	 * is defined in.
	 */
	public void setConfigFile(int configFile) {
		this.configFile = configFile;
	}

	/**
	 * Get any contact groups associated with this dynamic template.
	 * @return - The string list of contact groups asscoiated with this object.
	 */
	public String getContactGroups() {
		return contactGroups;
	}

	/**
	 * Set any contact groups associated with this dynamic template.
	 * @param contactGroups - The contact groups associated with this dynamic template.
	 */
	public void setContactGroups(String contactGroups) {
		this.contactGroups = contactGroups;
	}

	/**
	 * Get whether or not this object has been resolved.
	 * @return - int, common_h.TRUE if this object has been resolved.
	 */
	public int getHasBeenResolved() {
		return hasBeenResolved;
	}
	
	/**
	 * Set whether or not this object has been resolved.
	 * @param hasBeenResolved - int, common_h.TRUE if the object has been resolved.
	 */
	public void setHasBeenResolved(int hasBeenResolved) {
		this.hasBeenResolved = hasBeenResolved;
	}

	/**
	 * Get the hostgroup that this object joins.
	 * @return - The host group that this object joins.
	 */
	public String getJoinsHostGroup() {
		return joinsHostGroup;
	}

	/**
	 * Set the host group that this object joins.
	 * @param joinsHostGroup - The hostgroup that this object joins.
	 */
	public void setJoinsHostGroup(String joinsHostGroup) {
		this.joinsHostGroup = joinsHostGroup;
	}

	/**
	 * Get whether or not this object registration should be made persistent.
	 * @return - int, common_h.TRUE if the registration should be persisted.
	 */
	public int getPersistRegistration() {
		return persistRegistration;
	}

	/**
	 * Set whether or not this object registration should be made persistent.
	 * @param persistRegistration - int, common_h.TRUE if this object registration
	 * should be persisted.
	 */
	public void setPersistRegistration(int persistRegistration) {
		this.persistRegistration = persistRegistration;
	}

	/**
	 * Get whether or not this object should be registered.
	 * @return - int, common_h.TRUE if this object should be registered.
	 */
	public int getRegisterObject() {
		return registerObject;
	}

	/**
	 * Set whether or not this object should be registered.
	 * @param registerObject - int, common_h.TRUE if this object should be registered
	 */
	public void setRegisterObject(int registerObject) {
		this.registerObject = registerObject;
	}
	
	/**
	 * Get the remote template name of this dynamic template.
	 * @return - String, the remote template name of this dynamic template.
	 */
	public String getRemoteTemplateName() {
		return remoteTemplateName;
	}

	/**
	 * Set the remote template name of this dynamic template.
	 * @param remoteTemplateName - String, the remote template name of this dynamic template.
	 */
	public void setRemoteTemplateName(String remoteTemplateName) {
		this.remoteTemplateName = remoteTemplateName;
	}

	/**
	 * Get the services that run on this dynamic template definition.
	 * @return - String, the services that run on this dynamic template definition.
	 */
	public String getRunsServices() {
		return runsServices;
	}

	/**
	 * Set the services that run on this dynamic template definition.
	 * @param runsServices - String, the services that run on this dynamic template
	 */
	public void setRunsServices(String runsServices) {
		this.runsServices = runsServices;
	}

	/**
	 * Get the line of the config file that this object definition starts on.
	 * @return - int, the line of the config file that the object definition starts on
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * Set the line of the config file that this object definition starts on,
	 * @param startLine - The line that the object definition starts on.
	 */
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * Get the name of any template that should be applied to this dynamic template.
	 * @return - The name of any template that should be supplied.
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Set the name of any template that should be applied to this dynamic template
	 * @param template - the name of any template that should be applied.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * Get the host definition that this dynamic template definition uses.
	 * @return - String, the name of the host definition that this dynamic template uses.
	 */
	public String getUsesHost() {
		return usesHost;
	}

	/**
	 * Set the host definition that this dynamic template definition uses.
	 * @param usesHost - The name of the host that this dynamic template uses.
	 */
	public void setUsesHost(String usesHost) {
		this.usesHost = usesHost;
	}
}