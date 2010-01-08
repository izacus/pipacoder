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

package org.kiberpipa.coder.userinterfaces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.StringTokenizer;

import org.kiberpipa.coder.Configuration;
import org.kiberpipa.coder.Log;
import org.kiberpipa.coder.formats.FormatManager;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.jobs.Job;
import org.kiberpipa.coder.jobs.JobManager;
import org.kiberpipa.coder.jobs.JobStates;

public class WebInterface extends NanoHTTPD implements UserInterface
{ 
   /**
    * Starts a web interface HTTP daemon on selected port
    * @param port port to bind to
    * @throws IOException if binding fails.
    */
   public WebInterface(int port) throws IOException
   {
      super(port);

      Log.info("Web interface up and running on port " + port);
   }

   @Override
   public String getId()
   {
      return "webinterface";
   }

   @Override
   /**
    * Serves response for a HTTP request on server
    */
   public Response serve(String uri, String method, Properties header,
                         Properties parms)
   {
      // Get possible API call command
      String command = getAPICommandFromURI(uri);
      
      // Check if API call or file request command was issued
      if (command != null)
      {
         String responseString = "";
         
         // Available input file listing
         if (command.equals("inputfiles"))
         {
            responseString = getInputFilesJSON();
         }
         // Available encoding format listing
         else if (command.equals("formats"))
         {
            responseString = getFormatsJSON();
         }
         // Jobs in system listing
         else if (command.equals("jobs"))
         {
            responseString = getAllJobsJSON();
         }
         // Add new format
         else if (command.equals("addformat"))
         {
            responseString = addNewFormat(parms);
         }
         else if (command.equals("updateformat"))
         {
            responseString = updateFormat(parms);
         }
         else if (command.equals("removeformat"))
         {
            responseString = removeFormat(parms);
         }
         else if (command.equals("getformatinfo"))
         {
            responseString = getFormatInfoJSON(parms);
         }
         else if (command.equals("getfailreason"))
         {
            responseString = getJobFailReason(parms);
         }
         else if (command.equals("addjobs"))
         {
            responseString = addJobs(parms);
         }
         else if (command.equals("stopjob"))
         {
            responseString = stopJob(parms);
         }
         // Responses are JSON, so return text/javascript MIME type
         return new Response(HTTP_OK, "text/javascript", responseString);
      }
      
      // Attempt to serve file if there is no URL match
      return serveFile(uri, header, new File("webinterface"), false);
   }
   
   /**
    * Adds set of new jobs to Job manager passed as a filename and list of ids
    * @param parms parameters, which must include <i>filename</i> with filename string and <i>formats</i> with comma delimited format ID list
    * @return JSON response
    */
   private String addJobs(Properties parms)
   {
      String filename = parms.getProperty("filename");

      // Get format IDs from comma delimited string
      String formatStrings = parms.getProperty("formats");
      
      if (formatStrings == null)
      {
         return getErrorResponse("No output formats were specified!");
      }
      
      StringTokenizer tokenizer = new StringTokenizer(formatStrings, ",");
      
      // Put formats in array list first so full rollback can be done in case of an error
      ArrayList<OutputFormat> formats = new ArrayList<OutputFormat>();
      
      while (tokenizer.hasMoreTokens())
      {
         try
         {
            int id = Integer.parseInt(tokenizer.nextToken());
            OutputFormat format = FormatManager.getInstance().getFormatWithId(id);
            
            if (format == null)
            {
               return getErrorResponse("Format with ID " + id + " does not exist.");
            }
            else
            {
               formats.add(format);
            }
         }
         catch (NumberFormatException ex)
         {
            return getErrorResponse("Invalid format ID string passed.");
         }
      }
      
      // Possible to add multiple files in one go
      ArrayList<String> files = new ArrayList<String>();
      
      if (filename.equals("ALLFILES"))
      {
         File inputFileDirectory = new File(Configuration.getValue("inputdir"));
         for (String fileName : inputFileDirectory.list())
         {
            files.add(fileName);
         }
      }
      else
      {
         files.add(filename);
      }
      
      for (String file : files)
      {
         // Invoke job manager to add jobs to queue
         for (OutputFormat format : formats)
         {
            JobManager.getInstance().addJob(file, format);
         }
      }
      
      return "{ status : 'OK', message : 'Jobs added successfully.' } ";
   }
   
   private String stopJob(Properties parms)
   {
      int id = -1;
      
      // Check if ID parameter exists
      if (parms.get("id") == null)
      {
         return getErrorResponse("No id sent.");
      }
      
      // Check if number was sent
      try
      {
         id = Integer.parseInt(parms.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         return getErrorResponse("Invalid ID.");
      }
      
      Job job = JobManager.getInstance().getJobWithId(id);
      
      job.stop();
      
      return "{}";
   }

   /**
    * Strips command name from /api/*command* type URI
    * @param uri URI to strip from
    * @return stripped command requested or <b>null<b> if the URI is invalid for API command
    */
   private String getAPICommandFromURI(String uri)
   {
      if (uri.startsWith("/api/"))
      {
         String urlCommand = null;
         
         if (uri.endsWith("/"))
         {
            urlCommand = uri.substring(5, uri.length() - 2);
         }
         else
         {
            urlCommand = uri.substring(5);
         }
         
         return urlCommand;
      }
      
      return null;
   }
   
   /**
    * Builds input file listing JSON array
    * @return
    */
   private String getInputFilesJSON()
   {
      StringBuilder output = new StringBuilder();
      
      output.append("[ ");
      
      File inputFileDirectory = new File(Configuration.getValue("inputdir"));
      for (String fileName : inputFileDirectory.list())
      {
         output.append("'" + fileName + "',");
      }
      
      // Replace final comma with closing bracket
      output.setCharAt(output.length() - 1, ']');
      
      return output.toString();
   }
   
   /**
    * Builds available output formats listing JSON array
    * @return
    */
   private String getFormatsJSON()
   {
      StringBuilder output = new StringBuilder();
      
      output.append("[ ");
      
      for (OutputFormat format : FormatManager.getInstance().getFormats())
      {
         output.append("{id:" + format.getId() +",");
         output.append("name:'" + format.getName() +"'},");
      }
      
      output.setCharAt(output.length() - 1, ']');
      
      return output.toString();
   }
   
   /**
    * Builds are currently available jobs listing JSON array
    * @return
    */
   private String getAllJobsJSON()
   {
      StringBuilder output = new StringBuilder();
      output.append("[ ");
      
      ArrayList<Job> jobs = JobManager.getInstance().getJobs();
      Collections.reverse(jobs);
      
      // { id: xx, filename: 'xx', status:'xx', progress: 'xx', eta: 'xx' }
      for (int i = 0; i < Math.min(jobs.size(), 16); i++)
      {
         Job job = jobs.get(i);
         
         output.append("{id:" + job.getId() + ",");
         output.append("filename:'" + job.getInputFileName() + "', ");
         output.append("format: '" + job.getOutputFormat().getName() + "',");
         output.append("status: '" + job.getState().toString() +"'");
         
         if (job.getState() == JobStates.RUNNING)
         {
            output.append(",progress : '" + String.format("%.2f", job.getProgress()) + "%'");
            output.append(",eta : 'TBD' },");
         }
         else
         {
            output.append("},");
         }
      }
      
      output.setCharAt(output.length() - 1, ']');
      return output.toString();
   }
   
   private String getJobFailReason(Properties params)
   {
      int id = -1;
      
      // Check if ID parameter exists
      if (params.get("id") == null)
      {
         return getErrorResponse("No id sent.");
      }
      
      // Check if number was sent
      try
      {
         id = Integer.parseInt(params.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         return getErrorResponse("Invalid ID.");
      }
      
      Job job = JobManager.getInstance().getJobWithId(id);
      
      if (job == null)
      {
         return getErrorResponse("Job doesn't exist anymore.");
      }
      
      String message = job.getFailMessage() == null ? "No error message stored." : job.getFailMessage().replaceAll("\\n", "<br>").replaceAll("\\r", "").replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", "\\\\'");
      
      return "{ message: '" + message + "'}";
   }
   
   /**
    * Adds new known format to database and builds response
    * @param params  GET parameters of new format
    * @return response string
    */
   private String addNewFormat(Properties params)
   {
      Log.info("[API] Adding new format...");
      
      int id = -1;
      String formatName = null;
      String vFormat = null;
      int vBitrate = 0;
      String vResolution = null;
      String aFormat = null;
      int aBitrate = 0;
      int aChannels = 0;
      int aSampleRate = 0;
      
      String ffmpegParams = null;
      
      String suffix = null;
      
      // Validation
      try
      {
         if (!params.containsKey("id") ||
             !params.containsKey("formatname") ||
             !params.containsKey("vformat") ||
             !params.containsKey("vbitrate") ||
             !params.containsKey("vresolution") ||
             !params.containsKey("aformat") ||
             !params.containsKey("abitrate") ||
             !params.containsKey("achannels") || 
             !params.containsKey("asamplerate") ||
             !params.containsKey("suffix") ||
             !params.containsKey("ffmpegparams"))
         {
            Log.error("[API] Request for new format denied because of missing parameter.");
            return getErrorResponse("Missing parameter.");
         }
         
         id = Integer.parseInt(params.getProperty("id"));
         formatName = params.getProperty("formatname");
         vFormat = params.getProperty("vformat");
         vBitrate = Integer.parseInt(params.getProperty("vbitrate"));
         vResolution = params.getProperty("vresolution");
         aFormat = params.getProperty("aformat");
         aBitrate = Integer.parseInt(params.getProperty("abitrate"));
         aChannels = Integer.parseInt(params.getProperty("achannels"));
         aSampleRate = Integer.parseInt(params.getProperty("asamplerate"));
         ffmpegParams = params.getProperty("ffmpegparams");
         suffix = params.getProperty("suffix");
      }
      catch (NumberFormatException e)
      {
         Log.error("[API] Request for new format denied because of invalid parameter.");
         return getErrorResponse("Invalid parameter.");
      }
      
      // Dereference resolution
      int videoX = 0;
      int videoY = 0;
      
      try
      {
         int xPos = vResolution.indexOf('x');
         
         videoX = Integer.parseInt(vResolution.substring(0, xPos));
         videoY = Integer.parseInt(vResolution.substring(xPos + 1));
      }
      catch(Exception e)
      {
         Log.error("[API] Request for new format denied because of invalid parameter.");
         return getErrorResponse("Invalid parameter.");
      }
      
      // Create new format
      id = FormatManager.getInstance().addFormat(formatName, suffix, vFormat, videoX, videoY, vBitrate, aFormat, aChannels, aSampleRate, aBitrate, ffmpegParams);
      
      if (id == -1)
      {
         return getErrorResponse("Failed to add new format.");
      }
      
      return "{ status : 'OK', id : " + id + ", message : 'Format added successfully.' }";
   }
   
   /**
    * Updates an already existing format with new parameters
    * @param params
    * @return
    */
   private String updateFormat(Properties params)
   {
      Log.info("[API] Updating format...");
      
      int id = -1;
      String formatName = null;
      String vFormat = null;
      int vBitrate = 0;
      String vResolution = null;
      String aFormat = null;
      int aBitrate = 0;
      int aChannels = 0;
      int aSampleRate = 0;
      String ffmpegParams = null;
      
      String suffix = null;
      
      // Validation
      try
      {
         if (!params.containsKey("id") ||
             !params.containsKey("formatname") ||
             !params.containsKey("vformat") ||
             !params.containsKey("vbitrate") ||
             !params.containsKey("vresolution") ||
             !params.containsKey("aformat") ||
             !params.containsKey("abitrate") ||
             !params.containsKey("achannels") || 
             !params.containsKey("asamplerate") ||
             !params.containsKey("suffix") ||
             !params.containsKey("ffmpegparams"))
         {
            Log.error("[API] Request for new format denied because of missing parameter.");
            return getErrorResponse("Missing parameter.");
         }
         
         id = Integer.parseInt(params.getProperty("id"));
         formatName = params.getProperty("formatname");
         vFormat = params.getProperty("vformat");
         vBitrate = Integer.parseInt(params.getProperty("vbitrate"));
         vResolution = params.getProperty("vresolution");
         aFormat = params.getProperty("aformat");
         aBitrate = Integer.parseInt(params.getProperty("abitrate"));
         aChannels = Integer.parseInt(params.getProperty("achannels"));
         aSampleRate = Integer.parseInt(params.getProperty("asamplerate"));
         ffmpegParams = params.getProperty("ffmpegparams");
         suffix = params.getProperty("suffix");
      }
      catch (NumberFormatException e)
      {
         Log.error("[API] Request for update format denied because of invalid parameter.");
         return getErrorResponse("Invalid parameter.");
      }
      
      // Dereference resolution
      int videoX = 0;
      int videoY = 0;
      
      try
      {
         int xPos = vResolution.indexOf('x');
         
         videoX = Integer.parseInt(vResolution.substring(0, xPos));
         videoY = Integer.parseInt(vResolution.substring(xPos + 1));
      }
      catch(Exception e)
      {
         Log.error("[API] Request for update format denied because of invalid parameter.");
         return getErrorResponse("Invalid parameter.");
      }
      
      // Create new format
      try
      {
         FormatManager.getInstance().updateFormat(id, formatName, suffix, vFormat, videoX, videoY, vBitrate, aFormat, aChannels, aSampleRate, aBitrate, ffmpegParams);
      }
      catch (Exception e)
      {
         Log.error("[API] Error updating format id " + id + ": " + e.getMessage());
         
         return getErrorResponse(e.getMessage());
      }

      return "{ status : 'OK', id : " + id + ", message : 'Format updated successfully.' }";
   }
   
   private String removeFormat(Properties parms)
   {
      if (!parms.containsKey("id"))
      {
         Log.error("[API] Request for delete format denied because of invalid parameter.");
         return getErrorResponse("Invalid parameter.");
      }
      
      int id = -1;
      
      try
      {
         id = Integer.parseInt(parms.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         Log.error("[API] Request for delete format denied because of invalid parameter: " + parms.getProperty("id"));
         return getErrorResponse("Invalid parameter.");
      }
      
      try
      {
         FormatManager.getInstance().deleteFormat(id);
      }
      catch (Exception e)
      {
         Log.error("[API] Request for delete failed: " + e.getMessage());
         return getErrorResponse(e.getMessage());
      }
      
      return "{ status : 'OK', message : 'Format deleted successfully.' }";
   }
   
   /**
    * Retrieves format information in JSON form
    * @param params parameters including "id" with format id
    * @return json format information array
    */
   private String getFormatInfoJSON(Properties params)
   {
      int id = -1;
      
      // Check if ID parameter exists
      if (params.get("id") == null)
      {
         return getErrorResponse("No id sent.");
      }
      
      // Check if number was sent
      try
      {
         id = Integer.parseInt(params.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         return getErrorResponse("Invalid ID.");
      }
      
      OutputFormat format = FormatManager.getInstance().getFormatWithId(id);
      
      // Check existence of the format
      if (format == null)
      {
         return getErrorResponse("Invalid ID.");
      }
      
      // Build response
      StringBuilder response = new StringBuilder();
      
      response.append("{id: " + format.getId() + ",");
      response.append(" formatname: '" + format.getName() + "',");
      response.append(" vformat:'" + format.getVideoFormat() + "',");
      response.append(" vbitrate: " + format.getVideoBitrate() + ",");
      response.append(" vresolution: '" + format.getVideoResolution() + "',");
      response.append(" aformat:'" + format.getAudioFormat() + "',");
      response.append(" abitrate: " + format.getAudioBitrate() +",");
      response.append(" asamplerate: " + format.getAudioSamplerate() + ",");
      response.append(" achannels: " + format.getAudioChannels() + ",");
      response.append(" ffmpegparams: '" + format.getFfmpegParams() + "',");
      response.append(" suffix: '" + format.getFileAppendix() + "'}");
      
      return response.toString();
   }
      
   private String getErrorResponse(String message)
   {
      return "{ status : 'ERROR', message : '" + message + "' }";
   }
}
