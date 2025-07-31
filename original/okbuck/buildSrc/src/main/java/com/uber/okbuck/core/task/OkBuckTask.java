package com.uber.okbuck.core.task;

import static com.uber.okbuck.OkBuckGradlePlugin.OKBUCK_DEFS_FILE;

import com.uber.okbuck.OkBuckGradlePlugin;
import com.uber.okbuck.composer.base.BuckRuleComposer;
import com.uber.okbuck.core.manager.GroovyManager;
import com.uber.okbuck.core.manager.JetifierManager;
import com.uber.okbuck.core.manager.KotlinManager;
import com.uber.okbuck.core.manager.ScalaManager;
import com.uber.okbuck.core.model.base.ProjectType;
import com.uber.okbuck.core.util.ProguardUtil;
import com.uber.okbuck.core.util.ProjectUtil;
import com.uber.okbuck.extension.KotlinExtension;
import com.uber.okbuck.extension.OkBuckExtension;
import com.uber.okbuck.extension.ScalaExtension;
import com.uber.okbuck.generator.OkbuckBuckConfigGenerator;
import com.uber.okbuck.template.config.BuckDefs;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

@SuppressWarnings({"WeakerAccess", "unused", "ResultOfMethodCallIgnored", "NewApi"})
public class OkBuckTask extends DefaultTask {

  public static final String CLASSPATH_ABI_MACRO = "classpath_abi";

  @Nested public OkBuckExtension okBuckExtension;

  @Nested public KotlinExtension kotlinExtension;

  @Nested public ScalaExtension scalaExtension;

  @Inject
  public OkBuckTask(
      OkBuckExtension okBuckExtension,
      KotlinExtension kotlinExtension,
      ScalaExtension scalaExtension) {
    this.okBuckExtension = okBuckExtension;
    this.kotlinExtension = kotlinExtension;
    this.scalaExtension = scalaExtension;

    // Never up to date; this task isn't safe to run incrementally.
    getOutputs().upToDateWhen(Specs.satisfyNone());
  }

  @TaskAction
  void okbuck() {
    // Fetch Groovy support deps if needed
    boolean hasGroovyLib =
        okBuckExtension
            .buckProjects
            .stream()
            .anyMatch(project -> ProjectUtil.getType(project) == ProjectType.GROOVY_LIB);
    if (hasGroovyLib) {
      ProjectUtil.getGroovyManager(getProject()).setupGroovyHome();
    }

    // Fetch Scala support deps if needed
    String scalaLibraryLocation;
    boolean hasScalaLib =
        okBuckExtension
            .buckProjects
            .stream()
            .anyMatch(project -> ProjectUtil.getType(project) == ProjectType.SCALA_LIB);
    if (hasScalaLib) {
      Set<String> scalaDeps =
          ProjectUtil.getScalaManager(getProject()).setupScalaHome(scalaExtension.version);
      scalaLibraryLocation =
          BuckRuleComposer.fileRule(
              scalaDeps.stream().filter(it -> it.contains("scala-library")).findFirst().get());
    } else {
      scalaLibraryLocation = "";
    }

    // Fetch Kotlin deps if needed
    if (kotlinExtension.version != null) {
      ProjectUtil.getKotlinManager(getProject()).setupKotlinHome(kotlinExtension.version);
    }

    generate(
        okBuckExtension,
        hasGroovyLib ? GroovyManager.GROOVY_HOME_LOCATION : null,
        kotlinExtension.version != null ? KotlinManager.KOTLIN_HOME_LOCATION : null,
        hasScalaLib ? ScalaManager.SCALA_COMPILER_LOCATION : null,
        hasScalaLib ? scalaLibraryLocation : null);
  }

  @Override
  public String getGroup() {
    return OkBuckGradlePlugin.GROUP;
  }

  @Override
  public String getDescription() {
    return "Okbuck task for the root project. Also sets up groovy and kotlin if required.";
  }

  @OutputFile
  public File okbuckDefs() {
    return getProject().file(OKBUCK_DEFS_FILE);
  }

  @OutputFile
  public File dotBuckConfig() {
    return getProject().file(".buckconfig");
  }

  @OutputFile
  public File okbuckBuckConfig() {
    return getProject().file(OkBuckGradlePlugin.OKBUCK_CONFIG + "/okbuck.buckconfig");
  }

  @SuppressWarnings("NullAway")
  private void generate(
      OkBuckExtension okbuckExt,
      @Nullable String groovyHome,
      @Nullable String kotlinHome,
      @Nullable String scalaCompiler,
      @Nullable String scalaLibrary) {
    // generate empty .buckconfig if it does not exist
    try {
      dotBuckConfig().createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Setup defs
    new BuckDefs()
        .resourceExcludes(
            okBuckExtension
                .excludeResources
                .stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.toSet()))
        .classpathMacro(CLASSPATH_ABI_MACRO)
        .lintJvmArgs(okbuckExt.getLintExtension().jvmArgs)
        .enableLint(!okbuckExt.getLintExtension().disabled)
        .hasCustomJetifierConfigurationFile(
            okbuckExt.getJetifierExtension().customConfigFile != null)
        .externalDependencyCache(okbuckExt.externalDependencyCache)
        .classpathExclusionRegex(okbuckExt.getLintExtension().classpathExclusionRegex)
        .useCompilationClasspath(okbuckExt.getLintExtension().useCompilationClasspath)
        .render(okbuckDefs());

    // generate .buckconfig.okbuck
    OkbuckBuckConfigGenerator.generate(
            okbuckExt,
            groovyHome,
            kotlinHome,
            scalaCompiler,
            scalaLibrary,
            ProguardUtil.getProguardJarPath(getProject()))
        .render(okbuckBuckConfig());
  }
}
