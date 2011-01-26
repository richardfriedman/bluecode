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

public class statusdata_h {
    
    /*************************** STATUS ***************************/
    public static final int READ_PROGRAM_STATUS	= 1;
    public static final int READ_HOST_STATUS	= 2;
    public static final int READ_SERVICE_STATUS	=4;
    
    public static final int READ_ALL_STATUS_DATA    = READ_PROGRAM_STATUS | READ_HOST_STATUS | READ_SERVICE_STATUS;
    
    
    /*************************** SERVICE STATES ***************************/
    
    public static final int SERVICE_PENDING         = 1;
    public static final int SERVICE_OK               = 2;
    public static final int SERVICE_WARNING         = 4;
    public static final int SERVICE_UNKNOWN         = 8;
    public static final int SERVICE_CRITICAL        = 16;
    
    
    
    /**************************** HOST STATES ****************************/
    
    public static final int HOST_PENDING            = 1;
    public static final int HOST_UP                  = 2;
    public static final int HOST_DOWN               = 4;
    public static final int HOST_UNREACHABLE        = 8;
    
    /*************************** CHAINED HASH LIMITS ***************************/
    
    public static final int SERVICESTATUS_HASHSLOTS      = 1024;
    public static final int HOSTSTATUS_HASHSLOTS         = 1024;
    
    
    /**************************** DATA STRUCTURES ******************************/
    
    /* HOST STATUS structure */
    public static class hoststatus {
        public String host_name;
        public String plugin_output;
        public String perf_data;
        public int     status;
        public long  last_update;
        public int     has_been_checked;
        public int     should_be_scheduled;
        public int     current_attempt;
        public int     max_attempts;
        public long  last_check;
        public long  next_check;
        public int     check_type;
        public long	last_state_change;
        public long	last_hard_state_change;
        public int     last_hard_state;
        public long  last_time_up;
        public long  last_time_down;
        public long  last_time_unreachable;
        public int     state_type;
        public long  last_notification;
        public long  next_notification;
        public int     no_more_notifications;
        public int     notifications_enabled;
        public int     problem_has_been_acknowledged;
        public int     acknowledgement_type;
        public int     current_notification_number;
        public int     accept_passive_host_checks;
        public int     event_handler_enabled;
        public int     checks_enabled;
        public int     flap_detection_enabled;
        public int     is_flapping;
        public double  percent_state_change;
        public double  latency;
        public double  execution_time;
        public int     scheduled_downtime_depth;
        public int     failure_prediction_enabled;
        public int     process_performance_data;
        public int     obsess_over_host;
    }
    
    /* SERVICE STATUS structure */
    public static class servicestatus {
        public String host_name;
        public String description;
        public String plugin_output;
        public String perf_data;
        public int     max_attempts;
        public int     current_attempt;
        public int     status;
        public long  last_update;
        public int     has_been_checked;
        public int     should_be_scheduled;
        public long  last_check;
        public long  next_check;
        public int     check_type;
        public int	checks_enabled;
        public long	last_state_change;
        public long	last_hard_state_change;
        public int	last_hard_state;
        public long  last_time_ok;
        public long  last_time_warning;
        public long  last_time_unknown;
        public long  last_time_critical;
        public int     state_type;
        public long  last_notification;
        public long  next_notification;
        public int     no_more_notifications;
        public int     notifications_enabled;
        public int     problem_has_been_acknowledged;
        public int     acknowledgement_type;
        public int     current_notification_number;
        public int     accept_passive_service_checks;
        public int     event_handler_enabled;
        public int     flap_detection_enabled;
        public int     is_flapping;
        public double  percent_state_change;
        public double  latency;
        public double  execution_time;
        public int     scheduled_downtime_depth;
        public int     failure_prediction_enabled;
        public int     process_performance_data;
        public int     obsess_over_service;
    }
    
}