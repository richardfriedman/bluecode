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
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.config_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class statuswml extends blue_servlet {

public static final int DISPLAY_HOST		        =0;
public static final int DISPLAY_SERVICE                 =1;
public static final int DISPLAY_HOSTGROUP               =2;
public static final int DISPLAY_INDEX                   =3;
public static final int DISPLAY_PING                    =4;
public static final int DISPLAY_TRACEROUTE              =5;
public static final int DISPLAY_QUICKSTATS              =6;
public static final int DISPLAY_PROCESS                 =7;
public static final int DISPLAY_ALL_PROBLEMS            =8;
public static final int DISPLAY_UNHANDLED_PROBLEMS      =9;

public static final int DISPLAY_HOSTGROUP_SUMMARY       =0;
public static final int DISPLAY_HOSTGROUP_OVERVIEW      =1;

public static final int DISPLAY_HOST_SUMMARY            =0;
public static final int DISPLAY_HOST_SERVICES           =1;

public static int display_type=DISPLAY_INDEX;
public static int hostgroup_style=DISPLAY_HOSTGROUP_SUMMARY;
public static int host_style=DISPLAY_HOST_SUMMARY;

public static String host_name="";
public static String hostgroup_name="";
public static String service_desc="";
public static String ping_address="";
public static String traceroute_address="";

public static int show_all_hostgroups=common_h.TRUE;

public static cgiauth_h.authdata current_authdata;

public void reset_context() { 
   display_type=DISPLAY_INDEX;
   hostgroup_style=DISPLAY_HOSTGROUP_SUMMARY;
   host_style=DISPLAY_HOST_SUMMARY;

   host_name="";
   hostgroup_name="";
   service_desc="";
   ping_address="";
   traceroute_address="";

   show_all_hostgroups=common_h.TRUE;

   current_authdata = new cgiauth_h.authdata ();
}

public void call_main() {
   main( null );
}

public static void main(String[] args){
	int result=common_h.OK;
	
	/* get the arguments passed in the URL */
	process_cgivars();

	document_header();

	/* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		System.out.printf("<P>Error: Could not open CGI configuration file '%s' for reading!</P>\n",cgiutils.get_cgi_config_location());
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		System.out.printf("<P>Error: Could not open main configuration file '%s' for reading!</P>\n",cgiutils.main_config_file);
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		System.out.printf("<P>Error: Could not read some or all object configuration data!</P>\n");
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
                }

	/* read all status data */
	result=cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(),statusdata_h.READ_ALL_STATUS_DATA);
	if(result==common_h.ERROR){
		System.out.printf("<P>Error: Could not read host and service status information!</P>\n");
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
                }

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	/* decide what to display to the user */
	if(display_type==DISPLAY_HOST && host_style==DISPLAY_HOST_SERVICES)
		display_host_services();
	else if(display_type==DISPLAY_HOST)
		display_host();
	else if(display_type==DISPLAY_SERVICE)
		display_service();
	else if(display_type==DISPLAY_HOSTGROUP && hostgroup_style==DISPLAY_HOSTGROUP_OVERVIEW)
		display_hostgroup_overview();
	else if(display_type==DISPLAY_HOSTGROUP && hostgroup_style==DISPLAY_HOSTGROUP_SUMMARY)
		display_hostgroup_summary();
	else if(display_type==DISPLAY_PING)
		display_ping();
	else if(display_type==DISPLAY_TRACEROUTE)
		display_traceroute();
	else if(display_type==DISPLAY_QUICKSTATS)
		display_quick_stats();
	else if(display_type==DISPLAY_PROCESS)
		display_process();
	else if(display_type==DISPLAY_ALL_PROBLEMS || display_type==DISPLAY_UNHANDLED_PROBLEMS)
		display_problems();
	else
		display_index();

	document_footer();

	cgiutils.exit(  common_h.OK );
        }


public static void document_header(){
	String date_time ; // MAX_DATETIME_LENGTH

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );
       response.setContentType("text/vnd.wap.wml");
    } else {
      
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      	
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      
      	System.out.printf("Content-type: text/vnd.wap.wml\r\n\r\n");
    }
    
	System.out.printf("<?xml version=\"1.0\"?>\n");
	System.out.printf("<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">\n");

	System.out.printf("<wml>\n");
	
	System.out.printf("<head>\n");
	System.out.printf("<meta forua=\"true\" http-equiv=\"Cache-Control\" content=\"max-age=0\"/>\n");
	System.out.printf("</head>\n");

	return;
        }


public static void document_footer(){

	System.out.printf("</wml>\n");

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars(request_string);

	for(x=0; x < variables.length ;x++){

		/* we found the hostgroup argument */
		if(variables[x].equals("hostgroup")){
			display_type=DISPLAY_HOSTGROUP;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				hostgroup_name = variables[x];

			if( hostgroup_name.equals("all"))
				show_all_hostgroups=common_h.TRUE;
			else
				show_all_hostgroups=common_h.FALSE;
		        }

		/* we found the host argument */
		else if(variables[x].equals("host")){
			display_type=DISPLAY_HOST;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				host_name = variables[x];
		        }

		/* we found the service argument */
		else if(variables[x].equals("service")){
			display_type=DISPLAY_SERVICE;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				service_desc = variables[x];
		        }


		/* we found the hostgroup style argument */
		else if(variables[x].equals("style")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("overview"))
				hostgroup_style=DISPLAY_HOSTGROUP_OVERVIEW;
			else if(variables[x].equals("summary"))
				hostgroup_style=DISPLAY_HOSTGROUP_SUMMARY;
			else if(variables[x].equals("servicedetail"))
				host_style=DISPLAY_HOST_SERVICES;
			else if(variables[x].equals("processinfo"))
				display_type=DISPLAY_PROCESS;
			else if(variables[x].equals("aprobs"))
				display_type=DISPLAY_ALL_PROBLEMS;
			else if(variables[x].equals("uprobs"))
				display_type=DISPLAY_UNHANDLED_PROBLEMS;
			else
				display_type=DISPLAY_QUICKSTATS;
		        }		        

		/* we found the ping argument */
		else if(variables[x].equals("ping")){
			display_type=DISPLAY_PING;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				ping_address = variables[x];
		        }

		/* we found the traceroute argument */
		else if(variables[x].equals("traceroute")){
			display_type=DISPLAY_TRACEROUTE;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				traceroute_address = variables[x];
		        }

	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



/* main intro screen */
public static void display_index(){


	/**** MAIN MENU SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Nagios WAP Interface'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");

	System.out.printf("<b>Nagios</b><br/><b>WAP Interface</b><br/>\n");

	System.out.printf("<b><anchor title='Quick Stats'>Quick Stats<go href='%s'><postfield name='style' value='quickstats'/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("<b><anchor title='Status Summary'>Status Summary<go href='%s'><postfield name='hostgroup' value='all'/><postfield name='style' value='summary'/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("<b><anchor title='Status Overview'>Status Overview<go href='%s'><postfield name='hostgroup' value='all'/><postfield name='style' value='overview'/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("<b><anchor title='All Problems'>All Problems<go href='%s'><postfield name='style' value='aprobs'/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("<b><anchor title='Unhandled Problems'>Unhandled Problems<go href='%s'><postfield name='style' value='uprobs'/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("<b><anchor title='Process Info'>Process Info<go href='%s'><postfield name='style' value='processinfo'/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("<b><anchor title='Network Tools'>Tools<go href='#card2'/></anchor></b><br/>\n");

	System.out.printf("<b><anchor title='About'>About<go href='#card3'/></anchor></b><br/>\n");

	System.out.printf("</p>\n");
	System.out.printf("</card>\n");


	/**** TOOLS SCREEN (CARD 2) ****/
	System.out.printf("<card id='card2' title='Network Tools'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");

	System.out.printf("<b>Network Tools:</b><br/>\n");

	System.out.printf("<b><anchor title='Ping Host'>Ping<go href='%s'><postfield name='ping' value=''/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);
	System.out.printf("<b><anchor title='Traceroute'>Traceroute<go href='%s'><postfield name='traceroute' value=''/></go></anchor></b><br/>\n",cgiutils_h.STATUSWML_CGI);
	System.out.printf("<b><anchor title='View Host'>View Host<go href='#card4'/></anchor></b><br/>\n");
	System.out.printf("<b><anchor title='View Hostgroup'>View Hostgroup<go href='#card5'/></anchor></b><br/>\n");

	System.out.printf("</p>\n");
	System.out.printf("</card>\n");


	/**** ABOUT SCREEN (CARD 3) ****/
	System.out.printf("<card id='card3' title='About'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>About</b><br/>\n");
	System.out.printf("</p>\n");

	System.out.printf("<p align='center' mode='wrap'>\n");
	System.out.printf("<b>Nagios %s</b><br/><b>WAP Interface</b><br/>\n",common_h.PROGRAM_VERSION);
	System.out.printf("Copyright (C) 2001 Ethan Galstad<br/>\n");
	System.out.printf("blue@blue.org<br/><br/>\n");
	System.out.printf("License: <b>GPL</b><br/><br/>\n");
	System.out.printf("Based in part on features found in AskAround's Wireless Network Tools<br/>\n");
	System.out.printf("<b>www.askaround.com</b><br/>\n");
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");



	/**** VIEW HOST SCREEN (CARD 4) ****/
	System.out.printf("<card id='card4' title='View Host'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>View Host</b><br/>\n");
	System.out.printf("</p>\n");

	System.out.printf("<p align='center' mode='wrap'>\n");
	System.out.printf("<b>Host Name:</b><br/>\n");
	System.out.printf("<input name='hname'/>\n");
	System.out.printf("<do type='accept'>\n");
	System.out.printf("<go href='%s' method='post'><postfield name='host' value='$(hname)'/></go>\n",cgiutils_h.STATUSWML_CGI);
	System.out.printf("</do>\n");
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");



	/**** VIEW HOSTGROUP SCREEN (CARD 5) ****/
	System.out.printf("<card id='card5' title='View Hostgroup'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>View Hostgroup</b><br/>\n");
	System.out.printf("</p>\n");

	System.out.printf("<p align='center' mode='wrap'>\n");
	System.out.printf("<b>Hostgroup Name:</b><br/>\n");
	System.out.printf("<input name='gname'/>\n");
	System.out.printf("<do type='accept'>\n");
	System.out.printf("<go href='%s' method='post'><postfield name='hostgroup' value='$(gname)'/><postfield name='style' value='overview'/></go>\n",cgiutils_h.STATUSWML_CGI);
	System.out.printf("</do>\n");
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	return;
        }


/* displays process info */
public static void display_process(){


	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Process Info'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Process Info</b><br/><br/>\n");

	/* check authorization */
	if(cgiauth.is_authorized_for_system_information(current_authdata)==common_h.FALSE){

		System.out.printf("<b>Error: Not authorized for process info!</b>\n");
		System.out.printf("</p>\n");
		System.out.printf("</card>\n");
		return;
	        }

	if(cgiutils.blue_process_state==blue_h.STATE_OK)
		System.out.printf("Nagios process is running<br/>\n");
	else
		System.out.printf("<b>Nagios process may not be running</b><br/>\n");

	if(blue.enable_notifications==common_h.TRUE)
		System.out.printf("Notifications are enabled<br/>\n");
	else
		System.out.printf("<b>Notifications are disabled</b><br/>\n");

	if(blue.execute_service_checks==common_h.TRUE)
		System.out.printf("Check execution is enabled<br/>\n");
	else
		System.out.printf("<b>Check execution is disabled</b><br/>\n");

	System.out.printf("<br/>\n");
	System.out.printf("<b><anchor title='Process Commands'>Process Commands<go href='#card2'/></anchor></b>\n");
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	/**** COMMANDS SCREEN (CARD 2) ****/
	System.out.printf("<card id='card2' title='Process Commands'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Process Commands</b><br/>\n");

	if(blue.enable_notifications==common_h.FALSE)
		System.out.printf("<b><anchor title='Enable Notifications'>Enable Notifications<go href='%s' method='post'><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);
	else
		System.out.printf("<b><anchor title='Disable Notifications'>Disable Notifications<go href='%s' method='post'><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);

	if(blue.execute_service_checks==common_h.FALSE)
		System.out.printf("<b><anchor title='Enable Check Execution'>Enable Check Execution<go href='%s' method='post'><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,common_h.CMD_START_EXECUTING_SVC_CHECKS,cgiutils_h.CMDMODE_COMMIT);
	else
		System.out.printf("<b><anchor title='Disable Check Execution'>Disable Check Execution<go href='%s' method='post'><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_EXECUTING_SVC_CHECKS,cgiutils_h.CMDMODE_COMMIT);

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	return;
        }



/* displays quick stats */
public static void display_quick_stats(){
//	objects_h.host temp_host;
	statusdata_h.hoststatus temp_hoststatus;
//	objects_h.service temp_service;
	statusdata_h.servicestatus temp_servicestatus;
	int hosts_unreachable=0;
	int hosts_down=0;
	int hosts_up=0;
	int hosts_pending=0;
	int services_critical=0;
	int services_unknown=0;
	int services_warning=0;
	int services_ok=0;
	int services_pending=0;


	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Quick Stats'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Quick Stats</b><br/>\n");
	System.out.printf("</p>\n");

	/* check all hosts */
	for( objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			continue;

		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			hosts_unreachable++;
		else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			hosts_down++;
		else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
			hosts_pending++;
		else
			hosts_up++;
	        }

	/* check all services */
	for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ){

		if( cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			continue;

		temp_servicestatus= statusdata.find_servicestatus(temp_service.host_name,temp_service.description);
		if(temp_servicestatus==null)
			continue;

		if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
			services_critical++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
			services_unknown++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
			services_warning++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
			services_pending++;
		else
			services_ok++;
	        }

	System.out.printf("<p align='left' mode='nowrap'>\n");

	System.out.printf("<b>Host Totals</b>:<br/>\n");
	System.out.printf("%d UP<br/>\n",hosts_up);
	System.out.printf("%d DOWN<br/>\n",hosts_down);
	System.out.printf("%d UNREACHABLE<br/>\n",hosts_unreachable);
	System.out.printf("%d PENDING<br/>\n",hosts_pending);

	System.out.printf("<br/>\n");
	
	System.out.printf("<b>Service Totals:</b><br/>\n");
	System.out.printf("%d OK<br/>\n",services_ok);
	System.out.printf("%d WARNING<br/>\n",services_warning);
	System.out.printf("%d UNKNOWN<br/>\n",services_unknown);
	System.out.printf("%d CRITICAL<br/>\n",services_critical);
	System.out.printf("%d PENDING<br/>\n",services_pending);
	
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");

	return;
        }



/* displays hostgroup status overview */
public static void display_hostgroup_overview(){
//	objects_h.hostgroup temp_hostgroup;
//  objects_h.hostgroupmember temp_member;
    objects_h.host temp_host;
    statusdata_h.hoststatus temp_hoststatus;

	
	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Status Overview'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");

	System.out.printf("<b><anchor title='Status Overview'>Status Overview<go href='%s' method='post'><postfield name='hostgroup' value='%s'/><postfield name='style' value='summary'/></go></anchor></b><br/><br/>\n",cgiutils_h.STATUSWML_CGI,hostgroup_name);

	/* check all hostgroups */
	for(objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list ){

		if(show_all_hostgroups==common_h.FALSE && !temp_hostgroup.group_name.equals(hostgroup_name))
			continue;

		if( cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.FALSE)
			continue;

		System.out.printf("<b>%s</b>\n",temp_hostgroup.alias);

		System.out.printf("<table columns='2' align='LL'>\n");

		/* check all hosts in this hostgroup */
		for( objects_h.hostgroupmember temp_member : (ArrayList<objects_h.hostgroupmember>) temp_hostgroup.members ){

			temp_host=objects.find_host(temp_member.host_name);
			if(temp_host==null)
				continue;

			if( objects.is_host_member_of_hostgroup(temp_hostgroup,temp_host)==common_h.FALSE)
				continue;

			temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
			if(temp_hoststatus==null)
				continue;

			System.out.printf("<tr><td><anchor title='%s'>",temp_host.name);
			if(temp_hoststatus.status==statusdata_h.HOST_UP)
				System.out.printf("UP");
			else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
				System.out.printf("PND");
			else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
				System.out.printf("DWN");
			else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
				System.out.printf("UNR");
			else
				System.out.printf("???");
			System.out.printf("<go href='%s' method='post'><postfield name='host' value='%s'/></go></anchor></td>",cgiutils_h.STATUSWML_CGI,temp_host.name);
			System.out.printf("<td>%s</td></tr>\n",temp_host.name);
		        }

		System.out.printf("</table>\n");

		System.out.printf("<br/>\n");
	        }

	if(show_all_hostgroups==common_h.FALSE)
		System.out.printf("<b><anchor title='View All Hostgroups'>View All Hostgroups<go href='%s' method='post'><postfield name='hostgroup' value='all'/><postfield name='style' value='overview'/></go></anchor></b>\n",cgiutils_h.STATUSWML_CGI);

	System.out.printf("</p>\n");
	System.out.printf("</card>\n");

	return;
        }


/* displays hostgroup status summary */
public static void display_hostgroup_summary(){
//	objects_h.hostgroup temp_hostgroup;
//    objects_h.hostgroupmember temp_member;
    objects_h.host temp_host;
	statusdata_h.hoststatus temp_hoststatus;
//    objects_h.service temp_service;
	statusdata_h.servicestatus temp_servicestatus;
	int hosts_unreachable=0;
	int hosts_down=0;
	int hosts_up=0;
	int hosts_pending=0;
	int services_critical=0;
	int services_unknown=0;
	int services_warning=0;
	int services_ok=0;
	int services_pending=0;
	int found=0;


	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Status Summary'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");

	System.out.printf("<b><anchor title='Status Summary'>Status Summary<go href='%s' method='post'><postfield name='hostgroup' value='%s'/><postfield name='style' value='overview'/></go></anchor></b><br/><br/>\n",cgiutils_h.STATUSWML_CGI,hostgroup_name);

	/* check all hostgroups */
	for(objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list ){

		if(show_all_hostgroups==common_h.FALSE && !temp_hostgroup.group_name.equals(hostgroup_name))
			continue;

		if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.FALSE)
			continue;

		System.out.printf("<b><anchor title='%s'>%s<go href='%s' method='post'><postfield name='hostgroup' value='%s'/><postfield name='style' value='overview'/></go></anchor></b>\n",temp_hostgroup.group_name,temp_hostgroup.alias,cgiutils_h.STATUSWML_CGI,temp_hostgroup.group_name);

		System.out.printf("<table columns='2' align='LL'>\n");

		hosts_up=0;
		hosts_pending=0;
		hosts_down=0;
		hosts_unreachable=0;

		services_ok=0;
		services_pending=0;
		services_warning=0;
		services_unknown=0;
		services_critical=0;

		/* check all hosts in this hostgroup */
		for(objects_h.hostgroupmember temp_member : (ArrayList<objects_h.hostgroupmember>) temp_hostgroup.members ){

			temp_host=objects.find_host(temp_member.host_name);
			if(temp_host==null)
				continue;

			if( objects.is_host_member_of_hostgroup(temp_hostgroup,temp_host)==common_h.FALSE)
				continue;

			temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
			if(temp_hoststatus==null)
				continue;

			if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
				hosts_unreachable++;
			else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
				hosts_down++;
			else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
				hosts_pending++;
			else
				hosts_up++;

			/* check all services on this host */
			for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ){

				if(!temp_service.host_name.equals(temp_host.name))
					continue;

				if( cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
					continue;

				temp_servicestatus= statusdata.find_servicestatus(temp_service.host_name,temp_service.description);
				if(temp_servicestatus==null)
					continue;

				if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
					services_critical++;
				else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
					services_unknown++;
				else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
					services_warning++;
				else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
					services_pending++;
				else
					services_ok++;
			        }
		        }

		System.out.printf("<tr><td>Hosts:</td><td>");
		found=0;
		if(hosts_unreachable>0){
			System.out.printf("%d UNR",hosts_unreachable);
			found=1;
		        }
		if(hosts_down>0){
			System.out.printf("%s%d DWN",(found==1)?", ":"",hosts_down);
			found=1;
		        }
		if(hosts_pending>0){
			System.out.printf("%s%d PND",(found==1)?", ":"",hosts_pending);
			found=1;
		        }
		System.out.printf("%s%d UP",(found==1)?", ":"",hosts_up);
		System.out.printf("</td></tr>\n");
		System.out.printf("<tr><td>Services:</td><td>");
		found=0;
		if(services_critical>0){
			System.out.printf("%d CRI",services_critical);
			found=1;
		        }
		if(services_warning>0){
			System.out.printf("%s%d WRN",(found==1)?", ":"",services_warning);
			found=1;
		        }
		if(services_unknown>0){
			System.out.printf("%s%d UNK",(found==1)?", ":"",services_unknown);
			found=1;
		        }
		if(services_pending>0){
			System.out.printf("%s%d PND",(found==1)?", ":"",services_pending);
			found=1;
		        }
		System.out.printf("%s%d OK",(found==1)?", ":"",services_ok);
		System.out.printf("</td></tr>\n");

		System.out.printf("</table>\n");

		System.out.printf("<br/>\n");
	        }

	if(show_all_hostgroups==common_h.FALSE)
		System.out.printf("<b><anchor title='View All Hostgroups'>View All Hostgroups<go href='%s' method='post'><postfield name='hostgroup' value='all'/><postfield name='style' value='summary'/></go></anchor></b>\n",cgiutils_h.STATUSWML_CGI);
	
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");

	return;
        } 



/* displays host status */
public static void display_host(){
	objects_h.host temp_host;
	statusdata_h.hoststatus temp_hoststatus;
	String last_check ; // MAX_DATETIME_LENGTH
	long current_time;
	long t;
	String state_duration ; // 48
	int found;

	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Host Status'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Host '%s'</b><br/>\n",host_name);

	/* find the host */
	temp_host=objects.find_host(host_name);
	temp_hoststatus=statusdata.find_hoststatus(host_name);
	if(temp_host==null || temp_hoststatus==null){

		System.out.printf("<b>Error: Could not find host!</b>\n");
		System.out.printf("</p>\n");
		System.out.printf("</card>\n");
		return;
	        }

	/* check authorization */
	if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE){

		System.out.printf("<b>Error: Not authorized for host!</b>\n");
		System.out.printf("</p>\n");
		System.out.printf("</card>\n");
		return;
	        }


	System.out.printf("<table columns='2' align='LL'>\n");

	System.out.printf("<tr><td>Status:</td><td>");
	if(temp_hoststatus.status==statusdata_h.HOST_UP)
		System.out.printf("UP");
	else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
		System.out.printf("PENDING");
	else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
		System.out.printf("DOWN");
	else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
		System.out.printf("UNREACHABLE");
	else
		System.out.printf("?");
	System.out.printf("</td></tr>\n");

	System.out.printf("<tr><td>Info:</td><td>%s</td></tr>\n",temp_hoststatus.plugin_output);

    last_check = cgiutils.get_time_string(temp_hoststatus.last_check, common_h.SHORT_DATE_TIME);
	System.out.printf("<tr><td>Last Check:</td><td>%s</td></tr>\n",last_check);

	current_time= utils.currentTimeInSeconds();
	if(temp_hoststatus.last_state_change==0)
		t=current_time-blue.program_start;
	else
		t=current_time-temp_hoststatus.last_state_change;
	cgiutils.time_breakdown tb = cgiutils.get_time_breakdown( t );
	state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_hoststatus.last_state_change==0)?"+":"");
	System.out.printf("<tr><td>Duration:</td><td>%s</td></tr>\n",state_duration);

	System.out.printf("<tr><td>Properties:</td><td>");
	found=0;
	if(temp_hoststatus.checks_enabled==common_h.FALSE){
		System.out.printf("%sChecks disabled",(found==1)?", ":"");
		found=1;
	        }
	if(temp_hoststatus.notifications_enabled==common_h.FALSE){
		System.out.printf("%sNotifications disabled",(found==1)?", ":"");
		found=1;
	        }
	if(temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE){
		System.out.printf("%sProblem acknowledged",(found==1)?", ":"");
		found=1;
	        }
	if(temp_hoststatus.scheduled_downtime_depth>0){
		System.out.printf("%sIn scheduled downtime",(found==1)?", ":"");
		found=1;
	        }
	if(found==0)
		System.out.printf("N/A");
	System.out.printf("</td></tr>\n");

	System.out.printf("</table>\n");
	System.out.printf("<br/>\n");
	System.out.printf("<b><anchor title='View Services'>View Services<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='style' value='servicedetail'/></go></anchor></b>\n",cgiutils_h.STATUSWML_CGI,host_name);
	System.out.printf("<b><anchor title='Host Commands'>Host Commands<go href='#card2'/></anchor></b>\n");
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	/**** COMMANDS SCREEN (CARD 2) ****/
	System.out.printf("<card id='card2' title='Host Commands'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Host Commands</b><br/>\n");

	System.out.printf("<b><anchor title='Ping Host'>Ping Host<go href='%s' method='post'><postfield name='ping' value='%s'/></go></anchor></b>\n",cgiutils_h.STATUSWML_CGI,temp_host.address);
	System.out.printf("<b><anchor title='Traceroute'>Traceroute<go href='%s' method='post'><postfield name='traceroute' value='%s'/></go></anchor></b>\n",cgiutils_h.STATUSWML_CGI,temp_host.address);

	if(temp_hoststatus.status!=statusdata_h.HOST_UP && temp_hoststatus.status!=statusdata_h.HOST_PENDING)
		System.out.printf("<b><anchor title='Acknowledge Problem'>Acknowledge Problem<go href='#card3'/></anchor></b>\n");

	if(temp_hoststatus.checks_enabled==common_h.FALSE)
		System.out.printf("<b><anchor title='Enable Host Checks'>Enable Host Checks<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_ENABLE_HOST_CHECK,cgiutils_h.CMDMODE_COMMIT);
	else
		System.out.printf("<b><anchor title='Disable Host Checks'>Disable Host Checks<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_DISABLE_HOST_CHECK,cgiutils_h.CMDMODE_COMMIT);

	if(temp_hoststatus.notifications_enabled==common_h.FALSE)
		System.out.printf("<b><anchor title='Enable Host Notifications'>Enable Host Notifications<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_ENABLE_HOST_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);
	else
		System.out.printf("<b><anchor title='Disable Host Notifications'>Disable Host Notifications<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_DISABLE_HOST_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);


	System.out.printf("<b><anchor title='Enable All Service Checks'>Enable All Service Checks<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_ENABLE_HOST_SVC_CHECKS,cgiutils_h.CMDMODE_COMMIT);

	System.out.printf("<b><anchor title='Disable All Service Checks'>Disable All Service Checks<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_DISABLE_HOST_SVC_CHECKS,cgiutils_h.CMDMODE_COMMIT);

	System.out.printf("<b><anchor title='Enable All Service Notifications'>Enable All Service Notifications<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);

	System.out.printf("<b><anchor title='Disable All Service Notifications'>Disable All Service Notifications<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	/**** ACKNOWLEDGEMENT SCREEN (CARD 3) ****/
	System.out.printf("<card id='card3' title='Acknowledge Problem'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Acknowledge Problem</b><br/>\n");
	System.out.printf("</p>\n");

	System.out.printf("<p align='center' mode='wrap'>\n");
	System.out.printf("<b>Your Name:</b><br/>\n");
	System.out.printf("<input name='name'/><br/>\n");
	System.out.printf("<b>Comment:</b><br/>\n");
	System.out.printf("<input name='comment'/>\n");

	System.out.printf("<do type='accept'>\n");
	System.out.printf("<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='com_author' value='$(name)'/><postfield name='com_data' value='$(comment)'/><postfield name='persistent' value=''/><postfield name='send_notification' value=''/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go>\n",cgiutils_h.COMMAND_CGI,host_name,common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM,cgiutils_h.CMDMODE_COMMIT);
	System.out.printf("</do>\n");

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");

	return;
        }



/* displays services on a host */
public static void display_host_services(){
//	objects_h.service temp_service;
	statusdata_h.servicestatus temp_servicestatus;

	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Host Services'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Host <anchor title='%s'>'%s'<go href='%s' method='post'><postfield name='host' value='%s'/></go></anchor> Services</b><br/>\n",host_name,host_name,cgiutils_h.STATUSWML_CGI,host_name);

	System.out.printf("<table columns='2' align='LL'>\n");

	/* check all services */
	for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ){

		if(!temp_service.host_name.equals(host_name))
			continue;

		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			continue;

		temp_servicestatus=statusdata.find_servicestatus(temp_service.host_name,temp_service.description);
		if(temp_servicestatus==null)
			continue;

		System.out.printf("<tr><td><anchor title='%s'>",temp_service.description);
		if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
			System.out.printf("OK");
		else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
			System.out.printf("PND");
		else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
			System.out.printf("WRN");
		else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
			System.out.printf("UNK");
		else if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
			System.out.printf("CRI");
		else
			System.out.printf("???");

		System.out.printf("<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/></go></anchor></td>",cgiutils_h.STATUSWML_CGI,temp_service.host_name,temp_service.description);
		System.out.printf("<td>%s</td></tr>\n",temp_service.description);
	        }

	System.out.printf("</table>\n");

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");

	return;
        }



/* displays service status */
public static void display_service(){
	objects_h.service temp_service;
	statusdata_h.servicestatus temp_servicestatus;
	String last_check ; // MAX_DATETIME_LENGTH
	long current_time;
	long t;
	String state_duration ; // 48
	int found;

	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Service Status'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Service '%s' on host '%s'</b><br/>\n",service_desc,host_name);

	/* find the service */
	temp_service=objects.find_service(host_name,service_desc);
	temp_servicestatus=statusdata.find_servicestatus(host_name,service_desc);
	if(temp_service==null || temp_servicestatus==null){

		System.out.printf("<b>Error: Could not find service!</b>\n");
		System.out.printf("</p>\n");
		System.out.printf("</card>\n");
		return;
	        }

	/* check authorization */
	if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE){

		System.out.printf("<b>Error: Not authorized for service!</b>\n");
		System.out.printf("</p>\n");
		System.out.printf("</card>\n");
		return;
	        }


	System.out.printf("<table columns='2' align='LL'>\n");

	System.out.printf("<tr><td>Status:</td><td>");
	if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
		System.out.printf("OK");
	else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
		System.out.printf("PENDING");
	else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
		System.out.printf("WARNING");
	else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
		System.out.printf("UNKNOWN");
	else if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
		System.out.printf("CRITICAL");
	else
		System.out.printf("?");
	System.out.printf("</td></tr>\n");

	System.out.printf("<tr><td>Info:</td><td>%s</td></tr>\n",temp_servicestatus.plugin_output);

    last_check = cgiutils.get_time_string(temp_servicestatus.last_check, common_h.SHORT_DATE_TIME);
	System.out.printf("<tr><td>Last Check:</td><td>%s</td></tr>\n",last_check);

	current_time=utils.currentTimeInSeconds();
	if(temp_servicestatus.last_state_change==0)
		t=current_time-blue.program_start;
	else
		t=current_time-temp_servicestatus.last_state_change;
	cgiutils.time_breakdown tb = cgiutils.get_time_breakdown( t );
	state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_servicestatus.last_state_change==0)?"+":"");
	System.out.printf("<tr><td>Duration:</td><td>%s</td></tr>\n",state_duration);

	System.out.printf("<tr><td>Properties:</td><td>");
	found=0;
	if(temp_servicestatus.checks_enabled==common_h.FALSE){
		System.out.printf("%sChecks disabled",(found==1)?", ":"");
		found=1;
	        }
	if(temp_servicestatus.notifications_enabled==common_h.FALSE){
		System.out.printf("%sNotifications disabled",(found==1)?", ":"");
		found=1;
	        }
	if(temp_servicestatus.problem_has_been_acknowledged==common_h.TRUE){
		System.out.printf("%sProblem acknowledged",(found==1)?", ":"");
		found=1;
	        }
	if(temp_servicestatus.scheduled_downtime_depth>0){
		System.out.printf("%sIn scheduled downtime",(found==1)?", ":"");
		found=1;
	        }
	if(found==0)
		System.out.printf("N/A");
	System.out.printf("</td></tr>\n");

	System.out.printf("</table>\n");
	System.out.printf("<br/>\n");
	System.out.printf("<b><anchor title='View Host'>View Host<go href='%s' method='post'><postfield name='host' value='%s'/></go></anchor></b>\n",cgiutils_h.STATUSWML_CGI,host_name);
	System.out.printf("<b><anchor title='Service Commands'>Svc. Commands<go href='#card2'/></anchor></b>\n");
	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	/**** COMMANDS SCREEN (CARD 2) ****/
	System.out.printf("<card id='card2' title='Service Commands'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Service Commands</b><br/>\n");

	if(temp_servicestatus.status!=statusdata_h.SERVICE_OK && temp_servicestatus.status!=statusdata_h.SERVICE_PENDING)
		System.out.printf("<b><anchor title='Acknowledge Problem'>Acknowledge Problem<go href='#card3'/></anchor></b>\n");

	if(temp_servicestatus.checks_enabled==common_h.FALSE)
		System.out.printf("<b><anchor title='Enable Checks'>Enable Checks<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,service_desc,common_h.CMD_ENABLE_SVC_CHECK,cgiutils_h.CMDMODE_COMMIT);
	else{
		System.out.printf("<b><anchor title='Disable Checks'>Disable Checks<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,service_desc,common_h.CMD_DISABLE_SVC_CHECK,cgiutils_h.CMDMODE_COMMIT);
        System.out.printf("<b><anchor title='Schedule Immediate Check'>Schedule Immediate Check<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/><postfield name='start_time' value='%d'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,service_desc,current_time,common_h.CMD_SCHEDULE_SVC_CHECK,cgiutils_h.CMDMODE_COMMIT);
	        }

	if(temp_servicestatus.notifications_enabled==common_h.FALSE)
		System.out.printf("<b><anchor title='Enable Notifications'>Enable Notifications<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,service_desc,common_h.CMD_ENABLE_SVC_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);
	else
		System.out.printf("<b><anchor title='Disable Notifications'>Disable Notifications<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go></anchor></b><br/>\n",cgiutils_h.COMMAND_CGI,host_name,service_desc,common_h.CMD_DISABLE_SVC_NOTIFICATIONS,cgiutils_h.CMDMODE_COMMIT);

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");


	/**** ACKNOWLEDGEMENT SCREEN (CARD 3) ****/
	System.out.printf("<card id='card3' title='Acknowledge Problem'>\n");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>Acknowledge Problem</b><br/>\n");
	System.out.printf("</p>\n");

	System.out.printf("<p align='center' mode='wrap'>\n");
	System.out.printf("<b>Your Name:</b><br/>\n");
	System.out.printf("<input name='name'/><br/>\n");
	System.out.printf("<b>Comment:</b><br/>\n");
	System.out.printf("<input name='comment'/>\n");

	System.out.printf("<do type='accept'>\n");
	System.out.printf("<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/><postfield name='com_author' value='$(name)'/><postfield name='com_data' value='$(comment)'/><postfield name='persistent' value=''/><postfield name='send_notification' value=''/><postfield name='cmd_typ' value='%d'/><postfield name='cmd_mod' value='%d'/><postfield name='content' value='wml'/></go>\n",cgiutils_h.COMMAND_CGI,host_name,service_desc,common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM,cgiutils_h.CMDMODE_COMMIT);
	System.out.printf("</do>\n");

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");

	return;
        }


/* displays ping results */
public static void display_ping(){
	String input_buffer ; // MAX_INPUT_BUFFER
	String buffer ; // MAX_INPUT_BUFFER
//	String temp_ptr;
//	FILE *fp;
	int odd=0;
	int in_macro=common_h.FALSE;

	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Ping'>\n");

	if( ping_address.equals("")){

		System.out.printf("<p align='center' mode='nowrap'>\n");
		System.out.printf("<b>Ping Host</b><br/>\n");
		System.out.printf("</p>\n");

		System.out.printf("<p align='center' mode='wrap'>\n");
		System.out.printf("<b>Host Name/Address:</b><br/>\n");
		System.out.printf("<input name='address'/>\n");
		System.out.printf("<do type='accept'>\n");
		System.out.printf("<go href='%s'><postfield name='ping' value='$(address)'/></go>\n",cgiutils_h.STATUSWML_CGI);
		System.out.printf("</do>\n");
		System.out.printf("</p>\n");
	        }

	else{

		System.out.printf("<p align='center' mode='nowrap'>\n");
		System.out.printf("<b>Results For Ping Of %s:</b><br/>\n",ping_address);
		System.out.printf("</p>\n");

		System.out.printf("<p mode='nowrap'>\n");

		if(cgiutils.ping_syntax==null)
			System.out.printf("ping_syntax in CGI config file is null!\n");
	
		else{

			/* process macros in the ping syntax */
			buffer = "";
			input_buffer = cgiutils.ping_syntax ;
            String[] split = input_buffer.split( "[$]" );
            for ( String temp_ptr : split ) {

			    if(in_macro==common_h.FALSE){
			        buffer += temp_ptr ;
			        in_macro=common_h.TRUE;
			    }
			    else{
			        if( temp_ptr.equals("HOSTADDRESS"))
			            buffer = ping_address;
			        in_macro=common_h.FALSE;
			    }
			}

			try {
			    Process process = Runtime.getRuntime().exec( buffer );
			    BufferedReader input = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			    
			    for( buffer = input.readLine(); buffer != null; buffer = input.readLine() ) {
			        if( odd != 0 ) {
			            odd = 0;
			            System.out.printf("%s<br/>\n",buffer);
			        } else {
			            odd = 1;
			            System.out.printf("<b>%s</b><br/>\n",buffer);
			        }
			    }
			    
			} catch( Throwable e ) {
			    System.out.printf("Error executing traceroute!\n");
			}
		}

		System.out.printf("</p>\n");
	        }

	System.out.printf("</card>\n");

	return;
        }


/* displays traceroute results */
public static void display_traceroute(){
	String buffer ; // MAX_INPUT_BUFFER
//	FILE *fp;
	int odd=0;

	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='Traceroute'>\n");

	if( traceroute_address.equals("")){

		System.out.printf("<p align='center' mode='nowrap'>\n");
		System.out.printf("<b>Traceroute</b><br/>\n");
		System.out.printf("</p>\n");

		System.out.printf("<p align='center' mode='wrap'>\n");
		System.out.printf("<b>Host Name/Address:</b><br/>\n");
		System.out.printf("<input name='address'/>\n");
		System.out.printf("<do type='accept'>\n");
		System.out.printf("<go href='%s'><postfield name='traceroute' value='$(address)'/></go>\n",cgiutils_h.STATUSWML_CGI);
		System.out.printf("</do>\n");
		System.out.printf("</p>\n");
	        }

	else{

		System.out.printf("<p align='center' mode='nowrap'>\n");
		System.out.printf("<b>Results For Traceroute To %s:</b><br/>\n",traceroute_address);
		System.out.printf("</p>\n");

		System.out.printf("<p mode='nowrap'>\n");
	
		buffer = String.format( "%s %s", config_h.TRACEROUTE_COMMAND,traceroute_address);

		try {
		    Process process = Runtime.getRuntime().exec( buffer );
		    BufferedReader input = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
		    
		    for( buffer = input.readLine(); buffer != null; buffer = input.readLine() ) {
		        if( odd != 0 ) {
		            odd = 0;
		            System.out.printf("%s<br/>\n",buffer);
		        } else {
		            odd = 1;
		            System.out.printf("<b>%s</b><br/>\n",buffer);
		        }
		    }
		    
		} catch( Throwable e ) {
		    System.out.printf("Error executing traceroute!\n");
		}

		System.out.printf("</p>\n");
	        }

	System.out.printf("</card>\n");

	return;
        }



/* displays problems */
public static void display_problems(){
	objects_h.host temp_host;
    objects_h.service temp_service;
//	statusdata_h.hoststatus temp_hoststatus;
	int total_host_problems=0;
//	statusdata_h.servicestatus temp_servicestatus;
	int total_service_problems=0;

	/**** MAIN SCREEN (CARD 1) ****/
	System.out.printf("<card id='card1' title='%s Problems'>\n",(display_type==DISPLAY_ALL_PROBLEMS)?"All":"Unhandled");
	System.out.printf("<p align='center' mode='nowrap'>\n");
	System.out.printf("<b>%s Problems</b><br/><br/>\n",(display_type==DISPLAY_ALL_PROBLEMS)?"All":"Unhandled");

	System.out.printf("<b>Host Problems:</b>\n");

	System.out.printf("<table columns='2' align='LL'>\n");

	/* check all hosts */
	for( statusdata_h.hoststatus temp_hoststatus : (ArrayList<statusdata_h.hoststatus>) statusdata.hoststatus_list ){

		temp_host=objects.find_host(temp_hoststatus.host_name);
		if(temp_host==null)
			continue;

		if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			continue;

		if(temp_hoststatus.status==statusdata_h.HOST_UP || temp_hoststatus.status==statusdata_h.HOST_PENDING)
			continue;

		if(display_type==DISPLAY_UNHANDLED_PROBLEMS){
			if(temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE)
				continue;
			if(temp_hoststatus.checks_enabled==common_h.FALSE)
				continue;
			if(temp_hoststatus.notifications_enabled==common_h.FALSE)
				continue;
			if(temp_hoststatus.scheduled_downtime_depth>0)
				continue;
		        }

		total_host_problems++;

		System.out.printf("<tr><td><anchor title='%s'>",temp_host.name);
		if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			System.out.printf("DWN");
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			System.out.printf("UNR");
		else
			System.out.printf("???");
		System.out.printf("<go href='%s' method='post'><postfield name='host' value='%s'/></go></anchor></td>",cgiutils_h.STATUSWML_CGI,temp_host.name);
		System.out.printf("<td>%s</td></tr>\n",temp_host.name);
	        }

	if(total_host_problems==0)
		System.out.printf("<tr><td>No problems</td></tr>\n");

	System.out.printf("</table>\n");

	System.out.printf("<br/>\n");


	System.out.printf("<b>Svc Problems:</b>\n");

	System.out.printf("<table columns='2' align='LL'>\n");

	/* check all services */
	for(statusdata_h.servicestatus temp_servicestatus : (ArrayList<statusdata_h.servicestatus>) statusdata.servicestatus_list ){
		
		temp_service=objects.find_service(temp_servicestatus.host_name,temp_servicestatus.description);
		if(temp_service==null)
			continue;

		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			continue;

		if(temp_servicestatus.status==statusdata_h.SERVICE_OK || temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
			continue;

		if(display_type==DISPLAY_UNHANDLED_PROBLEMS){
			if(temp_servicestatus.problem_has_been_acknowledged==common_h.TRUE)
				continue;
			if(temp_servicestatus.checks_enabled==common_h.FALSE)
				continue;
			if(temp_servicestatus.notifications_enabled==common_h.FALSE)
				continue;
			if(temp_servicestatus.scheduled_downtime_depth>0)
				continue;
		        }

		total_service_problems++;

		System.out.printf("<tr><td><anchor title='%s'>",temp_servicestatus.description);
		if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
			System.out.printf("CRI");
		else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
			System.out.printf("WRN");
		else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
			System.out.printf("UNK");
		else
			System.out.printf("???");
		System.out.printf("<go href='%s' method='post'><postfield name='host' value='%s'/><postfield name='service' value='%s'/></go></anchor></td>",cgiutils_h.STATUSWML_CGI,temp_service.host_name,temp_service.description);
		System.out.printf("<td>%s/%s</td></tr>\n",temp_service.host_name,temp_service.description);
	        }

	if(total_service_problems==0)
		System.out.printf("<tr><td>No problems</td></tr>\n");

	System.out.printf("</table>\n");

	System.out.printf("</p>\n");

	System.out.printf("</card>\n");

	return;
        }



}