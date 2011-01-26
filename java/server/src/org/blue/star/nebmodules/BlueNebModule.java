/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
 *
 * Copyright (c) 2006-2007 Richard Friedman (blue@osadvisors.com)
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
package org.blue.star.nebmodules;

/**
 * <p>This is the interface that all Blue Event Broker Modules must implement!.
 * It provides method signatures that allow Blue to initilise and de-initialise the module. It also contains
 * a field that allows Blue to check the current version of the API that this module utilises.</p>
 * 
 * <p>To register this module for a specific type of event within the Blue Event Brokering structure, you
 * should make use of the org.blue.base.nebmods.neb_register_callback() method.</p>
 * 
 * <p>To unregister this module for a specific type of event within the Blue Event Brokering structure, you
 * should make use of the org.blue.base.nedmods.neb_unregister_callback() method</p>
 * 
 * <p>To set information about this module with the main Blue process, you should make use of the 
 * org.blue.base.nebmods.neb_set_module_info() method.
 * </p>
 * 
 * <p>Care should be taken to ensure that all BlueNebModules return as quickly as possible and that
 * all BlueNebModules operate in a thread safe manner.</p>
 * 
 * <p>If using a BlueNebModule to alter the internal configuration of Blue, you should proceed extremely
 * carefully as mistakes can result in the loss of monitoring information and loss of Blue functionality.</p>
 * 
 * @see <b>org.blue.include.nebcallbacks</b> - for the list of available callback types for which you can register 
 * this BlueNebModule.<br/><br/> 
 * @see <b>org.blue.base.nebmods.neb_unregister_callback()</b> - Method signature for unregistering callbacks.<br/><br/>
 * @see <b>org.blue.base.nebmods.neb_register_callback()</b> - Method signature for registereing callbacks.<br/><br/>
 * @see <b>org.blue.base.nedmods.neb_set_module_info()</b> - Method signature for setting Module Info with Blue.<br/><br/>
 * @author Rob.Blake@arjuna.com
 */

public interface BlueNebModule
{
	/** Variable reflecting the current version of the Blue NEB API in use */
	final public int _neb_api_version = org.blue.star.include.nebmodules_h.CURRENT_NEB_API_VERSION;
	
	/**
	 * This is the Initialisation routine for the module. This includes instantiating internal
	 * variables, opening connections to remote resources and other such prepatory tasks. You
	 * should also use this method to register all callbacks.
	 * 
	 * @param - int, The Event Type with which this module has been initialised.
	 * @param - String, any arguments that have been passed to this module.
	 * @param - Object, this will be a pointer to an instance of this class contained within Blue of the same type as your BlueNebModule. This should be assigned to an internal variable and used for registering for callbacks.
	 * 
	 * @return - This should be org.blue.include.common_h.OK (0) if your module has initialised correctly.
	 */
	public int nebmodule_init(int eventType,String args,Object handle);
	
	/**
	 * This is the De-Initialisation routine for the module. This should be used to free memory
	 * and close connections to remote resources in a clean manner. You should also use this method
	 * to remove all callbacks.
	 * 
	 * @param - int, any flags associated with the de-initialisation of the module
	 * @param - int, the reason why the module has been de-initialised.
	 */
	public int nebmodule_deinit(int flags, int reason);
		
	
}
