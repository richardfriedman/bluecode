package org.blue.star.agent.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.agent.Agent;
import org.blue.star.agent.config.utils.Utils;
import org.blue.star.common.agent.exceptions.AgentConfigurationException;

/**
 * This class is used to configure the Blue Agent via HTTP. It takes a http:// location
 * of a blue-agent.properties file and will configure the agent from the attribute/value
 * pairings within the file.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */

public class BlueAgentHttpConfig extends BaseConfig
{
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.config.BlueAgentHttpConfig");
	private String cn = "org.blue.star.registry.agent.config.BlueAgentHttpConfig";
	
	
	public BlueAgentHttpConfig()
	{}
	
	/**
	 * Constructor that takes an Agent to configure as a parameter.
	 * @param agent - The Agent to configure
	 */
	public BlueAgentHttpConfig(Agent agent)
	{
		this.agent = agent;
	}
	
	/**
	 * Constructor that takes an Agent and configuration source as parameters.
	 * @param agent - The Agent to Configure
	 * @param configurationSource - The configuration source.
	 */
	public BlueAgentHttpConfig(Agent agent, String configurationSource)
	{
		this.agent = agent;
		this.configLocation = configurationSource;
	}
	
	/* configureAgent() method */
	public void configureAgent() throws AgentConfigurationException 
	{
		logger.trace("Entering " + cn + ".configureAgent()");
		
		if(this.configLocation == null)
			throw new AgentConfigurationException("Cannot Utilise BlueAgentHttpConfig from Null Configuration Source");
		
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(this.configLocation).openStream()));
			String line = reader.readLine();
			int lineCount = 1;
			
			while(line != null)
			{
				if(!line.startsWith("#") && line.length() != 0)
				{
					String[] bits = line.split("=",2);
					
					if(bits.length !=2 || bits[1].length() == 0)
						throw new AgentConfigurationException("Null Variable at line '" + lineCount + "' in configuration source " + this.configLocation);
					/* Extra options can be added as required */
					if(bits[0].equals("transport"))
						this.agent.setTransport(bits[1]);
					else if(bits[0].equals("registry_host"))
						this.agent.setRegistryHost(bits[1]);
					else if(bits[0].equals("registry_port"))
						this.agent.setRegistryPort(Utils.setRegistryPort(bits[1]));
					else if(bits[0].equals("timeout"))
						this.agent.setTimeOut(Utils.setTimeOut(bits[1]));
					else if(bits[0].equals("agent_type"))
						this.agent.setAgentType(bits[1]);
					else if(bits[0].equals("daemon_only"))
						this.agent.setIsDaemon(Utils.convertToBooleanValue(bits[1]));
					else if(bits[0].startsWith("command"))
						this.agent.getCache().addCommand(Utils.getCommandName(bits[0]),bits[1]);
					else if(bits[0].equals("allow_command_arguments"))
						this.agent.setAllowsCommandArguments(Utils.setAllowCommandArguments(bits[1]));
					else
					{
							throw new AgentConfigurationException("Unknown Variable at line '" + lineCount + "' in configuration source '" + this.configLocation);
					}
				}
				line = reader.readLine();
				lineCount++;
			}
		}
		catch(MalformedURLException e)
		{
			logger.debug(cn + ".configureAgent() - Malformed URL for Config Location");
			throw new AgentConfigurationException("Bad URL for Configuration Source (" + this.configLocation + ")");
		}
		catch(IOException e)
		{
			logger.debug(cn + ".configureAgent() - Unable to Read From Config Location '" + this.configLocation + "'");
			throw new AgentConfigurationException("Unable To Open '" + this.configLocation + "' for Reading.");
		}
		
		this.updateMulticastDetails();
	}

	/* getConfigurationSource() */
	public String getConfigurationSource() {
		return this.configLocation;
	}

	/* getCurrentAgent() method */
	public Agent getCurrentAgent() {
		return this.agent;
	}

	/* parseAgentArguments() method */
	public void parseAgentArguments(String[] args) throws AgentConfigurationException {
		this.configureAgent();
	}

	/* setAgentToConfigure() method */
	public void setAgentToConfigure(Agent agent) {
			this.agent = agent;
	}

	/* setConfigurationSource() method */
	public void setConfigurationSource(String configurationSource) {
		this.configLocation = configurationSource;
	}
}