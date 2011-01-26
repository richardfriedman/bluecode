package org.blue.star.agent.config;

import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.blue.star.agent.Agent;
import org.blue.star.common.agent.exceptions.AgentConfigurationException;

/**
 * This abstract class holds some configuration data that is likely to be common amongst
 * all Agents. Currently this only supports the command line options that are valid for this
 * agent.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public abstract class BaseConfig implements AgentConfig 
{
	/** The options that can be used with this Agent. */
	private Options agentOptions = new Options();
	
	/** Location of the configuration for the Agent */
	protected String configLocation;
	/** Configuration Class for the Agent */
	protected String configClass;
	
	/** Agent Instance */
	protected Agent agent;
	
	/**
	 * This method returns the currently supported command line arguments for this Agent.
	 */
	public Options getAgentOptions()
	{
		return this.agentOptions;
	}
	
	/**
	 * This method sets the currently supported command line arguments for the Agent.
	 */
	protected void setAgentOptions()
	{
		Option d = new Option("d","Daemon Checking Mode",false,"Causes the Blue Agent to enter remote checking mode only");
		d.setArgName("Daemon Checking Mode");
		this.agentOptions.addOption(d);
		
		Option h = new Option("h","Help",false,"Display This Help Information");
		h.setArgName("Help");
		this.agentOptions.addOption(h);
	}
	
	/**
	 * This method prints out the help for the above available options.
	 */
	protected void printHelp() throws AgentConfigurationException
	{
		System.out.println("Usage: java -jar blue-agent.jar <options> <cfg_file>");
		System.out.println("");
		System.out.println("-d\t Causes the Blue Agent to enter Remote checking mode only.");
		System.out.println("Remote checking mode allows your central Blue Server to run checks");
		System.out.println("on this Host.");
		System.out.println("");
		System.out.println("-h\t Display this help information.");
		System.out.println("");
		System.out.println("This is the Blue Agent. The Blue Agent can be used to dynamically register");
		System.out.println("this system with your Blue Server. It can also be used to allow the Blue");
		System.out.println("Server to run checks on this Host to support a distributed monitoring solution.");
		System.out.println("");
		System.out.println("For more information on configuring this agent, please see documentation at");
		System.out.println("http://blue.sourceforge.net\n");
		
		throw new AgentConfigurationException();
	}
	
	/**
	 * This method is used to verify if a particular file exists.
	 * @param fileName - The name of the file to verify
	 * @return - true if the file exists.
	 */
	protected boolean checkIsFile(String fileName)
	{
		return new File(fileName).exists();	
	}
	
	/**
	 * Method that updates the multicast details of this agent. If we have no registry host
	 * and we are not running in daemon only mode, then we enter multicast registration.
	 */
	protected void updateMulticastDetails()
	{
		if((agent.getTransport() != null && agent.getRegistryHost() != null) || agent.isDaemon())
		{
			/* Default port details if none specified by user */
			if(agent.getRegistryPort() == 0)
			{
				agent.setRegistryPort(9999);
			}
			
			agent.setAllowsMulticasting(false);
		}
		else
		{
			agent.setAllowsMulticasting(true);
		}
	}
}