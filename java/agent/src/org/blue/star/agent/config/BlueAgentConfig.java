package org.blue.star.agent.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.agent.Agent;
import org.blue.star.agent.config.utils.Utils;
import org.blue.star.common.agent.exceptions.AgentConfigurationException;

/**
 * This class is an implementation of the AgentConfig interface. It is used to provide configuration
 * for the current BlueAgent class as of version 0.1
 * 
 * It should be noted that currently this implementation will read configuration details from a file
 * only.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 * 
 * @see - <b>org.blue.star.registry.agent.config.AgentConfig</b> - The interface this class implements.
 *
 */
public class BlueAgentConfig extends BaseConfig implements AgentConfig {

	/** The source to read the configuration from */
	private String configurationSource;
	/** Logging variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.config.BlueAgentConfig");
	private String cn = "org.blue.star.registry.agent.config.BlueAgentConfig";
	
	public BlueAgentConfig()
	{
		this.setAgentOptions();
	}
	
	/** Constructor that takes an agent as a parameter */
	public BlueAgentConfig(Agent agent)
	{
		this.agent = agent;
		this.setAgentOptions();
	}
	
	/* parseAgentArguments() method */
	public void parseAgentArguments(String[] args) throws AgentConfigurationException
	{
		logger.trace("Entering " + cn + ".parseAgentArguments");
		
		CommandLine cmd = null;
		
		try
		{
			cmd = new PosixParser().parse(this.getAgentOptions(),args);
		}
		catch(ParseException e)
		{
			logger.debug(cn + ".parseAgentArguments() - Unable to parse Agent Options");
			throw new AgentConfigurationException(e);
		}
		  
		Iterator i = cmd.iterator();
		  
		while(i.hasNext())
		{
		
			Option o =(Option)i.next();
		  
			switch(o.getId())
			{
				case 'h':
					this.printHelp();
					break;
							  
				case 'd':
					this.agent.setIsDaemon(true);
					break;
					
				default:
					logger.info("Unsupported Command Line Option.\n");
					this.printHelp();
					break;
			}
		 }
		  
		 /* Deal with anything else that's left on the command line */
		 if(!this.agent.isDaemon())
			this.parseCommandLineArguments(cmd.getArgs());
	}
	
	/**
	 * This method is used to parse any trailing command line arguments that have not
	 * been parsed by the parseAgentArguments method 
	 * @param args - The arguments to parse.
	 */
	private void parseCommandLineArguments(String[] args) throws AgentConfigurationException
	{
		logger.trace("Entering " + cn + ".parseCommandLineArguments");
		
		if(args.length == 1)
		{
			if(this.checkIsFile(args[0]))
			{
				this.configurationSource = args[0];
				this.configureAgent();
				this.updateMulticastDetails();
			}
			else
			{
				agent.setAgentType(args[0]);
				this.agent.setAllowsMulticasting(true);
				
			}
		}
		else if(this.checkIsFile(System.getProperty("user.dir") + "/blue-agent.props"))
		{
			this.configurationSource = System.getProperty("user.dir") + "/blue-agent.props";
			this.configureAgent();
			this.updateMulticastDetails();
		}
		else
		{
			this.agent.setAllowsMulticasting(true);
		}
		
		logger.trace("Exiting " + cn + ".parseCommandArguments");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.blue.star.registry.agent.config.AgentConfig#configureAgent()
	 * 
	 * NOTE: This version of the AgentConfig class currently reads from files
	 * only to configure the agent.
	 */
	public void configureAgent() throws AgentConfigurationException 
	{
		logger.trace("Entering " + cn + ".configureAgent()");
		
		if(this.agent == null || this.configurationSource == null)
			throw new AgentConfigurationException("Agent to Configure was null or Configuration Source was null");
		
		int lineCount = 1;
		BufferedReader reader;
		
		try
		{
			reader = new BufferedReader(new FileReader(this.configurationSource));
			String[] bits;
			String line = reader.readLine();
			
			while(line != null)
			{
				if(line.length() != 0 && !line.startsWith("#"))
				{
					bits = line.split("=",2);
				
					if(bits.length != 2 || bits[1].length() == 0)
					{
						throw new AgentConfigurationException("Error: Null Value in configuration source '" + this.configurationSource + "' at line " + lineCount);
					}
				
					/* Trim Values */
					bits[0] = bits[0].trim();
					bits[1] = bits[1].trim();
					
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
							throw new AgentConfigurationException("Unknown Variable at line '" + lineCount + "' in configuration source '" + this.configurationSource);
					}
				}
				
				line = reader.readLine();
				lineCount++;
			}
			
			reader.close();
		}
		catch(IOException e)
		{
			logger.debug(cn + ".configureAgent() - Unable to open Configuration Source for Reading");
			throw new AgentConfigurationException("Unable To Open Configuration Source '" + this.configurationSource + "' for Reading");
		}
		
	}

	/* getConfigurationSource() method */
	public String getConfigurationSource() {
		return this.configurationSource;
	}

	/* getCurrentAgent() method */
	public Agent getCurrentAgent() {
		return this.agent;
	}

	/* setAgentToConfigure() method */
	public void setAgentToConfigure(Agent agent) {
		this.agent = agent;
	}

	/* setConfigurationSource() method */
	public void setConfigurationSource(String configurationSource)
	{
		this.configurationSource = configurationSource;
	}
}