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
    
    Copyright© 2009 Jernej Virag
  */

package org.kiberpipa.coder.formats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.kiberpipa.coder.Database;

public class FormatManager 
{
	// This is a singleton class
	private static FormatManager instance = null;
	
	public static FormatManager getInstance()
	{
		if (FormatManager.instance == null)
		{
			FormatManager manager = new FormatManager();
			FormatManager.instance = manager;
		}
		
		return FormatManager.instance;
	}
	
	private HashMap<Integer, OutputFormat> formats = null;
	
	private FormatManager()
	{
		this.formats = new HashMap<Integer, OutputFormat>();
		
		// Load formats from database
		ArrayList<OutputFormat> formatList = Database.getFormats();
		
		for (OutputFormat format : formatList)
		{
		   formats.put(format.getId(), format);
		}
		
		System.out.println("Loaded " + formatList.size() + " known output formats from database.");
		
	}
	
	/**
	 * Adds new output format to known formats
	 * @param name				   Visible format name 
	 * @param fileAppendix		Appendix added to input filename to create output filename
	 * @param videoFormat		Encoded video format
	 * @param videoX			   Video resolution X
	 * @param videoY			   Video resolution Y
	 * @param videoBitrate		Encoded video bitrate
	 * @param audioFormat		Encoded audio format
	 * @param audioChannels		Number of output audio channels
	 * @param audioSamplerate	Encoded audio samplerate
	 * @param audioBitrate		Encoded audio bitrate
	 * @return					   ID of the new format
	 */
	public int addFormat(String name,
						 String fileAppendix,
						 String videoFormat,
						 int videoX,
						 int videoY,
						 int videoBitrate,
						 String audioFormat,
						 int audioChannels,
						 int audioSamplerate,
						 int audioBitrate)
	{
		// Format ID is set by the database query
		int id = -1;
		
		OutputFormat newFormat = new OutputFormat(id, 
												            name, 
												            fileAppendix, 
												            videoFormat, 
												            videoX, 
												            videoY, 
												            videoBitrate, 
												            audioFormat, 
												            audioChannels, 
												            audioSamplerate, 
												            audioBitrate);
		

	    // Attempt to insert format into database 
      try
      {
         Database.addFormat(newFormat);
      } 
      catch (SQLException e)
      {
         e.printStackTrace();
         
         return -1;
      }
		
		this.formats.put(id, newFormat);
		
		return id;
	}
	
	public void deleteFormat(int id) throws Exception
	{
		// Check if format exists
		if (formats.containsKey(id))
		{
			// Remove it from map
			formats.put(id, null);
			
			Database.removeFormat(id);
		}
		else
		{
			throw new Exception("Output format with chosen ID does not exist.");
		}
	}
	
	public OutputFormat getFormatWithId(int id)
	{
	   return formats.get(id);
	}
}
