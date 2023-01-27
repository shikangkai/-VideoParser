package com.holobor.main;

import com.holobor.constants.Config;
import com.holobor.helper.DatabaseHelper;
import com.holobor.infos.VideoInformation;
import com.holobor.utils.Filer;
import com.holobor.utils.VideoProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Options {

    public static void parseAndStoreVideos() {

        File dir = new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR);
        _extract(dir);
        for (File file : dir.listFiles()) {
            _clearEmptyFolder(file);
        }
    }


    private static void _extract(File root) {
//        if (needStop) { return; }
        if (root == null) { return; }
        if (root.isFile()) {
            if (root.length() < 32 * 1024) { return; }
            try {
                String videoPath = root.getAbsolutePath();
                if (!Filer.isMp4File(videoPath)) { return; }
                VideoInformation videoInformation = VideoProcessor.processVideo(videoPath);
                if (videoInformation == null) {
                    return;
                }
                File dstFile = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR, videoInformation.video.md5 + ".mp4");
                if (dstFile.exists()) {
                    root.delete();
                } else {
                    root.renameTo(dstFile);
                }
//                needStop = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return;
            }
        }

        File[] subFiles = root.listFiles();
        if (subFiles == null) { return; }
        for (File file : subFiles) {
            _extract(file);
        }


    }

    private static void _clearEmptyFolder(File root) {
        if (root != null && root.isFile() && root.getName().startsWith(".")) {
            root.delete();
        }
        if (root == null || !root.isDirectory()) { return; }
        File[] subFiles = root.listFiles();
        if (subFiles != null && subFiles.length != 0) {
            for (File file : subFiles) {
                _clearEmptyFolder(file);
            }
        }
        subFiles = root.listFiles();
        if (subFiles == null || subFiles.length == 0) {
            root.delete();
        }
    }

    public static void listUnStoredVideos() {
		File f = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR);
		for (File file : f.listFiles()) {
		    String md5 = file.getName().split("\\.")[0];
            ResultSet resultSet = null;
            try {
                resultSet = DatabaseHelper.query("select md5 from video where md5 = '" + md5 + "'");
                if (resultSet.next()) {
                    // pass
                } else {
                    System.out.println("未索引的视频：" + md5);
                    file.renameTo(new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR, file.getName()));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
    }

    public static void removeDeletedVideos() {
        try {
            ResultSet resultSet = DatabaseHelper.query("select md5, name from video where deleted = 1");
            while (resultSet.next()) {
                String md5 = resultSet.getString(1);
                File file = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR + md5 + ".mp4");
                file.renameTo(new File(Config.SRC_WORKSPACE_VIDEO_ABANDON_DIR + md5 + ".mp4"));
                System.out.println("删除视频：" + md5 + " | " + resultSet.getString(2));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public static void parseAndStorePictures() {
        File albumDir = new File(Config.SRC_WORKSPACE_IMAGE_SRC_DIR);
        for (File album : albumDir.listFiles()) {
            if (!album.isDirectory()) {
                continue;
            }

            if (album.getName().startsWith(".")) {
                continue;
            }

            int index = 1;
            File[] imageFiles = album.listFiles();
            for (File imgFile : imageFiles) {
                if (imgFile.getName().startsWith(".")) {
                    continue;
                }

                try {
                    Image image = ImageIO.read(imgFile);
                    int w = image.getWidth(null);
                    int h = image.getHeight(null);

                    int targetW = 320;

                    float scale = 1f * w / targetW;
                    w = (int) (w / scale);
                    h = (int) (h / scale);


                    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics g = bi.getGraphics();
                    g.drawImage(image, 0, 0, w, h, Color.LIGHT_GRAY, null);
                    g.dispose();
                    String suffix = "jpg";//imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1);
                    File thumbnailDir = new File(Config.SRC_WORKSPACE_IMAGE_THUMBNAIL_DIR + "/" + album.getName());

                    thumbnailDir.mkdirs();
                    String fileName = index + "." + suffix;
                    ImageIO.write(bi, suffix, new File(thumbnailDir, fileName));
                    imgFile.renameTo(new File(imgFile.getParent(), fileName));
                    index++;
                    System.out.println("正在处理：" + imgFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void move7ZFileOut(File srcDir) {

        if (!srcDir.isDirectory()) {
            System.out.println(srcDir.getAbsolutePath() + " 不是路径，停止");
            return;
        }

        for (File subDir : srcDir.listFiles()) {
            if (!subDir.isDirectory()) {
                System.out.println(subDir.getAbsolutePath() + " 不是路径，跳过");
                continue;
            }

            File[] files = subDir.listFiles();
            if (files == null) {
                System.out.println(subDir.getAbsolutePath() + " 子文件获取出错，跳过");
                continue;
            }

            if (files.length != 1) {
                System.out.println(subDir.getAbsolutePath() + " 子文件超过 1 个，跳过");
                continue;
            }

            files[0].renameTo(new File(files[0].getParentFile().getParentFile(), subDir.getName() + ".7z"));
            files = subDir.listFiles();
            if (files == null || files.length == 0) {
                subDir.delete();
            }
        }
    }


    public static void batchRename(File srcDir) {
        if (!srcDir.isDirectory()) {
            // skip for file
            return;
        }

        File[] files = srcDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            batchRename(file, file.getName());
        }
    }

    private static void batchRename(File file, String prefix) {

        System.out.println("重命名：" + file.getAbsolutePath());

        if (!file.isDirectory()) {
            // skip for file
            return;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        int index = 0;
        for (File subFile : files) {
            String fileName = subFile.getName();
            String renameFileName;
            if (subFile.isFile()) {
                int extensionIndex = fileName.lastIndexOf('.');
                if (extensionIndex == -1) {
                    renameFileName = index == 0 ? fileName : fileName + "-" + index;
                } else {
                    renameFileName = index == 0
                            ? (prefix + fileName.substring(extensionIndex))
                            : (prefix + "-" + index + fileName.substring(extensionIndex)) ;
                }

                subFile.renameTo(new File(subFile.getParentFile(), renameFileName));
                index++;
            }

            if (subFile.isDirectory()) {
                renameFileName = index == 0 ? prefix : prefix + "-" + index;
                File renamedFile = new File(subFile.getParentFile(), renameFileName);
                subFile.renameTo(renamedFile);
                batchRename(renamedFile, prefix + "_" + subFile.getName());
                index++;
            }
        }
    }
}
