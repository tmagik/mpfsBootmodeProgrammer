//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.utils;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.SecureRandom;

import com.microchip.sc.mpfsbootmodeprogrammer.main.GlobalState;

public class Utils {
  

  public static String random24HexCharacters() {
    var nonce = new byte[12]; // 12 bytes will be 24 hex nibbles
    new SecureRandom().nextBytes(nonce);  // There is tiny chance that it will generate 0s as example which we do not want to support, but taking the chances here
    var nonceHex = addLeadingText(24, "0", new BigInteger(nonce).abs().toString(16));                             //$NON-NLS-1$
    return nonceHex;
  }
  
  
  public static String strip0x(String input) {
    if (null == input) return null;
    
    if (input.startsWith("0x")) {                 //$NON-NLS-1$
      return input.substring(2);
    } else {
      return input;
    }
  }
  
    
  public static String addLeadingTextToECxyKey(BigInteger number) {
    return Utils.addLeadingText(96, "0", number.toString(16));   //$NON-NLS-1$
  }
  
  
  public static String addLeadingText(int length, String pad, String value) {
    String text = value;
    for (int x = 0; x < length - value.length(); x++) text = pad + text;
    return text;
  }
  

  public static String inputPath(String basename) {
    return GlobalState.workDirectory + "/" + basename;                                                            //$NON-NLS-1$
  }

  
  public static String outputPath(String basename) {
    return GlobalState.workDirectory + "/bootmode" + GlobalState.bootMode.getMode().toString() +"/"  + basename;  // Allow this later to be pointed to output directory  //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  
  public static void prepareOutputPath(String basename) throws ExitException {
    var dir = new File(GlobalState.workDirectory + "/bootmode" + GlobalState.bootMode.getMode().toString() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
    
    Log.info(Messages.format(MessageEnum.UTILS_PREPARING_OUTPUT_FOLDER, dir.toString()));
    
    deleteDirectory(dir).exitOnFailure(); // Delete the output directory if it exists first
    
    if (!dir.mkdir()) {
      Status.make(false, Messages.format(MessageEnum.UTILS_CANT_CREATE_FOLDER, dir.toString())).exitOnFailure();
    }
  }

  
  public static void wipeOutputPath(String basename) throws ExitException {
    var dir = new File(GlobalState.workDirectory + "/bootmode" + GlobalState.bootMode.getMode().toString() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
    
    Log.info(Messages.format(MessageEnum.UTILS_WIPING_OUTPUT_FOLDER, dir.toString()));

    deleteDirectory(dir).exitOnFailure();; // Delete the output directory after
  }
  
  
  public static Status deleteDirectory(File path) {   
    // Remove content
    try {
      if (path != null && path.exists()) {
        var files = path.listFiles();
        if (null != files) {
          for (File file : files) {
            // System.out.println(file.toString());

            if (file.isDirectory()) {
              deleteDirectory(file);
            } else {
              if (file.exists())
                if (!file.delete())
                  return Status.make(false, Messages.format(MessageEnum.UTILS_WIPING_FAILED, path.toString()));
            }
          }

          // Remove parent
          if (!path.delete())
            return Status.make(false, Messages.format(MessageEnum.UTILS_WIPING_FAILED, path.toString()));
        } else {
          // If the directory already is gone and no need to wipe it, then continue
          // quietly
        }
      } else {
        // If the directory already is gone and no need to wipe it, then continue
        // quietly
      }
    } catch (Exception e) {
      // We could be selective on exception NoSuchFileException, DirectoryNotEmptyException and IOException 
      // But in the end it doesn't matter, it failed and we have to abort
      
      return Status.make(false, Messages.format(MessageEnum.UTILS_WIPING_FAILED, path.toString()));              //$NON-NLS-1$
    }
    
    return Status.make(true);
  }


  public static void deleteDirectory(String path) {
    deleteDirectory(new File(path));
  }
  
  
  public static String getBasename(String file) {
    if (file.indexOf(".") > 0)                                                                                //$NON-NLS-1$
      return file.substring(0, file.lastIndexOf("."));                                                        //$NON-NLS-1$
         
    return file;
  }
  
  
  public static String getBasename(Path file) {
    return getBasename(file.toString());
  }

  
}
