package com.arjuna.blue.bluefrontend.faces;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Command extends BlueObject 
{
	private String name ="";
	private String commandLine = "";
		
	public Command()
	{
		
	}
	
	public Command(Command command)
	{
		super(command);
		this.name = command.name;
		this.commandLine = command.commandLine;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setCommandLine(String commandLine)
	{
		this.commandLine = commandLine;
	}
	
	public String getCommandLine()
	{
		return this.commandLine;
	}
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("command_name");details.add(getName());
		details.add("command_line");details.add(getCommandLine());
		
		return details;
		
	}
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("command_name",getName());
		details.put("command_line",getCommandLine());		
		return details;
	}
}
