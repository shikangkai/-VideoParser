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

    public static void deleteDir(File dir) {
        if (!dir.isDirectory()) {
            System.out.println(dir + " 不是文件夹，skip");
            return;
        }

        File[] subFiles = dir.listFiles();
        for (File subFile : subFiles) {
            if (!subFile.delete()) {
                System.out.println(subFile + " 文件删除失败，skip");
                return;
            }
        }

        if (!dir.delete()) {
            System.out.println(dir + " 文件夹删除失败，skip");
        } else {
            System.out.println(dir + " 文件夹删除成功");
        }
    }
}
