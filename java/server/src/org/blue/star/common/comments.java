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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.broker;
import org.blue.star.base.events;
import org.blue.star.base.utils;
import org.blue.star.include.blue_h;
import org.blue.star.include.broker_h;
import org.blue.star.include.comments_h;
import org.blue.star.include.common_h;
import org.blue.star.xdata.xcddefault;


public class comments 
{

   /** Logger instance */
   private static Logger logger = LogManager.getLogger("org.blue.common");
   private static String cn = "org.blue.common.comments";
    
        public static ArrayList comment_list = new ArrayList();
        public static HashMap comment_hashlist = new HashMap();
        public static HashMap comment_id_hashlist = new HashMap();

/******************************************************************/
/**************** INITIALIZATION/CLEANUP FUNCTIONS ****************/
/******************************************************************/

        
        /* initializes comment data */
        public static int initialize_comment_data(String config_file)
        {
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            return xcddefault.xcddefault_initialize_comment_data(config_file);
        }


        /* removes old/invalid comments */
        public static int cleanup_comment_data(String config_file)
        {
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            return xcddefault.xcddefault_cleanup_comment_data(config_file);
        }



/******************************************************************/
/****************** COMMENT OUTPUT FUNCTIONS **********************/
/******************************************************************/


        /* adds a new host or service comment */
        public static comments_h.comment add_new_comment(int type, int entry_type, String host_name, String svc_description, long entry_time, String author_name, String comment_data, int persistent, int source, int expires, long expire_time){
            
           comments_h.comment new_comment = null;
           
            if( type == comments_h.HOST_COMMENT )
                new_comment = add_new_host_comment(entry_type,host_name,entry_time,author_name,comment_data,persistent,source,expires,expire_time);
            else
               new_comment = add_new_service_comment(entry_type,host_name,svc_description,entry_time,author_name,comment_data,persistent,source,expires,expire_time);
            
            /* add an event to expire comment data if necessary... */
            if(expires==common_h.TRUE)
                events.schedule_new_event( blue_h.EVENT_EXPIRE_COMMENT, common_h.FALSE, expire_time, common_h.FALSE,0,null,common_h.TRUE, new Long(new_comment.comment_id) , null);
            
            return new_comment;
        }


        /* adds a new host comment */
        public static comments_h.comment  add_new_host_comment(int entry_type, String host_name, long entry_time, String author_name, String comment_data, int persistent, int source, int expires, long expire_time){
            comments_h.comment new_comment;
            
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            new_comment = xcddefault.xcddefault_add_new_host_comment(entry_type,host_name,entry_time,author_name,comment_data,persistent,source,expires,expire_time);
            
          /* send data to event broker */
          broker.broker_comment_data(broker_h.NEBTYPE_COMMENT_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,comments_h.HOST_COMMENT,entry_type,host_name,null,entry_time,author_name,comment_data,persistent,source,expires,expire_time,new_comment.comment_id,null);
            
            return new_comment;
        }

        
        /* adds a new service comment */
        public static comments_h.comment add_new_service_comment(int entry_type, String host_name, String svc_description, long entry_time, String author_name, String comment_data, int persistent, int source, int expires, long expire_time){
            comments_h.comment new_comment;
            
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            new_comment = xcddefault.xcddefault_add_new_service_comment(entry_type,host_name,svc_description,entry_time,author_name,comment_data,persistent,source,expires,expire_time );
            
          /* send data to event broker */
          broker.broker_comment_data(broker_h.NEBTYPE_COMMENT_ADD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,comments_h.SERVICE_COMMENT,entry_type,host_name,svc_description,entry_time,author_name,comment_data,persistent,source,expires,expire_time,new_comment.comment_id,null);
            
            return new_comment;
        }



/******************************************************************/
/***************** COMMENT DELETION FUNCTIONS *********************/
/******************************************************************/


        /* deletes a host or service comment */
        public static int delete_comment(int type, long comment_id)
        {
           
           int result = common_h.ERROR;
           
            /* find the comment we should remove */
            comments_h.comment temp_comment = (comments_h.comment) comment_id_hashlist.get( "" + comment_id );
           if ( temp_comment  != null ) {
              
            /* send data to event broker */
            broker.broker_comment_data(broker_h.NEBTYPE_COMMENT_DELETE,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,type,temp_comment.entry_type,temp_comment.host_name,temp_comment.service_description,temp_comment.entry_time,temp_comment.author,temp_comment.comment_data,temp_comment.persistent,temp_comment.source,temp_comment.expires,temp_comment.expire_time,comment_id,null);
              
              comment_id_hashlist.remove(temp_comment);
              comment_list.remove( temp_comment );
              
              Object o = comment_hashlist.get(temp_comment.host_name);
              
              if (o instanceof ArrayList )
              { 
                 ((ArrayList) o ).remove(temp_comment);
                 
                 if(((ArrayList)o).size() == 0)
                    comment_hashlist.remove(temp_comment.host_name );
              }
              else 
                 comment_hashlist.remove( temp_comment.host_name );
              
              result = common_h.OK;
            }

           /**** IMPLEMENTATION-SPECIFIC CALLS ****/
           if(type==comments_h.HOST_COMMENT)
              result=xcddefault.xcddefault_delete_host_comment(comment_id);
           else
              result=xcddefault.xcddefault_delete_service_comment(comment_id);

           return result;
        }
        
        /* deletes a host comment */
        public static int delete_host_comment( long comment_id)
        {
            /* delete the comment from memory */
            return delete_comment(comments_h.HOST_COMMENT,comment_id);
        }

        /* deletes a service comment */
        public static int delete_service_comment( long comment_id)
        {
            /* delete the comment from memory */
            return delete_comment( comments_h.SERVICE_COMMENT,comment_id);            
        }

        /* deletes all comments for a particular host or service */
        public static int delete_all_comments(int type, String host_name, String svc_description)
        {
            int result;
            
            if(type==comments_h.HOST_COMMENT)
                result=delete_all_host_comments(host_name);
            else
                result=delete_all_service_comments(host_name,svc_description);
            
            return result;
        }

        /* deletes all comments for a particular host */
        public static int delete_all_host_comments(String host_name)
        {
             if(host_name==null)
                return common_h.ERROR;
            
            /* delete host comments from memory */
            Object o = comment_hashlist.get( host_name );
            
            if ( o == null )
                ;
            else if ( o instanceof ArrayList )
            { 
                for ( ListIterator iter = ((ArrayList) o).listIterator() ; iter.hasNext(); ) {
                    comments_h.comment temp_comment = (comments_h.comment) iter.next();
                    if ( temp_comment.comment_type == comments_h.HOST_COMMENT )
                    {
                        delete_comment(comments_h.HOST_COMMENT,temp_comment.comment_id);
                    }
                }
            } else if ( ((comments_h.comment) o ).comment_type == comments_h.HOST_COMMENT ) {
                delete_comment(comments_h.HOST_COMMENT,((comments_h.comment) o).comment_id);
            }
            
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            return xcddefault.xcddefault_delete_all_host_comments(host_name);
        }

        
        /* deletes all comments for a particular service */
        public static int delete_all_service_comments(String host_name, String svc_description){
            
            if(host_name==null || svc_description == null )
                return common_h.ERROR;
            
            /* service host comments from memory */
            Object o = comment_hashlist.get( host_name );
            
            if ( o == null )
                ;
            else if ( o instanceof ArrayList ) { 
                for ( ListIterator iter = ((ArrayList) o).listIterator() ; iter.hasNext(); ) {
                    comments_h.comment temp_comment = (comments_h.comment) iter.next();
                    if ( temp_comment.comment_type == comments_h.SERVICE_COMMENT && temp_comment.service_description.equals( svc_description ) ) {
                        delete_comment(comments_h.SERVICE_COMMENT,temp_comment.comment_id);
                    }
                }
            } else if ( ((comments_h.comment) o ).comment_type == comments_h.SERVICE_COMMENT ) {
                delete_comment(comments_h.SERVICE_COMMENT,((comments_h.comment) o).comment_id);
            }
            
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            return xcddefault.xcddefault_delete_all_service_comments(host_name,svc_description);
        }
        
        /* checks for an expired comment (and removes it) */
        public static int check_for_expired_comment(long comment_id){
           
           /* find the comment we should expire */
           comments_h.comment temp_comment = (comments_h.comment) comment_id_hashlist.get( "" + comment_id );
           if ( temp_comment  != null ) {
              
              /* delete the now expired comment */
              if( temp_comment.expires==common_h.TRUE && temp_comment.expire_time< utils.currentTimeInSeconds() ){
                 delete_comment(temp_comment.comment_type,comment_id);
              }
           }
           
           return common_h.OK;
        }

/******************************************************************/
/********************** INPUT FUNCTIONS ***************************/
/******************************************************************/

        
        public static int read_comment_data(String main_config_file){
            int result;
            
            /**** IMPLEMENTATION-SPECIFIC CALLS ****/
            result=xcddefault.xcddefault_read_comment_data(main_config_file);
            
            return result;
        }



/******************************************************************/
/****************** CHAINED HASH FUNCTIONS ************************/
/******************************************************************/

/* adds comment to hash list in memory */
public static boolean add_comment_to_hashlist(comments_h.comment new_comment){

    /* initialize hash list */
    if ( new_comment == null )
        return false;
    
    if ( comment_hashlist.containsKey( new_comment.host_name )) {
        Object o = comment_hashlist.get(new_comment.host_name );
        if ( o instanceof ArrayList ) {
            ((ArrayList) o).add( new_comment );
        } else {
            ArrayList array = new ArrayList();
            array.add( o );
            array.add( new_comment );
            comment_hashlist.put( new_comment.host_name , array );
        }
    } else {
       comment_hashlist.put( new_comment.host_name, new_comment );
    }
    
    return true;
}



/******************************************************************/
/******************** ADDITION FUNCTIONS **************************/
/******************************************************************/


/* adds a host comment to the list in memory */
public static comments_h.comment add_host_comment(int entry_type, String host_name, long entry_time, String author, String comment_data,  long comment_id, int persistent, int expires, long expire_time, int source){
    return add_comment(comments_h.HOST_COMMENT,entry_type,host_name,null,entry_time,author,comment_data,comment_id,persistent,expires,expire_time,source);
}

/* adds a service comment to the list in memory */
public static comments_h.comment add_service_comment(int entry_type, String host_name, String svc_description, long entry_time, String author, String comment_data,  long comment_id, int persistent, int expires, long expire_time, int source){
    return add_comment(comments_h.SERVICE_COMMENT,entry_type,host_name,svc_description,entry_time,author,comment_data,comment_id,persistent,expires,expire_time,source);
}

/* adds a comment to the list in memory */
public static comments_h.comment add_comment(int comment_type, int entry_type, String host_name, String svc_description, long entry_time, String author, String comment_data, long comment_id, int persistent, int expires, long expire_time, int source)
{
    
    /* make sure we have the data we need */
    if(host_name==null || author==null || comment_data==null || (comment_type==comments_h.SERVICE_COMMENT && svc_description==null))
        return null;
    
    /* allocate memory for the comment */
    comments_h.comment new_comment = new comments_h.comment();
    new_comment.comment_id = comment_id;
    new_comment.host_name=host_name;
    new_comment.service_description=svc_description;
    new_comment.author=author;
    new_comment.comment_data=comment_data;
    new_comment.comment_type=comment_type;
    new_comment.entry_type=entry_type;
    new_comment.source=source;
    new_comment.entry_time=entry_time;
    new_comment.comment_id=comment_id;
    new_comment.persistent=(persistent==common_h.TRUE)?common_h.TRUE:common_h.FALSE;
    new_comment.expires=(expires==common_h.TRUE)?common_h.TRUE:common_h.FALSE;
    new_comment.expire_time=expire_time;
    
    /* add comment to hash list */
    if(!add_comment_to_hashlist(new_comment))
        return null;
    
    /* add new comment to comment list, sorted by comment id */
    comment_id_hashlist.put( "" + comment_id, new_comment );
    comment_list.add( new_comment );

    if(blue.is_core == true)
    {
       /* send data to event broker */
       broker.broker_comment_data(broker_h.NEBTYPE_COMMENT_LOAD,broker_h.NEBFLAG_NONE,broker_h.NEBATTR_NONE,comment_type,entry_type,host_name,svc_description,entry_time,author,comment_data,persistent,source,expires,entry_time,comment_id,null);
    }
    
    return new_comment;
}




/******************************************************************/
/********************* CLEANUP FUNCTIONS **************************/
/******************************************************************/

/* frees memory allocated for the comment data */
public static void free_comment_data(){
    comment_list.clear();
    comment_hashlist.clear();
    comment_id_hashlist.clear();
    return;
}

/******************************************************************/
/********************* UTILITY FUNCTIONS **************************/
/******************************************************************/

/* get the number of comments associated with a particular host */
public static int number_of_host_comments(String host_name){
    Object o = comment_hashlist.get( host_name );
    if ( o == null )
        return 0;
    else if ( o instanceof ArrayList )
        return ((ArrayList) o).size();
    else 
        return 1;
}

/* get the number of comments associated with a particular service */
public static int number_of_service_comments(String host_name, String svc_description){
    Object o = comment_hashlist.get( host_name );
    if ( o == null )
        return 0;
    else if ( o instanceof ArrayList ) {
        int counter = 0;
        for ( ListIterator iter = ((ArrayList) o).listIterator(); iter.hasNext();) {
            String service_description = ((comments_h.comment)iter.next()).service_description;
            if ( service_description != null && service_description.equals( svc_description )  )
                counter++;
        }
        return counter;
    } else if ( ((comments_h.comment)o).service_description != null && ((comments_h.comment)o).service_description.equals( svc_description )  ) 
        return 1;
    else 
        return 0;
}

/******************************************************************/
/********************* TRAVERSAL FUNCTIONS ************************/
/******************************************************************/

public static ArrayList get_comment_list_by_host ( String host_name ) {
    Object o = comment_hashlist.get(host_name);
    if ( o == null )
        return null;
    else if ( o instanceof ArrayList )
        return new ArrayList( (Collection) o );
    else {
        ArrayList result = new ArrayList();
        result.add( o );
        return result;
    }
}

/******************************************************************/
/********************** SEARCH FUNCTIONS **************************/
/******************************************************************/

/* find a service comment by id */
public static comments_h.comment find_service_comment(long comment_id){
    return find_comment(comment_id,comments_h.SERVICE_COMMENT);
}


/* find a host comment by id */
public static comments_h.comment find_host_comment(long comment_id){
    return find_comment(comment_id,comments_h.HOST_COMMENT);
}


/* find a comment by id */
public static comments_h.comment find_comment(long comment_id, int comment_type){
    
    return (comments_h.comment) comment_id_hashlist.get( "" + comment_id );
}


}
