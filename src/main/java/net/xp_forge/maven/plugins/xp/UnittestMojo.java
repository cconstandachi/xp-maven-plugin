/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
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
public class UnittestMojo extends AbstractXpFrameworkMojo {

  /**
   *
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
   * @parameter expression="${xp.unittest.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the unittest runner
   *
   * @parameter expression="${xp.unittest.classpaths}"
   */
  protected ArrayList<String> classpaths;

  /**
   * Define argument to pass to tests
   *
   * The -a argument for the unittest runner
   *
   * @parameter expression="${xp.unittest.testArguments}"
   */
  protected ArrayList<String> testArguments;

  /**
   * Directory to scan for *.ini files
   *
   * @parameter expression="${xp.unittest.iniDirectory}" default-value="${project.build.testOutputDirectory}/etc/unittest"
   */
  protected File iniDirectory;

  /**
   * Additional directories to scan for [*.ini] files
   *
   * @parameter
   */
  protected ArrayList<File> iniDirectories;

  /**
   * {@inheritDoc}
   *
   */
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {

    // Run tests
    getLog().info(LINE_SEPARATOR);
    getLog().info("UNITTEST");
    getLog().info(LINE_SEPARATOR);

    // Skip tests alltogether?
    if (this.skip) {
      getLog().info("Not running tests (maven.test.skip)");
      return;
    }

    // Debug info
    getLog().info("Ini files directory [" + this.iniDirectory + "]");
    getLog().debug("Additional directories [" + (null == this.iniDirectories ? "NULL" : this.iniDirectories.toString()) + "]");
    getLog().debug("Classes directory      [" + this.classesDirectory + "]");
    getLog().debug("Test classes directory [" + this.testClassesDirectory + "]");
    getLog().debug("Classpaths             [" + (null == this.classpaths ? "NULL" : this.classpaths.toString()) + "]");
    getLog().debug("Test arguments         [" + (null == this.testArguments ? "NULL" : this.testArguments.toString()) + "]");

    // Prepare [unittest] input
    UnittestRunnerInput input= new UnittestRunnerInput();
    input.verbose= this.verbose;

    // Add dependency classpaths
    input.addClasspath(project.getArtifacts());

    // Add custom classpaths
    input.addClasspath(this.classpaths);

    // Add classesDirectory and testClassesDirectory to classpath
    input.addClasspath(this.classesDirectory);
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
      for (File iniDirectory : this.iniDirectories) {
        input.addInifileDirectory(iniDirectory);
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
    runner.setTrace(getLog());

    // Set runner working directory to [/target]
    try {
      runner.setWorkingDirectory(this.outputDirectory);
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot set [unittest] runner working directory", ex);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [unittest] runner failed", ex);
    }

    getLog().info(LINE_SEPARATOR);
  }
}
