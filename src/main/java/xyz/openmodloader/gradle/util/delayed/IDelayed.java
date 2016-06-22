package xyz.openmodloader.gradle.util.delayed;

import xyz.openmodloader.gradle.ModGradleExtension;

public interface IDelayed<T> {
    T get(ModGradleExtension extension);
}
