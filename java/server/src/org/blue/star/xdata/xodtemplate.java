/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
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

package org.blue.star.xdata;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;
import org.blue.star.include.locations_h;
import org.blue.star.include.objects_h;

public class xodtemplate
{

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.xdata.xodtemplate");
    public static String cn = "org.blue.xdata.xodtemplate"; 

    public static ArrayList<xodtemplate_h.xodtemplate_timeperiod> xodtemplate_timeperiod_list = null ;  
    public static ArrayList<xodtemplate_h.xodtemplate_command> xodtemplate_command_list = null ;   
    public static ArrayList<xodtemplate_h.xodtemplate_contactgroup> xodtemplate_contactgroup_list=null;  
    public static ArrayList<xodtemplate_h.xodtemplate_hostgroup> xodtemplate_hostgroup_list=null;  
    public static ArrayList<xodtemplate_h.xodtemplate_servicegroup> xodtemplate_servicegroup_list=null;  
    public static ArrayList<xodtemplate_h.xodtemplate_servicedependency> xodtemplate_servicedependency_list=null;  
    public static ArrayList<xodtemplate_h.xodtemplate_serviceescalation> xodtemplate_serviceescalation_list=null;  
    public static ArrayList<xodtemplate_h.xodtemplate_contact> xodtemplate_contact_list=null; 
    public static ArrayList<xodtemplate_h.xodtemplate_host> xodtemplate_host_list=null; 
    public static ArrayList<xodtemplate_h.xodtemplate_service> xodtemplate_service_list=null; 
    public static ArrayList<xodtemplate_h.xodtemplate_hostdependency> xodtemplate_hostdependency_list=null; 
    public static ArrayList<xodtemplate_h.xodtemplate_hostescalation> xodtemplate_hostescalation_list=null;  
    public static ArrayList<xodtemplate_h.xodtemplate_hostextinfo> xodtemplate_hostextinfo_list=null; 
    public static ArrayList<xodtemplate_h.xodtemplate_serviceextinfo> xodtemplate_serviceextinfo_list=null;  

    public static Object xodtemplate_current_object= null;
    public static int xodtemplate_current_object_type = xodtemplate_h.XODTEMPLATE_NONE;

    public static int xodtemplate_current_config_file=0;
    public static ArrayList<String> xodtemplate_config_files = null;

    public static String xodtemplate_cache_file = null;

/******************************************************************/
    /************* TOP-LEVEL CONFIG DATA INPUT FUNCTION ***************/
    /******************************************************************/

    private static void xodtemplate_initialize() {
       /* initialise stores for our main config data. */

       xodtemplate_timeperiod_list = new ArrayList<xodtemplate_h.xodtemplate_timeperiod>();
       xodtemplate_command_list = new ArrayList<xodtemplate_h.xodtemplate_command>(); 
       xodtemplate_contactgroup_list = new ArrayList<xodtemplate_h.xodtemplate_contactgroup>();
       xodtemplate_hostgroup_list = new ArrayList<xodtemplate_h.xodtemplate_hostgroup>();
       xodtemplate_servicegroup_list = new ArrayList<xodtemplate_h.xodtemplate_servicegroup>();
       xodtemplate_servicedependency_list = new ArrayList<xodtemplate_h.xodtemplate_servicedependency>();
       xodtemplate_serviceescalation_list = new ArrayList<xodtemplate_h.xodtemplate_serviceescalation>();
       xodtemplate_contact_list = new ArrayList<xodtemplate_h.xodtemplate_contact>();
       xodtemplate_host_list = new ArrayList<xodtemplate_h.xodtemplate_host>();
       xodtemplate_service_list = new ArrayList<xodtemplate_h.xodtemplate_service>(); 
       xodtemplate_hostdependency_list = new ArrayList<xodtemplate_h.xodtemplate_hostdependency>(); 
       xodtemplate_hostescalation_list = new ArrayList<xodtemplate_h.xodtemplate_hostescalation>();
       xodtemplate_hostextinfo_list = new ArrayList<xodtemplate_h.xodtemplate_hostextinfo>();
       xodtemplate_serviceextinfo_list = new ArrayList<xodtemplate_h.xodtemplate_serviceextinfo>();

       /* Set the current object to null and the current type of that object to 'NONE' */  

       xodtemplate_current_object=null;
       xodtemplate_current_object_type= xodtemplate_h.XODTEMPLATE_NONE;

       xodtemplate_current_config_file=0;
       xodtemplate_config_files = new ArrayList<String>();
       
    }
    
    public static int xodtemplate_read_dynamic_data (String main_config_file, String temp_file, int options, int cache) {

       int result= common_h.OK;

       logger.trace( "entering " + cn + ".xodtemplate_read_dynamic_data()");

       /* Get the object_cache_file location from the main config file 
        * and then set blue.macro_x[blue_h.MACRO_OBJECTCACHEFILE] to that value.*/

       xodtemplate_grab_config_info(main_config_file);
       logger.debug("object_cache_file: " + blue.macro_x[blue_h.MACRO_OBJECTCACHEFILE]);        

       xodtemplate_initialize();
       
       result=xodtemplate_process_config_file(temp_file,options);
       
       logger.debug( "Process config file " + result );

        /* register objects */
		if (result == common_h.OK)
		{
			result = xodtemplate_register_dynamic_objects();
			
			logger.debug("Registering objects " + result);
		
			if (result == common_h.OK)
			{
				xodcache.cache_objects(blue.macro_x[blue_h.MACRO_OBJECTCACHEFILE]);
			}
		}

       /* cleanup */
       xodtemplate_free_memory();

       logger.trace( "exiting " + cn + ".xodtemplate_read_dynamic_data");
       
       return result;
    }
    
/* process all config files - both core and CGIs pass in name of main config file */
public static int xodtemplate_read_config_data(String main_config_file, int options, int cache)  {

   String config_file;
	String input= null;

	blue_h.mmapfile thefile;
	int result= common_h.OK;

	logger.trace( "entering " + cn + ".xodtemplate_read_config_data()");

	/* Get the object_cache_file location from the main config file 
	 * and then set blue.macro_x[blue_h.MACRO_OBJECTCACHEFILE] to that value.*/
	
	xodtemplate_grab_config_info(main_config_file);
	logger.info("object_cache_file: " + blue.macro_x[blue_h.MACRO_OBJECTCACHEFILE]);		

	/* open the main config file for reading (we need to find all the config files to read) */
	if((thefile = utils.file_functions.mmap_fopen(main_config_file)) == null)
	{
		logger.fatal("Error: Cannot open main configuration file '"+main_config_file+"' for reading!");
		return common_h.ERROR;
	}

    xodtemplate_initialize();

if(blue.is_core)
{
	/* daemon reads all config files/dirs specified in the main config file */
	/* read in all lines from the main config file */
	
	while(true)
	{
	    
	    /* get the next line */
	    if((input= utils.file_functions.mmap_fgets(thefile))== null)
	        break;
	    
	    /* strip input */
	    input = input.trim();
	    
	    /* skip blank lines and comments */
	    if(input.length() == 0  || input.charAt(0)=='#' || input.charAt(0)==';')
	        continue;
	    
	    String[] variable_value = input.split( "=", 2);
	    if ( variable_value == null && variable_value.length == 2 )
	        continue;
	    
	    /* Find the config files that we need to process from the main cfg file. */
	    if(variable_value[0].equals("xodtemplate_config_file") || variable_value[0].equals("cfg_file"))
	    {
	        config_file = variable_value[1].trim();
	        
	        if(config_file.length() == 0)
	            continue;
	        	        
	        result = xodtemplate_process_config_file(config_file,options);
	        
	        if (result == common_h.ERROR)
	            break;
	    }
	    
	    /* Find the config directories that we need to process from the main cfg file */
	    else if(variable_value[0].equals("xodtemplate_config_dir") || variable_value[0].equals("cfg_dir"))
	    {
	        config_file = variable_value[1].trim();
	        	        	        
	        if(config_file.length() == 0)
	            continue;
	        
	        /* Remember to strip off any trailing slashes */
	        if(config_file.endsWith( "\\") || config_file.endsWith( "/" ))
	            config_file = config_file.substring( 0 , config_file.length() - 1 );
	        
	        result = xodtemplate_process_config_dir(config_file,options) ;
	        
	        if(result == common_h.ERROR)
	            break;
	    }
	    
	}

	/* close the file */
    utils.file_functions.mmap_fclose(thefile);
}

 /* CGIs process only one file - the cached objects file */
if (!blue.is_core)
    result=xodtemplate_process_config_file(xodtemplate_cache_file,options);

if (blue.is_core) 
{
	/* resolve objects definitions */
	if(result== common_h.OK)
		result=xodtemplate_resolve_objects();

	/* do the meat and potatoes stuff... */
	if(result==common_h.OK)
		result=xodtemplate_recombobulate_contactgroups();
	if(result==common_h.OK)
		result=xodtemplate_recombobulate_hostgroups();
	if(result==common_h.OK)
		result=xodtemplate_duplicate_services();
	if(result==common_h.OK)
		result=xodtemplate_recombobulate_servicegroups();
	if(result==common_h.OK)
		result=xodtemplate_duplicate_objects();

	/* sort objects */
	if(result==common_h.OK)
		result=xodtemplate_sort_objects();

	/* cache object definitions */
	if(result==common_h.OK && cache==common_h.TRUE)
		xodtemplate_cache_objects(xodtemplate_cache_file);
}

	/* register objects */
	if(result== common_h.OK)
		result = xodtemplate_register_objects();

	/* cleanup */
	xodtemplate_free_memory();

	logger.trace( "exiting " + cn + ".xodtemplate_read_config_data");
	return result;
	}



/**
 * Method that returns the location of the object_cache_file variable from the main
 * blue.cfg file
 * 
 * @param String main_config_file, the name of the config file where object_cache_file variable is set.

 * @return int, common_h.OK if everything is hunkydory, common_h.ERROR otherwise.
 */

public static int xodtemplate_grab_config_info(String main_config_file)
{
    String input= null ;
    blue_h.mmapfile thefile;
    
    logger.trace("entering " + cn + ".xodtemplate_grab_config_info");
    
    /* default location of cached object file */
    xodtemplate_cache_file = locations_h.DEFAULT_OBJECT_CACHE_FILE;
    
    /* open the main config file for reading, or if we can't open it return an error. */

    if((thefile=utils.file_functions.mmap_fopen(main_config_file))== null)
        return common_h.ERROR;
    
    /* read in all lines from the main config file */
    
    while(true)
    {
        
        /* read the next line */
        if((input=utils.file_functions.mmap_fgets(thefile))== null)
            break;
        
        /* strip input */
        input = input.trim();
        
        /* skip blank lines and comments */
        
        if(input.length() == 0 || input.charAt(0)=='#' || input.charAt(0)==';')
            continue;
        
        /* Get the variable and the value of the variable */
        
        String[] variable_value = input.split( "=", 2);
        if (variable_value == null && variable_value.length != 2)
            continue;
        
        /* If we find the object_cache_file variable, use it to override the default
         * location specified within locations_h.DEFAULT_OBJECT_CACHE_FILE*/
        
        if (variable_value[0].startsWith("object_cache_file")) 
            xodtemplate_cache_file = variable_value[1].trim();
        
    }
    
    /* close the file */
    utils.file_functions.mmap_fclose(thefile);
    
    /* save the object cache file macro within our main blue config. */
    
    blue.macro_x [blue_h.MACRO_OBJECTCACHEFILE] = xodtemplate_cache_file;
    
    logger.trace("exiting " + cn + ".xodtemplate_grab_config_info");
    return common_h.OK;
}


/**
 * Method that processes all files within a specified directory.
 * @param String dirname, the directory name that is to be processed.
 * @param options
 * 
 * @return int, common_h.OK if everything went to plan, common_h.ERROR otherwise.
 */

public static int xodtemplate_process_config_dir(String dirname, int options)
{
    int result = common_h.OK;
    logger.trace( "entering " + cn + ".xodtemplate_process_config_dir" );
    try
    {
        String[] files = new File(dirname).list();
        
        if(files != null)
        {
            for (int i = 0;i < files.length && result!= common_h.ERROR;i++)
            {
                File file = new File(dirname, files[i]);
                
                /* If the specified file is a directory, recursively call this method! */
                if(file.isDirectory())
                {
                    result = xodtemplate_process_config_dir( file.toString(), options );
                }
                
                /* Otherwise process the file using xodtemplate_process_config_file() assuming the suffix
                 * of the file is .cfg.  Skip hiddent config files ( those which start with '.' )
                 */
                else if(files[i].endsWith(".cfg") && !files[i].startsWith( "." ))
                {
                   result = xodtemplate_process_config_file( file.toString() ,options);
                }
            }
        }
    }
    catch (Exception e)
    {
        logger.fatal( "Error: Could not open config directory '"+dirname+"' for reading.");
        result = common_h.ERROR;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_process_config_dir" );
    
    return result;
}


/**
 * Method that processes the data within a given config file.
 * 
 * @param String filename, the name of the config file that you wish to process.
 * @param options
 * 
 * @return int, common_h.OK if everything is hunkey dorey, common_h.ERROR otherwise.
 */

public static int xodtemplate_process_config_file(String filename, int options)
{
	blue_h.mmapfile thefile;
	String input = null;
	int in_definition = common_h.FALSE;
	int current_line = 0;
	int result = common_h.OK;

	logger.trace( "entering " + cn + ".xodtemplate_process_config_file()");
	    
	/* Save the name of the config file that we are working on and grab an identifier
	 * for it by querying the size of the current config files list. */
	
	xodtemplate_config_files.add(filename);
    xodtemplate_current_config_file = xodtemplate_config_files.size(); 

    /* open the config file for reading */
    
    if((thefile = utils.file_functions.mmap_fopen(filename))== null )
    {
        logger.fatal("Error: Cannot open config file '"+filename+"' for reading ");
        return common_h.ERROR;
    }

	/* read in all lines from the config file */
	
    while(true)
    {

		/* read the next line */
		if((input= utils.file_functions.mmap_fgets(thefile))==null)
			break;

		current_line++;

        /* strip input */
        input = input.trim();

        /* grab data before comment delimiter - faster than a strtok() and strncpy()... */
        
        int commentIndex = input.indexOf(';');
        
        /* If the line does not begin with a comment character */
        
        if (commentIndex > 0)
        {
            input = input.substring(0,commentIndex);
            input = input.trim();
        }
        
        /* skip empty/blank lines */
		
        if(input.length() == 0 || input.charAt(0) =='#' || input.charAt(0) ==';')
			continue;


		/* this is the start of an object definition */
        
        if (input.startsWith("define"))
		{
		    /* Move 6 characters into the string (leap over 'define' statement) */
        	input = input.substring(6).trim();
		    
        	/* Find the first index of '{' character, anything before this will be our object type! */
        	int bIndex = input.indexOf('{');
        	
        	/* If there is no index of '{' character, we have a malformed object definition. */
        	if (bIndex == 0) 
		        input = "";
		    
		    /* Otherwise retrieve the string that represents the type of object definition */
		    else if (bIndex > 0 )
		        input = input.substring(0,bIndex).trim();
		    
		    /* make sure an object type is specified... */
		    
		    if(input.length() == 0)
		    {
		        logger.fatal( "Error: No object type specified in file '"+filename+"' on line "+current_line+".");
		        result=common_h.ERROR;
		        break;
		    }
		    
		    /* check validity of object type */

		    if (!input.equals("timeperiod") && !input.equals("command") && !input.equals("contact") && !input.equals( "contactgroup") && !input.equals( "host") && !input.equals( "hostgroup") && !input.equals( "servicegroup") && !input.equals( "service") && !input.equals( "servicedependency") && !input.equals( "serviceescalation") && !input.equals( "hostdependency") && !input.equals( "hostescalation") && !input.equals( "hostextinfo") && !input.equals( "serviceextinfo") ) {
		        logger.fatal( "Error: Invalid object definition type "+input+" in file '"+filename+"' on line "+current_line+".");
		        result=common_h.ERROR;
		        break;
		    }
		    
            logger.debug("processing " + input);
		    
            /* we're already in an object definition... */
		    if(in_definition==common_h.TRUE)
		    {
		        logger.fatal( "Error: Unexpected start of object definition in file '"+filename+"' on line "+current_line+".");
		        result=common_h.ERROR;
		        break;
		    }
		    
		    
		    /* start a new object definition */
		    
		    /* parameters to xodtemplate_begin_object_definition =
		     * input - the current string value
		     * options - int of the options value
		     * int - the number id of the current config file
		     * the current line of the config file that we are on.
		     */
		    
		    /* If there is an error beginning the object definition then break out of config building */
		    
		    if(xodtemplate_begin_object_definition(input,options,xodtemplate_current_config_file,current_line) == common_h.ERROR )
		    {
		        logger.fatal( "Error: Could not add object definition in file '"+filename+"' on line "+current_line+".");
		        result=common_h.ERROR;
		        break;
		    }
		    
		    /* Identify that we are within the body of an object definition */
		    in_definition= common_h.TRUE;
		}
		
		/* A '}' character signifies the end of an object definition, if we are already in an object
		 * definition then we must have reached the end. */
        
		else if (input.equals("}") && in_definition== common_h.TRUE )
		{
		    /* Identify that we are no longer within the body of an object definition */
			in_definition= common_h.FALSE;
		    
		    /* End the object definition */
			/* Updated xodtemplate_end_object_definition method so that this check is actually fruitful */
			
		    if(xodtemplate_end_object_definition(options)== common_h.ERROR)
		    {
		        logger.fatal( "Error: Could not complete object definition in file '"+filename+"' on line "+current_line+".");
		        result=common_h.ERROR;
		        break;
		    }
		}

		/* we're currently inside an object definition */
		else if(in_definition== common_h.TRUE)
		{
		    // TODO removed chunk of code..., not sure this is possible! else if above checks in_def and for}
		    
			/* Begin to add properties to the objects that we have created. */
		    
			if(xodtemplate_add_object_property(input,options) == common_h.ERROR)
		    {
		        logger.fatal( "Error: Could not add object property in file '"+filename+"' on line "+current_line+".");
		        result=common_h.ERROR;
		        break;
		    }
		}

		/* Deals with processing another config file that has been specified through the 'include_file'
		 * directive within the config file */
        
		else if(input.startsWith("include_file="))
		{
		    
		    int eIndex = input.indexOf('=');
		    if (eIndex > 0 && !input.endsWith( "=" ))
		    {
		        /* Recursively call the xodtemplate_process_config_file() method to deal with any
		         * files specified by the user. */
		    	result = xodtemplate_process_config_file(input.substring(eIndex + 1).trim(),options);
		        
		    	if(result == common_h.ERROR)
		            logger.fatal("Error: Could not properly parse include_file: " + input.substring(eIndex + 1).trim());
		    		break;
		    }
		}
		
		/* Deals with processing a directory that has been specified through the 'include_dir'
		 * directive within the config file */
		
		else if( input.startsWith("include_dir"))
		{
		    int eIndex = input.indexOf('=');
		    
		    if (eIndex > 0 && !input.endsWith("="))
		    {
		        result=xodtemplate_process_config_dir(input.substring(eIndex + 1).trim(),options);
		        if(result== common_h.ERROR)
		            logger.fatal("Error: Could not properly parse config files within include_dir: " + input.substring(eIndex + 1).trim());
		        	break;
		    }
		}

		/* unexpected token or statement */
		else
		{
		    logger.fatal("Error: Unexpected token or statement in file '"+filename+"' on line "+current_line+".");
		    result=common_h.ERROR;
		    break;
		}
    }

	/* close file */
    utils.file_functions.mmap_fclose(thefile);

	/* whoops - EOF while we were in the middle of an object definition... */
	if(in_definition == common_h.TRUE && result == common_h.OK)
	{
		logger.fatal("Error: Unexpected EOF in file '"+filename+"' on line "+current_line+" - check for a missing closing bracket.");
		result=common_h.ERROR;
	}

    logger.trace("exiting " + cn + ".xodtemplate_process_config_file");
	return result;
	
   }

/******************************************************************/
/***************** OBJECT DEFINITION FUNCTIONS ********************/
/******************************************************************/

/* starts a new object definition */
public static int xodtemplate_begin_object_definition(String input, int options, int config_file, int start_line)
{
    int result= common_h.OK;
    
    logger.trace( "entering " + cn + ".xodtemplate_begin_object_definition");
    
    /* Set the type of the object that we are about to create */ 
    
    if (input.equals("service"))xodtemplate_current_object_type= xodtemplate_h.XODTEMPLATE_SERVICE;
    else if( input.equals("host"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_HOST;
    else if( input.equals("command"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_COMMAND;
    else if( input.equals("contact"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_CONTACT;
    else if( input.equals("contactgroup"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_CONTACTGROUP;
    else if( input.equals("hostgroup"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_HOSTGROUP;
    else if( input.equals("servicegroup"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_SERVICEGROUP;
    else if( input.equals("timeperiod"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_TIMEPERIOD;
    else if( input.equals("servicedependency"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_SERVICEDEPENDENCY;
    else if( input.equals("serviceescalation"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_SERVICEESCALATION;
    else if( input.equals("hostdependency"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_HOSTDEPENDENCY;
    else if( input.equals("hostescalation"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_HOSTESCALATION;
    else if( input.equals("hostextinfo"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_HOSTEXTINFO;
    else if( input.equals("serviceextinfo"))xodtemplate_current_object_type=xodtemplate_h.XODTEMPLATE_SERVICEEXTINFO;
    else
        return common_h.ERROR;
    
    
    /* check to see if we should process this type of object */
    switch(xodtemplate_current_object_type)
    {
    
    /* Check to see if we process timeperiod objects */
    case xodtemplate_h.XODTEMPLATE_TIMEPERIOD:
        if((options & common_h.READ_TIMEPERIODS)==0)
            return common_h.OK;
        break;
    
    /* Check to see if we process command objects */
    case xodtemplate_h.XODTEMPLATE_COMMAND:
        if((options & common_h.READ_COMMANDS) == 0)
            return common_h.OK;
        break;
    
    /* Check to see if we process contact objects */
    case xodtemplate_h.XODTEMPLATE_CONTACT:
        if((options & common_h.READ_CONTACTS) == 0)
            return common_h.OK;
        break;
    
    /* Check to see if we process contactgroup objects */
    case xodtemplate_h.XODTEMPLATE_CONTACTGROUP:
        if((options & common_h.READ_CONTACTGROUPS) == 0)
            return common_h.OK;
        break;
    
    /* Check to see if we process host objects*/
    case xodtemplate_h.XODTEMPLATE_HOST:
        if((options & common_h.READ_HOSTS) == 0)
            return common_h.OK;
        break;
    
    /* Check to see if we process hostgroup objects */
    case xodtemplate_h.XODTEMPLATE_HOSTGROUP:
        	if((options & common_h.READ_HOSTGROUPS) == 0)
            return common_h.OK;
        break;
    
    /* Check to see if we process servicegroup objects */
    case xodtemplate_h.XODTEMPLATE_SERVICEGROUP:
        if((options & common_h.READ_SERVICEGROUPS) == 0 )
            return common_h.OK;
        break;

    /* Check to see if we process service objects */
    case xodtemplate_h.XODTEMPLATE_SERVICE:
        if((options & common_h.READ_SERVICES) == 0 )
            return common_h.OK;
        break;
    
    /* Check to see if we process service dependency objects */
    case xodtemplate_h.XODTEMPLATE_SERVICEDEPENDENCY:
        if((options & common_h.READ_SERVICEDEPENDENCIES) == 0 )
            return common_h.OK;
        break;
    
    /* Check to see if we process service escalation objects */
    case xodtemplate_h.XODTEMPLATE_SERVICEESCALATION:
        if((options & common_h.READ_SERVICEESCALATIONS) == 0 )
            return common_h.OK;
        break;
    
    /* Check to see if we process host dependency objects */
    
    case xodtemplate_h.XODTEMPLATE_HOSTDEPENDENCY:
        if((options & common_h.READ_HOSTDEPENDENCIES) == 0 )
            return common_h.OK;
        break;
    
    /* Check to see if we process host escalation objects */
    case xodtemplate_h.XODTEMPLATE_HOSTESCALATION:
        if((options & common_h.READ_HOSTESCALATIONS) == 0 )
            return common_h.OK;
        break;
    
    /* Check to see if we process host extended info objects */
    case xodtemplate_h.XODTEMPLATE_HOSTEXTINFO:
        if((options & common_h.READ_HOSTEXTINFO) == 0 )
            return common_h.OK;
        break;
    
    /* Check to see if we process service extended info objects */
    case xodtemplate_h.XODTEMPLATE_SERVICEEXTINFO:
        if((options & common_h.READ_SERVICEEXTINFO) == 0 )
            return common_h.OK;
        break;
    
    default:
        
    	/* If we don't have a recognised object type, error */
    	return common_h.ERROR;
    }
    
    /* add a new (blank) object */
    switch(xodtemplate_current_object_type)
    {
    
    case xodtemplate_h.XODTEMPLATE_TIMEPERIOD:
        
        
    	/* Create new timeperiod object */
    	xodtemplate_h.xodtemplate_timeperiod new_timeperiod= new xodtemplate_h.xodtemplate_timeperiod();
        
    	/* Set some initial default attributes of the timeperiod object */
    	
        new_timeperiod.template= null;
        new_timeperiod.name= null;
        new_timeperiod.timeperiod_name=null;
        new_timeperiod.alias=null;
        for(int x=0;x<7;x++)
            new_timeperiod.timeranges[x]= null;
        new_timeperiod.has_been_resolved= common_h.FALSE;
        new_timeperiod.register_object= common_h.TRUE;
        
        /* Set the config file that the definition for this timeperiod can be found in */
        new_timeperiod._config_file = config_file;
        
        /* Set the line within said config file upon which the definition for this object begins */
        new_timeperiod._start_line= start_line;
        
        /* add new timeperiod to head of list in memory */
        xodtemplate_timeperiod_list.add(new_timeperiod);
        
        /* update current object pointer */
        xodtemplate_current_object = new_timeperiod;
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_COMMAND:
        
    	/* Create a new instance of the xodtemplate_command object */
        xodtemplate_h.xodtemplate_command new_command= new xodtemplate_h.xodtemplate_command();
        
        /* Set some initial attributes of the command object */
        new_command.template=null;
        new_command.name=null;
        new_command.command_name=null;
        new_command.command_line=null;
        new_command.has_been_resolved=common_h.FALSE;
        new_command.register_object=common_h.TRUE;
        
        /* Identify the config file that this object is defined within */
        new_command._config_file=config_file;
        
        /* Set the line that the definition for this object begins on */
        new_command._start_line=start_line;
        
        /* add new command to head of list in memory */
        xodtemplate_command_list.add( new_command );
        
        /* update current object pointer */
        xodtemplate_current_object = new_command;
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_CONTACTGROUP:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_contactgroup new_contactgroup = new xodtemplate_h.xodtemplate_contactgroup();
        
        new_contactgroup.template= null;
        new_contactgroup.name= null;
        new_contactgroup.contactgroup_name= null;
        new_contactgroup.alias= null;
        new_contactgroup.members= null;
        new_contactgroup.has_been_resolved= common_h.FALSE;
        new_contactgroup.register_object= common_h.TRUE;
        new_contactgroup._config_file=config_file;
        new_contactgroup._start_line=start_line;
        
        /* add new contactgroup to head of list in memory */
        xodtemplate_contactgroup_list.add( new_contactgroup );
        
        /* update current object pointer */
        xodtemplate_current_object=new_contactgroup;
        break;
        
        
    case xodtemplate_h.XODTEMPLATE_HOSTGROUP:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_hostgroup new_hostgroup= new xodtemplate_h.xodtemplate_hostgroup();
        
        new_hostgroup.template= null;
        new_hostgroup.name= null;
        new_hostgroup.hostgroup_name= null;
        new_hostgroup.alias= null;
        new_hostgroup.members= null;
        new_hostgroup.has_been_resolved= common_h.FALSE;
        new_hostgroup.register_object= common_h.TRUE;
        new_hostgroup._config_file=config_file;
        new_hostgroup._start_line=start_line;
        
        /* add new hostgroup to head of list in memory */
        xodtemplate_hostgroup_list.add(new_hostgroup);
        
        /* update current object pointer */
        xodtemplate_current_object= new_hostgroup;
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICEGROUP:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_servicegroup new_servicegroup = new xodtemplate_h.xodtemplate_servicegroup();
        
        new_servicegroup.template=null;
        new_servicegroup.name=null;
        new_servicegroup.servicegroup_name=null;
        new_servicegroup.alias=null;
        new_servicegroup.members= null;
        new_servicegroup.has_been_resolved=common_h.FALSE;
        new_servicegroup.register_object=common_h.TRUE;
        new_servicegroup._config_file=config_file;
        new_servicegroup._start_line=start_line;
        
        /* add new servicegroup to head of list in memory */
        xodtemplate_servicegroup_list.add(new_servicegroup);
        
        /* update current object pointer */
        xodtemplate_current_object=new_servicegroup;
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICEDEPENDENCY:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_servicedependency new_servicedependency= new xodtemplate_h.xodtemplate_servicedependency();
        
        new_servicedependency.template=null;
        new_servicedependency.name=null;
        new_servicedependency.servicegroup_name=null;
        new_servicedependency.hostgroup_name=null;
        new_servicedependency.host_name=null;
        new_servicedependency.service_description=null;
        new_servicedependency.dependent_servicegroup_name=null;
        new_servicedependency.dependent_hostgroup_name=null;
        new_servicedependency.dependent_host_name=null;
        new_servicedependency.dependent_service_description=null;
        new_servicedependency.inherits_parent=common_h.FALSE;
        new_servicedependency.fail_execute_on_ok=common_h.FALSE;
        new_servicedependency.fail_execute_on_unknown=common_h.FALSE;
        new_servicedependency.fail_execute_on_warning=common_h.FALSE;
        new_servicedependency.fail_execute_on_critical=common_h.FALSE;
        new_servicedependency.fail_execute_on_pending=common_h.FALSE;
        new_servicedependency.fail_notify_on_ok=common_h.FALSE;
        new_servicedependency.fail_notify_on_unknown=common_h.FALSE;
        new_servicedependency.fail_notify_on_warning=common_h.FALSE;
        new_servicedependency.fail_notify_on_critical=common_h.FALSE;
        new_servicedependency.fail_notify_on_pending=common_h.FALSE;
        new_servicedependency.have_inherits_parent=common_h.FALSE;
        new_servicedependency.have_execution_dependency_options=common_h.FALSE;
        new_servicedependency.have_notification_dependency_options=common_h.FALSE;
        new_servicedependency.has_been_resolved=common_h.FALSE;
        new_servicedependency.register_object=common_h.TRUE;
        new_servicedependency._config_file=config_file;
        new_servicedependency._start_line=start_line;
        
        /* add new servicedependency to head of list in memory */
        xodtemplate_servicedependency_list.add( new_servicedependency );
        
        /* update current object pointer */
        
        //TODO - Rob 12/01/07 - Surely this should be xodtemplate_current_object = new_servicedependency
        //otherwise it's going to throw a ClassCastException later on!
        
        //xodtemplate_current_object=xodtemplate_servicedependency_list;
        xodtemplate_current_object = new_servicedependency;
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICEESCALATION:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_serviceescalation new_serviceescalation= new xodtemplate_h.xodtemplate_serviceescalation();
        
        new_serviceescalation.template=null;
        new_serviceescalation.name=null;
        new_serviceescalation.servicegroup_name=null;
        new_serviceescalation.hostgroup_name=null;
        new_serviceescalation.host_name=null;
        new_serviceescalation.service_description=null;
        new_serviceescalation.escalation_period=null;
        new_serviceescalation.contact_groups=null;
        new_serviceescalation.first_notification=-2;
        new_serviceescalation.last_notification=-2;
        new_serviceescalation.notification_interval=-2;
        new_serviceescalation.escalate_on_warning=common_h.FALSE;
        new_serviceescalation.escalate_on_unknown=common_h.FALSE;
        new_serviceescalation.escalate_on_critical=common_h.FALSE;
        new_serviceescalation.escalate_on_recovery=common_h.FALSE;
        new_serviceescalation.have_first_notification=common_h.FALSE;
        new_serviceescalation.have_last_notification=common_h.FALSE;
        new_serviceescalation.have_notification_interval=common_h.FALSE;
        new_serviceescalation.have_escalation_options=common_h.FALSE;
        new_serviceescalation.has_been_resolved=common_h.FALSE;
        new_serviceescalation.register_object=common_h.TRUE;
        new_serviceescalation._config_file=config_file;
        new_serviceescalation._start_line=start_line;
        
        /* add new serviceescalation to head of list in memory */
        xodtemplate_serviceescalation_list.add(new_serviceescalation);
        
        /* update current object pointer */
        xodtemplate_current_object = new_serviceescalation;
        break;
        
    case xodtemplate_h.XODTEMPLATE_CONTACT:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_contact new_contact= new xodtemplate_h.xodtemplate_contact();
        
        new_contact.template=null;
        new_contact.name=null;
        new_contact.contact_name=null;
        new_contact.alias=null;
        new_contact.contactgroups=null;
        new_contact.email=null;
        new_contact.pager=null;
        for(int x=0;x<xodtemplate_h.MAX_XODTEMPLATE_CONTACT_ADDRESSES;x++)
            new_contact.address[x]=null;
        new_contact.host_notification_period=null;
        new_contact.host_notification_commands=null;
        new_contact.service_notification_period=null;
        new_contact.service_notification_commands=null;
        new_contact.notify_on_host_down=common_h.FALSE;
        new_contact.notify_on_host_unreachable=common_h.FALSE;
        new_contact.notify_on_host_recovery=common_h.FALSE;
        new_contact.notify_on_host_flapping=common_h.FALSE;
        new_contact.notify_on_service_unknown=common_h.FALSE;
        new_contact.notify_on_service_warning=common_h.FALSE;
        new_contact.notify_on_service_critical=common_h.FALSE;
        new_contact.notify_on_service_recovery=common_h.FALSE;
        new_contact.notify_on_service_flapping=common_h.FALSE;
        new_contact.have_host_notification_options=common_h.FALSE;
        new_contact.have_service_notification_options=common_h.FALSE;
        new_contact.has_been_resolved=common_h.FALSE;
        new_contact.register_object=common_h.TRUE;
        new_contact._config_file=config_file;
        new_contact._start_line=start_line;
        
        /* add new contact to head of list in memory */
        xodtemplate_contact_list.add( new_contact );
        
        /* update current object pointer */
        xodtemplate_current_object=new_contact;
        break;
        
    case xodtemplate_h.XODTEMPLATE_HOST:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_host new_host= new xodtemplate_h.xodtemplate_host();
        
        
        new_host.template=null;
        new_host.name=null;
        new_host.host_name=null;
        new_host.alias=null;
        new_host.address=null;
        new_host.parents=null;
        new_host.hostgroups=null;
        new_host.check_command=null;
        new_host.check_period=null;
        new_host.event_handler=null;
        new_host.contact_groups=null;
        new_host.notification_period=null;
        
        new_host.check_interval=0;
        new_host.have_check_interval=common_h.FALSE;
        new_host.active_checks_enabled=common_h.TRUE;
        new_host.have_active_checks_enabled=common_h.FALSE;
        new_host.passive_checks_enabled=common_h.TRUE;
        new_host.have_passive_checks_enabled=common_h.FALSE;
        new_host.obsess_over_host=common_h.TRUE;
        new_host.have_obsess_over_host=common_h.FALSE;
        new_host.max_check_attempts=-2;
        new_host.have_max_check_attempts=common_h.FALSE;
        new_host.event_handler_enabled=common_h.TRUE;
        new_host.have_event_handler_enabled=common_h.FALSE;
        new_host.check_freshness=common_h.FALSE;
        new_host.have_check_freshness=common_h.FALSE;
        new_host.freshness_threshold=0;
        new_host.have_freshness_threshold=0;
        new_host.flap_detection_enabled=common_h.TRUE;
        new_host.have_flap_detection_enabled=common_h.FALSE;
        new_host.low_flap_threshold=0.0F;
        new_host.have_low_flap_threshold=common_h.FALSE;
        new_host.high_flap_threshold=0.0F;
        new_host.have_high_flap_threshold=common_h.FALSE;
        new_host.notify_on_down=common_h.FALSE;
        new_host.notify_on_unreachable=common_h.FALSE;
        new_host.notify_on_recovery=common_h.FALSE;
        new_host.notify_on_flapping=common_h.FALSE;
        new_host.have_notification_options=common_h.FALSE;
        new_host.notifications_enabled=common_h.TRUE;
        new_host.have_notifications_enabled=common_h.FALSE;
        new_host.notification_interval=-2;
        new_host.have_notification_interval=common_h.FALSE;
        new_host.stalk_on_up=common_h.FALSE;
        new_host.stalk_on_down=common_h.FALSE;
        new_host.stalk_on_unreachable=common_h.FALSE;
        new_host.have_stalking_options=common_h.FALSE;
        new_host.process_perf_data=common_h.TRUE;
        new_host.have_process_perf_data=common_h.FALSE;
        new_host.failure_prediction_enabled=common_h.TRUE;
        new_host.have_failure_prediction_enabled=common_h.FALSE;
        new_host.failure_prediction_options=null;
        new_host.retain_status_information=common_h.TRUE;
        new_host.have_retain_status_information=common_h.FALSE;
        new_host.retain_nonstatus_information=common_h.TRUE;
        new_host.have_retain_nonstatus_information=common_h.FALSE;
        new_host.has_been_resolved=common_h.FALSE;
        new_host.register_object=common_h.TRUE;
        new_host._config_file=config_file;
        new_host._start_line=start_line;
        
        /* add new host to head of list in memory */
        xodtemplate_host_list.add( new_host );
        
        /* update current object pointer */
        xodtemplate_current_object=new_host;
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICE:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_service new_service= new xodtemplate_h.xodtemplate_service();
        
        new_service.template=null;
        new_service.name=null;
        new_service.hostgroup_name=null;
        new_service.host_name=null;
        new_service.service_description=null;
        new_service.servicegroups=null;
        new_service.check_command=null;
        new_service.check_period=null;
        new_service.event_handler=null;
        new_service.notification_period=null;
        new_service.contact_groups=null;
        
        new_service.max_check_attempts=-2;
        new_service.have_max_check_attempts=common_h.FALSE;
        new_service.normal_check_interval=-2;
        new_service.have_normal_check_interval=common_h.FALSE;
        new_service.retry_check_interval=-2;
        new_service.have_retry_check_interval=common_h.FALSE;
        new_service.active_checks_enabled=common_h.TRUE;
        new_service.have_active_checks_enabled=common_h.FALSE;
        new_service.passive_checks_enabled=common_h.TRUE;
        new_service.have_passive_checks_enabled=common_h.FALSE;
        new_service.parallelize_check=common_h.TRUE;
        new_service.have_parallelize_check=common_h.FALSE;
        new_service.is_volatile=common_h.FALSE;
        new_service.have_is_volatile=common_h.FALSE;
        new_service.obsess_over_service=common_h.TRUE;
        new_service.have_obsess_over_service=common_h.FALSE;
        new_service.event_handler_enabled=common_h.TRUE;
        new_service.have_event_handler_enabled=common_h.FALSE;
        new_service.check_freshness=common_h.FALSE;
        new_service.have_check_freshness=common_h.FALSE;
        new_service.freshness_threshold=0;
        new_service.have_freshness_threshold=common_h.FALSE;
        new_service.flap_detection_enabled=common_h.TRUE;
        new_service.have_flap_detection_enabled=common_h.FALSE;
        new_service.low_flap_threshold=0.0;
        new_service.have_low_flap_threshold=common_h.FALSE;
        new_service.high_flap_threshold=0.0;
        new_service.have_high_flap_threshold=common_h.FALSE;
        new_service.notify_on_unknown=common_h.FALSE;
        new_service.notify_on_warning=common_h.FALSE;
        new_service.notify_on_critical=common_h.FALSE;
        new_service.notify_on_recovery=common_h.FALSE;
        new_service.notify_on_flapping=common_h.FALSE;
        new_service.have_notification_options=common_h.FALSE;
        new_service.notifications_enabled=common_h.TRUE;
        new_service.have_notifications_enabled=common_h.FALSE;
        new_service.notification_interval=-2;
        new_service.have_notification_interval=common_h.FALSE;
        new_service.stalk_on_ok=common_h.FALSE;
        new_service.stalk_on_unknown=common_h.FALSE;
        new_service.stalk_on_warning=common_h.FALSE;
        new_service.stalk_on_critical=common_h.FALSE;
        new_service.have_stalking_options=common_h.FALSE;
        new_service.process_perf_data=common_h.TRUE;
        new_service.have_process_perf_data=common_h.FALSE;
        new_service.failure_prediction_enabled=common_h.TRUE;
        new_service.have_failure_prediction_enabled=common_h.FALSE;
        new_service.failure_prediction_options=null;
        new_service.retain_status_information=common_h.TRUE;
        new_service.have_retain_status_information=common_h.FALSE;
        new_service.retain_nonstatus_information=common_h.TRUE;
        new_service.have_retain_nonstatus_information=common_h.FALSE;
        new_service.has_been_resolved=common_h.FALSE;
        new_service.register_object=common_h.TRUE;
        new_service._config_file=config_file;
        new_service._start_line=start_line;
        
        /* add new service to head of list in memory */
        xodtemplate_service_list.add(new_service);
        
        /* update current object pointer */
        xodtemplate_current_object=new_service;
        break;
        
    case xodtemplate_h.XODTEMPLATE_HOSTDEPENDENCY:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_hostdependency new_hostdependency= new xodtemplate_h.xodtemplate_hostdependency ();
        
        
        new_hostdependency.template=null;
        new_hostdependency.name=null;
        new_hostdependency.hostgroup_name=null;
        new_hostdependency.dependent_hostgroup_name=null;
        new_hostdependency.host_name=null;
        new_hostdependency.dependent_host_name=null;
        new_hostdependency.inherits_parent=common_h.FALSE;
        new_hostdependency.fail_notify_on_up=common_h.FALSE;
        new_hostdependency.fail_notify_on_down=common_h.FALSE;
        new_hostdependency.fail_notify_on_unreachable=common_h.FALSE;
        new_hostdependency.fail_notify_on_pending=common_h.FALSE;
        new_hostdependency.fail_execute_on_up=common_h.FALSE;
        new_hostdependency.fail_execute_on_down=common_h.FALSE;
        new_hostdependency.fail_execute_on_unreachable=common_h.FALSE;
        new_hostdependency.fail_execute_on_pending=common_h.FALSE;
        new_hostdependency.have_inherits_parent=common_h.FALSE;
        new_hostdependency.have_notification_dependency_options=common_h.FALSE;
        new_hostdependency.have_execution_dependency_options=common_h.FALSE;
        new_hostdependency.has_been_resolved=common_h.FALSE;
        new_hostdependency.register_object=common_h.TRUE;
        new_hostdependency._config_file=config_file;
        new_hostdependency._start_line=start_line;
        
        /* add new hostdependency to head of list in memory */
        xodtemplate_hostdependency_list.add(new_hostdependency);
        
        /* update current object pointer */
        xodtemplate_current_object=new_hostdependency;
        break;
        
    case xodtemplate_h.XODTEMPLATE_HOSTESCALATION:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_hostescalation new_hostescalation= new xodtemplate_h.xodtemplate_hostescalation ();
        
        new_hostescalation.template=null;
        new_hostescalation.name=null;
        new_hostescalation.hostgroup_name=null;
        new_hostescalation.host_name=null;
        new_hostescalation.escalation_period=null;
        new_hostescalation.contact_groups=null;
        new_hostescalation.first_notification=-2;
        new_hostescalation.last_notification=-2;
        new_hostescalation.notification_interval=-2;
        new_hostescalation.escalate_on_down=common_h.FALSE;
        new_hostescalation.escalate_on_unreachable=common_h.FALSE;
        new_hostescalation.escalate_on_recovery=common_h.FALSE;
        new_hostescalation.have_first_notification=common_h.FALSE;
        new_hostescalation.have_last_notification=common_h.FALSE;
        new_hostescalation.have_notification_interval=common_h.FALSE;
        new_hostescalation.have_escalation_options=common_h.FALSE;
        new_hostescalation.has_been_resolved=common_h.FALSE;
        new_hostescalation.register_object=common_h.TRUE;
        new_hostescalation._config_file=config_file;
        new_hostescalation._start_line=start_line;
        
        /* add new hostescalation to head of list in memory */
        xodtemplate_hostescalation_list.add(new_hostescalation);
        
        /* update current object pointer */
        xodtemplate_current_object=new_hostescalation;
        break;
        
    case xodtemplate_h.XODTEMPLATE_HOSTEXTINFO:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_hostextinfo new_hostextinfo= new xodtemplate_h.xodtemplate_hostextinfo ();
        
        new_hostextinfo.template=null;
        new_hostextinfo.name=null;
        new_hostextinfo.host_name=null;
        new_hostextinfo.hostgroup_name=null;
        new_hostextinfo.notes=null;
        new_hostextinfo.notes_url=null;
        new_hostextinfo.action_url=null;
        new_hostextinfo.icon_image=null;
        new_hostextinfo.icon_image_alt=null;
        new_hostextinfo.vrml_image=null;
        new_hostextinfo.statusmap_image=null;
        new_hostextinfo.x_2d=-1;
        new_hostextinfo.y_2d=-1;
        new_hostextinfo.x_3d=0.0;
        new_hostextinfo.y_3d=0.0;
        new_hostextinfo.z_3d=0.0;
        new_hostextinfo.have_2d_coords=common_h.FALSE;
        new_hostextinfo.have_3d_coords=common_h.FALSE;
        new_hostextinfo.has_been_resolved=common_h.FALSE;
        new_hostextinfo.register_object=common_h.TRUE;
        new_hostextinfo._config_file=config_file;
        new_hostextinfo._start_line=start_line;
        
        /* add new extended host info to head of list in memory */
        xodtemplate_hostextinfo_list.add(new_hostextinfo);
        
        /* update current object pointer */
        xodtemplate_current_object=new_hostextinfo;
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICEEXTINFO:
        
        /* allocate memory */
        xodtemplate_h.xodtemplate_serviceextinfo new_serviceextinfo= new xodtemplate_h.xodtemplate_serviceextinfo ();
        
        new_serviceextinfo.template=null;
        new_serviceextinfo.name=null;
        new_serviceextinfo.host_name=null;
        new_serviceextinfo.hostgroup_name=null;
        new_serviceextinfo.service_description=null;
        new_serviceextinfo.notes=null;
        new_serviceextinfo.notes_url=null;
        new_serviceextinfo.action_url=null;
        new_serviceextinfo.icon_image=null;
        new_serviceextinfo.icon_image_alt=null;
        new_serviceextinfo.has_been_resolved=common_h.FALSE;
        new_serviceextinfo.register_object=common_h.TRUE;
        new_serviceextinfo._config_file=config_file;
        new_serviceextinfo._start_line=start_line;
        
        /* add new extended service info to head of list in memory */
        xodtemplate_serviceextinfo_list.add(new_serviceextinfo);
        
        /* update current object pointer */
        xodtemplate_current_object=new_serviceextinfo;
        break;
        
    default:
        return common_h.ERROR;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_begin_object_definition");
    
    return result;
}



/**
 * Method for adding a property to the current object that we are creating. The type
 * of object that is being created is designated by xodtemplate_current_object which
 * is set within the xodtemplate_begin_object_definition method().
 * 
 * @param String input, the string from the config file.
 * @param options
 * 
 * @return, int, common_h.OK if everything is funky, otherwise common_h.ERROR;
 */

public static int xodtemplate_add_object_property(String input, int options)
{
    int result= common_h.OK;
    
    logger.trace( "entering " + cn + ".xodtemplate_add_object_property");
    
    /* check to see if we should process this type of object */
    switch(xodtemplate_current_object_type)
    {
    case xodtemplate_h.XODTEMPLATE_TIMEPERIOD:
        if((options & common_h.READ_TIMEPERIODS)==0)
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_COMMAND:
        if((options & common_h.READ_COMMANDS) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_CONTACT:
        if((options & common_h.READ_CONTACTS) == 0)
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_CONTACTGROUP:
        if((options & common_h.READ_CONTACTGROUPS) == 0)
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_HOST:
        if((options & common_h.READ_HOSTS) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_HOSTGROUP:
        if((options & common_h.READ_HOSTGROUPS) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_SERVICEGROUP:
        if((options & common_h.READ_SERVICEGROUPS) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_SERVICE:
        if((options & common_h.READ_SERVICES) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_SERVICEDEPENDENCY:
        if((options & common_h.READ_SERVICEDEPENDENCIES) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_SERVICEESCALATION:
        if((options & common_h.READ_SERVICEESCALATIONS) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_HOSTDEPENDENCY:
        if((options & common_h.READ_HOSTDEPENDENCIES) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_HOSTESCALATION:
        if((options & common_h.READ_HOSTESCALATIONS) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_HOSTEXTINFO:
        if((options & common_h.READ_HOSTEXTINFO) == 0 )
            return common_h.OK;
        break;
    
    case xodtemplate_h.XODTEMPLATE_SERVICEEXTINFO:
        if((options & common_h.READ_SERVICEEXTINFO) == 0 )
            return common_h.OK;
        break;
    
    default:
        return common_h.ERROR;
    }
    
    /* Retrieve the variable that we are to be setting */
    String[] inputs = input.split( "\\s", 2 );
    
    /* Verify that the variable/value pair is correct (as best we can tell */
    if (inputs == null || inputs.length != 2 || inputs[1].trim().length() == 0)
       logger.fatal( "Error: NULL variable value in object definition." );
    
    /* Set the variable name and the value */
    String variable = inputs[0].trim();
    String value = inputs[1].trim(); 
    
    logger.debug("STRIPPED VARIABLE: "+ variable);
    logger.debug("STRIPPED VALUE: "+ value);
    
    
    switch(xodtemplate_current_object_type)
    {
    
    case xodtemplate_h.XODTEMPLATE_TIMEPERIOD:
        
        /* Cast the xodtemplate_current_object to be that of the type we are setting attributes for */
    	xodtemplate_h.xodtemplate_timeperiod temp_timeperiod=(xodtemplate_h.xodtemplate_timeperiod)xodtemplate_current_object;
        
        if( variable.equals("use"))
        {
            temp_timeperiod.template=value;
            
        }
        else if(variable.equals("name"))
        {
            /* check for duplicates */
            if(xodtemplate_find_timeperiod(value)!= null)
                logger.warn("Warning: Duplicate definition found for timeperiod '"+value+"' (config file '"+xodtemplate_config_file_name(temp_timeperiod._config_file)+"', starting on line "+temp_timeperiod._start_line+")");
            
            temp_timeperiod.name=value;
            
        }
        else if(variable.equals("timeperiod_name"))
        {
            temp_timeperiod.timeperiod_name=value;
            
        }
        else if(variable.equals("alias"))
        {
            temp_timeperiod.alias=value;
            
        }
        else if(variable.equals("monday") || variable.equals("tuesday") || variable.equals("wednesday") || variable.equals("thursday") || variable.equals("friday") || variable.equals("saturday") || variable.equals("sunday"))
        {
            int x;
            if(variable.equals("monday"))
                x=1;
            else if(variable.equals("tuesday"))
                x=2;
            else if(variable.equals("wednesday"))
                x=3;
            else if(variable.equals("thursday"))
                x=4;
            else if(variable.equals("friday"))
                x=5;
            else if(variable.equals("saturday"))
                x=6;
            else
                x=0;
            temp_timeperiod.timeranges[x]=value;
        }
        else if(variable.equals("register"))
            temp_timeperiod.register_object=(atoi(value)>0)? common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal("Error: Invalid timeperiod object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        break;
        
        
        
    case xodtemplate_h.XODTEMPLATE_COMMAND:
        
        xodtemplate_h.xodtemplate_command temp_command = (xodtemplate_h.xodtemplate_command) xodtemplate_current_object;
        
        if(variable.equals("use"))
        {
            temp_command.template=value;
        }
        else if(variable.equals("name")){
            
            if ( blue.is_core )
                /* check for duplicates */
                if(xodtemplate_find_command(value)!=null)
                    logger.warn("Warning: Duplicate definition found for command '"+value+"' (config file '"+xodtemplate_config_file_name(temp_command._config_file)+"', starting on line "+temp_command._start_line+")");
            temp_command.name=value;
        }
        else if(variable.equals("command_name")){
            temp_command.command_name=value;
        }
        else if(variable.equals("command_line")){
            temp_command.command_line=value;
        }
        else if(variable.equals("register"))
            temp_command.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal("Error: Invalid command object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_CONTACTGROUP:
        
        xodtemplate_h.xodtemplate_contactgroup temp_contactgroup=(xodtemplate_h.xodtemplate_contactgroup) xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_contactgroup.template=value;
        } else if(variable.equals("name")){
            
            if (blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_contactgroup(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contactgroup '"+value+"' (config file '"+xodtemplate_config_file_name(temp_contactgroup._config_file)+"', starting on line "+temp_contactgroup._start_line+")");
            temp_contactgroup.name=value;
        } else if(variable.equals("contactgroup_name")){
            temp_contactgroup.contactgroup_name=value;
        } else if(variable.equals("alias")){
            temp_contactgroup.alias=value;
        } else if(variable.equals("members")){
            temp_contactgroup.members=value;
        } else if(variable.equals("register"))
            temp_contactgroup.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal("Error: Invalid contactgroup object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_HOSTGROUP:
        
        xodtemplate_h.xodtemplate_hostgroup temp_hostgroup=(xodtemplate_h.xodtemplate_hostgroup)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_hostgroup.template=value;
        } else if(variable.equals("name")){
            if (blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_hostgroup(value)!=null)
                    logger.warn("Warning: Duplicate definition found for hostgroup'"+value+"' (config file '"+xodtemplate_config_file_name(temp_hostgroup._config_file)+"', starting on line "+temp_hostgroup._start_line+")");
            
            temp_hostgroup.name=value;
        } else if(variable.equals("hostgroup_name")){
            temp_hostgroup.hostgroup_name=value;
        } else if(variable.equals("alias")){
            temp_hostgroup.alias=value;
        } else if(variable.equals("members")){
            if(temp_hostgroup.members==null)
                temp_hostgroup.members=value;
            else
                temp_hostgroup.members += "," + value;
        } else if(variable.equals("register"))
            temp_hostgroup.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal( "Error: Invalid hostgroup object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
     case xodtemplate_h.XODTEMPLATE_SERVICEGROUP:
    
        xodtemplate_h.xodtemplate_servicegroup temp_servicegroup=(xodtemplate_h.xodtemplate_servicegroup)xodtemplate_current_object;
        
        if(variable.equals("use"))
        {
            temp_servicegroup.template=value;
        }
        else if(variable.equals("name"))
        {
    
        	if(blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_servicegroup(value)!=null)
                    logger.warn("Warning: Duplicate definition found for servicegroup'"+value+"' (config file '"+xodtemplate_config_file_name(temp_servicegroup._config_file)+"', starting on line "+temp_servicegroup._start_line+")");
    
        	temp_servicegroup.name=value;
        }
        else if(variable.equals("servicegroup_name"))
            temp_servicegroup.servicegroup_name=value;
        
        else if(variable.equals("alias"))
            temp_servicegroup.alias=value;
        
        else if(variable.equals("members"))
        {
        
        	if(temp_servicegroup.members == null)
        		temp_servicegroup.members=value;
            else
                temp_servicegroup.members += "," + value;
        }
        else if(variable.equals("register"))
            temp_servicegroup.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else
        {
            logger.fatal( "Error: Invalid servicegroup object directive '"+variable+"'." );
            return common_h.ERROR;
        }
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICEDEPENDENCY:
        
        xodtemplate_h.xodtemplate_servicedependency temp_servicedependency = (xodtemplate_h.xodtemplate_servicedependency)xodtemplate_current_object;
        
        if(variable.equals("use"))
        {
            temp_servicedependency.template=value;
        }
        else if(variable.equals("name"))
        {
            if ( blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_servicedependency(value)!=null)
                    logger.warn("Warning: Duplicate definition found for service dependency '"+value+"' (config file '"+xodtemplate_config_file_name(temp_servicedependency._config_file)+"', starting on line "+temp_servicedependency._start_line+")");
            temp_servicedependency.name=value;
        }
        else if(variable.equals("servicegroup") || variable.equals("servicegroups") || variable.equals("servicegroup_name")){
            temp_servicedependency.servicegroup_name=value;
        }
        else if(variable.equals("hostgroup") || variable.equals("hostgroups") || variable.equals("hostgroup_name")){
            temp_servicedependency.hostgroup_name=value;
        }
        else if(variable.equals("host") || variable.equals("host_name") || variable.equals("master_host") || variable.equals("master_host_name")){
            temp_servicedependency.host_name=value;
        }
        else if(variable.equals("description") || variable.equals("service_description") || variable.equals("master_description") || variable.equals("master_service_description")){
            temp_servicedependency.service_description=value;
        }
        else if(variable.equals("dependent_servicegroup") || variable.equals("dependent_servicegroups") || variable.equals("dependent_servicegroup_name")){
            temp_servicedependency.dependent_servicegroup_name=value;
        }
        else if(variable.equals("dependent_hostgroup") || variable.equals("dependent_hostgroups") || variable.equals("dependent_hostgroup_name")){
            temp_servicedependency.dependent_hostgroup_name=value;
        }
        else if(variable.equals("dependent_host") || variable.equals("dependent_host_name")){
            temp_servicedependency.dependent_host_name=value;
        }
        else if(variable.equals("dependent_description") || variable.equals("dependent_service_description")){
            temp_servicedependency.dependent_service_description=value;
        }
        else if(variable.equals("inherits_parent"))
        {
            temp_servicedependency.inherits_parent=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_servicedependency.have_inherits_parent=common_h.TRUE;
        }
        else if(variable.equals("execution_failure_options") || variable.equals("execution_failure_criteria"))
        {
            String[] split = value.split( "[, ]" );
            if ( split != null )
            {
                for ( int i = 0; i<split.length; i ++ )
                {
                    String temp_ptr = split[i].trim();
                    if(temp_ptr.equals("o") || temp_ptr.equals("ok"))
                        temp_servicedependency.fail_execute_on_ok= common_h.TRUE;
                    else if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                        temp_servicedependency.fail_execute_on_unknown=common_h.TRUE;
                    else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                        temp_servicedependency.fail_execute_on_warning=common_h.TRUE;
                    else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                        temp_servicedependency.fail_execute_on_critical=common_h.TRUE;
                    else if(temp_ptr.equals("p") || temp_ptr.equals("pending"))
                        temp_servicedependency.fail_execute_on_pending=common_h.TRUE;
                    else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                        temp_servicedependency.fail_execute_on_ok=common_h.FALSE;
                        temp_servicedependency.fail_execute_on_unknown=common_h.FALSE;
                        temp_servicedependency.fail_execute_on_warning=common_h.FALSE;
                        temp_servicedependency.fail_execute_on_critical=common_h.FALSE;
                    }
                    else{
                        logger.fatal( "Error: Invalid execution dependency option '"+temp_ptr+"' in servicedependency definition.");
                        return common_h.ERROR;
                    }
                }
            }
            temp_servicedependency.have_execution_dependency_options=common_h.TRUE;
        }
        else if(variable.equals("notification_failure_options") || variable.equals("notification_failure_criteria")){
            String[] split = value.split( "[, ]" );
            if ( split != null )
            {
                for ( int i = 0; i<split.length; i ++ )
                {
                    String temp_ptr = split[i].trim();
                    if(temp_ptr.equals("o") || temp_ptr.equals("ok"))
                        temp_servicedependency.fail_notify_on_ok=common_h.TRUE;
                    else if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                        temp_servicedependency.fail_notify_on_unknown=common_h.TRUE;
                    else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                        temp_servicedependency.fail_notify_on_warning=common_h.TRUE;
                    else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                        temp_servicedependency.fail_notify_on_critical=common_h.TRUE;
                    else if(temp_ptr.equals("p") || temp_ptr.equals("pending"))
                        temp_servicedependency.fail_notify_on_pending=common_h.TRUE;
                    else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                        temp_servicedependency.fail_notify_on_ok=common_h.FALSE;
                        temp_servicedependency.fail_notify_on_unknown=common_h.FALSE;
                        temp_servicedependency.fail_notify_on_warning=common_h.FALSE;
                        temp_servicedependency.fail_notify_on_critical=common_h.FALSE;
                        temp_servicedependency.fail_notify_on_pending=common_h.FALSE;
                 }
            else
            {
                 logger.fatal( "Error: Invalid notification dependency option '"+temp_ptr+"' in servicedependency definition.");
                 return common_h.ERROR;
            }
            }
                temp_servicedependency.have_notification_dependency_options=common_h.TRUE;
            }    
        }
        else if(variable.equals("register"))
        {
            temp_servicedependency.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        }
        else
        {
            logger.fatal( "Error: Invalid servicedependency object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        break;
        
        
    case xodtemplate_h.XODTEMPLATE_SERVICEESCALATION:
        
        xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation=(xodtemplate_h.xodtemplate_serviceescalation)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_serviceescalation.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_serviceescalation(value)!= null )
                    logger.warn("Warning: Duplicate definition found for service escalation '"+value+"' (config file '"+xodtemplate_config_file_name(temp_serviceescalation._config_file)+"', starting on line "+temp_serviceescalation._start_line+")");
            
            temp_serviceescalation.name=value;
        }
        // TODO
        // Rob 12/01/07 - Not sure if Nagios supports host and service group definition in escalation objects?
        // In any event, they are NOT required attributes.
        else if(variable.equals("servicegroup") || variable.equals("servicegroups") || variable.equals("servicegroup_name")){
            temp_serviceescalation.servicegroup_name=value;
        }
        else if(variable.equals("hostgroup") || variable.equals("hostgroups") || variable.equals("hostgroup_name")){
            temp_serviceescalation.hostgroup_name=value;
        }
        else if(variable.equals("host") || variable.equals("host_name")){
            temp_serviceescalation.host_name=value;
        } else if(variable.equals("description") || variable.equals("service_description")){
            temp_serviceescalation.service_description=value;
        } else if(variable.equals("contact_groups")){
            temp_serviceescalation.contact_groups=value;
        } else if(variable.equals("first_notification")){
            temp_serviceescalation.first_notification=atoi(value);
            temp_serviceescalation.have_first_notification=common_h.TRUE;
        } else if(variable.equals("last_notification")){
            temp_serviceescalation.last_notification=atoi(value);
            temp_serviceescalation.have_last_notification=common_h.TRUE;
        } else if(variable.equals("notification_interval")){
            temp_serviceescalation.notification_interval=atoi(value);
            temp_serviceescalation.have_notification_interval=common_h.TRUE;
        } else if(variable.equals("escalation_period")){
            temp_serviceescalation.escalation_period=value;
        } else if(variable.equals("escalation_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                    temp_serviceescalation.escalate_on_warning=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                    temp_serviceescalation.escalate_on_unknown=common_h.TRUE;
                else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                    temp_serviceescalation.escalate_on_critical=common_h.TRUE;
                else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                    temp_serviceescalation.escalate_on_recovery=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_serviceescalation.escalate_on_warning=common_h.FALSE;
                    temp_serviceescalation.escalate_on_unknown=common_h.FALSE;
                    temp_serviceescalation.escalate_on_critical=common_h.FALSE;
                    temp_serviceescalation.escalate_on_recovery=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid escalation option '"+temp_ptr+"' in serviceescalation definition. ");
                    return common_h.ERROR;
                }
            }
            temp_serviceescalation.have_escalation_options=common_h.TRUE;
        } else if(variable.equals("register"))
            temp_serviceescalation.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal( "Error: Invalid serviceescalation object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
        
    case xodtemplate_h.XODTEMPLATE_CONTACT:
        
        xodtemplate_h.xodtemplate_contact temp_contact=(xodtemplate_h.xodtemplate_contact)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_contact.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_contact(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_contact._config_file)+"', starting on line "+temp_contact._start_line+")");
            temp_contact.name=value;
        } else if(variable.equals("contact_name")){
            temp_contact.contact_name=value;
        } else if(variable.equals("alias")){
            temp_contact.alias=value;
        } else if(variable.equals("contactgroups")){
            temp_contact.contactgroups=value;
        } else if(variable.equals("email")){
            temp_contact.email=value;
        } else if(variable.equals("pager")){
            temp_contact.pager=value;
        } else if(variable.startsWith("address") ){
            int x=atoi(variable.substring(7)); // original was variable + 7 
            if(x<1 || x>xodtemplate_h.MAX_XODTEMPLATE_CONTACT_ADDRESSES){
                logger.fatal("Error: Invalid contact address id '"+x+"'.");
                return common_h.ERROR;
            }
            temp_contact.address[x-1]=value;
        } else if(variable.equals("host_notification_period")){
            temp_contact.host_notification_period=value;
        } else if(variable.equals("host_notification_commands")){
            temp_contact.host_notification_commands=value;
        } else if(variable.equals("service_notification_period")){
            temp_contact.service_notification_period=value;
        } else if(variable.equals("service_notification_commands")){
            temp_contact.service_notification_commands=value;
        } else if(variable.equals("host_notification_period")){
            temp_contact.host_notification_period=value;
        } else if(variable.equals("service_notification_period")){
            temp_contact.service_notification_period=value;
        } else if(variable.equals("host_notification_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("d") || temp_ptr.equals("down"))
                    temp_contact.notify_on_host_down=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
                    temp_contact.notify_on_host_unreachable=common_h.TRUE;
                else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                    temp_contact.notify_on_host_recovery=common_h.TRUE;
                else if(temp_ptr.equals("f") || temp_ptr.equals("flapping"))
                    temp_contact.notify_on_host_flapping=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_contact.notify_on_host_down=common_h.FALSE;
                    temp_contact.notify_on_host_unreachable=common_h.FALSE;
                    temp_contact.notify_on_host_recovery=common_h.FALSE;
                    temp_contact.notify_on_host_flapping=common_h.FALSE;
                }
                else{
                    logger.fatal("Error: Invalid host notification option '"+temp_ptr+"' in contact definition.");
                    return common_h.ERROR;
                }
            }
            temp_contact.have_host_notification_options=common_h.TRUE;
        }
        else if(variable.equals("service_notification_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                    temp_contact.notify_on_service_unknown=common_h.TRUE;
                else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                    temp_contact.notify_on_service_warning=common_h.TRUE;
                else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                    temp_contact.notify_on_service_critical=common_h.TRUE;
                else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                    temp_contact.notify_on_service_recovery=common_h.TRUE;
                else if(temp_ptr.equals("f") || temp_ptr.equals("flapping"))
                    temp_contact.notify_on_service_flapping=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_contact.notify_on_service_unknown=common_h.FALSE;
                    temp_contact.notify_on_service_warning=common_h.FALSE;
                    temp_contact.notify_on_service_critical=common_h.FALSE;
                    temp_contact.notify_on_service_recovery=common_h.FALSE;
                    temp_contact.notify_on_service_flapping=common_h.FALSE;
                }
                else{
                    logger.fatal("Error: Invalid service notification option '"+temp_ptr+"' in contact definition.");
                    return common_h.ERROR;
                }
            }
            temp_contact.have_service_notification_options=common_h.TRUE;
        }
        else if(variable.equals("register"))
            temp_contact.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal("Error: Invalid contact object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
        
    case xodtemplate_h.XODTEMPLATE_HOST:
        
        xodtemplate_h.xodtemplate_host temp_host=(xodtemplate_h.xodtemplate_host)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_host.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core) 
                /* check for duplicates */
                if(xodtemplate_find_host(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_host._config_file)+"', starting on line "+temp_host._start_line+")");
            temp_host.name=value;
        } else if(variable.equals("host_name")){
            temp_host.host_name=value;
        } else if(variable.equals("alias")){
            temp_host.alias=value;
        } else if(variable.equals("address")){
            temp_host.address=value;
        } else if(variable.equals("parents")){
            temp_host.parents=value;
        } else if(variable.equals("hostgroups")){
            temp_host.hostgroups=value;
        } else if(variable.equals("contact_groups")){
            temp_host.contact_groups=value;
        } else if(variable.equals("notification_period")){
            temp_host.notification_period=value;
        } else if(variable.equals("check_command")){
            temp_host.check_command=value;
        } else if(variable.equals("check_period")){
            temp_host.check_period=value;
        } else if(variable.equals("event_handler")){
            temp_host.event_handler=value;
        } else if(variable.equals("failure_prediction_options")){
            temp_host.failure_prediction_options=value;
        } else if(variable.equals("check_interval") || variable.equals("normal_check_interval")){
            temp_host.check_interval=atoi(value);
            temp_host.have_check_interval=common_h.TRUE;
        } else if(variable.equals("max_check_attempts")){
            temp_host.max_check_attempts=atoi(value);
            temp_host.have_max_check_attempts=common_h.TRUE;
        } else if(variable.equals("checks_enabled") || variable.equals("active_checks_enabled")){
            temp_host.active_checks_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_active_checks_enabled=common_h.TRUE;
        } else if(variable.equals("passive_checks_enabled")){
            temp_host.passive_checks_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_passive_checks_enabled=common_h.TRUE;
        } else if(variable.equals("event_handler_enabled")){
            temp_host.event_handler_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_event_handler_enabled=common_h.TRUE;
        } else if(variable.equals("check_freshness")){
            temp_host.check_freshness=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_check_freshness=common_h.TRUE;
        } else if(variable.equals("freshness_threshold")){
            temp_host.freshness_threshold=atoi(value);
            temp_host.have_freshness_threshold=common_h.TRUE;
        } else if(variable.equals("low_flap_threshold")){
            temp_host.low_flap_threshold=Float.parseFloat(value);
            temp_host.have_low_flap_threshold=common_h.TRUE;
        } else if(variable.equals("high_flap_threshold")){
            temp_host.high_flap_threshold=Float.parseFloat(value);
            temp_host.have_high_flap_threshold=common_h.TRUE;
        } else if(variable.equals("flap_detection_enabled")){
            temp_host.flap_detection_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_flap_detection_enabled=common_h.TRUE;
        } else if(variable.equals("notification_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("d") || temp_ptr.equals("down"))
                    temp_host.notify_on_down=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
                    temp_host.notify_on_unreachable=common_h.TRUE;
                else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                    temp_host.notify_on_recovery=common_h.TRUE;
                else if(temp_ptr.equals("f") || temp_ptr.equals("flapping"))
                    temp_host.notify_on_flapping=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_host.notify_on_down=common_h.FALSE;
                    temp_host.notify_on_unreachable=common_h.FALSE;
                    temp_host.notify_on_recovery=common_h.FALSE;
                    temp_host.notify_on_flapping=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid notification option '"+temp_ptr+"' in host definition.");
                    return common_h.ERROR;
                }
            }
            temp_host.have_notification_options=common_h.TRUE;
        } else if(variable.equals("notifications_enabled")){
            temp_host.notifications_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_notifications_enabled=common_h.TRUE;
        } else if(variable.equals("notification_interval")){
            temp_host.notification_interval=atoi(value);
            temp_host.have_notification_interval=common_h.TRUE;
        } else if(variable.equals("stalking_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("o") || temp_ptr.equals("up"))
                    temp_host.stalk_on_up=common_h.TRUE;
                else if(temp_ptr.equals("d") || temp_ptr.equals("down"))
                    temp_host.stalk_on_down=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
                    temp_host.stalk_on_unreachable=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_host.stalk_on_up=common_h.FALSE;
                    temp_host.stalk_on_down=common_h.FALSE;
                    temp_host.stalk_on_unreachable=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid stalking option '"+temp_ptr+"' in host definition.");
                    return common_h.ERROR;
                }
            }
            temp_host.have_stalking_options=common_h.TRUE;
        } else if(variable.equals("process_perf_data")){
            temp_host.process_perf_data=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_process_perf_data=common_h.TRUE;
        } else if(variable.equals("failure_prediction_enabled")){
            temp_host.failure_prediction_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_failure_prediction_enabled=common_h.TRUE;
        } else if(variable.equals("obsess_over_host")){
            temp_host.obsess_over_host=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_obsess_over_host=common_h.TRUE;
        } else if(variable.equals("retain_status_information")){
            temp_host.retain_status_information=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_retain_status_information=common_h.TRUE;
        } else if(variable.equals("retain_nonstatus_information")){
            temp_host.retain_nonstatus_information=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_host.have_retain_nonstatus_information=common_h.TRUE;
        } else if(variable.equals("register"))
            temp_host.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal( "Error: Invalid host object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICE:
        
        xodtemplate_h.xodtemplate_service temp_service=(xodtemplate_h.xodtemplate_service)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_service.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_service(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_service._config_file)+"', starting on line "+temp_service._start_line+")");
            temp_service.name=value;
        } else if(variable.equals("hostgroup") || variable.equals("hostgroups") || variable.equals("hostgroup_name")){
            temp_service.hostgroup_name=value;
        } else if(variable.equals("host") || variable.equals("hosts") || variable.equals("host_name")){
            temp_service.host_name=value;
        } else if(variable.equals("service_description") || variable.equals("description")){
            temp_service.service_description=value;
        } else if(variable.equals("servicegroups")){
            temp_service.servicegroups=value;
        } else if(variable.equals("check_command")){
            temp_service.check_command=value;
        } else if(variable.equals("check_period")){
            temp_service.check_period=value;
        } else if(variable.equals("event_handler")){
            temp_service.event_handler=value;
        } else if(variable.equals("notification_period")){
            temp_service.notification_period=value;
        } else if(variable.equals("contact_groups")){
            temp_service.contact_groups=value;
        } else if(variable.equals("failure_prediction_options")){
            temp_service.failure_prediction_options=value;
        } else if(variable.equals("max_check_attempts")){
            temp_service.max_check_attempts=atoi(value);
            temp_service.have_max_check_attempts=common_h.TRUE;
        } else if(variable.equals("normal_check_interval")){
            temp_service.normal_check_interval=atoi(value);
            temp_service.have_normal_check_interval=common_h.TRUE;
        } else if(variable.equals("retry_check_interval")){
            temp_service.retry_check_interval=atoi(value);
            temp_service.have_retry_check_interval=common_h.TRUE;
        } else if(variable.equals("active_checks_enabled")){
            temp_service.active_checks_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_active_checks_enabled=common_h.TRUE;
        } else if(variable.equals("passive_checks_enabled")){
            temp_service.passive_checks_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_passive_checks_enabled=common_h.TRUE;
        } else if(variable.equals("parallelize_check")){
            temp_service.parallelize_check=atoi(value);
            temp_service.have_parallelize_check=common_h.TRUE;
        } else if(variable.equals("is_volatile")){
            temp_service.is_volatile=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_is_volatile=common_h.TRUE;
        } else if(variable.equals("obsess_over_service")){
            temp_service.obsess_over_service=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_obsess_over_service=common_h.TRUE;
        } else if(variable.equals("event_handler_enabled")){
            temp_service.event_handler_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_event_handler_enabled=common_h.TRUE;
        } else if(variable.equals("check_freshness")){
            temp_service.check_freshness=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_check_freshness=common_h.TRUE;
        } else if(variable.equals("freshness_threshold")){
            temp_service.freshness_threshold=atoi(value);
            temp_service.have_freshness_threshold=common_h.TRUE;
        } else if(variable.equals("low_flap_threshold")){
            temp_service.low_flap_threshold=Float.parseFloat( value );
            temp_service.have_low_flap_threshold=common_h.TRUE;
        } else if(variable.equals("high_flap_threshold")){
            temp_service.high_flap_threshold=Float.parseFloat( value );
            temp_service.have_high_flap_threshold=common_h.TRUE;
        } else if(variable.equals("flap_detection_enabled")){
            temp_service.flap_detection_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_flap_detection_enabled=common_h.TRUE;
        } else if(variable.equals("notification_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                    temp_service.notify_on_unknown=common_h.TRUE;
                else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                    temp_service.notify_on_warning=common_h.TRUE;
                else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                    temp_service.notify_on_critical=common_h.TRUE;
                else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                    temp_service.notify_on_recovery=common_h.TRUE;
                else if(temp_ptr.equals("f") || temp_ptr.equals("flapping"))
                    temp_service.notify_on_flapping=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_service.notify_on_unknown=common_h.FALSE;
                    temp_service.notify_on_warning=common_h.FALSE;
                    temp_service.notify_on_critical=common_h.FALSE;
                    temp_service.notify_on_recovery=common_h.FALSE;
                    temp_service.notify_on_flapping=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid notification option '"+temp_ptr+"' in service definition.");
                    return common_h.ERROR;
                }
            }
            temp_service.have_notification_options=common_h.TRUE;
        } else if(variable.equals("notifications_enabled")){
            temp_service.notifications_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_notifications_enabled=common_h.TRUE;
        } else if(variable.equals("notification_interval")){
            temp_service.notification_interval=atoi(value);
            temp_service.have_notification_interval=common_h.TRUE;
        } else if(variable.equals("stalking_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("o") || temp_ptr.equals("ok"))
                    temp_service.stalk_on_ok=common_h.TRUE;
                else if(temp_ptr.equals("w") || temp_ptr.equals("warning"))
                    temp_service.stalk_on_warning=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unknown"))
                    temp_service.stalk_on_unknown=common_h.TRUE;
                else if(temp_ptr.equals("c") || temp_ptr.equals("critical"))
                    temp_service.stalk_on_critical=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_service.stalk_on_ok=common_h.FALSE;
                    temp_service.stalk_on_warning=common_h.FALSE;
                    temp_service.stalk_on_unknown=common_h.FALSE;
                    temp_service.stalk_on_critical=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid stalking option '"+temp_ptr+"' in service definition.");
                    return common_h.ERROR;
                }
            }
            temp_service.have_stalking_options=common_h.TRUE;
        } else if(variable.equals("process_perf_data")){
            temp_service.process_perf_data=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_process_perf_data=common_h.TRUE;
        } else if(variable.equals("failure_prediction_enabled")){
            temp_service.failure_prediction_enabled=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_failure_prediction_enabled=common_h.TRUE;
        } else if(variable.equals("retain_status_information")){
            temp_service.retain_status_information=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_retain_status_information=common_h.TRUE;
        } else if(variable.equals("retain_nonstatus_information")){
            temp_service.retain_nonstatus_information=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_service.have_retain_nonstatus_information=common_h.TRUE;
        } else if(variable.equals("register"))
            temp_service.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal("Error: Invalid service object directive '"+variable+"'.");
            return common_h.ERROR;
        }
       break;
        
    case xodtemplate_h.XODTEMPLATE_HOSTDEPENDENCY:
        
        xodtemplate_h.xodtemplate_hostdependency temp_hostdependency=(xodtemplate_h.xodtemplate_hostdependency)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_hostdependency.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_hostdependency(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_hostdependency._config_file)+"', starting on line "+temp_hostdependency._start_line+")");
            temp_hostdependency.name=value;
        } else if(variable.equals("hostgroup") || variable.equals("hostgroups") || variable.equals("hostgroup_name")){
            temp_hostdependency.hostgroup_name=value;
        } else if(variable.equals("host") || variable.equals("host_name") || variable.equals("master_host") || variable.equals("master_host_name")){
            temp_hostdependency.host_name=value;
        } else if(variable.equals("dependent_hostgroup") || variable.equals("dependent_hostgroups") || variable.equals("dependent_hostgroup_name")){
            temp_hostdependency.dependent_hostgroup_name=value;
        } else if(variable.equals("dependent_host") || variable.equals("dependent_host_name")){
            temp_hostdependency.dependent_host_name=value;
        } else if(variable.equals("inherits_parent")){
            temp_hostdependency.inherits_parent=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
            temp_hostdependency.have_inherits_parent=common_h.TRUE;
        } else if(variable.equals("notification_failure_options") || variable.equals("notification_failure_criteria")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("o") || temp_ptr.equals("up"))
                    temp_hostdependency.fail_notify_on_up=common_h.TRUE;
                else if(temp_ptr.equals("d") || temp_ptr.equals("down"))
                    temp_hostdependency.fail_notify_on_down=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
                    temp_hostdependency.fail_notify_on_unreachable=common_h.TRUE;
                else if(temp_ptr.equals("p") || temp_ptr.equals("pending"))
                    temp_hostdependency.fail_notify_on_pending=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_hostdependency.fail_notify_on_up=common_h.FALSE;
                    temp_hostdependency.fail_notify_on_down=common_h.FALSE;
                    temp_hostdependency.fail_notify_on_unreachable=common_h.FALSE;
                    temp_hostdependency.fail_notify_on_pending=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid notification dependency option '"+temp_ptr+"' in hostdependency definition.");
                    return common_h.ERROR;
                }
            }
            temp_hostdependency.have_notification_dependency_options=common_h.TRUE;
        } else if(variable.equals("execution_failure_options") || variable.equals("execution_failure_criteria")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("o") || temp_ptr.equals("up"))
                    temp_hostdependency.fail_execute_on_up=common_h.TRUE;
                else if(temp_ptr.equals("d") || temp_ptr.equals("down"))
                    temp_hostdependency.fail_execute_on_down=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
                    temp_hostdependency.fail_execute_on_unreachable=common_h.TRUE;
                else if(temp_ptr.equals("p") || temp_ptr.equals("pending"))
                    temp_hostdependency.fail_execute_on_pending=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_hostdependency.fail_execute_on_up=common_h.FALSE;
                    temp_hostdependency.fail_execute_on_down=common_h.FALSE;
                    temp_hostdependency.fail_execute_on_unreachable=common_h.FALSE;
                    temp_hostdependency.fail_execute_on_pending=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid execution dependency option '"+temp_ptr+"' in hostdependency definition.");
                    return common_h.ERROR;
                }
            }
            temp_hostdependency.have_execution_dependency_options=common_h.TRUE;
        } else if(variable.equals("register"))
            temp_hostdependency.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal("Error: Invalid hostdependency object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
        
    case xodtemplate_h.XODTEMPLATE_HOSTESCALATION:
        
        xodtemplate_h.xodtemplate_hostescalation temp_hostescalation=(xodtemplate_h.xodtemplate_hostescalation)xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_hostescalation.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core )
                /* check for duplicates */
                if(xodtemplate_find_hostescalation(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_hostescalation._config_file)+"', starting on line "+temp_hostescalation._start_line+")");
            temp_hostescalation.name=value;
        } else if(variable.equals("hostgroup") || variable.equals("hostgroups") || variable.equals("hostgroup_name")){
            temp_hostescalation.hostgroup_name=value;
        } else if(variable.equals("host") || variable.equals("host_name")){
            temp_hostescalation.host_name=value;
        } else if(variable.equals("contact_groups")){
            temp_hostescalation.contact_groups=value;
        } else if(variable.equals("first_notification")){
            temp_hostescalation.first_notification=atoi(value);
            temp_hostescalation.have_first_notification=common_h.TRUE;
        } else if(variable.equals("last_notification")){
            temp_hostescalation.last_notification=atoi(value);
            temp_hostescalation.have_last_notification=common_h.TRUE;
        } else if(variable.equals("notification_interval")){
            temp_hostescalation.notification_interval=atoi(value);
            temp_hostescalation.have_notification_interval=common_h.TRUE;
        } else if(variable.equals("escalation_period")){
            temp_hostescalation.escalation_period=value;
        } else if(variable.equals("escalation_options")){
            String[] split = value.split( "[, ]" );
            for ( int i = 0; i<split.length; i ++ ) {
                String temp_ptr = split[i].trim();
                if(temp_ptr.equals("d") || temp_ptr.equals("down"))
                    temp_hostescalation.escalate_on_down=common_h.TRUE;
                else if(temp_ptr.equals("u") || temp_ptr.equals("unreachable"))
                    temp_hostescalation.escalate_on_unreachable=common_h.TRUE;
                else if(temp_ptr.equals("r") || temp_ptr.equals("recovery"))
                    temp_hostescalation.escalate_on_recovery=common_h.TRUE;
                else if(temp_ptr.equals("n") || temp_ptr.equals("none")){
                    temp_hostescalation.escalate_on_down=common_h.FALSE;
                    temp_hostescalation.escalate_on_unreachable=common_h.FALSE;
                    temp_hostescalation.escalate_on_recovery=common_h.FALSE;
                }
                else{
                    logger.fatal( "Error: Invalid escalation option '"+temp_ptr+"' in hostescalation definition.");
                    return common_h.ERROR;
                }
            }
            temp_hostescalation.have_escalation_options=common_h.TRUE;
        } else if(variable.equals("register"))
            temp_hostescalation.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal( "Error: Invalid hostescalation object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_HOSTEXTINFO:
        
        xodtemplate_h.xodtemplate_hostextinfo temp_hostextinfo=(xodtemplate_h.xodtemplate_hostextinfo) xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_hostextinfo.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core )
                /* check for duplicates */
                if(xodtemplate_find_hostextinfo(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_hostextinfo._config_file)+"', starting on line "+temp_hostextinfo._start_line+")");
            temp_hostextinfo.name=value;
        } else if(variable.equals("host_name")){
            temp_hostextinfo.host_name=value;
        } else if(variable.equals("hostgroup") || variable.equals("hostgroup_name")){
            temp_hostextinfo.hostgroup_name=value;
        } else if(variable.equals("notes")){
            temp_hostextinfo.notes=value;
        } else if(variable.equals("notes_url")){
            temp_hostextinfo.notes_url=value;
        } else if(variable.equals("action_url")){
            temp_hostextinfo.action_url=value;
        } else if(variable.equals("icon_image")){
            temp_hostextinfo.icon_image=value;
        } else if(variable.equals("icon_image_alt")){
            temp_hostextinfo.icon_image_alt=value;
        } else if(variable.equals("vrml_image")){
            temp_hostextinfo.vrml_image=value;
        } else if(variable.equals("gd2_image")|| variable.equals("statusmap_image")){
            temp_hostextinfo.statusmap_image=value;
        } else if(variable.equals("2d_coords"))
        {
            String[] split = value.split( "[, ]" );
            
            if ( split.length != 2 )
                logger.fatal( "Error: Invalid 2d_coords value '"+value+"' in extended host info definition.");
            
            // TODO - Rob 12/01/07 - Typically coords are going to be x.x,y.y A Float is probably more
            // suitable here. However we seem to be tied to using int with regards to Graphics2d. For now
            // a simple check to see if we have a decimal value..
            
            if(split[0].contains("."))
               	temp_hostextinfo.x_2d = (int)Double.parseDouble(split[0]);
            else
            	temp_hostextinfo.x_2d = atoi(split[0]);
            
            if(split[1].contains("."))
               	temp_hostextinfo.y_2d = (int)Double.parseDouble(split[1]);
            else
                temp_hostextinfo.y_2d = atoi(split[1]);
            
            temp_hostextinfo.have_2d_coords=common_h.TRUE;
        }
        else if(variable.equals("3d_coords"))
        {
            String[] split = value.split( "[, ]" );
            if ( split.length != 3 )
                logger.fatal( "Error: Invalid 3d_coords value '"+value+"' in extended host info definition.");
            
            temp_hostextinfo.x_3d=Float.parseFloat( split[0] );
            temp_hostextinfo.y_3d=Float.parseFloat( split[1] );
            temp_hostextinfo.z_3d=Float.parseFloat( split[2] );
            temp_hostextinfo.have_3d_coords=common_h.TRUE;
        }
        else if(variable.equals("register"))
            temp_hostextinfo.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal( "Error: Invalid hostextinfo object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
    case xodtemplate_h.XODTEMPLATE_SERVICEEXTINFO:
        xodtemplate_h.xodtemplate_serviceextinfo temp_serviceextinfo=(xodtemplate_h.xodtemplate_serviceextinfo) xodtemplate_current_object;
        
        if(variable.equals("use")){
            temp_serviceextinfo.template=value;
        } else if(variable.equals("name")){
            if ( blue.is_core)
                /* check for duplicates */
                if(xodtemplate_find_serviceextinfo(value)!=null)
                    logger.warn("Warning: Duplicate definition found for contact'"+value+"' (config file '"+xodtemplate_config_file_name(temp_serviceextinfo._config_file)+"', starting on line "+temp_serviceextinfo._start_line+")");
            temp_serviceextinfo.name=value;
        } else if(variable.equals("host_name")){
            temp_serviceextinfo.host_name=value;
        } else if(variable.equals("hostgroup") || variable.equals("hostgroup_name")){
            temp_serviceextinfo.hostgroup_name=value;
        } else if(variable.equals("service_description")){
            temp_serviceextinfo.service_description=value;
        } else if(variable.equals("notes")){
            temp_serviceextinfo.notes=value;
        } else if(variable.equals("notes_url")){
            temp_serviceextinfo.notes_url=value;
        } else if(variable.equals("action_url")){
            temp_serviceextinfo.action_url=value;
        } else if(variable.equals("icon_image")){
            temp_serviceextinfo.icon_image=value;
        } else if(variable.equals("icon_image_alt")){
            temp_serviceextinfo.icon_image_alt=value;
        } else if(variable.equals("register"))
            temp_serviceextinfo.register_object=(atoi(value)>0)?common_h.TRUE:common_h.FALSE;
        else{
            logger.fatal( "Error: Invalid serviceextinfo object directive '"+variable+"'.");
            return common_h.ERROR;
        }
        
        break;
        
    default:
        return common_h.ERROR;
    }
    
    
    logger.trace( "exiting " + cn + ".xodtemplate_add_object_property");
    return result;
}



/**
 * Method that simply ends the definition on an object. Resets xodtemplate_current_object to null,
 * and returns xodtemplate_current_object_type to xodtemplate_h.XODTEMPLATE_NONE;
 * 
 * @param options
 * @return common_h.OK if variables have been cleanly reset, common_h.ERROR otherwise.
 */

// Changed by Rob 28/09/06
// Changed: return to simply common_h.OK, redundant variable instantiated and then returned.
// Changed: Added some error checking to verify that things have actually been reset!

// TODO - investigate whether options parameter is actually needed!

public static int xodtemplate_end_object_definition(int options)
{
    logger.trace("entering " + cn + ".xodtemplate_end_object_definition");
    
    /* Reset current object and current object type */
    xodtemplate_current_object = null;
    xodtemplate_current_object_type = xodtemplate_h.XODTEMPLATE_NONE;
    
    /* Verify that reset has been successful */
    
    if(xodtemplate_current_object != null && xodtemplate_current_object_type != xodtemplate_h.XODTEMPLATE_NONE)
    {
    	return common_h.ERROR;
    }
   
    logger.trace("exiting " + cn + ".xodtemplate_end_object_definition");
    
    return common_h.OK;
}


/******************************************************************/
/***************** OBJECT DUPLICATION FUNCTIONS *******************/
/******************************************************************/

/* duplicates service definitions */
public static int xodtemplate_duplicate_services()
{
    
    logger.trace( "entering " + cn + ".xodtemplate_duplicate_services");
    
    /****** DUPLICATE SERVICE DEFINITIONS WITH ONE OR MORE HOSTGROUP AND/OR HOST NAMES ******/
    for ( ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext(); ) {
        xodtemplate_h.xodtemplate_service temp_service = (xodtemplate_h.xodtemplate_service) iter.next();

        /* skip service definitions without enough data */
        if( temp_service.hostgroup_name==null && temp_service.host_name==null)
            continue;

        /* skip services that shouldn't be registered */
        if(temp_service.register_object==common_h.FALSE)
            continue;
        
        /* get list of hosts */
        ArrayList temp_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_service.hostgroup_name,temp_service.host_name);
        if( (temp_hostlist==null) || ( temp_hostlist.size() == 0 ) ){
            logger.fatal("Error: Could not expand hostgroups and/or hosts specified in service (config file '"+xodtemplate_config_file_name(temp_service._config_file)+"', starting on line "+temp_service._start_line+")");
            return common_h.ERROR;
        }
        
        /* add a copy of the service for every host in the hostgroup/host name list */
        temp_service.host_name = ((xodtemplate_h.xodtemplate_hostlist) temp_hostlist.get(0)).host_name;
        for ( int i=1; i<temp_hostlist.size(); i++ ) {
            /* duplicate service definition */
           xodtemplate_duplicate_service(iter , temp_service,((xodtemplate_h.xodtemplate_hostlist) temp_hostlist.get(i)).host_name);
        }
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_duplicate_services");
    return common_h.OK;
}

/* duplicates object definitions */
public static int xodtemplate_duplicate_objects()
{

	logger.trace( "entering " + cn + ".xodtemplate_duplicate_objects");

	/*************************************/
	/* SERVICES ARE DUPLICATED ELSEWHERE */
	/*************************************/

	/****** DUPLICATE HOST ESCALATION DEFINITIONS WITH ONE OR MORE HOSTGROUP AND/OR HOST NAMES ******/
	for(ListIterator iter = xodtemplate_hostescalation_list.listIterator(); iter.hasNext();)
	{
        xodtemplate_h.xodtemplate_hostescalation temp_hostescalation = (xodtemplate_h.xodtemplate_hostescalation)iter.next();
	    
	    /* get list of hosts */
	    ArrayList temp_hostlist = xodtemplate_expand_hostgroups_and_hosts(temp_hostescalation.hostgroup_name,temp_hostescalation.host_name);
	    
	    if((temp_hostlist == null) || ( temp_hostlist.size() == 0 ))
	    {
	        logger.fatal( "Error: Could not expand hostgroups and/or hosts specified in host escalation (config file '"+xodtemplate_config_file_name(temp_hostescalation._config_file)+"', starting on line "+temp_hostescalation._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* add a copy of the hostescalation for every host in the hostgroup/host name list */
	    temp_hostescalation.host_name = ((xodtemplate_h.xodtemplate_hostlist)temp_hostlist .get(0)).host_name;
	    
	    for(int i = 1;i < temp_hostlist.size(); i++)
	    {
	        if (xodtemplate_duplicate_hostescalation(iter,temp_hostescalation,((xodtemplate_h.xodtemplate_hostlist)temp_hostlist.get(i)).host_name) == common_h.ERROR) 
	            return common_h.ERROR;
	    }
	}
	
	/****** DUPLICATE SERVICE ESCALATION DEFINITIONS WITH ONE OR MORE HOSTGROUP AND/OR HOST NAMES ******/
	for ( ListIterator iter = xodtemplate_serviceescalation_list.listIterator(); iter.hasNext() ; ) {
        xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation = (xodtemplate_h.xodtemplate_serviceescalation) iter.next();
	    
	    /* get list of hosts */
	    ArrayList temp_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_serviceescalation.hostgroup_name,temp_serviceescalation.host_name);
	    if ( (temp_hostlist==null ) || (temp_hostlist.size() == 0 )){
	        logger.fatal( "Error: Could not expand hostgroups and/or hosts specified in service escalation (config file '"+xodtemplate_config_file_name(temp_serviceescalation._config_file)+"', starting on line "+temp_serviceescalation._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* duplicate service escalation entries */
	    temp_serviceescalation.host_name = ((xodtemplate_h.xodtemplate_hostlist)temp_hostlist.get(0)).host_name;
	    for ( int i = 1;  i < temp_hostlist.size(); i++ ) {
	        if ( xodtemplate_duplicate_serviceescalation( iter, temp_serviceescalation, ((xodtemplate_h.xodtemplate_hostlist)temp_hostlist.get(i)).host_name, null ) == common_h.ERROR ) 
	            return common_h.ERROR;
	    }
	}

	/****** DUPLICATE SERVICE ESCALATION DEFINITIONS WITH MULTIPLE DESCRIPTIONS ******/
	/* THIS MUST BE DONE AFTER DUPLICATING FOR MULTIPLE HOST NAMES (SEE ABOVE) */
	for ( ListIterator iter = xodtemplate_serviceescalation_list.listIterator(); iter.hasNext(); )
	{
	    xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation = (xodtemplate_h.xodtemplate_serviceescalation) iter.next();
	    
	    /* get list of services */
	    ArrayList temp_servicelist=xodtemplate_expand_servicegroups_and_services(null,temp_serviceescalation.host_name,temp_serviceescalation.service_description);
	    
	    if( (temp_servicelist==null) || ( temp_servicelist.size() == 0 ) )
	    {
	        logger.fatal( "Error: Could not expand services specified in service escalation (config file '"+xodtemplate_config_file_name(temp_serviceescalation._config_file)+"', starting on line "+temp_serviceescalation._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* duplicate service escalation entries */
	    temp_serviceescalation.service_description = ((xodtemplate_h.xodtemplate_servicelist)temp_servicelist.get(0)).service_description;
	    
	    for ( int i = 1;  i < temp_servicelist.size(); i++ )
	    {
	        xodtemplate_h.xodtemplate_servicelist this_servicelist = (xodtemplate_h.xodtemplate_servicelist) temp_servicelist.get(i);
            
	        // TODO raise question in forums abotu using temp_SE host_name instead of this_SE.
	        if ( xodtemplate_duplicate_serviceescalation( iter, temp_serviceescalation, temp_serviceescalation.host_name, this_servicelist.service_description ) == common_h.ERROR ) 
	            return common_h.ERROR;
	    }
	    
	}

	/****** DUPLICATE SERVICE ESCALATION DEFINITIONS WITH SERVICEGROUPS ******/
	/* THIS MUST BE DONE AFTER DUPLICATING FOR MULTIPLE HOST NAMES (SEE ABOVE) */
	
	//TODO - Rob 12/01/07 -  service_groups is not a required option for service escalation definition. Currently we
	// are assuming that the user will be using the service_group attribute within their service escalation definition.
	
	for ( ListIterator iter = xodtemplate_serviceescalation_list.listIterator(); iter.hasNext(); )
	{
	    xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation = (xodtemplate_h.xodtemplate_serviceescalation) iter.next();
	    
	    if(temp_serviceescalation.servicegroup_name != null)
	    {
	    	/* get list of services */
	    	ArrayList temp_servicelist=xodtemplate_expand_servicegroups_and_services(temp_serviceescalation.servicegroup_name,null,null);
	    
	    	if ( (temp_servicelist==null) || (temp_servicelist.size() == 0) )
	    	{
	    		logger.fatal( "Error: Could not expand servicegroups specified in service escalation (config file '"+xodtemplate_config_file_name(temp_serviceescalation._config_file)+"', starting on line "+temp_serviceescalation._start_line+")");
	    		return common_h.ERROR;
	    	}
	    
	    	/* duplicate service escalation entries */
	    	temp_serviceescalation.host_name = ((xodtemplate_h.xodtemplate_servicelist)temp_servicelist.get(0)).host_name;
	    	temp_serviceescalation.service_description = ((xodtemplate_h.xodtemplate_servicelist)temp_servicelist.get(0)).service_description;
	    
	    	for ( int i = 1;  i < temp_servicelist.size(); i++ )
	    	{
	    		xodtemplate_h.xodtemplate_servicelist this_servicelist = (xodtemplate_h.xodtemplate_servicelist) temp_servicelist.get(i);
	    		int result=xodtemplate_duplicate_serviceescalation(iter, temp_serviceescalation,this_servicelist.host_name,this_servicelist.service_description);
	        
	    		if ( result == common_h.ERROR )
	    			return common_h.ERROR;
	    	}
	    
	    }
	}

	/****** DUPLICATE HOST DEPENDENCY DEFINITIONS WITH MULTIPLE HOSTGROUP AND/OR HOST NAMES (MASTER AND DEPENDENT) ******/
	for ( ListIterator iter = xodtemplate_hostdependency_list.listIterator(); iter.hasNext(); ) {
	    xodtemplate_h.xodtemplate_hostdependency temp_hostdependency = (xodtemplate_h.xodtemplate_hostdependency) iter.next();
	    
	    /* get list of master host names */
	    ArrayList master_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_hostdependency.hostgroup_name,temp_hostdependency.host_name);
	    if ( (master_hostlist==null) || ( master_hostlist.size() == 0 ) ){
	        logger.fatal( "Error: Could not expand master hostgroups and/or hosts specified in service escalation (config file '"+xodtemplate_config_file_name(temp_hostdependency._config_file)+"', starting on line "+temp_hostdependency._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* get list of dependent host names */
	    ArrayList dependent_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_hostdependency.dependent_hostgroup_name,temp_hostdependency.dependent_host_name);
	    if( (dependent_hostlist==null) || (dependent_hostlist.size()==0) ){
	        logger.fatal( "Error: Could not expand dependent host and/or hosts specified in service escalation (config file '"+xodtemplate_config_file_name(temp_hostdependency._config_file)+"', starting on line "+temp_hostdependency._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* duplicate the dependency definitions */
	    temp_hostdependency.host_name=((xodtemplate_h.xodtemplate_hostlist) master_hostlist.get(0)).host_name;
	    temp_hostdependency.dependent_host_name=( (xodtemplate_h.xodtemplate_hostlist) dependent_hostlist.get(0)).host_name;
	    
	    for (int i = 0,j = 1;i < master_hostlist.size();i++,j = 0)
	    {
	        for (;j < dependent_hostlist.size(); j++)
	        {
	            String master_host_name=((xodtemplate_h.xodtemplate_hostlist) master_hostlist.get(i)).host_name;
	            String dependent_host_name=( (xodtemplate_h.xodtemplate_hostlist) dependent_hostlist.get(j)).host_name;
	            
	            if ( xodtemplate_duplicate_hostdependency( iter, temp_hostdependency,master_host_name,dependent_host_name) == common_h.ERROR )
	                return common_h.ERROR;
	        }
	    }
	}

	/****** DUPLICATE SERVICE DEPENDENCY DEFINITIONS WITH MULTIPLE HOSTGROUP AND/OR HOST NAMES (MASTER AND DEPENDENT) ******/
	for ( ListIterator iter = xodtemplate_servicedependency_list.listIterator(); iter.hasNext(); ) {
	    xodtemplate_h.xodtemplate_servicedependency temp_servicedependency = (xodtemplate_h.xodtemplate_servicedependency) iter.next();
	    
	    
	    /* get list of master host names */
	    ArrayList master_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_servicedependency.hostgroup_name,temp_servicedependency.host_name);
	    if ( (master_hostlist==null) || ( master_hostlist.size() == 0 ) ){
	        logger.fatal( "Error: Could not expand master hostgroups and/or hosts specified in service dependency (config file '"+xodtemplate_config_file_name(temp_servicedependency._config_file)+"', starting on line "+temp_servicedependency._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* get list of dependent host names */
	    ArrayList dependent_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_servicedependency.dependent_hostgroup_name,temp_servicedependency.dependent_host_name);
	    if( (dependent_hostlist==null) || (dependent_hostlist.size()==0) ){
	        logger.fatal( "Error: Could not expand dependent hostgropus and/or hosts specified in service dependency (config file '"+xodtemplate_config_file_name(temp_servicedependency._config_file)+"', starting on line "+temp_servicedependency._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* duplicate the dependency definitions */
	    temp_servicedependency.host_name=((xodtemplate_h.xodtemplate_hostlist) master_hostlist.get(0)).host_name;
	    temp_servicedependency.dependent_host_name=( (xodtemplate_h.xodtemplate_hostlist) dependent_hostlist.get(0)).host_name;
	    for ( int i = 0, j = 1;  i < master_hostlist.size(); i++, j = 0 ) {
	        for (  ;  j < dependent_hostlist.size(); j++ ) {
	            String master_host_name=((xodtemplate_h.xodtemplate_hostlist) master_hostlist.get(i)).host_name;
	            String dependent_host_name=( (xodtemplate_h.xodtemplate_hostlist) dependent_hostlist.get(j)).host_name;
	            
	            if ( xodtemplate_duplicate_servicedependency(iter, temp_servicedependency,master_host_name, temp_servicedependency.service_description,dependent_host_name ,temp_servicedependency.dependent_service_description) == common_h.ERROR )
	                return common_h.ERROR;
	        }
	    }
	}

	/****** DUPLICATE SERVICE DEPENDENCY DEFINITIONS WITH MULTIPLE MASTER DESCRIPTIONS ******/
	/* THIS MUST BE DONE AFTER DUPLICATING FOR MULTIPLE HOST NAMES (SEE ABOVE) */
	for ( ListIterator iter = xodtemplate_servicedependency_list.listIterator(); iter.hasNext(); ) {
	    xodtemplate_h.xodtemplate_servicedependency temp_servicedependency = (xodtemplate_h.xodtemplate_servicedependency) iter.next();
	    
	    /* get list of services */
	    ArrayList temp_servicelist= xodtemplate_expand_servicegroups_and_services(temp_servicedependency.servicegroup_name,temp_servicedependency.host_name,temp_servicedependency.service_description);
	    if( (temp_servicelist==null) && ( temp_servicelist.size() == 0 ) ) {
	        logger.fatal( "Error: Could not expand services specified in service dependency (config file '"+xodtemplate_config_file_name(temp_servicedependency._config_file)+"', starting on line "+temp_servicedependency._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* duplicate service escalation entries */
	    temp_servicedependency.service_description= ((xodtemplate_h.xodtemplate_servicelist) temp_servicelist.get(0)).service_description;
	    for ( int i = 1;  i < temp_servicelist.size(); i++ ) {
	        String service_description = ((xodtemplate_h.xodtemplate_servicelist) temp_servicelist.get(i)).service_description;
	        int result=xodtemplate_duplicate_servicedependency(iter, temp_servicedependency,temp_servicedependency.host_name, service_description,temp_servicedependency.dependent_host_name,temp_servicedependency.dependent_service_description);
	        if(result==common_h.ERROR)
	            return common_h.ERROR;
	    }
	}

	/****** DUPLICATE SERVICE DEPENDENCY DEFINITIONS WITH MULTIPLE DEPENDENCY DESCRIPTIONS ******/
	/* THIS MUST BE DONE AFTER DUPLICATING FOR MULTIPLE HOST NAMES (SEE ABOVE) */
	for ( ListIterator iter = xodtemplate_servicedependency_list.listIterator(); iter.hasNext(); ) {
	    xodtemplate_h.xodtemplate_servicedependency temp_servicedependency = (xodtemplate_h.xodtemplate_servicedependency) iter.next();
	    
	    /* get list of services */
	    ArrayList temp_servicelist=xodtemplate_expand_servicegroups_and_services(temp_servicedependency.dependent_servicegroup_name,temp_servicedependency.dependent_host_name,temp_servicedependency.dependent_service_description);
	    if( (temp_servicelist==null) || ( temp_servicelist.size() == 0 ) ){
	        logger.fatal( "Error: Could not expand services specified in service dependency (config file '"+xodtemplate_config_file_name(temp_servicedependency._config_file)+"', starting on line "+temp_servicedependency._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* duplicate service escalation entries */
	    temp_servicedependency.dependent_service_description= ((xodtemplate_h.xodtemplate_servicelist) temp_servicelist.get(0)).service_description;
	    for ( int i = 1;  i < temp_servicelist.size(); i++ ) {
	        String dependent_service_description = ((xodtemplate_h.xodtemplate_servicelist) temp_servicelist.get(i)).service_description;
	        int result=xodtemplate_duplicate_servicedependency(iter, temp_servicedependency,temp_servicedependency.host_name, temp_servicedependency.service_description,temp_servicedependency.dependent_host_name,dependent_service_description);
	        if(result==common_h.ERROR)
	            return common_h.ERROR;
	    }
	}


	/****** DUPLICATE HOSTEXTINFO DEFINITIONS WITH ONE OR MORE HOSTGROUP AND/OR HOST NAMES ******/
	for ( ListIterator iter = xodtemplate_hostextinfo_list.listIterator(); iter.hasNext(); ) {
	    xodtemplate_h.xodtemplate_hostextinfo temp_hostextinfo = (xodtemplate_h.xodtemplate_hostextinfo) iter.next();
	    
	    /* get list of hosts */
	    ArrayList temp_hostlist= xodtemplate_expand_hostgroups_and_hosts(temp_hostextinfo.hostgroup_name,temp_hostextinfo.host_name);
	    if ( (temp_hostlist==null ) || (temp_hostlist.size() == 0 ) ) {
	        logger.fatal( "Error: Could not expand hostgroups and/or hosts specified in extended host info (config file '"+xodtemplate_config_file_name(temp_hostextinfo._config_file)+"', starting on line "+temp_hostextinfo._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* add a copy of the definition for every host in the hostgroup/host name list */
	    temp_hostextinfo.host_name=((xodtemplate_h.xodtemplate_hostlist) temp_hostlist.get(0)).host_name;
	    for ( int i = 1;  i < temp_hostlist.size(); i++ ) {
	        if( xodtemplate_duplicate_hostextinfo(iter, temp_hostextinfo,((xodtemplate_h.xodtemplate_hostlist) temp_hostlist.get(i)).host_name) == common_h.ERROR)
	            return common_h.ERROR;
	    }
	}

	/****** DUPLICATE SERVICEEXTINFO DEFINITIONS WITH ONE OR MORE HOSTGROUP AND/OR HOST NAMES ******/
	for ( ListIterator iter = xodtemplate_serviceextinfo_list.listIterator(); iter.hasNext(); ) {
	    xodtemplate_h.xodtemplate_serviceextinfo temp_serviceextinfo = (xodtemplate_h.xodtemplate_serviceextinfo) iter.next();
	    
	    /* get list of hosts */
	    ArrayList temp_hostlist=xodtemplate_expand_hostgroups_and_hosts(temp_serviceextinfo.hostgroup_name,temp_serviceextinfo.host_name);
	    if ( (temp_hostlist==null ) || (temp_hostlist.size() == 0 ) ) {
	        logger.fatal( "Error: Could not expand hostgroups and/or hosts specified in extended service info (config file '"+xodtemplate_config_file_name(temp_serviceextinfo._config_file)+"', starting on line "+temp_serviceextinfo._start_line+")");
	        return common_h.ERROR;
	    }
	    
	    /* add a copy of the definition for every host in the hostgroup/host name list */
	    temp_serviceextinfo.host_name=((xodtemplate_h.xodtemplate_hostlist) temp_hostlist.get(0)).host_name;
	    for ( int i = 1;  i < temp_hostlist.size(); i++ ) {
	        if( xodtemplate_duplicate_serviceextinfo(iter, temp_serviceextinfo,((xodtemplate_h.xodtemplate_hostlist) temp_hostlist.get(i)).host_name) == common_h.ERROR)
	            return common_h.ERROR;
	    }
	}

	logger.trace( "exiting " + cn + ".xodtemplate_duplicate_objects");
	return common_h.OK;
        }

/* duplicates a service definition (with a new host name) */
public static int xodtemplate_duplicate_service(ListIterator iter, xodtemplate_h.xodtemplate_service temp_service, String host_name){
	logger.trace( "entering " + cn + ".xodtemplate_duplicate_service");

    /* allocate memory for a new service definition */
    xodtemplate_h.xodtemplate_service new_service = (xodtemplate_h.xodtemplate_service) temp_service.clone();
    
	/* override copy */
	new_service.hostgroup_name=null;
    new_service.host_name=host_name;

    iter.add( new_service );
    
	logger.trace( "exiting " + cn + ".xodtemplate_duplicate_service");
	return common_h.OK;
        }

/* duplicates a host escalation definition (with a new host name) */
public static int xodtemplate_duplicate_hostescalation(ListIterator iter, xodtemplate_h.xodtemplate_hostescalation temp_hostescalation, String host_name){
    logger.trace( "entering " + cn + ".xodtemplate_duplicate_hostescalation");
    
    /* allocate memory for a new host escalation definition */
    xodtemplate_h.xodtemplate_hostescalation  new_hostescalation = (xodtemplate_h.xodtemplate_hostescalation) temp_hostescalation.clone(); 
    
    /* overrides */
    new_hostescalation.hostgroup_name=null;
    new_hostescalation.host_name=host_name;
    
    /* add new hostescalation to head of list in memory */
    iter.add( new_hostescalation  );
    
    logger.trace( "exiting " + cn + ".xodtemplate_duplicate_hostescalation");
    return common_h.OK;
}

/* duplicates a service escalation definition (with a new host name and/or service description) */
public static int xodtemplate_duplicate_serviceescalation( ListIterator iter, xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation, String host_name, String svc_description)
{
    
    logger.trace( "entering " + cn + ".xodtemplate_duplicate_serviceescalation() start\n");
    
    /* allocate memory for a new service escalation definition */
    xodtemplate_h.xodtemplate_serviceescalation new_serviceescalation=(xodtemplate_h.xodtemplate_serviceescalation ) temp_serviceescalation.clone();
    
    /* overrides */
    new_serviceescalation.servicegroup_name=null;
    new_serviceescalation.hostgroup_name=null;
    new_serviceescalation.host_name=host_name;
    new_serviceescalation.service_description=svc_description;
    
    // TODO Foloowing line was in original, is it a bug?  Overrides statement above, right?
    // new_serviceescalation->host_name=strdup(temp_serviceescalation->host_name);
    // new_serviceescalation->service_description=strdup(temp_serviceescalation->service_description);
    /* add new serviceescalation to head of list in memory */
    iter.add( new_serviceescalation );
    
    logger.trace( "exiting " + cn + ".xodtemplate_duplicate_serviceescalation");
    return common_h.OK;
}

/* duplicates a host dependency definition (with master and dependent host names) */
public static int xodtemplate_duplicate_hostdependency(ListIterator iter, xodtemplate_h.xodtemplate_hostdependency temp_hostdependency, String master_host_name, String dependent_host_name){
	logger.trace( "entering " + cn + ".xodtemplate_duplicate_hostdependency");

	/* allocate memory for a new host dependency definition */
    xodtemplate_h.xodtemplate_hostdependency new_hostdependency = (xodtemplate_h.xodtemplate_hostdependency) temp_hostdependency.clone();

	/* overrides */
	new_hostdependency.hostgroup_name=null;
	new_hostdependency.dependent_hostgroup_name=null;
    new_hostdependency.host_name=master_host_name;
    new_hostdependency.dependent_host_name=dependent_host_name;

	/* add new hostdependency to head of list in memory */
	iter.add( new_hostdependency );

	logger.trace( "exiting " + cn + ".xodtemplate_duplicate_hostdependency");
	return common_h.OK;
        }

/* duplicates a service dependency definition */
public static int xodtemplate_duplicate_servicedependency(ListIterator iter, xodtemplate_h.xodtemplate_servicedependency temp_servicedependency, String master_host_name, String master_service_description, String dependent_host_name, String dependent_service_description){
    
    logger.trace( "entering " + cn + ".xodtemplate_duplicate_servicedependency");
    
    /* allocate memory for a new service dependency definition */
    xodtemplate_h.xodtemplate_servicedependency new_servicedependency=(xodtemplate_h.xodtemplate_servicedependency ) temp_servicedependency.clone();
    
    /* overrides */
    new_servicedependency.servicegroup_name=null;
    new_servicedependency.hostgroup_name=null;
    new_servicedependency.dependent_servicegroup_name=null;
    new_servicedependency.dependent_hostgroup_name=null;
    
    new_servicedependency.host_name=master_host_name;
    new_servicedependency.service_description=master_service_description;
    new_servicedependency.dependent_host_name=dependent_host_name;
    new_servicedependency.dependent_service_description=dependent_service_description;
    
    /* add new servicedependency to head of list in memory */
    iter.add( new_servicedependency );
    
    logger.trace( "exiting " + cn + ".xodtemplate_duplicate_servicedependency");
    return common_h.OK;
}

/* duplicates a hostextinfo object definition */
public static int xodtemplate_duplicate_hostextinfo(ListIterator iter, xodtemplate_h.xodtemplate_hostextinfo this_hostextinfo, String host_name){
    logger.trace( "entering " + cn + ".xodtemplate_duplicate_hostextinfo");
    
    xodtemplate_h.xodtemplate_hostextinfo new_hostextinfo = (xodtemplate_h.xodtemplate_hostextinfo) this_hostextinfo.clone();
    
    /* overrides */
    new_hostextinfo.hostgroup_name=null;
    new_hostextinfo.host_name=host_name;
    
    
    /* add new object to head of list */
    iter.add( new_hostextinfo );
    
    logger.trace( "exiting " + cn + ".xodtemplate_duplicate_hostextinfo");
    return common_h.OK;
}

/* duplicates a serviceextinfo object definition */
public static int xodtemplate_duplicate_serviceextinfo(ListIterator iter, xodtemplate_h.xodtemplate_serviceextinfo this_serviceextinfo, String host_name){
	logger.trace( "entering " + cn + ".xodtemplate_duplicate_serviceextinfo");

    xodtemplate_h.xodtemplate_serviceextinfo new_serviceextinfo=(xodtemplate_h.xodtemplate_serviceextinfo) this_serviceextinfo.clone();

    /* overrides */
	new_serviceextinfo.hostgroup_name=null;
    new_serviceextinfo.host_name=host_name;

	/* add new object to head of list */
	iter.add( new_serviceextinfo );

	logger.trace( "exiting " + cn + ".xodtemplate_duplicate_serviceextinfo");
	return common_h.OK;
        }


/******************************************************************/
/***************** OBJECT RESOLUTION FUNCTIONS ********************/
/******************************************************************/


/* resolves object definitions */
public static int xodtemplate_resolve_objects( ){
    logger.trace( "entering " + cn + ".xodtemplate_resolve_objects");
    
    /* resolve all timeperiod objects */
    for(ListIterator iter = xodtemplate_timeperiod_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_timeperiod((xodtemplate_h.xodtemplate_timeperiod) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all command objects */
    for(ListIterator iter = xodtemplate_command_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_command((xodtemplate_h.xodtemplate_command) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all contactgroup objects */
    for(ListIterator iter = xodtemplate_contactgroup_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_contactgroup((xodtemplate_h.xodtemplate_contactgroup) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all hostgroup objects */
    for(ListIterator iter = xodtemplate_hostgroup_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_hostgroup((xodtemplate_h.xodtemplate_hostgroup) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all servicegroup objects */
    for(ListIterator iter = xodtemplate_servicegroup_list.listIterator() ; iter.hasNext();)
    {
        if(xodtemplate_resolve_servicegroup((xodtemplate_h.xodtemplate_servicegroup) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all servicedependency objects */
    for(ListIterator iter = xodtemplate_servicedependency_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_servicedependency((xodtemplate_h.xodtemplate_servicedependency) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all serviceescalation objects */
    for(ListIterator iter = xodtemplate_serviceescalation_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_serviceescalation((xodtemplate_h.xodtemplate_serviceescalation) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all contact objects */
    for(ListIterator iter = xodtemplate_contact_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_contact((xodtemplate_h.xodtemplate_contact) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all host objects */
    for(ListIterator iter = xodtemplate_host_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_host((xodtemplate_h.xodtemplate_host) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all service objects */
    for(ListIterator iter = xodtemplate_service_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_service((xodtemplate_h.xodtemplate_service) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all hostdependency objects */
    for(ListIterator iter = xodtemplate_hostdependency_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_hostdependency((xodtemplate_h.xodtemplate_hostdependency) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all hostescalation objects */
    for(ListIterator iter = xodtemplate_hostescalation_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_hostescalation((xodtemplate_h.xodtemplate_hostescalation) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all hostextinfo objects */
    for(ListIterator iter = xodtemplate_hostextinfo_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_hostextinfo((xodtemplate_h.xodtemplate_hostextinfo) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    /* resolve all serviceextinfo objects */
    for(ListIterator iter = xodtemplate_serviceextinfo_list.listIterator() ; iter.hasNext();  ){
        if(xodtemplate_resolve_serviceextinfo((xodtemplate_h.xodtemplate_serviceextinfo) iter.next() )==common_h.ERROR)
            return common_h.ERROR;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_objects");
    return common_h.OK;
}

/* resolves a timeperiod object */
public static int xodtemplate_resolve_timeperiod(xodtemplate_h.xodtemplate_timeperiod this_timeperiod){
    xodtemplate_h.xodtemplate_timeperiod template_timeperiod =  null;
    
    logger.trace( "entering " + cn + ".xodtemplate_resolve_timeperiod" );
    
    /* return if this timeperiod has already been resolved */
    if(this_timeperiod.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_timeperiod.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_timeperiod.template== null)
        return common_h.OK;
    
    template_timeperiod= xodtemplate_find_timeperiod(this_timeperiod.template);
    if(template_timeperiod==null){
        logger.fatal( "Error: Template '"+this_timeperiod.template+"' specified in timeperiod definition could not be not found (config file '"+xodtemplate_config_file_name(this_timeperiod._config_file)+"', starting on line "+this_timeperiod._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template timeperiod... */
    xodtemplate_resolve_timeperiod(template_timeperiod);
    
    /* apply missing properties from template timeperiod... */
    if(this_timeperiod.timeperiod_name== null && template_timeperiod.timeperiod_name!=null)
        this_timeperiod.timeperiod_name=template_timeperiod.timeperiod_name;
    if(this_timeperiod.alias== null && template_timeperiod.alias!=null)
        this_timeperiod.alias=template_timeperiod.alias;
    for(int x=0;x<7;x++){
        if(this_timeperiod.timeranges[x]==null && template_timeperiod.timeranges[x]!=null){
            this_timeperiod.timeranges[x]=template_timeperiod.timeranges[x];
        }
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_timeperiod" );
    return common_h.OK;
}

/* resolves a command object */
public static int xodtemplate_resolve_command(xodtemplate_h.xodtemplate_command this_command){
    xodtemplate_h.xodtemplate_command template_command;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_command" );
    
    /* return if this command has already been resolved */
    if(this_command.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_command.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_command.template==null)
        return common_h.OK;
    
    template_command=xodtemplate_find_command(this_command.template);
    if(template_command==null){
        logger.fatal( "Error: Template '"+this_command.template+"' specified in command definition could not be not found (config file '"+xodtemplate_config_file_name(this_command._config_file)+"', starting on line "+this_command._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template command... */
    xodtemplate_resolve_command(template_command);
    
    /* apply missing properties from template command... */
    if(this_command.command_name==null && template_command.command_name!=null)
        this_command.command_name=template_command.command_name;
    if(this_command.command_line==null && template_command.command_line!=null)
        this_command.command_line=template_command.command_line;
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_command" );
    return common_h.OK;
}

/* resolves a contactgroup object */
public static int xodtemplate_resolve_contactgroup(xodtemplate_h.xodtemplate_contactgroup this_contactgroup){
    xodtemplate_h.xodtemplate_contactgroup template_contactgroup = null;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_contactgroup" );
    
    /* return if this contactgroup has already been resolved */
    if(this_contactgroup.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_contactgroup.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_contactgroup.template==null)
        return common_h.OK;
    
    template_contactgroup=xodtemplate_find_contactgroup(this_contactgroup.template);
    if(template_contactgroup==null){
        logger.fatal( "Error: Template '"+this_contactgroup.template+"' specified in contactgroup definition could not be not found (config file '"+xodtemplate_config_file_name(this_contactgroup._config_file)+"', starting on line "+this_contactgroup._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template contactgroup... */
    xodtemplate_resolve_contactgroup(template_contactgroup);
    
    /* apply missing properties from template contactgroup... */
    if(this_contactgroup.contactgroup_name==null && template_contactgroup.contactgroup_name!=null)
        this_contactgroup.contactgroup_name=template_contactgroup.contactgroup_name;
    if(this_contactgroup.alias==null && template_contactgroup.alias!=null)
        this_contactgroup.alias=template_contactgroup.alias;
    if(this_contactgroup.members==null && template_contactgroup.members!=null)
        this_contactgroup.members=template_contactgroup.members;
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_contactgroup" );
    return common_h.OK;
}

/* resolves a hostgroup object */
public static int xodtemplate_resolve_hostgroup(xodtemplate_h.xodtemplate_hostgroup this_hostgroup){
    xodtemplate_h.xodtemplate_hostgroup template_hostgroup;
    
    logger.trace( "entering " + cn + ".xodtemplate_resolve_hostgroup" );
    
    
    /* return if this hostgroup has already been resolved */
    if(this_hostgroup.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_hostgroup.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_hostgroup.template==null)
        return common_h.OK;
    
    template_hostgroup=xodtemplate_find_hostgroup(this_hostgroup.template);
    if(template_hostgroup==null){
        logger.fatal( "Error: Template '"+this_hostgroup.template+"' specified in hostgroup definition could not be not found (config file '"+xodtemplate_config_file_name(this_hostgroup._config_file)+"', starting on line "+this_hostgroup._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template hostgroup... */
    xodtemplate_resolve_hostgroup(template_hostgroup);
    
    /* apply missing properties from template hostgroup... */
    if(this_hostgroup.hostgroup_name==null && template_hostgroup.hostgroup_name!=null)
        this_hostgroup.hostgroup_name=template_hostgroup.hostgroup_name;
    if(this_hostgroup.alias==null && template_hostgroup.alias!=null)
        this_hostgroup.alias=template_hostgroup.alias;
    if(this_hostgroup.members==null && template_hostgroup.members!=null)
        this_hostgroup.members=template_hostgroup.members;
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_hostgroup" );
    return common_h.OK;
}

/* resolves a servicegroup object */
public static int xodtemplate_resolve_servicegroup(xodtemplate_h.xodtemplate_servicegroup this_servicegroup)
{
    /* Create another servicegroup object incase we are dealing with templates */
	xodtemplate_h.xodtemplate_servicegroup template_servicegroup;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_servicegroup" );
    
    /* return if this servicegroup has already been resolved */
    if(this_servicegroup.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_servicegroup.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_servicegroup.template==null)
        return common_h.OK;
    
    template_servicegroup=xodtemplate_find_servicegroup(this_servicegroup.template);
    if(template_servicegroup==null){
        logger.fatal( "Error: Template '"+this_servicegroup.template+"' specified in servicegroup definition could not be not found (config file '"+xodtemplate_config_file_name(this_servicegroup._config_file)+"', starting on line "+this_servicegroup._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template servicegroup... */
    xodtemplate_resolve_servicegroup(template_servicegroup);
    
    /* apply missing properties from template servicegroup... */
    if(this_servicegroup.servicegroup_name==null && template_servicegroup.servicegroup_name!=null)
        this_servicegroup.servicegroup_name=(template_servicegroup.servicegroup_name);
    if(this_servicegroup.alias==null && template_servicegroup.alias!=null)
        this_servicegroup.alias=(template_servicegroup.alias);
    if(this_servicegroup.members==null && template_servicegroup.members!=null)
        this_servicegroup.members=(template_servicegroup.members);
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_servicegroup" );
    return common_h.OK;
}

/* resolves a servicedependency object */
public static int xodtemplate_resolve_servicedependency(xodtemplate_h.xodtemplate_servicedependency this_servicedependency){
    xodtemplate_h.xodtemplate_servicedependency template_servicedependency;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_servicedependency");
    
    /* return if this servicedependency has already been resolved */
    if(this_servicedependency.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_servicedependency.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_servicedependency.template==null)
        return common_h.OK;
    
    template_servicedependency=xodtemplate_find_servicedependency(this_servicedependency.template);
    if(template_servicedependency==null){
        logger.fatal( "Error: Template '"+this_servicedependency.template+"' specified in service dependency definition could not be not found (config file '"+xodtemplate_config_file_name(this_servicedependency._config_file)+"', starting on line "+this_servicedependency._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template servicedependency... */
    xodtemplate_resolve_servicedependency(template_servicedependency);
    
    /* apply missing properties from template servicedependency... */
    if(this_servicedependency.servicegroup_name==null && template_servicedependency.servicegroup_name!=null)
        this_servicedependency.servicegroup_name=(template_servicedependency.servicegroup_name);
    if(this_servicedependency.hostgroup_name==null && template_servicedependency.hostgroup_name!=null)
        this_servicedependency.hostgroup_name=(template_servicedependency.hostgroup_name);
    if(this_servicedependency.host_name==null && template_servicedependency.host_name!=null)
        this_servicedependency.host_name=(template_servicedependency.host_name);
    if(this_servicedependency.service_description==null && template_servicedependency.service_description!=null)
        this_servicedependency.service_description=(template_servicedependency.service_description);
    if(this_servicedependency.dependent_servicegroup_name==null && template_servicedependency.dependent_servicegroup_name!=null)
        this_servicedependency.dependent_servicegroup_name=(template_servicedependency.dependent_servicegroup_name);
    if(this_servicedependency.dependent_hostgroup_name==null && template_servicedependency.dependent_hostgroup_name!=null)
        this_servicedependency.dependent_hostgroup_name=(template_servicedependency.dependent_hostgroup_name);
    if(this_servicedependency.dependent_host_name==null && template_servicedependency.dependent_host_name!=null)
        this_servicedependency.dependent_host_name=(template_servicedependency.dependent_host_name);
    if(this_servicedependency.dependent_service_description==null && template_servicedependency.dependent_service_description!=null)
        this_servicedependency.dependent_service_description=(template_servicedependency.dependent_service_description);
    if(this_servicedependency.have_inherits_parent==common_h.FALSE && template_servicedependency.have_inherits_parent==common_h.TRUE){
        this_servicedependency.inherits_parent=template_servicedependency.inherits_parent;
        this_servicedependency.have_inherits_parent=common_h.TRUE;
    }
    if(this_servicedependency.have_execution_dependency_options==common_h.FALSE && template_servicedependency.have_execution_dependency_options==common_h.TRUE){
        this_servicedependency.fail_execute_on_ok=template_servicedependency.fail_execute_on_ok;
        this_servicedependency.fail_execute_on_unknown=template_servicedependency.fail_execute_on_unknown;
        this_servicedependency.fail_execute_on_warning=template_servicedependency.fail_execute_on_warning;
        this_servicedependency.fail_execute_on_critical=template_servicedependency.fail_execute_on_critical;
        this_servicedependency.fail_execute_on_pending=template_servicedependency.fail_execute_on_pending;
        this_servicedependency.have_execution_dependency_options=common_h.TRUE;
    }
    if(this_servicedependency.have_notification_dependency_options==common_h.FALSE && template_servicedependency.have_notification_dependency_options==common_h.TRUE){
        this_servicedependency.fail_notify_on_ok=template_servicedependency.fail_notify_on_ok;
        this_servicedependency.fail_notify_on_unknown=template_servicedependency.fail_notify_on_unknown;
        this_servicedependency.fail_notify_on_warning=template_servicedependency.fail_notify_on_warning;
        this_servicedependency.fail_notify_on_critical=template_servicedependency.fail_notify_on_critical;
        this_servicedependency.fail_notify_on_pending=template_servicedependency.fail_notify_on_pending;
        this_servicedependency.have_notification_dependency_options=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_servicedependency");
    return common_h.OK;
}

/* resolves a serviceescalation object */
public static int xodtemplate_resolve_serviceescalation(xodtemplate_h.xodtemplate_serviceescalation this_serviceescalation){
    xodtemplate_h.xodtemplate_serviceescalation template_serviceescalation;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_serviceescalation");
    
    /* return if this serviceescalation has already been resolved */
    if(this_serviceescalation.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_serviceescalation.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_serviceescalation.template==null)
        return common_h.OK;
    
    template_serviceescalation=xodtemplate_find_serviceescalation(this_serviceescalation.template);
    if(template_serviceescalation==null){
        logger.fatal( "Error: Template '"+this_serviceescalation.template+"' specified in service escalation definition could not be not found (config file '"+xodtemplate_config_file_name(this_serviceescalation._config_file)+"', starting on line "+this_serviceescalation._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template serviceescalation... */
    xodtemplate_resolve_serviceescalation(template_serviceescalation);
    
    /* apply missing properties from template serviceescalation... */
    if(this_serviceescalation.servicegroup_name==null && template_serviceescalation.servicegroup_name!=null)
        this_serviceescalation.servicegroup_name=(template_serviceescalation.servicegroup_name);
    if(this_serviceescalation.hostgroup_name==null && template_serviceescalation.hostgroup_name!=null)
        this_serviceescalation.hostgroup_name=(template_serviceescalation.hostgroup_name);
    if(this_serviceescalation.host_name==null && template_serviceescalation.host_name!=null)
        this_serviceescalation.host_name=(template_serviceescalation.host_name);
    if(this_serviceescalation.service_description==null && template_serviceescalation.service_description!=null)
        this_serviceescalation.service_description=(template_serviceescalation.service_description);
    if(this_serviceescalation.escalation_period==null && template_serviceescalation.escalation_period!=null)
        this_serviceescalation.escalation_period=(template_serviceescalation.escalation_period);
    if(this_serviceescalation.contact_groups==null && template_serviceescalation.contact_groups!=null)
        this_serviceescalation.contact_groups=(template_serviceescalation.contact_groups);
    if(this_serviceescalation.have_first_notification==common_h.FALSE && template_serviceescalation.have_first_notification==common_h.TRUE){
        this_serviceescalation.first_notification=template_serviceescalation.first_notification;
        this_serviceescalation.have_first_notification=common_h.TRUE;
    }
    if(this_serviceescalation.have_last_notification==common_h.FALSE && template_serviceescalation.have_last_notification==common_h.TRUE){
        this_serviceescalation.last_notification=template_serviceescalation.last_notification;
        this_serviceescalation.have_last_notification=common_h.TRUE;
    }
    if(this_serviceescalation.have_notification_interval==common_h.FALSE && template_serviceescalation.have_notification_interval==common_h.TRUE){
        this_serviceescalation.notification_interval=template_serviceescalation.notification_interval;
        this_serviceescalation.have_notification_interval=common_h.TRUE;
    }
    if(this_serviceescalation.have_escalation_options==common_h.FALSE && template_serviceescalation.have_escalation_options==common_h.TRUE){
        this_serviceescalation.escalate_on_warning=template_serviceescalation.escalate_on_warning;
        this_serviceescalation.escalate_on_unknown=template_serviceescalation.escalate_on_unknown;
        this_serviceescalation.escalate_on_critical=template_serviceescalation.escalate_on_critical;
        this_serviceescalation.escalate_on_recovery=template_serviceescalation.escalate_on_recovery;
        this_serviceescalation.have_escalation_options=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_serviceescalation");
    return common_h.OK;
}

/* resolves a contact object */
public static int xodtemplate_resolve_contact(xodtemplate_h.xodtemplate_contact this_contact){
    xodtemplate_h.xodtemplate_contact template_contact;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_contact");
    
    /* return if this contact has already been resolved */
    if(this_contact.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_contact.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_contact.template==null)
        return common_h.OK;
    
    template_contact=xodtemplate_find_contact(this_contact.template);
    if(template_contact==null){
        logger.fatal( "Error: Template '"+this_contact.template+"' specified in contact definition could not be not found (config file '"+xodtemplate_config_file_name(this_contact._config_file)+"', starting on line "+this_contact._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template contact... */
    xodtemplate_resolve_contact(template_contact);
    
    /* apply missing properties from template contact... */
    if(this_contact.contact_name==null && template_contact.contact_name!=null)
        this_contact.contact_name=(template_contact.contact_name);
    if(this_contact.alias==null && template_contact.alias!=null)
        this_contact.alias=(template_contact.alias);
    if(this_contact.email==null && template_contact.email!=null)
        this_contact.email=(template_contact.email);
    if(this_contact.pager==null && template_contact.pager!=null)
        this_contact.pager=(template_contact.pager);
    for(int x=0;x<xodtemplate_h.MAX_XODTEMPLATE_CONTACT_ADDRESSES;x++){
        if(this_contact.address[x]==null && template_contact.address[x]!=null)
            this_contact.address[x]=(template_contact.address[x]);
    }
    if(this_contact.contactgroups==null && template_contact.contactgroups!=null)
        this_contact.contactgroups=template_contact.contactgroups;
    if(this_contact.host_notification_period==null && template_contact.host_notification_period!=null)
        this_contact.host_notification_period=(template_contact.host_notification_period);
    if(this_contact.service_notification_period==null && template_contact.service_notification_period!=null)
        this_contact.service_notification_period=(template_contact.service_notification_period);
    if(this_contact.host_notification_commands==null && template_contact.host_notification_commands!=null)
        this_contact.host_notification_commands=(template_contact.host_notification_commands);
    if(this_contact.service_notification_commands==null && template_contact.service_notification_commands!=null)
        this_contact.service_notification_commands=(template_contact.service_notification_commands);
    if(this_contact.have_host_notification_options==common_h.FALSE && template_contact.have_host_notification_options==common_h.TRUE){
        this_contact.notify_on_host_down=template_contact.notify_on_host_down;
        this_contact.notify_on_host_unreachable=template_contact.notify_on_host_unreachable;
        this_contact.notify_on_host_recovery=template_contact.notify_on_host_recovery;
        this_contact.notify_on_host_flapping=template_contact.notify_on_host_flapping;
        this_contact.have_host_notification_options=common_h.TRUE;
    }
    if(this_contact.have_service_notification_options==common_h.FALSE && template_contact.have_service_notification_options==common_h.TRUE){
        this_contact.notify_on_service_unknown=template_contact.notify_on_service_unknown;
        this_contact.notify_on_service_warning=template_contact.notify_on_service_warning;
        this_contact.notify_on_service_critical=template_contact.notify_on_service_critical;
        this_contact.notify_on_service_recovery=template_contact.notify_on_service_recovery;
        this_contact.notify_on_service_flapping=template_contact.notify_on_service_flapping;
        this_contact.have_service_notification_options=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_contact");
    return common_h.OK;
}

/* resolves a host object */
public static int xodtemplate_resolve_host(xodtemplate_h.xodtemplate_host this_host){
    xodtemplate_h.xodtemplate_host template_host;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_host");
    
    /* return if this host has already been resolved */
    if(this_host.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_host.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_host.template==null)
        return common_h.OK;
    
    template_host=xodtemplate_find_host(this_host.template);
    if(template_host==null){
        logger.fatal( "Error: Template '"+this_host.template+"' specified in host definition could not be not found (config file '"+xodtemplate_config_file_name(this_host._config_file)+"', starting on line "+this_host._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template host... */
    xodtemplate_resolve_host(template_host);
    
    /* apply missing properties from template host... */
    if(this_host.host_name==null && template_host.host_name!=null)
        this_host.host_name=(template_host.host_name);
    if(this_host.alias==null && template_host.alias!=null)
        this_host.alias=(template_host.alias);
    if(this_host.address==null && template_host.address!=null)
        this_host.address=(template_host.address);
    if(this_host.parents==null && template_host.parents!=null)
        this_host.parents=(template_host.parents);
    if(this_host.hostgroups==null && template_host.hostgroups!=null)
        this_host.hostgroups=(template_host.hostgroups);
    if(this_host.check_command==null && template_host.check_command!=null)
        this_host.check_command=(template_host.check_command);
    if(this_host.check_period==null && template_host.check_period!=null)
        this_host.check_period=(template_host.check_period);
    if(this_host.event_handler==null && template_host.event_handler!=null)
        this_host.event_handler=(template_host.event_handler);
    if(this_host.contact_groups==null && template_host.contact_groups!=null)
        this_host.contact_groups=(template_host.contact_groups);
    if(this_host.notification_period==null && template_host.notification_period!=null)
        this_host.notification_period=(template_host.notification_period);
    if(this_host.failure_prediction_options==null && template_host.failure_prediction_options!=null)
        this_host.failure_prediction_options=(template_host.failure_prediction_options);
    if(this_host.have_check_interval==common_h.FALSE && template_host.have_check_interval==common_h.TRUE){
        this_host.check_interval=template_host.check_interval;
        this_host.have_check_interval=common_h.TRUE;
    }
    if(this_host.have_max_check_attempts==common_h.FALSE && template_host.have_max_check_attempts==common_h.TRUE){
        this_host.max_check_attempts=template_host.max_check_attempts;
        this_host.have_max_check_attempts=common_h.TRUE;
    }
    if(this_host.have_active_checks_enabled==common_h.FALSE && template_host.have_active_checks_enabled==common_h.TRUE){
        this_host.active_checks_enabled=template_host.active_checks_enabled;
        this_host.have_active_checks_enabled=common_h.TRUE;
    }
    if(this_host.have_passive_checks_enabled==common_h.FALSE && template_host.have_passive_checks_enabled==common_h.TRUE){
        this_host.passive_checks_enabled=template_host.passive_checks_enabled;
        this_host.have_passive_checks_enabled=common_h.TRUE;
    }
    if(this_host.have_obsess_over_host==common_h.FALSE && template_host.have_obsess_over_host==common_h.TRUE){
        this_host.obsess_over_host=template_host.obsess_over_host;
        this_host.have_obsess_over_host=common_h.TRUE;
    }
    if(this_host.have_event_handler_enabled==common_h.FALSE && template_host.have_event_handler_enabled==common_h.TRUE){
        this_host.event_handler_enabled=template_host.event_handler_enabled;
        this_host.have_event_handler_enabled=common_h.TRUE;
    }
    if(this_host.have_check_freshness==common_h.FALSE && template_host.have_check_freshness==common_h.TRUE){
        this_host.check_freshness=template_host.check_freshness;
        this_host.have_check_freshness=common_h.TRUE;
    }
    if(this_host.have_freshness_threshold==common_h.FALSE && template_host.have_freshness_threshold==common_h.TRUE){
        this_host.freshness_threshold=template_host.freshness_threshold;
        this_host.have_freshness_threshold=common_h.TRUE;
    }
    if(this_host.have_low_flap_threshold==common_h.FALSE && template_host.have_low_flap_threshold==common_h.TRUE){
        this_host.low_flap_threshold=template_host.low_flap_threshold;
        this_host.have_low_flap_threshold=common_h.TRUE;
    }
    if(this_host.have_high_flap_threshold==common_h.FALSE && template_host.have_high_flap_threshold==common_h.TRUE){
        this_host.high_flap_threshold=template_host.high_flap_threshold;
        this_host.have_high_flap_threshold=common_h.TRUE;
    }
    if(this_host.have_flap_detection_enabled==common_h.FALSE && template_host.have_flap_detection_enabled==common_h.TRUE){
        this_host.flap_detection_enabled=template_host.flap_detection_enabled;
        this_host.have_flap_detection_enabled=common_h.TRUE;
    }
    if(this_host.have_notification_options==common_h.FALSE && template_host.have_notification_options==common_h.TRUE){
        this_host.notify_on_down=template_host.notify_on_down;
        this_host.notify_on_unreachable=template_host.notify_on_unreachable;
        this_host.notify_on_recovery=template_host.notify_on_recovery;
        this_host.notify_on_flapping=template_host.notify_on_flapping;
        this_host.have_notification_options=common_h.TRUE;
    }
    if(this_host.have_notifications_enabled==common_h.FALSE && template_host.have_notifications_enabled==common_h.TRUE){
        this_host.notifications_enabled=template_host.notifications_enabled;
        this_host.have_notifications_enabled=common_h.TRUE;
    }
    if(this_host.have_notification_interval==common_h.FALSE && template_host.have_notification_interval==common_h.TRUE){
        this_host.notification_interval=template_host.notification_interval;
        this_host.have_notification_interval=common_h.TRUE;
    }
    if(this_host.have_stalking_options==common_h.FALSE && template_host.have_stalking_options==common_h.TRUE){
        this_host.stalk_on_up=template_host.stalk_on_up;
        this_host.stalk_on_down=template_host.stalk_on_down;
        this_host.stalk_on_unreachable=template_host.stalk_on_unreachable;
        this_host.have_stalking_options=common_h.TRUE;
    }
    if(this_host.have_process_perf_data==common_h.FALSE && template_host.have_process_perf_data==common_h.TRUE){
        this_host.process_perf_data=template_host.process_perf_data;
        this_host.have_process_perf_data=common_h.TRUE;
    }
    if(this_host.have_failure_prediction_enabled==common_h.FALSE && template_host.have_failure_prediction_enabled==common_h.TRUE){
        this_host.failure_prediction_enabled=template_host.failure_prediction_enabled;
        this_host.have_failure_prediction_enabled=common_h.TRUE;
    }
    if(this_host.have_retain_status_information==common_h.FALSE && template_host.have_retain_status_information==common_h.TRUE){
        this_host.retain_status_information=template_host.retain_status_information;
        this_host.have_retain_status_information=common_h.TRUE;
    }
    if(this_host.have_retain_nonstatus_information==common_h.FALSE && template_host.have_retain_nonstatus_information==common_h.TRUE){
        this_host.retain_nonstatus_information=template_host.retain_nonstatus_information;
        this_host.have_retain_nonstatus_information=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_host");
    return common_h.OK;
}

/* resolves a service object */
public static int xodtemplate_resolve_service(xodtemplate_h.xodtemplate_service this_service){
    xodtemplate_h.xodtemplate_service template_service;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_service");
    
    /* return if this service has already been resolved */
    if(this_service.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_service.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_service.template==null)
        return common_h.OK;
    
    template_service=xodtemplate_find_service(this_service.template);
    if(template_service==null){
        logger.fatal( "Error: Template '"+this_service.template+"' specified in service definition could not be not found (config file '"+xodtemplate_config_file_name(this_service._config_file)+"', starting on line "+this_service._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template service... */
    xodtemplate_resolve_service(template_service);
    
    /* apply missing properties from template service... */
    if(this_service.hostgroup_name==null && template_service.hostgroup_name!=null)
        this_service.hostgroup_name=(template_service.hostgroup_name);
    if(this_service.host_name==null && template_service.host_name!=null)
        this_service.host_name=(template_service.host_name);
    if(this_service.service_description==null && template_service.service_description!=null)
        this_service.service_description=(template_service.service_description);
    if(this_service.servicegroups==null && template_service.servicegroups!=null)
        this_service.servicegroups=(template_service.servicegroups);
    if(this_service.check_command==null && template_service.check_command!=null)
        this_service.check_command=(template_service.check_command);
    if(this_service.check_period==null && template_service.check_period!=null)
        this_service.check_period=(template_service.check_period);
    if(this_service.event_handler==null && template_service.event_handler!=null)
        this_service.event_handler=(template_service.event_handler);
    if(this_service.notification_period==null && template_service.notification_period!=null)
        this_service.notification_period=(template_service.notification_period);
    if(this_service.contact_groups==null && template_service.contact_groups!=null)
        this_service.contact_groups=(template_service.contact_groups);
    if(this_service.failure_prediction_options==null && template_service.failure_prediction_options!=null)
        this_service.failure_prediction_options=(template_service.failure_prediction_options);
    if(this_service.have_max_check_attempts==common_h.FALSE && template_service.have_max_check_attempts==common_h.TRUE){
        this_service.max_check_attempts=template_service.max_check_attempts;
        this_service.have_max_check_attempts=common_h.TRUE;
    }
    if(this_service.have_normal_check_interval==common_h.FALSE && template_service.have_normal_check_interval==common_h.TRUE){
        this_service.normal_check_interval=template_service.normal_check_interval;
        this_service.have_normal_check_interval=common_h.TRUE;
    }
    if(this_service.have_retry_check_interval==common_h.FALSE && template_service.have_retry_check_interval==common_h.TRUE){
        this_service.retry_check_interval=template_service.retry_check_interval;
        this_service.have_retry_check_interval=common_h.TRUE;
    }
    if(this_service.have_active_checks_enabled==common_h.FALSE && template_service.have_active_checks_enabled==common_h.TRUE){
        this_service.active_checks_enabled=template_service.active_checks_enabled;
        this_service.have_active_checks_enabled=common_h.TRUE;
    }
    if(this_service.have_passive_checks_enabled==common_h.FALSE && template_service.have_passive_checks_enabled==common_h.TRUE){
        this_service.passive_checks_enabled=template_service.passive_checks_enabled;
        this_service.have_passive_checks_enabled=common_h.TRUE;
    }
    if(this_service.have_parallelize_check==common_h.FALSE && template_service.have_parallelize_check==common_h.TRUE){
        this_service.parallelize_check=template_service.parallelize_check;
        this_service.have_parallelize_check=common_h.TRUE;
    }
    if(this_service.have_is_volatile==common_h.FALSE && template_service.have_is_volatile==common_h.TRUE){
        this_service.is_volatile=template_service.is_volatile;
        this_service.have_is_volatile=common_h.TRUE;
    }
    if(this_service.have_obsess_over_service==common_h.FALSE && template_service.have_obsess_over_service==common_h.TRUE){
        this_service.obsess_over_service=template_service.obsess_over_service;
        this_service.have_obsess_over_service=common_h.TRUE;
    }
    if(this_service.have_event_handler_enabled==common_h.FALSE && template_service.have_event_handler_enabled==common_h.TRUE){
        this_service.event_handler_enabled=template_service.event_handler_enabled;
        this_service.have_event_handler_enabled=common_h.TRUE;
    }
    if(this_service.have_check_freshness==common_h.FALSE && template_service.have_check_freshness==common_h.TRUE){
        this_service.check_freshness=template_service.check_freshness;
        this_service.have_check_freshness=common_h.TRUE;
    }
    if(this_service.have_freshness_threshold==common_h.FALSE && template_service.have_freshness_threshold==common_h.TRUE){
        this_service.freshness_threshold=template_service.freshness_threshold;
        this_service.have_freshness_threshold=common_h.TRUE;
    }
    if(this_service.have_low_flap_threshold==common_h.FALSE && template_service.have_low_flap_threshold==common_h.TRUE){
        this_service.low_flap_threshold=template_service.low_flap_threshold;
        this_service.have_low_flap_threshold=common_h.TRUE;
    }
    if(this_service.have_high_flap_threshold==common_h.FALSE && template_service.have_high_flap_threshold==common_h.TRUE){
        this_service.high_flap_threshold=template_service.high_flap_threshold;
        this_service.have_high_flap_threshold=common_h.TRUE;
    }
    if(this_service.have_flap_detection_enabled==common_h.FALSE && template_service.have_flap_detection_enabled==common_h.TRUE){
        this_service.flap_detection_enabled=template_service.flap_detection_enabled;
        this_service.have_flap_detection_enabled=common_h.TRUE;
    }
    if(this_service.have_notification_options==common_h.FALSE && template_service.have_notification_options==common_h.TRUE){
        this_service.notify_on_unknown=template_service.notify_on_unknown;
        this_service.notify_on_warning=template_service.notify_on_warning;
        this_service.notify_on_critical=template_service.notify_on_critical;
        this_service.notify_on_recovery=template_service.notify_on_recovery;
        this_service.notify_on_flapping=template_service.notify_on_flapping;
        this_service.have_notification_options=common_h.TRUE;
    }
    if(this_service.have_notifications_enabled==common_h.FALSE && template_service.have_notifications_enabled==common_h.TRUE){
        this_service.notifications_enabled=template_service.notifications_enabled;
        this_service.have_notifications_enabled=common_h.TRUE;
    }
    if(this_service.have_notification_interval==common_h.FALSE && template_service.have_notification_interval==common_h.TRUE){
        this_service.notification_interval=template_service.notification_interval;
        this_service.have_notification_interval=common_h.TRUE;
    }
    if(this_service.have_stalking_options==common_h.FALSE && template_service.have_stalking_options==common_h.TRUE){
        this_service.stalk_on_ok=template_service.stalk_on_ok;
        this_service.stalk_on_unknown=template_service.stalk_on_unknown;
        this_service.stalk_on_warning=template_service.stalk_on_warning;
        this_service.stalk_on_critical=template_service.stalk_on_critical;
        this_service.have_stalking_options=common_h.TRUE;
    }
    if(this_service.have_process_perf_data==common_h.FALSE && template_service.have_process_perf_data==common_h.TRUE){
        this_service.process_perf_data=template_service.process_perf_data;
        this_service.have_process_perf_data=common_h.TRUE;
    }
    if(this_service.have_failure_prediction_enabled==common_h.FALSE && template_service.have_failure_prediction_enabled==common_h.TRUE){
        this_service.failure_prediction_enabled=template_service.failure_prediction_enabled;
        this_service.have_failure_prediction_enabled=common_h.TRUE;
    }
    if(this_service.have_retain_status_information==common_h.FALSE && template_service.have_retain_status_information==common_h.TRUE){
        this_service.retain_status_information=template_service.retain_status_information;
        this_service.have_retain_status_information=common_h.TRUE;
    }
    if(this_service.have_retain_nonstatus_information==common_h.FALSE && template_service.have_retain_nonstatus_information==common_h.TRUE){
        this_service.retain_nonstatus_information=template_service.retain_nonstatus_information;
        this_service.have_retain_nonstatus_information=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_service");
    return common_h.OK;
}

/* resolves a hostdependency object */
public static int xodtemplate_resolve_hostdependency(xodtemplate_h.xodtemplate_hostdependency this_hostdependency){
    xodtemplate_h.xodtemplate_hostdependency template_hostdependency;
	logger.trace( "entering " + cn + ".xodtemplate_resolve_hostdependency");

	/* return if this hostdependency has already been resolved */
	if(this_hostdependency.has_been_resolved==common_h.TRUE)
		return common_h.OK;

	/* set the resolved flag */
	this_hostdependency.has_been_resolved=common_h.TRUE;

	/* return if we have no template */
	if(this_hostdependency.template==null)
		return common_h.OK;

	template_hostdependency=xodtemplate_find_hostdependency(this_hostdependency.template);
	if(template_hostdependency==null){
        logger.fatal( "Error: Template '"+this_hostdependency.template+"' specified in host dependency definition could not be not found (config file '"+xodtemplate_config_file_name(this_hostdependency._config_file)+"', starting on line "+this_hostdependency._start_line+")");
        return common_h.ERROR;
	        }

	/* resolve the template hostdependency... */
	xodtemplate_resolve_hostdependency(template_hostdependency);

	/* apply missing properties from template hostdependency... */
	if(this_hostdependency.hostgroup_name==null && template_hostdependency.hostgroup_name!=null)
		this_hostdependency.hostgroup_name=(template_hostdependency.hostgroup_name);
	if(this_hostdependency.dependent_hostgroup_name==null && template_hostdependency.dependent_hostgroup_name!=null)
		this_hostdependency.dependent_hostgroup_name=(template_hostdependency.dependent_hostgroup_name);
	if(this_hostdependency.host_name==null && template_hostdependency.host_name!=null)
		this_hostdependency.host_name=(template_hostdependency.host_name);
	if(this_hostdependency.dependent_host_name==null && template_hostdependency.dependent_host_name!=null)
		this_hostdependency.dependent_host_name=(template_hostdependency.dependent_host_name);
	if(this_hostdependency.have_inherits_parent==common_h.FALSE && template_hostdependency.have_inherits_parent==common_h.TRUE){
		this_hostdependency.inherits_parent=template_hostdependency.inherits_parent;
		this_hostdependency.have_inherits_parent=common_h.TRUE;
	        }
	if(this_hostdependency.have_execution_dependency_options==common_h.FALSE && template_hostdependency.have_execution_dependency_options==common_h.TRUE){
		this_hostdependency.fail_execute_on_up=template_hostdependency.fail_execute_on_up;
		this_hostdependency.fail_execute_on_down=template_hostdependency.fail_execute_on_down;
		this_hostdependency.fail_execute_on_unreachable=template_hostdependency.fail_execute_on_unreachable;
		this_hostdependency.fail_execute_on_pending=template_hostdependency.fail_execute_on_pending;
		this_hostdependency.have_execution_dependency_options=common_h.TRUE;
	        }
	if(this_hostdependency.have_notification_dependency_options==common_h.FALSE && template_hostdependency.have_notification_dependency_options==common_h.TRUE){
		this_hostdependency.fail_notify_on_up=template_hostdependency.fail_notify_on_up;
		this_hostdependency.fail_notify_on_down=template_hostdependency.fail_notify_on_down;
		this_hostdependency.fail_notify_on_unreachable=template_hostdependency.fail_notify_on_unreachable;
		this_hostdependency.fail_notify_on_pending=template_hostdependency.fail_notify_on_pending;
		this_hostdependency.have_notification_dependency_options=common_h.TRUE;
	        }

	logger.trace( "exiting " + cn + ".xodtemplate_resolve_hostdependency");
	return common_h.OK;
        }

/* resolves a hostescalation object */
public static int xodtemplate_resolve_hostescalation(xodtemplate_h.xodtemplate_hostescalation this_hostescalation){
    xodtemplate_h.xodtemplate_hostescalation template_hostescalation;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_hostescalation");
    
    /* return if this hostescalation has already been resolved */
    if(this_hostescalation.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_hostescalation.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_hostescalation.template==null)
        return common_h.OK;
    
    template_hostescalation=xodtemplate_find_hostescalation(this_hostescalation.template);
    if(template_hostescalation==null){
        logger.fatal( "Error: Template '"+this_hostescalation.template+"' specified in host escalation definition could not be not found (config file '"+xodtemplate_config_file_name(this_hostescalation._config_file)+"', starting on line "+this_hostescalation._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template hostescalation... */
    xodtemplate_resolve_hostescalation(template_hostescalation);
    
    /* apply missing properties from template hostescalation... */
    if(this_hostescalation.host_name==null && template_hostescalation.host_name!=null)
        this_hostescalation.host_name=(template_hostescalation.host_name);
    if(this_hostescalation.escalation_period==null && template_hostescalation.escalation_period!=null)
        this_hostescalation.escalation_period=(template_hostescalation.escalation_period);
    if(this_hostescalation.contact_groups==null && template_hostescalation.contact_groups!=null)
        this_hostescalation.contact_groups=(template_hostescalation.contact_groups);
    if(this_hostescalation.have_first_notification==common_h.FALSE && template_hostescalation.have_first_notification==common_h.TRUE){
        this_hostescalation.first_notification=template_hostescalation.first_notification;
        this_hostescalation.have_first_notification=common_h.TRUE;
    }
    if(this_hostescalation.have_last_notification==common_h.FALSE && template_hostescalation.have_last_notification==common_h.TRUE){
        this_hostescalation.last_notification=template_hostescalation.last_notification;
        this_hostescalation.have_last_notification=common_h.TRUE;
    }
    if(this_hostescalation.have_notification_interval==common_h.FALSE && template_hostescalation.have_notification_interval==common_h.TRUE){
        this_hostescalation.notification_interval=template_hostescalation.notification_interval;
        this_hostescalation.have_notification_interval=common_h.TRUE;
    }
    if(this_hostescalation.have_escalation_options==common_h.FALSE && template_hostescalation.have_escalation_options==common_h.TRUE){
        this_hostescalation.escalate_on_down=template_hostescalation.escalate_on_down;
        this_hostescalation.escalate_on_unreachable=template_hostescalation.escalate_on_unreachable;
        this_hostescalation.escalate_on_recovery=template_hostescalation.escalate_on_recovery;
        this_hostescalation.have_escalation_options=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_hostdependency");
    return common_h.OK;
}

/* resolves a hostextinfo object */
public static int xodtemplate_resolve_hostextinfo(xodtemplate_h.xodtemplate_hostextinfo this_hostextinfo){
    xodtemplate_h.xodtemplate_hostextinfo template_hostextinfo;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_hostextinfo");
    
    /* return if this object has already been resolved */
    if(this_hostextinfo.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_hostextinfo.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_hostextinfo.template==null)
        return common_h.OK;
    
    template_hostextinfo=xodtemplate_find_hostextinfo(this_hostextinfo.template);
    if(template_hostextinfo==null){
        logger.fatal( "Error: Template '"+this_hostextinfo.template+"' specified in extended host definition could not be not found (config file '"+xodtemplate_config_file_name(this_hostextinfo._config_file)+"', starting on line "+this_hostextinfo._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template hostextinfo... */
    xodtemplate_resolve_hostextinfo(template_hostextinfo);
    
    /* apply missing properties from template hostextinfo... */
    if(this_hostextinfo.host_name==null && template_hostextinfo.host_name!=null)
        this_hostextinfo.host_name=(template_hostextinfo.host_name);
    if(this_hostextinfo.hostgroup_name==null && template_hostextinfo.hostgroup_name!=null)
        this_hostextinfo.hostgroup_name=(template_hostextinfo.hostgroup_name);
    if(this_hostextinfo.notes==null && template_hostextinfo.notes!=null)
        this_hostextinfo.notes=(template_hostextinfo.notes);
    if(this_hostextinfo.notes_url==null && template_hostextinfo.notes_url!=null)
        this_hostextinfo.notes_url=(template_hostextinfo.notes_url);
    if(this_hostextinfo.action_url==null && template_hostextinfo.action_url!=null)
        this_hostextinfo.action_url=(template_hostextinfo.action_url);
    if(this_hostextinfo.icon_image==null && template_hostextinfo.icon_image!=null)
        this_hostextinfo.icon_image=(template_hostextinfo.icon_image);
    if(this_hostextinfo.icon_image_alt==null && template_hostextinfo.icon_image_alt!=null)
        this_hostextinfo.icon_image_alt=(template_hostextinfo.icon_image_alt);
    if(this_hostextinfo.vrml_image==null && template_hostextinfo.vrml_image!=null)
        this_hostextinfo.vrml_image=(template_hostextinfo.vrml_image);
    if(this_hostextinfo.statusmap_image==null && template_hostextinfo.statusmap_image!=null)
        this_hostextinfo.statusmap_image=(template_hostextinfo.statusmap_image);
    if(this_hostextinfo.have_2d_coords==common_h.FALSE && template_hostextinfo.have_2d_coords==common_h.TRUE){
        this_hostextinfo.x_2d=template_hostextinfo.x_2d;
        this_hostextinfo.y_2d=template_hostextinfo.y_2d;
        this_hostextinfo.have_2d_coords=common_h.TRUE;
    }
    if(this_hostextinfo.have_3d_coords==common_h.FALSE && template_hostextinfo.have_3d_coords==common_h.TRUE){
        this_hostextinfo.x_3d=template_hostextinfo.x_3d;
        this_hostextinfo.y_3d=template_hostextinfo.y_3d;
        this_hostextinfo.z_3d=template_hostextinfo.z_3d;
        this_hostextinfo.have_3d_coords=common_h.TRUE;
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_hostextinfo");
    return common_h.OK;
}

/* resolves a serviceextinfo object */
public static int xodtemplate_resolve_serviceextinfo(xodtemplate_h.xodtemplate_serviceextinfo this_serviceextinfo)
{
    xodtemplate_h.xodtemplate_serviceextinfo template_serviceextinfo;
    logger.trace( "entering " + cn + ".xodtemplate_resolve_serviceextinfo");
    
    /* return if this object has already been resolved */
    if(this_serviceextinfo.has_been_resolved==common_h.TRUE)
        return common_h.OK;
    
    /* set the resolved flag */
    this_serviceextinfo.has_been_resolved=common_h.TRUE;
    
    /* return if we have no template */
    if(this_serviceextinfo.template==null)
        return common_h.OK;
    
    template_serviceextinfo=xodtemplate_find_serviceextinfo(this_serviceextinfo.template);
    if(template_serviceextinfo==null){
        logger.fatal( "Error: Template '"+this_serviceextinfo+"' specified in extended service definition could not be not found (config file '"+xodtemplate_config_file_name(this_serviceextinfo._config_file)+"', starting on line "+this_serviceextinfo._start_line+")");
        return common_h.ERROR;
    }
    
    /* resolve the template serviceextinfo... */
    xodtemplate_resolve_serviceextinfo(template_serviceextinfo);
    
    /* apply missing properties from template serviceextinfo... */
    if(this_serviceextinfo.host_name==null && template_serviceextinfo.host_name!=null)
        this_serviceextinfo.host_name=(template_serviceextinfo.host_name);
    if(this_serviceextinfo.hostgroup_name==null && template_serviceextinfo.hostgroup_name!=null)
        this_serviceextinfo.hostgroup_name=(template_serviceextinfo.hostgroup_name);
    if(this_serviceextinfo.service_description==null && template_serviceextinfo.service_description!=null)
        this_serviceextinfo.service_description=(template_serviceextinfo.service_description);
    if(this_serviceextinfo.notes==null && template_serviceextinfo.notes!=null)
        this_serviceextinfo.notes=(template_serviceextinfo.notes);
    if(this_serviceextinfo.notes_url==null && template_serviceextinfo.notes_url!=null)
        this_serviceextinfo.notes_url=(template_serviceextinfo.notes_url);
    if(this_serviceextinfo.action_url==null && template_serviceextinfo.action_url!=null)
        this_serviceextinfo.action_url=(template_serviceextinfo.action_url);
    if(this_serviceextinfo.icon_image==null && template_serviceextinfo.icon_image!=null)
        this_serviceextinfo.icon_image=(template_serviceextinfo.icon_image);
    if(this_serviceextinfo.icon_image_alt==null && template_serviceextinfo.icon_image_alt!=null)
        this_serviceextinfo.icon_image_alt=(template_serviceextinfo.icon_image_alt);
    
    logger.trace( "exiting " + cn + ".xodtemplate_resolve_serviceextinfo");
    return common_h.OK;
}


/******************************************************************/
/*************** OBJECT RECOMBOBULATION FUNCTIONS *****************/
/******************************************************************/

/**
 * Method that correctly puts all contacts into the required contact groups. Will remove
 * contacts from contact groups that they should not be in.
 * 
 * @return = int, common_h.OK if everything is funky, common_h.ERROR otherwise.
 */

public static int xodtemplate_recombobulate_contactgroups()
{
    logger.trace( "entering " + cn + ".xodtemplate_recombobulate_contactgroups");
    
    /* process all contacts that have contactgroup directives */
    for(ListIterator iter = xodtemplate_contact_list.listIterator();iter.hasNext();)
    {
        /* Cast the first object within the xodtemplate_contact_list ArrayList to be a xodtemplate_contact object */
    	xodtemplate_h.xodtemplate_contact temp_contact = (xodtemplate_h.xodtemplate_contact)iter.next();
        
        /* skip contacts without contactgroup directives or contact names */
        if(temp_contact.contactgroups == null || temp_contact.contact_name == null)
            continue;
        
        /* process the list of contactgroups */
        String[] split = temp_contact.contactgroups.split( "," );
        
        for(int i = 0;i<split.length;i++)
        {
            String temp_ptr = split[i].trim();
            
            /* find the contactgroup */
            xodtemplate_h.xodtemplate_contactgroup temp_contactgroup=xodtemplate_find_real_contactgroup(temp_ptr);
            
            if(temp_contactgroup == null)
            {
                logger.fatal( "Error: Could not find contactgroup '"+temp_ptr+"' specified in contact '"+temp_contact.contact_name+"' definition (config file '"+xodtemplate_config_file_name(temp_contact._config_file)+"', starting on line "+temp_contact._start_line+")");
                return common_h.ERROR;
            }
            
            /* add this contact to the contactgroup members directive */
            if(temp_contactgroup.members == null)
                /* If we are the first contact within the contact group, simply add us to the list */
            	temp_contactgroup.members=temp_contact.contact_name;
            else
            {
                /* Otherwise pre-pend our entry with a ',' */
            	temp_contactgroup.members += "," + temp_contact.contact_name;
            }
        }
        
    }

    /* expand members of all contactgroups - this could be done in xodtemplate_register_contactgroup(), but we can save the CGIs some work if we do it here */
    /* UDPATED 2.2 */
    
    for(ListIterator iter = xodtemplate_contactgroup_list.listIterator(); iter.hasNext();)
    {
        /* Cast the object from the xodtemplate_contactgroup_list to be a xodtemplate_contactgroup object */
    	xodtemplate_h.xodtemplate_contactgroup temp_contactgroup = (xodtemplate_h.xodtemplate_contactgroup) iter.next();
        
        /* If there are no members within the contact group, continue */
    	if(temp_contactgroup.members==null)
    		continue;
        
        /* get list of contacts in the contactgroup */
        ArrayList temp_contactlist= xodtemplate_expand_contacts(temp_contactgroup.members);
        
        /* add all members to the contact group */
        if(temp_contactlist == null)
        {
            logger.fatal( "Error: Could not expand member contacts specified in contactgroup (config file '"+xodtemplate_config_file_name(temp_contactgroup._config_file)+"', starting on line "+temp_contactgroup._start_line+")");
            return common_h.ERROR;
        }
        
        temp_contactgroup.members = null;
        
        for(ListIterator clIter = temp_contactlist.listIterator(); clIter.hasNext();)
        {
            xodtemplate_h.xodtemplate_contactlist this_contactlist = (xodtemplate_h.xodtemplate_contactlist)clIter.next();
            
            /* add this contact to the contactgroup members directive */
            if(temp_contactgroup.members == null)
                temp_contactgroup.members=this_contactlist.contact_name;
            else
                temp_contactgroup.members += "," + this_contactlist.contact_name;
        }
    }
        
    logger.trace( "exiting " + cn + ".xodtemplate_recombobulate_contactgroups");
    return common_h.OK;
}

/* recombobulates hostgroup definitions */
public static int xodtemplate_recombobulate_hostgroups(){
    xodtemplate_h.xodtemplate_host temp_host;
    xodtemplate_h.xodtemplate_hostgroup temp_hostgroup;
    xodtemplate_h.xodtemplate_hostlist temp_hostlist;
    String temp_ptr;
    
    logger.trace( "entering " + cn + ".xodtemplate_recombobulate_hostgroups");
    
    /* process all hosts that have hostgroup directives */
    for (ListIterator iter = xodtemplate_host_list.listIterator(); iter.hasNext();  ) {
        temp_host = (xodtemplate_h.xodtemplate_host) iter.next();
        /* skip hosts without hostgroup directives or host names */
        /* skip hosts that shouldn't be registered */
        if(temp_host.hostgroups==null || temp_host.host_name== null ||  temp_host.register_object==common_h.FALSE)
            continue;
        
        /* process the list of hostgroups */
        String[] split = temp_host.hostgroups.split( "," );
        for ( int i = 0; i<split.length; i ++ ) {
            temp_ptr = split[i].trim();
            
            /* find the hostgroup */
            temp_hostgroup=xodtemplate_find_real_hostgroup(temp_ptr);
            if(temp_hostgroup== null){
                logger.fatal( "Error: Could not find hostgroup '"+temp_ptr+"' specified in host '"+temp_host.host_name+"' definition (config file '"+xodtemplate_config_file_name(temp_host._config_file)+"', starting on line "+temp_host._start_line+")");
                return common_h.ERROR;
            }
            
            /* add this list to the hostgroup members directive */
            if(temp_hostgroup.members == null )
                temp_hostgroup.members=temp_host.host_name;
            else
                temp_hostgroup.members += "," + temp_host.host_name ;
        }
    }
    
    /* expand members of all hostgroups - this could be done in xodtemplate_register_hostgroup(), but we can save the CGIs some work if we do it here */
    /* UPDATED 2.2 */
    for (ListIterator iter = xodtemplate_hostgroup_list.listIterator(); iter.hasNext(); ) {
        temp_hostgroup = (xodtemplate_h.xodtemplate_hostgroup) iter.next();
        
        /* skip hostgroups that shouldn't be registered */
        if(temp_hostgroup.members==null ||  temp_hostgroup.register_object== common_h.FALSE)
            continue;
        
        /* get list of hosts in the hostgroup */
        ArrayList temp_hostlist_array = xodtemplate_expand_hostgroups_and_hosts( null,temp_hostgroup.members);
        
        /* add all members to the host group */
        if(temp_hostlist_array == null){
            logger.fatal( "Error: Could not expand member hosts specified in hostgroup (config file '"+xodtemplate_config_file_name(temp_hostgroup._config_file)+"', starting on line "+temp_hostgroup._start_line+")");
            return common_h.ERROR;
        }
        
        temp_hostgroup.members = null;
        for (ListIterator iter2 = temp_hostlist_array.listIterator(); iter2.hasNext(); ) {
            temp_hostlist = (xodtemplate_h.xodtemplate_hostlist) iter2.next();
            
            /* add this host to the hostgroup members directive */
            if(temp_hostgroup.members== null)
                temp_hostgroup.members=temp_hostlist.host_name;
            else
                temp_hostgroup.members += "," + temp_hostlist.host_name;
        }
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_recombobulate_hostgroups");
    return common_h.OK;
}

/* recombobulates servicegroup definitions */
/***** THIS NEEDS TO BE CALLED AFTER OBJECTS (SERVICES) ARE RESOLVED AND DUPLICATED *****/
public static int xodtemplate_recombobulate_servicegroups()
{
    logger.trace( "entering " + cn + ".xodtemplate_recombobulate_servicegroups");
    
    /* process all services that have servicegroup directives */
    for ( ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext(); )
    {
        xodtemplate_h.xodtemplate_service temp_service = (xodtemplate_h.xodtemplate_service) iter.next();
        
        /* skip services without servicegroup directives or service names */
        /* skip services that shouldn't be registered */
        if(temp_service.servicegroups==null || temp_service.host_name==null || temp_service.service_description==null || temp_service.register_object==common_h.FALSE)
            continue;
        
        /* process the list of servicegroups */
        String[] split = temp_service.servicegroups.split( "," );
        for ( int i = 0; i<split.length; i ++ )
        {
            String temp_ptr = split[i].trim();
            
            /* find the servicegroup */
            xodtemplate_h.xodtemplate_servicegroup temp_servicegroup=xodtemplate_find_real_servicegroup(temp_ptr);
            if(temp_servicegroup==null){
                logger.fatal("Error: Could not find servicegroup '"+temp_ptr+"' specified in service '"+temp_service.service_description+"' on host '"+temp_service.host_name+"' definition (config file '"+xodtemplate_config_file_name(temp_service._config_file)+"', starting on line "+temp_service._start_line+")");
                return common_h.ERROR;
            }
            
            /* add this list to the servicegroup members directive */
            if ( temp_servicegroup.members == null || temp_servicegroup.members.length() == 0 )
                temp_servicegroup.members = temp_service.host_name + "," + temp_service.service_description;
            else 
                temp_servicegroup.members += "," + temp_service.host_name + "," + temp_service.service_description;
        }
    }

    
    /* expand members of all servicegroups - this could be done in xodtemplate_register_servicegroup(), but we can save the CGIs some work if we do it here */
    /* UPDATED 2.2 */
    for ( ListIterator iter = xodtemplate_servicegroup_list.listIterator(); iter.hasNext(); )
    {
        xodtemplate_h.xodtemplate_servicegroup temp_servicegroup  = (xodtemplate_h.xodtemplate_servicegroup) iter.next();
        
        /* skip servicegroups that shouldn't be registered */
        if(temp_servicegroup.members==null || temp_servicegroup.register_object==common_h.FALSE)
            continue;
        
        String[] split = temp_servicegroup.members.split( "," );
   
        temp_servicegroup.members = null;
        String host_name = null;
        String service_description = null;
        
        for (int i = 0; i<split.length; i++)
        {
            String temp_ptr = split[i].trim();
            
            /* this is the host name */
            if(host_name == null)
                host_name=temp_ptr;

            /* this is the service description */
            else
            {
                
            	service_description=temp_ptr;
                
            	/* get list of services in the servicegroup */
                ArrayList temp_servicelist=xodtemplate_expand_servicegroups_and_services(null,host_name,service_description);
                
                /* add all members to the service group */
                if(temp_servicelist==null)
                {
                	logger.fatal("Error: Could not expand member services specified in servicegroup (config file '"+xodtemplate_config_file_name(temp_servicegroup._config_file)+"', starting on line "+temp_servicegroup._start_line+")");
                    return common_h.ERROR;
                }
                
                for ( ListIterator slIter = temp_servicelist.listIterator(); slIter.hasNext(); )
                {
                    xodtemplate_h.xodtemplate_servicelist this_servicelist = (xodtemplate_h.xodtemplate_servicelist) slIter.next();
                    
                    /* add this service to the servicegroup members directive */
                    if ( temp_servicegroup.members == null || temp_servicegroup.members.length() == 0)
                        temp_servicegroup.members = this_servicelist.host_name + "," + this_servicelist.service_description;
                    else 
                        temp_servicegroup.members += "," + this_servicelist.host_name + "," + this_servicelist.service_description;
                }
                host_name=null;
                service_description=null;
            }
        }
        
        /* error if there were an odd number of items specified (unmatched host/service pair) */
        if(host_name!=null){
            logger.fatal( "Error: Servicegroup members must be specified in <host_name>,<service_description> pairs (config file '"+xodtemplate_config_file_name(temp_servicegroup._config_file)+"', starting on line "+temp_servicegroup._start_line+")");
            return common_h.ERROR;
        }
    }
    
    logger.trace( "exiting " + cn + ".xodtemplate_recombobulate_servicegroups");
    return common_h.OK;
}


/******************************************************************/
/******************* OBJECT SEARCH FUNCTIONS **********************/
/******************************************************************/

	/**
 	* Method used for finding a specific timeperiod object. Is also utilised within config
 	* parsing to warn of potential naming clashes.
 	* 
 	* 	@param String name, name of the timeperiod that you are looking for.
 	* @return, xodtemplate_timeperiod object, the object that you are looking for.
 	*/

	public static xodtemplate_h.xodtemplate_timeperiod xodtemplate_find_timeperiod(String name)
	{
	    if(name== null )
	        return  null;
	    
	    for(ListIterator iter = xodtemplate_timeperiod_list.listIterator(); iter.hasNext();)
	    {
            xodtemplate_h.xodtemplate_timeperiod temp_timeperiod = (xodtemplate_h.xodtemplate_timeperiod) iter.next();
            
	        if ((temp_timeperiod.name != null ) && ( temp_timeperiod.name.equals(name)))
	        return temp_timeperiod;
	    }
	    
	    return null;
	}

	
	/**
	 * Method used for finding a specific command object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the command that you are looking for.
	 * @return, xodtemplate_command object, the object that you are looking for.
	 */
	
	public static xodtemplate_h.xodtemplate_command xodtemplate_find_command(String name)
	{
	    if(name== null)
	        return  null;
		    
	    for(ListIterator iter = xodtemplate_command_list.listIterator(); iter.hasNext();)
	    {
            /* Cast the object from te xodtemplate_command_list ArrayList to be a xodtemplate_command object */
	    	xodtemplate_h.xodtemplate_command temp_command = (xodtemplate_h.xodtemplate_command)iter.next();

	    	/* Compare name values of the two objects and return object if they are equal */ 
            if((temp_command.name != null) && (temp_command.name.equals(name)))
	            return temp_command;
	    }
        
	    return null;
	}

	
	/**
	 * Method used for finding a specific contactgroup object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the contactgroup that you are looking for.
	 * @return, xodtemplate_contactgroup object, the object that you are looking for.
	 */
	
    public static xodtemplate_h.xodtemplate_contactgroup xodtemplate_find_contactgroup(String name)
    {
	    if(name== null)
	        return null;
	    
        for(ListIterator iter = xodtemplate_contactgroup_list.listIterator(); iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_contactgroup temp_contactgroup = (xodtemplate_h.xodtemplate_contactgroup) iter.next();
    
            if (( temp_contactgroup.name != null ) && ( temp_contactgroup.name.equals(name)))
	            return temp_contactgroup;
	    }
	    
	    return null;
	}

    /**
	 * Method used for finding a specific contactgroup object by it's real name rather
	 *  that it's template name. Is also utilised within config parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the contactgroup that you are looking for.
	 * @return, xodtemplate_contactgroup object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_contactgroup xodtemplate_find_real_contactgroup(String name){
        if(name== null)
            return null;
        
        for(ListIterator iter = xodtemplate_contactgroup_list.listIterator(); iter.hasNext() ; ){
            xodtemplate_h.xodtemplate_contactgroup temp_contactgroup = (xodtemplate_h.xodtemplate_contactgroup) iter.next();
            if ( ( temp_contactgroup.register_object != common_h.FALSE ) && ( temp_contactgroup.contactgroup_name != null ) && ( temp_contactgroup.contactgroup_name.equals( name ) ) )
                return temp_contactgroup;
        }
        
        return null;
    }

    /**
	 * Method used for finding a specific hostgroup object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the hostgroup that you are looking for.
	 * @return, xodtemplate_hostgroup object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_hostgroup xodtemplate_find_hostgroup(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_hostgroup_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_hostgroup temp_hostgroup = (xodtemplate_h.xodtemplate_hostgroup) iter.next();
         
            if (( temp_hostgroup.name != null ) && ( temp_hostgroup.name.equals(name)))
                return temp_hostgroup;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific hostgroup object by it's real name rather
	 *  that it's template name. Is also utilised within config parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the hostgroup that you are looking for.
	 * @return, xodtemplate_hostgroup object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_hostgroup xodtemplate_find_real_hostgroup(String name){
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_hostgroup_list.listIterator(); iter.hasNext() ; ){
            xodtemplate_h.xodtemplate_hostgroup temp_hostgroup = (xodtemplate_h.xodtemplate_hostgroup) iter.next();
            if ( (temp_hostgroup.register_object!=common_h.FALSE) && ( temp_hostgroup.hostgroup_name != null ) && ( temp_hostgroup.hostgroup_name.equals( name ) ) )
                return temp_hostgroup;
        }
        
        return null;
    }

    /**
	 * Method used for finding a specific servicegroup object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the servicegroup that you are looking for.
	 * @return, xodtemplate_servicegroup object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_servicegroup xodtemplate_find_servicegroup(String name)
    {        
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_servicegroup_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_servicegroup temp_servicegroup = (xodtemplate_h.xodtemplate_servicegroup) iter.next();
            
            if (( temp_servicegroup.name != null ) && ( temp_servicegroup.name.equals(name)))
                return temp_servicegroup;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific servicegroup object by it's real name rather
	 *  that it's template name. Is also utilised within config parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the servicegroup that you are looking for.
	 * @return, xodtemplate_servicegroup object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_servicegroup xodtemplate_find_real_servicegroup(String name){
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_servicegroup_list.listIterator(); iter.hasNext() ; ){
            xodtemplate_h.xodtemplate_servicegroup temp_servicegroup = (xodtemplate_h.xodtemplate_servicegroup) iter.next();
            if ( (temp_servicegroup.register_object!=common_h.FALSE) &&( temp_servicegroup.servicegroup_name != null ) && ( temp_servicegroup.servicegroup_name.equals( name ) ) )
                return temp_servicegroup;
        }
        
        return null;
    }
    
    
    /**
	 * Method used for finding a specific servicedependency object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the servicedependency object that you are looking for.
	 * @return, xodtemplate_servicedependency object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_servicedependency xodtemplate_find_servicedependency(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_servicedependency_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_servicedependency temp_servicedependency = (xodtemplate_h.xodtemplate_servicedependency) iter.next();
            
            if ((temp_servicedependency.name != null ) && ( temp_servicedependency.name.equals(name)))
                return temp_servicedependency;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific serviceescalation object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the serviceescalation object that you are looking for.
	 * @return, xodtemplate_serviceescalation object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_serviceescalation xodtemplate_find_serviceescalation(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_serviceescalation_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation = (xodtemplate_h.xodtemplate_serviceescalation) iter.next();
        
            if ((temp_serviceescalation.name != null ) && ( temp_serviceescalation.name.equals(name)))
                return temp_serviceescalation;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific contact object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the contact object that you are looking for.
	 * @return, xodtemplate_contact object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_contact xodtemplate_find_contact( String name)
    {
        if(name==null)
            return  null;
        
        for(ListIterator iter = xodtemplate_contact_list.listIterator(); iter.hasNext();)
        {
        	
            xodtemplate_h.xodtemplate_contact temp_contact = (xodtemplate_h.xodtemplate_contact) iter.next();
            
            if(( temp_contact.name != null ) && ( temp_contact.name.equals(name)))
                return temp_contact;
        }
        
        return null;
    }
    
    /**
	 * Method used for finding a specific contact object by it's real name rather
	 *  that it's template name. Is also utilised within config parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the contact that you are looking for.
	 * @return, xodtemplate_contact object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_contact xodtemplate_find_real_contact(String name)
    {
        if(name==null)
            return  null;
        
        for(ListIterator iter = xodtemplate_contact_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_contact temp_contact = (xodtemplate_h.xodtemplate_contact) iter.next();
        
            if ((temp_contact.register_object!=common_h.FALSE) && ( temp_contact.contact_name != null ) && ( temp_contact.contact_name.equals(name)))
                return temp_contact ;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific host object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the host object that you are looking for.
	 * @return, xodtemplate_host object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_host xodtemplate_find_host(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_host_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_host temp_host = (xodtemplate_h.xodtemplate_host) iter.next();
        
            if ((temp_host.name != null ) && ( temp_host.name.equals(name)))
                return temp_host;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific host object by it's real name rather
	 *  that it's template name. Is also utilised within config parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the host that you are looking for.
	 * @return, xodtemplate_host object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_host xodtemplate_find_real_host(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_host_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_host temp_host = (xodtemplate_h.xodtemplate_host) iter.next();
            
            if ((temp_host.register_object!= common_h.FALSE) && ( temp_host.host_name != null ) && ( temp_host.host_name.equals(name)))
                return temp_host ;
        }
        
        return null;
    }
    

    /**
	 * Method used for finding a specific hostdependecy object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the hostdependency object that you are looking for.
	 * @return, xodtemplate_hostdependecny object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_hostdependency xodtemplate_find_hostdependency(String name)
    {
        if(name == null)
            return null;
        
        for(ListIterator iter = xodtemplate_hostdependency_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_hostdependency temp_hostdependency = (xodtemplate_h.xodtemplate_hostdependency) iter.next();
            
            if ((temp_hostdependency.name != null ) && ( temp_hostdependency.name.equals(name)))
                return temp_hostdependency;
        }
        
        return null;
    }

    
    /**
	 * Method used for finding a specific hostescalation object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the hostescalation object that you are looking for.
	 * @return, xodtemplate_hostescalation object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_hostescalation xodtemplate_find_hostescalation(String name)
    {
        if(name == null)
            return null;
        
        for(ListIterator iter = xodtemplate_hostescalation_list.listIterator(); iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_hostescalation temp_hostescalation = (xodtemplate_h.xodtemplate_hostescalation) iter.next();
            
            if((temp_hostescalation.name != null ) && ( temp_hostescalation.name.equals(name)))
                return temp_hostescalation ;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific hostextinfo object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the hostextinfo object that you are looking for.
	 * @return, xodtemplate_hostextinfo object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_hostextinfo xodtemplate_find_hostextinfo(String name)
    {
        if(name == null)
            return null;
        
        for(ListIterator iter = xodtemplate_hostextinfo_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_hostextinfo temp_hostextinfo = (xodtemplate_h.xodtemplate_hostextinfo) iter.next();
            
            if ((temp_hostextinfo.name != null ) && ( temp_hostextinfo.name.equals(name)))
                return temp_hostextinfo;
        }
        
        return null;
    }


    /**
	 * Method used for finding a specific serviceextinfo object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the serviceextinfo object that you are looking for.
	 * @return, xodtemplate_serviceextinfo object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_serviceextinfo xodtemplate_find_serviceextinfo(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_serviceextinfo_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_serviceextinfo temp_serviceextinfo = (xodtemplate_h.xodtemplate_serviceextinfo) iter.next();
            
            if ((temp_serviceextinfo.name != null ) && ( temp_serviceextinfo.name.equals(name)))
                return temp_serviceextinfo;
        }
        
        return null;
    }
    
    
    /**
	 * Method used for finding a specific service object. Is also utilised within config
	 * parsing to warn of potential naming clashes.
	 * 
	 * @param String name, name of the service object that you are looking for.
	 * @return, xodtemplate_service object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_service xodtemplate_find_service(String name)
    {
        if(name==null)
            return null;
        
        for(ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_service temp_service = (xodtemplate_h.xodtemplate_service) iter.next();
            
            if ((temp_service.name != null ) && (temp_service.name.equals(name)))
                return temp_service;
        }
        return null;
    }


    /**
	 * Method used for finding a specific service object by it's real name rather
	 *  that it's template name. Is also utilised within config parsing to warn of potential naming clashes.
	 * 
	 * @param String host_name, name of the service that you are looking for.
	 * @param String service_description, the description of the service you are looking for.
	 * 
	 * @return, xodtemplate_service object, the object that you are looking for.
	 */
    
    public static xodtemplate_h.xodtemplate_service xodtemplate_find_real_service(String host_name, String service_description)
    {
        if(host_name==null || service_description == null)
            return null;
        
        for(ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext();)
        {
            xodtemplate_h.xodtemplate_service temp_service = (xodtemplate_h.xodtemplate_service) iter.next();
            
            //TODO - changed 11/01/07 by Rob. This used to check to see if temp_service.name == null
            // I'm pretty sure it should be temp_service.service_description
            
            if ((temp_service.register_object==common_h.FALSE) ||( temp_service.service_description == null) || (temp_service.host_name == null))
               	continue;
            
            if ((temp_service.host_name.equals( host_name ) ) && ( temp_service.service_description.equals(service_description )))
                return temp_service;
        }
        
        return null;
    }

    /******************************************************************/
    /**************** OBJECT REGISTRATION FUNCTIONS *******************/
    /******************************************************************/
    
    /**
     * This is a slight alteration on the xod_register_objects() method. Currently with
     * dynamic registration, the system will process multiple definitions from one file.
     * This causes a problem whereby if an object has already been defined by blue, any
     * of the following object definitions in the file will be ignored. For the time being
     * we want Blue to keep processing the file, but acknowledge that an object has been
     * ignored as one of the same type with the same name already exists.
     * 
     */
    public static int xodtemplate_register_dynamic_objects()
    {
    	logger.trace( "entering " + cn + ".xodtemplate_register_objects");
        
        /* register timeperiods */
        for ( ListIterator<xodtemplate_h.xodtemplate_timeperiod> iter = xodtemplate_timeperiod_list.listIterator() ; iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_timeperiod t = iter.next();
            
        	if(xodtemplate_register_timeperiod(t)==common_h.ERROR)
            {
            	logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Timeperiod with name '" + t.name + "' already exists.");
            }
        }
        
        /* register commands */
        for ( ListIterator<xodtemplate_h.xodtemplate_command> iter = xodtemplate_command_list.listIterator() ; iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_command c = iter.next();
            
        	if(xodtemplate_register_command(c)==common_h.ERROR)
        	{
        		logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Command with name '" + c.name + "' already exists");
        	}
        }
        
        /* register contactgroups */
        for ( ListIterator<xodtemplate_h.xodtemplate_contactgroup> iter = xodtemplate_contactgroup_list.listIterator() ; iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_contactgroup c = iter.next();
            
        	if(xodtemplate_register_contactgroup(c)==common_h.ERROR)
        	{
        		logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Contact Group with name '" + c.name + "' already exists");
        	}
        }
        
        /* register hostgroups */
        for ( ListIterator<xodtemplate_h.xodtemplate_hostgroup> iter = xodtemplate_hostgroup_list.listIterator() ; iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_hostgroup h = iter.next();
            
        	if(xodtemplate_register_hostgroup(h)==common_h.ERROR)
        	{
        		logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Host Group with name '" + h.name + "' already exists");
        	}
        }
        
        /* register servicegroups */
        for ( ListIterator<xodtemplate_h.xodtemplate_servicegroup> iter = xodtemplate_servicegroup_list.listIterator() ; iter.hasNext() ; )
        {
            xodtemplate_h.xodtemplate_servicegroup s = iter.next();
            
        	if(xodtemplate_register_servicegroup(s)==common_h.ERROR)
        	{
        		logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Service Group with name '" + s.name + "' already exists");
        	}
        }
        
        /* register contacts */
        for ( ListIterator<xodtemplate_h.xodtemplate_contact> iter = xodtemplate_contact_list.listIterator(); iter.hasNext();)
        {
        	xodtemplate_h.xodtemplate_contact c = iter.next();
        	
        	if (xodtemplate_register_contact(c) == common_h.ERROR)
        	{
        		logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Contact Group with name '" + c.name + "' already exists");
        	}
        }

      /* register hosts */
      for (ListIterator<xodtemplate_h.xodtemplate_host> iter = xodtemplate_host_list.listIterator(); iter.hasNext();)
      {
          xodtemplate_h.xodtemplate_host h = iter.next();
    	  
          if (xodtemplate_register_host(h) == common_h.ERROR)
          {
        	  logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Host with name '" + h.name + "' already exists");
          }
      }

      /* register services */
      for (ListIterator<xodtemplate_h.xodtemplate_service> iter = xodtemplate_service_list.listIterator(); iter.hasNext();)
      {
    	  xodtemplate_h.xodtemplate_service s = iter.next();
    	  
    	  if (xodtemplate_register_service(s) == common_h.ERROR)
    	  {
    		  logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Service with description '" +  s.host_name + "-" + s.service_description + "' already exists");
    	  }
      }

      /* register service dependencies */
      for (ListIterator<xodtemplate_h.xodtemplate_servicedependency> iter = xodtemplate_servicedependency_list.listIterator(); iter.hasNext();)
      {
    	  xodtemplate_h.xodtemplate_servicedependency s = iter.next();
    	  
    	  if (xodtemplate_register_servicedependency(s) == common_h.ERROR)
    	  {
    		  logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Service Dependency with name " + s.name + "' already exists");
    	  }
      }

      /* register service escalations */
      for (ListIterator<xodtemplate_h.xodtemplate_serviceescalation> iter = xodtemplate_serviceescalation_list.listIterator(); iter.hasNext();)
      {
    	  xodtemplate_h.xodtemplate_serviceescalation s = iter.next();
    	  
    	  if (xodtemplate_register_serviceescalation(s) == common_h.ERROR)
    	  {
    		  logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Service escalation with name '" + s.name + "' already exists");
    	  }
      }

      /* register host dependencies */
      for (ListIterator<xodtemplate_h.xodtemplate_hostdependency> iter = xodtemplate_hostdependency_list.listIterator(); iter.hasNext();)
      {
         xodtemplate_h.xodtemplate_hostdependency h = iter.next();
         
    	 if (xodtemplate_register_hostdependency(h) == common_h.ERROR)
    	 {
            logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Host Dependency with name '" + h.name + "' already exists");
    	 }
      }

      /* register host escalations */
      for (ListIterator<xodtemplate_h.xodtemplate_hostescalation> iter = xodtemplate_hostescalation_list.listIterator(); iter.hasNext();)
      {
    	  xodtemplate_h.xodtemplate_hostescalation h = iter.next();
    	  
    	  if (xodtemplate_register_hostescalation(h) == common_h.ERROR)
    	  {
            logger.debug(cn + ".xodtemplate_register_dynamic_objects() - Host Escalation with name '" + h.name + "' already exists.");
          }
      }

      /* register host extended info */
      for (ListIterator<xodtemplate_h.xodtemplate_hostextinfo> iter = xodtemplate_hostextinfo_list.listIterator(); iter.hasNext();)
      {
    	  xodtemplate_h.xodtemplate_hostextinfo h = iter.next();
    	  
    	  if (xodtemplate_register_hostextinfo((xodtemplate_h.xodtemplate_hostextinfo) iter.next()) == common_h.ERROR)
    	  {
    		  logger.debug(cn + ".xodtemplate_register_dynamic_objects() - HostExtInfo with name '" + h.name + "' already exists");
    	  }
      }

      /* register service extended info */
      for (ListIterator<xodtemplate_h.xodtemplate_serviceextinfo> iter = xodtemplate_serviceextinfo_list.listIterator(); iter.hasNext();)
      {
    	  xodtemplate_h.xodtemplate_serviceextinfo s = iter.next();
    	  
    	  if (xodtemplate_register_serviceextinfo(s) == common_h.ERROR)
    	  {
    		  logger.debug(cn + ".xodtemplate_register_dynamic_objects() - ServiceExtInfo with name '" + s.name + "' already exists");
    	  }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_objects");
      return common_h.OK;

    }
    
    /* registers object definitions */
    public static int xodtemplate_register_objects(){
        logger.trace( "entering " + cn + ".xodtemplate_register_objects");
        
        /* register timeperiods */
        for ( ListIterator iter = xodtemplate_timeperiod_list.listIterator() ; iter.hasNext() ; ) {
            if(xodtemplate_register_timeperiod((xodtemplate_h.xodtemplate_timeperiod) iter.next())==common_h.ERROR)
                return common_h.ERROR;
        }
        
        /* register commands */
        for ( ListIterator iter = xodtemplate_command_list.listIterator() ; iter.hasNext() ; ) {
            if(xodtemplate_register_command((xodtemplate_h.xodtemplate_command) iter.next())==common_h.ERROR)
                return common_h.ERROR;
        }
        
        /* register contactgroups */
        for ( ListIterator iter = xodtemplate_contactgroup_list.listIterator() ; iter.hasNext() ; ) {
            if(xodtemplate_register_contactgroup((xodtemplate_h.xodtemplate_contactgroup) iter.next())==common_h.ERROR)
                return common_h.ERROR;
        }
        
        /* register hostgroups */
        for ( ListIterator iter = xodtemplate_hostgroup_list.listIterator() ; iter.hasNext() ; ) {
            if(xodtemplate_register_hostgroup((xodtemplate_h.xodtemplate_hostgroup) iter.next())==common_h.ERROR)
                return common_h.ERROR;
        }
        
        /* register servicegroups */
        for ( ListIterator iter = xodtemplate_servicegroup_list.listIterator() ; iter.hasNext() ; ) {
            if(xodtemplate_register_servicegroup((xodtemplate_h.xodtemplate_servicegroup) iter.next())==common_h.ERROR)
                return common_h.ERROR;
        }
        
        /* register contacts */
        for ( ListIterator iter = xodtemplate_contact_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_contact((xodtemplate_h.xodtemplate_contact) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register hosts */
      for (ListIterator iter = xodtemplate_host_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_host((xodtemplate_h.xodtemplate_host) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register services */
      for (ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_service((xodtemplate_h.xodtemplate_service) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register service dependencies */
      for (ListIterator iter = xodtemplate_servicedependency_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_servicedependency((xodtemplate_h.xodtemplate_servicedependency) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register service escalations */
      for (ListIterator iter = xodtemplate_serviceescalation_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_serviceescalation((xodtemplate_h.xodtemplate_serviceescalation) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register host dependencies */
      for (ListIterator iter = xodtemplate_hostdependency_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_hostdependency((xodtemplate_h.xodtemplate_hostdependency) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register host escalations */
      for (ListIterator iter = xodtemplate_hostescalation_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_hostescalation((xodtemplate_h.xodtemplate_hostescalation) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register host extended info */
      for (ListIterator iter = xodtemplate_hostextinfo_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_hostextinfo((xodtemplate_h.xodtemplate_hostextinfo) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      /* register service extended info */
      for (ListIterator iter = xodtemplate_serviceextinfo_list.listIterator(); iter.hasNext();)
      {
         if (xodtemplate_register_serviceextinfo((xodtemplate_h.xodtemplate_serviceextinfo) iter.next()) == common_h.ERROR)
            return common_h.ERROR;
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_objects");
      return common_h.OK;
   }

   ///* registers a timeperiod definition */
   public static int xodtemplate_register_timeperiod(xodtemplate_h.xodtemplate_timeperiod this_timeperiod)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_timeperiod");

      /* bail out if we shouldn't register this object */
      if (this_timeperiod.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the timeperiod */
      objects_h.timeperiod new_timeperiod = objects.add_timeperiod(this_timeperiod.timeperiod_name,
            this_timeperiod.alias);

      /* return with an error if we couldn't add the timeperiod */
      if (new_timeperiod == null)
      {
         logger.fatal("Error: Could not register timeperiod (config file '"
               + xodtemplate_config_file_name(this_timeperiod._config_file) + "', starting on line "
               + this_timeperiod._start_line + ")");
         return common_h.ERROR;
      }

      /* add all necessary timeranges to timeperiod */
      for (int day = 0; day < 7; day++)
      {

         if (this_timeperiod.timeranges[day] == null)
            continue;

         // for each [HH:MM-HH:MM],...
         String[] split_range = this_timeperiod.timeranges[day].split("[, ]");
         for (int i = 0; i < split_range.length; i++)
         {
            String temp_range = split_range[i].trim();

            // for each [HH:MM]-HH:MM
            String[] split_time = temp_range.split("-");
            if (split_time.length == 2)
            {
               // Start Time
               String[] split_start = split_time[0].split(":");
               String[] split_end = split_time[1].split(":");
               if (split_start.length == 2 && split_end.length == 2)
               {
                  int hours = atoi(split_start[0]);
                  int minutes = atoi(split_start[1]);
                  long range_start_time = ((minutes * 60) + (hours * 60 * 60));
                  hours = atoi(split_end[0]);
                  minutes = atoi(split_end[1]);
                  long range_end_time = ((minutes * 60) + (hours * 3600));
                  if (objects.add_timerange_to_timeperiod(new_timeperiod, day, range_start_time, range_end_time) == null)
                  {
                     logger.fatal("Error: Could not add timerange for day " + day + " to timeperiod (config file '"
                           + xodtemplate_config_file_name(this_timeperiod._config_file) + "', starting on line "
                           + this_timeperiod._start_line + ")");
                  }
               }
            }
            else
            {
               logger.fatal("Error: Could not add timerange for day " + day + " to timeperiod (config file '"
                     + xodtemplate_config_file_name(this_timeperiod._config_file) + "', starting on line "
                     + this_timeperiod._start_line + ")");
               return common_h.ERROR;
            }
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_timeperiod");
      return common_h.OK;
   }

   /* registers a command definition */
   public static int xodtemplate_register_command(xodtemplate_h.xodtemplate_command this_command)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_command");

      /* bail out if we shouldn't register this object */
      if (this_command.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the command */
      objects_h.command new_command = objects.add_command(this_command.command_name, this_command.command_line);

      /* return with an error if we couldn't add the command */
      if (new_command == null)
      {
         logger.fatal("Error: Could not register command (config file '"
               + xodtemplate_config_file_name(this_command._config_file) + "', starting on line "
               + this_command._start_line + ")");
         return common_h.ERROR;
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_command");
      return common_h.OK;
   }

   /* registers a contactgroup definition */
   public static int xodtemplate_register_contactgroup(xodtemplate_h.xodtemplate_contactgroup this_contactgroup)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_contactgroup");

      /* bail out if we shouldn't register this object */
      if (this_contactgroup.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the contact group */
      objects_h.contactgroup new_contactgroup = objects.add_contactgroup(this_contactgroup.contactgroup_name,
            this_contactgroup.alias);

      /* return with an error if we couldn't add the contactgroup */
      if (new_contactgroup == null)
      {
         logger.fatal("Error: Could not register contactgroup (config file '"
               + xodtemplate_config_file_name(this_contactgroup._config_file) + "', starting on line "
               + this_contactgroup._start_line + ")");
         return common_h.ERROR;
      }

      /* add all members to the contact group */
      if (this_contactgroup.members == null)
      {
         logger.fatal("Error: Contactgroup has no members (config file '"
               + xodtemplate_config_file_name(this_contactgroup._config_file) + "', starting on line "
               + this_contactgroup._start_line + ")");
         return common_h.ERROR;
      }

      String[] split = this_contactgroup.members.split(",");
      for (int x = 0; x < split.length; x++)
      {
         if (objects.add_contact_to_contactgroup(new_contactgroup, split[x].trim()) == null)
         {
            logger.fatal("Error: Could not add contact '" + split[x] + "' to contactgroup (config file '"
                  + xodtemplate_config_file_name(this_contactgroup._config_file) + "', starting on line "
                  + this_contactgroup._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_contactgroup");
      return common_h.OK;
   }

   /* registers a hostgroup definition */
   public static int xodtemplate_register_hostgroup(xodtemplate_h.xodtemplate_hostgroup this_hostgroup)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_hostgroup");

      /* bail out if we shouldn't register this object */
      if (this_hostgroup.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the  host group */
      objects_h.hostgroup new_hostgroup = objects.add_hostgroup(this_hostgroup.hostgroup_name, this_hostgroup.alias);

      /* return with an error if we couldn't add the hostgroup */
      if (new_hostgroup == null)
      {
         logger.fatal("Error: Could not register hostgroup (config file '"
               + xodtemplate_config_file_name(this_hostgroup._config_file) + "', starting on line "
               + this_hostgroup._start_line + ")");
         return common_h.ERROR;
      }

      /* add all members to hostgroup */
      if (this_hostgroup.members == null)
      {
         logger.fatal("Error: Hostgroup has no members (config file '"
               + xodtemplate_config_file_name(this_hostgroup._config_file) + "', starting on line "
               + this_hostgroup._start_line + ")");
         return common_h.ERROR;
      }

      String[] split = this_hostgroup.members.split(",");
      for (int x = 0; x < split.length; x++)
      {
         if (objects.add_host_to_hostgroup(new_hostgroup, split[x].trim()) == null)
         {
            logger.fatal("Error: Could not add host '" + split[x] + "' to hostgroup (config file '"
                  + xodtemplate_config_file_name(this_hostgroup._config_file) + "', starting on line "
                  + this_hostgroup._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_hostgroup");

      return common_h.OK;
   }

   /* registers a servicegroup definition */
   public static int xodtemplate_register_servicegroup(xodtemplate_h.xodtemplate_servicegroup this_servicegroup)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_servicegroup");

      /* bail out if we shouldn't register this object */
      if (this_servicegroup.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the  service group */
      objects_h.servicegroup new_servicegroup = objects.add_servicegroup(this_servicegroup.servicegroup_name,
            this_servicegroup.alias);

      /* return with an error if we couldn't add the servicegroup */
      if (new_servicegroup == null)
      {
         logger.fatal("Error: Could not register servicegroup (config file '"
               + xodtemplate_config_file_name(this_servicegroup._config_file) + "', starting on line "
               + this_servicegroup._start_line + ")");
         return common_h.ERROR;
      }

      /* add all members to servicegroup */
      if (this_servicegroup.members == null)
      {
         logger.fatal("Error: Servicegroup has no members (config file '"
               + xodtemplate_config_file_name(this_servicegroup._config_file) + "', starting on line "
               + this_servicegroup._start_line + ")");
         return common_h.ERROR;
      }

      String[] split = this_servicegroup.members.split(",");
      if ((split.length % 2) != 0)
      {
         logger.fatal("Error: Missing service name in servicegroup definition (config file '"
               + xodtemplate_config_file_name(this_servicegroup._config_file) + "', starting on line "
               + this_servicegroup._start_line + ")");
         return common_h.ERROR;
      }

      for (int x = 0; x < split.length; x += 2)
      {
         String host_name = split[x].trim();
         String svc_description = split[x + 1].trim();
         if (objects.add_service_to_servicegroup(new_servicegroup, host_name, svc_description) == null)
         {
            logger.fatal("Error: Could not add service '" + svc_description + "' on host '" + host_name
                  + "' to servicegroup (config file '" + xodtemplate_config_file_name(this_servicegroup._config_file)
                  + "', starting on line " + this_servicegroup._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_servicegroup");

      return common_h.OK;
   }

   /* registers a servicedependency definition */
   public static int xodtemplate_register_servicedependency(
         xodtemplate_h.xodtemplate_servicedependency this_servicedependency)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_servicedependency");

      /* bail out if we shouldn't register this object */
      if (this_servicedependency.register_object == common_h.FALSE)
         return common_h.OK;

      /* throw a warning on servicedeps that have no options */
      if (this_servicedependency.have_notification_dependency_options == common_h.FALSE
            && this_servicedependency.have_execution_dependency_options == common_h.FALSE)
      {
         logger.fatal("Warning: Ignoring lame service dependency (config file '"
               + xodtemplate_config_file_name(this_servicedependency._config_file) + "', line "
               + this_servicedependency._start_line + ")");
         return common_h.OK;
      }

      /* add the servicedependency */
      if (this_servicedependency.have_execution_dependency_options == common_h.TRUE)
      {

         objects_h.servicedependency new_servicedependency = objects.add_service_dependency(
               this_servicedependency.dependent_host_name, this_servicedependency.dependent_service_description,
               this_servicedependency.host_name, this_servicedependency.service_description,
               common_h.EXECUTION_DEPENDENCY, this_servicedependency.inherits_parent,
               this_servicedependency.fail_execute_on_ok, this_servicedependency.fail_execute_on_warning,
               this_servicedependency.fail_execute_on_unknown, this_servicedependency.fail_execute_on_critical,
               this_servicedependency.fail_execute_on_pending);

         /* return with an error if we couldn't add the servicedependency */
         if (new_servicedependency == null)
         {
            logger.fatal("Error: Could not register service execution dependency (config file '"
                  + xodtemplate_config_file_name(this_servicedependency._config_file) + "', starting on line "
                  + this_servicedependency._start_line + ")");
            return common_h.ERROR;
         }
      }
      if (this_servicedependency.have_notification_dependency_options == common_h.TRUE)
      {

         objects_h.servicedependency new_servicedependency = objects.add_service_dependency(
               this_servicedependency.dependent_host_name, this_servicedependency.dependent_service_description,
               this_servicedependency.host_name, this_servicedependency.service_description,
               common_h.NOTIFICATION_DEPENDENCY, this_servicedependency.inherits_parent,
               this_servicedependency.fail_notify_on_ok, this_servicedependency.fail_notify_on_warning,
               this_servicedependency.fail_notify_on_unknown, this_servicedependency.fail_notify_on_critical,
               this_servicedependency.fail_notify_on_pending);

         /* return with an error if we couldn't add the servicedependency */
         if (new_servicedependency == null)
         {
            logger.fatal("Error: Could not register service notification dependency (config file '"
                  + xodtemplate_config_file_name(this_servicedependency._config_file) + "', starting on line "
                  + this_servicedependency._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_servicedependency");

      return common_h.OK;
   }

   /* registers a serviceescalation definition */
   public static int xodtemplate_register_serviceescalation(
         xodtemplate_h.xodtemplate_serviceescalation this_serviceescalation)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_serviceescalation");

      /* bail out if we shouldn't register this object */
      if (this_serviceescalation.register_object == common_h.FALSE)
         return common_h.OK;

      /* default options if none specified */
      if (this_serviceescalation.have_escalation_options == common_h.FALSE)
      {
         this_serviceescalation.escalate_on_warning = common_h.TRUE;
         this_serviceescalation.escalate_on_unknown = common_h.TRUE;
        this_serviceescalation.escalate_on_critical = common_h.TRUE;
         this_serviceescalation.escalate_on_recovery = common_h.TRUE;
      }

      /* add the serviceescalation */
      objects_h.serviceescalation new_serviceescalation = objects.add_serviceescalation(
            this_serviceescalation.host_name, this_serviceescalation.service_description,
            this_serviceescalation.first_notification, this_serviceescalation.last_notification,
            this_serviceescalation.notification_interval, this_serviceescalation.escalation_period,
            this_serviceescalation.escalate_on_warning, this_serviceescalation.escalate_on_unknown,
            this_serviceescalation.escalate_on_critical, this_serviceescalation.escalate_on_recovery);

      /* return with an error if we couldn't add the serviceescalation */
      if (new_serviceescalation == null)
      {
         logger.fatal("Error: Could not register service escalation (config file '"
               + xodtemplate_config_file_name(this_serviceescalation._config_file) + "', starting on line "
               + this_serviceescalation._start_line + ")");
         return common_h.ERROR;
      }

      /* add the contact groups */
      if (this_serviceescalation.contact_groups == null)
      {
         logger.fatal("Error: Service escalation has no contact groups (config file '"
               + xodtemplate_config_file_name(this_serviceescalation._config_file) + "', starting on line "
               + this_serviceescalation._start_line + ")");
         return common_h.ERROR;
      }

      String[] split = this_serviceescalation.contact_groups.split("[, ]");
      for (int x = 0; x < split.length; x++)
      {
         if (objects.add_contactgroup_to_serviceescalation(new_serviceescalation, split[x]) == null)
         {
            logger.fatal("Error: Could not add contactgroup '" + split[x] + "' to service escalation (config file '"
                  + xodtemplate_config_file_name(this_serviceescalation._config_file) + "', starting on line "
                  + this_serviceescalation._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_serviceescalation");

      return common_h.OK;
   }

   /* registers a contact definition */
   public static int xodtemplate_register_contact(xodtemplate_h.xodtemplate_contact this_contact)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_contact");

      /* bail out if we shouldn't register this object */
      if (this_contact.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the contact */
      objects_h.contact new_contact = objects.add_contact(this_contact.contact_name, this_contact.alias,
            this_contact.email, this_contact.pager, this_contact.address, this_contact.service_notification_period,
            this_contact.host_notification_period, this_contact.notify_on_service_recovery,
            this_contact.notify_on_service_critical, this_contact.notify_on_service_warning,
            this_contact.notify_on_service_unknown, this_contact.notify_on_service_flapping,
            this_contact.notify_on_host_recovery, this_contact.notify_on_host_down,
            this_contact.notify_on_host_unreachable, this_contact.notify_on_host_flapping);

      /* return with an error if we couldn't add the contact */
      if (new_contact == null)
      {
         logger.fatal("Error: Could not register contact (config file '"
               + xodtemplate_config_file_name(this_contact._config_file) + "', starting on line "
               + this_contact._start_line + ")");
         return common_h.ERROR;
      }

      /* add all the host notification commands */
      if (this_contact.host_notification_commands != null)
      {
         String[] split = this_contact.host_notification_commands.split("[, ]");
         for (int x = 0; x < split.length; x++)
         {
            if (objects.add_host_notification_command_to_contact(new_contact, split[x]) == null)
            {
               logger.fatal("Error: Could not add host notification command '" + split[x]
                     + "' to contact (config file '" + xodtemplate_config_file_name(this_contact._config_file)
                     + "', starting on line " + this_contact._start_line + ")");
               return common_h.ERROR;
            }
         }
      }

      /* add all the service notification commands */
      if (this_contact.service_notification_commands != null)
      {
         String[] split = this_contact.service_notification_commands.split("[, ]");
         for (int x = 0; x < split.length; x++)
         {
            if (objects.add_service_notification_command_to_contact(new_contact, split[x]) == null)
            {
               logger.fatal("Error: Could not add service notification command '" + split[x]
                     + "' to contact (config file '" + xodtemplate_config_file_name(this_contact._config_file)
                     + "', starting on line " + this_contact._start_line + ")");
               return common_h.ERROR;
            }
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_contact");

      return common_h.OK;
   }

   /* registers a host definition */
   public static int xodtemplate_register_host(xodtemplate_h.xodtemplate_host this_host)
   {

      logger.trace("entering " + cn + ".xodtemplate_register_host");

      /* bail out if we shouldn't register this object */
      if (this_host.register_object == common_h.FALSE)
         return common_h.OK;

      /* if host has no alias or address, use host name - added 3/11/05 */
      if (this_host.alias == null && this_host.host_name != null)
         this_host.alias = this_host.host_name;
      if (this_host.address == null && this_host.host_name != null)
         this_host.address = this_host.host_name;

      /* add the host definition */
      objects_h.host new_host = objects.add_host(this_host.host_name, this_host.alias, this_host.address, 
            this_host.check_period, this_host.check_interval, this_host.max_check_attempts,
            this_host.notify_on_recovery, this_host.notify_on_down, this_host.notify_on_unreachable,
            this_host.notify_on_flapping, this_host.notification_interval, this_host.notification_period,
            this_host.notifications_enabled, this_host.check_command, this_host.active_checks_enabled,
            this_host.passive_checks_enabled, this_host.event_handler, this_host.event_handler_enabled,
            this_host.flap_detection_enabled, this_host.low_flap_threshold, this_host.high_flap_threshold,
            this_host.stalk_on_up, this_host.stalk_on_down, this_host.stalk_on_unreachable,
            this_host.process_perf_data, this_host.failure_prediction_enabled, this_host.failure_prediction_options,
            this_host.check_freshness, this_host.freshness_threshold, this_host.retain_status_information,
            this_host.retain_nonstatus_information, this_host.obsess_over_host);

      logger.debug("HOST: " + this_host.host_name + ", MAXATTEMPTS: " + this_host.max_check_attempts
            + ", NOTINVERVAL: " + this_host.notification_interval);

      /* return with an error if we couldn't add the host */
      if (new_host == null)
      {
         logger.fatal("Error: Could not register host (config file '"
               + xodtemplate_config_file_name(this_host._config_file) + "', starting on line " + this_host._start_line
               + ")");
         return common_h.ERROR;
      }

      /* add the parent hosts */
      if (this_host.parents != null)
      {

         String[] split = this_host.parents.split(",");
         for (int x = 0; x < split.length; x++)
         {
            if (objects.add_parent_host_to_host(new_host, split[x]) == null)
            {
               logger.fatal("Error: Could not add parent host '" + split[x] + "' to host (config file '"
                     + xodtemplate_config_file_name(this_host._config_file) + "', starting on line "
                     + this_host._start_line + ")");
               return common_h.ERROR;
            }
         }
      }

      /* add all contact groups to the host group */
      if (this_host.contact_groups != null)
      {

         String[] split = this_host.contact_groups.split(",");
         for (int x = 0; x < split.length; x++)
         {
            if (objects.add_contactgroup_to_host(new_host, split[x]) == null)
            {
               logger.fatal("Error: Could not add contactgroup '" + split[x] + "' to host (config file '"
                     + xodtemplate_config_file_name(this_host._config_file) + "', starting on line "
                     + this_host._start_line + ")");
               return common_h.ERROR;
            }
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_host");

      return common_h.OK;
   }

   /* registers a service definition */
   public static int xodtemplate_register_service(xodtemplate_h.xodtemplate_service this_service)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_service");

      /* bail out if we shouldn't register this object */
      if (this_service.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the service */
      objects_h.service new_service = objects.add_service(this_service.host_name, this_service.service_description,
            this_service.check_period, this_service.max_check_attempts, this_service.parallelize_check,
            this_service.passive_checks_enabled, this_service.normal_check_interval, this_service.retry_check_interval,
            this_service.notification_interval, this_service.notification_period, this_service.notify_on_recovery,
            this_service.notify_on_unknown, this_service.notify_on_warning, this_service.notify_on_critical,
            this_service.notify_on_flapping, this_service.notifications_enabled, this_service.is_volatile,
            this_service.event_handler, this_service.event_handler_enabled, this_service.check_command,
            this_service.active_checks_enabled, this_service.flap_detection_enabled, this_service.low_flap_threshold,
            this_service.high_flap_threshold, this_service.stalk_on_ok, this_service.stalk_on_warning,
            this_service.stalk_on_unknown, this_service.stalk_on_critical, this_service.process_perf_data,
            this_service.failure_prediction_enabled, this_service.failure_prediction_options,
            this_service.check_freshness, this_service.freshness_threshold, this_service.retain_status_information,
            this_service.retain_nonstatus_information, this_service.obsess_over_service);

      /* return with an error if we couldn't add the service */
      if (new_service == null)
      {
         logger.fatal("Error: Could not register service (config file '"
               + xodtemplate_config_file_name(this_service._config_file) + "', starting on line "
               + this_service._start_line + ")");
         return common_h.ERROR;
      }

      /* add all the contact groups to the service */
      if (this_service.contact_groups != null)
      {

         String[] split = this_service.contact_groups.split(",");
         for (int x = 0; x < split.length; x++)
         {
            /* add this contactgroup to the service definition */
            objects_h.contactgroupsmember new_contactgroupsmember = objects.add_contactgroup_to_service(new_service,
                  split[x]);

            /* stop adding contact groups if we ran into an error */
            if (new_contactgroupsmember == null)
            {
               logger.fatal("Error: Could not add contact group '" + split[x] + "' to service (config file '"
                     + xodtemplate_config_file_name(this_service._config_file) + "', starting on line "
                     + this_service._start_line + ")");
               return common_h.ERROR;
            }
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_service");

      return common_h.OK;
   }

   /* registers a hostdependency definition */
   public static int xodtemplate_register_hostdependency(xodtemplate_h.xodtemplate_hostdependency this_hostdependency)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_hostdependency");

      /* bail out if we shouldn't register this object */
      if (this_hostdependency.register_object == common_h.FALSE)
         return common_h.OK;

      /* add the host execution dependency */
      if (this_hostdependency.have_execution_dependency_options == common_h.TRUE)
      {

         objects_h.hostdependency new_hostdependency = objects.add_host_dependency(
               this_hostdependency.dependent_host_name, this_hostdependency.host_name, common_h.EXECUTION_DEPENDENCY,
               this_hostdependency.inherits_parent, this_hostdependency.fail_execute_on_up,
               this_hostdependency.fail_execute_on_down, this_hostdependency.fail_execute_on_unreachable,
               this_hostdependency.fail_execute_on_pending);

         /* return with an error if we couldn't add the hostdependency */
         if (new_hostdependency == null)
         {
            logger.fatal("Error: Could not register host execution dependency (config file '"
                  + xodtemplate_config_file_name(this_hostdependency._config_file) + "', starting on line "
                  + this_hostdependency._start_line + ")");
            return common_h.ERROR;
         }
      }

      /* add the host notification dependency */
      if (this_hostdependency.have_notification_dependency_options == common_h.TRUE)
      {

         objects_h.hostdependency new_hostdependency = objects.add_host_dependency(
               this_hostdependency.dependent_host_name, this_hostdependency.host_name,
               common_h.NOTIFICATION_DEPENDENCY, this_hostdependency.inherits_parent,
               this_hostdependency.fail_notify_on_up, this_hostdependency.fail_notify_on_down,
               this_hostdependency.fail_notify_on_unreachable, this_hostdependency.fail_notify_on_pending);

         /* return with an error if we couldn't add the hostdependency */
         if (new_hostdependency == null)
         {
            logger.fatal("Error: Could not register host notification dependency (config file '"
                  + xodtemplate_config_file_name(this_hostdependency._config_file) + "', starting on line "
                  + this_hostdependency._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_hostdependency");

      return common_h.OK;
   }

   /* registers a hostescalation definition */
   public static int xodtemplate_register_hostescalation(xodtemplate_h.xodtemplate_hostescalation this_hostescalation)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_hostescalation");

      /* bail out if we shouldn't register this object */
      if (this_hostescalation.register_object == common_h.FALSE)
         return common_h.OK;

      /* default options if none specified */
      if (this_hostescalation.have_escalation_options == common_h.FALSE)
      {
         this_hostescalation.escalate_on_down = common_h.TRUE;
         this_hostescalation.escalate_on_unreachable = common_h.TRUE;
         this_hostescalation.escalate_on_recovery = common_h.TRUE;
      }

      /* add the hostescalation */
      objects_h.hostescalation new_hostescalation = objects.add_hostescalation(this_hostescalation.host_name,
            this_hostescalation.first_notification, this_hostescalation.last_notification,
            this_hostescalation.notification_interval, this_hostescalation.escalation_period,
            this_hostescalation.escalate_on_down, this_hostescalation.escalate_on_unreachable,
            this_hostescalation.escalate_on_recovery);

      /* return with an error if we couldn't add the hostescalation */
      if (new_hostescalation == null)
      {
         logger.fatal("Error: Could not register host escalation (config file '"
               + xodtemplate_config_file_name(this_hostescalation._config_file) + "', starting on line "
               + this_hostescalation._start_line + ")");
         return common_h.ERROR;
      }

      /* add the contact groups */
      if (this_hostescalation.contact_groups == null)
      {
         logger.fatal("Error: Host escalation has no contact groups (config file '"
               + xodtemplate_config_file_name(this_hostescalation._config_file) + "', starting on line "
               + this_hostescalation._start_line + ")");
         return common_h.ERROR;
      }

      String[] split = this_hostescalation.contact_groups.split("[, ]");
      for (int x = 0; x < split.length; x++)
      {
         if (objects.add_contactgroup_to_hostescalation(new_hostescalation, split[x]) == null)
         {
            logger.fatal("Error: Could not add contactgroup '" + split[x] + "' to host escalation (config file '"
                  + xodtemplate_config_file_name(this_hostescalation._config_file) + "', starting on line "
                  + this_hostescalation._start_line + ")");
            return common_h.ERROR;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_hostescalation");

      return common_h.OK;
   }

   /* registers a hostextinfo definition */
   public static int xodtemplate_register_hostextinfo(xodtemplate_h.xodtemplate_hostextinfo this_hostextinfo)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_hostextinfo");

      /* bail out if we shouldn't register this object */
      if (this_hostextinfo.register_object == common_h.FALSE)
         return common_h.OK;

      /* register the extended host object */
      objects_h.hostextinfo new_hostextinfo = objects.add_hostextinfo(this_hostextinfo.host_name,
            this_hostextinfo.notes, this_hostextinfo.notes_url, this_hostextinfo.action_url,
            this_hostextinfo.icon_image, this_hostextinfo.vrml_image, this_hostextinfo.statusmap_image,
            this_hostextinfo.icon_image_alt, this_hostextinfo.x_2d, this_hostextinfo.y_2d, this_hostextinfo.x_3d,
            this_hostextinfo.y_3d, this_hostextinfo.z_3d, this_hostextinfo.have_2d_coords,
            this_hostextinfo.have_3d_coords);

      /* return with an error if we couldn't add the definition */
      if (new_hostextinfo == null)
      {
         logger.fatal("Error: Could not register extended host information (config file '"
               + xodtemplate_config_file_name(this_hostextinfo._config_file) + "', starting on line "
               + this_hostextinfo._start_line + ")");
         return common_h.ERROR;
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_hostextinfo");

      return common_h.OK;
   }

   /* registers a serviceextinfo definition */
   public static int xodtemplate_register_serviceextinfo(xodtemplate_h.xodtemplate_serviceextinfo this_serviceextinfo)
   {
      logger.trace("entering " + cn + ".xodtemplate_register_serviceextinfo");

      /* bail out if we shouldn't register this object */
      if (this_serviceextinfo.register_object == common_h.FALSE)
         return common_h.OK;

      /* register the extended service object */
      objects_h.serviceextinfo new_serviceextinfo = objects.add_serviceextinfo(this_serviceextinfo.host_name,
            this_serviceextinfo.service_description, this_serviceextinfo.notes, this_serviceextinfo.notes_url,
            this_serviceextinfo.action_url, this_serviceextinfo.icon_image, this_serviceextinfo.icon_image_alt);

      /* return with an error if we couldn't add the definition */
      if (new_serviceextinfo == null)
      {
         logger.fatal("Error: Could not register extended service information (config file '"
               + xodtemplate_config_file_name(this_serviceextinfo._config_file) + "', starting on line "
               + this_serviceextinfo._start_line + ")");
         return common_h.ERROR;
      }

      logger.trace("exiting " + cn + ".xodtemplate_register_serviceextinfo");

      return common_h.OK;
   }

   /******************************************************************/
   /********************** SORTING FUNCTIONS *************************/
   /******************************************************************/

   //#ifdef NSCORE
   /* sorts all objects by name */
   public static int xodtemplate_sort_objects()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_objects");

      /* sort timeperiods */
      if (xodtemplate_sort_timeperiods() == common_h.ERROR)
         return common_h.ERROR;

      /* sort commands */
      if (xodtemplate_sort_commands() == common_h.ERROR)
         return common_h.ERROR;

      /* sort contactgroups */
      if (xodtemplate_sort_contactgroups() == common_h.ERROR)
         return common_h.ERROR;

      /* sort hostgroups */
      if (xodtemplate_sort_hostgroups() == common_h.ERROR)
         return common_h.ERROR;

      /* sort servicegroups */
      if (xodtemplate_sort_servicegroups() == common_h.ERROR)
         return common_h.ERROR;

      /* sort contacts */
      if (xodtemplate_sort_contacts() == common_h.ERROR)
         return common_h.ERROR;

      /* sort hosts */
      if (xodtemplate_sort_hosts() == common_h.ERROR)
         return common_h.ERROR;

      /* sort services */
      if (xodtemplate_sort_services() == common_h.ERROR)
         return common_h.ERROR;

      /* sort service dependencies */
      if (xodtemplate_sort_servicedependencies() == common_h.ERROR)
         return common_h.ERROR;

      /* sort service escalations */
      if (xodtemplate_sort_serviceescalations() == common_h.ERROR)
         return common_h.ERROR;

      /* sort host dependencies */
      if (xodtemplate_sort_hostdependencies() == common_h.ERROR)
         return common_h.ERROR;

      /* sort hostescalations */
      if (xodtemplate_sort_hostescalations() == common_h.ERROR)
         return common_h.ERROR;

      /* sort host extended info */
      if (xodtemplate_sort_hostextinfo() == common_h.ERROR)
         return common_h.ERROR;

      /* sort service extended info */
      if (xodtemplate_sort_serviceextinfo() == common_h.ERROR)
         return common_h.ERROR;

      logger.trace("exiting " + cn + ".xodtemplate_sort_objects");

      return common_h.OK;
   }

   /* sort timeperiods by name */
   public static int xodtemplate_sort_timeperiods()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_timeperiods");

      Comparator<xodtemplate_h.xodtemplate_timeperiod> comp = new Comparator<xodtemplate_h.xodtemplate_timeperiod>()
      {
         public int compare(xodtemplate_h.xodtemplate_timeperiod a, xodtemplate_h.xodtemplate_timeperiod b)
         {
            return a.timeperiod_name.compareTo(b.timeperiod_name);
         }
      };

      java.util.Collections.sort(xodtemplate_timeperiod_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_timeperiods");
      return common_h.OK;
   }

   /* sort commands by name */
   public static int xodtemplate_sort_commands()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_commands");

      Comparator<xodtemplate_h.xodtemplate_command> comp = new Comparator<xodtemplate_h.xodtemplate_command>()
      {
         public int compare(xodtemplate_h.xodtemplate_command a, xodtemplate_h.xodtemplate_command b)
         {
            return a.command_name.compareTo(b.command_name);
         }
      };

      java.util.Collections.sort(xodtemplate_command_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_commands");
      return common_h.OK;
   }

   /* sort contactgroups by name */
   public static int xodtemplate_sort_contactgroups()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_contactgroups");

      Comparator<xodtemplate_h.xodtemplate_contactgroup> comp = new Comparator<xodtemplate_h.xodtemplate_contactgroup>()
      {
         public int compare(xodtemplate_h.xodtemplate_contactgroup a, xodtemplate_h.xodtemplate_contactgroup b)
         {
            return a.contactgroup_name.compareTo(b.contactgroup_name);
         }
      };

      java.util.Collections.sort(xodtemplate_contactgroup_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_contactgroups");
      return common_h.OK;
   }

   /* sort hostgroups by name */
   public static int xodtemplate_sort_hostgroups()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_hostgroups");

      Comparator<xodtemplate_h.xodtemplate_hostgroup> comp = new Comparator<xodtemplate_h.xodtemplate_hostgroup>()
      {
         public int compare(xodtemplate_h.xodtemplate_hostgroup a, xodtemplate_h.xodtemplate_hostgroup b)
         {
            return a.hostgroup_name.compareTo(b.hostgroup_name);
         }
      };

      java.util.Collections.sort(xodtemplate_hostgroup_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_hostgroups");
      return common_h.OK;
   }

   /* sort servicegroups by name */
   public static int xodtemplate_sort_servicegroups()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_servicegroups");

      Comparator<xodtemplate_h.xodtemplate_servicegroup> comp = new Comparator<xodtemplate_h.xodtemplate_servicegroup>()
      {
         public int compare(xodtemplate_h.xodtemplate_servicegroup a, xodtemplate_h.xodtemplate_servicegroup b)
         {
            return a.servicegroup_name.compareTo(b.servicegroup_name);
         }
      };

      java.util.Collections.sort(xodtemplate_servicegroup_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_servicegroups");
      return common_h.OK;
   }

   /* sort contacts by name */
   public static int xodtemplate_sort_contacts()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_contacts");

      Comparator<xodtemplate_h.xodtemplate_contact> comp = new Comparator<xodtemplate_h.xodtemplate_contact>()
      {
         public int compare(xodtemplate_h.xodtemplate_contact a, xodtemplate_h.xodtemplate_contact b)
         {
            return a.contact_name.compareTo(b.contact_name);
         }
      };

      java.util.Collections.sort(xodtemplate_contact_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_contacts");
      return common_h.OK;
   }

   /* sort hosts by name */
   public static int xodtemplate_sort_hosts()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_hosts");

      Comparator<xodtemplate_h.xodtemplate_host> comp = new Comparator<xodtemplate_h.xodtemplate_host>()
      {
         public int compare(xodtemplate_h.xodtemplate_host a, xodtemplate_h.xodtemplate_host b)
         {
            if (a.host_name == b.host_name)
               return 0;
            if (a.host_name == null)
               return 1;
            if (b.host_name == null)
               return -1;
            return a.host_name.compareTo(b.host_name);
         }
      };

      java.util.Collections.sort(xodtemplate_host_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_hosts");
      return common_h.OK;
   }

   /* sort services by name */
   public static int xodtemplate_sort_services()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_services");

      Comparator<xodtemplate_h.xodtemplate_service> comp = new Comparator<xodtemplate_h.xodtemplate_service>()
      {
         public int compare(xodtemplate_h.xodtemplate_service a, xodtemplate_h.xodtemplate_service b)
         {
            int result;
            if (a.host_name == b.host_name)
               result = 0;
            else if (a.host_name == null)
               result = 1;
            else
               result = a.host_name.compareTo(b.host_name);
            if (result == 0)
            {
               if (a.service_description == b.service_description)
                  result = 0;
               else if (a.service_description == null)
                  result = 1;
               else
                  result = a.service_description.compareTo(b.service_description);
            }
            return result;
         }
      };

      java.util.Collections.sort(xodtemplate_service_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_services");
      return common_h.OK;
   }

   /* sort servicedependencies by name */
   public static int xodtemplate_sort_servicedependencies()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_servicedependencies");

      Comparator<xodtemplate_h.xodtemplate_servicedependency> comp = new Comparator<xodtemplate_h.xodtemplate_servicedependency>()
      {
         public int compare(xodtemplate_h.xodtemplate_servicedependency a, xodtemplate_h.xodtemplate_servicedependency b)
         {
            int result = a.host_name.compareTo(b.host_name);
            if (result == 0)
               result = a.service_description.compareTo(b.service_description);
            return result;
         }
      };

      java.util.Collections.sort(xodtemplate_servicedependency_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_servicedependencies");
      return common_h.OK;
   }

   /* sort serviceescalations by name */
   public static int xodtemplate_sort_serviceescalations()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_serviceescalations");

      Comparator<xodtemplate_h.xodtemplate_serviceescalation> comp = new Comparator<xodtemplate_h.xodtemplate_serviceescalation>()
      {
         public int compare(xodtemplate_h.xodtemplate_serviceescalation a, xodtemplate_h.xodtemplate_serviceescalation b)
         {
            int result = a.host_name.compareTo(b.host_name);
            if (result == 0)
               result = a.service_description.compareTo(b.service_description);
            return result;
         }
      };

      java.util.Collections.sort(xodtemplate_serviceescalation_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_serviceescalations");
      return common_h.OK;
   }

   /* sort hostescalations by name */
   public static int xodtemplate_sort_hostescalations()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_hostescalations");

      Comparator<xodtemplate_h.xodtemplate_hostescalation> comp = new Comparator<xodtemplate_h.xodtemplate_hostescalation>()
      {
         public int compare(xodtemplate_h.xodtemplate_hostescalation a, xodtemplate_h.xodtemplate_hostescalation b)
         {
            return a.host_name.compareTo(b.host_name);
         }
      };

      java.util.Collections.sort(xodtemplate_hostescalation_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_serviceescalations");
      return common_h.OK;
   }

   /* sort hostdependencies by name */
   public static int xodtemplate_sort_hostdependencies()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_hostdependencies");

      Comparator<xodtemplate_h.xodtemplate_hostdependency> comp = new Comparator<xodtemplate_h.xodtemplate_hostdependency>()
      {
         public int compare(xodtemplate_h.xodtemplate_hostdependency a, xodtemplate_h.xodtemplate_hostdependency b)
         {
            return a.host_name.compareTo(b.host_name);
         }
      };

      java.util.Collections.sort(xodtemplate_hostdependency_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_hostdependencies");
      return common_h.OK;
   }

   /* sort extended host info by name */
   public static int xodtemplate_sort_hostextinfo()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_hostextinfo");

      Comparator<xodtemplate_h.xodtemplate_hostextinfo> comp = new Comparator<xodtemplate_h.xodtemplate_hostextinfo>()
      {
         public int compare(xodtemplate_h.xodtemplate_hostextinfo a, xodtemplate_h.xodtemplate_hostextinfo b)
         {
            return a.host_name.compareTo(b.host_name);
         }
      };

      java.util.Collections.sort(xodtemplate_hostextinfo_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_hostextinfo");
      return common_h.OK;
   }

   /* sort extended service info by name */
   public static int xodtemplate_sort_serviceextinfo()
   {
      logger.trace("entering " + cn + ".xodtemplate_sort_serviceextinfo");

      Comparator<xodtemplate_h.xodtemplate_serviceextinfo> comp = new Comparator<xodtemplate_h.xodtemplate_serviceextinfo>()
      {
         public int compare(xodtemplate_h.xodtemplate_serviceextinfo a, xodtemplate_h.xodtemplate_serviceextinfo b)
         {
            int result = a.host_name.compareTo(b.host_name);
            if (result == 0)
               result = a.service_description.compareTo(b.service_description);
            return result;
         }
      };

      java.util.Collections.sort(xodtemplate_serviceextinfo_list, comp);

      logger.trace("exiting " + cn + ".xodtemplate_sort_serviceextinfo");
      return common_h.OK;
   }

   /******************************************************************/
   /*********************** CACHE FUNCTIONS **************************/
   /******************************************************************/

   /**
    * Method that writes all object information to a cache file. This cache file is used by the 
    * web interface to produce all information shown to the user.
    * 
    * @param = String cache_file, the name of the cache file.
    * 
    * @return int, common_h.OK if everything went to plan, common_h.ERROR otherwise.
    */

   public static int xodtemplate_cache_objects(String cache_file)
   {
      int x;
      String[] days = new String[]
      {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};

      logger.trace("entering " + cn + ".xodtemplate_cache_objects");

      /* open the cache file for writing */
      PrintWriter pw;

      try
      {
         pw = new PrintWriter(cache_file);
      }
      catch (Exception e)
      {
         logger.warn("Warning: Could not open object cache file '" + cache_file + "' for writing!", e);
         return common_h.ERROR;
      }

      /* write header to cache file */
      pw.println("########################################");
      pw.println("#       BLUE OBJECT CACHE FILE");
      pw.println("#");
      pw.println("# THIS FILE IS AUTOMATICALLY GENERATED");
      pw.println("# BY BLUE.  DO NOT MODIFY THIS FILE!");
      pw.println("#");
      pw.println("# Created: " + DateFormat.getDateInstance().format(new Date()));
      pw.println("########################################");
      pw.println();

      /* cache timeperiods */
      for ( xodtemplate_h.xodtemplate_timeperiod temp_timeperiod : xodtemplate_timeperiod_list ) {

         if (temp_timeperiod.register_object == common_h.FALSE)
            continue;
         pw.println("define timeperiod {");

         if (temp_timeperiod.timeperiod_name != null)
            pw.println("\ttimeperiod_name\t" + temp_timeperiod.timeperiod_name);

         if (temp_timeperiod.alias != null)
            pw.println("\talias\t" + temp_timeperiod.alias);

         for (x = 0; x < 7; x++)
         {
            if (temp_timeperiod.timeranges[x] != null)
               pw.println("\t" + days[x] + "\t" + temp_timeperiod.timeranges[x]);
         }
         pw.println("\t}");
         pw.println();
      }

      /* cache commands */
      for (xodtemplate_h.xodtemplate_command temp_command : xodtemplate_command_list )
      {
         if (temp_command.register_object == common_h.FALSE)
            continue;
         pw.println("define command {");

         if (temp_command.command_name != null)
            pw.println("\tcommand_name\t" + temp_command.command_name);

         if (temp_command.command_line != null)
            pw.println("\tcommand_line\t" + temp_command.command_line);

         pw.println("\t}");
         pw.println();
      }

      /* cache contactgroups */
      for (xodtemplate_h.xodtemplate_contactgroup temp_contactgroup : xodtemplate_contactgroup_list )
      {
         if (temp_contactgroup.register_object == common_h.FALSE)
            continue;
         pw.println("define contactgroup {");

         if (temp_contactgroup.contactgroup_name != null)
            pw.println("\tcontactgroup_name\t" + temp_contactgroup.contactgroup_name);

         if (temp_contactgroup.alias != null)
            pw.println("\talias\t" + temp_contactgroup.alias);

         if (temp_contactgroup.members != null)
            pw.println("\tmembers\t" + temp_contactgroup.members);
         pw.println("\t}");
         pw.println();
      }

      /* cache hostgroups */
      for (xodtemplate_h.xodtemplate_hostgroup temp_hostgroup :xodtemplate_hostgroup_list )
      {
         if (temp_hostgroup.register_object == common_h.FALSE)
            continue;
         pw.println("define hostgroup {");

         if (temp_hostgroup.hostgroup_name != null)
            pw.println("\thostgroup_name\t" + temp_hostgroup.hostgroup_name);

         if (temp_hostgroup.alias != null)
            pw.println("\talias\t" + temp_hostgroup.alias);

         if (temp_hostgroup.members != null)
            pw.println("\tmembers\t" + temp_hostgroup.members);
         pw.println("\t}");
         pw.println();
      }

      /* cache servicegroups */
      for (xodtemplate_h.xodtemplate_servicegroup temp_servicegroup : xodtemplate_servicegroup_list )
      {
         if (temp_servicegroup.register_object == common_h.FALSE)
            continue;
         pw.println("define servicegroup {");

         if (temp_servicegroup.servicegroup_name != null)
            pw.println("\tservicegroup_name\t" + temp_servicegroup.servicegroup_name);

         if (temp_servicegroup.alias != null)
            pw.println("\talias\t" + temp_servicegroup.alias);

         if (temp_servicegroup.members != null)
            pw.println("\tmembers\t" + temp_servicegroup.members);
         pw.println("\t}");
         pw.println();
      }

      /* cache contacts */
      for (xodtemplate_h.xodtemplate_contact temp_contact : xodtemplate_contact_list )
      {
         if (temp_contact.register_object == common_h.FALSE)
            continue;
         pw.println("define contact {");
         if (temp_contact.contact_name != null)
            pw.println("\tcontact_name\t" + temp_contact.contact_name);
         if (temp_contact.alias != null)
            pw.println("\talias\t" + temp_contact.alias);
         if (temp_contact.service_notification_period != null)
            pw.println("\tservice_notification_period\t" + temp_contact.service_notification_period);
         if (temp_contact.host_notification_period != null)
            pw.println("\thost_notification_period\t" + temp_contact.host_notification_period);
         pw.print("\tservice_notification_options\t");
         x = 0;
         if (temp_contact.notify_on_service_warning == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "w");
         if (temp_contact.notify_on_service_unknown == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "u");         if (temp_contact.notify_on_service_critical == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "c");
         if (temp_contact.notify_on_service_recovery == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "r");
         if (temp_contact.notify_on_service_flapping == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "f");
         if (x == 0)
            pw.print("n");
         pw.println();
         pw.print("\thost_notification_options\t");
         x = 0;
         if (temp_contact.notify_on_host_down == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "d");
         if (temp_contact.notify_on_host_unreachable == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "u");
         if (temp_contact.notify_on_host_recovery == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "r");
         if (temp_contact.notify_on_host_flapping == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "f");
         if (x == 0)
            pw.print("n");
         pw.println();
         if (temp_contact.service_notification_commands != null)
            pw.println("\tservice_notification_commands\t" + temp_contact.service_notification_commands);
         if (temp_contact.host_notification_commands != null)
            pw.println("\thost_notification_commands\t" + temp_contact.host_notification_commands);
         if (temp_contact.email != null)
            pw.println("\temail\t" + temp_contact.email);
         if (temp_contact.pager != null)
            pw.println("\tpager\t" + temp_contact.pager);
         pw.println("\t}");
         pw.println();
      }

      /* cache hosts */
      for (xodtemplate_h.xodtemplate_host temp_host : xodtemplate_host_list )
      {
         if (temp_host.register_object == common_h.FALSE)
            continue;
         pw.println("define host {");
         if (temp_host.host_name != null)
            pw.println("\thost_name\t" + temp_host.host_name);
         if (temp_host.alias != null)
            pw.println("\talias\t" + temp_host.alias);
         if (temp_host.address != null)
            pw.println("\taddress\t" + temp_host.address);
         if (temp_host.parents != null)
            pw.println("\tparents\t" + temp_host.parents);
         if (temp_host.check_period != null)
            pw.println("\tcheck_period\t" + temp_host.check_period);
         if (temp_host.check_command != null)
            pw.println("\tcheck_command\t" + temp_host.check_command);
         if (temp_host.event_handler != null)
            pw.println("\tevent_handler\t" + temp_host.event_handler);
         if (temp_host.contact_groups != null)
            pw.println("\tcontact_groups\t" + temp_host.contact_groups);
         if (temp_host.notification_period != null)
            pw.println("\tnotification_period\t" + temp_host.notification_period);
         if (temp_host.failure_prediction_options != null)
            pw.println("\tfailure_prediction_options\t" + temp_host.failure_prediction_options);
         pw.println("\tcheck_interval\t" + temp_host.check_interval);
         pw.println("\tmax_check_attempts\t" + temp_host.max_check_attempts);
         pw.println("\tactive_checks_enabled\t" + temp_host.active_checks_enabled);
         pw.println("\tpassive_checks_enabled\t" + temp_host.passive_checks_enabled);
         pw.println("\tobsess_over_host\t" + temp_host.obsess_over_host);
         pw.println("\tevent_handler_enabled\t" + temp_host.event_handler_enabled);
         pw.println("\tlow_flap_threshold\t" + temp_host.low_flap_threshold);
         pw.println("\thigh_flap_threshold\t" + temp_host.high_flap_threshold);
         pw.println("\tflap_detection_enabled\t" + temp_host.flap_detection_enabled);
         pw.println("\tfreshness_threshold\t" + temp_host.freshness_threshold);
         pw.println("\tcheck_freshness\t" + temp_host.check_freshness);
         pw.print("\tnotification_options\t");
         x = 0;
         if (temp_host.notify_on_down == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "d");
         if (temp_host.notify_on_unreachable == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "u");
         if (temp_host.notify_on_recovery == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "r");
         if (temp_host.notify_on_flapping == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "f");
         if (x == 0)
            pw.print("n");
         pw.println();
         pw.println("\tnotifications_enabled\t" + temp_host.notifications_enabled);
         pw.println("\tnotification_interval\t" + temp_host.notification_interval);
         pw.print("\tstalking_options\t");
         x = 0;
         if (temp_host.stalk_on_up == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "o");
         if (temp_host.stalk_on_down == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "d");
         if (temp_host.stalk_on_unreachable == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "u");
         if (x == 0)
            pw.print("n");
         pw.println();
         pw.println("\tprocess_perf_data\t" + temp_host.process_perf_data);
         pw.println("\tfailure_prediction_enabled\t" + temp_host.failure_prediction_enabled);
         pw.println("\tretain_status_information\t" + temp_host.retain_status_information);
         pw.println("\tretain_nonstatus_information\t" + temp_host.retain_nonstatus_information);

         pw.println("\t}");
         pw.println();
      }

      /* cache services */
      for (xodtemplate_h.xodtemplate_service temp_service : xodtemplate_service_list )
      {
         if (temp_service.register_object == common_h.FALSE)
            continue;
         pw.println("define service {");
         if (temp_service.host_name != null)
            pw.println("\thost_name\t" + temp_service.host_name);
         if (temp_service.service_description != null)
            pw.println("\tservice_description\t" + temp_service.service_description);
         if (temp_service.check_period != null)
            pw.println("\tcheck_period\t" + temp_service.check_period);
         if (temp_service.check_command != null)
            pw.println("\tcheck_command\t" + temp_service.check_command);
         if (temp_service.event_handler != null)
            pw.println("\tevent_handler\t" + temp_service.event_handler);
         if (temp_service.contact_groups != null)
            pw.println("\tcontact_groups\t" + temp_service.contact_groups);
         if (temp_service.notification_period != null)
            pw.println("\tnotification_period\t" + temp_service.notification_period);
         if (temp_service.failure_prediction_options != null)
            pw.println("\tfailure_prediction_options\t" + temp_service.failure_prediction_options);
         pw.println("\tnormal_check_interval\t" + temp_service.normal_check_interval);
         pw.println("\tretry_check_interval\t" + temp_service.retry_check_interval);
         pw.println("\tmax_check_attempts\t" + temp_service.max_check_attempts);
         pw.println("\tis_volatile\t" + temp_service.is_volatile);
         pw.println("\tparallelize_check\t" + temp_service.parallelize_check);
         pw.println("\tactive_checks_enabled\t" + temp_service.active_checks_enabled);
         pw.println("\tpassive_checks_enabled\t" + temp_service.passive_checks_enabled);
         pw.println("\tobsess_over_service\t" + temp_service.obsess_over_service);
         pw.println("\tevent_handler_enabled\t" + temp_service.event_handler_enabled);
         pw.println("\tlow_flap_threshold\t" + temp_service.low_flap_threshold);
         pw.println("\thigh_flap_threshold\t" + temp_service.high_flap_threshold);
         pw.println("\tflap_detection_enabled\t" + temp_service.flap_detection_enabled);
         pw.println("\tfreshness_threshold\t" + temp_service.freshness_threshold);
         pw.println("\tcheck_freshness\t" + temp_service.check_freshness);
         pw.print("\tnotification_options\t");
         x = 0;
         if (temp_service.notify_on_unknown == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "u");
         if (temp_service.notify_on_warning == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "w");
         if (temp_service.notify_on_critical == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "c");
         if (temp_service.notify_on_recovery == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "r");
         if (temp_service.notify_on_flapping == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "f");
         if (x == 0)
            pw.print("n");
         pw.println();
         pw.println("\tnotifications_enabled\t" + temp_service.notifications_enabled);
         pw.println("\tnotification_interval\t" + temp_service.notification_interval);
         pw.print("\tstalking_options\t");
         x = 0;
         if (temp_service.stalk_on_ok == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "o");
         if (temp_service.stalk_on_unknown == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "u");
         if (temp_service.stalk_on_warning == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "w");
         if (temp_service.stalk_on_critical == common_h.TRUE)
            pw.print(((x++ > 0) ? "," : "") + "c");
         if (x == 0)
            pw.print("n");
         pw.println();
         pw.println("\tprocess_perf_data\t" + temp_service.process_perf_data);
         pw.println("\tfailure_prediction_enabled\t" + temp_service.failure_prediction_enabled);
         pw.println("\tretain_status_information\t" + temp_service.retain_status_information);
         pw.println("\tretain_nonstatus_information\t" + temp_service.retain_nonstatus_information);

         pw.println("\t}");
         pw.println();
      }

      /* cache service dependencies */
      for (xodtemplate_h.xodtemplate_servicedependency temp_servicedependency : xodtemplate_servicedependency_list )
      {
         if (temp_servicedependency.register_object == common_h.FALSE)
            continue;
         pw.println("define servicedependency {");
         if (temp_servicedependency.host_name != null)
            pw.println("\thost_name\t" + temp_servicedependency.host_name);
         if (temp_servicedependency.service_description != null)
            pw.println("\tservice_description\t" + temp_servicedependency.service_description);
         if (temp_servicedependency.dependent_host_name != null)
            pw.println("\tdependent_host_name\t" + temp_servicedependency.dependent_host_name);
         if (temp_servicedependency.dependent_service_description != null)
            pw.println("\tdependent_service_description\t" + temp_servicedependency.dependent_service_description);
         pw.println("\tinherits_parent\t" + temp_servicedependency.inherits_parent);
         if (temp_servicedependency.have_notification_dependency_options == common_h.TRUE)
         {
            pw.print("\tnotification_failure_options\t");
            x = 0;
            if (temp_servicedependency.fail_notify_on_ok == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "o");
            if (temp_servicedependency.fail_notify_on_unknown == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "u");
            if (temp_servicedependency.fail_notify_on_warning == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "w");
            if (temp_servicedependency.fail_notify_on_critical == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "c");
            if (temp_servicedependency.fail_notify_on_pending == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "p");
            if (x == 0)
               pw.print("n");
            pw.println();
         }
         if (temp_servicedependency.have_execution_dependency_options == common_h.TRUE)
         {
            pw.print("\texecution_failure_options\t");
            x = 0;
            if (temp_servicedependency.fail_execute_on_ok == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "o");
            if (temp_servicedependency.fail_execute_on_unknown == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "u");
            if (temp_servicedependency.fail_execute_on_warning == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "w");
            if (temp_servicedependency.fail_execute_on_critical == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "c");
            if (temp_servicedependency.fail_execute_on_pending == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "p");
            if (x == 0)
               pw.print("n");
            pw.println();
         }
         pw.println("\t}");
         pw.println();
      }

      /* cache service escalations */
      for (xodtemplate_h.xodtemplate_serviceescalation temp_serviceescalation : xodtemplate_serviceescalation_list )
      {
         if (temp_serviceescalation.register_object == common_h.FALSE)
            continue;
         pw.println("define serviceescalation {");
         if (temp_serviceescalation.host_name != null)
            pw.println("\thost_name\t" + temp_serviceescalation.host_name);
         if (temp_serviceescalation.service_description != null)
            pw.println("\tservice_description\t" + temp_serviceescalation.service_description);
         pw.println("\tfirst_notification\t" + temp_serviceescalation.first_notification);
         pw.println("\tlast_notification\t" + temp_serviceescalation.last_notification);
         pw.println("\tnotification_interval\t" + temp_serviceescalation.notification_interval);
         if (temp_serviceescalation.escalation_period != null)
            pw.println("\tescalation_period\t" + temp_serviceescalation.escalation_period);
         if (temp_serviceescalation.have_escalation_options == common_h.TRUE)
         {
            pw.print("\tescalation_options\t");
            x = 0;
            if (temp_serviceescalation.escalate_on_warning == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "w");
            if (temp_serviceescalation.escalate_on_unknown == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "u");
            if (temp_serviceescalation.escalate_on_critical == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "c");
            if (temp_serviceescalation.escalate_on_recovery == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "r");
            if (x == 0)
               pw.print("n");
            pw.println();
         }
         if (temp_serviceescalation.contact_groups != null)
            pw.println("\tcontact_groups\t" + temp_serviceescalation.contact_groups);
         pw.println("\t}");
         pw.println();
      }

      /* cache host dependencies */
      for (xodtemplate_h.xodtemplate_hostdependency temp_hostdependency : xodtemplate_hostdependency_list )
      {
         if (temp_hostdependency.register_object == common_h.FALSE)
            continue;
         pw.println("define hostdependency {");
         if (temp_hostdependency.host_name != null)
            pw.println("\thost_name\t" + temp_hostdependency.host_name);
         if (temp_hostdependency.dependent_host_name != null)
            pw.println("\tdependent_host_name\t" + temp_hostdependency.dependent_host_name);
         pw.println("\tinherits_parent\t" + temp_hostdependency.inherits_parent);
         if (temp_hostdependency.have_notification_dependency_options == common_h.TRUE)
         {
            pw.print("\tnotification_failure_options\t");
            x = 0;
            if (temp_hostdependency.fail_notify_on_up == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "o");
            if (temp_hostdependency.fail_notify_on_down == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "d");
            if (temp_hostdependency.fail_notify_on_unreachable == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "u");
            if (temp_hostdependency.fail_notify_on_pending == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "p");
            if (x == 0)
               pw.print("n");
            pw.println();
         }
         if (temp_hostdependency.have_execution_dependency_options == common_h.TRUE)
         {
            pw.print("\texecution_failure_options\t");
            x = 0;
            if (temp_hostdependency.fail_execute_on_up == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "o");
            if (temp_hostdependency.fail_execute_on_down == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "d");
            if (temp_hostdependency.fail_execute_on_unreachable == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "u");
            if (temp_hostdependency.fail_execute_on_pending == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "p");
            if (x == 0)
               pw.print("n");
            pw.println();
         }
         pw.println("\t}");
         pw.println();
      }

      /* cache host escalations */
      for (xodtemplate_h.xodtemplate_hostescalation temp_hostescalation : xodtemplate_hostescalation_list )
      {
         if (temp_hostescalation.register_object == common_h.FALSE)
            continue;
         pw.println("define hostescalation {");
         if (temp_hostescalation.host_name != null)
            pw.println("\thost_name\t" + temp_hostescalation.host_name);
         pw.println("\tfirst_notification\t" + temp_hostescalation.first_notification);
         pw.println("\tlast_notification\t" + temp_hostescalation.last_notification);
         pw.println("\tnotification_interval\t" + temp_hostescalation.notification_interval);
         if (temp_hostescalation.escalation_period != null)
            pw.println("\tescalation_period\t" + temp_hostescalation.escalation_period);
         if (temp_hostescalation.have_escalation_options == common_h.TRUE)
         {
            pw.print("\tescalation_options\t");
            x = 0;
            if (temp_hostescalation.escalate_on_down == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "d");
            if (temp_hostescalation.escalate_on_unreachable == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "u");
            if (temp_hostescalation.escalate_on_recovery == common_h.TRUE)
               pw.print(((x++ > 0) ? "," : "") + "r");
            if (x == 0)
               pw.print("n");
            pw.println();
         }
         if (temp_hostescalation.contact_groups != null)
            pw.println("\tcontact_groups\t" + temp_hostescalation.contact_groups);
         pw.println("\t}");
         pw.println();
      }

      /* cache host extended info */
      for (xodtemplate_h.xodtemplate_hostextinfo temp_hostextinfo : xodtemplate_hostextinfo_list )
      {
         if (temp_hostextinfo.register_object == common_h.FALSE)
            continue;
         pw.println("define hostextinfo {");
         if (temp_hostextinfo.host_name != null)
            pw.println("\thost_name\t" + temp_hostextinfo.host_name);
         if (temp_hostextinfo.icon_image != null)
            pw.println("\ticon_image\t" + temp_hostextinfo.icon_image);
         if (temp_hostextinfo.icon_image_alt != null)
            pw.println("\ticon_image_alt\t" + temp_hostextinfo.icon_image_alt);
         if (temp_hostextinfo.vrml_image != null)
            pw.println("\tvrml_image\t" + temp_hostextinfo.vrml_image);
         if (temp_hostextinfo.statusmap_image != null)
            pw.println("\tstatusmap_image\t" + temp_hostextinfo.statusmap_image);
         if (temp_hostextinfo.have_2d_coords == common_h.TRUE)
            pw.println("\t2d_coords\t" + temp_hostextinfo.x_2d + "," + temp_hostextinfo.y_2d);
         if (temp_hostextinfo.have_3d_coords == common_h.TRUE)
            pw.println("\t3d_coords\t" + temp_hostextinfo.x_3d + "," + temp_hostextinfo.y_3d + ","
                  + temp_hostextinfo.z_3d);
         if (temp_hostextinfo.notes != null)
            pw.println("\tnotes\t" + temp_hostextinfo.notes);
         if (temp_hostextinfo.notes_url != null)
            pw.println("\tnotes_url\t" + temp_hostextinfo.notes_url);
         if (temp_hostextinfo.action_url != null)
            pw.println("\taction_url\t" + temp_hostextinfo.action_url);
         pw.println("\t}");
         pw.println();
      }

      /* cache service extended info */
      for (xodtemplate_h.xodtemplate_serviceextinfo temp_serviceextinfo : xodtemplate_serviceextinfo_list )
      {
         if (temp_serviceextinfo.register_object == common_h.FALSE)
            continue;
         pw.println("define serviceextinfo {");
         if (temp_serviceextinfo.host_name != null)
            pw.println("\thost_name\t" + temp_serviceextinfo.host_name);
         if (temp_serviceextinfo.service_description != null)
            pw.println("\tservice_description\t" + temp_serviceextinfo.service_description);
         if (temp_serviceextinfo.icon_image != null)
            pw.println("\ticon_image\t" + temp_serviceextinfo.icon_image);
         if (temp_serviceextinfo.icon_image_alt != null)
            pw.println("\ticon_image_alt\t" + temp_serviceextinfo.icon_image_alt);
         if (temp_serviceextinfo.notes != null)
            pw.println("\tnotes\t" + temp_serviceextinfo.notes);
         if (temp_serviceextinfo.notes_url != null)
            pw.println("\tnotes_url\t" + temp_serviceextinfo.notes_url);
         if (temp_serviceextinfo.action_url != null)
            pw.println("\taction_url\t" + temp_serviceextinfo.action_url);
         pw.println("\t}");
         pw.println();
      }

      pw.close();

      logger.trace("exiting " + cn + ".xodtemplate_cache_objects");
      return common_h.OK;
   }

   ///******************************************************************/
   ///********************** CLEANUP FUNCTIONS *************************/
   ///******************************************************************/

   /**
    * Method that simply frees up memory utilised by all configuration objects.
    * 
    * @return = int, common_h.OK if the operation was successful.
    */

   public static int xodtemplate_free_memory()
   {
      logger.trace("entering " + cn + ".xodtemplate_free_memory");

      /* free memory allocated to timeperiod list */
      xodtemplate_timeperiod_list = null;

      /* free memory allocated to command list */
      xodtemplate_command_list = null;

      /* free memory allocated to contactgroup list */
      xodtemplate_contactgroup_list = null;

      /* free memory allocated to hostgroup list */
      xodtemplate_hostgroup_list = null;

      /* free memory allocated to servicegroup list */
      xodtemplate_servicegroup_list = null;

      /* free memory allocated to servicedependency list */
      xodtemplate_servicedependency_list = null;

      /* free memory allocated to serviceescalation list */
      xodtemplate_serviceescalation_list = null;

      /* free memory allocated to contact list */
      xodtemplate_contact_list = null;

      /* free memory allocated to host list */
      xodtemplate_host_list = null;

      /* free memory allocated to service list (chained hash) */
      xodtemplate_service_list = null;

      /* free memory allocated to hostdependency list */
      xodtemplate_hostdependency_list = null;

      /* free memory allocated to hostescalation list */
      xodtemplate_hostescalation_list = null;

      /* free memory allocated to hostextinfo list */
      xodtemplate_hostextinfo_list = null;

      /* free memory allocated to serviceextinfo list */
      xodtemplate_serviceextinfo_list = null;

      /* free memory for the config file names */
      xodtemplate_config_files.clear();
      xodtemplate_current_config_file = 0;

      logger.trace("exiting " + cn + ".xodtemplate_free_memory");
      return common_h.OK;
   }

   ///******************************************************************/
   ///********************** UTILITY FUNCTIONS *************************/
   ///******************************************************************/

   /**
    * Method that takes a comma seperated string of contacts and returns an ArrayList
    * of seperated contacts. Removes those who should not be on the contact list for a
    * certain group by calling xodtemplate_expand_contacts2() method.
    * 
    *  @param = String contacts, comma seperated list of contacts.
    *  
    *  @return = ArrayList, ArrayList with a contact as each of its elements.
    */

   public static ArrayList xodtemplate_expand_contacts(String contacts)
   {
      ArrayList temp_list = new ArrayList();
      ArrayList reject_list = new ArrayList();

      logger.trace("entering " + cn + ".xodtemplate_expand_contacts");

      /* process contact names */
      if (contacts != null)
      {

         /* expand contacts */
         if (xodtemplate_expand_contacts2(temp_list, reject_list, contacts) != common_h.OK)
            return null;

         /* remove rejects (if any) from the list (no duplicate entries exist in either list) */
         temp_list.removeAll(reject_list);
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_contacts");
      return temp_list;
   }

   /**
    * A method called by xodtemplate_expand_contacts(). This method expands the contacts for a contact group.
    * It then builds a list of who should be included in that contact group and who should. Those not allowed within
    * the contact group are added to the reject_list (i.e. !rob).
    * 
    * @param list
    * @param reject_list
    * @param contacts
    * 
    * @return, int common_h.OK if everything went to plan, common_h.ERROR otherwise.
    */

   //TODO - Can this method be combined into the xodtemplate_expand_contacts method?
   public static int xodtemplate_expand_contacts2(ArrayList list, ArrayList reject_list, String contacts)
   {
      xodtemplate_h.xodtemplate_contact temp_contact;
      boolean found_match = true;
      int use_regexp = common_h.FALSE;

      logger.trace("entering " + cn + ".xodtemplate_expand_contacts2");

      /* Verify that we have good variables */
      if (list == null || reject_list == null || contacts == null)
         return common_h.ERROR;

      /* expand each contact name */
      String[] split = contacts.split(",");

      for (int i = 0; i < split.length && found_match; i++)
      {

         /* Take the first contact name in the list */
         String temp_ptr = split[i].trim();

         found_match = false;

         /* should we use regular expression matching? */
         if (blue.use_regexp_matches == common_h.TRUE
               && (blue.use_true_regexp_matching == common_h.TRUE || temp_ptr.indexOf("*") >= 0 || temp_ptr
                     .indexOf("?") >= 0))
            use_regexp = common_h.TRUE;

         /* use regular expression matching */
         if (use_regexp == common_h.TRUE)
         {

            /* test match against all contacts */
            for (ListIterator iter = xodtemplate_contact_list.listIterator(); iter.hasNext();)
            {
               temp_contact = (xodtemplate_h.xodtemplate_contact) iter.next();

               if (temp_contact.contact_name != null)
               {
                  try
                  {
                     /* skip this contact if it did not match the expression */
                     found_match = Pattern.matches(split[i], temp_contact.contact_name);

                     /* dont' add contacts that shouldn't be registered, UPDATED 2.2  */
                     if (found_match && temp_contact.register_object == common_h.TRUE)
                        xodtemplate_add_contact_to_contactlist(list, temp_contact.contact_name);
                  }
                  catch (PatternSyntaxException psE)
                  {
                     logger.fatal("Regular Expression errro regex(" + split[0] + ") value(" + temp_contact.contact_name
                           + ")");
                     return common_h.ERROR;
                  }
               }
            }
         }

         /* use standard matching... */
         else
         {
            /* Because the member is specified as '*', we just add everyone to the contact list! */
            if (temp_ptr.equals("*"))
            {
               for (ListIterator iter = xodtemplate_contact_list.listIterator(); iter.hasNext();)
               {
                  temp_contact = (xodtemplate_h.xodtemplate_contact) iter.next();

                  if (temp_contact.contact_name != null)
                  {
                     xodtemplate_add_contact_to_contactlist(list, temp_contact.contact_name);
                     found_match = true;
                  }
               }

            }

            /* This lets us remove those who we do not want in the contact list i.e. !rob */

            else if (temp_ptr.startsWith("!"))
            {
               temp_ptr = temp_ptr.substring(1);
               temp_contact = xodtemplate_find_real_contact(temp_ptr);

               if (temp_contact != null)
               {
                  found_match = true;
                  xodtemplate_add_contact_to_contactlist(reject_list, temp_contact.contact_name);
               }

            }

            /* Otherwise we are simply dealing with a single contact... */
            else
            {
               temp_contact = xodtemplate_find_real_contact(temp_ptr);

               if (temp_contact != null)
               {
                  found_match = true;
                  xodtemplate_add_contact_to_contactlist(list, temp_contact.contact_name);
               }
            }
         }

         if (!found_match)
         {
            logger.fatal("Error: Could not find any contact matching '" + temp_ptr + "'");
            break;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_contacts2()");

      if (!found_match)
         return common_h.ERROR;

      return common_h.OK;
   }

   /**
    * A simple method that adds a contact name to a contact list
    * @param Arraylist list, the list you wish to add the contact to.
    * @param String contact_name, the name of the contact you wish to add to the list.
    * 
    * @return = int, common_h.OK if the contact was successfully added, common_h.ERROR otherwise.
    */

   public static int xodtemplate_add_contact_to_contactlist(ArrayList list, String contact_name)
   {
      xodtemplate_h.xodtemplate_contactlist new_item;

      if (list == null || contact_name == null)
         return common_h.ERROR;

      new_item = new xodtemplate_h.xodtemplate_contactlist(contact_name);

      /* skip this contact if its already in the list */
      if (list.contains(new_item))
         return common_h.OK;

      /* add new item to head of list */
      list.add(0, new_item);

      return common_h.OK;
   }

   /* expands a comma-delimited list of hostgroups and/or hosts to member host names */

   public static ArrayList xodtemplate_expand_hostgroups_and_hosts(String hostgroups, String hosts)
   {
      ArrayList temp_list = new ArrayList(); // xodtemplate_hostlist 
      ArrayList reject_list = new ArrayList(); // xodtemplate_hostlist 

      logger.trace("entering " + cn + ".xodtemplate_expand_hostgroups_and_hosts");

      /* process list of hostgroups... */
      if (hostgroups != null)
      {

         /* expand host */
         if (xodtemplate_expand_hostgroups(temp_list, reject_list, hostgroups) != common_h.OK)
            return null;

         /* remove rejects (if any) from the list (no duplicate entries exist in either list) */
         temp_list.removeAll(reject_list);
         reject_list.clear();
      }

      /* process host names */
      if (hosts != null)
      {

         /* expand hosts */
         if (xodtemplate_expand_hosts(temp_list, reject_list, hosts) != common_h.OK)
            return null;

         /* remove rejects (if any) from the list (no duplicate entries exist in either list) */
         /* NOTE: rejects from this list also affect hosts generated from processing hostgroup names (see above) */
         temp_list.removeAll(reject_list);
         reject_list.clear();

      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_hostgroups_and_hosts");

      return temp_list;
   }

   /* expands hostgroups */
   public static int xodtemplate_expand_hostgroups(ArrayList list, ArrayList reject_list, String hostgroups)
   {
      xodtemplate_h.xodtemplate_hostgroup temp_hostgroup;
      boolean found_match = true;
      boolean use_regexp = false;
      logger.trace("entering " + cn + ".xodtemplate_expand_hostgroups");

      if (list == null || hostgroups == null)
         return common_h.ERROR;

      String[] split = hostgroups.split(",");
      for (int i = 0; i < split.length && found_match; i++)
      {
         String temp_ptr = split[i].trim();

         found_match = false;

         /* should we use regular expression matching? */
         if (blue.use_regexp_matches == common_h.TRUE
               && (blue.use_true_regexp_matching == common_h.TRUE || temp_ptr.startsWith("*") || temp_ptr
                     .startsWith("?")))
            use_regexp = true;
         else
            use_regexp = false;

         /* use regular expression matching */
         if (use_regexp == true)
         {

            /* test match against all hostgroup names */
            for (ListIterator iter = xodtemplate_hostgroup_list.listIterator(); iter.hasNext();)
            {
               temp_hostgroup = (xodtemplate_h.xodtemplate_hostgroup) iter.next();
               if (temp_hostgroup.hostgroup_name != null)
               {
                  try
                  {
                     /* skip this hostgroup if it did not match the expression */
                     found_match = Pattern.matches(split[i], temp_hostgroup.hostgroup_name);

                     /* dont' add hostgroups that shouldn't be registered */
                     if (temp_hostgroup.register_object == common_h.FALSE)
                        continue;

                     if (found_match)
                        xodtemplate_add_hostgroup_members_to_hostlist(list, temp_hostgroup);
                  }
                  catch (PatternSyntaxException psE)
                  {
                     logger.fatal("Regular Expression errro regex(" + split[0] + ") value("
                           + temp_hostgroup.hostgroup_name + ")");
                     return common_h.ERROR;
                  }
               }
            }
         }

         /* use standard matching... */
         else
         {

            /* return a list of all hostgroups */
            if (temp_ptr.equals("*"))
            {
               found_match = true;
               for (xodtemplate_h.xodtemplate_hostgroup iter_hostgroup : xodtemplate_hostgroup_list)
               {
                  if (iter_hostgroup.register_object != common_h.FALSE)
                     xodtemplate_add_hostgroup_members_to_hostlist(list, iter_hostgroup);
               }
            }

            /* else this is just a single hostgroup... */
            else if (temp_ptr.startsWith("!"))
            {
               temp_ptr = temp_ptr.substring(1);
               temp_hostgroup = xodtemplate_find_real_hostgroup(temp_ptr);
               if (temp_hostgroup != null)
               {
                  found_match = true;
                  xodtemplate_add_hostgroup_members_to_hostlist(reject_list, temp_hostgroup);
               }
            }

            else
            {
               temp_hostgroup = xodtemplate_find_real_hostgroup(temp_ptr);
               if (temp_hostgroup != null)
               {
                  found_match = true;
                  xodtemplate_add_hostgroup_members_to_hostlist(list, temp_hostgroup);
               }
            }
         }

         if (!found_match)
         {
            logger.fatal("Error: Could not find any hostgroup matching '" + temp_ptr + "'");
            break;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_hostgroups");

      if (!found_match)
         return common_h.ERROR;

      return common_h.OK;
   }

   /* expands hosts */
   public static int xodtemplate_expand_hosts(ArrayList /* xodtemplate_hostlist */list,
         ArrayList /* xodtemplate_hostlist */reject_list, String hosts)
   {
      xodtemplate_h.xodtemplate_host temp_host;
      boolean found_match = true;
      boolean use_regexp = false;

      logger.trace("entering " + cn + ".xodtemplate_expand_hosts");

      if (list == null || hosts == null)
         return common_h.ERROR;

      /* expand each host name */
      String[] split = hosts.split(",");
      for (int i = 0; i < split.length && found_match; i++)
      {
         String temp_ptr = split[i].trim();

         found_match = false;

         /* should we use regular expression matching? */
         if (blue.use_regexp_matches == common_h.TRUE
               && (blue.use_true_regexp_matching == common_h.TRUE || temp_ptr.equals("*") || temp_ptr.equals("?")))
            use_regexp = true;

         /* use regular expression matching */
         if (use_regexp == true)
         {

            /* test match against all hostgroup names */
            for (ListIterator iter = xodtemplate_host_list.listIterator(); iter.hasNext();)
            {
               temp_host = (xodtemplate_h.xodtemplate_host) iter.next();
               if (temp_host.host_name != null)
               {
                  try
                  {
                     /* skip this hostgroup if it did not match the expression, UPDATED 2.2 */
                     found_match = Pattern.matches(split[i], temp_host.host_name);
                     if (found_match && temp_host.register_object == common_h.TRUE)
                        xodtemplate_add_host_to_hostlist(list, temp_host.host_name);
                  }
                  catch (PatternSyntaxException psE)
                  {
                     logger
                           .fatal("Regular Expression errro regex(" + split[0] + ") value(" + temp_host.host_name + ")");
                     return common_h.ERROR;
                  }
               }
            }
         }

         /* use standard matching... */
         else
         {

            /* return a list of all hosts */
            if (temp_ptr.equals("*"))
            {
               found_match = true;
               for (xodtemplate_h.xodtemplate_host iter_host : xodtemplate_host_list)
                  if (iter_host.register_object == common_h.TRUE)
                     xodtemplate_add_host_to_hostlist(list, iter_host.host_name);
            }

            /* else this is just a single host... */
            else if (temp_ptr.startsWith("!"))
            {
               temp_ptr = temp_ptr.substring(1);
               temp_host = xodtemplate_find_real_host(temp_ptr);
               if (temp_host != null)
               {
                  found_match = true;
                  xodtemplate_add_host_to_hostlist(reject_list, temp_host.host_name);
               }
            }
            else
            {
               temp_host = xodtemplate_find_real_host(temp_ptr);
               if (temp_host != null)
               {
                  found_match = true;
                  xodtemplate_add_host_to_hostlist(list, temp_host.host_name);
               }
            }
         }

         if (!found_match)
         {
            logger.fatal("Error: Could not find any host matching '" + temp_ptr + "'");
            break;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_hosts");
      if (!found_match)
         return common_h.ERROR;

      return common_h.OK;
   }

   /* adds members of a hostgroups to the list of expanded (accepted) or rejected hosts */
   public static int xodtemplate_add_hostgroup_members_to_hostlist(ArrayList list,
         xodtemplate_h.xodtemplate_hostgroup temp_hostgroup)
   {

      if (list == null || temp_hostgroup == null)
         return common_h.ERROR;

      /* skip hostgroups with no defined members */
      if (temp_hostgroup.members == null)
         return common_h.OK;

      /* save a copy of the members */
      if (temp_hostgroup.members == null)
         return common_h.ERROR;

      /* process all hosts that belong to the hostgroup */
      /* NOTE: members of the group have already have been expanded by xodtemplate_recombobulate_hostgroups(), so we don't need to do it here */
      String[] split = temp_hostgroup.members.split(",");
      for (int i = 0; i < split.length; i++)
      {
         String temp_ptr = split[i].trim();
         xodtemplate_add_host_to_hostlist(list, temp_ptr);
      }

      return common_h.OK;
   }

   /* adds a host entry to the list of expanded (accepted) or rejected hosts */
   public static int xodtemplate_add_host_to_hostlist(ArrayList list, String host_name)
   {
      xodtemplate_h.xodtemplate_hostlist new_item;

      if (list == null || host_name == null)
         return common_h.ERROR;

      new_item = new xodtemplate_h.xodtemplate_hostlist(host_name);

      /* skip this contact if its already in the list */
      if (list.contains(new_item))
         return common_h.OK;

      /* add new item to head of list */
      list.add(0, new_item);

      return common_h.OK;

   }

   /* expands a comma-delimited list of servicegroups and/or service descriptions */
   public static ArrayList /* xodtemplate_servicelist */xodtemplate_expand_servicegroups_and_services(
         String servicegroups, String host_name, String services)
   {
      ArrayList temp_list = new ArrayList();
      ArrayList reject_list = new ArrayList();

      logger.trace("entering " + cn + ".xodtemplate_expand_servicegroups_and_services");

      /* process list of servicegroups... */
      if (servicegroups != null)
      {

         /* expand services */
         if (xodtemplate_expand_servicegroups(temp_list, reject_list, servicegroups) != common_h.OK)
            return null;

         /* remove rejects (if any) from the list (no duplicate entries exist in either list) */
         temp_list.removeAll(reject_list);
         reject_list.clear();
      }

      /* process service names */
      if (host_name != null && services != null)
      {
         /* expand services */
         if (xodtemplate_expand_services(temp_list, reject_list, host_name, services) != common_h.OK)
            return null;

         /* remove rejects (if any) from the list (no duplicate entries exist in either list) */
         /* NOTE: rejects from this list also affect hosts generated from processing hostgroup names (see above) */
         temp_list.removeAll(reject_list);
         reject_list.clear();
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_servicegroups_and_services");
      return temp_list;
   }

   /* expands servicegroups */
   public static int xodtemplate_expand_servicegroups(ArrayList /* xodtemplate_servicelist */list,
         ArrayList reject_list, String servicegroups)
   {
      xodtemplate_h.xodtemplate_servicegroup temp_servicegroup;
      boolean found_match = true;
      boolean use_regexp = false;

      logger.trace("entering " + cn + ".xodtemplate_expand_servicegroups");

      if (list == null || servicegroups == null)
         return common_h.ERROR;

      /* expand each servicegroup */
      String[] split = servicegroups.split(",");
      for (int i = 0; i < split.length && found_match; i++)
      {
         String temp_ptr = split[i].trim();
         found_match = false;

         /* should we use regular expression matching? */
         if (blue.use_regexp_matches == common_h.TRUE
               && (blue.use_true_regexp_matching == common_h.TRUE || temp_ptr.equals("*") || temp_ptr.equals("?")))
            use_regexp = true;
         else
            use_regexp = false;

         /* use regular expression matching */
         if (use_regexp == true)
         {

            for (ListIterator iter = xodtemplate_servicegroup_list.listIterator(); iter.hasNext();)
            {
               temp_servicegroup = (xodtemplate_h.xodtemplate_servicegroup) iter.next();
               if (temp_servicegroup.servicegroup_name != null)
               {
                  try
                  {
                     /* skip this hostgroup if it did not match the expression, UPDATED 2.2 */
                     found_match = Pattern.matches(split[i], temp_servicegroup.servicegroup_name);
                     if (found_match && temp_servicegroup.register_object == common_h.TRUE)
                        xodtemplate_add_servicegroup_members_to_servicelist(list, temp_servicegroup);
                  }
                  catch (PatternSyntaxException psE)
                  {
                     logger.fatal("Regular Expression errro regex(" + split[0] + ") value("
                           + temp_servicegroup.servicegroup_name + ")");
                     return common_h.ERROR;
                  }
               }
            }

         }

         /* use standard matching... */
         else
         {

            /* return a list of all servicegroups */
            if (temp_ptr.equals("*"))
            {
               found_match = true;
               for (xodtemplate_h.xodtemplate_servicegroup iter_servicegroup : xodtemplate_servicegroup_list)
                  if (iter_servicegroup.register_object == common_h.TRUE)
                     xodtemplate_add_servicegroup_members_to_servicelist(list, iter_servicegroup);
            }

            /* else this is just a single servicegroup... */
            else if (temp_ptr.startsWith("!"))
            {
               temp_ptr = temp_ptr.substring(1);
               temp_servicegroup = xodtemplate_find_real_servicegroup(temp_ptr);
               if (temp_servicegroup != null)
               {
                  found_match = true;
                  xodtemplate_add_servicegroup_members_to_servicelist(reject_list, temp_servicegroup);
               }
            }
            else
            {
               temp_servicegroup = xodtemplate_find_real_servicegroup(temp_ptr);
               if (temp_servicegroup != null)
               {
                  found_match = true;
                  xodtemplate_add_servicegroup_members_to_servicelist(list, temp_servicegroup);
               }
            }
         }

         /* we didn't find a matching servicegroup */
         if (!found_match)
         {
            logger.fatal("Error: Could not find any servicegroup matching '" + temp_ptr + "'");
            break;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_servicegroups");
      if (!found_match)
         return common_h.ERROR;

      return common_h.OK;
   }

   /* expands services (host name is not expanded) */
   public static int xodtemplate_expand_services(ArrayList /* servicelsit */list, ArrayList reject_list,
         String host_name, String services)
   {
      xodtemplate_h.xodtemplate_service temp_service;

      boolean found_match = true;
      boolean use_regexp_host = false;
      boolean use_regexp_service = false;

      logger.trace("entering " + cn + ".xodtemplate_expand_services");

      if (list == null || host_name == null || services == null)
         return common_h.ERROR;

      /* should we use regular expression matching for the host name? */
      if (blue.use_regexp_matches == common_h.TRUE
            && (blue.use_true_regexp_matching == common_h.TRUE || host_name.startsWith("*") || host_name
                  .startsWith("?")))
         use_regexp_host = true;

      /* expand each service description */
      String[] split = services.split(",");

      for (int i = 0; i < split.length && found_match; i++)
      {
         String temp_ptr = split[i].trim();

         /* should we use regular expression matching for the service description? */
         if (blue.use_regexp_matches == common_h.TRUE
               && (blue.use_true_regexp_matching == common_h.TRUE || temp_ptr.startsWith("*") || temp_ptr
                     .startsWith("?")))
            use_regexp_service = true;
         else
            use_regexp_service = false;

         /* use regular expression matching */
         if (use_regexp_host || use_regexp_service)
         {

            /* test match against all services */
            for (ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext();)
            {
               temp_service = (xodtemplate_h.xodtemplate_service) iter.next();

               if (temp_service.host_name == null || temp_service.service_description == null)
                  continue;

               /* skip this service if it doesn't match the host name expression */
               if (use_regexp_host)
               {
                  if (!Pattern.matches(host_name, temp_service.host_name))
                     continue;
               }
               else
               {
                  if (!temp_service.host_name.equals(host_name))
                     continue;
               }

               /* skip this service if it doesn't match the service description expression */
               if (use_regexp_service)
               {
                  if (!Pattern.matches(temp_ptr, temp_service.service_description))
                     continue;
               }
               else
               {
                  if (!temp_service.service_description.equals(temp_ptr))
                     continue;
               }

               found_match = true;

               /* add service to the list, UPDATED 2.2 */
               if (temp_service.register_object == common_h.TRUE)
                  xodtemplate_add_service_to_servicelist(list, host_name, temp_service.service_description);
            }

         }

         /* use standard matching... */
         else
         {

            /* return a list of all services on the host */
            if (temp_ptr.equals("*"))
            {
               found_match = true;
               for (ListIterator iter = xodtemplate_service_list.listIterator(); iter.hasNext();)
               {
                  temp_service = (xodtemplate_h.xodtemplate_service) iter.next();
                  if (temp_service.host_name == null || temp_service.service_description == null)
                     continue;
                  if (!temp_service.host_name.equals(host_name))
                     continue;

                  if (temp_service.register_object == common_h.TRUE)
                     xodtemplate_add_service_to_servicelist(list, host_name, temp_service.service_description);
               }

            }

            /* else this is just a single service... */
            /* For not this service!" */
            else if (temp_ptr.startsWith("!"))
            {
               temp_ptr = temp_ptr.substring(1);
               temp_service = xodtemplate_find_real_service(host_name, temp_ptr);

               if (temp_service != null)
               {
                  found_match = true;
                  xodtemplate_add_service_to_servicelist(reject_list, host_name, temp_service.service_description);
               }
            }
            else
            {
               temp_service = xodtemplate_find_real_service(host_name, temp_ptr);

               if (temp_service != null)
               {
                  found_match = true;
                  xodtemplate_add_service_to_servicelist(list, host_name, temp_service.service_description);
               }
            }
         }

         /* we didn't find a match */
         if (!found_match)
         {
            logger.fatal("Error: Could not find a service matching host name '" + host_name + "' and description '"
                  + temp_ptr + "'");
            break;
         }
      }

      logger.trace("exiting " + cn + ".xodtemplate_expand_services");

      if (!found_match)
         return common_h.ERROR;

      return common_h.OK;
   }

   /* adds members of a servicegroups to the list of expanded services */
   public static int xodtemplate_add_servicegroup_members_to_servicelist(ArrayList list,
         xodtemplate_h.xodtemplate_servicegroup temp_servicegroup)
   {
      String host_name = null;

      if (list == null || temp_servicegroup == null)
         return common_h.ERROR;

      /* skip servicegroups with no defined members */
      if (temp_servicegroup.members == null)
         return common_h.OK;

      /* process all services that belong to the servicegroup */
      /* NOTE: members of the group have already have been expanded by xodtemplate_recombobulate_servicegroups(), so we don't need to do it here */
      String[] split = temp_servicegroup.members.split(",");
      for (int i = 0; i < split.length; i++)
      {
         String member_name = split[i].trim();

         /* host name */
         if (host_name == null)
         {
            host_name = member_name;
         }

         /* service description */
         else
         {

            /* add service to the list */
            xodtemplate_add_service_to_servicelist(list, host_name, member_name);
            host_name = null;
         }
      }

      return common_h.OK;
   }

   /**
    * Method that adds a service to a service list. 
    * @param ArrayList list, the list you wish to add the service to!
    * @param String host_name, the host name that the service runs upon.
    * @param String description, a description of the service.
    * 
    * @return int, common_h.OK if the operation is successful, common_h.ERROR otherwise.
    */

   public static int xodtemplate_add_service_to_servicelist(ArrayList list, String host_name, String description)
   {

      if (list == null || host_name == null || description == null)
         return common_h.ERROR;

      xodtemplate_h.xodtemplate_servicelist new_item = new xodtemplate_h.xodtemplate_servicelist();
      new_item.host_name = host_name;
      new_item.service_description = description;

      /* skip this contact if its already in the list */
      if (list.contains(new_item))
         return common_h.OK;

      /* add new item to head of list */
      list.add(0, new_item);

      return common_h.OK;
   }

   /**
    * Method that returns the name of a numbered xodtemplate_config_file!
    *  
    * @param int config_file, the id of the config file you wish to retrieve the name of!
    * 
    * @return string, the name of the config file.
    */

   public static String xodtemplate_config_file_name(int config_file)
   {

      if (config_file <= xodtemplate_config_files.size())
         return (String) xodtemplate_config_files.get(config_file - 1);

      return "?";
   }

   /**
    * Private method that converts a string to an integer. 
    * 
    * @param = String value, the string you wish to convert to an int;
    *
    * @return = int, the integer value of the string.
    */

   private static int atoi(String value)
   {
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException nfE)
      {
         logger.error("warning: " + nfE.getMessage(), nfE);
         return -1;
      }
   }
}