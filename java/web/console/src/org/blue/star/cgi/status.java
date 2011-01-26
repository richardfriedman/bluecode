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
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.comments;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class status extends blue_servlet {
    
public static final int MAX_MESSAGE_BUFFER		= 4096;

public static final int DISPLAY_HOSTS			= 0;
public static final int DISPLAY_HOSTGROUPS		= 1;
public static final int DISPLAY_SERVICEGROUPS           = 2;

public static final int STYLE_OVERVIEW			= 0;
public static final int STYLE_DETAIL			= 1;
public static final int STYLE_SUMMARY			= 2;
public static final int STYLE_GRID                      = 3;
public static final int STYLE_HOST_DETAIL               = 4;

public static cgiauth_h.authdata current_authdata = new cgiauth_h.authdata();
public static long current_time;

public static String alert_message = "";
public static String host_name=null;
public static String hostgroup_name=null;
public static String servicegroup_name=null;
public static String service_filter=null;
public static int host_alert=common_h.FALSE;
public static int show_all_hosts=common_h.TRUE;
public static int show_all_hostgroups=common_h.TRUE;
public static int show_all_servicegroups=common_h.TRUE;
public static int display_type=DISPLAY_HOSTS;
public static int overview_columns=3;
public static int max_grid_width=8;
public static int group_style_type=STYLE_OVERVIEW;
public static int navbar_search=common_h.FALSE;

public static int service_status_types=statusdata_h.SERVICE_PENDING|statusdata_h.SERVICE_OK|statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL;
public static int all_service_status_types=statusdata_h.SERVICE_PENDING|statusdata_h.SERVICE_OK|statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL;

public static int host_status_types=statusdata_h.HOST_PENDING|statusdata_h.HOST_UP|statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE;
public static int all_host_status_types=statusdata_h.HOST_PENDING|statusdata_h.HOST_UP|statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE;

public static int all_service_problems=statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL;
public static int all_host_problems=statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE;

public static long host_properties=0L;
public static long service_properties=0L;

public static int sort_type=cgiutils_h.SORT_NONE;
public static int sort_option=cgiutils_h.SORT_HOSTNAME;

public static int problem_hosts_down=0;
public static int problem_hosts_unreachable=0;
public static int problem_services_critical=0;
public static int problem_services_warning=0;
public static int problem_services_unknown=0;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;

public void reset_context() {
   current_authdata = new cgiauth_h.authdata();
   current_time = 0;

   alert_message = "";
   host_name=null;
   hostgroup_name=null;
   servicegroup_name=null;
   service_filter=null;
   host_alert=common_h.FALSE;
   show_all_hosts=common_h.TRUE;
   show_all_hostgroups=common_h.TRUE;
   show_all_servicegroups=common_h.TRUE;
   display_type=DISPLAY_HOSTS;
   overview_columns=3;
   max_grid_width=8;
   group_style_type=STYLE_OVERVIEW;
   navbar_search=common_h.FALSE;

   service_status_types=statusdata_h.SERVICE_PENDING|statusdata_h.SERVICE_OK|statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL;
   all_service_status_types=statusdata_h.SERVICE_PENDING|statusdata_h.SERVICE_OK|statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL;

   host_status_types=statusdata_h.HOST_PENDING|statusdata_h.HOST_UP|statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE;
   all_host_status_types=statusdata_h.HOST_PENDING|statusdata_h.HOST_UP|statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE;

   all_service_problems=statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL;
   all_host_problems=statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE;

   host_properties=0L;
   service_properties=0L;

   sort_type=cgiutils_h.SORT_NONE;
   sort_option=cgiutils_h.SORT_HOSTNAME;

   problem_hosts_down=0;
   problem_hosts_unreachable=0;
   problem_services_critical=0;
   problem_services_warning=0;
   problem_services_unknown=0;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;
}

public void call_main() {
   main( null );
}
public static void main(String[] args){
	int result=common_h.OK;
	String sound=null;
	objects_h.host temp_host=null;
	objects_h.hostgroup temp_hostgroup=null;
	objects_h.servicegroup temp_servicegroup=null;
	
	current_time = utils.currentTimeInSeconds();

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

	/* read all status data */
	result=cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(),statusdata_h.READ_ALL_STATUS_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		cgiutils.status_data_error();
		document_footer();
        cgiutils.exit(  common_h.ERROR );
        return;
                }

	document_header(common_h.TRUE);

	/* read in all host and service comments */
	comments.read_comment_data( cgiutils.get_cgi_config_location());

	/* get authentication information */
	cgiauth.get_authentication_information(/* TODO & */ current_authdata);

	/* if a navbar search was performed, find the host by name, address or partial name */
	if(navbar_search==common_h.TRUE){
		if((temp_host=objects.find_host(host_name))==null){
			for ( objects_h.host iter_host : (ArrayList<objects_h.host>)  objects.host_list ) {
				if(cgiauth.is_authorized_for_host(iter_host,/* TODO & */ current_authdata)==common_h.FALSE)
					continue;
				if(host_name.equals(iter_host.address)){
					temp_host = iter_host;
					host_name=iter_host.name;
					break;
			                }
		                }
			if(temp_host==null){
				for ( objects_h.host iter_host : (ArrayList<objects_h.host>)  objects.host_list ) {
					if(cgiauth.is_authorized_for_host(iter_host,/* TODO & */ current_authdata)==common_h.FALSE)
						continue;
					if((iter_host.name.startsWith(host_name)) || iter_host.name.equalsIgnoreCase(host_name)){
						temp_host=iter_host;
						host_name=iter_host.name;
						break;
			                        }
		                        }
			        }
			}
			/* last effort, search hostgroups then servicegroups */
			if(temp_host==null){
				if((temp_hostgroup=objects.find_hostgroup(host_name))!=null){
					display_type=DISPLAY_HOSTGROUPS;
					show_all_hostgroups=common_h.FALSE;
					host_name = null;
					hostgroup_name=temp_hostgroup.group_name;
					}
				else if((temp_servicegroup=objects.find_servicegroup(host_name))!=null){
					display_type=DISPLAY_SERVICEGROUPS;
					show_all_servicegroups=common_h.FALSE;
                    host_name = null;
					servicegroup_name=temp_servicegroup.group_name;
				}
			}
	        }

	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%% cellspacing=0 cellpadding=0>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top width=33%%>\n");

		/* info table */
        cgiutils.display_info_table("Current Network Status",common_h.TRUE,/* TODO & */ current_authdata);

		System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
		System.out.printf("<TR><TD CLASS='linkBox'>\n");

		if(display_type==DISPLAY_HOSTS){
			System.out.printf("<a href='%s?host=%s'>View History For %s</a><br>\n",cgiutils_h.HISTORY_CGI,(show_all_hosts==common_h.TRUE)?"all":cgiutils.url_encode(host_name),(show_all_hosts==common_h.TRUE)?"all hosts":"This Host");
			System.out.printf("<a href='%s?host=%s'>View Notifications For %s</a>\n",cgiutils_h.NOTIFICATIONS_CGI,(show_all_hosts==common_h.TRUE)?"all":cgiutils.url_encode(host_name),(show_all_hosts==common_h.TRUE)?"All Hosts":"This Host");
			if(show_all_hosts==common_h.FALSE)
				System.out.printf("<br><a href='%s?host=all'>View Service Status Detail For All Hosts</a>\n",cgiutils_h.STATUS_CGI);
			else
				System.out.printf("<br><a href='%s?hostgroup=all&style=hostdetail'>View Host Status Detail For All Hosts</a>\n",cgiutils_h.STATUS_CGI);
	                }
		else if(display_type==DISPLAY_SERVICEGROUPS){
			if(show_all_servicegroups==common_h.FALSE){

				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_GRID || group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?servicegroup=%s&style=detail'>View Service Status Detail For This Service Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_GRID || group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?servicegroup=%s&style=overview'>View Status Overview For This Service Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_GRID)
					System.out.printf("<a href='%s?servicegroup=%s&style=summary'>View Status Summary For This Service Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?servicegroup=%s&style=grid'>View Service Status Grid For This Service Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));

				if(group_style_type==STYLE_DETAIL)
					System.out.printf("<a href='%s?servicegroup=all&style=detail'>View Service Status Detail For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_OVERVIEW) /* UPDATED 2.2 */
					System.out.printf("<a href='%s?servicegroup=all&style=overview'>View Status Overview For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_SUMMARY) /* UPDATED 2.2 */
					System.out.printf("<a href='%s?servicegroup=all&style=summary'>View Status Summary For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_GRID)
					System.out.printf("<a href='%s?servicegroup=all&style=grid'>View Service Status Grid For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);

			        }
			else{
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_GRID || group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?servicegroup=all&style=detail'>View Service Status Detail For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_GRID || group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?servicegroup=all&style=overview'>View Status Overview For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_GRID)
					System.out.printf("<a href='%s?servicegroup=all&style=summary'>View Status Summary For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?servicegroup=all&style=grid'>View Service Status Grid For All Service Groups</a><br>\n",cgiutils_h.STATUS_CGI);
			        }
		
		        }
		else{
			if(show_all_hostgroups==common_h.FALSE){

				if(group_style_type==STYLE_DETAIL)
					System.out.printf("<a href='%s?hostgroup=all&style=detail'>View Service Status Detail For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=all&style=hostdetail'>View Host Status Detail For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_OVERVIEW)
					System.out.printf("<a href='%s?hostgroup=all&style=overview'>View Status Overview For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_SUMMARY)
					System.out.printf("<a href='%s?hostgroup=all&style=summary'>View Status Summary For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_GRID)
					System.out.printf("<a href='%s?hostgroup=all&style=grid'>View Status Grid For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);

				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_GRID || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=%s&style=detail'>View Service Status Detail For This Host Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_DETAIL || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_GRID)
					System.out.printf("<a href='%s?hostgroup=%s&style=hostdetail'>View Host Status Detail For This Host Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_GRID || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=%s&style=overview'>View Status Overview For This Host Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_DETAIL || group_style_type==STYLE_GRID || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=%s&style=summary'>View Status Summary For This Host Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_DETAIL || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=%s&style=grid'>View Status Grid For This Host Group</a><br>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
		                }
			else{
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_GRID || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=all&style=detail'>View Service Status Detail For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_DETAIL || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_GRID)
					System.out.printf("<a href='%s?hostgroup=all&style=hostdetail'>View Host Status Detail For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_DETAIL || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_GRID || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=all&style=overview'>View Status Overview For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_DETAIL || group_style_type==STYLE_GRID || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=all&style=summary'>View Status Summary For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
				if(group_style_type==STYLE_OVERVIEW || group_style_type==STYLE_DETAIL || group_style_type==STYLE_SUMMARY || group_style_type==STYLE_HOST_DETAIL)
					System.out.printf("<a href='%s?hostgroup=all&style=grid'>View Status Grid For All Host Groups</a><br>\n",cgiutils_h.STATUS_CGI);
		                }
	                }

		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</td>\n");

		/* middle column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");
		show_host_status_totals();
		System.out.printf("</td>\n");

		/* right hand column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");
		show_service_status_totals();
		System.out.printf("</td>\n");

		/* display context-sensitive help */
		System.out.printf("<td align=right valign=bottom>\n");
		if(display_type==DISPLAY_HOSTS)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_DETAIL);
		else if(display_type==DISPLAY_SERVICEGROUPS){
			if(group_style_type==STYLE_HOST_DETAIL)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_DETAIL);
			else if(group_style_type==STYLE_OVERVIEW)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_SGOVERVIEW);
			else if(group_style_type==STYLE_SUMMARY)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_SGSUMMARY);
			else if(group_style_type==STYLE_GRID)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_SGGRID);
		        }
		else{
			if(group_style_type==STYLE_HOST_DETAIL)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_HOST_DETAIL);
			else if(group_style_type==STYLE_OVERVIEW)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_HGOVERVIEW);
			else if(group_style_type==STYLE_SUMMARY)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_HGSUMMARY);
			else if(group_style_type==STYLE_GRID)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_STATUS_HGGRID);
		        }
		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");
	        }


	/* embed sound tag if necessary... */
	if(problem_hosts_unreachable>0 && cgiutils.host_unreachable_sound!=null)
		sound=cgiutils.host_unreachable_sound;
	else if(problem_hosts_down>0 && cgiutils.host_down_sound!=null)
		sound=cgiutils.host_down_sound;
	else if(problem_services_critical>0 && cgiutils.service_critical_sound!=null)
		sound=cgiutils.service_critical_sound;
	else if(problem_services_warning>0 && cgiutils.service_warning_sound!=null)
		sound=cgiutils.service_warning_sound;
	else if(problem_services_unknown>0 && cgiutils.service_unknown_sound!=null)
		sound=cgiutils.service_unknown_sound;
	else if(problem_services_unknown==0 && problem_services_warning==0 && problem_services_critical==0 && problem_hosts_down==0 && problem_hosts_unreachable==0 && cgiutils.normal_sound!=null)
		sound=cgiutils.normal_sound;
    if(sound!=null){
        System.out.printf("<object type=\"application/x-mplayer2\" data=\"%s%s\" height=\"-\" width=\"0\">",cgiutils.url_media_path,sound);
        System.out.printf("<param name=\"filename\" value=\"%s%s\">",cgiutils.url_media_path,sound);
        System.out.printf("<param name=\"autostart\" value=\"1\">");
        System.out.printf("<param name=\"playcount\" value=\"1\">");
        System.out.printf("</object>");
        }

	/* bottom portion of screen - service or hostgroup detail */
	if(display_type==DISPLAY_HOSTS)
		show_service_detail();
	else if(display_type==DISPLAY_SERVICEGROUPS){
		if(group_style_type==STYLE_OVERVIEW)
			show_servicegroup_overviews();
		else if(group_style_type==STYLE_SUMMARY)
			show_servicegroup_summaries();
		else if(group_style_type==STYLE_GRID)
			show_servicegroup_grids();
		else
			show_service_detail();
	        }
	else{
		if(group_style_type==STYLE_OVERVIEW)
			show_hostgroup_overviews();
		else if(group_style_type==STYLE_SUMMARY)
			show_hostgroup_summaries();
		else if(group_style_type==STYLE_GRID)
			show_hostgroup_grids();
		else if(group_style_type==STYLE_HOST_DETAIL)
			show_host_detail();
		else
			show_service_detail();
	        }

	document_footer();

    cgiutils.exit(  common_h.OK );
        }


public static void document_header(int use_stylesheet){
	String date_time;

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setIntHeader( "Refresh", cgiutils.refresh_rate );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );
       response.setContentType("text/html");
    } else {
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      	System.out.printf("Refresh: %d\r\n", cgiutils.refresh_rate);
      
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
	System.out.printf("Current Network Status\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>",cgiutils.url_stylesheets_path,cgiutils_h.STATUS_CSS);
	        }

	System.out.printf("</head>\n");

	System.out.printf("<body CLASS='status'>\n");

	/* include user SSI header */
	cgiutils.include_ssi_files(cgiutils_h.STATUS_CGI,cgiutils_h.SSI_HEADER);

	return;
        }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
	cgiutils.include_ssi_files(cgiutils_h.STATUS_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars( request_string );

	for(x=0;x<variables.length;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if ( variables[x] != null && variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the navbar search argument */
		else if(variables[x].equals("navbarsearch")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			navbar_search=common_h.TRUE;
		        }

		/* we found the hostgroup argument */
		else if(variables[x].equals("hostgroup")){
			display_type=DISPLAY_HOSTGROUPS;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			hostgroup_name=variables[x];

			if(hostgroup_name!=null && hostgroup_name.equals("all"))
				show_all_hostgroups=common_h.TRUE;
			else
				show_all_hostgroups=common_h.FALSE;
		        }

		/* we found the servicegroup argument */
		else if(variables[x].equals("servicegroup")){
			display_type=DISPLAY_SERVICEGROUPS;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			servicegroup_name=variables[x];

			if(servicegroup_name!=null && servicegroup_name.equals("all"))
				show_all_servicegroups=common_h.TRUE;
			else
				show_all_servicegroups=common_h.FALSE;
		        }

		/* we found the host argument */
		else if(variables[x].equals("host")){
			display_type=DISPLAY_HOSTS;
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_name=variables[x];

			if(host_name!=null && host_name.equals("all"))
				show_all_hosts=common_h.TRUE;
			else
				show_all_hosts=common_h.FALSE;
		        }

		/* we found the columns argument */
		else if(variables[x].equals("columns")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			overview_columns=atoi(variables[x]);
			if(overview_columns<=0)
				overview_columns=1;
		        }

		/* we found the service status type argument */
		else if(variables[x].equals("servicestatustypes")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			service_status_types=atoi(variables[x]);
		        }

		/* we found the host status type argument */
		else if(variables[x].equals("hoststatustypes")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_status_types=atoi(variables[x]);
		        }

		/* we found the service properties argument */
		else if(variables[x].equals("serviceprops")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			service_properties=strtoul(variables[x],null,10);
		        }

		/* we found the host properties argument */
		else if(variables[x].equals("hostprops")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_properties=strtoul(variables[x],null,10);
		        }

		/* we found the host or service group style argument */
		else if(variables[x].equals("style")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("overview"))
				group_style_type=STYLE_OVERVIEW;
			else if(variables[x].equals("detail"))
				group_style_type=STYLE_DETAIL;
			else if(variables[x].equals("summary"))
				group_style_type=STYLE_SUMMARY;
			else if(variables[x].equals("grid"))
				group_style_type=STYLE_GRID;
			else if(variables[x].equals("hostdetail"))
				group_style_type=STYLE_HOST_DETAIL;
			else
				group_style_type=STYLE_DETAIL;
		        }

		/* we found the sort type argument */
		else if(variables[x].equals("sorttype")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			sort_type=atoi(variables[x]);
		        }

		/* we found the sort option argument */
		else if(variables[x].equals("sortoption")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			sort_option=atoi(variables[x]);
		        }

		/* we found the embed option */
		else if(variables[x].equals("embedded"))
			embedded=common_h.TRUE;

		/* we found the noheader option */
		else if(variables[x].equals("noheader"))
			display_header=common_h.FALSE;

		/* servicefilter cgi var */
                else if(variables[x].equals("servicefilter")){
                        x++;
                        if(variables[x]==null){
                                error=common_h.TRUE;
                                break;
                                }
                        service_filter=variables[x];
                        }
	        }

	/* free memory allocated to the CGI variables */
//	free_cgivars(variables);

	return error;
        }



/* display table with service status totals... */
public static void show_service_status_totals(){
	int total_ok=0;
	int total_warning=0;
	int total_unknown=0;
	int total_critical=0;
	int total_pending=0;
	int total_services=0;
	int total_problems=0;
	objects_h.service temp_service;
	objects_h.host temp_host;
	int count_service;

	/* check the status of all services... */
	for ( statusdata_h.servicestatus temp_servicestatus : (ArrayList<statusdata_h.servicestatus>)  statusdata.servicestatus_list ) {

		/* find the host and service... */
		temp_host=objects.find_host(temp_servicestatus.host_name);
		temp_service=objects.find_service(temp_servicestatus.host_name,temp_servicestatus.description);

		/* make sure user has rights to see this service... */
		if(cgiauth.is_authorized_for_service(temp_service,/* TODO & */ current_authdata)==common_h.FALSE)
			continue;

		count_service=0;

		if(display_type==DISPLAY_HOSTS && (show_all_hosts==common_h.TRUE || host_name.equals(temp_servicestatus.host_name)))
			count_service=1;
		else if(display_type==DISPLAY_SERVICEGROUPS && (show_all_servicegroups==common_h.TRUE || (objects.is_service_member_of_servicegroup(objects.find_servicegroup(servicegroup_name),temp_service)==common_h.TRUE)))
			count_service=1;
		else if(display_type==DISPLAY_HOSTGROUPS && (show_all_hostgroups==common_h.TRUE || (objects.is_host_member_of_hostgroup(objects.find_hostgroup(hostgroup_name),temp_host)==common_h.TRUE)))
			count_service=1;

		if(count_service>0){

			if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL){
				total_critical++;
				if(temp_servicestatus.problem_has_been_acknowledged==common_h.FALSE && temp_servicestatus.checks_enabled==common_h.TRUE && temp_servicestatus.notifications_enabled==common_h.TRUE && temp_servicestatus.scheduled_downtime_depth==0)
					problem_services_critical++;
			        }
			else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING){
				total_warning++;
				if(temp_servicestatus.problem_has_been_acknowledged==common_h.FALSE && temp_servicestatus.checks_enabled==common_h.TRUE && temp_servicestatus.notifications_enabled==common_h.TRUE && temp_servicestatus.scheduled_downtime_depth==0)
					problem_services_warning++;
			        }
			else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN){
				total_unknown++;
				if(temp_servicestatus.problem_has_been_acknowledged==common_h.FALSE && temp_servicestatus.checks_enabled==common_h.TRUE && temp_servicestatus.notifications_enabled==common_h.TRUE && temp_servicestatus.scheduled_downtime_depth==0)
					problem_services_unknown++;
			        }
			else if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
				total_ok++;
			else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
				total_pending++;
			else
				total_ok++;
		        }
	        }

	total_services=total_ok+total_unknown+total_warning+total_critical+total_pending;
	total_problems=total_unknown+total_warning+total_critical;

	System.out.printf("<DIV CLASS='serviceTotals'>Service Status Totals</DIV>\n");

	System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD>\n");

	System.out.printf("<TABLE BORDER=1 CLASS='serviceTotals'>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&servicestatustypes=%d",statusdata_h.SERVICE_OK);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("Ok</A></TH>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&servicestatustypes=%d",statusdata_h.SERVICE_WARNING);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("Warning</A></TH>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&servicestatustypes=%d",statusdata_h.SERVICE_UNKNOWN);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("Unknown</A></TH>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&servicestatustypes=%d",statusdata_h.SERVICE_CRITICAL);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("Critical</A></TH>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&servicestatustypes=%d",statusdata_h.SERVICE_PENDING);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("Pending</A></TH>\n");

	System.out.printf("</TR>\n");

	System.out.printf("<TR>\n");


	/* total services ok */
	System.out.printf("<TD CLASS='serviceTotals%s'>%d</TD>\n",(total_ok>0)?"OK":"",total_ok);

	/* total services in warning state */
	System.out.printf("<TD CLASS='serviceTotals%s'>%d</TD>\n",(total_warning>0)?"WARNING":"",total_warning);

	/* total services in unknown state */
	System.out.printf("<TD CLASS='serviceTotals%s'>%d</TD>\n",(total_unknown>0)?"UNKNOWN":"",total_unknown);

	/* total services in critical state */
	System.out.printf("<TD CLASS='serviceTotals%s'>%d</TD>\n",(total_critical>0)?"CRITICAL":"",total_critical);

	/* total services in pending state */
	System.out.printf("<TD CLASS='serviceTotals%s'>%d</TD>\n",(total_pending>0)?"PENDING":"",total_pending);


	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</TD></TR><TR><TD ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=1 CLASS='serviceTotals'>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&servicestatustypes=%d",statusdata_h.SERVICE_UNKNOWN|statusdata_h.SERVICE_WARNING|statusdata_h.SERVICE_CRITICAL);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("<I>All Problems</I></A></TH>\n");

	System.out.printf("<TH CLASS='serviceTotals'>");
	System.out.printf("<A CLASS='serviceTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s&style=detail",servicegroup_name);
	else
		System.out.printf("hostgroup=%s&style=detail",hostgroup_name);
	System.out.printf("&hoststatustypes=%d'>",host_status_types);
	System.out.printf("<I>All Types</I></A></TH>\n");


	System.out.printf("</TR><TR>\n");

	/* total service problems */
	System.out.printf("<TD CLASS='serviceTotals%s'>%d</TD>\n",(total_problems>0)?"PROBLEMS":"",total_problems);

	/* total services */
	System.out.printf("<TD CLASS='serviceTotals'>%d</TD>\n",total_services);

	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</DIV>\n");

	return;
        }


/* display a table with host status totals... */
public static void show_host_status_totals(){
	int total_up=0;
	int total_down=0;
	int total_unreachable=0;
	int total_pending=0;
	int total_hosts=0;
	int total_problems=0;
//	statusdata_h.hoststatus temp_hoststatus;
	objects_h.host temp_host;
//	statusdata_h.servicestatus temp_servicestatus;
	objects_h.service temp_service;
	int count_host;


	/* check the status of all hosts... */
	for ( statusdata_h.hoststatus temp_hoststatus : (ArrayList<statusdata_h.hoststatus>)  statusdata.hoststatus_list ) {

		/* find the host... */
		temp_host=objects.find_host(temp_hoststatus.host_name);

		/* make sure user has rights to view this host */
		if(cgiauth.is_authorized_for_host(temp_host,/* TODO & */ current_authdata)==common_h.FALSE)
			continue;

		count_host=0;

		if(display_type==DISPLAY_HOSTS && (show_all_hosts==common_h.TRUE || host_name.equals(temp_hoststatus.host_name)))
			count_host=1;
		else if(display_type==DISPLAY_SERVICEGROUPS){
			if(show_all_servicegroups==common_h.TRUE)
				count_host=1;
			else{
				for ( statusdata_h.servicestatus temp_servicestatus : (ArrayList<statusdata_h.servicestatus>)  statusdata.servicestatus_list ) {
					if(!temp_servicestatus.host_name.equals(temp_hoststatus.host_name))
						continue;
					temp_service=objects.find_service(temp_servicestatus.host_name,temp_servicestatus.description);
					if(cgiauth.is_authorized_for_service(temp_service,/* TODO & */ current_authdata)==common_h.FALSE)
						continue;
					count_host=1;
					break;
				        }
			        }
		        }
		else if(display_type==DISPLAY_HOSTGROUPS && (show_all_hostgroups==common_h.TRUE || (objects.is_host_member_of_hostgroup(objects.find_hostgroup(hostgroup_name),temp_host)==common_h.TRUE)))
			count_host=1;

		if(count_host>0){

			if(temp_hoststatus.status==statusdata_h.HOST_UP)
				total_up++;
			else if(temp_hoststatus.status==statusdata_h.HOST_DOWN){
				total_down++;
				if(temp_hoststatus.problem_has_been_acknowledged==common_h.FALSE && temp_hoststatus.notifications_enabled==common_h.TRUE && temp_hoststatus.checks_enabled==common_h.TRUE && temp_hoststatus.scheduled_downtime_depth==0)
					problem_hosts_down++;
			        }
			else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE){
				total_unreachable++;
				if(temp_hoststatus.problem_has_been_acknowledged==common_h.FALSE && temp_hoststatus.notifications_enabled==common_h.TRUE && temp_hoststatus.checks_enabled==common_h.TRUE && temp_hoststatus.scheduled_downtime_depth==0)
					problem_hosts_unreachable++;
			        }

			else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
				total_pending++;
			else
				total_up++;
		        }
	        }

	total_hosts=total_up+total_down+total_unreachable+total_pending;
	total_problems=total_down+total_unreachable;

	System.out.printf("<DIV CLASS='hostTotals'>Host Status Totals</DIV>\n");

	System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD>\n");


	System.out.printf("<TABLE BORDER=1 CLASS='hostTotals'>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TH CLASS='hostTotals'>");
	System.out.printf("<A CLASS='hostTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s",servicegroup_name);
	else{
		System.out.printf("hostgroup=%s",hostgroup_name);
		if((service_status_types!=all_service_status_types) || group_style_type==STYLE_DETAIL)
			System.out.printf("&style=detail");
		else if(group_style_type==STYLE_HOST_DETAIL)
			System.out.printf("&style=hostdetail");
	        }
	if(service_status_types!=all_service_status_types)
		System.out.printf("&servicestatustypes=%d",service_status_types);
	System.out.printf("&hoststatustypes=%d'>",statusdata_h.HOST_UP);
	System.out.printf("Up</A></TH>\n");

	System.out.printf("<TH CLASS='hostTotals'>");
	System.out.printf("<A CLASS='hostTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s",servicegroup_name);
	else{
		System.out.printf("hostgroup=%s",hostgroup_name);
		if((service_status_types!=all_service_status_types) || group_style_type==STYLE_DETAIL)
			System.out.printf("&style=detail");
		else if(group_style_type==STYLE_HOST_DETAIL)
			System.out.printf("&style=hostdetail");
	        }
	if(service_status_types!=all_service_status_types)
		System.out.printf("&servicestatustypes=%d",service_status_types);
	System.out.printf("&hoststatustypes=%d'>",statusdata_h.HOST_DOWN);
	System.out.printf("Down</A></TH>\n");

	System.out.printf("<TH CLASS='hostTotals'>");
	System.out.printf("<A CLASS='hostTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s",servicegroup_name);
	else{
		System.out.printf("hostgroup=%s",hostgroup_name);
		if((service_status_types!=all_service_status_types) || group_style_type==STYLE_DETAIL)
			System.out.printf("&style=detail");
		else if(group_style_type==STYLE_HOST_DETAIL)
			System.out.printf("&style=hostdetail");
	        }
	if(service_status_types!=all_service_status_types)
		System.out.printf("&servicestatustypes=%d",service_status_types);
	System.out.printf("&hoststatustypes=%d'>",statusdata_h.HOST_UNREACHABLE);
	System.out.printf("Unreachable</A></TH>\n");

	System.out.printf("<TH CLASS='hostTotals'>");
	System.out.printf("<A CLASS='hostTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s",servicegroup_name);
	else{
		System.out.printf("hostgroup=%s",hostgroup_name);
		if((service_status_types!=all_service_status_types) || group_style_type==STYLE_DETAIL)
			System.out.printf("&style=detail");
		else if(group_style_type==STYLE_HOST_DETAIL)
			System.out.printf("&style=hostdetail");
	        }
	if(service_status_types!=all_service_status_types)
		System.out.printf("&servicestatustypes=%d",service_status_types);
	System.out.printf("&hoststatustypes=%d'>",statusdata_h.HOST_PENDING);
	System.out.printf("Pending</A></TH>\n");

	System.out.printf("</TR>\n");


	System.out.printf("<TR>\n");

	/* total hosts up */
	System.out.printf("<TD CLASS='hostTotals%s'>%d</TD>\n",(total_up>0)?"UP":"",total_up);

	/* total hosts down */
	System.out.printf("<TD CLASS='hostTotals%s'>%d</TD>\n",(total_down>0)?"DOWN":"",total_down);

	/* total hosts unreachable */
	System.out.printf("<TD CLASS='hostTotals%s'>%d</TD>\n",(total_unreachable>0)?"UNREACHABLE":"",total_unreachable);

	/* total hosts pending */
	System.out.printf("<TD CLASS='hostTotals%s'>%d</TD>\n",(total_pending>0)?"PENDING":"",total_pending);

	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</TD></TR><TR><TD ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=1 CLASS='hostTotals'>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TH CLASS='hostTotals'>");
	System.out.printf("<A CLASS='hostTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s",servicegroup_name);
	else{
		System.out.printf("hostgroup=%s",hostgroup_name);
		if((service_status_types!=all_service_status_types) || group_style_type==STYLE_DETAIL)
			System.out.printf("&style=detail");
		else if(group_style_type==STYLE_HOST_DETAIL)
			System.out.printf("&style=hostdetail");
	        }
	if(service_status_types!=all_service_status_types)
		System.out.printf("&servicestatustypes=%d",service_status_types);
	System.out.printf("&hoststatustypes=%d'>",statusdata_h.HOST_DOWN|statusdata_h.HOST_UNREACHABLE);
	System.out.printf("<I>All Problems</I></A></TH>\n");

	System.out.printf("<TH CLASS='hostTotals'>");
	System.out.printf("<A CLASS='hostTotals' HREF='%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		System.out.printf("host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		System.out.printf("servicegroup=%s",servicegroup_name);
	else{
		System.out.printf("hostgroup=%s",hostgroup_name);
		if((service_status_types!=all_service_status_types) || group_style_type==STYLE_DETAIL)
			System.out.printf("&style=detail");
		else if(group_style_type==STYLE_HOST_DETAIL)
			System.out.printf("&style=hostdetail");
	        }
	if(service_status_types!=all_service_status_types)
		System.out.printf("&servicestatustypes=%d",service_status_types);
	System.out.printf("'>");
	System.out.printf("<I>All Types</I></A></TH>\n");

	System.out.printf("</TR><TR>\n");

	/* total hosts with problems */
	System.out.printf("<TD CLASS='hostTotals%s'>%d</TD>\n",(total_problems>0)?"PROBLEMS":"",total_problems);

	/* total hosts */
	System.out.printf("<TD CLASS='hostTotals'>%d</TD>\n",total_hosts);

	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</DIV>\n");

	return;
        }



/* display a detailed listing of the status of all services... */
public static void show_service_detail(){
	long t;
	String date_time;
	String state_duration;
	String status = "";
	String temp_buffer;
	String temp_url;
	String status_class="";
	String status_bg_class="";
	String host_status_bg_class="";
	String last_host="";
	int new_host=common_h.FALSE;
//	statusdata_h.servicestatus temp_status=null;
	objects_h.hostgroup temp_hostgroup=null;
	objects_h.servicegroup temp_servicegroup=null;
	objects_h.hostextinfo temp_hostextinfo=null;
	objects_h.serviceextinfo temp_serviceextinfo=null;
	statusdata_h.hoststatus temp_hoststatus=null;
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;
	int odd=0;
	int total_comments=0;
	int user_has_seen_something=common_h.FALSE;
	int use_sort=common_h.FALSE;
	int result=common_h.OK;
//	int first_entry=common_h.TRUE;
    cgiutils.time_breakdown tb = null;
	int duration_error=common_h.FALSE;
	int total_entries=0;
	int show_service=common_h.FALSE;


	/* sort the service list if necessary */
	if(sort_type!=cgiutils_h.SORT_NONE){
		result=sort_services(sort_type,sort_option);
		if(result==common_h.ERROR)
			use_sort=common_h.FALSE;
		else
			use_sort=common_h.TRUE;
	        }
	else
		use_sort=common_h.FALSE;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	if(display_header==common_h.TRUE)
		show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Service Status Details For ");
	if(display_type==DISPLAY_HOSTS){
		if(show_all_hosts==common_h.TRUE)
			System.out.printf("All Hosts");
		else
			System.out.printf("Host '%s'",host_name);
	        }
	else if(display_type==DISPLAY_SERVICEGROUPS){
		if(show_all_servicegroups==common_h.TRUE)
			System.out.printf("All Service Groups");
		else
			System.out.printf("Service Group '%s'",servicegroup_name);
	        }
	else{
		if(show_all_hostgroups==common_h.TRUE)
			System.out.printf("All Host Groups");
		else
			System.out.printf("Host Group '%s'",hostgroup_name);
	        }
	System.out.printf("</DIV>\n");

	if(use_sort==common_h.TRUE){
		System.out.printf("<DIV ALIGN=CENTER CLASS='statusSort'>Entries sorted by <b>");
		if(sort_option==cgiutils_h.SORT_HOSTNAME)
			System.out.printf("host name");
		else if(sort_option==cgiutils_h.SORT_SERVICENAME)
			System.out.printf("service name");
		else if(sort_option==cgiutils_h.SORT_SERVICESTATUS)
			System.out.printf("service status");
		else if(sort_option==cgiutils_h.SORT_LASTCHECKTIME)
			System.out.printf("last check time");
		else if(sort_option==cgiutils_h.SORT_CURRENTATTEMPT)
			System.out.printf("attempt number");
		else if(sort_option==cgiutils_h.SORT_STATEDURATION)
			System.out.printf("state duration");
		System.out.printf("</b> (%s)\n",(sort_type==cgiutils_h.SORT_ASCENDING)?"ascending":"descending");
		System.out.printf("</DIV>\n");
	        }

	if(service_filter!=null)
		System.out.printf("<DIV ALIGN=CENTER CLASS='statusSort'>Filtered By Services Matching \'%s\'</DIV>",service_filter);

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	temp_url = String.format( "%s?",cgiutils_h.STATUS_CGI);
	if(display_type==DISPLAY_HOSTS)
		temp_buffer = String.format( "host=%s",host_name);
	else if(display_type==DISPLAY_SERVICEGROUPS)
		temp_buffer = String.format( "servicegroup=%s&style=detail",servicegroup_name);
	else
		temp_buffer = String.format( "hostgroup=%s&style=detail",hostgroup_name);
	
    temp_url += ( temp_buffer );
	if(service_status_types!=all_service_status_types){
		temp_buffer = String.format( "&servicestatustypes=%d",service_status_types);
		temp_url += ( temp_buffer );
	        }
	if(host_status_types!=all_host_status_types){
		temp_buffer = String.format( "&hoststatustypes=%d",host_status_types);
		temp_url += temp_buffer;
	        }
	if(service_properties!=0){
		temp_buffer = String.format( "&serviceprops=d",service_properties);
		temp_url += temp_buffer ;
	        }
	if(host_properties!=0){
		temp_buffer = String.format( "&hostprops=%d",host_properties);
		temp_url += temp_buffer;
	        }

	/* the main list of services */
	System.out.printf("<TABLE BORDER=0 width=100%% CLASS='status'>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TH CLASS='status'>Host&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host name (ascending)' TITLE='Sort by host name (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host name (descending)' TITLE='Sort by host name (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_HOSTNAME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_HOSTNAME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Service&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by service name (ascending)' TITLE='Sort by service name (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by service name (descending)' TITLE='Sort by service name (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_SERVICENAME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_SERVICENAME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Status&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by service status (ascending)' TITLE='Sort by service status (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by service status (descending)' TITLE='Sort by service status (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_SERVICESTATUS,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_SERVICESTATUS,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Last Check&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by last check time (ascending)' TITLE='Sort by last check time (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by last check time (descending)' TITLE='Sort by last check time (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_LASTCHECKTIME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_LASTCHECKTIME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Duration&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by state duration (ascending)' TITLE='Sort by state duration (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by state duration time (descending)' TITLE='Sort by state duration time (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_STATEDURATION,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_STATEDURATION,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Attempt&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by current attempt (ascending)' TITLE='Sort by current attempt (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by current attempt (descending)' TITLE='Sort by current attempt (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_CURRENTATTEMPT,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_CURRENTATTEMPT,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Status Information</TH>\n");
	System.out.printf("</TR>\n");


	temp_hostgroup=objects.find_hostgroup(hostgroup_name);
	temp_servicegroup=objects.find_servicegroup(servicegroup_name);

	/* check all services... */
    for ( statusdata_h.servicestatus temp_status : (ArrayList<statusdata_h.servicestatus>) statusdata.servicestatus_list ) {

		/* find the service  */
		temp_service=objects.find_service(temp_status.host_name,temp_status.description);

		/* if we couldn't find the service, go to the next service */
		if(temp_service==null)
			continue;

		/* find the host */
		temp_host=objects.find_host(temp_service.host_name);

		/* make sure user has rights to see this... */
		if(cgiauth.is_authorized_for_service(temp_service,/* TODO & */ current_authdata)==common_h.FALSE)
			continue;

		user_has_seen_something=common_h.TRUE;

		/* get the host status information */
		temp_hoststatus=statusdata.find_hoststatus(temp_service.host_name);

		/* see if we should display services for hosts with tis type of status */
		if(0==(host_status_types & temp_hoststatus.status))
			continue;

		/* see if we should display this type of service status */
		if(0==(service_status_types & temp_status.status))
			continue;	

		/* check host properties filter */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		/* check service properties filter */
		if(passes_service_properties_filter(temp_status)==common_h.FALSE)
			continue;

		/* servicefilter cgi var */
		if(service_filter!=null)
            if ( ! Pattern.matches( service_filter,  temp_status.description ) ) 
                    continue;

		show_service=common_h.FALSE;

		if(display_type==DISPLAY_HOSTS){
			if(show_all_hosts==common_h.TRUE)
				show_service=common_h.TRUE;
			else if(host_name.equals(temp_status.host_name))
				show_service=common_h.TRUE;
		        }

		else if(display_type==DISPLAY_HOSTGROUPS){
			if(show_all_hostgroups==common_h.TRUE)
				show_service=common_h.TRUE;
			else if(objects.is_host_member_of_hostgroup(temp_hostgroup,temp_host)==common_h.TRUE)
				show_service=common_h.TRUE;
		        }

		else if(display_type==DISPLAY_SERVICEGROUPS){
			if(show_all_servicegroups==common_h.TRUE)
				show_service=common_h.TRUE;
			else if(objects.is_service_member_of_servicegroup(temp_servicegroup,temp_service)==common_h.TRUE)
				show_service=common_h.TRUE;
		        }

		if(show_service==common_h.TRUE){

			if(!last_host.equals(temp_status.host_name))
				new_host=common_h.TRUE;
			else
				new_host=common_h.FALSE;

			if(new_host==common_h.TRUE){
				if(!last_host.equals("")){
					System.out.printf("<TR><TD colspan=6></TD></TR>\n");
					System.out.printf("<TR><TD colspan=6></TD></TR>\n");
				        }
			        }

			if(odd != 0)
				odd=0;
			else
				odd=1;

			/* keep track of total number of services we're displaying */
			total_entries++;

		        /* get the last service check time */
			t=temp_status.last_check;
            date_time = cgiutils.get_time_string(t,common_h.SHORT_DATE_TIME);
			if(temp_status.last_check==0L)
				date_time = "N/A";

			if(temp_status.status==statusdata_h.SERVICE_PENDING){
				status = "PENDING";
				status_class="PENDING";
				status_bg_class=(odd != 0)?"Even":"Odd";
		                }
			else if(temp_status.status==statusdata_h.SERVICE_OK){
				status = "OK";
				status_class="OK";
				status_bg_class=(odd!=0)?"Even":"Odd";
		                }
			else if(temp_status.status==statusdata_h.SERVICE_WARNING){
				status = "WARNING";
				status_class="WARNING";
				if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
					status_bg_class="BGWARNINGACK";
				else if(temp_status.scheduled_downtime_depth>0)
					status_bg_class="BGWARNINGSCHED";
				else
					status_bg_class="BGWARNING";
		                }
			else if(temp_status.status==statusdata_h.SERVICE_UNKNOWN){
				status = "UNKNOWN";
				status_class="UNKNOWN";
				if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
					status_bg_class="BGUNKNOWNACK";
				else if(temp_status.scheduled_downtime_depth>0)
					status_bg_class="BGUNKNOWNSCHED";
				else
					status_bg_class="BGUNKNOWN";
		                }
			else if(temp_status.status==statusdata_h.SERVICE_CRITICAL){
				status = "CRITICAL";
				status_class="CRITICAL";
				if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
					status_bg_class="BGCRITICALACK";
				else if(temp_status.scheduled_downtime_depth>0)
					status_bg_class="BGCRITICALSCHED";
				else
					status_bg_class="BGCRITICAL";
		                }

			System.out.printf("<TR>\n");

			/* host name column */
			if(new_host==common_h.TRUE){

				/* find extended information for this host */
				temp_hostextinfo=objects.find_hostextinfo(temp_status.host_name);

				if(temp_hoststatus.status==statusdata_h.HOST_DOWN){
					if(temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE)
						host_status_bg_class="HOSTDOWNACK";
					else if(temp_hoststatus.scheduled_downtime_depth>0)
						host_status_bg_class="HOSTDOWNSCHED";
					else
						host_status_bg_class="HOSTDOWN";
				        }
				else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE){
					if(temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE)
						host_status_bg_class="HOSTUNREACHABLEACK";
					else if(temp_hoststatus.scheduled_downtime_depth>0)
						host_status_bg_class="HOSTUNREACHABLESCHED";
					else
						host_status_bg_class="HOSTUNREACHABLE";
				        }
				else
					host_status_bg_class=(odd!=0)?"Even":"Odd";

				System.out.printf("<TD CLASS='status%s'>",host_status_bg_class);

				System.out.printf("<TABLE BORDER=0 WIDTH='100%%' cellpadding=0 cellspacing=0>\n");
				System.out.printf("<TR>\n");
				System.out.printf("<TD ALIGN=LEFT>\n");
				System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
				System.out.printf("<TR>\n");
				System.out.printf("<TD align=left valign=center CLASS='status%s'><A HREF='%s?type=%d&host=%s'>%s</A></TD>\n",host_status_bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),temp_status.host_name);
				System.out.printf("</TR>\n");
				System.out.printf("</TABLE>\n");
				System.out.printf("</TD>\n");
				System.out.printf("<TD align=right valign=center>\n");
				System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
				System.out.printf("<TR>\n");
				total_comments=comments.number_of_host_comments(temp_host.name);
				if(temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE){
					System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s#comments'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host problem has been acknowledged' TITLE='This host problem has been acknowledged'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.ACKNOWLEDGEMENT_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			                }
				if(total_comments>0)
					System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s#comments'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host has %d comment%s associated with it' TITLE='This host has %d comment%s associated with it'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.COMMENT_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,total_comments,(total_comments==1)?"":"s",total_comments,(total_comments==1)?"":"s");
				if(temp_hoststatus.notifications_enabled==common_h.FALSE){
					System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Notifications for this host have been disabled' TITLE='Notifications for this host have been disabled'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			                }
				if(temp_hoststatus.checks_enabled==common_h.FALSE){
					System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Checks of this host have been disabled'd TITLE='Checks of this host have been disabled'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
				        }
				if(temp_hoststatus.is_flapping==common_h.TRUE){
					System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host is flapping between states' TITLE='This host is flapping between states'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.FLAPPING_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
				        }
				if(temp_hoststatus.scheduled_downtime_depth>0){
					System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host is currently in a period of scheduled downtime' TITLE='This host is currently in a period of scheduled downtime'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.SCHEDULED_DOWNTIME_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
				        }
				if(temp_hostextinfo!=null){
					if(temp_hostextinfo.notes_url!=null){
						System.out.printf("<TD align=center valign=center>");
						System.out.printf("<A HREF='");
						cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.notes_url);
						System.out.printf("' TARGET='_blank'>");
						System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.NOTES_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extra Host Notes","View Extra Host Notes");
						System.out.printf("</A>");
						System.out.printf("</TD>\n");
					        }
					if(temp_hostextinfo.action_url!=null){
						System.out.printf("<TD align=center valign=center>");
						System.out.printf("<A HREF='");
						cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.action_url);
						System.out.printf("' TARGET='_blank'>");
						System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.ACTION_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Perform Extra Host Actions","Perform Extra Host Actions");
						System.out.printf("</A>");
						System.out.printf("</TD>\n");
					        }
					if(temp_hostextinfo.icon_image!=null){
						System.out.printf("<TD align=center valign=center>");
						System.out.printf("<A HREF='%s?type=%d&host=%s'>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name));
                        System.out.printf("<IMG SRC='%s",cgiutils.url_logo_images_path);
                        cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.icon_image);
                        System.out.printf("' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt);
                        System.out.printf("</A>");
						System.out.printf("</TD>\n");
					        }
				        }
				System.out.printf("</TR>\n");
				System.out.printf("</TABLE>\n");
				System.out.printf("</TD>\n");
				System.out.printf("</TR>\n");
				System.out.printf("</TABLE>\n");
			        }
			else
				System.out.printf("<TD>");
			System.out.printf("</TD>\n");

			/* service name column */
			System.out.printf("<TD CLASS='status%s'>",status_bg_class);
			System.out.printf("<TABLE BORDER=0 WIDTH='100%%' CELLSPACING=0 CELLPADDING=0>");
			System.out.printf("<TR>");
			System.out.printf("<TD ALIGN=LEFT>");
			System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0>\n");
			System.out.printf("<TR>\n");
			System.out.printf("<TD ALIGN=LEFT valign=center CLASS='status%s'><A HREF='%s?type=%d&host=%s",status_bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
			System.out.printf("&service=%s'>%s</A></TD>",cgiutils.url_encode(temp_status.description),temp_status.description);
			System.out.printf("</TR>\n");
			System.out.printf("</TABLE>\n");
			System.out.printf("</TD>\n");
			System.out.printf("<TD ALIGN=RIGHT CLASS='status%s'>\n",status_bg_class);
			System.out.printf("<TABLE BORDER=0 cellspacing=0 cellpadding=0>\n");
			System.out.printf("<TR>\n");
			total_comments=comments.number_of_service_comments(temp_service.host_name,temp_service.description);
			if(total_comments>0){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s#comments'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This service has %d comment%s associated with it' TITLE='This service has %d comment%s associated with it'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.COMMENT_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,total_comments,(total_comments==1)?"":"s",total_comments,(total_comments==1)?"":"s");
			        }
			if(temp_status.problem_has_been_acknowledged==common_h.TRUE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s#comments'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This service problem has been acknowledged' TITLE='This service problem has been acknowledged'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.ACKNOWLEDGEMENT_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_status.checks_enabled==common_h.FALSE && temp_status.accept_passive_service_checks==common_h.FALSE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Active and passive checks have been disabled for this service' TITLE='Active and passive checks have been disabled for this service'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			else if(temp_status.checks_enabled==common_h.FALSE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Active checks of the service have been disabled - only passive checks are being accepted' TITLE='Active checks of the service have been disabled - only passive checks are being accepted'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.PASSIVE_ONLY_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_status.notifications_enabled==common_h.FALSE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Notifications for this service have been disabled' TITLE='Notifications for this service have been disabled'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_status.is_flapping==common_h.TRUE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This service is flapping between states' TITLE='This service is flapping between states'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.FLAPPING_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_status.scheduled_downtime_depth>0){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_status.host_name));
				System.out.printf("&service=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This service is currently in a period of scheduled downtime' TITLE='This service is currently in a period of scheduled downtime'></A></TD>",cgiutils.url_encode(temp_status.description),cgiutils.url_images_path,cgiutils_h.SCHEDULED_DOWNTIME_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			temp_serviceextinfo=objects.find_serviceextinfo(temp_service.host_name,temp_service.description);
			if(temp_serviceextinfo!=null){
                
               /* UPDATED 2.3 */
			   if(temp_serviceextinfo.notes_url!=null){
			      System.out.printf("<TD align=center valign=center>");
			      System.out.printf("<A HREF='");
                  cgiutils.print_extra_service_url(temp_service.host_name,temp_service.description,temp_serviceextinfo.notes_url);
			      System.out.printf("' TARGET='_blank'>");
			      System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.NOTES_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extra Service Notes","View Extra Service Notes");
			      System.out.printf("</A>");
			      System.out.printf("</TD>\n");
			   }

               /* UPDATED 2.3 */
			   if(temp_serviceextinfo.action_url!=null){
			      System.out.printf("<TD align=center valign=center>");
			      System.out.printf("<A HREF='");
                  cgiutils.print_extra_service_url(temp_service.host_name,temp_service.description,temp_serviceextinfo.action_url);
			      System.out.printf("' TARGET='_blank'>");
			      System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.ACTION_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Perform Extra Service Actions","Perform Extra Service Actions");
			      System.out.printf("</A>");
			      System.out.printf("</TD>\n");
			   }
			   
                if(temp_serviceextinfo.icon_image!=null){
					System.out.printf("<TD ALIGN=center valign=center>");
					System.out.printf("<A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_service.host_name));
					System.out.printf("&service=%s'>",cgiutils.url_encode(temp_service.description));
                    System.out.printf("<IMG SRC='%s",cgiutils.url_logo_images_path);
                    cgiutils.print_extra_service_url(temp_service.host_name,temp_service.description,temp_serviceextinfo.icon_image);
                    System.out.printf("' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,(temp_serviceextinfo.icon_image_alt==null)?"":temp_serviceextinfo.icon_image_alt,(temp_serviceextinfo.icon_image_alt==null)?"":temp_serviceextinfo.icon_image_alt);
					System.out.printf("</A>");
					System.out.printf("</TD>\n");
				        }
			        }
			System.out.printf("</TR>\n");
			System.out.printf("</TABLE>\n");
			System.out.printf("</TD>\n");
			System.out.printf("</TR>");
			System.out.printf("</TABLE>");
			System.out.printf("</TD>\n");

			/* state duration calculation... */
			t=0;
			duration_error=common_h.FALSE;
			if(temp_status.last_state_change==0){
				if( blue.program_start>current_time)
					duration_error=common_h.TRUE;
				else
					t=(current_time-blue.program_start);
			        }
			else{
				if(temp_status.last_state_change>current_time)
					duration_error=common_h.TRUE;
				else
					t=current_time-temp_status.last_state_change;
			        }
            
            tb = cgiutils.get_time_breakdown(t);
			if(duration_error==common_h.TRUE)
				state_duration = String.format( "???");
			else
				state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_status.last_state_change==0)?"+":"");

                        /* the rest of the columns... */
			System.out.printf("<TD CLASS='status%s'>%s</TD>\n",status_class,status);
			System.out.printf("<TD CLASS='status%s' nowrap>%s</TD>\n",status_bg_class,date_time);
			System.out.printf("<TD CLASS='status%s' nowrap>%s</TD>\n",status_bg_class,state_duration);
			System.out.printf("<TD CLASS='status%s'>%d/%d</TD>\n",status_bg_class,temp_status.current_attempt,temp_status.max_attempts);
			System.out.printf("<TD CLASS='status%s'>%s&nbsp;</TD>\n",status_bg_class,(temp_status.plugin_output==null)?"":temp_status.plugin_output);

			System.out.printf("</TR>\n");

			last_host=temp_status.host_name;
		        }

	        }

	System.out.printf("</TABLE>\n");

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE){

		if(statusdata.servicestatus_list!=null && !statusdata.servicestatus_list.isEmpty() ){
			System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the services you requested...</DIV></P>\n");
			System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");
		        }
		else{
			System.out.printf("<P><DIV CLASS='infoMessage'>There doesn't appear to be any service status information in the status log...<br><br>\n");
			System.out.printf("Make sure that Blue is running and that you have specified the location of you status log correctly in the configuration files.</DIV></P>\n");
		        }
	        }

	else
		System.out.printf("<BR><DIV CLASS='itemTotalsTitle'>%d Matching Service Entries Displayed</DIV>\n",total_entries);

	return;
        }




/* display a detailed listing of the status of all hosts... */
public static void show_host_detail(){
	long t;
	String date_time="";
	String state_duration="";
	String status="";
	String temp_buffer="";
	String temp_url="";
	String status_class="";
	String status_bg_class="";
	objects_h.hostgroup temp_hostgroup=null;
	objects_h.hostextinfo temp_hostextinfo=null;
	objects_h.host temp_host=null;
//	hostsort *temp_hostsort=null;
	int odd=0;
	int total_comments=0;
	int user_has_seen_something=common_h.FALSE;
	int use_sort=common_h.FALSE;
	int result=common_h.OK;
//	int first_entry=common_h.TRUE;
    cgiutils.time_breakdown tb;
	int duration_error=common_h.FALSE;
	int total_entries=0;


	/* sort the host list if necessary */
	if(sort_type!=cgiutils_h.SORT_NONE){
		result=sort_hosts(sort_type,sort_option);
		if(result==common_h.ERROR)
			use_sort=common_h.FALSE;
		else
			use_sort=common_h.TRUE;
	        }
	else
		use_sort=common_h.FALSE;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Host Status Details For ");
	if(show_all_hostgroups==common_h.TRUE)
		System.out.printf("All Host Groups");
	else
		System.out.printf("Host Group '%s'",hostgroup_name);
	System.out.printf("</DIV>\n");

	if(use_sort==common_h.TRUE){
		System.out.printf("<DIV ALIGN=CENTER CLASS='statusSort'>Entries sorted by <b>");
		if(sort_option==cgiutils_h.SORT_HOSTNAME)
			System.out.printf("host name");
		else if(sort_option==cgiutils_h.SORT_HOSTSTATUS)
			System.out.printf("host status");
		else if(sort_option==cgiutils_h.SORT_LASTCHECKTIME)
			System.out.printf("last check time");
		else if(sort_option==cgiutils_h.SORT_CURRENTATTEMPT)
			System.out.printf("attempt number");
		else if(sort_option==cgiutils_h.SORT_STATEDURATION)
			System.out.printf("state duration");
		System.out.printf("</b> (%s)\n",(sort_type==cgiutils_h.SORT_ASCENDING)?"ascending":"descending");
		System.out.printf("</DIV>\n");
	        }

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");





	temp_url = String.format( "%s?",cgiutils_h.STATUS_CGI);
	temp_buffer = String.format( "hostgroup=%s&style=hostdetail",hostgroup_name);
	temp_url += temp_buffer;
	if(service_status_types!=all_service_status_types){
		temp_buffer = String.format( "&servicestatustypes=%d",service_status_types);
        temp_url += temp_buffer;
	        }
	if(host_status_types!=all_host_status_types){
		temp_buffer = String.format( "&hoststatustypes=%d",host_status_types);
        temp_url += temp_buffer;
	        }
	if(service_properties!=0){
		temp_buffer = String.format( "&serviceprops=%d",service_properties);
        temp_url += temp_buffer;
	        }
	if(host_properties!=0){
		temp_buffer = String.format( "&hostprops=%d",host_properties);
        temp_url += temp_buffer;
	        }


	/* the main list of hosts */
	System.out.printf("<DIV ALIGN='center'>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='status' WIDTH=100%%>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TH CLASS='status'>Host&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host name (ascending)' TITLE='Sort by host name (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host name (descending)' TITLE='Sort by host name (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_HOSTNAME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_HOSTNAME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Status&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host status (ascending)' TITLE='Sort by host status (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host status (descending)' TITLE='Sort by host status (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_HOSTSTATUS,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_HOSTSTATUS,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Last Check&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by last check time (ascending)' TITLE='Sort by last check time (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by last check time (descending)' TITLE='Sort by last check time (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_LASTCHECKTIME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_LASTCHECKTIME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Duration&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by state duration (ascending)' TITLE='Sort by state duration (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by state duration time (descending)' TITLE='Sort by state duration time (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_STATEDURATION,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_STATEDURATION,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='status'>Status Information</TH>\n");
	System.out.printf("</TR>\n");


	/* check all hosts... */
    for ( statusdata_h.hoststatus temp_status : (ArrayList<statusdata_h.hoststatus>) statusdata.hoststatus_list ) { 

		/* find the host  */
		temp_host=objects.find_host(temp_status.host_name);

		/* if we couldn't find the host, go to the next status entry */
		if(temp_host==null)
			continue;

		/* make sure user has rights to see this... */
		if(cgiauth.is_authorized_for_host(temp_host,/* TODO & */ current_authdata)==common_h.FALSE)
			continue;

		user_has_seen_something=common_h.TRUE;

		/* see if we should display services for hosts with this type of status */
		if(0==(host_status_types & temp_status.status))
			continue;

		/* check host properties filter */
		if(passes_host_properties_filter(temp_status)==common_h.FALSE)
			continue;


		/* see if this host is a member of the hostgroup */
		if(show_all_hostgroups==common_h.FALSE){
			temp_hostgroup=objects.find_hostgroup(hostgroup_name);
			if(temp_hostgroup==null)
				continue;
			if(objects.is_host_member_of_hostgroup(temp_hostgroup,temp_host)==common_h.FALSE)
				continue;
	                }
	
		total_entries++;


		if(display_type==DISPLAY_HOSTGROUPS){

			if(odd!=0)
				odd=0;
			else
				odd=1;

			
		        /* get the last host check time */
			t=temp_status.last_check;
            date_time = cgiutils.get_time_string(t,common_h.SHORT_DATE_TIME);
			if(temp_status.last_check==0L)
				date_time = "N/A";

			if(temp_status.status==statusdata_h.HOST_PENDING){
				status = "PENDING";
				status_class="PENDING";
				status_bg_class=(odd!=0)?"Even":"Odd";
		                }
			else if(temp_status.status==statusdata_h.HOST_UP){
				status = "UP";
				status_class="HOSTUP";
				status_bg_class=(odd!=0)?"Even":"Odd";
		                }
			else if(temp_status.status==statusdata_h.HOST_DOWN){
				status = "DOWN";
				status_class="HOSTDOWN";
				if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
					status_bg_class="BGDOWNACK";
				else if(temp_status.scheduled_downtime_depth>0)
					status_bg_class="BGDOWNSCHED";
				else
					status_bg_class="BGDOWN";
		                }
			else if(temp_status.status==statusdata_h.HOST_UNREACHABLE){
				status = "UNREACHABLE";
				status_class="HOSTUNREACHABLE";
				if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
					status_bg_class="BGUNREACHABLEACK";
				else if(temp_status.scheduled_downtime_depth>0)
					status_bg_class="BGUNREACHABLESCHED";
				else
					status_bg_class="BGUNREACHABLE";
		                }


			System.out.printf("<TR>\n");


			/**** host name column ****/

			/* find extended information for this host */
			temp_hostextinfo=objects.find_hostextinfo(temp_status.host_name);

			System.out.printf("<TD CLASS='status%s'>",status_class);

			System.out.printf("<TABLE BORDER=0 WIDTH='100%%' cellpadding=0 cellspacing=0>\n");
			System.out.printf("<TR>\n");
			System.out.printf("<TD ALIGN=LEFT>\n");
			System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
			System.out.printf("<TR>\n");
			System.out.printf("<TD align=left valign=center CLASS='status%s'><A HREF='%s?type=%d&host=%s'>%s</A>&nbsp;</TD>\n",status_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),temp_status.host_name);
			System.out.printf("</TR>\n");
			System.out.printf("</TABLE>\n");
			System.out.printf("</TD>\n");
			System.out.printf("<TD align=right valign=center>\n");
			System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
			System.out.printf("<TR>\n");
			total_comments=comments.number_of_host_comments(temp_host.name);
			if(temp_status.problem_has_been_acknowledged==common_h.TRUE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s#comments'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host problem has been acknowledged' TITLE='This host problem has been acknowledged'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.ACKNOWLEDGEMENT_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
		                }
			if(total_comments>0)
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s#comments'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host has %d comment%s associated with it' TITLE='This host has %d comment%s associated with it'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.COMMENT_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,total_comments,(total_comments==1)?"":"s",total_comments,(total_comments==1)?"":"s");
			if(temp_status.notifications_enabled==common_h.FALSE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Notifications for this host have been disabled' TITLE='Notifications for this host have been disabled'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
		                }
			if(temp_status.checks_enabled==common_h.FALSE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='Checks of this host have been disabled' TITLE='Checks of this host have been disabled'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_status.is_flapping==common_h.TRUE){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host is flapping between states' TITLE='This host is flapping between states'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.FLAPPING_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_status.scheduled_downtime_depth>0){
				System.out.printf("<TD ALIGN=center valign=center><A HREF='%s?type=%d&host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='This host is currently in a period of scheduled downtime' TITLE='This host is currently in a period of scheduled downtime'></A></TD>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.SCHEDULED_DOWNTIME_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT);
			        }
			if(temp_hostextinfo!=null){
				if(temp_hostextinfo.notes_url!=null){
					System.out.printf("<TD align=center valign=center>");
					System.out.printf("<A HREF='");
					cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.notes_url);
					System.out.printf("' TARGET='_blank'>");
					System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.NOTES_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extra Host Notes","View Extra Host Notes");
					System.out.printf("</A>");
					System.out.printf("</TD>\n");
				        }
				if(temp_hostextinfo.action_url!=null){
					System.out.printf("<TD align=center valign=center>");
					System.out.printf("<A HREF='");
					cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.action_url);
					System.out.printf("' TARGET='_blank'>");
					System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.ACTION_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Perform Extra Host Actions","Perform Extra Host Actions");
					System.out.printf("</A>");
					System.out.printf("</TD>\n");
				        }
				if(temp_hostextinfo.icon_image!=null){
					System.out.printf("<TD align=center valign=center>");
					System.out.printf("<A HREF='%s?type=%d&host=%s'>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_status.host_name));
                    System.out.printf("<IMG SRC='%s",cgiutils.url_logo_images_path);
                    cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.icon_image);
                    System.out.printf("' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt);
					System.out.printf("</A>");
					System.out.printf("</TD>\n");
				        }
			        }
			System.out.printf("<TD><a href='%s?host=%s'><img src='%s%s' border=0 alt='View Service Details For This Host' title='View Service Details For This Host'></a></TD>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_status.host_name),cgiutils.url_images_path,cgiutils_h.STATUS_DETAIL_ICON);
			System.out.printf("</TR>\n");
			System.out.printf("</TABLE>\n");
			System.out.printf("</TD>\n");
			System.out.printf("</TR>\n");
			System.out.printf("</TABLE>\n");

			System.out.printf("</TD>\n");


			/* state duration calculation... */
			t=0;
			duration_error=common_h.FALSE;
			if(temp_status.last_state_change==0){
				if(blue.program_start>current_time)
					duration_error=common_h.TRUE;
				else
					t=(current_time-blue.program_start);
			        }
			else{
				if(temp_status.last_state_change>current_time)
					duration_error=common_h.TRUE;
				else
					t=current_time-temp_status.last_state_change;
			        }
			tb = cgiutils.get_time_breakdown(t);
			if(duration_error==common_h.TRUE)
				state_duration = String.format( "???");
			else
				state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_status.last_state_change==0)?"+":"");

                        /* the rest of the columns... */
			System.out.printf("<TD CLASS='status%s'>%s</TD>\n",status_class,status);
			System.out.printf("<TD CLASS='status%s' nowrap>%s</TD>\n",status_bg_class,date_time);
			System.out.printf("<TD CLASS='status%s' nowrap>%s</TD>\n",status_bg_class,state_duration);
			System.out.printf("<TD CLASS='status%s'>%s&nbsp;</TD>\n",status_bg_class,(temp_status.plugin_output==null)?"":temp_status.plugin_output);

			System.out.printf("</TR>\n");
		        }

	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE){

		if(statusdata.hoststatus_list!=null && !statusdata.hoststatus_list.isEmpty()){
			System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV></P>\n");
			System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");
		        }
		else{
			System.out.printf("<P><DIV CLASS='infoMessage'>There doesn't appear to be any host status information in the status log...<br><br>\n");
			System.out.printf("Make sure that Blue is running and that you have specified the location of you status log correctly in the configuration files.</DIV></P>\n");
		        }
	        }

	else
		System.out.printf("<BR><DIV CLASS='itemTotalsTitle'>%d Matching Host Entries Displayed</DIV>\n",total_entries);

	return;
        }




/* show an overview of servicegroup(s)... */
public static void show_servicegroup_overviews(){
//	objects_h.servicegroup temp_servicegroup=null;
	int current_column;
	int user_has_seen_something=common_h.FALSE;
	int servicegroup_error=common_h.FALSE;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Service Overview For ");
	if(show_all_servicegroups==common_h.TRUE)
		System.out.printf("All Service Groups");
	else
		System.out.printf("Service Group '%s'",servicegroup_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</P>\n");


	/* display status overviews for all servicegroups */
	if(show_all_servicegroups==common_h.TRUE){


		System.out.printf("<DIV ALIGN=center>\n");
		System.out.printf("<TABLE BORDER=0 CELLPADDING=10>\n");

		current_column=1;

		/* loop through all servicegroups... */
		for ( objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>)  objects.servicegroup_list ) {

			/* make sure the user is authorized to view at least one host in this servicegroup */
			if(cgiauth.is_authorized_for_servicegroup(temp_servicegroup,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(current_column==1)
				System.out.printf("<TR>\n");
			System.out.printf("<TD VALIGN=top ALIGN=center>\n");
				
			show_servicegroup_overview(temp_servicegroup);

			user_has_seen_something=common_h.TRUE;

			System.out.printf("</TD>\n");
			if(current_column==overview_columns)
				System.out.printf("</TR>\n");

			if(current_column<overview_columns)
				current_column++;
			else
				current_column=1;
		        }

		if(current_column!=1){

			for(;current_column<=overview_columns;current_column++)
				System.out.printf("<TD></TD>\n");
			System.out.printf("</TR>\n");
		        }

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");
	        }

	/* else display overview for just a specific servicegroup */
	else{

		objects_h.servicegroup temp_servicegroup=objects.find_servicegroup(servicegroup_name);
		if(temp_servicegroup!=null){

			System.out.printf("<P>\n");
			System.out.printf("<DIV ALIGN=CENTER>\n");
			System.out.printf("<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0><TR><TD ALIGN=CENTER>\n");
			
			if(cgiauth.is_authorized_for_servicegroup(temp_servicegroup,/* TODO & */ current_authdata)==common_h.TRUE){

				show_servicegroup_overview(temp_servicegroup);
				
				user_has_seen_something=common_h.TRUE;
			        }

			System.out.printf("</TD></TR></TABLE>\n");
			System.out.printf("</DIV>\n");
			System.out.printf("</P>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='errorMessage'>Sorry, but service group '%s' doesn't seem to exist...</DIV>",servicegroup_name);
			servicegroup_error=common_h.TRUE;
		        }
	        }

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE && servicegroup_error==common_h.FALSE){

		System.out.printf("<p>\n");
		System.out.printf("<div align='center'>\n");

		if(objects.servicegroup_list!=null && !objects.servicegroup_list.isEmpty()){
			System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV>\n");
			System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='errorMessage'>There are no service groups defined.</DIV>\n");
			}

		System.out.printf("</div>\n");
		System.out.printf("</p>\n");
	        }

	return;
        }



/* shows an overview of a specific servicegroup... */
public static void show_servicegroup_overview(objects_h.servicegroup temp_servicegroup){
	objects_h.host temp_host;
	objects_h.host last_host;
	statusdata_h.hoststatus temp_hoststatus=null;
	int odd=0;


	System.out.printf("<DIV CLASS='status'>\n");
	System.out.printf("<A HREF='%s?servicegroup=%s&style=detail'>%s</A>", cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.alias);
	System.out.printf(" (<A HREF='%s?type=%d&servicegroup=%s'>%s</A>)",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICEGROUP_INFO,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.group_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<DIV CLASS='status'>\n");
	System.out.printf("<table border=1 CLASS='status'>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='status'>Host</TH><TH CLASS='status'>Status</TH><TH CLASS='status'>Services</TH><TH CLASS='status'>Actions</TH>\n");
	System.out.printf("</TR>\n");

	/* find all hosts that have services that are members of the servicegroup */
	last_host=null;
	for ( objects_h.servicegroupmember temp_member : (ArrayList<objects_h.servicegroupmember>)  temp_servicegroup.members ) {

		/* find the host */
		temp_host=objects.find_host(temp_member.host_name);
		if(temp_host==null)
			continue;

		/* skip this if it isn't a new host... */
		if(temp_host==last_host)
			continue;

		/* find the host status */
		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		/* make sure we only display hosts of the specified status levels */
		if((host_status_types & temp_hoststatus.status)==0)
			continue;

		/* make sure we only display hosts that have the desired properties */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		if(odd != 0)
			odd=0;
		else
			odd=1;

		show_servicegroup_hostgroup_member_overview(temp_hoststatus,odd,temp_servicegroup);

		last_host=temp_host;
	        }

	System.out.printf("</table>\n");
	System.out.printf("</DIV>\n");

	return;
        }



/* show a summary of servicegroup(s)... */
public static void show_servicegroup_summaries(){
	objects_h.servicegroup temp_servicegroup=null;
	int user_has_seen_something=common_h.FALSE;
	int servicegroup_error=common_h.FALSE;
	int odd=0;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Status Summary For ");
	if(show_all_servicegroups==common_h.TRUE)
		System.out.printf("All Service Groups");
	else
		System.out.printf("Service Group '%s'",servicegroup_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</P>\n");


	System.out.printf("<DIV ALIGN=center>\n");
	System.out.printf("<table border=1 CLASS='status'>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='status'>Service Group</TH><TH CLASS='status'>Host Status Totals</TH><TH CLASS='status'>Service Status Totals</TH>\n");
	System.out.printf("</TR>\n");

	/* display status summary for all servicegroups */
	if(show_all_servicegroups==common_h.TRUE){

		/* loop through all servicegroups... */
		for ( objects_h.servicegroup iter_servicegroup : (ArrayList<objects_h.servicegroup>)  objects.servicegroup_list ) {

			/* make sure the user is authorized to view at least one host in this servicegroup */
			if(cgiauth.is_authorized_for_servicegroup(iter_servicegroup,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(odd==0)
				odd=1;
			else
				odd=0;

			/* show summary for this servicegroup */
			show_servicegroup_summary(iter_servicegroup,odd);

			user_has_seen_something=common_h.TRUE;
		        }

	        }

	/* else just show summary for a specific servicegroup */
	else{
		temp_servicegroup=objects.find_servicegroup(servicegroup_name);
		if(temp_servicegroup==null)
			servicegroup_error=common_h.TRUE;
		else{
			show_servicegroup_summary(temp_servicegroup,1);
			user_has_seen_something=common_h.TRUE;
		        }
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE && servicegroup_error==common_h.FALSE){

		System.out.printf("<P><DIV ALIGN=CENTER>\n");

		if(objects.servicegroup_list!=null && !objects.servicegroup_list.isEmpty()){
			System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV>\n");
			System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='errorMessage'>There are no service groups defined.</DIV>\n");
			}

		System.out.printf("</DIV></P>\n");
	        }

	/* we couldn't find the servicegroup */
	else if(servicegroup_error==common_h.TRUE){
		System.out.printf("<P><DIV ALIGN=CENTER>\n");
		System.out.printf("<DIV CLASS='errorMessage'>Sorry, but servicegroup '%s' doesn't seem to exist...</DIV>\n",servicegroup_name);
		System.out.printf("</DIV></P>\n");
	        }

	return;
        }



/* displays status summary information for a specific servicegroup */
public static void show_servicegroup_summary(objects_h.servicegroup temp_servicegroup,int odd){
	String status_bg_class="";

	if(odd==1)
		status_bg_class="Even";
	else
		status_bg_class="Odd";

	System.out.printf("<TR CLASS='status%s'><TD CLASS='status%s'>\n",status_bg_class,status_bg_class);
	System.out.printf("<A HREF='%s?servicegroup=%s&style=overview'>%s</A> ",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.alias);
	System.out.printf("(<A HREF='%s?type=%d&servicegroup=%s'>%s</a>)",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICEGROUP_INFO,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.group_name);
	System.out.printf("</TD>");
				
	System.out.printf("<TD CLASS='status%s' ALIGN=CENTER VALIGN=CENTER>",status_bg_class);
	show_servicegroup_host_totals_summary(temp_servicegroup);
	System.out.printf("</TD>");

	System.out.printf("<TD CLASS='status%s' ALIGN=CENTER VALIGN=CENTER>",status_bg_class);
	show_servicegroup_service_totals_summary(temp_servicegroup);
	System.out.printf("</TD>");

	System.out.printf("</TR>\n");

	return;
        }



/* shows host total summary information for a specific servicegroup */
public static void show_servicegroup_host_totals_summary(objects_h.servicegroup temp_servicegroup){
	int total_up=0;
	int total_down=0;
	int total_unreachable=0;
	int total_pending=0;
	statusdata_h.hoststatus temp_hoststatus;
	objects_h.host temp_host;
	objects_h.host last_host;

	last_host=null;
	for ( objects_h.servicegroupmember temp_member : (ArrayList<objects_h.servicegroupmember>)  temp_servicegroup.members ) {

		/* find the host */
		temp_host=objects.find_host(temp_member.host_name);
		if(temp_host==null)
			continue;

		/* skip this if it isn't a new host... */
		if(temp_host==last_host)
			continue;

		/* find the host status */
		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		/* make sure we only display hosts of the specified status levels */
		if(0==(host_status_types & temp_hoststatus.status))
			continue;

		/* make sure we only display hosts that have the desired properties */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		if(temp_hoststatus.status==statusdata_h.HOST_UP)
			total_up++;
		else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			total_down++;
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			total_unreachable++;
		else
			total_pending++;

		last_host=temp_host;
	        }

	System.out.printf("<TABLE BORDER=0>\n");

	if(total_up>0)
		System.out.printf("<TR><TD CLASS='miniStatusUP'><A HREF='%s?servicegroup=%s&style=detail&&hoststatustypes=%d&hostprops=%d'>%d UP</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.HOST_UP,host_properties,total_up);
	if(total_down>0)
		System.out.printf("<TR><TD CLASS='miniStatusDOWN'><A HREF='%s?servicegroup=%s&style=detail&hoststatustypes=%d&hostprops=%d'>%d DOWN</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.HOST_DOWN,host_properties,total_down);
	if(total_unreachable>0)
		System.out.printf("<TR><TD CLASS='miniStatusUNREACHABLE'><A HREF='%s?servicegroup=%s&style=detail&hoststatustypes=%d&hostprops=%d'>%d UNREACHABLE</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.HOST_UNREACHABLE,host_properties,total_unreachable);
	if(total_pending>0)
		System.out.printf("<TR><TD CLASS='miniStatusPENDING'><A HREF='%s?servicegroup=%s&style=detail&hoststatustypes=%d&hostprops=%d'>%d PENDING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.HOST_PENDING,host_properties,total_pending);

	System.out.printf("</TABLE>\n");

	if((total_up + total_down + total_unreachable + total_pending)==0)
		System.out.printf("No matching hosts");

	return;
        }



/* shows service total summary information for a specific servicegroup */
public static void show_servicegroup_service_totals_summary(objects_h.servicegroup temp_servicegroup){
//	objects_h.servicegroupmember temp_member;
	int total_ok=0;
	int total_warning=0;
	int total_unknown=0;
	int total_critical=0;
	int total_pending=0;
	statusdata_h.servicestatus temp_servicestatus;
	objects_h.service temp_service;
	statusdata_h.hoststatus temp_hoststatus;


	/* check all services... */
	for ( objects_h.servicegroupmember temp_member : (ArrayList<objects_h.servicegroupmember>)  temp_servicegroup.members ) {

		/* find the service */
		temp_service=objects.find_service(temp_member.host_name,temp_member.service_description);
		if(temp_service==null)
			continue;

		/* find the service status */
		temp_servicestatus=statusdata.find_servicestatus(temp_service.host_name,temp_service.description);
		if(temp_servicestatus==null)
			continue;

		/* find the status of the associated host */
		temp_hoststatus=statusdata.find_hoststatus(temp_servicestatus.host_name);
		if(temp_hoststatus==null)
			continue;

		/* make sure we only display hosts of the specified status levels */
		if(0==(host_status_types & temp_hoststatus.status))
			continue;

		/* make sure we only display hosts that have the desired properties */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		/* make sure we only display services of the specified status levels */
		if(0==(service_status_types & temp_servicestatus.status))
			continue;

		/* make sure we only display services that have the desired properties */
		if(passes_service_properties_filter(temp_servicestatus)==common_h.FALSE)
			continue;

		if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
			total_critical++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
			total_warning++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
			total_unknown++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
			total_ok++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
			total_pending++;
		else
			total_ok++;
	        }


	System.out.printf("<TABLE BORDER=0>\n");

	if(total_ok>0)
		System.out.printf("<TR><TD CLASS='miniStatusOK'><A HREF='%s?servicegroup=%s&style=detail&&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d OK</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.SERVICE_OK,host_status_types,service_properties,host_properties,total_ok);
	if(total_warning>0)
		System.out.printf("<TR><TD CLASS='miniStatusWARNING'><A HREF='%s?servicegroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d WARNING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.SERVICE_WARNING,host_status_types,service_properties,host_properties,total_warning);
	if(total_unknown>0)
		System.out.printf("<TR><TD CLASS='miniStatusUNKNOWN'><A HREF='%s?servicegroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d UNKNOWN</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.SERVICE_UNKNOWN,host_status_types,service_properties,host_properties,total_unknown);
	if(total_critical>0)
		System.out.printf("<TR><TD CLASS='miniStatusCRITICAL'><A HREF='%s?servicegroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d CRITICAL</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.SERVICE_CRITICAL,host_status_types,service_properties,host_properties,total_critical);
	if(total_pending>0)
		System.out.printf("<TR><TD CLASS='miniStatusPENDING'><A HREF='%s?servicegroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d PENDING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),statusdata_h.SERVICE_PENDING,host_status_types,service_properties,host_properties,total_pending);

	System.out.printf("</TABLE>\n");

	if((total_ok + total_warning + total_unknown + total_critical + total_pending)==0)
		System.out.printf("No matching services");

	return;
        }



/* show a grid layout of servicegroup(s)... */
public static void show_servicegroup_grids(){
	objects_h.servicegroup temp_servicegroup=null;
	int user_has_seen_something=common_h.FALSE;
	int servicegroup_error=common_h.FALSE;
	int odd=0;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Status Grid For ");
	if(show_all_servicegroups==common_h.TRUE)
		System.out.printf("All Service Groups");
	else
		System.out.printf("Service Group '%s'",servicegroup_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</P>\n");


	/* display status grids for all servicegroups */
	if(show_all_servicegroups==common_h.TRUE){

		/* loop through all servicegroups... */
		for ( objects_h.servicegroup iter_servicegroup : (ArrayList<objects_h.servicegroup>)  objects.servicegroup_list ) {

			/* make sure the user is authorized to view at least one host in this servicegroup */
			if(cgiauth.is_authorized_for_servicegroup(iter_servicegroup,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(odd==0)
				odd=1;
			else
				odd=0;

			/* show grid for this servicegroup */
			show_servicegroup_grid(iter_servicegroup);

			user_has_seen_something=common_h.TRUE;
		        }

	        }

	/* else just show grid for a specific servicegroup */
	else{
		temp_servicegroup=objects.find_servicegroup(servicegroup_name);
		if(temp_servicegroup==null)
			servicegroup_error=common_h.TRUE;
		else{
			show_servicegroup_grid(temp_servicegroup);
			user_has_seen_something=common_h.TRUE;
		        }
	        }

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE && servicegroup_error==common_h.FALSE){

		System.out.printf("<P><DIV ALIGN=CENTER>\n");

		if(objects.servicegroup_list!=null && !objects.servicegroup_list.isEmpty()){
			System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV>\n");
			System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='errorMessage'>There are no service groups defined.</DIV>\n");
			}

		System.out.printf("</DIV></P>\n");
	        }

	/* we couldn't find the servicegroup */
	else if(servicegroup_error==common_h.TRUE){
		System.out.printf("<P><DIV ALIGN=CENTER>\n");
		System.out.printf("<DIV CLASS='errorMessage'>Sorry, but servicegroup '%s' doesn't seem to exist...</DIV>\n",servicegroup_name);
		System.out.printf("</DIV></P>\n");
	        }

	return;
        }


/* displays status grid for a specific servicegroup */
public static void show_servicegroup_grid(objects_h.servicegroup temp_servicegroup){
	String status_bg_class="";
	String host_status_class="";
	String service_status_class="";
//	objects_h.servicegroupmember temp_member;
//	objects_h.servicegroupmember temp_member2;
	objects_h.host temp_host;
	objects_h.host last_host;
	statusdata_h.hoststatus temp_hoststatus;
	statusdata_h.servicestatus temp_servicestatus;
	objects_h.hostextinfo temp_hostextinfo;
	int odd=0;
	int current_item;


	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<DIV CLASS='status'><A HREF='%s?servicegroup=%s&style=detail'>%s</A>",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.alias);
	System.out.printf(" (<A HREF='%s?type=%d&servicegroup=%s'>%s</A>)</DIV>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICEGROUP_INFO,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.group_name);

	System.out.printf("<TABLE BORDER=1 CLASS='status' ALIGN=CENTER>\n");
	System.out.printf("<TR><TH CLASS='status'>Host</TH><TH CLASS='status'>Services</a></TH><TH CLASS='status'>Actions</TH></TR>\n");

	/* find all hosts that have services that are members of the servicegroup */
	last_host=null;
	for ( objects_h.servicegroupmember temp_member : (ArrayList<objects_h.servicegroupmember>)  temp_servicegroup.members ) {

		/* find the host */
		temp_host=objects.find_host(temp_member.host_name);
		if(temp_host==null)
			continue;

		/* get the status of the host */
		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		/* skip this if it isn't a new host... */
		if(temp_host==last_host)
			continue;

		if(odd==1){
			status_bg_class="Even";
			odd=0;
		        }
		else{
			status_bg_class="Odd";
			odd=1;
		        }

		System.out.printf("<TR CLASS='status%s'>\n",status_bg_class);

		if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			host_status_class="HOSTDOWN";
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			host_status_class="HOSTUNREACHABLE";
		else
			host_status_class=status_bg_class;

		System.out.printf("<TD CLASS='status%s'>",host_status_class);

		System.out.printf("<TABLE BORDER=0 WIDTH='100%%' cellpadding=0 cellspacing=0>\n");
		System.out.printf("<TR>\n");
		System.out.printf("<TD ALIGN=LEFT>\n");
		System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
		System.out.printf("<TR>\n");
		System.out.printf("<TD align=left valign=center CLASS='status%s'>",host_status_class);
		System.out.printf("<A HREF='%s?type=%d&host=%s'>%s</A>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name),temp_host.name);
		System.out.printf("</TD>\n");
		System.out.printf("</TR>\n");
		System.out.printf("</TABLE>\n");
		System.out.printf("</TD>\n");
		System.out.printf("<TD align=right valign=center nowrap>\n");
		System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
		System.out.printf("<TR>\n");

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo!=null){
			if(temp_hostextinfo.icon_image!=null){
				System.out.printf("<TD align=center valign=center>");
				System.out.printf("<A HREF='%s?type=%d&host=%s'>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name));
                System.out.printf("<IMG SRC='%s",cgiutils.url_logo_images_path);
                cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.icon_image);
                System.out.printf("' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt);
                System.out.printf("</A>");
				System.out.printf("<TD>\n");
			        }
		        }

		System.out.printf("</TR>\n");
		System.out.printf("</TABLE>\n");
		System.out.printf("</TD>\n");
		System.out.printf("</TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='status%s'>",host_status_class);

		/* display all services on the host that are part of the hostgroup */
		current_item=1;        
		for ( objects_h.servicegroupmember temp_member2 : (ArrayList<objects_h.servicegroupmember>)  temp_servicegroup.members ) {

			/* bail out if we've reached the end of the services that are associated with this servicegroup */
			if(!temp_member2.host_name.equals(temp_host.name))
//				break; // TODO
                continue;

			if(current_item>max_grid_width && max_grid_width>0){
				System.out.printf("<BR>\n");
				current_item=1;
		                }

			/* get the status of the service */
			temp_servicestatus=statusdata.find_servicestatus(temp_member2.host_name,temp_member2.service_description);
			if(temp_servicestatus==null)
				service_status_class="null";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
				service_status_class="OK";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
				service_status_class="WARNING";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
				service_status_class="UNKNOWN";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
				service_status_class="CRITICAL";
			else
				service_status_class="PENDING";

			System.out.printf("<A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_servicestatus.host_name));
			System.out.printf("&service=%s' CLASS='status%s'>%s</A>&nbsp;",cgiutils.url_encode(temp_servicestatus.description),service_status_class,temp_servicestatus.description);

			current_item++;
	                }

		/* actions */
		System.out.printf("<TD CLASS='status%s'>",host_status_class);

		System.out.printf("<A HREF='%s?type=%d&host=%s'>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name));
		System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.DETAIL_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extended Information For This Host","View Extended Information For This Host");
		System.out.printf("</A>");

		if(temp_hostextinfo!=null){
			if(temp_hostextinfo.notes_url!=null){
				System.out.printf("<A HREF='");
				cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.notes_url);
				System.out.printf("' TARGET='_blank'>");
				System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.NOTES_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extra Host Notes","View Extra Host Notes");
				System.out.printf("</A>");
			        }
			if(temp_hostextinfo.action_url!=null){
				System.out.printf("<A HREF='");
				cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.action_url);
				System.out.printf("' TARGET='_blank'>");
				System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.ACTION_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Perform Extra Host Actions","Perform Extra Host Actions");
				System.out.printf("</A>");
			        }
		        }

		System.out.printf("<a href='%s?host=%s'><img src='%s%s' border=0 alt='View Service Details For This Host' title='View Service Details For This Host'></a>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_host.name),cgiutils.url_images_path,cgiutils_h.STATUS_DETAIL_ICON);


		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'></A>",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(temp_host.name),cgiutils.url_images_path,cgiutils_h.STATUSMAP_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Locate Host On Map","Locate Host On Map");

		System.out.printf("</TD>\n");
		System.out.printf("</TR>\n");

		last_host=temp_host;
		}

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }



/* show an overview of hostgroup(s)... */
public static void show_hostgroup_overviews(){
	int current_column;
	int user_has_seen_something=common_h.FALSE;
	int hostgroup_error=common_h.FALSE;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Service Overview For ");
	if(show_all_hostgroups==common_h.TRUE)
		System.out.printf("All Host Groups");
	else
		System.out.printf("Host Group '%s'",hostgroup_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</P>\n");


	/* display status overviews for all hostgroups */
	if(show_all_hostgroups==common_h.TRUE){


		System.out.printf("<DIV ALIGN=center>\n");
		System.out.printf("<TABLE BORDER=0 CELLPADDING=10>\n");

		current_column=1;

		/* loop through all hostgroups... */
		for ( objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>)  objects.hostgroup_list ) {

			/* make sure the user is authorized to view this hostgroup */
			if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(current_column==1)
				System.out.printf("<TR>\n");
			System.out.printf("<TD VALIGN=top ALIGN=center>\n");
				
			show_hostgroup_overview(temp_hostgroup);

			user_has_seen_something=common_h.TRUE;

			System.out.printf("</TD>\n");
			if(current_column==overview_columns)
				System.out.printf("</TR>\n");

			if(current_column<overview_columns)
				current_column++;
			else
				current_column=1;
		        }

		if(current_column!=1){

			for(;current_column<=overview_columns;current_column++)
				System.out.printf("<TD></TD>\n");
			System.out.printf("</TR>\n");
		        }

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");
	        }

	/* else display overview for just a specific hostgroup */
	else{

        objects_h.hostgroup temp_hostgroup=objects.find_hostgroup(hostgroup_name);
		if(temp_hostgroup!=null){

			System.out.printf("<P>\n");
			System.out.printf("<DIV ALIGN=CENTER>\n");
			System.out.printf("<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0><TR><TD ALIGN=CENTER>\n");
			
			if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,/* TODO & */ current_authdata)==common_h.TRUE){

				show_hostgroup_overview(temp_hostgroup);
				
				user_has_seen_something=common_h.TRUE;
			        }

			System.out.printf("</TD></TR></TABLE>\n");
			System.out.printf("</DIV>\n");
			System.out.printf("</P>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='errorMessage'>Sorry, but host group '%s' doesn't seem to exist...</DIV>",hostgroup_name);
			hostgroup_error=common_h.TRUE;
		        }
	        }

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE && hostgroup_error==common_h.FALSE){

		System.out.printf("<p>\n");
		System.out.printf("<div align='center'>\n");

		if(statusdata.hoststatus_list!=null && !statusdata.hoststatus_list.isEmpty()){
			System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV>\n");
			System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='infoMessage'>There doesn't appear to be any host status information in the status log...<br><br>\n");
			System.out.printf("Make sure that Blue is running and that you have specified the location of you status log correctly in the configuration files.</DIV>\n");
		        }

		System.out.printf("</div>\n");
		System.out.printf("</p>\n");
	        }

	return;
        }



/* shows an overview of a specific hostgroup... */
public static void show_hostgroup_overview(objects_h.hostgroup hstgrp){
//	objects_h.hostgroupmember temp_member;
	objects_h.host temp_host;
	statusdata_h.hoststatus temp_hoststatus=null;
	int odd=0;

	/* make sure the user is authorized to view this hostgroup */
	if(cgiauth.is_authorized_for_hostgroup(hstgrp,/* TODO & */ current_authdata)==common_h.FALSE)
		return;

	System.out.printf("<DIV CLASS='status'>\n");
	System.out.printf("<A HREF='%s?hostgroup=%s&style=detail'>%s</A>",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hstgrp.group_name),hstgrp.alias);
	System.out.printf(" (<A HREF='%s?type=%d&hostgroup=%s'>%s</A>)",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOSTGROUP_INFO,cgiutils.url_encode(hstgrp.group_name),hstgrp.group_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<DIV CLASS='status'>\n");
	System.out.printf("<table border=1 CLASS='status'>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='status'>Host</TH><TH CLASS='status'>Status</TH><TH CLASS='status'>Services</TH><TH CLASS='status'>Actions</TH>\n");
	System.out.printf("</TR>\n");

	/* find all the hosts that belong to the hostgroup */
	for ( objects_h.hostgroupmember temp_member : (ArrayList<objects_h.hostgroupmember>)  hstgrp.members ) {

		/* find the host... */
		temp_host=objects.find_host(temp_member.host_name);
		if(temp_host==null)
			continue;

		/* find the host status */
		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		/* make sure we only display hosts of the specified status levels */
		if(0==(host_status_types & temp_hoststatus.status))
			continue;

		/* make sure we only display hosts that have the desired properties */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		if(odd != 0)
			odd=0;
		else
			odd=1;

		show_servicegroup_hostgroup_member_overview(temp_hoststatus,odd,null);
	        }

	System.out.printf("</table>\n");
	System.out.printf("</DIV>\n");

	return;
        }

 

/* shows a host status overview... */
public static void show_servicegroup_hostgroup_member_overview(statusdata_h.hoststatus hststatus,int odd,Object data){
	String status = "";
	String status_bg_class="";
	String status_class="";
	objects_h.hostextinfo temp_hostextinfo;

	if(hststatus.status==statusdata_h.HOST_PENDING){
		status = "PENDING";
		status_class="HOSTPENDING";
		status_bg_class=(odd!=0)?"Even":"Odd";
	        }
	else if(hststatus.status==statusdata_h.HOST_UP){
		status = "UP";
		status_class="HOSTUP";
		status_bg_class=(odd!=0)?"Even":"Odd";
	        }
	else if(hststatus.status==statusdata_h.HOST_DOWN){
		status = "DOWN";
		status_class="HOSTDOWN";
		status_bg_class="HOSTDOWN";
	        }
	else if(hststatus.status==statusdata_h.HOST_UNREACHABLE){
		status = "UNREACHABLE";
		status_class="HOSTUNREACHABLE";
		status_bg_class="HOSTUNREACHABLE";
	        }

	System.out.printf("<TR CLASS='status%s'>\n",status_bg_class);

	/* find extended information for this host */
	temp_hostextinfo=objects.find_hostextinfo(hststatus.host_name);

	System.out.printf("<TD CLASS='status%s'>\n",status_bg_class);

	System.out.printf("<TABLE BORDER=0 WIDTH=100%% cellpadding=0 cellspacing=0>\n");
	System.out.printf("<TR CLASS='status%s'>\n",status_bg_class);
	System.out.printf("<TD CLASS='status%s'><A HREF='%s?host=%s&style=detail'>%s</A></TD>\n",status_bg_class,cgiutils_h.STATUS_CGI,cgiutils.url_encode(hststatus.host_name),hststatus.host_name);

	if(temp_hostextinfo!=null){
		if(temp_hostextinfo.icon_image!=null){
			System.out.printf("<TD CLASS='status%s' WIDTH=5></TD>\n",status_bg_class);
			System.out.printf("<TD CLASS='status%s' ALIGN=right>",status_bg_class);
			System.out.printf("<a href='%s?type=%d&host=%s'>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(hststatus.host_name));
            System.out.printf("<IMG SRC='%s",cgiutils.url_logo_images_path);
            cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.icon_image);
            System.out.printf("' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt);
            System.out.printf("</A>");
			System.out.printf("</TD>\n");
	                }
	        }
	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");
	System.out.printf("</TD>\n");

	System.out.printf("<td CLASS='status%s'>%s</td>\n",status_class,status);

	System.out.printf("<td CLASS='status%s'>\n",status_bg_class);
	show_servicegroup_hostgroup_member_service_status_totals(hststatus.host_name,data);
	System.out.printf("</td>\n");

	System.out.printf("<td valign=center CLASS='status%s'>",status_bg_class);
	System.out.printf("<a href='%s?type=%d&host=%s'><img src='%s%s' border=0 alt='View Extended Information For This Host' title='View Extended Information For This Host'></a>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(hststatus.host_name),cgiutils.url_images_path,cgiutils_h.DETAIL_ICON);
	if(temp_hostextinfo!=null){
		if(temp_hostextinfo.notes_url!=null){
			System.out.printf("<A HREF='");
			cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.notes_url);
			System.out.printf("' TARGET='_blank'>");
			System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.NOTES_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extra Host Notes","View Extra Host Notes");
			System.out.printf("</A>");
		        }
		if(temp_hostextinfo.action_url!=null){
			System.out.printf("<A HREF='");
			cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.action_url);
			System.out.printf("' TARGET='_blank'>");
			System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.ACTION_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Perform Extra Host Actions","Perform Extra Host Actions");
			System.out.printf("</A>");
		        }
	        }
	System.out.printf("<a href='%s?host=%s'><img src='%s%s' border=0 alt='View Service Details For This Host' title='View Service Details For This Host'></a>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hststatus.host_name),cgiutils.url_images_path,cgiutils_h.STATUS_DETAIL_ICON);

	System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'></A>",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(hststatus.host_name),cgiutils.url_images_path,cgiutils_h.STATUSMAP_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Locate Host On Map","Locate Host On Map");

	System.out.printf("</TD>");

	System.out.printf("</TR>\n");

	return;
        }



public static void show_servicegroup_hostgroup_member_service_status_totals(String host_name,Object data){
	int total_ok=0;
	int total_warning=0;
	int total_unknown=0;
	int total_critical=0;
	int total_pending=0;
    
//	statusdata_h.servicestatus temp_servicestatus;
	objects_h.service temp_service;
	objects_h.servicegroup temp_servicegroup=null;
	String  temp_buffer = "";


	if(display_type==DISPLAY_SERVICEGROUPS)
		temp_servicegroup=(objects_h.servicegroup)data;

	/* check all services... */
	for ( statusdata_h.servicestatus temp_servicestatus : (ArrayList<statusdata_h.servicestatus>)  statusdata.servicestatus_list ) {

		if(host_name.equals(temp_servicestatus.host_name)){

			/* make sure the user is authorized to see this service... */
			temp_service=objects.find_service(temp_servicestatus.host_name,temp_servicestatus.description);
			if(cgiauth.is_authorized_for_service(temp_service,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(display_type==DISPLAY_SERVICEGROUPS){

				/* is this service a member of the servicegroup? */
				if(objects.is_service_member_of_servicegroup(temp_servicegroup,temp_service)==common_h.FALSE)
					continue;
			        }

			/* make sure we only display services of the specified status levels */
			if(0==(service_status_types & temp_servicestatus.status))
				continue;

			/* make sure we only display services that have the desired properties */
			if(passes_service_properties_filter(temp_servicestatus)==common_h.FALSE)
				continue;

			if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
				total_critical++;
			else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
				total_warning++;
			else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
				total_unknown++;
			else if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
				total_ok++;
			else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
				total_pending++;
			else
				total_ok++;
		        }
	        }


	System.out.printf("<TABLE BORDER=0 WIDTH=100%%>\n");

	if(display_type==DISPLAY_SERVICEGROUPS)
		temp_buffer = "servicegroup="+cgiutils.url_encode(temp_servicegroup.group_name)+"&style=detail";
	else
		temp_buffer = "host=" + cgiutils.url_encode(host_name);

	if(total_ok>0)
		System.out.printf("<TR><TD CLASS='miniStatusOK'><A HREF='%s?%s&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d OK</A></TD></TR>\n",cgiutils_h.STATUS_CGI,temp_buffer,statusdata_h.SERVICE_OK,host_status_types,service_properties,host_properties,total_ok);
	if(total_warning>0)
		System.out.printf("<TR><TD CLASS='miniStatusWARNING'><A HREF='%s?%s&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d WARNING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,temp_buffer,statusdata_h.SERVICE_WARNING,host_status_types,service_properties,host_properties,total_warning);
	if(total_unknown>0)
		System.out.printf("<TR><TD CLASS='miniStatusUNKNOWN'><A HREF='%s?%s&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d UNKNOWN</A></TD></TR>\n",cgiutils_h.STATUS_CGI,temp_buffer,statusdata_h.SERVICE_UNKNOWN,host_status_types,service_properties,host_properties,total_unknown);
	if(total_critical>0)
		System.out.printf("<TR><TD CLASS='miniStatusCRITICAL'><A HREF='%s?%s&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d CRITICAL</A></TD></TR>\n",cgiutils_h.STATUS_CGI,temp_buffer,statusdata_h.SERVICE_CRITICAL,host_status_types,service_properties,host_properties,total_critical);
	if(total_pending>0)
		System.out.printf("<TR><TD CLASS='miniStatusPENDING'><A HREF='%s?%s&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d PENDING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,temp_buffer,statusdata_h.SERVICE_PENDING,host_status_types,service_properties,host_properties,total_pending);

	System.out.printf("</TABLE>\n");

	if((total_ok + total_warning + total_unknown + total_critical + total_pending)==0)
		System.out.printf("No matching services");

	return;
        }



/* show a summary of hostgroup(s)... */
public static void show_hostgroup_summaries(){
	objects_h.hostgroup temp_hostgroup=null;
	int user_has_seen_something=common_h.FALSE;
	int hostgroup_error=common_h.FALSE;
	int odd=0;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Status Summary For ");
	if(show_all_hostgroups==common_h.TRUE)
		System.out.printf("All Host Groups");
	else
		System.out.printf("Host Group '%s'",hostgroup_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</P>\n");


	System.out.printf("<DIV ALIGN=center>\n");
	System.out.printf("<table border=1 CLASS='status'>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='status'>Host Group</TH><TH CLASS='status'>Host Status Totals</TH><TH CLASS='status'>Service Status Totals</TH>\n");
	System.out.printf("</TR>\n");

	/* display status summary for all hostgroups */
	if(show_all_hostgroups==common_h.TRUE){

		/* loop through all hostgroups... */
		for ( objects_h.hostgroup iter_hostgroup : (ArrayList<objects_h.hostgroup>)  objects.hostgroup_list ) {

			/* make sure the user is authorized to view this hostgroup */
			if(cgiauth.is_authorized_for_hostgroup(iter_hostgroup,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(odd==0)
				odd=1;
			else
				odd=0;

			/* show summary for this hostgroup */
			show_hostgroup_summary(iter_hostgroup,odd);

			user_has_seen_something=common_h.TRUE;
		        }

	        }

	/* else just show summary for a specific hostgroup */
	else{
		temp_hostgroup=objects.find_hostgroup(hostgroup_name);
		if(temp_hostgroup==null)
			hostgroup_error=common_h.TRUE;
		else{
			show_hostgroup_summary(temp_hostgroup,1);
			user_has_seen_something=common_h.TRUE;
		        }
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE && hostgroup_error==common_h.FALSE){

		System.out.printf("<P><DIV ALIGN=CENTER>\n");

		if(statusdata.hoststatus_list!=null && !statusdata.hoststatus_list.isEmpty()){
			System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV>\n");
			System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='infoMessage'>There doesn't appear to be any host status information in the status log...<br><br>\n");
			System.out.printf("Make sure that Blue is running and that you have specified the location of you status log correctly in the configuration files.</DIV>\n");
		        }

		System.out.printf("</DIV></P>\n");
	        }

	/* we couldn't find the hostgroup */
	else if(hostgroup_error==common_h.TRUE){
		System.out.printf("<P><DIV ALIGN=CENTER>\n");
		System.out.printf("<DIV CLASS='errorMessage'>Sorry, but hostgroup '%s' doesn't seem to exist...</DIV>\n",hostgroup_name);
		System.out.printf("</DIV></P>\n");
	        }

	return;
        }



/* displays status summary information for a specific hostgroup */
public static void show_hostgroup_summary(objects_h.hostgroup temp_hostgroup,int odd){
	String status_bg_class="";

	if(odd==1)
		status_bg_class="Even";
	else
		status_bg_class="Odd";

	System.out.printf("<TR CLASS='status%s'><TD CLASS='status%s'>\n",status_bg_class,status_bg_class);
	System.out.printf("<A HREF='%s?hostgroup=%s&style=overview'>%s</A> ",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),temp_hostgroup.alias);
	System.out.printf("(<A HREF='%s?type=%d&hostgroup=%s'>%s</a>)",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOSTGROUP_INFO,cgiutils.url_encode(temp_hostgroup.group_name),temp_hostgroup.group_name);
	System.out.printf("</TD>");
				
	System.out.printf("<TD CLASS='status%s' ALIGN=CENTER VALIGN=CENTER>",status_bg_class);
	show_hostgroup_host_totals_summary(temp_hostgroup);
	System.out.printf("</TD>");

	System.out.printf("<TD CLASS='status%s' ALIGN=CENTER VALIGN=CENTER>",status_bg_class);
	show_hostgroup_service_totals_summary(temp_hostgroup);
	System.out.printf("</TD>");

	System.out.printf("</TR>\n");

	return;
        }



/* shows host total summary information for a specific hostgroup */
public static void show_hostgroup_host_totals_summary(objects_h.hostgroup temp_hostgroup){
//	objects_h.hostgroupmember temp_member;
	int total_up=0;
	int total_down=0;
	int total_unreachable=0;
	int total_pending=0;
	statusdata_h.hoststatus temp_hoststatus;
	objects_h.host temp_host;

	/* find all the hosts that belong to the hostgroup */
	for ( objects_h.hostgroupmember temp_member : (ArrayList<objects_h.hostgroupmember>)  temp_hostgroup.members ) {

		/* find the host... */
		temp_host=objects.find_host(temp_member.host_name);
		if(temp_host==null)
			continue;

		/* find the host status */
		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		/* make sure we only display hosts of the specified status levels */
		if(0==(host_status_types & temp_hoststatus.status))
			continue;

		/* make sure we only display hosts that have the desired properties */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		if(temp_hoststatus.status==statusdata_h.HOST_UP)
			total_up++;
		else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			total_down++;
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			total_unreachable++;
		else
			total_pending++;
	        }

	System.out.printf("<TABLE BORDER=0>\n");

	if(total_up>0)
		System.out.printf("<TR><TD CLASS='miniStatusUP'><A HREF='%s?hostgroup=%s&style=detail&&hoststatustypes=%d&hostprops=%d'>%d UP</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.HOST_UP,host_properties,total_up);
	if(total_down>0)
		System.out.printf("<TR><TD CLASS='miniStatusDOWN'><A HREF='%s?hostgroup=%s&style=detail&hoststatustypes=%d&hostprops=%d'>%d DOWN</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.HOST_DOWN,host_properties,total_down);
	if(total_unreachable>0)
		System.out.printf("<TR><TD CLASS='miniStatusUNREACHABLE'><A HREF='%s?hostgroup=%s&style=detail&hoststatustypes=%d&hostprops=%d'>%d UNREACHABLE</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.HOST_UNREACHABLE,host_properties,total_unreachable);
	if(total_pending>0)
		System.out.printf("<TR><TD CLASS='miniStatusPENDING'><A HREF='%s?hostgroup=%s&style=detail&hoststatustypes=%d&hostprops=%d'>%d PENDING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.HOST_PENDING,host_properties,total_pending);

	System.out.printf("</TABLE>\n");

	if((total_up + total_down + total_unreachable + total_pending)==0)
		System.out.printf("No matching hosts");

	return;
        }



/* shows service total summary information for a specific hostgroup */
public static void show_hostgroup_service_totals_summary(objects_h.hostgroup temp_hostgroup){
	int total_ok=0;
	int total_warning=0;
	int total_unknown=0;
	int total_critical=0;
	int total_pending=0;
//	statusdata_h.servicestatus temp_servicestatus;
	statusdata_h.hoststatus temp_hoststatus;
	objects_h.host temp_host;


	/* check all services... */
	for ( statusdata_h.servicestatus temp_servicestatus : (ArrayList<statusdata_h.servicestatus>)  statusdata.servicestatus_list ) {

		/* find the host this service is associated with */
		temp_host=objects.find_host(temp_servicestatus.host_name);
		if(temp_host==null)
			continue;

		/* see if this service is associated with a host in the specified hostgroup */
		if(objects.is_host_member_of_hostgroup(temp_hostgroup,temp_host)==common_h.FALSE)
			continue;

		/* find the status of the associated host */
		temp_hoststatus=statusdata.find_hoststatus(temp_servicestatus.host_name);
		if(temp_hoststatus==null)
			continue;

		/* make sure we only display hosts of the specified status levels */
		if(0==(host_status_types & temp_hoststatus.status))
			continue;

		/* make sure we only display hosts that have the desired properties */
		if(passes_host_properties_filter(temp_hoststatus)==common_h.FALSE)
			continue;

		/* make sure we only display services of the specified status levels */
		if(0==(service_status_types & temp_servicestatus.status))
			continue;

		/* make sure we only display services that have the desired properties */
		if(passes_service_properties_filter(temp_servicestatus)==common_h.FALSE)
			continue;

		if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
			total_critical++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
			total_warning++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
			total_unknown++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
			total_ok++;
		else if(temp_servicestatus.status==statusdata_h.SERVICE_PENDING)
			total_pending++;
		else
			total_ok++;
	        }


	System.out.printf("<TABLE BORDER=0>\n");

	if(total_ok>0)
		System.out.printf("<TR><TD CLASS='miniStatusOK'><A HREF='%s?hostgroup=%s&style=detail&&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d OK</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.SERVICE_OK,host_status_types,service_properties,host_properties,total_ok);
	if(total_warning>0)
		System.out.printf("<TR><TD CLASS='miniStatusWARNING'><A HREF='%s?hostgroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d WARNING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.SERVICE_WARNING,host_status_types,service_properties,host_properties,total_warning);
	if(total_unknown>0)
		System.out.printf("<TR><TD CLASS='miniStatusUNKNOWN'><A HREF='%s?hostgroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d UNKNOWN</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.SERVICE_UNKNOWN,host_status_types,service_properties,host_properties,total_unknown);
	if(total_critical>0)
		System.out.printf("<TR><TD CLASS='miniStatusCRITICAL'><A HREF='%s?hostgroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d CRITICAL</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.SERVICE_CRITICAL,host_status_types,service_properties,host_properties,total_critical);
	if(total_pending>0)
		System.out.printf("<TR><TD CLASS='miniStatusPENDING'><A HREF='%s?hostgroup=%s&style=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d&hostprops=%d'>%d PENDING</A></TD></TR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),statusdata_h.SERVICE_PENDING,host_status_types,service_properties,host_properties,total_pending);

	System.out.printf("</TABLE>\n");

	if((total_ok + total_warning + total_unknown + total_critical + total_pending)==0)
		System.out.printf("No matching services");

	return;
        }



/* show a grid layout of hostgroup(s)... */
public static void show_hostgroup_grids(){
	int user_has_seen_something=common_h.FALSE;
	int hostgroup_error=common_h.FALSE;
	int odd=0;


	System.out.printf("<P>\n");

	System.out.printf("<table border=0 width=100%%>\n");
	System.out.printf("<tr>\n");

	System.out.printf("<td valign=top align=left width=33%%>\n");

	show_filters();

	System.out.printf("</td>");

	System.out.printf("<td valign=top align=center width=33%%>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusTitle'>Status Grid For ");
	if(show_all_hostgroups==common_h.TRUE)
		System.out.printf("All Host Groups");
	else
		System.out.printf("Host Group '%s'",hostgroup_name);
	System.out.printf("</DIV>\n");

	System.out.printf("<br>");

	System.out.printf("</td>\n");

	System.out.printf("<td valign=top align=right width=33%%></td>\n");
	
	System.out.printf("</tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</P>\n");


	/* display status grids for all hostgroups */
	if(show_all_hostgroups==common_h.TRUE){

		/* loop through all hostgroups... */
		for ( objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>)  objects.hostgroup_list ) {

			/* make sure the user is authorized to view this hostgroup */
			if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,/* TODO & */ current_authdata)==common_h.FALSE)
				continue;

			if(odd==0)
				odd=1;
			else
				odd=0;

			/* show grid for this hostgroup */
			show_hostgroup_grid(temp_hostgroup);

			user_has_seen_something=common_h.TRUE;
		        }

	        }

	/* else just show grid for a specific hostgroup */
	else{
        objects_h.hostgroup temp_hostgroup=objects.find_hostgroup(hostgroup_name);
		if(temp_hostgroup==null)
			hostgroup_error=common_h.TRUE;
		else{
			show_hostgroup_grid(temp_hostgroup);
			user_has_seen_something=common_h.TRUE;
		        }
	        }

	/* if user couldn't see anything, print out some helpful info... */
	if(user_has_seen_something==common_h.FALSE && hostgroup_error==common_h.FALSE){

		System.out.printf("<P><DIV ALIGN=CENTER>\n");

		if(statusdata.hoststatus_list!=null && !statusdata.hoststatus_list.isEmpty()){
			System.out.printf("<DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for any of the hosts you requested...</DIV>\n");
			System.out.printf("<DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
			System.out.printf("and check the authorization options in your CGI configuration file.</DIV>\n");
		        }
		else{
			System.out.printf("<DIV CLASS='infoMessage'>There doesn't appear to be any host status information in the status log...<br><br>\n");
			System.out.printf("Make sure that Blue is running and that you have specified the location of you status log correctly in the configuration files.</DIV>\n");
		        }

		System.out.printf("</DIV></P>\n");
	        }

	/* we couldn't find the hostgroup */
	else if(hostgroup_error==common_h.TRUE){
		System.out.printf("<P><DIV ALIGN=CENTER>\n");
		System.out.printf("<DIV CLASS='errorMessage'>Sorry, but hostgroup '%s' doesn't seem to exist...</DIV>\n",hostgroup_name);
		System.out.printf("</DIV></P>\n");
	        }

	return;
        }


/* displays status grid for a specific hostgroup */
public static void show_hostgroup_grid(objects_h.hostgroup temp_hostgroup){
//	objects_h.hostgroupmember temp_member;
	String status_bg_class="";
	String host_status_class="";
	String service_status_class="";
	objects_h.host temp_host;
//	objects_h.service temp_service;
	statusdata_h.hoststatus temp_hoststatus;
	statusdata_h.servicestatus temp_servicestatus;
	objects_h.hostextinfo temp_hostextinfo;
	int odd=0;
	int current_item;


	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<DIV CLASS='status'><A HREF='%s?hostgroup=%s&style=detail'>%s</A>",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),temp_hostgroup.alias);
	System.out.printf(" (<A HREF='%s?type=%d&hostgroup=%s'>%s</A>)</DIV>",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOSTGROUP_INFO,cgiutils.url_encode(temp_hostgroup.group_name),temp_hostgroup.group_name);

	System.out.printf("<TABLE BORDER=1 CLASS='status' ALIGN=CENTER>\n");
	System.out.printf("<TR><TH CLASS='status'>Host</TH><TH CLASS='status'>Services</a></TH><TH CLASS='status'>Actions</TH></TR>\n");

	/* find all the hosts that belong to the hostgroup */
	for ( objects_h.hostgroupmember temp_member : (ArrayList<objects_h.hostgroupmember>)  temp_hostgroup.members ) {

		/* find the host... */
		temp_host=objects.find_host(temp_member.host_name);
		if(temp_host==null)
			continue;

		/* find the host status */
		temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
		if(temp_hoststatus==null)
			continue;

		if(odd==1){
			status_bg_class="Even";
			odd=0;
		        }
		else{
			status_bg_class="Odd";
			odd=1;
		        }

		System.out.printf("<TR CLASS='status%s'>\n",status_bg_class);

		/* get the status of the host */
		if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			host_status_class="HOSTDOWN";
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			host_status_class="HOSTUNREACHABLE";
		else
			host_status_class=status_bg_class;

		System.out.printf("<TD CLASS='status%s'>",host_status_class);

		System.out.printf("<TABLE BORDER=0 WIDTH='100%%' cellpadding=0 cellspacing=0>\n");
		System.out.printf("<TR>\n");
		System.out.printf("<TD ALIGN=LEFT>\n");
		System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
		System.out.printf("<TR>\n");
		System.out.printf("<TD align=left valign=center CLASS='status%s'>",host_status_class);
		System.out.printf("<A HREF='%s?type=%d&host=%s'>%s</A>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name),temp_host.name);
		System.out.printf("</TD>\n");
		System.out.printf("</TR>\n");
		System.out.printf("</TABLE>\n");
		System.out.printf("</TD>\n");
		System.out.printf("<TD align=right valign=center nowrap>\n");
		System.out.printf("<TABLE BORDER=0 cellpadding=0 cellspacing=0>\n");
		System.out.printf("<TR>\n");

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo!=null){
			if(temp_hostextinfo.icon_image!=null){
				System.out.printf("<TD align=center valign=center>");
				System.out.printf("<A HREF='%s?type=%d&host=%s'>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name));
                System.out.printf("<IMG SRC='%s",cgiutils.url_logo_images_path);
                cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.icon_image);
                System.out.printf("' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt);
                System.out.printf("</A>");
				System.out.printf("<TD>\n");
			        }
		        }
		System.out.printf("<TD>\n");

		System.out.printf("</TR>\n");
		System.out.printf("</TABLE>\n");
		System.out.printf("</TD>\n");
		System.out.printf("</TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</TD>\n");

		System.out.printf("<TD CLASS='status%s'>",host_status_class);

		/* display all services on the host */
		current_item=1;
		for ( objects_h.service temp_service : (ArrayList<objects_h.service>)  objects.service_list ) {

			/* skip this service if it's not associate with the host */
			if(!temp_service.host_name.equals(temp_host.name))
				continue;

			if(current_item>max_grid_width && max_grid_width>0){
				System.out.printf("<BR>\n");
				current_item=1;
			        }

			/* get the status of the service */
			temp_servicestatus=statusdata.find_servicestatus(temp_service.host_name,temp_service.description);
			if(temp_servicestatus==null)
				service_status_class="null";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_OK)
				service_status_class="OK";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_WARNING)
				service_status_class="WARNING";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_UNKNOWN)
				service_status_class="UNKNOWN";
			else if(temp_servicestatus.status==statusdata_h.SERVICE_CRITICAL)
				service_status_class="CRITICAL";
			else
				service_status_class="PENDING";

			System.out.printf("<A HREF='%s?type=%d&host=%s",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_servicestatus.host_name));
			System.out.printf("&service=%s' CLASS='status%s'>%s</A>&nbsp;",cgiutils.url_encode(temp_servicestatus.description),service_status_class,temp_servicestatus.description);

			current_item++;
		        }

		System.out.printf("</TD>\n");

		/* actions */
		System.out.printf("<TD CLASS='status%s'>",host_status_class);

		System.out.printf("<A HREF='%s?type=%d&host=%s'>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name));
		System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.DETAIL_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extended Information For This Host","View Extended Information For This Host");
		System.out.printf("</A>");

		if(temp_hostextinfo!=null){
			if(temp_hostextinfo.notes_url!=null){
				System.out.printf("<A HREF='");
				cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.notes_url);
				System.out.printf("' TARGET='_blank'>");
				System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.NOTES_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"View Extra Host Notes","View Extra Host Notes");
				System.out.printf("</A>");
			        }
			if(temp_hostextinfo.action_url!=null){
				System.out.printf("<A HREF='");
				cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.action_url);
				System.out.printf("' TARGET='_blank'>");
				System.out.printf("<IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'>",cgiutils.url_images_path,cgiutils_h.ACTION_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Perform Extra Host Actions","Perform Extra Host Actions");
				System.out.printf("</A>");
			        }
		        }

		System.out.printf("<a href='%s?host=%s'><img src='%s%s' border=0 alt='View Service Details For This Host' title='View Service Details For This Host'></a>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_host.name),cgiutils.url_images_path,cgiutils_h.STATUS_DETAIL_ICON);

		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 WIDTH=%d HEIGHT=%d ALT='%s' TITLE='%s'></A>",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(temp_host.name),cgiutils.url_images_path,cgiutils_h.STATUSMAP_ICON,cgiutils_h.STATUS_ICON_WIDTH,cgiutils_h.STATUS_ICON_HEIGHT,"Locate Host On Map","Locate Host On Map");

		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
		}

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");

	return;
        }




/******************************************************************/
/**********  SERVICE SORTING & FILTERING FUNCTIONS  ***************/
/******************************************************************/


/* sorts the service list */
public static int sort_services(int s_type, int s_option){

	if(s_type==cgiutils_h.SORT_NONE)
		return common_h.ERROR;

	if(statusdata.servicestatus_list==null)
		return common_h.ERROR;

	/* sort all services status entries */
    Collections.sort( statusdata.servicestatus_list, new service_comparator( s_type, s_option ));
    
	return common_h.OK;
        }


public static class service_comparator implements Comparator<statusdata_h.servicestatus> {
    int s_type;
    int s_option;
    
    public service_comparator( int type, int option ) {
        s_type = type;
        s_option = option;
    }
    
    public int compare( statusdata_h.servicestatus new_svcstatus, statusdata_h.servicestatus temp_svcstatus){
	long nt;
	long tt;

	if(s_type==cgiutils_h.SORT_ASCENDING){

		if(s_option==cgiutils_h.SORT_LASTCHECKTIME){
			if(new_svcstatus.last_check < temp_svcstatus.last_check)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_CURRENTATTEMPT){
			if(new_svcstatus.current_attempt < temp_svcstatus.current_attempt)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_SERVICESTATUS){
			if(new_svcstatus.status <= temp_svcstatus.status)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_HOSTNAME){
            if ( new_svcstatus.host_name.compareToIgnoreCase(temp_svcstatus.host_name) < 0 )
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_SERVICENAME){
            if ( new_svcstatus.description.compareToIgnoreCase(temp_svcstatus.description) < 0 )
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_STATEDURATION){
			if(new_svcstatus.last_state_change==0)
				nt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				nt=(new_svcstatus.last_state_change>current_time)?0:(current_time-new_svcstatus.last_state_change);
			if(temp_svcstatus.last_state_change==0)
				tt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				tt=(temp_svcstatus.last_state_change>current_time)?0:(current_time-temp_svcstatus.last_state_change);
			if(nt<tt)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
	        }
	else{
		if(s_option==cgiutils_h.SORT_LASTCHECKTIME){
			if(new_svcstatus.last_check > temp_svcstatus.last_check)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_CURRENTATTEMPT){
			if(new_svcstatus.current_attempt > temp_svcstatus.current_attempt)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_SERVICESTATUS){
			if(new_svcstatus.status > temp_svcstatus.status)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_HOSTNAME){
            
            if ( new_svcstatus.host_name.compareToIgnoreCase(temp_svcstatus.host_name) > 0 )
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_SERVICENAME){
            if ( new_svcstatus.description.compareToIgnoreCase(temp_svcstatus.description) > 0 )
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_STATEDURATION){
			if(new_svcstatus.last_state_change==0)
				nt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				nt=(new_svcstatus.last_state_change>current_time)?0:(current_time-new_svcstatus.last_state_change);
			if(temp_svcstatus.last_state_change==0)
				tt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				tt=(temp_svcstatus.last_state_change>current_time)?0:(current_time-temp_svcstatus.last_state_change);
			if(nt>tt)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
	        }

	return common_h.TRUE;
        }
}

/* sorts the host list */
public static int sort_hosts(int s_type, int s_option){

	if(s_type==cgiutils_h.SORT_NONE)
		return common_h.ERROR;

	if(statusdata.hoststatus_list==null)
		return common_h.ERROR;

	/* sort all hosts status entries */
    Collections.sort( statusdata.hoststatus_list, new host_comparator( s_type, s_option ));

	return common_h.OK;
        }


public static class host_comparator implements Comparator<statusdata_h.hoststatus> {
    int s_type;
    int s_option;
    
    public host_comparator( int type, int option ) {
        s_type = type;
        s_option = option;
    }
    
    public int compare( statusdata_h.hoststatus new_hststatus, statusdata_h.hoststatus temp_hststatus){
	long nt;
	long tt;

	if(s_type==cgiutils_h.SORT_ASCENDING){

		if(s_option==cgiutils_h.SORT_LASTCHECKTIME){
			if(new_hststatus.last_check < temp_hststatus.last_check)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_HOSTSTATUS){
			if(new_hststatus.status <= temp_hststatus.status)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_HOSTNAME){
			if(new_hststatus.host_name.compareToIgnoreCase(temp_hststatus.host_name)<0)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_STATEDURATION){
			if(new_hststatus.last_state_change==0)
				nt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				nt=(new_hststatus.last_state_change>current_time)?0:(current_time-new_hststatus.last_state_change);
			if(temp_hststatus.last_state_change==0)
				tt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				tt=(temp_hststatus.last_state_change>current_time)?0:(current_time-temp_hststatus.last_state_change);
			if(nt<tt)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
	        }
	else{
		if(s_option==cgiutils_h.SORT_LASTCHECKTIME){
			if(new_hststatus.last_check > temp_hststatus.last_check)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_HOSTSTATUS){
			if(new_hststatus.status > temp_hststatus.status)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_HOSTNAME){
			if(new_hststatus.host_name.compareToIgnoreCase(temp_hststatus.host_name)>0)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
		else if(s_option==cgiutils_h.SORT_STATEDURATION){
			if(new_hststatus.last_state_change==0)
				nt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				nt=(new_hststatus.last_state_change>current_time)?0:(current_time-new_hststatus.last_state_change);
			if(temp_hststatus.last_state_change==0)
				tt=(blue.program_start>current_time)?0:(current_time-blue.program_start);
			else
				tt=(temp_hststatus.last_state_change>current_time)?0:(current_time-temp_hststatus.last_state_change);
			if(nt>tt)
				return common_h.TRUE;
			else
				return common_h.FALSE;
		        }
	        }

	return common_h.TRUE;
        }
}

/* check host properties filter */
public static int passes_host_properties_filter(statusdata_h.hoststatus temp_hoststatus){

	if(((host_properties & cgiutils_h.HOST_SCHEDULED_DOWNTIME)>0) && temp_hoststatus.scheduled_downtime_depth<=0)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_NO_SCHEDULED_DOWNTIME)>0) && temp_hoststatus.scheduled_downtime_depth>0)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_STATE_ACKNOWLEDGED)>0) && temp_hoststatus.problem_has_been_acknowledged==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_STATE_UNACKNOWLEDGED)>0) && temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_CHECKS_DISABLED)>0) && temp_hoststatus.checks_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_CHECKS_ENABLED)>0) && temp_hoststatus.checks_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_EVENT_HANDLER_DISABLED)>0) && temp_hoststatus.event_handler_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_EVENT_HANDLER_ENABLED)>0) && temp_hoststatus.event_handler_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_FLAP_DETECTION_DISABLED)>0) && temp_hoststatus.flap_detection_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_FLAP_DETECTION_ENABLED)>0) && temp_hoststatus.flap_detection_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_IS_FLAPPING)>0) && temp_hoststatus.is_flapping==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_IS_NOT_FLAPPING)>0) && temp_hoststatus.is_flapping==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_NOTIFICATIONS_DISABLED)>0) && temp_hoststatus.notifications_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_NOTIFICATIONS_ENABLED)>0) && temp_hoststatus.notifications_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_PASSIVE_CHECKS_DISABLED)>0) && temp_hoststatus.accept_passive_host_checks==common_h.TRUE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_PASSIVE_CHECKS_ENABLED)>0) && temp_hoststatus.accept_passive_host_checks==common_h.FALSE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_PASSIVE_CHECK)>0) && temp_hoststatus.check_type==common_h.HOST_CHECK_ACTIVE)
		return common_h.FALSE;

	if(((host_properties & cgiutils_h.HOST_ACTIVE_CHECK)>0) && temp_hoststatus.check_type==common_h.HOST_CHECK_PASSIVE)
		return common_h.FALSE;

	return common_h.TRUE;
        }



/* check service properties filter */
public static int passes_service_properties_filter(statusdata_h.servicestatus temp_servicestatus){

	if(((service_properties & cgiutils_h.SERVICE_SCHEDULED_DOWNTIME)>0) && temp_servicestatus.scheduled_downtime_depth<=0)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_NO_SCHEDULED_DOWNTIME)>0) && temp_servicestatus.scheduled_downtime_depth>0)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_STATE_ACKNOWLEDGED)>0) && temp_servicestatus.problem_has_been_acknowledged==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_STATE_UNACKNOWLEDGED)>0) && temp_servicestatus.problem_has_been_acknowledged==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_CHECKS_DISABLED)>0) && temp_servicestatus.checks_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_CHECKS_ENABLED)>0) && temp_servicestatus.checks_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_EVENT_HANDLER_DISABLED)>0) && temp_servicestatus.event_handler_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_EVENT_HANDLER_ENABLED)>0) && temp_servicestatus.event_handler_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_FLAP_DETECTION_DISABLED)>0) && temp_servicestatus.flap_detection_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_FLAP_DETECTION_ENABLED)>0) && temp_servicestatus.flap_detection_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_IS_FLAPPING)>0) && temp_servicestatus.is_flapping==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_IS_NOT_FLAPPING)>0) && temp_servicestatus.is_flapping==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_NOTIFICATIONS_DISABLED)>0) && temp_servicestatus.notifications_enabled==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_NOTIFICATIONS_ENABLED)>0) && temp_servicestatus.notifications_enabled==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_PASSIVE_CHECKS_DISABLED)>0) && temp_servicestatus.accept_passive_service_checks==common_h.TRUE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_PASSIVE_CHECKS_ENABLED)>0) && temp_servicestatus.accept_passive_service_checks==common_h.FALSE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_PASSIVE_CHECK)>0) && temp_servicestatus.check_type==common_h.SERVICE_CHECK_ACTIVE)
		return common_h.FALSE;

	if(((service_properties & cgiutils_h.SERVICE_ACTIVE_CHECK)>0) && temp_servicestatus.check_type==common_h.SERVICE_CHECK_PASSIVE)
		return common_h.FALSE;

	return common_h.TRUE;
        }



/* shows service and host filters in use */
public static void show_filters(){
	int found=0;

	/* show filters box if necessary */
	if(host_properties!=0L || service_properties!=0L || host_status_types!=all_host_status_types || service_status_types!=all_service_status_types){

		System.out.printf("<table border=1 class='filter' cellspacing=0 cellpadding=0>\n");
		System.out.printf("<tr><td class='filter'>\n");
		System.out.printf("<table border=0 cellspacing=2 cellpadding=0>\n");
		System.out.printf("<tr><td colspan=2 valign=top align=left CLASS='filterTitle'>Display Filters:</td></tr>");
		System.out.printf("<tr><td valign=top align=left CLASS='filterName'>Host Status Types:</td>");
		System.out.printf("<td valign=top align=left CLASS='filterValue'>");
		if(host_status_types==all_host_status_types)
			System.out.printf("All");
		else if(host_status_types==all_host_problems)
			System.out.printf("All problems");
		else{
			found=0;
			if((host_status_types & statusdata_h.HOST_PENDING)>0){
				System.out.printf(" Pending");
				found=1;
		                }
			if((host_status_types & statusdata_h.HOST_UP)>0){
				System.out.printf("%s Up",(found==1)?" |":"");
				found=1;
		                }
			if((host_status_types & statusdata_h.HOST_DOWN)>0){
				System.out.printf("%s Down",(found==1)?" |":"");
				found=1;
		                }
			if((host_status_types & statusdata_h.HOST_UNREACHABLE)>0)
				System.out.printf("%s Unreachable",(found==1)?" |":"");
	                }
		System.out.printf("</td></tr>");
		System.out.printf("<tr><td valign=top align=left CLASS='filterName'>Host Properties:</td>");
		System.out.printf("<td valign=top align=left CLASS='filterValue'>");
		if(host_properties==0)
			System.out.printf("Any");
		else{
			found=0;
			if((host_properties & cgiutils_h.HOST_SCHEDULED_DOWNTIME)>0){
				System.out.printf(" In Scheduled Downtime");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_NO_SCHEDULED_DOWNTIME)>0){
				System.out.printf("%s Not In Scheduled Downtime",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_STATE_ACKNOWLEDGED)>0){
				System.out.printf("%s Has Been Acknowledged",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_STATE_UNACKNOWLEDGED)>0){
				System.out.printf("%s Has Not Been Acknowledged",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_CHECKS_DISABLED)>0){
				System.out.printf("%s Checks Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_CHECKS_ENABLED)>0){
				System.out.printf("%s Checks Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_EVENT_HANDLER_DISABLED)>0){
				System.out.printf("%s Event Handler Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_EVENT_HANDLER_ENABLED)>0){
				System.out.printf("%s Event Handler Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_FLAP_DETECTION_DISABLED)>0){
				System.out.printf("%s Flap Detection Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_FLAP_DETECTION_ENABLED)>0){
				System.out.printf("%s Flap Detection Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_IS_FLAPPING)>0){
				System.out.printf("%s Is Flapping",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_IS_NOT_FLAPPING)>0){
				System.out.printf("%s Is Not Flapping",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_NOTIFICATIONS_DISABLED)>0){
				System.out.printf("%s Notifications Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_NOTIFICATIONS_ENABLED)>0){
				System.out.printf("%s Notifications Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_PASSIVE_CHECKS_DISABLED)>0){
				System.out.printf("%s Passive Checks Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_PASSIVE_CHECKS_ENABLED)>0){
				System.out.printf("%s Passive Checks Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_PASSIVE_CHECK)>0){
				System.out.printf("%s Passive Checks",(found==1)?" &amp;":"");
				found=1;
		                }
			if((host_properties & cgiutils_h.HOST_ACTIVE_CHECK)>0){
				System.out.printf("%s Active Checks",(found==1)?" &amp;":"");
				found=1;
		                }
	                }
		System.out.printf("</td>");
		System.out.printf("</tr>\n");


		System.out.printf("<tr><td valign=top align=left CLASS='filterName'>Service Status Types:</td>");
		System.out.printf("<td valign=top align=left CLASS='filterValue'>");
		if(service_status_types==all_service_status_types)
			System.out.printf("All");
		else if(service_status_types==all_service_problems)
			System.out.printf("All Problems");
		else{
			found=0;
			if((service_status_types & statusdata_h.SERVICE_PENDING)>0){
				System.out.printf(" Pending");
				found=1;
		                }
			if((service_status_types & statusdata_h.SERVICE_OK)>0){
				System.out.printf("%s Ok",(found==1)?" |":"");
				found=1;
		                }
			if((service_status_types & statusdata_h.SERVICE_UNKNOWN)>0){
				System.out.printf("%s Unknown",(found==1)?" |":"");
				found=1;
		                }
			if((service_status_types & statusdata_h.SERVICE_WARNING)>0){
				System.out.printf("%s Warning",(found==1)?" |":"");
				found=1;
		                }
			if((service_status_types & statusdata_h.SERVICE_CRITICAL)>0){
				System.out.printf("%s Critical",(found==1)?" |":"");
				found=1;
		                }
	                }
		System.out.printf("</td></tr>");
		System.out.printf("<tr><td valign=top align=left CLASS='filterName'>Service Properties:</td>");
		System.out.printf("<td valign=top align=left CLASS='filterValue'>");
		if(service_properties==0)
			System.out.printf("Any");
		else{
			found=0;
			if((service_properties & cgiutils_h.SERVICE_SCHEDULED_DOWNTIME)>0){
				System.out.printf(" In Scheduled Downtime");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_NO_SCHEDULED_DOWNTIME)>0){
				System.out.printf("%s Not In Scheduled Downtime",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_STATE_ACKNOWLEDGED)>0){
				System.out.printf("%s Has Been Acknowledged",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_STATE_UNACKNOWLEDGED)>0){
				System.out.printf("%s Has Not Been Acknowledged",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_CHECKS_DISABLED)>0){
				System.out.printf("%s Active Checks Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_CHECKS_ENABLED)>0){
				System.out.printf("%s Active Checks Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_EVENT_HANDLER_DISABLED)>0){
				System.out.printf("%s Event Handler Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_EVENT_HANDLER_ENABLED)>0){
				System.out.printf("%s Event Handler Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_FLAP_DETECTION_DISABLED)>0){
				System.out.printf("%s Flap Detection Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_FLAP_DETECTION_ENABLED)>0){
				System.out.printf("%s Flap Detection Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_IS_FLAPPING)>0){
				System.out.printf("%s Is Flapping",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_IS_NOT_FLAPPING)>0){
				System.out.printf("%s Is Not Flapping",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_NOTIFICATIONS_DISABLED)>0){
				System.out.printf("%s Notifications Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_NOTIFICATIONS_ENABLED)>0){
				System.out.printf("%s Notifications Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_PASSIVE_CHECKS_DISABLED)>0){
				System.out.printf("%s Passive Checks Disabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_PASSIVE_CHECKS_ENABLED)>0){
				System.out.printf("%s Passive Checks Enabled",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_PASSIVE_CHECK)>0){
				System.out.printf("%s Passive Checks",(found==1)?" &amp;":"");
				found=1;
		                }
			if((service_properties & cgiutils_h.SERVICE_ACTIVE_CHECK)>0){
				System.out.printf("%s Active Checks",(found==1)?" &amp;":"");
				found=1;
		                }
	                }
		System.out.printf("</td></tr>");
		System.out.printf("</table>\n");

		System.out.printf("</td></tr>");
		System.out.printf("</table>\n");
	        }

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

}