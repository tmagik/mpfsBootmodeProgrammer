// Copyright 2021 Microchip FPGA Embedded Systems Solutions.
// SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.invoke;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.microchip.sc.mpfsbootmodeprogrammer.invoke.tools.Invoke;
import com.microchip.sc.mpfsbootmodeprogrammer.main.BootMode;
import com.microchip.sc.mpfsbootmodeprogrammer.main.GlobalState;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Env;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Utils;

public class InvokeFpgenprog {
  
  private static final String TOOL_POSTFIX        = "fpgenprog"  + Env.BIN_EXTENSION; // The binary to execute //$NON-NLS-1$
  private static final String PROJECT_FOLDER_NAME = "fpgenprogProject"; //$NON-NLS-1$ 
  
  
  private static String tool;

  
  private static String getProjectDir() {
    return GlobalState.workDirectory.toString() + "/bootmode" + GlobalState.bootMode.getMode().toString() +"/" + PROJECT_FOLDER_NAME; //$NON-NLS-1$ //$NON-NLS-2$ 
  }
  
  
  public static void setPaths(String pathToTheTool) {
    tool = pathToTheTool + "/" + TOOL_POSTFIX;                         //$NON-NLS-1$
  }
  
  
  public static Status detectTheFpgenprog() {
    var locations =
      Env.isWindows()?
        Arrays.asList(
            "C:/Microchip/Program_Debug_v2022.2/Program_Debug_Tool/bin64/",          //$NON-NLS-1$
            "C:/Microsemi/Program_Debug_v2022.2/Program_Debug_Tool/bin64/",          //$NON-NLS-1$
            "C:/Microchip/Libero_SoC_v2022.2/Designer/bin64/",                       //$NON-NLS-1$
            "C:/Microsemi/Libero_SoC_v2022.2/Designer/bin64/"                        //$NON-NLS-1$
        ) : Arrays.asList(
            "/usr/local/microchip/Program_Debug_v2022.2/Program_Debug_Tool/bin64/",  //$NON-NLS-1$
            "/usr/local/microsemi/Program_Debug_v2022.2/Program_Debug_Tool/bin64/",  //$NON-NLS-1$
            "/usr/local/microchip/Libero_SoC_v2022.2/Designer/bin64/",               //$NON-NLS-1$
            "/usr/local/microsemi/Libero_SoC_v2022.2/Designer/bin64/"                //$NON-NLS-1$
        );
             
    var envKey = "FPGENPROG"; //$NON-NLS-1$
    if (System.getenv().containsKey(envKey)) {
      // If environment variable set, do not test other locations
      tool = System.getenv(envKey);
           
      try {
        if (!Files.isExecutable(Path.of(tool))) {
          // Something is set in the FPGENPROG but doesn't point to a binary file
          return Status.make(false, Messages.format(MessageEnum.FPGENPROG_FPGENPROG_WRONG, TOOL_POSTFIX));
        }        
      } catch (Exception e) {
        // Give custom error messages for any cases when there are new lines in
        // in the path. And cover any other obscure reasons when the above could
        // fail.
        return Status.make(false, Messages.format(MessageEnum.FPGENPROG_FPGENPROG_PATH_EXCEPTION));        
      }

      // The FPGENPROG is specified and it points to a binary file
      return Status.make(true);
    }
    
    // The FPGENPROG environment variable was not specified, so try to detect it.
    // Test if the fpgenprog can be found in the path list
    var found = locations.stream()
        .filter(path -> Files.isExecutable(Paths.get(path + TOOL_POSTFIX)))
        .findFirst();

    // fpgenprog not found, return failure 
    if (found.isEmpty())
      return Status.make(false, Messages.format(MessageEnum.FPGENPROG_NOT_FOUND, Env.BIN_EXTENSION, Env.BIN_EXTENSION));

    // Got here so far so success
    setPaths(found.get());
    return Status.make(true);
  }
  
  
  public static Status createProject() {
    Utils.deleteDirectory(getProjectDir());
    
    var status = Invoke.command(tool + " new_project" +    //$NON-NLS-1$
        " --location " + getProjectDir() +                 //$NON-NLS-1$
        " --target_die " + GlobalState.die +               //$NON-NLS-1$
        " --target_package " + GlobalState.diePackage,     //$NON-NLS-1$
        true);
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_PROJECT_CREATION_FAILED, getProjectDir()));
  }
  
  
  public static Status selectBootmode(BootMode bootmode, String bootcfg, BigInteger x, BigInteger y) {
    var command = tool + " mss_boot_info" +                //$NON-NLS-1$ 
        " --location " + getProjectDir() +                 //$NON-NLS-1$
        " --u_mss_bootmode " + bootmode.getMode();         //$NON-NLS-1$
    
    if (null != bootcfg)
      command += " --u_mss_bootcfg " + bootcfg;            //$NON-NLS-1$
    
    if (null != x && null != y) {
      // 48 bytes is 96 hex character, if the number is smaller, make sure they are prepended with zeros
      command += String.format(" --ucskx %s", Utils.addLeadingTextToECxyKey(x));     //$NON-NLS-1$
      command += String.format(" --ucsky %s", Utils.addLeadingTextToECxyKey(y));     //$NON-NLS-1$
      
      command += " --reset_sbic_version";                                      //$NON-NLS-1$
    }
        
    var status = Invoke.command(command, true);
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_BOOTMODE_SELECT_FAILED, bootmode.getModeAndDescription(), getProjectDir()));
  }

  
  public static Status selectBootmode(BootMode bootmode) {
    return selectBootmode(bootmode, null, null, null);
  }

  
  public static Status envmClient(String fileName, long startPage, Long bootaddr, int numberOfBytes) {
    var hexFileName    = fileName + ".hex";                                             //$NON-NLS-1$
    var bootModeString = "bootmode" + GlobalState.bootMode.getMode() + "_" + startPage; //$NON-NLS-1$ //$NON-NLS-2$
    
    var command = tool + " envm_client" +                  //$NON-NLS-1$ 
        " --location " + getProjectDir() +                 //$NON-NLS-1$
        " --number_of_bytes " + numberOfBytes +            //$NON-NLS-1$
        " --content_file_format intel-hex" +               //$NON-NLS-1$
        " --content_file " + hexFileName +                 //$NON-NLS-1$
        " --start_page " + startPage +                     //$NON-NLS-1$
        " --client_name " + bootModeString;                //$NON-NLS-1$
    
    if (null != bootaddr) {
      // command += String.format(" --mem_file_base_address 0x%08X", bootaddr); //$NON-NLS-1$
      // Do not prefix 0x before the 'naked' hex number to avoid the error below 
      // Error: Parameter 'mem_file_base_address' with value '0x20220000' is not valid hex string.      
      command += String.format(" --mem_file_base_address %08X", bootaddr); //$NON-NLS-1$  
    }
    
    var status = Invoke.command(command, true);
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_ENVM_CLIENT_FAILED, getProjectDir()));
  }
  

  public static Status envmClient(String fileName) {
    return envmClient(fileName, 0, null, GlobalState.size);
  }


  public static Status envmClient(String fileName, long startPage, Long bootaddr) {
    return envmClient(fileName, startPage, bootaddr, GlobalState.size);  
  }
  
  
  public static Status snvmClient(String fileName, long startPage, Long bootaddr, boolean accessRegisters, boolean encrypt) {
    var hexFileName    = fileName + ".hex";                                                                    //$NON-NLS-1$
    var bootModeString = "bootmode" + GlobalState.bootMode.getMode();                                          //$NON-NLS-1$
    
    var command = tool + " snvm_client" +                                                                      //$NON-NLS-1$ 
        " --location " + getProjectDir() +                                                                     //$NON-NLS-1$
        " --number_of_bytes " + GlobalState.size +                                                             //$NON-NLS-1$
        " --content_file_format intel-hex" +                                                                   //$NON-NLS-1$
        " --content_file " + hexFileName +                                                                     //$NON-NLS-1$
        " --start_page " + startPage +                                                                         //$NON-NLS-1$
        " --client_name " + bootModeString +                                                                   //$NON-NLS-1$
        " --usk_key " + GlobalState.usk;                                                                       //$NON-NLS-1$
    
    if (encrypt) {
      command += " --encrypted_authenticated";                                                                 //$NON-NLS-1$
    } else {
      command += " --authenticated";                                                                           //$NON-NLS-1$
    }
    
    if (null != bootaddr) {  
      command += String.format(" --mem_file_base_address %08X", bootaddr);                                     //$NON-NLS-1$  
    }
    
    if (accessRegisters) {
      command += " --fabric_access_read 0 --fabric_access_write 0 --mss_access_read 1 --mss_access_write 0";   //$NON-NLS-1$
    }
    
    var status = Invoke.command(command, false);
    
    var ret = status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_SNVM_CLIENT_FAILED, getProjectDir()));

    if (!ret.isFinishedCorrectly()) {
      // Be quiet (no STDOUT), but on failure print the STDOUT. And later on higher level exit with STDERR message too
      Log.info(status.getStdOut());
    }

    return ret;
  }
  

  public static Status generateBistream() {
    var status = Invoke.command(tool + " generate_bitstream" +    //$NON-NLS-1$ 
        " --location " + getProjectDir(),                         //$NON-NLS-1$
        false);
    
    var ret = status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_GENERATE_BITSTREAM_FAILED, getProjectDir()));
  
    if (!ret.isFinishedCorrectly()) {
      // Be quiet (no STDOUT), but on failure print the STDOUT. And later on higher level exit with STDERR message too 
      Log.info(status.getStdOut());
    }
  
    return ret; 
  }
  
  
  public static Status runAction() {
    var status = Invoke.command(tool + " run_action" +    //$NON-NLS-1$ 
        " --location " + getProjectDir() +                //$NON-NLS-1$
        " --action PROGRAM",                              //$NON-NLS-1$
        false, false, false);
    
    var ret = status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_RUN_ACTION_PROGRAM_FAILED, Env.BIN_EXTENSION, getProjectDir()));
    
    if (!ret.isFinishedCorrectly()) {
      // Be quiet (no STDOUT), but on failure print the STDOUT. And later on higher level exit with STDERR message too 
      Log.info(status.getStdOut());
    }
       
    return ret;
  }
  
  
  public static Status verify() {
    var status = Invoke.command(tool + " run_action" +    //$NON-NLS-1$ 
        " --location " + getProjectDir() +                //$NON-NLS-1$
        " --action VERIFY",                               //$NON-NLS-1$
        false, false, false);
    
    var ret = status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.FPGENPROG_VERIFY_FAILED, getProjectDir()));
       
    if (!ret.isFinishedCorrectly()) {
      // Be quiet (no STDOUT), but on failure print the STDOUT. And later on higher level exit with STDERR message too 
      Log.info(status.getStdOut());
    }

    return ret; 
  }
  
  
}
