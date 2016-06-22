package xyz.openmodloader.gradle.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.zeroturnaround.zip.ZipUtil;

import xyz.openmodloader.gradle.utils.FileLocations;

public class DecompileTask extends AbstractTask {


    @TaskAction
    public void decompile () {
        try {
            if (!FileLocations.MINECRAFT_MAPPED_CLIENT.exists()) {
                if (FileLocations.MAPPING_SRG.exists()) {
                    System.out.println("Applying client class mappings");
                    Process proc = Runtime.getRuntime().exec("java -jar " + FileLocations.SPECIALSOURCE_JAR.getAbsolutePath() + " map -i " + FileLocations.MINECRAFT_CLIENT_JAR + " -m " + FileLocations.MAPPING_SRG.getAbsolutePath() + " -o" + FileLocations.MINECRAFT_MAPPED_CLIENT.getAbsolutePath());
                    InputStream stdin = proc.getInputStream();
                    InputStreamReader isr = new InputStreamReader(stdin);
                    BufferedReader br = new BufferedReader(isr);

                    String line;
                    while ((line = br.readLine()) != null)
                        getLogger().info(line);
                    int exitVal = proc.waitFor();
                    if (exitVal != 0) {
                        getLogger().error("SpecialSource exitValue: " + exitVal);
                        throw new RuntimeException("SpecialSource failed to decompile");
                    }
                }
            }
            if (!FileLocations.MINECRAFT_MAPPED.exists()) {
                System.out.println("Unpacking minecraft classes");
                ZipUtil.unpack(FileLocations.MINECRAFT_MAPPED_CLIENT, FileLocations.MINECRAFT_MAPPED, name -> {
                    if (name.startsWith("net/minecraft") || name.startsWith("assets") || name.startsWith("log4j2.xml") || name.endsWith(".class"))
                        return name;
                    else
                        return null;

                });
            }

            if (!FileLocations.MINECRAFT_SRC_DECOMP.exists()) {

                FileLocations.MINECRAFT_SRC_DECOMP.mkdir();

                System.out.println("Decompiling minecraft");

                Process proc = Runtime.getRuntime().exec("java -jar " + FileLocations.FERNFLOWER_JAR.getAbsolutePath() + " -dgs=1 -hdc=0 -asc=1 -udv=0 -din=1 -rbr=0 -rsy=1 " + FileLocations.MINECRAFT_MAPPED.getAbsolutePath() + " " + FileLocations.MINECRAFT_SRC_DECOMP.getAbsolutePath());
                InputStream stdin = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);

                String line;
                while ((line = br.readLine()) != null)
                    getLogger().info(line);
                int exitVal = proc.waitFor();
                if (exitVal != 0) {
                    getLogger().error("Fernflower exitValue: " + exitVal);
                    throw new RuntimeException("Fernflower failed to decompile");
                }

            }
            if (FileLocations.MINECRAFT_SRC_PATCHED.exists())
                FileUtils.deleteDirectory(FileLocations.MINECRAFT_SRC_PATCHED);

            FileUtils.copyDirectory(FileLocations.MINECRAFT_SRC_DECOMP, FileLocations.MINECRAFT_SRC_PATCHED);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}