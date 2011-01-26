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

import java.io.Serializable;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class utils_h {
   
// void die (int, const char *, ...) __attribute__((noreturn,format(printf, 2, 3)));
   
   /* Handle timeouts */
   public static int timeout_interval = common_h.DEFAULT_SOCKET_TIMEOUT;
   
   public static long start_time, end_time;
   
   /* Generalized timer that will do milliseconds if available */
   public static class timeval implements Serializable 
   {
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
   
   /* The idea here is that, although not every plugin will use all of these, 
    most will or should.  Therefore, for consistency, these very common 
    options should have only these meanings throughout the overall suite */
   public static Options getStandardOptions( ) {
      Options options = new Options();
      addStandardOptions( options );
      return options;
   }
   
   
   // Rob 23/01/07 - Tidied this a little. I feel that individual plugins should add the c & w
   // parameters as their value is not consistent across plugins. Thoughts on this?
   
   public static void addStandardOptions( Options options )
   {
      options.addOption( "n", "nohtml", false, "" );
      options.addOption( "L", "link", false, "show HTML in the plugin output (obsoleted by urlize)" );
      options.addOption( "V", "version", false, "Print version information");
      options.addOption( "v", "verbose", false, "Increase the information displayed");
      options.addOption( "h", "help", false, "Print detailed help screen");
      Option t = new Option( "t", "timeout", true, "Milli-Seconds before connection times out (default: "+utils_h.timeout_interval+")");
      t.setArgName("Timeout");
      options.addOption(t);
      //options.addOption( "c", "critical", true, "Specificy value which cause CRITICAL exit value");
      //options.addOption( "w", "warning", true, "Spcificy value which cause WARNING exit value");
      Option H = new Option( "H", "hostname", true, "Specify hostname we are acting upon");
      H.setArgName("Hostname");
      options.addOption( H );
   }
   
}
