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
        if (!Constants.MINECRAFT_NATIVES.exists())
            for (File source : getProject().getConfigurations().getByName(Constants.CONFIG_NATIVES))
                ZipUtil.unpack(source, Constants.MINECRAFT_NATIVES);
    }
}
