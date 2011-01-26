package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;

public class CommandHandler 
{
	
	private Command command;
	private Command modifyCommand;
	private DataModel commandDataModel;
	
	private BlueConfigXMLFileStore fileStore;
	private Paginator paginator;
	private FacesContext context;
	
	private int firstRowIndex;
	
	final private static int SORT_BY_COMMANDNAME = 1;
	final private static int SORT_BY_COMMANDLINE = 2;
	
	/* Default to ascending sorting via command name */
	private int sortBy = 1;
	private boolean ascending = true;
	
	private String delResult;
	private String selectResult;
	
	public CommandHandler()
	{
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.COMMAND);
		command = new Command();
		paginator = new Paginator(fileStore.getObjectHashMap());
	}
	
	public Command getCommand()
	{
		return this.command;
	}
	
	public void setCommand(Command command)
	{
		this.command = command;
	}
	
	public void setModifyCommand(Command modifyCommand)
	{
		this.modifyCommand = modifyCommand;
	}
	
	public Command getModifyCommand()
	{
		return this.modifyCommand;
	}
	
	public String delResult()
	{
		return this.delResult;
	}
	
	public int getFirstRowIndex()
	{
		return firstRowIndex;
	}
	
	public int getRowCount()
	{
		return paginator.getNumberOfRows();
	}
	
	public String scrollFirst()
	{
		firstRowIndex = paginator.scrollFirst();
		return "success";
	}
	
	public String scrollPrevious()
	{
		firstRowIndex = paginator.scrollPrevious();
		return "success";
	}
	
	public String scrollNext()
	{
		firstRowIndex = paginator.scrollNext();
		return "success";
	}
	
	public String scrollLast()
	{
		firstRowIndex = paginator.scrollLast();
		return "success";
	}
	
	public boolean isScrollFirstDisabled()
	{
		return firstRowIndex == 0;
	}
	
	public boolean isScrollLastDisabled()
	{
		return firstRowIndex >= fileStore.getObjectCount(ObjectXMLBuilder.COMMAND) - paginator.getNumberOfRows();
	}
		
	
	/*
	 * Method for adding a command to the current HashMap.
	 * 
	 *  @return = String, "success" if the operation is successful. "failure" otherwise.
	 */
	
	public synchronized String addCommand()
	{
		
		String result = fileStore.addObject(command,ObjectXMLBuilder.COMMAND);
		
		if(!result.equals("add-success"))
		{
			command.setId(-1);
			command.setIsModifiable(false);
			return result;
		}
		else
		{
			command = new Command();
			
			if(Utils.inWizard() && fileStore.getObjectCount(ObjectXMLBuilder.COMMAND) < 4)
				return "wizard-command";
			else if(Utils.inWizard() && fileStore.getObjectCount(ObjectXMLBuilder.COMMAND) >=4)
				return "wizard-command-add";
			else
				return result;
			
		}
	}
	
	/*
	 *	Method that modifies a command stored within the current command List.
	 *
	 * 	@return = String, "success" if the operation was a success.
	 */
	
	public synchronized String modifyCommand()
	{
		String result = fileStore.modifyObject(modifyCommand,ObjectXMLBuilder.COMMAND);
		
		if(result.equals("modify-success"))
			modifyCommand = new Command();
		
		return result;
	}
	

	public synchronized void deleteCommand(ActionEvent e)
	{
		int objectId;
		String result;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			objectId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("objectId"));
			result = fileStore.deleteObject(objectId,ObjectXMLBuilder.COMMAND);
		}
		catch(Exception ex)
		{
			return;
		}

		if(result.equals("delete-success"))
			command = new Command();
			
		delResult = "delete-success";
	}
	
	/*
	 * Method that returns a sorted datamodel of all current command information.
	 * 
	 *  @return = DataModel, a sorted dataModel of the current command information.
	 */
	
	public DataModel getSortedCommands()
	{
		List list = fileStore.getStoredObjects(ObjectXMLBuilder.COMMAND);
		sortList(list);
		
		commandDataModel = new ListDataModel(list);
		return commandDataModel;
	}
	
	/*
	 * Method for setting the environment for sorting by Name;
	 * 
	 *  @return = String, "success" if the operation was successful.
	 */
	
	public String sortByName()
	{
		if(sortBy == SORT_BY_COMMANDNAME)
		{
			ascending = !ascending;
		}
		else
		{
			sortBy = SORT_BY_COMMANDNAME;
			ascending = true;
		}
		return "success";
	}
	
	/*
	 * Method for setting the environment for sorting by Command Line;
	 * 
	 *  @return = String, "success" if the operation was successful.
	 */
	
	public String sortByCommandLine()
	{
		if(sortBy == SORT_BY_COMMANDLINE)
		{
			ascending = !ascending;
		}
		else
		{
			sortBy = SORT_BY_COMMANDLINE;
			ascending = true;
		}
		return "success";
	}
	
	public synchronized String select()
	{
		try
		{
			modifyCommand = (Command)commandDataModel.getRowData();
			return "select-command";
		}
		catch(Exception e)
		{
			return "failure";
		}
	}
	
	/* Search method, searches through the current list of commands by name and returns a 
	 * DataModel of any that contain the specified search String 
	 * 
	 * @param = String commandName, the name of the command that we are searching for.
	 * 
	 * @return = DataModel, a dataModel containing any found commands!;
	 */
	
	public DataModel searchByCommandName(String commandName)
	{
		return new ListDataModel(fileStore.searchByObjectName(commandName,ObjectXMLBuilder.COMMAND));
	}
	
	public int getCommandCount()
	{
		return fileStore.getObjectCount(ObjectXMLBuilder.COMMAND);
	}
	
	
	public List getCommandNames()
	{
		List<SelectItem> nameList = new ArrayList<SelectItem>();
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.COMMAND);
		
		for(BlueObject b: list)
		{
			SelectItem item = new SelectItem(b.getName(),b.getName());
			nameList.add(item);
		}
		
		list.clear();
		return nameList;
	}
	
	public synchronized void loadCommandById(ActionEvent e)
	{
		int commandId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			commandId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("command_id"));
			modifyCommand = (Command)fileStore.loadObjectById(commandId,ObjectXMLBuilder.COMMAND);
			selectResult = "select-command";
		}
		catch(Exception ex)
		{
			selectResult = "failure";
		}
	}
	
	public synchronized String selectResult()
	{
		return this.selectResult;
	}
	
	
	/*----------------- END OF PUBLIC METHODS -------------------------*/
	
	/*---------------------- COMPARATORS ------------------------------*/
	
	/* COMMANDNAME - DESC */
	private static final Comparator DESC_COMMANDNAME_COMPARATOR = new Comparator()
	{
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Command)o1).getName());
			String id2 = String.valueOf(((Command)o2).getName());
			
			return id2.compareTo(id1);
		}
	};
	
	/* COMMANDNAME - ASC */
	private static final Comparator ASC_COMMANDNAME_COMPARATOR = new Comparator()
	{
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Command)o1).getName());
			String id2 = String.valueOf(((Command)o2).getName());
			
			return id1.compareTo(id2);
		}
	};
	
	/* COMMANDLINE - ASC */
	private static final Comparator ASC_COMMANDLINE_COMPARATOR = new Comparator()
	{
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Command)o1).getCommandLine());
			String id2 = String.valueOf(((Command)o2).getCommandLine());
			
			return id1.compareTo(id2);
		}
	};
	
	/* COMMANDLINE - DESC */
	private static final Comparator DESC_COMMANDLINE_COMPARATOR = new Comparator()
	{
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Command)o1).getCommandLine());
			String id2 = String.valueOf(((Command)o2).getCommandLine());
			
			return id2.compareTo(id1);
		}
	};
	
	/*----------------------END COMPARATORS ---------------------------*/
	
	
	/*---------------------- PRIVATE METHODS --------------------------*/
	
	/*
	 *	Method that sorts a given list by the parameters set.
	 *
	 * @param = List list, the list that you require sorting.
	 */
	
	private void sortList(List list)
	{
		switch(sortBy)
		{
			case SORT_BY_COMMANDNAME:
				Collections.sort(list,ascending ? ASC_COMMANDNAME_COMPARATOR : DESC_COMMANDNAME_COMPARATOR);
				break;
			
			case SORT_BY_COMMANDLINE:
				Collections.sort(list,ascending ? ASC_COMMANDLINE_COMPARATOR : DESC_COMMANDLINE_COMPARATOR);
				break;
		}	
	}
}
