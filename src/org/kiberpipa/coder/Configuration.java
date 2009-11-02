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

package org.kiberpipa.coder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Configuration 
{
	private static HashMap<String, String> configurationOptions = null;
	
	static
	{
		configurationOptions = new HashMap<String, String>();
		
		// Parse configuration file on first invocation
		parseConfig();
	}
	
	private static void parseConfig()
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader("pipacoder.conf"));
			
			String line = reader.readLine();
			
			while(line != null)
			{
				
				StringTokenizer tokenizer = new StringTokenizer(line.trim(), "=");
				
				if (!line.startsWith("#") && tokenizer.countTokens() > 1)
				{
					String identifier = tokenizer.nextToken().toLowerCase();
					String value = tokenizer.nextToken();
					
					configurationOptions.put(identifier, value);
				}
				
				line = reader.readLine();
			}
		} 
		catch (IOException e) 
		{
			System.err.println("Error reading pipacoder.conf.");
			System.exit(-1);
		}
	}
	
	public static String getValue(String identifier)
	{
		
		return configurationOptions.get(identifier);
	}
}
