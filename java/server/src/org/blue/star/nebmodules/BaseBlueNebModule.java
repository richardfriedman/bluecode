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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.blue.star.include.common_h;

/**
 * This is the base class that *can* be used by all Blue NebModules. It provides some common
 * functionality that may be useful such as registering callbacks, unregistering callbacks and
 * inspecting the class for methods to use as callback handles.
 * 
 * It should be noted that this class delegates the implementation of the methods within the
 * org.blue.nebmodules.BlueNebModule interface to any concrete implementation classes.
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1 Beta
 * 
 * @see - <b>org.blue.nebmodules.BlueNebModule</b> - The interface that all concrete classes will
 * have to implement.
 */
public abstract class BaseBlueNebModule implements BlueNebModule
{
	public Map<Integer,String> registeredCallbacks = new HashMap<Integer,String>();
	
	/* All concrete classes must implement their own method of registering callbacks */
	public abstract void registerCallbacks();
	
	/**
	 * This Method inspects the current class and returns a Method object of the given methodName if
	 * any can be found. It is used to provide the method that should be called as a callback by the
	 * Blue Server.
	 * 
	 * @param - String methodname, the name of the Method to return.
	 * @return - Method, the Method object representing said method, null if the method does not exist within the class.
	 */
	public Method getMethod(String methodName)
	{
		for(Method m: this.getClass().getMethods())
		{
			if(m.getName().equals(methodName))
				return m;
		}
		
		return null;
	} 
	
	/**
	 * This method registers a callback with Blue. It verifies that the method actually exists before attempting
	 * to register the callback.
	 * 
	 * @param callbackType - The callback type this method should be registered for.
	 * @param methodName - The name of the method to register for this callback.
	 * 
	 * @see - <b>org.blue.include.nebcallbacks_h</b> - The valid callback types.
	 */
	public void registerCallback(int callbackType,String methodName)
	{
		/* Verify that we have a method name and that we have a method that maps to that name */
		if(methodName == null || getMethod(methodName) == null)
			return;
		
		/* If the registration of the callback is successful, we store it in our registered callback table. */
		if(org.blue.star.base.nebmods.neb_register_callback(callbackType,this,0,getMethod(methodName)) == common_h.OK)
			registeredCallbacks.put(callbackType,methodName);
		else
			System.out.println("Error: NebModule '" + this.getClass().getName() + "' cannot register callback method '" + methodName + "'");
	}
	
	/**
	 * This Method unregisters all callbacks for this NebModule. Registered callbacks
	 * are stored within the registeredCallbacks Map associated with this class.
	 */
	public void unregisterCallbacks()
	{
		for(Iterator<Integer> i = registeredCallbacks.keySet().iterator();i.hasNext();)
		{
			int o = i.next();
			this.unregisterCallback(o,registeredCallbacks.get(o));
		}
	}
	
	/**
	 * This method unregisters a callback for this Module.
	 * 
	 * @param callbackType - The type of callback to unregister this method from.
	 * @param methodName - The name of the method to unregister 
	 */
	public void unregisterCallback(int callbackType,String methodName)
	{
		if(methodName == null || getMethod(methodName) == null)
			return;
		
		org.blue.star.base.nebmods.neb_deregister_callback(callbackType,getMethod(methodName));
	}
}
