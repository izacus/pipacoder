package org.kiberpipa.coder;

/**
 * Describes a valid output transcoding format
 * @author Jernej Virag
 *
 */
public class OutputFormat
{
     private String name;
     private String description;
     private String fileAppendix;
     
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
     
     public OutputFormat(String name,
                         String description,
                         String appendix,
                         String vFormat,
                         int vResolutionX,
                         int vResolutionY,
                         int vBitrate,
                         String aFormat,
                         int aChannels,
                         int aSamplerate,
                         int aBitrate)
     {
        this.fileAppendix = appendix;
        
        this.videoFormat = vFormat;
        this.videoBitrate = vBitrate;
        this.videoResolutionX = vResolutionX;
        this.videoResolutionY = vResolutionY;
        
        this.audioFormat = aFormat;
        this.audioChannels = aChannels;
        this.audioBitrate = aBitrate;
        this.audioSamplerate = aSamplerate;
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

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }
}
