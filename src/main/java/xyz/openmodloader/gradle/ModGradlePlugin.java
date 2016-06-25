package xyz.openmodloader.gradle;

import com.google.common.collect.ImmutableMap;

import org.gradle.api.plugins.JavaPlugin;
import xyz.openmodloader.gradle.task.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.AbstractTask;

public class ModGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.apply(ImmutableMap.of("plugin", "java"));
        project.apply(ImmutableMap.of("plugin", "eclipse"));
        project.apply(ImmutableMap.of("plugin", "idea"));

        project.getExtensions().create("minecraft", ModGradleExtension.class);

        project.getTasks().create("download", DownloadTask.class);
        project.getTasks().create("map", MapJarsTask.class).dependsOn("download");
        project.getTasks().create("mergeJars", MergeJarsTask.class).dependsOn("map");
        project.getTasks().create("decompile", DecompileTask.class).dependsOn("mergeJars");
        project.getTasks().create("applyPatches", PatchMinecraftTask.class).dependsOn("decompile");
        project.getTasks().create("setupOML", AbstractTask.class).dependsOn("applyPatches");

        project.getTasks().create("genPatches", GenPatchesTask.class);

        project.getTasks().create("extractNatives", ExtractNativesTask.class).dependsOn("download");
        project.getTasks().create("genIdeaRuns", GenIdeaProjectTask.class).dependsOn("cleanIdea").dependsOn("idea").dependsOn("extractNatives");

        project.getDependencies().add(JavaPlugin.COMPILE_CONFIGURATION_NAME, project.fileTree("libs"));
    }
}
