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

package org.blue.star.base;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.xdata.xpddefault;

public class perfdata { 

   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.base.perfdata");
   public static String cn = "org.blue.base.perfdata"; 

    /******************************************************************/
    /************** INITIALIZATION & CLEANUP FUNCTIONS ****************/
    /******************************************************************/
    
    /* initializes performance data */
    public static int initialize_performance_data(String config_file)
    {
        
        xpddefault.xpddefault_initialize_performance_data(config_file);
        
        return common_h.OK;
    }
    
    
    
    /* cleans up performance data */
    public static int cleanup_performance_data(String config_file)
    {
        xpddefault.xpddefault_cleanup_performance_data(config_file);
        
        return common_h.OK;
    }
    
    
    
    /******************************************************************/
    /****************** PERFORMANCE DATA FUNCTIONS ********************/
    /******************************************************************/
    
    
    /* updates service performance data */
    public static int update_service_performance_data(objects_h.service svc)
    {
        
        /* should we be processing performance data for anything? */
        if(blue.process_performance_data==common_h.FALSE)
            return common_h.OK;
        
        /* should we process performance data for this service? */
        if(svc.process_performance_data==common_h.FALSE)
            return common_h.OK;
        
        /* process the performance data! */
        xpddefault.xpddefault_update_service_performance_data(svc);
        
        return common_h.OK;
    }
    
    
    
    /* updates host performance data */
    public static int update_host_performance_data(objects_h.host hst){
        
        /* should we be processing performance data for anything? */
        if( blue.process_performance_data==common_h.FALSE)
            return common_h.OK;
        
        /* should we process performance data for this host? */
        if(hst.process_performance_data==common_h.FALSE)
            return common_h.OK;
        
        /* process the performance data! */
        xpddefault.xpddefault_update_host_performance_data(hst);
        
        return common_h.OK;
    }
}