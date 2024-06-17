//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.invoke;

import java.util.Arrays;

import com.microchip.sc.mpfsbootmodeprogrammer.invoke.tools.Invoke;
import com.microchip.sc.mpfsbootmodeprogrammer.main.GlobalState;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Env;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;

public class InvokeReadElf {
  
  private static final String  TOOL_POSTFIX   = "readelf"; //$NON-NLS-1$
  private static final String  TOOL           = Env.PATH_TO_TOOLCHAIN + Env.TOOLCHAIN_PREFIX + TOOL_POSTFIX + Env.BIN_EXTENSION;
  
  public static Status getBootVector(String basename) {
    var input  =  basename + ".elf"; //$NON-NLS-1$
    
    var status = Invoke.command(TOOL + " --program-headers " + input + " ", false); //$NON-NLS-1$ //$NON-NLS-2$
    
    // If something went wrong respond with failure message
    if (!status.isInvokedCorrectly() || status.getExitVal()!=0)
      return Status.make(false, Messages.format(MessageEnum.READELF_INVOKE_FAILED, TOOL, input));
    
    try {
      // Example stdout:
      //      Elf file type is EXEC (Executable file)
      //      Entry point 0x8000000
      //      There is 1 program header, starting at offset 64
      //
      //      Program Headers:
      //        Type           Offset             VirtAddr           PhysAddr
      //                       FileSiz            MemSiz              Flags  Align
      //        LOAD           0x0000000000001000 0x0000000008000000 0x0000000008000000
      //                       0x00000000000058a0 0x0000000000009800  RWE    0x1000
      //
      //       Section to Segment mapping:
      //        Segment Sections...
      //         00     .text .sdata .data .sbss .bss .heap .stack
      
      var needle = "Entry point";                                                                    //$NON-NLS-1$
      
      var entry = Arrays.stream(status.getStdOut().split("\\r?\\n"))                                 //$NON-NLS-1$
          .filter( line -> line.trim().startsWith(needle)).findFirst().get();
      // This can throw NoSuchElementException, but we are in the 'try' scope so it get the 'catch'
      
      Log.debug(entry);
      
      entry = entry.replace(needle, "").trim();                                                      //$NON-NLS-1$ 
      entry = entry.replace("0x", "").trim();                                                        //$NON-NLS-1$ //$NON-NLS-2$
      
      GlobalState.bootVector = Long.parseLong(entry, 16);
      
      Log.debug(Messages.format(MessageEnum.READELF_DETECTED_ENTRY, entry));
      
    } catch (Exception e) {
      return Status.make(false, Messages.format(MessageEnum.READELF_FAILED_PARSE_OUTPUT, TOOL, status.getStdOut(), e.toString()));
    }
      
    return Status.make(true);
  }

  
}
