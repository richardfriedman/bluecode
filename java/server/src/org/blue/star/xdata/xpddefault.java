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

package org.blue.star.xdata;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.base.blue;
import org.blue.star.base.events;
import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;

public class xpddefault {
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.xdata.xpddefault");
    private static String cn = "org.blue.xdata.xpddefault";
    
    public static int     xpddefault_perfdata_timeout;
    
    public static String xpddefault_host_perfdata_command=null;
    public static String xpddefault_service_perfdata_command=null;
    
    public static String xpddefault_host_perfdata_file_template=null;
    public static String xpddefault_service_perfdata_file_template=null;
    
    public static String xpddefault_host_perfdata_file=null;
    public static String xpddefault_service_perfdata_file=null;
    
    public static int     xpddefault_host_perfdata_file_append=common_h.TRUE;
    public static int     xpddefault_service_perfdata_file_append=common_h.TRUE;
    
    public static long xpddefault_host_perfdata_file_processing_interval=0L;
    public static long xpddefault_service_perfdata_file_processing_interval=0L;
    
    public static String xpddefault_host_perfdata_file_processing_command;
    public static String xpddefault_service_perfdata_file_processing_command;
    
    public static FileWriter xpddefault_host_perfdata_fp=null;
    public static FileWriter xpddefault_service_perfdata_fp=null;
    
    
    /******************************************************************/
    /************** INITIALIZATION & CLEANUP FUNCTIONS ****************/
    /******************************************************************/
    
    /* initializes performance data */
    public static int xpddefault_initialize_performance_data(String config_file)
    {
//      char buffer[MAX_INPUT_BUFFER];
//      char temp_buffer[MAX_INPUT_BUFFER];
        String temp_command_name;
        long current_time;
        
        current_time = utils.currentTimeInSeconds();
        
        /* default values */
        xpddefault_perfdata_timeout=blue_h.DEFAULT_PERFDATA_TIMEOUT;
        xpddefault_host_perfdata_command=null;
        xpddefault_service_perfdata_command=null;
        xpddefault_host_perfdata_file_template=null;
        xpddefault_service_perfdata_file_template=null;
        xpddefault_host_perfdata_file=null;
        xpddefault_service_perfdata_file=null;
        xpddefault_host_perfdata_fp=null;
        xpddefault_service_perfdata_fp=null;
        xpddefault_host_perfdata_file_processing_interval=0L;
        xpddefault_service_perfdata_file_processing_interval=0L;
        xpddefault_host_perfdata_file_processing_command=null;
        xpddefault_service_perfdata_file_processing_command=null;
        
        /* grab config info from main config file */
        /* 
         * Rob 15/01/07 - We've already had the chance to do this when reading the main config
         * file. Is there any reason why we are delegating it until this stage? Seems 
         * pointless to read the same config file twice.
         */
        
        xpddefault_grab_config_info(config_file);
        
        /* make sure we have some templates defined */
        if(xpddefault_host_perfdata_file_template==null)
            xpddefault_host_perfdata_file_template=xpddefault_h.DEFAULT_HOST_PERFDATA_FILE_TEMPLATE;
        if(xpddefault_service_perfdata_file_template==null)
            xpddefault_service_perfdata_file_template=xpddefault_h.DEFAULT_SERVICE_PERFDATA_FILE_TEMPLATE;
        
        /* process special chars in templates */
        xpddefault_host_perfdata_file_template = xpddefault_preprocess_file_templates(xpddefault_host_perfdata_file_template);
        xpddefault_service_perfdata_file_template = xpddefault_preprocess_file_templates(xpddefault_service_perfdata_file_template);
        
        /* open the performance data files */
        xpddefault_open_host_perfdata_file();
        xpddefault_open_service_perfdata_file();
        
        /* verify that performance data commands are valid */
        if(xpddefault_host_perfdata_command!=null){
            
            /* get the command name, leave any arguments behind */
            String[] split = xpddefault_host_perfdata_command.split ( "\\!", 2);
            
            if(objects.find_command(split[0])==null)
                logger.warn(  "Warning: Host performance command '"+split[0]+"' was not found - host performance data will not be processed!");
        }
        
        if(xpddefault_service_perfdata_command!=null){
            
            /* get the command name, leave any arguments behind */
            String[] split = xpddefault_service_perfdata_command.split ( "\\!", 2);
            
            if(objects.find_command(split[0])==null)
                logger.warn( "Warning: Service performance command '"+split[0]+"' was not found - service performance data will not be processed!");
        }
        
        if(xpddefault_host_perfdata_file_processing_command!=null){
            
            /* get the command name, leave any arguments behind */
            String[] split = xpddefault_host_perfdata_file_processing_command.split ( "\\!", 2);
            
            if(objects.find_command(split[0])==null)
                logger.warn( "Warning: Host performance file processing command '"+split[0]+"' was not found - host performance data file will not be processed!");
        }
        
        if(xpddefault_service_perfdata_file_processing_command!=null){
            
            /* get the command name, leave any arguments behind */
            String[] split = xpddefault_service_perfdata_file_processing_command.split ( "\\!", 2);
            
            if(objects.find_command(split[0])==null)
                logger.warn( "Warning: Service performance file processing command '"+split[0]+"' was not found - service performance data file will not be processed!");
        }
        
        /* periodically process the host perfdata file */
        /* 
         * Does this by creating a new instance of Runnable class in which the run method simply processes the host_perfdata_file.
         * This object is then scheduled in the event queue to be run from now + perfdata_processing_interval.
         * It is also scheduled as a repeatable event and the interval between repeats is set at this point.  
         */
        
        if(xpddefault_host_perfdata_file_processing_interval>0 && xpddefault_host_perfdata_file_processing_command!=null) 
        {
            Runnable runLater = new Runnable ()
            {
            	public void run()
            	{
            		xpddefault.xpddefault_process_host_perfdata_file();
            	}
            };
            
            events.schedule_new_event( blue_h.EVENT_USER_FUNCTION, common_h.TRUE, current_time+xpddefault_host_perfdata_file_processing_interval, common_h.TRUE, xpddefault_host_perfdata_file_processing_interval, null, common_h.TRUE, runLater, null);
        }
        
        
        /* periodically process the service perfdata file */
        if(xpddefault_service_perfdata_file_processing_interval>0 && xpddefault_service_perfdata_file_processing_command!=null)
        {
            Runnable runLater = new Runnable ()
            {  
            	public void run()
            	{
            		xpddefault.xpddefault_process_service_perfdata_file();
            	}
            };
            
            events.schedule_new_event(blue_h.EVENT_USER_FUNCTION,common_h.TRUE,current_time+xpddefault_service_perfdata_file_processing_interval,common_h.TRUE,xpddefault_service_perfdata_file_processing_interval,null,common_h.TRUE,runLater,null);
        }
        
        /* save the host perf data file macro */
        blue.macro_x[blue_h.MACRO_HOSTPERFDATAFILE] = xpddefault_host_perfdata_file;
        
        /* save the service perf data file macro */
        blue.macro_x[ blue_h.MACRO_SERVICEPERFDATAFILE] = xpddefault_service_perfdata_file;
        
        return common_h.OK;
    }
    
    
    /* grabs configuration information from main config file */
    public static int xpddefault_grab_config_info(String config_file)
    {
        int error=common_h.FALSE;
        
        /* open the config file for reading */
        blue_h.mmapfile thefile = utils.file_functions.mmap_fopen( config_file );
        if( thefile == null )
            return common_h.ERROR;
        
        /* read in all lines from the config file */
        /* read in all lines from the main config file */
        while( true )
        {
            
            /* read the next line */
            String input = utils.file_functions.mmap_fgets(thefile);
            if ( input == null )
                break;
            
            input = input.trim();
            
            /* skip blank lines and comments */
            if( input.length() == 0 || input.charAt(0) =='#' )
                continue;
            
            /* get the variable name */
            String[] split = input.split ("=" ,2 );
            if ( split.length != 2 ) {
                error=common_h.TRUE;
                break;
            }
            
            String variable = split[0];
            String value = split[1].trim();
            
            if(variable.equals("perfdata_timeout"))
            {
                xpddefault_perfdata_timeout=atoi(value);
                
                if(xpddefault_perfdata_timeout<=0){
                    error=common_h.TRUE;
                    break;
                }
            }
            
            else if(variable.equals("host_perfdata_command"))
                xpddefault_host_perfdata_command=value;
            
            else if(variable.equals("service_perfdata_command"))
                xpddefault_service_perfdata_command=value;
            
            else if(variable.equals("host_perfdata_file_template"))
                xpddefault_host_perfdata_file_template=value;
            
            else if(variable.equals("service_perfdata_file_template"))
                xpddefault_service_perfdata_file_template=value;
            
            else if(variable.equals("host_perfdata_file"))
                xpddefault_host_perfdata_file=value;
            
            else if(variable.equals("service_perfdata_file"))
                xpddefault_service_perfdata_file=value;
            
            else if(variable.equals("host_perfdata_file_mode"))
            {
                if( value.indexOf("w") >= 0 )
                    xpddefault_host_perfdata_file_append=common_h.FALSE;
                else
                    xpddefault_host_perfdata_file_append=common_h.TRUE;
            }
            
            else if(variable.equals("service_perfdata_file_mode")){
                if( value.indexOf("w") >= 0 )
                    xpddefault_service_perfdata_file_append=common_h.FALSE;
                else
                    xpddefault_service_perfdata_file_append=common_h.TRUE;
            }
            
            else if(variable.equals("host_perfdata_file_processing_interval"))
                xpddefault_host_perfdata_file_processing_interval=strtoul(value,null,0);
            
            else if(variable.equals("service_perfdata_file_processing_interval"))
                xpddefault_service_perfdata_file_processing_interval=strtoul(value,null,0);
            
            else if(variable.equals("host_perfdata_file_processing_command"))
                xpddefault_host_perfdata_file_processing_command=value;
            
            else if(variable.equals("service_perfdata_file_processing_command"))
                xpddefault_service_perfdata_file_processing_command=value;
        }
        
        /* free memory and close the file */
        utils.file_functions.mmap_fclose(thefile);
        
        return common_h.OK;
    }
    
    
    /* cleans up performance data */
    public static int xpddefault_cleanup_performance_data(String config_file){
        
        /* free memory */
        xpddefault_free_memory();
        
        /* close the files */
        xpddefault_close_host_perfdata_file();
        xpddefault_close_service_perfdata_file();
        
        return common_h.OK;
    }
    
    
    /* frees allocated memory */
    public static int xpddefault_free_memory(){
        
        /* free memory */
        xpddefault_host_perfdata_command=null;
        xpddefault_service_perfdata_command=null;
        xpddefault_host_perfdata_file_template=null;
        xpddefault_service_perfdata_file_template=null;
        xpddefault_host_perfdata_file=null;
        xpddefault_service_perfdata_file=null;
        xpddefault_host_perfdata_file_processing_command=null;
        xpddefault_service_perfdata_file_processing_command=null;
        
        return common_h.OK;
    }
    
    
    
    
    /******************************************************************/
    /****************** PERFORMANCE DATA FUNCTIONS ********************/
    /******************************************************************/
    
    
    /* updates service performance data */
    public static int xpddefault_update_service_performance_data(objects_h.service svc){
        
        /* run the performance data command */
        xpddefault_run_service_performance_data_command(svc);
        
        /* update the performance data file */
        xpddefault_update_service_performance_data_file(svc);
        
        return common_h.OK;
    }
    
    
    /* updates host performance data */
    public static int xpddefault_update_host_performance_data(objects_h.host hst){
        
        /* run the performance data command */
        xpddefault_run_host_performance_data_command(hst);
        
        /* update the performance data file */
        xpddefault_update_host_performance_data_file(hst);
        
        return common_h.OK;
    }
    
    
    
    
    /******************************************************************/
    /************** PERFORMANCE DATA COMMAND FUNCTIONS ****************/
    /******************************************************************/
    
    
    /* runs the service performance data command */
    public static int xpddefault_run_service_performance_data_command(objects_h.service svc)
    {
        String raw_command_line;
        String processed_command_line;
//      char temp_buffer[MAX_INPUT_BUFFER];
        objects_h.host temp_host;
        int macro_options=blue_h.STRIP_ILLEGAL_MACRO_CHARS|blue_h.ESCAPE_MACRO_CHARS;
        
        /* we don't have a command */
        if(xpddefault_service_perfdata_command==null)
            return common_h.OK;
        
        /* find the associated host */
        temp_host=objects.find_host(svc.host_name);
        
        /* update service macros */
        utils.clear_volatile_macros();
        utils.grab_host_macros(temp_host);
        utils.grab_service_macros(svc);
        utils.grab_summary_macros(null);
        
        /* get the raw command line */
        raw_command_line = utils.get_raw_command_line(xpddefault_service_perfdata_command,macro_options).trim();
        
        logger.debug("\tRaw service performance data command line: " + raw_command_line);
        
        /* process any macros in the raw command line */
        processed_command_line = utils.process_macros(raw_command_line,macro_options);
        
        logger.debug("\tProcessed service performance data command line: " + processed_command_line);
        
        /* run the command */
        utils.system_result result = utils.my_system( processed_command_line, xpddefault_perfdata_timeout );
        
        /* check to see if the command timed out */
        if( result.early_timeout==true)
            logger.warn( "Warning: Service performance data command '"+processed_command_line+"' for service '"+svc.description+"' on host '"+svc.host_name+"' timed out after "+xpddefault_perfdata_timeout+" seconds");
        
        return common_h.OK;
    }
    
    
    /* runs the host performance data command */
    public static int xpddefault_run_host_performance_data_command(objects_h.host hst){
        String raw_command_line;
        String processed_command_line;
        boolean early_timeout=false;
        double exectime;
        int macro_options= blue_h.STRIP_ILLEGAL_MACRO_CHARS | blue_h.ESCAPE_MACRO_CHARS;
        
        /* we don't have a command */
        if(xpddefault_host_perfdata_command==null)
            return common_h.OK;
        
        /* update host macros */
        utils.clear_volatile_macros();
        utils.grab_host_macros(hst);
        utils.grab_summary_macros(null);
        
        /* get the raw command line */
        raw_command_line = utils.get_raw_command_line(xpddefault_service_perfdata_command,macro_options).trim();
        
        logger.debug("\tRaw host performance data command line: " + raw_command_line);
        
        /* process any macros in the raw command line */
        processed_command_line = utils.process_macros(raw_command_line,macro_options);
        
        logger.debug("\tProcessed host performance data command line: " + processed_command_line);
        
        /* run the command */
        utils.system_result result = utils.my_system( processed_command_line, xpddefault_perfdata_timeout );
        
        /* check to see if the command timed out */
        if( result.early_timeout==true){
            logger.warn( "Warning: Host performance data command '"+processed_command_line+"' for host '"+hst.name+"' timed out after "+xpddefault_perfdata_timeout+" seconds");
        }
        
        return common_h.OK;
    }
    
    
    
    /******************************************************************/
    /**************** FILE PERFORMANCE DATA FUNCTIONS *****************/
    /******************************************************************/
    
    /* open the host performance data file for writing */
    public static int xpddefault_open_host_perfdata_file(){
        
        if(xpddefault_host_perfdata_file!=null){
            
            try {
                xpddefault_host_perfdata_fp = new FileWriter( xpddefault_host_perfdata_file,  (xpddefault_host_perfdata_file_append==common_h.TRUE)?true:false);
            } catch (IOException ioE ) {
                logger.warn( "Warning: File '"+xpddefault_host_perfdata_file+"' could not be opened - host performance data will not be written to file!");
            }
        }
        return common_h.OK;
    }
    
    
    /* open the service performance data file for writing */
    public static int xpddefault_open_service_perfdata_file(){
        
        if(xpddefault_service_perfdata_file!=null){
            
            try {
                xpddefault_service_perfdata_fp = new FileWriter( xpddefault_service_perfdata_file,  (xpddefault_host_perfdata_file_append==common_h.TRUE)?true:false);
            } catch (IOException ioE ) {
                logger.warn( "Warning: File '"+xpddefault_service_perfdata_file+"' could not be opened - service performance data will not be written to file!");
            }
        }
        
        return common_h.OK;
    }
    
    
    /* close the host performance data file */
    public static int xpddefault_close_host_perfdata_file(){
        
        
        if(xpddefault_host_perfdata_fp!=null)
            try {xpddefault_host_perfdata_fp.close();} catch (IOException ioE ) {}
            
            return common_h.OK;
    }
    
    
    /* close the service performance data file */
    public static int xpddefault_close_service_perfdata_file(){
        
        if(xpddefault_service_perfdata_fp!=null)
            try {xpddefault_service_perfdata_fp.close();} catch (IOException ioE ) {}
            
            return common_h.OK;
    }
    
    
    /* processes delimiter characters in templates */
    public static String xpddefault_preprocess_file_templates(String _template){
        if(_template==null)
            return _template;
        
        int x=0;
        int y=0;
        StringBuffer tempbuf = new StringBuffer();
        char[] template = _template.toCharArray();
        
        for(x=0,y=0;x<template.length;x++,y++){
            if(template[x]=='\\'){
                if(template[x+1]=='t'){
                    tempbuf.append('\t');
                    x++;
                }
                else if(template[x+1]=='r'){
                    tempbuf.append('\r');
                    x++;
                }
                else if(template[x+1]=='n'){
                    tempbuf.append('\n');
                    x++;
                }
                else
                    tempbuf.append(template[x]);
            }
            else
                tempbuf.append(template[x]);
        }
        
        return tempbuf.toString();
    }
    
    
    /* updates service performance data file */
    public static int xpddefault_update_service_performance_data_file( objects_h.service svc){
        
        /* we don't have a file to write to*/
        if(xpddefault_service_perfdata_fp==null || xpddefault_service_perfdata_file_template==null)
            return common_h.OK;
        
        /* find the associated host */
        objects_h.host temp_host= objects.find_host(svc.host_name);
        
        /* update service macros */
        utils.clear_volatile_macros();
        utils.grab_host_macros(temp_host);
        utils.grab_service_macros(svc);
        utils.grab_summary_macros(null);
        
        /* get the raw line to write */
        String raw_output = xpddefault_service_perfdata_file_template;
        
        logger.debug("\tRaw service performance data output: " + raw_output);
        
        /* process any macros in the raw output line */
        raw_output = utils.process_macros(raw_output,0);
        
        logger.debug("\tProcessed service performance data output: " + raw_output );
        
        /* write the processed output line containing performance data to the service perfdata file */
        try
        {
            xpddefault_service_perfdata_fp.write( raw_output );
            xpddefault_service_perfdata_fp.write( "\n" );
            xpddefault_service_perfdata_fp.flush();
        }
        catch (IOException ioE)
        {
            logger.warn("warning: " + ioE.getMessage(), ioE);
        }
        
        return common_h.OK;
    }
    
    
    /* updates host performance data file */
    public static int xpddefault_update_host_performance_data_file(objects_h.host hst){
        /* we don't have a host perfdata file */
        if(xpddefault_host_perfdata_fp==null || xpddefault_host_perfdata_file_template==null)
            return common_h.OK;
        
        /* update host macros */
        utils.clear_volatile_macros();
        utils.grab_host_macros(hst);
        utils.grab_summary_macros(null);
        
        /* get the raw output */
        String raw_output = xpddefault_host_perfdata_file_template;
        
        logger.debug("\tRaw host performance output: " + raw_output);
        
        /* process any macros in the raw output */
        raw_output = utils.process_macros( raw_output ,0);
        
        logger.debug("\tProcessed host performance data output: " + raw_output);
        
        /* write the processed output line containing performance data to the host perfdata file */
        try {
            xpddefault_host_perfdata_fp.write( raw_output );
            xpddefault_host_perfdata_fp.write( "\n" );
            xpddefault_host_perfdata_fp.flush();
        } catch (IOException ioE) {
           logger.warn("warning: " + ioE.getMessage(), ioE);
        }
        
        return common_h.OK;
    }
    
    
    /* periodically process the host perf data file */
    public static int xpddefault_process_host_perfdata_file(){
        int macro_options= blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;
        
        /* we don't have a command */
        if(xpddefault_host_perfdata_file_processing_command==null)
            return common_h.OK;
        
        /* close the performance data files */
        xpddefault_close_host_perfdata_file();
        xpddefault_close_service_perfdata_file();
        
        /* update macros */
        utils.clear_volatile_macros();
        utils.grab_datetime_macros();
        utils.grab_summary_macros(null);
        
        /* get the raw command line */
        String raw_command_line = utils.get_raw_command_line(xpddefault_host_perfdata_file_processing_command,macro_options).trim();
        
        logger.debug("\tRaw host performance data file processing command line: " + raw_command_line);
        
        /* process any macros in the raw command line */
        String processed_command_line = utils.process_macros(raw_command_line,macro_options);
        
        logger.debug("\tProcessed host performance data file processing command line: " + processed_command_line);
        
        /* run the command */
        utils.system_result result = utils.my_system( processed_command_line, xpddefault_perfdata_timeout );
        
        /* check to see if the command timed out */
        if( result.early_timeout==true){
            logger.warn( "Warning: Host performance data file processing command '"+processed_command_line+"' timed out after "+xpddefault_perfdata_timeout+" seconds");
        }
        
        /* re-open the performance data files */
        xpddefault_open_service_perfdata_file();
        xpddefault_open_host_perfdata_file();
        
        return common_h.OK;
    }
    
    
    /* periodically process the service perf data file */
    public static int xpddefault_process_service_perfdata_file(){
        int macro_options= blue_h.STRIP_ILLEGAL_MACRO_CHARS| blue_h.ESCAPE_MACRO_CHARS;
        
        /* we don't have a command */
        if(xpddefault_service_perfdata_file_processing_command==null)
            return common_h.OK;
        
        /* close the performance data files */
        xpddefault_close_host_perfdata_file();
        xpddefault_close_service_perfdata_file();
        
        /* update macros */
        utils.clear_volatile_macros();
        utils.grab_datetime_macros();
        utils.grab_summary_macros(null);
        
        /* get the raw command line */
        String raw_command_line = utils.get_raw_command_line(xpddefault_service_perfdata_file_processing_command,macro_options).trim();
        
        logger.debug("\tRaw service performance data file processing command line: " + raw_command_line);
        
        /* process any macros in the raw command line */
        String processed_command_line = utils.process_macros(raw_command_line,macro_options);
        
        logger.debug("\tProcessed service performance data file processing command line: " + processed_command_line);
        
        /* run the command */
        utils.system_result result = utils.my_system( processed_command_line, xpddefault_perfdata_timeout );
        
        /* check to see if the command timed out */
        if( result.early_timeout==true){
            logger.warn( "Warning: Service performance data file processing command '"+processed_command_line+"' timed out after "+xpddefault_perfdata_timeout+" seconds");
        }
        
        /* re-open the performance data files */
        xpddefault_open_service_perfdata_file();
        xpddefault_open_host_perfdata_file();
        
        return common_h.OK;
    }
    
    private static int atoi(String value) {
        try {
            return Integer.parseInt(value);
        } catch ( NumberFormatException nfE ) {
           logger.error( "warning: " + nfE.getMessage(), nfE );
            return 0;
        }
    }
    
    private static double strtod(String value, Object ignore ) {
        try {
            return Double.parseDouble(value);
        } catch ( NumberFormatException nfE ) {
            logger.error( "warning: " + nfE.getMessage(), nfE );
            return 0.0;
        }
    }
    
    private static long strtoul(String value, Object ignore, int base ) {
        try {
            return Long.parseLong(value);
        } catch ( NumberFormatException nfE ) {
           logger.error( "warning: " + nfE.getMessage(), nfE );
            return 0L;
        }
    }
    
}