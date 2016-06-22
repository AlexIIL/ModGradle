package xyz.openmodloader.gradle.task.patches;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import xyz.openmodloader.gradle.util.Constants;

public class PatchMinecraftTask extends AbstractTask {
    @TaskAction
    public void patchMinecraft() {
        try {
            this.getLogger().lifecycle(":applying compile patches");
            int[] patches = {0, 0};
            if (Constants.COMPILE_PATCHES.exists()) {
                patches = this.applyPatches(Constants.COMPILE_PATCHES, Constants.MINECRAFT_SRC_PATCHED);
            }

            this.getLogger().lifecycle(":" + patches[0] + " failed patches, " + patches[1] + " successful patches");
            this.getLogger().lifecycle(":generating minecraft files");

            if (Constants.MINECRAFT_SOURCES.exists()) {
                FileUtils.deleteDirectory(Constants.MINECRAFT_SOURCES);
            }

            FileUtils.copyDirectory(Constants.MINECRAFT_SRC_PATCHED, Constants.MINECRAFT_SOURCES);

            File assets = new File(Constants.MINECRAFT_SOURCES, "assets");
            if (Constants.MINECRAFT_RESOURCES.exists()) {
                FileUtils.deleteDirectory(Constants.MINECRAFT_RESOURCES);
            }

            Constants.MINECRAFT_RESOURCES.mkdirs();

            FileUtils.moveDirectory(assets, new File(Constants.MINECRAFT_RESOURCES, "assets"));

            this.getLogger().lifecycle(":applying patches");
            patches = this.applyPatches(Constants.PATCHES, Constants.MINECRAFT_SOURCES);
            this.getLogger().lifecycle(":" + patches[0] + " failed patches, " + patches[1] + " successful patches");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] applyPatches(File input, File dest) {
        int successfulPatches = 0;
        int failedPatches = 0;
        try {
            for (Object obj : FileUtils.listFiles(input, new String[]{"patch"}, true)) {
                File file = (File) obj;
                String targetFile = file.getName().replaceAll("\\.patch", "");
                String targetPath = file.getParentFile().getAbsolutePath().replace(input.getAbsolutePath(), dest.getAbsolutePath());

                File target = new File(targetPath, targetFile);
                target.getParentFile().mkdirs();

                if (!target.exists()) {
                    this.getLogger().info(":could not find " + target.getAbsolutePath());
                    continue;
                }

                List<String> readFile = Files.readLines(file, Charsets.UTF_8);

                boolean preludeFound = false;
                for (int i = 0; i < Math.min(3, readFile.size()); i++) {
                    if (readFile.get(i).startsWith("+++")) {
                        preludeFound = true;
                        break;
                    }
                }

                if (!preludeFound) {
                    readFile.add(0, "+++");
                }

                try {
                    Patch<String> parsedPatch = DiffUtils.parseUnifiedDiff(readFile);
                    List<String> modifiedLines = DiffUtils.patch(Files.readLines(target, Charsets.UTF_8), parsedPatch);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(target));
                    for (String line : modifiedLines) {
                        bw.write(line);
                        bw.newLine();
                    }
                    bw.close();
                    successfulPatches++;
                } catch (PatchFailedException e) {
                    this.getLogger().lifecycle(":error applying patch for class " + file.getName().split("\\.")[0]);
                    this.getLogger().lifecycle(e.getMessage());
                    FileUtils.touch(new File(file.getAbsolutePath() + ".reject"));
                    FileUtils.writeStringToFile(new File(file.getAbsolutePath() + ".reject"), e.getMessage());
                    failedPatches++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[]{failedPatches, successfulPatches};
    }
}
