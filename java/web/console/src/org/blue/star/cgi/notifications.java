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

import org.blue.star.common.objects;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class notifications extends blue_servlet {
public static final int  FIND_HOST		= 1;
public static final int  FIND_CONTACT		= 2;
public static final int  FIND_SERVICE		= 3;

public static final int  MAX_QUERYNAME_LENGTH	= 256;

public static final int  SERVICE_NOTIFICATION	= 0;
public static final int  HOST_NOTIFICATION	 = 1;

public static final String SERVICE_NOTIFICATION_STRING 	= "] SERVICE NOTIFICATION:";
public static final String HOST_NOTIFICATION_STRING	= "] HOST NOTIFICATION:";

public static cgiauth_h.authdata current_authdata;

public static String log_file_to_use;
public static int log_archive=0;

public static int query_type=FIND_HOST;
public static int find_all=common_h.TRUE;
public static String query_contact_name="";
public static String query_host_name="";
public static String query_svc_description="";

public static int notification_options= cgiutils_h.NOTIFICATION_ALL;
public static int use_lifo=common_h.TRUE;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;

public void reset_context() {
   current_authdata = new cgiauth_h.authdata();

   log_file_to_use = null;
   log_archive=0;

   query_type=FIND_HOST;
   find_all=common_h.TRUE;
   query_contact_name="";
   query_host_name="";
   query_svc_description="";

   notification_options= cgiutils_h.NOTIFICATION_ALL;
   use_lifo=common_h.TRUE;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;
}

public void call_main() {
   main( null );
}

public static void main(String[] args){
	int result=common_h.OK;
	String temp_buffer;
	String temp_buffer2;
	
	/* get the arguments passed in the URL */
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

	/* determine what log file we should use */
    log_file_to_use = cgiutils.get_log_archive_to_use(log_archive);


	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%%>\n");
		System.out.printf("<tr>\n");

		/* left column of top row */
		System.out.printf("<td align=left valign=top width=33%%>\n");
	
		if(query_type==FIND_SERVICE)
			temp_buffer = "Service Notifications";
		else if(query_type==FIND_HOST){
			if(find_all==common_h.TRUE)
				temp_buffer = "Notifications";
			else
				temp_buffer = "Host Notifications";
		        }
		else
			temp_buffer = "Contact Notifications";
		cgiutils.display_info_table(temp_buffer,common_h.FALSE,current_authdata);

		if(query_type==FIND_HOST || query_type==FIND_SERVICE){
			System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
			System.out.printf("<TR><TD CLASS='linkBox'>\n");
			if(query_type==FIND_HOST){
				System.out.printf("<A HREF='%s?host=%s'>View Status Detail For %s</A><BR>\n",cgiutils_h.STATUS_CGI,(find_all==common_h.TRUE)?"all":cgiutils.url_encode(query_host_name),(find_all==common_h.TRUE)?"All Hosts":"This Host");
				System.out.printf("<A HREF='%s?host=%s'>View History For %s</A><BR>\n",cgiutils_h.HISTORY_CGI,(find_all==common_h.TRUE)?"all":cgiutils.url_encode(query_host_name),(find_all==common_h.TRUE)?"All Hosts":"This Host");
//#ifdef USE_TRENDS
                // TODO Make this an option
				if(find_all==common_h.FALSE)
                    System.out.printf("<A HREF='%s?host=%s'>View Trends For This Host</A><BR>\n",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(query_host_name));
//#endif
	                        }
			else if(query_type==FIND_SERVICE){
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.HISTORY_CGI,(find_all==common_h.TRUE)?"all":cgiutils.url_encode(query_host_name));
				System.out.printf("service=%s'>View History For This Service</A><BR>\n",cgiutils.url_encode(query_svc_description));
//#ifdef USE_TRENDS
                // TODO Make this an option
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.TRENDS_CGI,(find_all==common_h.TRUE)?"all":cgiutils.url_encode(query_host_name));
				System.out.printf("service=%s'>View Trends For This Service</A><BR>\n",cgiutils.url_encode(query_svc_description));
//#endif
	                        }
			System.out.printf("</TD></TR>\n");
			System.out.printf("</TABLE>\n");
	                }

		System.out.printf("</td>\n");


		/* middle column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>\n");
		if(query_type==FIND_SERVICE)
			System.out.printf("Service '%s' On Host '%s'",query_svc_description,query_host_name);
		else if(query_type==FIND_HOST){
			if(find_all==common_h.TRUE)
				System.out.printf("All Hosts and Services");
			else
				System.out.printf("Host '%s'",query_host_name);
		        }
		else{
			if(find_all==common_h.TRUE)
				System.out.printf("All Contacts");
			else
				System.out.printf("Contact '%s'",query_contact_name);
		        }
		System.out.printf("</DIV>\n");
		System.out.printf("<BR>\n");

		if(query_type==FIND_SERVICE){
			temp_buffer = String.format("%s?%shost=%s&",cgiutils_h.NOTIFICATIONS_CGI,(use_lifo==common_h.FALSE)?"oldestfirst&":"",cgiutils.url_encode(query_host_name));
			temp_buffer2 = String.format( "service=%s&type=%d&",cgiutils.url_encode(query_svc_description),notification_options);
			temp_buffer += temp_buffer2;
	                }
		else
			temp_buffer = String.format( "%s?%s%s=%s&type=%d&",cgiutils_h.NOTIFICATIONS_CGI,(use_lifo==common_h.FALSE)?"oldestfirst&":"",(query_type==FIND_HOST)?"host":"contact",(query_type==FIND_HOST)?cgiutils.url_encode(query_host_name):cgiutils.url_encode(query_contact_name),notification_options);

        cgiutils.display_nav_table(temp_buffer,log_archive);

		System.out.printf("</td>\n");


		/* right hand column of top row */
		System.out.printf("<td align=right valign=top width=33%%>\n");

		System.out.printf("<table border=0 CLASS='optBox'>\n");
		System.out.printf("<form method='GET' action='%s'>\n",cgiutils_h.NOTIFICATIONS_CGI);
		if(query_type==FIND_SERVICE){
			System.out.printf("<input type='hidden' name='host' value='%s'>\n",query_host_name);
			System.out.printf("<input type='hidden' name='service' value='%s'>\n",query_svc_description);
	                }
		else
			System.out.printf("<input type='hidden' name='%s' value='%s'>\n",(query_type==FIND_HOST)?"host":"contact",(query_type==FIND_HOST)?query_host_name:query_contact_name);
		System.out.printf("<input type='hidden' name='archive' value='%d'>\n",log_archive);
		System.out.printf("<tr>\n");
		if(query_type==FIND_SERVICE)
			System.out.printf("<td align=left colspan=2 CLASS='optBoxItem'>Notification detail level for this service:</td>");
		else
			System.out.printf("<td align=left colspan=2 CLASS='optBoxItem'>Notification detail level for %s %s%s:</td>",(find_all==common_h.TRUE)?"all":"this",(query_type==FIND_HOST)?"host":"contact",(find_all==common_h.TRUE)?"s":"");
		System.out.printf("</tr>\n");
		System.out.printf("<tr>\n");
		System.out.printf("<td align=left colspan=2 CLASS='optBoxItem'><select name='type'>\n");
		System.out.printf("<option value=%d %s>All notifications\n",cgiutils_h.NOTIFICATION_ALL,(notification_options==cgiutils_h.NOTIFICATION_ALL)?"selected":"");
		if(query_type!=FIND_SERVICE){
			System.out.printf("<option value=%d %s>All service notifications\n",cgiutils_h.NOTIFICATION_SERVICE_ALL,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_ALL)?"selected":"");
			System.out.printf("<option value=%d %s>All host notifications\n",cgiutils_h.NOTIFICATION_HOST_ALL,(notification_options==cgiutils_h.NOTIFICATION_HOST_ALL)?"selected":"");
	                }
		System.out.printf("<option value=%d %s>Service acknowledgements\n",cgiutils_h.NOTIFICATION_SERVICE_ACK,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_ACK)?"selected":"");
		System.out.printf("<option value=%d %s>Service warning\n",cgiutils_h.NOTIFICATION_SERVICE_WARNING,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_WARNING)?"selected":"");
		System.out.printf("<option value=%d %s>Service unknown\n",cgiutils_h.NOTIFICATION_SERVICE_UNKNOWN,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_UNKNOWN)?"selected":"");
		System.out.printf("<option value=%d %s>Service critical\n",cgiutils_h.NOTIFICATION_SERVICE_CRITICAL,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_CRITICAL)?"selected":"");
		System.out.printf("<option value=%d %s>Service recovery\n",cgiutils_h.NOTIFICATION_SERVICE_RECOVERY,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_RECOVERY)?"selected":"");
		System.out.printf("<option value=%d %s>Service flapping\n",cgiutils_h.NOTIFICATION_SERVICE_FLAP,(notification_options==cgiutils_h.NOTIFICATION_SERVICE_FLAP)?"selected":"");
		if(query_type!=FIND_SERVICE){
			System.out.printf("<option value=%d %s>Host acknowledgements\n",cgiutils_h.NOTIFICATION_HOST_ACK,(notification_options==cgiutils_h.NOTIFICATION_HOST_ACK)?"selected":"");
			System.out.printf("<option value=%d %s>Host down\n",cgiutils_h.NOTIFICATION_HOST_DOWN,(notification_options==cgiutils_h.NOTIFICATION_HOST_DOWN)?"selected":"");
			System.out.printf("<option value=%d %s>Host unreachable\n",cgiutils_h.NOTIFICATION_HOST_UNREACHABLE,(notification_options==cgiutils_h.NOTIFICATION_HOST_UNREACHABLE)?"selected":"");
			System.out.printf("<option value=%d %s>Host recovery\n",cgiutils_h.NOTIFICATION_HOST_RECOVERY,(notification_options==cgiutils_h.NOTIFICATION_HOST_RECOVERY)?"selected":"");
			System.out.printf("<option value=%d %s>Host flapping\n",cgiutils_h.NOTIFICATION_HOST_FLAP,(notification_options==cgiutils_h.NOTIFICATION_HOST_FLAP)?"selected":"");
	                }
		System.out.printf("</select></td>\n");
		System.out.printf("</tr>\n");
		System.out.printf("<tr>\n");
		System.out.printf("<td align=left CLASS='optBoxItem'>Older Entries First:</td>\n");
		System.out.printf("<td></td>\n");
		System.out.printf("</tr>\n");
		System.out.printf("<tr>\n");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='checkbox' name='oldestfirst' %s></td>",(use_lifo==common_h.FALSE)?"checked":"");
		System.out.printf("<td align=right CLASS='optBoxItem'><input type='submit' value='Update'></td>\n");
		System.out.printf("</tr>\n");

		/* display context-sensitive help */
		System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
        cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_NOTIFICATIONS);
		System.out.printf("</td></tr>\n");

		System.out.printf("</form>\n");
		System.out.printf("</table>\n");

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");

	        }


	/* display notifications */
    display_notifications();

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
      
      	date_time = cgiutils.get_time_string(0,common_h.HTTP_DATE_TIME);
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      	
      	date_time = cgiutils.get_time_string(0,common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      	
      	System.out.printf("Content-type: text/html\r\n\r\n");
    }
    
	if(embedded==common_h.TRUE)
		return;

	System.out.printf("<html>\n");
	System.out.printf("<head>\n");
	System.out.printf("<title>\n");
	System.out.printf("Alert Notifications\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.NOTIFICATIONS_CSS);
	        }
	
	System.out.printf("</head>\n");

	System.out.printf("<body CLASS='notifications'>\n");

	/* include user SSI header */
    cgiutils.include_ssi_files(cgiutils_h.NOTIFICATIONS_CGI,cgiutils_h.SSI_HEADER);

	return;
        }



public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.NOTIFICATIONS_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars(request_string);

	for(x=0; x < variables.length ;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the host argument */
		else if( variables[x].equals("host")){
			query_type=FIND_HOST;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			query_host_name=variables[x];
			if(query_host_name==null)
				query_host_name="";
			if(query_host_name.equals("all"))
				find_all=common_h.TRUE;
			else
				find_all=common_h.FALSE;
		        }
	
		/* we found the contact argument */
		else if(variables[x].equals("contact")){
			query_type=FIND_CONTACT;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			query_contact_name=variables[x];

			if(query_contact_name==null)
				query_contact_name="";
			if(query_contact_name.equals("all"))
				find_all=common_h.TRUE;
			else
				find_all=common_h.FALSE;
		        }
	
		/* we found the service argument */
		else if(variables[x].equals("service")){
			query_type=FIND_SERVICE;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			query_svc_description=variables[x];
			if(query_svc_description==null)
				query_svc_description="";
		        }
	
		/* we found the notification type argument */
		else if(variables[x].equals("type")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			notification_options=atoi(variables[x]);
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
                }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



public static void display_notifications(){
	cgiutils_h.mmapfile thefile = null;
	String input=null;
	String temp_buffer;
	String date_time;
	String alert_level;
	String alert_level_class = "";
	String contact_name;
	String service_name = "";
	String host_name;
	String method_name;
	int show_entry;
	int total_notifications;
	int notification_type=SERVICE_NOTIFICATION;
	int notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_CRITICAL;
	int odd=0;
	long t;
	objects_h.host temp_host;
	objects_h.service temp_service;
	int result;

	if(use_lifo==common_h.TRUE){
		result=cgiutils.read_file_into_lifo(log_file_to_use);
		if(result!=cgiutils_h.LIFO_OK){
			if(result==cgiutils_h.LIFO_ERROR_MEMORY){
				System.out.printf("<P><DIV CLASS='warningMessage'>Not enough memory to reverse log file - displaying notifications in natural order...</DIV></P>");
			        }
			else if(result==cgiutils_h.LIFO_ERROR_FILE){
				System.out.printf("<P><DIV CLASS='errorMessage'>Error: Cannot open log file '%s' for reading!</DIV></P>",log_file_to_use);
				return;
			        }
			use_lifo=common_h.FALSE;
		        }
	        }

	if(use_lifo==common_h.FALSE){

		if((thefile=cgiutils.mmap_fopen(log_file_to_use))==null){
			System.out.printf("<P><DIV CLASS='errorMessage'>Error: Cannot open log file '%s' for reading!</DIV></P>",log_file_to_use);
			return;
		        }
	        }

	System.out.printf("<p>\n");
	System.out.printf("<div align='center'>\n");

	System.out.printf("<table border=0 CLASS='notifications'>\n");
	System.out.printf("<tr>\n");
	System.out.printf("<th CLASS='notifications'>Host</th>\n");
	System.out.printf("<th CLASS='notifications'>Service</th>\n");
	System.out.printf("<th CLASS='notifications'>Type</th>\n");
	System.out.printf("<th CLASS='notifications'>Time</th>\n");
	System.out.printf("<th CLASS='notifications'>Contact</th>\n");
	System.out.printf("<th CLASS='notifications'>Notification Command</th>\n");
	System.out.printf("<th CLASS='notifications'>Information</th>\n");
	System.out.printf("</tr>\n");

	total_notifications=0;
  
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

		/* see if this line contains the notification event string */
		if(input.contains(HOST_NOTIFICATION_STRING)||input.contains(SERVICE_NOTIFICATION_STRING)){

			if(input.contains(HOST_NOTIFICATION_STRING))
				notification_type=HOST_NOTIFICATION;
			else
				notification_type=SERVICE_NOTIFICATION;
      
			/* get the date/time */
            String[] split = input.split( "[\\]]" , 2 );
//          temp_buffer = my_strtok(input2,"]");
            t =( split[0].trim().length() <= 1 )?0: strtoul( split[0].substring(1),null,10);
            date_time = cgiutils.get_time_string(t,common_h.SHORT_DATE_TIME).trim();
            if ( split.length == 2 )
                temp_buffer = split[1];
            else 
                temp_buffer = "";

            /* get the contact name */
            temp_buffer = split ( split, "[:]" );
            temp_buffer = split ( split, "[;]" );
			contact_name = (temp_buffer==null)?"":temp_buffer.substring(1);

			/* get the host name */
            temp_buffer = split ( split, "[;]" );
			host_name = (temp_buffer==null)?"":temp_buffer;

			/* get the service name */
			if(notification_type==SERVICE_NOTIFICATION){
                temp_buffer = split ( split, "[;]" );
				service_name = (temp_buffer==null)?"":temp_buffer;
			        }

			/* get the alert level */
            temp_buffer = split ( split, "[;]" );
			alert_level = (temp_buffer==null)?"":temp_buffer;

			if(notification_type==SERVICE_NOTIFICATION){

				if(alert_level.equals("CRITICAL")){
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_CRITICAL;
					alert_level_class = "CRITICAL";
				        }
				else if(alert_level.equals("WARNING")){
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_WARNING;
					alert_level_class = "WARNING";
				        }
				else if(alert_level.equals("RECOVERY") || alert_level.equals("OK")){
					alert_level = "OK";
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_RECOVERY;
					alert_level_class = "OK";
				        }
				else if(alert_level.contains("ACKNOWLEDGEMENT (")){
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_ACK;
					alert_level_class = "ACKNOWLEDGEMENT";
				        }
				else if(alert_level.contains("FLAPPINGSTART (")){
					alert_level = "FLAPPING START";
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_FLAP;
					alert_level_class = "UNKNOWN";
				        }
				else if(alert_level.contains("FLAPPINGSTOP (")){
					alert_level = "FLAPPING STOP";
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_FLAP;
					alert_level_class = "UNKNOWN";
				        }
				else{
					alert_level = "UNKNOWN";
					notification_detail_type=cgiutils_h.NOTIFICATION_SERVICE_UNKNOWN;
					alert_level_class = "UNKNOWN";
				        }
			        }

			else{

				if(alert_level.equals("DOWN")){
					alert_level = "HOST DOWN";
					alert_level_class = "HOSTDOWN";
					notification_detail_type=cgiutils_h.NOTIFICATION_HOST_DOWN;
				        }
				else if(alert_level.equals("UNREACHABLE")){
					alert_level = "HOST UNREACHABLE";
					alert_level_class = "HOSTUNREACHABLE";
					notification_detail_type=cgiutils_h.NOTIFICATION_HOST_UNREACHABLE;
				        }
				else if(alert_level.equals("RECOVERY") || alert_level.equals("UP")){
					alert_level="HOST UP";
					alert_level_class="HOSTUP";
					notification_detail_type=cgiutils_h.NOTIFICATION_HOST_RECOVERY;
				        }
				else if(alert_level.contains("ACKNOWLEDGEMENT (")){
					alert_level_class="HOSTACKNOWLEDGEMENT";
					notification_detail_type=cgiutils_h.NOTIFICATION_HOST_ACK;
				        }
				else if(alert_level.contains("FLAPPINGSTART (")){
					alert_level = "FLAPPING START" ;
					alert_level_class="UNKNOWN";
					notification_detail_type=cgiutils_h.NOTIFICATION_HOST_FLAP;
				        }
				else if(alert_level.contains("FLAPPINGSTOP (")){
					alert_level="FLAPPING STOP";
					alert_level_class="UNKNOWN";
					notification_detail_type=cgiutils_h.NOTIFICATION_HOST_FLAP;
				        }
			        }

			/* get the method name */
            temp_buffer = split ( split, "[;]" );
			method_name = (temp_buffer==null)?"":temp_buffer;

			/* move to the informational message */
            temp_buffer = split ( split, "[;]" );

			show_entry=common_h.FALSE;
      
			/* if we're searching by contact, filter out unwanted contact */
			if(query_type==FIND_CONTACT){
				if(find_all==common_h.TRUE)
					show_entry=common_h.TRUE;
				else if(query_contact_name.equals(contact_name))
					show_entry=common_h.TRUE;
			        }

			else if(query_type==FIND_HOST){
				if(find_all==common_h.TRUE)
					show_entry=common_h.TRUE;
				else if( query_host_name.equals(host_name))
					show_entry=common_h.TRUE;
			        }

			else if(query_type==FIND_SERVICE){
				if( query_host_name.equals(host_name) &&  query_svc_description.equals(service_name))
					show_entry=common_h.TRUE;
			        }

			if(show_entry==common_h.TRUE){
				if(notification_options==cgiutils_h.NOTIFICATION_ALL)
					show_entry=common_h.TRUE;
				else if(notification_options==cgiutils_h.NOTIFICATION_HOST_ALL && notification_type==HOST_NOTIFICATION)
					show_entry=common_h.TRUE;
				else if(notification_options==cgiutils_h.NOTIFICATION_SERVICE_ALL && notification_type==SERVICE_NOTIFICATION)
					show_entry=common_h.TRUE;
				else if ( (notification_detail_type & notification_options) != 0 )
					show_entry=common_h.TRUE;
				else 
					show_entry=common_h.FALSE;
			        }

			/* make sure user has authorization to view this notification */
			if(notification_type==HOST_NOTIFICATION){
				temp_host=objects.find_host(host_name);
				if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
					show_entry=common_h.FALSE;
			        }
			else{
				temp_service=objects.find_service(host_name,service_name);
				if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
					show_entry=common_h.FALSE;
			        }

			if(show_entry==common_h.TRUE){

				total_notifications++;

				if(odd!=0){
					odd=0;
					System.out.printf("<tr CLASS='notificationsOdd'>\n");
				        }
				else{
					odd=1;
					System.out.printf("<tr CLASS='notificationsEven'>\n");
				        }
				System.out.printf("<td CLASS='notifications%s'><a href='%s?type=%d&host=%s'>%s</a></td>\n",(odd!=0)?"Even":"Odd",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(host_name),host_name);
				if(notification_type==SERVICE_NOTIFICATION){
					System.out.printf("<td CLASS='notifications%s'><a href='%s?type=%d&host=%s",(odd!=0)?"Even":"Odd",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(host_name));
					System.out.printf("&service=%s'>%s</a></td>\n",cgiutils.url_encode(service_name),service_name);
				        }
				else
					System.out.printf("<td CLASS='notifications%s'>N/A</td>\n",(odd!=0)?"Even":"Odd");
				System.out.printf("<td CLASS='notifications%s'>%s</td>\n",alert_level_class,alert_level);
				System.out.printf("<td CLASS='notifications%s'>%s</td>\n",(odd!=0)?"Even":"Odd",date_time);
				System.out.printf("<td CLASS='notifications%s'><a href='%s?type=contacts#%s'>%s</a></td>\n",(odd!=0)?"Even":"Odd",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(contact_name),contact_name);
				System.out.printf("<td CLASS='notifications%s'><a href='%s?type=commands#%s'>%s</a></td>\n",(odd!=0)?"Even":"Odd",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(method_name),method_name);
				System.out.printf("<td CLASS='notifications%s'>%s</td>\n",(odd!=0)?"Even":"Odd",temp_buffer);
				System.out.printf("</tr>\n");
			        }
		        }
	        }


	System.out.printf("</table>\n");

	System.out.printf("</div>\n");
	System.out.printf("</p>\n");

	if(total_notifications==0){
		System.out.printf("<P><DIV CLASS='errorMessage'>No notifications have been recorded");
		if(find_all==common_h.FALSE){
			if(query_type==FIND_SERVICE)
				System.out.printf(" for this service");
			else if(query_type==FIND_CONTACT)
				System.out.printf(" for this contact");
			else
				System.out.printf(" for this host");
		        }
		System.out.printf(" in %s log file</DIV></P>",(log_archive==0)?"the current":"this archived");
	        }

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