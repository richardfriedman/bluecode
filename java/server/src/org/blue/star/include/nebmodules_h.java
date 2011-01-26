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

package org.blue.star.include;

import java.lang.reflect.Method;

public class nebmodules_h {
   
   /***** MODULE VERSION INFORMATION *****/
   
// #define NEB_API_VERSION(x) int __neb_api_version = x;
   public static final int CURRENT_NEB_API_VERSION    =2;
   
   /***** MODULE INFORMATION *****/
   public static final int  NEBMODULE_MODINFO_NUMITEMS  = 6;
   public static final int  NEBMODULE_MODINFO_TITLE     = 0;
   public static final int  NEBMODULE_MODINFO_AUTHOR     = 1;
   public static final int  NEBMODULE_MODINFO_COPYRIGHT = 2;
   public static final int  NEBMODULE_MODINFO_VERSION   = 3;
   public static final int  NEBMODULE_MODINFO_LICENSE   = 4;
   public static final int  NEBMODULE_MODINFO_DESC      = 5;
   
   /***** MODULE LOAD/UNLOAD OPTIONS *****/
   public static final int  NEBMODULE_NORMAL_LOAD       =0;    /* module is being loaded normally */
   public static final int  NEBMODULE_REQUEST_UNLOAD    = 0;    /* request module to unload (but don't force it) */
   public static final int  NEBMODULE_FORCE_UNLOAD      = 1;    /* force module to unload */
   
   /***** MODULES UNLOAD REASONS *****/
   public static final int  NEBMODULE_NEB_SHUTDOWN      =1;    /* event broker is shutting down */
   public static final int  NEBMODULE_NEB_RESTART       =2;    /* event broker is restarting */
   public static final int  NEBMODULE_ERROR_NO_INIT     =3;    /* _module_init() function was not found in module */
   public static final int  NEBMODULE_ERROR_BAD_INIT    =4;    /* _module_init() function returned a bad code */
   public static final int  NEBMODULE_ERROR_API_VERSION =5;    /* module version is incompatible with current api */
   
   /***** MODULE STRUCTURES *****/
   
   /* NEB module structure */
   public static class nebmodule{
      public String filename;
      public String args;
      public String[] info = new String[NEBMODULE_MODINFO_NUMITEMS];
      public int should_be_loaded;
      public int is_currently_loaded;
      public Object module_handle;
      public Method init_func;
      public Method deinit_func;
      public int thread_id;
   }
}
