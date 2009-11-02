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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database 
{
	static
	{
		// Check if database file exists and create new database if that is not the case
		if (!new File(Configuration.getValue("dbdir") + File.pathSeparator + "data.db").exists())
		{
			createDatabase();
		}
	}

	private static void createDatabase() 
	{
		System.out.println("Database file not found, creating new one...");
		
		// Create database file and check if directory is writable
		File databaseFile = null;
		
		try
		{
			File configurationDirectory = new File(Configuration.getValue("dbdir"));
			databaseFile = new File(configurationDirectory.getPath() + File.pathSeparator + "data.db");
			
			databaseFile.createNewFile();
		}
		catch (IOException e)
		{
			System.err.println("Could not create new database file, make sure the dbdir is writable by this process!");
			System.exit(-2);
		}
		
		// Create new SQLite database
		
		// Load SQLite JDBC driver
		try 
		{
			Class.forName("org.sqlite.JDBC");
		} 
		catch (ClassNotFoundException e) 
		{
			System.err.println("Missing sqlite-jdbc library, make sure it's present!");
			System.exit(-3);
		}
		
		try 
		{
			// Connect to database
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
			
			Statement statement = connection.createStatement();
			
			// Create table for formats
			statement.executeUpdate("CREATE TABLE formats (" +
									"id  			INTEGER PRIMARY KEY, " +		// Format ID
									"name	 		TEXT NOT NULL, " +				
									"appendix		TEXT NOT NULL UNIQUE, " +
									"videoFormat 	TEXT, " +
									"videoX			INTEGER, " +
									"videoY 		INTEGER, " +
									"videoBitrate	INTEGER, " +
									"audioFormat	TEXT, " +
									"audioChannels	INTEGER, " +
									"audioSamplerate INTEGER, " +
									"audioBitrate	INTEGER);");
			
			// Create table for jobs
			statement.executeUpdate("CREATE TABLE jobs (" +
									"id				INTEGER PRIMARY KEY, " +
									"inputFilename	TEXT NOT NULL, " +
									"outputFilename	TEXT NOT NULL, " +
									"fmt_id			INTEGER NOT NULL, " +
									"state			TEXT NOT NULL);");
			
			// Current version of SQLite JDBC driver doesn't support SQLite foreign keys, so instead
			// a trigger is used to enforce cascaded deletion
			statement.executeUpdate("CREATE TRIGGER fkd_fmt_id " +
									"BEFORE DELETE ON formats " +
									"FOR EACH ROW BEGIN " +
									"DELETE FROM jobs WHERE fmt_id = OLD.id; " +
									"END;");
			
			connection.close();
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void poke()
	{
		
	}
}
