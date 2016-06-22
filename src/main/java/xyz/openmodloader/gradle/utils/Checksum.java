package xyz.openmodloader.gradle.utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class Checksum {

    public static boolean sameChecksum(File file, String checksum) {
        try {
            if (file == null)
                return false;
            HashCode hash = Files.hash(file, Hashing.sha1());
            StringBuffer sb = new StringBuffer("");
            for (Byte hashBytes : hash.asBytes()) {
                sb.append(Integer.toString((hashBytes & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString().equals(checksum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
