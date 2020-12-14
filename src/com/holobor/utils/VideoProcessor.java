package com.holobor.utils;

import com.holobor.constants.Config;
import com.holobor.infos.Tag;
import com.holobor.infos.Video;
import com.holobor.infos.VideoInformation;
import com.holobor.infos.VideoTag;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class VideoProcessor {

    public static VideoInformation processVideo(String videoPath) throws Exception {

        FFmpegFrameGrabber videoFileGrabber = null;
        File videoFile = new File(videoPath);
        long videoFileLength = videoFile.length();
        if (videoFileLength == 0) {
            throw new IOException("Error " + videoPath + " length = 0");
        }

        String title = videoFile.getName().substring(0, videoFile.getName().lastIndexOf('.'));
        videoFileGrabber =  FFmpegFrameGrabber.createDefault(videoFile);
        String videoFileMd5 = Md5Encoder.genFileMd5(videoPath);

        videoFileGrabber.start();

        if (videoFileGrabber.getVideoCodec() != avcodec.AV_CODEC_ID_H264 ||
                (videoFileGrabber.getAudioCodec() != avcodec.AV_CODEC_ID_AAC && videoFileGrabber.getAudioCodec() != 0)) {
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
                videoFile.getName().substring(videoFile.getName().lastIndexOf('.') + 1));

        // 生成截图
        File tmpFile;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        long frameNumber = videoFileGrabber.getLengthInVideoFrames();
        videoFileGrabber.setVideoFrameNumber((int) (frameNumber / 3));
        Frame frame = videoFileGrabber.grabKeyFrame();
        tmpFile = new File(Config.SRC_WORKSPACE_THUMBNAIL_IMG_DIR, videoFileMd5 + ".jpg");
        if (tmpFile.exists()) { tmpFile.delete(); }
        ImageIO.write(
                converter.convert(frame),
                Config.THUMBNAIL_FORMAT,
                tmpFile);

        // 生成动图
        ScaleUtils.Size imgSize = ScaleUtils.scaleSize(new ScaleUtils.Size(videoFileGrabber.getImageWidth(), videoFileGrabber.getImageHeight()));
        videoFileGrabber.setImageWidth(imgSize.width);
        videoFileGrabber.setImageHeight(imgSize.height);

        tmpFile = new File(Config.SRC_WORKSPACE_THUMBNAIL_GIF_DIR + videoFileMd5 + ".gif");
        if (tmpFile.exists()) { tmpFile.delete(); }
        videoFileGrabber.setFrameNumber(0);
        AnimatedGifEncoder gifMaker = new AnimatedGifEncoder();

        final int gifFps = 8;
        final int videoFps = (int) Math.round(videoFileGrabber.getFrameRate());
        final int gifSampleInterval = videoFps / gifFps;
        gifMaker.setFrameRate(gifFps);
        gifMaker.start(tmpFile.getAbsolutePath());

        // 每10秒取1秒，最多取15秒，每秒取8帧
        for (int offset = 10 * videoFps; offset < frameNumber - videoFps && offset < 10 * videoFps * 15; offset += (10 * videoFps)) {
            for (int i = 0; i < gifFps; i++) {
                videoFileGrabber.setFrameNumber(offset + i * gifSampleInterval);
                gifMaker.addFrame(converter.convert(videoFileGrabber.grabImage()));
            }
        }
        gifMaker.finish();

        videoFileGrabber.stop();
        videoFileGrabber.release();

        video.insertIfNeeded();
        int videoId = video.getId();

        String[] tagNames = videoFile.getAbsolutePath().replace(Config.SRC_WORKSPACE_VIDEO_SRC_DIR, "").split("/");
        Tag[] tags = null;
        if (tagNames == null || tagNames.length == 0) {
            Tag tag = new Tag("v.a.", null);
            tags = new Tag[] { tag };
            tag.insertIfNeeded();
            VideoTag videoTag = new VideoTag(videoId, tag.getId());
            videoTag.insertIfNeeded();
        } else {
            tags = new Tag[tagNames.length - 1];
            for (int i = 0; i < tagNames.length - 1; i++) {
                Tag tag = new Tag(tagNames[i], null);
                tags[i] = tag;
                tag.insertIfNeeded();
                VideoTag videoTag = new VideoTag(videoId, tag.getId());
                videoTag.insertIfNeeded();
            }
        }

        return new VideoInformation(video, tags);
    }
}
