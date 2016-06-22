package xyz.openmodloader.gradle.task.download;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.Checksum;
import xyz.openmodloader.gradle.util.Constants;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import xyz.openmodloader.gradle.util.ManifestVersion;
import xyz.openmodloader.gradle.util.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class DownloadTask extends AbstractTask {
    @TaskAction
    public void download() {
        try {
            ModGradleExtension extension = this.getProject().getExtensions().getByType(ModGradleExtension.class);

            if (!Constants.MINECRAFT_JSON.get(extension).exists()) {
                this.getLogger().lifecycle(":downloading minecraft json");
                FileUtils.copyURLToFile(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), Constants.VERSION_MANIFEST);
                ManifestVersion mcManifest = new GsonBuilder().create().fromJson(FileUtils.readFileToString(Constants.VERSION_MANIFEST), ManifestVersion.class);

                Optional<ManifestVersion.Versions> v = mcManifest.versions.stream().filter(versions -> versions.id.equalsIgnoreCase(extension.version)).findFirst();
                if (v.isPresent()) {
                    FileUtils.copyURLToFile(new URL(v.get().url), Constants.MINECRAFT_JSON.get(extension));
                } else {
                    this.getLogger().info(":failed downloading minecraft json");
                    throw new RuntimeException("Failed downloading Minecraft json");
                }
            }

            Gson gson = new Gson();
            Version version = gson.fromJson(new FileReader(Constants.MINECRAFT_JSON.get(extension)), Version.class);

            if (!Constants.MINECRAFT_CLIENT_JAR.get(extension).exists() || !Checksum.equals(Constants.MINECRAFT_CLIENT_JAR.get(extension), version.downloads.get("client").sha1)) {
                this.getLogger().lifecycle(":downloading client");
                FileUtils.copyURLToFile(new URL(version.downloads.get("client").url), Constants.MINECRAFT_CLIENT_JAR.get(extension));
            }

            if (!Constants.MINECRAFT_SERVER_JAR.get(extension).exists() || !Checksum.equals(Constants.MINECRAFT_SERVER_JAR.get(extension), version.downloads.get("server").sha1)) {
                this.getLogger().lifecycle(":downloading server");
                FileUtils.copyURLToFile(new URL(version.downloads.get("server").url), Constants.MINECRAFT_SERVER_JAR.get(extension));
            }

            if (!Constants.SPECIALSOURCE_JAR.exists() || !Checksum.equals(Constants.SPECIALSOURCE_JAR, "a97de52504c52a36ff72ae00168427850843628b")) {
                this.getLogger().lifecycle(":downloading specialsource");
                FileUtils.copyURLToFile(new URL("https://modmuss50.me/files/grass/SpecialSource.jar"), Constants.SPECIALSOURCE_JAR);
            }

            if (!Constants.FERNFLOWER_JAR.exists() || !Checksum.equals(Constants.FERNFLOWER_JAR, "b48932fc7ceb3dbd8b79a36e2d0c882496479a0e")) {
                this.getLogger().lifecycle(":downloading fernflower");
                FileUtils.copyURLToFile(new URL("https://modmuss50.me/files/grass/fernflower-2.0-SNAPSHOT.jar"), Constants.FERNFLOWER_JAR);
            }

            if (!Constants.MAPPING_SRG.get(extension).exists()) {
                this.getLogger().lifecycle(":downloading mappings");
                FileUtils.copyURLToFile(new URL("http://modmuss50.me/files/grass/mappings/" + extension.version + "-obf2mcp.srg"), Constants.MAPPING_SRG.get(extension));
            }

            for (Version.Library library : version.libraries) {
                if (library.allowed() && library.getFile() != null) {
                    if (!library.getFile().exists() || !Checksum.equals(library.getFile(), library.getSha1())) {
                        this.getLogger().lifecycle(":downloading " + library.getURL());
                        FileUtils.copyURLToFile(new URL(library.getURL()), library.getFile());
                    }
                }
            }

            Version.AssetIndex assetIndex = version.assetIndex;

            File assets = new File(Constants.MINECRAFT_RESOURCES, "assets");
            if (!assets.exists()) {
                assets.mkdirs();
            }

            File assetsInfo = new File(assets, assetIndex.id + "-assets.json");
            if (!assetsInfo.exists() || !Checksum.equals(assetsInfo, assetIndex.sha1)) {
                FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
