package com.arjuna.blue.bluefrontend.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.arjuna.blue.bluefrontend.faces.Utils;

/*
 * ConfigParser.java - This class can take any current Blue/Nagios 2.x configuration file and
 * 					   parse it into an XML representation. This makes it much easier to manipulate
 * 					   and play around with the contents of the config files via the web front-end.
 */

public class ConfigParser
{
		
		private String outputLocation = "output";
		private String configLocation = "output";
				
		
		// LinkedList for storing repeating elements that lead to other files that must be processed.
		private LinkedList<String> elementsForProcessing = new LinkedList<String>(); 		
				
		/* Root elements for object XML files */
		private Element hostRoot = new Element("hosts",Utils.nameSpaces[ObjectXMLBuilder.HOST]);
		private Element serviceRoot = new Element("services",Utils.nameSpaces[ObjectXMLBuilder.SERVICE]);
		private Element contactRoot = new Element("contacts",Utils.nameSpaces[ObjectXMLBuilder.CONTACT]);
		private Element commandRoot = new Element("commands",Utils.nameSpaces[ObjectXMLBuilder.COMMAND]);
		private Element timeperiodRoot = new Element("timeperiods",Utils.nameSpaces[ObjectXMLBuilder.TIMEPERIOD]);
		private Element contactgroupRoot = new Element("contactgroups",Utils.nameSpaces[ObjectXMLBuilder.CONTACTGROUP]);
		private Element servicegroupRoot = new Element("servicegroups",Utils.nameSpaces[ObjectXMLBuilder.SERVICEGROUP]);
		private Element hostgroupRoot = new Element("hostgroups",Utils.nameSpaces[ObjectXMLBuilder.HOSTGROUP]);
		private Element serviceescalationRoot = new Element("serviceescalations",Utils.nameSpaces[ObjectXMLBuilder.SERVICEESCALATION]);
		private Element hostescalationRoot = new Element("hostescalations",Utils.nameSpaces[ObjectXMLBuilder.HOSTESCALATION]);
		private Element hostextinfoRoot = new Element("hostextinfos",Utils.nameSpaces[ObjectXMLBuilder.HOSTEXTINFO]);
		private Element serviceextinfoRoot = new Element("serviceextinfos",Utils.nameSpaces[ObjectXMLBuilder.SERVICEEXTINFO]);
		private Element hostdependencyRoot = new Element("hostdependencies",Utils.nameSpaces[ObjectXMLBuilder.HOSTDEPENDENCY]);
		private Element servicedependencyRoot = new Element("servicedependencies",Utils.nameSpaces[ObjectXMLBuilder.SERVICEDEPENDENCY]);
		
		/* XML Document bodys based around root elements */
		private Document hostDoc = new Document(hostRoot);
		private Document serviceDoc = new Document(serviceRoot);
		private Document contactDoc = new Document(contactRoot);
		private Document commandDoc = new Document(commandRoot);
		private Document timeperiodDoc = new Document(timeperiodRoot);
		private Document contactgroupDoc = new Document(contactgroupRoot);
		private Document servicegroupDoc = new Document(servicegroupRoot);
		private Document hostgroupDoc = new Document(hostgroupRoot);
		private Document serviceescalationDoc = new Document(serviceescalationRoot);
		private Document hostescalationDoc = new Document(hostescalationRoot);
		private Document hostextinfoDoc = new Document(hostextinfoRoot);
		private Document serviceextinfoDoc = new Document(serviceextinfoRoot);
		private Document hostdependencyDoc = new Document(hostdependencyRoot);
		private Document servicedependencyDoc = new Document(servicedependencyRoot);
		
				
		/* Element counters */
		
		private int hostCount = 0;
		private int hostgroupCount = 0;
		private int serviceCount = 0;
		private int servicegroupCount = 0;
		private int contactCount = 0;
		private int contactgroupCount = 0;
		private int timeperiodCount = 0;
		private int commandCount = 0;
		private int servicedependencyCount = 0;
		private int serviceescalationCount = 0;
		private int hostdependencyCount = 0;
		private int hostescalationCount = 0;
		private int hostextinfoCount = 0;
		private int serviceextinfoCount = 0;
		
		/* This HashMap is used to store Templates that are found when parsing Nagios config
		 * files. The templates are stored by their name, and then subsequently a list containing
		 * all of the template parameters. When another object uses this template, the contents of the 
		 * list are simply copied across. At the end we can purge this list and write them into any current
		 * template directory 
		 */
		
		private HashMap<String,List<String>> templateStore = new HashMap<String,List<String>>();
				
		/*
		 * Blank constructor. 
		 */
		
		public ConfigParser()
		{
		}
		
		public void setOutputLocation(String outputLocation)
		{
			this.outputLocation = outputLocation;
		}
		
		/*
		 *	Method that converts an specifed property file into the specifed XML version.
		 *	This method will seek out and user defined config files an attempt to convert those 
		 *	into valid XML aswell.
		 *
		 * @param = String propertiesFileName, the location of the main blue/nagios config file.
		 * @param = String xmlOutputFileName, the destination file for output of the XML process.
		 */
		
		public synchronized boolean convert(String propertiesFileName,String xmlOutputFileName)throws FileNotFoundException,IOException
		{
				FileInputStream input = new FileInputStream(propertiesFileName);
				Properties props = new Properties();
				props.load(input);
			
				convertConfigToXML(props,xmlOutputFileName);
				processListedElements(elementsForProcessing);
			
				/* Make sure we have the latest output location */
				outputLocation = Utils.getCurrentOutputLocation();
				
				printXMLDocument(hostDoc,outputLocation + "/xml/" + Utils.hostXMLFile);
				printXMLDocument(serviceDoc,outputLocation + "/xml/" + Utils.serviceXMLFile);
				printXMLDocument(contactDoc,outputLocation + "/xml/" + Utils.contactXMLFile);
				printXMLDocument(timeperiodDoc,outputLocation + "/xml/" + Utils.timeperiodXMLFile);
				printXMLDocument(contactDoc,outputLocation + "/xml/" + Utils.contactXMLFile);
				printXMLDocument(commandDoc,outputLocation + "/xml/" + Utils.commandXMLFile);
				printXMLDocument(contactgroupDoc,outputLocation + "/xml/" + Utils.contactgroupXMLFile);
				printXMLDocument(servicegroupDoc,outputLocation + "/xml/" + Utils.servicegroupXMLFile);
				printXMLDocument(hostgroupDoc,outputLocation + "/xml/" + Utils.hostgroupXMLFile);
				printXMLDocument(hostescalationDoc,outputLocation + "/xml/" + Utils.hostescalationXMLFile);
				printXMLDocument(serviceescalationDoc,outputLocation + "/xml/" + Utils.serviceescalationXMLFile);
				printXMLDocument(serviceextinfoDoc,outputLocation + "/xml/" + Utils.serviceextinfoXMLFile);
				printXMLDocument(hostextinfoDoc,outputLocation + "/xml/" + Utils.hostextinfoXMLFile);
				printXMLDocument(hostdependencyDoc,outputLocation + "/xml/" + Utils.hostdependencyXMLFile);
				printXMLDocument(servicedependencyDoc,outputLocation + "/xml/" + Utils.servicedependencyXMLFile);
				
				return true;
		}
		
		public void setConfigLocation(String configLocation)
		{
			this.configLocation = configLocation;
		}
		
		/*
		 * Method that reads in a current Blue/Nagios config and converts it into XML.
		 * 
		 *  @param = Properties props, a properties file that contains the attribute/value pairings of the main
		 *  		 blue/nagios configuration file.
		 *  @param = String xmlFileName, the destination filename for the XML output.
		 */
		
		private void convertConfigToXML(Properties props, String xmlFileName) throws IOException,FileNotFoundException
		{
			
			Namespace schemaNameSpace = Namespace.getNamespace("blue",Utils.schemaHost + Utils.schemaContext + "mainblueconfig");
			Element root = new Element("blue_config",schemaNameSpace);
			Document doc = new Document(root);
			LinkedList<String> list;
			
			
			for(String s: Utils.elementOrder)
			{
				String propertyValue = props.getProperty(s);
				
				if(propertyValue != null)
				{
					if(s.equals("cfg_file"))
					{
						list = retrieveRepeatingElements("cfg_file",configLocation);
						elementsForProcessing.addAll(list);
						addRepeatingElements(root,list,schemaNameSpace);
					}
					else if(s.equals("cfg_dir"))
					{
						list = retrieveRepeatingElements("cfg_dir",configLocation);
						elementsForProcessing.addAll(list);
						addRepeatingElements(root,list,schemaNameSpace);
					}
					else if(s.equals("broker_module"))
					{
						list = retrieveRepeatingElements("broker_module",configLocation);
						addRepeatingElements(root,list,schemaNameSpace);
					}
					else if(s.equals("resource_file"))
					{
						elementsForProcessing.add("resource_file = " + propertyValue);
						createXMLRepresentation(root,s,propertyValue,schemaNameSpace);
					}
					else
					{
						createXMLRepresentation(root,s,propertyValue,schemaNameSpace);
					}
				}
			}
			
			outputLocation = Utils.getCurrentOutputLocation();
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			FileOutputStream output = new FileOutputStream(outputLocation + "/xml/" + xmlFileName);
			outputter.output(doc,output);
						
		}
		
		/*
		 * Method that adds an element to a specified root element. 
		 * 
		 * @param = Element root, the root element that this element should become a child of.
		 * @param = String propertyName, the name of the new Element.
		 * @param = String propertyValue, the text value of the new Element.
		 */
		
		private void createXMLRepresentation(Element root,String propertyName,String propertyValue,Namespace nameSpace)
		{
			Element element = new Element(propertyName,nameSpace);
			element.setText(propertyValue);
			root.addContent(element);
		}
		
		/*
		 *	Method used to retrieve the possible repeating values from a valid Blue/Nagios config file
		 *
		 * 	@param = String elementName, the repeating attribute for which you are searching i.e. cfg_file;
		 *  @param = String fileName, the file in which you are looking to find a repeating element.
		 *  
		 *  @return = LinkedList<String>, a LinkedList of Strings containing lines from the above file
		 *  		  where a repeating element has been found i.e. cfg_file=/my/local/config/blue.config
		 */
		
		private LinkedList<String> retrieveRepeatingElements(String elementName,String fileName) throws FileNotFoundException,IOException
		{
			LinkedList<String> list = new LinkedList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
				
			while((line = reader.readLine()) != null)
			{
				line = line.trim();
					
				if(line.split("=")[0].trim().equals(elementName))
				{
					list.add(line);
				}
			}
			return list;
		}
	
		
		/*
		 *	Method used to add the retrieved repeating elements to a specified root element. 
		 */
		
		private void addRepeatingElements(Element root,LinkedList<String> list,Namespace schemaNameSpace)
		{
			if(list.size() == 0)
				return;
			
			for(String line: list)
			{
				createXMLRepresentation(root,line.split("=")[0],line.split("=")[1],schemaNameSpace);
			}
		}
		
		
		/*
		 * Method that deals with turning any user specified files into the required XML
		 * formats.
		 * 
		 * @param = LinkedList elements, a LinkedList that contains the attribute/value pairs
		 * 			from the main config file of blue that can contain user specified config
		 * 			files i.e. cfg_file=/my/local/config/blue.cfg
		 */
		
		private void processListedElements(LinkedList<String> elements) throws IOException,FileNotFoundException
		{
			Iterator i = elements.iterator();
			BufferedReader reader;
			
			while(i.hasNext())
			{
				String element = (String)i.next();
				String directive = element.split("=")[0].trim();
				String command = element.split("=")[1].trim();
				
				/* Process all config files */
				if(directive.equals("cfg_file"))
				{
					reader = getFileHandle(command);
					processConfigurationFile(reader);
				}
				
				/* Process all resource config dirs */
				if(directive.equals("cfg_dir"))
					processConfigurationDirectory(command);
								
				/* Process user specifed resource config files */
				if(directive.equals("resource_file"))
				{
					reader = getFileHandle(command);
					convertResourceFileToXML(reader);
			    }
			}
		}
		
		/*
		 *  Return a handle to a specifed file. Implemented in this fashion for brevity in the above
		 *  code.
		 *  
		 *  @param = String filename, the filename on which you want to open a FileReader;
		 *  @return = BufferedReader, a buffered FileReader.
		 */
		
		// TODO - checking for relative paths..
		private BufferedReader getFileHandle(String filename) throws FileNotFoundException
		{
			BufferedReader reader = null;
			filename = filename.replace("\\","/");
			reader = new BufferedReader(new FileReader(filename));
						
			return reader;
		}
		
		
		/*
		 * 	Method that converts a resource file containing user specified macros into an XML representation.
		 * 
		 *  @param = BufferedReader resourceFileHandle, a bufferedReader to the file you wish to convert.
		 */
		
		private void convertResourceFileToXML(BufferedReader resourceFileHandle) throws IOException,FileNotFoundException
		{
			/* Update outputLocation */
			outputLocation = Utils.getCurrentOutputLocation();
			
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			FileOutputStream output = new FileOutputStream(outputLocation + "/xml/macros.xml");
			Namespace schemaNamespace = Namespace.getNamespace("macros",Utils.schemaHost + Utils.schemaContext + "macros");
			Element root = new Element("macros",schemaNamespace);
			Document doc = new Document(root); 
			
			String line = resourceFileHandle.readLine();
			
			while(resourceFileHandle.readLine() != null)
			{
				line = line.trim();
				
				if(line.indexOf("$USER") == 0)					
				{
					Element macro = new Element("macro",schemaNamespace);
					Attribute macroId = new Attribute("macro_id",line.split("=")[0]);
					macro.setAttribute(macroId);
					createXMLRepresentation(macro,"macro_value",line.split("=")[1],schemaNamespace);					
					root.addContent(macro);
				}
				
				line = resourceFileHandle.readLine();
			}
			
			outputter.output(doc,output);
			
			/* Clear up */
			outputter = null;
			output = null;
			root = null;
			doc = null;
		}
		
		/*
		 * Method that is used to process a configuration file specified by the user.
		 * 
		 * @param = BufferedReader to the configuration file. 
		 */
					
		private void processConfigurationFile(BufferedReader reader) throws IOException
		{
			int currentLine = 0;
			String objectType;
			String templateName = null;
			String templateLoad;
			
			String line = reader.readLine();
						
			while(line != null)
			{
				line = line.trim();
				if(line.startsWith("define"))
				{
					line = line.substring(6).trim();
					int index = line.indexOf("{");
					
					if(index <=0)
						throw new IllegalStateException("Malformed Object definition in <<filename>> at line" + currentLine);
										
					line = line.substring(0,index).trim();
					
					if(line.length() == 0)
						throw new IllegalStateException("Malformed Object definition in <<filename>> at line" + currentLine);
									
					objectType = line.trim();
					
					line = reader.readLine();
					LinkedList<String> propsList = new LinkedList<String>();
					propsList.add(objectType);
					
					Pattern pattern = Pattern.compile("\\s+");
				
					while(!line.contains("}"))
					{
						if(line.trim().length() != 0)
						{
							// Split the line around any white space. This is how attribute
							// value pairs are assigned within the Nagios/Blue config files.
							
							String[] bits = line.trim().split(pattern.toString(),2);
							
							// Are we defining a template?
							
							if(bits[0].trim().equals("name"))
							{
								// Seems people like to comment their templates...need to remove this :)
								templateName = removeNagiosComments(bits[1].trim());
							}
							else if(bits[0].trim().equals("use"))
							{
								// Again we need to check for comments.
								templateLoad = removeNagiosComments(bits[1].trim());
															
								try
								{
									List<String> templateProps = templateStore.get(templateLoad);
									
									// pop out the object type..we don't need it.
									String templateType = templateProps.remove(0);
																		
									// Add all the template elements to the current object
									propsList.addAll(templateProps);
									templateProps.add(0,templateType);
								}
								catch(Exception e)
								{
									System.out.println("Cannot find specified template!");
								}
								
							}
							// anything that has register set to 0, is likely to be a template.
							// We're not using the register variable anyways.
							else if(!bits[0].trim().equals("register"))
							{
								propsList.add(bits[0].trim());
								// Again need to check for comments.
								propsList.add(removeNagiosComments(bits[1].trim()));
							}
						}
												
						line = reader.readLine();
					}
					
					// Cleanup last line properly.
					
					if(line.trim().indexOf("}") != 0)
					{
						line = line.substring(0,line.indexOf("}"));
						String[] bits = line.trim().split(pattern.toString(),2);
						propsList.add(bits[0].trim());
						propsList.add(bits[1].trim());
					}
					
					// If we have a template, we simply wish to put it in the template
					// store for the time being.
					
					if(templateName != null)
						templateStore.put(templateName,propsList);
					else
						buildObjectXML(propsList);
				}
				
				currentLine++;
				templateName = null;
				templateLoad = null;
				line = reader.readLine();
			}
			
			reader.close();
		}
		
		/*
		 * Method for processing configuration directories.
		 * 
		 * @param = String, the name of the directory you wish to process.
		 */
		
		private void processConfigurationDirectory(String directoryName) throws IOException
		{
			String[] fileList = new File(directoryName).list();
			
			if(fileList != null)
			{
				for(String filename: fileList)
				{
					File file = new File(filename);
					
					if(file.isDirectory())
					{
						processConfigurationDirectory(file.toString());
					}
					
					if(file.toString().endsWith(".cfg"))
					{
						processConfigurationFile(getFileHandle(file.toString()));
					}
					
				}
				
			}
		}
		
		
		/*
		 * Method that turns a Blue/Nagios object into an XML representation.
		 * N.B. This is version 0.2 of this method. The original relied on a properties object
		 * for the attribute/value pairings, however this does not maintain ordering which proved
		 * problematic. Therefore this has currently been migrated to use LinkedLists;
		 * 
		 *  @param = LinkedList<String> paramList, list of the object attribute/value pairs.
		 */
		
		private void buildObjectXML(LinkedList<String> paramList)
		{
			// defining a host
			if(paramList.get(0).trim().equals("host"))
			{
				Element host = new Element("host",Utils.nameSpaces[ObjectXMLBuilder.HOST]);
				Attribute id = new Attribute("id",String.valueOf(hostCount));
				host.setAttribute(id);
					
				addAllListElements(host,Utils.nameSpaces[ObjectXMLBuilder.HOST],paramList);					
				hostRoot.addContent(host);
				hostCount++;
				return;
			}
			
			// defining a service
			if(paramList.get(0).trim().equals("service"))
			{
				Element service = new Element("service",Utils.nameSpaces[ObjectXMLBuilder.SERVICE]);
				Attribute id = new Attribute("id",String.valueOf(serviceCount));
				service.setAttribute(id);
				
				addAllListElements(service,Utils.nameSpaces[ObjectXMLBuilder.SERVICE],paramList);
				serviceRoot.addContent(service);
				serviceCount++;
				return;
			}
			
			// Defining contacts.
			if(paramList.get(0).trim().equals("contact"))
			{
				Element contact = new Element("contact",Utils.nameSpaces[ObjectXMLBuilder.CONTACT]);
				Attribute id = new Attribute("id",String.valueOf(contactCount));
				contact.setAttribute(id);
				
				addAllListElements(contact,Utils.nameSpaces[ObjectXMLBuilder.CONTACT],paramList);
				contactRoot.addContent(contact);
				contactCount++;
				return;
			}
			
			// Defining timeperiods
			if(paramList.get(0).trim().equals("timeperiod"))
			{
				Element timeperiod = new Element("timeperiod",Utils.nameSpaces[ObjectXMLBuilder.TIMEPERIOD]);
				Attribute id = new Attribute("id",String.valueOf(timeperiodCount));
				timeperiod.setAttribute(id);
				
				addAllListElements(timeperiod,Utils.nameSpaces[ObjectXMLBuilder.TIMEPERIOD],paramList);
				timeperiodRoot.addContent(timeperiod);
				timeperiodCount++;
				return;
			}
									
			// Defining commands.
			if(paramList.get(0).trim().equals("command"))
			{
				Element command = new Element("command",Utils.nameSpaces[ObjectXMLBuilder.COMMAND]);
				Attribute id = new Attribute("id",String.valueOf(commandCount));
				command.setAttribute(id);
				
				addAllListElements(command,Utils.nameSpaces[ObjectXMLBuilder.COMMAND],paramList);
				commandRoot.addContent(command);
				commandCount++;
				return;
			}
			
			// Defining contact groups.
			if(paramList.get(0).trim().equals("contactgroup"))
			{
				Element contactgroup = new Element("contactgroup",Utils.nameSpaces[ObjectXMLBuilder.CONTACTGROUP]);
				Attribute id = new Attribute("id",String.valueOf(contactgroupCount));
				contactgroup.setAttribute(id);
				
				addAllListElements(contactgroup,Utils.nameSpaces[ObjectXMLBuilder.CONTACTGROUP],paramList);
				contactgroupRoot.addContent(contactgroup);
				contactgroupCount++;
				return;
			}
			
			// Defining service groups.
			if(paramList.get(0).trim().equals("servicegroup"))
			{
				Element servicegroup = new Element("servicegroup",Utils.nameSpaces[ObjectXMLBuilder.SERVICEGROUP]);
				Attribute id = new Attribute("id",String.valueOf(servicegroupCount));
				servicegroup.setAttribute(id);
				
				addAllListElements(servicegroup,Utils.nameSpaces[ObjectXMLBuilder.SERVICEGROUP],paramList);
				servicegroupRoot.addContent(servicegroup);
				servicegroupCount++;
				return;
			}

			// Defining host groups.
			if(paramList.get(0).trim().equals("hostgroup"))
			{
				Element hostgroup = new Element("hostgroup",Utils.nameSpaces[ObjectXMLBuilder.HOSTGROUP]);
				Attribute id = new Attribute("id",String.valueOf(hostgroupCount));
				hostgroup.setAttribute(id);
				
				addAllListElements(hostgroup,Utils.nameSpaces[ObjectXMLBuilder.HOSTGROUP],paramList);
				hostgroupRoot.addContent(hostgroup);
				hostgroupCount++;
				return;
			}
			
			// Defining service escalations.
			if(paramList.get(0).trim().equals("serviceescalation"))
			{
				Element serviceescalation = new Element("serviceescalation",Utils.nameSpaces[ObjectXMLBuilder.SERVICEESCALATION]);
				Attribute id = new Attribute("id",String.valueOf(serviceescalationCount));
				serviceescalation.setAttribute(id);
				
				addAllListElements(serviceescalation,Utils.nameSpaces[ObjectXMLBuilder.SERVICEESCALATION],paramList);
				serviceescalationRoot.addContent(serviceescalation);
				serviceescalationCount++;
				return;
			}
			
			// Defining host escalations.
			if(paramList.get(0).trim().equals("hostescalation"))
			{
				Element hostescalation = new Element("hostescalation",Utils.nameSpaces[ObjectXMLBuilder.HOSTESCALATION]);
				Attribute id = new Attribute("id",String.valueOf(hostescalationCount));
				hostescalation.setAttribute(id);
				
				addAllListElements(hostescalation,Utils.nameSpaces[ObjectXMLBuilder.HOSTESCALATION],paramList);
				hostescalationRoot.addContent(hostescalation);
				hostescalationCount++;
				return;				
			}
			
			// Defining host dependencies.
			if(paramList.get(0).trim().equals("hostdependency"))
			{
				Element hostdependency = new Element("hostdependency",Utils.nameSpaces[ObjectXMLBuilder.HOSTDEPENDENCY]);
				Attribute id = new Attribute("id",String.valueOf(hostdependencyCount));
				hostdependency.setAttribute(id);
				
				addAllListElements(hostdependency,Utils.nameSpaces[ObjectXMLBuilder.HOSTDEPENDENCY],paramList);
				hostdependencyRoot.addContent(hostdependency);
				hostdependencyCount++;
				return;
			}
			
			// Defining service dependencies
			if(paramList.get(0).trim().equals("servicedependency"))
			{
				Element servicedependency = new Element("servicedependency",Utils.nameSpaces[ObjectXMLBuilder.SERVICEDEPENDENCY]);
				Attribute id = new Attribute("id",String.valueOf(servicedependencyCount));
				servicedependency.setAttribute(id);
				
				addAllListElements(servicedependency,Utils.nameSpaces[ObjectXMLBuilder.SERVICEDEPENDENCY],paramList);
				servicedependencyRoot.addContent(servicedependency);
				servicedependencyCount++;
				return;
			}
			
			// Defining hostextinfos
			if(paramList.get(0).trim().equals("hostextinfo"))
			{
				Element hostextinfo = new Element("hostextinfo",Utils.nameSpaces[ObjectXMLBuilder.HOSTEXTINFO]);
				Attribute id = new Attribute("id",String.valueOf(hostextinfoCount));
				hostextinfo.setAttribute(id);
				
				addAllListElements(hostextinfo,Utils.nameSpaces[ObjectXMLBuilder.HOSTEXTINFO],paramList);
				hostextinfoRoot.addContent(hostextinfo);
				hostextinfoCount++;
				return;
			}
			
			// Defining serviceextinfos
			if(paramList.get(0).trim().equals("serviceextinfo"))
			{
				Element serviceextinfo = new Element("serviceextinfo",Utils.nameSpaces[ObjectXMLBuilder.SERVICEEXTINFO]);
				Attribute id = new Attribute("id",String.valueOf(serviceextinfoCount));
				serviceextinfo.setAttribute(id);
				
				addAllListElements(serviceextinfo,Utils.nameSpaces[ObjectXMLBuilder.SERVICEEXTINFO],paramList);
				serviceextinfoRoot.addContent(serviceextinfo);
				serviceextinfoCount++;
				return;
			}
		}
		
		/*
		 * Utility method for adding all elements within a LinkedList to a specified element.
		 * 
		 * @param = Element root, the root element that all elements should be added to.
		 * @param = Namespace namespace, the namespace associated with these elements.
		 * @param = LinkedList<String> elementList, the linkedlist of attribute/value pairs.
		 */
		
		private void addAllListElements(Element root,Namespace namespace,LinkedList<String> elementList)
		{
			for(int i=1;i<elementList.size();i=i+2)
			{
				String elementName = elementList.get(i);
				String elementValue = elementList.get(i+1);
				
				if(elementValue.contains(";"))
				{
					elementValue = elementValue.split(";")[0].trim();
				}
				
				createXMLRepresentation(root,elementName,elementValue,namespace);
				
			}
		}
		
			
		/*
		 * Utility method that simply prints the contents of a Document model to a FileOutputStream;
		 * 
		 * @param = Document documentName, the document that is to be written.
		 * @param = String documentLocation, the output destination of the writing process.
		 */
		
		private void printXMLDocument(Document documentName, String documentLocation) throws FileNotFoundException,IOException
		{
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			FileOutputStream output = new FileOutputStream(documentLocation);
			outputter.output(documentName,output);
		}
		
		/* 
		 * Simple method to remove Nagios comments from any command file string.
		 * They tend to pop up in the most annoying of places!
		 */
		
		private String removeNagiosComments(String configLine)
		{
			if(!configLine.contains(";"))
				return configLine;
			else
				return configLine.split(";")[0].trim();
		}

		public static void main(String[] args)
		{
			ConfigParser test = new ConfigParser();
			test.setConfigLocation(args[0]);
			String name = "this is my test contact group name";
			name = name.replace(" ","_");
			
		
			
			/*
			try
			{
				if(test.convert(args[0],args[1]))
				{
					System.out.println("Converted!");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(name);
			*/
		}

}
