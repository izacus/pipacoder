package org.kiberpipa.coder.processor;

import org.kiberpipa.coder.Job;

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
