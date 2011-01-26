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

public class downtime_h {
    
    /* SCHEDULED_DOWNTIME_ENTRY structure */
    public static class scheduled_downtime{
        public int type;
        public String host_name;
        public String service_description;
        public long entry_time;
        public long start_time;
        public long end_time;
        public int fixed;
        public long triggered_by;
        public long duration;
        public long downtime_id;
        public String author;
        public String comment;
        
        public long comment_id;
        public int is_in_effect;
        public int start_flex_downtime;
        public int incremented_pending_downtime;
        
//      struct scheduled_downtime_struct *next;
    }
    
}