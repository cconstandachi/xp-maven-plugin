/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.runners.input;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import net.xp_forge.maven.plugins.xp.runners.filter.InifileFilter;

/**
 * Unittest command
 * ~~~~~~~~~~~~~~~~
 *
 * Usage:
 * ========================================================================
 *   unittest [options] test [test [test...]]
 * ========================================================================
 *
 * Options is one of:
 *
 *   * -v : Be verbose
 *   * -cp: Add classpath elements
 *   * -a {argument}: Define argument to pass to tests (may be used
 *     multiple times)
 *   * -l {listener.class.Name} {output}, where output is either "-"
 *     for console output or a file name
 *
 * Tests can be one or more of:
 *
 *   * {tests}.ini: A configuration file
 *   * {package.name}.*: All classes inside a given package
 *   * {package.name}.**: All classes inside a given package and all subpackages
 *   * {Test}.class.php: A class file
 *   * {test.class.Name}: A fully qualified class name
 *   * {test.class.Name}::{testName}: A fully qualified class name and a test name
 *   * -e {test method sourcecode}: Evaluate source
 */
public class UnittestRunnerInput extends AbstractClasspathRunnerInput {
  public List<String> arguments;
  public List<File> inifiles;

  /**
   * Constructor
   *
   */
  public UnittestRunnerInput() {
    super();
    this.arguments = new ArrayList<String>();
    this.inifiles  = new ArrayList<File>();
  }

  /**
   * Setter for arguments
   *
   * @param  java.lang.String argument Argument to add
   * @return void
   */
  public void addArgument(String argument) {

    // Invalid argument
    if (null == argument || 0 == argument.trim().length()) return;

    // Check argument not added twice
    for (String arg : this.arguments) {
      if (arg.equals(argument)) return;
    }

    // Add to list
    this.arguments.add(argument);
  }

  /**
   * Setter for inifiles
   *
   * @param  java.io.File inifile Inifile to add to list
   * @return void
   */
  public void addInifile(File inifile) {

    // Invalid inifile
    if (!inifile.exists()) return;

    // Check inifile not added twice
    String inifilePath= inifile.getAbsolutePath();
    for (File ini : this.inifiles) {
      if (ini.getAbsolutePath().equals(inifilePath)) return;
    }

    // Add to list
    this.inifiles.add(inifile);
  }

  /**
   * Setter for inifiles
   *
   * @param  java.io.File inifileDirectory Inifile directory to add to list
   * @return void
   */
  public void addInifileDirectory(File inifileDirectory) {

    // Invalid inifile directory
    if (!inifileDirectory.exists() || !inifileDirectory.isDirectory()) return;

    // Get inifiles
    for (File ini : Arrays.asList(inifileDirectory.listFiles(new InifileFilter()))) {
      this.addInifile(ini);
    }
  }
}
