package org.blue.star.registry.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.registry.exceptions.RegistryCacheException;

/**
 * This class is an implementation of the HostCache interface associated with the BlueRegistry.
 * It is used to store details of hosts that have already been seen by the Registry.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class BlueRegistryHostCache implements HostCache
{
	/** Store for details of all hosts & agents coming to the registry */
    	private Map<String,RegistryCacheData> hostStore = new HashMap<String,RegistryCacheData>();
	
	/** Location to persist some state to */
	private String persistLocation;
	
	/** Logging variables */
	private static Logger logger = LogManager.getLogger("org.blue.star.registry.cache.BlueRegistryHostCache");
	private String cn = "org.blue.star.registry.cache.BlueRegistryHostCache";
	
	/* getPersistLocation() method */
	public String getPersistLocation() 
	{
		return this.persistLocation;
	}

	/* setPersistLocation() method */
	public void setPersistLocation(String location)
	{
		if(location.endsWith("/") || location.endsWith("\\"))
		{
		    location = location.substring(0,location.length() -1);
		}
		
	    	this.persistLocation = location;
	}
	
	/* getHostAgentCount() method */
	public int getHostAgentCount(String ipAddress,String hostname) throws RegistryCacheException
	{
		logger.trace("Entering " + cn + ".getHostAgentCount");
		
		if(ipAddress == null || hostname == null)
		{
			throw new RegistryCacheException("IPAddress or Hostname was null");
		}
		
		checkHostIsInStore(ipAddress,hostname);
		
		return hostStore.get(ipAddress + ":" + hostname).getAgentCount();
	}
	
	/* loadSeenHostList() method */
	public void loadSeenHostList(String location)
	{
		this.persistLocation = location;
		this.loadSeenHostList();
	}

	/* loadSeenHostList() method */
	public void loadSeenHostList()
	{
		if(this.persistLocation == null)
		{
		    return;
		}
	    
	    	BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new FileReader(this.persistLocation + System.getProperty("file.separator") + "seenhosts.dat"));
			String line = reader.readLine();
			
			while(line != null)
			{
				String[] bits = line.split(":");
				
				if(bits.length != 3)
				{
				    continue;
				}
				
				String ipAddress = bits[0];
				String hostname = bits[1];
										
				String[] bits2 = bits[2].split(",");
					
				RegistryCacheData data = new RegistryCacheData(ipAddress,hostname);
															
				for(String s: bits2)
				{
					data.addAgentToHost(s);
				}
					
				data.setPersistent(true);
				hostStore.put(ipAddress + ":" + hostname,data);
			}
			
			line = reader.readLine();
			
		}
		catch(IOException e)
		{
			logger.debug(cn + ".loadSeenHostList() - Error Loading seen Host list from Disk");
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
				logger.debug(cn + ".loadSeenHostList() - Unable to close File Handle");
			}
		}
	}
	
	/* persistSeenHostList() */
	public void persistSeenHostList()
	{
		BufferedWriter out = null;
		
		try
		{
			out = new BufferedWriter(new FileWriter(this.persistLocation + "/seenhosts.dat"));
			
			for(Iterator<String> i = this.hostStore.keySet().iterator();i.hasNext();)
			{
				
			    	RegistryCacheData d = hostStore.get(i.next());
				
			    	if(!d.isPersistent())
			    	{
			    	    continue;
			    	}

			    	out.write(d.getIpAddress() + ":" + d.getHostName() + ":"); 
				
				String agents = "";
				
				for(String s: d.getAllAgentsOnHost())
				{
				    agents = agents + s + ",";
				}
				
				if(agents.endsWith(","))
				{
				    agents = agents.substring(0,agents.length()-1);
				}
				 
				out.write(agents);
			}
			
			out.flush();
		}
		catch(IOException e)
		{
			logger.debug(cn + ".persistSeenHostList() - Unable to Persist Seen Host List");
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
				}
			}
			catch(IOException e)
			{
				logger.debug(cn + ".persistSeenHostList() - Unable to close File Handle.");
			}
		}
	}

	/* removeHost() method */
	public synchronized void removeHost(String IpAddress, String hostname) throws RegistryCacheException
	{
		logger.trace("Entering " + cn + ".removeHost");
		
		if(IpAddress == null || hostname == null)
		{
		    throw new RegistryCacheException("Cannot remove Host as IP Address or Hostname was null");
		}
		
		checkHostIsInStore(IpAddress,hostname);
		
		if(getHostAgentCount(IpAddress,hostname) > 0)
		{
		    throw new RegistryCacheException("Host '" + hostname +  " (" + IpAddress + ")', still has '" + getHostAgentCount(IpAddress,hostname) + "' agent(s) running. These must be removed first");
		}
		    
		hostStore.remove(IpAddress + ":" + hostname);
	}
	
	/* removeAgentFromHost() method */
	public synchronized void removeAgentFromHost(String ipAddress, String hostname, String agentType) throws RegistryCacheException
	{
		logger.trace("Entering " + cn + ".removeHost");
		
		if(ipAddress == null || hostname == null || agentType == null)
		{
			logger.debug(cn + ".removeHost() - Required variables are not set");
			return;
		}
		
		checkHostIsInStore(ipAddress,hostname);
		
		RegistryCacheData data  = hostStore.get(ipAddress + ":" + hostname);
		
		if(!data.hostHasAgent(agentType))
		{
		    logger.debug(cn + ".removeAgentFromHost() - Host is not running agent");
		    throw new RegistryCacheException("Host '" + hostname + " (" + ipAddress + "), is not running agent of type '" + agentType + "'");
		}
		
		data.removeAgentFromHost(agentType);
		
		hostStore.put(ipAddress + ":" + hostname,data);
	}

	/* storeHostDetails() method */
	public synchronized void storeHostDetails(String IpAddress, String hostname, String agentType, boolean isPersistent) throws RegistryCacheException
	{
		logger.trace("Entering " + cn + ".storeHostDetails");
		
		if(IpAddress == null || hostname == null || agentType == null)
		{
			logger.debug(cn + ".storeHostDetails() - Required Variables were not set");
			throw new RegistryCacheException("Required Variables to Store Host were not Set!");
		}
		
		/* If we have already seen this host, simply add the new agent to the list of available ones */
		if(this.hasSeenHost(IpAddress, hostname))
		{
			this.addAgentToHost(IpAddress,hostname,agentType);
			return;
		}
		
		RegistryCacheData data = new RegistryCacheData(IpAddress,hostname);
		data.addAgentToHost(agentType);
		data.setPersistent(isPersistent);
		
		this.hostStore.put(IpAddress + ":" + hostname,data);
	}

	/* hasSeenHost() method */
	public boolean hasSeenHost(String IpAddress, String hostname)
	{
		if(IpAddress == null || hostname == null || !this.hostStore.containsKey(IpAddress + ":" + hostname))
		{
			return false;
		}
		
		return true;
	}

	/* isHostRunningAgent() method */
	public boolean isHostRunningAgent(String IpAddress, String hostname, String agentType)
	{
		if(IpAddress == null || hostname == null || agentType == null || !this.hostStore.containsKey(IpAddress + ":" + hostname))
		{
			return false;
		}
		
		return hostStore.get(IpAddress + ":" + hostname).hostHasAgent(agentType);
	}
	
	/* isHostPersistent() method */
	public boolean isHostPersistent(String IpAddress, String hostname)
	{
		if(IpAddress == null || hostname == null || !this.hostStore.containsKey(IpAddress + ":" + hostname))
		{
			return false;
		}
		
		return this.hostStore.get(IpAddress + ":" + hostname).isPersistent();
	}
	
	/**
	 * This method is used to add an agent type to a remote host.
	 * @param ipAddress - The IpAddress of the remote host.
	 * @param hostname - The hostname of the remote host.
	 * @param agentType - The agent type to add to this host.
	 */
	public synchronized void addAgentToHost(String ipAddress,String hostname,String agentType)
	{
		if(ipAddress == null || hostname == null || agentType == null)
			return;
		
		if(!this.isHostRunningAgent(ipAddress, hostname, agentType))
		{
			this.hostStore.get(ipAddress + ":" + hostname).addAgentToHost(agentType);
		}
	}
	
	/**
	 * A simple utility method that checks to see if the host store has a reference to the host
	 * identified by the given ipaddress and hostname.
	 * @param ipAddress - the ipaddress of the host.
	 * @param hostname - the hostname of the host.
	 * @throws RegistryCacheException - thrown if the host is not in the store.
	 */
	private void checkHostIsInStore(String ipAddress,String hostname) throws RegistryCacheException
	{
	    if(!hostStore.containsKey(ipAddress + ":" + hostname))
	    {
		throw new RegistryCacheException("Host '" + hostname + "' with Ip Address '" + ipAddress + "' is not known to cache");
	    }
	}
}