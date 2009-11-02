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
    
    Copyright© 2009 Jernej Virag
 */

package org.kiberpipa.coder;

import java.io.IOException;

/**
 * @author Jernej Virag
 *
 */
public class Main
{

   public static void main(String[] args) throws IOException
   {    	   
	  Database.poke(); 
	   
      // Setup test objects
      OutputFormat format = new OutputFormat(0, "x264/aac", ".test.mp4", "libx264", 872, 486, 700000, "libfaac", 2, 48000, 128000);
      
      JobManager jobManager = JobManager.getInstance();
      jobManager.addJob("./input/casper.flv", format);
   }

}