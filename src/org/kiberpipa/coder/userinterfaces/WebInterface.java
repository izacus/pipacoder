package org.kiberpipa.coder.userinterfaces;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class WebInterface extends NanoHTTPD implements UserInterface
{  
   public WebInterface(int port) throws IOException
   {
      super(port);
      
      System.out.println("Web interface up and running on port " + port);
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
      // Attempt to serve file if there is no URL match
      return serveFile(uri, header, new File("webinterface"), false);
   }
}
