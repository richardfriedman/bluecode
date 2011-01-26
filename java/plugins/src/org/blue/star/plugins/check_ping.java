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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public class check_ping extends check_base {
   
   protected String getAuthor() { return "Richard Friedman richardfriedman@yahoo.com"; }
   protected String getCopyright() { return "Richard Friedman 2007"; }
   protected String getDescription() { 
       return "Use ping to check connection statistics for a remote host.";
   }
   protected String getNotes() {
      return "THRESHOLD is <rta>,<pl>%% \n" +
      "<rta> is the round trip average travel time (ms) which triggers a WARNING or CRITICAL state, \n" +
      "<pl> is the percentage of packet loss to trigger an alarm state. \n \n " +
      "This plugin uses the ping command to probe the specified host for " +
      "packet loss (percentage) and round trip average (milliseconds). \n \n" +
      "It can produce HTML output linking to a traceroute CGI contributed " +
      "by Ian Cass. The CGI can be found in the contrib area of the " +
      "downloads section at http://blue.sourceforge.net/ \n \n";
   }
   
   public static final String WARN_DUPLICATES = "DUPLICATES FOUND! ";
   public static final int UNKNOWN_TRIP_TIME = -1;	/* -1 seconds */
   
   public static final int UNKNOWN_PACKET_LOSS = 200;    /* 200% */
   public static final int DEFAULT_MAX_PACKETS = 5;       /* default no. of ICMP ECHO packets */
   
   public boolean display_html = false;
   public int wpl = UNKNOWN_PACKET_LOSS;
   public int cpl = UNKNOWN_PACKET_LOSS;
   public float wrta = UNKNOWN_TRIP_TIME;
   public float crta = UNKNOWN_TRIP_TIME;
   public ArrayList<String> addresses = new ArrayList<String>();
   public int n_addresses = 0;
   public int max_addr = 1;
   public int max_packets = -1;
   public int address_family;
   public String interface_name = null;
   public NetworkInterface network_interface = null;
   
   public static float rta = UNKNOWN_TRIP_TIME;
   public static int pl = UNKNOWN_PACKET_LOSS;
   
   public String warn_text = "";
   public int check_state = common_h.STATE_UNKNOWN;
   
   public static void main (String[] args) {
      new check_ping().process_request( args );
   }
      
   public void init_command() {
   }
   
   /* Add options specific to this plugin */
   public void add_command_arguments( Options options ) {
      Option p = new Option( "p", "packets", true, "number of ICMP ECHO packets to send (Default: "+DEFAULT_MAX_PACKETS+")" );
      p.setArgName( "packets" );
      options.addOption( p );
      Option w = new Option( "w", "warning", true, "warning threshold pair (round trip average, % packets lost)" );
      w.setArgName( "<wrta>,<wpl>%" );
      options.addOption(w);
      Option c = new Option( "c", "critical", true, "critical threshold pair (round trip average, % packets lost)" );
      c.setArgName( "<crta>,<cpl>%" );
      options.addOption( c );
      OptionGroup group = new OptionGroup();
      group.addOption( new Option( "4", "use-ipv4", false, "Use IPv4 connection" ) );
      group.addOption( new Option( "6", "use-ipv6", false, "Use IPv6 connection" ) );
      options.addOptionGroup( group );
      options.addOption( "i", "interface", true, "Select the interface name to use." );
   }

   /* process command-line arguments */
   public void process_command_option ( Option o )
      throws IllegalArgumentException {
         String optarg = o.getValue();
                  
         switch ( o.getId() ) {
            case '4':   /* IPv4 only */
               address_family = common_h.AF_INET;
               break;
            case '6':   /* IPv6 only */
               address_family = common_h.AF_INET6;
               // TODO Determine if IPv6 is even available.
               break;
            case 'H':   /* hostname */
               String[] hostnames = optarg.split( "," );
               for ( String hostname : hostnames )
                  addresses.add( hostname );
               n_addresses = addresses.size();
               break;
            case 'p':   /* number of packets to send */
               if (utils.is_intnonneg (optarg))
                  max_packets = Integer.parseInt(optarg);
               else
                  throw new IllegalArgumentException( utils.formatArgumentError(this.getClass().getName(), "<max_packets> (%s) must be a non-negative number\n", optarg));
               break;
            case 'n':   /* no HTML */
               display_html = false;
               break;
            case 'L':   /* show HTML */
               display_html = true;
               break;
            case 'c':
               crta = get_threshold_rta (optarg);
               cpl = get_threshold_pl (optarg);
               break;
            case 'w':
               wrta = get_threshold_rta (optarg);
               wpl = get_threshold_pl (optarg);
               break;
            case 'i':
               interface_name = optarg;
               
               try {
                  network_interface = NetworkInterface.getByName(interface_name);
               } catch ( Exception e ) {
                  throw new IllegalArgumentException( "Error acquiring acces to interface " + e.getMessage() );
               }
               if ( network_interface == null) {
                  throw new IllegalArgumentException( "Interface name "+ interface_name + " is not available." );
               }
               break;
         }
   }

   /**
    * Process remaining command line arguments.
    */
   public void process_command_arguments( String[] argv )
      throws IllegalArgumentException {

      if ( argv!=null && argv.length == 0 )
         return;
      
      int c = 0;    
      int argc = argv.length;
      if ( addresses.size() == 0 ) {
         if ( netutils.is_host (argv[c]) == false ) {
            throw new IllegalArgumentException( utils.formatArgumentError(this.getClass().getName(), "Invalid hostname/address",  argv[c] ) );
         } else {
            addresses.add( argv[c++] );
            n_addresses++;
            if (c == argc)
               return;
         }
      }
      
      if (wpl == UNKNOWN_PACKET_LOSS) {
         if ( utils.is_intpercent (argv[c]) == false ) {
            throw new IllegalArgumentException( String.format( "<wpl> (%s) must be an integer percentage\n" , argv[c]) );
         } else {
            wpl = Integer.parseInt(argv[c++]);
            if (c == argc)
               return;
         }
      }
      
      if (cpl == UNKNOWN_PACKET_LOSS) {
         if ( utils.is_intpercent (argv[c]) == false ) {
            throw new IllegalArgumentException( String.format( "<cpl> (%s) must be an integer percentage\n", argv[c]) );
         } else {
            cpl = Integer.parseInt(argv[c++]);
            if (c == argc)
               return;
         }
      }
      
      if (wrta < 0.0) {
         if (utils.is_negative (argv[c])) {
            throw new IllegalArgumentException( String.format("<wrta> (%s) must be a non-negative number\n", argv[c]) );
         } else {
            wrta = Float.parseFloat(argv[c++]);
            if (c == argc)
               return;
         }
      }
      
      if (crta < 0.0) {
         if ( utils.is_negative (argv[c])) {
            throw new IllegalArgumentException( String.format("<crta> (%s) must be a non-negative number\n", argv[c]) );
         } else {
            crta = Float.parseFloat(argv[c++]);
            if (c == argc)
               return;
         }
      }
      
      if (max_packets == -1) {
         if ( utils.is_intnonneg (argv[c])) {
            max_packets = Integer.parseInt(argv[c++]);
         } else {
            throw new IllegalArgumentException( String.format("<max_packets> (%s) must be a non-negative number\n", argv[c]) );
         }
      }   }
   
   public float get_threshold_rta (String arg)
      throws IllegalArgumentException {
      
      try {
         String[] split = arg.split( "[:,]",2 );
         return Float.parseFloat( split[0] );
      } catch (Exception e) {
         throw new IllegalArgumentException( String.format( "%s: Warning threshold must be integer!\n\n", arg ) );
      }
      
   }
   
   
   public int get_threshold_pl (String arg) 
      throws IllegalArgumentException{
      
      try {
         String[] split = arg.split( "[:,]",2 );
         if ( split.length == 2 ) 
            return Integer.parseInt( split[1].replace('%', ' ').trim() );
         else
            return 0;
      } catch (Exception e) {
         throw new IllegalArgumentException( String.format( "%s: Warning threshold must be percentage!\n\n", arg ) );
      }
   }
   
   /**
    * Validate the argument set.
    */
   public void validate_command_arguments ()
      throws IllegalArgumentException {
      
      if (wrta < 0.0) {
         throw new IllegalArgumentException("<wrta> was not set\n");
      }
      else if (crta < 0.0) {
         throw new IllegalArgumentException("<crta> was not set\n");
      }
      else if (wpl == UNKNOWN_PACKET_LOSS) {
         throw new IllegalArgumentException("<wpl> was not set\n");
      }
      else if (cpl == UNKNOWN_PACKET_LOSS) {
         throw new IllegalArgumentException("<cpl> was not set\n");
      }
      else if (wrta > crta) {
         throw new IllegalArgumentException( String.format( "<wrta> (%f) cannot be larger than <crta> (%f)\n", wrta, crta ) );
      }
      else if (wpl > cpl) {
         throw new IllegalArgumentException( String.format("<wpl> (%d) cannot be larger than <cpl> (%d)\n", wpl, cpl) ) ;
      }
      
      if (max_packets == -1)
         max_packets = DEFAULT_MAX_PACKETS;
      
      double max_seconds = crta / 1000.0 * max_packets + max_packets;
      if (max_seconds > utils_h.timeout_interval)
         utils_h.timeout_interval = (int) max_seconds;
      
      for (int i=0; i<n_addresses; i++) {
         if ( netutils.is_host(addresses.get(i)) == false)
            throw new IllegalArgumentException( utils.formatArgumentError(this.getClass().getName(), "Invalid hostname/address", addresses.get(i) ) );
     }
      
      if (n_addresses == 0) {
         throw new IllegalArgumentException("You must specify a server address or host name" );
      }

   }
   
   public boolean execute_check() {
   
      for ( String hostname : addresses ) {
         
         try {
            
            InetAddress inet = InetAddress.getByName( hostname );

            long execute_time = 0;
            long packet_loss = 0;
            for ( int pings = 0; pings < max_packets; pings++  ) {
               boolean reachable = false;
               long ping_time = System.currentTimeMillis();
               if ( network_interface != null ) {
                  reachable = inet.isReachable(network_interface,0, utils_h.timeout_interval);
               } else {
                  reachable = inet.isReachable( utils_h.timeout_interval ); 
               }
               execute_time += (System.currentTimeMillis() - ping_time);
               if ( !reachable )
                  packet_loss++;
            }
            rta = execute_time / max_packets;
            pl = (int) packet_loss/max_packets*100;
            
            if ( verbose > 0 ) {
               System.out.println( "rta = " + rta );
               System.out.println( "pl = " + pl);
            }
            
            if (verbose > 1) {
               System.out.println( "isAnyLocalAddress = " + inet.isAnyLocalAddress() );
               System.out.println( "isLinkLocalAddress = " + inet.isLinkLocalAddress() );
               System.out.println( "isLoopbackAddress = " + inet.isLoopbackAddress() );
               System.out.println( "isMCGlobal = " + inet.isMCGlobal() );
               System.out.println( "isMCLinkLocal = " + inet.isMCLinkLocal() );
               System.out.println( "isMCNodeLocal = " + inet.isMCNodeLocal() );
               System.out.println( "isMCOrgLocal = " + inet.isMCOrgLocal() );
               System.out.println( "isMCSiteLocal = " + inet.isMCSiteLocal() );
               System.out.println( "isMulticastAddress = " + inet.isMulticastAddress() );
               System.out.println( "isSiteLocalAddress = " + inet.isSiteLocalAddress() );
               System.out.println( "isReachable = " + inet.isReachable( utils_h.timeout_interval ) );
               System.out.println( "getCanonicalHostName = " + inet.getCanonicalHostName() );
               System.out.println( "getHostAddress = " + inet.getHostAddress() );
               System.out.println( "getHostName = " + inet.getHostName() );
               System.out.println( "getClass.getName = " + inet.getClass().getName() );
            }
       
            /* The list is only used to check alternatives, if we pass don't do more */
            if ( packet_loss != max_packets )
               break;
            
         } catch (Exception e ) {
            warn_text = e.getMessage();
            e.printStackTrace();
         }
         
      }
      
      if (pl >= cpl || rta >= crta || rta < 0)
         check_state= common_h.STATE_CRITICAL;
      else if (pl >= wpl || rta >= wrta)
         check_state = common_h.STATE_WARNING;
      else if (pl >= 0 && rta >= 0)
         check_state = common_h.STATE_OK;

      return true;
   }
   
   public int check_state() {
      return check_state;
   }

   public String check_message() {
      
      if (pl == 100)
         return String.format( _("PING %s - %sPacket loss = %d%%"), utils.state_text ( check_state ), warn_text, pl);
     else
         return String.format( _("PING %s - %sPacket loss = %d%%, RTA = %2.2f ms"), utils.state_text ( check_state ), warn_text, pl, rta);
     
   }

}