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

public class ServiceHandler 
{
	/* utility Objects */
	private FacesContext context;
	private Paginator paginator;
	
	private Service service;
	private Service modifyService;
	private DataModel serviceDataModel;
	private BlueConfigXMLFileStore fileStore;
		
	/* Utility Variables */
	private int templateToLoad;
	
	private String delResult;
	private String loadResult;
	
	private int firstRowIndex;
	
	/* Sorting parameters */
	final private static int SORT_BY_HOSTNAME = 1;
	final private static int SORT_BY_SERVICEDESCRIPTION = 2;
	
	private boolean ascending = true;
	private int sortBy = 1;
	
	
	/* Constructor */
	
	public ServiceHandler()
	{
		service = new Service();
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.SERVICE);
		paginator = new Paginator(fileStore.getObjectHashMap());
	}
	
	/*
	 *  Get the current template count;
	 *  
	 *  @return = int, the current template count.
	 */
	
	public int getTemplateCount()
	{
		return fileStore.getTemplateCount(ObjectXMLBuilder.SERVICE);
	}
	
	/*
	 * Set the current service instance.
	 * 
	 * @param = Service, service.
	 */
	
	public void setService(Service service)
	{
		this.service = service;
	}
	
	/*
	 * Get the current Service instance.
	 * 
	 * @return = Service, the current service instance.
	 */
	
	public Service getService()
	{
		return this.service;
	}
	
	public void setModifyService(Service modifyService)
	{
		this.modifyService = modifyService;
	}
	
	public Service getModifyService()
	{
		return this.modifyService;
	}
	
	/*
	 * Retrieve the id of the template to load.
	 * 
	 * @return = int, id of the template you wish to load.
	 */
	
	public int getTemplateToLoad()
	{
		return this.templateToLoad;
	}
	
	/*
	 * Set the id of the template to load.
	 * 
	 * @param = int templateToLoad, the id of the template to Load.
	 */
	
	public void setTemplateToLoad(int templateToLoad)
	{
		this.templateToLoad = templateToLoad;
	}
	
	/*
	 * Get the result of the del command. This is required to allow JSF navigations rules
	 * to work correctly. 
	 * 
	 * @return = String, the result of the delete command.
	 */
	
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
		return firstRowIndex >= fileStore.getObjectCount(ObjectXMLBuilder.SERVICE) - paginator.getNumberOfRows();
	}
	
	public synchronized String addService()
	{
		String result;
		
		if(checkServiceName(service.getServiceDescription(),service.getHostname()))
		{
			return "failure";
		}
		
		result = fileStore.addObject(service,ObjectXMLBuilder.SERVICE);
		
		if(result.equals("add-success"))
		{
			service = new Service();
			
			if(Utils.inWizard())
				return "wizard-service-add";
			
			return result;
		}
		
		service.setId(-1);
		service.setIsModifiable(false);
		return result;
		
	}
	
	public synchronized String addTemplate()
	{
		String result;
		
		if(checkTemplateNameExists(service.getServiceDescription()))
			return "failure";
				
		result = fileStore.addTemplate(service,ObjectXMLBuilder.SERVICE);
		
		if(result.equals("success"))
			service = new Service();
		
		return result;
		
	}
	
	/*
	 * Method to delete a service. This method works out which service is to be deleted by
	 * pulling out the f:param value from the current FacesContext.
	 * 
	 * @return = String, "success" if the operation was successful.
	 */
	
	public synchronized void deleteService(ActionEvent e)
	{
		int objectId;
		String result;
		try
		{
			context = FacesContext.getCurrentInstance();
			objectId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("objectId"));
		}
		catch(Exception eX)
		{
			delResult = "failure";
			return;
		}
		
		result = fileStore.deleteObject(objectId,ObjectXMLBuilder.SERVICE);
		delResult = result;
		
		if(result.equals("delete-success"))
			service = new Service();
	}
	
	public synchronized String useTemplate()
	{
		Service templateService = (Service)fileStore.loadTemplateById(Integer.valueOf(templateToLoad));
		
		if(templateService !=null)
		{
			service = new Service(templateService);
			service.setId(-1);
			service.setIsModifiable(false);
			service.setIsTemplate(false);
			service.setIsTemplateModifiable(false);
			return "success";
		}
		
		return "failure";
	}
	
	/*
	 *	Method to retrieve the current list of Template names within the ServiceHandler class.
	 *	
	 *	@return = List<SelectItem>, a list containing select items with attribute/value set to current
	 *			  template names. 
	 */
	
	public List<SelectItem> getTemplateNames()
	{
		
		List<SelectItem> templateNames = new ArrayList<SelectItem>();
		List<BlueObject> list = fileStore.getStoredTemplates(ObjectXMLBuilder.SERVICE);
				
		for(BlueObject b: list)
		{
			Service s = (Service)b;
		
			SelectItem item = new SelectItem(s.getId(),s.getServiceDescription());
			templateNames.add(item);
		}
		
		return templateNames;
	}
	
	/*
	 * Modify a service
	 * 
	 * @return = String, "modify-success" if the operation was successful.
	 */
	
	public synchronized String modifyService()
	{
		String result = fileStore.modifyObject(modifyService,ObjectXMLBuilder.SERVICE);
		
		if(result.equals("modify-success"))
			modifyService = new Service();
		
		return result;

	}
	
	/*
	 * Method to configure the environment to sort the Service DataModel by Service Hostname.
	 * 
	 * @return = String, "success" if the operation was successful.
	 */
	
	public String sortByHostname()
	{
		if(sortBy == SORT_BY_HOSTNAME)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_HOSTNAME;
			ascending = true;
		}
		
		return "success";
		
	}
	
	/*
	 * Method to configure the environment to sort the Service DataModel by Service Description
	 * 
	 * @return = String, "success" if the operation was successful.
	 */
	public String sortByServiceDescription()
	{
		if(sortBy == SORT_BY_SERVICEDESCRIPTION)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_SERVICEDESCRIPTION;
			ascending = true;
		}
		
		return "success";
	}
	
	/*
	 * Method for retrieving a sorted Data Model of all current Service Data.
	 * 
	 * @return = DataModel, a sorted DataModel of all current service Data.
	 */
	
	public DataModel getSortedServiceData()
	{
		List<BlueObject> list = /*getServices();*/fileStore.getStoredObjects(ObjectXMLBuilder.SERVICE);
		sortList(list);
		
		serviceDataModel = new ListDataModel(list);
		return serviceDataModel;
	}
	
	public int getServiceCount()
	{
		return fileStore.getObjectCount(ObjectXMLBuilder.SERVICE);
	}
	
	/* Search for service by Service Description */
	public DataModel searchByServiceDescription(String serviceDescription)
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.SERVICE);
		List<Service> searchList = new ArrayList<Service>();
		
		for(BlueObject b: list)
		{
			Service s = (Service)b;
			
			if(s.getServiceDescription().toLowerCase().contains(serviceDescription.toLowerCase()))
			{
				searchList.add(s);
			}
		}
		
		return new ListDataModel(searchList);
		
	}
	
	public synchronized String select()
	{
		try
		{
			modifyService = (Service)serviceDataModel.getRowData();
		}
		catch(Exception e)
		{
			return "failure";
		}
		
		return "select-service";
	}
	
	
	/* Retrieve list of current Service names */
	
	public List getServiceNames()
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.SERVICE);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject b: list)
		{
			Service s = (Service)b;
			SelectItem item = new SelectItem(s.getServiceDescription(),s.getServiceDescription());
			items.add(item);
		}
	
		return items;
	}
	
	public List<SelectItem> getServiceHostNames()
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.SERVICE);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject b: list)
		{
			Service s = (Service)b;
			for(String st : s.getHostname())
			{
				SelectItem item = new SelectItem(st + "," + s.getServiceDescription(),st + " - " + s.getServiceDescription());
				items.add(item);
			}
		}
		
		return items;
	}
	
	public synchronized String loadServiceById(ActionEvent e)
	{
		int serviceId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			serviceId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("service_id"));
			
			modifyService = (Service)fileStore.loadObjectById(serviceId,ObjectXMLBuilder.SERVICE);
			loadResult = "select-service";
			return "success";
		}
		catch(Exception ex)
		{
			return "failure";
		}
	}
	
	public synchronized String selectResult()
	{
		return this.loadResult;
	}
	
	/*--------------------- END OF PUBLIC METHODS ------------------*/
	
	/*-------------------- COMPARATORS ------------------------*/
	
	/* Hostname Desc */
	private static final Comparator DESC_HOSTNAME_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Service)o1).getHostname());
			String id2 = String.valueOf(((Service)o2).getHostname());
			
			return id2.compareTo(id1);
		}
	};
	
	private static final Comparator ASC_HOSTNAME_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Service)o1).getHostname());
			String id2 = String.valueOf(((Service)o2).getHostname());
			
			return id1.compareTo(id2);
		}
	};
	
	private static final Comparator ASC_SERVICEDESCRIPTION_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Service)o1).getServiceDescription());
			String id2 = String.valueOf(((Service)o2).getServiceDescription());
			
			return id1.compareTo(id2);
		}
	};
	
	private static final Comparator DESC_SERVICEDESCRIPTION_COMPARATOR = new Comparator(){
		
		public int compare(Object o1,Object o2)
		{
			String id1 = String.valueOf(((Service)o1).getServiceDescription());
			String id2 = String.valueOf(((Service)o2).getServiceDescription());
			
			return id2.compareTo(id1);
		}
	};
	
	/*------------------ END COMPARATORS -------------------*/
	
	/* Ideally we don't want a service with the same name duplicated on a host */
	
	private boolean checkServiceName(String serviceName,String[] hostName)
	{
		List<BlueObject> list =fileStore.getStoredObjects(ObjectXMLBuilder.SERVICE);	
		
		
		for(BlueObject b: list)
		{
			Service s = (Service)b;
			
			if(s.getServiceDescription().equals(serviceName))
				for(String st: hostName)
					if(st.equals(hostName))
						return true;
		}
		
		return false;
	}
	
	
	/*
	 * Method used to verify that we are not adding two templates of the same name!
	 * 
	 * @param = String, service description of the template that you are trying to store.
	 * 
	 * @return = boolean, true if a service template with that description already exists.
	 */
	
	private boolean checkTemplateNameExists(String serviceDescription)
	{
		List<BlueObject> list = fileStore.getStoredTemplates(ObjectXMLBuilder.SERVICE);
		
		for(BlueObject b : list)
		{
			Service s = (Service)b;
			
			if(s.getServiceDescription().equalsIgnoreCase(serviceDescription))
				return true;
		}
		
		return false;
	}
	
	/*
	 * Sort the current collection for return.
	 * 
	 * @return = list, the current lsit of services that has been sorted.
	 */
	private void sortList(List list)
	{
		switch(sortBy)
		{
			case SORT_BY_HOSTNAME:
				Collections.sort(list,ascending ? ASC_HOSTNAME_COMPARATOR : DESC_HOSTNAME_COMPARATOR);
				break;
			
			case SORT_BY_SERVICEDESCRIPTION:
				Collections.sort(list,ascending ? ASC_SERVICEDESCRIPTION_COMPARATOR : DESC_SERVICEDESCRIPTION_COMPARATOR);
				break;
		}
		
		
	}
	
}
