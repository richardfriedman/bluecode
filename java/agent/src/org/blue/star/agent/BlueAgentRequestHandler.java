package org.blue.star.agent;

import java.net.MalformedURLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.transport.Connector;

public class BlueAgentRequestHandler extends Thread
{
	/* Private Variables */
	private Connector connector;
	private InvokerLocator locator;
	private Agent agent;
	
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.BlueAgentRequestHandler");
	private String cn = "org.blue.star.registry.agent.BlueAgentRequestHandler";
	 
	/* Constructor */
	public BlueAgentRequestHandler(Agent agent)
	{
		this.agent = agent;
	}
	
	/* Our Thread method */
	public void run()
	{
		this.setName("Agent Request Handler");
		this.launchPluginRequestHandler();
	}
	
	/* Launch our Plugin Request Handler */
	private void launchPluginRequestHandler()
	{
		try
	    	{
	    	    locator = new InvokerLocator(agent.getTransport() + "://" + (agent.getHostName() == null ? agent.getIPAddress():agent.getHostName()) + ":" + agent.getPort() + "/?clientLeasePeriod=120000&timeout=10000");
	    	    
	    	    /* Create our connector & start it */
	    	    connector = new Connector();
	    	    connector.setInvokerLocator(locator.getLocatorURI());
	    	    connector.create();
	    	    connector.addInvocationHandler("executeCheck",new BlueAgentPluginInvocationHandler(agent));
	    	    connector.start();
	    	    logger.info("Blue Agent 0.1 - Remote Checking Service Launched");
	    	}
	    	catch(MalformedURLException e)
	    	{
	    	    logger.info("Blue Agent 0.1 - Malformed URL for Remote Checking Service, shutting down");
	    		logger.debug(cn + ".launchPluginRequestHandler() - Malformed URL for InvokerLocator");
	    	    this.agent.shutdown();
	    	}
	    	catch(Exception e)
	    	{
	    	    logger.info("Blue Agent 0.1 - Error Launching Remote Checking Service, shutting down");
	    		logger.debug(cn + ".launchPluginRequestHandler() - Error starting Connector for Checking Service");
	    	    this.agent.shutdown();
	    	}
	}
}