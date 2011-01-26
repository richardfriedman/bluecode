package org.blue.star.registry.cache;

import java.io.File;

import org.blue.star.registry.exceptions.RegistryCacheException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 * 
 * <p>Test case for HostCache.</p>
 *
 */
public class HostCacheTest
{
    private HostCache cache;
    private String agentType = "Generic Agent";
    private String ipAddress = "127.0.0.1";
    private String hostname = "My Host";
    
    @Before
    public void createCache()
    {
	cache = new BlueRegistryHostCache();
    }
    
    @Test(expected=RegistryCacheException.class)
    public void testAddOfNullHost() throws RegistryCacheException
    {
	cache.storeHostDetails(null,null,null,false);
    }
    
    @Test
    public void testAddOfPersistentHost() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress,hostname,agentType,true);
	assertTrue("Error, Host should be considered as persistent",cache.isHostPersistent(ipAddress,hostname));
    }
    
    /*
     * Currently it is the role of the agent to maintain a record of who has sent
     */
    @Test
    public void testMultipleSameAgentsOnHost() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress, hostname, agentType,false);
	cache.storeHostDetails(ipAddress, hostname, agentType,false);
	
	assertTrue("Agent Count is not correct",cache.getHostAgentCount(ipAddress, hostname)== 1);
	
    }
    
    @Test
    public void testRemoveOfAgent() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress,hostname,agentType,false);
	cache.removeAgentFromHost(ipAddress, hostname, agentType);
	
	assertTrue("Agent Count is not correct after removal",cache.getHostAgentCount(ipAddress, hostname) == 0);
    }
    
    @Test
    public void testRemoveOfOneOfMultipleAgents() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress,hostname,agentType,false);
	cache.storeHostDetails(ipAddress,hostname,"A New Agent",false);
	cache.removeAgentFromHost(ipAddress, hostname, agentType);
	
	assertTrue("Agent Count is not correct after removal",cache.getHostAgentCount(ipAddress, hostname) ==1);
    }
    
    @Test
    public void testRemovalOfAgentFromDifferentHost() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress,hostname,agentType,false);
	cache.storeHostDetails("192.168.0.1",hostname,agentType,false);
	
	cache.removeAgentFromHost("192.168.0.1",hostname,agentType);
	
	assertTrue("Host should still be running one agent of type '" + agentType + "'",cache.getHostAgentCount(ipAddress, hostname) == 1);
    }
    
    @Test(expected=RegistryCacheException.class)
    public void testRemovalOfUnknownHost() throws RegistryCacheException
    {
	cache.removeHost("foo","bar");
    }
    
    
    @Test(expected=RegistryCacheException.class)
    public void testRemoveHostWithActiveAgents() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress,hostname,agentType,false);
	
	cache.removeHost(ipAddress, hostname);
    }
    
    @Test
    public void testPersistSeenHostList() throws RegistryCacheException
    {
	cache.storeHostDetails(ipAddress, hostname, agentType,false);
	cache.storeHostDetails(ipAddress, hostname,"new Agent",false);
	cache.setPersistLocation("java/registry/test/org/blue/star/registry/cache");
	cache.persistSeenHostList();
	
	assertTrue("Persistent File Does Not Exist!",new File("java/registry/test/org/blue/star/registry/cache/seenHosts.dat").exists());
	
	//new File("java/registry/test/org/blue/star/registry/cache/seenHosts.dat").delete();
    }
    
    @After
    public void tearDown()
    {
	cache = null;
    }
}
