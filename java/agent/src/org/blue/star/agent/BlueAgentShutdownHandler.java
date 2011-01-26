package org.blue.star.agent;

/**
 * This class is used as a shutdown handler for the Blue Agent. It is registered with the runtime
 * at agent start-up and is executed upon receiving a shutdown call for the jvm in which the agent
 * is running. It's main purpose is to instruct the registry that this agent has gone offline. 
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */ 
public class BlueAgentShutdownHandler extends Thread 
{
    /** The Agent instance that we are to shutdown */
    private Agent b;
    
    public BlueAgentShutdownHandler(Agent agent)
    {
    	this.b = agent;
    }
    
    /** Threaded method that is run when jvm is exited */
    public void run()
    {
    	b.shutdown();
    }
}