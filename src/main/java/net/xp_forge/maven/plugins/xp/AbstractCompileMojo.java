/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.runners.XccRunner;
import net.xp_forge.maven.plugins.xp.runners.RunnerException;
import net.xp_forge.maven.plugins.xp.runners.input.XccRunnerInput;
import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.MavenResourceUtils;

/**
 * Wrapper around the XP-Framework "XccRunner;" runner
 *
 */
public abstract class AbstractCompileMojo extends AbstractXpMojo {

  /**
   * Display verbose diagnostics
   *
   * The -v argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.classpaths}"
   */
  protected List<String> classpaths;

  /**
   * Adds path to source path (source path will equal classpath initially)
   *
   * The -sp argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.sourcepaths}"
   */
  protected List<String> sourcepaths;

  /**
   * Use emitter, defaults to "source"
   *
   * The -e argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.emitter}"
   */
  protected String emitter;

  /**
   * Use compiler profiles (defaults to ["default"]) - xp/compiler/{profile}.xcp.ini
   *
   * The -p argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.profiles}"
   */
  protected List<String> profiles;

  /**
   * Get PHP sources
   *
   */
  protected abstract List<String> getPhpSourceRoots();

  /**
   * Get PHP sources include pattern
   *
   */
  protected abstract String getPhpIncludePattern();

  /**
   * Get XP sources
   *
   */
  protected abstract List<String> getCompileSourceRoots();

  /**
   * Get additional classpath
   *
   */
  protected abstract String getAdditionalClasspath();

  /**
   * Get classes directory where to output copied/compiled classes
   *
   */
  protected abstract File getClassesDirectory();

  /**
   * If true, skip compiling
   *
   */
  protected abstract boolean isSkip();

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {

    // Skip tests alltogether?
    if (this.isSkip()) {
      getLog().info("Not compiling sources (maven.test.skip)");
      return;
    }

    // Copy raw PHP files
    List<String> phpSourceRoots= this.getPhpSourceRoots();
    this.copyPhpSources(phpSourceRoots, this.getClassesDirectory(), this.getPhpIncludePattern());

    // Cleanup source roots
    List<String> compileSourceRoots= FileUtils.filterEmptyDirectories(this.getCompileSourceRoots());
    if (compileSourceRoots.isEmpty()) {
      getLog().info("There are no sources to compile");
      return;
    }

    // Let [xcc] know where to get sources from
    for (String compileSourceRoot : compileSourceRoots) {
      this.addSourcepath(compileSourceRoot);
    }

    // Also add the PHP sources to classpath
    for (String phpSourceRoot : phpSourceRoots) {
      this.addClasspath(phpSourceRoot);
    }

    // Add additional classpath
    this.addClasspath(this.getAdditionalClasspath());

    // Execute [xcc]
    this.executeXcc(compileSourceRoots, this.getClassesDirectory());
  }

  /**
   * Execute XP-Framework XCC compiler
   *
   * @param  java.util.List<String> sourceDirectories Source where .xp file are
   * @param  java.io.File classesDirectory Destination where to output compiled classes
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xcc runner failed
   */
  public void executeXcc(List<String> sourceDirectories, File classesDirectory) throws MojoExecutionException {

    // Compile each source root
    Iterator i= sourceDirectories.iterator();
    while (i.hasNext()) {
      this.executeXcc((String)i.next(), classesDirectory);
    }
  }

  /**
   * Execute XP-Framework XCC compiler
   *
   * @param  java.lang.String sourceDirectory Source where .xp file are
   * @param  java.io.File classesDirectory Destination where to output compiled classes
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xcc runner failed
   */
  public void executeXcc(String sourceDirectory, File classesDirectory) throws MojoExecutionException {
    Iterator i;

    // Debug info
    getLog().info("Source directory    [" + sourceDirectory + "]");
    getLog().info("PHP include pattern [" + this.getPhpIncludePattern() + "]");
    getLog().info("Classes directory    [" + classesDirectory + "]");
    getLog().debug("Sourcepaths          [" + (null == this.sourcepaths ? "NULL" : this.sourcepaths) + "]");
    getLog().debug("Classpaths           [" + (null == this.classpaths  ? "NULL" : this.classpaths)  + "]");

    // Prepare xcc input
    XccRunnerInput input= new XccRunnerInput();
    input.verbose= this.verbose;

    // Add dependency classpaths
    input.addClasspath(this.getArtifacts(false));

    // Add custom classpaths
    input.addClasspath(this.classpaths);

    // Add sourcepaths
    if (null != this.sourcepaths) {
      i= this.sourcepaths.iterator();
      while (i.hasNext()) {
        input.addSourcepath(new File((String)i.next()));
      }
    }

    // Add emitter
    input.emitter= this.emitter;

    // Add profiles
    if (null != this.profiles) {
      i= this.profiles.iterator();
      while (i.hasNext()) {
        input.addProfile((String)i.next());
      }
    }

    // Add outputdir
    input.outputdir= classesDirectory;

    // Add source
    input.addSource(new File(sourceDirectory));

    // Configure [xcc] runner
    File executable= new File(this.runnersDirectory, "xcc");
    XccRunner runner= new XccRunner(executable, input);
    runner.setLog(getLog());

    // Set runner working directory to [/target]
    runner.setWorkingDirectory(this.outputDirectory);

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [xcc] runner failed", ex);
    }
  }

  /**
   * Add an entry to sourcepaths
   *
   * @param  java.lang.String sourcepath
   * @return void
   */
  protected void addSourcepath(String sourcepath) {
    if (null == this.sourcepaths) this.sourcepaths= new ArrayList<String>();

    // Make sourcepath absolute
    String absoluteSourcepath= FileUtils.getAbsolutePath(sourcepath, this.basedir);
    if (null == absoluteSourcepath) return;

    // Add to sourcepaths
    this.sourcepaths.add(absoluteSourcepath);
  }

  /**
   * Add an entry to classpaths
   *
   * @param  java.lang.String classpath
   * @return void
   */
  protected void addClasspath(String classpath) {
    if (null == classpath) return;

    // Init classpaths
    if (null == this.classpaths) this.classpaths= new ArrayList<String>();

    // Make classpath absolute
    String absoluteClasspath= FileUtils.getAbsolutePath(classpath, this.basedir);
    if (null == absoluteClasspath) return;

    // Add to sourcepaths
    this.classpaths.add(absoluteClasspath);
  }

  /**
   * Copy "/src/main|test/php/**.class.php" files "/target/classes|test-classes/"
   *
   * @param  java.util.List<String> phpSourceRoots Source where raw PHP files are
   * @param  java.io.File classesDirectory Destination where to copy PHP files
   * @param  java.lang.String phpIncludePattern
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When copy of the PHP source files failed
   */
  protected void copyPhpSources(List<String> phpSourceRoots, File classesDirectory, String phpIncludePattern) throws MojoExecutionException {

    // Debug info
    getLog().debug("PHP source directories [" + (null == phpSourceRoots ? "NULL" : phpSourceRoots.toString()) + "]");

    // Ignore non-existing raw PHP files
    if (null == phpSourceRoots || phpSourceRoots.isEmpty()) {
      getLog().info("There are no PHP sources to copy");
      return;
    }

    // Create resources
    List<Resource> resources= new ArrayList<Resource>();

    // Build a resource for each phpSourceRoots
    for (String phpSourceRoot : phpSourceRoots) {

      // Check directory exists
      if (null == FileUtils.getAbsolutePath(phpSourceRoot, this.basedir)) {
        getLog().info("Skip non-existing PHP source directory [" + phpSourceRoot + "]");
        continue;
      }

      Resource resource= new Resource();
      resource.addInclude(phpIncludePattern);
      resource.setFiltering(false);
      resource.setDirectory(phpSourceRoot);
      resources.add(resource);

      // Filter resources
      try {
        MavenResourceUtils.copyResources(resources, classesDirectory, this.project, this.session, this.mavenResourcesFiltering);

      } catch(IOException ex) {
        throw new MojoExecutionException("Failed to copy PHP sources", ex);
      }
    }
  }
}
