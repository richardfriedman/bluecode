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

package org.blue.star.cgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.comments;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.locations_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class cgiutils {
   
   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.cgi");
   
   /** Flag to indicate if the CGI config file has been loaded, this flag can be reset force reloading of config file **/
   public static boolean loaded_cgi_config_file = false;
   
   /** Flag to indicate if the MAIN config file has been loaded, this flag can be reset force reloading of config file **/
   public static boolean loaded_main_config_file = false;
   
   /** Main config file, the config file blue server uses to configure itself and model. **/
   public static String main_config_file = "";
   
   /** File locked by BLUE SERVER when running. **/
   public static String lock_file = "";

   /** Command file used by Blue Server. **/
   public static String command_file = "";

   /** TODO  **/
   public static String log_file = "";
  
   /** TODO **/ 
   public static String log_archive_path = "";
   
   
   public static String physical_html_path = "";
   public static String physical_images_path = "";
   public static String physical_ssi_path = "";
   
   public static String url_html_path = "";
   public static String url_docs_path = "";
   public static String url_context_help_path = "";
   public static String url_images_path = "";
   public static String url_logo_images_path = "";
   public static String url_stylesheets_path = "";
   public static String url_media_path = "";
   
   public static String service_critical_sound=null;
   public static String service_warning_sound=null;
   public static String service_unknown_sound=null;
   public static String host_down_sound=null;
   public static String host_unreachable_sound=null;
   public static String normal_sound=null;
   public static String statusmap_background_image=null;
   public static String statuswrl_include=null;
   
   public static String ping_syntax=null;
   
   public static String blue_check_command ="";
   public static String blue_process_info ="";
   public static int blue_process_state=blue_h.STATE_OK;
   
   public static int check_external_commands=0;
   
   public static int date_format=common_h.DATE_FORMAT_US;
   
   public static int log_rotation_method=common_h.LOG_ROTATION_NONE;
   
   public static long this_scheduled_log_rotation=0L;
   public static long last_scheduled_log_rotation=0L;
   public static long next_scheduled_log_rotation=0L;
   
   public static int use_authentication=common_h.TRUE;
   
   public static int interval_length=60;
   
   public static int show_context_help=common_h.FALSE;
   
   public static int hosts_have_been_read=common_h.FALSE;
   public static int hostgroups_have_been_read=common_h.FALSE;
   public static int servicegroups_have_been_read=common_h.FALSE;
   public static int contacts_have_been_read=common_h.FALSE;
   public static int contactgroups_have_been_read=common_h.FALSE;
   public static int services_have_been_read=common_h.FALSE;
   public static int timeperiods_have_been_read=common_h.FALSE;
   public static int commands_have_been_read=common_h.FALSE;
   public static int servicedependencies_have_been_read=common_h.FALSE;
   public static int serviceescalations_have_been_read=common_h.FALSE;
   public static int hostdependencies_have_been_read=common_h.FALSE;
   public static int hostescalations_have_been_read=common_h.FALSE;
   public static int hostextinfo_have_been_read=common_h.FALSE;
   public static int serviceextinfo_have_been_read=common_h.FALSE;
   
   public static int host_status_has_been_read=common_h.FALSE;
   public static int service_status_has_been_read=common_h.FALSE;
   public static int program_status_has_been_read=common_h.FALSE;
   
   public static int refresh_rate= cgiutils_h.DEFAULT_REFRESH_RATE;
   
   public static int default_statusmap_layout_method=0;
   public static int default_statuswrl_layout_method=0;
   
   public static Stack<String> lifo_list = null;
   
   public static String nagios_cgi_config = null;
   public static String nagios_command_file = null;
   
   public static boolean is_servlet = false;
   
   /**********************************************************
    ***************** CLEANUP FUNCTIONS **********************
    **********************************************************/
   public static int exit( int result ) {
      if ( !is_servlet )
         System.exit( result );
      return result;
   }
   
   /**
    * Forces the reset of variables and indicates main and cgi files have not been loaded.
    * Hence this forces a refresh of many of the variables, hence, not requiring server restart.
    */
   public static void force_reset() {
      loaded_cgi_config_file = false;
      loaded_main_config_file = false;
      reset_request_context();
   }
   
   /**
    * This resets the init variables of BLUE servlet processing.  
    * This of course is legacy nagios, however, it forces a reload
    * per request of information. Hence any changes are reflected immediately.
    * We will slowly weed away from this, however, forceReset will cause a single reset to 
    * occur. THis will be made availae through a /admin servlet.
    * 
    */
   public static void reset_request_context(){
      
      if ( !loaded_cgi_config_file ) {
         main_config_file = "";
         show_context_help=common_h.FALSE;
         use_authentication=common_h.TRUE;
         blue_check_command ="";
         refresh_rate= cgiutils_h.DEFAULT_REFRESH_RATE;
         physical_html_path = "";
         physical_images_path = "";
         physical_ssi_path = "";
         url_html_path = "";
         url_docs_path = "";
         url_context_help_path = "";
         url_images_path = "";
         url_logo_images_path = "";
         url_stylesheets_path = "";
         url_media_path = "";
         service_critical_sound=null;
         service_warning_sound=null;
         service_unknown_sound=null;
         host_down_sound=null;
         host_unreachable_sound=null;
         normal_sound=null;
         statusmap_background_image=null;
         statuswrl_include=null;
         default_statusmap_layout_method=0;
         default_statusmap_layout_method=0;
         ping_syntax=null;
      }
      
      if ( !loaded_main_config_file) {
         interval_length=60;
         command_file = get_cmd_file_location();
         lock_file = "";
         check_external_commands=0;
         date_format=common_h.DATE_FORMAT_US;
         log_file="";
         log_archive_path = "";
         log_rotation_method= common_h.LOG_ROTATION_NONE;
      }
      
      /** DO NOT RESET THESE VARIABLES **/
//    cgiutils.is_servlet = false;
//    cgiutils.nagios_cgi_config = null;
//    cgiutils.nagios_command_file = null;
      
      blue_process_info ="";
      blue_process_state=blue_h.STATE_OK;
      
      hosts_have_been_read=common_h.FALSE;
      hostgroups_have_been_read=common_h.FALSE;
      servicegroups_have_been_read=common_h.FALSE;
      contacts_have_been_read=common_h.FALSE;
      contactgroups_have_been_read=common_h.FALSE;
      services_have_been_read=common_h.FALSE;
      timeperiods_have_been_read=common_h.FALSE;
      commands_have_been_read=common_h.FALSE;
      servicedependencies_have_been_read=common_h.FALSE;
      serviceescalations_have_been_read=common_h.FALSE;
      hostdependencies_have_been_read=common_h.FALSE;
      hostescalations_have_been_read=common_h.FALSE;
      hostextinfo_have_been_read=common_h.FALSE;
      serviceextinfo_have_been_read=common_h.FALSE;
      
      host_status_has_been_read=common_h.FALSE;
      service_status_has_been_read=common_h.FALSE;
      program_status_has_been_read=common_h.FALSE;
      
      lifo_list = null;
      
      this_scheduled_log_rotation=0L;
      last_scheduled_log_rotation=0L;
      next_scheduled_log_rotation=0L;
    }
   
   /**
    * Free memory held on a per request basis.  Currently each request reloads complete status information 
    * into memory.
    */
   public static void free_memory(){
      
      /* free memory for common object definitions */
      objects.free_object_data();
      
      /* free memory for status data */
      statusdata.free_status_data();
      
      /* free comment data */
      comments.free_comment_data();
      
      /* free downtime data */
      downtime.free_downtime_data();
   }
   
   /**********************************************************
    *************** CONFIG FILE FUNCTIONS ********************
    **********************************************************/

   /**
    * In original base CGI configuration is specified in external configuration file.
    * Currently we maintain that capability, however, we will migrate configuration to 
    * web.xml initialization. Hence we can simply package everything up and migrate it.
    * Still worried about cross compatibility.
    * 
    * Specified (a) in web.xml NAGIOS_CGI_CONFIG  (b) as a System environment variable (c) in locations_h compiled in.
    * 
    * @return CGI Configuration (typically cgi.xml) file, physical path/file.
    */
   public static String  get_cgi_config_location(){
      
      if ( nagios_cgi_config == null ) 
         nagios_cgi_config = System.getenv("NAGIOS_CGI_CONFIG");
      
      if( nagios_cgi_config == null)
         nagios_cgi_config=locations_h.DEFAULT_CGI_CONFIG_FILE;
      
      return nagios_cgi_config;
   }
   
   /* read the command file location from an environment variable */
   public static String  get_cmd_file_location(){
      
      if ( nagios_command_file == null ) 
         nagios_command_file = System.getenv("NAGIOS_COMMAND_FILE");
      if ( nagios_command_file == null )
         nagios_command_file = locations_h.DEFAULT_COMMAND_FILE;
      return nagios_command_file;
      
   }
   
   /*read the CGI configuration file */
   public static int read_cgi_config_file(String filename){
      String input=null;
      String temp_buffer;
      cgiutils_h.mmapfile thefile;
      
      if ( loaded_cgi_config_file )
         return common_h.OK;
      
      if((thefile=mmap_fopen(filename))==null)
         return common_h.ERROR;
      
      while(true){
         
         /* read the next line */
         if((input=mmap_fgets(thefile))==null)
            break;
         
         input = input.trim();
         
         if ( input.indexOf( "=" ) == -1 || input.startsWith( "#") ) 
            continue;
         
         String[] split = input.split( "[=]", 2 );
         input = split[0] + "=";
         temp_buffer = (split.length==2)?split[1]:null;
         
         if(input.startsWith( "main_config_file=" )){
            if(temp_buffer!=null){
               main_config_file = temp_buffer.trim();
            }
         }
         
         else if((input.startsWith( "show_context_help=" ))){
            if(temp_buffer==null)
               show_context_help=common_h.TRUE;
            else if(atoi(temp_buffer)==0)
               show_context_help=common_h.FALSE;
            else
               show_context_help=common_h.TRUE;
         }
         
         else if((input.startsWith( "use_authentication=" ))){
            if(temp_buffer==null)
               use_authentication=common_h.TRUE;
            else if(atoi(temp_buffer)==0)
               use_authentication=common_h.FALSE;
            else
               use_authentication=common_h.TRUE;
         }
         
         else if(input.startsWith( "nagios_check_command=" )){
            if(temp_buffer!=null){
               blue_check_command = temp_buffer.trim();
            }
         }
         
         else if(input.startsWith( "refresh_rate=" )){
            refresh_rate=atoi((temp_buffer==null)?"60":temp_buffer);
         }
         
         else if(input.startsWith( "physical_html_path=" )){
            
            physical_html_path = (temp_buffer==null)?"":temp_buffer.trim();
            if( !physical_html_path.endsWith( "/") )
               physical_html_path+="/";
            
            physical_images_path= String.format( "%simages/",physical_html_path);
            physical_ssi_path= String.format( "%sssi/",physical_html_path);
         }
         
         else if(input.startsWith( "url_html_path=" )){
            
            url_html_path = (temp_buffer==null)?"":temp_buffer.trim();
            if( !url_html_path.endsWith("/" ) )
               url_html_path += "/";
            
            url_docs_path = String.format( "%sdocs/",url_html_path);
            url_context_help_path = String.format( "%scontexthelp/",url_html_path);
            url_images_path = String.format( "%simages/",url_html_path);
            url_logo_images_path= String.format( "%slogos/",url_images_path);
            url_stylesheets_path= String.format( "%sstylesheets/",url_html_path);
            url_media_path= String.format( "%smedia/",url_html_path);
         }
         
         else if ( input.startsWith ( "url_context_help_path") ) {
            url_context_help_path = temp_buffer;
         }
         
         else if(input.startsWith( "service_critical_sound=" )){
            if(temp_buffer==null)
               continue;
            service_critical_sound=temp_buffer;
         }
         
         else if(input.startsWith( "service_warning_sound=" )){
            
            if(temp_buffer==null)
               continue;
            service_warning_sound=temp_buffer;
         }
         
         else if(input.startsWith( "service_unknown_sound=" )){
            
            if(temp_buffer==null)
               continue;
            service_unknown_sound=temp_buffer;
         }
         
         else if(input.startsWith( "host_down_sound=" )){
            
            if(temp_buffer==null)
               continue;
            host_down_sound=temp_buffer;
         }
         
         else if(input.startsWith( "host_unreachable_sound=" )){
            
            if(temp_buffer==null)
               continue;
            host_unreachable_sound=temp_buffer;
         }
         
         else if(input.startsWith( "normal_sound=" )){
            
            if(temp_buffer==null)
               continue;
            normal_sound=temp_buffer;
         }
         
         else if(input.startsWith( "statusmap_background_image=" )){
            
            if(temp_buffer==null)
               continue;
            statusmap_background_image=temp_buffer;
         }
         
         else if(input.startsWith( "default_statusmap_layout=" )){
            default_statusmap_layout_method=atoi((temp_buffer==null)?"0":temp_buffer);
         }
         
         else if(input.startsWith( "default_statuswrl_layout=" )){
            
            default_statuswrl_layout_method=atoi((temp_buffer==null)?"0":temp_buffer);
         }
         
         else if(input.startsWith( "statuswrl_include=" )){
            
            if(temp_buffer==null)
               
               continue;
            statuswrl_include=temp_buffer;
         }
         
         else if(input.startsWith( "ping_syntax=" )){
            
            if(temp_buffer==null)
               continue;
            ping_syntax=temp_buffer;
         }
         
      }
      
      /* free memory and close the file */
      mmap_fclose(thefile);
      
      if(main_config_file.equals(""))
         return common_h.ERROR;
      
      loaded_cgi_config_file = true; 
      return common_h.OK;
   }
   
   /* read the main configuration file */
   public static int read_main_config_file(String filename){
      String input=null;
      String temp_buffer;
      cgiutils_h.mmapfile thefile;
      
      if ( loaded_main_config_file )
         return common_h.OK;
     
      if((thefile=mmap_fopen(filename))==null)
         return common_h.ERROR;
      
      while(true){
         
         /* read the next line */
         input=mmap_fgets(thefile);
         if(input == null)
            break;
         
         input = input.trim();
         
         if(input.startsWith( "interval_length=" )){ 
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            interval_length=(temp_buffer.length()==0)?60:atoi(temp_buffer);
         }
         
         else if ( input.startsWith( "lock_file=") ) {
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            lock_file = ((temp_buffer.length()==0)?"":temp_buffer).trim();
         }
         
         else if(input.startsWith( "log_file=" )){
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            log_file = ((temp_buffer.length()==0)?"":temp_buffer).trim();            
         }
         
         else if(input.startsWith( "log_archive_path=" )){
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            log_archive_path = ( (temp_buffer.length()==0)?"":temp_buffer ).trim();
            if  ( !log_archive_path.endsWith( "/" ) )
               log_archive_path += "/";
         }
         
         else if(input.startsWith( "log_rotation_method=" )){
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            if(temp_buffer.length()==0)
               log_rotation_method=common_h.LOG_ROTATION_NONE;
            else if(temp_buffer.equals("h"))
               log_rotation_method=common_h.LOG_ROTATION_HOURLY;
            else if(temp_buffer.equals("d"))
               log_rotation_method=common_h.LOG_ROTATION_DAILY;
            else if(temp_buffer.equals("w"))
               log_rotation_method=common_h.LOG_ROTATION_WEEKLY;
            else if(temp_buffer.equals("m"))
               log_rotation_method=common_h.LOG_ROTATION_MONTHLY;
         }
         
         else if(input.startsWith( "command_file=" )){
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            command_file = ((temp_buffer.length()==0)?"":temp_buffer).trim();
         }
         
         else if(input.startsWith( "check_external_commands=" )){
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            check_external_commands=(temp_buffer.length()==0)?0:atoi(temp_buffer);
         }
         
         else if(input.startsWith( "date_format=" )){
            temp_buffer = input.substring( input.indexOf( "=") + 1 );
            if(temp_buffer==null)
               date_format=common_h.DATE_FORMAT_US;
            else if(temp_buffer.equals("euro"))
               date_format=common_h.DATE_FORMAT_EURO;
            else if(temp_buffer.equals("iso8601"))
               date_format=common_h.DATE_FORMAT_ISO8601;
            else if(temp_buffer.equals("strict-iso8601"))
               date_format=common_h.DATE_FORMAT_STRICT_ISO8601;
            else
               date_format=common_h.DATE_FORMAT_US;
         }
      }
      
      /* free memory and close the file */
      mmap_fclose(thefile);
      
      loaded_main_config_file = true;
      return common_h.OK;
   }
   
   /* read all object definitions */
   public static int read_all_object_configuration_data(String config_file,int options){
      int result=common_h.OK;
      
      /* don't duplicate things we've already read in */
      if(hosts_have_been_read==common_h.TRUE && ((options & common_h.READ_HOSTS)>0))
         options-=common_h.READ_HOSTS;
      if(hostgroups_have_been_read==common_h.TRUE && ((options & common_h.READ_HOSTGROUPS)>0))
         options-=common_h.READ_HOSTGROUPS;
      if(contacts_have_been_read==common_h.TRUE && ((options & common_h.READ_CONTACTS)>0))
         options-=common_h.READ_CONTACTS;
      if(contactgroups_have_been_read==common_h.TRUE && ((options & common_h.READ_CONTACTGROUPS)>0))
         options-=common_h.READ_CONTACTGROUPS;
      if(services_have_been_read==common_h.TRUE && ((options & common_h.READ_SERVICES)>0))
         options-=common_h.READ_SERVICES;
      if(timeperiods_have_been_read==common_h.TRUE && ((options & common_h.READ_TIMEPERIODS)>0))
         options-=common_h.READ_TIMEPERIODS;
      if(commands_have_been_read==common_h.TRUE && ((options & common_h.READ_COMMANDS)>0))
         options-=common_h.READ_COMMANDS;
      if(servicedependencies_have_been_read==common_h.TRUE && ((options & common_h.READ_SERVICEDEPENDENCIES)>0))
         options-=common_h.READ_SERVICEDEPENDENCIES;
      if(serviceescalations_have_been_read==common_h.TRUE && ((options & common_h.READ_SERVICEESCALATIONS)>0))
         options-=common_h.READ_SERVICEESCALATIONS;
      if(hostdependencies_have_been_read==common_h.TRUE && ((options & common_h.READ_HOSTDEPENDENCIES)>0))
         options-=common_h.READ_HOSTDEPENDENCIES;
      if(hostescalations_have_been_read==common_h.TRUE && ((options & common_h.READ_HOSTESCALATIONS)>0))
         options-=common_h.READ_HOSTESCALATIONS;
      if(hostextinfo_have_been_read==common_h.TRUE && ((options & common_h.READ_HOSTEXTINFO)>0))
         options-=common_h.READ_HOSTEXTINFO;
      if(serviceextinfo_have_been_read==common_h.TRUE && ((options & common_h.READ_SERVICEEXTINFO)>0))
         options-=common_h.READ_SERVICEEXTINFO;
      if(serviceextinfo_have_been_read==common_h.TRUE && ((options & common_h.READ_SERVICEGROUPS)>0))
         options-=common_h.READ_SERVICEGROUPS;
      
      /* bail out if we've already read what we need */
      if(options<=0)
         return common_h.OK;
      
      /* read in all external config data of the desired type(s) */
      result=objects.read_object_config_data(config_file,options,common_h.FALSE);
      
      /* mark what items we've read in... */
      if((options & common_h.READ_HOSTS)>0)
         hosts_have_been_read=common_h.TRUE;
      if((options & common_h.READ_HOSTGROUPS)>0)
         hostgroups_have_been_read=common_h.TRUE;
      if((options & common_h.READ_CONTACTS)>0)
         contacts_have_been_read=common_h.TRUE;
      if((options & common_h.READ_CONTACTGROUPS)>0)
         contactgroups_have_been_read=common_h.TRUE;
      if((options & common_h.READ_SERVICES)>0)
         services_have_been_read=common_h.TRUE;
      if((options & common_h.READ_TIMEPERIODS)>0)
         timeperiods_have_been_read=common_h.TRUE;
      if((options & common_h.READ_COMMANDS)>0)
         commands_have_been_read=common_h.TRUE;
      if((options & common_h.READ_SERVICEDEPENDENCIES)>0)
         servicedependencies_have_been_read=common_h.TRUE;
      if((options & common_h.READ_SERVICEESCALATIONS)>0)
         serviceescalations_have_been_read=common_h.TRUE;
      if((options & common_h.READ_HOSTDEPENDENCIES)>0)
         hostdependencies_have_been_read=common_h.TRUE;
      if((options & common_h.READ_HOSTESCALATIONS)>0)
         hostescalations_have_been_read=common_h.TRUE;
      if((options & common_h.READ_HOSTEXTINFO)>0)
         hostextinfo_have_been_read=common_h.TRUE;
      if((options & common_h.READ_SERVICEEXTINFO)>0)
         serviceextinfo_have_been_read=common_h.TRUE;
      if((options & common_h.READ_SERVICEGROUPS)>0)
         servicegroups_have_been_read=common_h.TRUE;
      
      return result;
   }
   
// /* read all status data */
   public static int read_all_status_data(String config_file,int options){
      int result=common_h.OK;
      
      /* don't duplicate things we've already read in */
      if( program_status_has_been_read==common_h.TRUE && ((options & statusdata_h.READ_PROGRAM_STATUS) >0) )
         options-=statusdata_h.READ_PROGRAM_STATUS;
      if(host_status_has_been_read==common_h.TRUE && ((options & statusdata_h.READ_HOST_STATUS) >0 ))
         options-=statusdata_h.READ_HOST_STATUS;
      if(service_status_has_been_read==common_h.TRUE && ( (options & statusdata_h.READ_SERVICE_STATUS)> 0 ) )
         options-=statusdata_h.READ_SERVICE_STATUS;
      
      /* bail out if we've already read what we need */
      if(options<=0)
         return common_h.OK;
      
      /* read in all external status data */
      result= statusdata.read_status_data(config_file,options);
      
      /* mark what items we've read in... */
      if ((options & statusdata_h.READ_PROGRAM_STATUS)> 0)
         program_status_has_been_read=common_h.TRUE;
      if ((options & statusdata_h.READ_HOST_STATUS) > 0 )
         host_status_has_been_read=common_h.TRUE;
      if( (options & statusdata_h.READ_SERVICE_STATUS) > 0 )
         service_status_has_been_read=common_h.TRUE;
      
      return result;
   }

   /**********************************************************
    ******************* LIFO FUNCTIONS ***********************
    **********************************************************/
   
   /* reads contents of file into the lifo struct */
   public static int read_file_into_lifo(String filename){
      String input=null;
      cgiutils_h.mmapfile thefile;
      int lifo_result;
      
      if ( lifo_list == null ) 
         lifo_list = new Stack<String>();
      else 
         lifo_list.clear();
      
      if((thefile=mmap_fopen(filename))==null)
         return cgiutils_h.LIFO_ERROR_FILE;
      
      while(true){
         
         if((input=mmap_fgets(thefile))==null)
            break;
         
         lifo_result = push_lifo(input);
         
         if(lifo_result!=cgiutils_h.LIFO_OK){
            free_lifo_memory();
            mmap_fclose(thefile);
            return lifo_result;
         }
      }
      
      mmap_fclose(thefile);
      
      return cgiutils_h.LIFO_OK;
   }
   
   /* frees all memory allocated to lifo */
   public static void free_lifo_memory(){
      if ( lifo_list != null )
         lifo_list.clear();
   }
   
   /* adds an item to lifo */
   public static int push_lifo(String buffer){
      
      lifo_list.push(buffer);
      return cgiutils_h.LIFO_OK;
      
   }
   
   /* returns/clears an item from lifo */
   public static String pop_lifo(){
      
      if ( lifo_list.isEmpty() )
         return null;
      
      return lifo_list.pop();
   }
   
 /**********************************************************
 *************** MISC UTILITY FUNCTIONS *******************
 **********************************************************/

   /**
    * Determine if BLUE SERVER is currently running.
    * Currently blue server and console are executed on the same server.  
    * Blue Server on startup will LOCK a file, if this process can get a lock
    * on that file, the process must NOT be running.  The lock file is defined in the main configuration.
    * 
    */
   public static void check_blue_server ()
   {

      FileLock lock = null;
      File file = new File( lock_file );
      
      if (!file.exists())
      {
         cgiutils.blue_process_state = blue_h.STATE_UNKNOWN;
         return;
      }
      
      try
      {
         // Get a file channel for the file
         FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
     
         lock = channel.tryLock();
         
         if(lock == null || lock.overlaps(0, Long.MAX_VALUE ))
         {
            cgiutils.blue_process_state = blue_h.STATE_OK;
         }
         else
         {
            lock.release();
            channel.close();
            cgiutils.blue_process_state = blue_h.STATE_UNKNOWN;
         }
     }
     catch (IOException e)
     {
        cgiutils.blue_process_state = blue_h.STATE_UNKNOWN;
     }
     catch(OverlappingFileLockException e)
     {
    	 /* It seems to me that if there is already a lock on the lock file, then the blue server
    	  * must be running. Hence we can return state ok from this. */
    	 cgiutils.blue_process_state = blue_h.STATE_OK;
     }
         
   }
   
   /* strips HTML and bad stuff from plugin output */
   public static String sanitize_plugin_output(String buffer){
      int in_html=common_h.FALSE;
      
      if(buffer==null)
         return null;
      
      StringBuffer new_buffer = new StringBuffer();
      
      /* check each character */
      for( char x : buffer.toCharArray() ){
         
         /* we just started an HTML tag */
         if(x=='<'){
            in_html=common_h.TRUE;
            continue;
         }
         
         /* end of an HTML tag */
         else if(x=='>'){
            in_html=common_h.FALSE;
            continue;
         }
         
         /* skip everything inside HTML tags */
         else if(in_html==common_h.TRUE)
            continue;
         
         /* strip single and double quotes */
         else if(x=='\'' || x=='\"')
            new_buffer.append(' ');
         
         /* strip semicolons (replace with colons) */
         else if(x==';')
            new_buffer.append(':');
         
         /* strip pipe and ampersand */
         else if(x=='&' || x=='|')
            new_buffer.append(' ');
         
         /* normal character */
         else
            new_buffer.append(x);
      }
      
      return new_buffer.toString();
   }
   
   /**
    * Convert a given time into a string.
    * 
    * @param raw_time  time in seconds since epoch
    * @param type  type of date string to create
    * @return  String formated with time.
    */
   public static String get_time_string(long raw_time, int type){
      String buffer;
      
      if ( raw_time == 0 )
         raw_time = System.currentTimeMillis();
      else 
         raw_time *= 1000;
      
      /* ctime() style date/time */
      if(type== common_h.LONG_DATE_TIME)
         buffer = new SimpleDateFormat( "EEE MMM d HH:mm:ss zzz yyyy" ).format( new Long( raw_time));
      
      /* short date/time */
      else if(type==common_h.SHORT_DATE_TIME){
         if(blue.date_format==common_h.DATE_FORMAT_EURO)
            buffer = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss").format( new Long( raw_time ));
         else if(blue.date_format==common_h.DATE_FORMAT_ISO8601 )
            buffer = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Long( raw_time ));
         else if(blue.date_format==common_h.DATE_FORMAT_STRICT_ISO8601)
            buffer = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).format( new Long( raw_time ));
         else
            buffer = new SimpleDateFormat( "MM-dd-yyyy HH:mm:ss" ).format( new Long( raw_time ));
      }
      
      /* short date */
      else if(type==common_h.SHORT_DATE){
         if(blue.date_format==common_h.DATE_FORMAT_EURO)
            buffer = new SimpleDateFormat( "dd-MM-yyyy").format( new Long( raw_time ));
         else if(blue.date_format==common_h.DATE_FORMAT_ISO8601 || blue.date_format==common_h.DATE_FORMAT_STRICT_ISO8601)
            buffer = new SimpleDateFormat( "yyyy-MM-dd" ).format( new Long( raw_time ));
         else
            buffer = new SimpleDateFormat( "MM-dd-yyyy" ).format( new Long( raw_time ));
      }
      
      /* expiration date/time for HTTP headers */
      else if(type==common_h.HTTP_DATE_TIME) {
         SimpleDateFormat sdf = new SimpleDateFormat( "EEE, d MMM zzz yyyy HH:mm:ss zzz" );
         sdf.setTimeZone( TimeZone.getTimeZone("GMT")) ;
         buffer = sdf.format( new Long( raw_time));
      }
      /* short time */
      else
         buffer = new SimpleDateFormat( "HH:mm:ss").format( new Long( raw_time ));
      
      return buffer;
   }
   
   
   /* get time string for an interval of time */
   public static String get_interval_time_string(int time_units ){
      long total_seconds;
      int hours=0;
      int minutes=0;
      int seconds=0;
      
      total_seconds = (time_units*interval_length);
      hours=(int)total_seconds/3600;
      total_seconds%=3600;
      minutes=(int)total_seconds/60;
      total_seconds%=60;
      seconds=(int)total_seconds;
      String buffer = String.format( "%dh %dm %ds",hours,minutes,seconds);
      
      return buffer;
   }

   /* open a file read-only via mmap() */
   public static cgiutils_h.mmapfile mmap_fopen( String filename){
      // TODO move the whole thing to use memory mapped file as they did.
      cgiutils_h.mmapfile new_mmapfile = new cgiutils_h.mmapfile ();
      
      /* open the file */
      try {
         File file = new File ( filename );
         BufferedReader reader = new BufferedReader( new FileReader ( file ) );
         
         /* populate struct info for later use */
         new_mmapfile.current_line=0L;
         new_mmapfile.reader = reader;
         new_mmapfile.file = file;
         
      } catch (Exception e ) {
         return null;
      }
      
      return new_mmapfile;
   }

   /* close a file originally opened via mmap() */
   public static int mmap_fclose(cgiutils_h.mmapfile temp_mmapfile){
      
      try { 
         temp_mmapfile.reader.close();
         temp_mmapfile.reader = null;
         temp_mmapfile.file = null;
         return common_h.OK;
      } catch (Throwable t) {
         logger.error( "warning: " + t.getMessage(), t);
         return common_h.ERROR;
      }
      
   }

   /* gets one line of input from an mmap()'ed file */
   public static String mmap_fgets(cgiutils_h.mmapfile temp_mmapfile){
      
      try {
         String line = temp_mmapfile.reader.readLine();
         if ( line != null )
            temp_mmapfile.current_line++;
         return line;
      } catch (Exception e) {
         logger.error( "warning: " + e.getMessage(), e);
         return null;
      }
      
   }
   
   public static class time_breakdown {
      public int days;
      public int hours;
      public int minutes;
      public int seconds;
   }
   
   /* get days, hours, minutes, and seconds from a raw time_t format or total seconds */
   public static time_breakdown get_time_breakdown(long raw_time){
      time_breakdown tb = new time_breakdown();
      long temp_time;
      
      temp_time=raw_time;
      
      tb.days=(int) temp_time/86400;
      temp_time-=(tb.days * 86400);
      tb.hours=(int) temp_time/3600;
      temp_time-=(tb.hours * 3600);
      tb.minutes=(int) temp_time/60;
      temp_time-=(tb.minutes * 60);
      tb.seconds =(int)temp_time;
      
      return tb;
   }
   
   /* encodes a string in proper URL format */
   public static String  url_encode(String input){
      try { 
         return URLEncoder.encode(input, "UTF-8");
      } catch (Exception e) {
         return input;
      }
   }
   
   /* escapes a string used in HTML */
   public static String  html_encode(String input_string){
      
      char[] input = input_string.toCharArray();
      
      StringBuffer encoded_html_string = new StringBuffer();
      
      for( int x=0 ;x<input.length ;x++){
         
         /* alpha-numeric characters don't get encoded */
         if((input[x]>='0' && input[x]<='9') || (input[x]>='A' && input[x]<='Z') || (input[x]>='a' && input[x]<='z')){
            encoded_html_string.append(input[x]);
         }
         
         /* spaces are encoded as non-breaking spaces */
         else if(input[x]<=' '){
            
            encoded_html_string.append("&nbsp;");
         }
         
         /* for simplicity, everything else gets represented by its numeric value */
         else{
            encoded_html_string.append(String.format( "&#%d;",(int) input[x]));
         }
      }
      
      return encoded_html_string.toString();
   }
   
   /* determines the log file we should use (from current time) */
   public static String get_log_archive_to_use(int archive){
      Calendar t;
      String buffer = null;
      
      /* determine the time at which the log was rotated for this archive # */
      determine_log_rotation_times(archive);
      
      /* if we're not rotating the logs or if we want the current log, use the main one... */
      if(log_rotation_method==common_h.LOG_ROTATION_NONE || archive<=0){
         buffer = log_file;
         return buffer;
      }
      
      t = Calendar.getInstance();
      t.setTimeInMillis(this_scheduled_log_rotation * 1000);
      
      /* use the time that the log rotation occurred to figure out the name of the log file */
      buffer = String.format( "%snagios-%02d-%02d-%d-%02d.log",log_archive_path,t.get(Calendar.MONTH)+1,t.get(Calendar.DAY_OF_MONTH),t.get(Calendar.YEAR),t.get(Calendar.HOUR_OF_DAY));
      
      return buffer;
   }
   
   /* determines log archive to use, given a specific time */
   public static int determine_archive_to_use_from_time(long target_time){
      long current_time;
      int current_archive=0;
      
      /* if log rotation is disabled, we don't have archives */
      if( log_rotation_method== common_h.LOG_ROTATION_NONE)
         return 0;
      
      /* make sure target time is rational */
      current_time= utils.currentTimeInSeconds();
      if(target_time>=current_time)
         return 0;
      
      /* backtrack through archives to find the one we need for this time */
      /* start with archive of 1, subtract one when we find the right time period to compensate for current (non-rotated) log */
      for(current_archive=1;;current_archive++){
         
         /* determine time at which the log rotation occurred for this archive number */
         determine_log_rotation_times(current_archive);
         
         /* if the target time falls within the times encompassed by this archive, we have the right archive! */
         if(target_time>=this_scheduled_log_rotation)
            return current_archive-1;
      }
      
//    return 0;
   }
   
   /* determines the log rotation times - past, present, future */
   public static void determine_log_rotation_times(int archive){
      Calendar t;
      int current_month;
      int is_dst_now=common_h.FALSE;
      long current_time;
      
      /* negative archive numbers don't make sense */
      /* if archive=0 (current log), this_scheduled_log_rotation time is set to next rotation time */
      if(archive<0)
         return;
      
      t= Calendar.getInstance();
      is_dst_now = (t.isSet( Calendar.DST_OFFSET ))?common_h.TRUE:common_h.FALSE;
      t.set( Calendar.MINUTE, 0 );
      t.set( Calendar.SECOND, 0 );
      
      switch(log_rotation_method){
         
         case common_h.LOG_ROTATION_HOURLY:
            this_scheduled_log_rotation= utils.getTimeInSeconds(t);
            this_scheduled_log_rotation=(this_scheduled_log_rotation-((archive-1)*3600));
            last_scheduled_log_rotation=(this_scheduled_log_rotation-3600);
            break;
            
         case common_h.LOG_ROTATION_DAILY:
            t.set( Calendar.HOUR, 0 );
            this_scheduled_log_rotation=utils.getTimeInSeconds(t);
            this_scheduled_log_rotation=(this_scheduled_log_rotation-((archive-1)*86400));
            last_scheduled_log_rotation=(this_scheduled_log_rotation-86400);
            break;
            
         case common_h.LOG_ROTATION_WEEKLY:
            t.set( Calendar.HOUR, 0 );
            this_scheduled_log_rotation=utils.getTimeInSeconds(t);
            this_scheduled_log_rotation=(this_scheduled_log_rotation-(86400*(t.get( Calendar.DAY_OF_WEEK )-1)));
            this_scheduled_log_rotation=(this_scheduled_log_rotation-((archive-1)*604800));
            last_scheduled_log_rotation=(this_scheduled_log_rotation-604800);
            break;
            
         case common_h.LOG_ROTATION_MONTHLY:
            current_time = utils.currentTimeInSeconds();
            
            t.setTimeInMillis( current_time * 1000 );
            t.roll( Calendar.MONTH, true );
            t.set( Calendar.DAY_OF_MONTH, 1 );
            t.set( Calendar.HOUR, 0 );
            t.set( Calendar.MINUTE, 0 );
            t.set( Calendar.SECOND, 0 );
            for(current_month=0;current_month<=archive;current_month++){
               if(t.get( Calendar.MONTH ) ==0 ){
                  t.set( Calendar.MONTH, 11 );
                  t.roll( Calendar.YEAR, false );
               }
               else
                  t.roll( Calendar.MONTH, false );
            }
            last_scheduled_log_rotation= utils.getTimeInSeconds(t);
            
            t.setTimeInMillis(current_time * 1000);
            t.roll( Calendar.MONTH, true );
            t.set( Calendar.DAY_OF_MONTH, 1 );
            t.set( Calendar.HOUR, 0 );
            t.set( Calendar.MINUTE, 0 );
            t.set( Calendar.SECOND, 0 );
            for(current_month=0;current_month<archive;current_month++){
               if(t.get( Calendar.MONTH ) ==0 ){
                  t.set( Calendar.MONTH, 11 );
                  t.roll( Calendar.YEAR, false );
               }
               else
                  t.roll( Calendar.MONTH, false );
            }
            this_scheduled_log_rotation=utils.getTimeInSeconds(t);
            
            break;
         default:
            break;
      }
      
      /* adjust this rotation time for daylight savings time */
      t.setTimeInMillis( this_scheduled_log_rotation * 1000 );
      if(t.get(Calendar.DST_OFFSET)>0 && is_dst_now==common_h.FALSE)
         this_scheduled_log_rotation= (this_scheduled_log_rotation-3600);
      else if(t.get(Calendar.DST_OFFSET)==0 && is_dst_now==common_h.TRUE)
         this_scheduled_log_rotation=(this_scheduled_log_rotation+3600);
      
      /* adjust last rotation time for daylight savings time */
      t.setTimeInMillis( last_scheduled_log_rotation * 1000 );
      if(t.get(Calendar.DST_OFFSET)>0 && is_dst_now==common_h.FALSE)
         last_scheduled_log_rotation=(last_scheduled_log_rotation-3600);
      else if(t.get(Calendar.DST_OFFSET)==0 && is_dst_now==common_h.TRUE)
         last_scheduled_log_rotation=(last_scheduled_log_rotation+3600);
      
      return;
   }
   
   /**********************************************************
    *************** COMMON HTML FUNCTIONS ********************
    **********************************************************/
   
   public static void display_info_table(String title,int refresh, cgiauth_h.authdata current_authdata){
      String date_time;
      int result;
      
      /* read program status */
      result= cgiutils.read_all_status_data( cgiutils.get_cgi_config_location() ,statusdata_h.READ_PROGRAM_STATUS);
      cgiutils.check_blue_server();
      
      System.out.printf("<TABLE CLASS='infoBox' BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
      System.out.printf("<TR><TD CLASS='infoBox'>\n");
      System.out.printf("<DIV CLASS='infoBoxTitle'>%s</DIV>\n",title);
      
      date_time = get_time_string( 0, common_h.LONG_DATE_TIME);
      
      /* Last time this page has been updated */
      System.out.printf("Last Updated: %s<BR>\n",date_time);
      
      /* Refresh Indicator */
      if(refresh==common_h.TRUE)
         System.out.printf("Updated every %d seconds<br>\n",refresh_rate);
      
      /* Project Name, TODO update this with BLUE for X. */
      System.out.printf("Blue&reg; - <A HREF='http://blue.sourceforge.net' TARGET='_new' CLASS='homepageURL'>blue.sourceforge.net</A><BR>\n");
      
      /* Specifies who you are logged in as */
      if(current_authdata!=null)
         System.out.printf("Logged in as <i>%s</i><BR>\n",(current_authdata.username.equals(""))?"?":current_authdata.username);

      /* Display blue process state */
      if(blue_process_state!= blue_h.STATE_OK)
         System.out.printf("<DIV CLASS='infoBoxBadProcStatus'>Warning: Monitoring process may not be running!<BR>Click <A HREF='%s?type=%d'>here</A> for more info.</DIV>",cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PROCESS_INFO);
      
      /* Display status of reading status information */
      if(result==common_h.ERROR)
         System.out.printf("<DIV CLASS='infoBoxBadProcStatus'>Warning: Could not read program status information!</DIV>");
      else{
         /* Indicator for notifications disabled. */
         if(blue.enable_notifications==common_h.FALSE)
            System.out.printf("<DIV CLASS='infoBoxBadProcStatus'>- Notifications are disabled</DIV>");
         
         /* Indicator for service checks disabled. */
         if(blue.execute_service_checks==common_h.FALSE)
            System.out.printf("<DIV CLASS='infoBoxBadProcStatus'>- Service checks are disabled</DIV>");
      }
      
      System.out.printf("</TD></TR>\n");
      System.out.printf("</TABLE>\n");
      
      return;
   }
   
   
   public static void display_nav_table(String url,int archive){
      String date_time;
      String archive_file;
      String archive_basename = null;
      
      if(log_rotation_method!=common_h.LOG_ROTATION_NONE){
         System.out.printf("<table border=0 cellspacing=0 cellpadding=0 CLASS='navBox'>\n");
         System.out.printf("<tr>\n");
         System.out.printf("<td align=center valign=center CLASS='navBoxItem'>\n");
         if(archive==0){
            System.out.printf("Latest Archive<br>");
            System.out.printf("<a href='%sarchive=1'><img src='%s%s' border=0 alt='Latest Archive' title='Latest Archive'></a>",url,url_images_path,cgiutils_h.LEFT_ARROW_ICON);
         }
         else{
            System.out.printf("Earlier Archive<br>");
            System.out.printf("<a href='%sarchive=%d'><img src='%s%s' border=0 alt='Earlier Archive' title='Earlier Archive'></a>",url,archive+1,url_images_path,cgiutils_h.LEFT_ARROW_ICON);
         }
         System.out.printf("</td>\n");
         
         System.out.printf("<td width=15></td>\n");
         
         System.out.printf("<td align=center CLASS='navBoxDate'>\n");
         System.out.printf("<DIV CLASS='navBoxTitle'>Log File Navigation</DIV>\n");
         date_time = get_time_string(last_scheduled_log_rotation,common_h.LONG_DATE_TIME);
         System.out.printf("%s",date_time);
         System.out.printf("<br>to<br>");
         if(archive==0)
            System.out.printf("Present..");
         else{
            date_time = get_time_string(this_scheduled_log_rotation,common_h.LONG_DATE_TIME);
            System.out.printf("%s",date_time);
         }
         System.out.printf("</td>\n");
         
         System.out.printf("<td width=15></td>\n");
         if(archive!=0){
            
            System.out.printf("<td align=center valign=center CLASS='navBoxItem'>\n");
            if(archive==1){
               System.out.printf("Current Log<br>");
               System.out.printf("<a href='%s'><img src='%s%s' border=0 alt='Current Log' title='Current Log'></a>",url,url_images_path,cgiutils_h.RIGHT_ARROW_ICON);
            }
            else{
               System.out.printf("More Recent Archive<br>");
               System.out.printf("<a href='%sarchive=%d'><img src='%s%s' border=0 alt='More Recent Archive' title='More Recent Archive'></a>",url,archive-1,url_images_path,cgiutils_h.RIGHT_ARROW_ICON);
            }
            System.out.printf("</td>\n");
         }
         else
            System.out.printf("<td><img src='%s%s' border=0 width=75 height=1></td>\n",url_images_path,cgiutils_h.EMPTY_ICON);
         
         System.out.printf("</tr>\n");
         
         System.out.printf("</table>\n");
      }
      
      /* get archive to use */
      archive_file = get_log_archive_to_use(archive);
      
      /* cut the pathname for security, and the remaining slash for clarity */
      archive_basename = archive_file;
      if ( archive_basename.lastIndexOf( '/') != -1 )
         archive_basename= archive_basename.substring( archive_file.lastIndexOf( '/') + 1);
      
      /* now it's safe to print the filename */
      System.out.printf("<BR><DIV CLASS='navBoxFile'>File: %s</DIV>\n",archive_basename);
      
      return;
   }
   
   
   
   /* prints the additional notes or action url for a host (with macros substituted) */
   public static void print_extra_host_url(String host_name, String url){
      String input_buffer="";
      StringBuffer output_buffer= new StringBuffer();
      String temp_buffer;
      int in_macro=common_h.FALSE;
      objects_h.host temp_host;
      
      if(host_name==null || url==null)
         return;
      
      temp_host=objects.find_host(host_name);
      if(temp_host==null){
         System.out.printf("%s",url);
         return;
      }
      
      input_buffer = url;
      
      String[] split = input_buffer.split( "[$]" );
      for ( int x = 0; x < split.length; x ++ ) {
         temp_buffer = split[x];
         if(in_macro==common_h.FALSE){
            output_buffer.append( temp_buffer );
            in_macro=common_h.TRUE;
         }
         else{
            
            if(temp_buffer.equals("HOSTNAME"))
               output_buffer.append( url_encode(temp_host.name ));
            
            else if(temp_buffer.equals("HOSTADDRESS"))
               output_buffer.append( (temp_host.address==null)?"":url_encode(temp_host.address) );
            
            in_macro=common_h.FALSE;
         }
      }
      
      System.out.printf("%s",output_buffer);
      
      return;
   }
   
   /* prints the additional notes or action url for a service (with macros substituted) */
   public static void print_extra_service_url(String host_name, String svc_description, String url){
      String input_buffer="";
      StringBuffer output_buffer=new StringBuffer();
      String temp_buffer;
      int in_macro=common_h.FALSE;
      objects_h.service temp_service;
      objects_h.host temp_host;
      
      if(host_name==null || svc_description==null || url==null)
         return;
      
      temp_service= objects.find_service(host_name,svc_description);
      if(temp_service==null){
         System.out.printf("%s",url);
         return;
      }
      
      temp_host=objects.find_host(host_name);
      
      input_buffer = url;
      
      String[] split = input_buffer.split( "[$]" );
      for ( int x = 0; x < split.length; x ++ ) {
         temp_buffer = split[x];
         
         if(in_macro==common_h.FALSE){
            output_buffer.append( temp_buffer );
            in_macro=common_h.TRUE;
         }
         else{
            
            if(temp_buffer.equals("HOSTNAME"))
               output_buffer.append( url_encode(temp_service.host_name) ) ;
            
            else if(temp_buffer.equals("HOSTADDRESS") && temp_host!=null)
               output_buffer.append( (temp_host.address==null)?"":url_encode(temp_host.address));
            
            else if(temp_buffer.equals("SERVICEDESC"))
               output_buffer.append( url_encode(temp_service.description) );
            
            in_macro=common_h.FALSE;
         }
      }
      
      System.out.printf("%s",output_buffer);
      
      return;
   }
   
   /* include user-defined SSI footers or headers */
   public static void include_ssi_files(String cgi_name, int type){
      String common_ssi_file;
      String cgi_ssi_file;
      String raw_cgi_name;
      String stripped_cgi_name;
      
      /* common header or footer */
      common_ssi_file = String.format ( "%scommon-%s.ssi",physical_ssi_path,(type==cgiutils_h.SSI_HEADER)?"header":"footer");
      
      /* CGI-specific header or footer */
      raw_cgi_name = cgi_name;
      stripped_cgi_name= raw_cgi_name.split( "[.]" )[0];
      cgi_ssi_file = String.format( "%s%s-%s.ssi",physical_ssi_path,(stripped_cgi_name==null)?"":stripped_cgi_name,(type==cgiutils_h.SSI_HEADER)?"header":"footer");
      cgi_ssi_file = cgi_ssi_file.toLowerCase();
      
      if(type==cgiutils_h.SSI_HEADER){
         System.out.printf("\n<!-- Produced by Nagios (http://www.nagios.org).  Copyright (c) 1999-2003 Ethan Galstad. -->\n");
         include_ssi_file(common_ssi_file);
         include_ssi_file(cgi_ssi_file);
      }
      else{
         include_ssi_file(cgi_ssi_file);
         include_ssi_file(common_ssi_file);
         System.out.printf("\n<!-- Produced by Nagios (http://www.nagios.org).  Copyright (c) 1999-2003 Ethan Galstad. -->\n");
      }
      
      return;
   }
   
   /* include user-defined SSI footer or header */
   public static void include_ssi_file(String filename){
//    char buffer[MAX_INPUT_BUFFER];
//    FILE *fp;
//    struct stat stat_result;
//    int call_return = 1;
      
      /* TODO if file is executable, we want to run it rather than print it */
//    call_return=stat(filename,&stat_result);
      
//    /* file is executable */
//    if(call_return==0 ) // && (stat_result.st_mode & (S_IXUSR | S_IXGRP | S_IXOTH))){
//    
//    /* must flush output stream first so that output
//    from script shows up in correct place. Other choice
//    is to open program under pipe and copy the data from
//    the program to our output stream.
//    */
//    System.out.flush();
//    
//    /* ignore return status from system call. */
//    call_return=system(filename);
//    
//    return;
//    }
//    
//    /* an error occurred trying to stat() the file */
//    else if(call_return!=0){
//    
//    /* Handle error conditions. Assume that standard posix error codes and errno are available. If not, comment this section out. */
//    switch(errno){
//    case ENOTDIR: /* - A component of the path is not a directory. */
//    case ELOOP: /* Too many symbolic links encountered while traversing the path. */
//    case EFAULT: /* Bad address. */
//    case ENOMEM: /* Out of memory (i.e. kernel memory). */
//    case ENAMETOOLONG: /* File name too long. */
//    System.out.printf("<br /> A stat call returned %d while looking for the file %s.<br />", errno, filename);
//    return;
//    case EACCES: /* Permission denied. -- The file should be accessible by blue. */
//    System.out.printf("<br /> A stat call returned a permissions error(%d) while looking for the file %s.<br />", errno, filename);
//    return;
//    case ENOENT: /* A component of the path file_name does not exist, or the path is an empty string. Just return if the file doesn't exist. */
//    return;
//    default:
//    return;
//    }
//    }
      
      try {
         InputStream in = new FileInputStream(filename);
         
         // Transfer bytes from in to out
         byte[] buf = new byte[1024*512];
         
         int len;
         while ((len = in.read(buf)) > 0) {
            System.out.write(buf, 0, len);
         }
         in.close();
      } catch (IOException ioE ) {
         ;
      }
      return;
   }
   
   
   /* displays an error if CGI config file could not be read */
   public static void cgi_config_file_error(String config_file){
      
      System.out.printf("<H1>Whoops!</H1>\n");
      
      System.out.printf("<P><STRONG><FONT COLOR='RED'>Error: Could not open CGI config file '%s' for reading!</FONT></STRONG></P>\n",config_file);
      
      System.out.printf("<P>\n");
      System.out.printf("Here are some things you should check in order to resolve this error:\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("<OL>\n");
      
      System.out.printf("<LI>Make sure you've installed a CGI config file in its proper location.  See the error message about for details on where the CGI is expecting to find the configuration file.  A sample CGI configuration file (named <b>cgi.cfg</b>) can be found in the <b>sample-config/</b> subdirectory of the Nagios source code distribution.\n");
      System.out.printf("<LI>Make sure the user your web server is running as has permission to read the CGI config file.\n");
      
      System.out.printf("</OL>\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("Make sure you read the documentation on installing and configuring Nagios thoroughly before continuing.  If all else fails, try sending a message to one of the mailing lists.  More information can be found at <a href='http://www.nagios.org'>http://www.nagios.org</a>.\n");
      System.out.printf("</P>\n");
      
      return;
   }
   
   /* displays an error if main config file could not be read */
   public static void main_config_file_error(String config_file){
      
      System.out.printf("<H1>Whoops!</H1>\n");
      
      System.out.printf("<P><STRONG><FONT COLOR='RED'>Error: Could not open main config file '%s' for reading!</FONT></STRONG></P>\n",config_file);
      
      System.out.printf("<P>\n");
      System.out.printf("Here are some things you should check in order to resolve this error:\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("<OL>\n");
      
      System.out.printf("<LI>Make sure you've installed a main config file in its proper location.  See the error message about for details on where the CGI is expecting to find the configuration file.  A sample main configuration file (named <b>blue.cfg</b>) can be found in the <b>sample-config/</b> subdirectory of the Nagios source code distribution.\n");
      System.out.printf("<LI>Make sure the user your web server is running as has permission to read the main config file.\n");
      
      System.out.printf("</OL>\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("Make sure you read the documentation on installing and configuring Nagios thoroughly before continuing.  If all else fails, try sending a message to one of the mailing lists.  More information can be found at <a href='http://www.nagios.org'>http://www.nagios.org</a>.\n");
      System.out.printf("</P>\n");
      
      return;
   }
   
   
   /* displays an error if object data could not be read */
   public static void object_data_error(){
      
      System.out.printf("<H1>Whoops!</H1>\n");
      
      System.out.printf("<P><STRONG><FONT COLOR='RED'>Error: Could not read object configuration data!</FONT></STRONG></P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("Here are some things you should check in order to resolve this error:\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("<OL>\n");
      
      System.out.printf("<LI>Verify configuration options using the <b>-v</b> command-line option to check for errors.\n");
      System.out.printf("<LI>Check the Nagios log file for messages relating to startup or status data errors.\n");
      System.out.printf("<LI>Make sure you've compiled the main program and the CGIs to use the same object data storage options (i.e. default text file or template-based file).\n");
      
      System.out.printf("</OL>\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("Make sure you read the documentation on installing, configuring and running Nagios thoroughly before continuing.  If all else fails, try sending a message to one of the mailing lists.  More information can be found at <a href='http://www.nagios.org'>http://www.nagios.org</a>.\n");
      System.out.printf("</P>\n");
      
      return;
   }
   
   
   /* displays an error if status data could not be read */
   public static void status_data_error(){
      
      System.out.printf("<H1>Whoops!</H1>\n");
      
      System.out.printf("<P><STRONG><FONT COLOR='RED'>Error: Could not read host and service status information!</FONT></STRONG></P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("The most common cause of this error message (especially for new users), is the fact that Nagios is not actually running.  If Nagios is indeed not running, this is a normal error message.  It simply indicates that the CGIs could not obtain the current status of hosts and services that are being monitored.  If you've just installed things, make sure you read the documentation on starting Nagios.\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("Some other things you should check in order to resolve this error include:\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("<OL>\n");
      
      System.out.printf("<LI>Check the Blue log file for messages relating to startup or status data errors.\n");
      System.out.printf("<LI>Always verify configuration options using the <b>-v</b> command-line option before starting or restarting Nagios!\n");
      System.out.printf("<LI>Make sure you've compiled the main program and the CGIs to use the same status data storage options (i.e. text file or database).  If the main program is storing status data in a text file and the CGIs are trying to read status data from a database, you'll have problems.\n");
      
      System.out.printf("</OL>\n");
      System.out.printf("</P>\n");
      
      System.out.printf("<P>\n");
      System.out.printf("Make sure you read the documentation on installing, configuring and running Nagios thoroughly before continuing.  If all else fails, try sending a message to one of the mailing lists.  More information can be found at <a href='http://www.nagios.org'>http://www.nagios.org</a>.\n");
      System.out.printf("</P>\n");
      
      return;
   }
   
   /* displays context-sensitive help window */
   public static void display_context_help(String chid){
      String icon= cgiutils_h.CONTEXT_HELP_ICON1;
      
      if(show_context_help==common_h.FALSE)
         return;
      
      /* change icon if necessary */
      if(chid.equals(cgiutils_h.CONTEXTHELP_TAC))
         icon=cgiutils_h.CONTEXT_HELP_ICON2;
      
      System.out.printf("<a href='%s%s.html' target='cshw' onClick='javascript:window.open(\"%s%s.html\",\"cshw\",\"width=550,height=600,toolbar=0,location=0,status=0,resizable=1,scrollbars=1\");return true'><img src='%s%s' border=0 alt='Display context-sensitive help for this screen' title='Display context-sensitive help for this screen'></a>\n",url_context_help_path,chid,url_context_help_path,chid,url_images_path,icon);
      
      return;
   }
   
   private static int atoi(String value) {
      try {
         return Integer.parseInt(value);
      } catch ( NumberFormatException nfE ) {
//       logger.throwing( cn, "atoi", nfE);
         return 0;
      }
   }
}

