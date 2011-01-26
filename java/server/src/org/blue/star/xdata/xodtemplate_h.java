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

package org.blue.star.xdata;

// TODO implement equals on all the objects. 

public class xodtemplate_h {

/*********** GENERAL DEFINITIONS ************/

public static int MAX_XODTEMPLATE_INPUT_BUFFER    = 65535;

public static int MAX_XODTEMPLATE_CONTACT_ADDRESSES = 6;

public static final int XODTEMPLATE_NONE                = 0;
public static final int XODTEMPLATE_TIMEPERIOD          = 1;
public static final int XODTEMPLATE_COMMAND             = 2;
public static final int XODTEMPLATE_CONTACT             = 3;
public static final int XODTEMPLATE_CONTACTGROUP        = 4;
public static final int XODTEMPLATE_HOST                = 5;
public static final int XODTEMPLATE_HOSTGROUP           = 6;
public static final int XODTEMPLATE_SERVICE             = 7;
public static final int XODTEMPLATE_SERVICEDEPENDENCY   = 8;
public static final int XODTEMPLATE_HOSTGROUPESCALATION = 9;     /* no longer implemented */
public static final int XODTEMPLATE_SERVICEESCALATION   = 10;
public static final int XODTEMPLATE_HOSTESCALATION      = 11;
public static final int XODTEMPLATE_HOSTDEPENDENCY      = 12;
public static final int XODTEMPLATE_HOSTEXTINFO         = 13;
public static final int XODTEMPLATE_SERVICEEXTINFO      = 14;
public static final int XODTEMPLATE_SERVICEGROUP        = 15;



/********** STRUCTURE DEFINITIONS **********/

/* TIMEPERIOD TEMPLATE STRUCTURE */
public static class xodtemplate_timeperiod {
    public String template;
    public String name;
    public int        _config_file;
    public int        _start_line;
    
    public String timeperiod_name;
    public String alias;
    public String[] timeranges = new String[7];
    
    public int        has_been_resolved;
    public int        register_object;
    // struct xodtemplate_timeperiod_struct *next;        
}


/* COMMAND TEMPLATE STRUCTURE */
public static class xodtemplate_command {
	public String template;
	public String name;
	public int     _config_file;
	public int     _start_line;

	public String command_name;
	public String command_line;

	public int     has_been_resolved;
	public int     register_object;
//	struct xodtemp late_command_struct *next;
        }


/* CONTACT TEMPLATE STRUCTURE */
public static class xodtemplate_contact {
	public String template;
	public String name;
	public int     _config_file;
	public int     _start_line;

	public String contact_name;
	public String alias;
	public String contactgroups;
	public String email;
	public String pager;
	public String[] address = new String[MAX_XODTEMPLATE_CONTACT_ADDRESSES];
	public String host_notification_period;
	public String host_notification_commands;
	public int    notify_on_host_down;
	public int    notify_on_host_unreachable;
	public int    notify_on_host_recovery;
	public int    notify_on_host_flapping;
	public String service_notification_period;
	public String service_notification_commands;
	public int    notify_on_service_unknown;
	public int    notify_on_service_warning;
	public int    notify_on_service_critical;
	public int    notify_on_service_recovery;
	public int    notify_on_service_flapping;

	public int    have_host_notification_options;
	public int    have_service_notification_options;

	public int    has_been_resolved;
	public int    register_object;
//	struct xodtemplate_contact_struct *next;
        }


/* CONTACTGROUP TEMPLATE STRUCTURE */
public static class xodtemplate_contactgroup {
	public String template;
	public String name;
	public int        _config_file;
	public int        _start_line;

	public String contactgroup_name;
	public String alias;
        public String members;

	public int       has_been_resolved;
	public int       register_object;
//	struct xodtemplate_contactgroup_struct *next;
    
        }


/* HOST TEMPLATE STRUCTURE */
public static class xodtemplate_host {
	public String template;
	public String name;
	public int    _config_file;
	public int    _start_line;

	public String host_name;
	public String alias;
	public String address;
	public String parents;
	public String hostgroups;
	public String check_command;
	public String check_period;
	public int   check_interval;
	public int   max_check_attempts;
	public int   active_checks_enabled;
	public int   passive_checks_enabled;
	public int   obsess_over_host;
	public String event_handler;
	public int   event_handler_enabled;
	public int   check_freshness;
	public int   freshness_threshold;
	public float     low_flap_threshold;
	public float     high_flap_threshold;
	public int   flap_detection_enabled;
	public String contact_groups;
	public int   notify_on_down;
	public int   notify_on_unreachable;
	public int   notify_on_recovery;
	public int   notify_on_flapping;
	public int   notifications_enabled;
	public String notification_period;
	public int   notification_interval;
	public int   stalk_on_up;
	public int   stalk_on_down;
	public int   stalk_on_unreachable;
	public int   process_perf_data;
	public int   failure_prediction_enabled;
	public String failure_prediction_options;
	public int   retain_status_information;
	public int   retain_nonstatus_information;

	public int   have_check_interval;
	public int   have_max_check_attempts;
	public int   have_active_checks_enabled;
	public int   have_passive_checks_enabled;
	public int   have_obsess_over_host;
	public int   have_event_handler_enabled;
	public int   have_check_freshness;
	public int   have_freshness_threshold;
	public int   have_low_flap_threshold;
	public int   have_high_flap_threshold;
	public int   have_flap_detection_enabled;
	public int   have_notification_options;
	public int   have_notifications_enabled;
	public int   have_notification_interval;
	public int   have_stalking_options;
	public int   have_process_perf_data;
	public int   have_failure_prediction_enabled;
	public int   have_retain_status_information;
	public int   have_retain_nonstatus_information;

	public int   has_been_resolved;
	public int   register_object;
//	struct xodtemplate_host_struct *next;
        }


/* HOSTGROUP TEMPLATE STRUCTURE */
public static class xodtemplate_hostgroup {
	public String template;
	public String name;
	public int     _config_file;
	public int     _start_line;

	public String hostgroup_name;
	public String alias;
	public String members;

	public int    has_been_resolved;
	public int    register_object;
//	struct xodtemplate_hostgroup_struct *next;
        }


/* SERVICE TEMPLATE STRUCTURE */
public static class xodtemplate_service implements Cloneable
{
    public String template;
	public String name;
	public int      _config_file;
	public int      _start_line;

	public String hostgroup_name;
	public String host_name;
	public String service_description;
	public String servicegroups;
	public String check_command;
	public int      max_check_attempts;
    public int      normal_check_interval;
    public int      retry_check_interval;
    public String check_period;
    public int      active_checks_enabled;
    public int      passive_checks_enabled;
    public int      parallelize_check;
	public int      is_volatile;
	public int      obsess_over_service;
	public String event_handler;
	public int      event_handler_enabled;
	public int      check_freshness;
	public int      freshness_threshold;
	public double     low_flap_threshold;
	public double     high_flap_threshold;
	public int      flap_detection_enabled;
	public int      notify_on_unknown;
	public int      notify_on_warning;
	public int      notify_on_critical;
	public int      notify_on_recovery;
	public int      notify_on_flapping;
	public int      notifications_enabled;
	public String notification_period;
	public int      notification_interval;
	public String contact_groups;
	public int      stalk_on_ok;
	public int      stalk_on_unknown;
	public int      stalk_on_warning;
	public int      stalk_on_critical;
	public int      process_perf_data;
	public int      failure_prediction_enabled;
	public String failure_prediction_options;
	public int      retain_status_information;
	public int      retain_nonstatus_information;
	public int      have_max_check_attempts;
	public int      have_normal_check_interval;
	public int      have_retry_check_interval;
    public int      have_active_checks_enabled;
    public int      have_passive_checks_enabled;
    public int      have_parallelize_check;
	public int      have_is_volatile;
	public int      have_obsess_over_service;
	public int      have_event_handler_enabled;
	public int      have_check_freshness;
	public int      have_freshness_threshold;
	public int      have_low_flap_threshold;
	public int      have_high_flap_threshold;
	public int      have_flap_detection_enabled;
	public int      have_notification_options;
	public int      have_notifications_enabled;
	public int      have_notification_dependencies;
	public int      have_notification_interval;
	public int      have_stalking_options;
	public int      have_process_perf_data;
	public int      have_failure_prediction_enabled;
	public int      have_retain_status_information;
	public int      have_retain_nonstatus_information;
	public int      has_been_resolved;
	public int      register_object;
//	struct xodtemplate_service_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }
        }


/* SERVICEGROUP TEMPLATE STRUCTURE */
public static class xodtemplate_servicegroup {
	public String template;
	public String name;
	public int     _config_file;
	public int     _start_line;

	public String servicegroup_name;
	public String alias;
	public String members;

	public int    has_been_resolved;
	public int    register_object;
//	struct xodtemplate_servicegroup_struct *next;
        }


/* SERVICEDEPENDENCY TEMPLATE STRUCTURE */
public static class xodtemplate_servicedependency implements Cloneable {
	public String template;
        public String name;
	public int      _config_file;
	public int      _start_line;

	public String servicegroup_name;
	public String hostgroup_name;
	public String host_name;
	public String service_description;
	public String dependent_servicegroup_name;
	public String dependent_hostgroup_name;
	public String dependent_host_name;
	public String dependent_service_description;
	public int      inherits_parent;
	public int      fail_notify_on_ok;
	public int      fail_notify_on_unknown;
	public int      fail_notify_on_warning;
	public int      fail_notify_on_critical;
	public int      fail_notify_on_pending;
	public int      fail_execute_on_ok;
	public int      fail_execute_on_unknown;
	public int      fail_execute_on_warning;
	public int      fail_execute_on_critical;
	public int      fail_execute_on_pending;

	public int      have_inherits_parent;
	public int      have_notification_dependency_options;
	public int      have_execution_dependency_options;

	public int      has_been_resolved;
	public int      register_object;
//	struct xodtemplate_servicedependency_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }
        }


/* SERVICEESCALATION TEMPLATE STRUCTURE */
public static class xodtemplate_serviceescalation implements Cloneable{
	public String template;
	public String name;
	public int     _config_file;
	public int     _start_line;

	public String servicegroup_name;
	public String hostgroup_name;
	public String host_name;
	public String service_description;
	public int    first_notification;
	public int    last_notification;
	public int    notification_interval;
	public String escalation_period;
	public int    escalate_on_warning;
	public int    escalate_on_unknown;
	public int    escalate_on_critical;
	public int    escalate_on_recovery;
	public String contact_groups;

	public int    have_first_notification;
	public int    have_last_notification;
	public int    have_notification_interval;
	public int    have_escalation_options;

	public int    has_been_resolved;
	public int    register_object;
//	struct xodtemplate_serviceescalation_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }
        }


/* HOSTDEPENDENCY TEMPLATE STRUCTURE */
public static class xodtemplate_hostdependency implements Cloneable{
	public String template;
        public String name;
	public int  _config_file;
	public int  _start_line;

	public String hostgroup_name;
	public String dependent_hostgroup_name;
	public String host_name;
	public String dependent_host_name;
	public int inherits_parent;
	public int fail_notify_on_up;
	public int fail_notify_on_down;
	public int fail_notify_on_unreachable;
	public int fail_notify_on_pending;
	public int fail_execute_on_up;
	public int fail_execute_on_down;
	public int fail_execute_on_unreachable;
	public int fail_execute_on_pending;

	public int have_inherits_parent;
	public int have_notification_dependency_options;
	public int have_execution_dependency_options;

	public int has_been_resolved;
	public int register_object;
//	struct xodtemplate_hostdependency_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }

        }

/* HOSTESCALATION TEMPLATE STRUCTURE */
public static class xodtemplate_hostescalation implements Cloneable {
	public String template;
	public String name;
	public int      _config_file;
	public int      _start_line;

	public String hostgroup_name;
	public String host_name;
	public int     first_notification;
	public int     last_notification;
	public int     notification_interval;
	public String escalation_period;
	public int     escalate_on_down;
	public int     escalate_on_unreachable;
	public int     escalate_on_recovery;
	public String contact_groups;

	public int     have_first_notification;
	public int     have_last_notification;
	public int     have_notification_interval;
    public int       have_escalation_options;

    public int       has_been_resolved;
	public int       register_object;
//	struct xodtemplate_hostescalation_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }
        }


/* HOSTEXTINFO TEMPLATE STRUCTURE */
public static class xodtemplate_hostextinfo implements Cloneable {
	public String template;
	public String name;
	public int        _config_file;
	public int        _start_line;

	public String host_name;
	public String hostgroup_name;
	public String notes;
	public String notes_url;
	public String action_url;
	public String icon_image;
	public String icon_image_alt;
	public String vrml_image;
	public String statusmap_image;
	
	public int        x_2d;
    public int        y_2d;
	public double     x_3d;
    public double     y_3d;
    public double     z_3d;
	
    public int        have_2d_coords;
    public int        have_3d_coords;

    public int        has_been_resolved;
    public int        register_object;
//	struct xodtemplate_hostextinfo_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }
    }


/* SERVICEEXTINFO TEMPLATE STRUCTURE */
public static class xodtemplate_serviceextinfo implements Cloneable {
	public String template;
	public String name;
	public int        _config_file;
	public int        _start_line;

	public String host_name;
	public String hostgroup_name;
	public String service_description;
	public String notes;
	public String notes_url;
	public String action_url;
    public String icon_image;
    public String icon_image_alt;

    public int        has_been_resolved;
	public int        register_object;
//	struct xodtemplate_serviceextinfo_struct *next;
    public Object clone() { try { return super.clone(); } catch (CloneNotSupportedException cnsE) { return null; } }
    }


/* CONTACT LIST STRUCTURE */
public static class xodtemplate_contactlist{
    public String contact_name;
//  struct xodtemplate_contactlist_struct *next;
    public xodtemplate_contactlist() { }
    public xodtemplate_contactlist( String contact_name ) {
        this.contact_name = contact_name;
    }
    public boolean equals(Object oth) {
        if (this == oth) return true;
        if (oth == null) return false;
        if (oth.getClass() != getClass()) return false;
        
        xodtemplate_contactlist other = (xodtemplate_contactlist) oth;
        if (this.contact_name == null && other.contact_name != null ) return false;
        else if (!this.contact_name.equals(other.contact_name)) return false; 
        
        return true;
    }
    
   public int hashCode()
      {
         final int PRIME = 1000003;
         int result = 0;
         if (contact_name != null)
         {
            result = PRIME * result + contact_name.hashCode();
         }
   
         return result;
      }

}


/* HOST LIST STRUCTURE */
public static class xodtemplate_hostlist{
    public 	String host_name;
//	struct xodtemplate_hostlist_struct *next;
    public xodtemplate_hostlist() { }
    public xodtemplate_hostlist( String host_name ) {
        this.host_name = host_name;
    }
   public boolean equals(Object oth)
      {
         if (this == oth)
         {
            return true;
         }
   
         if (oth == null)
         {
            return false;
         }
   
         if (oth.getClass() != getClass())
         {
            return false;
         }
   
         xodtemplate_hostlist other = (xodtemplate_hostlist) oth;
         if (this.host_name == null)
         {
            if (other.host_name != null)
            {
               return false;
            }
         }
         else
         {
            if (!this.host_name.equals(other.host_name))
            {
               return false;
            }
         }
   
         return true;
      }
   public int hashCode()
      {
         final int PRIME = 1000003;
         int result = 0;
         if (host_name != null)
         {
            result = PRIME * result + host_name.hashCode();
         }
   
         return result;
      }

        }


/* SERVICE LIST STRUCTURE */
public static class xodtemplate_servicelist{
	public String host_name;
	public String service_description;
//	struct xodtemplate_servicelist_struct *next;
   public boolean equals(Object oth)
      {
         if (this == oth)
         {
            return true;
         }
   
         if (oth == null)
         {
            return false;
         }
   
         if (oth.getClass() != getClass())
         {
            return false;
         }
   
         xodtemplate_servicelist other = (xodtemplate_servicelist) oth;
         if (this.host_name == null)
         {
            if (other.host_name != null)
            {
               return false;
            }
         }
         else
         {
            if (!this.host_name.equals(other.host_name))
            {
               return false;
            }
         }
         if (this.service_description == null)
         {
            if (other.service_description != null)
            {
               return false;
            }
         }
         else
         {
            if (!this.service_description.equals(other.service_description))
            {
               return false;
            }
         }
   
         return true;
      }
   public int hashCode()
      {
         final int PRIME = 1000003;
         int result = 0;
         if (host_name != null)
         {
            result = PRIME * result + host_name.hashCode();
         }
         if (service_description != null)
         {
            result = PRIME * result + service_description.hashCode();
         }
   
         return result;
      }
        }



/***** CHAINED HASH DATA STRUCTURES ******/

public static class xodtemplate_service_cursor {
	public int xodtemplate_service_iterator;
//	xodtemplate_service *current_xodtemplate_service;
        }



}