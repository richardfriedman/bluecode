package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;

public class GroupHandler 
{
	private Group contactGroup;
	private Group hostGroup;
	private Group serviceGroup;
	private Group modifyGroup;
	
	private FacesContext context;
	private Paginator paginator;
	
	/* Required for Navigation Rules! */
	private String addResult;
	private String delResult;
	private String modResult;
	private String loadResult;
	private String selectResult;
	private int modifyGroupType;
	
	private int firstRowIndex;
	
	/* Sorting values */
	final private static int SORT_BY_GROUPNAME = 1;
	final private static int SORT_BY_GROUPALIAS = 2;
	
	/* default to sorting by group name */
	
	private int sortBy = 1;
	private boolean ascending = true;
	
	private BlueConfigXMLFileStore serviceGroupFileStore;
	private BlueConfigXMLFileStore hostGroupFileStore;
	private BlueConfigXMLFileStore contactGroupFileStore;
	
	private DataModel contactGroupsModel;
	private DataModel hostGroupsModel;
	private DataModel serviceGroupsModel;
	
	
	public GroupHandler()
	{
		contactGroup = new Group();
		serviceGroup = new Group();
		hostGroup = new Group();
				
		serviceGroupFileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.SERVICEGROUP);
		hostGroupFileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.HOSTGROUP);
		contactGroupFileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.CONTACTGROUP);
		
		paginator = new Paginator();
	}
	
	/*
	 * Method for adding a group to the current groupList;
	 * 
	 * @return  = String, "success" if the operation was successful. "failure" otherwise.
	 */
	
	public void setHostGroup(Group hostGroup)
	{
		this.hostGroup = hostGroup;
	}
	
	public Group getHostGroup()
	{
		return this.hostGroup;
	}
	
	public void setServiceGroup(Group serviceGroup)
	{
		this.serviceGroup = serviceGroup;
	}
	
	public Group getServiceGroup()
	{
		return this.serviceGroup;
	}
	
	public void setContactGroup(Group contactGroup)
	{
		this.contactGroup = contactGroup;
	}
	
	public Group getContactGroup()
	{
		return this.contactGroup;
	}
	
	public void setModifyGroup(Group modifyGroup)
	{
		this.modifyGroup = modifyGroup;
	}
	
	public Group getModifyGroup()
	{
		return this.modifyGroup;
	}
	
	public synchronized String addResult()
	{
		return this.addResult;
	}
	
	public synchronized String modResult()
	{
		return this.modResult;
	}
	
	public synchronized String delResult()
	{
		return this.delResult;
	}
	
	public synchronized int getModifyGroupType()
	{
		return this.modifyGroupType;
	}
	
	public int getFirstRowIndex()
	{
		return firstRowIndex;
	}
	
	public int getRowCount()
	{
		return paginator.getNumberOfRows();
	}
	
	public synchronized String scrollFirst(ActionEvent e)
	{
		try
		{
			paginator.setObjectList(getObjectList(getGroupType(e)));
			firstRowIndex = paginator.scrollFirst();
			return "success";
		}
		catch(Exception ex)
		{
			return "failure";
		}
	}
	
	public synchronized String scrollPrevious(ActionEvent e)
	{
		try
		{
			paginator.setObjectList(getObjectList(getGroupType(e)));
			firstRowIndex = paginator.scrollPrevious();
		}
		catch(Exception ex)
		{
			return "failure";
		}
		return "success";
	}
	
	public synchronized String scrollNext(ActionEvent e)
	{
		try
		{
			paginator.setObjectList(getObjectList(getGroupType(e)));
			firstRowIndex = paginator.scrollNext();
			return "success";
		}
		catch(Exception ex)
		{
			return "failure";
		}
	}
	
	public synchronized String scrollLast(ActionEvent e)
	{
		try
		{
			paginator.setObjectList(getObjectList(getGroupType(e)));
			firstRowIndex = paginator.scrollLast();
			return "success";
		}
		catch(Exception ex)
		{
			return "failure";
		}
	}
	
	public synchronized boolean isScrollFirstDisabled()
	{
		return firstRowIndex == 0;
	}
	
	public synchronized boolean isScrollLastHostDisabled()
	{
		return firstRowIndex >= hostGroupFileStore.getObjectCount(ObjectXMLBuilder.HOSTGROUP) - paginator.getNumberOfRows();
	}

	public synchronized boolean isScrollLastServiceDisabled()
	{
		return firstRowIndex >= serviceGroupFileStore.getObjectCount(ObjectXMLBuilder.SERVICEGROUP) - paginator.getNumberOfRows();
	}
	
	public synchronized boolean isScrollLastContactDisabled()
	{
		return firstRowIndex >= contactGroupFileStore.getObjectCount(ObjectXMLBuilder.CONTACTGROUP) - paginator.getNumberOfRows();
	}
	
	/*
	 * Method for adding a group.
	 */	
	public synchronized void addGroup(ActionEvent e)
	{
		int groupType;
		String result;
		
		try
		{
			groupType = Integer.valueOf((String)e.getComponent().getAttributes().get("groupType"));
		}
		catch(NumberFormatException f)
		{
			addResult = "failure";
			return;
		}
		
		boolean inWizard = Utils.inWizard();
		
		if(groupType == Group.CONTACTGROUP)
		{
				/* Added 19/01/07 - Seems that spaces in contact group names can confuse things */
				contactGroup.setType(Group.CONTACTGROUP);
				contactGroup.setName(contactGroup.getName().replace(" ","_"));
				result = contactGroupFileStore.addObject(contactGroup,ObjectXMLBuilder.CONTACTGROUP);
				
				if(!result.equals("add-success"))
				{
					contactGroup.setId(-1);
					contactGroup.setIsModifiable(false);
					addResult = result;
					return;
				}
					
				if(inWizard)
				{
					addResult ="wizard-contactgroup-add";
					return;
				}
			
				addResult = "add-success";
			
		}
		else if(groupType == Group.HOSTGROUP)
		{
			if(hostGroup.getId() == -1)
			{
				hostGroup.setType(Group.HOSTGROUP);
				result = hostGroupFileStore.addObject(hostGroup,ObjectXMLBuilder.HOSTGROUP);
				
				if(!result.equals("add-success"))
				{
					hostGroup.setId(-1);
					hostGroup.setIsModifiable(false);
					addResult = result;
					return;
				}
					
				if(inWizard)
				{
					addResult ="wizard-hostgroup-add";
					return;
				}
				
				addResult = "add-success";
			}
		}
		else if(groupType == Group.SERVICEGROUP)
		{
			if(serviceGroup.getId() == -1)
			{
				serviceGroup.setType(Group.SERVICEGROUP);
				result = serviceGroupFileStore.addObject(serviceGroup,ObjectXMLBuilder.SERVICEGROUP);
				
				if(!result.equals("add-success"))
				{
					serviceGroup.setId(-1);
					serviceGroup.setIsModifiable(false);
					addResult = result;
					return;
				}
					
				if(inWizard)
				{
					addResult ="wizard-servicegroup-add";
					return;
				}
				
				addResult = "add-success";
			}
		}
	}
	
	/*
	 * Method to delete a group. The faces page passes a group type 
	 * 
	 * @param = ActionEvent, from the component that fired the event.
	 * 
	 * @return = String, success if the object was deleted ok.
	 */

	public synchronized void deleteGroup(ActionEvent e)
	{
		int groupType;
		int groupId;
		String result;
		
		
		try
		{
			FacesContext context = FacesContext.getCurrentInstance();
			groupType = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("groupType"));
			groupId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("groupId"));
					
			if(groupType == Group.CONTACTGROUP)
			{
				result = contactGroupFileStore.deleteObject(groupId,ObjectXMLBuilder.CONTACTGROUP);

				if(result.equals("delete-success"))
					contactGroup = new Group();
				
				delResult = result;
			}
			else if(groupType == Group.HOSTGROUP)
			{
				result = hostGroupFileStore.deleteObject(groupId,ObjectXMLBuilder.HOSTGROUP);
				if(result.equals("delete-success"))
					hostGroup = new Group();
				
				delResult = result;
			}
			else if(groupType == Group.SERVICEGROUP)
			{
				result = serviceGroupFileStore.deleteObject(groupId,ObjectXMLBuilder.SERVICEGROUP);
				if(result.equals("delete-success"))
					serviceGroup = new Group();
				
				delResult = result;
			}
			
		}
		catch(Exception ex)
		{
			delResult ="failure";
		}
	}
	
	/*
	 * Method for modifing a current Group object.
	 * 
	 * @return = String, "success" if the operation was successful; "failure" otherwise.
	 */
	
	public synchronized void modifyGroup(ActionEvent e)
	{
		int groupType;
		String result;
		
		try
		{
			groupType = Integer.valueOf((String)e.getComponent().getAttributes().get("groupType"));
		}
		catch(NumberFormatException f)
		{
			modResult = "failure";
			return;
		}
		
		if(groupType == Group.HOSTGROUP)
		{
			result = hostGroupFileStore.modifyObject(modifyGroup,ObjectXMLBuilder.HOSTGROUP);
			
			if(result.equals("modify-success"))
				modifyGroup = new Group();
			modResult = result;
		}
		else if(groupType == Group.SERVICEGROUP)
		{
			
			result = serviceGroupFileStore.modifyObject(modifyGroup,ObjectXMLBuilder.SERVICEGROUP);
			
			if(result.equals("modify-success"))
				modifyGroup = new Group();
			modResult = result;
		}
		else if(groupType == Group.CONTACTGROUP)
		{
			result = contactGroupFileStore.modifyObject(modifyGroup,ObjectXMLBuilder.CONTACTGROUP);
			
			if(result.equals("modify-success"))
				modifyGroup = new Group();
			
			modResult = result;
		}
	}
	
	/*
	 * Method that returns a sorted DataModel of the current ContactGroups
	 * 
	 * @return = DataModel, a sorted list of the current ContactGroups.
	 */
	
	public DataModel getSortedContactGroups()
	{
		List<BlueObject> groups = contactGroupFileStore.getStoredObjects(ObjectXMLBuilder.CONTACTGROUP);
		sortGroups(groups);
		
		contactGroupsModel = new ListDataModel(groups);
		return contactGroupsModel;		
		
	}
	
	/*
	 * Method that returns a sorted DataModel of the current HostGroups.
	 * 
	 * @return = DataModel, a sorted list of the current HostGroups.
	 */
	
	public DataModel getSortedHostGroups()
	{
		List<BlueObject> groups = hostGroupFileStore.getStoredObjects(ObjectXMLBuilder.HOST);
		sortGroups(groups);
		
		hostGroupsModel = new ListDataModel(groups);
		return hostGroupsModel;
		
	}
	
	/*
	 * Method that returns a sorted DataModel of the current ServiceGroups.
	 * 
	 * @return = DataModel, a sorted list of the current ServiceGroups.
	 */
	
	public DataModel getSortedServiceGroups()
	{
		List<BlueObject> groups = serviceGroupFileStore.getStoredObjects(ObjectXMLBuilder.SERVICEGROUP);
		sortGroups(groups);
		serviceGroupsModel = new ListDataModel(groups);
		return serviceGroupsModel;
	}
	
	/* Method to set parameters for sorting by Groupname */
	
	public String sortByGroupName()
	{
		if(sortBy == SORT_BY_GROUPNAME)
		{
			ascending = !ascending;
		}
		else
		{
			sortBy = SORT_BY_GROUPNAME;
			ascending = true;
		}
		
		return "success";
	}
	
	/* Method to set parameters for sorting by Alias */
	
	public String sortByGroupAlias()
	{
		if(sortBy == SORT_BY_GROUPALIAS)
		{
			ascending = !ascending;
		}
		else
		{
			sortBy = SORT_BY_GROUPALIAS;
			ascending = true;
		}
		
		return "success";
	}
	
	public String select(ActionEvent e)
	{
		int groupType;
		
		try
		{
			groupType = Integer.valueOf((String)e.getComponent().getAttributes().get("groupType"));
		}
		catch(NumberFormatException f)
		{
			return "failure";
		}
		
		try
		{
			if(groupType == Group.CONTACTGROUP)
			{
				modifyGroup = (Group)contactGroupsModel.getRowData();
				modifyGroupType = Group.CONTACTGROUP;
			}
			else if(groupType == Group.HOSTGROUP)
			{
				modifyGroup = (Group)hostGroupsModel.getRowData();
				modifyGroupType = Group.HOSTGROUP;
			}
			else if(groupType == Group.SERVICEGROUP)
			{
				modifyGroup = (Group)serviceGroupsModel.getRowData();
				modifyGroupType = Group.SERVICEGROUP;
			}
		}
		catch(Exception ex)
		{
			return "failure";
		}
		
		selectResult = "select-group";
		return "select-group";
	}
	
	public String selectResult()
	{
		return this.selectResult;
	}
	
	public DataModel searchByGroupName(String groupName, int groupType)
	{
		if(groupType == Group.HOSTGROUP)
		{
			return new ListDataModel(hostGroupFileStore.searchByObjectName(groupName,ObjectXMLBuilder.HOSTGROUP));
		}
		else if(groupType == Group.SERVICEGROUP)
		{
			return new ListDataModel(serviceGroupFileStore.searchByObjectName(groupName,ObjectXMLBuilder.SERVICEGROUP));
		}
		else if(groupType == Group.CONTACTGROUP)
		{
			return new ListDataModel(contactGroupFileStore.searchByObjectName(groupName,ObjectXMLBuilder.CONTACTGROUP));
		}
		
		return null;
	}
	
	public int getHostGroupCount()
	{
		return hostGroupFileStore.getObjectCount(ObjectXMLBuilder.HOSTGROUP);
	}
	
	public int getContactGroupCount()
	{
		return contactGroupFileStore.getObjectCount(ObjectXMLBuilder.CONTACTGROUP);
	}
	
	public int getServiceGroupCount()
	{
		return serviceGroupFileStore.getObjectCount(ObjectXMLBuilder.SERVICEGROUP);
	}
	
	public List<SelectItem> getHostGroupNames()
	{
		List<BlueObject> list = hostGroupFileStore.getStoredObjects(ObjectXMLBuilder.HOSTGROUP);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject b: list)
		{
			SelectItem item = new SelectItem(b.getName(),b.getName());
			items.add(item);
		}
		
		list = null;
		return items;
	}
	
	public List<SelectItem> getServiceGroupNames()
	{
		List<BlueObject> list = serviceGroupFileStore.getStoredObjects(ObjectXMLBuilder.SERVICEGROUP);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject b: list)
		{
			SelectItem item = new SelectItem(b.getName(),b.getName());
			items.add(item);
		}
		
		list = null;
		return items;
	}
	
	public List<SelectItem> getContactGroupNames()
	{
		List<BlueObject> list = contactGroupFileStore.getStoredObjects(ObjectXMLBuilder.CONTACTGROUP);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject b: list)
		{
			SelectItem item = new SelectItem(b.getName(),b.getName());
			items.add(item);
		}
		
		list = null;
		return items;
	}
	
	public String loadResult()
	{
		return this.loadResult;
	}
	
	public String loadGroupById(ActionEvent e)
	{
		int groupType;
		int groupId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			groupType = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("group_type"));
			groupId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("group_id"));
		}
		catch(Exception ex)
		{
			return "failure";
		}
		
		try
		{
			if(groupType == Group.HOSTGROUP)
			{
				modifyGroup = (Group)hostGroupFileStore.loadObjectById(groupId,ObjectXMLBuilder.HOSTGROUP);
				modifyGroupType = Group.HOSTGROUP;
			}
			else if(groupType == Group.SERVICEGROUP)
			{
				modifyGroup = (Group)serviceGroupFileStore.loadObjectById(groupId,ObjectXMLBuilder.SERVICEGROUP);
				modifyGroupType = Group.SERVICEGROUP;
			}
			else if(groupType == Group.CONTACTGROUP)
			{
				modifyGroup = (Group)contactGroupFileStore.loadObjectById(groupId,ObjectXMLBuilder.CONTACTGROUP);
				modifyGroupType = Group.CONTACTGROUP;
			}
		}
		catch(Exception ex)
		{
			return "failure";
		}
		
		selectResult = "select-group";
		return "success";
	}
	
	/* ----------------------- END OF PUBLIC METHODS ---------------------------------*/
	
	/*------------------------ COMPARATORS -------------------------------------------*/
	
	/* Alias - ASC */
	private static final Comparator ASC_ALIAS_COMPARATOR = new Comparator(){
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Group)o1).getAlias());
			String id2 = String.valueOf(((Group)o2).getAlias());
			
			return id1.compareTo(id2);
		}
	};
	
	/* Alias - DESC */
	private static final Comparator DESC_ALIAS_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Group)o1).getAlias());
			String id2 = String.valueOf(((Group)o2).getAlias());
			
			return id2.compareTo(id1);
		}
	};
	
	/* Group name - ASC */
	private static final Comparator ASC_GROUPNAME_COMPARATOR = new Comparator(){
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Group)o1).getName());
			String id2 = String.valueOf(((Group)o2).getName());
			
			return id1.compareTo(id2);
		}
	};
	
	/* Group name - DESC */
	private static final Comparator DESC_GROUPNAME_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Group)o1).getName());
			String id2 = String.valueOf(((Group)o2).getName());
			
			return id2.compareTo(id1);
		}
	};
	/*------------------------ END OF COMPARATORS ------------------------------------*/
	
	
	/*------------------------ PRIVATE METHODS ---------------------------------------*/
	
	/* Method to sort a given list of group objects */
	
	private void sortGroups(List groupList)
	{
		switch(sortBy)
		{
			case SORT_BY_GROUPNAME:
				Collections.sort(groupList,ascending ? ASC_GROUPNAME_COMPARATOR : DESC_GROUPNAME_COMPARATOR);
				break;
			
			case SORT_BY_GROUPALIAS:
				Collections.sort(groupList,ascending ? ASC_ALIAS_COMPARATOR : DESC_ALIAS_COMPARATOR);
				break;
		}	
	}
	
	private int getGroupType(ActionEvent e)
	{
		int groupType;
		
		try
		{
			groupType = Integer.valueOf((String)e.getComponent().getAttributes().get("groupType"));
		}
		catch(Exception ex)
		{
			return -1;
		}
		
		return groupType;
	}
	
	private HashMap getObjectList(int objectType)
	{
		switch(objectType)
		{
			case Group.HOSTGROUP:
				return hostGroupFileStore.getObjectHashMap();
				
			case Group.SERVICEGROUP:
				return serviceGroupFileStore.getObjectHashMap();
				
			case Group.CONTACTGROUP:
				return contactGroupFileStore.getObjectHashMap();
				
			default:
				throw new IllegalStateException("Unknown Group Type!");
		}
	}
}
