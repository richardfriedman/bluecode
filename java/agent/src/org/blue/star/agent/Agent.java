package org.blue.star.agent;

import java.util.Map;

import org.blue.star.common.agent.CommandCache;
import org.blue.star.common.agent.exceptions.AgentRegistrationException;

/**
 * This interface defines the Agent class that all Blue Agents should implement.
 * 
 * The agent should be viewed as a very small footpring daemon that resides on a remote host. It provides
 * Blue with the capability to conduct checks on the remote host, but also allows the remote host to dynamicallyu
 * join the Blue Monitoring Network by conversing with the Blue Dynamic Registry 
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 * 
 */
public interface Agent 
{
    /**
     * This method is used to perform any tasks to initialise the Agent.
     * This method can contain a list of arguments for it's initialisation. These may
     * be from the command line or any other source.
     */
    public void init(String[] args);
    
    /**
     * This method is used to allow the agent to register with the Blue Registry
     */
    public void register() throws AgentRegistrationException;
    
    /**
     * This method is used to launch the checking service that allows Blue to conduct
     * remote checks on this host.
     */
    public void launchCheckingService();
    
    /**
     * This method is used to launch the service that allows for communication between
     * one or more agent running on the same host.
     */
    public void launchInterAgentCommunicationService();
    
    /**
     * This method is used to indicate whether or not this Agent is currently the Master
     * agent of the Host it is running on.
     * @return - boolean, true if this Agent is the Master agent.
     */
    public boolean isMaster();
    
    /**
     * This method is used to allow the agent to unregister from the Blue Registry
     */
    public void unregister() throws AgentRegistrationException;
    
    /**
     * This method is used to clear-up any resources used by the agent as it exits.
     */
    public void destroy();
    
    /**
     * Used to tell the Agent that it should shutdown and clear away all launched services
     */
    public void shutdown();
    
    /**
     * Used to indicate whether or not this Agent is running in daemon only mode.
     */
    public boolean isDaemon();
    
    /**
     * Used to indicate whether or not this Agent runs in daemon only mode i.e.
     * will only launch the checking service.
     * @param isDaemon - True/False if this agent is to only run in daemon mode.
     */
    public void setIsDaemon(boolean isDaemon);
    
    /**
     * This method is used to set the current type of this agent.
     * @param agentType - The type of the agent.
     */
    public void setAgentType(String agentType);
    
    /**
     * This method is used to get the current type of this agent.
     * @return - The current type of this agent.
     */
    public String getAgentType();
    
    /**
     * This method returns the name of the host on which this agent is running.
     * @return - The name of the host on which this agent is running.
     */
    public String getHostName();
    
    /**
     * This method returns the IP Address of the Host on which this agent is running.
     * @return - The IP Address of the host the agent is running on.
     */
    public String getIPAddress();
    
    /**
     * This method returns the current timeout value of the agent in milliseconds.
     * @return - The current timeout value of the agent, -1 if not set.
     */
    public long getTimeOut();
    
    /**
     * This method sets the current timeout value of the agent. 
     * @param timeout - The timeout value of the agent in milliseconds.
     */
    public void setTimeOut(long timeout);
    
    /**
     * This method returns the port on which this agent is running.
     * @return - The port on which this agent is running.
     */
    public int getPort();
    
    /**
     * This method is used to set the port on which this agent will run the checking service.
     * @param port - The port on which the agent will run the checking service.
     */
    public void setPort(int port);
    
    /**
     * This method returns the transport that this agent is using.
     * @return - The transport that this agent is utilising.
     */
    public String getTransport();
    
    /**
     * This method is used to set the Transport that will be used by this agent. This must
     * be set before the init method of the agent is called.
     * @param transport - The transport to be used by this agent.
     */
    public void setTransport(String transport);
    
    /**
     * This method is used to return the Hostname of the Registry that this agent is currently
     * connected to.
     * @return - The name of the registry that this agent is currently connected to, null if not connected.
     */
    public String getRegistryHost();
    
    /**
     * This method is used to set the name of the Registry that the Agent will connect to. This must
     * be set before the init() method of the agent is called.
     * @param registryHost - The hostname of the registry to connect to.
     */
    public void setRegistryHost(String registryHost);
    
    /**
     * This method returns the port on which the remote registry is running.
     * @return - The port of the remote registry, -1 if not connected.
     */
    public int getRegistryPort();
    
    /**
     * This method is used to set the port of the remote registry. This method should be
     * called before the init() method of the Agent.
     * @param port - The port of the remote registry.
     */
    public void setRegistryPort(int port);
    
    /**
     * This method returns access to the cache of commands currently associated with
     * this Agent.
     * @return - The cache of commands currently associated with this agent.
     */
    public CommandCache getCache();
    
    /**
     * This method is used to return a Map of the commands and their respective command lines
     * that this agent can execute.
     * @return - Map containing name of the commands and the command line that this agent can
     * execute.
     */
    public Map<String,String> getCommandCache();
    
    /**
	 * This method returns whether or not this agent allows multicast registration.
	 * @return - true if this agent is configured for Multicast registration.
	 */
	public boolean isMulticasting();
	
	/**
	 * This method sets whether or not this agent can use multicast registration to find
	 * a remote repository.
	 * @param isMulticasting - boolean indicating whether or not this agent can use multicast 
	 * registration.
	 */
	public void setAllowsMulticasting(boolean isMulticasting);
	
	/**
	 * This method determines whether or not this Agent allows command arguments.
	 * @return - true if this agent allows command arguments.
	 */
	public boolean allowsCommandArguments();
	
	/**
	 * This method sets whether or not this agent will support arguments to any command
	 * definitions.
	 * @param allowsCommandArguments - true if this agent is allowed to support command arguments.
	 */
	public void setAllowsCommandArguments(boolean allowsCommandArguments);
}