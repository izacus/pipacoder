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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.kiberpipa.coder.enums.JobStates;
import org.kiberpipa.coder.formats.FormatManager;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.jobs.Job;

/**
 * @author Jernej
 *
 */
public class Database 
{
   private static File databaseFile = null;
   
	static
	{
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
	   
      // Set database file
      databaseFile = new File(Configuration.getValue("dbdir")+ File.separator + "data.db");
      
		// Check if database file exists and create new database if that is not the case
		if (!databaseFile.exists())
		{
			createDatabase();
		}
		
		System.out.println("Database initialized.");
	}

	
	private static Connection connectSQLite() throws SQLException
	{
	   return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
	}
	
	/**
	 * Creates SQLite database to store data 
	 */
	private static void createDatabase() 
	{
		System.out.println("Database file not found, creating new one...");
		
		// Create database file and check if directory is writable
		
		try
		{
			databaseFile.createNewFile();
		}
		catch (IOException e)
		{
			System.err.println("Could not create new database file, make sure the dbdir is writable by this process!");
			System.exit(-2);
		}
		
		Connection connection = null;
		
		// Create new SQLite database
		try 
		{
			// Connect to database
		   connection = connectSQLite();
			
			Statement statement = connection.createStatement();
			// Create table for formats
			statement.executeUpdate("CREATE TABLE formats (" +
									      "id  			   INTEGER PRIMARY KEY, " +		// Format ID
									      "name	 		   TEXT NOT NULL, " +				
									      "appendix		TEXT NOT NULL UNIQUE, " +
									      "videoFormat 	TEXT, " +
									      "videoX			INTEGER, " +
									      "videoY 		   INTEGER, " +
									      "videoBitrate	INTEGER, " +
									      "audioFormat	TEXT, " +
									      "audioChannels	INTEGER, " +
									      "audioSamplerate INTEGER, " +
									      "audioBitrate	INTEGER);");
			
			// Create table for jobs
			statement.executeUpdate("CREATE TABLE jobs (" +
									      "id				     INTEGER PRIMARY KEY, " +
									      "inputFilename	     TEXT NOT NULL, " +
									      "outputFilename	  TEXT NOT NULL, " +
									      "fmt_id			     INTEGER NOT NULL, " +
									      "state			     TEXT NOT NULL, " +
									      "message            TEXT DEFAULT NULL);");
			
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
			System.err.println("Error while creating database, check that the destination directory is writable!");
			System.exit(-4);
		}
		finally
		{
		   if (connection != null)
		   {
		      try
            {
               connection.close();
            } 
		      catch (SQLException e)
            {}
		   }
		}
	}
	
	/*
	 * FORMAT MANAGEMENT
	 */
	
	/**
	 * Retrieves list of known output formats from SQLite database
	 * @return List of known formats, empty list if there aren't any or error occured
	 */
	public static ArrayList<OutputFormat> getFormats()
	{
	   ArrayList<OutputFormat> formats = new ArrayList<OutputFormat>();
	   
	   Connection dbConn = null;
	   
	   try
	   {
	      dbConn = connectSQLite();
	      
	      Statement statement = dbConn.createStatement();
	      ResultSet results = statement.executeQuery("SELECT * FROM formats");
	      
	      while(results.next())
	      {
	         OutputFormat format = new OutputFormat(results.getInt("id"), 
	                                                results.getString("name"),
	                                                results.getString("appendix"),
	                                                results.getString("videoFormat"),
	                                                results.getInt("videoX"),
	                                                results.getInt("videoY"),
	                                                results.getInt("videoBitrate"),
	                                                results.getString("audioFormat"),
	                                                results.getInt("audioChannels"),
	                                                results.getInt("audioSamplerate"),
	                                                results.getInt("audioBitrate"));
	         
	         formats.add(format);
	      }
	     
	   }
	   catch (SQLException e)
	   {
	      System.err.println("Error while retrieving saved formats: " + e.getMessage());
	   }
	   finally
	   {
	      if (dbConn != null)
	      {
	         try
            {
               dbConn.close();
            }
	         catch (SQLException e)
            {}
	      }
	   }
	   
	   return formats;
	}
	
	/**
	 * Adds an output format to database. Also sets "ID" field on the passed format
	 * @param format output format to add
	 * @throws SQLException if addition to database fails.
	 */
	public static synchronized void addFormat(OutputFormat format) throws SQLException
	{
	   Connection dbConn = null;
	   
	   try
	   {
	      dbConn = connectSQLite();
	      
	      dbConn.setAutoCommit(false);
	      
	      PreparedStatement statement = dbConn.prepareStatement("INSERT INTO formats" +
	      		                                                "(name, appendix, videoFormat, videoX, videoY, videoBitrate, audioFormat, audioChannels, audioSamplerate, audioBitrate)" +
	      		                                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	      
	      statement.setString(1, format.getName());
	      statement.setString(2, format.getFileAppendix());
	      // Video
	      statement.setString(3, format.getVideoFormat());
	      statement.setInt(4, format.getVideoResX());
	      statement.setInt(5, format.getVideoResY());
	      statement.setInt(6, format.getVideoBitrate());
	      // Audio
	      statement.setString(7, format.getAudioFormat());
	      statement.setInt(8, format.getAudioChannels());
	      statement.setInt(9, format.getAudioSamplerate());
	      statement.setInt(10, format.getAudioBitrate());
	      
	      statement.executeUpdate();

	      dbConn.commit();
	      
	      // Get retrieved key
         statement = dbConn.prepareStatement("SELECT last_insert_rowid();");
	      
	      ResultSet rowId = statement.executeQuery();
	      rowId.next();
	      
	      int id = rowId.getInt(1);
	      
	      
	      format.setId(id);
	      
	      System.out.println("Succesfully saved format " + format.getName() + " with id " + format.getId());
	   }
	   catch(SQLException e)
	   {
	      throw e;
	   }
	   finally
	   {
	      if (dbConn != null)
            try
            {
               dbConn.close();
            } 
	         catch (SQLException e)
            {}
	   }
	}
	
	public static void removeFormat(int formatId)
	{
	     Connection dbConn = null;
	      
	      try
	      {
	         dbConn = connectSQLite();
	         
	         PreparedStatement statement = dbConn.prepareStatement("DELETE FROM formats WHERE id = ?");
	         statement.setInt(1, formatId);
	         
	         statement.executeUpdate();
	         
	      }
	      catch (SQLException e)
	      {
	         System.err.println("Failed to delete format id " + formatId);
	      }
	      finally
	      {
	         if (dbConn != null)
	            try
	            {
	               dbConn.close();
	            } 
	            catch (SQLException e)
	            {}
	      }
	}
	
	/*
	 * JOB MANAGEMENT
	 */
	
	
	/**
	 * Retrieves last 50 jobs from database
	 * @return list of jobs, empty list if there are none or error occurs
	 */
	public static ArrayList<Job> getJobs()
	{
	      ArrayList<Job> jobs = new ArrayList<Job>();
	      
	      FormatManager formatManager = FormatManager.getInstance();
	      
	      Connection dbConn = null;
	      
	      try
	      {
	         dbConn = connectSQLite();
	         
	         Statement statement = dbConn.createStatement();
	         
	         ResultSet jobSet = statement.executeQuery("SELECT * FROM jobs LIMIT 50");
	         
	         while (jobSet.next())
	         {
	            Job job = new Job(jobSet.getInt("id"),
	                              jobSet.getString("inputFilename"),
	                              formatManager.getFormatWithId(jobSet.getInt("fmt_id")));
	            
	            // Set correct job state
	            JobStates state = JobStates.valueOf(jobSet.getString("state"));
	            job.setState(state, false);
	            
	            jobs.add(job);
	         }
	      }
	      catch (SQLException e)
	      {
	         System.err.println("Error while retrieving stored jobs: " + e.getMessage());
	      }
	      catch (Exception e)
	      {
	         System.err.println(e.getMessage());
	      }
	      finally
	      {
	         if (dbConn != null)
	         {
	            try
	            {
	               dbConn.close();
	            }
	            catch (SQLException e)
	            {};
	         }
	      }

	      System.out.println("Loaded " + jobs.size() + " jobs from database.");
	      
	      return jobs;
	}
	
	public static void updateJobState(Job job)
	{
      Connection dbConn = null;
      
      try
      {
         dbConn = connectSQLite();
         
         PreparedStatement statement = dbConn.prepareStatement("UPDATE jobs SET state = ?, message = ? WHERE id = ?");
         statement.setString(1, job.getState().toString());
         statement.setString(2, job.getFailMessage());
         statement.setInt(3, job.getId());
         
         statement.executeUpdate();
      }
      catch (SQLException e)
      {
         System.err.println("Failed to update database job state! Database is inconsistent with process status - " + e.getMessage());
      }
      finally
      {
         if (dbConn != null)
         {
            try
            {
               dbConn.close();
            }
            catch (SQLException e)
            {};
         }
      }
	}
	
	public static synchronized void addJob(Job job) throws SQLException
	{
	     Connection dbConn = null;
	      
	      try
	      {
	         dbConn = connectSQLite();
	         dbConn.setAutoCommit(false);
	         
	         PreparedStatement statement = dbConn.prepareStatement("INSERT INTO jobs (inputFilename, outputFilename, fmt_id, state)" +
	         		                                                "VALUES (?, ?, ?, ?);");
	         
	         statement.setString(1, job.getInputFileName());
	         statement.setString(2, job.getOutputFileName());
	         statement.setInt(3, job.getOutputFormat().getId());
	         statement.setString(4, job.getState().toString());
	         
	         statement.executeUpdate();
	         
	         dbConn.commit();
	         
	         statement = dbConn.prepareStatement("SELECT last_insert_rowid();");
	         
	         ResultSet rowId = statement.executeQuery();
	         rowId.next();
	         
	         int id = rowId.getInt(1);
	         job.setId(id);
	         
	      }
        catch (SQLException e)
         {
            throw e;
         }
         finally
         {
            if (dbConn != null)
            {
               try
               {
                  dbConn.close();
               }
               catch (SQLException e)
               {};
            }
         }

	}
}
