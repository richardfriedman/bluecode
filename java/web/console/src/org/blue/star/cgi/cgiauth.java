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

package org.blue.star.cgi;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.blue.star.common.objects;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

//#include "../include/common.h"
//#include "../include/config.h"
//#include "../include/objects.h"
//
//#include "../include/cgiutils.h"
//#include "../include/cgiauth.h"

//extern char            main_config_file[MAX_FILENAME_LENGTH];
//
//extern hostgroup       *hostgroup_list;
//extern servicegroup    *servicegroup_list;
//
//extern int             use_authentication;
//
//extern int             hosts_have_been_read;
//extern int             hostgroups_have_been_read;
//extern int             contactgroups_have_been_read;
//extern int             contacts_have_been_read;
//extern int             services_have_been_read;
//extern int             serviceescalations_have_been_read;
//extern int             hostescalations_have_been_read;

public class cgiauth {

/* get current authentication information */
public static int get_authentication_information(cgiauth_h.authdata authinfo){
	cgiutils_h.mmapfile thefile;
	String input=null;
	String temp_ptr;
	int needed_options;
    StringTokenizer st;

	if(authinfo==null)
		return common_h.ERROR;

	/* make sure we have read in all the configuration information we need for the authentication routines... */
	needed_options=0;
	if(cgiutils.hosts_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_HOSTS;
	if(cgiutils.hostgroups_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_HOSTGROUPS;
	if(cgiutils.contactgroups_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_CONTACTGROUPS;
	if(cgiutils.contacts_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_CONTACTS;
	if(cgiutils.services_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_SERVICES;
	if(cgiutils.serviceescalations_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_SERVICEESCALATIONS;
	if(cgiutils.hostescalations_have_been_read==common_h.FALSE)
		needed_options|=common_h.READ_HOSTESCALATIONS;
	if(needed_options>0)
		cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,needed_options);

	/* initial values... */
	authinfo.authorized_for_all_hosts=common_h.FALSE;
	authinfo.authorized_for_all_host_commands=common_h.FALSE;
	authinfo.authorized_for_all_services=common_h.FALSE;
	authinfo.authorized_for_all_service_commands=common_h.FALSE;
	authinfo.authorized_for_system_information=common_h.FALSE;
	authinfo.authorized_for_system_commands=common_h.FALSE;
	authinfo.authorized_for_configuration_information=common_h.FALSE;

	/* grab username from the environment... */
	temp_ptr=System.getenv("REMOTE_USER");
	if(temp_ptr==null){
		authinfo.username="";
		authinfo.authenticated=common_h.FALSE;
	        }
	else
	{
		authinfo.username = temp_ptr.trim();
        
		if(authinfo.username.length() == 0)
			authinfo.authenticated=common_h.FALSE;
		else
			authinfo.authenticated=common_h.TRUE;
	}

	/* read in authorization override vars from config file... */
	if((thefile=cgiutils.mmap_fopen( cgiutils.get_cgi_config_location()))!=null){

		while(true){

			/* free memory */

			/* read the next line */
			if((input=cgiutils.mmap_fgets(thefile))==null)
				break;

			input = input.trim();

			/* we don't have a username yet, so fake the authentication if we find a default username defined */
			if( authinfo.username.length() == 0 && input.startsWith( "default_user_name=") ){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                temp_ptr = temp_ptr.split ( "[,]")[0];
				authinfo.username = temp_ptr.trim();
				if( authinfo.username.length() == 0 )
					authinfo.authenticated=common_h.FALSE;
				else
					authinfo.authenticated=common_h.TRUE;
			        }

			else if(input.startsWith( "authorized_for_all_hosts=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
				for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_all_hosts=common_h.TRUE;
				        }
			        }
			else if(input.startsWith("authorized_for_all_services=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_all_services=common_h.TRUE;
				        }
			        }
			else if(input.startsWith("authorized_for_system_information=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_system_information=common_h.TRUE;
				        }
			        }
			else if(input.startsWith("authorized_for_configuration_information=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_configuration_information=common_h.TRUE;
				        }
			        }
			else if(input.startsWith("authorized_for_all_host_commands=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_all_host_commands=common_h.TRUE;
				        }
			        }
			else if(input.startsWith("authorized_for_all_service_commands=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_all_service_commands=common_h.TRUE;
				        }
			        }
			else if(input.startsWith("authorized_for_system_commands=")){
                temp_ptr = input.substring( input.indexOf( "=") + 1 );
                for ( String token : temp_ptr.split ( "[,]") ) {
					if(token.equals( authinfo.username) || token.equals( "*" ))
						authinfo.authorized_for_system_commands=common_h.TRUE;
				        }
			        }
		        }

		/* free memory and close the file */
		cgiutils.mmap_fclose(thefile);
	        }

	if(authinfo.authenticated==common_h.TRUE)
		return common_h.OK;
	else
		return common_h.ERROR;
        }



/* check if user is authorized to view information about a particular host */
public static int is_authorized_for_host(objects_h.host hst, cgiauth_h.authdata authinfo){
	objects_h.contact temp_contact;

	if(hst==null)
		return common_h.FALSE;

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	/* if this user is authorized for all hosts, they are for this one... */
	if(is_authorized_for_all_hosts(authinfo)==common_h.TRUE)
		return common_h.TRUE;

	/* find the contact */
	temp_contact= objects.find_contact(authinfo.username);

	/* see if this user is a contact for the host */
	if(objects.is_contact_for_host(hst,temp_contact) == true )
		return common_h.TRUE;

	/* see if this user is an escalated contact for the host */
	if( objects.is_escalated_contact_for_host(hst,temp_contact)==common_h.TRUE)
		return common_h.TRUE;

	return common_h.FALSE;
        }


/* check if user is authorized to view information about all hosts in a particular hostgroup */
public static int is_authorized_for_hostgroup(objects_h.hostgroup hg, cgiauth_h.authdata authinfo){
    objects_h.host temp_host;

	if(hg==null)
		return common_h.FALSE;

	/* CHANGED in 2.0 - user must be authorized for ALL hosts in a hostgroup, not just one */
	/* see if user is authorized for all hosts in the hostgroup */
	for ( objects_h.hostgroupmember temp_hostgroupmember : (ArrayList<objects_h.hostgroupmember>)  hg.members ) {
		temp_host=objects.find_host(temp_hostgroupmember.host_name);
		if(is_authorized_for_host(temp_host,authinfo)==common_h.FALSE)
			return common_h.FALSE;
	        }

	return common_h.TRUE;
        }



/* check if user is authorized to view information about all services in a particular servicegroup */
public static int is_authorized_for_servicegroup(objects_h.servicegroup sg, cgiauth_h.authdata authinfo){
    objects_h.service temp_service;

	if(sg==null)
		return common_h.FALSE;

	/* see if user is authorized for all services in the servicegroup */
	for ( objects_h.servicegroupmember temp_servicegroupmember : (ArrayList<objects_h.servicegroupmember>)  sg.members ) {
		temp_service=objects.find_service(temp_servicegroupmember.host_name,temp_servicegroupmember.service_description);
		if(is_authorized_for_service(temp_service,authinfo)==common_h.FALSE)
			return common_h.FALSE;
	        }

	return common_h.TRUE;
        }


/* check if user is authorized to view information about a particular service */
public static int is_authorized_for_service(objects_h.service svc, cgiauth_h.authdata authinfo){
	objects_h.host temp_host;
	objects_h.contact temp_contact;

	if(svc==null)
		return common_h.FALSE;

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	/* if this user is authorized for all services, they are for this one... */
	if(is_authorized_for_all_services(authinfo)==common_h.TRUE)
		return common_h.TRUE;

	/* find the host */
	temp_host=objects.find_host(svc.host_name);
	if(temp_host==null)
		return common_h.FALSE;

	/* if this user is authorized for this host, they are for all services on it as well... */
	if(is_authorized_for_host(temp_host,authinfo)==common_h.TRUE)
		return common_h.TRUE;

	/* find the contact */
	temp_contact=objects.find_contact(authinfo.username);

	/* see if this user is a contact for the service */
	if(objects.is_contact_for_service(svc,temp_contact)== true)
		return common_h.TRUE;

	/* see if this user is an escalated contact for the service */
	if(objects.is_escalated_contact_for_service(svc,temp_contact)==common_h.TRUE)
		return common_h.TRUE;

	return common_h.FALSE;
        }


/* check if current user is authorized to view information on all hosts */
public static int is_authorized_for_all_hosts(cgiauth_h.authdata authinfo){

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	return authinfo.authorized_for_all_hosts;
        }


/* check if current user is authorized to view information on all service */
public static int is_authorized_for_all_services(cgiauth_h.authdata authinfo){

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	return authinfo.authorized_for_all_services;
        }


/* check if current user is authorized to view system information */
public static int is_authorized_for_system_information(cgiauth_h.authdata authinfo){

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	return authinfo.authorized_for_system_information;
        }


/* check if current user is authorized to view configuration information */
public static int is_authorized_for_configuration_information(cgiauth_h.authdata authinfo){

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	return authinfo.authorized_for_configuration_information;
        }


/* check if current user is authorized to issue system commands */
public static int is_authorized_for_system_commands(cgiauth_h.authdata authinfo){

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	return authinfo.authorized_for_system_commands;
        }


/* check is the current user is authorized to issue commands relating to a particular service */
public static int is_authorized_for_service_commands(objects_h.service svc, cgiauth_h.authdata authinfo){
	objects_h.host temp_host;
    objects_h.contact temp_contact;

	if(svc==null)
		return common_h.FALSE;

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	/* the user is authorized if they have rights to the service */
	if(is_authorized_for_service(svc,authinfo)==common_h.TRUE){

		/* find the host */
		temp_host=objects.find_host(svc.host_name);
		if(temp_host==null)
			return common_h.FALSE;

		/* find the contact */
		temp_contact=objects.find_contact(authinfo.username);

		/* see if this user is a contact for the host */
		if(objects.is_contact_for_host(temp_host,temp_contact)==true)
			return common_h.TRUE;

		/* see if this user is an escalated contact for the host */
		if(objects.is_escalated_contact_for_host(temp_host,temp_contact)==common_h.TRUE)
			return common_h.TRUE;

		/* this user is a contact for the service, so they have permission... */
		if(objects.is_contact_for_service(svc,temp_contact)==true)
			return common_h.TRUE;

		/* this user is an escalated contact for the service, so they have permission... */
		if( objects.is_escalated_contact_for_service(svc,temp_contact)==common_h.TRUE)
			return common_h.TRUE;

		/* this user is not a contact for the host, so they must have been given explicit permissions to all service commands */
		if(authinfo.authorized_for_all_service_commands==common_h.TRUE)
			return common_h.TRUE;
	        }

	return common_h.FALSE;
        }


/* check is the current user is authorized to issue commands relating to a particular host */
public static int is_authorized_for_host_commands(objects_h.host hst, cgiauth_h.authdata authinfo){
    objects_h.contact temp_contact;

	if(hst==null)
		return common_h.FALSE;

	/* if we're not using authentication, fake it */
	if(cgiutils.use_authentication==common_h.FALSE)
		return common_h.TRUE;

	/* if this user has not authenticated return error */
	if(authinfo.authenticated==common_h.FALSE)
		return common_h.FALSE;

	/* the user is authorized if they have rights to the host */
	if(is_authorized_for_host(hst,authinfo)==common_h.TRUE){

		/* find the contact */
		temp_contact=objects.find_contact(authinfo.username);

		/* this user is a contact for the host, so they have permission... */
		if(objects.is_contact_for_host(hst,temp_contact)==true)
			return common_h.TRUE;

		/* this user is an escalated contact for the host, so they have permission... */
		if(objects.is_escalated_contact_for_host(hst,temp_contact)==common_h.TRUE)
			return common_h.TRUE;

		/* this user is not a contact for the host, so they must have been given explicit permissions to all host commands */
		if(authinfo.authorized_for_all_host_commands==common_h.TRUE)
			return common_h.TRUE;
	        }

	return common_h.FALSE;
        }


}