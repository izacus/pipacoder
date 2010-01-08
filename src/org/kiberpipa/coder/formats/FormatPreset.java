 /**
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

package org.kiberpipa.coder.formats;

import java.util.ArrayList;

public class FormatPreset
{
   private int id;
   private String name;
   private ArrayList<OutputFormat> formats;
   
   public FormatPreset(int id, String name, ArrayList<OutputFormat> formats)
   {
      this.id = id;
      this.name = name;
      this.formats = formats;
   }

   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public ArrayList<OutputFormat> getFormats()
   {
      return formats;
   }

   public void setFormats(ArrayList<OutputFormat> formats)
   {
      this.formats = formats;
   }
}
