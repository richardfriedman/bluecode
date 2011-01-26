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

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class checks
{

   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.base.checks");

   private static String cn = "org.blue.base.notifications";

   /******************************************************************/
   /****************** SERVICE MONITORING FUNCTIONS ******************/
   /******************************************************************/

   /**
    *  forks a child process to run a service check, but does not wait for the service check result.
    *  Instead of forking child process, it spawns thread and continues on.
    *  
    *  @param svc service to run check for.
    */
   public static void run_service_check(objects_h.service svc)
   {
      String raw_command;
      String processed_command;
      int check_service = common_h.TRUE;
      long current_time;
      long preferred_time = 0L;
      long next_valid_time;
      objects_h.host temp_host = null;
      int time_is_valid = common_h.TRUE;

      /* get the current time */
      current_time = utils.currentTimeInSeconds();

      /* if the service check is currently disabled... */
      if (svc.checks_enabled == common_h.FALSE)
      {

         /* don't check the service if we're not forcing it through */
         if (0 == (svc.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            check_service = common_h.FALSE;

         /* reschedule the service check */
         preferred_time = current_time + (svc.check_interval * blue.interval_length);
      }

      /* if the service check should not be checked on a regular interval... */
      if (svc.check_interval == 0)
      {

         /* don't check the service if we're not forcing it through */
         if (0 == (svc.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            check_service = common_h.FALSE;

         /* don't reschedule the service check */
         svc.should_be_scheduled = common_h.FALSE;
      }

      /* make sure this is a valid time to check the service */
      if (utils.check_time_against_period(current_time, svc.check_period) == common_h.ERROR)
      {

         /* don't check the service if we're not forcing it through */
         if (0 == (svc.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            check_service = common_h.FALSE;

         /* get the next valid time we can run the check */
         preferred_time = current_time;

         /* set the invalid time flag */
         time_is_valid = common_h.FALSE;
      }

      /* check service dependencies for execution */
      if (check_service_dependencies(svc, common_h.EXECUTION_DEPENDENCY) == blue_h.DEPENDENCIES_FAILED)
      {

         /* don't check the service if we're not forcing it through */
         if (0 == (svc.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            check_service = common_h.FALSE;

         /* reschedule the service check */
         preferred_time = current_time + (svc.check_interval * blue.interval_length);
      }

      /* clear the force execution flag */
      if ((svc.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0)
         svc.check_options -= blue_h.CHECK_OPTION_FORCE_EXECUTION;

      /* find the host associated with this service */
      temp_host = objects.find_host(svc.host_name);

      /* don't check the service if we couldn't find the associated host */
      if (temp_host == null)
         check_service = common_h.FALSE;

      /* if we shouldn't check the service, just reschedule it and leave... */
      if (check_service == common_h.FALSE)
      {

         /* only attempt to (re)schedule checks that should get checked... */
         if (svc.should_be_scheduled == common_h.TRUE)
         {

            /* make sure we rescheduled the next service check at a valid time */
            next_valid_time = utils.get_next_valid_time(preferred_time, svc.check_period);

            /* the service could not be rescheduled properly - set the next check time for next year, but don't actually reschedule it */
            if (time_is_valid == common_h.FALSE && next_valid_time == preferred_time)
            {

               svc.next_check = (next_valid_time + (60 * 60 * 24 * 365));
               svc.should_be_scheduled = common_h.FALSE;
               logger.debug("Warning: Could not find any valid times to reschedule a check of service '"
                     + svc.description + "' on host '" + svc.host_name + "'!");
            }

            /* this service could be rescheduled... */
            else
            {
               svc.next_check = next_valid_time;
               svc.should_be_scheduled = common_h.TRUE;
            }
         }

         /* update the status log with the current service */
         statusdata.update_service_status(svc, common_h.FALSE);

         /* reschedule the next service check - unless we couldn't find a valid next check time */
         if (svc.should_be_scheduled == common_h.TRUE)
            schedule_service_check(svc, svc.next_check, common_h.FALSE);

         return;
      }

      /**** ELSE RUN THE SERVICE CHECK ****/

      logger.debug("\tChecking service '" + svc.description + "' on host '" + svc.host_name + "'...");

      /* increment number of parallel service checks currently out there... */
      blue.currently_running_service_checks++;

      /* set a flag if this service check shouldn't be parallelized with others... */
      if (svc.parallelize == common_h.FALSE)
         blue.non_parallelized_check_running = common_h.TRUE;

      /* set the execution flag */
      svc.is_executing = common_h.TRUE;

      /* grab the host and service macro variables */
      utils.clear_volatile_macros();
      utils.grab_host_macros(temp_host);
      utils.grab_service_macros(svc);
      utils.grab_summary_macros(null);

      /* get the raw command line */
      raw_command = utils.get_raw_command_line(svc.service_check_command, 0);
      raw_command = utils.strip(raw_command);

      /* process any macros contained in the argument */
      processed_command = utils.process_macros(raw_command, 0);
      processed_command = utils.strip(processed_command);

      /* save service info */
      blue_h.service_message svc_msg = new blue_h.service_message();
      blue.svc_msg = svc_msg;
      svc_msg.host_name = svc.host_name;
      svc_msg.description = svc.description;
      svc_msg.parallelized = svc.parallelize;

      /* send data to event broker */
      broker.broker_service_check( broker_h.NEBTYPE_SERVICECHECK_INITIATE, broker_h.NEBFLAG_NONE, broker_h.NEBATTR_NONE, svc, common_h.SERVICE_CHECK_ACTIVE, new blue_h.timeval(), new blue_h.timeval(0,0), svc.service_check_command, svc.latency,0.0,0,common_h.FALSE,0,processed_command,null);

      /* Run the SERVICE CHECK in it's own thread away from everyone else */
      Thread t = new service_exec_worker_thread(processed_command, svc_msg);
      t.setName("Service Exec Worker Thread");
      t.start();
   }

   /* reaps service check results */
   public static void reap_service_checks()
   {
      blue_h.service_message queued_svc_msg;
      objects_h.service temp_service = null;
      objects_h.host temp_host = null;
      long preferred_time;
      long next_valid_time;
      String temp_buffer;
      int state_change = common_h.FALSE;
      int hard_state_change = common_h.FALSE;
      int route_result = blue_h.HOST_UP;
      //	int dependency_result=nagios_h.DEPENDENCIES_OK;
      long current_time;
      //	int first_check=common_h.FALSE;
      int state_was_logged = common_h.FALSE;
      String old_plugin_output = "";
      String temp_plugin_output = "";
      String temp_ptr;
      long reaper_start_time;
      blue_h.timeval tv;

      logger.trace("entering " + cn + ".reap_service_checks");

      logger.debug("Starting to reap service check results...\n");

      reaper_start_time = utils.currentTimeInSeconds();

      /* read all service checks results that have come in... */

      while ((queued_svc_msg = utils.read_svc_message()) != null)
      {

         /* make sure we really have something... */
         if (queued_svc_msg.description.length() == 0 && queued_svc_msg.host_name.length() == 0)
         {
            logger.debug("Found an empty message in service result pipe!\n");
            continue;
         }

         /* get the current time */
         current_time = utils.currentTimeInSeconds();

         /* skip this service check results if its passive and we aren't accepting passive check results */
         if (blue.accept_passive_service_checks == common_h.FALSE
               && queued_svc_msg.check_type == common_h.SERVICE_CHECK_PASSIVE)
            continue;

         /* because of my idiotic idea of having UNKNOWN states be equivalent to -1, I must hack things a bit... */
         if (queued_svc_msg.return_code == 255 || queued_svc_msg.return_code == -1)
            queued_svc_msg.return_code = blue_h.STATE_UNKNOWN;

         /* find the service */
         temp_service = objects.find_service(queued_svc_msg.host_name, queued_svc_msg.description);
         if (temp_service == null)
         {
            temp_buffer = "Warning:  Message queue contained results for service '" + queued_svc_msg.description
                  + "' on host '" + queued_svc_msg.host_name + "'.  The service could not be found!";
            logger.info(temp_buffer);

            continue;
         }

         /* calculate passive check latency */
         if (queued_svc_msg.check_type == common_h.SERVICE_CHECK_PASSIVE)
         {
            tv = new blue_h.timeval();
            // TODO question the usage of u_sec and proper division.
            temp_service.latency = ((tv.tv_sec - queued_svc_msg.finish_time.tv_sec) + ((tv.tv_usec - queued_svc_msg.finish_time.tv_usec) / 1000.0));
            if (temp_service.latency < 0.0)
               temp_service.latency = 0.0;
         }

         /* update the execution time for this check (millisecond resolution) */
         temp_service.execution_time = ((queued_svc_msg.finish_time.tv_sec - queued_svc_msg.start_time.tv_sec) + ((queued_svc_msg.finish_time.tv_usec - queued_svc_msg.start_time.tv_usec) / 1000) / 1000.0);

         //#ifdef REMOVED_050803
         //		if(queued_svc_msg.start_time.time>current_time || queued_svc_msg.finish_time.time>current_time || (queued_svc_msg.finish_time.time<queued_svc_msg.start_time.time))
         //			temp_service.execution_time=0.0;
         //		else
         //			temp_service.execution_time=(double)((double)(queued_svc_msg.finish_time.time-queued_svc_msg.start_time.time)+(double)((queued_svc_msg.finish_time.millitm-queued_svc_msg.start_time.millitm)/*/1000.0*/));
         //#endif

         /* clear the freshening flag (it would have been set if this service was determined to be stale) */
         temp_service.is_being_freshened = common_h.FALSE;

         /* ignore passive service check results if we're not accepting them for this service */
         if (temp_service.accept_passive_service_checks == common_h.FALSE
               && queued_svc_msg.check_type == common_h.SERVICE_CHECK_PASSIVE)
            continue;

         logger.debug("\n\tFound check result for service '" + temp_service.description + "' on host '"
               + temp_service.host_name + "'");
         logger.debug("\t\tCheck Type:    %"
               + ((queued_svc_msg.check_type == common_h.SERVICE_CHECK_ACTIVE) ? "ACTIVE" : "PASSIVE"));
         logger.debug("\t\tParallelized?: " + ((queued_svc_msg.parallelized == common_h.TRUE) ? "Yes" : "No"));
         logger.debug("\t\tExited common_h.OK?:    " + ((queued_svc_msg.exited_ok == common_h.TRUE) ? "Yes" : "No"));
         logger.debug("\t\tReturn Status: " + queued_svc_msg.return_code);
         logger.debug("\t\tPlugin Output: '" + queued_svc_msg.output + "'");

         /* decrement the number of service checks still out there... */
         if (queued_svc_msg.check_type == common_h.SERVICE_CHECK_ACTIVE && blue.currently_running_service_checks > 0)
            blue.currently_running_service_checks--;

         /* if this check was not parallelized, clear the flag */
         if (queued_svc_msg.parallelized == common_h.FALSE
               && queued_svc_msg.check_type == common_h.SERVICE_CHECK_ACTIVE)
            blue.non_parallelized_check_running = common_h.FALSE;

         /* clear the execution flag if this was an active check */
         if (queued_svc_msg.check_type == common_h.SERVICE_CHECK_ACTIVE)
            temp_service.is_executing = common_h.FALSE;

         /* get the last check time */
         temp_service.last_check = queued_svc_msg.start_time.tv_sec;
         //#ifdef REMOVED_050803
         //		temp_service.last_check=queued_svc_msg.start_time.time;
         //#endif

         /* was this check passive or active? */
         temp_service.check_type = (queued_svc_msg.check_type == common_h.SERVICE_CHECK_ACTIVE)
               ? common_h.SERVICE_CHECK_ACTIVE
               : common_h.SERVICE_CHECK_PASSIVE;

         /* INITIALIZE VARIABLES FOR THIS SERVICE */
         state_change = common_h.FALSE;
         hard_state_change = common_h.FALSE;
         route_result = blue_h.HOST_UP;
         //		dependency_result=nagios_h.DEPENDENCIES_OK;
         //		first_check=common_h.FALSE;
         state_was_logged = common_h.FALSE;
         old_plugin_output = "";
         temp_plugin_output = "";

         /* save the old service status info */
         temp_service.last_state = temp_service.current_state;

         /* save old plugin output */
         old_plugin_output = ((temp_service.plugin_output == null) ? "" : temp_service.plugin_output);

         /* clear the old plugin output and perf data buffers */
         temp_service.plugin_output = "";
         temp_service.perf_data = "";

         /* check for empty plugin output */
         if (temp_plugin_output.length() == 0)
            temp_plugin_output = "(No output returned from plugin)";

         /* get performance data (if it exists) */
         temp_plugin_output = queued_svc_msg.output;
         String[] split = temp_plugin_output.split("[|\n]");
         temp_ptr = getSplit(split, 1);
         if (temp_ptr != null)
         {
            temp_ptr = utils.strip(temp_ptr);
            temp_service.perf_data = temp_ptr;
         }

         /* get status data - everything before pipe sign */
         temp_plugin_output = queued_svc_msg.output;
         temp_ptr = split[0];

         /* if there was some error running the command, just skip it (this shouldn't be happening) */
         if (queued_svc_msg.exited_ok == common_h.FALSE)
         {

            temp_buffer = "Warning:  Check of service '" + temp_service.description + "' on host '"
                  + temp_service.host_name + "' did not exit properly!";
            logger.info(temp_buffer);

            temp_service.plugin_output = "(Service check did not exit properly)";

            temp_service.current_state = blue_h.STATE_CRITICAL;
         }

         /* make sure the return code is within bounds */
         else if (queued_svc_msg.return_code < 0 || queued_svc_msg.return_code > 3)
         {

            temp_buffer = "Warning: Return code of "
                  + queued_svc_msg.return_code
                  + " for check of service '"
                  + temp_service.description
                  + "' on host '"
                  + temp_service.host_name
                  + "' was out of bounds."
                  + ((queued_svc_msg.return_code == 126 || queued_svc_msg.return_code == 127)
                        ? " Make sure the plugin you're trying to run actually exists."
                        : "");
            logger.info(temp_buffer);

            temp_service.plugin_output = "(Return code of "
                  + queued_svc_msg.return_code
                  + " is out of bounds"
                  + ((queued_svc_msg.return_code == 126 || queued_svc_msg.return_code == 127)
                        ? " - plugin may be missing"
                        : "") + ")";
            temp_service.current_state = blue_h.STATE_CRITICAL;
         }

         /* else the return code is okay... */
         else
         {

            /* make sure the plugin output isn't null */
            if (temp_ptr == null)
            {
               temp_service.plugin_output = "(No output returned from plugin)";
            }

            /* grab the plugin output */
            else
            {

               temp_ptr = utils.strip(temp_ptr);
               if (temp_ptr.length() == 0)
                  temp_service.plugin_output = "(No output returned from plugin)";
               else
                  temp_service.plugin_output = temp_ptr;
            }

            /* replace semicolons in plugin output (but not performance data) with colons */
            temp_ptr = temp_service.plugin_output.replace(";", ":");

            /* grab the return code */
            temp_service.current_state = queued_svc_msg.return_code;
         }

         /* record the last state time */
         switch (temp_service.current_state)
         {
            case blue_h.STATE_OK :
               temp_service.last_time_ok = temp_service.last_check;
               break;
            case blue_h.STATE_WARNING :
               temp_service.last_time_warning = temp_service.last_check;
               break;
            case blue_h.STATE_UNKNOWN :
               temp_service.last_time_unknown = temp_service.last_check;
               break;
            case blue_h.STATE_CRITICAL :
               temp_service.last_time_critical = temp_service.last_check;
               break;
            default :
               break;
         }

         /* get the host that this service runs on */
         temp_host = objects.find_host(temp_service.host_name);

         /* if the service check was okay... */
         if (temp_service.current_state == blue_h.STATE_OK)
         {

            /* if the host has never been checked before... */
            if (temp_host.has_been_checked == common_h.FALSE)
            {

               /* verify the host status */
               verify_route_to_host(temp_host, blue_h.CHECK_OPTION_NONE);

               //#ifdef REMOVED_080303
               //				/* really check the host status if we're using aggressive host checking */
               //				if(use_aggressive_host_checking==common_h.TRUE)
               //					verify_route_to_host(temp_host,CHECK_OPTION_NONE);
               //
               //				/* else let's just assume the host is up... */
               //				else{
               //
               //					/* set the checked flag */
               //					temp_host.has_been_checked=common_h.TRUE;
               //				
               //					/* update the last check time */
               //					temp_host.last_check=temp_service.last_check;
               //
               //					/* set the host state and check types */
               //					temp_host.current_state=nagios_h.HOST_UP;
               //					temp_host.current_attempt=1;
               //					temp_host.state_type=common_h.HARD_STATE;
               //					temp_host.check_type=HOST_CHECK_ACTIVE;
               //					
               //					/* plugin output should reflect our guess at the current host state */
               //					temp_host.plugin_output = "(Host assumed to be up)",MAX_PLUGINOUTPUT_LENGTH);
               //					temp_host.plugin_output[MAX_PLUGINOUTPUT_LENGTH-1]='\x0';
               //
               //					/* should we be calling handle_host_state() here?  probably not, but i'm not sure at the present time - 02/18/03 */
               //
               //					/* update the status log with the host status */
               //					statusdata.update_host_status(temp_host,common_h.FALSE);
               //
               //#ifdef REMOVED_042903
               //					/* log the initial state if the user wants */
               //					if(log_initial_states==common_h.TRUE)
               //						log_host_event(temp_host);
               //#endif
               //				        }
               //#endif
            }

            //#ifdef REMOVED_042903
            //			/* log the initial state if the user wants */
            //			if(temp_service.has_been_checked==common_h.FALSE && log_initial_states==common_h.TRUE){
            //				log_service_event(temp_service);
            //				state_was_logged=common_h.TRUE;
            //			        }
            //#endif
         }

         /**** NOTE - THIS WAS MOVED UP FROM LINE 1049 BELOW TO FIX PROBLEMS WHERE CURRENT ATTEMPT VALUE WAS ACTUALLY "LEADING" REAL VALUE ****/
         /* increment the current attempt number if this is a soft state (service was rechecked) */
         if (temp_service.state_type == common_h.SOFT_STATE
               && (temp_service.current_attempt < temp_service.max_attempts))
            temp_service.current_attempt = temp_service.current_attempt + 1;

         logger.debug("SERVICE '" + temp_service.description + "' on HOST '" + temp_service.host_name + "'\n");
         logger.debug(new Date(temp_service.last_check*1000).toString());
         logger.debug("\tST: " + temp_service.current_attempt + "  CA: " + temp_service.max_attempts + "  MA: "
               + ((temp_service.state_type == common_h.SOFT_STATE) ? "SOFT" : "HARD") + "  CS: "
               + temp_service.current_state + "  LS: " + temp_service.last_state + "  LHS: "
               + temp_service.last_hard_state);

         /* check for a state change (either soft or hard) */
         if (temp_service.current_state != temp_service.last_state)
         {
            logger.debug("\t\tService '" + temp_service.description + "' on host '" + temp_service.host_name
                  + "' has changed state since last check!");
            state_change = common_h.TRUE;
            logger.debug("\tSTATE CHANGE\n");
         }

         /* checks for a hard state change where host was down at last service check */
         /* this occurs in the case where host goes down and service current attempt gets reset to 1 */
         /* if this check is not made, the service recovery looks like a soft recovery instead of a hard one */
         if (temp_service.host_problem_at_last_check == common_h.TRUE
               && temp_service.current_state == blue_h.STATE_OK)
         {
            logger.debug("\t\tService '" + temp_service.description + "' on host '" + temp_service.host_name
                  + "' has had a HARD STATE CHANGE!!");
            hard_state_change = common_h.TRUE;
            logger.debug("\tHARD STATE CHANGE A\n");
         }

         /* check for a "normal" hard state change where max check attempts is reached */
         if (temp_service.current_attempt >= temp_service.max_attempts
               && temp_service.current_state != temp_service.last_hard_state)
         {
            logger.debug("\t\tService '" + temp_service.description + "' on host '" + temp_service.host_name
                  + "' has had a HARD STATE CHANGE!!");
            hard_state_change = common_h.TRUE;
            logger.debug("\tHARD STATE CHANGE B\n");
         }

         /* reset last and next notification times and acknowledgement flag if necessary */
         if (state_change == common_h.TRUE || hard_state_change == common_h.TRUE)
         {

            /* reset notification times */
            temp_service.last_notification = 0;
            temp_service.next_notification = 0;

            /* reset notification suppression option */
            temp_service.no_more_notifications = common_h.FALSE;

            if (temp_service.acknowledgement_type == common_h.ACKNOWLEDGEMENT_NORMAL)
            {
               temp_service.problem_has_been_acknowledged = common_h.FALSE;
               temp_service.acknowledgement_type = common_h.ACKNOWLEDGEMENT_NONE;
            }
            else if (temp_service.acknowledgement_type == common_h.ACKNOWLEDGEMENT_STICKY
                  && temp_service.current_state == blue_h.STATE_OK)
            {
               temp_service.problem_has_been_acknowledged = common_h.FALSE;
               temp_service.acknowledgement_type = common_h.ACKNOWLEDGEMENT_NONE;
            }

            /* do NOT reset current notification number!!! */
            /* hard changes between non-common_h.OK states should continue to be escalated, so don't reset current notification number */
            /*temp_service.current_notification_number=0;*/
         }

         /* initialize the last host and service state change times if necessary */
         if (temp_service.last_state_change == 0)
            temp_service.last_state_change = temp_service.last_check;
         if (temp_service.last_hard_state_change == 0)
            temp_service.last_hard_state_change = temp_service.last_check;
         if (temp_host.last_state_change == 0)
            temp_host.last_state_change = temp_service.last_check;
         if (temp_host.last_hard_state_change == 0)
            temp_host.last_hard_state_change = temp_service.last_check;

         /* update last service state change times */
         if (state_change == common_h.TRUE)
            temp_service.last_state_change = temp_service.last_check;
         if (hard_state_change == common_h.TRUE)
            temp_service.last_hard_state_change = temp_service.last_check;

         /**************************************/
         /******* SERVICE CHECK common_h.OK LOGIC *******/
         /**************************************/

         /* if the service is up and running common_h.OK... */
         if (temp_service.current_state == blue_h.STATE_OK)
         {

            /* reset the acknowledgement flag (this should already have been done, but just in case...) */
            temp_service.problem_has_been_acknowledged = common_h.FALSE;
            temp_service.acknowledgement_type = common_h.ACKNOWLEDGEMENT_NONE;

            logger.debug("\tOriginally common_h.OK\n");

            /* the service check was okay, so the associated host must be up... */
            if (temp_host.current_state != blue_h.HOST_UP)
            {
               logger.debug("\tSECTION A1\n");

               /* verify the route to the host and send out host recovery notifications */
               verify_route_to_host(temp_host, blue_h.CHECK_OPTION_NONE);

               //#ifdef REMOVED_041403
               //				/* set the host problem flag (i.e. don't notify about recoveries for this service) */
               //				temp_service.host_problem_at_last_check=common_h.TRUE;
               //#endif
            }

            /* if a hard service recovery has occurred... */
            if (hard_state_change == common_h.TRUE)
            {
               logger.debug("\tSECTION A2\n");

               /* set the state type macro */
               temp_service.state_type = common_h.HARD_STATE;

               /* log the service recovery */
               logging.log_service_event(temp_service);
               state_was_logged = common_h.TRUE;

               /* notify contacts about the service recovery */
               notifications.service_notification(temp_service, blue_h.NOTIFICATION_NORMAL, null, null);

               /* run the service event handler to handle the hard state change */
               sehandlers.handle_service_event(temp_service);
            }

            /* else if a soft service recovery has occurred... */
            else if (state_change == common_h.TRUE)
            {
               logger.debug("\tSECTION A3\n");

               /* this is a soft recovery */
               temp_service.state_type = common_h.SOFT_STATE;

               /* log the soft recovery */
               logging.log_service_event(temp_service);
               state_was_logged = common_h.TRUE;

               /* run the service event handler to handle the soft state change */
               sehandlers.handle_service_event(temp_service);
            }

            /* else no service state change has occurred... */

            /* should we obsessive over service checks? */
            if (blue.obsess_over_services == common_h.TRUE)
               sehandlers.obsessive_compulsive_service_check_processor(temp_service);

            /* reset all service variables because its okay now... */
            temp_service.host_problem_at_last_check = common_h.FALSE;
            //#ifdef REMOVED_041403
            //			temp_service.no_recovery_notification=common_h.FALSE;
            //#endif
            temp_service.current_attempt = 1;
            temp_service.state_type = common_h.HARD_STATE;
            temp_service.last_hard_state = blue_h.STATE_OK;
            temp_service.last_notification = 0;
            temp_service.next_notification = 0;
            temp_service.current_notification_number = 0;
            temp_service.problem_has_been_acknowledged = common_h.FALSE;
            temp_service.acknowledgement_type = common_h.ACKNOWLEDGEMENT_NONE;
            temp_service.notified_on_unknown = common_h.FALSE;
            temp_service.notified_on_warning = common_h.FALSE;
            temp_service.notified_on_critical = common_h.FALSE;
            temp_service.no_more_notifications = common_h.FALSE;

            if (temp_service.check_type == common_h.SERVICE_CHECK_ACTIVE)
               temp_service.next_check = (temp_service.last_check + (temp_service.check_interval * blue.interval_length));
         }

         /*******************************************/
         /******* SERVICE CHECK PROBLEM LOGIC *******/
         /*******************************************/

         /* hey, something's not working quite like it should... */
         else
         {
            logger.debug("\tOriginally PROBLEM\n");

            //#ifdef REMOVED_041403
            //			/* reset the recovery notification flag (it may get set again though) */
            //			temp_service.no_recovery_notification=common_h.FALSE;
            //#endif

            /* check the route to the host if its supposed to be up right now... */
            if (temp_host.current_state == blue_h.HOST_UP)
            {
               route_result = verify_route_to_host(temp_host, blue_h.CHECK_OPTION_NONE);
               logger.debug("\tSECTION B1\n");
            }

            /* else the host is either down or unreachable, so recheck it if necessary */
            else
            {
               logger.debug("\tSECTION B2a\n");

               /* we're using aggressive host checking, so really do recheck the host... */
               if (blue.use_aggressive_host_checking == common_h.TRUE)
               {
                  route_result = verify_route_to_host(temp_host, blue_h.CHECK_OPTION_NONE);
                  logger.debug("\tSECTION B2b\n");
               }

               /* the service wobbled between non-common_h.OK states, so check the host... */
               else if (state_change == common_h.TRUE && temp_service.last_hard_state != blue_h.STATE_OK)
               {
                  route_result = verify_route_to_host(temp_host, blue_h.CHECK_OPTION_NONE);
                  logger.debug("\tSECTION B2c\n");
               }

               /* else fake the host check, but (possibly) resend host notifications to contacts... */
               else
               {
                  logger.debug("\tSECTION B2d\n");

                  //#ifdef REMOVED_042903
                  //					/* log the initial state if the user wants to and this host hasn't been checked yet */
                  //					if(log_initial_states==common_h.TRUE && temp_host.has_been_checked==common_h.FALSE)
                  //						log_host_event(temp_host);
                  //#endif

                  /* if the host has never been checked before, set the checked flag */
                  if (temp_host.has_been_checked == common_h.FALSE)
                     temp_host.has_been_checked = common_h.TRUE;

                  /* update the last host check time */
                  temp_host.last_check = temp_service.last_check;

                  /* fake the route check result */
                  route_result = temp_host.current_state;

                  /* possibly re-send host notifications... */
                  notifications.host_notification(temp_host, blue_h.NOTIFICATION_NORMAL, null, null);
               }
            }

            /* if the host is down or unreachable ... */
            if (route_result != blue_h.HOST_UP)
            {
               logger.debug("\tSECTION B3\n");

               /* "fake" a hard state change for the service - well, its not really fake, but it didn't get caught earlier... */
               if (temp_service.last_hard_state != temp_service.current_state)
                  hard_state_change = common_h.TRUE;

               /* update last state change times */
               if (state_change == common_h.TRUE || hard_state_change == common_h.TRUE)
                  temp_service.last_state_change = temp_service.last_check;
               if (hard_state_change == common_h.TRUE)
                  temp_service.last_hard_state_change = temp_service.last_check;

               /* put service into a hard state without attempting check retries and don't send out notifications about it */
               temp_service.host_problem_at_last_check = common_h.TRUE;
               temp_service.state_type = common_h.HARD_STATE;
               temp_service.last_hard_state = temp_service.current_state;
               temp_service.current_attempt = 1;
            }

            /* the host is up - it recovered since the last time the service was checked... */
            else if (temp_service.host_problem_at_last_check == common_h.TRUE)
            {

               logger.debug("\tSECTION B4\n");

               /* next time the service is checked we shouldn't get into this same case... */
               temp_service.host_problem_at_last_check = common_h.FALSE;

               /* reset the current check counter, so we give the service a chance */
               /* this helps prevent the case where service has N max check attempts, N-1 of which have already occurred. */
               /* if we didn't do this, the next check might fail and result in a hard problem - we should really give it more time */
               /* ADDED IF STATEMENT 01-17-05 EG */
               /* 01-17-05: Services in hard problem states before hosts went down would sometimes come back as soft problem states after */
               /* the hosts recovered.  This caused problems, so hopefully this will fix it */
               if (temp_service.state_type == common_h.SOFT_STATE)
                  temp_service.current_attempt = 1;

               //#ifdef REMOVED_041403
               //				/* don't send a recovery notification if the service recovers at the next check */
               //				temp_service.no_recovery_notification=common_h.TRUE;
               //#endif
            }

            /* if we should retry the service check, do so (except it the host is down or unreachable!) */
            if (temp_service.current_attempt < temp_service.max_attempts)
            {
               logger.debug("\tSECTION B5a\n");

               /* the host is down or unreachable, so don't attempt to retry the service check */
               if (route_result != blue_h.HOST_UP)
               {

                  logger.debug("\tSECTION B5b\n");

                  /* the host is not up, so reschedule the next service check at regular interval */
                  if (temp_service.check_type == common_h.SERVICE_CHECK_ACTIVE)
                     temp_service.next_check = (temp_service.last_check + (temp_service.check_interval * blue.interval_length));

                  /* log the problem as a hard state if the host just went down */
                  if (hard_state_change == common_h.TRUE)
                  {
                     logging.log_service_event(temp_service);
                     state_was_logged = common_h.TRUE;
                  }
               }

               /* the host is up, so continue to retry the service check */
               else
               {

                  logger.debug("\tSECTION B5c\n");

                  /* this is a soft state */
                  temp_service.state_type = common_h.SOFT_STATE;

                  /* log the service check retry */
                  logging.log_service_event(temp_service);
                  state_was_logged = common_h.TRUE;

                  /* run the service event handler to handle the soft state */
                  sehandlers.handle_service_event(temp_service);

                  //#ifdef REMOVED_021803
                  //					/*** NOTE TO SELF - THIS SHOULD BE MOVED SOMEWHERE ELSE - 02/18/03 ***/
                  //					/*** MOVED UP TO ~ LINE 780 ***/
                  //					/* increment the current attempt number */
                  //					temp_service.current_attempt=temp_service.current_attempt+1;
                  //#endif

                  if (temp_service.check_type == common_h.SERVICE_CHECK_ACTIVE)
                     temp_service.next_check = (temp_service.last_check + (temp_service.retry_interval * blue.interval_length));
               }
            }

            /* we've reached the maximum number of service rechecks, so handle the error */
            else
            {

               logger.debug("\tSECTION B6a\n");
               logger.debug("\tMAXED OUT! HSC: " + hard_state_change);

               /* this is a hard state */
               temp_service.state_type = common_h.HARD_STATE;

               /* if we've hard a hard state change... */
               if (hard_state_change == common_h.TRUE)
               {
                  logger.debug("\tSECTION B6b\n");

                  /* log the service problem (even if host is not up, which is new in 0.0.5) */
                  logging.log_service_event(temp_service);
                  state_was_logged = common_h.TRUE;
               }

               /* else log the problem (again) if this service is flagged as being volatile */
               else if (temp_service.is_volatile == common_h.TRUE)
               {
                  logger.debug("\tSECTION B6c\n");

                  logging.log_service_event(temp_service);
                  state_was_logged = common_h.TRUE;
               }

               /* check for start of flexible (non-fixed) scheduled downtime if we just had a hard error */
               if (hard_state_change == common_h.TRUE && temp_service.pending_flex_downtime > 0)
                  downtime.check_pending_flex_service_downtime(temp_service);

               /* (re)send notifications out about this service problem if the host is up (and was at last check also) and the dependencies were okay... */
               notifications.service_notification(temp_service, blue_h.NOTIFICATION_NORMAL, null, null);

               /* run the service event handler if we changed state from the last hard state or if this service is flagged as being volatile */
               if (hard_state_change == common_h.TRUE || temp_service.is_volatile == common_h.TRUE)
               {
                  logger.debug("\tSECTION B6d\n");

                  sehandlers.handle_service_event(temp_service);
               }

               /* save the last hard state */
               temp_service.last_hard_state = temp_service.current_state;

               /* reschedule the next check at the regular interval */
               if (temp_service.check_type == common_h.SERVICE_CHECK_ACTIVE)
                  temp_service.next_check = (temp_service.last_check + (temp_service.check_interval * blue.interval_length));
            }

            /* should we obsessive over service checks? */
            if (blue.obsess_over_services == common_h.TRUE)
               sehandlers.obsessive_compulsive_service_check_processor(temp_service);
         }

         /* reschedule the next service check ONLY for active checks */
         if (temp_service.check_type == common_h.SERVICE_CHECK_ACTIVE)
         {

            /* default is to reschedule service check unless a test below fails... */
            temp_service.should_be_scheduled = common_h.TRUE;

            /* make sure we don't get ourselves into too much trouble... */
            if (current_time > temp_service.next_check)
               temp_service.next_check = current_time;

            /* make sure we rescheduled the next service check at a valid time */
            preferred_time = temp_service.next_check;
            next_valid_time = utils.get_next_valid_time(preferred_time, temp_service.check_period);
            temp_service.next_check = next_valid_time;

            /* services with non-recurring intervals do not get rescheduled */
            if (temp_service.check_interval == 0)
               temp_service.should_be_scheduled = common_h.FALSE;

            /* services with active checks disabled do not get rescheduled */
            if (temp_service.checks_enabled == common_h.FALSE)
               temp_service.should_be_scheduled = common_h.FALSE;

            /* schedule a non-forced check if we can */
            if (temp_service.should_be_scheduled == common_h.TRUE)
               schedule_service_check(temp_service, temp_service.next_check, common_h.FALSE);
         }

         logger.debug("\tDONE\n");

         /* if we're stalking this state type and state was not already logged AND the plugin output changed since last check, log it now.. */
         if (temp_service.state_type == common_h.HARD_STATE && state_change == common_h.FALSE
               && state_was_logged == common_h.FALSE && !old_plugin_output.equals(temp_service.plugin_output))
         {

            if ((temp_service.current_state == blue_h.STATE_OK && temp_service.stalk_on_ok == common_h.TRUE))
               logging.log_service_event(temp_service);

            else if ((temp_service.current_state == blue_h.STATE_WARNING && temp_service.stalk_on_warning == common_h.TRUE))
               logging.log_service_event(temp_service);

            else if ((temp_service.current_state == blue_h.STATE_UNKNOWN && temp_service.stalk_on_unknown == common_h.TRUE))
               logging.log_service_event(temp_service);

            else if ((temp_service.current_state == blue_h.STATE_CRITICAL && temp_service.stalk_on_critical == common_h.TRUE))
               logging.log_service_event(temp_service);
         }

        /* send data to event broker */
         broker.broker_service_check(broker_h.NEBTYPE_SERVICECHECK_PROCESSED,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_service,temp_service.check_type,queued_svc_msg.start_time,queued_svc_msg.finish_time,null,temp_service.latency,temp_service.execution_time,blue.service_check_timeout,(queued_svc_msg.early_timeout?common_h.TRUE:common_h.FALSE),queued_svc_msg.return_code,null,null);

         /* set the checked flag */
         temp_service.has_been_checked = common_h.TRUE;

         /* update the current service status log */
         statusdata.update_service_status(temp_service, common_h.FALSE);

         /* check to see if the service is flapping */
         flapping.check_for_service_flapping(temp_service, common_h.TRUE);

         /* check to see if the associated host is flapping */
         flapping.check_for_host_flapping(temp_host, common_h.TRUE);

         /* update service performance info */
         perfdata.update_service_performance_data(temp_service);

         /* break out if we've been here too long (max_check_reaper_time seconds) */
         current_time = utils.currentTimeInSeconds();
         if ((int) (current_time - reaper_start_time) > blue.max_check_reaper_time)
            break;

         //#if OLD_CRUD
         //		/* check for external commands if we're doing so as often as possible */
         //		if(command_check_interval==-1)
         //			check_for_external_commands();
         //#endif
      }

      logger.debug("Finished reaping service check results.\n");

      logger.trace("exiting" + cn + ".reap_service_checks");

      return;
   }

   /* schedules an immediate or delayed service check */
   public static void schedule_service_check(objects_h.service svc, long check_time, int forced)
   {
      blue_h.timed_event temp_event = null;
      blue_h.timed_event new_event;
      int found = common_h.FALSE;
      int use_original_event = common_h.TRUE;

      logger.trace("entering " + cn + ".schedule_service_check");

      /* don't schedule a check if active checks are disabled */
      if ((blue.execute_service_checks == common_h.FALSE || svc.checks_enabled == common_h.FALSE)
            && forced == common_h.FALSE)
         return;

      /* allocate memory for a new event item */
      new_event = new blue_h.timed_event();

      /* see if there are any other scheduled checks of this service in the queue */
      for (blue_h.timed_event iter_event : (ArrayList<blue_h.timed_event>) events.event_list_low)
      {

         if (iter_event.event_type == blue_h.EVENT_SERVICE_CHECK && svc == (objects_h.service) iter_event.event_data)
         {
            temp_event = iter_event;
            found = common_h.TRUE;
            break;
         }
      }

      /* we found another service check event for this service in the queue - what should we do? */
      if (found == common_h.TRUE && temp_event != null)
      {

         /* use the originally scheduled check unless we decide otherwise */
         use_original_event = common_h.TRUE;

         /* the original event is a forced check... */
         if ((svc.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0)
         {

            /* the new event is also forced and its execution time is earlier than the original, so use it instead */
            if (forced == common_h.TRUE && check_time < svc.next_check)
               use_original_event = common_h.FALSE;
         }

         /* the original event is not a forced check... */
         else
         {

            /* the new event is a forced check, so use it instead */
            if (forced == common_h.TRUE)
               use_original_event = common_h.FALSE;

            /* the new event is not forced either and its execution time is earlier than the original, so use it instead */
            else if (check_time < svc.next_check)
               use_original_event = common_h.FALSE;
         }

         /* the originally queued event won the battle, so keep it and exit */
         if (use_original_event == common_h.TRUE)
         {
            return;
         }

         events.remove_event(temp_event, events.event_list_low);
      }

      /* set the next service check time */
      svc.next_check = check_time;

      /* set the force service check option */
      if (forced == common_h.TRUE)
         svc.check_options |= blue_h.CHECK_OPTION_FORCE_EXECUTION;

      /* place the new event in the event queue */
      new_event.event_type = blue_h.EVENT_SERVICE_CHECK;
      new_event.event_data = svc;
      new_event.event_args = null;
      new_event.run_time = svc.next_check;
      new_event.recurring = common_h.FALSE;
      new_event.event_interval = 0L;
      new_event.timing_func = null;
      new_event.compensate_for_time_change = common_h.TRUE;
      events.reschedule_event(new_event, events.event_list_low);

      /* update the status log */
      statusdata.update_service_status(svc, common_h.FALSE);

      logger.trace("exiting " + cn + ".schedule_service_check");

      return;
   }

   /* checks service dependencies */
   public static int check_service_dependencies(objects_h.service svc, int dependency_type)
   {
      int state;

      logger.trace("entering " + cn + ".check_service_dependencies");

      /* check all dependencies... */
      for (objects_h.servicedependency temp_dependency : objects.get_servicedependency_list_by_dependent_host(
            svc.host_name, svc.description))
      {

         /* only check dependencies of the desired type (notification or execution) */
         if (temp_dependency.dependency_type != dependency_type)
            continue;

         /* find the service we depend on... */
         objects_h.service temp_service = objects.find_service(temp_dependency.host_name,
               temp_dependency.service_description);
         if (temp_service == null)
            continue;

         /* get the status to use (use last hard state if its currently in a soft state) */
         if (temp_service.state_type == common_h.SOFT_STATE && blue.soft_state_dependencies == common_h.FALSE)
            state = temp_service.last_hard_state;
         else
            state = temp_service.current_state;

         /* is the service we depend on in state that fails the dependency tests? */
         if (state == blue_h.STATE_OK && temp_dependency.fail_on_ok == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if (state == blue_h.STATE_WARNING && temp_dependency.fail_on_warning == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if (state == blue_h.STATE_UNKNOWN && temp_dependency.fail_on_unknown == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if (state == blue_h.STATE_CRITICAL && temp_dependency.fail_on_critical == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if ((state == blue_h.STATE_OK && temp_service.has_been_checked == common_h.FALSE)
               && temp_dependency.fail_on_pending == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;

         /* immediate dependencies ok at this point - check parent dependencies if necessary */
         if (temp_dependency.inherits_parent == common_h.TRUE)
         {
            if (check_service_dependencies(temp_service, dependency_type) != blue_h.DEPENDENCIES_OK)
               return blue_h.DEPENDENCIES_FAILED;
         }
      }

      logger.trace("exiting " + cn + ".check_service_dependencies");

      return blue_h.DEPENDENCIES_OK;
   }

   /* checks host dependencies */
   public static int check_host_dependencies(objects_h.host hst, int dependency_type)
   {

      logger.trace("entering " + cn + ".check_host_dependencies");

      /* check all dependencies... */
      for (objects_h.hostdependency temp_dependency : objects.get_hostdependency_list_by_dependent_host(hst.name))
      {
         //          for(temp_dependency=get_first_hostdependency_by_dependent_host(hst.name);temp_dependency!=null;temp_dependency=get_next_hostdependency_by_dependent_host(hst.name,temp_dependency)){

         /* only check dependencies of the desired type (notification or execution) */
         if (temp_dependency.dependency_type != dependency_type)
            continue;

         /* find the host we depend on... */
         objects_h.host temp_host = objects.find_host(temp_dependency.host_name);
         if (temp_host == null)
            continue;

         /* is the host we depend on in state that fails the dependency tests? */
         if (temp_host.current_state == blue_h.HOST_UP && temp_dependency.fail_on_up == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if (temp_host.current_state == blue_h.HOST_DOWN && temp_dependency.fail_on_down == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if (temp_host.current_state == blue_h.HOST_UNREACHABLE
               && temp_dependency.fail_on_unreachable == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;
         if ((temp_host.current_state == blue_h.HOST_UP && temp_host.has_been_checked == common_h.FALSE)
               && temp_dependency.fail_on_pending == common_h.TRUE)
            return blue_h.DEPENDENCIES_FAILED;

         /* immediate dependencies ok at this point - check parent dependencies if necessary */
         if (temp_dependency.inherits_parent == common_h.TRUE)
         {
            if (check_host_dependencies(temp_host, dependency_type) != blue_h.DEPENDENCIES_OK)
               return blue_h.DEPENDENCIES_FAILED;
         }
      }

      logger.trace("exiting " + cn + ".check_host_dependencies");

      return blue_h.DEPENDENCIES_OK;
   }

   /* check for services that never returned from a check... */
   public static void check_for_orphaned_services()
   {
      long current_time;
      long expected_time;
      String buffer;

      logger.trace("entering " + cn + ".check_for_orphaned_services");

      /* get the current time */
      current_time = utils.currentTimeInSeconds();

      /* check all services... */
      for (objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list)
      {

         /* skip services that are not currently executing */
         if (temp_service.is_executing == common_h.FALSE)
            continue;

         /* determine the time at which the check results should have come in (allow 10 minutes slack time) */
         expected_time = (long) (temp_service.next_check + temp_service.latency + blue.service_check_timeout
               + blue.service_check_reaper_interval + 600);

         /* this service was supposed to have executed a while ago, but for some reason the results haven't come back in... */
         if (expected_time < current_time)
         {

            /* log a warning */
            buffer = "Warning: The check of service '"
                  + temp_service.description
                  + "' on host '"
                  + temp_service.host_name
                  + "' looks like it was orphaned (results never came back).  I'm scheduling an immediate check of the service...";
            logger.info(buffer);

            /* decrement the number of running service checks */
            if (blue.currently_running_service_checks > 0)
               blue.currently_running_service_checks--;

            /* disable the executing flag */
            temp_service.is_executing = common_h.FALSE;

            /* schedule an immediate check of the service */
            schedule_service_check(temp_service, current_time, common_h.FALSE);
         }

      }

      logger.trace("exiting " + cn + ".check_for_orphaned_services");

      return;
   }

   /* check freshness of service results */
   public static void check_service_result_freshness()
   {
      long current_time;
      long expiration_time;
      int freshness_threshold;
      String buffer;

      logger.trace("entering " + cn + ".check_service_result_freshness");

      logger.debug("\n======FRESHNESS START======\n");
      logger.debug("CHECKFRESHNESS 1\n");

      /* bail out if we're not supposed to be checking freshness */
      if (blue.check_service_freshness == common_h.FALSE)
         return;

      /* get the current time */
      current_time = utils.currentTimeInSeconds();

      logger.debug("CHECKFRESHNESS 2: " + current_time);

      /* check all services... */
      for (objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list)
      {

         if (temp_service.description.equals("Freshness Check Test"))
            logger.debug("Checking: " + temp_service.host_name + "/" + temp_service.description);

         /* skip services we shouldn't be checking for freshness */
         if (temp_service.check_freshness == common_h.FALSE)
            continue;

         /* skip services that are currently executing (problems here will be caught by orphaned service check) */
         if (temp_service.is_executing == common_h.TRUE)
            continue;

         /* skip services that have both active and passive checks disabled */
         if (temp_service.checks_enabled == common_h.FALSE
               && temp_service.accept_passive_service_checks == common_h.FALSE)
            continue;

         /* skip services that are already being freshened */
         if (temp_service.is_being_freshened == common_h.TRUE)
            continue;

         /* see if the time is right... */
         if (utils.check_time_against_period(current_time, temp_service.check_period) == common_h.ERROR)
            continue;

         /* EXCEPTION */
         /* don't check freshness of services without regular check intervals if we're using auto-freshness threshold */
         if (temp_service.check_interval == 0 && temp_service.freshness_threshold == 0)
            continue;

         logger.debug("CHECKFRESHNESS 3\n");

         /* use user-supplied freshness threshold or auto-calculate a freshness threshold to use? */
         if (temp_service.freshness_threshold == 0)
         {
            if (temp_service.state_type == common_h.HARD_STATE || temp_service.current_state == blue_h.STATE_OK)
               freshness_threshold = (int) ((temp_service.check_interval * blue.interval_length) + temp_service.latency + 15);
            else
               freshness_threshold = (int) ((temp_service.retry_interval * blue.interval_length) + temp_service.latency + 15);
         }
         else
            freshness_threshold = temp_service.freshness_threshold;

         logger.debug("THRESHOLD: SVC=" + temp_service.freshness_threshold + ", USE=" + freshness_threshold);

         /* calculate expiration time */
         /* passive checks also become stale, so remove dependence on active check logic */
         /* Updated for 2.2 */
        if(temp_service.has_been_checked==common_h.FALSE || blue.program_start > temp_service.last_check)
            expiration_time = (blue.program_start + freshness_threshold);
         else
            expiration_time = (temp_service.last_check + freshness_threshold);

         logger.debug("HASBEENCHECKED: " + temp_service.has_been_checked);
         logger.debug("PROGRAM START:  " + blue.program_start);
         logger.debug("LAST CHECK:     " + temp_service.last_check);
         logger.debug("CURRENT TIME:   " + current_time);
         logger.debug("EXPIRE TIME:    " + expiration_time);

         /* the results for the last check of this service are stale */
         if (expiration_time < current_time)
         {

            /* log a warning */
            buffer = "Warning: The results of service '" + temp_service.description + "' on host '"
                  + temp_service.host_name + "' are stale by " + (current_time - expiration_time)
                  + " seconds (threshold=" + freshness_threshold
                  + " seconds).  I'm forcing an immediate check of the service.";
            logger.info(buffer);

            /* set the freshen flag */
            temp_service.is_being_freshened = common_h.TRUE;

            /* schedule an immediate forced check of the service */
            schedule_service_check(temp_service, current_time, common_h.TRUE);
         }
      }

      logger.debug("\n======FRESHNESS END======\n");

      logger.trace("exiting " + cn + ".check_service_result_freshness");

      return;
   }

   /******************************************************************/
   /******************* ROUTE/HOST CHECK FUNCTIONS *******************/
   /******************************************************************/

   /*** ON-DEMAND HOST CHECKS USE THIS FUNCTION ***/
   /* check to see if we can reach the host */
   public static int verify_route_to_host(objects_h.host hst, int check_options)
   {
      int result;

      logger.trace("entering " + cn + ".verify_route_to_host");

      /* reset latency, since on-demand checks have none */
      hst.latency = 0.0;

      /* check route to the host (propagate problems and recoveries both up and down the tree) */
      result = check_host(hst, blue_h.PROPAGATE_TO_PARENT_HOSTS | blue_h.PROPAGATE_TO_CHILD_HOSTS, check_options);

      logger.trace("exiting " + cn + ".verify_route_to_host");

      return result;
   }

   /*** SCHEDULED HOST CHECKS USE THIS FUNCTION ***/
   /* run a scheduled host check */
   
   public static int run_scheduled_host_check(objects_h.host hst)
   {
      long current_time;
      long preferred_time;
      long next_valid_time;
      int perform_check = common_h.TRUE;
      int time_is_valid = common_h.TRUE;

      logger.trace("entering " + cn + ".run_scheduled_host_check");

      /*********************************************************************
       NOTE: A lot of the checks that occur before the host is actually
       checked (checks enabled, time period, dependencies) are checked
       later in the run_host_check() function.  The only reason we
       duplicate them here is to nicely reschedule the next host check
       as soon as possible, instead of at the next regular interval in 
       the event we know that the host check will not be run at the
       current time 
       *********************************************************************/

      current_time = utils.currentTimeInSeconds();

      /* default time to reschedule the next host check */
      
      preferred_time = current_time + (hst.check_interval * blue.interval_length);

      /* if  checks of the host are currently disabled... */
      
      if (hst.checks_enabled == common_h.FALSE)
      {

         /* don't check the host if we're not forcing it through */
         if (0 == (hst.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            perform_check = common_h.FALSE;
      }

      /* make sure this is a valid time to check the host */
      if (utils.check_time_against_period(current_time, hst.check_period) == common_h.ERROR)
      {

         /* don't check the host if we're not forcing it through */
         if (0 == (hst.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            perform_check = common_h.FALSE;

         /* get the next valid time we can run the check */
         preferred_time = current_time;

         /* set the invalid time flag */
         time_is_valid = common_h.FALSE;
      }

      /* check host dependencies for execution */
      if (check_host_dependencies(hst, common_h.EXECUTION_DEPENDENCY) == blue_h.DEPENDENCIES_FAILED)
      {

         /* don't check the host if we're not forcing it through */
         if (0 == (hst.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION))
            perform_check = common_h.FALSE;
      }

      /**** RUN THE SCHEDULED HOST CHECK ****/
      /* check route to the host (propagate problems and recoveries both up and down the tree) */
      if (perform_check == common_h.TRUE)
         check_host(hst, blue_h.PROPAGATE_TO_PARENT_HOSTS | blue_h.PROPAGATE_TO_CHILD_HOSTS, hst.check_options);

      /* clear the force execution flag */
      if ((hst.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0)
         hst.check_options -= blue_h.CHECK_OPTION_FORCE_EXECUTION;

      /* default is to schedule the host check unless test below fail */
      hst.should_be_scheduled = common_h.TRUE;

      /* don't reschedule non-recurring host checks */
      if (hst.check_interval == 0)
         hst.should_be_scheduled = common_h.FALSE;

      /* don't reschedule hosts with active checks disabled */
      if (hst.checks_enabled == common_h.FALSE)
         hst.should_be_scheduled = common_h.FALSE;

      /* make sure we rescheduled the next host check at a valid time */
      next_valid_time = utils.get_next_valid_time(preferred_time, hst.check_period);

      /* the host could not be rescheduled properly - set the next check time for next year, but don't actually reschedule it */
      if (time_is_valid == common_h.FALSE && next_valid_time == preferred_time)
      {

         hst.next_check = (next_valid_time + (60 * 60 * 24 * 365));
         hst.should_be_scheduled = common_h.FALSE;
         logger.debug("Warning: Could not find any valid times to reschedule a check of host '" + hst.name + "'!");
      }

      /* this host could be rescheduled... */
      else
         hst.next_check = next_valid_time;

      /* update the status data */
      statusdata.update_host_status(hst, common_h.FALSE);

      /* reschedule the next host check if we're able */
      if (hst.should_be_scheduled == common_h.TRUE)
         schedule_host_check(hst, hst.next_check, common_h.FALSE);

      logger.trace("exiting " + cn + ".run_scheduled_host_check");

      return common_h.OK;
   }

   /* check freshness of host results */
   public static void check_host_result_freshness()
   {
      long current_time;
      long expiration_time;
      int freshness_threshold;
      String buffer;

      logger.trace("entering " + cn + ".check_host_result_freshness");

      /* bail out if we're not supposed to be checking freshness */
      if (blue.check_host_freshness == common_h.FALSE)
         return;

      /* get the current time */
      current_time = utils.currentTimeInSeconds();

      /* check all hosts... */
      for (objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list)
      {

         /* skip hosts we shouldn't be checking for freshness */
         if (temp_host.check_freshness == common_h.FALSE)
            continue;

         /* skip hosts that have both active and passive checks disabled */
         if (temp_host.checks_enabled == common_h.FALSE && temp_host.accept_passive_host_checks == common_h.FALSE)
            continue;

         /* skip hosts that are already being freshened */
         if (temp_host.is_being_freshened == common_h.TRUE)
            continue;

         /* see if the time is right... */
         if (utils.check_time_against_period(current_time, temp_host.check_period) == common_h.ERROR)
            continue;

         /* use user-supplied freshness threshold or auto-calculate a freshness threshold to use? */
         if (temp_host.freshness_threshold == 0)
            freshness_threshold = (int) ((temp_host.check_interval * blue.interval_length) + temp_host.latency + 15.0);
         else
            freshness_threshold = temp_host.freshness_threshold;

         /* calculate expiration time */
         /* CHANGED 11/10/05 EG - program start is only used in expiration time calculation if > last check AND active checks are enabled, so active checks can become stale immediately upon program startup */
         if (temp_host.has_been_checked == common_h.FALSE
               || (temp_host.checks_enabled == common_h.TRUE && (blue.program_start > temp_host.last_check)))
            expiration_time = (blue.program_start + freshness_threshold);
         else
            expiration_time = (temp_host.last_check + freshness_threshold);

         /* the results for the last check of this host are stale */
         if (expiration_time < current_time)
         {

            /* log a warning */
            buffer = "Warning: The results of host '" + temp_host.name + "' are stale by "
                  + (current_time - expiration_time) + " seconds (threshold=" + freshness_threshold
                  + " seconds).  I'm forcing an immediate check of the host.\n";
            logger.info(buffer);

            /* set the freshen flag */
            temp_host.is_being_freshened = common_h.TRUE;

            /* schedule an immediate forced check of the host */
            schedule_host_check(temp_host, current_time, common_h.TRUE);
         }
      }

      logger.trace("exiting " + cn + ".check_host_result_freshness");

      return;
   }

   /* see if the remote host is alive at all */
   public static int check_host(objects_h.host hst, int propagation_options, int check_options)
   {
      int result = blue_h.HOST_UP;
      int parent_result = blue_h.HOST_UP;
      //	objects_h.host parent_host=null;
      //	objects_h.hostsmember temp_hostsmember=null;
      //	objects_h.host child_host=null;
      int return_result = blue_h.HOST_UP;
      int max_check_attempts = 1;
      int route_blocked = common_h.TRUE;
      int old_state=blue_h.HOST_UP;
      blue_h.timeval start_time;
      blue_h.timeval end_time;
      String old_plugin_output = "";
      
      /* Added for 2.7 */
      /* bail on signal */
      if ( blue.sigrestart == common_h.TRUE || blue.sigshutdown == common_h.TRUE )
         return hst.current_attempt;

      logger.debug("entering " + cn + ".check_host");

      /* high resolution time for broker */
      start_time = new blue_h.timeval();
      
      end_time = new blue_h.timeval(0,0);
      broker.broker_host_check(broker_h.NEBTYPE_HOSTCHECK_INITIATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,hst,common_h.HOST_CHECK_ACTIVE,hst.current_state,hst.state_type,start_time,end_time,hst.host_check_command,hst.latency,0.0,0,common_h.FALSE,0,null,null,null,null);

      /* make sure we return the original host state unless it changes... */
      return_result = hst.current_state;

      /* save old state - a host is always in a hard state when this function is called... */
      hst.last_state = hst.current_state;
      hst.last_hard_state = hst.current_state;

      /* set the checked flag */
      hst.has_been_checked = common_h.TRUE;

      /* clear the freshness flag */
      hst.is_being_freshened = common_h.FALSE;

      /* save the old plugin output and host state for use with state stalking routines */
      old_state=hst.current_state;
      old_plugin_output = ((hst.plugin_output == null) ? "" : hst.plugin_output);

      /***** HOST IS NOT UP INITIALLY *****/
      /* if the host is already down or unreachable... */
      if (hst.current_state != blue_h.HOST_UP)
      {

         /* set the state type (should already be set) */
         hst.state_type = common_h.HARD_STATE;

         /* how many times should we retry checks for this host? */
         if (blue.use_aggressive_host_checking == common_h.FALSE)
            max_check_attempts = 1;
         else
            max_check_attempts = hst.max_attempts;

         /* retry the host check as many times as necessary or allowed... */
         for (hst.current_attempt = 1; hst.current_attempt <= max_check_attempts; hst.current_attempt++)
         {
            /* added for 2.7 */
            /* bail on signal */
            if( blue.sigrestart==common_h.TRUE || blue.sigshutdown==common_h.TRUE){
                hst.current_attempt=1;
                hst.current_state=old_state;
                hst.plugin_output= old_plugin_output;
                return hst.current_state;
                }

            /* check the host */
            result = run_host_check(hst, check_options);

            /* the host recovered from a hard problem... */
            if (result == blue_h.HOST_UP)
            {

               /* update host state */
               hst.current_state = blue_h.HOST_UP;

               return_result = blue_h.HOST_UP;

               /* handle the hard host recovery */
               sehandlers.handle_host_state(hst);

               /* propagate the host recovery upwards (at least one parent should be up now) */
               if ((propagation_options & blue_h.PROPAGATE_TO_PARENT_HOSTS) > 0)
               {

                  /* propagate to all parent hosts */
                  for (objects_h.hostsmember temp_hostsmember : (ArrayList<objects_h.hostsmember>) hst.parent_hosts)
                  {

                     /* find the parent host */
                     objects_h.host parent_host = objects.find_host(temp_hostsmember.host_name);

                     /* check the parent host (and propagate upwards) if its not up */
                     if (parent_host != null && parent_host.current_state != blue_h.HOST_UP)
                        check_host(parent_host, blue_h.PROPAGATE_TO_PARENT_HOSTS, check_options);
                  }
               }

               /* propagate the host recovery downwards (children may or may not be down) */
               if ((propagation_options & blue_h.PROPAGATE_TO_CHILD_HOSTS) > 0)
               {

                  /* check all child hosts... */
                  for (objects_h.host child_host : (ArrayList<objects_h.host>) objects.host_list)
                  {

                     /* if this is a child of the host, check it if it is not marked as UP */
                     if (objects.is_host_immediate_child_of_host(hst, child_host) == common_h.TRUE
                           && child_host.current_state != blue_h.HOST_UP)
                        check_host(child_host, blue_h.PROPAGATE_TO_CHILD_HOSTS, check_options);
                  }
               }

               break;
            }

            /* there is still a problem with the host... */

            /* if this is the last check and the host is currently marked as being UNREACHABLE, make sure it hasn't changed to a DOWN state. */
            /* to do this we have to check the (saved) status of all parent hosts.  this situation can occur if a host is */
            /* unreachable, one of its parent recovers, but the host does not return to an UP state.  Even though the host is not UP, */
            /* it has changed from an UNREACHABLE to a DOWN state */

            else if (hst.last_state == blue_h.HOST_UNREACHABLE && hst.current_attempt == max_check_attempts)
            {

               /* check all parent hosts */
               for (objects_h.hostsmember temp_hostsmember : (ArrayList<objects_h.hostsmember>) hst.parent_hosts)
               {

                  /* find the parent host */
                  objects_h.host parent_host = objects.find_host(temp_hostsmember.host_name);

                  /* if at least one parent host is up, this host is no longer unreachable - it is now down instead */
                  if (parent_host.current_state == blue_h.HOST_UP)
                  {

                     /* change the host state to DOWN */
                     hst.current_state = blue_h.HOST_DOWN;

                     break;
                  }
               }
            }

            /* handle the host problem */
            sehandlers.handle_host_state(hst);
         }

         /* readjust the current check number - added 01/10/05 EG */
         hst.current_attempt--;
      }

      /***** HOST IS UP INITIALLY *****/
      /* else the host is supposed to be up right now... */
      else
      {

         for (hst.current_attempt = 1; hst.current_attempt <= hst.max_attempts; hst.current_attempt++)
         {

            /* added 2.7 */
            /* bail on signal */
            if(blue.sigrestart==common_h.TRUE || blue.sigshutdown==common_h.TRUE){
                hst.current_attempt=1;
                hst.current_state=old_state;
                hst.plugin_output= old_plugin_output;
                return hst.current_state;
                }

            /* run the host check */
            result = run_host_check(hst, check_options);

            /* update state type */
            if (result == blue_h.HOST_UP)
               hst.state_type = (hst.current_attempt == 1) ? common_h.HARD_STATE : common_h.SOFT_STATE;
            else
               hst.state_type = (hst.current_attempt == hst.max_attempts) ? common_h.HARD_STATE : common_h.SOFT_STATE;
            
            /*** HARD common_h.ERROR STATE ***/
            /* if this is the last check and we still haven't had a recovery, check the parents, handle the hard state and propagate the check to children */
            if (result != blue_h.HOST_UP && (hst.current_attempt == hst.max_attempts))
            {

               /* check all parent hosts */
               if ( hst.parent_hosts != null ) {
                  for (objects_h.hostsmember temp_hostsmember : (ArrayList<objects_h.hostsmember>) hst.parent_hosts)
                  {
                     
                     /* find the parent host */
                     objects_h.host parent_host = objects.find_host(temp_hostsmember.host_name);
                     
                     /* check the parent host, assume its up if we can't find it, use the parent host's "old" status if we shouldn't propagate */
                     if (parent_host == null)
                        parent_result = blue_h.HOST_UP;
                     else if ((propagation_options & blue_h.PROPAGATE_TO_PARENT_HOSTS) > 0)
                        parent_result = check_host(parent_host, blue_h.PROPAGATE_TO_PARENT_HOSTS, check_options);
                     else
                        parent_result = parent_host.current_state;
                     
                     /* if this parent host was up, the route is okay */
                     if (parent_result == blue_h.HOST_UP)
                        route_blocked = common_h.FALSE;
                     
                     /* we could break out of this loop once we've found one parent host that is up, but we won't because we want
                      immediate notification of state changes (i.e. recoveries) for parent hosts */
                  }
               }

               /* if this host has at least one parent host and the route to this host is blocked, it is unreachable */
               if (route_blocked == common_h.TRUE && hst.parent_hosts != null)
                  return_result = blue_h.HOST_UNREACHABLE;

               /* else the parent host is up (or there isn't a parent host), so this host must be down */
               else
                  return_result = blue_h.HOST_DOWN;

               /* update host state */
               hst.current_state = return_result;

               /* handle the hard host state (whether it is DOWN or UNREACHABLE) */
               sehandlers.handle_host_state(hst);

               /* propagate the host problem to all child hosts (they should be unreachable now unless they have multiple parents) */
               if ((propagation_options & blue_h.PROPAGATE_TO_CHILD_HOSTS) > 0)
               {

                  /* check all child hosts... */
                  for (objects_h.host child_host : (ArrayList<objects_h.host>) objects.host_list)
                  {

                     /* if this is a child of the host, check it if it is not marked as UP */
                     if (objects.is_host_immediate_child_of_host(hst, child_host) == common_h.TRUE
                           && child_host.current_state != blue_h.HOST_UP)
                        check_host(child_host, blue_h.PROPAGATE_TO_CHILD_HOSTS, check_options);
                  }
               }
            }

            /*** SOFT common_h.ERROR STATE ***/
            /* handle any soft error states (during host check retries that return a non-ok state) */
            else if (result != blue_h.HOST_UP)
            {

               /* update the current host state */
               hst.current_state = result;

               /* handle the host state */
               sehandlers.handle_host_state(hst);

               /* update the status log with the current host info */
               /* this needs to be called to update status data on soft error states */
               statusdata.update_host_status(hst, common_h.FALSE);
            }

            /*** SOFT RECOVERY STATE ***/
            /* handle any soft recovery states (during host check retries that return an ok state) */
            else if (result == blue_h.HOST_UP && hst.current_attempt != 1)
            {

               /* update the current host state */
               hst.current_state = result;

               /* handle the host state */
               sehandlers.handle_host_state(hst);

               /* update the status log with the current host info */
               /* this needs to be called to update status data on soft error states */
               statusdata.update_host_status(hst, common_h.FALSE);

               break;
            }

            /*** UNCHANGED common_h.OK STATE ***/
            /* the host never went down */
            else if (result == blue_h.HOST_UP)
            {

               /* update the current host state */
               hst.current_state = blue_h.HOST_UP;

               /* this is the first check of the host */
               if (hst.has_been_checked == common_h.FALSE)
               {

                  /* set the checked flag */
                  hst.has_been_checked = common_h.TRUE;
               }

               /* handle the host state */
               sehandlers.handle_host_state(hst);

               break;
            }
         }
      }

      /* adjust the current check number if we exceeded the max count */
      if (hst.current_attempt > hst.max_attempts)
         hst.current_attempt = hst.max_attempts;

      /* make sure state type is hard */
      hst.state_type = common_h.HARD_STATE;

      /* update the status log with the current host info */
      statusdata.update_host_status(hst, common_h.FALSE);

      /* if the host didn't change state and the plugin output differs from the last time it was checked, log the current state/output if state stalking is enabled */
      if (hst.last_state == hst.current_state && !old_plugin_output.equals(hst.plugin_output))
      {

         if (hst.current_state == blue_h.HOST_UP && hst.stalk_on_up == common_h.TRUE)
            logging.log_host_event(hst);

         else if (hst.current_state == blue_h.HOST_DOWN && hst.stalk_on_down == common_h.TRUE)
            logging.log_host_event(hst);

         else if (hst.current_state == blue_h.HOST_UNREACHABLE && hst.stalk_on_unreachable == common_h.TRUE)
            logging.log_host_event(hst);
      }


       /* high resolution time for broker */
       end_time.reset();
      
      /* send data to event broker */
      long execution_time= end_time.time - start_time.time ;
      broker.broker_host_check(broker_h.NEBTYPE_HOSTCHECK_PROCESSED,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,hst,common_h.HOST_CHECK_ACTIVE,hst.current_state,hst.state_type,start_time,end_time,hst.host_check_command,hst.latency,execution_time,0,common_h.FALSE,0,null,hst.plugin_output,hst.perf_data,null);

      /* check to see if the associated host is flapping */
      flapping.check_for_host_flapping(hst, common_h.TRUE);

      /* check for external commands if we're doing so as often as possible */
      if (blue.command_check_interval == -1)
         commands.check_for_external_commands();

      logger.debug("\tHost Check Result: Host '" + hst.name + "' is ");
      if (return_result == blue_h.HOST_UP)
         logger.debug("UP");
      else if (return_result == blue_h.HOST_DOWN)
         logger.debug("DOWN");
      else if (return_result == blue_h.HOST_UNREACHABLE)
         logger.debug("UNREACHABLE");

      logger.trace("exiting " + cn + ".check_host");

      return return_result;
   }

   /* run an "alive" check on a host */
   public static int run_host_check(objects_h.host hst, int check_options)
   {
//      int result = nagios_h.STATE_OK;
      int return_result = blue_h.HOST_UP;
      String processed_command;
      String raw_command;
      String temp_buffer;
      long current_time;
      long start_time;
      blue_h.timeval start_time_hires;
      blue_h.timeval end_time_hires;
      String temp_ptr;
      String temp_plugin_output;

      logger.trace("entering " + cn + ".run_host_check");

      /* if we're not forcing the check, see if we should actually go through with it... */
      if ((check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) == 0)
      {

         current_time = utils.currentTimeInSeconds();

         /* make sure host checks are enabled */
         if (blue.execute_host_checks == common_h.FALSE || hst.checks_enabled == common_h.FALSE)
            return hst.current_state;

         /* make sure this is a valid time to check the host */
         if (utils.check_time_against_period(current_time, hst.check_period) == common_h.ERROR)
            return hst.current_state;

         /* check host dependencies for execution */
         if (checks.check_host_dependencies(hst, common_h.EXECUTION_DEPENDENCY) == blue_h.DEPENDENCIES_FAILED)
            return hst.current_state;
      }

      /* if there is no host check command, just return with no error */
      if (hst.host_check_command == null)
      {
         logger.debug("\tNo host check command specified, so no check will be done (host state assumed to be unchanged)!\n");
         return blue_h.HOST_UP;
      }

      /* grab the host macros */
      utils.clear_volatile_macros();
      utils.grab_host_macros(hst);
      utils.grab_summary_macros(null);

      //	/* high resolution start time for event broker */
      start_time_hires = new blue_h.timeval();

      /* get the last host check time */
      start_time = utils.currentTimeInSeconds();
      hst.last_check = start_time;

      /* get the raw command line */
      raw_command = utils.get_raw_command_line(hst.host_check_command, 0);
      raw_command = utils.strip(raw_command);

      /* process any macros contained in the argument */
      processed_command = utils.process_macros(raw_command, 0);
      processed_command = utils.strip(processed_command);

      /* send data to event broker */
      end_time_hires = new blue_h.timeval(); 
      end_time_hires.tv_usec=0L;
      broker.broker_host_check(broker_h.NEBTYPE_HOSTCHECK_RAW_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,hst,common_h.HOST_CHECK_ACTIVE,return_result,hst.state_type,start_time_hires,end_time_hires,hst.host_check_command,0.0,0.0,blue.host_check_timeout,common_h.FALSE,0,processed_command,hst.plugin_output,hst.perf_data,null);
      
      logger.debug("\t\tRaw Command: " + raw_command);
      logger.debug("\t\tProcessed Command: " + processed_command);

      /* clear plugin output and performance data buffers */
      hst.plugin_output = "";
      hst.perf_data = "";

      /* run the host check command */
      utils.system_result sys_result = utils.my_system(processed_command, blue.host_check_timeout);

      /* if the check timed out, report an error */
      if (sys_result.early_timeout == true)
      {

         hst.plugin_output = "Host check timed out after " + blue.host_check_timeout + " seconds";

         /* log the timeout */
         temp_buffer = "Warning: Host check command '" + processed_command + "' for host '" + hst.name
               + "' timed out after " + blue.host_check_timeout + " seconds";
         logger.info(temp_buffer);
      }

      /* calculate total execution time */
      hst.execution_time = sys_result.exec_time;

      /* record check type */
      hst.check_type = common_h.HOST_CHECK_ACTIVE;

      /* check for empty plugin output */
      temp_plugin_output = sys_result.output;
      if (temp_plugin_output == null || temp_plugin_output.trim().length() == 0)
         temp_plugin_output = "(No Information Returned From Host Check)";

      /* first part of plugin output (up to pipe) is status info */
      String split[] = temp_plugin_output.split("[|\n]");
      temp_ptr = split[0];

      /* make sure the plugin output isn't null */
      if (temp_ptr == null)
      {
         hst.plugin_output = "(No output returned from host check)";
      }

      else
      {

         temp_ptr = utils.strip(temp_ptr);
         if (temp_ptr.length() == 0)
         {
            hst.plugin_output = "(No output returned from host check)";
         }
         else
         {
            hst.plugin_output = temp_ptr;
         }
      }

      /* second part of plugin output (after pipe) is performance data (which may or may not exist) */
      if (split.length > 1)
         temp_ptr = split[1];
      else
         temp_ptr = null;

      /* grab performance data if we found it available */
      if (temp_ptr != null)
      {
         hst.perf_data = temp_ptr.trim();

      }

      /* replace semicolons in plugin output (but not performance data) with colons */
      temp_ptr = hst.plugin_output.replace(';', ':');

      /* if we're not doing aggressive host checking, let WARNING states indicate the host is up (fake the result to be STATE_OK) */      
      if (blue.use_aggressive_host_checking == common_h.FALSE && sys_result.result == blue_h.STATE_WARNING)
         sys_result.result = blue_h.STATE_OK;

      if (sys_result.result == blue_h.STATE_OK)
         return_result = blue_h.HOST_UP;
      else
         return_result = blue_h.HOST_DOWN;

      	/* high resolution end time for event broker */
      	end_time_hires.reset();

   	/* send data to event broker */
       broker.broker_host_check(broker_h.NEBTYPE_HOSTCHECK_RAW_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,hst,common_h.HOST_CHECK_ACTIVE,return_result,hst.state_type,start_time_hires,end_time_hires,hst.host_check_command,0.0,sys_result.exec_time,blue.host_check_timeout,sys_result.early_timeout?common_h.TRUE:common_h.FALSE,sys_result.result,processed_command,hst.plugin_output,hst.perf_data,null);

      String buffer = ("\tHost Check Result: Host '" + hst.name + "' is ");
      if (return_result == blue_h.HOST_UP)
         buffer += ("UP");
      else
         buffer += ("DOWN");
      logger.debug(buffer);

      logger.trace("exiting " + cn + ".run_host_check");

      return return_result;
   }

   /* schedules an immediate or delayed host check */
   public static void schedule_host_check(objects_h.host hst, long check_time, int forced)
   {
      blue_h.timed_event temp_event = null;
      blue_h.timed_event new_event;
      int found = common_h.FALSE;
      int use_original_event = common_h.TRUE;

      logger.trace("entering " + cn + ".schedule_host_check");

      /* don't schedule a check if active checks are disabled */
      if ((blue.execute_host_checks == common_h.FALSE || hst.checks_enabled == common_h.FALSE)
            && forced == common_h.FALSE)
         return;

      /* allocate memory for a new event item */
      new_event = new blue_h.timed_event();

      /* see if there are any other scheduled checks of this host in the queue */
      for (blue_h.timed_event iter_event : (ArrayList<blue_h.timed_event>) events.event_list_low)
      {

         if (iter_event.event_type == blue_h.EVENT_HOST_CHECK && hst == (objects_h.host) iter_event.event_data)
         {
            found = common_h.TRUE;
            temp_event = iter_event;
            break;
         }
      }

      /* we found another host check event for this host in the queue - what should we do? */
      if (found == common_h.TRUE && temp_event != null)
      {

         /* use the originally scheduled check unless we decide otherwise */
         use_original_event = common_h.TRUE;

         /* the original event is a forced check... */
         if ((hst.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0)
         {

            /* the new event is also forced and its execution time is earlier than the original, so use it instead */
            if (forced == common_h.TRUE && check_time < hst.next_check)
               use_original_event = common_h.FALSE;
         }

         /* the original event is not a forced check... */
         else
         {

            /* the new event is a forced check, so use it instead */
            if (forced == common_h.TRUE)
               use_original_event = common_h.FALSE;

            /* the new event is not forced either and its execution time is earlier than the original, so use it instead */
            else if (check_time < hst.next_check)
               use_original_event = common_h.FALSE;
         }

         /* the originally queued event won the battle, so keep it and exit */
         if (use_original_event == common_h.TRUE)
         {
            return;
         }

         events.remove_event(temp_event, events.event_list_low);
      }

      /* set the next host check time */
      hst.next_check = check_time;

      /* set the force service check option */
      if (forced == common_h.TRUE)
         hst.check_options |= blue_h.CHECK_OPTION_FORCE_EXECUTION;

      /* place the new event in the event queue */
      new_event.event_type = blue_h.EVENT_HOST_CHECK;
      new_event.event_data = hst;
      new_event.event_args = null;
      new_event.run_time = hst.next_check;
      new_event.recurring = common_h.FALSE;
      new_event.event_interval = 0L;
      new_event.timing_func = null;
      new_event.compensate_for_time_change = common_h.TRUE;
      events.reschedule_event(new_event, events.event_list_low);

      /* update the status log */
      statusdata.update_host_status(hst, common_h.FALSE);

      logger.trace("exiting " + cn + ".schedule_host_check");

      return;
   }

   private static String getSplit(String[] split, int index)
   {
      if (split.length <= index)
         return null;
      else
         return split[index].trim();
   }
}
