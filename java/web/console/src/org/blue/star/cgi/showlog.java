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

import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;

public class showlog extends blue_servlet {
   
public static cgiauth_h.authdata current_authdata = new cgiauth_h.authdata();

public static String log_file_to_use="";
public static int log_archive=0;

public static int use_lifo=common_h.TRUE;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;
public static int display_frills=common_h.TRUE;
public static int display_timebreaks=common_h.TRUE;

public void reset_context() {
   log_file_to_use="";
   log_archive=0;
   use_lifo=common_h.TRUE;
   embedded=common_h.FALSE;
   display_header=common_h.TRUE;
   display_frills=common_h.TRUE;
   display_timebreaks=common_h.TRUE;
}

public void call_main() {
   main( null );
}

public static void main(String[] args){
	int result=common_h.OK;
	String temp_buffer;

	/* get the CGI variables passed in the URL */
	process_cgivars();

	/* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
		document_footer();
		cgiutils.exit( common_h.ERROR);
        return;
	        }

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.main_config_file_error(cgiutils.main_config_file);
		document_footer();
		cgiutils.exit( common_h.ERROR);
        return;
	        }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.object_data_error();
		document_footer();
		cgiutils.exit( common_h.ERROR);
        return;
                }


	document_header(common_h.TRUE);

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	/* determine what log file we should be using */
    log_file_to_use = cgiutils.get_log_archive_to_use(log_archive);

	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%% cellpadding=0 cellspacing=0>\n");
		System.out.printf("<tr>\n");

		/* left column of top table - info box */
		System.out.printf("<td align=left valign=top width=33%%>\n");
		cgiutils.display_info_table((cgiutils.log_rotation_method==common_h.LOG_ROTATION_NONE || log_archive==0)?"Current Event Log":"Archived Event Log",common_h.FALSE,current_authdata);
		System.out.printf("</td>\n");

		/* middle column of top table - log file navigation options */
		System.out.printf("<td align=center valign=top width=33%%>\n");
		temp_buffer = String.format( "%s?%s",cgiutils_h.SHOWLOG_CGI,(use_lifo==common_h.FALSE)?"oldestfirst&":"");
		cgiutils.display_nav_table(temp_buffer,log_archive);
		System.out.printf("</td>\n");

		/* right hand column of top row */
		System.out.printf("<td align=right valign=top width=33%%>\n");

		System.out.printf("<table border=0 cellspacing=0 cellpadding=0 CLASS='optBox'>\n");
		System.out.printf("<form method='GET' action='%s'>\n",cgiutils_h.SHOWLOG_CGI);
		System.out.printf("<input type='hidden' name='archive' value='%d'>\n",log_archive);
		System.out.printf("<tr>");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='checkbox' name='oldestfirst' %s> Older Entries First:</td>",(use_lifo==common_h.FALSE)?"checked":"");
		System.out.printf("</tr>\n");
		System.out.printf("<tr>");
		System.out.printf("<td align=left valign=bottom CLASS='optBoxItem'><input type='submit' value='Update'></td>\n");
		System.out.printf("</tr>\n");

		/* display context-sensitive help */
		System.out.printf("<tr>\n");
		System.out.printf("<td align=right>\n");
        cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_LOG);
		System.out.printf("</td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("</form>\n");
		System.out.printf("</table>\n");

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");
		System.out.printf("</p>\n");

	        }


	/* display the contents of the log file */
	display_log();

	document_footer();

	cgiutils.exit(  common_h.OK);
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
      	
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      
      	System.out.printf("Content-type: text/html\r\n\r\n");
    }
    
	if(embedded==common_h.TRUE)
		return;

	System.out.printf("<HTML>\n");
	System.out.printf("<HEAD>\n");
	System.out.printf("<TITLE>\n");
	System.out.printf("Blue Log File\n");
	System.out.printf("</TITLE>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.SHOWLOG_CSS);
	}

	System.out.printf("</HEAD>\n");
	System.out.printf("<BODY CLASS='showlog'>\n");

	/* include user SSI header */
    cgiutils.include_ssi_files(cgiutils_h.SHOWLOG_CGI,cgiutils_h.SSI_HEADER);

	return;
        }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.SHOWLOG_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</BODY>\n");
	System.out.printf("</HTML>\n");

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
			continue;
		        }

		/* we found the archive argument */
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
		else if (variables[x].equals("nofrills"))
			display_frills=common_h.FALSE;

		/* we found the notimebreaks option */
		else if(variables[x].equals("notimebreaks"))
			display_timebreaks=common_h.FALSE;

		/* we received an invalid argument */
		else
			error=common_h.TRUE;
	
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



/* display the contents of the log file */
public static int display_log(){
	String input=null;
	String image;
	String image_alt;
	long t;
	String temp_buffer;
	String date_time;
	int error;
	cgiutils_h.mmapfile thefile = null;
	String last_message_date="";
	String current_message_date="";
	Calendar time_ptr;


	/* check to see if the user is authorized to view the log file */
	if(cgiauth.is_authorized_for_system_information(current_authdata)==common_h.FALSE)
	{
		System.out.printf("<HR>\n");
		System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view the log file...</DIV><br><br>\n");
		System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>and check the authorization options in your CGI configuration file.</DIV>\n");
		System.out.printf("<HR>\n");
		return common_h.ERROR;
	        }

	error=common_h.FALSE;

	if(use_lifo==common_h.TRUE){
		error=cgiutils.read_file_into_lifo(log_file_to_use);
		if(error!=cgiutils_h.LIFO_OK){
			if(error==cgiutils_h.LIFO_ERROR_MEMORY){
				System.out.printf("<P><DIV CLASS='warningMessage'>Not enough memory to reverse log file - displaying log in natural order...</DIV></P>");
				error=common_h.FALSE;
			        }
			else
				error=common_h.TRUE;
			use_lifo=common_h.FALSE;
		        }
		else
			error=common_h.FALSE;
	        }

	if(use_lifo==common_h.FALSE){
  
	   if((thefile=cgiutils.mmap_fopen(log_file_to_use))==null)
	   {
	      System.out.printf("<HR>\n");
	      System.out.printf("<P><DIV CLASS='errorMessage'>Error: Could not open log file '%s' for reading!</DIV></P>",log_file_to_use);
	      System.out.printf("<HR>\n");
	      error=common_h.TRUE;
	   }
	}

	if(error==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='logEntries'>\n");
		
		while(true){

			if(use_lifo==common_h.TRUE){
				if((input=cgiutils.pop_lifo())==null)
					break;
			        }
			else if((input=cgiutils.mmap_fgets(thefile))==null)
				break;

			input = input.trim();
            if ( input.length() == 0 )
               continue;

			if(input.contains(" starting...")){
				image = cgiutils_h.START_ICON ;
				image_alt = cgiutils_h.START_ICON_ALT ;
			        }
			else if(input.contains(" shutting down...")){
				image = cgiutils_h.STOP_ICON ;
				image_alt = cgiutils_h.STOP_ICON_ALT ;
			        }
			else if(input.contains("Bailing out")){
				image = cgiutils_h.STOP_ICON ;
				image_alt = cgiutils_h.STOP_ICON_ALT ;
			        }
			else if(input.contains(" restarting...")){
				image = cgiutils_h.RESTART_ICON ;
				image_alt = cgiutils_h.RESTART_ICON_ALT ;
			        }
			else if(input.contains("HOST ALERT:") && input.contains(";DOWN;")){
				image = cgiutils_h.HOST_DOWN_ICON ;
				image_alt = cgiutils_h.HOST_DOWN_ICON_ALT ;
			        }
			else if(input.contains("HOST ALERT:") && input.contains(";UNREACHABLE;")){
				image = cgiutils_h.HOST_UNREACHABLE_ICON ;
				image_alt = cgiutils_h.HOST_UNREACHABLE_ICON_ALT ;
			        }
			else if(input.contains("HOST ALERT:") && (input.contains(";RECOVERY;") || input.contains(";UP;"))){
				image = cgiutils_h.HOST_UP_ICON ;
				image_alt = cgiutils_h.HOST_UP_ICON_ALT ;
			        }
			else if(input.contains("HOST NOTIFICATION:")){
				image = cgiutils_h.HOST_NOTIFICATION_ICON ;
				image_alt = cgiutils_h.HOST_NOTIFICATION_ICON_ALT ;
			        }
			else if(input.contains("SERVICE ALERT:") && input.contains(";CRITICAL;")){
				image = cgiutils_h.CRITICAL_ICON ;
				image_alt = cgiutils_h.CRITICAL_ICON_ALT ;
			        }
			else if(input.contains("SERVICE ALERT:") && input.contains(";WARNING;")){
				image = cgiutils_h.WARNING_ICON ;
				image_alt = cgiutils_h.WARNING_ICON_ALT ;
			        }
			else if(input.contains("SERVICE ALERT:") && input.contains(";UNKNOWN;")){
				image = cgiutils_h.UNKNOWN_ICON ;
				image_alt = cgiutils_h.UNKNOWN_ICON_ALT ;
			        }
			else if(input.contains("SERVICE ALERT:") && (input.contains(";RECOVERY;") || input.contains(";OK;"))){
				image = cgiutils_h.OK_ICON ;
				image_alt = cgiutils_h.OK_ICON_ALT ;
			        }
			else if(input.contains("SERVICE NOTIFICATION:")){
				image = cgiutils_h.NOTIFICATION_ICON ;
				image_alt = cgiutils_h.NOTIFICATION_ICON_ALT ;
			        }
			else if(input.contains("SERVICE EVENT HANDLER:")){
				image = cgiutils_h.SERVICE_EVENT_ICON ;
				image_alt = cgiutils_h.SERVICE_EVENT_ICON_ALT ;
			        }
			else if(input.contains("HOST EVENT HANDLER:")){
				image = cgiutils_h.HOST_EVENT_ICON ;
				image_alt = cgiutils_h.HOST_EVENT_ICON_ALT ;
			        }
			else if(input.contains("EXTERNAL COMMAND:")){
				image = cgiutils_h.EXTERNAL_COMMAND_ICON ;
				image_alt = cgiutils_h.EXTERNAL_COMMAND_ICON_ALT ;
			        }
			else if(input.contains("LOG ROTATION:")){
				image = cgiutils_h.LOG_ROTATION_ICON ;
				image_alt = cgiutils_h.LOG_ROTATION_ICON_ALT ;
			        }
			else if(input.contains("active mode...")){
				image = cgiutils_h.ACTIVE_ICON ;
				image_alt = cgiutils_h.ACTIVE_ICON_ALT ;
			        }
			else if(input.contains("standby mode...")){
				image = cgiutils_h.STANDBY_ICON ;
				image_alt = cgiutils_h.STANDBY_ICON_ALT ;
			        }
			else if(input.contains("SERVICE FLAPPING ALERT:") && input.contains(";STARTED;")){
				image = cgiutils_h.FLAPPING_ICON ;
				image_alt = "Service started flapping" ;
			        }
			else if(input.contains("SERVICE FLAPPING ALERT:") && input.contains(";STOPPED;")){
				image = cgiutils_h.FLAPPING_ICON ;
				image_alt = "Service stopped flapping" ;
			        }
			else if(input.contains("SERVICE FLAPPING ALERT:") && input.contains(";DISABLED;")){
				image = cgiutils_h.FLAPPING_ICON ;
				image_alt = "Service flap detection disabled" ;
			        }
			else if(input.contains("HOST FLAPPING ALERT:") && input.contains(";STARTED;")){
				image = cgiutils_h.FLAPPING_ICON ;
				image_alt = "Host started flapping" ;
			        }
			else if(input.contains("HOST FLAPPING ALERT:") && input.contains(";STOPPED;")){
				image = cgiutils_h.FLAPPING_ICON ;
				image_alt = "Host stopped flapping" ;
			        }
			else if(input.contains("HOST FLAPPING ALERT:") && input.contains(";DISABLED;")){
				image = cgiutils_h.FLAPPING_ICON ;
				image_alt = "Host flap detection disabled" ;
			        }
			else if(input.contains("SERVICE DOWNTIME ALERT:") && input.contains(";STARTED;")){
				image = cgiutils_h.SCHEDULED_DOWNTIME_ICON ;
				image_alt = "Service entered a period of scheduled downtime" ;
			        }
			else if(input.contains("SERVICE DOWNTIME ALERT:") && input.contains(";STOPPED;")){
				image = cgiutils_h.SCHEDULED_DOWNTIME_ICON ;
				image_alt = "Service exited a period of scheduled downtime" ;
			        }
			else if(input.contains("SERVICE DOWNTIME ALERT:") && input.contains(";CANCELLED;")){
				image = cgiutils_h.SCHEDULED_DOWNTIME_ICON ;
				image_alt = "Service scheduled downtime has been cancelled" ;
			        }
			else if(input.contains("HOST DOWNTIME ALERT:") && input.contains(";STARTED;")){
				image = cgiutils_h.SCHEDULED_DOWNTIME_ICON ;
				image_alt = "Host entered a period of scheduled downtime" ;
			        }
			else if(input.contains("HOST DOWNTIME ALERT:") && input.contains(";STOPPED;")){
				image = cgiutils_h.SCHEDULED_DOWNTIME_ICON ;
				image_alt = "Host exited a period of scheduled downtime" ;
			        }
			else if(input.contains("HOST DOWNTIME ALERT:") && input.contains(";CANCELLED;")){
				image = cgiutils_h.SCHEDULED_DOWNTIME_ICON ;
				image_alt = "Host scheduled downtime has been cancelled" ;
			        }
            else if(input.contains("SYSTEM:") && input.contains(";WARNING;")){
                image = cgiutils_h.WARNING_ICON ;
                image_alt = cgiutils_h.WARNING_ICON_ALT ;
                    }
			else{
				image = cgiutils_h.INFO_ICON ;
				image_alt = cgiutils_h.INFO_ICON_ALT ;
			        }
			
			String[] split = input.split( "[\\]]" , 2 );
			
            temp_buffer= split[0];
			t=(temp_buffer==null)?0L:strtoul(temp_buffer.substring(1),null,10);

            time_ptr = Calendar.getInstance();
			time_ptr.setTimeInMillis(t*1000);
			current_message_date = String.format( "%tB %<td, %<tY %<tH:00\n",time_ptr);

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
				last_message_date = current_message_date ;
				}

            date_time = cgiutils.get_time_string( t, common_h.SHORT_DATE_TIME);
			date_time = date_time.trim();

            if ( split.length == 1 )
                temp_buffer = null;
            else 
                temp_buffer=split[1];
			
			if(display_frills==common_h.TRUE)
				System.out.printf("<img align='left' src='%s%s' alt='%s' title='%s'>",cgiutils.url_images_path,image,image_alt,image_alt);
			System.out.printf("[%s] %s<br clear='all'>\n",date_time,(temp_buffer==null)?"":temp_buffer);
		        }

		System.out.printf("</DIV></P>\n");
		System.out.printf("<HR>\n");

		if(use_lifo==common_h.FALSE)
			cgiutils.mmap_fclose(thefile);
	        }

	if(use_lifo==common_h.TRUE)
		cgiutils.free_lifo_memory();

	return common_h.OK;
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

}
