package org.blue.star.plugins.salesforce;
/*****************************************************************************
*
* Blue Star, a Java Port of .
* Last Modified : 23/01/07
*
* License:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
*****************************************************************************/
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import org.blue.star.plugins.util.xml.Util;
import org.blue.star.plugins.util.xml.XmlDiff;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PicklistEntry;
import com.sforce.soap.partner.SoapBindingStub;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;

/**
 * @author Mark
 */
public class GetSchema
{
	private SoapBindingStub binding;
	private SalesForceConnector sfc;
	private String user = null;
	private String password = null;
	private String url = null;

	/**
	 * @param objectName
	 * @param config
	 * @param dbConnection2
	 * @throws Exception
	 */
	public GetSchema(String url, String user, String pass)
	{
		this.url = url;
		this.user = user;
		this.password = pass;

		SalesForceConnector sfc = new SalesForceConnector(this.url, this.user, this.password);
		this.binding = sfc.login();
	}

	/**
	 * 
	 */
	private void reconnect()
	{
		try
		{
			Thread.sleep(15000L);
			binding = sfc.login();
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 */
	public Document getSchema()
	{
		Document doc = new Document();
		Element xml = new Element("Schema");
		doc.setRootElement(xml);

		DescribeGlobalResult describeGlobalResult;
		try
		{
			describeGlobalResult = this.binding.describeGlobal();
		} catch (RemoteException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (describeGlobalResult != null)
		{
			String types[] = describeGlobalResult.getTypes();
			if (types != null)
			{
				for (int i = 0; i < types.length; i++)
				{
					try
					{
						// Describe the object here
						this.describeObject(types[i], xml);
					} catch (Exception e)
					{
						// try to reconnect if it fails
						reconnect();
						this.describeObject(types[i], xml);
					}
				}
			}
		}
		return doc;
	}

	/**
	 * @param name
	 * @return
	 */
	private void describeObject(String name, Element xml)
	{
		try
		{
			DescribeSObjectResult describeSObjectResult = binding.describeSObject(name);

			if (describeSObjectResult != null)
			{
				Field fields[] = describeSObjectResult.getFields();

				if (fields != null)
				{
					String objectName = describeSObjectResult.getName();
					Element child = new Element("Object");
					xml.addContent(child);
					
					Attribute n = new Attribute("name", objectName);
					child.setAttribute(n);
					
					for (int f = 0; f < fields.length; f++)
					{
						Field field = fields[f];
						this.printField(field, child);
					}
				}
			}
		} catch (UnexpectedErrorFault uef)
		{
			throw new RuntimeException(uef.getExceptionMessage());
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException("Failed to get " + ex.getMessage());
		}
	}
	
	/**
	 * @param field
	 * @param xml
	 * @return
	 */
	public void printField(Field field, Element xml)
	{
		Element fieldXML = new Element("field");
		xml.addContent(fieldXML);
		
		Attribute name = new Attribute("name", field.getName());
		fieldXML.setAttribute(name);
		
		fieldXML.addContent(new Element("Name").addContent(field.getName()));
		fieldXML.addContent(new Element("Type").addContent(field.getType().getValue())); 
		fieldXML.addContent(new Element("ByteLength").addContent(Integer.toString(field.getByteLength())));  
		fieldXML.addContent(new Element("Length").addContent(Integer.toString(field.getLength()))); 
		fieldXML.addContent(new Element("Digits").addContent(Integer.toString(field.getDigits())));  
		fieldXML.addContent(new Element("Label").addContent(field.getLabel())); 
		fieldXML.addContent(new Element("Precision").addContent(Integer.toString(field.getPrecision()))); 
		fieldXML.addContent(new Element("Scale").addContent(Integer.toString(field.getScale()))); 
		fieldXML.addContent(new Element("isNillable").addContent(Boolean.toString(field.isNillable()))); 
		fieldXML.addContent(new Element("isCreateable").addContent(Boolean.toString(field.isCreateable()))); 
		fieldXML.addContent(new Element("isUpdateable").addContent(Boolean.toString(field.isUpdateable()))); 
		fieldXML.addContent(new Element("isCustom").addContent(Boolean.toString(field.isCustom())));
		fieldXML.addContent(new Element("isFilterable").addContent(Boolean.toString(field.isFilterable()))); 
		fieldXML.addContent(new Element("isRestrictedPicklist").addContent(Boolean.toString(field.isRestrictedPicklist())));

		String[] references = field.getReferenceTo(); 
		
		if(null != references)
		{
			Element referencesXML = new Element("ReferenceTo");
			fieldXML.addContent(referencesXML);
			
			for(int i = 0; i < references.length; i++)
			{
				Element reference = new Element("Reference");
				reference.addContent(references[i]);
				
				referencesXML.addContent(reference);
			}
		}
		
		PicklistEntry picklistValues[] = field.getPicklistValues();
		
		if (picklistValues != null)
		{
			if (field.isRestrictedPicklist())
			{
				Element pickList = new Element("PickList");
				fieldXML.addContent(pickList);
				for (int p = 0; p < picklistValues.length; p++)
				{
					Element value = new Element("value");
					value.addContent(picklistValues[p].getValue());
					
					pickList.addContent(value);
				}
			}
		}
	}
	
	/**
	 * @param args
	 * Used to test the class
	 */
	public static void main(String[] args)
	{
		String baseName = "base.xml";
		try
		{
			File outDir = Util.getOutputDir("salesforce_get_schema", false);
			File base = new File(outDir.getCanonicalPath() +  File.separator + baseName);
			
			if(base.exists())
			{
				//File file = Util.getNewFileName(outDir);
				File file = new File(outDir.getCanonicalPath() +  File.separator + "base_saved.xml");
				//Util.writeXMLFile(doc, file);
				
				StringBuffer diff = XmlDiff.diff(base, file);
				
				//Util.writeXMLFile(new Document().setRootElement(diff), diffFile);
				System.out.println("Difference was: \n" + diff);
			}else
			{
				//Util.writeXMLFile(doc, base);
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
