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
   private Integer id;
   private String name;
   private ArrayList<OutputFormat> formats;
   
   public FormatPreset(int id, String name)
   {
      this.id = id;
      this.name = name;
      formats = new ArrayList<OutputFormat>();
   }

   public Integer getId()
   {
      return id;
   }

   public void setId(Integer id)
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
   
   public void addFormat(OutputFormat format)
   {
	   formats.add(format);
   }

   public void setFormats(ArrayList<OutputFormat> formats)
   {
      this.formats = formats;
   }
}
