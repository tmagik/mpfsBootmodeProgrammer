//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.invoke;

import com.microchip.sc.mpfsbootmodeprogrammer.invoke.tools.Invoke;
import com.microchip.sc.mpfsbootmodeprogrammer.main.GlobalState;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Env;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;

public class InvokeSize {
  
  
  private static final String  TOOL_POSTFIX = "size"; //$NON-NLS-1$
  private static final String  TOOL         = Env.PATH_TO_TOOLCHAIN + Env.TOOLCHAIN_PREFIX + TOOL_POSTFIX + Env.BIN_EXTENSION;
  
  
  public static Status getTextAndDataSize(String basename) {
    // Because the text and data might be aligned differently which depends on the linker script
    // it means that the sections my physically occupy slightly more space compared to how
    // much they logically need, therefore just getting file size of the bin file is more reliable
    // than this method.
    
    var input  = GlobalState.workDirectory.toString() + "/" + basename + ".elf";                //$NON-NLS-1$ //$NON-NLS-2$
    
    var status = Invoke.command(TOOL + " --format=berkeley " + input + " ", false);         //$NON-NLS-1$ //$NON-NLS-2$
    
    // If something went wrong respond with failure message
    if (!status.isInvokedCorrectly() || status.getExitVal()!=0)
      return Status.make(false, Messages.format(MessageEnum.SIZE_INVOKE_FAILED, TOOL, input));
    
    try {
      // Example stdout:
      //   text    data     bss     dec     hex filename
      //  19040    3648   12560   35248    89b0 C:\Microchip\SoftConsole-v6.4\extras\workspace.examples\mpfs-blinky\Debug/mpfs-blinky.elf
      
      var size = status.getStdOut().split("\\r?\\n")[1]; // get the second line   //$NON-NLS-1$
      
      Log.debug(size);
      
      size = size.replaceAll("\\s+", " "); // remove all EXTRA white-spaces (keep just one space)  //$NON-NLS-1$ //$NON-NLS-2$
      size = size.trim();                  // remove first white-space
      var text = size.split(" ")[0];       // get the TEXT column                                  //$NON-NLS-1$
      var data = size.split(" ")[1];       // get the DATA column                                  //$NON-NLS-1$
      
      GlobalState.size = Integer.parseInt(text) + Integer.parseInt(data);
      
      Log.debug(Messages.format(MessageEnum.SIZE_DETECTED_SIZE, size));
      
    } catch (Exception e) {
      return Status.make(false, Messages.format(MessageEnum.SIZE_FAILED_PARSE_OUTPUT, TOOL, status.getStdOut(), e.toString()));
    }
      
    return Status.make(true);
  }

  
}
