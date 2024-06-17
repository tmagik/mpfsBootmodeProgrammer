package com.microchip.sc.mpfsbootmodeprogrammer.utils;

public class ExitException extends Exception {

  private static final long serialVersionUID = 1L;
  
  private boolean printHelp;
  
  public ExitException(boolean printHelp, String message) {
    super(message);
    this.printHelp = printHelp;
  }
  
  public boolean isPrintHelp() {
	  return printHelp;
  }

}
