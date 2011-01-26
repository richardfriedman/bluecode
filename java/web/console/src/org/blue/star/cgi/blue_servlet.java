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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class blue_servlet extends HttpServlet {
    static Logger logger = LogManager.getLogger("org.blue.cgi");

    public static HttpServletResponse response = null;
    public static String request_string = null;
    private static final ReentrantLock lock = new ReentrantLock();

    public abstract void call_main( );
    public abstract void reset_context( );
    
    public void init() {
        
        cgiutils.is_servlet = true;
        
        cgiutils.nagios_cgi_config = this.getServletContext().getInitParameter("NAGIOS_CGI_CONFIG");
        cgiutils.nagios_command_file = this.getServletContext().getInitParameter("NAGIOS_COMMAND_FILE");

    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       String local_request_string = null;
       Enumeration e = req.getParameterNames();
       while ( e.hasMoreElements() ) {
          String name = (String) e.nextElement();
          String[] value = req.getParameterValues(name);
          if ( local_request_string == null )
             local_request_string = "";
          else 
             local_request_string += "&";
          
          if ( value != null ) {
               local_request_string += name + "=" + value[0];
               for ( int x = 1; x < value.length ; x++ ) 
                  local_request_string += "&" + name + "=" + value[x];
          } else {
             local_request_string += name;
          }
       }
       
       if ( local_request_string == null )
          local_request_string = "";

       doBlueServlet( local_request_string, req, res );

    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       String local_request_string = req.getQueryString();
       if ( local_request_string == null )
           local_request_string = "";

       doBlueServlet( local_request_string, req, res );
    }
    
    public void doBlueServlet(String local_request_string,HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       lock.lock();
       try { 
          
          cgiutils.reset_request_context();
          cgiutils.free_memory();
          reset_context();

          response = res;
          request_string = local_request_string;
          
          res.reset();
          PrintStream out = System.out;
          System.setOut(new PrintStream( res.getOutputStream() ));
          
           call_main( );
          
          System.setOut( out );
          request_string = null;
          response = null;
       } catch ( RuntimeException t ) {
          throw t;
       } finally {
          response = null;
          lock.unlock();
       }
       
    }
}
