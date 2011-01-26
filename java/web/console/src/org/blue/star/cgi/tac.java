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

import org.blue.star.base.blue;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class tac extends blue_servlet
{

   public static final int HEALTH_WARNING_PERCENTAGE = 90;

   public static final int HEALTH_CRITICAL_PERCENTAGE = 75;

   /* HOSTOUTAGE structure */
   public static class hostoutage
   {
      public objects_h.host hst;

      public int affected_child_hosts;
   }

   // - Rob - instantiated this object as it was causing a null pointer. 
   public static cgiauth_h.authdata current_authdata = new cgiauth_h.authdata();

   public static int embedded = common_h.FALSE;

   public static int display_header = common_h.FALSE;

   public static ArrayList<hostoutage> hostoutage_list = new ArrayList<hostoutage>();

   public static int total_blocking_outages = 0;

   public static int total_nonblocking_outages = 0;

   public static int total_service_health = 0;

   public static int total_host_health = 0;

   public static int potential_service_health = 0;

   public static int potential_host_health = 0;

   public static double percent_service_health = 0.0;

   public static double percent_host_health = 0.0;

   public static int total_hosts = 0;

   public static int total_services = 0;

   public static int total_active_service_checks = 0;

   public static int total_active_host_checks = 0;

   public static int total_passive_service_checks = 0;

   public static int total_passive_host_checks = 0;

   public static double min_service_execution_time = -1.0;

   public static double max_service_execution_time = -1.0;

   public static double total_service_execution_time = 0.0;

   public static double average_service_execution_time = -1.0;

   public static double min_host_execution_time = -1.0;

   public static double max_host_execution_time = -1.0;

   public static double total_host_execution_time = 0.0;

   public static double average_host_execution_time = -1.0;

   public static double min_service_latency = -1.0;

   public static double max_service_latency = -1.0;

   public static double total_service_latency = 0.0;

   public static double average_service_latency = -1.0;

   public static double min_host_latency = -1.0;

   public static double max_host_latency = -1.0;

   public static double total_host_latency = 0.0;

   public static double average_host_latency = -1.0;

   public static int flapping_services = 0;

   public static int flapping_hosts = 0;

   public static int flap_disabled_services = 0;

   public static int flap_disabled_hosts = 0;

   public static int notification_disabled_services = 0;

   public static int notification_disabled_hosts = 0;

   public static int event_handler_disabled_services = 0;

   public static int event_handler_disabled_hosts = 0;

   public static int active_checks_disabled_services = 0;

   public static int active_checks_disabled_hosts = 0;

   public static int passive_checks_disabled_services = 0;

   public static int passive_checks_disabled_hosts = 0;

   public static int hosts_pending = 0;

   public static int hosts_pending_disabled = 0;

   public static int hosts_up_disabled = 0;

   public static int hosts_up_unacknowledged = 0;

   public static int hosts_up = 0;

   public static int hosts_down_scheduled = 0;

   public static int hosts_down_acknowledged = 0;

   public static int hosts_down_disabled = 0;

   public static int hosts_down_unacknowledged = 0;

   public static int hosts_down = 0;

   public static int hosts_unreachable_scheduled = 0;

   public static int hosts_unreachable_acknowledged = 0;

   public static int hosts_unreachable_disabled = 0;

   public static int hosts_unreachable_unacknowledged = 0;

   public static int hosts_unreachable = 0;

   public static int services_pending = 0;

   public static int services_pending_disabled = 0;

   public static int services_ok_disabled = 0;

   public static int services_ok_unacknowledged = 0;

   public static int services_ok = 0;

   public static int services_warning_host_problem = 0;

   public static int services_warning_scheduled = 0;

   public static int services_warning_acknowledged = 0;

   public static int services_warning_disabled = 0;

   public static int services_warning_unacknowledged = 0;

   public static int services_warning = 0;

   public static int services_unknown_host_problem = 0;

   public static int services_unknown_scheduled = 0;

   public static int services_unknown_acknowledged = 0;

   public static int services_unknown_disabled = 0;

   public static int services_unknown_unacknowledged = 0;

   public static int services_unknown = 0;

   public static int services_critical_host_problem = 0;

   public static int services_critical_scheduled = 0;

   public static int services_critical_acknowledged = 0;

   public static int services_critical_disabled = 0;

   public static int services_critical_unacknowledged = 0;

   public static int services_critical = 0;

   public void call_main() {
      main( null );
   }

   public void reset_context()
   {
      embedded = common_h.FALSE;
      display_header = common_h.FALSE;

      hostoutage_list.clear();

      total_blocking_outages = 0;
      total_nonblocking_outages = 0;

      total_service_health = 0;
      total_host_health = 0;
      potential_service_health = 0;
      potential_host_health = 0;
      percent_service_health = 0.0;
      percent_host_health = 0.0;

      total_hosts = 0;
      total_services = 0;

      total_active_service_checks = 0;
      total_active_host_checks = 0;
      total_passive_service_checks = 0;
      total_passive_host_checks = 0;

      min_service_execution_time = -1.0;
      max_service_execution_time = -1.0;
      total_service_execution_time = 0.0;
      average_service_execution_time = -1.0;
      min_host_execution_time = -1.0;
      max_host_execution_time = -1.0;
      total_host_execution_time = 0.0;
      average_host_execution_time = -1.0;
      min_service_latency = -1.0;
      max_service_latency = -1.0;
      total_service_latency = 0.0;
      average_service_latency = -1.0;
      min_host_latency = -1.0;
      max_host_latency = -1.0;
      total_host_latency = 0.0;
      average_host_latency = -1.0;

      flapping_services = 0;
      flapping_hosts = 0;
      flap_disabled_services = 0;
      flap_disabled_hosts = 0;
      notification_disabled_services = 0;
      notification_disabled_hosts = 0;
      event_handler_disabled_services = 0;
      event_handler_disabled_hosts = 0;
      active_checks_disabled_services = 0;
      active_checks_disabled_hosts = 0;
      passive_checks_disabled_services = 0;
      passive_checks_disabled_hosts = 0;

      hosts_pending = 0;
      hosts_pending_disabled = 0;
      hosts_up_disabled = 0;
      hosts_up_unacknowledged = 0;
      hosts_up = 0;
      hosts_down_scheduled = 0;
      hosts_down_acknowledged = 0;
      hosts_down_disabled = 0;
      hosts_down_unacknowledged = 0;
      hosts_down = 0;
      hosts_unreachable_scheduled = 0;
      hosts_unreachable_acknowledged = 0;
      hosts_unreachable_disabled = 0;
      hosts_unreachable_acknowledged = 0;
      hosts_unreachable = 0;

      services_pending = 0;
      services_pending_disabled = 0;
      services_ok_disabled = 0;
      services_ok_unacknowledged = 0;
      services_ok = 0;
      services_warning_host_problem = 0;
      services_warning_scheduled = 0;
      services_warning_acknowledged = 0;
      services_warning_disabled = 0;
      services_warning_unacknowledged = 0;
      services_warning = 0;
      services_unknown_host_problem = 0;
      services_unknown_scheduled = 0;
      services_unknown_acknowledged = 0;
      services_unknown_disabled = 0;
      services_unknown_unacknowledged = 0;
      services_unknown = 0;
      services_critical_host_problem = 0;
      services_critical_scheduled = 0;
      services_critical_acknowledged = 0;
      services_critical_disabled = 0;
      services_critical_unacknowledged = 0;
      services_critical = 0;
   }

   public static void main(String[] args)
   {
      int result = common_h.OK;
      String sound = null;

      /* get the CGI variables passed in the URL */
      process_cgivars();
      
      /* read the CGI configuration file */
      result = cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
      if (result == common_h.ERROR)
      {
         document_header(common_h.FALSE);
         cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
         document_footer();
         cgiutils.exit(common_h.ERROR);
         return;
      }

      /* read the main configuration file */
      result = cgiutils.read_main_config_file(cgiutils.main_config_file);
      if (result == common_h.ERROR)
      {
         document_header(common_h.FALSE);
         cgiutils.main_config_file_error(cgiutils.main_config_file);
         document_footer();
         cgiutils.exit(common_h.ERROR);
         return;
      }

      /* read all object configuration data */
      result = cgiutils.read_all_object_configuration_data(cgiutils.main_config_file, common_h.READ_ALL_OBJECT_DATA);
      if (result == common_h.ERROR)
      {
         document_header(common_h.FALSE);
         cgiutils.object_data_error();
         document_footer();
         cgiutils.exit(common_h.ERROR);
         return;
      }

      /* read all status data */
      result = cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(), statusdata_h.READ_ALL_STATUS_DATA);
      if (result == common_h.ERROR)
      {
         document_header(common_h.FALSE);
         cgiutils.status_data_error();
         document_footer();
         cgiutils.exit(common_h.ERROR);
         return;
      }

      document_header(common_h.TRUE);

      /* get authentication information */
      cgiauth.get_authentication_information(current_authdata);

      if (display_header == common_h.TRUE)
      {

         /* begin top table */
         System.out.printf("<table border=0 width=100%% cellpadding=0 cellspacing=0>\n");
         System.out.printf("<tr>\n");

         /* left column of top table - info box */
         System.out.printf("<td align=left valign=top width=33%%>\n");
         cgiutils.display_info_table("Tactical Status Overview", common_h.TRUE, current_authdata);
         System.out.printf("</td>\n");

         /* middle column of top table - log file navigation options */
         System.out.printf("<td align=center valign=top width=33%%>\n");
         System.out.printf("</td>\n");

         /* right hand column of top row */
         System.out.printf("<td align=right valign=top width=33%%>\n");
         System.out.printf("</td>\n");

         /* end of top table */
         System.out.printf("</tr>\n");
         System.out.printf("</table>\n");
         System.out.printf("</p>\n");

      }

      /* analyze current host and service status data for tac overview */
      analyze_status_data();

      /* find all hosts that are causing network outages */
      find_hosts_causing_outages();

      /* embed sound tag if necessary... */
      if (hosts_unreachable_unacknowledged > 0 && cgiutils.host_unreachable_sound != null)
         sound = cgiutils.host_unreachable_sound;
      else if (hosts_down_unacknowledged > 0 && cgiutils.host_down_sound != null)
         sound = cgiutils.host_down_sound;
      else if (services_critical_unacknowledged > 0 && cgiutils.service_critical_sound != null)
         sound = cgiutils.service_critical_sound;
      else if (services_warning_unacknowledged > 0 && cgiutils.service_warning_sound != null)
         sound = cgiutils.service_warning_sound;
      else if (services_unknown_unacknowledged == 0 && services_warning_unacknowledged == 0
            && services_critical_unacknowledged == 0 && hosts_down_unacknowledged == 0
            && hosts_unreachable_unacknowledged == 0 && cgiutils.normal_sound != null)
         sound = cgiutils.normal_sound;
      if(sound!=null){
         System.out.printf("<object type=\"application/x-mplayer2\" height=\"-\" width=\"0\">");
         System.out.printf("<param name=\"filename\" value=\"%s%s\">",cgiutils.url_media_path,sound);
         System.out.printf("<param name=\"autostart\" value=\"1\">");
         System.out.printf("<param name=\"playcount\" value=\"1\">");
         System.out.printf("</object>");
      }

      /**** display main tac screen ****/
      display_tac_overview();

      document_footer();

      cgiutils.exit(common_h.OK);
   }

   public static void document_header(int use_stylesheet)
   {
      String date_time;

      if (response != null)
      {
         response.setHeader("Cache-Control", "no-store");
         response.setHeader("Pragma", "no-cache");
         response.setIntHeader("Refresh", cgiutils.refresh_rate);
         response.setDateHeader("Last-Modified", System.currentTimeMillis());
         response.setDateHeader("Expires", System.currentTimeMillis());
         response.setContentType("text/html");
      }
      else
      {
         System.out.printf("Cache-Control: no-store\r\n");
         System.out.printf("Pragma: no-cache\r\n");
         System.out.printf("Refresh: %d\r\n", cgiutils.refresh_rate);

         date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
         System.out.printf("Last-Modified: %s\r\n", date_time);

         date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
         System.out.printf("Expires: %s\r\n", date_time);

         System.out.printf("Content-type: text/html\r\n\r\n");
      }

      if (embedded == common_h.TRUE)
         return;

      System.out.printf("<HTML>\n");
      System.out.printf("<HEAD>\n");
      System.out.printf("<TITLE>\n");
      System.out.printf("Blue Tactical Monitoring Overview\n");
      System.out.printf("</TITLE>\n");

      if (use_stylesheet == common_h.TRUE)
      {
         System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n", cgiutils.url_stylesheets_path,
               cgiutils_h.COMMON_CSS);
         System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n", cgiutils.url_stylesheets_path,
               cgiutils_h.TAC_CSS);
      }

      System.out.printf("</HEAD>\n");
      System.out.printf("<BODY CLASS='tac' marginwidth=2 marginheight=2 topmargin=0 leftmargin=0 rightmargin=0>\n");

      /* include user SSI header */
      cgiutils.include_ssi_files(cgiutils_h.TAC_CGI, cgiutils_h.SSI_HEADER);

      return;
   }

   public static void document_footer()
   {

      if (embedded == common_h.TRUE)
         return;

      /* include user SSI footer */
      cgiutils.include_ssi_files(cgiutils_h.TAC_CGI, cgiutils_h.SSI_FOOTER);

      System.out.printf("</BODY>\n");
      System.out.printf("</HTML>\n");

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
            continue;
         }

         /* we found the embed option */
         else if (variables[x].equals("embedded"))
            embedded = common_h.TRUE;

         /* we found the noheader option */
         else if (variables[x].equals("noheader"))
            display_header = common_h.FALSE;

         /* we received an invalid argument */
         else
            error = common_h.TRUE;

      }

      /* free memory allocated to the CGI variables */
      getcgi.free_cgivars(variables);

      return error;
   }

   public static void analyze_status_data()
   {
      //	statusdata_h.servicestatus temp_servicestatus;
      objects_h.service temp_service;
      //	statusdata_h.hoststatus temp_hoststatus;
      objects_h.host temp_host;
      int problem = common_h.TRUE;

      /* check all services */
      for (statusdata_h.servicestatus temp_servicestatus : (ArrayList<statusdata_h.servicestatus>) statusdata.servicestatus_list)
      {

         /* see if user is authorized to view this service */
         temp_service = objects.find_service(temp_servicestatus.host_name, temp_servicestatus.description);
         
         if(cgiauth.is_authorized_for_service(temp_service, current_authdata) == common_h.FALSE)
            continue;

         /******** CHECK FEATURES *******/

         /* check flapping */
         if (temp_servicestatus.flap_detection_enabled == common_h.FALSE)
            flap_disabled_services++;
         else if (temp_servicestatus.is_flapping == common_h.TRUE)
            flapping_services++;

         /* check notifications */
         if (temp_servicestatus.notifications_enabled == common_h.FALSE)
            notification_disabled_services++;

         /* check event handler */
         if (temp_servicestatus.event_handler_enabled == common_h.FALSE)
            event_handler_disabled_services++;

         /* active check execution */
         if (temp_servicestatus.checks_enabled == common_h.FALSE)
            active_checks_disabled_services++;

         /* passive check acceptance */
         if (temp_servicestatus.accept_passive_service_checks == common_h.FALSE)
            passive_checks_disabled_services++;

         /********* CHECK STATUS ********/

         problem = common_h.TRUE;

         if (temp_servicestatus.status == statusdata_h.SERVICE_OK)
         {
            if (temp_servicestatus.checks_enabled == common_h.FALSE)
               services_ok_disabled++;
            else
               services_ok_unacknowledged++;
            services_ok++;
         }

         else if (temp_servicestatus.status == statusdata_h.SERVICE_WARNING)
         {
            statusdata_h.hoststatus temp_hoststatus = statusdata.find_hoststatus(temp_servicestatus.host_name);
            if (temp_hoststatus != null
                  && (temp_hoststatus.status == statusdata_h.HOST_DOWN || temp_hoststatus.status == statusdata_h.HOST_UNREACHABLE))
            {
               services_warning_host_problem++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.scheduled_downtime_depth > 0)
            {
               services_warning_scheduled++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.problem_has_been_acknowledged == common_h.TRUE)
            {
               services_warning_acknowledged++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.checks_enabled == common_h.FALSE)
            {
               services_warning_disabled++;
               problem = common_h.FALSE;
            }
            if (problem == common_h.TRUE)
               services_warning_unacknowledged++;
            services_warning++;
         }

         else if (temp_servicestatus.status == statusdata_h.SERVICE_UNKNOWN)
         {
            statusdata_h.hoststatus temp_hoststatus = statusdata.find_hoststatus(temp_servicestatus.host_name);
            if (temp_hoststatus != null
                  && (temp_hoststatus.status == statusdata_h.HOST_DOWN || temp_hoststatus.status == statusdata_h.HOST_UNREACHABLE))
            {
               services_unknown_host_problem++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.scheduled_downtime_depth > 0)
            {
               services_unknown_scheduled++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.problem_has_been_acknowledged == common_h.TRUE)
            {
               services_unknown_acknowledged++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.checks_enabled == common_h.FALSE)
            {
               services_unknown_disabled++;
               problem = common_h.FALSE;
            }
            if (problem == common_h.TRUE)
               services_unknown_unacknowledged++;
            services_unknown++;
         }

         else if (temp_servicestatus.status == statusdata_h.SERVICE_CRITICAL)
         {
            statusdata_h.hoststatus temp_hoststatus = statusdata.find_hoststatus(temp_servicestatus.host_name);
            if (temp_hoststatus != null
                  && (temp_hoststatus.status == statusdata_h.HOST_DOWN || temp_hoststatus.status == statusdata_h.HOST_UNREACHABLE))
            {
               services_critical_host_problem++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.scheduled_downtime_depth > 0)
            {
               services_critical_scheduled++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.problem_has_been_acknowledged == common_h.TRUE)
            {
               services_critical_acknowledged++;
               problem = common_h.FALSE;
            }
            if (temp_servicestatus.checks_enabled == common_h.FALSE)
            {
               services_critical_disabled++;
               problem = common_h.FALSE;
            }
            if (problem == common_h.TRUE)
               services_critical_unacknowledged++;
            services_critical++;
         }

         else if (temp_servicestatus.status == statusdata_h.SERVICE_PENDING)
         {
            if (temp_servicestatus.checks_enabled == common_h.FALSE)
               services_pending_disabled++;
            services_pending++;
         }

         /* get health stats */
         if (temp_servicestatus.status == statusdata_h.SERVICE_OK)
            total_service_health += 2;

         else if (temp_servicestatus.status == statusdata_h.SERVICE_WARNING
               || temp_servicestatus.status == statusdata_h.SERVICE_UNKNOWN)
            total_service_health++;

         if (temp_servicestatus.status != statusdata_h.SERVICE_PENDING)
            potential_service_health += 2;

         /* calculate execution time and latency stats */
         if (temp_servicestatus.check_type == common_h.SERVICE_CHECK_ACTIVE) {
            total_active_service_checks++;

            if (min_service_latency == -1.0 || temp_servicestatus.latency < min_service_latency)
               min_service_latency = temp_servicestatus.latency;
            
            if (max_service_latency == -1.0 || temp_servicestatus.latency > max_service_latency)
               max_service_latency = temp_servicestatus.latency;

            if (min_service_execution_time == -1.0 || temp_servicestatus.execution_time < min_service_execution_time)
               min_service_execution_time = temp_servicestatus.execution_time;
            
            if (max_service_execution_time == -1.0 || temp_servicestatus.execution_time > max_service_execution_time)
               max_service_execution_time = temp_servicestatus.execution_time;

            total_service_latency += temp_servicestatus.latency;
            total_service_execution_time += temp_servicestatus.execution_time;
         }
         else
            total_passive_service_checks++;

         total_services++;
      }

      /* check all hosts */
      for (statusdata_h.hoststatus temp_hoststatus : (ArrayList<statusdata_h.hoststatus>) statusdata.hoststatus_list)
      {

         /* see if user is authorized to view this host */
         temp_host = objects.find_host(temp_hoststatus.host_name);
         if (cgiauth.is_authorized_for_host(temp_host, current_authdata) == common_h.FALSE)
            continue;

         /******** CHECK FEATURES *******/

         /* check flapping */
         if (temp_hoststatus.flap_detection_enabled == common_h.FALSE)
            flap_disabled_hosts++;
         else if (temp_hoststatus.is_flapping == common_h.TRUE)
            flapping_hosts++;

         /* check notifications */
         if (temp_hoststatus.notifications_enabled == common_h.FALSE)
            notification_disabled_hosts++;

         /* check event handler */
         if (temp_hoststatus.event_handler_enabled == common_h.FALSE)
            event_handler_disabled_hosts++;

         /* active check execution */
         if (temp_hoststatus.checks_enabled == common_h.FALSE)
            active_checks_disabled_hosts++;

         /* passive check acceptance */
         if (temp_hoststatus.accept_passive_host_checks == common_h.FALSE)
            passive_checks_disabled_hosts++;

         /********* CHECK STATUS ********/

         problem = common_h.TRUE;

         if (temp_hoststatus.status == statusdata_h.HOST_UP)
         {
            if (temp_hoststatus.checks_enabled == common_h.FALSE)
               hosts_up_disabled++;
            else
               hosts_up_unacknowledged++;
            hosts_up++;
         }

         else if (temp_hoststatus.status == statusdata_h.HOST_DOWN)
         {
            if (temp_hoststatus.scheduled_downtime_depth > 0)
            {
               hosts_down_scheduled++;
               problem = common_h.FALSE;
            }
            if (temp_hoststatus.problem_has_been_acknowledged == common_h.TRUE)
            {
               hosts_down_acknowledged++;
               problem = common_h.FALSE;
            }
            if (temp_hoststatus.checks_enabled == common_h.FALSE)
            {
               hosts_down_disabled++;
               problem = common_h.FALSE;
            }
            if (problem == common_h.TRUE)
               hosts_down_unacknowledged++;
            hosts_down++;
         }

         else if (temp_hoststatus.status == statusdata_h.HOST_UNREACHABLE)
         {
            if (temp_hoststatus.scheduled_downtime_depth > 0)
            {
               hosts_unreachable_scheduled++;
               problem = common_h.FALSE;
            }
            if (temp_hoststatus.problem_has_been_acknowledged == common_h.TRUE)
            {
               hosts_unreachable_acknowledged++;
               problem = common_h.FALSE;
            }
            if (temp_hoststatus.checks_enabled == common_h.FALSE)
            {
               hosts_unreachable_disabled++;
               problem = common_h.FALSE;
            }
            if (problem == common_h.TRUE)
               hosts_unreachable_unacknowledged++;
            hosts_unreachable++;
         }

         else if (temp_hoststatus.status == statusdata_h.HOST_PENDING)
         {
            if (temp_hoststatus.checks_enabled == common_h.FALSE)
               hosts_pending_disabled++;
            hosts_pending++;
         }

         /* get health stats */
         if (temp_hoststatus.status == statusdata_h.HOST_UP)
            total_host_health++;

         if (temp_hoststatus.status != statusdata_h.HOST_PENDING)
            potential_host_health++;

         /* check type stats */
         if (temp_hoststatus.check_type == common_h.HOST_CHECK_ACTIVE)
         {

            total_active_host_checks++;

            if (min_host_latency == -1.0 || temp_hoststatus.latency < min_host_latency)
               min_host_latency = temp_hoststatus.latency;
            if (max_host_latency == -1.0 || temp_hoststatus.latency > max_host_latency)
               max_host_latency = temp_hoststatus.latency;

            if (min_host_execution_time == -1.0 || temp_hoststatus.execution_time < min_host_execution_time)
               min_host_execution_time = temp_hoststatus.execution_time;
            if (max_host_execution_time == -1.0 || temp_hoststatus.execution_time > max_host_execution_time)
               max_host_execution_time = temp_hoststatus.execution_time;

            total_host_latency += temp_hoststatus.latency;
            total_host_execution_time += temp_hoststatus.execution_time;
         }
         else
            total_passive_host_checks++;

         total_hosts++;
      }

      /* calculate service health */
      if (potential_service_health == 0)
         percent_service_health = 0.0;
      else
         percent_service_health = ((double) total_service_health / (double) potential_service_health) * 100.0;

      /* calculate host health */
      if (potential_host_health == 0)
         percent_host_health = 0.0;
      else
         percent_host_health = ((double) total_host_health / (double) potential_host_health) * 100.0;

      /* calculate service latency */
      if (total_service_latency == 0L)
         average_service_latency = 0.0;
      else
         average_service_latency = (total_service_latency / total_active_service_checks);

      /* calculate host latency */
      if (total_host_latency == 0L)
         average_host_latency = 0.0;
      else
         average_host_latency = (total_host_latency / total_active_host_checks);

      /* calculate service execution time */
      if (total_service_execution_time == 0.0)
         average_service_execution_time = 0.0;
      else
         average_service_execution_time = (total_service_execution_time / total_active_service_checks);

      /* calculate host execution time */
      if (total_host_execution_time == 0.0)
         average_host_execution_time = 0.0;
      else
         average_host_execution_time = (total_host_execution_time / total_active_host_checks);

      return;
   }

   /* determine what hosts are causing network outages */
   public static void find_hosts_causing_outages()
   {
      // statusdata_h.hoststatus temp_hoststatus;
      // hostoutage temp_hostoutage;
      objects_h.host temp_host;

      /* user must be authorized for all hosts in order to see outages */
      if (cgiauth.is_authorized_for_all_hosts(current_authdata) == common_h.FALSE)
         return;

      /* check all hosts */
      for (statusdata_h.hoststatus temp_hoststatus : (ArrayList<statusdata_h.hoststatus>) statusdata.hoststatus_list)
      {

         /* check only hosts that are not up and not pending */
         if (temp_hoststatus.status != statusdata_h.HOST_UP && temp_hoststatus.status != statusdata_h.HOST_PENDING)
         {

            /* find the host entry */
            temp_host = objects.find_host(temp_hoststatus.host_name);

            if (temp_host == null)
               continue;

            /* if the route to this host is not blocked, it is a causing an outage */
            if (is_route_to_host_blocked(temp_host) == common_h.FALSE)
               add_hostoutage(temp_host);
         }
      }

      /* check all hosts that are causing problems and calculate the extent of the problem */
      for (hostoutage temp_hostoutage : hostoutage_list)
      {

         /* calculate the outage effect of this particular hosts */
         temp_hostoutage.affected_child_hosts = calculate_outage_effect_of_host(temp_hostoutage.hst);

         if (temp_hostoutage.affected_child_hosts > 1)
            total_blocking_outages++;
         else
            total_nonblocking_outages++;
      }

      return;
   }

   /* adds a host outage entry */
   public static void add_hostoutage(objects_h.host hst)
   {
      hostoutage new_hostoutage;

      /* allocate memory for a new structure */
      new_hostoutage = new hostoutage();

      if (new_hostoutage == null)
         return;

      new_hostoutage.hst = hst;
      new_hostoutage.affected_child_hosts = 0;

      /* add the structure to the head of the list in memory */
      hostoutage_list.add(new_hostoutage);

      return;
   }


   /* calculates network outage effect of a particular host being down or unreachable */
   public static int calculate_outage_effect_of_host(objects_h.host hst)
   {
      int total_child_hosts_affected = 0;
      int temp_child_hosts_affected = 0;

      /* find all child hosts of this host */
      for (objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list)
      {

         /* skip this host if it is not a child */
         if (objects.is_host_immediate_child_of_host(hst, temp_host) == common_h.FALSE)
            continue;

         /* calculate the outage effect of the child */
         temp_child_hosts_affected = calculate_outage_effect_of_host(temp_host);

         /* keep a running total of outage effects */
         total_child_hosts_affected += temp_child_hosts_affected;
      }

      return total_child_hosts_affected + 1;

   }

   /* tests whether or not a host is "blocked" by upstream parents (host is already assumed to be down or unreachable) */
   public static int is_route_to_host_blocked(objects_h.host hst)
   {
      statusdata_h.hoststatus temp_hoststatus;

      /* if the host has no parents, it is not being blocked by anyone */
      if (hst.parent_hosts == null || hst.parent_hosts.size() == 0)
         return common_h.FALSE;

      /* check all parent hosts */
      for (objects_h.hostsmember temp_hostsmember : (ArrayList<objects_h.hostsmember>) hst.parent_hosts)
      {

         /* find the parent host's status */
         temp_hoststatus = statusdata.find_hoststatus(temp_hostsmember.host_name);

         if (temp_hoststatus == null)
            continue;

         /* at least one parent it up (or pending), so this host is not blocked */
         if (temp_hoststatus.status == statusdata_h.HOST_UP || temp_hoststatus.status == statusdata_h.HOST_PENDING)
            return common_h.FALSE;
      }

      return common_h.TRUE;
   }

   public static void display_tac_overview()
   {
      String host_health_image;
      String service_health_image;

      System.out.printf("<p align=left>\n");

      System.out.printf("<table border=0 align=left width=100%% cellspacing=4 cellpadding=0>\n");
      System.out.printf("<tr>\n");

      /* left column */
      System.out.printf("<td align=left valign=top width=50%%>\n");

      cgiutils.display_info_table("Tactical Monitoring Overview", common_h.TRUE, current_authdata);

      System.out.printf("</td>\n");

      /* right column */
      System.out.printf("<td align=right valign=bottom width=50%%>\n");

      System.out.printf("<table border=0 cellspacing=0 cellspadding=0>\n");

      System.out.printf("<tr>\n");

      System.out.printf("<td valign=bottom align=right>\n");

      /* display context-sensitive help */
      cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_TAC);

      System.out.printf("</td>\n");

      System.out.printf("<td>\n");

      System.out.printf("<table border=0 cellspacing=4 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf(
            "<td class='perfTitle'>&nbsp;<a href='%s?type=%d' class='perfTitle'>Monitoring Performance</a></td>\n",
            cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE);
      System.out.printf("</tr>\n");

      System.out.printf("<tr>\n");
      System.out.printf("<td>\n");

      System.out.printf("<table border=0 cellspacing=0 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td class='perfBox'>\n");
      System.out.printf("<table border=0 cellspacing=4 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td align=left valign=center class='perfItem'><a href='%s?type=%d' class='perfItem'>Service Check Execution Time:</a></td>",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE);
      System.out
            .printf(
                  "<td valign=top class='perfValue' nowrap><a href='%s?type=%d' class='perfValue'>%.2f / %.2f / %.3f sec</a></td>\n",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE, min_service_execution_time,
                  max_service_execution_time, average_service_execution_time);
      System.out.printf("</tr>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td align=left valign=center class='perfItem'><a href='%s?type=%d' class='perfItem'>Service Check Latency:</a></td>",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE);
      System.out
            .printf(
                  "<td valign=top class='perfValue' nowrap><a href='%s?type=%d' class='perfValue'>%.2f / %.2f / %.3f sec</a></td>\n",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE, min_service_latency, max_service_latency,
                  average_service_latency);
      System.out.printf("</tr>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td align=left valign=center class='perfItem'><a href='%s?type=%d' class='perfItem'>Host Check Execution Time:</a></td>",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE);
      System.out
            .printf(
                  "<td valign=top class='perfValue' nowrap><a href='%s?type=%d' class='perfValue'>%.2f / %.2f / %.3f sec</a></td>\n",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE, min_host_execution_time,
                  max_host_execution_time, average_host_execution_time);
      System.out.printf("</tr>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td align=left valign=center class='perfItem'><a href='%s?type=%d' class='perfItem'>Host Check Latency:</a></td>",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE);
      System.out
            .printf(
                  "<td valign=top class='perfValue' nowrap><a href='%s?type=%d' class='perfValue'>%.2f / %.2f / %2.3f sec</a></td>\n",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PERFORMANCE, min_host_latency, max_host_latency,
                  average_host_latency);
      System.out.printf("</tr>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td align=left valign=center class='perfItem'><a href='%s?host=all&serviceprops=%d' class='perfItem'># Active Host / Service Checks:</a></td>",
                  cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_ACTIVE_CHECK);
      System.out
            .printf(
                  "<td valign=top class='perfValue' nowrap><a href='%s?hostgroup=all&hostprops=%d&style=hostdetail' class='perfValue'>%d</a> / <a href='%s?host=all&serviceprops=%d' class='perfValue'>%d</a></td>\n",
                  cgiutils_h.STATUS_CGI, cgiutils_h.HOST_ACTIVE_CHECK, total_active_host_checks, cgiutils_h.STATUS_CGI,
                  cgiutils_h.SERVICE_ACTIVE_CHECK, total_active_service_checks);
      System.out.printf("</tr>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td align=left valign=center class='perfItem'><a href='%s?host=all&serviceprops=%d' class='perfItem'># Passive Host / Service Checks:</a></td>",
                  cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_PASSIVE_CHECK);
      System.out
            .printf(
                  "<td valign=top class='perfValue' nowrap><a href='%s?hostgroup=all&hostprops=%d&style=hostdetail' class='perfValue'>%d</a> / <a href='%s?host=all&serviceprops=%d' class='perfValue'>%d</a></td>\n",
                  cgiutils_h.STATUS_CGI, cgiutils_h.HOST_PASSIVE_CHECK, total_passive_host_checks,
                  cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_PASSIVE_CHECK, total_passive_service_checks);
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</p>\n");

      System.out.printf("<br clear=all>\n");
      System.out.printf("<br>\n");

      System.out.printf("<table border=0 cellspacing=0 cellpadding=0 width=100%%>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=top align=left width=50%%>\n");

      /******* OUTAGES ********/

      System.out.printf("<p>\n");

      System.out.printf("<table class='tac' width=125 cellspacing=4 cellpadding=0 border=0>\n");

      System.out.printf("<tr><td colspan=1 height=20 class='outageTitle'>&nbsp;Network Outages</td></tr>\n");

      System.out.printf("<tr>\n");
      System.out
            .printf("<td class='outageHeader' width=125><a href='%s' class='outageHeader'>", cgiutils_h.OUTAGES_CGI);
      if (cgiauth.is_authorized_for_all_hosts(current_authdata) == common_h.FALSE)
         System.out.printf("N/A");
      else
         System.out.printf("%d Outages", total_blocking_outages);
      System.out.printf("</a></td>\n");
      System.out.printf("</tr>\n");

      System.out.printf("<tr>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;&nbsp;&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (total_blocking_outages > 0)
         System.out.printf(
               "<tr><td width=100%% class='outageImportantProblem'><a href='%s'>%d Blocking Outages</a></td></tr>\n",
               cgiutils_h.OUTAGES_CGI, total_blocking_outages);

      /*
       if(total_nonblocking_outages>0)
       System.out.printf("<tr><td width=100%% class='outageUnimportantProblem'><a href='%s'>%d Nonblocking Outages</a></td></tr>\n",OUTAGES_CGI,total_nonblocking_outages);
       */

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</p>\n");

      System.out.printf("</td>\n");

      /* right column */
      System.out.printf("<td valign=top align=right width=50%%>\n");

      if (percent_host_health < HEALTH_CRITICAL_PERCENTAGE)
         host_health_image = cgiutils_h.THERM_CRITICAL_IMAGE;
      else if (percent_host_health < HEALTH_WARNING_PERCENTAGE)
         host_health_image = cgiutils_h.THERM_WARNING_IMAGE;
      else
         host_health_image = cgiutils_h.THERM_OK_IMAGE;

      if (percent_service_health < HEALTH_CRITICAL_PERCENTAGE)
         service_health_image = cgiutils_h.THERM_CRITICAL_IMAGE;
      else if (percent_service_health < HEALTH_WARNING_PERCENTAGE)
         service_health_image = cgiutils_h.THERM_WARNING_IMAGE;
      else
         service_health_image = cgiutils_h.THERM_OK_IMAGE;

      System.out.printf("<table border=0 cellspacing=0 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td>\n");

      System.out.printf("<table border=0 cellspacing=4 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td class='healthTitle'>&nbsp;Network Health</td>\n");
      System.out.printf("</tr>\n");

      System.out.printf("<tr>\n");
      System.out.printf("<td>\n");

      System.out.printf("<table border=0 cellspacing=0 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td class='healthBox'>\n");
      System.out.printf("<table border=0 cellspacing=4 cellspadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td align=left valign=center class='healthItem'>Host Health:</td>");
      System.out
            .printf(
                  "<td valign=top width=100 class='healthBar'><img src='%s%s' border=0 width=%d height=20 alt='%2.1f%% Health' title='%2.1f%% Health'></td>\n",
                  cgiutils.url_images_path, host_health_image, (percent_host_health < 5.0)
                        ? 5
                        : (int) percent_host_health, percent_host_health, percent_host_health);
      System.out.printf("</tr>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td align=left valign=center class='healthItem'>Service Health:</td>");
      System.out
            .printf(
                  "<td valign=top width=100 class='healthBar'><img src='%s%s' border=0 width=%d height=20 alt='%2.1f%% Health' title='%2.1f%% Health'></td>\n",
                  cgiutils.url_images_path, service_health_image, (percent_service_health < 5.0)
                        ? 5
                        : (int) percent_service_health, percent_service_health, percent_service_health);
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      /******* HOSTS ********/

      System.out.printf("<p>\n");

      System.out.printf("<table class='tac' width=516 cellspacing=4 cellpadding=0 border=0>\n");

      System.out.printf("<tr><td colspan=4 height=20 class='hostTitle'>&nbsp;Hosts</td></tr>\n");

      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td class='hostHeader' width=125><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d' class='hostHeader'>%d Down</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.HOST_DOWN, hosts_down);
      System.out
            .printf(
                  "<td class='hostHeader' width=125><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d' class='hostHeader'>%d Unreachable</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.HOST_UNREACHABLE, hosts_unreachable);
      System.out
            .printf(
                  "<td class='hostHeader' width=125><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d' class='hostHeader'>%d Up</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.HOST_UP, hosts_up);
      System.out
            .printf(
                  "<td class='hostHeader' width=125><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d' class='hostHeader'>%d Pending</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.HOST_PENDING, hosts_pending);
      System.out.printf("</tr>\n");

      System.out.printf("<tr>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;&nbsp;&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (hosts_down_unacknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostImportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Unhandled Problems</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_DOWN, cgiutils_h.HOST_NO_SCHEDULED_DOWNTIME
                           | cgiutils_h.HOST_STATE_UNACKNOWLEDGED | cgiutils_h.HOST_CHECKS_ENABLED,
                     hosts_down_unacknowledged);

      if (hosts_down_scheduled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Scheduled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_DOWN, cgiutils_h.HOST_SCHEDULED_DOWNTIME,
                     hosts_down_scheduled);

      if (hosts_down_acknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Acknowledged</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_DOWN, cgiutils_h.HOST_STATE_ACKNOWLEDGED,
                     hosts_down_acknowledged);

      if (hosts_down_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_DOWN, cgiutils_h.HOST_CHECKS_DISABLED,
                     hosts_down_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (hosts_unreachable_unacknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostImportantProblem'><a href='%s?host=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Unhandled Problems</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_UNREACHABLE, cgiutils_h.HOST_NO_SCHEDULED_DOWNTIME
                           | cgiutils_h.HOST_STATE_UNACKNOWLEDGED | cgiutils_h.HOST_CHECKS_ENABLED,
                     hosts_unreachable_unacknowledged);

      if (hosts_unreachable_scheduled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Scheduled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_UNREACHABLE, cgiutils_h.HOST_SCHEDULED_DOWNTIME,
                     hosts_unreachable_scheduled);

      if (hosts_unreachable_acknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Acknowledged</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_UNREACHABLE, cgiutils_h.HOST_STATE_ACKNOWLEDGED,
                     hosts_unreachable_acknowledged);

      if (hosts_unreachable_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_UNREACHABLE, cgiutils_h.HOST_CHECKS_DISABLED,
                     hosts_unreachable_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (hosts_up_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_UP, cgiutils_h.HOST_CHECKS_DISABLED, hosts_up_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (hosts_pending_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='hostUnimportantProblem'><a href='%s?hostgroup=all&style=hostdetail&hoststatustypes=%d&hostprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.HOST_PENDING, cgiutils_h.HOST_CHECKS_DISABLED,
                     hosts_pending_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      /*
       System.out.printf("</tr>\n");
       System.out.printf("</table>\n");
       */

      System.out.printf("</p>\n");

      /*System.out.printf("<br clear=all>\n");*/

      /******* SERVICES ********/

      System.out.printf("<p>\n");

      System.out.printf("<table class='tac' width=641 cellspacing=4 cellpadding=0 border=0>\n");

      System.out.printf("<tr><td colspan=5 height=20 class='serviceTitle'>&nbsp;Services</td></tr>\n");

      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td class='serviceHeader' width=125><a href='%s?host=all&stye=detail&servicestatustypes=%d' class='serviceHeader'>%d Critical</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_CRITICAL, services_critical);
      System.out
            .printf(
                  "<td class='serviceHeader' width=125><a href='%s?host=all&stye=detail&servicestatustypes=%d' class='serviceHeader'>%d Warning</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_WARNING, services_warning);
      System.out
            .printf(
                  "<td class='serviceHeader' width=125><a href='%s?host=all&stye=detail&servicestatustypes=%d' class='serviceHeader'>%d Unknown</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_UNKNOWN, services_unknown);
      System.out
            .printf(
                  "<td class='serviceHeader' width=125><a href='%s?host=all&stye=detail&servicestatustypes=%d' class='serviceHeader'>%d Ok</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_OK, services_ok);
      System.out
            .printf(
                  "<td class='serviceHeader' width=125><a href='%s?host=all&stye=detail&servicestatustypes=%d' class='serviceHeader'>%d Pending</a></td>\n",
                  cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_PENDING, services_pending);
      System.out.printf("</tr>\n");

      System.out.printf("<tr>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;&nbsp;&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (services_critical_unacknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceImportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d'>%d Unhandled Problems</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_CRITICAL, statusdata_h.HOST_UP
                           | statusdata_h.HOST_PENDING, cgiutils_h.SERVICE_NO_SCHEDULED_DOWNTIME
                           | cgiutils_h.SERVICE_STATE_UNACKNOWLEDGED | cgiutils_h.SERVICE_CHECKS_ENABLED,
                     services_critical_unacknowledged);

      if (services_critical_host_problem > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&hoststatustypes=%d'>%d on Problem Hosts</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_CRITICAL, statusdata_h.HOST_DOWN
                           | statusdata_h.HOST_UNREACHABLE, services_critical_host_problem);

      if (services_critical_scheduled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Scheduled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_CRITICAL, cgiutils_h.SERVICE_SCHEDULED_DOWNTIME,
                     services_critical_scheduled);

      if (services_critical_acknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Acknowledged</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_CRITICAL, cgiutils_h.SERVICE_STATE_ACKNOWLEDGED,
                     services_critical_acknowledged);

      if (services_critical_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_CRITICAL, cgiutils_h.SERVICE_CHECKS_DISABLED,
                     services_critical_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (services_warning_unacknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceImportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d'>%d Unhandled Problems</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_WARNING, statusdata_h.HOST_UP
                           | statusdata_h.HOST_PENDING, cgiutils_h.SERVICE_NO_SCHEDULED_DOWNTIME
                           | cgiutils_h.SERVICE_STATE_UNACKNOWLEDGED | cgiutils_h.SERVICE_CHECKS_ENABLED,
                     services_warning_unacknowledged);

      if (services_warning_host_problem > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&hoststatustypes=%d'>%d on Problem Hosts</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_WARNING, statusdata_h.HOST_DOWN
                           | statusdata_h.HOST_UNREACHABLE, services_warning_host_problem);

      if (services_warning_scheduled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Scheduled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_WARNING, cgiutils_h.SERVICE_SCHEDULED_DOWNTIME,
                     services_warning_scheduled);

      if (services_warning_acknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Acknowledged</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_WARNING, cgiutils_h.SERVICE_STATE_ACKNOWLEDGED,
                     services_warning_acknowledged);

      if (services_warning_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_WARNING, cgiutils_h.SERVICE_CHECKS_DISABLED,
                     services_warning_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (services_unknown_unacknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceImportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&hoststatustypes=%d&serviceprops=%d'>%d Unhandled Problems</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_UNKNOWN, statusdata_h.HOST_UP
                           | statusdata_h.HOST_PENDING, cgiutils_h.SERVICE_NO_SCHEDULED_DOWNTIME
                           | cgiutils_h.SERVICE_STATE_UNACKNOWLEDGED | cgiutils_h.SERVICE_CHECKS_ENABLED,
                     services_unknown_unacknowledged);

      if (services_unknown_host_problem > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&hoststatustypes=%d'>%d on Problem Hosts</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_UNKNOWN, statusdata_h.HOST_DOWN
                           | statusdata_h.HOST_UNREACHABLE, services_unknown_host_problem);

      if (services_unknown_scheduled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Scheduled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_UNKNOWN, cgiutils_h.SERVICE_SCHEDULED_DOWNTIME,
                     services_unknown_scheduled);

      if (services_unknown_acknowledged > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Acknowledged</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_UNKNOWN, cgiutils_h.SERVICE_STATE_ACKNOWLEDGED,
                     services_unknown_acknowledged);

      if (services_unknown_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_UNKNOWN, cgiutils_h.SERVICE_CHECKS_DISABLED,
                     services_unknown_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<Td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (services_ok_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_OK, cgiutils_h.SERVICE_CHECKS_DISABLED,
                     services_ok_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=125 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out.printf("<td valign=bottom width=25>&nbsp;</td>\n");
      System.out.printf("<Td width=10>&nbsp;</td>\n");

      System.out.printf("<td valign=top width=100%%>\n");
      System.out.printf("<table border=0 width=100%%>\n");

      if (services_pending_disabled > 0)
         System.out
               .printf(
                     "<tr><td width=100%% class='serviceUnimportantProblem'><a href='%s?host=all&type=detail&servicestatustypes=%d&serviceprops=%d'>%d Disabled</a></td></tr>\n",
                     cgiutils_h.STATUS_CGI, statusdata_h.SERVICE_PENDING, cgiutils_h.SERVICE_CHECKS_DISABLED,
                     services_pending_disabled);

      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");

      System.out.printf("</p>\n");

      /*System.out.printf("<br clear=all>\n");*/

      /******* MONITORING FEATURES ********/

      System.out.printf("<p>\n");

      System.out.printf("<table class='tac' cellspacing=4 cellpadding=0 border=0>\n");

      System.out.printf("<tr><td colspan=5 height=20 class='featureTitle'>&nbsp;Monitoring Features</td></tr>\n");

      System.out.printf("<tr>\n");
      System.out.printf("<td class='featureHeader' width=135>Flap Detection</td>\n");
      System.out.printf("<td class='featureHeader' width=135>Notifications</td>\n");
      System.out.printf("<td class='featureHeader' width=135>Event Handlers</td>\n");
      System.out.printf("<td class='featureHeader' width=135>Active Checks</td>\n");
      System.out.printf("<td class='featureHeader' width=135>Passive Checks</td>\n");
      System.out.printf("</tr>\n");

      System.out.printf("<tr>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=135 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td valign=top><a href='%s?cmd_typ=%d'><img src='%s%s' border=0 alt='Flap Detection %s' title='Flap Detection %s'></a></td>\n",
                  cgiutils_h.COMMAND_CGI, (blue.enable_flap_detection == common_h.TRUE)
                        ? common_h.CMD_DISABLE_FLAP_DETECTION
                        : common_h.CMD_ENABLE_FLAP_DETECTION, cgiutils.url_images_path,
                  (blue.enable_flap_detection == common_h.TRUE)
                        ? cgiutils_h.TAC_ENABLED_ICON
                        : cgiutils_h.TAC_DISABLED_ICON, (blue.enable_flap_detection == common_h.TRUE)
                        ? "Enabled"
                        : "Disabled", (blue.enable_flap_detection == common_h.TRUE) ? "Enabled" : "Disabled");
      System.out.printf("<Td width=10>&nbsp;</td>\n");
      if (blue.enable_flap_detection == common_h.TRUE)
      {
         System.out.printf("<Td valign=top width=100%% class='featureEnabledFlapDetection'>\n");
         System.out.printf("<table border=0 width=100%%>\n");

         if (flap_disabled_services > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledServiceFlapDetection'><a href='%s?host=all&type=detail&serviceprops=%d'>%d Service%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_FLAP_DETECTION_DISABLED, flap_disabled_services,
                        (flap_disabled_services == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledServiceFlapDetection'>All Services Enabled</td></tr>\n");

         if (flapping_services > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemServicesFlapping'><a href='%s?host=all&type=detail&serviceprops=%d'>%d Service%s Flapping</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_IS_FLAPPING, flapping_services,
                        (flapping_services == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemServicesNotFlapping'>No Services Flapping</td></tr>\n");

         if (flap_disabled_hosts > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledHostFlapDetection'><a href='%s?host=all&type=detail&hostprops=%d'>%d Host%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.HOST_FLAP_DETECTION_DISABLED, flap_disabled_hosts,
                        (flap_disabled_hosts == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledHostFlapDetection'>All Hosts Enabled</td></tr>\n");

         if (flapping_hosts > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemHostsFlapping'><a href='%s?host=all&type=detail&hostprops=%d'>%d Host%s Flapping</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.HOST_IS_FLAPPING, flapping_hosts, (flapping_hosts == 1)
                              ? ""
                              : "s");
         else
            System.out.printf("<tr><td width=100%% class='featureItemHostsNotFlapping'>No Hosts Flapping</td></tr>\n");

         System.out.printf("</table>\n");
         System.out.printf("</td>\n");
      }
      else
         System.out.printf("<Td valign=center width=100%% class='featureDisabledFlapDetection'>N/A</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=135 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td valign=top><a href='%s?cmd_typ=%d'><img src='%s%s' border=0 alt='Notifications %s' title='Notifications %s'></a></td>\n",
                  cgiutils_h.COMMAND_CGI, (blue.enable_notifications == common_h.TRUE)
                        ? common_h.CMD_DISABLE_NOTIFICATIONS
                        : common_h.CMD_ENABLE_NOTIFICATIONS, cgiutils.url_images_path,
                  (blue.enable_notifications == common_h.TRUE)
                        ? cgiutils_h.TAC_ENABLED_ICON
                        : cgiutils_h.TAC_DISABLED_ICON, (blue.enable_notifications == common_h.TRUE)
                        ? "Enabled"
                        : "Disabled", (blue.enable_notifications == common_h.TRUE) ? "Enabled" : "Disabled");
      System.out.printf("<Td width=10>&nbsp;</td>\n");
      if (blue.enable_notifications == common_h.TRUE)
      {
         System.out.printf("<Td valign=top width=100%% class='featureEnabledNotifications'>\n");
         System.out.printf("<table border=0 width=100%%>\n");

         if (notification_disabled_services > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledServiceNotifications'><a href='%s?host=all&type=detail&serviceprops=%d'>%d Service%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_NOTIFICATIONS_DISABLED,
                        notification_disabled_services, (notification_disabled_services == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledServiceNotifications'>All Services Enabled</td></tr>\n");

         if (notification_disabled_hosts > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledHostNotifications'><a href='%s?host=all&type=detail&hostprops=%d'>%d Host%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.HOST_NOTIFICATIONS_DISABLED, notification_disabled_hosts,
                        (notification_disabled_hosts == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledHostNotifications'>All Hosts Enabled</td></tr>\n");

         System.out.printf("</table>\n");
         System.out.printf("</td>\n");
      }
      else
         System.out.printf("<Td valign=center width=100%% class='featureDisabledNotifications'>N/A</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=135 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td valign=top><a href='%s?cmd_typ=%d'><img src='%s%s' border=0 alt='Event Handlers %s' title='Event Handlers %s'></a></td>\n",
                  cgiutils_h.COMMAND_CGI, (blue.enable_event_handlers == common_h.TRUE)
                        ? common_h.CMD_DISABLE_EVENT_HANDLERS
                        : common_h.CMD_ENABLE_EVENT_HANDLERS, cgiutils.url_images_path,
                  (blue.enable_event_handlers == common_h.TRUE)
                        ? cgiutils_h.TAC_ENABLED_ICON
                        : cgiutils_h.TAC_DISABLED_ICON, (blue.enable_event_handlers == common_h.TRUE)
                        ? "Enabled"
                        : "Disabled", (blue.enable_event_handlers == common_h.TRUE) ? "Enabled" : "Disabled");
      System.out.printf("<Td width=10>&nbsp;</td>\n");
      if (blue.enable_event_handlers == common_h.TRUE)
      {
         System.out.printf("<Td valign=top width=100%% class='featureEnabledHandlers'>\n");
         System.out.printf("<table border=0 width=100%%>\n");

         if (event_handler_disabled_services > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledServiceHandlers'><a href='%s?host=all&type=detail&serviceprops=%d'>%d Service%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_EVENT_HANDLER_DISABLED,
                        event_handler_disabled_services, (event_handler_disabled_services == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledServiceHandlers'>All Services Enabled</td></tr>\n");

         if (event_handler_disabled_hosts > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledHostHandlers'><a href='%s?host=all&type=detail&hostprops=%d'>%d Host%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.HOST_EVENT_HANDLER_DISABLED, event_handler_disabled_hosts,
                        (event_handler_disabled_hosts == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledHostHandlers'>All Hosts Enabled</td></tr>\n");

         System.out.printf("</table>\n");
         System.out.printf("</td>\n");
      }
      else
         System.out.printf("<Td valign=center width=100%% class='featureDisabledHandlers'>N/A</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=135 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td valign=top><a href='%s?type=%d'><img src='%s%s' border='0' alt='Active Checks %s' title='Active Checks %s'></a></td>\n",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PROCESS_INFO, cgiutils.url_images_path,
                  (blue.execute_service_checks == common_h.TRUE)
                        ? cgiutils_h.TAC_ENABLED_ICON
                        : cgiutils_h.TAC_DISABLED_ICON, (blue.execute_service_checks == common_h.TRUE)
                        ? "Enabled"
                        : "Disabled", (blue.execute_service_checks == common_h.TRUE) ? "Enabled" : "Disabled");
      System.out.printf("<Td width=10>&nbsp;</td>\n");
      if (blue.execute_service_checks == common_h.TRUE)
      {
         System.out.printf("<Td valign=top width=100%% class='featureEnabledActiveChecks'>\n");
         System.out.printf("<table border=0 width=100%%>\n");

         if (active_checks_disabled_services > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledActiveServiceChecks'><a href='%s?host=all&type=detail&serviceprops=%d'>%d Service%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_CHECKS_DISABLED, active_checks_disabled_services,
                        (active_checks_disabled_services == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledActiveServiceChecks'>All Services Enabled</td></tr>\n");

         if (active_checks_disabled_hosts > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledActiveHostChecks'><a href='%s?host=all&type=detail&hostprops=%d'>%d Host%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.HOST_CHECKS_DISABLED, active_checks_disabled_hosts,
                        (active_checks_disabled_hosts == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledActiveHostChecks'>All Hosts Enabled</td></tr>\n");

         System.out.printf("</table>\n");
         System.out.printf("</td>\n");
      }
      else
         System.out.printf("<Td valign=center width=100%% class='featureDisabledActiveChecks'>N/A</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("<td valign=top>\n");
      System.out.printf("<table border=0 width=135 cellspacing=0 cellpadding=0>\n");
      System.out.printf("<tr>\n");
      System.out
            .printf(
                  "<td valign=top><a href='%s?type=%d'><img src='%s%s' border='0' alt='Passive Checks %s' title='Passive Checks %s'></a></td>\n",
                  cgiutils_h.EXTINFO_CGI, cgiutils_h.DISPLAY_PROCESS_INFO, cgiutils.url_images_path,
                  (blue.accept_passive_service_checks == common_h.TRUE)
                        ? cgiutils_h.TAC_ENABLED_ICON
                        : cgiutils_h.TAC_DISABLED_ICON, (blue.accept_passive_service_checks == common_h.TRUE)
                        ? "Enabled"
                        : "Disabled", (blue.accept_passive_service_checks == common_h.TRUE) ? "Enabled" : "Disabled");
      System.out.printf("<Td width=10>&nbsp;</td>\n");
      if (blue.accept_passive_service_checks == common_h.TRUE)
      {

         System.out.printf("<Td valign=top width=100%% class='featureEnabledPassiveChecks'>\n");
         System.out.printf("<table border=0 width=100%%>\n");

         if (passive_checks_disabled_services > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledPassiveServiceChecks'><a href='%s?host=all&type=detail&serviceprops=%d'>%d Service%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.SERVICE_PASSIVE_CHECKS_DISABLED,
                        passive_checks_disabled_services, (passive_checks_disabled_services == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledPassiveServiceChecks'>All Services Enabled</td></tr>\n");

         if (passive_checks_disabled_hosts > 0)
            System.out
                  .printf(
                        "<tr><td width=100%% class='featureItemDisabledPassiveHostChecks'><a href='%s?host=all&type=detail&hostprops=%d'>%d Host%s Disabled</a></td></tr>\n",
                        cgiutils_h.STATUS_CGI, cgiutils_h.HOST_PASSIVE_CHECKS_DISABLED, passive_checks_disabled_hosts,
                        (passive_checks_disabled_hosts == 1) ? "" : "s");
         else
            System.out
                  .printf("<tr><td width=100%% class='featureItemEnabledPassiveHostChecks'>All Hosts Enabled</td></tr>\n");

         System.out.printf("</table>\n");
         System.out.printf("</td>\n");
      }
      else
         System.out.printf("<Td valign=center width=100%% class='featureDisabledPassiveChecks'>N/A</td>\n");
      System.out.printf("</tr>\n");
      System.out.printf("</table>\n");
      System.out.printf("</td>\n");

      System.out.printf("</tr>\n");

      System.out.printf("</table>\n");

      System.out.printf("</p>\n");

      return;
   }

}