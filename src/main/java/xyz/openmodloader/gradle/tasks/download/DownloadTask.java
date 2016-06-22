package xyz.openmodloader.gradle.tasks.download;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import xyz.openmodloader.gradle.MinecraftExtension;
import xyz.openmodloader.gradle.utils.Checksum;
import xyz.openmodloader.gradle.utils.FileLocations;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

public class DownloadTask extends AbstractTask {

    @TaskAction
    public void download() {
        try {
            if (!FileLocations.minecraftJson.exists()) {
                getLogger().lifecycle("Downloading Minecraft json");
                FileUtils.copyURLToFile(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), FileLocations.VERSION_MANIFEST);
                ManifestVersion mcManifest = new GsonBuilder().create().fromJson(FileUtils.readFileToString(FileLocations.VERSION_MANIFEST), ManifestVersion.class);

                Optional<ManifestVersion.Versions> v = mcManifest.versions.stream().filter(versions -> versions.id.equalsIgnoreCase(MinecraftExtension.version)).findFirst();
                if (v.isPresent())
                    FileUtils.copyURLToFile(new URL(v.get().url), FileLocations.minecraftJson);
                else
                    System.out.println("ERROR: Getting Minecraft JSON");
            }

            Gson gson = new Gson();
            Version version = gson.fromJson(new FileReader(FileLocations.minecraftJson), Version.class);

            if (!FileLocations.MINECRAFT_CLIENT_JAR.exists() || !Checksum.sameChecksum(FileLocations.MINECRAFT_CLIENT_JAR, version.downloads.get("client").sha1)) {
                getLogger().lifecycle("Downloading Minecraft Client");
                FileUtils.copyURLToFile(new URL(version.downloads.get("client").url), FileLocations.MINECRAFT_CLIENT_JAR);
            }
            if (!FileLocations.MINECRAFT_SERVER_JAR.exists() || !Checksum.sameChecksum(FileLocations.MINECRAFT_SERVER_JAR, version.downloads.get("server").sha1)) {
                getLogger().lifecycle("Downloading Minecraft Server");
                FileUtils.copyURLToFile(new URL(version.downloads.get("server").url), FileLocations.MINECRAFT_SERVER_JAR);
            }
            if (!FileLocations.SPECIALSOURCE_JAR.exists()|| !Checksum.sameChecksum(FileLocations.SPECIALSOURCE_JAR, "a97de52504c52a36ff72ae00168427850843628b")) {
                getLogger().lifecycle("Downloading Special Source");
                FileUtils.copyURLToFile(new URL("https://modmuss50.me/files/grass/SpecialSource.jar"), FileLocations.SPECIALSOURCE_JAR);
            }
            if (!FileLocations.FERNFLOWER_JAR.exists()||!Checksum.sameChecksum(FileLocations.FERNFLOWER_JAR, "b48932fc7ceb3dbd8b79a36e2d0c882496479a0e")) {
                getLogger().lifecycle("Downloading FernFlower");
                FileUtils.copyURLToFile(new URL("https://modmuss50.me/files/grass/fernflower-2.0-SNAPSHOT.jar"), FileLocations.FERNFLOWER_JAR);
            }
            if (!FileLocations.MAPPING_SRG.exists()) {
                getLogger().lifecycle("Downloading Minecraft Mappings");
                FileUtils.copyURLToFile(new URL("http://modmuss50.me/files/grass/mappings/" + MinecraftExtension.version + "-obf2mcp.srg"), FileLocations.MAPPING_SRG);
            }

            for (Version.Library library : version.libraries) {
                if (library.allowed() && library.getFile() != null) {
                    if (!library.getFile().exists() || !Checksum.sameChecksum(library.getFile(), library.getSha1())) {
                        getLogger().lifecycle("Downloading " + library.getURL());
                        FileUtils.copyURLToFile(new URL(library.getURL()), library.getFile());
                    }
                }
            }

            Version.AssetIndex assetIndex = version.assetIndex;

            File assets = new File(FileLocations.workingMcResources, "assets");
            if(!assets.exists()){
                assets.mkdirs();
            }
            File assetsInfo = new File(assets, assetIndex.id + "-assets.json");
            if(!assetsInfo.exists() || !Checksum.sameChecksum(assetsInfo, assetIndex.sha1)){
                FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
