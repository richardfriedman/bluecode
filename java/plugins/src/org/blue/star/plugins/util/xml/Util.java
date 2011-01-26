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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import org.blue.star.plugins.common_h;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author Mark
 *
 */
public class Util
{
	public static void writeXMLFile(Document doc, File file) throws FileNotFoundException,IOException
	{
		XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
		FileOutputStream output;
		
		if(!file.exists())
		{
			file.createNewFile();
		}
		
		output = new FileOutputStream(file);

		outputter.output(doc,output);
		output.close();
	}
	
	/**
	 * @param module
	 * @param isTemplate
	 * @return
	 */
	public static File getOutputDir(String module, boolean isTemplate)
	{
		File file;
		String outputDirectory = "./etc";
		
		String base = "/xml" + ((module != null && module.length() > 0) ? "/" + module : "");
		
		if(isTemplate)
			file = new File(outputDirectory + base + "/templates");
		else
			file = new File(outputDirectory + base);
				
		file.mkdirs();
		
		return file;
	}
	
	/**
	 * @param directory
	 * @return
	 */
	public static File getNewFileName(File directory)
	{
		Calendar c = Calendar.getInstance();
		String name = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + 
			c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR) + "-" + c.get(Calendar.MINUTE) + "-" + 
			c.get(Calendar.SECOND)  + "-" + c.get(Calendar.AM_PM);
		
		return new File(directory + File.separator + common_h.SF_HISTORY_PREFIX + name + ".xml");
	}
	
	/**
	 * @param args
	 * Used as a test method
	 */
	public static void main(String[] args)
	{
		System.out.println(Util.getNewFileName(new File("c:\\")));
	}
}
