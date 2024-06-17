//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.main;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import com.microchip.sc.mpfsbootmodeprogrammer.utils.Env;

public enum ProvidingKey {

  
  //@formatter:off
  GENERATE("GENERATE",   "No key files are needed, both private and public keys will be generated (default mode if none is selected)."),                                                               //$NON-NLS-1$ //$NON-NLS-2$
  SEPARATED("SEPARATED", "1st argument is private key file and 2nd argument is the public key file.")                                                                                                 //$NON-NLS-1$ //$NON-NLS-2$
//  ENCRYPTED("ENCRYPTED", "1st argument is the encrypted private key file (" + Env.ENV_KEY_PASSWORD_KEY + " environment variable must contain the password) and 2nd argument is the public key file.") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//  COMBINED( "COMBINED",  "One argument, file containing a private/public key pair.");                                                                                                                  //$NON-NLS-1$ //$NON-NLS-2$
  ;
  //@formatter:on

  
  // Will hold the bootmode number and its description for all possible options
  public final static Map<String, ProvidingKey> map = Stream.of(ProvidingKey.values())
      .collect(Collectors.toMap(ProvidingKey::getValue, thisEnum -> thisEnum));

  
  private final String value;
  private final String description;

  
  private ProvidingKey(String value, String description) {
    this.value = value;
    this.description = description;
  }

  
  public String getValue() {
    return value;
  }

  
  public String getDescription() {
    return description;
  }

  
  // Allow to convert to this enum just by providing a String
  public static ProvidingKey fromString(String inputValue) {
    // If we have a enum under given inputNumber, then return the enum itself
    if (map.containsKey(inputValue))
      return map.get(inputValue);

    // Just return null for all other cases where the input is not valid
    return null;
  }
  
  
  public String getValueAndDescription() {
    return this.value + " - " + this.description;                                 //$NON-NLS-1$
  }

  
  public String getValueAndDescriptionPadded() {
    int maxLen      = Stream.of(ProvidingKey.values()).map(val -> val.getValue().length()).reduce(Integer::max).get();
    var paddedValue = this.value + " ".repeat(maxLen - this.value.length());      //$NON-NLS-1$
    
    return paddedValue + " - " + this.description;                                //$NON-NLS-1$
  }
  
  
}
