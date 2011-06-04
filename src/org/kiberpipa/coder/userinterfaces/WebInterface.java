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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kiberpipa.coder.Configuration;
import org.kiberpipa.coder.Log;
import org.kiberpipa.coder.formats.FormatManager;
import org.kiberpipa.coder.formats.FormatPreset;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.formats.PresetManager;
import org.kiberpipa.coder.jobs.Job;
import org.kiberpipa.coder.jobs.JobManager;
import org.kiberpipa.coder.jobs.JobStates;
import org.kiberpipa.coder.processor.FFMpegProcessor;

public class WebInterface extends NanoHTTPD implements UserInterface
{ 
   private boolean started = false;
   
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
   
   /**
    * Causes interface to start processing requests
    * Call this after any privilege drop after port binding
    */
   public void start()
   {
      this.started = true;
   }

   @Override
   /**
    * Serves response for a HTTP request on server
    */
   public Response serve(String uri, String method, Properties header,
                         Properties parms)
   {
      // Do not serve responses until the webservice is started
      // This is here to prevent requests from comming through until privileges are dropped
      if (!this.started)
         return new Response(HTTP_FORBIDDEN, "text/html", "Forbidden, transcoder is not ready yet.");
      
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
         else if (command.equals("presets"))
         {
        	 responseString = getPresetsJSON();
         }
         else if (command.equals("supportedformats"))
         {
            responseString = getSupportedFormats();
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
         else if (command.equals("savepreset"))
         {
        	 responseString = savePreset(parms);
         }
         // Responses are JSON, so return text/javascript MIME type
         return new Response(HTTP_OK, "text/javascript", responseString);
      }
      
      // Attempt to serve file if there is no URL match
      return serveFile(uri, header, new File("webinterface"), false);
   }

   private String savePreset(Properties parms) 
   {
	   Integer presetId;
	   
	   if (parms.getProperty("presetId").equalsIgnoreCase("null"))
	   {
		   presetId = null;
	   }
	   else
	   {
		   presetId = Integer.valueOf(parms.getProperty("presetId"));
	   }
	   
	   String presetName = parms.getProperty("presetName");
	   String formatList = parms.getProperty("formats[]");
	   
	   ArrayList<Integer> listOfFormats = new ArrayList<Integer>();
       StringTokenizer tokenizer = new StringTokenizer(formatList, ",");

       while (tokenizer.hasMoreTokens())
       {
    	   int id = Integer.parseInt(tokenizer.nextToken());
    	   
           OutputFormat format = FormatManager.getInstance().getFormatWithId(id);
           
           if (format == null)
           {
              return getStatusResponse("ERROR", "Format with ID " + id + " does not exist.");
           }
           else
           {
              listOfFormats.add(id);
           }
       }
       
       try
       {
    	   PresetManager.getInstance().putPreset(presetId, presetName, listOfFormats);
       }
       catch (Exception e)
       {
    	   return getStatusResponse("ERROR", "Failed to create new preset: " + e.getMessage());
       }
	   
	   return getStatusResponse("OK", "Preset " + presetName + " stored successfully.");
   }

   @SuppressWarnings("unchecked")
   private String getPresetsJSON()
   {
	   List<FormatPreset> presets = PresetManager.getInstance().getPresets();
	   
	   JSONArray array = new JSONArray();
	   
	   for (FormatPreset preset : presets)
	   {
		   JSONObject obj = new JSONObject();
		   obj.put("id", preset.getId());
		   obj.put("name", preset.getName());
		   
		   JSONArray formatArray = new JSONArray();
		   
		   for (OutputFormat format : preset.getFormats())
		   {
			   formatArray.add(format.getId());
		   }
		   
		   obj.put("formatIds", formatArray);
		   
		   array.add(obj);
	   }
	   
	   return array.toString();
   }

   @SuppressWarnings("unchecked")
   private String getSupportedFormats()
   {
      // Get supported formats from FFMPEG processor
      HashMap<String, String> formats = FFMpegProcessor.getSupportedVideoFormats();
   
      
      JSONObject response = new JSONObject();
      JSONArray videoFormats = new JSONArray();
      
      response.put("video", videoFormats);
      
      LinkedList<String> keys = new LinkedList<String>(formats.keySet());
      Collections.sort(keys);
      
      for (String abbrev : keys)
      {
    	 JSONObject format = new JSONObject();
    	 format.put("abbrev", abbrev);
    	 format.put("name", formats.get(abbrev));
    	 videoFormats.add(format);
      }
      
      JSONArray audioFormats = new JSONArray();
      response.put("audio", audioFormats);
      
      formats = FFMpegProcessor.getSupportedAudioFormats();
      keys = new LinkedList<String>(formats.keySet());
      Collections.sort(keys);
      
      for (String abbrev : keys)
      {
     	 JSONObject format = new JSONObject();
    	 format.put("abbrev", abbrev);
    	 format.put("name", formats.get(abbrev));
    	 audioFormats.add(format);
      }
            
      return response.toString();
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
      String formatStrings = parms.getProperty("formats[]");
      
      if (formatStrings == null)
      {
         return getStatusResponse("ERROR", "No output formats were specified!");
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
               return getStatusResponse("ERROR", "Format with ID " + id + " does not exist.");
            }
            else
            {
               formats.add(format);
            }
         }
         catch (NumberFormatException ex)
         {
            return getStatusResponse("ERROR", "Invalid format ID string passed.");
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
      
      return getStatusResponse("OK", "Jobs added successfully.");
   }
   
   private String stopJob(Properties parms)
   {
      int id = -1;
      
      // Check if ID parameter exists
      if (parms.get("id") == null)
      {
         return getStatusResponse("ERROR", "No id sent.");
      }
      
      // Check if number was sent
      try
      {
         id = Integer.parseInt(parms.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         return getStatusResponse("ERROR", "Invalid ID.");
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
   @SuppressWarnings("unchecked")
   private String getInputFilesJSON()
   {
	  JSONArray response = new JSONArray();
      
      File inputFileDirectory = new File(Configuration.getValue("inputdir"));
      
      if (!inputFileDirectory.exists())
      {
    	  return response.toJSONString();
      }
      
      for (String fileName : inputFileDirectory.list())
      {
         response.add(fileName);
      }
      
      return response.toString();
   }
   
   /**
    * Builds available output formats listing JSON array
    * @return
    */
   @SuppressWarnings("unchecked")
   private String getFormatsJSON()
   {
	  JSONArray response = new JSONArray(); 
	  
      for (OutputFormat format : FormatManager.getInstance().getFormats())
      {
    	 JSONObject jsonFormat = new JSONObject();
    	 jsonFormat.put("id", format.getId());
    	 jsonFormat.put("name", format.getName());
         response.add(jsonFormat);
      }
      
      return response.toJSONString();
   }
   
   /**
    * Builds are currently available jobs listing JSON array
    * @return
    */
   @SuppressWarnings("unchecked")
   private String getAllJobsJSON()
   {
	  JSONArray response = new JSONArray(); 
	   
      ArrayList<Job> jobs = JobManager.getInstance().getJobs();
      Collections.reverse(jobs);
      
      // { id: xx, filename: 'xx', status:'xx', progress: 'xx', eta: 'xx' }
      for (int i = 0; i < Math.min(jobs.size(), 16); i++)
      {
         Job job = jobs.get(i);
         
         JSONObject jsonJob = new JSONObject();
         jsonJob.put("id", job.getId());
         jsonJob.put("filename", job.getInputFileName());
         jsonJob.put("format", job.getOutputFormat().getName());
         jsonJob.put("status", job.getState().toString());
         
         if (job.getState() == JobStates.RUNNING)
         {
        	jsonJob.put("progress", String.format("%.2f", job.getProgress()) + "%'");
         }
         
         response.add(jsonJob);
      }
      
      return response.toJSONString();
   }
   
   @SuppressWarnings("unchecked")
   private String getJobFailReason(Properties params)
   {
      int id = -1;
      
      // Check if ID parameter exists
      if (params.get("id") == null)
      {
         return getStatusResponse("ERROR", "No id sent.");
      }
      
      // Check if number was sent
      try
      {
         id = Integer.parseInt(params.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         return getStatusResponse("ERROR", "Invalid ID.");
      }
      
      Job job = JobManager.getInstance().getJobWithId(id);
      
      if (job == null)
      {
         return getStatusResponse("ERROR", "Job doesn't exist anymore.");
      }
      
      String message = job.getFailMessage() == null ? "No error message stored." : job.getFailMessage().replaceAll("\\n", "<br>").replaceAll("\\r", "").replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", "\\\\'");
      
      JSONObject response = new JSONObject();
      response.put("message", message);
      
      return response.toJSONString();
   }
   
   /**
    * Adds new known format to database and builds response
    * @param params  GET parameters of new format
    * @return response string
    */
   @SuppressWarnings("unchecked")
   private String addNewFormat(Properties params)
   {
      Log.info("[API] Adding new format...");
      
      int id = -1;
      String formatName = null;
      String suffix = null;
      boolean twopass = false;
      String vFormat = null;
      int vBitrate = 0;
      String vResolution = null;
      String aFormat = null;
      int aBitrate = 0;
      int aChannels = 0;
      int aSampleRate = 0;
      
      String ffmpegParams = null;
      
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
            return getStatusResponse("ERROR", "Missing parameter.");
         }
         
         id = Integer.parseInt(params.getProperty("id"));
         formatName = params.getProperty("formatname");
         twopass = params.containsKey("twopass") ? Boolean.parseBoolean(params.getProperty("twopass")) : false;
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
         return getStatusResponse("ERROR", "Invalid parameter.");
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
         return getStatusResponse("ERROR", "Invalid parameter.");
      }
      
      // Create new format
      id = FormatManager.getInstance().addFormat(formatName, suffix, twopass, vFormat, videoX, videoY, vBitrate, aFormat, aChannels, aSampleRate, aBitrate, ffmpegParams);
      
      if (id == -1)
      {
         return getStatusResponse("ERROR", "Failed to add new format.");
      }
      
      JSONObject response = new JSONObject();
      response.put("status", "OK");
      response.put("id", id);
      response.put("message", "Format added successfully.");
      
      return response.toJSONString();
   }
   
   /**
    * Updates an already existing format with new parameters
    * @param params
    * @return
    */
   @SuppressWarnings("unchecked")
   private String updateFormat(Properties params)
   {
      Log.info("[API] Updating format...");
      
      int id = -1;
      String formatName = null;
      String suffix = null;
      boolean twopass = false;
      String vFormat = null;
      int vBitrate = 0;
      String vResolution = null;
      String aFormat = null;
      int aBitrate = 0;
      int aChannels = 0;
      int aSampleRate = 0;
      String ffmpegParams = null;
      
      
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
            return getStatusResponse("ERROR", "Missing parameter.");
         }
         
         id = Integer.parseInt(params.getProperty("id"));
         formatName = params.getProperty("formatname");
         twopass = params.containsKey("twopass") ? Boolean.parseBoolean(params.getProperty("twopass")) : false;
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
         return getStatusResponse("ERROR", "Invalid parameter.");
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
         return getStatusResponse("ERROR", "Invalid parameter.");
      }
      
      // Create new format
      try
      {
         FormatManager.getInstance().updateFormat(id, formatName, suffix, twopass, vFormat, videoX, videoY, vBitrate, aFormat, aChannels, aSampleRate, aBitrate, ffmpegParams);
      }
      catch (Exception e)
      {
         Log.error("[API] Error updating format id " + id + ": " + e.getMessage());
         
         return getStatusResponse("ERROR", e.getMessage());
      }

      JSONObject response = new JSONObject();
      response.put("status", "OK");
      response.put("id", id);
      response.put("message", "Format updated successfully.");
      
      return response.toJSONString();
   }
   
   private String removeFormat(Properties parms)
   {
      if (!parms.containsKey("id"))
      {
         Log.error("[API] Request for delete format denied because of invalid parameter.");
         return getStatusResponse("ERROR", "Invalid parameter.");
      }
      
      int id = -1;
      
      try
      {
         id = Integer.parseInt(parms.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         Log.error("[API] Request for delete format denied because of invalid parameter: " + parms.getProperty("id"));
         return getStatusResponse("ERROR", "Invalid parameter.");
      }
      
      try
      {
         FormatManager.getInstance().deleteFormat(id);
      }
      catch (Exception e)
      {
         Log.error("[API] Request for delete failed: " + e.getMessage());
         return getStatusResponse("ERROR", e.getMessage());
      }
      
      return getStatusResponse("OK", "Format deleted successfully.");
   }
   
   /**
    * Retrieves format information in JSON form
    * @param params parameters including "id" with format id
    * @return json format information array
    */
   @SuppressWarnings("unchecked")
   private String getFormatInfoJSON(Properties params)
   {
      int id = -1;
      
      // Check if ID parameter exists
      if (params.get("id") == null)
      {
         return getStatusResponse("ERROR", "No id sent.");
      }
      
      // Check if number was sent
      try
      {
         id = Integer.parseInt(params.getProperty("id"));
      }
      catch (NumberFormatException e)
      {
         return getStatusResponse("ERROR", "Invalid ID.");
      }
      
      OutputFormat format = FormatManager.getInstance().getFormatWithId(id);
      
      // Check existence of the format
      if (format == null)
      {
         return getStatusResponse("ERROR", "Invalid ID.");
      }
      
      JSONObject response = new JSONObject();
      response.put("id", format.getId());
      response.put("formatname", format.getName());
      response.put("twopass", format.isTwopass());
      response.put("vformat", format.getVideoFormat());
      response.put("vbitrate", format.getVideoBitrate());
      response.put("vresolution", format.getVideoResolution());
      response.put("aformat", format.getAudioFormat());
      response.put("abitrate", format.getAudioBitrate());
      response.put("asamplerate", format.getAudioSamplerate());
      response.put("achannels", format.getAudioChannels());
      response.put("ffmpegparams", format.getFfmpegParams());
      response.put("suffix", format.getFileAppendix());
      
      return response.toString();
   }
      
   @SuppressWarnings("unchecked")
   private String getStatusResponse(String status, String message)
   {
	  JSONObject obj = new JSONObject();
	  obj.put("status", status);
	  obj.put("message", message);
	  
      return obj.toJSONString();
   }
}
