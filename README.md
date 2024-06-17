# Build

- Written in Eclipse 2020-09 for Java Developers although Eclipse is not required for building the project.
- To build the project Maven 3.6.3 is used. Use `mvn package` to generate the jar. See build.cmd. Maven needs to be installed fully and be on the **PATH**
- Compiled with OpenJDK 13.0.1 which is the same JDK bundled with SoftConsole v2021.1-6.6. Might not work with older JDK versions as the project is using some newer Java features.
- Ensure that the **PATH** and **JAVA_HOME** environment variables are set correctly - for example:

``set PATH=%windir%;%windir%\System32;C:\Microchip\SoftConsole-v2021.1-6.6\eclipse\jre\bin;c:\tools\apache-maven-3.6.3\bin``

``set JAVA_HOME=C:\Microchip\SoftConsole-v2021.1-6.6\eclipse\jre``

- `pom.xml` includes metadata for tests. If you have some tests (distributed source code has no bundled tests) and want them to pass correctly, then check the entry in maven-surefire-plugin and make sure that the `SC_INSTALL_DIR` environment variable is set to point at an existing SoftConsole installation.
   - It may also be necesary to add the `FPGENPROG` environment variable to `pom.xml` depending on what the tests are doing and where your Libero or Program_Debug tools are installed.
   
# Custom Eclipse formatting scheme

A custom Eclipse formatting scheme is provided with the following differences from the default one:

- Using spaces instead of tabs for indentation
- Using 2 spaces instead of 4 for indentation
- Enabled keywords to disable formatter

To use the custom formatting scheme:

Main Menu -> Window -> Preferences -> Java -> Code Style -> Formatter -> Import > Select `project-formatting-scheme.xml` -> Apply and Close

# Strings

All strings must follow these rules:

- All Strings must be **externalized** if they going to be shown to the user (DEBUG_VERBOSE can be kept internally)
- Test classes can use the following to supress warnings `@SuppressWarnings("nls")`
- All other Strings which are not messages musthave the `//$NON-NLS-1$` annotation
- Strings which do not follow rules above are inteded to be removed before a package release 
 
# Custom Eclipse dictionary

The custom dictionary file `dictionary.txt` can be used on top of regular Eclipse spell-checking. 

To use the customer dictionary:

Main Menu -> Window -> Preferences -> General -> Editors -> Text Editors -> Spelling -> User defined dictionary -> Browse... -> Select `dictionary.txt` -> Open -> Apply and Close

# Static code analysis

A spotbugs plugin can be used to check `mvn spotbugs:check` for possible problems and display them in the GUI `mvn spotbugs:gui`.

Some methods use SpotBugs annotation, for example Status and InvokeStatus returns must be used (checked) in order to avoid possible bugs by creating a return status and then forgetting to return it, or use it in some other ways.

# Tests

Split into a few categories (tags):

- **worksWithoutHw** These tests should pass without any hardware requirements, the `SC_INSTALL_DIR` environment variable has to point to SoftConsole installation. Note: Some tests in this category are skipped as they are using the same parameters as different tests, however in this scope they would need `FPGENPROG` and therefore are skipped in this test scope.
- **needsAttention** These tests require some handholding (making sure the tested directories exist on the host, etc.), and along side the `SC_INSTALL_DIR` environment variable and might require the `FPGENPROG` environment variable to be set if it's not auto-detected in the default installation locations.  
- **needsHw** These tests require connected and working PolarFire SoC Icicle Kit board. The Icicle board must be programmed with `testFiles\Icicle-TestDesign\MPFS_ICICLE_eMMC.job` and have J11 USB connector connected and the`ICICLE_UART_COM` environment variable set to match the COM port used (should be the first CP2108 port). The `SC_INSTALL_DIR` and `FPGENPROG` environment variables must also be set correctly.

Tests will be triggered with each `mvn package` or with `mvn test` commands

# Test reports

The finished tests save the results into an XML file which can be converted into human readable HTML.
Ivoking the following will make sure the tests will be run, the test report produced and everything packaged into a single archive file `<project>-site.jar`.

`mvn clean compile test site surefire-report:report site:jar` 

After running it check the following files:
 - `target/site/surefire-report.html` : contains the test report.
 - `target/mpfsBootmodeProgrammer-2.0-site.jar` : the whole target/site folder compressed to a JAR archive file.
 - `target/site/index.html` : various information about the project, however it does not  link/reference `surefire-report.html`.
