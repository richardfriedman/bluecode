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
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;


public class notifications { 
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.notifications");
    private static String cn = "org.blue.base.notifications";
    
    /* notify contacts about a service problem or recovery */
    public static int service_notification(objects_h.service svc, int type, String ack_author, String ack_data)
    {
        int contacts_notified=0;
        int escalated = common_h.FALSE;
        
        logger.trace( "entering " + cn + ".service_notification" );
        
        /* get the current time */
        long current_time = utils.currentTimeInSeconds();
        blue_h.timeval start_time = new blue_h.timeval();
        
        logger.debug("\nSERVICE NOTIFICATION ATTEMPT: Service '"+svc.description+"' on host '"+svc.host_name+"'");
        logger.debug("\tType: " + type);
        logger.debug("\tCurrent time: " + new Date(current_time*1000).toString() );
        
        /* find the host this service is associated with */
        objects_h.host temp_host = objects.find_host(svc.host_name);
        
        /* if we couldn't find the host, return an error */
        if(temp_host==null)
        {
            logger.debug ("\tCouldn't find the host associated with this service, so we won't send a notification!\n");
            return common_h.ERROR;
        }
        
        /* check the viability of sending out a service notification */
        if(check_service_notification_viability(svc,type)==common_h.ERROR)
        {
            logger.debug ("\tSending out a notification for this service is not viable at this time.\n");
            return common_h.OK;
        }
        
        /* if this is just a normal notification... */
        if(type==blue_h.NOTIFICATION_NORMAL){
            
            /* increase the current notification number */
            svc.current_notification_number++;
            
            logger.debug ("\tCurrent notification number: " + svc.current_notification_number);
            
        }
                
        /* create the contact notification list for this service */
        escalated = create_notification_list_from_service(svc);

        /* send data to event broker */
        broker.broker_notification_data(broker_h.NEBTYPE_NOTIFICATION_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_NOTIFICATION,type,start_time,new blue_h.timeval(0,0),(Object)svc,ack_author,ack_data,escalated,0,null);
    
        /* we have contacts to notify... */
        if(blue.notification_list!=null && !blue.notification_list.isEmpty() ){
            
            /* grab the macro variables */
            utils.clear_volatile_macros();
            utils.grab_host_macros(temp_host);
            utils.grab_service_macros(svc);
            
            /* if this is an acknowledgement, get the acknowledgement macros */
            if(type==blue_h.NOTIFICATION_ACKNOWLEDGEMENT){
                blue.macro_x[blue_h.MACRO_SERVICEACKAUTHOR]=ack_author;
                blue.macro_x[blue_h.MACRO_SERVICEACKCOMMENT]=ack_data;
            }
            
            /* set the notification type macro */
            if(type==blue_h.NOTIFICATION_ACKNOWLEDGEMENT)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="ACKNOWLEDGEMENT";
            else if(type==blue_h.NOTIFICATION_FLAPPINGSTART)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="FLAPPINGSTART";
            else if(type==blue_h.NOTIFICATION_FLAPPINGSTOP)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="FLAPPINGSTOP";
            else if(svc.current_state==blue_h.STATE_OK)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="RECOVERY";
            else
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="PROBLEM";
            
            /* set the notification number macro */
            blue.macro_x[blue_h.MACRO_NOTIFICATIONNUMBER]= "" + svc.current_notification_number;
            
            /* notify each contact (duplicates have been removed) */
            for ( blue_h.notification temp_notification : (ArrayList<blue_h.notification>)  blue.notification_list ) {
                
                /* grab the macro variables for this contact */
                utils.grab_contact_macros(temp_notification.contact);
                
                /* grab summary macros (customized for this contact) */
                utils.grab_summary_macros(temp_notification.contact);
                
                /* notify this contact */
                /* keep track of how many contacts were notified */
                if(notify_contact_of_service(temp_notification.contact,svc,type,ack_author,ack_data,escalated)==common_h.OK)
                    contacts_notified++;
            }
            
            /* free memory allocated to the notification list */
            utils.free_notification_list();
            
            if(type==blue_h.NOTIFICATION_NORMAL){
                
                /* adjust last/next notification time and notification flags if we notified someone */
                if(contacts_notified>0){
                    
                    /* calculate the next acceptable re-notification time */
                    svc.next_notification=get_next_service_notification_time(svc,current_time);
                    
                    logger.debug("\tCurrent Time: " + new Date(current_time*1000).toString() );
                    logger.debug("\tNext acceptable notification time: " + new Date(svc.next_notification*1000));
                    
                    /* update the last notification time for this service (this is needed for rescheduling later notifications) */
                    svc.last_notification=current_time;
                    
                    /* update notifications flags */
                    if(svc.current_state==blue_h.STATE_UNKNOWN)
                        svc.notified_on_unknown=common_h.TRUE;
                    else if(svc.current_state==blue_h.STATE_WARNING)
                        svc.notified_on_warning=common_h.TRUE;
                    else if(svc.current_state==blue_h.STATE_CRITICAL)
                        svc.notified_on_critical=common_h.TRUE;
                }
                
                /* we didn't end up notifying anyone, so adjust current notification number */
                else
                    svc.current_notification_number--;
            }
            logger.debug ("\tAPPROPRIATE CONTACTS HAVE BEEN NOTIFIED\n");
        }
        
        /* there were no contacts, so no notification really occurred... */
        else{
            
            /* readjust current notification number, since one didn't go out */
            if(type==blue_h.NOTIFICATION_NORMAL)
                svc.current_notification_number--;
            logger.debug ("\tTHERE WERE NO CONTACTS TO BE NOTIFIED!\n");
        }
        

        /* send data to event broker */
        broker.broker_notification_data(broker_h.NEBTYPE_NOTIFICATION_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_NOTIFICATION,type,start_time,new blue_h.timeval(),(Object)svc,ack_author,ack_data,escalated,contacts_notified,null);
        
        /* update the status log with the service information */
        statusdata.update_service_status(svc,common_h.FALSE);
        
        logger.trace( "exiting " + cn + ".service_notification" );
        
        return common_h.OK;
    }
    
    /* checks the viability of sending out a service alert (top level filters) */
    public static int check_service_notification_viability(objects_h.service svc, int type){
       objects_h.host temp_host;
        long timeperiod_start;
        
        logger.trace( "entering " + cn + ".check_service_notification_viability" );
        
        /* get current time */
        long current_time = utils.currentTimeInSeconds(); 
        
        /* are notifications enabled? */
        if(blue.enable_notifications==common_h.FALSE){
            logger.debug ("\tNotifications are disabled, so service notifications (problems and acknowledments) will not be sent out!\n");
            return common_h.ERROR;
        }
        
        /* find the host this service is associated with, UPDATED 2.2 */
        temp_host=objects.find_host(svc.host_name);
        
        /* if we couldn't find the host, return an error, UPDATED 2.2 */
        if(temp_host==null){
           logger.debug("\tCouldn't find the host associated with this service, so we won't send a notification!");
           return common_h.ERROR;
        }
        
        /* see if the service can have notifications sent out at this time */
        if( utils.check_time_against_period(current_time,svc.notification_period)==common_h.ERROR){
            logger.debug ("\tThis service shouldn't have notifications sent out at this time!\n");
            
            /* calculate the next acceptable notification time, once the next valid time range arrives... */
            if(type==blue_h.NOTIFICATION_NORMAL){
                
                timeperiod_start = utils.get_next_valid_time(current_time,svc.notification_period);
                
                /* looks like there are no valid notification times defined, so schedule the next one far into the future (one year)... */
                if(timeperiod_start==0)
                    svc.next_notification=(current_time+(60*60*24*365));
                
                /* else use the next valid notification time */
                else
                    svc.next_notification=timeperiod_start;
            }
            
            return common_h.ERROR;
        }
        
        /* are notifications temporarily disabled for this service? */
        if(svc.notifications_enabled==common_h.FALSE){
            logger.debug ("\tNotifications are temporarily disabled for this service, so we won't send one out!\n");
            return common_h.ERROR;
        }
        
        
        /****************************************/
        /*** SPECIAL CASE FOR ACKNOWLEGEMENTS ***/
        /****************************************/
        
        /* acknowledgements only have to pass three general filters, although they have another test of their own... */
        if(type==blue_h.NOTIFICATION_ACKNOWLEDGEMENT){
            
            /* don't send an acknowledgement if there isn't a problem... */
            if(svc.current_state==blue_h.STATE_OK){
                logger.debug ("\tThe service is currently OK, so we won't send an acknowledgement!\n");
                return common_h.ERROR;
            }
            
            /* acknowledgement viability test passed, so the notification can be sent out */
            return common_h.OK;
        }
        
        
        /****************************************/
        /*** SPECIAL CASE FOR FLAPPING ALERTS ***/
        /****************************************/
        
        /* flapping notifications only have to pass three general filters */
        if(type==blue_h.NOTIFICATION_FLAPPINGSTART || type==blue_h.NOTIFICATION_FLAPPINGSTOP){
            
            /* don't send a notification if we're not supposed to... */
            if(svc.notify_on_flapping==common_h.FALSE){
                logger.debug ("\tWe shouldn't notify about FLAPPING events for this service!\n");
                return common_h.ERROR;
            }
            
            /* don't send notifications during scheduled downtime, UPDATED 2.2 */
            if(svc.scheduled_downtime_depth>0 || temp_host.scheduled_downtime_depth>0){
               logger.debug("\tWe shouldn't notify about FLAPPING events during scheduled downtime!");
               return common_h.ERROR;
            }
            
            /* flapping viability test passed, so the notification can be sent out */
            return common_h.OK;
        }
        
        
        /****************************************/
        /*** NORMAL NOTIFICATIONS ***************/
        /****************************************/

        /* has this problem already been acknowledged? */
        if(svc.problem_has_been_acknowledged==common_h.TRUE){
            logger.debug ("\tThis service problem has already been acknowledged, so we won't send a notification out!\n");
            return common_h.ERROR;
        }

        /* check service notification dependencies */
        if(  checks.check_service_dependencies(svc,common_h.NOTIFICATION_DEPENDENCY)==blue_h.DEPENDENCIES_FAILED){
            logger.debug ("\tService notification dependencies for this service have failed, so we won't sent a notification out!\n");
            return common_h.ERROR;
        }
        
        /* check host notification dependencies */
        if( checks.check_host_dependencies(temp_host,common_h.NOTIFICATION_DEPENDENCY)==blue_h.DEPENDENCIES_FAILED){
            logger.debug ("\tHost notification dependencies for this service have failed, so we won't sent a notification out!\n");
            return common_h.ERROR;
        }
        
        /* see if we should notify about problems with this service */
        if(svc.current_state==blue_h.STATE_UNKNOWN && svc.notify_on_unknown==common_h.FALSE){
            logger.debug ("\tWe shouldn't notify about UNKNOWN states for this service!\n");
            return common_h.ERROR;
        }
        if(svc.current_state==blue_h.STATE_WARNING && svc.notify_on_warning==common_h.FALSE){
            logger.debug ("\tWe shouldn't notify about WARNING states for this service!\n");
            return common_h.ERROR;
        }
        if(svc.current_state==blue_h.STATE_CRITICAL && svc.notify_on_critical==common_h.FALSE){
            logger.debug ("\tWe shouldn't notify about CRITICAL states for this service!\n");
            return common_h.ERROR;
        }
        if(svc.current_state==blue_h.STATE_OK){
            if(svc.notify_on_recovery==common_h.FALSE){
                logger.debug ("\tWe shouldn't notify about RECOVERY states for this service!\n");
                return common_h.ERROR;
            }
            if(!(svc.notified_on_unknown==common_h.TRUE || svc.notified_on_warning==common_h.TRUE || svc.notified_on_critical==common_h.TRUE)){
                logger.debug ("\tWe shouldn't notify about this recovery\n");
                return common_h.ERROR;
            }
        }
        
        /* if this service is currently flapping, don't send the notification */
        if(svc.is_flapping==common_h.TRUE){
            logger.debug ("\tThis service is currently flapping, so we won't send notifications!\n");
            return common_h.ERROR;
        }
        
        /***** RECOVERY NOTIFICATIONS ARE GOOD TO GO AT THIS POINT *****/
        if(svc.current_state==blue_h.STATE_OK)
            return common_h.OK;

        /* don't notify contacts about this service problem again if the notification interval is set to 0 */
        if(svc.no_more_notifications==common_h.TRUE){
            logger.debug ("\tWe shouldn't re-notify contacts about this service problem!\n");
            return common_h.ERROR;
        }
        
        /* if the host is down or unreachable, don't notify contacts about service failures */
        if(temp_host.current_state!=blue_h.HOST_UP){
            logger.debug ("\tThe host is either down or unreachable, so we won't notify contacts about this service!\n");
            return common_h.ERROR;
        }
        
        /* don't notify if we haven't waited long enough since the last time (and the service is not marked as being volatile) */
        if((current_time < svc.next_notification) && svc.is_volatile==common_h.FALSE){
            logger.debug("\tWe haven't waited long enough to re-notify contacts about this service!");
            logger.debug("\tNext valid notification time:" + new Date(svc.next_notification*1000).toString() );
            return common_h.ERROR;
        }
        
        /* if this service is currently in a scheduled downtime period, don't send the notification */
        if(svc.scheduled_downtime_depth>0){
            logger.debug ("\tThis service is currently in a scheduled downtime, so we won't send notifications!\n");
            return common_h.ERROR;
        }
        
        /* if this host is currently in a scheduled downtime period, don't send the notification */
        if(temp_host.scheduled_downtime_depth>0){
            logger.debug ("\tThe host this service is associated with is currently in a scheduled downtime, so we won't send notifications!\n");
            return common_h.ERROR;
        }
        
        logger.trace( "exiting " + cn + ".check_service_notification_viability" );
        
        return common_h.OK;
    }
    
    /* check viability of sending out a service notification to a specific contact (contact-specific filters) */
    public static int check_contact_service_notification_viability(objects_h.contact cntct, objects_h.service svc, int type){
        
        logger.trace( "entering " + cn + ".check_contact_service_notification_viability" );
        
        /* see if the contact can be notified at this time */
        if( utils.check_time_against_period( utils.currentTimeInSeconds(),cntct.service_notification_period)==common_h.ERROR){
            logger.debug ("\tThis contact shouldn't be notified at this time!\n");
            return common_h.ERROR;
        }
        
        /****************************************/
        /*** SPECIAL CASE FOR FLAPPING ALERTS ***/
        /****************************************/
        
        if(type==blue_h.NOTIFICATION_FLAPPINGSTART || type==blue_h.NOTIFICATION_FLAPPINGSTOP){
            
            if(cntct.notify_on_service_flapping == common_h.FALSE ){
                logger.debug ("\tWe shouldn't notify this contact about FLAPPING service events!\n");
                return common_h.ERROR;
            }
            
            return common_h.OK;
        }
        
        /*************************************/
        /*** ACKS AND NORMAL NOTIFICATIONS ***/
        /*************************************/
        
        /* see if we should notify about problems with this service */
        if(svc.current_state==blue_h.STATE_UNKNOWN && cntct.notify_on_service_unknown == common_h.FALSE ){
            logger.debug ("\tWe shouldn't notify this contact about UNKNOWN service states!\n");
            return common_h.ERROR;
        }
        
        if(svc.current_state==blue_h.STATE_WARNING && cntct.notify_on_service_warning == common_h.FALSE ){
            logger.debug ("\tWe shouldn't notify this contact about WARNING service states!\n");
            return common_h.ERROR;
        }
        
        if(svc.current_state==blue_h.STATE_CRITICAL && cntct.notify_on_service_critical == common_h.FALSE ){
            logger.debug ("\tWe shouldn't notify this contact about CRITICAL service states!\n");
            return common_h.ERROR;
        }
        
        if(svc.current_state==blue_h.STATE_OK){
            
            if(cntct.notify_on_service_recovery == common_h.FALSE ){
                logger.debug ("\tWe shouldn't notify this contact about RECOVERY service states!\n");
                return common_h.ERROR;
            }
            
            if(!((svc.notified_on_unknown==common_h.TRUE && cntct.notify_on_service_unknown == common_h.TRUE) || (svc.notified_on_warning==common_h.TRUE && cntct.notify_on_service_warning== common_h.TRUE ) || (svc.notified_on_critical==common_h.TRUE && cntct.notify_on_service_critical == common_h.TRUE ))){
                logger.debug ("\tWe shouldn't notify about this recovery\n");
                return common_h.ERROR;
            }
            
        }
        
        logger.trace( "exiting " + cn + ".check_contact_service_notification_viability" );
        
        return common_h.OK;
    }
    
    
    /* notify a specific contact about a service problem or recovery */
    public static int notify_contact_of_service(objects_h.contact cntct, objects_h.service svc, int type, String ack_author, String ack_data, int escalated ){
       int macro_options= blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;
       
       logger.trace( "entering " + cn + ".notify_contact_of_service" );
       logger.debug ("\tNotify user " + cntct.name);
       
       /* check viability of notifying this user */
       /* acknowledgements are no longer excluded from this test - added 8/19/02 Tom Bertelson */
       if( check_contact_service_notification_viability(cntct,svc,type)==common_h.ERROR)
          return common_h.ERROR;
       
       /* get start time */
       blue_h.timeval start_time = new blue_h.timeval();
       
       /* send data to event broker */
       blue_h.timeval end_time = new blue_h.timeval();
       broker.broker_contact_notification_data(broker_h.NEBTYPE_CONTACTNOTIFICATION_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_NOTIFICATION,type,start_time,end_time,(Object)svc,cntct,ack_author,ack_data,escalated,null);
       
       /* process all the notification commands this user has */
       for ( objects_h.commandsmember temp_commandsmember : (ArrayList<objects_h.commandsmember>)  cntct.service_notification_commands ) {
          
           /* get start time */
           blue_h.timeval method_start_time = new blue_h.timeval();
           
           /* send data to event broker */
           blue_h.timeval method_end_time = new blue_h.timeval(0,0);
           broker.broker_contact_notification_method_data(broker_h.NEBTYPE_CONTACTNOTIFICATIONMETHOD_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_NOTIFICATION,type,method_start_time,method_end_time,(Object)svc,cntct,temp_commandsmember.command,ack_author,ack_data,escalated,null);
           
            /* get the command name */
            String command_name = temp_commandsmember.command;
            logger.info( command_name );

            if ( command_name.indexOf( "!") > 0 ) 
                command_name = command_name.substring( 0, command_name.indexOf( "!"));
            
            /* get the raw command line */
            String raw_command = utils.get_raw_command_line(temp_commandsmember.command,macro_options);
            
            /* process any macros contained in the argument */
            String processed_command = utils.process_macros(raw_command,macro_options);
            
            /* run the notification command */
            if ( processed_command.trim().length() != 0 ) {
                
                logger.debug("\tRaw Command:       " + raw_command);
                logger.debug("\tProcessed Command: " + processed_command);
                
                /* log the notification to program log file */
                if(blue.log_notifications==common_h.TRUE){
                    String temp_buffer;
                    switch(type){
                    case blue_h.NOTIFICATION_ACKNOWLEDGEMENT:
                        temp_buffer = "SERVICE NOTIFICATION: "+cntct.name+";"+svc.host_name+";"+svc.description+";ACKNOWLEDGEMENT ("+blue.macro_x[blue_h.MACRO_SERVICESTATE]+");"+command_name+";"+blue.macro_x[blue_h.MACRO_SERVICEOUTPUT]+";"+blue.macro_x[blue_h.MACRO_SERVICEACKAUTHOR]+";"+blue.macro_x[blue_h.MACRO_SERVICEACKCOMMENT];
                        break;
                    case blue_h.NOTIFICATION_FLAPPINGSTART:
                        temp_buffer = "SERVICE NOTIFICATION: "+cntct.name+";"+svc.host_name+";"+svc.description+";FLAPPINGSTART ("+blue.macro_x[blue_h.MACRO_SERVICESTATE]+");"+command_name+";"+blue.macro_x[blue_h.MACRO_SERVICEOUTPUT] ;
                        break;
                    case blue_h.NOTIFICATION_FLAPPINGSTOP:
                        temp_buffer = "SERVICE NOTIFICATION: "+cntct.name+";"+svc.host_name+";"+svc.description+";FLAPPINGSTOP ("+blue.macro_x[blue_h.MACRO_SERVICESTATE]+");"+command_name+";"+blue.macro_x[blue_h.MACRO_SERVICEOUTPUT] ;
                        break;
                    default:
                        temp_buffer = "SERVICE NOTIFICATION: "+cntct.name+";"+svc.host_name+";"+svc.description+";"+blue.macro_x[blue_h.MACRO_SERVICESTATE]+";"+command_name+";"+blue.macro_x[blue_h.MACRO_SERVICEOUTPUT];
                    break;
                    }
                    logger.info(temp_buffer);
                }
                
                /* run the command */
                utils.system_result result = utils.my_system( processed_command, blue.notification_timeout );
                
                /* check to see if the notification command timed out */
                if( result.early_timeout==true)
                    logger.warn( "Warning: Contact '"+cntct.name+"' service notification command '"+processed_command+"' timed out after "+ blue_h.DEFAULT_NOTIFICATION_TIMEOUT+" seconds" );
            }
            
            /* get end time */
            method_end_time = new blue_h.timeval();
            
            /* send data to event broker */
            broker.broker_contact_notification_method_data(broker_h.NEBTYPE_CONTACTNOTIFICATIONMETHOD_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_NOTIFICATION,type,method_start_time,method_end_time,(Object)svc,cntct,temp_commandsmember.command,ack_author,ack_data,escalated,null);
            
        }

       /* get end time */
       end_time = new blue_h.timeval();
       
       /* send data to event broker */
       broker.broker_contact_notification_data(broker_h.NEBTYPE_CONTACTNOTIFICATION_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.SERVICE_NOTIFICATION,type,start_time,end_time,(Object)svc,cntct,ack_author,ack_data,escalated,null);
        
        logger.trace( "exiting " + cn + ".notify_contact_of_service" );
        
        return common_h.OK;
    }
    
    
    /* checks to see if a service escalation entry is a match for the current service notification */
    public static int is_valid_escalation_for_service_notification(objects_h.service svc, objects_h.serviceescalation se){
        int notification_number;
        
        logger.trace( "entering " + cn + ".is_valid_escalation_for_service_notification" );
        
        /* get the current time */
        long current_time = utils.currentTimeInSeconds();
        
        /* if this is a recovery, really we check for who got notified about a previous problem */
        if(svc.current_state==blue_h.STATE_OK)
            notification_number=svc.current_notification_number-1;
        else
            notification_number=svc.current_notification_number;
        
        /* this entry if it is not for this service */
        if(svc.host_name.equals(se.host_name) || svc.description.equals(se.description))
            return common_h.FALSE;
        
        /* skip this escalation if it happens later */
        if(se.first_notification > notification_number)
            return common_h.FALSE;
        
        /* skip this escalation if it has already passed */
        if(se.last_notification!=0 && se.last_notification < notification_number)
            return common_h.FALSE;
        
        /* skip this escalation if it has a timeperiod and the current time isn't valid */
        if(se.escalation_period!=null && utils.check_time_against_period(current_time,se.escalation_period)==common_h.ERROR)
            return common_h.FALSE;
        
        /* skip this escalation if the state options don't match */
        if(svc.current_state==blue_h.STATE_OK && se.escalate_on_recovery==common_h.FALSE)
            return common_h.FALSE;
        else if(svc.current_state==blue_h.STATE_WARNING && se.escalate_on_warning==common_h.FALSE)
            return common_h.FALSE;
        else if(svc.current_state==blue_h.STATE_UNKNOWN && se.escalate_on_unknown==common_h.FALSE)
            return common_h.FALSE;
        else if(svc.current_state==blue_h.STATE_CRITICAL && se.escalate_on_critical==common_h.FALSE)
            return common_h.FALSE;
        
        logger.trace( "exiting " + cn + ".is_valid_escalation_for_service_notification" );
        
        return common_h.TRUE;
    }
    
    
    /* checks to see whether a service notification should be escalation */
    public static int should_service_notification_be_escalated( objects_h.service svc){
        
        logger.trace( "entering " + cn + ".should_service_notification_be_escalated" );
        
        /* search the service escalation list */
        for ( objects_h.serviceescalation temp_se : (ArrayList<objects_h.serviceescalation>)  objects.serviceescalation_list ) {
            
            /* we found a matching entry, so escalate this notification! */
            if(is_valid_escalation_for_service_notification(svc,temp_se)==common_h.TRUE){
                logger.debug ("\tService notification WILL BE escalated\n");
                return common_h.TRUE;
            }
        }
        
        logger.debug ("\tService notification will NOT be escalated\n");
        
        logger.trace( "exiting " + cn + ".should_service_notification_be_escalated" );
        
        return  common_h.FALSE;
    }
    
    /**
     *  given a service, create a list of contacts to be notified, removing duplicates 
     *
     *  @return ESCALATED, if this notification should be/has been escalated.
     */
    public static int create_notification_list_from_service(objects_h.service svc ){
        
        logger.trace( "entering " + cn + ".create_notification_list_from_service" );
        
        /* should this notification be escalated? */
        int escalated = should_service_notification_be_escalated(svc);
        if(escalated == common_h.TRUE){
            
            /* search all the escalation entries for valid matches */
            for ( objects_h.serviceescalation temp_se : (ArrayList<objects_h.serviceescalation>)  objects.serviceescalation_list ) {
                
                /* skip this entry if it isn't appropriate */
                if(is_valid_escalation_for_service_notification(svc,temp_se)==common_h.FALSE)
                    continue;
                
                /* find each contact group in this escalation entry */
                for ( objects_h.contactgroupsmember temp_group : (ArrayList<objects_h.contactgroupsmember>)  temp_se.contact_groups ) {
                    
                    objects_h.contactgroup temp_contactgroup= objects.find_contactgroup(temp_group.group_name);
                    if(temp_contactgroup==null)
                        continue;
                    
                    /* check all contacts */
                    for ( objects_h.contact temp_contact : (ArrayList<objects_h.contact>)  objects.contact_list ) {
                        
                        if( objects.is_contact_member_of_contactgroup(temp_contactgroup,temp_contact)==common_h.TRUE)
                            add_notification(temp_contact);
                    }
                }
            }
        }
        
        /* no escalation is necessary - just continue normally... */
        else{
            
            /* find all contacts for this service */
            for ( objects_h.contact temp_contact : (ArrayList<objects_h.contact>)  objects.contact_list ) {
                
                if( objects.is_contact_for_service(svc,temp_contact)== true)
                    add_notification(temp_contact);
            }
        }
        
        logger.trace( "exiting " + cn + ".create_notification_list_from_service" );
        
        return escalated;
    }
    
    /******************************************************************/
    /******************* HOST NOTIFICATION FUNCTIONS ******************/
    /******************************************************************/
    
    /* notify all contacts for a host that the entire host is down or up */
    public static int host_notification(objects_h.host hst, int type, String ack_author, String ack_data){
        int result=common_h.OK;
        int contacts_notified=0;
        int escalated;
        
        logger.trace( "entering " + cn + ".host_notification" );
        
        /* get the current time */
        long current_time = utils.currentTimeInSeconds();
        blue_h.timeval start_time = new blue_h.timeval();
        
        logger.debug("\nHOST NOTIFICATION ATTEMPT: Host '"+hst.name+"'");
        logger.debug("\tType: "+type);
        logger.debug("\tCurrent time:  " + current_time);
        
        /* check viability of sending out a host notification */
        if(check_host_notification_viability(hst,type)==common_h.ERROR){
            logger.debug("\tSending out a notification for this host is not viable at this time.");
            return common_h.OK;
        }
        
        /* if this is just a normal notification... */
        if(type==blue_h.NOTIFICATION_NORMAL){
            
            /* increment the current notification number */
            hst.current_notification_number++;
            logger.debug("\tCurrent notification number: " + hst.current_notification_number);
        }
        
        /* create the contact notification list for this host */
        escalated = create_notification_list_from_host(hst);

        /* send data to event broker */
        blue_h.timeval end_time = new blue_h.timeval(0,0);
        broker.broker_notification_data(broker_h.NEBTYPE_NOTIFICATION_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_NOTIFICATION,type,start_time,end_time,(Object)hst,ack_author,ack_data,escalated,0,null);
        
        /* there are contacts to be notified... */
        if(blue.notification_list!=null && blue.notification_list.size() > 0 ){
            
            /* grab the macro variables */
            utils.clear_volatile_macros();
            utils.grab_host_macros(hst);
            
            /* if this is an acknowledgement, get the acknowledgement macros */
            if(type==blue_h.NOTIFICATION_ACKNOWLEDGEMENT){
                blue.macro_x[blue_h.MACRO_HOSTACKAUTHOR]=ack_author;
                blue.macro_x[blue_h.MACRO_HOSTACKCOMMENT]=ack_data;
            }
            /* set the notification type macro */
            if(type==blue_h.NOTIFICATION_ACKNOWLEDGEMENT)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="ACKNOWLEDGEMENT";
            else if(type==blue_h.NOTIFICATION_FLAPPINGSTART)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="FLAPPINGSTART";
            else if(type==blue_h.NOTIFICATION_FLAPPINGSTOP)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="FLAPPINGSTOP";
            else if(hst.current_state==blue_h.HOST_UP)
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="RECOVERY";
            else
                blue.macro_x[blue_h.MACRO_NOTIFICATIONTYPE]="PROBLEM";
            
            /* set the notification number macro */
            blue.macro_x[blue_h.MACRO_NOTIFICATIONNUMBER] = "" + hst.current_notification_number;
            
            /* notify each contact (duplicates have been removed) */
            for ( blue_h.notification temp_notification : (ArrayList<blue_h.notification>) blue.notification_list ) {
                
                /* grab the macro variables for this contact */
                utils.grab_contact_macros(temp_notification.contact);
                
                /* grab summary macros (customized for this contact) */
                utils.grab_summary_macros(temp_notification.contact);
                
                /* notify this contact, UPDATED 2.2 */
                result=notify_contact_of_host(temp_notification.contact,hst,type,ack_author,ack_data,escalated);
                
                /* keep track of how many contacts were notified */
                if(result==common_h.OK)
                    contacts_notified++;
            }
            
            /* free memory allocated to the notification list */
            utils.free_notification_list();
            
            if(type==blue_h.NOTIFICATION_NORMAL){
                
                /* adjust last/next notification time and notification flags if we notified someone */
                if(contacts_notified>0){
                    
                    /* calculate the next acceptable re-notification time */
                    hst.next_host_notification=get_next_host_notification_time(hst,current_time);
                    
                    logger.debug("\tCurrent Time: " + new Date(current_time*1000).toString() );
                    logger.debug("\tNext acceptable notification time: " + new Date(hst.next_host_notification*1000).toString() );
                    
                    /* update the last notification time for this host (this is needed for scheduling the next problem notification) */
                    hst.last_host_notification=current_time;
                    
                    /* update notifications flags */
                    if(hst.current_state==blue_h.HOST_DOWN)
                        hst.notified_on_down=common_h.TRUE;
                    else if(hst.current_state==blue_h.HOST_UNREACHABLE)
                        hst.notified_on_unreachable=common_h.TRUE;
                }
                
                /* we didn't end up notifying anyone, so adjust current notification number */
                else
                    hst.current_notification_number--;
            }
            
            logger.debug("\tAPPROPRIATE CONTACTS HAVE BEEN NOTIFIED\n");
        }
        
        /* there were no contacts, so no notification really occurred... */
        else{
            
            /* adjust notification number, since no notification actually went out */
            if(type==blue_h.NOTIFICATION_NORMAL)
                hst.current_notification_number--;
            
            logger.debug("\tTHERE WERE NO CONTACTS TO BE NOTIFIED!\n");
        }
        
      /* get the time we finished */
      end_time = new blue_h.timeval();
      
      /* send data to event broker */
      broker.broker_notification_data(broker_h.NEBTYPE_NOTIFICATION_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_NOTIFICATION,type,start_time,end_time,(Object)hst,ack_author,ack_data,escalated,contacts_notified,null);
        
        /* update the status log with the host info */
        statusdata.update_host_status(hst,common_h.FALSE);
        
        logger.trace( "exiting " + cn + ".host_notification" );
        
        return common_h.OK;
    }
    
    /* checks viability of sending a host notification */
    public static int check_host_notification_viability(objects_h.host hst, int type){
        long timeperiod_start;
        
        logger.trace( "entering " + cn + ".check_host_notification_viability" );
        
        /* get current time */
        long current_time = utils.currentTimeInSeconds();
        
        /* are notifications enabled? */
        if(blue.enable_notifications==common_h.FALSE){
            logger.debug("\tNotifications are disabled, so host notifications (problems or acknowledgements) will not be sent out!");
            return common_h.ERROR;
        }
        
        /* see if the host can have notifications sent out at this time */
        if(utils.check_time_against_period(current_time,hst.notification_period)==common_h.ERROR){
            logger.debug("\tThis host shouldn't have notifications sent out at this time!");
            
            /* if this is a normal notification, calculate the next acceptable notification time, once the next valid time range arrives... */
            if(type==blue_h.NOTIFICATION_NORMAL){
                
                timeperiod_start = utils.get_next_valid_time(current_time,hst.notification_period);
                
                /* it looks like there is no notification time defined, so schedule next one far into the future (one year)... */
                if(timeperiod_start==0)
                    hst.next_host_notification= (current_time+(60*60*24*365));
                
                /* else use the next valid notification time */
                else
                    hst.next_host_notification=timeperiod_start;
            }
            
            return common_h.ERROR;
        }
        
        /* are notifications temporarily disabled for this host? */
        if(hst.notifications_enabled==common_h.FALSE){
            logger.debug("\tNotifications are temporarily disabled for this host, so we won't send one out!");
            return common_h.ERROR;
        }
        
        /****************************************/
        /*** SPECIAL CASE FOR ACKNOWLEGEMENTS ***/
        /****************************************/
        
        /* acknowledgements only have to pass three general filters, although they have another test of their own... */
        if(type==blue_h.NOTIFICATION_ACKNOWLEDGEMENT){
            
            /* don't send an acknowledgement if there isn't a problem... */
            if(hst.current_state==blue_h.HOST_UP){
                logger.debug("\tThe host is currently UP, so we won't send an acknowledgement!");
                return common_h.ERROR;
            }
            
            /* acknowledgement viability test passed, so the notification can be sent out */
            return common_h.OK;
        }
        
        /*****************************************/
        /*** SPECIAL CASE FOR FLAPPING ALERTS ***/
        /*****************************************/
        
        /* flapping notifications only have to pass three general filters */
        if(type==blue_h.NOTIFICATION_FLAPPINGSTART || type==blue_h.NOTIFICATION_FLAPPINGSTOP){
            
            /* don't send a notification if we're not supposed to... */
            if(hst.notify_on_flapping==common_h.FALSE){
                logger.debug("\tWe shouldn't notify about FLAPPING events for this host!\n");
                return common_h.ERROR;
            }

            /* don't send notifications during scheduled downtime, UPDATED 2.2 */
            if(hst.scheduled_downtime_depth>0){
               logger.debug("\tWe shouldn't notify about FLAPPING events during scheduled downtime!");
               return common_h.ERROR;
            }
            
            /* flapping viability test passed, so the notification can be sent out */
            return common_h.OK;
        }
        
        /****************************************/
        /*** NORMAL NOTIFICATIONS ***************/
        /****************************************/
        
        /* has this problem already been acknowledged? */
        if(hst.problem_has_been_acknowledged==common_h.TRUE){
            logger.debug ("\tThis host problem has already been acknowledged, so we won't send a notification out!\n");
            return common_h.ERROR;
        }
        
        /* check notification dependencies */
        if( checks.check_host_dependencies(hst,common_h.NOTIFICATION_DEPENDENCY)==blue_h.DEPENDENCIES_FAILED){
            logger.debug ("\tNotification dependencies for this host have failed, so we won't sent a notification out!\n");
            return common_h.ERROR;
        }
        
        /* see if we should notify about problems with this host */
        if(hst.current_state==blue_h.HOST_UNREACHABLE && hst.notify_on_unreachable==common_h.FALSE){
            logger.debug ("\tWe shouldn't notify about UNREACHABLE status for this host!\n");
            return common_h.ERROR;
        }
        if(hst.current_state==blue_h.HOST_DOWN && hst.notify_on_down==common_h.FALSE){
            logger.debug ("\tWe shouldn't notify about DOWN states for this host!\n");
            return common_h.ERROR;
        }
        if(hst.current_state==blue_h.HOST_UP){
            
            if(hst.notify_on_recovery==common_h.FALSE){
                logger.debug ("\tWe shouldn't notify about RECOVERY states for this host!\n");
                return common_h.ERROR;
            }
            if(!(hst.notified_on_down==common_h.TRUE || hst.notified_on_unreachable==common_h.TRUE)){
                logger.debug ("\tWe shouldn't notify about this recovery\n");
                return common_h.ERROR;
            }
            
        }
        
        /* if this host is currently flapping, don't send the notification */
        if(hst.is_flapping==common_h.TRUE){
            logger.debug ("\tThis host is currently flapping, so we won't send notifications!\n");
            return common_h.ERROR;
        }
        
        /***** RECOVERY NOTIFICATIONS ARE GOOD TO GO AT THIS POINT *****/
        if(hst.current_state==blue_h.HOST_UP)
            return common_h.OK;
        
        /* if this host is currently in a scheduled downtime period, don't send the notification */
        if(hst.scheduled_downtime_depth>0){
            logger.debug ("\tThis host is currently in a scheduled downtime, so we won't send notifications!\n");
            return common_h.ERROR;
        }
        
        /* check if we shouldn't renotify contacts about the host problem */
        if(hst.no_more_notifications==common_h.TRUE){
            
            logger.debug ("\tWe shouldn't re-notify contacts about this host problem!!\n");
            return common_h.ERROR;
        }
        
        /* check if its time to re-notify the contacts about the host... */
        if(current_time < hst.next_host_notification){
            
            logger.debug("\tIts not yet time to re-notify the contacts about this host problem...\n");
            logger.debug("\tNext acceptable notification time: " + new Date( hst.next_host_notification*1000).toString() );
            return common_h.ERROR;
        }
        
        logger.trace( "exiting " + cn + ".check_host_notification_viability" );
        
        return common_h.OK;
    }
    
    /* checks the viability of notifying a specific contact about a host */
    public static int check_contact_host_notification_viability(objects_h.contact cntct, objects_h.host hst, int type){
        
        logger.trace( "entering " + cn + ".check_contact_host_notification_viability" );
        
        /* see if the contact can be notified at this time */
        if( utils.check_time_against_period(utils.currentTimeInSeconds(),cntct.host_notification_period)==common_h.ERROR){
            logger.debug ("\tThis contact shouldn't be notified at this time!\n");
            return common_h.ERROR;
        }
        
        /****************************************/
        /*** SPECIAL CASE FOR FLAPPING ALERTS ***/
        /****************************************/
        
        if(type== blue_h.NOTIFICATION_FLAPPINGSTART || type== blue_h.NOTIFICATION_FLAPPINGSTOP){
            
            if(cntct.notify_on_host_flapping == common_h.FALSE ){
                logger.debug ("\tWe shouldn't notify this contact about FLAPPING host events!\n");
                return common_h.ERROR;
            }
            
            return common_h.OK;
        }
        
        
        /*************************************/
        /*** ACKS AND NORMAL NOTIFICATIONS ***/
        /*************************************/
        
        /* see if we should notify about problems with this host */
        if(hst.current_state==blue_h.HOST_DOWN && cntct.notify_on_host_down== common_h.FALSE){
            logger.debug ("\tWe shouldn't notify this contact about DOWN states!\n");
            return common_h.ERROR;
        }
        
        if(hst.current_state==blue_h.HOST_UNREACHABLE && cntct.notify_on_host_unreachable== common_h.FALSE ){
            logger.debug ("\tWe shouldn't notify this contact about UNREACHABLE states!\n");
            return common_h.ERROR;
        }
        
        if(hst.current_state==blue_h.HOST_UP){
            
            if(cntct.notify_on_host_recovery == common_h.FALSE ){
                logger.debug ("\tWe shouldn't notify this contact about RECOVERY states!\n");
                return common_h.ERROR;
            }
            
            if(!((hst.notified_on_down==common_h.TRUE && cntct.notify_on_host_down== common_h.TRUE ) || (hst.notified_on_unreachable==common_h.TRUE && cntct.notify_on_host_unreachable == common_h.TRUE ))){
                logger.debug ("\tWe shouldn't notify about this recovery\n");
                return common_h.ERROR;
            }
            
        }
        
        logger.trace( "exiting " + cn + ".check_contact_host_notification_viability" );
        
        return common_h.OK;
    }
    
    /* notify a specific contact that an entire host is down or up */
    public static int notify_contact_of_host(objects_h.contact cntct,objects_h.host hst, int type, String ack_author, String ack_data, int escalated){
        int macro_options= blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;
        
        logger.trace( "entering " + cn + ".notify_contact_of_host" );
        logger.debug ("\tNotify user "+cntct.name);
        
        /* check viability of notifying this user about the host */
        /* acknowledgements are no longer excluded from this test - added 8/19/02 Tom Bertelson */
        if( check_contact_host_notification_viability(cntct,hst,type)== common_h.ERROR)
            return common_h.ERROR;

      /* get start time */
      blue_h.timeval start_time = new blue_h.timeval();
      blue_h.timeval end_time = new blue_h.timeval(0,0);
      
      /* send data to event broker */
      broker.broker_contact_notification_data(broker_h.NEBTYPE_CONTACTNOTIFICATION_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_NOTIFICATION,type,start_time,end_time,(Object)hst,cntct,ack_author,ack_data,escalated,null);
        
        /* process all the notification commands this user has */
        for  ( objects_h.commandsmember temp_commandsmember : (ArrayList<objects_h.commandsmember>) cntct.host_notification_commands ) { 

         /* get start time */
           blue_h.timeval method_start_time = new blue_h.timeval ();
           blue_h.timeval method_end_time = new blue_h.timeval(0,0);
         
         /* send data to event broker */
         broker.broker_contact_notification_method_data(broker_h.NEBTYPE_CONTACTNOTIFICATIONMETHOD_START,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_NOTIFICATION,type,method_start_time,method_end_time,(Object)hst,cntct,temp_commandsmember.command,ack_author,ack_data,escalated,null);
           
            /* get the command name */
            String command_name = temp_commandsmember.command;
            if ( command_name.indexOf( "!") > 0 ) 
                command_name = command_name.substring( 0, command_name.indexOf( "!"));
            
            /* get the raw command line */
            String raw_command = utils.get_raw_command_line(temp_commandsmember.command,macro_options);
            
            /* process any macros contained in the argument */
            String pocess_command = utils.process_macros(raw_command,macro_options);
            
            /* run the notification command */
            if ( pocess_command.trim().length() != 0 ) {
                
                
                logger.debug("\tRaw Command:       " + raw_command);
                logger.debug("\tProcessed Command: " + pocess_command);
                
                /* log the notification to program log file */
                if(blue.log_notifications==common_h.TRUE){
                    String temp_buffer;
                    switch(type){
                    case blue_h.NOTIFICATION_ACKNOWLEDGEMENT:
                        temp_buffer = "HOST NOTIFICATION: "+cntct.name +";"+hst.name+";ACKNOWLEDGEMENT ("+blue.macro_x[blue_h.MACRO_HOSTSTATE]+");"+command_name+";"+blue.macro_x[blue_h.MACRO_HOSTOUTPUT]+";"+blue.macro_x[blue_h.MACRO_HOSTACKAUTHOR]+";" + blue.macro_x[blue_h.MACRO_HOSTACKCOMMENT];
                        break;
                    case blue_h.NOTIFICATION_FLAPPINGSTART:
                        temp_buffer = "HOST NOTIFICATION: "+cntct.name+";"+hst.name+";FLAPPINGSTART ("+blue.macro_x[blue_h.MACRO_HOSTSTATE]+");"+command_name+";" + blue.macro_x[blue_h.MACRO_HOSTOUTPUT];
                        break;
                    case blue_h.NOTIFICATION_FLAPPINGSTOP:
                        temp_buffer = "HOST NOTIFICATION: "+cntct.name+";"+hst.name+";FLAPPINGSTOP ("+blue.macro_x[blue_h.MACRO_HOSTSTATE]+");"+command_name+";" + blue.macro_x[blue_h.MACRO_HOSTOUTPUT];					
                        break;
                    default:
                        temp_buffer = "HOST NOTIFICATION: "+cntct.name+";"+hst.name+";"+blue.macro_x[blue_h.MACRO_HOSTSTATE]+";"+command_name+";" + blue.macro_x[blue_h.MACRO_HOSTOUTPUT];
                    break;
                    }
                    logger.info( temp_buffer );
                }
                
                /* run the command */
                utils.system_result result = utils.my_system( pocess_command, blue.notification_timeout );
                
                /* check to see if the notification timed out */
                if( result.early_timeout==true)
                    logger.warn( "Warning: Contact '"+cntct.name+"' host notification command '"+pocess_command+"' timed out after "+blue_h.DEFAULT_NOTIFICATION_TIMEOUT+" seconds\n");
            }

          method_end_time = new blue_h.timeval();
          
          /* send data to event broker */
          broker.broker_contact_notification_method_data(broker_h.NEBTYPE_CONTACTNOTIFICATIONMETHOD_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_NOTIFICATION,type,method_start_time,method_end_time,(Object)hst,cntct,temp_commandsmember.command,ack_author,ack_data,escalated,null);
        }
        
        /* get end time */
        end_time = new blue_h.timeval();
        
        /* send data to event broker */
        broker.broker_contact_notification_data(broker_h.NEBTYPE_CONTACTNOTIFICATION_END,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,blue_h.HOST_NOTIFICATION,type,start_time,end_time,(Object)hst,cntct,ack_author,ack_data,escalated,null);
    
        logger.trace( "exiting " + cn + ".notify_contact_of_host" );
        
        return common_h.OK;
    }
    
    
    /* checks to see if a host escalation entry is a match for the current host notification */
    public static int is_valid_host_escalation_for_host_notification( objects_h.host hst, objects_h.hostescalation he){
        int notification_number;
        
        logger.trace( "entering " + cn + ".is_valid_host_escalation_for_host_notification" );
        
        /* get the current time */
        long current_time = utils.currentTimeInSeconds();
        
        /* if this is a recovery, really we check for who got notified about a previous problem */
        if(hst.current_state==  blue_h.HOST_UP)
            notification_number=hst.current_notification_number-1;
        else
            notification_number=hst.current_notification_number;
        
        /* find the host this escalation entry is associated with */
        objects_h.host temp_host= objects.find_host(he.host_name);
        if(temp_host==null || temp_host!=hst)
            return common_h.FALSE;
        
        /* skip this escalation if it happens later */
        if(he.first_notification > notification_number)
            return common_h.FALSE;
        
        /* skip this escalation if it has already passed */
        if(he.last_notification!=0 && he.last_notification < notification_number)
            return common_h.FALSE;
        
        /* skip this escalation if it has a timeperiod and the current time isn't valid */
        if(he.escalation_period!=null && utils.check_time_against_period(current_time,he.escalation_period)== common_h.ERROR)
            return common_h.FALSE;
        
        /* skip this escalation if the state options don't match */
        if(hst.current_state==blue_h.HOST_UP && he.escalate_on_recovery==common_h.FALSE)
            return common_h.FALSE;
        else if(hst.current_state==blue_h.HOST_DOWN && he.escalate_on_down==common_h.FALSE)
            return common_h.FALSE;
        else if(hst.current_state==blue_h.HOST_UNREACHABLE && he.escalate_on_unreachable==common_h.FALSE)
            return common_h.FALSE;
        
        logger.trace( "exiting " + cn + ".is_valid_host_escalation_for_host_notification" );
        
        return common_h.TRUE;
    }
    
    /* checks to see whether a host notification should be escalation */
    public static int should_host_notification_be_escalated(objects_h.host hst){
        
        logger.trace( "entering " + cn + ".should_host_notification_be_escalated" );
        
        /* search the host escalation list */
        for (objects_h.hostescalation temp_he : (ArrayList<objects_h.hostescalation>) objects.hostescalation_list ) {
            
            /* we found a matching entry, so escalate this notification! */
            if(is_valid_host_escalation_for_host_notification(hst,temp_he)==common_h.TRUE)
                return common_h.TRUE;
        }
        
        logger.trace( "exiting " + cn + ".should_host_notification_be_escalated" );
        
        return common_h.FALSE;
    }
    
    /**
     *  given a host, create a list of contacts to be notified, removing duplicates 
     * 
     *  @RETURN ESCALATED value, true for false.
     */
    public static int create_notification_list_from_host(objects_h.host hst){
        
        
        logger.trace( "entering " + cn + ".create_notification_list_from_host" );
        
        /* see if this notification should be escalated */
        int escalated = should_host_notification_be_escalated(hst); 
        if(escalated ==common_h.TRUE){
            
            /* check all the host escalation entries */
            for (objects_h.hostescalation temp_he : (ArrayList<objects_h.hostescalation>) objects.hostescalation_list ) {
                
                /* see if this escalation if valid for this notification */
                if(is_valid_host_escalation_for_host_notification(hst,temp_he)==common_h.FALSE)
                    continue;
                
                /* find each contact group in this escalation entry */
                for (objects_h.contactgroupsmember temp_group : (ArrayList<objects_h.contactgroupsmember>) temp_he.contact_groups ) {
                    
                    objects_h.contactgroup temp_contactgroup= objects.find_contactgroup(temp_group.group_name);
                    if(temp_contactgroup==null)
                        continue;
                    
                    /* check all contacts */
                    for (objects_h.contact temp_contact : (ArrayList<objects_h.contact>) objects.contact_list ) {
                        
                        if(objects.is_contact_member_of_contactgroup(temp_contactgroup,temp_contact)==common_h.TRUE)
                            add_notification(temp_contact);
                    }
                }
            }
        }
        
        /* else we shouldn't escalate the notification, so continue as normal... */
        else{
            
            /* get all contacts for this host */
            for (objects_h.contact temp_contact : (ArrayList<objects_h.contact>) objects.contact_list ) {
                
                if(objects.is_contact_for_host(hst,temp_contact)== true)
                    add_notification(temp_contact);
            }
        }
        
        logger.trace( "exiting " + cn + ".create_notification_list_from_host" );
        
        return escalated;
    }
    
    /******************************************************************/
    /***************** NOTIFICATION TIMING FUNCTIONS ******************/
    /******************************************************************/
    
    
    /* calculates next acceptable re-notification time for a service */
    public static long get_next_service_notification_time(objects_h.service svc, long offset){
        long next_notification;
        int interval_to_use;
        int have_escalated_interval= common_h.FALSE;
        
        logger.trace( "entering " + cn + ".get_next_service_notification_time" );
        logger.debug ("\tCalculating next valid notification time...\n");
        
        /* default notification interval */
        interval_to_use=svc.notification_interval;
        
        logger.debug ("\t\tDefault interval: " + interval_to_use);
        
        /* search all the escalation entries for valid matches for this service (at its current notification number) */
        for ( objects_h.serviceescalation temp_se : (ArrayList<objects_h.serviceescalation>)  objects.serviceescalation_list ) {
            
            /* interval < 0 means to use non-escalated interval */
            if(temp_se.notification_interval<0)
                continue;
            
            /* skip this entry if it isn't appropriate */
            if( is_valid_escalation_for_service_notification(svc,temp_se)==common_h.FALSE)
                continue;
            
            logger.debug ("\t\tFound a valid escalation w/ interval of " + temp_se.notification_interval);
            
            /* if we haven't used a notification interval from an escalation yet, use this one */
            if(have_escalated_interval==common_h.FALSE){
                have_escalated_interval=common_h.TRUE;
                interval_to_use=temp_se.notification_interval;
            }
            
            /* else use the shortest of all valid escalation intervals */
            else if(temp_se.notification_interval<interval_to_use)
                interval_to_use=svc.notification_interval;
            logger.debug ("\t\tNew interval: " + interval_to_use);
            
        }
        
        /* if notification interval is 0, we shouldn't send any more problem notifications (unless service is volatile) */
        if(interval_to_use==0 && svc.is_volatile==common_h.FALSE)
            svc.no_more_notifications=common_h.TRUE;
        else
            svc.no_more_notifications=common_h.FALSE;
        
        logger.debug ("\tInterval used for calculating next valid notification time: " + interval_to_use);
        
        /* calculate next notification time */
        next_notification=offset+(interval_to_use*blue.interval_length);
        
        logger.trace( "exiting " + cn + ".get_next_service_notification_time" );
        
        return next_notification;
    }
    
    /* calculates next acceptable re-notification time for a host */
    public static long get_next_host_notification_time( objects_h.host hst, long offset){
        long next_notification;
        int interval_to_use;
        int have_escalated_interval= common_h.FALSE;
        
        logger.trace( "entering " + cn + ".get_next_host_notification_time");
        
        /* default notification interval */
        interval_to_use=hst.notification_interval;
        
        /* check all the host escalation entries for valid matches for this host (at its current notification number) */
        for ( objects_h.hostescalation temp_he : (ArrayList<objects_h.hostescalation>) objects.hostescalation_list ) {
            
            /* interval < 0 means to use non-escalated interval */
            if(temp_he.notification_interval<0)
                continue;
            
            /* skip this entry if it isn't appropriate */
            if(is_valid_host_escalation_for_host_notification(hst,temp_he)== common_h.FALSE)
                continue;
            
            /* if we haven't used a notification interval from an escalation yet, use this one */
            if(have_escalated_interval==common_h.FALSE){
                have_escalated_interval=common_h.TRUE;
                interval_to_use=temp_he.notification_interval;
            }
            
            /* else use the shortest of all valid escalation intervals  */
            else if(temp_he.notification_interval<interval_to_use)
                interval_to_use=temp_he.notification_interval;
        }
        
        /* if interval is 0, no more notifications should be sent */
        if(interval_to_use==0)
            hst.no_more_notifications=common_h.TRUE;
        else
            hst.no_more_notifications=common_h.FALSE;
        
        /* calculate next notification time */
        next_notification=offset+(interval_to_use*blue.interval_length);
        
        logger.trace( "exiting " + cn + ".get_next_host_notification_time");
        
        return next_notification;
    }
    
    
    
    /******************************************************************/
    /***************** NOTIFICATION OBJECT FUNCTIONS ******************/
    /******************************************************************/
    
    
    /* given a contact name, find the notification entry for them for the list in memory */
    public static blue_h.notification find_notification(String contact_name){
        
        logger.trace( "entering " + cn + ".find_notification" );
        
        if(contact_name==null)
            return null;
        
        for ( blue_h.notification temp_notification : (ArrayList<blue_h.notification>) blue.notification_list ) 
            if ( contact_name.equals( temp_notification.contact.name ) )
                return temp_notification;
        
        logger.trace( "exiting " + cn + ".find_notification" );
        
        /* we couldn't find the contact in the notification list */
        return null;
    }
    
    /* add a new notification to the list in memory */
    public static int add_notification(objects_h.contact cntct){
        
        logger.trace( "entering " + cn + ".add_notification" );
        logger.debug("\tAdd contact '"+cntct.name+"'");
        
        /* don't add anything if this contact is already on the notification list */
        blue_h.notification temp_notification= find_notification(cntct.name);
        if(temp_notification!=null)
            return common_h.OK;
        
        /* allocate memory for a new contact in the notification list */
        blue_h.notification new_notification = new blue_h.notification();
        
        /* fill in the contact info */
        new_notification.contact=cntct;
        
        /* add new notification to head of list */
        blue.notification_list.add( new_notification );
        
        logger.trace( "exiting " + cn + ".add_notification" );
        
        return common_h.OK;
    }
}