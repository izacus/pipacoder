package org.kiberpipa.coder.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kiberpipa.coder.Job;
import org.kiberpipa.coder.OutputFormat;
import org.kiberpipa.coder.enums.JobStates;

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
   
   public FFMpegProcessor(Job processingJob)
   {
      super(processingJob);
   }
   
   private String commandString()
   {
      StringBuilder string = new StringBuilder();
      
      string.append("ffmpeg.exe -y");  // -y to overwrite possibly existing output
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
      // Start FFMpeg process
      Process p = null;
      
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
         
         //System.out.println(line);
         ProcessLine(line);
      }
      
      System.out.println("Process exit code: " + p.exitValue());
      
      if (p.exitValue() != 0)
      {
         job.setState(JobStates.FAILED);
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
      
      System.out.println(line);
   }

   @Override
   public void stop()
   {
      
   }
}
