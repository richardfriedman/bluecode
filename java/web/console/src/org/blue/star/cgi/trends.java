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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import javax.imageio.ImageIO;

import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class trends extends blue_servlet
{

   /* archived state types */
   public static final int AS_CURRENT_STATE = -1; /* special case for initial assumed state */

   public static final int AS_NO_DATA = 0;

   public static final int AS_PROGRAM_END = 1;

   public static final int AS_PROGRAM_START = 2;

   public static final int AS_HOST_UP = 3;

   public static final int AS_HOST_DOWN = 4;

   public static final int AS_HOST_UNREACHABLE = 5;

   public static final int AS_SVC_OK = 6;

   public static final int AS_SVC_UNKNOWN = 7;

   public static final int AS_SVC_WARNING = 8;

   public static final int AS_SVC_CRITICAL = 9;

   public static final int AS_SOFT_STATE = 1;

   public static final int AS_HARD_STATE = 2;

   /* display types */
   public static final int DISPLAY_HOST_TRENDS = 0;

   public static final int DISPLAY_SERVICE_TRENDS = 1;

   public static final int DISPLAY_NO_TRENDS = 2;

   /* input types */
   public static final int GET_INPUT_NONE = 0;

   public static final int GET_INPUT_TARGET_TYPE = 1;

   public static final int GET_INPUT_HOST_TARGET = 2;

   public static final int GET_INPUT_SERVICE_TARGET = 3;

   public static final int GET_INPUT_OPTIONS = 4;

   /* modes */
   public static final int CREATE_HTML = 0;

   public static final int CREATE_IMAGE = 1;

   /* standard report times */
   public static final int TIMEPERIOD_CUSTOM = 0;

   public static final int TIMEPERIOD_TODAY = 1;

   public static final int TIMEPERIOD_YESTERDAY = 2;

   public static final int TIMEPERIOD_THISWEEK = 3;

   public static final int TIMEPERIOD_LASTWEEK = 4;

   public static final int TIMEPERIOD_THISMONTH = 5;

   public static final int TIMEPERIOD_LASTMONTH = 6;

   public static final int TIMEPERIOD_THISQUARTER = 7;

   public static final int TIMEPERIOD_LASTQUARTER = 8;

   public static final int TIMEPERIOD_THISYEAR = 9;

   public static final int TIMEPERIOD_LASTYEAR = 10;

   public static final int TIMEPERIOD_LAST24HOURS = 11;

   public static final int TIMEPERIOD_LAST7DAYS = 12;

   public static final int TIMEPERIOD_LAST31DAYS = 13;

   public static final int MIN_TIMESTAMP_SPACING = 10;

   public static final int MAX_ARCHIVE_SPREAD = 65;

   public static final int MAX_ARCHIVE = 65;

   public static final int MAX_ARCHIVE_BACKTRACKS = 60;

   public static class archived_state
   {
      public long time_stamp;

      public int entry_type;

      public int processed_state;

      public int state_type;

      public String state_info;
   }

   public static cgiauth_h.authdata current_authdata;

   public static ArrayList<archived_state> as_list = new ArrayList<archived_state>();

   public static long t1;

   public static long t2;

   public static int start_second = 0;

   public static int start_minute = 0;

   public static int start_hour = 0;

   public static int start_day = 1;

   public static int start_month = 1;

   public static int start_year = 2000;

   public static int end_second = 0;

   public static int end_minute = 0;

   public static int end_hour = 24;

   public static int end_day = 1;

   public static int end_month = 1;

   public static int end_year = 2000;

   public static int display_type = DISPLAY_NO_TRENDS;

   public static int mode = CREATE_HTML;

   public static int input_type = GET_INPUT_NONE;

   public static int timeperiod_type = TIMEPERIOD_LAST24HOURS;

   public static int compute_time_from_parts = common_h.FALSE;

   public static int display_popups = common_h.TRUE;

   public static int use_map = common_h.TRUE;

   public static int small_image = common_h.FALSE;

   public static int embedded = common_h.FALSE;

   public static int display_header = common_h.TRUE;

   public static int assume_initial_states = common_h.TRUE;

   public static int assume_state_retention = common_h.TRUE;

   public static int assume_states_during_notrunning = common_h.TRUE;

   public static int include_soft_states = common_h.FALSE;

   public static String host_name = "";

   public static String svc_description = "";

   public static BufferedImage trends_image = null;

   public static Graphics2D gd = null;

   public static Color color_white;

   public static Color color_black;

   public static Color color_red;

   public static Color color_darkred;

   public static Color color_green;

   public static Color color_darkgreen;

   public static Color color_yellow;

   public static Color color_orange;

   public static File image_file = null;

   public static int image_width = 600;

   public static int image_height = 300;

   public static int HOST_DRAWING_WIDTH = 498;

   public static int HOST_DRAWING_HEIGHT = 70;

   public static int HOST_DRAWING_X_OFFSET = 116;

   public static int HOST_DRAWING_Y_OFFSET = 55;

   public static int SVC_DRAWING_WIDTH = 498;

   public static int SVC_DRAWING_HEIGHT = 90;

   public static int SVC_DRAWING_X_OFFSET = 116;

   public static int SVC_DRAWING_Y_OFFSET = 55;

   public static int SMALL_HOST_DRAWING_WIDTH = 500;

   public static int SMALL_HOST_DRAWING_HEIGHT = 20;

   public static int SMALL_HOST_DRAWING_X_OFFSET = 0;

   public static int SMALL_HOST_DRAWING_Y_OFFSET = 0;

   public static int SMALL_SVC_DRAWING_WIDTH = 500;

   public static int SMALL_SVC_DRAWING_HEIGHT = 20;

   public static int SMALL_SVC_DRAWING_X_OFFSET = 0;

   public static int SMALL_SVC_DRAWING_Y_OFFSET = 0;

   public static int drawing_width = 0;

   public static int drawing_height = 0;

   public static int drawing_x_offset = 0;

   public static int drawing_y_offset = 0;

   public static int last_known_state = AS_NO_DATA;

   public static int zoom_factor = 4;

   public static int backtrack_archives = 2;

   public static int earliest_archive = 0;

   public static long earliest_time;

   public static long latest_time;

   public static int earliest_state = AS_NO_DATA;

   public static int latest_state = AS_NO_DATA;

   public static int initial_assumed_host_state = AS_NO_DATA;

   public static int initial_assumed_service_state = AS_NO_DATA;

   public static long time_up = 0L;

   public static long time_down = 0L;

   public static long time_unreachable = 0L;

   public static long time_ok = 0L;

   public static long time_warning = 0L;

   public static long time_unknown = 0L;

   public static long time_critical = 0L;

   public void reset_context()
   {
      current_authdata = new cgiauth_h.authdata();

      as_list.clear();

      t1 = 0;
      t2 = 0;

      start_second = 0;
      start_minute = 0;
      start_hour = 0;
      start_day = 1;
      start_month = 1;
      start_year = 2000;
      end_second = 0;
      end_minute = 0;
      end_hour = 24;
      end_day = 1;
      end_month = 1;
      end_year = 2000;

      display_type = DISPLAY_NO_TRENDS;
      mode = CREATE_HTML;
      input_type = GET_INPUT_NONE;
      timeperiod_type = TIMEPERIOD_LAST24HOURS;
      compute_time_from_parts = common_h.FALSE;

      display_popups = common_h.TRUE;
      use_map = common_h.TRUE;
      small_image = common_h.FALSE;
      embedded = common_h.FALSE;
      display_header = common_h.TRUE;

      assume_initial_states = common_h.TRUE;
      assume_state_retention = common_h.TRUE;
      assume_states_during_notrunning = common_h.TRUE;
      include_soft_states = common_h.FALSE;

      host_name = "";
      svc_description = "";

      trends_image = null;
      gd = null;

      color_white = null;
      color_black = null;
      color_red = null;
      color_darkred = null;
      color_green = null;
      color_darkgreen = null;
      color_yellow = null;
      color_orange = null;

      image_file = null;

      image_width = 600;
      image_height = 300;

      HOST_DRAWING_WIDTH = 498;
      HOST_DRAWING_HEIGHT = 70;
      HOST_DRAWING_X_OFFSET = 116;
      HOST_DRAWING_Y_OFFSET = 55;

      SVC_DRAWING_WIDTH = 498;
      SVC_DRAWING_HEIGHT = 90;
      SVC_DRAWING_X_OFFSET = 116;
      SVC_DRAWING_Y_OFFSET = 55;

      SMALL_HOST_DRAWING_WIDTH = 500;
      SMALL_HOST_DRAWING_HEIGHT = 20;
      SMALL_HOST_DRAWING_X_OFFSET = 0;
      SMALL_HOST_DRAWING_Y_OFFSET = 0;

      SMALL_SVC_DRAWING_WIDTH = 500;
      SMALL_SVC_DRAWING_HEIGHT = 20;
      SMALL_SVC_DRAWING_X_OFFSET = 0;
      SMALL_SVC_DRAWING_Y_OFFSET = 0;

      drawing_width = 0;
      drawing_height = 0;

      drawing_x_offset = 0;
      drawing_y_offset = 0;

      last_known_state = AS_NO_DATA;

      zoom_factor = 4;
      backtrack_archives = 2;
      earliest_archive = 0;
      earliest_time = 0;
      latest_time = 0;
      earliest_state = AS_NO_DATA;
      latest_state = AS_NO_DATA;

      initial_assumed_host_state = AS_NO_DATA;
      initial_assumed_service_state = AS_NO_DATA;

      time_up = 0L;
      time_down = 0L;
      time_unreachable = 0L;
      time_ok = 0L;
      time_warning = 0L;
      time_unknown = 0L;
      time_critical = 0L;
   }

   public void call_main()
   {
      main(null);
   }

   public static void main(String[] args)
   {
      int result = common_h.OK;
      String temp_buffer; // MAX_INPUT_BUFFER
      String image_template; // MAX_INPUT_BUFFER
      String start_time; // MAX_INPUT_BUFFER
      String end_time; // MAX_INPUT_BUFFER
      int string_width;
      int string_height;
      String start_timestring; // MAX_INPUT_BUFFER
      String end_timestring; // MAX_INPUT_BUFFER
      objects_h.host temp_host;
      objects_h.service temp_service;
      int is_authorized = common_h.TRUE;
      int found = common_h.FALSE;
      String first_service = null;
      long t3;
      long current_time;
      Calendar t = Calendar.getInstance();

      /* get the arguments passed in the URL */
      process_cgivars();

      /* read the CGI configuration file */
      result = cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
      if (result == common_h.ERROR)
      {
         if (mode == CREATE_HTML)
         {
            document_header(common_h.FALSE);
            cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
            document_footer();
         }
         cgiutils.exit(common_h.ERROR);
         return;
      }

      /* read the main configuration file */
      result = cgiutils.read_main_config_file(cgiutils.main_config_file);
      if (result == common_h.ERROR)
      {
         if (mode == CREATE_HTML)
         {
            document_header(common_h.FALSE);
            cgiutils.main_config_file_error(cgiutils.main_config_file);
            document_footer();
         }
         cgiutils.exit(common_h.ERROR);
         return;
      }

      /* initialize time period to last 24 hours */
      current_time = utils.currentTimeInSeconds();
      t2 = current_time;
      t1 = (current_time - (60 * 60 * 24));

      /* default number of backtracked archives */
      switch (cgiutils.log_rotation_method)
      {
         case common_h.LOG_ROTATION_MONTHLY :
            backtrack_archives = 1;
            break;
         case common_h.LOG_ROTATION_WEEKLY :
            backtrack_archives = 2;
            break;
         case common_h.LOG_ROTATION_DAILY :
            backtrack_archives = 4;
            break;
         case common_h.LOG_ROTATION_HOURLY :
            backtrack_archives = 8;
            break;
         default :
            backtrack_archives = 2;
            break;
      }

      /* get authentication information */
      cgiauth.get_authentication_information(current_authdata);

      /* read all object configuration data */
      result = cgiutils.read_all_object_configuration_data(cgiutils.main_config_file, common_h.READ_ALL_OBJECT_DATA);
      if (result == common_h.ERROR)
      {
         if (mode == CREATE_HTML)
         {
            document_header(common_h.FALSE);
            cgiutils.object_data_error();
            document_footer();
         }
         cgiutils.exit(common_h.ERROR);
         return;
      }

      /* read all status data */
      result = cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(), statusdata_h.READ_ALL_STATUS_DATA);
      if (result == common_h.ERROR)
      {
         if (mode == CREATE_HTML)
         {
            document_header(common_h.FALSE);
            cgiutils.status_data_error();
            document_footer();
         }
         cgiutils.exit(common_h.ERROR);
         return;
      }

      document_header(common_h.TRUE);

      if (compute_time_from_parts == common_h.TRUE)
         compute_report_times();

      /* make sure times are sane, otherwise swap them */
      if (t2 < t1)
      {
         t3 = t2;
         t2 = t1;
         t1 = t3;
      }

      /* don't let user create reports in the future */
      if (t2 > current_time)
      {
         t2 = current_time;
         if (t1 > t2)
            t1 = t2 - (60 * 60 * 24);
      }

      if (mode == CREATE_HTML && display_header == common_h.TRUE)
      {

         /* begin top table */
         System.out.printf("<table border=0 width=100%% cellspacing=0 cellpadding=0>\n");
         System.out.printf("<tr>\n");

         /* left column of the first row */
         System.out.printf("<td align=left valign=top width=33%%>\n");

         if (display_type == DISPLAY_HOST_TRENDS)
            temp_buffer = "Host State Trends";
         else if (display_type == DISPLAY_SERVICE_TRENDS)
            temp_buffer = "Service State Trends";
         else
            temp_buffer = "Host and Service State Trends";

         cgiutils.display_info_table(temp_buffer, common_h.FALSE, current_authdata);

         if (display_type != DISPLAY_NO_TRENDS && input_type == GET_INPUT_NONE)
         {

            System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
            System.out.printf("<TR><TD CLASS='linkBox'>\n");

            if (display_type == DISPLAY_HOST_TRENDS)
            {
               System.out
                     .printf(
                           "<a href='%s?host=%s&t1=%d&t2=%d&includesoftstates=%s&assumestateretention=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedhoststate=%d&backtrack=%d&show_log_entries'>View Availability Report For This Host</a><BR>\n",
                           cgiutils_h.AVAIL_CGI, cgiutils.url_encode(host_name), t1, t2,
                           (include_soft_states == common_h.TRUE) ? "yes" : "no",
                           (assume_state_retention == common_h.TRUE) ? "yes" : "no",
                           (assume_initial_states == common_h.TRUE) ? "yes" : "no",
                           (assume_states_during_notrunning == common_h.TRUE) ? "yes" : "no",
                           initial_assumed_host_state, backtrack_archives);
               System.out
                     .printf(
                           "<a href='%s?host=%s&t1=%d&t2=%d&assumestateretention=%s'>View Alert Histogram For This Host</a><BR>\n",
                           cgiutils_h.HISTOGRAM_CGI, cgiutils.url_encode(host_name), t1, t2,
                           (assume_state_retention == common_h.TRUE) ? "yes" : "no");
               System.out.printf("<a href='%s?host=%s'>View Status Detail For This Host</a><BR>\n",
                     cgiutils_h.STATUS_CGI, cgiutils.url_encode(host_name));
               System.out.printf("<a href='%s?host=%s'>View Alert History For This Host</a><BR>\n",
                     cgiutils_h.HISTORY_CGI, cgiutils.url_encode(host_name));
               System.out.printf("<a href='%s?host=%s'>View Notifications For This Host</a><BR>\n",
                     cgiutils_h.NOTIFICATIONS_CGI, cgiutils.url_encode(host_name));
            }
            else
            {
               System.out
                     .printf(
                           "<a href='%s?host=%s&t1=%d&t2=%d&includesoftstates=%s&assumestateretention=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedservicestate=%d&backtrack=%d'>View Trends For This Host</a><BR>\n",
                           cgiutils_h.TRENDS_CGI, cgiutils.url_encode(host_name), t1, t2,
                           (include_soft_states == common_h.TRUE) ? "yes" : "no",
                           (assume_state_retention == common_h.TRUE) ? "yes" : "no",
                           (assume_initial_states == common_h.TRUE) ? "yes" : "no",
                           (assume_states_during_notrunning == common_h.TRUE) ? "yes" : "no",
                           initial_assumed_service_state, backtrack_archives);
               System.out.printf("<a href='%s?host=%s", cgiutils_h.AVAIL_CGI, cgiutils.url_encode(host_name));
               System.out
                     .printf(
                           "&service=%s&t1=%d&t2=%d&assumestateretention=%s&includesoftstates=%s&assumeinitialstates=%s&assumestatesduringnotrunning=%s&initialassumedservicestate=%d&backtrack=%d&show_log_entries'>View Availability Report For This Service</a><BR>\n",
                           cgiutils.url_encode(svc_description), t1, t2, (include_soft_states == common_h.TRUE)
                                 ? "yes"
                                 : "no", (assume_state_retention == common_h.TRUE) ? "yes" : "no",
                           (assume_initial_states == common_h.TRUE) ? "yes" : "no",
                           (assume_states_during_notrunning == common_h.TRUE) ? "yes" : "no",
                           initial_assumed_service_state, backtrack_archives);
               System.out.printf("<a href='%s?host=%s", cgiutils_h.HISTOGRAM_CGI, cgiutils.url_encode(host_name));
               System.out
                     .printf(
                           "&service=%s&t1=%d&t2=%d&assumestateretention=%s'>View Alert Histogram For This Service</a><BR>\n",
                           cgiutils.url_encode(svc_description), t1, t2, (assume_state_retention == common_h.TRUE)
                                 ? "yes"
                                 : "no");
               System.out.printf("<A HREF='%s?host=%s&", cgiutils_h.HISTORY_CGI, cgiutils.url_encode(host_name));
               System.out.printf("service=%s'>View Alert History This Service</A><BR>\n", cgiutils
                     .url_encode(svc_description));
               System.out.printf("<A HREF='%s?host=%s&", cgiutils_h.NOTIFICATIONS_CGI, cgiutils.url_encode(host_name));
               System.out.printf("service=%s'>View Notifications For This Service</A><BR>\n", cgiutils
                     .url_encode(svc_description));
            }

            System.out.printf("</TD></TR>\n");
            System.out.printf("</TABLE>\n");
         }

         System.out.printf("</td>\n");

         /* center column of top row */
         System.out.printf("<td align=center valign=top width=33%%>\n");

         if (display_type != DISPLAY_NO_TRENDS && input_type == GET_INPUT_NONE)
         {

            System.out.printf("<DIV ALIGN=CENTER CLASS='dataTitle'>\n");
            if (display_type == DISPLAY_HOST_TRENDS)
               System.out.printf("Host '%s'", host_name);
            else if (display_type == DISPLAY_SERVICE_TRENDS)
               System.out.printf("Service '%s' On Host '%s'", svc_description, host_name);
            System.out.printf("</DIV>\n");

            System.out.printf("<BR>\n");

            System.out.printf("<IMG SRC='%s%s' BORDER=0 ALT='%s State Trends' TITLE='%s State Trends'>\n",
                  cgiutils.url_images_path, cgiutils_h.TRENDS_ICON, (display_type == DISPLAY_HOST_TRENDS)
                        ? "Host"
                        : "Service", (display_type == DISPLAY_HOST_TRENDS) ? "Host" : "Service");

            System.out.printf("<BR CLEAR=ALL>\n");

            start_timestring = cgiutils.get_time_string(t1, common_h.SHORT_DATE_TIME);
            end_timestring = cgiutils.get_time_string(t2, common_h.SHORT_DATE_TIME);
            System.out.printf("<div align=center class='reportRange'>%s to %s</div>\n", start_timestring,
                  end_timestring);

            cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((t2 - t1));
            System.out.printf("<div align=center class='reportDuration'>Duration: %dd %dh %dm %ds</div>\n", tb.days,
                  tb.hours, tb.minutes, tb.seconds);
         }

         System.out.printf("</td>\n");

         /* right hand column of top row */
         System.out.printf("<td align=right valign=bottom width=33%%>\n");

         System.out.printf("<table border=0 CLASS='optBox'>\n");

         if (display_type != DISPLAY_NO_TRENDS && input_type == GET_INPUT_NONE)
         {

            System.out.printf("<form method=\"GET\" action=\"%s\">\n", cgiutils_h.TRENDS_CGI);
            if (display_popups == common_h.FALSE)
               System.out.printf("<input type='hidden' name='nopopups' value=''>\n");
            if (use_map == common_h.FALSE)
               System.out.printf("<input type='hidden' name='nomap' value=''>\n");
            System.out.printf("<input type='hidden' name='t1' value='%d'>\n", t1);
            System.out.printf("<input type='hidden' name='t2' value='%d'>\n", t2);
            System.out.printf("<input type='hidden' name='host' value='%s'>\n", host_name);
            if (display_type == DISPLAY_SERVICE_TRENDS)
               System.out.printf("<input type='hidden' name='service' value='%s'>\n", svc_description);

            System.out.printf("<input type='hidden' name='assumeinitialstates' value='%s'>\n",
                  (assume_initial_states == common_h.TRUE) ? "yes" : "no");
            System.out.printf("<input type='hidden' name='assumestateretention' value='%s'>\n",
                  (assume_state_retention == common_h.TRUE) ? "yes" : "no");
            System.out.printf("<input type='hidden' name='assumestatesduringnotrunning' value='%s'>\n",
                  (assume_states_during_notrunning == common_h.TRUE) ? "yes" : "no");
            System.out.printf("<input type='hidden' name='includesoftstates' value='%s'>\n",
                  (include_soft_states == common_h.TRUE) ? "yes" : "no");

            System.out
                  .printf(
                        "<tr><td CLASS='optBoxItem' valign=top align=left>First assumed %s state:</td><td CLASS='optBoxItem' valign=top align=left>Backtracked archives:</td></tr>\n",
                        (display_type == DISPLAY_HOST_TRENDS) ? "host" : "service");
            System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>");
            if (display_type == DISPLAY_HOST_TRENDS)
            {
               System.out.printf("<input type='hidden' name='initialassumedservicestate' value='%d'>",
                     initial_assumed_service_state);
               System.out.printf("<select name='initialassumedhoststate'>\n");
               System.out.printf("<option value=%d %s>Unspecified\n", AS_NO_DATA,
                     (initial_assumed_host_state == AS_NO_DATA) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Current State\n", AS_CURRENT_STATE,
                     (initial_assumed_host_state == AS_CURRENT_STATE) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Host Up\n", AS_HOST_UP,
                     (initial_assumed_host_state == AS_HOST_UP) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Host Down\n", AS_HOST_DOWN,
                     (initial_assumed_host_state == AS_HOST_DOWN) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Host Unreachable\n", AS_HOST_UNREACHABLE,
                     (initial_assumed_host_state == AS_HOST_UNREACHABLE) ? "SELECTED" : "");
            }
            else
            {
               System.out.printf("<input type='hidden' name='initialassumedhoststate' value='%d'>",
                     initial_assumed_host_state);
               System.out.printf("<select name='initialassumedservicestate'>\n");
               System.out.printf("<option value=%d %s>Unspecified\n", AS_NO_DATA,
                     (initial_assumed_service_state == AS_NO_DATA) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Current State\n", AS_CURRENT_STATE,
                     (initial_assumed_service_state == AS_CURRENT_STATE) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Service Ok\n", AS_SVC_OK,
                     (initial_assumed_service_state == AS_SVC_OK) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Service Warning\n", AS_SVC_WARNING,
                     (initial_assumed_service_state == AS_SVC_WARNING) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Service Unknown\n", AS_SVC_UNKNOWN,
                     (initial_assumed_service_state == AS_SVC_UNKNOWN) ? "SELECTED" : "");
               System.out.printf("<option value=%d %s>Service Critical\n", AS_SVC_CRITICAL,
                     (initial_assumed_service_state == AS_SVC_CRITICAL) ? "SELECTED" : "");
            }
            System.out.printf("</select>\n");
            System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
            System.out.printf("<input type='text' name='backtrack' size='2' maxlength='2' value='%d'>\n",
                  backtrack_archives);
            System.out.printf("</td></tr>\n");

            System.out
                  .printf("<tr><td CLASS='optBoxItem' valign=top align=left>Report period:</td><td CLASS='optBoxItem' valign=top align=left>Zoom factor:</td></tr>\n");
            System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>\n");
            System.out.printf("<select name='timeperiod'>\n");
            System.out.printf("<option>[ Current time range ]\n");
            System.out
                  .printf("<option value=today %s>Today\n", (timeperiod_type == TIMEPERIOD_TODAY) ? "SELECTED" : "");
            System.out.printf("<option value=last24hours %s>Last 24 Hours\n",
                  (timeperiod_type == TIMEPERIOD_LAST24HOURS) ? "SELECTED" : "");
            System.out.printf("<option value=yesterday %s>Yesterday\n", (timeperiod_type == TIMEPERIOD_YESTERDAY)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=thisweek %s>This Week\n", (timeperiod_type == TIMEPERIOD_THISWEEK)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=last7days %s>Last 7 Days\n", (timeperiod_type == TIMEPERIOD_LAST7DAYS)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=lastweek %s>Last Week\n", (timeperiod_type == TIMEPERIOD_LASTWEEK)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=thismonth %s>This Month\n", (timeperiod_type == TIMEPERIOD_THISMONTH)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=last31days %s>Last 31 Days\n", (timeperiod_type == TIMEPERIOD_LAST31DAYS)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=lastmonth %s>Last Month\n", (timeperiod_type == TIMEPERIOD_LASTMONTH)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=thisyear %s>This Year\n", (timeperiod_type == TIMEPERIOD_THISYEAR)
                  ? "SELECTED"
                  : "");
            System.out.printf("<option value=lastyear %s>Last Year\n", (timeperiod_type == TIMEPERIOD_LASTYEAR)
                  ? "SELECTED"
                  : "");
            System.out.printf("</select>\n");
            System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
            System.out.printf("<select name='zoom'>\n");
            System.out.printf("<option value=%d selected>%d\n", zoom_factor, zoom_factor);
            System.out.printf("<option value=+2>+2\n");
            System.out.printf("<option value=+3>+3\n");
            System.out.printf("<option value=+4>+4\n");
            System.out.printf("<option value=-2>-2\n");
            System.out.printf("<option value=-3>-3\n");
            System.out.printf("<option value=-4>-4\n");
            System.out.printf("</select>\n");
            System.out.printf("</td></tr>\n");

            System.out.printf("<tr><td CLASS='optBoxItem' valign=top align=left>\n");
            System.out.printf("</td><td CLASS='optBoxItem' valign=top align=left>\n");
            System.out.printf("<input type='submit' value='Update'>\n");
            System.out.printf("</td></tr>\n");

            System.out.printf("</form>\n");
         }

         /* display context-sensitive help */
         System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
         if (display_type != DISPLAY_NO_TRENDS && input_type == GET_INPUT_NONE)
         {
            if (display_type == DISPLAY_HOST_TRENDS)
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_HOST);
            else
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_SERVICE);
         }
         else if (display_type == DISPLAY_NO_TRENDS || input_type != GET_INPUT_NONE)
         {
            if (input_type == GET_INPUT_NONE)
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_MENU1);
            else if (input_type == GET_INPUT_TARGET_TYPE)
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_MENU1);
            else if (input_type == GET_INPUT_HOST_TARGET)
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_MENU2);
            else if (input_type == GET_INPUT_SERVICE_TARGET)
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_MENU3);
            else if (input_type == GET_INPUT_OPTIONS)
               cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TRENDS_MENU4);
         }
         System.out.printf("</td></tr>\n");

         System.out.printf("</table>\n");

         System.out.printf("</td>\n");

         /* end of top table */
         System.out.printf("</tr>\n");
         System.out.printf("</table>\n");
      }

      //#ifndef DEBUG
      /* check authorization... */
      if (display_type == DISPLAY_HOST_TRENDS)
      {
         temp_host = objects.find_host(host_name);
         if (temp_host == null || cgiauth.is_authorized_for_host(temp_host, current_authdata) == common_h.FALSE)
            is_authorized = common_h.FALSE;
      }
      else if (display_type == DISPLAY_SERVICE_TRENDS)
      {
         temp_service = objects.find_service(host_name, svc_description);
         if (temp_service == null
               || cgiauth.is_authorized_for_service(temp_service, current_authdata) == common_h.FALSE)
            is_authorized = common_h.FALSE;
      }
      if (is_authorized == common_h.FALSE)
      {

         if (mode == CREATE_HTML)
            System.out
                  .printf(
                        "<P><DIV ALIGN=CENTER CLASS='errorMessage'>It appears as though you are not authorized to view information for the specified %s...</DIV></P>\n",
                        (display_type == DISPLAY_HOST_TRENDS) ? "host" : "service");

         document_footer();
         cgiutils.free_memory();
         cgiutils.exit(common_h.ERROR);
      }
      //#endif

      /* set drawing parameters, etc */

      if (small_image == common_h.TRUE)
      {
         image_height = 20;
         image_width = 500;
      }
      else
      {
         image_height = 300;
         image_width = 600;
      }

      if (display_type == DISPLAY_HOST_TRENDS)
      {

         if (small_image == common_h.TRUE)
         {
            drawing_width = SMALL_HOST_DRAWING_WIDTH;
            drawing_height = SMALL_HOST_DRAWING_HEIGHT;
            drawing_x_offset = SMALL_HOST_DRAWING_X_OFFSET;
            drawing_y_offset = SMALL_HOST_DRAWING_Y_OFFSET;
         }
         else
         {
            drawing_width = HOST_DRAWING_WIDTH;
            drawing_height = HOST_DRAWING_HEIGHT;
            drawing_x_offset = HOST_DRAWING_X_OFFSET;
            drawing_y_offset = HOST_DRAWING_Y_OFFSET;
         }
      }
      else if (display_type == DISPLAY_SERVICE_TRENDS)
      {

         if (small_image == common_h.TRUE)
         {
            drawing_width = SMALL_SVC_DRAWING_WIDTH;
            drawing_height = SMALL_SVC_DRAWING_HEIGHT;
            drawing_x_offset = SMALL_SVC_DRAWING_X_OFFSET;
            drawing_y_offset = SMALL_SVC_DRAWING_Y_OFFSET;
         }
         else
         {
            drawing_width = SVC_DRAWING_WIDTH;
            drawing_height = SVC_DRAWING_HEIGHT;
            drawing_x_offset = SVC_DRAWING_X_OFFSET;
            drawing_y_offset = SVC_DRAWING_Y_OFFSET;
         }
      }

      /* last known state should always be initially set to indeterminate! */
      last_known_state = AS_NO_DATA;

      /* initialize PNG image */
      if (display_type != DISPLAY_NO_TRENDS && mode == CREATE_IMAGE)
      {

         if (small_image == common_h.TRUE)
         { // TODO what image type should we use.
            trends_image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_ARGB);
            if (trends_image == null)
            {
               //#ifdef DEBUG
               //				System.out.printf("Error: Could not allocate memory for image\n");
               //#endif
               cgiutils.exit(common_h.ERROR);
            }
         }

         else
         {

            if (display_type == DISPLAY_HOST_TRENDS)
            {
               // TODO Make all calls for external files leverage a go-between, so j2ee stuff can execute differently.
               image_template = String.format("%s/trendshost.png", cgiutils.physical_images_path);
               image_template = trends.class.getClassLoader().getResource("images/trendshost.png").getFile();
            }
            else
            {
               image_template = String.format("%s/trendssvc.png", cgiutils.physical_images_path);
               image_template = trends.class.getClassLoader().getResource("images/trendssvc.png").getFile();
            }

            trends_image = null;
            try
            {
               trends_image = ImageIO.read(new File(image_template));
            }
            catch (IOException ioE)
            {
               // TODO log this
               trends_image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_3BYTE_BGR);
            }

            if (trends_image == null)
            {
               //#ifdef DEBUG
               //				System.out.printf("Error: Could not allocate memory for image\n");
               //#endif
               cgiutils.exit(common_h.ERROR);
            }
         }

         /* allocate colors used for drawing */

         color_white = Color.WHITE;
         color_black = Color.BLACK;
         color_red = Color.RED;
         color_darkred = new Color(128, 0, 0);
         color_green = Color.GREEN;
         color_darkgreen = new Color(0, 128, 0);
         color_yellow = Color.YELLOW;
         color_orange = Color.ORANGE;

         gd = trends_image.createGraphics();

         /* Set starting color */
         gd.setColor(color_black);
         Font monospaced = Font.decode("Monospaced");
         monospaced = monospaced.deriveFont(8);
         gd.setFont(monospaced);

         /* set transparency index */
         // TODO
         //		gdImageColorTransparent(trends_image,color_white);
         /* make sure the graphic is interlaced */

         //		gdImageInterlace(trends_image,1);
         if (small_image == common_h.FALSE)
         {

            /* title */

            start_time = new Date(t1*1000).toLocaleString();
            end_time = new Date(t1*1000).toLocaleString();

            if (display_type == DISPLAY_HOST_TRENDS)
               temp_buffer = String.format("State History For Host '%s'", host_name);
            else
               temp_buffer = String.format("State History For Service '%s' On Host '%s'", svc_description, host_name);

            string_height = gd.getFontMetrics().getHeight();
            string_width = gd.getFontMetrics().stringWidth(temp_buffer);
            gd.drawString(temp_buffer, (drawing_width / 2) - (string_width / 2) + drawing_x_offset, string_height);

            temp_buffer = String.format("%s to %s", start_time, end_time);
            string_width = gd.getFontMetrics().stringWidth(temp_buffer);
            gd.drawString(temp_buffer, (drawing_width / 2) - (string_width / 2) + drawing_x_offset,
                  (string_height * 2) + 5);

            /* first time stamp */
            temp_buffer = start_time;
            string_width = gd.getFontMetrics().stringWidth(temp_buffer);
            drawRotatedText(temp_buffer, drawing_x_offset + (string_height / 2), drawing_y_offset + drawing_height
                  + string_width + 5);
         }
      }

      if (display_type != DISPLAY_NO_TRENDS && input_type == GET_INPUT_NONE)
      {

         if (mode == CREATE_IMAGE || (mode == CREATE_HTML && use_map == common_h.TRUE))
         {

            /* read in all necessary archived state data */
            read_archived_state_data();

            /* graph archived state trend data */
            graph_all_trend_data();
         }

         /* print URL to image */
         if (mode == CREATE_HTML)
         {

            System.out.printf("<BR><BR>\n");
            System.out.printf("<DIV ALIGN=CENTER>\n");
            System.out.printf("<IMG SRC='%s?createimage&t1=%d&t2=%d", cgiutils_h.TRENDS_CGI, t1, t2);
            System.out.printf("&assumeinitialstates=%s", (assume_initial_states == common_h.TRUE) ? "yes" : "no");
            System.out.printf("&assumestatesduringnotrunning=%s", (assume_states_during_notrunning == common_h.TRUE)
                  ? "yes"
                  : "no");
            System.out.printf("&initialassumedhoststate=%d", initial_assumed_host_state);
            System.out.printf("&initialassumedservicestate=%d", initial_assumed_service_state);
            System.out.printf("&assumestateretention=%s", (assume_state_retention == common_h.TRUE) ? "yes" : "no");
            System.out.printf("&includesoftstates=%s", (include_soft_states == common_h.TRUE) ? "yes" : "no");
            System.out.printf("&host=%s", cgiutils.url_encode(host_name));
            if (display_type == DISPLAY_SERVICE_TRENDS)
               System.out.printf("&service=%s", cgiutils.url_encode(svc_description));
            if (backtrack_archives > 0)
               System.out.printf("&backtrack=%d", backtrack_archives);
            System.out.printf("&zoom=%d", zoom_factor);
            System.out.printf("' BORDER=0 name='trendsimage' useMap='#trendsmap' width=900>\n");
            System.out.printf("</DIV>\n");
         }

         if (mode == CREATE_IMAGE || (mode == CREATE_HTML && use_map == common_h.TRUE))
         {

            /* draw timestamps */
            draw_timestamps();

            /* draw horizontal lines */
            draw_horizontal_grid_lines();

            /* draw state time breakdowns */
            draw_time_breakdowns();
         }

         if (mode == CREATE_IMAGE)
         {

            /* write the image to file */
            try
            {
               ImageIO.write(trends_image, "png", System.out);

            }
            catch (IOException ioE)
            {
               ioE.printStackTrace();
            }

            //#ifdef DEBUG
            //            /* write the image to file */
            //            try {
            //              ImageIO.write(trends_image, "png", new File( "c:\\my.png" ) );
            //                
            //            } catch (IOException ioE ) {
            //                ioE.printStackTrace();
            //            }
            //#endif

            /* free memory allocated to image */
            //            gd.dispose();
            trends_image.flush();
            //			gdImageDestroy(trends_image);
         }
      }

      /* show user a selection of hosts and services to choose from... */
      if (display_type == DISPLAY_NO_TRENDS || input_type != GET_INPUT_NONE)
      {

         /* ask the user for what host they want a report for */
         if (input_type == GET_INPUT_HOST_TARGET)
         {

            System.out.printf("<P><DIV ALIGN=CENTER>\n");
            System.out.printf("<DIV CLASS='reportSelectTitle'>Step 2: Select Host</DIV>\n");
            System.out.printf("</DIV></P>\n");

            System.out.printf("<P><DIV ALIGN=CENTER>\n");

            System.out.printf("<TABLE BORDER=0 cellspacing=0 cellpadding=10>\n");
            System.out.printf("<form method=\"GET\" action=\"%s\">\n", cgiutils_h.TRENDS_CGI);
            System.out.printf("<input type='hidden' name='input' value='getoptions'>\n");

            System.out.printf("<tr><td class='reportSelectSubTitle' valign=center>Host:</td>\n");
            System.out.printf("<td class='reportSelectItem' valign=center>\n");
            System.out.printf("<select name='host'>\n");

            for (Iterator iter = objects.host_list.iterator(); iter.hasNext();)
            {
               temp_host = (objects_h.host) iter.next();
               if (cgiauth.is_authorized_for_host(temp_host, current_authdata) == common_h.TRUE)
                  System.out.printf("<option value='%s'>%s\n", temp_host.name, temp_host.name);
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
         else if (input_type == GET_INPUT_SERVICE_TARGET)
         {

            System.out.printf("<SCRIPT LANGUAGE='JavaScript'>\n");
            System.out.printf("function gethostname(hostindex){\n");
            System.out.printf("hostnames=[");

            for (Iterator iter = objects.service_list.iterator(); iter.hasNext();)
            {
               temp_service = (objects_h.service) iter.next();
               if (cgiauth.is_authorized_for_service(temp_service, current_authdata) == common_h.TRUE)
               {
                  if (found == common_h.TRUE)
                     System.out.printf(",");
                  else
                     first_service = temp_service.host_name;
                  System.out.printf(" \"%s\"", temp_service.host_name);
                  found = common_h.TRUE;
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
            System.out.printf("<form method=\"GET\" action=\"%s\" name=\"serviceform\">\n", cgiutils_h.TRENDS_CGI);
            System.out.printf("<input type='hidden' name='input' value='getoptions'>\n");
            System.out.printf("<input type='hidden' name='host' value='%s'>\n", (first_service == null)
                  ? "unknown"
                  : first_service);

            System.out.printf("<tr><td class='reportSelectSubTitle'>Service:</td>\n");
            System.out.printf("<td class='reportSelectItem'>\n");
            System.out
                  .printf("<select name='service' onFocus='document.serviceform.host.value=gethostname(this.selectedIndex);' onChange='document.serviceform.host.value=gethostname(this.selectedIndex);'>\n");

            for (Iterator iter = objects.service_list.iterator(); iter.hasNext();)
            {
               temp_service = (objects_h.service) iter.next();
               if (cgiauth.is_authorized_for_service(temp_service, current_authdata) == common_h.TRUE)
                  System.out.printf("<option value='%s'>%s;%s\n", temp_service.description, temp_service.host_name,
                        temp_service.description);
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
         else if (input_type == GET_INPUT_OPTIONS)
         {

            current_time = utils.currentTimeInSeconds();

            t.setTimeInMillis(current_time * 1000);

            start_day = 1;
            start_year = t.get(Calendar.YEAR);
            end_day = t.get(Calendar.DAY_OF_MONTH);
            end_year = t.get(Calendar.YEAR);
            ;

            System.out.printf("<P><DIV ALIGN=CENTER>\n");
            System.out.printf("<DIV CLASS='reportSelectTitle'>Step 3: Select Report Options</DIV>\n");
            System.out.printf("</DIV></P>\n");

            System.out.printf("<P><DIV ALIGN=CENTER>\n");

            System.out.printf("<TABLE BORDER=0 CELLPADDING=5>\n");
            System.out.printf("<form method=\"GET\" action=\"%s\">\n", cgiutils_h.TRENDS_CGI);
            System.out.printf("<input type='hidden' name='host' value='%s'>\n", host_name);
            if (display_type == DISPLAY_SERVICE_TRENDS)
               System.out.printf("<input type='hidden' name='service' value='%s'>\n", svc_description);

            System.out.printf("<tr><td class='reportSelectSubTitle' align=right>Report period:</td>\n");
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

            System.out.printf("<tr><td valign=top class='reportSelectSubTitle'>If Custom Report Period...</td></tr>\n");

            System.out.printf("<tr>");
            System.out.printf("<td valign=top class='reportSelectSubTitle'>Start Date (Inclusive):</td>\n");
            System.out.printf("<td align=left valign=top class='reportSelectItem'>");
            System.out.printf("<select name='smon'>\n");
            System.out.printf("<option value='1' %s>January\n", (t.get(Calendar.MONTH) == 0) ? "SELECTED" : "");
            System.out.printf("<option value='2' %s>February\n", (t.get(Calendar.MONTH) == 1) ? "SELECTED" : "");
            System.out.printf("<option value='3' %s>March\n", (t.get(Calendar.MONTH) == 2) ? "SELECTED" : "");
            System.out.printf("<option value='4' %s>April\n", (t.get(Calendar.MONTH) == 3) ? "SELECTED" : "");
            System.out.printf("<option value='5' %s>May\n", (t.get(Calendar.MONTH) == 4) ? "SELECTED" : "");
            System.out.printf("<option value='6' %s>June\n", (t.get(Calendar.MONTH) == 5) ? "SELECTED" : "");
            System.out.printf("<option value='7' %s>July\n", (t.get(Calendar.MONTH) == 6) ? "SELECTED" : "");
            System.out.printf("<option value='8' %s>August\n", (t.get(Calendar.MONTH) == 7) ? "SELECTED" : "");
            System.out.printf("<option value='9' %s>September\n", (t.get(Calendar.MONTH) == 8) ? "SELECTED" : "");
            System.out.printf("<option value='10' %s>October\n", (t.get(Calendar.MONTH) == 9) ? "SELECTED" : "");
            System.out.printf("<option value='11' %s>November\n", (t.get(Calendar.MONTH) == 10) ? "SELECTED" : "");
            System.out.printf("<option value='12' %s>December\n", (t.get(Calendar.MONTH) == 11) ? "SELECTED" : "");
            System.out.printf("</select>\n ");
            System.out.printf("<input type='text' size='2' maxlength='2' name='sday' value='%d'> ", start_day);
            System.out.printf("<input type='text' size='4' maxlength='4' name='syear' value='%d'>", start_year);
            System.out.printf("<input type='hidden' name='shour' value='0'>\n");
            System.out.printf("<input type='hidden' name='smin' value='0'>\n");
            System.out.printf("<input type='hidden' name='ssec' value='0'>\n");
            System.out.printf("</td>\n");
            System.out.printf("</tr>\n");

            System.out.printf("<tr>");
            System.out.printf("<td valign=top class='reportSelectSubTitle'>End Date (Inclusive):</td>\n");
            System.out.printf("<td align=left valign=top class='reportSelectItem'>");
            System.out.printf("<select name='emon'>\n");
            System.out.printf("<option value='1' %s>January\n", (t.get(Calendar.MONTH) == 0) ? "SELECTED" : "");
            System.out.printf("<option value='2' %s>February\n", (t.get(Calendar.MONTH) == 1) ? "SELECTED" : "");
            System.out.printf("<option value='3' %s>March\n", (t.get(Calendar.MONTH) == 2) ? "SELECTED" : "");
            System.out.printf("<option value='4' %s>April\n", (t.get(Calendar.MONTH) == 3) ? "SELECTED" : "");
            System.out.printf("<option value='5' %s>May\n", (t.get(Calendar.MONTH) == 4) ? "SELECTED" : "");
            System.out.printf("<option value='6' %s>June\n", (t.get(Calendar.MONTH) == 5) ? "SELECTED" : "");
            System.out.printf("<option value='7' %s>July\n", (t.get(Calendar.MONTH) == 6) ? "SELECTED" : "");
            System.out.printf("<option value='8' %s>August\n", (t.get(Calendar.MONTH) == 7) ? "SELECTED" : "");
            System.out.printf("<option value='9' %s>September\n", (t.get(Calendar.MONTH) == 8) ? "SELECTED" : "");
            System.out.printf("<option value='10' %s>October\n", (t.get(Calendar.MONTH) == 9) ? "SELECTED" : "");
            System.out.printf("<option value='11' %s>November\n", (t.get(Calendar.MONTH) == 10) ? "SELECTED" : "");
            System.out.printf("<option value='12' %s>December\n", (t.get(Calendar.MONTH) == 11) ? "SELECTED" : "");
            System.out.printf("</select>\n ");
            System.out.printf("<input type='text' size='2' maxlength='2' name='eday' value='%d'> ", end_day);
            System.out.printf("<input type='text' size='4' maxlength='4' name='eyear' value='%d'>", end_year);
            System.out.printf("<input type='hidden' name='ehour' value='24'>\n");
            System.out.printf("<input type='hidden' name='emin' value='0'>\n");
            System.out.printf("<input type='hidden' name='esec' value='0'>\n");
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

            System.out
                  .printf("<tr><td class='reportSelectSubTitle' align=right>Assume States During Program Downtime:</td>\n");
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

            System.out.printf("<tr><td class='reportSelectSubTitle' align=right>First Assumed %s State:</td>\n",
                  (display_type == DISPLAY_HOST_TRENDS) ? "Host" : "Service");
            System.out.printf("<td class='reportSelectItem'>\n");
            if (display_type == DISPLAY_HOST_TRENDS)
            {
               System.out.printf("<select name='initialassumedhoststate'>\n");
               System.out.printf("<option value=%d>Unspecified\n", AS_NO_DATA);
               System.out.printf("<option value=%d>Current State\n", AS_CURRENT_STATE);
               System.out.printf("<option value=%d>Host Up\n", AS_HOST_UP);
               System.out.printf("<option value=%d>Host Down\n", AS_HOST_DOWN);
               System.out.printf("<option value=%d>Host Unreachable\n", AS_HOST_UNREACHABLE);
            }
            else
            {
               System.out.printf("<select name='initialassumedservicestate'>\n");
               System.out.printf("<option value=%d>Unspecified\n", AS_NO_DATA);
               System.out.printf("<option value=%d>Current State\n", AS_CURRENT_STATE);
               System.out.printf("<option value=%d>Service Ok\n", AS_SVC_OK);
               System.out.printf("<option value=%d>Service Warning\n", AS_SVC_WARNING);
               System.out.printf("<option value=%d>Service Unknown\n", AS_SVC_UNKNOWN);
               System.out.printf("<option value=%d>Service Critical\n", AS_SVC_CRITICAL);
            }
            System.out.printf("</select>\n");
            System.out.printf("</td></tr>\n");

            System.out
                  .printf("<tr><td class='reportSelectSubTitle' align=right>Backtracked Archives (To Scan For Initial States):</td>\n");
            System.out.printf("<td class='reportSelectItem'>\n");
            System.out.printf("<input type='text' name='backtrack' size='2' maxlength='2' value='%d'>\n",
                  backtrack_archives);
            System.out.printf("</td></tr>\n");

            System.out
                  .printf("<tr><td class='reportSelectSubTitle' align=right>Suppress image map:</td><td class='reportSelectItem'><input type='checkbox' name='nomap'></td></tr>");
            System.out
                  .printf("<tr><td class='reportSelectSubTitle' align=right>Suppress popups:</td><td class='reportSelectItem'><input type='checkbox' name='nopopups'></td></tr>\n");

            System.out
                  .printf("<tr><td></td><td class='reportSelectItem'><input type='submit' value='Create Report'></td></tr>\n");

            System.out.printf("</form>\n");
            System.out.printf("</TABLE>\n");

            System.out.printf("</DIV></P>\n");

            /*
             System.out.printf("<P><DIV ALIGN=CENTER CLASS='helpfulHint'>\n");
             System.out.printf("Note: Choosing the 'suppress image map' option will make the report run approximately twice as fast as it would otherwise, but it will prevent you from being able to zoom in on specific time periods.\n");
             System.out.printf("</DIV></P>\n");
             */
         }

         /* as the user whether they want a graph for a host or service */
         else
         {
            System.out.printf("<P><DIV ALIGN=CENTER>\n");
            System.out.printf("<DIV CLASS='reportSelectTitle'>Step 1: Select Report Type</DIV>\n");
            System.out.printf("</DIV></P>\n");

            System.out.printf("<P><DIV ALIGN=CENTER>\n");

            System.out.printf("<TABLE BORDER=0 cellpadding=5>\n");
            System.out.printf("<form method=\"GET\" action=\"%s\">\n", cgiutils_h.TRENDS_CGI);

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

      cgiutils.exit(common_h.OK);
   }

   public static void document_header(int use_stylesheet)
   {
      String date_time; // MAX_DATETIME_LENGTH

      if (mode == CREATE_HTML)
      {
         if (response != null)
         {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Last-Modified", System.currentTimeMillis());
            response.setDateHeader("Expires", System.currentTimeMillis());
            response.setContentType("text/html");
         }
         else
         {
            System.out.printf("Cache-Control: no-store\r\n");
            System.out.printf("Pragma: no-cache\r\n");

            date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
            System.out.printf("Last-Modified: %s\r\n", date_time);

            date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
            System.out.printf("Expires: %s\r\n", date_time);

            System.out.printf("Content-type: text/html\r\n\r\n");
         }

         if (embedded == common_h.TRUE)
            return;

         System.out.printf("<html>\n");
         System.out.printf("<head>\n");
         System.out.printf("<title>\n");
         System.out.printf("Blue Trends\n");
         System.out.printf("</title>\n");

         if (use_stylesheet == common_h.TRUE)
         {
            System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n", cgiutils.url_stylesheets_path,
                  cgiutils_h.COMMON_CSS);
            System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n", cgiutils.url_stylesheets_path,
                  cgiutils_h.TRENDS_CSS);
         }

         /* write JavaScript code for popup window */
         if (display_type != DISPLAY_NO_TRENDS)
            write_popup_code();

         System.out.printf("</head>\n");

         System.out.printf("<BODY CLASS='trends'>\n");

         /* include user SSI header */
         cgiutils.include_ssi_files(cgiutils_h.TRENDS_CGI, cgiutils_h.SSI_HEADER);

         System.out.printf("<div id=\"popup\" style=\"position:absolute; z-index:1; visibility: hidden\"></div>\n");
      }

      else
      {
         if (response != null)
         {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Last-Modified", System.currentTimeMillis());
            response.setDateHeader("Expires", System.currentTimeMillis());
            response.setContentType("image/png");
         }
         else
         {
            System.out.printf("Cache-Control: no-store\r\n");
            System.out.printf("Pragma: no-cache\r\n");

            date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
            System.out.printf("Last-Modified: %s\r\n", date_time);

            date_time = cgiutils.get_time_string(0, common_h.HTTP_DATE_TIME);
            System.out.printf("Expires: %s\r\n", date_time);

            System.out.printf("Content-Type: image/png\r\n\r\n");
         }

      }
   }

   public static void document_footer()
   {

      if (embedded == common_h.TRUE)
         return;

      if (mode == CREATE_HTML)
      {

         /* include user SSI footer */
         cgiutils.include_ssi_files(cgiutils_h.TRENDS_CGI, cgiutils_h.SSI_FOOTER);

         System.out.printf("</body>\n");
         System.out.printf("</html>\n");
      }

      return;
   }

   public static int process_cgivars()
   {
      String[] variables;
      int error = common_h.FALSE;
      int x;

      variables = getcgi.getcgivars(request_string);

      for (x = 0; x < variables.length; x++)
      {

         /* do some basic length checking on the variable identifier to prevent buffer overflows */
         if (variables[x].length() >= common_h.MAX_INPUT_BUFFER - 1)
         {
            x++;
            continue;
         }

         /* we found the host argument */
         else if (variables[x].equals("host"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            host_name = variables[x];

            display_type = DISPLAY_HOST_TRENDS;
         }

         /* we found the node width argument */
         else if (variables[x].equals("service"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            svc_description = variables[x];

            display_type = DISPLAY_SERVICE_TRENDS;
         }

         /* we found first time argument */
         else if (variables[x].equals("t1"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            t1 = strtoul(variables[x], null, 10);
            timeperiod_type = TIMEPERIOD_CUSTOM;
         }

         /* we found first time argument */
         else if (variables[x].equals("t2"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            t2 = strtoul(variables[x], null, 10);
            timeperiod_type = TIMEPERIOD_CUSTOM;
         }

         /* we found the image creation option */
         else if (variables[x].equals("createimage"))
         {
            mode = CREATE_IMAGE;
         }

         /* we found the assume initial states option */
         else if (variables[x].equals("assumeinitialstates"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (variables[x].equals("yes"))
               assume_initial_states = common_h.TRUE;
            else
               assume_initial_states = common_h.FALSE;
         }

         /* we found the initial assumed host state option */
         else if (variables[x].equals("initialassumedhoststate"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            initial_assumed_host_state = atoi(variables[x]);
         }

         /* we found the initial assumed service state option */
         else if (variables[x].equals("initialassumedservicestate"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            initial_assumed_service_state = atoi(variables[x]);
         }

         /* we found the assume state during program not running option */
         else if (variables[x].equals("assumestatesduringnotrunning"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (variables[x].equals("yes"))
               assume_states_during_notrunning = common_h.TRUE;
            else
               assume_states_during_notrunning = common_h.FALSE;
         }

         /* we found the assume state retention option */
         else if (variables[x].equals("assumestateretention"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (variables[x].equals("yes"))
               assume_state_retention = common_h.TRUE;
            else
               assume_state_retention = common_h.FALSE;
         }

         /* we found the include soft states option */
         else if (variables[x].equals("includesoftstates"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (variables[x].equals("yes"))
               include_soft_states = common_h.TRUE;
            else
               include_soft_states = common_h.FALSE;
         }

         /* we found the zoom factor argument */
         else if (variables[x].equals("zoom"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            zoom_factor = atoi(variables[x]);
            if (zoom_factor == 0)
               zoom_factor = 1;
         }

         /* we found the backtrack archives argument */
         else if (variables[x].equals("backtrack"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            backtrack_archives = atoi(variables[x]);
            if (backtrack_archives < 0)
               backtrack_archives = 0;
            if (backtrack_archives > MAX_ARCHIVE_BACKTRACKS)
               backtrack_archives = MAX_ARCHIVE_BACKTRACKS;
         }

         /* we found the standard timeperiod argument */
         else if (variables[x].equals("timeperiod"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (variables[x].equals("today"))
               timeperiod_type = TIMEPERIOD_TODAY;
            else if (variables[x].equals("yesterday"))
               timeperiod_type = TIMEPERIOD_YESTERDAY;
            else if (variables[x].equals("thisweek"))
               timeperiod_type = TIMEPERIOD_THISWEEK;
            else if (variables[x].equals("lastweek"))
               timeperiod_type = TIMEPERIOD_LASTWEEK;
            else if (variables[x].equals("thismonth"))
               timeperiod_type = TIMEPERIOD_THISMONTH;
            else if (variables[x].equals("lastmonth"))
               timeperiod_type = TIMEPERIOD_LASTMONTH;
            else if (variables[x].equals("thisquarter"))
               timeperiod_type = TIMEPERIOD_THISQUARTER;
            else if (variables[x].equals("lastquarter"))
               timeperiod_type = TIMEPERIOD_LASTQUARTER;
            else if (variables[x].equals("thisyear"))
               timeperiod_type = TIMEPERIOD_THISYEAR;
            else if (variables[x].equals("lastyear"))
               timeperiod_type = TIMEPERIOD_LASTYEAR;
            else if (variables[x].equals("last24hours"))
               timeperiod_type = TIMEPERIOD_LAST24HOURS;
            else if (variables[x].equals("last7days"))
               timeperiod_type = TIMEPERIOD_LAST7DAYS;
            else if (variables[x].equals("last31days"))
               timeperiod_type = TIMEPERIOD_LAST31DAYS;
            else if (variables[x].equals("custom"))
               timeperiod_type = TIMEPERIOD_CUSTOM;
            else
               continue;

            convert_timeperiod_to_times(timeperiod_type);
         }

         /* we found time argument */
         else if (variables[x].equals("smon"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            start_month = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("sday"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            start_day = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("syear"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            start_year = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("smin"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            start_minute = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("ssec"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            start_second = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("shour"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            start_hour = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("emon"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            end_month = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("eday"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            end_day = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("eyear"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            end_year = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("emin"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            end_minute = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("esec"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            end_second = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found time argument */
         else if (variables[x].equals("ehour"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (timeperiod_type != TIMEPERIOD_CUSTOM)
               continue;

            end_hour = atoi(variables[x]);
            timeperiod_type = TIMEPERIOD_CUSTOM;
            compute_time_from_parts = common_h.TRUE;
         }

         /* we found the embed option */
         else if (variables[x].equals("embedded"))
            embedded = common_h.TRUE;

         /* we found the noheader option */
         else if (variables[x].equals("noheader"))
            display_header = common_h.FALSE;

         /* we found the nopopups option */
         else if (variables[x].equals("nopopups"))
            display_popups = common_h.FALSE;

         /* we found the nomap option */
         else if (variables[x].equals("nomap"))
         {
            display_popups = common_h.FALSE;
            use_map = common_h.FALSE;
         }

         /* we found the input option */
         else if (variables[x].equals("input"))
         {
            x++;
            if (variables[x] == null)
            {
               error = common_h.TRUE;
               break;
            }

            if (variables[x].equals("gethost"))
               input_type = GET_INPUT_HOST_TARGET;
            else if (variables[x].equals("getservice"))
               input_type = GET_INPUT_SERVICE_TARGET;
            else if (variables[x].equals("getoptions"))
               input_type = GET_INPUT_OPTIONS;
            else
               input_type = GET_INPUT_TARGET_TYPE;
         }

         /* we found the small image option */
         else if (variables[x].equals("smallimage"))
            small_image = common_h.TRUE;

      }

      /* free memory allocated to the CGI variables */
      getcgi.free_cgivars(variables);

      return error;
   }

   /* top level routine for graphic all trend data */
   public static void graph_all_trend_data()
   {
      //	archived_state temp_as;
      archived_state last_as;
      long a;
      long b;
      long current_time;
      int current_state = AS_NO_DATA;
      int have_some_real_data = common_h.FALSE;
      statusdata_h.hoststatus hststatus = null;
      statusdata_h.servicestatus svcstatus = null;
      long wobble = 300;
      int first_real_state = AS_NO_DATA;
      long initial_assumed_time;
      int initial_assumed_state = AS_SVC_OK;
      int error = common_h.FALSE;

      current_time = utils.currentTimeInSeconds();

      /* if left hand of graph is after current time, we can't do anything at all.... */
      if (t1 > current_time)
         return;

      /* find current state for host or service */
      if (display_type == DISPLAY_HOST_TRENDS)
         hststatus = statusdata.find_hoststatus(host_name);
      else
         svcstatus = statusdata.find_servicestatus(host_name, svc_description);

      /************************************/
      /* INSERT CURRENT STATE (IF WE CAN) */
      /************************************/

      /* if current time DOES NOT fall within graph bounds, so we can't do anything as far as assuming current state */
      /* the "wobble" value is necessary because when the CGI is called to do the PNG generation, t2 will actually be less that current_time by a bit */

      /* if we don't have any data, assume current state (if possible) */
      if (as_list == null && current_time > t1 && current_time < (t2 + wobble))
      {

         /* we don't have any historical information, but the current time falls within the reporting period, so use */
         /* the current status of the host/service as the starting data */
         if (display_type == DISPLAY_HOST_TRENDS)
         {
            if (hststatus != null)
            {

               if (hststatus.status == statusdata_h.HOST_DOWN)
                  last_known_state = AS_HOST_DOWN;
               else if (hststatus.status == statusdata_h.HOST_UNREACHABLE)
                  last_known_state = AS_HOST_UNREACHABLE;
               else
                  last_known_state = AS_HOST_UP;

               /* add a dummy archived state item, so something can get graphed */
               add_archived_state(last_known_state, AS_HARD_STATE, t1, "Current Host State Assumed (Faked Log Entry)");

               /* use the current state as the last known real state */
               first_real_state = last_known_state;
            }
         }
         else
         {
            if (svcstatus != null)
            {

               if (svcstatus.status == statusdata_h.SERVICE_OK)
                  last_known_state = AS_SVC_OK;
               else if (svcstatus.status == statusdata_h.SERVICE_WARNING)
                  last_known_state = AS_SVC_WARNING;
               else if (svcstatus.status == statusdata_h.SERVICE_CRITICAL)
                  last_known_state = AS_SVC_CRITICAL;
               else if (svcstatus.status == statusdata_h.SERVICE_UNKNOWN)
                  last_known_state = AS_SVC_UNKNOWN;

               /* add a dummy archived state item, so something can get graphed */
               add_archived_state(last_known_state, AS_HARD_STATE, t1,
                     "Current Service State Assumed (Faked Log Entry)");

               /* use the current state as the last known real state */
               first_real_state = last_known_state;
            }
         }
      }

      /******************************************/
      /* INSERT FIRST ASSUMED STATE (IF WE CAN) */
      /******************************************/

      if ((display_type == DISPLAY_HOST_TRENDS && initial_assumed_host_state != AS_NO_DATA)
            || (display_type == DISPLAY_SERVICE_TRENDS && initial_assumed_service_state != AS_NO_DATA))
      {

         /* see if its okay to assume initial state for this subject */
         error = common_h.FALSE;
         if (display_type == DISPLAY_SERVICE_TRENDS)
         {
            if (initial_assumed_service_state != AS_SVC_OK && initial_assumed_service_state != AS_SVC_WARNING
                  && initial_assumed_service_state != AS_SVC_UNKNOWN
                  && initial_assumed_service_state != AS_SVC_CRITICAL
                  && initial_assumed_service_state != AS_CURRENT_STATE)
               error = common_h.TRUE;
            else
               initial_assumed_state = initial_assumed_service_state;
            if (initial_assumed_service_state == AS_CURRENT_STATE && svcstatus == null)
               error = common_h.TRUE;
         }
         else
         {
            if (initial_assumed_host_state != AS_HOST_UP && initial_assumed_host_state != AS_HOST_DOWN
                  && initial_assumed_host_state != AS_HOST_UNREACHABLE
                  && initial_assumed_host_state != AS_CURRENT_STATE)
               error = common_h.TRUE;
            else
               initial_assumed_state = initial_assumed_host_state;
            if (initial_assumed_host_state == AS_CURRENT_STATE && hststatus == null)
               error = common_h.TRUE;
         }

         /* get the current state if applicable */
         if (((display_type == DISPLAY_HOST_TRENDS && initial_assumed_host_state == AS_CURRENT_STATE) || (display_type == DISPLAY_SERVICE_TRENDS && initial_assumed_service_state == AS_CURRENT_STATE))
               && error == common_h.FALSE)
         {
            if (display_type == DISPLAY_HOST_TRENDS)
            {
               switch (hststatus.status)
               {
                  case statusdata_h.HOST_DOWN :
                     initial_assumed_state = AS_HOST_DOWN;
                     break;
                  case statusdata_h.HOST_UNREACHABLE :
                     initial_assumed_state = AS_HOST_UNREACHABLE;
                     break;
                  case statusdata_h.HOST_UP :
                     initial_assumed_state = AS_HOST_UP;
                     break;
                  default :
                     error = common_h.TRUE;
                     break;
               }
            }
            else
            {
               switch (svcstatus.status)
               {
                  case statusdata_h.SERVICE_OK :
                     initial_assumed_state = AS_SVC_OK;
                     break;
                  case statusdata_h.SERVICE_WARNING :
                     initial_assumed_state = AS_SVC_WARNING;
                     break;
                  case statusdata_h.SERVICE_UNKNOWN :
                     initial_assumed_state = AS_SVC_UNKNOWN;
                     break;
                  case statusdata_h.SERVICE_CRITICAL :
                     initial_assumed_state = AS_SVC_CRITICAL;
                     break;
                  default :
                     error = common_h.TRUE;
                     break;
               }
            }
         }

         if (error == common_h.FALSE)
         {

            /* add this assumed state entry before any entries in the list and <= t1 */
            if (as_list == null || as_list.isEmpty())
               initial_assumed_time = t1;
            else if (as_list.get(0).time_stamp > t1)
               initial_assumed_time = t1;
            else
               initial_assumed_time = as_list.get(0).time_stamp - 1;

            if (display_type == DISPLAY_HOST_TRENDS)
               add_archived_state(initial_assumed_state, AS_HARD_STATE, initial_assumed_time,
                     "First Host State Assumed (Faked Log Entry)");
            else
               add_archived_state(initial_assumed_state, AS_HARD_STATE, initial_assumed_time,
                     "First Service State Assumed (Faked Log Entry)");
         }
      }

      /**************************************/
      /* BAIL OUT IF WE DON'T HAVE ANYTHING */
      /**************************************/

      have_some_real_data = common_h.FALSE;
      for (archived_state temp_as : as_list)
      {
         if (temp_as.entry_type != AS_NO_DATA && temp_as.entry_type != AS_PROGRAM_START
               && temp_as.entry_type != AS_PROGRAM_END)
         {
            have_some_real_data = common_h.TRUE;
            break;
         }
      }
      if (have_some_real_data == common_h.FALSE)
         return;

      /* if we're creating the HTML, start map code... */
      if (mode == CREATE_HTML)
         System.out.printf("<MAP name='trendsmap'>\n");

      last_as = null;
      earliest_time = t2;
      latest_time = t1;

      //#ifdef DEBUG
      //	System.out.printf("--- BEGINNING/MIDDLE SECTION ---<BR>\n");
      //#endif

      /**********************************/
      /*    BEGINNING/MIDDLE SECTION    */
      /**********************************/

      int count_as = 0;
      for (archived_state temp_as : as_list)
      {

         count_as++;

         /* keep this as last known state if this is the first entry or if it occurs before the starting point of the graph */
         if ((temp_as.time_stamp <= t1 || (count_as == 1))
               && (temp_as.entry_type != AS_NO_DATA && temp_as.entry_type != AS_PROGRAM_END && temp_as.entry_type != AS_PROGRAM_START))
         {
            last_known_state = temp_as.entry_type;
            //#ifdef DEBUG
            //			System.out.printf("SETTING LAST KNOWN STATE=%d<br>\n",last_known_state);
            //#endif
         }

         /* skip this entry if it occurs before the starting point of the graph */
         if (temp_as.time_stamp <= t1)
         {
            //#ifdef DEBUG
            //			System.out.printf("SKIPPING PRE-EVENT: %d @ %d<br>\n",temp_as.entry_type,temp_as.time_stamp);
            //#endif
            last_as = temp_as;
            continue;
         }

         /* graph this span if we're not on the first item */
         if (last_as != null)
         {

            a = last_as.time_stamp;
            b = temp_as.time_stamp;

            /* we've already passed the last time displayed in the graph */
            if (a > t2)
               break;

            /* only graph this data if its on the graph */
            else if (b > t1)
            {

               /* clip last time if it exceeds graph limits */
               if (b > t2)
                  b = t2;

               /* clip first time if it precedes graph limits */
               if (a < t1)
                  a = t1;

               /* save this time if its the earliest we've graphed */
               if (a < earliest_time)
               {
                  earliest_time = a;
                  earliest_state = last_as.entry_type;
               }

               /* save this time if its the latest we've graphed */
               if (b > latest_time)
               {
                  latest_time = b;
                  latest_state = last_as.entry_type;
               }

               /* compute availability times for this chunk */
               graph_trend_data(last_as.entry_type, temp_as.entry_type, last_as.time_stamp, a, b, last_as.state_info);

               /* return if we've reached the end of the graph limits */
               if (b >= t2)
               {
                  last_as = temp_as;
                  break;
               }
            }
         }

         /* keep track of the last item */
         last_as = temp_as;
      }

      //#ifdef DEBUG
      //	System.out.printf("--- END SECTION ---<BR>\n");
      //#endif

      /**********************************/
      /*           END SECTION          */
      /**********************************/

      if (last_as != null)
      {

         /* don't process an entry that is beyond the limits of the graph */
         if (last_as.time_stamp < t2)
         {

            current_time = utils.currentTimeInSeconds();
            b = current_time;
            if (b > t2)
               b = t2;

            a = last_as.time_stamp;
            if (a < t1)
               a = t1;

            /* fake the current state (it doesn't really matter for graphing) */
            if (display_type == DISPLAY_HOST_TRENDS)
               current_state = AS_HOST_UP;
            else
               current_state = AS_SVC_OK;

            /* compute availability times for last state */
            graph_trend_data(last_as.entry_type, current_state, a, a, b, last_as.state_info);
         }
      }

      /* if we're creating the HTML, close the map code */
      if (mode == CREATE_HTML)
         System.out.printf("</MAP>\n");

      return;
   }

   /* graphs trend data */
   public static void graph_trend_data(int first_state, int last_state, long real_start_time, long start_time,
         long end_time, String state_info)
   {
      int start_state;
      int end_state;
      int start_pixel = 0;
      int end_pixel = 0;
      Color color_to_use;
      int height = 0;
      double start_pixel_ratio;
      double end_pixel_ratio;
      String temp_buffer; // MAX_INPUT_BUFFER
      String state_string; // MAX_INPUT_BUFFER
      String end_timestring; // MAX_INPUT_BUFFER
      String start_timestring; // MAX_INPUT_BUFFER
      long center_time;
      long next_start_time;
      long next_end_time;

      /* can't graph if we don't have data... */
      if (first_state == AS_NO_DATA || last_state == AS_NO_DATA)
         return;
      if (first_state == AS_PROGRAM_START && (last_state == AS_PROGRAM_END || last_state == AS_PROGRAM_START))
      {
         if (assume_initial_states == common_h.FALSE)
            return;
      }
      if (first_state == AS_PROGRAM_END)
      {
         if (assume_states_during_notrunning == common_h.TRUE)
            first_state = last_known_state;
         else
            return;
      }

      /* special case if first entry was program start */
      if (first_state == AS_PROGRAM_START)
      {
         //#ifdef DEBUG
         //		System.out.printf("First state=program start!\n");
         //#endif
         if (assume_initial_states == common_h.TRUE)
         {
            //#ifdef DEBUG
            //			System.out.printf("\tWe are assuming initial states...\n");
            //#endif
            if (assume_state_retention == common_h.TRUE)
            {
               start_state = last_known_state;
               //#ifdef DEBUG
               //				System.out.printf("\tWe are assuming state retention (%d)...\n",start_state);
               //#endif
            }
            else
            {
               //#ifdef DEBUG
               //				System.out.printf("\tWe are NOT assuming state retention...\n");
               //#endif
               if (display_type == DISPLAY_HOST_TRENDS)
                  start_state = AS_HOST_UP;
               else
                  start_state = AS_SVC_OK;
            }
         }
         else
         {
            //#ifdef DEBUG
            //			System.out.printf("We ARE NOT assuming initial states!\n");
            //#endif
            return;
         }
      }
      else
      {
         start_state = first_state;
         last_known_state = first_state;
      }

      /* special case if last entry was program stop */
      if (last_state == AS_PROGRAM_END)
         end_state = first_state;
      else
         end_state = last_state;

      //#ifdef DEBUG
      //	System.out.printf("Graphing state %d\n",start_state);
      //	System.out.printf("\tfrom %s",ctime(&start_time));
      //	System.out.printf("\tto %s",ctime(&end_time));
      //#endif

      if (start_time < t1)
         start_time = t1;
      if (end_time > t2)
         end_time = t2;
      if (end_time < t1 || start_time > t2)
         return;

      /* calculate the first and last pixels to use */
      if (start_time == t1)
         start_pixel = 0;
      else
      {
         start_pixel_ratio = ((double) (start_time - t1)) / ((double) (t2 - t1));
         start_pixel = (int) (start_pixel_ratio * (drawing_width - 1));
      }
      if (end_time == t1)
         end_pixel = 0;
      else
      {
         end_pixel_ratio = ((double) (end_time - t1)) / ((double) (t2 - t1));
         end_pixel = (int) (end_pixel_ratio * (drawing_width - 1));
      }

      //#ifdef DEBUG
      //	System.out.printf("\tPixel %d to %d\n\n",start_pixel,end_pixel);
      //#endif

      /* we're creating the image, so draw... */
      if (mode == CREATE_IMAGE)
      {

         /* figure out the color to use for drawing */
         switch (start_state)
         {
            case AS_HOST_UP :
               color_to_use = color_green;
               height = 60;
               break;
            case AS_HOST_DOWN :
               color_to_use = color_red;
               height = 40;
               break;
            case AS_HOST_UNREACHABLE :
               color_to_use = color_darkred;
               height = 20;
               break;
            case AS_SVC_OK :
               color_to_use = color_green;
               height = 80;
               break;
            case AS_SVC_WARNING :
               color_to_use = color_yellow;
               height = 60;
               break;
            case AS_SVC_UNKNOWN :
               color_to_use = color_orange;
               height = 40;
               break;
            case AS_SVC_CRITICAL :
               color_to_use = color_red;
               height = 20;
               break;
            default :
               color_to_use = color_black;
               height = 0;
               break;
         }

         /* draw a rectangle */
         if (start_state != AS_NO_DATA)
         {
            Color temp_color = gd.getColor();
            gd.setColor(color_to_use);
            gd.fillRect(start_pixel + drawing_x_offset, drawing_height - height + drawing_y_offset, end_pixel
                  + drawing_x_offset, drawing_height + drawing_y_offset);
            gd.setColor(temp_color);
         }
      }

      /* else we're creating the HTML, so write map area code... */
      else
      {

         /* figure out the the state string to use */
         switch (start_state)
         {
            case AS_HOST_UP :
               state_string = "UP";
               height = 60;
               break;
            case AS_HOST_DOWN :
               state_string = "DOWN";
               height = 40;
               break;
            case AS_HOST_UNREACHABLE :
               state_string = "UNREACHABLE";
               height = 20;
               break;
            case AS_SVC_OK :
               state_string = "OK";
               height = 80;
               break;
            case AS_SVC_WARNING :
               state_string = "WARNING";
               height = 60;
               break;
            case AS_SVC_UNKNOWN :
               state_string = "UNKNOWN";
               height = 40;
               break;
            case AS_SVC_CRITICAL :
               state_string = "CRITICAL";
               height = 20;
               break;
            default :
               state_string = "?";
               height = 5;
               break;
         }

         /* get the center of this time range */
         center_time = start_time + ((end_time - start_time) / 2);

         /* determine next start and end time range with zoom factor */
         if (zoom_factor > 0)
         {
            next_start_time = center_time - (((t2 - t1) / 2) / zoom_factor);
            next_end_time = center_time + (((t2 - t1) / 2) / zoom_factor);
         }
         else
         {
            next_start_time = center_time + (((t2 - t1) / 2) * zoom_factor);
            next_end_time = center_time - (((t2 - t1) / 2) * zoom_factor);
         }

         System.out.printf("<AREA shape='rect' ");

         System.out.printf("coords='%d,%d,%d,%d' ", drawing_x_offset + start_pixel, drawing_y_offset
               + (drawing_height - height), drawing_x_offset + end_pixel, drawing_y_offset + drawing_height);

         System.out.printf("href='%s?t1=%d&t2=%d&host=%s", cgiutils_h.TRENDS_CGI, next_start_time, next_end_time,
               cgiutils.url_encode(host_name));
         if (display_type == DISPLAY_SERVICE_TRENDS)
            System.out.printf("&service=%s", cgiutils.url_encode(svc_description));
         System.out.printf("&assumeinitialstates=%s", (assume_initial_states == common_h.TRUE) ? "yes" : "no");
         System.out.printf("&initialassumedhoststate=%d", initial_assumed_host_state);
         System.out.printf("&initialassumedservicestate=%d", initial_assumed_service_state);
         System.out.printf("&assumestateretention=%s", (assume_state_retention == common_h.TRUE) ? "yes" : "no");
         System.out.printf("&assumestatesduringnotrunning=%s", (assume_states_during_notrunning == common_h.TRUE)
               ? "yes"
               : "no");
         System.out.printf("&includesoftstates=%s", (include_soft_states == common_h.TRUE) ? "yes" : "no");
         if (backtrack_archives > 0)
            System.out.printf("&backtrack=%d", backtrack_archives);
         System.out.printf("&zoom=%d", zoom_factor);

         System.out.printf("' ");

         /* display popup text */
         if (display_popups == common_h.TRUE)
         {

            start_timestring = new Date(real_start_time*1000).toLocaleString();
            end_timestring = new Date(end_time*1000).toLocaleString();

            /* calculate total time in this state */
            cgiutils.time_breakdown tb = cgiutils.get_time_breakdown((end_time - start_time));

            /* sanitize plugin output */
            state_info = cgiutils.sanitize_plugin_output(state_info);

            System.out.printf("onMouseOver='showPopup(\"");
            temp_buffer = String
                  .format(
                        "<B><U>%s</U></B><BR><B>Time Range</B>: <I>%s</I> to <I>%s</I><BR><B>Duration</B>: <I>%dd %dh %dm %ds</I><BR><B>State Info</B>: <I>%s</I>",
                        state_string, start_timestring, end_timestring, tb.days, tb.hours, tb.minutes, tb.seconds,
                        (state_info == null) ? "N/A" : state_info);
            System.out.printf("%s", temp_buffer);
            System.out.printf("\",event)' onMouseOut='hidePopup()'");
         }

         System.out.printf(">\n");

      }

      /* calculate time in this state */
      switch (start_state)
      {
         case AS_HOST_UP :
            time_up += (end_time - start_time);
            break;
         case AS_HOST_DOWN :
            time_down += (end_time - start_time);
            break;
         case AS_HOST_UNREACHABLE :
            time_unreachable += (end_time - start_time);
            break;
         case AS_SVC_OK :
            time_ok += (end_time - start_time);
            break;
         case AS_SVC_WARNING :
            time_warning += (end_time - start_time);
            break;
         case AS_SVC_UNKNOWN :
            time_unknown += (end_time - start_time);
            break;
         case AS_SVC_CRITICAL :
            time_critical += (end_time - start_time);
            break;
         default :
            break;
      }

      return;
   }

   /* convert current host state to archived state value */
   public static int convert_host_state_to_archived_state(int current_status)
   {

      if (current_status == statusdata_h.HOST_UP)
         return AS_HOST_UP;
      if (current_status == statusdata_h.HOST_DOWN)
         return AS_HOST_DOWN;
      if (current_status == statusdata_h.HOST_UNREACHABLE)
         return AS_HOST_UNREACHABLE;

      return AS_NO_DATA;
   }

   /* convert current service state to archived state value */
   public static int convert_service_state_to_archived_state(int current_status)
   {

      if (current_status == statusdata_h.SERVICE_OK)
         return AS_SVC_OK;
      if (current_status == statusdata_h.SERVICE_UNKNOWN)
         return AS_SVC_UNKNOWN;
      if (current_status == statusdata_h.SERVICE_WARNING)
         return AS_SVC_WARNING;
      if (current_status == statusdata_h.SERVICE_CRITICAL)
         return AS_SVC_CRITICAL;

      return AS_NO_DATA;
   }

   /* adds an archived state entry */
   public static void add_archived_state(int entry_type, int state_type, long time_stamp, String state_info)
   {
      archived_state new_as = null;

      //#ifdef DEBUG
      //	System.out.printf("Added state %d @ %s",state_type,ctime(&time_stamp));
      //#endif

      /* allocate memory for the new entry */
      new_as = new archived_state();

      new_as.state_info = state_info;
      new_as.entry_type = entry_type;
      new_as.processed_state = entry_type;
      new_as.state_type = state_type;
      new_as.time_stamp = time_stamp;

      /* add the new entry to the list in memory, sorted by time */
      boolean added = false;
      for (ListIterator<archived_state> iter = as_list.listIterator(); iter.hasNext();)
      {
         archived_state temp_as = iter.next();

         if (new_as.time_stamp < temp_as.time_stamp)
         {
            iter.add(new_as);
            added = true;
            break;
         }
      }

      if (!added)
         as_list.add(new_as);

   }

   /* reads log files for archived state data */
   public static void read_archived_state_data()
   {
      String filename; // MAX_FILENAME_LENGTH
      int newest_archive = 0;
      int oldest_archive = 0;
      int current_archive;

      //#ifdef DEBUG
      //	System.out.printf("Determining archives to use...\n");
      //#endif

      /* determine earliest archive to use */
      oldest_archive = cgiutils.determine_archive_to_use_from_time(t1);
      if (cgiutils.log_rotation_method != common_h.LOG_ROTATION_NONE)
         oldest_archive += backtrack_archives;

      /* determine most recent archive to use */
      newest_archive = cgiutils.determine_archive_to_use_from_time(t2);

      if (oldest_archive < newest_archive)
         oldest_archive = newest_archive;

      //#ifdef DEBUG
      //	System.out.printf("Oldest archive: %d\n",oldest_archive);
      //	System.out.printf("Newest archive: %d\n",newest_archive);
      //#endif

      /* read in all the necessary archived logs */
      for (current_archive = newest_archive; current_archive <= oldest_archive; current_archive++)
      {

         /* get the name of the log file that contains this archive */
         filename = cgiutils.get_log_archive_to_use(current_archive);

         //#ifdef DEBUG	
         //		System.out.printf("\tCurrent archive: %d (%s)\n",current_archive,filename);
         //#endif

         /* scan the log file for archived state data */
         scan_log_file_for_archived_state_data(filename);
      }

   }

   /* grabs archives state data from a log file */
   public static void scan_log_file_for_archived_state_data(String filename)
   {
      String input = null;
      String input2 = null;
      String entry_host_name; // MAX_INPUT_BUFFER
      String entry_svc_description; // MAX_INPUT_BUFFER
      String plugin_output;
      String temp_buffer;
      long time_stamp;
      cgiutils_h.mmapfile thefile;
      int state_type = -1;

      /* print something so browser doesn't time out */
      if (mode == CREATE_HTML)
      {
         System.out.printf(" ");
         System.out.flush();
      }

      if ((thefile = cgiutils.mmap_fopen(filename)) == null)
      {
         //#ifdef DEBUG
         //		System.out.printf("Could not open file '%s' for reading.\n",filename);
         //#endif
         return;
      }

      //#ifdef DEBUG
      //	System.out.printf("Scanning log file '%s' for archived state data...\n",filename);
      //#endif

      while (true)
      {

         /* free memory */
         input = null;
         input2 = null;

         /* read the next line */
         if ((input = cgiutils.mmap_fgets(thefile)) == null)
            break;

         input = input.trim();

         String[] split = input.split("[\\]]", 2);
         time_stamp = (split[0].trim().length() <= 1) ? 0 : strtoul(split[0].substring(1), null, 10);

         /* program starts/restarts */
         if (input.contains(" starting..."))
            add_archived_state(AS_PROGRAM_START, AS_NO_DATA, time_stamp, "Program start");
         if (input.contains(" restarting..."))
            add_archived_state(AS_PROGRAM_START, AS_NO_DATA, time_stamp, "Program restart");

         /* program stops */
         if (input.contains(" shutting down..."))
            add_archived_state(AS_PROGRAM_END, AS_NO_DATA, time_stamp, "Normal program termination");
         if (input.contains("Bailing out"))
            add_archived_state(AS_PROGRAM_END, AS_NO_DATA, time_stamp, "Abnormal program termination");

         if (display_type == DISPLAY_HOST_TRENDS)
         {
            if (input.contains("HOST ALERT:") || input.contains("INITIAL HOST STATE:")
                  || input.contains("CURRENT HOST STATE:"))
            {

               /* get host name */
               temp_buffer = split(split, "[:]");
               temp_buffer = split(split, "[;]");
               entry_host_name = (temp_buffer == null) ? "" : temp_buffer.substring(1);

               if (!host_name.equals(entry_host_name))
                  continue;

               /* state types */
               if (input.contains(";SOFT;"))
               {
                  if (include_soft_states == common_h.FALSE)
                     continue;
                  state_type = AS_SOFT_STATE;
               }
               if (input.contains(";HARD;"))
                  state_type = AS_HARD_STATE;

               /* get the plugin output */
               temp_buffer = split(split, "[;]");
               temp_buffer = split(split, "[;]");
               temp_buffer = split(split, "[;]");
               plugin_output = split(split, "[\\n]");

               if (input.contains(";DOWN;"))
                  add_archived_state(AS_HOST_DOWN, state_type, time_stamp, plugin_output);
               else if (input.contains(";UNREACHABLE;"))
                  add_archived_state(AS_HOST_UNREACHABLE, state_type, time_stamp, plugin_output);
               else if (input.contains(";RECOVERY") || input.contains(";UP;"))
                  add_archived_state(AS_HOST_UP, state_type, time_stamp, plugin_output);
               else
                  add_archived_state(AS_NO_DATA, AS_NO_DATA, time_stamp, plugin_output);
            }
         }
         if (display_type == DISPLAY_SERVICE_TRENDS)
         {
            if (input.contains("SERVICE ALERT:") || input.contains("INITIAL SERVICE STATE:")
                  || input.contains("CURRENT SERVICE STATE:"))
            {

               /* get host name */
               temp_buffer = split(split, "[:]");
               temp_buffer = split(split, "[;]");
               entry_host_name = (temp_buffer == null) ? "" : temp_buffer.substring(1);

               if (!host_name.equals(entry_host_name))
                  continue;

               /* get service description */
               temp_buffer = split(split, "[;]");
               entry_svc_description = (temp_buffer == null) ? "" : temp_buffer;

               if (!svc_description.equals(entry_svc_description))
                  continue;

               /* state types */
               if (input.contains(";SOFT;"))
               {
                  if (include_soft_states == common_h.FALSE)
                     continue;
                  state_type = AS_SOFT_STATE;
               }
               if (input.contains(";HARD;"))
                  state_type = AS_HARD_STATE;

               /* get the plugin output */
               temp_buffer = split(split, "[;]");
               temp_buffer = split(split, "[;]");
               temp_buffer = split(split, "[;]");
               plugin_output = split(split, "[\\n]");

               if (input.contains(";CRITICAL;"))
                  add_archived_state(AS_SVC_CRITICAL, state_type, time_stamp, plugin_output);
               else if (input.contains(";WARNING;"))
                  add_archived_state(AS_SVC_WARNING, state_type, time_stamp, plugin_output);
               else if (input.contains(";UNKNOWN;"))
                  add_archived_state(AS_SVC_UNKNOWN, state_type, time_stamp, plugin_output);
               else if (input.contains(";RECOVERY;") || input.contains(";OK;"))
                  add_archived_state(AS_SVC_OK, state_type, time_stamp, plugin_output);
               else
                  add_archived_state(AS_NO_DATA, AS_NO_DATA, time_stamp, plugin_output);

            }
         }

      }

      /* free memory and close the file */
      cgiutils.mmap_fclose(thefile);

      return;
   }

   /* write JavaScript code and layer for popup window */
   public static void write_popup_code()
   {
      String border_color = "#000000";
      String background_color = "#ffffcc";
      int border = 1;
      int padding = 3;
      int x_offset = 3;
      int y_offset = 3;

      System.out.printf("<SCRIPT LANGUAGE='JavaScript'>\n");
      System.out.printf("<!--\n");
      System.out
            .printf("// JavaScript popup based on code originally found at http://www.helpmaster.com/htmlhelp/javascript/popjbpopup.htm\n");
      System.out.printf("function showPopup(text, eventObj){\n");
      System.out.printf("if(!document.all && document.getElementById)\n");
      System.out.printf("{ document.all=document.getElementsByTagName(\"*\")}\n");
      System.out.printf("ieLayer = 'document.all[\\'popup\\']';\n");
      System.out.printf("nnLayer = 'document.layers[\\'popup\\']';\n");
      System.out.printf("moLayer = 'document.getElementById(\\'popup\\')';\n");

      System.out.printf("if(!(document.all||document.layers||document.documentElement)) return;\n");

      System.out.printf("if(document.all) { document.popup=eval(ieLayer); }\n");
      System.out.printf("else {\n");
      System.out.printf("  if (document.documentElement) document.popup=eval(moLayer);\n");
      System.out.printf("  else document.popup=eval(nnLayer);\n");
      System.out.printf("}\n");

      System.out.printf("var table = \"\";\n");

      System.out.printf("if (document.all||document.documentElement){\n");
      System.out.printf("table += \"<table bgcolor='%s' border=%d cellpadding=%d cellspacing=0>\";\n",
            background_color, border, padding);
      System.out.printf("table += \"<tr><td>\";\n");
      System.out.printf("table += \"<table cellspacing=0 cellpadding=%d>\";\n", padding);
      System.out.printf("table += \"<tr><td bgcolor='%s' class='popupText'>\" + text + \"</td></tr>\";\n",
            background_color);
      System.out.printf("table += \"</table></td></tr></table>\"\n");
      System.out.printf("document.popup.innerHTML = table;\n");
      System.out.printf("document.popup.style.left = (document.all ? eventObj.x : eventObj.layerX) + %d;\n", x_offset);
      System.out.printf("document.popup.style.top  = (document.all ? eventObj.y : eventObj.layerY) + %d;\n", y_offset);
      System.out.printf("document.popup.style.visibility = \"visible\";\n");
      System.out.printf("} \n");

      System.out.printf("else{\n");
      System.out.printf("table += \"<table cellpadding=%d border=%d cellspacing=0 bordercolor='%s'>\";\n", padding,
            border, border_color);
      System.out.printf("table += \"<tr><td bgcolor='%s' class='popupText'>\" + text + \"</td></tr></table>\";\n",
            background_color);
      System.out.printf("document.popup.document.open();\n");
      System.out.printf("document.popup.document.write(table);\n");
      System.out.printf("document.popup.document.close();\n");

      /* set x coordinate */
      System.out.printf("document.popup.left = eventObj.layerX + %d;\n", x_offset);

      /* make sure we don't overlap the right side of the screen */
      System.out
            .printf(
                  "if(document.popup.left + document.popup.document.width + %d > window.innerWidth) document.popup.left = window.innerWidth - document.popup.document.width - %d - 16;\n",
                  x_offset, x_offset);

      /* set y coordinate */
      System.out.printf("document.popup.top  = eventObj.layerY + %d;\n", y_offset);

      /* make sure we don't overlap the bottom edge of the screen */
      System.out
            .printf(
                  "if(document.popup.top + document.popup.document.height + %d > window.innerHeight) document.popup.top = window.innerHeight - document.popup.document.height - %d - 16;\n",
                  y_offset, y_offset);

      /* make the popup visible */
      System.out.printf("document.popup.visibility = \"visible\";\n");
      System.out.printf("}\n");
      System.out.printf("}\n");

      System.out.printf("function hidePopup(){ \n");
      System.out.printf("if (!(document.all || document.layers || document.documentElement)) return;\n");
      System.out.printf("if (document.popup == null){ }\n");
      System.out
            .printf("else if (document.all||document.documentElement) document.popup.style.visibility = \"hidden\";\n");
      System.out.printf("else document.popup.visibility = \"hidden\";\n");
      System.out.printf("document.popup = null;\n");
      System.out.printf("}\n");
      System.out.printf("//-.\n");

      System.out.printf("</SCRIPT>\n");

      return;
   }

   /* write timestamps */
   public static void draw_timestamps()
   {
      int last_timestamp = 0;
      //	archived_state temp_as;
      double start_pixel_ratio;
      int start_pixel;

      if (mode != CREATE_IMAGE)
         return;

      /* draw first timestamp */
      draw_timestamp(0, t1);
      last_timestamp = 0;

      for (archived_state temp_as : as_list)
      {

         if (temp_as.time_stamp < t1 || temp_as.time_stamp > t2)
            continue;

         start_pixel_ratio = ((temp_as.time_stamp - t1)) / ((t2 - t1));
         start_pixel = (int) (start_pixel_ratio * (drawing_width - 1));

         /* draw start timestamp if possible */
         if ((start_pixel > last_timestamp + MIN_TIMESTAMP_SPACING)
               && (start_pixel < drawing_width - 1 - MIN_TIMESTAMP_SPACING))
         {
            draw_timestamp(start_pixel, temp_as.time_stamp);
            last_timestamp = start_pixel;
         }
      }

      /* draw last timestamp */
      draw_timestamp(drawing_width - 1, t2);

      return;
   }

   public static void drawRotatedText(String text, int x, int y)
   {

      //  Draw string rotated counter-clockwise 90 degrees.
      gd.rotate(-Math.PI / 2);

      //    gd.rotate(-Math.PI/2 );
      gd.drawString(text, -1 * y, x);

      gd.rotate(Math.PI / 2);
   }

   /* write timestamp below graph */
   public static void draw_timestamp(int ts_pixel, long ts_time)
   {
      String temp_buffer; // MAX_INPUT_BUFFER
      int string_height;
      int string_width;

      temp_buffer = new Date(ts_time*1000).toLocaleString();

      string_height = gd.getFontMetrics().getHeight();
      string_width = gd.getFontMetrics().stringWidth(temp_buffer);

      if (small_image == common_h.FALSE)
         drawRotatedText(temp_buffer, ts_pixel + drawing_x_offset + (string_height / 2), drawing_y_offset
               + drawing_height + string_width + 5);

      /* draw a dashed vertical line at this point */
      if (ts_pixel > 0 && ts_pixel < (drawing_width - 1))
         draw_dashed_line(ts_pixel + drawing_x_offset, drawing_y_offset, ts_pixel + drawing_x_offset, drawing_y_offset
               + drawing_height, color_black);

      return;
   }

   /* draw total state times */
   public static void draw_time_breakdowns()
   {
      String temp_buffer; // MAX_INPUT_BUFFER
      long total_time = 0L;
      long total_state_time;
      long time_indeterminate = 0L;
      int string_height;

      if (mode == CREATE_HTML)
         return;

      if (small_image == common_h.TRUE)
         return;

      total_time = (t2 - t1);

      if (display_type == DISPLAY_HOST_TRENDS)
         total_state_time = time_up + time_down + time_unreachable;
      else
         total_state_time = time_ok + time_warning + time_unknown + time_critical;

      if (total_state_time >= total_time)
         time_indeterminate = 0L;
      else
         time_indeterminate = total_time - total_state_time;

      string_height = gd.getFontMetrics().getHeight();

      Color temp_color = gd.getColor();
      FontMetrics metrics = gd.getFontMetrics();

      if (display_type == DISPLAY_HOST_TRENDS)
      {

         temp_buffer = get_time_breakdown_string(total_time, time_up, "Up");

         gd.setColor(color_darkgreen);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 5);
         gd.drawString("Up", drawing_x_offset - 10 - (metrics.stringWidth("Up")), drawing_y_offset + 5);

         temp_buffer = get_time_breakdown_string(total_time, time_down, "Down");
         gd.setColor(color_red);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 25);
         gd.drawString("Down", drawing_x_offset - 10 - (metrics.stringWidth("Down")), drawing_y_offset + 25);

         temp_buffer = get_time_breakdown_string(total_time, time_unreachable, "Unreachable");
         gd.setColor(color_darkred);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 45);
         gd.drawString("Unreachable", drawing_x_offset - 10 - (metrics.stringWidth("Unreachable")),
               drawing_y_offset + 45);

         temp_buffer = get_time_breakdown_string(total_time, time_indeterminate, "Indeterminate");
         gd.setColor(color_black);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 65);
         gd.drawString("Indeterminate", drawing_x_offset - 10 - (metrics.stringWidth("Indeterminate")),
               drawing_y_offset + 65);
      }
      else
      {
         temp_buffer = get_time_breakdown_string(total_time, time_ok, "Ok");
         gd.setColor(color_darkgreen);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 5);
         gd.drawString("Ok", drawing_x_offset - 10 - (metrics.stringWidth("Ok")), drawing_y_offset + 5);

         temp_buffer = get_time_breakdown_string(total_time, time_warning, "Warning");
         gd.setColor(color_yellow);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 25);
         gd.drawString("Warning", drawing_x_offset - 10 - (metrics.stringWidth("Warning")), drawing_y_offset + 25);

         temp_buffer = get_time_breakdown_string(total_time, time_unknown, "Unknown");
         gd.setColor(color_orange);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 45);
         gd.drawString("Unknown", drawing_x_offset - 10 - (metrics.stringWidth("Unknown")), drawing_y_offset + 45);

         temp_buffer = get_time_breakdown_string(total_time, time_critical, "Critical");
         gd.setColor(color_red);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 65);
         gd.drawString("Critical", drawing_x_offset - 10 - (metrics.stringWidth("Critical")), drawing_y_offset + 65);

         temp_buffer = get_time_breakdown_string(total_time, time_indeterminate, "Indeterminate");
         gd.setColor(color_black);
         gd.drawString(temp_buffer, drawing_x_offset + drawing_width + 20, drawing_y_offset + 85);
         gd.drawString("Indeterminate", drawing_x_offset - 10 - (metrics.stringWidth("Indeterminate")),
               drawing_y_offset + 85);
      }

      gd.setColor(temp_color);
      return;
   }

   public static String get_time_breakdown_string(long total_time, long state_time, String state_string)
   {
      double percent_time;

      cgiutils.time_breakdown tb = cgiutils.get_time_breakdown(state_time);
      if (total_time == 0L)
         percent_time = 0.0;
      else
         percent_time = ((double) state_time / total_time) * 100.0;

      return String.format("%-13s: (%.3f%%) %dd %dh %dm %ds", state_string, percent_time, tb.days, tb.hours,
            tb.minutes, tb.seconds);

   }

   public static void convert_timeperiod_to_times(int type)
   {
      long current_time;
      Calendar t = Calendar.getInstance();

      /* get the current time */
      current_time = utils.currentTimeInSeconds();

      t.setTimeInMillis(current_time * 1000);

      t.set(Calendar.SECOND, 0);
      t.set(Calendar.MINUTE, 0);
      t.set(Calendar.HOUR, 0);

      switch (type)
      {
         case TIMEPERIOD_LAST24HOURS :
            t1 = current_time - (60 * 60 * 24);
            t2 = current_time;
            break;
         case TIMEPERIOD_TODAY :
            t1 = utils.getTimeInSeconds(t);
            t2 = current_time;
            break;
         case TIMEPERIOD_YESTERDAY :
            t1 = (utils.getTimeInSeconds(t) - (60 * 60 * 24));
            t2 = utils.getTimeInSeconds(t);
            break;
         case TIMEPERIOD_THISWEEK :
            t1 = (utils.getTimeInSeconds(t) - (60 * 60 * 24 * (t.get(Calendar.DAY_OF_WEEK)-1)));
            t2 = current_time;
            break;
         case TIMEPERIOD_LASTWEEK :
            t1 = (utils.getTimeInSeconds(t) - (60 * 60 * 24 * (t.get(Calendar.DAY_OF_WEEK)-1)) - (60 * 60 * 24 * 7));
            t2 = (utils.getTimeInSeconds(t) - (60 * 60 * 24 * (t.get(Calendar.DAY_OF_WEEK)-1)));
            break;
         case TIMEPERIOD_THISMONTH :
            t.set(Calendar.DAY_OF_MONTH, 1);
            t1 = utils.getTimeInSeconds(t);
            t2 = current_time;
            break;
         case TIMEPERIOD_LASTMONTH :
            t.set(Calendar.DAY_OF_MONTH, 1);
            t2 = utils.getTimeInSeconds(t);
            if (t.get(Calendar.MONTH) == 0)
            {
               t.set(Calendar.MONTH, 11);
               t.roll(Calendar.YEAR, false);
            }
            else
               t.roll(Calendar.MONTH, false);
            t1 = utils.getTimeInSeconds(t);
            break;
         case TIMEPERIOD_THISQUARTER :
            break;
         case TIMEPERIOD_LASTQUARTER :
            break;
         case TIMEPERIOD_THISYEAR :
            t.set(Calendar.MONTH, 0);
            t.set(Calendar.DAY_OF_MONTH, 1);
            t1 = utils.getTimeInSeconds(t);
            t2 = current_time;
            break;
         case TIMEPERIOD_LASTYEAR :
            t.set(Calendar.MONTH, 0);
            t.set(Calendar.DAY_OF_MONTH, 1);
            t2 = utils.getTimeInSeconds(t);
            t.roll(Calendar.YEAR, false);
            t1 = utils.getTimeInSeconds(t);
            break;
         case TIMEPERIOD_LAST7DAYS :
            t2 = current_time;
            t1 = current_time - (7 * 24 * 60 * 60);
            break;
         case TIMEPERIOD_LAST31DAYS :
            t2 = current_time;
            t1 = current_time - (31 * 24 * 60 * 60);
            break;
         default :
            break;
      }

      return;
   }

   public static void compute_report_times()
   {
      Calendar st = Calendar.getInstance();
      Calendar et = Calendar.getInstance();

      /* get the current time */
      long current_time = utils.currentTimeInSeconds();

      st.setTimeInMillis(current_time * 1000);

      st.set(Calendar.SECOND, start_second);
      st.set(Calendar.MINUTE, start_minute);
      st.set(Calendar.HOUR, start_hour);
      st.set(Calendar.DAY_OF_MONTH, start_day);
      st.set(Calendar.MONTH, start_month - 1);
      st.set(Calendar.YEAR, start_year);

      t1 = utils.getTimeInSeconds(st);

      et.setTimeInMillis(current_time * 1000);

      et.set(Calendar.SECOND, end_second);
      et.set(Calendar.MINUTE, end_minute);
      et.set(Calendar.HOUR, end_hour);
      et.set(Calendar.DAY_OF_MONTH, end_day);
      et.set(Calendar.MONTH, end_month - 1);
      et.set(Calendar.YEAR, end_year);

      t2 = utils.getTimeInSeconds(st);
   }

   /* draws a dashed line */
   public static void draw_dashed_line(int x1, int y1, int x2, int y2, Color color)
   {

      float[] styleDashed = new float[]
      {2.0f, 2.0f};

      /* sets current style to a dashed line */
      BasicStroke stroke = (BasicStroke) gd.getStroke();
      BasicStroke dashed = new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke
            .getMiterLimit(), styleDashed, 0.0f);
      gd.setStroke(dashed);

      /* draws a line (dashed) */
      Color temp_color = gd.getColor();
      gd.setColor(color);

      gd.drawLine(x1, y1, x2, y2);

      gd.setColor(temp_color);
      gd.setStroke(stroke);

      return;
   }

   /* draws horizontal grid lines */
   public static void draw_horizontal_grid_lines()
   {

      if (mode == CREATE_HTML)
         return;

      if (small_image == common_h.TRUE)
         return;

      draw_dashed_line(drawing_x_offset, drawing_y_offset + 10, drawing_x_offset + drawing_width,
            drawing_y_offset + 10, color_black);
      draw_dashed_line(drawing_x_offset, drawing_y_offset + 30, drawing_x_offset + drawing_width,
            drawing_y_offset + 30, color_black);
      draw_dashed_line(drawing_x_offset, drawing_y_offset + 50, drawing_x_offset + drawing_width,
            drawing_y_offset + 50, color_black);
      if (display_type == DISPLAY_SERVICE_TRENDS)
         draw_dashed_line(drawing_x_offset, drawing_y_offset + 70, drawing_x_offset + drawing_width,
               drawing_y_offset + 70, color_black);

      return;
   }

   private static int atoi(String value)
   {
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException nfE)
      {
         //        logger.throwing( cn, "atoi", nfE);
         return 0;
      }
   }

   private static long strtoul(String value, Object ignore, int base)
   {
      try
      {
         return Long.parseLong(value);
      }
      catch (NumberFormatException nfE)
      {
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
   private static String split(String[] split, String regex)
   {

      if (split == null)
      {
         return null;
      }
      else if (split.length == 1 || split[1] == null)
      {
         split[0] = null;
         return null;
      }
      else
      {
         String[] split2 = split[1].split(regex, 2);
         split[0] = split2[0];
         if (split2.length == 1)
            split[1] = null;
         return split[1];
      }
   }
}