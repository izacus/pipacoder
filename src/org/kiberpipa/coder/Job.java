package org.kiberpipa.coder;

import org.kiberpipa.coder.enums.JobStates;
import org.kiberpipa.coder.processor.FFMpegProcessor;
import org.kiberpipa.coder.processor.VideoProcessor;

public class Job
{
   private String inputFileName;
   private OutputFormat outputFormat;
   private VideoProcessor videoProcessor;
   private String outputFileName;
   
   private JobStates jobState;
   private float progress;
   
   /**
    * This constructor chooses FFMpegProcessor as default.
    * @param fileName Input filename
    * @param outputFormat Format to convert the file to
    */
   public Job(String fileName, OutputFormat outputFormat)
   {  
      this(fileName, outputFormat, null);
   }
   
   public Job(String fileName, OutputFormat outputFormat, VideoProcessor videoProcessor)
   {
      this.inputFileName = fileName;
      this.outputFormat = outputFormat;
      this.videoProcessor = new FFMpegProcessor(this);
      this.outputFileName = inputFileName + outputFormat.getFileAppendix();
      
      this.jobState = JobStates.WAITING;
   }
   
   public void start()
   {
      this.videoProcessor.start();
   }
   
   public void setState(JobStates state)
   {
      synchronized (this.jobState)
      {
         
      }
      this.jobState = state;
   }
   
   public JobStates getState()
   {
      return this.jobState;
   }
   
   public void setProgress(float progress)
   {
      this.progress = progress;
   }
   
   public float getProgress()
   {
      return this.progress;
   }
   
   public OutputFormat getOutputFormat()
   {
      return this.outputFormat;
   }

   public String getOutputFileName()
   {
      return outputFileName;
   }

   public String getInputFileName()
   {
      return inputFileName;
   }
}
