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

public interface broker_h {
    
/*************** EVENT BROKER OPTIONS *****************/

    public static final int BROKER_NOTHING                  = 0;
    public static final int BROKER_EVERYTHING	 	= 1048575;  /* UPDATED 2.2 */ 

    public static final int BROKER_PROGRAM_STATE            = 1;	/* DONE */
    public static final int BROKER_TIMED_EVENTS             = 2;	/* DONE */
    public static final int BROKER_SERVICE_CHECKS           = 4;	/* DONE */
    public static final int BROKER_HOST_CHECKS              = 8;	/* DONE */
    public static final int BROKER_EVENT_HANDLERS   	= 16;	/* DONE */
    public static final int BROKER_LOGGED_DATA              = 32;	/* DONE */
    public static final int BROKER_NOTIFICATIONS    	= 64;      /* DONE */
    public static final int BROKER_FLAPPING_DATA   	        = 128;	/* DONE */
    public static final int BROKER_COMMENT_DATA         	= 256;	/* DONE */
    public static final int BROKER_DOWNTIME_DATA		= 512;     /* DONE */
    public static final int BROKER_SYSTEM_COMMANDS          = 1024;	/* DONE */
    public static final int BROKER_OCP_DATA                 = 2048;	/* DONE */
    public static final int BROKER_STATUS_DATA              = 4096;    /* DONE */
    public static final int BROKER_ADAPTIVE_DATA            = 8192;    /* DONE */
    public static final int BROKER_EXTERNALCOMMAND_DATA     = 16384;   /* DONE */
    public static final int BROKER_RETENTION_DATA           = 32768;   /* DONE */
    public static final int BROKER_ACKNOWLEDGEMENT_DATA     = 65536;
    public static final int BROKER_STATECHANGE_DATA         = 131072; /* UPDATED 2.2 */
    public static final int BROKER_RESERVED18               = 262144; /* UPDATED 2.2 */
    public static final int BROKER_RESERVED19               = 524288; /* UPDATED 2.2 */


/****** EVENT TYPES ************************/

    public static final int NEBTYPE_NONE                          = 0;

    public static final int NEBTYPE_HELLO                         = 1;
    public static final int NEBTYPE_GOODBYE                       = 2;
    public static final int NEBTYPE_INFO                          = 3;

    public static final int NEBTYPE_PROCESS_START                 = 100;
    public static final int NEBTYPE_PROCESS_DAEMONIZE             = 101;
    public static final int NEBTYPE_PROCESS_RESTART               = 102;
    public static final int NEBTYPE_PROCESS_SHUTDOWN              = 103;
    public static final int NEBTYPE_PROCESS_PRELAUNCH             = 104;   /* before objects are read or verified */
    public static final int NEBTYPE_PROCESS_EVENTLOOPSTART        = 105; /* UPDATED 2.2 */
    public static final int NEBTYPE_PROCESS_EVENTLOOPEND          = 106; /* UPDATED 2.2 */
    
    public static final int NEBTYPE_TIMEDEVENT_ADD                = 200;
    public static final int NEBTYPE_TIMEDEVENT_REMOVE             = 201;
    public static final int NEBTYPE_TIMEDEVENT_EXECUTE            = 202;
    public static final int NEBTYPE_TIMEDEVENT_DELAY              = 203;   /* NOT IMPLEMENTED */
    public static final int NEBTYPE_TIMEDEVENT_SKIP               = 204;   /* NOT IMPLEMENTED */
    public static final int NEBTYPE_TIMEDEVENT_SLEEP              = 205;

    public static final int NEBTYPE_LOG_DATA                      = 300;
    public static final int NEBTYPE_LOG_ROTATION                  = 301;

    public static final int NEBTYPE_SYSTEM_COMMAND_START            = 400; /* UPDATED 2.2 */
    public static final int NEBTYPE_SYSTEM_COMMAND_END              = 401; /* UPDATED 2.2 */
    
    public static final int NEBTYPE_EVENTHANDLER_START             = 500;
    public static final int NEBTYPE_EVENTHANDLER_END      = 501;

    public static final int NEBTYPE_NOTIFICATION_START            = 600;
    public static final int NEBTYPE_NOTIFICATION_END              = 601;
    public static final int  NEBTYPE_CONTACTNOTIFICATION_START        = 602; /* UPDATED 2.2 */
    public static final int  NEBTYPE_CONTACTNOTIFICATION_END          = 603; /* UPDATED 2.2 */
    public static final int  NEBTYPE_CONTACTNOTIFICATIONMETHOD_START  = 604; /* UPDATED 2.2 */
    public static final int  NEBTYPE_CONTACTNOTIFICATIONMETHOD_END    = 605; /* UPDATED 2.2 */
    
    public static final int NEBTYPE_SERVICECHECK_INITIATE         = 700;
    public static final int  NEBTYPE_SERVICECHECK_PROCESSED           = 701;  /* UPDATED 2.2 */
    public static final int  NEBTYPE_SERVICECHECK_RAW_START           = 702;   /* NOT IMPLEMENTED, UPDATED 2.2 */
    public static final int  NEBTYPE_SERVICECHECK_RAW_END             = 703;   /* NOT IMPLEMENTED, UPDATED 2.2 */

    public static final int NEBTYPE_HOSTCHECK_INITIATE            = 800;        /* a check of the route to the host has been initiated */
    public static final int  NEBTYPE_HOSTCHECK_PROCESSED             = 801;   /* the processed/final result of a host check, UPDATED 2.2 */
    public static final int  NEBTYPE_HOSTCHECK_RAW_START              = 802;   /* the start of a "raw" host check, UPDATED 2.2 */
    public static final int  NEBTYPE_HOSTCHECK_RAW_END                = 803;   /* a finished "raw" host check, UPDATED 2.2 */

    public static final int NEBTYPE_COMMENT_ADD                   = 900;
    public static final int NEBTYPE_COMMENT_DELETE                = 901;
    public static final int NEBTYPE_COMMENT_LOAD                  = 902;

    public static final int NEBTYPE_FLAPPING_START                = 1000;
    public static final int NEBTYPE_FLAPPING_STOP                 = 1001;

    public static final int NEBTYPE_DOWNTIME_ADD                  = 1100;
    public static final int NEBTYPE_DOWNTIME_DELETE               = 1101;
    public static final int NEBTYPE_DOWNTIME_LOAD                 = 1102;
    public static final int NEBTYPE_DOWNTIME_START                = 1103;
    public static final int NEBTYPE_DOWNTIME_STOP                 = 1104;

    public static final int NEBTYPE_PROGRAMSTATUS_UPDATE          = 1200;
    public static final int NEBTYPE_HOSTSTATUS_UPDATE             = 1201;
    public static final int NEBTYPE_SERVICESTATUS_UPDATE          = 1202;

    public static final int NEBTYPE_ADAPTIVEPROGRAM_UPDATE        = 1300;
    public static final int NEBTYPE_ADAPTIVEHOST_UPDATE           = 1301;
    public static final int NEBTYPE_ADAPTIVESERVICE_UPDATE        = 1302;

    public static final int NEBTYPE_EXTERNALCOMMAND_START         = 1400;
    public static final int  NEBTYPE_EXTERNALCOMMAND_END              = 1401; /* UPDATED 2.2 */

    public static final int NEBTYPE_AGGREGATEDSTATUS_STARTDUMP    = 1500;
    public static final int NEBTYPE_AGGREGATEDSTATUS_ENDDUMP      = 1501;

    public static final int NEBTYPE_RETENTIONDATA_STARTLOAD       = 1600;
    public static final int NEBTYPE_RETENTIONDATA_ENDLOAD         = 1601;
    public static final int NEBTYPE_RETENTIONDATA_STARTSAVE       = 1602;
    public static final int NEBTYPE_RETENTIONDATA_ENDSAVE         = 1603;

    public static final int  NEBTYPE_ACKNOWLEDGEMENT_ADD              = 1700; /* UPDATED 2.2 */
    public static final int  NEBTYPE_ACKNOWLEDGEMENT_REMOVE           = 1701;   /* NOT IMPLEMENTED, UPDATED 2.2 */
    public static final int  NEBTYPE_ACKNOWLEDGEMENT_LOAD             = 1702;   /* NOT IMPLEMENTED, UPDATED 2.2 */

    public static final int  NEBTYPE_STATECHANGE_START                = 1800;   /* NOT IMPLEMENTED, UPDATED 2.2 */
    public static final int  NEBTYPE_STATECHANGE_END                  = 1801; /* UPDATED 2.2 */


/****** EVENT FLAGS ************************/

    public static final int NEBFLAG_NONE                          = 0;
    public static final int NEBFLAG_PROCESS_INITIATED             = 1;         /* event was initiated by Nagios process */
    public static final int NEBFLAG_USER_INITIATED                = 2;        /* event was initiated by a user request */
    public static final int NEBFLAG_MODULE_INITIATED              = 3;         /* event was initiated by an event broker module */




/****** EVENT ATTRIBUTES *******************/

    public static final int NEBATTR_NONE                          = 0;

    public static final int NEBATTR_SHUTDOWN_NORMAL               = 1;
    public static final int NEBATTR_SHUTDOWN_ABNORMAL             = 2;
    public static final int NEBATTR_RESTART_NORMAL                = 4;
    public static final int NEBATTR_RESTART_ABNORMAL              = 8;

    public static final int NEBATTR_FLAPPING_STOP_NORMAL          = 1;
    public static final int NEBATTR_FLAPPING_STOP_DISABLED        = 2;         /* flapping stopped because flap detection was disabled */

    public static final int NEBATTR_DOWNTIME_STOP_NORMAL          = 1;
    public static final int NEBATTR_DOWNTIME_STOP_CANCELLED       = 2;

}