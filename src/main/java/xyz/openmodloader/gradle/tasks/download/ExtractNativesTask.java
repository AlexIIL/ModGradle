package xyz.openmodloader.gradle.tasks.download;

import com.google.gson.Gson;
import xyz.openmodloader.gradle.MinecraftExtension;
import xyz.openmodloader.gradle.utils.FileLocations;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by mark on 20/06/2016.
 */
public class ExtractNativesTask extends AbstractTask {

	@TaskAction
	public void extractNatives () throws FileNotFoundException {
		MinecraftExtension.version = "1.9.4";
		Gson gson = new Gson();
		Version version = gson.fromJson(new FileReader(FileLocations.minecraftJson), Version.class);
		File lwjglNativesJar = null;
		for(Version.Library library : version.libraries){
			if(library.allowed() && library.natives != null && library.getFile().getName().startsWith("lwjgl")){
				lwjglNativesJar = library.getFile();
			}
		}

		if(!FileLocations.MINECRAFT_NATIVES.exists()){
			ZipUtil.unpack(lwjglNativesJar, FileLocations.MINECRAFT_NATIVES);
		}
	}
}
