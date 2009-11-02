/**
 * 
 */
package org.kiberpipa.coder;

import java.io.IOException;

/**
 * @author Jernej Virag
 *
 */
public class Main
{

   public static void main(String[] args) throws IOException
   {
      // Setup test objects
      OutputFormat format = new OutputFormat("x264/aac", "omg", ".test.mp4", "libx264", 320, 240, 500000, "libfaac", 2, 48000, 128000);
      
      JobManager jobManager = JobManager.getInstance();
      jobManager.addJob("video.ogg", format);
   }

}
