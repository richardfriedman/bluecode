package org.blue.star.plugins.util.xml;
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
import java.util.Collection;
import java.util.Iterator;

import fr.loria.ecoo.so6.xml.node.Document;
import fr.loria.ecoo.so6.xml.node.ElementNode;
import fr.loria.ecoo.so6.xml.node.TreeNode;
import fr.loria.ecoo.so6.xml.util.XmlUtil;
import fr.loria.ecoo.so6.xml.xydiff.DeleteAttribute;
import fr.loria.ecoo.so6.xml.xydiff.DeleteNode;
import fr.loria.ecoo.so6.xml.xydiff.DeltaConstructor;
import fr.loria.ecoo.so6.xml.xydiff.InsertAttribute;
import fr.loria.ecoo.so6.xml.xydiff.InsertNode;
import fr.loria.ecoo.so6.xml.xydiff.UpdateAttribute;
import fr.loria.ecoo.so6.xml.xydiff.XyDiff;


/**
 * @author Mark
 *
 */
public class XmlDiff
{
	/**
		
	 */
	public static StringBuffer diff(File base, File file) 
	{
		StringBuffer out = new StringBuffer(512);
		System.out.println(base);
		System.out.println(file);
		
		try {
			XyDiff xydiff = new XyDiff(base.getAbsolutePath(), file.getAbsolutePath());

			DeltaConstructor delta = xydiff.diff() ;
			
			Collection c = delta.getXMLCommand();
			Iterator it = c.iterator();
			
			Document d = delta.getDeltaDocument();
			System.out.println(d);
			
			Document baseDoc = XmlUtil.load(base.getAbsolutePath());
			Document fileDoc = XmlUtil.load(file.getAbsolutePath());
			
			//Document doc = delta.getDeltaDocument();
			//Iterator it = doc.getChildren().iterator();
			/*
			– insert node
			– delete node
			– move node
			– update node
			– insert attribute
			– delete attribute
			– update attribute
			 */
			while(it.hasNext())
			{
				Object o = it.next();
				if(o instanceof DeleteNode)
				{
					//System.out.println("Got a DeleteNode!");
					DeleteNode dn = (DeleteNode)o;
					if(!dn.getIsMoved())
					{
						TreeNode node = baseDoc.getNode(dn.getNodePath());
						SfXmlPathBean bean = getPath(node, baseDoc);
						out.append("Item Deleted From Salesforce Schema: \n");
						out.append("Object Name: " + bean.getObjectName() + "\n");
						out.append("Field Name: " + bean.getFieldName() + "\n");
						out.append("Element Changed: " + bean.getName() + "\n");
						out.append("Element that Changed: " + bean.getPath() + "\n");
						out.append("Element Value: " + bean.getValue() + "\n");
						out.append("--------------------------------------------\n");
					}
				}else if(o instanceof InsertNode)
				{
					InsertNode in = (InsertNode)o;
					if(!in.getIsMoved())
					{
						TreeNode node = fileDoc.getNode(in.getNodePath());
						SfXmlPathBean bean = getPath(node, fileDoc);
						out.append("Item Inserted Into The Salesforce Schema: \n");
						out.append("Object Name: " + bean.getObjectName() + "\n");
						out.append("Field Name: " + bean.getFieldName() + "\n");
						out.append("Element Changed: " + bean.getName() + "\n");
						out.append("Element that Changed: " + bean.getPath() + "\n");
						out.append("Element Value: " + bean.getValue() + "\n");
						out.append("--------------------------------------------\n");
					}
				}else if(o instanceof InsertAttribute)
				{
					System.out.println("Got an InsertAttribute!");
				}else if(o instanceof DeleteAttribute)
				{
					System.out.println("Got a DeleteAttribute!");
				}else if(o instanceof UpdateAttribute)
				{
					System.out.println("Got an UpdateAttribute!");
				}else
					System.out.println("Should not be possible: " + o.getClass().getCanonicalName());
			}

			return out;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private static SfXmlPathBean getPath(TreeNode node, Document fileDoc)
	{
		/*
		 * CDataNode
		 * CommentNode
		 * DocTypeNode
		 * ProcessingInstructionNode
		 * TextNode
		 */
		if(node instanceof ElementNode)
		{
			return computePath((ElementNode)node);
		}else
		{
			TreeNode parent = node.getParent();
			if(parent instanceof ElementNode)
			{
				return computePath((ElementNode)parent);
			}else
				System.out.println("parent was not an ElementNode: " + parent.getClass().getName());
			
		}
		
		return null;
	}
	
	private static SfXmlPathBean computePath(ElementNode parent)
	{
		SfXmlPathBean bean = new SfXmlPathBean();
		bean.setName(parent.getElementName());
		bean.setValue(parent.toString());
		
		String path = "";
		while(null != parent)
		{
			String name = parent.getElementName();
			if(name.equalsIgnoreCase("Object"))
			{
				bean.setObjectName(parent.getAttribute("name"));
			}else if(name.equalsIgnoreCase("field"))
			{
				bean.setFieldName(parent.getAttribute("name"));
			}
			
			path = name + "/" + path;
			TreeNode parentNode = parent.getParent();
			if(parentNode instanceof ElementNode)
			{
				parent = (ElementNode)parent.getParent();
			}else
			{
				// At the end it's always a document, so this output line is useless
				//System.out.println("ParentNode was not an ElementNode: " + parentNode.getClass().getName());
				parent = null;
			}
		}
		path = "/" + path;
		bean.setPath(path);
		return bean;
	}
}



