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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;
import org.blue.star.include.downtime_h;
import org.blue.star.include.locations_h;

public class xdddefault {

    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.xdata.xdddefault");
    private static String cn = "org.blue.xdata.xdddefault";
    
    public static String xdddefault_downtime_file="";
    public static String xdddefault_temp_file="";

    public static long current_downtime_id=0;
    
/******************************************************************/
/***************** COMMON CONFIG INITIALIZATION  ******************/
/******************************************************************/

/* grab configuration information from appropriate config file(s) */
    public static int xdddefault_grab_config_info(String config_file){
        blue_h.mmapfile thefile;

	/*** CORE PASSES IN MAIN CONFIG FILE, CGIS PASS IN CGI CONFIG FILE! ***/

	/* initialize the location of the downtime and temp files */
        xdddefault_downtime_file = locations_h.DEFAULT_DOWNTIME_FILE;
        xdddefault_temp_file = locations_h.DEFAULT_TEMP_FILE;

	/* open the config file for reading */
        thefile = utils.file_functions.mmap_fopen( config_file );
        if( thefile == null )
            return common_h.ERROR;

	/* read in all lines from the config file */
        while( true ){
            
            /* read the next line */
            String input = utils.file_functions.mmap_fgets(thefile);
            if ( input == null )
                break;
            
            input = input.trim();
            
            /* skip blank lines and comments */
            if(input.length() == 0 ||  input.charAt(0) =='#' )
                continue;

            if ( blue.is_core ) {
                /* core reads variables directly from the main config file */
                xdddefault_grab_config_directives(input); 
                
            } else if ( input.startsWith("main_config_file=") ) {
                String config_file2 = input.substring( input.indexOf("=")+1 );
                /* open the config file for reading */
                blue_h.mmapfile thefile2 = utils.file_functions.mmap_fopen( config_file2 );
                if( thefile2 == null )
                    continue;
                
                /* read in all lines from the main config file */
                while( true ){
                    
                    /* read the next line */
                    String input2 = utils.file_functions.mmap_fgets(thefile2);
                    if ( input2 == null )
                        break;
                    
                    input2 = input2.trim();
                    
                    /* skip blank lines and comments */
                    if(input2.length() == 0 ||  input2.charAt(0) =='#' )
                        continue;
                    
                    /* core reads variables directly from the main config file */
                    xdddefault_grab_config_directives(input2); 
                }
                
                /* free memory and close the file */
                utils.file_functions.mmap_fclose(thefile2);
            }
        }

	/* close the file */
        utils.file_functions.mmap_fclose(thefile);

	/* we didn't find the downtime file */
        if( xdddefault_downtime_file.length() == 0 )
            return common_h.ERROR;

	/* we didn't find the temp file */
        if( xdddefault_temp_file.length() == 0 )
            return common_h.ERROR;

	if ( blue.is_core )
        /* save the downtime data file macro */
        blue.macro_x [ blue_h.MACRO_DOWNTIMEDATAFILE ] = xdddefault_downtime_file.trim();
    
	return common_h.OK;
    }


public static void xdddefault_grab_config_directives(String input_buffer){
    /* downtime file definition */
    if ( input_buffer.startsWith( "downtime_file") || input_buffer.startsWith( "xdddefault_downtime_file" ) ) {
        int index = input_buffer.indexOf( "=");
        if ( index <0 || index == input_buffer.length() )
            return;
        xdddefault_downtime_file = input_buffer.substring( index + 1);
    } 
    
    /* temp file definition */
    else if( input_buffer.startsWith( "temp_file") || input_buffer.startsWith("xdddefault_temp_file" ) )  {
        
        int index = input_buffer.indexOf( "=");
        if ( index <0 || index == input_buffer.length() )
            return;
        xdddefault_temp_file = input_buffer.substring( index );
    }

	return;	
        }

/******************************************************************/
/*********** DOWNTIME INITIALIZATION/CLEANUP FUNCTIONS ************/
/******************************************************************/


/* initialize downtime data */
public static int xdddefault_initialize_downtime_data(String main_config_file){

	/* grab configuration information */
	if(xdddefault_grab_config_info(main_config_file)==common_h.ERROR)
		return common_h.ERROR;

	/* create downtime file if necessary */
	xdddefault_create_downtime_file();

	/* read downtime data into memory */
	xdddefault_read_downtime_data(main_config_file);

	/* clean up the old downtime data */
	xdddefault_validate_downtime_data();

	return common_h.OK;
        }



/* creates an empty downtime data file if one doesn't already exist */
public static int xdddefault_create_downtime_file( ){

	/* bail out if file already exists */
    if  ( new File( xdddefault_downtime_file ).exists() )
		return common_h.OK;

	/* create an empty file */
	xdddefault_save_downtime_data();

	return common_h.OK;
        }



/* removes invalid and old downtime entries from the downtime file */
public static int xdddefault_validate_downtime_data(){
	downtime_h.scheduled_downtime temp_downtime;
	boolean update_file=false;
	boolean save=true;

	/* remove stale downtimes */
    for (ListIterator iter = downtime.scheduled_downtime_list.listIterator(); iter.hasNext(); ) { 
        temp_downtime = (downtime_h.scheduled_downtime) iter.next();

		save=true;

		/* delete downtimes with invalid host names */
		if( objects.find_host(temp_downtime.host_name)==null)
			save=false;

		/* delete downtimes with invalid service descriptions */
		if(temp_downtime.type==common_h.SERVICE_DOWNTIME && objects.find_service(temp_downtime.host_name,temp_downtime.service_description)==null)
			save=false;

		/* delete downtimes that have expired */
		if(temp_downtime.end_time< utils.currentTimeInSeconds())
			save=false;

		/* delete the downtime */
		if(save==false){
			update_file=true;
			downtime.delete_downtime(temp_downtime.type,temp_downtime.downtime_id);
		        }
	        }

	/* remove triggered downtimes without valid parents */
    for (ListIterator iter = downtime.scheduled_downtime_list.listIterator(); iter.hasNext(); ) { 
        temp_downtime = (downtime_h.scheduled_downtime) iter.next();
		save=true;

		if(temp_downtime.triggered_by==0)
			continue;

		if(downtime.find_host_downtime(temp_downtime.triggered_by)==null && downtime.find_service_downtime(temp_downtime.triggered_by)==null)
			save=false;

		/* delete the downtime */
		if(save==false){
			update_file=true;
			downtime.delete_downtime(temp_downtime.type,temp_downtime.downtime_id);
		        }
	        }

	/* update downtime file */
	if(update_file==true)
		xdddefault_save_downtime_data();

	/* reset the current downtime counter */
	current_downtime_id=0;

	/* find the new starting index for downtime id */
    for (ListIterator iter = downtime.scheduled_downtime_list.listIterator(); iter.hasNext(); ) { 
        temp_downtime = (downtime_h.scheduled_downtime) iter.next();
		if(temp_downtime.downtime_id>current_downtime_id)
			current_downtime_id=temp_downtime.downtime_id;
	        }

	return common_h.OK;
        }



/* removes invalid and old downtime entries from the downtime file */
public static int xdddefault_cleanup_downtime_data(String main_config_file){

	/* we don't need to do any cleanup... */
	return common_h.OK;
        }



/******************************************************************/
/************************ SAVE FUNCTIONS **************************/
/******************************************************************/

/* adds a new scheduled host downtime entry */
public static downtime_h.scheduled_downtime xdddefault_add_new_host_downtime(String host_name, long entry_time, String author, String comment, long start_time, long end_time, int fixed, long triggered_by, long duration ){

	/* find the next valid downtime id */
	do{
		current_downtime_id++;
		if(current_downtime_id==0)
			current_downtime_id++;
  	        }while(downtime.find_host_downtime(current_downtime_id)!=null);

	/* add downtime to list in memory */
	downtime_h.scheduled_downtime new_downtime = downtime.add_host_downtime(host_name,entry_time,author,comment,start_time,end_time,fixed,triggered_by,duration,current_downtime_id);

	/* update downtime file */
	xdddefault_save_downtime_data();

	return new_downtime;
        }



/* adds a new scheduled service downtime entry */
public static downtime_h.scheduled_downtime xdddefault_add_new_service_downtime(String host_name, String service_description, long entry_time, String author, String comment, long start_time, long end_time, int fixed, long triggered_by, long duration ){

	/* find the next valid downtime id */
	do{
		current_downtime_id++;
		if(current_downtime_id==0)
			current_downtime_id++;
  	        }while(downtime.find_service_downtime(current_downtime_id)!=null);

	/* add downtime to list in memory */
    downtime_h.scheduled_downtime new_downtime = downtime.add_service_downtime(host_name,service_description,entry_time,author,comment,start_time,end_time,fixed,triggered_by,duration,current_downtime_id);

	/* update downtime file */
	xdddefault_save_downtime_data();

	return new_downtime;
        }


/******************************************************************/
/********************** DELETION FUNCTIONS ************************/
/******************************************************************/

/* deletes a scheduled host downtime entry */
public static int xdddefault_delete_host_downtime(long downtime_id){
	int result;

	result=xdddefault_delete_downtime(common_h.HOST_DOWNTIME,downtime_id);

	return result;
        }


/* deletes a scheduled service downtime entry */
public static int xdddefault_delete_service_downtime(long downtime_id){
	int result;

	result=xdddefault_delete_downtime(common_h.SERVICE_DOWNTIME,downtime_id);

	return result;
        }


/* deletes a scheduled host or service downtime entry */
    public static int xdddefault_delete_downtime(int type, long downtime_id){

	/* rewrite the downtime file (downtime was already removed from memory) */
	xdddefault_save_downtime_data();

	return common_h.OK;
        }



/******************************************************************/
/****************** DOWNTIME OUTPUT FUNCTIONS *********************/
/******************************************************************/

/* writes downtime data to file */
public static int xdddefault_save_downtime_data(){

    /* open a safe temp file for output */
    File temp_file;
    try {
        temp_file = File.createTempFile( "downtime", "file" );
    } catch ( IOException ioE ) {
        logger.fatal( "Error: Unable to create temp file for writing status data!", ioE );
        return common_h.ERROR;
    }
    
    PrintWriter pw;
    try {
        pw = new PrintWriter ( new FileWriter ( temp_file ) );
    } catch ( IOException ioE ) {
        logger.fatal( "Error: Unable to open temp file '"+temp_file.toString() +"' for writing downtime data!", ioE );
        return common_h.ERROR;
    }


	/* write header */
	pw.println("########################################");
	pw.println("#          NAGIOS DOWNTIME FILE");
	pw.println("#");
	pw.println("# THIS FILE IS AUTOMATICALLY GENERATED");
	pw.println("# BY NAGIOS.  DO NOT MODIFY THIS FILE!");
	pw.println("########################################");
    pw.println();

    long current_time = utils.currentTimeInSeconds();

	/* write file info */
	pw.println("info {");
	pw.println("\tcreated=" + current_time);
	pw.println("\tversion=" + common_h.PROGRAM_VERSION);
	pw.println("\t}");
    pw.println();

	/* save all downtime */
	for (ListIterator iter = downtime.scheduled_downtime_list.listIterator(); iter.hasNext(); ) {
	    downtime_h.scheduled_downtime temp_downtime = (downtime_h.scheduled_downtime) iter.next();
		if(temp_downtime.type==common_h.HOST_DOWNTIME)
			pw.println("hostdowntime {");
		else
			pw.println("servicedowntime {");
		pw.println("\thost_name=" + temp_downtime.host_name);
		if(temp_downtime.type==common_h.SERVICE_DOWNTIME)
			pw.println("\tservice_description=" + temp_downtime.service_description);
		pw.println("\tdowntime_id=" + temp_downtime.downtime_id);
		pw.println("\tentry_time=" + temp_downtime.entry_time);
		pw.println("\tstart_time=" + temp_downtime.start_time);
		pw.println("\tend_time=" + temp_downtime.end_time);
		pw.println("\ttriggered_by=" + temp_downtime.triggered_by);
		pw.println("\tfixed=%d" + temp_downtime.fixed);
		pw.println("\tduration=" + temp_downtime.duration);
		pw.println("\tauthor=" + temp_downtime.author);
		pw.println("\tcomment=" + temp_downtime.comment);
		pw.println("\t}");
        pw.println();
	        }

	/* reset file permissions */
//	fchmod(fd,S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH);

	/* close the temp file */
    pw.close();

	/* move the temp file to the downtime file (overwrite the old downtime file) */
    if( utils.file_functions.my_rename( temp_file.toString(), xdddefault_downtime_file) != 0 ){
        logger.fatal( "Error: Unable to update comment data file '"+xdddefault_downtime_file+"'!");
        return common_h.ERROR;
    }
    
    temp_file.delete();

	return common_h.OK;
        }


/******************************************************************/
/****************** DOWNTIME INPUT FUNCTIONS **********************/
/******************************************************************/


/* read the downtime file */
public static int xdddefault_read_downtime_data(String main_config_file){
	String input=null;
	int data_type=xdddefault_h.XDDDEFAULT_NO_DATA;
	String var;
	String val;
	long downtime_id=0;
	long entry_time=0L;
	long start_time=0L;
	long end_time=0L;
	int fixed=common_h.FALSE;
	long triggered_by=0;
	long duration=0L;
	String host_name=null;
	String service_description=null;
	String comment=null;
	String author=null;

    if ( !blue.is_core)
        /* grab configuration information */
        if(xdddefault_grab_config_info(main_config_file)==common_h.ERROR)
            return common_h.ERROR;


	/* open the downtime file */
    blue_h.mmapfile thefile = utils.file_functions.mmap_fopen(xdddefault_downtime_file);
    if( thefile == null )
        return common_h.ERROR;

	while( true ){

        /* read the next line */
        input = utils.file_functions.mmap_fgets( thefile );
        if( input == null )
            break;
        
        input = input.trim();
        /* skip blank lines and comments */
        if( input.length() == 0 || input.charAt(0) =='#' )
            continue;

		else if(input.equals("info {"))
			data_type=xdddefault_h.XDDDEFAULT_INFO_DATA;
		else if(input.equals("hostdowntime {"))
			data_type=xdddefault_h.XDDDEFAULT_HOST_DATA;
		else if(input.equals("servicedowntime {"))
			data_type=xdddefault_h.XDDDEFAULT_SERVICE_DATA;

		else if(input.equals("}")){

			switch(data_type){

			case xdddefault_h.XDDDEFAULT_INFO_DATA:
				break;

			case xdddefault_h.XDDDEFAULT_HOST_DATA:
			case xdddefault_h.XDDDEFAULT_SERVICE_DATA:

				/* add the downtime */
                downtime_h.scheduled_downtime temp_downtime;
				if(data_type==xdddefault_h.XDDDEFAULT_HOST_DATA)
                    temp_downtime= downtime.add_host_downtime(host_name,entry_time,author,comment,start_time,end_time,fixed,triggered_by,duration,downtime_id);
				else
                    temp_downtime = downtime.add_service_downtime(host_name,service_description,entry_time,author,comment,start_time,end_time,fixed,triggered_by,duration,downtime_id);

				if (blue.is_core ) 
                    /* must register the downtime with Nagios so it can schedule it, add comments, etc. */
				    downtime.register_downtime((data_type==xdddefault_h.XDDDEFAULT_HOST_DATA)?common_h.HOST_DOWNTIME:common_h.SERVICE_DOWNTIME,temp_downtime.downtime_id);
				break;

			default:
				break;
			        }

            /* reset defaults */
			data_type=xdddefault_h.XDDDEFAULT_NO_DATA;
            
			host_name=null;
			service_description=null;
			author=null;
			comment=null;
			downtime_id=0;
			entry_time=0L;
			start_time=0L;
			end_time=0L;
			fixed=common_h.FALSE;
			triggered_by=0;
			duration=0L;
		        }

		else if(data_type!=xdddefault_h.XDDDEFAULT_NO_DATA){

            String[] split = input.split( "=", 2 );
            if ( split.length != 2 )
                continue;
            var = split[0];
            val = split[1];

			switch(data_type){

			case xdddefault_h.XDDDEFAULT_INFO_DATA:
				break;

			case xdddefault_h.XDDDEFAULT_HOST_DATA:
			case xdddefault_h.XDDDEFAULT_SERVICE_DATA:
				if(var.equals("host_name"))
					host_name=val;
				else if(var.equals("service_description"))
					service_description=val;
				else if(var.equals("downtime_id"))
					downtime_id=strtoul(val,null,10);
				else if(var.equals("entry_time"))
					entry_time=strtoul(val,null,10);
				else if(var.equals("start_time"))
					start_time=strtoul(val,null,10);
				else if(var.equals("end_time"))
					end_time=strtoul(val,null,10);
				else if(var.equals("fixed"))
					fixed=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
				else if(var.equals("triggered_by"))
					triggered_by=strtoul(val,null,10);
				else if(var.equals("duration"))
					duration=strtoul(val,null,10);
				else if(var.equals("author"))
					author=val;
				else if(var.equals("comment"))
					comment=val;
				break;

			default:
				break;
			        }

		        }
	        }

    /* free memory and close the file */
    utils.file_functions.mmap_fclose(thefile);
    
    return common_h.OK;
        }
private static int atoi(String value) {
    try {
        return Integer.parseInt(value);
    } catch ( NumberFormatException nfE ) {
       logger.error( "warning: " + nfE.getMessage(), nfE);
        return 0;
    }
}

private static long strtoul(String value, Object ignore, int base ) {
    try {
        return Long.parseLong(value);
    } catch ( NumberFormatException nfE ) {
       logger.error( "warning: " + nfE.getMessage(), nfE);
        return 0L;
    }
}


}