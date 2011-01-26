package org.blue.star.registry;

/**
 * This class is used to control the execution of the BlueDynamicRegistry when being run
 * in .jar fashion.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class RegistryControl 
{
    /** Our Registry Instance */
    private static BlueDynamicRegistry registry;
			
    public static void main(String[] args)
    {
    	registry = new BlueDynamicRegistry();
    	registry.setCommandLineArgs(args);
	    
    	registry.start();
        
		while(!registry.isShutdown())
		{
		    try
		    {
		    	Thread.sleep(1000);
		    }
		    catch(InterruptedException e)
		    {
			    
		    }
		}
    }
}