//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.invoke.tools;

import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;

public class InvokeStatus {
  
  
  private boolean isInvokedCorrectly; // To check if a exception happened
  private String  message;            // Additional message to the exception (or lack of it)
  private int     exitVal;            // Even if there was no exception, still it might not have done what was expected
  private String  stdOut;             // Contains full stdout content
  private String  stdErr;             // Contains full stderr content
  
  
  public InvokeStatus(boolean isInvokedCorrectly, String  message, int exitVal, String stdOut, String stdErr) {
    //@formatter:off
    this.isInvokedCorrectly = isInvokedCorrectly;
    this.message            = message;
    this.exitVal            = exitVal;
    this.stdOut             = stdOut;
    this.stdErr             = stdErr;
    //@formatter:on    
  }
  
  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public static InvokeStatus make(boolean isInvokedCorrectly, String  message, int exitVal, String stdOut, String stdErr) {
    return new InvokeStatus(isInvokedCorrectly, message, exitVal, stdOut, stdErr);
  }

  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public boolean isInvokedCorrectly() {
    return isInvokedCorrectly;
  }

  
  public void setInvokedCorrectly(boolean isInvokedCorrectly) {
    this.isInvokedCorrectly = isInvokedCorrectly;
  }

  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public int getExitVal() {
    return exitVal;
  }

  
  public void setExitVal(int exitVal) {
    this.exitVal = exitVal;
  }

  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public String getStdOut() {
    return stdOut;
  }

  
  public void setStdOut(String stdOut) {
    this.stdOut = stdOut;
  }

  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public String getStdErr() {
    return stdErr;
  }

  
  public void setStdErr(String stdErr) {
    this.stdErr = stdErr;
  }

  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public String getMessage() {
    return message;
  }

  
  public void setMessage(String message) {
    this.message = message;
  }
  
  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public Status onTypicalIssuesPassFailure(String failMessage) {
    if (this.isInvokedCorrectly() && this.getExitVal()==0) {
      // Check if no exception happened during the execution and if it returned zero exit code
      return Status.make(true);      
    }
    else {
      // Pass failure for everything else
      return Status.make(false, failMessage);      
    }
  }
  
  
}
