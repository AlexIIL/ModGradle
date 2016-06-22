package xyz.openmodloader.gradle.utils;

public enum  MojangConstants {

    LIBRARIES_BASE("https://libraries.minecraft.net/"),
    RESOURCES_BASE("http://resources.download.minecraft.net/"),
    DOWNLOAD_BASE("http://s3.amazonaws.com/Minecraft.Download/");

    private final String url;

    MojangConstants(String url) {
        this.url = url;
    }

    public String getURL(String path) {
        return this.url + path;
    }
}
