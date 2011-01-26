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
import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.blue.star.plugins.salesforce.GetSchema;
import org.blue.star.plugins.util.xml.Util;
import org.blue.star.plugins.util.xml.XmlDiff;
import org.jdom.Document;

/**
 * @author Mark
 * Done: Keep history.  For now just keep multiple files and put a timestamp on the file.
 * Done: Allow users to have us check against a baseline, like their approved production Schema.  Basically they would either pass in the baseline file,
 * 	or make it so that we don't overwrite that file.
 * TODO: Allow users to set the state levels.  For instance, they may want something like:
 * 	Field Delete		Critical
 * 	Field Value Change	Warning
 * 	Object Delete		Critical
 * 	Object Insert		None
 * 	Field Insert		None
 */
public class check_salesforce_schema extends check_base
{
	private String user = "";

	private String pass = "";

	private String url = "";
	
	private boolean keepBase = false;

	private int state = common_h.STATE_OK;

	private StringBuffer diff;

	protected String getAuthor()
	{
		return "Mark Lugert mlugert@yahoo.com";
	}

	protected String getCopyright()
	{
		return "Blue 2007";
	}

	protected String getDescription()
	{
		return "\nUse this plugin to check your Sales Force Schema for changes.\n";
	}

	protected String getNotes()
	{
		return "\nThis plugin uses the Sales Force API to get the Sales Force Schema for the supplied User\n" +
				 		"Once the Schema is retrieved from Sales Force the check will compare it to the Schema\n" +
				 		"retrieved the last time this check was executed.  This check can take some time to execute\n" +
				 		"depending on the size of the Schema.\n\n" +
						"Output files can be found in ./etc/xml/salesforce_get_schema directory.\n" +
						"The file which all checks are compared against is base.xml.  If the k (Keep)\n" +
						"option is not specified then every check performed will replace the base.xml file\n" +
						"with the latest schema.\n\n" +
						"If your Sales Force Schema changes occur on a regular production release schedule\n" +
						"and you wish to enforce this by making sure the schema doesn't change between\n" +
						"releases you can specify the k (Keep) option which will keep base.xml without replacing it. \n" +
						"In order to replace base.xml with the latest Sales Force Schema simply do not specify\n" +
						"the k option, or delete the base.xml file.\n\n" +
						"Please note that each schema check will write out the schema from that check.  The\n" +
						"files have the following format blue_sf_[name].xml where name is:\n\n" +
						"YEAR-MONTH-DAY-HOUR-MINUTE-SECOND-AM_PM";
	}

	public static void main(String[] args)
	{
		new check_salesforce_schema().process_request(args);
	}

	/* Give out some details about ourself */
	public void init_command()
	{
	}

	/* Add all our plugin options */
	public void add_command_arguments(Options options)
	{
		Option U = new Option("u", "User Name", true, "The Sales Force User Name.");
		U.setArgName("User Name");
		options.addOption(U);

		Option P = new Option("p", "Password", true, "The Sales Force Password.");
		P.setArgName("Password");
		options.addOption(P);

		Option W = new Option(
				"w",
				"Sales Force API Url",
				true,
				"The Sales Force API Url.  Typically this is not needed because the Service Locator returns the correct Url.");
		W.setArgName("Sales Force API Url");
		options.addOption(W);
		
		Option K = new Option("k", "Keep Baseline", true, "If Set this will keep the base.xml " +
				"file as the standard schema to always check against.  This is useful in " +
				"production environments where the schema is only supposed to change when " +
				"new code is checked in.  In that event just delete base.xml and the " +
				"check will recreate with the latest schema.");
		P.setArgName("Keep Base");
		options.addOption(K);
	}

	/* Process the user supplied options */
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
			case 'k':
				this.keepBase = true;
				break;
		}
	}

	public void process_command_arguments(String[] argv) throws IllegalArgumentException
	{
	}

	public void validate_command_arguments()
	{
		if (this.user == null || this.pass == null)
		{
			throw new IllegalArgumentException("You must both a User Name and Password!");
		}
	}

	public boolean execute_check()
	{
		try
		{
			// Get the latest
			GetSchema gs = new GetSchema(this.url, this.user, this.pass);
			Document doc = gs.getSchema();

			File outDir = Util.getOutputDir("salesforce_get_schema", false);
			File base = new File(outDir.getCanonicalPath() + File.separator + common_h.SF_BASE_NAME);

			// write the file out no matter what, this way we have history
			File file = Util.getNewFileName(outDir);
			Util.writeXMLFile(doc, file);
			
			// If the base file exists we can do a compare
			if (base.exists())
			{
				StringBuffer diff = XmlDiff.diff(base, file);

				if (diff.length() > 0)
				{
					this.diff = diff;
					this.state = common_h.STATE_WARNING;

					if(!this.keepBase)
						Util.writeXMLFile(doc, base);
				}
			} else
			{
				Util.writeXMLFile(doc, base);
			}
		} catch (Exception e) // Need to catch all exceptions, because no matter what we don't know what to do with it
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			this.state = common_h.STATE_UNKNOWN;
			return false;
		} 
		return true;
	}

	public String check_message()
	{
	    if ( diff == null )
		return this.diff.toString();
	}

	public int check_state()
	{
		return this.state;
	}

}
