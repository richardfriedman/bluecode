package org.blue.star.registry.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 * 
 * <p>The role of this class is to store details about registrations from hosts and the
 * agents that run on these hosts. This class stores information such as the number of 
 * agents running on a given host, the name and Ip Address of a host and whether or not
 * this host should be considered a persistent registration.</p>
 *
 */
public class RegistryCacheData 
{
    /** The time this host was last seen by the registry since the registry started */
    private long registrationTime;
    
    /** The hostname of the remote host */
    private String hostname;
    
    /** The ipAddress of the remote host */
    private String ipAddress;
    
    /** Should this host be considered a persistent registration? */
    private boolean isPersistent = false;
    
    private List<String> agentTypes;
    
    public RegistryCacheData(String IpAddress,String hostname)
    {
	registrationTime = System.currentTimeMillis();
	agentTypes = new ArrayList<String>();
	this.ipAddress = IpAddress;
	this.hostname = hostname;
    }
    
    public long getRegistrationTime()
    {
	return this.registrationTime;
    }
    
    public String getHostName()
    {
	return this.hostname;
    }
    
    public String getIpAddress()
    {
	return this.ipAddress;
    }
    
    public boolean isPersistent()
    {
	return this.isPersistent;
    }
    
    public void setPersistent(boolean persistent)
    {
	this.isPersistent = persistent;
    }
    
    public void addAgentToHost(String agentType)
    {
	if(agentType == null)
	{
	    return;
	}
	
	if(!hostHasAgent(agentType))
	{
	    agentTypes.add(agentType);
	}
    }
    
    public void removeAgentFromHost(String agentType)
    {
	if(agentType == null || !hostHasAgent(agentType))
	{
	    return;
	}
	
	agentTypes.remove(agentType);
    }
    
    public int getAgentCount()
    {
	return agentTypes.size();
    }
    
    public List<String> getAllAgentsOnHost()
    {
	return this.agentTypes;
    }
    
    public boolean hostHasAgent(String agentType)
    {
	if(agentType == null || agentTypes.size() == 0)
	{
	    return false;
	}
	
	for(String s: agentTypes)
	{
	    if(s.equals(agentType))
	    {
		return true;
	    }
	}
	
	return false;
    }
}