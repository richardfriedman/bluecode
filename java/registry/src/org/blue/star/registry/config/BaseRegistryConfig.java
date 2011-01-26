package org.blue.star.registry.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.registry.BlueDynamicRegistry;
import org.blue.star.registry.common.RegistryUtils;
import org.blue.star.registry.exceptions.RegistryConfigurationException;
import org.blue.star.registry.objects.RegistryObjects;

/**
 * 
 * 	@created 30 Jul 2007 - 10:39:40
 *	@filename org.blue.star.registry.config.BaseRegistryConfig.java
 *  @author Rob.Blake@arjuna.com
 *	@version 0.1
 *
 *	<p>This class performs the base configuration for the Blue Registry. It should be extended
 *	by any custom configuration engine.</p>
 *
 */
public abstract class BaseRegistryConfig implements RegistryConfig{

    /** The options that can be used with this Agent. */
	private Options registryOptions = new Options();
	
	/** The location of the configuration file we are to read */
	protected String configLocation = "etc/registry.cfg";
	/** The class that is to be used as a configuration engine for the registry */
	protected String configClass;
	/** The registry instance we are to configure */
	protected BlueDynamicRegistry registry;
	
	/** Logging */
	protected static Logger logger = LogManager.getLogger(BaseRegistryConfig.class.getClass());
	private String cn = "org.blue.star.registry.config.BaseRegistryConfig";
	
	/* setRegistryToConfigure() method */
	public void setRegistryToConfigure(BlueDynamicRegistry registry)
	{
		this.registry = registry;
	}
	
	/* getCurrentRegistry() method */
	public BlueDynamicRegistry getCurrentRegistry()
	{
		return this.registry;
	}
	
	/* setConfigurationSource() method */
	public void setConfigurationSource(String configurationSource)
	{
		this.configLocation = configurationSource;
	}
	
	public String getConfigurationSource()
	{
		return this.configLocation;
	}
	
	/* getRegistryOptions() method */
	public Options getRegistryOptions()
	{
	    return this.registryOptions;
	}
	
	/* parseRegistryArguments() method */
	public void parseRegistryArguments(String[] args) throws RegistryConfigurationException
	{
	    logger.info("Blue Dynamic Registry: Parsing Command Line Arguments...");
	    
	    if(args == null || args.length == 0)
	    {
	    	configureRegistry();
	    	return;
	    }
	    
		CommandLine cmd = null;
		setRegistryOptions();
		
	    try
	    {
			cmd = new PosixParser().parse(getRegistryOptions(),args);
	    }
	    catch(ParseException e)
	    {
	    	logger.debug(cn + ".parseRegistryArguments() - Unable to parse Registry Options");
	    	throw new RegistryConfigurationException("Unable To Parse Command Line Args",e);	
	    }
		  
	    Iterator i = cmd.iterator();
			    
	    while(i.hasNext())
	    {
			Option o =(Option)i.next();
			String argValue = o.getValue().trim();
					  
			switch(o.getId())
			{    
				case '?':
			    case 'h':
			        this.printHelp();
			        break;
			    	    
			    case 'c':
			        this.configLocation = argValue;
			        break;
			}
	    }
	    
	    logger.info("Blue Dynamic Registry: Command Line Parsed...");
	    configureRegistry();
	}
	
	/* Read the object configuration data relevant to the registry */
	public void readObjectConfigurationData() throws RegistryConfigurationException
	{
	    RegistryObjects.readObjectConfigurationData(this.configLocation);
	}
	
	/* configureRegistry() method */
	public void configureRegistry() throws RegistryConfigurationException
	{
	    	logger.info("Blue Dynamic Registry: Beginning Configuration Processing...");
	    	
			BufferedReader reader = null;
    		int lineCount = 0;
    		String line;
    		String variable;
    		String value;
    		
    		File config_file = new File(this.configLocation);
    	
    		if(!config_file.exists() || !config_file.canRead())
    		{
    		    logger.debug(cn + ".configureRegistry() - Cannot open configuration source");
    		    throw new RegistryConfigurationException("Cannot Open Configuration Source '" + this.configLocation + "' for Reading");
    		}
    	
    		try
    		{
    		    reader = new BufferedReader(new FileReader(config_file));
    		    line = reader.readLine();
    		
    		    while(line != null)
    		    {
	    			// Ignore blank lines or comments.
	    			if(line.length() !=0 && !line.startsWith("#"))
	    			{
	    			    String[] bits = line.split("=",2);
	    				
	    			    /* Check for valid variable/value definition */
	    			    if(bits.length != 2 || bits[1].length() == 0)
	    			    {
	    			    	throw new RegistryConfigurationException("Error: NULL Variable at line " + lineCount + " of configuration file " + this.configLocation);
	    			    }
	    				
	    			    variable = bits[0].trim();
	    			    value = bits[1].trim();
	    				
	    			    /* Assign variable values */
	    			    if(variable.equals("registry_transport")) this.registry.setTransport(value); 
	    			    else if(variable.equals("registry_port")) this.registry.setPort(stringToInt(value));
	    			    else if(variable.equals("registry_enabled")) this.registry.setRegistryEnabled(RegistryUtils.stringToBoolean(value));
	    			    else if(variable.equals("multicast_registration_enabled")) this.registry.setAllowMulticasts(RegistryUtils.stringToBoolean(value));
	    			    else if(variable.equals("allow_unknown_registration")) this.registry.setAllowUnknownRegistration(RegistryUtils.stringToBoolean(value));
	    			    else if(variable.equals("command_file")) this.registry.setCommandFile(value);
	    			    else if(variable.equals("persist_dir")) this.registry.setPersistDirectory(value);
	    			    
	    			}
    			
	    			/* Read the next line */
	    			line = reader.readLine();
	    			lineCount++;
    		    }
    		   
    		    readObjectConfigurationData();
    		    //TODO - visit this.
    		    
    		    logger.info("Blue Dynamic Registry: Configuration Successfully Read...");
    		}
    		catch(IOException e)
    		{
    		    logger.fatal("Error reading from Registry configuration file");
    		    throw new RegistryConfigurationException("Unable to Read From Configuration Source '" + this.configLocation + "'");
    		}
    		finally
    		{
    		    try
    		    {
    		    	if(reader != null)
    		    	{
    		    		reader.close();
    		    	}
    		    }
    		    catch(IOException e)
    		    {
    		    	logger.debug(cn + ".configureRegistry() - Unabled To Close File Handle on Configuration File");
    		    }
    		}
    	}
	
	/**
	 * This method sets the currently supported command line arguments for the Registry.
	 */
	protected void setRegistryOptions()
	{
	    this.registryOptions.addOption( "h", "help", false, "Display Help");
	    this.registryOptions.addOption( "?", "help", false, "Display Help");
	    	    
	    Option c = new Option("c","Configuration File",true,"Location of the Configuration File with which to start the Regsitry");
	    c.setArgName("Configuration File");
	    this.registryOptions.addOption(c);
	}
	
	/**
	 * This method prints out the help for the above available options.
	 */
	protected void printHelp() throws RegistryConfigurationException
	{
	    System.out.println("");
	    System.out.println("Usage: java -jar blue-registry.jar [option] <main_config_file>" );
	    System.out.println("");
	    System.out.println("Options:");
	    System.out.println("");
	    System.out.println("	-c   Specifies the location of the configuration file with which to");
	    System.out.println("        start the registry. It is recommended that you place all registry");
	    System.out.println("        configuration into your main Blue config files.");
	    System.out.println("");
	    System.out.println("	-V	 Print versioning information about the Registry");
	    System.out.println("");
	    System.out.println("	-?	 Show this Help Information");
	    System.out.println("");
	    System.out.println("	-h	 Show this Help Information");
	    System.out.println("");
	    System.out.println("For more information on how to configure the registry, please see documentation");
	    System.out.println("at http://blue.sourceforge.net");
	    System.out.println("");
		
	    throw new RegistryConfigurationException();
	}
	
	/**
	 * This method is used to get a BufferedReader on the configuration source associated with
	 * this Configuration Engine.
	 * @return - A BufferedReader onto the configuration source of this configuration engine.
	 */
	protected abstract BufferedReader getBufferedReader(String configLocation) throws RegistryConfigurationException;
	
	/**
	 * This method is used to return the int value of a given string from the config source.
	 * @param value - The value to get the int from.
	 * @return - the int value
	 * @throws RegistryConfigurationException - thrown if there is an issue getting the int value.
	 */
	private int stringToInt(String value) throws RegistryConfigurationException
	{
	    try
	    {
	    	return Integer.valueOf(value);
	    }
	    catch(NumberFormatException e)
	    {
	    	logger.debug(cn + ".getIntegerValueOfString() - NFE");
	    	throw new RegistryConfigurationException("Value Must be a postive Integer",e);
	    }
	}
}