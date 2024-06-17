//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.utils;

//SPDX-License-Identifier: MIT
import java.util.Map;

public class Status {
  
  
  final Map.Entry<Boolean, String> status;

  
  public Status(Boolean isFinishedCorrectly, String message) {
    status = Map.entry(isFinishedCorrectly, message);
  }
  

  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public static Status make(Boolean isFinishedCorrectly, String message) {
    return new Status(isFinishedCorrectly, message);
  }


  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public static Status make(Boolean isFinishedCorrectly) {
    // Make status with empty message
    return new Status(isFinishedCorrectly, "");                              //$NON-NLS-1$
  }
  
  
  // Printer used only from unit tests
  public void printMessageIfNotExpected(boolean expected) {
    if (expected != isFinishedCorrectly()) {
      Log.error("Not expecting this result:");                               //$NON-NLS-1$
      Log.error(getMessage());
    }
  }

  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public Boolean isFinishedCorrectly() {
    return status.getKey();
  }
  
  
  @edu.umd.cs.findbugs.annotations.CheckReturnValue
  public String getMessage() {
    return status.getValue();
  }

  
  @Override
  public String toString() {
    return "Status [" + isFinishedCorrectly() + ", " + getMessage() + "]";   //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
  }
  

  public void exitOnFailure() throws ExitException {
    if (!this.isFinishedCorrectly()) {
      throw new ExitException(false, this.getMessage());
    }
  }

  
}
