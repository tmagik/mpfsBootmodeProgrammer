//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.main;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import com.microchip.sc.mpfsbootmodeprogrammer.utils.Env;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Utils;

// TODO: Allow to select custom output directory
// TODO: Allow to select one flashpro from many connected after Libero support will be added

public class CmdArguments {

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings
  public  final static List<String> DIE_OPTIONS = Arrays.asList("MPFS025T", "MPFS095T", "MPFS160T", "MPFS250T",                      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      "MPFS460T", "MPFS025T_ES", "MPFS095T_ES", "MPFS160T_ES", "MPFS250T_ES", "MPFS460T_ES");                                        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings
  public  final static List<String> DIE_PACKAGE_OPTIONS = Arrays.asList("FCG1152", "FCSG325", "FCSG536", "FCVG484", "FCVG784");      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

  public static String flagsToString() {
    return "Workdir=" + GlobalState.workDirectory + " die=" + GlobalState.die + " diePackage=" + GlobalState.diePackage + " bm=" +   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        GlobalState.bootMode.getModeAndDescription() + " verify=" + GlobalState.verify + " elf=" + GlobalState.elfFile;              //$NON-NLS-1$ //$NON-NLS-2$
  }

  
  public static Status parse(String[] args) {
    // Simpler wrapper with simpler argument calling the fuller function
    return parse(Arrays.asList(args).iterator());
  }
  
  
  private static Path parseKeyFileStringToPath(String filePath) {
    Path ret = null;
    try {
      ret = Path.of(filePath);
      
      if ( Files.notExists(ret) ||  
           !Files.isRegularFile(ret) ) {
        return null;                
      }
    } catch (Exception e) {
      return null;                
    }    
    
    return ret;
  }

  
  public static Status parse(Iterator<String> argumentIterator) {
    boolean onlyOneFile = true;

    // Set default values first
    GlobalState.populateDefaults();

//    if (!argumentIterator.hasNext()) {
//      // nothing given, exit with help message
//      return Map.entry(false, "The " + Prog.toolName + " needs some arguments"); //$NON-NLS-1$ //$NON-NLS-2$
//    }

    while (argumentIterator.hasNext()) {
      String currentArgument = argumentIterator.next();
      switch (currentArgument) {

      case "--workdir": {                                          //$NON-NLS-1$
        if (!argumentIterator.hasNext())
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));

        String workdirString = argumentIterator.next();
        GlobalState.workDirectory = Paths.get(workdirString);

        if (GlobalState.workDirectory == null) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_WORKDIR_NOT_A_PATH, workdirString));          
        }
        
        break;
      }

      case "--help": { //$NON-NLS-1$
        // This is odd case, because we are returning status that this failed,
        // even when the argument is parsed correctly, this is to force the
        // printing of the help
        return Status.make(false); // You are asking for the help to be display => no error msg
      }

      case "--die": { //$NON-NLS-1$
        if (!argumentIterator.hasNext())
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));
        
        String dieString = argumentIterator.next().toUpperCase();
        if (!DIE_OPTIONS.contains(dieString)) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_DIE_WRONG, dieString));
        }

        GlobalState.die = dieString;
        break;
      }

      case "--package": { //$NON-NLS-1$
        if (!argumentIterator.hasNext())
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));
        
        String packageString = argumentIterator.next().toUpperCase();
        if (!DIE_PACKAGE_OPTIONS.contains(packageString)) {
          return Status.make(false, Messages.get(MessageEnum.ARG_ERROR_PACKAGE_WRONG));
        }

        GlobalState.diePackage = packageString;
        break;
      }

      case "--bootmode": { //$NON-NLS-1$
        if (!argumentIterator.hasNext())
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));

        Integer bootmodeInput;
        var inputString = argumentIterator.next();

        try {
          bootmodeInput = Integer.parseInt(inputString);
        } catch (NumberFormatException e) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_BOOT_NOT_INT, inputString));
        }

        GlobalState.bootMode = BootMode.fromInt(bootmodeInput);
        if (GlobalState.bootMode == null) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_BOOT_WRONG_INT, inputString));
        }

        break;
      }

      
      case "--encrypt": { //$NON-NLS-1$
        GlobalState.encrypt = true;
        break;
      }
      
      
      case "--dryrun": { //$NON-NLS-1$
        GlobalState.dryRun = true;
        break;
      }
      
      
      case "--usk": { //$NON-NLS-1$
        if (!argumentIterator.hasNext())
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));

        GlobalState.usk = Utils.strip0x(argumentIterator.next());
        break;
      }
      
      
      case "--snvm_page": { //$NON-NLS-1$
        if (!argumentIterator.hasNext())
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));

        var inputString = argumentIterator.next();
        try {
          GlobalState.startSNVMPage = Integer.parseInt(inputString);
        } catch (NumberFormatException e) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_SNVM_PAGE_WRONG_INT, inputString, Payload.SNVM_PAGES-1));
        }
        break;
      }

      
      case "--keys": { //$NON-NLS-1$
        final String keysOptions = Stream.of(ProvidingKey.values()).map(ProvidingKey::getValue).collect(Collectors.joining(", "));
        
        if (!argumentIterator.hasNext()) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_KEY_WRONG, "", keysOptions));
        }

        var keysModeString = argumentIterator.next();
        GlobalState.providingKey = ProvidingKey.fromString(keysModeString);
        
        if (null == GlobalState.providingKey) {
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_KEY_WRONG, keysModeString, keysOptions));          
        }
        
        switch (GlobalState.providingKey) {
          case GENERATE:
            // no extra arguments needed
            break;  

            
//          case ENCRYPTED:
//            GlobalState.keyEncrypted = true; 
//            // intentionally not breaking as encrypted and separated are the same
            
          case SEPARATED:
            // Parse private key
            if (!argumentIterator.hasNext())
              return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));
            
            var privateFileString = argumentIterator.next();
            GlobalState.privateKeyPath = parseKeyFileStringToPath(privateFileString);
            if (null == GlobalState.privateKeyPath)            
              return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_KEY_FILE_NOT_EXIST, privateFileString));                
            
            // Parse public key            
            if (!argumentIterator.hasNext())
              return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));
            
            var publicFileString = argumentIterator.next();
            GlobalState.publicKeyPath = parseKeyFileStringToPath(publicFileString);
            if (null == GlobalState.privateKeyPath)            
              return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_KEY_FILE_NOT_EXIST, publicFileString));
            
            break;

            
//          case COMBINED:
//            // Parse combined key
//            if (!argumentIterator.hasNext())
//              return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_AGUMENTS_MORE));
//            
//            var combinedFileString = argumentIterator.next();
//            GlobalState.privateKeyPath = parseKeyFileStringToPath(combinedFileString);
//            if (null == GlobalState.privateKeyPath)            
//              return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_KEY_FILE_NOT_EXIST, combinedFileString));                
//            
//            break;
            
            
          default:
            break;        
        }
        
        
      }

      
      case "--verify": {  //$NON-NLS-1$
        GlobalState.verify = true;
        break;
      }
      
      
      default:
        if (currentArgument.startsWith("-")) { //$NON-NLS-1$
          // wrong argument, exit with help
          return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_INVALID_ARG, currentArgument));
        }

        if (onlyOneFile) {
          try {
            GlobalState.elfFile = Path.of(currentArgument);            
          } catch (Exception e) {
            return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_FILE_NOT_A_FILE, currentArgument));
          }
          
          // TODO: Better handle relative and absolute paths at the same time
          if (GlobalState.elfFile == null)
            return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_FILE_NOT_A_FILE, currentArgument));

          onlyOneFile = false; // do not allow another (second) file to be listed in the arguments
        } else {
          // when the file was set already yet I provided something else -> exit with help
          // message
          return Status.make(false, Messages.get(MessageEnum.ARG_ERROR_TOO_MANY_FILES));
        }

      }
    }

    return Status.make(true); // Everything parsed correctly
  }

  
  public static Status validateWorkDirectory() {
    // --------- Work directory checks --------- 
    if (!Files.exists(GlobalState.workDirectory)) {
      return Status.make(false,
          Messages.format(MessageEnum.VALIDATE_ERROR_WORK_DIRECTORY_DOESNT_EXISTS, GlobalState.workDirectory.toString()));
    }
    
    return Status.make(true); // Everything validated correctly continue
  }
  
  
  public static Status validateRest() {    
    // --------- USK checks (BM2 related) --------- 
    if (null != GlobalState.usk && BootMode.USER_SECURE_SNVM != GlobalState.bootMode) {
      // If the USK set then only BM2 should be used
      return Status.make(false, Messages.get(MessageEnum.VALIDATE_USK_NOT_NEEDED));
    }

    if (BootMode.USER_SECURE_SNVM == GlobalState.bootMode) {
      var nonceHex = Utils.random24HexCharacters();
      
      if (null == GlobalState.usk) {
    	GlobalState.usk = "0";
      }
      
      try {
        // Just trying to convert it from hex nibbles, just to see if there will be a problem
        var currentUskValue = new BigInteger(GlobalState.usk, 16);
        
        if (currentUskValue.equals(BigInteger.ZERO)) {
          // When zero USK given, use a generated one
          Log.warn(Messages.format(MessageEnum.VALIDATE_USK_ZERO, nonceHex));
          GlobalState.usk     = nonceHex;
          GlobalState.saveUsk = true;      // Set a flag to save the key to a file later, when the destination directory will be prepared etc...
        }
      } catch (Exception e) {
        return Status.make(false, Messages.format(MessageEnum.VALIDATE_USK_NOT_VALID, GlobalState.usk));      
      }
      
      if (24 != GlobalState.usk.length()) {
        // Do the length check after it got a chance to be generated (if 0 was given, it was not required to give 000000000000000000000000)
        return Status.make(false, Messages.format(MessageEnum.VALIDATE_USK_NOT_VALID, GlobalState.usk));
      }      
    }
    
    // --------- SNVM PAGE checks (BM2 related) ------------
    if (0 != GlobalState.startSNVMPage && BootMode.USER_SECURE_SNVM != GlobalState.bootMode) {
      // If the SNVM PAGE set then only BM2 should be used
      return Status.make(false, Messages.get(MessageEnum.VALIDATE_SNVP_PAGE_NOT_NEEDED));
    }
    
    if (BootMode.USER_SECURE_SNVM == GlobalState.bootMode) {
      if ( (GlobalState.startSNVMPage < 0) || (GlobalState.startSNVMPage >= Payload.SNVM_PAGES) )
        return Status.make(false, Messages.format(MessageEnum.ARG_ERROR_SNVM_PAGE_WRONG_INT, Integer.toString(GlobalState.startSNVMPage), Payload.SNVM_PAGES-1));
    }

    // --------- ENCRYPT BM2 check -----------------------
    if (BootMode.USER_SECURE_SNVM != GlobalState.bootMode && GlobalState.encrypt) {
      return Status.make(false, Messages.format(MessageEnum.VALIDATE_ENCRYPT_IN_WRONG_BM));      
    }
    
    if (BootMode.FACTORY_SECURE_ENVM != GlobalState.bootMode && ProvidingKey.GENERATE != GlobalState.providingKey) {
      // If we are not in the BM3 then do not allow to separate and provide keys 
      
      return Status.make(false, Messages.format(MessageEnum.VALIDATE_ONLY_BM3_KEYS));      
    }
    
//    if (ProvidingKey.ENCRYPTED == GlobalState.providingKey) {      
//      GlobalState.keySecret = Env.getEcdsaPrivKeyPass();
//    }
    
    
    // --------- ELF file detection --------- 
    if (BootMode.IDLE != GlobalState.bootMode) {
      // Bootmode 0 (IDLE) doesn't need ELF file, all other modes do require it
      
      // If a file is set specifically then do not allow absolute paths
      if (GlobalState.elfFile != null && GlobalState.elfFile.isAbsolute()) {
        return Status.make(false, Messages.format(MessageEnum.VALIDATE_ERROR_ELF_ABSOLUTE, GlobalState.elfFile));        
      }
      
      if (GlobalState.elfFile == null) {
        // File was not set, let's auto-detect first ELF file in the work directory  
        try {
          Log.debug(Messages.format(MessageEnum.VALIDATE_GOING_TO_DETECT_ELF, GlobalState.workDirectory));            
                  
          GlobalState.elfFile = Path.of(
              Arrays.stream(new File(GlobalState.workDirectory.toString()).listFiles())
              .filter((file) -> file.isFile() && file.toString().endsWith(".elf"))                                               //$NON-NLS-1$
              .findFirst().orElse(null).getName());
          
          Log.debug(Messages.format(MessageEnum.VALIDATE_ELF_FILE_FOUND, GlobalState.elfFile.toString()));
          
        } catch (Exception e) {
          return Status.make(false, Messages.format(MessageEnum.VALIDATE_ERROR_WORKDIR_WALK_EXCEPTION,
              GlobalState.workDirectory.toString()));
        }
  
        if (GlobalState.elfFile == null ) {
          // Still null? Then the file is not found
          return Status.make(false,
              Messages.format(MessageEnum.VALIDATE_ERROR_CANT_FIND_ELF_IN_WORKDIR, GlobalState.workDirectory.toString()));
        }
      }

      
      var elfFullPath = GlobalState.workDirectory + "/" + GlobalState.elfFile;
  
      try {
        if (!Files.exists(Path.of(elfFullPath))) {
          return Status.make(false, Messages.format(MessageEnum.VALIDATE_ERROR_ELF_DOESNT_EXISTS, elfFullPath));
        }        
      } catch (Exception e) {
        return Status.make(false, Messages.format(MessageEnum.VALIDATE_ERROR_ELF_DOESNT_EXISTS, elfFullPath));        
      }
      
    }

    return Status.make(true); // Everything validated correctly continue
  }

  
}
