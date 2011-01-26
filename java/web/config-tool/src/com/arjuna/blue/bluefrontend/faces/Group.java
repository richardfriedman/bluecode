/**
 * This class represents all 'Group' object types that are available within the Blue
 * Framework such as Contactgroup, HostGroup and Servicegroup. As all of these object type
 * only differ in the object that they relate to, a simple groupType id is employed to allow
 * for distinction between different group types. If more functionality was required, the above
 * mentioned groups would become sub-classes of this glass..
 */

package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Group extends BlueObject
{
	
	final public static int HOSTGROUP = 0;
	final public static int SERVICEGROUP = 1;
	final public static int CONTACTGROUP = 2;
	
	private int type = -1;
	private String name = "";
	private String alias = "";
	private String[] members;
	private int listPosition;
	
	public Group()
	{
		
	}
	
	public Group(Group group)
	{
		super(group);
		this.type = group.type;
		this.name = group.name;
		this.alias = group.alias;
		this.members = group.members;
		this.listPosition = group.listPosition;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	public void setMembers(String[] members)
	{
		this.members = members;
	}
	
	public String[] getMembers()
	{
		return this.members;
	}
	
	public void setListPosition(int listPosition)
	{
		this.listPosition = listPosition;
	}
	
	public int getListPosition()
	{
		return this.listPosition;
	}
	
	/*
	 * Return a List containing the details of this Group 
	 * 
	 * @return = List, contains all of the attribute/value pairings for this group.
	 */
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		
		switch(getType())
		{
			case Group.HOSTGROUP:
				details.add("hostgroup_name");details.add(this.getName());
				break;
			
			case Group.SERVICEGROUP:
				details.add("servicegroup_name");details.add(this.getName());
				break;
			
			case Group.CONTACTGROUP:
				details.add("contactgroup_name");details.add(this.getName());
				break;
				
		}
		
		details.add("alias");details.add(getAlias());
		details.add("members");details.add(Utils.arrayToString(getMembers()));
		
		return details;
	}
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details =  new HashMap<String,String>();
		
		switch(getType())
		{
			case Group.HOSTGROUP:
				details.put("hostgroup_name",this.getName());
				break;
			
			case Group.SERVICEGROUP:
				details.put("servicegroup_name",this.getName());
				break;
			
			case Group.CONTACTGROUP:
				details.put("contactgroup_name",this.getName());
				break;
		}
		
		details.put("alias",getAlias());
		details.put("members",Utils.arrayToString(getMembers()));
		
		return details;
	}
		
}
