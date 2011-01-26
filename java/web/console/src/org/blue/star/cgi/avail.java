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
import java.util.Iterator;
import java.util.ListIterator;

import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class avail extends blue_servlet {
   /* output types */
   public static final int HTML_OUTPUT            = 0;
   public static final int  CSV_OUTPUT              =1;
   
   
   /* archived state types */
   public static final int  AS_CURRENT_STATE        =-1;   /* special case for initial assumed state */
   public static final int  AS_NO_DATA		=0;
   public static final int  AS_PROGRAM_END		=1;
   public static final int  AS_PROGRAM_START	=2;
   public static final int  AS_HOST_UP		=3;
   public static final int  AS_HOST_DOWN		=4;
   public static final int  AS_HOST_UNREACHABLE	=5;
   public static final int  AS_SVC_OK		=6;
   public static final int  AS_SVC_UNKNOWN		=7;
   public static final int  AS_SVC_WARNING		=8;
   public static final int  AS_SVC_CRITICAL		=9;
   
   public static final int  AS_SVC_DOWNTIME_START   =10;
   public static final int  AS_SVC_DOWNTIME_END     =11;
   public static final int  AS_HOST_DOWNTIME_START  =12;
   public static final int  AS_HOST_DOWNTIME_END    =13;
   
   public static final int  AS_SOFT_STATE           =1;
   public static final int  AS_HARD_STATE           =2;
   
   
   /* display types */
   public static final int  DISPLAY_NO_AVAIL        =0;
   public static final int  DISPLAY_HOSTGROUP_AVAIL =1;
   public static final int  DISPLAY_HOST_AVAIL      =2;
   public static final int  DISPLAY_SERVICE_AVAIL   =3;
   public static final int  DISPLAY_SERVICEGROUP_AVAIL =4;
   
   /* subject types */
   public static final int  HOST_SUBJECT            =0;
   public static final int  SERVICE_SUBJECT         =1;
   
   
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
   public static final int  TIMEPERIOD_LAST31DAYS	=13;
   
   public static final int  MIN_TIMESTAMP_SPACING	=10;
   
   public static final int  MAX_ARCHIVE_SPREAD	=65;
   public static final int  MAX_ARCHIVE		=65;
   public static final int  MAX_ARCHIVE_BACKTRACKS	=60;
   
   public static cgiauth_h.authdata current_authdata = new cgiauth_h.authdata();
   
   public static class archived_state {
      public long  time_stamp;
      public int     entry_type;
      public int     state_type;
      public String state_info;
      int     processed_state;
      archived_state misc_ptr;
   };
   
   public static class avail_subject {
      public int type;
      public String host_name;
      public String service_description;
      public ArrayList<archived_state> as_list = new ArrayList<archived_state>();        /* archived state list */
      public archived_state as_list_tail;
      
      public ArrayList<archived_state> sd_list = new ArrayList<archived_state>();        /* scheduled downtime list */
      public int last_known_state;
      public long earliest_time;
      public long latest_time;
      public int earliest_state;
      public int latest_state;
      
      public long time_up;
      public long time_down;
      public long time_unreachable;
      public long time_ok;
      public long time_warning;
      public long time_unknown;
      public long time_critical;
      
      public long scheduled_time_up;
      public long scheduled_time_down;
      public long scheduled_time_unreachable;
      public long scheduled_time_ok;
      public long scheduled_time_warning;
      public long scheduled_time_unknown;
      public long scheduled_time_critical;
      public long scheduled_time_indeterminate;
      
      public long time_indeterminate_nodata;
      public long time_indeterminate_notrunning;
   }
   
   public static ArrayList<avail_subject> subject_list = new ArrayList<avail_subject>();
   
   public static long t1;
   public static long t2;
   
   public static int display_type=DISPLAY_NO_AVAIL;
   public static int timeperiod_type=TIMEPERIOD_LAST24HOURS;
   public static int show_log_entries=common_h.FALSE;
   public static int full_log_entries=common_h.FALSE;
   public static int show_scheduled_downtime=common_h.TRUE;
   
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
   
   public static int get_date_parts=common_h.FALSE;
   public static int select_hostgroups=common_h.FALSE;
   public static int select_hosts=common_h.FALSE;
   public static int select_servicegroups=common_h.FALSE;
   public static int select_services=common_h.FALSE;
   public static int select_output_format=common_h.FALSE;
   
   public static int compute_time_from_parts=common_h.FALSE;
   
   public static int show_all_hostgroups=common_h.FALSE;
   public static int show_all_hosts=common_h.FALSE;
   public static int show_all_servicegroups=common_h.FALSE;
   public static int show_all_services=common_h.FALSE;
   
   public static int assume_initial_states=common_h.TRUE;
   public static int assume_state_retention=common_h.TRUE;
   public static int assume_states_during_notrunning=common_h.TRUE;
   public static int initial_assumed_host_state=AS_NO_DATA;
   public static int initial_assumed_service_state=AS_NO_DATA;
   public static int include_soft_states=common_h.FALSE;
   
   public static String hostgroup_name="";
   public static String host_name="";
   public static String servicegroup_name="";
   public static String svc_description="";
   
   public static int backtrack_archives=2;
   public static int earliest_archive=0;
   
   public static int embedded=common_h.FALSE;
   public static int display_header=common_h.TRUE;
   
   public static objects_h.timeperiod current_timeperiod=null;
   
   public static int output_format=HTML_OUTPUT;
   
   /**
    * For each type the servlet is called, , since we are stupidly using static variablesl, to emulate the original CGIs
    * we need to reset the context on each call.
    */
   public void reset_context() {
      
      current_authdata = new cgiauth_h.authdata();
      
      subject_list.clear();
      
      t1 = 0;
      t2 = 0;
      
      display_type=DISPLAY_NO_AVAIL;
      timeperiod_type=TIMEPERIOD_LAST24HOURS;
      show_log_entries=common_h.FALSE;
      full_log_entries=common_h.FALSE;
      show_scheduled_downtime=common_h.TRUE;
      
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
      
      get_date_parts=common_h.FALSE;
      select_hostgroups=common_h.FALSE;
      select_hosts=common_h.FALSE;
      select_servicegroups=common_h.FALSE;
      select_services=common_h.FALSE;
      select_output_format=common_h.FALSE;
      
      compute_time_from_parts=common_h.FALSE;
      
      show_all_hostgroups=common_h.FALSE;
      show_all_hosts=common_h.FALSE;
      show_all_servicegroups=common_h.FALSE;
      show_all_services=common_h.FALSE;
      
      assume_initial_states=common_h.TRUE;
      assume_state_retention=common_h.TRUE;
      assume_states_during_notrunning=common_h.TRUE;
      initial_assumed_host_state=AS_NO_DATA;
      initial_assumed_service_state=AS_NO_DATA;
      include_soft_states=common_h.FALSE;
      
      hostgroup_name="";
      host_name="";
      servicegroup_name="";
      svc_description="";
      
      backtrack_archives=2;
      earliest_archive=0;
      
      embedded=common_h.FALSE;
      display_header=common_h.TRUE;
      
      current_timeperiod=null;
      
      output_format=HTML_OUTPUT;
   }
   
   public void call_main() {
      main( null );
   }
   
   public static void main( String[] argv){
      int result=common_h.OK;
      String temp_buffer;
      String start_timestring;
      String end_timestring;
      int is_authorized=common_h.TRUE;
      long report_start_time;
      long report_end_time;
      long t3;
      long current_time;
      Calendar t;
      String firsthostpointer = null;
      
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
         cgiutils.exit( common_h.ERROR ); 
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
      
      /* initialize time period to last 24 hours */
      current_time = utils.currentTimeInSeconds();
      t2=current_time;
      t1=(current_time-(60*60*24));
      
      /* default number of backtracked archives */
      switch(blue.log_rotation_method){
         case common_h.LOG_ROTATION_MONTHLY:
            backtrack_archives=1;
            break;
         case common_h.LOG_ROTATION_WEEKLY:
            backtrack_archives=2;
            break;
         case common_h.LOG_ROTATION_DAILY:
            backtrack_archives=4;
            break;
         case common_h.LOG_ROTATION_HOURLY:
            backtrack_archives=8;
            break;
         default:
            backtrack_archives=2;
         break;
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
      
      /* don't let user create reports in the future */
      if(t2>current_time){
         t2=current_time;
         if(t1>t2)
            t1=t2-(60*60*24);
      }
      
      if(display_header==common_h.TRUE){
         
         /* begin top table */
         System.out.printf("<table border=0 width=100%% cellspacing=0 cellpadding=0>\n");
         System.out.printf("<tr>\n");
         
         /* left column of the first row */
         System.out.printf("<td align=left valign=top width=33%%>\n");
         
         switch(display_type){
            case DISPLAY_HOST_AVAIL:
               temp_buffer = "Host Availability Report";
               break;
            case DISPLAY_SERVICE_AVAIL:
               temp_buffer = "Service Availability Report";
               break;
            case DISPLAY_HOSTGROUP_AVAIL:
               temp_buffer = "Hostgroup Availability Report";
               break;
            case DISPLAY_SERVICEGROUP_AVAIL:
               temp_buffer = "Servicegroup Availability Report";
               break;
            default:
               temp_buffer = "Availability Report";
            break;
         }
         cgiutils.display_info_table(temp_buffer,common_h.FALSE,current_authdata);
         
         if(((display_type==DISPLAY_HOST_AVAIL && show_all_hosts==common_h.FALSE) || (display_type==DISPLAY_SERVICE_AVAIL && show_all_services==common_h.FALSE)) && get_date_parts==common_h.FALSE){
            
            System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
            System.out.printf("<TR><TD CLASS='linkBox'>\n");
            
            if(display_type==DISPLAY_HOST_AVAIL && show_all_hosts==common_h.FALSE){
               host_report_url("all","View Availability Report For All Hosts");
               System.out.printf("<BR>\n");
               System.out.printf("<a href='%s?host=%s&t1=%d&t2=%d&assumestateretention=%s&assumeinitialstates=%s&includesoftstates=%s&assumestatesduringnotrunning=%s&initialassumedhoststate=%d&backtrack=%d'>View Trends For This Host</a><BR>\n",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name),t1,t2,(include_soft_states==common_h.TRUE)?"yes":"no",(assume_state_retention==common_h.TRUE)?"yes":"no",(assume_initial_states==common_h.TRUE)?"yes":"no",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no",initial_assumed_host_state,backtrack_archives);
               System.out.printf("<a href='%s?host=%s&t1=%d&t2=%d&assumestateretention=%s'>View Alert Histogram For This Host</a><BR>\n",cgiutils_h.HISTOGRAM_CGI,cgiutils.url_encode(host_name),t1,t2,(assume_state_retention==common_h.TRUE)?"yes":"no");
               System.out.printf("<a href='%s?host=%s'>View Status Detail For This Host</a><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(host_name));
               System.out.printf("<a href='%s?host=%s'>View Alert History For This Host</a><BR>\n",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
               System.out.printf("<a href='%s?host=%s'>View Notifications For This Host</a><BR>\n",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
            }
            else if(display_type==DISPLAY_SERVICE_AVAIL && show_all_services==common_h.FALSE){
               host_report_url(host_name,"View Availability Report For This Host");
               System.out.printf("<BR>\n");
               service_report_url("null","all","View Availability Report For All Services");
               System.out.printf("<BR>\n");
               System.out.printf("<a href='%s?host=%s",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
               System.out.printf("&service=%s&t1=%d&t2=%d&assumestateretention=%s&includesoftstates=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedservicestate=%d&backtrack=%d'>View Trends For This Service</a><BR>\n",cgiutils.url_encode(svc_description),t1,t2,(include_soft_states==common_h.TRUE)?"yes":"no",(assume_state_retention==common_h.TRUE)?"yes":"no",(assume_initial_states==common_h.TRUE)?"yes":"no",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no",initial_assumed_service_state,backtrack_archives);
               System.out.printf("<a href='%s?host=%s",cgiutils_h.HISTOGRAM_CGI,cgiutils.url_encode(host_name));
               System.out.printf("&service=%s&t1=%d&t2=%d&assumestateretention=%s'>View Alert Histogram For This Service</a><BR>\n",cgiutils.url_encode(svc_description),t1,t2,(assume_state_retention==common_h.TRUE)?"yes":"no");
               System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.HISTORY_CGI,cgiutils.url_encode(host_name));
               System.out.printf("service=%s'>View Alert History This Service</A><BR>\n",cgiutils.url_encode(svc_description));
               System.out.printf("<A HREF='%s?host=%s&",cgiutils_h.NOTIFICATIONS_CGI,cgiutils.url_encode(host_name));
               System.out.printf("service=%s'>View Notifications For This Service</A><BR>\n",cgiutils.url_encode(svc_description));
            }
            
            System.out.printf("</TD></TR>\n");
            System.out.printf("</TABLE>\n");
         }
         
         System.out.printf("</td>\n");
         
         /* center column of top row */
         System.out.printf("<td align=center valign=top width=33%%>\n");
         
         if(display_type!=DISPLAY_NO_AVAIL && get_date_parts==common_h.FALSE){
            
            System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>\n");
            if(display_type==DISPLAY_HOST_AVAIL){
               if(show_all_hosts==common_h.TRUE)
                  System.out.printf("All Hosts");
               else
                  System.out.printf("Host '%s'",host_name);
            }
            else if(display_type==DISPLAY_SERVICE_AVAIL){
               if(show_all_services==common_h.TRUE)
                  System.out.printf("All Services");
               else
                  System.out.printf("Service '%s' On Host '%s'",svc_description,host_name);
            }
            else if(display_type==DISPLAY_HOSTGROUP_AVAIL){
               if(show_all_hostgroups==common_h.TRUE)
                  System.out.printf("All Hostgroups");
               else
                  System.out.printf("Hostgroup '%s'",hostgroup_name);
            }
            else if(display_type==DISPLAY_SERVICEGROUP_AVAIL){
               if(show_all_servicegroups==common_h.TRUE)
                  System.out.printf("All Servicegroups");
               else
                  System.out.printf("Servicegroup '%s'",servicegroup_name);
            }
            System.out.printf("</DIV>\n");
            
            System.out.printf("<BR>\n");
            
            System.out.printf("<IMG SRC='%s%s' BORDER=0 ALT='Availability Report' TITLE='Availability Report'>\n",cgiutils.url_images_path,cgiutils_h.TRENDS_ICON);
            
            System.out.printf("<BR CLEAR=ALL>\n");
            
            start_timestring = cgiutils.get_time_string(t1,common_h.SHORT_DATE_TIME);
            end_timestring = cgiutils.get_time_string(t2,common_h.SHORT_DATE_TIME);
            System.out.printf("<div align=center class='reportRange'>%s to %s</div>\n",start_timestring,end_timestring);
            
            cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((t2-t1));
            System.out.printf("<div align=center class='reportDuration'>Duration: %dd %dh %dm %ds</div>\n",tb.days,tb.hours,tb.minutes,tb.seconds);
         }
         
         System.out.printf("</td>\n");
         
         /* right hand column of top row */
         System.out.printf("<td align=right valign=bottom width=33%%>\n");
         
         System.out.printf("<table border=0 CLASS='optBox'>\n");
         
         if(display_type!=DISPLAY_NO_AVAIL && get_date_parts==common_h.FALSE){
            
            System.out.printf("<form method=\"GET\" action=\"%s\">\n",cgiutils_h.AVAIL_CGI);
            
            System.out.printf("<input type='hidden' name='t1' value='%d'>\n",t1);
            System.out.printf("<input type='hidden' name='t2' value='%d'>\n",t2);
            if(show_log_entries==common_h.TRUE)
               System.out.printf("<input type='hidden' name='show_log_entries' value=''>\n");
            if(full_log_entries==common_h.TRUE)
               System.out.printf("<input type='hidden' name='full_log_entries' value=''>\n");
            if(display_type==DISPLAY_HOSTGROUP_AVAIL)
               System.out.printf("<input type='hidden' name='hostgroup' value='%s'>\n",hostgroup_name);
            if(display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_SERVICE_AVAIL)
               System.out.printf("<input type='hidden' name='host' value='%s'>\n",host_name);
            if(display_type==DISPLAY_SERVICE_AVAIL)
               System.out.printf("<input type='hidden' name='service' value='%s'>\n",svc_description);
            if(display_type==DISPLAY_SERVICEGROUP_AVAIL)
               System.out.printf("<input type='hidden' name='servicegroup' value='%s'>\n",servicegroup_name);
            
            System.out.printf("<input type='hidden' name='assumeinitialstates' value='%s'>\n",(assume_initial_states==common_h.TRUE)?"yes":"no");
            System.out.printf("<input type='hidden' name='assumestateretention' value='%s'>\n",(assume_state_retention==common_h.TRUE)?"yes":"no");
            System.out.printf("<input type='hidden' name='assumestatesduringnotrunning' value='%s'>\n",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no");
            System.out.printf("<input type='hidden' name='includesoftstates' value='%s'>\n",(include_soft_states==common_h.TRUE)?"yes":"no");
            
            System.out.printf("<tr><td valign=top align=left class='optBoxItem'>First assumed %s state:</td><td valign=top align=left class='optBoxItem'>%s</td></tr>\n",(display_type==DISPLAY_SERVICE_AVAIL)?"service":"host",(display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_HOSTGROUP_AVAIL || display_type==DISPLAY_SERVICEGROUP_AVAIL)?"First assumed service state":"");
            System.out.printf("<tr>\n");
            System.out.printf("<td valign=top align=left class='optBoxItem'>\n");
            if(display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_HOSTGROUP_AVAIL || display_type==DISPLAY_SERVICEGROUP_AVAIL){
               System.out.printf("<select name='initialassumedhoststate'>\n");
               System.out.printf("<option value=%d %s>Unspecified\n",AS_NO_DATA,(initial_assumed_host_state==AS_NO_DATA)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Current State\n",AS_CURRENT_STATE,(initial_assumed_host_state==AS_CURRENT_STATE)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Host Up\n",AS_HOST_UP,(initial_assumed_host_state==AS_HOST_UP)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Host Down\n",AS_HOST_DOWN,(initial_assumed_host_state==AS_HOST_DOWN)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Host Unreachable\n",AS_HOST_UNREACHABLE,(initial_assumed_host_state==AS_HOST_UNREACHABLE)?"SELECTED":"");
               System.out.printf("</select>\n");
            }
            else{
               System.out.printf("<input type='hidden' name='initialassumedhoststate' value='%d'>",initial_assumed_host_state);
               System.out.printf("<select name='initialassumedservicestate'>\n");
               System.out.printf("<option value=%d %s>Unspecified\n",AS_NO_DATA,(initial_assumed_service_state==AS_NO_DATA)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Current State\n",AS_CURRENT_STATE,(initial_assumed_service_state==AS_CURRENT_STATE)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Ok\n",AS_SVC_OK,(initial_assumed_service_state==AS_SVC_OK)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Warning\n",AS_SVC_WARNING,(initial_assumed_service_state==AS_SVC_WARNING)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Unknown\n",AS_SVC_UNKNOWN,(initial_assumed_service_state==AS_SVC_UNKNOWN)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Critical\n",AS_SVC_CRITICAL,(initial_assumed_service_state==AS_SVC_CRITICAL)?"SELECTED":"");
               System.out.printf("</select>\n");
            }
            System.out.printf("</td>\n");
            System.out.printf("<td CLASS='optBoxItem'>\n");
            if(display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_HOSTGROUP_AVAIL || display_type==DISPLAY_SERVICEGROUP_AVAIL){
               System.out.printf("<select name='initialassumedservicestate'>\n");
               System.out.printf("<option value=%d %s>Unspecified\n",AS_NO_DATA,(initial_assumed_service_state==AS_NO_DATA)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Current State\n",AS_CURRENT_STATE,(initial_assumed_service_state==AS_CURRENT_STATE)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Ok\n",AS_SVC_OK,(initial_assumed_service_state==AS_SVC_OK)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Warning\n",AS_SVC_WARNING,(initial_assumed_service_state==AS_SVC_WARNING)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Unknown\n",AS_SVC_UNKNOWN,(initial_assumed_service_state==AS_SVC_UNKNOWN)?"SELECTED":"");
               System.out.printf("<option value=%d %s>Service Critical\n",AS_SVC_CRITICAL,(initial_assumed_service_state==AS_SVC_CRITICAL)?"SELECTED":"");
               System.out.printf("</select>\n");
            }
            System.out.printf("</td>\n");
            System.out.printf("</tr>\n");
            
            System.out.printf("<tr><td valign=top align=left class='optBoxItem'>Report period:</td><td valign=top align=left class='optBoxItem'>Backtracked archives:</td></tr>\n");
            System.out.printf("<tr>\n");
            System.out.printf("<td valign=top align=left class='optBoxItem'>\n");
            System.out.printf("<select name='timeperiod'>\n");
            System.out.printf("<option SELECTED>[ Current time range ]\n");
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
            System.out.printf("</td>\n");
            System.out.printf("<td valign=top align=left CLASS='optBoxItem'>\n");
            System.out.printf("<input type='text' size='2' maxlength='2' name='backtrack' value='%d'>\n",backtrack_archives);
            System.out.printf("</td>\n");
            System.out.printf("</tr>\n");
            
            System.out.printf("<tr><td valign=top align=left></td>\n");
            System.out.printf("<td valign=top align=left CLASS='optBoxItem'>\n");
            System.out.printf("<input type='submit' value='Update'>\n");
            System.out.printf("</td>\n");
            System.out.printf("</tr>\n");
            
            System.out.printf("</form>\n");
         }
         
         /* display context-sensitive help */
         System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
         if(get_date_parts==common_h.TRUE)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_MENU5);
         else if(select_hostgroups==common_h.TRUE)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_MENU2);
         else if(select_hosts==common_h.TRUE)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_MENU3);
         else if(select_services==common_h.TRUE)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_MENU4);
         else if(display_type==DISPLAY_HOSTGROUP_AVAIL)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_HOSTGROUP);
         else if(display_type==DISPLAY_HOST_AVAIL)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_HOST);
         else if(display_type==DISPLAY_SERVICE_AVAIL)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_SERVICE);
         else if(display_type==DISPLAY_SERVICEGROUP_AVAIL)
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_SERVICEGROUP);
         else
            cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_AVAIL_MENU1);
         System.out.printf("</td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</td>\n");
         
         /* end of top table */
         System.out.printf("</tr>\n");
         System.out.printf("</table>\n");
      }
      
      
      
      
      /* step 3 - ask user for report date range */
      if(get_date_parts==common_h.TRUE){
         
         current_time = utils.currentTimeInSeconds();
         t = Calendar.getInstance();
         
         start_day=1;
         start_year = t.get( Calendar.YEAR );
         end_day= t.get( Calendar.DAY_OF_MONTH );
         end_year  = t.get( Calendar.YEAR );
         
         System.out.printf("<P><DIV ALIGN=CENTER CLASS='dateSelectTitle'>Step 3: Select Report Options</DIV></p>\n");
         
         System.out.printf("<P><DIV ALIGN=CENTER>\n");
         
         System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.AVAIL_CGI);
         System.out.printf("<input type='hidden' name='show_log_entries' value=''>\n");
         if(display_type==DISPLAY_HOSTGROUP_AVAIL)
            System.out.printf("<input type='hidden' name='hostgroup' value='%s'>\n",hostgroup_name);
         if(display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_SERVICE_AVAIL)
            System.out.printf("<input type='hidden' name='host' value='%s'>\n",host_name);
         if(display_type==DISPLAY_SERVICE_AVAIL)
            System.out.printf("<input type='hidden' name='service' value='%s'>\n",svc_description);
         if(display_type==DISPLAY_SERVICEGROUP_AVAIL)
            System.out.printf("<input type='hidden' name='servicegroup' value='%s'>\n",servicegroup_name);
         
         System.out.printf("<table border=0 cellpadding=5>\n");
         
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
         System.out.printf("<option value='1' %s>January\n",(t.get(Calendar.MONTH)==0)?"SELECTED":"");
         System.out.printf("<option value='2' %s>February\n",(t.get(Calendar.MONTH)==1)?"SELECTED":"");
         System.out.printf("<option value='3' %s>March\n",(t.get(Calendar.MONTH)==2)?"SELECTED":"");
         System.out.printf("<option value='4' %s>April\n",(t.get(Calendar.MONTH)==3)?"SELECTED":"");
         System.out.printf("<option value='5' %s>May\n",(t.get(Calendar.MONTH)==4)?"SELECTED":"");
         System.out.printf("<option value='6' %s>June\n",(t.get(Calendar.MONTH)==5)?"SELECTED":"");
         System.out.printf("<option value='7' %s>July\n",(t.get(Calendar.MONTH)==6)?"SELECTED":"");
         System.out.printf("<option value='8' %s>August\n",(t.get(Calendar.MONTH)==7)?"SELECTED":"");
         System.out.printf("<option value='9' %s>September\n",(t.get(Calendar.MONTH)==8)?"SELECTED":"");
         System.out.printf("<option value='10' %s>October\n",(t.get(Calendar.MONTH)==9)?"SELECTED":"");
         System.out.printf("<option value='11' %s>November\n",(t.get(Calendar.MONTH)==10)?"SELECTED":"");
         System.out.printf("<option value='12' %s>December\n",(t.get(Calendar.MONTH)==11)?"SELECTED":"");
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
         System.out.printf("<option value='1' %s>January\n",(t.get(Calendar.MONTH)==0)?"SELECTED":"");
         System.out.printf("<option value='2' %s>February\n",(t.get(Calendar.MONTH)==1)?"SELECTED":"");
         System.out.printf("<option value='3' %s>March\n",(t.get(Calendar.MONTH)==2)?"SELECTED":"");
         System.out.printf("<option value='4' %s>April\n",(t.get(Calendar.MONTH)==3)?"SELECTED":"");
         System.out.printf("<option value='5' %s>May\n",(t.get(Calendar.MONTH)==4)?"SELECTED":"");
         System.out.printf("<option value='6' %s>June\n",(t.get(Calendar.MONTH)==5)?"SELECTED":"");
         System.out.printf("<option value='7' %s>July\n",(t.get(Calendar.MONTH)==6)?"SELECTED":"");
         System.out.printf("<option value='8' %s>August\n",(t.get(Calendar.MONTH)==7)?"SELECTED":"");
         System.out.printf("<option value='9' %s>September\n",(t.get(Calendar.MONTH)==8)?"SELECTED":"");
         System.out.printf("<option value='10' %s>October\n",(t.get(Calendar.MONTH)==9)?"SELECTED":"");
         System.out.printf("<option value='11' %s>November\n",(t.get(Calendar.MONTH)==10)?"SELECTED":"");
         System.out.printf("<option value='12' %s>December\n",(t.get(Calendar.MONTH)==11)?"SELECTED":"");
         System.out.printf("</select>\n ");
         System.out.printf("<input type='text' size='2' maxlength='2' name='eday' value='%d'> ",end_day);
         System.out.printf("<input type='text' size='4' maxlength='4' name='eyear' value='%d'>",end_year);
         System.out.printf("<input type='hidden' name='ehour' value='24'>\n");
         System.out.printf("<input type='hidden' name='emin' value='0'>\n");
         System.out.printf("<input type='hidden' name='esec' value='0'>\n");
         System.out.printf("</td>\n");
         System.out.printf("</tr>\n");
         
         System.out.printf("<tr><td colspan=2><br></td></tr>\n");
         
         System.out.printf("<tr>");
         System.out.printf("<td valign=top class='reportSelectSubTitle'>Report time Period:</td>\n");
         System.out.printf("<td valign=top align=left class='optBoxItem'>\n");
         System.out.printf("<select name='rpttimeperiod'>\n");
         System.out.printf("<option value=\"\">None\n");
         /* check all the time periods... */
         for( objects_h.timeperiod temp_timeperiod : (ArrayList<objects_h.timeperiod>) objects.timeperiod_list) 
            System.out.printf("<option value=%s>%s\n",cgiutils.url_encode(temp_timeperiod.name),temp_timeperiod.name);
         System.out.printf("</select>\n");
         System.out.printf("</td>\n");
         System.out.printf("</tr>\n");
         System.out.printf("<tr><td colspan=2><br></td></tr>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Assume Initial States:</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<select name='assumeinitialstates'>\n");
         System.out.printf("<option value=yes>Yes\n");
         System.out.printf("<option value=no>No\n");
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Assume State Retention:</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<select name='assumestateretention'>\n");
         System.out.printf("<option value=yes>Yes\n");
         System.out.printf("<option value=no>No\n");
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Assume States During Program Downtime:</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<select name='assumestatesduringnotrunning'>\n");
         System.out.printf("<option value=yes>Yes\n");
         System.out.printf("<option value=no>No\n");
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Include Soft States:</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<select name='includesoftstates'>\n");
         System.out.printf("<option value=yes>Yes\n");
         System.out.printf("<option value=no SELECTED>No\n");
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         if(display_type!=DISPLAY_SERVICE_AVAIL){
            System.out.printf("<tr><td class='reportSelectSubTitle' align=right>First Assumed Host State:</td>\n");
            System.out.printf("<td class='reportSelectItem'>\n");
            System.out.printf("<select name='initialassumedhoststate'>\n");
            System.out.printf("<option value=%d>Unspecified\n",AS_NO_DATA);
            System.out.printf("<option value=%d>Current State\n",AS_CURRENT_STATE);
            System.out.printf("<option value=%d>Host Up\n",AS_HOST_UP);
            System.out.printf("<option value=%d>Host Down\n",AS_HOST_DOWN);
            System.out.printf("<option value=%d>Host Unreachable\n",AS_HOST_UNREACHABLE);
            System.out.printf("</select>\n");
            System.out.printf("</td></tr>\n");
         }
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>First Assumed Service State:</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<select name='initialassumedservicestate'>\n");
         System.out.printf("<option value=%d>Unspecified\n",AS_NO_DATA);
         System.out.printf("<option value=%d>Current State\n",AS_CURRENT_STATE);
         System.out.printf("<option value=%d>Service Ok\n",AS_SVC_OK);
         System.out.printf("<option value=%d>Service Warning\n",AS_SVC_WARNING);
         System.out.printf("<option value=%d>Service Unknown\n",AS_SVC_UNKNOWN);
         System.out.printf("<option value=%d>Service Critical\n",AS_SVC_CRITICAL);
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Backtracked Archives (To Scan For Initial States):</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<input type='text' name='backtrack' size='2' maxlength='2' value='%d'>\n",backtrack_archives);
         System.out.printf("</td></tr>\n");
         
         if((display_type==DISPLAY_HOST_AVAIL && show_all_hosts==common_h.TRUE) || (display_type==DISPLAY_SERVICE_AVAIL && show_all_services==common_h.TRUE)){
            System.out.printf("<tr>");
            System.out.printf("<td valign=top class='reportSelectSubTitle'>Output in CSV Format:</td>\n");
            System.out.printf("<td valign=top class='reportSelectItem'>");
            System.out.printf("<input type='checkbox' name='csvoutput' value=''>\n");
            System.out.printf("</td>\n");
            System.out.printf("</tr>\n");
         }
         
         System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Create Availability Report!'></td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</form>\n");
         System.out.printf("</DIV></P>\n");
      }
      
      
      /* step 2 - the user wants to select a hostgroup */
      else if(select_hostgroups==common_h.TRUE){
         System.out.printf("<p><div align=center class='reportSelectTitle'>Step 2: Select Hostgroup</div></p>\n");
         
         System.out.printf("<p><div align=center>\n");
         
         System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.AVAIL_CGI);
         System.out.printf("<input type='hidden' name='get_date_parts'>\n");
         
         System.out.printf("<table border=0 cellpadding=5>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Hostgroup(s):</td><td align=left valign=center class='reportSelectItem'>\n");
         System.out.printf("<select name='hostgroup'>\n");
         System.out.printf("<option value='all'>** ALL HOSTGROUPS **\n");
         for( objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list ) {
            if(cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.TRUE)
               System.out.printf("<option value='%s'>%s\n",temp_hostgroup.group_name,temp_hostgroup.group_name);
         }
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Continue to Step 3'></td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</form>\n");
         
         System.out.printf("</div></p>\n");
      }
      
      /* step 2 - the user wants to select a host */
      else if(select_hosts==common_h.TRUE){
         System.out.printf("<p><div align=center class='reportSelectTitle'>Step 2: Select Host</div></p>\n");
         
         System.out.printf("<p><div align=center>\n");
         
         System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.AVAIL_CGI);
         System.out.printf("<input type='hidden' name='get_date_parts'>\n");
         
         System.out.printf("<table border=0 cellpadding=5>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Host(s):</td><td align=left valign=center class='reportSelectItem'>\n");
         System.out.printf("<select name='host'>\n");
         System.out.printf("<option value='all'>** ALL HOSTS **\n");
         for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ) {
            if( cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.TRUE)
               System.out.printf("<option value='%s'>%s\n",temp_host.name,temp_host.name);
         }
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Continue to Step 3'></td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</form>\n");
         
         System.out.printf("</div></p>\n");
         
         System.out.printf("<div align=center class='helpfulHint'>Tip: If you want to have the option of getting the availability data in CSV format, select '<b>** ALL HOSTS **</b>' from the pull-down menu.\n");
      }
      
      /* step 2 - the user wants to select a servicegroup */
      else if(select_servicegroups==common_h.TRUE){
         System.out.printf("<p><div align=center class='reportSelectTitle'>Step 2: Select Servicegroup</div></p>\n");
         
         System.out.printf("<p><div align=center>\n");
         
         System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.AVAIL_CGI);
         System.out.printf("<input type='hidden' name='get_date_parts'>\n");
         
         System.out.printf("<table border=0 cellpadding=5>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Servicegroup(s):</td><td align=left valign=center class='reportSelectItem'>\n");
         System.out.printf("<select name='servicegroup'>\n");
         System.out.printf("<option value='all'>** ALL SERVICEGROUPS **\n");
         for( objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>) objects.servicegroup_list ) { 
            if(cgiauth.is_authorized_for_servicegroup(temp_servicegroup,current_authdata)==common_h.TRUE)
               System.out.printf("<option value='%s'>%s\n",temp_servicegroup.group_name,temp_servicegroup.group_name);
         }
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Continue to Step 3'></td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</form>\n");
         
         System.out.printf("</div></p>\n");
      }
      
      /* step 2 - the user wants to select a service */
      else if(select_services==common_h.TRUE){
         
         System.out.printf("<SCRIPT LANGUAGE='JavaScript'>\n");
         System.out.printf("function gethostname(hostindex){\n");
         System.out.printf("hostnames=[\"all\"");
         
         firsthostpointer=null;
         for( objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ) {
            if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.TRUE){
               if(firsthostpointer == null)
                  firsthostpointer=temp_service.host_name;
               System.out.printf(", \"%s\"",temp_service.host_name);
            }
         }
         
         System.out.printf(" ]\n");
         System.out.printf("return hostnames[hostindex];\n");
         System.out.printf("}\n");
         System.out.printf("</SCRIPT>\n");
         
         System.out.printf("<p><div align=center class='reportSelectTitle'>Step 2: Select Service</div></p>\n");
         
         System.out.printf("<p><div align=center>\n");
         
         System.out.printf("<form method=\"get\" action=\"%s\" name='serviceform'>\n",cgiutils_h.AVAIL_CGI);
         System.out.printf("<input type='hidden' name='get_date_parts'>\n");
         System.out.printf("<input type='hidden' name='host' value='%s'>\n",(firsthostpointer==null)?"unknown":firsthostpointer);
         
         System.out.printf("<table border=0 cellpadding=5>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Service(s):</td><td align=left valign=center class='reportSelectItem'>\n");
         System.out.printf("<select name='service' onFocus='document.serviceform.host.value=gethostname(this.selectedIndex);' onChange='document.serviceform.host.value=gethostname(this.selectedIndex);'>\n");
         System.out.printf("<option value='all'>** ALL SERVICES **\n");
         for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ) {
            if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.TRUE)
               System.out.printf("<option value='%s'>%s;%s\n",temp_service.description,temp_service.host_name,temp_service.description);
         }
         
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Continue to Step 3'></td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</form>\n");
         
         System.out.printf("</div></p>\n");
         
         System.out.printf("<div align=center class='helpfulHint'>Tip: If you want to have the option of getting the availability data in CSV format, select '<b>** ALL SERVICES **</b>' from the pull-down menu.\n");
      }
      
      
      /* generate availability report */
      else if(display_type!=DISPLAY_NO_AVAIL){
         
         /* check authorization */
         is_authorized=common_h.TRUE;
         if((display_type==DISPLAY_HOST_AVAIL && show_all_hosts==common_h.FALSE) || (display_type==DISPLAY_SERVICE_AVAIL && show_all_services==common_h.FALSE)){
            
            if(display_type==DISPLAY_HOST_AVAIL && show_all_hosts==common_h.FALSE)
               is_authorized=cgiauth.is_authorized_for_host( objects.find_host(host_name),current_authdata);
            else
               is_authorized=cgiauth.is_authorized_for_service( objects.find_service(host_name,svc_description),current_authdata);
         }
         
         if(is_authorized==common_h.FALSE)
            System.out.printf("<P><DIV ALIGN=CENTER CLASS='errorMessage'>It appears as though you are not authorized to view information for the specified %s...</DIV></P>\n",(display_type==DISPLAY_HOST_AVAIL)?"host":"service");
         
         else{
            
            report_start_time = utils.currentTimeInSeconds();
            
            /* create list of subjects to collect availability data for */
            create_subject_list();
            
            /* read in all necessary archived state data */
            read_archived_state_data();
            
            /* compute availability data */
            compute_availability();
            
            report_end_time = utils.currentTimeInSeconds();
            
            if(output_format==HTML_OUTPUT){
               cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((report_end_time-report_start_time));
               System.out.printf("<div align=center class='reportTime'>[ Availability report completed in %d min %d sec ]</div>\n",tb.minutes,tb.seconds);
               System.out.printf("<BR><BR>\n");
            }
            
            /* display availability data */
            if(display_type==DISPLAY_HOST_AVAIL)
               display_host_availability();
            else if(display_type==DISPLAY_SERVICE_AVAIL)
               display_service_availability();
            else if(display_type==DISPLAY_HOSTGROUP_AVAIL)
               display_hostgroup_availability();
            else if(display_type==DISPLAY_SERVICEGROUP_AVAIL)
               display_servicegroup_availability();
            
            /* free memory allocated to availability data */
            free_availability_data();
         }
      }
      
      
      /* step 1 - ask the user what kind of report they want */
      else{
         
         System.out.printf("<p><div align=center class='reportSelectTitle'>Step 1: Select Report Type</div></p>\n");
         
         System.out.printf("<p><div align=center>\n");
         
         System.out.printf("<form method=\"get\" action=\"%s\">\n",cgiutils_h.AVAIL_CGI);
         
         System.out.printf("<table border=0 cellpadding=5>\n");
         
         System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Type:</td>\n");
         System.out.printf("<td class='reportSelectItem'>\n");
         System.out.printf("<select name='report_type'>\n");
         System.out.printf("<option value=hostgroups>Hostgroup(s)\n");
         System.out.printf("<option value=hosts>Host(s)\n");
         System.out.printf("<option value=servicegroups>Servicegroup(s)\n");
         System.out.printf("<option value=services>Service(s)\n");
         System.out.printf("</select>\n");
         System.out.printf("</td></tr>\n");
         
         System.out.printf("<tr><td></td><td align=left class='dateSelectItem'><input type='submit' value='Continue to Step 2'></td></tr>\n");
         
         System.out.printf("</table>\n");
         
         System.out.printf("</form>\n");
         
         System.out.printf("</div></p>\n");
      }
      
      
      document_footer();
      
      cgiutils.exit(  common_h.OK );
   }

public static void document_header( int use_stylesheet){
	String date_time;

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );

       if(output_format==HTML_OUTPUT)
           response.setContentType("text/html");
       else{
          response.setContentType("text/plain");
          return;
       }
    } else {
          
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      	
      	date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      	
      	date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
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
	System.out.printf("Blue Availability\n");
	System.out.printf("</title>\n");

	if(use_stylesheet==common_h.TRUE){
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
		System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.AVAIL_CSS);
	        }
	
	System.out.printf("</head>\n");

	System.out.printf("<BODY CLASS='avail'>\n");

	/* include user SSI header */
	cgiutils.include_ssi_files(cgiutils_h.AVAIL_CGI,cgiutils_h.SSI_HEADER);

	return;
        }



public static void document_footer(){

	if(output_format!=HTML_OUTPUT)
		return;

	if(embedded==common_h.TRUE)
		return;

	/* include user SSI footer */
    cgiutils.include_ssi_files(cgiutils_h.AVAIL_CGI,cgiutils_h.SSI_FOOTER);

	System.out.printf("</body>\n");
	System.out.printf("</html>\n");

	return;
        }



public static int process_cgivars(){
	String []variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars( request_string );

	for(x=0; x < variables.length ;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the hostgroup argument */
		else if(variables[x].equals("hostgroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			hostgroup_name = variables[x];
			display_type=DISPLAY_HOSTGROUP_AVAIL;
			show_all_hostgroups=(!hostgroup_name.equals("all"))?common_h.FALSE:common_h.TRUE;
		        }

		/* we found the servicegroup argument */
		else if( variables[x].equals( "servicegroup")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			servicegroup_name = variables[x];
			display_type=DISPLAY_SERVICEGROUP_AVAIL;
			show_all_servicegroups=(!servicegroup_name.equals("all"))?common_h.FALSE:common_h.TRUE;
		        }

		/* we found the host argument */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			host_name = variables[x];
			display_type=DISPLAY_HOST_AVAIL;
			show_all_hosts=(!host_name.equals("all"))?common_h.FALSE:common_h.TRUE;
		        }

		/* we found the service description argument */
		else if(variables[x].equals("service")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				svc_description = variables[x];
			display_type=DISPLAY_SERVICE_AVAIL;
			show_all_services=(!svc_description.equals("all"))?common_h.FALSE:common_h.TRUE;
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

		/* we found the assume initial states option */
		else if(variables[x].equals("assumeinitialstates")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				assume_initial_states=common_h.TRUE;
			else
				assume_initial_states=common_h.FALSE;
		        }

		/* we found the assume state during program not running option */
		else if(variables[x].equals("assumestatesduringnotrunning")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				assume_states_during_notrunning=common_h.TRUE;
			else
				assume_states_during_notrunning=common_h.FALSE;
		        }

		/* we found the initial assumed host state option */
		else if(variables[x].equals("initialassumedhoststate")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			initial_assumed_host_state=atoi(variables[x]);
		        }

		/* we found the initial assumed service state option */
		else if(variables[x].equals("initialassumedservicestate")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			initial_assumed_service_state=atoi(variables[x]);
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

		/* we found the include soft states option */
		else if(variables[x].equals("includesoftstates")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				include_soft_states=common_h.TRUE;
			else
				include_soft_states=common_h.FALSE;
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

            // TODO DEBUG
//			System.out.printf("BACKTRACK ARCHIVES: %d\n",backtrack_archives);
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

		/* we found the CSV output option */
		else if(variables[x].equals("csvoutput")){
			display_header=common_h.FALSE;
			output_format=CSV_OUTPUT;
		        }

		/* we found the log entries option  */
		else if(variables[x].equals("show_log_entries"))
			show_log_entries=common_h.TRUE;

		/* we found the full log entries option */
		else if(variables[x].equals("full_log_entries"))
			full_log_entries=common_h.TRUE;

		/* we found the get date parts option */
		else if(variables[x].equals("get_date_parts"))
		        get_date_parts=common_h.TRUE;

		/* we found the report type selection option */
		else if(variables[x].equals("report_type")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			if(variables[x].equals("hostgroups"))
				select_hostgroups=common_h.TRUE;
			else if(variables[x].equals("servicegroups"))
				select_servicegroups=common_h.TRUE;
			else if(variables[x].equals("hosts"))
				select_hosts=common_h.TRUE;
			else
				select_services=common_h.TRUE;
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

		/* we found the show scheduled downtime option */
		else if(variables[x].equals("showscheduleddowntime")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("yes"))
				show_scheduled_downtime=common_h.TRUE;
			else
				show_scheduled_downtime=common_h.FALSE;
		        }

		/* we found the report timeperiod option */
		else if(variables[x].equals("rpttimeperiod")){

			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			for( objects_h.timeperiod temp_timeperiod : (ArrayList<objects_h.timeperiod>) objects.timeperiod_list) {
				if( cgiutils.url_encode(temp_timeperiod.name).equals( variables[x])){
					current_timeperiod=temp_timeperiod;
					break;
				        }
			        }
		        }

	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



/* computes availability data for all subjects */
public static void compute_availability(){
	
	long current_time;

	current_time = utils.currentTimeInSeconds();

	for(avail_subject temp_subject : subject_list ) { 
		compute_subject_availability(temp_subject,current_time);
		compute_subject_downtime(temp_subject,current_time);
	        }

	return;
        }



/* computes availability data for a given subject */
public static void compute_subject_availability(avail_subject subject, long current_time){
//	archived_state temp_as;
	archived_state last_as;
	long a;
	long b;
	int current_state=AS_NO_DATA;
	int have_some_real_data=common_h.FALSE;
	statusdata_h.hoststatus hststatus=null;
	statusdata_h.servicestatus svcstatus=null;
	int first_real_state=AS_NO_DATA;
	long initial_assumed_time;
	int initial_assumed_state=AS_NO_DATA;
	int error;


	/* if left hand of graph is after current time, we can't do anything at all.... */
	if(t1>current_time)
		return;

	/* get current state of host or service if possible */
	if(subject.type==HOST_SUBJECT)
		hststatus=statusdata.find_hoststatus(subject.host_name);
	else
		svcstatus=statusdata.find_servicestatus(subject.host_name,subject.service_description);


	/************************************/
	/* INSERT CURRENT STATE (IF WE CAN) */
	/************************************/

	/* if current time DOES NOT fall within graph bounds, so we can't do anything as far as assuming current state */

	/* if we don't have any data, assume current state (if possible) */
	if(subject.as_list==null && current_time>t1 && current_time<=t2){

		/* we don't have any historical information, but the current time falls within the reporting period, so use */
		/* the current status of the host/service as the starting data */
		if(subject.type==HOST_SUBJECT){
			if(hststatus!=null){

				if(hststatus.status==statusdata_h.HOST_DOWN)
					subject.last_known_state=AS_HOST_DOWN;
				else if(hststatus.status==statusdata_h.HOST_UNREACHABLE)
					subject.last_known_state=AS_HOST_UNREACHABLE;
				else if(hststatus.status==statusdata_h.HOST_UP)
					subject.last_known_state=AS_HOST_UP;
				else
					subject.last_known_state=AS_NO_DATA;

				if(subject.last_known_state!=AS_NO_DATA){

				        /* add a dummy archived state item, so something can get graphed */
					add_archived_state(subject.last_known_state,AS_HARD_STATE,t1,"Current Host State Assumed (Faked Log Entry)",subject);

				        /* use the current state as the last known real state */
					first_real_state=subject.last_known_state;
				        }
			        }
		        }
		else{
			if(svcstatus!=null){

				if(svcstatus.status==statusdata_h.SERVICE_OK)
					subject.last_known_state=AS_SVC_OK;
				else if(svcstatus.status==statusdata_h.SERVICE_WARNING)
					subject.last_known_state=AS_SVC_WARNING;
				else if(svcstatus.status==statusdata_h.SERVICE_CRITICAL)
					subject.last_known_state=AS_SVC_CRITICAL;
				else if(svcstatus.status==statusdata_h.SERVICE_UNKNOWN)
					subject.last_known_state=AS_SVC_UNKNOWN;
				else
					subject.last_known_state=AS_NO_DATA;

				if(subject.last_known_state!=AS_NO_DATA){

				        /* add a dummy archived state item, so something can get graphed */
					add_archived_state(subject.last_known_state,AS_HARD_STATE,t1,"Current Service State Assumed (Faked Log Entry)",subject);

				        /* use the current state as the last known real state */
					first_real_state=subject.last_known_state;
				        }
			        }
		        }
	        }



	/******************************************/
	/* INSERT FIRST ASSUMED STATE (IF WE CAN) */
	/******************************************/

	if((subject.type==HOST_SUBJECT && initial_assumed_host_state!=AS_NO_DATA) || (subject.type==SERVICE_SUBJECT && initial_assumed_service_state!=AS_NO_DATA)){

		/* see if its okay to assume initial state for this subject */
		error=common_h.FALSE;
		if(subject.type==SERVICE_SUBJECT){
			if(initial_assumed_service_state!=AS_SVC_OK && initial_assumed_service_state!=AS_SVC_WARNING && initial_assumed_service_state!=AS_SVC_UNKNOWN && initial_assumed_service_state!=AS_SVC_CRITICAL && initial_assumed_service_state!=AS_CURRENT_STATE)
				error=common_h.TRUE;
			else
				initial_assumed_state=initial_assumed_service_state;
			if(initial_assumed_service_state==AS_CURRENT_STATE && svcstatus==null)
				error=common_h.TRUE;
		        }
		else{
			if(initial_assumed_host_state!=AS_HOST_UP && initial_assumed_host_state!=AS_HOST_DOWN && initial_assumed_host_state!=AS_HOST_UNREACHABLE && initial_assumed_host_state!=AS_CURRENT_STATE)
				error=common_h.TRUE;
			else
				initial_assumed_state=initial_assumed_host_state;
			if(initial_assumed_host_state==AS_CURRENT_STATE && hststatus==null)
				error=common_h.TRUE;
		        }

		/* get the current state if applicable */
		if(((subject.type==HOST_SUBJECT && initial_assumed_host_state==AS_CURRENT_STATE) || (subject.type==SERVICE_SUBJECT && initial_assumed_service_state==AS_CURRENT_STATE)) && error==common_h.FALSE){
			if(subject.type==SERVICE_SUBJECT){
				switch(svcstatus.status){
				case statusdata_h.SERVICE_OK:
					initial_assumed_state=AS_SVC_OK;
					break;
				case statusdata_h.SERVICE_WARNING:
					initial_assumed_state=AS_SVC_WARNING;
					break;
				case statusdata_h.SERVICE_UNKNOWN:
					initial_assumed_state=AS_SVC_UNKNOWN;
					break;
				case statusdata_h.SERVICE_CRITICAL:
					initial_assumed_state=AS_SVC_CRITICAL;
					break;
				default:
					error=common_h.TRUE;
					break;
				        }
			        }
			else{
				switch(hststatus.status){
				case statusdata_h.HOST_DOWN:
					initial_assumed_state=AS_HOST_DOWN;
					break;
				case statusdata_h.HOST_UNREACHABLE:
					initial_assumed_state=AS_HOST_UNREACHABLE;
					break;
				case statusdata_h.HOST_UP:
					initial_assumed_state=AS_HOST_UP;
					break;
				default:
					error=common_h.TRUE;
					break;
				        }
			        }
		        }

		if(error==common_h.FALSE){

			/* add this assumed state entry before any entries in the list and <= t1 */
			if(subject.as_list==null || subject.as_list.isEmpty())
				initial_assumed_time=t1;
			else if(subject.as_list.get(0).time_stamp>t1)
				initial_assumed_time=t1;
			else
				initial_assumed_time=subject.as_list.get(0).time_stamp-1;
			
			if(subject.type==HOST_SUBJECT)
				add_archived_state(initial_assumed_state,AS_HARD_STATE,initial_assumed_time,"First Host State Assumed (Faked Log Entry)",subject);
			else
				add_archived_state(initial_assumed_state,AS_HARD_STATE,initial_assumed_time,"First Service State Assumed (Faked Log Entry)",subject);
		        }
	        }




	/**************************************/
	/* BAIL OUT IF WE DON'T HAVE ANYTHING */
	/**************************************/

	have_some_real_data=common_h.FALSE;
	for( archived_state temp_as : subject.as_list ){
		if(temp_as.entry_type!=AS_NO_DATA && temp_as.entry_type!=AS_PROGRAM_START && temp_as.entry_type!=AS_PROGRAM_END){
			have_some_real_data=common_h.TRUE;
			break;
		        }
	        }
	if(have_some_real_data==common_h.FALSE)
		return;



	
	last_as=null;
	subject.earliest_time=t2;
	subject.latest_time=t1;


    // TODO DEBUG
//	System.out.printf("--- BEGINNING/MIDDLE SECTION ---<BR>\n");

	/**********************************/
	/*    BEGINNING/MIDDLE SECTION    */
	/**********************************/

	for( archived_state temp_as : subject.as_list ){

		/* keep this as last known state if this is the first entry or if it occurs before the starting point of the graph */
		if((temp_as.time_stamp<=t1 || temp_as==subject.as_list.get(0)) && (temp_as.entry_type!=AS_NO_DATA && temp_as.entry_type!=AS_PROGRAM_END && temp_as.entry_type!=AS_PROGRAM_START)){
			subject.last_known_state=temp_as.entry_type;

            // TODO DEBUG
//			System.out.printf("SETTING LAST KNOWN STATE=%d<br>\n",subject.last_known_state);
		        }

		/* skip this entry if it occurs before the starting point of the graph */
		if(temp_as.time_stamp<=t1){
            // TODO DEBUG
//			System.out.printf("SKIPPING PRE-EVENT: %d @ %d<br>\n",temp_as.entry_type,temp_as.time_stamp);

			last_as=temp_as;
			continue;
		        }

		/* graph this span if we're not on the first item */
		if(last_as!=null){

			a=last_as.time_stamp;
			b=temp_as.time_stamp;

			/* we've already passed the last time displayed in the graph */
			if(a>t2)
				break;

			/* only graph this data if its on the graph */
			else if(b>t1){
				
				/* clip last time if it exceeds graph limits */
				if(b>t2)
					b=t2;

				/* clip first time if it precedes graph limits */
				if(a<t1)
					a=t1;

				/* save this time if its the earliest we've graphed */
				if(a<subject.earliest_time){
					subject.earliest_time=a;
					subject.earliest_state=last_as.entry_type;
				        }

				/* save this time if its the latest we've graphed */
				if(b>subject.latest_time){
					subject.latest_time=b;
					subject.latest_state=last_as.entry_type;
				        }

				/* compute availability times for this chunk */
				compute_subject_availability_times(last_as.entry_type,temp_as.entry_type,last_as.time_stamp,a,b,subject,temp_as);

				/* return if we've reached the end of the graph limits */
				if(b>=t2){
					last_as=temp_as;
					break;
				        }
			        }
                        }

		
		/* keep track of the last item */
		last_as=temp_as;
	        }


// TODO DEBUG
//    System.out.printf("--- END SECTION ---<BR>\n");

	/**********************************/
	/*           END SECTION          */
	/**********************************/

	if(last_as!=null){

		/* don't process an entry that is beyond the limits of the graph */
		if(last_as.time_stamp<t2){

			current_time = utils.currentTimeInSeconds();
			b=current_time;
			if(b>t2)
				b=t2;

			a=last_as.time_stamp;
			if(a<t1)
				a=t1;

			/* fake the current state (it doesn't really matter for graphing) */
			if(subject.type==HOST_SUBJECT)
				current_state=AS_HOST_UP;
			else
				current_state=AS_SVC_OK;

			/* compute availability times for last state */
			compute_subject_availability_times(last_as.entry_type,current_state,last_as.time_stamp,a,b,subject,last_as);
		        }
	        }


	return;
        }


/* computes availability times */
public static void compute_subject_availability_times(int first_state,int last_state,long real_start_time,long start_time,long end_time,avail_subject subject, archived_state as){
	int start_state;
	int end_state;
	long state_duration;
	Calendar t;
	long midnight_today;
	int weekday;
//	objects_h.timerange temp_timerange;
	long temp_duration;
	long temp_end;
	long temp_start;
	long start;
	long end;

// TODO DEBUG
//	if(subject.type==HOST_SUBJECT)
//		System.out.printf("HOST '%s'...\n",subject.host_name);
//	else
//		System.out.printf("SERVICE '%s' ON HOST '%s'...\n",subject.service_description,subject.host_name);
//
//	System.out.printf("COMPUTING %d.%d FROM %d to %d (%d seconds) FOR %s<br>\n",first_state,last_state,start_time,end_time,(end_time-start_time),(subject.type==HOST_SUBJECT)?"HOST":"SERVICE");

	/* clip times if necessary */
	if(start_time<t1)
		start_time=t1;
	if(end_time>t2)
		end_time=t2;

	/* make sure this is a valid time */
	if(start_time>t2)
		return;
	if(end_time<t1)
		return;

	/* MickeM - attempt to handle the current time_period (if any) */
	if(current_timeperiod!=null){
		t = Calendar.getInstance();
        t.setTimeInMillis(start_time * 1000);
		state_duration = 0;

		/* calculate the start of the day (midnight, 00:00 hours) */
		t.set( Calendar.SECOND , 0 );
		t.set( Calendar.MINUTE, 0 );
		t.set( Calendar.HOUR, 0 );
		midnight_today= utils.getTimeInSeconds(t);
		weekday = t.get( Calendar.DAY_OF_WEEK )-1;

		while(midnight_today<end_time){
			temp_duration=0;
			temp_end=Math.min(86400,end_time-midnight_today);
			temp_start=0;
			if(start_time>midnight_today)
				temp_start=start_time-midnight_today;
// TODO DEBUG
//			System.out.printf("<b>Matching: %ld . %ld. (%ld . %ld)</b><br>\n",temp_start, temp_end, midnight_today+temp_start, midnight_today+temp_end);

            /* check all time ranges for this day of the week */
			for( objects_h.timerange temp_timerange : (ArrayList<objects_h.timerange>) current_timeperiod.days[weekday] ){
					
//            TODO DEBUG
//				System.out.printf("<li>Matching in timerange[%d]: %d . %d (%ld . %ld)<br>\n",weekday,temp_timerange.range_start,temp_timerange.range_end,temp_start,temp_end);

				start=Math.max(temp_timerange.range_start,temp_start);
				end=Math.min(temp_timerange.range_end,temp_end);

				if(start<end){
					temp_duration+=end-start;
//                   TODO DEBUG
//					System.out.printf("<li>Matched time: %ld . %ld = %d<br>\n",start, end, temp_duration);

				        } 
//               TODO DEBUG
//				else
//					System.out.printf("<li>Ignored time: %ld . %ld<br>\n",start, end);
			        }
			state_duration+=temp_duration;
			temp_start=0;
			midnight_today+=86400;
			if(++weekday>6)
				weekday=0;
		        }
		}

	/* no report timeperiod was selected (assume 24x7) */
	else{
		/* calculate time in this state */
		state_duration=(end_time-start_time);
	        }

	/* can't graph if we don't have data... */
	if(first_state==AS_NO_DATA || last_state==AS_NO_DATA){
		subject.time_indeterminate_nodata+=state_duration;
		return;
	        }
	if(first_state==AS_PROGRAM_START && (last_state==AS_PROGRAM_END || last_state==AS_PROGRAM_START)){
		if(assume_initial_states==common_h.FALSE){
			subject.time_indeterminate_nodata+=state_duration;
			return;
		        }
	        }
	if(first_state==AS_PROGRAM_END){

		/* added 7/24/03 */
		if(assume_states_during_notrunning==common_h.TRUE){
			first_state=subject.last_known_state;
		        }
		else{
			subject.time_indeterminate_notrunning+=state_duration;
			return;
		        }
	        }

	/* special case if first entry was program start */
	if(first_state==AS_PROGRAM_START){

		if(assume_initial_states==common_h.TRUE){

			if(assume_state_retention==common_h.TRUE)
				start_state=subject.last_known_state;

			else{
				if(subject.type==HOST_SUBJECT)
					start_state=AS_HOST_UP;
				else
					start_state=AS_SVC_OK;
			        }
		        }
		else
			return;
	        }
	else{
		start_state=first_state;
		subject.last_known_state=first_state;
	        }

	/* special case if last entry was program stop */
	if(last_state==AS_PROGRAM_END)
		end_state=first_state;
	else
		end_state=last_state;

	/* save "processed state" info */
	as.processed_state=start_state;

//   TODO DEBUG
//	System.out.printf("PASSED TIME CHECKS, CLIPPED VALUES: START=%d, END=%d\n",start_time,end_time);


	/* add time in this state to running totals */
	switch(start_state){
	case AS_HOST_UP:
		subject.time_up+=state_duration;
		break;
	case AS_HOST_DOWN:
		subject.time_down+=state_duration;
		break;
	case AS_HOST_UNREACHABLE:
		subject.time_unreachable+=state_duration;
		break;
	case AS_SVC_OK:
		subject.time_ok+=state_duration;
		break;
	case AS_SVC_WARNING:
		subject.time_warning+=state_duration;
		break;
	case AS_SVC_UNKNOWN:
		subject.time_unknown+=state_duration;
		break;
	case AS_SVC_CRITICAL:
		subject.time_critical+=state_duration;
		break;
	default:
		break;
                }

	return;
        }


/* computes downtime data for a given subject */
public static void compute_subject_downtime(avail_subject subject, long current_time){
	archived_state temp_sd;
	long start_time;
	long end_time;
	int host_downtime_depth=0;
	int service_downtime_depth=0;
	int process_chunk=common_h.FALSE;

    // TODO DEBUG
//	System.out.printf("COMPUTE_SUBJECT_DOWNTIME\n");

	/* if left hand of graph is after current time, we can't do anything at all.... */
	if(t1>current_time)
		return;

	/* no scheduled downtime data for subject... */
	if(subject.sd_list==null || subject.sd_list.isEmpty() )
		return;

	/* all data we have occurs after last time on graph... */
	if(subject.sd_list.get(0).time_stamp>=t2)
		return;

	/* initialize pointer */
    Iterator<archived_state> iter = subject.sd_list.iterator();
	temp_sd = iter.next();

	/* special case if first entry is the end of scheduled downtime */
	if((temp_sd.entry_type==AS_HOST_DOWNTIME_END || temp_sd.entry_type==AS_SVC_DOWNTIME_END) && temp_sd.time_stamp>t1){

        // TODO DEBUG
//		System.out.printf("\tSPECIAL DOWNTIME CASE\n");
		start_time=t1;
		end_time=(temp_sd.time_stamp>t2)?t2:temp_sd.time_stamp;
		compute_subject_downtimes(start_time,end_time,subject,null);
	        }

	/* process all periods of scheduled downtime */
    while ( iter.hasNext() ) { 
	   temp_sd=iter.next();

		/* we've passed graph bounds... */
		if(temp_sd.time_stamp>=t2)
			break;

		if(temp_sd.entry_type==AS_HOST_DOWNTIME_START)
			host_downtime_depth++;
		else if(temp_sd.entry_type==AS_HOST_DOWNTIME_END)
			host_downtime_depth--;
		else if(temp_sd.entry_type==AS_SVC_DOWNTIME_START)
			service_downtime_depth++;
		else if(temp_sd.entry_type==AS_SVC_DOWNTIME_END)
			service_downtime_depth--;
		else
			continue;

		process_chunk=common_h.FALSE;
		if(temp_sd.entry_type==AS_HOST_DOWNTIME_START || temp_sd.entry_type==AS_SVC_DOWNTIME_START)
			process_chunk=common_h.TRUE;
		else if(subject.type==SERVICE_SUBJECT && (host_downtime_depth>0 || service_downtime_depth>0))
			process_chunk=common_h.TRUE;

		/* process this specific "chunk" of scheduled downtime */
		if(process_chunk==common_h.TRUE){

			start_time=temp_sd.time_stamp;
            // TODO this is not good, need to think of traversing list another way.
			end_time=( !iter.hasNext() )?current_time: subject.sd_list.get( subject.sd_list.indexOf(temp_sd) + 1 ).time_stamp;

			/* check time sanity */
			if(end_time<=t1)
				continue;
			if(start_time>=t2)
				continue;
			if(start_time>=end_time)
				continue;

			/* clip time values */
			if(start_time<t1)
				start_time=t1;
			if(end_time>t2)
				end_time=t2;

			compute_subject_downtimes(start_time,end_time,subject,temp_sd);
		        }
	        }

	return;
        }



/* computes downtime times */
public static void compute_subject_downtimes(long start_time, long end_time, avail_subject subject, archived_state sd){
    archived_state temp_as = null;
    archived_state last_as = null;
    archived_state prev_as = null;
	long part_start_time;
	int part_subject_state;
    int count=0;
    int save_status = 0;
    int saved_status=0;
    long saved_stamp=0;

    if ( logger.isDebugEnabled() ) 
       System.out.printf("<P><b>ENTERING COMPUTE_SUBJECT_DOWNTIME_TIMES: start=%lu, end=%lu, t1=%lu, t2=%lu </b></P>",start_time,end_time,t1,t2);

	/* times are weird, so bail out... */
	if(start_time>end_time)
		return;
	if(start_time<t1 || end_time>t2)
		return;

    ListIterator<archived_state> iter = null;
    
	/* find starting point in archived state list */
	if(sd==null){

        if ( logger.isDebugEnabled() ) 
           System.out.printf("<P>TEMP_AS=SUBJECT->AS_LIST </P>");

        // TODO slight concern that there is nothing in here?
	    iter = subject.as_list.listIterator();
		if ( iter.hasNext() ) temp_as = iter.next();
	}
	else if(sd.misc_ptr==null){
	   if ( logger.isDebugEnabled() ) 
	      System.out.printf("<P>TEMP_AS=SUBJECT->AS_LIST</P>");
	   
	   iter = subject.as_list.listIterator();
	   if ( iter.hasNext() ) 
	      temp_as = iter.next();
	}
	else {
	   iter = subject.as_list.listIterator( subject.as_list.indexOf( sd.misc_ptr ) );
	   temp_as = iter.next();
	   if ( iter.hasNext() ) {
	      if ( logger.isDebugEnabled() ) 
	         System.out.printf("<P>TEMP_AS=SD->MISC_PTR->NEXT</P>");
	         temp_as = iter.next();
	   } else {
	      if ( logger.isDebugEnabled() ) 
	         System.out.printf("<P>TEMP_AS=SD->MISC_PTR</P>");
	   }
	}
	
	/* initialize values */
	part_start_time=start_time;
	if(temp_as==null)
	   part_subject_state=AS_NO_DATA;
	else if(temp_as.processed_state==AS_PROGRAM_START || temp_as.processed_state==AS_PROGRAM_END || temp_as.processed_state==AS_NO_DATA){
	   if ( logger.isDebugEnabled() ) 
	      System.out.printf("<P>ENTRY TYPE #1: %d</P>",temp_as.entry_type);
	   part_subject_state=AS_NO_DATA;
	}
	else{
	   if ( logger.isDebugEnabled() ) 
	      System.out.printf("<P>ENTRY TYPE #2: %d</P>",temp_as.entry_type);
	   
	   part_subject_state=temp_as.processed_state;
	}

	if ( logger.isDebugEnabled() ) {
	   System.out.printf("<P>TEMP_AS=%s</P>",(temp_as==null)?"NULL":"Not NULL");
       System.out.printf("<P>SD=%s</P>",(sd==null)?"NULL":"Not NULL");
	}
       
	/* temp_as now points to first event to possibly "break" this chunk */
	for( ; iter.hasNext();temp_as=iter.next()){
	   count++;
       last_as = temp_as;
       
	   if ( prev_as == null ) {
	      if ( temp_as.time_stamp > start_time ) {
	         if ( temp_as.time_stamp> end_time )
	            compute_subject_downtime_part_times(start_time,end_time,part_subject_state,subject);
	         else
	            compute_subject_downtime_part_times(start_time,temp_as.time_stamp,part_subject_state,subject);
	      }
	      
          prev_as = temp_as;
          saved_status = temp_as.entry_type;
          saved_stamp = temp_as.time_stamp;
          
          /* check if first time is before schedule downtime */
          if(saved_stamp<start_time)
             saved_stamp = start_time;
          
          continue;          
	   }
          
	   /* if status changed, we have to calculate */
	   if ( saved_status != temp_as.entry_type ) {
	      /* is outside schedule time, use end schdule downtime */
	      if(temp_as.time_stamp>end_time){ 
	         if(saved_stamp<start_time)
	            compute_subject_downtime_part_times(start_time,end_time,saved_status,subject);
	         else
	            compute_subject_downtime_part_times(saved_stamp,end_time,saved_status,subject);
	      }
	      else{
	         if(saved_stamp<start_time)
	            compute_subject_downtime_part_times(start_time,temp_as.time_stamp,saved_status,subject);
	         else
	            compute_subject_downtime_part_times(saved_stamp,temp_as.time_stamp,saved_status,subject);
	      }
	      saved_status=temp_as.entry_type;
	      saved_stamp=temp_as.time_stamp;            
	   }
    }
	   /* just one entry inside the scheduled downtime */
	   if(count==0)
	      compute_subject_downtime_part_times(start_time,end_time,part_subject_state,subject);
	   else {
	      /* is outside scheduled time, use end schdule downtime */
	      if(last_as.time_stamp>end_time) 
	         compute_subject_downtime_part_times(saved_stamp,end_time,saved_status,subject);
	      else
	         compute_subject_downtime_part_times(saved_stamp,last_as.time_stamp,saved_status,subject);
	   }       
       
       return;

}

/* computes downtime times */
public static void compute_subject_downtime_part_times(long start_time, long end_time, int subject_state, avail_subject subject){
	long state_duration;

    // TODO DEBUG
//	System.out.printf("ENTERING COMPUTE_SUBJECT_DOWNTIME_PART_TIMES\n");

	/* times are weird */
	if(start_time>end_time)
		return;

	state_duration=(end_time-start_time);

	switch(subject_state){
	case AS_HOST_UP:
		subject.scheduled_time_up+=state_duration;
		break;
	case AS_HOST_DOWN:
		subject.scheduled_time_down+=state_duration;
		break;
	case AS_HOST_UNREACHABLE:
		subject.scheduled_time_unreachable+=state_duration;
		break;
	case AS_SVC_OK:
		subject.scheduled_time_ok+=state_duration;
		break;
	case AS_SVC_WARNING:
		subject.scheduled_time_warning+=state_duration;
		break;
	case AS_SVC_UNKNOWN:
		subject.scheduled_time_unknown+=state_duration;
		break;
	case AS_SVC_CRITICAL:
		subject.scheduled_time_critical+=state_duration;
		break;
	default:
		subject.scheduled_time_indeterminate+=state_duration;
		break;
	        }

    // TODO DEBUG
//	System.out.printf("\tSUBJECT DOWNTIME: Host '%s', Service '%s', State=%d, Duration=%d, Start=%d\n",subject.host_name,(subject.service_description==null)?"null":subject.service_description,subject_state,state_duration,start_time);

	return;
        }



/* convert current host state to archived state value */
public static int convert_host_state_to_archived_state(int current_status){

	if(current_status==statusdata_h.HOST_UP)
		return AS_HOST_UP;
	if(current_status==statusdata_h.HOST_DOWN)
		return AS_HOST_DOWN;
	if(current_status==statusdata_h.HOST_UNREACHABLE)
		return AS_HOST_UNREACHABLE;

	return AS_NO_DATA;
        }


/* convert current service state to archived state value */
public static int convert_service_state_to_archived_state(int current_status){

	if(current_status==statusdata_h.SERVICE_OK)
		return AS_SVC_OK;
	if(current_status==statusdata_h.SERVICE_UNKNOWN)
		return AS_SVC_UNKNOWN;
	if(current_status==statusdata_h.SERVICE_WARNING)
		return AS_SVC_WARNING;
	if(current_status==statusdata_h.SERVICE_CRITICAL)
		return AS_SVC_CRITICAL;

	return AS_NO_DATA;
        }



/* create list of subjects to collect availability data for */
public static void create_subject_list(){
//	objects_h.hostgroup temp_hostgroup;
//	objects_h.hostgroupmember temp_hgmember;
//	objects_h.servicegroup temp_servicegroup;
//	objects_h.servicegroupmember temp_sgmember;
//	objects_h.host temp_host;
//	objects_h.service temp_service;
	String last_host_name="";

	/* we're displaying one or more hosts */
	if(display_type==DISPLAY_HOST_AVAIL && host_name!=""){

		/* we're only displaying a specific host (and summaries for all services associated with it) */
		if(show_all_hosts==common_h.FALSE){
			add_subject(HOST_SUBJECT,host_name,null);
			for( objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ){
				if(temp_service.host_name.equals(host_name))
					add_subject(SERVICE_SUBJECT,host_name,temp_service.description);
			        }
		        }

		/* we're displaying all hosts */
		else{
			for( objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list )
				add_subject(HOST_SUBJECT,temp_host.name,null);
		        }
	        }

	/* we're displaying a specific service */
	else if(display_type==DISPLAY_SERVICE_AVAIL && svc_description!=""){

		/* we're only displaying a specific service */
		if(show_all_services==common_h.FALSE)
			add_subject(SERVICE_SUBJECT,host_name,svc_description);

		/* we're displaying all services */
		else{
			for( objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list )
				add_subject(SERVICE_SUBJECT,temp_service.host_name,temp_service.description);
		        }
	        }

	/* we're displaying one or more hostgroups (the host members of the groups) */
	else if(display_type==DISPLAY_HOSTGROUP_AVAIL && hostgroup_name!=""){

		/* we're displaying all hostgroups */
		if(show_all_hostgroups==common_h.TRUE){
			for( objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list ){
				for( objects_h.hostgroupmember temp_hgmember : (ArrayList<objects_h.hostgroupmember>) temp_hostgroup.members )
					add_subject(HOST_SUBJECT,temp_hgmember.host_name,null);
			        }
		        }
		/* we're only displaying a specific hostgroup */
		else{
			objects_h.hostgroup temp_hostgroup=objects.find_hostgroup(hostgroup_name);
			if(temp_hostgroup!=null){
				for( objects_h.hostgroupmember temp_hgmember : (ArrayList<objects_h.hostgroupmember>) temp_hostgroup.members )
					add_subject(HOST_SUBJECT,temp_hgmember.host_name,null);
			        }
		        }
	        }

	/* we're displaying one or more servicegroups (the host and service members of the groups) */
	else if(display_type==DISPLAY_SERVICEGROUP_AVAIL && servicegroup_name!=""){

		/* we're displaying all servicegroups */
		if(show_all_servicegroups==common_h.TRUE){
			for(objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>) objects.servicegroup_list ){
				for( objects_h.servicegroupmember temp_sgmember : (ArrayList<objects_h.servicegroupmember>) temp_servicegroup.members ){
					add_subject(SERVICE_SUBJECT,temp_sgmember.host_name,temp_sgmember.service_description);
					if( ! last_host_name.equals(temp_sgmember.host_name))
						add_subject(HOST_SUBJECT,temp_sgmember.host_name,null);
					last_host_name=temp_sgmember.host_name;
				        }
			        }
		        }
		/* we're only displaying a specific servicegroup */
		else{
            objects_h.servicegroup temp_servicegroup= objects.find_servicegroup(servicegroup_name);
			if(temp_servicegroup!=null){
				for( objects_h.servicegroupmember temp_sgmember : (ArrayList<objects_h.servicegroupmember>) temp_servicegroup.members ){
					add_subject(SERVICE_SUBJECT,temp_sgmember.host_name,temp_sgmember.service_description);
					if(!last_host_name.equals(temp_sgmember.host_name))
						add_subject(HOST_SUBJECT,temp_sgmember.host_name,null);
					last_host_name=temp_sgmember.host_name;
				        }
			        }
		        }
	        }

	return;
        }



/* adds a subject */
public static void add_subject(int subject_type, String hn, String sd){
//	avail_subject last_subject=null;
//	avail_subject temp_subject=null;
	avail_subject new_subject=null;
	int is_authorized=common_h.FALSE;

	/* bail if we've already added the subject */
	if(find_subject(subject_type,hn,sd) != null)
		return;

	/* see if the user is authorized to see data for this host or service */
	if(subject_type==HOST_SUBJECT)
		is_authorized=cgiauth.is_authorized_for_host(objects.find_host(hn),current_authdata);
	else
		is_authorized=cgiauth.is_authorized_for_service(objects.find_service(hn,sd),current_authdata);
	if(is_authorized==common_h.FALSE)
		return;

	/* allocate memory for the new entry */
	new_subject= new avail_subject();

	/* allocate memory for the host name */
	if(hn!=null){
			new_subject.host_name = hn;
	        }
	else 
		new_subject.host_name=null;

	/* allocate memory for the service description */
	if(sd!=null){
			new_subject.service_description=sd;
    }
	else
		new_subject.service_description=null;

	new_subject.type=subject_type;
        new_subject.earliest_state=AS_NO_DATA;
	new_subject.latest_state=AS_NO_DATA;
	new_subject.time_up=0L;
	new_subject.time_down=0L;
	new_subject.time_unreachable=0L;
	new_subject.time_ok=0L;
	new_subject.time_warning=0L;
	new_subject.time_unknown=0L;
	new_subject.time_critical=0L;
	new_subject.scheduled_time_up=0L;
	new_subject.scheduled_time_down=0L;
	new_subject.scheduled_time_unreachable=0L;
	new_subject.scheduled_time_ok=0L;
	new_subject.scheduled_time_warning=0L;
	new_subject.scheduled_time_unknown=0L;
	new_subject.scheduled_time_critical=0L;
	new_subject.scheduled_time_indeterminate=0L;
	new_subject.time_indeterminate_nodata=0L;
	new_subject.time_indeterminate_notrunning=0L;
//	new_subject.as_list=null;
//	new_subject.sd_list=null;
	new_subject.last_known_state=AS_NO_DATA;

	/* add the new entry to the list in memory, sorted by host name */
    boolean added = false;
	for( avail_subject temp_subject : subject_list ){
		if( new_subject.host_name.compareTo(temp_subject.host_name) < 0 ){
            subject_list.add( subject_list.indexOf(temp_subject) , new_subject );
			break;
		        }
	        }
	if ( !added )
        subject_list.add( new_subject );
    
        }

/* finds a specific subject */
public static avail_subject find_subject(int type, String hn, String sd){

	if(hn==null)
		return null;

	if(type==SERVICE_SUBJECT && sd==null)
		return null;

	for(avail_subject temp_subject : subject_list ) {
		if(temp_subject.type!=type)
			continue;
		if(! hn.equals(temp_subject.host_name))
			continue;
		if(type==SERVICE_SUBJECT && !sd.equals(temp_subject.service_description))
			continue;
		return temp_subject;
	        }

	return null;
        }

/* adds an archived state entry to all subjects */
public static void add_global_archived_state(int entry_type, int state_type, long time_stamp, String state_info) {

	for( avail_subject temp_subject : subject_list )
		add_archived_state(entry_type,state_type,time_stamp,state_info,temp_subject);

        }

/* adds an archived state entry to a specific subject */
public static void add_archived_state(int entry_type, int state_type, long time_stamp, String state_info, avail_subject subject){

    /* allocate memory for the new entry */
    archived_state new_as= new archived_state();

	/* allocate memory for the state info */
	new_as.state_info = state_info;

	/* initialize the "processed state" value - this gets modified later for most entries */
	if(entry_type!=AS_PROGRAM_START && entry_type!=AS_PROGRAM_END && entry_type!=AS_NO_DATA)
		new_as.processed_state=entry_type;
	else
		new_as.processed_state=AS_NO_DATA;

	new_as.entry_type=entry_type;
	new_as.state_type=state_type;
	new_as.time_stamp=time_stamp;
	new_as.misc_ptr=null;

	/* add the new entry to the list in memory, sorted by time (more recent entries should appear towards end of list) */
	boolean added = false;
	for( archived_state temp_as : subject.as_list ){
		if(new_as.time_stamp<temp_as.time_stamp){
			subject.as_list.add( subject.as_list.indexOf( temp_as ) , new_as );
            added = true;
			break;
		        }
	        }
	if(!added)
        subject.as_list.add( new_as );

    /* update "tail" of the list - not really the tail, just last item added */
    subject.as_list_tail=new_as;

}


/* adds a scheduled downtime entry to a specific subject */
public static void add_scheduled_downtime(int state_type, long time_stamp, avail_subject subject){
	/* allocate memory for the new entry */
    archived_state new_sd = new archived_state ();

	new_sd.state_info=null;
	new_sd.processed_state=state_type;
	new_sd.entry_type=state_type;
	new_sd.time_stamp=time_stamp;
	new_sd.misc_ptr=subject.as_list_tail;

	/* add the new entry to the list in memory, sorted by time (more recent entries should appear towards end of list) */
	boolean added = false;
	for( archived_state temp_sd : subject.sd_list ){
		if(new_sd.time_stamp<=temp_sd.time_stamp){
		    subject.sd_list.add( subject.sd_list.indexOf(temp_sd), new_sd );
            added = true;
			break;
		        }
	        }
	if( !added )
	    subject.sd_list.add( new_sd );
}

/* frees memory allocated to all availability data */
public static void free_availability_data(){
    subject_list.clear();
}

/* reads log files for archived state data */
public static void read_archived_state_data(){
	String filename;
	int oldest_archive=0;
	int newest_archive=0;
	int current_archive=0;

	/* determine oldest archive to use when scanning for data (include backtracked archives as well) */
	oldest_archive = cgiutils.determine_archive_to_use_from_time(t1);
	if(blue.log_rotation_method!=common_h.LOG_ROTATION_NONE)
		oldest_archive+=backtrack_archives;

	/* determine most recent archive to use when scanning for data */
	newest_archive=cgiutils.determine_archive_to_use_from_time(t2);

	if(oldest_archive<newest_archive)
		oldest_archive=newest_archive;

	/* read in all the necessary archived logs (from most recent to earliest) */
	for(current_archive=newest_archive;current_archive<=oldest_archive;current_archive++){

//    TODO DEBUG
//		System.out.printf("Reading archive #%d\n",current_archive);

		/* get the name of the log file that contains this archive */
        filename = cgiutils.get_log_archive_to_use(current_archive);

//       TODO DEBUG
//		System.out.printf("Archive name: '%s'\n",filename);


		/* scan the log file for archived state data */
		scan_log_file_for_archived_state_data(filename);
	        }

	return;
        }



/* grabs archives state data from a log file */
public static void scan_log_file_for_archived_state_data(String filename){
	String input=null;
	String entry_host_name;
	String entry_svc_description;
	String plugin_output;
	String temp_buffer;
	long time_stamp;
	cgiutils_h.mmapfile thefile;
//	avail_subject temp_subject;
	int state_type = 0;

	if((thefile=cgiutils.mmap_fopen(filename))==null)
		return;

	while(true){

		/* read the next line */
		if((input=cgiutils.mmap_fgets(thefile))==null)
			break;

		input = input.trim();

        String[] split = input.split( "[\\]]" , 2 );
//        temp_buffer = my_strtok(input2,"]");
		time_stamp=( split[0].trim().length() <= 1 )?0: strtoul( split[0].substring(1),null,10);
        if ( split.length == 2 )
            temp_buffer = split[1];
        else 
            temp_buffer = "";

		/* program starts/restarts */
		if(input.contains(" starting..."))
			add_global_archived_state(AS_PROGRAM_START,AS_NO_DATA,time_stamp,"Program start");
		if(input.contains(" restarting..."))
			add_global_archived_state(AS_PROGRAM_START,AS_NO_DATA,time_stamp,"Program restart");

		/* program stops */
		if(input.contains(" shutting down..."))
			add_global_archived_state(AS_PROGRAM_END,AS_NO_DATA,time_stamp,"Normal program termination");
		if(input.contains("Bailing out"))
			add_global_archived_state(AS_PROGRAM_END,AS_NO_DATA,time_stamp,"Abnormal program termination");

		if(display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_HOSTGROUP_AVAIL || display_type==DISPLAY_SERVICEGROUP_AVAIL){

			/* normal host alerts and initial/current states */
			if(input.contains("HOST ALERT:") || input.contains("INITIAL HOST STATE:") || input.contains("CURRENT HOST STATE:")){

				/* get host name */
                temp_buffer = split ( split, "[:]" ); 
				temp_buffer = split ( split, "[;]" );
				entry_host_name = (temp_buffer==null)?"": temp_buffer.substring(1) ;

				/* see if there is a corresponding subject for this host */
                avail_subject temp_subject=find_subject(HOST_SUBJECT,entry_host_name,null);
				if(temp_subject==null)
					continue;

				/* state types */
				if(input.contains(";SOFT;")){
					if(include_soft_states==common_h.FALSE)
						continue;
					state_type=AS_SOFT_STATE;
				        }
				if(input.contains(";HARD;"))
					state_type=AS_HARD_STATE;

				/* get the plugin output */
                temp_buffer = split ( split, "[;]" );
                temp_buffer = split ( split, "[;]" );
                temp_buffer = split ( split, "[;]" );
                plugin_output = split ( split, "[\\n]" );

				if(input.contains(";DOWN;"))
					add_archived_state(AS_HOST_DOWN,state_type,time_stamp,plugin_output,temp_subject);
				else if(input.contains(";UNREACHABLE;"))
					add_archived_state(AS_HOST_UNREACHABLE,state_type,time_stamp,plugin_output,temp_subject);
				else if(input.contains(";RECOVERY") || input.contains(";UP;"))
					add_archived_state(AS_HOST_UP,state_type,time_stamp,plugin_output,temp_subject);
				else
					add_archived_state(AS_NO_DATA,AS_NO_DATA,time_stamp,plugin_output,temp_subject);
			        }

			/* scheduled downtime notices */
			else if(input.contains("HOST DOWNTIME ALERT:")){

				/* get host name */
                temp_buffer = split ( split, "[:]" );
                temp_buffer = split ( split, "[;]" );
				entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1) ;

				/* see if there is a corresponding subject for this host */
                avail_subject temp_subject=find_subject(HOST_SUBJECT,entry_host_name,null);
				if(temp_subject==null)
					continue;

				if(show_scheduled_downtime==common_h.FALSE)
					continue;
			
				if(input.contains(";STARTED;"))
					add_scheduled_downtime(AS_HOST_DOWNTIME_START,time_stamp,temp_subject);
				else
					add_scheduled_downtime(AS_HOST_DOWNTIME_END,time_stamp,temp_subject);

			        }
		        }

		if(display_type==DISPLAY_SERVICE_AVAIL || display_type==DISPLAY_HOST_AVAIL || display_type==DISPLAY_SERVICEGROUP_AVAIL){

			/* normal service alerts and initial/current states */
			if(input.contains("SERVICE ALERT:") || input.contains("INITIAL SERVICE STATE:") || input.contains("CURRENT SERVICE STATE:")){

				/* get host name */
                temp_buffer = split ( split, "[:]" );
                temp_buffer = split ( split, "[;]" );
				entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);

				/* get service description */
                temp_buffer = split ( split, "[;]" );
				entry_svc_description = (temp_buffer==null)?"":temp_buffer;

				/* see if there is a corresponding subject for this service */
                avail_subject temp_subject=find_subject(SERVICE_SUBJECT,entry_host_name,entry_svc_description);
				if(temp_subject==null)
					continue;

				/* state types */
				if(input.contains(";SOFT;")){
					if(include_soft_states==common_h.FALSE)
						continue;
					state_type=AS_SOFT_STATE;
				        }
				if(input.contains(";HARD;"))
					state_type=AS_HARD_STATE;

				/* get the plugin output */
                temp_buffer = split ( split, "[;]" );
                temp_buffer = split ( split, "[;]" );
                temp_buffer = split ( split, "[;]" );
                plugin_output = split ( split, "[;]" );

				if(input.contains(";CRITICAL;"))
					add_archived_state(AS_SVC_CRITICAL,state_type,time_stamp,plugin_output,temp_subject);
				else if(input.contains(";WARNING;"))
					add_archived_state(AS_SVC_WARNING,state_type,time_stamp,plugin_output,temp_subject);
				else if(input.contains(";UNKNOWN;"))
					add_archived_state(AS_SVC_UNKNOWN,state_type,time_stamp,plugin_output,temp_subject);
				else if( input.contains(";RECOVERY;") ||  input.contains(";OK;"))
					add_archived_state(AS_SVC_OK,state_type,time_stamp,plugin_output,temp_subject);
				else
					add_archived_state(AS_NO_DATA,AS_NO_DATA,time_stamp,plugin_output,temp_subject);

			        }

			/* scheduled service downtime notices */
			else if( input.contains("SERVICE DOWNTIME ALERT:")){

				/* get host name */
                temp_buffer = split ( split, "[:]" );
                temp_buffer = split ( split, "[;]" );
				entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);

				/* get service description */
                temp_buffer = split ( split, "[;]" );
				entry_svc_description = (temp_buffer==null)?"":temp_buffer;

				/* see if there is a corresponding subject for this service */
                avail_subject temp_subject=find_subject(SERVICE_SUBJECT,entry_host_name,entry_svc_description);
				if(temp_subject==null)
					continue;

				if(show_scheduled_downtime==common_h.FALSE)
					continue;
			
				if( input.contains(";STARTED;"))
					add_scheduled_downtime(AS_SVC_DOWNTIME_START,time_stamp,temp_subject);
				else
					add_scheduled_downtime(AS_SVC_DOWNTIME_END,time_stamp,temp_subject);
		                }

			/* scheduled host downtime notices */
            else if ( input.contains( "HOST DOWNTIME ALERT:" ) ) {

				/* get host name */
                temp_buffer = split ( split, "[:]" );
                temp_buffer = split ( split, "[;]" );
				entry_host_name = (temp_buffer==null)?"":temp_buffer.substring(1);

				/* this host downtime entry must be added to all service subjects associated with the host! */
				for( avail_subject temp_subject : subject_list ){
					
					if(temp_subject.type!=SERVICE_SUBJECT)
						continue;

					if( ! temp_subject.host_name.equals(entry_host_name))
						continue;

					if(show_scheduled_downtime==common_h.FALSE)
						continue;
			
					if( input.contains(";STARTED;"))
						add_scheduled_downtime(AS_HOST_DOWNTIME_START,time_stamp,temp_subject);
					else
						add_scheduled_downtime(AS_HOST_DOWNTIME_END,time_stamp,temp_subject);
				        }
			        }
		        }
		
	        }

	/* free memory and close the file */
	cgiutils.mmap_fclose(thefile);
	
	return;
        }
	



public static void convert_timeperiod_to_times(int type){
	long current_time;
	Calendar t;

	/* get the current time */
	current_time = utils.currentTimeInSeconds();

	t=  Calendar.getInstance();

	t.set( Calendar.SECOND, 0 );
	t.set( Calendar.MINUTE, 0 );
	t.set( Calendar.HOUR, 0);

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
		t1=(utils.getTimeInSeconds(t)-(60*60*24*(t.get( Calendar.DAY_OF_WEEK )-1) ));
		t2=current_time;
		break;
	case TIMEPERIOD_LASTWEEK:
		t1=( utils.getTimeInSeconds(t) -(60*60*24*(t.get( Calendar.DAY_OF_WEEK )-1))-(60*60*24*7));
		t2=( utils.getTimeInSeconds(t) -(60*60*24*(t.get( Calendar.DAY_OF_WEEK )-1)));
		break;
	case TIMEPERIOD_THISMONTH:
		t.set( Calendar.DAY_OF_MONTH, 1);
		t1 = utils.getTimeInSeconds(t);
		t2=current_time;
		break;
	case TIMEPERIOD_LASTMONTH:
        t.set( Calendar.DAY_OF_MONTH, 1);
		t2 = utils.getTimeInSeconds(t);
		if(t.get( Calendar.MONTH )==0){
			t.set( Calendar.MONTH, 11 );
			t.roll( Calendar.YEAR, false );
		        }
		else
			t.roll( Calendar.MONTH, false ) ;
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
		t1 = utils.getTimeInSeconds(t);
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
   Calendar st;
   Calendar et;
   
   /* get the current time */
   current_time = utils.currentTimeInSeconds();
   
   st=Calendar.getInstance();
   st.setTimeInMillis( current_time * 1000 );
   
   st.set( Calendar.SECOND, start_second );
   st.set( Calendar.MINUTE, start_minute );
   st.set( Calendar.HOUR,start_hour );
   st.set( Calendar.DAY_OF_MONTH, start_day );
   st.set( Calendar.MONTH, start_month-1 );
   st.set( Calendar.YEAR, start_year );
   
   t1=  utils.getTimeInSeconds(st);
   
   et = Calendar.getInstance();
   et.setTimeInMillis( current_time * 1000 );
   
   et.set( Calendar.SECOND, end_second );
   et.set( Calendar.MINUTE, end_minute );
   et.set( Calendar.HOUR,end_hour );
   et.set( Calendar.DAY_OF_MONTH, end_day );
   et.set( Calendar.MONTH, end_month-1 );
   et.set( Calendar.YEAR, end_year );
   
   t2= utils.getTimeInSeconds(et);
}


/* writes log entries to screen */
public static void write_log_entries(avail_subject subject){
//	archived_state temp_as;
//	archived_state temp_sd;
	long current_time;
	String start_date_time;
	String end_date_time;
	String duration;
	String bgclass="";
	String ebgclass="";
	String entry_type="";
	String state_type="";
	int odd=0;


	if(output_format!=HTML_OUTPUT)
		return;

	if(show_log_entries==common_h.FALSE)
		return;

	if(subject==null)
		return;

	current_time = utils.currentTimeInSeconds();

	/* inject all scheduled downtime entries into the main list for display purposes */
	for(archived_state temp_sd : subject.sd_list ){
		switch(temp_sd.entry_type){
		case AS_SVC_DOWNTIME_START:
		case AS_HOST_DOWNTIME_START:
			entry_type="Start of scheduled downtime";
			break;
		case AS_SVC_DOWNTIME_END:
		case AS_HOST_DOWNTIME_END:
			entry_type="End of scheduled downtime";
			break;
		default:
			entry_type="?";
			break;
		        }
		add_archived_state(temp_sd.entry_type,AS_NO_DATA,temp_sd.time_stamp,entry_type,subject);
	        }


	System.out.printf("<BR><BR>\n");

	System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>%s Log Entries:</DIV>\n",(subject.type==HOST_SUBJECT)?"Host":"Service");

	System.out.printf("<DIV ALIGN=CENTER CLASS='infoMessage'>");
	if(full_log_entries==common_h.TRUE){
		full_log_entries=common_h.FALSE;
		if(subject.type==HOST_SUBJECT)
			host_report_url(subject.host_name,"[ View condensed log entries ]");
		else
			service_report_url(subject.host_name,subject.service_description,"[ View condensed log entries ]");
		full_log_entries=common_h.TRUE;
	        }
	else{
		full_log_entries=common_h.TRUE;
		if(subject.type==HOST_SUBJECT)
			host_report_url(subject.host_name,"[ View full log entries ]");
		else
			service_report_url(subject.host_name,subject.service_description,"[ View full log entries ]");
		full_log_entries=common_h.FALSE;
	        }
	System.out.printf("</DIV>\n");

	System.out.printf("<DIV ALIGN=CENTER>\n");

	System.out.printf("<table border=1 cellspacing=0 cellpadding=3 class='logEntries'>\n");
	System.out.printf("<tr><th class='logEntries'>Event Start Time</th><th class='logEntries'>Event End Time</th><th class='logEntries'>Event Duration</th><th class='logEntries'>Event/State Type</th><th class='logEntries'>Event/State Information</th></tr>\n");

	/* write all archived state entries */
    
	for( ListIterator<archived_state> iter = subject.as_list.listIterator(); iter.hasNext(); ){
        archived_state temp_as = iter.next();
        
		if(temp_as.state_type==AS_HARD_STATE)
			state_type=" (HARD)";
		else if(temp_as.state_type==AS_SOFT_STATE)
			state_type=" (SOFT)";
		else
			state_type="";

		switch(temp_as.entry_type){
		case AS_NO_DATA:
			if(full_log_entries==common_h.FALSE)
				continue;
			entry_type="NO DATA";
			ebgclass="INDETERMINATE";
			break;
		case AS_PROGRAM_END:
			if(full_log_entries==common_h.FALSE)
				continue;
			entry_type="PROGRAM END";
			ebgclass="INDETERMINATE";
			break;
		case AS_PROGRAM_START:
			if(full_log_entries==common_h.FALSE)
				continue;
			entry_type="PROGRAM (RE)START";
			ebgclass="INDETERMINATE";
			break;
		case AS_HOST_UP:
			entry_type="HOST UP";
			ebgclass="UP";
			break;
		case AS_HOST_DOWN:
			entry_type="HOST DOWN";
			ebgclass="DOWN";
			break;
		case AS_HOST_UNREACHABLE:
			entry_type="HOST UNREACHABLE";
			ebgclass="UNREACHABLE";
			break;
		case AS_SVC_OK:
			entry_type="SERVICE OK";
			ebgclass="OK";
			break;
		case AS_SVC_UNKNOWN:
			entry_type="SERVICE UNKNOWN";
			ebgclass="UNKNOWN";
			break;
		case AS_SVC_WARNING:
			entry_type="SERVICE WARNING";
			ebgclass="WARNING";
			break;
		case AS_SVC_CRITICAL:
			entry_type="SERVICE CRITICAL";
			ebgclass="CRITICAL";
			break;
		case AS_SVC_DOWNTIME_START:
			entry_type="SERVICE DOWNTIME START";
			ebgclass="INDETERMINATE";
			break;
		case AS_SVC_DOWNTIME_END:
			entry_type="SERVICE DOWNTIME END";
			ebgclass="INDETERMINATE";
			break;
		case AS_HOST_DOWNTIME_START:
			entry_type="HOST DOWNTIME START";
			ebgclass="INDETERMINATE";
			break;
		case AS_HOST_DOWNTIME_END:
			entry_type="HOST DOWNTIME END";
			ebgclass="INDETERMINATE";
			break;
		default:
			if(full_log_entries==common_h.FALSE)
				continue;
			entry_type="?";
			ebgclass="INDETERMINATE";
		        }

        start_date_time = cgiutils.get_time_string( temp_as.time_stamp,common_h.SHORT_DATE_TIME);
		if( !iter.hasNext() ){
            end_date_time = cgiutils.get_time_string(t2,common_h.SHORT_DATE_TIME);
			cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((t2-temp_as.time_stamp));
			duration = String.format( "%dd %dh %dm %ds+",tb.days,tb.hours,tb.minutes,tb.seconds);
		        }
		else{
            // TODO why was first param passed in by reference
            archived_state next_as = iter.next();
            end_date_time = cgiutils.get_time_string( next_as.time_stamp,common_h.SHORT_DATE_TIME);
			cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((next_as.time_stamp-temp_as.time_stamp));
            duration = String.format("%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
            iter.previous();
		        }

		if(odd != 0){
			bgclass="Odd";
			odd=0;
		        }
		else{
			bgclass="Even";
			odd=1;
		        }

		System.out.printf("<tr class='logEntries%s'>",bgclass);
		System.out.printf("<td class='logEntries%s'>%s</td>",bgclass,start_date_time);
		System.out.printf("<td class='logEntries%s'>%s</td>",bgclass,end_date_time);
		System.out.printf("<td class='logEntries%s'>%s</td>",bgclass,duration);
		System.out.printf("<td class='logEntries%s'>%s%s</td>",ebgclass,entry_type,state_type);
		System.out.printf("<td class='logEntries%s'>%s</td>",bgclass,(temp_as.state_info==null)?"":temp_as.state_info);
		System.out.printf("</tr>\n");
	        }

	System.out.printf("</table>\n");

	System.out.printf("</DIV>\n");

	return;
        }
 


/* display hostgroup availability */
public static void display_hostgroup_availability(){

	/* display data for a specific hostgroup */
	if(show_all_hostgroups==common_h.FALSE){
        objects_h.hostgroup temp_hostgroup=objects.find_hostgroup(hostgroup_name);
		display_specific_hostgroup_availability(temp_hostgroup);
	        }

	/* display data for all hostgroups */
	else{
		for( objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list )
			display_specific_hostgroup_availability(temp_hostgroup);
	        }

	return;
        }



/* display availability for a specific hostgroup */
public static void display_specific_hostgroup_availability(objects_h.hostgroup hg){
	long total_time;
	long time_determinate;
	long time_indeterminate;
//	avail_subject temp_subject;
	double percent_time_up=0.0;
	double percent_time_down=0.0;
	double percent_time_unreachable=0.0;
	double percent_time_up_known=0.0;
	double percent_time_down_known=0.0;
	double percent_time_unreachable_known=0.0;
	double percent_time_indeterminate=0.0;

	double average_percent_time_up=0.0;
	double average_percent_time_up_known=0.0;
	double average_percent_time_down=0.0;
	double average_percent_time_down_known=0.0;
	double average_percent_time_unreachable=0.0;
	double average_percent_time_unreachable_known=0.0;
	double average_percent_time_indeterminate=0.0;

	int current_subject=0;

	String bgclass="";
	int odd=1;
	objects_h.host temp_host;

	if(hg==null)
		return;

	/* the user isn't authorized to view this hostgroup */
	if(cgiauth.is_authorized_for_hostgroup(hg,current_authdata)==common_h.FALSE)
		return;

	/* calculate total time during period based on timeperiod used for reporting */
	total_time=calculate_total_time(t1,t2);

	System.out.printf("<BR><BR>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Hostgroup '%s' Host State Breakdowns:</DIV>\n",hg.group_name);

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR><TH CLASS='data'>Host</TH><TH CLASS='data'>%% Time Up</TH><TH CLASS='data'>%% Time Down</TH><TH CLASS='data'>%% Time Unreachable</TH><TH CLASS='data'>%% Time Undetermined</TH></TR>\n");

	for(avail_subject temp_subject : subject_list ){

		if(temp_subject.type!=HOST_SUBJECT)
			continue;

		temp_host=objects.find_host(temp_subject.host_name);
		if(temp_host==null)
			continue;

		if(objects.is_host_member_of_hostgroup(hg,temp_host)==common_h.FALSE)
			continue;

		current_subject++;

		/* reset variables */
		percent_time_up=0.0;
		percent_time_down=0.0;
		percent_time_unreachable=0.0;
		percent_time_indeterminate=0.0;
		percent_time_up_known=0.0;
		percent_time_down_known=0.0;
		percent_time_unreachable_known=0.0;

		time_determinate=temp_subject.time_up+temp_subject.time_down+temp_subject.time_unreachable;
		time_indeterminate=total_time-time_determinate;
	
		if(total_time>0){
			percent_time_up=((temp_subject.time_up*100.0)/total_time);
			percent_time_down=((temp_subject.time_down*100.0)/total_time);
			percent_time_unreachable=((temp_subject.time_unreachable*100.0)/total_time);
			percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
			if(time_determinate>0){
				percent_time_up_known=((temp_subject.time_up*100.0)/time_determinate);
				percent_time_down_known=((temp_subject.time_down*100.0)/time_determinate);
				percent_time_unreachable_known=((temp_subject.time_unreachable*100.0)/time_determinate);
	                        }
		        }

		if(odd != 0){
			odd=0;
			bgclass="Odd";
	                }
		else{
			odd=1;
			bgclass="Even";
	                }

		System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>",bgclass,bgclass);
		host_report_url(temp_subject.host_name,temp_subject.host_name);
		System.out.printf("</td><td CLASS='hostUP'>%2.3f%% (%2.3f%%)</td><td CLASS='hostDOWN'>%2.3f%% (%2.3f%%)</td><td CLASS='hostUNREACHABLE'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",percent_time_up,percent_time_up_known,percent_time_down,percent_time_down_known,percent_time_unreachable,percent_time_unreachable_known,bgclass,percent_time_indeterminate);

        average_percent_time_up = get_running_average(average_percent_time_up,percent_time_up,current_subject);
        average_percent_time_up_known = get_running_average(average_percent_time_up_known,percent_time_up_known,current_subject);
        average_percent_time_down = get_running_average(average_percent_time_down,percent_time_down,current_subject);
        average_percent_time_down_known = get_running_average(average_percent_time_down_known,percent_time_down_known,current_subject);
        average_percent_time_unreachable = get_running_average(average_percent_time_unreachable,percent_time_unreachable,current_subject);
        average_percent_time_unreachable_known = get_running_average(average_percent_time_unreachable_known,percent_time_unreachable_known,current_subject);
        average_percent_time_indeterminate = get_running_average(average_percent_time_indeterminate,percent_time_indeterminate,current_subject);
                }

	/* average statistics */
	if(odd != 0){
		odd=0;
		bgclass="Odd";
	        }
	else{
		odd=1;
		bgclass="Even";
      	        }
	System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>Average</td><td CLASS='hostUP'>%2.3f%% (%2.3f%%)</td><td CLASS='hostDOWN'>%2.3f%% (%2.3f%%)</td><td CLASS='hostUNREACHABLE'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>",bgclass,bgclass,average_percent_time_up,average_percent_time_up_known,average_percent_time_down,average_percent_time_down_known,average_percent_time_unreachable,average_percent_time_unreachable_known,bgclass,average_percent_time_indeterminate);

	System.out.printf("</table>\n");
	System.out.printf("</DIV>\n");

	return;
        }


/* display servicegroup availability */
public static void display_servicegroup_availability(){

	/* display data for a specific servicegroup */
	if(show_all_servicegroups==common_h.FALSE){
        objects_h.servicegroup temp_servicegroup=objects.find_servicegroup(servicegroup_name);
		display_specific_servicegroup_availability(temp_servicegroup);
	        }

	/* display data for all servicegroups */
	else{
		for(objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>) objects.servicegroup_list) {
			display_specific_servicegroup_availability(temp_servicegroup);
	        }

        }
}

/* display availability for a specific servicegroup */
public static void display_specific_servicegroup_availability(objects_h.servicegroup sg){
	long total_time;
	long time_determinate;
	long time_indeterminate;
//	avail_subject temp_subject;
	double percent_time_up=0.0;
	double percent_time_down=0.0;
	double percent_time_unreachable=0.0;
	double percent_time_up_known=0.0;
	double percent_time_down_known=0.0;
	double percent_time_unreachable_known=0.0;
	double percent_time_indeterminate=0.0;
	double percent_time_ok=0.0;
	double percent_time_warning=0.0;
	double percent_time_unknown=0.0;
	double percent_time_critical=0.0;
	double percent_time_ok_known=0.0;
	double percent_time_warning_known=0.0;
	double percent_time_unknown_known=0.0;
	double percent_time_critical_known=0.0;

	double average_percent_time_up=0.0;
	double average_percent_time_up_known=0.0;
	double average_percent_time_down=0.0;
	double average_percent_time_down_known=0.0;
	double average_percent_time_unreachable=0.0;
	double average_percent_time_unreachable_known=0.0;
	double average_percent_time_ok=0.0;
	double average_percent_time_ok_known=0.0;
	double average_percent_time_unknown=0.0;
	double average_percent_time_unknown_known=0.0;
	double average_percent_time_warning=0.0;
	double average_percent_time_warning_known=0.0;
	double average_percent_time_critical=0.0;
	double average_percent_time_critical_known=0.0;
	double average_percent_time_indeterminate=0.0;

	int current_subject=0;

	String bgclass="";
	int odd=1;
	objects_h.host temp_host;
	objects_h.service temp_service;
	String last_host;

	if(sg==null)
		return;

	/* the user isn't authorized to view this servicegroup */
	if(cgiauth.is_authorized_for_servicegroup(sg,current_authdata)==common_h.FALSE)
		return;

	/* calculate total time during period based on timeperiod used for reporting */
	total_time=calculate_total_time(t1,t2);

	System.out.printf("<BR><BR>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Servicegroup '%s' Host State Breakdowns:</DIV>\n",sg.group_name);

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR><TH CLASS='data'>Host</TH><TH CLASS='data'>%% Time Up</TH><TH CLASS='data'>%% Time Down</TH><TH CLASS='data'>%% Time Unreachable</TH><TH CLASS='data'>%% Time Undetermined</TH></TR>\n");

	for(avail_subject temp_subject : subject_list ){

		if(temp_subject.type!=HOST_SUBJECT)
			continue;

		temp_host= objects.find_host(temp_subject.host_name);
		if(temp_host==null)
			continue;

		if( objects.is_host_member_of_servicegroup(sg,temp_host)==common_h.FALSE)
			continue;

		current_subject++;

		/* reset variables */
		percent_time_up=0.0;
		percent_time_down=0.0;
		percent_time_unreachable=0.0;
		percent_time_indeterminate=0.0;
		percent_time_up_known=0.0;
		percent_time_down_known=0.0;
		percent_time_unreachable_known=0.0;

		time_determinate=temp_subject.time_up+temp_subject.time_down+temp_subject.time_unreachable;
		time_indeterminate=total_time-time_determinate;
	
		if(total_time>0){
			percent_time_up=((temp_subject.time_up*100.0)/total_time);
			percent_time_down=((temp_subject.time_down*100.0)/total_time);
			percent_time_unreachable=((temp_subject.time_unreachable*100.0)/total_time);
			percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
			if(time_determinate>0){
				percent_time_up_known=((temp_subject.time_up*100.0)/time_determinate);
				percent_time_down_known=((temp_subject.time_down*100.0)/time_determinate);
				percent_time_unreachable_known=((temp_subject.time_unreachable*100.0)/time_determinate);
	                        }
		        }

		if(odd != 0 ){
			odd=0;
			bgclass="Odd";
	                }
		else{
			odd=1;
			bgclass="Even";
	                }

		System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>",bgclass,bgclass);
		host_report_url(temp_subject.host_name,temp_subject.host_name);
		System.out.printf("</td><td CLASS='hostUP'>%2.3f%% (%2.3f%%)</td><td CLASS='hostDOWN'>%2.3f%% (%2.3f%%)</td><td CLASS='hostUNREACHABLE'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",percent_time_up,percent_time_up_known,percent_time_down,percent_time_down_known,percent_time_unreachable,percent_time_unreachable_known,bgclass,percent_time_indeterminate);

        average_percent_time_up = get_running_average(average_percent_time_up,percent_time_up,current_subject);
        average_percent_time_up_known = get_running_average(average_percent_time_up_known,percent_time_up_known,current_subject);
        average_percent_time_down  = get_running_average(average_percent_time_down,percent_time_down,current_subject);
        average_percent_time_down_known = get_running_average(average_percent_time_down_known,percent_time_down_known,current_subject);
        average_percent_time_unreachable = get_running_average(average_percent_time_unreachable,percent_time_unreachable,current_subject);
        average_percent_time_unreachable_known = get_running_average(average_percent_time_unreachable_known,percent_time_unreachable_known,current_subject);
        average_percent_time_indeterminate = get_running_average(average_percent_time_indeterminate,percent_time_indeterminate,current_subject);
                }

	/* average statistics */
	if(odd != 0 ){
		odd=0;
		bgclass="Odd";
	        }
	else{
		odd=1;
		bgclass="Even";
      	        }
	System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>Average</td><td CLASS='hostUP'>%2.3f%% (%2.3f%%)</td><td CLASS='hostDOWN'>%2.3f%% (%2.3f%%)</td><td CLASS='hostUNREACHABLE'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>",bgclass,bgclass,average_percent_time_up,average_percent_time_up_known,average_percent_time_down,average_percent_time_down_known,average_percent_time_unreachable,average_percent_time_unreachable_known,bgclass,average_percent_time_indeterminate);

	System.out.printf("</table>\n");
	System.out.printf("</DIV>\n");

	System.out.printf("<BR>\n");
	System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Servicegroup '%s' Service State Breakdowns:</DIV>\n",sg.group_name);

	System.out.printf("<DIV ALIGN=CENTER>\n");
	System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
	System.out.printf("<TR><TH CLASS='data'>Host</TH><TH CLASS='data'>Service</TH><TH CLASS='data'>%% Time OK</TH><TH CLASS='data'>%% Time Warning</TH><TH CLASS='data'>%% Time Unknown</TH><TH CLASS='data'>%% Time Critical</TH><TH CLASS='data'>%% Time Undetermined</TH></TR>\n");

	current_subject=0;
	average_percent_time_indeterminate=0.0;

	for( avail_subject temp_subject : subject_list ){

		if(temp_subject.type!=SERVICE_SUBJECT)
			continue;

		temp_service= objects.find_service(temp_subject.host_name,temp_subject.service_description);
		if(temp_service==null)
			continue;

		if( objects.is_service_member_of_servicegroup(sg,temp_service)==common_h.FALSE)
			continue;

		current_subject++;

		time_determinate=temp_subject.time_ok+temp_subject.time_warning+temp_subject.time_unknown+temp_subject.time_critical;
		time_indeterminate=total_time-time_determinate;

		/* adjust indeterminate time due to insufficient data (not all was caught) */
		temp_subject.time_indeterminate_nodata=time_indeterminate-temp_subject.time_indeterminate_notrunning;

		/* initialize values */
		percent_time_ok=0.0;
		percent_time_warning=0.0;
		percent_time_unknown=0.0;
		percent_time_critical=0.0;
        percent_time_indeterminate=0.0;
		percent_time_ok_known=0.0;
		percent_time_warning_known=0.0;
		percent_time_unknown_known=0.0;
		percent_time_critical_known=0.0;

		if(total_time>0){
			percent_time_ok=((temp_subject.time_ok*100.0)/total_time);
			percent_time_warning=((temp_subject.time_warning*100.0)/total_time);
			percent_time_unknown=((temp_subject.time_unknown*100.0)/total_time);
			percent_time_critical=((temp_subject.time_critical*100.0)/total_time);
            percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
			if(time_determinate>0){
				percent_time_ok_known=((temp_subject.time_ok*100.0)/time_determinate);
				percent_time_warning_known=((temp_subject.time_warning*100.0)/time_determinate);
				percent_time_unknown_known=((temp_subject.time_unknown*100.0)/time_determinate);
				percent_time_critical_known=((temp_subject.time_critical*100.0)/time_determinate);
	                        }
                        }

		if(odd != 0 ){
			odd=0;
			bgclass="Odd";
	                }
		else{
			odd=1;
			bgclass="Even";
	                }

		System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>",bgclass,bgclass);
        // TODO figure this out, last_host has not yet been initiatilized. This line really reads is host_name != null
        // where as the original code read if host_name != last_host do host_report_url
        // So it s
//		if( ! temp_subject.host_name.equals( last_host))
        if ( temp_subject.host_name != null )
			host_report_url(temp_subject.host_name,temp_subject.host_name);
		System.out.printf("</td><td CLASS='data%s'>",bgclass);
		service_report_url(temp_subject.host_name,temp_subject.service_description,temp_subject.service_description);
		System.out.printf("</td><td CLASS='serviceOK'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceWARNING'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceUNKNOWN'>%2.3f%% (%2.3f%%)</td><td class='serviceCRITICAL'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",percent_time_ok,percent_time_ok_known,percent_time_warning,percent_time_warning_known,percent_time_unknown,percent_time_unknown_known,percent_time_critical,percent_time_critical_known,bgclass,percent_time_indeterminate);

		last_host = temp_subject.host_name;

        average_percent_time_ok = get_running_average(average_percent_time_ok,percent_time_ok,current_subject);
        average_percent_time_ok_known = get_running_average(average_percent_time_ok_known,percent_time_ok_known,current_subject);
        average_percent_time_unknown = get_running_average(average_percent_time_unknown,percent_time_unknown,current_subject);
        average_percent_time_unknown_known = get_running_average(average_percent_time_unknown_known,percent_time_unknown_known,current_subject);
        average_percent_time_warning = get_running_average(average_percent_time_warning,percent_time_warning,current_subject);
        average_percent_time_warning_known = get_running_average(average_percent_time_warning_known,percent_time_warning_known,current_subject);
        average_percent_time_critical = get_running_average(average_percent_time_critical,percent_time_critical,current_subject);
        average_percent_time_critical_known = get_running_average(average_percent_time_critical_known,percent_time_critical_known,current_subject);
        average_percent_time_indeterminate = get_running_average(average_percent_time_indeterminate,percent_time_indeterminate,current_subject);
	        }

	/* display average stats */
	if(odd != 0 ){
		odd=0;
		bgclass="Odd";
	        }
	else{
		odd=1;
		bgclass="Even";
	        }

	System.out.printf("<tr CLASS='data%s'><td CLASS='data%s' colspan='2'>Average</td><td CLASS='serviceOK'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceWARNING'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceUNKNOWN'>%2.3f%% (%2.3f%%)</td><td class='serviceCRITICAL'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",bgclass,bgclass,average_percent_time_ok,average_percent_time_ok_known,average_percent_time_warning,average_percent_time_warning_known,average_percent_time_unknown,average_percent_time_unknown_known,average_percent_time_critical,average_percent_time_critical_known,bgclass,average_percent_time_indeterminate);

	System.out.printf("</table>\n");
	System.out.printf("</DIV>\n");

	return;
        }


/* display host availability */
public static void display_host_availability(){
	long total_time;
	long time_determinate;
	long time_indeterminate;
	avail_subject temp_subject;
	objects_h.host temp_host;
	objects_h.service temp_service;
	String time_indeterminate_string; // 48
	String time_determinate_string; // 48
	String total_time_string; // 48
	double percent_time_ok=0.0;
	double percent_time_warning=0.0;
	double percent_time_unknown=0.0;
	double percent_time_critical=0.0;
	double percent_time_indeterminate=0.0;
	double percent_time_ok_known=0.0;
	double percent_time_warning_known=0.0;
	double percent_time_unknown_known=0.0;
	double percent_time_critical_known=0.0;
	String time_up_string; // 48
	String time_down_string; // 48
	String time_unreachable_string; // 48
	double percent_time_up=0.0;
	double percent_time_down=0.0;
	double percent_time_unreachable=0.0;
	double percent_time_up_known=0.0;
	double percent_time_down_known=0.0;
	double percent_time_unreachable_known=0.0;

	double percent_time_up_scheduled=0.0;
	double percent_time_up_unscheduled=0.0;
	double percent_time_down_scheduled=0.0;
	double percent_time_down_unscheduled=0.0;
	double percent_time_unreachable_scheduled=0.0;
	double percent_time_unreachable_unscheduled=0.0;
	double percent_time_up_scheduled_known=0.0;
	double percent_time_up_unscheduled_known=0.0;
	double percent_time_down_scheduled_known=0.0;
	double percent_time_down_unscheduled_known=0.0;
	double percent_time_unreachable_scheduled_known=0.0;
	double percent_time_unreachable_unscheduled_known=0.0;
	String time_up_scheduled_string; // 48
	String time_up_unscheduled_string; // 48
	String time_down_scheduled_string; // 48
	String time_down_unscheduled_string; // 48
	String time_unreachable_scheduled_string; // 48
	String time_unreachable_unscheduled_string; // 48

	String time_indeterminate_scheduled_string; // 48
	String time_indeterminate_unscheduled_string; // 48
	double percent_time_indeterminate_scheduled=0.0;
	double percent_time_indeterminate_unscheduled=0.0;
	String time_indeterminate_notrunning_string; // 48
	String time_indeterminate_nodata_string; // 48
	double percent_time_indeterminate_notrunning=0.0;
	double percent_time_indeterminate_nodata=0.0;

	double average_percent_time_up=0.0;
	double average_percent_time_up_known=0.0;
	double average_percent_time_down=0.0;
	double average_percent_time_down_known=0.0;
	double average_percent_time_unreachable=0.0;
	double average_percent_time_unreachable_known=0.0;
	double average_percent_time_indeterminate=0.0;

	double average_percent_time_ok=0.0;
	double average_percent_time_ok_known=0.0;
	double average_percent_time_unknown=0.0;
	double average_percent_time_unknown_known=0.0;
	double average_percent_time_warning=0.0;
	double average_percent_time_warning_known=0.0;
	double average_percent_time_critical=0.0;
	double average_percent_time_critical_known=0.0;

	int current_subject=0;

	String bgclass="";
	int odd=1;

	/* calculate total time during period based on timeperiod used for reporting */
	total_time=calculate_total_time(t1,t2);

//   TODO DEBUG
//	System.out.printf("Total time: '%ld' seconds<br>\n",total_time);

	/* show data for a specific host */
	if(show_all_hosts==common_h.FALSE){

		temp_subject= find_subject(HOST_SUBJECT,host_name,null);
		if(temp_subject==null)
			return;

		temp_host=objects.find_host(temp_subject.host_name);
		if(temp_host==null)
			return;

		/* the user isn't authorized to view this host */
		if( cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			return;

		time_determinate=temp_subject.time_up+temp_subject.time_down+temp_subject.time_unreachable;
		time_indeterminate=total_time-time_determinate;

		/* adjust indeterminate time due to insufficient data (not all was caught) */
		temp_subject.time_indeterminate_nodata=time_indeterminate-temp_subject.time_indeterminate_notrunning;

		/* up times */
		cgiutils.time_breakdown tb = cgiutils.get_time_breakdown(temp_subject.time_up);
		time_up_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_up);
		time_up_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.time_up-temp_subject.scheduled_time_up);
		time_up_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* down times */
		tb = cgiutils.get_time_breakdown(temp_subject.time_down);
		time_down_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_down);
		time_down_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.time_down-temp_subject.scheduled_time_down);
		time_down_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* unreachable times */
		tb = cgiutils.get_time_breakdown(temp_subject.time_unreachable);
		time_unreachable_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_unreachable);
		time_unreachable_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.time_unreachable-temp_subject.scheduled_time_unreachable);
		time_unreachable_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* indeterminate times */
		tb = cgiutils.get_time_breakdown(time_indeterminate);
		time_indeterminate_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_indeterminate);
		time_indeterminate_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(time_indeterminate-temp_subject.scheduled_time_indeterminate);
		time_indeterminate_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.time_indeterminate_notrunning);
		time_indeterminate_notrunning_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
		tb = cgiutils.get_time_breakdown(temp_subject.time_indeterminate_nodata);
		time_indeterminate_nodata_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		tb = cgiutils.get_time_breakdown(time_determinate);
		time_determinate_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		tb = cgiutils.get_time_breakdown(total_time);
		total_time_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		if(total_time>0){
			percent_time_up=((temp_subject.time_up*100.0)/total_time);
			percent_time_up_scheduled=((temp_subject.scheduled_time_up*100.0)/total_time);
			percent_time_up_unscheduled=percent_time_up-percent_time_up_scheduled;
			percent_time_down=((temp_subject.time_down*100.0)/total_time);
			percent_time_down_scheduled=((temp_subject.scheduled_time_down*100.0)/total_time);
			percent_time_down_unscheduled=percent_time_down-percent_time_down_scheduled;
			percent_time_unreachable=((temp_subject.time_unreachable*100.0)/total_time);
			percent_time_unreachable_scheduled=((temp_subject.scheduled_time_unreachable*100.0)/total_time);
			percent_time_unreachable_unscheduled=percent_time_unreachable-percent_time_unreachable_scheduled;
			percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
			percent_time_indeterminate_scheduled=((temp_subject.scheduled_time_indeterminate*100.0)/total_time);
			percent_time_indeterminate_unscheduled=percent_time_indeterminate-percent_time_indeterminate_scheduled;
			percent_time_indeterminate_notrunning=((temp_subject.time_indeterminate_notrunning*100.0)/total_time);
			percent_time_indeterminate_nodata=((temp_subject.time_indeterminate_nodata*100.0)/total_time);
			if(time_determinate>0){
				percent_time_up_known=((temp_subject.time_up*100.0)/time_determinate);
				percent_time_up_scheduled_known=((temp_subject.scheduled_time_up*100.0)/time_determinate);
				percent_time_up_unscheduled_known=percent_time_up_known-percent_time_up_scheduled_known;
				percent_time_down_known=((temp_subject.time_down*100.0)/time_determinate);
				percent_time_down_scheduled_known=((temp_subject.scheduled_time_down*100.0)/time_determinate);
				percent_time_down_unscheduled_known=percent_time_down_known-percent_time_down_scheduled_known;
				percent_time_unreachable_known=((temp_subject.time_unreachable*100.0)/time_determinate);
				percent_time_unreachable_scheduled_known=((temp_subject.scheduled_time_unreachable*100.0)/time_determinate);
				percent_time_unreachable_unscheduled_known=percent_time_unreachable_known-percent_time_unreachable_scheduled_known;
		                }
	                }

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Host State Breakdowns:</DIV>\n");

		System.out.printf("<p align='center'>\n");
		System.out.printf("<a href='%s?host=%s",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
		System.out.printf("&t1=%d&t2=%d&includesoftstates=%s&assumestateretention=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedhoststate=%d&backtrack=%d'>",t1,t2,(include_soft_states==common_h.TRUE)?"yes":"no",(assume_state_retention==common_h.TRUE)?"yes":"no",(assume_initial_states==common_h.TRUE)?"yes":"no",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no",initial_assumed_host_state,backtrack_archives);
		System.out.printf("<img src='%s?createimage&smallimage&host=%s",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
		System.out.printf("&t1=%d&t2=%d&includesoftstates=%s&assumestateretention=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedhoststate=%d&backtrack=%d' border=1 alt='Host State Trends' title='Host State Trends' width='500' height='20'>",t1,t2,(include_soft_states==common_h.TRUE)?"yes":"no",(assume_state_retention==common_h.TRUE)?"yes":"no",(assume_initial_states==common_h.TRUE)?"yes":"no",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no",initial_assumed_host_state,backtrack_archives);
		System.out.printf("</a><br>\n");
		System.out.printf("</p>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Type / Reason</TH><TH CLASS='data'>Time</TH><TH CLASS='data'>%% Total Time</TH><TH CLASS='data'>%% Known Time</TH></TR>\n");

		/* up times */
		System.out.printf("<tr CLASS='dataEven'><td CLASS='hostUP' rowspan=3>UP</td>");
		System.out.printf("<td CLASS='dataEven'>Unscheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td class='dataEven'>%2.3f%%</td></tr>\n",time_up_unscheduled_string,percent_time_up,percent_time_up_known);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Scheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td class='dataEven'>%2.3f%%</td></tr>\n",time_up_scheduled_string,percent_time_up_scheduled,percent_time_up_scheduled_known);
		System.out.printf("<tr CLASS='hostUP'><td CLASS='hostUP'>Total</td><td CLASS='hostUP'>%s</td><td CLASS='hostUP'>%2.3f%%</td><td class='hostUP'>%2.3f%%</td></tr>\n",time_up_string,percent_time_up,percent_time_up_known);

		/* down times */
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='hostDOWN' rowspan=3>DOWN</td>");
		System.out.printf("<td CLASS='dataOdd'>Unscheduled</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td class='dataOdd'>%2.3f%%</td></tr>\n",time_down_unscheduled_string,percent_time_down_unscheduled,percent_time_down_known);
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd'>Scheduled</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td class='dataOdd'>%2.3f%%</td></tr>\n",time_down_scheduled_string,percent_time_down_scheduled,percent_time_down_scheduled_known);
		System.out.printf("<tr CLASS='hostDOWN'><td CLASS='hostDOWN'>Total</td><td CLASS='hostDOWN'>%s</td><td CLASS='hostDOWN'>%2.3f%%</td><td class='hostDOWN'>%2.3f%%</td></tr>\n",time_down_string,percent_time_down,percent_time_down_known);

		/* unreachable times */
		System.out.printf("<tr CLASS='dataEven'><td CLASS='hostUNREACHABLE' rowspan=3>UNREACHABLE</td>");
		System.out.printf("<td CLASS='dataEven'>Unscheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td class='dataEven'>%2.3f%%</td></tr>\n",time_unreachable_unscheduled_string,percent_time_unreachable,percent_time_unreachable_known);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Scheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td class='dataEven'>%2.3f%%</td></tr>\n",time_unreachable_scheduled_string,percent_time_unreachable_scheduled,percent_time_unreachable_scheduled_known);
		System.out.printf("<tr CLASS='hostUNREACHABLE'><td CLASS='hostUNREACHABLE'>Total</td><td CLASS='hostUNREACHABLE'>%s</td><td CLASS='hostUNREACHABLE'>%2.3f%%</td><td class='hostUNREACHABLE'>%2.3f%%</td></tr>\n",time_unreachable_string,percent_time_unreachable,percent_time_unreachable_known);

		/* indeterminate times */
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd' rowspan=3>Undetermined</td>");
		System.out.printf("<td CLASS='dataOdd'>Blue Not Running</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'></td></tr>\n",time_indeterminate_notrunning_string,percent_time_indeterminate_notrunning);
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd'>Insufficient Data</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'></td></tr>\n",time_indeterminate_nodata_string,percent_time_indeterminate_nodata);
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd'>Total</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'></td></tr>\n",time_indeterminate_string,percent_time_indeterminate);

		System.out.printf("<tr><td colspan=3></td></tr>\n");

		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>All</td><td class='dataEven'>Total</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>100.000%%</td><td CLASS='dataEven'>100.000%%</td></tr>\n",total_time_string);
		System.out.printf("</table>\n");
		System.out.printf("</DIV>\n");



		/* display state breakdowns for all services on this host */

		System.out.printf("<BR><BR>\n");
		System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>State Breakdowns For Host Services:</DIV>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>Service</TH><TH CLASS='data'>%% Time OK</TH><TH CLASS='data'>%% Time Warning</TH><TH CLASS='data'>%% Time Unknown</TH><TH CLASS='data'>%% Time Critical</TH><TH CLASS='data'>%% Time Undetermined</TH></TR>\n");

		for( Iterator<avail_subject> iter = subject_list.iterator(); iter.hasNext();  ){
            temp_subject = iter.next();

			if(temp_subject.type!=SERVICE_SUBJECT)
				continue;

			temp_service= objects.find_service(temp_subject.host_name,temp_subject.service_description);
			if(temp_service==null)
				continue;

			/* the user isn't authorized to view this service */
			if( cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
				continue;

			current_subject++;

			if(odd != 0 ){
				odd=0;
				bgclass="Odd";
		                }
			else{
				odd=1;
				bgclass="Even";
		                }

			/* reset variables */
			percent_time_ok=0.0;
			percent_time_warning=0.0;
			percent_time_unknown=0.0;
			percent_time_critical=0.0;
			percent_time_indeterminate=0.0;
			percent_time_ok_known=0.0;
			percent_time_warning_known=0.0;
			percent_time_unknown_known=0.0;
			percent_time_critical_known=0.0;

			time_determinate=temp_subject.time_ok+temp_subject.time_warning+temp_subject.time_unknown+temp_subject.time_critical;
			time_indeterminate=total_time-time_determinate;

			if(total_time>0){
				percent_time_ok=((temp_subject.time_ok*100.0)/total_time);
				percent_time_warning=((temp_subject.time_warning*100.0)/total_time);
				percent_time_unknown=((temp_subject.time_unknown*100.0)/total_time);
				percent_time_critical=((temp_subject.time_critical*100.0)/total_time);
				percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
				if(time_determinate>0){
					percent_time_ok_known=((temp_subject.time_ok*100.0)/time_determinate);
					percent_time_warning_known=((temp_subject.time_warning*100.0)/time_determinate);
					percent_time_unknown_known=((temp_subject.time_unknown*100.0)/time_determinate);
					percent_time_critical_known=((temp_subject.time_critical*100.0)/time_determinate);
			                }
	                        }

			System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>",bgclass,bgclass);
			service_report_url(temp_subject.host_name,temp_subject.service_description,temp_subject.service_description);
			System.out.printf("</td><td CLASS='serviceOK'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceWARNING'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceUNKNOWN'>%2.3f%% (%2.3f%%)</td><td class='serviceCRITICAL'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",percent_time_ok,percent_time_ok_known,percent_time_warning,percent_time_warning_known,percent_time_unknown,percent_time_unknown_known,percent_time_critical,percent_time_critical_known,bgclass,percent_time_indeterminate);

			average_percent_time_ok = get_running_average(average_percent_time_ok,percent_time_ok,current_subject);
			average_percent_time_ok_known = get_running_average(average_percent_time_ok_known,percent_time_ok_known,current_subject);
			average_percent_time_unknown = get_running_average(average_percent_time_unknown,percent_time_unknown,current_subject);
			average_percent_time_unknown_known = get_running_average(average_percent_time_unknown_known,percent_time_unknown_known,current_subject);
			average_percent_time_warning = get_running_average(average_percent_time_warning,percent_time_warning,current_subject);
			average_percent_time_warning_known = get_running_average(average_percent_time_warning_known,percent_time_warning_known,current_subject);
			average_percent_time_critical = get_running_average(average_percent_time_critical,percent_time_critical,current_subject);
			average_percent_time_critical_known = get_running_average(average_percent_time_critical_known,percent_time_critical_known,current_subject);
			average_percent_time_indeterminate = get_running_average(average_percent_time_indeterminate,percent_time_indeterminate,current_subject);
	                }

		/* display average stats */
		if(odd != 0 ){
			odd=0;
			bgclass="Odd";
	                }
		else{
			odd=1;
			bgclass="Even";
	                }

		System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>Average</td><td CLASS='serviceOK'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceWARNING'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceUNKNOWN'>%2.3f%% (%2.3f%%)</td><td class='serviceCRITICAL'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",bgclass,bgclass,average_percent_time_ok,average_percent_time_ok_known,average_percent_time_warning,average_percent_time_warning_known,average_percent_time_unknown,average_percent_time_unknown_known,average_percent_time_critical,average_percent_time_critical_known,bgclass,average_percent_time_indeterminate);

		System.out.printf("</table>\n");
		System.out.printf("</DIV>\n");


		/* write log entries for the host */
		temp_subject=find_subject(HOST_SUBJECT,host_name,null);
		write_log_entries(temp_subject);
	        }


	/* display data for all hosts */
	else{

		if(output_format==HTML_OUTPUT){

			System.out.printf("<BR><BR>\n");
			System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Host State Breakdowns:</DIV>\n");

			System.out.printf("<DIV ALIGN=CENTER>\n");
			System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
			System.out.printf("<TR><TH CLASS='data'>Host</TH><TH CLASS='data'>%% Time Up</TH><TH CLASS='data'>%% Time Down</TH><TH CLASS='data'>%% Time Unreachable</TH><TH CLASS='data'>%% Time Undetermined</TH></TR>\n");
		        }

		else if(output_format==CSV_OUTPUT){
			System.out.printf("HOST_NAME,");

			System.out.printf(" TIME_UP_SCHEDULED, PERCENT_TIME_UP_SCHEDULED, PERCENT_KNOWN_TIME_UP_SCHEDULED, TIME_UP_UNSCHEDULED, PERCENT_TIME_UP_UNSCHEDULED, PERCENT_KNOWN_TIME_UP_UNSCHEDULED, TOTAL_TIME_UP, PERCENT_TOTAL_TIME_UP, PERCENT_KNOWN_TIME_UP,");

			System.out.printf(" TIME_DOWN_SCHEDULED, PERCENT_TIME_DOWN_SCHEDULED, PERCENT_KNOWN_TIME_DOWN_SCHEDULED, TIME_DOWN_UNSCHEDULED, PERCENT_TIME_DOWN_UNSCHEDULED, PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED, TOTAL_TIME_DOWN, PERCENT_TOTAL_TIME_DOWN, PERCENT_KNOWN_TIME_DOWN,");

			System.out.printf(" TIME_UNREACHABLE_SCHEDULED, PERCENT_TIME_UNREACHABLE_SCHEDULED, PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED, TIME_UNREACHABLE_UNSCHEDULED, PERCENT_TIME_UNREACHABLE_UNSCHEDULED, PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED, TOTAL_TIME_UNREACHABLE, PERCENT_TOTAL_TIME_UNREACHABLE, PERCENT_KNOWN_TIME_UNREACHABLE,");

			System.out.printf(" TIME_UNDETERMINED_NOT_RUNNING, PERCENT_TIME_UNDETERMINED_NOT_RUNNING, TIME_UNDETERMINED_NO_DATA, PERCENT_TIME_UNDETERMINED_NO_DATA, TOTAL_TIME_UNDETERMINED, PERCENT_TOTAL_TIME_UNDETERMINED\n");
		        }
		   

        for( Iterator<avail_subject> iter = subject_list.iterator(); iter.hasNext();  ){
            temp_subject = iter.next();

			if(temp_subject.type!=HOST_SUBJECT)
				continue;
			
			temp_host=objects.find_host(temp_subject.host_name);
			if(temp_host==null)
				continue;

			/* the user isn't authorized to view this host */
			if( cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
				continue;

			current_subject++;

			time_determinate=temp_subject.time_up+temp_subject.time_down+temp_subject.time_unreachable;
			time_indeterminate=total_time-time_determinate;

			/* adjust indeterminate time due to insufficient data (not all was caught) */
			temp_subject.time_indeterminate_nodata=time_indeterminate-temp_subject.time_indeterminate_notrunning;

			/* initialize values */
			percent_time_up=0.0;
			percent_time_up_scheduled=0.0;
			percent_time_up_unscheduled=0.0;
			percent_time_down=0.0;
			percent_time_down_scheduled=0.0;
			percent_time_down_unscheduled=0.0;
			percent_time_unreachable=0.0;
			percent_time_unreachable_scheduled=0.0;
			percent_time_unreachable_unscheduled=0.0;
			percent_time_indeterminate=0.0;
			percent_time_indeterminate_scheduled=0.0;
			percent_time_indeterminate_unscheduled=0.0;
			percent_time_indeterminate_notrunning=0.0;
			percent_time_indeterminate_nodata=0.0;
			percent_time_up_known=0.0;
			percent_time_up_scheduled_known=0.0;
			percent_time_up_unscheduled_known=0.0;
			percent_time_down_known=0.0;
			percent_time_down_scheduled_known=0.0;
			percent_time_down_unscheduled_known=0.0;
			percent_time_unreachable_known=0.0;
			percent_time_unreachable_scheduled_known=0.0;
			percent_time_unreachable_unscheduled_known=0.0;
	
			if(total_time>0){
				percent_time_up=((temp_subject.time_up*100.0)/total_time);
				percent_time_up_scheduled=((temp_subject.scheduled_time_up*100.0)/total_time);
				percent_time_up_unscheduled=percent_time_up-percent_time_up_scheduled;
				percent_time_down=((temp_subject.time_down*100.0)/total_time);
				percent_time_down_scheduled=((temp_subject.scheduled_time_down*100.0)/total_time);
				percent_time_down_unscheduled=percent_time_down-percent_time_down_scheduled;
				percent_time_unreachable=((temp_subject.time_unreachable*100.0)/total_time);
				percent_time_unreachable_scheduled=((temp_subject.scheduled_time_unreachable*100.0)/total_time);
				percent_time_unreachable_unscheduled=percent_time_unreachable-percent_time_unreachable_scheduled;
				percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
				percent_time_indeterminate_scheduled=((temp_subject.scheduled_time_indeterminate*100.0)/total_time);
				percent_time_indeterminate_unscheduled=percent_time_indeterminate-percent_time_indeterminate_scheduled;
				percent_time_indeterminate_notrunning=((temp_subject.time_indeterminate_notrunning*100.0)/total_time);
				percent_time_indeterminate_nodata=((temp_subject.time_indeterminate_nodata*100.0)/total_time);
				if(time_determinate>0){
					percent_time_up_known=((temp_subject.time_up*100.0)/time_determinate);
					percent_time_up_scheduled_known=((temp_subject.scheduled_time_up*100.0)/time_determinate);
					percent_time_up_unscheduled_known=percent_time_up_known-percent_time_up_scheduled_known;
					percent_time_down_known=((temp_subject.time_down*100.0)/time_determinate);
					percent_time_down_scheduled_known=((temp_subject.scheduled_time_down*100.0)/time_determinate);
					percent_time_down_unscheduled_known=percent_time_down_known-percent_time_down_scheduled_known;
					percent_time_unreachable_known=((temp_subject.time_unreachable*100.0)/time_determinate);
					percent_time_unreachable_scheduled_known=((temp_subject.scheduled_time_unreachable*100.0)/time_determinate);
					percent_time_unreachable_unscheduled_known=percent_time_unreachable_known-percent_time_unreachable_scheduled_known;
		                        }
	                        }

			if(output_format==HTML_OUTPUT){

				if(odd != 0 ){
					odd=0;
					bgclass="Odd";
			                }
				else{
					odd=1;
					bgclass="Even";
			                }

				System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>",bgclass,bgclass);
				host_report_url(temp_subject.host_name,temp_subject.host_name);
				System.out.printf("</td><td CLASS='hostUP'>%2.3f%% (%2.3f%%)</td><td CLASS='hostDOWN'>%2.3f%% (%2.3f%%)</td><td CLASS='hostUNREACHABLE'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",percent_time_up,percent_time_up_known,percent_time_down,percent_time_down_known,percent_time_unreachable,percent_time_unreachable_known,bgclass,percent_time_indeterminate);
			        }
			else if(output_format==CSV_OUTPUT){

				/* host name */
				System.out.printf("\"%s\",",temp_subject.host_name);
				
				/* up times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_up,percent_time_up_scheduled,percent_time_up_scheduled_known,temp_subject.time_up-temp_subject.scheduled_time_up,percent_time_up_unscheduled,percent_time_up_unscheduled_known,temp_subject.time_up,percent_time_up,percent_time_up_known);

				/* down times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_down,percent_time_down_scheduled,percent_time_down_scheduled_known,temp_subject.time_down-temp_subject.scheduled_time_down,percent_time_down_unscheduled,percent_time_down_unscheduled_known,temp_subject.time_down,percent_time_down,percent_time_down_known);

				/* unreachable times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_unreachable,percent_time_unreachable_scheduled,percent_time_unreachable_scheduled_known,temp_subject.time_unreachable-temp_subject.scheduled_time_unreachable,percent_time_unreachable_unscheduled,percent_time_unreachable_unscheduled_known,temp_subject.time_unreachable,percent_time_unreachable,percent_time_unreachable_known);

				/* indeterminate times */
				System.out.printf(" %d, %2.3f%%, %d, %2.3f%%, %d, %2.3f%%\n",temp_subject.time_indeterminate_notrunning,percent_time_indeterminate_notrunning,temp_subject.time_indeterminate_nodata,percent_time_indeterminate_nodata,time_indeterminate,percent_time_indeterminate);
			        }

			average_percent_time_up = get_running_average(average_percent_time_up,percent_time_up,current_subject);
			average_percent_time_up_known = get_running_average(average_percent_time_up_known,percent_time_up_known,current_subject);
			average_percent_time_down = get_running_average(average_percent_time_down,percent_time_down,current_subject);
			average_percent_time_down_known = get_running_average(average_percent_time_down_known,percent_time_down_known,current_subject);
			average_percent_time_unreachable = get_running_average(average_percent_time_unreachable,percent_time_unreachable,current_subject);
			average_percent_time_unreachable_known = get_running_average(average_percent_time_unreachable_known,percent_time_unreachable_known,current_subject);
			average_percent_time_indeterminate = get_running_average(average_percent_time_indeterminate,percent_time_indeterminate,current_subject);
	                }

		if(output_format==HTML_OUTPUT){

			/* average statistics */
			if(odd != 0 ){
				odd=0;
				bgclass="Odd";
			        }
			else{
				odd=1;
				bgclass="Even";
		      	        }
			System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>Average</td><td CLASS='hostUP'>%2.3f%% (%2.3f%%)</td><td CLASS='hostDOWN'>%2.3f%% (%2.3f%%)</td><td CLASS='hostUNREACHABLE'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>",bgclass,bgclass,average_percent_time_up,average_percent_time_up_known,average_percent_time_down,average_percent_time_down_known,average_percent_time_unreachable,average_percent_time_unreachable_known,bgclass,average_percent_time_indeterminate);

				System.out.printf("</table>\n");
				System.out.printf("</DIV>\n");
			        }
		        }

	return;
        }


/* display service availability */
public static void display_service_availability(){
	long total_time;
	long time_determinate;
	long time_indeterminate;
//	avail_subject temp_subject;
	objects_h.service temp_service;
	String time_ok_string; //48
	String time_warning_string; //48
	String time_unknown_string; //48
	String time_critical_string; //48
	String time_indeterminate_string; //48
	String time_determinate_string; //48
	String total_time_string; //48
	double percent_time_ok=0.0;
	double percent_time_warning=0.0;
	double percent_time_unknown=0.0;
	double percent_time_critical=0.0;
	double percent_time_indeterminate=0.0;
	double percent_time_ok_known=0.0;
	double percent_time_warning_known=0.0;
	double percent_time_unknown_known=0.0;
	double percent_time_critical_known=0.0;

	String time_critical_scheduled_string; // 48
	String time_critical_unscheduled_string; // 48
	double percent_time_critical_scheduled=0.0;
	double percent_time_critical_unscheduled=0.0;
	double percent_time_critical_scheduled_known=0.0;
	double percent_time_critical_unscheduled_known=0.0;
	String time_unknown_scheduled_string; // 48
	String time_unknown_unscheduled_string; // 48
	double percent_time_unknown_scheduled=0.0;
	double percent_time_unknown_unscheduled=0.0;
	double percent_time_unknown_scheduled_known=0.0;
	double percent_time_unknown_unscheduled_known=0.0;
	String time_warning_scheduled_string; // 48
	String time_warning_unscheduled_string; // 48
	double percent_time_warning_scheduled=0.0;
	double percent_time_warning_unscheduled=0.0;
	double percent_time_warning_scheduled_known=0.0;
	double percent_time_warning_unscheduled_known=0.0;
	String time_ok_scheduled_string; // 48
	String time_ok_unscheduled_string; // 48
	double percent_time_ok_scheduled=0.0;
	double percent_time_ok_unscheduled=0.0;
	double percent_time_ok_scheduled_known=0.0;
	double percent_time_ok_unscheduled_known=0.0;

	double average_percent_time_ok=0.0;
	double average_percent_time_ok_known=0.0;
	double average_percent_time_unknown=0.0;
	double average_percent_time_unknown_known=0.0;
	double average_percent_time_warning=0.0;
	double average_percent_time_warning_known=0.0;
	double average_percent_time_critical=0.0;
	double average_percent_time_critical_known=0.0;
	double average_percent_time_indeterminate=0.0;

	int current_subject=0;

	String time_indeterminate_scheduled_string; // 48
	String time_indeterminate_unscheduled_string; // 48
	double percent_time_indeterminate_scheduled=0.0;
	double percent_time_indeterminate_unscheduled=0.0;
	String time_indeterminate_notrunning_string; // 48
	String time_indeterminate_nodata_string; // 48
	double percent_time_indeterminate_notrunning=0.0;
	double percent_time_indeterminate_nodata=0.0;

	int odd=1;
	String bgclass="";
	String last_host="";


	/* calculate total time during period based on timeperiod used for reporting */
	total_time=calculate_total_time(t1,t2);

	/* we're only getting data for one service */
	if(show_all_services==common_h.FALSE){

        avail_subject temp_subject=find_subject(SERVICE_SUBJECT,host_name,svc_description);
		if(temp_subject==null)
			return;

		temp_service=objects.find_service(temp_subject.host_name,temp_subject.service_description);
		if(temp_service==null)
			return;

		/* the user isn't authorized to view this service */
		if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
			return;

		time_determinate=temp_subject.time_ok+temp_subject.time_warning+temp_subject.time_unknown+temp_subject.time_critical;
		time_indeterminate=total_time-time_determinate;
	
		/* adjust indeterminate time due to insufficient data (not all was caught) */
		temp_subject.time_indeterminate_nodata=time_indeterminate-temp_subject.time_indeterminate_notrunning;

		/* ok states */
		cgiutils.time_breakdown tb = cgiutils.get_time_breakdown(temp_subject.time_ok);
		time_ok_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_ok);
		time_ok_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.time_ok-temp_subject.scheduled_time_ok);
		time_ok_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* warning states */
        tb = cgiutils.get_time_breakdown(temp_subject.time_warning);
		time_warning_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_warning );
		time_warning_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.time_warning-temp_subject.scheduled_time_warning );
		time_warning_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* unknown states */
        tb = cgiutils.get_time_breakdown(temp_subject.time_unknown );
		time_unknown_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_unknown );
		time_unknown_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.time_unknown-temp_subject.scheduled_time_unknown );
		time_unknown_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* critical states */
        tb = cgiutils.get_time_breakdown(temp_subject.time_critical );
		time_critical_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_critical );
		time_critical_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.time_critical-temp_subject.scheduled_time_critical );
		time_critical_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		/* indeterminate time */
        tb = cgiutils.get_time_breakdown(time_indeterminate );
		time_indeterminate_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.scheduled_time_indeterminate );
		time_indeterminate_scheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(time_indeterminate-temp_subject.scheduled_time_indeterminate );
		time_indeterminate_unscheduled_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.time_indeterminate_notrunning );
		time_indeterminate_notrunning_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);
        tb = cgiutils.get_time_breakdown(temp_subject.time_indeterminate_nodata );
		time_indeterminate_nodata_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

        tb = cgiutils.get_time_breakdown(time_determinate );
		time_determinate_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

        tb = cgiutils.get_time_breakdown(total_time );
		total_time_string = String.format( "%dd %dh %dm %ds",tb.days,tb.hours,tb.minutes,tb.seconds);

		if(total_time>0){
			percent_time_ok=((temp_subject.time_ok*100.0)/total_time);
			percent_time_ok_scheduled=((temp_subject.scheduled_time_ok*100.0)/total_time);
			percent_time_ok_unscheduled=percent_time_ok-percent_time_ok_scheduled;
			percent_time_warning=((temp_subject.time_warning*100.0)/total_time);
			percent_time_warning_scheduled=((temp_subject.scheduled_time_unknown*100.0)/total_time);
			percent_time_warning_unscheduled=percent_time_warning-percent_time_warning_scheduled;
			percent_time_unknown=((temp_subject.time_unknown*100.0)/total_time);
			percent_time_unknown_scheduled=((temp_subject.scheduled_time_unknown*100.0)/total_time);
			percent_time_unknown_unscheduled=percent_time_unknown-percent_time_unknown_scheduled;
			percent_time_critical=((temp_subject.time_critical*100.0)/total_time);
			percent_time_critical_scheduled=((temp_subject.scheduled_time_critical*100.0)/total_time);
			percent_time_critical_unscheduled=percent_time_critical-percent_time_critical_scheduled;
			percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
			percent_time_indeterminate_scheduled=((temp_subject.scheduled_time_indeterminate*100.0)/total_time);
			percent_time_indeterminate_unscheduled=percent_time_indeterminate-percent_time_indeterminate_scheduled;
			percent_time_indeterminate_notrunning=((temp_subject.time_indeterminate_notrunning*100.0)/total_time);
			percent_time_indeterminate_nodata=((temp_subject.time_indeterminate_nodata*100.0)/total_time);
			if(time_determinate>0){
				percent_time_ok_known=((temp_subject.time_ok*100.0)/time_determinate);
				percent_time_ok_scheduled_known=((temp_subject.scheduled_time_ok*100.0)/time_determinate);
				percent_time_ok_unscheduled_known=percent_time_ok_known-percent_time_ok_scheduled_known;
				percent_time_warning_known=((temp_subject.time_warning*100.0)/time_determinate);
				percent_time_warning_scheduled_known=((temp_subject.scheduled_time_warning*100.0)/time_determinate);
				percent_time_warning_unscheduled_known=percent_time_warning_known-percent_time_warning_scheduled_known;
				percent_time_unknown_known=((temp_subject.time_unknown*100.0)/time_determinate);
				percent_time_unknown_scheduled_known=((temp_subject.scheduled_time_unknown*100.0)/time_determinate);
				percent_time_unknown_unscheduled_known=percent_time_unknown_known-percent_time_unknown_scheduled_known;
				percent_time_critical_known=((temp_subject.time_critical*100.0)/time_determinate);
				percent_time_critical_scheduled_known=((temp_subject.scheduled_time_critical*100.0)/time_determinate);
				percent_time_critical_unscheduled_known=percent_time_critical_known-percent_time_critical_scheduled_known;
		                }
	                }

		System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Service State Breakdowns:</DIV>\n");

		System.out.printf("<p align='center'>\n");
		System.out.printf("<a href='%s?host=%s",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
		System.out.printf("&service=%s&t1=%d&t2=%d&includesoftstates=%s&assumestateretention=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedservicestate=%d&backtrack=%d'>",cgiutils.url_encode(svc_description),t1,t2,(include_soft_states==common_h.TRUE)?"yes":"no",(assume_state_retention==common_h.TRUE)?"yes":"no",(assume_initial_states==common_h.TRUE)?"yes":"no",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no",initial_assumed_service_state,backtrack_archives);
		System.out.printf("<img src='%s?createimage&smallimage&host=%s",cgiutils_h.TRENDS_CGI,cgiutils.url_encode(host_name));
		System.out.printf("&service=%s&t1=%d&t2=%d&includesoftstates=%s&assumestateretention=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedservicestate=%d&backtrack=%d' border=1 alt='Service State Trends' title='Service State Trends' width='500' height='20'>",cgiutils.url_encode(svc_description),t1,t2,(include_soft_states==common_h.TRUE)?"yes":"no",(assume_state_retention==common_h.TRUE)?"yes":"no",(assume_initial_states==common_h.TRUE)?"yes":"no",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no",initial_assumed_service_state,backtrack_archives);
		System.out.printf("</a><br>\n");
		System.out.printf("</p>\n");

		System.out.printf("<DIV ALIGN=CENTER>\n");
		System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
		System.out.printf("<TR><TH CLASS='data'>State</TH><TH CLASS='data'>Type / Reason</TH><TH CLASS='data'>Time</TH><TH CLASS='data'>%% Total Time</TH><TH CLASS='data'>%% Known Time</TH></TR>\n");

		/* ok states */
		System.out.printf("<tr CLASS='dataEven'><td CLASS='serviceOK' rowspan=3>OK</td>");
		System.out.printf("<td CLASS='dataEven'>Unscheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'>%2.3f%%</td></tr>\n",time_ok_unscheduled_string,percent_time_ok_unscheduled,percent_time_ok_unscheduled_known);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Scheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'>%2.3f%%</td></tr>\n",time_ok_scheduled_string,percent_time_ok_scheduled,percent_time_ok_scheduled_known);
		System.out.printf("<tr CLASS='serviceOK'><td CLASS='serviceOK'>Total</td><td CLASS='serviceOK'>%s</td><td CLASS='serviceOK'>%2.3f%%</td><td CLASS='serviceOK'>%2.3f%%</td></tr>\n",time_ok_string,percent_time_ok,percent_time_ok_known);

		/* warning states */
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='serviceWARNING' rowspan=3>WARNING</td>");
		System.out.printf("<td CLASS='dataOdd'>Unscheduled</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'>%2.3f%%</td></tr>\n",time_warning_unscheduled_string,percent_time_warning_unscheduled,percent_time_warning_unscheduled_known);
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd'>Scheduled</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'>%2.3f%%</td></tr>\n",time_warning_scheduled_string,percent_time_warning_scheduled,percent_time_warning_scheduled_known);
		System.out.printf("<tr CLASS='serviceWARNING'><td CLASS='serviceWARNING'>Total</td><td CLASS='serviceWARNING'>%s</td><td CLASS='serviceWARNING'>%2.3f%%</td><td CLASS='serviceWARNING'>%2.3f%%</td></tr>\n",time_warning_string,percent_time_warning,percent_time_warning_known);

		/* unknown states */
		System.out.printf("<tr CLASS='dataEven'><td CLASS='serviceUNKNOWN' rowspan=3>UNKNOWN</td>");
		System.out.printf("<td CLASS='dataEven'>Unscheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'>%2.3f%%</td></tr>\n",time_unknown_unscheduled_string,percent_time_unknown_unscheduled,percent_time_unknown_unscheduled_known);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Scheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'>%2.3f%%</td></tr>\n",time_unknown_scheduled_string,percent_time_unknown_scheduled,percent_time_unknown_scheduled_known);
		System.out.printf("<tr CLASS='serviceUNKNOWN'><td CLASS='serviceUNKNOWN'>Total</td><td CLASS='serviceUNKNOWN'>%s</td><td CLASS='serviceUNKNOWN'>%2.3f%%</td><td CLASS='serviceUNKNOWN'>%2.3f%%</td></tr>\n",time_unknown_string,percent_time_unknown,percent_time_unknown_known);

		/* critical states */
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='serviceCRITICAL' rowspan=3>CRITICAL</td>");
		System.out.printf("<td CLASS='dataOdd'>Unscheduled</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'>%2.3f%%</td></tr>\n",time_critical_unscheduled_string,percent_time_critical_unscheduled,percent_time_critical_unscheduled_known);
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd'>Scheduled</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>%2.3f%%</td><td CLASS='dataOdd'>%2.3f%%</td></tr>\n",time_critical_scheduled_string,percent_time_critical_scheduled,percent_time_critical_scheduled_known);
		System.out.printf("<tr CLASS='serviceCRITICAL'><td CLASS='serviceCRITICAL'>Total</td><td CLASS='serviceCRITICAL'>%s</td><td CLASS='serviceCRITICAL'>%2.3f%%</td><td CLASS='serviceCRITICAL'>%2.3f%%</td></tr>\n",time_critical_string,percent_time_critical,percent_time_critical_known);


		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven' rowspan=3>Undetermined</td>");
		/*
		System.out.printf("<td CLASS='dataEven'>Unscheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'></td></tr>\n",time_indeterminate_unscheduled_string,percent_time_indeterminate_unscheduled);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Scheduled</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'></td></tr>\n",time_indeterminate_scheduled_string,percent_time_indeterminate_scheduled);
		*/
		System.out.printf("<td CLASS='dataEven'>Blue Not Running</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'></td></tr>\n",time_indeterminate_notrunning_string,percent_time_indeterminate_notrunning);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Insufficient Data</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'></td></tr>\n",time_indeterminate_nodata_string,percent_time_indeterminate_nodata);
		System.out.printf("<tr CLASS='dataEven'><td CLASS='dataEven'>Total</td><td CLASS='dataEven'>%s</td><td CLASS='dataEven'>%2.3f%%</td><td CLASS='dataEven'></td></tr>\n",time_indeterminate_string,percent_time_indeterminate);

		System.out.printf("<tr><td colspan=3></td></tr>\n");
		System.out.printf("<tr CLASS='dataOdd'><td CLASS='dataOdd'>All</td><td CLASS='dataOdd'>Total</td><td CLASS='dataOdd'>%s</td><td CLASS='dataOdd'>100.000%%</td><td CLASS='dataOdd'>100.000%%</td></tr>\n",total_time_string);
		System.out.printf("</table>\n");
		System.out.printf("</DIV>\n");


		write_log_entries(temp_subject);
	        }


	/* display data for all services */
	else{

		if(output_format==HTML_OUTPUT){

			System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>Service State Breakdowns:</DIV>\n");

			System.out.printf("<DIV ALIGN=CENTER>\n");
			System.out.printf("<TABLE BORDER=0 CLASS='data'>\n");
			System.out.printf("<TR><TH CLASS='data'>Host</TH><TH CLASS='data'>Service</TH><TH CLASS='data'>%% Time OK</TH><TH CLASS='data'>%% Time Warning</TH><TH CLASS='data'>%% Time Unknown</TH><TH CLASS='data'>%% Time Critical</TH><TH CLASS='data'>%% Time Undetermined</TH></TR>\n");
		        }
		else if(output_format==CSV_OUTPUT){
			System.out.printf("HOST_NAME, SERVICE_DESCRIPTION,");
			System.out.printf(" TIME_OK_SCHEDULED, PERCENT_TIME_OK_SCHEDULED, PERCENT_KNOWN_TIME_OK_SCHEDULED, TIME_OK_UNSCHEDULED, PERCENT_TIME_OK_UNSCHEDULED, PERCENT_KNOWN_TIME_OK_UNSCHEDULED, TOTAL_TIME_OK, PERCENT_TOTAL_TIME_OK, PERCENT_KNOWN_TIME_OK,");
			System.out.printf(" TIME_WARNING_SCHEDULED, PERCENT_TIME_WARNING_SCHEDULED, PERCENT_KNOWN_TIME_WARNING_SCHEDULED, TIME_WARNING_UNSCHEDULED, PERCENT_TIME_WARNING_UNSCHEDULED, PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED, TOTAL_TIME_WARNING, PERCENT_TOTAL_TIME_WARNING, PERCENT_KNOWN_TIME_WARNING,");
			System.out.printf(" TIME_UNKNOWN_SCHEDULED, PERCENT_TIME_UNKNOWN_SCHEDULED, PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED, TIME_UNKNOWN_UNSCHEDULED, PERCENT_TIME_UNKNOWN_UNSCHEDULED, PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED, TOTAL_TIME_UNKNOWN, PERCENT_TOTAL_TIME_UNKNOWN, PERCENT_KNOWN_TIME_UNKNOWN,");
			System.out.printf(" TIME_CRITICAL_SCHEDULED, PERCENT_TIME_CRITICAL_SCHEDULED, PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED, TIME_CRITICAL_UNSCHEDULED, PERCENT_TIME_CRITICAL_UNSCHEDULED, PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED, TOTAL_TIME_CRITICAL, PERCENT_TOTAL_TIME_CRITICAL, PERCENT_KNOWN_TIME_CRITICAL,");
			System.out.printf(" TIME_UNDETERMINED_NOT_RUNNING, PERCENT_TIME_UNDETERMINED_NOT_RUNNING, TIME_UNDETERMINED_NO_DATA, PERCENT_TIME_UNDETERMINED_NO_DATA, TOTAL_TIME_UNDETERMINED, PERCENT_TOTAL_TIME_UNDETERMINED\n");
		        }
		   

		for( avail_subject temp_subject : subject_list ){

			if(temp_subject.type!=SERVICE_SUBJECT)
				continue;

			temp_service= objects.find_service(temp_subject.host_name,temp_subject.service_description);
			if(temp_service==null)
				continue;

			/* the user isn't authorized to view this service */
			if(cgiauth.is_authorized_for_service(temp_service,current_authdata)==common_h.FALSE)
				continue;

			current_subject++;

			time_determinate=temp_subject.time_ok+temp_subject.time_warning+temp_subject.time_unknown+temp_subject.time_critical;
			time_indeterminate=total_time-time_determinate;

			/* adjust indeterminate time due to insufficient data (not all was caught) */
			temp_subject.time_indeterminate_nodata=time_indeterminate-temp_subject.time_indeterminate_notrunning;

			/* initialize values */
			percent_time_ok=0.0;
			percent_time_ok_scheduled=0.0;
			percent_time_ok_unscheduled=0.0;
			percent_time_warning=0.0;
			percent_time_warning_scheduled=0.0;
			percent_time_warning_unscheduled=0.0;
			percent_time_unknown=0.0;
			percent_time_unknown_scheduled=0.0;
			percent_time_unknown_unscheduled=0.0;
			percent_time_critical=0.0;
			percent_time_critical_scheduled=0.0;
			percent_time_critical_unscheduled=0.0;
			percent_time_indeterminate=0.0;
			percent_time_indeterminate_scheduled=0.0;
			percent_time_indeterminate_unscheduled=0.0;
			percent_time_indeterminate_notrunning=0.0;
			percent_time_indeterminate_nodata=0.0;
			percent_time_ok_known=0.0;
			percent_time_ok_scheduled_known=0.0;
			percent_time_ok_unscheduled_known=0.0;
			percent_time_warning_known=0.0;
			percent_time_warning_scheduled_known=0.0;
			percent_time_warning_unscheduled_known=0.0;
			percent_time_unknown_known=0.0;
			percent_time_unknown_scheduled_known=0.0;
			percent_time_unknown_unscheduled_known=0.0;
			percent_time_critical_known=0.0;
			percent_time_critical_scheduled_known=0.0;
			percent_time_critical_unscheduled_known=0.0;
	
			if(total_time>0){
				percent_time_ok=((temp_subject.time_ok*100.0)/total_time);
				percent_time_ok_scheduled=((temp_subject.scheduled_time_ok*100.0)/total_time);
				percent_time_ok_unscheduled=percent_time_ok-percent_time_ok_scheduled;
				percent_time_warning=((temp_subject.time_warning*100.0)/total_time);
				percent_time_warning_scheduled=((temp_subject.scheduled_time_unknown*100.0)/total_time);
				percent_time_warning_unscheduled=percent_time_warning-percent_time_warning_scheduled;
				percent_time_unknown=((temp_subject.time_unknown*100.0)/total_time);
				percent_time_unknown_scheduled=((temp_subject.scheduled_time_unknown*100.0)/total_time);
				percent_time_unknown_unscheduled=percent_time_unknown-percent_time_unknown_scheduled;
				percent_time_critical=((temp_subject.time_critical*100.0)/total_time);
				percent_time_critical_scheduled=((temp_subject.scheduled_time_critical*100.0)/total_time);
				percent_time_critical_unscheduled=percent_time_critical-percent_time_critical_scheduled;
				percent_time_indeterminate=((time_indeterminate*100.0)/total_time);
				percent_time_indeterminate_scheduled=((temp_subject.scheduled_time_indeterminate*100.0)/total_time);
				percent_time_indeterminate_unscheduled=percent_time_indeterminate-percent_time_indeterminate_scheduled;
				percent_time_indeterminate_notrunning=((temp_subject.time_indeterminate_notrunning*100.0)/total_time);
				percent_time_indeterminate_nodata=((temp_subject.time_indeterminate_nodata*100.0)/total_time);
				if(time_determinate>0){
					percent_time_ok_known=((temp_subject.time_ok*100.0)/time_determinate);
					percent_time_ok_scheduled_known=((temp_subject.scheduled_time_ok*100.0)/time_determinate);
					percent_time_ok_unscheduled_known=percent_time_ok_known-percent_time_ok_scheduled_known;
					percent_time_warning_known=((temp_subject.time_warning*100.0)/time_determinate);
					percent_time_warning_scheduled_known=((temp_subject.scheduled_time_warning*100.0)/time_determinate);
					percent_time_warning_unscheduled_known=percent_time_warning_known-percent_time_warning_scheduled_known;
					percent_time_unknown_known=((temp_subject.time_unknown*100.0)/time_determinate);
					percent_time_unknown_scheduled_known=((temp_subject.scheduled_time_unknown*100.0)/time_determinate);
					percent_time_unknown_unscheduled_known=percent_time_unknown_known-percent_time_unknown_scheduled_known;
					percent_time_critical_known=((temp_subject.time_critical*100.0)/time_determinate);
					percent_time_critical_scheduled_known=((temp_subject.scheduled_time_critical*100.0)/time_determinate);
					percent_time_critical_unscheduled_known=percent_time_critical_known-percent_time_critical_scheduled_known;
		                        }
	                        }

			if(output_format==HTML_OUTPUT){

				if(odd!=0){
					odd=0;
					bgclass="Odd";
			                }
				else{
					odd=1;
					bgclass="Even";
			                }

				System.out.printf("<tr CLASS='data%s'><td CLASS='data%s'>",bgclass,bgclass);
				if(!temp_subject.host_name.equals(last_host))
					host_report_url(temp_subject.host_name,temp_subject.host_name);
				System.out.printf("</td><td CLASS='data%s'>",bgclass);
				service_report_url(temp_subject.host_name,temp_subject.service_description,temp_subject.service_description);
				System.out.printf("</td><td CLASS='serviceOK'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceWARNING'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceUNKNOWN'>%2.3f%% (%2.3f%%)</td><td class='serviceCRITICAL'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",percent_time_ok,percent_time_ok_known,percent_time_warning,percent_time_warning_known,percent_time_unknown,percent_time_unknown_known,percent_time_critical,percent_time_critical_known,bgclass,percent_time_indeterminate);
			        }
			else if(output_format==CSV_OUTPUT){

				/* host name and service description */
				System.out.printf("\"%s\", \"%s\",",temp_subject.host_name,temp_subject.service_description);

				/* ok times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_ok,percent_time_ok_scheduled,percent_time_ok_scheduled_known,temp_subject.time_ok-temp_subject.scheduled_time_ok,percent_time_ok_unscheduled,percent_time_ok_unscheduled_known,temp_subject.time_ok,percent_time_ok,percent_time_ok_known);

				/* warning times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_warning,percent_time_warning_scheduled,percent_time_warning_scheduled_known,temp_subject.time_warning-temp_subject.scheduled_time_warning,percent_time_warning_unscheduled,percent_time_warning_unscheduled_known,temp_subject.time_warning,percent_time_warning,percent_time_warning_known);

				/* unknown times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_unknown,percent_time_unknown_scheduled,percent_time_unknown_scheduled_known,temp_subject.time_unknown-temp_subject.scheduled_time_unknown,percent_time_unknown_unscheduled,percent_time_unknown_unscheduled_known,temp_subject.time_unknown,percent_time_unknown,percent_time_unknown_known);

				/* critical times */
				System.out.printf(" %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%, %d, %2.3f%%, %2.3f%%,",temp_subject.scheduled_time_critical,percent_time_critical_scheduled,percent_time_critical_scheduled_known,temp_subject.time_critical-temp_subject.scheduled_time_critical,percent_time_critical_unscheduled,percent_time_critical_unscheduled_known,temp_subject.time_critical,percent_time_critical,percent_time_critical_known);

				/* indeterminate times */
				System.out.printf(" %d, %2.3f%%, %d, %2.3f%%, %d, %2.3f%%\n",temp_subject.time_indeterminate_notrunning,percent_time_indeterminate_notrunning,temp_subject.time_indeterminate_nodata,percent_time_indeterminate_nodata,time_indeterminate,percent_time_indeterminate);
			        }

			last_host=temp_subject.host_name;

			average_percent_time_ok = get_running_average(average_percent_time_ok,percent_time_ok,current_subject);
			average_percent_time_ok_known = get_running_average(average_percent_time_ok_known,percent_time_ok_known,current_subject);
			average_percent_time_unknown = get_running_average(average_percent_time_unknown,percent_time_unknown,current_subject);
			average_percent_time_unknown_known = get_running_average(average_percent_time_unknown_known,percent_time_unknown_known,current_subject);
			average_percent_time_warning = get_running_average(average_percent_time_warning,percent_time_warning,current_subject);
			average_percent_time_warning_known = get_running_average(average_percent_time_warning_known,percent_time_warning_known,current_subject);
			average_percent_time_critical = get_running_average(average_percent_time_critical,percent_time_critical,current_subject);
			average_percent_time_critical_known = get_running_average(average_percent_time_critical_known,percent_time_critical_known,current_subject);
			average_percent_time_indeterminate = get_running_average(average_percent_time_indeterminate,percent_time_indeterminate,current_subject);
                        }

		if(output_format==HTML_OUTPUT){

			/* average statistics */
			if(odd != 0){
				odd=0;
				bgclass="Odd";
	                        }
			else{
				odd=1;
				bgclass="Even";
      	                        }
			System.out.printf("<tr CLASS='data%s'><td CLASS='data%s' colspan='2'>Average</td><td CLASS='serviceOK'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceWARNING'>%2.3f%% (%2.3f%%)</td><td CLASS='serviceUNKNOWN'>%2.3f%% (%2.3f%%)</td><td class='serviceCRITICAL'>%2.3f%% (%2.3f%%)</td><td class='data%s'>%2.3f%%</td></tr>\n",bgclass,bgclass,average_percent_time_ok,average_percent_time_ok_known,average_percent_time_warning,average_percent_time_warning_known,average_percent_time_unknown,average_percent_time_unknown_known,average_percent_time_critical,average_percent_time_critical_known,bgclass,average_percent_time_indeterminate);

			System.out.printf("</table>\n");
			System.out.printf("</DIV>\n");
		        }
	        }

	return;
        }




public static void host_report_url(String hn, String label){

	System.out.printf("<a href='%s?host=%s", cgiutils_h.AVAIL_CGI,cgiutils.url_encode(hn));
	System.out.printf("&show_log_entries");
	System.out.printf("&t1=%d&t2=%d",t1,t2);
	System.out.printf("&backtrack=%d",backtrack_archives);
	System.out.printf("&assumestateretention=%s",(assume_state_retention==common_h.TRUE)?"yes":"no");
	System.out.printf("&assumeinitialstates=%s",(assume_initial_states==common_h.TRUE)?"yes":"no");
	System.out.printf("&assumestatesduringnotrunning=%s",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no");
	System.out.printf("&initialassumedhoststate=%d",initial_assumed_host_state);
	System.out.printf("&initialassumedservicestate=%d",initial_assumed_service_state);
	if(show_log_entries==common_h.TRUE)
		System.out.printf("&show_log_entries");
	if(full_log_entries==common_h.TRUE)
		System.out.printf("&full_log_entries");
	System.out.printf("&showscheduleddowntime=%s",(show_scheduled_downtime==common_h.TRUE)?"yes":"no");
	if(current_timeperiod!=null)
		System.out.printf("&rpttimeperiod=%s",cgiutils.url_encode(current_timeperiod.name));
	System.out.printf("'>%s</a>",label);

	return;
        }


public static void service_report_url(String hn, String sd, String label){

	System.out.printf("<a href='%s?host=%s",cgiutils_h.AVAIL_CGI,cgiutils.url_encode(hn));
	System.out.printf("&service=%s",cgiutils.url_encode(sd));
	System.out.printf("&t1=%d&t2=%d",t1,t2);
	System.out.printf("&backtrack=%d",backtrack_archives);
	System.out.printf("&assumestateretention=%s",(assume_state_retention==common_h.TRUE)?"yes":"no");
	System.out.printf("&assumeinitialstates=%s",(assume_initial_states==common_h.TRUE)?"yes":"no");
	System.out.printf("&assumestatesduringnotrunning=%s",(assume_states_during_notrunning==common_h.TRUE)?"yes":"no");
	System.out.printf("&initialassumedhoststate=%d",initial_assumed_host_state);
	System.out.printf("&initialassumedservicestate=%d",initial_assumed_service_state);
	if(show_log_entries==common_h.TRUE)
		System.out.printf("&show_log_entries");
	if(full_log_entries==common_h.TRUE)
		System.out.printf("&full_log_entries");
	System.out.printf("&showscheduleddowntime=%s",(show_scheduled_downtime==common_h.TRUE)?"yes":"no");
	if(current_timeperiod!=null)
		System.out.printf("&rpttimeperiod=%s",cgiutils.url_encode(current_timeperiod.name));
	System.out.printf("'>%s</a>",label);

	return;
        }


/* calculates running average */
public static double get_running_average(double running_average, double new_value, int current_item){

	running_average=(((running_average*(current_item-1.0))+new_value)/current_item);

	return running_average;
        }


/* used in reports where a timeperiod is selected */
public static long calculate_total_time(long start_time, long end_time){
	Calendar t;
	long midnight_today;
	int weekday;
	long total_time;
//	objects_h.timerange temp_timerange;
	long temp_duration;
	long temp_end;
	long temp_start;
	long start;
	long end;

	/* attempt to handle the current time_period */
	if(current_timeperiod!=null){

		/* "A day" is 86400 seconds */
        t = Calendar.getInstance();
		t.setTimeInMillis(start_time * 1000);

		/* calculate the start of the day (midnight, 00:00 hours) */
		t.set( Calendar.SECOND, 0 );
		t.set( Calendar.MINUTE, 0 );
		t.set( Calendar.HOUR, 0 );
		midnight_today= utils.getTimeInSeconds(t);
		weekday=t.get( Calendar.DAY_OF_WEEK )-1;

		total_time=0;
		while(midnight_today<end_time){
			temp_duration=0;
			temp_end=Math.min(86400,t2-midnight_today);
			temp_start=0;
			if(t1>midnight_today)
				temp_start=t1-midnight_today;

			/* check all time ranges for this day of the week */
			for( objects_h.timerange temp_timerange : (ArrayList<objects_h.timerange>) current_timeperiod.days[weekday] ){
				start=Math.max(temp_timerange.range_start,temp_start);
				end= Math.min(temp_timerange.range_end,temp_end);

                // TODO DEBUG
//				System.out.printf("<li>Matching in timerange[%d]: %d . %d (%ld . %ld) %d . %d = %ld<br>\n",weekday,temp_timerange.range_start,temp_timerange.range_end,temp_start,temp_end,start,end,end-start);
                
				if(end>start)
					temp_duration += end-start;
			        }
			total_time+=temp_duration;
			temp_start=0;
			midnight_today+=86400;
			if(++weekday>6)
				weekday=0;
		        }

		return total_time;
	        }

	/* no timeperiod was selected */
	return end_time-start_time;
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