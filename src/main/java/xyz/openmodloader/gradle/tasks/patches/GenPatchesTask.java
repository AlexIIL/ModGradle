package xyz.openmodloader.gradle.tasks.patches;

import com.cloudbees.diff.Diff;
import com.cloudbees.diff.Hunk;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import xyz.openmodloader.gradle.utils.FileLocations;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;

public class GenPatchesTask extends AbstractTask {

    @TaskAction
    public void genPatches() {
        try {
            getLogger().lifecycle("Patch pass 1/2");
            for (Object i : FileUtils.listFiles(FileLocations.MINECRAFT_SRC_PATCHED, new String[]{"java"}, true)) {
                File input = (File) i;
                String targetOldPath = input.getParentFile().getAbsolutePath().replace(FileLocations.MINECRAFT_SRC_PATCHED.getAbsolutePath(), FileLocations.MINECRAFT_SRC_DECOMP.getAbsolutePath());
                String relative = input.getParentFile().getAbsolutePath().replace(FileLocations.MINECRAFT_SRC_PATCHED.getAbsolutePath(), "");
                File original = new File(targetOldPath, input.getName());
                processFile(relative, new FileInputStream(original), new FileInputStream(input), input.getName(), "CompilePatches");
            }

            getLogger().lifecycle("Pass 2/2");
            for (Object i : FileUtils.listFiles(FileLocations.workingMcSource, new String[]{"java"}, true)) {
                File input = (File) i;
                String targetOldPath = input.getParentFile().getAbsolutePath().replace(FileLocations.workingMcSource.getAbsolutePath(), FileLocations.MINECRAFT_SRC_PATCHED.getAbsolutePath());
                String relative = input.getParentFile().getAbsolutePath().replace(FileLocations.workingMcSource.getAbsolutePath(), "");
                File original = new File(targetOldPath, input.getName());
                processFile(relative, new FileInputStream(original), new FileInputStream(input), input.getName(), "patches");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processFile(String relative, InputStream original, InputStream changed, String name, String patchDir) throws IOException {
        File patchFile = new File(new File(FileLocations.WORKING_DIRECTORY, patchDir), relative + "/" + name + ".patch").getCanonicalFile();

        if (changed == null) {
            System.out.println("Changed File does not exist");
            return;
        }

        if (original == null)
            throw new IllegalArgumentException("Original data for " + relative + " is null");

        // We have to cache the bytes because diff reads the stream twice.. why.. who knows.
        byte[] oData = ByteStreams.toByteArray(original);
        byte[] cData = ByteStreams.toByteArray(changed);

        Diff diff = Diff.diff(new InputStreamReader(new ByteArrayInputStream(oData), Charsets.UTF_8), new InputStreamReader(new ByteArrayInputStream(cData), Charsets.UTF_8), false);

        if (!relative.startsWith("/"))
            relative = "/" + relative;

        if (!diff.isEmpty()) {
            String unidiff = diff.toUnifiedDiff(relative, relative,
                    new InputStreamReader(new ByteArrayInputStream(oData), Charsets.UTF_8),
                    new InputStreamReader(new ByteArrayInputStream(cData), Charsets.UTF_8), 3);
            unidiff = unidiff.replace("\r\n", "\n"); //Normalize lines
            unidiff = unidiff.replace("\n" + Hunk.ENDING_NEWLINE + "\n", "\n"); //We give 0 shits about this.

            String olddiff = "";
            if (patchFile.exists()) {
                olddiff = Files.toString(patchFile, Charsets.UTF_8);
            }

            if (!olddiff.equals(unidiff)) {
                System.out.println("Writing patch: " + patchFile);
                patchFile.getParentFile().mkdirs();
                Files.touch(patchFile);
                Files.write(unidiff, patchFile, Charsets.UTF_8);
            } else {
                System.out.println("Patch did not change");
            }
        }
    }
}
