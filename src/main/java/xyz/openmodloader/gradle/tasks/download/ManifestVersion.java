package xyz.openmodloader.gradle.tasks.download;

import java.util.ArrayList;
import java.util.List;

public class ManifestVersion {

    List<Versions> versions = new ArrayList<>();

    class Versions{
        public String id, url;
    }
}
