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

/*********** COMMON HEADER FILES ***********/
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.comments;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.include.locations_h;


public class xcddefault {
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.xdata.xcddefault");
    private static String cn = "org.blue.xdata.xcddefault";
    
    public static String xcddefault_comment_file="";
    public static String xcddefault_temp_file="";
    
    public static long current_comment_id=0;
    public static boolean processing_main_config_file = false;
    
    
    /******************************************************************/
    /***************** COMMON CONFIG INITIALIZATION  ******************/
    /******************************************************************/
    
    /* grab configuration information from appropriate config file(s) */
    public static int xcddefault_grab_config_info(String config_file)
    {
        /*** CORE PASSES IN MAIN CONFIG FILE, CGIS PASS IN CGI CONFIG FILE! ***/
        
        /* initialize the location of the status log */
        xcddefault_comment_file = locations_h.DEFAULT_COMMENT_FILE;
        xcddefault_temp_file = locations_h.DEFAULT_TEMP_FILE;
        
        /* open the config file for reading */
        blue_h.mmapfile thefile = utils.file_functions.mmap_fopen(config_file);
        
        if( thefile == null )
            return common_h.ERROR;
        
        /* read in all lines from the main config file */
        while(true)
        {
            
            /* read the next line */
            String input = utils.file_functions.mmap_fgets(thefile);
            
            if(input == null)
                break;
            
            input = input.trim();
            
            /* skip blank lines and comments */
            if(input.length() == 0 ||  input.charAt(0) =='#' )
                continue;

            if(blue.is_core)
            {
                /* core reads variables directly from the main config file */
                xcddefault_grab_config_directives(input); 
                
            }
            else if(input.startsWith("main_config_file="))
            {
                String config_file2 = input.substring( input.indexOf("=")+1 );
                /* open the config file for reading */
                blue_h.mmapfile thefile2 = utils.file_functions.mmap_fopen(config_file2);
                
                if( thefile2 == null )
                    continue;
                
                /* read in all lines from the main config file */
                while(true)
                {
                    
                    /* read the next line */
                    String input2 = utils.file_functions.mmap_fgets(thefile2);
                    if (input2 == null)
                        break;
                    
                    input2 = input2.trim();
                    
                    /* skip blank lines and comments */
                    if(input2.length() == 0 ||  input2.charAt(0) =='#' )
                        continue;
                    
                    /* core reads variables directly from the main config file */
                    xcddefault_grab_config_directives(input2); 
                }
                
                /* free memory and close the file */
                utils.file_functions.mmap_fclose(thefile2);
            }
        }
        
        /* free memory and close the file */
        utils.file_functions.mmap_fclose(thefile);
        
        /* we didn't find the comment file */
        if( xcddefault_comment_file.length() == 0 )
            return common_h.ERROR;
        
        /* we didn't find the temp file */
        if( xcddefault_temp_file.length() == 0 )
            return common_h.ERROR;
        
        if ( blue.is_core )
            /* save the comment data file macro */
            blue.macro_x [ blue_h.MACRO_COMMENTDATAFILE ]  = xcddefault_comment_file;
        
        return common_h.OK;
    }
    
    public static void xcddefault_grab_config_directives(String input_buffer)
    {
        /* comment file definition */
        if ( input_buffer.startsWith( "comment_file") || input_buffer.startsWith( "xcddefault_comment_file"))
        {
            int index = input_buffer.indexOf("=");
            
            if ( index <0 || index == input_buffer.length())
                return;
            xcddefault_comment_file = input_buffer.substring( index + 1);
        } 
        
        /* temp file definition */
        else if(input_buffer.startsWith( "temp_file") || input_buffer.startsWith("xcddefault_temp_file" ))
        {
            
            int index = input_buffer.indexOf( "=");
            if ( index <0 || index == input_buffer.length() )
                return;
            xcddefault_temp_file = input_buffer.substring(index);
        }

        // If CGI config then main_config_file is passed in.
        else if( input_buffer.startsWith( "main_config_file") )  {
            
            int index = input_buffer.indexOf( "=");
            if ( index <0 || index == input_buffer.length() )
                return;
            
            String temp_buffer = input_buffer.substring( index );
            xcddefault_grab_config_info( temp_buffer );
        }

    }
    
    /******************************************************************/
    /************ COMMENT INITIALIZATION/CLEANUP FUNCTIONS ************/
    /******************************************************************/
    
    
    /* initialize comment data */
    public static int xcddefault_initialize_comment_data( String config_file){
        int result;
        
        /* grab configuration data */
        result = xcddefault_grab_config_info(config_file);
        if(result== common_h.ERROR)
            return  common_h.ERROR;
        
        /* create comment file if necessary */
        xcddefault_create_comment_file();
        
        /* read comment data */
        xcddefault_read_comment_data(config_file);
        
        /* clean up the old comment data */
        xcddefault_validate_comment_data();
        
        return common_h.OK;
    }
    
    
    
    /* creates an empty comment data file if one doesn't already exist */
    public static int xcddefault_create_comment_file(){
        
        File file = new File(xcddefault_comment_file);
        if ( file.exists() )
            return common_h.OK;
        
        /* create an empty file */
        xcddefault_save_comment_data();
        
        return common_h.OK;
    }
    
    /* removes invalid and old comments from the comment file */
    public static int xcddefault_validate_comment_data()
    {
//      comment *next_comment;
        boolean update_file = false;
        boolean delete_comment = false;
        
        /* remove stale comments */
        for ( ListIterator iter = comments.comment_list.listIterator(); iter.hasNext(); ) {
            comments_h.comment temp_comment = (comments_h.comment) iter.next();
            delete_comment = false;
            
            /* delete comments with invalid host names */
            if( objects.find_host(temp_comment.host_name)==null)
                delete_comment = true;
            
            /* delete comments with invalid service descriptions */
            if(temp_comment.comment_type==comments_h.SERVICE_COMMENT && objects.find_service(temp_comment.host_name,temp_comment.service_description)==null)
                delete_comment = true;
            
            /* delete non-persistent comments */
            if(temp_comment.persistent==common_h.FALSE)
                delete_comment = true;
            
            /* delete the comment */
            if( delete_comment )
            {
                update_file = true;
                comments.delete_comment(temp_comment.comment_type,temp_comment.comment_id);
            }
        }	
        
        /* update comment file */
        if(update_file)
            xcddefault_save_comment_data();
        
        /* reset the current comment counter */
        current_comment_id=0;
        
        /* find the new starting index for comment id */
        for ( ListIterator iter = comments.comment_list.listIterator(); iter.hasNext(); ) {
            comments_h.comment temp_comment = (comments_h.comment) iter.next();
            if(temp_comment.comment_id>current_comment_id)
                current_comment_id=temp_comment.comment_id;
        }
        
        return common_h.OK;
    }
    
    
    /* removes invalid and old comments from the comment file */
    public static int xcddefault_cleanup_comment_data(String main_config_file)
    {
        /* we don't need to do any cleanup... */
        return common_h.OK;
    }
    
    /******************************************************************/
    /***************** DEFAULT DATA OUTPUT FUNCTIONS ******************/
    /******************************************************************/
    
    
    /* adds a new host comment */
    public static comments_h.comment xcddefault_add_new_host_comment(int entry_type, String host_name, long entry_time, String author_name, String comment_data, int persistent, int source, int expires, long expire_time ){
        
        /* find the next valid comment id */
        do{
            current_comment_id++;
        } while( comments.find_host_comment(current_comment_id)!=null);
        
        /* add comment to list in memory */
        comments_h.comment temp_comment = comments.add_host_comment(entry_type,host_name,entry_time,author_name,comment_data,current_comment_id,persistent,expires,expire_time,source);
        
        /* update comment file */
        xcddefault_save_comment_data();
        
        return temp_comment;
    }
    
    
    /* adds a new service comment */
    public static comments_h.comment xcddefault_add_new_service_comment(int entry_type, String host_name, String svc_description, long entry_time, String author_name, String comment_data, int persistent, int source, int expires, long expire_time){
        
        
        /* find the next valid comment id */
        do{
            current_comment_id++;
        } while( comments.find_service_comment(current_comment_id)!=null);
        
        /* add comment to list in memory */
        comments_h.comment temp_comment = comments.add_service_comment(entry_type,host_name,svc_description,entry_time,author_name,comment_data,current_comment_id,persistent,expires,expire_time,source);
        
        /* update comment file */
        xcddefault_save_comment_data();
        
        return temp_comment;
    }
    
    
    
    /******************************************************************/
    /**************** COMMENT DELETION FUNCTIONS **********************/
    /******************************************************************/
    
    
    /* deletes a host comment */
    public static int xcddefault_delete_host_comment(long comment_id){
        
        /* update comment file */
        xcddefault_save_comment_data();
        
        return common_h.OK;
    }
    
    /* deletes a service comment */
    public static int xcddefault_delete_service_comment( long comment_id){
        
        /* update comment file */
        xcddefault_save_comment_data();
        
        return common_h.OK;
    }
    
    /* deletes all comments for a particular host */
    public static int xcddefault_delete_all_host_comments(String host_name){
        
        /* update comment file */
        xcddefault_save_comment_data();
        
        return common_h.OK;
    }
    
    /* deletes all comments for a particular service */
    public static int xcddefault_delete_all_service_comments(String host_name, String svc_description){
        
        /* update comment file */
        xcddefault_save_comment_data();
        
        return common_h.OK;
    }
    
    /******************************************************************/
    /****************** COMMENT OUTPUT FUNCTIONS **********************/
    /******************************************************************/
    
    /* writes comment data to file */
    public static int xcddefault_save_comment_data()
    {
        
        /* open a safe temp file for output */
        File temp_file;
        
        try
        {
            temp_file = File.createTempFile("comment", "file");
        }
        catch ( IOException ioE )
        {
            logger.fatal( "Error: Unable to create temp file for writing comment data!", ioE );
            return common_h.ERROR;
        }
        
        PrintWriter pw;
        
        try
        {
            pw = new PrintWriter ( new FileWriter ( temp_file ) );
        }
        catch ( IOException ioE )
        {
            logger.fatal( "Error: Unable to open temp file '"+temp_file.toString() +"' for writing comment data!" , ioE);
            return common_h.ERROR;
        }
        
        /* write header */
        pw.println( "########################################");
        pw.println( "#          NAGIOS COMMENT FILE");
        pw.println( "#");
        pw.println( "# THIS FILE IS AUTOMATICALLY GENERATED");
        pw.println( "# BY NAGIOS.  DO NOT MODIFY THIS FILE!");
        pw.println( "########################################");
        pw.println();
        
        long current_time = utils.currentTimeInSeconds();
        
        /* write file info */
        pw.println( "info {");
        pw.println( "\tcreated=" + current_time);
        pw.println( "\tversion=" + common_h.PROGRAM_VERSION);
        pw.println( "\t}");
        pw.println();
        
        /* save all comments */
        for(ListIterator iter = comments.comment_list.listIterator(); iter.hasNext();)
        {
            comments_h.comment temp_comment = (comments_h.comment) iter.next();
            
            if(temp_comment.comment_type==comments_h.HOST_COMMENT)
                pw.println( "hostcomment {" );
            else
                pw.println( "servicecomment {" );
            pw.println( "\thost_name=" + temp_comment.host_name);
            if(temp_comment.comment_type==comments_h.SERVICE_COMMENT)
                pw.println( "\tservice_description=" + temp_comment.service_description);
            pw.println( "\tentry_type=" + temp_comment.entry_type);
            pw.println( "\tcomment_id=" + temp_comment.comment_id);
            pw.println( "\tsource=" + temp_comment.source);
            pw.println( "\tpersistent=" + temp_comment.persistent);
            pw.println( "\tentry_time=" + temp_comment.entry_time);
            pw.println( "\texpires=" + temp_comment.expires);
            pw.println( "\texpire_time=" + temp_comment.expire_time);
            pw.println( "\tauthor=" + temp_comment.author);
            pw.println( "\tcomment_data=" + temp_comment.comment_data);
            pw.println( "\t}");
            pw.println();
        }
        
//      /* reset file permissions */
//      fchmod(fd,S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH);
        
        /* close the temp file */
        pw.close();
        
        /* move the temp file to the comment file (overwrite the old comment file) */
        if(utils.file_functions.my_rename( temp_file.toString(), xcddefault_comment_file) != 0 )
        {
            logger.fatal( "Error: Unable to update comment data file '"+xcddefault_comment_file+"'!");
            return common_h.ERROR;
        }
        
        temp_file.delete();
        
        return common_h.OK;
    }
    
    /******************************************************************/
    /****************** COMMENT INPUT FUNCTIONS ***********************/
    /******************************************************************/
    
    
    /* read the comment file */
    public static int xcddefault_read_comment_data(String main_config_file)
    {
        
    	String input=null;
        int data_type=xcddefault_h.XCDDEFAULT_NO_DATA;
        String var;
        String val;
//        int result;
        long comment_id=0;
        int persistent=common_h.FALSE;
        int expires=common_h.FALSE;
        long expire_time=0L;
        int entry_type= comments_h.USER_COMMENT;
        int source=comments_h.COMMENTSOURCE_INTERNAL;
        long entry_time=0L;
        String host_name=null;
        String service_description=null;
        String author=null;
        String comment_data=null;
        
        /* grab configuration data */
        int result=xcddefault_grab_config_info(main_config_file);
        if(result==common_h.ERROR)
            return common_h.ERROR;
        
        /* open the comment file for reading */
        blue_h.mmapfile thefile = utils.file_functions.mmap_fopen(xcddefault_comment_file);
        
        logger.debug( "OPENING COMMENT FILE!");
        if( thefile == null )
            return common_h.ERROR;
        
        /* read all lines in the comment file */
        while( true )
        {
            
            /* read the next line */
            input = utils.file_functions.mmap_fgets( thefile );
            if( input == null )
                break;
            
            input = input.trim();
            /* skip blank lines and comments */
            if( input.length() == 0 || input.charAt(0) =='#' )
                continue;
            
            else if(input.equals("info {"))
                data_type=xcddefault_h.XCDDEFAULT_INFO_DATA;
            else if(input.equals("hostcomment {"))
                data_type=xcddefault_h.XCDDEFAULT_HOST_DATA;
            else if(input.equals("servicecomment {"))
                data_type=xcddefault_h.XCDDEFAULT_SERVICE_DATA;
            
            else if(input.equals("}"))
            {
                
                switch(data_type){
                
                case xcddefault_h.XCDDEFAULT_INFO_DATA:
                    break;
                    
                case xcddefault_h.XCDDEFAULT_HOST_DATA:
                case xcddefault_h.XCDDEFAULT_SERVICE_DATA:
                    comments.add_comment((data_type==xcddefault_h.XCDDEFAULT_HOST_DATA)?comments_h.HOST_COMMENT:comments_h.SERVICE_COMMENT,entry_type,host_name,service_description,entry_time,author,comment_data,comment_id,persistent,expires,expire_time,source);
                    break;
                    
                default:
                    break;
                }
                
                /* reset defaults */
                data_type=xcddefault_h.XCDDEFAULT_NO_DATA;
                
                host_name=null;
                service_description=null;
                author=null;
                comment_data=null;
                entry_type=comments_h.USER_COMMENT;
                comment_id=0;
                source=comments_h.COMMENTSOURCE_INTERNAL;
                persistent=common_h.FALSE;
                entry_time=0L;
                expires=common_h.FALSE;
                expire_time=0L;
            }
            
            else if(data_type!=xcddefault_h.XCDDEFAULT_NO_DATA)
            {
                
                String[] split = input.split( "=", 2 );
                if ( split.length != 2 )
                    continue;
                var = split[0];
                val = split[1];
                
                switch(data_type){
                
                case xcddefault_h.XCDDEFAULT_INFO_DATA:
                    break;
                    
                case xcddefault_h.XCDDEFAULT_HOST_DATA:
                case xcddefault_h.XCDDEFAULT_SERVICE_DATA:
                    if(var.equals("host_name"))
                        host_name=val;
                    else if(var.equals("service_description"))
                        service_description=val;
                    else if(var.equals("entry_type"))
                        entry_type=atoi(val);
                    else if(var.equals("comment_id"))
                        comment_id=strtoul(val,null,10);
                    else if(var.equals("source"))
                        source=atoi(val);
                    else if(var.equals("persistent"))
                        persistent=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
                    else if(var.equals("entry_time"))
                        entry_time=strtoul(val,null,10);
                    else if(var.equals("expires"))
                        expires=(atoi(val)>0)?common_h.TRUE:common_h.FALSE;
                    else if(var.equals("expire_time"))
                        expire_time=strtoul(val,null,10);
                    else if(var.equals("author"))
                        author=val;
                    else if(var.equals("comment_data"))
                        comment_data=val;
                    break;
                    
                default:
                    break;
                }
                
            }
        }
        
        /* free memory and close the file */
        logger.debug( "CLOSING COMMENT FILE!");
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