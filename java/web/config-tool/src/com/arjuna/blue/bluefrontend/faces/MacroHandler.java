package com.arjuna.blue.bluefrontend.faces;

import java.util.Iterator;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;

public class MacroHandler
{
	private Macro macro;
	private BlueConfigXMLFileStore fileStore;
	private Byte[] idTaken = new Byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private DataModel macroModel;
	private String delResult;
	private FacesContext context;
		
	public MacroHandler()
	{
		macro = new Macro();
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.MACRO);
			
		Iterator i = fileStore.getObjectHashMap().keySet().iterator();
		/* Work out what the next valid macro id is */
		while(i.hasNext())
		{
			Integer id = (Integer)i.next();
			
			if(id <= 32)
				idTaken[id-1] = 1;
		}
		
	}
	
	public String delResult()
	{
		return delResult;
	}
	
	public void setMacro(Macro macro)
	{
		this.macro = macro;
	}
	
	public Macro getMacro()
	{
		return this.macro;
	}
	
	public int getMacroCount()
	{
		return fileStore.getObjectCount(ObjectXMLBuilder.MACRO);
	}

	public synchronized String addMacro()
	{
		String result;
		
		if(macro.getId() == -1)
		{
			int nextId = getNextId();
			
			if(fileStore.getObjectCount(ObjectXMLBuilder.MACRO) == 32 || nextId == -1)
			{
				return "failure";
			}
			
			macro.setId(nextId);
			
			result = fileStore.storeMacro(macro);
						
			if(result.equals("add-success"))
			{
				idTaken[nextId -1] = 1;
				macro = new Macro();
			
				if(Utils.inWizard())
					return "wizard-macro-add";
			
				return "add-success";
			}
		}
		return "failure";
	}
	
	public synchronized void deleteMacro(ActionEvent e)
	{
		String result;
		int macroId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			macroId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("macroId"));
		}
		catch(Exception ex)
		{
			delResult="failure";
			return;
		}
		
		result = fileStore.deleteMacro(macroId);
		if(result.equals("delete-success"))
		{
			idTaken[macroId -1] = 0;
			macro = new Macro();
		}
		
		delResult = result;
	}
	
	public synchronized String modifyMacro()
	{
		String result = fileStore.modifyMacro(macro);
		
		if(result.equals("modify-success"))
			macro = new Macro();
		
		return result;
	}
	
	public synchronized String select()
	{
		try
		{
			macro = (Macro)macroModel.getRowData();
		}
		catch(Exception e)
		{
			return "failure";
		}
		return "success";
	}
	
	public DataModel getMacroDetails()
	{
		macroModel = new ListDataModel(fileStore.getStoredObjects(ObjectXMLBuilder.MACRO));
		return macroModel;
	}
	
	private int getNextId()
	{
		int counter = 1;
		
		for(Byte b : idTaken)
		{
			if(b == 0)
				return counter;
			
			counter++;
		}
		return -1;
	}
		
}
