package org.kiberpipa.coder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
    private static Logger logger = null;
    
    static
    {
       logger = Logger.getLogger("PipaCoderMain");
       
       // TODO: change in production
       logger.setLevel(Level.ALL);
    }
    
    public static void info(String logLine)
    {
       logger.info(logLine);
    }
    
    public static void warn(String logLine)
    {
       logger.warning(logLine);
    }
    
    public static void error(String logLine)
    {
       logger.severe(logLine);
    }
}
