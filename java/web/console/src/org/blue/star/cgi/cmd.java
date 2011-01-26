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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.blue.star.base.utils;
import org.blue.star.common.comments;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.include.downtime_h;
import org.blue.star.include.objects_h;

public class cmd extends blue_servlet {
    
public static final int MAX_AUTHOR_LENGTH	= 64;
public static final int MAX_COMMENT_LENGTH	= 1024;

public static final int HTML_CONTENT   = 0;
public static final int WML_CONTENT    = 1;

public static String host_name="";
public static String hostgroup_name="";
public static String servicegroup_name="";
public static String service_desc="";
public static String comment_author="";
public static String comment_data="";
public static String start_time_string="";
public static String end_time_string="";

public static long comment_id=0;
public static long downtime_id=0;
public static int notification_delay=0;
public static int schedule_delay=0;
public static int persistent_comment=common_h.FALSE;
public static int sticky_ack=common_h.FALSE;
public static int send_notification=common_h.FALSE;
public static int force_check=common_h.FALSE;
public static int plugin_state=cgiutils_h.STATE_OK;
public static String plugin_output="";
public static String performance_data="";
public static long start_time=0L;
public static long end_time=0L;
public static int affect_host_and_services=common_h.FALSE;
public static int propagate_to_children=common_h.FALSE;
public static int fixed=common_h.FALSE;
public static long duration=0L;
public static long triggered_by=0L;
public static int child_options=0;

public static int command_type=common_h.CMD_NONE;
public static int command_mode=cgiutils_h.CMDMODE_REQUEST;

public static int content_type=HTML_CONTENT;

public static int display_header=common_h.TRUE;

public static cgiauth_h.authdata current_authdata = new cgiauth_h.authdata();

public void call_main() {
   main( null );
}

public void reset_context() {
   host_name="";
   hostgroup_name="";
   servicegroup_name="";
   service_desc="";
   comment_author="";
   comment_data="";
   start_time_string="";
   end_time_string="";
   
   comment_id=0;
   downtime_id=0;
   notification_delay=0;
   schedule_delay=0;
   persistent_comment=common_h.FALSE;
   sticky_ack=common_h.FALSE;
   send_notification=common_h.FALSE;
   force_check=common_h.FALSE;
   plugin_state=cgiutils_h.STATE_OK;
   plugin_output="";
   performance_data="";
   start_time=0L;
   end_time=0L;
   affect_host_and_services=common_h.FALSE;
   propagate_to_children=common_h.FALSE;
   fixed=common_h.FALSE;
   duration=0L;
   triggered_by=0L;
   child_options=0;
   
   command_type=common_h.CMD_NONE;
   command_mode=cgiutils_h.CMDMODE_REQUEST;
   
   content_type=HTML_CONTENT;
   
   display_header=common_h.TRUE;
   
   current_authdata = new cgiauth_h.authdata();
}

public static void main(String[] args){
	int result=common_h.OK;
    
    /* get the arguments passed in the URL */
    process_cgivars();	
	
    /* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR)
	{
		document_header(common_h.FALSE);
		if(content_type==WML_CONTENT)
			System.out.printf("<p>Error: Could not open CGI config file!</p>\n");
		else
            cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
	}

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		if(content_type== WML_CONTENT)
			System.out.printf("<p>Error: Could not open main config file!</p>\n");
		else
            cgiutils.main_config_file_error(cgiutils.main_config_file);
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* This requires the date_format parameter in the main config file */
	if ( start_time_string != null && start_time_string.length() != 0 )
        start_time = string_to_time(start_time_string);

    if ( end_time_string != null && end_time_string.length() != 0 )
        end_time = string_to_time(end_time_string );


	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file, common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		if(content_type==WML_CONTENT)
			System.out.printf("<p>Error: Could not read object config data!</p>\n");
		else
			cgiutils.object_data_error();
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
                }

    document_header(common_h.TRUE);

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%%>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top width=33%%>\n");
		cgiutils.display_info_table("External Command Interface",common_h.FALSE,current_authdata);
		System.out.printf("</td>\n");

		/* center column of the first row */
		System.out.printf("<td align=center valign=top width=33%%>\n");
		System.out.printf("</td>\n");
		
		/* right column of the first row */
		System.out.printf("<td align=right valign=bottom width=33%%>\n");
		
		/* display context-sensitive help */
		if(command_mode==cgiutils_h.CMDMODE_COMMIT)
		   cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CMD_COMMIT);
		else
		   cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CMD_INPUT);
		
		System.out.printf("</td>\n");
		
		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");
	}
	
	/* if no command was specified... */
	if(command_type==common_h.CMD_NONE){
	   if(content_type==WML_CONTENT)
	      System.out.printf("<p>Error: No command specified!</p>\n");
	   else
	      System.out.printf("<P><DIV CLASS='errorMessage'>Error: No command was specified</DIV></P>\n");
	}
	
	/* if this is the first request for a command, present option */
	else if(command_mode==cgiutils_h.CMDMODE_REQUEST)
	   request_command_data(command_type);
	
	/* the user wants to commit the command */
	else if(command_mode==cgiutils_h.CMDMODE_COMMIT)
	   commit_command_data(command_type);
	
	document_footer();
	
	cgiutils.exit(  common_h.OK );
}



public static void document_header(int use_stylesheet){

	if(content_type==WML_CONTENT){

	   if ( response != null ) 
          response.setContentType( "text/vnd.wap.wml" );
       else 
          System.out.printf("Content-type: text/vnd.wap.wml\r\n\r\n");

		System.out.printf("<?xml version=\"1.0\"?>\n");
		System.out.printf("<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">\n");

		System.out.printf("<wml>\n");

		System.out.printf("<card id='card1' title='Command Results'>\n");
	        }

	else{

       if ( response != null ) 
          response.setContentType( "text/html" );
       else 
		System.out.printf("Content-type: text/html\r\n\r\n");

		System.out.printf("<html>\n");
		System.out.printf("<head>\n");
		System.out.printf("<title>\n");
		System.out.printf("External Command Interface\n");
		System.out.printf("</title>\n");

		if(use_stylesheet==common_h.TRUE){
			System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
			System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMAND_CSS);
		        }

		System.out.printf("</head>\n");

		System.out.printf("<body CLASS='cmd'>\n");

		/* include user SSI header */
        cgiutils.include_ssi_files(cgiutils_h.COMMAND_CGI,cgiutils_h.SSI_HEADER);
	        }

	return;
        }


public static void document_footer(){

	if(content_type==WML_CONTENT){
		System.out.printf("</card>\n");
		System.out.printf("</wml>\n");
	        }

	else{

		/* include user SSI footer */
        cgiutils.include_ssi_files(cgiutils_h.COMMAND_CGI,cgiutils_h.SSI_FOOTER);

		System.out.printf("</body>\n");
		System.out.printf("</html>\n");
	        }

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars( request_string );
    
	for(x=0; x < variables.length ;x++){
		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the command type */
		else if(variables[x].equals("cmd_typ")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			command_type=atoi(variables[x]);
		        }

		/* we found the command mode */
		else if(variables[x].equals("cmd_mod")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			command_mode=atoi(variables[x]);
		        }

		/* we found the comment id */
		else if(variables[x].equals("com_id")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			comment_id=strtoul(variables[x],null,10);
		        }

		/* we found the downtime id */
		else if(variables[x].equals("down_id")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			downtime_id=strtoul(variables[x],null,10);
		        }

		/* we found the notification delay */
		else if(variables[x].equals("not_dly")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			notification_delay=atoi(variables[x]);
		        }

		/* we found the schedule delay */
		else if(variables[x].equals("sched_dly")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			schedule_delay=atoi(variables[x]);
		        }

		/* we found the comment author */
		else if(variables[x].equals("com_author")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				comment_author = variables[x];
			}

		/* we found the comment data */
		else if(variables[x].equals("com_data")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				comment_data = variables[x];
			}

		/* we found the host name */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				host_name = variables[x];
			}

		/* we found the hostgroup name */
		else if(variables[x].equals("hostgroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				hostgroup_name = variables[x];
			}

		/* we found the service name */
		else if(variables[x].equals("service")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				service_desc = variables[x];
			}

		/* we found the servicegroup name */
		else if(variables[x].equals("servicegroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				servicegroup_name = variables[x];
			}

		/* we got the persistence option for a comment */
		else if(variables[x].equals("persistent"))
			persistent_comment=common_h.TRUE;

		/* we got the notification option for an acknowledgement */
		else if(variables[x].equals("send_notification"))
			send_notification=common_h.TRUE;

		/* we got the acknowledgement type */
		else if(variables[x].equals("sticky_ack"))
			sticky_ack=common_h.TRUE;

		/* we got the service check force option */
		else if(variables[x].equals("force_check"))
			force_check=common_h.TRUE;

		/* we got the option to affect host and all its services */
		else if(variables[x].equals("ahas"))
			affect_host_and_services=common_h.TRUE;

		/* we got the option to propagate to child hosts */
		else if(variables[x].equals("ptc"))
			propagate_to_children=common_h.TRUE;

		/* we got the option for fixed downtime */
		else if(variables[x].equals("fixed")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			fixed=(atoi(variables[x])>0)?common_h.TRUE:common_h.FALSE;
		        }

		/* we got the triggered by downtime option */
		else if(variables[x].equals("trigger")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			triggered_by=strtoul(variables[x],null,10);
		        }

		/* we got the child options */
		else if(variables[x].equals("childoptions")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			child_options=atoi(variables[x]);
		        }

		/* we found the plugin output */
		else if(variables[x].equals("plugin_output")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			/* protect against buffer overflows */
			if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
				error=common_h.TRUE;
				break;
			        }
			else
				plugin_output  = variables[x];
			}

		/* we found the performance data */
		else if(variables[x].equals("performance_data")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			/* protect against buffer overflows */
			if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
				error=common_h.TRUE;
				break;
			        }
			else
				performance_data = variables[x];
			}

		/* we found the plugin state */
		else if(variables[x].equals("plugin_state")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			plugin_state=atoi(variables[x]);
		        }

		/* we found the hour duration */
		else if(variables[x].equals("hours")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(atoi(variables[x])<0){
				error=common_h.TRUE;
				break;
			        }
			duration+=(atoi(variables[x])*3600);
		        }

		/* we found the minute duration */
		else if(variables[x].equals("minutes")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(atoi(variables[x])<0){
				error=common_h.TRUE;
				break;
			        }
			duration+=(atoi(variables[x])*60);
		        }

		/* we found the start time */
		else if(variables[x].equals("start_time")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				start_time_string = variables[x];
		        }

		/* we found the end time */
		else if(variables[x].equals("end_time")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				end_time_string = variables[x];
		        }

		/* we found the content type argument */
		else if(variables[x].equals("content")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			if(variables[x].equals("wml")){
				content_type=WML_CONTENT;
				display_header=common_h.FALSE;
			        }
			else
				content_type=HTML_CONTENT;
		        }
                }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



public static void request_command_data(int cmd){
	long t;
	String start_time;
    String buffer;
	objects_h.contact temp_contact;
	downtime_h.scheduled_downtime temp_downtime;


	/* get default name to use for comment author */
	temp_contact=objects.find_contact(current_authdata.username);
	if(temp_contact!=null && temp_contact.alias!=null)
		comment_author=temp_contact.alias;
	else
		comment_author=current_authdata.username;


	System.out.printf("<P><DIV ALIGN=CENTER CLASS='cmdType'>You are requesting to ");

	switch(cmd){

	case common_h.CMD_ADD_HOST_COMMENT:
	case common_h.CMD_ADD_SVC_COMMENT:
		System.out.printf("add a %s comment",(cmd==common_h.CMD_ADD_HOST_COMMENT)?"host":"service");
		break;

	case common_h.CMD_DEL_HOST_COMMENT:
	case common_h.CMD_DEL_SVC_COMMENT:
		System.out.printf("delete a %s comment",(cmd==common_h.CMD_DEL_HOST_COMMENT)?"host":"service");
		break;
		
	case common_h.CMD_DELAY_HOST_NOTIFICATION:
	case common_h.CMD_DELAY_SVC_NOTIFICATION:
		System.out.printf("delay a %s notification",(cmd==common_h.CMD_DELAY_HOST_NOTIFICATION)?"host":"service");
		break;

	case common_h.CMD_SCHEDULE_SVC_CHECK:
		System.out.printf("schedule a service check");
		break;

	case common_h.CMD_ENABLE_SVC_CHECK:
	case common_h.CMD_DISABLE_SVC_CHECK:
		System.out.printf("%s actice checks of a particular service",(cmd==common_h.CMD_ENABLE_SVC_CHECK)?"enable":"disable");
		break;
		
	case common_h.CMD_ENABLE_NOTIFICATIONS:
	case common_h.CMD_DISABLE_NOTIFICATIONS:
		System.out.printf("%s notifications",(cmd==common_h.CMD_ENABLE_NOTIFICATIONS)?"enable":"disable");
		break;
		
	case common_h.CMD_SHUTDOWN_PROCESS:
	case common_h.CMD_RESTART_PROCESS:
		System.out.printf("%s the Blue process",(cmd==common_h.CMD_SHUTDOWN_PROCESS)?"shutdown":"restart");
		break;

	case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
		System.out.printf("%s active checks of all services on a host",(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS)?"enable":"disable");
		break;

	case common_h.CMD_SCHEDULE_HOST_SVC_CHECKS:
		System.out.printf("schedule a check of all services for a host");
		break;

	case common_h.CMD_DEL_ALL_HOST_COMMENTS:
	case common_h.CMD_DEL_ALL_SVC_COMMENTS:
		System.out.printf("delete all comments for a %s",(cmd==common_h.CMD_DEL_ALL_HOST_COMMENTS)?"host":"service");
		break;

	case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
		System.out.printf("%s notifications for a service",(cmd==common_h.CMD_ENABLE_SVC_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
		System.out.printf("%s notifications for a host",(cmd==common_h.CMD_ENABLE_HOST_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
	case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
		System.out.printf("%s notifications for all hosts and services beyond a host",(cmd==common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
		System.out.printf("%s notifications for all services on a host",(cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM:
	case common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM:
		System.out.printf("acknowledge a %s problem",(cmd==common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM)?"host":"service");
		break;

	case common_h.CMD_START_EXECUTING_SVC_CHECKS:
	case common_h.CMD_STOP_EXECUTING_SVC_CHECKS:
		System.out.printf("%s executing active service checks",(cmd==common_h.CMD_START_EXECUTING_SVC_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS:
		System.out.printf("%s accepting passive service checks",(cmd==common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
		System.out.printf("%s accepting passive service checks for a particular service",(cmd==common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_ENABLE_EVENT_HANDLERS:
	case common_h.CMD_DISABLE_EVENT_HANDLERS:
		System.out.printf("%s event handlers",(cmd==common_h.CMD_ENABLE_EVENT_HANDLERS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
	case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
		System.out.printf("%s the event handler for a particular host",(cmd==common_h.CMD_ENABLE_HOST_EVENT_HANDLER)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
	case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
		System.out.printf("%s the event handler for a particular service",(cmd==common_h.CMD_ENABLE_SVC_EVENT_HANDLER)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOST_CHECK:
	case common_h.CMD_DISABLE_HOST_CHECK:
		System.out.printf("%s active checks of a particular host",(cmd==common_h.CMD_ENABLE_HOST_CHECK)?"enable":"disable");
		break;

	case common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS:
	case common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS:
		System.out.printf("%s obsessing over service checks",(cmd==common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS)?"stop":"start");
		break;

	case common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT:
	case common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT:
		System.out.printf("remove a %s acknowledgement",(cmd==common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT)?"host":"service");
		break;

	case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
	case common_h.CMD_SCHEDULE_SVC_DOWNTIME:
		System.out.printf("schedule downtime for a particular %s",(cmd==common_h.CMD_SCHEDULE_HOST_DOWNTIME)?"host":"service");
		break;

	case common_h.CMD_PROCESS_HOST_CHECK_RESULT:
	case common_h.CMD_PROCESS_SERVICE_CHECK_RESULT:
		System.out.printf("submit a passive check result for a particular %s",(cmd==common_h.CMD_PROCESS_HOST_CHECK_RESULT)?"host":"service");
		break;

	case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
	case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
		System.out.printf("%s flap detection for a particular host",(cmd==common_h.CMD_ENABLE_HOST_FLAP_DETECTION)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
	case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
		System.out.printf("%s flap detection for a particular service",(cmd==common_h.CMD_ENABLE_SVC_FLAP_DETECTION)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_FLAP_DETECTION:
	case common_h.CMD_DISABLE_FLAP_DETECTION:
		System.out.printf("%s flap detection for hosts and services",(cmd==common_h.CMD_ENABLE_FLAP_DETECTION)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
		System.out.printf("%s notifications for all services in a particular hostgroup",(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
		System.out.printf("%s notifications for all hosts in a particular hostgroup",(cmd==common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
		System.out.printf("%s active checks of all services in a particular hostgroup",(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS)?"enable":"disable");
		break;

	case common_h.CMD_DEL_HOST_DOWNTIME:
	case common_h.CMD_DEL_SVC_DOWNTIME:
		System.out.printf("cancel scheduled downtime for a particular %s",(cmd==common_h.CMD_DEL_HOST_DOWNTIME)?"host":"service");
		break;

	case common_h.CMD_ENABLE_FAILURE_PREDICTION:
	case common_h.CMD_DISABLE_FAILURE_PREDICTION:
		System.out.printf("%s failure prediction for hosts and service",(cmd==common_h.CMD_ENABLE_FAILURE_PREDICTION)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_PERFORMANCE_DATA:
	case common_h.CMD_DISABLE_PERFORMANCE_DATA:
		System.out.printf("%s performance data processing for hosts and services",(cmd==common_h.CMD_ENABLE_PERFORMANCE_DATA)?"enable":"disable");
		break;

	case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
		System.out.printf("schedule downtime for all hosts in a particular hostgroup");
		break;

	case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME:
		System.out.printf("schedule downtime for all services in a particular hostgroup");
		break;

	case common_h.CMD_START_EXECUTING_HOST_CHECKS:
	case common_h.CMD_STOP_EXECUTING_HOST_CHECKS:
		System.out.printf("%s executing host checks",(cmd==common_h.CMD_START_EXECUTING_HOST_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS:
		System.out.printf("%s accepting passive host checks",(cmd==common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
		System.out.printf("%s accepting passive checks for a particular host",(cmd==common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS:
		System.out.printf("%s obsessing over host checks",(cmd==common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS)?"start":"stop");
		break;

	case common_h.CMD_SCHEDULE_HOST_CHECK:
		System.out.printf("schedule a host check");
		break;

	case common_h.CMD_START_OBSESSING_OVER_SVC:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC:
		System.out.printf("%s obsessing over a particular service",(cmd==common_h.CMD_START_OBSESSING_OVER_SVC)?"start":"stop");
		break;

	case common_h.CMD_START_OBSESSING_OVER_HOST:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST:
		System.out.printf("%s obsessing over a particular host",(cmd==common_h.CMD_START_OBSESSING_OVER_HOST)?"start":"stop");
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
		System.out.printf("%s notifications for all services in a particular servicegroup",(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
		System.out.printf("%s notifications for all hosts in a particular servicegroup",(cmd==common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS)?"enable":"disable");
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
		System.out.printf("%s active checks of all services in a particular servicegroup",(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS)?"enable":"disable");
		break;

	case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
		System.out.printf("schedule downtime for all hosts in a particular servicegroup");
		break;

	case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:
		System.out.printf("schedule downtime for all services in a particular servicegroup");
		break;

	default:
		System.out.printf("execute an unknown command.  Shame on you!</DIV>");
		return;
	        }

	System.out.printf("</DIV></p>\n");

	System.out.printf("<p>\n");
	System.out.printf("<div align='center'>\n");

	System.out.printf("<table border=0 width=90%%>\n");
	System.out.printf("<tr>\n");
	System.out.printf("<td align=center valign=top>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='optBoxTitle'>Command Options</DIV>\n");

	System.out.printf("<TABLE CELLSPACING=0 CELLPADDING=0 BORDER=1 CLASS='optBox'>\n");
	System.out.printf("<TR><TD CLASS='optBoxItem'>\n");
	System.out.printf("<form method='post' action='%s'>\n", cgiutils_h.COMMAND_CGI);
	System.out.printf("<TABLE CELLSPACING=0 CELLPADDING=0 CLASS='optBox'>\n");

	System.out.printf("<tr><td><INPUT TYPE='HIDDEN' NAME='cmd_typ' VALUE='%d'><INPUT TYPE='HIDDEN' NAME='cmd_mod' VALUE='%d'></td></tr>\n",cmd,cgiutils_h.CMDMODE_COMMIT);

	switch(cmd){

	case common_h.CMD_ADD_HOST_COMMENT:
	case common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM){
			System.out.printf("<tr><td CLASS='optBoxItem'>Sticky Acknowledgement:</td><td><b>");
			System.out.printf("<INPUT TYPE='checkbox' NAME='sticky_ack' CHECKED>");
			System.out.printf("</b></td></tr>\n");
			System.out.printf("<tr><td CLASS='optBoxItem'>Send Notification:</td><td><b>");
			System.out.printf("<INPUT TYPE='checkbox' NAME='send_notification' CHECKED>");
			System.out.printf("</b></td></tr>\n");
		        }
		System.out.printf("<tr><td CLASS='optBoxItem'>Persistent%s:</td><td><b>",(cmd==common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM)?" Comment":"");
		System.out.printf("<INPUT TYPE='checkbox' NAME='persistent' CHECKED>");
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Author (Your Name):</td><td><b>");
		System.out.printf("<INPUT TYPE'TEXT' NAME='com_author' VALUE='%s'>",comment_author);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Comment:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_data' VALUE='%s' SIZE=40>",comment_data);
		System.out.printf("</b></td></tr>\n");
		break;
		
	case common_h.CMD_ADD_SVC_COMMENT:
	case common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Service:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='service' VALUE='%s'>",service_desc);
		if(cmd==common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM){
			System.out.printf("<tr><td CLASS='optBoxItem'>Sticky Acknowledgement:</td><td><b>");
			System.out.printf("<INPUT TYPE='checkbox' NAME='sticky_ack' CHECKED>");
			System.out.printf("</b></td></tr>\n");
			System.out.printf("<tr><td CLASS='optBoxItem'>Send Notification:</td><td><b>");
			System.out.printf("<INPUT TYPE='checkbox' NAME='send_notification' CHECKED>");
			System.out.printf("</b></td></tr>\n");
		        }
		System.out.printf("<tr><td CLASS='optBoxItem'>Persistent%s:</td><td><b>",(cmd==common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM)?" Comment":"");
		System.out.printf("<INPUT TYPE='checkbox' NAME='persistent' CHECKED>");
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Author (Your Name):</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_author' VALUE='%s'>",comment_author);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Comment:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_data' VALUE='%s' SIZE=40>",comment_data);
		System.out.printf("</b></td></tr>\n");
		break;

	case common_h.CMD_DEL_HOST_COMMENT:
	case common_h.CMD_DEL_SVC_COMMENT:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Comment ID:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_id' VALUE='%d'>",comment_id);
		System.out.printf("</b></td></tr>\n");
		break;
		
	case common_h.CMD_DELAY_HOST_NOTIFICATION:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Notification Delay (minutes from now):</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='not_dly' VALUE='%d'>",notification_delay);
		System.out.printf("</b></td></tr>\n");
		break;

	case common_h.CMD_DELAY_SVC_NOTIFICATION:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Service:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='service' VALUE='%s'>",service_desc);
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Notification Delay (minutes from now):</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='not_dly' VALUE='%d'>",notification_delay);
		System.out.printf("</b></td></tr>\n");
		break;

	case common_h.CMD_SCHEDULE_SVC_CHECK:
	case common_h.CMD_SCHEDULE_HOST_CHECK:
	case common_h.CMD_SCHEDULE_HOST_SVC_CHECKS:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_SCHEDULE_SVC_CHECK){
			System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Service:</td><td><b>");
			System.out.printf("<INPUT TYPE='TEXT' NAME='service' VALUE='%s'>",service_desc);
			System.out.printf("</b></td></tr>\n");
		        }
		t = utils.currentTimeInSeconds();
		buffer = cgiutils.get_time_string(t, common_h.SHORT_DATE_TIME);
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Check Time:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='start_time' VALUE='%s'>",buffer);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxItem'>Force Check:</td><td><b>");
		System.out.printf("<INPUT TYPE='checkbox' NAME='force_check' CHECKED>");
		System.out.printf("</b></td></tr>\n");
		break;

	case common_h.CMD_ENABLE_SVC_CHECK:
	case common_h.CMD_DISABLE_SVC_CHECK:
	case common_h.CMD_DEL_ALL_SVC_COMMENTS:
	case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
	case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
	case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
	case common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT:
	case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
	case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
	case common_h.CMD_START_OBSESSING_OVER_SVC:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Service:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='service' VALUE='%s'>",service_desc);
		System.out.printf("</b></td></tr>\n");
		break;
		
	case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
	case common_h.CMD_DEL_ALL_HOST_COMMENTS:
	case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
	case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
	case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
	case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
	case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
	case common_h.CMD_ENABLE_HOST_CHECK:
	case common_h.CMD_DISABLE_HOST_CHECK:
	case common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT:
	case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
	case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
	case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
	case common_h.CMD_START_OBSESSING_OVER_HOST:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS || cmd==common_h.CMD_DISABLE_HOST_SVC_CHECKS || cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS || cmd==common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS){
			System.out.printf("<tr><td CLASS='optBoxItem'>%s For Host Too:</td><td><b>",(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS || cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS)?"Enable":"Disable");
			System.out.printf("<INPUT TYPE='checkbox' NAME='ahas'>");
			System.out.printf("</b></td></tr>\n");
		        }
		if(cmd==common_h.CMD_ENABLE_HOST_NOTIFICATIONS || cmd==common_h.CMD_DISABLE_HOST_NOTIFICATIONS){
			System.out.printf("<tr><td CLASS='optBoxItem'>%s Notifications For Child Hosts Too:</td><td><b>",(cmd==common_h.CMD_ENABLE_HOST_NOTIFICATIONS)?"Enable":"Disable");
			System.out.printf("<INPUT TYPE='checkbox' NAME='ptc'>");
			System.out.printf("</b></td></tr>\n");
		        }
		break;

	case common_h.CMD_ENABLE_NOTIFICATIONS:
	case common_h.CMD_DISABLE_NOTIFICATIONS:
	case common_h.CMD_SHUTDOWN_PROCESS:
	case common_h.CMD_RESTART_PROCESS:
	case common_h.CMD_START_EXECUTING_SVC_CHECKS:
	case common_h.CMD_STOP_EXECUTING_SVC_CHECKS:
	case common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS:
	case common_h.CMD_ENABLE_EVENT_HANDLERS:
	case common_h.CMD_DISABLE_EVENT_HANDLERS:
	case common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS:
	case common_h.CMD_ENABLE_FLAP_DETECTION:
	case common_h.CMD_DISABLE_FLAP_DETECTION:
	case common_h.CMD_ENABLE_FAILURE_PREDICTION:
	case common_h.CMD_DISABLE_FAILURE_PREDICTION:
	case common_h.CMD_ENABLE_PERFORMANCE_DATA:
	case common_h.CMD_DISABLE_PERFORMANCE_DATA:
	case common_h.CMD_START_EXECUTING_HOST_CHECKS:
	case common_h.CMD_STOP_EXECUTING_HOST_CHECKS:
	case common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS:
	case common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS:
		System.out.printf("<tr><td CLASS='optBoxItem' colspan=2>There are no options for this command.<br>Click the 'Commit' button to submit the command.</td></tr>");
		break;
		
	case common_h.CMD_PROCESS_HOST_CHECK_RESULT:
	case common_h.CMD_PROCESS_SERVICE_CHECK_RESULT:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_PROCESS_SERVICE_CHECK_RESULT){
			System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Service:</td><td><b>");
			System.out.printf("<INPUT TYPE='TEXT' NAME='service' VALUE='%s'>",service_desc);
			System.out.printf("</b></td></tr>\n");
		        }
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Check Result:</td><td><b>");
		System.out.printf("<SELECT NAME='plugin_state'>");
		if(cmd==common_h.CMD_PROCESS_SERVICE_CHECK_RESULT){
			System.out.printf("<OPTION VALUE=%d SELECTED>OK\n",cgiutils_h.STATE_OK);
			System.out.printf("<OPTION VALUE=%d>WARNING\n",cgiutils_h.STATE_WARNING);
			System.out.printf("<OPTION VALUE=%d>UNKNOWN\n",cgiutils_h.STATE_UNKNOWN);
			System.out.printf("<OPTION VALUE=%d>CRITICAL\n",cgiutils_h.STATE_CRITICAL);
		        }
		else{
			System.out.printf("<OPTION VALUE=0 SELECTED>UP\n");
			System.out.printf("<OPTION VALUE=1>DOWN\n");
			System.out.printf("<OPTION VALUE=2>UNREACHABLE\n");
		        }
		System.out.printf("</SELECT>\n");
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Check Output:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='plugin_output' VALUE=''>");
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxItem'>Performance Data:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='performance_data' VALUE=''>");
		System.out.printf("</b></td></tr>\n");
		break;
		
	case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
	case common_h.CMD_SCHEDULE_SVC_DOWNTIME:

		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Host Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='host' VALUE='%s'>",host_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_SCHEDULE_SVC_DOWNTIME){
			System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Service:</td><td><b>");
			System.out.printf("<INPUT TYPE='TEXT' NAME='service' VALUE='%s'>",service_desc);
		        }
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Author (Your Name):</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_author' VALUE='%s'>",comment_author);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Comment:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_data' VALUE='%s' SIZE=40>",comment_data);
		System.out.printf("</b></td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'><br></td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'>Triggered By:</td><td>\n");
		System.out.printf("<select name='trigger'>\n");
		System.out.printf("<option value='0'>N/A\n");

		/* read scheduled downtime */
		downtime.read_downtime_data(cgiutils.get_cgi_config_location());
		for( Iterator iter = downtime.scheduled_downtime_list.iterator(); iter.hasNext();  ){
            temp_downtime = (downtime_h.scheduled_downtime) iter.next();
            
			if(temp_downtime.type!=common_h.HOST_DOWNTIME)
				continue;
			System.out.printf("<option value='%d'>",temp_downtime.downtime_id);
            start_time = cgiutils.get_time_string( temp_downtime.start_time, common_h.SHORT_DATE_TIME);
			System.out.printf("ID: %d, Host '%s' starting @ %s\n",temp_downtime.downtime_id,temp_downtime.host_name,start_time);
		        }
		for( Iterator iter = downtime.scheduled_downtime_list.iterator(); iter.hasNext();  ){
            temp_downtime = (downtime_h.scheduled_downtime) iter.next();
            
			if(temp_downtime.type!=common_h.SERVICE_DOWNTIME)
				continue;
			System.out.printf("<option value='%d'>",temp_downtime.downtime_id);
            start_time = cgiutils.get_time_string(temp_downtime.start_time, common_h.SHORT_DATE_TIME);
			System.out.printf("ID: %d, Service '%s' on host '%s' starting @ %s \n",temp_downtime.downtime_id,temp_downtime.service_description,temp_downtime.host_name,start_time);
		        }

		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'><br></td></tr>\n");

		t = utils.currentTimeInSeconds();
        buffer = cgiutils.get_time_string( t, common_h.SHORT_DATE_TIME);
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Start Time:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='start_time' VALUE='%s'>",buffer);
		System.out.printf("</b></td></tr>\n");
		t+=7200;
        buffer = cgiutils.get_time_string( t, common_h.SHORT_DATE_TIME);
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>End Time:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='end_time' VALUE='%s'>",buffer);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxItem'>Type:</td><td><b>");
		System.out.printf("<SELECT NAME='fixed'>");
		System.out.printf("<OPTION VALUE=1>Fixed\n");
		System.out.printf("<OPTION VALUE=0>Flexible\n");
		System.out.printf("</SELECT>\n");
		System.out.printf("</b></td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'>If Flexible, Duration:</td><td>");
		System.out.printf("<table border=0><tr>\n");
		System.out.printf("<td align=right><INPUT TYPE='TEXT' NAME='hours' VALUE='2' SIZE=2 MAXLENGTH=2></td>\n");
		System.out.printf("<td align=left>Hours</td>\n");
		System.out.printf("<td align=right><INPUT TYPE='TEXT' NAME='minutes' VALUE='0' SIZE=2 MAXLENGTH=2></td>\n");
		System.out.printf("<td align=left>Minutes</td>\n");
		System.out.printf("</tr></table>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'><br></td></tr>\n");

		if(cmd==common_h.CMD_SCHEDULE_HOST_DOWNTIME){
			System.out.printf("<tr><td CLASS='optBoxItem'>Child Hosts:</td><td><b>");
			System.out.printf("<SELECT name='childoptions'>");
			System.out.printf("<option value='0'>Do nothing with child hosts\n");
			System.out.printf("<option value='1'>Schedule triggered downtime for all child hosts\n");
			System.out.printf("<option value='2'>Schedule non-triggered downtime for all child hosts\n");
			System.out.printf("</SELECT>\n");
			System.out.printf("</b></td></tr>\n");
		        }

		System.out.printf("<tr><td CLASS='optBoxItem'><br></td></tr>\n");

		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Hostgroup Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='hostgroup' VALUE='%s'>",hostgroup_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS || cmd==common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS || cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS || cmd==common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS){
			System.out.printf("<tr><td CLASS='optBoxItem'>%s For Hosts Too:</td><td><b>",(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS || cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS)?"Enable":"Disable");
			System.out.printf("<INPUT TYPE='checkbox' NAME='ahas'>");
			System.out.printf("</b></td></tr>\n");
		        }
		break;
		
	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Servicegroup Name:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='servicegroup' VALUE='%s'>",servicegroup_name);
		System.out.printf("</b></td></tr>\n");
		if(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS || cmd==common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS || cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS || cmd==common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS){
			System.out.printf("<tr><td CLASS='optBoxItem'>%s For Hosts Too:</td><td><b>",(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS || cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS)?"Enable":"Disable");
			System.out.printf("<INPUT TYPE='checkbox' NAME='ahas'>");
			System.out.printf("</b></td></tr>\n");
		        }
		break;
		
	case common_h.CMD_DEL_HOST_DOWNTIME:
	case common_h.CMD_DEL_SVC_DOWNTIME:
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Scheduled Downtime ID:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='down_id' VALUE='%d'>",downtime_id);
		System.out.printf("</b></td></tr>\n");
		break;


	case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
	case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME:
	case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
	case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:

		if(cmd==common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME){
			System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Hostgroup Name:</td><td><b>");
			System.out.printf("<INPUT TYPE='TEXT' NAME='hostgroup' VALUE='%s'>",hostgroup_name);
			System.out.printf("</b></td></tr>\n");
		        }
		else{
			System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Servicegroup Name:</td><td><b>");
			System.out.printf("<INPUT TYPE='TEXT' NAME='servicegroup' VALUE='%s'>",servicegroup_name);
			System.out.printf("</b></td></tr>\n");
		        }
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Author (Your Name):</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_author' VALUE='%s'>",comment_author);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Comment:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='com_data' VALUE='%s' SIZE=40>",comment_data);
		System.out.printf("</b></td></tr>\n");
		t = utils.currentTimeInSeconds();
        buffer = cgiutils.get_time_string(t, common_h.SHORT_DATE_TIME);
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>Start Time:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='start_time' VALUE='%s'>",buffer);
		System.out.printf("</b></td></tr>\n");
		t+=7200;
        buffer = cgiutils.get_time_string( t, common_h.SHORT_DATE_TIME);
		System.out.printf("<tr><td CLASS='optBoxRequiredItem'>End Time:</td><td><b>");
		System.out.printf("<INPUT TYPE='TEXT' NAME='end_time' VALUE='%s'>",buffer);
		System.out.printf("</b></td></tr>\n");
		System.out.printf("<tr><td CLASS='optBoxItem'>Type:</td><td><b>");
		System.out.printf("<SELECT NAME='fixed'>");
		System.out.printf("<OPTION VALUE=1>Fixed\n");
		System.out.printf("<OPTION VALUE=0>Flexible\n");
		System.out.printf("</SELECT>\n");
		System.out.printf("</b></td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'>If Flexible, Duration:</td><td>");
		System.out.printf("<table border=0><tr>\n");
		System.out.printf("<td align=right><INPUT TYPE='TEXT' NAME='hours' VALUE='2' SIZE=2 MAXLENGTH=2></td>\n");
		System.out.printf("<td align=left>Hours</td>\n");
		System.out.printf("<td align=right><INPUT TYPE='TEXT' NAME='minutes' VALUE='0' SIZE=2 MAXLENGTH=2></td>\n");
		System.out.printf("<td align=left>Minutes</td>\n");
		System.out.printf("</tr></table>\n");
		System.out.printf("</td></tr>\n");
		if(cmd==common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME || cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME){
			System.out.printf("<tr><td CLASS='optBoxItem'>Schedule Downtime For Hosts Too:</td><td><b>");
			System.out.printf("<INPUT TYPE='checkbox' NAME='ahas'>");
			System.out.printf("</b></td></tr>\n");
		        }
		break;

	default:
		System.out.printf("<tr><td CLASS='optBoxItem'>This should not be happening... :-(</td><td></td></tr>\n");
	        }


	System.out.printf("<tr><td CLASS='optBoxItem' COLSPAN=2></td></tr>\n");
	System.out.printf("<tr><td CLASS='optBoxItem'></td><td CLASS='optBoxItem'><INPUT TYPE='submit' NAME='btnSubmit' VALUE='Commit'> <INPUT TYPE='reset' VALUE='Reset'></td></tr>\n");

	System.out.printf("</table>\n");
	System.out.printf("</form>\n");	
	System.out.printf("</td>\n");
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</td>\n");
	System.out.printf("<td align=center valign=top width=50%%>\n");

	/* show information about the command... */
	show_command_help(cmd);
	
	System.out.printf("</td>\n");
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</div>\n");
	System.out.printf("</p>\n");


	System.out.printf("<P><DIV CLASS='infoMessage'>Please enter all required information before committing the command.<br>Required fields are marked in red.<br>Failure to supply all required values will result in an error.</DIV></P>");

	return;
        }


public static void commit_command_data(int cmd){
	int result=common_h.OK;
	int authorized=common_h.FALSE;
	objects_h.service temp_service;
    objects_h.host temp_host;
    objects_h.hostgroup temp_hostgroup;
    comments_h.comment temp_comment;
	downtime_h.scheduled_downtime temp_downtime;
    objects_h.servicegroup temp_servicegroup=null;
    String error_string = null;

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	switch(cmd){
	case common_h.CMD_ADD_HOST_COMMENT:
	case common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM:

        /* make sure we have author name, and comment data... */
	   if(comment_author.equals(""))
	      error_string= "Author was not entered";
	   else if( comment_data.equals(""))
	      error_string= "Comment was not entered";

		/* clean up the comment data */
        comment_author = clean_comment_data(comment_author);
        comment_data = clean_comment_data(comment_data);

		/* see if the user is authorized to issue a command... */
		temp_host=objects.find_host(host_name);
		if(cgiauth.is_authorized_for_host_commands(temp_host,current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;
		break;
		
	case common_h.CMD_ADD_SVC_COMMENT:
	case common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM:

       /* make sure we have author name, and comment data... */
       if(comment_author.equals(""))
          error_string= "Author was not entered";
       else if( comment_data.equals(""))
          error_string= "Comment was not entered";

		/* clean up the comment data */
        comment_author = clean_comment_data(comment_author);
        comment_data = clean_comment_data(comment_data);

		/* see if the user is authorized to issue a command... */
		temp_service=objects.find_service(host_name,service_desc);
		if(cgiauth.is_authorized_for_service_commands(temp_service,current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;
		break;

	case common_h.CMD_DEL_HOST_COMMENT:
	case common_h.CMD_DEL_SVC_COMMENT:

		/* check the sanity of the comment id */
		if(comment_id==0)
            error_string= "Comment id cannot be 0";

		/* read comments */
		comments.read_comment_data(cgiutils.get_cgi_config_location());

		/* find the comment */
		if(cmd==common_h.CMD_DEL_HOST_COMMENT)
			temp_comment=comments.find_host_comment(comment_id);
		else
			temp_comment=comments.find_service_comment(comment_id);

		/* see if the user is authorized to issue a command... */
		if(cmd==common_h.CMD_DEL_HOST_COMMENT && temp_comment!=null){
			temp_host=objects.find_host(temp_comment.host_name);
			if(cgiauth.is_authorized_for_host_commands(temp_host,current_authdata)==common_h.TRUE)
				authorized=common_h.TRUE;
		        }
		if(cmd==common_h.CMD_DEL_SVC_COMMENT && temp_comment!=null){
			temp_service=objects.find_service(temp_comment.host_name,temp_comment.service_description);
			if(cgiauth.is_authorized_for_service_commands(temp_service,current_authdata)==common_h.TRUE)
				authorized=common_h.TRUE;
		        }

		/* free comment data */
		comments.free_comment_data();

		break;
		
	case common_h.CMD_DEL_HOST_DOWNTIME:
	case common_h.CMD_DEL_SVC_DOWNTIME:

		/* check the sanity of the downtime id */
		if(downtime_id==0)
           error_string = "Downtime id cannot be 0";

		/* read scheduled downtime */
		downtime.read_downtime_data( cgiutils.get_cgi_config_location());

		/* find the downtime entry */
		if(cmd==common_h.CMD_DEL_HOST_DOWNTIME)
			temp_downtime=downtime.find_host_downtime(downtime_id);
		else
			temp_downtime=downtime.find_service_downtime(downtime_id);

		/* see if the user is authorized to issue a command... */
		if(cmd==common_h.CMD_DEL_HOST_DOWNTIME && temp_downtime!=null){
			temp_host=objects.find_host(temp_downtime.host_name);
			if(cgiauth.is_authorized_for_host_commands(temp_host,current_authdata)==common_h.TRUE)
				authorized=common_h.TRUE;
		        }
		if(cmd==common_h.CMD_DEL_SVC_DOWNTIME && temp_downtime!=null){
			temp_service=objects.find_service(temp_downtime.host_name,temp_downtime.service_description);
			if(cgiauth.is_authorized_for_service_commands(temp_service,current_authdata)==common_h.TRUE)
				authorized=common_h.TRUE;
		        }

		/* free downtime data */
		downtime.free_downtime_data();

		break;
		
	case common_h.CMD_SCHEDULE_SVC_CHECK:
	case common_h.CMD_ENABLE_SVC_CHECK:
	case common_h.CMD_DISABLE_SVC_CHECK:
	case common_h.CMD_DEL_ALL_SVC_COMMENTS:
	case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
	case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
	case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
	case common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT:
	case common_h.CMD_PROCESS_SERVICE_CHECK_RESULT:
	case common_h.CMD_SCHEDULE_SVC_DOWNTIME:
	case common_h.CMD_DELAY_SVC_NOTIFICATION:
	case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
	case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
	case common_h.CMD_START_OBSESSING_OVER_SVC:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC:

	   /* make sure we have author name and comment data... */
	   if(cmd==common_h.CMD_SCHEDULE_SVC_DOWNTIME){
	      if( comment_data.equals("") && error_string == null)
	         error_string= "Comment was not entered" ;
	      else if( comment_author.equals("") && error_string == null )
	         error_string= "Author was not entered";
	   }

		/* see if the user is authorized to issue a command... */
		temp_service=objects.find_service(host_name,service_desc);
		if(cgiauth.is_authorized_for_service_commands(temp_service,current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;

		/* make sure we have passive check info (if necessary) */
		if(cmd==common_h.CMD_PROCESS_SERVICE_CHECK_RESULT && plugin_output.equals("") && error_string == null )
           error_string = "Plugin output cannot be blank";

		/* make sure we have a notification delay (if necessary) */
		if(cmd==common_h.CMD_DELAY_SVC_NOTIFICATION && notification_delay<=0 && error_string == null )
           error_string = "Notification delay must be greater than 0";

		/* clean up the comment data if scheduling downtime */
		if(cmd==common_h.CMD_SCHEDULE_SVC_DOWNTIME){
			clean_comment_data(comment_author);
			clean_comment_data(comment_data);
		        }

		/* make sure we have check time (if necessary) */
		if(cmd==common_h.CMD_SCHEDULE_SVC_CHECK && start_time==0 && error_string == null )
           error_string = "Start time must be non-zero";

		/* make sure we have start/end times for downtime (if necessary) */
		if(cmd==common_h.CMD_SCHEDULE_SVC_DOWNTIME && (start_time==0 || end_time==0 || end_time<start_time) && error_string == null )
           error_string = "Start or end time not valid";

		break;
		
	case common_h.CMD_ENABLE_NOTIFICATIONS:
	case common_h.CMD_DISABLE_NOTIFICATIONS:
	case common_h.CMD_SHUTDOWN_PROCESS:
	case common_h.CMD_RESTART_PROCESS:
	case common_h.CMD_START_EXECUTING_SVC_CHECKS:
	case common_h.CMD_STOP_EXECUTING_SVC_CHECKS:
	case common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS:
	case common_h.CMD_ENABLE_EVENT_HANDLERS:
	case common_h.CMD_DISABLE_EVENT_HANDLERS:
	case common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS:
	case common_h.CMD_ENABLE_FLAP_DETECTION:
	case common_h.CMD_DISABLE_FLAP_DETECTION:
	case common_h.CMD_ENABLE_FAILURE_PREDICTION:
	case common_h.CMD_DISABLE_FAILURE_PREDICTION:
	case common_h.CMD_ENABLE_PERFORMANCE_DATA:
	case common_h.CMD_DISABLE_PERFORMANCE_DATA:
	case common_h.CMD_START_EXECUTING_HOST_CHECKS:
	case common_h.CMD_STOP_EXECUTING_HOST_CHECKS:
	case common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS:
	case common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS:

		/* see if the user is authorized to issue a command... */
		if(cgiauth.is_authorized_for_system_commands(current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;
		break;
		
	case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
	case common_h.CMD_DEL_ALL_HOST_COMMENTS:
	case common_h.CMD_SCHEDULE_HOST_SVC_CHECKS:
	case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
	case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
	case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
	case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
	case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
	case common_h.CMD_ENABLE_HOST_CHECK:
	case common_h.CMD_DISABLE_HOST_CHECK:
	case common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT:
	case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
	case common_h.CMD_DELAY_HOST_NOTIFICATION:
	case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
	case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
	case common_h.CMD_PROCESS_HOST_CHECK_RESULT:
	case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
	case common_h.CMD_SCHEDULE_HOST_CHECK:
	case common_h.CMD_START_OBSESSING_OVER_HOST:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST:

	   /* make sure we have author name and comment data... */
	   if(cmd==common_h.CMD_SCHEDULE_HOST_DOWNTIME){
	      if( comment_data.equals("") && error_string == null )
	         error_string= "Comment was not entered";
	      else if( comment_author.equals(""))
	         error_string= "Author was not entered";
	   }

		/* see if the user is authorized to issue a command... */
		temp_host=objects.find_host(host_name);
		if(cgiauth.is_authorized_for_host_commands(temp_host,current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;

		/* clean up the comment data if scheduling downtime */
		if(cmd==common_h.CMD_SCHEDULE_HOST_DOWNTIME){
			clean_comment_data(comment_author);
			clean_comment_data(comment_data);
		        }

		/* make sure we have a notification delay (if necessary) */
		if(cmd==common_h.CMD_DELAY_HOST_NOTIFICATION && notification_delay<=0 && error_string == null)
           error_string= "Notification delay must be greater than 0";

		/* make sure we have start/end times for downtime (if necessary) */
		if(cmd==common_h.CMD_SCHEDULE_HOST_DOWNTIME && (start_time==0 || end_time== 0 || start_time>end_time) && error_string == null )
           error_string= "Start or end time not valid";

		/* make sure we have check time (if necessary) */
		if((cmd==common_h.CMD_SCHEDULE_HOST_CHECK || cmd==common_h.CMD_SCHEDULE_HOST_SVC_CHECKS)&& start_time==0 && error_string == null)
           error_string = "Start time must be non-zero";

		/* make sure we have passive check info (if necessary) */
		if(cmd==common_h.CMD_PROCESS_HOST_CHECK_RESULT && plugin_output.equals("") && error_string == null )
           error_string = "Plugin output cannot be blank";

		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
	case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
	case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME:

	   /* make sure we have author and comment data */
	   if(cmd==common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME) {
	      if( comment_data.equals("") && error_string == null )
	         error_string = "Comment was not entered";
	      else if( comment_author.equals(""))
	         error_string= "Author was not entered" ;
	   }

		/* make sure we have start/end times for downtime */
		if( error_string == null && (cmd==common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME) && (start_time==0 || end_time==0 || start_time>end_time))
           error_string= "Start or end time not valid";

		/* see if the user is authorized to issue a command... */
		temp_hostgroup=objects.find_hostgroup(hostgroup_name);
		if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;

		/* clean up the comment data if scheduling downtime */
		if(cmd==common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME){
			clean_comment_data(comment_author);
			clean_comment_data(comment_data);
		        }

		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
	case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
	case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:

	   /* make sure we have author and comment data */
	   if( cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME) {
	      if( comment_data.equals("") && error_string == null )
	         error_string = "Comment was not entered";
	      else if( comment_author.equals(""))
	         error_string= "Author was not entered" ;
	   }

        /* make sure we have start/end times for downtime */
		if( error_string == null && (cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME) && (start_time==0 || end_time==0 || start_time>end_time))
           error_string= "Start or end time not valid";

		/* see if the user is authorized to issue a command... */

		temp_servicegroup=objects.find_servicegroup(servicegroup_name);
		if(cgiauth.is_authorized_for_servicegroup(temp_servicegroup,current_authdata)==common_h.TRUE)
			authorized=common_h.TRUE;

		break;

	default:
       if( error_string == null )
          error_string= "An error occurred while processing your command!";
	}


	/* to be safe, we are going to REQUIRE that the authentication functionality is enabled... */
	if(cgiutils.use_authentication==common_h.FALSE)
	{
	   if(content_type==WML_CONTENT)
	      System.out.printf("<p>Error: Authentication is not enabled!</p>\n");
	   else
	   {
	      System.out.printf("<P>\n");
	      System.out.printf("<DIV CLASS='errorMessage'>Executing a command without security is NOT an option ;) </DIV><br>");
	      System.out.printf("<DIV CLASS='errorDescription'>");
	      System.out.printf("It seems that you have chosen to not use the authentication functionality.<br><br>");
	      System.out.printf("I don't want to be personally responsible for what may happen as a result of allowing unauthorized users to issue commands to Blue,");
	      System.out.printf("so you'll have to disable this safeguard if you are really stubborn and want to invite trouble.<br><br>");
	      System.out.printf("<strong>Read servlet specification to understand how to secure this environment.</strong>\n");
	      System.out.printf("</DIV>\n");
	      System.out.printf("</P>\n");
	   }
	}

	/* the user is not authorized to issue the given command */
	else if(authorized==common_h.FALSE){
	   if(content_type==WML_CONTENT)
	      System.out.printf("<p>Error: You're not authorized to commit that command!</p>\n");
	   else{
	      System.out.printf("<P><DIV CLASS='errorMessage'>Sorry, but you are not authorized to commit the specified command.</DIV></P>\n");
	      System.out.printf("<P><DIV CLASS='errorDescription'>Read the section of the documentation that deals with authentication and authorization in the CGIs for more information.<BR><BR>\n");
	      System.out.printf("<A HREF='javascript:window.history.go(-2)'>Return from whence you came</A></DIV></P>\n");
	   }
	}

	/* some error occurred (data was probably missing) */
    else if(error_string != null ){
       if(content_type==WML_CONTENT)
          System.out.printf("<p>%s</p>\n", error_string);
       else{
          System.out.printf("<P><DIV CLASS='errorMessage'>%s</DIV></P>\n", error_string);
          System.out.printf("<P><DIV CLASS='errorDescription'>Go <A HREF='javascript:window.history.go(-1)'>back</A> and verify that you entered all required information correctly.<BR>\n");
          System.out.printf("<A HREF='javascript:window.history.go(-2)'>Return from whence you came</A></DIV></P>\n");
       }
    }

	/* if Nagios isn't checking external commands, don't do anything... */
    else if(cgiutils.check_external_commands==common_h.FALSE){
       if(content_type==WML_CONTENT)
          System.out.printf("<p>Error: Blue is not checking external commands!</p>\n");
       else{
          System.out.printf("<P><DIV CLASS='errorMessage'>Sorry, but Blue is currently not checking for external commands, so your command will not be committed!</DIV></P>\n");
          System.out.printf("<P><DIV CLASS='errorDescription'>Read the documentation for information on how to enable external commands...<BR><BR>\n");
          System.out.printf("<A HREF='javascript:window.history.go(-2)'>Return from whence you came</A></DIV></P>\n");
       }
    }
	
	/* everything looks okay, so let's go ahead and commit the command... */
	else{

		/* commit the command */
		result=commit_command(cmd);

		if(result==common_h.OK){
			if(content_type==WML_CONTENT)
				System.out.printf("<p>Your command was submitted sucessfully...</p>\n");
			else{
				System.out.printf("<P><DIV CLASS='infoMessage'>Your command request was successfully submitted to Blue for processing.<BR><BR>\n");
				System.out.printf("Note: It may take a while before the command is actually processed.<BR><BR>\n");
				System.out.printf("<A HREF='javascript:window.history.go(-2)'>Done</A></DIV></P>");
			        }
		        }
		else{
			if(content_type==WML_CONTENT)
				System.out.printf("<p>An error occurred while committing your command!</p>\n");
			else{
				System.out.printf("<P><DIV CLASS='errorMessage'>An error occurred while attempting to commit your command for processing.<BR><BR>\n");
				System.out.printf("<A HREF='javascript:window.history.go(-2)'>Return from whence you came</A></DIV></P>\n");
			        }
		        }
	        }

	return;
        }


/* commits a command for processing */
public static int commit_command(int cmd){
	String command_buffer;
	long current_time;
	long scheduled_time;
	long notification_time;
	int result;

	/* get the current time */
	current_time = utils.currentTimeInSeconds();

	/* get the scheduled time */
	scheduled_time=current_time+(schedule_delay*60);

	/* get the notification time */
	notification_time=current_time+(notification_delay*60);

	/* decide how to form the command line... */
	switch(cmd){

	case common_h.CMD_ADD_HOST_COMMENT:
		command_buffer = String.format( "[%d] ADD_HOST_COMMENT;%s;%d;%s;%s\n",current_time,host_name,(persistent_comment==common_h.TRUE)?1:0,comment_author,comment_data);
		break;
		
	case common_h.CMD_ADD_SVC_COMMENT:
		command_buffer = String.format( "[%d] ADD_SVC_COMMENT;%s;%s;%d;%s;%s\n",current_time,host_name,service_desc,(persistent_comment==common_h.TRUE)?1:0,comment_author,comment_data);
		break;

	case common_h.CMD_DEL_HOST_COMMENT:
		command_buffer = String.format( "[%d] DEL_HOST_COMMENT;%d\n",current_time,comment_id);
		break;
		
	case common_h.CMD_DEL_SVC_COMMENT:
		command_buffer = String.format( "[%d] DEL_SVC_COMMENT;%d\n",current_time,comment_id);
		break;
		
	case common_h.CMD_DELAY_HOST_NOTIFICATION:
		command_buffer = String.format( "[%d] DELAY_HOST_NOTIFICATION;%s;%d\n",current_time,host_name,notification_time);
		break;

	case common_h.CMD_DELAY_SVC_NOTIFICATION:
		command_buffer = String.format( "[%d] DELAY_SVC_NOTIFICATION;%s;%s;%d\n",current_time,host_name,service_desc,notification_time);
		break;

	case common_h.CMD_SCHEDULE_SVC_CHECK:
		command_buffer = String.format( "[%d] SCHEDULE_%sSVC_CHECK;%s;%s;%d\n",current_time,(force_check==common_h.TRUE)?"FORCED_":"",host_name,service_desc,start_time);
		break;

	case common_h.CMD_ENABLE_SVC_CHECK:
	case common_h.CMD_DISABLE_SVC_CHECK:
		command_buffer = String.format( "[%d] %s_SVC_CHECK;%s;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SVC_CHECK)?"ENABLE":"DISABLE",host_name,service_desc);
		break;
		
	case common_h.CMD_DISABLE_NOTIFICATIONS:
		command_buffer = String.format( "[%d] DISABLE_NOTIFICATIONS;%d\n",current_time,scheduled_time);
		break;
		
	case common_h.CMD_ENABLE_NOTIFICATIONS:
		command_buffer = String.format( "[%d] ENABLE_NOTIFICATIONS;%d\n",current_time,scheduled_time);
		break;
		
	case common_h.CMD_SHUTDOWN_PROCESS:
	case common_h.CMD_RESTART_PROCESS:
		command_buffer = String.format( "[%d] %s_PROGRAM;%d\n",current_time,(cmd==common_h.CMD_SHUTDOWN_PROCESS)?"SHUTDOWN":"RESTART",scheduled_time);
		break;

	case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] %s_HOST_SVC_CHECKS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS)?"ENABLE":"DISABLE",host_name);
		else
			command_buffer = String.format( "[%d] %s_HOST_SVC_CHECKS;%s\n[%d] %s_HOST_CHECK;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS)?"ENABLE":"DISABLE",host_name,current_time,(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_SCHEDULE_HOST_SVC_CHECKS:
		command_buffer = String.format( "[%d] SCHEDULE_%sHOST_SVC_CHECKS;%s;%d\n",current_time,(force_check==common_h.TRUE)?"FORCED_":"",host_name,scheduled_time);
		break;

	case common_h.CMD_DEL_ALL_HOST_COMMENTS:
		command_buffer = String.format( "[%d] DEL_ALL_HOST_COMMENTS;%s\n",current_time,host_name);
		break;
		
	case common_h.CMD_DEL_ALL_SVC_COMMENTS:
		command_buffer = String.format( "[%d] DEL_ALL_SVC_COMMENTS;%s;%s\n",current_time,host_name,service_desc);
		break;

	case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
		command_buffer = String.format( "[%d] %s_SVC_NOTIFICATIONS;%s;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",host_name,service_desc);
		break;
		
	case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
		if(propagate_to_children==common_h.TRUE)
			command_buffer = String.format( "[%d] %s_HOST_AND_CHILD_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_NOTIFICATIONS)?"ENABLE":"DISABLE",host_name);
		else
			command_buffer = String.format( "[%d] %s_HOST_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_NOTIFICATIONS)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
	case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
		command_buffer = String.format( "[%d] %s_ALL_NOTIFICATIONS_BEYOND_HOST;%s\n",current_time,(cmd==common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] %s_HOST_SVC_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",host_name);
		else
			command_buffer = String.format( "[%d] %s_HOST_SVC_NOTIFICATIONS;%s\n[%d] %s_HOST_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",host_name,current_time,(cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM:
		command_buffer = String.format( "[%d] ACKNOWLEDGE_HOST_PROBLEM;%s;%d;%d;%d;%s;%s\n",current_time,host_name,(sticky_ack==common_h.TRUE)?common_h.ACKNOWLEDGEMENT_STICKY:common_h.ACKNOWLEDGEMENT_NORMAL,(send_notification==common_h.TRUE)?1:0,(persistent_comment==common_h.TRUE)?1:0,comment_author,comment_data);
		break;
		
	case common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM:
		command_buffer = String.format( "[%d] ACKNOWLEDGE_SVC_PROBLEM;%s;%s;%d;%d;%d;%s;%s\n",current_time,host_name,service_desc,(sticky_ack==common_h.TRUE)?common_h.ACKNOWLEDGEMENT_STICKY:common_h.ACKNOWLEDGEMENT_NORMAL,(send_notification==common_h.TRUE)?1:0,(persistent_comment==common_h.TRUE)?1:0,comment_author,comment_data);
		break;

	case common_h.CMD_START_EXECUTING_SVC_CHECKS:
	case common_h.CMD_STOP_EXECUTING_SVC_CHECKS:
		command_buffer = String.format( "[%d] %s_EXECUTING_SVC_CHECKS;\n",current_time,(cmd==common_h.CMD_START_EXECUTING_SVC_CHECKS)?"START":"STOP");
		break;

	case common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS:
		command_buffer = String.format( "[%d] %s_ACCEPTING_PASSIVE_SVC_CHECKS;\n",current_time,(cmd==common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS)?"START":"STOP");
		break;

	case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
		command_buffer = String.format( "[%d] %s_PASSIVE_SVC_CHECKS;%s;%s\n",current_time,(cmd==common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS)?"ENABLE":"DISABLE",host_name,service_desc);
		break;
		
	case common_h.CMD_ENABLE_EVENT_HANDLERS:
	case common_h.CMD_DISABLE_EVENT_HANDLERS:
		command_buffer = String.format( "[%d] %s_EVENT_HANDLERS;\n",current_time,(cmd==common_h.CMD_ENABLE_EVENT_HANDLERS)?"ENABLE":"DISABLE");
		break;

	case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
	case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
		command_buffer = String.format( "[%d] %s_SVC_EVENT_HANDLER;%s;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SVC_EVENT_HANDLER)?"ENABLE":"DISABLE",host_name,service_desc);
		break;
		
	case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
	case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
		command_buffer = String.format( "[%d] %s_HOST_EVENT_HANDLER;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_EVENT_HANDLER)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_ENABLE_HOST_CHECK:
	case common_h.CMD_DISABLE_HOST_CHECK:
		command_buffer = String.format( "[%d] %s_HOST_CHECK;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_CHECK)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS:
		command_buffer = String.format( "[%d] %s_OBSESSING_OVER_SVC_CHECKS;\n",current_time,(cmd==common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS)?"START":"STOP");
		break;
		
	case common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT:
		command_buffer = String.format( "[%d] REMOVE_HOST_ACKNOWLEDGEMENT;%s\n",current_time,host_name);
		break;
		
	case common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT:
		command_buffer = String.format( "[%d] REMOVE_SVC_ACKNOWLEDGEMENT;%s;%s\n",current_time,host_name,service_desc);
		break;
		
	case common_h.CMD_PROCESS_SERVICE_CHECK_RESULT:
		command_buffer = String.format( "[%d] PROCESS_SERVICE_CHECK_RESULT;%s;%s;%d;%s|%s\n",current_time,host_name,service_desc,plugin_state,plugin_output,performance_data);
		break;
		
	case common_h.CMD_PROCESS_HOST_CHECK_RESULT:
		command_buffer = String.format( "[%d] PROCESS_HOST_CHECK_RESULT;%s;%d;%s|%s\n",current_time,host_name,plugin_state,plugin_output,performance_data);
		break;
		
	case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
		if(child_options==1)
			command_buffer = String.format( "[%d] SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME;%s;%d;%d;%d;%d;%d;%s;%s\n",current_time,host_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,triggered_by,duration,comment_author,comment_data);
		else if(child_options==2)
			command_buffer = String.format( "[%d] SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME;%s;%d;%d;%d;%d;%d;%s;%s\n",current_time,host_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,triggered_by,duration,comment_author,comment_data);
		else
			command_buffer = String.format( "[%d] SCHEDULE_HOST_DOWNTIME;%s;%d;%d;%d;%d;%d;%s;%s\n",current_time,host_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,triggered_by,duration,comment_author,comment_data);
		break;
		
	case common_h.CMD_SCHEDULE_SVC_DOWNTIME:
		command_buffer = String.format( "[%d] SCHEDULE_SVC_DOWNTIME;%s;%s;%d;%d;%d;%d;%d;%s;%s\n",current_time,host_name,service_desc,start_time,end_time,(fixed==common_h.TRUE)?1:0,triggered_by,duration,comment_author,comment_data);
		break;
		
	case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
	case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
		command_buffer = String.format( "[%d] %s_HOST_FLAP_DETECTION;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOST_FLAP_DETECTION)?"ENABLE":"DISABLE",host_name);
		break;
		
	case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
	case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
		command_buffer = String.format( "[%d] %s_SVC_FLAP_DETECTION;%s;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SVC_FLAP_DETECTION)?"ENABLE":"DISABLE",host_name,service_desc);
		break;
		
	case common_h.CMD_ENABLE_FLAP_DETECTION:
	case common_h.CMD_DISABLE_FLAP_DETECTION:
		command_buffer = String.format( "[%d] %s_FLAP_DETECTION\n",current_time,(cmd==common_h.CMD_ENABLE_FLAP_DETECTION)?"ENABLE":"DISABLE");
		break;
		
	case common_h.CMD_DEL_HOST_DOWNTIME:
	case common_h.CMD_DEL_SVC_DOWNTIME:
		command_buffer = String.format( "[%d] DEL_%s_DOWNTIME;%d\n",current_time,(cmd==common_h.CMD_DEL_HOST_DOWNTIME)?"HOST":"SVC",downtime_id);
		break;

	case common_h.CMD_ENABLE_FAILURE_PREDICTION:
	case common_h.CMD_DISABLE_FAILURE_PREDICTION:
		command_buffer = String.format( "[%d] %s_FAILURE_PREDICTION\n",current_time,(cmd==common_h.CMD_ENABLE_FAILURE_PREDICTION)?"ENABLE":"DISABLE");
		break;
		
	case common_h.CMD_ENABLE_PERFORMANCE_DATA:
	case common_h.CMD_DISABLE_PERFORMANCE_DATA:
		command_buffer = String.format( "[%d] %s_PERFORMANCE_DATA\n",current_time,(cmd==common_h.CMD_ENABLE_PERFORMANCE_DATA)?"ENABLE":"DISABLE");
		break;
		
	case common_h.CMD_START_EXECUTING_HOST_CHECKS:
	case common_h.CMD_STOP_EXECUTING_HOST_CHECKS:
		command_buffer = String.format( "[%d] %s_EXECUTING_HOST_CHECKS;\n",current_time,(cmd==common_h.CMD_START_EXECUTING_HOST_CHECKS)?"START":"STOP");
		break;

	case common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS:
	case common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS:
		command_buffer = String.format( "[%d] %s_ACCEPTING_PASSIVE_HOST_CHECKS;\n",current_time,(cmd==common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS)?"START":"STOP");
		break;

	case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
	case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
		command_buffer = String.format( "[%d] %s_PASSIVE_HOST_CHECKS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS)?"ENABLE":"DISABLE",host_name);
		break;

	case common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS:
		command_buffer = String.format( "[%d] %s_OBSESSING_OVER_HOST_CHECKS;\n",current_time,(cmd==common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS)?"START":"STOP");
		break;

	case common_h.CMD_SCHEDULE_HOST_CHECK:
		command_buffer = String.format( "[%d] SCHEDULE_%sHOST_CHECK;%s;%d\n",current_time,(force_check==common_h.TRUE)?"FORCED_":"",host_name,start_time);
		break;

	case common_h.CMD_START_OBSESSING_OVER_SVC:
	case common_h.CMD_STOP_OBSESSING_OVER_SVC:
		command_buffer = String.format( "[%d] %s_OBSESSING_OVER_SVC;%s;%s\n",current_time,(cmd==common_h.CMD_START_OBSESSING_OVER_SVC)?"START":"STOP",host_name,service_desc);
		break;

	case common_h.CMD_START_OBSESSING_OVER_HOST:
	case common_h.CMD_STOP_OBSESSING_OVER_HOST:
		command_buffer = String.format( "[%d] %s_OBSESSING_OVER_HOST;%s\n",current_time,(cmd==common_h.CMD_START_OBSESSING_OVER_HOST)?"START":"STOP",host_name);
		break;


		/***** HOSTGROUP COMMANDS *****/

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] %s_HOSTGROUP_SVC_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",hostgroup_name);
		else
			command_buffer = String.format( "[%d] %s_HOSTGROUP_SVC_NOTIFICATIONS;%s\n[%d] %s_HOSTGROUP_HOST_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",hostgroup_name,current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",hostgroup_name);
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
		command_buffer = String.format( "[%d] %s_HOSTGROUP_HOST_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS)?"ENABLE":"DISABLE",hostgroup_name);
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] %s_HOSTGROUP_SVC_CHECKS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS)?"ENABLE":"DISABLE",hostgroup_name);
		else
			command_buffer = String.format( "[%d] %s_HOSTGROUP_SVC_CHECKS;%s\n[%d] %s_HOSTGROUP_HOST_CHECKS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS)?"ENABLE":"DISABLE",hostgroup_name,current_time,(cmd==common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS)?"ENABLE":"DISABLE",hostgroup_name);
		break;

	case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
		command_buffer = String.format( "[%d] SCHEDULE_HOSTGROUP_HOST_DOWNTIME;%s;%d;%d;%d;0;%d;%s;%s\n",current_time,hostgroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data);
		break;

	case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] SCHEDULE_HOSTGROUP_SVC_DOWNTIME;%s;%d;%d;%d;0;%d;%s;%s\n",current_time,hostgroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data);
		else
			command_buffer = String.format( "[%d] SCHEDULE_HOSTGROUP_SVC_DOWNTIME;%s;%d;%d;%d;0;%d;%s;%s\n[%d] SCHEDULE_HOSTGROUP_HOST_DOWNTIME;%s;%d;%d;%d;%d;%s;%s\n",current_time,hostgroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data,current_time,hostgroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data);
		break;


		/***** SERVICEGROUP COMMANDS *****/

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] %s_SERVICEGROUP_SVC_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",servicegroup_name);
		else
			command_buffer = String.format( "[%d] %s_SERVICEGROUP_SVC_NOTIFICATIONS;%s\n[%d] %s_SERVICEGROUP_HOST_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",servicegroup_name,current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS)?"ENABLE":"DISABLE",servicegroup_name);
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
	case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
		command_buffer = String.format( "[%d] %s_SERVICEGROUP_HOST_NOTIFICATIONS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS)?"ENABLE":"DISABLE",servicegroup_name);
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] %s_SERVICEGROUP_SVC_CHECKS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS)?"ENABLE":"DISABLE",servicegroup_name);
		else
			command_buffer = String.format( "[%d] %s_SERVICEGROUP_SVC_CHECKS;%s\n[%d] %s_SERVICEGROUP_HOST_CHECKS;%s\n",current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS)?"ENABLE":"DISABLE",servicegroup_name,current_time,(cmd==common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS)?"ENABLE":"DISABLE",servicegroup_name);
		break;

	case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
		command_buffer = String.format( "[%d] SCHEDULE_SERVICEGROUP_HOST_DOWNTIME;%s;%d;%d;%d;0;%d;%s;%s\n",current_time,servicegroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data);
		break;

	case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:
		if(affect_host_and_services==common_h.FALSE)
			command_buffer = String.format( "[%d] SCHEDULE_SERVICEGROUP_SVC_DOWNTIME;%s;%d;%d;%d;0;%d;%s;%s\n",current_time,servicegroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data);
		else
			command_buffer = String.format( "[%d] SCHEDULE_SERVICEGROUP_SVC_DOWNTIME;%s;%d;%d;%d;0;%d;%s;%s\n[%d] SCHEDULE_SERVICEGROUP_HOST_DOWNTIME;%s;%d;%d;%d;%d;%s;%s\n",current_time,servicegroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data,current_time,servicegroup_name,start_time,end_time,(fixed==common_h.TRUE)?1:0,duration,comment_author,comment_data);
		break;

	default:
		return common_h.ERROR;
		
	        }

	/* write the command to the command file */
	result=write_command_to_file(command_buffer);

	return result;
        }



/* write a command entry to the command file */
public static int write_command_to_file(String cmd){
   
 	/* open the command for writing (since this is a pipe, it will really be appended) */
	try {
       
       FileChannel wChannel = new FileOutputStream( new File(cgiutils.command_file), true ).getChannel();
       FileLock lock = wChannel.tryLock();
       while ( lock == null ) 
          lock = wChannel.tryLock();
       
       wChannel.write( ByteBuffer.wrap( cmd.getBytes() ) );
       lock.release();

       wChannel.close();
       

//	    BufferedWriter out = new BufferedWriter(new FileWriter( cgiutils.command_file ));
//	    out.append( cmd );
//	    out.close();
	} catch ( IOException ioE ) {
       ioE.printStackTrace();
	    if(content_type==WML_CONTENT)
	        System.out.printf("<p>Error: Could not open command file for update!</p>\n");
	    else{
	        System.out.printf("<P><DIV CLASS='errorMessage'>Error: Could not open command file '%s' for update!</DIV></P>\n",cgiutils.command_file);
	        System.out.printf("<P><DIV CLASS='errorDescription'>");
	        System.out.printf("The permissions on the external command file and/or directory may be incorrect.  Read the FAQs on how to setup proper permissions.\n");
	        System.out.printf("</DIV></P>\n");
	    }
	    
	    return common_h.ERROR;
	}
    
	return common_h.OK;
}


/* strips out semicolons from comment data */
public static String clean_comment_data(String buffer){   
    return buffer.replace( ';', ' ' );
}


/* display information about a command */
public static void show_command_help(int cmd){

	System.out.printf("<DIV ALIGN=CENTER CLASS='descriptionTitle'>Command Description</DIV>\n");
	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='commandDescription'>\n");
	System.out.printf("<TR><TD CLASS='commandDescription'>\n");

	/* decide what information to print out... */
	switch(cmd){

	case common_h.CMD_ADD_HOST_COMMENT:
		System.out.printf("This command is used to add a comment for the specified host.  If you work with other administrators, you may find it useful to share information about a host\n");
		System.out.printf("that is having problems if more than one of you may be working on it.  If you do not check the 'persistent' option, the comment will be automatically be deleted\n");
		System.out.printf("the next time Blue is restarted.\n");
		break;
		
	case common_h.CMD_ADD_SVC_COMMENT:
		System.out.printf("This command is used to add a comment for the specified service.  If you work with other administrators, you may find it useful to share information about a host\n");
		System.out.printf("or service that is having problems if more than one of you may be working on it.  If you do not check the 'persistent' option, the comment will automatically be\n");
		System.out.printf("deleted the next time Blue is restarted.\n");
		break;

	case common_h.CMD_DEL_HOST_COMMENT:
		System.out.printf("This command is used to delete a specific host comment.\n");
		break;
		
	case common_h.CMD_DEL_SVC_COMMENT:
		System.out.printf("This command is used to delete a specific service comment.\n");
		break;
		
	case common_h.CMD_DELAY_HOST_NOTIFICATION:
		System.out.printf("This command is used to delay the next problem notification that is sent out for the specified host.  The notification delay will be disregarded if\n");
		System.out.printf("the host changes state before the next notification is scheduled to be sent out.  This command has no effect if the host is currently UP.\n");
		break;

	case common_h.CMD_DELAY_SVC_NOTIFICATION:
		System.out.printf("This command is used to delay the next problem notification that is sent out for the specified service.  The notification delay will be disregarded if\n");
		System.out.printf("the service changes state before the next notification is scheduled to be sent out.  This command has no effect if the service is currently in an OK state.\n");
		break;

	case common_h.CMD_SCHEDULE_SVC_CHECK:
		System.out.printf("This command is used to schedule the next check of a particular service.  Blue will re-queue the service to be checked at the time you specify.\n");
		System.out.printf("If you select the <i>force check</i> option, Blue will force a check of the service regardless of both what time the scheduled check occurs and whether or not checks are enabled for the service.\n");
		break;

	case common_h.CMD_ENABLE_SVC_CHECK:
		System.out.printf("This command is used to enable active checks of a service.\n");
		break;
		
	case common_h.CMD_DISABLE_SVC_CHECK:
		System.out.printf("This command is used to disable active checks of a service.\n");
		break;
		
	case common_h.CMD_DISABLE_NOTIFICATIONS:
		System.out.printf("This command is used to disable host and service notifications on a program-wide basis.\n");
		break;
		
	case common_h.CMD_ENABLE_NOTIFICATIONS:
		System.out.printf("This command is used to enable host and service notifications on a program-wide basis.\n");
		break;
		
	case common_h.CMD_SHUTDOWN_PROCESS:
		System.out.printf("This command is used to shutdown the Blue process. Note: Once the Blue has been shutdown, it cannot be restarted via the web interface!\n");
		break;

	case common_h.CMD_RESTART_PROCESS:
		System.out.printf("This command is used to restart the Blue process.   Executing a restart command is equivalent to sending the process a HUP signal.\n");
		System.out.printf("All information will be flushed from memory, the configuration files will be re-read, and Blue will start monitoring with the new configuration information.\n");
		break;

	case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
		System.out.printf("This command is used to enable active checks of all services associated with the specified host.  This <i>does not</i> enable checks of the host unless you check the 'Enable for host too' option.\n");
		break;
		
	case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
		System.out.printf("This command is used to disable active checks of all services associated with the specified host.  When a service is disabled Blue will not monitor the service.  Doing this will prevent any notifications being sent out for\n");
		System.out.printf("the specified service while it is disabled.  In order to have Blue check the service in the future you will have to re-enable the service.\n");
		System.out.printf("Note that disabling service checks may not necessarily prevent notifications from being sent out about the host which those services are associated with.  This <i>does not</i> disable checks of the host unless you check the 'Disable for host too' option.\n");
		break;
		
	case common_h.CMD_SCHEDULE_HOST_SVC_CHECKS:
		System.out.printf("This command is used to scheduled the next check of all services on the specified host.  If you select the <i>force check</i> option, Blue will force a check of all services on the host regardless of both what time the scheduled checks occur and whether or not checks are enabled for those services.\n");
		break;

	case common_h.CMD_DEL_ALL_HOST_COMMENTS:
		System.out.printf("This command is used to delete all comments associated with the specified host.\n");
		break;
		
	case common_h.CMD_DEL_ALL_SVC_COMMENTS:
		System.out.printf("This command is used to delete all comments associated with the specified service.\n");
		break;

	case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for the specified service.  Notifications will only be sent out for the\n");
		System.out.printf("service state types you defined in your service definition.\n");
		break;

	case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for the specified service.  You will have to re-enable notifications\n");
		System.out.printf("for this service before any alerts can be sent out in the future.\n");
		break;

	case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for the specified host.  Notifications will only be sent out for the\n");
		System.out.printf("host state types you defined in your host definition.  Note that this command <i>does not</i> enable notifications\n");
		System.out.printf("for services associated with this host.\n");
		break;

	case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for the specified host.  You will have to re-enable notifications for this host\n");
		System.out.printf("before any alerts can be sent out in the future.  Note that this command <i>does not</i> disable notifications for services associated with this host.\n");
		break;

	case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
		System.out.printf("This command is used to enable notifications for all hosts and services that lie \"beyond\" the specified host\n");
		System.out.printf("(from the view of Blue).\n");
		break;

	case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
		System.out.printf("This command is used to temporarily prevent notifications from being sent out for all hosts and services that lie\n");
		System.out.printf("\"beyone\" the specified host (from the view of Blue).\n");
		break;
		
	case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for all services on the specified host.  Notifications will only be sent out for the\n");
		System.out.printf("service state types you defined in your service definition.  This <i>does not</i> enable notifications for the host unless you check the 'Enable for host too' option.\n");
		break;

	case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for all services on the specified host.  You will have to re-enable notifications for\n");
		System.out.printf("all services associated with this host before any alerts can be sent out in the future.  This <i>does not</i> prevent notifications from being sent out about the host unless you check the 'Disable for host too' option.\n");
		break;

	case common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM:
		System.out.printf("This command is used to acknowledge a host problem.  When a host problem is acknowledged, future notifications about problems are temporarily disabled until the host changes from its current state.\n");
		System.out.printf("If you want acknowledgement to disable notifications until the host recovers, check the 'Sticky Acknowledgement' checkbox.\n");
		System.out.printf("Contacts for this host will receive a notification about the acknowledgement, so they are aware that someone is working on the problem.  Additionally, a comment will also be added to the host.\n");
		System.out.printf("Make sure to enter your name and fill in a brief description of what you are doing in the comment field.  If you would like the host comment to be retained between restarts of Blue, check\n");
		System.out.printf("the 'Persistent Comment' checkbox.  If you do not want an acknowledgement notification sent out to the appropriate contacts, uncheck the 'Send Notification' checkbox.\n");
		break;

	case common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM:
		System.out.printf("This command is used to acknowledge a service problem.  When a service problem is acknowledged, future notifications about problems are temporarily disabled until the service changes from its current state.\n");
		System.out.printf("If you want acknowledgement to disable notifications until the service recovers, check the 'Sticky Acknowledgement' checkbox.\n");
		System.out.printf("Contacts for this service will receive a notification about the acknowledgement, so they are aware that someone is working on the problem.  Additionally, a comment will also be added to the service.\n");
		System.out.printf("Make sure to enter your name and fill in a brief description of what you are doing in the comment field.  If you would like the service comment to be retained between restarts of Blue, check\n");
		System.out.printf("the 'Persistent Comment' checkbox.  If you do not want an acknowledgement notification sent out to the appropriate contacts, uncheck the 'Send Notification' checkbox.\n");
		break;

	case common_h.CMD_START_EXECUTING_SVC_CHECKS:
		System.out.printf("This command is used to resume execution of active service checks on a program-wide basis.  Individual services which are disabled will still not be checked.\n");
		break;

	case common_h.CMD_STOP_EXECUTING_SVC_CHECKS:
		System.out.printf("This command is used to temporarily stop Blue from actively executing any service checks.  This will have the side effect of preventing any notifications from being sent out (for any and all services and hosts).\n");
		System.out.printf("Service checks will not be executed again until you issue a command to resume service check execution.\n");
		break;

	case common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS:
		System.out.printf("This command is used to make Blue start accepting passive service check results that it finds in the external command file\n");
		break;

	case common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS:
		System.out.printf("This command is use to make Blue stop accepting passive service check results that it finds in the external command file.  All passive check results that are found will be ignored.\n");
		break;

	case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
		System.out.printf("This command is used to allow Blue to accept passive service check results that it finds in the external command file for this particular service.\n");
		break;

	case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
		System.out.printf("This command is used to stop Blue accepting passive service check results that it finds in the external command file for this particular service.  All passive check results that are found for this service will be ignored.\n");
		break;

	case common_h.CMD_ENABLE_EVENT_HANDLERS:
		System.out.printf("This command is used to allow Blue to run host and service event handlers.\n");
		break;

	case common_h.CMD_DISABLE_EVENT_HANDLERS:
		System.out.printf("This command is used to temporarily prevent Blue from running any host or service event handlers.\n");
		break;

	case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
		System.out.printf("This command is used to allow Blue to run the service event handler for a particular service when necessary (if one is defined).\n");
		break;

	case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
		System.out.printf("This command is used to temporarily prevent Blue from running the service event handler for a particular service.\n");
		break;

	case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
		System.out.printf("This command is used to allow Blue to run the host event handler for a particular service when necessary (if one is defined).\n");
		break;

	case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
		System.out.printf("This command is used to temporarily prevent Blue from running the host event handler for a particular host.\n");
		break;

	case common_h.CMD_ENABLE_HOST_CHECK:
		System.out.printf("This command is used to enable active checks of this host.\n");
		break;

	case common_h.CMD_DISABLE_HOST_CHECK:
		System.out.printf("This command is used to temporarily prevent Blue from actively checking the status of a particular host.  If Blue needs to check the status of this host, it will assume that it is in the same state that it was in before checks were disabled.\n");
		break;

	case common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS:
		System.out.printf("This command is used to have Blue start obsessing over service checks.  Read the documentation on distributed monitoring for more information on this.\n");
		break;

	case common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS:
		System.out.printf("This command is used stop Blue from obsessing over service checks.\n");
		break;

	case common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT:
		System.out.printf("This command is used to remove an acknowledgement for a particular host problem.  Once the acknowledgement is removed, notifications may start being\n");
		System.out.printf("sent out about the host problem.  Note: Removing the acknowledgement does <i>not</i> remove the host comment that was originally associated\n");
		System.out.printf("with the acknowledgement.  You'll have to remove that as well if that's what you want.\n");
		break;

	case common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT:
		System.out.printf("This command is used to remove an acknowledgement for a particular service problem.  Once the acknowledgement is removed, notifications may start being\n");
		System.out.printf("sent out about the service problem.  Note: Removing the acknowledgement does <i>not</i> remove the service comment that was originally associated\n");
		System.out.printf("with the acknowledgement.  You'll have to remove that as well if that's what you want.\n");
		break;

	case common_h.CMD_PROCESS_SERVICE_CHECK_RESULT:
		System.out.printf("This command is used to submit a passive check result for a particular service.  It is particularly useful for resetting security-related services to OK states once they have been dealt with.\n");
		break;

	case common_h.CMD_PROCESS_HOST_CHECK_RESULT:
		System.out.printf("This command is used to submit a passive check result for a particular host.\n");
		break;

	case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
		System.out.printf("This command is used to schedule downtime for a particular host.  During the specified downtime, Blue will not send notifications out about the host.\n");
		System.out.printf("When the scheduled downtime expires, Blue will send out notifications for this host as it normally would.  Scheduled downtimes are preserved\n");
		System.out.printf("across program shutdowns and restarts.  Both the start and end times should be specified in the following format:  <b>mm/dd/yyyy hh:mm:ss</b>.\n");
		System.out.printf("If you select the <i>fixed</i> option, the downtime will be in effect between the start and end times you specify.  If you do not select the <i>fixed</i>\n");
		System.out.printf("option, Blue will treat this as \"flexible\" downtime.  Flexible downtime starts when the host goes down or becomes unreachable (sometime between the\n");
		System.out.printf("start and end times you specified) and lasts as long as the duration of time you enter.  The duration fields do not apply for fixed downtime.\n");
		break;

	case common_h.CMD_SCHEDULE_SVC_DOWNTIME:
		System.out.printf("This command is used to schedule downtime for a particular service.  During the specified downtime, Blue will not send notifications out about the service.\n");
		System.out.printf("When the scheduled downtime expires, Blue will send out notifications for this service as it normally would.  Scheduled downtimes are preserved\n");
		System.out.printf("across program shutdowns and restarts.  Both the start and end times should be specified in the following format:  <b>mm/dd/yyyy hh:mm:ss</b>.\n");
		System.out.printf("option, Blue will treat this as \"flexible\" downtime.  Flexible downtime starts when the service enters a non-OK state (sometime between the\n");
		System.out.printf("start and end times you specified) and lasts as long as the duration of time you enter.  The duration fields do not apply for fixed downtime.\n");
		break;

	case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
		System.out.printf("This command is used to enable flap detection for a specific host.  If flap detection is disabled on a program-wide basis, this will have no effect,\n");
		break;

	case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
		System.out.printf("This command is used to disable flap detection for a specific host.\n");
		break;

	case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
		System.out.printf("This command is used to enable flap detection for a specific service.  If flap detection is disabled on a program-wide basis, this will have no effect,\n");
		break;

	case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
		System.out.printf("This command is used to disable flap detection for a specific service.\n");
		break;

	case common_h.CMD_ENABLE_FLAP_DETECTION:
		System.out.printf("This command is used to enable flap detection for hosts and services on a program-wide basis.  Individual hosts and services may have flap detection disabled.\n");
		break;

	case common_h.CMD_DISABLE_FLAP_DETECTION:
		System.out.printf("This command is used to disable flap detection for hosts and services on a program-wide basis.\n");
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for all services in the specified hostgroup.  Notifications will only be sent out for the\n");
		System.out.printf("service state types you defined in your service definitions.  This <i>does not</i> enable notifications for the hosts in this hostgroup unless you check the 'Enable for hosts too' option.\n");
		break;

	case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for all services in the specified hostgroup.  You will have to re-enable notifications for\n");
		System.out.printf("all services in this hostgroup before any alerts can be sent out in the future.  This <i>does not</i> prevent notifications from being sent out about the hosts in this hostgroup unless you check the 'Disable for hosts too' option.\n");
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for all hosts in the specified hostgroup.  Notifications will only be sent out for the\n");
		System.out.printf("host state types you defined in your host definitions.\n");
		break;

	case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for all hosts in the specified hostgroup.  You will have to re-enable notifications for\n");
		System.out.printf("all hosts in this hostgroup before any alerts can be sent out in the future.\n");
		break;

	case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
		System.out.printf("This command is used to enable active checks of all services in the specified hostgroup.  This <i>does not</i> enable active checks of the hosts in the hostgroup unless you check the 'Enable for hosts too' option.\n");
		break;
		
	case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
		System.out.printf("This command is used to disable active checks of all services in the specified hostgroup.  This <i>does not</i> disable checks of the hosts in the hostgroup unless you check the 'Disable for hosts too' option.\n");
		break;

	case common_h.CMD_DEL_HOST_DOWNTIME:
		System.out.printf("This command is used to cancel active or pending scheduled downtime for the specified host.\n");
		break;

	case common_h.CMD_DEL_SVC_DOWNTIME:
		System.out.printf("This command is used to cancel active or pending scheduled downtime for the specified service.\n");
		break;

	case common_h.CMD_ENABLE_FAILURE_PREDICTION:
		System.out.printf("This command is used to enable failure prediction for hosts and services on a program-wide basis.  Individual hosts and services may have failure prediction disabled.\n");
		break;

	case common_h.CMD_DISABLE_FAILURE_PREDICTION:
		System.out.printf("This command is used to disable failure prediction for hosts and services on a program-wide basis.\n");
		break;

	case common_h.CMD_ENABLE_PERFORMANCE_DATA:
		System.out.printf("This command is used to enable the processing of performance data for hosts and services on a program-wide basis.  Individual hosts and services may have performance data processing disabled.\n");
		break;

	case common_h.CMD_DISABLE_PERFORMANCE_DATA:
		System.out.printf("This command is used to disable the processing of performance data for hosts and services on a program-wide basis.\n");
		break;

	case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
		System.out.printf("This command is used to schedule downtime for all hosts in a particular hostgroup.  During the specified downtime, Blue will not send notifications out about the hosts.\n");
		System.out.printf("When the scheduled downtime expires, Blue will send out notifications for the hosts as it normally would.  Scheduled downtimes are preserved\n");
		System.out.printf("across program shutdowns and restarts.  Both the start and end times should be specified in the following format:  <b>mm/dd/yyyy hh:mm:ss</b>.\n");
		System.out.printf("If you select the <i>fixed</i> option, the downtime will be in effect between the start and end times you specify.  If you do not select the <i>fixed</i>\n");
		System.out.printf("option, Blue will treat this as \"flexible\" downtime.  Flexible downtime starts when a host goes down or becomes unreachable (sometime between the\n");
		System.out.printf("start and end times you specified) and lasts as long as the duration of time you enter.  The duration fields do not apply for fixed dowtime.\n");
		break;

	case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME:
		System.out.printf("This command is used to schedule downtime for all services in a particular hostgroup.  During the specified downtime, Blue will not send notifications out about the services.\n");
		System.out.printf("When the scheduled downtime expires, Blue will send out notifications for the services as it normally would.  Scheduled downtimes are preserved\n");
		System.out.printf("across program shutdowns and restarts.  Both the start and end times should be specified in the following format:  <b>mm/dd/yyyy hh:mm:ss</b>.\n");
		System.out.printf("If you select the <i>fixed</i> option, the downtime will be in effect between the start and end times you specify.  If you do not select the <i>fixed</i>\n");
		System.out.printf("option, Blue will treat this as \"flexible\" downtime.  Flexible downtime starts when a service enters a non-OK state (sometime between the\n");
		System.out.printf("start and end times you specified) and lasts as long as the duration of time you enter.  The duration fields do not apply for fixed dowtime.\n");
		System.out.printf("Note that scheduling downtime for services does not automatically schedule downtime for the hosts those services are associated with.  If you want to also schedule downtime for all hosts in the hostgroup, check the 'Schedule downtime for hosts too' option.\n");
		break;

	case common_h.CMD_START_EXECUTING_HOST_CHECKS:
		System.out.printf("This command is used to enable active host checks on a program-wide basis.\n");
		break;

	case common_h.CMD_STOP_EXECUTING_HOST_CHECKS:
		System.out.printf("This command is used to disable active host checks on a program-wide basis.\n");
		break;

	case common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS:
		System.out.printf("This command is used to have Blue start obsessing over host checks.  Read the documentation on distributed monitoring for more information on this.\n");
		break;

	case common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS:
		System.out.printf("This command is used to stop Blue from obsessing over host checks.\n");
		break;

	case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
		System.out.printf("This command is used to allow Blue to accept passive host check results that it finds in the external command file for a particular host.\n");
		break;

	case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
		System.out.printf("This command is used to stop Blue from accepting passive host check results that it finds in the external command file for a particular host.  All passive check results that are found for this host will be ignored.\n");
		break;

	case common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS:
		System.out.printf("This command is used to have Blue start obsessing over host checks.  Read the documentation on distributed monitoring for more information on this.\n");
		break;

	case common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS:
		System.out.printf("This command is used to stop Blue from obsessing over host checks.\n");
		break;

	case common_h.CMD_SCHEDULE_HOST_CHECK:
		System.out.printf("This command is used to schedule the next check of a particular host.  Blue will re-queue the host to be checked at the time you specify.\n");
		System.out.printf("If you select the <i>force check</i> option, Blue will force a check of the host regardless of both what time the scheduled check occurs and whether or not checks are enabled for the host.\n");
		break;

	case common_h.CMD_START_OBSESSING_OVER_SVC:
		System.out.printf("This command is used to have Blue start obsessing over a particular service.\n");
		break;

	case common_h.CMD_STOP_OBSESSING_OVER_SVC:
		System.out.printf("This command is used to stop Blue from obsessing over a particular service.\n");
		break;

	case common_h.CMD_START_OBSESSING_OVER_HOST:
		System.out.printf("This command is used to have Blue start obsessing over a particular host.\n");
		break;

	case common_h.CMD_STOP_OBSESSING_OVER_HOST:
		System.out.printf("This command is used to stop Blue from obsessing over a particular host.\n");
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for all services in the specified servicegroup.  Notifications will only be sent out for the\n");
		System.out.printf("service state types you defined in your service definitions.  This <i>does not</i> enable notifications for the hosts in this servicegroup unless you check the 'Enable for hosts too' option.\n");
		break;

	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for all services in the specified servicegroup.  You will have to re-enable notifications for\n");
		System.out.printf("all services in this servicegroup before any alerts can be sent out in the future.  This <i>does not</i> prevent notifications from being sent out about the hosts in this servicegroup unless you check the 'Disable for hosts too' option.\n");
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
		System.out.printf("This command is used to enable notifications for all hosts in the specified servicegroup.  Notifications will only be sent out for the\n");
		System.out.printf("host state types you defined in your host definitions.\n");
		break;

	case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
		System.out.printf("This command is used to prevent notifications from being sent out for all hosts in the specified servicegroup.  You will have to re-enable notifications for\n");
		System.out.printf("all hosts in this servicegroup before any alerts can be sent out in the future.\n");
		break;

	case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
		System.out.printf("This command is used to enable active checks of all services in the specified servicegroup.  This <i>does not</i> enable active checks of the hosts in the servicegroup unless you check the 'Enable for hosts too' option.\n");
		break;
		
	case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
		System.out.printf("This command is used to disable active checks of all services in the specified servicegroup.  This <i>does not</i> disable checks of the hosts in the servicegroup unless you check the 'Disable for hosts too' option.\n");
		break;

	case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
		System.out.printf("This command is used to schedule downtime for all hosts in a particular servicegroup.  During the specified downtime, Blue will not send notifications out about the hosts.\n");
		System.out.printf("When the scheduled downtime expires, Blue will send out notifications for the hosts as it normally would.  Scheduled downtimes are preserved\n");
		System.out.printf("across program shutdowns and restarts.  Both the start and end times should be specified in the following format:  <b>mm/dd/yyyy hh:mm:ss</b>.\n");
		System.out.printf("If you select the <i>fixed</i> option, the downtime will be in effect between the start and end times you specify.  If you do not select the <i>fixed</i>\n");
		System.out.printf("option, Blue will treat this as \"flexible\" downtime.  Flexible downtime starts when a host goes down or becomes unreachable (sometime between the\n");
		System.out.printf("start and end times you specified) and lasts as long as the duration of time you enter.  The duration fields do not apply for fixed dowtime.\n");
		break;

	case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:
		System.out.printf("This command is used to schedule downtime for all services in a particular servicegroup.  During the specified downtime, Blue will not send notifications out about the services.\n");
		System.out.printf("When the scheduled downtime expires, Blue will send out notifications for the services as it normally would.  Scheduled downtimes are preserved\n");
		System.out.printf("across program shutdowns and restarts.  Both the start and end times should be specified in the following format:  <b>mm/dd/yyyy hh:mm:ss</b>.\n");
		System.out.printf("If you select the <i>fixed</i> option, the downtime will be in effect between the start and end times you specify.  If you do not select the <i>fixed</i>\n");
		System.out.printf("option, Blue will treat this as \"flexible\" downtime.  Flexible downtime starts when a service enters a non-OK state (sometime between the\n");
		System.out.printf("start and end times you specified) and lasts as long as the duration of time you enter.  The duration fields do not apply for fixed dowtime.\n");
		System.out.printf("Note that scheduling downtime for services does not automatically schedule downtime for the hosts those services are associated with.  If you want to also schedule downtime for all hosts in the servicegroup, check the 'Schedule downtime for hosts too' option.\n");
		break;

	default:
		System.out.printf("Sorry, but no information is available for this command.");
	        }

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	return;
        }



/* converts a time string to a UNIX timestamp, respecting the date_format option */
public static long string_to_time(String buffer){

    SimpleDateFormat sdf;
	
	if(cgiutils.date_format==common_h.DATE_FORMAT_EURO)
        sdf = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" );
    else if(cgiutils.date_format==common_h.DATE_FORMAT_ISO8601)
        sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    else if( cgiutils.date_format==common_h.DATE_FORMAT_STRICT_ISO8601)
        sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
	else
        sdf = new SimpleDateFormat( "MM-dd-yyyy HH:mm:ss" );

    Date d = new Date();
    try { 
    d = sdf.parse(buffer);
    } catch ( Exception e ) {
    }
	return d.getTime()/1000;
        }


private static int atoi(String value) {
    try {
        return Integer.parseInt(value);
    } catch ( NumberFormatException nfE ) {
//        logger.throwing( cn, "atoi", nfE);
        return 0;
    }
}

    private static long strtoul(String value, Object ignore, int base ) {
        try {
            return Long.parseLong(value);
        } catch ( NumberFormatException nfE ) {
//            logger.throwing( cn, "atoi", nfE);
            return 0L;
        }
    }

}