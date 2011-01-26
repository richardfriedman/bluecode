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

import java.io.File;
import java.io.BufferedReader;


public class cgiutils_h {
    
    /* #undef USE_STATUSMAP */		/* should we compile and use the statusmap CGI? */
    /* #undef USE_STATUSWRL */ 		/* should we compile and use the statuswrl CGI? */
    /* #undef USE_TRENDS */		/* should we compile and use the trends CGI? */
    /* #undef USE_HISTOGRAM */            /* should we compile and use the histogram CGI? */
    
    
    /**************************** CGI REFRESH RATE ******************************/
    
    public static int DEFAULT_REFRESH_RATE	= 60;	/* 60 second refresh rate for CGIs */;
    
    
    /******************************* CGI NAMES **********************************/
    
    public static String STATUS_CGI = 		"status.cgi";
    public static String STATUSMAP_CGI = 		"statusmap.cgi";
    public static String STATUSWORLD_CGI = 	        "statuswrl.cgi";
    public static String COMMAND_CGI = 		"cmd.cgi";
    public static String EXTINFO_CGI = 		"extinfo.cgi";
    public static String SHOWLOG_CGI = 		"showlog.cgi";
    public static String NOTIFICATIONS_CGI = 	"notifications.cgi";
    public static String HISTORY_CGI = 		"history.cgi";
    public static String CONFIG_CGI =               "config.cgi";
    public static String OUTAGES_CGI = 		"outages.cgi";
    public static String TRENDS_CGI = 		"trends.cgi";
    public static String AVAIL_CGI = 		"avail.cgi";
    public static String TAC_CGI = 			"tac.cgi";
    public static String STATUSWML_CGI =            "statuswml.cgi";
    public static String TRACEROUTE_CGI = 		"traceroute.cgi";
    public static String HISTOGRAM_CGI = 		"histogram.cgi";
    public static String CHECKSANITY_CGI = 	   	"checksanity.cgi";
    public static String MINISTATUS_CGI =           "ministatus.cgi";
    public static String SUMMARY_CGI = 	        "summary.cgi";
    
    
    /**************************** STYLE SHEET NAMES ******************************/
    
    public static String COMMON_CSS = 		"common.css";
    
    public static String SHOWLOG_CSS = 		"showlog.css";
    public static String STATUS_CSS = 		"status.css";
    public static String STATUSMAP_CSS = 		"statusmap.css";
    public static String COMMAND_CSS = 		"cmd.css";
    public static String EXTINFO_CSS = 		"extinfo.css";
    public static String NOTIFICATIONS_CSS = 	"notifications.css";
    public static String HISTORY_CSS = 		"history.css";
    public static String CONFIG_CSS = 		"config.css";
    public static String OUTAGES_CSS = 		"outages.css";
    public static String TRENDS_CSS = 		"trends.css";
    public static String AVAIL_CSS = 		"avail.css";
    public static String TAC_CSS = 			"tac.css";
    public static String HISTOGRAM_CSS = 		"histogram.css";
    public static String CHECKSANITY_CSS = 		"checksanity.css";
    public static String MINISTATUS_CSS =           "ministatus.css";
    public static String SUMMARY_CSS =              "summary.css";
    
    
    /********************************* ICONS ************************************/
    
    public static int STATUS_ICON_WIDTH		= 20;
    public static int STATUS_ICON_HEIGHT		= 20;
    
    public static String INFO_ICON =		"info.png";
    public static String INFO_ICON_ALT =			"Informational Message";
    public static String START_ICON =		"start.gif";
    public static String START_ICON_ALT =			"Program Start";
    public static String STOP_ICON =		"stop.gif";
    public static String STOP_ICON_ALT =			"Program End";
    public static String RESTART_ICON =		"restart.gif";
    public static String RESTART_ICON_ALT =		"Program Restart";
    public static String OK_ICON =			"recovery.png";
    public static String OK_ICON_ALT =			"Service Ok";
    public static String CRITICAL_ICON =		"critical.png";
    public static String CRITICAL_ICON_ALT =		"Service Critical";
    public static String WARNING_ICON =		"warning.png";
    public static String WARNING_ICON_ALT =		"Service Warning";
    public static String UNKNOWN_ICON =		"unknown.png";
    public static String UNKNOWN_ICON_ALT =		"Service Unknown";
    public static String NOTIFICATION_ICON =	"notify.gif";
    public static String NOTIFICATION_ICON_ALT =		"Service Notification";
    public static String LOG_ROTATION_ICON =	"logrotate.png";
    public static String LOG_ROTATION_ICON_ALT =		"Log Rotation";
    public static String EXTERNAL_COMMAND_ICON =	"command.png";
    public static String EXTERNAL_COMMAND_ICON_ALT =	"External Command";
    
    public static String STATUS_DETAIL_ICON =		"status2.gif";
    public static String STATUS_OVERVIEW_ICON =		"status.gif";
    public static String STATUSMAP_ICON =                  "status3.gif";
    public static String STATUSWORLD_ICON =                "status4.gif";
    public static String EXTINFO_ICON =                	"extinfo.gif";
    public static String HISTORY_ICON =			"history.gif";
    public static String CONTACTGROUP_ICON =		"contactgroup.gif";
    public static String TRENDS_ICON =			"trends.gif";
    
    public static String DISABLED_ICON =			"disabled.gif";
    public static String ENABLED_ICON =			"enabled.gif";
    public static String PASSIVE_ONLY_ICON =		"passiveonly.gif";
    public static String NOTIFICATIONS_DISABLED_ICON =	"ndisabled.gif";
    public static String ACKNOWLEDGEMENT_ICON =            "ack.gif";
    public static String REMOVE_ACKNOWLEDGEMENT_ICON =     "noack.gif";
    public static String COMMENT_ICON =			"comment.gif";
    public static String DELETE_ICON =			"delete.gif";
    public static String DELAY_ICON =			"delay.gif";
    public static String DOWNTIME_ICON =			"downtime.gif";
    public static String PASSIVE_ICON =			"unknown.png";
    public static String RIGHT_ARROW_ICON =		"right.gif";
    public static String LEFT_ARROW_ICON =			"left.gif";
    public static String UP_ARROW_ICON =			"up.gif";
    public static String DOWN_ARROW_ICON =			"down.gif";
    public static String FLAPPING_ICON =			"flapping.gif";
    public static String SCHEDULED_DOWNTIME_ICON =		"downtime.gif";
    public static String EMPTY_ICON =			"empty.gif";
    
    public static String ACTIVE_ICON =			"active.gif";
    public static String ACTIVE_ICON_ALT =                 "Active Mode";
    public static String STANDBY_ICON =			"standby.gif";
    public static String STANDBY_ICON_ALT =                "Standby Mode";
    
    public static String HOST_DOWN_ICON =			"critical.png";
    public static String HOST_DOWN_ICON_ALT =		"Host Down";
    public static String HOST_UNREACHABLE_ICON =		"critical.png";
    public static String HOST_UNREACHABLE_ICON_ALT =	"Host Unreachable";
    public static String HOST_UP_ICON =			"recovery.png";
    public static String HOST_UP_ICON_ALT =		"Host Up";
    public static String HOST_NOTIFICATION_ICON =		"notify.gif";
    public static String HOST_NOTIFICATION_ICON_ALT =	"Host Notification";
    
    public static String SERVICE_EVENT_ICON =		"serviceevent.gif";
    public static String SERVICE_EVENT_ICON_ALT =		"Service Event Handler";
    public static String HOST_EVENT_ICON =			"hostevent.gif";
    public static String HOST_EVENT_ICON_ALT =		"Host Event Handler";
    
    public static String THERM_OK_IMAGE			="thermok.png";
    public static String THERM_WARNING_IMAGE	=	"thermwarn.png";
    public static String THERM_CRITICAL_IMAGE	=	"thermcrit.png";
    
    public static String CONFIGURATION_ICON =		"config.gif";
    public static String NOTES_ICON =			"notes.gif";
    public static String ACTION_ICON =			"action.gif";
    public static String DETAIL_ICON             =        "detail.gif";
    
    public static String PARENT_TRAVERSAL_ICON =		"parentup.gif";
    
    public static String TAC_DISABLED_ICON =		"tacdisabled.png";
    public static String TAC_ENABLED_ICON =		"tacenabled.png";
    
    public static String ZOOM1_ICON =			"zoom1.gif";
    public static String ZOOM2_ICON =			"zoom2.gif";
    
    public static String CONTEXT_HELP_ICON1	=	"contexthelp1.gif";
    public static String CONTEXT_HELP_ICON2	=	"contexthelp2.gif";
    
    
    
    /************************** PLUGIN RETURN VALUES ****************************/
    
    public static int STATE_OK		= 0;
    public static int STATE_WARNING		= 1;
    public static int STATE_CRITICAL		= 2;
    public static int STATE_UNKNOWN		=3;       /* changed from -1 on 02/24/2001 */;
    
    
    /********************* EXTENDED INFO CGI DISPLAY TYPES  *********************/
    
    public static int DISPLAY_PROCESS_INFO		= 0;
    public static int DISPLAY_HOST_INFO		= 1;
    public static int DISPLAY_SERVICE_INFO		= 2;
    public static int DISPLAY_COMMENTS		= 3;
    public static int DISPLAY_PERFORMANCE		= 4;
    public static int DISPLAY_HOSTGROUP_INFO =		5;
    public static int DISPLAY_DOWNTIME =		6;
    public static int DISPLAY_SCHEDULING_QUEUE =	7;
    public static int DISPLAY_SERVICEGROUP_INFO =       8;
    
    
    /************************ COMMAND CGI COMMAND MODES *************************/
    
    public static int CMDMODE_NONE =            0;
    public static int CMDMODE_REQUEST =         1;
    public static int CMDMODE_COMMIT =          2;
    
    
    
    /******************** HOST AND SERVICE NOTIFICATION TYPES ******************/
    
    public static int NOTIFICATION_ALL		= 0;	/* all service and host notifications */;
    public static int NOTIFICATION_SERVICE_ALL	=1;	/* all types of service notifications */;
    public static int NOTIFICATION_HOST_ALL		=2;	/* all types of host notifications */;
    public static int NOTIFICATION_SERVICE_WARNING =	4;
    public static int NOTIFICATION_SERVICE_UNKNOWN =	8;
    public static int NOTIFICATION_SERVICE_CRITICAL =	16;
    public static int NOTIFICATION_SERVICE_RECOVERY =	32;
    public static int NOTIFICATION_HOST_DOWN =		64;
    public static int NOTIFICATION_HOST_UNREACHABLE =	128;
    public static int NOTIFICATION_HOST_RECOVERY =	256;
    public static int NOTIFICATION_SERVICE_ACK =	512;
    public static int NOTIFICATION_HOST_ACK =		1024;
    public static int NOTIFICATION_SERVICE_FLAP =	2048;
    public static int NOTIFICATION_HOST_FLAP =		4096;
    
    
    /********************** HOST AND SERVICE ALERT TYPES **********************/
    
    public static int HISTORY_ALL			=0;	/* all service and host alert */;
    public static int HISTORY_SERVICE_ALL		=1;	/* all types of service alerts */;
    public static int HISTORY_HOST_ALL		=2;	/* all types of host alerts */;
    public static int HISTORY_SERVICE_WARNING =		4;
    public static int HISTORY_SERVICE_UNKNOWN =		8;
    public static int HISTORY_SERVICE_CRITICAL =	16;
    public static int HISTORY_SERVICE_RECOVERY =	32;
    public static int HISTORY_HOST_DOWN =		64;
    public static int HISTORY_HOST_UNREACHABLE =	128;
    public static int HISTORY_HOST_RECOVERY =		256;
    
    
    /****************************** SORT TYPES  *******************************/
    
    public static int SORT_NONE =			0;
    public static int SORT_ASCENDING =			1;
    public static int SORT_DESCENDING =			2;
    
    
    /***************************** SORT OPTIONS  ******************************/
    
    public static int SORT_NOTHING =			0;
    public static int SORT_HOSTNAME =			1;
    public static int SORT_SERVICENAME =		2;
    public static int SORT_SERVICESTATUS =		3;
    public static int SORT_LASTCHECKTIME =		4;
    public static int SORT_CURRENTATTEMPT =		5;
    public static int SORT_STATEDURATION =		6;
    public static int SORT_NEXTCHECKTIME =		7;
    public static int SORT_HOSTSTATUS =                 8;
    
    
    /****************** HOST AND SERVICE FILTER PROPERTIES  *******************/
    
    public static int HOST_SCHEDULED_DOWNTIME =		1;
    public static int HOST_NO_SCHEDULED_DOWNTIME =	2;
    public static int HOST_STATE_ACKNOWLEDGED =		4;
    public static int HOST_STATE_UNACKNOWLEDGED =	8;
    public static int HOST_CHECKS_DISABLED =		16;
    public static int HOST_CHECKS_ENABLED =		32;
    public static int HOST_EVENT_HANDLER_DISABLED =	64;
    public static int HOST_EVENT_HANDLER_ENABLED =	128;
    public static int HOST_FLAP_DETECTION_DISABLED =	256;
    public static int HOST_FLAP_DETECTION_ENABLED =	512;
    public static int HOST_IS_FLAPPING =		1024;
    public static int HOST_IS_NOT_FLAPPING =		2048;
    public static int HOST_NOTIFICATIONS_DISABLED =	4096;
    public static int HOST_NOTIFICATIONS_ENABLED =	8192;
    public static int HOST_PASSIVE_CHECKS_DISABLED =	16384;
    public static int HOST_PASSIVE_CHECKS_ENABLED =	32768;
    public static int HOST_PASSIVE_CHECK =           	65536;
    public static int HOST_ACTIVE_CHECK =            	131072;
    
    
    public static int SERVICE_SCHEDULED_DOWNTIME =	1;
    public static int SERVICE_NO_SCHEDULED_DOWNTIME =	2;
    public static int SERVICE_STATE_ACKNOWLEDGED =	4;
    public static int SERVICE_STATE_UNACKNOWLEDGED =	8;
    public static int SERVICE_CHECKS_DISABLED =		16;
    public static int SERVICE_CHECKS_ENABLED =		32;
    public static int SERVICE_EVENT_HANDLER_DISABLED =	64;
    public static int SERVICE_EVENT_HANDLER_ENABLED =	128;
    public static int SERVICE_FLAP_DETECTION_ENABLED =	256;
    public static int SERVICE_FLAP_DETECTION_DISABLED =	512;
    public static int SERVICE_IS_FLAPPING =		1024;
    public static int SERVICE_IS_NOT_FLAPPING =		2048;
    public static int SERVICE_NOTIFICATIONS_DISABLED =	4096;
    public static int SERVICE_NOTIFICATIONS_ENABLED =	8192;
    public static int SERVICE_PASSIVE_CHECKS_DISABLED =	16384;
    public static int SERVICE_PASSIVE_CHECKS_ENABLED =	32768;
    public static int SERVICE_PASSIVE_CHECK =           65536;
    public static int SERVICE_ACTIVE_CHECK =            131072;
    
    
    /****************************** SSI TYPES  ********************************/
    
    public static int SSI_HEADER =                      0;
    public static int SSI_FOOTER =                      1;
    
    
    
    /************************ CONTEXT-SENSITIVE HELP  *************************/
    
    public static String CONTEXTHELP_STATUS_DETAIL =	"A1";
    public static String CONTEXTHELP_STATUS_HGOVERVIEW =	"A2";
    public static String CONTEXTHELP_STATUS_HGSUMMARY =	"A3";
    public static String CONTEXTHELP_STATUS_HGGRID =	"A4";
    public static String CONTEXTHELP_STATUS_SVCPROBLEMS =	"A5";
    public static String CONTEXTHELP_STATUS_HOST_DETAIL =  "A6";
    public static String CONTEXTHELP_STATUS_HOSTPROBLEMS = "A7";
    public static String CONTEXTHELP_STATUS_SGOVERVIEW =   "A8";
    public static String CONTEXTHELP_STATUS_SGSUMMARY =    "A9";
    public static String CONTEXTHELP_STATUS_SGGRID =       "A10";
    
    public static String CONTEXTHELP_TAC =			"B1";
    
    public static String CONTEXTHELP_MAP =			"C1";
    
    public static String CONTEXTHELP_LOG =			"D1";
    
    public static String CONTEXTHELP_HISTORY =		"E1";
    
    public static String CONTEXTHELP_NOTIFICATIONS =	"F1";
    
    public static String CONTEXTHELP_TRENDS_MENU1 =	"G1";
    public static String CONTEXTHELP_TRENDS_MENU2	= "G2";
    public static String CONTEXTHELP_TRENDS_MENU3	= "G3";
    public static String CONTEXTHELP_TRENDS_MENU4	= "G4";
    public static String CONTEXTHELP_TRENDS_HOST		= "G5";
    public static String CONTEXTHELP_TRENDS_SERVICE	= "G6";
    
    public static String CONTEXTHELP_AVAIL_MENU1		= "H1";
    public static String CONTEXTHELP_AVAIL_MENU2		= "H2";
    public static String CONTEXTHELP_AVAIL_MENU3		= "H3";
    public static String CONTEXTHELP_AVAIL_MENU4		= "H4";
    public static String CONTEXTHELP_AVAIL_MENU5		= "H5";
    public static String CONTEXTHELP_AVAIL_HOSTGROUP	= "H6";
    public static String CONTEXTHELP_AVAIL_HOST		= "H7";
    public static String CONTEXTHELP_AVAIL_SERVICE	= "H8";
    public static String CONTEXTHELP_AVAIL_SERVICEGROUP	= "H9";
    
    public static String CONTEXTHELP_EXT_HOST		= "I1";
    public static String CONTEXTHELP_EXT_SERVICE		= "I2";
    public static String CONTEXTHELP_EXT_HOSTGROUP	= "I3";
    public static String CONTEXTHELP_EXT_PROCESS		= "I4";
    public static String CONTEXTHELP_EXT_PERFORMANCE	= "I5";
    public static String CONTEXTHELP_EXT_COMMENTS	= "I6";
    public static String CONTEXTHELP_EXT_DOWNTIME	= "I7";
    public static String CONTEXTHELP_EXT_QUEUE		= "I8";
    public static String CONTEXTHELP_EXT_SERVICEGROUP	= "I9";
    
    public static String CONTEXTHELP_CMD_INPUT		= "J1";
    public static String CONTEXTHELP_CMD_COMMIT		= "J2";
    
    public static String CONTEXTHELP_OUTAGES		= "K1";
    
    public static String CONTEXTHELP_CONFIG_MENU			= "L1";
    public static String CONTEXTHELP_CONFIG_HOSTS		= "L2";
    public static String CONTEXTHELP_CONFIG_HOSTDEPENDENCIES	= "L3";
    public static String CONTEXTHELP_CONFIG_HOSTESCALATIONS	= "L4";
    public static String CONTEXTHELP_CONFIG_HOSTGROUPS		= "L5";
    public static String CONTEXTHELP_CONFIG_HOSTGROUPESCALATIONS	= "L6";
    public static String CONTEXTHELP_CONFIG_SERVICES		= "L7";
    public static String CONTEXTHELP_CONFIG_SERVICEDEPENDENCIES	= "L8";
    public static String CONTEXTHELP_CONFIG_SERVICEESCALATIONS	= "L9";
    public static String CONTEXTHELP_CONFIG_CONTACTS		= "L10";
    public static String CONTEXTHELP_CONFIG_CONTACTGROUPS	= "L11";
    public static String CONTEXTHELP_CONFIG_TIMEPERIODS		= "L12";
    public static String CONTEXTHELP_CONFIG_COMMANDS		= "L13";
    public static String CONTEXTHELP_CONFIG_HOSTEXTINFO		= "L14";
    public static String CONTEXTHELP_CONFIG_SERVICEEXTINFO	= "L15";
    public static String CONTEXTHELP_CONFIG_SERVICEGROUPS        = "L16";
    
    public static String CONTEXTHELP_HISTOGRAM_MENU1	= "M1";
    public static String CONTEXTHELP_HISTOGRAM_MENU2	= "M2";
    public static String CONTEXTHELP_HISTOGRAM_MENU3	= "M3";
    public static String CONTEXTHELP_HISTOGRAM_MENU4	= "M4";
    public static String CONTEXTHELP_HISTOGRAM_HOST	= "M5";
    public static String CONTEXTHELP_HISTOGRAM_SERVICE	= "M6";
    
    public static String CONTEXTHELP_SUMMARY_MENU                   = "N1";
    public static String CONTEXTHELP_SUMMARY_RECENT_ALERTS          = "N2";
    public static String CONTEXTHELP_SUMMARY_ALERT_TOTALS           = "N3";
    public static String CONTEXTHELP_SUMMARY_HOSTGROUP_ALERT_TOTALS = "N4";
    public static String CONTEXTHELP_SUMMARY_HOST_ALERT_TOTALS      = "N5";
    public static String CONTEXTHELP_SUMMARY_SERVICE_ALERT_TOTALS   = "N6";
    public static String CONTEXTHELP_SUMMARY_ALERT_PRODUCERS        = "N7";
    public static String CONTEXTHELP_SUMMARY_SERVICEGROUP_ALERT_TOTALS = "N8";
    
    
    /************************** LIFO RETURN CODES  ****************************/
    
    public static int LIFO_OK =			0;
    public static int LIFO_ERROR_MEMORY =	1;
    public static int LIFO_ERROR_FILE =		2;
    public static int LIFO_ERROR_DATA =		3;
    
    
    
    
    
    /*************************** DATA STRUCTURES  *****************************/
    
    /* LIFO data structure */
    public static class lifo {
        String data;
//      struct lifo_struct *next;
    }
    
    
    /* MMAPFILE structure - used for reading files via mmap() */
    public static class mmapfile {
        public String path;
//        public int mode;
//        public int fd;
//        public long file_size;
//        public long current_position;
        public long current_line;
//        public ByteBuffer mmap_buf;
        public File file;
        public BufferedReader reader;
//        public FileChannel fChannel;
    }
    
}