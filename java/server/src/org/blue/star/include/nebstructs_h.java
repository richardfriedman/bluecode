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

package org.blue.star.include;

public class nebstructs_h {
   
   /****** STRUCTURES *************************/
   
   /* process data structure */
   public static class nebstruct_process_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
   }
   
   
   /* timed event data structure */
   public static class nebstruct_timed_event_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int event_type;
      public int recurring;
      public long run_time;
      public Object event_data;
   }
   
   
   /* log data structure */
   public static class nebstruct_log_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public long entry_time;
      public long data_type;
      public String data;
   }
   
   
   /* system command structure */
   public static class nebstruct_system_command_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public int timeout;
      public String command_line;
      public int early_timeout;
      public double execution_time;
      public int return_code;
      public String output;
   }
   
   
   /* event handler structure */
   public static class nebstruct_event_handler_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int eventhandler_type;
      public String host_name;
      public String service_description;
      public int state_type;
      public int state;
      public int timeout;
      public String command_name;
      public String command_args;
      public String command_line;
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public int early_timeout;
      public double execution_time;
      public int return_code;
      public String output;
   }
   
   
   /* host check structure */
   public static class nebstruct_host_check_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public String host_name;
      public int current_attempt;
      public int check_type;
      public int max_attempts;
      public int state_type;
      public int state;
      public int timeout;
      public String command_name;
      public String command_args;
      public String command_line;
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public int early_timeout;
      public double execution_time;
      public double latency;
      public int return_code;
      public String output;
      public String perf_data;
   }
   
   
   /* service check structure */
   public static class nebstruct_service_check_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public String host_name;
      public String service_description;
      public int check_type;
      public int current_attempt;
      public int max_attempts;
      public int state_type;
      public int state;
      public int timeout;
      public String command_name;
      public String command_args;
      public String command_line;
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public int early_timeout;
      public double execution_time;
      public double latency;
      public int return_code;
      public String output;
      public String perf_data;
   }
   
   
   /* comment data structure */
   public static class nebstruct_comment_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int comment_type;
      public String host_name;
      public String service_description;
      public long entry_time;
      public String author_name;
      public String comment_data;
      public int persistent;
      public int source;
      public int entry_type;
      public int expires;
      public long expire_time;
      public long   comment_id;
   }
   
   
   /* downtime data structure */
   public static class nebstruct_downtime_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int downtime_type;
      public String host_name;
      public String service_description;
      public long entry_time;
      public String author_name;
      public String comment_data;
      public long start_time;
      public long end_time;
      public int fixed;
      public long   duration;
      public long   triggered_by;
      public long   downtime_id;
   }
   
   
   /* flapping data structure */
   public static class nebstruct_flapping_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int flapping_type;
      public String host_name;
      public String service_description;
      public double percent_change;
      public double high_threshold;
      public double low_threshold;
      public long comment_id;
   }
   
   
   /* program status structure */
   public static class nebstruct_program_status_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public long program_start;
      public int pid;
      public int daemon_mode;
      public long last_command_check;
      public long last_log_rotation;
      public int notifications_enabled;
      public int active_service_checks_enabled;
      public int passive_service_checks_enabled;
      public int active_host_checks_enabled;
      public int passive_host_checks_enabled;
      public int event_handlers_enabled;
      public int flap_detection_enabled;
      public int failure_prediction_enabled;
      public int process_performance_data;
      public int obsess_over_hosts;
      public int obsess_over_services;
      public long   modified_host_attributes;
      public long   modified_service_attributes;
      public String global_host_event_handler;
      public String global_service_event_handler;
   }
   
   
   /* host status structure */
   public static class nebstruct_host_status_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public Object object_ptr;
   }
   
   
   /* service status structure */
   public static class nebstruct_service_status_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public Object object_ptr;
   }
   
   
   /* notification data structure */
   public static class nebstruct_notification_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int notification_type;
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public String host_name;
      public String service_description;
      public int reason_type;
      public int state;
      public String output;
      public String ack_author;
      public String ack_data;
      public int escalated;
      public int contacts_notified;
   }
   
   
   /* contact notification data structure */
   public static class nebstruct_contact_notification_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int notification_type;
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public String host_name;
      public String service_description;
      public String contact_name;
      public int reason_type;
      public int state;
      public String output;
      public String ack_author;
      public String ack_data;
      public int escalated;
   }
   
   
   /* contact notification method data structure */
   public static class nebstruct_contact_notification_method_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int notification_type;
      public blue_h.timeval  start_time;
      public blue_h.timeval  end_time;
      public String host_name;
      public String service_description;
      public String contact_name;
      public String command_name;
      public String command_args;
      public int reason_type;
      public int state;
      public String output;
      public String ack_author;
      public String ack_data;
      public int escalated;
   }
   
   
   /* adaptive program data structure */
   public static class nebstruct_adaptive_program_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int command_type;
      public long   modified_host_attribute;
      public long   modified_host_attributes;
      public long   modified_service_attribute;
      public long   modified_service_attributes;
      public String global_host_event_handler;
      public String global_service_event_handler;
   }
   
   
   /* adaptive host data structure */
   public static class nebstruct_adaptive_host_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int command_type;
      public long   modified_attribute;
      public long   modified_attributes;
      public Object object_ptr;
   }
   
   
   /* adaptive service data structure */
   public static class nebstruct_adaptive_service_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int command_type;
      public long   modified_attribute;
      public long   modified_attributes;
      public Object object_ptr;
   }
   
   
   /* external command data structure */
   public static class nebstruct_external_command_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int command_type;
      public long entry_time;
      public String command_string;
      public String command_args;
   }
   
   
   /* aggregated status data structure */
   public static class nebstruct_aggregated_status_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
   }
   
   
   /* retention data structure */
   public static class nebstruct_retention_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
   }
   
   
   /* acknowledgement structure */
   public static class nebstruct_acknowledgement_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int acknowledgement_type;
      public String host_name;
      public String service_description;
      public int state;
      public String author_name;
      public String comment_data;
      public int is_sticky;
      public int persistent_comment;
      public int notify_contacts;
   }
   
   
   /* state change structure */
   public static class nebstruct_statechange_data {
      public int type;
      public int flags;
      public int attr;
      public blue_h.timeval  timestamp;
      
      public int statechange_type;
      public String host_name;
      public String service_description;
      public int state;
      public int state_type;
      public int current_attempt;
      public int max_attempts;
      public String output;
   }
   
}