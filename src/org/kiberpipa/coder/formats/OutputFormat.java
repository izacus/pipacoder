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

package org.kiberpipa.coder.formats;

/**
 * Describes a valid output transcoding format
 * @author Jernej Virag
 *
 */
public class OutputFormat
{
   private int id;

   private String name;
   private String fileAppendix;
   private boolean twopass;
   
   // VIDEO Parameters
   private String videoFormat;
   private int videoResolutionX;
   private int videoResolutionY;
   private int videoBitrate;
     
   // AUDIO Parameters
   private String audioFormat;
   private int audioChannels;
   private int audioSamplerate;
   private int audioBitrate;
   
   // Special parameters
   private String ffmpegParams;
     
   public OutputFormat(int id,
 		 			        String name,
                       String appendix,
                       boolean twopass,
                       String vFormat,
                       int vResolutionX,
                       int vResolutionY,
                       int vBitrate,
                       String aFormat,
                       int aChannels,
                       int aSamplerate,
                       int aBitrate,
                       String ffmpegParams)
   {
        this.id = id;
        this.name = name; 
    	 
        this.fileAppendix = appendix;
        this.twopass = twopass;
        
        this.videoFormat = vFormat;
        this.videoBitrate = vBitrate;
        this.videoResolutionX = vResolutionX;
        this.videoResolutionY = vResolutionY;
        
        this.audioFormat = aFormat;
        this.audioChannels = aChannels;
        this.audioBitrate = aBitrate;
        this.audioSamplerate = aSamplerate;
        this.ffmpegParams = ffmpegParams;
   }

   public String getFileAppendix()
   {
     return fileAppendix;
   }

   public String getVideoFormat()
   {
      return videoFormat;
   }

   public int getVideoBitrate()
   {
      return videoBitrate;
   }

   public String getAudioFormat()
   {
      return audioFormat;
   }
   
   public int getAudioChannels()
   {
      return audioChannels;
   }

   public int getAudioSamplerate()
   {
      return audioSamplerate;
   }

   public int getAudioBitrate()
   {
      return audioBitrate;
   }
   
   public String getVideoResolution()
   {
      return this.videoResolutionX + "x" + this.videoResolutionY;
   }
   
   public int getVideoResX()
   {
      return this.videoResolutionX;
   }
   
   public int getVideoResY()
   {
      return this.videoResolutionY;
   }

   public String getName()
   {
      return name;
   }

   public int getId() 
   {
      return id;
   }
   
   public void setId(int id)
   {
      this.id = id;
   }

   public String getFfmpegParams()
   {
      return ffmpegParams;
   }

   public boolean isTwopass()
   {
      return twopass;
   }
}
