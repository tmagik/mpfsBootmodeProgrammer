//Copyright 2021 Microchip FPGA Embedded Systems Solutions.
//SPDX-License-Identifier: MIT

package com.microchip.sc.mpfsbootmodeprogrammer.utils;


@SuppressWarnings("nls")
public enum MessageEnum {

  //@formatter:off
  MAIN_STARTED(                               "main.started"),
  MAIN_WITH_PARAMS(                           "main.with_params"),
  MAIN_HELP(                                  "main.help"),                              
  MAIN_VERSION(                               "main.version"),    
  MAIN_GENERATE_HEX(                          "main.generate_hex"),
  MAIN_GENERATE_BIN(                          "main.generate_bin"),
  MAIN_GENERATE_FAKE_SBIC(                    "main.generate_fake_sbic"),
  MAIN_GENERATE_SBIC(                         "main.generate_sbic"),
  MAIN_ECDSA_GENERATE(                        "main.ecdsa_generate"),
  MAIN_ECDSA_SEPARATED(                       "main.ecdsa_separated"),
  MAIN_CANNOT_FIT_SBIC_BEFORE_THE_IMAGE(      "main.cannot_fit_sbic_before_the_image"),
  MAIN_WARN_SNVM_NUKED(                       "main.warn_snvm_nuked"),
  MAIN_PREPARE(                               "main.prepare"),
  MAIN_GENERATE_BISTREAM(                     "main.generate_bistream"),
  MAIN_DRY_RUN(                               "main.dry_run"),
  MAIN_PROGRAMMING(                           "main.programming"),
  MAIN_VERIFY(                                "main.verify"),
  MAIN_COMPLETE(                              "main.complete"),
  
  ARG_ERROR_BOOT_NOT_INT(                     "arg.error_boot_not_int"),
  ARG_ERROR_BOOT_WRONG_INT(                   "arg.error_boot_wrong_int"),
  ARG_ERROR_SNVM_PAGE_WRONG_INT(              "arg.error_snvm_page_wrong_int"),
  ARG_ERROR_DIE_WRONG(                        "arg.error_die_wrong"),
  ARG_ERROR_INVALID_ARG(                      "arg.error_invalid_arg"),
  ARG_ERROR_PACKAGE_WRONG(                    "arg.error_package_wrong"),
  ARG_ERROR_TOO_MANY_FILES(                   "arg.error_too_many_files"),
  ARG_ERROR_WORKDIR_NOT_A_PATH(               "arg.error_workdir_not_a_path"),
  ARG_ERROR_FILE_NOT_A_FILE(                  "arg.error_file_not_a_file"),
  ARG_ERROR_KEY_WRONG(                        "arg.error_key_wrong"),
  ARG_ERROR_KEY_FILE_NOT_EXIST(               "arg.error_key_file_not_exist"),
  ARG_ERROR_AGUMENTS_MORE(                    "arg.error_arguments_more"),
  
  VALIDATE_GOING_TO_DETECT_ELF(               "validate.going_to_detect_elf"),
  VALIDATE_ELF_FILE_FOUND(                    "validate.elf_file_found"),
//  VALIDATE_ERROR_COULDNT_USE_WORKDIR_TO_WALK( "validate.error_couldnt_use_workdir_to_walk"),
  VALIDATE_ERROR_WORKDIR_WALK_EXCEPTION(      "validate.error_workdir_walk_exception"),
  VALIDATE_ERROR_CANT_FIND_ELF_IN_WORKDIR(    "validate.error_cant_find_elf_in_workdir"),
  VALIDATE_ERROR_ELF_DOESNT_EXISTS(           "validate.error_elf_doesnt_exists"),
  VALIDATE_ERROR_ELF_ABSOLUTE(                "validate.error_elf_absolute"),
  VALIDATE_ERROR_ELF_NOT_ELF(                 "validate.error_elf_not_elf"),
  VALIDATE_ERROR_WORK_DIRECTORY_DOESNT_EXISTS("validate.error_work_directory_doesnt_exists"),
  VALIDATE_ENCRYPT_IN_WRONG_BM(               "validate.encrypt_in_wrong_bm"),
  VALIDATE_USK_NOT_NEEDED(                    "validate.usk_not_needed"),
  VALIDATE_SNVP_PAGE_NOT_NEEDED(              "validate.snvp_page_not_needed"),
  VALIDATE_USK_NOT_VALID(                     "validate.usk_not_valid"),
  VALIDATE_USK_ZERO(                          "validate.usk_zero"),
  VALIDATE_ONLY_BM3_KEYS(                     "validate.only_bm3_keys"),
  
  UTILS_PREPARING_OUTPUT_FOLDER(              "utils.preparing_output_folder"),
  UTILS_WIPING_OUTPUT_FOLDER(                 "utils.wiping_output_folder"),
  UTILS_WIPING_FAILED(                        "utils.wiping_failed"),
  UTILS_CANT_CREATE_FOLDER(                   "utils.cant_create_folder"),
  
  INVOKE_EXCEPTION(                           "invoke.exception"),
  INVOKE_FINISHED(                            "invoke.finished"),
  INVOKE_VERBOSE_INVOKE(                      "invoke.verbose_invoke"),
  
  ENV_NO_SC_INSTALL_DIR(                      "env.no_sc_install_dir"),
  ENV_SC_ECDSA_PRIV_KEY_PASS(                 "env.sc_ecdsa_priv_key_pass"),
  
  OBJCOPY_SANITY_CHECK_FAILED(                "objcopy.sanity_check_failed"),
  OBJCOPY_ELF2HEX_FAILED(                     "objcopy.elf2hex_failed"),
  OBJCOPY_ELF2BIN_FAILED(                     "objcopy.elf2bin_failed"),
  OBJCOPY_BIN2HEX_FAILED(                     "objcopy.bin2hex_failed"),
  
  SIZE_INVOKE_FAILED(                         "size.invoke_failed"),
  SIZE_FAILED_PARSE_OUTPUT(                   "size.failed_parse_output"),
  SIZE_DETECTED_SIZE(                         "size.detected_size"),
  
  READELF_INVOKE_FAILED(                      "readelf.invoke_failed"),
  READELF_FAILED_PARSE_OUTPUT(                "readelf.failed_parse_output"),
  READELF_DETECTED_ENTRY(                     "readelf.detected_entry"),
  
  PAYLOAD_DUMMY_FAILED(                       "payload.dummy_failed"),
  PAYLOAD_SHA_FAILED(                         "payload.sha_failed"),
  PAYLOAD_GET_SIZE_FAILED(                    "payload.get_size_failed"),
  PAYLOAD_ENVM_WRONG_BOOTVECTOR(              "payload.envm_wrong_bootvector"),
  PAYLOAD_ENVM_IMAGEADDR_NOT_ALIGNED(         "payload.envm_imageaddr_not_aligned"),
  PAYLOAD_ENVM_TOO_BIG(                       "payload.envm_too_big"),
  PAYLOAD_BOOT2_UBLI_FAILED(                  "payload.boot2_ubli_failed"),
  PAYLOAD_BOOT2_WRONG_BOOTVECTOR(             "payload.boot2_wrong_bootvector"),
  PAYLOAD_BOOT2_IMAGEADDR_NOT_ALIGNED(        "payload.boot2_imageaddr_not_aligned"),
  PAYLOAD_BOOT2_IMAGELEN_NOT_ALIGNED(         "payload.boot2_imagelen_not_aligned"),
  PAYLOAD_BOOT2_SNVM_TOO_BIG(                 "payload.boot2_snvm_too_big"),
  PAYLOAD_BOOT2_LIM_NOT_FIT(                  "payload.boot2_lim_not_fit"),
  PAYLOAD_USK_SAVE_FAILED(                    "payload.usk_save_failed"),
  PAYLOAD_BOOT3_SBIC_FAILED(                  "payload.boot3_sbic_failed"),
  PAYLOAD_BOOT3_IMAGE_SBIC_COMBINING_FAILED(  "payload.boot3_image_sbic_combining_failed"),
  PAYLOAD_BOOT3_XY_SAVE_FAILED(               "payload.boot3_xy_save_failed"),
  PAYLOAD_DEBUG_SBIC_PAGE_BEFORE_IMAGE(       "payload.debug_sbic_page_before_image"),
  PAYLOAD_DEBUG_SBIC_ADDR_BEFORE_IMAGE(       "payload.debug_sbic_addr_before_image"),
  PAYLOAD_DEBUG_BIN_SIZE(                     "payload.debug_bin_size"),
  
  FPGENPROG_NOT_FOUND(                        "fpgenprog.not_found"),
  FPGENPROG_FPGENPROG_WRONG(                  "fpgenprog.fpgenprog_wrong"),
  FPGENPROG_FPGENPROG_PATH_EXCEPTION(         "fpgenprog.fpgenprog_path_exception"),
  FPGENPROG_PROJECT_CREATION_FAILED(          "fpgenprog.project_creation_failed"),
  FPGENPROG_BOOTMODE_SELECT_FAILED(           "fpgenprog.bootmode_select_failed"),
  FPGENPROG_ENVM_CLIENT_FAILED(               "fpgenprog.envm_client_failed"),
  FPGENPROG_SNVM_CLIENT_FAILED(               "fpgenprog.snvm_client_failed"),
  FPGENPROG_GENERATE_BITSTREAM_FAILED(        "fpgenprog.generate_bitstream_failed"),
  FPGENPROG_RUN_ACTION_PROGRAM_FAILED(        "fpgenprog.run_action_program_failed"),
  FPGENPROG_VERIFY_FAILED(                    "fpgenprog.verify_failed"),
  
  LOG_FILE_WRITE_FAILED(                      "log.file_write_failed"),
  LOG_SAVING_TO_LOG(                          "log.saving_to_log"),
  
  ECDSA_KEY_GEN_FAILED(                       "ecdsa.key_gen_failed"),
  ECDSA_KEY_READ_FAILED(                      "ecdsa.key_read_failed"),
  ECDSA_SIGN_FAILED(                          "ecdsa.sign_failed"),
  ECDSA_PARSE_KEY_FAILED(                     "ecdsa.parse_key_failed"),
  ECDSA_APPENDABLE_KEY_EXPLANATION(           "ecdsa.appendable_key_explanation");
  
//  END("end");
  
  //@formatter:on
  
  
  private final String  propertyKey;
  
  
  private MessageEnum(String propertyKey) {
    this.propertyKey = propertyKey;
  }
  
  
  public String getPropertyKey() {
    return propertyKey;
  }
  

}
