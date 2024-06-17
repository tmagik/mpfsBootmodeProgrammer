//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.main;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.microchip.sc.mpfsbootmodeprogrammer.invoke.InvokeFpgenprog;
import com.microchip.sc.mpfsbootmodeprogrammer.invoke.InvokeObjCopy;
import com.microchip.sc.mpfsbootmodeprogrammer.invoke.InvokeReadElf;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.ExitException;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Utils;

public class MainClass {

  
  //@formatter:off
  public  final static String  TOOL_NAME        = "mpfsBootmodeProgrammer";   //$NON-NLS-1$
  public  final static String  TOOL_VERSION     = "3.7";                      //$NON-NLS-1$
  private final static String  DEBIG_FILENAME   = "debugLog.txt";             //$NON-NLS-1$

  public  final static boolean DEBUG_VERBOSITY  = false;
  //@formatter:on
    
  private static Collector<CharSequence, ?, String> helpIndentation6 = Collectors.joining("\r\n      "); //$NON-NLS-1$
  
  
  public static String getProductAndVersion() {
	  return Messages.get(MessageEnum.MAIN_VERSION);	  
  }
  
  
  public static void printProductAndVersion() {
    Log.info(getProductAndVersion());
  }

  
  public static String getHelpString() {
    final String exampleUsk       = Utils.random24HexCharacters();
    final String dieAllowed       = CmdArguments.DIE_OPTIONS.stream().collect(Collectors.joining("\r\n      "));          //$NON-NLS-1$
    final String dieDefault       = GlobalState.die;
    final String packageAllowed   = CmdArguments.DIE_PACKAGE_OPTIONS.stream().collect(Collectors.joining("\r\n      "));  //$NON-NLS-1$
    final String packageDefault   = GlobalState.diePackage;
    final String bmAllowed        = Stream.of(BootMode.values()).map(BootMode::getModeAndDescriptionWithEqualsSign).collect(helpIndentation6);
    final String bmDefault        = GlobalState.BOOT_MODE_DEFAULT.getModeAndDescriptionWithEqualsSign();
    
    final String ecdsaArguments   = Stream.of(ProvidingKey.values()).map(ProvidingKey::getValueAndDescriptionPadded).collect(helpIndentation6);
    final String ecdsaExplanation = Stream.of(Messages.get(MessageEnum.ECDSA_APPENDABLE_KEY_EXPLANATION).split("\r\n")).collect(helpIndentation6);
    
    return
        Messages.format(
            MessageEnum.MAIN_HELP,
			
        		TOOL_NAME,
        		dieAllowed,
        		dieDefault,
        		packageAllowed,
        		packageDefault,
        		bmAllowed,
        		bmDefault,
        		exampleUsk,
        		exampleUsk,
        		ecdsaArguments,
        		ecdsaExplanation
    		);
  }
  
  
  public static void printHelp() {
	  System.out.println(getHelpString());
  }

  
  public static void displayMessageAndExit(String message) throws ExitException {
    throw new ExitException(false, message);
  }
  
  
  public static void bm123PrepareBinSizeAndBootvector(String basename) throws ExitException {
    // Check for SC_INSTALL_DIR with the sanityCheck and then generate BIN to get the size
    Log.info(Messages.get(MessageEnum.MAIN_GENERATE_BIN));
    InvokeObjCopy.toolVersionCheck().exitOnFailure();
    InvokeObjCopy.generateBinFromElf(Utils.inputPath(basename), Utils.outputPath(basename)).exitOnFailure();
    Payload.getBinarySize(Utils.outputPath(basename)).exitOnFailure();
    
    // Get the boot vector
    InvokeReadElf.getBootVector(Utils.inputPath(basename)).exitOnFailure();    
  }
  
  
  public static void bm0Idle() throws ExitException {
    final String dummyHex = "bootmode0";                                                                                         //$NON-NLS-1$
    
    Log.info(Messages.get(MessageEnum.MAIN_PREPARE));
    GlobalState.size = 1;
    Payload.makeDummyHexFile(Utils.outputPath(dummyHex)).exitOnFailure();
    
    InvokeFpgenprog.selectBootmode(GlobalState.bootMode).exitOnFailure();
    InvokeFpgenprog.envmClient(Utils.outputPath(dummyHex)).exitOnFailure();    
  }
  
  
  public static void bm1SecureEnvm(String basename, String bmString) throws ExitException {
    final String basenameSbic     = basename + bmString + "-dummySbic";                                                          //$NON-NLS-1$
    final String basenameCombined = basename + bmString + "-p" + (Payload.getEnvmPage(GlobalState.bootVector) - 1);              //$NON-NLS-1$
        
    // Check if the boot vector and the size are correct 
    Payload.envmSanityCheck().exitOnFailure();
        
    if (Payload.getEnvmPage(GlobalState.bootVector) > 0) {
      long sbicAddress = Payload.ENVM_LOW + ((Payload.getEnvmPage(GlobalState.bootVector) - 1) * Payload.ENVM_PAGE_SIZE);   // -1 because SBIC is before the image and then * 256, to get absolute address
      
      Log.info(Messages.get(MessageEnum.MAIN_GENERATE_FAKE_SBIC));
      
      Payload.createFakeSBIC(
          Utils.outputPath(basename),                                                 
          Utils.outputPath(basenameSbic)
      ).exitOnFailure();
      
      Payload.combineSbicWithImage(
          Utils.outputPath(basename), 
          Utils.outputPath(basenameSbic), 
          Utils.outputPath(basenameCombined),
          Payload.ENVM_PAGE_SIZE                  // Size of the fake SBIC is not Payload.SBIC_SIZE, but is bigger one whole page (Payload.ENVM_PAGE_SIZE) 
      ).exitOnFailure();
      
      Log.info(Messages.get(MessageEnum.MAIN_GENERATE_HEX));
      InvokeObjCopy.generateHexFromBin(Utils.outputPath(basenameCombined), Utils.outputPath(basenameCombined), sbicAddress).exitOnFailure();
      
      Log.info(Messages.get(MessageEnum.MAIN_PREPARE));
      InvokeFpgenprog.selectBootmode(GlobalState.bootMode, Payload.bootcfgBootmode1(), null, null).exitOnFailure();
      
      InvokeFpgenprog.envmClient(
          Utils.outputPath(basenameCombined), 
          Payload.getEnvmPage(GlobalState.bootVector) - 1,   // We decrement it by one before SBIC being prepended 
          sbicAddress,
          (int)(GlobalState.size + Payload.ENVM_PAGE_SIZE)   // The size of the image is bigger by 1 page because we have SBIC before it 
      ).exitOnFailure();    
      
    } else {
      // Can't fit the SBIC before the application image, fail
      Status.make(false, Messages.get(MessageEnum.MAIN_CANNOT_FIT_SBIC_BEFORE_THE_IMAGE)).exitOnFailure();
    }    
    
  }

  
  public static void bm2SecureSnvm(String basename, String bmString) throws ExitException {
    final String basenameUbli  = basename + bmString + "-p" + GlobalState.startSNVMPage + (GlobalState.encrypt? "-ac" : "-ap");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    // If you do not want to save USK all the time, uncomment the condition which will save it only when
    // 0 usk was specified and it the tool had to auto-generate one for the user
//    if (GlobalState.saveUsk) {
    Payload.saveUskKey(basename + bmString + "-usk.txt", GlobalState.usk).exitOnFailure();                              //$NON-NLS-1$
//    }
    
    Log.warn(String.format(Messages.get(MessageEnum.MAIN_WARN_SNVM_NUKED)));

    // Check if the boot vector and the size are correct 
    Payload.snvmAndLimSanityCheck().exitOnFailure();

    // Generate the UBLI (User Boot Loader Image) bin file first
    Payload.prependUbliToImage(
        Utils.outputPath(basename),
        Utils.outputPath(basenameUbli)
    ).exitOnFailure(); 

    // Increment the size only after the UBLI was generated to not affect the UBLI header
    GlobalState.size += Payload.UBLI_SIZE; // Add 60 bytes because we will prefix a 60 byte header before the raw payload
    
    // Then convert the UBLI bin file into the hex file
    Log.info(Messages.get(MessageEnum.MAIN_GENERATE_HEX));
    InvokeObjCopy.generateHexFromBin(Utils.outputPath(basenameUbli)).exitOnFailure();            
    
    Log.info(Messages.get(MessageEnum.MAIN_PREPARE));        
    InvokeFpgenprog.selectBootmode(
        GlobalState.bootMode, 
        Payload.bootcfgBootmode2(GlobalState.startSNVMPage, GlobalState.usk), 
        null,
        null
    ).exitOnFailure();
    
    InvokeFpgenprog.snvmClient(
        Utils.outputPath(basenameUbli), 
        GlobalState.startSNVMPage, 
        GlobalState.bootVector, 
        true,
        GlobalState.encrypt
    ).exitOnFailure();   
  }
  
  
  public static void bm3SecureSnvm(String basename, String bmString) throws ExitException {
    final long   startingPage     = Payload.getEnvmPage(GlobalState.bootVector);
    final long   sbicPage         = startingPage - 1;                              // Expect the SBIC go before the start page all the time
    final String basenameSbic     = basename + bmString + "-sbic";                                                                          //$NON-NLS-1$
    final String basenameCombined = basename + bmString + "-p" + sbicPage;                                                                  //$NON-NLS-1$

    // Check if the boot vector and the size are correct 
    Payload.envmSanityCheck().exitOnFailure();
      
    if (Payload.getEnvmPage(GlobalState.bootVector) > 0) {
      long sbicAddress = Payload.ENVM_LOW + ((Payload.getEnvmPage(GlobalState.bootVector) - 1) * Payload.ENVM_PAGE_SIZE);   // -1 because SBIC is before the image and then * 256, to get absolute address

      Log.info(Messages.get(MessageEnum.MAIN_GENERATE_SBIC));
      
      if (ProvidingKey.GENERATE == GlobalState.providingKey) {
        Log.info(Messages.get(MessageEnum.MAIN_ECDSA_GENERATE));
        GlobalState.ecdsa.generateKeyPair();        
      } else {
        Log.info(Messages.format(MessageEnum.MAIN_ECDSA_SEPARATED));
        GlobalState.ecdsa.setKeysFromFiles().exitOnFailure();
      }
      
      Payload.savePublicXYKey(basename + bmString + "-public-key-xy.txt", GlobalState.ecdsa.x, GlobalState.ecdsa.y).exitOnFailure(); //$NON-NLS-1$
      //    GlobalState.ecdsa.setKeysFromFields().exitOnFailure();
  
      Payload.createSBIC(
          Utils.outputPath(basename),                                                 
          Utils.outputPath(basenameSbic)
      ).exitOnFailure();  
    
      // ---------------------------------------------
      // SBIC + padding + image => combined image
      // BOOT VECTOR (aka beginning of the image) is not at eNVM page 0 and therefore we can put SBIC in front
      
      Payload.combineSbicWithImage(
          Utils.outputPath(basename), 
          Utils.outputPath(basenameSbic), 
          Utils.outputPath(basenameCombined)
      ).exitOnFailure();
      
      Log.info(Messages.get(MessageEnum.MAIN_GENERATE_HEX)); 
      InvokeObjCopy.generateHexFromBin(Utils.outputPath(basenameCombined), Utils.outputPath(basenameCombined), sbicAddress).exitOnFailure();
      
      Log.info(Messages.get(MessageEnum.MAIN_PREPARE));    
      
      InvokeFpgenprog.selectBootmode(
          GlobalState.bootMode, 
          Payload.bootcfgBootmode3(Payload.getSbicBeforeImageAddress()),
          GlobalState.ecdsa.x,
          GlobalState.ecdsa.y
      ).exitOnFailure();

      // eNVM client for combined image
      InvokeFpgenprog.envmClient(
          Utils.outputPath(basenameCombined),
          Payload.getEnvmPage(GlobalState.bootVector) - 1,   // We are putting SBIC before the image, so we need to start one page before 
          sbicAddress,
          Payload.getCombinedSbicAndImageSize()
       ).exitOnFailure();
      
    } else {
      
      // ---------------------------------------------          
      // Image + padding + SBIC => combined image
      // Image is at beginning of eNVM, with SBIC after the image
      // This is not supported intentionally as Libero wouldn't be able 
      // to tell which layout was used for specific HEX file and as the
      // SBIC before the image is more versatile, only that is supported
      Status.make(false, Messages.get(MessageEnum.MAIN_CANNOT_FIT_SBIC_BEFORE_THE_IMAGE)).exitOnFailure();
    }    
  }
  
  
  private static void exitWithHelpOnAnyIssue(Status status) throws ExitException {
    if (!status.isFinishedCorrectly()) {
      throw new ExitException(true, status.getMessage());
    }
  }
  
  
  public static void mpfsBootModeProgrammer(String[] args) throws ExitException {
    Log.info(Messages.format(MessageEnum.MAIN_STARTED, TOOL_NAME, TOOL_VERSION)); 

    var parsingStatus = CmdArguments.parse(args);
    exitWithHelpOnAnyIssue(parsingStatus);

    // If the arguments had correct syntax, now check if work directory is OK
    var partialValidationStatus = CmdArguments.validateWorkDirectory();
    exitWithHelpOnAnyIssue(partialValidationStatus);

    // We know where we can put the logger's file, so let's make a folder ready quickly so we can configure proper logger 
    Utils.prepareOutputPath(GlobalState.workDirectory.toString());
    
    Log.configureLoggerFile(Utils.outputPath(DEBIG_FILENAME)).exitOnFailure();

    // Now do all the other validation
    var validationStatus = CmdArguments.validateRest();
    exitWithHelpOnAnyIssue(validationStatus);
    
    Log.info(
        Messages.format(MessageEnum.MAIN_WITH_PARAMS,
            GlobalState.bootMode.getModeAndDescription(), 
            GlobalState.workDirectory.toString()));  
    
    Log.debug(CmdArguments.flagsToString());
    
    
    InvokeFpgenprog.detectTheFpgenprog().exitOnFailure();
    InvokeFpgenprog.createProject().exitOnFailure();
   
    String basename = "";                                                      //$NON-NLS-1$
    String bmString = "-bm" + GlobalState.bootMode.getMode().toString();       //$NON-NLS-1$
    
    if (BootMode.IDLE != GlobalState.bootMode) {
      // Generate the BIN file, get its size and get the boot vector for Bootmode 1, 2 and 3
      basename = Utils.getBasename(GlobalState.elfFile);
      bm123PrepareBinSizeAndBootvector(basename);
    }
       
    switch (GlobalState.bootMode) {
       
      case IDLE: {
        bm0Idle();
        break;
      }
      
      case NON_SECURE_ENVM: {
        bm1SecureEnvm(basename, bmString);
        break;
      }
    
      case USER_SECURE_SNVM: {
        bm2SecureSnvm(basename, bmString);
        break;
      }      
      
      case FACTORY_SECURE_ENVM: {
        bm3SecureSnvm(basename, bmString);
        break;
      }
      
      default:
        // We shouldn't be able to reach this place as we check the validity of bootmode before
        throw new IllegalArgumentException("Unexpected value: " + GlobalState.bootMode);              //$NON-NLS-1$
    }
    
    Log.info(Messages.get(MessageEnum.MAIN_GENERATE_BISTREAM));
    InvokeFpgenprog.generateBistream().exitOnFailure();
    
    if (GlobalState.dryRun) {
      Log.info(Messages.get(MessageEnum.MAIN_DRY_RUN));
    } else {
      Log.info(Messages.get(MessageEnum.MAIN_PROGRAMMING));
      InvokeFpgenprog.runAction().exitOnFailure();
      
      if (GlobalState.verify) {
        Log.info(Messages.get(MessageEnum.MAIN_VERIFY));
        InvokeFpgenprog.verify().exitOnFailure();
      }      
    }

//  We are not removing the output directory for BM0 as useful verbose debug log will be contained there
//    if (BootMode.IDLE == GlobalState.bootMode) {
//      // For BM0 wipe the output directory after being used 
//      Utils.wipeOutputPath(GlobalState.workDirectory.toString());      
//    }  
    
    Log.info(String.format(Messages.get(MessageEnum.MAIN_COMPLETE), TOOL_NAME));
    Log.removeLoggerFile();
  }

    
  public static void main(String[] args) {
    try {
      mpfsBootModeProgrammer(args);
    } catch (ExitException e) {
      Log.error(e.getMessage());
      
      if (e.isPrintHelp()) printHelp();
    	
      Log.removeLoggerFile();
      System.exit(-1);
    }
  }

  
}
