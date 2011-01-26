package org.blue.star.registry;

import java.io.File;
import java.net.InetAddress;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.registry.cache.BlueRegistryHostCache;
import org.blue.star.registry.cache.HostCache;
import org.blue.star.registry.config.BlueRegistryConfig;
import org.blue.star.registry.config.RegistryConfig;
import org.blue.star.registry.exceptions.ConfigurationGenerationException;
import org.blue.star.registry.exceptions.RegistryConfigurationException;
import org.blue.star.registry.objects.RegistryObjects;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.detection.multicast.MulticastDetector;
import org.jboss.remoting.transport.Connector;

/**
 * 
 * 	@created 31 Jul 2007 - 09:47:39
 *	@filename org.blue.star.registry.BlueDynamicRegistry.java
 *  @author Rob.Blake@arjuna.com
 *	@version 0.1
 *
 *	<p>The BlueDynamicRegistry class. This class is used to launch the Blue Dynamic Registry. It contains
 *	all logic for loading the Registry Configuraton engines, and will hand of configuration reading to
 *	required components.</p>
 */
public class BlueDynamicRegistry extends Thread
{
	/** Version information */
	private double version = 0.1;
	
	/** The transport this registry will use */
    private String transport = "socket";
    /** The name of the host that this registry runs on */
	private String hostname;
	/** The location of the External Command File to Blue */
	private String commandFile = "var/rw/blue.cmd";
	/** The direcrory relative to the registry that should be used for persistence */
	private String persistDirectory = "etc/dynamic";
	/** Flag to indicate if the registry is shutdown */
	private boolean shutdown; 
	/** Directory to use for temporary configuration files */
	final private String tempDir = "temp/registry";
	/** The port this registry will run on */
	private int port;
	/** Flag to determine if this registry accepts multicast regisrations */
	private boolean allowMulticasts;
	/** Flag to determine if the registry is enabled */
	private boolean registryEnabled;
	/** Flag to determine if the registry allows unknown registration */
	private boolean allowUnknownRegistration;
	/** The location of the configuration file for the registry */
	private String configurationFile = "etc/registry.cfg";
	/** Any Command Line args passed to Registry */
	private String[] commandLineArgs;
	
	/** Properties to configure our Dynamic Registry Connector */
	private String registryProps = "/?clientLeasePeriod=10000&timeout=120000";
	private MBeanServer server;
	private MulticastDetector detector;
	private InvokerLocator locator;
	private Connector connector;
		
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.BlueDynamicRegistry");
	private String cn = "org.blue.star.registry.BlueDynamicRegistry";

	/** Host Cache */
	private HostCache cache;
	/** Registry Object Walker */
	private RegistryObjectWalker walker;
	/** Config Object */
	private RegistryConfig config;
		
	/** Blank Constructor */
	public BlueDynamicRegistry()
	{
	    this.config = new BlueRegistryConfig();
	}
	
	/*=== GETTERS/SETTERS ===*/
	
	/**
	 * This method is used to set the transport used by this registry.
	 * @param - String, the transport this registry will use.
	 */
	public void setTransport(String transport)
	{
	    this.transport = transport;
	}
	
	/**
	 * This method gets the transport currently used by this registry
	 * @return - String, the transport currently used by this registry.
	 */
	public String getTransport()
	{
	    return this.transport;
	}
	
	/**
	 * This method sets the hostname that this registry is running on.
	 * @param hostname - The name of the host that the registry is running on.
	 */
	public void setHostName(String hostname)
	{
	    this.hostname = hostname;
	}
	
	/**
	 * This method gets the name of the host the registry is running on.
	 * @return - The name of the host the registry is running on.
	 */
	public String getHostName()
	{
	    return this.hostname;
	}
	
	/**
	 * This method sets the location of the Blue External Command File.
	 * @param commandFile - The location of the Blue External Command File.
	 */
	public void setCommandFile(String commandFile)
	{
	    this.commandFile = commandFile;
	}
	
	/**
	 * This method gets the current location of the Blue External Command File.
	 * @return - The location of the current Blue External Command File.
	 */
	public String getCommandFile()
	{
	    return this.commandFile;
	}
	
	/**
	 * This method sets the directory into which configs should be persisted.
	 * @param persistDirectory - The directory into which configs should be persisted.
	 */
	public void setPersistDirectory(String persistDirectory)
	{
	    if(persistDirectory.endsWith("\\") || persistDirectory.endsWith("/"))
	    {
		persistDirectory = persistDirectory.substring(0,persistDirectory.length()-1);
	    }
	    
	    this.persistDirectory = persistDirectory;
	}
	
	/**
	 * This method gets the current directory into which configs are persisted.
	 * @return - The current directory into which configs are persisted.
	 */
	public String getPersistDirectory()
	{
	    return this.persistDirectory;
	}
	
	/**
	 * This method returns the current temporary directory of the Registry.
	 * @return - String, the current temporary directory of the registry.
	 */
	public String getTempDirectory()
	{
	    return this.tempDir;
	}
	
	/**
	 * This method sets the registry shutdown flag to true.
	 */
	public void shutdown()
	{
	    this.shutdown = true;
	}
	/**
	 * This method returns the value of the registry shutdown flag.
	 * @return - boolean, true if the registry is shutdown.
	 */
	public boolean isShutdown()
	{
	    return this.shutdown;
	}
	
	/**
	 * This method sets the port of the current registry.
	 * @param port - The port of the current registry.
	 */
	public void setPort(int port)
	{
	    this.port = port;
	}
	
	/**
	 * This method returns the current port of the registry.
	 * @return - int, the current port of the registry.
	 */
	public int getPort()
	{
	    return this.port;
	}
	
	/**
	 * This method sets if the registry is allowed to accept multicast registrations.
	 * @param allowMulticasts - boolean, true if the registry is to allow multicast registrations.
	 */
	public void setAllowMulticasts(boolean allowMulticasts)
	{
	    this.allowMulticasts = allowMulticasts;
	}
	
	/**
	 * This method is used to indicate if the registry currently allows multicast registrations.
	 * @return - boolean, true if the registry currently allows multicast registrations.
	 */
	public boolean getAllowMulticasts()
	{
	    return this.allowMulticasts;
	}
	
	/**
	 * This method is used to indicate if the registry should be enabled.
	 * @param enabled - boolean, true if the registry should be enabled.
	 */
	public void setRegistryEnabled(boolean enabled)
	{
	    this.registryEnabled = enabled;
	}
	
	/**
	 * This method returns if the registry is currently enabled.
	 * @return - boolean, true if the registry is currently enabled.
	 */
	public boolean isEnabled()
	{
	    return this.registryEnabled;
	}
	
	/**
	 * This method sets if the registry allows unknown registrations.
	 * @param allowUnknown - boolean, true if the registry should allow unknown registrations.
	 */
	public void setAllowUnknownRegistration(boolean allowUnknown)
	{
	    this.allowUnknownRegistration = allowUnknown;
	}
	
	/**
	 * This method gets if the registry currently allows unknown registrations.
	 * @return - boolean, true if the registry currently allows unknown registrations.
	 */
	public boolean allowsUnknownRegistration()
	{
	    return this.allowUnknownRegistration;
	}
	
	/**
	 * This method is used to set the configuration file that should be used to configure
	 * the registry.
	 * @param configurationFile - The path to the file to use for configuration.
	 */
	public void setConfigurationFile(String configurationFile)
	{
	    this.configurationFile = configurationFile;
	    config.setConfigurationSource(configurationFile);
	}
	
	/**
	 * This method returns the configuration file currently used by the registry.
	 * @return - String, the current configuration file.
	 */
	public String getConfigurationFile()
	{
	    return this.configurationFile;
	}
	
	/**
	 * This method returns the currently available host cache.
	 * @return - The host cache of the registry.
	 */
	protected HostCache getHostCache()
	{
	    return this.cache;
	}
	
	/**
	 * This method is used to pass any command line args into the registry.
	 * @param commandLineArgs - The command line args to pass to the registry.
	 */
	public void setCommandLineArgs(String[] commandLineArgs)
	{
	    this.commandLineArgs = commandLineArgs;
	}
	
	/*=== END OF GETTERS/SETTERS ===*/
	
	
	/* run() method */
	public void run()
	{
	    this.startRegistry();
	}
	
	/**
	 * This method starts our registry by performing all the required actions 
	 */
	public void startRegistry()
	{
	    logger.trace("Entering " + cn + ".startRegistry");
	    logger.info("Starting Blue Dynamic Registry: Version " + version);
	    
	    /* Load our Cache & ObjectWalker */
	    loadCache();
	    this.walker = new RegistryObjectWalker(this);
			
	    try
	    {
	    	/* Read in our configuration */
	    	config.setRegistryToConfigure(this);
	    	this.config.parseRegistryArguments(this.commandLineArgs);
	    	
	    	/* Verify that we actually have some template definitions */
	    	this.verifyTemplateCount();
	    	
	    	/* Verify that the external command file exists */
	    	this.verifyExternalCommandFile();
	    	
			/* Prep the initial services */
			this.prepInitialServices();
				
			/* Pre-register our objects */
			this.preRegisterObjects();
				
			/* If we are allowing multicast requests to register, start our listener */
			if(this.allowMulticasts)
			{
			    this.startMulticastListener();
			}
				
			/* Add our register Invocation handler for the register subsystem */
			this.addRegisterInvocationHandler();
				
			/* Make our required directories */
			this.createRegistryDirs();
				
			/* Add our shutdown handler */
			this.addShutdownHandler();
			
			/* Start the Dynamic Registry Service */
			this.startService();
			
			this.shutdown = false;
		}
		catch(RegistryConfigurationException e)
		{
		  	logger.debug(cn + ".startRegistry() - Error starting Registry, reason: " + e.getMessage());
		   	logger.info(e.getMessage());
		   	this.shutdown = true;
		}
		catch(ConfigurationGenerationException e)
		{
		  	logger.debug(cn + ".startRegistry() - Error starting Registry, reason: " + e.getMessage());
		   	logger.info(e.getMessage());
		   	this.shutdown = true;
		}
	
		logger.trace("Exiting " + cn + ".startRegistry");
	}
	
	/**
	 * This method is used to add a shutdown handler for the Registry.
	 */
	private void addShutdownHandler()
	{
		Runtime.getRuntime().addShutdownHook(new BlueDynamicRegistryShutdownHandler(this));
	}
	
	/**
	 * This method is used to prep our initial services before they are fully launched.
	 * @throws Exception - Thrown if there was any issue prepping the services.
	 */
	private void prepInitialServices() throws RegistryConfigurationException
	{
	    try
	    {
		    hostname = InetAddress.getLocalHost().getHostName();
			server = MBeanServerFactory.createMBeanServer();
			locator = new InvokerLocator(transport + "://" + hostname + ":" + port + registryProps);
			connector = new Connector();
			connector.setInvokerLocator(locator.getLocatorURI());
			connector.create();
			
			logger.info("Blue Dynamic Registry: Inital Services Prepped...");
	    }
	    catch(Exception e)
	    {
	    	logger.debug(cn + ".prepInitialServices() - Exception thrown prepping inital Services");
	    	throw new RegistryConfigurationException("Cannot Prep Initial Services of Registry",e);
	    }
	}
	
	/**
	 * This method starts the multicast listener should this registry support multicast
	 * registrations.
	 * @throws Exception - Thrown if the multicast listener cannot be started properly.
	 */
	private void startMulticastListener() throws RegistryConfigurationException
	{
	   try
	   {
        	detector = new MulticastDetector();
        	server.registerMBean(detector,new ObjectName("remoting:type=MulticastDetector"));
        	detector.start();
        	logger.info("Blue Dynamic Registry: Multicast Listener Started...");
	   }
	   catch(Exception e)
	   {
	       logger.debug(cn + ".startMulticastListener() - Error starting Multicast Listener");
	       throw new RegistryConfigurationException("Cannot Start Multicast Listener",e);
	   }
    }
	
	/**
	 * This method adds our registry invocation handler to the registry connector 
	 * @throws Exception - Thrown if there is an issue adding the InvocationHandler
	 */
	private void addRegisterInvocationHandler() throws RegistryConfigurationException
	{
	    try
	    {
	    	connector.addInvocationHandler("register",new RegistrationInvocationHandler(this));
	    	logger.info("Blue Dynamic Registry: Registry Invocation Handler started...");
	    }
	    catch(Exception e)
	    {
	    	logger.debug(cn + ".addRegisterInvocationHandler() - Error Adding Handler");
	    	throw new RegistryConfigurationException("Cannot Add Registration Handler",e);
	    }
	}
	
	/**
	 * This method is used to start our connector.
	 * @throws RegistryConfigurationException - Thrown if there is an issue starting the connector.
	 */
	private void startService() throws RegistryConfigurationException
	{
	    try
	    {
	    	this.connector.start();
	    	logger.info("Blue Dynamic Registry: Registry Services Started...");
	    }
	    catch(Exception e)
	    {
	    	logger.debug(cn + ".startService() - Error Starting Service");
	    	throw new RegistryConfigurationException("Error Launching Dynamic Registry Service",e);
	    }
	}
	
	/**
	 * This method is used to instruct the object walker to pre-register all
	 * objects read from our configuration sources.
	 * 
	 * @throws ConfigurationGenerationException - Thrown if there is an issue pre-registering the
	 * objects.
	 */
	private void preRegisterObjects() throws ConfigurationGenerationException
	{
		this.walker.preRegisterObjects();
	}
	
	/**
	 * This method is used to created the required directory structure for the registry
	 */
	private void createRegistryDirs()
	{
		/* Firstly strip away any trailing slashes*/
		if(this.persistDirectory.endsWith("/") || this.persistDirectory.endsWith("\\"))
		{
			this.persistDirectory = this.persistDirectory.substring(0,(this.tempDir.length()-1));
		}
		
		makeDirectories(this.tempDir);
		makeDirectories(this.persistDirectory);
	}
	
	/**
	 * Method that checks for the presence of the Blue External Command File as specified
	 * by the User in the configuration file. The Registry will exit if the External Command
	 * File does not exist.
	 * 
	 * @throws RegistryConfigurationException - Thrown if the External Command File Does
	 * not exist at the location specified by the user.
	 */
	private void verifyExternalCommandFile() throws RegistryConfigurationException
	{
		if(this.commandFile == null)
		{
			logger.debug(cn + ".verifyExternalCommandFile() - No Command File Value Set");
			throw new RegistryConfigurationException("No External Command File Specified");
		}
		
		if(!new File(this.commandFile).exists())
		{
			throw new RegistryConfigurationException("Cannot locate External Command File at :" + this.commandFile);
		}
	}
	
	/**
	 * This method is used to verify that we actually have some dynamic_template objects
	 * defined as part of the Registry Object store.
	 * 
	 * @throws RegistryConfigurationException - thrown if there are no dynamic_template objects
	 * currently defined.
	 * 
	 */
	private void verifyTemplateCount() throws RegistryConfigurationException
	{
		if(RegistryObjects.getDynamicTemplateCount() == 0)
		{
			throw new RegistryConfigurationException("There are currently no dynamic_template objects defined in Registry config files");
		}
	}
	
	/**
	 * Method to help build out the directory locations required by the Blue Dynamic Registry.
	 * @param directoryLocation - the directory location to make.
	 */
	private void makeDirectories(String directoryLocation)
	{
		if(directoryLocation == null)
		{
			return;
		}
		
		if(!new File(directoryLocation).exists())
		{
			new File(directoryLocation).mkdirs();
		}
	}
	
	/**
	 * Small utility method for instructing our cache to populate the seen host list.
	 */
	private void loadCache()
	{
	    cache = new BlueRegistryHostCache();
	    cache.setPersistLocation(this.persistDirectory);
	    cache.loadSeenHostList();
	}
}