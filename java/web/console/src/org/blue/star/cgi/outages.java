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

public class outages extends blue_servlet {

    /* HOSTOUTAGE structure */
    public static class hostoutage{
        public objects_h.host hst;
        public int  severity;
        public int  affected_child_hosts;
        public int  affected_child_services;
        public long monitored_time;
        public long time_up;
        public float percent_time_up;
        public long time_down;
        public float percent_time_down;
        public long time_unreachable;
        public float percent_time_unreachable;
    }


///* HOSTOUTAGESORT structure */
//typedef struct hostoutagesort_struct{
//	hostoutage *outage;
//	struct hostoutagesort_struct *next;
//        }hostoutagesort;


public static cgiauth_h.authdata current_authdata;

public static ArrayList<hostoutage> hostoutage_list = new ArrayList<hostoutage> ();
//hostoutagesort *hostoutagesort_list=null;

public static int service_severity_divisor=4;            /* default = services are 1/4 as important as hosts */

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;

public void reset_context() {

   current_authdata = new cgiauth_h.authdata();

   hostoutage_list.clear();
   service_severity_divisor=4;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;
   
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
	comments.read_comment_data(cgiutils.get_cgi_config_location());

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%%>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top width=33%%>\n");
		cgiutils.display_info_table("Network Outages",common_h.TRUE,current_authdata);
		System.out.printf("</td>\n");

		/* middle column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");
		System.out.printf("</td>\n");

		/* right column of top row */
		System.out.printf("<td align=right valign=bottom width=33%%>\n");

		/* display context-sensitive help */
		cgiutils.display_context_help( cgiutils_h.CONTEXTHELP_OUTAGES);

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");

	        }


	/* display network outage info */
	display_network_outages();

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
      	
      	date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      	
      	System.out.printf("Content-type: text/html\r\n\r\n");
    }
    
	if(embedded==common_h.TRUE)
		return;

	System.out.printf("<html>\n");
	System.out.printf("<head>\n");
	System.out.printf("<title>\n");
	System.out.printf("Network Outages\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>",cgiutils.url_stylesheets_path,cgiutils_h.OUTAGES_CSS);
	        }

	System.out.printf("</head>\n");

	System.out.printf("<body CLASS='outages'>\n");

	/* include user SSI header */
	cgiutils.include_ssi_files(cgiutils_h.OUTAGES_CGI,cgiutils_h.SSI_HEADER);

	return;
        }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.OUTAGES_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }


public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars( request_string );

	for(x=0; x < variables.length;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the service severity divisor option */
		if(variables[x].equals("service_divisor")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			service_severity_divisor=atoi(variables[x]);
			if(service_severity_divisor<1)
				service_severity_divisor=1;
		        }

		/* we found the embed option */
		else if( variables[x].equals("embedded"))
			embedded=common_h.TRUE;

		/* we found the noheader option */
		else if( variables[x].equals("noheader"))
			display_header=common_h.FALSE;
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }




/* shows all hosts that are causing network outages */
public static void display_network_outages(){
	String temp_buffer;
	int number_of_problem_hosts=0;
	int number_of_blocking_problem_hosts=0;
//	hostoutagesort *temp_hostoutagesort;
//	hostoutage temp_hostoutage;
	statusdata_h.hoststatus temp_hoststatus;
	int odd=0;
	String bg_class="";
	String status="";
	int days;
	int hours;
	int minutes;
	int seconds;
	int total_comments;
	long t;
	long current_time;
	String state_duration; // 48
	int total_entries=0;

	/* user must be authorized for all hosts.. */
	if(cgiauth.is_authorized_for_all_hosts(current_authdata)==common_h.FALSE){

		System.out.printf("<P><DIV CLASS='errorMessage'>It appears as though you do not have permission to view information you requested...</DIV></P>\n");
		System.out.printf("<P><DIV CLASS='errorDescription'>If you believe this is an error, check the HTTP server authentication requirements for accessing this CGI<br>");
		System.out.printf("and check the authorization options in your CGI configuration file.</DIV></P>\n");

		return;
	        }

	/* find all hosts that are causing network outages */
	find_hosts_causing_outages();

	/* calculate outage effects */
	calculate_outage_effects();

	/* sort the outage list by severity */
	sort_hostoutages();

	/* count the number of top-level hosts that are down and the ones that are actually blocking children hosts */
	for( hostoutage temp_hostoutage : hostoutage_list ){
		number_of_problem_hosts++;
		if(temp_hostoutage.affected_child_hosts>1)
			number_of_blocking_problem_hosts++;
	        }

	/* display the problem hosts... */
	System.out.printf("<P><DIV ALIGN=CENTER>\n");
	System.out.printf("<DIV CLASS='dataTitle'>Blocking Outages</DIV>\n");

	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR>\n");
	System.out.printf("<TH CLASS='data'>Severity</TH><TH CLASS='data'>Host</TH><TH CLASS='data'>State</TH><TH CLASS='data'>Notes</TH><TH CLASS='data'>State Duration</TH><TH CLASS='data'># Hosts Affected</TH><TH CLASS='data'># Services Affected</TH><TH CLASS='data'>Actions</TH>\n");
	System.out.printf("</TR>\n");

    for( hostoutage temp_hostoutage : hostoutage_list ){

		/* skip hosts that are not blocking anyone */
		if(temp_hostoutage.affected_child_hosts<=1)
			continue;

		temp_hoststatus=statusdata.find_hoststatus(temp_hostoutage.hst.name);
		if(temp_hoststatus==null)
			continue;

		/* make	sure we only caught valid state types */
		if(temp_hoststatus.status!=statusdata_h.HOST_DOWN && temp_hoststatus.status!=statusdata_h.HOST_UNREACHABLE)
			continue;

		total_entries++;

		if(odd==0){
			odd=1;
			bg_class="dataOdd";
		        }
		else{
			odd=0;
			bg_class="dataEven";
		        }

		if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
			status="UNREACHABLE";
		else if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
			status="DOWN";

		System.out.printf("<TR CLASS='%s'>\n",bg_class);

		System.out.printf("<TD CLASS='%s'>%d</TD>\n",bg_class,temp_hostoutage.severity);
		System.out.printf("<TD CLASS='%s'><A HREF='%s?type=%d&host=%s'>%s</A></TD>\n",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_hostoutage.hst.name),temp_hostoutage.hst.name);
		System.out.printf("<TD CLASS='host%s'>%s</TD>\n",status,status);

		total_comments=comments.number_of_host_comments(temp_hostoutage.hst.name);
		if(total_comments>0){
			temp_buffer = String.format( "This host has %d comment%s associated with it",total_comments,(total_comments==1)?"":"s");
			System.out.printf("<TD CLASS='%s'><A HREF='%s?type=%d&host=%s#comments'><IMG SRC='%s%s' BORDER=0 ALT='%s' TITLE='%s'></A></TD>\n",bg_class,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.COMMENT_ICON,temp_buffer,temp_buffer);
		        }
		else
			System.out.printf("<TD CLASS='%s'>N/A</TD>\n",bg_class);



		current_time= utils.currentTimeInSeconds();
		if(temp_hoststatus.last_state_change==0)
			t=current_time-blue.program_start;
		else
			t=current_time-temp_hoststatus.last_state_change;
		cgiutils.time_breakdown tb =  cgiutils.get_time_breakdown(t );
		state_duration = String.format( "%2dd %2dh %2dm %2ds%s",tb.days,tb.hours,tb.minutes,tb.seconds,(temp_hoststatus.last_state_change==0)?"+":"");
		System.out.printf("<TD CLASS='%s'>%s</TD>\n",bg_class,state_duration);

		System.out.printf("<TD CLASS='%s'>%d</TD>\n",bg_class,temp_hostoutage.affected_child_hosts);
		System.out.printf("<TD CLASS='%s'>%d</TD>\n",bg_class,temp_hostoutage.affected_child_services);

		System.out.printf("<TD CLASS='%s'>",bg_class);
		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 ALT='View status detail for this host' TITLE='View status detail for this host'></A>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.STATUS_DETAIL_ICON);
//#ifdef USE_STATUSMAP  // TODO Make these options.
		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 ALT='View status map for this host and its children' TITLE='View status map for this host and its children'></A>\n",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.STATUSMAP_ICON);
//#endif
//#ifdef USE_STATUSWRL
		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 ALT='View 3-D status map for this host and its children' TITLE='View 3-D status map for this host and its children'></A>\n",cgiutils_h.STATUSWORLD_CGI,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.STATUSWORLD_ICON);
//#endif
//#ifdef USE_TRENDS
		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 ALT='View trends for this host' TITLE='View trends for this host'></A>\n",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.TRENDS_ICON);
//#endif
		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 ALT='View alert history for this host' TITLE='View alert history for this host'></A>\n",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.HISTORY_ICON);
		System.out.printf("<A HREF='%s?host=%s'><IMG SRC='%s%s' BORDER=0 ALT='View notifications for this host' TITLE='View notifications for this host'></A>\n",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(temp_hostoutage.hst.name),cgiutils.url_images_path,cgiutils_h.NOTIFICATION_ICON);
		System.out.printf("</TD>\n");

		System.out.printf("</TR>\n");
	        }

	System.out.printf("</TABLE>\n");

	System.out.printf("</DIV></P>\n");

	if(total_entries==0)
		System.out.printf("<DIV CLASS='itemTotalsTitle'>%d Blocking Outages Displayed</DIV>\n",total_entries);

	/* free memory allocated to the host outage list */
	free_hostoutage_list();

	return;
        }





/* determine what hosts are causing network outages */
public static void find_hosts_causing_outages(){
    objects_h.host temp_host;

	/* check all hosts */
	for(statusdata_h.hoststatus temp_hoststatus : (ArrayList<statusdata_h.hoststatus>) statusdata.hoststatus_list ){

		/* check only hosts that are not up and not pending */
		if(temp_hoststatus.status!=statusdata_h.HOST_UP && temp_hoststatus.status!=statusdata_h.HOST_PENDING){

			/* find the host entry */
			temp_host= objects.find_host(temp_hoststatus.host_name);

			if(temp_host==null)
				continue;

			/* if the route to this host is not blocked, it is a causing an outage */
			if( is_route_to_host_blocked(temp_host)==common_h.FALSE)
				add_hostoutage(temp_host);
		        }
	        }

	return;
        }





/* adds a host outage entry */
public static void add_hostoutage(objects_h.host hst){

    /* allocate memory for a new structure */
    hostoutage new_hostoutage= new hostoutage();

	new_hostoutage.hst=hst;
	new_hostoutage.severity=0;
	new_hostoutage.affected_child_hosts=0;
	new_hostoutage.affected_child_services=0;

	/* add the structure to the head of the list in memory */
    hostoutage_list.add( new_hostoutage );

        }



/* frees all memory allocated to the host outage list */
public static void free_hostoutage_list(){
    hostoutage_list.clear();
        }



/* frees all memory allocated to the host outage sort list */
//void free_hostoutagesort_list(void){
//	hostoutagesort *this_hostoutagesort;
//	hostoutagesort *next_hostoutagesort;
//
//	/* free all list members */
//	for(this_hostoutagesort=hostoutagesort_list;this_hostoutagesort!=null;this_hostoutagesort=next_hostoutagesort){
//		next_hostoutagesort=this_hostoutagesort.next;
//		free(this_hostoutagesort);
//	        }
//
//	/* reset list pointer */
//	hostoutagesort_list=null;
//
//	return;
//        }


private static class affected {
    public int affected_hosts;
    public int affected_services;
}

/* calculates network outage effect of all hosts that are causing blockages */
public static void calculate_outage_effects(){
    
    /* check all hosts causing problems */
    for(hostoutage temp_hostoutage : hostoutage_list ){
        
        affected temp_affected = new affected();
        /* calculate the outage effect of this particular hosts */
        calculate_outage_effect_of_host(temp_hostoutage.hst, temp_affected );
        temp_hostoutage.affected_child_hosts = temp_affected.affected_hosts;
        temp_hostoutage.affected_child_services = temp_affected.affected_services;
        
        temp_hostoutage.severity=(temp_hostoutage.affected_child_hosts+(temp_hostoutage.affected_child_services/service_severity_divisor));
    }
}

/* calculates network outage effect of a particular host being down or unreachable */
public static void calculate_outage_effect_of_host(objects_h.host hst, affected this_affected){
	int total_child_hosts_affected=0;
	int total_child_services_affected=0;
	affected temp_child_affected = new affected();


	/* find all child hosts of this host */
	for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		/* skip this host if it is not a child */
		if( objects.is_host_immediate_child_of_host(hst,temp_host)==common_h.FALSE)
			continue;

		/* calculate the outage effect of the child */
		calculate_outage_effect_of_host(temp_host, temp_child_affected);

		/* keep a running total of outage effects */
		total_child_hosts_affected+=temp_child_affected.affected_hosts;
		total_child_services_affected+=temp_child_affected.affected_services;
	        }

    this_affected.affected_hosts=total_child_hosts_affected+1;
    this_affected.affected_services=total_child_services_affected+number_of_host_services(hst);

}

/* tests whether or not a host is "blocked" by upstream parents (host is already assumed to be down or unreachable) */
public static int is_route_to_host_blocked(objects_h.host hst){
//	hostsmember *temp_hostsmember;
	statusdata_h.hoststatus temp_hoststatus;

	/* if the host has no parents, it is not being blocked by anyone */
	if(hst.parent_hosts==null || hst.parent_hosts.size() == 0)
		return common_h.FALSE;

	/* check all parent hosts */
	for(objects_h.hostsmember temp_hostsmember : (ArrayList<objects_h.hostsmember>) hst.parent_hosts ){

		/* find the parent host's status */
		temp_hoststatus=statusdata.find_hoststatus(temp_hostsmember.host_name);

		if(temp_hoststatus==null)
			continue;

		/* at least one parent it up (or pending), so this host is not blocked */
		if(temp_hoststatus.status==statusdata_h.HOST_UP || temp_hoststatus.status==statusdata_h.HOST_PENDING)
			return common_h.FALSE;
	        }

	return common_h.TRUE;
        }



/* calculates the number of services associated a particular host */
public static int number_of_host_services(objects_h.host hst){
	int total_services=0;

	/* check all services */
	for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ){

		if( temp_service.host_name.equals( hst.name))
			total_services++;
	        }

	return total_services;
        }



/* sort the host outages by severity */
public static void sort_hostoutages(){

    Comparator<hostoutage> comparator = new Comparator<hostoutage> () {
        public int compare( hostoutage h1, hostoutage h2 ) {
            if ( h1.severity >= h2.severity )
                return -1;
            else 
                return 1;
        }
    };
    
    Collections.sort( hostoutage_list, comparator );

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