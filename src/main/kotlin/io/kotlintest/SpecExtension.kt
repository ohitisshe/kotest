package io.kotlintest

// TODO rename and split file

import org.reflections.Reflections

data class TestCaseContext(
    val spec: TestBase,
    val testCase: TestCase)

interface ProjectExtension {
  fun beforeAll() {}
  fun afterAll() {}
  fun interceptProject(project: () -> Unit) {
    project()
  }
}


abstract class ProjectConfig {
  open val extensions: List<ProjectExtension> = listOf()
}


object Project {

  private var projectConfig: ProjectConfig? = null
  private var executedBefore = false
  private var executedAfter = false

  init {
    val configClasses = Reflections("io.kotlintest").getSubTypesOf(ProjectConfig::class.java)
    if (configClasses.size > 1)  {
      val configClassNames = configClasses.map { config -> config.typeName }
      throw InvalidConfigException("Duplicate GlobalConfig found: $configClassNames")
    }
    projectConfig = configClasses.firstOrNull()?.kotlin?.objectInstance
  }

  fun beforeAll() {
    synchronized(executedBefore) {
      if (!executedBefore) {
        projectConfig?.extensions?.forEach { extension -> extension.beforeAll() }
        executedBefore = true
      }
    }

  }

  fun afterAll() {
    synchronized(executedAfter) {
      if (!executedAfter) {
        projectConfig?.extensions?.forEach { extension -> extension.afterAll() }
        executedAfter = true
      }
    }
  }
}


class InvalidConfigException(message: String) : Exception(message)