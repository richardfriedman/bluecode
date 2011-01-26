package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlueObject
{
	private int id = -1;
	private boolean isModifiable;
	private boolean isTemplate;
	private boolean isTemplateModifiable;
	
	public BlueObject()
	{
		
	}
	
	public BlueObject(BlueObject bObject)
	{
		this.id = bObject.id;
		this.isModifiable = bObject.isModifiable;
		this.isTemplate = bObject.isTemplate;
		this.isTemplateModifiable = bObject.isTemplateModifiable;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public void setIsModifiable(boolean isModifiable)
	{
		this.isModifiable = isModifiable;
	}
	
	public boolean getIsModifiable()
	{
		return this.isModifiable;
	}
	
	public void setIsTemplate(boolean isTemplate)
	{
		this.isTemplate = isTemplate;
	}
	
	public boolean getIsTemplate()
	{
		return this.isTemplate;
	}
	
	public void setIsTemplateModifiable(boolean isTemplateModifiable)
	{
		this.isTemplateModifiable = isTemplateModifiable;
	}
	
	public boolean getIsTemplateModifiable()
	{
		return this.isTemplateModifiable;
	}
	
	public String getName()
	{
		return null;
	}
	
	public List<String> getObjectDetails()
	{
		return new ArrayList<String>();
	}
	
	public HashMap<String,String> getObjectMapDetails()
	{
		return new HashMap<String,String>();
	}
}
