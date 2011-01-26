package org.blue.star.agent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.management.MBeanServer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.agent.exceptions.BlueAgentException;
import org.blue.star.common.agent.utils.Utils;
import org.blue.star.messaging.BlueMessage;
import org.blue.star.messaging.Message;
import org.blue.star.messaging.MessageProperties;
import org.blue.star.messaging.MessageTypes;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;

/**
 * <p>This class acts as an invocation handler for the execution of plugins by the Blue Agent.
 * Currently this class utilises exec() to call the plugin, however this should be evaluated
 * to allow for the instantiation of classes to complete plugin work.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */ 
public class BlueAgentPluginInvocationHandler implements ServerInvocationHandler 
{
	/** Logging variables */
    private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.BlueAgentPluginInvocationHandler");
	private String cn = "org.blue.star.registry.agent.BlueAgentPluginInvocationHandler";
    	
	/** The Agent that this InvocationHandler will be working for */
	private Agent agent;
		
	/** Messaging Variables */
	private Message m = new BlueMessage();
	private HashMap<String,String> props = new HashMap<String,String>();
	
	/** The name of the command that is to be executed */
	private String commandName;
	/** Any command Args that should be passed to the command execution */
	private String commandArgs;
		
	public BlueAgentPluginInvocationHandler(Agent agent)
	{
	    this.agent = agent;
	}
	
	/* Our Overridden invoke method */
	public Object invoke(InvocationRequest request) throws Throwable
	{
		logger.trace("Entering " + cn + ".invoke");
		int returnCode;
		String pluginOutput = "";
		
		/* TODO -For now we are only working with commands that have been defined */
		// Can we instantiate an instance of this class?
		// If not, can we download a copy from our repository?
    	
		try
		{
		    if(request == null)
		    	throw new BlueAgentException("InvocationRequest object was null");
        		
		    /* Check the request is for our subsystem */
		    this.verifyRequestSubsystem(request);
        		
		    /* Pull out the name of the plugin */
		    commandName = this.getPluginToExecute(request);
        		        		
		    /* See if any args have been passed with the plugin name */
		    commandArgs = this.retrievePluginArgs(request);
		
		    /* See if the user has already defined this command in the cfg file */
		    String commandLine = this.agent.getCache().getCommandLine(commandName);
	    	
	    	if(this.agent.allowsCommandArguments())
	    	{
	    		commandLine = Utils.parseCommandMacros(commandLine, commandArgs);
	    	}
		    
		    //TODO - Update here to deal with In VM Plugin Execution
		    
		    /* Fork a process and get and InputStream on that process */
       		Process p = Runtime.getRuntime().exec(commandLine);
        	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
       		
        	/* Grab the output from this plugin */
       		String line = reader.readLine();
        			
       		/* We want to grab the last line of the plugin output for the time being */
       		while(line != null)
       		{
       		    pluginOutput = line;
       		    line = reader.readLine();
       		}
        			
        	p.waitFor();
        	reader.close();
        			
        	returnCode = p.exitValue();	        
        	this.returnMessage(pluginOutput,String.valueOf(returnCode));
		}
		catch(Exception e)
		{
			this.returnMessage(e.getMessage(),"3");
		}
		
		return m;
	}
	
	/* Required Interface Methods */
	public void addListener(InvokerCallbackHandler arg0){}
	public void removeListener(InvokerCallbackHandler arg0){}
	public void setInvoker(ServerInvoker arg0){}
	public void setMBeanServer(MBeanServer arg0){}

	/* Send a return message */
	private Message returnMessage(String message,String returnCode)
	{
		props.put(MessageProperties.PLUGIN_OUTPUT,message);
		props.put(MessageProperties.PLUGIN_RETURN_CODE,returnCode);
		m.setMessageProperties(props);
		m.setMessageType(MessageTypes.REMOTE_SERVICE_CHECK_RESULT);
		return m;
	}
	
	/**
	 * This method is used to verify that the received request is for the correct
	 * subsystem
	 * @param req - The received request.
	 * @throws BlueAgentException - Thrown if the Message is not for the correct subsystem.
	 */
	private void verifyRequestSubsystem(InvocationRequest req) throws BlueAgentException
	{
	    if(!req.getSubsystem().equals("executeCheck"))
	    	throw new BlueAgentException("Request not for Correct sub-system");
	}
	
	/**
	 * This method is used to retrieve the name of the plugin that should be executed in this
	 * request.
	 * @param req - The request to execute a plugin
	 * @return - The name of the plugin that is requested to execute.
	 * @throws BlueAgentException - Thrown if there is an issue determining the name of the plugin
	 * to execute.
	 */
	private String getPluginToExecute(InvocationRequest req) throws BlueAgentException
	{
	    try
	    {
	    	return req.getParameter().toString();
	    }
	    catch(Exception e)
	    {
			logger.debug(cn + ".getPluginToExecute() - Error obtaining plugin name");
			throw new BlueAgentException("Cannot Determine Plugin To Execute",e);
		}
	}
	
	/**
	 * This method is used to retrieve any arguments that may have been passed along 
	 * with the request to execute the plugin.
	 * @param req - The request to execute the plugin
	 * @return - Any arguments associated with the request, null if there were none.
	 */
	private String retrievePluginArgs(InvocationRequest req)
	{
	    try
	    {
	    	return (String)req.getRequestPayload().get(MessageProperties.PLUGIN_ARGS);
	    }
	    catch(Exception e)
	    {
			logger.debug(cn + ".retrievePluginArgs() - This Plugin has No Args");
			return null;
		}
	}
}