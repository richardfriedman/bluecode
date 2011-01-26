/*
 * Experimental class to see if we can complete searching in a satisfactory manner
 */

package com.arjuna.blue.bluefrontend.faces;

import java.util.List;
import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

public class SearchHandler
{
	
	final private static int SEARCH_HOSTS = 0;
	final private static int SEARCH_HOSTGROUPS = 1;
	final private static int SEARCH_SERVICES = 2;
	final private static int SEARCH_SERVICEGROUPS = 3;
	final private static int SEARCH_CONTACTS = 4;
	final private static int SEARCH_CONTACTGROUPS = 5;
	final private static int SEARCH_TIMEPERIODS = 6;
	final private static int SEARCH_COMMANDS = 7;
	
	private String searchString;
	private int[] searchTypes;
	private List<String> searchHistory;
	private DataModel recentSearches;
	private int objectSearchFor;
	private boolean searchSet = false;
	
	private FacesContext context;
	
	/* DataModel to store the results of a specific object search */
	private DataModel searchModel;
	
	public SearchHandler()
	{
		searchHistory = new ArrayList<String>();
	}
	
	public SearchHandler(SearchHandler s)
	{
		this.searchString = s.searchString;
		this.searchTypes = s.searchTypes;
		this.searchHistory = s.searchHistory;
	}
	
	public void setSearchString(String searchString)
	{
		this.searchString = searchString;
						
	}
	
	public String getSearchString()
	{
		return this.searchString;
	}
	
	public void setSearchTypes(int[] searchTypes)
	{
		this.searchTypes = searchTypes;
	}
	
	public int[] searchTypes()
	{
		return this.searchTypes;
	}
	
	public void setSearchHistory(List<String> searchHistory)
	{
		this.searchHistory = searchHistory;
	}
	
	public List getSearchHistory()
	{
		return this.searchHistory;
	}

	public DataModel getRecentSearches()
	{
		recentSearches = new ListDataModel(searchHistory);
		return recentSearches;
	}
	
	/*
	 *  Returns the number of searches that have been recently completed.
	 *  
	 *  @return = int, the number of searches that have been recently completed.
	 */
	
	public int getSearchCount()
	{
		return searchHistory.size();
	}
	
	
	/* Setup the search variables */
		
	public void setSearchType(ActionEvent e)
	{
		try
		{
			objectSearchFor = Integer.valueOf((String)e.getComponent().getAttributes().get("searchType"));
			searchSet = true;
		}
		catch(NumberFormatException f)
		{
			searchSet = false;
		}
		
	}
	
	public String searchObjects()
	{
		/* Make sure we received and correctly set searchType parameter */
		context = FacesContext.getCurrentInstance();
		
		if(!searchSet)
		{
			return "failure";
		}
		
		switch(objectSearchFor)
		{
		
		case SEARCH_HOSTS:
			HostHandler hhandler = (HostHandler)context.getExternalContext().getApplicationMap().get("hostHandler");
			if(hhandler==null) return "failure";
			
			searchModel = hhandler.searchByHostName(searchString);
			searchHistory.add(searchString);
			break;
		
		case SEARCH_HOSTGROUPS:
			GroupHandler hghandler = (GroupHandler)context.getExternalContext().getApplicationMap().get("groupHandler");
			if(hghandler == null) return "failure";
			
			searchModel = hghandler.searchByGroupName(searchString,Group.HOSTGROUP);
			searchHistory.add(searchString);
			break;
			
		case SEARCH_SERVICES:
			ServiceHandler shandler = (ServiceHandler)context.getExternalContext().getApplicationMap().get("serviceHandler");
			if(shandler == null) return "failure";
			
			searchModel = shandler.searchByServiceDescription(searchString);
			searchHistory.add(searchString);
			break;
		
		case SEARCH_SERVICEGROUPS:
			GroupHandler sghandler = (GroupHandler)context.getExternalContext().getApplicationMap().get("groupHandler");
			if(sghandler == null) return "failure";
			
			searchModel = sghandler.searchByGroupName(searchString, Group.SERVICEGROUP);
			searchHistory.add(searchString);
			break;
			
		case SEARCH_COMMANDS:
			CommandHandler chandler = (CommandHandler)context.getExternalContext().getApplicationMap().get("commandHandler");
			if(chandler==null) return "failure";
			
			searchModel = chandler.searchByCommandName(searchString);
			searchHistory.add(searchString);
			break;
		
		case SEARCH_CONTACTS:
			ContactHandler cohandler = (ContactHandler)context.getExternalContext().getApplicationMap().get("contactHandler");
			if(cohandler == null) return "failure";
			
			searchModel = cohandler.searchByContactName(searchString);
			searchHistory.add(searchString);
			break;
		
		case SEARCH_CONTACTGROUPS:
			GroupHandler cghandler = (GroupHandler)context.getExternalContext().getApplicationMap().get("groupHandler");
			if(cghandler == null) return "failure";
			
			searchModel = cghandler.searchByGroupName(searchString,Group.CONTACTGROUP);
			searchHistory.add(searchString);
			break;
				
		case SEARCH_TIMEPERIODS:
			TimePeriodHandler tpHandler = (TimePeriodHandler)context.getExternalContext().getApplicationMap().get("timePeriodHandler");
			if(tpHandler == null) return "failure";
			
			searchModel = tpHandler.searchByTimePeriodName(searchString);
			searchHistory.add(searchString);
			break;
		}
		
		return "success";
		
	}
	
	/* Returns the numeric identifier of the object type we just searched for 
	 *
	 * @return = int, the type of the object that we just searched for.
	 */
	
	public int getObjectSearchFor()
	{
		return this.objectSearchFor;
	}
	
	/* return the results of the current object search */
	
	public DataModel getObjectSearchResults()
	{
		return searchModel;
	}
	
}
