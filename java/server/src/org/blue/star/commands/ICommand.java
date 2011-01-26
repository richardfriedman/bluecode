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

/**
 * This interface is a new mechanism (Feb 2007) by which to add commands
 * to the base server.  While the interface is made for calling commands within
 * the server, it is also a best practice to define methods for executing a command.
 * 
 * The method getCommandString() should return a parameterized string which represents
 * this command.  For example add_host will generate a command string.
 * EXECUTE_JAVA_COMMAND;org.blue.commands.add_host;%s;%s
 * 
 * The method getCommandName() should return the name of the command.  This will be used 
 * in the future to map all existing commands into the engine. 
 * ie. getCommandName() for existing java commands would be EXECUTE_JAVA_COMMAND

 */
package org.blue.star.commands;

public interface ICommand {
   public void processCommand( long timestamp, String args );
   public String getCommandString();
   public String getCommandName();
}
