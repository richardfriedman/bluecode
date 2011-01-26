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

import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;

public class service_result_worker_thread extends Thread
{
    
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.service_result_worker_thread");
    private static String cn = "org.blue.base.service_result_worker_thread";
    
    /* Externalized mechanism to kill processing thread */
    private static boolean halt = false;

//    private Selector selector = null;
//    private Pipe.SourceChannel source = null;
    
    public void run ()
    {
        halt = false;
        this.run_service_result_worker_thread();
    }
    
    /* initializes service result worker thread */
    public static int init_service_result_worker_thread()
    {
        
        /* initialize circular buffer */
        blue.service_result_buffer.buffer.clear();
        
        /* create worker thread */
        service_result_worker_thread t = new service_result_worker_thread( );
        t.setName( "Service Result Worker Thread");

//        try {
//            t.source = blue.ipc_pipe.source();
//            
//            t.source.configureBlocking(false);
//            t.selector = Selector.open();
//            t.source.register( t.selector, t.source.validOps());
//                
//            
//        } catch ( IOException ioE ) {
//            logger.fatal( "Pipe creation failed.");
//            logger.throwing(cn, "init_service_result_worker_thread", ioE);
//            return common_h.ERROR;
//        } catch ( Throwable T ) {
//            logger.fatal( "Throwing " + T.getMessage() );
//            T.printStackTrace();
//            return common_h.ERROR;
//        }
        
        blue.worker_threads[ blue_h.SERVICE_WORKER_THREAD ] = t;
        t.start();
        
        return common_h.OK;
    }
    
    /* shutdown command file worker thread */
    public static int shutdown_service_result_worker_thread()
    {
        
        /* wait for the worker thread to exit */
        try
        {
            halt = true;
            blue.worker_threads[ blue_h.SERVICE_WORKER_THREAD ].join();
        }
        catch( Exception e )
        {}
        
        return common_h.OK;
    }
    
    /* clean up resources used by service result worker thread */
    
    public static void cleanup_service_result_worker_thread()
    {
        blue.service_result_buffer.buffer.clear();
        return;
    }
    
    
    /* worker thread - artificially increases buffer of named pipe */
    public void run_service_result_worker_thread() {
       
        logger.trace( "entering " + cn + ".run_service_result_worker_thread ");
    
        while(!halt)
        {

            try
            {
            	Object o = blue.ipc_queue.poll( 250, TimeUnit.MILLISECONDS );
            	if ( o != null )
                
            		while (!halt && !blue.service_result_buffer.buffer.offer( o, 500, TimeUnit.MILLISECONDS ));
            		//TODO - are we supposed to be doing something here?
            }
            catch (InterruptedException iE )
            {
            
            }
//            while ( !halt && !blue.service_result_buffer.buffer.offer( message, 500, TimeUnit.MILLISECONDS ) );
//                /* wait for data to arrive */
//                /* TODO test, make sure we do not need to revert select seems to not work, so we have to use poll instead */
//                try {
//                    // Wait for an event
//                    selector.select();
//                } catch (IOException e) {
//                    // Handle error with selector
//                    break;
//                }
//          
//                // Get list of selection keys with pending events
//                Iterator it = selector.selectedKeys().iterator();
//                
//                /* process data in the pipe (one message max) if there's some free space in the circular buffer */
//                if ( blue.service_result_buffer.buffer.remainingCapacity() > 0 ) {
//                 
//                    // Process each key at a time
//                    while ( !halt && it.hasNext()) {
//                        // Get the selection key
//                        SelectionKey selKey = (SelectionKey)it.next();
//                        
//                        // Remove it from the list to indicate that it is being processed
//                        it.remove();
//                        
//                        try {
//                            if (selKey.isValid() && selKey.isReadable()) {
//                                
//                                nagios_h.service_message message = (nagios_h.service_message) oiStream.readObject();
//                                
//                                /* the read was good, so save it */
//                                try {
//                                    while ( !halt && !blue.service_result_buffer.buffer.offer( message, 500, TimeUnit.MILLISECONDS ) );
//                                } catch (InterruptedException iE ) { 
//                                    logger.throwing( cn, "run_service_result_worker_thread", iE );
//                                } catch (NullPointerException npE ) {
//                                    logger.throwing( cn, "run_service_result_worker_thread", npE );
//                                }
//                            }
//                        } catch (IOException e) {
//                            // Handle error with channel and unregister
//                            selKey.cancel();
//                        } catch (ClassNotFoundException cnfE ) {
//                            logger.throwing( cn, "run_service_result_worker_thread", cnfE );
//                        }
//                }
//                
//            }

        }
        
        logger.info( "out Running Thread");

        /* removes cleanup handler - this should never be reached */
        cleanup_service_result_worker_thread();
        logger.trace( "exiting " + cn + ".run_service_result_worker_thread ");
    }
    
}
