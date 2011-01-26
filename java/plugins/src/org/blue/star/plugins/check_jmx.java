package org.blue.star.plugins;
/*****************************************************************************
*
* Blue Star, a Java Port of .
* Last Modified : 23/01/07
*
* License:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
*****************************************************************************/
/*
 * Author: Rob.blake@arjuna.com
 * 
 * Version 0.1 of the check_jmx plugin. This plugin can be used to check the value of any attribute of
 * any mbean on any mbean server that is reachable via RMI from your Blue Server. You can also perform other
 * operations such as check the count of the number of Mbeans on the server, check for the presence of a 
 * particular domain.
 * 
 * In forthcoming versions:
 *  -extended into generic app servers (This is currently JBoss Only)
 *  -check multiple attribute values
 *  -check uptime of your app server i.e. user specifies time in secs|mins|hrs|days and check uptime against this.
 *  -authentication
 */

import java.io.IOException;
import java.util.Properties;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class check_jmx extends check_base{
	
/* Versioning info */
   protected String getAuthor() { return "Rob Blake rob.blake@arjuna.com"; }
   protected String getCopyright() { return "Arjuna 2007"; }
   protected String getDescription() { 
        return "\nUse this plugin to check various MBean values on a specifed MBean Server.\n";
   }
   protected String getNotes() {
      return "\nThis plugin uses JMX over RMI to connect to the MBean Server on the Specified Host.\n" +
      "It can be used in various ways, allowing you to check a property of a specified MBean,\n" +
      "set warning and critical thresholds should the aforementioned property be an integer,\n" + 
      "check for the existence of a specific domain, count of the number of MBeans managed by the\n" + 
      "specified MBean Server, or count the number of MBeans matched by the MBean name specified.\n";
   }
    	
	/* Instance Variables */
	private Properties p;
	private String hostname;
	private int port = 1099;
	private String jndiAdaptorName = "jmx/rmi/RMIAdaptor";
	private String mbeanName;
	private String attribute;
	private long criticalThreshold = -1;
	private long warningThreshold = -1;
	private boolean compareThresholds;
	private String expectString;
	private String domain;
	private long count = -1;
	
	private MBeanServerConnection server;
	private ObjectName objectName;
	
	private String checkMessage;
	private int checkState;
	
	/* Main */
	public static void main(String[] args)
	{
		new check_jmx().process_request(args);
	}
	
	/* Some information about us */
	public void init_command()
	{
	}
	
	/* Set up our legal arguments */
	public void add_command_arguments(Options options)
	{
		Option H = new Option("H","Host",true,"The Hostname that your MBean Server resides on.");
		H.setArgName("MBeanServer Host");
		options.addOption(H);
		
		Option P = new Option("P","Port",true,"The Port your MBean Server listens on (default:1099).");
		P.setArgName("Port");
		options.addOption(P);
		
		Option J = new Option("J","JNDI Name",true,"The JNDI name of your RMI Adaptor (default:jmx/rmi/RMIAdaptor)");
		J.setArgName("JNDI Name");
		options.addOption(J);
		
		Option M = new Option("M","MBean",true,"The name of the MBean to lookup.");
		M.setArgName("MBean name");
		options.addOption(M);
		
		Option A = new Option("A","Attribute",true,"The Attribute of the specified MBean you wish to check.");
		A.setArgName("Attribute");
		options.addOption(A);
		
		Option e = new Option("e","Expect",true,"The expected value of the named MBean attribute.");
		e.setArgName("Expect");
		options.addOption(e);
		
		Option D = new Option("D","Domain",true,"A domain to check for on the specified MBean Server.");
		D.setArgName("Domain");
		options.addOption(D);
		
		Option w = new Option("w","Warning",true,"An Integer value for the specified attribute, that will cause a warning exit.");
		w.setArgName("Warning");
		options.addOption(w);
		
		Option c = new Option("c","Critical",true,"An Integer value for the specified attribute that will cause a critical exit.");
		c.setArgName("Critical");
		options.addOption(c);
		
		Option C = new Option("C","Count",true,"An Integer value for checking the number of MBeans within the MBean Server");
		C.setArgName("Count");
		options.addOption(C);
		
		// To be Considered....
		/*Option U = new Option("U","Uptime",true,"Specify the uptime of your server. Valid options = <int>(s|m|h|d)");
		U.setArgName("Uptime");
		options.addOption(U);*/
		
	}
	
	
	/* Process our expected command line arguments */
	public void process_command_option(Option o)
       throws IllegalArgumentException
	{
		String argValue = o.getValue();
		
		switch(o.getId())
		{
			case 'H':
				if(!netutils.is_host(argValue))
					throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Hostname> (%H) must be a valid Host\n",argValue ) );
				else
					hostname = argValue.trim();
				break;
			
			case 'P':
				if(!utils.is_intnonneg(argValue))
                   throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Port> (%P) must be a positive Integer\n", argValue ));
				else
					port = Integer.valueOf(argValue);
				break;
				
			case 'J':
					jndiAdaptorName = argValue.trim();
					break;
			
			case 'M':
					mbeanName = argValue.trim();
					break;
			
			case 'A':
					attribute = argValue.trim();
					break;
			
			case 'e':
					expectString = argValue.trim();
					break;
			case 'D':
					domain = argValue.trim();
					break;
			
			case 'w':
					if(!utils.is_intnonneg(argValue))
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<warning> (%w) must be a positive Integer\n",argValue ) );
					else
					{
						warningThreshold = Long.valueOf(argValue);
						compareThresholds = true;
					}
					break;
			
			case 'c':
					if(!utils.is_intnonneg(argValue))
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<critical> (%c) must be a positive Integer\n",argValue )) ;
					else
					{
						criticalThreshold = Long.valueOf(argValue);
						compareThresholds =true;
					}						
					break;
			
			case 'C':
					if(!utils.is_intnonneg(argValue))
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Count> (%C) must be a positive Integer" ,argValue ));
					else
						count = Integer.valueOf(argValue);
					break;			
		}
		
	}

	/* Process any other command line arguments */
	public void process_command_arguments(String[] argv)
	   throws IllegalArgumentException {
	}
	
	/* Validate the commands given by the user */
	public void validate_command_arguments()
	   throws IllegalArgumentException {
		if(hostname == null)
		{
           throw new IllegalArgumentException( "You must specify a Hostname on which the MBean Server runs");
		}
		
		if(mbeanName == null && domain == null && count == -1)
		{
           throw new IllegalArgumentException( "You must specify at least a MBean to check for, a Domain to check for or a count of MBeans to check for");
		}
		
		if((warningThreshold > -1 && criticalThreshold == -1) || (warningThreshold == -1 && criticalThreshold > -1))
		{
           throw new IllegalArgumentException( "You must specify both a Warning and Critical Threshold for numeric checks");
		}
		
		if((warningThreshold == -1 || criticalThreshold == -1) && count > 0)
		{
           throw new IllegalArgumentException( "You must specify both a warning and critical Threshold when checking MBean count");
		}		
	
	}
	
	/* Execute our check */
	public boolean execute_check()
	{
		/* Our Jboss Specific Properties */
		p = new Properties();
		p.put(InitialContext.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		p.put(InitialContext.PROVIDER_URL,"jnp://" + hostname + ":" + port);
		p.put(InitialContext.URL_PKG_PREFIXES,"org.jboss.naming.interfaces");
				
		/* Attempt our connection */
		try
		{
			server = (MBeanServerConnection)new InitialContext(p).lookup(jndiAdaptorName);
		}
		catch(NamingException e)
		{
			checkMessage = "Critical - Unable to connect to MBean Server at jnp://" + hostname + ":" + port;
			checkState = common_h.STATE_CRITICAL;
			return false;
		}
		
		/* Has the user specified a bean to check ? */
		if(mbeanName != null)
		{
			try
			{
				objectName = ObjectName.getInstance(mbeanName);
			}
			catch(MalformedObjectNameException e)
			{
				checkMessage = "Critical - Malformed Object name for MBean instance";
				checkState = common_h.STATE_CRITICAL;
				return false;
			}
			
			/* Are we testing a specific attribute of that bean */
			if(attribute != null)
			{
				try
				{
					Object o = server.getAttribute(objectName,attribute);
					
					/* Check for expected value? */
					if(expectString != null)
						if(!expectString.equals(o.toString()))
						{
							checkMessage = "Warning - Value of Attribute does not match expected value.";
							checkState = common_h.STATE_WARNING;
							return false;
						}
					
					/* Are we comparing the value to warn/crit thresholds */
					if(compareThresholds)
						if(!compareThresholds(o.toString()))
							return false;
				}
				catch(InstanceNotFoundException e)
				{
					checkMessage = "Critical - No Instance of MBean " +  mbeanName + " found on MBean Server";
					checkState = common_h.STATE_CRITICAL;
					return false;
				}
				catch(AttributeNotFoundException e)
				{
					checkMessage = "Critical - Attribute " + attribute + " not found for MBean " + mbeanName;
					checkState = common_h.STATE_CRITICAL;
					return false;
				}
				catch(Exception e)
				{
					checkMessage = "Critical - Error retrieving Attribute value of " + attribute + " for Mbean " + mbeanName;
					checkState = common_h.STATE_CRITICAL;
					return false;
				}
			}
			
		}
				
		/* Check to see if the specified domain exists */
		if(domain != null)
			if(!checkDomains())
				return false;
		
		/* Perform MBean count checks */
		if(count > -1)
			if(!checkCount())
				return false;
		
		
		/* Mbean checking takes precedence in our reporting */
		if(mbeanName != null)
		{
			checkMessage = "OK - MBean " + mbeanName + " alive on MBean Server jnp://" + hostname + ":" + port;
			checkState = common_h.STATE_OK;
		}
		
		return true;
	}
	
	public String check_message()
	{
		return checkMessage;
	}

	public int check_state()
	{
		return checkState;
	}

	/* Check to see if the specified domain exists */
	private boolean checkDomains()
	{
		try
		{
			for(String s : server.getDomains())
				if(s.equalsIgnoreCase(domain))
				{	
					checkMessage = "OK - Domain " + domain + " found on MBeanServer jnp:// " + hostname + ":" + port;
					checkState = common_h.OK;
					return true;
				}
		}
		catch(IOException e)
		{
			checkMessage = "Warning - Unable to retrieve list of domains from MBean Server";
			checkState = common_h.STATE_WARNING;
			return false;
		}
		
		checkMessage = "Warning - Domain " + domain + " not found on MBean Server";
		checkState = common_h.STATE_WARNING;
		return false;
	}

	/* Compare the value of a given attribute to our thresholds */
	private boolean compareThresholds(String attributeValue)
	{
		long attributeNum;
		
		try
		{
			attributeNum = Long.parseLong(attributeValue);
		}
		catch(Exception e)
		{
			checkMessage = "Warning - Value of Attribute " + attribute + " is not a numeric value.";
			checkState = common_h.STATE_WARNING;
			return false;
		}
		
		if(attributeNum < warningThreshold)
			return true;
		else if(attributeNum >=warningThreshold && attributeNum < criticalThreshold)
		{
			checkMessage = "Warning - Attribute " + attribute + " of MBean " + mbeanName + ": value = " + attributeNum;
			checkState = common_h.STATE_WARNING;
			return false;
		}
		else if(attributeNum >= criticalThreshold)
		{
			checkMessage  ="Critical - Attribute " + attribute + "of MBean" + mbeanName + ": value = " + attributeNum;
			checkState = common_h.STATE_CRITICAL;
			return false;
		}
		
		return false;
	}

	/* Check the number of MBeans on the Server */
	private boolean checkCount()
	{
		try
		{
			int beanCount = server.getMBeanCount();
			
			if(beanCount < warningThreshold)
			{
				if(mbeanName == null)
				{
					checkMessage = "OK - MBean count = " + beanCount;
					checkState = common_h.STATE_OK;
				}
				return true;
			}
			else if(beanCount >= warningThreshold && beanCount < criticalThreshold)
			{
				checkMessage = "Warning - MBean count = " + beanCount;
				checkState = common_h.STATE_WARNING;
				return false;
			}
			else if(beanCount >= criticalThreshold)
			{
				checkMessage = "Critical - MBean count = " + beanCount;
				checkState = common_h.STATE_CRITICAL;
				return false;
			}
		}
		catch(IOException e)
		{
			checkMessage = "Critical - Unable to retrieve MBean count from MBean Server";
			checkState = common_h.STATE_CRITICAL;
			return false;
		}
		return false;
	}
	
}
