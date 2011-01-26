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

/*
 * This class forms the basis of a thread that performs operations upon the external command file.
 * The external command file is written to during the operation of Blue and it used for 
 * running things such as Event Handlers or processing the results of passive checks sent to the Blue
 * monitoring server.
 * 
 * N.B : Edited names of variables for clarity.
 */

package org.blue.star.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.blue_h;
import org.blue.star.include.common_h;




public class command_file_worker_thread extends Thread 
{
     
    /** Logger instance */
    private static Logger logger = LogManager.getLogger("org.blue.base.command_file_worker_thread");
    
    /* Externalized mechanism to kill processing thread */
    private static boolean halt = false;
    
    /* Filename to be reading commands from */
    private BufferedReader commandFileReader;
    private FileChannel commandFileChannel;
    
    public command_file_worker_thread(FileChannel channel)
    {
        logger.debug( "Creating command_file_worker_thread");
        commandFileChannel = channel;
        commandFileReader = new BufferedReader( Channels.newReader(commandFileChannel, "ISO-8859-15" ) );
    }
    
    public void run ()
    {
        halt = false;
        this.run_command_file_worker_thread();
    }
    
    /* initializes command file worker thread */
    
    public static int init_command_file_worker_thread()
    {

        /* initialize circular buffer */
        blue.external_command_buffer.buffer.clear();
        
        /* create worker thread */
        command_file_worker_thread t = new command_file_worker_thread( blue.command_file_channel );
        blue.worker_threads[ blue_h.COMMAND_WORKER_THREAD ] = t;
        t.setDaemon(true);
        t.setName( "Command File Worker Thread");
        t.start();
        
        logger.debug("COMMAND FILE THREAD: "+ blue.worker_threads[blue_h.COMMAND_WORKER_THREAD].getId());
        
        return common_h.OK;
    }
    
    /* shutdown command file worker thread */
    public static int shutdown_command_file_worker_thread()
    {
        
        /* wait for the worker thread to exit */
        try 
        {
            halt = true;
            blue.worker_threads[ blue_h.COMMAND_WORKER_THREAD ].join();
        }
        catch ( Exception e )
        {
        	
        }
        
        return common_h.OK;
    }
    
    /* clean up resources used by command file worker thread */
    public static void cleanup_command_file_worker_thread()
    {
        /* release memory allocated to circular buffer */
        blue.external_command_buffer.buffer.clear();
        return;
    }
    
    
    /* worker thread - artificially increases buffer of named pipe */
    public void run_command_file_worker_thread() {
       
        try {
            while(!halt)  {                

               logger.debug("(CFWT) BUFFER ITEMS: "+blue.external_command_buffer.buffer.size()+"/" + blue_h.COMMAND_BUFFER_SLOTS);

               boolean rewindFile = false;
                
                /* process all commands in the file (named pipe) if there's some space in the buffer */
                if (blue.external_command_buffer.buffer.remainingCapacity() > 0 ) {
                    
                    /* read and process the next command in the file */
                    String commandString = commandFileReader.readLine();
                    
                    while(!halt && commandString != null) { 
                       rewindFile = true;
                       logger.info("(CFWT) READ: " + commandString);
                       
                       while (!halt && !utils.submit_external_command(commandString, 500)) 
                            ;
                        
                        // TODO understand the mechanism breaking when circular buffer is full.
                        commandString = commandFileReader.readLine();
                    }
                }
                
                /* Rewind the file */
                if(rewindFile) {
                   FileLock lock = commandFileChannel.tryLock();
                   
                   if (lock != null) {
                	   commandFileChannel.truncate(0);
                       commandFileChannel.position(0);
                   }
                   
                   
                   lock.release();
                }
                
                /**** POLL() AND SELECT() DON'T SEEM TO WORK ****/
                Thread.sleep(500);
            }
            
        }
        /* If any errors occur, try to gracefully close the commandFileChannel. */
        
        catch ( IOException ioE )
        {
            try 
            {
            	commandFileReader.close();
            }
            catch ( Exception e )
            {} 
        }
        catch (InterruptedException iE )
        {
            try
            {
            	commandFileReader.close();
            }
            catch (Exception e)
            {}
        }

        logger.info( "out Running Thread");
        try
        {
        	commandFileReader.close();
        }
        catch(Exception e)
        {
        	logger.debug("Unable to close Command File!");
        }
        
        /* removes cleanup handler - this should never be reached */
        cleanup_command_file_worker_thread();
    }
    
}
