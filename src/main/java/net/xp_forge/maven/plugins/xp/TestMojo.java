/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.runners.RunnerException;
import net.xp_forge.maven.plugins.xp.runners.UnittestRunner;
import net.xp_forge.maven.plugins.xp.runners.input.UnittestRunnerInput;

/**
 * Run unittests
 *
 * @goal test
 * @requiresDependencyResolution test
 */
public class TestMojo extends AbstractXpMojo {

  /**
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * Display verbose diagnostics
   *
   * The -v argument for the unittest runner
   *
   * @parameter expression="${xp.test.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the unittest runner
   *
   * @parameter expression="${xp.test.classpaths}"
   */
  protected List<String> classpaths;

  /**
   * Define argument to pass to tests
   *
   * The -a argument for the unittest runner
   *
   * @parameter expression="${xp.test.testArguments}"
   */
  protected List<String> testArguments;

  /**
   * Directory to scan for [*.ini] files
   *
   * @parameter expression="${xp.test.iniDirectory}" default-value="${basedir}/src/test/ini"
   */
  protected File iniDirectory;

  /**
   * Additional directories to scan for [*.ini] files
   *
   * @parameter
   */
  protected List<File> iniDirectories;

  /**
   * {@inheritDoc}
   *
   */
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {

    // Skip tests alltogether?
    if (this.skip) {
      getLog().info("Not running tests (maven.test.skip)");
      return;
    }

    // Debug info
    getLog().info("Running tests from [" + this.iniDirectory + "]");
    getLog().debug("Additional directories [" + (null == this.iniDirectories ? "NULL" : this.iniDirectories) + "]");
    getLog().debug("Classes directory      [" + this.classesDirectory + "]");
    getLog().debug("Test classes directory [" + this.testClassesDirectory + "]");
    getLog().debug("Classpaths             [" + (null == this.classpaths ? "NULL" : this.classpaths) + "]");
    getLog().debug("Test arguments         [" + (null == this.testArguments ? "NULL" : this.testArguments) + "]");

    // Prepare [unittest] input
    UnittestRunnerInput input= new UnittestRunnerInput();
    input.verbose= this.verbose;

    // Add dependency classpaths
    input.addClasspath(this.getArtifacts(false));

    // Add custom classpaths
    input.addClasspath(this.classpaths);

    // Add classesDirectory to classpath
    if (null != this.classifier && this.classifier.equals("patch")) {
      input.addClasspath("!" + this.classesDirectory);
    } else {
      input.addClasspath(this.classesDirectory);
    }

    // Add testClassesDirectory to classpath
    input.addClasspath(this.testClassesDirectory);

    // Add arguments
    if (null != this.testArguments) {
      for (String testArgument : this.testArguments) {
        input.addArgument(testArgument);
      }
    }

    // Inifiles
    input.addInifileDirectory(this.iniDirectory);
    if (null != this.iniDirectories) {
      for (File dir : this.iniDirectories) {
        input.addInifileDirectory(dir);
      }
    }

    // Check no tests to run
    if (0 == input.inifiles.size()) {
      getLog().info("There are no tests to run");
      getLog().info(LINE_SEPARATOR);
      return;
    }

    // Configure [unittest] runner
    File executable= new File(this.runnersDirectory, "unittest");
    UnittestRunner runner= new UnittestRunner(executable, input);
    runner.setLog(getLog());

    // Set runner working directory to [/target]
    runner.setWorkingDirectory(this.outputDirectory);

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [unittest] runner failed", ex);
    }
  }
}
