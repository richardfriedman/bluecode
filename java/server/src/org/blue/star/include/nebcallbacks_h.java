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

public class nebcallbacks_h {
   
   /***** CALLBACK TYPES *****/
   
   /** The total number of NEB Callback Items */
   public static final int  NEBCALLBACK_NUMITEMS                         =31;    /* total number of callback types we have */
   
   /** Reserved Callback number 0 - Not implemented */
   public static final int  NEBCALLBACK_RESERVED0                        =0;     /* reserved for future use */
   /** Reserved Callback number 1 - Not implemented */
   public static final int  NEBCALLBACK_RESERVED1                        =1;
   /** Reserved Callback number 2 - Not implemented */
   public static final int  NEBCALLBACK_RESERVED2                        =2;
   /** Reserved Callback number 3 - Not implemented */
   public static final int  NEBCALLBACK_RESERVED3                        =3;
   /** Reserved Callback number 4 - Not implemented */
   public static final int  NEBCALLBACK_RESERVED4                        =4;
   
   /** Not Currently Implemented By Blue */
   public static final int  NEBCALLBACK_RAW_DATA                         =5;
   /** Not Currently Implemented By Blue */
   public static final int  NEBCALLBACK_NEB_DATA                         =6;
   
   /**
    * Information from the main Blue process. Invoked when starting up, shutting down, restarting or abending */
   public static final int  NEBCALLBACK_PROCESS_DATA                     =7;
   /** For the execution of a timed event */
   public static final int  NEBCALLBACK_TIMED_EVENT_DATA                 =8;
   /** Data being written to the main Blue log */
   public static final int  NEBCALLBACK_LOG_DATA                         =9;
   /** For the execution of System Commands */
   public static final int  NEBCALLBACK_SYSTEM_COMMAND_DATA              =10;
   /** For the execution of Event Handler Commands */
   public static final int  NEBCALLBACK_EVENT_HANDLER_DATA               =11;
   /** For the execution of notification Commands */
   public static final int  NEBCALLBACK_NOTIFICATION_DATA                =12;
   /** For the execution of Service Check Commands */
   public static final int  NEBCALLBACK_SERVICE_CHECK_DATA               =13;
   /** For the execution of Host Check Commands */
   public static final int  NEBCALLBACK_HOST_CHECK_DATA                  =14;
   /** For the addition of a comment */
   public static final int  NEBCALLBACK_COMMENT_DATA                     =15;
   /** For the addition of downtime data */
   public static final int  NEBCALLBACK_DOWNTIME_DATA                    =16;
   /** For the addition of Flapping data */
   public static final int  NEBCALLBACK_FLAPPING_DATA                    =17;
   /** For notification of program-wide status change */
   public static final int  NEBCALLBACK_PROGRAM_STATUS_DATA              =18;
   /** For the notification of host-wide status change */
   public static final int  NEBCALLBACK_HOST_STATUS_DATA                 =19;
   /** For the notification of service-wide status change */
   public static final int  NEBCALLBACK_SERVICE_STATUS_DATA              =20;
   /** For the notification of adaptive program change */
   public static final int  NEBCALLBACK_ADAPTIVE_PROGRAM_DATA            =21;
   /** For the notification of adaptive host change */
   public static final int  NEBCALLBACK_ADAPTIVE_HOST_DATA               =22;
   /** For the notification of adaptive service change */
   public static final int  NEBCALLBACK_ADAPTIVE_SERVICE_DATA            =23;
   /** For the notification of external command processing */
   public static final int  NEBCALLBACK_EXTERNAL_COMMAND_DATA            =24;
   /** For the notification of aggregated status dump */
   public static final int  NEBCALLBACK_AGGREGATED_STATUS_DATA           =25;
   /** For the notification of retention data loading and saving */
   public static final int  NEBCALLBACK_RETENTION_DATA                   =26;
   /** For the notification of contact notification change */
   public static final int  NEBCALLBACK_CONTACT_NOTIFICATION_DATA        =27;
   /** For the notification of contact notification method change */
   public static final int  NEBCALLBACK_CONTACT_NOTIFICATION_METHOD_DATA =28;
   /**For the notification of acknowledgements to problems */
   public static final int  NEBCALLBACK_ACKNOWLEDGEMENT_DATA             =29;
   /** For the notification of state changes */
   public static final int  NEBCALLBACK_STATE_CHANGE_DATA                =30;
   
}
