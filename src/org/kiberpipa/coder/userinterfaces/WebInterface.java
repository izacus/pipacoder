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
import java.util.Properties;

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
         String responseString = null;
         
         // Available input file listing
         if (command.equals("inputfiles"))
         {
            Log.info("[API] Listing input files... ");
            
            responseString = getInputFilesJSON();
         }
         // Available encoding format listing
         else if (command.equals("formats"))
         {
            Log.info("[API] Listing output formats...");
            
            responseString = getFormatsJSON();
         }
         // Jobs in system listing
         else if (command.equals("jobs"))
         {
            Log.info("[API] Listing jobs...");
            
            responseString = getAllJobsJSON();
         }
         else if (command.equals("addformat"))
         {
            responseString = addNewFormat(parms);
         }
         
         // Responses are JSON, so return text/javascript MIME type
         return new Response(HTTP_OK, "text/javascript", responseString);
      }
      
      // Attempt to serve file if there is no URL match
      return serveFile(uri, header, new File("webinterface"), false);
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
      
      // { id: xx, filename: 'xx', status:'xx', progress: 'xx', eta: 'xx' }
      for (Job job : JobManager.getInstance().getJobs())
      {
         output.append("{id:" + job.getId() + ",");
         output.append("filename:'" + job.getInputFileName() +"', ");
         output.append("status: '" + job.getState().toString() +"'");
         
         if (job.getState() == JobStates.RUNNING)
         {
            output.append(",progress : '" + job.getProgress() * 10 + "%',");
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
             !params.containsKey("suffix"))
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
      id = FormatManager.getInstance().addFormat(formatName, suffix, vFormat, videoX, videoY, vBitrate, aFormat, aChannels, aSampleRate, aBitrate);
      
      if (id == -1)
      {
         return getErrorResponse("Failed to add new format.");
      }
      
      return "{ status : 'OK', id : " + id + ", message : 'Format added successfully.' }";
   }
      
   public String getErrorResponse(String message)
   {
      return "{ status : 'ERROR', message : '" + message + "' }";
   }
}
