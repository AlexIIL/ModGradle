package xyz.openmodloader.gradle;

import org.gradle.api.Project;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import xyz.openmodloader.gradle.task.*;
import xyz.openmodloader.gradle.util.Constants;

public class ModGradlePlugin extends AbstractPlugin {
    @Override
    public void apply(Project target) {
        super.apply(target);
        // Minecraft libraries configuration
        project.getConfigurations().maybeCreate(Constants.CONFIG_MC_DEPENDENCIES);
        project.getConfigurations().maybeCreate(Constants.CONFIG_MC_DEPENDENCIES_CLIENT);
        project.getConfigurations().maybeCreate(Constants.CONFIG_NATIVES);

        // Common libraries extends from client libraries, CONFIG_MC_DEPENDENCIES will contains all MC dependencies
        project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES).extendsFrom(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES_CLIENT));

        configureIDEs();

        makeTask("download", DownloadTask.class);
        makeTask("map", MapJarsTask.class).dependsOn("download");
        makeTask("mergeJars", MergeJarsTask.class).dependsOn("map");
        makeTask("decompile", DecompileTask.class).dependsOn("mergeJars");
        makeTask("applyPatches", PatchMinecraftTask.class).dependsOn("decompile");
        makeTask("setupOML", AbstractTask.class).dependsOn("applyPatches");

        makeTask("genPatches", GenPatchesTask.class);

        makeTask("extractNatives", ExtractNativesTask.class).dependsOn("download");
        makeTask("genIdeaRuns", GenIdeaProjectTask.class).dependsOn("cleanIdea").dependsOn("idea").dependsOn("extractNatives");
    }

    private void configureIDEs() {
        // IDEA
        IdeaModel ideaModule = (IdeaModel) project.getExtensions().getByName("idea");

        ideaModule.getModule().getExcludeDirs().addAll(project.files(".gradle", "build", ".idea", "out").getFiles());
        ideaModule.getModule().setDownloadJavadoc(true);
        ideaModule.getModule().setDownloadSources(true);
        ideaModule.getModule().setInheritOutputDirs(true);
        ideaModule.getModule().getScopes().get("COMPILE").get("plus").add(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES));

        // ECLIPSE
        EclipseModel eclipseModule = (EclipseModel) project.getExtensions().getByName("eclipse");
        eclipseModule.getClasspath().getPlusConfigurations().add(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES));
    }
    /**
     * Add MC dependencies to compile time
     */
    private void configureCompile() {
        JavaPluginConvention javaModule = (JavaPluginConvention) project.getConvention().getPlugins().get("java");

        SourceSet main = javaModule.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = javaModule.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        main.setCompileClasspath(main.getCompileClasspath()
                .plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));
        test.setCompileClasspath(test.getCompileClasspath()
                .plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));
        main.setRuntimeClasspath(main.getCompileClasspath()
                .plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));
        test.setCompileClasspath(test.getCompileClasspath()
                .plus(project.getConfigurations().getByName(Constants.CONFIG_MC_DEPENDENCIES)));

        Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        javadoc.setClasspath(main.getOutput().plus(main.getCompileClasspath()));
    }
}
