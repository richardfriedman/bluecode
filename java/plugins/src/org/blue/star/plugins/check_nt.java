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

package org.blue.star.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public class check_nt extends check_base {

   protected String getAuthor() { return "Richard Friedman richardfriedman@yahoo.com"; }
   protected String getCopyright() { return "Richard Friedman 2007"; }
   protected String getDescription() { 
      return "This plugin collects data from the NSClient service running on Windows NT/2000/XP/2003 server.";
   }
   protected String getNotes() {
      return "Notes:\n " +
             " - The NSClient service should be running on the server to get any information\n " +
             " (http://nsclient.ready2run.nl).\n " +
             " - Critical thresholds should be lower than warning thresholds\n";
   }

   public static String DEF_PASSWORD_FILE = 
      " -S <password file>\n" + 
      "   Extract password needed for the request from a file that looks like this:\n" + 
      "   <ip> <port> <password>\n" + 
      "   eg. 192.168.12.3 1248 secret\n" + 
      "   So you can have all your servers passwords in one file.\n" + 
      "   The right entry will be choosen by the given ip+port on the command line.\n" + 
      "   DO NOT use comments or blank lines in that file!\n";

   public static String DEF_VARIABLE = 
      "-v, --variable=STRING\n" +    
      "   Variable to check.  Valid variables are:\n " +
      "      CLIENTVERSION = Get the NSClient version\n" +
      "        If -l <version> is specified, will return warning if versions differ.\n" +
      "      CPULOAD = Average CPU load on last x minutes.\n" +
      "       Request a -l parameter with the following syntax:\n" +
      "       -l <minutes range>,<warning threshold>,<critical threshold>.\n" +
      "       <minute range> should be less than 24*60.\n" +
      "       Thresholds are percentage and up to 10 requests can be done in one shot.\n" +
      "       ie: -l 60,90,95,120,90,95\n " +
      "      UPTIME = Get the uptime of the machine. Warning and/or critical thresholds are minimum uptime in minutes.\n" + 
      "      USEDDISKSPACE = Size and percentage of disk use. Request a -l parameter containing the drive letter only.\n" + 
      "                      Warning and critical thresholds can be specified with -w and -c.\n" + 
      "      MEMUSE = Memory use. Warning and critical thresholds can be specified with -w and -c.\n" +  
      "      SERVICESTATE = Check the state of one or several services.\n " +
      "                     Request a -l parameters with the following syntax:\n " +
      "                     -l <service1>,<service2>,<service3>,...\n " +
      "                     You can specify -d SHOWALL in case you want to see working services\n" +
      "                     in the returned string.\n " +
      "      PROCSTATE = Check if one or several process are running.\n " +
      "                  Same syntax as SERVICESTATE.\n " +
      "      COUNTER = Check any performance counter of Windows NT/2000.\n " +
      "          Request a -l parameters with the following syntax:\n" +
      "          -l \"\\\\<performance object>\\\\counter\",\"<description>\n " +
      "          The <description> parameter is optional and \n " +
      "          is given to a printf output command which requires a float parameter.\n" +
      "          If <description> does not include \"%%\", it is used as a label.\n " +
      "          Some examples:\n " +
      "             \"Paging file usage is %%.2f %%%%\"\n " +
      "             \"%%.f %%%% paging file used.\"\n";


   public static final int CHECK_NONE = 0;
   public static final int CHECK_CLIENTVERSION = 1;
   public static final int CHECK_CPULOAD  = 2;
   public static final int CHECK_UPTIME   = 3;
   public static final int CHECK_USEDDISKSPACE = 4;
   public static final int CHECK_SERVICESTATE = 5;
   public static final int CHECK_PROCSTATE = 6;
   public static final int CHECK_MEMUSE  =  7;
   public static final int CHECK_COUNTER  = 8;
   public static final int CHECK_FILEAGE = 9;
   public static final int MAX_VALUE_LIST = 30;

   public static final int PORT = 1248;    
   public static final int DEFAULT_SOCKET_TIMEOUT = common_h.DEFAULT_SOCKET_TIMEOUT;

   public String server_address=null;
   public String volume_name=null;
   public int server_port=PORT;
   public int socket_timeout = 0;
   public String value_list=null;
   public String req_password=null;
   public String password_file=null;
// public long lvalue_list[MAX_VALUE_LIST];
   public long warning_value=0L;
   public long critical_value=0L;
   public boolean check_value_list=false;
   public boolean check_warning_value=false;
   public boolean check_critical_value=false;
   public int vars_to_check=CHECK_NONE;
   public boolean show_all=false;

   public String check_message = "";
   public int check_state = common_h.STATE_UNKNOWN;

   public static void main (String[] args) {
      new check_nt().process_request( args );
   }

   public void init_command() {
   }

   /**
    *  Add options specific to this plugin
    */
   public void add_command_arguments( Options options ) {
      options.addOption( "p", "port", true, " --port=INTEGER Optional port number (default: "+PORT+")" );
      options.addOption( "t", "timeout", true, " --timeout=INTEGER Seconds before connection attempt times out (default: "+DEFAULT_SOCKET_TIMEOUT+")" );
      options.addOption( "t", "to", true, " [deprecated]" );
      options.addOption( "w", "warning", true, "--warning=INTEGER Threshold which will result in a warning status" );
      options.addOption( "w", "wv", true, "[deprecated]" );
      options.addOption( "c", "critical", true, "--critical=INTEGER Threshold which will result in a critical status" );
      options.addOption( "c", "cv", true, "[deprecated]" );
      options.addOption( "H", "hostname", true, "--hostname=HOST Name of the host to check" );
      options.addOption( "v", "variable", true, DEF_VARIABLE );
      options.addOption( "s", "password", true, "Password needed for the request" );
      options.addOption( "S", "PASSWORD", true, DEF_PASSWORD_FILE );
      options.addOption( "l", "list", true, "Depends on the variable.");
   }


   /** 
    * process command-line arguments 
    */
   public void process_command_option ( Option o ) 
      throws IllegalArgumentException {
      String optarg = o.getValue();

      switch ( o.getId() ) {
         case 'H': /* hostname */
            server_address=optarg;
            break;
         case 's': /* password */
            req_password=optarg;
            break;
         case 'S': /* password file */
            password_file=optarg;
            break;
         case 'p': /* port */
            try {
               server_port = Integer.parseInt(optarg);
            } catch ( NumberFormatException nfE ) {
               throw new IllegalArgumentException( "Port must be a valid integer." );
            }
         case 'v':
            if( optarg.equals("CLIENTVERSION"))
               vars_to_check=CHECK_CLIENTVERSION;
            else if(optarg.equals("CPULOAD"))
               vars_to_check=CHECK_CPULOAD;
            else if(optarg.equals("UPTIME"))
               vars_to_check=CHECK_UPTIME;
            else if(optarg.equals("USEDDISKSPACE"))
               vars_to_check=CHECK_USEDDISKSPACE;
            else if(optarg.equals("SERVICESTATE"))
               vars_to_check=CHECK_SERVICESTATE;
            else if(optarg.equals("PROCSTATE"))
               vars_to_check=CHECK_PROCSTATE;
            else if(optarg.equals("MEMUSE"))
               vars_to_check=CHECK_MEMUSE;
            else if(optarg.equals("COUNTER"))
               vars_to_check=CHECK_COUNTER;
            else if(optarg.equals("FILEAGE"))
               vars_to_check=CHECK_FILEAGE;
            else {
               throw new IllegalArgumentException( "Unknown variable." );
            }
            break;
         case 'l': /* value list */
            value_list=optarg;
            break;
         case 'w': /* warning threshold */
            warning_value=Integer.parseInt( optarg );
            check_warning_value=true;
            break;
         case 'c': /* critical threshold */
            critical_value=Integer.parseInt( optarg );
            check_critical_value=true;
            break;
         case 'd': /* Display select for services */
            if ( optarg.equals("SHOWALL"))
               show_all = true;
            break;
         case 't': /* timeout */
            socket_timeout=Integer.parseInt(optarg);
            if(socket_timeout<=0) {
               throw new IllegalArgumentException( "Socket timeout must be greater than 0." );
            }
      }
   }

   /**
    * Process remaining command line arguments.
    */
   public void process_command_arguments( String[] argv )
      throws IllegalArgumentException {

      if ( argv != null && server_address == null )
         server_address = argv[0];

   }

   /**
    * Validate the argument set.
    */
   public void validate_command_arguments () 
      throws IllegalArgumentException {
      if (vars_to_check==CHECK_NONE) {
         throw new IllegalArgumentException ( "Vars to check must be specified." );
      }

      if (req_password == null)
         req_password = "None";

   }   
   
   public boolean execute_check() {
      try {
         inside_check();
      } catch ( NSClient4JException ns4) {
         check_state = common_h.STATE_UNKNOWN;
         check_message = ns4.getMessage();
         return false;
      }
      
      return true;
   }

   private boolean inside_check() throws NSClient4JException {
      String perfdata = null;

      /* Read NT password from a file if requested */
      if(password_file!=null) {

         BufferedReader reader = null;
         try {
            File pFile = new File( password_file );
            reader = new BufferedReader( new FileReader( pFile ));

            req_password=null;
            String line = null;
            while (( line = reader.readLine()) != null){
               String[] parms = line.split("\\s+");
               if ( parms.length >= 3) {
                  if( parms[0].equals(server_address) && Integer.parseInt(parms[1])==server_port) {
                     req_password=parms[2];
                  }
               }
            }
         } catch (IOException ioE ) {
            check_message = String.format( "Could not open password file %s.\n",password_file);
            check_state = common_h.STATE_UNKNOWN;
            return false;
         }
         finally {
            try {
               if (reader!= null) {
                  reader.close();
               }
            } catch (IOException ex) {
            }
         }

         if(req_password==null) {
            check_message = String.format( "Password for host %s and port %d not found in %s.\n",server_address,server_port,password_file);
            check_state = common_h.STATE_UNKNOWN;
         }
      }

      NSClient4j client;
      try {
         client = new NSClient4j( server_address, server_port, req_password );
         client.setSocketTimeOut(socket_timeout);
      } catch ( NSClient4JException ns4jE ) { 
         check_state = common_h.STATE_CRITICAL;
         check_message = ns4jE.getMessage();
         return false;
      }

//    } catch ( NSClient4JException ns4jE ) { 
//    check_state = common_h.STATE_CRITICAL;
//    check_message = ns4jE.getMessage();
//    return false;
//    }

      check_state = common_h.STATE_UNKNOWN;
      switch ( vars_to_check ) {
         case CHECK_CLIENTVERSION:
            String version = client.getNSClientVersion();
            if (value_list != null && !version.equals(value_list) ) {
               check_message = String.format ("Wrong client version - running: %s, required: %s", version, value_list);
               check_state = common_h.STATE_WARNING;
            } else {
               check_message = version;
               check_state = common_h.STATE_OK;
            }
            break;

         case CHECK_CPULOAD: 
            long[] lvalue_list = strtolarray( value_list, "," );

            if (value_list==null) {
               check_message = "missing -l parameters";
            } else if ( lvalue_list == null ) {
               check_message = "wrong -l parameter.";
            } else if ( lvalue_list.length > MAX_VALUE_LIST ) {
               check_message = "-l parameter greater than " + MAX_VALUE_LIST ;
            } else if ( lvalue_list.length % 3 != 0 ) {
               check_message = "not enought values for -l parameters";
            } else {
               /* -l parameters is present with only integers */
               check_state=common_h.STATE_OK;
               String temp_string =  "CPU Load" ;
               String temp_string_perf = " ";

               /* loop until one of the parameters is wrong or not present */
               int offset = 0;
               while ( lvalue_list[0+offset]<=(long)17280 && lvalue_list[1+offset]<=(long)100 && lvalue_list[2+offset]<=(long)100) {

                  /* Send request and retrieve data */
                  String usage = client.getCPUUsage( lvalue_list[0+offset] );
                  long utilization = Long.parseLong( usage );

                  /* Check if any of the request is in a warning or critical state */
                  if(utilization >= lvalue_list[2+offset])
                     check_state = common_h.STATE_CRITICAL;
                  else if(utilization >= lvalue_list[1+offset] && check_state < common_h.STATE_WARNING)
                     check_state = common_h.STATE_WARNING;

                  check_message = String.format(" %d%% (%d min average)", utilization, lvalue_list[0+offset]);
                  temp_string = String.format("%s%s",temp_string,check_message);
                  perfdata = String.format(" '%d min avg Load'=%d%%;%d;%d;0;100", lvalue_list[0+offset], utilization, lvalue_list[1+offset], lvalue_list[2+offset]);
                  temp_string_perf = String.format("%s%s",temp_string_perf,perfdata);
                  offset+=3;  /* move across the array */
               }

               check_message = temp_string;
               perfdata = temp_string_perf;
            }   
            break;

         case CHECK_UPTIME :
            long upseconds = client.getUpTimeSeconds();
            long updays = upseconds / 86400;            
            long uphours = (upseconds % 86400) / 3600;
            long upminutes = ((upseconds % 86400) % 3600) / 60;
            check_message = String.format("System Uptime : %u day(s) %u hour(s) %u minute(s)", updays,uphours, upminutes);

            if ( check_critical_value && upseconds/60 < critical_value )
               check_state = common_h.STATE_CRITICAL;
            else if (check_warning_value && upseconds/60 < warning_value)
               check_state = common_h.STATE_WARNING;  
            else
               check_state=common_h.STATE_OK;
            break;

         case CHECK_USEDDISKSPACE:
            if (value_list==null)
               check_message = "missing -l parameters";
            else if ( value_list.length()!=1)
               check_message = "wrong -l argument";
            else {
               double[] disk_space = client.getFreeAndTotalDiskSpace( value_list );
               double free_disk_space = disk_space[0];
               double total_disk_space = disk_space[1];
               double percent_used_space = ((total_disk_space - free_disk_space) / total_disk_space) * 100;
               double warning_used_space = ((float)warning_value / 100) * total_disk_space;
               double critical_used_space = ((float)critical_value / 100) * total_disk_space;

               if (free_disk_space>=0) {
                  String temp_string = String.format( "%s:\\ - total: .2f Gb - used: %.2f Gb (%.0f%%) - free %.2f Gb (%.0f%%)",
                        value_list, total_disk_space / 1073741824, (total_disk_space - free_disk_space) / 1073741824,
                        percent_used_space, free_disk_space / 1073741824, (free_disk_space / total_disk_space)*100);

                  String temp_string_perf = String.format( "'%s:\\ Used Space'=%.2fGb;%.2f;%.2f;0.00;%.2f", value_list,
                        (total_disk_space - free_disk_space) / 1073741824, warning_used_space / 1073741824,
                        critical_used_space / 1073741824, total_disk_space / 1073741824);

                  if(check_critical_value==true && percent_used_space >= critical_value)
                     check_state = common_h.STATE_CRITICAL;
                  else if (check_warning_value==true && percent_used_space >= warning_value)
                     check_state= common_h.STATE_WARNING;  
                  else
                     check_state= common_h.STATE_OK;   

                  check_message = temp_string;
                  perfdata = temp_string_perf;
               } else {
                  check_message = "Free disk space : Invalid drive ";
                  check_state = common_h.STATE_UNKNOWN;
               }
            }
            break;

         case CHECK_SERVICESTATE:
            if (value_list==null)
               check_message = "No service/process specified";
            else {
               String[] result = client.checkServices( value_list.split(",") , show_all );

               check_state = Integer.parseInt( result[0] );
               check_message = result[1];
            }
            break;

         case CHECK_PROCSTATE:
            if (value_list==null)
               check_message = "No service/process specified";
            else {
               String[] result = client.checkProcesses( value_list.split(",") , show_all );

               check_state = Integer.parseInt( result[0] );
               check_message = result[1];
            }
            break;

         case CHECK_MEMUSE:
            double[] memory_usage = client.getMemoryage();
            double mem_commitLimit = memory_usage[0];
            double mem_commitByte = memory_usage[1];

            double percent_used_space = (mem_commitByte / mem_commitLimit) * 100;
            double warning_used_space = ((float)warning_value / 100) * mem_commitLimit;
            double critical_used_space = ((float)critical_value / 100) * mem_commitLimit;

            /* Divisor should be 1048567, not 3044515, as we are measuring "Commit Charge" here, 
        which equals RAM + Pagefiles. */
            check_message = String.format( "Memory usage: total:%.2f Mb - used: %.2f Mb (%.0f%%) - free: %.2f Mb (%.0f%%)", 
                  mem_commitLimit / 1048567, mem_commitByte / 1048567, percent_used_space,  
                  (mem_commitLimit - mem_commitByte) / 1048567, (mem_commitLimit - mem_commitByte) / mem_commitLimit * 100);

            perfdata = String.format( "'Memory usage'=%.2fMb;%.2f;%.2f;0.00;%.2f", mem_commitByte / 1048567,
                  warning_used_space / 1048567, critical_used_space / 1048567, mem_commitLimit / 1048567);

            check_state = common_h.STATE_OK;
            if(check_critical_value==true && percent_used_space >= critical_value)
               check_state=common_h.STATE_CRITICAL;
            else if (check_warning_value==true && percent_used_space >= warning_value)
               check_state=common_h.STATE_WARNING;  

            break;

         case CHECK_COUNTER: 

            /* 
        CHECK_COUNTER has been modified to provide extensive perfdata information.
                In order to do this, some modifications have been done to the code
                and some constraints have been introduced.

                1) For the sake of simplicity of the code, perfdata information will only be 
                 provided when the "description" field is added. 

                2) If the counter you're going to measure is percent-based, the code will detect
                 the percent sign in its name and will attribute minimum (0%) and maximum (100%) 
                 values automagically, as well the "%" sign to graph units.

                3) OTOH, if the counter is "absolute", you'll have to provide the following
                 the counter unit - that is, the dimensions of the counter you're getting. Examples:
                 pages/s, packets transferred, etc.

                4) If you want, you may provide the minimum and maximum values to expect. They aren't mandatory,
                 but once specified they MUST have the same order of magnitude and units of -w and -c; otherwise.
                 strange things will happen when you make graphs of your data.
             */

            double counter_value = -1;
            if (value_list == null)
               check_message = "No counter specified";
            else {
               String[] vList = value_list.split( "," );
               boolean isPercent = value_list.contains("%");
               boolean allRight = false;

               String description = (vList.length>1)?vList[1]:null;
               String counter_unit = (vList.length>2)?vList[2]:null;
               String minval = (vList.length>3)?vList[3]:null;
               String maxval = (vList.length>4)?vList[4]:null;

               String recv_buffer = client.getPerfMonCounter(vList[0]);
               counter_value = Double.parseDouble( recv_buffer );

               if (description == null)
                  check_message = String.format( "%.f", counter_value);
               else if (isPercent) {  
                  counter_unit = "%";
                  allRight = true;
               }

               double fminval = -1;
               double fmaxval = -1;

               if ((counter_unit != null ) && (!allRight))
               {   
                  /* All parameters specified. Let's check the numbers */
                  String checking = null;
                  try {
                     checking = "Minimum";
                     fminval = (minval != null) ? Double.parseDouble(minval)  : -1;
                     checking = "Maximum";
                     fmaxval = (maxval != null) ? Double.parseDouble(maxval)  : -1;
                     allRight = true;
                  } catch (NumberFormatException nfE) {
                     check_message = checking + " value contains non-numbers";
                  }
               }
               else if ((counter_unit == null) && (description != null))
                  check_message = "No unit counter specified";

               if (allRight)
               {
                  /* Let's format the output string, finally... */
                  if ( description.indexOf("%") < 0 ) {
                     check_message = String.format( "%s = %.2f %s", description, counter_value, counter_unit);
                  } else {
                     /* has formatting, will segv if wrong */
                     check_message = String.format( description, counter_value);
                  }
                  check_message = String.format( "%s |", check_message);
                  check_message = String.format("%s %s", check_message, 
                        utils.fperfdata(description, counter_value, 
                              counter_unit, 1, warning_value, 1, critical_value,
                              (!(isPercent) && (minval != null)), fminval,
                              (!(isPercent) && (minval != null)), fmaxval));
               }
            }

            if (critical_value > warning_value) {           
               /* Normal thresholds */
               if (check_critical_value == true && counter_value >= critical_value)
                  check_state = common_h.STATE_CRITICAL;
               else if (check_warning_value == true && counter_value >= warning_value)
                  check_state = common_h.STATE_WARNING;
               else
                  check_state = common_h.STATE_OK;
            }
            else {
               /* inverse thresholds */
               check_state = common_h.STATE_OK;
               if (check_critical_value == true && counter_value <= critical_value)
                  check_state = common_h.STATE_CRITICAL;
               else if (check_warning_value == true && counter_value <= warning_value)
                  check_state = common_h.STATE_WARNING;
            }
            break;

         case CHECK_FILEAGE:

            if (value_list==null) {
               check_message = "No counter specified";
            } else {
               value_list = value_list.replace( ",", "&" );  /* replace , between services with & to send the request */
               String[] results  = client.getFileInformation( value_list );

               int age_in_minutes = Integer.parseInt( results[0] );
               check_message = results.length > 1 ? results[1] : null;

               if (critical_value > warning_value) {        /* Normal thresholds */
                  if(check_critical_value== true && age_in_minutes >= critical_value)
                     check_state=common_h.STATE_CRITICAL;
                  else if (check_warning_value==true && age_in_minutes >= warning_value)
                     check_state=common_h.STATE_WARNING;  
                  else
                     check_state=common_h.STATE_OK;   
               } else {                                       /* inverse thresholds */
                  if(check_critical_value==true && age_in_minutes <= critical_value)
                     check_state=common_h.STATE_CRITICAL;
                  else if (check_warning_value==true && age_in_minutes <= warning_value)
                     check_state=common_h.STATE_WARNING;  
                  else
                     check_state=common_h.STATE_OK;   
               }
            }
            break;
      }

      if (perfdata!=null)
         check_message = String.format("%s | %s",check_message,perfdata);

      return true;
   }

   /** 
    * Parse string into array of long.
    * 
    * @param string  to parse
    * @param delim   regex as delim
    * @return  array of long or null if failed.
    */
   public long[] strtolarray( String string, String delim) {
      if ( string == null )
         return null;

      String[] split = string.split(delim);
      long[] array = new long[split.length+1];

      try {
         for ( int i = 0; i < split.length; i++ ) {
            array[i] = Long.parseLong( split[i] );
         }
      } catch (NumberFormatException nfE ) {
         array = null;
      }

      return array;
   }

   public int check_state () { return check_state; }
   public String check_message() { return check_message; }

}