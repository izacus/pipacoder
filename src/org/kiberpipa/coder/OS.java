 /*
  *     
  * This file is part of PipaCoder.

    PipaCoder is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PipaCoder is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with PipaCoder.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright� 2009 Jernej Virag
  */

package org.kiberpipa.coder;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * This class contain OS specific JNA-calling code
 * @author Jernej
 *
 */
public class OS
{
   private interface UIDLib extends Library
   {
      UIDLib INSTANCE = (UIDLib)Native.loadLibrary("libuid.so", UIDLib.class);
      
      int getUID();
      int setUID(int uid);
   }
   
   public static int setUID(int uid)
   {
      return UIDLib.INSTANCE.setUID(uid);
   }
   
   public static int getUID()
   {
      return UIDLib.INSTANCE.getUID();
   }
}
