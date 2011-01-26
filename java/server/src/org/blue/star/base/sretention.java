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
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.xdata.xrddefault;

//TODO many of these classes should be pluggable with implementations and base abstract classes.
public class sretention 
{
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.sretention");
    private static String cn = "org.blue.base.sretention";
    
    /* save all host and service state information */
    public static int save_state_information(String main_config_file, int autosave)
    {
        logger.trace( "entering " + cn + ".save_state_information");
        
        if( blue.retain_state_information  == common_h.FALSE)
            return common_h.OK;
        
      /* send data to event broker */
      broker.broker_retention_data(broker_h.NEBTYPE_RETENTIONDATA_STARTSAVE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        /********* IMPLEMENTATION-SPECIFIC OUTPUT FUNCTION ********/
        int result = xrddefault.xrddefault_save_state_information(main_config_file);
        
      /* send data to event broker */
      broker.broker_retention_data(broker_h.NEBTYPE_RETENTIONDATA_ENDSAVE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        if(result==common_h.ERROR)
            return result;
        
        if(autosave==common_h.TRUE)
            logger.debug("Auto-save of retention data completed successfully.");
        
        logger.trace( "exiting " + cn + ".save_state_information");
        
        return common_h.OK;
    }
    
    /* reads in initial host and state information */
    public static int read_initial_state_information(String main_config_file)
    {
        logger.trace( "entering " + cn + ".read_initial_state_information");
        
        if(blue.retain_state_information== common_h.FALSE)
            return common_h.OK;
        
      /* send data to event broker */
      broker.broker_retention_data(broker_h.NEBTYPE_RETENTIONDATA_STARTLOAD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        /********* IMPLEMENTATION-SPECIFIC INPUT FUNCTION ********/
        int result= xrddefault.xrddefault_read_state_information(main_config_file);
        
      /* send data to event broker */
      broker.broker_retention_data(broker_h.NEBTYPE_RETENTIONDATA_ENDLOAD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        if(result==common_h.ERROR)
            return common_h.ERROR;
        
        logger.trace( "exiting " + cn + ".read_initial_state_information");
        
        return common_h.OK;
    }
    
}