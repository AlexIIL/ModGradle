package xyz.openmodloader.gradle.task;

import groovy.lang.Closure;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import org.zeroturnaround.zip.ZipUtil;
import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.Constants;

import java.io.IOException;

public class DecompileTask extends DefaultTask {
    @TaskAction
    public void decompile() {
        try {
            ModGradleExtension extension = this.getProject().getExtensions().getByType(ModGradleExtension.class);
            if (!Constants.MINECRAFT_FERN_OUTPUT_JAR.get(extension).exists()) {
                this.getLogger().lifecycle(":decompiling Minecraft");

                // TODO: Separate task
                ExecResult result = getProject().javaexec(new Closure<JavaExecSpec>(this) {
                    public JavaExecSpec call() {
                        JavaExecSpec exec = (JavaExecSpec) getDelegate();
                        Constants.MINECRAFT_FERN_OUTPUT.mkdir();
                        exec.args(
                                Constants.FERNFLOWER_JAR.getAbsolutePath(),
                                "-dgs=1",
                                "-hdc=0",
                                "-asc=1",
                                "-udv=0",
                                "-din=1",
                                "-rbr=0",
                                "-rsy=1",
                                "-ind=    ",
                                //"-udv=1",
                                //"-log=ERROR", //
                                Constants.MINECRAFT_MERGED.get(extension).getAbsolutePath(),
                                Constants.MINECRAFT_FERN_OUTPUT.getAbsolutePath()
                        );
                        exec.setMain("-jar");
                        exec.setWorkingDir(Constants.CACHE_FILES);
                        exec.classpath(Constants.getClassPath());
                        //exec.setStandardOutput(System.out); // TODO: store the logs?
                        exec.setMaxHeapSize("512M");

                        return exec;
                    }

                    public JavaExecSpec call(Object obj) {
                        return call();
                    }
                });

                int exitValue = result.getExitValue();
                if (exitValue != 0) {
                    this.getLogger().error(":FernFlower exit value: " + exitValue);
                    throw new RuntimeException("FernFlower failed to decompile");
                }
            }

            if (Constants.MINECRAFT_SRC_PATCHED.exists()) {
                FileUtils.deleteDirectory(Constants.MINECRAFT_SRC_PATCHED);
            }

            if (Constants.MINECRAFT_SRC_DECOMP.exists()) {
                FileUtils.deleteDirectory(Constants.MINECRAFT_SRC_DECOMP);
            }

            ZipUtil.unpack(Constants.MINECRAFT_FERN_OUTPUT_JAR.get(extension), Constants.MINECRAFT_SRC_DECOMP);
            FileUtils.copyDirectory(Constants.MINECRAFT_SRC_DECOMP, Constants.MINECRAFT_SRC_PATCHED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}