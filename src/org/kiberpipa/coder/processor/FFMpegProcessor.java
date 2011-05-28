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

package org.kiberpipa.coder.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiberpipa.coder.Configuration;
import org.kiberpipa.coder.Log;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.jobs.Job;
import org.kiberpipa.coder.jobs.JobStates;

public class FFMpegProcessor extends VideoProcessor
{
   // Static patterns
   private static Pattern durationPattern;
   private static Pattern progressPattern;
   
   private static HashMap<String, String> supportedVideoFormats = null;
   private static HashMap<String, String> supportedAudioFormats = null;
   
   static
   {
      durationPattern = Pattern.compile("Duration: ([0-9][0-9]):([0-9][0-9]):([0-9][0-9])\\.([0-9][0-9]), start: .*");
      progressPattern = Pattern.compile("frame=[0-9 ]+ fps=[0-9 ]+ q=[0-9\\. ]+ size=[0-9 ]+kB time=([0-9\\.]+) .*");
      
      parseAvailableFormats();
   }
   
   /**
    * Runs ffmpeg process and parses all available audio and video formats for help display
    */
   private static void parseAvailableFormats()
   {
      Log.info("Parsing available FFMPEG output formats...");
      
      String command = "";
      String OS = System.getProperty("os.name");
      
      if (OS.trim().startsWith("Win"))
      {
         command = "ffmpeg.exe";
      }
      else
      {
         command = "ffmpeg";
      }
      
      command = Configuration.getValue("ffmpegdir") + command + " -codecs";
      
      
      Process ffmpeg = null;
      
      try
      {
         ffmpeg = Runtime.getRuntime().exec(command);
      } 
      catch (IOException e)
      {
         Log.error("FATAL: Cannot execute FFMpeg!" + e.getMessage());
         System.exit(1);
      }
      
      BufferedReader output = new BufferedReader(new InputStreamReader(ffmpeg.getInputStream()));
      
      String line = "";
      
      supportedAudioFormats = new HashMap<String, String>();
      supportedVideoFormats = new HashMap<String, String>();
      
      boolean codecsStart = false;
      
      try
      {
         while((line = output.readLine()) != null)
         {
        	 if (!codecsStart)
        	 {
        		 // Find start of codec listing in output
        		 if (line.startsWith(" ------"))
        		 {
        			 codecsStart = true;
        			 continue;
        		 }
        	 }
        	 else
        	 {
        		 // Encoder format
        		 if (line.length() > 7 && line.charAt(2) == 'E')
        		 {
        			 if (line.charAt(3) == 'V')
        			 {
        				 String abbrev = line.substring(8, line.indexOf(' ', 8));
        				 String name = line.substring(line.indexOf(' ', 8));
        				 supportedVideoFormats.put(abbrev, name);
        				 
        			 }
        			 else if (line.charAt(3) == 'A')
        			 {
        				 String abbrev = line.substring(8, line.indexOf(' ', 8));
        				 String name = line.substring(line.indexOf(' ', 8));
        				 supportedAudioFormats.put(abbrev, name);
        			 }
        			 
        		 }
        	 }
         }
         
         output.close();
      } 
      catch (IOException e)
      {
         Log.warn("Error parsing FFMPEG formats: " + e.getMessage());
         
      }
      
      ffmpeg.destroy();
      Log.info("Finished parsing available FFMpeg formats.");
   }
   
   public static HashMap<String, String> getSupportedVideoFormats()
   {
      return supportedVideoFormats;
   }
   
   public static HashMap<String, String> getSupportedAudioFormats()
   {
      return supportedAudioFormats;
   }
   
   private int videoDuration;
   private volatile Process p = null;
   private LinkedHashMap<Integer, String> lastLineOutput;
   private int lineCount = 0;
   private boolean twopass = false;
   private int currentPass = 1;
   
   public FFMpegProcessor(Job processingJob)
   {
      super(processingJob);
      
      twopass = processingJob.getOutputFormat().isTwopass();
      
      // Create last ffmpeg output lines LRU
      // It will hold last 10 lines of unrecognised ffmpeg output
      lastLineOutput = new LinkedHashMap<Integer, String>()
      {
         private static final long serialVersionUID = 2103610185557147898L;

         @Override
         protected boolean removeEldestEntry(Entry<Integer, String> eldest)
         {
            return this.size() > 10;   
         }
      };
   }
   
   /**
    * Builds ffmpeg executable command line string
    * @return
    */
   private String commandString()
   {
      StringBuilder string = new StringBuilder();
      
      String executable = "";
      String OS = System.getProperty("os.name");
      
      int numCores = Runtime.getRuntime().availableProcessors();
      
      if (OS.trim().startsWith("Win"))
      {
         executable = "ffmpeg.exe";
      }
      else
      {
         executable = "ffmpeg";
      }
      
      string.append(Configuration.getValue("ffmpegdir") + executable + " -y");         // -y to overwrite possibly existing output
      string.append(" -threads " + (Runtime.getRuntime().availableProcessors() + 1));  // Number of cores + 1 threads
      
      // Input file name
      File inputFile = new File(Configuration.getValue("inputdir") + File.separatorChar + job.getInputFileName());
      string.append(" -i " + inputFile.getPath());
      
      OutputFormat outputFormat = job.getOutputFormat();
      
      // Video options
      string.append(" -vcodec " + outputFormat.getVideoFormat());
      // Sets bitrate with two parameters (certain codecs use the first, others the second)
      string.append(" -b " + outputFormat.getVideoBitrate() + " -bt " + outputFormat.getVideoBitrate());
      string.append(" -s " + outputFormat.getVideoResolution());
      
      // Sets number of threads for encoder
      string.append(" -threads " + numCores);
      
      // Set pass for twopass
      if (twopass)
      {
         string.append(" -pass " + currentPass);
         
         File logFile = new File(Configuration.getValue("outputdir") + File.separatorChar + job.getOutputFileName() + "-log");
         string.append(" -passlogfile " + logFile.getPath());
      }
      
      // Add possible additional options
      string.append(" " + outputFormat.getFfmpegParams() + " ");
      
      // Audio options
      string.append(" -acodec " + outputFormat.getAudioFormat());
      string.append(" -ab " + outputFormat.getAudioBitrate());
      string.append(" -ar " + outputFormat.getAudioSamplerate());
      string.append(" -ac " + outputFormat.getAudioChannels());
      string.append(" -threads " + numCores);
      
      // Output file name
      File outputFile = new File(Configuration.getValue("outputdir") + File.separatorChar + job.getOutputFileName());
      string.append(" " + outputFile.getPath());
      
      Log.info("[FFMPEG] Starting: " + string.toString());
      return string.toString();
   }
   
   public void start()
   {
      new Thread(this).start();
   }
   
   private void doEncodingPass()
   {
      String execString = commandString();
      
      try
      {
         p = Runtime.getRuntime().exec(execString);
      } 
      catch (IOException e)
      {
         Log.error("Failed to find ffmpeg executable!");
         job.fail("Failed to find ffmpeg executable.");
         return;
      }
      
      // Set job status to running
      job.setState(JobStates.RUNNING);
      
      BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      String line = "";
      
      while(true)
      {
         try
         {
            line = inputStreamReader.readLine();
         }
         catch(IOException e)
         {
            e.printStackTrace();
            break;
         }
         
         
         if (line == null)
         {
            break;
         }
         
         ProcessLine(line);
      }
      
      try
      {
         inputStreamReader.close();
      } 
      catch (IOException e)
      {
         Log.error(e.getMessage());
      }
      
      // Linux needs a longer time to clean up after process
      // so we need to wait or retrieving exit value fails
      try
      {
         p.waitFor();
      } 
      catch (InterruptedException e)
      {
         Log.warn("Waiting for process to terminate was interrupted.");
      }
      
      if (p.exitValue() != 0)
      {
         StringBuilder failMessage = new StringBuilder();
         
         for (String failLine : lastLineOutput.values())
         {
            failMessage.append(failLine);
            failMessage.append("\r\n");
         }
         
         job.fail(failMessage.toString());
      }
      else
      {
         if (!twopass || (twopass && currentPass == 2))
         {
            Log.info("Job completed: " + job.getInputFileName() + " => " + job.getOutputFormat().getName());
            job.setState(JobStates.DONE);
         }
      }
   }

   @Override
   public void run()
   {
      doEncodingPass();
      
      // Start the second pass of twopass encoding
      if (twopass && job.getState() == JobStates.RUNNING)
      {
         currentPass = 2;
         doEncodingPass();
      }
   }
   
   /**
    * Processes a single line of ffmpeg output looking for known patterns
    * @param line
    */
   private void ProcessLine(String line)
   {
      // Check for Duration string
      Matcher durationMatcher = durationPattern.matcher(line.trim());
      
      if (durationMatcher.matches())
      {
         // Calculate duration
         videoDuration = Integer.parseInt(durationMatcher.group(1)) * 3600 + Integer.parseInt(durationMatcher.group(2)) * 60 + Integer.parseInt(durationMatcher.group(3));
         
         return;
      }

      // Check for video progress string
      Matcher progressMatcher = progressPattern.matcher(line.trim());
      if (progressMatcher.matches())
      {
         float currentTime = Float.parseFloat(progressMatcher.group(1).trim());
         
         if (!twopass)
         {
            job.setProgress(currentTime * 100/videoDuration);
         }
         else
         {
            // In two pass encoding the first pass goes from 0 - 50%, second pass 50 - 100%
            float progress = (currentTime * 100/videoDuration)/2; 
            
            if (currentPass == 2)
            {
               progress += 50.0f;
            }
            
            job.setProgress(progress);
         }
         
         return;
      }
      
      lastLineOutput.put(lineCount++, line);
   }

   @Override
   /**
    * Kills transcoding process. Does not update job state.
    */
   public void stop()
   {
      p.destroy();
      
      try
      {
         p.waitFor();
      } 
      catch (InterruptedException e)
      {}
      
      job.fail("Stopped by user.");
   }
}
