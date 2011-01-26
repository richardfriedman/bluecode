package com.arjuna.blue.bluefrontend.faces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.arjuna.blue.bluefrontend.xml.ConfigParser;
import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;
import com.arjuna.blue.bluefrontend.xml.ObjectXMLConverter;

/*
 * A very simple bean used for handling some details regarding where the user would like the
 * config to be written to, whether or not they are running the wizard and the location of their current
 * nagios config, should they have one.
 */

public class AccountHandler 
{
	
	private boolean inWizard = false;
	private boolean hasRun = false;
	private boolean userSetDirectory = false;
	private String outputLocation = "";
	private String configLocation;
	private ConfigParser parser;
	private char[] illegalFileChars = new char[]{'`','¬','!','"','£','$','%','^','*','(',')','=','+','#','~','@','\'','|','?','<','>','{','}','[',']'};
		
	public AccountHandler()
	{
		File file  = new File("blueconfig.lock");
		
		if(file.exists())
		{
			inWizard = false;
			hasRun = true;
		}
		
		file = new File("blueconfig.log");
		
		if(file.exists())
		{
			userSetDirectory = true;
			
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader("blueconfig.log"));
				outputLocation = reader.readLine().trim();
				outputLocation = stripIllegalFileChars(outputLocation);
				reader.close();
			}
			catch(Exception e)
			{
				outputLocation = "";
			}
		}
		
	}
	
	public void setInWizard(boolean inWizard)
	{
		this.inWizard = inWizard;
	}
	
	public boolean getInWizard()
	{
		return this.inWizard;
	}
	
	public boolean getUserSetDirectory()
	{
		return this.userSetDirectory;
	}
	
	public void setUserSetDirectory(boolean userSetDirectory)
	{
		this.userSetDirectory = userSetDirectory;
	}
	
	public void setOutputLocation(String outputLocation)
	{
		/* Try to force the file output description into a specified format */
		outputLocation = outputLocation.replace('\\','/');
		
		if(outputLocation.endsWith("/"))
			outputLocation = outputLocation.substring(0,outputLocation.length()-1); 
		
		outputLocation = stripIllegalFileChars(outputLocation);
		
		/* Make all possible required directories in the new location */
		if(!makeDirectories(outputLocation))
		{
			/* If we have been unable to make all the current directories, default to blue-config */
			makeDirectories("blue-config");
			this.outputLocation = "blue-config";
		}
		
		this.outputLocation = new File(outputLocation).getAbsolutePath();
		writeStateFile("blueconfig.log",this.outputLocation);
	}
	
	public String setInitialOutputLocation()
	{
		setOutputLocation(this.outputLocation);
		userSetDirectory = true;
		return "success";
	}
	
	public String getOutputLocation()
	{
		return this.outputLocation;
	}
	
	
	public void setConfigLocation(String configLocation)
	{
		configLocation = configLocation.replace('\\','/');
		this.configLocation = configLocation;
	}
	
	public String getConfigLocation()
	{
		return this.configLocation;
	}
	
	public boolean getHasRun()
	{
		return this.hasRun;
	}
	
	public void setHasRun(boolean hasRun)
	{
		this.hasRun = hasRun;
	}
	
	public void selectWizard()
	{
		this.inWizard = true;
	}
	
	public String beginWizard()
	{
		return "wizard-begin";
	}
	
	public String finishWizard()
	{
		inWizard = false;
		hasRun = true;
		writeStateFile("blueconfig.lock",null);		
		return "finish";
	}
	
	public String finishImport()
	{
		inWizard = false;
		hasRun = true;
		writeStateFile("blueconfig.lock",null);
		
		return "finish";
	}
	
	public String importConfig()
	{
		parser = new ConfigParser();
		parser.setOutputLocation(outputLocation);
		parser.setConfigLocation(configLocation);
		
		try
		{
			
			if(parser.convert(configLocation,"blue.xml"))
			{
				ObjectXMLConverter converter = new ObjectXMLConverter();
				converter.setOutputDirectory(outputLocation);
				converter.convertBlueMainConfig("blue.xml","blue.cfg");
				converter.convertMacros();
				converter.convertDocument("hosts.xml",ObjectXMLBuilder.HOST);
				converter.convertDocument("hostgroups.xml",ObjectXMLBuilder.HOSTGROUP);
				converter.convertDocument("services.xml",ObjectXMLBuilder.SERVICE);
				converter.convertDocument("servicegroups.xml",ObjectXMLBuilder.SERVICEGROUP);
				converter.convertDocument("contacts.xml",ObjectXMLBuilder.CONTACT);
				converter.convertDocument("contactgroups.xml",ObjectXMLBuilder.CONTACTGROUP);
				converter.convertDocument("timeperiods.xml",ObjectXMLBuilder.TIMEPERIOD);
				converter.convertDocument("commands.xml",ObjectXMLBuilder.COMMAND);
				converter.convertDocument("serviceescalations.xml",ObjectXMLBuilder.SERVICEESCALATION);
				converter.convertDocument("servicedependencies.xml", ObjectXMLBuilder.SERVICEDEPENDENCY);
				converter.convertDocument("hostdependencies.xml",ObjectXMLBuilder.HOSTDEPENDENCY);
				converter.convertDocument("hostescalations.xml",ObjectXMLBuilder.HOSTESCALATION);
				converter.convertDocument("hostextinfo.xml",ObjectXMLBuilder.HOSTEXTINFO);
				converter.convertDocument("serviceextinfo.xml",ObjectXMLBuilder.SERVICEEXTINFO);
				finishImport();
			}
			
			
		}
		catch(Exception e)
		{
			return "parsing-error";
		}
		
		return "parsing-success";
	}
	
	private boolean makeDirectories(String outputLocation)
	{
		File file = new File(outputLocation + "/xml/templates");
		
		try
		{
			if(!file.exists())
				file.mkdirs();
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	private boolean writeStateFile(String filename, String message)
	{
		BufferedWriter out;
		
		try
		{
			out = new BufferedWriter(new FileWriter(filename));
			
			if(message == null)
				out.write("config locked");
			else
				out.write(message);
		
			out.close();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	private String stripIllegalFileChars(String filename)
	{
		for(char c: illegalFileChars)
			filename = filename.replace(c,'^');
			
		filename = filename.replace("^","");
		return filename;
	}
}
