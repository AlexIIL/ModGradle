package xyz.openmodloader.gradle.util.delayed;

import xyz.openmodloader.gradle.ModGradleExtension;

import java.io.File;
import java.util.function.Function;

public class DelayedFile implements IDelayed<File> {
    private File file;
    private Function<ModGradleExtension, File> function;

    public DelayedFile(Function<ModGradleExtension, File> function) {
        this.function = function;
    }

    @Override
    public File get(ModGradleExtension extension) {
        if (this.file == null) {
            this.file = this.function.apply(extension);
        }
        return this.file;
    }
}
