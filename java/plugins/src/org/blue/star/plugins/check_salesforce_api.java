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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.blue.star.plugins.salesforce.SalesForceConnector;

import com.sforce.soap.partner.SoapBindingStub;

/**
 * @author Mark
 *
 */
public class check_salesforce_api extends check_base
{
	private String user = "";

	private String pass = "";

	private String url = "";

	private int state = common_h.STATE_OK;

	private StringBuffer diff;

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#getAuthor()
	 */
	protected String getAuthor()
	{
		return "Mark Lugert mlugert@yahoo.com";
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#getCopyright()
	 */
	protected String getCopyright()
	{
		return "Blue 2007";
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#getDescription()
	 */
	protected String getDescription()
	{
		return "\nUse this plugin to check if the Sales Force API is up\n";
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#getNotes()
	 */
	protected String getNotes()
	{
		return "\nThis plugin uses the Sales Force Web Services API to\nlogin to Sales Force with the supplied User Name and Password\n"
				+ "This check has low overhead and can be executed as often as desired.";
	}

	public static void main(String[] args)
	{
		new check_salesforce_api().process_request(args);
	}

	/* Give out some details about ourself */
	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#init_command()
	 */
	public void init_command()
	{
	}

	/* Add all our plugin options */
	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#add_command_arguments(org.apache.commons.cli.Options)
	 */
	public void add_command_arguments(Options options)
	{
		Option U = new Option("u", "User Name", true, "The Sales Force User Name.");
		U.setArgName("User Name");
		options.addOption(U);

		Option P = new Option("p", "Password", true, "The Sales Force Password.");
		P.setArgName("Password");
		options.addOption(P);

		Option W = new Option("w", "Sales Force API Url", true,
				"The Sales Force API Url.  Typically this is not needed.");
		W.setArgName("Sales Force API Url");
		options.addOption(W);
	}

	
	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#process_command_option(org.apache.commons.cli.Option)
	 */
	public void process_command_option(Option o) throws IllegalArgumentException
	{
		String argValue = o.getValue();

		switch (o.getId())
		{
			case 'u':
				this.user = argValue;
				break;
			case 'p':
				this.pass = argValue;
				break;
			case 'w':
				this.url = argValue;
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#process_command_arguments(java.lang.String[])
	 */
	public void process_command_arguments(String[] argv) throws IllegalArgumentException
	{
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#validate_command_arguments()
	 */
	public void validate_command_arguments()
	{
		if (this.user == null || this.pass == null)
		{
			throw new IllegalArgumentException("You must both a User Name and Password!");
		}
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#execute_check()
	 */
	public boolean execute_check()
	{
		try{
		SalesForceConnector sfc = new SalesForceConnector(this.url, this.user, this.pass);
		SoapBindingStub binding = sfc.login();

		if (this.user.equals(binding.getUsername()))
		{
			this.state = common_h.STATE_OK;
			return true;
		}
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			this.state = common_h.STATE_UNKNOWN;
			return false;
		}
		
		this.state = common_h.STATE_WARNING;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#check_message()
	 */
	public String check_message()
	{
		return this.diff.toString();
	}

	/* (non-Javadoc)
	 * @see org.blue.plugins.check_base#check_state()
	 */
	public int check_state()
	{
		return this.state;
	}

}
