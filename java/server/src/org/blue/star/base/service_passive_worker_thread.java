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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;

public class service_passive_worker_thread extends Thread
{
    
    /** Logger instance */
    private static Logger logger = org.apache.log4j.LogManager.getLogger("org.blue.base.service_passive_worker_thread");
    private static String cn = "org.blue.base.service_result_worker_thread";
    
    private ArrayList<blue_h.passive_check_result> passive_check_list;
    
    public service_passive_worker_thread( ArrayList<blue_h.passive_check_result> list ) { 
        passive_check_list = list;
    }
    
    public void run () {

        
              /* write all service checks to the IPC pipe for later processing by the grandparent */
              for ( blue_h.passive_check_result temp_pcr : (ArrayList<blue_h.passive_check_result>)  passive_check_list ) {
                  blue_h.service_message svc_msg = new blue_h.service_message();
                  svc_msg.host_name = temp_pcr.host_name;
                  svc_msg.description = temp_pcr.svc_description;
                  svc_msg.output = temp_pcr.output;
                  svc_msg.return_code=temp_pcr.return_code;
                  svc_msg.parallelized=common_h.FALSE;
                  svc_msg.exited_ok=common_h.TRUE;
                  svc_msg.check_type=common_h.SERVICE_CHECK_PASSIVE;
                  svc_msg.start_time.tv_sec=temp_pcr.check_time;
                  svc_msg.start_time.tv_usec=0;
                  svc_msg.finish_time=svc_msg.start_time;
    
                  /* write the service check results... */
                  utils.write_svc_message(svc_msg);
                              }
                  
    
    passive_check_list.clear();
    //
//          exit(common_h.OK);
//              }
    }
    
}
