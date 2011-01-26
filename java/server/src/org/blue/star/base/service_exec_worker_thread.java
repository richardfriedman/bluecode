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

/*
 * This class is used to run a service check.  
 */

package org.blue.star.base;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;

public class service_exec_worker_thread extends Thread
{
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.baseservice_exec_worker_thread");
    private static String cn = "org.blue.base.service_exec_worker_thread";

    private String processed_command;
    private blue_h.service_message svc_msg;
    
    public service_exec_worker_thread (String command, blue_h.service_message message)
    {
        processed_command = command;
        svc_msg = message;
    }

    /**
     * Just runs a SERVICE CHECK. In original base this was executed via 
     * FORK.   We could do a better job hear by making this happen via QUEUE and control the total number.
     * Which is controled by the number of parrallel service checks currently.
     */
    public void run ()
    {
       logger.trace( "entering " + cn + ".run" );

        /* get the command start time */
        blue_h.timeval start_time = new blue_h.timeval();
        
        /* Start by setting the start time/end time and early time out values of the service check */
        svc_msg.start_time=start_time;
        svc_msg.finish_time=start_time;
        svc_msg.early_timeout=false;

        /* Run the service check - the result object contains all we need to know about the return
         * values of the plug-in check.
         */
        utils.system_result result = utils.my_system(processed_command,blue.service_check_timeout);

        svc_msg.finish_time = new blue_h.timeval();
        svc_msg.early_timeout = result.early_timeout;
        
        if (result.output == null || result.output.trim().length() == 0 )
            svc_msg.output = "(No output!)";
        else 
            svc_msg.output = result.output;
        
        /* Set other values of the service message including return code, check_type and whether
         * the exit was ok */
        
        svc_msg.return_code = (result.result == -1)?blue_h.STATE_CRITICAL:result.result;
        svc_msg.check_type = common_h.SERVICE_CHECK_ACTIVE;
        svc_msg.exited_ok = (result.result == -1)?common_h.FALSE:common_h.TRUE;

          /* write check result to message queue */
        utils.write_svc_message(svc_msg);
        
        logger.trace( "exiting " + cn + ".run" );
    }
    
    
}
