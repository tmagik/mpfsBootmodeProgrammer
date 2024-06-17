//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.main;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.microchip.sc.mpfsbootmodeprogrammer.utils.Log;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.MessageEnum;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Messages;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Status;
import com.microchip.sc.mpfsbootmodeprogrammer.utils.Utils;

public class Payload {
  
  
  static byte[] payloadDigest;
  
  // 128KiB of eNVM (boot mode 1 & 3)
  public  final static long ENVM_LOW       = 0x20220000;
  public  final static long ENVM_HIGH      = 0x2023ffff;
  public  final static long ENVM_SIZE      = (ENVM_HIGH - ENVM_LOW) + 1; 
  public  final static long ENVM_PAGE_SIZE = 256;
  
  // 1920KiB of Local Integrated Memory/LIM (boot mode 2)
  public  final static long LIM_LOW        = 0x08000000;
  public  final static long LIM_HIGH       = 0x081dffff;
  public  final static long LIM_SIZE       = (LIM_HIGH - LIM_LOW) + 1;  // The size might be irrelevant for the BM2 because SNVM_SIZE is more restricting
  
  // 55692bytes / 52156bytes of sNVM (boot mode 2)
  public  final static long SNVM_PAGE_SIZE_PLAIN         = 252;   // Only plain text mode is using this size
  public  final static long SNVM_PAGE_SIZE_AUTHENTICATED = 236;   // Both Authenticated plain text and Authenticated cipher text are using this size
  public  final static long SNVM_PAGES                   = 221;
  public  final static long SNVM_SIZE_PLAIN              = SNVM_PAGES * SNVM_PAGE_SIZE_PLAIN;
  public  final static long SNVM_SIZE_AUTHENTICATED      = SNVM_PAGES * SNVM_PAGE_SIZE_AUTHENTICATED;
  
  public  final static int  SBIC_SIZE = 208;
  public  final static int  UBLI_SIZE = 60;
  
  
  static public Status makeDummyHexFile(String filename) {
    var file = new File(filename + ".hex");                                             //$NON-NLS-1$
    
    try {
      
      if (!file.exists())
        if (!file.createNewFile()) return Status.make(false, Messages.format(MessageEnum.PAYLOAD_DUMMY_FAILED, file));
      
      var writer = Files.newBufferedWriter(file.toPath(), StandardOpenOption.TRUNCATE_EXISTING);
      writer.write(":0100000000FF");                                                   //$NON-NLS-1$
      writer.close();
      
      // Everything worked as expected, return with success
      return Status.make(true);
      
    } catch (IOException e1) {
      // Just catching the exceptions so it will get propagated to the top, but anything below here is 
      // failure anyway so we do not need to return failure message straight away here as it will 
      // happen few lines below anyway.
    }

    return Status.make(false, Messages.format(MessageEnum.PAYLOAD_DUMMY_FAILED, file));
  }
  
  
  static public String bootcfgBootmode1() {
      var reset = GlobalState.bootVector;
      
      // Reset vector for each hart (20bytes, 40 HEX characters)
      // The order of bytes/fields might not be as it would seem naturally, but because at this
      // moment all of the are the same boot vector it doesn't matter that much
      return String.format("%08X%08X%08X%08X%08X", reset, reset, reset, reset, reset);    //$NON-NLS-1$
  }
  
  
  static public String bootcfgBootmode2(int startPage, String usk) {
    // The order of bytes/fields might not be as it would seem naturally
    //  0  1  U_MSS_BOOT_SNVM_PAGE  Start page in SNVM
    //  1  3  RESERVED              For alignment
    //  4 12  U_MSS_BOOT_SNVM_USK   For authenticated/encrypted pages
    //  4 bytes padding
    
    return String.format("%08X%s%06X%02X", 0, usk, 0, startPage);    //$NON-NLS-1$;
  }
  
  
  static public String bootcfgBootmode2() {
    return bootcfgBootmode2(0, "000000000000000000000000");             //$NON-NLS-1$
  }

  
  static public String bootcfgBootmode3(Integer sbicAddr, Integer revocation) {
    // The order of bytes/fields might not be as it would seem naturally
    //  0 4 U_MSS_SBIC_ADDR Address of SBIC in MSS address space
    //  4 4 U_MSS_REVOCATION_ENABLE Enable SBIC revocation if non-zero
    //  12 bytes padding

    return String.format("%024X%08X%08X", 0, revocation, sbicAddr);   //$NON-NLS-1$;
  }

  
  static public String bootcfgBootmode3(Long sbicAddr) {
    return bootcfgBootmode3(sbicAddr.intValue(), 0);
  }

  
  static public Long getSbicBeforeImageAddress() {
    Long sbicStartPage    = ((GlobalState.bootVector - ENVM_LOW) / ENVM_PAGE_SIZE) - 1;
    Long sbicStartAddress = (sbicStartPage * ENVM_PAGE_SIZE) + ENVM_LOW;

    Log.debug(Messages.format(MessageEnum.PAYLOAD_DEBUG_SBIC_PAGE_BEFORE_IMAGE, GlobalState.bootVector, sbicStartPage));
    Log.debug(Messages.format(MessageEnum.PAYLOAD_DEBUG_SBIC_ADDR_BEFORE_IMAGE, sbicStartPage, sbicStartAddress));
    
    return sbicStartAddress;
  }
   

  static private Status shaDigest(byte[] content, String algorithm) {
    // update the payloadDigest variable with the SHA-256 and SHA-384 digest
    MessageDigest digest;
    try {
      digest        = MessageDigest.getInstance(algorithm);
      payloadDigest = digest.digest(content);
      return Status.make(true);
      
    } catch (NoSuchAlgorithmException e) {
      return Status.make(false, Messages.format(MessageEnum.PAYLOAD_SHA_FAILED, algorithm));
    }
  }

  
  static private Status sha256(byte[] content) {
    return shaDigest(content, "SHA-256");                              //$NON-NLS-1$
  }
  
  
  static private Status sha384(byte[] content) {
    return shaDigest(content, "SHA-384");                              //$NON-NLS-1$
  } 
  
  
  static public Status prependUbliToImage(String inputImage, String outputImage) {
    // Take input BIN file and generate a new BIN file with prepended UBLI header
    
    // User Boot Loader Image Format (UBLI)
    //  0     4            IMAGEADDR Target MSS address for IMAGE 
    //  4     4            IMAGELEN  Size of IMAGE in 32b words
    //  8     4            BOOTVEC0  Boot vector for E51
    // 12     4            BOOTVEC1  Boot vector for U541
    // 16     4            BOOTVEC2  Boot vector for U542
    // 20     4            BOOTVEC3  Boot vector for U543
    // 24     4            BOOTVEC4  Boot vector for U544
    // 28    32            H         Image hash
    // 60    <IMAGELEN>*4  IMAGE     User boot loader image binary
    // Total <IMAGELEN>*4+60 Bytes    
    
    var resetVector = GlobalState.bootVector.intValue();
    
    try (
      var fis = new FileInputStream(inputImage + ".bin");                                                             //$NON-NLS-1$
      var dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputImage + ".bin")));           //$NON-NLS-1$
    ) {
      var theRawImagePayload = fis.readAllBytes();
      var shaStatus = sha256(theRawImagePayload);                                 // the payloadDigest variable will contain the SHA-256
      if (!shaStatus.isFinishedCorrectly()) return shaStatus;

      dos.writeInt(Integer.reverseBytes(resetVector));                            // Target address
      dos.writeInt(Integer.reverseBytes(GlobalState.getSize32bitWordsAligned())); // Size of the image in 32-bit words
      dos.writeInt(Integer.reverseBytes(resetVector));                            // e51
      dos.writeInt(Integer.reverseBytes(resetVector));                            // u54_1
      dos.writeInt(Integer.reverseBytes(resetVector));                            // u54_2
      dos.writeInt(Integer.reverseBytes(resetVector));                            // u54_3
      dos.writeInt(Integer.reverseBytes(resetVector));                            // u54_4
      dos.write(payloadDigest);                                                   // Just add the hash as it is, byte by byte      
      dos.write(theRawImagePayload);                                              // Add whole raw content of the original BIN image file 
            
    } catch (IOException | NullPointerException e) {
      return Status.make(false, Messages.get(MessageEnum.PAYLOAD_BOOT2_UBLI_FAILED));
    }
      
    return Status.make(true);
  }
  
  
  static Status saveUskKey(String basename, String key) {
    var out = Utils.outputPath(basename);
    
    try {
      var writer = new BufferedWriter(new FileWriter(out, StandardCharsets.UTF_8));
      writer.write(key);      
      writer.close();
    } catch (Exception e) {
      return Status.make(false, Messages.format(MessageEnum.PAYLOAD_USK_SAVE_FAILED, out));
    }
    
    return Status.make(true);
  }
  

  
  static Status savePublicXYKey(String basename, BigInteger x, BigInteger y) {
    var out = Utils.outputPath(basename);
    
    try {
      var writer = new BufferedWriter(new FileWriter(out, StandardCharsets.UTF_8));
      
      writer.write(String.format("ucskx=%s%n", Utils.addLeadingTextToECxyKey(x)));               //$NON-NLS-1$
      writer.write(String.format("ucsky=%s%n", Utils.addLeadingTextToECxyKey(y)));               //$NON-NLS-1$
      
      writer.close();
    } catch (Exception e) {
      return Status.make(false, Messages.format(MessageEnum.PAYLOAD_BOOT3_XY_SAVE_FAILED, out));
    }
    
    return Status.make(true);
  }
  
  
  static private void putIntIntoBytes(byte[] bytes, int offset, int value) {
    var intArray = ByteBuffer.allocate(4).putInt(Integer.reverseBytes(value)).array();  // Reverse the byte order
//    var intArray = ByteBuffer.allocate(4).putInt(value).array();                          // Do not reverse byte order
    System.arraycopy(intArray, 0, bytes, offset, 4);
  }
  
  
  static public Status createFakeSBIC(String inputImage, String outputImage) {
    //    0     4   TRAMPOLINE  Jump to BOOTVEC0
    //    4     4   IMAGELEN  Size in bytes of program
    //    8     4   BOOTVEC0  Boot vector in UBL for E51
    //    12    4   BOOTVEC1  Boot vector in UBL for U541
    //    16    4   BOOTVEC2  Boot vector in UBL for U542
    //    20    4   BOOTVEC3  Boot vector in UBL for U543
    //    24    4   BOOTVEC4  Boot vector in UBL for U544
    //    28    1   RESERVED  Zeroized
    //    29    3   RESERVED  Zeroized
    //    32    8   RESERVED  Zeroized
    //    40    16  RESERVED  Zeroized
    //    56    48  RESERVED  Zeroized
    //    104   104 RESERVED  Zeroized
    //    208   4   ILLEGALINSTR  0xFFFFFFFF
    //    212   24  IDSTRING  “BOOT MODE 1 DUMMY SBIC\0\0”
    //    236   4   ILLEGALINSTR  0xFFFFFFFF
    //    240   16  PADDING Padding 0s to fill a single eNVM page
    //    Total 256    
    
    var resetVector = GlobalState.bootVector.intValue();
    
    try (
      var fis = new FileInputStream(inputImage + ".bin");                                                           //$NON-NLS-1$
      var dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputImage + ".bin")));         //$NON-NLS-1$
    ) {
      byte[] sbicHeader = new byte[256];                         // 
      putIntIntoBytes(sbicHeader, 0,  getTrampolineOpCode());
      putIntIntoBytes(sbicHeader, 4,  GlobalState.size);
      putIntIntoBytes(sbicHeader, 8,  resetVector);
      putIntIntoBytes(sbicHeader, 12, resetVector);
      putIntIntoBytes(sbicHeader, 16, resetVector);
      putIntIntoBytes(sbicHeader, 20, resetVector);
      putIntIntoBytes(sbicHeader, 24, resetVector);
      
      // 24 - 208 zeros, not doing anything as the array is zeroized
      
      // Invalid instruction OPcode
      putIntIntoBytes(sbicHeader, 208, 0xffffffff);
      
      // Adding hex values for the string: “BOOT MODE 1 DUMMY SBIC\0\0” 
      putIntIntoBytes(sbicHeader, 212, 0x544F4F42);
      putIntIntoBytes(sbicHeader, 216, 0x444F4D20);
      putIntIntoBytes(sbicHeader, 220, 0x20312045);
      putIntIntoBytes(sbicHeader, 224, 0x4D4D5544);
      putIntIntoBytes(sbicHeader, 228, 0x42532059);
      putIntIntoBytes(sbicHeader, 232, 0x00004349);      
      
      // Invalid instruction OPcode
      putIntIntoBytes(sbicHeader, 236, 0xffffffff);

      // 204 - 256 padding to fill a single page, however nothing to do as the array is zeroized anyway
      
      dos.write(sbicHeader);                           // Output into the header bytes into the file
     
    } catch (IOException | NullPointerException e) {
      return Status.make(false, Messages.get(MessageEnum.PAYLOAD_BOOT3_SBIC_FAILED));
    }
      
    return Status.make(true);
  }

  
  static public Status createSBIC(String inputImage, String outputImage) {
    //    0   4   IMAGEADDR     Address of UBL in MSS memory map
    //    4   4   IMAGELEN      Size of UBL in bytes
    //    8   4   BOOTVEC0      Boot vector in UBL for E51
    //    12  4   BOOTVEC1      Boot vector in UBL for U541
    //    16  4   BOOTVEC2      Boot vector in UBL for U542
    //    20  4   BOOTVEC3      Boot vector in UBL for U543
    //    24  4   BOOTVEC4      Boot vector in UBL for U544
    //    28  1   OPTIONS[7:0]  SBIC options
    //    29  3   RESERVED      For alignment and future use
    //    32  8   VERSION       SBIC/Image version 
    //    40  16  DSN           Optional DSN binding
    //    56  48  H             UBL image SHA-384 hash 
    //    104 104 CODESIG       DER-encoded ECDSA signature
    //    Total 208 Bytes 

    
    var resetVector = GlobalState.bootVector.intValue();
    
    try (
      var fis = new FileInputStream(inputImage + ".bin");                                                           //$NON-NLS-1$
      var dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputImage + ".bin")));         //$NON-NLS-1$
    ) {
      var theRawImagePayload = fis.readAllBytes();
      var shaStatus = sha384(theRawImagePayload);                // the payloadDigest variable will contain the SHA-384
      if (!shaStatus.isFinishedCorrectly()) return shaStatus;
      
      byte[] sbicHeader = new byte[104];
      putIntIntoBytes(sbicHeader, 0,  resetVector);
      putIntIntoBytes(sbicHeader, 4,  GlobalState.size);
      putIntIntoBytes(sbicHeader, 8,  resetVector);
      putIntIntoBytes(sbicHeader, 12, resetVector);
      putIntIntoBytes(sbicHeader, 16, resetVector);
      putIntIntoBytes(sbicHeader, 20, resetVector);
      putIntIntoBytes(sbicHeader, 24, resetVector);
      // SBIC, RESERVED, VERSION, DSN zeroes, expect these to be set when the array was constructed
      
      // Adding the 48 byte long digest into the header at the offset 56
      // this will make the CODESIG contain the IMAGE's digest 
      System.arraycopy(payloadDigest, 0, sbicHeader, 56, 48);  

      dos.write(sbicHeader);                           // Output into the header bytes into the file
     
      var status = GlobalState.ecdsa.sign(sbicHeader);
      if (!status.isFinishedCorrectly()) return status;
      
      dos.write(GlobalState.ecdsa.signature);          // Output into the CODESIG bytes into the file
      
      // If the signature is smaller than 104 bytes, then zero pad the end
      for (int currentSize = GlobalState.ecdsa.signature.length; currentSize < 104; currentSize++) {
        dos.writeByte(0);
      }
           
    } catch (IOException | NullPointerException e) {
      return Status.make(false, Messages.get(MessageEnum.PAYLOAD_BOOT3_SBIC_FAILED));
    }
      
    return Status.make(true);
  }
  
  
  static public Status combineSbicWithImage(String inputImage, String inputSbic, String outputImage, long sbicSize) {
    try (
      var fis = new FileInputStream(inputImage + ".bin");                                                       //$NON-NLS-1$
      var fss = new FileInputStream(inputSbic  + ".bin");                                                       //$NON-NLS-1$
      var dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputImage + ".bin")));     //$NON-NLS-1$
    ) {
      var imagePayload = fis.readAllBytes();
      var sbicPaylod   = fss.readAllBytes();
      
      dos.write(sbicPaylod);

      // The difference between where SBIC ends and image starts needs to be padded
      var start = sbicSize;
      var end   = ENVM_PAGE_SIZE;      
      for (long i=start; i<end; i++) {
        dos.writeByte(0);
      }
      
      dos.write(imagePayload); 
            
    } catch (IOException | NullPointerException e) {
      return Status.make(false, Messages.format(MessageEnum.PAYLOAD_BOOT3_IMAGE_SBIC_COMBINING_FAILED));
    }
      
    return Status.make(true);
  }

  
  static public Status combineSbicWithImage(String inputImage, String inputSbic, String outputImage) {
    return combineSbicWithImage(inputImage, inputSbic, outputImage, SBIC_SIZE);
  }

  
  static public int getCombinedSbicAndImageSize() {
    var start = (getEnvmPage(getSbicBeforeImageAddress()) * ENVM_PAGE_SIZE); // SBIC will start one page before the application image
    var end   = (getEnvmPage(GlobalState.bootVector)      * ENVM_PAGE_SIZE) + GlobalState.size;
    
    return (int)(end - start); 
  }
  
  
  static public Status envmSanityCheck() {
    
    // Check if the boot vector is within a valid eNVM range and the size is not too big
    if ((GlobalState.bootVector < ENVM_LOW) || (GlobalState.bootVector > ENVM_HIGH))
      return Status.make(false, 
          Messages.format(MessageEnum.PAYLOAD_ENVM_WRONG_BOOTVECTOR, ENVM_LOW, ENVM_HIGH, GlobalState.elfFile, GlobalState.bootVector));

    // Imageaddr (assuming is our bootvector) needs to be aligned to beginning of a eVNM page
    if (0 != (GlobalState.bootVector % ENVM_PAGE_SIZE)) {
      return Status.make(false, 
          Messages.format(MessageEnum.PAYLOAD_ENVM_IMAGEADDR_NOT_ALIGNED, GlobalState.bootVector));
    }
    
    // And check if the size is not too big for eNVM
    if (GlobalState.bootVector + GlobalState.size > ENVM_HIGH)
      return Status.make(false, 
          Messages.format(
              MessageEnum.PAYLOAD_ENVM_TOO_BIG,
                  
              ENVM_SIZE, 
              GlobalState.elfFile, 
              GlobalState.size, 
              (GlobalState.bootVector - ENVM_LOW) / ENVM_PAGE_SIZE, 
              GlobalState.bootVector - ENVM_LOW
      ));
    
    return Status.make(true);
  }

  
  static public Status snvmAndLimSanityCheck() {
    // Check if the boot vector is within a valid LIM range and the size is not too big
    if ((GlobalState.bootVector < LIM_LOW) || (GlobalState.bootVector > LIM_HIGH))
      return Status.make(false, 
          Messages.format(MessageEnum.PAYLOAD_BOOT2_WRONG_BOOTVECTOR, LIM_LOW, LIM_HIGH, GlobalState.elfFile, GlobalState.bootVector));
    
    // Imageaddr (assuming is our bootvector) needs to be aligned to 32-bit address
    if (0 != (GlobalState.bootVector % 4)) {
      return Status.make(false, 
          Messages.format(MessageEnum.PAYLOAD_BOOT2_IMAGEADDR_NOT_ALIGNED, GlobalState.bootVector));
    }

    // Image length needs to be aligned to 32-bit address
    if (0 != (GlobalState.size % 4)) {
      return Status.make(false, 
          Messages.format(MessageEnum.PAYLOAD_BOOT2_IMAGEADDR_NOT_ALIGNED, GlobalState.size));
    }

    if ( (GlobalState.bootVector + GlobalState.size) > LIM_HIGH) {
      return Status.make(false, 
          Messages.format(MessageEnum.PAYLOAD_BOOT2_LIM_NOT_FIT, GlobalState.bootVector, GlobalState.size, LIM_HIGH));      
    }
    
    // And check if the size of the image + the overhead is not too big for SNVM
    //      i.  Size-of-available-sNVM in plain-text mode: (221 - start_page) * 252 bytes
    //      ii. Size-of-available-sNVM in authenticated or encrypted+authenticated mode: (221 - start_page) * 236 bytes
    if (((GlobalState.startSNVMPage * SNVM_PAGE_SIZE_AUTHENTICATED) + GlobalState.size + 60) > SNVM_SIZE_AUTHENTICATED)
      return Status.make(false, 
          Messages.format(
              MessageEnum.PAYLOAD_BOOT2_SNVM_TOO_BIG,
              
              SNVM_SIZE_AUTHENTICATED, 
              GlobalState.elfFile, 
              GlobalState.size, 
              GlobalState.startSNVMPage,
              GlobalState.startSNVMPage * SNVM_PAGE_SIZE_AUTHENTICATED
      ));
    
    return Status.make(true);
  }
  
  
  static public int getTrampolineOpCode() {
    // TODO: Implement full OP code generation to allow us different jumps, not just to the next page.
    return 0x1000006F;
  }
  
  
  static public Status getBinarySize(String basename) {
    var fileName = basename + ".bin";                                                                                                       //$NON-NLS-1$
    
    try {
      GlobalState.size = (int) new File(fileName).length();
      Log.debug(Messages.format(MessageEnum.PAYLOAD_DEBUG_BIN_SIZE , GlobalState.size, GlobalState.size)); 
    }
    catch (Exception e) {
      return Status.make(false, Messages.format(MessageEnum.PAYLOAD_GET_SIZE_FAILED, fileName));
    }
    
    return Status.make(true);
  }

  
  static public long getEnvmPage(long absoluteAddress) {
    // Do not round up as we are calculating the start page index, not the size
    return (absoluteAddress - ENVM_LOW) / ENVM_PAGE_SIZE;    
  }

  
  static public long getLimPage(long absoluteAddress) {
    // Do not round up as we are calculating the start page index, not the size
    return (absoluteAddress - LIM_LOW) / ENVM_PAGE_SIZE;    
  }
  
}

