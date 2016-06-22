package xyz.openmodloader.gradle.task;

import com.google.gson.Gson;
import xyz.openmodloader.gradle.ModGradleExtension;
import xyz.openmodloader.gradle.util.Constants;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.zeroturnaround.zip.ZipUtil;
import xyz.openmodloader.gradle.util.Version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ExtractNativesTask extends AbstractTask {
    @TaskAction
    public void extractNatives() throws FileNotFoundException {
        ModGradleExtension extension = this.getProject().getExtensions().getByType(ModGradleExtension.class);
        Gson gson = new Gson();
        Version version = gson.fromJson(new FileReader(Constants.MINECRAFT_JSON.get(extension)), Version.class);
        File lwjglNativesJar = null;
        for (Version.Library library : version.libraries) {
            if (library.allowed() && library.natives != null && library.getFile().getName().startsWith("lwjgl")) {
                lwjglNativesJar = library.getFile();
            }
        }

        if (lwjglNativesJar == null) {
            this.getLogger().info(":failed extracting natives");
            return;
        }

        if (!Constants.MINECRAFT_NATIVES.exists()) {
            ZipUtil.unpack(lwjglNativesJar, Constants.MINECRAFT_NATIVES);
        }
    }
}
