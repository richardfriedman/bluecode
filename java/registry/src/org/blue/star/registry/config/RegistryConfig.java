package org.blue.star.registry.config;

import org.apache.commons.cli.Options;
import org.blue.star.registry.BlueDynamicRegistry;
import org.blue.star.registry.exceptions.RegistryConfigurationException;

/**
 * This interface is used to define the Configuration Mechanisms that should
 * be used to configure the Blue Dynamic Registry
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public interface RegistryConfig 
{

    	/**
	 * This method is used to set the source from which the Registry should read
	 * its configuration
	 * @param configurationSource - The source of the configuration.
	 */
	public void setConfigurationSource(String configurationSource);
	
	/**
	 * This method is used to retrieve the current source from which this Registry
	 * is reading it's configuration.
	 * @return - The current source of configuration for this Registry.
	 */
	public String getConfigurationSource();
	
	/**
	 * This method is used to set Registry that should be configured.
	 * @param Registry - The Registry to be configured.
	 */
	public void setRegistryToConfigure(BlueDynamicRegistry registry);
	
	/**
	 * This method is used to get the current Registry that is being configured.
	 * @return - The Registry that is being configured.
	 */
	public BlueDynamicRegistry getCurrentRegistry();
	
	/**
	 * This method is used to configure the registry with the properties from the configuration
	 * source.
	 * @throws RegistryConfigurationException - Thrown if there is an issue configuring the registry.
	 */
	public void configureRegistry() throws RegistryConfigurationException;
	
	/**
	 * This method is used to parse any options that have been passed to the Registry.
	 * @param args - The arguments that have been passed to Registry.
	 */
	public void parseRegistryArguments(String[] args) throws RegistryConfigurationException;
	
	/**
	 * This returns the currently available command line options that can be used with this Registry.
	 * @return - The options to be used with this Registry.
	 */
	public Options getRegistryOptions();
	
	/**
	 * This method is used to read the Object Configuration Data that is associated with
	 * the Blue Dynamic Registry.
	 */
	public void readObjectConfigurationData() throws RegistryConfigurationException;
}
