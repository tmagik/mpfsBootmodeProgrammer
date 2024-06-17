//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.invoke.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;

public class Invoke {

  
  public static InvokeStatus command(String cmdLine, boolean printStdOut , boolean printStdErr, boolean catchStdErr) {
    // Setting the catchStdErr false and then printStdErr to true will not print any 
    // StdErr as you are not catching in the first place
    Log.debug(Messages.format(MessageEnum.INVOKE_VERBOSE_INVOKE, cmdLine));
    
    var stdOutBuf = new StringBuffer();    // All stdout lines together
    var stdErrBuf = new StringBuffer();    // All stderr lines together 
    var retMessage   = "";                 // Exception and other status messages                        //$NON-NLS-1$
    
    Process proc;
    try {
      proc = Runtime.getRuntime().exec(cmdLine);
      // if better handing of spaces and quotes is needed then it might have to be done with the
      // process builder:
      // https://blog.krecan.net/2008/02/09/processbuilder-and-quotes/
      // https://coderanch.com/t/530076/java/Runtime-exec-method-executing-correct
    } catch (Throwable  e ) {
      retMessage = Messages.format(MessageEnum.INVOKE_EXCEPTION, cmdLine, e.toString());
      
      // Most likely the exception message will be displayed in the parent 
      // anyway, so this might be redundant        
      Log.debug(retMessage);         
      
      return InvokeStatus.make(false, retMessage, -1, stdOutBuf.toString(), stdErrBuf.toString());
    }
      
    var stdOutLine = ""; //$NON-NLS-1$
    var stdErrLine = ""; //$NON-NLS-1$

    BufferedReader stdOut;
    BufferedReader stdErr;
    try {
      stdOut = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
      stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      return InvokeStatus.make(
          false, 
          Messages.format(MessageEnum.INVOKE_EXCEPTION, cmdLine, e.toString()),
          -1, 
          "",                                                                      //$NON-NLS-1$
          ""                                                                       //$NON-NLS-1$
      );
    }
    
    try {
      if (catchStdErr) {
        // Will be capturing the std error as well
        stdOutLine = stdOut.readLine(); // Single line
        stdErrLine = stdErr.readLine(); // Single line
        
        // Try to process both stdout/stderr at the same time
        while ( (stdOutLine != null) || (stdErrLine != null) ) {
          
          // As first process the stdout line (if there is something)
          if (stdOutLine != null) {
            stdOutBuf.append(stdOutLine + '\n');
            
            if (printStdOut) Log.info(stdOutLine);
          }
          
          // Then as second process the stderr line (if there is something)
          if (stdErrLine != null) {
            stdErrBuf.append(stdErrLine + '\n');
            
            if (printStdErr) Log.error(stdErrLine);
          }
          
          // Read next set of lines
          stdOutLine = stdOut.readLine();
          stdErrLine = stdErr.readLine();          
        } 
        
      } else {
        
        while ( (stdOutLine = stdOut.readLine()) != null ) {          
          stdOutBuf.append(stdOutLine + '\n'); 
          
          if (printStdOut) Log.info(stdOutLine);
        }
      }

      var exitVal = proc.waitFor();
      retMessage = Messages.format(MessageEnum.INVOKE_FINISHED, Integer.toString(exitVal));

      Log.debug(retMessage);
      
      return InvokeStatus.make(true, retMessage, exitVal, stdOutBuf.toString(), stdErrBuf.toString());
      
    } catch (Throwable  e ) {
      retMessage = Messages.format(MessageEnum.INVOKE_EXCEPTION, cmdLine, e.toString());
      
      // Most likely the exception message will be displayed in the parent 
      // anyway, so this might be redundant
      Log.debug(retMessage);         
      
      return InvokeStatus.make(false, retMessage, -1, stdOutBuf.toString(), stdErrBuf.toString());
    } finally {
      try {
        stdOut.close();
        stdErr.close();
      } catch (IOException e) {
        // If we failed to close a stream then ignore it anyway, this is not invoked that frequently
        // that even in the edge cause of catastrophic failure of closing the stdout/stderr stream it
        // wouldn't be that huge leak to deal, but worrying why it would fail in the first place.
        // However if the rest of the command finished without problems, then let's not interfere with
        // the status results
      }
    }
  }

  
  // Shorter method with printStdErr set to true
  public static InvokeStatus command(String cmdLine, boolean printStdOut) {
    return command(cmdLine, printStdOut , true, true);
  }
  
  
}
