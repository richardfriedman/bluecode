package org.blue.star.agent;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.agent.config.BlueAgentConfig;
import org.blue.star.common.agent.exceptions.AgentRegistrationException;
import org.blue.star.messaging.Message;
import org.blue.star.messaging.MessageProperties;
import org.blue.star.messaging.MessageTypes;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;

/**
 * This class is the initial Blue Agent that is used to launch the remote checking facilities
 * and communicate with the any remote Registry.
 * 
 * @author Rob.Blake@arjuna.com
 * @verson 0.1
 *
 */
public class BlueAgent extends BaseAgent
{
	/** The Client that we will use to connect to the remote Registry */
	private Client client;
	/** Our invoker Locator for invoking the Registry */
	private InvokerLocator invoker;
	/** Connection properties for the Registry */
	private String clientProps = "/?clientMaxPoolSize=10&numberOfRetries=1&numberOfCallRetries=1&timeout=360000";
	
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.agent.BlueAgent");
	private String cn = "org.blue.star.registry.agent.BlueAgent";
	
	/** Constructor instantiates our AgentConfig object */
	public BlueAgent()
	{
		this.config = new BlueAgentConfig(this);
		
	}
	
	/* register() method */
	public void register() throws AgentRegistrationException
	{
		/* If Multicasting is allowed, attempt to make a multicast connection */
		if(this.isMulticasting())
		{
			this.performMulticastRegistration();
		}
		else
		{
			/* We're going to try and connect using statically defined variables */
			if(this.getRegistryHost() == null || this.getRegistryPort() == 0 || this.getTransport() == null)
			{
				logger.debug(cn + ".register() - Aborting Registration Request due to missing Configuration Parameters");
				throw new AgentRegistrationException("Registry Host, Registry Port and Agent Transport must be defined if not Multicasting");
			}
			
			this.performRegistration();
		}
	}
	
	
	/* unregister() method */
	public void unregister() throws AgentRegistrationException
	{
		try
		{
			if(this.client != null && this.client.isConnected())
			{
				handleResponse(client.invoke(MessageTypes.REQUEST_TO_UNREGISTER,this.createMessageProperties()));
			}
		}
		catch(Throwable t)
		{
			logger.debug(cn + ".unregister() - Error unregistering from Remote Registry");
			throw new AgentRegistrationException("Cannot Unregister from Registry",t);
		}
		finally
		{
			if(this.client !=null)
			{
				try
				{
					client.disconnect();
				}
				catch(Exception E)
				{
					logger.debug(cn + ".unregister() - Error Disconnecting from Remote Host");
				}
			}
		}
	}
	
	/**
	 * This method is used to try and register with a remote Registry.
	 * @throws AgentRegistrationException - Thrown if there is an issue registering with the registry.
	 */
	private void performRegistration() throws AgentRegistrationException
	{
		logger.trace("Entering " + cn + ".performRegistration");
		
		try
		{
			/* Set up the invoker around user defined variables */
			this.invoker = new InvokerLocator(this.getTransport() + "://" + this.getRegistryHost() + ":" + this.getRegistryPort() + this.clientProps);
			this.client = new Client(invoker);
			this.client.setSubsystem("register");
			this.client.connect();
			this.handleResponse(client.invoke(MessageTypes.REQUEST_TO_REGISTER,this.createMessageProperties()));
		}
		catch(Throwable e)
		{
			logger.debug(cn + ".register() - Error Registering with Remote Registry");
			throw new AgentRegistrationException("Error Registering with Remote Registry",e);
		}
	}
	
	/**
	 * This method is used to generate the generic properties for a request
	 * sent to the Registry.
	 * 
	 * @return - The Map containing the properties of a request to register.
	 */
	private Map<String,String> createMessageProperties()
	{
		Map<String,String> props = new HashMap<String,String>();
		props.put(MessageProperties.HOST_NAME,this.getHostName());
		props.put(MessageProperties.IP_ADDRESS,this.getIPAddress());
		props.put(MessageProperties.AGENT_TYPE,this.getAgentType());
		
		return props;
	}
	
	/**
	 * This method is used to perform a multicast registration with a remote registry.
	 * @throws AgentRegistrationException - Thrown if there is an issue creating a client to
	 * the remote registry or no remote registry was found via multicasting.
	 */
	private void performMulticastRegistration() throws AgentRegistrationException
	{
		logger.trace("Entering " + cn + ".performMulticastRegistration");
		
		this.multicastAgent = new MulticastAgent(this.getHostName(),this.getIPAddress(),this.getAgentType(),this.getTimeOut());
		this.multicastAgent.multicast();
		
		if(multicastAgent.getFinalInvokerLocator() != null)
		{
			try
			{
				invoker = multicastAgent.getFinalInvokerLocator();
				client = new Client(invoker);
				client.setSubsystem("register");
				client.connect();
				return;
			}
			catch(Exception e)
			{
				logger.debug(cn + ".performMulticastRegistration() - Error creating Client to Multicast Discovered Registry");
				throw new AgentRegistrationException("Error Creating Client to Multicast Discovered Registry",e);
			}
		}
		
		throw new AgentRegistrationException("Cannot Find Remote Registry via Multicast Registration");
	}
	
	/**
	 * This method deals with any response that is received from the registry.
	 * @param response - The response object.
	 */
	private void handleResponse(Object response) throws AgentRegistrationException
	{
		if(response  == null)
			return;
		
		if(response instanceof Message)
		{
			Message m = (Message)response;
			
			if(m.getMessageType().equals(MessageTypes.REGISTRY_ERROR))
			{
				logger.info("Error received from Registry: " + m.getMessageProperties().get(MessageProperties.REGISTRY_ERROR_MESSAGE));
				throw new AgentRegistrationException();
			}
			else if(m.getMessageType().equals(MessageTypes.REGISTRATION_RESPONSE))
			{
				logger.info("Registration with Registry Successful!");
			}
		}
	}
	
	/**
	 * Main method to run the agent when utilising this in .jar format.
	 * @param args - Command line parameters to the agent.
	 */
	public static void main(String[] args)
	{
		new BlueAgent().init(args);
	}		
}