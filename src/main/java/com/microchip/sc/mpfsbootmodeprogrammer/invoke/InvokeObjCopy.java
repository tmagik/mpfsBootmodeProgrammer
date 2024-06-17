//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.invoke;

import com.microchip.sc.mpfsbootmodeprogrammer.invoke.tools.Invoke;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Env;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;

public class InvokeObjCopy {
  
  
  private static final String TOOL_POSTFIX = "objcopy"; //$NON-NLS-1$
  private static final String TOOL         = Env.PATH_TO_TOOLCHAIN + Env.TOOLCHAIN_PREFIX + TOOL_POSTFIX + Env.BIN_EXTENSION;
  
  
  public static Status toolVersionCheck() {
    // Run the OBJCOPY with --version to see if the SC_INSTALL_DIR is set correctly to functional SC installation.
    // Assuming that --version worked on OBJCOPY, so other tools are just fine (if their execution will fail they 
    // will produce their own error messages anyway).
    // Doing the SC_INSTALL_DIR check on the OBJCOPY because that's the very first tool which will be invoked
    // from all the riscv toolchain.
    
    var status = Invoke.command(TOOL + " --version", false); //$NON-NLS-1$   
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.OBJCOPY_SANITY_CHECK_FAILED));
  }
  
  
  public static Status generateHexFromElf(String inputBasename, String outputBasename) {
    var input  = inputBasename  + ".elf";    //$NON-NLS-1$
    var output = outputBasename + ".hex";    //$NON-NLS-1$ 
 
    var status = Invoke.command(TOOL + " -O ihex " + input + " " + output + " ", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.OBJCOPY_ELF2HEX_FAILED, TOOL, input, output));
  }

   
  public static Status generateHexFromBin(String basename) {
    // TODO: duplicate code, generalize
    var input  = basename + ".bin";          //$NON-NLS-1$
    var output = basename + ".hex";          //$NON-NLS-1$
 
    var status = Invoke.command(TOOL + " -I binary -O ihex " + input + " " + output + " ", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.OBJCOPY_BIN2HEX_FAILED, TOOL, input, output));
  }

  
  public static Status generateHexFromBin(String basenameInput, String basenameOutput, long offset) {
    var input  = basenameInput  + ".bin";          //$NON-NLS-1$
    var output = basenameOutput + ".hex";          //$NON-NLS-1$
    
    var changeSections = (0 == offset) ? "" : String.format("--change-section-lma *+0x%08X", offset);                      //$NON-NLS-1$ //$NON-NLS-2$
 
    var status = Invoke.command(TOOL + " -I binary -O ihex " + changeSections + " " + input + " " + output + " ", false);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.OBJCOPY_BIN2HEX_FAILED, TOOL, input, output));  //$NON-NLS-1$
  }

  
  public static Status generateHexFromBin(String basenameInput, String basenameOutput) {
    return generateHexFromBin(basenameInput, basenameOutput, 0);
  }
  
  
  public static Status generateBinFromElf(String inputBasename, String outputBasename) {
    var input  = inputBasename  + ".elf";    //$NON-NLS-1$ 
    var output = outputBasename + ".bin";    //$NON-NLS-1$ 
 
    var status = Invoke.command(TOOL + " -O binary " + input + " " + output + " ", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    return status.onTypicalIssuesPassFailure(Messages.format(MessageEnum.OBJCOPY_ELF2BIN_FAILED, TOOL, input, output));
  }
  
  
}
