//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.utils;

import java.util.Formatter;
import java.util.Locale;

//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Messages {
  
  
  private static final String         BUNDLE_NAME     = "messages"; //$NON-NLS-1$
  private static final Locale         EN_US           = new Locale("en", "US"); //$NON-NLS-1$  //$NON-NLS-2$
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, EN_US);
  
  
  private Messages() {
  }

  
//  public static String get(String key) {
//    try {
//      return RESOURCE_BUNDLE.getString(key);
//    } catch (MissingResourceException e) {
//      return '!' + key + '!';
//    }
//  }


  public static String get(MessageEnum key) {
    try {
      return RESOURCE_BUNDLE.getString(key.getPropertyKey());
    } catch (MissingResourceException e) {
      return '!' + key.getPropertyKey() + '!';
    }
  }
  
  
//  public static String getRaw(String key) throws MissingResourceException {
//    return RESOURCE_BUNDLE.getString(key);
//  }

  
  public static String getRaw(MessageEnum key) throws MissingResourceException {
    return RESOURCE_BUNDLE.getString(key.getPropertyKey());
  }
  
  
  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=434065
  @SuppressWarnings("resource")
  public static String format(MessageEnum format, Object... args) {
    var formatString = get(format);
    Formatter formatter = new Formatter().format( formatString, args);
    String ret = formatter.toString();
    formatter.close();  // Closing the formatter, yet getting the warning
    return ret.toString();  
  }
  
  
}
