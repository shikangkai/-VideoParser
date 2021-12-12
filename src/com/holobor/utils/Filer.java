package com.holobor.utils;

import java.io.File;

public class Filer {

    public static String getExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
    }

    public static boolean isMp4File(String filePath) {
        return "mp4".equals(getExtension(filePath));
    }

    public static String getNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        return file.getName().substring(0, file.getName().lastIndexOf('.'));
    }
}
