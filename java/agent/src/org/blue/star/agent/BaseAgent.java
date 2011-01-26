package org.blue.star.agent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.agent.config.AgentConfig;
import org.blue.star.agent.config.BlueAgentConfig;
import org.blue.star.agent.config.utils.Utils;
import org.blue.star.common.agent.CommandCache;
import org.blue.star.common.agent.exceptions.AgentConfigurationException;
import org.blue.star.common.agent.exceptions.AgentRegistrationException;


/**
 * This class is an abstract class that partially implements the Agent interface. It's role
 * is to form as a base from which other agents can be derived. It also gives an indication
 * as to how the Agent concept works.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public abstract class BaseAgent implements Agent
{
	/** Our MulticastAgent that will multicast for a registry */
	protected MulticastAgent multicastAgent;
	/** The hostname of the registry that this agent is communicating with */
	private String registryHost;
	/** The port of the registry that this agent is communicating with */
	private int registryPort;
	/** The command cache to store details of the commands this agent can execute. */
	private CommandCache cache = new CommandCache();
	/** Our AgentConfig Instance */
	protected AgentConfig config;
	/** String to store details of the Config Class this agent is to use */
	protected String configClass;
	/** String to store details of the Config Location for this config */
	protected String configLocation;
	
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.BaseAgent");
	private String cn = "org.blue.star.registry.agent.BaseAgent";
	
	/** Details of the Host on which we are running */
	private String hostname;
	/** The port on which the checking service has been launched */
	private int port = 5667;
	/** The IP Address of this Agent */
	private String ipAddress;
	/** The transport that this agent is using */
	private String transport = "socket";
	/** Is this Agent the master of agents on this host? */
	private boolean isMaster;
	/** Used to indicate if this Agent supports multicast registration */
	private boolean multicasting;
	/** Used to indicate if this agents supports Command Arguments */	
	private boolean allowCommandArguments;
	/** Used to indicate the type of this agent */
	private String agentType;
	/** Used to set the timeout of this agent */
	private long timeout = 60000;
	/** Used to indicate if this agent should shutdown. */
	private boolean shutdown;
	/** Used to indicate if this agent is running in daemon only mode */
	private boolean justDaemon;
	/** Our checking Service Thread */
	private Thread checkingServiceThread;
	
			
	/* isDaemon() method */
	public boolean isDaemon()
	{
	    return this.justDaemon;
	}
	
	/* setIsDaemon() method */
	public void setIsDaemon(boolean isDaemon)
	{
		this.justDaemon = isDaemon;
	}
		
	/* getHostName() method */
	public String getHostName()
	{
	    return this.hostname;
	}
	 
	/* getIPAddress() method */
	public String getIPAddress()
	{
	    return this.ipAddress;
	}
	
	/* getPort() method */
	public int getPort()
	{
	    return this.port;
	}
	
	/* setPort() method */
	public void setPort(int port)
	{
		this.port = port;
	}
	
	/* getTransport() method */
	public String getTransport()
	{
	    return this.transport;
	}
	
	/* setTransport() method */
	public void setTransport(String transport)
	{
		this.transport = transport;
	}
	
	/* setAgentType() method */
	public void setAgentType(String agentType)
	{
	    this.agentType = agentType;
	}
	
	/* getAgentType() method */
	public String getAgentType()
	{
	    return this.agentType;
	}
	
	/* isMaster() method */
	public boolean isMaster()
	{
		return this.isMaster;
	}
	
	/* getCache() method */
	public CommandCache getCache()
	{
		return this.cache;
	}
	
	/* getCommandCache() method */
	public Map<String,String> getCommandCache()
	{
		return this.cache.getAllCommands();
	}
	
	/* getRegistryHost() method */
	public String getRegistryHost()
	{
		return this.registryHost;
	}
	
	/* setRegistryHost() method */
	public void setRegistryHost(String registryHost)
	{
		this.registryHost = registryHost;
	}
	
	/* getRegistryPort() method */
	public int getRegistryPort()
	{
		return this.registryPort;
	}
	
	/* setRegistryPort() method */
	public void setRegistryPort(int registryPort)
	{
		this.registryPort = registryPort;
	}
	
	/* getTimeout() method */
	public long getTimeOut()
	{
		return this.timeout;
	}
	
	/* setTimeOut() method */
	public void setTimeOut(long timeout)
	{
		this.timeout = timeout;
	}
	
	/* isMulticasting() method */
	public boolean isMulticasting()
	{
		return this.multicasting;
	}
	
	/* setAllowsMulticasting() method */
	public void setAllowsMulticasting(boolean multicast)
	{
		this.multicasting = multicast;
	}
	
	/* setAllowsCommandArguments() method */
	public void setAllowsCommandArguments(boolean allowsArguments)
	{
		this.allowCommandArguments = allowsArguments;
	}
	
	/* allowsCommandArguments() method */
	public boolean allowsCommandArguments()
	{
		return this.allowCommandArguments;
	}
	
	/*==== END OF GET/SET METHODS ==== */
	
	/*==== LIFECYCLE METHODS ====*/
	
	/* init() method */
	public void init(String[] args)
	{
	   logger.trace("Entering " + cn + ".init");
	   logger.info("Launching Blue Agent - Version 0.1");
	   
	   this.addAgentShutdownHandler();
	   this.setHostDetails();
	   
	   try
	   {
		   this.preConfigureConfig(args);
		   this.config.parseAgentArguments(args);
		   
		   /* Launch The Checking Service */
		   this.launchCheckingService();

		   if(!this.isDaemon())
		   {
			   logger.info("Attempting Registration with remote Registry.");
			   /* If we're not running in just daemon mode, we need to register */
			   this.register();
		   }
		   
		   /* Simply Keep the Checking Service Alive */
		   this.keepCheckingServiceAlive();
	   }
	   catch(AgentConfigurationException e)
	   {
		   logger.debug(cn  + ".init() - Exiting Configuring Agent");
		   
		   if(e.getMessage() != null)
			   logger.info(e.getMessage());
	   }
	   catch(AgentRegistrationException e)
	   {
		   logger.debug(cn + ".init() - Unable to Register Agent with Registry");
		   
		   if(e.getMessage() != null)
			   logger.info(e.getMessage());
	   }
	   
	   /* If we are not in daemon only mode, unregister from the Registry */
	   if(!this.justDaemon)
	   {
		   try
		   {
			   this.unregister();
		   }
		   catch(AgentRegistrationException e)
		   {
			   logger.debug(cn + ".init() - Unable to deregister Agent from Registry");
			   logger.info(e.getMessage());
		   }
	   }
	   
	   this.shutdownCheckingService();
	}
	
	/* destroy() method */
	public void destroy(){}
	
	/* launchInterAgentCommunicationService() method */
	public void launchInterAgentCommunicationService()
	{
		//TODO
	}
		
	/* shutdown() method */
	public void shutdown()
	{
	    this.shutdown = true;
	    
	    if(!this.justDaemon)
	    {
	    	try
	    	{
	    		this.unregister();
	    	}
	    	catch(AgentRegistrationException e)
	    	{
	    		logger.debug(cn + ".shutdown() - Error Unregistering from Remote Registry: " + e.getMessage());
	    	}
	    }
	}
	
	/**
	 * This method is used to set the details of the Host that this agent is running on.
	 * It will set the Hostname and IPAddress of this Host using inet.getHostAddress() and
	 * inet.getHostName()
	 */
	private void setHostDetails()
	{
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			ipAddress = inet.getHostAddress();
			hostname = inet.getHostName();
		}
		catch(UnknownHostException e)
		{
			logger.debug(cn + ".setHostDetails() - Error retrieving Host Details, setting defaults");
			hostname = "localhost";
			ipAddress = "127.0.0.1";
		}
	}
		
	/* launchCheckingService() method */
	public void launchCheckingService()
	{
		logger.trace("Entering " + cn + ".launchCheckingService");
		
		this.checkingServiceThread = new Thread(new BlueAgentRequestHandler(this));
		checkingServiceThread.setName("Blue Remote Checking Service");
		checkingServiceThread.start();
	}
	
	/**
	 * This method is used to shutdown the checking service.
	 */
	private void shutdownCheckingService()
	{
		if(this.checkingServiceThread == null)
		    return;
	    
	    try
		{
			this.checkingServiceThread.join();
		}
		catch(InterruptedException e)
		{
			logger.debug(cn + ".shutdownCheckingService() - Currently Unable to Stop Checking service");
		}
	}
	
	/**
	 * This method is used to add a shutdown handler for the Agent to the current runtime.
	 */
	private void addAgentShutdownHandler()
	{
		Runtime.getRuntime().addShutdownHook(new BlueAgentShutdownHandler(this));
	}
	
	/**
	 * This method is used to keep the Checking Service Alive. It performs a rudimentary
	 * while loop until the Agent receives a shutdown signal.
	 */
	private void keepCheckingServiceAlive()
	{
		logger.trace("Entering " + cn + ".keepCheckingServiceAlive");
		
		while(!this.shutdown)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{}
		}
	}
	
	/**
	 * This method attempts to identify the file that should be used to read any
	 * initial configuration from.
	 * 
	 * @param args - Any command line arguments passed to the agent.
	 */
	private void preConfigureConfig(String[] args) throws AgentConfigurationException
	{
		if(args.length > 0)
		{
			if(Utils.checkIsFile(args[0]))
			{
				/* Read from args[0] as our configuration file */
				this.readInitialConfig(args[0]);
				return;
			}
		}
		
		if(Utils.checkIsFile(System.getProperty("user.dir") + "/blue-agent.props"));
		{
			/* read from blue-agent.props as our configuration file */
			this.readInitialConfig(System.getProperty("user.dir") + "/blue-agent.props");
		}
		
		this.instantiateConfig();
	}
	
	/**
	 * This method reads the initial configuration file for the agent. It allows for the specification
	 * of a particular class of AgentConfig facilitating distributed configuration of the Blue Agent.
	 * @param fileName - The filename to read the initial configuration from
	 * @throws AgentConfigurationException - thrown if there are any issues reading the initial configuration.
	 */
	private void readInitialConfig(String fileName) throws AgentConfigurationException
	{
		logger.trace("Entering " + cn + ".readInitialConfig");
		
		if(fileName == null || fileName.length() == 0)
				return;
			
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(fileName));
				String line = reader.readLine();
				int linecount = 1;
				
				while(line !=null)
				{
					if(line.length() != 0 && !line.startsWith("#"))
					{
						String[] bits = line.split("=",2);
						
						if(bits.length != 2 || bits[1].length() == 0)
							throw new AgentConfigurationException("Null Variable at line " + linecount + " in file '" + fileName + "'.");
						
						if(bits[0].equals("config_location")) this.configLocation = bits[1].trim();
						else if(bits[0].equals("config_class")) this.configClass = bits[1].trim();
					}
					
					line = reader.readLine();
					linecount++;
				}
				
				reader.close();
				
			}
			catch(FileNotFoundException e)
			{
				logger.debug(cn + ".runPreConfig() - File Not Found");
				throw new AgentConfigurationException("File '" + fileName + "' cannot be found.");
			}
			catch(IOException e)
			{
				logger.debug(cn + ".runPreConfig() - IOException thrown");
				throw new AgentConfigurationException("Cannot open file '" + fileName + "' for Reading");
			}
			
		logger.trace("Exiting " + cn + ".readInitialConfig");
	}
	
	/**
	 * This method is used to instantiate the AgentConfig and set any specific Config
	 * location
	 * @throws AgentConfigurationException - thrown if there is any issue configuring the AgentConfig.
	 */
	private void instantiateConfig() throws AgentConfigurationException
	{
		this.loadConfigClass();
		
		if(this.configLocation != null)
			this.config.setConfigurationSource(this.configLocation);
		
		this.config.setAgentToConfigure(this);
	}
	
	/**
	 * This method is used to instantiate an instance of any specified AgentConfig class.
	 * @throws AgentConfigurationException - Thrown if there is an issue instantiating the class.
	 */
	private void loadConfigClass() throws AgentConfigurationException
	{
		try
		{
			if(this.configClass != null)
			{
				this.config = (AgentConfig)Class.forName(this.configClass).newInstance();
				return;
			}
		}
		catch(ClassNotFoundException e)
		{
			logger.debug(cn + ".loadConfigClass() - Cannot find Config Class '" + this.configClass + "'");
			throw new AgentConfigurationException(e);
		}
		catch(IllegalAccessException e)
		{
			logger.debug(cn + ".loadConfigClass() - IllegalAccessException trying to instantiate class '" + this.configClass + "'");
			throw new AgentConfigurationException(e);
		}
		catch(InstantiationException e)
		{
			logger.debug(cn + ".loadConfigClass() - InstantiationException trying to instantiate class '" + this.configClass + "'");
			throw new AgentConfigurationException(e);
		}
	
		this.config = new BlueAgentConfig();
	}
}