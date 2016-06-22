package xyz.openmodloader.gradle.tasks.download;

import com.google.gson.JsonObject;
import xyz.openmodloader.gradle.utils.FileLocations;
import xyz.openmodloader.gradle.utils.MojangConstants;
import xyz.openmodloader.gradle.utils.OSUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Version {

    public List<Library> libraries;
    public HashMap<String, Downloads> downloads;
    public AssetIndex assetIndex;

    public class Downloads {
        public String url;
        public String sha1;
        public int size;
    }

    public class AssetIndex {
        public String id;
        public String sha1;
        public String url;
    }

    public class Library {

        public String name;
        public JsonObject natives;
        public JsonObject downloads;

        public Rule[] rules;

        public String getURL() {
            String path;
            String[] parts = this.name.split(":", 3);
            path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + getClassifier() + ".jar";
            return MojangConstants.LIBRARIES_BASE.getURL(path);
        }


        public File getFile() {
            String[] parts = this.name.split(":", 3);
            return new File(FileLocations.MINECRAFT_LIBS, parts[1] + "-" + parts[2] + getClassifier() + ".jar");
        }

        public String getSha1() {
            if (this.downloads == null)
                return "";

            if (this.downloads.getAsJsonObject("artifact") == null)
                return "";

            if (this.downloads.getAsJsonObject("artifact").get("sha1") == null)
                return "";
            return this.downloads.getAsJsonObject("artifact").get("sha1").getAsString();
        }

        public String getClassifier() {
            if (natives == null) {
                return "";
            }
            return "-" + natives.get(OSUtils.getOS().replace("${arch}", OSUtils.getArch())).getAsString().replace("\"", "");
        }

        public boolean allowed() {
            if (this.rules == null || this.rules.length <= 0)
                return true;

            boolean success = false;
            for (Rule rule : this.rules) {
                if (rule.os != null && rule.os.name != null) {
                    if (rule.os.name.equalsIgnoreCase(OSUtils.getOS()))
                        return rule.action.equalsIgnoreCase("allow");
                } else
                    success = rule.action.equalsIgnoreCase("allow");

            }
            return success;
        }
    }

    private class Rule {
        public String action;
        public OS os;

        private class OS {
            String name;
        }
    }
}
