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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.kiberpipa.coder.enums.JobStates;

public class JobManager implements Runnable
{
   // Singleton class initializer
   private static JobManager instance = null;
   
   public static JobManager getInstance()
   {
      if (JobManager.instance == null)
      {
         JobManager.instance = new JobManager();
      }
      
      return JobManager.instance;
   }
   
   private List<Job> jobs;
   private List<Job> jobQueue;
   
   private Object jobMonitor;
   
   private Job runningJob;
   
   
   private JobManager()
   {
      // Those datastructures are accessed from multiple threads
      jobs = Collections.synchronizedList(new ArrayList<Job>());
      jobQueue = Collections.synchronizedList(new LinkedList<Job>());
      runningJob = null;
      jobMonitor = new Object();
      
      // Create new management thread
      new Thread(this, "Job manager").start();
   }


   @Override
   public void run()
   {
     while(true)
     {
        synchronized (jobMonitor)
        {
           // Remove complete job from the variable
           if (runningJob != null &&
               (runningJob.getState() == JobStates.DONE || runningJob.getState() == JobStates.FAILED))
           {
              runningJob = null;
           }
           
           // Start executing next job if the queue is empty
           if (runningJob == null &&
               !jobQueue.isEmpty())
           {
              // Remove first element in queue
              Job nextJob = jobQueue.remove(0);
              
              runningJob = nextJob;
              nextJob.start();
           }
           
           // TODO: check output folder and remove done jobs without associated files
           
           // Wait for notification or maximum of 20 seconds before rerunning
           try
           {
              jobMonitor.wait(20000);
           } 
           catch (InterruptedException e)
           {
              // Theres nothing to be done here, execution continues normally.
              // Checked exception.
           }
        }
     }
   }
   
   public int addJob(String fileName, OutputFormat format)
   {
	  // TODO: create ID
	   int id = 0;
	   
      Job newJob = new Job(id, fileName, format);
      
      // TODO: add job to database
      
      synchronized (jobMonitor)
      {
         jobs.add(newJob);
         jobQueue.add(newJob);
         
         // Wake up the manager to check queue
         jobMonitor.notify();
      }
      
      return id;
   }
   
}
