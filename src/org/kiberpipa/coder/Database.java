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
import java.util.HashMap;

import org.kiberpipa.coder.formats.FormatManager;
import org.kiberpipa.coder.formats.FormatPreset;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.jobs.Job;
import org.kiberpipa.coder.jobs.JobStates;

/**
 * @author Jernej
 *
 */
public class Database 
{
   private static File databaseFile = null;
   
   private static Object insertMonitor = null;  // usage of last_row_id can cause a race condition when inserting several things at once
                                                // therefore mutex is enforced
	static
	{
	   // Load SQLite JDBC driver
      try 
      {
         Class.forName("org.sqlite.JDBC");
      } 
      catch (ClassNotFoundException e) 
      {
         Log.error("Missing sqlite-jdbc library, make sure it is present!");
         System.exit(-3);
      }
	   
      // Set database file
      databaseFile = new File(Configuration.getValue("dbdir")+ File.separator + "data.db");
      
		// Check if database file exists and create new database if that is not the case
		if (!databaseFile.exists())
		{
			createDatabase();
		}
		
		insertMonitor = new Object();
		
		Log.info("Database initialized.");
	}

	
	private static Connection connectSQLite() throws SQLException
	{
	   return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
	}
	
	/**
	 * Creates SQLite database file and structure to store data
	 */
	private static void createDatabase() 
	{
		Log.info("Database file not found, creating new one...");
		
		// Create database file and check if directory is writable
		
		try
		{
			databaseFile.createNewFile();
		}
		catch (IOException e)
		{
			Log.error("Could not create new database file, make sure the dbdir is writable by this process!");
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
									      "name	 		   TEXT NOT NULL, " +	         // Visible name			
									      "appendix		TEXT NOT NULL, " +            // File name appendix
									      "twopass       BOOLEAN NOT NULL," +          // Requires two-pass encoding
									      "videoFormat 	TEXT, " +                     // Output video format
									      "videoX			INTEGER, " +                  // Output video resolution
									      "videoY 		   INTEGER, " +
									      "videoBitrate	INTEGER, " +                  // Target output video bitrate
									      "audioFormat	TEXT, " +                     // Output audio format
									      "audioChannels	INTEGER, " +                  // Number of output audio channels
									      "audioSamplerate INTEGER, " +                // Samplerate
									      "audioBitrate	INTEGER," +
									      "ffmpegParams TEXT);");                  // Bitrate
			
			// Create table for jobs
			statement.executeUpdate("CREATE TABLE jobs (" +
									      "id				     INTEGER PRIMARY KEY, " +
									      "inputFilename	     TEXT NOT NULL, " +
									      "outputFilename	  TEXT NOT NULL, " +
									      "fmt_id			     INTEGER NOT NULL, " +    // Target output format ID
									      "state			     TEXT NOT NULL, " +       // Current job state
									      "message            TEXT DEFAULT NULL);");   // Job state message (eg. failure cause)
			
			statement.executeUpdate("CREATE TABLE presets (" +
			                        "id               INTEGER PRIMARY KEY," +
			                        "name             TEXT NOT NULL);");
			
			statement.executeUpdate("CREATE TABLE preset_format (" +
					                  "id        INTEGER PRIMARY KEY, " +
					                  "prst_id   INTEGER NOT NULL, " +
					                  "fmt_id    INTEGER NOT NULL);");
			
         // Current version of SQLite JDBC driver doesn't support SQLite foreign keys, so instead
         // a trigger is used to enforce cascaded deletion
         statement.executeUpdate("CREATE TRIGGER fkd_fmt_id " +
                                 "BEFORE DELETE ON formats " +
                                 "FOR EACH ROW BEGIN " +
                                 "DELETE FROM jobs WHERE fmt_id = OLD.id;" +
                                 "DELETE FROM preset_format WHERE fmt_id = OLD.id;" +
                                 "END;");
			
			// Create foreign key trigger for deleted presets
			statement.executeUpdate("CREATE TRIGGER fkd_prst_id " +
			                        "BEFORE DELETE ON presets " +
			                        "FOR EACH ROW BEGIN " +
			                        "DELETE FROM preset_format WHERE prst_id = OLD.id; " +
			                        "END;" );
			
			connection.close();
		} 
		catch (SQLException e) 
		{
		   Log.error(e.getMessage());
			Log.error("Error while creating database, check that the destination directory is writable!");
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
	 * Retrieves list of known output formats from database
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
	                                                results.getBoolean("twopass"),
	                                                results.getString("videoFormat"),
	                                                results.getInt("videoX"),
	                                                results.getInt("videoY"),
	                                                results.getInt("videoBitrate"),
	                                                results.getString("audioFormat"),
	                                                results.getInt("audioChannels"),
	                                                results.getInt("audioSamplerate"),
	                                                results.getInt("audioBitrate"),
	                                                results.getString("ffmpegParams"));
	         
	         formats.add(format);
	      }
	     
	   }
	   catch (SQLException e)
	   {
	      Log.error("Error while retrieving saved formats: " + e.getMessage());
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
	public static void addFormat(OutputFormat format) throws SQLException
	{
	   Connection dbConn = null;
	   
	   synchronized (insertMonitor)
      {
	        try
	         {
	            dbConn = connectSQLite();
	            dbConn.setAutoCommit(false);
	            
	            PreparedStatement statement = dbConn.prepareStatement("INSERT INTO formats" +
	                                                                  "(name, appendix, twopass, videoFormat, videoX, videoY, videoBitrate, audioFormat, audioChannels, audioSamplerate, audioBitrate, ffmpegParams)" +
	                                                                  " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	            
	            statement.setString(1, format.getName());
	            statement.setString(2, format.getFileAppendix());
	            statement.setBoolean(3, format.isTwopass());
	            // Video
	            statement.setString(4, format.getVideoFormat());
	            statement.setInt(5, format.getVideoResX());
	            statement.setInt(6, format.getVideoResY());
	            statement.setInt(7, format.getVideoBitrate());
	            // Audio
	            statement.setString(8, format.getAudioFormat());
	            statement.setInt(9, format.getAudioChannels());
	            statement.setInt(10, format.getAudioSamplerate());
	            statement.setInt(11, format.getAudioBitrate());
	            
	            // FFMPEG parameters
	            statement.setString(12, format.getFfmpegParams());
	            
	            statement.executeUpdate();
	   
	            dbConn.commit();
	            
	            // Get retrieved key
	            statement = dbConn.prepareStatement("SELECT last_insert_rowid();");
	            ResultSet rowId = statement.executeQuery();
	            rowId.next();
	            int id = rowId.getInt(1);
	            
	            format.setId(id);
	            
	            Log.info("Succesfully saved format " + format.getName() + " with id " + format.getId());
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
	}
	

	/**
	 * Updates format information into database
	 * @throws SQLException
	 */
   public static void updateFormat(OutputFormat format) throws SQLException
   {
      Connection dbConn = null;
      
      try
      {
         dbConn = connectSQLite();
         
         PreparedStatement statement = dbConn.prepareStatement("UPDATE formats SET name = ?, appendix = ?, twopass = ?, videoFormat = ?, videoX = ?, " +
         		                                                "videoY = ?, videoBitrate = ?, audioFormat = ?, audioChannels = ?, audioSamplerate = ?, audioBitrate = ?, ffmpegParams = ? WHERE id = ?");
         
         statement.setString(1, format.getName());
         statement.setString(2, format.getFileAppendix());
         statement.setBoolean(3, format.isTwopass());
         // Video
         statement.setString(4, format.getVideoFormat());
         statement.setInt(5, format.getVideoResX());
         statement.setInt(6, format.getVideoResY());
         statement.setInt(7, format.getVideoBitrate());
         // Audio
         statement.setString(8, format.getAudioFormat());
         statement.setInt(9, format.getAudioChannels());
         statement.setInt(10, format.getAudioSamplerate());
         statement.setInt(11, format.getAudioBitrate());
         
         // FFmpeg parameters
         statement.setString(12, format.getFfmpegParams());
         
         statement.setInt(13, format.getId());
         
         statement.executeUpdate();
      }
      catch (SQLException e)
      {
         Log.error("Failed to update format " + e.getMessage());
         
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
	
	/**
	 * Removes format from database
	 * @param formatId
	 */
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
	         Log.error("Failed to delete format id " + formatId);
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
	 * PRESET MANAGEMENT
	 */
	public static ArrayList<FormatPreset> getPresets(FormatManager formatManager)
	{
		HashMap<Integer, FormatPreset> presets = new HashMap<Integer, FormatPreset>();
		
		Connection dbConn = null;
		
		try
		{
		      dbConn = connectSQLite();
		      
		      // Find list of all available presets
		      Statement statement = dbConn.createStatement();
		      ResultSet results = statement.executeQuery("SELECT * FROM presets");
		      
		      while(results.next())
		      {
		    	  FormatPreset preset = new FormatPreset(results.getInt("id"),
		    			  								 results.getString("name"));
		    	  
		    	  presets.put(preset.getId(), preset);
		      }
		      
		      // Resolve preset to format mappings
		      results = statement.executeQuery("SELECT * FROM preset_format");
		      
		      while(results.next())
		      {
		    	  int presetId = results.getInt("prst_id");
		    	  int formatId = results.getInt("fmt_id");
		    	  presets.get(presetId).addFormat(formatManager.getFormatWithId(formatId));
		      }
		}
		catch (SQLException e)
		{
			Log.error("Failed to load preset list: " + e.getMessage());
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
		
		return new ArrayList<FormatPreset>(presets.values());
	}
	
	public static void putPreset(FormatPreset preset)
	{
		deletePreset(preset);
		
		Connection dbConn = null;
		
		try
		{
			dbConn = connectSQLite();
			dbConn.setAutoCommit(false);
			
			// Create preset
			PreparedStatement stmt = dbConn.prepareStatement("INSERT INTO presets (name) VALUES (?)");
			stmt.setString(1, preset.getName());
			stmt.executeUpdate();
			
			// Create foreign key mappings
			for (OutputFormat format : preset.getFormats())
			{
				stmt = dbConn.prepareStatement("INSERT INTO preset_format (prst_id, fmt_id) VALUES (?, ?)");
				stmt.setInt(1, preset.getId());
				stmt.setInt(2, format.getId());
				stmt.executeUpdate();
			}
			
			dbConn.commit();
			
			if (preset.getId() == null)
			{
	            PreparedStatement statement = dbConn.prepareStatement("SELECT last_insert_rowid();");
	            ResultSet rowId = statement.executeQuery();
	            rowId.next();
	            int id = rowId.getInt(1);
	            preset.setId(id);
			}
		}
		catch (SQLException e)
		{
			Log.error("Failed to put preset to database: " + e.getMessage());
		}
		finally
		{
			if (dbConn != null)
			{
				try
				{
					dbConn.close();
				} catch (SQLException e)
				{}
			}
		}
	}
	
	public static void deletePreset(FormatPreset preset)
	{
		if (preset.getId() == null)
			return;
		
		Connection dbConn = null;
		
		try
		{
	         dbConn = connectSQLite();
	         
	         PreparedStatement statement = dbConn.prepareStatement("DELETE FROM presets WHERE id = ?");
	         statement.setInt(1, preset.getId());
	         statement.executeUpdate();
		}
		catch (SQLException e)
		{
			Log.warn("Deletion of existing preset failed, possible update...");
		}
		finally
		{
			if (dbConn != null)
			{
				try
				{
					dbConn.close();
				} catch (SQLException e)
				{}
			}
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
	            job.setFailMessage(jobSet.getString("message"));
	            
	            jobs.add(job);
	         }
	      }
	      catch (SQLException e)
	      {
	         Log.error("Error while retrieving stored jobs: " + e.getMessage());
	      }
	      catch (Exception e)
	      {
	         Log.error(e.getMessage());
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

	      Log.info("Loaded " + jobs.size() + " jobs from database.");
	      
	      return jobs;
	}
	
	/**
	 * Updates <b>only</b> state of job in the database
	 * @param job 
	 */
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
         
         statement.close();
      }
      catch (SQLException e)
      {
         Log.error("Failed to update database job state! Database is inconsistent with process status - " + e.getMessage());
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
	
	/**
	 * Adds new job to the database
	 * @param job
	 * @throws SQLException if insertion fails
	 */
	public static void addJob(Job job) throws SQLException
	{
	   synchronized (insertMonitor)
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
}
