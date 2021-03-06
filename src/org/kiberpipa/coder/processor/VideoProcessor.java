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
    
    Copyrightę 2009 Jernej Virag
  */

package org.kiberpipa.coder.processor;

import org.kiberpipa.coder.jobs.Job;

public abstract class VideoProcessor implements Runnable
{
   protected Job job;
   protected Thread processingThread;
   
   public VideoProcessor(Job processingJob)
   {
      this.job = processingJob;
   }
   
   public void start()
   {
      processingThread = new Thread(this);
      processingThread.start();
   }
   
   public abstract void stop();
  
}
