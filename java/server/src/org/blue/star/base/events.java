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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.comments;
import org.blue.star.common.downtime;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class events {
   
   public static blue_h.sched_info scheduling_info = new blue_h.sched_info(); 
   
   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.base.events");
   private static String cn = "org.blue.base.events";
   
   public static ArrayList event_list_low = new ArrayList(); // timed_event 
   public static ArrayList event_list_high = new ArrayList(); // timed_event 
   
   /******************************************************************/
   /************ EVENT SCHEDULING/HANDLING FUNCTIONS *****************/
   /******************************************************************/
   
   /* initialize the event timing loop before we start monitoring */
   public static void init_timing_loop( )
   {
      long current_time = utils.currentTimeInSeconds();
      long interval_to_use;
      int total_interleave_blocks=0;
      int current_interleave_block=1;
      int interleave_block_index=0;
      int mult_factor;
      int is_valid_time;
      long next_valid_time;
      boolean schedule_check;
      
      logger.trace( "entering " + cn + ".init_timing_loop");
      
      /******** GET BASIC HOST/SERVICE INFO  ********/
      
      scheduling_info.total_services=0;
      scheduling_info.total_scheduled_services=0;
      scheduling_info.total_hosts=0;
      scheduling_info.total_scheduled_hosts=0;
      scheduling_info.average_services_per_host=0.0;
      scheduling_info.average_scheduled_services_per_host=0.0;
      scheduling_info.service_check_interval_total=0;
      scheduling_info.average_service_inter_check_delay=0.0;
      scheduling_info.host_check_interval_total=0;
      scheduling_info.average_host_inter_check_delay=0.0;
      
      /* get info on service checks to be scheduled */
      for ( objects_h.service temp_service : objects.service_list )
     {
         schedule_check= true;
         
         /* service has no check interval */
         if(temp_service.check_interval==0)
            schedule_check=false;
         
         /* active checks are disabled */
         if(temp_service.checks_enabled==common_h.FALSE)
            schedule_check=false;
         
         /* are there any valid times this service can be checked? */
         is_valid_time = utils.check_time_against_period(current_time,temp_service.check_period);
         
         if(is_valid_time== common_h.ERROR)
         {
            next_valid_time = utils.get_next_valid_time(current_time,temp_service.check_period);
            if(current_time==next_valid_time)
               schedule_check=false;
         }
         
         if(schedule_check == true)
         {
            scheduling_info.total_scheduled_services++;
            
            /* used later in inter-check delay calculations */
            scheduling_info.service_check_interval_total+=temp_service.check_interval;
         }
         else
         {
            temp_service.should_be_scheduled=common_h.FALSE;
            logger.debug("Service '"+temp_service.description+"' on host '"+temp_service.host_name+"' should not be scheduled.");
         }
         
         scheduling_info.total_services++;
      }
      
      /* get info on host checks to be scheduled */
      for ( objects_h.host temp_host : objects.host_list ) {
         schedule_check=true;
         
         /* host has no check interval */
         if(temp_host.check_interval==0)
            schedule_check=false;
         
         /* active checks are disabled */
         if(temp_host.checks_enabled==common_h.FALSE)
            schedule_check=false;
         
         /* are there any valid times this host can be checked? */
         is_valid_time = utils.check_time_against_period(current_time,temp_host.check_period);
         if(is_valid_time == common_h.ERROR){
            next_valid_time = utils.get_next_valid_time(current_time,temp_host.check_period);
            if(current_time==next_valid_time)
               schedule_check= false;
         }
         
         if(schedule_check== true ){
            
            scheduling_info.total_scheduled_hosts++;
            
            /* this is used later in inter-check delay calculations */
            scheduling_info.host_check_interval_total+=temp_host.check_interval;
         }
         else{
            temp_host.should_be_scheduled= common_h.FALSE;
            logger.debug("Host '"+temp_host.name+"' should not be scheduled");
         }
         
         scheduling_info.total_hosts++;
      }
      
      scheduling_info.average_services_per_host=(scheduling_info.total_services/scheduling_info.total_hosts);
      scheduling_info.average_scheduled_services_per_host=(scheduling_info.total_scheduled_services/scheduling_info.total_hosts);
      
      
      /******** DETERMINE SERVICE SCHEDULING PARAMS  ********/
      
      /* default max service check spread (in minutes) */
      scheduling_info.max_service_check_spread = blue.max_service_check_spread;
      
      /* how should we determine the service inter-check delay to use? */
      switch( blue.service_inter_check_delay_method)
      {
         
         case blue_h.ICD_NONE:
            
            /* don't spread checks out - useful for testing parallelization code */
            scheduling_info.service_inter_check_delay=0.0;
            break;
            
         case blue_h.ICD_DUMB:
            
            /* be dumb and just schedule checks 1 second apart */
            scheduling_info.service_inter_check_delay=1.0;
            break;
            
         case blue_h.ICD_USER:
            
            /* the user specified a delay, so don't try to calculate one */
            break;
            
         case blue_h.ICD_SMART:
         default:
            
            /* be smart and calculate the best delay to use to minimize local load... */
            if(scheduling_info.total_scheduled_services>0 && scheduling_info.service_check_interval_total>0)
            {
               
               /* adjust the check interval total to correspond to the interval length */
               scheduling_info.service_check_interval_total=(scheduling_info.service_check_interval_total* blue.interval_length);
               
               /* calculate the average check interval for services */
               scheduling_info.average_service_check_interval=(scheduling_info.service_check_interval_total/scheduling_info.total_scheduled_services);
               
               /* calculate the average inter check delay (in seconds) needed to evenly space the service checks out */
               scheduling_info.average_service_inter_check_delay=(scheduling_info.average_service_check_interval/scheduling_info.total_scheduled_services);
               
               /* set the global inter check delay value */
               scheduling_info.service_inter_check_delay=scheduling_info.average_service_inter_check_delay;
               
               /* calculate max inter check delay and see if we should use that instead */
               double max_inter_check_delay = ((scheduling_info.max_service_check_spread*60.0)/scheduling_info.total_scheduled_services);
               if(scheduling_info.service_inter_check_delay>max_inter_check_delay)
                  scheduling_info.service_inter_check_delay=max_inter_check_delay;
            }
            else
               scheduling_info.service_inter_check_delay=0.0;
         
         logger.debug("\tTotal scheduled service checks:  " + scheduling_info.total_scheduled_services);
         logger.debug("\tService check interval total:    " + scheduling_info.service_check_interval_total);
         logger.debug("\tAverage service check interval:  "+scheduling_info.average_service_check_interval+" sec");
         logger.debug("\tService inter-check delay:       " + scheduling_info.service_inter_check_delay + " sec" );
      }
      
      /* how should we determine the service interleave factor? */
      switch(blue.service_interleave_factor_method){
         
         case blue_h.ILF_USER:
            
            /* the user supplied a value, so don't do any calculation */
            break;
            
         case blue_h.ILF_SMART:
         default:
            
            /* protect against a divide by zero problem - shouldn't happen, but just in case... */
            if(scheduling_info.total_hosts==0)
               scheduling_info.total_hosts=1;
         
         scheduling_info.service_interleave_factor=(int)(Math.ceil(scheduling_info.average_scheduled_services_per_host));
         
         logger.debug("\tTotal scheduled service checks: " + scheduling_info.total_scheduled_services);
         logger.debug("\tTotal hosts:                    " + scheduling_info.total_hosts);
         logger.debug("\tService Interleave factor:      " + scheduling_info.service_interleave_factor);
      }
      
      /* calculate number of service interleave blocks */
      if(scheduling_info.service_interleave_factor==0)
         total_interleave_blocks = scheduling_info.total_scheduled_services;
      else
         total_interleave_blocks = (int) Math.ceil(scheduling_info.total_scheduled_services/scheduling_info.service_interleave_factor);
      
      scheduling_info.first_service_check = 0L;
      scheduling_info.last_service_check = 0L;
      
      logger.debug("Total scheduled services: " + scheduling_info.total_scheduled_services);
      logger.debug("Service Interleave factor: " + scheduling_info.service_interleave_factor);
      logger.debug("Total service interleave blocks: " + total_interleave_blocks);
      logger.debug("Service inter-check delay: " + scheduling_info.service_inter_check_delay);
      
      /******** SCHEDULE SERVICE CHECKS  ********/
      
      /* determine check times for service checks (with interleaving to minimize remote load) */
      current_interleave_block=0;
      for ( ListIterator iter = objects.service_list.listIterator(); iter.hasNext() && (scheduling_info.service_interleave_factor>0); ) {
         objects_h.service temp_service = null;
         
         logger.debug("\tCurrent Interleave Block: " + current_interleave_block);
         
         for(interleave_block_index=0;interleave_block_index<scheduling_info.service_interleave_factor && iter.hasNext(); ){
            temp_service=(objects_h.service) iter.next();
            
            /* skip this service if it shouldn't be scheduled */
            if(temp_service.should_be_scheduled==common_h.FALSE)
               continue;
            
            /* skip services that are already scheduled (from retention data) */
            if(temp_service.next_check != 0)
               continue;
            
            /* interleave block index should only be increased when we find a schedulable service */
            /* moved from for() loop 11/05/05 EG */
            interleave_block_index++;
            
            mult_factor=current_interleave_block+(interleave_block_index*total_interleave_blocks);
            
            logger.debug("\t\tService '"+temp_service.description+"' on host '"+temp_service.host_name+"'");
            logger.debug("\t\t\tCIB: "+current_interleave_block+", IBI: "+interleave_block_index+", TIB: "+total_interleave_blocks+", SIF: " + scheduling_info.service_interleave_factor);
            logger.debug("\t\t\tMult factor: " + mult_factor);
            
            /* set the preferred next check time for the service */
            temp_service.next_check = (long) (current_time+(mult_factor*scheduling_info.service_inter_check_delay));
            
            logger.debug("\t\t\tPreferred Check Time: "+temp_service.next_check+" -. " + new Date( temp_service.next_check*1000 ).toString() );
            
            /* make sure the service can actually be scheduled when we want */
            is_valid_time = utils.check_time_against_period(temp_service.next_check,temp_service.check_period);
            
            if(is_valid_time==common_h.ERROR){
               next_valid_time = utils.get_next_valid_time(temp_service.next_check,temp_service.check_period);
               temp_service.next_check=next_valid_time;
            }
            
            logger.debug("\t\t\tActual Check Time: "+temp_service.next_check+" -. " + new Date(temp_service.next_check *1000 ) .toString());
            
            if( scheduling_info.first_service_check==0 || (temp_service.next_check<scheduling_info.first_service_check) )
               scheduling_info.first_service_check = temp_service.next_check;
            if( temp_service.next_check > scheduling_info.last_service_check )
               scheduling_info.last_service_check = temp_service.next_check;
         }
         
         current_interleave_block++;
      }
      
      /* add scheduled service checks to event queue */
      for ( objects_h.service temp_service : objects.service_list ) {
         /* skip services that shouldn't be scheduled */
         if(temp_service.should_be_scheduled== common_h.FALSE)
            continue;
         
         schedule_new_event( blue_h.EVENT_SERVICE_CHECK, common_h.FALSE, temp_service.next_check, common_h.FALSE, 0L,  null ,  common_h.TRUE , temp_service, null);
      }
      
      /******** DETERMINE HOST SCHEDULING PARAMS  ********/
      
      scheduling_info.first_host_check=0L;
      scheduling_info.last_host_check=0L;
      
      /* default max host check spread (in minutes) */
      scheduling_info.max_host_check_spread = blue.max_host_check_spread;
      
      /* how should we determine the host inter-check delay to use? */
      switch(blue.host_inter_check_delay_method){
         
         case blue_h.ICD_NONE:
            
            /* don't spread checks out */
            scheduling_info.host_inter_check_delay=0.0;
            break;
            
         case blue_h.ICD_DUMB:
            
            /* be dumb and just schedule checks 1 second apart */
            scheduling_info.host_inter_check_delay=1.0;
            break;
            
         case blue_h.ICD_USER:
            
            /* the user specified a delay, so don't try to calculate one */
            break;
            
         case blue_h.ICD_SMART:
         default:
            
            /* be smart and calculate the best delay to use to minimize local load... */
            if(scheduling_info.total_scheduled_hosts>0 && scheduling_info.host_check_interval_total>0){
               
               /* adjust the check interval total to correspond to the interval length */
               scheduling_info.host_check_interval_total=(scheduling_info.host_check_interval_total*blue.interval_length);
               
               /* calculate the average check interval for hosts */
               scheduling_info.average_host_check_interval=(scheduling_info.host_check_interval_total/scheduling_info.total_scheduled_hosts);
               
               /* calculate the average inter check delay (in seconds) needed to evenly space the host checks out */
               scheduling_info.average_host_inter_check_delay=(scheduling_info.average_host_check_interval/scheduling_info.total_scheduled_hosts);
               
               /* set the global inter check delay value */
               scheduling_info.host_inter_check_delay=scheduling_info.average_host_inter_check_delay;
               
               /* calculate max inter check delay and see if we should use that instead */
               double max_inter_check_delay=((scheduling_info.max_host_check_spread*60.0)/scheduling_info.total_scheduled_hosts);
               if(scheduling_info.host_inter_check_delay>max_inter_check_delay)
                  scheduling_info.host_inter_check_delay=max_inter_check_delay;
            }
            else
               scheduling_info.host_inter_check_delay=0.0;
         
         logger.debug("\tTotal scheduled host checks:  " + scheduling_info.total_scheduled_hosts);
         logger.debug("\tHost check interval total:    " + scheduling_info.host_check_interval_total);
         logger.debug("\tAverage host check interval:  "+scheduling_info.average_host_check_interval+" sec");
         logger.debug("\tHost inter-check delay:       "+scheduling_info.host_inter_check_delay+"  sec");
      }
      
      /******** SCHEDULE HOST CHECKS  ********/
      
      /* determine check times for host checks */
      mult_factor=0;
      for ( ListIterator iter = objects.host_list.listIterator(); iter.hasNext(); ) {
         objects_h.host temp_host = (objects_h.host) iter.next();
         
         /* skip hosts that shouldn't be scheduled */
         if(temp_host.should_be_scheduled==common_h.FALSE)
            continue;
         
         /* skip hosts that are already scheduled (from retention data) */
         if(temp_host.next_check!=0)
            continue;
         
         logger.debug("\t\tHost '"+temp_host.name+"'");
         
         /* calculate preferred host check time */
         temp_host.next_check=(long)(current_time+(mult_factor*scheduling_info.host_inter_check_delay));
         
         logger.debug("\t\t\tPreferred Check Time: "+temp_host.next_check+" -. " + new Date(temp_host.next_check *1000 ).toString() );
         
         /* make sure the host can actually be scheduled at this time */
         is_valid_time = utils.check_time_against_period(temp_host.next_check,temp_host.check_period);
         if(is_valid_time == common_h.ERROR){
            next_valid_time = utils.get_next_valid_time(temp_host.next_check,temp_host.check_period);
            temp_host.next_check = next_valid_time;
         }
         
         logger.debug("\t\t\tActual Check Time: "+temp_host.next_check+" -. " + new Date(temp_host.next_check *1000 ).toString() );
         
         if( scheduling_info.first_host_check == 0 || ( temp_host.next_check < scheduling_info.first_host_check ))
            scheduling_info.first_host_check=temp_host.next_check;
         if(temp_host.next_check > scheduling_info.last_host_check)
            scheduling_info.last_host_check=temp_host.next_check;
         mult_factor++;
      }
      
      /* add scheduled host checks to event queue */
      for ( ListIterator iter = objects.host_list.listIterator(); iter.hasNext(); ) {
         objects_h.host temp_host = (objects_h.host) iter.next();
         
         /* skip hosts that shouldn't be scheduled */
         if( temp_host.should_be_scheduled == common_h.FALSE )
            continue;
         
         /* schedule a new host check event */
         schedule_new_event( blue_h.EVENT_HOST_CHECK, common_h.FALSE, temp_host.next_check, common_h.FALSE, 0, null, common_h.TRUE, temp_host, null);
      }
      
      /******** SCHEDULE MISC EVENTS ********/
      
      /* add a host and service check rescheduling event */
      if(blue.auto_reschedule_checks==common_h.TRUE)
         schedule_new_event(blue_h.EVENT_RESCHEDULE_CHECKS,common_h.TRUE,current_time+blue.auto_rescheduling_interval,common_h.TRUE,blue.auto_rescheduling_interval,null,common_h.TRUE,null,null);
      
      /* add a service check reaper event */
      schedule_new_event(blue_h.EVENT_SERVICE_REAPER,common_h.TRUE,current_time+blue.service_check_reaper_interval,common_h.TRUE,blue.service_check_reaper_interval,null,common_h.TRUE,null,null);
      
      /* add an orphaned service check event */
      if(blue.check_orphaned_services==common_h.TRUE)
         schedule_new_event(blue_h.EVENT_ORPHAN_CHECK,common_h.TRUE,current_time+(blue.service_check_timeout*2),common_h.TRUE,(blue.service_check_timeout*2),null,common_h.TRUE,null,null);
      
      /* add a service result "freshness" check event */
      if(blue.check_service_freshness==common_h.TRUE)
         schedule_new_event(blue_h.EVENT_SFRESHNESS_CHECK,common_h.TRUE,current_time + blue.service_freshness_check_interval,common_h.TRUE,blue.service_freshness_check_interval,null,common_h.TRUE,null,null);
      
      /* add a host result "freshness" check event */
      if(blue.check_host_freshness==common_h.TRUE)
         schedule_new_event(blue_h.EVENT_HFRESHNESS_CHECK,common_h.TRUE,current_time + blue.host_freshness_check_interval,common_h.TRUE,blue.host_freshness_check_interval,null,common_h.TRUE,null,null);
      
      /* add a status save event */
      if(blue.aggregate_status_updates==common_h.TRUE)
         schedule_new_event(blue_h.EVENT_STATUS_SAVE,common_h.TRUE,current_time+ blue.status_update_interval,common_h.TRUE,blue.status_update_interval,null,common_h.TRUE,null,null);
      
      /* add an external command check event if needed */
      if(blue.check_external_commands==common_h.TRUE){
         if(blue.command_check_interval==-1)
            interval_to_use= 60;
         else
            interval_to_use= blue.command_check_interval;
         schedule_new_event(blue_h.EVENT_COMMAND_CHECK,common_h.TRUE,current_time+interval_to_use,common_h.TRUE,interval_to_use,null,common_h.TRUE,null,null);
      }
      
      /* add a log rotation event if necessary */
      if(blue.log_rotation_method!= common_h.LOG_ROTATION_NONE) {
         blue_h.timed_event_timing_func run_get_next_log_rotation_time = new blue_h.timed_event_timing_func () {
            public long get_time() {
               return utils.get_next_log_rotation_time();
            }
         };
         schedule_new_event(blue_h.EVENT_LOG_ROTATION,common_h.TRUE, utils.get_next_log_rotation_time(),common_h.TRUE,0, run_get_next_log_rotation_time,common_h.TRUE,null,null);
      }
      
      /* add a retention data save event if needed */
      if( blue.retain_state_information==common_h.TRUE && blue.retention_update_interval>0)
         schedule_new_event(blue_h.EVENT_RETENTION_SAVE,common_h.TRUE,current_time+(blue.retention_update_interval*60),common_h.TRUE,(blue.retention_update_interval*60),null,common_h.TRUE,null,null);
      
      logger.trace( "exiting " + cn + ".init_timing_loop");
      return;
   }
   
   /* displays service check scheduling information */
   public static void display_scheduling_info(){
      // Calendar time = Calendar.getInstance();
      double minimum_concurrent_checks;
      double max_reaper_interval;
      int suggestions=0;
      
      System.out.println("Projected scheduling information for host and service");
      System.out.println("checks is listed below.  This information assumes that");
      System.out.println("you are going to start running Nagios with your current");
      System.out.println("config files.");
      System.out.println();
      
      System.out.println("HOST SCHEDULING INFORMATION");
      System.out.println("---------------------------");
      System.out.println("Total hosts:                     " + scheduling_info.total_hosts);
      System.out.println("Total scheduled hosts:           " + scheduling_info.total_scheduled_hosts);
      
      System.out.print("Host inter-check delay method:   ");
      if( blue.host_inter_check_delay_method== blue_h.ICD_NONE)
         System.out.println("NONE");
      else if( blue.host_inter_check_delay_method==blue_h.ICD_DUMB)
         System.out.println("DUMB");
      else if( blue.host_inter_check_delay_method==blue_h.ICD_SMART){
         System.out.println("SMART");
         System.out.println("Average host check interval:     "+ scheduling_info.average_host_check_interval+" sec");
      }
      else
         System.out.println("USER-SUPPLIED VALUE");
      System.out.println("Host inter-check delay:          "+scheduling_info.host_inter_check_delay+" sec");
      System.out.println("Max host check spread:           "+scheduling_info.max_host_check_spread+" min");
      System.out.println("First scheduled check:           " + ((scheduling_info.total_scheduled_hosts==0)?"N/A": new Date(scheduling_info.first_host_check*1000).toString() ));
      System.out.println("Last scheduled check:            " + ((scheduling_info.total_scheduled_hosts==0)?"N/A": new Date(scheduling_info.last_host_check*1000).toString() ));
      System.out.println();
      System.out.println();
      
      System.out.println("SERVICE SCHEDULING INFORMATION");
      System.out.println("-------------------------------");
      System.out.println("Total services:                     " + scheduling_info.total_services);
      System.out.println("Total scheduled services:           " + scheduling_info.total_scheduled_services);
      
      System.out.print("Service inter-check delay method:   ");
      if(blue.service_inter_check_delay_method==blue_h.ICD_NONE)
         System.out.println("NONE");
      else if(blue.service_inter_check_delay_method==blue_h.ICD_DUMB)
         System.out.println("DUMB");
      else if(blue.service_inter_check_delay_method==blue_h.ICD_SMART){
         System.out.println("SMART");
         System.out.println("Average service check interval:     "+scheduling_info.average_service_check_interval+" sec");
      }
      else
         System.out.println("USER-SUPPLIED VALUE");
      System.out.println("Inter-check delay:                  "+scheduling_info.service_inter_check_delay+" sec");
      
      System.out.println("Interleave factor method:           " + ((blue.service_interleave_factor_method==blue_h.ILF_USER)?"USER-SUPPLIED VALUE":"SMART"));
      if(blue.service_interleave_factor_method==blue_h.ILF_SMART)
         System.out.println("Average services per host:          " + scheduling_info.average_services_per_host);
      System.out.println("Service interleave factor:          " + scheduling_info.service_interleave_factor);
      
      System.out.println("Max service check spread:           "+scheduling_info.max_service_check_spread+" min");
      System.out.println("First scheduled check:              " + new Date(scheduling_info.first_service_check*1000).toString());
      System.out.println("Last scheduled check:               " + new Date(scheduling_info.last_service_check*1000).toString());
      System.out.println();
      System.out.println();
      
      System.out.println("CHECK PROCESSING INFORMATION");
      System.out.println("----------------------------");
      System.out.println("Service check reaper interval:      "+blue.service_check_reaper_interval+" sec");
      System.out.print("Max concurrent service checks:      ");
      if( blue.max_parallel_service_checks==0)
         System.out.println("Unlimited");
      else
         System.out.println( blue.max_parallel_service_checks);
      System.out.println();
      System.out.println();
      
      System.out.println("PERFORMANCE SUGGESTIONS");
      System.out.println("-----------------------");
      
      /* check sanity of host inter-check delay */
      if(scheduling_info.host_inter_check_delay<=10.0 && scheduling_info.total_scheduled_hosts>0){
         System.out.println("* Host checks might be scheduled too closely together - consider increasing 'check_interval' option for your hosts");
         suggestions++;
      }
      
      /* assume a 100% (2x) service check burst for max concurrent checks */
      if(scheduling_info.service_inter_check_delay==0.0)
         minimum_concurrent_checks=Math.ceil(blue.service_check_reaper_interval*2.0);
      minimum_concurrent_checks=Math.ceil((blue.service_check_reaper_interval*2.0)/scheduling_info.service_inter_check_delay);
      if(((int)minimum_concurrent_checks > blue.max_parallel_service_checks) && blue.max_parallel_service_checks!=0){
         System.out.println("* Value for 'max_concurrent_checks' option should >= " + (int)minimum_concurrent_checks);
         suggestions++;
      }
      
      /* assume a 100% (2x) service check burst for service check reaper */
      max_reaper_interval=Math.floor(blue_h.SERVICE_BUFFER_SLOTS/scheduling_info.service_inter_check_delay);
      if(max_reaper_interval<2.0)
         max_reaper_interval=2.0;
      if(max_reaper_interval>30.0)
         max_reaper_interval=30.0;
      if((int)max_reaper_interval<blue.service_check_reaper_interval){
         System.out.println("* Value for 'service_reaper_frequency' should be <= "+ ((int)max_reaper_interval)+" seconds");
         suggestions++;
      }
      if(blue.service_check_reaper_interval<2){
         System.out.println("* Value for 'service_reaper_frequency' should be >= 2 seconds");
         suggestions++;
      }
      
      if(suggestions==0)
         System.out.println("I have no suggestions - things look okay.");
      
      System.out.println();
      
      return;
   }
   
   
   /* schedule a new timed event */
   public static int schedule_new_event(
         int event_type, 
         int high_priority, 
         long run_time, 
         int recurring, 
         long event_interval, 
         blue_h.timed_event_timing_func timing_func, 
         int compensate_for_time_change, 
         Object event_data, 
         Object event_args){
      
//    timed_event **event_list;
      
      logger.trace( "entering " + cn + ".schedule_new_event");
      
      ArrayList event_list;
      if( high_priority == common_h.TRUE)
         event_list = event_list_high;
      else
         event_list = event_list_low;
      
      blue_h.timed_event new_event= new blue_h.timed_event();
      new_event.event_type=event_type;
      new_event.event_data=event_data;
      new_event.event_args=event_args;
      new_event.run_time=run_time;
      new_event.recurring=recurring;
      new_event.event_interval=event_interval;
      new_event.timing_func=timing_func;
      new_event.compensate_for_time_change=compensate_for_time_change;
      
      /* add the event to the event list */
      add_event( new_event, event_list);
      
      logger.trace( "exiting " + cn + ".schedule_new_event");
      return common_h.OK;
   }
   
   
   /* reschedule an event in order of execution time */
   public static void reschedule_event( blue_h.timed_event event, ArrayList event_list){
      long current_time;
      
      logger.trace( "entering " + cn + ".reschedule_event");
      
      logger.debug("INITIAL TIME: " + new Date( event.run_time*1000).toString() );
      
      /* reschedule recurring events... */
      if(event.recurring==common_h.TRUE){
         
         /* use custom timing function */
         if(event.timing_func!=null){
            event.run_time=event.timing_func.get_time();
         }
         
         /* normal recurring events */
         else{
            event.run_time=event.run_time+event.event_interval;
            current_time = utils.currentTimeInSeconds();
            if(event.run_time<current_time)
               event.run_time=current_time;
         }
      }
      
      logger.debug("RESCHEDULED TIME: " + new Date (event.run_time *1000).toString() );
      
      /* add the event to the event list */
      add_event(event,event_list);
      
      logger.trace( "exiting " + cn + ".reschedule_event");
      
      return;
   }
   
   /* remove event from schedule */
   public static int deschedule_event(int event_type, int high_priority, Object event_data, Object event_args){
      ArrayList event_list;
      blue_h.timed_event temp_event = null;
      boolean found=false;
      
      logger.trace( "entering " + cn + ".deschedule_event" );
      
      if(high_priority==common_h.TRUE)
         event_list=event_list_high;
      else
         event_list=event_list_low;
      
      for ( blue_h.timed_event iter_event : (ArrayList<blue_h.timed_event>)  event_list ) {
         if(iter_event.event_type==event_type && iter_event.event_data==event_data && iter_event.event_args==event_args){
            found=true;
            temp_event = iter_event;
            break;
         }
      }
      
      /* remove the event from the event list */
      if (found){
         events.remove_event(temp_event,event_list);
      }
      else{
         return common_h.ERROR; 
      }
      
      logger.trace( "exiting " + cn + ".deschedule_event" );
      
      return common_h.OK;
   }
   
   /* add an event to list ordered by execution time */
   public static void add_event( blue_h.timed_event event, ArrayList /* timed_event */ event_list)
   {
      
      logger.trace( "entering " + cn + ".add_event");
      
      
      /* add the event to the head of the list if there are no other events */
      if( event_list.size() == 0)
         event_list.add(event);
      
      /* add event to head of the list if it should be executed first */
      else
      {
         for ( ListIterator iter = event_list.listIterator(); iter.hasNext(); )
         {
            blue_h.timed_event temp_event = (blue_h.timed_event) iter.next();
            
            if ( event.run_time < temp_event.run_time )
            {
               event_list.add( iter.nextIndex() - 1, event );
               break;
            }
            else if ( !iter.hasNext() )
            {
               event_list.add( event );
               break;
            }
         }
         
      }
      
      /* send event data to broker */
      broker.broker_timed_event(broker_h.NEBTYPE_TIMEDEVENT_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,event,null);
      
      logger.trace( "exiting " + cn + ".add_event");
   }
   
   /* remove an event from the queue */
   public static void remove_event(blue_h.timed_event event, ArrayList event_list){
      logger.trace( "entering " + cn + ".remove_event" );
      
      /* send event data to broker */
      broker.broker_timed_event(broker_h.NEBTYPE_TIMEDEVENT_REMOVE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,event,null);
      
      if(event_list==null || event_list.isEmpty() )
         return;
      
      event_list.remove( event );
      
      logger.trace( "exiting " + cn + ".remove_event" );
   }
   
   /* this is the main event handler loop */
  
   public static int event_execution_loop()
   {
      
	  blue_h.timed_event temp_event;
      blue_h.timed_event top_event_list_high;
      blue_h.timed_event top_event_list_low;
      blue_h.timed_event sleep_event;
      
      long last_time;
      long current_time;
      int run_event= common_h.TRUE;

      //    host *temp_host;
      objects_h.service temp_service;
      blue_h.timespec delay = new blue_h.timespec();
      blue_h.timeval tv;
      
      logger.trace("entering " + cn + ".event_execution_loop");
      
      last_time = utils.currentTimeInSeconds();
      
      /* initialize fake "sleep" event */
      sleep_event = new blue_h.timed_event();
      sleep_event.event_type=blue_h.EVENT_SLEEP;
      sleep_event.run_time=last_time;
      sleep_event.recurring=common_h.FALSE;
      sleep_event.event_interval=0L;
      sleep_event.compensate_for_time_change=common_h.FALSE;
      sleep_event.timing_func=null;
      sleep_event.event_data=common_h.FALSE;
      sleep_event.event_args=common_h.FALSE;
      
      while(true)
      {
         
         /* see if we should exit or restart (a signal was encountered) */
         if(blue.sigshutdown == common_h.TRUE || blue.sigrestart == common_h.TRUE)
            break;
         
         /* if we don't have any events to handle, exit */
         if(event_list_high==null && event_list_low==null)
         {
            logger.debug("There aren't any events that need to be handled!");
            break;
         }
         
         /* get the current time */
         current_time = utils.currentTimeInSeconds();
         
         /* hey, wait a second...  we traveled back in time! */
         if(current_time<last_time)
            compensate_for_system_time_change(last_time,current_time);
         
         /* else if the time advanced over the specified threshold, try and compensate... */
         else if((current_time-last_time)>=blue.time_change_threshold)
            compensate_for_system_time_change(last_time, current_time);
         
         /* keep track of the last time */
         last_time=current_time;
         
         /* If the high priority event list is not empty, then pull out the first event to be processed */
          
         if (!event_list_high.isEmpty())
            top_event_list_high = (blue_h.timed_event) event_list_high.get(0);
         else 
            top_event_list_high = null;
         
         /* If the low priority event list is not empty, then pull out the first event to be processed */
         if ( !event_list_low.isEmpty() )
            top_event_list_low = (blue_h.timed_event) event_list_low.get( 0 );
         else 
            top_event_list_low = null;
         
                  
         logger.debug("*** Event Check Loop ***");
         logger.debug("\tCurrent time: " + new Date( current_time *1000).toString() );
         if(top_event_list_high!=null)
            logger.debug("\tNext High Priority Event Time: " + new Date(top_event_list_high.run_time*1000).toString() );
         else
            logger.debug("\tNo high priority events are scheduled...");
         if(top_event_list_low!=null)
            logger.debug("\tNext Low Priority Event Time:  " + new Date(top_event_list_low.run_time*1000).toString() );
         else
            logger.debug("\tNo low priority events are scheduled...");
         
         logger.debug("Current/Max Outstanding Service Checks: "+blue.currently_running_service_checks+"/"+blue.max_parallel_service_checks );
         
         /* handle high priority events */
         if(top_event_list_high != null && (current_time >= top_event_list_high.run_time))
         {
            logger.debug("Handling high priority event.");
            /* remove the first event from the timing loop */
            event_list_high.remove(0);
            
            /* handle the event */
            handle_timed_event(top_event_list_high);
            
            /* reschedule the event if necessary */
            if(top_event_list_high.recurring == common_h.TRUE)
               reschedule_event(top_event_list_high,event_list_high);
         }
         
         /* handle low priority events */
         else if(top_event_list_low != null && (current_time >= top_event_list_low.run_time))
         {
            temp_event = top_event_list_low;
            
            /* default action is to execute the event */
            run_event = common_h.TRUE;
            
            /* run a few checks before executing a service check... */
            if(temp_event.event_type== blue_h.EVENT_SERVICE_CHECK)
            {
               temp_service = (objects_h.service)temp_event.event_data;
               
               /* update service check latency */
               tv = new blue_h.timeval();
               // TODO RE-EVALUATE TIME ACROSS ALL BLUE
               temp_service.latency= ((tv.tv_sec-temp_event.run_time)+(tv.tv_usec/1000)/*/1000.0*/);
               
               /* don't run a service check if we're not supposed to right now */
               
               if(blue.execute_service_checks==common_h.FALSE && ((temp_service.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION)==0))
               {
                  // Change back to debug..
            	  logger.info("\tWe're not executing service checks right now...");
                  
                  /* remove the service check from the event queue and reschedule it for a later time */
                  
            	  event_list_low.remove(temp_event);
                  
            	  if(temp_service.state_type == common_h.SOFT_STATE && temp_service.current_state != blue_h.STATE_OK)
                     temp_service.next_check =(temp_service.next_check+(temp_service.retry_interval* blue.interval_length));
                  else
                     temp_service.next_check =(temp_service.next_check+(temp_service.check_interval* blue.interval_length));

            	  temp_event.run_time=temp_service.next_check;
                  reschedule_event(temp_event,event_list_low);
                  statusdata.update_service_status(temp_service,common_h.FALSE);
                  
                  run_event= common_h.FALSE;
               }
               
               /* don't run a service check if we're already maxed out on the number of parallel service checks...  */
               
               if(blue.max_parallel_service_checks!=0 && (blue.currently_running_service_checks >= blue.max_parallel_service_checks))
               {
                  logger.debug("\tMax concurrent service checks ("+ blue.max_parallel_service_checks+") has been reached.  Delaying further checks until previous checks are complete...");
                  run_event= common_h.FALSE;
               }
               
               /* don't run a service check that can't be parallized if there are other checks out there... */
               
               if(temp_service.parallelize== common_h.FALSE && blue.currently_running_service_checks>0)
               {
                  logger.debug("\tA non-parallelizable check is queued for execution, but there are still other checks executing.  We'll wait...");
                  run_event= common_h.FALSE;
               }
               
               /* a service check that shouldn't be parallized with other checks is currently running, so don't execute another check */
               
               if( blue.non_parallelized_check_running== common_h.TRUE)
               {
                  logger.debug("\tA non-parallelizable check is currently running, so we have to wait before executing other checks...");
                  run_event= common_h.FALSE;
               }
            }
            
            /* run a few checks before executing a host check... */
            
            if(temp_event.event_type == blue_h.EVENT_HOST_CHECK)
            {
               
               objects_h.host temp_host=(objects_h.host)temp_event.event_data;
               
               /* update host check latency */
               
               tv = new blue_h.timeval();
               temp_host.latency=((tv.tv_sec-temp_event.run_time)+(tv.tv_usec/1000)/*/1000.0*/);
               
               /* don't run a host check if we're not supposed to right now */
               if(blue.execute_host_checks == common_h.FALSE && ((temp_host.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION)==0))
               {
                  logger.debug("\tWe're not executing host checks right now...");
                  
                  /* remove the host check from the event queue and reschedule it for a later time */
                  event_list_low.remove( temp_event );
                  temp_host.next_check=(temp_host.next_check+(temp_host.check_interval* blue.interval_length));
                  temp_event.run_time=temp_host.next_check;
                  reschedule_event(temp_event,event_list_low);
                  statusdata.update_host_status(temp_host, common_h.FALSE);
                  
                  run_event= common_h.FALSE;
               }
            }
            
            /* run the event except... */
            
            if(run_event==common_h.TRUE){
               
               /* remove the first event from the timing loop */
               event_list_low.remove(temp_event);
               
               /* handle the event */
               handle_timed_event(temp_event);
               
               /* reschedule the event if necessary */
               if(temp_event.recurring== common_h.TRUE)
                  reschedule_event(temp_event,event_list_low);
               
            }
            
            /* wait a while so we don't hog the CPU... */
            else
            {
               try
               { 
                  Thread.sleep((long)(blue.sleep_time * 1000)); 
               }
               catch(InterruptedException iE)
               {}
            }
        }
         
         /* we don't have anything to do at this moment in time... */
         else if( (event_list_high.isEmpty() || (current_time<top_event_list_high.run_time)) && (event_list_low.isEmpty() || (current_time<top_event_list_low.run_time))){
            
            /* check for external commands if we're supposed to check as often as possible */
            if(blue.command_check_interval==-1)
               commands.check_for_external_commands();
            
            /* populate fake "sleep" event */
            sleep_event.run_time=current_time;
            sleep_event.event_data=(Object)delay;
            
            /* send event data to broker */
            broker.broker_timed_event(broker_h.NEBTYPE_TIMEDEVENT_SLEEP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,sleep_event,null);
            
            /* wait a while so we don't hog the CPU... */
            try { Thread.sleep( (long) (blue.sleep_time * 1000) ); } catch ( InterruptedException iE ) {}
         }
         
      }
      
      logger.info("Exiting event execution loop!");
      logger.trace( "exiting " + cn + ".event_execution_loop" );
      
      return common_h.OK;
   }
   
   /* handles a timed event */
   public static int handle_timed_event(blue_h.timed_event event)
   {
      String temp_buffer;
            
      logger.debug( "entering " + cn + ".handle_timed_event" );
      
      /* send event data to broker */
      broker.broker_timed_event(broker_h.NEBTYPE_TIMEDEVENT_EXECUTE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,event,null);
      
      logger.debug("*** Event Details ***");
      logger.debug("\tEvent time: " + new Date(event.run_time*1000).toString() );
      logger.debug("\tEvent type: " + event.event_type);
      
      /* how should we handle the event? */
      switch(event.event_type)
      {
         
         case blue_h.EVENT_SERVICE_CHECK:
            logger.debug("(service check)");
            
            objects_h.service temp_service=(objects_h.service)event.event_data;
            logger.debug("\t\tService Description: " + temp_service.description);
            logger.debug("\t\tAssociated Host:     " + temp_service.host_name);
            
            /* run  a service check */
            checks.run_service_check(temp_service);
            break;
            
         case blue_h.EVENT_HOST_CHECK:
            logger.debug("(host check)");
            
            objects_h.host temp_host=(objects_h.host)event.event_data;
            logger.debug("\t\tHost:     " + temp_host.name);
            
            /* run a host check */
            checks.run_scheduled_host_check(temp_host);
            logger.debug("running scheduled_host_check");
            break;
            
         case blue_h.EVENT_COMMAND_CHECK:
            logger.debug("(external command check)");
            
            /* check for external commands */
            commands.check_for_external_commands();
            break;
            
         case blue_h.EVENT_LOG_ROTATION:
            logger.debug("(log file rotation)");
            
            /* rotate the log file */
            logging.rotate_log_file(event.run_time);
            break;
            
         case blue_h.EVENT_PROGRAM_SHUTDOWN:
            logger.debug("(program shutdown)");
            
            /* set the shutdown flag */
            blue.sigshutdown=common_h.TRUE;
            
            /* log the shutdown */
            temp_buffer = "PROGRAM_SHUTDOWN event encountered, shutting down...";
            logger.debug( temp_buffer );
            break;
            
         case blue_h.EVENT_PROGRAM_RESTART:
            logger.debug("(program restart)");
            
            /* set the restart flag */
            blue.sigrestart=common_h.TRUE;
            
            /* log the restart */
            temp_buffer = "PROGRAM_RESTART event encountered, restarting...";
            logger.debug(temp_buffer );
            break;
            
         case blue_h.EVENT_SERVICE_REAPER:
            logger.debug("(service check reaper)");
            
            /* reap service check results */
            checks.reap_service_checks();
            break;
            
         case blue_h.EVENT_ORPHAN_CHECK:
            logger.debug("(orphaned service check)");
            
            /* check for orphaned services */
            checks.check_for_orphaned_services();
            break;
            
         case blue_h.EVENT_RETENTION_SAVE:
            logger.debug("(retention save)");
            
            /* save state retention data */
            sretention.save_state_information(blue.config_file,common_h.TRUE);
            break;
            
         case blue_h.EVENT_STATUS_SAVE:
            logger.debug("(status save)");
            
            /* save all status data (program, host, and service) */
            statusdata.update_all_status_data();
            break;
            
         case blue_h.EVENT_SCHEDULED_DOWNTIME:
            logger.debug("(scheduled downtime)");
            
            /* process scheduled downtime info */
            downtime.handle_scheduled_downtime_by_id((Long)event.event_data);
            break;
            
         case blue_h.EVENT_SFRESHNESS_CHECK:
            logger.debug("(service result freshness check)");
            
            /* check service result freshness */
            checks.check_service_result_freshness();
            break;
            
         case blue_h.EVENT_HFRESHNESS_CHECK:
            
            logger.debug("(host result freshness check)");
            /* check host result freshness */
            checks.check_host_result_freshness();
            break;
            
         case blue_h.EVENT_EXPIRE_DOWNTIME:
            logger.debug("(expire downtime)");
            
            /* check for expired scheduled downtime entries */
            downtime.check_for_expired_downtime();
            break;
            
         case blue_h.EVENT_RESCHEDULE_CHECKS:
            logger.debug("(reschedule checks)");
            
            /* adjust scheduling of host and service checks */
            adjust_check_scheduling();
            break;
            
            
         case blue_h.EVENT_EXPIRE_COMMENT:
            logger.debug("(expire comment)\n");
            /* check for expired comment */
            if (event.event_data != null ) 
               comments.check_for_expired_comment( ((Long)event.event_data).longValue() );
            else
               logger.warn( "Warning: EXPIRED COMMENT EVENT retrieved without comment id." );
            break;            
            
         case blue_h.EVENT_USER_FUNCTION:
            logger.debug("(user function)");
            
            /* run a user-defined function */
            if(event.event_data!=null){
               Runnable userfunc = (Runnable) event.event_data;
               userfunc.run();
               // TODO Should this happen in its own thread
            }
            break;
            
         default:
            
            break;
      }
      
      logger.trace( "exiting " + cn + ".handle_timed_event" );
      
      return common_h.OK;
   }
   
   /* adjusts scheduling of host and service checks */
   public static void adjust_check_scheduling(){
//    nagios_h.timed_event temp_event;
      objects_h.service temp_service=null;
      objects_h.host temp_host=null;
      double projected_host_check_overhead=0.9;
      double projected_service_check_overhead=0.1;
      long current_time;
      long first_window_time=0L;
      long last_window_time=0L;
      long last_check_time=0L;
      long new_run_time=0L;
      int total_checks=0;
      int current_check=0;
      double inter_check_delay=0.0;
      double current_icd_offset=0.0;
      double total_check_exec_time=0.0;
      double last_check_exec_time=0.0;
      int adjust_scheduling=common_h.FALSE;
      double exec_time_factor=0.0;
      double current_exec_time=0.0;
      double current_exec_time_offset=0.0;
      double new_run_time_offset=0.0;
      
      logger.trace( "entering " + cn + ".adjust_check_scheduling" );
      
      /* TODO:
       - Track host check overhead on a per-host bases
       - Figure out how to calculate service check overhead 
       */
      
      /* determine our adjustment window */
      current_time = utils.currentTimeInSeconds();
      first_window_time=current_time;
      last_window_time=first_window_time+blue.auto_rescheduling_window;
      
      /* get current scheduling data */
      for ( blue_h.timed_event  temp_event : (ArrayList<blue_h.timed_event>)  events.event_list_low ) {
         
         /* skip events outside of our current window */
         if(temp_event.run_time<=first_window_time)
            continue;
         if(temp_event.run_time>last_window_time)
            break;
         
         if(temp_event.event_type==blue_h.EVENT_HOST_CHECK){
            
            temp_host=(objects_h.host)temp_event.event_data;
            if(temp_host==null)
               continue;
            
            /* ignore forced checks */
            if(( temp_host.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0 )
               continue;
            
            /* does the last check "bump" into this one? */
            if((long)(last_check_time+last_check_exec_time)>temp_event.run_time)
               adjust_scheduling=common_h.TRUE;
            
            last_check_time=temp_event.run_time;
            
            /* calculate time needed to perform check */
            last_check_exec_time=temp_host.execution_time+projected_host_check_overhead;
            total_check_exec_time+=last_check_exec_time;
         }
         
         else if(temp_event.event_type==blue_h.EVENT_SERVICE_CHECK){
            
            temp_service=(objects_h.service)temp_event.event_data;
            if(temp_service==null)
               continue;
            
            /* ignore forced checks */
            if ( (temp_service.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0 )
               continue;
            
            /* does the last check "bump" into this one? */
            if((long)(last_check_time+last_check_exec_time)>temp_event.run_time)
               adjust_scheduling=common_h.TRUE;
            
            last_check_time=temp_event.run_time;
            
            /* calculate time needed to perform check */
            /* NOTE: service check execution time is not taken into account, as service checks are run in parallel */
            last_check_exec_time=projected_service_check_overhead;
            total_check_exec_time+=last_check_exec_time;
         }
         
         else
            continue;
         
         total_checks++;
      }
      
      /* nothing to do... */
      if(total_checks==0 || adjust_scheduling==common_h.FALSE){
         
         /*
          System.out.println();
          System.out.println();
          System.out.println("NOTHING TO DO!");
          System.out.println("# CHECKS:    %d",total_checks);
          System.out.println("WINDOW TIME: %d",auto_rescheduling_window);
          System.out.println("EXEC TIME:   %.3f",total_check_exec_time);
          */
         
         return;
      }
      
      if((long)total_check_exec_time>blue.auto_rescheduling_window){
         inter_check_delay=0.0;
         exec_time_factor=(blue.auto_rescheduling_window/total_check_exec_time);
      }
      else{
         inter_check_delay=(((blue.auto_rescheduling_window)-total_check_exec_time)/(total_checks*1.0));
         exec_time_factor=1.0;
      }
      
      /*
       System.out.println();
       System.out.println();
       System.out.println("TOTAL CHECKS: %d",total_checks);
       System.out.println("WINDOW TIME:  %d",auto_rescheduling_window);
       System.out.println("EXEC TIME:    %.3f",total_check_exec_time);
       System.out.println("ICD:          %.3f",inter_check_delay);
       System.out.println("EXEC FACTOR:  %.3f",exec_time_factor);
       */
      
      /* adjust check scheduling */
      current_icd_offset=(inter_check_delay/2.0);
      for ( blue_h.timed_event temp_event : (ArrayList<blue_h.timed_event>)  event_list_low ) {
         
         /* skip events outside of our current window */
         if(temp_event.run_time<=first_window_time)
            continue;
         if(temp_event.run_time>last_window_time)
            break;
         
         if(temp_event.event_type==blue_h.EVENT_HOST_CHECK){
            
            temp_host=(objects_h.host)temp_event.event_data;
            if(temp_host==null)
               continue;
            
            /* ignore forced checks */
            if( (temp_host.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0 )
               continue;
            
            current_exec_time=((temp_host.execution_time+projected_host_check_overhead)*exec_time_factor);
         }
         
         else if(temp_event.event_type==blue_h.EVENT_SERVICE_CHECK){
            
            temp_service=(objects_h.service)temp_event.event_data;
            if(temp_service==null)
               continue;
            
            /* ignore forced checks */
            if( ( temp_service.check_options & blue_h.CHECK_OPTION_FORCE_EXECUTION) > 0 )
               continue;
            
            /* NOTE: service check execution time is not taken into account, as service checks are run in parallel */
            current_exec_time=(projected_service_check_overhead*exec_time_factor);
         }
         
         else
            continue;
         
         current_check++;
         new_run_time_offset=current_exec_time_offset+current_icd_offset;
         new_run_time=(first_window_time+(long)new_run_time_offset);
         
         /*
          printf("  CURRENT CHECK #:      %d",current_check);
          printf("  CURRENT ICD OFFSET:   %.3f",current_icd_offset);
          printf("  CURRENT EXEC TIME:    %.3f",current_exec_time);
          printf("  CURRENT EXEC OFFSET:  %.3f",current_exec_time_offset);
          printf("  NEW RUN TIME:         %lu",new_run_time);
          */
         
         if(temp_event.event_type==blue_h.EVENT_HOST_CHECK){
            temp_event.run_time=new_run_time;
            temp_host.next_check=new_run_time;
            statusdata.update_host_status(temp_host,common_h.FALSE);
         }
         else{
            temp_event.run_time=new_run_time;
            temp_service.next_check=new_run_time;
            statusdata.update_service_status(temp_service,common_h.FALSE);
         }
         
         current_icd_offset+=inter_check_delay;
         current_exec_time_offset+=current_exec_time;
      }
      
      /* resort event list (some events may be out of order at this point) */
      resort_event_list(event_list_low);
      
      logger.trace( "exiting " + cn + ".adjust_check_scheduling" );
      
      return;
   }
   
   /* attempts to compensate for a change in the system time */
   public static void compensate_for_system_time_change(long last_time,long current_time){
      long time_difference;
      
      logger.trace( "entering " + cn + ".compensate_for_system_time_change" );
      
      /* we moved back in time... */
      if(last_time>current_time)
         time_difference=last_time-current_time;
      
      /* we moved into the future... */
      else
         time_difference=current_time-last_time;
      
      /* log the time change */
      logger.warn( "Warning: A system time change of "+time_difference+" seconds ("+((last_time>current_time)?"backwards":"forwards")+" in time) has been detected.  Compensating...");
      
      /* adjust the next run time for all high priority timed events */
      for ( blue_h.timed_event temp_event : (ArrayList<blue_h.timed_event>) event_list_high ) {
         
         /* skip special events that occur at specific times... */
         if(temp_event.compensate_for_time_change==common_h.FALSE)
            continue;
         
         /* use custom timing function */
         if(temp_event.timing_func!=null){
            temp_event.run_time=temp_event.timing_func.get_time();
         }
         
         /* else use standard adjustment */
         else
            temp_event.run_time = adjust_timestamp_for_time_change(last_time,current_time,time_difference, temp_event.run_time);
      }
      
      /* resort event list (some events may be out of order at this point) */
      resort_event_list(event_list_high);
      
      /* adjust the next run time for all low priority timed events */
      for ( blue_h.timed_event temp_event : (ArrayList<blue_h.timed_event>)  event_list_low ) {
         
         /* skip special events that occur at specific times... */
         if(temp_event.compensate_for_time_change==common_h.FALSE)
            continue;
         
         /* use custom timing function */
         if(temp_event.timing_func!=null){
            temp_event.run_time=temp_event.timing_func.get_time();
         }
         
         /* else use standard adjustment */
         else
            temp_event.run_time = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_event.run_time);
      }
      
      /* resort event list (some events may be out of order at this point) */
      resort_event_list(event_list_low);
      
      /* adjust service timestamps */
      for ( objects_h.service temp_service : (ArrayList<objects_h.service>)  objects.service_list ) {
         
         temp_service.last_notification= adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_service.last_notification);
         temp_service.last_check = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_service.last_check);
         temp_service.next_check = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_service.next_check);
         temp_service.last_state_change = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_service.last_state_change);
         temp_service.last_hard_state_change = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_service.last_hard_state_change);
         
         /* recalculate next re-notification time */
         temp_service.next_notification = notifications.get_next_service_notification_time(temp_service,temp_service.last_notification);
         
         /* update the status data */
         statusdata.update_service_status(temp_service,common_h.FALSE);
      }
      
      /* adjust host timestamps */
      for ( objects_h.host temp_host : (ArrayList<objects_h.host>)  objects.host_list ) {
         
         temp_host.last_host_notification = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_host.last_host_notification);
         temp_host.last_check = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_host.last_check);
         temp_host.next_check = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_host.next_check);
         temp_host.last_state_change = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_host.last_state_change);
         temp_host.last_hard_state_change = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_host.last_hard_state_change);
         temp_host.last_state_history_update = adjust_timestamp_for_time_change(last_time,current_time,time_difference,temp_host.last_state_history_update);
         
         /* recalculate next re-notification time */
         temp_host.next_host_notification= notifications.get_next_host_notification_time(temp_host,temp_host.last_host_notification);
         
         /* update the status data */
         statusdata.update_host_status(temp_host,common_h.FALSE);
      }
      
      /* adjust program timestamps */
      blue.program_start = adjust_timestamp_for_time_change(last_time,current_time,time_difference,blue.program_start);
      blue.last_command_check = adjust_timestamp_for_time_change(last_time,current_time,time_difference, blue.last_command_check);
      
      /* update the status data */
      statusdata.update_program_status(common_h.FALSE);
      
      
      logger.trace( "exiting " + cn + ".compensate_for_system_time_change" );
   }
   
   /* resorts an event list by event execution time - needed when compensating for system time changes */
   public static void resort_event_list(ArrayList event_list){
      
      
      Comparator<blue_h.timed_event> comparator = new Comparator<blue_h.timed_event>() {
         public int compare ( blue_h.timed_event event1, blue_h.timed_event event2 ) {
            return (int) Math.signum(  (event1.run_time - event2.run_time ) );
         }
      };
      Collections.sort( event_list, comparator );
      
      return;
   }
   
   /* adjusts a timestamp variable in accordance with a system time change */
   public static long adjust_timestamp_for_time_change(long last_time, long current_time, long time_difference, long ts){
      
      /* we shouldn't do anything with epoch values */
      if(ts==0)
         return ts;
      
      /* we moved back in time... */
      if(last_time>current_time){
         
         /* we can't precede the UNIX epoch */
         if(time_difference > ts)
            return 0;
         else
            return (ts-time_difference);
      }
      
      /* we moved into the future... */
      else
         return (ts+time_difference);
      
   }
   
}