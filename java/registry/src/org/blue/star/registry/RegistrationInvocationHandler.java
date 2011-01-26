package org.blue.star.registry;


import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.messaging.BlueMessage;
import org.blue.star.messaging.Message;
import org.blue.star.messaging.MessageProperties;
import org.blue.star.messaging.MessageTypes;
import org.blue.star.registry.common.RegistryUtils;
import org.blue.star.registry.exceptions.ConfigurationGenerationException;
import org.blue.star.registry.exceptions.RegistrationRequestException;
import org.blue.star.registry.exceptions.RegistryCacheException;
import org.blue.star.registry.objects.RegistryObjects;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;

/**
 * This is the invocation handler that deals with all requests to register with Blue.
 * It supports both multicasting and direct connections. It will verify that a given template
 * exists for the specified host. If a template is present, it will utilise the details of the 
 * remote host to create a new host object within Blue, applying the supplied services etc that
 * can be found within the templates defined by the user. 
 *
 *@author - Rob.Blake@arjuna.com
 *@version 0.1
 */
public class RegistrationInvocationHandler implements ServerInvocationHandler 
{
	/** The RegistryObjectWalker used to generate config files */
	private RegistryObjectWalker walker;
	/** The Registry instance for which we are responsible for handling requests */
	private BlueDynamicRegistry registry;
	
	/** Logging Variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.RegistrationInvocationHandler");
	private String cn = "org.blue.star.registry.RegistrationInvocationHandler";
	
	/**
	 * Constructor that takes a registry as a parameter and builds an object
	 * walker around this registry.
	 * 
	 * @param registry - The registry for which we are handling invocation requests.
	 */
	public RegistrationInvocationHandler(BlueDynamicRegistry registry)
	{
		this.registry = registry;
		this.walker = new RegistryObjectWalker(this.registry);
	}
	
	/**
	 * This is our over-ridden method to deal with requests to register.
	 * When receiving a request, we look at the type of host that is attempting to
	 * register. We ask the registry to verify that we have a template for that type of 
	 * host. If no template can be found, we apply a generic template add then ask for this
	 * to be added to our monitoring setup.
	 */
	
	public Object invoke(InvocationRequest invocation) throws Throwable
	{
		/* Our return message and our properties HashMap */
		Message message;
		
		if(invocation == null)
		{
		    logger.debug(cn + ".invoke() - Received a Null Message - Ignoring");
		    return this.sendRegistryError("Message Received was Null, Ignoring Request"); 
		}    
		
		try
		{
			this.verifySubSystem(invocation);
			
			/* Deal with the individual request types applicable to the "register" subsystem" */
			if(this.getRequestType(invocation).equals(MessageTypes.REQUEST_TO_REGISTER))
			{
				message = this.performRequestToRegister(invocation);
			}
			else if(this.getRequestType(invocation).equals(MessageTypes.REQUEST_TO_UNREGISTER))
			{
				message = this.performRequestToUnregister(invocation);
			}
			else
			{
				message = this.sendRegistryError("Your Request was Not Understood By the Registry");
				logger.debug(cn + ".invoke() - Unknown Request Type received by Registry");
			}
		}
		catch(RegistrationRequestException e)
		{
			logger.info(e.getMessage());
			message = this.sendRegistryError(e.getMessage());
		}
		
		return message;
	}
	
	/* Methods required by implementing interface ServerInvocationHandler; not currently using these */
	public void removeListener(InvokerCallbackHandler arg0){}
	public void setInvoker(ServerInvoker arg0){}
	public void setMBeanServer(MBeanServer arg0){}
	public void addListener(InvokerCallbackHandler arg0){}
		
	/**
	 * This method performs a request to register.
	 * @param remoteHostName - The remote host name to register
	 * @param remoteHostAddress - The remote IP Address to register
	 * @param agentType - The agent type to register.
	 * @throws RegistrationRequestException - Thrown if there is any issue performing the registration
	 */
	private Message performRequestToRegister(InvocationRequest invocation) throws RegistrationRequestException
	{
		logger.trace("Entering " + cn + ".performRequestToRegister");
		
		String hostname = this.getRequestHost(invocation);
		String ipAddress = this.sanityCheckIPAddress(hostname,this.getRequestIPAddress(invocation));
		String agentType = this.getAgentType(invocation);
		boolean isPersistent = false;
		
		try
		{
			 if(registry.getHostCache().hasSeenHost(ipAddress, hostname))
		    	 {
		    	     isPersistent = registry.getHostCache().isHostPersistent(ipAddress,hostname);
		    	     registry.getHostCache().addAgentToHost(ipAddress, hostname, agentType);
		    	     performRunningHostRegistration(ipAddress,hostname,agentType,isPersistent);
		    	 }
		    	 else
		    	 {
		    	     isPersistent = isPersistentRegistration(invocation);
		    	     registry.getHostCache().storeHostDetails(ipAddress, hostname, agentType, isPersistent);
		    	     
		    	     walker.addHostAndServicesToBlue(agentType,hostname,ipAddress,isPersistent);		    	 
		    	 }
		}
		catch(ConfigurationGenerationException e)
		{
			logger.info(e.getMessage());
			throw new RegistrationRequestException("Error Generating Registration Configuration Files",e);
		}
		catch(RegistryCacheException e)
		{
			logger.info(e.getMessage());
			throw new RegistrationRequestException("Error Adding Host To Registry Cache",e);
		}
		
		return this.sendRegistryMessage(MessageTypes.REGISTRATION_RESPONSE);
	}
	
	/**
	 * This method is used to enable the services for a particular agent on a host already within Blue.
	 * @param hostname - The name of the host that the services are to run on.
	 * @param IpAddress - The IpAddress of the host that the services are to run on.
	 * @param agentType - The agent type that the services should be drawn from.
	 * @param isPersistent - flag to indicate if this host is persistent or not.
	 *
	 * @throws ConfigurationGenerationException - thrown if there is any issue adding the new agent services.
	 */
	private void performRunningHostRegistration(String hostname,String IpAddress,String agentType,boolean isPersistent) throws ConfigurationGenerationException
	{
		if(hostname == null || IpAddress == null || agentType == null)
		{
			logger.debug(cn + ".performRunningHostRegistration() - Required variables were not set");
			throw new ConfigurationGenerationException("Cannot Perform Registration With Missing Variables");
		}
		
		//TODO - deal with adding to a persistent running host.
		this.walker.addAgentServicesToHost(hostname,IpAddress,agentType,isPersistent);
	}
	
	/**
	 * This method performs a request to unregister.
	 * @param remoteHostName - The remote host name to unregister
	 * @param remoteHostAddress - The remote host address to unregister
	 * @param agentType - The agentType to unregister
	 * @throws RegistrationRequestException - Thrown if there is any issue performing unregistration request.
	 */
	private Message performRequestToUnregister(InvocationRequest invocation) throws RegistrationRequestException
	{
		logger.trace("Entering " + cn + ".performRequestToUnregister");
		
		String hostname = this.getRequestHost(invocation);
		String ipAddress = this.sanityCheckIPAddress(hostname,this.getRequestIPAddress(invocation));
		String agentType = this.getAgentType(invocation);
		Message m;
		
		try
		{
			/* Check to see if the cache knows about this host */
		    	if(!registry.getHostCache().hasSeenHost(ipAddress, hostname))
		    	{
		    	    throw new RegistrationRequestException("Cannot unregister host '" + hostname + " (" + ipAddress + ")' as it is Unknown to the Registry");
		    	}
		    
		    	this.registry.getHostCache().removeAgentFromHost(ipAddress,hostname,agentType);
			
			/* If there are no more agents running on the host, disable monitoring for it */
			if(this.registry.getHostCache().getHostAgentCount(ipAddress, hostname) == 0)
			{
				this.walker.deleteHostFromBlue(hostname);
				this.registry.getHostCache().removeHost(ipAddress,hostname);
			}
			else
			{
				/* Otherwise simply remove the services associated with the agent */
				this.walker.removeAgentServicesFromHost(hostname, agentType);
			}
			
			m = sendRegistryMessage(MessageTypes.UNREGISTER_RESPONSE);
		}
		catch(RegistryCacheException e)
		{
			logger.debug(cn + ".performRequestToUnregister() - Error Consulting Registry Cache");
			m = sendRegistryError("Registry Is Unable To Complete Request To Unregister");
		}
		catch(ConfigurationGenerationException e)
		{
			logger.debug(cn + ".performRequestToUnregister() - Unable to remove Host from Blue");
			m = sendRegistryError("Registry Is Unable To Complete Request To Unregister");
		}

		return m;
	}
	
	/**
	 * This method is used to check to see if the received request is for the correct subsystem
	 * @param request - The request to inspect.
	 * @throws RegistrationRequestException - Thrown if the request is not for the correct subsystem.
	 */
	private void verifySubSystem(InvocationRequest request) throws RegistrationRequestException
	{
		if(request == null || request.getSubsystem() == null || !request.getSubsystem().equals("register"))
		{
			throw new RegistrationRequestException("Registration Request was not for Correct Subsystem");
		}
	}
	
	/**
	 * Get the Agent Type that has been sent with this request.
	 * 
	 * @param request - The request to get the type of.
	 * @return - The request type of the request.
	 * @throws - RegistrationRequestException  - Thrown if the request type cannot be determined.
	 */
	private String getAgentType(InvocationRequest request) throws RegistrationRequestException
	{
		try
		{
			return request.getRequestPayload().get(MessageProperties.AGENT_TYPE).toString();
		}
		catch(Exception e)
		{
			logger.debug(cn + ".getRequestType() - Cannot determine Request Type");
			throw new RegistrationRequestException("Cannot Determine Request Type",e);
		}
	}
	
	/**
	 * This method is used to get the hostname from which the original request was sent.
	 * @param request - The request object.
	 * @return - The name of the host from where the request was received.
	 * @throws - RegistrationRequestException - Thrown if the remote host cannot be determined.
	 */
	private String getRequestHost(InvocationRequest request) throws RegistrationRequestException
	{
		try
		{
			return request.getRequestPayload().get(MessageProperties.HOST_NAME).toString();
		}
		catch(Exception e)
		{
			logger.debug(cn + ".getRequestHost() - Cannot Determine Request Host");
			throw new RegistrationRequestException("Cannot Determine Request Host",e);
		}
	}
	
	/**
	 * Get the IP Address from where this request originated.
	 * @param request - The request object.
	 * @return - The IPAddress of origin.
	 * @throws RegistrationRequestException - Thrown if there is any issue determining the request origin.
	 */
	private String getRequestIPAddress(InvocationRequest request) throws RegistrationRequestException
	{
		try
		{
			return request.getRequestPayload().get(MessageProperties.IP_ADDRESS).toString();
		}
		catch(Exception e)
		{
			logger.debug(cn + ".getRequestIPAddress() - Cannot Determine Request IP Address");
			throw new RegistrationRequestException("Cannot Determine Request IP Address",e);
		}
	}
	
	/**
	 * Get The Request Type that has been sent to the Blue Registry.
	 *  
	 * @param request - The request to pull the information from.
	 * @return - The agent type that is requesting to register.
	 * @throws RegistrationRequestException - Thrown if the agent type cannot be determined.
	 */
	private String getRequestType(InvocationRequest request) throws RegistrationRequestException
	{
		try
		{
			return request.getParameter().toString();
		}
		catch(Exception e)
		{
			logger.debug(cn + ".getAgentType() - Cannot determine Request Type");
			throw new RegistrationRequestException("Cannot Determine Agent Type of Request",e);
		}
	}
	
	/**
	 * This method is used to sanity check the IP Address of the message received against
	 * our own DNS records.
	 * 
	 * @param hostname - The name of the host from which the request was received.
	 * @param currentIPAddress - The supposed IP Address of the remote host.
	 * @return - The true IP Address of the remote host.
	 */
	private String sanityCheckIPAddress(String hostname,String currentIPAddress)
	{
		try
		{
			if(!currentIPAddress.equals(InetAddress.getByName(hostname).getHostAddress()))
			{
				currentIPAddress = InetAddress.getByName(hostname).getHostAddress();
			}
		}
		catch(Exception e)
		{
			logger.debug(cn + ".sanityCheckIPAddress() - Unable to Determine Remote IP Address");
		}
		
		return currentIPAddress;
	}
	
	/**
	 * This method is used to check to see if the request received includes a registration that should be
	 * made persistent.
	 * @param request - The received request.
	 * @return - boolean, true if this registration should be made persistent.
	 */
	private boolean isPersistentRegistration(InvocationRequest request)
	{
		boolean persist = false;
		
		try
		{
			if(request.getRequestPayload().containsKey(MessageProperties.PERSIST_REGISTRATION))
			{
			    persist = RegistryUtils.stringToBoolean((String)request.getRequestPayload().get(MessageProperties.PERSIST_REGISTRATION));
			}
			else
			{
			    persist = RegistryObjects.persistAgentType(this.getAgentType(request));
			}
		}
		catch(Exception e)
		{
			logger.debug(cn + ".isPersistentRegistration() - Error Occurred Checking for Persistence Property in Message");
		}
		
		return persist;
	}
	
	/**
	 * This method is used to generate an error message from the Registry to be
	 * send back to the remote Agent.
	 * 
	 * @param errorMessage - The error message from the registry.
	 * @return - The Message object.
	 */
	private Message sendRegistryError(String errorMessage)
	{
		Message m = new BlueMessage();
		m.setMessageType(MessageTypes.REGISTRY_ERROR);
		
		Map<String,String> props = new HashMap<String,String>();
		props.put(MessageProperties.REGISTRY_ERROR_MESSAGE,errorMessage);
		m.setMessageProperties(props);		
		
		return m;
	}
	
	/**
	 * Method to send a bog standard message back to the agent.
	 * @param messageType - The type of message to send back
	 * @return - The message to send back.
	 */
	private Message sendRegistryMessage(String messageType)
	{
		Message m = new BlueMessage();
		m.setMessageType(messageType);
		
		return m;
	}
}