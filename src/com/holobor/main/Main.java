package com.holobor.main;

import com.holobor.constants.Config;
import com.holobor.infos.VideoInformation;
import com.holobor.utils.VideoProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    private static boolean needStop = false;
	public static void main(String[] args) {
		File dir = new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR);
		extract(dir);
		for (File file : dir.listFiles()) {
            clearEmptyFolder(file);
        }

        ////////
        /*
        try {

		    String dstPath = Config.SRC_WORKSPACE_VIDEO_CATEGORY_DIR;
            ResultSet resultSet = DatabaseHelper.query("select md5, tag.name as tname, title, ext from " +
                    "(select video.md5 as md5, video.title as title, video.extension as ext, video_tag.tag_id as tid from video_tag left join video on video_tag.video_id = video.id) A " +
                    "left join tag " +
                    "on A.tid = tag.id");

            while (resultSet != null && resultSet.next()) {
                String videoMd5 = resultSet.getString(1);
                String tagName = resultSet.getString(2);
                tagName.replace(":", "-");

                String srcVideoName = String.format("%s.mp4", videoMd5);
                String videoName = String.format("%s.%s", resultSet.getString(3), resultSet.getString(4));

                File tagDir = new File(dstPath, tagName);
                if (!tagDir.exists()) {
                    tagDir.mkdirs();
                }
                // make hard link
                try {
                    Files.createLink(new File(tagDir, videoName).toPath(), new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR, srcVideoName).toPath());
                } catch (FileAlreadyExistsException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

//        genPictureThumbnails();

    }

    private static void extract(File root) {
	    if (needStop) { return; }
	    if (root == null) { return; }
	    if (root.isFile()) {
            if (root.length() < 32 * 1024) { return; }
            try {
                String videoPath = root.getAbsolutePath();
                if (!"mp4".equals(videoPath.substring(videoPath.lastIndexOf('.') + 1))) { return; }
                VideoInformation videoInformation = VideoProcessor.processVideo(videoPath);
                if (videoInformation == null) {
                    return;
                }
                File dstFile = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR, videoInformation.video.md5 + videoPath.substring(videoPath.lastIndexOf('.')));
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
	        extract(file);
        }


    }

    private static void clearEmptyFolder(File root) {
	    if (root != null && root.isFile() && root.getName().startsWith(".")) {
	        root.delete();
        }
	    if (root == null || !root.isDirectory()) { return; }
	    File[] subFiles = root.listFiles();
	    if (subFiles != null && subFiles.length != 0) {
	        for (File file : subFiles) {
	            clearEmptyFolder(file);
            }
        }
        subFiles = root.listFiles();
        if (subFiles == null || subFiles.length == 0) {
            root.delete();
        }
    }

    private static void genPictureThumbnails() {
        File imgDir = new File(Config.SRC_WORKSPACE_IMAGE_SRC_DIR);
        for (File img : imgDir.listFiles()) {
            if (!img.isDirectory()) {
                continue;
            }

            if (img.getName().startsWith(".")) {
                continue;
            }

            int index = 1;
            for (File imgFile : img.listFiles()) {
                if (imgFile.getName().startsWith(".")) {
                    continue;
                }

                try {
                    Image image = ImageIO.read(imgFile);
                    int w = image.getWidth(null);
                    int h = image.getHeight(null);

                    int targetW = 400;
                    int targetH = 400;

                    float scale = 1f * w / targetW < 1f * h / targetH ?
                            1f * w / targetW : 1f * h / targetH;
                    w = (int) (w / scale);
                    h = (int) (h / scale);


                    BufferedImage bi = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
                    Graphics g = bi.getGraphics();
                    g.drawImage(image, -(w - targetW) / 2, -(h - targetH) / 2, w, h, Color.LIGHT_GRAY, null);
                    g.dispose();
                    String suffix = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1);
                    File thumbnailDir = new File(Config.SRC_WORKSPACE_IMAGE_THUMBNAIL_DIR + "/" + img.getName());
                    thumbnailDir.mkdirs();
                    ImageIO.write(bi, suffix, new File(thumbnailDir, index + "." + suffix));
                    index++;
                    System.out.println(String.format("w=%d, h=%d", w, h));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
