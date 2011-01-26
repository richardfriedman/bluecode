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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class check_ftp extends check_base
{
   protected String getAuthor() { return "Rob Blake rob.blake@arjuna.com"; }
   protected String getCopyright() { return "Arjuna 2007"; }
   protected String getDescription() { 
      return "\nUse this plugin to check FTP connections to a remote host.\n";

   }
   protected String getNotes() {
        return "\nThis plugin uses FTP to probe the FTP service on a specified host. " +
               "It has support for authenticated login, allowing specification of username " +
               "and password. This plugin returns OK if login and file retrieval was successful, " +
               "warning if the directory/file could not be found and critical if no connection " + 
               "could be made.";
   }

	private String hostname;
	private int port = 21;
	private int timeout = utils_h.timeout_interval;
	private String username;
	private String password;
	private String directory;
	private String file;
	private boolean passive = false;
	private boolean verbose = false;
	
	private String check_message = "";
	private int check_state = common_h.STATE_UNKNOWN;
	
	public static void main(String[] args)
	{
		new check_ftp().process_request(args);
	}
	
	public void init_command() {
	}
	
	public void add_command_arguments(Options options)
	{
		Option f = new Option("f","File",true,"File to retrieve");
		f.setArgName("File");
		options.addOption(f);
		Option d = new Option("d","Directory",true,"Directory Location of File");
		d.setArgName("Directory");
		options.addOption(d);
		Option u = new Option("u","Username",true,"Username required to connect to remote FTP");
		u.setArgName("Username");
		options.addOption(u);
		Option p = new Option("p","Password",true,"Password required to connect to remote FTP");
		p.setArgName("Password");
		options.addOption(p);
		Option P = new Option("P","Port",true,"Port of remote FTP Service");
		P.setArgName("Port");
		options.addOption(P);
		Option m = new Option("m","Passive Mode",false,"Utilise PASV mode for FTP session");
		m.setArgName("Passive Mode");
		options.addOption(m);		
		Option V = new Option("V","Version",false,"Print Version Information");
		V.setArgName("Version");
		options.addOption(V);
		Option h = new Option("h","Help",false,"Display Help Information for this Plugin");
		h.setArgName("Help");
		options.addOption(h);
	}
	
	public void process_command_option(Option o)
	    throws IllegalArgumentException {
       
		String argValue = o.getValue();
		switch(o.getId())
		{
			case 'H':
				hostname = argValue.trim();
				break;
			case 'f':
				file = argValue.trim();
				break;
			case 'd':
				directory = argValue.trim();
				break;
			case 'u':
				username = argValue.trim();
				break;
			case 'p':
				password = argValue.trim();
				break;
			case 'P':
				if(utils.is_intnonneg(argValue))
					port = Integer.valueOf(argValue);
				else 
                   throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Port> (%P) must be a non-negative number\n", argValue ) );
                break;
			case 'm':
				passive = true;
				break;
		}
	}
	
	public void validate_command_arguments()
	    throws IllegalArgumentException {
		if(hostname == null) {
           throw new IllegalArgumentException( "You must specify a Hostname for the remote FTP service" );
		}
		
		if(!netutils.is_host(hostname)) {
			throw new IllegalArgumentException( "You must specify a valid Host Address for the remote FTP service" );
		}
		
        if(file == null) {
			throw new IllegalArgumentException( "You must specify the name of a remote file to retrieve" );
		}

        if(directory !=null && file == null) {
			throw new IllegalArgumentException( "You must specify a File name along with a Directory" );
		}
		
		if(password !=null && username == null) {
			throw new IllegalArgumentException( "You must specify a Username for the supplied Password.");
		}
		
	}

	public void process_command_arguments(String[] argv)
	   throws IllegalArgumentException {
	}
	
	/* Execute the FTP check using the user determined parameters */
	public boolean execute_check()
	{
		/* Declare variables */
		FTPClient ftp = new FTPClient();
		File filename = null;
		FileChannel channel;
		InputStream is;
		OutputStream os;
		int reply;
		
		if(super.verbose > 0)
			verbose = true;
		
		/* Configure client to meet our requirements */
		ftp.setDefaultPort(port);
		ftp.setDefaultTimeout(timeout);
		
		if(verbose)
		{
			System.out.println("Using FTP Server: " + hostname);
			System.out.println("Using FTP Port: " + port);
			System.out.println("Using Timeout of: " + timeout);
		}	
		
		if(passive)
		{
			ftp.enterLocalPassiveMode();
			if(verbose)
				System.out.println("Using Passive Mode");
		}
		
		try
		{
			filename = new File(file);
			channel = new RandomAccessFile(filename,"rw").getChannel();
			
			if(verbose) System.out.println("Attempting FTP Connection to " + hostname);
			ftp.connect(hostname);
			reply = ftp.getReplyCode();
			
			/* Test to see if we actually managed to connect */
			if(!FTPReply.isPositiveCompletion(reply))
			{
				if(verbose) System.out.println("FTP Connection to " + hostname + " failed");
				check_state = common_h.STATE_CRITICAL;
				check_message = ftp.getReplyString();
				filename.delete();
				ftp.disconnect();
				return true;
			}
			
			/* Try and login if we're using username/password */
			if(username != null && password != null)
			{
				if(verbose) System.out.println("Attempting to log in into FTP Server " + hostname);
				
				if(!ftp.login(username,password))
				{
					if(verbose) System.out.println("Unable to log in to FTP Server " + hostname);
					check_state=common_h.STATE_CRITICAL;
					check_message = ftp.getReplyString();
					ftp.disconnect();
					filename.delete();
					return true;
				}
			}
			
			if(verbose) System.out.println("Attempting to change to required directory");
			/* Try and change to the given directory */
			if(!ftp.changeWorkingDirectory(directory))
			{
				if(verbose) System.out.println("Required directory cannot be found!");
				check_state=common_h.STATE_WARNING;
				check_message = ftp.getReplyString();
				ftp.disconnect();
				filename.delete();
				return true;
			}
			
			if(verbose) System.out.println("Attempting to retrieve specified file!");
			/* Try to get Stream on Remote File! */
			is = ftp.retrieveFileStream(file);
			
			if(is == null)
			{
				if(verbose) System.out.println("Unable to locate required file.");
				check_state = common_h.STATE_WARNING;
				check_message = ftp.getReplyString();
				ftp.disconnect();
				filename.delete();
				return true;
			}
			
			/* OutputStream */
			os = Channels.newOutputStream(channel);
			
			/* Create the buffer */
			byte[] buf = new byte[4096];
			
			if(verbose) System.out.println("Beginning File transfer...");
			for(int len=-1;(len=is.read(buf))!=-1;)
		        os.write(buf,0,len);
			
			if(verbose)
			{
				System.out.println("...transfer complete.");
				System.out.println("Attempting to finalise Command");
			}
			
			/* Finalise the transfer details */
			if(!ftp.completePendingCommand())
			{
				if(verbose) System.out.println("Unable to finalise command");
				check_state = common_h.STATE_WARNING;
				check_message = ftp.getReplyString();
				ftp.disconnect();
				filename.delete();
				return true;
			}
			
			/* Clean up */
			if(verbose) System.out.println("Check Completed.");		
			check_state = common_h.STATE_OK;
			check_message = ftp.getReplyString();
			
			/* Close out things */
			is.close();
			os.close();
			channel.close();
			filename.delete();
			
		}
		catch(IOException e)
		{
			check_state = common_h.STATE_CRITICAL;
			check_message = e.getMessage();
			if(filename !=null)
				filename.delete();
		}
		finally
		{
			if(ftp.isConnected())
			{
				try
				{
					ftp.logout();
					ftp.disconnect();
					
				}
				catch(Exception e)
				{}
			}
		}
		
		return true;
	}

	public String check_message()
	{
		return check_message;
	}

	public int check_state()
	{
		return check_state;
	}
}
