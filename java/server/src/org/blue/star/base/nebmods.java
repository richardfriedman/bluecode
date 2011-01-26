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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.blue.star.include.common_h;
import org.blue.star.include.nebcallbacks_h;
import org.blue.star.include.neberrors_h;
import org.blue.star.include.nebmods_h;
import org.blue.star.include.nebmodules_h;

public class nebmods
{
   
   /* Logger instance */ 
   private static Logger logger = LogManager.getLogger("org.blue.base.nebmods");
   
   /* Create neb_module_list and neb_callback_list */ 
   public static ArrayList<nebmodules_h.nebmodule> neb_module_list = new ArrayList<nebmodules_h.nebmodule>();
   public static HashMap<Integer,ArrayList<nebmods_h.nebcallback>> neb_callback_list = new HashMap<Integer,ArrayList<nebmods_h.nebcallback>>();
   
   /****************************************************************************/
   /****************************************************************************/
   /* INITIALIZATION/CLEANUP FUNCTIONS                                         */
   /****************************************************************************/
   /****************************************************************************/
   
   /* initialize module routines */
   public static int neb_init_modules()
   {
      return common_h.OK;
   }
   
   /* deinitialize module routines */
   public static int neb_deinit_modules()
   {
      return common_h.OK;
   }
   
   /* add a new module to module list */
   // Rob, 15/01/07 - Updated this method to take a String[] for args to the 
   // nebmodule...
   public static int neb_add_module(String filename,/* String args*/String[] args,int should_be_loaded)
   {
      
      if(filename == null)
         return common_h.ERROR;
      
      /* Create new instance of nebmodule object */
      nebmodules_h.nebmodule new_module = new nebmodules_h.nebmodule();
      
      /* initialize variables of new_module */
      
      new_module.filename = filename;
      
      // Required to deal with Event broker modules that have more than one argument
      // within the object definition file.
      
      if(args.length == 1)
    	  new_module.args = "";
      else
      {
    	  new_module.args = "";
    	  
    	  for(int i = 1;i < args.length; i++)
    	  {
    		  if(i == args.length-1)
    			  new_module.args += args[i];
    		  else
    			  new_module.args += args[i] + " ";
    	  }
      }
      
      new_module.should_be_loaded = should_be_loaded;
      new_module.is_currently_loaded = common_h.FALSE;
      
      for(int x=0;x< nebmodules_h.NEBMODULE_MODINFO_NUMITEMS;x++)
         new_module.info[x]=null;
      
      new_module.module_handle=null;
      new_module.init_func=null;
      new_module.deinit_func=null;
      new_module.thread_id=-1;
      
      /* Add it to the list of all current modules */
      neb_module_list.add(new_module);
      
      logger.info("Added module: name='"+new_module.filename+"', args='"+new_module.args+"', should_be_loaded='"+new_module.should_be_loaded+"'");
      return common_h.OK;
   }
   
   
   /* free memory allocated to module list */
   public static int neb_free_module_list()
   {
      neb_module_list.clear();
      return common_h.OK;
   }
   
   
   
   /****************************************************************************/
   /****************************************************************************/
   /* LOAD/UNLOAD FUNCTIONS                                                    */
   /****************************************************************************/
   /****************************************************************************/
   
   /* load all modules */
   public static int neb_load_all_modules()
   {
      
      for ( nebmodules_h.nebmodule temp_module : neb_module_list )
         neb_load_module( temp_module );
      
      return common_h.OK;
   }
   
   /* load a particular module */
   public static int neb_load_module( nebmodules_h.nebmodule mod)
   {
      Class neb_class = null;
      int module_version_ptr;
      
      if(mod== null|| mod.filename==null)
         return common_h.ERROR;
      
      /* don't reopen the module */
      if(mod.is_currently_loaded==common_h.TRUE)
         return common_h.OK;
      
      /* don't load modules unless they should be loaded */
      if(mod.should_be_loaded==common_h.FALSE)
         return common_h.ERROR;
      
      /* load the module TODO leverage a different class loader for this, for now keep it intact. */
      try 
      {
         neb_class = Class.forName( mod.filename );
         mod.module_handle = neb_class.newInstance();
      }
      catch (Exception e)
      {
         logger.fatal( "Error: Could not load module "+mod.filename+" -> " + e.getMessage() );
         logger.debug( "Exception", e );
         return common_h.ERROR;
      }
      
      /* mark the module as being loaded */
      mod.is_currently_loaded=common_h.TRUE;

      // TODO - Rob, this needs to be reviewed. Is this really necessary when utilising Java Interfaces.
      /* find module API version */ 
      try
      {
         module_version_ptr = neb_class.getField("_neb_api_version").getInt(mod.module_handle);
         
         /* check the module API version */ 
         if(module_version_ptr != nebmodules_h.CURRENT_NEB_API_VERSION )
            throw new IllegalArgumentException("API Version of Module '" + mod.filename + "' does not match current API version");
         
      }
      catch(Exception e)
      {
         logger.fatal( "Error: Module '"+mod.filename+"' is using an old or unspecified version of the event broker API.  Module will be unloaded.");
         neb_unload_module(mod,nebmodules_h.NEBMODULE_FORCE_UNLOAD,nebmodules_h.NEBMODULE_ERROR_API_VERSION);
      }
      
      /* Here we make sure that this class actually implements the BlueNebModule interface. If it 
       * does not, then it is not a valid Blue NebModule.
       */
            
      if(verifyModuleImplementsInterface(neb_class.getInterfaces()) == common_h.FALSE)
      {
    	  logger.fatal("Module '" + mod.filename + "' does not implement interface org.blue.nebmodules.BlueNebModule");
    	  neb_unload_module(mod,nebmodules_h.NEBMODULE_FORCE_UNLOAD,nebmodules_h.NEBMODULE_ERROR_BAD_INIT);
    	  return common_h.ERROR;
      }
      
      logger.debug("Module '" + mod.filename + "' implements interface org.blue.nebmodules.BlueNebModule");
        
      /* Attempt to locate the nebmodule_init(int,String,Object) method in the BlueNebModule */
      try
      {
         mod.init_func = neb_class.getMethod("nebmodule_init", new Class[]{int.class,String.class,Object.class});
      }
      catch ( Exception e )
      {
         /* If we were unable to find the method, log our error and unload the module */
    	 logger.fatal( "Error: Could not locate nebmodule_init() in module '"+mod.filename+"'.  Module will be unloaded.");
         neb_unload_module(mod,nebmodules_h.NEBMODULE_FORCE_UNLOAD,nebmodules_h.NEBMODULE_ERROR_NO_INIT);
         return common_h.ERROR;
      }
      
      /* Run the new found nebmodule_init method */
      try 
      {
         Integer init_result = (Integer) mod.init_func.invoke( mod.module_handle, new Object[] {nebmodules_h.NEBMODULE_NORMAL_LOAD,mod.args,mod.module_handle}  );
         
         /* if the init function returned an error, unload the module */
         if(init_result.intValue() != common_h.OK) 
            throw new IllegalStateException("nedmodule_init() Method of Module '" + mod.filename + "' did not result in a common_h.OK return status."); 
      }
      catch(Exception e )
      {
          e.printStackTrace();
    	  logger.fatal( "Error: Function nebmodule_init() in module '"+mod.filename+"' returned an error.  Module will be unloaded.");
         neb_unload_module(mod,nebmodules_h.NEBMODULE_FORCE_UNLOAD,nebmodules_h.NEBMODULE_ERROR_BAD_INIT);
         return common_h.ERROR;
      }
      
      logger.info( "Event broker module '"+mod.filename+"' initialized successfully.");
      
      /* locate the de-initialization function (may or may not be present) */
      try
      {
         mod.deinit_func = neb_class.getMethod("nebmodule_deinit", new Class[]{ int.class ,int.class} );
         logger.debug("Module '"+mod.filename+"' loaded ");
         
         if ( mod.deinit_func != null ) 
            logger.debug("\tnebmodule_deinit() found in Module '" + mod.filename + "'");
      }
      catch( Exception e )
      {
         logger.debug( "Error: Could not locate nebmodule_deinit() in module '"+mod.filename+"'.  Module will be unloaded.");
      }
      
      return common_h.OK;
   }
   
   /* close (unload) all modules that are currently loaded */
   public static int neb_unload_all_modules(int flags, int reason){
      
      for ( nebmodules_h.nebmodule temp_module : neb_module_list ) {
         
         /* skip modules that are not loaded */
         if(temp_module.is_currently_loaded==common_h.FALSE)
            continue;
         
         /* skip modules that do not have a valid handle */
         if(temp_module.module_handle==null)
            continue;
         
         /* close/unload the module */
         neb_unload_module(temp_module,flags,reason);
      }
      
      return common_h.OK;
   }
   
   /* close (unload) a particular module */
   public static int neb_unload_module(nebmodules_h.nebmodule mod, int flags, int reason){
      
      if(mod==null)
         return common_h.ERROR;
      
      logger.debug("Attempting to unload module '"+mod.filename+"': flags="+flags+", reason="+reason );
      
      /* call the de-initialization function if available (and the module was initialized) */
      if(mod.deinit_func != null && reason!=nebmodules_h.NEBMODULE_ERROR_BAD_INIT){
         
         /* module can opt to not be unloaded */
         Integer deinit_result = null;
         try 
         {
            deinit_result = (Integer) mod.deinit_func.invoke(mod.module_handle, new Object[] { flags, reason } );
         }
         catch (Exception e)
         {
            e.printStackTrace();
        	logger.warn( "Deinit Failed " + mod.filename );
         }
         
         /* if module doesn't want to be unloaded, exit with error (unless its being forced) */
         if(deinit_result.intValue()!=common_h.OK && (0==(flags & nebmodules_h.NEBMODULE_FORCE_UNLOAD)))
            return common_h.ERROR;
      }
      
      /* deregister all of the module's callbacks */
      neb_deregister_module_callbacks(mod);
      
      /* unload the module */
      /* TODO HOw to remove from the class loader.  Should each BROKER be loaded in a seperate class loader. */
      
      /* mark the module as being unloaded */
      mod.is_currently_loaded=common_h.FALSE;
      
      logger.debug("Module '"+mod.filename+"' unloaded successfully.");
      
      logger.info("Event broker module '"+mod.filename+"' deinitialized successfully.");
      
      return common_h.OK;
   }
   
   /****************************************************************************/
   /****************************************************************************/
   /* INFO FUNCTIONS                                                           */
   /****************************************************************************/
   /****************************************************************************/
   
   /* sets module information */
   public static int neb_set_module_info( Object handle, int type, String data){
      nebmodules_h.nebmodule mod;
      
      if(handle==null)
         return neberrors_h.NEBERROR_NOMODULE;
      
      /* check type */
      if(type<0 || type>=nebmodules_h.NEBMODULE_MODINFO_NUMITEMS)
         return neberrors_h.NEBERROR_MODINFOBOUNDS;
      
      /* get the module */
      mod=(nebmodules_h.nebmodule)handle;
      
      /* allocate memory for the new data */
      if(data==null)
         mod.info[type]=null;
      else{
         mod.info[type]=data;
         if(mod.info[type]==null)
            return neberrors_h.NEBERROR_NOMEM;
      }
      
      return common_h.OK;
   }
   
   
   /****************************************************************************/
   /****************************************************************************/
   /* CALLBACK FUNCTIONS                                                       */
   /****************************************************************************/
   /****************************************************************************/
   
   /**
    * This method allows modules to register callbacks for certain Blue Event Types.
    * 
    * @param - int callback_type, the type of callbacks that this module wishes to register for.
    * @param - Object mod_handle, a reference to the module itself.
    * @param - int priority, the priority with which this module should be executed.
    * @param - Method callback_func, the method to execute when making callbacks.
    * 
    * @see - <b>org.blue.include.nebcallbacks_h</b> - The valid callback types.
    * @see - <b>org.blue.nebmodules.BlueNebModule</b> - The interface all Blue NEBModules must implement.
    */
   public static int neb_register_callback(int callback_type, Object mod_handle, int priority, Method callback_func )
   {
      nebmodules_h.nebmodule temp_module = null;
      nebmods_h.nebcallback new_callback = null;
      nebmods_h.nebcallback temp_callback = null;
      
      /* Make sure that we have a callback method */
      if(callback_func == null)
        return neberrors_h.NEBERROR_NOCALLBACKFUNC;
      
      /* Make sure that the callback list has been initialised */
      if(neb_callback_list==null )
        return neberrors_h.NEBERROR_NOCALLBACKLIST;
      
      /* Make sure that we have a reference to the module */
      if(mod_handle==null)
        return neberrors_h.NEBERROR_NOMODULEHANDLE;
      
      /* make sure the callback type is within bounds */
      if(callback_type<0 || callback_type>=nebcallbacks_h.NEBCALLBACK_NUMITEMS)
        return neberrors_h.NEBERROR_CALLBACKBOUNDS;
      
      
      /* make sure module handle is valid */
      for( ListIterator<nebmodules_h.nebmodule> iter = neb_module_list.listIterator(); iter.hasNext(); ) {
         temp_module = iter.next();
         if( temp_module.module_handle == mod_handle)
            break;
      }
      
      /* If we couldn't find our module, return */
      if( temp_module==null )
        return neberrors_h.NEBERROR_BADMODULEHANDLE;
      
      /* allocate memory */
      new_callback= new nebmods_h.nebcallback ();
      
      new_callback.priority=priority;
      new_callback.module_handle= mod_handle;
      new_callback.callback_func= callback_func;
      
      /* add new function to callback list, sorted by priority (first come, first served for same priority) */    
      if(!neb_callback_list.containsKey(callback_type))
      {
         ArrayList<nebmods_h.nebcallback> callback_type_list = new ArrayList<nebmods_h.nebcallback>();
         callback_type_list.add( new_callback );
         neb_callback_list.put( callback_type, callback_type_list );
      }
      else
      {
          ArrayList<nebmods_h.nebcallback> callback_type_list = neb_callback_list.get(callback_type);
         int index_callback = 0;
         
         /* If there is nothing currently in the list, simply add our callback */
         if(callback_type_list.size() == 0)
         {
        	 callback_type_list.add(new_callback);
        	 return common_h.OK;
         }
         
         /* Otherwise we must calculate where to insert into the list based around the callback priority */
         for(ListIterator<nebmods_h.nebcallback> iter = callback_type_list.listIterator(); iter.hasNext(); index_callback++ )
         {
            temp_callback = iter.next();
            
            if(temp_callback.priority > new_callback.priority )
            {
               callback_type_list.add(index_callback,new_callback);
               break;
            }
         }
      }
      
      return common_h.OK;
   }
   
   /* dregisters all callback functions for a given module */
   public static int neb_deregister_module_callbacks(nebmodules_h.nebmodule mod){
      
      if(mod==null)
         return neberrors_h.NEBERROR_NOMODULE;
      
      if(neb_callback_list==null)
         return common_h.OK;
      
      for ( int callback_type : neb_callback_list.keySet() ) {
         ArrayList<nebmods_h.nebcallback> nebcallback_type_list = neb_callback_list.get(callback_type);
         for ( ListIterator<nebmods_h.nebcallback> iter = nebcallback_type_list.listIterator(); iter.hasNext(); ) {
            nebmods_h.nebcallback temp_callback = iter.next();
            logger.info("TEMP_CALLBACK: " + temp_callback);
            if ( temp_callback.module_handle == mod.module_handle ) {
               neb_deregister_callback(callback_type, temp_callback.callback_func);
            }
         }
      }
      
      return common_h.OK;
   }
   
   
   /**
    *  allows a module to deregister a callback function
    * 
    *  @param callback_type type of callback method.
    *  @param method int (*callback_func)(int,void *)
    */
   public static int neb_deregister_callback(int callback_type, Method callback_func ){
      
      if(callback_func==null)
         return neberrors_h.NEBERROR_NOCALLBACKFUNC;
      
      if(neb_callback_list==null)
         return neberrors_h.NEBERROR_NOCALLBACKLIST;
      
      /* make sure the callback type is within bounds */
      if(callback_type<0 || callback_type>=nebcallbacks_h.NEBCALLBACK_NUMITEMS)
         return neberrors_h.NEBERROR_CALLBACKBOUNDS;
      
      /* find the callback to remove */
      boolean callback_removed = false;
      
      ArrayList<nebmods_h.nebcallback> callbacks = neb_callback_list.get(callback_type);
      
      if(callbacks == null || callbacks.size() == 0)
      {
    	  logger.debug(".neb_deregister_callback() - Cannot Retrieve Callback List to Deregister Callback");
    	  return neberrors_h.NEBERROR_NOCALLBACKLIST;
      }
      
      for ( nebmods_h.nebcallback temp_callback : callbacks)
      {
         if(temp_callback.callback_func==callback_func)
         {
            neb_callback_list.get(callback_type).remove( temp_callback );
            callback_removed = true;
            break;
         }
      }
      
      /* we couldn't find the callback */
      if( !callback_removed )
         return neberrors_h.NEBERROR_CALLBACKNOTFOUND;
      
      return common_h.OK;
   }
   
   /* make callbacks to modules */
   public static int neb_make_callbacks(int callback_type, Object data)
   {
      
      /* make sure callback list is initialized */
      if(neb_callback_list==null)
         return common_h.ERROR;
      
      /* make sure the callback type is within bounds */
      if(callback_type<0 || callback_type>=nebcallbacks_h.NEBCALLBACK_NUMITEMS)
         return common_h.ERROR;
      
      /* make the callbacks... */
      ArrayList<nebmods_h.nebcallback> list = neb_callback_list.get(callback_type);
      
      if ( list == null )
    	  return common_h.ERROR;
      
      for ( nebmods_h.nebcallback temp_callback : list )
      {
         Object result = null;
         
         try
         {
        	result = temp_callback.callback_func.invoke(temp_callback.module_handle, new Object[] {callback_type, data } );
            logger.debug("Callback type "+callback_type+" resulted in return code of " + result );
         } 
         catch(Exception e )
         {
            logger.warn( "Failed: Callback type "+callback_type+" resulted in return code of " + result );
         }
      }
      
      return common_h.OK;
   }
   
   /* initialize callback list */
   public static int neb_init_callback_list( ){
      int x;
      
      /* allocate memory for the callback list */
      neb_callback_list.clear();
      
      /* initialize list pointers */
      for(x=0;x<nebcallbacks_h.NEBCALLBACK_NUMITEMS;x++)
         neb_callback_list.put(x, new ArrayList<nebmods_h.nebcallback>() );
      
      return common_h.OK;
   }
   
   /* free memory allocated to callback list */
   public static int neb_free_callback_list( ){
      
      neb_callback_list.clear();
      return common_h.OK;
      
   }
   
   /* Private Method to check to see if our NEBModule class implements the BlueNebModule interface 
    * 
    * @param - Class[], the interfaces implemented by the BlueNebModule
    * 
    * @return - int, common_h.TRUE if the module implements the required interface.
    */
   
   private static int verifyModuleImplementsInterface(Class[] interfaces)
   {
	   for(Class c: interfaces)
	   {
		   /* Remember that interface comparison is against the fully qualified name of the interface */
		   if(c.getName().equals("org.blue.star.nebmodules.BlueNebModule"))
			   return common_h.TRUE;
	   }
	   
	   return common_h.FALSE;
   }
}
