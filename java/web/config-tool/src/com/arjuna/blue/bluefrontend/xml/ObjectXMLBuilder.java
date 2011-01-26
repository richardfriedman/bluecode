package com.arjuna.blue.bluefrontend.xml;

/*
 * Class that can be used to build XML documents based around the objects within
 * Nagios/Blue.
 * ##version 0.2: Updated to now build objects from LinkedLists to maintain element ordering. 
 */

import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.Namespace;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;

import com.arjuna.blue.bluefrontend.faces.Utils;




public class ObjectXMLBuilder 
{
	/* Class variables that can be used to help with setting of object specific parameters */
	final public static int HOST = 0;
	final public static int HOSTGROUP = 1;
	final public static int SERVICE = 2;
	final public static int SERVICEGROUP = 3; 
	final public static int CONTACT = 4;
	final public static int CONTACTGROUP = 5;
	final public static int TIMEPERIOD = 6;
	final public static int COMMAND = 7;
	final public static int SERVICEDEPENDENCY = 8;
	final public static int SERVICEESCALATION = 9;
	final public static int HOSTDEPENDENCY = 10;
	final public static int HOSTESCALATION = 11;
	final public static int HOSTEXTINFO = 12;
	final public static int SERVICEEXTINFO = 13;
	final public static int MACRO = 14;
	final public static int BLUECONFIG = 15;
	
	
	/* Starting locations for XML output files. */ 
	private String outputDirectory = "output";
		
	/*
	 * Blank constructor.
	 */
	public ObjectXMLBuilder()
	{
	}	
	
	/* Constructor that sets the output Location from the word go */
	
	public ObjectXMLBuilder(String outputLocation)
	{
		this.outputDirectory = outputLocation;
	}
	
	/*
	 * This method takes a HashMap as an arguement as well as the type of object that is being written.
	 * It opens the XML document for writing, then appends the new object to the doc.
	 * 
	 *  @param = HashMap map, a map containing all attribute/value pairings to be written.
	 *  @param = int objectType, an integer representing the type of object to be written. 
	 */
	
	public void buildObject(HashMap<String,String> map, int objectType, boolean isTemplate) throws JDOMException,IOException
	{
		SAXBuilder builder = new SAXBuilder();
		FileInputStream input;
		Element root = null;
		Document doc;
		
		outputDirectory = Utils.getCurrentOutputLocation();
		
		try
		{
			input = new FileInputStream(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
			doc = builder.build(input);
			input.close();
		}
		catch(FileNotFoundException e)
		{
			doc = new Document();
		}
		
		try
		{
			root = doc.getRootElement();
		}
		catch(IllegalStateException e)
		{
			String objectName = getObjectName(objectType);
			
			/* Need to pluralise certain object types for document root element */
			
			if(objectName.equals("servicedependency") || objectName.equals("hostdependency"))
			{
				if(objectName.equals("servicedependency"))
				{
					objectName = "servicedependencies";
				}
				else
				{
					objectName = "hostdependencies";
				}
				
				root = new Element(objectName,Utils.nameSpaces[objectType]);
				doc.setRootElement(root);
								
			}
			else
			{
				root = new Element(objectName + "s",Utils.nameSpaces[objectType]);
				doc.setRootElement(root);
			}
		}
		
		Element newElement = new Element(getObjectName(objectType),root.getNamespace());
		Attribute id = new Attribute("id",map.get("id"));
		newElement.setAttribute(id);
		map.remove("id");
				
		addAllMapElements(newElement,root.getNamespace(),map);
		root.addContent(newElement);
		
		if(isTemplate)
		{
			writeXMLFile(doc,Utils.xmlFileLocations[objectType],true);
		}
		else
		{
			writeXMLFile(doc,Utils.xmlFileLocations[objectType],false);
		}
		
		builder = null;
		
	}
	
	/*
	 *	 Same as above but uses a linkedList instead.
	 *	 
	 */
	
	public void buildObject(List<String> props,int objectType, boolean isTemplate) throws JDOMException,IOException
	{
		SAXBuilder builder = new SAXBuilder();
		FileInputStream input;
		Element root = null;
		Document doc;
		
		outputDirectory = Utils.getCurrentOutputLocation();
		
		try
		{
			if(isTemplate)
			{
				input = new FileInputStream(outputDirectory + "/xml/templates/" + Utils.xmlFileLocations[objectType]);
			}
			else
			{
				input = new FileInputStream(outputDirectory + "/xml/" + Utils.xmlFileLocations[objectType]);
			}
			doc = builder.build(input);
			input.close();
		}
		catch(FileNotFoundException e)
		{
			doc = new Document();
		}
		
		try
		{
			root = doc.getRootElement();
		}
		catch(IllegalStateException e)
		{
			
			String objectName = getObjectName(objectType);
			
			if(objectName.equals("servicedependency") || objectName.equals("hostdependency"))
			{
				if(objectName.equals("servicedependency"))
				{
					objectName = "servicedependencies";
				}
				else
				{
					objectName = "hostdependencies";
				}
				
				root = new Element(objectName,Utils.nameSpaces[objectType]);
				doc.setRootElement(root);
				
			}
			else
			{
				root = new Element(objectName + "s",Utils.nameSpaces[objectType]);
				doc.setRootElement(root);
			}
			
		}
		
		Element newElement = new Element(getObjectName(objectType),root.getNamespace());
		Attribute id = new Attribute("id",props.get(0));
		newElement.setAttribute(id);
		
		addAllListElements(newElement,root.getNamespace(),props);
		root.addContent(newElement);
		
		if(isTemplate)
		{
			writeXMLFile(doc,Utils.xmlFileLocations[objectType],true);
		}
		else
		{
			writeXMLFile(doc,Utils.xmlFileLocations[objectType],false);
		}
		
		builder = null;
	}
	
	/*
	 * This method builds a Blue/Nagios main config file from a user submitted list of properties.
	 * It is envisioned that the properties will be generated by a web-form and the attribute/value pairings
	 * inserted into the properties object that is used thus to generate the main config file.
	 * 
	 * @param = Properties props, a list of attribute/value pairings that represent the main config
	 * 			of a Blue/Nagios installation. 
	 */
	
	public void buildBlueMainConfig(Properties props) throws IOException
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		Namespace namespace = Namespace.getNamespace("blue",Utils.schemaHost + Utils.schemaContext + "mainblueconfig");
		Element root = new Element("blue_config",namespace);
		Document doc = new Document(root);
		
		outputDirectory = Utils.getCurrentOutputLocation();
		
		for(String s : Utils.elementOrder)
		{
			String propertyValue = String.valueOf(props.get(s));
			
			if(s.equals("resource_file"))
			{
				propertyValue=outputDirectory + String.valueOf(props.get(s));
			}
			
			if(propertyValue != null && !propertyValue.equals(""))
			{
				if(s.equals("cfg_file"))
				{
						for(String location : Utils.xmlFileLocations)
						{
							createXMLRepresentation(root,s,outputDirectory + "/" + location.split("\\.")[0] + ".cfg",namespace);
						}
				}
				else
				{
					createXMLRepresentation(root,s.trim(),propertyValue,namespace);
				}
			}
				
		}
		
		outputter.output(doc,new FileOutputStream(outputDirectory + "/xml/" + "blue.xml"));
		outputter = null;					
	}
	
	public void buildMacros(List macroDetails) throws IOException, JDOMException  
	{
		
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		Namespace schemaNamespace = Namespace.getNamespace("macros",Utils.schemaHost + Utils.schemaContext + "macros.xsd");
		FileInputStream input;
		SAXBuilder builder = new SAXBuilder();
		
		Element root;
		Document doc; 
		
		outputDirectory = Utils.getCurrentOutputLocation();
		
		try
		{
			input = new FileInputStream(outputDirectory + "/xml/macros.xml");
			doc = builder.build(input);
			input.close();
		}
		catch(FileNotFoundException e)
		{
			doc = new Document();
		}
		
		try
		{
			root = doc.getRootElement();
		}
		catch(IllegalStateException e)
		{
			root = new Element("macros",schemaNamespace);
			doc.setRootElement(root);
		}
		
		Element macro = new Element("macro",schemaNamespace);
		Attribute macroId = new Attribute("macro_id","$USER" + macroDetails.get(0) + "$");
		macro.setAttribute(macroId);
		
		createXMLRepresentation(macro,"macro_value",String.valueOf(macroDetails.get(1)),schemaNamespace);					
		root.addContent(macro);
		outputter.output(doc,new FileOutputStream(outputDirectory + "/xml/macros.xml"));
		outputter = null;
	}

	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}
	
	public String getOutputDirectory()
	{
		return this.outputDirectory;
	}
	
	/*
	 * Utility method for writing an XML document to file.
	 * 
	 * @param = Document doc, the XML document you wish to write.
	 * @param = String filename, the proposed location you wish to write to.
	 */
	
	private void writeXMLFile(Document doc,String filename,boolean isTemplate) throws FileNotFoundException,IOException
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		FileOutputStream output;
		File file;
		
		outputDirectory = Utils.getCurrentOutputLocation();
		
		if(isTemplate)
			file = new File(outputDirectory + "/xml/templates");
		else
			file = new File(outputDirectory + "/xml");
				
		if(!file.exists())
			file.mkdir();
		
		if(isTemplate)
			output = new FileOutputStream(outputDirectory + "/xml/templates/" + filename);
		else
			output = new FileOutputStream(outputDirectory + "/xml/" + filename);
		
		outputter.output(doc,output);
		output.close();
		
	}
			
	/*
	 *  Method that returns the name of an object based around its integer identifier.
	 *  
	 *  @param = int objectType, the integer identifier of the object.
	 *  
	 *  @return = String, the name of the object.
	 */
	
	private String getObjectName(int objectType)
	{
		String object = null;
		
		switch(objectType)
		{
			case 0: object = "host";break;
			case 1: object = "hostgroup";break;
			case 2: object = "service";break;
			case 3: object = "servicegroup";break;
			case 4: object = "contact";break;
			case 5: object = "contactgroup";break;
			case 6: object = "timeperiod";break;
			case 7: object = "command";break;
			case 8: object = "servicedependency";break;
			case 9: object = "serviceescalation";break;
			case 10: object = "hostdependency";break;
			case 11: object = "hostescalation";break;
			case 12: object = "hostextinfo";break;
			case 13: object = "serviceextinfo";break;
		}
	
		return object;
	}
	
	/*
	 * Utility method for generating an XML representation  
	 */
	
	private void createXMLRepresentation(Element root,String propertyName,String propertyValue,Namespace namespace)
	{
		Element element = new Element(propertyName,namespace);
		element.setText(propertyValue);
		root.addContent(element);
	}
	
	/*
	 * Utility method for adding all elements within a LinkedList to a specified element.
	 * 
	 * @param = Element root, the root element that all elements should be added to.
	 * @param = Namespace namespace, the namespace associated with these elements.
	 * @param = LinkedList<String> elementList, the linkedlist of attribute/value pairs.
	 */
	
	private void addAllListElements(Element root,Namespace namespace,List<String> elementList)
	{
		for(int i=1;i<elementList.size();i=i+2)
		{
			String elementName = elementList.get(i);
			String elementValue = elementList.get(i+1);
			
			if(!elementName.trim().equals("") && !elementValue.trim().equals("") && !elementValue.trim().equals("null") && elementValue.length() >=1)
			{
				if(elementValue.contains(";"))
				{
					elementValue = elementValue.split(";")[0].trim();
				}
			
				createXMLRepresentation(root,elementName,elementValue,namespace);
			}
			
		}
	}
	
	private void addAllMapElements(Element root,Namespace namespace,HashMap<String,String> map)
	{
		Iterator i = map.keySet().iterator();
		
		while(i.hasNext())
		{
			String elementName = String.valueOf(i.next());
			String elementValue = map.get(elementName);
			
			if(elementValue != null && !elementValue.equals(""))
			{
				createXMLRepresentation(root,elementName,elementValue,namespace);
			}
		}
	}

}
