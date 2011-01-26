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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.include.*;

public class histogram extends blue_servlet { 

public static String HISTOGRAM_IMAGE         = "histogram.png";

/* archived state types */
public static final int  AS_NO_DATA              =0;
public static final int  AS_PROGRAM_START        =1;
public static final int  AS_PROGRAM_END          =2;
public static final int  AS_HOST_UP		=3;
public static final int  AS_HOST_DOWN		=4;
public static final int  AS_HOST_UNREACHABLE	=5;
public static final int  AS_SVC_OK		=6;
public static final int  AS_SVC_UNKNOWN		=7;
public static final int  AS_SVC_WARNING		=8;
public static final int  AS_SVC_CRITICAL		=9;


/* display types */
public static final int  DISPLAY_HOST_HISTOGRAM	        =0;
public static final int  DISPLAY_SERVICE_HISTOGRAM	=1;
public static final int  DISPLAY_NO_HISTOGRAM    	=2;

/* input types */
public static final int  GET_INPUT_NONE                  =0;
public static final int  GET_INPUT_TARGET_TYPE           =1;
public static final int  GET_INPUT_HOST_TARGET           =2;
public static final int  GET_INPUT_SERVICE_TARGET        =3;
public static final int  GET_INPUT_OPTIONS               =4;

/* breakdown types */
public static final int  BREAKDOWN_MONTHLY       =0;
public static final int  BREAKDOWN_DAY_OF_MONTH  =1;
public static final int  BREAKDOWN_DAY_OF_WEEK   =2;
public static final int  BREAKDOWN_HOURLY        =3;

/* modes */
public static final int  CREATE_HTML		=0;
public static final int  CREATE_IMAGE		=1;

/* standard report times */
public static final int  TIMEPERIOD_CUSTOM	=0;
public static final int  TIMEPERIOD_TODAY	=1;
public static final int  TIMEPERIOD_YESTERDAY	=2;
public static final int  TIMEPERIOD_THISWEEK	=3;
public static final int  TIMEPERIOD_LASTWEEK	=4;
public static final int  TIMEPERIOD_THISMONTH	=5;
public static final int  TIMEPERIOD_LASTMONTH	=6;
public static final int  TIMEPERIOD_THISQUARTER	=7;
public static final int  TIMEPERIOD_LASTQUARTER	=8;
public static final int  TIMEPERIOD_THISYEAR	=9;
public static final int  TIMEPERIOD_LASTYEAR	=10;
public static final int  TIMEPERIOD_LAST24HOURS	=11;
public static final int  TIMEPERIOD_LAST7DAYS	=12;
public static final int  TIMEPERIOD_LAST31DAYS   =13;


public static final int  MAX_ARCHIVE_SPREAD	=65;
public static final int  MAX_ARCHIVE		=65;
public static final int  MAX_ARCHIVE_BACKTRACKS	=60;

public static final int  DRAWING_WIDTH	        =550;
public static final int  DRAWING_HEIGHT	        =195;
public static final int  DRAWING_X_OFFSET	=60;
public static final int  DRAWING_Y_OFFSET        =235;

public static final int  GRAPH_HOST_UP                   =1;
public static final int  GRAPH_HOST_DOWN                 =2;
public static final int  GRAPH_HOST_UNREACHABLE          =4;
public static final int  GRAPH_SERVICE_OK                =8;
public static final int  GRAPH_SERVICE_WARNING           =16;
public static final int  GRAPH_SERVICE_UNKNOWN           =32;
public static final int  GRAPH_SERVICE_CRITICAL          =64;

public static final int  GRAPH_HOST_PROBLEMS             =6;
public static final int  GRAPH_HOST_ALL                  =7;

public static final int  GRAPH_SERVICE_PROBLEMS          =112;
public static final int  GRAPH_SERVICE_ALL               =120;

public static final int  GRAPH_EVERYTHING                =255;

public static final int  GRAPH_SOFT_STATETYPES           =1;
public static final int  GRAPH_HARD_STATETYPES           =2;
public static final int  GRAPH_ALL_STATETYPES            =3;

public static class timeslice_data{
	public long    service_ok;
    public long    host_up;
    public long    service_critical;
    public long    host_down;
    public long    service_unknown;
    public long    host_unreachable;
    public long    service_warning;
}

public static cgiauth_h.authdata current_authdata;

public static timeslice_data[] tsdata;

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

public static int display_type=DISPLAY_NO_HISTOGRAM;
public static int mode=CREATE_HTML;
public static int input_type=GET_INPUT_NONE;
public static int timeperiod_type=TIMEPERIOD_LAST24HOURS;
public static int breakdown_type=BREAKDOWN_HOURLY;
public static int compute_time_from_parts=common_h.FALSE;

public static int initial_states_logged=common_h.FALSE;
public static int assume_state_retention=common_h.TRUE;
public static int new_states_only=common_h.FALSE;

public static int last_state=AS_NO_DATA;
public static int program_restart_has_occurred=common_h.FALSE;

public static int graph_events=GRAPH_EVERYTHING;
public static int graph_statetypes=GRAPH_HARD_STATETYPES;

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;

public static String host_name="";
public static String svc_description="";

public static BufferedImage histogram_image = null;
public static Graphics2D gd = null;

public static Color color_white;
public static Color color_black;
public static Color color_red;
public static Color color_darkred;
public static Color color_green;
public static Color color_yellow;
public static Color color_orange;
public static Color color_lightgray;

public static File image_file=null;

public static int backtrack_archives=0;
public static int earliest_archive=0;
public static long earliest_time;
public static long latest_time;

public static int image_width=900;
public static int image_height=320;

public static int total_buckets=96;

public void reset_context() {
   current_authdata = new cgiauth_h.authdata ();

   tsdata = null;

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

   display_type=DISPLAY_NO_HISTOGRAM;
   mode=CREATE_HTML;
   input_type=GET_INPUT_NONE;
   timeperiod_type=TIMEPERIOD_LAST24HOURS;
   breakdown_type=BREAKDOWN_HOURLY;
   compute_time_from_parts=common_h.FALSE;

   initial_states_logged=common_h.FALSE;
   assume_state_retention=common_h.TRUE;
   new_states_only=common_h.FALSE;

   last_state=AS_NO_DATA;
   program_restart_has_occurred=common_h.FALSE;

   graph_events=GRAPH_EVERYTHING;
   graph_statetypes=GRAPH_HARD_STATETYPES;

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;

   host_name="";
   svc_description="";

   histogram_image = null;
   gd = null;

   color_white = null;
   color_black = null;
   color_red = null;
   color_darkred = null;
   color_green = null;
   color_yellow = null;
   color_orange = null;
   color_lightgray = null;

   image_file=null;

   backtrack_archives=0;
   earliest_archive=0;
   earliest_time =0;
   latest_time = 0;

   image_width=900;
   image_height=320;

   total_buckets=96;   
}

public void call_main() {
   main( null );
}

public static void main(String[] args){
	int result=common_h.OK;
	String temp_buffer ; // MAX_INPUT_BUFFER
	String image_template ; // MAX_INPUT_BUFFER
	String start_timestring ; // MAX_INPUT_BUFFER
	String end_timestring ; // MAX_INPUT_BUFFER
	objects_h.host temp_host;
	objects_h.service temp_service;
	int is_authorized=common_h.TRUE;
	int found=common_h.FALSE;
	
	String first_service=null;
	int x;
	long t3;
	long current_time;
	Calendar t = Calendar.getInstance();

	/* initialize time period to last 24 hours */
	t2 = utils.currentTimeInSeconds();
	t1=(t2-(60*60*24));

    /* get the arguments passed in the URL */
    process_cgivars();

	/* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		if(mode==CREATE_HTML){
			document_header(common_h.FALSE);
            cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
			document_footer();
		        }
		cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		if(mode==CREATE_HTML){
			document_header(common_h.FALSE);
            cgiutils.main_config_file_error(cgiutils.main_config_file);
			document_footer();
		        }
        cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		if(mode==CREATE_HTML){
			document_header(common_h.FALSE);
            cgiutils.object_data_error();
			document_footer();
		        }
        cgiutils.exit(  common_h.ERROR );
        return;
                }

	/* read all status data */
	result=cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(),statusdata_h.READ_ALL_STATUS_DATA);
	if(result==common_h.ERROR){
		if(mode==CREATE_HTML){
			document_header(common_h.FALSE);
            cgiutils.status_data_error();
			document_footer();
		        }
        cgiutils.exit(  common_h.ERROR );
        return;
                }

	document_header(common_h.TRUE);

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	if(compute_time_from_parts==common_h.TRUE)
		compute_report_times();

	/* make sure times are sane, otherwise swap them */
	if(t2<t1){
		t3=t2;
		t2=t1;
		t1=t3;
	        }
			

	if(mode==CREATE_HTML && display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%% cellspacing=0 cellpadding=0>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top width=33%%>\n");

		if(display_type==DISPLAY_HOST_HISTOGRAM)
			temp_buffer = "Host Alert Histogram";
		else if(display_type==DISPLAY_SERVICE_HISTOGRAM)
			temp_buffer = "Service Alert Histogram";
		else
			temp_buffer = "Host and Service Alert Histogram";

		cgiutils.display_info_table(temp_buffer,common_h.FALSE,current_authdata);

		if(display_type!=DISPLAY_NO_HISTOGRAM && input_type==GET_INPUT_NONE){

			System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
			System.out.printf("<TR><TD CLASS='linkBox'>\n");

			if(display_type==DISPLAY_HOST_HISTOGRAM){
				System.out.printf("<a href='%s?host=%s&t1=%d&t2=%d&assumestateretention=%s'>View Trends For This Host</a><BR>\n",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name),t1,t2,(assume_state_retention==common_h.TRUE)?"yes":"no");
				System.out.printf("<a href='%s?host=%s&t1=%d&t2=%d&assumestateretention=%s&show_log_entries'>View Availability Report For This Host</a><BR>\n",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(host_name),t1,t2,(assume_state_retention==common_h.TRUE)?"yes":"no");
				System.out.printf("<a href='%s?host=%s'>View Status Detail For This Host</a><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(host_name));
				System.out.printf("<a href='%s?host=%s'>View History For This Host</a><BR>\n",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
				System.out.printf("<a href='%s?host=%s'>View Notifications For This Host</a><BR>\n",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
		                }
			else{
				System.out.printf("<a href='%s?host=%s",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
				System.out.printf("&service=%s&t1=%d&t2=%d&assumestateretention=%s'>View Trends For This Service</a><BR>\n",cgiutils.url_encode(svc_description),t1,t2,(assume_state_retention==common_h.TRUE)?"yes":"no");
				System.out.printf("<a href='%s?host=%s",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(host_name));
				System.out.printf("&service=%s&t1=%d&t2=%d&assumestateretention=%s&show_log_entries'>View Availability Report For This Service</a><BR>\n",cgiutils.url_encode(svc_description),t1,t2,(assume_state_retention==common_h.TRUE)?"yes":"no");
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s'>View History This Service</A><BR>\n",cgiutils.url_encode(svc_description));
				System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
				System.out.printf("service=%s'>View Notifications For This Service</A><BR>\n",cgiutils.url_encode(svc_description));
		                }

			System.out.printf("</TD></TR>\n");
			System.out.printf("</TABLE>\n");
		        }

		System.out.printf("</td>\n");

		/* center column of top row */
		System.out.printf("<td align=center valign=top width=33%%>\n");

		if(display_type!=DISPLAY_NO_HISTOGRAM && input_type==GET_INPUT_NONE){

			System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>\n");
			if(display_type==DISPLAY_HOST_HISTOGRAM)
				System.out.printf("Host '%s'",host_name);
			else if(display_type==DISPLAY_SERVICE_HISTOGRAM)
				System.out.printf("Service '%s' On Host '%s'",svc_description,host_name);
			System.out.printf("</DIV>\n");

			System.out.printf("<BR>\n");

			System.out.printf("<IMG SRC='%s%s' BORDER=0 ALT='%s Event Histogram' TITLE='%s Event Histogram'>\n",cgiutils.url_images_path,cgiutils_h.TRENDS_ICON,(display_type==DISPLAY_HOST_HISTOGRAM)?"Host":"Service",(display_type==DISPLAY_HOST_HISTOGRAM)?"Host":"Service");

			System.out.printf("<BR CLEAR=ALL>\n");

            start_timestring = cgiutils.get_time_string( t1,common_h.SHORT_DATE_TIME);
            end_timestring = cgiutils.get_time_string( t2,common_h.SHORT_DATE_TIME);
			System.out.printf("<div align=center class='reportRange'>%s to %s</div>\n",start_timestring,end_timestring);

			cgiutils.time_breakdown tb =  cgiutils.get_time_breakdown((t2-t1) );
			System.out.printf("<div align=center class='reportDuration'>Duration: %dd %dh %dm %ds</div>\n",tb.days,tb.hours,tb.minutes,tb.seconds);
		        }

		System.out.printf("</td>\n");

		/* right hand column of top row */
		System.out.printf("<td align=right valign=bottom width=33%%>\n");

		System.out.printf("<table border=0 CLASS='optBox'>\n");

		if(display_type!=DISPLAY_NO_HISTOGRAM && input_type==GET_INPUT_NONE){

			System.out.printf("<form method=\"GET\" action=\"%s\">\n",cgiutils_h.HISTOGRAM_CGI);
			System.out.printf("<input type='hidden' name='t1' value='%d'>\n",t1);
			System.out.printf("<input type='hidden' name='t2' value='%d'>\n",t2);
			System.out.printf("<input type='hidden' name='host' value='%s'>\n",host_name);
			if(display_type==DISPLAY_SERVICE_HISTOGRAM)
				System.out.printf("<input type='hidden' name='service' value='%s'>\n",svc_description);


			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>Report period:</td><td CLASS='optBoxItem' valign=top align=left>Assume state retention:</td></tr>\n");
			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='timeperiod'>\n");
			System.out.printf("<option value=custom>[ Current time range ]\n");
			System.out.printf("<option value=today %s>Today\n",(timeperiod_type==TIMEPERIOD_TODAY)?"SELECTED":"");
			System.out.printf("<option value=last24hours %s>Last 24 Hours\n",(timeperiod_type==TIMEPERIOD_LAST24HOURS)?"SELECTED":"");
			System.out.printf("<option value=yesterday %s>Yesterday\n",(timeperiod_type==TIMEPERIOD_YESTERDAY)?"SELECTED":"");
			System.out.printf("<option value=thisweek %s>This Week\n",(timeperiod_type==TIMEPERIOD_THISWEEK)?"SELECTED":"");
			System.out.printf("<option value=last7days %s>Last 7 Days\n",(timeperiod_type==TIMEPERIOD_LAST7DAYS)?"SELECTED":"");
			System.out.printf("<option value=lastweek %s>Last Week\n",(timeperiod_type==TIMEPERIOD_LASTWEEK)?"SELECTED":"");
			System.out.printf("<option value=thismonth %s>This Month\n",(timeperiod_type==TIMEPERIOD_THISMONTH)?"SELECTED":"");
			System.out.printf("<option value=last31days %s>Last 31 Days\n",(timeperiod_type==TIMEPERIOD_LAST31DAYS)?"SELECTED":"");
			System.out.printf("<option value=lastmonth %s>Last Month\n",(timeperiod_type==TIMEPERIOD_LASTMONTH)?"SELECTED":"");
			System.out.printf("<option value=thisyear %s>This Year\n",(timeperiod_type==TIMEPERIOD_THISYEAR)?"SELECTED":"");
			System.out.printf("<option value=lastyear %s>Last Year\n",(timeperiod_type==TIMEPERIOD_LASTYEAR)?"SELECTED":"");
			System.out.printf("</select>\n");
			System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='assumestateretention'>\n");
			System.out.printf("<option value=yes %s>yes\n",(assume_state_retention==common_h.TRUE)?"SELECTED":"");
			System.out.printf("<option value=no %s>no\n",(assume_state_retention==common_h.TRUE)?"":"SELECTED");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>Breakdown type:</td><td CLASS='optBoxItem' valign=top align=left>Initial states logged:</td></tr>\n");
			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='breakdown'>\n");
			System.out.printf("<option value=monthly %s>Month\n",(breakdown_type==BREAKDOWN_MONTHLY)?"SELECTED":"");
			System.out.printf("<option value=dayofmonth %s>Day of the Month\n",(breakdown_type==BREAKDOWN_DAY_OF_MONTH)?"SELECTED":"");
			System.out.printf("<option value=dayofweek %s>Day of the Week\n",(breakdown_type==BREAKDOWN_DAY_OF_WEEK)?"SELECTED":"");
			System.out.printf("<option value=hourly %s>Hour of the Day\n",(breakdown_type==BREAKDOWN_HOURLY)?"SELECTED":"");
			System.out.printf("</select>\n");
			System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='initialstateslogged'>\n");
			System.out.printf("<option value=yes %s>yes\n",(initial_states_logged==common_h.TRUE)?"SELECTED":"");
			System.out.printf("<option value=no %s>no\n",(initial_states_logged==common_h.TRUE)?"":"SELECTED");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>Events to graph:</td><td CLASS='optBoxItem' valign=top align=left>Ignore repeated states:</td></tr>\n");
			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='graphevents'>\n");
			if(display_type==DISPLAY_HOST_HISTOGRAM){
				System.out.printf("<option value=%d %s>All host events\n",GRAPH_HOST_ALL,(graph_events==GRAPH_HOST_ALL)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host problem events\n",GRAPH_HOST_PROBLEMS,(graph_events==GRAPH_HOST_PROBLEMS)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host up events\n",GRAPH_HOST_UP,(graph_events==GRAPH_HOST_UP)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host down events\n",GRAPH_HOST_DOWN,(graph_events==GRAPH_HOST_DOWN)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host unreachable events\n",GRAPH_HOST_UNREACHABLE,(graph_events==GRAPH_HOST_UNREACHABLE)?"SELECTED":"");
			        }
			else{
				System.out.printf("<option value=%d %s>All service events\n",GRAPH_SERVICE_ALL,(graph_events==GRAPH_SERVICE_ALL)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service problem events\n",GRAPH_SERVICE_PROBLEMS,(graph_events==GRAPH_SERVICE_PROBLEMS)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service ok events\n",GRAPH_SERVICE_OK,(graph_events==GRAPH_SERVICE_OK)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service warning events\n",GRAPH_SERVICE_WARNING,(graph_events==GRAPH_SERVICE_WARNING)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service unknown events\n",GRAPH_SERVICE_UNKNOWN,(graph_events==GRAPH_SERVICE_UNKNOWN)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service critical events\n",GRAPH_SERVICE_CRITICAL,(graph_events==GRAPH_SERVICE_CRITICAL)?"SELECTED":"");
			        }
			System.out.printf("</select>\n");
			System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='newstatesonly'>\n");
			System.out.printf("<option value=yes %s>yes\n",(new_states_only==common_h.TRUE)?"SELECTED":"");
			System.out.printf("<option value=no %s>no\n",(new_states_only==common_h.TRUE)?"":"SELECTED");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>State types to graph:</td><td CLASS='optBoxItem' valign=top align=left></td></tr>\n");
			System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<select name='graphstatetypes'>\n");
			System.out.printf("<option value=%d %s>Hard states\n",GRAPH_HARD_STATETYPES,(graph_statetypes==GRAPH_HARD_STATETYPES)?"SELECTED":"");
			System.out.printf("<option value=%d %s>Soft states\n",GRAPH_SOFT_STATETYPES,(graph_statetypes==GRAPH_SOFT_STATETYPES)?"SELECTED":"");
			System.out.printf("<option value=%d %s>Hard and soft states\n",GRAPH_ALL_STATETYPES,(graph_statetypes==GRAPH_ALL_STATETYPES)?"SELECTED":"");
			System.out.printf("</select>\n");
			System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
			System.out.printf("<input type='submit' value='Update'>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("</form>\n");
		        }

		/* display context-sensitive help */
		System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
		if(display_type!=DISPLAY_NO_HISTOGRAM && input_type==GET_INPUT_NONE){
			if(display_type==DISPLAY_HOST_HISTOGRAM)
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_HOST);
			else
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_SERVICE);
		        }
		else if(display_type==DISPLAY_NO_HISTOGRAM || input_type!=GET_INPUT_NONE){
			if(input_type==GET_INPUT_NONE)
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_MENU1);
			else if(input_type==GET_INPUT_TARGET_TYPE)
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_MENU1);
			else if(input_type==GET_INPUT_HOST_TARGET)
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_MENU2);
			else if(input_type==GET_INPUT_SERVICE_TARGET)
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_MENU3);
			else if(input_type==GET_INPUT_OPTIONS)
				cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_HISTOGRAM_MENU4);
		        }
		System.out.printf("</td></tr>\n");

		System.out.printf("</table>\n");

		System.out.printf("</td>\n");

		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");
	        }

	/* check authorization... */
	if(display_type==DISPLAY_HOST_HISTOGRAM){
		temp_host=objects.find_host(host_name);
		if(temp_host==null || cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			is_authorized=common_h.FALSE;
	        }
	else if(display_type==DISPLAY_SERVICE_HISTOGRAM){
		temp_service=objects.find_service(host_name,svc_description);
		if(temp_service==null || cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			is_authorized=common_h.FALSE;
	        }
	if(is_authorized==common_h.FALSE){

		if(mode==CREATE_HTML)
			System.out.printf("<P><DIV ALIGN=CENTER CLASS='errorMessage'>It appears as though you are not authorized to view information for the specified %s...</DIV></P>\n",(display_type==DISPLAY_HOST_HISTOGRAM)?"host":"service");

		document_footer();
		cgiutils.free_memory();
		cgiutils.exit(  common_h.ERROR );
	        }

	if(display_type!=DISPLAY_NO_HISTOGRAM && input_type==GET_INPUT_NONE){

		/* print URL to image */
		if(mode==CREATE_HTML){

			System.out.printf("<BR><BR>\n");
			System.out.printf("<DIV ALIGN=CENTER>\n");
			System.out.printf("<IMG SRC='%s?createimage&t1=%d&t2=%d",cgiutils_h.HISTOGRAM_CGI,t1,t2);
			System.out.printf("&host=%s",cgiutils.url_encode(host_name));
			if(display_type==DISPLAY_SERVICE_HISTOGRAM)
				System.out.printf("&service=%s",cgiutils.url_encode(svc_description));
			System.out.printf("&breakdown=");
			if(breakdown_type==BREAKDOWN_MONTHLY)
				System.out.printf("monthly");
			else if(breakdown_type==BREAKDOWN_DAY_OF_MONTH)
				System.out.printf("dayofmonth");
			else if(breakdown_type==BREAKDOWN_DAY_OF_WEEK)
				System.out.printf("dayofweek");
			else
				System.out.printf("hourly");
			System.out.printf("&assumestateretention=%s",(assume_state_retention==common_h.TRUE)?"yes":"no");
			System.out.printf("&initialstateslogged=%s",(initial_states_logged==common_h.TRUE)?"yes":"no");
			System.out.printf("&newstatesonly=%s",(new_states_only==common_h.TRUE)?"yes":"no");
			System.out.printf("&graphevents=%d",graph_events);
			System.out.printf("&graphstatetypes=%d",graph_statetypes);
			System.out.printf("' BORDER=0 name='histogramimage'>\n");
			System.out.printf("</DIV>\n");
		        }

		/* read and process state data */
		else{

			/* allocate memory */
			tsdata=null;
			if(breakdown_type==BREAKDOWN_MONTHLY)
				total_buckets=12;
			else if(breakdown_type==BREAKDOWN_DAY_OF_MONTH)
				total_buckets=31;
			else if(breakdown_type==BREAKDOWN_DAY_OF_WEEK)
				total_buckets=7;
			else
				total_buckets=96;

			tsdata= new timeslice_data[total_buckets];
			if(tsdata==null)
				cgiutils.exit(  common_h.ERROR );

			for(x=0;x<total_buckets;x++){
				tsdata[x].service_ok=0L;
				tsdata[x].service_unknown=0L;
				tsdata[x].service_warning=0L;
				tsdata[x].service_critical=0L;
				tsdata[x].host_up=0L;
				tsdata[x].host_down=0L;
				tsdata[x].host_unreachable=0L;
			        }

			/* read in all necessary archived state data */
			read_archived_state_data();

//#ifdef DEBUG
//			System.out.printf("Done reading archived state data.\n");
//#endif

			/* location of image template */            
			image_template = String.format( "%s/%s",cgiutils.physical_images_path,HISTOGRAM_IMAGE);

			/* allocate buffer for storing image */
            try {
                histogram_image = ImageIO.read( new File( image_template ));
            } catch (IOException ioE) {
                // TODO log this
                histogram_image= new BufferedImage( image_width,image_height, BufferedImage.TYPE_INT_ARGB );
            }
 
			if(histogram_image==null){
//#ifdef DEBUG
//				System.out.printf("Error: Could not allocate memory for image\n");
//#endif
				cgiutils.exit(  common_h.ERROR );
	                        }

            gd = histogram_image.createGraphics();
            
			/* allocate colors used for drawing */
			color_white= Color.WHITE;
			color_black=Color.BLACK;
			color_red=Color.RED;
			color_darkred=new Color( 128,0,0);
			color_green=new Color( 0,128,0);
			color_yellow=new Color( 176,178,20);
			color_orange=new Color( 255,100,25);
			color_lightgray=new Color( 192,192,192);

			/* set transparency index */
//			gdImageColorTransparent(histogram_image,color_white);

			/* make sure the graphic is interlaced */
//			gdImageInterlace(histogram_image,1);

//#ifdef DEBUG
//			System.out.printf("Starting to graph data...\n");
//#endif

			/* graph archived state histogram data */
			graph_all_histogram_data();

//#ifdef DEBUG
//			System.out.printf("Done graphing data.\n");
//#endif

			/* use STDOUT for writing the image data... */
//			image_file=stdout;

            /* write the image to file */
            try {
//                ImageIO.write(histogram_image, "png", System.out );
// #ifdef DEBUG
                ImageIO.write(histogram_image, "png", new File("c://histogram.png") );
//              image_file=fopen("/tmp/histogram.png","w");
//    #endif
                
            } catch (IOException ioE ) {
                ioE.printStackTrace();
            }

	                }
	        }

	/* show user a selection of hosts and services to choose from... */
	if(display_type==DISPLAY_NO_HISTOGRAM || input_type!=GET_INPUT_NONE){

		/* ask the user for what host they want a report for */
		if(input_type==GET_INPUT_HOST_TARGET){

			System.out.printf("<P><DIV ALIGN=CENTER>\n");
			System.out.printf("<DIV CLASS='reportSelectTitle'>Step 2: Select Host</DIV>\n");
			System.out.printf("</DIV></P>\n");

			System.out.printf("<P><DIV ALIGN=CENTER>\n");

			System.out.printf("<TABLE BORDER=0 cellspacing=0 cellpadding=10>\n");
			System.out.printf("<form method=\"GET\" action=\"%s\">\n",cgiutils_h.HISTOGRAM_CGI);
			System.out.printf("<input type='hidden' name='input' value='getoptions'>\n");

			System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Host:</td>\n");
			System.out.printf("<td class='reportSelectItem' valign=center>\n");
			System.out.printf("<select name='host'>\n");

			for(Iterator iter = objects.host_list.iterator(); iter.hasNext();  ){
                temp_host= (objects_h.host) iter.next();
				if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.TRUE)
					System.out.printf("<option value='%s'>%s\n",temp_host.name,temp_host.name);
			        }

			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td></td><td class='reportSelectItem'>\n");
			System.out.printf("<input type='submit' value='Continue to Step 3'>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("</form>\n");
			System.out.printf("</TABLE>\n");

			System.out.printf("</DIV></P>\n");
		        }

		/* ask the user for what service they want a report for */
		else if(input_type==GET_INPUT_SERVICE_TARGET){

			System.out.printf("<SCRIPT LANGUAGE='JavaScript'>\n");
			System.out.printf("function gethostname(hostindex){\n");
			System.out.printf("hostnames=[");

			for( Iterator iter = objects.service_list.iterator(); iter.hasNext(); ){
                temp_service=(objects_h.service) iter.next();
				if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.TRUE){
					if(found==common_h.TRUE)
						System.out.printf(",");
					else
						first_service=temp_service.host_name;
					System.out.printf(" \"%s\"",temp_service.host_name);
					found=common_h.TRUE;
			                }
		                }
		
			System.out.printf(" ]\n");
			System.out.printf("return hostnames[hostindex];\n");
			System.out.printf("}\n");
			System.out.printf("</SCRIPT>\n");


			System.out.printf("<P><DIV ALIGN=CENTER>\n");
			System.out.printf("<DIV CLASS='reportSelectTitle'>Step 2: Select Service</DIV>\n");
			System.out.printf("</DIV></P>\n");

			System.out.printf("<P><DIV ALIGN=CENTER>\n");
			
			System.out.printf("<TABLE BORDER=0 cellpadding=5>\n");
			System.out.printf("<form method=\"GET\" action=\"%s\" name=\"serviceform\">\n",cgiutils_h.HISTOGRAM_CGI);
			System.out.printf("<input type='hidden' name='input' value='getoptions'>\n");
			System.out.printf("<input type='hidden' name='host' value='%s'>\n",(first_service==null)?"unknown":first_service);

			System.out.printf("<tr><td class='reportSelectSubTitle'>Service:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='service' onFocus='document.serviceform.host.value=gethostname(this.selectedIndex);' onChange='document.serviceform.host.value=gethostname(this.selectedIndex);'>\n");

            for( Iterator iter = objects.service_list.iterator(); iter.hasNext(); ){
                temp_service=(objects_h.service) iter.next();
				if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.TRUE)
					System.out.printf("<option value='%s'>%s;%s\n",temp_service.description,temp_service.host_name,temp_service.description);
		                }

			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td></td><td class='reportSelectItem'>\n");
			System.out.printf("<input type='submit' value='Continue to Step 3'>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("</form>\n");
			System.out.printf("</TABLE>\n");

			System.out.printf("</DIV></P>\n");
		        }

		/* ask the user for report range and options */
		else if(input_type==GET_INPUT_OPTIONS){

			current_time = utils.currentTimeInSeconds();
			t.setTimeInMillis( current_time * 1000 );

			start_day=1;
			start_year=t.get( Calendar.YEAR );
			end_day=t.get( Calendar.DAY_OF_MONTH );
			end_year=t.get( Calendar.YEAR );

			System.out.printf("<P><DIV ALIGN=CENTER>\n");
			System.out.printf("<DIV CLASS='reportSelectTitle'>Step 3: Select Report Options</DIV>\n");
			System.out.printf("</DIV></P>\n");

			System.out.printf("<P><DIV ALIGN=CENTER>\n");

			System.out.printf("<TABLE BORDER=0 cellpadding=5>\n");
			System.out.printf("<form method=\"GET\" action=\"%s\">\n",cgiutils_h.HISTOGRAM_CGI);
			System.out.printf("<input type='hidden' name='host' value='%s'>\n",host_name);
			if(display_type==DISPLAY_SERVICE_HISTOGRAM)
				System.out.printf("<input type='hidden' name='service' value='%s'>\n",svc_description);

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Report Period:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
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
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td valign=top calss='reportSelectSubTitle'>If Custom Report Period...</td></tr>\n");

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

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Statistics Breakdown:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='breakdown'>\n");
			System.out.printf("<option value=monthly>Month\n");
			System.out.printf("<option value=dayofmonth SELECTED>Day of the Month\n");
			System.out.printf("<option value=dayofweek>Day of the Week\n");
			System.out.printf("<option value=hourly>Hour of the Day\n");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Events To Graph:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='graphevents'>\n");
			if(display_type==DISPLAY_HOST_HISTOGRAM){
				System.out.printf("<option value=%d %s>All host events\n",GRAPH_HOST_ALL,(graph_events==GRAPH_HOST_ALL)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host problem events\n",GRAPH_HOST_PROBLEMS,(graph_events==GRAPH_HOST_PROBLEMS)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host up events\n",GRAPH_HOST_UP,(graph_events==GRAPH_HOST_UP)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host down events\n",GRAPH_HOST_DOWN,(graph_events==GRAPH_HOST_DOWN)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Host unreachable events\n",GRAPH_HOST_UNREACHABLE,(graph_events==GRAPH_HOST_UNREACHABLE)?"SELECTED":"");
			        }
			else{
				System.out.printf("<option value=%d %s>All service events\n",GRAPH_SERVICE_ALL,(graph_events==GRAPH_SERVICE_ALL)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service problem events\n",GRAPH_SERVICE_PROBLEMS,(graph_events==GRAPH_SERVICE_PROBLEMS)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service ok events\n",GRAPH_SERVICE_OK,(graph_events==GRAPH_SERVICE_OK)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service warning events\n",GRAPH_SERVICE_WARNING,(graph_events==GRAPH_SERVICE_WARNING)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service unknown events\n",GRAPH_SERVICE_UNKNOWN,(graph_events==GRAPH_SERVICE_UNKNOWN)?"SELECTED":"");
				System.out.printf("<option value=%d %s>Service critical events\n",GRAPH_SERVICE_CRITICAL,(graph_events==GRAPH_SERVICE_CRITICAL)?"SELECTED":"");
			        }
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>State Types To Graph:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='graphstatetypes'>\n");
			System.out.printf("<option value=%d>Hard states\n",GRAPH_HARD_STATETYPES);
			System.out.printf("<option value=%d>Soft states\n",GRAPH_SOFT_STATETYPES);
			System.out.printf("<option value=%d SELECTED>Hard and soft states\n",GRAPH_ALL_STATETYPES);
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Assume State Retention:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='assumestateretention'>\n");
			System.out.printf("<option value='yes'>Yes\n");
			System.out.printf("<option value='no'>No\n");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Initial States Logged:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='initialstateslogged'>\n");
			System.out.printf("<option value='yes'>Yes\n");
			System.out.printf("<option value='no' SELECTED>No\n");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Ignore Repeated States:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='newstatesonly'>\n");
			System.out.printf("<option value='yes'>Yes\n");
			System.out.printf("<option value='no' SELECTED>No\n");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td></td><td class='reportSelectItem'><input type='submit' value='Create Report'></td></tr>\n");

			System.out.printf("</form>\n");
			System.out.printf("</TABLE>\n");

			System.out.printf("</DIV></P>\n");
		        }

		/* as the user whether they want a graph for a host or service */
		else{
			System.out.printf("<P><DIV ALIGN=CENTER>\n");
			System.out.printf("<DIV CLASS='reportSelectTitle'>Step 1: Select Report Type</DIV>\n");
			System.out.printf("</DIV></P>\n");

			System.out.printf("<P><DIV ALIGN=CENTER>\n");

			System.out.printf("<TABLE BORDER=0 cellpadding=5>\n");
			System.out.printf("<form method=\"GET\" action=\"%s\">\n",cgiutils_h.HISTOGRAM_CGI);

			System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Type:</td>\n");
			System.out.printf("<td class='reportSelectItem'>\n");
			System.out.printf("<select name='input'>\n");
			System.out.printf("<option value=gethost>Host\n");
			System.out.printf("<option value=getservice>Service\n");
			System.out.printf("</select>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("<tr><td></td><td class='reportSelectItem'>\n");
			System.out.printf("<input type='submit' value='Continue to Step 2'>\n");
			System.out.printf("</td></tr>\n");

			System.out.printf("</form>\n");
			System.out.printf("</TABLE>\n");

			System.out.printf("</DIV></P>\n");
		        }
		
                }

	document_footer();

	cgiutils.exit(  common_h.OK );
        }


public static void document_header(int use_stylesheet){
	String date_time ; // MAX_DATETIME_LENGTH

	if(mode==CREATE_HTML){
        if ( response != null ) {
           response.setHeader( "Cache-Control",  "no-store" );
           response.setHeader( "Pragma",  "no-cache" );
           response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
           response.setDateHeader( "Expires", System.currentTimeMillis() );
           response.setContentType("text/html");
        } else {
      		System.out.printf("Cache-Control: no-store\r\n");
      		System.out.printf("Pragma: no-cache\r\n");
      
      		date_time = cgiutils.get_time_string( 0,common_h.HTTP_DATE_TIME);
      		System.out.printf("Last-Modified: %s\r\n",date_time);
      		
      		date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      		System.out.printf("Expires: %s\r\n",date_time);
      		
      		System.out.printf("Content-type: text/html\n\n");
        }
        
		if(embedded==common_h.TRUE)
			return;

		System.out.printf("<html>\n");
		System.out.printf("<head>\n");
		System.out.printf("<title>\n");
		System.out.printf("Blue Histogram\n");
		System.out.printf("</title>\n");

		if(use_stylesheet==common_h.TRUE){
			System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
			System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.HISTOGRAM_CSS);
		        }
	
		System.out.printf("</head>\n");

		System.out.printf("<BODY CLASS='histogram'>\n");

		/* include user SSI header */
        cgiutils.include_ssi_files(cgiutils_h.HISTOGRAM_CGI,cgiutils_h.SSI_HEADER);

		System.out.printf("<div id=\"popup\" style=\"position:absolute; z-index:1; visibility: hidden\"></div>\n");
	        }

	else{
        if ( response != null ) {
           response.setHeader( "Cache-Control",  "no-store" );
           response.setHeader( "Pragma",  "no-cache" );
           response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
           response.setDateHeader( "Expires", System.currentTimeMillis() );
           response.setContentType("image/png");
        } else {

           System.out.printf("Cache-Control: no-store\r\n");
      		System.out.printf("Pragma: no-cache\r\n");
      
      		date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      		System.out.printf("Last-Modified: %s\r\n",date_time);
      		
      		date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      		System.out.printf("Expires: %s\r\n",date_time);
      
      		System.out.printf("Content-Type: image/png\r\n\r\n");
        }
        }

	return;
        }



public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	if(mode==CREATE_HTML){

		/* include user SSI footer */
        cgiutils.include_ssi_files(cgiutils_h.HISTOGRAM_CGI,cgiutils_h.SSI_FOOTER);

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

		/* we found the host argument */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				host_name = variables[x];

			display_type=DISPLAY_HOST_HISTOGRAM;
		        }

		/* we found the node width argument */
		else if(variables[x].equals("service")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				svc_description = variables[x];

			display_type=DISPLAY_SERVICE_HISTOGRAM;
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
		        }

		/* we found the image creation option */
		else if(variables[x].equals("createimage")){
			mode=CREATE_IMAGE;
		        }

		/* we found the backtrack archives argument */
		else if(variables[x].equals("backtrack")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			backtrack_archives=atoi(variables[x]);
			if(backtrack_archives<0)
				backtrack_archives=0;
			if(backtrack_archives>MAX_ARCHIVE_BACKTRACKS)
				backtrack_archives=MAX_ARCHIVE_BACKTRACKS;
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
				timeperiod_type=TIMEPERIOD_TODAY;

	
			if(timeperiod_type!=TIMEPERIOD_CUSTOM)
				convert_timeperiod_to_times(timeperiod_type);
		        }

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

		/* we found the embed option */
		else if(variables[x].equals("embedded"))
			embedded=common_h.TRUE;

		/* we found the noheader option */
		else if(variables[x].equals("noheader"))
			display_header=common_h.FALSE;

		/* we found the input option */
		else if(variables[x].equals("input")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("gethost"))
				input_type=GET_INPUT_HOST_TARGET;
			else if(variables[x].equals("getservice"))
				input_type=GET_INPUT_SERVICE_TARGET;
			else if(variables[x].equals("getoptions"))
				input_type=GET_INPUT_OPTIONS;
			else
				input_type=GET_INPUT_TARGET_TYPE;
		        }

		/* we found the graph states option */
		else if(variables[x].equals("graphevents")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			graph_events=atoi(variables[x]);
		        }

		/* we found the graph state types option */
		else if(variables[x].equals("graphstatetypes")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			graph_statetypes=atoi(variables[x]);
		        }

		/* we found the breakdown option */
		else if(variables[x].equals("breakdown")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("monthly"))
				breakdown_type=BREAKDOWN_MONTHLY;
			else if(variables[x].equals("dayofmonth"))
				breakdown_type=BREAKDOWN_DAY_OF_MONTH;
			else if(variables[x].equals("dayofweek"))
				breakdown_type=BREAKDOWN_DAY_OF_WEEK;
			else
				breakdown_type=BREAKDOWN_HOURLY;
		        }

		/* we found the assume state retention option */
		else if(variables[x].equals("assumestateretention")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				assume_state_retention=common_h.TRUE;
			else
				assume_state_retention=common_h.FALSE;
		        }

		/* we found the initial states logged option */
		else if(variables[x].equals("initialstateslogged")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				initial_states_logged=common_h.TRUE;
			else
				initial_states_logged=common_h.FALSE;

		        }

		/* we found the new states only option */
		else if(variables[x].equals("newstatesonly")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				new_states_only=common_h.TRUE;
			else
				new_states_only=common_h.FALSE;

		        }
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



/* graphs histogram data */
public static void graph_all_histogram_data(){
	int pixel_x;
	int pixel_y;
	int last_pixel_y;
	int current_bucket;
	int actual_bucket;
	long max_value;
	double x_scaling_factor;
	double y_scaling_factor;
	double x_units;
	double y_units;
	int current_unit;
	int actual_unit;
	String temp_buffer ; // MAX_INPUT_BUFFER
	int string_width;
	int string_height;
	String[] days = new String[] {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
	String[] months= new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"};
	String start_time ; // MAX_INPUT_BUFFER
	String end_time ; // MAX_INPUT_BUFFER

	long state1_max=0L;
	long state1_min=0L;
	int have_state1_min=common_h.FALSE;
	long state1_sum=0L;
	double state1_avg=0.0;
	long state2_max=0L;
	long state2_min=0L;
	int have_state2_min=common_h.FALSE;
	long state2_sum=0L;
	double state2_avg=0.0;
	long state3_max=0L;
	long state3_min=0L;
	int have_state3_min=common_h.FALSE;
	long state3_sum=0L;
	double state3_avg=0.0;
	long state4_max=0L;
	long state4_min=0L;
	int have_state4_min=common_h.FALSE;
	long state4_sum=0L;
	double state4_avg=0.0;


//#ifdef DEBUG
//	System.out.printf("Total Buckets: %d\n",total_buckets);
//#endif

	/* determine max value in the buckets (for scaling) */
	max_value=0L;
	for(current_bucket=0;current_bucket<total_buckets;current_bucket++){
		if(tsdata[current_bucket].service_ok>max_value)
			max_value=tsdata[current_bucket].service_ok;
		if(tsdata[current_bucket].service_warning>max_value)
			max_value=tsdata[current_bucket].service_warning;
		if(tsdata[current_bucket].service_unknown>max_value)
			max_value=tsdata[current_bucket].service_unknown;
		if(tsdata[current_bucket].service_critical>max_value)
			max_value=tsdata[current_bucket].service_critical;
		if(tsdata[current_bucket].host_up>max_value)
			max_value=tsdata[current_bucket].host_up;
		if(tsdata[current_bucket].host_down>max_value)
			max_value=tsdata[current_bucket].host_down;
		if(tsdata[current_bucket].host_unreachable>max_value)
			max_value=tsdata[current_bucket].host_unreachable;
	        }

//#ifdef DEBUG
//	System.out.printf("Done determining max bucket values\n");
//	System.out.printf("MAX_VALUE=%d\n",max_value);
//	System.out.printf("DRAWING_HEIGHT=%d\n",DRAWING_HEIGHT);
//#endif

	/* min number of values to graph */
	if(max_value<10)
		max_value=10;
//#ifdef DEBUG
//	System.out.printf("ADJUSTED MAX_VALUE=%d\n",max_value);
//#endif

	/* determine y scaling factor */
	/*y_scaling_factor=floor(DRAWING_HEIGHT/max_value);*/
	y_scaling_factor=(DRAWING_HEIGHT/max_value);

	/* determine x scaling factor */
	x_scaling_factor=(DRAWING_WIDTH/total_buckets);

	/* determine y units resolution - we want a max of about 10 y grid lines */
	/*
	y_units=(DRAWING_HEIGHT/19.0);
	y_units=ceil(y_units/y_scaling_factor)*y_scaling_factor;
	*/
	y_units=Math.ceil(19.0/y_scaling_factor);

	/* determine x units resolution */
	if(breakdown_type==BREAKDOWN_HOURLY)
		x_units=(DRAWING_WIDTH/(total_buckets/4.0));
	else
		x_units=x_scaling_factor;

//#ifdef DEBUG
//	System.out.printf("DRAWING_WIDTH: %d\n",DRAWING_WIDTH);
//	System.out.printf("DRAWING_HEIGHT: %d\n",DRAWING_HEIGHT);
//	System.out.printf("max_value: %d\n",max_value);
//	System.out.printf("x_scaling_factor: %.3f\n",x_scaling_factor);
//	System.out.printf("y_scaling_factor: %.3f\n",y_scaling_factor);
//	System.out.printf("x_units: %.3f\n",x_units);
//	System.out.printf("y_units: %.3f\n",y_units);
//	System.out.printf("y units to draw: %.3f\n",(max_value/y_units));
//#endif

	string_height= gd.getFontMetrics().getHeight();

//#ifdef DEBUG
//	System.out.printf("Starting to draw Y grid lines...\n");
//#endif

	/* draw y grid lines */
	if(max_value>0){
		for(current_unit=1;(current_unit*y_units*y_scaling_factor)<=DRAWING_HEIGHT;current_unit++){
			draw_dashed_line(DRAWING_X_OFFSET,DRAWING_Y_OFFSET-(int) (current_unit*y_units*y_scaling_factor),DRAWING_X_OFFSET+DRAWING_WIDTH,DRAWING_Y_OFFSET-(int) (current_unit*y_units*y_scaling_factor),color_lightgray);
//#ifdef DEBUG
//			System.out.printf("  Drawing Y unit #%d @ %d\n",current_unit,(int)(current_unit*y_units*y_scaling_factor));
//#endif
		        }
	        }

//#ifdef DEBUG
//	System.out.printf("Starting to draw X grid lines...\n");
//#endif

	/* draw x grid lines */
	for(current_unit=1;(int)(current_unit*x_units)<=DRAWING_WIDTH;current_unit++)
		draw_dashed_line(DRAWING_X_OFFSET+(int)(current_unit*x_units),DRAWING_Y_OFFSET,DRAWING_X_OFFSET+(int)(current_unit*x_units),DRAWING_Y_OFFSET-DRAWING_HEIGHT,color_lightgray);

//#ifdef DEBUG
//	System.out.printf("Starting to draw grid units...\n");
//#endif

	/* draw y units */
	if(max_value>0){
		for(current_unit=0;(current_unit*y_units*y_scaling_factor)<=DRAWING_HEIGHT;current_unit++){
			temp_buffer = String.format( "%d",(int)(current_unit*y_units));

			string_width=gd.getFontMetrics().stringWidth(temp_buffer);
			gd.drawString( temp_buffer, DRAWING_X_OFFSET-string_width-5,(int) (DRAWING_Y_OFFSET-(current_unit*y_units*y_scaling_factor)-(string_height/2)) );
	                }
	        }

	/* draw x units */
	for(current_unit=0,actual_unit=0;(int)(current_unit*x_units)<=DRAWING_WIDTH;current_unit++,actual_unit++){

		if(actual_unit>=total_buckets)
			actual_unit=0;

		if(breakdown_type==BREAKDOWN_MONTHLY)
			temp_buffer = months[actual_unit];
		else if(breakdown_type==BREAKDOWN_DAY_OF_MONTH)
			temp_buffer = String.format( "%d",actual_unit+1);
		else if(breakdown_type==BREAKDOWN_DAY_OF_WEEK)
			temp_buffer = days[actual_unit];
		else
            temp_buffer = String.format( "%02d:00",(actual_unit==24)?0:actual_unit);

        string_width=gd.getFontMetrics().stringWidth(temp_buffer);
		drawRotatedText(temp_buffer,(int)( DRAWING_X_OFFSET+(current_unit*x_units)-(string_height/2)),DRAWING_Y_OFFSET+5+string_width );
	        }

	/* draw y unit measure */
	temp_buffer = "Number of Events";
    string_width=gd.getFontMetrics().stringWidth(temp_buffer);
    drawRotatedText(temp_buffer,0,DRAWING_Y_OFFSET-(DRAWING_HEIGHT/2)+(string_width/2) );

	/* draw x unit measure */
	if(breakdown_type==BREAKDOWN_MONTHLY)
        temp_buffer = String.format( "Month");
	else if(breakdown_type==BREAKDOWN_DAY_OF_MONTH)
        temp_buffer = String.format( "Day of the Month");
	else if(breakdown_type==BREAKDOWN_DAY_OF_WEEK)
        temp_buffer = String.format( "Day of the Week");
	else
		temp_buffer = "Hour of the Day (15 minute increments)";
    string_width=gd.getFontMetrics().stringWidth(temp_buffer);
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+(DRAWING_WIDTH/2)-(string_width/2),DRAWING_Y_OFFSET+70 );

	/* draw title */
	start_time = new Date(t1*1000).toLocaleString();
	end_time = new Date(t2*1000).toLocaleString();

	if(display_type==DISPLAY_HOST_HISTOGRAM)
        temp_buffer = String.format( "Event History For Host '%s'",host_name);
	else
		temp_buffer = String.format( "Event History For Service '%s' On Host '%s'",svc_description,host_name);
    string_width=gd.getFontMetrics().stringWidth(temp_buffer);
    gd.drawString( temp_buffer, DRAWING_X_OFFSET+(DRAWING_WIDTH/2)-(string_width/2),0 );

	temp_buffer = String.format( "%s to %s",start_time,end_time);
    string_width=gd.getFontMetrics().stringWidth(temp_buffer);
    gd.drawString( temp_buffer, DRAWING_X_OFFSET+(DRAWING_WIDTH/2)-(string_width/2),string_height+5 );


//#ifdef DEBUG
//	System.out.printf("About to starting graphing (total_buckets=%d)...\n",total_buckets);
//#endif
	

	/* graph service states */
	if(display_type==DISPLAY_HOST_HISTOGRAM){

		/* graph host recoveries */
		if(0!=(graph_events & GRAPH_HOST_UP)){

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){

				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
			
				pixel_y=(int)(tsdata[actual_bucket].host_up*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_green);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state1_min==common_h.FALSE || tsdata[actual_bucket].host_up<state1_min){
						state1_min=tsdata[actual_bucket].host_up;
						have_state1_min=common_h.TRUE;
				                }
					if(state1_max==0 || tsdata[actual_bucket].host_up>state1_max)
						state1_max=tsdata[actual_bucket].host_up;
					state1_sum+=tsdata[actual_bucket].host_up;
				        }
			        }
	                }

//#ifdef DEBUG
//		System.out.printf("Done graphing HOST UP states...\n");
//#endif

		/* graph host down states */
		if(0 != (graph_events & GRAPH_HOST_DOWN)){

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){

				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
			
				pixel_y=(int)(tsdata[actual_bucket].host_down*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_red);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state2_min==common_h.FALSE || tsdata[actual_bucket].host_down<state2_min){
						state2_min=tsdata[actual_bucket].host_down;
						have_state2_min=common_h.TRUE;
				                }
					if(state2_max==0 || tsdata[actual_bucket].host_down>state2_max)
						state2_max=tsdata[actual_bucket].host_down;
					state2_sum+=tsdata[actual_bucket].host_down;
				        }
			        }
	                }

//#ifdef DEBUG
//		System.out.printf("Done graphing HOST DOWN states...\n");
//#endif

		/* graph host unreachable states */
		if(0 != (graph_events & GRAPH_HOST_UNREACHABLE)) {

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){

				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
			
				pixel_y=(int)(tsdata[actual_bucket].host_unreachable*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_darkred);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state3_min==common_h.FALSE || tsdata[actual_bucket].host_unreachable<state3_min){
						state3_min=tsdata[actual_bucket].host_unreachable;
						have_state3_min=common_h.TRUE;
				                }
					if(state3_max==0 || tsdata[actual_bucket].host_unreachable>state3_max)
						state3_max=tsdata[actual_bucket].host_unreachable;
					state3_sum+=tsdata[actual_bucket].host_unreachable;
				        }
			        }
	                }

//#ifdef DEBUG
//		System.out.printf("Done graphing HOST UNREACHABLE states...\n");
//#endif

	        }

	/* graph service states */
	else{

		/* graph service recoveries */
		if(0 != (graph_events & GRAPH_SERVICE_OK)){

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){

				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
			
				pixel_y=(int)(tsdata[actual_bucket].service_ok*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_green);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state1_min==common_h.FALSE || tsdata[actual_bucket].service_ok<state1_min){
						state1_min=tsdata[actual_bucket].service_ok;
						have_state1_min=common_h.TRUE;
				                }
					if(state1_max==0 || tsdata[actual_bucket].service_ok>state1_max)
						state1_max=tsdata[actual_bucket].service_ok;
					state1_sum+=tsdata[actual_bucket].service_ok;
				        }
			        }
	                }

		/* graph service warning states */
		if(0 != (graph_events & GRAPH_SERVICE_WARNING) ){

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){

				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
				
				pixel_y=(int)(tsdata[actual_bucket].service_warning*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_yellow);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state2_min==common_h.FALSE || tsdata[actual_bucket].service_warning<state2_min){
						state2_min=tsdata[actual_bucket].service_warning;
						have_state2_min=common_h.TRUE;
				                }
					if(state2_max==0 || tsdata[actual_bucket].service_warning>state2_max)
						state2_max=tsdata[actual_bucket].service_warning;
					state2_sum+=tsdata[actual_bucket].service_warning;
				        }
			        }
	                }

		/* graph service unknown states */
		if( 0 != (graph_events & GRAPH_SERVICE_UNKNOWN) ){

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){
				
				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
			
				pixel_y=(int)(tsdata[actual_bucket].service_unknown*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_orange);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state3_min==common_h.FALSE || tsdata[actual_bucket].service_unknown<state3_min){
						state3_min=tsdata[actual_bucket].service_unknown;
						have_state3_min=common_h.TRUE;
				                }
					if(state3_max==0 || tsdata[actual_bucket].service_unknown>state3_max)
						state3_max=tsdata[actual_bucket].service_unknown;
					state3_sum+=tsdata[actual_bucket].service_unknown;
				        }
			        }
	                }

		/* graph service critical states */
		if(0 != (graph_events & GRAPH_SERVICE_CRITICAL) ){

			last_pixel_y=0;
			for(current_bucket=0,actual_bucket=0;current_bucket<=total_buckets;current_bucket++,actual_bucket++){

				if(actual_bucket>=total_buckets)
					actual_bucket=0;

				pixel_x=(int)(current_bucket*x_scaling_factor);
			
				pixel_y=(int)(tsdata[actual_bucket].service_critical*y_scaling_factor);

				if(current_bucket>0 && !(last_pixel_y==0 && pixel_y==0))
					draw_line(DRAWING_X_OFFSET+pixel_x-(int)x_scaling_factor,DRAWING_Y_OFFSET-last_pixel_y,DRAWING_X_OFFSET+pixel_x,DRAWING_Y_OFFSET-pixel_y,color_red);

				last_pixel_y=pixel_y;

				if(current_bucket<total_buckets){
					if(have_state4_min==common_h.FALSE || tsdata[actual_bucket].service_critical<state4_min){
						state4_min=tsdata[actual_bucket].service_critical;
						have_state4_min=common_h.TRUE;
				                }
					if(state4_max==0 || tsdata[actual_bucket].service_critical>state4_max)
						state4_max=tsdata[actual_bucket].service_critical;
					state4_sum+=tsdata[actual_bucket].service_critical;
				        }
			        }
	                }
	        }

//#ifdef DEBUG
//	System.out.printf("Done graphing states...\n");
//#endif

	/* draw graph boundaries */
	draw_line(DRAWING_X_OFFSET,DRAWING_Y_OFFSET,DRAWING_X_OFFSET,DRAWING_Y_OFFSET-DRAWING_HEIGHT,color_black);
	draw_line(DRAWING_X_OFFSET+DRAWING_WIDTH,DRAWING_Y_OFFSET,DRAWING_X_OFFSET+DRAWING_WIDTH,DRAWING_Y_OFFSET-DRAWING_HEIGHT,color_black);
	draw_line(DRAWING_X_OFFSET,DRAWING_Y_OFFSET,DRAWING_X_OFFSET+DRAWING_WIDTH,DRAWING_Y_OFFSET,color_black);


    Color temp_color = gd.getColor();
    
	/* graph stats */
    FontMetrics metrics = gd.getFontMetrics();
	temp_buffer = String.format( "EVENT TYPE");
	string_width= metrics.stringWidth(temp_buffer);
    gd.setColor( color_black );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+15,DRAWING_Y_OFFSET-DRAWING_HEIGHT);

	temp_buffer = String.format( "  MIN   MAX   SUM   AVG");
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( color_black );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+115,DRAWING_Y_OFFSET-DRAWING_HEIGHT);

	draw_line(DRAWING_X_OFFSET+DRAWING_WIDTH+15,DRAWING_Y_OFFSET-DRAWING_HEIGHT+string_height+2,DRAWING_X_OFFSET+DRAWING_WIDTH+275,DRAWING_Y_OFFSET-DRAWING_HEIGHT+string_height+2,color_black);

	temp_buffer = String.format( "Recovery (%s):",(display_type==DISPLAY_SERVICE_HISTOGRAM)?"Ok":"Up");
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( color_green );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+15,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*1));

	state1_avg=(state1_sum/total_buckets);
	temp_buffer = String.format( "%5lu %5lu %5lu   %.2f",state1_min,state1_max,state1_sum,state1_avg);
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( color_black );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+115,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*1));

	temp_buffer = String.format( "%s:",(display_type==DISPLAY_SERVICE_HISTOGRAM)?"Warning":"Down");
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( (display_type==DISPLAY_SERVICE_HISTOGRAM)?color_yellow:color_red );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+15,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*2));

	state2_avg=(state2_sum/total_buckets);
	temp_buffer = String.format( "%5lu %5lu %5lu   %.2f",state2_min,state2_max,state2_sum,state2_avg);
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( color_black );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+115,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*2));

	temp_buffer = String.format( "%s:",(display_type==DISPLAY_SERVICE_HISTOGRAM)?"Unknown":"Unreachable");
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( (display_type==DISPLAY_SERVICE_HISTOGRAM)?color_orange:color_darkred );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+15,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*3));

	state3_avg=(state3_sum/total_buckets);
	temp_buffer = String.format( "%5lu %5lu %5lu   %.2f",state3_min,state3_max,state3_sum,state3_avg);
	string_width=metrics.stringWidth(temp_buffer);
    gd.setColor( color_black );
	gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+115,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*3));

	if(display_type==DISPLAY_SERVICE_HISTOGRAM){

		temp_buffer = String.format( "Critical:");
		string_width=metrics.stringWidth(temp_buffer);
        gd.setColor( color_red );
		gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+15,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*4));

		state4_avg=(state4_sum/total_buckets);
		temp_buffer = String.format( "%5lu %5lu %5lu   %.2f",state4_min,state4_max,state4_sum,state4_avg);
		string_width=metrics.stringWidth(temp_buffer);
        gd.setColor( color_black );
		gd.drawString( temp_buffer, DRAWING_X_OFFSET+DRAWING_WIDTH+115,DRAWING_Y_OFFSET-DRAWING_HEIGHT+((string_height+5)*4));
	        }

    gd.setColor ( temp_color );
	return;
        }


/* adds an archived state entry */
public static void add_archived_state(int state_type, long time_stamp){
	Calendar our_time = Calendar.getInstance();
	int bucket;
	int skip_state=common_h.FALSE;
//
//#ifdef DEBUG2
//	System.out.printf("NEW ENTRY: last=%d this=%d\n",last_state,state_type);
//#endif

	/* don't record program starts/stops, just make a note that one occurred */
	if(state_type==AS_PROGRAM_START || state_type==AS_PROGRAM_END){
//#ifdef DEBUG2
//		System.out.printf("Recording a program start: %d\n",state_type);
//#endif
		program_restart_has_occurred=common_h.TRUE;
		return;
	        }

	/* see if we should even take into account this event */
	if(program_restart_has_occurred==common_h.TRUE){

//#ifdef DEBUG2
//		System.out.printf("program_restart_has_occurred: last=%d this=%d\n",last_state,state_type);
//#endif
		
		if(initial_states_logged==common_h.TRUE){
			if(state_type==AS_SVC_OK && last_state==AS_SVC_OK)
				skip_state=common_h.TRUE;
			if(state_type==AS_HOST_UP && last_state==AS_HOST_UP)
				skip_state=common_h.TRUE;
		        }
			
		if(assume_state_retention==common_h.TRUE && initial_states_logged==common_h.TRUE){
			if(state_type==AS_SVC_WARNING && last_state==AS_SVC_WARNING)
				skip_state=common_h.TRUE;
			if(state_type==AS_SVC_UNKNOWN && last_state==AS_SVC_UNKNOWN)
				skip_state=common_h.TRUE;
			if(state_type==AS_SVC_CRITICAL && last_state==AS_SVC_CRITICAL)
				skip_state=common_h.TRUE;
			if(state_type==AS_HOST_DOWN && last_state==AS_HOST_DOWN)
				skip_state=common_h.TRUE;
			if(state_type==AS_HOST_UNREACHABLE && last_state==AS_HOST_UNREACHABLE)
				skip_state=common_h.TRUE;
		        }

		if(skip_state==common_h.TRUE){
			program_restart_has_occurred=common_h.FALSE;
//#ifdef DEBUG2
//			System.out.printf("Skipping state...\n");
//#endif
			return;
		        }
	        }

	/* reset program restart variable */
	program_restart_has_occurred=common_h.FALSE;

	/* are we only processing new states */
	if(new_states_only==common_h.TRUE && state_type==last_state){
//#ifdef DEBUG2
//		System.out.printf("Skipping state (not a new state)...\n");
//#endif
		return;
	        }

//#ifdef DEBUG2
//	System.out.printf("GOODSTATE: %d @ %d\n",state_type,time_stamp);
//#endif
		


	our_time.setTimeInMillis(time_stamp * 1000);

	/* calculate the correct bucket to dump the data into */
	if(breakdown_type==BREAKDOWN_MONTHLY)
		bucket=our_time.get(Calendar.MONTH);

	else if(breakdown_type==BREAKDOWN_DAY_OF_MONTH)
		bucket=our_time.get( Calendar.DAY_OF_MONTH ) ;

	else if(breakdown_type==BREAKDOWN_DAY_OF_WEEK)
		bucket=our_time.get( Calendar.DAY_OF_WEEK )-1;

	else
		bucket=(our_time.get( Calendar.HOUR )*4)+(our_time.get( Calendar.MINUTE )/15);

//#ifdef DEBUG2
//	System.out.printf("\tBucket=%d\n",bucket);
//#endif	

	/* save the data in the correct bucket */
	if(state_type==AS_SVC_OK)
		tsdata[bucket].service_ok++;
	else if(state_type==AS_SVC_UNKNOWN)
		tsdata[bucket].service_unknown++;
	else if(state_type==AS_SVC_WARNING)
		tsdata[bucket].service_warning++;
	else if(state_type==AS_SVC_CRITICAL)
		tsdata[bucket].service_critical++;
	else if(state_type==AS_HOST_UP)
		tsdata[bucket].host_up++;
	else if(state_type==AS_HOST_DOWN)
		tsdata[bucket].host_down++;
	else if(state_type==AS_HOST_UNREACHABLE)
		tsdata[bucket].host_unreachable++;

	/* record last state type */
	last_state=state_type;

	return;
        }



/* reads log files for archived state data */
public static void read_archived_state_data(){
	String filename ; // MAX_FILENAME_LENGTH
	int newest_archive=0;
	int oldest_archive=0;
	int current_archive;

//#ifdef DEBUG2
//	System.out.printf("Determining archives to use...\n");
//#endif

	/* determine earliest archive to use */
	oldest_archive= cgiutils.determine_archive_to_use_from_time(t1);
	if( cgiutils.log_rotation_method!= common_h.LOG_ROTATION_NONE)
		oldest_archive+=backtrack_archives;

	/* determine most recent archive to use */
	newest_archive= cgiutils.determine_archive_to_use_from_time(t2);

	if(oldest_archive<newest_archive)
		oldest_archive=newest_archive;

//#ifdef DEBUG2
//	System.out.printf("Oldest archive: %d\n",oldest_archive);
//	System.out.printf("Newest archive: %d\n",newest_archive);
//#endif

	/* read in all the necessary archived logs */
	for(current_archive=newest_archive;current_archive<=oldest_archive;current_archive++){

		/* get the name of the log file that contains this archive */
        filename = cgiutils.get_log_archive_to_use(current_archive );

//#ifdef DEBUG2
//		System.out.printf("\tCurrent archive: %d (%s)\n",current_archive,filename);
//#endif

		/* scan the log file for archived state data */
		scan_log_file_for_archived_state_data(filename);
	        }

	return;
        }



/* grabs archives state data from a log file */
public static void scan_log_file_for_archived_state_data(String filename){
	String input=null;
	String entry_host_name ; // MAX_INPUT_BUFFER
	String entry_svc_description ; // MAX_INPUT_BUFFER
	String temp_buffer;
	long time_stamp;
	cgiutils_h.mmapfile thefile;

	/* print something so browser doesn't time out */
	if(mode==CREATE_HTML){
		System.out.printf(" ");
		System.out.flush();
	        }

	if((thefile=cgiutils.mmap_fopen(filename))==null){
//#ifdef DEBUG2
//		System.out.printf("Could not open file '%s' for reading.\n",filename);
//#endif
		return;
	        }

//#ifdef DEBUG2
//	System.out.printf("Scanning log file '%s' for archived state data...\n",filename);
//#endif

	while(true){

		/* free memory */

		/* read the next line */
		if((input=cgiutils.mmap_fgets(thefile))==null)
			break;

		input = input.trim();

        String[] split = input.split( "[\\]]" , 2 );
        time_stamp=( split[0].trim().length() <= 1 )?0: strtoul( split[0].substring(1),null,10);

		/* program starts/restarts */
		if(input.contains(" starting..."))
			add_archived_state(AS_PROGRAM_START,time_stamp);
		if(input.contains(" restarting..."))
			add_archived_state(AS_PROGRAM_START,time_stamp);

		/* program stops */
		if(input.contains(" shutting down..."))
			add_archived_state(AS_PROGRAM_END,time_stamp);
		if(input.contains("Bailing out"))
			add_archived_state(AS_PROGRAM_END,time_stamp);

		if(display_type==DISPLAY_HOST_HISTOGRAM){
			if(input.contains("HOST ALERT:")){

				/* get host name */
                temp_buffer = split ( split, "[:]" ); 
                temp_buffer = split ( split, "[;]" );
                entry_host_name = (temp_buffer==null)?"": temp_buffer.substring(1) ;

                if(!host_name.equals(entry_host_name))
                    continue;

				/* skip soft states if necessary */
				if((0==(graph_statetypes & GRAPH_SOFT_STATETYPES)) && input.contains(";SOFT;"))
					continue;

				/* skip hard states if necessary */
				if((0==(graph_statetypes & GRAPH_HARD_STATETYPES)) && input.contains(";HARD;"))
					continue;

				if(input.contains(";DOWN;"))
					add_archived_state(AS_HOST_DOWN,time_stamp);
				else if(input.contains(";UNREACHABLE;"))
					add_archived_state(AS_HOST_UNREACHABLE,time_stamp);
				else if(input.contains(";RECOVERY") || input.contains(";UP;"))
					add_archived_state(AS_HOST_UP,time_stamp);
			        }
		        }
		if(display_type==DISPLAY_SERVICE_HISTOGRAM){
			if(input.contains("SERVICE ALERT:")){

				/* get host name */
                temp_buffer = split ( split, "[:]" ); 
                temp_buffer = split ( split, "[;]" );
                entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);

                if(!host_name.equals(entry_host_name))
                    continue;
                
                /* get service description */
                temp_buffer = split ( split, "[;]" );
                entry_svc_description = (temp_buffer==null)?"":temp_buffer;

                if( !svc_description.equals(entry_svc_description))
                    continue;

				/* skip soft states if necessary */
				if((0==(graph_statetypes & GRAPH_SOFT_STATETYPES)) && input.contains(";SOFT;"))
					continue;

				/* skip hard states if necessary */
				if((0==(graph_statetypes & GRAPH_HARD_STATETYPES)) && input.contains(";HARD;"))
					continue;

				if(input.contains(";CRITICAL;"))
					add_archived_state(AS_SVC_CRITICAL,time_stamp);
				else if(input.contains(";WARNING;"))
					add_archived_state(AS_SVC_WARNING,time_stamp);
				else if(input.contains(";UNKNOWN;"))
					add_archived_state(AS_SVC_UNKNOWN,time_stamp);
				else if(input.contains(";RECOVERY;") || input.contains(";OK;"))
					add_archived_state(AS_SVC_OK,time_stamp);
			        }
		        }
		
	        }

	/* free memory and close the file */
	cgiutils.mmap_fclose(thefile);
	
        }
	



public static void convert_timeperiod_to_times(int type){
	long current_time;
	Calendar t = Calendar.getInstance();

	/* get the current time */
	current_time = utils.currentTimeInSeconds() ;

	t.setTimeInMillis(current_time * 1000);

	t.set( Calendar.SECOND,0 );
	t.set( Calendar.MINUTE, 0 );
	t.set( Calendar.HOUR , 0 );

	switch(type){
	case TIMEPERIOD_LAST24HOURS:
		t1=current_time-(60*60*24);
		t2=current_time;
		break;
	case TIMEPERIOD_TODAY:
		t1=utils.getTimeInSeconds(t);
		t2=current_time;
		break;
	case TIMEPERIOD_YESTERDAY:
		t1=(utils.getTimeInSeconds(t)-(60*60*24));
		t2=utils.getTimeInSeconds(t);
		break;
	case TIMEPERIOD_THISWEEK:
		t1=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK )-1)));
		t2=current_time;
		break;
	case TIMEPERIOD_LASTWEEK:
		t1=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK )-1))-(60*60*24*7));
		t2=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK )-1)));
		break;
	case TIMEPERIOD_THISMONTH:
		t.set( Calendar.DAY_OF_MONTH, 1 );
		t1=utils.getTimeInSeconds(t);
		t2=current_time;
		break;
	case TIMEPERIOD_LASTMONTH:
        t.set( Calendar.DAY_OF_MONTH, 1 );
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
		break;
	case TIMEPERIOD_LASTQUARTER:
		break;
	case TIMEPERIOD_THISYEAR:
		t.set( Calendar.MONTH, 0 );
        t.set( Calendar.DAY_OF_MONTH, 1 );
		t1=utils.getTimeInSeconds(t);
		t2=current_time;
		break;
	case TIMEPERIOD_LASTYEAR:
        t.set( Calendar.MONTH, 0 );
        t.set( Calendar.DAY_OF_MONTH, 1 );
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
   Calendar st = Calendar.getInstance();
   Calendar et= Calendar.getInstance();
   
   /* get the current time */
   /* get the current time */
   long current_time = utils.currentTimeInSeconds();
   
   st.setTimeInMillis( current_time * 1000 );
   
   st.set( Calendar.SECOND, start_second );
   st.set( Calendar.MINUTE, start_minute );
   st.set( Calendar.HOUR, start_hour );
   st.set( Calendar.DAY_OF_MONTH, start_day );
   st.set( Calendar.MONTH, start_month-1 );
   st.set( Calendar.YEAR, start_year);
   
   t1= utils.getTimeInSeconds(st);
   
   et.setTimeInMillis( current_time * 1000 );
   
   et.set( Calendar.SECOND, end_second );
   et.set( Calendar.MINUTE, end_minute );
   et.set( Calendar.HOUR, end_hour );
   et.set( Calendar.DAY_OF_MONTH, end_day );
   et.set( Calendar.MONTH, end_month-1 );
   et.set( Calendar.YEAR, end_year);
   
   t2= utils.getTimeInSeconds(et);
}



/* draws a solid line */
public static void draw_line(int x1,int y1,int x2,int y2,Color color){

    Color temp_color = gd.getColor();
    gd.setColor( color );

    /* draws a line (dashed) */
    gd.drawLine(x1, y1, x2, y2);
	
    gd.setColor(temp_color);
    
	}


/* draws a dashed line */
/* draws a dashed line */
public static void draw_dashed_line(int x1,int y1,int x2,int y2,Color color){
    
    float[] styleDashed = new float[] {2.0f,4.0f};

    /* sets current style to a dashed line */
    BasicStroke stroke = (BasicStroke) gd.getStroke();
    BasicStroke dashed = new BasicStroke( stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), styleDashed, 0.0f );
    gd.setStroke( dashed );

    /* draws a line (dashed) */
    Color temp_color = gd.getColor();
    gd.setColor(color);
    
    gd.drawLine(x1, y1, x2, y2);
    
    gd.setColor(temp_color);
    gd.setStroke( stroke );

    }

public static void drawRotatedText( String text, int x, int y ) { 

    //  Draw string rotated counter-clockwise 90 degrees.
    gd.rotate(-Math.PI/2);
  
//    gd.rotate(-Math.PI/2 );
    gd.drawString( text, -1*y, x );

    gd.rotate( Math.PI/2 );
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