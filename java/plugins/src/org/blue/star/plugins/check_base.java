/*****************************************************************************
 
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

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * Plugins are just an implementation of a main, needing a specific input line.
 * The java plugin base class provides a base for implementation as such:
 *
 * Your main should be implemented as such
 * <code>
 *    public static void main (String[] args) {
 *    new check_[class]().process_request( args );
 *   }
 * </code>
 * 
 * The check_base.process_request(String[] args ) has the following flow
 * 1.  call super.init_command()
 * 2.  setLanguage to US TODO: This should be changed to support multi-language
 * 3.  call process_arguments( args ) which intern
 * 3a. calls super.add_command_arguments( options ), this enables super to add it's own parameters
 * 3b. parse command line
 * 3c. process base args (?,h,V,v,t)
 * 3d. for non-base args call super.process_command_option( option )
 * 3e. for remaining arguments call super.process_command_arguments( args_remaining)
 * 3f. all processed, enabled super to validate, super.validate_command_arguements()
 * 4. execute the check, super.execute_check
 * 5.  get the state and the message
 * 5a. check_state
 * 5b. check_message
 * 6. Print to stdout the message
 * 7. exit with state
 * 
 */
public abstract class check_base{

   private static final String COPYRIGHT = "Copyright (c) %s Blue Plugin Development Team\n\t<%s>\n\n";
   private static final String SUPPORT = "\nSend email to blue-devel@lists.sourceforge.ne if you have questions\nregarding use of this software. To submit patches or suggest improvements,\nsend email to blue-devel@lists.sourceforge.net\n";
   private static final String email = "blue-devel@lists.sourceforge.net";

   private String programName = super.getClass().getName();
   private String revision = "0.8.1";

   Locale currentLocale;
   ResourceBundle messages;

   /* Keep track of the number of verbose options.  */
   public int verbose = 0;
   public Options options;
   
   /** Set the plugin arguments */
   private String[] pluginArgs;
   
   /** Flag to indicate we are running in VM */
   private boolean inVm;
     
   public void setPluginArgs(String[] args)
   {
	   pluginArgs = args;
	   inVm = true;
	   
	   process_request(args);
   }
   
   public void process_request( String[] args ) {
      
      /* This was compiled in the legacy base, we can make this configurable, or leverage VM information */
      init_command();
      setLanguage( "en", "US", "C");
      
      try
      {
         process_arguments( args );
         execute_check();
      }
      catch (IllegalArgumentException iaE )
      {
         if(!inVm)
         {
        	 this.complete(iaE.getMessage(), common_h.STATE_UNKNOWN );
         }
      }
      
      if(!inVm)
      {
    	  complete( check_message(), check_state() );
      }
   }

   protected String _(String message)
   {
      if ( messages == null )
      {
         return message;
      }
      else
      {
         return messages.getString( message );
      }
   }

   public void setLanguage(String language, String country, String numeric ) {
      try {
         currentLocale = new Locale(language, country);
         messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);   
      } catch ( Exception e ) {
      }
   }

   /**
    * Handle the flow for processing command line arguments, as well as processing the common set.
    * TODO  if needed to move this to a in process, must remove the System.exit calls.
    * @param args Command line arguments
    * @return  true/false depending on processing results.
    */
   private final void process_arguments( String[] args )throws IllegalArgumentException 
   {

      options = utils_h.getStandardOptions();

      // call super to add it's command arguments.
      add_command_arguments( options );
      
      CommandLine cmd = null;
      try 
      {
			cmd = new PosixParser().parse(options, args);
      }
      catch (Exception e)
      {
		throw new IllegalArgumentException(programName + ": Could not parse arguments.");
	  }
      
      java.util.Iterator iter = cmd.iterator();
      while ( iter.hasNext() ) {
         Option o = (Option) iter.next();
         String optarg = o.getValue();
         
         if ( verbose > 0 )
            System.out.println( "processing "+o+"("+o.getId()+") " + o.getValue() );
      
         /* Process the basic command line agruments, default sends this to child for processing */
         switch ( o.getId() ) {
            case 'h':   /* help */
               print_help ();
               System.exit ( common_h.STATE_UNKNOWN);
               break;
            case 'V':   /* version */
               utils.print_revision ( programName, revision );
               System.exit (common_h.STATE_UNKNOWN);
               break;
            case 't':   /* timeout period */
               utils_h.timeout_interval = Integer.parseInt(optarg);
               break;
            case 'v':   /* verbose mode */
               verbose++;
               break;
         }
         /*  Allow extension to process all options, even if we already processed */
         process_command_option( o ); 
      }
      
      String[] argv = cmd.getArgs();
      if ( argv!=null && argv.length > 0 )
         process_command_arguments( argv  );
      
      validate_command_arguments ();
      
   }

   protected abstract String getCopyright();
   protected abstract String getAuthor();
   protected abstract String getDescription();
   protected abstract String getNotes();
   
   public abstract void init_command();
   public abstract void add_command_arguments( Options options );
   public abstract void process_command_option( Option o ) throws IllegalArgumentException;
   public abstract void process_command_arguments( String[] argv ) throws IllegalArgumentException;
   public abstract void validate_command_arguments() throws IllegalArgumentException;
     
   public abstract boolean execute_check();

   public abstract int check_state();
   public abstract String check_message();

   private void print_help ()
   {
      // plugin name
      utils.print_revision ( programName, revision );

      // copyright information
      System.out.printf ( COPYRIGHT , getCopyright(), email );
      
      // description 
      System.out.println( getDescription() );
      
      // print usage and options.
      HelpFormatter help = new HelpFormatter();
      help.printHelp( programName, options );
      
      // notes
      System.out.println( getNotes() );

      // Getting Help
      System.out.println( SUPPORT );
   }

   /**
    * Covering the completion in this space so we can rework
    * it for embedded in the future.
    * 
    * @param message  message to send/print
    * @param state common_h.[] defined states
    */
   private void complete( String message,  int state ) {
      System.out.println( message );
      System.exit( state );
   }
   
}