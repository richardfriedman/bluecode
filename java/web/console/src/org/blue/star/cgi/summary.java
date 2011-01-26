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
import java.util.Calendar;
import java.util.Comparator;
import java.util.ListIterator;

import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.include.*;

public class summary extends blue_servlet {
    
/* output types */
public static final int HTML_OUTPUT             =0;
public static final int CSV_OUTPUT              =1;

/* custom report types */
public static final int REPORT_NONE                    =0;
public static final int REPORT_RECENT_ALERTS           =1;
public static final int REPORT_ALERT_TOTALS            =2;
public static final int REPORT_TOP_ALERTS              =3;
public static final int REPORT_HOSTGROUP_ALERT_TOTALS  =4; 
public static final int REPORT_HOST_ALERT_TOTALS       =5;
public static final int REPORT_SERVICE_ALERT_TOTALS    =6;
public static final int REPORT_SERVICEGROUP_ALERT_TOTALS =7;

/* standard report types */
public static final int SREPORT_NONE                   =0;
public static final int SREPORT_RECENT_ALERTS          =1;
public static final int SREPORT_RECENT_HOST_ALERTS     =2;
public static final int SREPORT_RECENT_SERVICE_ALERTS  =3;
public static final int SREPORT_TOP_HOST_ALERTS        =4;
public static final int SREPORT_TOP_SERVICE_ALERTS     =5;

/* standard report times */
public static final int TIMEPERIOD_CUSTOM	=0;
public static final int TIMEPERIOD_TODAY	=1;
public static final int TIMEPERIOD_YESTERDAY	=2;
public static final int TIMEPERIOD_THISWEEK	=3;
public static final int TIMEPERIOD_LASTWEEK	=4;
public static final int TIMEPERIOD_THISMONTH	=5;
public static final int TIMEPERIOD_LASTMONTH	=6;
public static final int TIMEPERIOD_THISQUARTER	=7;
public static final int TIMEPERIOD_LASTQUARTER	=8;
public static final int TIMEPERIOD_THISYEAR	=9;
public static final int TIMEPERIOD_LASTYEAR	=10;
public static final int TIMEPERIOD_LAST24HOURS	=11;
public static final int TIMEPERIOD_LAST7DAYS	=12;
public static final int TIMEPERIOD_LAST31DAYS	=13;

public static final int AE_SOFT_STATE           =1;
public static final int AE_HARD_STATE           =2;

public static final int AE_HOST_ALERT           =1;
public static final int AE_SERVICE_ALERT        =2;

public static final int AE_HOST_PRODUCER        =1;
public static final int AE_SERVICE_PRODUCER     =2;

public static final int AE_HOST_DOWN            =1;
public static final int AE_HOST_UNREACHABLE     =2;
public static final int AE_HOST_UP              =4;
public static final int AE_SERVICE_WARNING      =8;
public static final int AE_SERVICE_UNKNOWN      =16;
public static final int AE_SERVICE_CRITICAL     =32;
public static final int AE_SERVICE_OK           =64;

public static class archived_event{
	public long    time_stamp;
    public int     event_type;
    public int     entry_type;
    public String  host_name;
    public String  service_description;
    public int     state;
    public int     state_type;
    public String  event_info;
}

public static class alert_producer {
    public int     producer_type;
    public String  host_name;
    public String  service_description;
    public int     total_alerts;
}

public static ArrayList<archived_event> event_list=new ArrayList<archived_event>();
public static ArrayList<alert_producer> producer_list= new ArrayList<alert_producer>();

public static cgiauth_h.authdata current_authdata;

public static long t1;
public static long t2;

public static int start_second=0;
public static int start_minute=0;
public static int start_hour=0;
public static int start_day=1;
public static int start_month=1;
public static int start_year=2000;
public static int end_second=0;
public static int end_minute=0;
public static int end_hour=24;
public static int end_day=1;
public static int end_month=1;
public static int end_year=2000;

public static int compute_time_from_parts=common_h.FALSE;
public static int timeperiod_type=TIMEPERIOD_CUSTOM;

public static int state_types=AE_HARD_STATE+AE_SOFT_STATE;
public static int alert_types=AE_HOST_ALERT+AE_SERVICE_ALERT;
public static int host_states=AE_HOST_UP+AE_HOST_DOWN+AE_HOST_UNREACHABLE;
public static int service_states=AE_SERVICE_OK+AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL;

public static int show_all_hostgroups=common_h.TRUE;
public static int show_all_servicegroups=common_h.TRUE;
public static int show_all_hosts=common_h.TRUE;

public static String target_hostgroup_name="";
public static String target_servicegroup_name="";
public static String target_host_name="";
public static objects_h.hostgroup target_hostgroup=null;
public static objects_h.servicegroup target_servicegroup=null;
public static objects_h.host target_host=null;

public static int earliest_archive=0;
public static int item_limit=25;
public static int total_items=0;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;

public static int output_format=HTML_OUTPUT;
public static int display_type=REPORT_RECENT_ALERTS;
public static int standard_report=SREPORT_NONE;
public static int generate_report=common_h.FALSE;

public void reset_context() {
   event_list.clear();
   producer_list.clear();

   current_authdata = new cgiauth_h.authdata ();

   t1 = 0;
   t2 = 0;

   start_second=0;
   start_minute=0;
   start_hour=0;
   start_day=1;
   start_month=1;
   start_year=2000;
   end_second=0;
   end_minute=0;
   end_hour=24;
   end_day=1;
   end_month=1;
   end_year=2000;

   compute_time_from_parts=common_h.FALSE;
   timeperiod_type=TIMEPERIOD_CUSTOM;

   state_types=AE_HARD_STATE+AE_SOFT_STATE;
   alert_types=AE_HOST_ALERT+AE_SERVICE_ALERT;
   host_states=AE_HOST_UP+AE_HOST_DOWN+AE_HOST_UNREACHABLE;
   service_states=AE_SERVICE_OK+AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL;

   show_all_hostgroups=common_h.TRUE;
   show_all_servicegroups=common_h.TRUE;
   show_all_hosts=common_h.TRUE;

   target_hostgroup_name="";
   target_servicegroup_name="";
   target_host_name="";
   objects_h.hostgroup target_hostgroup=null;
   objects_h.servicegroup target_servicegroup=null;
   objects_h.host target_host=null;

   earliest_archive=0;
   item_limit=25;
   total_items=0;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;

   output_format=HTML_OUTPUT;
   display_type=REPORT_RECENT_ALERTS;
   standard_report=SREPORT_NONE;
   generate_report=common_h.FALSE;
   
}

public void call_main() {
   main( null );
}

public static void main(String[] args ){
	int result=common_h.OK;
	String temp_buffer ; // MAX_INPUT_BUFFER
	String start_timestring ; // MAX_DATETIME_LENGTH
	String end_timestring ; // MAX_DATETIME_LENGTH
	long t3;
	long current_time;
	Calendar t = Calendar.getInstance();
	int x;

    /* get the arguments passed in the URL */
    process_cgivars();
    
	/* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
		document_footer();
		cgiutils.exit(  common_h.ERROR);
        return;
	        }

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.main_config_file_error(cgiutils.main_config_file);
		document_footer();
		cgiutils.exit(  common_h.ERROR);
        return;
	        }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
        cgiutils.object_data_error();
		document_footer();
		cgiutils.exit(  common_h.ERROR);
        return;
                }

	/* initialize report time period to last 24 hours */
	t2 = utils.currentTimeInSeconds();
	t1=(t2-(60*60*24));

	document_header(common_h.TRUE);

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	if(standard_report!=SREPORT_NONE)
		determine_standard_report_options();

	if(compute_time_from_parts==common_h.TRUE)
		compute_report_times();

	/* make sure times are sane, otherwise swap them */
	if(t2<t1){
		t3=t2;
		t2=t1;
		t1=t3;
	        }
			
	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%% cellspacing=0 cellpadding=0>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top width=33%%>\n");

		temp_buffer = "Alert Summary Report";
		cgiutils.display_info_table(temp_buffer,common_h.FALSE,current_authdata);

		System.out.printf("</td>\n");

		/* center column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");

		if(generate_report==common_h.TRUE){

			System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>\n");
			if(display_type==REPORT_TOP_ALERTS)
				System.out.printf("Top Alert Producers");
			else if(display_type==REPORT_ALERT_TOTALS || display_type==REPORT_HOSTGROUP_ALERT_TOTALS || display_type==REPORT_SERVICEGROUP_ALERT_TOTALS || display_type==REPORT_HOST_ALERT_TOTALS || display_type==REPORT_SERVICE_ALERT_TOTALS)
				System.out.printf("Alert Totals");
			else
				System.out.printf("Most Recent Alerts");

			if(show_all_hostgroups==common_h.FALSE)
				System.out.printf(" For Hostgroup '%s'",target_hostgroup_name);
			else if(show_all_servicegroups==common_h.FALSE)
				System.out.printf(" For Servicegroup '%s'",target_servicegroup_name);
			else if(show_all_hosts==common_h.FALSE)
				System.out.printf(" For Host '%s'",target_host_name);

			System.out.printf("</DIV>\n");

			System.out.printf("<BR>\n");

            start_timestring = cgiutils.get_time_string( t1, common_h.SHORT_DATE_TIME);
            end_timestring = cgiutils.get_time_string( t2, common_h.SHORT_DATE_TIME);
			System.out.printf("<div align=center class='reportRange'>%s to %s</div>\n",start_timestring,end_timestring);

			cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((t2-t1) );
			System.out.printf("<div align=center class='reportDuration'>Duration: %dd %dh %dm %ds</div>\n",tb.days,tb.hours,tb.minutes,tb.seconds);
		        }

		System.out.printf("</td>\n");

		/* right hand column of top row */
		System.out.printf("<td align=right valign=bottom width=33%%>\n");

		if(generate_report==common_h.TRUE){

			System.out.printf("<table border=0>\n");

			System.out.printf("<tr>\n");
			System.out.printf("<td valign=top align=left class='optBoxTitle' colspan=2>Report Options Summary:</td>\n");
			System.out.printf("</tr>\n");

			System.out.printf("<tr>\n");
			System.out.printf("<td valign=top align=left class='optBoxItem'>Alert Types:</td>\n");
			System.out.printf("<td valign=top align=left class='optBoxValue'>\n");
			if((alert_types & AE_HOST_ALERT) != 0)
				System.out.printf("Host");
			if((alert_types & AE_SERVICE_ALERT) != 0)
				System.out.printf("%sService",((alert_types & AE_HOST_ALERT)!=0)?" &amp; ":"");
			System.out.printf(" Alerts</td>\n");
			System.out.printf("</tr>\n");

			System.out.printf("<tr>\n");
			System.out.printf("<td valign=top align=left class='optBoxItem'>State Types:</td>\n");
			System.out.printf("<td valign=top align=left class='optBoxValue'>");
			if((state_types & AE_SOFT_STATE) != 0 )
				System.out.printf("Soft");
			if((state_types & AE_HARD_STATE) != 0 )
				System.out.printf("%sHard",((state_types & AE_SOFT_STATE) != 0 )?" &amp; ":"");
			System.out.printf(" States</td>\n");
			System.out.printf("</tr>\n");

			System.out.printf("<tr>\n");
			System.out.printf("<td valign=top align=left class='optBoxItem'>Host States:</td>\n");
			System.out.printf("<td valign=top align=left class='optBoxValue'>");
			x=0;
			if((host_states & AE_HOST_UP) != 0 ){
				System.out.printf("Up");
				x=1;
			        }
			if((host_states & AE_HOST_DOWN) != 0 ){
				System.out.printf("%sDown",(x==1)?", ":"");
				x=1;
			        }
			if((host_states & AE_HOST_UNREACHABLE) != 0 )
				System.out.printf("%sUnreachable",(x==1)?", ":"");
			if(x==0)
				System.out.printf("None");
			System.out.printf("</td>\n");
			System.out.printf("</tr>\n");

			System.out.printf("<tr>\n");
			System.out.printf("<td valign=top align=left class='optBoxItem'>Service States:</td>\n");
			System.out.printf("<td valign=top align=left class='optBoxValue'>");
			x=0;
			if((service_states & AE_SERVICE_OK) != 0 ){
				System.out.printf("Ok");
				x=1;
			        }
			if((service_states & AE_SERVICE_WARNING) != 0 ){
				System.out.printf("%sWarning",(x==1)?", ":"");
				x=1;
			        }
			if((service_states & AE_SERVICE_UNKNOWN) != 0 ){
				System.out.printf("%sUnknown",(x==1)?", ":"");
				x=1;
			        }
			if((service_states & AE_SERVICE_CRITICAL) != 0 )
				System.out.printf("%sCritical",(x==1)?", ":"");
			if(x==0)
				System.out.printf("None");
			System.out.printf("</td>\n");
			System.out.printf("</tr>\n");

			System.out.printf("<tr>\n");
			System.out.printf("<td valign=top align=left colspan=2 class='optBoxItem'>\n");
			System.out.printf("<form action='%s' method='GET'>\n",cgiutils_h.SUMMARY_CGI);
			System.out.printf("<input type='submit' name='btnSubmit' value='Generate New Report'>\n");
			System.out.printf("</form>\n");
			System.out.printf("</td>\n");
			System.out.printf("</tr>\n");

			/* display context-sensitive help */
			System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
			if(display_type==REPORT_TOP_ALERTS)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_ALERT_PRODUCERS);
			else if(display_type==REPORT_ALERT_TOTALS)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_ALERT_TOTALS);
			else if(display_type==REPORT_HOSTGROUP_ALERT_TOTALS)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_HOSTGROUP_ALERT_TOTALS);
			else if(display_type==REPORT_HOST_ALERT_TOTALS)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_HOST_ALERT_TOTALS);
			else if(display_type==REPORT_SERVICE_ALERT_TOTALS)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_SERVICE_ALERT_TOTALS);
			else if(display_type==REPORT_SERVICEGROUP_ALERT_TOTALS)
                cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_SERVICEGROUP_ALERT_TOTALS);
			else
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_RECENT_ALERTS);
			System.out.printf("</td></tr>\n");

			System.out.printf("</table>\n");
		        }

		else{
			System.out.printf("<table border=0>\n");

			System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_SUMMARY_MENU);
			System.out.printf("</td></tr>\n");

			System.out.printf("</table>\n");
		        }

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");
	        }


	/*********************************/
	/****** GENERATE THE REPORT ******/
	/*********************************/

	if(generate_report==common_h.TRUE){
		read_archived_event_data();
		display_report();
	        }

	/* ask user for report options */
	else{

		current_time = utils.currentTimeInSeconds();
		t.setTimeInMillis(current_time * 1000);

		start_day=1;
		start_year=t.get( Calendar.YEAR );
		end_day=t.get( Calendar.DAY_OF_MONTH );
		end_year=t.get( Calendar.YEAR );

		System.out.printf("<DIV ALIGN=CENTER CLASS='dateSelectTitle'>Standard Reports:</DIV>\n");
		System.out.printf("<DIV ALIGN=CENTER>\n");
	        System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.SUMMARY_CGI);

		System.out.printf("<input type='hidden' name='report' value='1'>\n");

		System.out.printf("<table border=0 cellpadding=5>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Report Type:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<select name='standardreport'>\n");
		System.out.printf("<option value=%d>25 Most Recent Hard Alerts\n",SREPORT_RECENT_ALERTS);
		System.out.printf("<option value=%d>25 Most Recent Hard Host Alerts\n",SREPORT_RECENT_HOST_ALERTS);
		System.out.printf("<option value=%d>25 Most Recent Hard Service Alerts\n",SREPORT_RECENT_SERVICE_ALERTS);
		System.out.printf("<option value=%d>Top 25 Hard Host Alert Producers\n",SREPORT_TOP_HOST_ALERTS);
		System.out.printf("<option value=%d>Top 25 Hard Service Alert Producers\n",SREPORT_TOP_SERVICE_ALERTS);
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Create Summary Report!'></td></tr>\n");

		System.out.printf("</table>\n");

		System.out.printf("</form>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dateSelectTitle'>Custom Report Options:</DIV>\n");
		System.out.printf("<DIV ALIGN=CENTER>\n");
	        System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.SUMMARY_CGI);

		System.out.printf("<input type='hidden' name='report' value='1'>\n");

		System.out.printf("<table border=0 cellpadding=5>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Report Type:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<select name='displaytype'>\n");
		System.out.printf("<option value=%d>Most Recent Alerts\n",REPORT_RECENT_ALERTS);
		System.out.printf("<option value=%d>Alert Totals\n",REPORT_ALERT_TOTALS);
		System.out.printf("<option value=%d>Alert Totals By Hostgroup\n",REPORT_HOSTGROUP_ALERT_TOTALS);
		System.out.printf("<option value=%d>Alert Totals By Host\n",REPORT_HOST_ALERT_TOTALS);
		System.out.printf("<option value=%d>Alert Totals By Servicegroup\n",REPORT_SERVICEGROUP_ALERT_TOTALS);
		System.out.printf("<option value=%d>Alert Totals By Service\n",REPORT_SERVICE_ALERT_TOTALS);
		System.out.printf("<option value=%d>Top Alert Producers\n",REPORT_TOP_ALERTS);
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr>");
		System.out.printf("<td valign=top class='reportSelectSubTitle'>Report Period:</td>\n");
		System.out.printf("<td valign=top align=left class='optBoxItem'>\n");
		System.out.printf("<select name='timeperiod'>\n");
		System.out.printf("<option value=today>Today\n");
		System.out.printf("<option value=last24hours>Last 24 Hours\n");
		System.out.printf("<option value=yesterday>Yesterday\n");
		System.out.printf("<option value=thisweek>This Week\n");
		System.out.printf("<option value=last7days SELECTED>Last 7 Days\n");
		System.out.printf("<option value=lastweek>Last Week\n");
		System.out.printf("<option value=thismonth>This Month\n");
		System.out.printf("<option value=last31days>Last 31 Days\n");
		System.out.printf("<option value=lastmonth>Last Month\n");
		System.out.printf("<option value=thisyear>This Year\n");
		System.out.printf("<option value=lastyear>Last Year\n");
		System.out.printf("<option value=custom>* CUSTOM REPORT PERIOD *\n");
		System.out.printf("</select>\n");
		System.out.printf("</td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("<tr><td valign=top class='reportSelectSubTitle'>If Custom Report Period...</td></tr>\n");

		System.out.printf("<tr>");
		System.out.printf("<td valign=top class='reportSelectSubTitle'>Start Date (Inclusive):</td>\n");
		System.out.printf("<td align=left valign=top class='reportSelectItem'>");
		System.out.printf("<select name='smon'>\n");
		System.out.printf("<option value='1' %s>January\n",(t.get( Calendar.MONTH )==0)?"SELECTED":"");
		System.out.printf("<option value='2' %s>February\n",(t.get( Calendar.MONTH )==1)?"SELECTED":"");
		System.out.printf("<option value='3' %s>March\n",(t.get( Calendar.MONTH )==2)?"SELECTED":"");
		System.out.printf("<option value='4' %s>April\n",(t.get( Calendar.MONTH )==3)?"SELECTED":"");
		System.out.printf("<option value='5' %s>May\n",(t.get( Calendar.MONTH )==4)?"SELECTED":"");
		System.out.printf("<option value='6' %s>June\n",(t.get( Calendar.MONTH )==5)?"SELECTED":"");
		System.out.printf("<option value='7' %s>July\n",(t.get( Calendar.MONTH )==6)?"SELECTED":"");
		System.out.printf("<option value='8' %s>August\n",(t.get( Calendar.MONTH )==7)?"SELECTED":"");
		System.out.printf("<option value='9' %s>September\n",(t.get( Calendar.MONTH )==8)?"SELECTED":"");
		System.out.printf("<option value='10' %s>October\n",(t.get( Calendar.MONTH )==9)?"SELECTED":"");
		System.out.printf("<option value='11' %s>November\n",(t.get( Calendar.MONTH )==10)?"SELECTED":"");
		System.out.printf("<option value='12' %s>December\n",(t.get( Calendar.MONTH )==11)?"SELECTED":"");
		System.out.printf("</select>\n ");
		System.out.printf("<input type='text' size='2' maxlength='2' name='sday' value='%d'> ",start_day);
		System.out.printf("<input type='text' size='4' maxlength='4' name='syear' value='%d'>",start_year);
		System.out.printf("<input type='hidden' name='shour' value='0'>\n");
		System.out.printf("<input type='hidden' name='smin' value='0'>\n");
		System.out.printf("<input type='hidden' name='ssec' value='0'>\n");
		System.out.printf("</td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("<tr>");
		System.out.printf("<td valign=top class='reportSelectSubTitle'>End Date (Inclusive):</td>\n");
		System.out.printf("<td align=left valign=top class='reportSelectItem'>");
		System.out.printf("<select name='emon'>\n");
		System.out.printf("<option value='1' %s>January\n",(t.get( Calendar.MONTH )==0)?"SELECTED":"");
		System.out.printf("<option value='2' %s>February\n",(t.get( Calendar.MONTH )==1)?"SELECTED":"");
		System.out.printf("<option value='3' %s>March\n",(t.get( Calendar.MONTH )==2)?"SELECTED":"");
		System.out.printf("<option value='4' %s>April\n",(t.get( Calendar.MONTH )==3)?"SELECTED":"");
		System.out.printf("<option value='5' %s>May\n",(t.get( Calendar.MONTH )==4)?"SELECTED":"");
		System.out.printf("<option value='6' %s>June\n",(t.get( Calendar.MONTH )==5)?"SELECTED":"");
		System.out.printf("<option value='7' %s>July\n",(t.get( Calendar.MONTH )==6)?"SELECTED":"");
		System.out.printf("<option value='8' %s>August\n",(t.get( Calendar.MONTH )==7)?"SELECTED":"");
		System.out.printf("<option value='9' %s>September\n",(t.get( Calendar.MONTH )==8)?"SELECTED":"");
		System.out.printf("<option value='10' %s>October\n",(t.get( Calendar.MONTH )==9)?"SELECTED":"");
		System.out.printf("<option value='11' %s>November\n",(t.get( Calendar.MONTH )==10)?"SELECTED":"");
		System.out.printf("<option value='12' %s>December\n",(t.get( Calendar.MONTH )==11)?"SELECTED":"");
		System.out.printf("</select>\n ");
		System.out.printf("<input type='text' size='2' maxlength='2' name='eday' value='%d'> ",end_day);
		System.out.printf("<input type='text' size='4' maxlength='4' name='eyear' value='%d'>",end_year);
		System.out.printf("<input type='hidden' name='ehour' value='24'>\n");
		System.out.printf("<input type='hidden' name='emin' value='0'>\n");
		System.out.printf("<input type='hidden' name='esec' value='0'>\n");
		System.out.printf("</td>\n");
		System.out.printf("</tr>\n");

		System.out.printf("<tr><td colspan=2><br></td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Limit To Hostgroup:</td><td align=left valign=center class='reportSelectItem'>\n");
		System.out.printf("<select name='hostgroup'>\n");
		System.out.printf("<option value='all'>** ALL HOSTGROUPS **\n");
		for(objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list ){
			if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.TRUE)
				System.out.printf("<option value='%s'>%s\n",temp_hostgroup.group_name,temp_hostgroup.group_name);
		        }
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Limit To Servicegroup:</td><td align=left valign=center class='reportSelectItem'>\n");
		System.out.printf("<select name='servicegroup'>\n");
		System.out.printf("<option value='all'>** ALL SERVICEGROUPS **\n");
		for(objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>) objects.servicegroup_list ){
			if(cgiauth.is_authorized_for_servicegroup(temp_servicegroup,current_authdata)==common_h.TRUE)
				System.out.printf("<option value='%s'>%s\n",temp_servicegroup.group_name,temp_servicegroup.group_name);
		        }
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Limit To Host:</td><td align=left valign=center class='reportSelectItem'>\n");
		System.out.printf("<select name='host'>\n");
		System.out.printf("<option value='all'>** ALL HOSTS **\n");

		for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){
			if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.TRUE)
				System.out.printf("<option value='%s'>%s\n",temp_host.name,temp_host.name);
		        }
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Alert Types:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<select name='alerttypes'>\n");
		System.out.printf("<option value=%d %s>Host and Service Alerts\n",AE_HOST_ALERT+AE_SERVICE_ALERT,(alert_types==AE_HOST_ALERT+AE_SERVICE_ALERT)?"SELECTED":"");
		System.out.printf("<option value=%d %s>Host Alerts\n",AE_HOST_ALERT,(alert_types==AE_HOST_ALERT)?"SELECTED":"");
		System.out.printf("<option value=%d %s>Service Alerts\n",AE_SERVICE_ALERT,(alert_types==AE_SERVICE_ALERT)?"SELECTED":"");
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>State Types:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<select name='statetypes'>\n");
		System.out.printf("<option value=%d %s>Hard and Soft States\n",AE_HARD_STATE+AE_SOFT_STATE,(state_types==AE_HARD_STATE+AE_SOFT_STATE)?"SELECTED":"");
		System.out.printf("<option value=%d %s>Hard States\n",AE_HARD_STATE,(state_types==AE_HARD_STATE)?"SELECTED":"");
		System.out.printf("<option value=%d %s>Soft States\n",AE_SOFT_STATE,(state_types==AE_SOFT_STATE)?"SELECTED":"");
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Host States:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<select name='hoststates'>\n");
		System.out.printf("<option value=%d>All Host States\n",AE_HOST_UP+AE_HOST_DOWN+AE_HOST_UNREACHABLE);
		System.out.printf("<option value=%d>Host Problem States\n",AE_HOST_DOWN+AE_HOST_UNREACHABLE);
		System.out.printf("<option value=%d>Host Up States\n",AE_HOST_UP);
		System.out.printf("<option value=%d>Host Down States\n",AE_HOST_DOWN);
		System.out.printf("<option value=%d>Host Unreachable States\n",AE_HOST_UNREACHABLE);
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Service States:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<select name='servicestates'>\n");
		System.out.printf("<option value=%d>All Service States\n",AE_SERVICE_OK+AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL);
		System.out.printf("<option value=%d>Service Problem States\n",AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL);
		System.out.printf("<option value=%d>Service Ok States\n",AE_SERVICE_OK);
		System.out.printf("<option value=%d>Service Warning States\n",AE_SERVICE_WARNING);
		System.out.printf("<option value=%d>Service Unknown States\n",AE_SERVICE_UNKNOWN);
		System.out.printf("<option value=%d>Service Critical States\n",AE_SERVICE_CRITICAL);
		System.out.printf("</select>\n");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Max List Items:</td>\n");
		System.out.printf("<td class='reportSelectItem'>\n");
		System.out.printf("<input type='text' name='limit' size='3' maxlength='3' value='%d'>\n",item_limit);
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Create Summary Report!'></td></tr>\n");

		System.out.printf("</table>\n");

		System.out.printf("</form>\n");
		System.out.printf("</DIV>\n");
	        }


	document_footer();

	cgiutils.exit(  common_h.OK );
        }



public static void document_header(int use_stylesheet){
	String date_time ; // MAX_DATETIME_LENGTH

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );
       if ( output_format == HTML_OUTPUT ) 
         response.setContentType("text/html");
       else {
          response.setContentType("text/plain");
       }
    } else {
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      	
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      	
      	if(output_format==HTML_OUTPUT)
      	   System.out.printf("Content-type: text/html\r\n\r\n");
      	else{
      	   System.out.printf("Content-type: text/plain\r\n\r\n");
      	   return;
      	}
    }
    
    if(embedded==common_h.TRUE || output_format==CSV_OUTPUT)
       return;
    
    System.out.printf("<html>\n");
    System.out.printf("<head>\n");
    System.out.printf("<title>\n");
    System.out.printf("Blue Event Summary\n");
    System.out.printf("</title>\n");
    
    if(use_stylesheet==common_h.TRUE){
       System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
       System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.SUMMARY_CSS);
    }
    
    System.out.printf("</head>\n");
    
    System.out.printf("<BODY CLASS='summary'>\n");
    
    /* include user SSI header */
    cgiutils.include_ssi_files(cgiutils_h.SUMMARY_CGI,cgiutils_h.SSI_HEADER);
    
    return;
}

public static void document_footer(){

	if(output_format!=HTML_OUTPUT)
		return;

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.SUMMARY_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }



public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars(request_string);

	for(x=0; x <variables.length;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found first time argument */
		else if(variables[x].equals("t1")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			t1=strtoul(variables[x],null,10);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.FALSE;
		        }

		/* we found first time argument */
		else if(variables[x].equals("t2")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			t2=strtoul(variables[x],null,10);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.FALSE;
		        }

		/* we found the standard timeperiod argument */
		else if(variables[x].equals("timeperiod")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("today"))
				timeperiod_type=TIMEPERIOD_TODAY;
			else if(variables[x].equals("yesterday"))
				timeperiod_type=TIMEPERIOD_YESTERDAY;
			else if(variables[x].equals("thisweek"))
				timeperiod_type=TIMEPERIOD_THISWEEK;
			else if(variables[x].equals("lastweek"))
				timeperiod_type=TIMEPERIOD_LASTWEEK;
			else if(variables[x].equals("thismonth"))
				timeperiod_type=TIMEPERIOD_THISMONTH;
			else if(variables[x].equals("lastmonth"))
				timeperiod_type=TIMEPERIOD_LASTMONTH;
			else if(variables[x].equals("thisquarter"))
				timeperiod_type=TIMEPERIOD_THISQUARTER;
			else if(variables[x].equals("lastquarter"))
				timeperiod_type=TIMEPERIOD_LASTQUARTER;
			else if(variables[x].equals("thisyear"))
				timeperiod_type=TIMEPERIOD_THISYEAR;
			else if(variables[x].equals("lastyear"))
				timeperiod_type=TIMEPERIOD_LASTYEAR;
			else if(variables[x].equals("last24hours"))
				timeperiod_type=TIMEPERIOD_LAST24HOURS;
			else if(variables[x].equals("last7days"))
				timeperiod_type=TIMEPERIOD_LAST7DAYS;
			else if(variables[x].equals("last31days"))
				timeperiod_type=TIMEPERIOD_LAST31DAYS;
			else if(variables[x].equals("custom"))
				timeperiod_type=TIMEPERIOD_CUSTOM;
			else
				continue;

			convert_timeperiod_to_times(timeperiod_type);
			compute_time_from_parts=common_h.FALSE;
		        }

		/* we found the embed option */
		else if(variables[x].equals("embedded"))
			embedded=common_h.TRUE;

		/* we found the noheader option */
		else if(variables[x].equals("noheader"))
			display_header=common_h.FALSE;

		/* we found time argument */
		else if(variables[x].equals("smon")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			start_month=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("sday")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			start_day=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("syear")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			start_year=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("smin")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			start_minute=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("ssec")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			start_second=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("shour")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			start_hour=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }


		/* we found time argument */
		else if(variables[x].equals("emon")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			end_month=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("eday")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			end_day=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("eyear")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			end_year=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("emin")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			end_minute=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("esec")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			end_second=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found time argument */
		else if(variables[x].equals("ehour")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				continue;

			end_hour=atoi(variables[x]);
			timeperiod_type=TIMEPERIOD_CUSTOM;
			compute_time_from_parts=common_h.TRUE;
		        }

		/* we found the item limit argument */
		else if(variables[x].equals("limit")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			item_limit=atoi(variables[x]);
		        }

		/* we found the state types argument */
		else if(variables[x].equals("statetypes")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			state_types=atoi(variables[x]);
		        }

		/* we found the alert types argument */
		else if(variables[x].equals("alerttypes")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			alert_types=atoi(variables[x]);
		        }

		/* we found the host states argument */
		else if(variables[x].equals("hoststates")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_states=atoi(variables[x]);
		        }

		/* we found the service states argument */
		else if(variables[x].equals("servicestates")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			service_states=atoi(variables[x]);
		        }

		/* we found the generate report argument */
		else if(variables[x].equals("report")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			generate_report=(atoi(variables[x])>0)?common_h.TRUE:common_h.FALSE;
		        }


		/* we found the display type argument */
		else if(variables[x].equals("displaytype")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			display_type=atoi(variables[x]);
		        }

		/* we found the standard report argument */
		else if(variables[x].equals("standardreport")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			standard_report=atoi(variables[x]);
		        }

		/* we found the hostgroup argument */
		else if(variables[x].equals("hostgroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				target_hostgroup_name = variables[x];

			if( target_hostgroup_name.equals("all"))
				show_all_hostgroups=common_h.TRUE;
			else{
				show_all_hostgroups=common_h.FALSE;
				target_hostgroup=objects.find_hostgroup(target_hostgroup_name);
			        }
		        }

		/* we found the servicegroup argument */
		else if(variables[x].equals("servicegroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				target_servicegroup_name = variables[x];

			if( target_servicegroup_name.equals("all"))
				show_all_servicegroups=common_h.TRUE;
			else{
				show_all_servicegroups=common_h.FALSE;
				target_servicegroup=objects.find_servicegroup(target_servicegroup_name);
			        }
		        }

		/* we found the host argument */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				target_host_name = variables[x];

			if( target_host_name.equals( "all"))
				show_all_hosts=common_h.TRUE;
			else{
				show_all_hosts=common_h.FALSE;
				target_host=objects.find_host(target_host_name);
			        }
		        }
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }


	
/* reads log files for archived event data */
public static void read_archived_event_data(){
	String filename ; // MAX_FILENAME_LENGTH
	int oldest_archive=0;
	int newest_archive=0;
	int current_archive=0;

	/* determine oldest archive to use when scanning for data */
	oldest_archive=cgiutils.determine_archive_to_use_from_time(t1);

	/* determine most recent archive to use when scanning for data */
	newest_archive=cgiutils.determine_archive_to_use_from_time(t2);

	if(oldest_archive<newest_archive)
		oldest_archive=newest_archive;

	/* read in all the necessary archived logs (from most recent to earliest) */
	for(current_archive=newest_archive;current_archive<=oldest_archive;current_archive++){

		/* get the name of the log file that contains this archive */
        filename = cgiutils.get_log_archive_to_use(current_archive);

		/* scan the log file for archived state data */
		scan_log_file_for_archived_event_data(filename);
	        }

	return;
        }



/* grabs archived event data from a log file */
public static void scan_log_file_for_archived_event_data(String filename){
	String input=null;
	String input2=null;
	String entry_host_name ; // MAX_INPUT_BUFFER
	String entry_svc_description ; // MAX_INPUT_BUFFER
	int state;
	int state_type;
	String temp_buffer;
	String plugin_output;
	long time_stamp;
	cgiutils_h.mmapfile thefile;


	if((thefile=cgiutils.mmap_fopen(filename))==null)
		return;

	while(true){

		/* read the next line */
		if((input=cgiutils.mmap_fgets(thefile))==null)
			break;

		input = input.trim();

		/* get the timestamp */
        String[] split = input.split( "[\\]]" , 2 );
        time_stamp=( split[0].trim().length() <= 1 )?0: strtoul( split[0].substring(1),null,10);

		if(time_stamp<t1 || time_stamp>t2)
			continue;
		
		/* host alerts */
		if(input.contains("HOST ALERT:")){

			/* get host name */
            temp_buffer = split ( split, "[:]" ); 
            temp_buffer = split ( split, "[;]" );
            entry_host_name = (temp_buffer==null)?"": temp_buffer.substring(1) ;

			/* state type */
			if(input.contains(";SOFT;"))
				state_type=AE_SOFT_STATE;
			else
				state_type=AE_HARD_STATE;
				
			/* get the plugin output */
            temp_buffer = split ( split, "[:]" ); 
            temp_buffer = split ( split, "[;]" );
            temp_buffer = split ( split, "[;]" );
            plugin_output = split ( split, "[\\n]" );

			/* state */
			if(input.contains(";DOWN;"))
				state=AE_HOST_DOWN;
			else if(input.contains(";UNREACHABLE;"))
				state=AE_HOST_UNREACHABLE;
			else if(input.contains(";RECOVERY") || input.contains(";UP;"))
				state=AE_HOST_UP;
			else
				continue;

			add_archived_event(AE_HOST_ALERT,time_stamp,state,state_type,entry_host_name,null,plugin_output);
		        }

		/* service alerts */
		if(input.contains("SERVICE ALERT:")){

			/* get host name */
            temp_buffer = split ( split, "[:]" ); 
            temp_buffer = split ( split, "[;]" );
            entry_host_name = (temp_buffer==null)?"": temp_buffer.substring(1) ;

			/* get service description */
            temp_buffer = split ( split, "[;]" );
            entry_svc_description = (temp_buffer==null)?"":temp_buffer;

			/* state type */
			if(input.contains(";SOFT;"))
				state_type=AE_SOFT_STATE;
			else
				state_type=AE_HARD_STATE;

			/* get the plugin output */
            temp_buffer = split ( split, "[:]" ); 
            temp_buffer = split ( split, "[;]" );
            temp_buffer = split ( split, "[;]" );
            plugin_output = split ( split, "[\\n]" );

			/* state */
			if(input.contains(";WARNING;"))
				state=AE_SERVICE_WARNING;
			else if(input.contains(";UNKNOWN;"))
				state=AE_SERVICE_UNKNOWN;
			else if(input.contains(";CRITICAL;"))
				state=AE_SERVICE_CRITICAL;
			else if(input.contains(";RECOVERY") || input.contains(";OK;"))
				state=AE_SERVICE_OK;
			else
				continue;

			add_archived_event(AE_SERVICE_ALERT,time_stamp,state,state_type,entry_host_name,entry_svc_description,plugin_output);
		        }
	        }

	cgiutils.mmap_fclose(thefile);
	
	return;
        }
	



public static void convert_timeperiod_to_times(int type){
	long current_time;
	Calendar t = Calendar.getInstance();

	/* get the current time */
	current_time = utils.currentTimeInSeconds(); 

	t.setTimeInMillis(current_time * 1000);

	t.set( Calendar.SECOND , 0 );
	t.set( Calendar.MINUTE, 0 );
	t.set( Calendar.HOUR , 0 );
    
	switch(type){
	case TIMEPERIOD_LAST24HOURS:
		t1=current_time-(60*60*24);
		t2=current_time;
		break;
	case TIMEPERIOD_TODAY:
		t1= utils.getTimeInSeconds(t); 
		t2=current_time;
		break;
	case TIMEPERIOD_YESTERDAY:
		t1=(utils.getTimeInSeconds(t)-(60*60*24));
		t2=utils.getTimeInSeconds(t);
		break;
	case TIMEPERIOD_THISWEEK:
		t1=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK)-1)));
		t2=current_time;
		break;
	case TIMEPERIOD_LASTWEEK:
		t1=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK)-1))-(60*60*24*7));
		t2=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK)-1)));
		break;
	case TIMEPERIOD_THISMONTH:
		t.set( Calendar.DAY_OF_MONTH, 1);
		t1=utils.getTimeInSeconds(t);
		t2=current_time;
		break;
	case TIMEPERIOD_LASTMONTH:
        t.set( Calendar.DAY_OF_MONTH, 1);
		t2=utils.getTimeInSeconds(t);
		if(t.get( Calendar.MONTH )==0){
			t.set( Calendar.MONTH, 11 );
            t.roll( Calendar.YEAR, false );
		        }
		else
            t.roll( Calendar.MONTH, false );
		t1=utils.getTimeInSeconds(t);
		break;
	case TIMEPERIOD_THISQUARTER:
		/* not implemented */
		break;
	case TIMEPERIOD_LASTQUARTER:
		/* not implemented */
		break;
	case TIMEPERIOD_THISYEAR:
        t.set( Calendar.MONTH, 0 );
        t.set( Calendar.DAY_OF_MONTH, 1);
		t1=utils.getTimeInSeconds(t);
		t2=current_time;
		break;
	case TIMEPERIOD_LASTYEAR:
        t.set( Calendar.MONTH, 0 );
        t.set( Calendar.DAY_OF_MONTH, 1);
		t2=utils.getTimeInSeconds(t);
        t.roll( Calendar.YEAR, false );
		t1=utils.getTimeInSeconds(t);
		break;
	case TIMEPERIOD_LAST7DAYS:
		t2=current_time;
		t1=current_time-(7*24*60*60);
		break;
	case TIMEPERIOD_LAST31DAYS:
		t2=current_time;
		t1=current_time-(31*24*60*60);
		break;
	default:
		break;
	        }

	return;
        }



public static void compute_report_times(){
	long current_time;
	Calendar st = Calendar.getInstance();
	Calendar et = Calendar.getInstance();

	/* get the current time */
	current_time = utils.currentTimeInSeconds();

	st.setTimeInMillis(current_time * 1000);

	st.set( Calendar.SECOND , start_second );
    st.set( Calendar.MINUTE, start_minute );
    st.set( Calendar.HOUR, start_hour );
    st.set( Calendar.DAY_OF_MONTH, start_day );
    st.set( Calendar.MONTH, start_month-1 );
    st.set( Calendar.YEAR, start_year );

	t1= utils.getTimeInSeconds(st);

    et.setTimeInMillis(current_time * 1000);

    et.set( Calendar.SECOND , end_second );
    et.set( Calendar.MINUTE, end_minute );
    et.set( Calendar.HOUR, end_hour );
    et.set( Calendar.DAY_OF_MONTH, end_day );
    et.set( Calendar.MONTH, end_month-1 );
    et.set( Calendar.YEAR, end_year );

    t2= utils.getTimeInSeconds(et);
}

/* adds an archived event entry to the list in memory */
public static void add_archived_event(int event_type, long time_stamp, int entry_type, int state_type, String host_name, String svc_description, String event_info){
	archived_event last_event=null;
	archived_event temp_event=null;
	archived_event new_event=null;
    objects_h.service temp_service=null;
	objects_h.host temp_host;


	/* check timestamp sanity */
	if(time_stamp<t1 || time_stamp>t2)
		return;

	/* check alert type (host or service alert) */
	if(0==(alert_types & event_type))
		return;

	/* check state type (soft or hard state) */
	if(0==(state_types & state_type))
		return;

	/* check state (host or service state) */
	if(event_type==AE_HOST_ALERT){
		if(0==(host_states & entry_type))
			return;
	        }
	else{
		if(0==(service_states & entry_type))
			return;
	        }
		
	/* find the host this entry is associated with */
	temp_host=objects.find_host(host_name);

	/* check hostgroup match (valid filter for all reports) */
	if(show_all_hostgroups==common_h.FALSE && objects.is_host_member_of_hostgroup(target_hostgroup,temp_host)==common_h.FALSE)
		return;

	/* check host match (valid filter for some reports) */
	if(show_all_hosts==common_h.FALSE && (display_type==REPORT_RECENT_ALERTS || display_type==REPORT_HOST_ALERT_TOTALS || display_type==REPORT_SERVICE_ALERT_TOTALS)){
		if(target_host==null || temp_host==null)
			return;
		if( !target_host.name.equals(temp_host.name))
			return;
	        }

	/* check servicegroup math (valid filter for all reports) */
	if(event_type==AE_SERVICE_ALERT){
		temp_service=objects.find_service(host_name,svc_description);
		if(show_all_servicegroups==common_h.FALSE && objects.is_service_member_of_servicegroup(target_servicegroup,temp_service)==common_h.FALSE)
			return;
	         }
	else{
		if(show_all_servicegroups==common_h.FALSE && objects.is_host_member_of_servicegroup(target_servicegroup,temp_host)==common_h.FALSE)
			return;
	         }

	/* check authorization */
	if(event_type==AE_SERVICE_ALERT){
		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			return;
	        }
	else{
		if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			return;
	        }

//#ifdef DEBUG
//	if(event_type==AE_HOST_ALERT)
//		System.out.printf("Adding host alert (%s) @ %d<BR>\n",host_name,(unsigned long)time_stamp);
//	else
//		System.out.printf("Adding service alert (%s/%s) @ %d<BR>\n",host_name,svc_description,(unsigned long)time_stamp);
//#endif

	/* allocate memory for the new entry */
	new_event = new archived_event();

	/* allocate memory for the host name */
			new_event.host_name = host_name;

	/* allocate memory for the service description */
			new_event.service_description = svc_description;

	/* allocate memory for the event info */
			new_event.event_info = event_info;

	new_event.event_type=event_type;
	new_event.time_stamp=time_stamp;
	new_event.entry_type=entry_type;
	new_event.state_type=state_type;


	/* add the new entry to the list in memory, sorted by time */
    boolean added = false;
	for(ListIterator<archived_event> iter = event_list.listIterator(); iter.hasNext(); ){
        temp_event = iter.next();
        
		if(new_event.time_stamp>=temp_event.time_stamp){
            iter.add( new_event );
            added = true;
			break;
		        }
	        }
	if( !added )
        event_list.add( new_event );

	total_items++;

	return;
        }



/* determines standard report options */
public static void determine_standard_report_options(){

	/* report over last 7 days */
	convert_timeperiod_to_times(TIMEPERIOD_LAST7DAYS);
	compute_time_from_parts=common_h.FALSE;

	/* common options */
	state_types=AE_HARD_STATE;
	item_limit=25;

	/* report-specific options */
	switch(standard_report){

	case SREPORT_RECENT_ALERTS:
		display_type=REPORT_RECENT_ALERTS;
		alert_types=AE_HOST_ALERT+AE_SERVICE_ALERT;
		host_states=AE_HOST_UP+AE_HOST_DOWN+AE_HOST_UNREACHABLE;
		service_states=AE_SERVICE_OK+AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL;
		break;

	case SREPORT_RECENT_HOST_ALERTS:
		display_type=REPORT_RECENT_ALERTS;
		alert_types=AE_HOST_ALERT;
		host_states=AE_HOST_UP+AE_HOST_DOWN+AE_HOST_UNREACHABLE;
		break;

	case SREPORT_RECENT_SERVICE_ALERTS:
		display_type=REPORT_RECENT_ALERTS;
		alert_types=AE_SERVICE_ALERT;
		service_states=AE_SERVICE_OK+AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL;
		break;

	case SREPORT_TOP_HOST_ALERTS:
		display_type=REPORT_TOP_ALERTS;
		alert_types=AE_HOST_ALERT;
		host_states=AE_HOST_UP+AE_HOST_DOWN+AE_HOST_UNREACHABLE;
		break;

	case SREPORT_TOP_SERVICE_ALERTS:
		display_type=REPORT_TOP_ALERTS;
		alert_types=AE_SERVICE_ALERT;
		service_states=AE_SERVICE_OK+AE_SERVICE_WARNING+AE_SERVICE_UNKNOWN+AE_SERVICE_CRITICAL;
		break;

	default:
		break;
	        }

	return;
        }



/* displays report */
public static void display_report(){

	switch(display_type){

	case REPORT_ALERT_TOTALS:
		display_alert_totals();
		break;

	case REPORT_HOSTGROUP_ALERT_TOTALS:
		display_hostgroup_alert_totals();
		break;

	case REPORT_HOST_ALERT_TOTALS:
		display_host_alert_totals();
		break;

	case REPORT_SERVICEGROUP_ALERT_TOTALS:
		display_servicegroup_alert_totals();
		break;

	case REPORT_SERVICE_ALERT_TOTALS:
		display_service_alert_totals();
		break;

	case REPORT_TOP_ALERTS:
		display_top_alerts();
		break;

	default:
		display_recent_alerts();
		break;
	        }

	return;
        }


/* displays recent alerts */
public static void display_recent_alerts(){

	int current_item=-1;
	int odd=0;
	String bgclass="";
	String status_bgclass="";
	String status="";
	String date_time ; // MAX_DATETIME_LENGTH



	System.out.printf("<BR>\n");

	if(item_limit<=0 || total_items<=item_limit || total_items==0)
		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Displaying all %d matching alerts\n",total_items);
	else
		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Displaying most recent %d of %d total matching alerts\n",item_limit,total_items);

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR><TH CLASS='data'>Time</TH><TH CLASS='data'>Alert Type</TH><TH CLASS='data'>Host</TH><TH CLASS='data'>Service</TH><TH CLASS='data'>State</TH><TH CLASS='data'>State Type</TH><TH CLASS='data'>Information</TH></TR>\n");


	for(archived_event temp_event : event_list){
        current_item++;
        
		if(current_item>=item_limit && item_limit>0)
			break;

		if(odd != 0){
			odd=0;
			bgclass="Odd";
	                }
		else{
			odd=1;
			bgclass="Even";
	                }

		System.out.printf("<tr CLASS='data%s'>",bgclass);

        date_time = cgiutils.get_time_string(temp_event.time_stamp, common_h.SHORT_DATE_TIME);
		System.out.printf("<td CLASS='data%s'>%s</td>",bgclass,date_time);

		System.out.printf("<td CLASS='data%s'>%s</td>",bgclass,(temp_event.event_type==AE_HOST_ALERT)?"Host Alert":"Service Alert");

		System.out.printf("<td CLASS='data%s'><a href='%s?type=%d&host=%s'>%s</a></td>",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_event.host_name),temp_event.host_name);

		if(temp_event.event_type==AE_HOST_ALERT)
			System.out.printf("<td CLASS='data%s'>N/A</td>",bgclass);
		else{
			System.out.printf("<td CLASS='data%s'><a href='%s?type=%d&host=%s",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_event.host_name));
			System.out.printf("&service=%s'>%s</a></td>",cgiutils.url_encode(temp_event.service_description),temp_event.service_description);
		        }

		switch(temp_event.entry_type){
		case AE_HOST_UP:
			status_bgclass="hostUP";
			status="UP";
			break;
		case AE_HOST_DOWN:
			status_bgclass="hostDOWN";
			status="DOWN";
			break;
		case AE_HOST_UNREACHABLE:
			status_bgclass="hostUNREACHABLE";
			status="UNREACHABLE";
			break;
		case AE_SERVICE_OK:
			status_bgclass="serviceOK";
			status="OK";
			break;
		case AE_SERVICE_WARNING:
			status_bgclass="serviceWARNING";
			status="WARNING";
			break;
		case AE_SERVICE_UNKNOWN:
			status_bgclass="serviceUNKNOWN";
			status="UNKNOWN";
			break;
		case AE_SERVICE_CRITICAL:
			status_bgclass="serviceCRITICAL";
			status="CRITICAL";
			break;
		default:
			status_bgclass=bgclass;
			status="???";
			break;
		        }

		System.out.printf("<td CLASS='%s'>%s</td>",status_bgclass,status);

		System.out.printf("<td CLASS='data%s'>%s</td>",bgclass,(temp_event.state_type==AE_SOFT_STATE)?"SOFT":"HARD");

		System.out.printf("<td CLASS='data%s'>%s</td>",bgclass,temp_event.event_info);

		System.out.printf("</tr>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

	return;
        }



/* displays alerts totals */
public static void display_alert_totals(){
	int hard_host_up_alerts=0;
	int soft_host_up_alerts=0;
	int hard_host_down_alerts=0;
	int soft_host_down_alerts=0;
	int hard_host_unreachable_alerts=0;
	int soft_host_unreachable_alerts=0;
	int hard_service_ok_alerts=0;
	int soft_service_ok_alerts=0;
	int hard_service_warning_alerts=0;
	int soft_service_warning_alerts=0;
	int hard_service_unknown_alerts=0;
	int soft_service_unknown_alerts=0;
	int hard_service_critical_alerts=0;
	int soft_service_critical_alerts=0;


	/************************/
	/**** OVERALL TOTALS ****/
	/************************/

	/* process all events */
	for(archived_event temp_event : event_list ){

		/* host alerts */
		if(temp_event.event_type==AE_HOST_ALERT){
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_HOST_UP)
					soft_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					soft_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					soft_host_unreachable_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_HOST_UP)
					hard_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					hard_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					hard_host_unreachable_alerts++;
			        }
		        }

		/* service alerts */
		else{
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_SERVICE_OK)
					soft_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					soft_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					soft_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					soft_service_critical_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_SERVICE_OK)
					hard_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					hard_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					hard_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					hard_service_critical_alerts++;
			        }
		        }
	        }

	System.out.printf("<BR>\n");

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Overall Totals</DIV>\n");
	System.out.printf("<BR>\n");
	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='reportDataOdd'><TR><TD>\n");
	System.out.printf("<TABLE BORDER=0>\n");
	System.out.printf("<TR>\n");

	if(( alert_types & AE_HOST_ALERT) != 0 ){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Host Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUP'>UP</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_up_alerts,hard_host_up_alerts,soft_host_up_alerts+hard_host_up_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='hostDOWN'>DOWN</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_host_down_alerts,hard_host_down_alerts,soft_host_down_alerts+hard_host_down_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUNREACHABLE'>UNREACHABLE</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_unreachable_alerts,hard_host_unreachable_alerts,soft_host_unreachable_alerts+hard_host_unreachable_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='dataEven'>All States</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'><B>%d</B></TD></TR>\n",soft_host_up_alerts+soft_host_down_alerts+soft_host_unreachable_alerts,hard_host_up_alerts+hard_host_down_alerts+hard_host_unreachable_alerts,soft_host_up_alerts+hard_host_up_alerts+soft_host_down_alerts+hard_host_down_alerts+soft_host_unreachable_alerts+hard_host_unreachable_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	if ((alert_types & AE_SERVICE_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Service Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceOK'>OK</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_ok_alerts,hard_service_ok_alerts,soft_service_ok_alerts+hard_service_ok_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceWARNING'>WARNING</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_warning_alerts,hard_service_warning_alerts,soft_service_warning_alerts+hard_service_warning_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceUNKNOWN'>UNKNOWN</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_unknown_alerts,hard_service_unknown_alerts,soft_service_unknown_alerts+hard_service_unknown_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceCRITICAL'>CRITICAL</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_critical_alerts,hard_service_critical_alerts,soft_service_critical_alerts+hard_service_critical_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='dataOdd'>All States</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'><B>%d</B></TD></TR>\n",soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts,hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts,soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts+hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	System.out.printf("</TR>\n");
	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR></TABLE>\n");
	System.out.printf("</DIV>\n");

	return;
        }



/* displays hostgroup alert totals  */
public static void display_hostgroup_alert_totals(){

	/**************************/
	/**** HOSTGROUP TOTALS ****/
	/**************************/

	System.out.printf("<BR>\n");

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Totals By Hostgroup</DIV>\n");

	if(show_all_hostgroups==common_h.FALSE)
		display_specific_hostgroup_alert_totals(target_hostgroup);
	else{
		for(objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list )
			display_specific_hostgroup_alert_totals(temp_hostgroup);
	        }

	System.out.printf("</DIV>\n");
	
	return;
        }


/* displays alert totals for a specific hostgroup */
public static void display_specific_hostgroup_alert_totals(objects_h.hostgroup grp){
	int hard_host_up_alerts=0;
	int soft_host_up_alerts=0;
	int hard_host_down_alerts=0;
	int soft_host_down_alerts=0;
	int hard_host_unreachable_alerts=0;
	int soft_host_unreachable_alerts=0;
	int hard_service_ok_alerts=0;
	int soft_service_ok_alerts=0;
	int hard_service_warning_alerts=0;
	int soft_service_warning_alerts=0;
	int hard_service_unknown_alerts=0;
	int soft_service_unknown_alerts=0;
	int hard_service_critical_alerts=0;
	int soft_service_critical_alerts=0;
	objects_h.host temp_host;

	if(grp==null)
		return;

	/* make sure the user is authorized to view this hostgroup */
	if(cgiauth.is_authorized_for_hostgroup(grp,current_authdata)==common_h.FALSE)
		return;

	/* process all events */
	for(archived_event temp_event : event_list ){

		temp_host=objects.find_host(temp_event.host_name);
		if(objects.is_host_member_of_hostgroup(grp,temp_host)==common_h.FALSE)
			continue;

		/* host alerts */
		if(temp_event.event_type==AE_HOST_ALERT){
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_HOST_UP)
					soft_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					soft_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					soft_host_unreachable_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_HOST_UP)
					hard_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					hard_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					hard_host_unreachable_alerts++;
			        }
		        }

		/* service alerts */
		else{
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_SERVICE_OK)
					soft_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					soft_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					soft_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					soft_service_critical_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_SERVICE_OK)
					hard_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					hard_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					hard_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					hard_service_critical_alerts++;
			        }
		        }
	        }


	System.out.printf("<BR>\n");
	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='reportDataEven'><TR><TD>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<TR><TD COLSPAN=2 ALIGN=CENTER CLASS='dataSubTitle'>Hostgroup '%s' (%s)</TD></TR>\n",grp.group_name,grp.alias);

	System.out.printf("<TR>\n");

	if((alert_types & AE_HOST_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Host Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUP'>UP</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_up_alerts,hard_host_up_alerts,soft_host_up_alerts+hard_host_up_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='hostDOWN'>DOWN</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_host_down_alerts,hard_host_down_alerts,soft_host_down_alerts+hard_host_down_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUNREACHABLE'>UNREACHABLE</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_unreachable_alerts,hard_host_unreachable_alerts,soft_host_unreachable_alerts+hard_host_unreachable_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='dataEven'>All States</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'><B>%d</B></TD></TR>\n",soft_host_up_alerts+soft_host_down_alerts+soft_host_unreachable_alerts,hard_host_up_alerts+hard_host_down_alerts+hard_host_unreachable_alerts,soft_host_up_alerts+hard_host_up_alerts+soft_host_down_alerts+hard_host_down_alerts+soft_host_unreachable_alerts+hard_host_unreachable_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	if((alert_types & AE_SERVICE_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Service Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceOK'>OK</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_ok_alerts,hard_service_ok_alerts,soft_service_ok_alerts+hard_service_ok_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceWARNING'>WARNING</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_warning_alerts,hard_service_warning_alerts,soft_service_warning_alerts+hard_service_warning_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceUNKNOWN'>UNKNOWN</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_unknown_alerts,hard_service_unknown_alerts,soft_service_unknown_alerts+hard_service_unknown_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceCRITICAL'>CRITICAL</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_critical_alerts,hard_service_critical_alerts,soft_service_critical_alerts+hard_service_critical_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='dataOdd'>All States</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'><B>%d</B></TD></TR>\n",soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts,hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts,soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts+hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	System.out.printf("</TR>\n");

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR></TABLE>\n");

	return;
        }



/* displays host alert totals  */
public static void display_host_alert_totals(){

	/*********************/
	/**** HOST TOTALS ****/
	/*********************/

	System.out.printf("<BR>\n");

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Totals By Host</DIV>\n");

	if(show_all_hosts==common_h.FALSE)
		display_specific_host_alert_totals(target_host);
	else{
		for( objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list )
			display_specific_host_alert_totals(temp_host);
	        }

	System.out.printf("</DIV>\n");
	
	return;
        }


/* displays alert totals for a specific host */
public static void display_specific_host_alert_totals(objects_h.host hst){
	int hard_host_up_alerts=0;
	int soft_host_up_alerts=0;
	int hard_host_down_alerts=0;
	int soft_host_down_alerts=0;
	int hard_host_unreachable_alerts=0;
	int soft_host_unreachable_alerts=0;
	int hard_service_ok_alerts=0;
	int soft_service_ok_alerts=0;
	int hard_service_warning_alerts=0;
	int soft_service_warning_alerts=0;
	int hard_service_unknown_alerts=0;
	int soft_service_unknown_alerts=0;
	int hard_service_critical_alerts=0;
	int soft_service_critical_alerts=0;

	if(hst==null)
		return;

	/* make sure the user is authorized to view this host */
	if(cgiauth.is_authorized_for_host(hst,current_authdata)==common_h.FALSE)
		return;

	if(show_all_hostgroups==common_h.FALSE && target_hostgroup!=null){
		if(objects.is_host_member_of_hostgroup(target_hostgroup,hst)==common_h.FALSE)
			return;
	        }

	/* process all events */
	for(archived_event temp_event : event_list ){

		if(!temp_event.host_name.equals(hst.name))
			continue;

		/* host alerts */
		if(temp_event.event_type==AE_HOST_ALERT){
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_HOST_UP)
					soft_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					soft_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					soft_host_unreachable_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_HOST_UP)
					hard_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					hard_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					hard_host_unreachable_alerts++;
			        }
		        }

		/* service alerts */
		else{
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_SERVICE_OK)
					soft_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					soft_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					soft_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					soft_service_critical_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_SERVICE_OK)
					hard_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					hard_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					hard_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					hard_service_critical_alerts++;
			        }
		        }
	        }


	System.out.printf("<BR>\n");
	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='reportDataEven'><TR><TD>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<TR><TD COLSPAN=2 ALIGN=CENTER CLASS='dataSubTitle'>Host '%s' (%s)</TD></TR>\n",hst.name,hst.alias);

	System.out.printf("<TR>\n");

	if((alert_types & AE_HOST_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Host Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUP'>UP</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_up_alerts,hard_host_up_alerts,soft_host_up_alerts+hard_host_up_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='hostDOWN'>DOWN</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_host_down_alerts,hard_host_down_alerts,soft_host_down_alerts+hard_host_down_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUNREACHABLE'>UNREACHABLE</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_unreachable_alerts,hard_host_unreachable_alerts,soft_host_unreachable_alerts+hard_host_unreachable_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='dataEven'>All States</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'><B>%d</B></TD></TR>\n",soft_host_up_alerts+soft_host_down_alerts+soft_host_unreachable_alerts,hard_host_up_alerts+hard_host_down_alerts+hard_host_unreachable_alerts,soft_host_up_alerts+hard_host_up_alerts+soft_host_down_alerts+hard_host_down_alerts+soft_host_unreachable_alerts+hard_host_unreachable_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	if((alert_types & AE_SERVICE_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Service Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceOK'>OK</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_ok_alerts,hard_service_ok_alerts,soft_service_ok_alerts+hard_service_ok_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceWARNING'>WARNING</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_warning_alerts,hard_service_warning_alerts,soft_service_warning_alerts+hard_service_warning_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceUNKNOWN'>UNKNOWN</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_unknown_alerts,hard_service_unknown_alerts,soft_service_unknown_alerts+hard_service_unknown_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceCRITICAL'>CRITICAL</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_critical_alerts,hard_service_critical_alerts,soft_service_critical_alerts+hard_service_critical_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='dataOdd'>All States</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'><B>%d</B></TD></TR>\n",soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts,hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts,soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts+hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	System.out.printf("</TR>\n");

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR></TABLE>\n");

	return;
        }


/* displays servicegroup alert totals  */
public static void display_servicegroup_alert_totals(){

	/**************************/
	/**** SERVICEGROUP TOTALS ****/
	/**************************/

	System.out.printf("<BR>\n");

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Totals By Servicegroup</DIV>\n");

	if(show_all_servicegroups==common_h.FALSE)
		display_specific_servicegroup_alert_totals(target_servicegroup);
	else{
		for(objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>) objects.servicegroup_list )
			display_specific_servicegroup_alert_totals(temp_servicegroup);
	        }

	System.out.printf("</DIV>\n");
	
	return;
        }


/* displays alert totals for a specific servicegroup */
public static void display_specific_servicegroup_alert_totals(objects_h.servicegroup grp){
	int hard_host_up_alerts=0;
	int soft_host_up_alerts=0;
	int hard_host_down_alerts=0;
	int soft_host_down_alerts=0;
	int hard_host_unreachable_alerts=0;
	int soft_host_unreachable_alerts=0;
	int hard_service_ok_alerts=0;
	int soft_service_ok_alerts=0;
	int hard_service_warning_alerts=0;
	int soft_service_warning_alerts=0;
	int hard_service_unknown_alerts=0;
	int soft_service_unknown_alerts=0;
	int hard_service_critical_alerts=0;
	int soft_service_critical_alerts=0;
	objects_h.host temp_host;
    objects_h.service temp_service;

	if(grp==null)
		return;

	/* make sure the user is authorized to view this servicegroup */
	if(cgiauth.is_authorized_for_servicegroup(grp,current_authdata)==common_h.FALSE)
		return;

	/* process all events */
	for(archived_event temp_event : event_list ){

		if(temp_event.event_type==AE_HOST_ALERT){

			temp_host=objects.find_host(temp_event.host_name);
			if(objects.is_host_member_of_servicegroup(grp,temp_host)==common_h.FALSE)
				continue;
		        }
		else{

			temp_service=objects.find_service(temp_event.host_name,temp_event.service_description);
			if(objects.is_service_member_of_servicegroup(grp,temp_service)==common_h.FALSE)
				continue;
		        }

		/* host alerts */
		if(temp_event.event_type==AE_HOST_ALERT){
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_HOST_UP)
					soft_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					soft_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					soft_host_unreachable_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_HOST_UP)
					hard_host_up_alerts++;
				else if(temp_event.entry_type==AE_HOST_DOWN)
					hard_host_down_alerts++;
				else if(temp_event.entry_type==AE_HOST_UNREACHABLE)
					hard_host_unreachable_alerts++;
			        }
		        }

		/* service alerts */
		else{
			if(temp_event.state_type==AE_SOFT_STATE){
				if(temp_event.entry_type==AE_SERVICE_OK)
					soft_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					soft_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					soft_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					soft_service_critical_alerts++;
			        }
			else{
				if(temp_event.entry_type==AE_SERVICE_OK)
					hard_service_ok_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_WARNING)
					hard_service_warning_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
					hard_service_unknown_alerts++;
				else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
					hard_service_critical_alerts++;
			        }
		        }
	        }


	System.out.printf("<BR>\n");
	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='reportDataEven'><TR><TD>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<TR><TD COLSPAN=2 ALIGN=CENTER CLASS='dataSubTitle'>Servicegroup '%s' (%s)</TD></TR>\n",grp.group_name,grp.alias);

	System.out.printf("<TR>\n");

	if((alert_types & AE_HOST_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Host Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUP'>UP</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_up_alerts,hard_host_up_alerts,soft_host_up_alerts+hard_host_up_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='hostDOWN'>DOWN</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_host_down_alerts,hard_host_down_alerts,soft_host_down_alerts+hard_host_down_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='hostUNREACHABLE'>UNREACHABLE</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_host_unreachable_alerts,hard_host_unreachable_alerts,soft_host_unreachable_alerts+hard_host_unreachable_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='dataEven'>All States</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'><B>%d</B></TD></TR>\n",soft_host_up_alerts+soft_host_down_alerts+soft_host_unreachable_alerts,hard_host_up_alerts+hard_host_down_alerts+hard_host_unreachable_alerts,soft_host_up_alerts+hard_host_up_alerts+soft_host_down_alerts+hard_host_down_alerts+soft_host_unreachable_alerts+hard_host_unreachable_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	if((alert_types & AE_SERVICE_ALERT)!=0){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Service Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceOK'>OK</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_ok_alerts,hard_service_ok_alerts,soft_service_ok_alerts+hard_service_ok_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceWARNING'>WARNING</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_warning_alerts,hard_service_warning_alerts,soft_service_warning_alerts+hard_service_warning_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceUNKNOWN'>UNKNOWN</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_unknown_alerts,hard_service_unknown_alerts,soft_service_unknown_alerts+hard_service_unknown_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceCRITICAL'>CRITICAL</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_critical_alerts,hard_service_critical_alerts,soft_service_critical_alerts+hard_service_critical_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='dataOdd'>All States</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'><B>%d</B></TD></TR>\n",soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts,hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts,soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts+hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	System.out.printf("</TR>\n");

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR></TABLE>\n");

	return;
        }



/* displays service alert totals  */
public static void display_service_alert_totals(){

	/************************/
	/**** SERVICE TOTALS ****/
	/************************/

	System.out.printf("<BR>\n");

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Totals By Service</DIV>\n");

	for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list )
		display_specific_service_alert_totals(temp_service);

	System.out.printf("</DIV>\n");

        }


/* displays alert totals for a specific service */
public static void display_specific_service_alert_totals(objects_h.service svc){
	int hard_service_ok_alerts=0;
	int soft_service_ok_alerts=0;
	int hard_service_warning_alerts=0;
	int soft_service_warning_alerts=0;
	int hard_service_unknown_alerts=0;
	int soft_service_unknown_alerts=0;
	int hard_service_critical_alerts=0;
	int soft_service_critical_alerts=0;
	objects_h.host temp_host;

	if(svc==null)
		return;

	/* make sure the user is authorized to view this service */
	if(cgiauth.is_authorized_for_service(svc,current_authdata)==common_h.FALSE)
		return;

	if(show_all_hostgroups==common_h.FALSE && target_hostgroup!=null){
		temp_host=objects.find_host(svc.host_name);
		if(objects.is_host_member_of_hostgroup(target_hostgroup,temp_host)==common_h.FALSE)
			return;
	        }

	if(show_all_hosts==common_h.FALSE && target_host!=null){
		if(!target_host.name.equals(svc.host_name))
			return;
	        }

	/* process all events */
	for(archived_event temp_event : event_list ){

		if(temp_event.event_type!=AE_SERVICE_ALERT)
			continue;

		if(!temp_event.host_name.equals(svc.host_name) || !temp_event.service_description.equals(svc.description))
			continue;

		/* service alerts */
		if(temp_event.state_type==AE_SOFT_STATE){
			if(temp_event.entry_type==AE_SERVICE_OK)
				soft_service_ok_alerts++;
			else if(temp_event.entry_type==AE_SERVICE_WARNING)
				soft_service_warning_alerts++;
			else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
				soft_service_unknown_alerts++;
			else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
				soft_service_critical_alerts++;
		        }
		else{
			if(temp_event.entry_type==AE_SERVICE_OK)
				hard_service_ok_alerts++;
			else if(temp_event.entry_type==AE_SERVICE_WARNING)
				hard_service_warning_alerts++;
			else if(temp_event.entry_type==AE_SERVICE_UNKNOWN)
				hard_service_unknown_alerts++;
			else if(temp_event.entry_type==AE_SERVICE_CRITICAL)
				hard_service_critical_alerts++;
		        }
	        }


	System.out.printf("<BR>\n");
	System.out.printf("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 CLASS='reportDataEven'><TR><TD>\n");
	System.out.printf("<TABLE BORDER=0>\n");

	System.out.printf("<TR><TD COLSPAN=2 ALIGN=CENTER CLASS='dataSubTitle'>Service '%s' on Host '%s'</TD></TR>\n",svc.description,svc.host_name);

	System.out.printf("<TR>\n");

	if (0!=(alert_types & AE_SERVICE_ALERT)){

		System.out.printf("<TD ALIGN=CENTER VALIGN=TOP>\n");

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Service Alerts</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Soft Alerts</TH><TH CLASS='data'>Hard Alerts</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceOK'>OK</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_ok_alerts,hard_service_ok_alerts,soft_service_ok_alerts+hard_service_ok_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceWARNING'>WARNING</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_warning_alerts,hard_service_warning_alerts,soft_service_warning_alerts+hard_service_warning_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='serviceUNKNOWN'>UNKNOWN</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD></TR>\n",soft_service_unknown_alerts,hard_service_unknown_alerts,soft_service_unknown_alerts+hard_service_unknown_alerts);
		System.out.printf("<TR CLASS='dataEven'><TD CLASS='serviceCRITICAL'>CRITICAL</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD><TD CLASS='dataEven'>%d</TD></TR>\n",soft_service_critical_alerts,hard_service_critical_alerts,soft_service_critical_alerts+hard_service_critical_alerts);
		System.out.printf("<TR CLASS='dataOdd'><TD CLASS='dataOdd'>All States</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'>%d</TD><TD CLASS='dataOdd'><B>%d</B></TD></TR>\n",soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts,hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts,soft_service_ok_alerts+soft_service_warning_alerts+soft_service_unknown_alerts+soft_service_critical_alerts+hard_service_ok_alerts+hard_service_warning_alerts+hard_service_unknown_alerts+hard_service_critical_alerts);

		System.out.printf("</TABLE>\n");
		System.out.printf("</DIV>\n");

		System.out.printf("</TD>\n");
	        }

	System.out.printf("</TR>\n");

	System.out.printf("</TABLE>\n");
	System.out.printf("</TD></TR></TABLE>\n");

	return;
        }


/* find a specific alert producer */
public static alert_producer find_producer(int type, String hname, String sdesc){

	for(alert_producer temp_producer : producer_list ){

		if(temp_producer.producer_type!=type)
			continue;
		if(hname!=null && !hname.equals(temp_producer.host_name))
			continue;
		if(sdesc!=null && !sdesc.equals(temp_producer.service_description))
			continue;

		return temp_producer;
	        }

	return null;
        }


/* adds a new producer to the list in memory */
public static alert_producer add_producer(int producer_type, String host_name, String service_description){
	alert_producer new_producer=null;

	/* allocate memory for the new entry */
	new_producer= new alert_producer();

	/* allocate memory for the host name */
			new_producer.host_name = host_name;

	/* allocate memory for the service description */
			new_producer.service_description = service_description;

	new_producer.producer_type=producer_type;
	new_producer.total_alerts=0;

	/* add the new entry to the list in memory, sorted by time */
    producer_list.add( new_producer );

	return new_producer;
        }

/* displays top alerts */
public static void display_top_alerts(){
//	archived_event temp_event=null;
	alert_producer temp_producer=null;
	int producer_type=AE_HOST_PRODUCER;
	int current_item=0;
	int odd=0;
	String bgclass="";

	/* process all events */
	for(archived_event temp_event : event_list ){
		
		producer_type=(temp_event.event_type==AE_HOST_ALERT)?AE_HOST_PRODUCER:AE_SERVICE_PRODUCER;

		/* see if we already have a record for the producer */
		temp_producer=find_producer(producer_type,temp_event.host_name,temp_event.service_description);

		/* if not, add a record */
		if(temp_producer==null)
			temp_producer=add_producer(producer_type,temp_event.host_name,temp_event.service_description);

		/* producer record could not be added */
		if(temp_producer==null)
			continue;

		/* update stats for producer */
		temp_producer.total_alerts++;
	        }

    Comparator<alert_producer> comparator = new Comparator<alert_producer>() {
        
        public int compare( alert_producer new_producer, alert_producer temp_producer ) {
            return new_producer.total_alerts - temp_producer.total_alerts;
        }
    };

	/* sort the producer list by total alerts (descending) */
    java.util.Collections.sort( producer_list, comparator );
	total_items=producer_list.size();

	System.out.printf("<BR>\n");

	if(item_limit<=0 || total_items<=item_limit || total_items==0)
		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Displaying all %d matching alert producers\n",total_items);
	else
		System.out.printf("<DIV ALIGN=CENTER CLASS='dataSubTitle'>Displaying top %d of %d total matching alert producers\n",item_limit,total_items);

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR><TH CLASS='data'>Rank</TH><TH CLASS='data'>Producer Type</TH><TH CLASS='data'>Host</TH><TH CLASS='data'>Service</TH><TH CLASS='data'>Total Alerts</TH></TR>\n");

	/* display top producers */
	for( java.util.Iterator<alert_producer> iter = producer_list.iterator(); iter.hasNext(); ){
        temp_producer = iter.next();

		if(current_item>=item_limit && item_limit>0)
			break;

		current_item++;

		if(odd != 0){
			odd=0;
			bgclass="Odd";
	                }
		else{
			odd=1;
			bgclass="Even";
	                }

		System.out.printf("<tr CLASS='data%s'>",bgclass);

		System.out.printf("<td CLASS='data%s'>#%d</td>",bgclass,current_item);

		System.out.printf("<td CLASS='data%s'>%s</td>",bgclass,(temp_producer.producer_type==AE_HOST_PRODUCER)?"Host":"Service");

		System.out.printf("<td CLASS='data%s'><a href='%s?type=%d&host=%s'>%s</a></td>",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_HOST_INFO,cgiutils.url_encode(temp_producer.host_name),temp_producer.host_name);

		if(temp_producer.producer_type==AE_HOST_PRODUCER)
			System.out.printf("<td CLASS='data%s'>N/A</td>",bgclass);
		else{
			System.out.printf("<td CLASS='data%s'><a href='%s?type=%d&host=%s",bgclass,cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_SERVICE_INFO,cgiutils.url_encode(temp_producer.host_name));
			System.out.printf("&service=%s'>%s</a></td>",cgiutils.url_encode(temp_producer.service_description),temp_producer.service_description);
		        }

		System.out.printf("<td CLASS='data%s'>%d</td>",bgclass,temp_producer.total_alerts);

		System.out.printf("</tr>\n");
	        }

	System.out.printf("</TABLE>\n");
	System.out.printf("</DIV>\n");

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