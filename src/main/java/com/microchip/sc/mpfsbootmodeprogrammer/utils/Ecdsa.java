//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.utils;

//import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

//import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
//import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.microchip.sc.mpfsbootmodeprogrammer.main.GlobalState;
//import org.bouncycastle.openssl.PEMKeyPair;
//import org.bouncycastle.openssl.PEMParser;
//import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
//import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
//import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

//import com.microchip.sc.mpfsbootmodeprogrammer.main.GlobalState;

public class Ecdsa {
  
  public static final boolean VERBOSE_DEBUG       = false;

  public  PrivateKey    privateKey;
//  private char[]        privateKeyPasswordArray = "".toCharArray();  // Use empty password if no specified //$NON-NLS-1$
  
  public  PublicKey     publicKey;
  public  BigInteger    x;
  public  BigInteger    y;
   
  public byte[] signature;
  
  private final static String KEY_CURVE_NAME      = "secp384r1";               //$NON-NLS-1$
  private final static String SIGN_ALGORITHM      = "SHA384withECDSA";         //$NON-NLS-1$
  
  public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

  
//  private final static String publicPem = "-----BEGIN PUBLIC KEY-----\r\n"     //$NON-NLS-1$
//      + "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAENIssNsD47M4WpXtJj5Iggk83Dc01tYO8\r\n" //$NON-NLS-1$
//      + "6lKDd1zbmW2itRR4VVTLMsFGkujElCSOq9UHFM4jJjFhmGVEkquJ6J4kw7fCDY/x\r\n" //$NON-NLS-1$
//      + "JZ3SoW4d6Egqt49aCivXGF4t7hrgxV5L\r\n"                                 //$NON-NLS-1$
//      + "-----END PUBLIC KEY-----\r\n";                                        //$NON-NLS-1$
//  
//  
//  private final static String privatePem = "-----BEGIN PRIVATE KEY-----\r\n"   //$NON-NLS-1$
//      + "MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDBBdsy5smSA2+DvnIdx\r\n" //$NON-NLS-1$
//      + "bqq6GwwHadEdtHqcKO9N8hB0/NKvFOCmHBSfBpYPgCWlybGhZANiAAQ0iyw2wPjs\r\n" //$NON-NLS-1$
//      + "zhale0mPkiCCTzcNzTW1g7zqUoN3XNuZbaK1FHhVVMsywUaS6MSUJI6r1QcUziMm\r\n" //$NON-NLS-1$
//      + "MWGYZUSSq4noniTDt8INj/ElndKhbh3oSCq3j1oKK9cYXi3uGuDFXks=\r\n"         //$NON-NLS-1$
//      + "-----END PRIVATE KEY-----\r\n";                                       //$NON-NLS-1$
// 

  
  public Ecdsa() {
    Security.addProvider(BOUNCY_CASTLE_PROVIDER);
  }
  
  
  public static void showSupportedCurves() {
    Log.debug(Security.getProviders("AlgorithmParameters.EC")[0]                    //$NON-NLS-1$
       .getService("AlgorithmParameters", "EC").getAttribute("SupportedCurves"));   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
  
  
  private byte[] parseKeyToBytes(String pcksString) {
    pcksString = pcksString.replaceAll("-----BEGIN ENCRYPTED PRIVATE KEY-----", ""); //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("-----BEGIN EC PRIVATE KEY-----", "");        //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("-----BEGIN PUBLIC KEY-----", "");            //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("-----BEGIN PRIVATE KEY-----", "");           //$NON-NLS-1$ //$NON-NLS-2$
    
    pcksString = pcksString.replaceAll("-----END EC PRIVATE KEY-----", "");          //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("-----END ENCRYPTED PRIVATE KEY-----", "");   //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("-----END PUBLIC KEY-----", "");              //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("-----END PRIVATE KEY-----", "");             //$NON-NLS-1$ //$NON-NLS-2$
    
    pcksString = pcksString.replaceAll("\r", "");                                    //$NON-NLS-1$ //$NON-NLS-2$
    pcksString = pcksString.replaceAll("\n", "");                                    //$NON-NLS-1$ //$NON-NLS-2$
    
    var bytes = Base64.getDecoder().decode(pcksString);
    return bytes;
  }
  
  
  private void setPublicXY(ECPublicKey pubKey) {
    var ecPoint = pubKey.getW();
    x = ecPoint.getAffineX();
    y = ecPoint.getAffineY();
    
    // Verbose debug printout of the X/Y values
    Log.debug("Public key X(hex)=0x" + Utils.addLeadingTextToECxyKey(x));                              //$NON-NLS-1$
    Log.debug("Public key Y(hex)=0x" + Utils.addLeadingTextToECxyKey(y));                              //$NON-NLS-1$
  }
  
  
  public boolean isPublicXYSmall() {
    var ecPoint = ((ECPublicKey)publicKey).getW();
    var x = ecPoint.getAffineX();
    var y = ecPoint.getAffineY();
    
    if (x.toString(16).length() != 96 || y.toString(16).length() != 96) {
      return true;
    } else {
      return false;
    }
  }
   
  
//  private void detectPass() {
//    privateKeyPasswordArray = GlobalState.keySecret.toCharArray();   
//  }
  
  
//  private void parsePrivateKey(String keyString) throws Exception {
//    var privParserObject  = new PEMParser(new StringReader(keyString)).readObject();
//    
//    if (GlobalState.keyEncrypted) {
//      detectPass();
//      var encryptedPrivKI = ((PKCS8EncryptedPrivateKeyInfo)privParserObject);
//      var privKI          = encryptedPrivKI.decryptPrivateKeyInfo(new JceOpenSSLPKCS8DecryptorProviderBuilder().build(privateKeyPasswordArray));
//      privateKey          = new JcaPEMKeyConverter().getPrivateKey(privKI);      
//    } else {
//      privateKey          = new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo)privParserObject);      
//    }
//  } 
//
//  
//  private void parsePublicKey(String keyString) throws Exception {
//    var pubParserObject  = new PEMParser(new StringReader(keyString)).readObject();
//    publicKey            = new JcaPEMKeyConverter().getPublicKey((SubjectPublicKeyInfo)pubParserObject);
//    
//    setPublicXY((ECPublicKey)publicKey);
//  } 
  
  
//  private void parseKeyPair(String keyString) throws Exception {    
//    var keysParserObject = new PEMParser (new StringReader (keyString)).readObject();
//    var converter        = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)keysParserObject);
//    
//    privateKey = converter.getPrivate();
//    publicKey  = converter.getPublic();
//    
//    setPublicXY((ECPublicKey)publicKey);
//  } 
   
  
  public Status setKeysFromFiles() {
    String publicPem  = "";
    String privatePem = "";
    
    try {
      publicPem  = Files.readString(GlobalState.publicKeyPath);
      privatePem = Files.readString(GlobalState.privateKeyPath);
    } catch (Exception e) {
      return Status.make(false, Messages.get(MessageEnum.ECDSA_KEY_READ_FAILED));
    }
    
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");                                    //$NON-NLS-1$ //$NON-NLS-2$
      
      privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(parseKeyToBytes(privatePem)));
      publicKey  = keyFactory.generatePublic( new X509EncodedKeySpec( parseKeyToBytes(publicPem)));
      
      setPublicXY((ECPublicKey)publicKey);
      
    } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException | NullPointerException e) {
      return Status.make(false, 
          Messages.get(MessageEnum.ECDSA_PARSE_KEY_FAILED) +
          Messages.get(MessageEnum.ECDSA_APPENDABLE_KEY_EXPLANATION)
      );
    }
    
    return Status.make(true); 
  }  
  

  public Status generateKeyPair() {
    try {
      var pairGenerator = KeyPairGenerator.getInstance("EC");                                       //$NON-NLS-1$
      pairGenerator.initialize(new ECGenParameterSpec(KEY_CURVE_NAME), new SecureRandom());
      
      var keypair = pairGenerator.generateKeyPair();
      
      privateKey = keypair.getPrivate();
      publicKey  = keypair.getPublic();  
             
      setPublicXY((ECPublicKey)publicKey);
      
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NullPointerException e) {
      return Status.make(false, Messages.get(MessageEnum.ECDSA_KEY_GEN_FAILED));
    }
    
    return Status.make(true);
  }
    
  
  public Status sign(byte[] content) {
    try {
      Signature ecdsaSign = Signature.getInstance(SIGN_ALGORITHM, BOUNCY_CASTLE_PROVIDER);
//      Signature ecdsaSign = Signature.getInstance(SIGN_ALGORITHM);
      ecdsaSign.initSign(privateKey, new SecureRandom());
      ecdsaSign.update(content);
      signature = ecdsaSign.sign();

    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NullPointerException e) {
      return Status.make(false, String.format(
          Messages.get(MessageEnum.ECDSA_SIGN_FAILED) +
          Messages.get(MessageEnum.ECDSA_APPENDABLE_KEY_EXPLANATION)
      ));
    }
    
    return Status.make(true);
  }
  
  
}
