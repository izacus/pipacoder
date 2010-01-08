/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright� 2009 Jernej Virag
 */

package org.kiberpipa.coder;

import java.io.IOException;

import org.kiberpipa.coder.jobs.JobManager;
import org.kiberpipa.coder.userinterfaces.WebInterface;

/**
 * @author Jernej Virag
 *
 */
public class Main
{
   public static void main(String[] args) throws IOException
   {    	   
      // Initializes the Job manager
      JobManager.getInstance();
      // Start web interface
      
      try
      {
         WebInterface webInt = new WebInterface(Integer.parseInt(Configuration.getValue("webport")));
      }
      catch(IOException e)
      {
         Log.error("[WebInterface] Failed to bind to socket port " + Configuration.getValue("webport"));
         Log.error(e.getMessage());
         
         System.exit(1);
      }
   }
}