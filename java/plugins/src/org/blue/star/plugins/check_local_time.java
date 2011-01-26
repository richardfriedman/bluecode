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

package org.blue.star.plugins;

import java.net.InetAddress;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Impl;
import org.apache.commons.net.ntp.TimeInfo;

public class check_local_time extends check_base
{
   protected String getAuthor() { return "Rob Blake rob.blake@arjuna.com"; }
   protected String getCopyright() { return "Arjuna 2007"; }
   protected String getDescription() { 
        return "\nUse this plugin to check your local clock against that of a specified NTP server.\n";
   }
   protected String getNotes() {
      return "\nThis plugin uses NTP to gather time information from a NTP server. " +
             "It then compares the gathered time to the local system clock and calculates " +
             "any discrepancies between the two. You can define both warning and critical thresholds " +
             "representative of difference between local clock and ntp clock in seconds.\n\n";
   }
	
	private String hostname;
	private int port = 123;
	private int timeout = utils_h.timeout_interval;
	private int ntpVersion = NtpV3Impl.VERSION_3;
	private int checkCount = 4;
	private long[] offsets;
	private long average;
	private boolean verbose = false;
	private String offsetWarning = "+";
	
	/* Default warning threshold to a minute, critical to two */
	private long warningThreshold = 60000;
	private long criticalThreshold = 120000;
	private String check_message = "";
	private int check_state = common_h.STATE_UNKNOWN;
	
	/* Main method to ick off the check */
	public static void main(String[] args)
	{
		new check_local_time().process_request(args);
	}
	
	/* Initialise the plugin with its own information */
	public void init_command() {
	}
	
	/* Add all applicable options for this plugin */
	public void add_command_arguments(Options options)
	{
		Option w = new Option("w","Warning",true,"Warning Threshold in Seconds.");
		w.setArgName("Warning");
		options.addOption(w);
		Option c = new Option("c","Critical",true,"Critical Threshold in Seconds");
		c.setArgName("Critical");
		options.addOption(c);
		Option P = new Option("P","Port",true,"Port of remote NTP Service");
		P.setArgName("Port");
		options.addOption(P);
		Option n = new Option("n","NTP Version",false,"Use NTP version of choice (3 or 4)");
		n.setArgName("NTP Version");
		options.addOption(n);
		Option C = new Option("C","Check Count",true,"The number of NTP checks to perform (default=4)");
		C.setArgName("Check Count");
		options.addOption(C);
		Option V = new Option("V","Version",false,"Print Version Information");
		V.setArgName("Version");
		options.addOption(V);
		Option h = new Option("h","Help",false,"Display Help Information for this Plugin");
		h.setArgName("Help");
		options.addOption(h);
	}
	
	/* Process the command line options */
	public void process_command_option(Option o)
	   throws IllegalArgumentException {
		String argValue = o.getValue();
		int version;
		
		switch(o.getId())
		{
			case 'H':
				hostname = argValue.trim();
				break;
			case 'w':
				if(utils.is_intnonneg(argValue))
					warningThreshold = Long.valueOf(argValue)*1000;
				else
					throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Warning> (%w) must be a non-negative number\n", argValue) );
				break;
				
			case 'c':
				if(utils.is_intnonneg(argValue))
					criticalThreshold = Long.valueOf(argValue)*1000;
				else
                    throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Critical> (%c) must be a non-negative number\n", argValue) );
				break;
				
			case 'P':
				if(utils.is_intnonneg(argValue))
					port = Integer.valueOf(argValue);
				else
                   throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Port> (%P) must be a non-negative number\n", argValue) );
				break;
			case 'n':
				if(utils.is_intnonneg(argValue))
				{
					version = Integer.valueOf(argValue);
					
					if(version == 3)
						ntpVersion = NtpV3Impl.VERSION_3;
					else if(version == 4)
						ntpVersion = NtpV3Impl.VERSION_4;
					else
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<NTP Version> (%n) must be either V3 or V4\n", argValue) );
				}
				else
                   throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<NTP Version> (%n) must be a non-negative number\n", argValue) );
				break;
		}
	}
	
	/* Validate that all required combinations of arguments are set properly */	
	public void validate_command_arguments()
	   throws IllegalArgumentException {
		if(hostname == null)
		{
			throw new IllegalArgumentException("You must specify a Hostname for the remote NTP service" );
		}
		
		if(!netutils.is_host(hostname))
		{
			throw new IllegalArgumentException("You must specify a valid Host Address for the remote NTP service" );
		}
				
	}

	/* Process any other command line arguments */
	public void process_command_arguments(String[] argv)
	   throws IllegalArgumentException {
	}
	
	/* Execute the local time check using the user determined parameters */
	public boolean execute_check()
	{
		
		/* Declare variables */
		NTPUDPClient ntpClient = new NTPUDPClient();
		TimeInfo time;
		InetAddress server;
		offsets = new long[checkCount];
		
		/* Check for verbosity */
		if(super.verbose > 0)
			verbose = true;
		
		/* Configure client to meet our requirements */
		ntpClient.setDefaultTimeout(timeout);
		ntpClient.setVersion(ntpVersion);
		
		try
		{
			if(verbose)
			{
				System.out.println("Using NTP Server: " + hostname);
				System.out.println("Using NTP Port: " + port);
				System.out.println("Using NTP Version: " + ntpVersion);
			}
			
			server = InetAddress.getByName(hostname);
			ntpClient.open();
			
			if(verbose) System.out.println("Beginning total of " + checkCount + " checks.");
			
			for(int i =0;i<checkCount;i++)
			{
				if(verbose) System.out.println("Taking time reading number " + i);
			
				time = ntpClient.getTime(server,port);
				
				if(time == null)
				{
					/* State unknown if we can't connect to NTP server, our local time could be fine */
					check_state = common_h.STATE_UNKNOWN;
					check_message = "State Unknown - Unable to connect to NTP Server!";
					return true;
				}
				time.computeDetails();
						
				offsets[i] = time.getOffset() - time.getDelay();
				
				/* Small sleep so that we are at least getting some variety in our readings */  
				Thread.sleep(200);
			}
			
			if(verbose) System.out.println("Calculating & storing time averages.");
			/* Calculate and store average offset */
			long total =0;
						
			for(int i =0;i<offsets.length;i++)
				total += offsets[i];
			
			average = total/offsets.length;
		}
		catch(Exception e)
		{
			check_state = common_h.STATE_CRITICAL;
			check_message = e.getMessage();
		}
		finally
		{
			try
			{
				ntpClient.close();
			}
			catch(Exception e)
			{}
		}
		return true;
	}
	
	/* Return the check message String */
	public String check_message()
	{
		
		if(average >=0)
			offsetWarning = "-";
		
		/* Negative clock offset */
		if(average >=0)
		{
			if(average < warningThreshold)
				check_message = "OK - Clock Offset = " + offsetWarning + average/1000 + " secs - OK";
			else if(average >=warningThreshold && average < criticalThreshold)
				check_message = "Warning - Clock Offset = " + average/1000 + offsetWarning + " secs - Warning";
			else if(average >=criticalThreshold)
				check_message = "Critical - Clock Offset = " + average/1000 + offsetWarning + " secs - Critical";
		}
		else
		{
			/*Positive clock offset */
			if((warningThreshold + average) > 0)
				check_message = "OK - Clock Offset = " + offsetWarning + convertToPositive(average/1000) + " secs";
			else if((warningThreshold + average) <=0 && (warningThreshold + average) > (0 - criticalThreshold))
				check_message = "Warning - Clock Offset = " + offsetWarning + convertToPositive(average/1000) + " secs";
			else if(average <= (0 - criticalThreshold))
				check_message = "Critical - Clock Offset = " + offsetWarning + convertToPositive(average/1000) + " secs";
		}
			
		return check_message;
	}

	/* Return the check state */
	public int check_state()
	{
		/* Negative Clock Offset */
		if(average >0)
		{
			if(average < warningThreshold)
				check_state = common_h.STATE_OK;
			else if(average >= warningThreshold && average < criticalThreshold)
				check_state = common_h.STATE_WARNING;
			else if(average >=criticalThreshold)
				check_state = common_h.STATE_CRITICAL;
		}
		else
		{
			/* Positive Clock Offset */
			if((warningThreshold + average) > 0)
				check_state = common_h.STATE_OK;
			else if((warningThreshold + average) <=0 && (warningThreshold + average) > (0 - criticalThreshold))
				check_state = common_h.STATE_WARNING;
			else if(average <= (0 - criticalThreshold))
				check_state = common_h.STATE_CRITICAL;
		}
		
		return check_state;
	}

	private long convertToPositive(long negLong)
	{
		negLong = negLong - (negLong*2);
		return negLong;
	}
}
