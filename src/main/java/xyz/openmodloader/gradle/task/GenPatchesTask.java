package xyz.openmodloader.gradle.task;

import com.cloudbees.diff.Diff;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.Constants;

import java.io.*;

public class GenPatchesTask extends DefaultTask {
    @TaskAction
    public void genPatches() {
        try {
            ModGradleExtension extension = this.getProject().getExtensions().getByType(ModGradleExtension.class);

            if (extension.genCompilePatches) {
                this.getLogger().lifecycle(":generating compile patches");
                for (Object obj : FileUtils.listFiles(Constants.MINECRAFT_SRC_PATCHED, new String[]{"java"}, true)) {
                    File input = (File) obj;
                    String targetOldPath = input.getParentFile().getAbsolutePath().replace(Constants.MINECRAFT_SRC_PATCHED.getAbsolutePath(), Constants.MINECRAFT_SRC_DECOMP.getAbsolutePath());
                    String relative = input.getParentFile().getAbsolutePath().replace(Constants.MINECRAFT_SRC_PATCHED.getAbsolutePath(), "").replace("\\", "/");
                    File original = new File(targetOldPath, input.getName());
                    this.processFile(relative, new FileInputStream(original), new FileInputStream(input), input.getName(), "CompilePatches");
                }
            }

            this.getLogger().lifecycle(":generating patches");
            for (Object obj : FileUtils.listFiles(Constants.MINECRAFT_SOURCES, new String[]{"java"}, true)) {
                File input = (File) obj;
                String targetOldPath = input.getParentFile().getAbsolutePath().replace(Constants.MINECRAFT_SOURCES.getAbsolutePath(), Constants.MINECRAFT_SRC_PATCHED.getAbsolutePath());
                String relative = input.getParentFile().getAbsolutePath().replace(Constants.MINECRAFT_SOURCES.getAbsolutePath(), "").replace("\\", "/");
                File original = new File(targetOldPath, input.getName());
                this.processFile(relative, new FileInputStream(original), new FileInputStream(input), input.getName(), "patches");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFile(String relative, InputStream original, InputStream changed, String name, String patchDir) throws IOException {
        File patchFile = new File(new File(Constants.WORKING_DIRECTORY, patchDir), relative + "/" + name + ".patch").getCanonicalFile();

        if (changed == null) {
            this.getLogger().error(":changed file does not exist");
            return;
        } else if (original == null) {
            this.getLogger().error(":original file does not exist");
            return;
        }

        byte[] oData = ByteStreams.toByteArray(original);
        byte[] cData = ByteStreams.toByteArray(changed);

        Diff diff = Diff.diff(new InputStreamReader(new ByteArrayInputStream(oData), Charsets.UTF_8), new InputStreamReader(new ByteArrayInputStream(cData), Charsets.UTF_8), false);

        if (!diff.isEmpty()) {
            String newPatch = diff.toUnifiedDiff(relative, relative, new InputStreamReader(new ByteArrayInputStream(oData), Charsets.UTF_8), new InputStreamReader(new ByteArrayInputStream(cData), Charsets.UTF_8), 3);
            String oldDiff = "";
            if (patchFile.exists()) {
                oldDiff = Files.toString(patchFile, Charsets.UTF_8);
            }

            if (!oldDiff.equals(newPatch)) {
                this.getLogger().info(":writing patch: " + patchFile);
                patchFile.getParentFile().mkdirs();
                Files.touch(patchFile);
                Files.write(newPatch, patchFile, Charsets.UTF_8);
            } else {
                this.getLogger().info(":patch did not change");
            }
        }
    }
}
