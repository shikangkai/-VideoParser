package com.holobor.utils;

import com.holobor.constants.Config;
import com.holobor.helper.DatabaseHelper;
import com.holobor.infos.Tag;
import com.holobor.infos.Video;
import com.holobor.infos.VideoInformation;
import com.holobor.infos.VideoTag;
//import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VideoProcessor {

    public static VideoInformation processVideo(String videoPath) throws Exception {

        FFmpegFrameGrabber videoFileGrabber = new FFmpegFrameGrabber(videoPath);
        File videoFile = new File(videoPath);

        videoFileGrabber.start();

        long videoFileLength = videoFile.length();
        if (videoFileLength == 0) {
            throw new IOException("Error " + videoPath + " length = 0");
        }

        String title = Filer.getNameWithoutExtension(videoPath);
        ResultSet resultSet = DatabaseHelper.query("select duration_ms from video where title = '" + title.replace('\'', '‘') + "'");
        if (resultSet.next()) {
            if (Math.abs(videoFileGrabber.getLengthInTime() / 1000L - resultSet.getLong(1)) < 1000L) {
                System.out.println("file " + title + " already exist!!!");
                File target = new File(Config.SRC_WORKSPACE_VIDEO_DUP_DIR, new File(videoPath).getName());
                int prefixOrder = 1;
                while (target.exists()); {
                    target = new File(Config.SRC_WORKSPACE_VIDEO_DUP_DIR, String.format("%d-%s", prefixOrder, new File(videoPath).getName()));
                    prefixOrder++;
                }
                new File(videoPath).renameTo(target);
                throw new FileAlreadyExistsException(title);
            }
        }
        String videoFileMd5 = Md5Encoder.genFileMd5(videoPath);

        if (new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR, videoFileMd5 + ".mp4").exists()) {
            System.out.println("file " + videoFileMd5 + " already exist!!!");
            new File(videoPath).renameTo(new File(Config.SRC_WORKSPACE_VIDEO_DUP_DIR, new File(videoPath).getName()));
            throw new FileAlreadyExistsException(videoFileMd5);
        }

        if (videoFileGrabber.getVideoCodec() != 27 /* avcodec.AV_CODEC_ID_H264 */ ||
                (videoFileGrabber.getAudioCodec() != 86018 /* avcodec.AV_CODEC_ID_AAC */ && videoFileGrabber.getAudioCodec() != 0)) {
            // 条件不满足
            videoFileGrabber.stop();
            videoFileGrabber.release();
            return null;
        }

        // 视频基本信息
        Video video = new Video(
                videoFileMd5,
                videoFileLength,
                videoFileGrabber.getLengthInTime() / 1000,
                videoFileGrabber.getImageWidth(),
                videoFileGrabber.getImageHeight(),
                videoFileGrabber.getFrameRate(),
                videoFileGrabber.getVideoBitrate(),
                videoFileGrabber.getAudioBitrate(),
                videoFileGrabber.getFormat(),
                title,
                videoFile.lastModified(),
                videoFile.getName().substring(videoFile.getName().lastIndexOf('.') + 1).toLowerCase(Locale.ROOT));

        // 生成截图
        File tmpFile;
        tmpFile = new File(Config.SRC_WORKSPACE_THUMBNAIL_IMG_DIR, videoFileMd5 + ".jpg");
        if (tmpFile.exists()) { tmpFile.delete(); }
        JpgMaker.make(videoFileGrabber, tmpFile);

        // 生成动图
        tmpFile = new File(Config.SRC_WORKSPACE_THUMBNAIL_GIF_DIR + videoFileMd5 + ".gif");
        if (tmpFile.exists()) { tmpFile.delete(); }
        GifMaker.make(videoFileGrabber, tmpFile);

        videoFileGrabber.stop();
        videoFileGrabber.release();

        video.insertIfNeeded();
        int videoId = video.getId();


        File rootDir = new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR);
        tmpFile = videoFile.getParentFile();
        List<String> tagNames = new ArrayList<String>();

        while (!rootDir.equals(tmpFile)) {

            tagNames.add(tmpFile.getName());
            tmpFile = tmpFile.getParentFile();
        }

        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = new Tag(tagName, null);
            tags.add(tag);
            tag.insertIfNeeded();
            VideoTag videoTag = new VideoTag(videoId, tag.getId());
            videoTag.insertIfNeeded();
        }

        return new VideoInformation(video, tags);
    }
}
