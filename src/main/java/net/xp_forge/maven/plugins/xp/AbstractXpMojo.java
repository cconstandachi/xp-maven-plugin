/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

/**
 * Base class for all MOJO's
 *
 */
public abstract class AbstractXpMojo extends AbstractMojo {
  public static final String LINE_SEPARATOR= "------------------------------------------------------------------------";
  public static final String CREATED_BY_NOTICE= "This file was automatically created by xp-maven-plugin";

  public static final String XP_FRAMEWORK_GROUP_ID = "net.xp-framework";
  public static final String POM_ARTIFACT_ID       = "pom";
  public static final String CORE_ARTIFACT_ID      = "core";
  public static final String TOOLS_ARTIFACT_ID     = "tools";
  public static final String COMPILER_ARTIFACT_ID  = "compiler";

  /**
   * The Maven project
   *
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * The projects in the reactor
   *
   * @parameter expression="${reactorProjects}"
   * @readonly
   */
  protected List<MavenProject> reactorProjects;

  /**
   * The Maven session
   *
   * @parameter default-value="${session}"
   * @readonly
   * @required
   */
  protected MavenSession session;

  /**
   * Maven project helper
   *
   * @component
   */
  protected MavenProjectHelper projectHelper;

  /**
   * Maven resource filtering
   *
   * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
   * @required
   */
  protected MavenResourcesFiltering mavenResourcesFiltering;

  /**
   * Project base directory
   *
   * @parameter expression="${basedir}" default-value="${basedir}"
   * @required
   * @readonly
   */
  protected File basedir;

  /**
   * Directory containing the generated XAR
   *
   * @parameter expression="${project.build.directory}"
   * @required
   * @readonly
   */
  protected File outputDirectory;

  /**
   * The directory containing generated classes of the project being tested
   * This will be included after the test classes in the test classpath
   *
   * @parameter expression="${project.build.outputDirectory}"
   * @required
   * @readonly
   */
  protected File classesDirectory;

  /**
   * The directory containing generated test classes of the project being tested
   * This will be included at the beginning of the test classpath
   *
   * @parameter expression="${project.build.testOutputDirectory}"
   * @required
   * @readonly
   */
  protected File testClassesDirectory;

  /**
   * Classifier to add to the generated artifact
   *
   * @parameter expression="${project.classifier}"
   * @readonly
   */
  protected String classifier;

  /**
   * Whether to use local XP-Framework install or to use bootstrap in [/target]. Default [false].
   *
   * @parameter expression="${xp.runtime.local}"
   */
  protected boolean local;

  /**
   * USE_XP
   *
   * @parameter expression="${xp.runtime.use_xp}"
   */
  protected String use_xp;

  /**
   * Directory where XP-Runners are located. If not set, runners will be
   * extracted from resources to "${project.build.directory}/.runtime/runners"
   *
   * @parameter expression="${xp.runtime.runners.directory}"
   */
  protected File runnersDirectory;

  /**
   * Bootstrap timezone. If not set will use system default
   *
   * @parameter expression="${xp.runtime.timezone}"
   */
  protected String timezone;

  /**
   * Location of the PHP executable. If not set will search for it in PATH
   *
   * @parameter expression="${xp.runtime.php}"
   */
  protected File php;

  /**
   * Helper function to find a project dependency
   *
   * @param  java.lang.String groupId
   * @param  java.lang.String artifactId
   * @return org.apache.maven.model.Dependency null if the specified dependency cannot be found
   */
  @SuppressWarnings("unchecked")
  protected Dependency findDependency(String groupId, String artifactId) {
    for (Dependency dependency : (Iterable<Dependency>)this.project.getDependencies()) {
      if (dependency.getGroupId().equals(groupId) && dependency.getArtifactId().equals(artifactId)) {
        return dependency;
      }
    }

    // Specified dependency not found
    return null;
  }

  /**
   * Helper function to find a project artifact
   *
   * @param  java.lang.String groupId
   * @param  java.lang.String artifactId
   * @return org.apache.maven.artifact.Artifact null if the specified artifact cannot be found
   */
  @SuppressWarnings("unchecked")
  protected Artifact findArtifact(String groupId, String artifactId) {
    for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
      if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)) {
        return artifact;
      }
    }

    // Specified artifact not found
    return null;
  }

  /**
   * Helper function to return all project artifacts
   *
   *
   * @param  boolean includeXpArtifacts
   * @return java.util.Set<org.apache.maven.artifact.Artifact> null if the specified artifact cannot be found
   */
  @SuppressWarnings("unchecked")
  protected Set<Artifact> getArtifacts(boolean includeXpArtifacts) {

    // Short-circut
    if (includeXpArtifacts) {
      return this.project.getArtifacts();
    }

    // Return all non XP-artifacts
    Set<Artifact> retVal= new HashSet<Artifact>();
    for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
      if (
        artifact.getGroupId().equals(XP_FRAMEWORK_GROUP_ID) &&
        null == artifact.getClassifier() &&       // Some projects may require core-tests artifact as dependency
        (
          artifact.getArtifactId().equals(POM_ARTIFACT_ID) ||
          artifact.getArtifactId().equals(CORE_ARTIFACT_ID) ||
          artifact.getArtifactId().equals(TOOLS_ARTIFACT_ID) ||
          artifact.getArtifactId().equals(COMPILER_ARTIFACT_ID)
        )
      ) continue;
      retVal.add(artifact);
    }

    return retVal;
  }
}
