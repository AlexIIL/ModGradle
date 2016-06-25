package xyz.openmodloader.gradle.task;

import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.Constants;

import java.io.File;

public class MapJarsTask extends DefaultTask {

    @TaskAction
    public void mapJars() {
        ModGradleExtension extension = this.getProject().getExtensions().getByType(ModGradleExtension.class);
        if (!Constants.MINECRAFT_CLIENT_MAPPED_JAR.get(extension).exists() || !Constants.MINECRAFT_SERVER_MAPPED_JAR.get(extension).exists()) {
            this.getLogger().lifecycle(":remapping classes");
            mapJarFile(Constants.MINECRAFT_CLIENT_JAR.get(extension), Constants.MINECRAFT_CLIENT_MAPPED_JAR.get(extension), extension);
            mapJarFile(Constants.MINECRAFT_SERVER_JAR.get(extension), Constants.MINECRAFT_SERVER_MAPPED_JAR.get(extension), extension);
        }
    }

    public void mapJarFile(File input, File output, ModGradleExtension extension) {
        ExecResult result = getProject().javaexec(new Closure<JavaExecSpec>(this) {
            public JavaExecSpec call() {
                JavaExecSpec exec = (JavaExecSpec) getDelegate();
                exec.args(
                        Constants.SPECIALSOURCE_JAR.getAbsolutePath(),
                        "map",
                        "-i",
                        input.getAbsolutePath(),
                        "-m",
                        Constants.MAPPING_SRG.get(extension).getAbsolutePath(),
                        "-o",
                        output.getAbsolutePath()
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
            this.getLogger().error(":SpecialSource exit value: " + exitValue);
            throw new RuntimeException("SpecialSource failed to decompile");
        }
    }


}
