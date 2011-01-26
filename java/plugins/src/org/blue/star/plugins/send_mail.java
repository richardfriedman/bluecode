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

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.sun.mail.smtp.SMTPTransport;

public class send_mail extends check_base
{
   protected String getAuthor() { return "Richard Friedman richardfriedman@yahoo.com"; }
   protected String getCopyright() { return "Richard Friedman 2007"; }
   protected String getDescription() { return "Java Send Mail client."; }
   protected String getNotes() { return ""; }
   
   
   private String message;
   private String subject;
   private String from;
   private String to;
   private String smtpServer = "localhost";
   private String smtpUser = null;
   private String smtpPass = null;
   private boolean smtpAuth = false;
   private boolean ssl = false;

   public String progname = "java org.blue.plugins.send_mail";
   public String revision = "$Revision: 1.2 $";
   public String copyright = "2006";
   public String email = "akuns@users.sourceforge.net";

   private int state;
   private String text;
   
   public static void main (String[] args) {
      new send_mail().process_request( args );
   }
      
   public void init_command() {
   }

   /* Add options specific to this plugin */
   public void add_command_arguments( Options options ) {
       options.addOption( new Option( "M", "MESSAGE", true, "Message text" ) );
       options.addOption( new Option( "S", "SUBJECT", true, "Message subject" ) );
       options.addOption( new Option( "F", "FROM", true, "From" ) );
       options.addOption( new Option( "T", "TO", true, "From" ) );
       options.addOption( new Option( "s", "smtp", true, "smtp server" ) );
       options.addOption( new Option( "U", "USER", true, "smtp user" ) );
       options.addOption( new Option( "P", "PASS", true, "smtp password" ) );
       options.addOption( new Option( "A", "AUTH", false, "Authorization should be used (true|false)" ) );
       options.addOption( new Option( "L", "SSL", false, "SSL should be used (true|false)" ) );
   }

   /* process command-line arguments */
   public void process_command_option ( Option o )
         throws IllegalArgumentException
   {
         String optarg = o.getValue();
                  
         switch ( o.getId() ) {
            case 'M': 
               this.message = optarg;
               break;
            case 'S': 
               this.subject = optarg;
               break;
            case 'F': 
               this.from = optarg;
               break;
            case 'T': 
               this.to = optarg;
               break;
            case 's': 
               this.smtpServer = optarg;
               break;
            case 'U': 
               this.smtpUser = optarg;
               break;
            case 'P':
               this.smtpPass = optarg;
               break;
            case 'A': 
               this.smtpAuth = true;
               break;
            case 'L': 
                this.ssl = true;
                break;
         }
         
   }

   public void process_command_arguments( String[] argv )
      throws IllegalArgumentException {
   }
   
   
   /**
    * Validate the argument set.
    */
   public void validate_command_arguments ()
      throws IllegalArgumentException {

      if ( this.message == null  && this.subject == null ) {
         throw new IllegalArgumentException( "No subject and body available." );
      }
      
      if ( this.to == null ) {
         throw new IllegalArgumentException ( "No to address specified. ");
      }
      
   }
   
   public boolean execute_check() {

      Properties props = System.getProperties();
      props.put( "mail.smtp.host", smtpServer );
      if (smtpAuth) {
        props.put("mail.smtp.auth", "true");
      }
      
      Session session = Session.getInstance(props, null);
      SMTPTransport transport  = null;
//      if (debug)
//         session.setDebug(true);

      // construct the message
      try {
	    Message msg = new MimeMessage(session);
	    if (from != null)
		msg.setFrom(new InternetAddress(from));
	    else
		msg.setFrom();

	    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse( to, false));

	    if (subject != null)
		msg.setSubject(subject);

	    msg.setHeader("X-Mailer", "blue-send-mail");
	    msg.setSentDate(new Date());
	    msg.setText(message.replace("\\n", "\n").replace("\\t", "\t"));

	    transport = (SMTPTransport)session.getTransport(ssl ? "smtps" : "smtp");
	    if (smtpAuth)
		transport.connect(smtpServer, smtpUser, smtpPass);
	    else
		transport.connect();
	    transport.sendMessage(msg, msg.getAllRecipients());
	    transport.close();
	    
	} catch (MessagingException mE) {
	    mE.printStackTrace();
	    this.state = common_h.STATE_CRITICAL;
	    this.text = mE.getMessage();
	    return false;
	} finally {
	    try { transport.close(); } catch (Exception e) {}
	}
      
      state = common_h.STATE_OK;
      text = "Message Sent!";
      return true;
   }

   public int check_state() {
      return state;
   }
   
   public String check_message () {
      return text;
   }
   
}
