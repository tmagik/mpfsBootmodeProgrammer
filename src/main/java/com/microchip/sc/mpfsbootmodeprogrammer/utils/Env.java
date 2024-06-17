//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.utils;

public class Env {
  
  public static final String TOOLCHAIN_PREFIX     = "riscv64-unknown-elf-";                                  //$NON-NLS-1$
  public static final String BIN_EXTENSION        = isWindows() ? ".exe" : "";                               //$NON-NLS-1$ //$NON-NLS-2$
  public static final String ENV_KEY_PASSWORD_KEY = "SC_ECDSA_PRIV_KEY_PASS";                                //$NON-NLS-1$

  
  //@formatter:off
  public static final String SC_INSTALL_PATH   = getScInstallPath(); 
  public static final String PATH_TO_TOOLCHAIN = SC_INSTALL_PATH + "/riscv-unknown-elf-gcc/bin/";            //$NON-NLS-1$
  //@formatter:on
  
  
  public static boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");                                              //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  
  public static String getScInstallPath() {
    //TODO: Add heuristics to detect few common locations of SC
    var scInstallationKey = "SC_INSTALL_DIR";                                                             //$NON-NLS-1$
    
    if (!System.getenv().containsKey(scInstallationKey)) {
      Log.error(Messages.format(MessageEnum.ENV_NO_SC_INSTALL_DIR, scInstallationKey));
      return "";                                                                                          //$NON-NLS-1$ 
    }
    
    return System.getenv(scInstallationKey);
  }
  
  
  public static String getEcdsaPrivKeyPass() {
    if (System.getenv().containsKey(ENV_KEY_PASSWORD_KEY)) {
      return System.getenv(ENV_KEY_PASSWORD_KEY);    
    } else {      
      Log.warn(Messages.format(MessageEnum.ENV_SC_ECDSA_PRIV_KEY_PASS, ENV_KEY_PASSWORD_KEY));
      return "";                                                                                          //$NON-NLS-1$
    }
  }
  

}
