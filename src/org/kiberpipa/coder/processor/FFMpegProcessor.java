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

package org.kiberpipa.coder.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiberpipa.coder.Configuration;
import org.kiberpipa.coder.enums.JobStates;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.jobs.Job;

public class FFMpegProcessor extends VideoProcessor
{
   // Static patterns
   private static Pattern durationPattern;
   private static Pattern progressPattern;
   
   static
   {
      durationPattern = Pattern.compile("Duration: ([0-9][0-9]):([0-9][0-9]):([0-9][0-9])\\.([0-9][0-9]), start: .*");
      progressPattern = Pattern.compile("frame=[0-9 ]+ fps=[0-9 ]+ q=[0-9\\. ]+ size=[0-9 ]+kB time=([0-9\\.]+) .*");
   }
   
   private int videoDuration;
   private Process p = null;
   private LinkedHashMap<Integer, String> lastLineOutput;
   private int lineCount = 0;
   
   public FFMpegProcessor(Job processingJob)
   {
      super(processingJob);
      
      // Create last ffmpeg output lines LRU
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
   
   private String commandString()
   {
      StringBuilder string = new StringBuilder();
      
      string.append(Configuration.getValue("ffmpegdir") + "ffmpeg.exe -y");  // -y to overwrite possibly existing output
      string.append(" -threads " + (Runtime.getRuntime().availableProcessors() + 1));  // Number of cores + 1 threads
      
      // Input file name
      string.append(" -i " + job.getInputFileName());
      
      OutputFormat outputFormat = job.getOutputFormat();
      
      // Video options
      string.append(" -vcodec " + outputFormat.getVideoFormat());
      string.append(" -b " + outputFormat.getVideoBitrate() + " -bt " + outputFormat.getVideoBitrate());
      string.append(" -s " + outputFormat.getVideoResolution());
      
      // Audio options
      string.append(" -acodec " + outputFormat.getAudioFormat());
      string.append(" -ab " + outputFormat.getAudioBitrate());
      string.append(" -ar " + outputFormat.getAudioSamplerate());
      string.append(" -ac " + outputFormat.getAudioChannels());
      
      // Output file name
      string.append(" " + job.getOutputFileName());
      
      return string.toString();
   }
   
   public void start()
   {
      new Thread(this).start();
   }

   @Override
   public void run()
   {
      String execString = commandString();
      
      System.out.println(execString);
      
      try
      {
         p = Runtime.getRuntime().exec(execString);
      } catch (IOException e)
      {
         System.err.println("Failed to find ffmpeg executable!");
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
      
      System.out.println("Transcoding done, exit code was " + p.exitValue());
      
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
         job.setState(JobStates.DONE);
      }
   }
   
   private void ProcessLine(String line)
   {
      // Check for Duration string
      Matcher durationMatcher = durationPattern.matcher(line.trim());
      
      if (durationMatcher.matches())
      {
         // Calculate duration
         videoDuration = Integer.parseInt(durationMatcher.group(1)) * 3600 + Integer.parseInt(durationMatcher.group(2)) * 60 + Integer.parseInt(durationMatcher.group(3));
         System.out.println("Duration of video is " + videoDuration + " seconds.");
         
         return;
      }

      // Check for video progress string
      Matcher progressMatcher = progressPattern.matcher(line.trim());
      if (progressMatcher.matches())
      {
         float currentTime = Float.parseFloat(progressMatcher.group(1).trim());
         
         job.setProgress(currentTime * 100/videoDuration);
         
         return;
      }
      
      lastLineOutput.put(lineCount++, line);
      
      System.out.println(line);
   }

   @Override
   public void stop()
   {
      p.destroy();
      job.setState(JobStates.FAILED);
   }
}
