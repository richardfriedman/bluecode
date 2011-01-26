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

public class config_h {
    
    /***** NAGIOS STUFF *****/
    
    public static String DEFAULT_NAGIOS_USER = "blue";
    public static String DEFAULT_NAGIOS_GROUP = "blue";
    
    /* Event broker integration */
    public static int USE_EVENT_BROKER = 1;
    
    /* Embed a PERL interpreter into Nagios with optional cache for compiled code (contributed by Stephen Davies) */
//  #undef EMBEDDEDPERL
//  #undef THREADEDPERL
    /* 0 = cache, 1 = do not cache */
    public static String DO_CLEAN		= "1";
    
    /* commands used by CGIs */
    public static String TRACEROUTE_COMMAND = "";
//  #undef PING_COMMAND
//  #undef PING_PACKETS_FIRST
    
//  /* Debugging options */
//  /* function entry and exit */
//  /* #undef DEBUG0 */
//  /* general info messages */
//  /* #undef DEBUG1 */
//  /* warning messages */
//  /* #undef DEBUG2 */
//  /* service and host checks, other events */
//  /* #undef DEBUG3 */
//  /* service and host notifications */
//  /* #undef DEBUG4 */
//  /* SQL queries (defunct) */
//  /* #undef DEBUG5 */
//  
    
    /* I/O implementations */
    public static int USE_XSDDEFAULT = 1;
    public static int USE_XCDDEFAULT = 1;
    public static int USE_XRDDEFAULT = 1;
    public static int USE_XODTEMPLATE = 1;
    public static int USE_XPDDEFAULT = 1;
    public static int USE_XDDDEFAULT = 1;
    
    /***** FUNCTION DEFINITIONS *****/
    public static int HAVE_SETENV = 1;
    public static int HAVE_UNSETENV = 1;
    /* public static int HAVE_SOCKET */
    public static int HAVE_STRDUP = 1;
    public static int HAVE_STRSTR = 1;
    public static int HAVE_STRTOUL = 1;
    public static int HAVE_INITGROUPS = 1;
    /* public static int HAVE_GETLOADAVG */
    
    /***** MISC DEFINITIONS *****/
    
    public static int STDC_HEADERS = 1;
//  /* #undef HAVE_TM_ZONE */
    public static int HAVE_TZNAME = 1;
//  /* #undef USE_PROC */
//  #define SOCKET_SIZE_TYPE size_t
//  #define GETGROUPS_T gid_t
//  #define RETSIGTYPE void
    
    public static int RTLD_GLOBAL = 0;
    public static int RTLD_NOW = 0;
    
    
    /***** MARO DEFINITIONS *****/
    
    /* this needs to come after all system include files, so we don't accidentally attempt to redefine it */
//  public long WEXITSTATUS(long stat_val) { return ((long)(stat_val >>> 8); }
//  #endif
//  #ifndef WIFEXITED
//  # define WIFEXITED(stat_val) (((stat_val) & 255) == 0)
//  #endif
//  
    
}