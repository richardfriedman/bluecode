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

import java.util.ArrayList;

import org.blue.star.common.objects;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class config extends blue_servlet {
    
public static final int DISPLAY_NONE                      = 0;
public static final int DISPLAY_HOSTS                     = 1;
public static final int DISPLAY_HOSTGROUPS                = 2;
public static final int DISPLAY_CONTACTS                  = 3;
public static final int DISPLAY_CONTACTGROUPS             = 4;
public static final int DISPLAY_SERVICES                  = 5;
public static final int DISPLAY_TIMEPERIODS               = 6;
public static final int DISPLAY_COMMANDS                  = 7;
public static final int DISPLAY_HOSTGROUPESCALATIONS      = 8    /* no longer implemented */;
public static final int DISPLAY_SERVICEDEPENDENCIES       = 9;
public static final int DISPLAY_SERVICEESCALATIONS        = 10;
public static final int DISPLAY_HOSTDEPENDENCIES          = 11;
public static final int DISPLAY_HOSTESCALATIONS           = 12;
public static final int DISPLAY_HOSTEXTINFO               = 13;
public static final int DISPLAY_SERVICEEXTINFO            = 14;
public static final int DISPLAY_SERVICEGROUPS             = 15;

public static cgiauth_h.authdata current_authdata;

public static int display_type=DISPLAY_NONE;

public static int embedded=common_h.FALSE;

public void reset_context() {
   current_authdata = new cgiauth_h.authdata ();
   display_type=DISPLAY_NONE;
   embedded=common_h.FALSE;
}

public void call_main() {
   main( null );
}
public static void main(String[] args){
	int result=common_h.OK;

	/* get the arguments passed in the URL */
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


	/* begin top table */
	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	/* left column of the first row */
	System.out.printf("<td align=left valign=top width=50%%>\n");
	cgiutils.display_info_table("Configuration",common_h.FALSE,current_authdata);
	System.out.printf("</td>\n");

	/* right hand column of top row */
	System.out.printf("<td align=right valign=bottom width=50%%>\n");

	if(display_type!=DISPLAY_NONE){

		System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.CONFIG_CGI);
		System.out.printf("<table border=0>\n");

		System.out.printf("<tr><td align=left class='reportSelectSubTitle'>Object Type:</td></tr>\n");
		System.out.printf("<tr><td align=left class='reportSelectItem'>");
		System.out.printf("<select name='type'>\n");
		System.out.printf("<option value='hosts' %s>Hosts\n",(display_type==DISPLAY_HOSTS)?"SELECTED":"");
		System.out.printf("<option value='hostdependencies' %s>Host Dependencies\n",(display_type==DISPLAY_HOSTDEPENDENCIES)?"SELECTED":"");
		System.out.printf("<option value='hostescalations' %s>Host Escalations\n",(display_type==DISPLAY_HOSTESCALATIONS)?"SELECTED":"");
		System.out.printf("<option value='hostgroups' %s>Host Groups\n",(display_type==DISPLAY_HOSTGROUPS)?"SELECTED":"");
		System.out.printf("<option value='services' %s>Services\n",(display_type==DISPLAY_SERVICES)?"SELECTED":"");
		System.out.printf("<option value='servicegroups' %s>Service Groups\n",(display_type==DISPLAY_SERVICEGROUPS)?"SELECTED":"");
		System.out.printf("<option value='servicedependencies' %s>Service Dependencies\n",(display_type==DISPLAY_SERVICEDEPENDENCIES)?"SELECTED":"");
		System.out.printf("<option value='serviceescalations' %s>Service Escalations\n",(display_type==DISPLAY_SERVICEESCALATIONS)?"SELECTED":"");
		System.out.printf("<option value='contacts' %s>Contacts\n",(display_type==DISPLAY_CONTACTS)?"SELECTED":"");
		System.out.printf("<option value='contactgroups' %s>Contact Groups\n",(display_type==DISPLAY_CONTACTGROUPS)?"SELECTED":"");
		System.out.printf("<option value='timeperiods' %s>Timeperiods\n",(display_type==DISPLAY_TIMEPERIODS)?"SELECTED":"");
		System.out.printf("<option value='commands' %s>Commands\n",(display_type==DISPLAY_COMMANDS)?"SELECTED":"");
		System.out.printf("<option value='hostextinfo' %s>Extended Host Information\n",(display_type==DISPLAY_HOSTEXTINFO)?"SELECTED":"");
		System.out.printf("<option value='serviceextinfo' %s>Extended Service Information\n",(display_type==DISPLAY_SERVICEEXTINFO)?"SELECTED":"");
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectItem'><input type='submit' value='Update'></td></tr>\n");
		System.out.printf("</table>\n");
		System.out.printf("</form>\n");
	        }

	/* display context-sensitive help */
	switch(display_type){
	case DISPLAY_HOSTS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_HOSTS);
		break;
	case DISPLAY_HOSTGROUPS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_HOSTGROUPS);
		break;
	case DISPLAY_SERVICEGROUPS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_SERVICEGROUPS);
		break;
	case DISPLAY_CONTACTS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_CONTACTS);
		break;
	case DISPLAY_CONTACTGROUPS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_CONTACTGROUPS);
		break;
	case DISPLAY_SERVICES:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_SERVICES);
		break;
	case DISPLAY_TIMEPERIODS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_TIMEPERIODS);
		break;
	case DISPLAY_COMMANDS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_COMMANDS);
		break;
	case DISPLAY_SERVICEDEPENDENCIES:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_SERVICEDEPENDENCIES);
		break;
	case DISPLAY_SERVICEESCALATIONS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_HOSTESCALATIONS);
		break;
	case DISPLAY_HOSTDEPENDENCIES:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_HOSTDEPENDENCIES);
		break;
	case DISPLAY_HOSTESCALATIONS:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_HOSTESCALATIONS);
		break;
	case DISPLAY_HOSTEXTINFO:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_HOSTEXTINFO);
		break;
	case DISPLAY_SERVICEEXTINFO:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_SERVICEEXTINFO);
		break;
	default:
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_CONFIG_MENU);
		break;
	        }

	System.out.printf("</td>\n");

	/* end of top table */
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");


	switch(display_type){
	case DISPLAY_HOSTS:
		display_hosts();
		break;
	case DISPLAY_HOSTGROUPS:
		display_hostgroups();
		break;
	case DISPLAY_SERVICEGROUPS:
		display_servicegroups();
		break;
	case DISPLAY_CONTACTS:
		display_contacts();
		break;
	case DISPLAY_CONTACTGROUPS:
		display_contactgroups();
		break;
	case DISPLAY_SERVICES:
		display_services();
		break;
	case DISPLAY_TIMEPERIODS:
		display_timeperiods();
		break;
	case DISPLAY_COMMANDS:
		display_commands();
		break;
	case DISPLAY_SERVICEDEPENDENCIES:
		display_servicedependencies();
		break;
	case DISPLAY_SERVICEESCALATIONS:
		display_serviceescalations();
		break;
	case DISPLAY_HOSTDEPENDENCIES:
		display_hostdependencies();
		break;
	case DISPLAY_HOSTESCALATIONS:
		display_hostescalations();
		break;
	case DISPLAY_HOSTEXTINFO:
		display_hostextinfo();
		break;
	case DISPLAY_SERVICEEXTINFO:
		display_serviceextinfo();
		break;
	default:
		display_options();
		break;
	        }

	document_footer();

	cgiutils.exit(  common_h.OK );
        }




public static void document_header(int use_stylesheet){
	String date_time ; // MAX_DATETIME_LENGTH

	if(embedded==common_h.TRUE)
		return;

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );
       response.setContentType("text/html");
    } else {

       date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      	System.out.printf("Expires: %s\r\n",date_time);
      	System.out.printf("Content-type: text/html\r\n\r\n");
    }
    
	System.out.printf("<html>\n");
	System.out.printf("<head>\n");
	System.out.printf("<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>\n");
	System.out.printf("<title>\n");
	System.out.printf("Configuration\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.CONFIG_CSS);
	        }

	System.out.printf("</head>\n");

	System.out.printf("<body CLASS='config'>\n");

	/* include user SSI header */
    cgiutils.include_ssi_files(cgiutils_h.CONFIG_CGI,cgiutils_h.SSI_HEADER);

	return;
        }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.CONFIG_CGI,cgiutils_h.SSI_FOOTER);

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

		/* we found the configuration type argument */
		else if(variables[x].equals("type")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			/* what information should we display? */
			if(variables[x].equals("hosts"))
				display_type=DISPLAY_HOSTS;
			else if(variables[x].equals("hostgroups"))
				display_type=DISPLAY_HOSTGROUPS;
			else if(variables[x].equals("servicegroups"))
				display_type=DISPLAY_SERVICEGROUPS;
			else if(variables[x].equals("contacts"))
				display_type=DISPLAY_CONTACTS;
			else if(variables[x].equals("contactgroups"))
				display_type=DISPLAY_CONTACTGROUPS;
			else if(variables[x].equals("services"))
				display_type=DISPLAY_SERVICES;
			else if(variables[x].equals("timeperiods"))
				display_type=DISPLAY_TIMEPERIODS;
			else if(variables[x].equals("commands"))
				display_type=DISPLAY_COMMANDS;
			else if(variables[x].equals("servicedependencies"))
				display_type=DISPLAY_SERVICEDEPENDENCIES;
			else if(variables[x].equals("serviceescalations"))
				display_type=DISPLAY_SERVICEESCALATIONS;
			else if(variables[x].equals("hostdependencies"))
				display_type=DISPLAY_HOSTDEPENDENCIES;
			else if(variables[x].equals("hostescalations"))
				display_type=DISPLAY_HOSTESCALATIONS;
			else if(variables[x].equals("hostextinfo"))
				display_type=DISPLAY_HOSTEXTINFO;
			else if(variables[x].equals("serviceextinfo"))
				display_type=DISPLAY_SERVICEEXTINFO;

			/* we found the embed option */
			else if(variables[x].equals("embedded"))
				embedded=common_h.TRUE;
		        }

		/* we received an invalid argument */
		else
			error=common_h.TRUE;
	
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



public static void display_hosts(){
    int first = 0;
	int options=0;
	int odd=0;
	String time_string ; // 16
	String bg_class="";

	/* see if user is authorized to view host information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Hosts</DIV></P>\n");

	System.out.printf("<P><DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host Name</TH>");
	System.out.printf("<TH CLASS='data'>Alias/Description</TH>");
	System.out.printf("<TH CLASS='data'>Address</TH>");
	System.out.printf("<TH CLASS='data'>Parent Hosts</TH>");
	System.out.printf("<TH CLASS='data'>Max. Check Attempts</TH>");
	System.out.printf("<TH CLASS='data'>Check Interval</TH>\n");
	System.out.printf("<TH CLASS='data'>Host Check Command</TH>");
	System.out.printf("<TH CLASS='data'>Obsess Over</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Active Checks</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Passive Checks</TH>\n");
	System.out.printf("<TH CLASS='data'>Check Freshness</TH>\n");
	System.out.printf("<TH CLASS='data'>Freshness Threshold</TH>\n");
	System.out.printf("<TH CLASS='data'>Default Contact Groups</TH>\n");
	System.out.printf("<TH CLASS='data'>Notification Interval</TH>");
	System.out.printf("<TH CLASS='data'>Notification Options</TH>");
	System.out.printf("<TH CLASS='data'>Notification Period</TH>");
	System.out.printf("<TH CLASS='data'>Event Handler</TH>");
	System.out.printf("<TH CLASS='data'>Enable Event Handler</TH>");
	System.out.printf("<TH CLASS='data'>Stalking Options</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Flap Detection</TH>");
	System.out.printf("<TH CLASS='data'>Low Flap Threshold</TH>");
	System.out.printf("<TH CLASS='data'>High Flap Threshold</TH>");
	System.out.printf("<TH CLASS='data'>Process Performance Data</TH>");
	System.out.printf("<TH CLASS='data'>Enable Failure Prediction</TH>");
	System.out.printf("<TH CLASS='data'>Failure Prediction Options</TH>");
	System.out.printf("<TH CLASS='data'>Retention Options</TH>");
	System.out.printf("</TR>\n");

	/* check all the hosts... */
	for(objects_h.host  temp_host : objects.host_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><a name='%s'>%s</a></TD>\n",bg_class,cgiutils.url_encode(temp_host.name),temp_host.name);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_host.alias);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_host.address);

		System.out.printf("<TD CLASS='%s'>",bg_class);
        first = 0;
        if(temp_host.parent_hosts==null || temp_host.parent_hosts.size() == 0 )
            System.out.printf("&nbsp;");
        else 
           for(objects_h.hostsmember  temp_hostsmember : (ArrayList<objects_h.hostsmember>) temp_host.parent_hosts ){
              
              if( first++ > 0  ) 
                 System.out.printf(", ");
              
              System.out.printf("<a href='%s?type=hosts#%s'>%s</a>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_hostsmember.host_name),temp_hostsmember.host_name);
           }
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%d</TD>\n",bg_class,temp_host.max_attempts);

        time_string = cgiutils.get_interval_time_string(temp_host.check_interval);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,time_string);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_host.host_check_command==null)
			System.out.printf("&nbsp;");
		else
		   System.out.printf("<a href='%s?type=commands#%s'>%s</a></TD>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_host.host_check_command),temp_host.host_check_command);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_host.obsess_over_host==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_host.checks_enabled==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_host.accept_passive_host_checks==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_host.check_freshness==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_host.freshness_threshold==0)
			System.out.printf("Auto-determined value\n");
		else
			System.out.printf("%d seconds\n",temp_host.freshness_threshold);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);

		/* find all the contact groups for this host... */
        first = 0;
		for( objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>) temp_host.contact_groups ){

			if( first++ > 0  ) 
				System.out.printf(", ");

			System.out.printf("<A HREF='%s?type=contactgroups#%s'>%s</A>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contactgroupsmember.group_name),temp_contactgroupsmember.group_name);
		        }
		System.out.printf("</TD>\n");

        time_string = cgiutils.get_interval_time_string(temp_host.notification_interval);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_host.notification_interval==0)?"<i>No Re-notification</I>":time_string);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_host.notify_on_down==common_h.TRUE){
			options=1;
			System.out.printf("Down");
		        }
		if(temp_host.notify_on_unreachable==common_h.TRUE){
			System.out.printf("%sUnreachable",(options!=0)?", ":"");
			options=1;
		        }
		if(temp_host.notify_on_recovery==common_h.TRUE){
			System.out.printf("%sRecovery",(options!=0)?", ":"");
			options=1;
		        }
		if(temp_host.notify_on_flapping==common_h.TRUE){
			System.out.printf("%sFlapping",(options!=0)?", ":"");
			options=1;
		        }
		if(options==0)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_host.notification_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<a href='%s?type=timeperiods#%s'>%s</a>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_host.notification_period),temp_host.notification_period);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_host.event_handler==null)
			System.out.printf("&nbsp");
		else
			System.out.printf("<a href='%s?type=commands#%s'>%s</a></TD>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_host.event_handler),temp_host.event_handler);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_host.event_handler_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_host.stalk_on_up==common_h.TRUE){
			options=1;
			System.out.printf("Up");
		        }
		if(temp_host.stalk_on_down==common_h.TRUE){
			System.out.printf("%sDown",(options!=0)?", ":"");
			options=1;
		        }
		if(temp_host.stalk_on_unreachable==common_h.TRUE){
			System.out.printf("%sUnreachable",(options!=0)?", ":"");
			options=1;
		        }
		if(options==0)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_host.flap_detection_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_host.low_flap_threshold==0.0)
			System.out.printf("Program-wide value\n");
		else
			System.out.printf("%3.1f%%\n",temp_host.low_flap_threshold);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_host.high_flap_threshold==0.0)
			System.out.printf("Program-wide value\n");
		else
			System.out.printf("%3.1f%%\n",temp_host.high_flap_threshold);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_host.process_performance_data==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_host.failure_prediction_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_host.failure_prediction_options==null)?"&nbsp;":temp_host.failure_prediction_options);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_host.retain_status_information==common_h.TRUE){
			options=1;
			System.out.printf("Status Information");
		        }
		if(temp_host.retain_nonstatus_information==common_h.TRUE){
			System.out.printf("%sNon-Status Information",(options==1)?", ":"");
			options=1;
		        }
		if(options==0)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_hostgroups(){
    int first = 0;
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view hostgroup information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Host Groups</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Group Name</TH>");
	System.out.printf("<TH CLASS='data'>Description</TH>");
	System.out.printf("<TH CLASS='data'>Host Members</TH>");
	System.out.printf("</TR>\n");

	/* check all the hostgroups... */
	for(objects_h.hostgroup temp_hostgroup : objects.hostgroup_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,temp_hostgroup.group_name);

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_hostgroup.alias);

		System.out.printf("<TD CLASS='%s'>",bg_class);

		/* find all the hosts that are members of this hostgroup... */
        first = 0;
		for( objects_h.hostgroupmember temp_hostgroupmember : (ArrayList<objects_h.hostgroupmember>) temp_hostgroup.members ){

			if( first++ > 0 )  
				System.out.printf(", ");
			System.out.printf("<A HREF='%s?type=hosts#%s'>%s</A>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_hostgroupmember.host_name),temp_hostgroupmember.host_name);
		        }
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_servicegroups(){
    int first = 0;
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view servicegroup information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Service Groups</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Group Name</TH>");
	System.out.printf("<TH CLASS='data'>Description</TH>");
	System.out.printf("<TH CLASS='data'>Service Members</TH>");
	System.out.printf("</TR>\n");

	/* check all the servicegroups... */
	for(objects_h.servicegroup temp_servicegroup : objects.servicegroup_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,temp_servicegroup.group_name);

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_servicegroup.alias);

		System.out.printf("<TD CLASS='%s'>",bg_class);

		/* find all the services that are members of this servicegroup... */
        first = 0;
		for(objects_h.servicegroupmember temp_servicegroupmember : (ArrayList<objects_h.servicegroupmember>) temp_servicegroup.members ){

			System.out.printf("%s<A HREF='%s?type=hosts#%s'>%s</A> / ",(first++> 0)?"":", ",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_servicegroupmember.host_name),temp_servicegroupmember.host_name);
            
			System.out.printf("<A HREF='%s?type=services#%s;",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_servicegroupmember.host_name));
			System.out.printf("%s'>%s</A>\n",cgiutils.url_encode(temp_servicegroupmember.service_description),temp_servicegroupmember.service_description);
		        }

		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_contacts(){
    int first = 0;
	int odd=0;
	int options;
	int found;
	String bg_class="";

	/* see if user is authorized to view contact information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in contact definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_CONTACTS);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Contacts</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE CLASS='data'>\n");
    
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Contact Name</TH>");
	System.out.printf("<TH CLASS='data'>Alias</TH>");
	System.out.printf("<TH CLASS='data'>Email Address</TH>");
	System.out.printf("<TH CLASS='data'>Pager Address/Number</TH>");
	System.out.printf("<TH CLASS='data'>Service Notification Options</TH>");
	System.out.printf("<TH CLASS='data'>Host Notification Options</TH>");
	System.out.printf("<TH CLASS='data'>Service Notification Period</TH>");
	System.out.printf("<TH CLASS='data'>Host Notification Period</TH>");
	System.out.printf("<TH CLASS='data'>Service Notification Commands</TH>");
	System.out.printf("<TH CLASS='data'>Host Notification Commands</TH>");
	System.out.printf("</TR>\n");
	
	/* check all contacts... */
	for(objects_h.contact  temp_contact : objects.contact_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A NAME='%s'>%s</a></TD>\n",bg_class,cgiutils.url_encode(temp_contact.name),temp_contact.name);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_contact.alias);
		System.out.printf("<TD CLASS='%s'><A HREF='mailto:%s'>%s</A></TD>\n",bg_class,(temp_contact.email==null)?"&nbsp;":temp_contact.email,(temp_contact.email==null)?"&nbsp;":temp_contact.email);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_contact.pager==null)?"&nbsp;":temp_contact.pager);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_contact.notify_on_service_unknown == common_h.TRUE){
			options=1;
			System.out.printf("Unknown");
		        }
		if(temp_contact.notify_on_service_warning == common_h.TRUE){
			System.out.printf("%sWarning",(options != 0)?", ":"");
			options=1;
		        }
		if(temp_contact.notify_on_service_critical == common_h.TRUE){
			System.out.printf("%sCritical",(options != 0)?", ":"");
			options=1;
		        }
		if(temp_contact.notify_on_service_recovery == common_h.TRUE){
			System.out.printf("%sRecovery",(options != 9)?", ":"");
			options=1;
		        }
		if(temp_contact.notify_on_service_flapping == common_h.TRUE){
			System.out.printf("%sFlapping",(options != 0)?", ":"");
			options=1;
		        }
		if(0==options)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_contact.notify_on_host_down == common_h.TRUE){
			options=1;
			System.out.printf("Down");
		        }
		if(temp_contact.notify_on_host_unreachable == common_h.TRUE){
			System.out.printf("%sUnreachable",(options != 0)?", ":"");
			options=1;
		        }
		if(temp_contact.notify_on_host_recovery == common_h.TRUE){
			System.out.printf("%sRecovery",(options != 0)?", ":"");
			options=1;
		        }
		if(temp_contact.notify_on_host_flapping == common_h.TRUE){
			System.out.printf("%sFlapping",(options != 0)?", ":"");
			options=1;
		        }
		if(0 == options)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>\n",bg_class);
		if(temp_contact.service_notification_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=timeperiods#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contact.service_notification_period),temp_contact.service_notification_period);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>\n",bg_class);
		if(temp_contact.host_notification_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=timeperiods#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contact.host_notification_period),temp_contact.host_notification_period);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		found=common_h.FALSE;
        first = 0;
		for( objects_h.commandsmember temp_commandsmember : (ArrayList<objects_h.commandsmember>) temp_contact.service_notification_commands ){

			if( first++ > 0  ) 
				System.out.printf(", ");

			System.out.printf("<A HREF='%s?type=commands#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_commandsmember.command),temp_commandsmember.command);

			found=common_h.TRUE;
		        }
		if(found==common_h.FALSE)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		found=common_h.FALSE;
        first = 0;
		for(objects_h.commandsmember  temp_commandsmember : (ArrayList<objects_h.commandsmember>) temp_contact.host_notification_commands ){

			if( first++ > 0 ) 
				System.out.printf(", ");

			System.out.printf("<A HREF='%s?type=commands#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_commandsmember.command),temp_commandsmember.command);

			found=common_h.TRUE;
		        }
		if(found==common_h.FALSE)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_contactgroups(){
    int first = 0;
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view contactgroup information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }


	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Contact Groups</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CELLSPACING=3 CELLPADDING=0>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Group Name</TH>\n");
	System.out.printf("<TH CLASS='data'>Description</TH>\n");
	System.out.printf("<TH CLASS='data'>Contact Members</TH>\n");
	System.out.printf("</TR>\n");

	/* check all the contact groups... */
	for(objects_h.contactgroup temp_contactgroup : objects.contactgroup_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A NAME='%s'></A>%s</TD>\n",bg_class,cgiutils.url_encode(temp_contactgroup.group_name),temp_contactgroup.group_name);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_contactgroup.alias);

		/* find all the contact who are members of this contact group... */
		System.out.printf("<TD CLASS='%s'>",bg_class);
		for(objects_h.contactgroupmember temp_contactgroupmember : (ArrayList<objects_h.contactgroupmember>) temp_contactgroup.members ){
			
			if( first++ > 0 ) 
				System.out.printf(", ");

			System.out.printf("<A HREF='%s?type=contacts#%s'>%s</A>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contactgroupmember.contact_name),temp_contactgroupmember.contact_name);
		        }
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_services(){
    int first = 0;
	String command_line ; // MAX_INPUT_BUFFER
	String command_name="";
	int options;
	int odd=0;
	String time_string ; // 16
	String bg_class;


	/* see if user is authorized to view service information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in service definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_SERVICES);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Services</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data' COLSPAN=2>Service</TH>");
	System.out.printf("</TR>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host</TH>\n");
	System.out.printf("<TH CLASS='data'>Description</TH>\n");
	System.out.printf("<TH CLASS='data'>Max. Check Attempts</TH>\n");
	System.out.printf("<TH CLASS='data'>Normal Check Interval</TH>\n");
	System.out.printf("<TH CLASS='data'>Retry Check Interal</TH>\n");
	System.out.printf("<TH CLASS='data'>Check Command</TH>\n");
	System.out.printf("<TH CLASS='data'>Check Period</TH>\n");
	System.out.printf("<TH CLASS='data'>Parallelize</TH>\n");
	System.out.printf("<TH CLASS='data'>Volatile</TH>\n");
	System.out.printf("<TH CLASS='data'>Obsess Over</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Active Checks</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Passive Checks</TH>\n");
	System.out.printf("<TH CLASS='data'>Check Freshness</TH>\n");
	System.out.printf("<TH CLASS='data'>Freshness Threshold</TH>\n");
	System.out.printf("<TH CLASS='data'>Default Contact Groups</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Notifications</TH>\n");
	System.out.printf("<TH CLASS='data'>Notification Interval</TH>\n");
	System.out.printf("<TH CLASS='data'>Notification Options</TH>\n");
	System.out.printf("<TH CLASS='data'>Notification Period</TH>\n");
	System.out.printf("<TH CLASS='data'>Event Handler</TH>");
	System.out.printf("<TH CLASS='data'>Enable Event Handler</TH>");
	System.out.printf("<TH CLASS='data'>Stalking Options</TH>\n");
	System.out.printf("<TH CLASS='data'>Enable Flap Detection</TH>");
	System.out.printf("<TH CLASS='data'>Low Flap Threshold</TH>");
	System.out.printf("<TH CLASS='data'>High Flap Threshold</TH>");
	System.out.printf("<TH CLASS='data'>Process Performance Data</TH>");
	System.out.printf("<TH CLASS='data'>Enable Failure Prediction</TH>");
	System.out.printf("<TH CLASS='data'>Failure Prediction Options</TH>");
	System.out.printf("<TH CLASS='data'>Retention Options</TH>");
	System.out.printf("</TR>\n");

	/* check all the services... */
	for(objects_h.service temp_service : objects.service_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
	                }
		else{
			odd=1;
			bg_class="dataEven";
	                }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A NAME='%s;",bg_class,cgiutils.url_encode(temp_service.host_name));
		System.out.printf("%s'></A>",cgiutils.url_encode(temp_service.description));
		System.out.printf("<A HREF='%s?type=hosts#%s'>%s</A></TD>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_service.host_name),temp_service.host_name);
		
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_service.description);
		
		System.out.printf("<TD CLASS='%s'>%d</TD>\n",bg_class,temp_service.max_attempts);

        time_string = cgiutils.get_interval_time_string(temp_service.check_interval );
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,time_string);
        time_string = cgiutils.get_interval_time_string(temp_service.retry_interval);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,time_string);

		command_line = temp_service.service_check_command;
		command_name=command_line.split("!")[0];

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=commands#%s'>%s</A></TD>\n",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(command_name),temp_service.service_check_command);
		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_service.check_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=timeperiods#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_service.check_period),temp_service.check_period);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.parallelize==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.is_volatile==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.obsess_over_service==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.checks_enabled==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.accept_passive_service_checks==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.check_freshness==common_h.TRUE)?"Yes":"No");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_service.freshness_threshold==0)
			System.out.printf("Auto-determined value\n");
		else
			System.out.printf("%d seconds\n",temp_service.freshness_threshold);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
        first = 0;
        if(temp_service.contact_groups==null)
            System.out.printf("&nbsp;");
        else 
           for(objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>) temp_service.contact_groups ){
              
              if( first++ > 0 ) 
                 System.out.printf(", ");
              
              System.out.printf("<A HREF='%s?type=contactgroups#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contactgroupsmember.group_name),temp_contactgroupsmember.group_name);
           }
        System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_service.notifications_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

        time_string = cgiutils.get_interval_time_string(temp_service.notification_interval );
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.notification_interval==0)?"<i>No Re-notification</i>":time_string);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_service.notify_on_unknown==common_h.TRUE){
			options=1;
			System.out.printf("Unknown");
	                }
		if(temp_service.notify_on_warning==common_h.TRUE){
			System.out.printf("%sWarning",(options != 0)?", ":"");
			options=1;
	                }
		if(temp_service.notify_on_critical==common_h.TRUE){
			System.out.printf("%sCritical",(options != 0)?", ":"");
			options=1;
	                }
		if(temp_service.notify_on_recovery==common_h.TRUE){
			System.out.printf("%sRecovery",(options != 0)?", ":"");
			options=1;
	                }
		if(temp_service.notify_on_flapping==common_h.TRUE){
			System.out.printf("%sFlapping",(options != 0)?", ":"");
			options=1;
	                }
		if(0==options)
			System.out.printf("None");
		System.out.printf("</TD>\n");
		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_service.notification_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=timeperiods#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_service.notification_period),temp_service.notification_period);
		System.out.printf("</TD>\n");
		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_service.event_handler==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=commands#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_service.event_handler),temp_service.event_handler);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_service.event_handler_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_service.stalk_on_ok==common_h.TRUE){
			options=1;
			System.out.printf("Ok");
	                }
		if(temp_service.stalk_on_warning==common_h.TRUE){
			System.out.printf("%sWarning",(options != 0)?", ":"");
			options=1;
	                }
		if(temp_service.stalk_on_unknown==common_h.TRUE){
			System.out.printf("%sUnknown",(options != 0)?", ":"");
			options=1;
	                }
		if(temp_service.stalk_on_critical==common_h.TRUE){
			System.out.printf("%sCritical",(options != 0)?", ":"");
			options=1;
	                }
		if(options==0)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_service.flap_detection_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_service.low_flap_threshold==0.0)
			System.out.printf("Program-wide value\n");
		else
			System.out.printf("%3.1f%%\n",temp_service.low_flap_threshold);
		System.out.printf("</TD>\n");
			
		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_service.high_flap_threshold==0.0)
			System.out.printf("Program-wide value\n");
		else
			System.out.printf("%3.1f%%\n",temp_service.high_flap_threshold);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_service.process_performance_data==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("%s\n",(temp_service.failure_prediction_enabled==common_h.TRUE)?"Yes":"No");
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,(temp_service.failure_prediction_options==null)?"&nbsp;":temp_service.failure_prediction_options);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=0;
		if(temp_service.retain_status_information==common_h.TRUE){
			options=1;
			System.out.printf("Status Information");
	                }
		if(temp_service.retain_nonstatus_information==common_h.TRUE){
			System.out.printf("%sNon-Status Information",(options==1)?", ":"");
			options=1;
	                }
		if(options==0)
			System.out.printf("None");
		System.out.printf("</TD>\n");
		
		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_timeperiods(){
    int first = 0;
	int odd=0;
	int day=0;
	String bg_class="";
	String timestring ; // 10
	int hours=0;
	int minutes=0;
	int seconds=0;

	/* see if user is authorized to view time period information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in time period definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_TIMEPERIODS);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Time Periods</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Name</TH>\n");
	System.out.printf("<TH CLASS='data'>Alias/Description</TH>\n");
	System.out.printf("<TH CLASS='data'>Sunday Time Ranges</TH>\n");
	System.out.printf("<TH CLASS='data'>Monday Time Ranges</TH>\n");
	System.out.printf("<TH CLASS='data'>Tuesday Time Ranges</TH>\n");
	System.out.printf("<TH CLASS='data'>Wednesday Time Ranges</TH>\n");
	System.out.printf("<TH CLASS='data'>Thursday Time Ranges</TH>\n");
	System.out.printf("<TH CLASS='data'>Friday Time Ranges</TH>\n");
	System.out.printf("<TH CLASS='data'>Saturday Time Ranges</TH>\n");
	System.out.printf("</TR>\n");
	
	/* check all the time periods... */
	for(objects_h.timeperiod temp_timeperiod : objects.timeperiod_list ){
	   
	   if(odd!=0){
	      odd=0;
	      bg_class="dataOdd";
	   }
	   else{
	      odd=1;
	      bg_class="dataEven";
	   }
	   
	   System.out.printf("<TR CLASS='%s'>\n",bg_class);
	   
	   System.out.printf("<TD CLASS='%s'><A NAME='%s'>%s</A></TD>\n",bg_class,cgiutils.url_encode(temp_timeperiod.name),temp_timeperiod.name);
	   System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_timeperiod.alias);
	   
	   for(day=0;day<7;day++){
	      
	      System.out.printf("<TD CLASS='%s'>",bg_class);
	      
	      first=0;
	      if(temp_timeperiod.days[day]==null)
	         System.out.printf("&nbsp;");
	      else 
	         for(objects_h.timerange temp_timerange : temp_timeperiod.days[day] ){
	            
	            if( first++ > 0 ) 
	               System.out.printf(", ");
	            
	            hours=(int) temp_timerange.range_start/3600;
	            minutes=(int) (temp_timerange.range_start-(hours*3600))/60;
	            seconds=(int) temp_timerange.range_start-(hours*3600)-(minutes*60);
	            timestring = String.format( "%02d:%02d:%02d",hours,minutes,seconds);
	            System.out.printf("%s - ",timestring);
	            
	            hours=(int) temp_timerange.range_end/3600;
	            minutes=(int) (temp_timerange.range_end-(hours*3600))/60;
	            seconds=(int) temp_timerange.range_end-(hours*3600)-(minutes*60);
	            timestring = String.format( "%02d:%02d:%02d",hours,minutes,seconds);
	            System.out.printf("%s",timestring);
	         }

			System.out.printf("</TD>\n");
		        }

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_commands(){
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view command information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_COMMANDS);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Commands</DIV></P>\n");

	System.out.printf("<P><DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR><TH CLASS='data'>Command Name</TH><TH CLASS='data'>Command Line</TH></TR>\n");

	/* check all commands */
	for(objects_h.command temp_command : objects.command_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataEven";
		        }
		else{
			odd=1;
			bg_class="dataOdd";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A NAME='%s'></A>%s</TD>\n",bg_class,cgiutils.url_encode(temp_command.name),temp_command.name);
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,temp_command.command_line);

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV></P>\n");

	return;
        }


public static void display_servicedependencies(){

	int odd=0;
	int options;
	String bg_class="";

	/* see if user is authorized to view hostgroup information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_SERVICEDEPENDENCIES);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Service Dependencies</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data' COLSPAN=2>Dependent Service</TH>");
	System.out.printf("<TH CLASS='data' COLSPAN=2>Master Service</TH>");
	System.out.printf("</TR>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host</TH>");
	System.out.printf("<TH CLASS='data'>Service</TH>");
	System.out.printf("<TH CLASS='data'>Host</TH>");
	System.out.printf("<TH CLASS='data'>Service</TH>");
	System.out.printf("<TH CLASS='data'>Dependency Type</TH>");
	System.out.printf("<TH CLASS='data'>Dependency Failure Options</TH>");
	System.out.printf("</TR>\n");

	/* check all the service dependencies... */
	for(objects_h.servicedependency temp_sd : objects.servicedependency_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_sd.dependent_host_name),temp_sd.dependent_host_name);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=services#%s;",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_sd.dependent_host_name));
		System.out.printf("%s'>%s</A></TD>\n",cgiutils.url_encode(temp_sd.dependent_service_description),temp_sd.dependent_service_description);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_sd.host_name),temp_sd.host_name);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=services#%s;",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_sd.host_name));
		System.out.printf("%s'>%s</A></TD>\n",cgiutils.url_encode(temp_sd.service_description),temp_sd.service_description);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,(temp_sd.dependency_type==common_h.NOTIFICATION_DEPENDENCY)?"Notification":"Check Execution");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=common_h.FALSE;
		if(temp_sd.fail_on_ok==common_h.TRUE){
			System.out.printf("Ok");
			options=common_h.TRUE;
		        }
		if(temp_sd.fail_on_warning==common_h.TRUE){
			System.out.printf("%sWarning",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_sd.fail_on_unknown==common_h.TRUE){
			System.out.printf("%sUnknown",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_sd.fail_on_critical==common_h.TRUE){
			System.out.printf("%sCritical",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_sd.fail_on_pending==common_h.TRUE){
			System.out.printf("%sPending",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_serviceescalations(){
    int first = 0;
	int options=common_h.FALSE;
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view hostgroup information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_SERVICEESCALATIONS);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Service Escalations</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data' COLSPAN=2>Service</TH>");
	System.out.printf("</TR>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host</TH>");
	System.out.printf("<TH CLASS='data'>Description</TH>");
	System.out.printf("<TH CLASS='data'>Contact Groups</TH>");
	System.out.printf("<TH CLASS='data'>First Notification</TH>");
	System.out.printf("<TH CLASS='data'>Last Notification</TH>");
	System.out.printf("<TH CLASS='data'>Notification Interval</TH>");
	System.out.printf("<TH CLASS='data'>Escalation Period</TH>");
	System.out.printf("<TH CLASS='data'>Escalation Options</TH>");
	System.out.printf("</TR>\n");

	/* check all the service escalations... */
	for(objects_h.serviceescalation temp_se : objects.serviceescalation_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_se.host_name),temp_se.host_name);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=services#%s;",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_se.host_name));
		System.out.printf("%s'>%s</A></TD>\n",cgiutils.url_encode(temp_se.description),temp_se.description);

		System.out.printf("<TD CLASS='%s'>",bg_class);
        first = 0;        
		for( objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>) temp_se.contact_groups ){

			if( first++ > 0 )
				System.out.printf(", ");

			System.out.printf("<A HREF='%s?type=contactgroups#%s'>%s</A>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contactgroupsmember.group_name),temp_contactgroupsmember.group_name);
		        }
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%d</TD>",bg_class,temp_se.first_notification);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_se.last_notification==0)
			System.out.printf("Infinity");
		else
			System.out.printf("%d",temp_se.last_notification);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_se.notification_interval==0)
			System.out.printf("Notify Only Once (No Re-notification)");
		else
			System.out.printf("%d",temp_se.notification_interval);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_se.escalation_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=timeperiods#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_se.escalation_period),temp_se.escalation_period);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=common_h.FALSE;
		if(temp_se.escalate_on_warning==common_h.TRUE){
			System.out.printf("%sWarning",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_se.escalate_on_unknown==common_h.TRUE){
			System.out.printf("%sUnknown",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_se.escalate_on_critical==common_h.TRUE){
			System.out.printf("%sCritical",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_se.escalate_on_recovery==common_h.TRUE){
			System.out.printf("%sRecovery",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(options==common_h.FALSE)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_hostdependencies(){

	int odd=0;
	int options;
	String bg_class="";

	/* see if user is authorized to view hostdependency information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_HOSTDEPENDENCIES);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Host Dependencies</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Dependent Host</TH>");
	System.out.printf("<TH CLASS='data'>Master Host</TH>");
	System.out.printf("<TH CLASS='data'>Dependency Type</TH>");
	System.out.printf("<TH CLASS='data'>Dependency Failure Options</TH>");
	System.out.printf("</TR>\n");

	/* check all the host dependencies... */
	for(objects_h.hostdependency temp_hd : objects.hostdependency_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_hd.dependent_host_name),temp_hd.dependent_host_name);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_hd.host_name),temp_hd.host_name);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,(temp_hd.dependency_type==common_h.NOTIFICATION_DEPENDENCY)?"Notification":"Check Execution");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=common_h.FALSE;
		if(temp_hd.fail_on_up==common_h.TRUE){
			System.out.printf("Up");
			options=common_h.TRUE;
		        }
		if(temp_hd.fail_on_down==common_h.TRUE){
			System.out.printf("%sDown",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_hd.fail_on_unreachable==common_h.TRUE){
			System.out.printf("%sUnreachable",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_hd.fail_on_pending==common_h.TRUE){
			System.out.printf("%sPending",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void display_hostescalations(){
    int first = 0;
	int options=common_h.FALSE;
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view hostgroup information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_HOSTESCALATIONS);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Host Escalations</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host</TH>");
	System.out.printf("<TH CLASS='data'>Contact Groups</TH>");
	System.out.printf("<TH CLASS='data'>First Notification</TH>");
	System.out.printf("<TH CLASS='data'>Last Notification</TH>");
	System.out.printf("<TH CLASS='data'>Notification Interval</TH>");
	System.out.printf("<TH CLASS='data'>Escalation Period</TH>");
	System.out.printf("<TH CLASS='data'>Escalation Options</TH>");
	System.out.printf("</TR>\n");

	/* check all the host escalations... */
	for(objects_h.hostescalation temp_he : objects.hostescalation_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_he.host_name),temp_he.host_name);

		System.out.printf("<TD CLASS='%s'>",bg_class);
        first = 0;
		for(objects_h.contactgroupsmember temp_contactgroupsmember : (ArrayList<objects_h.contactgroupsmember>) temp_he.contact_groups ){

			if( first++ > 0 )
				System.out.printf(", ");

			System.out.printf("<A HREF='%s?type=contactgroups#%s'>%s</A>\n",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_contactgroupsmember.group_name),temp_contactgroupsmember.group_name);
		        }
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>%d</TD>",bg_class,temp_he.first_notification);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_he.last_notification==0)
			System.out.printf("Infinity");
		else
			System.out.printf("%d",temp_he.last_notification);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_he.notification_interval==0)
			System.out.printf("Notify Only Once (No Re-notification)");
		else
			System.out.printf("%d",temp_he.notification_interval);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		if(temp_he.escalation_period==null)
			System.out.printf("&nbsp;");
		else
			System.out.printf("<A HREF='%s?type=timeperiods#%s'>%s</A>",cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_he.escalation_period),temp_he.escalation_period);
		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='%s'>",bg_class);
		options=common_h.FALSE;
		if(temp_he.escalate_on_down==common_h.TRUE){
			System.out.printf("%sDown",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_he.escalate_on_unreachable==common_h.TRUE){
			System.out.printf("%sUnreachable",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(temp_he.escalate_on_recovery==common_h.TRUE){
			System.out.printf("%sRecovery",(options==common_h.TRUE)?", ":"");
			options=common_h.TRUE;
		        }
		if(options==common_h.FALSE)
			System.out.printf("None");
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }


public static void display_hostextinfo(){
	
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view hostdependency information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
	cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_HOSTEXTINFO);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Extended Host Information</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host</TH>");
	System.out.printf("<TH CLASS='data'>Notes URL</TH>");
	System.out.printf("<TH CLASS='data'>2-D Coords</TH>");
	System.out.printf("<TH CLASS='data'>3-D Coords</TH>");
	System.out.printf("<TH CLASS='data'>Statusmap Image</TH>");
	System.out.printf("<TH CLASS='data'>VRML Image</TH>");
	System.out.printf("<TH CLASS='data'>Logo Image</TH>");
	System.out.printf("<TH CLASS='data'>Image Alt</TH>");
	System.out.printf("</TR>\n");

	/* check all the definitions... */
	for(objects_h.hostextinfo temp_hostextinfo : objects.hostextinfo_list ){

		if(odd != 0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_hostextinfo.host_name),temp_hostextinfo.host_name);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,(temp_hostextinfo.notes_url==null)?"&nbsp;":temp_hostextinfo.notes_url);

		if(temp_hostextinfo.have_2d_coords==common_h.FALSE)
			System.out.printf("<TD CLASS='%s'>&nbsp;</TD>",bg_class);
		else
			System.out.printf("<TD CLASS='%s'>%d,%d</TD>",bg_class,temp_hostextinfo.x_2d,temp_hostextinfo.y_2d);

		if(temp_hostextinfo.have_3d_coords==common_h.FALSE)
			System.out.printf("<TD CLASS='%s'>&nbsp;</TD>",bg_class);
		else
			System.out.printf("<TD CLASS='%s'>%.2f,%.2f,%.2f</TD>",bg_class,temp_hostextinfo.x_3d,temp_hostextinfo.y_3d,temp_hostextinfo.z_3d);

		if(temp_hostextinfo.statusmap_image==null)
			System.out.printf("<TD CLASS='%s'>&nbsp;</TD>",bg_class);
		else
			System.out.printf("<TD CLASS='%s' valign='center'><img src='%s%s' border='0' width='20' height='20'> %s</TD>",bg_class,cgiutils.url_logo_images_path,temp_hostextinfo.statusmap_image,temp_hostextinfo.statusmap_image);

		if(temp_hostextinfo.vrml_image==null)
			System.out.printf("<TD CLASS='%s'>&nbsp;</TD>",bg_class);
		else
			System.out.printf("<TD CLASS='%s' valign='center'><img src='%s%s' border='0' width='20' height='20'> %s</TD>",bg_class,cgiutils.url_logo_images_path,temp_hostextinfo.vrml_image,temp_hostextinfo.vrml_image);

		if(temp_hostextinfo.icon_image==null)
			System.out.printf("<TD CLASS='%s'>&nbsp;</TD>",bg_class);
		else
			System.out.printf("<TD CLASS='%s' valign='center'><img src='%s%s' border='0' width='20' height='20'> %s</TD>",bg_class,cgiutils.url_logo_images_path,temp_hostextinfo.icon_image,temp_hostextinfo.icon_image);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,(temp_hostextinfo.icon_image_alt==null)?"&nbsp;":temp_hostextinfo.icon_image_alt);

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }


public static void display_serviceextinfo(){
	
	int odd=0;
	String bg_class="";

	/* see if user is authorized to view hostdependency information... */
	if(cgiauth.is_authorized_for_configuration_information(current_authdata)==common_h.FALSE){
		unauthorized_message();
		return;
	        }

	/* read in command definitions... */
    cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_HOSTEXTINFO);

	System.out.printf("<P><DIV ALIGN=CENTER CLASS='dataTitle'>Extended Service Information</DIV></P>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data' COLSPAN=2>Service</TH>");
	System.out.printf("</TR>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Host</TH>");
	System.out.printf("<TH CLASS='data'>Description</TH>");
	System.out.printf("<TH CLASS='data'>Notes URL</TH>");
	System.out.printf("<TH CLASS='data'>Logo Image</TH>");
	System.out.printf("<TH CLASS='data'>Image Alt</TH>");
	System.out.printf("</TR>\n");

	/* check all the definitions... */
	for(objects_h.serviceextinfo temp_serviceextinfo : objects.serviceextinfo_list ){

		if(odd!=0){
			odd=0;
			bg_class="dataOdd";
		        }
		else{
			odd=1;
			bg_class="dataEven";
		        }

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=hosts#%s'>%s</A></TD>",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_serviceextinfo.host_name),temp_serviceextinfo.host_name);

		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=services#%s;",bg_class,cgiutils_h.CONFIG_CGI,cgiutils.url_encode(temp_serviceextinfo.host_name));
		System.out.printf("%s'>%s</A></TD>\n",cgiutils.url_encode(temp_serviceextinfo.description),temp_serviceextinfo.description);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,(temp_serviceextinfo.notes_url==null)?"&nbsp;":temp_serviceextinfo.notes_url);

		if(temp_serviceextinfo.icon_image==null)
			System.out.printf("<TD CLASS='%s'>&nbsp;</TD>",bg_class);
		else
			System.out.printf("<TD CLASS='%s' valign='center'><img src='%s%s' border='0' width='20' height='20'> %s</TD>",bg_class,cgiutils.url_logo_images_path,temp_serviceextinfo.icon_image,temp_serviceextinfo.icon_image);

		System.out.printf("<TD CLASS='%s'>%s</TD>",bg_class,(temp_serviceextinfo.icon_image_alt==null)?"&nbsp;":temp_serviceextinfo.icon_image_alt);

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



public static void unauthorized_message(){

	System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view the configuration information you requested...</DIV></P>\n");
	System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
	System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

	return;
	}




public static void display_options(){

	System.out.printf("<br><br>\n");

	System.out.printf("<div align=center class='reportSelectTitle'>Select Type of Config Data You Wish To View</div>\n");

	System.out.printf("<br><br>\n");

        System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.CONFIG_CGI);

	System.out.printf("<div align=center>\n");
	System.out.printf("<table border=0>\n");

	System.out.printf("<tr><td align=left class='reportSelectSubTitle'>Object Type:</td></tr>\n");
	System.out.printf("<tr><td align=left class='reportSelectItem'>");
	System.out.printf("<select name='type'>\n");
	System.out.printf("<option value='hosts' %s>Hosts\n",(display_type==DISPLAY_HOSTS)?"SELECTED":"");
	System.out.printf("<option value='hostdependencies' %s>Host Dependencies\n",(display_type==DISPLAY_HOSTDEPENDENCIES)?"SELECTED":"");
	System.out.printf("<option value='hostescalations' %s>Host Escalations\n",(display_type==DISPLAY_HOSTESCALATIONS)?"SELECTED":"");
	System.out.printf("<option value='hostgroups' %s>Host Groups\n",(display_type==DISPLAY_HOSTGROUPS)?"SELECTED":"");
	System.out.printf("<option value='services' %s>Services\n",(display_type==DISPLAY_SERVICES)?"SELECTED":"");
	System.out.printf("<option value='servicegroups' %s>Service Groups\n",(display_type==DISPLAY_SERVICEGROUPS)?"SELECTED":"");
	System.out.printf("<option value='servicedependencies' %s>Service Dependencies\n",(display_type==DISPLAY_SERVICEDEPENDENCIES)?"SELECTED":"");
	System.out.printf("<option value='serviceescalations' %s>Service Escalations\n",(display_type==DISPLAY_SERVICEESCALATIONS)?"SELECTED":"");
	System.out.printf("<option value='contacts' %s>Contacts\n",(display_type==DISPLAY_CONTACTS)?"SELECTED":"");
	System.out.printf("<option value='contactgroups' %s>Contact Groups\n",(display_type==DISPLAY_CONTACTGROUPS)?"SELECTED":"");
	System.out.printf("<option value='timeperiods' %s>Timeperiods\n",(display_type==DISPLAY_TIMEPERIODS)?"SELECTED":"");
	System.out.printf("<option value='commands' %s>Commands\n",(display_type==DISPLAY_COMMANDS)?"SELECTED":"");
	System.out.printf("<option value='hostextinfo' %s>Extended Host Information\n",(display_type==DISPLAY_HOSTEXTINFO)?"SELECTED":"");
	System.out.printf("<option value='serviceextinfo' %s>Extended Service Information\n",(display_type==DISPLAY_SERVICEEXTINFO)?"SELECTED":"");
	System.out.printf("</select>\n");
	System.out.printf("</td></tr>\n");

	System.out.printf("<tr><td class='reportSelectItem'><input type='submit' value='Continue'></td></tr>\n");
	System.out.printf("</table>\n");
	System.out.printf("</div>\n");

	System.out.printf("</form>\n");

	return;
        }
}