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

package org.kiberpipa.coder;

import org.kiberpipa.coder.enums.JobStates;
import org.kiberpipa.coder.processor.FFMpegProcessor;
import org.kiberpipa.coder.processor.VideoProcessor;

public class Job
{
   private int id;	
	
   private String inputFileName;
   private OutputFormat outputFormat;
   private VideoProcessor videoProcessor;
   private String outputFileName;
   
   private JobStates jobState;
   private float progress;
   
   /**
    * This constructor chooses FFMpegProcessor as default.
    * @param id Job ID
    * @param fileName Input filename
    * @param outputFormat Format to convert the file to
    */
   public Job(int id, String fileName, OutputFormat outputFormat)
   {  
      this(id, fileName, outputFormat, null);
   }
   
   public Job(int id, String fileName, OutputFormat outputFormat, VideoProcessor videoProcessor)
   {
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
   
   public void setState(JobStates state)
   {
      this.jobState = state;
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
}
