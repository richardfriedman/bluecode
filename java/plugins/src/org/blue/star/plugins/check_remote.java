package org.blue.star.plugins;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.blue.star.messaging.BlueMessage;
import org.blue.star.messaging.Message;
import org.blue.star.messaging.MessageProperties;
import org.blue.star.messaging.MessageTypes;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;

/**
 * This plugin is used to run checks on remote hosts. It can specify the port and hostname
 * of the remote host to connect to, as well as the transport that should be used to connect
 * to the remote host.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 *
 */
public class check_remote extends check_base 
{
	/** The name of the remote host to connect to. */
	private String hostname;
	/** The transport to use to connect to the remote host */
	private String transport = "socket";
	/** The port of the remote host to connect to */
	private int port = 5667;
	/** Args used to set our connection to the remote host */
	private String connectArgs;
	/** The name of the command on the remote host to execute */
	private String commandName;
	/** The list of args, if any that should be passed to the remote host */
	private String argList;
	/** Default timeout value */
	private long timeout = 30000;
	/** The returned check message for this plugin */
	private String check_message;
	/** The returned check state for this plugin */
	private int check_state;
	/** The InvokerLocator for the remote host */
	private InvokerLocator locator;
	/** The Client used to connect to the remote host */
	private Client client;
	
	/* main() method */
	public static void main(String[] args)
	{
		new check_remote().process_request(args);
	}
	
	/* getNotes() method */
	protected String getNotes()
	{
		return "\nThis plugin is used to execute checks on Remote Hosts running the Blue Agent\n"+
				"The remote Host must be running the Blue Agent. You can also pass arguments to\n"+
				"the remote command. For this to work, the remote Blue Agent\n"+
				"must be configured with the allow_command_arguments option. The arguments to the\n"+
				"command should be a space seperated list at the end of the plugin definition. \n";
		
	}
	
	/* getAuthor() method */
	protected String getAuthor()
	{
		return "Rob Blake, Rob.Blake@arjuna.com";
	}

	/* getCopyright() method */
	protected String getCopyright()
	{
		return "Arjuna 2007";
	}

	/* getDescription() method */
	protected String getDescription()
	{
		return "Use this plugin to perform checks on remote Hosts. The remote Host must have Blue Agent.";
	}	
	
	public void add_command_arguments(Options options)
	{
		Option T = new Option("T","Transport",true,"The Transport to use to connect to the remote Host (default:socket)");
		T.setArgName("Transport");
		options.addOption(T);
		
		Option P = new Option("P","Port",true,"The port to connect to on the remote Host (default:5667");
		P.setArgName("Port");
		options.addOption(P);
		
		Option C = new Option("C","Command Name",true,"The name of the Command to execute on the remote Host");
		C.setArgName("Command Name");
		options.addOption(C);
				
	}

	/* init_command() method */
	public void init_command() {		
	}

	/* process_command_option() method */
	public void process_command_option(Option o) throws IllegalArgumentException
	{
		String argValue = o.getValue().trim();
		
		switch(o.getId())
		{
			case 'T':
				this.transport = argValue;
				break;
			
			case 'P':
				
				if(utils.is_intnonneg(argValue))
					this.port = Integer.valueOf(argValue);
				else
					throw new IllegalArgumentException(utils.formatArgumentError(this.getClass().getName(),"<Port> (%P) must be a non-negative number\n",argValue));
				
				break;
			
			case 'H':
				if(netutils.is_host(argValue))
					this.hostname = argValue;
				else
					throw new IllegalArgumentException(utils.formatArgumentError(this.getClass().getName(),"<Hostname> (%H) must be a valid Host\n",argValue));
				
				break;
			
			case 'C':
					this.commandName = argValue;
					break;
		}
	}
	
	/* process_command_arguments() method */
	public void process_command_arguments(String[] argv) throws IllegalArgumentException
	{
		if(argv == null || argv.length ==0)
			return;
		
		/* In our case, we assume that anything else on the command line are arguments
		 * to the remote command */
		this.argList = "";
		
		for(String s: argv)
		{
			this.argList += s + " ";
		}
		
		this.argList = this.argList.trim();
	}

	/* Validate our command options */
	public void validate_command_arguments() throws IllegalArgumentException 
	{
		if(this.hostname == null)
		{
			throw new IllegalArgumentException("You must specify a Host name or IP Address to connect to.");
		}
		
		if(this.commandName == null)
		{
			throw new IllegalArgumentException("You must specify a Command to execute on the remote Host.");
		}
	}

	/* execute_check() method */
	public boolean execute_check()
	{
		try
		{
			this.connectArgs = "/?datatype=serializable&clientMaxPoolSize=10&numberOfRetries=1&numberOfCallRetries=1&timeout=" + this.timeout;
			
			this.locator = new InvokerLocator(this.transport + "://" + this.hostname + ":" + this.port + this.connectArgs);
			this.client = new Client(locator);
			this.client.setSubsystem("executeCheck");
			this.client.connect();
			
			this.processResponse(client.invoke(this.commandName,this.addCommandArguments()));
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			check_message = "Unable to connect to " + transport + "://" + hostname + ":" + port;
			check_state = common_h.STATE_CRITICAL;
			return false;
		}
		finally
		{
			if(client != null && client.isConnected())
			{
				client.disconnect();
			}
		}
		
		return true;
	}
	
	/**
	 * This method is used to put the command arguments that should be executed by 
	 * the remote plugin into the properties of the message.
	 * @return - The Map containing the relevant message properties.
	 */
	private Map<String,String> addCommandArguments()
	{
		if(this.argList == null || this.argList.length() ==0)
			return new HashMap<String,String>();
		
		Map<String,String> commandArgs = new HashMap<String,String>();
		commandArgs.put(MessageProperties.PLUGIN_ARGS,this.argList);
		
		return commandArgs;
	}
	
	/* check_message() method */
	public String check_message()
	{
		return check_message;
	}

	public int check_state()
	{
		return check_state;
	}

	/* Process the response received from the remote host */
	private void processResponse(Object remoteResponse)
	{
		if(remoteResponse instanceof Message)
		{
			Message m = (BlueMessage)remoteResponse;
			try
			{
				this.checkResponseMessage(m);
				check_message = this.getPluginOutput(m);
				check_state = this.getPluginReturnCode(m);
			}
			catch(Exception e)
			{
				check_message = e.getMessage();
				check_state = common_h.STATE_CRITICAL;
			}
		}
		else
		{
			check_message = "Unknown Response from Remote Host";
			check_state = common_h.STATE_CRITICAL;
		}
	}
	
	/**
	 * This method is used to return this plugin output from the received message.
	 * @param m - The message that has been received from the remote host.
	 * @return - The plugin output from the message.
	 */
	private String getPluginOutput(Message m) throws Exception
	{
		if(m == null)
			throw new Exception("Cannot get Plugin Output from Null Message");
		
		return (String)m.getMessageProperties().get(MessageProperties.PLUGIN_OUTPUT);
	}

	/**
	 * This method is used to get the return code from a remote execution.
	 * @param m - The message in which the return code of a remote execution is stored.
	 * @return - The return code of the remote execution.
	 * @throws Exception - Thrown if there is a problem fetching the return code.
	 */
	private int getPluginReturnCode(Message m) throws Exception
	{
		if(m == null)
			throw new Exception("Cannot get Plugin Return Code from Null Message");
		
		try
		{
			return Integer.valueOf((String)m.getMessageProperties().get(MessageProperties.PLUGIN_RETURN_CODE));
		}
		catch(NumberFormatException e)
		{
			throw new Exception("Plugin Return Code was not in Numeric Format");
		}
	}
	
	/**
	 * This method is used to check the response from the remote host is actually
	 * a remote service check result
	 * @param m - The message response from the remote host.
	 * @throws Exception - Thrown if the message is not a remote service check result.
	 */
	private void checkResponseMessage(Message m) throws Exception
	{
		if(m == null)
			throw new Exception("Response Received was Null");
		
		if(!m.getMessageType().equals(MessageTypes.REMOTE_SERVICE_CHECK_RESULT))
			throw new Exception("Unrecognised Response Received from Remote Host");
	}
}