package xyz.openmodloader.gradle.util;

import java.io.File;
import java.util.function.Function;

import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.delayed.DelayedFile;
import xyz.openmodloader.gradle.util.delayed.IDelayed;

public class Constants {
    public static final File WORKING_DIRECTORY = new File(".");
    public static final File CACHE_FILES = new File(WORKING_DIRECTORY, ".gradle/minecraft");

    public static final IDelayed<File> MINECRAFT_CLIENT_JAR = new DelayedFile(extension -> new File(CACHE_FILES, extension.version + "-client.jar"));
    public static final IDelayed<File> MINECRAFT_SERVER_JAR = new DelayedFile(extension -> new File(CACHE_FILES, extension.version + "-server.jar"));

    public static final File SPECIALSOURCE_JAR = new File(CACHE_FILES, "SpecialSource.jar");
    public static final File FERNFLOWER_JAR = new File(CACHE_FILES, "fernflower-2.0-SNAPSHOT.jar");

    public static final File MINECRAFT_MAPPED_CLIENT = new File(CACHE_FILES, "minecraft-client-mapped.jar");
    public static final File MINECRAFT_MAPPED_SERVER = new File(CACHE_FILES, "minecraft-server-mapped.jar");
    public static final IDelayed<File> MINECRAFT_MAPPED = new DelayedFile(extension -> new File(CACHE_FILES, "minecraft-" + extension.version + "-mapped"));

    public static final IDelayed<File> MAPPING_SRG = new DelayedFile(extension -> new File(CACHE_FILES, extension.version + "-mappings.srg"));

    public static final File MINECRAFT_SRC_DECOMP = new File(CACHE_FILES, "srcDecomp");
    public static final File MINECRAFT_SRC_PATCHED = new File(CACHE_FILES, "srcPatched");

    public static final File MINECRAFT_LIBS = new File(CACHE_FILES, "libs");
    public static final File MINECRAFT_NATIVES = new File(CACHE_FILES, "natives");
    public static final IDelayed<File> MINECRAFT_JSON = new DelayedFile(extension -> new File(CACHE_FILES, extension.version + "-info.json"));

    public static final File MINECRAFT_ROOT = new File(WORKING_DIRECTORY, "minecraft");
    public static final File MINECRAFT_MAIN = new File(MINECRAFT_ROOT, "src" + File.separator + "main");
    public static final File MINECRAFT_SOURCES = new File(MINECRAFT_MAIN, "java");
    public static final File MINECRAFT_RESOURCES = new File(MINECRAFT_MAIN, "resources");

    public static final File COMPILE_PATCHES = new File(WORKING_DIRECTORY, "compilePatches");
    public static final File PATCHES = new File(WORKING_DIRECTORY, "patches");

    public static final File VERSION_MANIFEST = new File(CACHE_FILES, "version_manifest.json");

    public static final String LIBRARIES_BASE = "https://libraries.minecraft.net/";
    public static final String RESOURCES_BASE = "http://resources.download.minecraft.net/";
    public static final String DOWNLOAD_BASE = "http://s3.amazonaws.com/Minecraft.Download/";
}
