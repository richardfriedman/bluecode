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


package org.blue.star.plugins;

import java.text.NumberFormat;
import java.util.Formatter;

//#define LOCAL_TIMEOUT_ALARM_HANDLER
//
//#include "common.h"
//#include "utils.h"
//#include <stdarg.h>
//#include <limits.h>
//
//#include <arpa/inet.h>
//
//extern String progname;

public class utils {
   public static final int STRLEN = 64;
   public static final int TXTBLK = 128;
   
   /**
    * max_state(STATE_x, STATE_y)
    * compares STATE_x to  STATE_y and returns result based on the following
    * STATE_UNKNOWN < STATE_OK < STATE_WARNING < STATE_CRITICAL
    *
    * Note that numerically the above does not hold
    **/
   public static int max_state (int a, int b)
   {
      if (a == common_h.STATE_CRITICAL || b == common_h.STATE_CRITICAL)
         return common_h.STATE_CRITICAL;
      else if (a == common_h.STATE_WARNING || b == common_h.STATE_WARNING)
         return common_h.STATE_WARNING;
      else if (a == common_h.STATE_OK || b == common_h.STATE_OK)
         return common_h.STATE_OK;
      else if (a == common_h.STATE_UNKNOWN || b == common_h.STATE_UNKNOWN)
         return common_h.STATE_UNKNOWN;
      else if (a == common_h.STATE_DEPENDENT || b == common_h.STATE_DEPENDENT)
         return common_h.STATE_DEPENDENT;
      else
         return Math.max(a, b);
   }

   public static String formatArgumentError( String programName, String message, String argumentName) { 
      return String.format( "%s: %s - arg %s", programName, argumentName, message);
   }
   
   /**
    * Strip out the revision number.
    */
   public static String clean_revstring (String revstring) {
      
      String plugin_revision = "N/A";
      
      if ( revstring != null ) {
         java.util.regex.Pattern p = java.util.regex.Pattern.compile( "\\$Revision: ([0-9.]*)" );
         java.util.regex.Matcher m = p.matcher ( revstring );
         if ( m.lookingAt() )
            plugin_revision = m.group(1);
      }
      
      return plugin_revision;
   }
   
   public static void print_revision ( String command_name, String revision_string)
   {
      String  plugin_revision = clean_revstring( revision_string );
      
      System.out.printf ("%s (%s %s) %s\n",
            command_name, config_h.PACKAGE, config_h.VERSION, plugin_revision);
   }
   
   public static String state_text (int result)
   {
      switch (result) {
         case common_h.STATE_OK:
            return "OK";
         case common_h.STATE_WARNING:
            return "WARNING";
         case common_h.STATE_CRITICAL:
            return "CRITICAL";
         case common_h.STATE_DEPENDENT:
            return "DEPENDENT";
         default:
            return "UNKNOWN";
      }
   }
   
////void die (int, const char *, ...) __attribute__((noreturn,format(printf, 2, 3)));
// public static void die (int result, String fmt, Object... arguments )
// {
// va_list ap;
// va_start (ap, fmt);
// vprintf (fmt, ap);
// va_end (ap);
// exit (result);
// }
   
// /**
// * This is a timeout handler, triggered by os alarms.  How to handle in java?
// */
// public static void timeout_alarm_handler (int signo)
// {
// if (signo == SIGALRM) {
// printf (_("CRITICAL - Plugin timed out after %d seconds\n"),
// timeout_interval);
// exit (common_h.STATE_CRITICAL);
// }
// }
   
   public static boolean is_numeric (String number) {  
      NumberFormat f = NumberFormat.getInstance();
      try { 
         f.parse( number );
      } catch (Exception e) {
         return false; 
      } 
      
      return true;
      
   }
   
   public static boolean is_positive (String number) {
      try {
      if ( is_numeric (number) && Float.parseFloat(number) > 0.0)
         return true;
      else
         return false;
      } catch (NumberFormatException nfE) {
         return false;
      }      
   }
   
   public static boolean is_negative (String number) {
      try {
      if (is_numeric (number) && Float.parseFloat (number) < 0.0)
         return true;
      else
         return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_nonnegative (String number) {
      try {
      if (is_numeric (number) && Float.parseFloat (number) >= 0.0)
         return true;
      else
         return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_percentage (String number) {
      try {
      int x = (int) Float.parseFloat(number);
      if (is_numeric (number) && x >= 0 && x <= 100)
         return true;
      else
         return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_integer (String number)
   {
      try {
         long n = Long.parseLong(number);
      
      if ( /* errno != ERANGE && */ n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE )
         return true;
      else
         return false;

      } catch (NumberFormatException nfE) { return false; }
   }
   
   public static boolean is_intpos (String number)
   {
      try {
      if (is_integer (number) && Integer.parseInt(number) > 0)
         return true;
      else
         return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_intneg (String number)
   {
      try {
      if (is_integer (number) && Integer.parseInt(number) < 0)
         return true;
      else
         return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_intnonneg (String number)
   {
      try {
         if (is_integer (number) && Integer.parseInt (number) >= 0)
            return true;
         else
            return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_intpercent (String number)
   {
      try {
         int i = Integer.parseInt(number);
         if (is_integer (number) && i >= 0 && i <= 100)
            return true;
         else
            return false;
      } catch (NumberFormatException nfE) {
         return false;
      }
   }
   
   public static boolean is_option (String str)
   {
      
      if ( str == null )
         return false;
      else if ( str.startsWith("-") && !str.startsWith("---") )
         return true;
      else
         return false;
   }
   
   
// public double delta_time (struct timeval tv)
// {
// struct timeval now;
// 
// gettimeofday (&now, NULL);
// return ((double)(now.tv_sec - tv.tv_sec) + (double)(now.tv_usec - tv.tv_usec) / (double)1000000);
// }
   
   
   
// long
// deltime (struct timeval tv)
// {
// struct timeval now;
// gettimeofday (&now, NULL);
// return (now.tv_sec - tv.tv_sec)*1000000 + now.tv_usec - tv.tv_usec;
// }
   
   
   /**
    *
    * Returns a pointer to the next line of a multiline string buffer
    *
    * Given a pointer string, find the text following the next sequence
    * of \r and \n characters. This has the effect of skipping blank
    * lines as well
    *
    * Example:
    *
    * Given text as follows:
    *
    * ==============================
    * This
    * is
    * a
    * 
    * multiline string buffer
    * ==============================
    *
    * int i=0;
    * String str=NULL;
    * String ptr=NULL;
    * str = strscpy(str,"This\nis\r\na\n\nmultiline string buffer\n");
    * ptr = str;
    * while (ptr) {
    *   printf("%d %s",i++,firstword(ptr));
    *   ptr = strnl(ptr);
    * }
    * 
    * Produces the following:
    *
    * 1 This
    * 2 is
    * 3 a
    * 4 multiline
    *
    * NOTE: The 'firstword()' function is conceptual only and does not
    *       exist in this package.
    *
    * NOTE: Although the second 'ptr' variable is not strictly needed in
    *       this example, it is good practice with these utilities. Once
    *       the * pointer is advance in this manner, it may no longer be
    *       handled with * realloc(). So at the end of the code fragment
    *       above, * strscpy(str,"foo") work perfectly fine, but
    *       strscpy(ptr,"foo") will * cause the the program to crash with
    *       a segmentation fault.
    *
    **/
   public String strnl (String str)
   {
      if ( str == null )
         return null;
      
      return str.split( "[\r\n]+", 2 )[0];
   }
   
   
   /**
    *
    * Like strscpy, except only the portion of the source string up to
    * the provided delimiter is copied.
    *
    * Example:
    *
    * str = strpcpy(str,"This is a line of text with no trailing newline","x");
    * printf("%s\n",str);
    *
    * Produces:
    *
    *This is a line of te
    *
    **/
   public String strpcpy (String dest, String src, String str)
   {
      if ( dest == null || src == null )
         return dest;
      
      String[] split = src.split( "[" + str + "]" , 2 );
      
      if ( split == null )
         return src ;
      else
         return split[0];
      
   }
   
   
   
   /******************************************************************************
    *
    * Like strscat, except only the portion of the source string up to
    * the provided delimiter is copied.
    *
    * str = strpcpy(str,"This is a line of text with no trailing newline","x");
    * str = strpcat(str,"This is a line of text with no trailing newline","x");
    * printf("%s\n",str);
    * 
    *This is a line of texThis is a line of tex
    *
    *****************************************************************************/
   
   public String strpcat (String dest, String src, String str)
   {
      if ( dest == null || src == null )
         return dest;
      
      String[] split = src.split( "[" + str + "]" , 2 );
      
      if ( split == null )
         return dest + src ;
      else
         return dest + split[0];
   }
   
   
   /******************************************************************************
    *
    * Print perfdata in a standard format
    *
    ******************************************************************************/
   
   public static String perfdata (String label, long val, String uom, int warnp, long warn, int critp, long crit, int minp, long minv, int maxp, long maxv)
   {
      Formatter f = new Formatter( new StringBuffer());
      
      if ( label.matches("'= "))
         f.format ("'%s'=%ld%s;", label, val, uom);
      else
         f.format ("%s=%ld%s;", label, val, uom);
      
      if (warnp != 0)
         f.format ("%ld;", warn);
      else
         f.format (";");
      
      if (critp != 0)
         f.format ("%ld;", crit);
      else
         f.format (";");
      
      if (minp != 0)
         f.format ("%ld", minv);
      
      if (maxp != 0)
         f.format (";%ld", maxv);
      
      return f.toString();
   }
   
   
   public static String fperfdata (String label, double val, String uom, int warnp, double warn, int critp, double crit, boolean minp, double minv, boolean maxp, double maxv)
   {
      Formatter f = new Formatter( new StringBuffer());
      
      if (   label.matches( "['= ]") )
         f.format ("'%s'=", label);
      else
         f.format ( "%s=", label);
      
      
      f.format ("%f", val);
      f.format ("%s;", uom);
      
      if (warnp != 0)
         f.format ("%f", warn);
      
      f.format (";");
      
      if (critp != 0)
         f.format ("%f", crit);
      
      f.format (";" );
      
      if (minp)
         f.format ("%f", minv);
      
      if (maxp) {
         f.format (";");
         f.format ("%f", maxv);
      }
      
      return f.toString();
   }
   
}
