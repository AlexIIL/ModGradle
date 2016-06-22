package xyz.openmodloader.gradle.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.zeroturnaround.zip.ZipUtil;

import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.Constants;

public class DecompileTask extends AbstractTask {
    @TaskAction
    public void decompile() {
        try {
            ModGradleExtension extension = this.getProject().getExtensions().getByType(ModGradleExtension.class);

            if (!Constants.MINECRAFT_MAPPED_CLIENT.exists()) {
                if (Constants.MAPPING_SRG.get(extension).exists()) {
                    this.getLogger().lifecycle(":remapping classes");

                    Process process = Runtime.getRuntime().exec("java -jar " + Constants.SPECIALSOURCE_JAR.getAbsolutePath() + " map -i " + Constants.MINECRAFT_CLIENT_JAR.get(extension).getAbsolutePath() + " -m " + Constants.MAPPING_SRG.get(extension).getAbsolutePath() + " -o" + Constants.MINECRAFT_MAPPED_CLIENT.getAbsolutePath());
                    InputStream stdin = process.getInputStream();
                    InputStreamReader streamReader = new InputStreamReader(stdin);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        this.getLogger().info(line);
                    }
                    int exitValue = process.waitFor();
                    if (exitValue != 0) {
                        this.getLogger().info(":specialsource exit value: " + exitValue);
                        throw new RuntimeException("SpecialSource failed to decompile");
                    }
                }
            }

            if (!Constants.MINECRAFT_MAPPED.get(extension).exists()) {
                this.getLogger().lifecycle(":unpacking minecraft jar");

                ZipUtil.unpack(Constants.MINECRAFT_MAPPED_CLIENT, Constants.MINECRAFT_MAPPED.get(extension), name -> {
                    if (name.startsWith("net/minecraft") || name.startsWith("assets") || name.startsWith("log4j2.xml") || name.endsWith(".class")) {
                        return name;
                    } else {
                        return null;
                    }
                });
            }

            if (!Constants.MINECRAFT_SRC_DECOMP.exists()) {
                Constants.MINECRAFT_SRC_DECOMP.mkdir();

                this.getLogger().lifecycle(":decompiling minecraft");

                Process process = Runtime.getRuntime().exec("java -jar " + Constants.FERNFLOWER_JAR.getAbsolutePath() + " -dgs=1 -hdc=0 -asc=1 -udv=0 -din=1 -rbr=0 -rsy=1 " + Constants.MINECRAFT_MAPPED.get(extension).getAbsolutePath() + " " + Constants.MINECRAFT_SRC_DECOMP.getAbsolutePath());
                InputStream stdin = process.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(stdin);
                BufferedReader bufferedReader = new BufferedReader(streamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    this.getLogger().info(line);
                }
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    this.getLogger().info(":fernflower exit value: " + exitValue);
                    throw new RuntimeException("Fernflower failed to decompile");
                }
            }

            if (Constants.MINECRAFT_SRC_PATCHED.exists()) {
                FileUtils.deleteDirectory(Constants.MINECRAFT_SRC_PATCHED);
            }

            FileUtils.copyDirectory(Constants.MINECRAFT_SRC_DECOMP, Constants.MINECRAFT_SRC_PATCHED);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}