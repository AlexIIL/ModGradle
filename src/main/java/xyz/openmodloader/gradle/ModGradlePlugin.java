package xyz.openmodloader.gradle;

import com.google.common.collect.ImmutableMap;

import xyz.openmodloader.gradle.tasks.download.ExtractNativesTask;
import xyz.openmodloader.gradle.tasks.idea.GenIdeaProjectTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.AbstractTask;

import xyz.openmodloader.gradle.tasks.DecompileTask;
import xyz.openmodloader.gradle.tasks.download.DownloadTask;
import xyz.openmodloader.gradle.tasks.patches.GenPatchesTask;
import xyz.openmodloader.gradle.tasks.patches.PatchMinecraftTask;

public class ModGradlePlugin implements Plugin<Project> {

    @Override
    public void apply (Project project) {
        project.apply(ImmutableMap.of("plugin", "java"));
        project.apply(ImmutableMap.of("plugin", "eclipse"));
        project.apply(ImmutableMap.of("plugin", "idea"));

        project.getExtensions().create("minecraft", MinecraftExtension.class, project);

        project.getTasks().create("download", DownloadTask.class);
        project.getTasks().create("decompile", DecompileTask.class).dependsOn("download");
        project.getTasks().create("applyPatches", PatchMinecraftTask.class).dependsOn("decompile");
        project.getTasks().create("setupOML", AbstractTask.class).dependsOn("applyPatches");

        project.getTasks().create("genPatches", GenPatchesTask.class);

        project.getTasks().create("extractNatives", ExtractNativesTask.class).dependsOn("download");
        project.getTasks().create("genIdeaRuns", GenIdeaProjectTask.class).dependsOn("cleanIdea").dependsOn("idea").dependsOn("extractNatives");
    }
}
