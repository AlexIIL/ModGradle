package xyz.openmodloader.gradle.tasks.patches;

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
import xyz.openmodloader.gradle.utils.FileLocations;

public class PatchMinecraftTask extends AbstractTask {

    private int succesfullPatches = 0;
    private int failedPatches = 0;

    @TaskAction
    public void patchMinecraft () {
        try {

            System.out.println("Applying compile patches");
            if (FileLocations.compilePatches.exists())
                applyPatches(FileLocations.compilePatches, FileLocations.MINECRAFT_SRC_PATCHED);

            System.out.println(failedPatches + " Failed : " + succesfullPatches + " Successfull Patches");

            System.out.println("Creating work dir's");


            if (FileLocations.workingMcSource.exists())
                FileUtils.deleteDirectory(FileLocations.workingMcSource);

            FileUtils.copyDirectory(FileLocations.MINECRAFT_SRC_PATCHED, FileLocations.workingMcSource);

            File assets = new File(FileLocations.workingMcSource, "assets");
            if(FileLocations.workingMcResources.exists()){
                FileUtils.deleteDirectory(FileLocations.workingMcResources);
            }

            FileLocations.workingMcResources.mkdirs();

            FileUtils.moveDirectory(assets, new File(FileLocations.workingMcResources, "assets"));

            System.out.println("Applying patches");
            failedPatches = 0;
            succesfullPatches = 0;

            applyPatches(FileLocations.patches, FileLocations.workingMcSource);

            System.out.println(failedPatches + " Failed : " + succesfullPatches + " Successfull Patches");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void applyPatches (File input, File dest) {
        try {
            for (Object f : FileUtils.listFiles(input, new String[]{"patch"}, true)) {
                File file = (File) f;
                String targetFile = file.getName().replaceAll("\\.patch", "");
                String targetPath = file.getParentFile().getAbsolutePath().replace(input.getAbsolutePath(), dest.getAbsolutePath());


                File patchFile = file;
                File target = new File(targetPath, targetFile);
                target.getParentFile().mkdirs();

                if (!target.exists()) {
                    System.out.println("could not find " + target.getAbsolutePath());
                    continue;
                }

                List<String> readFile = Files.readLines(patchFile, Charsets.UTF_8);

                boolean preludeFound = false;
                for (int i = 0; i < Math.min(3, readFile.size()); i++) {
                    if (readFile.get(i).startsWith("+++")) {
                        preludeFound = true;
                        break;
                    }
                }
                if (!preludeFound)
                    readFile.add(0, "+++");


                try {
                    Patch<String> parsedPatch = DiffUtils.parseUnifiedDiff(readFile);
                    List<String> modifiedLines = DiffUtils.patch(Files.readLines(target, Charsets.UTF_8), parsedPatch);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(target));
                    for (String line : modifiedLines) {
                        bw.write(line);
                        bw.newLine();
                    }
                    bw.close();
                    succesfullPatches++;
                } catch (PatchFailedException e) {
                    System.out.println("Error Applying " + file.getName());
                    System.out.println(e.getMessage());
                    FileUtils.touch(new File(patchFile.getAbsolutePath() + ".reject"));
                    FileUtils.writeStringToFile(new File(patchFile.getAbsolutePath() + ".reject"), e.getMessage());
                    failedPatches++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
