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

import java.util.Calendar;

import org.blue.star.common.objects;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class history extends blue_servlet {
    
public static final int DISPLAY_HOSTS			= 0;
public static final int DISPLAY_SERVICES		= 1;

public static final int SERVICE_HISTORY			= 0;
public static final int HOST_HISTORY			= 1;
public static final int SERVICE_FLAPPING_HISTORY        = 2;
public static final int HOST_FLAPPING_HISTORY           = 3;
public static final int SERVICE_DOWNTIME_HISTORY        = 4;
public static final int HOST_DOWNTIME_HISTORY           = 5;

public static final int STATE_ALL			= 0;
public static final int STATE_SOFT			= 1;
public static final int STATE_HARD			= 2;

public static cgiauth_h.authdata current_authdata;

public static String log_file_to_use;
public static int log_archive=0;

public static int show_all_hosts=common_h.TRUE;
public static  String host_name="all";
public static String svc_description="";
public static int display_type=DISPLAY_HOSTS;
public static int use_lifo=common_h.TRUE;

public static int history_options=cgiutils_h.HISTORY_ALL;
public static int state_options=STATE_ALL;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;
public static int display_frills=common_h.TRUE;
public static int display_timebreaks=common_h.TRUE;
public static int display_system_messages=common_h.TRUE;
public static int display_flapping_alerts=common_h.TRUE;
public static int display_downtime_alerts=common_h.TRUE;

public void reset_context() {
   current_authdata = new cgiauth_h.authdata();

   log_file_to_use = null;
   log_archive=0;

   show_all_hosts=common_h.TRUE;
   host_name="all";
   svc_description="";
   display_type=DISPLAY_HOSTS;
   use_lifo=common_h.TRUE;

   history_options=cgiutils_h.HISTORY_ALL;
   state_options=STATE_ALL;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;
   display_frills=common_h.TRUE;
   display_timebreaks=common_h.TRUE;
   display_system_messages=common_h.TRUE;
   display_flapping_alerts=common_h.TRUE;
   display_downtime_alerts=common_h.TRUE;
   
}

public void call_main() {
   main( null );
}

public static void main(String[] args){
	int result=common_h.OK;
	String temp_buffer;
	String temp_buffer2;

	/* get the variables passed to us */
	process_cgivars();

	/* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		cgiutils.main_config_file_error(cgiutils.main_config_file);
		document_footer();
        cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.object_data_error();
		document_footer();
        cgiutils.exit(  common_h.ERROR );
        return;
                }

	document_header(common_h.TRUE);

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	/* determine what log file we should be using */
    log_file_to_use = cgiutils.get_log_archive_to_use(log_archive );

	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%%>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top width=33%%>\n");

		if(display_type==DISPLAY_SERVICES)
			temp_buffer ="Service Alert History";
		else if(show_all_hosts==common_h.TRUE)
			temp_buffer = "Alert History";
		else
			temp_buffer = "Host Alert History";

		cgiutils.display_info_table(temp_buffer,common_h.FALSE,current_authdata);

		System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
		System.out.printf("<TR><TD CLASS='linkBox'>\n");
		if(display_type==DISPLAY_HOSTS){
			System.out.printf("<A HREF='%s?host=%s'>View Status Detail For %s</A><BR>\n",cgiutils_h.STATUS_CGI,(show_all_hosts==common_h.TRUE)?"all":cgiutils.url_encode(host_name),(show_all_hosts==common_h.TRUE)?"All Hosts":"This Host");
			System.out.printf("<A HREF='%s?host=%s'>View Notifications For %s</A><BR>\n",cgiutils_h.NOTIFICATIONS_CGI,(show_all_hosts==common_h.TRUE)?"all":cgiutils.url_encode(host_name),(show_all_hosts==common_h.TRUE)?"All Hosts":"This Host");
//#ifdef USE_TRENDS
			if(show_all_hosts==common_h.FALSE)
				System.out.printf("<A HREF='%s?host=%s'>View Trends For This Host</A>\n",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
//#endif
	                }
		else{
			System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
			System.out.printf("service=%s'>View Notifications For This Service</A><BR>\n",cgiutils.url_encode(svc_description));
//#ifdef USE_TRENDS
			System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
			System.out.printf("service=%s'>View Trends For This Service</A><BR>\n",cgiutils.url_encode(svc_description));
//#endif
			System.out.printf("<A HREF='%s?host=%s'>View History For This Host</A>\n",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
	                }
		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</td>\n");


		/* middle column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>\n");
		if(display_type==DISPLAY_SERVICES)
			System.out.printf("Service '%s' On Host '%s'",svc_description,host_name);
		else if(show_all_hosts==common_h.TRUE)
			System.out.printf("All Hosts and Services");
		else
			System.out.printf("Host '%s'",host_name);
		System.out.printf("</DIV>\n");
		System.out.printf("<BR>\n");

		temp_buffer = String.format( "%s?%shost=%s&type=%d&statetype=%d&",cgiutils_h.HISTORY_CGI,(use_lifo==common_h.FALSE)?"oldestfirst&":"",cgiutils.url_encode(host_name),history_options,state_options);

		if(display_type==DISPLAY_SERVICES){
			temp_buffer2 = String.format( "service=%s&",cgiutils.url_encode(svc_description));
			temp_buffer += temp_buffer2;
	                }
		cgiutils.display_nav_table(temp_buffer,log_archive);

		System.out.printf("</td>\n");


		/* right hand column of top row */
		System.out.printf("<td align=right valign=top width=33%%>\n");

		System.out.printf("<table border=0 CLASS='optBox'>\n");
		System.out.printf("<form method=\"GET\" action=\"%s\">\n",cgiutils_h.HISTORY_CGI);
		System.out.printf("<input type='hidden' name='host' value='%s'>\n",(show_all_hosts==common_h.TRUE)?"all":host_name);
		if(display_type==DISPLAY_SERVICES)
			System.out.printf("<input type='hidden' name='service' value='%s'>\n",svc_description);
		System.out.printf("<input type='hidden' name='archive' value='%d'>\n",log_archive);

		System.out.printf("<tr>\n");
		System.out.printf("<td align=left CLASS='optBoxItem'>State type options:</td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("<tr>\n");
		System.out.printf("<td align=left CLASS='optBoxItem'><select name='statetype'>\n");
		System.out.printf("<option value=%d %s>All state types\n",STATE_ALL,(state_options==STATE_ALL)?"selected":"");
		System.out.printf("<option value=%d %s>Soft states\n",STATE_SOFT,(state_options==STATE_SOFT)?"selected":"");
		System.out.printf("<option value=%d %s>Hard states\n",STATE_HARD,(state_options==STATE_HARD)?"selected":"");
		System.out.printf("</select></td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("<tr>\n");
		System.out.printf("<td align=left CLASS='optBoxItem'>History detail level for ");
		if(display_type==DISPLAY_HOSTS)
			System.out.printf("%s host%s",(show_all_hosts==common_h.TRUE)?"all":"this",(show_all_hosts==common_h.TRUE)?"s":"");
		else
			System.out.printf("service");
		System.out.printf(":</td>\n");
		System.out.printf("</tr>\n")
;
		System.out.printf("<tr>\n");
		System.out.printf("<td align=left CLASS='optBoxItem'><select name='type'>\n");
		if(display_type==DISPLAY_HOSTS)
			System.out.printf("<option value=%d %s>All alerts\n",cgiutils_h.HISTORY_ALL,(history_options==cgiutils_h.HISTORY_ALL)?"selected":"");
		System.out.printf("<option value=%d %s>All service alerts\n",cgiutils_h.HISTORY_SERVICE_ALL,(history_options==cgiutils_h.HISTORY_SERVICE_ALL)?"selected":"");
		if(display_type==DISPLAY_HOSTS)
			System.out.printf("<option value=%d %s>All host alerts\n",cgiutils_h.HISTORY_HOST_ALL,(history_options==cgiutils_h.HISTORY_HOST_ALL)?"selected":"");
		System.out.printf("<option value=%d %s>Service warning\n",cgiutils_h.HISTORY_SERVICE_WARNING,(history_options==cgiutils_h.HISTORY_SERVICE_WARNING)?"selected":"");
		System.out.printf("<option value=%d %s>Service unknown\n",cgiutils_h.HISTORY_SERVICE_UNKNOWN,(history_options==cgiutils_h.HISTORY_SERVICE_UNKNOWN)?"selected":"");
		System.out.printf("<option value=%d %s>Service critical\n",cgiutils_h.HISTORY_SERVICE_CRITICAL,(history_options==cgiutils_h.HISTORY_SERVICE_CRITICAL)?"selected":"");
		System.out.printf("<option value=%d %s>Service recovery\n",cgiutils_h.HISTORY_SERVICE_RECOVERY,(history_options==cgiutils_h.HISTORY_SERVICE_RECOVERY)?"selected":"");
		if(display_type==DISPLAY_HOSTS){
			System.out.printf("<option value=%d %s>Host down\n",cgiutils_h.HISTORY_HOST_DOWN,(history_options==cgiutils_h.HISTORY_HOST_DOWN)?"selected":"");
			System.out.printf("<option value=%d %s>Host unreachable\n",cgiutils_h.HISTORY_HOST_UNREACHABLE,(history_options==cgiutils_h.HISTORY_HOST_UNREACHABLE)?"selected":"");
		        System.out.printf("<option value=%d %s>Host recovery\n",cgiutils_h.HISTORY_HOST_RECOVERY,(history_options==cgiutils_h.HISTORY_HOST_RECOVERY)?"selected":"");
	                }
		System.out.printf("</select></td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("<tr>\n");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='checkbox' name='noflapping' %s> Hide Flapping Alerts</td>",(display_flapping_alerts==common_h.FALSE)?"checked":"");
		System.out.printf("</tr>\n");
		System.out.printf("<tr>\n");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='checkbox' name='nodowntime' %s> Hide Downtime Alerts</td>",(display_downtime_alerts==common_h.FALSE)?"checked":"");
		System.out.printf("</tr>\n");

		System.out.printf("<tr>\n");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='checkbox' name='nosystem' %s> Hide Process Messages</td>",(display_system_messages==common_h.FALSE)?"checked":"");
		System.out.printf("</tr>\n");
		System.out.printf("<tr>\n");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='checkbox' name='oldestfirst' %s> Older Entries First</td>",(use_lifo==common_h.FALSE)?"checked":"");
		System.out.printf("</tr>\n");

		System.out.printf("<tr>\n");
		System.out.printf("<td align=left CLASS='optBoxItem'><input type='submit' value='Update'></td>\n");
		System.out.printf("</tr>\n");

		/* display context-sensitive help */
		System.out.printf("<tr>\n");
		System.out.printf("<td align=right>\n");
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTORY);
		System.out.printf("</td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("</form>\n");
		System.out.printf("</table>\n");

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");

	        }


	/* display history */
	get_history();

	document_footer();

    cgiutils.exit(  common_h.OK );
}



public static void document_header(int use_stylesheet){
	String date_time;

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );
       response.setContentType("text/html");
    } else {
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      
      	System.out.printf("Content-type: text/html\r\n\r\n");
    }
    
	if(embedded==common_h.TRUE)
		return;

	System.out.printf("<html>\n");
	System.out.printf("<head>\n");
	System.out.printf("<title>\n");
	System.out.printf("Blue History\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.HISTORY_CSS);
	        }

	System.out.printf("</head>\n");
	System.out.printf("<BODY CLASS='history'>\n");

	/* include user SSI header */
    cgiutils.include_ssi_files(cgiutils_h.HISTORY_CGI,cgiutils_h.SSI_HEADER);

	    }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.HISTORY_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars( request_string );

	for(x=0; x < variables.length ;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1)
			continue;

		/* we found the host argument */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_name = variables[x];

			display_type=DISPLAY_HOSTS;

			if( host_name.equals("all"))
				show_all_hosts=common_h.TRUE;
			else
				show_all_hosts=common_h.FALSE;
		        }
	
		/* we found the service argument */
		else if(variables[x].equals("service")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				svc_description = variables[x];

			display_type=DISPLAY_SERVICES;
		        }
	
	
		/* we found the history type argument */
		else if(variables[x].equals("type")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			history_options=atoi(variables[x]);
		        }
	
		/* we found the history state type argument */
		else if(variables[x].equals("statetype")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			state_options=atoi(variables[x]);
		        }
	
	
		/* we found the log archive argument */
		else if(variables[x].equals("archive")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			log_archive=atoi(variables[x]);
			if(log_archive<0)
				log_archive=0;
		        }

		/* we found the order argument */
		else if(variables[x].equals("oldestfirst")){
			use_lifo=common_h.FALSE;
		        }

		/* we found the embed option */
		else if(variables[x].equals("embedded"))
			embedded=common_h.TRUE;

		/* we found the noheader option */
		else if(variables[x].equals("noheader"))
			display_header=common_h.FALSE;

		/* we found the nofrills option */
		else if(variables[x].equals("nofrills"))
			display_frills=common_h.FALSE;

		/* we found the notimebreaks option */
		else if(variables[x].equals("notimebreaks"))
			display_timebreaks=common_h.FALSE;

		/* we found the no system messages option */
		else if(variables[x].equals("nosystem"))
			display_system_messages=common_h.FALSE;

		/* we found the no flapping alerts option */
		else if(variables[x].equals("noflapping"))
			display_flapping_alerts=common_h.FALSE;

		/* we found the no downtime alerts option */
		else if(variables[x].equals("nodowntime"))
			display_downtime_alerts=common_h.FALSE;
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



public static void get_history(){
	cgiutils_h.mmapfile thefile = null;
	String image ;
	String image_alt ;
	String input=null;
	String input_buffer2 ;
	String match1 ="" ;
	String match2 ="";
	int found_line=common_h.FALSE;
	int system_message=common_h.FALSE;
	int display_line=common_h.FALSE;
	long t;
	String date_time ;
	String temp_buffer;
	int history_type=SERVICE_HISTORY;
	int history_detail_type=cgiutils_h.HISTORY_SERVICE_CRITICAL;
	String entry_host_name = "" ;
	String entry_service_desc = "";
	objects_h.host temp_host;
	objects_h.service temp_service;
	int result;

	String last_message_date ="";
	String current_message_date ="";
	Calendar time_ptr;


	if(use_lifo==common_h.TRUE){
		result=cgiutils.read_file_into_lifo(log_file_to_use);
		if(result!=cgiutils_h.LIFO_OK){
			if(result==cgiutils_h.LIFO_ERROR_MEMORY){
				System.out.printf("<P><DIV CLASS='warningMessage'>Not enough memory to reverse log file - displaying history in natural order...</DIV></P>\n");
			        }
			else if(result==cgiutils_h.LIFO_ERROR_FILE){
				System.out.printf("<HR><P><DIV CLASS='errorMessage'>Error: Cannot open log file '%s' for reading!</DIV></P><HR>",log_file_to_use);
				return;
			        }
			use_lifo=common_h.FALSE;
		        }
	        }

	if(use_lifo==common_h.FALSE){

		if((thefile=cgiutils.mmap_fopen(log_file_to_use))==null){
			System.out.printf("<HR><P><DIV CLASS='errorMessage'>Error: Cannot open log file '%s' for reading!</DIV></P><HR>",log_file_to_use);
			return;
		        }
	        }

	System.out.printf("<P><DIV CLASS='logEntries'>\n");

	while(true){

		if(use_lifo==common_h.TRUE){
			if((input=cgiutils.pop_lifo())==null)
				break;
		        }
		else{
			if((input=cgiutils.mmap_fgets(thefile))==null)
				break;
		        }

		input = input.trim();

		image = "";
		image_alt = "";
		system_message=common_h.FALSE;

		input_buffer2 = input;

		/* service state alerts */
		if(input.contains("SERVICE ALERT:")){
			
			history_type=SERVICE_HISTORY;

			/* get host and service names */
            String[] split = input_buffer2.split( "[\\]]", 2);
			temp_buffer=split(split,"[:]");
			temp_buffer=split(split,"[;]");
			entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);
            temp_buffer=split(split,"[;]");
			entry_service_desc = (temp_buffer==null)?"":temp_buffer;

			if(input.contains(";CRITICAL;")){
				image = cgiutils_h.CRITICAL_ICON;
				image_alt = cgiutils_h.CRITICAL_ICON_ALT;
				history_detail_type=cgiutils_h.HISTORY_SERVICE_CRITICAL;
                                }
			else if(input.contains(";WARNING;")){
				image = cgiutils_h.WARNING_ICON;
				image_alt = cgiutils_h.WARNING_ICON_ALT;
				history_detail_type=cgiutils_h.HISTORY_SERVICE_WARNING;
                                }
			else if(input.contains(";UNKNOWN;")){
				image = cgiutils_h.UNKNOWN_ICON;
				image_alt = cgiutils_h.UNKNOWN_ICON_ALT;
 				history_detail_type=cgiutils_h.HISTORY_SERVICE_UNKNOWN;
                                }
			else if(input.contains(";RECOVERY;") || input.contains(";OK;")){
				image = cgiutils_h.OK_ICON;
				image_alt = cgiutils_h.OK_ICON_ALT;
				history_detail_type=cgiutils_h.HISTORY_SERVICE_RECOVERY;
                                }
		        }

		/* service flapping alerts */
		else if(input.contains("SERVICE FLAPPING ALERT:")){

			if(display_flapping_alerts==common_h.FALSE)
				continue;
			
			history_type=SERVICE_FLAPPING_HISTORY;

			/* get host and service names */
            String[] split = input_buffer2.split( "[\\]]", 2);
            temp_buffer=split(split,"[:]");
            temp_buffer=split(split,"[;]");
			entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);
            temp_buffer=split(split,"[;]");
			entry_service_desc = (temp_buffer==null)?"":temp_buffer;

			image = cgiutils_h.FLAPPING_ICON;

			if(input.contains(";STARTED;"))
			        image_alt = "Service started flapping";
			else if(input.contains(";STOPPED;"))
			        image_alt = "Service stopped flapping";
			else if(input.contains(";DISABLED;"))
			        image_alt = "Service flap detection disabled";
		        }

		/* service downtime alerts */
		else if(input.contains("SERVICE DOWNTIME ALERT:")){
			
			if(display_downtime_alerts==common_h.FALSE)
				continue;
			
			history_type=SERVICE_DOWNTIME_HISTORY;

			/* get host and service names */
            String[] split = input_buffer2.split( "[\\]]", 2);
            temp_buffer=split(split,"[:]");
            temp_buffer=split(split,"[;]");
			entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);
            temp_buffer=split(split,"[;]");
			entry_service_desc = (temp_buffer==null)?"":temp_buffer;

			image = cgiutils_h.SCHEDULED_DOWNTIME_ICON;

			if(input.contains(";STARTED;"))
			        image_alt = "Service entered a period of scheduled downtime";
			else if(input.contains(";STOPPED;"))
			        image_alt = "Service exited from a period of scheduled downtime";
			else if(input.contains(";CANCELLED;"))
			        image_alt = "Service scheduled downtime has been cancelled";
		        }

		/* host state alerts */
		else if(input.contains("HOST ALERT:")){

			history_type=HOST_HISTORY;

			/* get host name */
            String[] split = input_buffer2.split( "[\\]]", 2);
            temp_buffer=split(split,"[:]");
            temp_buffer=split(split,"[;]");
			entry_host_name = (temp_buffer==null)?"":temp_buffer+1;

			if(input.contains(";DOWN;")){
				image = cgiutils_h.HOST_DOWN_ICON;
				image_alt = cgiutils_h.HOST_DOWN_ICON_ALT;
				history_detail_type=cgiutils_h.HISTORY_HOST_DOWN;
		                }
			else if(input.contains(";UNREACHABLE;")){
				image = cgiutils_h.HOST_UNREACHABLE_ICON;
				image_alt = cgiutils_h.HOST_UNREACHABLE_ICON_ALT;
				history_detail_type=cgiutils_h.HISTORY_HOST_UNREACHABLE;
		                }
			else if(input.contains(";RECOVERY") || input.contains(";UP;")){
				image = cgiutils_h.HOST_UP_ICON;
				image_alt = cgiutils_h.HOST_UP_ICON_ALT;
				history_detail_type=cgiutils_h.HISTORY_HOST_RECOVERY;
		                }
		        }

		/* host flapping alerts */
		else if(input.contains("HOST FLAPPING ALERT:")){
			
			if(display_flapping_alerts==common_h.FALSE)
				continue;
			
			history_type=HOST_FLAPPING_HISTORY;

			/* get host name */
            String[] split = input_buffer2.split( "[\\]]", 2);
            temp_buffer=split(split,"[:]");
            temp_buffer=split(split,"[;]");
			entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);

			image = cgiutils_h.FLAPPING_ICON;

			if(input.contains(";STARTED;"))
			        image_alt = "Host started flapping";
			else if(input.contains(";STOPPED;"))
			        image_alt = "Host stopped flapping";
			else if(input.contains(";DISABLED;"))
			        image_alt = "Host flap detection disabled";
		        }

		/* host downtime alerts */
		else if(input.contains("HOST DOWNTIME ALERT:")){
			
			if(display_downtime_alerts==common_h.FALSE)
				continue;
			
			history_type=HOST_DOWNTIME_HISTORY;

			/* get host name */
            String[] split = input_buffer2.split( "[\\]]", 2);
            temp_buffer=split(split,"[:]");
            temp_buffer=split(split,"[;]");
			entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);

			image = cgiutils_h.SCHEDULED_DOWNTIME_ICON;

			if(input.contains(";STARTED;"))
			        image_alt = "Host entered a period of scheduled downtime";
			else if(input.contains(";STOPPED;"))
			        image_alt = "Host exited from a period of scheduled downtime";
			else if(input.contains(";CANCELLED;"))
			        image_alt = "Host scheduled downtime has been cancelled";
		        }

		else if(display_system_messages==common_h.FALSE)
			continue;

		/* program start */
		else if(input.contains(" starting...")){
			image = cgiutils_h.START_ICON;
			image_alt = cgiutils_h.START_ICON_ALT;
			system_message=common_h.TRUE;
		        }

		/* normal program termination */
		else if(input.contains(" shutting down...")){
			image = cgiutils_h.STOP_ICON;
			image_alt = cgiutils_h.STOP_ICON_ALT;
			system_message=common_h.TRUE;
		        }

		/* abnormal program termination */
		else if(input.contains("Bailing out")){
			image = cgiutils_h.STOP_ICON;
			image_alt = cgiutils_h.STOP_ICON_ALT;
			system_message=common_h.TRUE;
		        }

		/* program restart */
		else if(input.contains(" restarting...")){
			image = cgiutils_h.RESTART_ICON;
			image_alt = cgiutils_h.RESTART_ICON_ALT;
			system_message=common_h.TRUE;
		        }

		/* get the timestamp */
        String[] split2 = input_buffer2.split( "[\\]]", 2);
        temp_buffer=split2[0];
		t=(temp_buffer==null)?0L:strtoul(temp_buffer+1,null,10);

        time_ptr = Calendar.getInstance();
		time_ptr.setTimeInMillis(t*1000);
        
		current_message_date = String.format( "%tB %td, %tY %tH:00\n",time_ptr, time_ptr, time_ptr, time_ptr);

        date_time = cgiutils.get_time_string(t , common_h.SHORT_DATE_TIME).trim();

		temp_buffer=split( split2, "[\\n]" );
        if ( temp_buffer == null ) temp_buffer = "";

		if(image.equals("")){

			display_line=common_h.FALSE;

			if(system_message==common_h.TRUE)
				display_line=common_h.TRUE;

			else if(display_type==DISPLAY_HOSTS){

				if(history_type==HOST_HISTORY || history_type==SERVICE_HISTORY){
					match1 = String.format( " HOST ALERT: %s;",host_name);
                    match2 = String.format( " SERVICE ALERT: %s;",host_name);
				        }
				else if(history_type==HOST_FLAPPING_HISTORY || history_type==SERVICE_FLAPPING_HISTORY){
                    match1 = String.format( " HOST FLAPPING ALERT: %s;",host_name);
                    match2 = String.format( " SERVICE FLAPPING ALERT: %s;",host_name);
				        }
				else if(history_type==HOST_DOWNTIME_HISTORY || history_type==SERVICE_DOWNTIME_HISTORY){
                    match1 = String.format( " HOST DOWNTIME ALERT: %s;",host_name);
                    match2 = String.format( " SERVICE DOWNTIME ALERT: %s;",host_name);
				        }

				if(show_all_hosts==common_h.TRUE)
					display_line=common_h.TRUE;
				else if( temp_buffer.contains(match1))
					display_line=common_h.TRUE;
				else if( temp_buffer.contains(match2))
					display_line=common_h.TRUE;

				if(display_line==common_h.TRUE){
					if(history_options==cgiutils_h.HISTORY_ALL)
						display_line=common_h.TRUE;
					else if(history_options==cgiutils_h.HISTORY_HOST_ALL && (history_type==HOST_HISTORY || history_type==HOST_FLAPPING_HISTORY || history_type==HOST_DOWNTIME_HISTORY))
						display_line=common_h.TRUE;
					else if(history_options==cgiutils_h.HISTORY_SERVICE_ALL && (history_type==SERVICE_HISTORY || history_type==SERVICE_FLAPPING_HISTORY || history_type==SERVICE_DOWNTIME_HISTORY))
						display_line=common_h.TRUE;
					else if((history_type==HOST_HISTORY || history_type==SERVICE_HISTORY) && ((history_detail_type & history_options)!=0))
						display_line=common_h.TRUE;
					else 
						display_line=common_h.FALSE;
			                }

				/* check alert state types */
				if(display_line==common_h.TRUE && (history_type==HOST_HISTORY || history_type==SERVICE_HISTORY)){
					if(state_options==STATE_ALL)
						display_line=common_h.TRUE;
					else if(((state_options & STATE_SOFT)!=0) && temp_buffer.contains(";SOFT;"))
						display_line=common_h.TRUE;
					else if(((state_options & STATE_HARD)!=0) && temp_buffer.contains(";HARD;"))
						display_line=common_h.TRUE;
					else
						display_line=common_h.FALSE;
				        }
			        }

			else if(display_type==DISPLAY_SERVICES){

				if(history_type==SERVICE_HISTORY)
                    match1 = String.format( " SERVICE ALERT: %s;%s;",host_name,svc_description);
				else if(history_type==SERVICE_FLAPPING_HISTORY)
                    match1 = String.format( " SERVICE FLAPPING ALERT: %s;%s;",host_name,svc_description);
				else if(history_type==SERVICE_DOWNTIME_HISTORY)
					match1 = String.format( " SERVICE DOWNTIME ALERT: %s;%s;",host_name,svc_description);

				if( temp_buffer.contains(match1) && (history_type==SERVICE_HISTORY || history_type==SERVICE_FLAPPING_HISTORY || history_type==SERVICE_DOWNTIME_HISTORY))
					display_line=common_h.TRUE;

				if(display_line==common_h.TRUE){
					if(history_options==cgiutils_h.HISTORY_ALL || history_options==cgiutils_h.HISTORY_SERVICE_ALL)
						display_line=common_h.TRUE;
					else if((history_options & history_detail_type)!=0)
						display_line=common_h.TRUE;
					else 
						display_line=common_h.FALSE;
			                }

				/* check alert state type */
				if(display_line==common_h.TRUE && history_type==SERVICE_HISTORY){

					if(state_options==STATE_ALL)
						display_line=common_h.TRUE;
					else if(((state_options & STATE_SOFT)!=0) && temp_buffer.contains(";SOFT;"))
						display_line=common_h.TRUE;
					else if( ((state_options & STATE_HARD)!=0) && temp_buffer.contains(";HARD;"))
						display_line=common_h.TRUE;
					else
						display_line=common_h.FALSE;
				        }
			        }


			/* make sure user is authorized to view this host or service information */
			if(system_message==common_h.FALSE){

				if(history_type==HOST_HISTORY || history_type==HOST_FLAPPING_HISTORY || history_type==HOST_DOWNTIME_HISTORY){
					temp_host=objects.find_host(entry_host_name);
					if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
						display_line=common_h.FALSE;
					
				        }
				else{
					temp_service=objects.find_service(entry_host_name,entry_service_desc);
					if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
						display_line=common_h.FALSE;
				        }
			        }
			
			/* display the entry if we should... */
			if(display_line==common_h.TRUE){

				if( !last_message_date.equals(current_message_date) && display_timebreaks==common_h.TRUE){
					System.out.printf("<BR CLEAR='all'>\n");
					System.out.printf("<DIV CLASS='dateTimeBreak'>\n");
					System.out.printf("<table border=0 width=95%%><tr>");
					System.out.printf("<td width=40%%><hr width=100%%></td>");
					System.out.printf("<td align=center CLASS='dateTimeBreak'>%s</td>",current_message_date);
					System.out.printf("<td width=40%%><hr width=100%%></td>");
					System.out.printf("</tr></table>\n");
					System.out.printf("</DIV>\n");
					System.out.printf("<BR CLEAR='all'><DIV CLASS='logEntries'>\n");
					last_message_date = current_message_date;
				        }

				if(display_frills==common_h.TRUE)
					System.out.printf("<img align='left' src='%s%s' alt='%s' title='%s'>",cgiutils.url_images_path,image,image_alt,image_alt);
				System.out.printf("[%s] %s<br clear='all'>\n",date_time,temp_buffer);
				found_line=common_h.TRUE;
			        }
		        }

                }

	System.out.printf("</P>\n");
	
	if(found_line==common_h.FALSE){
		System.out.printf("<HR>\n");
		System.out.printf("<P><DIV CLASS='warningMessage'>No history information was found ");
		if(display_type==DISPLAY_HOSTS)
			System.out.printf("%s",(show_all_hosts==common_h.TRUE)?"":"for this host ");
		else
			System.out.printf("for this this service ");
		System.out.printf("in %s log file</DIV></P>",(log_archive==0)?"the current":"this archived");
	        }

	System.out.printf("<HR>\n");

	if(use_lifo==common_h.TRUE)
        cgiutils.free_lifo_memory();
	else
        cgiutils.mmap_fclose(thefile);

	return;
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
//        logger.throwing( cn, "atoi", nfE);
        return 0L;
    }
}

/**
 * Re-split split[1] if it exists. Basically rolling splits on a new key to the left.
 * 
 * @param split
 * @param regex
 * @return
 */
private static String split ( String[] split, String regex ) {
        
        if ( split == null ) {
            return null;
        } else if ( split.length == 1 || split[1] == null ) {
            split[0] = null;
            return null;
        } else {
            String[] split2 = split[1].split(regex,2);
            split[0] = split2[0];
            if ( split2.length == 1 ) 
                split[1] = null;
            return split[1];
        }
}
}