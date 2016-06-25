package xyz.openmodloader.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import xyz.openmodloader.gradle.task.*;

public class ModGradlePlugin extends AbstractPlugin {
    @Override
    public void apply(Project target) {
        super.apply(target);

        makeTask("download", DownloadTask.class);
        makeTask("mapJars", MapJarsTask.class).dependsOn("download");
        makeTask("mergeJars", MergeJarsTask.class).dependsOn("mapJars");
        makeTask("decompile", DecompileTask.class).dependsOn("mergeJars");
        makeTask("applyPatches", PatchMinecraftTask.class).dependsOn("decompile");
        makeTask("setupOML", DefaultTask.class).dependsOn("applyPatches");

        makeTask("genPatches", GenPatchesTask.class);

        makeTask("extractNatives", ExtractNativesTask.class).dependsOn("download");
        makeTask("genIdeaRuns", GenIdeaProjectTask.class).dependsOn("cleanIdea").dependsOn("idea").dependsOn("extractNatives");
    }
}
