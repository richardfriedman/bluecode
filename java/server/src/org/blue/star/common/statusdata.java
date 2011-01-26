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

package org.blue.star.common;

/*********** COMMON HEADER FILES ***********/
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;

import org.blue.star.base.blue;
import org.blue.star.base.broker;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;
import org.blue.star.xdata.xsddefault;

/**** IMPLEMENTATION SPECIFIC HEADER FILES ****/

public class statusdata
{
    
    public static ArrayList hoststatus_list = new ArrayList(); // hoststatus
    public static ArrayList servicestatus_list = new ArrayList(); // servicestatus

    public static HashMap hoststatus_hashlist = new HashMap(); //hoststatus      
    public static HashMap servicestatus_hashlist = new HashMap(); // servicestatus   **

/******************************************************************/
/****************** TOP-LEVEL OUTPUT FUNCTIONS ********************/
/******************************************************************/

    /* initializes status data at program start */
    public static int initialize_status_data(String config_file){
        /**** IMPLEMENTATION-SPECIFIC CALLS ****/
        // TODO I think the intent was to make this area pluggable via IFDEFs USE_XSDDEFAULT
        int result = xsddefault.xsddefault_initialize_status_data(config_file);
        
        return result;
    }


    /* update all status data (aggregated dump) */
    public static int update_all_status_data(){
        
       /* send data to event broker */
       broker.broker_aggregated_status_data(broker_h.NEBTYPE_AGGREGATEDSTATUS_STARTDUMP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        /**** IMPLEMENTATION-SPECIFIC CALLS ****/
        int result = xsddefault.xsddefault_save_status_data();
        
        /* send data to event broker */
        broker.broker_aggregated_status_data(broker_h.NEBTYPE_AGGREGATEDSTATUS_ENDDUMP,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        if(result!= common_h.OK)
            return common_h.ERROR;
        
        return common_h.OK;
    }


    /* cleans up status data before program termination */
    public static int cleanup_status_data( String config_file,int delete_status_data){
        int result= xsddefault.xsddefault_cleanup_status_data(config_file,delete_status_data);
        return result;
    }



    /* updates program status info */
    public static int update_program_status(int aggregated_dump){
        
       /* send data to event broker (non-aggregated dumps only) */
       if(aggregated_dump==common_h.FALSE)
          broker.broker_program_status(broker_h.NEBTYPE_PROGRAMSTATUS_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,null);
        
        /* currently a noop if aggregated updates is TRUE */
        
        /* update all status data if we're not aggregating updates */
        if( blue.aggregate_status_updates==common_h.FALSE)
            update_all_status_data();
        
        return common_h.OK;
    }



    /* updates host status info */
    public static int update_host_status(objects_h.host hst,int aggregated_dump){
        
       /* send data to event broker (non-aggregated dumps only) */
       if(aggregated_dump==common_h.FALSE)
          broker.broker_host_status(broker_h.NEBTYPE_HOSTSTATUS_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,hst,null);
        
        /* currently a noop if aggregated updates is TRUE */
        
        /* update all status data if we're not aggregating updates */
        if( blue.aggregate_status_updates== common_h.FALSE)
            update_all_status_data();
        
        return common_h.OK;
    }



    /* updates service status info */
    public static int update_service_status(objects_h.service svc,int aggregated_dump){
        
       /* send data to event broker (non-aggregated dumps only) */
       if(aggregated_dump==common_h.FALSE)
          broker.broker_service_status(broker_h.NEBTYPE_SERVICESTATUS_UPDATE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,svc,null);
        
        /* currently a noop if aggregated updates is TRUE */
        
        /* update all status data if we're not aggregating updates */
        if( blue.aggregate_status_updates== common_h.FALSE)
            update_all_status_data();
        
        return common_h.OK;
    }


/******************************************************************/
/******************* TOP-LEVEL INPUT FUNCTIONS ********************/
/******************************************************************/


    /* reads in all status data */
    public static int read_status_data( String config_file,int options){
        
        int result;
        
        result=xsddefault.xsddefault_read_status_data(config_file,options);

//       /* TODO what is this ifdef about. #ifdef USE_XSDDB */
//        result=xsddefault.xsddb_read_status_data(config_file,options);
        
        return result;
    }



/******************************************************************/
/****************** CHAINED HASH FUNCTIONS ************************/
/******************************************************************/
    
    /* adds hoststatus to hash list in memory */
    public static int add_hoststatus_to_hashlist(statusdata_h.hoststatus new_hoststatus){
        
        if ( hoststatus_hashlist.put( new_hoststatus.host_name, new_hoststatus ) == null )
            return 1;
        else 
            /* Already Exists */
            return 0;
    }
    

    
    /* adds hoststatus to hash list in memory */
    public static int add_servicestatus_to_hashlist(statusdata_h.servicestatus new_servicestatus){
        
        if ( servicestatus_hashlist.put( new_servicestatus.host_name + "." + new_servicestatus.description, new_servicestatus ) == null )
            return 1;
        else 
            /* Already Exists */
            return 0;
    }
    
/******************************************************************/
/********************** ADDITION FUNCTIONS ************************/
/******************************************************************/


    /* adds a host status entry to the list in memory */
    public static int add_host_status(statusdata_h.hoststatus new_hoststatus){
        
        /* make sure we have what we need */
        if(new_hoststatus==null ||  new_hoststatus.host_name==null)
            return common_h.ERROR;
        
        /* massage host status a bit */
        switch(new_hoststatus.status){
        case 0:
            new_hoststatus.status = statusdata_h.HOST_UP;
            break;
        case 1:
            new_hoststatus.status = statusdata_h.HOST_DOWN;
            break;
        case 2:
            new_hoststatus.status= statusdata_h.HOST_UNREACHABLE;
            break;
        default:
            new_hoststatus.status= statusdata_h.HOST_UP;
        break;
        }
        if(new_hoststatus.has_been_checked==common_h.FALSE){
            new_hoststatus.status= statusdata_h.HOST_PENDING;
            if(new_hoststatus.should_be_scheduled==common_h.TRUE){
                new_hoststatus.plugin_output="Host check scheduled for " + new Date( new_hoststatus.next_check * 1000 ).toString();
            }
            else
                new_hoststatus.plugin_output="Host has not been checked yet";
        }
        
        
        /* add new hoststatus to hoststatus chained hash list */
        if( add_hoststatus_to_hashlist(new_hoststatus) == 0 )
            return common_h.ERROR;
        
        /* object cache file is already sorted, so just add new items to end of list */
        hoststatus_list.add( new_hoststatus );
        
        return common_h.OK;
    }


    /* adds a service status entry to the list in memory */
    public static int add_service_status(statusdata_h.servicestatus new_svcstatus){
        
        /* make sure we have what we need */
        if(new_svcstatus==null || new_svcstatus.host_name==null || new_svcstatus.description==null)
            return common_h.ERROR;
        
        
        /* massage service status a bit */
        if( new_svcstatus != null){
            switch(new_svcstatus.status){
            case 0:
                new_svcstatus.status=statusdata_h.SERVICE_OK;
                break;
            case 1:
                new_svcstatus.status=statusdata_h.SERVICE_WARNING;
                break;
            case 2:
                new_svcstatus.status=statusdata_h.SERVICE_CRITICAL;
                break;
            case 3:
                new_svcstatus.status=statusdata_h.SERVICE_UNKNOWN;
                break;
            default:
                new_svcstatus.status=statusdata_h.SERVICE_OK;
            break;
            }
            if(new_svcstatus.has_been_checked==common_h.FALSE){
                new_svcstatus.status=statusdata_h.SERVICE_PENDING;
                if(new_svcstatus.should_be_scheduled== common_h.TRUE){
                    new_svcstatus.plugin_output= "Service check scheduled for " + new Date( new_svcstatus.next_check * 1000 ).toString();
                }
                else
                    new_svcstatus.plugin_output= "Service is not scheduled to be checked...";
            }
        }
        
        
        /* add new servicestatus to servicestatus chained hash list */
        if( add_servicestatus_to_hashlist(new_svcstatus) == 0 )
            return common_h.ERROR;
        
        /* object cache file is already sorted, so just add new items to end of list */
        servicestatus_list.add( new_svcstatus );
        
        return common_h.OK;
    }





/******************************************************************/
/*********************** CLEANUP FUNCTIONS ************************/
/******************************************************************/


    /* free all memory for status data */
    public static void free_status_data(){
        
        /* free memory for the host status list */
        hoststatus_list.clear();
        hoststatus_hashlist.clear();
        
        servicestatus_list.clear();
        servicestatus_hashlist.clear();
        
    }




/******************************************************************/
/************************ SEARCH FUNCTIONS ************************/
/******************************************************************/


    /* find a host status entry */
    public static statusdata_h.hoststatus find_hoststatus( String host_name){
        
        return (statusdata_h.hoststatus) hoststatus_hashlist.get( host_name );
    }
    
    /* find a service status entry */
    public static statusdata_h.servicestatus find_servicestatus( String host_name, String svc_desc){
        
        return (statusdata_h.servicestatus) servicestatus_hashlist.get( host_name + "." + svc_desc );
    }




/******************************************************************/
/*********************** UTILITY FUNCTIONS ************************/
/******************************************************************/


    /* gets the total number of services of a certain state for a specific host */
    public static int get_servicestatus_count( String host_name, int type){
        statusdata_h.servicestatus temp_status;
        int count=0;
        
        if(host_name!= null) {
            for (ListIterator iter = servicestatus_list.listIterator(); iter.hasNext(); ) {
                temp_status = (statusdata_h.servicestatus) iter.next();
                if ( ( ( temp_status.status & type ) > 0 ) &&  ( host_name.equals( temp_status.host_name ) ) )
                    count ++;
            }
        }
        
        return count;
    }
}