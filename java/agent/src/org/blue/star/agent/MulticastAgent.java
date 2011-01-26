package org.blue.star.agent;

import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.blue.star.messaging.BlueMessage;
import org.blue.star.messaging.MessageProperties;
import org.blue.star.messaging.MessageTypes;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.detection.multicast.MulticastDetector;
import org.jboss.remoting.network.NetworkNotification;
import org.jboss.remoting.network.NetworkRegistry;

public class MulticastAgent implements NotificationListener 
{
	private MBeanServer server;
	private NetworkRegistry registry;
	private MulticastDetector detector;
	private boolean onLine;
	private long timeout;
	private String agentType;
	private String ipAddress;
	private String hostname;
	private InvokerLocator locator;
	
	public MulticastAgent()
	{
		
	}
	
	public MulticastAgent(String hostname,String ipAddress,String agentType,long timeout)
	{
		this.hostname = hostname;
		this.ipAddress = ipAddress;
		this.agentType = agentType;
		this.timeout = timeout;
	}
	
	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}
	
	public long getTimeout()
	{
		return this.timeout;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
	
	public void setAgentType(String agentType)
	{
		this.agentType = agentType;
	}
	
	/* We use this to retrieve the final invoker location of our remote Blue host */
	public InvokerLocator getFinalInvokerLocator()
	{
		return this.locator;
	}
	
	/* Start our mulitcasting */
	public void multicast()
	{
		try
		{
			initMulticast();
			
			// - TODO - add in a timeout mechanism here.
			System.out.println("Searching for Blue...");
			while(!onLine)
			{
				Thread.sleep(10);
				
			}
		}
		catch(Exception e)
		{
			  
		}
	}
	
	/* Initialise our Multicast Detector sequence */
	public void initMulticast() throws Exception
	{
		server = MBeanServerFactory.createMBeanServer();
		registry = NetworkRegistry.getInstance();
		server.registerMBean(registry,new ObjectName("remoting:type=NetworkRegistry"));
		registry.addNotificationListener(this,null,null);
		
		detector = new MulticastDetector();
		server.registerMBean(detector,new ObjectName("remoting:type=MulticastDector"));
		detector.start();
	}
		
	/* Handle the JMX notification */
	public void handleNotification(Notification notification, Object object)
	{
		InvokerLocator[] locators;
 		
 		if(notification instanceof NetworkNotification)
		{
			NetworkNotification netNotification = (NetworkNotification)notification;
			
			if(netNotification.getType().equals(NetworkNotification.SERVER_ADDED))
			{
				locators = netNotification.getLocator();
				
				for(InvokerLocator i: locators)
				{
					try
					{
						makeRequest(i.getLocatorURI());
					}
					catch(Throwable t)
					{
						t.printStackTrace();
					}
				}
			}
		}
	}
 	
	/* Make a request to anything that could potentially be a Blue Host */
 	private void makeRequest(String locatorURI) throws Throwable
 	{
 		locator = new InvokerLocator(locatorURI);
 		Client client = new Client(locator);
 		client.setSubsystem("register");
 		
 		/* Set some properties */
 		HashMap<String,String> properties = new HashMap<String,String>();
 		properties.put(MessageProperties.HOST_NAME,hostname);
 		properties.put(MessageProperties.IP_ADDRESS,ipAddress);
 		properties.put(MessageProperties.AGENT_TYPE,agentType);
 		
 		client.connect();
 		analyseResponse(client.invoke(MessageTypes.REQUEST_TO_REGISTER,properties)); 		 		
 		 		
 	}
 	
 	/* Analyse any response that we have received. */
 	private void analyseResponse(Object response)
 	{
 		 if(response instanceof BlueMessage)
 		 {
 			BlueMessage message = (BlueMessage)response;
 			
 			if(message.getMessageType().equals(MessageTypes.REGISTRATION_RESPONSE))
 			{
 				onLine = true;
 			}
 		 }
 	}	
}