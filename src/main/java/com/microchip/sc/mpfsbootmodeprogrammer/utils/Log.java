package com.microchip.sc.mpfsbootmodeprogrammer.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class Log {
  private static final  String FAKE_LOGGER_TIME_FORMAT = "HH:mm:ss";                                  //$NON-NLS-1$
  private static final  String FAKE_LOGGER_FORMAT      = "%s %s - %s";                                //$NON-NLS-1$
    
  private static        String loggerFilename          = null;
  
  
  private static String getTime() {
    var formatter = DateTimeFormatter.ofPattern(FAKE_LOGGER_TIME_FORMAT);
    return LocalTime.now().format(formatter);
  }
  
  
  private static void addStringToLog(String line) {
    if (null != loggerFilename) {
      // Only try to save to a file, when a filename was correctly set and tested that it's writtable
      
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(loggerFilename, true));
        writer.append(line);
        writer.append("\r\n");      // Add a newline to the end of the line                       //$NON-NLS-1$
        writer.close();    
      } catch (IOException e) {
        var oldFilename = loggerFilename;
        removeLoggerFile(); // Something went wrong, no point to try the next time
        warn(Messages.format(MessageEnum.LOG_FILE_WRITE_FAILED, oldFilename));
      }      
    }
  }
  
  
  public static void info(String message) {
    var fullMessage = String.format(FAKE_LOGGER_FORMAT, getTime(), "INFO ", message);               //$NON-NLS-1$
    System.out.println(fullMessage);
    addStringToLog(fullMessage);
  }
  
  
  public static void warn(String message) {
    var fullMessage = String.format(FAKE_LOGGER_FORMAT, getTime(), "WARN ", message);               //$NON-NLS-1$
    System.out.println(fullMessage);
    addStringToLog(fullMessage);
  }
  
  
  public static void error(String message) {
    if (null != message && message.length() > 0) {
      // Only log non-empty messages, ignore empty ones
      var fullMessage = String.format(FAKE_LOGGER_FORMAT, getTime(), "ERROR", message);               //$NON-NLS-1$
      System.err.println(fullMessage);
      addStringToLog(fullMessage);      
    }
  }
  
  
  public static void debug(String message) {
    var fullMessage = String.format(FAKE_LOGGER_FORMAT, getTime(), "DEBUG", message);               //$NON-NLS-1$
    addStringToLog(fullMessage);
  }
  
  
  public static Status configureLoggerFile(String filename) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
      writer.append("\r\n");      // Write one line just to confirm we can write to the file      //$NON-NLS-1$
      writer.close();    
      loggerFilename = filename;  // We didn't have exception so far, so we can direct all logging into the file
      return Status.make(true);
          
    } catch (IOException e) {
      
      return Status.make(false, Messages.format(MessageEnum.LOG_FILE_WRITE_FAILED, filename));
    }    
  }
  
  
  public static void removeLoggerFile() {
    loggerFilename = null;
  }
  
  
}
