package xyz.openmodloader.gradle.utils;

import java.io.File;

import xyz.openmodloader.gradle.MinecraftExtension;

public class FileLocations {

    public static final File WORKING_DIRECTORY = new File(".");
    public static final File CACHE_FILES = new File(WORKING_DIRECTORY, ".gradle/minecraft");

    public static final File MINECRAFT_CLIENT_JAR = new File(CACHE_FILES, MinecraftExtension.version + "-client.jar");
    public static final File MINECRAFT_SERVER_JAR = new File(CACHE_FILES, MinecraftExtension.version + "-server.jar");

    public static final File SPECIALSOURCE_JAR = new File(CACHE_FILES, "SpecialSource.jar");
    public static final File FERNFLOWER_JAR = new File(CACHE_FILES, "fernflower-2.0-SNAPSHOT.jar");

    public static final File MINECRAFT_MAPPED_CLIENT = new File(CACHE_FILES, "minecraft-client-mapped.jar");
    public static final File MINECRAFT_MAPPED_SERVER = new File(CACHE_FILES, "minecraft-server-mapped.jar");
    public static final File MINECRAFT_MAPPED = new File(CACHE_FILES, "minecraft-"+MinecraftExtension.version + "-mapped");

    public static final File MAPPING_SRG = new File(CACHE_FILES, MinecraftExtension.version + "-mappings.srg");

    public static final File MINECRAFT_SRC_DECOMP = new File(CACHE_FILES, "srcDecomp");
    public static final File MINECRAFT_SRC_PATCHED = new File(CACHE_FILES, "srcPatched");

    public static final File MINECRAFT_LIBS = new File(CACHE_FILES, "libs");
    public static final File MINECRAFT_NATIVES = new File(CACHE_FILES, "natives");
    public static final File minecraftJson = new File(CACHE_FILES, MinecraftExtension.version + "-info.json");

    public static final File workingDir = new File(WORKING_DIRECTORY, "work");
    public static final File workingMcSource = new File(workingDir, "src");
    /**
     * Fixes errors that happen when decompiling
     */
    public static final File compilePatches = new File(WORKING_DIRECTORY, "compilePatches");

    /**
     * The patches that get added to the java files after all compile errors have been fixed
     */
    public static final File patches = new File(WORKING_DIRECTORY, "patches");

    public static final File VERSION_MANIFEST = new File(CACHE_FILES, "version_manifest.json");
}
