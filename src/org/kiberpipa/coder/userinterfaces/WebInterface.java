package org.kiberpipa.coder.userinterfaces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.kiberpipa.coder.Log;
import org.kiberpipa.coder.formats.FormatManager;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.jobs.Job;
import org.kiberpipa.coder.jobs.JobManager;

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
      if (uri.equals("/list"))
      {
         StringBuilder response = new StringBuilder();
         
         response.append("<html><body>Formats: <br>");
         
         ArrayList<OutputFormat> formats = FormatManager.getInstance().getFormats();
         
         for (OutputFormat format : formats)
         {
            response.append(format.getName() + "<br>");
         }
         
         response.append("Jobs: <br>");
         
         ArrayList<Job> jobs = JobManager.getInstance().getJobs();
         
         for (Job job : jobs)
         {
            response.append(job.getInputFileName() + "<br>");
         }
         
         response.append("</body></html>");
         
         return new Response(HTTP_OK, "text/html", response.toString());
      }
      
      // Attempt to serve file if there is no URL match
      return serveFile(uri, header, new File("webinterface"), false);
   }
}
