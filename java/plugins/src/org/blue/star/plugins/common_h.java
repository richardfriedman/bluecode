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

public class common_h {
   
   public static final int OK = 0;
   public static final int ERROR = -1;
   
   public static final int TRUE                 = 1;
   public static final int FALSE                = 0;
   
   public static final int STATE_OK             = 0;
   public static final int STATE_WARNING        = 1;
   public static final int STATE_CRITICAL       = 2;
   public static final int STATE_UNKNOWN        = 3;
   public static final int STATE_DEPENDENT      = 4;
   
   public static final int DEFAULT_SOCKET_TIMEOUT   = 1000;	 /* timeout after 1 seconds */
   public static final int MAX_INPUT_BUFFER         = 1024;     /* max size of most buffers we use */
   public static final int MAX_HOST_ADDESS_LENGTH   = 256;	 /* max size of a host address */
   
   public static final int AF_INET = 0;
   public static final int AF_INET6 = 1;
   public static final int AF_UNSPEC = 2;
   
   public static final String SF_BASE_NAME = "base.xml";
   
   public static final String SF_HISTORY_PREFIX = "blue_sf_schema_";
}
