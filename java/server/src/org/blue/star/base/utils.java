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

package org.blue.star.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.config_h;
import org.blue.star.include.locations_h;
import org.blue.star.include.nebmodules_h;
import org.blue.star.include.objects_h;

//#ifdef EMBEDDEDPERL
//#include "../include/epn_nagios.h"
//static PerlInterpreter *my_perl = null;
//int use_embedded_perl=TRUE;
//#endif

public class utils
{

   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.base.utils");

   private static String cn = "org.blue.base.utils";

   /******************************************************************/
   /************************ MACRO FUNCTIONS *************************/
   /******************************************************************/

   /*
    * This method simply replaces all macros within command definitions with their real values.
    * 
    * @param = String input_buffer, this is the raw command that should have it's macro values
    * 			replaces with their "real" values.
    * 
    * @return = String, the raw command with all macro values replaced with their "real values".
    */
   
   public static String process_macros(String input_buffer, int options)
   {
      String temp_buffer;
      boolean in_macro;
      int x;
      int arg_index = 0;
      int user_index = 0;
      int address_index = 0;
      String selected_macro = null;
      boolean clean_macro = false;
      boolean found_macro_x = false;
      String output_buffer = "";

      logger.trace("entering " + cn + ".process_macros");

      if (input_buffer == null)
         return null;

      in_macro = false;

      logger.debug("**** BEGIN MACRO PROCESSING ***********");
      logger.debug("Processing:  " + input_buffer);

      String split[] = input_buffer.split("\\$");
      for (int s = 0; s < split.length; s++)
      {
         temp_buffer = split[s];

         logger.debug("  Processing part: '" + temp_buffer + "'");

         selected_macro = null;
         found_macro_x = false;
         clean_macro = false;

         if (in_macro == false)
         {
            output_buffer += temp_buffer;
            logger.debug("    Not currently in macro.  Running output (" + output_buffer.length() + "): '"
                  + output_buffer + "'");
            in_macro = true;
         }

         else
         {
            /* general macros */
            for (x = 0; x < blue_h.MACRO_X_COUNT; x++)
            {
               if (blue.macro_x_names[x] != null && temp_buffer.equals(blue.macro_x_names[x]))
               {

                  selected_macro = blue.macro_x[x];
                  found_macro_x = true;

                  /* host/service output/perfdata macros get cleaned */
                  if (x >= 16 && x <= 19)
                  {
                     clean_macro = true;
                     options &= blue_h.STRIP_ILLEGAL_MACRO_CHARS | blue_h.ESCAPE_MACRO_CHARS;
                  }

                  break;
               }
            }

            /* we already have a macro... */
            if (found_macro_x == true)
               x = 0;

            /* on-demand host macros */
            else if (temp_buffer.indexOf("HOST") > 0 && temp_buffer.indexOf(":") > 0)
            {

               grab_on_demand_macro(temp_buffer);
               selected_macro = blue.macro_ondemand;

               /* output/perfdata macros get cleaned */
               if (temp_buffer.startsWith("HOSTOUTPUT:") || temp_buffer.indexOf("HOSTPERFDATA:") > 0)
               {
                  clean_macro = true;
                  options &= blue_h.STRIP_ILLEGAL_MACRO_CHARS | blue_h.ESCAPE_MACRO_CHARS;
               }
            }

            /* on-demand service macros */
            else if (temp_buffer.contains("SERVICE") && temp_buffer.contains(":"))
            {

               grab_on_demand_macro(temp_buffer);
               selected_macro = blue.macro_ondemand;

               /* output/perfdata macros get cleaned */
               if (temp_buffer.startsWith("SERVICEOUTPUT:") || temp_buffer.contains("SERVICEPERFDATA:"))
               {
                  clean_macro = true;
                  options &= blue_h.STRIP_ILLEGAL_MACRO_CHARS | blue_h.ESCAPE_MACRO_CHARS;
               }
            }

            /* argv macros */
            else if (temp_buffer.startsWith("ARG"))
            {
               arg_index = atoi(temp_buffer.substring(3));
               if (arg_index >= 1 && arg_index <= blue_h.MAX_COMMAND_ARGUMENTS)
                  selected_macro = blue.macro_argv[arg_index - 1];
               else
                  selected_macro = null;
            }

            /* user macros */
            else if (temp_buffer.startsWith("USER"))
            {
               user_index = atoi(temp_buffer.substring(4));
               if (user_index >= 1 && user_index <= blue_h.MAX_USER_MACROS)
                  selected_macro = blue.macro_user[user_index - 1];
               else
                  selected_macro = null;
            }

            /* contact address macros */
            else if (temp_buffer.startsWith("CONTACTADDRESS"))
            {
               address_index = atoi(temp_buffer.substring(14));
               if (address_index >= 1 && address_index <= objects_h.MAX_CONTACT_ADDRESSES)
                  selected_macro = blue.macro_contactaddress[address_index - 1];
               else
                  selected_macro = null;
            }

            /* an escaped $ is done by specifying two $$ next to each other */
            else if (temp_buffer.equals(""))
            {
               logger
                     .debug("    Escaped $. Running output (" + output_buffer.length() + "): '" + output_buffer + "'\n");
               output_buffer += "$";
            }

            /* a non-macro, just some user-defined string between two $s */
            else
            {
               logger.debug("    Non-macro.  Running output (" + output_buffer.length() + "): '" + output_buffer
                     + "'\n");
               output_buffer += "$";
               output_buffer += temp_buffer;
               output_buffer += "$";
            }

            /* insert macro */
            if (selected_macro != null)
            {

               /* URL encode the macro */
               if ((options & blue_h.URL_ENCODE_MACRO_CHARS) > 0)
                  selected_macro = get_url_encoded_string(selected_macro);

               /* some macros are cleaned... */
               if (clean_macro == true
                     || (((options & blue_h.STRIP_ILLEGAL_MACRO_CHARS) > 0) || ((options & blue_h.ESCAPE_MACRO_CHARS) > 0)))
                  output_buffer += ((selected_macro == null) ? "" : clean_macro_chars(selected_macro, options));

               /* others are not cleaned */
               else
                  output_buffer += ((selected_macro == null) ? "" : selected_macro);

               /* free memory if necessary */
               if ((options & blue_h.URL_ENCODE_MACRO_CHARS) > 0)
                  selected_macro = null;
               logger.debug("    Just finished macro.  Running output (" + output_buffer.length() + "): '"
                     + output_buffer + "'\n");
            }

            in_macro = false;
         }
      }

      logger.debug("Done.  Final output: '" + output_buffer + "'");
      logger.debug("**** END MACRO PROCESSING *************");

      logger.trace("exiting " + cn + ".process_macros");

      return output_buffer;
   }

   /* grab macros that are specific to a particular service */
   public static void grab_service_macros(objects_h.service svc)
   {
      objects_h.serviceextinfo temp_serviceextinfo;
      long current_time;
      long duration;
      int days;
      int hours;
      int minutes;
      int seconds;

      logger.trace("entering " + cn + ".grab_service_macros");

      /* get the service description */
      blue.macro_x[blue_h.MACRO_SERVICEDESC] = svc.description;

      /* get the plugin output */
      blue.macro_x[blue_h.MACRO_SERVICEOUTPUT] = svc.plugin_output;

      /* get the performance data */
      blue.macro_x[blue_h.MACRO_SERVICEPERFDATA] = svc.perf_data;

      /* get the service check command */
      blue.macro_x[blue_h.MACRO_SERVICECHECKCOMMAND] = svc.service_check_command;

      /* grab the service check type */
      blue.macro_x[blue_h.MACRO_SERVICECHECKTYPE] = ((svc.check_type == common_h.SERVICE_CHECK_PASSIVE)
            ? "PASSIVE"
            : "ACTIVE");

      /* grab the service state type macro (this is usually overridden later on) */
      blue.macro_x[blue_h.MACRO_SERVICESTATETYPE] = ((svc.state_type == common_h.HARD_STATE) ? "HARD" : "SOFT");

      /* get the service state */
      if (svc.current_state == blue_h.STATE_OK)
         blue.macro_x[blue_h.MACRO_SERVICESTATE] = "OK";
      else if (svc.current_state == blue_h.STATE_WARNING)
         blue.macro_x[blue_h.MACRO_SERVICESTATE] = "WARNING";
      else if (svc.current_state == blue_h.STATE_CRITICAL)
         blue.macro_x[blue_h.MACRO_SERVICESTATE] = "CRITICAL";
      else
         blue.macro_x[blue_h.MACRO_SERVICESTATE] = "UNKNOWN";

      /* get the service state id */
      blue.macro_x[blue_h.MACRO_SERVICESTATEID] = "" + svc.current_state;

      /* get the current service check attempt macro */
      blue.macro_x[blue_h.MACRO_SERVICEATTEMPT] = "" + svc.current_attempt;

      /* get the execution time macro */
      blue.macro_x[blue_h.MACRO_SERVICEEXECUTIONTIME] = "" + svc.execution_time;

      /* get the latency macro */
      blue.macro_x[blue_h.MACRO_SERVICELATENCY] = "" + svc.latency;

      /* get the last check time macro */
      blue.macro_x[blue_h.MACRO_LASTSERVICECHECK] = "" + svc.last_check;

      /* get the last state change time macro */
      blue.macro_x[blue_h.MACRO_LASTSERVICESTATECHANGE] = "" + svc.last_state_change;

      /* get the last time ok macro */
      blue.macro_x[blue_h.MACRO_LASTSERVICEOK] = "" + svc.last_time_ok;

      /* get the last time warning macro */
      blue.macro_x[blue_h.MACRO_LASTSERVICEWARNING] = "" + svc.last_time_warning;

      /* get the last time unknown macro */
      blue.macro_x[blue_h.MACRO_LASTSERVICEUNKNOWN] = "" + svc.last_time_unknown;

      /* get the last time critical macro */
      blue.macro_x[blue_h.MACRO_LASTSERVICECRITICAL] = "" + svc.last_time_critical;

      /* get the service downtime depth */
      blue.macro_x[blue_h.MACRO_SERVICEDOWNTIME] = "" + svc.scheduled_downtime_depth;

      /* get the percent state change */
      blue.macro_x[blue_h.MACRO_SERVICEPERCENTCHANGE] = "" + svc.percent_state_change;

      current_time = utils.currentTimeInSeconds();
      duration = (current_time - svc.last_state_change);

      /* get the state duration in seconds */
      blue.macro_x[blue_h.MACRO_SERVICEDURATIONSEC] = "" + duration;

      /* get the state duration */
      days = (int) duration / 86400;
      duration -= (days * 86400);
      hours = (int) duration / 3600;
      duration -= (hours * 3600);
      minutes = (int) duration / 60;
      duration -= (minutes * 60);
      seconds = (int) duration;
      blue.macro_x[blue_h.MACRO_SERVICEDURATION] = days + "d " + hours + "h " + minutes + "m " + seconds + "s";

      /* find one servicegroup (there may be none or several) this service is associated with */
      for (objects_h.servicegroup temp_servicegroup : (ArrayList<objects_h.servicegroup>) objects.servicegroup_list)
      {
         if (objects.is_service_member_of_servicegroup(temp_servicegroup, svc) == common_h.TRUE)
         {
            /* get the servicegroup name */
            blue.macro_x[blue_h.MACRO_SERVICEGROUPNAME] = temp_servicegroup.group_name;

            /* get the servicegroup alias */
            blue.macro_x[blue_h.MACRO_SERVICEGROUPALIAS] = temp_servicegroup.alias;
            break;
         }
      }

      temp_serviceextinfo = objects.find_serviceextinfo(svc.host_name, svc.description);
      if (temp_serviceextinfo != null)
      {

         /* get the action url */
         blue.macro_x[blue_h.MACRO_SERVICEACTIONURL] = temp_serviceextinfo.action_url;

         /* get the notes url */
         blue.macro_x[blue_h.MACRO_SERVICENOTESURL] = temp_serviceextinfo.notes_url;

         /* get the notes */
         blue.macro_x[blue_h.MACRO_SERVICENOTES] = temp_serviceextinfo.notes;
      }

      /* get the date/time macros */
      grab_datetime_macros();

      blue.macro_x[blue_h.MACRO_SERVICEOUTPUT] = strip(blue.macro_x[blue_h.MACRO_SERVICEOUTPUT]);
      blue.macro_x[blue_h.MACRO_SERVICEPERFDATA] = strip(blue.macro_x[blue_h.MACRO_SERVICEPERFDATA]);
      blue.macro_x[blue_h.MACRO_SERVICECHECKCOMMAND] = strip(blue.macro_x[blue_h.MACRO_SERVICECHECKCOMMAND]);
      blue.macro_x[blue_h.MACRO_SERVICENOTES] = strip(blue.macro_x[blue_h.MACRO_SERVICENOTES]);

      /* notes and action URL macros may themselves contain macros, so process them... */
      blue.macro_x[blue_h.MACRO_SERVICEACTIONURL] = process_macros(blue.macro_x[blue_h.MACRO_SERVICEACTIONURL],
            blue_h.URL_ENCODE_MACRO_CHARS);
      blue.macro_x[blue_h.MACRO_SERVICENOTESURL] = process_macros(blue.macro_x[blue_h.MACRO_SERVICENOTESURL],
            blue_h.URL_ENCODE_MACRO_CHARS);

      logger.trace("exiting " + cn + ".grab_service_macros");
   }

   /* grab macros that are specific to a particular host */
   public static void grab_host_macros(objects_h.host hst)
   {
      objects_h.hostextinfo temp_hostextinfo;
      long current_time;
      long duration;
      int days;
      int hours;
      int minutes;
      int seconds;

      logger.trace("entering " + cn + ".grab_host_macros");

      /* get the host name */
      blue.macro_x[blue_h.MACRO_HOSTNAME] = hst.name;

      /* get the host alias */
      blue.macro_x[blue_h.MACRO_HOSTALIAS] = hst.alias;

      /* get the host address */
      blue.macro_x[blue_h.MACRO_HOSTADDRESS] = hst.address;

      /* get the host state */
      if (hst.current_state == blue_h.HOST_DOWN)
         blue.macro_x[blue_h.MACRO_HOSTSTATE] = "DOWN";
      else if (hst.current_state == blue_h.HOST_UNREACHABLE)
         blue.macro_x[blue_h.MACRO_HOSTSTATE] = "UNREACHABLE";
      else
         blue.macro_x[blue_h.MACRO_HOSTSTATE] = "UP";

      /* get the host state id */
      blue.macro_x[blue_h.MACRO_HOSTSTATEID] = "" + hst.current_state;

      /* grab the host check type */
      blue.macro_x[blue_h.MACRO_HOSTCHECKTYPE] = ((hst.check_type == common_h.HOST_CHECK_PASSIVE)
            ? "PASSIVE"
            : "ACTIVE");

      /* get the host state type macro */
      blue.macro_x[blue_h.MACRO_HOSTSTATETYPE] = ((hst.state_type == common_h.HARD_STATE) ? "HARD" : "SOFT");

      /* get the plugin output */
      blue.macro_x[blue_h.MACRO_HOSTOUTPUT] = hst.plugin_output;

      /* get the performance data */
      blue.macro_x[blue_h.MACRO_HOSTPERFDATA] = hst.perf_data;

      /* get the host check command */
      blue.macro_x[blue_h.MACRO_HOSTCHECKCOMMAND] = hst.host_check_command;

      /* get the host current attempt */
      blue.macro_x[blue_h.MACRO_HOSTATTEMPT] = "" + hst.current_attempt;

      /* get the host downtime depth */
      blue.macro_x[blue_h.MACRO_HOSTDOWNTIME] = "" + hst.scheduled_downtime_depth;

      /* get the percent state change */
      blue.macro_x[blue_h.MACRO_HOSTPERCENTCHANGE] = "" + hst.percent_state_change;

      current_time = utils.currentTimeInSeconds();
      duration = (current_time - hst.last_state_change);

      /* get the state duration in seconds */
      blue.macro_x[blue_h.MACRO_HOSTDURATIONSEC] = "" + duration;

      /* get the state duration */
      days = (int) duration / 86400;
      duration -= (days * 86400);
      hours = (int) duration / 3600;
      duration -= (hours * 3600);
      minutes = (int) duration / 60;
      duration -= (minutes * 60);
      seconds = (int) duration;
      blue.macro_x[blue_h.MACRO_HOSTDURATION] = "" + days + " " + hours + " " + minutes + " " + seconds;

      /* get the execution time macro */
      blue.macro_x[blue_h.MACRO_HOSTEXECUTIONTIME] = "" + hst.execution_time;

      /* get the latency macro */
      blue.macro_x[blue_h.MACRO_HOSTLATENCY] = "" + hst.latency;

      /* get the last check time macro */
      blue.macro_x[blue_h.MACRO_LASTHOSTCHECK] = "" + hst.last_check;

      /* get the last state change time macro */
      blue.macro_x[blue_h.MACRO_LASTHOSTSTATECHANGE] = "" + hst.last_state_change;

      /* get the last time up macro */
      blue.macro_x[blue_h.MACRO_LASTHOSTUP] = "" + hst.last_time_up;

      /* get the last time down macro */
      blue.macro_x[blue_h.MACRO_LASTHOSTDOWN] = "" + hst.last_time_down;

      /* get the last time unreachable macro */
      blue.macro_x[blue_h.MACRO_LASTHOSTUNREACHABLE] = "" + hst.last_time_unreachable;

      /* find one hostgroup (there may be none or several) this host is associated with */
      for (objects_h.hostgroup temp_hostgroup : (ArrayList<objects_h.hostgroup>) objects.hostgroup_list)
      {
         if (objects.is_host_member_of_hostgroup(temp_hostgroup, hst) == common_h.TRUE)
         {
            /* get the hostgroup name */
            blue.macro_x[blue_h.MACRO_HOSTGROUPNAME] = temp_hostgroup.group_name;

            /* get the hostgroup alias */
            blue.macro_x[blue_h.MACRO_HOSTGROUPALIAS] = temp_hostgroup.alias;
            break;
         }
      }

      temp_hostextinfo = objects.find_hostextinfo(hst.name);
      if (temp_hostextinfo != null)
      {

         /* get the action url */
         blue.macro_x[blue_h.MACRO_HOSTACTIONURL] = temp_hostextinfo.action_url;

         /* get the notes url */
         blue.macro_x[blue_h.MACRO_HOSTNOTESURL] = temp_hostextinfo.notes_url;

         /* get the notes */
         blue.macro_x[blue_h.MACRO_HOSTNOTES] = temp_hostextinfo.notes;
      }

      /* get the date/time macros */
      grab_datetime_macros();

      blue.macro_x[blue_h.MACRO_HOSTOUTPUT] = strip(blue.macro_x[blue_h.MACRO_HOSTOUTPUT]);
      blue.macro_x[blue_h.MACRO_HOSTPERFDATA] = strip(blue.macro_x[blue_h.MACRO_HOSTPERFDATA]);
      blue.macro_x[blue_h.MACRO_HOSTCHECKCOMMAND] = strip(blue.macro_x[blue_h.MACRO_HOSTCHECKCOMMAND]);
      blue.macro_x[blue_h.MACRO_HOSTNOTES] = strip(blue.macro_x[blue_h.MACRO_HOSTNOTES]);

      /* notes and action URL macros may themselves contain macros, so process them... */
      blue.macro_x[blue_h.MACRO_HOSTACTIONURL] = process_macros(blue.macro_x[blue_h.MACRO_HOSTACTIONURL],
            blue_h.URL_ENCODE_MACRO_CHARS);
      blue.macro_x[blue_h.MACRO_HOSTNOTESURL] = process_macros(blue.macro_x[blue_h.MACRO_HOSTNOTESURL],
            blue_h.URL_ENCODE_MACRO_CHARS);

      logger.trace("exiting " + cn + ".grab_host_macros");
   }

   /* grab an on-demand host or service macro */
   public static int grab_on_demand_macro(String str)
   {
      String macro = null;
      StringBuffer result_buffer = new StringBuffer();
      objects_h.host temp_host;
      objects_h.hostgroup temp_hostgroup;
      objects_h.hostgroupmember temp_hostgroupmember;
      objects_h.service temp_service;
      objects_h.servicegroup temp_servicegroup;
      objects_h.servicegroupmember temp_servicegroupmember;
      int return_val = common_h.ERROR;

      logger.trace("entering " + cn + ".grab_on_demand_macro");

      /* clear the on-demand macro */
      blue.macro_ondemand = null;

      /* get the host name */
      String[] split = macro.split("\\:");

      /* process the macro */
      if (macro.indexOf("HOST") >= 0)
      {

         /* process a host macro */
         if (split.length == 1)
         {
            temp_host = objects.find_host(split[0]);
            return_val = grab_on_demand_host_macro(temp_host, macro);
         }

         /* process a host macro containing a hostgroup */
         else
         {
            temp_hostgroup = objects.find_hostgroup(split[0]);
            if (temp_hostgroup == null)
               return common_h.ERROR;

            /* process each host in the hostgroup */
            if (temp_hostgroup.members == null || temp_hostgroup.members.size() == 0)
            {
               blue.macro_ondemand = "";
               return common_h.OK;
            }

            return_val = common_h.OK; /* start off assuming there's no error */
            ListIterator iterator = temp_hostgroup.members.listIterator();
            temp_hostgroupmember = (objects_h.hostgroupmember) iterator.next();
            while (true)
            {
               temp_host = objects.find_host(temp_hostgroupmember.host_name);
               if (grab_on_demand_host_macro(temp_host, macro) == common_h.OK)
               {
                  result_buffer.append(blue.macro_ondemand);
                  if (!iterator.hasNext())
                     break;
                  temp_hostgroupmember = (objects_h.hostgroupmember) iterator.next();
                  result_buffer.append(split[1]);
               }
               else
               {
                  return_val = common_h.ERROR;
                  if (!iterator.hasNext())
                     break;
                  temp_hostgroupmember = (objects_h.hostgroupmember) iterator.next();
               }

               blue.macro_ondemand = null;
            }

            blue.macro_ondemand = result_buffer.toString();
         }
      }

      else if (macro.indexOf("SERVICE") >= 0)
      {

         /* second args will either be service description or delimiter */
         if (split.length == 1)
            return common_h.ERROR;

         /* process a service macro */
         temp_service = objects.find_service(split[0], split[1]);

         if (temp_service != null)
            return_val = grab_on_demand_service_macro(temp_service, macro);

         /* process a service macro containing a servicegroup */
         else
         {
            temp_servicegroup = objects.find_servicegroup(split[0]);
            if (temp_servicegroup == null)
               return common_h.ERROR;

            /* process each service in the servicegroup */
            if (temp_servicegroup.members == null || temp_servicegroup.members.size() == 0)
            {
               blue.macro_ondemand = "";
               return common_h.OK;
            }

            return_val = common_h.OK; /* start off assuming there's no error */
            result_buffer = new StringBuffer();
            ListIterator iterator = temp_servicegroup.members.listIterator();
            temp_servicegroupmember = (objects_h.servicegroupmember) iterator.next();

            while (true)
            {
               temp_service = objects.find_service(temp_servicegroupmember.host_name,
                     temp_servicegroupmember.service_description);
               if (grab_on_demand_service_macro(temp_service, macro) == common_h.OK)
               {
                  result_buffer.append(blue.macro_ondemand);
                  if (!iterator.hasNext())
                     break;

                  temp_servicegroupmember = (objects_h.servicegroupmember) iterator.next();
                  result_buffer.append(split[1]);
               }
               else
               {
                  return_val = common_h.ERROR;
                  if (!iterator.hasNext())
                     break;
                  temp_servicegroupmember = (objects_h.servicegroupmember) iterator.next();
               }

               blue.macro_ondemand = null;
            }

            blue.macro_ondemand = result_buffer.toString();
         }
      }

      else
         return_val = common_h.ERROR;

      logger.trace("exiting " + cn + ".grab_on_demand_macro");
      return return_val;
   }

   /* grab an on-demand host macro */
   public static int grab_on_demand_host_macro(objects_h.host hst, String macro)
   {
      objects_h.hostgroup temp_hostgroup = null;
      objects_h.hostextinfo temp_hostextinfo;
      long current_time;
      long duration;
      int days;
      int hours;
      int minutes;
      int seconds;

      logger.trace("entering " + cn + ".grab_on_demand_host_macro");

      if (hst == null || macro == null)
         return common_h.ERROR;

      /* initialize the macro */
      blue.macro_ondemand = null;

      current_time = utils.currentTimeInSeconds();
      duration = (current_time - hst.last_state_change);

      /* find one hostgroup (there may be none or several) this host is associated with */
      for (ListIterator iter = objects.hostgroup_list.listIterator(); iter.hasNext();)
      {
         temp_hostgroup = (objects_h.hostgroup) iter.next();
         if (objects.is_host_member_of_hostgroup(temp_hostgroup, hst) == common_h.TRUE)
            break;
      }

      /* get the host alias */
      if (macro.equals("HOSTALIAS"))
         blue.macro_ondemand = hst.alias;

      /* get the host address */
      else if (macro.equals("HOSTADDRESS"))
         blue.macro_ondemand = hst.address;

      /* get the host state */
      else if (macro.equals("HOSTSTATE"))
      {
         if (hst.current_state == blue_h.HOST_DOWN)
            blue.macro_ondemand = "DOWN";
         else if (hst.current_state == blue_h.HOST_UNREACHABLE)
            blue.macro_ondemand = "UNREACHABLE";
         else
            blue.macro_ondemand = "UP";
      }

      /* get the host state id */
      else if (macro.equals("HOSTSTATEID"))
      {
         blue.macro_ondemand = "" + hst.current_state;
      }

      /* grab the host check type */
      else if (macro.equals("HOSTCHECKTYPE"))
      {
         blue.macro_ondemand = ((hst.check_type == common_h.HOST_CHECK_PASSIVE) ? "PASSIVE" : "ACTIVE");
      }

      /* get the host state type macro */
      else if (macro.equals("HOSTSTATETYPE"))
      {
         blue.macro_ondemand = ((hst.state_type == common_h.HARD_STATE) ? "HARD" : "SOFT");
      }

      /* get the plugin output */
      else if (macro.equals("HOSTOUTPUT"))
      {
         blue.macro_ondemand = hst.plugin_output;
      }

      /* get the performance data */
      else if (macro.equals("HOSTPERFDATA"))
      {
         blue.macro_ondemand = hst.perf_data;
      }

      /* get the host current attempt */
      else if (macro.equals("HOSTATTEMPT"))
      {
         blue.macro_ondemand = "" + hst.current_attempt;
      }

      /* get the host downtime depth */
      else if (macro.equals("HOSTDOWNTIME"))
      {
         blue.macro_ondemand = "" + hst.scheduled_downtime_depth;
      }

      /* get the percent state change */
      else if (macro.equals("HOSTPERCENTCHANGE"))
      {
         blue.macro_ondemand = "" + hst.percent_state_change;
      }

      /* get the state duration in seconds */
      else if (macro.equals("HOSTDURATIONSEC"))
      {
         blue.macro_ondemand = "" + duration;
      }

      /* get the state duration */
      else if (macro.equals("HOSTDURATION"))
      {
         days = (int) duration / 86400;
         duration -= (days * 86400);
         hours = (int) duration / 3600;
         duration -= (hours * 3600);
         minutes = (int) duration / 60;
         duration -= (minutes * 60);
         seconds = (int) duration;
         //	    macro_ondemand = "%dd %dh %dm %ds",days,hours,minutes,seconds);
         blue.macro_ondemand = days + " " + hours + " " + minutes + " " + seconds;
      }

      /* get the execution time macro */
      else if (macro.equals("HOSTEXECUTIONTIME"))
      {
         blue.macro_ondemand = "" + hst.execution_time;
      }

      /* get the latency macro */
      else if (macro.equals("HOSTLATENCY"))
      {
         blue.macro_ondemand = "" + hst.latency;
      }

      /* get the last check time macro */
      else if (macro.equals("LASTHOSTCHECK"))
      {
         blue.macro_ondemand = "" + hst.last_check;
      }

      /* get the last state change time macro */
      else if (macro.equals("LASTHOSTSTATECHANGE"))
      {
         blue.macro_ondemand = "" + hst.last_state_change;
      }

      /* get the last time up macro */
      else if (macro.equals("LASTHOSTUP"))
      {
         blue.macro_ondemand = "" + hst.last_time_up;
      }

      /* get the last time down macro */
      else if (macro.equals("LASTHOSTDOWN"))
      {
         blue.macro_ondemand = "" + hst.last_time_down;
      }

      /* get the last time unreachable macro */
      else if (macro.equals("LASTHOSTUNREACHABLE"))
      {
         blue.macro_ondemand = "" + hst.last_time_unreachable;
      }

      /* get the hostgroup name */
      else if (macro.equals("HOSTGROUPNAME") && temp_hostgroup != null)
      {
         blue.macro_ondemand = temp_hostgroup.group_name;
      }

      /* get the hostgroup alias */
      else if (macro.equals("HOSTGROUPALIAS") && temp_hostgroup != null)
      {
         blue.macro_ondemand = temp_hostgroup.alias;
      }

      /* extended info */
      else if (macro.equals("HOSTACTIONURL") || macro.equals("HOSTNOTESURL") || macro.equals("HOSTNOTES"))
      {

         /* find the extended info entry */
         temp_hostextinfo = objects.find_hostextinfo(hst.name);
         if (temp_hostextinfo != null)
         {

            /* action url */
            if (macro.equals("HOSTACTIONURL"))
            {

               /* action URL macros may themselves contain macros, so process them... */
               if (temp_hostextinfo.action_url != null)
               {
                  //                TODO validate that the string is passed back if no encoding occurs.
                  //	                blue.macro_ondemand=temp_hostextinfo.action_url;
                  blue.macro_ondemand = process_macros(temp_hostextinfo.action_url, blue_h.URL_ENCODE_MACRO_CHARS);

               }
            }

            /* notes url */
            if (macro.equals("HOSTNOTESURL"))
            {

               /* action URL macros may themselves contain macros, so process them... */
               if (temp_hostextinfo.notes_url != null)
               {
                  //                TODO validate that the string is passed back if no encoding occurs.
                  //	                blue.macro_ondemand=temp_hostextinfo.notes_url;
                  blue.macro_ondemand = process_macros(temp_hostextinfo.notes_url, blue_h.URL_ENCODE_MACRO_CHARS);
               }
            }

            /* notes */
            if (macro.equals("HOSTNOTES"))
            {
               blue.macro_ondemand = temp_hostextinfo.notes;
            }
         }
      }

      else
         return common_h.ERROR;

      logger.trace("exiting " + cn + ".grab_on_demand_host_macro");

      return common_h.OK;
   }

   /* grab an on-demand service macro */
   public static int grab_on_demand_service_macro(objects_h.service svc, String macro)
   {
      objects_h.servicegroup temp_servicegroup = null;
      objects_h.serviceextinfo temp_serviceextinfo;
      long current_time;
      long duration;
      int days;
      int hours;
      int minutes;
      int seconds;

      logger.trace("entering " + cn + ".grab_on_demand_service_macro");

      if (svc == null || macro == null)
         return common_h.ERROR;

      /* initialize the macro */
      blue.macro_ondemand = null;

      current_time = utils.currentTimeInSeconds();
      duration = (current_time - svc.last_state_change);

      /* find one servicegroup (there may be none or several) this service is associated with */
      for (ListIterator iter = objects.servicegroup_list.listIterator(); iter.hasNext();)
      {
         temp_servicegroup = (objects_h.servicegroup) iter.next();
         if (objects.is_service_member_of_servicegroup(temp_servicegroup, svc) == common_h.TRUE)
            break;
      }

      /* get the plugin output */
      if (macro.equals("SERVICEOUTPUT"))
      {
         blue.macro_ondemand = svc.plugin_output;
      }

      /* get the performance data */
      else if (macro.equals("SERVICEPERFDATA"))
      {
         blue.macro_ondemand = svc.perf_data;
      }

      /* grab the servuce check type */
      else if (macro.equals("SERVICECHECKTYPE"))
      {
         blue.macro_ondemand = ((svc.check_type == common_h.SERVICE_CHECK_PASSIVE) ? "PASSIVE" : "ACTIVE");
      }

      /* grab the service state type macro (this is usually overridden later on) */
      else if (macro.equals("SERVICESTATETYPE"))
      {
         blue.macro_ondemand = ((svc.state_type == common_h.HARD_STATE) ? "HARD" : "SOFT");
      }

      /* get the service state */
      else if (macro.equals("SERVICESTATE"))
      {
         if (svc.current_state == blue_h.STATE_OK)
            blue.macro_ondemand = "OK";
         else if (svc.current_state == blue_h.STATE_WARNING)
            blue.macro_ondemand = "WARNING";
         else if (svc.current_state == blue_h.STATE_CRITICAL)
            blue.macro_ondemand = "CRITICAL";
         else
            blue.macro_ondemand = "UNKNOWN";
      }

      /* get the service state id */
      else if (macro.equals("SERVICESTATEID"))
      {
         blue.macro_ondemand = "" + svc.current_state;
      }

      /* get the current service check attempt macro */
      else if (macro.equals("SERVICEATTEMPT"))
      {
         blue.macro_ondemand = "" + svc.current_attempt;
      }

      /* get the execution time macro */
      else if (macro.equals("SERVICEEXECUTIONTIME"))
      {
         blue.macro_ondemand = "" + svc.execution_time;
      }

      /* get the latency macro */
      else if (macro.equals("SERVICELATENCY"))
      {
         blue.macro_ondemand = "" + svc.latency;
      }

      /* get the last check time macro */
      else if (macro.equals("LASTSERVICECHECK"))
      {
         blue.macro_ondemand = "" + svc.last_check;
      }

      /* get the last state change time macro */
      else if (macro.equals("LASTSERVICESTATECHANGE"))
      {
         blue.macro_ondemand = "" + svc.last_state_change;
      }

      /* get the last time ok macro */
      else if (macro.equals("LASTSERVICEOK"))
      {
         blue.macro_ondemand = "" + svc.last_time_ok;
      }

      /* get the last time warning macro */
      else if (macro.equals("LASTSERVICEWARNING"))
      {
         blue.macro_ondemand = "" + svc.last_time_warning;
      }

      /* get the last time unknown macro */
      else if (macro.equals("LASTSERVICEUNKNOWN"))
      {
         blue.macro_ondemand = "" + svc.last_time_unknown;
      }

      /* get the last time critical macro */
      else if (macro.equals("LASTSERVICECRITICAL"))
      {
         blue.macro_ondemand = "" + svc.last_time_critical;
      }

      /* get the service downtime depth */
      else if (macro.equals("SERVICEDOWNTIME"))
      {
         blue.macro_ondemand = "" + svc.scheduled_downtime_depth;
      }

      /* get the percent state change */
      else if (macro.equals("SERVICEPERCENTCHANGE"))
      {
         blue.macro_ondemand = "" + svc.percent_state_change;
      }

      /* get the state duration in seconds */
      else if (macro.equals("SERVICEDURATIONSEC"))
      {
         blue.macro_ondemand = "" + duration;
      }

      /* get the state duration */
      else if (macro.equals("SERVICEDURATION"))
      {
         days = (int) duration / 86400;
         duration -= (days * 86400);
         hours = (int) duration / 3600;
         duration -= (hours * 3600);
         minutes = (int) duration / 60;
         duration -= (minutes * 60);
         seconds = (int) duration;
         blue.macro_ondemand = "" + days + " " + hours + " " + minutes + "  " + seconds;
      }

      /* get the servicegroup name */
      else if (macro.equals("SERVICEGROUPNAME") && temp_servicegroup != null)
      {
         blue.macro_ondemand = temp_servicegroup.group_name;
      }

      /* get the servicegroup alias */
      else if (macro.equals("SERVICEGROUPALIAS") && temp_servicegroup != null)
      {
         blue.macro_ondemand = temp_servicegroup.alias;
      }

      /* extended info */
      else if (macro.equals("SERVICEACTIONURL") || macro.equals("SERVICENOTESURL") || macro.equals("SERVICENOTES"))
      {

         /* find the extended info entry */
         temp_serviceextinfo = objects.find_serviceextinfo(svc.host_name, svc.description);
         if (temp_serviceextinfo != null)
         {

            /* action url */
            if (macro.equals("SERVICEACTIONURL"))
            {

               /* action URL macros may themselves contain macros, so process them... */
               blue.macro_ondemand = process_macros(temp_serviceextinfo.action_url, blue_h.URL_ENCODE_MACRO_CHARS);
            }

            /* notes url */
            if (macro.equals("SERVICENOTESURL"))
            {

               /* action URL macros may themselves contain macros, so process them... */
               blue.macro_ondemand = process_macros(temp_serviceextinfo.notes_url, blue_h.URL_ENCODE_MACRO_CHARS);
            }

            /* notes */
            if (macro.equals("SERVICENOTES"))
            {
               blue.macro_ondemand = temp_serviceextinfo.notes;
            }
         }
      }

      else
         return common_h.ERROR;

      logger.trace("exiting " + cn + ".grab_on_demand_service_macro() end\n");

      return common_h.OK;
   }

   /* grab macros that are specific to a particular contact */
   public static int grab_contact_macros(objects_h.contact cntct)
   {

      logger.trace("entering " + cn + ".grab_contact_macros");

      /* get the name */
      blue.macro_x[blue_h.MACRO_CONTACTNAME] = cntct.name;

      /* get the alias */
      blue.macro_x[blue_h.MACRO_CONTACTALIAS] = cntct.alias;

      /* get the email address */
      blue.macro_x[blue_h.MACRO_CONTACTEMAIL] = cntct.email;

      /* get the pager number */
      blue.macro_x[blue_h.MACRO_CONTACTPAGER] = cntct.pager;

      /* get misc contact addresses */
      for (int x = 0; x < objects_h.MAX_CONTACT_ADDRESSES && x < cntct.address.length; x++)
      {
         blue.macro_contactaddress[x] = cntct.address[x];
      }

      /* get the date/time macros */
      grab_datetime_macros();

      logger.trace("exiting " + cn + ".grab_contact_macros");

      return common_h.OK;
   }

   /* grab summary macros (filtered for a specific contact) */
   public static void grab_summary_macros(objects_h.contact temp_contact)
   {
      boolean authorized = true;
      boolean problem = true;

      int hosts_up = 0;
      int hosts_down = 0;
      int hosts_unreachable = 0;
      int hosts_down_unhandled = 0;
      int hosts_unreachable_unhandled = 0;
      int host_problems = 0;
      int host_problems_unhandled = 0;

      int services_ok = 0;
      int services_warning = 0;
      int services_unknown = 0;
      int services_critical = 0;
      int services_warning_unhandled = 0;
      int services_unknown_unhandled = 0;
      int services_critical_unhandled = 0;
      int service_problems = 0;
      int service_problems_unhandled = 0;

      logger.trace("entering " + cn + ".grab_summary_macros");

      /* get host totals */
      for (objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list)
      {

         /* filter totals based on contact if necessary */
         if (temp_contact != null)
            authorized = objects.is_contact_for_host(temp_host, temp_contact);

         if (authorized == true)
         {
            problem = true;

            if (temp_host.current_state == blue_h.HOST_UP && temp_host.has_been_checked == common_h.TRUE)
               hosts_up++;
            else if (temp_host.current_state == blue_h.HOST_DOWN)
            {
               if (temp_host.scheduled_downtime_depth > 0)
                  problem = false;
               if (temp_host.problem_has_been_acknowledged == common_h.TRUE)
                  problem = false;
               if (temp_host.checks_enabled == common_h.FALSE)
                  problem = false;
               if (problem == true)
                  hosts_down_unhandled++;
               hosts_down++;
            }
            else if (temp_host.current_state == blue_h.HOST_UNREACHABLE)
            {
               if (temp_host.scheduled_downtime_depth > 0)
                  problem = false;
               if (temp_host.problem_has_been_acknowledged == common_h.TRUE)
                  problem = false;
               if (temp_host.checks_enabled == common_h.FALSE)
                  problem = false;
               if (problem == true)
                  hosts_down_unhandled++;
               hosts_unreachable++;
            }
         }
      }

      host_problems = hosts_down + hosts_unreachable;
      host_problems_unhandled = hosts_down_unhandled + hosts_unreachable_unhandled;

      /* get service totals */
      for (objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list)
      {

         /* filter totals based on contact if necessary */
         if (temp_contact != null)
            authorized = objects.is_contact_for_service(temp_service, temp_contact);

         if (authorized == true)
         {
            problem = true;

            if (temp_service.current_state == blue_h.STATE_OK && temp_service.has_been_checked == common_h.TRUE)
               services_ok++;
            else if (temp_service.current_state == blue_h.STATE_WARNING)
            {
               objects_h.host temp_host = objects.find_host(temp_service.host_name);
               if (temp_host != null
                     && (temp_host.current_state == blue_h.HOST_DOWN || temp_host.current_state == blue_h.HOST_UNREACHABLE))
                  problem = false;
               if (temp_service.scheduled_downtime_depth > 0)
                  problem = false;
               if (temp_service.problem_has_been_acknowledged == common_h.TRUE)
                  problem = false;
               if (temp_service.checks_enabled == common_h.FALSE)
                  problem = false;
               if (problem == true)
                  services_warning_unhandled++;
               services_warning++;
            }
            else if (temp_service.current_state == blue_h.STATE_UNKNOWN)
            {
               objects_h.host temp_host = objects.find_host(temp_service.host_name);
               if (temp_host != null
                     && (temp_host.current_state == blue_h.HOST_DOWN || temp_host.current_state == blue_h.HOST_UNREACHABLE))
                  problem = false;
               if (temp_service.scheduled_downtime_depth > 0)
                  problem = false;
               if (temp_service.problem_has_been_acknowledged == common_h.TRUE)
                  problem = false;
               if (temp_service.checks_enabled == common_h.FALSE)
                  problem = false;
               if (problem == true)
                  services_unknown_unhandled++;
               services_unknown++;
            }
            else if (temp_service.current_state == blue_h.STATE_CRITICAL)
            {
               objects_h.host temp_host = objects.find_host(temp_service.host_name);
               if (temp_host != null
                     && (temp_host.current_state == blue_h.HOST_DOWN || temp_host.current_state == blue_h.HOST_UNREACHABLE))
                  problem = false;
               if (temp_service.scheduled_downtime_depth > 0)
                  problem = false;
               if (temp_service.problem_has_been_acknowledged == common_h.TRUE)
                  problem = false;
               if (temp_service.checks_enabled == common_h.FALSE)
                  problem = false;
               if (problem == true)
                  services_critical_unhandled++;
               services_critical++;
            }
         }
      }

      service_problems = services_warning + services_critical + services_unknown;
      service_problems_unhandled = services_warning_unhandled + services_critical_unhandled
            + services_unknown_unhandled;

      /* get total hosts up */
      blue.macro_x[blue_h.MACRO_TOTALHOSTSUP] = "" + hosts_up;

      /* get total hosts down */
      blue.macro_x[blue_h.MACRO_TOTALHOSTSDOWN] = "" + hosts_down;

      /* get total hosts unreachable */
      blue.macro_x[blue_h.MACRO_TOTALHOSTSUNREACHABLE] = "" + hosts_unreachable;

      /* get total unhandled hosts down */
      blue.macro_x[blue_h.MACRO_TOTALHOSTSDOWNUNHANDLED] = "" + hosts_down_unhandled;

      /* get total unhandled hosts unreachable */
      blue.macro_x[blue_h.MACRO_TOTALHOSTSUNREACHABLEUNHANDLED] = "" + hosts_unreachable_unhandled;

      /* get total host problems */
      blue.macro_x[blue_h.MACRO_TOTALHOSTPROBLEMS] = "" + host_problems;

      /* get total unhandled host problems */
      blue.macro_x[blue_h.MACRO_TOTALHOSTPROBLEMSUNHANDLED] = "" + host_problems_unhandled;

      /* get total services ok */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESOK] = "" + services_ok;

      /* get total services warning */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESWARNING] = "" + services_warning;

      /* get total services critical */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESCRITICAL] = "" + services_critical;

      /* get total services unknown */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESUNKNOWN] = "" + services_unknown;

      /* get total unhandled services warning */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESWARNINGUNHANDLED] = "" + services_warning_unhandled;

      /* get total unhandled services critical */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESCRITICALUNHANDLED] = "" + services_critical_unhandled;

      /* get total unhandled services unknown */
      blue.macro_x[blue_h.MACRO_TOTALSERVICESUNKNOWNUNHANDLED] = "" + services_unknown_unhandled;

      /* get total service problems */
      blue.macro_x[blue_h.MACRO_TOTALSERVICEPROBLEMS] = "" + service_problems;

      /* get total unhandled service problems */
      blue.macro_x[blue_h.MACRO_TOTALSERVICEPROBLEMSUNHANDLED] = "" + service_problems_unhandled;

      logger.trace("exiting " + cn + ".grab_summary_macros() end\n");
   }

   /* updates date/time macros */
   public static void grab_datetime_macros()
   {

      logger.trace("entering " + cn + ".grab_datetime_macros");

      /* get the current time */
      long t = utils.currentTimeInSeconds();

      /* get the current date/time (long format macro) */
      blue.macro_x[blue_h.MACRO_LONGDATETIME] = get_datetime_string(t*1000, common_h.LONG_DATE_TIME);

      /* get the current date/time (short format macro) */
      blue.macro_x[blue_h.MACRO_SHORTDATETIME] = get_datetime_string(t*1000, common_h.SHORT_DATE_TIME);

      /* get the short format date macro */
      blue.macro_x[blue_h.MACRO_DATE] = get_datetime_string(t*1000, common_h.SHORT_DATE);

      /* get the short format time macro */
      blue.macro_x[blue_h.MACRO_TIME] = get_datetime_string(t*1000, common_h.SHORT_TIME);

      /* get the time_t format time macro */
      blue.macro_x[blue_h.MACRO_TIMET] = "" + t;

      logger.trace("exiting " + cn + ".grab_datetime_macros");
   }

   /* clear argv macros - used in commands */
   public static int clear_argv_macros()
   {
      logger.trace("exiting " + cn + ".clear_argv_macros");

      /* command argument macros */
      for (int x = 0; x < blue_h.MAX_COMMAND_ARGUMENTS; x++)
      {
         blue.macro_argv[x] = null;
      }

      logger.trace("entering " + cn + ".clear_argv_macros");
      return common_h.OK;
   }

   /* clear all macros that are not "constant" (i.e. they change throughout the course of monitoring) */
   public static void clear_volatile_macros()
   {
      logger.trace("entering " + cn + ".clear_volatile_macros");

      for (int x = 0; x < blue_h.MACRO_X_COUNT; x++)
      {
         switch (x)
         {
            case blue_h.MACRO_ADMINEMAIL :
            case blue_h.MACRO_ADMINPAGER :
            case blue_h.MACRO_MAINCONFIGFILE :
            case blue_h.MACRO_STATUSDATAFILE :
            case blue_h.MACRO_COMMENTDATAFILE :
            case blue_h.MACRO_DOWNTIMEDATAFILE :
            case blue_h.MACRO_RETENTIONDATAFILE :
            case blue_h.MACRO_OBJECTCACHEFILE :
            case blue_h.MACRO_TEMPFILE :
            case blue_h.MACRO_LOGFILE :
            case blue_h.MACRO_RESOURCEFILE :
            case blue_h.MACRO_COMMANDFILE :
            case blue_h.MACRO_HOSTPERFDATAFILE :
            case blue_h.MACRO_SERVICEPERFDATAFILE :
            case blue_h.MACRO_PROCESSSTARTTIME :
               break;
            default :
               blue.macro_x[x] = null;
               break;
         }
      }

      /* command argument macros */
      for (int x = 0; x < blue_h.MAX_COMMAND_ARGUMENTS; x++)
      {
         blue.macro_argv[x] = null;
      }

      /* contact address macros */
      for (int x = 0; x < objects_h.MAX_CONTACT_ADDRESSES; x++)
      {
         blue.macro_contactaddress[x] = null;
      }

      /* clear on-demand macro */
      blue.macro_ondemand = null;

      clear_argv_macros();

      logger.trace("exiting " + cn + ".clear_volatile_macros");
   }

   //
   //
   ///* clear macros that are constant (i.e. they do NOT change during monitoring) */
   //int clear_nonvolatile_macros(void){
   //	int x=0;
   //
   // logger.trace( "entering " + cn + ".clear_nonvolatile_macros" );
   //	
   //	for(x=0;x<MACRO_X_COUNT;x++){
   //		switch(x){
   //		case MACRO_ADMINEMAIL:
   //		case MACRO_ADMINPAGER:
   //		case MACRO_MAINCONFIGFILE:
   //		case MACRO_STATUSDATAFILE:
   //		case MACRO_COMMENTDATAFILE:
   //		case MACRO_DOWNTIMEDATAFILE:
   //		case MACRO_RETENTIONDATAFILE:
   //		case MACRO_OBJECTCACHEFILE:
   //		case MACRO_TEMPFILE:
   //		case MACRO_LOGFILE:
   //		case MACRO_RESOURCEFILE:
   //		case MACRO_COMMANDFILE:
   //		case MACRO_HOSTPERFDATAFILE:
   //		case MACRO_SERVICEPERFDATAFILE:
   //		case MACRO_PROCESSSTARTTIME:
   //			if(macro_x[x]!=null){
   //				free(macro_x[x]);
   //				macro_x[x]=null;
   //			        }
   //			break;
   //		default:
   //			break;
   //		        }
   //	        }
   //
   // logger.trace( "exiting " + cn + ".clear_nonvolatile_macros" );
   //
   //	return OK;
   //        }
   //
   //
   /* initializes the names of macros */
   public static int init_macrox_names()
   {
      logger.trace("entering " + cn + ".init_macrox_names()");

      /* initialize macro names */
      for (int x = 0; x < blue_h.MACRO_X_COUNT; x++)
         blue.macro_x_names[x] = null;

      /* initialize each macro name */
      add_macrox_name(blue_h.MACRO_HOSTNAME, "HOSTNAME");
      add_macrox_name(blue_h.MACRO_HOSTALIAS, "HOSTALIAS");
      add_macrox_name(blue_h.MACRO_HOSTADDRESS, "HOSTADDRESS");
      add_macrox_name(blue_h.MACRO_SERVICEDESC, "SERVICEDESC");
      add_macrox_name(blue_h.MACRO_SERVICESTATE, "SERVICESTATE");
      add_macrox_name(blue_h.MACRO_SERVICESTATEID, "SERVICESTATEID");
      add_macrox_name(blue_h.MACRO_SERVICEATTEMPT, "SERVICEATTEMPT");
      add_macrox_name(blue_h.MACRO_LONGDATETIME, "LONGDATETIME");
      add_macrox_name(blue_h.MACRO_SHORTDATETIME, "SHORTDATETIME");
      add_macrox_name(blue_h.MACRO_DATE, "DATE");
      add_macrox_name(blue_h.MACRO_TIME, "TIME");
      add_macrox_name(blue_h.MACRO_TIMET, "TIMET");
      add_macrox_name(blue_h.MACRO_LASTHOSTCHECK, "LASTHOSTCHECK");
      add_macrox_name(blue_h.MACRO_LASTSERVICECHECK, "LASTSERVICECHECK");
      add_macrox_name(blue_h.MACRO_LASTHOSTSTATECHANGE, "LASTHOSTSTATECHANGE");
      add_macrox_name(blue_h.MACRO_LASTSERVICESTATECHANGE, "LASTSERVICESTATECHANGE");
      add_macrox_name(blue_h.MACRO_HOSTOUTPUT, "HOSTOUTPUT");
      add_macrox_name(blue_h.MACRO_SERVICEOUTPUT, "SERVICEOUTPUT");
      add_macrox_name(blue_h.MACRO_HOSTPERFDATA, "HOSTPERFDATA");
      add_macrox_name(blue_h.MACRO_SERVICEPERFDATA, "SERVICEPERFDATA");
      add_macrox_name(blue_h.MACRO_CONTACTNAME, "CONTACTNAME");
      add_macrox_name(blue_h.MACRO_CONTACTALIAS, "CONTACTALIAS");
      add_macrox_name(blue_h.MACRO_CONTACTEMAIL, "CONTACTEMAIL");
      add_macrox_name(blue_h.MACRO_CONTACTPAGER, "CONTACTPAGER");
      add_macrox_name(blue_h.MACRO_ADMINEMAIL, "ADMINEMAIL");
      add_macrox_name(blue_h.MACRO_ADMINPAGER, "ADMINPAGER");
      add_macrox_name(blue_h.MACRO_HOSTSTATE, "HOSTSTATE");
      add_macrox_name(blue_h.MACRO_HOSTSTATEID, "HOSTSTATEID");
      add_macrox_name(blue_h.MACRO_HOSTATTEMPT, "HOSTATTEMPT");
      add_macrox_name(blue_h.MACRO_NOTIFICATIONTYPE, "NOTIFICATIONTYPE");
      add_macrox_name(blue_h.MACRO_NOTIFICATIONNUMBER, "NOTIFICATIONNUMBER");
      add_macrox_name(blue_h.MACRO_HOSTEXECUTIONTIME, "HOSTEXECUTIONTIME");
      add_macrox_name(blue_h.MACRO_SERVICEEXECUTIONTIME, "SERVICEEXECUTIONTIME");
      add_macrox_name(blue_h.MACRO_HOSTLATENCY, "HOSTLATENCY");
      add_macrox_name(blue_h.MACRO_SERVICELATENCY, "SERVICELATENCY");
      add_macrox_name(blue_h.MACRO_HOSTDURATION, "HOSTDURATION");
      add_macrox_name(blue_h.MACRO_SERVICEDURATION, "SERVICEDURATION");
      add_macrox_name(blue_h.MACRO_HOSTDURATIONSEC, "HOSTDURATIONSEC");
      add_macrox_name(blue_h.MACRO_SERVICEDURATIONSEC, "SERVICEDURATIONSEC");
      add_macrox_name(blue_h.MACRO_HOSTDOWNTIME, "HOSTDOWNTIME");
      add_macrox_name(blue_h.MACRO_SERVICEDOWNTIME, "SERVICEDOWNTIME");
      add_macrox_name(blue_h.MACRO_HOSTSTATETYPE, "HOSTSTATETYPE");
      add_macrox_name(blue_h.MACRO_SERVICESTATETYPE, "SERVICESTATETYPE");
      add_macrox_name(blue_h.MACRO_HOSTPERCENTCHANGE, "HOSTPERCENTCHANGE");
      add_macrox_name(blue_h.MACRO_SERVICEPERCENTCHANGE, "SERVICEPERCENTCHANGE");
      add_macrox_name(blue_h.MACRO_HOSTGROUPNAME, "HOSTGROUPNAME");
      add_macrox_name(blue_h.MACRO_HOSTGROUPALIAS, "HOSTGROUPALIAS");
      add_macrox_name(blue_h.MACRO_SERVICEGROUPNAME, "SERVICEGROUPNAME");
      add_macrox_name(blue_h.MACRO_SERVICEGROUPALIAS, "SERVICEGROUPALIAS");
      add_macrox_name(blue_h.MACRO_HOSTACKAUTHOR, "HOSTACKAUTHOR");
      add_macrox_name(blue_h.MACRO_HOSTACKCOMMENT, "HOSTACKCOMMENT");
      add_macrox_name(blue_h.MACRO_SERVICEACKAUTHOR, "SERVICEACKAUTHOR");
      add_macrox_name(blue_h.MACRO_SERVICEACKCOMMENT, "SERVICEACKCOMMENT");
      add_macrox_name(blue_h.MACRO_LASTSERVICEOK, "LASTSERVICEOK");
      add_macrox_name(blue_h.MACRO_LASTSERVICEWARNING, "LASTSERVICEWARNING");
      add_macrox_name(blue_h.MACRO_LASTSERVICEUNKNOWN, "LASTSERVICEUNKNOWN");
      add_macrox_name(blue_h.MACRO_LASTSERVICECRITICAL, "LASTSERVICECRITICAL");
      add_macrox_name(blue_h.MACRO_LASTHOSTUP, "LASTHOSTUP");
      add_macrox_name(blue_h.MACRO_LASTHOSTDOWN, "LASTHOSTDOWN");
      add_macrox_name(blue_h.MACRO_LASTHOSTUNREACHABLE, "LASTHOSTUNREACHABLE");
      add_macrox_name(blue_h.MACRO_SERVICECHECKCOMMAND, "SERVICECHECKCOMMAND");
      add_macrox_name(blue_h.MACRO_HOSTCHECKCOMMAND, "HOSTCHECKCOMMAND");
      add_macrox_name(blue_h.MACRO_MAINCONFIGFILE, "MAINCONFIGFILE");
      add_macrox_name(blue_h.MACRO_STATUSDATAFILE, "STATUSDATAFILE");
      add_macrox_name(blue_h.MACRO_COMMENTDATAFILE, "COMMENTDATAFILE");
      add_macrox_name(blue_h.MACRO_DOWNTIMEDATAFILE, "DOWNTIMEDATAFILE");
      add_macrox_name(blue_h.MACRO_RETENTIONDATAFILE, "RETENTIONDATAFILE");
      add_macrox_name(blue_h.MACRO_OBJECTCACHEFILE, "OBJECTCACHEFILE");
      add_macrox_name(blue_h.MACRO_TEMPFILE, "TEMPFILE");
      add_macrox_name(blue_h.MACRO_LOGFILE, "LOGFILE");
      add_macrox_name(blue_h.MACRO_RESOURCEFILE, "RESOURCEFILE");
      add_macrox_name(blue_h.MACRO_COMMANDFILE, "COMMANDFILE");
      add_macrox_name(blue_h.MACRO_HOSTPERFDATAFILE, "HOSTPERFDATAFILE");
      add_macrox_name(blue_h.MACRO_SERVICEPERFDATAFILE, "SERVICEPERFDATAFILE");
      add_macrox_name(blue_h.MACRO_HOSTACTIONURL, "HOSTACTIONURL");
      add_macrox_name(blue_h.MACRO_HOSTNOTESURL, "HOSTNOTESURL");
      add_macrox_name(blue_h.MACRO_HOSTNOTES, "HOSTNOTES");
      add_macrox_name(blue_h.MACRO_SERVICEACTIONURL, "SERVICEACTIONURL");
      add_macrox_name(blue_h.MACRO_SERVICENOTESURL, "SERVICENOTESURL");
      add_macrox_name(blue_h.MACRO_SERVICENOTES, "SERVICENOTES");
      add_macrox_name(blue_h.MACRO_TOTALHOSTSUP, "TOTALHOSTSUP");
      add_macrox_name(blue_h.MACRO_TOTALHOSTSDOWN, "TOTALHOSTSDOWN");
      add_macrox_name(blue_h.MACRO_TOTALHOSTSUNREACHABLE, "TOTALHOSTSUNREACHABLE");
      add_macrox_name(blue_h.MACRO_TOTALHOSTSDOWNUNHANDLED, "TOTALHOSTSDOWNUNHANDLED");
      add_macrox_name(blue_h.MACRO_TOTALHOSTSUNREACHABLEUNHANDLED, "TOTALHOSTSUNREACHABLEUNHANDLED");
      add_macrox_name(blue_h.MACRO_TOTALHOSTPROBLEMS, "TOTALHOSTPROBLEMS");
      add_macrox_name(blue_h.MACRO_TOTALHOSTPROBLEMSUNHANDLED, "TOTALHOSTPROBLEMSUNHANDLED");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESOK, "TOTALSERVICESOK");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESWARNING, "TOTALSERVICESWARNING");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESCRITICAL, "TOTALSERVICESCRITICAL");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESUNKNOWN, "TOTALSERVICESUNKNOWN");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESWARNINGUNHANDLED, "TOTALSERVICESWARNINGUNHANDLED");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESCRITICALUNHANDLED, "TOTALSERVICESCRITICALUNHANDLED");
      add_macrox_name(blue_h.MACRO_TOTALSERVICESUNKNOWNUNHANDLED, "TOTALSERVICESUNKNOWNUNHANDLED");
      add_macrox_name(blue_h.MACRO_TOTALSERVICEPROBLEMS, "TOTALSERVICEPROBLEMS");
      add_macrox_name(blue_h.MACRO_TOTALSERVICEPROBLEMSUNHANDLED, "TOTALSERVICEPROBLEMSUNHANDLED");
      add_macrox_name(blue_h.MACRO_PROCESSSTARTTIME, "PROCESSSTARTTIME");
      add_macrox_name(blue_h.MACRO_HOSTCHECKTYPE, "HOSTCHECKTYPE");
      add_macrox_name(blue_h.MACRO_SERVICECHECKTYPE, "SERVICECHECKTYPE");

      logger.trace("exiting " + cn + ".init_macrox_names()");
      return common_h.OK;
   }

   /* saves the name of a macro */
   public static int add_macrox_name(int i, String name)
   {

      /* dup the macro name */
      blue.macro_x_names[i] = name;

      return common_h.OK;
   }

   /* free memory associated with the macrox names */
   public static int free_macrox_names()
   {

      logger.trace("entering " + cn + ".free_macrox_names");

      /* free each macro name */
      for (int x = 0; x < blue_h.MACRO_X_COUNT; x++)
      {
         blue.macro_x_names[x] = null;
      }

      logger.trace("exiting " + cn + ".free_macrox_names");

      return common_h.OK;
   }

   /* sets or unsets all macro environment variables */
   
   public static void set_all_macro_environment_vars(HashMap<String, String> envHashMap)
   {

      logger.trace("entering " + cn + ".set_all_macro_environment_vars");

      set_macrox_environment_vars(envHashMap);
      set_argv_macro_environment_vars(envHashMap);

      logger.trace("exiting " + cn + ".set_all_macro_environment_vars");

   }

   
   /*
    * This method simply adds all of the user defined macros into a Hashmap that is available
    * when running a host/service check.  
    */
   
   public static void set_macrox_environment_vars(HashMap<String, String> envHashMap)
   {
      logger.trace("entering " + cn + ".set_macrox_environment_vars");

      /* set each of the macrox environment variables */
      for (int x = 0; x < blue_h.MACRO_X_COUNT; x++)
      {

         /* host/service output/perfdata macros get cleaned */
         if (x >= 16 && x <= 19)
            envHashMap.put(blue.macro_x_names[x], clean_macro_chars(blue.macro_x[x], blue_h.STRIP_ILLEGAL_MACRO_CHARS
                  | blue_h.ESCAPE_MACRO_CHARS));

         /* others don't */
         else
            envHashMap.put(blue.macro_x_names[x], blue.macro_x[x]);
      }

      logger.trace("exiting " + cn + ".set_macrox_environment_vars");
   }

   /*
    * This method simply adds all of the system defined macros into a hashmap that is 
    * used when calling service checks. The value of a service check will then be available
    * by calling HashMap.get("ARG1"); 
    */
   
   public static void set_argv_macro_environment_vars(HashMap<String, String> envHashMap)
   {

      logger.trace("entering " + cn + ".set_argv_macro_environment_vars ");

      /* set each of the argv macro environment variables */
      for (int x = 0; x < blue_h.MAX_COMMAND_ARGUMENTS; x++)
      {
         envHashMap.put("ARG" + (x + 1), blue.macro_argv[x]);
      }

      logger.trace("exiting " + cn + ".set_argv_macro_environment_vars");
   }

   ///******************************************************************/
   ///******************** SYSTEM COMMAND FUNCTIONS ********************/
   ///******************************************************************/
  
   /*
    * This class is used to represent the result of a system call, i.e. when calling
    * a check of a service. The attributes of the class are quite self-explanitory.
    * 
    * The output relates to the output of the plug-in that has been used to run the check.
    */
   
   public static class system_result
   {
      public double exec_time = 0.0;

      public int result = -1;

      // TODO propertly set early_timeout in my_system.
      public boolean early_timeout = false;

      public String output = null;
   }

   
   public static void main(String[] args)
   {
      System.out.println("Exec " + args[0]);
      my_system(args[0], 50000);
   }

   
   /** 
    * executes a system command - used for service checks and notifications
    * 
    *  @param cmd command to be executed, it is expected this command has already been prepped.
    *  @param timeout timeout to kill spawned processes if it takes too long
    */
   
   public static utils.system_result my_system(String cmd, int timeout)
   {
      logger.trace("entering " + cn + ".my_system");

      /* initialize return variables */
      utils.system_result result = new system_result();

      /* This populates the Hashmap with not only system variables, but also Blue system macros 
       * i.e. HOSTNAME,SERVICENAME etc, and also the user defined macros.
       */
      HashMap<String, String> envHashMap = new HashMap(System.getenv());
      set_all_macro_environment_vars(envHashMap);

      /* run the command */
      blue_h.timeval start_time = new blue_h.timeval();
      Process process = null;
     
      try
      {

       /* send data to event broker */
         broker.broker_system_command(broker_h.NEBTYPE_SYSTEM_COMMAND_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,start_time,new blue_h.timeval(0,0), result.exec_time,timeout, result.early_timeout?common_h.TRUE:common_h.FALSE,result.result,cmd,null,null);
         
        String[] cmdArray = utils.processCommandLine( cmd );
        logger.debug( "\tCMD : " + cmd );
        logger.debug( "\tCMDArray : " + cmdArray.length );

        process = Runtime.getRuntime().exec(cmdArray, getEnv(envHashMap));

         BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
         String buffer = input.readLine();
         logger.debug( "\tLINE : " + buffer);

         result.output = buffer;

         while (buffer != null)
            buffer = input.readLine();
         // TODO add the time out back in.

         process.waitFor();
         result.result = process.exitValue();
         logger.debug( "\tRESULT : " + result.result);

      }
      catch (Throwable e)
      {
         e.printStackTrace();
    	  //TODO - not sure we need this..if result was unsuccessful, we can simply set the
    	  // result to be critical.
         result.result = -1;
         logger.warn("Warning: " + e.getMessage().toString() );
         result.result = blue_h.STATE_CRITICAL;
      }

      /* return execution time in milliseconds */
      result.exec_time = (start_time.time - System.currentTimeMillis());

      /* check for possibly missing scripts/binaries/etc */
      if (result.result == 126 || result.result == 127)
      {
         logger.warn("Warning: Attempting to execute the command \"" + cmd + "\" resulted in a return code of "
               + result.result + ".  Make sure the script or binary you are trying to execute actually exists...");
      }

      /* because of my idiotic idea of having UNKNOWN states be equivalent to -1, I must hack things a bit... */
      if (result.result == 255 || result.result == -1)
         result.result = blue_h.STATE_UNKNOWN;

      /* check bounds on the return value */
      if (result.result < -1 || result.result > 3)
         result.result = blue_h.STATE_UNKNOWN;

      /* send data to event broker */
      broker.broker_system_command(broker_h.NEBTYPE_SYSTEM_COMMAND_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,start_time,new blue_h.timeval(),result.exec_time,timeout,result.early_timeout?common_h.TRUE:common_h.FALSE,result.result,cmd,result.output,null);

      logger.trace("exiting " + cn + ".my_system");

      return result;
   }

  
   /* given a "raw" command, return the "expanded" or "whole" command line */
   public static String get_raw_command_line(String cmd, int macro_options)
   {
      String raw_command;
      objects_h.command temp_command;

      logger.trace("entering " + cn + ".get_raw_command_line");
      logger.debug("\tInput: " + cmd);

      /* clear the argv macros */
      clear_argv_macros();

      /* make sure we've got all the requirements */
      if (cmd == null)
      {
         logger.debug("\tWe don't have enough data to get the raw command line!");
         return null;
      }

      /* lookup the command... */

      /* get the command name */
      String[] split = cmd.split("\\!");
      raw_command = split[0];

      /* find the command used to check this service */
      temp_command = objects.find_command(raw_command);

      /* error if we couldn't find the command */
      if (temp_command == null)
         return null;

      raw_command = temp_command.command_line.trim();

      /* get the command arguments */
      for (int x = 1; x < split.length; x++)
      {
         /* ADDED 01/29/04 EG */
         /* process any macros we find in the argument */
         blue.macro_argv[x - 1] = process_macros(split[x], macro_options);
      }

      logger.debug("\tOutput: " + raw_command);
      logger.trace("exiting " + cn + ".get_raw_command_line");

      return raw_command;
   }

  
   /******************************************************************/
   /************************* TIME FUNCTIONS *************************/
   /******************************************************************/

   /* see if the specified time falls into a valid time range in the given time period */
   public static int check_time_against_period(long check_time, String period_name)
   {
      logger.trace("entering " + cn + ".check_time_against_period");

      /* if no period name was specified, assume the time is good */
      if (period_name == null || period_name.length() == 0)
         return common_h.OK;

      /* if period could not be found, assume the time is good */
      objects_h.timeperiod temp_period = objects.find_timeperiod(period_name);
      if (temp_period == null)
         return common_h.OK;

      /* calculate the start of the day (midnight, 00:00 hours) */
      Calendar t = Calendar.getInstance();
      t.setTimeInMillis(check_time * 1000);
      t.set( Calendar.SECOND, 0);
      t.set( Calendar.MINUTE, 0 );
      t.set( Calendar.HOUR, 0 );

      long midnight_today = utils.getTimeInSeconds(t);

      for (ListIterator iter = temp_period.days[t.get(Calendar.DAY_OF_WEEK)-1].listIterator(); iter.hasNext();)
      {
         objects_h.timerange temp_range = (objects_h.timerange) iter.next();

         /* if the user-specified time falls in this range, return with a positive result */
         if ((check_time >= midnight_today + temp_range.range_start)
               && (check_time <= midnight_today + temp_range.range_end))
            return common_h.OK;
      }

      logger.trace("exiting " + cn + ".check_time_against_period");
      return common_h.ERROR;
   }

   /* given a preferred time, get the next valid time within a time period */
   public static long get_next_valid_time(long preferred_time, String period_name)
   {
      long earliest_next_valid_time = 0L;
      long valid_time = 0L;

      logger.trace("entering " + cn + ".get_next_valid_time");
      logger.debug("\tPreferred Time: " + preferred_time + " -. " + preferred_time);

      /* if the preferred time is valid today, go with it */
      if (check_time_against_period(preferred_time, period_name) == common_h.OK)
      {
         valid_time = preferred_time;

         /* else find the next available time */
      }
      else
      {

         /* find the time period - if we can't find it, go with the preferred time */
         objects_h.timeperiod temp_timeperiod = objects.find_timeperiod(period_name);
         if (temp_timeperiod == null)
         {
            return preferred_time;
         }

         /* calculate the start of the day (midnight, 00:00 hours) */
         Date t = new Date();
         t.setSeconds(0);
         t.setMinutes(0);
         t.setHours(0);

         long midnight_today = t.getTime();
         int today = t.getDay();
         boolean has_looped = false;

         /* check a one week rotation of time */
         for (int weekday = today, days_into_the_future = 0;; weekday++, days_into_the_future++)
         {

            if (weekday >= 7)
            {
               weekday -= 7;
               has_looped = true;
            }

            /* check all time ranges for this day of the week */
            for (ListIterator iter = temp_timeperiod.days[weekday].listIterator(); iter.hasNext();)
            {
               objects_h.timerange temp_timerange = (objects_h.timerange) iter.next();

               /* calculate the time for the start of this time range */
               long this_time_range_start = (midnight_today + (days_into_the_future * 3600 * 24) + temp_timerange.range_start);

               /* we're looking for the earliest possible start time for this day */
               if ((earliest_next_valid_time == 0 || (this_time_range_start < earliest_next_valid_time))
                     && (this_time_range_start >= preferred_time))
                  earliest_next_valid_time = this_time_range_start;
            }

            /* break out of the loop if we have checked an entire week already */
            if (has_looped == true && weekday >= today)
               break;
         }

         /* if we couldn't find a time period (there must be none defined) */
         if (earliest_next_valid_time == 0)
            valid_time = preferred_time;

         /* else use the calculated time */
         else
            valid_time = earliest_next_valid_time;
      }

      logger.debug("\tNext Valid Time: " + valid_time + " -. " + new Date(valid_time*1000).toString());
      logger.trace("exiting " + cn + ".get_next_valid_time");
      return valid_time;
   }

   /* given a date/time in time_t format, produce a corresponding date/time string, including timezone */
   public static String get_datetime_string(long raw_time, int type)
   {
      String buffer;

      /* ctime() style date/time */
      if (type == common_h.LONG_DATE_TIME)
         buffer = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy").format(new Long(raw_time));

      /* short date/time */
      else if (type == common_h.SHORT_DATE_TIME)
      {
         if (blue.date_format == common_h.DATE_FORMAT_EURO)
            buffer = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Long(raw_time));
         else if (blue.date_format == common_h.DATE_FORMAT_ISO8601)
            buffer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Long(raw_time));
         else if (blue.date_format == common_h.DATE_FORMAT_STRICT_ISO8601)
            buffer = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Long(raw_time));
         else
            buffer = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Long(raw_time));
      }

      /* short date */
      else if (type == common_h.SHORT_DATE)
      {
         if (blue.date_format == common_h.DATE_FORMAT_EURO)
            buffer = new SimpleDateFormat("dd-MM-yyyy").format(new Long(raw_time));
         else if (blue.date_format == common_h.DATE_FORMAT_ISO8601
               || blue.date_format == common_h.DATE_FORMAT_STRICT_ISO8601)
            buffer = new SimpleDateFormat("yyyy-MM-dd").format(new Long(raw_time));
         else
            buffer = new SimpleDateFormat("MM-dd-yyyy").format(new Long(raw_time));
      }

      /* short time */
      else
         buffer = new SimpleDateFormat("HH:mm:ss").format(new Long(raw_time));

      return buffer;
   }

   /* get the next time to schedule a log rotation */
   public static long get_next_log_rotation_time()
   {
      //  time_t run_time;

      logger.trace("entering " + cn + ".get_next_log_rotation_time");

      Calendar now = Calendar.getInstance();
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.SECOND, 0);

      switch (blue.log_rotation_method)
      {
         case common_h.LOG_ROTATION_HOURLY :
            now.add(Calendar.HOUR_OF_DAY, 1);
            break;
         case common_h.LOG_ROTATION_DAILY :
            now.add(Calendar.DAY_OF_MONTH, 1);
            now.set(Calendar.HOUR_OF_DAY, 0);
            break;
         case common_h.LOG_ROTATION_WEEKLY :
            now.add(Calendar.DAY_OF_MONTH, 7 - (now.get(Calendar.DAY_OF_WEEK)-1));
            now.set(Calendar.HOUR_OF_DAY, 0);
            break;
         case common_h.LOG_ROTATION_MONTHLY :
         default :
            now.add(Calendar.MONTH, 1);
            now.set(Calendar.DAY_OF_MONTH, 1);
            now.set(Calendar.HOUR_OF_DAY, 0);
            break;
      }

      //  is_dst_now=now.getTimeZone().inDaylightTime(current_time);
      //  if(is_dst_now==TRUE && t.tm_isdst==0)
      //  run_time+=3600;
      //  else if(is_dst_now==FALSE && t.tm_isdst>0)
      //  run_time-=3600;

      logger.debug("\tNext Log Rotation Time: " + now.toString());
      logger.trace("exiting " + cn + ".get_next_log_rotation_time");
      return getTimeInSeconds(now);
   }

   /******************************************************************/
   /******************** SIGNAL HANDLER FUNCTIONS ********************/
   /******************************************************************/
   
   
   /*
    * Method that simply adds a shutdown hook into the current RunTime.
    * The shutdown hook adds an instance of the class shutdown_handler as the hook and 
    * prior to that resets the blue.sigshutdown value to false. This means that we are 
    * continuously monitoring within blue.Java until the sigshutdown value has the 'true'
    * value. 
    */
   
   public static void setup_sighandler()
   {
   
    logger.trace( "entering " + cn + ".setup_sighandler" );
   
   	/* reset the shutdown flag */
   	blue.sigshutdown=common_h.FALSE;
   
   	/* initialize signal handling */
    Runtime.getRuntime().addShutdownHook(new shutdown_handler());

    logger.trace( "exiting " + cn + ".setup_sighandler" );
   
   	return;
           }
   
   
   //
   //
   ///* reset signal handling... */
   //void reset_sighandler(void){
   //
   // logger.trace( "entering " + cn + ".reset_sighandler" );
   //
   //	/* set signal handling to default actions */
   //	signal(SIGQUIT,SIG_DFL);
   //	signal(SIGTERM,SIG_DFL);
   //	signal(SIGHUP,SIG_DFL);
   //	signal(SIGSEGV,SIG_DFL);
   //	signal(SIGPIPE,SIG_DFL);
   //
   // logger.trace( "exiting " + cn + ".reset_sighandler" );
   //
   //	return;
   //        }
   //
   //

   /*
    * Small class that attempts to deal with Blue shutdowns cleanly. This class is registered
    * as a Shutdown Handler. When the VM receives a shutdown command, this firstly sets the sigshutdown
    * value to true which will stop the main monitoring within blue.Java. It then releases locks
    * held on the Blue lock file so that we know the process is no longer running. It also closes away
    * the external command file that may have commands in it from passive/aggressive check results or event handlers.
    */
   
   public static class shutdown_handler extends Thread {
      
      public void run()
      {
         logger.trace( "entering shutdown_handler" );
         
         if ( blue.sigshutdown == common_h.FALSE )
         {
         
         blue.sigshutdown = common_h.TRUE;

         logger.info("Caught Shutdown, shutting down..." );         

         /* Release the lock file, so others are sure we are not running! */

         try
         { 
            blue.blue_file_lock.release();
            blue.blue_file_lock_channel.close();
            
            File lock = new File(blue.lock_file);
            lock.delete();         
         }
          	 catch (Exception e)
          	 {}
         }

         close_command_file();
         
         logger.trace( "exiting shutdown_handler" );
         
      }
   }

   //
   ///* handle timeouts when executing service checks */
   //void service_check_sighandler(int sig){
   //	struct timeval end_time;
   //
   //	/* get the current time */
   //	gettimeofday(&end_time,null);
   //
   //	/* write plugin check results to message queue */
   //	strncpy(svc_msg.output,"(Service Check Timed Out)",sizeof(svc_msg.output)-1);
   //	svc_msg.output[sizeof(svc_msg.output)-1]='\x0';
   //#ifdef SERVICE_CHECK_TIMEOUTS_RETURN_UNKNOWN
   //	svc_msg.return_code=STATE_UNKNOWN;
   //#else
   //	svc_msg.return_code=STATE_CRITICAL;
   //#endif
   //	svc_msg.exited_ok=TRUE;
   //	svc_msg.check_type=SERVICE_CHECK_ACTIVE;
   //	svc_msg.finish_time=end_time;
   //	svc_msg.early_timeout=TRUE;
   //	write_svc_message(&svc_msg);
   //
   //	/* close write end of IPC pipe */
   //	close(ipc_pipe[1]);
   //
   //	/* try to kill the command that timed out by sending termination signal to our process group */
   //	/* we also kill ourselves while doing this... */
   //	kill((pid_t)0,SIGKILL);
   //	
   //	/* force the child process (service check) to exit... */
   //	exit(STATE_CRITICAL);
   //        }
   //
   //
   ///* handle timeouts when executing commands via my_system() */
   //void my_system_sighandler(int sig){
   //
   //	/* force the child process to exit... */
   //	exit(STATE_CRITICAL);
   //        }

   ///******************************************************************/
   ///************************ DAEMON FUNCTIONS ************************/
   ///******************************************************************/
   //
   //int daemon_init(void){
   //	pid_t pid=-1;
   //	int pidno;
   //	int lockfile;
   //	int val=0;
   //	char buf[256];
   //	struct flock lock;
   //	char temp_buffer[MAX_INPUT_BUFFER];
   //	String homedir=null;
   //
   //#ifdef RLIMIT_CORE
   //	struct rlimit limit;
   //#endif
   //
   //	/* change working directory. scuttle home if we're dumping core */
   //	homedir=getenv("HOME");
   //	if(daemon_dumps_core==TRUE && homedir!=null)
   //		chdir(homedir);
   //	else
   //		chdir("/");
   //
   //	umask(S_IWGRP|S_IWOTH);
   //
   //	lockfile=open(lock_file,O_RDWR | O_CREAT, S_IWUSR | S_IRUSR | S_IRGRP | S_IROTH);
   //
   //	if(lockfile<0){
   //		strcpy(temp_buffer,"");
   //		if(errno==EISDIR)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"%s is a directory\n",lock_file);
   //		else if(errno==EACCES)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"You do not have permission to write to %s\n",lock_file);
   //		else if(errno==ENAMETOOLONG)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"The filename is too long: %s\n",lock_file);
   //		else if(errno==ENOENT)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"%s does not exist (ENOENT)\n",lock_file);
   //		else if(errno==ENOTDIR)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"%s does not exist (ENOTDIR)\n",lock_file);
   //		else if(errno==ENXIO)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Cannot write to special file\n");
   //		else if(errno==ENODEV)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Cannot write to device\n");
   //		else if(errno==EROFS)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"%s is on a read-only file system\n",lock_file);
   //		else if(errno==ETXTBSY)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"%s is a currently running program\n",lock_file);
   //		else if(errno==EFAULT)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"%s is outside address space\n",lock_file);
   //		else if(errno==ELOOP)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Too many symbolic links\n");
   //		else if(errno==ENOSPC)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"No space on device\n");
   //		else if(errno==ENOMEM)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Insufficient kernel memory\n");
   //		else if(errno==EMFILE)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Too many files open in process\n");
   //		else if(errno==ENFILE)
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Too many files open on system\n");
   //
   //		temp_buffer[sizeof(temp_buffer)-1]='\x0';
   //		write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_ERROR,TRUE);
   //
   //		snprintf(temp_buffer,sizeof(temp_buffer),"Bailing out due to errors encountered while attempting to daemonize... (PID=%d)",(int)getpid());
   //		temp_buffer[sizeof(temp_buffer)-1]='\x0';
   //		write_to_logs_and_console(temp_buffer,NSLOG_PROCESS_INFO | NSLOG_RUNTIME_ERROR,TRUE);
   //
   //		cleanup();
   //		exit(ERROR);
   //	        }
   //
   //	/* see if we can read the contents of the lockfile */
   //	if((val=read(lockfile,buf,(size_t)10))<0){
   //		write_to_logs_and_console("Lockfile exists but cannot be read",NSLOG_RUNTIME_ERROR,TRUE);
   //		cleanup();
   //		exit(ERROR);
   //	        }
   //
   //	/* we read something - check the PID */
   //	if(val>0){
   //		if((val=sscanf(buf,"%d",&pidno))<1){
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Lockfile '%s' does not contain a valid PID (%s)",lock_file,buf);
   //			write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_ERROR,TRUE);
   //			cleanup();
   //			exit(ERROR);
   //		        }
   //	        }
   //
   //	/* check for SIGHUP */
   //	if(val==1 && (pid=(pid_t)pidno)==getpid()){
   //		close(lockfile);
   //		return OK;
   //	        }
   //
   //	/* exit on errors... */
   //	if((pid=fork())<0)
   //		return(ERROR);
   //
   //	/* parent process goes away.. */
   //	else if((int)pid!=0)
   //		exit(OK);
   //
   //	/* child continues... */
   //
   //	/* child becomes session leader... */
   //	setsid();
   //
   //	/* place a file lock on the lock file */
   //	lock.l_type=F_WRLCK;
   //	lock.l_start=0;
   //	lock.l_whence=SEEK_SET;
   //	lock.l_len=0;
   //	if(fcntl(lockfile,F_SETLK,&lock)<0){
   //		if(errno==EACCES || errno==EAGAIN){
   //			fcntl(lockfile,F_GETLK,&lock);
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Lockfile '%s' is held by PID %d.  Bailing out...",lock_file,(int)lock.l_pid);
   //		        }
   //		else
   //			snprintf(temp_buffer,sizeof(temp_buffer)-1,"Cannot lock lockfile '%s': %s. Bailing out...",lock_file,strerror(errno));
   //		write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_ERROR,TRUE);
   //		cleanup();
   //		exit(ERROR);
   //	        }
   //
   //	/* prevent daemon from dumping a core file... */
   //#ifdef RLIMIT_CORE
   //	if(daemon_dumps_core==FALSE){
   //		getrlimit(RLIMIT_CORE,&limit);
   //		limit.rlim_cur=0;
   //		setrlimit(RLIMIT_CORE,&limit);
   //	        }
   //#endif
   //
   //	/* write PID to lockfile... */
   //	lseek(lockfile,0,SEEK_SET);
   //	ftruncate(lockfile,0);
   //	sprintf(buf,"%d\n",(int)getpid());
   //	write(lockfile,buf,strlen(buf));
   //
   //	/* make sure lock file stays open while program is executing... */
   //	val=fcntl(lockfile,F_GETFD,0);
   //	val|=FD_CLOEXEC;
   //	fcntl(lockfile,F_SETFD,val);
   //
   //        /* close existing stdin, stdout, stderr */
   //	close(0);
   //	close(1);
   //	close(2);
   //
   //	/* THIS HAS TO BE DONE TO AVOID PROBLEMS WITH STDERR BEING REDIRECTED TO SERVICE MESSAGE PIPE! */
   //	/* re-open stdin, stdout, stderr with known values */
   //	open("/dev/null",O_RDONLY);
   //	open("/dev/null",O_WRONLY);
   //	open("/dev/null",O_WRONLY);
   //
   //#ifdef USE_EVENT_BROKER
   //	/* send program data to broker */
   //	broker_program_state(NEBTYPE_PROCESS_DAEMONIZE,NEBFLAG_NONE,NEBATTR_NONE,null);
   //#endif
   //
   //	return OK;
   //	}
   //

   ///******************************************************************/
   ///*********************** SECURITY FUNCTIONS ***********************/
   ///******************************************************************/
   //
   ///* drops privileges */
   //public int drop_privileges( String user, String group){
   ////	char temp_buffer[MAX_INPUT_BUFFER];
   ////	uid_t uid=-1;
   ////	gid_t gid=-1;
   //    GroupStruct grp;
   ////	struct passwd *pw;
   ////	int result=OK;
   //
   //    logger.debug("org.blue.base", "drop_privileges()" );
   //    logger.info("Original UID/GID: "+getuid()+"/"+getgid()+"\n" );
   //
   //
   //	/* only drop privileges if we're running as root, so we don't interfere with being debugged while running as some random user */
   //	if( getuid()!=0 )
   //		return common_h.OK;
   //
   //	/* set effective group ID */
   //	if(group!= null){
   //		
   //	    /* see if this is a group name */
   //	    if( strspn(group,"0123456789")<strlen(group)){
   //	        grp = Posix.getgrnam(group);
   //	        if( grp!= null )
   //	            gid = (gid_t)( grp.gr_gid() );
   //	        else
   //	            logger.warning( "Warning: Could not get group entry for '"+group+"'" );
   //	    }
   //
   //		/* else we were passed the GID */
   //		else
   //			gid=(gid_t)atoi(group);
   //
   //		/* set effective group ID if other than current EGID */
   //		if(gid!=getegid()){
   //
   //			if(setgid(gid)==-1){
   //				snprintf(temp_buffer,sizeof(temp_buffer)-1,"Warning: Could not set effective GID=%d",(int)gid);
   //				temp_buffer[sizeof(temp_buffer)-1]='\x0';
   //				write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_WARNING,TRUE);
   //				result=ERROR;
   //			        }
   //		        }
   //	        }
   //
   //
   //	/* set effective user ID */
   //	if(user!= null){
   //		
   //		/* see if this is a user name */
   //		if(strspn(user,"0123456789")<strlen(user)){
   //			pw=(struct passwd *)getpwnam(user);
   //			if(pw!= null )
   //				uid=(uid_t)(pw.pw_uid);
   //			else{
   //				snprintf(temp_buffer,sizeof(temp_buffer)-1,"Warning: Could not get passwd entry for '%s'",user);
   //				temp_buffer[sizeof(temp_buffer)-1]='\x0';
   //				write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_WARNING,TRUE);
   //			        }
   //		        }
   //
   //		/* else we were passed the UID */
   //		else
   //			uid=(uid_t)atoi(user);
   //			
   //		if(uid!=geteuid()){
   //
   //			/* initialize supplementary groups */
   //			if(initgroups(user,gid)==-1){
   //				if(errno==EPERM){
   //					snprintf(temp_buffer,sizeof(temp_buffer)-1,"Warning: Unable to change supplementary groups using initgroups() -- I hope you know what you're doing");
   //					temp_buffer[sizeof(temp_buffer)-1]='\x0';
   //					write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_WARNING,TRUE);
   //		                        }
   //				else{
   //					snprintf(temp_buffer,sizeof(temp_buffer)-1,"Warning: Possibly root user failed dropping privileges with initgroups()");
   //					temp_buffer[sizeof(temp_buffer)-1]='\x0';
   //					write_to_logs_and_console(temp_buffer,NSLOG_RUNTIME_WARNING,TRUE);
   //					return ERROR;
   //			                }
   //	                        }
   //		        }
   //
   //		if(setuid(uid)==-1){
   //            logger.warning( "Warning: Could not set effective UID=" + uid);
   //			result=ERROR;
   //		        }
   //	        }
   //
   //logger.info( "New UID/GID: "+ getuid() +"/"+ getgid() +"\n" );
   //logger.debug("org.blue.base", "drop_privileges()" );
   //
   //	return result;
   //        }

   /******************************************************************/
   /************************* IPC FUNCTIONS **************************/
   /******************************************************************/

   /* reads a service message from the circular buffer */
   public static blue_h.service_message read_svc_message()
   {
      blue_h.service_message message = null;

      logger.trace("entering " + cn + ".read_svc_message");

      /* get a lock on the buffer */
      synchronized (blue.service_result_buffer.buffer_lock)
      {

         /* handle detected overflows */
         //	if(service_result_buffer.overflow>0){
         //
         //		/* log the warning */
         //		snprintf(buffer,sizeof(buffer)-1,"Warning: Overflow detected in service check result buffer - %lu message(s) lost.\n",service_result_buffer.overflow);
         //		buffer[sizeof(buffer)-1]='\x0';
         //		write_to_logs_and_console(buffer,NSLOG_RUNTIME_WARNING,TRUE);
         //
         //		/* reset overflow counter */
         //		service_result_buffer.overflow=0;
         //	        }
         /* there are no items in the buffer */
         if (blue.service_result_buffer.buffer.isEmpty())
            message = null;

         /* return the message from the tail of the buffer */
         else
         {

            /* copy message to user-supplied structure */
            message = (blue_h.service_message) blue.service_result_buffer.buffer.poll();
         }

         /* release the lock on the buffer */
      }

      logger.trace("exiting " + cn + ".read_svc_message");

      return message;
   }

   
   /**
    * writes a service message to the message pipe
    * 
    * Interesting about this method is it was based on PIPE's and specifically a forked process
    * writing to a pipe connected to the partent process.  The issue of even needing to fork vs create thread 
    * is still in my mind. 
    * 
    */
   
   public static int write_svc_message(blue_h.service_message message)
   {
      int write_result = common_h.OK;

      logger.trace("entering " + cn + ".write_svc_message");

      if (message == null)
         return 0;

      blue.ipc_queue.offer(message);
      logger.trace("exiting " + cn + ".write_svc_message");

      return write_result;
   }

   //
   /* creates external command file as a named pipe (FIFO) and opens it for reading (non-blocked mode) */
   
   public static int open_command_file()
   {
      //  char buffer[MAX_INPUT_BUFFER];
      //  struct stat st;
      //  int result;
      logger.trace("entering " + cn + ".open_command_file() start\n");

      logger.debug("open_command_file check_external_commands " + blue.check_external_commands);

      /* if we're not checking external commands, don't do anything */
      if (blue.check_external_commands == common_h.FALSE)
         return common_h.OK;

      logger.debug("open_command_file command_file_created " + blue.command_file_created);

      /* the command file was already created */
      if (blue.command_file_created == common_h.TRUE)
         return common_h.OK;

      /* use existing FIFO if possible */
      try
      {
         logger.debug("open_command_file command_file_channel " + blue.command_file);
         blue.command_file_channel = new RandomAccessFile(blue.command_file, "rw").getChannel();
      }
      catch (IOException ioE)
      {
         logger.fatal(
                     " Error: Could not create external command file '"
                           + blue.command_file
                           + "' as named pipe:  If this file already exists and you are sure that another copy of Blue is not running, you should delete this file.",
                     ioE);
         return common_h.ERROR;
      }

      /* initialize worker thread */
      if (command_file_worker_thread.init_command_file_worker_thread() == common_h.ERROR)
      {
         logger.fatal("Error: Could not initialize command file worker thread.");

         /* close the command file */
         try
         {
            blue.command_file_channel.close();
         }
         catch (IOException ioE)
         {
         }

         /* delete the named pipe */
         new File(blue.command_file).delete();

         return common_h.ERROR;
      }

      /* set a flag to remember we already created the file */
      blue.command_file_created = common_h.TRUE;

      logger.trace("exiting " + cn + ".open_command_file");
      return common_h.OK;
   }

   /* closes the external command file FIFO and deletes it */
   
   public static int close_command_file()
   {

      logger.trace("entering " + cn + ".close_command_file");

      /* if we're not checking external commands, don't do anything
       * as there won't be anything written to the external command file */
      
      if (blue.check_external_commands == common_h.FALSE)
         return common_h.OK;

      /* the command file wasn't created or was already cleaned up */
      if (blue.command_file_created == common_h.FALSE)
         return common_h.OK;

      /* reset our flag */
      blue.command_file_created = common_h.FALSE;

      /* shutdown the worker thread */
      command_file_worker_thread.shutdown_command_file_worker_thread();

      /* close the command file */
      try
      {
         blue.command_file_channel.close();
      }
      catch (Exception e)
      {
         logger.error("warning: " + e.getMessage(), e);
      }

      /* delete the named pipe */
      /*
       if(unlink(command_file)!=0)
       return ERROR;
       */

      logger.trace("exiting " + cn + ".close_command_file");
      return common_h.OK;
   }

   //
   //
   //
   //
   ///******************************************************************/
   ///************************ STRING FUNCTIONS ************************/
   ///******************************************************************/
   //

   /* determines whether or not an object name (host, service, etc) contains illegal characters */
   public static int contains_illegal_object_chars(String name)
   {

      if (name == null)
         return common_h.FALSE;

      for (int x = name.length() - 1; x >= 0; x--)
      {

         char ch = name.charAt(x);

         /* illegal ASCII characters */
         if (ch < 32 || ch == 127)
            return common_h.TRUE;

         /* illegal user-specified characters */
         if (blue.illegal_object_chars != null)
            for (int y = 0; y < blue.illegal_object_chars.length(); y++)
               if (ch == blue.illegal_object_chars.charAt(y))
                  return common_h.TRUE;
      }

      return common_h.FALSE;
   }

   /* cleans illegal characters in macros before output */
   public static String clean_macro_chars(String macro, int options)
   {

      if (macro == null)
         return "";

      /* strip illegal characters out of macro */
      if ((options & blue_h.STRIP_ILLEGAL_MACRO_CHARS) > 0)
      {
         return macro.replace(blue.illegal_output_chars, "");
      }

      return macro;
   }

   //
   //
   //
   ///* fix the problem with strtok() skipping empty options between tokens */	
   //String my_strtok(String buffer,String tokens){
   //	String token_position;
   //	String sequence_head;
   //
   //	if(buffer!=null){
   //		if(original_my_strtok_buffer!=null)
   //			free(original_my_strtok_buffer);
   //		my_strtok_buffer=(String )malloc(strlen(buffer)+1);
   //		if(my_strtok_buffer==null)
   //			return null;
   //		original_my_strtok_buffer=my_strtok_buffer;
   //		strcpy(my_strtok_buffer,buffer);
   //	        }
   //	
   //	sequence_head=my_strtok_buffer;
   //
   //	if(sequence_head[0]=='\x0')
   //		return null;
   //	
   //	token_position=strchr(my_strtok_buffer,tokens[0]);
   //
   //	if(token_position==null){
   //		my_strtok_buffer=strchr(my_strtok_buffer,'\x0');
   //		return sequence_head;
   //	        }
   //
   //	token_position[0]='\x0';
   //	my_strtok_buffer=token_position+1;
   //
   //	return sequence_head;
   //        }
   //
   //
   //
   ///* fixes compiler problems under Solaris, since strsep() isn't included */
   ///* this code is taken from the glibc source */
   //String my_strsep (String *stringp, const String delim){
   //	String begin, *end;
   //
   //	begin = *stringp;
   //	if (begin == null)
   //		return null;
   //
   //	/* A frequent case is when the delimiter string contains only one
   //	   character.  Here we don't need to call the expensive `strpbrk'
   //	   function and instead work using `strchr'.  */
   //	if(delim[0]=='\0' || delim[1]=='\0'){
   //		char ch = delim[0];
   //
   //		if(ch=='\0')
   //			end=null;
   //		else{
   //			if(*begin==ch)
   //				end=begin;
   //			else
   //				end=strchr(begin+1,ch);
   //			}
   //		}
   //
   //	else
   //		/* Find the end of the token.  */
   //		end = strpbrk (begin, delim);
   //
   //	if(end){
   //
   //		/* Terminate the token and set *STRINGP past NUL character.  */
   //		*end++='\0';
   //		*stringp=end;
   //		}
   //	else
   //		/* No more delimiters; this is the last token.  */
   //		*stringp=null;
   //
   //	return begin;
   //	}

   /* encodes a string in proper URL format */
   public static String get_url_encoded_string(String input)
   {
      try
      {
         return URLEncoder.encode(input, "UTF-8");
      }
      catch (UnsupportedEncodingException ueE)
      {
         logger.warn("warnging: utils.get_url_encoded_string illegal encoding UTF-8");
         return input;
      }
   }

   ///******************************************************************/
   ///************************* HASH FUNCTIONS *************************/
   ///******************************************************************/
   //
   ///* single hash function */
   //int hashfunc1(const String name1,int hashslots){
   //	unsigned int i,result;
   //
   //	result=0;
   //
   //	if(name1)
   //		for(i=0;i<strlen(name1);i++)
   //			result+=name1[i];
   //
   //	result=result%hashslots;
   //
   //	return result;
   //        }
   //
   //
   ///* dual hash function */
   //int hashfunc2(const String name1,const String name2,int hashslots){
   //	unsigned int i,result;
   //
   //	result=0;
   //	if(name1)
   //		for(i=0;i<strlen(name1);i++)
   //			result+=name1[i];
   //
   //	if(name2)
   //		for(i=0;i<strlen(name2);i++)
   //			result+=name2[i];
   //
   //	result=result%hashslots;
   //
   //	return result;
   //        }
   //
   //
   ///* single hash data comparison */
   //int compare_hashdata1(const String val1, const String val2){
   //	
   //	return strcmp(val1,val2);
   //        }
   //
   //
   ///* dual hash data comparison */
   //int compare_hashdata2(const String val1a, const String val1b, const String val2a, const String val2b){
   //	int result;
   //
   //	result=strcmp(val1a,val2a);
   //	if(result>0)
   //		return 1;
   //	else if(result<0)
   //		return -1;
   //	else
   //		return strcmp(val1b,val2b);
   //        }
   //
   //
   //

   public static class file_functions
   {
      /******************************************************************/
      /************************* FILE FUNCTIONS *************************/
      /******************************************************************/

      /* renames a file - works across filesystems (Mike Wiacek) */
      public static int my_rename(String source, String dest)
      {
         logger.debug( "RENAME "+ source +" to "+ dest );
//         boolean rename_result;

         /* make sure we have something */
         if (source == null || dest == null)
            return -1;

//         if (!rename_result)
         {
            logger.debug("\tMoving file across file systems.");

            FileChannel srcChannel = null;
            FileChannel dstChannel = null;
            FileLock lock = null;
            try
            {
               // Create channel on the source
               srcChannel = new FileInputStream(source).getChannel();

               // Create channel on the destination
               dstChannel = new FileOutputStream(dest).getChannel();

               lock = dstChannel.lock();
               // Copy file contents from source to destination
               dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
               dstChannel.force(true);
               
               
            }
            catch (IOException e)
            {
               logger.fatal("Error while copying file '" + source + "' to file '"
                     + dest + "'. " + e.getMessage(), e);
               return common_h.ERROR;
            }
            finally
            {
               try {
                  lock.release();
               } catch ( Throwable t) {
                  logger.fatal( "Error releasing file lock - " + dest );
               } 
               
               // Close the channels
               try
               {
                  srcChannel.close();
               }
               catch (Throwable t)
               {
               }
               try
               {
                  dstChannel.close();
               }
               catch (Throwable t)
               {
               }

            }
         }

         return common_h.OK;
      }

      /* open a file read-only via mmap() */
      public static blue_h.mmapfile mmap_fopen(String filename)
      {
         logger.debug( "OPEN " + filename );

         blue_h.mmapfile new_mmapfile = null;

         try
         {
            /* open the file */
//            File file = new File( filename );
//            RandomAccessFile file = new RandomAccessFile(filename, "rw");
//            FileChannel fileChannel = file.getChannel();

            new_mmapfile = new blue_h.mmapfile();
            new_mmapfile.path = filename;
//            new_mmapfile.fd = file.getFD();
//            new_mmapfile.file_size = fileChannel.size();
//            new_mmapfile.current_position = 0L;
            new_mmapfile.current_line = 0L;
            new_mmapfile.fc = Channels.newChannel( new FileInputStream( filename ) );
            new_mmapfile.reader = new BufferedReader( Channels.newReader(new_mmapfile.fc, "ISO-8859-1") );

//            MappedByteBuffer mappedfile = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
//            mappedfile.load();
//            new_mmapfile.mmap_buf = mappedfile;

//            Charset charset = Charset.forName("ISO-8859-1");
//            CharsetDecoder decoder = charset.newDecoder();
//            new_mmapfile.char_buf = decoder.decode(mappedfile);

         }
         catch (IOException ioE)
         {
            logger.error("SYSTEM: " + ioE.getMessage() + ";WARNING; File Set to null.");
            new_mmapfile = null;
         }

         return new_mmapfile;
      }

      /* close a file originally opened via mmap() */
      public static int mmap_fclose(blue_h.mmapfile temp_mmapfile)
      {

         logger.debug( "CLOSE " + temp_mmapfile.path );
         if (temp_mmapfile == null)
            return common_h.ERROR;

         try { temp_mmapfile.fc.close(); } catch ( IOException ioE ) {}
         try
         {
            temp_mmapfile.reader.close();
//            temp_mmapfile.lock.release();
//            temp_mmapfile.lock = null;
//            temp_mmapfile.fc.close();
//            temp_mmapfile.fc = null;
//            temp_mmapfile.char_buf = null;
//            temp_mmapfile.fd = null;
//            temp_mmapfile.mmap_buf = null;
         }
         catch (IOException ioE)
         {
            logger.error("warning: " + ioE.getMessage(), ioE);
            return common_h.ERROR;
         }

         return common_h.OK;
      }

      /* gets one line of input from an mmap()'ed file */
      public static String mmap_fgets(blue_h.mmapfile temp_mmapfile)
      {

         if (temp_mmapfile == null)
            return null;
         
         String buffer = null;
         
         try { 
            buffer = temp_mmapfile.reader.readLine();
         } catch ( IOException ioE ) {
            logger.fatal(ioE.getMessage(), ioE );
         }

//         /* we've reached the end of the file */
//         if (temp_mmapfile.current_position >= temp_mmapfile.file_size)
//            return null;

//         /* find the end of the string (or buffer) */
//         StringBuffer buffer = new StringBuffer();
//         char c;
//         for (long x = temp_mmapfile.current_position; x < temp_mmapfile.file_size; x++)
//         {
//            c = temp_mmapfile.char_buf.get();
//            if (c == 0 || c == '\n')
//            {
//               break;
//            }
//            buffer.append(c);
//         }

//         /* update the current position */
//         temp_mmapfile.current_position = temp_mmapfile.char_buf.position();

         /* increment the current line */
         temp_mmapfile.current_line++;

//         return buffer.toString();
         return buffer;
      }

      //
      //
      ///* gets one line of input from an mmap()'ed file (may be contained on more than one line in the source file) */
      //String mmap_fgets_multiline(mmapfile *temp_mmapfile){
      //	String buf=null;
      //	String tempbuf=null;
      //	int len;
      //	int len2;
      //
      //	if(temp_mmapfile==null)
      //		return null;
      //
      //	while(1){
      //
      //		free(tempbuf);
      //
      //		if((tempbuf=mmap_fgets(temp_mmapfile))==null)
      //			break;
      //
      //		if(buf==null){
      //			len=strlen(tempbuf);
      //			if((buf=(String )malloc(len+1))==null)
      //				break;
      //			memcpy(buf,tempbuf,len);
      //			buf[len]='\x0';
      //		        }
      //		else{
      //			len=strlen(tempbuf);
      //			len2=strlen(buf);
      //			if((buf=(String )realloc(buf,len+len2+1))==null)
      //				break;
      //			strcat(buf,tempbuf);
      //			len+=len2;
      //			buf[len]='\x0';
      //		        }
      //
      //		/* we shouldn't continue to the next line... */
      //		if(!(len>0 && buf[len-1]=='\\' && (len==1 || buf[len-2]!='\\')))
      //			break;
      //	        }
      //
      //	free(tempbuf);
      //
      //	return buf;
      //        }
   }

   //
   //
   ///******************************************************************/
   ///******************** EMBEDDED PERL FUNCTIONS *********************/
   ///******************************************************************/
   //
   ///* initializes embedded perl interpreter */
   //int init_embedded_perl(String *env){
   //#ifdef EMBEDDEDPERL
   //	String embedding[] = { "", "" };
   //	int exitstatus = 0;
   //	char buffer[MAX_INPUT_BUFFER];
   //
   //	embedding[1]=p1_file;
   //
   //	use_embedded_perl=TRUE;
   //
   //	PERL_SYS_INIT3(2,embedding,&env);
   //
   //	if((my_perl=perl_alloc())==null){
   //		use_embedded_perl=FALSE;
   //		snprintf(buffer,sizeof(buffer),"Error: Could not allocate memory for embedded Perl interpreter!\n");
   //		buffer[sizeof(buffer)-1]='\x0';
   //		write_to_logs_and_console(buffer,NSLOG_RUNTIME_ERROR,TRUE);
   //		return ERROR;
   //                }
   //
   //	perl_construct(my_perl);
   //	exitstatus=perl_parse(my_perl,xs_init,2,embedding,env);
   //	if(!exitstatus)
   //		exitstatus=perl_run(my_perl);
   //
   //#endif
   //	return OK;
   //        }
   //
   //
   ///* closes embedded perl interpreter */
   //int deinit_embedded_perl(void){
   //#ifdef EMBEDDEDPERL
   //
   //	PL_perl_destruct_level=0;
   //	perl_destruct(my_perl);
   //	perl_free(my_perl);
   //	PERL_SYS_TERM();
   //
   //#endif
   //	return OK;
   //        }

   /******************************************************************/
   /************************ THREAD FUNCTIONS ************************/
   /******************************************************************/

   /* This method is called by the command file worker thread when it has a command to process
    * within the external command file. The string is the command found within the command file
    * and the int represents the delay in processing
    * 
    *  @param = String cmd, the string of the command from the external command file.
    *  @param = int delay, the wait delay to see if a space becomes available in the command processing queue.
    */
   
   public static boolean submit_external_command(String cmd, int delay)
   {
      boolean result = true;

      try
      {
          /* try to insert the external command into the current Queue */
    	  result = blue.external_command_buffer.buffer.offer(cmd, delay, TimeUnit.MICROSECONDS);
      }
      catch (InterruptedException iE)
      {
         result = false;
      }
      catch (NullPointerException npE)
      {
         result = false;
      }

      return result;

   }

   //
   //
   //
   ///* submits a raw external command (without timestamp) for processing */
   //int submit_raw_external_command(String cmd, time_t *ts, int *buffer_items){
   //	String newcmd=null;
   //	int length=0;
   //	int result=OK;
   //	time_t timestamp;
   //
   //	if(cmd==null)
   //		return ERROR;
   //
   //	/* allocate memory for the command string */
   //	length=strlen(cmd)+16;
   //	newcmd=(String )malloc(length);
   //	if(newcmd==null)
   //		return ERROR;
   //
   //	/* get the time */
   //	if(ts!=null)
   //		timestamp=*ts;
   //	else
   //		time(&timestamp);
   //
   //	/* create the command string */
   //	snprintf(newcmd,length-1,"[%lu] %s",(unsigned long)timestamp,cmd);
   //	newcmd[length-1]='\x0';
   //
   //	/* submit the command */
   //	result=submit_external_command(newcmd,buffer_items);
   //
   //	/* free allocated memory */
   //	free(newcmd);
   //
   //	return result;
   //        }
   //
   //
   //
   //

   /******************************************************************/
   /************************* MISC FUNCTIONS *************************/
   /******************************************************************/
   
   /* returns Nagios version */
   public static String get_program_version()
   {
      
      return common_h.PROGRAM_VERSION;
   }
   
   
   /* returns Nagios modification date */
   public static String get_program_modification_date()
   {
      
      return common_h.PROGRAM_MODIFICATION_DATE;
   }

   /******************************************************************/
   /*********************** CLEANUP FUNCTIONS ************************/
   /******************************************************************/

   /* do some cleanup before we exit */
   public static void cleanup()
   {
      logger.trace("entering " + cn + ".cleanup");

      /* unload modules */
      if( blue.test_scheduling==common_h.FALSE && blue.verify_config==common_h.FALSE){
         nebmods.neb_free_callback_list();
         nebmods.neb_unload_all_modules( nebmodules_h.NEBMODULE_FORCE_UNLOAD,(blue.sigshutdown==common_h.TRUE)? nebmodules_h.NEBMODULE_NEB_SHUTDOWN:nebmodules_h.NEBMODULE_NEB_RESTART);
         nebmods.neb_free_module_list();
         nebmods.neb_deinit_modules();
      }

      /* free all allocated memory - including macros */
      free_memory();

      logger.trace("exiting " + cn + ".cleanup");
      return;
   }

   /* free the memory allocated to the linked lists */
   public static void free_memory()
   {

      logger.trace("entering " + cn + ".free_memory");

      //	/* free all allocated memory for the object definitions */
      //	free_object_data();
      //
      //	/* free memory allocated to comments */
      //	free_comment_data();
      //
      /* free memory for the high and low priority event list */
      events.event_list_high.clear();
      events.event_list_low.clear();
      logger.debug("\tevent lists freed");

      /* free memory for global event handlers */
      blue.global_host_event_handler = null;
      blue.global_service_event_handler = null;
      logger.debug("\tglobal event handlers freed\n");

      /* free any notification list that may have been overlooked */
      free_notification_list();
      logger.debug("\tnotification_list freed");

      /* free obsessive compulsive commands */
      blue.ocsp_command = null;
      blue.ochp_command = null;

      /* free memory associated with macros */
      for (int x = 0; x < blue_h.MAX_COMMAND_ARGUMENTS; x++)
         blue.macro_argv[x] = null;
      for (int x = 0; x < blue_h.MAX_USER_MACROS; x++)
         blue.macro_user[x] = null;
      for (int x = 0; x < blue_h.MACRO_X_COUNT; x++)
         blue.macro_x[x] = null;
      //	free_macrox_names();

      /* free illegal char strings */
      blue.illegal_object_chars = null;
      blue.illegal_output_chars = null;

      /* free blue user and group */
      blue.nagios_user = null;
      blue.nagios_group = null;

      /* free file/path variables */
      blue.log_file = null;
      blue.temp_file = null;
      blue.command_file = null;
      blue.lock_file = null;
      blue.auth_file = null;
      blue.p1_file = null;
      blue.log_archive_path = null;

      logger.trace("exiting " + cn + ".free_memory");

      return;
   }

   /* free a notification list that was created */
   public static void free_notification_list()
   {
      logger.trace("entering " + cn + ".free_notification_list");
      blue.notification_list.clear();
      logger.trace("exiting " + cn + ".free_notification_list");
   }

   /* reset all system-wide variables, so when we've receive a SIGHUP we can restart cleanly */

   public static int reset_variables()
   {
      logger.trace("entering " + cn + ".reset_variables()");

      blue.log_file = locations_h.DEFAULT_LOG_FILE;
      blue.temp_file = locations_h.DEFAULT_TEMP_FILE;
      blue.command_file = locations_h.DEFAULT_COMMAND_FILE;
      blue.lock_file = locations_h.DEFAULT_LOCK_FILE;
      blue.auth_file = locations_h.DEFAULT_AUTH_FILE;
      blue.p1_file = locations_h.DEFAULT_P1_FILE;
      blue.log_archive_path = locations_h.DEFAULT_LOG_ARCHIVE_PATH;

      blue.nagios_user = config_h.DEFAULT_NAGIOS_USER;
      blue.nagios_group = config_h.DEFAULT_NAGIOS_GROUP;

      blue.use_regexp_matches = common_h.FALSE;
      blue.use_true_regexp_matching = common_h.FALSE;

      blue.use_syslog = blue_h.DEFAULT_USE_SYSLOG;
      blue.log_service_retries = blue_h.DEFAULT_LOG_SERVICE_RETRIES;
      blue.log_host_retries = blue_h.DEFAULT_LOG_HOST_RETRIES;
      blue.log_initial_states = blue_h.DEFAULT_LOG_INITIAL_STATES;

      blue.log_notifications = blue_h.DEFAULT_NOTIFICATION_LOGGING;
      blue.log_event_handlers = blue_h.DEFAULT_LOG_EVENT_HANDLERS;
      blue.log_external_commands = blue_h.DEFAULT_LOG_EXTERNAL_COMMANDS;
      blue.log_passive_checks = blue_h.DEFAULT_LOG_PASSIVE_CHECKS;

      blue.logging_options = blue_h.NSLOG_RUNTIME_ERROR | blue_h.NSLOG_RUNTIME_WARNING
            | blue_h.NSLOG_VERIFICATION_ERROR | blue_h.NSLOG_VERIFICATION_WARNING | blue_h.NSLOG_CONFIG_ERROR
            | blue_h.NSLOG_CONFIG_WARNING | blue_h.NSLOG_PROCESS_INFO | blue_h.NSLOG_HOST_NOTIFICATION
            | blue_h.NSLOG_SERVICE_NOTIFICATION | blue_h.NSLOG_EVENT_HANDLER | blue_h.NSLOG_EXTERNAL_COMMAND
            | blue_h.NSLOG_PASSIVE_CHECK | blue_h.NSLOG_HOST_UP | blue_h.NSLOG_HOST_DOWN
            | blue_h.NSLOG_HOST_UNREACHABLE | blue_h.NSLOG_SERVICE_OK | blue_h.NSLOG_SERVICE_WARNING
            | blue_h.NSLOG_SERVICE_UNKNOWN | blue_h.NSLOG_SERVICE_CRITICAL | blue_h.NSLOG_INFO_MESSAGE;
      blue.syslog_options = blue_h.NSLOG_RUNTIME_ERROR | blue_h.NSLOG_RUNTIME_WARNING
            | blue_h.NSLOG_VERIFICATION_ERROR | blue_h.NSLOG_VERIFICATION_WARNING | blue_h.NSLOG_CONFIG_ERROR
            | blue_h.NSLOG_CONFIG_WARNING | blue_h.NSLOG_PROCESS_INFO | blue_h.NSLOG_HOST_NOTIFICATION
            | blue_h.NSLOG_SERVICE_NOTIFICATION | blue_h.NSLOG_EVENT_HANDLER | blue_h.NSLOG_EXTERNAL_COMMAND
            | blue_h.NSLOG_PASSIVE_CHECK | blue_h.NSLOG_HOST_UP | blue_h.NSLOG_HOST_DOWN
            | blue_h.NSLOG_HOST_UNREACHABLE | blue_h.NSLOG_SERVICE_OK | blue_h.NSLOG_SERVICE_WARNING
            | blue_h.NSLOG_SERVICE_UNKNOWN | blue_h.NSLOG_SERVICE_CRITICAL | blue_h.NSLOG_INFO_MESSAGE;

      blue.service_check_timeout = blue_h.DEFAULT_SERVICE_CHECK_TIMEOUT;
      blue.host_check_timeout = blue_h.DEFAULT_HOST_CHECK_TIMEOUT;
      blue.event_handler_timeout = blue_h.DEFAULT_EVENT_HANDLER_TIMEOUT;
      blue.notification_timeout = blue_h.DEFAULT_NOTIFICATION_TIMEOUT;
      blue.ocsp_timeout = blue_h.DEFAULT_OCSP_TIMEOUT;
      blue.ochp_timeout = blue_h.DEFAULT_OCHP_TIMEOUT;

      blue.sleep_time = blue_h.DEFAULT_SLEEP_TIME;
      blue.interval_length = blue_h.DEFAULT_INTERVAL_LENGTH;
      blue.service_inter_check_delay_method = blue_h.ICD_SMART;
      blue.host_inter_check_delay_method = blue_h.ICD_SMART;
      blue.service_interleave_factor_method = blue_h.ILF_SMART;
      blue.max_service_check_spread = blue_h.DEFAULT_SERVICE_CHECK_SPREAD;
      blue.max_host_check_spread = blue_h.DEFAULT_HOST_CHECK_SPREAD;

      blue.use_aggressive_host_checking = blue_h.DEFAULT_AGGRESSIVE_HOST_CHECKING;

      blue.soft_state_dependencies = common_h.FALSE;

      blue.retain_state_information = common_h.FALSE;
      blue.retention_update_interval = blue_h.DEFAULT_RETENTION_UPDATE_INTERVAL;
      blue.use_retained_program_state = common_h.TRUE;
      blue.use_retained_scheduling_info = common_h.FALSE;
      blue.retention_scheduling_horizon = blue_h.DEFAULT_RETENTION_SCHEDULING_HORIZON;
      blue.modified_host_process_attributes = common_h.MODATTR_NONE;
      blue.modified_service_process_attributes = common_h.MODATTR_NONE;

      blue.command_check_interval = blue_h.DEFAULT_COMMAND_CHECK_INTERVAL;
      blue.service_check_reaper_interval = blue_h.DEFAULT_SERVICE_REAPER_INTERVAL;
      blue.service_freshness_check_interval = blue_h.DEFAULT_FRESHNESS_CHECK_INTERVAL;
      blue.host_freshness_check_interval = blue_h.DEFAULT_FRESHNESS_CHECK_INTERVAL;
      blue.auto_rescheduling_interval = blue_h.DEFAULT_AUTO_RESCHEDULING_INTERVAL;
      blue.auto_rescheduling_window = blue_h.DEFAULT_AUTO_RESCHEDULING_WINDOW;

      blue.check_external_commands = blue_h.DEFAULT_CHECK_EXTERNAL_COMMANDS;
      blue.check_orphaned_services = blue_h.DEFAULT_CHECK_ORPHANED_SERVICES;
      blue.check_service_freshness = blue_h.DEFAULT_CHECK_SERVICE_FRESHNESS;
      blue.check_host_freshness = blue_h.DEFAULT_CHECK_HOST_FRESHNESS;
      blue.auto_reschedule_checks = blue_h.DEFAULT_AUTO_RESCHEDULE_CHECKS;

      blue.log_rotation_method = common_h.LOG_ROTATION_NONE;

      blue.last_command_check = 0L;
      blue.last_command_status_update=0L;
      blue.last_log_rotation = 0L;

      blue.max_parallel_service_checks = blue_h.DEFAULT_MAX_PARALLEL_SERVICE_CHECKS;
      blue.currently_running_service_checks = 0;

      blue.enable_notifications = common_h.TRUE;
      blue.execute_service_checks = common_h.TRUE;
      blue.accept_passive_service_checks = common_h.TRUE;
      blue.execute_host_checks = common_h.TRUE;
      blue.accept_passive_service_checks = common_h.TRUE;
      blue.enable_event_handlers = common_h.TRUE;
      blue.obsess_over_services = common_h.FALSE;
      blue.obsess_over_hosts = common_h.FALSE;
      blue.enable_failure_prediction = common_h.TRUE;

      blue.aggregate_status_updates = common_h.TRUE;
      blue.status_update_interval = blue_h.DEFAULT_STATUS_UPDATE_INTERVAL;

      blue.event_broker_options = broker_h.BROKER_NOTHING;

      blue.time_change_threshold = blue_h.DEFAULT_TIME_CHANGE_THRESHOLD;

      blue.enable_flap_detection = blue_h.DEFAULT_ENABLE_FLAP_DETECTION;
      blue.low_service_flap_threshold = blue_h.DEFAULT_LOW_SERVICE_FLAP_THRESHOLD;
      blue.high_service_flap_threshold = blue_h.DEFAULT_HIGH_SERVICE_FLAP_THRESHOLD;
      blue.low_host_flap_threshold = blue_h.DEFAULT_LOW_HOST_FLAP_THRESHOLD;
      blue.high_host_flap_threshold = blue_h.DEFAULT_HIGH_HOST_FLAP_THRESHOLD;

      blue.process_performance_data = blue_h.DEFAULT_PROCESS_PERFORMANCE_DATA;

      blue.date_format = common_h.DATE_FORMAT_US;

      for (int x = 0; x < blue_h.MACRO_X_COUNT; x++)
         blue.macro_x[x] = null;

      for (int x = 0; x < blue_h.MAX_COMMAND_ARGUMENTS; x++)
         blue.macro_argv[x] = null;

      for (int x = 0; x < blue_h.MAX_USER_MACROS; x++)
         blue.macro_user[x] = null;

      for (int x = 0; x < objects_h.MAX_CONTACT_ADDRESSES; x++)
         blue.macro_contactaddress[x] = null;

      blue.macro_ondemand = null;

      utils.init_macrox_names();

      blue.global_host_event_handler = null;
      blue.global_service_event_handler = null;

      blue.ocsp_command = null;
      blue.ochp_command = null;

      /* reset umask */
      // umask(S_IWGRP|S_IWOTH);
      logger.trace("exiting " + cn + ".reset_variables()");
      return common_h.OK;
   }

   public static int lock_file_exists()
   {
	   File lock = new File(blue.lock_file);
	   
	   if(lock.exists())
	   		   return common_h.OK;
		
	   return common_h.ERROR;
	   
   }
   public static int atoi(String value)
   {
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException nfE)
      {
         logger.error("warning: " + nfE.getMessage(), nfE);
         return 0;
      }
   }

   public static String[] getEnv(HashMap<String, String> envHashMap)
   {
      String[] result = new String[envHashMap.size()];

      int x = 0;
      for (Iterator<Map.Entry<String, String>> iter = envHashMap.entrySet().iterator(); iter.hasNext(); x++)
      {
         Map.Entry<String, String> e = iter.next();
         result[x] = e.getKey() + "=" + e.getValue();
      }
      return result;
   }
   
   /*
    * Simply trims a string - seems a little bit overkill?
    */
   public static String strip(String value)
   {
      if (value != null)
         value = value.trim();
      return value;
   }

   public static long strtoul(String value, Object ignore, int base)
   {
      try
      {
         return Long.parseLong(value);
      }
      catch (NumberFormatException nfE)
      {
         logger.error("warning: " + nfE.getMessage(), nfE);
         return 0L;
      }
   }
   
   public static long currentTimeInSeconds() {
      return System.currentTimeMillis()/1000;
   }

   public static long getTimeInSeconds(Calendar t) {
      return t.getTimeInMillis()/1000;
   }
   
   /** 
    * Parses a command like string converting into an array of command parameters.
    * Supports basic "java -jar x.jar -s "test me" -x file" would be 7 parameters
    * java, -jar, x.jar, -s, test me, -x, file
    */
   public static String[] processCommandLine( String command )  {
      ArrayList<String> list = new ArrayList<String>();
      String pattern = "\"([^\"]+?)\" ?|([^ ]+) ?| ";
      Pattern cliRE = Pattern.compile( pattern );
      Matcher m = cliRE.matcher( command );
      while ( m.find() ) {
         String match = m.group();
         if (match == null)
           break;
         if (match.endsWith(" ")) {  // trim trailing ,
           match = match.substring(0, match.length() - 1);
         }
         if (match.startsWith("\"")) { // assume also ends with
           match = match.substring(1, match.length() - 1);
         }
         if (match.length() != 0)
            list.add(match);
      }
      
      return list.toArray(new String[list.size()]);
   }
   
   /* Method to recursively copy a directory and it's contents */
   public static int copyDirectory(File srcDir,File destDir)
   {
	   /* Check to see if it is a directory */
	   if(!srcDir.isDirectory())
		   return common_h.ERROR;
	      
	   if(!destDir.canWrite())
	      return common_h.ERROR;
	   
	   /* Grab the initial directory contents */
	   String[] contents = srcDir.list();
       
	   for (int i=0; i<contents.length; i++)
	   {
           /* If Any of the contents are a directory, recursively call this method */
		   if(new File(srcDir.getAbsolutePath(),contents[i]).isDirectory())
        	   copyDirectory(new File(srcDir.getAbsolutePath(),contents[i]),new File(destDir.getAbsolutePath(),contents[i]));
           else
           {
        	   try
        	   {
        		   InputStream in = new FileInputStream(srcDir.getAbsolutePath() + "/"  + contents[i]);
        		   OutputStream out = new FileOutputStream(destDir.getAbsolutePath() + "/" + contents[i]);
       
        		   byte[] buf = new byte[1024];
        		   int len;
        		   while ((len = in.read(buf)) > 0)
        		   {
        			   out.write(buf, 0, len);
        		   }
        		   in.close();
        		   out.close();
        	   }
        	   catch(Exception e)
        	   {
        		   return common_h.ERROR;
        	   }
           }
	   }
	   
	   return common_h.OK;
   }
   
   /* Method for essentially emulating sed, replaces an old string with the new one in the given file */
   public static int replaceString(String oldString, String newString,File filename) throws IOException
   {
	   String line;
	   StringBuffer buffer = new StringBuffer();
	   
	   /* convert line to Unix format */
	   newString = newString.replace("\\","/");
	   
	   	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			
	   	while((line = reader.readLine()) != null)
		{
			line = line.replace(oldString,newString);
			buffer.append(line+"\n");
		}
		
		reader.close();
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		out.write(buffer.toString());
		out.close();
				
	   return common_h.OK;
   }
   
   /* Method to provide lock files for the config tool */
   public static int lockConfigTool()
   {
	   BufferedWriter out;
	   String installDir = System.getProperty("user.dir");
	   
	   /* Let's keep everything in one format for display purposes */
	   installDir = installDir.replace("\\","/");
	   
	   /* The more I look at this, the more I realise the need for a new way to lock the config tool */
	   try
	   {
		   out = new BufferedWriter(new FileWriter(installDir + "/blueconfig.log"));
		   out.write(installDir + "/etc/basic");
		   out.close();
		   
		   out = new BufferedWriter(new FileWriter(installDir + "/blueconfig.lock"));
		   out.write("New Config");
		   out.close();
	   }
	   catch(Exception e)
	   {
		   return common_h.ERROR;
	   }
	   
	   return common_h.OK;
   }
}
   

