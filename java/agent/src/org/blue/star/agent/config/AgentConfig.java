package org.blue.star.agent.config;

import org.apache.commons.cli.Options;
import org.blue.star.agent.Agent;
import org.blue.star.common.agent.exceptions.AgentConfigurationException;

/**
 * <p>This interface defines the class that completes configuration for the Agent.
 * As the interface shows, it is possible to set a source from which the agent
 * should pull it's configuration, and it is also possible to set the Agent that 
 * is being configured.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public interface AgentConfig 
{
	/**
	 * This method is used to set the source from which the Agent should read
	 * its configuration
	 * @param configurationSource - The source of the configuration.
	 */
	public void setConfigurationSource(String configurationSource);
	
	/**
	 * This method is used to retrieve the current source from which this Agent
	 * is reading it's configuration.
	 * @return - The current source of configuration for this agent.
	 */
	public String getConfigurationSource();
	
	/**
	 * This method is used to set Agent that should be configured.
	 * @param agent - The agent to be configured.
	 */
	public void setAgentToConfigure(Agent agent);
	
	/**
	 * This method is used to get the current Agent that is being configured.
	 * @return - The agent that is being configured.
	 */
	public Agent getCurrentAgent();
	
	/**
	 * This method is used to configure the agent with the properties from the configuration
	 * source.
	 * @throws AgentConfigurationException - Thrown if there is an issue configuring the agent.
	 */
	public void configureAgent() throws AgentConfigurationException;
	
	/**
	 * This method is used to parse any options that have been passed to the Agent.
	 * @param args - The arguments that have been passed to agent.
	 */
	public void parseAgentArguments(String[] args) throws AgentConfigurationException;
	
	/**
	 * This returns the currently available command line options that can be used with this agent.
	 * @return - The options to be used with this Agent.
	 */
	public Options getAgentOptions();
}