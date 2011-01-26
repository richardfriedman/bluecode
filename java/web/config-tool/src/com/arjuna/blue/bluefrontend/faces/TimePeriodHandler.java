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

public class TimePeriodHandler
{
	
	private Paginator paginator;
	private FacesContext context;
	
	private BlueConfigXMLFileStore fileStore;
	
	private TimePeriod timePeriod;
	private TimePeriod modifyTimePeriod;
	private int templateToLoad;
		
	private int firstRowIndex;
	private String delResult;
	private String selectResult;
	
	private DataModel timePeriodModel;
	
	final private static int SORT_BY_ID = 0;
	final private static int SORT_BY_NAME = 1;
	final private static int SORT_BY_ALIAS = 2;
	private boolean ascending = true;
	private int sortBy = 1;
	
	/* Constructor */
	
	public TimePeriodHandler()
	{
		timePeriod = new TimePeriod();
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.TIMEPERIOD);
		paginator = new Paginator(fileStore.getObjectHashMap());		
	}
	
	
	public void setTimePeriod(TimePeriod timePeriod)
	{
		this.timePeriod = timePeriod;
	}
	
	public TimePeriod getTimePeriod()
	{
		return this.timePeriod;
	}
	
	public void setModifyTimePeriod(TimePeriod modifyTimePeriod)
	{
		this.modifyTimePeriod = modifyTimePeriod;
	}
	
	public TimePeriod getModifyTimePeriod()
	{
		return this.modifyTimePeriod;
	}
	
	public int getTemplateCount()
	{
		return fileStore.getTemplateCount(ObjectXMLBuilder.TIMEPERIOD);
	}
	
	public void setTemplateToLoad(int templateToLoad)
	{
		this.templateToLoad = templateToLoad;
	}
	
	public int getTemplateToLoad()
	{
		return this.templateToLoad;
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
		return firstRowIndex >= fileStore.getObjectCount(ObjectXMLBuilder.TIMEPERIOD) - paginator.getNumberOfRows();
	}
	
	public synchronized String addTimePeriod()
	{
		String result;
		
		result = fileStore.addObject(timePeriod,ObjectXMLBuilder.TIMEPERIOD);
		
		if(result.equals("add-success"))
		{
			timePeriod = new TimePeriod();
			
			if(Utils.inWizard())
				return "wizard-timeperiod-add";
			
			return result;
		}
		
		timePeriod.setId(-1);
		timePeriod.setIsModifiable(false);
		return result;
		
	}
	
	public synchronized String modifyTimePeriod()
	{
		String result;
		
		result = fileStore.modifyObject(modifyTimePeriod,ObjectXMLBuilder.TIMEPERIOD);
		
		if(result.equals("modify-success"))
			modifyTimePeriod = new TimePeriod();
		
		return result;
	}
	
	/*
	 * Method that is used to delete a TimePeriod. The id of the timePeriod to delete
	 * is sent as part of the request parameter map. Using the current FacesContext,
	 * retrieve the id, and then use the XMLObjectLocator to delete the specified object.
	 */
	
	public synchronized void deleteTimePeriod(ActionEvent e)
	{
		int objectId;
		String result;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			objectId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("objectId"));
		}
		catch(Exception ex)
		{
			delResult = "failure";
			return;
		}
		
		result = fileStore.deleteObject(objectId,ObjectXMLBuilder.TIMEPERIOD);
		delResult = result;
		
		if(result.equals("delete-success"))
			timePeriod = new TimePeriod();
	}
	
	public synchronized String addTemplate()
	{
		String result;
		
		result = fileStore.addTemplate(timePeriod,ObjectXMLBuilder.TIMEPERIOD);
		
		if(result.equals("success"))
		{
			timePeriod = new TimePeriod();
			return result;
		}
		
		timePeriod.setId(-1);
		timePeriod.setIsTemplate(false);
		timePeriod.setIsTemplateModifiable(false);
		
		return result;
		
	}
	
	public synchronized String useTemplate()
	{
		TimePeriod templateTimePeriod = (TimePeriod)fileStore.loadTemplateById(Integer.valueOf(templateToLoad));
		
		if(templateTimePeriod !=null)
		{
			timePeriod = new TimePeriod(templateTimePeriod);
			timePeriod.setIsTemplate(false);
			timePeriod.setIsModifiable(false);
			timePeriod.setIsTemplateModifiable(false);
			timePeriod.setId(-1);
			return "success";
		}
		
		return "failure";
		
		
	}
	
	public List<SelectItem> getTemplateNames()
	{
		List<SelectItem> templateNames = new ArrayList<SelectItem>();
		List<BlueObject> list = fileStore.getStoredTemplates(ObjectXMLBuilder.TIMEPERIOD);
		
		for(BlueObject b : list)
		{
			SelectItem item = new SelectItem(b.getId(),b.getName());
			templateNames.add(item);
		}
		
		return templateNames;
	}
	
	public int getTimePeriodCount()
	{
		return fileStore.getObjectCount(ObjectXMLBuilder.TIMEPERIOD);
	}
	
	public String sortById()
	{
		if(sortBy == SORT_BY_ID)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_ID;
			ascending = true;
		}
		
		return "success";
	}
	
	public String sortByName()
	{
		if(sortBy == SORT_BY_NAME)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_NAME;
			ascending = true;
		}
		
		return "success";
	}
	
	public String sortByAlias()
	{
		if(sortBy == SORT_BY_ALIAS)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_ALIAS;
			ascending = true;
		}
		
		return "success";
	}
	
	public DataModel getSortedTimePeriodData()
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.TIMEPERIOD);
		sortList(list);
		
		timePeriodModel = new ListDataModel(list);
		return timePeriodModel;
	}
	
	public DataModel searchByTimePeriodName(String timePeriodName)
	{
		return new ListDataModel(fileStore.searchByObjectName(timePeriodName,ObjectXMLBuilder.TIMEPERIOD));
	}
	
	public synchronized String select()
	{
		try
		{
			modifyTimePeriod = (TimePeriod)timePeriodModel.getRowData();
		}
		catch(Exception e)
		{
			return "failure";
		}
		
		return "select-timeperiod";
	}
	
	public List getTimePeriodNames()
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.TIMEPERIOD);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject t: list)
		{
			SelectItem item = new SelectItem(t.getName(),t.getName());
			items.add(item);
		}
		
		return items;
		
	}
	
	public synchronized String loadTimePeriodById(ActionEvent e)
	{
		int timePeriodId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			timePeriodId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("timeperiod_id"));
			
			modifyTimePeriod = (TimePeriod)fileStore.loadObjectById(timePeriodId,ObjectXMLBuilder.TIMEPERIOD);
			selectResult = "select-timeperiod";
			return "success";
		}
		catch(Exception ex)
		{
			return "failure";
		}
		
		
	}
	
	public synchronized String selectResult()
	{
		return this.selectResult;
	}
	/*---------------- END OF PUBLIC METHODS --------------------*/
	
	/* ----------------COMPARATORS ---------------------*/
	
	private static final Comparator ASC_ID_COMPARATOR = new Comparator(){
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((TimePeriod)o1).getId());
			String id2 = String.valueOf(((TimePeriod)o2).getId());
			
			return id1.compareTo(id2);
		}
	};
	
	/* Service ID - DESC */
	private static final Comparator DESC_ID_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((TimePeriod)o1).getId());
			String id2 = String.valueOf(((TimePeriod)o2).getId());
			
			return id2.compareTo(id1);
		}
	};
	
	private static final Comparator DESC_NAME_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((TimePeriod)o1).getName());
			String id2 = String.valueOf(((TimePeriod)o2).getName());
			
			return id2.compareTo(id1);
		}
	};
	
	private static final Comparator ASC_NAME_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((TimePeriod)o1).getName());
			String id2 = String.valueOf(((TimePeriod)o2).getName());
			
			return id1.compareTo(id2);
		}
	};
	
	private static final Comparator ASC_ALIAS_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((TimePeriod)o1).getAlias());
			String id2 = String.valueOf(((TimePeriod)o2).getAlias());
			
			return id1.compareTo(id2);
		}
	};
	
	private static final Comparator DESC_ALIAS_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((TimePeriod)o1).getAlias());
			String id2 = String.valueOf(((TimePeriod)o2).getAlias());
			
			return id2.compareTo(id1);
		}
	};
	
	
	/*------------------ END COMPARATORS ---------------*/
	/*---------------- PRIVATE METHODS -------------------------*/
	
	private void sortList(List list)
	{
		switch(sortBy)
		{
			case SORT_BY_ID:
				Collections.sort(list,ascending ? ASC_ID_COMPARATOR : DESC_ID_COMPARATOR);
				break;
			
			case SORT_BY_NAME:
				Collections.sort(list,ascending ? ASC_NAME_COMPARATOR : DESC_NAME_COMPARATOR);
				break;
			
			case SORT_BY_ALIAS:
				Collections.sort(list,ascending ? ASC_ALIAS_COMPARATOR : DESC_ALIAS_COMPARATOR);
				break;
		}
	}
	
}
