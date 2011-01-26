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

import java.net.InetAddress;

public class netutils {

   int socket_timeout = common_h.DEFAULT_SOCKET_TIMEOUT; 
   int econn_refuse_state = common_h.STATE_CRITICAL;
   int was_refused = common_h.FALSE;
   int address_family = common_h.AF_UNSPEC;

   public static boolean is_host (String address)
   {
      if ( is_addr (address) /* || is_hostname (address) */)
         return true;

      return false;
   }

   public static boolean is_addr (String address)
   {
      try {
         InetAddress.getByName( address );
         return true;
      } catch (Exception e) {
         System.out.println( e.getMessage() );
         return false;
      }

   }
}
