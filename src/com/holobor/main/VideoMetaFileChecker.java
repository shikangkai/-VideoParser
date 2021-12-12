package com.holobor.main;

import com.holobor.constants.Config;
import com.holobor.helper.DatabaseHelper;
import com.holobor.utils.GifMaker;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VideoMetaFileChecker {

    public static void main(String[] args) {
        checkAndReGen();
    }

    public static void checkAndReGen() {
        File videoDir = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR);
        for (File videoFile : videoDir.listFiles()) {
            String videoFileName = videoFile.getName();
            String fileMd5 = videoFileName.substring(0, videoFileName.length() - 4);

            if (videoFileName.endsWith("MP4")) {
                System.out.println(videoFileName);
            }

            // check JPG
            File jpgFile = new File(Config.SRC_WORKSPACE_THUMBNAIL_IMG_DIR, fileMd5 + ".jpg");
            if (!jpgFile.exists()) {
                System.out.println(fileMd5 + "'s jpg not exists");
            }

            // check GIF
            File gifFile = new File(Config.SRC_WORKSPACE_THUMBNAIL_GIF_DIR, fileMd5 + ".gif");
            if (!gifFile.exists() || gifFile.length() < 10 * 1024) { // 小于 10K 则重新生成
                if (gifFile.exists()) { gifFile.delete(); }

                System.out.println(fileMd5 + "'s gif not exists");
                try {
                    GifMaker.make(videoFile, gifFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // check in Database
            try {
                ResultSet resultSet = DatabaseHelper.query(String.format("select * from video where md5='%s'", fileMd5));
                if (resultSet.next()) {
                    // ok
                } else {
                    System.out.println(fileMd5 + "'s database record not exists");
                    videoFile.renameTo(new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR, videoFileName));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
