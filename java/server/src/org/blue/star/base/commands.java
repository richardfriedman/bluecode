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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.commands.ICommand;
import org.blue.star.common.comments;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.include.downtime_h;
import org.blue.star.include.objects_h;

public class commands { 

   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.base.commands");
   public static String cn = "org.blue.base.commands"; 

   public static ArrayList<blue_h.passive_check_result> passive_check_result_list = null;

   /******************************************************************/
   /****************** EXTERNAL COMMAND PROCESSING *******************/
   /******************************************************************/


   /**
    * checks for the existence of the external command file and 
    * processes all commands found in it.
    * 
    * But what does a command look like?
    * [time]command_id;args
    *  
    **/
   public static void check_for_external_commands()
   {
      String buffer;
      String command_id;
      String args;
      long entry_time;
      int command_type= common_h.CMD_NONE;
      int update_status = common_h.FALSE;

      logger.trace( "entering " + cn + ".check_for_external_commands" );

      /* bail out if we shouldn't be checking for external commands */
      if(blue.check_external_commands==common_h.FALSE)
         return;

      /* update last command check time */
      blue.last_command_check= utils.currentTimeInSeconds();

      /* update the status log with new program information */
      /* go easy on the frequency of this if we're checking often - only update program status every 10 seconds.... */
      if(blue.last_command_check<(blue.last_command_status_update+10))
         update_status=common_h.FALSE;
      else
         update_status=common_h.TRUE;

      if(update_status==common_h.TRUE){
         blue.last_command_status_update=blue.last_command_check;
         statusdata.update_program_status(common_h.FALSE);
      }

      /* reset passive check result list pointers */
      passive_check_result_list= new ArrayList<blue_h.passive_check_result>();

      /* process all commands found in the buffer */
      while( true ){

         buffer = (String) blue.external_command_buffer.buffer.poll();
         if ( buffer == null )
            break;

         logger.debug("\tRaw command entry: " + buffer);

         /* get the command entry time */
         String[] split = buffer.split ( "\\[" , 2);
         if ( split.length != 2 )
            continue;

         split = split[1].split( "\\]" , 2 );
         if ( split.length != 2 )
            continue;
         entry_time = utils.strtoul(split[0],null,10);

         /* get the command identifier */
         split = split[1].split (";" , 2);
         command_id = split[0].substring(1);

         /* get the command arguments */
         if ( split.length == 1 )
            args = "";
         else
            args = split [1];

         /* decide what type of command this is... */

         /**************************/
         /**** PROCESS COMMANDS ****/
         /**************************/
         
         if( command_id.equals("ENTER_STANDBY_MODE") || command_id.equals("DISABLE_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_NOTIFICATIONS;
         else if(command_id.equals("ENTER_ACTIVE_MODE") || command_id.equals("ENABLE_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_NOTIFICATIONS;

         else if(command_id.equals("SHUTDOWN_PROGRAM"))
            command_type=common_h.CMD_SHUTDOWN_PROCESS;
         else if(command_id.equals("RESTART_PROGRAM"))
            command_type=common_h.CMD_RESTART_PROCESS;

         else if(command_id.equals("SAVE_STATE_INFORMATION"))
            command_type=common_h.CMD_SAVE_STATE_INFORMATION;
         else if(command_id.equals("READ_STATE_INFORMATION"))
            command_type=common_h.CMD_READ_STATE_INFORMATION;

         else if(command_id.equals("ENABLE_EVENT_HANDLERS"))
            command_type=common_h.CMD_ENABLE_EVENT_HANDLERS;
         else if(command_id.equals("DISABLE_EVENT_HANDLERS"))
            command_type=common_h.CMD_DISABLE_EVENT_HANDLERS;

         else if(command_id.equals("FLUSH_PENDING_COMMANDS"))
            command_type=common_h.CMD_FLUSH_PENDING_COMMANDS;

         else if(command_id.equals("ENABLE_FAILURE_PREDICTION"))
            command_type=common_h.CMD_ENABLE_FAILURE_PREDICTION;
         else if(command_id.equals("DISABLE_FAILURE_PREDICTION"))
            command_type=common_h.CMD_DISABLE_FAILURE_PREDICTION;

         else if(command_id.equals("ENABLE_PERFORMANCE_DATA"))
            command_type=common_h.CMD_ENABLE_PERFORMANCE_DATA;
         else if(command_id.equals("DISABLE_PERFORMANCE_DATA"))
            command_type=common_h.CMD_DISABLE_PERFORMANCE_DATA;

         else if(command_id.equals("START_EXECUTING_HOST_CHECKS"))
            command_type=common_h.CMD_START_EXECUTING_HOST_CHECKS;
         else if(command_id.equals("STOP_EXECUTING_HOST_CHECKS"))
            command_type=common_h.CMD_STOP_EXECUTING_HOST_CHECKS;

         else if(command_id.equals("START_EXECUTING_SVC_CHECKS"))
            command_type=common_h.CMD_START_EXECUTING_SVC_CHECKS;
         else if(command_id.equals("STOP_EXECUTING_SVC_CHECKS"))
            command_type=common_h.CMD_STOP_EXECUTING_SVC_CHECKS;

         else if(command_id.equals("START_ACCEPTING_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS;
         else if(command_id.equals("STOP_ACCEPTING_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS;

         else if(command_id.equals("START_ACCEPTING_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS;
         else if(command_id.equals("STOP_ACCEPTING_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS;

         else if(command_id.equals("START_OBSESSING_OVER_HOST_CHECKS"))
            command_type=common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS;
         else if(command_id.equals("STOP_OBSESSING_OVER_HOST_CHECKS"))
            command_type=common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS;

         else if(command_id.equals("START_OBSESSING_OVER_SVC_CHECKS"))
            command_type=common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS;
         else if(command_id.equals("STOP_OBSESSING_OVER_SVC_CHECKS"))
            command_type=common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS;

         else if(command_id.equals("ENABLE_FLAP_DETECTION"))
            command_type=common_h.CMD_ENABLE_FLAP_DETECTION;
         else if(command_id.equals("DISABLE_FLAP_DETECTION"))
            command_type=common_h.CMD_DISABLE_FLAP_DETECTION;

         else if(command_id.equals("CHANGE_GLOBAL_HOST_EVENT_HANDLER"))
            command_type=common_h.CMD_CHANGE_GLOBAL_HOST_EVENT_HANDLER;
         else if(command_id.equals("CHANGE_GLOBAL_SVC_EVENT_HANDLER"))
            command_type=common_h.CMD_CHANGE_GLOBAL_SVC_EVENT_HANDLER;

         else if(command_id.equals("ENABLE_SERVICE_FRESHNESS_CHECKS"))
            command_type=common_h.CMD_ENABLE_SERVICE_FRESHNESS_CHECKS;
         else if(command_id.equals("DISABLE_SERVICE_FRESHNESS_CHECKS"))
            command_type=common_h.CMD_DISABLE_SERVICE_FRESHNESS_CHECKS;

         else if(command_id.equals("ENABLE_HOST_FRESHNESS_CHECKS"))
            command_type=common_h.CMD_ENABLE_HOST_FRESHNESS_CHECKS;
         else if(command_id.equals("DISABLE_HOST_FRESHNESS_CHECKS"))
            command_type=common_h.CMD_DISABLE_HOST_FRESHNESS_CHECKS;


         /*******************************/
         /**** HOST-RELATED COMMANDS ****/
         /*******************************/

         else if(command_id.equals("ADD_HOST_COMMENT"))
            command_type=common_h.CMD_ADD_HOST_COMMENT;
         else if(command_id.equals("DEL_HOST_COMMENT"))
            command_type=common_h.CMD_DEL_HOST_COMMENT;
         else if(command_id.equals("DEL_ALL_HOST_COMMENTS"))
            command_type=common_h.CMD_DEL_ALL_HOST_COMMENTS;

         else if(command_id.equals("DELAY_HOST_NOTIFICATION"))
            command_type=common_h.CMD_DELAY_HOST_NOTIFICATION;

         else if(command_id.equals("ENABLE_HOST_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_HOST_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_HOST_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_HOST_NOTIFICATIONS;

         else if(command_id.equals("ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST"))
            command_type=common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST;
         else if(command_id.equals("DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST"))
            command_type=common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST;

         else if(command_id.equals("ENABLE_HOST_AND_CHILD_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_HOST_AND_CHILD_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_HOST_AND_CHILD_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_HOST_AND_CHILD_NOTIFICATIONS;

         else if(command_id.equals("ENABLE_HOST_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_HOST_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS;

         else if(command_id.equals("ENABLE_HOST_SVC_CHECKS"))
            command_type=common_h.CMD_ENABLE_HOST_SVC_CHECKS;
         else if(command_id.equals("DISABLE_HOST_SVC_CHECKS"))
            command_type=common_h.CMD_DISABLE_HOST_SVC_CHECKS;

         else if(command_id.equals("ENABLE_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS;
         else if(command_id.equals("DISABLE_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS;

         else if(command_id.equals("SCHEDULE_HOST_SVC_CHECKS"))
            command_type=common_h.CMD_SCHEDULE_HOST_SVC_CHECKS;
         else if(command_id.equals("SCHEDULE_FORCED_HOST_SVC_CHECKS"))
            command_type=common_h.CMD_SCHEDULE_FORCED_HOST_SVC_CHECKS;

         else if(command_id.equals("ACKNOWLEDGE_HOST_PROBLEM"))
            command_type=common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM;
         else if(command_id.equals("REMOVE_HOST_ACKNOWLEDGEMENT"))
            command_type=common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT;

         else if(command_id.equals("ENABLE_HOST_EVENT_HANDLER"))
            command_type=common_h.CMD_ENABLE_HOST_EVENT_HANDLER;
         else if(command_id.equals("DISABLE_HOST_EVENT_HANDLER"))
            command_type=common_h.CMD_DISABLE_HOST_EVENT_HANDLER;

         else if(command_id.equals("ENABLE_HOST_CHECK"))
            command_type=common_h.CMD_ENABLE_HOST_CHECK;
         else if(command_id.equals("DISABLE_HOST_CHECK"))
            command_type=common_h.CMD_DISABLE_HOST_CHECK;

         else if(command_id.equals("SCHEDULE_HOST_CHECK"))
            command_type=common_h.CMD_SCHEDULE_HOST_CHECK;
         else if(command_id.equals("SCHEDULE_FORCED_HOST_CHECK"))
            command_type=common_h.CMD_SCHEDULE_FORCED_HOST_CHECK;

         else if(command_id.equals("SCHEDULE_HOST_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_HOST_DOWNTIME;
         else if(command_id.equals("SCHEDULE_HOST_SVC_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_HOST_SVC_DOWNTIME;
         else if(command_id.equals("DEL_HOST_DOWNTIME"))
            command_type=common_h.CMD_DEL_HOST_DOWNTIME;

         else if(command_id.equals("ENABLE_HOST_FLAP_DETECTION"))
            command_type=common_h.CMD_ENABLE_HOST_FLAP_DETECTION;
         else if(command_id.equals("DISABLE_HOST_FLAP_DETECTION"))
            command_type=common_h.CMD_DISABLE_HOST_FLAP_DETECTION;

         else if(command_id.equals("START_OBSESSING_OVER_HOST"))
            command_type=common_h.CMD_START_OBSESSING_OVER_HOST;
         else if(command_id.equals("STOP_OBSESSING_OVER_HOST"))
            command_type=common_h.CMD_STOP_OBSESSING_OVER_HOST;

         else if(command_id.equals("CHANGE_HOST_EVENT_HANDLER"))
            command_type=common_h.CMD_CHANGE_HOST_EVENT_HANDLER;
         else if(command_id.equals("CHANGE_HOST_CHECK_COMMAND"))
            command_type=common_h.CMD_CHANGE_HOST_CHECK_COMMAND;

         else if(command_id.equals("CHANGE_NORMAL_HOST_CHECK_INTERVAL") )
            command_type=common_h.CMD_CHANGE_NORMAL_HOST_CHECK_INTERVAL;

         else if(command_id.equals("CHANGE_MAX_HOST_CHECK_ATTEMPTS"))
            command_type=common_h.CMD_CHANGE_MAX_HOST_CHECK_ATTEMPTS;

         else if(command_id.equals("SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME;

         else if(command_id.equals("SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME;

         else if(command_id.equals("SET_HOST_NOTIFICATION_NUMBER"))
            command_type=common_h.CMD_SET_HOST_NOTIFICATION_NUMBER;
         
         
         /************************************/
         /**** HOSTGROUP-RELATED COMMANDS ****/
         /************************************/
         
         else if(command_id.equals("ENABLE_HOSTGROUP_HOST_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_HOSTGROUP_HOST_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS;
         
         else if(command_id.equals("ENABLE_HOSTGROUP_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_HOSTGROUP_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS;
         
         else if(command_id.equals("ENABLE_HOSTGROUP_HOST_CHECKS"))
            command_type=common_h.CMD_ENABLE_HOSTGROUP_HOST_CHECKS;
         else if(command_id.equals("DISABLE_HOSTGROUP_HOST_CHECKS"))
            command_type=common_h.CMD_DISABLE_HOSTGROUP_HOST_CHECKS;
         
         else if(command_id.equals("ENABLE_HOSTGROUP_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_ENABLE_HOSTGROUP_PASSIVE_HOST_CHECKS;
         else if(command_id.equals("DISABLE_HOSTGROUP_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_DISABLE_HOSTGROUP_PASSIVE_HOST_CHECKS;
        
         else if(command_id.equals("ENABLE_HOSTGROUP_SVC_CHECKS"))
            command_type=common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS;
         else if(command_id.equals("DISABLE_HOSTGROUP_SVC_CHECKS"))
            command_type=common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS;
         
         else if(command_id.equals("ENABLE_HOSTGROUP_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_ENABLE_HOSTGROUP_PASSIVE_SVC_CHECKS;
         else if(command_id.equals("DISABLE_HOSTGROUP_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_DISABLE_HOSTGROUP_PASSIVE_SVC_CHECKS;
         
         else if(command_id.equals("SCHEDULE_HOSTGROUP_HOST_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME;
         else if(command_id.equals("SCHEDULE_HOSTGROUP_SVC_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME;
         
         
         /**********************************/
         /**** SERVICE-RELATED COMMANDS ****/
         /**********************************/
         
         else if(command_id.equals("ADD_SVC_COMMENT"))
            command_type=common_h.CMD_ADD_SVC_COMMENT;
         else if(command_id.equals("DEL_SVC_COMMENT"))
            command_type=common_h.CMD_DEL_SVC_COMMENT;
         else if(command_id.equals("DEL_ALL_SVC_COMMENTS"))
            command_type=common_h.CMD_DEL_ALL_SVC_COMMENTS;
         
         else if(command_id.equals("SCHEDULE_SVC_CHECK"))
            command_type=common_h.CMD_SCHEDULE_SVC_CHECK;
         else if(command_id.equals("SCHEDULE_FORCED_SVC_CHECK"))
            command_type=common_h.CMD_SCHEDULE_FORCED_SVC_CHECK;
         
         else if(command_id.equals("ENABLE_SVC_CHECK"))
            command_type=common_h.CMD_ENABLE_SVC_CHECK;
         else if(command_id.equals("DISABLE_SVC_CHECK"))
            command_type=common_h.CMD_DISABLE_SVC_CHECK;
         
         else if(command_id.equals("ENABLE_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS;
         else if(command_id.equals("DISABLE_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS;
         
         else if(command_id.equals("DELAY_SVC_NOTIFICATION"))
            command_type=common_h.CMD_DELAY_SVC_NOTIFICATION;
         else if(command_id.equals("ENABLE_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_SVC_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_SVC_NOTIFICATIONS;
         
         else if(command_id.equals("PROCESS_SERVICE_CHECK_RESULT"))
            command_type=common_h.CMD_PROCESS_SERVICE_CHECK_RESULT;
         else if(command_id.equals("PROCESS_HOST_CHECK_RESULT"))
            command_type=common_h.CMD_PROCESS_HOST_CHECK_RESULT;
         
         else if(command_id.equals("ENABLE_SVC_EVENT_HANDLER"))
            command_type=common_h.CMD_ENABLE_SVC_EVENT_HANDLER;
         else if(command_id.equals("DISABLE_SVC_EVENT_HANDLER"))
            command_type=common_h.CMD_DISABLE_SVC_EVENT_HANDLER;
         
         else if(command_id.equals("ENABLE_SVC_FLAP_DETECTION"))
            command_type=common_h.CMD_ENABLE_SVC_FLAP_DETECTION;
         else if(command_id.equals("DISABLE_SVC_FLAP_DETECTION"))
            command_type=common_h.CMD_DISABLE_SVC_FLAP_DETECTION;
         
         else if(command_id.equals("SCHEDULE_SVC_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_SVC_DOWNTIME;
         else if(command_id.equals("DEL_SVC_DOWNTIME"))
            command_type=common_h.CMD_DEL_SVC_DOWNTIME;
         
         else if(command_id.equals("ACKNOWLEDGE_SVC_PROBLEM"))
            command_type=common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM;
         else if(command_id.equals("REMOVE_SVC_ACKNOWLEDGEMENT"))
            command_type=common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT;
         
         else if(command_id.equals("START_OBSESSING_OVER_SVC"))
            command_type=common_h.CMD_START_OBSESSING_OVER_SVC;
         else if(command_id.equals("STOP_OBSESSING_OVER_SVC"))
            command_type=common_h.CMD_STOP_OBSESSING_OVER_SVC;
         
         else if(command_id.equals("CHANGE_SVC_EVENT_HANDLER"))
            command_type=common_h.CMD_CHANGE_SVC_EVENT_HANDLER;
         else if(command_id.equals("CHANGE_SVC_CHECK_COMMAND"))
            command_type=common_h.CMD_CHANGE_SVC_CHECK_COMMAND;
         
         else if(command_id.equals("CHANGE_NORMAL_SVC_CHECK_INTERVAL"))
            command_type=common_h.CMD_CHANGE_NORMAL_SVC_CHECK_INTERVAL;
         else if(command_id.equals("CHANGE_RETRY_SVC_CHECK_INTERVAL"))
            command_type=common_h.CMD_CHANGE_RETRY_SVC_CHECK_INTERVAL;
         
         else if(command_id.equals("CHANGE_MAX_SVC_CHECK_ATTEMPTS"))
            command_type=common_h.CMD_CHANGE_MAX_SVC_CHECK_ATTEMPTS;
         
         else if(command_id.equals("SET_SVC_NOTIFICATION_NUMBER"))
            command_type=common_h.CMD_SET_SVC_NOTIFICATION_NUMBER;
         
         
         /***************************************/
         /**** SERVICEGROUP-RELATED COMMANDS ****/
         /***************************************/
         
         else if(command_id.equals("ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS;
         
         else if(command_id.equals("ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS;
         else if(command_id.equals("DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS"))
            command_type=common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS;
         
         else if(command_id.equals("ENABLE_SERVICEGROUP_HOST_CHECKS"))
            command_type=common_h.CMD_ENABLE_SERVICEGROUP_HOST_CHECKS;
         else if(command_id.equals("DISABLE_SERVICEGROUP_HOST_CHECKS"))
            command_type=common_h.CMD_DISABLE_SERVICEGROUP_HOST_CHECKS;
         
         else if(command_id.equals("ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS;
         else if(command_id.equals("DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS"))
            command_type=common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS;
         
         else if(command_id.equals("ENABLE_SERVICEGROUP_SVC_CHECKS"))
            command_type=common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS;
         else if(command_id.equals("DISABLE_SERVICEGROUP_SVC_CHECKS"))
            command_type=common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS;
         
         else if(command_id.equals("ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS;
         else if(command_id.equals("DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS"))
            command_type=common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS;
         
         else if(command_id.equals("SCHEDULE_SERVICEGROUP_HOST_DOWNTIME"))
           command_type=common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME;
         else if(command_id.equals("SCHEDULE_SERVICEGROUP_SVC_DOWNTIME"))
            command_type=common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME;
         else if ( command_id.equals("EXECUTE_JAVA_COMMAND"))
           	 command_type = common_h.CMD_JAVA_COMMAND;
         
         /** Support commands which are class based **/
         /**** UNKNOWN COMMAND ****/
         else{
            /* log the bad external command */
            logger.warn( "Warning: Unrecognized external command . "+ command_id +";" + args);
            continue;
         }

         /* log the external command */
         buffer = "EXTERNAL COMMAND: "+command_id+";" + args;
         if( command_type == common_h.CMD_PROCESS_SERVICE_CHECK_RESULT ){
            if( blue.log_passive_checks==common_h.TRUE)
               logger.info(buffer);
         } else {
            if( blue.log_external_commands == common_h.TRUE )
               logger.info(buffer);
         }

         /* send data to event broker */
         broker.broker_external_command(broker_h.NEBTYPE_EXTERNALCOMMAND_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,command_type,entry_time,command_id,args,null);

         /* process the command if its not a passive check */
         process_external_command(command_type,entry_time,args);

         /* send data to event broker */
         broker.broker_external_command(broker_h.NEBTYPE_EXTERNALCOMMAND_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,command_type,entry_time,command_id,args,null);
      }
      
      /**** PROCESS ALL PASSIVE SERVICE CHECK RESULTS AT ONE TIME ****/
      if( passive_check_result_list!=null && !passive_check_result_list.isEmpty() )
         process_passive_service_checks();
      
      logger.trace( "exiting " + cn + ".check_for_external_commands" );
      
   }
   
   /* top-level processor for a single external command */
   public static void process_external_command(int cmd, long entry_time, String args){
      
      logger.trace( "entering " + cn + ".process_external_command" );
      
      logger.debug("\tExternal Command Type: " + cmd);
      logger.debug("\tCommand Entry Time: " + entry_time);
      logger.debug("\tCommand Arguments: " + args);
      
      /* how shall we execute the command? */
      switch(cmd){
         
         /***************************/
         /***** SYSTEM COMMANDS *****/
         /***************************/
         
         case common_h.CMD_SHUTDOWN_PROCESS:
         case common_h.CMD_RESTART_PROCESS:
            cmd_signal_process(cmd,args);
            break;
            
         case common_h.CMD_SAVE_STATE_INFORMATION:
            sretention.save_state_information(blue.config_file,common_h.FALSE);
            break;
            
         case common_h.CMD_READ_STATE_INFORMATION:
            sretention.read_initial_state_information( blue.config_file);
            break;
            
         case common_h.CMD_ENABLE_NOTIFICATIONS:
            enable_all_notifications();
            break;
            
         case common_h.CMD_DISABLE_NOTIFICATIONS:
            disable_all_notifications();
            break;
            
         case common_h.CMD_START_EXECUTING_SVC_CHECKS:
            start_executing_service_checks();
            break;
            
         case common_h.CMD_STOP_EXECUTING_SVC_CHECKS:
            stop_executing_service_checks();
            break;
            
         case common_h.CMD_START_ACCEPTING_PASSIVE_SVC_CHECKS:
            start_accepting_passive_service_checks();
            break;
            
         case common_h.CMD_STOP_ACCEPTING_PASSIVE_SVC_CHECKS:
            stop_accepting_passive_service_checks();
            break;
            
         case common_h.CMD_START_OBSESSING_OVER_SVC_CHECKS:
            start_obsessing_over_service_checks();
            break;
            
         case common_h.CMD_STOP_OBSESSING_OVER_SVC_CHECKS:
            stop_obsessing_over_service_checks();
            break;
            
         case common_h.CMD_START_EXECUTING_HOST_CHECKS:
            start_executing_host_checks();
            break;
            
         case common_h.CMD_STOP_EXECUTING_HOST_CHECKS:
            stop_executing_host_checks();
            break;
            
         case common_h.CMD_START_ACCEPTING_PASSIVE_HOST_CHECKS:
            start_accepting_passive_host_checks();
            break;
            
         case common_h.CMD_STOP_ACCEPTING_PASSIVE_HOST_CHECKS:
            stop_accepting_passive_host_checks();
            break;
            
         case common_h.CMD_START_OBSESSING_OVER_HOST_CHECKS:
            start_obsessing_over_host_checks();
            break;
            
         case common_h.CMD_STOP_OBSESSING_OVER_HOST_CHECKS:
            stop_obsessing_over_host_checks();
            break;
            
         case common_h.CMD_ENABLE_EVENT_HANDLERS:
            start_using_event_handlers();
            break;
            
         case common_h.CMD_DISABLE_EVENT_HANDLERS:
            stop_using_event_handlers();
            break;
            
         case common_h.CMD_ENABLE_FLAP_DETECTION:
            flapping.enable_flap_detection_routines();
            break;
            
         case common_h.CMD_DISABLE_FLAP_DETECTION:
            flapping.disable_flap_detection_routines();
            break;
            
         case common_h.CMD_ENABLE_SERVICE_FRESHNESS_CHECKS:
            enable_service_freshness_checks();
            break;
            
         case common_h.CMD_DISABLE_SERVICE_FRESHNESS_CHECKS:
            disable_service_freshness_checks();
            break;
            
         case common_h.CMD_ENABLE_HOST_FRESHNESS_CHECKS:
            enable_host_freshness_checks();
            break;
            
         case common_h.CMD_DISABLE_HOST_FRESHNESS_CHECKS:
            disable_host_freshness_checks();
            break;
            
         case common_h.CMD_ENABLE_FAILURE_PREDICTION:
            enable_all_failure_prediction();
            break;
            
         case common_h.CMD_DISABLE_FAILURE_PREDICTION:
            disable_all_failure_prediction();
            break;
            
         case common_h.CMD_ENABLE_PERFORMANCE_DATA:
            enable_performance_data();
            break;
            
         case common_h.CMD_DISABLE_PERFORMANCE_DATA:
            disable_performance_data();
            break;
            
            
            /***************************/
            /*****  HOST COMMANDS  *****/
            /***************************/
            
         case common_h.CMD_ENABLE_HOST_CHECK:
         case common_h.CMD_DISABLE_HOST_CHECK:
         case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
         case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
         case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
         case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
         case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
         case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
         case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
         case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
         case common_h.CMD_ENABLE_HOST_AND_CHILD_NOTIFICATIONS:
         case common_h.CMD_DISABLE_HOST_AND_CHILD_NOTIFICATIONS:
         case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
         case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
         case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
         case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
         case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
         case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
         case common_h.CMD_START_OBSESSING_OVER_HOST:
         case common_h.CMD_STOP_OBSESSING_OVER_HOST:
         case common_h.CMD_SET_HOST_NOTIFICATION_NUMBER:
            process_host_command(cmd,entry_time,args);
            break;
            
            
            /*****************************/
            /***** HOSTGROUP COMMANDS ****/
            /*****************************/
            
         case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
         case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
         case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
         case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
         case common_h.CMD_ENABLE_HOSTGROUP_HOST_CHECKS:
         case common_h.CMD_DISABLE_HOSTGROUP_HOST_CHECKS:
         case common_h.CMD_ENABLE_HOSTGROUP_PASSIVE_HOST_CHECKS:
         case common_h.CMD_DISABLE_HOSTGROUP_PASSIVE_HOST_CHECKS:
         case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
         case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
         case common_h.CMD_ENABLE_HOSTGROUP_PASSIVE_SVC_CHECKS:
         case common_h.CMD_DISABLE_HOSTGROUP_PASSIVE_SVC_CHECKS:
            process_hostgroup_command(cmd,entry_time,args);
            break;
            
            
            /***************************/
            /***** SERVICE COMMANDS ****/
            /***************************/
            
         case common_h.CMD_ENABLE_SVC_CHECK:
         case common_h.CMD_DISABLE_SVC_CHECK:
         case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
         case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
         case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
         case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
         case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
         case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
         case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
         case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
         case common_h.CMD_START_OBSESSING_OVER_SVC:
         case common_h.CMD_STOP_OBSESSING_OVER_SVC:
         case common_h.CMD_SET_SVC_NOTIFICATION_NUMBER:
            process_service_command(cmd,entry_time,args);
            break;
            
            
            /********************************/
            /***** SERVICEGROUP COMMANDS ****/
            /********************************/
            
         case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
         case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
         case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
         case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
         case common_h.CMD_ENABLE_SERVICEGROUP_HOST_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_HOST_CHECKS:
         case common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS:
         case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
         case common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS:
            process_servicegroup_command(cmd,entry_time,args);
            break;
            
            
            /***************************/
            /**** UNSORTED COMMANDS ****/
            /***************************/
            
            
         case common_h.CMD_ADD_HOST_COMMENT:
         case common_h.CMD_ADD_SVC_COMMENT:
            cmd_add_comment(cmd,entry_time,args);
            break;
            
         case common_h.CMD_DEL_HOST_COMMENT:
         case common_h.CMD_DEL_SVC_COMMENT:
            cmd_delete_comment(cmd,args);
            break;
            
         case common_h.CMD_DELAY_HOST_NOTIFICATION:
         case common_h.CMD_DELAY_SVC_NOTIFICATION:
            cmd_delay_notification(cmd,args);
            break;
            
         case common_h.CMD_SCHEDULE_SVC_CHECK:
         case common_h.CMD_SCHEDULE_FORCED_SVC_CHECK:
            cmd_schedule_check(cmd,args);
            break;
            
         case common_h.CMD_SCHEDULE_HOST_SVC_CHECKS:
         case common_h.CMD_SCHEDULE_FORCED_HOST_SVC_CHECKS:
            cmd_schedule_check(cmd,args);
            break;
            
         case common_h.CMD_DEL_ALL_HOST_COMMENTS:
         case common_h.CMD_DEL_ALL_SVC_COMMENTS:
            cmd_delete_all_comments(cmd,args);
            break;
            
         case common_h.CMD_PROCESS_SERVICE_CHECK_RESULT:
            cmd_process_service_check_result(cmd,entry_time,args);
            break;
            
         case common_h.CMD_PROCESS_HOST_CHECK_RESULT:
            cmd_process_host_check_result(cmd,entry_time,args);
            break;
            
         case common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM:
         case common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM:
            cmd_acknowledge_problem(cmd,args);
            break;
            
         case common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT:
         case common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT:
            cmd_remove_acknowledgement(cmd,args);
            break;
            
         case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
         case common_h.CMD_SCHEDULE_SVC_DOWNTIME:
         case common_h.CMD_SCHEDULE_HOST_SVC_DOWNTIME:
         case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
         case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME:
         case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
         case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:
         case common_h.CMD_SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME:
         case common_h.CMD_SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME:
            cmd_schedule_downtime(cmd,entry_time,args);
            break;
            
         case common_h.CMD_DEL_HOST_DOWNTIME:
         case common_h.CMD_DEL_SVC_DOWNTIME:
            cmd_delete_downtime(cmd,args);
            break;
            
         case common_h.CMD_CANCEL_ACTIVE_HOST_SVC_DOWNTIME:
         case common_h.CMD_CANCEL_PENDING_HOST_SVC_DOWNTIME:
            break;
            
         case common_h.CMD_SCHEDULE_HOST_CHECK:
         case common_h.CMD_SCHEDULE_FORCED_HOST_CHECK:
            cmd_schedule_check(cmd,args);
            break;
            
         case common_h.CMD_CHANGE_GLOBAL_HOST_EVENT_HANDLER:
         case common_h.CMD_CHANGE_GLOBAL_SVC_EVENT_HANDLER:
         case common_h.CMD_CHANGE_HOST_EVENT_HANDLER:
         case common_h.CMD_CHANGE_SVC_EVENT_HANDLER:
         case common_h.CMD_CHANGE_HOST_CHECK_COMMAND:
         case common_h.CMD_CHANGE_SVC_CHECK_COMMAND:
            cmd_change_command(cmd,args);
            break;
            
         case common_h.CMD_CHANGE_NORMAL_HOST_CHECK_INTERVAL:
         case common_h.CMD_CHANGE_NORMAL_SVC_CHECK_INTERVAL:
         case common_h.CMD_CHANGE_RETRY_SVC_CHECK_INTERVAL:
            cmd_change_check_interval(cmd,args);
            break;
            
         case common_h.CMD_CHANGE_MAX_HOST_CHECK_ATTEMPTS:
         case common_h.CMD_CHANGE_MAX_SVC_CHECK_ATTEMPTS:
            cmd_change_max_attempts(cmd,args);
            break;
            
         case common_h.CMD_JAVA_COMMAND:
        	process_java_command(cmd,args);
            break;
            
         default:
            break;
      }
      
      logger.trace( "exiting " + cn + ".process_external_command" );
      
   }
   
   public static void process_java_command( long entry_time, String args )
   {
	  /* get the host name */
      String[] split = args.split(";",2);
      String cmd = split[0].trim();
      
      String otherArgs = split.length==2?split[1]:null;

      try
      {
         Class commandClass = Class.forName( cmd );
         ICommand command = (ICommand) commandClass.newInstance();
         command.processCommand(entry_time,otherArgs);
      } catch ( Exception e )
      { 
          e.printStackTrace();
    	  logger.warn( "Warning: Unrecognized java command ("+e.getMessage()+"). "+ cmd +";" + otherArgs);
      }

   }
   
   /* processes an external host command */
   public static int process_host_command(int cmd, long entry_time, String args){
      String host_name=null;
      objects_h.host temp_host=null;
//    String str=null;
//    int intval;
      
      /* get the host name */
      String[] split = args.split(";");
      host_name = split[0].trim();
      if(host_name.length() == 0)
         return common_h.ERROR;
      
      /* find the host */
      temp_host= objects.find_host(host_name);
      if(temp_host==null)
         return common_h.ERROR;
      
      switch(cmd){
         
         case common_h.CMD_ENABLE_HOST_NOTIFICATIONS:
            enable_host_notifications(temp_host);
            break;
            
         case common_h.CMD_DISABLE_HOST_NOTIFICATIONS:
            disable_host_notifications(temp_host);
            break;
            
         case common_h.CMD_ENABLE_HOST_AND_CHILD_NOTIFICATIONS:
            enable_and_propagate_notifications(temp_host,0,common_h.TRUE,common_h.TRUE,common_h.FALSE);
            break;
            
         case common_h.CMD_DISABLE_HOST_AND_CHILD_NOTIFICATIONS:
            disable_and_propagate_notifications(temp_host,0,common_h.TRUE,common_h.TRUE,common_h.FALSE);
            break;
            
         case common_h.CMD_ENABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
            enable_and_propagate_notifications(temp_host,0,common_h.FALSE,common_h.TRUE,common_h.TRUE);
            
         case common_h.CMD_DISABLE_ALL_NOTIFICATIONS_BEYOND_HOST:
            disable_and_propagate_notifications(temp_host,0,common_h.FALSE,common_h.TRUE,common_h.TRUE);
            
         case common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS:
         case common_h.CMD_DISABLE_HOST_SVC_NOTIFICATIONS:
            for ( objects_h.service temp_service : objects.service_list ) {
               if( temp_service.host_name.equals(host_name)){
                  if(cmd==common_h.CMD_ENABLE_HOST_SVC_NOTIFICATIONS)
                     enable_service_notifications(temp_service);
                  else
                     disable_service_notifications(temp_service);
               }
            }
            break;
            
         case common_h.CMD_ENABLE_HOST_SVC_CHECKS:
         case common_h.CMD_DISABLE_HOST_SVC_CHECKS:
            for ( objects_h.service temp_service : objects.service_list ) {
               if(temp_service.host_name.equals(host_name)){
                  if(cmd==common_h.CMD_ENABLE_HOST_SVC_CHECKS)
                     enable_service_checks(temp_service);
                  else
                     disable_service_checks(temp_service);
               }
            } 
            break;
            
         case common_h.CMD_ENABLE_HOST_CHECK:
            enable_host_checks(temp_host);
            break;
            
         case common_h.CMD_DISABLE_HOST_CHECK:
            disable_host_checks(temp_host);
            break;
            
         case common_h.CMD_ENABLE_HOST_EVENT_HANDLER:
            enable_host_event_handler(temp_host);
            break;
            
         case common_h.CMD_DISABLE_HOST_EVENT_HANDLER:
            disable_host_event_handler(temp_host);
            break;
            
         case common_h.CMD_ENABLE_HOST_FLAP_DETECTION:
            flapping.enable_host_flap_detection(temp_host);
            break;
            
         case common_h.CMD_DISABLE_HOST_FLAP_DETECTION:
            flapping.disable_host_flap_detection(temp_host);
            break;
            
         case common_h.CMD_ENABLE_PASSIVE_HOST_CHECKS:
            enable_passive_host_checks(temp_host);
            break;
            
         case common_h.CMD_DISABLE_PASSIVE_HOST_CHECKS:
            disable_passive_host_checks(temp_host);
            break;
            
         case common_h.CMD_START_OBSESSING_OVER_HOST:
            start_obsessing_over_host(temp_host);
            break;
            
         case common_h.CMD_STOP_OBSESSING_OVER_HOST:
            stop_obsessing_over_host(temp_host);
            break;
            
         case common_h.CMD_SET_HOST_NOTIFICATION_NUMBER:
            
            if( split.length > 1  ) {
               int intval= utils.atoi(split[1]);
               set_host_notification_number(temp_host,intval);
            }
            break;
            
         default:
            break;
      }
      
      return common_h.OK;
   }
   
   /* processes an external hostgroup command */
   public static int process_hostgroup_command(int cmd, long entry_time, String args){
      String hostgroup_name=null;
      objects_h.hostgroup temp_hostgroup=null;
      
      /* get the hostgroup name */
      String[] split = args.split(";");
      hostgroup_name = split[0].trim();
      if(hostgroup_name.length() == 0)
         return common_h.ERROR;
      
      /* find the hostgroup */
      temp_hostgroup= objects.find_hostgroup(hostgroup_name);
      if(temp_hostgroup==null)
         return common_h.ERROR;
      
      /* loop through all hosts in the hostgroup */
      for ( objects_h.hostgroupmember temp_member : temp_hostgroup.members ) {
         
         objects_h.host temp_host= objects.find_host(temp_member.host_name);
         if(temp_host==null)
            continue;
         
         switch(cmd){
            
            case common_h.CMD_ENABLE_HOSTGROUP_HOST_NOTIFICATIONS:
               enable_host_notifications(temp_host);
               break;
               
            case common_h.CMD_DISABLE_HOSTGROUP_HOST_NOTIFICATIONS:
               disable_host_notifications(temp_host);
               break;
               
            case common_h.CMD_ENABLE_HOSTGROUP_HOST_CHECKS:
               enable_host_checks(temp_host);
               break;
               
            case common_h.CMD_DISABLE_HOSTGROUP_HOST_CHECKS:
               disable_host_checks(temp_host);
               break;
               
            case common_h.CMD_ENABLE_HOSTGROUP_PASSIVE_HOST_CHECKS:
               enable_passive_host_checks(temp_host);
               break;
               
            case common_h.CMD_DISABLE_HOSTGROUP_PASSIVE_HOST_CHECKS:
               disable_passive_host_checks(temp_host);
               break;
               
            default:
               
               /* loop through all services on the host */
               for ( objects_h.service temp_service : objects.service_list ) {
                  if( temp_service.host_name.equals( temp_host.name)){
                     
                     switch(cmd){
                        
                        case common_h.CMD_ENABLE_HOSTGROUP_SVC_NOTIFICATIONS:
                           enable_service_notifications(temp_service);
                           break;
                           
                        case common_h.CMD_DISABLE_HOSTGROUP_SVC_NOTIFICATIONS:
                           disable_service_notifications(temp_service);
                           break;
                           
                        case common_h.CMD_ENABLE_HOSTGROUP_SVC_CHECKS:
                           enable_service_checks(temp_service);
                           break;
                           
                        case common_h.CMD_DISABLE_HOSTGROUP_SVC_CHECKS:
                           disable_service_checks(temp_service);
                           break;
                           
                        case common_h.CMD_ENABLE_HOSTGROUP_PASSIVE_SVC_CHECKS:
                           enable_passive_service_checks(temp_service);
                           break;
                           
                        case common_h.CMD_DISABLE_HOSTGROUP_PASSIVE_SVC_CHECKS:
                           disable_passive_service_checks(temp_service);
                           break;
                           
                        default:
                           break;
                     }
                  }
               }
            
            break;
         }
         
      }
      
      return common_h.OK;
   }
   
   /* processes an external service command */
   public static int process_service_command(int cmd, long entry_time, String args){
      String host_name=null;
      String svc_description=null;
//    objects_h.service temp_service=null;
//    String str=null;
//    int intval;
      
      String[] split = args.split(";");
      if ( split.length < 2 )
         return common_h.ERROR;
      
      /* get the host name */
      host_name = split[0].trim();
      
      /* get the service description */
      svc_description=split[1].trim();
      
      if(host_name.length() == 0 || svc_description.length() == 0)
         return common_h.ERROR;
      
      /* find the objects_h.service */
      objects_h.service temp_service= objects.find_service(host_name,svc_description);
      if(temp_service==null)
         return common_h.ERROR;
      
      switch(cmd){
         
         case common_h.CMD_ENABLE_SVC_NOTIFICATIONS:
            enable_service_notifications(temp_service);
            break;
            
         case common_h.CMD_DISABLE_SVC_NOTIFICATIONS:
            disable_service_notifications(temp_service);
            break;
            
         case common_h.CMD_ENABLE_SVC_CHECK:
            enable_service_checks(temp_service);
            break;
            
         case common_h.CMD_DISABLE_SVC_CHECK:
            disable_service_checks(temp_service);
            break;
            
         case common_h.CMD_ENABLE_SVC_EVENT_HANDLER:
            enable_service_event_handler(temp_service);
            break;
            
         case common_h.CMD_DISABLE_SVC_EVENT_HANDLER:
            disable_service_event_handler(temp_service);
            break;
            
         case common_h.CMD_ENABLE_SVC_FLAP_DETECTION:
            flapping.enable_service_flap_detection(temp_service);
            break;
            
         case common_h.CMD_DISABLE_SVC_FLAP_DETECTION:
            flapping.disable_service_flap_detection(temp_service);
            break;
            
         case common_h.CMD_ENABLE_PASSIVE_SVC_CHECKS:
            enable_passive_service_checks(temp_service);
            break;
            
         case common_h.CMD_DISABLE_PASSIVE_SVC_CHECKS:
            disable_passive_service_checks(temp_service);
            break;
            
         case common_h.CMD_START_OBSESSING_OVER_SVC:
            start_obsessing_over_service(temp_service);
            break;
            
         case common_h.CMD_STOP_OBSESSING_OVER_SVC:
            stop_obsessing_over_service(temp_service);
            break;
            
         case common_h.CMD_SET_SVC_NOTIFICATION_NUMBER:
            if( split.length > 2  ) {
               int intval= utils.atoi(split[1]);
               set_service_notification_number(temp_service,intval);
            }
            break;
            
         default:
            break;
      }
      
      return common_h.OK;
   }
   
   /* processes an external servicegroup command */
   public static int process_servicegroup_command(int cmd, long entry_time, String args){
      String servicegroup_name=null;
      objects_h.servicegroup temp_servicegroup=null;
//    objects_h.host temp_host=null;
//    objects_h.host last_host=null;
      objects_h.service temp_service=null;
      
      /* get the servicegroup name */
      String[] split = args.split(";");
      servicegroup_name = split[0].trim();
      if(servicegroup_name.length() == 0)
         return common_h.ERROR;
      
      /* find the servicegroup */
      temp_servicegroup= objects.find_servicegroup(servicegroup_name);
      if(temp_servicegroup==null)
         return common_h.ERROR;
      
      switch(cmd){
         
         case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
         case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
         case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
         case common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS:
            
            /* loop through all servicegroup members */
            for ( objects_h.servicegroupmember temp_member : temp_servicegroup.members ) {
               
               temp_service = objects.find_service(temp_member.host_name,temp_member.service_description);
               if(temp_service==null)
                  continue;
               
               switch(cmd){
                  
                  case common_h.CMD_ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
                     enable_service_notifications(temp_service);
                     break;
                     
                  case common_h.CMD_DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS:
                     disable_service_notifications(temp_service);
                     break;
                     
                  case common_h.CMD_ENABLE_SERVICEGROUP_SVC_CHECKS:
                     enable_service_checks(temp_service);
                     break;
                     
                  case common_h.CMD_DISABLE_SERVICEGROUP_SVC_CHECKS:
                     disable_service_checks(temp_service);
                     break;
                     
                  case common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS:
                     enable_passive_service_checks(temp_service);
                     break;
                     
                  case common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_SVC_CHECKS:
                     disable_passive_service_checks(temp_service);
                     break;
                     
                  default:
                     break;
               }
            }
            
            break;
            
         case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
         case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
         case common_h.CMD_ENABLE_SERVICEGROUP_HOST_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_HOST_CHECKS:
         case common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS:
         case common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS:
            
            /* loop through all hosts that have services belonging to the servicegroup */
            objects_h.host last_host=null;
            for ( objects_h.servicegroupmember temp_member : temp_servicegroup.members ) {
               
               objects_h.host temp_host= objects.find_host(temp_member.host_name);
               if(temp_host==null)
                  continue;
               
               if(temp_host==last_host)
                  continue;
               
               switch(cmd){
                  
                  case common_h.CMD_ENABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
                     enable_host_notifications(temp_host);
                     break;
                     
                  case common_h.CMD_DISABLE_SERVICEGROUP_HOST_NOTIFICATIONS:
                     disable_host_notifications(temp_host);
                     break;
                     
                  case common_h.CMD_ENABLE_SERVICEGROUP_HOST_CHECKS:
                     enable_host_checks(temp_host);
                     break;
                     
                  case common_h.CMD_DISABLE_SERVICEGROUP_HOST_CHECKS:
                     disable_host_checks(temp_host);
                     break;
                     
                  case common_h.CMD_ENABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS:
                     enable_passive_host_checks(temp_host);
                     break;
                     
                  case common_h.CMD_DISABLE_SERVICEGROUP_PASSIVE_HOST_CHECKS:
                     disable_passive_host_checks(temp_host);
                     break;
                     
                  default:
                     break;
               }
               
               last_host=temp_host;
            }
            
            break;
            
         default:
            break;
      }
      
      return common_h.OK;
   }
   
   /******************************************************************/
   /*************** EXTERNAL COMMAND IMPLEMENTATIONS  ****************/
   /******************************************************************/
   
   /* adds a host or service comment to the status log */
   public static int cmd_add_comment(int cmd,long entry_time,String args){
      String temp_ptr;
      objects_h.host temp_host;
      objects_h.service temp_service;
      String host_name="";
      String svc_description="";
      String user;
      String comment_data;
      int persistent=0;
      logger.trace( "entering " + cn + ".cmd_add_comment" );
      
      String split[] = args.split ( ";" );
      int x = 0;
      
      /* get the host name */
      host_name= getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      /* if we're adding a service comment...  */
      if(cmd==common_h.CMD_ADD_SVC_COMMENT){
         
         /* get the service description */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(host_name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      
      /* else verify that the host is valid */
      temp_host= objects.find_host(host_name);
      if(temp_host==null)
         return common_h.ERROR;
      
      /* get the persistent flag */
      temp_ptr=getSplit( split, x++);
      if(temp_ptr==null)
         return common_h.ERROR;
      persistent=utils.atoi(temp_ptr);
      if(persistent>1)
         persistent=1;
      else if(persistent<0)
         persistent=0;
      
      /* get the name of the user who entered the comment */
      user=getSplit( split, x++);
      if(user==null)
         return common_h.ERROR;
      
      /* get the comment */
      comment_data=getSplit( split, x++);
      if(comment_data==null)
         return common_h.ERROR;
      
      /* add the comment */
      comments_h.comment comment = comments.add_new_comment((cmd==common_h.CMD_ADD_HOST_COMMENT)?comments_h.HOST_COMMENT:comments_h.SERVICE_COMMENT,comments_h.USER_COMMENT,host_name,svc_description,entry_time,user,comment_data,persistent,comments_h.COMMENTSOURCE_EXTERNAL,common_h.FALSE,0);
      if(comment == null )
         return common_h.ERROR;
      
      logger.trace( "exiting " + cn + ".cmd_add_comment" );
      return common_h.OK;
   }
   
   /* removes a host or service comment from the status log */
   public static int cmd_delete_comment(int cmd,String args){
      long comment_id;
      
      logger.trace( "entering " + cn + ".cmd_del_comment" );
      
      /* get the comment id we should delete */
      comment_id=utils.strtoul(args,null,10);
      if(comment_id==0)
         return common_h.ERROR;
      
      /* delete the specified comment */
      if(cmd==common_h.CMD_DEL_HOST_COMMENT)
         comments.delete_host_comment(comment_id);
      else
         comments.delete_service_comment(comment_id);
      
      logger.trace( "exiting " + cn + ".cmd_del_comment" );
      return common_h.OK;
   }
   
   /* removes all comments associated with a host or service from the status log */
   public static int cmd_delete_all_comments(int cmd,String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      String host_name="";
      String svc_description="";
      
      logger.trace( "entering " + cn + ".cmd_del_all_comments" );
      
      /* get the host name */
      String split[] = args.split ( ";" );
      host_name=split[0];
      if(host_name==null || host_name.length()==0)
         return common_h.ERROR;
      
      /* if we're deleting service comments...  */
      if(cmd==common_h.CMD_DEL_ALL_SVC_COMMENTS){
         
         /* get the service description */
         svc_description=getSplit( split, 1 );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(host_name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      
      /* else verify that the host is valid */
      temp_host=objects.find_host(host_name);
      if(temp_host==null)
         return common_h.ERROR;
      
      /* delete comments */
      comments.delete_all_comments((cmd==common_h.CMD_DEL_ALL_HOST_COMMENTS)?comments_h.HOST_COMMENT:comments_h.SERVICE_COMMENT,host_name,svc_description);
      
      logger.trace( "exiting " + cn + ".cmd_del_all_comments" );
      return common_h.OK;
   }
   
   /* delays a host or service notification for given number of minutes */
   public static int cmd_delay_notification(int cmd,String args){
      String temp_ptr;
      objects_h.host temp_host=null;
      objects_h.service temp_service=null;
      String host_name="";
      String svc_description="";
      long delay_time;
      
      logger.trace( "entering " + cn + ".cmd_delay_notification" );
      String split[] = args.split ( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      /* if this is a service notification delay...  */
      if(cmd==common_h.CMD_DELAY_SVC_NOTIFICATION){
         
         /* get the service description */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(host_name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      
      /* else verify that the host is valid */
      else{
         
         temp_host=objects.find_host(host_name);
         if(temp_host==null)
            return common_h.ERROR;
      }
      
      /* get the time that we should delay until... */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      delay_time=utils.strtoul(temp_ptr,null,10);
      
      /* delay the next notification... */
      if(cmd==common_h.CMD_DELAY_HOST_NOTIFICATION)
         temp_host.next_host_notification=delay_time;
      else
         temp_service.next_notification=delay_time;
      
      logger.trace( "exiting " + cn + ".cmd_delay_notification" );
      return common_h.OK;
   }
   
   /* schedules a host check at a particular time */
   public static int cmd_schedule_check(int cmd,String args){
      String temp_ptr;
      objects_h.host temp_host=null;
      objects_h.service temp_service = null;
      String host_name="";
      String svc_description="";
      long delay_time=0L;
      
      logger.trace( "entering " + cn + ".cmd_schedule_check" );
      String split[] = args.split ( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      if(cmd==common_h.CMD_SCHEDULE_HOST_CHECK || cmd==common_h.CMD_SCHEDULE_FORCED_HOST_CHECK || cmd==common_h.CMD_SCHEDULE_HOST_SVC_CHECKS || cmd==common_h.CMD_SCHEDULE_FORCED_HOST_SVC_CHECKS){
         
         /* verify that the host is valid */
         temp_host=objects.find_host(host_name);
         if(temp_host==null)
            return common_h.ERROR;
      }
      
      else{
         
         /* get the service description */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(host_name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      
      /* get the next check time */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      delay_time=utils.strtoul(temp_ptr,null,10);
      
      /* schedule the check */
      if(cmd==common_h.CMD_SCHEDULE_HOST_CHECK || cmd==common_h.CMD_SCHEDULE_FORCED_HOST_CHECK)
         checks.schedule_host_check(temp_host,delay_time,(cmd==common_h.CMD_SCHEDULE_FORCED_HOST_CHECK)?common_h.TRUE:common_h.FALSE);
      else if(cmd==common_h.CMD_SCHEDULE_HOST_SVC_CHECKS || cmd==common_h.CMD_SCHEDULE_FORCED_HOST_SVC_CHECKS){
         for ( objects_h.service iter_service : objects.service_list ) {
            if(iter_service.host_name.equals(host_name))
               checks.schedule_service_check(iter_service,delay_time,(cmd==common_h.CMD_SCHEDULE_FORCED_HOST_SVC_CHECKS)?common_h.TRUE:common_h.FALSE);
         }
      }
      else
         checks.schedule_service_check(temp_service,delay_time,(cmd==common_h.CMD_SCHEDULE_FORCED_SVC_CHECK)?common_h.TRUE:common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".cmd_schedule_check" );
      return common_h.OK;
   }
   
   /* schedules all service checks on a host for a particular time */
   public static int cmd_schedule_host_service_checks(int cmd,String args, int force){
      String temp_ptr;
      objects_h.host temp_host=null;
      String host_name="";
      long delay_time=0L;
      
      logger.trace( "entering " + cn + ".cmd_schedule_host_service_checks" );
      
      String[] split = args.split( "\n" );
      int x=0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      /* verify that the host is valid */
      temp_host=objects.find_host(host_name);
      if(temp_host==null)
         return common_h.ERROR;
      
      /* get the next check time */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      delay_time=utils.strtoul(temp_ptr,null,10);
      
      /* reschedule all services on the specified host */
      for ( objects_h.service temp_service : objects.service_list ) {
         if( temp_service.host_name.equals(host_name))
            checks.schedule_service_check(temp_service,delay_time,force);
      }
      
      logger.trace( "exiting " + cn + ".cmd_schedule_host_service_checks" );
      return common_h.OK;
   }
   
   /* schedules a program shutdown or restart */
   public static int cmd_signal_process(int cmd, String args){
      long scheduled_time;
      
      logger.trace( "entering " + cn + ".cmd_signal_process" );
      
      if ( args == null || args.trim().length() == 0) {
         scheduled_time = 0;
      } else {
         /* get the time to schedule the event */
         String[] split = args.split( "\n" );
         scheduled_time= utils.strtoul(split[0],null,10);
      }
      
      /* add a scheduled program shutdown or restart to the event list */
      int result = events.schedule_new_event((cmd==common_h.CMD_SHUTDOWN_PROCESS)?blue_h.EVENT_PROGRAM_SHUTDOWN:blue_h.EVENT_PROGRAM_RESTART,common_h.TRUE,scheduled_time,common_h.FALSE,0,null,common_h.FALSE,null,null);
      
      logger.trace( "exiting " + cn + ".cmd_signal_process" );
      
      return result;
   }
   
   /* processes results of an external service check */
   public static int cmd_process_service_check_result(int cmd,long check_time,String args){
      String host_name;
      String svc_description;
      int return_code;
      String output = "";
      int result;
      
      logger.trace( "entering " + cn + ".cmd_process_service_check_result" );
      
      String[] split = args.split( ";" );
      if ( split.length < 3 ) 
         return common_h.ERROR;
      
      /* get the host name */
      host_name=split[0];
      
      /* get the service description */
      svc_description=split[1];
      
      /* get the service check return code */
      return_code=utils.atoi(split[2]);
      
      /* get the plugin output (may be empty) */
      if (split.length > 3 ) 
         output=split[3];
      
      /* submit the passive check result */
      result = process_passive_service_check(check_time,host_name,svc_description,return_code,output);
      
      logger.trace( "exiting " + cn + ".cmd_process_service_check_result" );
      
      return result;
   }
   
   /* submits a passive service check result for later processing */
   public static int process_passive_service_check(long check_time, String host_name, String svc_description, int return_code, String output){
      objects_h.service temp_service=null;
      String real_host_name=null;
      
      logger.trace( "entering " + cn + ".process_passive_service_check" );
      
      /* skip this service check result if we aren't accepting passive service checks */
      if( blue.accept_passive_service_checks==common_h.FALSE)
         return common_h.ERROR;
      
      /* make sure we have all required data */
      if(host_name==null || svc_description==null || output==null)
         return common_h.ERROR;
      
      /* find the host by its name or address */
      if( objects.find_host(host_name)!=null)
         real_host_name=host_name;
      else{
         for ( objects_h.host temp_host : objects.host_list ) {
            if( host_name.equals(temp_host.address)){
               real_host_name=temp_host.name;
               break;
            }
         }
      }
      
      /* we couldn't find the host */
      if(real_host_name==null)
         return common_h.ERROR;
      
      /* make sure the service exists */
      temp_service = objects.find_service(real_host_name,svc_description);
      if( temp_service ==null)
         return common_h.ERROR;
      
      /* skip this is we aren't accepting passive checks for this objects_h.service */
      if(temp_service.accept_passive_service_checks==common_h.FALSE)
         return common_h.ERROR;
      
      /* allocate memory for the passive check result */
      blue_h.passive_check_result new_pcr= new blue_h.passive_check_result();
      
      /* save the host name */
      new_pcr.host_name=real_host_name;
      
      /* save the service description */
      new_pcr.svc_description=svc_description;
      
      /* save the return code */
      new_pcr.return_code=return_code;
      
      /* make sure the return code is within bounds */
      if(new_pcr.return_code<0 || new_pcr.return_code>3)
         new_pcr.return_code=blue_h.STATE_UNKNOWN;
      
      /* save the output */
      new_pcr.output=output;
      
      new_pcr.check_time=check_time;
      
      /* add the passive check result to the end of the list in memory */
      if(passive_check_result_list == null)
         passive_check_result_list = new ArrayList();
      
      passive_check_result_list.add( new_pcr );
      
      logger.trace( "exiting " + cn + ".process_passive_service_check" );
      
      return common_h.OK;
   }
   
   /* process passive host check result */
   public static int cmd_process_host_check_result(int cmd,long check_time,String args){
      String temp_ptr;
      String host_name;
      int return_code;
      String output = "";
      int result;
      
      logger.trace( "entering " + cn + ".cmd_process_host_check_result" );
      String[] split = args.split( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      /* get the host check return code */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null){
         return common_h.ERROR;
      }
      return_code=utils.atoi(temp_ptr);
      
      /* get the plugin output (may be empty) */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr!=null)
         output= temp_ptr;
      
      /* submit the check result */
      result=process_passive_host_check(check_time,host_name,return_code,output);
      
      
      logger.trace( "exiting " + cn + ".cmd_process_host_check_result" );
      
      return result;
   }
   
   /* process passive host check result */
   /* this function is a bit more involved than for passive service checks, as we need to replicate most functions performed by check_route_to_host() */
   public static int process_passive_host_check(long check_time, String host_name, int return_code, String output){
      objects_h.host temp_host;
      String real_host_name="";
      blue_h.timeval tv;
      String temp_plugin_output ="";
      String old_plugin_output ="";
      String temp_ptr;
      
      logger.trace( "entering " + cn + ".process_passive_host_check" );
      
      /* skip this host check result if we aren't accepting passive host check results */
      if(blue.accept_passive_host_checks==common_h.FALSE)
         return common_h.ERROR;
      
      /* make sure we have all required data */
      if(host_name==null || output==null)
         return common_h.ERROR;
      
      /* find the host by its name or address */
      if((temp_host=objects.find_host(host_name))!=null)
         real_host_name=host_name;
      else{
         for ( objects_h.host iter_host : objects.host_list ) {
            if(host_name.equals(iter_host.address)){
               real_host_name=iter_host.name;
               break;
            }
         }
         
         temp_host=objects.find_host(real_host_name);
      }
      
      /* we couldn't find the host */
      if(temp_host==null){
         
         logger.warn( "Warning:  Passive check result was received for host '"+host_name+"', but the host could not be found!");
         return common_h.ERROR;
      }
      
      /* skip this is we aren't accepting passive checks for this host */
      if(temp_host.accept_passive_host_checks==common_h.FALSE)
         return common_h.ERROR;
      
      /* make sure the return code is within bounds */
      if(return_code<0 || return_code>2)
         return common_h.ERROR;
      
      /* save the plugin output */
      temp_plugin_output = output;
      
      /********* LET'S DO IT (SUBSET OF NORMAL HOST CHECK OPERATIONS) ********/
      
      /* set the checked flag */
      temp_host.has_been_checked=common_h.TRUE;
      
      /* save old state */
      temp_host.last_state=temp_host.current_state;
      if(temp_host.state_type==common_h.HARD_STATE)
         temp_host.last_hard_state=temp_host.current_state;
      
      /* NOTE TO SELF: Host state should be adjusted to reflect current host topology... */
      /* record new state */
      temp_host.current_state=return_code;
      
      /* record check type */
      temp_host.check_type=common_h.HOST_CHECK_PASSIVE;
      
      /* record state type */
      temp_host.state_type=common_h.HARD_STATE;
      
      /* set the current attempt - should this be set to max_attempts instead? */
      temp_host.current_attempt=1;
      
      /* save the old plugin output and host state for use with state stalking routines */
      old_plugin_output = ((temp_host.plugin_output==null)?"":temp_host.plugin_output);
      
      /* set the last host check time */
      temp_host.last_check=check_time;
      
      /* clear plugin output and performance data buffers */
      temp_host.plugin_output= "";
      temp_host.perf_data = "";
      
      /* passive checks have ZERO execution time! */
      temp_host.execution_time=0.0;
      
      /* calculate latency */
      tv = new blue_h.timeval();
      temp_host.latency=((tv.tv_sec-check_time)+(tv.tv_usec/1000)/*/1000.0*/);
      if(temp_host.latency<0.0)
         temp_host.latency=0.0;
      
      /* check for empty plugin output */
      if(temp_plugin_output.trim().length() == 0 )
         temp_plugin_output = "(No Information Returned From Host Check)";
      
      /* first part of plugin output (up to pipe) is status info */
      String[] split_temp_plugin_output = temp_plugin_output.split( "[|\n]" );
      temp_ptr = split_temp_plugin_output[0].trim();
      
      /* make sure the plugin output isn't null */
      if( temp_ptr.trim().length() == 0 ){
         temp_host.plugin_output = "(No output returned from host check)" ;
      }
      
      else{
         
         temp_host.plugin_output = temp_ptr ;
      }
      
      /* second part of plugin output (after pipe) is performance data (which may or may not exist) */
      
      temp_ptr=getSplit( split_temp_plugin_output, 1 );
      
      /* grab performance data if we found it available */
      if(temp_ptr!=null){
         temp_host.perf_data = temp_ptr.trim();
      }
      
      /* replace semicolons in plugin output (but not performance data) with colons */
      temp_host.plugin_output = temp_host.plugin_output.replace(';', ':' );
      
      /***** HANDLE THE HOST STATE *****/
      sehandlers.handle_host_state(temp_host);
      
      
      /***** UPDATE HOST STATUS *****/
      statusdata.update_host_status(temp_host,common_h.FALSE);
      
      
      /****** STALKING STUFF *****/
      /* if the host didn't change state and the plugin output differs from the last time it was checked, log the current state/output if state stalking is enabled */
      if(temp_host.last_state==temp_host.current_state && old_plugin_output.equals(temp_host.plugin_output)){
         
         if(temp_host.current_state==blue_h.HOST_UP && temp_host.stalk_on_up==common_h.TRUE)
            logging.log_host_event(temp_host);
         
         else if(temp_host.current_state==blue_h.HOST_DOWN && temp_host.stalk_on_down==common_h.TRUE)
            logging.log_host_event(temp_host);
         
         else if(temp_host.current_state==blue_h.HOST_UNREACHABLE && temp_host.stalk_on_unreachable==common_h.TRUE)
            logging.log_host_event(temp_host);
      }
      
    /* send data to event broker */
    broker.broker_host_check(broker_h.NEBTYPE_HOSTCHECK_PROCESSED,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_host,common_h.HOST_CHECK_PASSIVE,temp_host.current_state,temp_host.state_type,tv,tv,null,0.0,temp_host.execution_time,0,common_h.FALSE,return_code,null,temp_host.plugin_output,temp_host.perf_data,null);
      
      /***** CHECK FOR FLAPPING *****/
      flapping.check_for_host_flapping(temp_host,common_h.TRUE);
      
      
      logger.trace( "exiting " + cn + ".process_passive_host_check" );
      
      return common_h.OK;
   }
   
   /* acknowledges a host or service problem */
   public static int cmd_acknowledge_problem(int cmd,String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      String host_name="";
      String svc_description="";
      String ack_author;
      String ack_data;
      String temp_ptr;
      int type=common_h.ACKNOWLEDGEMENT_NORMAL;
      int notify=common_h.TRUE;
      int persistent=common_h.TRUE;
      
      logger.trace( "entering " + cn + ".cmd_acknowledge_problem" );
      String[] split = args.split( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      /* verify that the host is valid */
      temp_host=objects.find_host(host_name);
      if(temp_host==null)
         return common_h.ERROR;
      
      /* this is a service acknowledgement */
      if(cmd==common_h.CMD_ACKNOWLEDGE_SVC_PROBLEM){
         
         /* get the service name */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(temp_host.name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      
      /* get the type */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      type=utils.atoi(temp_ptr);
      
      /* get the notification option */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      notify=(utils.atoi(temp_ptr)>0)?common_h.TRUE:common_h.FALSE;
      
      /* get the persistent option */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      persistent=(utils.atoi(temp_ptr)>0)?common_h.TRUE:common_h.FALSE;
      
      /* get the acknowledgement author */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      ack_author=temp_ptr;
      
      /* get the acknowledgement data */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      ack_data=temp_ptr;
      
      /* acknowledge the host problem */
      if(cmd==common_h.CMD_ACKNOWLEDGE_HOST_PROBLEM)
         acknowledge_host_problem(temp_host,ack_author,ack_data,type,notify,persistent);
      
      /* acknowledge the service problem */
      else
         acknowledge_service_problem(temp_service,ack_author,ack_data,type,notify,persistent);
      
      logger.trace( "exiting " + cn + ".cmd_acknowledge_problem" );
      return common_h.OK;
   }
   
   /* removes a host or service acknowledgement */
   public static int cmd_remove_acknowledgement(int cmd,String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      String host_name="";
      String svc_description="";
      
      logger.trace( "entering " + cn + ".cmd_remove_acknowledgement" );
      String[] split = args.split( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      /* verify that the host is valid */
      temp_host=objects.find_host(host_name);
      if(temp_host==null)
         return common_h.ERROR;
      
      /* we are removing a service acknowledgement */
      if(cmd==common_h.CMD_REMOVE_SVC_ACKNOWLEDGEMENT){
         
         /* get the service name */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(temp_host.name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      
      /* acknowledge the host problem */
      if(cmd==common_h.CMD_REMOVE_HOST_ACKNOWLEDGEMENT)
         remove_host_acknowledgement(temp_host);
      
      /* acknowledge the service problem */
      else
         remove_service_acknowledgement(temp_service);
      
      logger.trace( "exiting " + cn + ".cmd_remove_acknowledgement" );
      return common_h.OK;
   }
   
   /* schedules downtime for a specific host or objects_h.service */
   public static int cmd_schedule_downtime(int cmd, long entry_time, String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      objects_h.host last_host=null;
      objects_h.hostgroup temp_hostgroup=null;
//    hostgroupmember *temp_hgmember=null;
      objects_h.servicegroup temp_servicegroup=null;
//    servicegroupmember *temp_sgmember=null;
      String host_name="";
      String hostgroup_name="";
      String servicegroup_name="";
      String svc_description="";
      String temp_ptr;
      long start_time;
      long end_time;
      int fixed;
      long triggered_by;
      long duration;
      String author="";
      String comment_data="";
      long downtime_id;
      
      logger.trace( "entering " + cn + ".cmd_schedule_downtime" );
      String[] split = args.split( ";" );
      int x = 0;
      
      if(cmd==common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME){
         
         /* get the hostgroup name */
         hostgroup_name=getSplit( split, x++ );
         if(hostgroup_name==null)
            return common_h.ERROR;
         
         /* verify that the hostgroup is valid */
         temp_hostgroup=objects.find_hostgroup(hostgroup_name);
         if(temp_hostgroup==null)
            return common_h.ERROR;
      }
      
      else if(cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME || cmd==common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME){
         
         /* get the servicegroup name */
         servicegroup_name=getSplit( split, x++ );
         if(servicegroup_name==null)
            return common_h.ERROR;
         
         /* verify that the servicegroup is valid */
         temp_servicegroup=objects.find_servicegroup(servicegroup_name);
         if(temp_servicegroup==null)
            return common_h.ERROR;
      }
      
      else{
         
         /* get the host name */
         host_name=getSplit( split, x++ );
         if(host_name==null)
            return common_h.ERROR;
         
         /* verify that the host is valid */
         temp_host=objects.find_host(host_name);
         if(temp_host==null)
            return common_h.ERROR;
         
         /* this is a service downtime */
         if(cmd==common_h.CMD_SCHEDULE_SVC_DOWNTIME){
            
            /* get the service name */
            svc_description=getSplit( split, x++ );
            if(svc_description==null)
               return common_h.ERROR;
            
            /* verify that the service is valid */
            temp_service=objects.find_service(temp_host.name,svc_description);
            if(temp_service==null)
               return common_h.ERROR;
         }
      }
      
      /* get the start time */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      start_time=utils.strtoul(temp_ptr,null,10);
      
      /* get the end time */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      end_time=utils.strtoul(temp_ptr,null,10);
      
      /* get the fixed flag */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      fixed=utils.atoi(temp_ptr);
      
      /* get the trigger id */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      triggered_by=utils.strtoul(temp_ptr,null,10);
      
      /* get the duration */
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      duration=utils.strtoul(temp_ptr,null,10);
      
      /* get the author */
      author=getSplit( split, x++ );
      if(author==null)
         return common_h.ERROR;
      
      /* get the comment */
      comment_data=getSplit( split, x++ );
      if(comment_data==null)
         return common_h.ERROR;
      
      /* duration should be auto-calculated, not user-specified */
      if(fixed>0)
         duration=(end_time-start_time);
      
      /* schedule downtime */
      switch(cmd){
         
         case common_h.CMD_SCHEDULE_HOST_DOWNTIME:
            downtime.schedule_downtime(common_h.HOST_DOWNTIME,host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            break;
            
         case common_h.CMD_SCHEDULE_SVC_DOWNTIME:
            downtime.schedule_downtime(common_h.SERVICE_DOWNTIME,host_name,svc_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            break;
            
         case common_h.CMD_SCHEDULE_HOST_SVC_DOWNTIME:
            for ( objects_h.service iter_service : objects.service_list ) {
               if( iter_service.host_name.equals(host_name) ) {  
                  downtime.schedule_downtime( common_h.SERVICE_DOWNTIME,host_name,iter_service.description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration );
               }
            }
            break;
            
         case common_h.CMD_SCHEDULE_HOSTGROUP_HOST_DOWNTIME:
            for ( objects_h.hostgroupmember temp_hgmember : temp_hostgroup.members ) 
               downtime.schedule_downtime( common_h.HOST_DOWNTIME,temp_hgmember.host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            break;
            
         case common_h.CMD_SCHEDULE_HOSTGROUP_SVC_DOWNTIME: 
            for ( objects_h.hostgroupmember temp_hgmember : temp_hostgroup.members ) {
               for ( objects_h.service iter_service : objects.service_list ) {
                  if( iter_service.host_name.equals(temp_hgmember.host_name))
                     downtime.schedule_downtime( common_h.SERVICE_DOWNTIME,iter_service.host_name,iter_service.description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
               }
            }
            break;
            
         case common_h.CMD_SCHEDULE_SERVICEGROUP_HOST_DOWNTIME:
            last_host=null;
            for ( objects_h.servicegroupmember temp_sgmember : temp_servicegroup.members ) {
               temp_host=objects.find_host(temp_sgmember.host_name);
               if(temp_host==null)
                  continue;
               if(last_host==temp_host)
                  continue;
               downtime.schedule_downtime(common_h.HOST_DOWNTIME,temp_sgmember.host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
               last_host=temp_host;
            }
            break;
            
         case common_h.CMD_SCHEDULE_SERVICEGROUP_SVC_DOWNTIME:
            for ( objects_h.servicegroupmember temp_sgmember : temp_servicegroup.members ) 
               downtime.schedule_downtime(common_h.SERVICE_DOWNTIME,temp_sgmember.host_name,temp_sgmember.service_description,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            break;
            
         case common_h.CMD_SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME:
            
            /* schedule downtime bfor "parent" host */
            downtime.schedule_downtime(common_h.HOST_DOWNTIME,host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            
            /* schedule (non-triggered) downtime for all child hosts */
            schedule_and_propagate_downtime(temp_host,entry_time,author,comment_data,start_time,end_time,fixed,0,duration);
            break;
            
         case common_h.CMD_SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME:
            
            /* schedule downtime for "parent" host */
            downtime_h.scheduled_downtime dt = downtime.schedule_downtime(common_h.HOST_DOWNTIME,host_name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            downtime_id = dt.downtime_id;
            
            /* schedule triggered downtime for all child hosts */
            schedule_and_propagate_downtime(temp_host,entry_time,author,comment_data,start_time,end_time,fixed,downtime_id,duration);
            break;
            
         default:
            break;
      }
      
      logger.trace( "exiting " + cn + ".cmd_schedule_downtime" );
      return common_h.OK;
   }
   
   /* deletes scheduled host or service downtime */
   public static int cmd_delete_downtime(int cmd, String args){
      long downtime_id;
      String temp_ptr;
      
      logger.trace( "entering " + cn + ".cmd_delete_downtime" );
      
      /* get the id of the downtime to delete */
      if ( args == null || args.trim().length() == 0 )
         return common_h.ERROR;
      
      String[] split = args.split ("\n" );
      temp_ptr = split[0];
      downtime_id=utils.strtoul(temp_ptr,null,10);
      
      if(cmd==common_h.CMD_DEL_HOST_DOWNTIME)
         downtime.unschedule_downtime(common_h.HOST_DOWNTIME,downtime_id);
      else
         downtime.unschedule_downtime(common_h.SERVICE_DOWNTIME,downtime_id);
      
      logger.trace( "exiting " + cn + ".cmd_delete_downtime" );
      
      return common_h.OK;
   }
   
   /* changes a host or service command */
   public static int cmd_change_command(int cmd,String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      objects_h.command temp_command=null;
      String host_name="";
      String svc_description="";
      String command_name="";
      String temp_ptr;
      long attr=common_h.MODATTR_NONE;
      
      logger.trace( "entering " + cn + ".cmd_change_command" );
      String[] split = args.split( ";" );
      int x = 0;
      
      if(cmd!=common_h.CMD_CHANGE_GLOBAL_HOST_EVENT_HANDLER && cmd!=common_h.CMD_CHANGE_GLOBAL_SVC_EVENT_HANDLER){
         
         /* get the host name */
         host_name=getSplit( split, x++ );
         if(host_name==null)
            return common_h.ERROR;
         
         if(cmd==common_h.CMD_CHANGE_SVC_EVENT_HANDLER || cmd==common_h.CMD_CHANGE_SVC_CHECK_COMMAND){
            
            /* get the service name */
            svc_description=getSplit( split, x++ );
            if(svc_description==null)
               return common_h.ERROR;
            
            /* verify that the service is valid */
            temp_service=objects.find_service(host_name,svc_description);
            if(temp_service==null)
               return common_h.ERROR;
         }
         else{
            
            /* verify that the host is valid */
            temp_host=objects.find_host(host_name);
            if(temp_host==null)
               return common_h.ERROR;
         }
         
         command_name=getSplit( split, x++ );
      }
      
      else
         command_name=getSplit( split, x++ );
      
      if(command_name==null)
         return common_h.ERROR;
      temp_ptr=command_name;
      
      /* make sure the command exists */
      String[] split2 = command_name.split ( "\\!" );
      temp_command=objects.find_command(split2[0]);
      if(temp_command==null)
         return common_h.ERROR;
      
      temp_ptr=command_name;
      
      switch(cmd){
         
         case common_h.CMD_CHANGE_GLOBAL_HOST_EVENT_HANDLER:
         case common_h.CMD_CHANGE_GLOBAL_SVC_EVENT_HANDLER:
            
            if(cmd==common_h.CMD_CHANGE_GLOBAL_HOST_EVENT_HANDLER){
               blue.global_host_event_handler=temp_ptr;
               attr=common_h.MODATTR_EVENT_HANDLER_COMMAND;
               blue.modified_host_process_attributes|=attr;
               
             /* send data to event broker */
             broker.broker_adaptive_program_data(broker_h.NEBTYPE_ADAPTIVEPROGRAM_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,cmd,attr,blue.modified_host_process_attributes,common_h.MODATTR_NONE,blue.modified_service_process_attributes,blue.global_host_event_handler,blue.global_service_event_handler,null);
            }
            else{
               blue.global_service_event_handler=temp_ptr;
               attr=common_h.MODATTR_EVENT_HANDLER_COMMAND;
               blue.modified_service_process_attributes|=attr;
               
             /* send data to event broker */
             broker.broker_adaptive_program_data(broker_h.NEBTYPE_ADAPTIVEPROGRAM_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,cmd,common_h.MODATTR_NONE,blue.modified_host_process_attributes,attr,blue.modified_service_process_attributes,blue.global_host_event_handler,blue.global_service_event_handler,null);

            }
            
            /* update program status */
            statusdata.update_program_status(common_h.FALSE);
            break;
            
         case common_h.CMD_CHANGE_HOST_EVENT_HANDLER:
         case common_h.CMD_CHANGE_HOST_CHECK_COMMAND:
            
            if(cmd==common_h.CMD_CHANGE_HOST_EVENT_HANDLER){
               temp_host.event_handler=temp_ptr;
               attr=common_h.MODATTR_EVENT_HANDLER_COMMAND;
            }
            else{
               temp_host.host_check_command=temp_ptr;
               attr=common_h.MODATTR_CHECK_COMMAND;
            }
            
            temp_host.modified_attributes|=attr;
            
          /* send data to event broker */
          broker.broker_adaptive_host_data(broker_h.NEBTYPE_ADAPTIVEHOST_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_host,cmd,attr,temp_host.modified_attributes,null);
            
            /* update host status */
            statusdata.update_host_status(temp_host,common_h.FALSE);
            break;
            
         case common_h.CMD_CHANGE_SVC_EVENT_HANDLER:
         case common_h.CMD_CHANGE_SVC_CHECK_COMMAND:
            
            if(cmd==common_h.CMD_CHANGE_SVC_EVENT_HANDLER){
               temp_service.event_handler=temp_ptr;
               attr=common_h.MODATTR_EVENT_HANDLER_COMMAND;
            }
            else{
               temp_service.service_check_command=temp_ptr;
               attr=common_h.MODATTR_CHECK_COMMAND;
            }
            
            temp_service.modified_attributes|=attr;
            
          /* send data to event broker */          broker.broker_adaptive_service_data(broker_h.NEBTYPE_ADAPTIVESERVICE_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_service,cmd,attr,temp_service.modified_attributes,null);
            
            /* update service status */
            statusdata.update_service_status(temp_service,common_h.FALSE);
            break;
            
         default:
            break;
      }
      
      logger.trace( "exiting " + cn + ".cmd_change_command" );
      
      return common_h.OK;
   }
   
   /* changes a host or service check interval */
   public static int cmd_change_check_interval(int cmd,String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      String host_name="";
      String svc_description="";
      String temp_ptr;
      int interval;
      int old_interval;
      long preferred_time;
      long next_valid_time;
      long attr;
      
      logger.trace( "entering " + cn + ".cmd_change_check_interval" );
      String[] split = args.split( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      if(cmd==common_h.CMD_CHANGE_NORMAL_SVC_CHECK_INTERVAL || cmd==common_h.CMD_CHANGE_RETRY_SVC_CHECK_INTERVAL){
         
         /* get the service name */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(host_name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      else{
         
         /* verify that the host is valid */
         temp_host=objects.find_host(host_name);
         if(temp_host==null)
            return common_h.ERROR;
      }
      
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      
      interval=utils.atoi(temp_ptr);
      if(interval<0)
         return common_h.ERROR;
      
      switch(cmd){
         
         case common_h.CMD_CHANGE_NORMAL_HOST_CHECK_INTERVAL:
            
            /* save the old check interval */
            old_interval=temp_host.check_interval;
            
            /* modify the check interval */
            temp_host.check_interval=interval;
            attr=common_h.MODATTR_NORMAL_CHECK_INTERVAL;
            temp_host.modified_attributes|=attr;
            
            /* schedule a host check if previous interval was 0 (checks were not regularly scheduled) */
            if(old_interval==0 && temp_host.checks_enabled==common_h.TRUE){
               
               /* set the host check flag */
               temp_host.should_be_scheduled=common_h.TRUE;
               
               /* schedule a check for right now (or as soon as possible) */
               preferred_time = utils.currentTimeInSeconds();
               if(utils.check_time_against_period(preferred_time,temp_host.check_period)==common_h.ERROR){
                  next_valid_time = utils.get_next_valid_time(preferred_time,temp_host.check_period);
                  temp_host.next_check=next_valid_time;
               }
               else
                  temp_host.next_check=preferred_time;
               
               /* schedule a check if we should */
               if(temp_host.should_be_scheduled==common_h.TRUE)
                  checks.schedule_host_check(temp_host,temp_host.next_check,common_h.FALSE);
            }
            
            /* send data to event broker */
            broker.broker_adaptive_host_data(broker_h.NEBTYPE_ADAPTIVEHOST_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_host,cmd,attr,temp_host.modified_attributes,null);
            
            /* update the status log with the host info */
            statusdata.update_host_status(temp_host,common_h.FALSE);
            break;
            
         case common_h.CMD_CHANGE_NORMAL_SVC_CHECK_INTERVAL:
            
            /* save the old check interval */
            old_interval=temp_service.check_interval;
            
            /* modify the check interval */
            temp_service.check_interval=interval;
            attr=common_h.MODATTR_NORMAL_CHECK_INTERVAL;
            temp_service.modified_attributes|=attr;
            
            /* schedule a service check if previous interval was 0 (checks were not regularly scheduled) */
            if(old_interval==0 && temp_service.checks_enabled==common_h.TRUE && temp_service.check_interval!=0){
               
               /* set the service check flag */
               temp_service.should_be_scheduled=common_h.TRUE;
               
               /* schedule a check for right now (or as soon as possible) */
               preferred_time = utils.currentTimeInSeconds();
               if( utils.check_time_against_period(preferred_time,temp_service.check_period)==common_h.ERROR){
                  next_valid_time = utils.get_next_valid_time(preferred_time,temp_service.check_period);
                  temp_service.next_check=next_valid_time;
               }
               else
                  temp_service.next_check=preferred_time;
               
               /* schedule a check if we should */
               if(temp_service.should_be_scheduled==common_h.TRUE)
                  checks.schedule_service_check(temp_service,temp_service.next_check,common_h.FALSE);
            }
            
            /* send data to event broker */
            broker.broker_adaptive_service_data(broker_h.NEBTYPE_ADAPTIVESERVICE_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_service,cmd,attr,temp_service.modified_attributes,null);
            
            /* update the status log with the service info */
            statusdata.update_service_status(temp_service,common_h.FALSE);
            break;
            
         case common_h.CMD_CHANGE_RETRY_SVC_CHECK_INTERVAL:
            
            temp_service.retry_interval=interval;
            attr=common_h.MODATTR_RETRY_CHECK_INTERVAL;
            temp_service.modified_attributes|=attr;
            
            /* send data to event broker */
            broker.broker_adaptive_service_data(broker_h.NEBTYPE_ADAPTIVESERVICE_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_service,cmd,attr,temp_service.modified_attributes,null);
            
            /* update the status log with the service info */
            statusdata.update_service_status(temp_service,common_h.FALSE);
            break;
            
         default:
            break;
      }
      
      logger.trace( "exiting " + cn + ".cmd_change_check_interval" );
      
      return common_h.OK;
   }
   
   /* changes a host or service max check attempts */
   public static int cmd_change_max_attempts(int cmd,String args){
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      String host_name="";
      String svc_description="";
      String temp_ptr;
      int max_attempts;
      long attr;
      
      logger.trace( "entering " + cn + ".cmd_change_max_attempts" );
      String[] split = args.split( ";" );
      int x = 0;
      
      /* get the host name */
      host_name=getSplit( split, x++ );
      if(host_name==null)
         return common_h.ERROR;
      
      if(cmd==common_h.CMD_CHANGE_MAX_SVC_CHECK_ATTEMPTS){
         
         /* get the service name */
         svc_description=getSplit( split, x++ );
         if(svc_description==null)
            return common_h.ERROR;
         
         /* verify that the service is valid */
         temp_service=objects.find_service(host_name,svc_description);
         if(temp_service==null)
            return common_h.ERROR;
      }
      else{
         
         /* verify that the host is valid */
         temp_host=objects.find_host(host_name);
         if(temp_host==null)
            return common_h.ERROR;
      }
      
      temp_ptr=getSplit( split, x++ );
      if(temp_ptr==null)
         return common_h.ERROR;
      
      max_attempts=utils.atoi(temp_ptr);
      if(max_attempts<1)
         return common_h.ERROR;
      
      switch(cmd){
         
         case common_h.CMD_CHANGE_MAX_HOST_CHECK_ATTEMPTS:
            
            temp_host.max_attempts=max_attempts;
            attr=common_h.MODATTR_MAX_CHECK_ATTEMPTS;
            temp_host.modified_attributes|=attr;
            
            /* adjust current attempt number if in a hard state */
            if(temp_host.state_type==common_h.HARD_STATE && temp_host.current_state!=blue_h.HOST_UP && temp_host.current_attempt>1)
               temp_host.current_attempt=temp_host.max_attempts;
            
            /* send data to event broker */
            broker.broker_adaptive_host_data(broker_h.NEBTYPE_ADAPTIVEHOST_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_host,cmd,attr,temp_host.modified_attributes,null);
            
            /* update host status info */
            statusdata.update_host_status(temp_host,common_h.FALSE);
            break;
            
         case common_h.CMD_CHANGE_MAX_SVC_CHECK_ATTEMPTS:
            
            temp_service.max_attempts=max_attempts;
            attr=common_h.MODATTR_MAX_CHECK_ATTEMPTS;
            temp_service.modified_attributes|=attr;
            
            /* adjust current attempt number if in a hard state */
            if(temp_service.state_type==common_h.HARD_STATE && temp_service.current_state!=blue_h.STATE_OK && temp_service.current_attempt>1)
               temp_service.current_attempt=temp_service.max_attempts;
            
            /* send data to event broker */
            broker.broker_adaptive_service_data(broker_h.NEBTYPE_ADAPTIVESERVICE_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,temp_service,cmd,attr,temp_service.modified_attributes,null);
            
            /* update service status info */
            statusdata.update_service_status(temp_service,common_h.FALSE);
            break;
            
         default:
            break;
      }
      
      logger.trace( "exiting " + cn + ".cmd_change_max_attempts" );
      
      return common_h.OK;
   }
   
   /******************************************************************/
   /*************** INTERNAL COMMAND IMPLEMENTATIONS  ****************/
   /******************************************************************/
   
   /* temporarily disables a service check */
   public static void disable_service_checks(objects_h.service svc)
   {
      logger.trace( "entering " + cn + ".disable_service_checks" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* checks are already disabled */
      if(svc.checks_enabled==common_h.FALSE)
         
         /* disable the service check... */
         svc.checks_enabled=common_h.FALSE;
      svc.should_be_scheduled=common_h.FALSE;
      
      /* remove scheduled checks of this service from the event queue */
      blue_h.timed_event event = null;
      
      for( blue_h.timed_event temp_event : (ArrayList<blue_h.timed_event>)  events.event_list_low )
      {
         /*	Rob 22/03/07 - Pretty confident this is around the wrong way 
    	 if(temp_event.event_type==blue_h.EVENT_SERVICE_CHECK && svc==(objects_h.service )temp_event.event_data)
          break;
         
         else
         {
            events.remove_event(temp_event,events.event_list_low);
            break;
         }*/
    	  
    	 if(temp_event.event_type == blue_h.EVENT_SERVICE_CHECK && svc == (objects_h.service)temp_event.event_data)
    	 {
    		 event = temp_event;
    	 }
      }
      
      if(event != null)
    	  events.remove_event(event,events.event_list_low);
      
      /* update the status log to reflect the new service state */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_service_checks" );
      
   }
   
   /* enables a service check */
   public static void enable_service_checks(objects_h.service svc){
      long preferred_time;
      long next_valid_time;
      
      logger.trace( "entering " + cn + ".enable_service_checks" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* checks are already enabled */
      if(svc.checks_enabled==common_h.TRUE)
         
         /* enable the service check... */
         svc.checks_enabled=common_h.TRUE;
      svc.should_be_scheduled=common_h.TRUE;
      
      /* services with no check intervals don't get checked */
      if(svc.check_interval==0)
         svc.should_be_scheduled=common_h.FALSE;
      
      /* schedule a check for right now (or as soon as possible) */
      preferred_time = utils.currentTimeInSeconds();
      if( utils.check_time_against_period(preferred_time,svc.check_period)==common_h.ERROR){
         next_valid_time = utils.get_next_valid_time(preferred_time,svc.check_period);
         svc.next_check=next_valid_time;
      }
      else
         svc.next_check=preferred_time;
      
      /* schedule a check if we should */
      if(svc.should_be_scheduled==common_h.TRUE)
         checks.schedule_service_check(svc,svc.next_check,common_h.FALSE);
      
      /* update the status log to reflect the new service state */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_service_checks" );
      
   }
   
   /* enable notifications on a program-wide basis */
   public static void enable_all_notifications(){
      
      logger.trace( "entering " + cn + ".enable_all_notifications" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      
      /* bail out if we're already set... */
      if(blue.enable_notifications==common_h.TRUE)
         
         /* update notification status */
         blue.enable_notifications=common_h.TRUE;
      
      /* update the status log */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_all_notifications" );
      
   }
   
   /* disable notifications on a program-wide basis */
   public static void disable_all_notifications(){
      
      logger.trace( "entering " + cn + ".disable_all_notifications" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      
      /* bail out if we're already set... */
      if(blue.enable_notifications==common_h.FALSE)
         
         /* update notification status */
         blue.enable_notifications=common_h.FALSE;
      
      /* update the status log */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_all_notifications" );
      
   }
   
   /* enables notifications for a objects_h.service */
   public static void enable_service_notifications(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".enable_service_notifications" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      
      /* enable the service notifications... */
      svc.notifications_enabled=common_h.TRUE;
      
      /* update the status log to reflect the new service state */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_service_notifications" );
      
   }
   
   /* disables notifications for a objects_h.service */
   public static void disable_service_notifications(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".disable_service_notifications" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      
      /* disable the service notifications... */
      svc.notifications_enabled=common_h.FALSE;
      
      /* update the status log to reflect the new service state */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_service_notifications" );
      
   }
   
   /* enables notifications for a host */
   public static void enable_host_notifications(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".enable_host_notifications" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      
      /* enable the host notifications... */
      hst.notifications_enabled=common_h.TRUE;
      
      /* update the status log to reflect the new host state */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_host_notifications" );
      
   }
   
   /* disables notifications for a host */
   public static void disable_host_notifications(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".disable_host_notifications" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_NOTIFICATIONS_ENABLED;
      
      /* disable the host notifications... */
      hst.notifications_enabled=common_h.FALSE;
      
      /* update the status log to reflect the new host state */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_host_notifications" );
      
   }
   
   /* enables notifications for all hosts and services "beyond" a given host */
   public static void enable_and_propagate_notifications(objects_h.host hst, int level, int affect_top_host, int affect_hosts, int affect_services){
      
      logger.trace( "entering " + cn + ".enable_and_propagate_notifications" );
      
      /* enable notification for top level host */
      if(affect_top_host==common_h.TRUE && level==0)
         enable_host_notifications(hst);
      
      /* check all child hosts... */
      for ( objects_h.host temp_host : objects.host_list ) {
         
         if( objects.is_host_immediate_child_of_host(hst,temp_host)==common_h.TRUE){
            
            /* recurse... */
            enable_and_propagate_notifications(temp_host,level+1,affect_top_host,affect_hosts,affect_services);
            
            /* enable notifications for this host */
            if(affect_hosts==common_h.TRUE)
               enable_host_notifications(temp_host);
            
            /* enable notifications for all services on this host... */
            if(affect_services==common_h.TRUE){
               for ( objects_h.service temp_service : objects.service_list ) {
                  if(temp_service.host_name.equals(temp_host.name))
                     enable_service_notifications(temp_service);
               }
            }
         }
      }
      
      logger.trace( "exiting " + cn + ".enable_and_propagate_notifications" );
      
   }
   
   /* disables notifications for all hosts and services "beyond" a given host */
   public static void disable_and_propagate_notifications(objects_h.host hst, int level, int affect_top_host, int affect_hosts, int affect_services){
      
      logger.trace( "entering " + cn + ".disable_and_propagate_notifications" );
      
      if(hst==null)
         
         /* disable notifications for top host */
         if(affect_top_host==common_h.TRUE && level==0)
            disable_host_notifications(hst);
      
      /* check all child hosts... */
      for ( objects_h.host temp_host : objects.host_list ) {
         
         if( objects.is_host_immediate_child_of_host(hst,temp_host)==common_h.TRUE){
            
            /* recurse... */
            disable_and_propagate_notifications(temp_host,level+1,affect_top_host,affect_hosts,affect_services);
            
            /* disable notifications for this host */
            if(affect_hosts==common_h.TRUE)
               disable_host_notifications(temp_host);
            
            /* disable notifications for all services on this host... */
            if(affect_services==common_h.TRUE){
               for ( objects_h.service temp_service : objects.service_list ) {
                  if( temp_service.host_name.equals(temp_host.name))
                     disable_service_notifications(temp_service);
               }
            }
         }
      }
      
      logger.trace( "exiting " + cn + ".disable_and_propagate_notifications" );
      
   }
   
   /* schedules downtime for all hosts "beyond" a given host */
   public static void schedule_and_propagate_downtime(objects_h.host temp_host, long entry_time, String author, String comment_data, long start_time, long end_time, int fixed, long triggered_by, long duration){
      
      logger.trace( "entering " + cn + ".schedule_and_propagate_downtime" );
      
      /* check all child hosts... */
      for ( objects_h.host this_host : objects.host_list ) {
         
         if( objects.is_host_immediate_child_of_host(temp_host,this_host)==common_h.TRUE){
            
            /* recurse... */
            schedule_and_propagate_downtime(this_host,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
            
            /* schedule downtime for this host */
            downtime.schedule_downtime(common_h.HOST_DOWNTIME,this_host.name,null,entry_time,author,comment_data,start_time,end_time,fixed,triggered_by,duration);
         }
      }
      
      logger.trace( "exiting " + cn + ".schedule_and_propagate_downtime" );
      
   }
   
   /* acknowledges a host problem */
   public static void acknowledge_host_problem(objects_h.host hst, String ack_author, String ack_data, int type, int notify, int persistent){
      long current_time;
      
      logger.trace( "entering " + cn + ".acknowledge_host_problem" );
      
      /* cannot acknowledge a non-existent problem */
      if(hst.current_state==blue_h.HOST_UP)
         return;
      
      /* send data to event broker */
      broker.broker_acknowledgement_data(broker_h.NEBTYPE_ACKNOWLEDGEMENT_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,common_h.HOST_ACKNOWLEDGEMENT, hst,ack_author,ack_data,type,notify,persistent,null);
      
      /* send out an acknowledgement notification */
      if(notify==common_h.TRUE)
         notifications.host_notification(hst,blue_h.NOTIFICATION_ACKNOWLEDGEMENT,ack_author,ack_data);
      
      /* set the acknowledgement flag */
      hst.problem_has_been_acknowledged=common_h.TRUE;
      
      /* set the acknowledgement type */
      hst.acknowledgement_type=(type==common_h.ACKNOWLEDGEMENT_STICKY)?common_h.ACKNOWLEDGEMENT_STICKY:common_h.ACKNOWLEDGEMENT_NORMAL;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      /* add a comment for the acknowledgement */
      current_time = utils.currentTimeInSeconds(); 
      comments.add_new_host_comment( comments_h.ACKNOWLEDGEMENT_COMMENT,hst.name,current_time,ack_author,ack_data,persistent,comments_h.COMMENTSOURCE_INTERNAL,common_h.FALSE,0);
      
      logger.trace( "exiting " + cn + ".acknowledge_host_problem" );
      
   }
   
   /* acknowledges a service problem */
   public static void acknowledge_service_problem(objects_h.service svc, String ack_author, String ack_data, int type, int notify, int persistent){
      long current_time;
      
      logger.trace( "entering " + cn + ".acknowledge_service_problem" );
      
      /* cannot acknowledge a non-existent problem */
      if(svc.current_state==blue_h.STATE_OK)
         return;
      
      /* send data to event broker */
      broker.broker_acknowledgement_data(broker_h.NEBTYPE_ACKNOWLEDGEMENT_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,common_h.SERVICE_ACKNOWLEDGEMENT, svc,ack_author,ack_data,type,notify,persistent,null);
      
      /* send out an acknowledgement notification */
      if(notify==common_h.TRUE)
         notifications.service_notification(svc,blue_h.NOTIFICATION_ACKNOWLEDGEMENT,ack_author,ack_data);
      
      /* set the acknowledgement flag */
      svc.problem_has_been_acknowledged=common_h.TRUE;
      
      /* set the acknowledgement type */
      svc.acknowledgement_type=(type==common_h.ACKNOWLEDGEMENT_STICKY)?common_h.ACKNOWLEDGEMENT_STICKY:common_h.ACKNOWLEDGEMENT_NORMAL;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      /* add a comment for the acknowledgement */
      current_time = utils.currentTimeInSeconds(); 
      comments.add_new_service_comment( comments_h.ACKNOWLEDGEMENT_COMMENT,svc.host_name,svc.description,current_time,ack_author,ack_data,persistent,comments_h.COMMENTSOURCE_INTERNAL,common_h.FALSE,0 );
      
      logger.trace( "exiting " + cn + ".acknowledge_service_problem" );
   }
   
   /* removes a host acknowledgement */
   public static void remove_host_acknowledgement(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".remove_host_acknowledgement" );
      
      /* set the acknowledgement flag */
      hst.problem_has_been_acknowledged=common_h.FALSE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".remove_host_acknowledgement" );
      
   }
   
   /* removes a service acknowledgement */
   public static void remove_service_acknowledgement(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".remove_service_acknowledgement" );
      
      /* set the acknowledgement flag */
      svc.problem_has_been_acknowledged=common_h.FALSE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".remove_service_acknowledgement" );
      
   }
   
   /* starts executing service checks */
   public static void start_executing_service_checks(){
      
      logger.trace( "entering " + cn + ".start_executing_service_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* bail out if we're already executing services */
      if(blue.execute_service_checks==common_h.TRUE)
         
         /* set the service check execution flag */
         blue.execute_service_checks=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_executing_service_checks" );
      
   }
   
   /* stops executing service checks */
   public static void stop_executing_service_checks () {
      
      logger.trace( "entering " + cn + ".stop_executing_service_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* bail out if we're already not executing services */
      if(blue.execute_service_checks==common_h.FALSE)
         
         /* set the service check execution flag */
         blue.execute_service_checks=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_executing_service_checks" );
      
   }
   
   /* starts accepting passive service checks */
   public static void start_accepting_passive_service_checks () {
      
      logger.trace( "entering " + cn + ".start_accepting_passive_service_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* bail out if we're already accepting passive services */
      if( blue.accept_passive_service_checks==common_h.TRUE)
         
         /* set the service check flag */
         blue.accept_passive_service_checks=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_accepting_passive_service_checks" );
      
   }
   
   /* stops accepting passive service checks */
   public static void stop_accepting_passive_service_checks () {
      
      logger.trace( "entering " + cn + ".stop_accepting_passive_service_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* bail out if we're already not accepting passive services */
      if(blue.accept_passive_service_checks==common_h.FALSE)
         
         /* set the service check flag */
         blue.accept_passive_service_checks=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_accepting_passive_service_checks" );
      
   }
   
   /* enables passive service checks for a particular objects_h.service */
   public static void enable_passive_service_checks(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".enable_passive_service_checks" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* set the passive check flag */
      svc.accept_passive_service_checks=common_h.TRUE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_passive_service_checks" );
      
   }
   
   /* disables passive service checks for a particular objects_h.service */
   public static void disable_passive_service_checks(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".disable_passive_service_checks" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* set the passive check flag */
      svc.accept_passive_service_checks=common_h.FALSE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_passive_service_checks" );
      
   }
   
   /* starts executing host checks */
   public static void start_executing_host_checks () {
      
      logger.trace( "entering " + cn + ".start_executing_host_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* bail out if we're already executing hosts */
      if(blue.execute_host_checks==common_h.TRUE)
         
         /* set the host check execution flag */
         blue.execute_host_checks=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_executing_host_checks" );
      
   }
   
   /* stops executing host checks */
   public static void stop_executing_host_checks () {
      
      logger.trace( "entering " + cn + ".stop_executing_host_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* bail out if we're already not executing hosts */
      if(blue.execute_host_checks==common_h.FALSE)
         
         /* set the host check execution flag */
         blue.execute_host_checks=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_executing_host_checks" );
      
   }
   
   /* starts accepting passive host checks */
   public static void start_accepting_passive_host_checks () {
      
      logger.trace( "entering " + cn + ".start_accepting_passive_host_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* bail out if we're already accepting passive hosts */
      if(blue.accept_passive_host_checks==common_h.TRUE)
         
         /* set the host check flag */
         blue.accept_passive_host_checks=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_accepting_passive_host_checks" );
      
   }
   
   /* stops accepting passive host checks */
   public static void stop_accepting_passive_host_checks () {
      
      logger.trace( "entering " + cn + ".stop_accepting_passive_host_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* bail out if we're already not accepting passive hosts */
      if(blue.accept_passive_host_checks==common_h.FALSE)
         
         /* set the host check flag */
         blue.accept_passive_host_checks=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_accepting_passive_host_checks" );
      
   }
   
   /* enables passive host checks for a particular host */
   public static void enable_passive_host_checks(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".enable_passive_host_checks" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* set the passive check flag */
      hst.accept_passive_host_checks=common_h.TRUE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_passive_host_checks" );
      
   }
   
   
   
   /* disables passive host checks for a particular host */
   public static void disable_passive_host_checks(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".disable_passive_host_checks" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_PASSIVE_CHECKS_ENABLED;
      
      /* set the passive check flag */
      hst.accept_passive_host_checks=common_h.FALSE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_passive_host_checks" );
      
   }
   
   /* enables event handlers on a program-wide basis */
   public static void start_using_event_handlers () {
      
      logger.trace( "entering " + cn + ".start_using_event_handlers" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      
      /* set the event handler flag */
      blue.enable_event_handlers=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_using_event_handlers" );
      
   }
   
   
   /* disables event handlers on a program-wide basis */
   public static void stop_using_event_handlers () {
      
      logger.trace( "entering " + cn + ".stop_using_event_handlers" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      
      /* set the event handler flag */
      blue.enable_event_handlers=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_using_event_handlers" );
      
   }
   
   /* enables the event handler for a particular objects_h.service */
   public static void enable_service_event_handler(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".enable_service_event_handler" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      
      /* set the event handler flag */
      svc.event_handler_enabled=common_h.TRUE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_service_event_handler" );
      
   }
   
   
   
   /* disables the event handler for a particular objects_h.service */
   public static void disable_service_event_handler(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".disable_service_event_handler" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      
      /* set the event handler flag */
      svc.event_handler_enabled=common_h.FALSE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_service_event_handler" );
      
   }
   
   /* enables the event handler for a particular host */
   public static void enable_host_event_handler(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".enable_host_event_handler" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      
      /* set the event handler flag */
      hst.event_handler_enabled=common_h.TRUE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_host_event_handler" );
      
   }
   
   
   /* disables the event handler for a particular host */
   public static void disable_host_event_handler(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".disable_host_event_handler" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_EVENT_HANDLER_ENABLED;
      
      /* set the event handler flag */
      hst.event_handler_enabled=common_h.FALSE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_host_event_handler" );
      
   }
   
   /* disables checks of a particular host */
   public static void disable_host_checks(objects_h.host hst)
   {
      
      logger.trace( "entering " + cn + ".disable_host_checks" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* checks are already disabled */
      if(hst.checks_enabled==common_h.FALSE)
         
         /* set the host check flag */
         hst.checks_enabled=common_h.FALSE;
      hst.should_be_scheduled=common_h.FALSE;
      
      blue_h.timed_event event = null;
      
      /* remove scheduled checks of this host from the event queue */
      /* Updated Rob 22/03/07 */
      for ( blue_h.timed_event temp_event : (ArrayList<blue_h.timed_event>)  events.event_list_low ) {
         if(temp_event.event_type==blue_h.EVENT_HOST_CHECK && hst==(objects_h.host)temp_event.event_data)
        	 event = temp_event;
      }
      
   		if(event != null)
   			events.remove_event(event,events.event_list_low);
   
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_host_checks" );
      
   }
   
   
   /* Method to cleanly stop all service checks for a specific host.
    * 
    *  ADDED - Rob 21/03/07 - This is used by our new command set 
    *  to cleanly stop the checking of any services on a particular
    *  host. While there is already a DISABLE_HOST_SVC_CHECKS external
    *  command, by exposing this method it prevents us have to write
    *  to the external command file */
   
   public static void disable_host_service_checks(objects_h.host host)
   {
	   for(objects_h.service s: objects.service_list)
	   {
		   if(s.host_name.equals(host.name))
		   {
			   disable_service_checks(s);
		   }
	   }
   }
   
   /* enables checks of a particular host */
   public static void enable_host_checks(objects_h.host hst){
      long preferred_time;
      long next_valid_time;
      
      logger.trace( "entering " + cn + ".enable_host_checks" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_ACTIVE_CHECKS_ENABLED;
      
      /* checks are already enabled */
      if(hst.checks_enabled==common_h.TRUE)
         
         /* set the host check flag */
         hst.checks_enabled=common_h.TRUE;
      hst.should_be_scheduled=common_h.TRUE;
      
      /* hosts with no check intervals don't get checked */
      if(hst.check_interval==0)
         hst.should_be_scheduled=common_h.FALSE;
      
      /* schedule a check for right now (or as soon as possible) */
      preferred_time = utils.currentTimeInSeconds();
      if( utils.check_time_against_period(preferred_time,hst.check_period)==common_h.ERROR){
         next_valid_time = utils.get_next_valid_time(preferred_time,hst.check_period);
         hst.next_check=next_valid_time;
      }
      else
         hst.next_check=preferred_time;
      
      /* schedule a check if we should */
      if(hst.should_be_scheduled==common_h.TRUE)
         checks.schedule_host_check(hst,hst.next_check,common_h.FALSE);
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_host_checks" );
      
   }
   
   /* start obsessing over service check results */
   public static void start_obsessing_over_service_checks () {
      
      logger.trace( "entering " + cn + ".start_obsessing_over_service_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the service obsession flag */
      blue.obsess_over_services=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_obsessing_over_service_checks" );
      
   }
   
   
   
   /* stop obsessing over service check results */
   public static void stop_obsessing_over_service_checks () {
      
      logger.trace( "entering " + cn + ".stop_obsessing_over_service_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the service obsession flag */
      blue.obsess_over_services=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_obsessing_over_service_checks" );
      
   }
   
   /* start obsessing over host check results */
   public static void start_obsessing_over_host_checks () {
      
      logger.trace( "entering " + cn + ".start_obsessing_over_host_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the host obsession flag */
      blue.obsess_over_hosts=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_obsessing_over_host_checks" );
      
   }
   
   
   
   /* stop obsessing over host check results */
   public static void stop_obsessing_over_host_checks () {
      
      logger.trace( "entering " + cn + ".stop_obsessing_over_host_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the host obsession flag */
      blue.obsess_over_hosts=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_obsessing_over_host_checks" );
      
   }
   
   /* enables service freshness checking */
   public static void enable_service_freshness_checks () {
      
      logger.trace( "entering " + cn + ".enable_service_freshness_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_FRESHNESS_CHECKS_ENABLED;
      
      /* set the freshness check flag */
      blue.check_service_freshness=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_service_freshness_checks" );
      
   }
   
   
   /* disables service freshness checking */
   public static void disable_service_freshness_checks () {
      
      logger.trace( "entering " + cn + ".disable_service_freshness_checks" );
      
      /* set the attribute modified flag */
      blue.modified_service_process_attributes|=common_h.MODATTR_FRESHNESS_CHECKS_ENABLED;
      
      /* set the freshness check flag */
      blue.check_service_freshness=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_service_freshness_checks" );
      
   }
   
   /* enables host freshness checking */
   public static void enable_host_freshness_checks () {
      
      logger.trace( "entering " + cn + ".enable_host_freshness_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_FRESHNESS_CHECKS_ENABLED;
      
      /* set the freshness check flag */
      blue.check_host_freshness=common_h.TRUE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_host_freshness_checks" );
      
   }
   
   
   /* disables host freshness checking */
   public static void disable_host_freshness_checks () {
      
      logger.trace( "entering " + cn + ".disable_host_freshness_checks" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_FRESHNESS_CHECKS_ENABLED;
      
      /* set the freshness check flag */
      blue.check_host_freshness=common_h.FALSE;
      
      /* update the status log with the program info */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_host_freshness_checks" );
      
   }
   
   /* enable failure prediction on a program-wide basis */
   public static void enable_all_failure_prediction () {
      
      logger.trace( "entering " + cn + ".enable_all_failure_prediction" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_FAILURE_PREDICTION_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_FAILURE_PREDICTION_ENABLED;
      
      /* bail out if we're already set... */
      if(blue.enable_failure_prediction==common_h.TRUE)
         
         blue.enable_failure_prediction=common_h.TRUE;
      
      /* update the status log */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_all_failure_prediction" );
      
   }
   
   /* disable failure prediction on a program-wide basis */
   public static void disable_all_failure_prediction () {
      
      logger.trace( "entering " + cn + ".disable_all_failure_prediction" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_FAILURE_PREDICTION_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_FAILURE_PREDICTION_ENABLED;
      
      /* bail out if we're already set... */
      if(blue.enable_failure_prediction==common_h.FALSE)
         
         blue.enable_failure_prediction=common_h.FALSE;
      
      /* update the status log */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_all_failure_prediction" );
      
   }
   
   /* enable performance data on a program-wide basis */
   public static void enable_performance_data () {
      
      logger.trace( "entering " + cn + ".enable_performance_data" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_PERFORMANCE_DATA_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_PERFORMANCE_DATA_ENABLED;
      
      /* bail out if we're already set... */
      if(blue.process_performance_data==common_h.TRUE)
         
         blue.process_performance_data=common_h.TRUE;
      
      /* update the status log */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".enable_performance_data" );
      
   }
   
   /* disable performance data on a program-wide basis */
   public static void disable_performance_data () {
      
      logger.trace( "entering " + cn + ".disable_performance_data" );
      
      /* set the attribute modified flag */
      blue.modified_host_process_attributes|=common_h.MODATTR_PERFORMANCE_DATA_ENABLED;
      blue.modified_service_process_attributes|=common_h.MODATTR_PERFORMANCE_DATA_ENABLED;
      
      /* bail out if we're already set... */
      if(blue.process_performance_data==common_h.FALSE)
         
         blue.process_performance_data=common_h.FALSE;
      
      /* update the status log */
      statusdata.update_program_status(common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".disable_performance_data" );
      
   }
   
   /* start obsessing over a particular objects_h.service */
   public static void start_obsessing_over_service(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".start_obsessing_over_service" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the obsess over service flag */
      svc.obsess_over_service=common_h.TRUE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_obsessing_over_service" );
      
   }
   
   /* stop obsessing over a particular objects_h.service */
   public static void stop_obsessing_over_service(objects_h.service svc){
      
      logger.trace( "entering " + cn + ".stop_obsessing_over_service" );
      
      /* set the attribute modified flag */
      svc.modified_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the obsess over service flag */
      svc.obsess_over_service=common_h.FALSE;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_obsessing_over_service" );
      
   }
   
   /* start obsessing over a particular host */
   public static void start_obsessing_over_host(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".start_obsessing_over_host" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the obsess over host flag */
      hst.obsess_over_host=common_h.TRUE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".start_obsessing_over_host" );
   }
   
   /* stop obsessing over a particular host */
   public static void stop_obsessing_over_host(objects_h.host hst){
      
      logger.trace( "entering " + cn + ".stop_obsessing_over_host" );
      
      /* set the attribute modified flag */
      hst.modified_attributes|=common_h.MODATTR_OBSESSIVE_HANDLER_ENABLED;
      
      /* set the obsess over host flag */
      hst.obsess_over_host=common_h.FALSE;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".stop_obsessing_over_host" );
   }
   
   /* sets the current notification number for a specific host */
   public static void set_host_notification_number(objects_h.host hst, int num){
      
      logger.trace( "entering " + cn + ".set_host_notification_number" );
      
      /* set the notification number */
      hst.current_notification_number=num;
      
      /* update the status log with the host info */
      statusdata.update_host_status(hst,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".set_host_notification_number" );
   }
   
   /* sets the current notification number for a specific objects_h.service */
   public static void set_service_notification_number(objects_h.service svc, int num){
      
      logger.trace( "entering " + cn + ".set_service_notification_number" );
      
      /* set the notification number */
      svc.current_notification_number=num;
      
      /* update the status log with the service info */
      statusdata.update_service_status(svc,common_h.FALSE);
      
      logger.trace( "exiting " + cn + ".set_service_notification_number" );
   }
   
   /* process all passive service checks found in a given file */
   public static void process_passive_service_checks () {
      
      logger.trace( "entering " + cn + ".process_passive_service_checks" );
      
      ArrayList<blue_h.passive_check_result> list = new ArrayList<blue_h.passive_check_result>( commands.passive_check_result_list );
      commands.passive_check_result_list.removeAll( list );
      
      Thread t = new service_passive_worker_thread ( list );
      t.start();
      
      logger.trace( "exiting " + cn + ".process_passive_service_checks" );
      
   }
   
   private static String getSplit( String[] split, int index ) {
      if ( split.length <= index )
         return null;
      else
         return split[index].trim();
   }
}