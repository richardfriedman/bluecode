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

public class neberrors_h {

   /***** GENERIC DEFINES *****/
   public static final int NEB_OK                      = 0;
   public static final int NEB_ERROR                   = -1;
   
   public static final int NEB_TRUE                    = 1;
   public static final int NEB_FALSE                   = 0;
   
   
   
   /***** GENERIC ERRORS *****/
   public static final int NEBERROR_NOMEM             = 100;     /* memory could not be allocated */
   
   /***** CALLBACK ERRORS *****/
   public static final int NEBERROR_NOCALLBACKFUNC     = 200;     /* no callback function was specified */
   public static final int NEBERROR_NOCALLBACKLIST     = 201;     /* callback list not initialized */
   public static final int NEBERROR_CALLBACKBOUNDS     = 202;     /* callback type was out of bounds */
   public static final int NEBERROR_CALLBACKNOTFOUND   = 203;     /* the callback could not be found */
   public static final int NEBERROR_NOMODULEHANDLE     = 204;     /* no module handle specified */
   public static final int NEBERROR_BADMODULEHANDLE    = 205;     /* bad module handle */
   
   
   
   /***** MODULE ERRORS *****/
   public static final int NEBERROR_NOMODULE           =300;     /* no module was specified */
   
   
   
   /***** MODULE INFO ERRORS *****/
   public static final int NEBERROR_MODINFOBOUNDS      = 400;     /* module info index was out of bounds */
   
}
