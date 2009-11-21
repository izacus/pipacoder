package org.kiberpipa.coder.userinterfaces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
   public Response serve(String uri, String method, Properties header,
                         Properties parms)
   {
      // Intercept all api calls
      String command = getAPICommandFromURI(uri);
      
      if (command != null)
      {
         String responseString = null;
         
         if (command.equals("inputfiles"))
         {
            Log.info("[API] Listing input files... ");
            
            responseString = getInputFilesJSON();
         }
         else if (command.equals("formats"))
         {
            Log.info("[API] Listing output formats...");
            
            responseString = getFormatsJSON();
         }
         else if (command.equals("jobs"))
         {
            Log.info("[API] Listing jobs...");
            
            responseString = getAllJobsJSON();
         }
         
         return new Response(HTTP_OK, "text/javascript", responseString);
      }
      
      // Attempt to serve file if there is no URL match
      return serveFile(uri, header, new File("webinterface"), false);
   }
   
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
   
   private String getInputFilesJSON()
   {
      StringBuilder output = new StringBuilder();
      
      output.append("[");
      
      File inputFileDirectory = new File(Configuration.getValue("inputdir"));
      for (String fileName : inputFileDirectory.list())
      {
         output.append("'" + fileName + "',");
      }
      
      // Replace final comma with closing bracket
      output.setCharAt(output.length() - 1, ']');
      
      return output.toString();
   }
   
   private String getFormatsJSON()
   {
      StringBuilder output = new StringBuilder();
      
      output.append("[");
      
      for (OutputFormat format : FormatManager.getInstance().getFormats())
      {
         output.append("{name:'" + format.getName() +"'},");
      }
      
      output.setCharAt(output.length() - 1, ']');
      
      return output.toString();
   }
   
   private String getAllJobsJSON()
   {
      StringBuilder output = new StringBuilder();
      output.append("[");
      
      for (Job job : JobManager.getInstance().getJobs())
      {
         output.append("{ filename:'" + job.getInputFileName() +"', ");
         output.append(" status: '" + job.getState().toString() +"'");
         
         if (job.getState() == JobStates.RUNNING)
         {
            output.append(", progress : '" + job.getProgress() * 10 + "%',");
            output.append(", eta : 'TBD' },");
         }
         else
         {
            output.append("},");
         }
         
      }
      
      output.setCharAt(output.length() - 1, ']');
      return output.toString();
   }
}
