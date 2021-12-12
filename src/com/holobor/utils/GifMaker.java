package com.holobor.utils;

import com.holobor.constants.Config;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GifMaker {

    public static void make(FFmpegFrameGrabber videoFileGrabber, File gifFile) throws IOException {

        if (gifFile.exists()) { gifFile.delete(); }

        Java2DFrameConverter converter = new Java2DFrameConverter();

        // 生成动图
        ScaleUtils.Size imgSize = ScaleUtils.scaleSize(new ScaleUtils.Size(videoFileGrabber.getImageWidth(), videoFileGrabber.getImageHeight()));
        videoFileGrabber.setImageWidth(imgSize.width);
        videoFileGrabber.setImageHeight(imgSize.height);

        videoFileGrabber.setFrameNumber(0);
        AnimatedGifEncoder gifMaker = new AnimatedGifEncoder();

        final int gifFps = 8;
        final int videoFps = (int) Math.round(videoFileGrabber.getFrameRate());
        final int gifSampleInterval = videoFps / gifFps;
        final long frameNumber = videoFileGrabber.getLengthInVideoFrames();

        gifMaker.setFrameRate(gifFps);
        gifMaker.start(gifFile.getAbsolutePath());

        // 每10秒取1秒，最多取15秒，每秒取8帧

        int gifVideoFrameOffset;
        if (10 * videoFps < frameNumber - videoFps) {
            gifVideoFrameOffset = 10 * videoFps;
        } else {
            if (frameNumber > 2 * videoFps) {
                gifVideoFrameOffset = (int) (frameNumber - videoFps * 2);
            } else {
                gifVideoFrameOffset = 0;
            }
        }
        for (int offset = gifVideoFrameOffset;
             offset < frameNumber - videoFps && offset < 10 * videoFps * 15;
             offset += (10 * videoFps)) {

            for (int i = 0; i < gifFps; i++) {
                videoFileGrabber.setFrameNumber(offset + i * gifSampleInterval);
                while (true) {
                    Frame frame = videoFileGrabber.grabFrame();
                    if (frame.image != null) {
                        gifMaker.addFrame(converter.convert(frame));
                        break;
                    }
                }
            }
        }
        gifMaker.finish();
    }

    public static void make(File videoFile, File gifFile) throws IOException {
        FFmpegFrameGrabber videoFileGrabber = new FFmpegFrameGrabber(videoFile.getAbsolutePath());
        videoFileGrabber.start();

        make(videoFileGrabber, gifFile);

        videoFileGrabber.stop();
        videoFileGrabber.close();
    }
}
