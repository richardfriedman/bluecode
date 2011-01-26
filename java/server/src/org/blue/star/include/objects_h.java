
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class objects_h {
   /*************** CURRENT OBJECT REVISION **************/
   public static final int CURRENT_OBJECT_STRUCTURE_VERSION        = 2;
    
   /***************** OBJECT SIZE LIMITS *****************/
   /* These setting should be fairly irrelevant in the java implementation */
   /** max. host name length */
    public static int MAX_HOSTNAME_LENGTH            		= 64;
    /** max. service description length */
    public static int MAX_SERVICEDESC_LENGTH			= 64;
    /** max. length of plugin output UPDATED 2.2 */
    public static int MAX_PLUGINOUTPUT_LENGTH			= 332;	
    /** max number of old states to keep track of for flap detection */
    public static int MAX_STATE_HISTORY_ENTRIES		= 21;	
    /** max number of custom addresses a contact can have */
    public static int MAX_CONTACT_ADDRESSES                   = 6;       
    
    
    /***************** CHAINED HASH LIMITS ****************/
    
    public static int SERVICE_HASHSLOTS                      = 1024;
    public static int HOST_HASHSLOTS                         = 1024;
    public static int COMMAND_HASHSLOTS                      = 256;
    public static int TIMEPERIOD_HASHSLOTS                   = 64;
    public static int CONTACT_HASHSLOTS                      = 128;
    public static int CONTACTGROUP_HASHSLOTS                 = 64;
    public static int HOSTGROUP_HASHSLOTS                    = 128;
    public static int SERVICEGROUP_HASHSLOTS                 = 128;
    public static int HOSTEXTINFO_HASHSLOTS                  = 1024;
    public static int SERVICEEXTINFO_HASHSLOTS               = 1024;
    
    public static int HOSTDEPENDENCY_HASHSLOTS               = 1024;
    public static int SERVICEDEPENDENCY_HASHSLOTS            = 1024;
    public static int HOSTESCALATION_HASHSLOTS               = 1024;
    public static int SERVICEESCALATION_HASHSLOTS            = 1024;

    
    /* TIMERANGE structure */
    public static class timerange implements Serializable {
	
        /** The serialVersionUID */
	private static final long serialVersionUID = 305864174153584057L;

	public long range_start;
        public long range_end;
        
        public long getRange_end() {
	    return range_end;
	}

	public void setRange_end(long range_end) {
	    this.range_end = range_end;
	}

	public long getRange_start() {
	    return range_start;
	}

	public void setRange_start(long range_start) {
	    this.range_start = range_start;
	}

	public String toString() {
           long start_hh = range_start / 3600;
           long start_mm = (range_start - (3600*start_hh))/60;
           long end_hh = range_end / 3600;
           long end_mm = (range_end - (3600*end_hh))/60;
           return String.format("%02d:%02d-%02d:%02d",start_hh, start_mm, end_hh, end_mm );
        }

    }
    
    /* TIMEPERIOD structure */
    public static class timeperiod implements Serializable {
        public String name;
        public String alias;
        public ArrayList<objects_h.timerange> days[] = new ArrayList[7];  // timerange
        
	public String getAlias() {
	    return alias;
	}
	public void setAlias(String alias) {
	    this.alias = alias;
	}
	public ArrayList<objects_h.timerange>[] getDays() {
	    return days;
	}
	public void setDays(ArrayList<objects_h.timerange>[] days) {
	    this.days = days;
	}
	public String getName() {
	    return name;
	}
	public void setName(String name) {
	    this.name = name;
	}
    }
    
    public static class group implements Serializable {
        public String group_name;
        public String alias;

        public String getAlias() {
	    return alias;
	}
	public void setAlias(String alias) {
	    this.alias = alias;
	}
	public String getGroup_name() {
	    return group_name;
	}
	public void setGroup_name(String group_name) {
	    this.group_name = group_name;
	}
	
	@Override
	public int hashCode() {
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + ((alias == null) ? 0 : alias.hashCode());
	    result = PRIME * result + ((group_name == null) ? 0 : group_name.hashCode());
	    return result;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final group other = (group) obj;
	    if (alias == null) {
		if (other.alias != null)
		    return false;
	    } else if (!alias.equals(other.alias))
		return false;
	    if (group_name == null) {
		if (other.group_name != null)
		    return false;
	    } else if (!group_name.equals(other.group_name))
		return false;
	    return true;
	}
        
	
    }

    /* CONTACTGROUPMEMBER structure */
    public static class contactgroupmember implements Serializable {
        public String contact_name;
        
        public contactgroupmember() {
        }
        
        public contactgroupmember(String contact_name) {
	    super();
	    this.contact_name = contact_name;
	}

	public String toString() { return contact_name; }
	
        public String getContact_name() {
	    return contact_name;
	}
	public void setContact_name(String contact_name) {
	    this.contact_name = contact_name;
	}
	
	@Override
	public int hashCode() {
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + ((contact_name == null) ? 0 : contact_name.hashCode());
	    return result;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final contactgroupmember other = (contactgroupmember) obj;
	    if (contact_name == null) {
		if (other.contact_name != null)
		    return false;
	    } else if (!contact_name.equals(other.contact_name))
		return false;
	    return true;
	}
    }

    /* CONTACTGROUP structure */
    public static class contactgroup extends group implements Serializable {
        public List<objects_h.contactgroupmember> members = new ArrayList<objects_h.contactgroupmember>();

	public List<objects_h.contactgroupmember> getMembers() {
	    return members;
	}

	public void setMembers(List<objects_h.contactgroupmember> members) {
	    this.members = members;
	}
        
    }

    /* CONTACTGROUPSMEMBER structure */
    public static class contactgroupsmember implements Serializable {
        public String group_name;
        public String toString() { return group_name; }

        public contactgroupsmember() {}
        
	public contactgroupsmember(String group_name) {
	    super();
	    this.group_name = group_name;
	} 
        
        public String getGroup_name() {
	    return group_name;
	}
	
	public void setGroup_name(String group_name) {
	    this.group_name = group_name;
	}
	
	@Override
	public int hashCode() {
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + ((group_name == null) ? 0 : group_name.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final contactgroupsmember other = (contactgroupsmember) obj;
	    if (group_name == null) {
		if (other.group_name != null)
		    return false;
	    } else if (!group_name.equals(other.group_name))
		return false;
	    return true;
	}
        
    }
    
    /* HOSTSMEMBER structure */
    public static class hostsmember implements Serializable {
        public String host_name;
        public String toString() { return host_name; }
        
        public hostsmember() {}
        
	public hostsmember(String host_name) {
	    super();
	    this.host_name = host_name;
	}

	public String getHost_name() {
	    return host_name;
	}
	public void setHost_name(String host_name) {
	    this.host_name = host_name;
	}

	@Override
	public int hashCode() {
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + ((host_name == null) ? 0 : host_name.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final hostsmember other = (hostsmember) obj;
	    if (host_name == null) {
		if (other.host_name != null)
		    return false;
	    } else if (!host_name.equals(other.host_name))
		return false;
	    return true;
	}
        
	
    }
    
    /* HOST structure */
    public static class host implements Serializable {
        public String name;
        public String alias;
        public String address;
        public List<objects_h.hostsmember> parent_hosts = new ArrayList<objects_h.hostsmember>(); // TODO Single or list - hostsmember
        public String host_check_command;
        public int check_interval;
        public int max_attempts;
        public String event_handler;
        public List<objects_h.contactgroupsmember> contact_groups = new ArrayList<objects_h.contactgroupsmember>(); // TODO Single or list - contactgroupsmember
        public int notification_interval;
        public int  notify_on_down;
        public int  notify_on_unreachable;
        public int notify_on_recovery;
        public int notify_on_flapping;
        public String notification_period;
        public String check_period;
        public int flap_detection_enabled;
        public double low_flap_threshold;
        public double high_flap_threshold;
        public int stalk_on_up;
        public int stalk_on_down;
        public int stalk_on_unreachable;
        public int check_freshness;
        public int freshness_threshold;
        public int process_performance_data;
        public int checks_enabled;
        public int accept_passive_host_checks;
        public int event_handler_enabled;
        public int retain_status_information;
        public int retain_nonstatus_information;
        public int failure_prediction_enabled;
        public String failure_prediction_options;
        public int obsess_over_host;
        public int notifications_enabled;
//      #ifdef NSCORE
        public int problem_has_been_acknowledged;
        public int acknowledgement_type;
        public int check_type;
        public int current_state;
        public int last_state;
        public int last_hard_state;
        public String plugin_output;
        public String perf_data;
        public int state_type;
        public int current_attempt;
        public double  latency;
        public double  execution_time;
        public int check_options;
        public long last_host_notification; //time_t
        public long next_host_notification; //time_t
        public long next_check; //time_t
        public int should_be_scheduled;
        public long last_check; //time_t
        public long last_state_change; //time_t
        public long last_hard_state_change; // time_t
        public long last_time_up; //time_t
        public long last_time_down; // time_t
        public long last_time_unreachable; //time_t
        public int has_been_checked;
        public int is_being_freshened;
        public int notified_on_down;
        public int notified_on_unreachable;
        public int current_notification_number;
        public int no_more_notifications;
        public int check_flapping_recovery_notification;
        public int scheduled_downtime_depth;
        public int pending_flex_downtime;
        public int[] state_history = new int[objects_h.MAX_STATE_HISTORY_ENTRIES];    /* flap detection */
        public int state_history_index;
        public long last_state_history_update; // time_t
        public int is_flapping;
        public long flapping_comment_id;
        public double  percent_state_change;
        public int total_services;
        public long total_service_check_interval;
        public long modified_attributes;
        public int circular_path_checked;
        public int contains_circular_path;
        //      #endif
        
	public int getAccept_passive_host_checks() {
	    return accept_passive_host_checks;
	}
	public void setAccept_passive_host_checks(int accept_passive_host_checks) {
	    this.accept_passive_host_checks = accept_passive_host_checks;
	}
	public int getAcknowledgement_type() {
	    return acknowledgement_type;
	}
	public void setAcknowledgement_type(int acknowledgement_type) {
	    this.acknowledgement_type = acknowledgement_type;
	}
	public String getAddress() {
	    return address;
	}
	public void setAddress(String address) {
	    this.address = address;
	}
	public String getAlias() {
	    return alias;
	}
	public void setAlias(String alias) {
	    this.alias = alias;
	}
	public int getCheck_flapping_recovery_notification() {
	    return check_flapping_recovery_notification;
	}
	public void setCheck_flapping_recovery_notification(
		int check_flapping_recovery_notification) {
	    this.check_flapping_recovery_notification = check_flapping_recovery_notification;
	}
	public int getCheck_freshness() {
	    return check_freshness;
	}
	public void setCheck_freshness(int check_freshness) {
	    this.check_freshness = check_freshness;
	}
	public int getCheck_interval() {
	    return check_interval;
	}
	public void setCheck_interval(int check_interval) {
	    this.check_interval = check_interval;
	}
	public int getCheck_options() {
	    return check_options;
	}
	public void setCheck_options(int check_options) {
	    this.check_options = check_options;
	}
	public String getCheck_period() {
	    return check_period;
	}
	public void setCheck_period(String check_period) {
	    this.check_period = check_period;
	}
	public int getCheck_type() {
	    return check_type;
	}
	public void setCheck_type(int check_type) {
	    this.check_type = check_type;
	}
	public int getChecks_enabled() {
	    return checks_enabled;
	}
	public void setChecks_enabled(int checks_enabled) {
	    this.checks_enabled = checks_enabled;
	}
	public int getCircular_path_checked() {
	    return circular_path_checked;
	}
	public void setCircular_path_checked(int circular_path_checked) {
	    this.circular_path_checked = circular_path_checked;
	}
	public List<objects_h.contactgroupsmember> getContact_groups() {
	    return contact_groups;
	}
	public void setContact_groups(List<objects_h.contactgroupsmember> contact_groups) {
	    this.contact_groups = contact_groups;
	}
	public int getContains_circular_path() {
	    return contains_circular_path;
	}
	public void setContains_circular_path(int contains_circular_path) {
	    this.contains_circular_path = contains_circular_path;
	}
	public int getCurrent_attempt() {
	    return current_attempt;
	}
	public void setCurrent_attempt(int current_attempt) {
	    this.current_attempt = current_attempt;
	}
	public int getCurrent_notification_number() {
	    return current_notification_number;
	}
	public void setCurrent_notification_number(int current_notification_number) {
	    this.current_notification_number = current_notification_number;
	}
	public int getCurrent_state() {
	    return current_state;
	}
	public void setCurrent_state(int current_state) {
	    this.current_state = current_state;
	}
	public String getEvent_handler() {
	    return event_handler;
	}
	public void setEvent_handler(String event_handler) {
	    this.event_handler = event_handler;
	}
	public int getEvent_handler_enabled() {
	    return event_handler_enabled;
	}
	public void setEvent_handler_enabled(int event_handler_enabled) {
	    this.event_handler_enabled = event_handler_enabled;
	}
	public double getExecution_time() {
	    return execution_time;
	}
	public void setExecution_time(double execution_time) {
	    this.execution_time = execution_time;
	}
	public int getFailure_prediction_enabled() {
	    return failure_prediction_enabled;
	}
	public void setFailure_prediction_enabled(int failure_prediction_enabled) {
	    this.failure_prediction_enabled = failure_prediction_enabled;
	}
	public String getFailure_prediction_options() {
	    return failure_prediction_options;
	}
	public void setFailure_prediction_options(String failure_prediction_options) {
	    this.failure_prediction_options = failure_prediction_options;
	}
	public int getFlap_detection_enabled() {
	    return flap_detection_enabled;
	}
	public void setFlap_detection_enabled(int flap_detection_enabled) {
	    this.flap_detection_enabled = flap_detection_enabled;
	}
	public long getFlapping_comment_id() {
	    return flapping_comment_id;
	}
	public void setFlapping_comment_id(long flapping_comment_id) {
	    this.flapping_comment_id = flapping_comment_id;
	}
	public int getFreshness_threshold() {
	    return freshness_threshold;
	}
	public void setFreshness_threshold(int freshness_threshold) {
	    this.freshness_threshold = freshness_threshold;
	}
	public int getHas_been_checked() {
	    return has_been_checked;
	}
	public void setHas_been_checked(int has_been_checked) {
	    this.has_been_checked = has_been_checked;
	}
	public double getHigh_flap_threshold() {
	    return high_flap_threshold;
	}
	public void setHigh_flap_threshold(double high_flap_threshold) {
	    this.high_flap_threshold = high_flap_threshold;
	}
	public String getHost_check_command() {
	    return host_check_command;
	}
	public void setHost_check_command(String host_check_command) {
	    this.host_check_command = host_check_command;
	}
	public int getIs_being_freshened() {
	    return is_being_freshened;
	}
	public void setIs_being_freshened(int is_being_freshened) {
	    this.is_being_freshened = is_being_freshened;
	}
	public int getIs_flapping() {
	    return is_flapping;
	}
	public void setIs_flapping(int is_flapping) {
	    this.is_flapping = is_flapping;
	}
	public long getLast_check() {
	    return last_check;
	}
	public void setLast_check(long last_check) {
	    this.last_check = last_check;
	}
	public int getLast_hard_state() {
	    return last_hard_state;
	}
	public void setLast_hard_state(int last_hard_state) {
	    this.last_hard_state = last_hard_state;
	}
	public long getLast_hard_state_change() {
	    return last_hard_state_change;
	}
	public void setLast_hard_state_change(long last_hard_state_change) {
	    this.last_hard_state_change = last_hard_state_change;
	}
	public long getLast_host_notification() {
	    return last_host_notification;
	}
	public void setLast_host_notification(long last_host_notification) {
	    this.last_host_notification = last_host_notification;
	}
	public int getLast_state() {
	    return last_state;
	}
	public void setLast_state(int last_state) {
	    this.last_state = last_state;
	}
	public long getLast_state_change() {
	    return last_state_change;
	}
	public void setLast_state_change(long last_state_change) {
	    this.last_state_change = last_state_change;
	}
	public long getLast_state_history_update() {
	    return last_state_history_update;
	}
	public void setLast_state_history_update(long last_state_history_update) {
	    this.last_state_history_update = last_state_history_update;
	}
	public long getLast_time_down() {
	    return last_time_down;
	}
	public void setLast_time_down(long last_time_down) {
	    this.last_time_down = last_time_down;
	}
	public long getLast_time_unreachable() {
	    return last_time_unreachable;
	}
	public void setLast_time_unreachable(long last_time_unreachable) {
	    this.last_time_unreachable = last_time_unreachable;
	}
	public long getLast_time_up() {
	    return last_time_up;
	}
	public void setLast_time_up(long last_time_up) {
	    this.last_time_up = last_time_up;
	}
	public double getLatency() {
	    return latency;
	}
	public void setLatency(double latency) {
	    this.latency = latency;
	}
	public double getLow_flap_threshold() {
	    return low_flap_threshold;
	}
	public void setLow_flap_threshold(double low_flap_threshold) {
	    this.low_flap_threshold = low_flap_threshold;
	}
	public int getMax_attempts() {
	    return max_attempts;
	}
	public void setMax_attempts(int max_attempts) {
	    this.max_attempts = max_attempts;
	}
	public long getModified_attributes() {
	    return modified_attributes;
	}
	public void setModified_attributes(long modified_attributes) {
	    this.modified_attributes = modified_attributes;
	}
	public String getName() {
	    return name;
	}
	public void setName(String name) {
	    this.name = name;
	}
	public long getNext_check() {
	    return next_check;
	}
	public void setNext_check(long next_check) {
	    this.next_check = next_check;
	}
	public long getNext_host_notification() {
	    return next_host_notification;
	}
	public void setNext_host_notification(long next_host_notification) {
	    this.next_host_notification = next_host_notification;
	}
	public int getNo_more_notifications() {
	    return no_more_notifications;
	}
	public void setNo_more_notifications(int no_more_notifications) {
	    this.no_more_notifications = no_more_notifications;
	}
	public int getNotification_interval() {
	    return notification_interval;
	}
	public void setNotification_interval(int notification_interval) {
	    this.notification_interval = notification_interval;
	}
	public String getNotification_period() {
	    return notification_period;
	}
	public void setNotification_period(String notification_period) {
	    this.notification_period = notification_period;
	}
	public int getNotifications_enabled() {
	    return notifications_enabled;
	}
	public void setNotifications_enabled(int notifications_enabled) {
	    this.notifications_enabled = notifications_enabled;
	}
	public int getNotified_on_down() {
	    return notified_on_down;
	}
	public void setNotified_on_down(int notified_on_down) {
	    this.notified_on_down = notified_on_down;
	}
	public int getNotified_on_unreachable() {
	    return notified_on_unreachable;
	}
	public void setNotified_on_unreachable(int notified_on_unreachable) {
	    this.notified_on_unreachable = notified_on_unreachable;
	}
	public int getNotify_on_down() {
	    return notify_on_down;
	}
	public void setNotify_on_down(int notify_on_down) {
	    this.notify_on_down = notify_on_down;
	}
	public int getNotify_on_flapping() {
	    return notify_on_flapping;
	}
	public void setNotify_on_flapping(int notify_on_flapping) {
	    this.notify_on_flapping = notify_on_flapping;
	}
	public int getNotify_on_recovery() {
	    return notify_on_recovery;
	}
	public void setNotify_on_recovery(int notify_on_recovery) {
	    this.notify_on_recovery = notify_on_recovery;
	}
	public int getNotify_on_unreachable() {
	    return notify_on_unreachable;
	}
	public void setNotify_on_unreachable(int notify_on_unreachable) {
	    this.notify_on_unreachable = notify_on_unreachable;
	}
	public int getObsess_over_host() {
	    return obsess_over_host;
	}
	public void setObsess_over_host(int obsess_over_host) {
	    this.obsess_over_host = obsess_over_host;
	}
	public List<objects_h.hostsmember> getParent_hosts() {
	    return parent_hosts;
	}
	public void setParent_hosts(List<objects_h.hostsmember> parent_hosts) {
	    this.parent_hosts = parent_hosts;
	}
	public int getPending_flex_downtime() {
	    return pending_flex_downtime;
	}
	public void setPending_flex_downtime(int pending_flex_downtime) {
	    this.pending_flex_downtime = pending_flex_downtime;
	}
	public double getPercent_state_change() {
	    return percent_state_change;
	}
	public void setPercent_state_change(double percent_state_change) {
	    this.percent_state_change = percent_state_change;
	}
	public String getPerf_data() {
	    return perf_data;
	}
	public void setPerf_data(String perf_data) {
	    this.perf_data = perf_data;
	}
	public String getPlugin_output() {
	    return plugin_output;
	}
	public void setPlugin_output(String plugin_output) {
	    this.plugin_output = plugin_output;
	}
	public int getProblem_has_been_acknowledged() {
	    return problem_has_been_acknowledged;
	}
	public void setProblem_has_been_acknowledged(int problem_has_been_acknowledged) {
	    this.problem_has_been_acknowledged = problem_has_been_acknowledged;
	}
	public int getProcess_performance_data() {
	    return process_performance_data;
	}
	public void setProcess_performance_data(int process_performance_data) {
	    this.process_performance_data = process_performance_data;
	}
	public int getRetain_nonstatus_information() {
	    return retain_nonstatus_information;
	}
	public void setRetain_nonstatus_information(int retain_nonstatus_information) {
	    this.retain_nonstatus_information = retain_nonstatus_information;
	}
	public int getRetain_status_information() {
	    return retain_status_information;
	}
	public void setRetain_status_information(int retain_status_information) {
	    this.retain_status_information = retain_status_information;
	}
	public int getScheduled_downtime_depth() {
	    return scheduled_downtime_depth;
	}
	public void setScheduled_downtime_depth(int scheduled_downtime_depth) {
	    this.scheduled_downtime_depth = scheduled_downtime_depth;
	}
	public int getShould_be_scheduled() {
	    return should_be_scheduled;
	}
	public void setShould_be_scheduled(int should_be_scheduled) {
	    this.should_be_scheduled = should_be_scheduled;
	}
	public int getStalk_on_down() {
	    return stalk_on_down;
	}
	public void setStalk_on_down(int stalk_on_down) {
	    this.stalk_on_down = stalk_on_down;
	}
	public int getStalk_on_unreachable() {
	    return stalk_on_unreachable;
	}
	public void setStalk_on_unreachable(int stalk_on_unreachable) {
	    this.stalk_on_unreachable = stalk_on_unreachable;
	}
	public int getStalk_on_up() {
	    return stalk_on_up;
	}
	public void setStalk_on_up(int stalk_on_up) {
	    this.stalk_on_up = stalk_on_up;
	}
	public int[] getState_history() {
	    return state_history;
	}
	public void setState_history(int[] state_history) {
	    this.state_history = state_history;
	}
	public int getState_history_index() {
	    return state_history_index;
	}
	public void setState_history_index(int state_history_index) {
	    this.state_history_index = state_history_index;
	}
	public int getState_type() {
	    return state_type;
	}
	public void setState_type(int state_type) {
	    this.state_type = state_type;
	}
	public long getTotal_service_check_interval() {
	    return total_service_check_interval;
	}
	public void setTotal_service_check_interval(long total_service_check_interval) {
	    this.total_service_check_interval = total_service_check_interval;
	}
	public int getTotal_services() {
	    return total_services;
	}
	public void setTotal_services(int total_services) {
	    this.total_services = total_services;
	}

        
        
    }
    
    
    /* HOSTGROUPMEMBER structure */
    public static class hostgroupmember implements Serializable{
        public String host_name;
        public String toString() { return host_name; }
    }
    
    
    /* HOSTGROUP structure */
    public static class hostgroup implements Serializable{
        public String group_name;
        public String alias;
        public ArrayList<hostgroupmember> members = new ArrayList<hostgroupmember>(); // TODO Single or list- hostgroupmember
    }
    
    
    /* SERVICEGROUPMEMBER structure */
    public static class servicegroupmember implements Serializable{
        public String host_name;
        public String service_description;
        public String toString() { return host_name + "," + service_description; } 
    }
    
    
    /* SERVICEGROUP structure */
    public static class servicegroup implements Serializable{
        public String group_name;
        public String alias;
        public ArrayList<servicegroupmember> members = new ArrayList<servicegroupmember>(); // TODO Single or list -- servicegroupmember
    }
    
    
    /* COMMANDSMEMBER structure */
    public static class commandsmember implements Serializable {
	
        /** The serialVersionUID */
	private static final long serialVersionUID = 2007891535487630807L;
	
	public String command;
        public commandsmember() {}
        public commandsmember(String cmd) { command = cmd; }
        public String toString() { return command; }
	
	public int hashCode() {
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + ((command == null) ? 0 : command.hashCode());
	    return result;
	}
	
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final commandsmember other = (commandsmember) obj;
	    if (command == null) {
		if (other.command != null)
		    return false;
	    } else if (!command.equals(other.command))
		return false;
	    return true;
	}
        
    }
    
    /* CONTACT structure */
    public static class contact implements Serializable {
	
        /** The serialVersionUID */
	private static final long serialVersionUID = 7336268321241662855L;
	
	public String name;
        public String alias;
        public String email;
        public String pager;
        public String[] address = new String[objects_h.MAX_CONTACT_ADDRESSES];
        public List<objects_h.commandsmember> host_notification_commands = new ArrayList<objects_h.commandsmember>(); 
        public List<objects_h.commandsmember> service_notification_commands = new ArrayList<objects_h.commandsmember>(); 
        public int notify_on_service_unknown;
        public int notify_on_service_warning;
        public int notify_on_service_critical;
        public int notify_on_service_recovery;
        public int notify_on_service_flapping;
        public int notify_on_host_down;
        public int notify_on_host_unreachable;
        public int notify_on_host_recovery;
        public int notify_on_host_flapping;
        public String host_notification_period;
        public String service_notification_period;
        
	public String[] getAddress() {
	    return address;
	}
	public void setAddress(String[] address) {
	    this.address = address;
	}
	public String getAlias() {
	    return alias;
	}
	public void setAlias(String alias) {
	    this.alias = alias;
	}
	public String getEmail() {
	    return email;
	}
	public void setEmail(String email) {
	    this.email = email;
	}
	public List<objects_h.commandsmember> getHost_notification_commands() {
	    return host_notification_commands;
	}
	public void setHost_notification_commands(
		List<objects_h.commandsmember> host_notification_commands) {
	    this.host_notification_commands = host_notification_commands;
	}
	public String getHost_notification_period() {
	    return host_notification_period;
	}
	public void setHost_notification_period(String host_notification_period) {
	    this.host_notification_period = host_notification_period;
	}
	public String getName() {
	    return name;
	}
	public void setName(String name) {
	    this.name = name;
	}
	public int getNotify_on_host_down() {
	    return notify_on_host_down;
	}
	public void setNotify_on_host_down(int notify_on_host_down) {
	    this.notify_on_host_down = notify_on_host_down;
	}
	public int getNotify_on_host_flapping() {
	    return notify_on_host_flapping;
	}
	public void setNotify_on_host_flapping(int notify_on_host_flapping) {
	    this.notify_on_host_flapping = notify_on_host_flapping;
	}
	public int getNotify_on_host_recovery() {
	    return notify_on_host_recovery;
	}
	public void setNotify_on_host_recovery(int notify_on_host_recovery) {
	    this.notify_on_host_recovery = notify_on_host_recovery;
	}
	public int getNotify_on_host_unreachable() {
	    return notify_on_host_unreachable;
	}
	public void setNotify_on_host_unreachable(int notify_on_host_unreachable) {
	    this.notify_on_host_unreachable = notify_on_host_unreachable;
	}
	public int getNotify_on_service_critical() {
	    return notify_on_service_critical;
	}
	public void setNotify_on_service_critical(int notify_on_service_critical) {
	    this.notify_on_service_critical = notify_on_service_critical;
	}
	public int getNotify_on_service_flapping() {
	    return notify_on_service_flapping;
	}
	public void setNotify_on_service_flapping(int notify_on_service_flapping) {
	    this.notify_on_service_flapping = notify_on_service_flapping;
	}
	public int getNotify_on_service_recovery() {
	    return notify_on_service_recovery;
	}
	public void setNotify_on_service_recovery(int notify_on_service_recovery) {
	    this.notify_on_service_recovery = notify_on_service_recovery;
	}
	public int getNotify_on_service_unknown() {
	    return notify_on_service_unknown;
	}
	public void setNotify_on_service_unknown(int notify_on_service_unknown) {
	    this.notify_on_service_unknown = notify_on_service_unknown;
	}
	public int getNotify_on_service_warning() {
	    return notify_on_service_warning;
	}
	public void setNotify_on_service_warning(int notify_on_service_warning) {
	    this.notify_on_service_warning = notify_on_service_warning;
	}
	public String getPager() {
	    return pager;
	}
	public void setPager(String pager) {
	    this.pager = pager;
	}
	public List<objects_h.commandsmember> getService_notification_commands() {
	    return service_notification_commands;
	}
	public void setService_notification_commands(
		List<objects_h.commandsmember> service_notification_commands) {
	    this.service_notification_commands = service_notification_commands;
	}
	public String getService_notification_period() {
	    return service_notification_period;
	}
	public void setService_notification_period(String service_notification_period) {
	    this.service_notification_period = service_notification_period;
	}

        
    }
    
    /* SERVICE structure */
    public static class service implements Serializable {
        public String host_name;
        public String description;
        public String service_check_command;
        public String event_handler;
        public int  check_interval;
        public int retry_interval;
        public int  max_attempts;
        public int parallelize;
        public ArrayList<objects_h.contactgroupsmember> contact_groups = new ArrayList<objects_h.contactgroupsmember>(); 
        public int  notification_interval;
        public int notify_on_unknown;
        public int  notify_on_warning;
        public int  notify_on_critical;
        public int  notify_on_recovery;
        public int notify_on_flapping;
        public int stalk_on_ok;
        public int stalk_on_warning;
        public int stalk_on_unknown;
        public int stalk_on_critical;
        public int is_volatile;
        public String notification_period;
        public String check_period;
        public int flap_detection_enabled;
        public double  low_flap_threshold;
        public double  high_flap_threshold;
        public int process_performance_data;
        public int check_freshness;
        public int freshness_threshold;
        public int accept_passive_service_checks;
        public int event_handler_enabled;
        public int  checks_enabled;
        public int retain_status_information;
        public int retain_nonstatus_information;
        public int notifications_enabled;
        public int obsess_over_service;
        public int failure_prediction_enabled;
        public String failure_prediction_options;
//      #ifdef NSCORE
        public int problem_has_been_acknowledged;
        public int acknowledgement_type;
        public int host_problem_at_last_check;
//      #ifdef REMOVED_041403
        public int no_recovery_notification;
//      #endif
        public int check_type;
        public int  current_state;
        public int  last_state;
        public int  last_hard_state;
        public String plugin_output;
        public String perf_data;
        public int state_type;
        public long next_check; // time_t
        public int should_be_scheduled;
        public long last_check; // time_t
        public int  current_attempt;
        public long last_notification; // time_t
        public long next_notification; // time_t
        public int no_more_notifications;
        public int check_flapping_recovery_notification;
        public long last_state_change; // time_t
        public long last_hard_state_change; //time_t
        public long last_time_ok; // time_t
        public long last_time_warning; //time_t
        public long last_time_unknown; // time_t
        public long last_time_critical; // time_t
        public int has_been_checked;
        public int is_being_freshened;
        public int notified_on_unknown;
        public int notified_on_warning;
        public int notified_on_critical;
        public int current_notification_number;
        public double  latency;
        public double  execution_time;
        public int is_executing;
        public int check_options;
        public int scheduled_downtime_depth;
        public int pending_flex_downtime;
        public int[] state_history = new int[objects_h.MAX_STATE_HISTORY_ENTRIES];    /* flap detection */
        public int state_history_index;
        public int is_flapping;
        public long flapping_comment_id; // time_t
        public double  percent_state_change;
        public long modified_attributes; // unsigned
//      #endif
    }
    
    
    /* COMMAND structure */
    public static class command implements Serializable {
        public String name;
        public String command_line;
        
        public command() {}
        
	public String getCommand_line() {
	    return command_line;
	}
	public void setCommand_line(String command_line) {
	    this.command_line = command_line;
	}
	public String getName() {
	    return name;
	}
	public void setName(String name) {
	    this.name = name;
	}
    }
    
    
    /* SERVICE ESCALATION structure */
    public static class serviceescalation implements Serializable{
        public String host_name;
        public String description;
        public int first_notification;
        public int last_notification;
        public int notification_interval;
        public String escalation_period;
        public int escalate_on_recovery;
        public int escalate_on_warning;
        public int escalate_on_unknown;
        public int escalate_on_critical;
        public ArrayList<objects_h.contactgroupsmember> contact_groups = new ArrayList<objects_h.contactgroupsmember>(); // TODO Single or list - contactgroupsmember
//      struct  serviceescalation_struct *next;
//      struct  serviceescalation_struct *nexthash;
    }
    
    
    /* SERVICE DEPENDENCY structure */
    public static class servicedependency implements Serializable { 
        public int dependency_type;
        public String dependent_host_name;
        public String dependent_service_description;
        public String host_name;
        public String service_description;
        public int inherits_parent;
        public int fail_on_ok;
        public int fail_on_warning;
        public int fail_on_unknown;
        public int fail_on_critical;
        public int fail_on_pending;
//      #ifdef NSCORE
        public int     circular_path_checked;
        public int     contains_circular_path;
//      #endif
//      struct servicedependency_struct *next;
//      struct servicedependency_struct *nexthash;
    }
    
    
    /* HOST ESCALATION structure */
    public static class hostescalation implements Serializable{
        public String host_name;
        public int first_notification;
        public int last_notification;
        public int notification_interval;
        public String escalation_period;
        public int escalate_on_recovery;
        public int escalate_on_down;
        public int escalate_on_unreachable;
        public ArrayList<objects_h.contactgroupsmember> contact_groups = new ArrayList<objects_h.contactgroupsmember>(); // TODO Single or list 
    }
    
    
    /* HOST DEPENDENCY structure */
    public static class hostdependency implements Serializable {
        public int dependency_type;
        public String dependent_host_name;
        public String host_name;
        public int inherits_parent;
        public int fail_on_up;
        public int fail_on_down;
        public int fail_on_unreachable;
        public int fail_on_pending;
//      #ifdef NSCORE
        public int circular_path_checked;
        public int contains_circular_path;
//      #endif
    }
    
    
    /* EXTENDED HOST INFO structure */
    public static class hostextinfo implements Serializable{
        public String host_name;
        public String notes;
        public String notes_url;
        public String action_url;
        public String icon_image;
        public String vrml_image;
        public String statusmap_image;
        public String icon_image_alt;
        public int have_2d_coords;
        public int x_2d;
        public int y_2d;
        public int have_3d_coords;
        public double x_3d;
        public double y_3d;
        public double z_3d;
        public int should_be_drawn;
    }
    
    
    /* EXTENDED SERVICE INFO structure */
    public static class serviceextinfo implements Serializable {
        public String host_name;
        public String description;
        public String notes;
        public String notes_url;
        public String action_url;
        public String icon_image;
        public String icon_image_alt;
//      struct serviceextinfo_struct *next;
//      struct serviceextinfo_struct *nexthash;
    }
    
    
    
    /****************** HASH STRUCTURES ********************/
    
    public static class host_cursor  implements Serializable {
        public int host_hashchain_iterator;
        public ArrayList current_host_pointer = new ArrayList(); // TODO Single or list - host
    }
    
}