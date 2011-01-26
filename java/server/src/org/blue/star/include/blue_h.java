
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

import java.io.BufferedReader;
import java.io.Serializable;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class blue_h {
   
   public static final int MAX_COMMAND_ARGUMENTS			= 32;	/* maximum number of $ARGx$ macros */
   public static final int MAX_USER_MACROS				= 256;	/* maximum number of $USERx$ macros */
   
   public static final int MAX_STATE_LENGTH			= 32;	/* length definitions used in macros */
   public static final int MAX_STATETYPE_LENGTH			=24;
   public static final int MAX_CHECKTYPE_LENGTH         		=8;
   public static final int MAX_NOTIFICATIONTYPE_LENGTH		=32;
   public static final int MAX_NOTIFICATIONNUMBER_LENGTH		=8;
   public static final int  MAX_ATTEMPT_LENGTH			=8;
   public static final int  MAX_TOTALS_LENGTH			=8;
   public static final int  MAX_EXECUTIONTIME_LENGTH		=10;
   public static final int  MAX_LATENCY_LENGTH			=10;
   public static final int  MAX_DURATION_LENGTH			=17;
   public static final int  MAX_DOWNTIME_LENGTH			=3;
   public static final int  MAX_STATEID_LENGTH			=2;
   public static final int  MAX_PERCENTCHANGE_LENGTH           	=8;
   
   public static String MACRO_ENV_VAR_PREFIX = "NAGIOS_";
   
   public static final int  MACRO_X_COUNT				=99;	/* size of macro_x[] array */
   
   public static final int  MACRO_HOSTNAME				=0;
   public static final int  MACRO_HOSTALIAS				=1;
   public static final int  MACRO_HOSTADDRESS			=2;
   public static final int  MACRO_SERVICEDESC			=3;
   public static final int  MACRO_SERVICESTATE			=4;
   public static final int  MACRO_SERVICESTATEID            =        5;
   public static final int  MACRO_SERVICEATTEMPT			=6;
   public static final int  MACRO_LONGDATETIME			=7;
   public static final int  MACRO_SHORTDATETIME			=8;
   public static final int  MACRO_DATE				=9;
   public static final int  MACRO_TIME				=10;
   public static final int  MACRO_TIMET				=11;
   public static final int  MACRO_LASTHOSTCHECK			=12;
   public static final int  MACRO_LASTSERVICECHECK			=13;
   public static final int  MACRO_LASTHOSTSTATECHANGE		=14;
   public static final int  MACRO_LASTSERVICESTATECHANGE		=15;
   public static final int  MACRO_HOSTOUTPUT			=16;
   public static final int  MACRO_SERVICEOUTPUT			=17;
   public static final int  MACRO_HOSTPERFDATA			=18;
   public static final int  MACRO_SERVICEPERFDATA			=19;
   public static final int  MACRO_CONTACTNAME			=20;
   public static final int  MACRO_CONTACTALIAS			=21;
   public static final int  MACRO_CONTACTEMAIL			=22;
   public static final int  MACRO_CONTACTPAGER			  =23;
   public static final int  MACRO_ADMINEMAIL			      =24;
   public static final int  MACRO_ADMINPAGER			      =25;
   public static final int  MACRO_HOSTSTATE				  =26;
   public static final int  MACRO_HOSTSTATEID              =27;
   public static final int  MACRO_HOSTATTEMPT			  =28;
   public static final int  MACRO_NOTIFICATIONTYPE		  =29;
   public static final int  MACRO_NOTIFICATIONNUMBER		  =30;
   public static final int  MACRO_HOSTEXECUTIONTIME		  =31;
   public static final int  MACRO_SERVICEEXECUTIONTIME	  =32;
   public static final int  MACRO_HOSTLATENCY              =33;
   public static final int  MACRO_SERVICELATENCY			  =34;
   public static final int  MACRO_HOSTDURATION			  =35;
   public static final int  MACRO_SERVICEDURATION		  =36;
   public static final int  MACRO_HOSTDURATIONSEC		  =37;
   public static final int  MACRO_SERVICEDURATIONSEC		  =38;
   public static final int  MACRO_HOSTDOWNTIME			  =39;
   public static final int  MACRO_SERVICEDOWNTIME		  =40;
   public static final int  MACRO_HOSTSTATETYPE			  =41;
   public static final int  MACRO_SERVICESTATETYPE		  =42;
   public static final int  MACRO_HOSTPERCENTCHANGE		=43;
   public static final int  MACRO_SERVICEPERCENTCHANGE	=	44;
   public static final int  MACRO_HOSTGROUPNAME			=45;
   public static final int  MACRO_HOSTGROUPALIAS			=46;
   public static final int  MACRO_SERVICEGROUPNAME			=47;
   public static final int  MACRO_SERVICEGROUPALIAS			=48;
   public static final int  MACRO_HOSTACKAUTHOR                     =49;
   public static final int  MACRO_HOSTACKCOMMENT                    =50;
   public static final int  MACRO_SERVICEACKAUTHOR                  =51;
   public static final int  MACRO_SERVICEACKCOMMENT                 =52;
   public static final int  MACRO_LASTSERVICEOK                     =53;
   public static final int  MACRO_LASTSERVICEWARNING                =54;
   public static final int  MACRO_LASTSERVICEUNKNOWN                =55;
   public static final int  MACRO_LASTSERVICECRITICAL               =56;
   public static final int  MACRO_LASTHOSTUP                        =57;
   public static final int  MACRO_LASTHOSTDOWN                      =58;
   public static final int  MACRO_LASTHOSTUNREACHABLE               =59;
   public static final int  MACRO_SERVICECHECKCOMMAND		=60;
   public static final int  MACRO_HOSTCHECKCOMMAND			=61;
   public static final int  MACRO_MAINCONFIGFILE			=62;
   public static final int  MACRO_STATUSDATAFILE			=63;
   public static final int  MACRO_COMMENTDATAFILE			=64;
   public static final int  MACRO_DOWNTIMEDATAFILE			=65;
   public static final int  MACRO_RETENTIONDATAFILE			=66;
   public static final int  MACRO_OBJECTCACHEFILE			=67;
   public static final int  MACRO_TEMPFILE				=68;
   public static final int  MACRO_LOGFILE				=69;
   public static final int  MACRO_RESOURCEFILE			=70;
   public static final int  MACRO_COMMANDFILE			=71;
   public static final int  MACRO_HOSTPERFDATAFILE			=72;
   public static final int  MACRO_SERVICEPERFDATAFILE		=73;
   public static final int  MACRO_HOSTACTIONURL			=74;
   public static final int  MACRO_HOSTNOTESURL			=75;
   public static final int  MACRO_HOSTNOTES				=76;
   public static final int  MACRO_SERVICEACTIONURL			=77;
   public static final int  MACRO_SERVICENOTESURL			=78;
   public static final int  MACRO_SERVICENOTES			=79;
   public static final int  MACRO_TOTALHOSTSUP			=80;
   public static final int  MACRO_TOTALHOSTSDOWN			=81;
   public static final int  MACRO_TOTALHOSTSUNREACHABLE		=82;
   public static final int  MACRO_TOTALHOSTSDOWNUNHANDLED		=83;
   public static final int  MACRO_TOTALHOSTSUNREACHABLEUNHANDLED	=84;
   public static final int  MACRO_TOTALHOSTPROBLEMS			=85;
   public static final int  MACRO_TOTALHOSTPROBLEMSUNHANDLED	=86;
   public static final int  MACRO_TOTALSERVICESOK			=87;
   public static final int  MACRO_TOTALSERVICESWARNING		=88;
   public static final int  MACRO_TOTALSERVICESCRITICAL		=89;
   public static final int  MACRO_TOTALSERVICESUNKNOWN		=90;
   public static final int  MACRO_TOTALSERVICESWARNINGUNHANDLED	=91;
   public static final int  MACRO_TOTALSERVICESCRITICALUNHANDLED	=92;
   public static final int  MACRO_TOTALSERVICESUNKNOWNUNHANDLED	=93;
   public static final int  MACRO_TOTALSERVICEPROBLEMS		=94;
   public static final int  MACRO_TOTALSERVICEPROBLEMSUNHANDLED	=95;
   public static final int  MACRO_PROCESSSTARTTIME			=96;
   public static final int  MACRO_HOSTCHECKTYPE			=97;
   public static final int  MACRO_SERVICECHECKTYPE			=98;
      
   
   
   public static final int  DEFAULT_LOG_LEVEL			=1;	/* log all events to main log file */
   public static final int  DEFAULT_USE_SYSLOG			=1;	/* log events to syslog? 1=yes, 0=no */
   public static final int  DEFAULT_SYSLOG_LEVEL			=2;	/* log only severe events to syslog */
   
   public static final int  DEFAULT_NOTIFICATION_LOGGING		=1;	/* log notification events? 1=yes, 0=no */
   
   public static double DEFAULT_INTER_CHECK_DELAY		=5.0;	/* seconds between initial service check scheduling */
   public static final int  DEFAULT_INTERLEAVE_FACTOR      	=1;       /* default interleave to use when scheduling checks */
   public static double DEFAULT_SLEEP_TIME      		   =0.5;    	/* seconds between event run checks */
   public static final int  DEFAULT_INTERVAL_LENGTH 		   =60;    	/* seconds per interval unit for check scheduling */
   public static final int  DEFAULT_RETRY_INTERVAL  		=30;	/* services are retried in 30 seconds if they're not OK */
   public static final int  DEFAULT_COMMAND_CHECK_INTERVAL		=-1;	/* interval to check for external commands (default = as often as possible) */
   public static final int  DEFAULT_SERVICE_REAPER_INTERVAL		=10;	/* interval in seconds to reap service check results */
   public static final int  DEFAULT_MAX_REAPER_TIME                 =30;      /* maximum number of seconds to spend reaping service checks before we break out for a while */
   public static final int  DEFAULT_MAX_PARALLEL_SERVICE_CHECKS 	=0;	/* maximum number of service checks we can have running at any given time (0=unlimited) */
   public static final int  DEFAULT_RETENTION_UPDATE_INTERVAL	=60;	/* minutes between auto-save of retention data */
   public static final int  DEFAULT_RETENTION_SCHEDULING_HORIZON    =900;     /* max seconds between program restarts that we will preserve scheduling information */
   public static final int  DEFAULT_STATUS_UPDATE_INTERVAL		=60;	/* seconds between aggregated status data updates */
   public static final int  DEFAULT_FRESHNESS_CHECK_INTERVAL        =60;      /* seconds between service result freshness checks */
   public static final int  DEFAULT_AUTO_RESCHEDULING_INTERVAL      =30;      /* seconds between host and service check rescheduling events */
   public static final int  DEFAULT_AUTO_RESCHEDULING_WINDOW        =180;     /* window of time (in seconds) for which we should reschedule host and service checks */
   
   public static final int  DEFAULT_NOTIFICATION_TIMEOUT		=30;	/* max time in seconds to wait for notification commands to complete */
   public static final int  DEFAULT_EVENT_HANDLER_TIMEOUT		=30;	/* max time in seconds to wait for event handler commands to complete */
   public static final int  DEFAULT_HOST_CHECK_TIMEOUT		=30;	/* max time in seconds to wait for host check commands to complete */
   public static final int  DEFAULT_SERVICE_CHECK_TIMEOUT		=60;	/* max time in seconds to wait for service check commands to complete */
   public static final int  DEFAULT_OCSP_TIMEOUT			=15;	/* max time in seconds to wait for obsessive compulsive processing commands to complete */
   public static final int  DEFAULT_OCHP_TIMEOUT			=15;	/* max time in seconds to wait for obsessive compulsive processing commands to complete */
   public static final int  DEFAULT_PERFDATA_TIMEOUT                =5;       /* max time in seconds to wait for performance data commands to complete */
   public static final int  DEFAULT_TIME_CHANGE_THRESHOLD		=900;	/* compensate for time changes of more than 15 minutes */
   
   public static final int  DEFAULT_LOG_HOST_RETRIES		=0;	/* don't log host retries */
   public static final int  DEFAULT_LOG_SERVICE_RETRIES		=0;	/* don't log service retries */
   public static final int  DEFAULT_LOG_EVENT_HANDLERS		=1;	/* log event handlers */
   public static final int  DEFAULT_LOG_INITIAL_STATES		=0;	/* don't log initial service and host states */
   public static final int  DEFAULT_LOG_EXTERNAL_COMMANDS		=1;	/* log external commands */
   public static final int  DEFAULT_LOG_PASSIVE_CHECKS		=1;	/* log passive service checks */
   
   public static final int  DEFAULT_AGGRESSIVE_HOST_CHECKING	=0;	/* don't use "aggressive" host checking */
   public static final int  DEFAULT_CHECK_EXTERNAL_COMMANDS		=0; 	/* don't check for external commands */
   public static final int  DEFAULT_CHECK_ORPHANED_SERVICES		=1;	/* don't check for orphaned services */
   public static final int  DEFAULT_ENABLE_FLAP_DETECTION           = 0;       /* don't enable flap detection */
   public static final int  DEFAULT_PROCESS_PERFORMANCE_DATA        = 0;       /* don't process performance data */
   public static final int  DEFAULT_CHECK_SERVICE_FRESHNESS         =1;       /* check service result freshness */
   public static final int  DEFAULT_CHECK_HOST_FRESHNESS            =0;       /* don't check host result freshness */
   public static final int  DEFAULT_AUTO_RESCHEDULE_CHECKS          =0;       /* don't auto-reschedule host and service checks */
   
   public static double DEFAULT_LOW_SERVICE_FLAP_THRESHOLD	=20.0;	/* low threshold for detection of service flapping */
   public static double DEFAULT_HIGH_SERVICE_FLAP_THRESHOLD	=30.0;	/* high threshold for detection of service flapping */
   public static double DEFAULT_LOW_HOST_FLAP_THRESHOLD		=20.0;	/* low threshold for detection of host flapping */
   public static double DEFAULT_HIGH_HOST_FLAP_THRESHOLD	=30.0;	/* high threshold for detection of host flapping */
   
   public static final int DEFAULT_HOST_CHECK_SPREAD		=30;	/* max minutes to schedule all initial host checks */
   public static final int DEFAULT_SERVICE_CHECK_SPREAD		=30;	/* max minutes to schedule all initial service checks */
   
   
   
   /******************* LOGGING TYPES ********************/
   
   public static final int NSLOG_RUNTIME_ERROR		=1;
   public static final int NSLOG_RUNTIME_WARNING		=2;
   
   public static final int NSLOG_VERIFICATION_ERROR	=4;
   public static final int NSLOG_VERIFICATION_WARNING	=8;
   
   public static final int NSLOG_CONFIG_ERROR		=16;
   public static final int NSLOG_CONFIG_WARNING		=32;
   
   public static final int NSLOG_PROCESS_INFO		=64;
   public static final int NSLOG_EVENT_HANDLER		=128;
   /*    public static final int NSLOG_NOTIFICATION		=256;*/	/* NOT USED ANYMORE - CAN BE REUSED */
   public static final int NSLOG_EXTERNAL_COMMAND		=512;
   
   public static final int NSLOG_HOST_UP      		=1024;
   public static final int NSLOG_HOST_DOWN			=2048;
   public static final int NSLOG_HOST_UNREACHABLE		=4096;
   
   public static final int NSLOG_SERVICE_OK		=8192;
   public static final int NSLOG_SERVICE_UNKNOWN		=16384;
   public static final int NSLOG_SERVICE_WARNING		=32768;
   public static final int NSLOG_SERVICE_CRITICAL		=65536;
   
   public static final int NSLOG_PASSIVE_CHECK		=131072;
   
   public static final int NSLOG_INFO_MESSAGE		=262144;
   
   public static final int NSLOG_HOST_NOTIFICATION		=524288;
   public static final int NSLOG_SERVICE_NOTIFICATION	=1048576;
   
   
   /******************** HOST STATUS *********************/
   
   public static final int HOST_UP				=0;
   public static final int HOST_DOWN			=1;
   public static final int HOST_UNREACHABLE		=2;	
   
   
   /******************* STATE LOGGING TYPES **************/
   
   public static final int INITIAL_STATES                  =1;
   public static final int CURRENT_STATES                  =2;
   
   
   /************ SERVICE DEPENDENCY VALUES ***************/
   
   public static final int DEPENDENCIES_OK			=0;
   public static final int DEPENDENCIES_FAILED		=1;
   
   
   /*********** ROUTE CHECK PROPAGATION TYPES ************/
   
   public static final int PROPAGATE_TO_PARENT_HOSTS	=1;
   public static final int PROPAGATE_TO_CHILD_HOSTS	=2;
   
   
   /****************** SERVICE STATES ********************/
   
   public static final int STATE_OK			=0;
   public static final int STATE_WARNING			=1;
   public static final int STATE_CRITICAL			=2;
   public static final int STATE_UNKNOWN			=3;       /* changed from -1 on 02/24/2001 */
   
   
   /****************** FLAPPING TYPES ********************/
   
   public static final int HOST_FLAPPING                   =0;
   public static final int SERVICE_FLAPPING                =1;
   
   
   /**************** NOTIFICATION TYPES ******************/
   
   public static final int HOST_NOTIFICATION               =0;
   public static final int SERVICE_NOTIFICATION            =1;
   
   
   /************* NOTIFICATION REASON TYPES ***************/
   
   public static final int NOTIFICATION_NORMAL             =0;
   public static final int NOTIFICATION_ACKNOWLEDGEMENT    =1;
   public static final int NOTIFICATION_FLAPPINGSTART      =2;
   public static final int NOTIFICATION_FLAPPINGSTOP       =3;
   
   
   /**************** EVENT HANDLER TYPES *****************/
   
   public static final int  HOST_EVENTHANDLER               =0; /* UPDATED 2.2 */
   public static final int  SERVICE_EVENTHANDLER            =1; /* UPDATED 2.2 */
   public static final int  GLOBAL_HOST_EVENTHANDLER        =2; /* UPDATED 2.2 */
   public static final int  GLOBAL_SERVICE_EVENTHANDLER     =3; /* UPDATED 2.2 */
   
   
   /***************** STATE CHANGE TYPES *****************/
   
   public static final int  HOST_STATECHANGE                =0; /* UPDATED 2.2 */
   public static final int  SERVICE_STATECHANGE             =1; /* UPDATED 2.2 */
   
   /******************* EVENT TYPES **********************/
   
   public static final int EVENT_SERVICE_CHECK		=0;	/* active service check */
   public static final int EVENT_COMMAND_CHECK		=1;	/* external command check */
   public static final int EVENT_LOG_ROTATION		=2;	/* log file rotation */
   public static final int EVENT_PROGRAM_SHUTDOWN		=3;	/* program shutdown */
   public static final int EVENT_PROGRAM_RESTART		=4;	/* program restart */
   public static final int EVENT_SERVICE_REAPER		=5;	/* reaps results from service checks */
   public static final int EVENT_ORPHAN_CHECK		=6;	/* checks for orphaned service checks */
   public static final int EVENT_RETENTION_SAVE		=7;	/* save (dump) retention data */
   public static final int EVENT_STATUS_SAVE		=8;	/* save (dump) status data */
   public static final int EVENT_SCHEDULED_DOWNTIME	=9;	/* scheduled host or service downtime */
   public static final int EVENT_SFRESHNESS_CHECK          =10;      /* checks service result "freshness" */
   public static final int EVENT_EXPIRE_DOWNTIME		=11;      /* checks for (and removes) expired scheduled downtime */
   public static final int EVENT_HOST_CHECK                =12;      /* active host check */
   public static final int EVENT_HFRESHNESS_CHECK          =13;      /* checks host result "freshness" */
   public static final int EVENT_RESCHEDULE_CHECKS		=14;      /* adjust scheduling of host and service checks */
   public static final int EVENT_EXPIRE_COMMENT            = 15;      /* removes expired comments */
   public static final int EVENT_SLEEP                    = 98;      /* asynchronous sleep event that occurs when event queues are empty */
   public static final int EVENT_USER_FUNCTION             =99;      /* USER-defined function (modules) */
   
   
   /******* INTER-CHECK DELAY CALCULATION TYPES **********/
   
   public static final int ICD_NONE			=0;	/* no inter-check delay */
   public static final int ICD_DUMB			=1;	/* dumb delay of 1 second */
   public static final int ICD_SMART			=2;	/* smart delay */
   public static final int ICD_USER			=3;       /* user-specified delay */
   
   
   /******* INTERLEAVE FACTOR CALCULATION TYPES **********/
   
   public static final int ILF_USER			=0;	/* user-specified interleave factor */
   public static final int ILF_SMART			=1;	/* smart interleave */
   
   
   /************** SERVICE CHECK OPTIONS *****************/
   
   public static final int CHECK_OPTION_NONE		=0;	/* no check options */
   public static final int CHECK_OPTION_FORCE_EXECUTION	=1;	/* force execution of a service check (ignores disabled services, invalid timeperiods) */
   
   
   /************ SCHEDULED DOWNTIME TYPES ****************/
   
   public static final int ACTIVE_DOWNTIME                 =0;       /* active downtime - currently in effect */
   public static final int PENDING_DOWNTIME                =1 ;      /* pending downtime - scheduled for the future */
   
   
   /************* MACRO CLEANING OPTIONS *****************/
   
   public static final int STRIP_ILLEGAL_MACRO_CHARS       =1;
   public static final int ESCAPE_MACRO_CHARS              =2;
   public static final int URL_ENCODE_MACRO_CHARS		=4;
   
   /* slots in circular buffers - changed 2.7 */
   public static final int DEFAULT_EXTERNAL_COMMAND_BUFFER_SLOTS    = 4096;
   public static final int DEFAULT_CHECK_RESULT_BUFFER_SLOTS        = 4096;

   /* slots in circular buffers */
   public static final int COMMAND_BUFFER_SLOTS                 = 4096;
   public static final int SERVICE_BUFFER_SLOTS                 = 4096;

   /* worker threads */
   public static final int TOTAL_WORKER_THREADS              =2;
   
   public static final int COMMAND_WORKER_THREAD		  =0;
   public static final int SERVICE_WORKER_THREAD  		  =1;
   
   /****************** DATA STRUCTURES *******************/
   
   /* TIMED_EVENT structure */
   public static class timed_event {
      public int event_type;
      public long run_time;
      public int recurring;
      public long event_interval;
      public int compensate_for_time_change;
      public timed_event_timing_func timing_func;
      public Object event_data;
      public Object event_args;
//    struct timed_event_struct *next;
   }
   
   public static interface timed_event_timing_func {
      public long get_time();
   }
   
   /* NOTIFY_LIST structure */
   public static class notification {
      public objects_h.contact contact;
//    struct notify_list_struct *next;
   }
   
   
   /* SERVICE_MESSAGE structure */
   public static class service_message implements Serializable
   {
	  private static final long serialVersionUID = 1L;
	  public String host_name;        /* host name */
      public String description;   /* service description */
      public int return_code;                /* plugin return code */
      public int exited_ok;                  /* did the plugin check return okay? */
      public int check_type;                 /* was this an active or passive service check? */
      public int parallelized;                               /* was this check run in parallel? */
      public timeval start_time;          /* time the service check was initiated */
      public timeval finish_time;         /* time the service check was completed */
      public boolean early_timeout;                              /* did the service check timeout? */
      public String output;       /* plugin output */
   }
   
   public static class timeval implements Serializable
   {
	  private static final long serialVersionUID = 1L;
	  public long time;
      public long tv_sec;
      public long tv_usec;
      
      public timeval( ) {
         time = System.currentTimeMillis();
         tv_sec = time/1000;
         tv_usec = (time % 1000);
      }        
      
      public timeval( long sec, long usec ) {
         tv_sec = sec;
         tv_usec = usec;
      }
      
      public void reset() {
         time = System.currentTimeMillis();
         tv_sec = time/1000;
         tv_usec = (time % 1000);
      }
      
   }
   
   public static class timespec implements Serializable 
   {
	  private static final long serialVersionUID = 1L;
	  public long tv_sec;
      public long tv_nsec;
            
      public timespec()
      {}
   }
   
   public static class sched_info {
      
      /* SCHED_INFO structure */
      public int total_services;
      public int total_scheduled_services;
      public int total_hosts;
      public int total_scheduled_hosts;
      public double average_services_per_host;
      public double average_scheduled_services_per_host;
      public long service_check_interval_total;
      public long host_check_interval_total;
      public double average_service_check_interval;
      public double average_host_check_interval;
      public double average_service_inter_check_delay;
      public double average_host_inter_check_delay;
      public double service_inter_check_delay;
      public double host_inter_check_delay;
      public int service_interleave_factor;
      public int max_service_check_spread;
      public int max_host_check_spread;
      public long first_service_check;
      public long last_service_check;
      public long first_host_check;
      public long last_host_check;
   }
   
   
   /* PASSIVE_CHECK_RESULT structure */
   public static class passive_check_result{
      public String host_name;
      public String svc_description;
      public int return_code;
      public String output;
      public long check_time;
//    struct passive_check_result_struct *next;
   }
   
   /* CIRCULAR_BUFFER structure - used by worker threads */
   public static class circular_buffer {
      public Object buffer_lock = new Object();
      public ArrayBlockingQueue buffer;
      public int     high;       /* highest number of items that has ever been stored in buffer */
      
      public circular_buffer( int size ) {
         buffer = new ArrayBlockingQueue( size );
      }
   }
   
   
   /* MMAPFILE structure - used for reading files via mmap() */
   public static class mmapfile {
      public String path;
//    public int mode;
//    public int fd;
//    public long file_size;
//    public long current_position;
      public long current_line;
//    public FileDescriptor fd;
      public ReadableByteChannel fc;
//    public MappedByteBuffer mmap_buf;
//    public CharBuffer char_buf;
      public FileLock lock;
      public BufferedReader reader;
   }
   
}

