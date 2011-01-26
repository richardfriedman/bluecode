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

public class HostHandler
{
	/* private variables */
	private Host host;
	private Host modifyHost;
		
	private Paginator paginator;
	private BlueConfigXMLFileStore fileStore;
	
	private DataModel hostsModel;
	private int templateToLoad;
	private String delResult;
	private String selectResult;
	private FacesContext context;
	
	private int firstRowIndex;
	
	/* Sorting methods */
	
	private static final int SORT_BY_HOSTNAME = 1;
	private static final int SORT_BY_ALIAS = 2;
	private static final int SORT_BY_IPADDRESS = 3;
	
	/* Defaults to sorting by hostname */
	private int sortBy = SORT_BY_HOSTNAME;
	
	/* Sort order, defaults to asc */
	private boolean ascending = true;
	
	/* Constructor */
	public HostHandler()
	{
		host = new Host();
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.HOST);
		paginator = new Paginator(fileStore.getObjectHashMap());
	}
		
	public Host getHost()
	{
		return this.host;
	}
	
	public void setHost(Host host)
	{
		this.host = host;
	}
	
	public void setModifyHost(Host modifyHost)
	{
		this.modifyHost = modifyHost;
	}
	
	public Host getModifyHost()
	{
		return this.modifyHost;
	}
	
	public synchronized String delResult()
	{
		return this.delResult;
	}
	
	public synchronized String selectResult()
	{
		return this.selectResult;
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
		return firstRowIndex >= fileStore.getObjectCount(ObjectXMLBuilder.HOST) - paginator.getNumberOfRows();
	}
	
	/*
	 * Method to add a host to the current HostList.
	 */
	
	public synchronized String addHost()
	{
		String result = fileStore.addObject(host,ObjectXMLBuilder.HOST);
		
		if(!result.equals("add-success"))
		{
			host.setId(-1);
			host.setIsModifiable(false);
			return result;
		}
		else
		{
			host = new Host();
		
			if(Utils.inWizard())
				return "wizard-host-add";
			
		return result;
		}
	}
	
	/* Method that stores a template
	 * 
	 *  @return = String, "success" if the operation was successful.
	 *  */
	public synchronized String addTemplate()
	{
		String result = fileStore.addTemplate(host,ObjectXMLBuilder.HOST);
		
		if(!result.equals("success"))
		{
			host.setId(-1);
			host.setIsTemplate(false);
			host.setIsTemplateModifiable(false);
			host.setIsModifiable(false);
			return result;
		}
		else
		{
			host = new Host();
			return result;
		}
		
	}
	
	/* Method that updates a Host */
	public synchronized String updateHost()
	{
		String result = fileStore.modifyObject(modifyHost,ObjectXMLBuilder.HOST);
		
		if(result.equals("modify-success"))
			modifyHost = new Host();
		
		return result;
	}
	
	
	public synchronized void deleteHost(ActionEvent e)
	{
		int objectId;
		String result;
		try
		{
			context = FacesContext.getCurrentInstance();
			objectId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("objectId"));
			result = fileStore.deleteObject(objectId,ObjectXMLBuilder.HOST);
		}
		catch(Exception ex)
		{
			delResult = "failure";
			return;
		}
		
		if(result.equals("delete-success"))
			host = new Host();
		
		delResult ="delete-success";
	}

	/* Returns the current number of hosts */
	
	public int getHostCount()
	{
		return fileStore.getObjectCount(ObjectXMLBuilder.HOST);
	}
	
	/* Returns the current number of templates */
	public int getTemplateCount()
	{
		return fileStore.getTemplateCount(ObjectXMLBuilder.HOST);
	}
	
	/* An Updated version of the current Hosts datamodel.
	 * Returns the data model in sorted format.
	 * 
	 * @return = Datamodel, a datamodel representation of the current sorted hostList.
	 */
	
	public DataModel getSortedHostDetails()
	{
		List<BlueObject> hosts = fileStore.getStoredObjects(ObjectXMLBuilder.HOST);			
		sortHosts(hosts);
		hostsModel = new ListDataModel(hosts);
		return hostsModel;		
	}
	
	
	/*
	 * Method that returns all current Template names.
	 */
	
	public List<SelectItem> getTemplateNames()
	{
		List<SelectItem> templateNames = new ArrayList<SelectItem>();
		List<BlueObject> templates = fileStore.getStoredTemplates(ObjectXMLBuilder.HOST);
		
		for(BlueObject b : templates)
		{
			SelectItem item = new SelectItem(b.getId(),b.getName());
			templateNames.add(item);
		}
		
		return templateNames;
	}
	
	/*
	 * Method to select a specific host. Utilises the ArrayDataModel getRowData() method to 
	 * work out which host you clicked on.
	 * 
	 * @return = String, success if the host was found.
	 */
	
	public synchronized String select()
	{
		try
		{
			modifyHost = (Host)hostsModel.getRowData();
		}
		catch(Exception e)
		{
			return "failure";
		}
		
		return "select-host";
	}
	
	/* Method to config internal parameters to sort the current hostlist by hostname 
	 * 
	 *	@return = String, success if the operation was successful. 
	 */ 
	
	public String sortByHostname()
	{
		if(sortBy == SORT_BY_HOSTNAME)
		{
			ascending = !ascending;
			
		}
		else
		{
			sortBy = SORT_BY_HOSTNAME;
			ascending = true;
		}
		
		return "success";
	}
	
	/* Method to config internal parameters to sort the current hostlist by alias 
	 * 
	 *	@return = String, success if the operation was successful. 
	 */
	
	public String sortByAlias()
	{
		if(sortBy == SORT_BY_ALIAS)
		{
			ascending = !ascending;
		}
		else
		{
			sortBy = SORT_BY_ALIAS;
			ascending = true;
		}
		
		return "success";
	}
	
	/* Method to config internal parameters to sort the current hostlist by IP Address 
	 * 
	 *	@return = String, success if the operation was successful. 
	 */
	
	public String sortByIPAddress()
	{
		if(sortBy == SORT_BY_IPADDRESS)
		{
			ascending = !ascending;
		}
		else
		{
			sortBy = SORT_BY_IPADDRESS;
			ascending = true;
		}
		
		return "success";
	}
	
	/*
	 * Perform searching of all host objects by hostname.
	 * 
	 *  @param = String hostname, the name of the hostname we are searching for.
	 *  
	 *  @return = Datamodel, a datamodel containing all possible matches.
	 */
	
	public DataModel searchByHostName(String hostname)
	{
		return new ListDataModel(fileStore.searchByObjectName(hostname,ObjectXMLBuilder.HOST));
	}
	
	/*
	 * Method that sets the id of the template that is to be loaded.
	 * 
	 * @param = int templateToLoad, the id of the template to Load.
	 */
	
	public void setTemplateToLoad(int templateToLoad)
	{
		this.templateToLoad = templateToLoad;
	}
	
	/*
	 * Return the value of the template to Load.
	 * 
	 * @return = int, id of the template to load.
	 */
	
	public int getTemplateToLoad()
	{
		return this.templateToLoad;
	}
	
	/*
	 *	Method that loads a selected template into the current host object.
	 *
	 * @return = String, "success" if the template has been properly loaded.
	 */
	
	public synchronized String useTemplate()
	{
		Host templateHost = (Host)fileStore.loadTemplateById(Integer.valueOf(templateToLoad));
		
		if(templateHost !=null)
		{
			templateHost.setId(-1);
			templateHost.setIsModifiable(false);
			templateHost.setIsTemplate(false);
			host = new Host(templateHost);
			return "success";
		}
		
		return "failure";
	}
	
	/*
	 * Method to return a list of the current available host names.
	 * 
	 * @return = List, list containing all available host names.
	 */
	
	public List getHostNames()
	{
		//List<Host> list = getHosts(false);
		List<SelectItem> hostNames = new ArrayList<SelectItem>();
		
		for(BlueObject b: fileStore.getStoredObjects(ObjectXMLBuilder.HOST))
		{
			SelectItem item = new SelectItem(b.getName(),b.getName());
			hostNames.add(item);
		}
		//}
		//return hostNames;
		return hostNames;
	}
	
	public synchronized String loadHostById(ActionEvent e)
	{
		int hostId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			hostId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("host_id"));
			
			modifyHost = /*hostList.get(hostId);*/(Host)fileStore.loadObjectById(hostId,ObjectXMLBuilder.HOST);
			selectResult = "select-host";
			return "select-host";
		}
		catch(Exception ex)
		{
			return "failure";
		}
	}
	
	/* ------- END OF PUBLIC METHODS ---------------------------*/
	
	/* ------- COMPARATORS FOR SORTING -------------------------*/
	
	/* HOSTNAME - ASC */
	private static final Comparator ASC_HOSTNAME_COMPARATOR = new Comparator(){
		public int compare(Object o1,Object o2)
		{
			String host1 = ((Host)o1).getHostname();
			String host2 = ((Host)o2).getHostname();
			
			return host1.compareTo(host2);
		}
	};
	
	/* HOSTNAME - DESC */
	private static final Comparator DESC_HOSTNAME_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Host)o1).getHostname();
			String host2 = ((Host)o2).getHostname();
			
			return host2.compareTo(host1);
		}
		
	};
	
	/* ALIAS - ASC */
	private static final Comparator ASC_ALIAS_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Host)o1).getAlias();
			String host2 = ((Host)o2).getAlias();
			
			return host1.compareTo(host2);
		}
		
	};
	
	
	/* ALIAS - DESC */
	private static final Comparator DESC_ALIAS_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Host)o1).getAlias();
			String host2 = ((Host)o2).getAlias();
			
			return host2.compareTo(host1);
		}
		
	};
	
	private static final Comparator ASC_IPADDRESS_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Host)o1).getIPAddress();
			String host2 = ((Host)o2).getIPAddress();
			
			return host1.compareTo(host2);
		}
		
	};
	
	private static final Comparator DESC_IPADDRESS_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Host)o1).getIPAddress();
			String host2 = ((Host)o2).getIPAddress();
			
			return host2.compareTo(host1);
		}
		
	};
	
	/* ------- END OF COMPARATORS ------------------------------*/
	
	/* ------- PRIVATE METHODS ---------------------------------*/
	
	/* Method to sort a given host list in a particular manner */
	private void sortHosts(List hostList)
	{
		switch(sortBy)
		{
			case SORT_BY_HOSTNAME:
				Collections.sort(hostList,ascending ? ASC_HOSTNAME_COMPARATOR : DESC_HOSTNAME_COMPARATOR);
				break;
			
			case SORT_BY_ALIAS:
				Collections.sort(hostList,ascending ? ASC_ALIAS_COMPARATOR : DESC_ALIAS_COMPARATOR);
				break;
			
			case SORT_BY_IPADDRESS:
				Collections.sort(hostList,ascending ? ASC_IPADDRESS_COMPARATOR : DESC_IPADDRESS_COMPARATOR);
				break;			
		}
	}
	
}
