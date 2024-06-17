//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.main;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BootMode {
  
  
  //@formatter:off
  IDLE(               0, "idle boot"),                              //$NON-NLS-1$
  NON_SECURE_ENVM(    1, "non-secure boot from eNVM"),              //$NON-NLS-1$
  USER_SECURE_SNVM(   2, "user secure boot from sNVM"),             //$NON-NLS-1$
  FACTORY_SECURE_ENVM(3, "factory secure boot from eNVM");          //$NON-NLS-1$
  //@formatter:on

  
  // Will hold the bootmode number and its description for all possible options
  private static Map<Integer, BootMode> map = Stream.of(BootMode.values())
      .collect(Collectors.toMap(BootMode::getMode, thisEnum -> thisEnum));

  private final Integer mode;
  private final String  description;

  
  private BootMode(Integer mode, String description) {
    this.mode        = mode;
    this.description = description;
  }

  
  public Integer getMode() {
    return mode;
  }

  
  public String getDescription() {
    return description;
  }

  
  // Return a human friendly number - description version useful for printing 
  public String getModeAndDescription() {
    return mode + " - " + description;     //$NON-NLS-1$
  }

  
  public String getModeAndDescriptionWithEqualsSign() {
    return mode + " = " + description;     //$NON-NLS-1$
  }

  
  // Allow to convert to this enum just by providing a int
  public static BootMode fromInt(int inputNumber) {
    // If we have a enum under given inputNumber, then return the enum
    if (map.containsKey(inputNumber))
      return map.get(inputNumber);

    // Just return null for all other cases where the input is not valid
    return null;
  }

  
}
