package xyz.openmodloader.gradle;

import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

public class MinecraftExtension {

    @Input
    public static String version;
    @Input
    public static String mappings;
    @Input
    public static String mappingsGroup;
    @Input
    public static boolean genCompilePatches = false;
    @Input
    public static String runDir = "run";

    public MinecraftExtension () {
    }

    public MinecraftExtension (Project plugin) {
        //For some reason Gradle forces this constructor
    }

    public void project (Closure<MinecraftExtension> closure) {
        MinecraftExtension project = new MinecraftExtension();
        ConfigureUtil.configure(closure, project);
    }
}
