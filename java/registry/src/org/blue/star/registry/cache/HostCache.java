package org.blue.star.registry.cache;

import org.blue.star.registry.exceptions.RegistryCacheException;

/**
 * <p>This interface is represents the Host Cache that is associated with the 
 * Blue Dynamic Registry. It is used to store details of the hosts that have been
 * seen by the Registry. Hosts can be marked as online (active) or offline (dormant).</p>
 * 
 * <p>By utilising the Cache, it allows for more than one agent to reside on the same remote
 * host. This allows for the amalgamation of services dynamically onto a single Host.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public interface HostCache
{
	/**
	 * This method is used to store the details of a host that has been
	 * seen by the Blue Dynamic Registry.
	 * 
	 * @param IpAddress - The Ip address of the host that was seen.
	 * @param hostname - The hostname of the host that was seen.
	 * @param agentType - The agentType that is associated with this registration.
	 * @param isPersistent - Is this host a persistent registration
	 */
	public void storeHostDetails(String IpAddress,String hostname,String agentType,boolean isPersistent) throws RegistryCacheException;
	
	/**
	 * This method is used to return the number of agents running on a current host.
	 * @param IpAddress - The IpAddress of the host
	 * @param hostname - The hostname of the host.
	 * @return - int, the number of agents on this host.
	 */
	public int getHostAgentCount(String IpAddress,String hostname) throws RegistryCacheException;
	
	/**
	 * This method is used to discover if a host has already been seen by the registry
	 * and that it is still active on the network.
	 * @param IpAddress - The IP address of the host that we are checking.
	 * @param hostname - The hostname of the host that we are checking.
	 * @param agentType - The Agent Type on the host that we are looking for.
	 * @return - boolean, true if the host has been seen before and it is active.
	 */
	public boolean hasSeenHost(String IpAddress,String hostname);
	
	/**
	 * This method is used to determine if a host is already believed to be running a specific
	 * agent type.
	 * @param IpAddress - The IpAddress of the remote host.
	 * @param hostname - The hostname of the remote host.
	 * @param agentType - The agent type of the remote host
	 * @return - boolean, true if the remote host is already running the agent.
	 */
	public boolean isHostRunningAgent(String IpAddress,String hostname,String agentType);
	
	/**
	 * This method is used to determine if a host that has registered should be made into
	 * a persistent part of the Blue monitoring configuration.
	 * 
	 * @param IpAddress - The IpAddress of the remote host.
	 * @param hostname - The hostname of the remote host.
	 * @return - boolean, true if this host should be persistent.
	 */
	public boolean isHostPersistent(String IpAddress,String hostname);
	
	/**
	 * This method is used to remove a particular host from the seen host list.
	 * @param IpAddress - The IpAddress of the host to remove.
	 * @param hostname - The hostname of the host to remove.
	 */
	public void removeHost(String IpAddress,String hostname) throws RegistryCacheException;

	/**
	 * This method is used to remove a specific agent type from a host.
	 * @param IpAddress - The IPAddress of the host to remove the agent from
	 * @param hostname - The name of the host to remove the agent from.
	 * @param agentType - The type of agent to remove.
	 */
	public void removeAgentFromHost(String IpAddress,String hostname,String agentType) throws RegistryCacheException;
	
	/**
	 * This method is used to persist the seen host list.
	 */
	public void persistSeenHostList();
	
	/**
	 * This method is used to load a seen host list.
	 *
	 */
	public void loadSeenHostList();
	
	/**
	 * This method is used to load a seen host list from a given location.
	 * @param location - The location from which to load the seen host list.
	 */
	public void loadSeenHostList(String location) throws RegistryCacheException;
	
	/**
	 * This method is used to set the location to which the Registry should persist it's seen
	 * host list.
	 * 
	 * @param location - The location to output the seen hosts list to.
	 */
	public void setPersistLocation(String location);
	
	/**
	 * This method is used to get the current persistence location.
	 * @return - The current persistent location
	 */
	public String getPersistLocation();
	
	/**
	 * This method is used to add an agent type to a host.
	 * @param ipAddress - the IpAddress of the host running the agent.
	 * @param hostname - the hostname of the host running the agent.
	 * @param agentType - the type of agent running on the remote host.
	 * 
	 * @throws RegistryCacheException - thrown if the host you are trying to add the agent to does not
	 * exist.
	 */
	public void addAgentToHost(String ipAddress,String hostname,String agentType) throws RegistryCacheException;
}