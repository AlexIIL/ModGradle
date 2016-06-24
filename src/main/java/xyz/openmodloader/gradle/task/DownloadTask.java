package xyz.openmodloader.gradle.task;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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
import java.util.Map;
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

                Optional<ManifestVersion.Versions> optionalVersion = mcManifest.versions.stream().filter(versions -> versions.id.equalsIgnoreCase(extension.version)).findFirst();
                if (optionalVersion.isPresent()) {
                    FileUtils.copyURLToFile(new URL(optionalVersion.get().url), Constants.MINECRAFT_JSON.get(extension));
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
                FileUtils.copyURLToFile(new URL("http://maven.ilexiconn.net/fernflower.jar"), Constants.FERNFLOWER_JAR);
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

            File assets = new File(Constants.CACHE_FILES, "assets");
            if (!assets.exists()) {
                assets.mkdirs();
            }

            File assetsInfo = new File(assets, "indexes" + File.separator + assetIndex.id + ".json");
            if (!assetsInfo.exists() || !Checksum.equals(assetsInfo, assetIndex.sha1)) {
                this.getLogger().lifecycle(":downloading asset index");
                FileUtils.copyURLToFile(new URL(assetIndex.url), assetsInfo);
            }

            Map<String, JsonObject> map = new Gson().fromJson(new FileReader(assetsInfo), new TypeToken<Map<String, JsonObject>>(){}.getType());
            JsonObject parent = map.get("objects").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : parent.entrySet()) {
                JsonObject object = entry.getValue().getAsJsonObject();
                String sha1 = object.get("hash").getAsString();
                File file = new File(assets, "objects" + File.separator + sha1.substring(0, 2) + File.separator + sha1);
                if (!file.exists() || !Checksum.equals(file, sha1)) {
                    this.getLogger().lifecycle(":downloading asset " + entry.getKey());
                    FileUtils.copyURLToFile(new URL(Constants.RESOURCES_BASE + sha1.substring(0, 2) + "/" + sha1), file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
