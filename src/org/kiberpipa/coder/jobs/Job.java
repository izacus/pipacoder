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

package org.kiberpipa.coder.jobs;

import org.kiberpipa.coder.Database;
import org.kiberpipa.coder.formats.OutputFormat;
import org.kiberpipa.coder.processor.FFMpegProcessor;
import org.kiberpipa.coder.processor.VideoProcessor;

public class Job
{
   private int id;	
	
   private String inputFileName;
   private OutputFormat outputFormat;
   private VideoProcessor videoProcessor;
   private String outputFileName;
   private String failMessage = null;
   
   private JobStates jobState;
   private float progress;
   
   /**
    * This constructor chooses FFMpegProcessor as default.
    * @param id Job ID
    * @param fileName Input filename
    * @param outputFormat Format to convert the file to
    */
   public Job(int id, String fileName, OutputFormat outputFormat) throws Exception
   {  
      this(id, fileName, outputFormat, null);
   }
   
   // TODO: implement other processor support, until then this is private
   private Job(int id, String fileName, OutputFormat outputFormat, VideoProcessor videoProcessor) throws Exception
   {
      if (outputFormat == null)
      {
         throw new Exception("Invalid format specified!");
      }
      
      this.id = id;
      
      this.inputFileName = fileName;
      this.outputFormat = outputFormat;
      
      // Create FFMpeg processor by default
      if (videoProcessor == null)
      {
    	  this.videoProcessor = new FFMpegProcessor(this);  
      }
      else
      {
    	  this.videoProcessor = videoProcessor;
      }
      
      this.outputFileName = inputFileName + outputFormat.getFileAppendix();
      
      this.jobState = JobStates.WAITING;
   }
   
   /**
    * Starts the file processing
    */
   public void start()
   {
      this.videoProcessor.start();
   }
   
   /**
    * Stops processing, sets job status to FAILED
    */
   public void stop()
   {
      this.videoProcessor.stop();
      
      this.fail("Cancelled by user.");
   }
   
   /**
    * Changes job state (including the database)
    * @param state
    */
   public void setState(JobStates state)
   {
      setState(state, true);
   }
   
   /**
    * Changes job state
    * @param state new state
    * @param updateDB update database record or not (false is used for eg. when loading from database)
    */
   public void setState(JobStates state, boolean updateDB)
   {
      this.jobState = state;
      
      if (updateDB)
      {
         Database.updateJobState(this);
      }
   }
   
   /**
    * Sets job state to FAILED and updates failure message
    * It does not stop the processor
    * @param message
    */
   public void fail(String message)
   {
      this.failMessage = message;
      this.setState(JobStates.FAILED, true);
   }
   
   public JobStates getState()
   {
      return this.jobState;
   }
   
   public void setProgress(float progress)
   {
	  System.out.println(progress); 
	   
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

   public int getId() 
   {
	   return id;
   }
   
   public void setId(int id)
   {
      this.id = id;
   }
   
   public String getFailMessage()
   {
      return this.failMessage;
   }
}
