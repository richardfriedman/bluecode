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


public class comments_h {
    
    /**************************** COMMENT SOURCES ******************************/
    public static int COMMENTSOURCE_INTERNAL  = 0;
    public static int COMMENTSOURCE_EXTERNAL  = 1;
    
    
    
    /***************************** COMMENT TYPES *******************************/
    public static int HOST_COMMENT			    = 1;
    public static int SERVICE_COMMENT			= 2;
    
    
    /****************************** ENTRY TYPES ********************************/
    public static final int USER_COMMENT                    = 1;
    public static final int DOWNTIME_COMMENT                = 2;
    public static final int FLAPPING_COMMENT                = 3;
    public static final int ACKNOWLEDGEMENT_COMMENT         = 4;
    
    
    /*************************** CHAINED HASH LIMITS ***************************/
    public static int COMMENT_HASHSLOTS      =1024;
    
    /**************************** DATA STRUCTURES ******************************/
    
    /* COMMENT structure */
    public static class comment {
        public int 	comment_type;
        public int     entry_type;
        public long comment_id;
        public int     source;
        public int     persistent;
        public long entry_time;
        public int     expires;
        public long expire_time;
        public String host_name;
        public String service_description;
        public String author;
        public String comment_data;
//      struct 	comment_struct *next;
//      struct 	comment_struct *nexthash;
    }
    
}