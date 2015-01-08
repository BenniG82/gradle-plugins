package com.ewerk.gradle.plugins

import com.ewerk.gradle.plugins.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin

/**
 * This plugin can be used to easily create Mysema querydsl classes and attach them to the project
 * classpath.<br/><br/>
 *
 * The plugin registers the extension 'querydsl' so that plugin specific configuration can
 * be overwritten within the build sScript. Please see the readme doc on Github for details on that.
 * <br/><br/>
 *
 * The plugin will generate an additional source directory into where the querydsl
 * classes will be compiled, so that they can be ignored from SCM commits. Per default, this will
 * be {@link QuerydslPluginExtension#DEFAULT_QUERYDSL_SOURCES_DIR}.
 * <br/><br/>
 *
 * @author holgerstolzenberg
 * @since 1.0.0
 */
class QuerydslPlugin implements Plugin<Project> {

  public static final String TASK_GROUP = "Querydsl tasks"

  private static final Logger LOG = Logging.getLogger(QuerydslPlugin.class)

  @Override
  void apply(final Project project) {
    LOG.info("Applying Querydsl plugin")

    // do nothing if plugin is already applied
    if (project.plugins.hasPlugin(QuerydslPlugin.class)) {
      return;
    }

    LOG.info("Applying querydsl plugin")

    // apply core 'java' plugin if not present to make 'sourceSets' available
    if (!project.plugins.hasPlugin(JavaPlugin.class)) {
      project.plugins.apply(JavaPlugin.class)
    }

    // add 'Querydsl' DSL extension
    project.extensions.create(QuerydslPluginExtension.NAME, QuerydslPluginExtension)

    // add new tasks for creating/cleaning the auto-value sources dir
    project.task(type: CleanQuerydslSourcesDir, "cleanQuerydslSourcesDir")
    project.task(type: InitQuerydslSourcesDir, "initQuerydslSourcesDir")

    // make 'clean' depend clean ing querydsl sources
    project.tasks.clean.dependsOn project.tasks.cleanQuerydslSourcesDir

    project.afterEvaluate {

      jpaTask(project)
      jdoTask(project)
      hibernateTask(project)
      morphiaTask(project)
      rooTask(project)
      springDataMongoTask(project)

      File querydslSourcesDir = querydslSourcesDir(project)

      addLibrary(project)
      addSourceSet(project, querydslSourcesDir)
      registerSourceAtCompileJava(project, querydslSourcesDir)
    }
  }

  private static void rooTask(Project project) {
    if (project.extensions.querydsl.roo) {
      project.task(type: CompileQuerydslRoo, "compileQuerydslRoo")
      project.tasks.compileQuerydslRoo.dependsOn project.tasks.initQuerydslSourcesDir
      project.tasks.compileJava.dependsOn project.tasks.compileQuerydslRoo
    }
  }

  private static void morphiaTask(Project project) {
    if (project.extensions.querydsl.morphia) {
      project.task(type: CompileQuerydslMorphia, "compileQuerydslMorphia")
      project.tasks.compileQuerydslMorphia.dependsOn project.tasks.initQuerydslSourcesDir
      project.tasks.compileJava.dependsOn project.tasks.compileQuerydslMorphia
    }
  }

  private static void hibernateTask(Project project) {
    if (project.extensions.querydsl.hibernate) {
      project.task(type: CompileQuerydslHibernate, "compileQuerydslHibernate")
      project.tasks.compileQuerydslHibernate.dependsOn project.tasks.initQuerydslSourcesDir
      project.tasks.compileJava.dependsOn project.tasks.compileQuerydslHibernate
    }
  }

  private static void jdoTask(Project project) {
    if (project.extensions.querydsl.jdo) {
      project.task(type: CompileQuerydslJdo, "compileQuerydslJdo")
      project.tasks.compileQuerydslJdo.dependsOn project.tasks.initQuerydslSourcesDir
      project.tasks.compileJava.dependsOn project.tasks.compileQuerydslJdo
    }
  }

  private static void jpaTask(Project project) {
    if (project.extensions.querydsl.jpa) {
      project.task(type: CompileQuerydslJpa, "compileQuerydslJpa")
      project.tasks.compileQuerydslJpa.dependsOn project.tasks.initQuerydslSourcesDir
      project.tasks.compileJava.dependsOn project.tasks.compileQuerydslJpa
    }
  }

  private static void springDataMongoTask(Project project) {
    if (project.extensions.querydsl.springDataMongo) {
      project.task(type: CompileQuerydslSpringDataMongo, "compileQuerydslSpringDataMongo")
      project.tasks.compileQuerydslSpringDataMongo.dependsOn project.tasks.initQuerydslSourcesDir
      project.tasks.compileJava.dependsOn project.tasks.compileQuerydslSpringDataMongo
    }
  }

  private void registerSourceAtCompileJava(Project project, File querydslSourcesDir) {
    project.compileJava {
      source querydslSourcesDir
    }
  }

  private void addLibrary(Project project) {
    def library = project.extensions.querydsl.library
    LOG.info("Querydsl library: {}", library)
    project.dependencies {
      compile library
    }
  }

  private void addSourceSet(Project project, File sourcesDir) {
    LOG.info("Create source set 'querydsl'.");

    project.sourceSets {
      querydsl {
        java.srcDirs = [sourcesDir]
      }
    }
  }

  private static File querydslSourcesDir(Project project) {
    String path = project.extensions.querydsl.querydslSourcesDir
    File querydslSourcesDir = project.file(path)
    LOG.info("Querydsl sources dir: {}", querydslSourcesDir.absolutePath);
    return querydslSourcesDir
  }
}