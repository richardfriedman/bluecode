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


public class cgiauth_h {
    
    public static class authdata{
        public String username;
        public int authorized_for_all_hosts;
        public int authorized_for_all_host_commands;
        public int authorized_for_all_services;
        public int authorized_for_all_service_commands;
        public int authorized_for_system_information;
        public int authorized_for_system_commands;
        public int authorized_for_configuration_information;
        public int authenticated;
    }
    
}