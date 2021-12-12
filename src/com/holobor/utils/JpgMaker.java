package com.holobor.utils;

import com.holobor.constants.Config;
import com.sun.xml.internal.bind.v2.model.core.EnumLeafInfo;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class JpgMaker {

    public static void make(FFmpegFrameGrabber videoFileGrabber, File jpgFile) throws IOException {

        if (jpgFile.exists()) { jpgFile.delete(); }

        Java2DFrameConverter converter = new Java2DFrameConverter();
        long frameNumber = videoFileGrabber.getLengthInVideoFrames();
        videoFileGrabber.setVideoFrameNumber((int) (frameNumber / 3));
        Frame frame = null;
        do {
            frame = videoFileGrabber.grabKeyFrame();
        } while (frame.image == null);

        ImageIO.write(
                converter.convert(frame),
                Config.THUMBNAIL_FORMAT,
                jpgFile);
    }

    public static void make(File videoFile, File jpgFile) throws IOException {
        FFmpegFrameGrabber videoFileGrabber = new FFmpegFrameGrabber(videoFile.getAbsolutePath());
        videoFileGrabber.start();

        make(videoFileGrabber, jpgFile);

        videoFileGrabber.stop();
        videoFileGrabber.close();
    }
}
