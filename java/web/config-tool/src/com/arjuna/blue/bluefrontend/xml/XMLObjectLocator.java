package com.arjuna.blue.bluefrontend.xml;


import org.jdom.xpath.XPath;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;


import com.arjuna.blue.bluefrontend.faces.BlueObject;
import com.arjuna.blue.bluefrontend.faces.Command;
import com.arjuna.blue.bluefrontend.faces.Contact;
import com.arjuna.blue.bluefrontend.faces.Group;
import com.arjuna.blue.bluefrontend.faces.Host;
import com.arjuna.blue.bluefrontend.faces.HostDependency;
import com.arjuna.blue.bluefrontend.faces.HostEscalation;
import com.arjuna.blue.bluefrontend.faces.HostExtInfo;
import com.arjuna.blue.bluefrontend.faces.Macro;
import com.arjuna.blue.bluefrontend.faces.Service;
import com.arjuna.blue.bluefrontend.faces.ServiceDependency;
import com.arjuna.blue.bluefrontend.faces.ServiceEscalation;
import com.arjuna.blue.bluefrontend.faces.ServiceExtInfo;
import com.arjuna.blue.bluefrontend.faces.TimePeriod;
import com.arjuna.blue.bluefrontend.faces.Utils;

import java.util.List;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/*
 *	This class is used to return useful information about the data stored within
 *	the XML files associated with Blue configuration. 
 */

public class XMLObjectLocator
{
	
	/* definition of object search strings */
	private String[] objectSearches = new String[]{
			"hosts:hosts/hosts:host",
			"hostgroups:hostgroups/hostgroups:hostgroup",
			"services:services/services:service",
			"servicegroups:servicegroups/servicegroups:servicegroup",
			"contacts:contacts/contacts:contact",
			"contactgroups:contactgroups/contactgroups:contactgroup",
			"timeperiods:timeperiods/timeperiods:timeperiod",
			"commands:commands/commands:command",
			"servicedependencies:servicedependencies/servicedependencies:servicedependency",
			"serviceescalations:serviceescalations/serviceescalations:serviceescalation",
			"hostdependencies:hostdependencies/hostdependencies:hostdependency",
			"hostescalations:hostescalations/hostescalations:hostescalation",
			"hostextinfos:hostextinfos/hostextinfos:hostextinfo",
			"serviceextinfos:serviceextinfos/serviceextinfos:serviceextinfo"};
	
	private SAXBuilder builder = new SAXBuilder();
	private String outputDirectory = "output";
	
	/* Blank Constructor */
	public XMLObjectLocator()
	{
		
	}
	
	/* Constructor that sets the output Location from the word go */
	public XMLObjectLocator(String outputLocation)
	{
		this.outputDirectory = outputLocation;
	}
	
	/*
	 *	Set the output directory that this class writes XML to.
	 * 
	 *  @param = String outputDirectory, the name of the directory to write XML to.
	 */
	
	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}
	
	/*
	 * Get the output directory that this class currently writes to.
	 * 
	 * @return = String, the current directory that this class writes to.
	 */
	
	public String getOutputDirectory()
	{
		return this.outputDirectory;
	}
	
	/*
	 *  Method that counts the current number of a specifed object Type.
	 *  
	 *  @param = int objectType, the objectType you wish to search for.
	 *  
	 *  @return = int, the number of these objects currently defined.
	 */
	
	public int countObjectInstances(int objectType) throws JDOMException,IOException
	{
		outputDirectory = Utils.getCurrentOutputLocation();
		Document doc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
		XPath x = XPath.newInstance(objectSearches[objectType]);
				
		return x.selectNodes(doc).size();
	}
	
	/*
	 *	This method returns the details of a specifed Object. To identify the object you will
	 *	need to provide the objectType and the id identifier of said object.
	 *
	 * @param = int objectType, the objectType of the specified object.
	 * @param = int identifier, the "id" identifier of the object.
	 * 
	 * @return = List, the found object plus all associated child elements.
	 */
	
	public List getObjectDetails(int objectType,int identifier) throws JDOMException,IOException
	{
		outputDirectory = Utils.getCurrentOutputLocation();
		Document doc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
		XPath x = XPath.newInstance(objectSearches[objectType] + "[@id='" + identifier + "']");
				
		return x.selectNodes(doc);
	}
	
	/*
	 * This method is extremely similar to the getObjectDetails method, however it is used to 
	 * retrieve the details of a user defined macro rather than a conventional object.
	 * 
	 * @param = String macroIdentifer, the string id of the macro you wish to retrieve.
	 * 
	 * @return = List, the found macro object plus all associated child elements.
	 */
	
	public List getMacroDetails(String macroIdentifier)throws JDOMException,IOException
	{
		outputDirectory = Utils.getCurrentOutputLocation();
		Document doc = builder.build(outputDirectory + "/xml" + "/macros.xml");
		XPath x = XPath.newInstance("macros:macros/macros:macro[@id='" + macroIdentifier + "']");
		
		return x.selectNodes(doc);
	}
	
	/*
	 * Method to update the details of an individual object.
	 * 
	 * @param = int objectType, the type of the object you want to update.
	 * @param = int objectId, the id of the object you wish to update.
	 * @param = String elementName, the name of the element you wish to update.
	 * @param = String elementValue, the new value of the element you are updating.
	 * 
	 * @return = boolean, true if the operation succeeded.
	 *
	 */
	
	public boolean updateObjectDetails(int objectType,int objectId,HashMap objectProperties)throws JDOMException,IOException
	{
		Document doc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
		XPath x = XPath.newInstance(objectSearches[objectType] + "[@id='" + objectId + "']");
		
		Element selected = (Element)x.selectSingleNode(doc);
		
		if(selected != null)
		{	
			for(Object o : selected.getChildren())
			{
				Element e = (Element)o;
				
				try
				{
					e.setText(String.valueOf(objectProperties.get(e.getName())));
				}
				catch(Exception ef)
				{
				}
				
			}
			outputDirectory = Utils.getCurrentOutputLocation();
			writeXMLFile(doc,outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
			return true;
		
		}
		return false;
	}
	
	/*
	 * Method that can be used to delete a object.
	 * 
	 * @param = int objectType, the type of object that you are looking to delete.
	 * @param = int objectIdentifer, the identifer of the object that you are looking to delete.
	 * 
	 * @return = boolean, true if the operation succeeded.
	 */
	
	public boolean deleteObject(int objectType,int objectIdentifier)throws JDOMException,IOException
	{
		outputDirectory = Utils.getCurrentOutputLocation();
		Document doc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
		XPath x = XPath.newInstance(objectSearches[objectType] + "[@id='" + objectIdentifier + "']");
		
		Element selected  = (Element)x.selectSingleNode(doc);
		
		if(selected != null)
		{
			Element root = doc.getRootElement();
			root.removeContent(selected);
			writeXMLFile(doc,outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
			
			return true;
		}
		
		return false;
	}
	
	/*
	 * This method can be used to update the macro details created by the user.
	 * 
	 * @param = String macroId, the identifier of the macro you wish to update.
	 * @param = String macroValue, the new value of the macro.
	 * 
	 * @return = boolean, true if the operation was a success.
	 */
	
	public boolean updateMacroDetails(List macroDetails) throws JDOMException,IOException
	{
		outputDirectory = Utils.getCurrentOutputLocation();
		Document doc = builder.build(outputDirectory + "/xml/macros.xml");
		XPath x = XPath.newInstance("macros:macros/macros:macro[@macro_id='$USER" + macroDetails.get(0) + "$']");
		
		Element selected = (Element)x.selectSingleNode(doc);
		
		if(selected != null)
		{
			for(Object o : selected.getChildren())
			{
				Element e = (Element)o;
				
				if(e.getName().equals("macro_value"))
					e.setText(String.valueOf(macroDetails.get(1)));
			}
			writeXMLFile(doc,outputDirectory + "/xml/macros.xml");
			return true;
		}
			
		return false;
	}
	
	
	/*
	 * This method can be used to delete any of the macros that have been created by the user.
	 * 
	 * @param = String macroId, the id of the macro that you wish to delete.
	 * 
	 * @return = boolean, true if the operation was successful.
	 */
	
	public boolean deleteMacro(int macroId)throws JDOMException,IOException
	{
		outputDirectory = Utils.getCurrentOutputLocation();
		Document doc = builder.build(outputDirectory + "/xml/macros.xml");
		XPath x = XPath.newInstance("macros:macros/macros:macro[@macro_id='$USER" + macroId + "$']");
		
		Element selected  = (Element)x.selectSingleNode(doc);
			
		if(selected != null)
		{
			Element root = doc.getRootElement();
			root.removeContent(selected);
			writeXMLFile(doc,outputDirectory + "/xml/macros.xml");
			return true;
		}
		
		return false;		
	}
	
	/*
	 * Method to add a host to a specifed hostgroup.
	 * 
	 * @param = int hostgroupId, the id of the hostgroup that you wish to add the host to.
	 * @param = int hostId, the id of the host that you wish to add to the group.
	 * 
	 * @return = boolean, true if the operation was successful!.
	 */
	
	public boolean addObjectToObjectGroup(int objectgroupId,int objectId,int objectType)throws JDOMException,IOException
	{
		/* Setup initial variables */
		Document objectDoc = null;
		Document objectgroupDoc = null;
		Namespace namespace = null;
		Namespace gnamespace = null;
		int groupType = -1;
				
		if(objectType == ObjectXMLBuilder.HOST)
		{
			objectDoc = builder.build(Utils.xmlFileLocations[objectType]);
			objectgroupDoc = builder.build(Utils.xmlFileLocations[ObjectXMLBuilder.HOSTGROUP]);
			namespace = Utils.nameSpaces[objectType];
			gnamespace = Utils.nameSpaces[ObjectXMLBuilder.HOSTGROUP];
			groupType = ObjectXMLBuilder.HOSTGROUP;
		}
		else if(objectType == ObjectXMLBuilder.SERVICE)
		{
			objectDoc = builder.build(Utils.xmlFileLocations[objectType]);
			objectgroupDoc = builder.build(Utils.xmlFileLocations[ObjectXMLBuilder.SERVICEGROUP]);
			namespace = Utils.nameSpaces[objectType];
			gnamespace = Utils.nameSpaces[ObjectXMLBuilder.SERVICEGROUP];
			groupType = ObjectXMLBuilder.SERVICEGROUP;
		}	
		else if(objectType == ObjectXMLBuilder.CONTACTGROUP)
		{
			objectDoc = builder.build(Utils.xmlFileLocations[objectType]);
			objectgroupDoc = builder.build(Utils.xmlFileLocations[ObjectXMLBuilder.CONTACTGROUP]);
			namespace = Utils.nameSpaces[objectType];
			gnamespace = Utils.nameSpaces[ObjectXMLBuilder.CONTACTGROUP];
			groupType = ObjectXMLBuilder.CONTACTGROUP;
		}	
		
		/* Testing to see if we have a valid group option */
		if(groupType == -1)
		{
			return false;
		}
		
		XPath xog = XPath.newInstance(objectSearches[groupType] + "[@id='" + objectgroupId + "']");
		Element foundObjectgroup = (Element)xog.selectSingleNode(objectgroupDoc);
		String currentMembers = foundObjectgroup.getChildText("members",gnamespace);
						
		
		if(foundObjectgroup != null)
		{
			
			XPath x = XPath.newInstance(objectSearches[objectType] + "[@id='" + objectId + "']");
			Element foundObject = (Element)x.selectSingleNode(objectDoc);
					
			if(foundObject != null)
			{
				if(objectType == ObjectXMLBuilder.HOST)
				{
					foundObjectgroup.getChild("members",gnamespace).setText(currentMembers + "," + foundObject.getChildText("host_name",namespace));
					writeXMLFile(objectgroupDoc,Utils.xmlFileLocations[ObjectXMLBuilder.HOSTGROUP]);
					return true;
				}
				else if(objectType == ObjectXMLBuilder.SERVICE)
				{
					foundObjectgroup.getChild("members",gnamespace).setText(currentMembers + "," + foundObject.getChildText("service_description",namespace));
					writeXMLFile(objectgroupDoc,Utils.xmlFileLocations[ObjectXMLBuilder.SERVICEGROUP]);
					return true;
				}
				else if(objectType == ObjectXMLBuilder.CONTACT)
				{
					foundObjectgroup.getChild("members",gnamespace).setText(currentMembers + "," + foundObject.getChildText("contact_name",namespace));
					writeXMLFile(objectgroupDoc,Utils.xmlFileLocations[ObjectXMLBuilder.CONTACTGROUP]);
					return true;
				}
					
			}
			return false;
		}
		
		return false;
	}
	
	/*
	 * This method removes a specified object from a specified object group.
	 * 
	 * @param = int objectGroupId, the Id of the object group that you wish to remove this object from.
	 * @param = String objectIdentifier, the name of the object that you wish to remove from this group.
	 * @param = int objectType, the type of the object that you wish to remove from said group.
	 * 
	 * @return = boolean, true if the operation was successful!
	 */
	
	public boolean removeObjectFromObjectGroup(int objectgroupId,String objectIdentifier,int objectType) throws JDOMException,IOException
	{
		Document objectgroupDoc = null;
		Namespace namespace = null;
		int groupType = -1;
		
		if(objectType == ObjectXMLBuilder.HOST)
		{
			objectgroupDoc = builder.build(Utils.xmlFileLocations[objectType]);
			namespace = Utils.nameSpaces[ObjectXMLBuilder.HOSTGROUP];
			groupType = ObjectXMLBuilder.HOSTGROUP;
		}
		else if(objectType == ObjectXMLBuilder.SERVICE)
		{
			objectgroupDoc = builder.build(Utils.xmlFileLocations[objectType]);
			namespace = Utils.nameSpaces[ObjectXMLBuilder.SERVICEGROUP];
			groupType = ObjectXMLBuilder.SERVICEGROUP;
		}	
		else if(objectType == ObjectXMLBuilder.CONTACTGROUP)
		{
			objectgroupDoc = builder.build(Utils.xmlFileLocations[objectType]);
			namespace = Utils.nameSpaces[ObjectXMLBuilder.CONTACTGROUP];
			groupType = ObjectXMLBuilder.CONTACTGROUP;
		}
		
		/* Testing to see if we have a valid group option */
		if(groupType == -1)
			return false;
				
		XPath xog = XPath.newInstance(objectSearches[groupType] + "[@id='" + objectgroupId + "']");
		Element foundObjectgroup = (Element)xog.selectSingleNode(objectgroupDoc);
		String currentMembers = foundObjectgroup.getChildText("members",namespace);
		
		if(foundObjectgroup != null)
		{
			
			String[] members = null;
			String newMembers = "";
			
			/* check to see if there is more than one member in this group */
			if(currentMembers.contains(","))
			{
				members = currentMembers.split(",");
			}
			
			if(members != null)
			{
				int i = 1;
				
				for(String s : members)
				{
					System.out.println(i + ":" + members.length);
					/* exclude the object we no longer wish to keep */
					if(!s.equals(objectIdentifier) && i < members.length)
					{
						newMembers += s + ",";
					}
					/* If we are adding the last element, we no longer require the , suffix. */
					else if(!s.equals(objectIdentifier) && i == members.length)
					{
						newMembers += s;
					}
					i++;
				}
				
				foundObjectgroup.getChild("members",namespace).setText(newMembers);
				writeXMLFile(objectgroupDoc,"output/hostgroups.xml");
				return true;
			}
			else
			{
				foundObjectgroup.getChild("members",namespace).setText("");
				return true;
			}
		}
		
		
		return false;
	}
	
	/*
	 * This method is used to work out the current highest id within the objects specified.
	 * This is required so that we do not give duplicate id's to any of the objects.
	 * 
	 * @param = int objectType, the type of object you wish to find the highest id for.
	 * 
	 * @return = int, the next available id for an object of the type specified. 
	 */
	
	public int getNextObjectId(int objectType) throws JDOMException,IOException
	{
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
		XPath x = XPath.newInstance(objectSearches[objectType] + "[last()]");
		
		Element lastNode = (Element)x.selectSingleNode(objectDoc);
		
		/* If we don't have any defined elements within the doc, the obviously our first id is 1 */
		if(lastNode == null)
		{
			return 0;			
		}
	
		return lastNode.getAttribute("id").getIntValue()+1;
	}
	
	/*
	 * Method for building required objects for Groups from XML document.
	 * 
	 * @param = int, the group type you want to build.
	 * @return = HashMap, group objects.
	 */
	
	public HashMap<Integer,BlueObject> buildGroupObjectsFromXML(int groupType)throws JDOMException,IOException,IllegalStateException
	{
		HashMap<Integer,BlueObject> groups = new HashMap<Integer,BlueObject>();
		Document objectDoc;
		XPath x;
		List list;
		Group group;
		int groupT;
		outputDirectory = Utils.getCurrentOutputLocation();
		
		if(groupType == ObjectXMLBuilder.HOSTGROUP)
		{
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[groupType]);
			x = XPath.newInstance("hostgroups:hostgroups/hostgroups:hostgroup");
			groupT = Group.HOSTGROUP;
		}
		else if(groupType == ObjectXMLBuilder.CONTACTGROUP)
		{
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[groupType]);
			x = XPath.newInstance("contactgroups:contactgroups/contactgroups:contactgroup");
			groupT = Group.CONTACTGROUP;
		}
		else if(groupType == ObjectXMLBuilder.SERVICEGROUP)
		{
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[groupType]);
			x = XPath.newInstance("servicegroups:servicegroups/servicegroups:servicegroup");
			groupT = Group.SERVICEGROUP;
		}
		else
		{
			throw new IllegalStateException("Un-recognised Group Type!");
		}
		
		list = x.selectNodes(objectDoc);
		
		for(Object o :list)
		{
			Element e = (Element)o;
			group = new Group();
			
			group.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				
				if(el.getName().contains("name")) group.setName(el.getText());
				else if(el.getName().equals("alias")) group.setAlias(el.getText());
				else if(el.getName().equals("members"))
				{
					if(groupType == ObjectXMLBuilder.SERVICEGROUP)
					{
						String[] members = el.getText().split(",");
						String[] newMembers = new String[members.length/2];
						int count = 0;
												
						for(int i = 1;i<members.length;i = i+2)
						{
							newMembers[count] = members[i-1] + "," + members[i];
							count++;
						}
						
						group.setMembers(newMembers);
					}
					else
					{
						group.setMembers(el.getText().split(","));
					}
				}
			}
			
			group.setType(groupT);
			group.setIsModifiable(true);
			groups.put(group.getId(),new Group(group));
		}
		
		group = null;
		x = null;
		list.clear();
		objectDoc = null;
		
		return groups;
	}
	
	
	/*
	 * Method for building contact objects from XML
	 * 
	 * @return = HashMap, contains all contact objects.
	 */
	
	public HashMap<Integer,BlueObject> buildContactObjectsFromXML(boolean buildTemplates)throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> contacts = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("contacts:contacts/contacts:contact");
		Contact contact;
		Document objectDoc = null;
		outputDirectory = Utils.getCurrentOutputLocation();
		
		if(buildTemplates)
			objectDoc = builder.build(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[ObjectXMLBuilder.CONTACT]);
		else
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.CONTACT]);
		
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			contact = new Contact();
			contact.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("contact_name")) contact.setContactName(el.getText());
				else if(el.getName().equals("alias")) contact.setAlias(el.getText());
				else if(el.getName().equals("service_notification_period")) contact.setServiceNotificationPeriod(el.getText());
				else if(el.getName().equals("host_notification_period")) contact.setHostNotificationPeriod(el.getText());
				else if(el.getName().equals("host_notification_options")) contact.setHostNotificationOptions(el.getText().split(","));
				else if(el.getName().equals("service_notification_options")) contact.setServiceNotificationOptions(el.getText().split(","));
				else if(el.getName().equals("host_notifiation_commands")) contact.setHostNotificationCommands(el.getText().split(","));
				else if(el.getName().equals("service_notification_commands")) contact.setServiceNotificationCommands(el.getText().split(","));
				else if(el.getName().equals("email")) contact.setEmail(el.getText());
				else if(el.getName().equals("pager")) contact.setPager(el.getText());
			}
			
			contact.setIsModifiable(true);
			
			if(buildTemplates)
			{
				contact.setIsModifiable(false);
				contact.setIsTemplate(true);
				contact.setIsTemplateModifiable(true);
			}
			
			contacts.put(contact.getId(),new Contact(contact));
			
		}
		
		x = null;
		contact = null;
		objectDoc = null;
		list.clear();
		return contacts;
	}
	
	/*
	 *  Method for building all Service objects from XML.
	 *  
	 *  @return = Hashmap, containing all service objects.
	 */
	
	public HashMap<Integer,BlueObject> buildServiceObjectsFromXML(boolean includeTemplates) throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> services = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("services:services/services:service");
		Service service;
		Document objectDoc = null;
		outputDirectory = Utils.getCurrentOutputLocation();
		
		if(includeTemplates)
			objectDoc = builder.build(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[ObjectXMLBuilder.SERVICE]);
		else
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.SERVICE]);
				
		List list = x.selectNodes(objectDoc);
		
		for(Object o: list)
		{
			Element e = (Element)o;
			service = new Service();
			service.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				
				if(el.getName().equals("host_name")) service.setHostname(el.getText().split(","));
				else if(el.getName().equals("service_description")) service.setServiceDescription(el.getText());
				else if(el.getName().equals("service_groups")) service.setServiceGroups(el.getText().split(","));
				else if(el.getName().equals("is_volatile")) service.setIsVolatile(stringToBoolean(el.getText()));
				else if(el.getName().equals("check_command"))
				{
					int index = el.getText().indexOf("!");
					service.setCheckCommand(el.getText().substring(0,index));
					service.setCommandArgs(el.getText().substring(index));
				}
				else if(el.getName().equals("max_check_attempts")) service.setMaxCheckAttempts(Integer.valueOf(el.getText()));
				else if(el.getName().equals("normal_check_interval")) service.setNormalCheckInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("retry_check_interval")) service.setRetryCheckInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("active_checks_enabled")) service.setActiveChecksEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("passive_checks_enabled")) service.setPassiveChecksEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("check_period")) service.setCheckPeriod(el.getText());
				else if(el.getName().equals("parallelize_check")) service.setParallelizeChecks(stringToBoolean(el.getText()));
				else if(el.getName().equals("obsess_over_service")) service.setObsessOverService(stringToBoolean(el.getText()));
				else if(el.getName().equals("check_freshness")) service.setCheckFreshness(stringToBoolean(el.getText()));
				else if(el.getName().equals("freshness_threshold")) service.setFreshnessThreshold(Integer.valueOf(el.getText()));
				else if(el.getName().equals("event_handler")) service.setEventHandler(el.getText().split(","));
				else if(el.getName().equals("event_handler_enabled")) service.setEventHandlerEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("low_flap_threshold")) service.setLowFlapThreshold(Integer.valueOf(el.getText()));
				else if(el.getName().equals("high_flap_threshold")) service.setHighFlapThreshold(Integer.valueOf(el.getText()));
				else if(el.getName().equals("flap_detection_enabled")) service.setFlapDetectionEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("process_perf_data")) service.setProcessPerfData(stringToBoolean(el.getText()));
				else if(el.getName().equals("retain_status_information")) service.setRetainStatusInformation(stringToBoolean(el.getText()));
				else if(el.getName().equals("retain_nonstatus_information")) service.setRetainNonStatusInformation(stringToBoolean(el.getText()));
				else if(el.getName().equals("notification_interval")) service.setNotificationInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("notification_period")) service.setNotificationPeriod(el.getText());
				else if(el.getName().equals("notification_options")) service.setNotificationOptions(el.getText().split(","));
				else if(el.getName().equals("notifications_enabled")) service.setNotificationsEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("contact_groups")) service.setContactGroups(el.getText().split(","));
				else if(el.getName().equals("stalking_options")) service.setStalkingOptions(el.getText().split(","));
				
			}
			
			service.setIsModifiable(true);
			
			if(includeTemplates)
			{
				service.setIsModifiable(false);
				service.setIsTemplate(true);
				service.setIsTemplateModifiable(true);
			}
			services.put(service.getId(),new Service(service));
		}
		
		
		service = null;
		x = null;
		objectDoc = null;
		list.clear();
		
		return services;
	}
	
	/*
	 * Method for building all Service Dependency objects from XML
	 * 
	 * @return = Hashmap, containing all service dependency objects.
	 */
	
	public HashMap<Integer,BlueObject> buildServiceDependencyObjectsFromXML() throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> serviceDependencies = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("servicedependencies:servicedependencies/servicedependencies:servicedependency");
		ServiceDependency sD;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.SERVICEDEPENDENCY]);
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			sD = new ServiceDependency();
			sD.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				
				if(el.getName().equals("dependent_host_name")) sD.setDependentHostname(el.getText());
				else if(el.getName().equals("dependent_service_description")) sD.setDependentServiceDescription(el.getText());
				else if(el.getName().equals("host_name")) sD.setHostname(el.getText());
				else if(el.getName().equals("service_description")) sD.setServiceDescription(el.getText());
				else if(el.getName().equals("inherits_parent")) sD.setInheritsParents(stringToBoolean(el.getText()));
				else if(el.getName().equals("execution_failure_criteria")) sD.setExecutionFailureCriteria(el.getText().split(","));
				else if(el.getName().equals("notification_failure_criteria")) sD.setNotificationFailureCriteria(el.getText().split(","));
			}
			
			sD.setIsModifiable(true);
			serviceDependencies.put(sD.getId(),new ServiceDependency(sD));
		}
				
		sD = null;
		list.clear();
		objectDoc = null;
		x = null;
		return serviceDependencies;
	}
	
	
	/*
	 * Method for building all ServiceEscalation objects from XML.
	 * 
	 *  @return = Hashmap, containing all ServiceEscalations.
	 */
	
	public HashMap<Integer,BlueObject> buildServiceEscalationObjectsFromXML() throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> sel = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("serviceescalations:serviceescalations/serviceescalations:serviceescalation");
		ServiceEscalation sE;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.SERVICEESCALATION]);
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			sE = new ServiceEscalation();
			sE.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("host_name")) sE.setHostname(el.getText());
				else if(el.getName().equals("service_description")) sE.setServiceDescription(el.getText());
				else if(el.getName().equals("contact_groups")) sE.setContactGroups(el.getText().split(","));
				else if(el.getName().equals("first_notification")) sE.setFirstNotification(Integer.valueOf(el.getText()));
				else if(el.getName().equals("last_notification")) sE.setLastNotification(Integer.valueOf(el.getText()));
				else if(el.getName().equals("notification_interval")) sE.setNotificationInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("escalation_period")) sE.setEscalationPeriod(el.getText());
				else if(el.getName().equals("escalation_options")) sE.setEscalationOptions(el.getText().split(","));
			}
			
			sE.setIsModifiable(true);
			sel.put(sE.getId(),new ServiceEscalation(sE));
			
		}
		
		sE = null;
		list.clear();
		x = null;
		objectDoc = null;
		
		return sel;
		
	}
	
	
	/*
	 * Method for building all HostEscalation Objects from XML.
	 * 
	 * @return = Hashmap, containing all Host Escalation objects.
	 */
	
	public HashMap<Integer,BlueObject> buildHostEscalationObjectsFromXML() throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> sel = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("hostescalations:hostescalations/hostescalations:hostescalation");
		HostEscalation hE;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.HOSTESCALATION]);
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			hE = new HostEscalation();
			hE.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("host_name")) hE.setHostname(el.getText());
				else if(el.getName().equals("hostgroup_name")) hE.setHostGroups(el.getText().split(","));
				else if(el.getName().equals("contact_groups")) hE.setContactGroups(el.getText().split(","));
				else if(el.getName().equals("first_notification")) hE.setFirstNotification(Integer.valueOf(el.getText()));
				else if(el.getName().equals("last_notification")) hE.setLastNotification(Integer.valueOf(el.getText()));
				else if(el.getName().equals("notification_interval")) hE.setNotificationInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("contact_groups")) hE.setContactGroups(el.getText().split(","));
			}
			
			hE.setIsModifiable(true);
			sel.put(hE.getId(),new HostEscalation(hE));
			
		}
				
		hE = null;
		list.clear();
		x = null;
		objectDoc = null;
		
		return sel;
		
	}
	
	/*
	 * Method for building all Host Dependency objects from XML
	 * 
	 *  @return = Hashmap, containing all host dependency objects.
	 */
	
	public HashMap<Integer,BlueObject> buildHostDependencyObjectsFromXML() throws JDOMException,IOException
	{
	
		HashMap<Integer,BlueObject> hostd = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("hostdependencies:hostdependencies/hostdependencies:hostdependency");
		HostDependency hD;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.HOSTDEPENDENCY]);
		List list = x.selectNodes(objectDoc);
				
		for(Object o: list)
		{
			Element e = (Element)o;
			hD = new HostDependency();
			hD.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				
				if(el.getName().equals("dependent_host_name")) hD.setDependentHostname(el.getText());
				else if(el.getName().equals("host_name")) hD.setHostname(el.getText());
				else if(el.getName().equals("inherits_parent")) hD.setInheritsParents(stringToBoolean(el.getText()));
				else if(el.getName().equals("execution_failure_criteria")) hD.setExecutionFailureCriteria(el.getText().split(","));
				else if(el.getName().equals("notification_failure_criteria")) hD.setNotificationFailureCriteria(el.getText().split(","));
			}
			
			hD.setIsModifiable(true);
			hostd.put(hD.getId(),new HostDependency(hD));
		}
				
		list.clear();
		x = null;
		objectDoc = null;
		hD = null;
		
		return hostd;
	}
	
	/*
	 * Method for building all TimePeriods from XML.
	 * 
	 *  @return = Hashmap, containing all current Timeperiods.
	 */
	
	public HashMap<Integer,BlueObject> buildTimePeriodObjectsFromXML(boolean buildTemplates) throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> timePeriods = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("timeperiods:timeperiods/timeperiods:timeperiod");
		TimePeriod timePeriod;
		Document objectDoc = null;
		String[] dateBits;
		
		outputDirectory = Utils.getCurrentOutputLocation();		

		if(buildTemplates)
		{
			File file = new File(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[ObjectXMLBuilder.TIMEPERIOD]);
			
			if(!file.exists())
			{
				try
				{
				    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/arjuna/blue/bluefrontend/xml/timeperiods.xml");
        			OutputStream out = new FileOutputStream(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[ObjectXMLBuilder.TIMEPERIOD]);
    
        			if(in == null)
        				/* Try another way of loading the the Resource if the above fails */
        				this.getClass().getResourceAsStream("/com/arjuna/blue/bluefrontend/xml/timeperiods.xml");
        			
        			byte[] buf = new byte[1024];
        			int len;
        			    			
        			while ((len = in.read(buf)) > 0)
        			{
            			out.write(buf, 0, len);
        			}
        			
        			in.close();
        			out.close();
			    }
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("Cannot copy existing templates!");
				}
			}
			
			objectDoc = builder.build(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[ObjectXMLBuilder.TIMEPERIOD]);
		}
		else
		{
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.TIMEPERIOD]);
		}
		
		List list = x.selectNodes(objectDoc);
			
		for(Object o : list)
		{
			Element e = (Element)o;
			timePeriod = new TimePeriod();

			timePeriod.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("timeperiod_name")) timePeriod.setName(el.getText());
				else if(el.getName().equals("alias")) timePeriod.setAlias(el.getText());
				else if(el.getName().equals("sunday"))
				{
			
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setSundayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setSundayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setSundayStart(dateBits[0]);
						timePeriod.setSundayEnd(dateBits[1]);
					}
				}
				else if(el.getName().equals("monday"))
				{
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setMondayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setMondayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setMondayStart(dateBits[0]);
						timePeriod.setMondayEnd(dateBits[1]);
					}
				}
				else if(el.getName().equals("tuesday"))
				{
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setTuesdayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setTuesdayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setTuesdayStart(dateBits[0]);
						timePeriod.setTuesdayEnd(dateBits[1]);
					}
				}
				else if(el.getName().equals("wednesday"))
				{
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setWednesdayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setWednesdayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setWednesdayStart(dateBits[0]);
						timePeriod.setWednesdayEnd(dateBits[1]);
					}
				}
				else if(el.getName().equals("thursday"))
				{
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setThursdayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setThursdayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setThursdayStart(dateBits[0]);
						timePeriod.setThursdayEnd(dateBits[1]);
					}
				}
				else if(el.getName().equals("friday"))
				{
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setFridayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setFridayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setFridayStart(dateBits[0]);
						timePeriod.setFridayEnd(dateBits[1]);
					}
				}
				else if(el.getName().equals("saturday"))
				{
					dateBits = el.getText().split("-");
					if(dateBits.length == 4)
					{
						timePeriod.setSaturdayStart(dateBits[0] + " " + dateBits[1]);
						timePeriod.setSaturdayEnd(dateBits[2] + "-" + dateBits[3]);
					}
					else
					{
						timePeriod.setSaturdayStart(dateBits[0]);
						timePeriod.setSaturdayEnd(dateBits[1]);
					}
				}
			}
		
			timePeriod.setIsModifiable(true);
			
			if(buildTemplates)
			{
				timePeriod.setIsModifiable(false);
				timePeriod.setIsTemplate(true);
				timePeriod.setIsTemplateModifiable(true);
			}
			
			timePeriods.put(timePeriod.getId(),new TimePeriod(timePeriod));
		}
		
		x = null;
		timePeriod = null;
		objectDoc = null;
		list.clear();
		
		return timePeriods;
		
	}
	
	public HashMap<Integer,BlueObject> buildHostExtInfoObjectsFromXML()throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> extInfo = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("hostextinfos:hostextinfos/hostextinfos:hostextinfo");
		HostExtInfo hostExtInfo;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.HOSTEXTINFO]);
		
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			hostExtInfo = new HostExtInfo();
			hostExtInfo.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("host_name")) hostExtInfo.setHostname(el.getText());
				else if(el.getName().equals("notes")) hostExtInfo.setNotes(el.getText());
				else if(el.getName().equals("notes_url")) hostExtInfo.setNotesURL(el.getText());
				else if(el.getName().equals("action_url")) hostExtInfo.setActionURL(el.getText());
				else if(el.getName().equals("icon_image")) hostExtInfo.setIconImage(el.getText());
				else if(el.getName().equals("icon_image_alt")) hostExtInfo.setIconImageAlt(el.getText());
				else if(el.getName().equals("vrml_image")) hostExtInfo.setVrmlImage(el.getText());
				else if(el.getName().equals("statusmap_image")) hostExtInfo.setStatusMapImage(el.getText());
				else if(el.getName().equals("twod_coords"))
				{
					hostExtInfo.setTwodX(Double.valueOf(el.getText().split(",")[0]));
					hostExtInfo.setTwodY(Double.valueOf(el.getText().split(",")[1]));
				}
				else if(el.getName().equals("threed_coords"))
				{
					hostExtInfo.setThreedX(Double.valueOf(el.getText().split(",")[0]));
					hostExtInfo.setThreedY(Double.valueOf(el.getText().split(",")[1]));
					hostExtInfo.setThreedZ(Double.valueOf(el.getText().split(",")[2]));
				}
			}
			
			hostExtInfo.setIsModifiable(true);
			
			extInfo.put(hostExtInfo.getId(),new HostExtInfo(hostExtInfo));
			
		}
		
		x = null;
		hostExtInfo = null;
		objectDoc = null;
		list.clear();
		return extInfo;
	}
	
	
	public HashMap<Integer,BlueObject> buildServiceExtInfoObjectsFromXML()throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> extInfo = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("serviceextinfos:serviceextinfos/serviceextinfos:serviceextinfo");
		ServiceExtInfo serviceExtInfo;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.SERVICEEXTINFO]);
		
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			serviceExtInfo = new ServiceExtInfo();
			serviceExtInfo.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("host_name")) serviceExtInfo.setHostname(el.getText());
				else if(el.getName().equals("service_description")) serviceExtInfo.setServiceDescription(el.getText());
				else if(el.getName().equals("notes")) serviceExtInfo.setNotes(el.getText());
				else if(el.getName().equals("notes_url")) serviceExtInfo.setNotesURL(el.getText());
				else if(el.getName().equals("action_url")) serviceExtInfo.setActionURL(el.getText());
				else if(el.getName().equals("icon_image")) serviceExtInfo.setIconImage(el.getText());
				else if(el.getName().equals("icon_image_alt")) serviceExtInfo.setIconImageAlt(el.getText());
			}
			
			serviceExtInfo.setIsModifiable(true);
			extInfo.put(serviceExtInfo.getId(),new ServiceExtInfo(serviceExtInfo));
		}
		
		x = null;
		serviceExtInfo = null;
		objectDoc = null;
		list.clear();
		return extInfo;
	}
	
	/*
	 * Method for building all command objects from XML.
	 * 
	 *  @return = Hashmap, containing all command objects.
	 */
	
	public HashMap<Integer,BlueObject> buildCommandObjectsFromXML() throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> commands = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("commands:commands/commands:command");
		Command command;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.COMMAND]);
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			command = new Command();
			command.setId(Integer.valueOf(e.getAttribute("id").getValue()));
						
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				
				if(el.getName().equals("command_name")) command.setName(el.getText());
				else if(el.getName().equals("command_line")) command.setCommandLine(el.getText());
				
			}
			
			command.setIsModifiable(true);
			commands.put(command.getId(),new Command(command));
		}
		
		objectDoc = null;
		x = null;
		list.clear();
		command = null;
		
		return commands;
	}
	
	/*
	 * Method for building all host objects from XML.
	 * 
	 * @return = Hashmap, containing all host objects.
	 */
	
	public HashMap<Integer,BlueObject> buildHostObjectsFromXML(boolean includeTemplates) throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> hosts = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("hosts:hosts/hosts:host");
		Host host;
		Document objectDoc = null;
		outputDirectory = Utils.getCurrentOutputLocation();
		
		if(includeTemplates)
			objectDoc = builder.build(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[ObjectXMLBuilder.HOST]);
		else
			objectDoc = builder.build(outputDirectory + "/xml/" + Utils.xmlFileLocations[ObjectXMLBuilder.HOST]);
		
		List list = x.selectNodes(objectDoc);
			
		for(Object o : list)
		{
			Element e = (Element)o;
			host = new Host();
			host.setId(Integer.valueOf(e.getAttribute("id").getValue()));
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				if(el.getName().equals("host_name")) host.setHostname(el.getText());
				else if(el.getName().equals("alias")) host.setAlias(el.getText());
				else if(el.getName().equals("address")) host.setIPAddress(el.getText());
				else if(el.getName().equals("parents")) host.setParents(el.getText().split(","));
				else if(el.getName().equals("hostgroups")) host.setHostGroups(el.getText().split(","));
				else if(el.getName().equals("check_command")) host.setCheckCommand(el.getText());
				else if(el.getName().equals("max_check_attempts")) host.setMaxCheckAttempts(Integer.valueOf(el.getText()));
				else if(el.getName().equals("check_interval")) host.setCheckInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("active_checks_enabled")) host.setActiveChecksEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("passive_checks_enabled")) host.setPassiveChecksEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("check_period")) host.setCheckPeriod(el.getText());
				else if(el.getName().equals("checks_enabled")) host.setChecksEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("obsess_over_host")) host.setObsessOverHost(stringToBoolean(el.getText()));
				else if(el.getName().equals("check_freshness")) host.setCheckFreshness(stringToBoolean(el.getText()));
				else if(el.getName().equals("freshness_threshold")) host.setFreshnessThreshold(Integer.valueOf(el.getText()));
				else if(el.getName().equals("event_handler")) host.setEventHandler(el.getText());
				else if(el.getName().equals("event_handler_enabled")) host.setEventHandlerEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("low_flap_threshold")) host.setLowFlapThreshold(Integer.valueOf(el.getText()));
				else if(el.getName().equals("high_flap_threshold")) host.setHighFlapThreshold(Integer.valueOf(el.getText()));
				else if(el.getName().equals("flap_detection_enabled")) host.setFlapDetectionEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("failure_prediction_enabled")) host.setFailurePredictionEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("process_perf_data")) host.setProcessPerfData(stringToBoolean(el.getText()));
				else if(el.getName().equals("retain_status_information")) host.setRetainStatusInformation(stringToBoolean(el.getText()));
				else if(el.getName().equals("contact_groups")) host.setContactGroups(el.getText().split(","));
				else if(el.getName().equals("notification_interval")) host.setNotificationInterval(Integer.valueOf(el.getText()));
				else if(el.getName().equals("notification_period")) host.setNotificationPeriod(el.getText());
				else if(el.getName().equals("notification_options")) host.setNotificationOptions(el.getText().split(","));
				else if(el.getName().equals("notifications_enabled")) host.setNotificationsEnabled(stringToBoolean(el.getText()));
				else if(el.getName().equals("stalking_options")) host.setStalkingOptions(el.getText().split(","));
				
			}
			host.setIsModifiable(true);
			
			if(includeTemplates)
			{
				host.setIsTemplate(true);
				host.setIsModifiable(false);
				host.setIsTemplateModifiable(true);
			}
			
			hosts.put(host.getId(),new Host(host));
		}

		x = null;
		host = null;
		objectDoc = null;
		list.clear();
		
		return hosts;
	}
	
	public HashMap<Integer,BlueObject> buildMacroObjectsFromXML() throws JDOMException,IOException
	{
		HashMap<Integer,BlueObject> macros = new HashMap<Integer,BlueObject>();
		XPath x = XPath.newInstance("macros:macros/macros:macro");
		Macro macro;
		outputDirectory = Utils.getCurrentOutputLocation();
		Document objectDoc = builder.build(outputDirectory + "/xml/macros.xml");
		List list = x.selectNodes(objectDoc);
		
		for(Object o : list)
		{
			Element e = (Element)o;
			macro = new Macro();
			String[] id = e.getAttribute("macro_id").getValue().split("R");
			macro.setId(Integer.valueOf(id[1].substring(0,id[1].length()-1)));
			
			
			for(Object ob: e.getChildren())
			{
				Element el = (Element)ob;
				
				if(el.getName().equals("macro_value")) macro.setMacroValue(el.getText());
			}
			
			macro.setIsModifiable(true);
			macros.put(macro.getId(),new Macro(macro));
		}
		
		objectDoc = null;
		x = null;
		list.clear();
		macro = null;
		
		return macros;
	}
	
	/*
	 * Method used for testing in version 0.1 of this class.
	 */
	private void writeXMLFile(Document doc,String filename) throws FileNotFoundException,IOException
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		FileOutputStream output = new FileOutputStream(filename);
		
		outputter.output(doc,output);
		output.close();
			
	}
	
	private boolean stringToBoolean(String toChange)
	{
		if(toChange.trim().equals("1"))
			return true;
		
		return false;
	}
	
	
}
