package org.blue.star.common.agent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.agent.exceptions.UnknownCommandException;

/**
 * <p>This class represents a command cache to be used by the Blue Agent. The Agent can
 * store in this cache, any commands that are defined by the user in the configuration.
 * It can also store any new commands that are passed to it by the registry.</p> 
 * 
 * <p>The BlueAgentPluginInvocationRequestHandler also consults this cache to verify
 * that a command exists before it begins the execution of a plugin.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class CommandCache 
{
	/** Cache for our defined commands */
    private Map<String,String> commandCache;
    
    /** Logging */
    private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.CommandCache");
    private String cn = "org.blue.star.registry.agent.CommandCache";
    
    /** Flag to indicate whether or not we preserve commands */
    private boolean preservesCommands;
    
    public CommandCache()
    {
    	commandCache = new HashMap<String,String>();
    }
    
    /**
     * This method returns whether or not this CommandCache is set to preserve commands
     * or not.
     * 
     * @return - boolean, true if the CommandCache is set to preserve commands.
     */
    public boolean getPreservesCommands()
    {
    	return this.preservesCommands;
    }
    
    /**
     * This method is used to set whether or not this CommandCache will preserve commands.
     * @param preservesCommands - boolean, true if you wish this CommandCache to preserve
     * commands.
     */
    public void setPreservesCommands(boolean preservesCommands)
    {
    	this.preservesCommands = preservesCommands;
    }
    
    /**
     * This method is used to add a command to the command cache.
     * @param commandName - The name of the command to add.
     * @param commandLine - The command line associated with this command.
     */
    public void addCommand(String commandName,String commandLine)
    {
    	if(commandName == null || commandLine == null)
    		return;
    	
    	if(preservesCommands)
    	{
    		addPreservedCommand(commandName,commandLine);
    		return;
    	}
    	
    	commandCache.put(commandName,commandLine);
    }
    
    /**
     * This method is used to check to see if a command should be added in the
     * event of this CommandCache being set to preserve commands.
     * @param commandName - the name of the command to add.
     * @param commandLine - the command line of the command to add.
     */
    private void addPreservedCommand(String commandName,String commandLine)
    {
    	if(!commandCache.containsKey(commandName))
    	{
    		commandCache.put(commandName,commandLine);
    	}
    }
    
    /**
     * This method is used to put a collection of commands into the command cache.
     * @param commands - The collection of commands to add to the command cache.
     */
    public void addCommands(Map<String,String> commands)
    {
    	if(commands == null || commands.size() == 0)
    		return;
    	
    	if(preservesCommands)
    	{
    		addPreservedCommands(commands);
    		return;
    	}
    	
    	this.commandCache.putAll(commands);
    }
    
    /**
     * This method is used to check to see if a collection of commands should be
     * added to the cache should this CommandCache be set to preserve commands.
     * @param commands - The Map of commands to add to the cache.
     */
    private void addPreservedCommands(Map<String,String> commands)
    {
    	for(Iterator<String> i = commands.keySet().iterator();i.hasNext();)
    	{
    		String commandName = i.next();
    		addPreservedCommand(commandName,commands.get(commandName));
    	}
    }
    
    /**
     * This method is used to remove a command from the command cache.
     * @param commandName - The name of the command to remove.
     */
    public void removeCommand(String commandName)
    {
    	if(commandName == null)
    		return;
    	
    	try
    	{
    		commandCache.remove(commandName);
    	}
    	catch(Exception e)
    	{
    		logger.debug(cn + ".removeCommand() - Command was not found in Command Cache.");
    	}
    }
    
    /**
     * This method is used to remove a collection of commands from the command cache.
     * @param commands - The collection of commands to remove.
     */
    public void removeCommands(Map<String,String> commands)
    {
    	if(commands == null || commands.size() == 0)
    		return;
    	
    	for(Iterator<String> i = commands.keySet().iterator();i.hasNext();)
    	{
    		this.removeCommand(i.next());
    	}
    }
    
    /**
     * This method is used to verifiy if a command identified by the given command name
     * exists within the command cache.
     * @param commandName - The name of the command to verify that exists.
     * @return - boolean, true if the command exists.
     */
    public boolean checkCommandExists(String commandName)
    {
    	if(commandName == null)
    	{
    		return false;
    	}
    	
    	return commandCache.containsKey(commandName);
    }
    
    /**
     * This method is used to retrieve the command line for a given command name.
     * @param commandName - The name of the command to retrieve.
     * @return - The command line for this command.
     * @throws UnknownCommandException - Thrown if the command is not within the cache.
     */
    public String getCommandLine(String commandName) throws UnknownCommandException
    {
    	if(commandName == null)
    	{
    		throw new UnknownCommandException("Cannot retrieve Command Line for Null Command");
    	}
    	
    	if(this.commandCache.get(commandName) == null)
    	{
    		throw new UnknownCommandException("Command '" + commandName + "' not defined on Host");
    	}
    	
    	return this.commandCache.get(commandName);
    }
    
    /**
     * This method is used to return all commands currently known by this command cache.
     * @return - Map containing the details of all commands currently known by this
     * command cache.
     */
    public Map<String,String> getAllCommands()
    {
    	return this.commandCache;
    }
    
    /**
     * This method is used to retrieve the total number of commands that are currently stored within
     * the CommandCache.
     * @return - int, the total number of commands stored within the CommandCache.
     */
    public int getCommandCount()
    {
    	if(this.commandCache != null)
    	{
    		return this.commandCache.size();
    	}
    	
    	return 0;
    }
}