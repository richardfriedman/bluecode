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
import java.util.Iterator;

import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.comments;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.include.downtime_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;


public class extinfo extends blue_servlet { 
    
public static final int MAX_MESSAGE_BUFFER		= 4096;

public static final int HEALTH_WARNING_PERCENTAGE       = 85;
public static final int HEALTH_CRITICAL_PERCENTAGE      = 75;

/* SORTDATA structure */
public static class sortdata {
	int is_service;
    statusdata_h.servicestatus svcstatus;
	statusdata_h.hoststatus hststatus;
    
    public sortdata( int _is_service, statusdata_h.servicestatus _svcstatus, statusdata_h.hoststatus _hststatus) {
        is_service = _is_service;
        svcstatus = _svcstatus;
        hststatus = _hststatus;
    }
}

public static cgiauth_h.authdata current_authdata;

public static ArrayList<sortdata> sortdata_list= new ArrayList<sortdata>();

public static String host_name="";
public static String hostgroup_name="";
public static String servicegroup_name="";
public static String service_desc="";

public static int display_type=cgiutils_h.DISPLAY_PROCESS_INFO;

public static int sort_type=cgiutils_h.SORT_ASCENDING;
public static int sort_option=cgiutils_h.SORT_NEXTCHECKTIME;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;

public void reset_context() {
   current_authdata = new cgiauth_h.authdata ();
   sortdata_list.clear();
   
   host_name="";
   hostgroup_name="";
   servicegroup_name="";
   service_desc="";

   display_type=cgiutils_h.DISPLAY_PROCESS_INFO;

   sort_type=cgiutils_h.SORT_ASCENDING;
   sort_option=cgiutils_h.SORT_NEXTCHECKTIME;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;   
}

public void call_main() {
   main( null );
}

public static void main(String[] args ){
	int result= common_h.OK;
	int found=common_h.FALSE;
	String temp_buffer;
	objects_h.hostextinfo temp_hostextinfo=null;
	objects_h.serviceextinfo temp_serviceextinfo=null;
    objects_h.host temp_host=null;
    objects_h.hostgroup temp_hostgroup=null;
    objects_h.service temp_service=null;
    objects_h.servicegroup temp_servicegroup=null;
	

	/* get the arguments passed in the URL */
	process_cgivars();

	/* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.cgi_config_file_error(cgiutils.main_config_file);
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

	/* read all status data */
	result=cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(),statusdata_h.READ_ALL_STATUS_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.status_data_error();
		document_footer();
		cgiutils.exit( common_h.ERROR);
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

		if(display_type==cgiutils_h.DISPLAY_HOST_INFO)
			temp_buffer = "Host Information";
		else if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO)
			temp_buffer = "Service Information";
		else if(display_type==cgiutils_h.DISPLAY_COMMENTS)
			temp_buffer = "All Host and Service Comments";
		else if(display_type==cgiutils_h.DISPLAY_PERFORMANCE)
			temp_buffer = "Performance Information";
		else if(display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO)
			temp_buffer = "Hostgroup Information";
		else if(display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO)
			temp_buffer = "Servicegroup Information";
		else if(display_type==cgiutils_h.DISPLAY_DOWNTIME)
			temp_buffer = "All Host and Service Scheduled Downtime";
		else if(display_type==cgiutils_h.DISPLAY_SCHEDULING_QUEUE)
			temp_buffer = "Check Scheduling Queue";
		else
			temp_buffer = "Blue Process Information";
		cgiutils.display_info_table(temp_buffer,common_h.TRUE,current_authdata);

		/* find the host */
		if(display_type== cgiutils_h.DISPLAY_HOST_INFO || display_type==cgiutils_h.DISPLAY_SERVICE_INFO){

			temp_host=objects.find_host(host_name);
			if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO)
				temp_service=objects.find_service(host_name,service_desc);

			/* write some Javascript helper functions */
			if(temp_host!=null){
				System.out.printf("<SCRIPT LANGUAGE=\"JavaScript\">\n<!--\n");
				System.out.printf("function nagios_get_host_name()\n{\n");
				System.out.printf("return \"%s\";\n",temp_host.name);
				System.out.printf("}\n");
				System.out.printf("function nagios_get_host_address()\n{\n");
				System.out.printf("return \"%s\";\n",temp_host.address);
				System.out.printf("}\n");
				if(temp_service!=null){
					System.out.printf("function nagios_get_service_description()\n{\n");
					System.out.printf("return \"%s\";\n",temp_service.description);
					System.out.printf("}\n");
				        }
				System.out.printf("//-->\n</SCRIPT>\n");
			        }
		        }

		/* find the hostgroup */
		else if(display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO)
			temp_hostgroup=objects.find_hostgroup(hostgroup_name);

		/* find the servicegroup */
		else if(display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO)
			temp_servicegroup=objects.find_servicegroup(servicegroup_name);

		if(((display_type==cgiutils_h.DISPLAY_HOST_INFO || display_type==cgiutils_h.DISPLAY_SERVICE_INFO) && temp_host!=null) || (display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO && temp_hostgroup!=null) || (display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO && temp_servicegroup!=null)){
			System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
			System.out.printf("<TR><TD CLASS='linkBox'>\n");
			if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO)
				System.out.printf("<A HREF='%s?type=%d&host=%s'>View Information For This Host</A><br>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(host_name));
			if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO || display_type==cgiutils_h.DISPLAY_HOST_INFO)
				System.out.printf("<A HREF='%s?host=%s'>View Status Detail For This Host</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(host_name));
			if(display_type==cgiutils_h.DISPLAY_HOST_INFO){
				System.out.printf("<A HREF='%s?host=%s'>View Alert History For This Host</A><BR>\n",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
//#ifdef USE_TRENDS
				System.out.printf("<A HREF='%s?host=%s'>View Trends For This Host</A><BR>\n",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
//#endif
//#ifdef USE_HISTOGRAM
				System.out.printf("<A HREF='%s?host=%s'>View Alert Histogram For This Host</A><BR>\n",cgiutils_h.HISTOGRAM_CGI,cgiutils.url_encode(host_name));
//#endif
				System.out.printf("<A HREF='%s?host=%s&show_log_entries'>View Availability Report For This Host</A><BR>\n",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(host_name));
				System.out.printf("<A HREF='%s?host=%s'>View Notifications This Host</A>\n",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
		                }
			else if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO){
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s'>View Alert History For This Service</A><BR>\n",cgiutils.url_encode(service_desc));
//#ifdef USE_TRENDS
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s'>View Trends For This Service</A><BR>\n",cgiutils.url_encode(service_desc));
//#endif
//#ifdef USE_HISTOGRAM
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.HISTOGRAM_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s'>View Alert Histogram For This Service</A><BR>\n",cgiutils.url_encode(service_desc));
//#endif
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s&show_log_entries'>View Availability Report For This Service</A><BR>\n",cgiutils.url_encode(service_desc));
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s'>View Notifications For This Service</A>\n",cgiutils.url_encode(service_desc));
		                }
			else if(display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO){
				System.out.printf("<A HREF='%s?hostgroup=%s&style=detail'>View Status Detail For This Hostgroup</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				System.out.printf("<A HREF='%s?hostgroup=%s&style=overview'>View Status Overview For This Hostgroup</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				System.out.printf("<A HREF='%s?hostgroup=%s&style=grid'>View Status Grid For This Hostgroup</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(hostgroup_name));
				System.out.printf("<A HREF='%s?hostgroup=%s'>View Availability For This Hostgroup</A><BR>\n",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(hostgroup_name));
		                }
			else if(display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO){
				System.out.printf("<A HREF='%s?servicegroup=%s&style=detail'>View Status Detail For This Servicegroup</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));
				System.out.printf("<A HREF='%s?servicegroup=%s&style=overview'>View Status Overview For This Servicegroup</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));
				System.out.printf("<A HREF='%s?servicegroup=%s&style=grid'>View Status Grid For This Servicegroup</A><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(servicegroup_name));
				System.out.printf("<A HREF='%s?servicegroup=%s'>View Availability For This Servicegroup</A><BR>\n",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(servicegroup_name));
		                }
			System.out.printf("</TD></TR>\n");
			System.out.printf("</TABLE>\n");
	                }

		System.out.printf("</td>\n");

		/* middle column of top row */
		System.out.printf("<td align=center valign=center width=33%%>\n");

		if(((display_type==cgiutils_h.DISPLAY_HOST_INFO || display_type==cgiutils_h.DISPLAY_SERVICE_INFO) && temp_host!=null) || (display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO && temp_hostgroup!=null) || (display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO && temp_servicegroup!=null)){

			if(display_type==cgiutils_h.DISPLAY_HOST_INFO){
				System.out.printf("<DIV CLASS='data'>Host</DIV>\n");
				System.out.printf("<DIV CLASS='dataTitle'>%s</DIV>\n",temp_host.alias);
				System.out.printf("<DIV CLASS='dataTitle'>(%s)</DIV><BR>\n",temp_host.name);
				System.out.printf("<DIV CLASS='data'>Member of</DIV><DIV CLASS='dataTitle'>");
				for( Iterator iter = objects.hostgroup_list.iterator(); iter.hasNext(); ){
                    temp_hostgroup = (objects_h.hostgroup)iter.next();
					if( objects.is_host_member_of_hostgroup(temp_hostgroup,temp_host)==common_h.TRUE){
						if(found==common_h.TRUE)
							System.out.printf(", ");	
						System.out.printf("<A HREF='%s?hostgroup=%s&style=overview'>%s</A>",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostgroup.group_name),temp_hostgroup.group_name);
						found=common_h.TRUE;
						}
					}
			
				if(found==common_h.FALSE)
					System.out.printf("No hostgroups");
				System.out.printf("</DIV><BR>\n");
				System.out.printf("<DIV CLASS='data'>%s</DIV>\n",temp_host.address);
			        }
			if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO){
				System.out.printf("<DIV CLASS='data'>Service</DIV><DIV CLASS='dataTitle'>%s</DIV><DIV CLASS='data'>On Host</DIV>\n",service_desc);
				System.out.printf("<DIV CLASS='dataTitle'>%s</DIV>\n",temp_host.alias);
				System.out.printf("<DIV CLASS='dataTitle'>(<A HREF='%s?type=%d&host=%s'>%s</a>)</DIV><BR>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_host.name),temp_host.name);
				System.out.printf("<DIV CLASS='data'>Member of</DIV><DIV CLASS='dataTitle'>");
				for(Iterator iter = objects.servicegroup_list.iterator(); iter.hasNext();  ) {
                    temp_servicegroup = (objects_h.servicegroup) iter.next();
				    if( objects.is_service_member_of_servicegroup(temp_servicegroup,temp_service)==common_h.TRUE){
				        if(found==common_h.TRUE)
				            System.out.printf(", ");
				        System.out.printf("<A HREF='%s?servicegroup=%s&style=overview'>%s</A>",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_servicegroup.group_name),temp_servicegroup.group_name);
				        found=common_h.TRUE;
				    }
				}
				
				if(found==common_h.FALSE)
				    System.out.printf("No servicegroups.");
				System.out.printf("</DIV><BR>\n");
				
				System.out.printf("<DIV CLASS='data'>%s</DIV>\n",temp_host.address);
			}
			if(display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO){
			    System.out.printf("<DIV CLASS='data'>Hostgroup</DIV>\n");
			    System.out.printf("<DIV CLASS='dataTitle'>%s</DIV>\n",temp_hostgroup.alias);
			    System.out.printf("<DIV CLASS='dataTitle'>(%s)</DIV>\n",temp_hostgroup.group_name);
			}
			if(display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO){
			    System.out.printf("<DIV CLASS='data'>Servicegroup</DIV>\n");
				System.out.printf("<DIV CLASS='dataTitle'>%s</DIV>\n",temp_servicegroup.alias);
				System.out.printf("<DIV CLASS='dataTitle'>(%s)</DIV>\n",temp_servicegroup.group_name);
			        }

			if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO){
				temp_serviceextinfo=objects.find_serviceextinfo(host_name,service_desc);
				if(temp_serviceextinfo!=null){
                    if(temp_serviceextinfo.icon_image!=null){
                        System.out.printf("<img src='%s",cgiutils.url_logo_images_path);
                        cgiutils.print_extra_service_url(temp_serviceextinfo.host_name,temp_serviceextinfo.description,temp_serviceextinfo.icon_image);
                        System.out.printf("' border=0 alt='%s' title='%s'><BR CLEAR=ALL>",(temp_serviceextinfo.icon_image_alt==null)?"":temp_serviceextinfo.icon_image_alt,(temp_serviceextinfo.icon_image_alt==null)?"":temp_serviceextinfo.icon_image_alt);
                        }
					if(temp_serviceextinfo.icon_image_alt!=null)
						System.out.printf("<font size=-1><i>( %s )</i><font>\n",temp_serviceextinfo.icon_image_alt);
					if(temp_serviceextinfo.notes!=null)
						System.out.printf("<p>%s</p>\n",temp_serviceextinfo.notes);
				        }
			        }

			if(display_type==cgiutils_h.DISPLAY_HOST_INFO){
				temp_hostextinfo=objects.find_hostextinfo(host_name);
				if(temp_hostextinfo!=null){
                    if(temp_hostextinfo.icon_image!=null){
                        System.out.printf("<img src='%s",cgiutils.url_logo_images_path);
                        cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.icon_image);
                        System.out.printf("' border=0 alt='%s' title='%s'><BR CLEAR=ALL>",(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt,(temp_hostextinfo.icon_image_alt==null)?"":temp_hostextinfo.icon_image_alt);
                        }
					if(temp_hostextinfo.icon_image_alt!=null)
						System.out.printf("<font size=-1><i>( %s )</i><font>\n",temp_hostextinfo.icon_image_alt);
					if(temp_hostextinfo.notes!=null)
						System.out.printf("<p>%s</p>\n",temp_hostextinfo.notes);
				        }
		                }
 	                }

		System.out.printf("</td>\n");

		/* right column of top row */
		System.out.printf("<td align=right valign=bottom width=33%%>\n");

		if(display_type==cgiutils_h.DISPLAY_HOST_INFO){
			if(temp_hostextinfo!=null){
				System.out.printf("<TABLE BORDER='0'>\n");
				if(temp_hostextinfo.action_url!=null && !temp_hostextinfo.action_url.equals("")){
					System.out.printf("<TR><TD ALIGN='right'>\n");
					System.out.printf("<A HREF='");
					cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.action_url);
					System.out.printf("' TARGET='_blank'><img src='%s%s' border=0 alt='Perform Additional Actions On This Host' title='Perform Additional Actions On This Host'></A>\n",cgiutils.url_images_path,cgiutils_h.ACTION_ICON);
					System.out.printf("<BR CLEAR=ALL><FONT SIZE=-1><I>Extra Host Actions</I></FONT><BR CLEAR=ALL><BR CLEAR=ALL>\n");
					System.out.printf("</TD></TR>\n");
				        }
				if(temp_hostextinfo.notes_url!=null && ! temp_hostextinfo.notes_url.equals("")){
					System.out.printf("<TR><TD ALIGN='right'>\n");
					System.out.printf("<A HREF='");
					cgiutils.print_extra_host_url(temp_hostextinfo.host_name,temp_hostextinfo.notes_url);
					System.out.printf("' TARGET='_blank'><img src='%s%s' border=0 alt='View Additional Notes For This Host' title='View Additional Notes For This Host'></A>\n",cgiutils.url_images_path,cgiutils_h.NOTES_ICON);
					System.out.printf("<BR CLEAR=ALL><FONT SIZE=-1><I>Extra Host Notes</I></FONT><BR CLEAR=ALL><BR CLEAR=ALL>\n");
					System.out.printf("</TD></TR>\n");
				        }
				System.out.printf("</TABLE>\n");
		                }
	                }

		else if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO){
			if(temp_serviceextinfo!=null){
				System.out.printf("<TABLE BORDER='0'>\n");
				if(temp_serviceextinfo.action_url!=null && ! temp_serviceextinfo.action_url.equals("")){
					System.out.printf("<A HREF='");
					cgiutils.print_extra_service_url(temp_serviceextinfo.host_name,temp_serviceextinfo.description,temp_serviceextinfo.action_url);
					System.out.printf("' TARGET='_blank'><img src='%s%s' border=0 alt='Perform Additional Actions On This Service' title='Perform Additional Actions On This Service'></A>\n",cgiutils.url_images_path,cgiutils_h.ACTION_ICON);
					System.out.printf("<BR CLEAR=ALL><FONT SIZE=-1><I>Extra Service Actions</I></FONT><BR CLEAR=ALL><BR CLEAR=ALL>\n");
				        }
				if(temp_serviceextinfo.notes_url!=null && ! temp_serviceextinfo.notes_url.equals("")){
					System.out.printf("<A HREF='");
                    cgiutils.print_extra_service_url(temp_serviceextinfo.host_name,temp_serviceextinfo.description,temp_serviceextinfo.notes_url);
					System.out.printf("' TARGET='_blank'><img src='%s%s' border=0 alt='View Additional Notes For This Service' title='View Additional Notes For This Service'></A>\n",cgiutils.url_images_path,cgiutils_h.NOTES_ICON);
					System.out.printf("<BR CLEAR=ALL><FONT SIZE=-1><I>Extra Service Notes</I></FONT><BR CLEAR=ALL><BR CLEAR=ALL>\n");
				        }
				System.out.printf("</TABLE>\n");
		                }
	                }

		/* display context-sensitive help */
		if(display_type==cgiutils_h.DISPLAY_HOST_INFO)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_HOST);
		else if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_SERVICE);
		else if(display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_HOSTGROUP);
		else if(display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_SERVICEGROUP);
		else if(display_type==cgiutils_h.DISPLAY_PROCESS_INFO)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_PROCESS);
		else if(display_type==cgiutils_h.DISPLAY_PERFORMANCE)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_PERFORMANCE);
		else if(display_type==cgiutils_h.DISPLAY_COMMENTS)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_COMMENTS);
		else if(display_type==cgiutils_h.DISPLAY_DOWNTIME)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_DOWNTIME);
		else if(display_type==cgiutils_h.DISPLAY_SCHEDULING_QUEUE)
			cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_EXT_QUEUE);

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");

	        }

	System.out.printf("<BR>\n");

	if(display_type==cgiutils_h.DISPLAY_HOST_INFO)
		show_host_info();
	else if(display_type==cgiutils_h.DISPLAY_SERVICE_INFO)
		show_service_info();
	else if(display_type==cgiutils_h.DISPLAY_COMMENTS)
		show_all_comments();
	else if(display_type==cgiutils_h.DISPLAY_PERFORMANCE)
		show_performance_data();
	else if(display_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO)
		show_hostgroup_info();
	else if(display_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO)
		show_servicegroup_info();
	else if(display_type==cgiutils_h.DISPLAY_DOWNTIME)
		show_all_downtime();
	else if(display_type==cgiutils_h.DISPLAY_SCHEDULING_QUEUE)
		show_scheduling_queue();
	else
		show_process_info();

	document_footer();

	cgiutils.exit(  common_h.OK );
        }



public static void document_header(int use_stylesheet){
	String date_time;

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setIntHeader( "Refresh" , cgiutils.refresh_rate );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis()  );
       response.setContentType("text/html");
    } else {
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      	System.out.printf("Refresh: %d\r\n",cgiutils.refresh_rate);
      
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
	System.out.printf("Extended Information\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>",cgiutils.url_stylesheets_path,cgiutils_h.EXTINFO_CSS);
	        }
	System.out.printf("</head>\n");

	System.out.printf("<body CLASS='extinfo'>\n");

	/* include user SSI header */
    cgiutils.include_ssi_files(cgiutils_h.EXTINFO_CGI,cgiutils_h.SSI_HEADER);

	return;
        }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.EXTINFO_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int temp_type;
	int x;

	variables= getcgi.getcgivars( request_string );

	for(x=0; x < variables.length ;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the display type */
		else if(variables[x].equals("type")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
				}
			temp_type=atoi(variables[x]);
			if(temp_type==cgiutils_h.DISPLAY_HOST_INFO)
				display_type=cgiutils_h.DISPLAY_HOST_INFO;
			else if(temp_type==cgiutils_h.DISPLAY_SERVICE_INFO)
				display_type=cgiutils_h.DISPLAY_SERVICE_INFO;
			else if(temp_type==cgiutils_h.DISPLAY_COMMENTS)
				display_type=cgiutils_h.DISPLAY_COMMENTS;
			else if(temp_type==cgiutils_h.DISPLAY_PERFORMANCE)
				display_type=cgiutils_h.DISPLAY_PERFORMANCE;
			else if(temp_type==cgiutils_h.DISPLAY_HOSTGROUP_INFO)
				display_type=cgiutils_h.DISPLAY_HOSTGROUP_INFO;
			else if(temp_type==cgiutils_h.DISPLAY_SERVICEGROUP_INFO)
				display_type=cgiutils_h.DISPLAY_SERVICEGROUP_INFO;
			else if(temp_type==cgiutils_h.DISPLAY_DOWNTIME)
				display_type=cgiutils_h.DISPLAY_DOWNTIME;
			else if(temp_type==cgiutils_h.DISPLAY_SCHEDULING_QUEUE)
				display_type=cgiutils_h.DISPLAY_SCHEDULING_QUEUE;
			else
				display_type=cgiutils_h.DISPLAY_PROCESS_INFO;
			}

		/* we found the host name */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_name=variables[x];
			if(host_name==null)
				host_name="";
			}

		/* we found the hostgroup name */
		else if(variables[x].equals("hostgroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			hostgroup_name=variables[x];
			if(hostgroup_name==null)
				hostgroup_name="";
			}

		/* we found the service name */
		else if(variables[x].equals("service")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			service_desc=variables[x];
			if(service_desc==null)
				service_desc="";
			}

		/* we found the servicegroup name */
		else if(variables[x].equals("servicegroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			servicegroup_name=variables[x];
			if(servicegroup_name==null)
				servicegroup_name="";
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
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



public static void show_process_info(){
	String date_time;
	long current_time;
	long run_time;
	String run_time_string;

	/* make sure the user has rights to view system information */
	if(cgiauth.is_authorized_for_system_information(current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view process information...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<TABLE BORDER=0 CELLPADDING=20>\n");
	System.out.printf("<TR><TD VALIGN=TOP>\n");

	System.out.printf("<DIV CLASS='dataTitle'>Process Information</DIV>\n");

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='data'>\n");
	System.out.printf("<TR><TD class='stateInfoTable1'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	/* program start time */
    date_time = cgiutils.get_time_string(blue.program_start, common_h.SHORT_DATE_TIME);
	System.out.printf("<TR><TD CLASS='dataVar'>Program Start Time:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",date_time);

	/* total running time */
	current_time = utils.currentTimeInSeconds();
	run_time=(current_time-blue.program_start);
	cgiutils.time_breakdown tb = cgiutils.get_time_breakdown(run_time);
	run_time_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
	System.out.printf("<TR><TD CLASS='dataVar'>Total Running Time:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",run_time_string);

	/* last external check */
    date_time = cgiutils.get_time_string( blue.last_command_check, common_h.SHORT_DATE_TIME);
	System.out.printf("<TR><TD CLASS='dataVar'>Last External Command Check:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.last_command_check==0)?"N/A":date_time);

	/* last log file rotation */
    date_time = cgiutils.get_time_string(blue.last_log_rotation, common_h.SHORT_DATE_TIME);
	System.out.printf("<TR><TD CLASS='dataVar'>Last Log File Rotation:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.last_log_rotation==0)?"N/A":date_time);

	/* PID */
	System.out.printf("<TR><TD CLASS='dataVar'>Blue PID</TD><TD CLASS='dataval'>%d</TD></TR>\n",blue.blue_pid);

	/* notifications enabled */
	System.out.printf("<TR><TD CLASS='dataVar'>Notifications Enabled?</TD><TD CLASS='dataVal'><DIV CLASS='notifications%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(blue.enable_notifications==common_h.TRUE)?"ENABLED":"DISABLED",(blue.enable_notifications==common_h.TRUE)?"YES":"NO");

	/* service check execution enabled */
	System.out.printf("<TR><TD CLASS='dataVar'>Service Checks Being Executed?</TD><TD CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(blue.execute_service_checks==common_h.TRUE)?"ENABLED":"DISABLED",(blue.execute_service_checks==common_h.TRUE)?"YES":"NO");

	/* passive service check acceptance */
	System.out.printf("<TR><TD CLASS='dataVar'>Passive Service Checks Being Accepted?</TD><TD CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(blue.accept_passive_service_checks==common_h.TRUE)?"ENABLED":"DISABLED",(blue.accept_passive_service_checks==common_h.TRUE)?"YES":"NO");

	/* host check execution enabled */
	System.out.printf("<TR><TD CLASS='dataVar'>Host Checks Being Executed?</TD><TD CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(blue.execute_host_checks==common_h.TRUE)?"ENABLED":"DISABLED",(blue.execute_host_checks==common_h.TRUE)?"YES":"NO");

	/* passive host check acceptance */
	System.out.printf("<TR><TD CLASS='dataVar'>Passive Host Checks Being Accepted?</TD><TD CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(blue.accept_passive_host_checks==common_h.TRUE)?"ENABLED":"DISABLED",(blue.accept_passive_host_checks==common_h.TRUE)?"YES":"NO");

	/* event handlers enabled */
	System.out.printf("<TR><TD CLASS='dataVar'>Event Handlers Enabled?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.enable_event_handlers==common_h.TRUE)?"Yes":"No");

	/* obsessing over services */
	System.out.printf("<TR><TD CLASS='dataVar'>Obsessing Over Services?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.obsess_over_services==common_h.TRUE)?"Yes":"No");

	/* obsessing over hosts */
	System.out.printf("<TR><TD CLASS='dataVar'>Obsessing Over Hosts?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.obsess_over_hosts==common_h.TRUE)?"Yes":"No");

	/* flap detection enabled */
	System.out.printf("<TR><TD CLASS='dataVar'>Flap Detection Enabled?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.enable_flap_detection==common_h.TRUE)?"Yes":"No");

//#ifdef PREDICT_FAILURES
	/* failure prediction enabled */
	System.out.printf("<TR><TD CLASS='dataVar'>Failure Prediction Enabled?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.enable_failure_prediction==common_h.TRUE)?"Yes":"No");
//#endif

	/* process performance data */
	System.out.printf("<TR><TD CLASS='dataVar'>Performance Data Being Processed?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.process_performance_data==common_h.TRUE)?"Yes":"No");

//#ifdef USE_OLDCRUD
	/* daemon mode */
	System.out.printf("<TR><TD CLASS='dataVar'>Running As A Daemon?</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(blue.daemon_mode==common_h.TRUE)?"Yes":"No");
//#endif

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");


	System.out.printf("</TD><TD VALIGN=TOP>\n");

	System.out.printf("<DIV CLASS='commandTitle'>Process Commands</DIV>\n");

	System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='command'>\n");
	System.out.printf("<TR><TD>\n");

	if(cgiutils.blue_process_state==cgiutils_h.STATE_OK){
		System.out.printf("<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 CLASS='command'>\n");

//#ifndef DUMMY_INSTALL
		System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Shutdown the Blue Process' TITLE='Shutdown the Blue Process'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Shutdown the Blue process</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.STOP_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SHUTDOWN_PROCESS);
		System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Restart the Blue Process' TITLE='Restart the Blue Process'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Restart the Blue process</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.RESTART_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_RESTART_PROCESS);
//#endif

		if(blue.enable_notifications==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Disable Notifications' TITLE='Disable Notifications'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Disable notifications</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_NOTIFICATIONS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Enable Notifications' TITLE='Enable Notifications'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Enable notifications</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_NOTIFICATIONS);

		if(blue.execute_service_checks==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Stop Executing Service Checks' TITLE='Stop Executing Service Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Stop executing service checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_EXECUTING_SVC_CHECKS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Start Executing Service Checks' TITLE='Start Executing Service Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Start executing service checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_EXECUTING_SVC_CHECKS);

		if(blue.accept_passive_service_checks==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Stop Accepting Passive Service Checks' TITLE='Stop Accepting Passive Service Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Stop accepting passive service checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Start Accepting Passive Service Checks' TITLE='Start Accepting Passive Service Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Start accepting passive service checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS);

		if(blue.execute_host_checks==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Stop Executing Host Checks' TITLE='Stop Executing Host Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Stop executing host checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_EXECUTING_HOST_CHECKS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Start Executing Host Checks' TITLE='Start Executing Host Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Start executing host checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_EXECUTING_HOST_CHECKS);

		if(blue.accept_passive_host_checks==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Stop Accepting Passive Host Checks' TITLE='Stop Accepting Passive Host Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Stop accepting passive host checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Start Accepting Passive Host Checks' TITLE='Start Accepting Passive Host Checks'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Start accepting passive host checks</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS);

		if(blue.enable_event_handlers==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Disable Event Handlers' TITLE='Disable Event Handlers'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Disable event handlers</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_EVENT_HANDLERS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Enable Event Handlers' TITLE='Enable Event Handlers'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Enable event handlers</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_EVENT_HANDLERS);

		if(blue.obsess_over_services==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Stop Obsessing Over Services' TITLE='Stop Obsessing Over Services'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Stop obsessing over services</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Start Obsessing Over Services' TITLE='Start Obsessing Over Services'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Start obsessing over services</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS);

		if(blue.obsess_over_hosts==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Stop Obsessing Over Hosts' TITLE='Stop Obsessing Over Hosts'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Stop obsessing over hosts</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Start Obsessing Over Hosts' TITLE='Start Obsessing Over Hosts'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Start obsessing over hosts</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS);

		if(blue.enable_flap_detection==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Disable Flap Detection' TITLE='Disable Flap Detection'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Disable flap detection</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_FLAP_DETECTION);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Enable Flap Detection' TITLE='Enable Flap Detection'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Enable flap detection</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_FLAP_DETECTION);

//#ifdef PREDICT_FAILURES
		if(blue.enable_failure_prediction==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Disable Failure Prediction' TITLE='Disable Failure Prediction'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Disable failure prediction</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_FAILURE_PREDICTION);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Enable Failure Prediction' TITLE='Enable Failure Prediction'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Enable failure prediction</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_FAILURE_PREDICTION);
//#endif
		if(blue.process_performance_data==common_h.TRUE)
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Disable Performance Data' TITLE='Disable Performance Data'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Disable performance data</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_PERFORMANCE_DATA);
		else
			System.out.printf("<TR CLASS='command'><TD><img src='%s%s' border=0 ALT='Enable Performance Data' TITLE='Enable Performance Data'></td><td CLASS='command'><a href='%s?cmd_typ=%d'>Enable performance data</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_PERFORMANCE_DATA);

		System.out.printf("</TABLE>\n");
	        }
	else{
		System.out.printf("<DIV ALIGN=CENTER CLASS='infoMessage'>It appears as though Blue is not running, so commands are temporarily unavailable...\n");
		if( cgiutils.blue_check_command.equals("")){
			System.out.printf("<BR><BR>\n");
			System.out.printf("Hint: It looks as though you have not defined a command for checking the process state by supplying a value for the <b>nagios_check_command</b> option in the CGI configuration file.<BR>\n");
			System.out.printf("Read the documentation for more information on checking the status of the Blue process in the CGIs.\n");
		        }
		System.out.printf("</DIV>\n");
	        }

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</TD></TR></TABLE>\n");

//#ifdef REMOVED_081203
//	System.out.printf("<P>");
//	System.out.printf("<DIV ALIGN=CENTER>\n");
//
//	System.out.printf("<DIV CLASS='dataTitle'>Process Status Information</DIV>\n");
//
//	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='data'>\n");
//	System.out.printf("<TR><TD class='stateInfoTable2'>\n");
//	System.out.printf("<TABLE BORDER=0>\n");
//
//	if(nagios_process_state==STATE_OK){
//		strcpy(state_string,"OK");
//		state_class="processOK";
//		}
//	else if(nagios_process_state==STATE_WARNING){
//		strcpy(state_string,"WARNING");
//		state_class="processWARNING";
//		}
//	else if(nagios_process_state==STATE_CRITICAL){
//		strcpy(state_string,"CRITICAL");
//		state_class="processCRITICAL";
//		}
//	else{
//		strcpy(state_string,"UNKNOWN");
//		state_class="processUNKNOWN";
//		}
//
//	/* process state */
//	System.out.printf("<TR><TD CLASS='dataVar'>Process Status:</TD><TD CLASS='dataVal'><DIV CLASS='%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",state_class,state_string);
//
//	/* process check command result */
//	System.out.printf("<TR><TD CLASS='dataVar'>Check Command Output:&nbsp;</TD><TD CLASS='dataVal'>%s&nbsp;</TD></TR>\n",nagios_process_info);
//
//	System.out.printf("</TABLE>\n");
//	System.out.printf("</TD></TR>\n");
//	System.out.printf("</TABLE>\n");
//
//	System.out.printf("</DIV>\n");
//	System.out.printf("</P>\n");
//#endif

	return;
	}


public static void show_host_info(){
	statusdata_h.hoststatus temp_hoststatus;
	objects_h.host temp_host;
	String date_time;
	String state_duration; // 48
	String status_age; //48
	String state_string = "";
	String bg_class="";
	long current_time;
	long t;
	int duration_error=common_h.FALSE;


	/* get host info */
	temp_host=objects.find_host(host_name);

	/* make sure the user has rights to view host information */
	if( cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for this host...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	/* get host status info */
	temp_hoststatus=statusdata.find_hoststatus(host_name);

	/* make sure host information exists */
	if(temp_host==null){
		System.out.printf("<P><DIV CLASS='errorMessage'>Error: Host Not Found!</DIV></P>>");
		return;
		}
	if(temp_hoststatus==null){
		System.out.printf("<P><DIV CLASS='errorMessage'>Error: Host Status Information Not Found!</DIV></P");
		return;
		}


	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CELLPADDING=0 WIDTH=100%%>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel'>\n");
	
	System.out.printf("<DIV CLASS='dataTitle'>Host State Information</DIV>\n");

	if(temp_hoststatus.has_been_checked==common_h.FALSE)
		System.out.printf("<P><DIV ALIGN=CENTER>This host has not yet been checked, so status information is not available.</DIV></P>\n");

	else{

		System.out.printf("<TABLE BORDER=0>\n");
		System.out.printf("<TR><TD>\n");

		System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
		System.out.printf("<TR><TD class='stateInfoTable1'>\n");
		System.out.printf("<TABLE BORDER=0>\n");

		if(temp_hoststatus.status==statusdata_h.HOST_UP){
			state_string = "UP";
			bg_class="hostUP";
		        }
		else if(temp_hoststatus.status==statusdata_h.HOST_DOWN){
			state_string = "DOWN";
			bg_class="hostDOWN";
		        }
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE){
			state_string = "UNREACHABLE";
			bg_class="hostUNREACHABLE";
		        }

		System.out.printf("<TR><TD CLASS='dataVar'>Host Status:</td><td CLASS='dataVal'><DIV CLASS='%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></td></tr>\n",bg_class,state_string,(temp_hoststatus.problem_has_been_acknowledged==common_h.TRUE)?"(Has been acknowledged)":"");

		System.out.printf("<TR><TD CLASS='dataVar'>Status Information:</td><td CLASS='dataVal'>%s</td></tr>\n",(temp_hoststatus.plugin_output==null)?"":temp_hoststatus.plugin_output);
        System.out.printf("<TR><TD CLASS='dataVar'>Host Check Command:</td><td CLASS='dataVal'>%s</td></tr>\n",temp_host.host_check_command );
        

		System.out.printf("<TR><TD CLASS='dataVar'>Performance Data:</td><td CLASS='dataVal'>%s</td></tr>\n",(temp_hoststatus.perf_data==null)?"":temp_hoststatus.perf_data);

		System.out.printf("<TR><TD CLASS='dataVar'>Current Attempt:</TD><TD CLASS='dataVal'>%d/%d</TD></TR>\n",temp_hoststatus.current_attempt,temp_hoststatus.max_attempts);

		System.out.printf("<TR><TD CLASS='dataVar'>State Type:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_hoststatus.state_type==common_h.HARD_STATE)?"HARD":"SOFT");

		System.out.printf("<TR><TD CLASS='dataVar'>Last Check Type:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_hoststatus.check_type==common_h.HOST_CHECK_ACTIVE)?"ACTIVE":"PASSIVE");

        date_time = cgiutils.get_time_string( temp_hoststatus.last_check, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last Check Time:</td><td CLASS='dataVal'>%s</td></tr>\n",date_time);

		current_time=utils.currentTimeInSeconds();
		t=0;
		duration_error=common_h.FALSE;
		if(temp_hoststatus.last_check>current_time)
			duration_error=common_h.TRUE;
		else
			t=current_time-temp_hoststatus.last_check;
		cgiutils.time_breakdown tb = cgiutils.get_time_breakdown( t );
		if(duration_error==common_h.TRUE)
			status_age = "???";
		else if(temp_hoststatus.last_check==0)
			status_age = "N/A";
		else
			status_age = String.format( "%2dd %2dh %2dm %2ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		System.out.printf("<TR><TD CLASS='dataVar'>Status Data Age:</td><td CLASS='dataVal'>%s</td></tr>\n",status_age);

        date_time = cgiutils.get_time_string(temp_hoststatus.next_check, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Next Scheduled Active Check:&nbsp;&nbsp;</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_hoststatus.checks_enabled!=0 && temp_hoststatus.next_check!=0 && temp_hoststatus.should_be_scheduled==common_h.TRUE)?date_time:"N/A");

		System.out.printf("<TR><TD CLASS='dataVar'>Latency:</TD><TD CLASS='dataVal'>");
		if(temp_hoststatus.check_type==common_h.HOST_CHECK_ACTIVE)
			System.out.printf("%.3f seconds",temp_hoststatus.latency);
		else
			System.out.printf("N/A");
		System.out.printf("</TD></TR>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>Check Duration:</td><td CLASS='dataVal'>%.3f seconds</td></tr>\n",temp_hoststatus.execution_time);

        date_time = cgiutils.get_time_string( temp_hoststatus.last_state_change, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last State Change:</td><td CLASS='dataVal'>%s</td></tr>\n",(temp_hoststatus.last_state_change==0)?"N/A":date_time);

		t=0;
		duration_error=common_h.FALSE;
		if(temp_hoststatus.last_state_change==0){
			if(blue.program_start>current_time)
				duration_error=common_h.TRUE;
			else
				t=current_time-blue.program_start;
		        }
		else{
			if(temp_hoststatus.last_state_change>current_time)
				duration_error=common_h.TRUE;
			else
				t=current_time-temp_hoststatus.last_state_change;
		        }
		tb = cgiutils.get_time_breakdown( t );
		if(duration_error==common_h.TRUE)
			state_duration = "???";
		else
			state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_hoststatus.last_state_change==0)?"+":"");
		System.out.printf("<TR><TD CLASS='dataVar'>Current State Duration:</td><td CLASS='dataVal'>%s</td></tr>\n",state_duration);

        date_time = cgiutils.get_time_string(temp_hoststatus.last_notification, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last Host Notification:</td><td CLASS='dataVal'>%s</td></tr>\n",(temp_hoststatus.last_notification==0)?"N/A":date_time);

		System.out.printf("<TR><TD CLASS='dataVar'>Current Notification Number:&nbsp;&nbsp;</td><td CLASS='dataVal'>%d&nbsp;&nbsp;</td></tr>\n",temp_hoststatus.current_notification_number);

		System.out.printf("<TR><TD CLASS='dataVar'>Is This Host Flapping?</td><td CLASS='dataVal'>");
		if(temp_hoststatus.flap_detection_enabled==common_h.FALSE || blue.enable_flap_detection==common_h.FALSE)
			System.out.printf("N/A");
		else
			System.out.printf("<DIV CLASS='%sflapping'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV>",(temp_hoststatus.is_flapping==common_h.TRUE)?"":"not",(temp_hoststatus.is_flapping==common_h.TRUE)?"YES":"NO");
		System.out.printf("</td></tr>\n");

		System.out.printf("<TR'><TD CLASS='dataVar'>Percent State Change:</td><td CLASS='dataVal'>");
		if(temp_hoststatus.flap_detection_enabled==common_h.FALSE || blue.enable_flap_detection==common_h.FALSE)
			System.out.printf("N/A");
		else
			System.out.printf("%3.2f%%",temp_hoststatus.percent_state_change);
		System.out.printf("</td></tr>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>In Scheduled Downtime?</td><td CLASS='dataVal'><DIV CLASS='downtime%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></td></tr>\n",(temp_hoststatus.scheduled_downtime_depth>0)?"ACTIVE":"INACTIVE",(temp_hoststatus.scheduled_downtime_depth>0)?"YES":"NO");

        date_time = cgiutils.get_time_string( temp_hoststatus.last_update, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last Update:</td><td CLASS='dataVal'>%s</td></tr>\n",(temp_hoststatus.last_update==0)?"N/A":date_time);

		System.out.printf("</TABLE>\n");
		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</TD></TR>\n");
		System.out.printf("<TR><TD>\n");

		System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
		System.out.printf("<TR><TD class='stateInfoTable2'>\n");
		System.out.printf("<TABLE BORDER=0>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>Active Checks:</TD><TD CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_hoststatus.checks_enabled==common_h.TRUE)?"ENABLED":"DISABLED",(temp_hoststatus.checks_enabled==common_h.TRUE)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Passive Checks:</TD><td CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_hoststatus.accept_passive_host_checks==common_h.TRUE)?"ENABLED":"DISABLED",(temp_hoststatus.accept_passive_host_checks!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Obsessing:</TD><td CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_hoststatus.obsess_over_host==common_h.TRUE)?"ENABLED":"DISABLED",(temp_hoststatus.obsess_over_host!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Notifications:</td><td CLASS='dataVal'><DIV CLASS='notifications%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></td></tr>\n",(temp_hoststatus.notifications_enabled!=0)?"ENABLED":"DISABLED",(temp_hoststatus.notifications_enabled!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Event Handler:</td><td CLASS='dataVal'><DIV CLASS='eventhandlers%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></td></tr>\n",(temp_hoststatus.event_handler_enabled!=0)?"ENABLED":"DISABLED",(temp_hoststatus.event_handler_enabled!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Flap Detection:</td><td CLASS='dataVal'><DIV CLASS='flapdetection%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></td></tr>\n",(temp_hoststatus.flap_detection_enabled==common_h.TRUE)?"ENABLED":"DISABLED",(temp_hoststatus.flap_detection_enabled==common_h.TRUE)?"ENABLED":"DISABLED");

		System.out.printf("</TABLE>\n");
		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");
	        }

	System.out.printf("</TD>\n");

	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");
	System.out.printf("<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0><TR>\n");

	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='commandPanel'>\n");

	System.out.printf("<DIV CLASS='commandTitle'>Host Commands</DIV>\n");

	System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='command'><TR><TD>\n");

	if( cgiutils.blue_process_state==cgiutils_h.STATE_OK){

		System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Locate Host On Map' TITLE='Locate Host On Map'></td><td CLASS='command'><a href='%s?host=%s'>Locate host on map</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.STATUSMAP_ICON,cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(host_name));

		if(temp_hoststatus.checks_enabled==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Active Checks Of This Host' TITLE='Disable Active Checks Of This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Disable active checks of this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_CHECK,cgiutils.url_encode(host_name));
			System.out.printf("<tr CLASS='data'><td><img src='%s%s' border=0 ALT='Re-schedule Next Host Check' TITLE='Re-schedule Next Host Check'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Re-schedule the next check of this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DELAY_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOST_CHECK,cgiutils.url_encode(host_name));
		        }
		else
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Active Checks Of This Host' TITLE='Enable Active Checks Of This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Enable active checks of this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_CHECK,cgiutils.url_encode(host_name));

		if(temp_hoststatus.accept_passive_host_checks==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Submit Passive Check Result For This Host' TITLE='Submit Passive Check Result For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Submit passive check result for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.PASSIVE_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_PROCESS_HOST_CHECK_RESULT,cgiutils.url_encode(host_name));
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Stop Accepting Passive Checks For This Host' TITLE='Stop Accepting Passive Checks For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Stop accepting passive checks for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS,cgiutils.url_encode(host_name));
		        }
		else
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Start Accepting Passive Checks For This Host' TITLE='Start Accepting Passive Checks For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Start accepting passive checks for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS,cgiutils.url_encode(host_name));

		if(temp_hoststatus.obsess_over_host==common_h.TRUE)
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Stop Obsessing Over This Host' TITLE='Stop Obsessing Over This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Stop obsessing over this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_OBSESSING_OVER_HOST,cgiutils.url_encode(host_name));
		else
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Start Obsessing Over This Host' TITLE='Start Obsessing Over This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Start obsessing over this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_OBSESSING_OVER_HOST,cgiutils.url_encode(host_name));

		if(temp_hoststatus.status==statusdata_h.HOST_DOWN || temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE){
			if(temp_hoststatus.problem_has_been_acknowledged==common_h.FALSE)
				System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Acknowledge This Host Problem' TITLE='Acknowledge This Host Problem'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Acknowledge this host problem</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ACKNOWLEDGEMENT_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM,cgiutils.url_encode(host_name));
			else
				System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Remove Problem Acknowledgement' TITLE='Remove Problem Acknowledgement'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Remove problem acknowledgement</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.REMOVE_ACKNOWLEDGEMENT_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT,cgiutils.url_encode(host_name));
		        }

		if(temp_hoststatus.notifications_enabled==common_h.TRUE)
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For This Host' TITLE='Disable Notifications For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Disable notifications for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_NOTIFICATIONS,cgiutils.url_encode(host_name));
		else
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For This Host' TITLE='Enable Notifications For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Enable notifications for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_NOTIFICATIONS,cgiutils.url_encode(host_name));
		if(temp_hoststatus.status!=statusdata_h.HOST_UP)
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Delay Next Host Notification' TITLE='Delay Next Host Notification'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Delay next host notification</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DELAY_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DELAY_HOST_NOTIFICATION,cgiutils.url_encode(host_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule Downtime For This Host' TITLE='Schedule Downtime For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Schedule downtime for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOST_DOWNTIME,cgiutils.url_encode(host_name));

		/*
		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Cancel Scheduled Downtime For This Host' TITLE='Cancel Scheduled Downtime For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Cancel scheduled downtime for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.SCHEDULED_DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_CANCEL_HOST_DOWNTIME,cgiutils.url_encode(host_name));
		*/

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For All Services On This Host' TITLE='Disable Notifications For All Services On This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Disable notifications for all services on this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS,cgiutils.url_encode(host_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For All Services On This Host' TITLE='Enable Notifications For All Services On This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Enable notifications for all services on this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS,cgiutils.url_encode(host_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule A Check Of All Services On This Host' TITLE='Schedule A Check Of All Services On This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Schedule a check of all services on this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DELAY_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOST_SVC_CHECKS,cgiutils.url_encode(host_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Checks Of All Services On This Host' TITLE='Disable Checks Of All Services On This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Disable checks of all services on this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_SVC_CHECKS,cgiutils.url_encode(host_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Checks Of All Services On This Host' TITLE='Enable Checks Of All Services On This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Enable checks of all services on this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_SVC_CHECKS,cgiutils.url_encode(host_name));

		if(temp_hoststatus.event_handler_enabled==common_h.TRUE)
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Event Handler For This Host' TITLE='Disable Event Handler For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Disable event handler for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_EVENT_HANDLER,cgiutils.url_encode(host_name));
		else
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Event Handler For This Host' TITLE='Enable Event Handler For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Enable event handler for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_EVENT_HANDLER,cgiutils.url_encode(host_name));
		if(temp_hoststatus.flap_detection_enabled==common_h.TRUE)
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Flap Detection For This Host' TITLE='Disable Flap Detection For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Disable flap detection for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_FLAP_DETECTION,cgiutils.url_encode(host_name));
		else
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Flap Detection For This Host' TITLE='Enable Flap Detection For This Host'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s'>Enable flap detection for this host</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_FLAP_DETECTION,cgiutils.url_encode(host_name));

		System.out.printf("</TABLE>\n");
	        }
	else{
		System.out.printf("<DIV ALIGN=CENTER CLASS='infoMessage'>It appears as though Blue is not running, so commands are temporarily unavailable...<br>\n");
		System.out.printf("Click <a href='%s?type=%d'>here</a> to view Blue process information</DIV>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_PROCESS_INFO);
	        }
	System.out.printf("</TD></TR></TABLE>\n");

	System.out.printf("</TD>\n");

	System.out.printf("</TR>\n");
	System.out.printf("</TABLE></TR>\n");

	System.out.printf("<TR>\n");

	System.out.printf("<TD COLSPAN=2 ALIGN=CENTER VALIGN=TOP CLASS='commentPanel'>\n");

	/* display comments */
	display_comments(comments_h.HOST_COMMENT);

	System.out.printf("</TD>\n");

	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

	return;
	}


public static void show_service_info(){
	objects_h.service temp_service;
	String date_time;
	String status_age; // 48
	String state_duration; //48
	statusdata_h.servicestatus temp_svcstatus;
	String state_string;
	String bg_class="";
	long t;
	long current_time;
	int duration_error=common_h.FALSE;

	/* find the service */
	temp_service= objects.find_service(host_name,service_desc);

	/* make sure the user has rights to view service information */
	if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for this service...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	/* get service status info */
	temp_svcstatus=statusdata.find_servicestatus(host_name,service_desc);

	/* make sure service information exists */
	if(temp_service==null){
		System.out.printf("<P><DIV CLASS='errorMessage'>Error: Service Not Found!</DIV></P>");
		return;
		}
	if(temp_svcstatus==null){
		System.out.printf("<P><DIV CLASS='errorMessage'>Error: Service Status Not Found!</DIV></P>");
		return;
		}


	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 WIDTH=100%%>\n");
	System.out.printf("<TR>\n");

	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel'>\n");
	
	System.out.printf("<DIV CLASS='dataTitle'>Service State Information</DIV>\n");

	if(temp_svcstatus.has_been_checked==common_h.FALSE)
		System.out.printf("<P><DIV ALIGN=CENTER>This service has not yet been checked, so status information is not available.</DIV></P>\n");

	else{

		System.out.printf("<TABLE BORDER=0>\n");

		System.out.printf("<TR><TD>\n");

		System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
		System.out.printf("<TR><TD class='stateInfoTable1'>\n");
		System.out.printf("<TABLE BORDER=0>\n");


		if(temp_svcstatus.status==statusdata_h.SERVICE_OK){
			state_string = "OK";
			bg_class="serviceOK";
			}
		else if(temp_svcstatus.status==statusdata_h.SERVICE_WARNING){
			state_string = "WARNING";
			bg_class="serviceWARNING";
			}
		else if(temp_svcstatus.status==statusdata_h.SERVICE_CRITICAL){
			state_string = "CRITICAL";
			bg_class="serviceCRITICAL";
			}
		else{
			state_string = "UNKNOWN";
			bg_class="serviceUNKNOWN";
			}
		System.out.printf("<TR><TD CLASS='dataVar'>Current Status:</TD><TD CLASS='dataVal'><DIV CLASS='%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",bg_class,state_string,(temp_svcstatus.problem_has_been_acknowledged==common_h.TRUE)?"(Has been acknowledged)":"");

		System.out.printf("<TR><TD CLASS='dataVar'>Status Information:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.plugin_output==null)?"":temp_svcstatus.plugin_output);

		System.out.printf("<TR><TD CLASS='dataVar'>Performance Data:</td><td CLASS='dataVal'>%s</td></tr>\n",(temp_svcstatus.perf_data==null)?"":temp_svcstatus.perf_data);

		System.out.printf("<TR><TD CLASS='dataVar'>Current Attempt:</TD><TD CLASS='dataVal'>%d/%d</TD></TR>\n",temp_svcstatus.current_attempt,temp_svcstatus.max_attempts);

		System.out.printf("<TR><TD CLASS='dataVar'>State Type:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.state_type==common_h.HARD_STATE)?"HARD":"SOFT");

		System.out.printf("<TR><TD CLASS='dataVar'>Last Check Type:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.check_type==common_h.SERVICE_CHECK_ACTIVE)?"ACTIVE":"PASSIVE");

        date_time = cgiutils.get_time_string(temp_svcstatus.last_check, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last Check Time:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",date_time);

		current_time=utils.currentTimeInSeconds();
		t=0;
		duration_error=common_h.FALSE;
		if(temp_svcstatus.last_check>current_time)
			duration_error=common_h.TRUE;
		else
			t=current_time-temp_svcstatus.last_check;
		cgiutils.time_breakdown tb =  cgiutils.get_time_breakdown( t );
		if(duration_error==common_h.TRUE)
			status_age = "???";
		else if(temp_svcstatus.last_check==0)
			status_age = "N/A";
		else
			status_age = String.format("%2dd %2dh %2dm %2ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		System.out.printf("<TR><TD CLASS='dataVar'>Status Data Age:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",status_age);

        date_time = cgiutils.get_time_string(temp_svcstatus.next_check, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Next Scheduled Active Check:&nbsp;&nbsp;</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.checks_enabled!=0 && temp_svcstatus.next_check!=0 && temp_svcstatus.should_be_scheduled==common_h.TRUE)?date_time:"N/A");

		System.out.printf("<TR><TD CLASS='dataVar'>Latency:</TD><TD CLASS='dataVal'>");
		if(temp_svcstatus.check_type==common_h.SERVICE_CHECK_ACTIVE)
			System.out.printf("%.3f seconds",temp_svcstatus.latency);
		else
			System.out.printf("N/A");
		System.out.printf("</TD></TR>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>Check Duration:</TD><TD CLASS='dataVal'>%.3f seconds</TD></TR>\n",temp_svcstatus.execution_time);

        date_time = cgiutils.get_time_string(temp_svcstatus.last_state_change, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last State Change:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.last_state_change==0)?"N/A":date_time);

		t=0;
		duration_error=common_h.FALSE;
		if(temp_svcstatus.last_state_change==0){
			if(blue.program_start>current_time)
				duration_error=common_h.TRUE;
			else
				t=current_time-blue.program_start;
		        }
		else{
			if(temp_svcstatus.last_state_change>current_time)
				duration_error=common_h.TRUE;
			else
				t=current_time-temp_svcstatus.last_state_change;
		        }
		tb = cgiutils.get_time_breakdown( t );
		if(duration_error==common_h.TRUE)
			state_duration = "???";
		else
			state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_svcstatus.last_state_change==0)?"+":"");
		System.out.printf("<TR><TD CLASS='dataVar'>Current State Duration:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",state_duration);

        date_time = cgiutils.get_time_string( temp_svcstatus.last_notification, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last Service Notification:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.last_notification==0)?"N/A":date_time);

        date_time = cgiutils.get_time_string( temp_svcstatus.last_notification,common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Current Notification Number:</TD><TD CLASS='dataVal'>%d</TD></TR>\n",temp_svcstatus.current_notification_number);

		System.out.printf("<TR><TD CLASS='dataVar'>Is This Service Flapping?</TD><TD CLASS='dataVal'>");
		if(temp_svcstatus.flap_detection_enabled==common_h.FALSE || blue.enable_flap_detection==common_h.FALSE)
			System.out.printf("N/A");
		else
			System.out.printf("<DIV CLASS='%sflapping'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV>",(temp_svcstatus.is_flapping==common_h.TRUE)?"":"not",(temp_svcstatus.is_flapping==common_h.TRUE)?"YES":"NO");
		System.out.printf("</TD></TR>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>Percent State Change:</TD><TD CLASS='dataVal'>");
		if(temp_svcstatus.flap_detection_enabled==common_h.FALSE || blue.enable_flap_detection==common_h.FALSE)
			System.out.printf("N/A");
		else
			System.out.printf("%3.2f%%",temp_svcstatus.percent_state_change);
		System.out.printf("</TD></TR>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>In Scheduled Downtime?</TD><TD CLASS='dataVal'><DIV CLASS='downtime%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.scheduled_downtime_depth>0)?"ACTIVE":"INACTIVE",(temp_svcstatus.scheduled_downtime_depth>0)?"YES":"NO");

        date_time = cgiutils.get_time_string( temp_svcstatus.last_update, common_h.SHORT_DATE_TIME);
		System.out.printf("<TR><TD CLASS='dataVar'>Last Update:</TD><TD CLASS='dataVal'>%s</TD></TR>\n",(temp_svcstatus.last_update==0)?"N/A":date_time);


		System.out.printf("</TABLE>\n");
		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</TD></TR>\n");
		System.out.printf("<TR><TD>\n");

		System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
		System.out.printf("<TR><TD class='stateInfoTable2'>\n");
		System.out.printf("<TABLE BORDER=0>\n");

		System.out.printf("<TR><TD CLASS='dataVar'>Active Checks:</TD><td CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.checks_enabled!=0)?"ENABLED":"DISABLED",(temp_svcstatus.checks_enabled!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Passive Checks:</TD><td CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.accept_passive_service_checks==common_h.TRUE)?"ENABLED":"DISABLED",(temp_svcstatus.accept_passive_service_checks!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Obsessing:</TD><td CLASS='dataVal'><DIV CLASS='checks%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.obsess_over_service==common_h.TRUE)?"ENABLED":"DISABLED",(temp_svcstatus.obsess_over_service!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><td CLASS='dataVar'>Notifications:</TD><td CLASS='dataVal'><DIV CLASS='notifications%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.notifications_enabled!=0)?"ENABLED":"DISABLED",(temp_svcstatus.notifications_enabled!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Event Handler:</TD><td CLASS='dataVal'><DIV CLASS='eventhandlers%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.event_handler_enabled!=0)?"ENABLED":"DISABLED",(temp_svcstatus.event_handler_enabled!=0)?"ENABLED":"DISABLED");

		System.out.printf("<TR><TD CLASS='dataVar'>Flap Detection:</TD><td CLASS='dataVal'><DIV CLASS='flapdetection%s'>&nbsp;&nbsp;%s&nbsp;&nbsp;</DIV></TD></TR>\n",(temp_svcstatus.flap_detection_enabled==common_h.TRUE)?"ENABLED":"DISABLED",(temp_svcstatus.flap_detection_enabled==common_h.TRUE)?"ENABLED":"DISABLED");


		System.out.printf("</TABLE>\n");
		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");
                }
	

	System.out.printf("</TD>\n");

	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");
	System.out.printf("<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0><TR>\n");

	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='commandPanel'>\n");

	System.out.printf("<DIV CLASS='dataTitle'>Service Commands</DIV>\n");

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");
	System.out.printf("<TR><TD>\n");

	if(cgiutils.blue_process_state==cgiutils_h.STATE_OK){
		System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");

		if(temp_svcstatus.checks_enabled!=0){

			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Active Checks Of This Service' TITLE='Disable Active Checks Of This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SVC_CHECK,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Disable active checks of this service</a></td></tr>\n",cgiutils.url_encode(service_desc));

			System.out.printf("<tr CLASS='data'><td><img src='%s%s' border=0 ALT='Re-schedule Next Service Check' TITLE='Re-schedule Next Service Check'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DELAY_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_SVC_CHECK,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Re-schedule the next check of this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
	                }
		else{
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Active Checks Of This Service' TITLE='Enable Active Checks Of This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SVC_CHECK,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Enable active checks of this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
	                }

		if(temp_svcstatus.accept_passive_service_checks==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Submit Passive Check Result For This Service' TITLE='Submit Passive Check Result For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.PASSIVE_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_PROCESS_SERVICE_CHECK_RESULT,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Submit passive check result for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));

			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Stop Accepting Passive Checks For This Service' TITLE='Stop Accepting Passive Checks For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Stop accepting passive checks for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }
		else{
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Start Accepting Passive Checks For This Service' TITLE='Start Accepting Passive Checks For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Start accepting passive checks for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }

		if(temp_svcstatus.obsess_over_service==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Stop Obsessing Over This Service' TITLE='Stop Obsessing Over This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_STOP_OBSESSING_OVER_SVC,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Stop obsessing over this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }
		else{
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Start Obsessing Over This Service' TITLE='Start Obsessing Over This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_START_OBSESSING_OVER_SVC,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Start obsessing over this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }

		if((temp_svcstatus.status==statusdata_h.SERVICE_WARNING || temp_svcstatus.status==statusdata_h.SERVICE_UNKNOWN || temp_svcstatus.status==statusdata_h.SERVICE_CRITICAL) && temp_svcstatus.state_type==common_h.HARD_STATE){
			if(temp_svcstatus.problem_has_been_acknowledged==common_h.FALSE){
				System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Acknowledge This Service Problem' TITLE='Acknowledge This Service Problem'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ACKNOWLEDGEMENT_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM,cgiutils.url_encode(host_name));
				System.out.printf("&service=%s'>Acknowledge this service problem</a></td></tr>\n",cgiutils.url_encode(service_desc));
			        }
			else{
				System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Remove Problem Acknowledgement' TITLE='Remove Problem Acknowledgement'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.REMOVE_ACKNOWLEDGEMENT_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT,cgiutils.url_encode(host_name));
				System.out.printf("&service=%s'>Remove problem acknowledgement</a></td></tr>\n",cgiutils.url_encode(service_desc));
			        }
		        }
		if(temp_svcstatus.notifications_enabled==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For This Service' TITLE='Disable Notifications For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SVC_NOTIFICATIONS,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Disable notifications for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
			if(temp_svcstatus.status!=statusdata_h.SERVICE_OK){
				System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Delay Next Service Notification' TITLE='Delay Next Service Notification'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DELAY_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DELAY_SVC_NOTIFICATION,cgiutils.url_encode(host_name));
				System.out.printf("&service=%s'>Delay next service notification</a></td></tr>\n",cgiutils.url_encode(service_desc));
		                }
		        }
		else{
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For This Service' TITLE='Enable Notifications For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SVC_NOTIFICATIONS,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Enable notifications for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule Downtime For This Service' TITLE='Schedule Downtime For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_SVC_DOWNTIME,cgiutils.url_encode(host_name));
		System.out.printf("&service=%s'>Schedule downtime for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));

		/*
		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Cancel Scheduled Downtime For This Service' TITLE='Cancel Scheduled Downtime For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.SCHEDULED_DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_CANCEL_SVC_DOWNTIME,cgiutils.url_encode(host_name));
		System.out.printf("&service=%s'>Cancel scheduled downtime for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		*/

		if(temp_svcstatus.event_handler_enabled==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Event Handler For This Service' TITLE='Disable Event Handler For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SVC_EVENT_HANDLER,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Disable event handler for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }
		else{
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Event Handler For This Service' TITLE='Enable Event Handler For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SVC_EVENT_HANDLER,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Enable event handler for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }

		if(temp_svcstatus.flap_detection_enabled==common_h.TRUE){
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Flap Detection For This Service' TITLE='Disable Flap Detection For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SVC_FLAP_DETECTION,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Disable flap detection for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }
		else{
			System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Flap Detection For This Service' TITLE='Enable Flap Detection For This Service'></td><td CLASS='command'><a href='%s?cmd_typ=%d&host=%s",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SVC_FLAP_DETECTION,cgiutils.url_encode(host_name));
			System.out.printf("&service=%s'>Enable flap detection for this service</a></td></tr>\n",cgiutils.url_encode(service_desc));
		        }

		System.out.printf("</table>\n");
	        }
	else{
		System.out.printf("<DIV CLASS='infoMessage'>It appears as though Blue is not running, so commands are temporarily unavailable...<br>\n");
		System.out.printf("Click <a href='%s?type=%d'>here</a> to view Blue process information</DIV>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_PROCESS_INFO);
	        }

	System.out.printf("</td></tr>\n");
	System.out.printf("</table>\n");

	System.out.printf("</TD>\n");

	System.out.printf("</TR></TABLE></TD>\n");
	System.out.printf("</TR>\n");

	System.out.printf("<TR><TD COLSPAN=2><BR></TD></TR>\n");

	System.out.printf("<TR>\n");
	System.out.printf("<TD COLSPAN=2 ALIGN=CENTER VALIGN=TOP CLASS='commentPanel'>\n");

	/* display comments */
	display_comments(comments_h.SERVICE_COMMENT);

	System.out.printf("</TD>\n");
	System.out.printf("</TR>\n");

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

	return;
	}




public static void show_hostgroup_info(){
	objects_h.hostgroup temp_hostgroup;


	/* get hostgroup info */
	temp_hostgroup=objects.find_hostgroup(hostgroup_name);

	/* make sure the user has rights to view hostgroup information */
	if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for this hostgroup...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	/* make sure hostgroup information exists */
	if(temp_hostgroup==null){
		System.out.printf("<P><DIV CLASS='errorMessage'>Error: Hostgroup Not Found!</DIV></P>");
		return;
		}


	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 WIDTH=100%%>\n");
	System.out.printf("<TR>\n");


	/* top left panel */
	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel'>\n");

	/* right top panel */
	System.out.printf("</TD><TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel' ROWSPAN=2>\n");

	System.out.printf("<DIV CLASS='dataTitle'>Hostgroup Commands</DIV>\n");

	if(cgiutils.blue_process_state==cgiutils_h.STATE_OK){

		System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");
		System.out.printf("<TR><TD>\n");

		System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule Downtime For All Hosts In This Hostgroup' TITLE='Schedule Downtime For All Hosts In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Schedule downtime for all hosts in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule Downtime For All Services In This Hostgroup' TITLE='Schedule Downtime For All Services In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Schedule downtime for all services in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For All Hosts In This Hostgroup' TITLE='Enable Notifications For All Hosts In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Enable notifications for all hosts in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATION_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For All Hosts In This Hostgroup' TITLE='Disable Notifications For All Hosts In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Disable notifications for all hosts in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For All Services In This Hostgroup' TITLE='Enable Notifications For All Services In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Enable notifications for all services in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATION_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For All Services In This Hostgroup' TITLE='Disable Notifications For All Services In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Disable notifications for all services in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Active Checks Of All Services In This Hostgroup' TITLE='Enable Active Checks Of All Services In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Enable active checks of all services in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS,cgiutils.url_encode(hostgroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Active Checks Of All Services In This Hostgroup' TITLE='Disable Active Checks Of All Services In This Hostgroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&hostgroup=%s'>Disable active checks of all services in this hostgroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS,cgiutils.url_encode(hostgroup_name));

		System.out.printf("</table>\n");

		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");
	        }
	else{
		System.out.printf("<DIV CLASS='infoMessage'>It appears as though Blue is not running, so commands are temporarily unavailable...<br>\n");
		System.out.printf("Click <a href='%s?type=%d'>here</a> to view Nagios process information</DIV>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_PROCESS_INFO);
	        }

	System.out.printf("</TD></TR>\n");
	System.out.printf("<TR>\n");

	/* left bottom panel */
	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel'>\n");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");


	System.out.printf("</div>\n");

	System.out.printf("</TD>\n");



	return;
	}

public static void show_servicegroup_info(){
	objects_h.servicegroup temp_servicegroup;


	/* get servicegroup info */
	temp_servicegroup=objects.find_servicegroup(servicegroup_name);

	/* make sure the user has rights to view servicegroup information */
	if(cgiauth.is_authorized_for_servicegroup(temp_servicegroup,current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information for this servicegroup...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	/* make sure servicegroup information exists */
	if(temp_servicegroup==null){
		System.out.printf("<P><DIV CLASS='errorMessage'>Error: Servicegroup Not Found!</DIV></P>");
		return;
		}


	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 WIDTH=100%%>\n");
	System.out.printf("<TR>\n");


	/* top left panel */
	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel'>\n");

	/* right top panel */
	System.out.printf("</TD><TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel' ROWSPAN=2>\n");

	System.out.printf("<DIV CLASS='dataTitle'>Servicegroup Commands</DIV>\n");

	if(cgiutils.blue_process_state==cgiutils_h.STATE_OK){

		System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");
		System.out.printf("<TR><TD>\n");

		System.out.printf("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0 CLASS='command'>\n");

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule Downtime For All Hosts In This Servicegroup' TITLE='Schedule Downtime For All Hosts In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Schedule downtime for all hosts in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Schedule Downtime For All Services In This Servicegroup' TITLE='Schedule Downtime For All Services In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Schedule downtime for all services in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For All Hosts In This Servicegroup' TITLE='Enable Notifications For All Hosts In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Enable notifications for all hosts in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATION_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For All Hosts In This Servicegroup' TITLE='Disable Notifications For All Hosts In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Disable notifications for all hosts in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Notifications For All Services In This Servicegroup' TITLE='Enable Notifications For All Services In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Enable notifications for all services in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATION_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Notifications For All Services In This Servicegroup' TITLE='Disable Notifications For All Services In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Disable notifications for all services in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.NOTIFICATIONS_DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Enable Active Checks Of All Services In This Servicegroup' TITLE='Enable Active Checks Of All Services In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Enable active checks of all services in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS,cgiutils.url_encode(servicegroup_name));

		System.out.printf("<tr CLASS='command'><td><img src='%s%s' border=0 ALT='Disable Active Checks Of All Services In This Servicegroup' TITLE='Disable Active Checks Of All Services In This Servicegroup'></td><td CLASS='command'><a href='%s?cmd_typ=%d&servicegroup=%s'>Disable active checks of all services in this servicegroup</a></td></tr>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON,cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS,cgiutils.url_encode(servicegroup_name));

		System.out.printf("</table>\n");

		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");
	        }
	else{
		System.out.printf("<DIV CLASS='infoMessage'>It appears as though Blue is not running, so commands are temporarily unavailable...<br>\n");
		System.out.printf("Click <a href='%s?type=%d'>here</a> to view Blue process information</DIV>\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_PROCESS_INFO);
	        }

	System.out.printf("</TD></TR>\n");
	System.out.printf("<TR>\n");

	/* left bottom panel */
	System.out.printf("<TD ALIGN=CENTER VALIGN=TOP CLASS='stateInfoPanel'>\n");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");


	System.out.printf("</div>\n");

	System.out.printf("</TD>\n");


	return;
        }



/* shows all service and host comments */
public static void show_all_comments(){
	int total_comments=0;
	String bg_class="";
	int odd=0;
	String date_time;
	comments_h.comment temp_comment;
    objects_h.host temp_host;
    objects_h.service temp_service;
	String comment_type;
	String expire_time;


	/* read in all comments */
	comments.read_comment_data( cgiutils.get_cgi_config_location());

	System.out.printf("<P>\n");
	System.out.printf("<DIV CLASS='commentNav'>[&nbsp;<A HREF='#HOSTCOMMENTS' CLASS='commentNav'>Host Comments</A>&nbsp;|&nbsp;<A HREF='#SERVICECOMMENTS' CLASS='commentNav'>Service Comments</A>&nbsp;]</DIV>\n");
	System.out.printf("</P>\n");

	System.out.printf("<A NAME=HOSTCOMMENTS></A>\n");
	System.out.printf("<DIV CLASS='commentTitle'>Host Comments</DIV>\n");

	System.out.printf("<div CLASS='comment'><img src='%s%s' border=0>&nbsp;",cgiutils.url_images_path,cgiutils_h.COMMENT_ICON);
	System.out.printf("<a href='%s?cmd_typ=%d'>",cgiutils_h.COMMAND_CGI,common_h.CMD_ADD_HOST_COMMENT);
	System.out.printf("Add a new host comment</a></div>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='comment'>\n");
	System.out.printf("<TR CLASS='comment'><TH CLASS='comment'>Host Name</TH><TH CLASS='comment'>Entry Time</TH><TH CLASS='comment'>Author</TH><TH CLASS='comment'>Comment</TH><TH CLASS='comment'>Comment ID</TH><TH CLASS='comment'>Persistent</TH><TH CLASS='comment'>Type</TH><TH CLASS='comment'>Expires</TH><TH CLASS='comment'>Actions</TH></TR>\n");

	/* display all the host comments */
	for( Iterator iter = comments.comment_list.iterator(); iter.hasNext(); ){
        temp_comment = (comments_h.comment) iter.next();

		if(temp_comment.comment_type!= comments_h.HOST_COMMENT)
			continue;

		temp_host=objects.find_host(temp_comment.host_name);

		/* make sure the user has rights to view host information */
		if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			continue;

		total_comments++;

		if(odd != 0 ){
			odd=0;
			bg_class="commentOdd";
		        }
		else{
			odd=1;
			bg_class="commentEven";
		        }

		switch(temp_comment.entry_type){
		case comments_h.USER_COMMENT:
			comment_type="User";
			break;
		case comments_h.DOWNTIME_COMMENT:
			comment_type="Scheduled Downtime";
			break;
		case comments_h.FLAPPING_COMMENT:
			comment_type="Flap Detection";
			break;
		case comments_h.ACKNOWLEDGEMENT_COMMENT:
			comment_type="Acknowledgement";
			break;
		default:
			comment_type="?";
		        }

        date_time = cgiutils.get_time_string(temp_comment.entry_time ,common_h.SHORT_DATE_TIME);
        expire_time = cgiutils.get_time_string(temp_comment.expire_time,common_h.SHORT_DATE_TIME);
		System.out.printf("<tr CLASS='%s'>",bg_class);
		System.out.printf("<td CLASS='%s'><A HREF='%s?type=%d&host=%s'>%s</A></td>",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_comment.host_name),temp_comment.host_name);
		System.out.printf("<td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%d</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td>",bg_class,date_time,bg_class,temp_comment.author,bg_class,temp_comment.comment_data,bg_class,temp_comment.comment_id,bg_class,(temp_comment.persistent!=0)?"Yes":"No",bg_class,comment_type,bg_class,(temp_comment.expires==common_h.TRUE)?expire_time:"N/A");
		System.out.printf("<td><a href='%s?cmd_typ=%d&com_id=%d'><img src='%s%s' border=0 ALT='Delete This Comment' TITLE='Delete This Comment'></td>",cgiutils_h.COMMAND_CGI,common_h.CMD_DEL_HOST_COMMENT,temp_comment.comment_id,cgiutils.url_images_path,cgiutils_h.DELETE_ICON);
		System.out.printf("</tr>\n");
	        }

	if(total_comments==0)
		System.out.printf("<TR CLASS='commentOdd'><TD CLASS='commentOdd' COLSPAN=9>There are no host comments</TD></TR>");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("<P><BR></P>\n");


	System.out.printf("<A NAME=SERVICECOMMENTS></A>\n");
	System.out.printf("<DIV CLASS='commentTitle'>Service Comments</DIV>\n");

	System.out.printf("<div CLASS='comment'><img src='%s%s' border=0>&nbsp;",cgiutils.url_images_path,cgiutils_h.COMMENT_ICON);
	System.out.printf("<a href='%s?cmd_typ=%d'>",cgiutils_h.COMMAND_CGI,common_h.CMD_ADD_SVC_COMMENT);
	System.out.printf("Add a new service comment</a></div>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='comment'>\n");
	System.out.printf("<TR CLASS='comment'><TH CLASS='comment'>Host Name</TH><TH CLASS='comment'>Service</TH><TH CLASS='comment'>Entry Time</TH><TH CLASS='comment'>Author</TH><TH CLASS='comment'>Comment</TH><TH CLASS='comment'>Comment ID</TH><TH CLASS='comment'>Persistent</TH><TH CLASS='comment'>Type</TH><TH CLASS='comment'>Expires</TH><TH CLASS='comment'>Actions</TH></TR>\n");

	/* display all the service comments */
	for( Iterator iter = comments.comment_list.iterator(); iter.hasNext();  ){
        temp_comment = (comments_h.comment) iter.next();

		if(temp_comment.comment_type!=comments_h.SERVICE_COMMENT)
			continue;

		temp_service=objects.find_service(temp_comment.host_name,temp_comment.service_description);

		/* make sure the user has rights to view service information */
		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			continue;

		total_comments++;

		if(odd != 0){
			odd=0;
			bg_class="commentOdd";
		        }
		else{
			odd=1;
			bg_class="commentEven";
		        }

		switch(temp_comment.entry_type){
		case comments_h.USER_COMMENT:
			comment_type="User";
			break;
		case comments_h.DOWNTIME_COMMENT:
            comment_type="Scheduled Downtime";
			break;
		case comments_h.FLAPPING_COMMENT:
            comment_type="Flap Detection";
			break;
		case comments_h.ACKNOWLEDGEMENT_COMMENT:
            comment_type="Acknowledgement";
			break;
		default:
			comment_type="?";
		        }

        date_time = cgiutils.get_time_string(temp_comment.entry_time,common_h.SHORT_DATE_TIME);
        expire_time = cgiutils.get_time_string(temp_comment.expire_time,common_h.SHORT_DATE_TIME);
		System.out.printf("<tr CLASS='%s'>",bg_class);
		System.out.printf("<td CLASS='%s'><A HREF='%s?type=%d&host=%s'>%s</A></td>",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_comment.host_name),temp_comment.host_name);
		System.out.printf("<td CLASS='%s'><A HREF='%s?type=%d&host=%s",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_comment.host_name));
		System.out.printf("&service=%s'>%s</A></td>",cgiutils.url_encode(temp_comment.service_description),temp_comment.service_description);
		System.out.printf("<td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%d</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td>",bg_class,date_time,bg_class,temp_comment.author,bg_class,temp_comment.comment_data,bg_class,temp_comment.comment_id,bg_class,(temp_comment.persistent!=0)?"Yes":"No",bg_class,comment_type,bg_class,(temp_comment.expires==common_h.TRUE)?expire_time:"N/A");
		System.out.printf("<td><a href='%s?cmd_typ=%d&com_id=%d'><img src='%s%s' border=0 ALT='Delete This Comment' TITLE='Delete This Comment'></td>",cgiutils_h.COMMAND_CGI,common_h.CMD_DEL_SVC_COMMENT,temp_comment.comment_id,cgiutils.url_images_path,cgiutils_h.DELETE_ICON);
		System.out.printf("</tr>\n");
	        }

	if(total_comments==0)
		System.out.printf("<TR CLASS='commentOdd'><TD CLASS='commentOdd' COLSPAN=10>There are no service comments</TD></TR>");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	return;
        }



public static void show_performance_data(){
	objects_h.service temp_service=null;
    statusdata_h.servicestatus temp_servicestatus=null;
    objects_h.host temp_host=null;
	statusdata_h.hoststatus temp_hoststatus=null;
	int total_active_service_checks=0;
	int total_passive_service_checks=0;
	double min_service_execution_time=0.0;
	double max_service_execution_time=0.0;
	double total_service_execution_time=0.0;
	int have_min_service_execution_time=common_h.FALSE;
	int have_max_service_execution_time=common_h.FALSE;
	double min_service_latency=0.0;
	double max_service_latency=0.0;
	double total_service_latency=0.0;
	int have_min_service_latency=common_h.FALSE;
	int have_max_service_latency=common_h.FALSE;
	double min_host_latency=0.0;
	double max_host_latency=0.0;
	double total_host_latency=0.0;
	int have_min_host_latency=common_h.FALSE;
	int have_max_host_latency=common_h.FALSE;
	double min_service_percent_change_a=0.0;
	double max_service_percent_change_a=0.0;
	double total_service_percent_change_a=0.0;
	int have_min_service_percent_change_a=common_h.FALSE;
	int have_max_service_percent_change_a=common_h.FALSE;
	double min_service_percent_change_b=0.0;
	double max_service_percent_change_b=0.0;
	double total_service_percent_change_b=0.0;
	int have_min_service_percent_change_b=common_h.FALSE;
	int have_max_service_percent_change_b=common_h.FALSE;
	int active_service_checks_1min=0;
	int active_service_checks_5min=0;
	int active_service_checks_15min=0;
	int active_service_checks_1hour=0;
	int active_service_checks_start=0;
	int active_service_checks_ever=0;
	int passive_service_checks_1min=0;
	int passive_service_checks_5min=0;
	int passive_service_checks_15min=0;
	int passive_service_checks_1hour=0;
	int passive_service_checks_start=0;
	int passive_service_checks_ever=0;
	int total_active_host_checks=0;
	int total_passive_host_checks=0;
	double min_host_execution_time=0.0;
	double max_host_execution_time=0.0;
	double total_host_execution_time=0.0;
	int have_min_host_execution_time=common_h.FALSE;
	int have_max_host_execution_time=common_h.FALSE;
	double min_host_percent_change_a=0.0;
	double max_host_percent_change_a=0.0;
	double total_host_percent_change_a=0.0;
	int have_min_host_percent_change_a=common_h.FALSE;
	int have_max_host_percent_change_a=common_h.FALSE;
	double min_host_percent_change_b=0.0;
	double max_host_percent_change_b=0.0;
	double total_host_percent_change_b=0.0;
	int have_min_host_percent_change_b=common_h.FALSE;
	int have_max_host_percent_change_b=common_h.FALSE;
	int active_host_checks_1min=0;
	int active_host_checks_5min=0;
	int active_host_checks_15min=0;
	int active_host_checks_1hour=0;
	int active_host_checks_start=0;
	int active_host_checks_ever=0;
	int passive_host_checks_1min=0;
	int passive_host_checks_5min=0;
	int passive_host_checks_15min=0;
	int passive_host_checks_1hour=0;
	int passive_host_checks_start=0;
	int passive_host_checks_ever=0;
	long current_time;


	current_time = utils.currentTimeInSeconds();

	/* check all services */
	for( Iterator iter = statusdata.servicestatus_list.iterator(); iter.hasNext();  ){
        temp_servicestatus = (statusdata_h.servicestatus) iter.next();

		/* find the service */
		temp_service=objects.find_service(temp_servicestatus.host_name,temp_servicestatus.description);
		
		/* make sure the user has rights to view service information */
		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			continue;

		/* is this an active or passive check? */
		if(temp_servicestatus.check_type==common_h.SERVICE_CHECK_ACTIVE){

			total_active_service_checks++;

			total_service_execution_time+=temp_servicestatus.execution_time;
			if(have_min_service_execution_time==common_h.FALSE || temp_servicestatus.execution_time<min_service_execution_time){
				have_min_service_execution_time=common_h.TRUE;
				min_service_execution_time=temp_servicestatus.execution_time;
			        }
			if(have_max_service_execution_time==common_h.FALSE || temp_servicestatus.execution_time>max_service_execution_time){
				have_max_service_execution_time=common_h.TRUE;
				max_service_execution_time=temp_servicestatus.execution_time;
			        }

			total_service_percent_change_a+=temp_servicestatus.percent_state_change;
			if(have_min_service_percent_change_a==common_h.FALSE || temp_servicestatus.percent_state_change<min_service_percent_change_a){
				have_min_service_percent_change_a=common_h.TRUE;
				min_service_percent_change_a=temp_servicestatus.percent_state_change;
			        }
			if(have_max_service_percent_change_a==common_h.FALSE || temp_servicestatus.percent_state_change>max_service_percent_change_a){
				have_max_service_percent_change_a=common_h.TRUE;
				max_service_percent_change_a=temp_servicestatus.percent_state_change;
			        }

			total_service_latency+=temp_servicestatus.latency;
			if(have_min_service_latency==common_h.FALSE || temp_servicestatus.latency<min_service_latency){
				have_min_service_latency=common_h.TRUE;
				min_service_latency=temp_servicestatus.latency;
			        }
			if(have_max_service_latency==common_h.FALSE || temp_servicestatus.latency>max_service_latency){
				have_max_service_latency=common_h.TRUE;
				max_service_latency=temp_servicestatus.latency;
			        }

			if(temp_servicestatus.last_check>=(current_time-60))
				active_service_checks_1min++;
			if(temp_servicestatus.last_check>=(current_time-300))
				active_service_checks_5min++;
			if(temp_servicestatus.last_check>=(current_time-900))
				active_service_checks_15min++;
			if(temp_servicestatus.last_check>=(current_time-3600))
				active_service_checks_1hour++;
			if(temp_servicestatus.last_check>=blue.program_start)
				active_service_checks_start++;
			if(temp_servicestatus.last_check!=0)
				active_service_checks_ever++;
		        }

		else{
			total_passive_service_checks++;

			total_service_percent_change_b+=temp_servicestatus.percent_state_change;
			if(have_min_service_percent_change_b==common_h.FALSE || temp_servicestatus.percent_state_change<min_service_percent_change_b){
				have_min_service_percent_change_b=common_h.TRUE;
				min_service_percent_change_b=temp_servicestatus.percent_state_change;
			        }
			if(have_max_service_percent_change_b==common_h.FALSE || temp_servicestatus.percent_state_change>max_service_percent_change_b){
				have_max_service_percent_change_b=common_h.TRUE;
				max_service_percent_change_b=temp_servicestatus.percent_state_change;
			        }

			if(temp_servicestatus.last_check>=(current_time-60))
				passive_service_checks_1min++;
			if(temp_servicestatus.last_check>=(current_time-300))
				passive_service_checks_5min++;
			if(temp_servicestatus.last_check>=(current_time-900))
				passive_service_checks_15min++;
			if(temp_servicestatus.last_check>=(current_time-3600))
				passive_service_checks_1hour++;
			if(temp_servicestatus.last_check>=blue.program_start)
				passive_service_checks_start++;
			if(temp_servicestatus.last_check!=0)
				passive_service_checks_ever++;
		        }
	        }

	/* check all hosts */
	for( Iterator iter = statusdata.hoststatus_list.iterator(); iter.hasNext(); ){
        temp_hoststatus= (statusdata_h.hoststatus) iter.next();

		/* find the host */
		temp_host=objects.find_host(temp_hoststatus.host_name);
		
		/* make sure the user has rights to view host information */
		if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			continue;

		/* is this an active or passive check? */
		if(temp_hoststatus.check_type==common_h.HOST_CHECK_ACTIVE){

			total_active_host_checks++;

			total_host_execution_time+=temp_hoststatus.execution_time;
			if(have_min_host_execution_time==common_h.FALSE || temp_hoststatus.execution_time<min_host_execution_time){
				have_min_host_execution_time=common_h.TRUE;
				min_host_execution_time=temp_hoststatus.execution_time;
			        }
			if(have_max_host_execution_time==common_h.FALSE || temp_hoststatus.execution_time>max_host_execution_time){
				have_max_host_execution_time=common_h.TRUE;
				max_host_execution_time=temp_hoststatus.execution_time;
			        }

			total_host_percent_change_a+=temp_hoststatus.percent_state_change;
			if(have_min_host_percent_change_a==common_h.FALSE || temp_hoststatus.percent_state_change<min_host_percent_change_a){
				have_min_host_percent_change_a=common_h.TRUE;
				min_host_percent_change_a=temp_hoststatus.percent_state_change;
			        }
			if(have_max_host_percent_change_a==common_h.FALSE || temp_hoststatus.percent_state_change>max_host_percent_change_a){
				have_max_host_percent_change_a=common_h.TRUE;
				max_host_percent_change_a=temp_hoststatus.percent_state_change;
			        }

			total_host_latency+=temp_hoststatus.latency;
			if(have_min_host_latency==common_h.FALSE || temp_hoststatus.latency<min_host_latency){
				have_min_host_latency=common_h.TRUE;
				min_host_latency=temp_hoststatus.latency;
			        }
			if(have_max_host_latency==common_h.FALSE || temp_hoststatus.latency>max_host_latency){
				have_max_host_latency=common_h.TRUE;
				max_host_latency=temp_hoststatus.latency;
			        }

			if(temp_hoststatus.last_check>=(current_time-60))
				active_host_checks_1min++;
			if(temp_hoststatus.last_check>=(current_time-300))
				active_host_checks_5min++;
			if(temp_hoststatus.last_check>=(current_time-900))
				active_host_checks_15min++;
			if(temp_hoststatus.last_check>=(current_time-3600))
				active_host_checks_1hour++;
			if(temp_hoststatus.last_check>=blue.program_start)
				active_host_checks_start++;
			if(temp_hoststatus.last_check!=0)
				active_host_checks_ever++;
		        }

		else{
			total_passive_host_checks++;

			total_host_percent_change_b+=temp_hoststatus.percent_state_change;
			if(have_min_host_percent_change_b==common_h.FALSE || temp_hoststatus.percent_state_change<min_host_percent_change_b){
				have_min_host_percent_change_b=common_h.TRUE;
				min_host_percent_change_b=temp_hoststatus.percent_state_change;
			        }
			if(have_max_host_percent_change_b==common_h.FALSE || temp_hoststatus.percent_state_change>max_host_percent_change_b){
				have_max_host_percent_change_b=common_h.TRUE;
				max_host_percent_change_b=temp_hoststatus.percent_state_change;
			        }

			if(temp_hoststatus.last_check>=(current_time-60))
				passive_host_checks_1min++;
			if(temp_hoststatus.last_check>=(current_time-300))
				passive_host_checks_5min++;
			if(temp_hoststatus.last_check>=(current_time-900))
				passive_host_checks_15min++;
			if(temp_hoststatus.last_check>=(current_time-3600))
				passive_host_checks_1hour++;
			if(temp_hoststatus.last_check>=blue.program_start)
				passive_host_checks_start++;
			if(temp_hoststatus.last_check!=0)
				passive_host_checks_ever++;
		        }
	        }


	System.out.printf("<div align=center>\n");


	System.out.printf("<DIV CLASS='dataTitle'>Program-Wide Performance Information</DIV>\n");

	System.out.printf("<table border=0 cellpadding=10>\n");


	/***** ACTIVE SERVICE CHECKS *****/

	System.out.printf("<tr>\n");
	System.out.printf("<td valign=center><div class='perfTypeTitle'>Active Service Checks:</div></td>\n");
	System.out.printf("<td valign=top>\n");

	/* fake this so we don't divide by zero for just showing the table */
	if(total_active_service_checks==0)
		total_active_service_checks=1;

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable1'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Time Frame</th><th class='data'>Checks Completed</th></tr>\n");
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 minute:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_service_checks_1min,((active_service_checks_1min*100.0)/total_active_service_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 5 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_service_checks_5min,((active_service_checks_5min*100.0)/total_active_service_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 15 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_service_checks_15min,((active_service_checks_15min*100.0)/total_active_service_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 hour:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_service_checks_1hour,((active_service_checks_1hour*100.0)/total_active_service_checks));
	System.out.printf("<tr><td class='dataVar'>Since program start:&nbsp;&nbsp;</td><td class='dataVal'>%d (%.1f%%)</td>",active_service_checks_start,((active_service_checks_start*100.0)/total_active_service_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</td><td valign=top>\n");

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable2'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Metric</th><th class='data'>Min.</th><th class='data'>Max.</th><th class='data'>Average</th></tr>\n");

	System.out.printf("<tr><td class='dataVar'>Check Execution Time:&nbsp;&nbsp;</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.3f sec</td></tr>\n",min_service_execution_time,max_service_execution_time,(total_service_execution_time/total_active_service_checks));

	System.out.printf("<tr><td class='dataVar'>Check Latency:</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.3f sec</td></tr>\n",min_service_latency,max_service_latency,(total_service_latency/total_active_service_checks));

	System.out.printf("<tr><td class='dataVar'>Percent State Change:</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td></tr>\n",min_service_percent_change_a,max_service_percent_change_a,(total_service_percent_change_a/total_active_service_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");


	System.out.printf("</td>\n");
	System.out.printf("</tr>\n");


	/***** PASSIVE SERVICE CHECKS *****/

	System.out.printf("<tr>\n");
	System.out.printf("<td valign=center><div class='perfTypeTitle'>Passive Service Checks:</div></td>\n");
	System.out.printf("<td valign=top>\n");
	

	/* fake this so we don't divide by zero for just showing the table */
	if(total_passive_service_checks==0)
		total_passive_service_checks=1;

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable1'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Time Frame</th><th class='data'>Checks Completed</th></tr>\n");
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 minute:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_service_checks_1min,((passive_service_checks_1min*100.0)/total_passive_service_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 5 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_service_checks_5min,((passive_service_checks_5min*100.0)/total_passive_service_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 15 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_service_checks_15min,((passive_service_checks_15min*100.0)/total_passive_service_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 hour:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_service_checks_1hour,((passive_service_checks_1hour*100.0)/total_passive_service_checks));
	System.out.printf("<tr><td class='dataVar'>Since program start:&nbsp;&nbsp;</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_service_checks_start,((passive_service_checks_start*100.0)/total_passive_service_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</td><td valign=top>\n");

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable2'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Metric</th><th class='data'>Min.</th><th class='data'>Max.</th><th class='data'>Average</th></tr>\n");
	System.out.printf("<tr><td class='dataVar'>Percent State Change:&nbsp;&nbsp;</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td></tr>\n",min_service_percent_change_b,max_service_percent_change_b,(total_service_percent_change_b/total_passive_service_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</td>\n");
	System.out.printf("</tr>\n");


	/***** ACTIVE HOST CHECKS *****/

	System.out.printf("<tr>\n");
	System.out.printf("<td valign=center><div class='perfTypeTitle'>Active Host Checks:</div></td>\n");
	System.out.printf("<td valign=top>\n");

	/* fake this so we don't divide by zero for just showing the table */
	if(total_active_host_checks==0)
		total_active_host_checks=1;

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable1'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Time Frame</th><th class='data'>Checks Completed</th></tr>\n");
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 minute:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_host_checks_1min,((active_host_checks_1min*100.0)/total_active_host_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 5 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_host_checks_5min,((active_host_checks_5min*100.0)/total_active_host_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 15 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_host_checks_15min,((active_host_checks_15min*100.0)/total_active_host_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 hour:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",active_host_checks_1hour,((active_host_checks_1hour*100.0)/total_active_host_checks));
	System.out.printf("<tr><td class='dataVar'>Since program start:&nbsp;&nbsp;</td><td class='dataVal'>%d (%.1f%%)</td>",active_host_checks_start,((active_host_checks_start*100.0)/total_active_host_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</td><td valign=top>\n");

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable2'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Metric</th><th class='data'>Min.</th><th class='data'>Max.</th><th class='data'>Average</th></tr>\n");

	System.out.printf("<tr><td class='dataVar'>Check Execution Time:&nbsp;&nbsp;</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.3f sec</td></tr>\n",min_host_execution_time,max_host_execution_time,(total_host_execution_time/total_active_host_checks));

	System.out.printf("<tr><td class='dataVar'>Check Latency:</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.2f sec</td><td class='dataVal'>%.3f sec</td></tr>\n",min_host_latency,max_host_latency,(total_host_latency/total_active_host_checks));

	System.out.printf("<tr><td class='dataVar'>Percent State Change:</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td></tr>\n",min_host_percent_change_a,max_host_percent_change_a,(total_host_percent_change_a/total_active_host_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");


	System.out.printf("</td>\n");
	System.out.printf("</tr>\n");


	/***** PASSIVE HOST CHECKS *****/

	System.out.printf("<tr>\n");
	System.out.printf("<td valign=center><div class='perfTypeTitle'>Passive Host Checks:</div></td>\n");
	System.out.printf("<td valign=top>\n");
	

	/* fake this so we don't divide by zero for just showing the table */
	if(total_passive_host_checks==0)
		total_passive_host_checks=1;

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable1'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Time Frame</th><th class='data'>Checks Completed</th></tr>\n");
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 minute:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_host_checks_1min,((passive_host_checks_1min*100.0)/total_passive_host_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 5 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_host_checks_5min,((passive_host_checks_5min*100.0)/total_passive_host_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 15 minutes:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_host_checks_15min,((passive_host_checks_15min*100.0)/total_passive_host_checks));
	System.out.printf("<tr><td class='dataVar'>&lt;= 1 hour:</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_host_checks_1hour,((passive_host_checks_1hour*100.0)/total_passive_host_checks));
	System.out.printf("<tr><td class='dataVar'>Since program start:&nbsp;&nbsp;</td><td class='dataVal'>%d (%.1f%%)</td></tr>",passive_host_checks_start,((passive_host_checks_start*100.0)/total_passive_host_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</td><td valign=top>\n");

	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	System.out.printf("<TR><TD class='stateInfoTable2'>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr class='data'><th class='data'>Metric</th><th class='data'>Min.</th><th class='data'>Max.</th><th class='data'>Average</th></tr>\n");
	System.out.printf("<tr><td class='dataVar'>Percent State Change:&nbsp;&nbsp;</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td><td class='dataVal'>%.2f%%</td></tr>\n",min_host_percent_change_b,max_host_percent_change_b,(total_host_percent_change_b/total_passive_host_checks));

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("</td>\n");
	System.out.printf("</tr>\n");


	System.out.printf("</table>\n");


	System.out.printf("</div>\n");

	return;
        }



public static void display_comments(int type){
	objects_h.host temp_host=null;
	objects_h.service temp_service=null;
	int total_comments=0;
	int display_comment=common_h.FALSE;
	String bg_class="";
	int odd=1;
	String date_time;
	comments_h.comment temp_comment;
	String comment_type;
	String expire_time;


	/* find the host or service */
	if(type==comments_h.HOST_COMMENT){
		temp_host=objects.find_host(host_name);
		if(temp_host==null)
			return;
	        }
	else{
		temp_service=objects.find_service(host_name,service_desc);
		if(temp_service==null)
			return;
	        }


	System.out.printf("<A NAME=comments></A>\n");
	System.out.printf("<DIV CLASS='commentTitle'>%s Comments</DIV>\n",(type==comments_h.HOST_COMMENT)?"Host":"Service");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<tr><td valign=center><img src='%s%s' border=0 align=center></td><td CLASS='comment'>",cgiutils.url_images_path,cgiutils_h.COMMENT_ICON);
	if(type==comments_h.HOST_COMMENT)
		System.out.printf("<a href='%s?cmd_typ=%d&host=%s' CLASS='comment'>",cgiutils_h.COMMAND_CGI,common_h.CMD_ADD_HOST_COMMENT,cgiutils.url_encode(host_name));
	else{
		System.out.printf("<a href='%s?cmd_typ=%d&host=%s&",cgiutils_h.COMMAND_CGI,common_h.CMD_ADD_SVC_COMMENT,cgiutils.url_encode(host_name));
		System.out.printf("service=%s' CLASS='comment'>",cgiutils.url_encode(service_desc));
	        }
	System.out.printf("Add a new comment</a></td></tr>\n");

	System.out.printf("<tr><td valign=center><img src='%s%s' border=0 align=center></td><td CLASS='comment'>",cgiutils.url_images_path,cgiutils_h.DELETE_ICON);
	if(type==comments_h.HOST_COMMENT)
		System.out.printf("<a href='%s?cmd_typ=%d&host=%s' CLASS='comment'>",cgiutils_h.COMMAND_CGI,common_h.CMD_DEL_ALL_HOST_COMMENTS,cgiutils.url_encode(host_name));
	else{
		System.out.printf("<a href='%s?cmd_typ=%d&host=%s&",cgiutils_h.COMMAND_CGI,common_h.CMD_DEL_ALL_SVC_COMMENTS,cgiutils.url_encode(host_name));
		System.out.printf("service=%s' CLASS='comment'>",cgiutils.url_encode(service_desc));
	        }
	System.out.printf("Delete all comments</a></td></tr>\n");

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");


	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='comment'>\n");
	System.out.printf("<TR CLASS='comment'><TH CLASS='comment'>Entry Time</TH><TH CLASS='comment'>Author</TH><TH CLASS='comment'>Comment</TH><TH CLASS='comment'>Comment ID</TH><TH CLASS='comment'>Persistent</TH><TH CLASS='comment'>Type</TH><TH CLASS='comment'>Expires</TH><TH CLASS='comment'>Actions</TH></TR>\n");

	/* read in all comments */
	comments.read_comment_data(cgiutils.get_cgi_config_location());

	/* check all the comments to see if they apply to this host or service */
    ArrayList list = comments.get_comment_list_by_host(host_name);
    if ( list != null ) {
	for( Iterator iter = list.iterator(); iter.hasNext(); ){
        temp_comment = (comments_h.comment) iter.next();
        
		display_comment=common_h.FALSE;

		if(type==comments_h.HOST_COMMENT && temp_comment.comment_type==comments_h.HOST_COMMENT)
			display_comment=common_h.TRUE;

		else if(type==comments_h.SERVICE_COMMENT && temp_comment.comment_type==comments_h.SERVICE_COMMENT && temp_comment.service_description.equals(service_desc))
			display_comment=common_h.TRUE;

		if(display_comment==common_h.TRUE){

			if(odd != 0 ){
				odd=0;
				bg_class="commentOdd";
			        }
			else{
				odd=1;
				bg_class="commentEven";
			        }

			switch(temp_comment.entry_type){
			case comments_h.USER_COMMENT:
				comment_type="User";
				break;
			case comments_h.DOWNTIME_COMMENT:
				comment_type="Scheduled Downtime";
				break;
			case comments_h.FLAPPING_COMMENT:
				comment_type="Flap Detection";
				break;
			case comments_h.ACKNOWLEDGEMENT_COMMENT:
				comment_type="Acknowledgement";
				break;
			default:
				comment_type="?";
			        }

            date_time = cgiutils.get_time_string(temp_comment.entry_time, common_h.SHORT_DATE_TIME);
            expire_time = cgiutils.get_time_string(temp_comment.expire_time,common_h.SHORT_DATE_TIME);
			System.out.printf("<tr CLASS='%s'>",bg_class);
			System.out.printf("<td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%d</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td><td CLASS='%s'>%s</td>",bg_class,date_time,bg_class,temp_comment.author,bg_class,temp_comment.comment_data,bg_class,temp_comment.comment_id,bg_class,(temp_comment.persistent!=0)?"Yes":"No",bg_class,comment_type,bg_class,(temp_comment.expires==common_h.TRUE)?expire_time:"N/A");
			System.out.printf("<td><a href='%s?cmd_typ=%d&com_id=%d'><img src='%s%s' border=0 ALT='Delete This Comment' TITLE='Delete This Comment'></td>",cgiutils_h.COMMAND_CGI,(type==comments_h.HOST_COMMENT)?common_h.CMD_DEL_HOST_COMMENT:common_h.CMD_DEL_SVC_COMMENT,temp_comment.comment_id,cgiutils.url_images_path,cgiutils_h.DELETE_ICON);
			System.out.printf("</tr>\n");

			total_comments++;
			}
	        }
    }

	/* see if this host or service has any comments associated with it */
	if(total_comments==0)
		System.out.printf("<TR CLASS='commentOdd'><TD CLASS='commentOdd' COLSPAN='%d'>This %s has no comments associated with it</TD></TR>",(type==comments_h.HOST_COMMENT)?9:10,(type==comments_h.HOST_COMMENT)?"host":"service");

	System.out.printf("</TABLE>\n");

	return;
        }




/* shows all service and host scheduled downtime */
public static void show_all_downtime(){
	int total_downtime=0;
	String bg_class="";
	int odd=0;
	String date_time;
	downtime_h.scheduled_downtime temp_downtime;
	objects_h.host temp_host;
	objects_h.service temp_service;

	/* read in all downtime */
	downtime.read_downtime_data(cgiutils.get_cgi_config_location());

	System.out.printf("<P>\n");
	System.out.printf("<DIV CLASS='downtimeNav'>[&nbsp;<A HREF='#HOSTDOWNTIME' CLASS='downtimeNav'>Host Downtime</A>&nbsp;|&nbsp;<A HREF='#SERVICEDOWNTIME' CLASS='downtimeNav'>Service Downtime</A>&nbsp;]</DIV>\n");
	System.out.printf("</P>\n");

	System.out.printf("<A NAME=HOSTDOWNTIME></A>\n");
	System.out.printf("<DIV CLASS='downtimeTitle'>Scheduled Host Downtime</DIV>\n");

	System.out.printf("<div CLASS='comment'><img src='%s%s' border=0>&nbsp;",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON);
	System.out.printf("<a href='%s?cmd_typ=%d'>",cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOST_DOWNTIME);
	System.out.printf("Schedule host downtime</a></div>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='downtime'>\n");
	System.out.printf("<TR CLASS='downtime'><TH CLASS='downtime'>Host Name</TH><TH CLASS='downtime'>Entry Time</TH><TH CLASS='downtime'>Author</TH><TH CLASS='downtime'>Comment</TH><TH CLASS='downtime'>Start Time</TH><TH CLASS='downtime'>End Time</TH><TH CLASS='downtime'>Type</TH><TH CLASS='downtime'>Duration</TH><TH CLASS='downtime'>Downtime ID</TH><TH CLASS='downtime'>Trigger ID</TH><TH CLASS='downtime'>Actions</TH></TR>\n");

	/* display all the host downtime */
    total_downtime=0;
	for( Iterator iter = downtime.scheduled_downtime_list.iterator(); iter.hasNext(); ){
        temp_downtime= (downtime_h.scheduled_downtime) iter.next();

		if(temp_downtime.type!=common_h.HOST_DOWNTIME)
			continue;

		temp_host=objects.find_host(temp_downtime.host_name);

		/* make sure the user has rights to view host information */
		if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			continue;

		total_downtime++;

		if(odd != 0 ){
			odd=0;
			bg_class="downtimeOdd";
		        }
		else{
			odd=1;
			bg_class="downtimeEven";
		        }

		System.out.printf("<tr CLASS='%s'>",bg_class);
		System.out.printf("<td CLASS='%s'><A HREF='%s?type=%d&host=%s'>%s</A></td>",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_downtime.host_name),temp_downtime.host_name);
        date_time = cgiutils.get_time_string(temp_downtime.entry_time, common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,date_time);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,(temp_downtime.author==null)?"N/A":temp_downtime.author);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,(temp_downtime.comment==null)?"N/A":temp_downtime.comment);
        date_time = cgiutils.get_time_string(temp_downtime.start_time, common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,date_time);
        date_time = cgiutils.get_time_string(temp_downtime.end_time, common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,date_time);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,(temp_downtime.fixed==common_h.TRUE)?"Fixed":"Flexible");
		cgiutils.time_breakdown tb = cgiutils.get_time_breakdown(temp_downtime.duration);
		System.out.printf("<td CLASS='%s'>%dd %dh %dm %ds</td>",bg_class,tb.days,tb.hours,tb.minutes,tb.seconds);
		System.out.printf("<td CLASS='%s'>%d</td>",bg_class,temp_downtime.downtime_id);
		System.out.printf("<td CLASS='%s'>",bg_class);
		if(temp_downtime.triggered_by==0)
			System.out.printf("N/A");
		else
			System.out.printf("%d",temp_downtime.triggered_by);
		System.out.printf("</td>\n");
		System.out.printf("<td><a href='%s?cmd_typ=%d&down_id=%d'><img src='%s%s' border=0 ALT='Delete/Cancel This Scheduled Downtime Entry' TITLE='Delete/Cancel This Scheduled Downtime Entry'></td>",cgiutils_h.COMMAND_CGI,common_h.CMD_DEL_HOST_DOWNTIME,temp_downtime.downtime_id,cgiutils.url_images_path,cgiutils_h.DELETE_ICON);
		System.out.printf("</tr>\n");
	        }

	if(total_downtime==0)
		System.out.printf("<TR CLASS='downtimeOdd'><TD CLASS='downtimeOdd' COLSPAN=11>There are no hosts with scheduled downtime</TD></TR>");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	System.out.printf("<P><BR></P>\n");


	System.out.printf("<A NAME=SERVICEDOWNTIME></A>\n");
	System.out.printf("<DIV CLASS='downtimeTitle'>Scheduled Service Downtime</DIV>\n");

	System.out.printf("<div CLASS='comment'><img src='%s%s' border=0>&nbsp;",cgiutils.url_images_path,cgiutils_h.DOWNTIME_ICON);
	System.out.printf("<a href='%s?cmd_typ=%d'>",cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_SVC_DOWNTIME);
	System.out.printf("Schedule service downtime</a></div>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='downtime'>\n");
	System.out.printf("<TR CLASS='downtime'><TH CLASS='downtime'>Host Name</TH><TH CLASS='downtime'>Service</TH><TH CLASS='downtime'>Entry Time</TH><TH CLASS='downtime'>Author</TH><TH CLASS='downtime'>Comment</TH><TH CLASS='downtime'>Start Time</TH><TH CLASS='downtime'>End Time</TH><TH CLASS='downtime'>Type</TH><TH CLASS='downtime'>Duration</TH><TH CLASS='downtime'>Downtime ID</TH><TH CLASS='downtime'>Trigger ID</TH><TH CLASS='downtime'>Actions</TH></TR>\n");

	/* display all the service downtime */
    total_downtime = 0;
	for( Iterator iter = downtime.scheduled_downtime_list.iterator(); iter.hasNext(); ){
	    temp_downtime = (downtime_h.scheduled_downtime) iter.next();
        
		if(temp_downtime.type!=common_h.SERVICE_DOWNTIME)
			continue;

		temp_service=objects.find_service(temp_downtime.host_name,temp_downtime.service_description);

		/* make sure the user has rights to view service information */
		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			continue;

		total_downtime++;

		if(odd!=0){
			odd=0;
			bg_class="downtimeOdd";
		        }
		else{
			odd=1;
			bg_class="downtimeEven";
		        }

		System.out.printf("<tr CLASS='%s'>",bg_class);
		System.out.printf("<td CLASS='%s'><A HREF='%s?type=%d&host=%s'>%s</A></td>",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_downtime.host_name),temp_downtime.host_name);
		System.out.printf("<td CLASS='%s'><A HREF='%s?type=%d&host=%s",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_downtime.host_name));
		System.out.printf("&service=%s'>%s</A></td>",cgiutils.url_encode(temp_downtime.service_description),temp_downtime.service_description);
        date_time = cgiutils.get_time_string(temp_downtime.entry_time,common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,date_time);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,(temp_downtime.author==null)?"N/A":temp_downtime.author);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,(temp_downtime.comment==null)?"N/A":temp_downtime.comment);
        date_time = cgiutils.get_time_string(temp_downtime.start_time,common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,date_time);
        date_time = cgiutils.get_time_string(temp_downtime.end_time,common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,date_time);
		System.out.printf("<td CLASS='%s'>%s</td>",bg_class,(temp_downtime.fixed==common_h.TRUE)?"Fixed":"Flexible");
		cgiutils.time_breakdown tb = cgiutils.get_time_breakdown(temp_downtime.duration );
		System.out.printf("<td CLASS='%s'>%dd %dh %dm %ds</td>",bg_class,tb.days,tb.hours,tb.minutes,tb.seconds);
		System.out.printf("<td CLASS='%s'>%d</td>",bg_class,temp_downtime.downtime_id);
		System.out.printf("<td CLASS='%s'>",bg_class);
		if(temp_downtime.triggered_by==0)
			System.out.printf("N/A");
		else
			System.out.printf("%d",temp_downtime.triggered_by);
		System.out.printf("</td>\n");
		System.out.printf("<td><a href='%s?cmd_typ=%d&down_id=%d'><img src='%s%s' border=0 ALT='Delete/Cancel This Scheduled Downtime Entry' TITLE='Delete/Cancel This Scheduled Downtime Entry'></td>",cgiutils_h.COMMAND_CGI,common_h.CMD_DEL_SVC_DOWNTIME,temp_downtime.downtime_id,cgiutils.url_images_path,cgiutils_h.DELETE_ICON);
		System.out.printf("</tr>\n");
	        }

	if(total_downtime==0)
		System.out.printf("<TR CLASS='downtimeOdd'><TD CLASS='downtimeOdd' COLSPAN=12>There are no services with scheduled downtime</TD></TR>");

	System.out.printf("</TD></TR>\n");
	System.out.printf("</TABLE>\n");

	return;
        }



/* shows check scheduling queue */
public static void show_scheduling_queue(){
//	sortdata temp_sortdata;
	statusdata_h.servicestatus temp_svcstatus=null;
	statusdata_h.hoststatus temp_hststatus=null;
	String date_time;
	String temp_url;
	int odd=0;
	String bgclass="";


	/* make sure the user has rights to view system information */
	if(cgiauth.is_authorized_for_system_information(current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view process information...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	/* sort hosts and services */
	sort_data(sort_type,sort_option);

	System.out.printf("<DIV ALIGN=CENTER CLASS='statusSort'>Entries sorted by <b>");
	if(sort_option==cgiutils_h.SORT_HOSTNAME)
		System.out.printf("host name");
	else if(sort_option==cgiutils_h.SORT_SERVICENAME)
		System.out.printf("service name");
	else if(sort_option==cgiutils_h.SORT_SERVICESTATUS)
		System.out.printf("service status");
	else if(sort_option==cgiutils_h.SORT_LASTCHECKTIME)
		System.out.printf("last check time");
	else if(sort_option==cgiutils_h.SORT_NEXTCHECKTIME)
		System.out.printf("next check time");
	System.out.printf("</b> (%s)\n",(sort_type==cgiutils_h.SORT_ASCENDING)?"ascending":"descending");
	System.out.printf("</DIV>\n");

	System.out.printf("<P>\n");
	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='queue'>\n");
	System.out.printf("<TR CLASS='queue'>");

	temp_url = String.format("%s?type=%d",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SCHEDULING_QUEUE);

	System.out.printf("<TH CLASS='queue'>Host&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host name (ascending)' TITLE='Sort by host name (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by host name (descending)' TITLE='Sort by host name (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_HOSTNAME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_HOSTNAME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='queue'>Service&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by service name (ascending)' TITLE='Sort by service name (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by service name (descending)' TITLE='Sort by service name (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_SERVICENAME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_SERVICENAME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='queue'>Last Check&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by last check time (ascending)' TITLE='Sort by last check time (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by last check time (descending)' TITLE='Sort by last check time (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_LASTCHECKTIME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_LASTCHECKTIME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);

	System.out.printf("<TH CLASS='queue'>Next Check&nbsp;<A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by next check time (ascending)' TITLE='Sort by next check time (ascending)'></A><A HREF='%s&sorttype=%d&sortoption=%d'><IMG SRC='%s%s' BORDER=0 ALT='Sort by next check time (descending)' TITLE='Sort by next check time (descending)'></A></TH>",temp_url,cgiutils_h.SORT_ASCENDING,cgiutils_h.SORT_NEXTCHECKTIME,cgiutils.url_images_path,cgiutils_h.UP_ARROW_ICON,temp_url,cgiutils_h.SORT_DESCENDING,cgiutils_h.SORT_NEXTCHECKTIME,cgiutils.url_images_path,cgiutils_h.DOWN_ARROW_ICON);


	System.out.printf("<TH CLASS='queue'>Active Checks</TH><TH CLASS='queue'>Actions</TH></TR>\n");


	/* display all services and hosts */
	for( sortdata temp_sortdata : sortdata_list ){

		/* skip hosts and services that shouldn't be scheduled */
		if(temp_sortdata.is_service==common_h.TRUE){
			temp_svcstatus=temp_sortdata.svcstatus;
			if(temp_svcstatus.should_be_scheduled==common_h.FALSE)
				continue;
		        }
		else{
			temp_hststatus=temp_sortdata.hststatus;
			if(temp_hststatus.should_be_scheduled==common_h.FALSE)
				continue;
		        }
		
		if(odd!=0){
			odd=0;
			bgclass="Even";
		        }
		else{
			odd=1;
			bgclass="Odd";
		        }

		System.out.printf("<TR CLASS='queue%s'>",bgclass);

		/* get the service status */
		if(temp_sortdata.is_service==common_h.TRUE){
			
			System.out.printf("<TD CLASS='queue%s'><A HREF='%s?type=%d&host=%s'>%s</A></TD>",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_svcstatus.host_name),temp_svcstatus.host_name);
			
			System.out.printf("<TD CLASS='queue%s'><A HREF='%s?type=%d&host=%s",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_svcstatus.host_name));
			System.out.printf("&service=%s'>%s</A></TD>",cgiutils.url_encode(temp_svcstatus.description),temp_svcstatus.description);

            date_time = cgiutils.get_time_string( temp_svcstatus.last_check, common_h.SHORT_DATE_TIME);
			System.out.printf("<TD CLASS='queue%s'>%s</TD>",bgclass,(temp_svcstatus.last_check==0)?"N/A":date_time);

            date_time = cgiutils.get_time_string(temp_svcstatus.next_check,common_h.SHORT_DATE_TIME);
			System.out.printf("<TD CLASS='queue%s'>%s</TD>",bgclass,(temp_svcstatus.next_check==0)?"N/A":date_time);

			System.out.printf("<TD CLASS='queue%s'>%s</TD>",(temp_svcstatus.checks_enabled==common_h.TRUE)?"ENABLED":"DISABLED",(temp_svcstatus.checks_enabled==common_h.TRUE)?"ENABLED":"DISABLED");

			System.out.printf("<TD CLASS='queue%s'>",bgclass);
			if(temp_svcstatus.checks_enabled==common_h.TRUE){
				System.out.printf("<a href='%s?cmd_typ=%d&host=%s",cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_SVC_CHECK,cgiutils.url_encode(temp_svcstatus.host_name));
				System.out.printf("&service=%s'><img src='%s%s' border=0 ALT='Disable Active Checks Of This Service' TITLE='Disable Active Checks Of This Service'></a>\n",cgiutils.url_encode(temp_svcstatus.description),cgiutils.url_images_path,cgiutils_h.DISABLED_ICON);
		                }
			else{
				System.out.printf("<a href='%s?cmd_typ=%d&host=%s",cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_SVC_CHECK,cgiutils.url_encode(temp_svcstatus.host_name));
				System.out.printf("&service=%s'><img src='%s%s' border=0 ALT='Enable Active Checks Of This Service' TITLE='Enable Active Checks Of This Service'></a>\n",cgiutils.url_encode(temp_svcstatus.description),cgiutils.url_images_path,cgiutils_h.ENABLED_ICON);
		                }
			System.out.printf("<a href='%s?cmd_typ=%d&host=%s",cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_SVC_CHECK,cgiutils.url_encode(temp_svcstatus.host_name));
			System.out.printf("&service=%s'><img src='%s%s' border=0 ALT='Re-schedule This Service Check' TITLE='Re-schedule This Service Check'></a>\n",cgiutils.url_encode(temp_svcstatus.description),cgiutils.url_images_path,cgiutils_h.DELAY_ICON);
			System.out.printf("</TD>\n");
		        }

		/* get the host status */
		else{
			
			System.out.printf("<TD CLASS='queue%s'><A HREF='%s?type=%d&host=%s'>%s</A></TD>",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_hststatus.host_name),temp_hststatus.host_name);
			
			System.out.printf("<TD CLASS='queue%s'>&nbsp;</TD>",bgclass);

            date_time = cgiutils.get_time_string(temp_hststatus.last_check, common_h.SHORT_DATE_TIME);
			System.out.printf("<TD CLASS='queue%s'>%s</TD>",bgclass,(temp_hststatus.last_check==0)?"N/A":date_time);

            date_time = cgiutils.get_time_string(temp_hststatus.next_check,common_h.SHORT_DATE_TIME);
			System.out.printf("<TD CLASS='queue%s'>%s</TD>",bgclass,(temp_hststatus.next_check==0)?"N/A":date_time);

			System.out.printf("<TD CLASS='queue%s'>%s</TD>",(temp_hststatus.checks_enabled==common_h.TRUE)?"ENABLED":"DISABLED",(temp_hststatus.checks_enabled==common_h.TRUE)?"ENABLED":"DISABLED");

			System.out.printf("<TD CLASS='queue%s'>",bgclass);
			if(temp_hststatus.checks_enabled==common_h.TRUE){
				System.out.printf("<a href='%s?cmd_typ=%d&host=%s",cgiutils_h.COMMAND_CGI,common_h.CMD_DISABLE_HOST_CHECK,cgiutils.url_encode(temp_hststatus.host_name));
				System.out.printf("'><img src='%s%s' border=0 ALT='Disable Active Checks Of This Host' TITLE='Disable Active Checks Of This Host'></a>\n",cgiutils.url_images_path,cgiutils_h.DISABLED_ICON);
		                }
			else{
				System.out.printf("<a href='%s?cmd_typ=%d&host=%s",cgiutils_h.COMMAND_CGI,common_h.CMD_ENABLE_HOST_CHECK,cgiutils.url_encode(temp_hststatus.host_name));
				System.out.printf("'><img src='%s%s' border=0 ALT='Enable Active Checks Of This Host' TITLE='Enable Active Checks Of This Host'></a>\n",cgiutils.url_images_path,cgiutils_h.ENABLED_ICON);
		                }
			System.out.printf("<a href='%s?cmd_typ=%d&host=%s",cgiutils_h.COMMAND_CGI,common_h.CMD_SCHEDULE_HOST_CHECK,cgiutils.url_encode(temp_hststatus.host_name));
			System.out.printf("'><img src='%s%s' border=0 ALT='Re-schedule This Host Check' TITLE='Re-schedule This Host Check'></a>\n",cgiutils.url_images_path,cgiutils_h.DELAY_ICON);
			System.out.printf("</TD>\n");
		        }

		System.out.printf("</TR>\n");

	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");
	System.out.printf("</P>\n");


	/* free memory allocated to sorted data list */
	free_sortdata_list();

	return;
        }



/* sorts host and service data */
public static int sort_data(int s_type, int s_option){

	if(s_type==cgiutils_h.SORT_NONE)
		return common_h.ERROR;

	/* sort all service status entries */
	for( statusdata_h.servicestatus temp_svcstatus : (ArrayList<statusdata_h.servicestatus>)statusdata.servicestatus_list ){

		/* allocate memory for a new sort structure */
        sortdata_list.add( new sortdata( common_h.TRUE, temp_svcstatus, null ) );
	}

	/* sort all host status entries */
	for(statusdata_h.hoststatus temp_hststatus : (ArrayList<statusdata_h.hoststatus>) statusdata.hoststatus_list ){

		/* allocate memory for a new sort structure */
        sortdata_list.add( new sortdata( common_h.FALSE, null, temp_hststatus ) );
	        }

    Collections.sort ( sortdata_list, new compare_sortdata_entries( s_type, s_option ) );
	return common_h.OK;
        }

public static class compare_sortdata_entries implements Comparator<sortdata>{
   int s_type;
   int s_option;

   public compare_sortdata_entries( int type, int option ) {
      s_type = type;
      s_option = option;
   }

   public  int compare(sortdata new_sortdata, sortdata temp_sortdata){
      statusdata_h.hoststatus temp_hststatus;
      statusdata_h.servicestatus temp_svcstatus;
      long[] last_check = new long[2];
      long[] next_check = new long[2];
      int[] current_attempt = new int[2];
      int[] status = new int[2];
      String[] host_name = new String[2];
      String[] service_description = new String[2];

      if(new_sortdata.is_service==common_h.TRUE){
         temp_svcstatus=new_sortdata.svcstatus;
         last_check[0]=temp_svcstatus.last_check;
         next_check[0]=temp_svcstatus.next_check;
         status[0]=temp_svcstatus.status;
         host_name[0]=temp_svcstatus.host_name;
         service_description[0]=temp_svcstatus.description;
         current_attempt[0]=temp_svcstatus.current_attempt;
      }
      else{
         temp_hststatus=new_sortdata.hststatus;
         last_check[0]=temp_hststatus.last_check;
         next_check[0]=temp_hststatus.next_check;
         status[0]=temp_hststatus.status;
         host_name[0]=temp_hststatus.host_name;
         service_description[0]="";
         current_attempt[0]=temp_hststatus.current_attempt;
      }
      if(temp_sortdata.is_service==common_h.TRUE){
         temp_svcstatus=temp_sortdata.svcstatus;
         last_check[1]=temp_svcstatus.last_check;
         next_check[1]=temp_svcstatus.next_check;
         status[1]=temp_svcstatus.status;
         host_name[1]=temp_svcstatus.host_name;
         service_description[1]=temp_svcstatus.description;
         current_attempt[1]=temp_svcstatus.current_attempt;
      }
      else{
         temp_hststatus=temp_sortdata.hststatus;
         last_check[1]=temp_hststatus.last_check;
         next_check[1]=temp_hststatus.next_check;
         status[1]=temp_hststatus.status;
         host_name[1]=temp_hststatus.host_name;
         service_description[1]="";
         current_attempt[1]=temp_hststatus.current_attempt;
      }

      if(s_type==cgiutils_h.SORT_DESCENDING){

         if(s_option==cgiutils_h.SORT_LASTCHECKTIME){
            if(last_check[0] <= last_check[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         if(s_option==cgiutils_h.SORT_NEXTCHECKTIME){
            if(next_check[0] <= next_check[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_CURRENTATTEMPT){
            if(current_attempt[0] <= current_attempt[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_SERVICESTATUS){
            if(status[0] <= status[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_HOSTNAME){
            if(  host_name[0].compareToIgnoreCase(host_name[1])<0)
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_SERVICENAME){
            if( service_description[0].compareToIgnoreCase(service_description[1])<0)
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
      }
      else{
         if(s_option==cgiutils_h.SORT_LASTCHECKTIME){
            if(last_check[0] > last_check[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         if(s_option==cgiutils_h.SORT_NEXTCHECKTIME){
            if(next_check[0] > next_check[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_CURRENTATTEMPT){
            if(current_attempt[0] > current_attempt[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_SERVICESTATUS){
            if(status[0] > status[1])
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_HOSTNAME){
            if( host_name[0].compareToIgnoreCase(host_name[1])>0)
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
         else if(s_option==cgiutils_h.SORT_SERVICENAME){
            if( service_description[0].compareToIgnoreCase(service_description[1])>0)
               return common_h.TRUE;
            else
               return common_h.FALSE;
         }
      }

      return common_h.TRUE;
   }
}


/* free all memory allocated to the sortdata structures */
public static void free_sortdata_list(){
    sortdata_list.clear();
        }

private static int atoi(String value) {
    try {
        return Integer.parseInt(value);
    } catch ( NumberFormatException nfE ) {
//        logger.throwing( cn, "atoi", nfE);
        return 0;
    }
}

}