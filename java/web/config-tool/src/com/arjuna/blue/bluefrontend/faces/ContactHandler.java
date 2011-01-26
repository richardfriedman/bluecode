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

public class ContactHandler
{
	private Contact contact;
	private Contact modifyContact;
	private BlueConfigXMLFileStore fileStore;
	
	private FacesContext context;
	private Paginator paginator;
	
	private int templateToLoad;
		
	final private static int SORT_BY_CONTACTNAME = 1;
	final private static int SORT_BY_ALIAS = 2;
	final private static int SORT_BY_EMAIL = 3;
	private String delResult;
	private String selectResult;
	
	private int firstRowIndex;
	
	/* Default to ascended contactName sorting */
	private boolean ascending = true;
	private int sortBy = 1;
	
	private DataModel contactDataModel;
	
	public ContactHandler()
	{
		contact = new Contact();
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.CONTACT);
		paginator = new Paginator(fileStore.getObjectHashMap());
		
	}
	
	public void setContact(Contact contact)
	{
		this.contact = contact;
	}
	
	public Contact getContact()
	{
		return this.contact;
	}
	
	public void setModifyContact(Contact modifyContact)
	{
		this.modifyContact = modifyContact;
	}
	
	public Contact getModifyContact()
	{
		return this.modifyContact;
	}
	
	public int getTemplateCount()
	{
		return fileStore.getTemplateCount(ObjectXMLBuilder.CONTACT);
	}
	
	public int getTemplateToLoad()
	{
		return this.templateToLoad;
	}
	
	public void setTemplateToLoad(int templateToLoad)
	{
		this.templateToLoad = templateToLoad;
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
		return firstRowIndex >= fileStore.getObjectCount(ObjectXMLBuilder.CONTACT) - paginator.getNumberOfRows();
	}
	
	public synchronized String addContact()
	{
		String result;
		
		result = fileStore.addObject(contact,ObjectXMLBuilder.CONTACT);
		
		if(result.equals("add-success"))
		{
			contact = new Contact();
			
			if(Utils.inWizard())
				return "wizard-contact-add";
			
			return result;
		}
		
		contact.setId(-1);
		contact.setIsModifiable(false);
		return result;
	}
	
	
	public synchronized String addTemplate()
	{
		String result;
		
		result = fileStore.addTemplate(contact,ObjectXMLBuilder.CONTACT);
		
		if(result.equals("success"))
		{
			contact = new Contact();
			return result;
		}
		
		contact.setId(-1);
		contact.setIsTemplate(false);
		contact.setIsModifiable(false);
		contact.setIsTemplateModifiable(false);
		return result;
	}
	
	public synchronized String useTemplate()
	{
		Contact templateContact = (Contact)fileStore.loadTemplateById(templateToLoad);
		
		if(templateContact !=null)
		{
			contact = new Contact(templateContact);
			contact.setId(-1);
			contact.setIsModifiable(false);
			contact.setIsTemplate(false);
			contact.setIsTemplateModifiable(false);
			return "success";
		}
		
		return "failure";
	}
	
	/*
	 * Method to modify a contact.
	 * 
	 * @return, String "success" if the operation was successful.
	 */
	
	public synchronized String modifyContact()
	{
		String result;
		
		result = fileStore.modifyObject(modifyContact,ObjectXMLBuilder.CONTACT);
		
		if(result.equals("modify-success"))
			modifyContact = new Contact();
		
		return result;
	}
	
	public synchronized void deleteContact(ActionEvent e)
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
		
		result = fileStore.deleteObject(objectId,ObjectXMLBuilder.CONTACT);
		if(result.equals("delete-success"))
			contact = new Contact();
		
		delResult = result;
		
	}
	
	public DataModel getSortedContactDetails()
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.CONTACT);
		sortContacts(list);
		contactDataModel = new ListDataModel(list);
				
		return contactDataModel;
	}
	
	public List<SelectItem> getTemplateNames()
	{
		List<BlueObject> list = fileStore.getStoredTemplates(ObjectXMLBuilder.CONTACT);
		List<SelectItem> templateNames = new ArrayList<SelectItem>();
		
		for(BlueObject c: list)
		{
			SelectItem item = new SelectItem(c.getId(),c.getName());
			templateNames.add(item);
		}
		return templateNames;
	}
	
	public String sortByName()
	{
		if(sortBy == SORT_BY_CONTACTNAME)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_CONTACTNAME;
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
	
	public String sortByEmailAddress()
	{
		if(sortBy == SORT_BY_EMAIL)
			ascending = !ascending;
		else
		{
			sortBy = SORT_BY_EMAIL;
			ascending = true;
		}
		
		return "success";
	}
	
	public DataModel searchByContactName(String contactName)
	{
		return new ListDataModel(fileStore.searchByObjectName(contactName,ObjectXMLBuilder.CONTACT));
	}
	
	public int getContactCount()
	{
		return fileStore.getObjectCount(ObjectXMLBuilder.CONTACT);
	}
	
	public synchronized String select()
	{
		try
		{
			modifyContact = (Contact)contactDataModel.getRowData();
		}
		catch(Exception e)
		{
			return "failure";
		}
		
		return "select-contact";
	}
	
	public String selectResult()
	{
		return this.selectResult;
	}
	
	public List getContactNames()
	{
		List<BlueObject> list = fileStore.getStoredObjects(ObjectXMLBuilder.CONTACT);
		List<SelectItem> items = new ArrayList<SelectItem>();
		
		for(BlueObject c: list)
		{
			SelectItem item = new SelectItem(c.getName(),c.getName());
			items.add(item);
		}
		
		return items;
	}
	
	public synchronized String loadContactById(ActionEvent e)
	{
		int contactId;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			contactId = Integer.valueOf((String)context.getExternalContext().getRequestParameterMap().get("contact_id"));
			
			modifyContact = (Contact)fileStore.loadObjectById(contactId,ObjectXMLBuilder.CONTACT);
			selectResult = "select-contact";
			return "success";
		}
		catch(Exception ex)
		{
			return "failure";
		}
		
		
	}
	
	/*----------------- END OF PUBLIC METHODS ----------------------*/
	
	/*----------------- COMPARATORS --------------------------------*/
	 
	/* HOSTNAME - ASC */
	private static final Comparator ASC_CONTACTNAME_COMPARATOR = new Comparator(){
		public int compare(Object o1,Object o2)
		{
			String host1 = ((Contact)o1).getContactName();
			String host2 = ((Contact)o2).getContactName();
			
			return host1.compareTo(host2);
		}
	};
	
	/* HOSTNAME - DESC */
	private static final Comparator DESC_CONTACTNAME_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Contact)o1).getContactName();
			String host2 = ((Contact)o2).getContactName();
			
			return host2.compareTo(host1);
		}
		
	};
	
	/* ALIAS - ASC */
	private static final Comparator ASC_ALIAS_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Contact)o1).getAlias();
			String host2 = ((Contact)o2).getAlias();
			
			return host1.compareTo(host2);
		}
		
	};
	
	
	/* ALIAS - DESC */
	private static final Comparator DESC_ALIAS_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Contact)o1).getAlias();
			String host2 = ((Contact)o2).getAlias();
			
			return host2.compareTo(host1);
		}
		
	};
	
	/* HOSTNAME - ASC */
	private static final Comparator ASC_EMAIL_COMPARATOR = new Comparator(){
		public int compare(Object o1,Object o2)
		{
			String host1 = ((Contact)o1).getEmail();
			String host2 = ((Contact)o2).getEmail();
			
			return host1.compareTo(host2);
		}
	};
	
	/* HOSTNAME - DESC */
	private static final Comparator DESC_EMAIL_COMPARATOR = new Comparator(){
		public int compare(Object o1, Object o2)
		{
			String host1 = ((Contact)o1).getEmail();
			String host2 = ((Contact)o2).getEmail();
			
			return host2.compareTo(host1);
		}
		
	};
	
	 
	/*----------------- END OF COMPARATORS -------------------------*/
	
	/*----------------- PRIVATE METHODS ----------------------------*/
	
	private void sortContacts(List contactList)
	{
		switch(sortBy)
		{
			case SORT_BY_CONTACTNAME:
				Collections.sort(contactList,ascending ? ASC_CONTACTNAME_COMPARATOR : DESC_CONTACTNAME_COMPARATOR);
				break;
			
			case SORT_BY_ALIAS:
				Collections.sort(contactList,ascending ? ASC_ALIAS_COMPARATOR : DESC_ALIAS_COMPARATOR);
				break;
				
			case SORT_BY_EMAIL:
				Collections.sort(contactList,ascending ? ASC_EMAIL_COMPARATOR : DESC_EMAIL_COMPARATOR);
				break;
		}
	}
}
