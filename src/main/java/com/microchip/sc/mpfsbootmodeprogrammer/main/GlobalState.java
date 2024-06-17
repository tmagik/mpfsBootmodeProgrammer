//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.main;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.microchip.sc.mpfsbootmodeprogrammer.utils.Ecdsa;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings
public class GlobalState {

  
  // @formatter:off
  // Default values for arguments 
  protected static final BootMode BOOT_MODE_DEFAULT   = BootMode.IDLE;
  protected static final String   DIE_DEFAULT         = "MPFS250T_ES"; //$NON-NLS-1$
  protected static final String   DIE_PACKAGE_DEFAULT = "FCVG484";     //$NON-NLS-1$
  
  
  // Arguments
  public static Path         workDirectory;
  public static String       die;
  public static String       diePackage;
  public static BootMode     bootMode;
  public static boolean      verify;
  public static Path         elfFile;
  public static String       usk;
  public static int          startEVNMPage;
  public static int          startSNVMPage;
  public static boolean      dryRun;
  public static boolean      encrypt;
  public static ProvidingKey providingKey;
  public static Path         publicKeyPath;
  public static Path         privateKeyPath;
  public static boolean      keyEncrypted;
  public static String       keySecret;
  
  
  // Runtime and inferred values
  public static int      size;
  public static Long     bootVector; // Can be left as string as it will be just passed on
  public static boolean  saveUsk;
  //@formatter:on
  
  
  public final static Ecdsa ecdsa = new Ecdsa();

  
  public static void populateDefaults() {
    // @formatter:off
    workDirectory  = Paths.get(System.getProperty("user.dir")); //$NON-NLS-1$
    die            = DIE_DEFAULT;
    diePackage     = DIE_PACKAGE_DEFAULT;
    bootMode       = BOOT_MODE_DEFAULT;
    verify         = false;
    elfFile        = null;
    usk            = null;
    saveUsk        = false;
    startEVNMPage  = 0;
    startSNVMPage  = 0;
    dryRun         = false;
    encrypt        = false;
    providingKey   = ProvidingKey.GENERATE;
    publicKeyPath  = null;
    privateKeyPath = null;
    keyEncrypted   = false;
    keySecret      = "";
    //@formatter:on
  }
  
  
  public static int getSize32bitWordsAligned() {
    return (size + 3) / 4;
  }
  
}
